package io.github.rubixtheslime.rubix.gaygrass;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.sun.jna.Memory;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RubixMod;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.util.Identifier;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.ByteBufferSeekableByteChannel;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class FlagBufferGetter {
    private final ResourceFinder finder;
    private final boolean canAntialias;

    FlagBufferGetter(String extension, boolean canAntialias) {
        finder = new ResourceFinder("flags", "." + extension);
        this.canAntialias = canAntialias;
    }

    public static FlagBufferGetter of(String format) throws RuntimeException {
        if (Objects.equals(format, "vector")) return SVG_GETTER;
        if (Objects.equals(format, "pixel")) return PNG_GETTER;
        if (Objects.equals(format, "video") && EnabledMods.GAY_GRASS_VIDEO) return MP4_GETTER;
        throw new RuntimeException("invalid format name: %s".formatted(format));
    }

    public Identifier toResourcePath(Identifier identifier) {
        return finder.toResourcePath(identifier);
    }

    public abstract FlagBuffer build(Resource resource, Identifier identifier) throws RuntimeException;

    private static final FlagBufferGetter SVG_GETTER = new FlagBufferGetter("svg", true) {
        @Override
        public FlagBuffer build(Resource resource, Identifier identifier) throws RuntimeException {
            try {
                var stream = resource.getInputStream();
                var loader = new SVGLoader();
                var svgDocument = loader.load(stream, null, LoaderContext.createDefault());
                stream.close();
                if (svgDocument == null) {
                    throw new RuntimeException("null svg document");
                }
                return new FlagBuffer.Vector(identifier, svgDocument);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private static final FlagBufferGetter PNG_GETTER = new FlagBufferGetter("png", false) {
        @Override
        public FlagBuffer build(Resource resource, Identifier identifier) throws RuntimeException {
            try {
                InputStream stream = resource.getInputStream();
                var image = ImageIO.read(stream);
                if (image == null) {
                    throw new RuntimeException("null png image");
                }
                return new FlagBuffer.Pixel(identifier, image);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private static final FlagBufferGetter MP4_GETTER = new FlagBufferGetter("mp4", false) {
        @Override
        public FlagBuffer build(Resource resource, Identifier identifier) throws RuntimeException {
            try {
                InputStream stream = resource.getInputStream();
                var byteBuffer = ByteBuffer.wrap(stream.readAllBytes());
                var seekableChannel = ByteBufferSeekableByteChannel.readFromByteBuffer(byteBuffer);

                var frameGrab = FrameGrab.createFrameGrab(seekableChannel);
                var track = (AbstractMP4DemuxerTrack)frameGrab.getVideoTrack();

                return new FlagBuffer.Video(identifier, track.getFrameCount(), (int) track.getTimescale(), frameGrab);
            } catch (IOException | JCodecException e) {
                throw new RuntimeException(e);
            }
        }
    };

}
