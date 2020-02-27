/**
 * 
 */
package org.ubstorm.service.parser;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import com.sun.pdfview.PDFRenderer;

public abstract class printCmd {

    /**
     * mark the page or change the graphics state
     * @param state the current graphics state;  may be modified during
     * execution.
     * @return the region of the page made dirty by executing this command
     *         or null if no region was touched.  Note this value should be
     *         in the coordinates of the image touched, not the page.
     */
    public abstract Graphics2D execute(Json2PrintParser j2p);

    /**
     * a human readable representation of this command
     */
    @Override
    public String toString() {
        String name = getClass().getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            return name.substring(lastDot + 1);
        } else {
            return name;
        }
    }

    /**
     * the details of this command
     */
    public String getDetails() {
        return super.toString();
    }
}
