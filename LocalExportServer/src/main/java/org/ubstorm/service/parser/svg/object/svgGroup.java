package org.ubstorm.service.parser.svg.object;

import java.util.ArrayList;
import java.util.List;

public class svgGroup {

	private int x = 0;
	private int y = 0;
	private int dx = 0;
	private int dy = 0;
	
	private String fontStyle = "";
	private String fontWeight = "normal";
	private String fontFamily = "돋움";
	private int fontSize = 10;	// px
	
	List<svgText> texts = new ArrayList<svgText>();
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
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

	public List<svgText> getTexts() {
		return texts;
	}

	public void setTexts(List<svgText> texts) {
		this.texts = texts;
	}

	/**
	 * Group에 text하나를 추가한다.
	 * 
	 * @param text
	 * @return 성공:text갯수, 실패:-1
	 */
	public int addText(svgText text)
	{
		if(this.texts.add(text))
		{
			text.setParent(this);
			text.setFontFamily(this.fontFamily);
			text.setFontSize(this.fontSize);
			return this.texts.size();
		}
		else
			return -1;
	}
	
	/**
	 * Group에 저장된 text갯수를 반환한다.
	 * 
	 * @return
	 */
	public int getTextCount()
	{
		return this.texts.size();
	}
		
	/**
	 * Group에 마지막으로 저장된 text을 반환한다.
	 * 
	 * @return
	 */
	public svgText getLastText()
	{
		return this.texts.get(this.texts.size()-1);
	}
	
	public svgGroup getClone()
	{
		svgGroup _cloneGrp = new svgGroup();
		_cloneGrp.setDx( dx );
		_cloneGrp.setDy( dy );
		_cloneGrp.setFontFamily( fontFamily );
		_cloneGrp.setFontSize( fontSize );
		_cloneGrp.setFontWeight(fontWeight);
		_cloneGrp.setX(x);
		_cloneGrp.setY(y);
		
		return _cloneGrp;
	}
	
}
