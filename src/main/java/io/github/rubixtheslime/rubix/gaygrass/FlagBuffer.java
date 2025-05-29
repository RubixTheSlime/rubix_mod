package io.github.rubixtheslime.rubix.gaygrass;

import com.github.weisj.jsvg.SVGDocument;
import io.github.rubixtheslime.rubix.RubixMod;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import net.minecraft.util.Identifier;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public abstract class FlagBuffer {
    private final Identifier identifier;

    protected FlagBuffer(Identifier identifier) {
        this.identifier = identifier;
    }

    public abstract void draw(BufferedImage image, AffineTransform transform, Object antialiasKey, float opacity, FlagInstance.AnimationKey animationKey);
    public abstract double height();
    public abstract double width();

    public Identifier getIdentifier() {
        return identifier;
    }

    public Animated asAnimated() {
        return null;
    }

    public interface Animated {
        void pause();
        void play();
        void restart();
        void setTime(long millis);
    }

    public static class Vector extends FlagBuffer {
        private final SVGDocument svgDocument;

        public Vector(Identifier identifier, SVGDocument svgDocument) {
            super(identifier);
            this.svgDocument = svgDocument;
        }

        @Override
        public void draw(BufferedImage image, AffineTransform transform, Object antialiasKey, float opacity, FlagInstance.AnimationKey animationKey) {
            BufferedImage tmpImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
            Graphics2D g = tmpImage.createGraphics();
            g.transform(transform);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasKey);
            svgDocument.render(null, g);
            g.dispose();
            g = image.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
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
    }

    public static class Pixel extends FlagBuffer {
        private final BufferedImage bufferedImage;

        protected Pixel(Identifier identifier, BufferedImage bufferedImage) {
            super(identifier);
            this.bufferedImage = bufferedImage;
        }

        @Override
        public void draw(BufferedImage image, AffineTransform transform, Object antialiasKey, float opacity, FlagInstance.AnimationKey animationKey) {
            Graphics2D g = image.createGraphics();
            g.transform(transform);
            // pixel format does not allow antialiasing
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.drawImage(bufferedImage, 0, 0, null);
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

        public Video(Identifier identifier, long frameCount, int timescale, FrameGrab frameGrab) {
            super(identifier);
            this.frameCount = frameCount;
            this.timescale = timescale;
            this.frameGrab = frameGrab;
            var size = frameGrab.getMediaInfo().getDim();
            this.bounds = new Rectangle2D.Double(0, 0, size.getWidth(), size.getHeight());
        }

        @Override
        public void draw(BufferedImage image, AffineTransform transform, Object antialiasKey, float opacity, FlagInstance.AnimationKey animationKey) {
            if (animationKey == FlagInstance.AnimationKey.ACTUAL) {
                if (bufferedImage == null) return;
                Graphics2D g = image.createGraphics();
                g.transform(transform);
                // pixel format does not allow antialiasing
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g.drawImage(bufferedImage, 0, 0, null);
                g.dispose();
            } else {
                Graphics2D g = image.createGraphics();
                g.transform(transform);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
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

        @Override
        public Animated asAnimated() {
            return this;
        }
    }
}
