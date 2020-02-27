package org.ubstorm.service.parser.svg.object;

public class svgTspan {
	
	private svgText parent = null;
	private int dx = 0;
	private int dy = 0;
	private String text = "";

	private String fontColor = "black";
	private String fontStyle = "";
	private String fontWeight = "normal";
	private String fontFamily = "돋움";
	private int fontSize = 10;	// px

	public svgText getParent() {
		return parent;
	}
	public void setParent(svgText parent) {
		this.parent = parent;
	}
	
	public int getDx() {
		return dx;
	}
	public void setDx(int dx) {
		this.dx = dx;
	}
	public int getDy() {
		return dy;
	}
	public void setDy(int dy) {
		this.dy = dy;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public String getFontColor() {
		return fontColor;
	}
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}
	
	public String getFontStyle() {
		return fontStyle;
	}
	public void setFontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
	}
	public String getFontWeight() {
		return fontWeight;
	}
	public void setFontWeight(String fontWeight) {
		this.fontWeight = fontWeight;
	}
	public String getFontFamily() {
		return fontFamily;
	}
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}	
}
