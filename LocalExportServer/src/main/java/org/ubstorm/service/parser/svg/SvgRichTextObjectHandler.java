package org.ubstorm.service.parser.svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.svg.object.HeightOverflowException;
import org.ubstorm.service.parser.svg.object.svgGroup;
import org.ubstorm.service.parser.svg.object.svgTable;
import org.ubstorm.service.parser.svg.object.svgText;
import org.ubstorm.service.parser.svg.object.svgTspan;
import org.ubstorm.service.utils.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SvgRichTextObjectHandler extends DefaultHandler {

	protected Logger log = Logger.getLogger(getClass());
	
	/**
	 * CONSTANT VALUE DEFINE
	 */
	private int INDENT_DEP1 = 0;
	private int INDENT_DEP2 = 20;
	private int INDENT_DEP3 = 40;
	
	private FontMetrics fm = null;
	private	Font font = null;
	private int nCurCharPosX = 0;
	
	private svgTable svgTbl = new svgTable();	
	
	private String textDecoration = "";
	private String textColor = "black";
	private String fontWeight = "normal";
	private int fontSizeUnit = 0;
	private int fontSize = 10;
	private String fontFamily = "돋움";
	
	private boolean bSetTextColor = false;
	private boolean bDecoration = false;
	private boolean bBr = false;
	private boolean bStrong = false;
	private String  stBullet = "none";
	private String  stTerms = "none";
	
	private int nLineGap = 6;
	private int nLineHeight = 13;
	private int nRNumberIndex = 0;
	private int nDNumberIndex = 0;
	private int nGanaIndex = 0;
	private int nCurDepth = 1;
	private int nDocWidth = 800;
	private int nDocHeight = 600;
	
	private int breakLineCount = 0;
	private boolean isCheckRowHeight = true;
	private boolean isUseWordWrap = true;
	
	
	// AdjustableHeight 일경우 사용 변수
	private float[] rowHeights = {};	// rowHeight값을 담아둘 변수
	public ArrayList<Float> resultHeightList = new ArrayList<Float>(); // 페이지별로 지정된 Height값을 담아둔 객체
	private boolean useAdjustableHeight = false;						// adjustableHeight값 사용 여부
	public List<svgTable> svgTblList = new ArrayList<svgTable>();		// adjustableHeight사용시 페이지별로 담길 table을 담아둔 객체
	
	private svgGroup argoGroup = null;		// 마지막 생성된 svgGroup를 담아둘객체
	private svgText argoText = null;		// 마지막 생성된 SvgText객체를 담아둘 객체
	private boolean isNewLineFlag = false;		// 다음페이지로 잘렷을 경우 라인을 신규로 지정하도록 처리
	
	private int nOrizinalHeight = 0;
	
	private int nAdjustableGap = 5;
	
	public void init(int docWidth, int docHeight, String fontFamily, int fontSize, int lineGap, boolean checkRowHeight, boolean useWordWrap, int fontSizeUnit)
	{
		this.nDocWidth = docWidth;
		this.nDocHeight = docHeight;
		
		this.bSetTextColor = false;
		this.bDecoration = false;
		this.bBr = false;
		this.bStrong = false;
		this.textDecoration = "";
		this.textColor = "black";
		this.fontFamily = fontFamily;
		
		this.fontSizeUnit = fontSizeUnit;
		
		if( GlobalVariableData.M_REGISTER_FONT_LISE.indexOf(this.fontFamily) == -1 && GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.containsKey(this.fontFamily) )
		{
			this.fontFamily = GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.get(this.fontFamily);
		}

		if(fontSizeUnit == 0)
		{
			// 8, 13, 15, 17사이즈는 존재하지 않도록 해야 한다. (pt와 px간 변환문제 발생)
			if(fontSize == 8 || fontSize == 13 || fontSize == 15 || fontSize == 17)
				fontSize = fontSize - 1;
		}
			
		this.fontSize = fontSize;
		this.fontWeight = "normal";
		this.nLineGap = lineGap;
		
		this.nCurDepth = 0;
		this.nRNumberIndex = 0;
		this.nDNumberIndex = 0;
		this.nGanaIndex = 0;
		
		this.svgTbl = new svgTable();
		this.svgTbl.setWidth(docWidth);
		this.svgTbl.setHeight(docHeight);
		
		this.stBullet = "none";
		this.stTerms = "none";
	
		int _fontStyle = this.fontWeight.equalsIgnoreCase("bold") ? Font.BOLD : Font.PLAIN;
		font = new Font(this.fontFamily, _fontStyle, this.fontSize);	
		fm = StringUtil.getFontMatrix(font);
		
		//this.nLineHeight = fm.getHeight() + nLineGap;	
		this.nLineHeight = this.fontSize + this.nLineGap;
		this.nCurCharPosX = 0;
		
		this.breakLineCount = 0;
		
		this.isCheckRowHeight = checkRowHeight;
		this.isUseWordWrap = useWordWrap;
		
		this.nOrizinalHeight = docHeight;
	}
	
	
	public void init(int docWidth, int docHeight, String fontFamily, int fontSize, int lineGap, boolean checkRowHeight, boolean useWordWrap, float[] _heights, boolean isUseAdjutable, int fontSizeUnit )
	{
		this.nDocWidth = docWidth;
		this.nDocHeight = docHeight;
		
		this.bSetTextColor = false;
		this.bDecoration = false;
		this.bBr = false;
		this.bStrong = false;
		this.textDecoration = "";
		this.textColor = "black";
		this.fontFamily = fontFamily;
		
		if( GlobalVariableData.M_REGISTER_FONT_LISE.indexOf(this.fontFamily) == -1 && GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.containsKey(this.fontFamily) )
		{
			this.fontFamily = GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.get(this.fontFamily);
		}
		
		if(fontSizeUnit == 0)
		{
			// 8, 13, 15, 17사이즈는 존재하지 않도록 해야 한다. (pt와 px간 변환문제 발생)
			if(fontSize == 8 || fontSize == 13 || fontSize == 15 || fontSize == 17)
				fontSize = fontSize - 1;
		}
			
		this.nOrizinalHeight = docHeight;
		
		if( _heights.length > 0 ) 
		{
			docHeight = Float.valueOf(_heights[0]).intValue(); 
			this.nDocHeight = docHeight - nAdjustableGap;
		}
		
		this.fontSize = fontSize;
		this.fontWeight = "normal";
		this.nLineGap = lineGap;
		
		this.nCurDepth = 0;
		this.nRNumberIndex = 0;
		this.nDNumberIndex = 0;
		this.nGanaIndex = 0;
		
		this.svgTbl = new svgTable();
		this.svgTbl.setWidth(docWidth);
		this.svgTbl.setHeight(docHeight);
		
		this.stBullet = "none";
		this.stTerms = "none";
	
		int _fontStyle = this.fontWeight.equalsIgnoreCase("bold") ? Font.BOLD : Font.PLAIN;
		font = new Font(this.fontFamily, _fontStyle, this.fontSize);	
		fm = StringUtil.getFontMatrix(font);
		
		//this.nLineHeight = fm.getHeight() + nLineGap;	
		this.nLineHeight = this.fontSize + this.nLineGap;
		this.nCurCharPosX = 0;
		
		this.breakLineCount = 0;
		
		this.isCheckRowHeight = checkRowHeight;
		this.isUseWordWrap = useWordWrap;
		useAdjustableHeight = isUseAdjutable;
		rowHeights = _heights;
		
	}
	
	
	
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		log.debug(getClass().getName() + "::" + "Start Element ----------> " + qName);
		
		font = new Font(this.fontFamily, Font.PLAIN , this.fontSize);	
		fm = StringUtil.getFontMatrix(font);
		
		String stBullet = attributes.getValue("bullet");
			   
		String _bullet = "";
		if (qName.equalsIgnoreCase("dep1")) {
		   
		   nRNumberIndex = 0;
		   nDNumberIndex = 0;
		   nGanaIndex = 0;
			
		   stTerms = attributes.getValue("terms");
		   log.debug(getClass().getName() + "::" + qName + " Attrinutes = bullet : " + stBullet + ", terms : " + stTerms);
		  
		   svgGroup svgGrp = new svgGroup();
		   svgGrp.setFontFamily(this.fontFamily);
		   svgGrp.setFontSize(this.fontSize);
		   this.svgTbl.addGroup(svgGrp);
		   
		   //this.breakLineCount++;
		   try {
			   this.addRow();
		   } catch (HeightOverflowException e) {
				// TODO Auto-generated catch block
			   //e.printStackTrace();
			   throw new SAXException("HeightOverflowException");
		   }
		   
		   this.nCurDepth = 1;
		   this.nCurCharPosX = INDENT_DEP1;
		   svgText svgTxt = new svgText();
		   svgTxt.setX(0);
		   svgTxt.setDepth(this.nCurDepth);
		   svgGrp.addText(svgTxt);
		   if(!stBullet.equalsIgnoreCase("none"))
		   {
			   _bullet = getBullet(stBullet);
			   this.nCurCharPosX += fm.stringWidth(_bullet); 
			   svgTxt.setBullet(stBullet);
			   svgTspan svgTs = new svgTspan();
			   svgTs.setText(_bullet);
			   svgTxt.addTspan(svgTs);
			   
			   this.nCurCharPosX += fm.stringWidth(" "); 
			   svgTspan svgTs2 = new svgTspan();
			   svgTs2.setText(" ");
			   svgTxt.addTspan(svgTs2);
		   }
		   svgTxt.setIndent(this.nCurCharPosX);
		   argoGroup = svgGrp;
	   } else if (qName.equalsIgnoreCase("dep2")) {

		   log.debug(getClass().getName() + "::" + qName + " Attrinutes = bullet : " + stBullet);
		   
		   //this.breakLineCount++;
		   try {
			   this.addRow();
		   } catch (HeightOverflowException e) {
				// TODO Auto-generated catch block
			   //e.printStackTrace();
			   throw new SAXException("HeightOverflowException");
		   }
		   
		   this.nCurDepth = 2;
		   this.nCurCharPosX = INDENT_DEP2;
		   svgText svgTxt = new svgText();
		   svgTxt.setX(INDENT_DEP2);
		   svgTxt.setDepth(this.nCurDepth);
		   this.svgTbl.getLastGroup().addText(svgTxt);
		   if(!stBullet.equalsIgnoreCase("none"))
		   {
			   _bullet = getBullet(stBullet);
			   this.nCurCharPosX += fm.stringWidth(_bullet); 
			   
			   svgTxt.setBullet(stBullet);
			   svgTspan svgTs = new svgTspan();
			   svgTs.setText(_bullet);
			   svgTxt.addTspan(svgTs);
			   
			   this.nCurCharPosX += fm.stringWidth(" "); 
			   svgTspan svgTs2 = new svgTspan();
			   svgTs2.setText(" ");
			   svgTxt.addTspan(svgTs2);
		   }
		   svgTxt.setIndent(this.nCurCharPosX);
		   argoText = svgTxt;
	   } else if (qName.equalsIgnoreCase("dep3")) {
	   
		   log.debug(getClass().getName() + "::" + qName + " Attrinutes = bullet : " + stBullet);

		   //this.breakLineCount++;
		   try {
			   this.addRow();
		   } catch (HeightOverflowException e) {
				// TODO Auto-generated catch block
			   //e.printStackTrace();
			   throw new SAXException("HeightOverflowException");
		   }
		   
		   this.nCurDepth = 3;
		   this.nCurCharPosX = INDENT_DEP3;
		   svgText svgTxt = new svgText();
		   svgTxt.setX(INDENT_DEP3);
		   svgTxt.setDepth(this.nCurDepth);
		   this.svgTbl.getLastGroup().addText(svgTxt);
		   if(!stBullet.equalsIgnoreCase("none"))
		   {
			   _bullet = getBullet(stBullet);
			   this.nCurCharPosX += fm.stringWidth(_bullet); 
			   
			   svgTxt.setBullet(stBullet);
			   svgTspan svgTs = new svgTspan();
			   svgTs.setText(_bullet);
			   svgTxt.addTspan(svgTs);
			   
			   this.nCurCharPosX += fm.stringWidth(" "); 
			   svgTspan svgTs2 = new svgTspan();
			   svgTs2.setText(" ");
			   svgTxt.addTspan(svgTs2);
		   }
		   svgTxt.setIndent(this.nCurCharPosX);
		   argoText = svgTxt;
	   } else if (qName.equalsIgnoreCase("strong")) {
		   bStrong = true;
	   }
	   else if (qName.equalsIgnoreCase("br")) {
		   bBr = true;
	   }
	   else if (qName.equalsIgnoreCase("u")) {
		   bDecoration = true;
		   this.textDecoration = "underline";
	   }
	   else if (qName.equalsIgnoreCase("s") || qName.equalsIgnoreCase("strike")) {
		   bDecoration = true;
		   this.textDecoration = "line-through";
	   }
	   else if (qName.equalsIgnoreCase("font")) {
		   bSetTextColor = true;
		   String _txtColor = attributes.getValue("color");
		   this.textColor = _txtColor != null && _txtColor.length() > 0 ? _txtColor : "black";
	   }
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
	   log.debug(getClass().getName() + "::" + "End Element ----------> " + qName);
	   
	   if (qName.equalsIgnoreCase("dep1")) {
		   nCurDepth = 0;
	   } else if (qName.equalsIgnoreCase("dep2")) {
		   nCurDepth = 1;
	   } else if (qName.equalsIgnoreCase("dep3")) {
		   nCurDepth = 2;
	   } else if (qName.equalsIgnoreCase("strong")) {
		   bStrong = false;
	   } else if (qName.equalsIgnoreCase("u")) {
		   bDecoration = false;
		   this.textDecoration = "";
	   } else if (qName.equalsIgnoreCase("s") || qName.equalsIgnoreCase("strike")) {
		   bDecoration = true;
		   this.textDecoration = "";
	   } else if (qName.equalsIgnoreCase("br")) {
		   bBr = false;
		   
		   try {
			   this.addRow();
		   } catch (HeightOverflowException e) {
				// TODO Auto-generated catch block
			   //e.printStackTrace();
			   throw new SAXException("HeightOverflowException");
		   }
		   
		   this.nCurCharPosX = this.svgTbl.getLastGroup().getLastText().getIndent();
		   svgTspan svgTsp = new svgTspan();
		   
		   if(nCurDepth == 3)
			   svgTsp.setDx(this.nCurCharPosX - INDENT_DEP3);
		   else if(nCurDepth == 2)
			   svgTsp.setDx(this.nCurCharPosX - INDENT_DEP2);
		   else
			   svgTsp.setDx(this.nCurCharPosX - INDENT_DEP1);
		   
		   svgTsp.setDy(this.nLineHeight);
		   this.svgTbl.getLastGroup().getLastText().addTspan(svgTsp);
	   } else if (qName.equalsIgnoreCase("font")) {
		   bSetTextColor = false;
		   this.textColor = "black";
	   }
   }
   
   @Override
   public void characters(char ch[], int start, int length) throws SAXException {
	   String stCurText = new String(ch, start, length);
	   
	   if(length == 0)  return;
	   
	   stCurText = stCurText.replaceAll("[\n|\t]", "");
	   
	   if(stCurText.length() > 0) 
	   {
		   int curTextWidth = 0;
		   String [] words = null;
		   
		   int _fontStyle = bStrong ? Font.BOLD : Font.PLAIN;
		   font = new Font(this.fontFamily, _fontStyle, this.fontSize);	
		   fm = StringUtil.getFontMatrix(font);
		   
		   curTextWidth = fm.stringWidth(stCurText);					   
		   			   
		   if(this.nCurCharPosX + curTextWidth < this.nDocWidth)
		   {
			   this.nCurCharPosX += curTextWidth;
			   log.debug(getClass().getName() + "::" + "[CASE1] CurPosX-" + this.nCurCharPosX + ":Depth" + nCurDepth + ":characters=" + stCurText);

			   svgTspan svgTsp = new svgTspan();
			   if(font.isBold())
				   svgTsp.setFontWeight("bold");
			   if(!this.textDecoration.equals(""))
				   svgTsp.setFontStyle(this.textDecoration);
			   if(!this.textColor.equals("black"))
				   svgTsp.setFontColor(this.textColor);
			   svgTsp.setText(stCurText);
			   this.svgTbl.getLastGroup().getLastText().addTspan(svgTsp);
		   }
		   else
		   {
			   words = stCurText.split(" ");
			   
			   String formatted = "";
			   int n = 0;
			   String currentLine = "";
			   while(n < words.length)
			   {
				   boolean isNewLine = currentLine.equals("");
				   String testOverlap = currentLine + " " + words[n];
				   
				   // are we over width?
			       int w = fm.stringWidth(testOverlap);
			       if(this.nCurCharPosX + w < this.nDocWidth)
			       {
			    	   if (!isNewLine) currentLine += " "; 		
			    	   currentLine += words[n];
			       }
			       else
			       {
			    	   if (!isNewLine) currentLine += " "; 		
			    	   this.nCurCharPosX += fm.stringWidth(currentLine);			  
			    	   	
			    	   if(this.isUseWordWrap)
			    	   {
			    		   /*
			    		    * 워드 단위로 자르기 하는 경우의 구현처리
			    		    */ 
				    	   // if this hits, we got a word that need to be hypenated
				           if (isNewLine) {
				                String wordOverlap = "";
	
				                // test word length until its over maxW
				                for (int i = 0; i < words[n].length(); i++) {
				                    wordOverlap += words[n].charAt(i);
				                    String withHypeh = wordOverlap + "-";
				                    
				                    if (this.nCurCharPosX + fm.stringWidth(withHypeh) >= this.nDocWidth) {
				                        // add hyphen when splitting a word
				                        withHypeh = wordOverlap.substring(0, wordOverlap.length() - 1) + "-";
				                        // update current word with remainder
				                        words[n] = words[n].substring(wordOverlap.length() - 1, words[n].length());
				                        formatted += withHypeh; // add hypenated word
				                        break;
				                    }
				                }
				           }
			    	   }
			    	   else
			    	   {
			    		   /*
			    		    * 문자 단위로 자르기 하는 경우의 구현처리
			    		    */     
		                    String wordOverlap = "";
	
			                // test word length until its over maxW
			                for (int i = 0; i < words[n].length(); i++) {
			                    wordOverlap += words[n].charAt(i);
			                    String withHypeh = wordOverlap + "-";
			                    
			                    if (this.nCurCharPosX + fm.stringWidth(withHypeh) >= this.nDocWidth) {
			                        // update current word with remainder
			                        words[n] = words[n].substring(wordOverlap.length() - 1, words[n].length());
			                        break;
			                    }
			                    else
			                    {
			                    	currentLine += words[n].charAt(i);
			                    }
			                }
			    	   }
			            
			           this.nCurCharPosX = this.svgTbl.getLastGroup().getLastText().getIndent();
			            
			           log.debug(getClass().getName() + "::" + "[CASE2] CurPosX-" + this.nCurCharPosX + ":Depth" + nCurDepth + ":characters=" + currentLine);
					     	
			           formatted += currentLine + '\n';
			           currentLine = "";

			           continue; // restart cycle
			       }
			       
			       n++;
			   }
			   
			   if (!currentLine.equals("")) {
				  	
				   	this.nCurCharPosX += fm.stringWidth(currentLine);
				   
				   	log.debug(getClass().getName() + "::" + "[CASE3] CurPosX-" + this.nCurCharPosX + ":Depth" + nCurDepth + ":characters=" + currentLine);
				  	
			        formatted += currentLine + '\n';
			        currentLine = "";
			   }

			   // get rid of empy newline at the end
			   if(formatted.length() > 0)
			   {
				   formatted = formatted.substring(0, formatted.length() - 1);
			   }
			   
			   String [] rows = formatted.split("\n");
			   for(int k=0; k< rows.length; k++)
			   {
				   if(k==0)
				   {
					   svgTspan svgTsp = new svgTspan();
					   svgTsp.setDx(0);
					   if(font.isBold())
						   svgTsp.setFontWeight("bold");
					   if(!this.textDecoration.equals("")) 
						   svgTsp.setFontStyle(this.textDecoration);
					   if(!this.textColor.equals("black")) 
						   svgTsp.setFontColor(this.textColor);
					   svgTsp.setText(rows[k]);
					   this.svgTbl.getLastGroup().getLastText().addTspan(svgTsp);
				   }
				   else
				   {
					   //this.breakLineCount++;
					   try {
						   this.addRow();
					   } catch (HeightOverflowException e) {
						   //e.printStackTrace();
						   throw new SAXException("HeightOverflowException");
					   }
					   
					   int nIndent = this.svgTbl.getLastGroup().getLastText().getIndent();
					   
					   svgTspan svgTsp = new svgTspan();
					   
					   if(nCurDepth==3)
						   svgTsp.setDx(nIndent - INDENT_DEP3);
					   else if(nCurDepth==2)
						   svgTsp.setDx(nIndent - INDENT_DEP2);
					   else
						   svgTsp.setDx(nIndent - INDENT_DEP1);
					   
					   if(!isNewLineFlag)
					   {
						   svgTsp.setDy(this.nLineHeight);
					   }
					   else
					   {
						   isNewLineFlag = false;
					   }
						   
					   if(font.isBold())
						   svgTsp.setFontWeight("bold");
					   if(!this.textDecoration.equals("")) 
						   svgTsp.setFontStyle(this.textDecoration);
					   if(!this.textColor.equals("black")) 
						   svgTsp.setFontColor(this.textColor);
					   svgTsp.setText(rows[k]);
					   this.svgTbl.getLastGroup().getLastText().addTspan(svgTsp);
				   }
			   }
		   }
	   }
	   
	   if( isNewLineFlag ) isNewLineFlag = false;
   }
   
   /**
    * Get SVG Table Object
    * 
    * @return
    */
   public svgTable getSvgTableObject() 
   {
	   return this.svgTbl;
   }
   /**
    * Set SVG Table Object
    * 
    * @return
    */
   public void setSvgTableObject( svgTable _tbl ) 
   {
	   this.svgTbl = _tbl;
   }
   /**
    * Get SVG Table Object Row Count
    * 
    * @return
    */
   public int getRowCount() 
   {
	   return breakLineCount;
   }
   
   /**
    * svgTable 객체로부터 얻은 정보를 바탕으로 SVG Tag를 생성한다.
    * tspan tag를 사용하여 생성.
    * 
    * @return
    */
   public String getSvg()
   {
	   StringBuilder stbSvgTag = new StringBuilder();
	   
	   int yPos = this.nLineHeight;
	   
	   int _dxAddedValue = 0;
	   int _dyAddedValue = 0;
	   
	   stbSvgTag.append("<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" ");
	   stbSvgTag.append("width=\"" + this.svgTbl.getWidth() + "\" height=\"" + this.svgTbl.getHeight() + "\">");
	   
	   for(int i=0; i< this.svgTbl.getGroupCount(); i++)
	   {
		   svgGroup svgGrp = this.svgTbl.getGroup(i);
		   stbSvgTag.append("<g style=\"font-family:" + svgGrp.getFontFamily() + "; font-size:" + svgGrp.getFontSize() + "px;\">");
			
		   for(int j=0; j< svgGrp.getTextCount(); j++)
		   {
			   svgText svgTxt = svgGrp.getTexts().get(j);
			   stbSvgTag.append("<text x=\"" + svgTxt.getX() + "\" y=\"" + yPos + "\">");
			   
			   for(int k=0; k < svgTxt.getTspanCount(); k++)
			   {
				   svgTspan svgTsp = svgTxt.getTspans().get(k);
				   yPos = yPos + svgTsp.getDy();
				   
				   // < 태그를 &lt; 로 변환
				   if( svgTsp.getText().equals("<") )
				   {
					   svgTsp.setText("&lt;");
				   }
				   // > 태그를 &gt; 로 변환
				   if( svgTsp.getText().equals(">") )
				   {
					   svgTsp.setText("&gt;");
				   }
					  
				   if(svgTsp.getText().equals(""))
				   {
					   _dxAddedValue = svgTsp.getDx();
					   _dyAddedValue = svgTsp.getDy();
					   continue;
				   }
				   
				   if(svgTsp.getDy() > 0 || _dyAddedValue > 0)
				   {
					   if(svgTsp.getFontWeight().equals("normal"))
					   {
						   if(!svgTsp.getFontStyle().equals(""))
							   stbSvgTag.append("<tspan x=\"" + svgTxt.getX() + "\" dx=\"" + (svgTsp.getDx()+_dxAddedValue) + "\" dy=\"" + (svgTsp.getDy()+_dyAddedValue) + "\" text-decoration=\"" + svgTsp.getFontStyle() + "\" fill=\"" + svgTsp.getFontColor() + "\">");
						   else
							   stbSvgTag.append("<tspan x=\"" + svgTxt.getX() + "\" dx=\"" + (svgTsp.getDx()+_dxAddedValue) + "\" dy=\"" + (svgTsp.getDy()+_dyAddedValue) + "\" fill=\"" + svgTsp.getFontColor() + "\">");
					   }
					   else
					   {
						   if(!svgTsp.getFontStyle().equals(""))
							   stbSvgTag.append("<tspan x=\"" + svgTxt.getX() + "\" dx=\"" + (svgTsp.getDx()+_dxAddedValue) + "\" dy=\"" + (svgTsp.getDy()+_dyAddedValue) + "\" font-weight=\"" + svgTsp.getFontWeight() + "\" text-decoration=\"" + svgTsp.getFontStyle() + "\" fill=\"" + svgTsp.getFontColor() + "\">");
						   else
							   stbSvgTag.append("<tspan x=\"" + svgTxt.getX() + "\" dx=\"" + (svgTsp.getDx()+_dxAddedValue) + "\" dy=\"" + (svgTsp.getDy()+_dyAddedValue) + "\" font-weight=\"" + svgTsp.getFontWeight() + "\" fill=\"" + svgTsp.getFontColor() + "\">");
					   }
					   stbSvgTag.append(svgTsp.getText());
					   stbSvgTag.append("</tspan>");
				   }
				   else
				   {
					   if(svgTsp.getFontWeight().equals("normal"))
					   {
						   if(!svgTsp.getFontStyle().equals(""))
							   stbSvgTag.append("<tspan dx=\"" + (svgTsp.getDx()+_dxAddedValue) + "\" text-decoration=\"" + svgTsp.getFontStyle() + "\" fill=\"" + svgTsp.getFontColor() + "\">");
						   else
							   stbSvgTag.append("<tspan dx=\"" + (svgTsp.getDx()+_dxAddedValue) + "\" fill=\"" + svgTsp.getFontColor() + "\">");
					   }
					   else
					   {
						   if(!svgTsp.getFontStyle().equals(""))
							   stbSvgTag.append("<tspan dx=\"" + (svgTsp.getDx()+_dxAddedValue) + "\" font-weight=\"" + svgTsp.getFontWeight() + "\" text-decoration=\"" + svgTsp.getFontStyle() + "\" fill=\"" + svgTsp.getFontColor() + "\">");
						   else
							   stbSvgTag.append("<tspan dx=\"" + (svgTsp.getDx()+_dxAddedValue) + "\" font-weight=\"" + svgTsp.getFontWeight() + "\" fill=\"" + svgTsp.getFontColor() + "\">");
					   }
					   stbSvgTag.append(svgTsp.getText());
					   stbSvgTag.append("</tspan>");
				   }
				   
				   _dxAddedValue = 0;
				   _dyAddedValue = 0;
			   }
			   
			   stbSvgTag.append("</text>");
			   
			   if(svgTxt.getTspanCount() > 0)
				   yPos = yPos + this.nLineHeight;
		   }
		   
		   stbSvgTag.append("</g>");
	   }	   
	   
	   stbSvgTag.append("</svg>");
	   
	   return stbSvgTag.toString();
   }
   

   /**
    * svgTable 객체로부터 얻은 정보를 바탕으로 RowList정보를 생성한다.
    * 
    * @return
    */
   public ArrayList< ArrayList<HashMap<String, Object>> > getSvgRowItems()
   {
	   StringBuilder stbSvgTag = new StringBuilder();
	   ArrayList<HashMap<String,String>> _rowItems = new ArrayList<HashMap<String,String>>();
	   
	   int xPos = 0;
	   int yPos = this.nLineHeight;
	   
	   ArrayList< HashMap<String, Object> > _groups = new ArrayList<HashMap<String, Object>>();
	   HashMap<String, Object> _groupItem = new HashMap<String, Object>();
	   
	   ArrayList< ArrayList<HashMap<String, Object>> > _rows = new ArrayList< ArrayList<HashMap<String, Object>> >();

	   for(int i=0; i< this.svgTbl.getGroupCount(); i++)
	   {
		   svgGroup svgGrp = this.svgTbl.getGroup(i);
		   stbSvgTag.append("<g style=\"font-family:" + svgGrp.getFontFamily() + "; font-size:" + svgGrp.getFontSize() + "px;\">");
		   
//		   _groupItem = new HashMap<String, Object>();
//		   
//		   _groupItem.put("fontFamily", svgGrp.getFontFamily());
//		   _groupItem.put("fontSize", svgGrp.getFontSize());
		   
		   ArrayList<HashMap<String, Object>> _line = new ArrayList<HashMap<String, Object>>();
		   String _addText = "";
		   
		   for(int j=0; j< svgGrp.getTextCount(); j++)
		   {
			   _line = new ArrayList<HashMap<String, Object>>();
			   
			   svgText svgTxt = svgGrp.getTexts().get(j);
			   xPos = svgTxt.getX();
			   float _argoY = 0;
			   
			   for(int k=0; k < svgTxt.getTspanCount(); k++) 
			   {
				   svgTspan svgTsp = svgTxt.getTspans().get(k);
				   _addText = "";
				   
				   yPos = yPos + svgTsp.getDy();
				   xPos = svgTsp.getDy() > 0 ? svgTxt.getX() + svgTsp.getDx() : xPos + svgTsp.getDx();
				  
				   if( k == 0 )
				   {
					   _argoY = yPos;
				   }
				   
				   HashMap<String, Object> _tspan = new HashMap<String, Object>();
				   
				   _tspan.put("fontFamily",svgTsp.getFontFamily() );
				   _tspan.put("fontWeight",svgTsp.getFontWeight() );
				   _tspan.put("fontSize",svgTsp.getFontSize() );
				   _tspan.put("fontColor",svgTsp.getFontColor() );
				   _tspan.put("text-decoration",svgTsp.getFontStyle());
				   _tspan.put("text",svgTsp.getText() );
				   
				   // fontColorInt값을 처리하기 위해 16진수 값을 변환한다.
				   if( _tspan.get("fontColor").toString().charAt(0) == '#' )
				   {
					   _tspan.put("fontColorInt",Integer.parseInt(_tspan.get("fontColor").toString().substring(1), 16) );
				   }
				   
				   if(_argoY < yPos)
				   {
					   if( _line.size() > 0 )
					   {
						   _rows.add(_line);
					   }
					   _line = new ArrayList<HashMap<String, Object>>();
				   }
				   
				   _argoY = yPos;
				   
				   if(svgTsp.getFontWeight().equals("normal"))
				   {
					   font = new Font(svgTsp.getFontFamily(), Font.PLAIN, svgTsp.getFontSize());	
				   }
				   else
				   {
					   font = new Font(svgTsp.getFontFamily(), Font.BOLD, svgTsp.getFontSize());
				   }

				   fm = StringUtil.getFontMatrix(font);
				   xPos =  xPos + fm.stringWidth(svgTsp.getText());
				   
				   if(_line.size() == 0 )
				   {
					   font = new Font(svgTsp.getFontFamily(), Font.PLAIN, svgTsp.getFontSize());
					   fm = StringUtil.getFontMatrix(font);
					   
					   int _spaceWidth = fm.stringWidth(" ");
					   int _positionX = svgTxt.getX() + svgTsp.getDx();
					   int _spaceCnt = (int) Math.round( (float) _positionX/_spaceWidth );
					  
					   for (int l = 0; l < _spaceCnt; l++) {
						   _addText += " ";
					   }
					   _tspan.put("text",_addText + svgTsp.getText() );
				   }
				   
				   _line.add(_tspan);
			   }
			   
			   if(svgTxt.getTspanCount() > 0)
			   {
				   _rows.add(_line);				   
				   yPos = yPos + this.nLineHeight;
			   }
		   }
		   
//		   _groupItem.put("lines", _rows);
//		   _groups.add(_groupItem);
	   }	   
	   
	   return _rows;
   }
   
   
   
   /**
    * Item Text Properties 
    * */
   public enum EBulletType{ sqrB, sqrW, tria, strS, noti, fngr, gana, numR, numD, dash };
   
   /**
    * Get bullet string
    * 
    * @param type
    * @return
    */
   	private String getBullet(String type)
   	{
	   String bullet = "";
	   String gana[] = { "가.", "나.", "다.", "라.", "마.", "바." , "사." , "아." , "자." , "차." , "카." , "타." , "파." , "하." };
	   String numR[] = { "①", "②", "③", "④", "⑤", "⑥" , "⑦" , "⑧" , "⑨" , "⑩" , "⑪" , "⑫" , "⑬" , "⑭" };
	   String numD[] = { "1.", "2.", "3.", "4.", "5.", "6." , "7." , "8." , "9." , "10." , "11." , "12." , "13." , "14." };
	   
	   switch(EBulletType.valueOf(type))
	   {
	   		case sqrB:
	   			bullet = "■";
	   			break;
	   		case sqrW:
	   			bullet = "□";
	   			break;
	   		case tria:
	   			bullet = "▶";
	   			break;
	   		case strS:
	   			bullet = "*";
	   			break;
	   		case noti:
	   			bullet = "※";
	   			break;
	   		case fngr:
	   			bullet = "☞";
	   			break;
	   		case dash:
	   			bullet = "-";
	   			break;
	   		case gana:
   				bullet = gana[nGanaIndex%14];
   				nGanaIndex++;
	   			break;	
	   		case numR:
   				bullet = numR[nRNumberIndex%14];
   				nRNumberIndex++;
	   			break;	
	   		case numD:
   				bullet = numD[nDNumberIndex%14];
   				nDNumberIndex++;
	   			break;		
	   		default:
	   			break;
	   }
	   
	   return bullet;
   	}
      
    @Override
    public void endDocument() throws SAXException {
    	// TODO Auto-generated method stub
    	super.endDocument();
    	
    	if( this.svgTblList.size() == 0 || this.svgTblList.get(  this.svgTblList.size() -1 ).equals(svgTbl) == false )
        {
    		this.svgTblList.add(svgTbl);
    		int _currentHeight = this.breakLineCount*this.nLineHeight;
    		if( this.nOrizinalHeight < (_currentHeight + nAdjustableGap) )
    		{
    			resultHeightList.add((float) _currentHeight  + nAdjustableGap );
    			svgTbl.setHeight( _currentHeight  + nAdjustableGap );
    		}
    		else
    		{
    			resultHeightList.add((float) this.nOrizinalHeight );
    			svgTbl.setHeight(this.nOrizinalHeight);
    		}
    		
        }
    	
    }
   	
	private void addRow() throws HeightOverflowException
	{
		// adjust for vertical offset
		int maxHAdjusted = this.nDocHeight > 0 ? this.nDocHeight - this.nLineHeight : 0;		   
		isNewLineFlag = false;
		
		if(this.isCheckRowHeight && (this.breakLineCount*this.nLineHeight >= maxHAdjusted))
		{
			throw new HeightOverflowException();
		}
		else if( this.useAdjustableHeight && (this.breakLineCount*this.nLineHeight >= maxHAdjusted) )
		{
			
			resultHeightList.add((float) this.nDocHeight + nAdjustableGap );
			
			svgTblList.add(this.svgTbl);
			
			maxHAdjusted = Float.valueOf( rowHeights[1] ).intValue() - nAdjustableGap;
			this.nDocHeight = maxHAdjusted;
			
			
			this.svgTbl = new svgTable();
			this.svgTbl.setWidth(this.nDocWidth);
			this.svgTbl.setHeight(maxHAdjusted  + nAdjustableGap );
			
			if( this.argoGroup != null )
			{
				svgGroup _svg = argoGroup.getClone();
				argoGroup = _svg;
				this.svgTbl.addGroup(_svg);
			}
			

			if( this.argoText != null )
			{
				svgText _svgTxt = argoText.getClone();
				_svgTxt.setParent( argoGroup );
				this.breakLineCount = 0;
				
				this.svgTbl.getGroup(0).getTexts().add(_svgTxt);
			}
			isNewLineFlag = true;
		}
			
		this.breakLineCount++;
	}
}
