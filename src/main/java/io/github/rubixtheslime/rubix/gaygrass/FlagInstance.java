package io.github.rubixtheslime.rubix.gaygrass;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class FlagInstance {
    private final FlagBuffer flagBuffer;
    private final AffineTransform mainTransform;
    private final AffineTransform invTransform;
    private final double radius;
    private final Rectangle2D bounds;
    private final Rectangle2D docBounds;
    private final double opacity;
    private final Object antialiasKey;

    public FlagInstance(FlagBuffer flagBuffer, AffineTransform mainTransform, double radius, double opacity, Object antialiasKey) {
        this.flagBuffer = flagBuffer;
        this.mainTransform = mainTransform;
        try {
            this.invTransform = mainTransform.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
        this.radius = radius;
        this.opacity = opacity;
        this.antialiasKey = antialiasKey;

        this.docBounds = new Rectangle2D.Double(0, 0, flagBuffer.width(), flagBuffer.height());
        this.bounds = getTransformedRect(mainTransform, docBounds);
    }

    private static double[] getTransformedCorners(AffineTransform transform, Rectangle2D rect) {
        var res = new double[]{
            rect.getMinX(), rect.getMinY(),
            rect.getMaxX(), rect.getMinY(),
            rect.getMinX(), rect.getMaxY(),
            rect.getMaxX(), rect.getMaxY()
        };
        transform.transform(res, 0, res, 0, 4);
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

    public int compareZIndex(FlagInstance o) {
        int cmp = Double.compare(o.radius, this.radius);
        if (cmp == 0) cmp = Double.compare(this.bounds.getCenterX(), o.bounds.getCenterX());
        if (cmp == 0) cmp = Double.compare(this.bounds.getCenterY(), o.bounds.getCenterY());
        return cmp;
    }

    public void applyTo(int x, int z, BufferedImage image, AnimationKey animationKey) {
        var transform = AffineTransform.getTranslateInstance(-x, -z);
        transform.concatenate(mainTransform);
        flagBuffer.draw(image, transform, antialiasKey, (float) opacity, animationKey);
    }

    public enum AnimationKey {
        ACTUAL,
        BLACK,
        WHITE
    }
}
