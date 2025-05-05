package io.github.rubixtheslime.rubix.gaygrass;

import com.github.weisj.jsvg.SVGDocument;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class FlagBuffer {
    private final SVGDocument svgDocument;
    private final AffineTransform mainTransform;
    private final AffineTransform invTransform;
    private final double radius;
    private final Rectangle2D bounds;
    private final Rectangle2D docBounds;
    private final double opacity;
    private final Object antialiasKey;

    public FlagBuffer(SVGDocument svgDocument, AffineTransform mainTransform, double radius, double opacity, Object antialiasKey) {
        this.svgDocument = svgDocument;
        this.mainTransform = mainTransform;
        try {
            this.invTransform = mainTransform.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
        this.radius = radius;
        this.opacity = opacity;
        this.antialiasKey = antialiasKey;

        var size = svgDocument.size();
        this.docBounds = new Rectangle2D.Double(0, 0, size.width, size.height);
        this.bounds = getTransformedRect(mainTransform, docBounds);
    }

    private static double[] getTransformedCorners(AffineTransform transform, Rectangle2D rect) {
        var res = new double[]{
            rect.getMinX(), rect.getMinY(),
            rect.getMaxX(), rect.getMinY(),
            rect.getMinX(), rect.getMaxY(),
            rect.getMaxX(), rect.getMaxY()
//                0, 0
        };
        transform.transform(res, 0, res, 0, 4);
//            res[6] = res[2] + res[4] - res[0];
//            res[7] = res[3] + res[5] - res[1];
        return res;
    }

    private static Rectangle2D getTransformedRect(AffineTransform transform, Rectangle2D rect) {
        var corners = getTransformedCorners(transform, rect);

        int xIndex = (corners[0] > corners[2] ? 2 : 0) | (corners[0] > corners[4] ? 4 : 0);
        int yIndex = (corners[1] > corners[3] ? 3 : 1) | (corners[1] > corners[5] ? 4 : 0);
        double x = corners[xIndex];
        double y = corners[yIndex];

        return new Rectangle2D.Double(x, y, corners[6 - xIndex] - x, corners[8 - yIndex] - y);
    }

    public boolean intersects(Rectangle2D rect) {
        if (!bounds.intersects(rect)) return false;
        if (bounds.getMinX() >= rect.getMinX() && bounds.getMaxX() <= rect.getMaxX()) return true;
        if (bounds.getMinY() >= rect.getMinY() && bounds.getMaxY() <= rect.getMaxY()) return true;
        return getTransformedRect(invTransform, rect).intersects(docBounds);
    }

    public int compareZIndex(FlagBuffer o) {
        int cmp = Double.compare(o.radius, this.radius);
        if (cmp == 0) cmp = Double.compare(this.bounds.getCenterX(), o.bounds.getCenterX());
        if (cmp == 0) cmp = Double.compare(this.bounds.getCenterY(), o.bounds.getCenterY());
        return cmp;
    }

    public void applyTo(int x, int z, BufferedImage image) {
        var transform = AffineTransform.getTranslateInstance(-x, -z);
        transform.concatenate(mainTransform);
        BufferedImage tmpImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g = tmpImage.createGraphics();
        g.transform(transform);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasKey);
        svgDocument.render(null, g);
        g.dispose();
        g = image.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity));
        g.drawImage(tmpImage, 0, 0, null);
        g.dispose();
    }
}
