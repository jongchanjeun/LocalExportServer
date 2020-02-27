package org.ubstorm.service.parser.svg.object;

import java.util.ArrayList;
import java.util.List;

public class svgText {

	private svgGroup parent = null;
	
	private int x = 0;
	private int y = 0;
	
	private String fontStyle = "";
	private String fontWeight = "normal";
	private String fontFamily = "돋움";
	private int fontSize = 10;	// px
	
	private String bullet = "none";
	private String terms = "none";
	
	private int depth = 0;
	private int indent = 0;
	
	List<svgTspan> tspans = new ArrayList<svgTspan>();

	public svgGroup getParent() {
		return parent;
	}

	public void setParent(svgGroup parent) {
		this.parent = parent;
	}
	
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

	public String getBullet() {
		return bullet;
	}

	public void setBullet(String bullet) {
		this.bullet = bullet;
	}
	
	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}
	
	public List<svgTspan> getTspans() {
		return tspans;
	}

	public void setTspans(List<svgTspan> tspans) {
		this.tspans = tspans;
	}
	
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public int getIndent() {
		return indent;
	}

	public void setIndent(int indent) {
		this.indent = indent;
	}
	
	/**
	 * text에 tspan하나를 추가한다.
	 * 
	 * @param tspan
	 * @return 성공:tspan갯수, 실패:-1
	 */
	public int addTspan(svgTspan tspan)
	{
		if(this.tspans.add(tspan))
		{
			tspan.setParent(this);
			tspan.setFontFamily(this.fontFamily);
			tspan.setFontSize(this.fontSize);
			return this.tspans.size();
		}
		else
			return -1;
	}
	
	/**
	 * text에 저장된 tspan갯수를 반환한다.
	 * 
	 * @return
	 */
	public int getTspanCount()
	{
		return this.tspans.size();
	}
	
	public svgText getClone()
	{
		svgText _cloneText = new svgText();
		_cloneText.setBullet(bullet);
		_cloneText.setDepth(depth);
		_cloneText.setX(x);
		_cloneText.setY(y);
		_cloneText.setFontFamily(fontFamily);
		_cloneText.setFontSize(fontSize);
		_cloneText.setFontWeight(fontWeight);
		_cloneText.setTerms(terms);
		_cloneText.setX(x);
		_cloneText.setIndent(indent);
		
		return _cloneText;
	}
	
}
