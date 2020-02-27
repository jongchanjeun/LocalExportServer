package org.ubstorm.service.parser;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.ref.WeakReference;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ubstorm.service.utils.ValueConverter;

import com.sun.pdfview.Cache;
import com.sun.pdfview.ImageInfo;
import com.sun.pdfview.PDFImage;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.PDFRenderer;
import com.sun.pdfview.PDFShapeCmd;
import com.sun.pdfview.RefImage;
import com.sun.pdfview.Watchable;

public class printCmdMng {

    /** the array of commands.  The length of this array will always
     * be greater than or equal to the actual number of commands. */
    private ArrayList<printCmd> commands;
    /** whether this page has been finished.  If true, there will be no
     * more commands added to the cmds list. */
    private boolean finished = false;
    /** the page number used to find this page */
    private int pageNumber;
    /** the bounding box of the page, in page coordinates */
    private Rectangle2D bbox;
    /** the rotation of this page, in degrees */
    private int rotation;
    /** a map from image info (width, height, clip) to a soft reference to the
    rendered image */
    private Cache cache;
    /** a map from image info to weak references to parsers that are active */
    private Map<ImageInfo,WeakReference> renderers;

    /**
     * create a PDFPage with dimensions in bbox and rotation.
     */
    public printCmdMng(Graphics2D bbox, int rotation) {
        this(-1, bbox, rotation, null);
    }

    /**
     * create a PDFPage with dimensions in bbox and rotation.
     */
    public printCmdMng(int pageNumber, Graphics2D bbox, int rotation,
            Cache cache) {
        this.pageNumber = pageNumber;       
        // initialize the list of commands
        commands = new ArrayList<printCmd>(250);
    }
   
    /**
     * get the page number used to lookup this page
     * @return the page number
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * get the aspect ratio of the correctly oriented page.
     * @return the width/height aspect ratio of the page
     */
    public float getAspectRatio() {
        return getWidth() / getHeight();
    }

    /**
     * get the bounding box of the page, before any rotation.
     */
    public Rectangle2D getBBox() {
        return bbox;
    }

    /**
     * get the width of this page, after rotation
     */
    public float getWidth() {
        return (float) bbox.getWidth();
    }

    /**
     * get the height of this page, after rotation
     */
    public float getHeight() {
        return (float) bbox.getHeight();
    }

    /**
     * get the rotation of this image
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * Get the initial transform to map from a specified clip rectangle in
     * pdf coordinates to an image of the specfied width and
     * height in device coordinates
     *
     * @param width the width of the image
     * @param height the height of the image
     * @param clip the desired clip rectangle (in PDF space) or null to use
     *             the page's bounding box
     */
    public AffineTransform getInitialTransform(int width, int height,
            Rectangle2D clip) {
        AffineTransform at = new AffineTransform();
        switch (getRotation()) {
            case 0:
                at = new AffineTransform(1, 0, 0, -1, 0, height);
                break;
            case 90:
                at = new AffineTransform(0, 1, 1, 0, 0, 0);
                break;
            case 180:
                at = new AffineTransform(-1, 0, 0, 1, width, 0);
                break;
            case 270:
                at = new AffineTransform(0, -1, -1, 0, width, height);
                break;
        }

        if (clip == null) {
            clip = getBBox();
        } else if (getRotation() == 90 || getRotation() == 270) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        // now scale the image to be the size of the clip
        double scaleX = width / clip.getWidth();
        double scaleY = height / clip.getHeight();
        at.scale(scaleX, scaleY);

        // create a transform that moves the top left corner of the clip region
        // (minX, minY) to (0,0) in the image
        at.translate(-clip.getMinX(), -clip.getMinY());

        return at;
    }

    /**
     * get the current number of commands for this page
     */
    public int getCommandCount() {
        return commands.size();
    }

    /**
     * get the command at a given index
     */
    public printCmd getCommand(int index) {
        return commands.get(index);
    }

    /**
     * get all the commands in the current page
     */
    public ArrayList<printCmd> getCommands() {
        return commands;
    }

    /**
     * get all the commands in the current page starting at the given index
     */
    public List getCommands(int startIndex) {
        return getCommands(startIndex, getCommandCount());
    }

    /*
     * get the commands in the page within the given start and end indices
     */
    public List getCommands(int startIndex, int endIndex) {
        return commands.subList(startIndex, endIndex);
    }

    /**
     * Add a single command to the page list.
     */
    public void addCommand(printCmd cmd) {
        synchronized (commands) {
            commands.add(cmd);
        }   
    }    
        
    
    
    /**
     * set the fill color
     * @param color of the backgroundColor
     */
    public void addSetColor(Color color) {
    	printSetColor pc = new printSetColor(color);
        addCommand(pc);
    }        
 
    
    /**
     * addImage
     */
    public void addImage(Object _ac , BufferedImage _image, int _x, int _y, int _w,int _h) {
    	imageCmd pc = new imageCmd(_ac, _image, _x, _y, _w, _h);
    	addCommand(pc);
    }
    
    
    
    /**
     * addLine
     */
    public void addLine(Stroke _stroke, Color _lineColor, Line2D _line) {
    	lineCmd pc = new lineCmd(_stroke, _lineColor, _line);
    	addCommand(pc);
    }
    
    
    
    /**
     * addCircle
     */
    public void addCircle(Stroke _stroke, AlphaComposite _bgAc , Ellipse2D.Double _hole, AlphaComposite _brAc ,Color _bgColor, Color _brColor,
    		int _x, int _y, int _w,int _h) {
    	circleCmd pc = new circleCmd(_stroke, _bgAc, _hole, _brAc, _bgColor, _brColor, _x, _y, _w, _h);
    	addCommand(pc);
    }
    
    
    /**
     * addRectangle
     */
    public void addRectangle(Stroke _stroke, AlphaComposite _bgAc , AlphaComposite _brAc,Color _bgcolor, Color _brcolor,
    		int _x, int _y, int _w,int _h,int _radius) {
    	rectangleCmd pc = new rectangleCmd(_stroke, _bgAc, _brAc, _bgcolor, _brcolor, _x, _y, _w, _h, _radius);
    	addCommand(pc);
    }
        

    /**
     * addGradiantRectangle
     */
    public void addGradiantRectangle(Stroke _stroke, AlphaComposite _ac , GradientPaint _gradient, Color _brColor,
    		int _x, int _y, int _w,int _h,int _radius ) {    	 
    	gradiantRectangleCmd pc = new gradiantRectangleCmd(_stroke, _ac, _gradient, _brColor, _x, _y, _w, _h, _radius);
    	addCommand(pc);
    }
    
    
    /**
     * addBorder 명령어 추가
     */
    public void addBorder(ArrayList<Color> _colors ,ArrayList<Stroke> _strokes , ArrayList<Line2D.Float> _lines  , ArrayList<AlphaComposite> _acs ) {    	 
    	borderCmd pc = new borderCmd(_colors, _strokes,_lines,_acs);
    	addCommand(pc);
    }
    
    
    /**
     * addRotateLabelString
     */
    public void addRotateLabelString(ArrayList<AttributedCharacterIterator> _texts  , ArrayList<Float> _transX , ArrayList<Float> _transY ,Font _font , double _rotation,  AlphaComposite _fontAc , Color _fontColor) {    	 
    	rotationLabelStringCmd pc = new rotationLabelStringCmd(_texts, _transX, _transY, _font, _rotation,_fontAc,_fontColor);
    	addCommand(pc);
    }
    
    /**
     * addLabelString
     */
    public void addLabelString(ArrayList<AttributedCharacterIterator> _texts  , ArrayList<Float> _transX , ArrayList<Float> _transY ,Font _font,  AlphaComposite _fontAc , Color _fontColor ) {    	 
    	labelStringCmd pc = new labelStringCmd(_texts, _transX, _transY, _font,_fontAc,_fontColor);
    	addCommand(pc);
    }
       
    /**
     * addBgFill
     */
    public void addBgFill(Color _bgColor, AlphaComposite _ac , int _x, int _y, int _w, int _h) {    	 
    	bgFillCmd pc = new bgFillCmd(_bgColor, _ac, _x, _y, _w, _h);
    	addCommand(pc);
    }
    
    
    /**
     * addSingleString
     */
    public void addSingleString(Color fontColor, Font font,int x, int y ,  String text) {    	 
    	
    	singleStringCmd  pc = new singleStringCmd(fontColor, font, x, y, text);
    		
    	addCommand(pc);
    }
    
    
    /**
     * addRectBorder
     */
    public void addRectBorder(Stroke stroke , Color color, Rectangle2D rect) {    	 
    	
    	rectBorderCmd  pc = new rectBorderCmd(stroke,color,rect);    		
    	addCommand(pc);
    }
    
    /**
     * addEllipse
     */
    public void addEllipse(Stroke stroke , Color borderColor, Color fillColor, Ellipse2D.Double hole , boolean isFill) {    	 
    	
    	ellipseCmd  pc = new ellipseCmd(stroke, borderColor,fillColor, hole, isFill);    		
    	addCommand(pc);
    }
    
    /**
     * addWaterMark
     */
    public void addLicense(String _license, Color _color, Font _font , FontMetrics _metrics , AlphaComposite ac) {  	 
    	
    	licenseCmd pc = new licenseCmd(_license, _color,_font, _metrics , ac);       		
    	addCommand(pc);
    }
    
    /**
     * addEllipse
     */
    public void addWaterMark(String _waterMarkTxt, Color _txColor, Font _font, int _x , int _y , AlphaComposite _ac) {  	 
    	
    	waterMarkCmd pc = new waterMarkCmd(_waterMarkTxt, _txColor,_font, _x, _y, _ac);       		
    	addCommand(pc);
    }
    
//###########################################################################################################// 
//                                               명령어 Class Start  											 //
//###########################################################################################################//
    
/**
 * set the fill 
 */
class printSetColor extends printCmd {    
	private Color fillColor = Color.white;
    public printSetColor(Color color) {
        this.fillColor = color;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	//g2d.setColor(this.fillColor);
        return null;
    }	
}


/**
 * drawImage
 */
class imageCmd extends printCmd {    	
	Object ac = null;	
	BufferedImage image=null;			
	int x = 0;
	int y = 0;
	int w = 0;
	int h = 0;
	
    public imageCmd(Object _ac , BufferedImage _image, int _x, int _y, int _w,int _h) {    	
    	ac = _ac;
    	image = _image;
    	x = _x;
    	y = _y;
    	w = _w;
    	h = _h;   
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawImage(ac, image, x, y, w, h);
        return null;
    }	
}


/**
 * drawLine
 */
class lineCmd extends printCmd {    
	Stroke stroke = null;	
	Color lineColor = Color.black;	
	Line2D line = null;				
	
    public lineCmd(Stroke _stroke, Color _lineColor, Line2D _line) {    	
    	stroke = _stroke;    	
    	lineColor = _lineColor;
    	line = _line;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawLine(stroke, lineColor, line);
        return null;
    }	
}

/**
 * drawRectangle
 */
class circleCmd extends printCmd {    
	Stroke stroke = null;
	AlphaComposite bgAc = null;
	AlphaComposite brAc = null;
	Color bgColor = Color.white;
	Color brColor = Color.black;
	int x = 0;
	int y = 0;
	int w = 0;
	int h = 0;
	Ellipse2D.Double hole = null;				
	
    public circleCmd(Stroke _stroke, AlphaComposite _bgAc , Ellipse2D.Double _hole, AlphaComposite _brAc ,Color _bgColor, Color _brColor,
    		int _x, int _y, int _w,int _h ) {    	
    	stroke = _stroke;
    	bgAc = _bgAc;
    	brAc = _brAc;
    	bgColor =_bgColor;
    	brColor = _brColor;
    	x = _x;
    	y = _y;
    	w = _w;
    	h = _h;   
    	hole = _hole;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawCircle(stroke, bgAc, hole, brAc, bgColor, brColor, x, y, w, h);
        return null;
    }	
}

/**
 * drawRectangle
 */
class rectangleCmd extends printCmd {    
	Stroke stroke = null;
	AlphaComposite bgAc = null;
	AlphaComposite brAc = null;
	Color bgColor = Color.white;
	Color brColor = Color.black;
	int x = 0;
	int y = 0;
	int w = 0;
	int h = 0;
	int radius = 0;							
	
    public rectangleCmd(Stroke _stroke, AlphaComposite _bgAc , AlphaComposite _brAc,Color _bgColor, Color _brColor,
    		int _x, int _y, int _w,int _h,int _radius ) {    	
    	stroke = _stroke;
    	bgAc = _bgAc;
    	brAc = _brAc;
    	bgColor =_bgColor;
    	brColor = _brColor;
    	x = _x;
    	y = _y;
    	w = _w;
    	h = _h;
    	radius = _radius;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawRectangle(stroke, bgAc, brAc, bgColor, brColor, x, y, w, h, radius);
        return null;
    }	
}

/**
 * set the fill 
 */
class gradiantRectangleCmd extends printCmd {    
	Stroke stroke = null;
	AlphaComposite ac = null;
	GradientPaint gradient = null;
	Color brColor = Color.black;
	int x = 0;
	int y = 0;
	int w = 0;
	int h = 0;
	int radius = 0;							
	
    public gradiantRectangleCmd(Stroke _stroke, AlphaComposite _ac , GradientPaint _gradient, Color _brColor,
    		int _x, int _y, int _w,int _h,int _radius ) {    	
    	stroke = _stroke;    
    	ac = _ac;    	
    	gradient = _gradient;
    	brColor = _brColor;
    	x = _x;
    	y = _y;
    	w = _w;
    	h = _h;
    	radius = _radius;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.exdcDrawGradiantRectangle(stroke, ac, gradient, brColor, x, y, w, h, radius);
        return null;
    }	
}

/**
 * set the fill 
 */
class borderCmd extends printCmd {    
	ArrayList<Color> colors = null;
	ArrayList<Line2D.Float> lines = null;
	ArrayList<Stroke> strokes = null;
	ArrayList<AlphaComposite> acs = new ArrayList<AlphaComposite>();			
	
    public borderCmd(ArrayList<Color> _colors ,ArrayList<Stroke> _strokes , ArrayList<Line2D.Float> _lines , ArrayList<AlphaComposite> _acs) {    	
    	colors = _colors;
    	lines = _lines;
    	strokes = _strokes;
    	acs = _acs;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawBorder(colors, strokes, lines, acs);
        return null;
    }	
}

/**
 * set the Rotation Text
 */
class rotationLabelStringCmd extends printCmd {    
	ArrayList<AttributedCharacterIterator> texts = null;
	ArrayList<Float> transX = null;
	ArrayList<Float> transY = null;
	Font font  = null;
	double rotation = 0;			
	AlphaComposite fontAc = null;
	Color color = Color.black;
	
    public rotationLabelStringCmd(ArrayList<AttributedCharacterIterator> _texts  , ArrayList<Float> _transX , ArrayList<Float> _transY ,Font _font , Double _rotation,  AlphaComposite _fontAc , Color _fontColor ) {    	
    	texts = _texts;
    	transX = _transX;
    	transY = _transY;
    	font = _font;
    	rotation = _rotation;
    	color = _fontColor;
    	fontAc =_fontAc; 
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawRotateString(texts, transX, transY, font, rotation,fontAc,color);
        return null;
    }	
}

/**
 * set the Text
 */
class labelStringCmd extends printCmd {    
	ArrayList<AttributedCharacterIterator> texts = null;
	ArrayList<Float> transX = null;
	ArrayList<Float> transY = null;
	AlphaComposite fontAc = null;
	Color color = Color.black;
	Font font  = null;	
    public labelStringCmd(ArrayList<AttributedCharacterIterator> _texts  , ArrayList<Float> _transX , ArrayList<Float> _transY ,Font _font,  AlphaComposite _fontAc , Color _fontColor  ) {    	
    	texts = _texts;
    	transX = _transX;
    	transY = _transY;
    	font = _font;    
    	color = _fontColor;
    	fontAc =_fontAc; 
    }
    
    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawString(texts, transX, transY, font,fontAc,color);
        return null;
    }	
}

/**
 * set the Label fill 
 */
class bgFillCmd extends printCmd {    	
	AlphaComposite ac = null;	
	Color bgColor = Color.white;
	int x = 0;
	int y = 0;
	int w = 0;
	int h = 0;
	int radius = 0;							
	
    public bgFillCmd(Color _bgColor, AlphaComposite _ac , int _x, int _y, int _w, int _h ) {    	
    	ac = _ac; 
    	bgColor = _bgColor;
    	x = _x;
    	y = _y;
    	w = _w;
    	h = _h;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawBgFill(bgColor,ac, x, y, w, h);
        return null;
    }	
}

/**
 * set the Ellipse Fill and Border
 */
class ellipseCmd extends printCmd {    	
	Stroke stroke = null;	
	Color fillColor = Color.WHITE;
	Color borderColor = Color.BLACK;
	Ellipse2D.Double hole = null;
	boolean isFill = true;
    
	public ellipseCmd(Stroke _stroke , Color _boderColor, Color _fillColor, Ellipse2D.Double _hole , boolean _isFill ) {    	
		 stroke = _stroke;	
		 fillColor = _fillColor;
		 borderColor =_boderColor;
		 hole = _hole;
		 isFill = _isFill;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawEllipse(stroke, borderColor, fillColor, hole, isFill);
        return null;
    }	
}


/**
 * set the Rectangle Border
 */
class rectBorderCmd extends printCmd {   	
	Stroke stroke = null;		
	Color color = Color.BLACK;
	Rectangle2D reck = null;	
    
	public rectBorderCmd(Stroke _stroke , Color _color, Rectangle2D _rect ) {    	
		 stroke = _stroke;	
		 color = _color;		
		 reck = _rect;		
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawRectBorder(stroke, color,reck);
        return null;
    }	
}


/**
 * set the Rectangle Border
 */
class singleStringCmd extends printCmd {   			
	Color fontColor = Color.BLACK;
	Font font = null;	
	int x = 0;
	int y = 0;
	String text = "";
    
	public singleStringCmd(Color _fontColor, Font _font,int _x, int _y ,  String _text) {    	
		fontColor = _fontColor;	
		font = _font;		
		 x = _x;	
		 y = _y;	
		 text = _text;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawSingleString(fontColor, font, x, y, text);
        return null;
    }	
}


/**
 * set the Rectangle Border
 */
class licenseCmd extends printCmd {   			
	Color color = Color.BLACK;
	Font font = null;	
	FontMetrics metrics = null;
	String license = "";
	AlphaComposite ac = null;
	public licenseCmd(String _license, Color _fontColor, Font _font , FontMetrics _metrics,AlphaComposite _ac) {    	
		license = _license;
		color = _fontColor;	
		font = _font;		
		metrics = _metrics;
		ac = _ac;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawLicense(license, color, font, metrics,ac);
        return null;
    }	
}

/**
 * set the Rectangle Border
 */
class waterMarkCmd extends printCmd {   			
	Color fontColor = Color.BLACK;
	Font font = null;	
	int x = 0;
	int y = 0;
	String waterMarkTxt = "";
	AlphaComposite ac = null;
	public waterMarkCmd(String _waterMarkTxt, Color _fontColor, Font _font, int _x , int _y, AlphaComposite _ac) {    	
		fontColor = _fontColor;	
		font = _font;		
		x = _x;	
		y = _y;	
		waterMarkTxt = _waterMarkTxt;
		ac = _ac;
    }

    public Graphics2D execute(Json2PrintParser j2p) {
    	j2p.execDrawWaterMark(waterMarkTxt, fontColor, font, x, y , ac);
        return null;
    }	
}


}