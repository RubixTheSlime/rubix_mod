package io.github.rubixtheslime.rubix.gaygrass;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import io.github.rubixtheslime.rubix.EnabledMods;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.util.Identifier;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.ByteBufferSeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

public abstract class FlagBuffer {

    static final Getter SVG_GETTER = new Getter("svg", true) {
        @Override
        public FlagBuffer build(Resource resource, Identifier identifier, JsonFlagEntry flagEntry) throws RuntimeException {
            try {
                var stream = resource.getInputStream();
                var loader = new SVGLoader();
                var svgDocument = loader.load(stream, null, LoaderContext.createDefault());
                stream.close();
                if (svgDocument == null) {
                    throw new RuntimeException("null svg document");
                }
                return new Vector(identifier, flagEntry, svgDocument);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };
    static final Getter PNG_GETTER = new Getter("png", false) {
        @Override
        public FlagBuffer build(Resource resource, Identifier identifier, JsonFlagEntry flagEntry) throws RuntimeException {
            try {
                InputStream stream = resource.getInputStream();
                var image = ImageIO.read(stream);
                if (image == null) {
                    throw new RuntimeException("null png image");
                }
                return new Pixel(identifier, flagEntry, image);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };
    static final Getter MP4_GETTER = new Getter("mp4", false) {
        @Override
        public FlagBuffer build(Resource resource, Identifier identifier, JsonFlagEntry flagEntry) throws RuntimeException {
            try {
                InputStream stream = resource.getInputStream();
                var byteBuffer = ByteBuffer.wrap(stream.readAllBytes());
                var seekableChannel = ByteBufferSeekableByteChannel.readFromByteBuffer(byteBuffer);

                var frameGrab = FrameGrab.createFrameGrab(seekableChannel);
                var track = (AbstractMP4DemuxerTrack)frameGrab.getVideoTrack();

                return new Video(identifier, flagEntry, track.getFrameCount(), (int) track.getTimescale(), frameGrab);
            } catch (IOException | JCodecException e) {
                throw new RuntimeException(e);
            }
        }
    };

    protected FlagBuffer(Identifier identifier, JsonFlagEntry flagEntry) {
    }

    public abstract void draw(BufferedImage image, AffineTransform transform, FlagInstance.AnimationKey animationKey, FlagInstance instance);
    public abstract double height();
    public abstract double width();
    public abstract Rectangle2D bounds();

    public void postDraw(Graphics2D g, FlagInstance instance, float opacity) {
        if (instance.flagData.randomColorAlpha > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.setPaint(instance.getShade());
            g.fill(bounds());
        }
    }

    public interface Animated {
        void pause();
        void play();
        void restart();
        void setTime(long millis);
    }

    public static class Vector extends FlagBuffer {
        private final SVGDocument svgDocument;

        public Vector(Identifier identifier, JsonFlagEntry flagEntry, SVGDocument svgDocument) {
            super(identifier, flagEntry);
            this.svgDocument = svgDocument;
        }

        @Override
        public void draw(BufferedImage image, AffineTransform transform, FlagInstance.AnimationKey animationKey, FlagInstance instance) {
            BufferedImage tmpImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
            Graphics2D g = tmpImage.createGraphics();
            g.transform(transform);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, instance.flagData.antialiasKey);
            svgDocument.render(null, g);
            postDraw(g, instance, 1);
            g.dispose();
            g = image.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, instance.flagData.opacity));
            g.drawImage(tmpImage, 0, 0, null);
            g.dispose();
        }

        @Override
        public double height() {
            return svgDocument.size().height;
        }

        @Override
        public double width() {
            return svgDocument.size().width;
        }

        @Override
        public Rectangle2D bounds() {
            var size = svgDocument.size();
            return new Rectangle2D.Double(0, 0, size.width, size.height);
        }
    }

    public static class Pixel extends FlagBuffer {
        private final BufferedImage bufferedImage;

        protected Pixel(Identifier identifier, JsonFlagEntry flagEntry, BufferedImage bufferedImage) {
            super(identifier, flagEntry);
            this.bufferedImage = bufferedImage;
        }

        @Override
        public void draw(BufferedImage image, AffineTransform transform, FlagInstance.AnimationKey animationKey, FlagInstance instance) {
            Graphics2D g = image.createGraphics();
            g.transform(transform);
            // pixel format does not allow antialiasing
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, instance.flagData.opacity));
            g.drawImage(bufferedImage, 0, 0, null);
            postDraw(g, instance, instance.flagData.opacity);
            g.dispose();
        }

        @Override
        public double height() {
            return bufferedImage.getHeight();
        }

        @Override
        public double width() {
            return bufferedImage.getWidth();
        }

        @Override
        public Rectangle2D bounds() {
            return bufferedImage.getRaster().getBounds();
        }
    }

    public static class Video extends FlagBuffer implements Animated {
        private static final int QUEUE_SIZE = 16;

        private final long frameCount;
        private final int timescale;
        private final FrameGrab frameGrab;
        private final SortedMap<Double, Picture> queue = new Double2ObjectRBTreeMap<>();
        private final Rectangle2D bounds;

        private long startTimeMillis;
        private long pauseTimeMillis;
        private long sysTimeMillis;
        private int lastFrame = 0;
        private boolean paused;
        private BufferedImage bufferedImage = null;

        public Video(Identifier identifier, JsonFlagEntry flagEntry, long frameCount, int timescale, FrameGrab frameGrab) {
            super(identifier, flagEntry);
            this.frameCount = frameCount;
            this.timescale = timescale;
            this.frameGrab = frameGrab;
            var size = frameGrab.getMediaInfo().getDim();
            this.bounds = new Rectangle2D.Double(0, 0, size.getWidth(), size.getHeight());
        }

        @Override
        public void draw(BufferedImage image, AffineTransform transform, FlagInstance.AnimationKey animationKey, FlagInstance instance) {
            if (animationKey == FlagInstance.AnimationKey.ACTUAL) {
                if (bufferedImage == null) return;
                Graphics2D g = image.createGraphics();
                g.transform(transform);
                // pixel format does not allow antialiasing
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, instance.flagData.opacity));
                g.drawImage(bufferedImage, 0, 0, null);
                postDraw(g, instance, instance.flagData.opacity);
                g.dispose();
            } else {
                Graphics2D g = image.createGraphics();
                g.transform(transform);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, instance.flagData.opacity));
                g.setPaint(animationKey == FlagInstance.AnimationKey.BLACK ? Color.BLACK : Color.WHITE);
                g.fill(bounds);
                g.dispose();
            }
        }

        @Override
        public double height() {
            return bounds.getHeight();
        }

        @Override
        public double width() {
            return bounds.getWidth();
        }

        @Override
        public Rectangle2D bounds() {
            return bounds;
        }

        @Override
        public void pause() {
            if (paused) return;
            pauseTimeMillis = sysTimeMillis - startTimeMillis;
            paused = true;
        }

        @Override
        public void play() {
            if (!paused) return;
            paused = false;
            startTimeMillis = sysTimeMillis - pauseTimeMillis;
        }

        @Override
        public void restart() {
            pauseTimeMillis = 0;
            lastFrame = 0;
            startTimeMillis = sysTimeMillis;
            queue.clear();
            try {
                frameGrab.seekToFramePrecise(0);
            } catch (Throwable ignored) {}
        }

        @Override
        public void setTime(long millis) {
            sysTimeMillis = millis;
            if (paused) return;
            long frame = (millis - startTimeMillis) * timescale / 32000;
            if (frame >= frameCount || frame < 0) {
                frame = 0;
                restart();
            }
            Picture pic = null;
            for (int i = 0; i < frame - lastFrame; i++) {
                pic = advance();
            }
            lastFrame = (int) frame;
            if (pic == null) return;
            bufferedImage = AWTUtil.toBufferedImage(pic);
//            try {
//                AWTUtil.toBufferedImage(pic, bufferedImage);
//            } catch (Throwable ignored) {
//            }
        }
        
        private Picture advance() {
            try {
                while (queue.size() < QUEUE_SIZE) {
                    var picWithMetadata = frameGrab.getNativeFrameWithMetadata();
                    if (picWithMetadata == null) break;
                    var pic1 = picWithMetadata.getPicture();
                    var pic2 = pic1.createCompatible();
                    pic2.copyFrom(pic1);
                    queue.put(picWithMetadata.getTimestamp(), pic2);
                }
            } catch (IOException ignored) {
            }
            var res = queue.pollFirstEntry();
            return res == null ? null : res.getValue();
        }

    }

    public abstract static class Getter {
        private final ResourceFinder finder;
        private final boolean canAntialias;

        Getter(String extension, boolean canAntialias) {
            finder = new ResourceFinder("flags", "." + extension);
            this.canAntialias = canAntialias;
        }

        public static Getter of(String format) throws RuntimeException {
            if (Objects.equals(format, "vector")) return SVG_GETTER;
            if (Objects.equals(format, "pixel")) return PNG_GETTER;
            if (Objects.equals(format, "video") && EnabledMods.GAY_GRASS_VIDEO) return MP4_GETTER;
            throw new RuntimeException("invalid format name: %s".formatted(format));
        }

        public Identifier toResourcePath(Identifier identifier) {
            return finder.toResourcePath(identifier);
        }

        public abstract FlagBuffer build(Resource resource, Identifier identifier, JsonFlagEntry flagEntry) throws RuntimeException;

    }
}
