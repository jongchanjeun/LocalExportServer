package org.ubstorm.service.chart;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.TableOrder;

public class SpiderWebPlotWithGrid extends SpiderWebPlot {

    private int gridUnit = 0;
    private boolean drawGrid = false;
    
    public SpiderWebPlotWithGrid(CategoryDataset data, int gridUnit) {
        // TODO Auto-generated constructor stub
        super(data);        
        this.gridUnit = gridUnit;
        this.drawGrid = false;
        
    }  


    @Override
    protected void drawRadarPoly(Graphics2D g2,
                                 Rectangle2D plotArea,
                                 Point2D centre,
                                 PlotRenderingInfo info,
                                 int series, int catCount,
                                 double headH, double headW) {

        Polygon polygon = new Polygon();

        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        // plot the data...
        for (int cat = 0; cat < catCount; cat++) {

            Number dataValue = getPlotValue(series, cat);

            if (dataValue != null) {
                double value = dataValue.doubleValue();

                if (value >= 0) { // draw the polygon series...

                    // Finds our starting angle from the centre for this axis

                    double angle = getStartAngle()
                        + (getDirection().getFactor() * cat * 360 / catCount);

                    // The following angle calc will ensure there isn't a top
                    // vertical axis - this may be useful if you don't want any
                    // given criteria to 'appear' move important than the
                    // others..
                    //  + (getDirection().getFactor()
                    //        * (cat + 0.5) * 360 / catCount);

                    // find the point at the appropriate distance end point
                    // along the axis/angle identified above and add it to the
                    // polygon

                    Point2D point = getWebPoint(plotArea, angle,
                            value / this.getMaxValue());
                    polygon.addPoint((int) point.getX(), (int) point.getY());

                    // put an elipse at the point being plotted..

                    Paint paint = getSeriesPaint(series);
                    Paint outlinePaint = getSeriesOutlinePaint(series);
                    Stroke outlineStroke = getSeriesOutlineStroke(series);

                    Ellipse2D head = new Ellipse2D.Double(point.getX()
                            - headW / 2, point.getY() - headH / 2, headW,
                            headH);
                    g2.setPaint(paint);
                    g2.fill(head);
                    g2.setStroke(outlineStroke);
                    g2.setPaint(outlinePaint);
                    g2.draw(head);
                 
                    
                    if (entities != null) {
                        int row = 0; int col = 0;
                        if (this.getDataExtractOrder() == TableOrder.BY_ROW) {
                            row = series;
                            col = cat;
                        }
                        else {
                            row = cat;
                            col = series;
                        }
                        String tip = null;
                        if (this.getToolTipGenerator() != null) {
                            tip = this.getToolTipGenerator().generateToolTip(
                                    this.getDataset(), row, col);
                        }

                        String url = null;
                        if (this.getURLGenerator() != null) {
                            url = this.getURLGenerator().generateURL(this.getDataset(),
                                   row, col);
                        }

                        Shape area = new Rectangle(
                                (int) (point.getX() - headW),
                                (int) (point.getY() - headH),
                                (int) (headW * 2), (int) (headH * 2));
                        CategoryItemEntity entity = new CategoryItemEntity(
                                area, tip, url, this.getDataset(),
                                this.getDataset().getRowKey(row),
                                this.getDataset().getColumnKey(col));
                        entities.add(entity);
                    }

                }
            }
        }
        
        
        int labelX = 0;
       	int labelY = 0;
       	double maxVal = getMaxValue();     
    	if(maxVal > 0 && this.gridUnit > 0){
         	for(int i = 1; i< maxVal; i++){
         		if(i%this.gridUnit == 0){
         			Polygon gridPolygon = new Polygon();
         			for (int cat = 0; cat < catCount; cat++) {
         				 
         				double angle = getStartAngle()
                                  + (getDirection().getFactor() * cat * 360 / catCount);

         				Point2D point = getWebPoint(plotArea, angle,
                              i / this.getMaxValue());
         				gridPolygon.addPoint((int) point.getX(), (int) point.getY());
         				if(cat == 0){
         					labelX = (int) point.getX() + 5;
         					labelY = (int) point.getY(); 
         				}   
         			}               			
         			Rectangle2D rec = polygon.getBounds2D();

                    Paint paint = getSeriesPaint(series);
                    // create linear vertical gradient based upon the bounds of the polygon.
                    //Paint paint = new GradientPaint(new Point2D.Double(rec.getCenterX(),rec.getMinY()), startColor,
                    //        new Point2D.Double(rec.getCenterX(),rec.getMaxY()), endColor);

                    g2.setPaint(Color.LIGHT_GRAY);
                    g2.setStroke(getSeriesOutlineStroke(series));
                    g2.draw(gridPolygon);


                    if (this.isWebFilled()) {
                        // made this the variable alpha instead of the fixed .1f
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                0f));
                        g2.fill(polygon);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                getForegroundAlpha()));
                    } 
                    g2.setPaint(Color.GRAY);
                    g2.drawString(String.valueOf(i), (float) labelX, (float) labelY);
                    
                    
           		}        
         	}        	 
        } 
        
        // Plot the polygon

        // Lastly, fill the web polygon if this is required

        Rectangle2D rec = polygon.getBounds2D();

        Paint paint = getSeriesPaint(series);
        // create linear vertical gradient based upon the bounds of the polygon.
        //Paint paint = new GradientPaint(new Point2D.Double(rec.getCenterX(),rec.getMinY()), startColor,
        //        new Point2D.Double(rec.getCenterX(),rec.getMaxY()), endColor);

        g2.setPaint(paint);
        g2.setStroke(getSeriesOutlineStroke(series));
        g2.draw(polygon);


        if (this.isWebFilled()) {
            // made this the variable alpha instead of the fixed .1f
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    0.1f));
            g2.fill(polygon);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    getForegroundAlpha()));
        }
    }
}