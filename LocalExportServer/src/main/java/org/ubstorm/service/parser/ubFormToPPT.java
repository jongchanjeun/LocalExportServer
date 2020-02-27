package org.ubstorm.service.parser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationShape;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.sl.usermodel.TableCell.BorderEdge;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.ShapeTypes;
import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFColor;
import org.apache.poi.xslf.usermodel.XSLFHyperlink;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSimpleShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.json.simple.JSONObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStopList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveFixedPercentage;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.ubstorm.service.dictionary.ImageDictionary;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.xmlToUbForm.EBorderType;
import org.ubstorm.service.parser.xmlToUbForm.EButtonType;
import org.ubstorm.service.parser.xmlToUbForm.EFontWeight;
import org.ubstorm.service.parser.xmlToUbForm.ETextAlign;
import org.ubstorm.service.parser.xmlToUbForm.ETextDecoration;
import org.ubstorm.service.parser.xmlToUbForm.EVerticalAlign;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.oreilly.servlet.Base64Decoder;

public class ubFormToPPT {

	private Logger log = Logger.getLogger(getClass());
	private ArrayList<ArrayList<HashMap<String, Object>>> itemPropList;
	
	protected static ArrayList jobThreadList = null;
	Runtime runtime = Runtime.getRuntime();

	private int START_PAGE = 0;
	
	private XMLSlideShow _mXMLSlideShow;

	private XSLFSlide _mXSLFSlide;
	
	private String _mSVGfileName;
	
	private ImageDictionary mImageDictionary;
	
	private HashMap<String, Object> mChangeItemList;
	
	String[] mPreBdTypeAr = null;
	
	String mTableId = "";
	
	JSONObject mBackgroundImage;
	
	int mPageBackgroundColor = -1;
	
	public void setBackgroundImage(JSONObject _bgImage)
	{
		mBackgroundImage = _bgImage;
	}
	public void setBackgroundColor( int _value )
	{
		mPageBackgroundColor = _value;
	}
	
	public static BigInteger BIGINTEGER_ZERO=BigInteger.valueOf( 0);
	
	public void setSTART_PAGE(int sTART_PAGE) {
		START_PAGE = sTART_PAGE;
	}

	
	public XMLSlideShow xmlParsingPPT(ArrayList<ArrayList<HashMap<String, Object>>> dataMap) throws Exception {

		log.info(getClass().getName() + "::" + "Call xmlParsingPPT()...");
		
		if( dataMap == null || dataMap.size() == 0){ 
			log.error(getClass().getName() + "::" + ">>>>> dataMap parameter is null.");
			return null;
		}
		else if( _mXMLSlideShow == null ){
			log.error(getClass().getName() + "::" + ">>>>> XMLSlideShow is null.");
			return null;
		}
		
		itemPropList = dataMap;
		
		Date start = new Date();
		XMLSlideShow xMLSlideShow = toPPT();
		Date end = new Date();
		long diff = end.getTime() - start.getTime();
		long diffSec = diff / 1000% 60;         
		long diffMin = diff / (60 * 1000)% 60;        
		log.debug(getClass().getName() + "::toPPT::" + "toPPT complete. [ "+diffMin +":"+diffSec+"." + diff%1000+" ]");
		
		return xMLSlideShow;
	}

		private int pageNo=0;
	
	public XMLSlideShow xmlParsingPPT(ArrayList<ArrayList<HashMap<String, Object>>> dataMap,XMLSlideShow _xmlSs, int pNo) throws Exception {

		log.info(getClass().getName() + "::" + "Call xmlParsingPPT()...");
		
		
		if( dataMap == null || dataMap.size() == 0){ 
			log.error(getClass().getName() + "::" + "dataMap parameter is null.");
			return null;
		}
		
		itemPropList = dataMap;
		_mXMLSlideShow = _xmlSs;
		_xmlSs = null;
		pageNo = pNo;
		
		XMLSlideShow xMLSlideShow = toPPT();
		
		
		return xMLSlideShow;
	}

	protected void finalize() throws Throwable {
		// Invoke the finalizer of our superclass
		// We haven't discussed superclasses or this syntax yet
		super.finalize();

		// Delete a temporary file we were using
		// If the file doesn't exist or tempfile is null, this can throw
		// an exception, but that exception is ignored.
	}
	
	/*****************************************************
	*	파워포인트 문서 생성.
	* 	@return XSMLSlideShow
	*****************************************************/
	private XMLSlideShow toPPT() throws Exception{
		
		log.info(getClass().getName() + "::" + "Start Parsing PPT...");

		_mXSLFSlide =  _mXMLSlideShow.createSlide();
		
		if( mPageBackgroundColor != -1 )
		{
			  if (_mXSLFSlide.getXmlObject().getCSld().getBg() == null)
			  {
				  _mXSLFSlide.getXmlObject().getCSld().addNewBg();
			  }
			  _mXSLFSlide.getBackground().setFillColor( new Color( mPageBackgroundColor )  );
		}
		
		for (ArrayList<HashMap<String, Object>> pageAr : itemPropList) {
			Date begin = new Date();
			
			if( itemPropList.indexOf(pageAr) == 0 && pageAr.size() == 1 ){
				HashMap<String, Object> _item = pageAr.get(0);
				if( _item.containsKey("waterMark") ){
					makeWaterMark(String.valueOf(_item.get("waterMark")));
					continue;
				}
			}
			
			if( mBackgroundImage != null )
			{
				drawBackGroundImage(mBackgroundImage);
			}
			
			if(mChangeItemList != null && mChangeItemList.size() > 0)
			{
				pptChangeDataMapping(mChangeItemList, pageAr, pageNo);
			}
			for (HashMap<String, Object> _item : pageAr) {
				// page 정보를 담고있는 아이템인가?
				Object pH = null;
				Object pW = null;
				if ( ((pH=_item.get("cPHeight")) != null) && ((pW=_item.get("cPWidth")) != null) ) {
					
					
					int pHeight = Double.valueOf( stringToDouble( String.valueOf( toNum(pH)) ) ).intValue();
					int pWidth	= Double.valueOf( stringToDouble( String.valueOf( toNum(pW)) ) ).intValue();
					
//					int pHeight = toNum(pH);
//					int pWidth= toNum(pW);
//					
					_mXMLSlideShow.setPageSize(new Dimension(pWidth,pHeight));
				}
				else{
					// 클래스명이 없다면 패스
					if( _item.get("className") == null ) continue;

					// visible false인 아이템은 패스!
					if( _item.get("visible") != null && String.valueOf(_item.get("visible")).equals("false") ) continue;
					

					String itemClass = String.valueOf(_item.get("className"));

					try {

						// 아이템이 라벨 종류인 경우
						if( itemClass.contains("Label") || itemClass.equals("UBApproval") 
							|| itemClass.equals("UBTextInput") || itemClass.equals("UBTextArea") ){
							XSLFTextBox label = makeLabel(_item);
						}
						else if( itemClass.equals("UBComboBox") || itemClass.equals("UBDateFiled") ){
							// 콤보, 데이트필드의 경우 테두리를 그리지 않도록 한다. - 이장환이사님의견 반영.
							_item.put("borderWidth", "0" );
							_item.put("isCell", "false" );
							_item.put("borderType", "none" );
							_item.put("borderSide", "[]" );
							
							XSLFTextBox label = makeLabel(_item);
						}
						else if( itemClass.equals("UBRadioBorder") || itemClass.equals("UBCheckBox") ){
							XSLFTextBox label = makeLabel(setEformItemAttr(_item));
						}
						else if( itemClass.equals("UBTable") ){
							XSLFTable table = makeTable(_item);
							if( table != null ){
								table.getCTTable().validate();
								tableCellMerge(_item, table);
							}
						}
						else if( itemClass.equals("UBGraphicsCircle") ){
							XSLFAutoShape circle = makeCircle(_item);
						}
						// UBGraphicsRectangle || UBGraphicsGradiantRectangle
						else if( itemClass.contains("Rectangle") ){
							XSLFAutoShape rect = makeRect(_item);
						}
						else if( itemClass.equals("UBGraphicsLine") ){
							XSLFSimpleShape line = makeLine(_item);
						}
						else if( itemClass.equals("UBConnectLine") ){
							XSLFSimpleShape line = makeConnectLine(_item);
						}
						// svg 형식의 text
						// High Chart : svg 태그를 base64로 변환하여 src 속성값 변경 후 이미지 생성 함수를 태운다.
						//else if( itemClass.equals("UBSVGRichText") || itemClass.equals("UBSVGArea") ){
						else if( itemClass.equals("UBSVGRichText") || itemClass.equals("UBSVGArea") || itemClass.equals("UBQRCode") ){
							
							if( itemClass.equals("UBQRCode") )
							{
								String _svgData = _item.get("src").toString().substring(4);
								_item.put("data", _svgData);
							}	
							
							String _src = makeSVGArea(_item);
							_item.put("src", _src);
							makeImageBase64(_item);
						}
						else if( itemClass.equals("UBClipArtContainer") ){
							
							String _src = CreateClipImage(_item);
							if( !_src.equals("null") && !_src.equals("") ){
								_item.put("src", _src);
								makeImageBase64(_item);
							}
						}
						else if( itemClass.equals("UBSignature") || itemClass.equals("UBPicture") || itemClass.equals("UBTextSignature")){
							if( _item.get("src") != null ){
								makeImageBase64(_item);
							}
						}
						// 나머지 타입은 아미지로 처리.
						else{
							if( _item.get("src") != null ){
								makeImage(_item);
							}
							
							else{
								log.error(getClass().getName() + "::toPPT::" + ">>>>>>>>>>[ "+_item.get("className")+"\t"+_item.get("id")+" ] item's src value is empty.");
							}
						}
						
					} catch (Exception e) {
						log.error(getClass().getName() + "::toPPT::" + ">>>>>>>>>>[ "+_item.get("className")+"\t"+_item.get("id")+" ] parsing Error. >>>"+e.getMessage());
//						File file = new File(_mSVGfileName);
//						file.delete();
					}
				}
			}// For item End

			Date end = new Date();
			long diff = end.getTime() - begin.getTime();
			long diffSec = diff / 1000% 60;         
			long diffMin = diff / (60 * 1000)% 60;
			
			log.debug(getClass().getName() + "::toPPT::" +pageNo+" page [ "+diffMin +":"+diffSec+"." + diff%1000+" ]\t\t"+runtime.freeMemory()+" KB");
			
		}// For data list End
		
		return _mXMLSlideShow;
	}// toPPT End
	

	/**
	 * <pre>
	 * 라디오버튼/체크박스 아이템을 선택된 상태에 따라 문자로 치환하여 표시한다.
	 * 라디오 :  "⊙ ":"○ "
	 * 체크 : "▣ ":"□ "
	 * 체크박스의 경우 체크모양 딩벳기호는 깨져 네모로 대체한다.
	 * </pre>
	 * @param _item 라디오/체크박스 아이템
	 * @return _item 텍스트 값으로 변경된 아이템.
	*/
	private HashMap<String, Object> setEformItemAttr( HashMap<String, Object> _item ) {
		
		String itemClassName = ValueConverter.getString(_item.get("className"));
		boolean isSelected = ValueConverter.getBoolean(_item.get("selected"));
		String iconTxt = "";
		String _itemLabel = "";
		
		String symbolType = ValueConverter.getString(_item.get("symbolType"));

		if( itemClassName.equals("UBRadioBorder") ){
			if( symbolType.equalsIgnoreCase("check") ){
				iconTxt = (isSelected)? "▣":"□";
			}else{
				iconTxt = (isSelected)? "⊙":"○";	
			}
			
		}
		else if( itemClassName.equals("UBCheckBox") ){
			iconTxt = (isSelected)? "▣":"□";
		}
		
		_itemLabel = ValueConverter.getString(_item.get("label"));
		
		if(_itemLabel.equals("") == false && iconTxt.equals("") == false )  _itemLabel = " " + _itemLabel;
		
		_item.put("text", iconTxt+_itemLabel);
		
		return _item;
	}

	/** 
	 * <pre>
	 *  라벨 아이템 생성 함수.
	 *  파워포인트의 텍스트 상자 아이템 사용.
	 *  R/Pict/Shape/Textbox/TextboxContent/P/R/
	 * </pre>
	 * @return XSLFTextBox
	 * @throws Exception 
	 * */
	protected XSLFTextBox makeLabel( HashMap<String, Object> _item ) throws Exception {

		XSLFTextBox resultR = _mXSLFSlide.createTextBox();
		
		//Paragraph 추가시 한 단락 띄어지는 부분 삭제
		resultR.clearText();
		
		XSLFTextParagraph paragraph = resultR.addNewTextParagraph();
		
		XSLFTextRun textRun = paragraph.addNewTextRun();
		
		if( _item.get("text") != null ){
			
			String itemTxt = String.valueOf(_item.get("text"));
			
			// 줄바꿈 문자가 들어있는 경우.
			if(  itemTxt.indexOf("\\n") != -1 || itemTxt.indexOf("\\r") != -1 ){
				itemTxt = itemTxt.replace("\\n", "\n").replaceAll("\\r", "\r");
				textRun.setText(itemTxt);
			}
			// 줄바꿈 없는 경우.
			else{
				textRun.setText( itemTxt );
			}
		}
		
		if( _item.get("fontWeight") != null ){
			switch (EFontWeight.valueOf(String.valueOf(_item.get("fontWeight")))) {
			case normal:textRun.setBold(false); break;
			case bold: textRun.setBold(true); break;
			}
		}
		//fontStyle
		if( _item.get("fontStyle") != null ){
			if( String.valueOf(_item.get("fontStyle")).equals("italic") ){
				textRun.setItalic(true);
			}
		}
		//fontSize
		if( _item.get("fontSize") != null ){
//	        textRun.setFontSize(Double.valueOf(_item.get("fontSize").toString()));
	        textRun.setFontSize(  getPointToPixel(Double.valueOf(_item.get("fontSize").toString()) ) );
		}
		//fontFamily
		if( _item.get("fontFamily") != null ){
			String fname = getFontName(String.valueOf(_item.get("fontFamily")));
			textRun.setFontFamily(fname);
		}
		//textDecoration
		if( _item.get("textDecoration") != null ){
			switch (ETextDecoration.valueOf(String.valueOf(_item.get("textDecoration")))) {
			case none: textRun.setUnderlined(false); break;
			case normal: textRun.setUnderlined(false); break;
			case underline: textRun.setUnderlined(true); break;
			}
		}
		
		//TODO lineHeight Test
		if( _item.get("lineHeight") != null ){
			paragraph.setLineSpacing(Double.parseDouble(_item.get("lineHeight").toString())*100);//%로 설정
		}	
		
		
		float _alpha = 1;
		boolean _isAlpha = false;
		if( _item.get("alpha") != null ){				
			_alpha =  Float.valueOf(_item.get("alpha").toString()) ;	
			if(_alpha < 1){
				_isAlpha = true;
			}
		}			
		
		//fontColor
		if( _item.get("fontColor") != null ){
			Color _fontColor = new Color(ValueConverter.getInteger(_item.get("fontColorInt")));			
			//fontAlpha
			if( _isAlpha ){	
				_fontColor = new Color(_fontColor.getRed(),_fontColor.getGreen(),_fontColor.getBlue(),Integer.valueOf(String.valueOf(Math.round(_alpha * 255))));
			}			
			textRun.setFontColor(_fontColor);
		}		
		
		
		//textRotate
		String textRotate = String.valueOf(_item.get("textRotate"));
		if( !textRotate.equals("null") && !textRotate.equals("") ){
			// -90 ~ 90
			double tRotate = Double.valueOf(textRotate);
			switch (Short.valueOf(textRotate)) {
			case 0: resultR.setTextRotation(tRotate); break;
			case 45: resultR.setTextRotation(tRotate); break;
			case 180: resultR.setTextRotation(tRotate); break; // 지원안됨.
			case 90: resultR.setTextRotation(tRotate); break;
			case 270: resultR.setTextRotation(tRotate); break;
			}
			
		}
		//textBoxRotate
		String boxRotate = String.valueOf(_item.get("rotate"));
		if( !boxRotate.equals("null") && !boxRotate.equals("") ){
			// -90 ~ 90
			double bRotate = Double.valueOf(boxRotate);
			switch (Short.valueOf(boxRotate)) {
			case 0: resultR.setRotation(bRotate); break;
			case 45: resultR.setRotation(bRotate); break;
			case 180: resultR.setRotation(bRotate); break; // 지원안됨.
			case 90: resultR.setRotation(bRotate); break;
			case 270: resultR.setRotation(bRotate); break;
			}
			
		}
		// Border : color, type, width 체크
		String vIsCell = String.valueOf(_item.get("isCell"));
		String bdSide = String.valueOf(_item.get("borderSide"));
		boolean isCell = ( vIsCell.equals("null") || vIsCell.equals("") ) ? false :Boolean.valueOf(vIsCell);
		String bdType = "solid";
		if( bdSide.equals("null") || bdSide.equals("[]")){
			bdType = "none";
		}
		else if( isCell ){
			// isCell = true. 속성이 배열. 첫번째 원소의 값으로 border를 지정한다.
			
			// 테두리는 TOP의 타입을 따른다.
			bdSide = bdSide.replace("[", "");
			bdSide = bdSide.replace("]", "");
			bdSide = bdSide.replaceAll(" ", "");
			ArrayList<String> bdSideAr = new ArrayList<String>(Arrays.asList(bdSide.split(",")));
			int bdTopIdx = bdSideAr.indexOf("top");
			// TOP이 없는 경우 LEFT를 가져온다. right, bottom 은 없는 경우가 많으므로 따로 체크하진 않는다.
			if( bdTopIdx == -1 ) bdTopIdx = bdSideAr.indexOf("left");

			//borderTypes
			String bdTypes = String.valueOf(_item.get("borderTypes"));
			if( !bdTypes.equals("null") && !bdTypes.equals("") ){
				bdTypes = bdTypes.replace("[", "");
				bdTypes = bdTypes.replace("]", "");
				bdTypes = bdTypes.replaceAll(" ", "");
				String[] bdTypeAr = bdTypes.split(",");
				
				if( bdTypeAr != null ){
					if(  bdTopIdx != -1 && bdTopIdx <= bdTypeAr.length)
						bdType = bdTypeAr[bdTopIdx];
					// top, left 가 없으면 0번째로..
					else
						bdType = bdTypeAr[0];
				}
			}
			if( !bdType.equals("none") ){
				String bdColors = String.valueOf(_item.get("borderColors"));
				if( !bdColors.equals("null") && !bdColors.equals("")  ){
					bdColors = bdColors.replace("[", "");
					bdColors = bdColors.replace("#", "");
					bdColors = bdColors.replace("]", "");
					bdColors = bdColors.replaceAll(" ", "");
					String[] bdColorAr = bdColors.split(",");
					
					if( bdColorAr != null ){
						String colorHex = String.valueOf(bdColorAr[0]);
						int colorInt = Integer.parseInt(colorHex,16);
						Color _boderColors = new Color(colorInt);
						
						if( _isAlpha ){	
							_boderColors = new Color(_boderColors.getRed(),_boderColors.getGreen(),_boderColors.getBlue(),Integer.valueOf(String.valueOf(Math.round(_alpha * 255))));
						}
						resultR.setLineColor(_boderColors);
					}
				}
				//borderWidths
				String bdWidths = String.valueOf(_item.get("borderWidths"));
				if( !bdWidths.equals("null") && !bdWidths.equals("") ){
					bdWidths = bdWidths.replace("[", "");
					bdWidths = bdWidths.replace("]", "");
					bdWidths = bdWidths.replaceAll(" ", "");
					String[] bdWidthAr = bdWidths.split(",");
					
					if( bdWidthAr != null ) resultR.setLineWidth(Double.valueOf((bdWidthAr[0]).toString()));
				}
			}
		}
		else{
			// borderType
			bdType = String.valueOf(_item.get("borderType"));
			if( bdType.equals("null") || bdType.equals("") ){
				bdType = "none";
			}
			if( !bdType.equals("none") ){
				//borderColor
				String bdColor = String.valueOf(_item.get("borderColor"));
				Color _boderColor = new Color(ValueConverter.getInteger(_item.get("borderColor")));
				if( !bdColor.equals("null") && !bdColor.equals("") ){					
					if( _isAlpha ){	
						_boderColor = new Color(_boderColor.getRed(),_boderColor.getGreen(),_boderColor.getBlue(),Integer.valueOf(String.valueOf(Math.round(_alpha * 255))));
					}					
					resultR.setLineColor(_boderColor);
				}
				//borderWidth
				String bdWidth = String.valueOf(_item.get("borderWidth"));
				if( !bdWidth.equals("null") && !bdWidth.equals("") ){
					resultR.setLineWidth(Double.valueOf((bdWidth).toString()));
				}else{
					bdWidth = "1pt";
					resultR.setLineWidth(Double.valueOf((bdWidth).toString()));
				}
			}
		}

		// border Type 적용.
		if( bdType.equals("solid") || bdType.equals("SOLD")){
			// 일반실선이 기본.
			resultR.setLineDash(LineDash.SOLID);
		}
		else if( bdType.equals("none") || bdType.equals("") ){
			resultR.setLineDash(null);
		}
		else{
			if( bdType.equals("double") ){
				resultR.setLineCompound(LineCompound.DOUBLE);
				resultR.setLineWidth(3);
			}
			else{
				try {
					//borderType
					switch (EBorderType.valueOf(bdType)) {
					case dot: 
						resultR.setLineDash(LineDash.DOT);
						resultR.setLineCap(LineCap.ROUND);
						break;
					case dash: 
						resultR.setLineDash(LineDash.DASH);
						break;
					case dash_dot:
						resultR.setLineDash(LineDash.LG_DASH_DOT);
						break;
					case dash_dot_dot: 
						resultR.setLineDash(LineDash.LG_DASH_DOT_DOT);
						break;
					}
					
				} catch (Exception e) {
					log.error(getClass().getName()+"::>>>>>>>>>> EBorderType value is wrong : " +bdType);
				}
			}
			
		}
		
		//textAlign
		String tAlign = String.valueOf(_item.get("textAlign"));
		if( isValid(tAlign) ){
			switch ( ETextAlign.valueOf(tAlign)) {
			case left: paragraph.setTextAlign(TextAlign.LEFT); break;
			case center: paragraph.setTextAlign(TextAlign.CENTER); break;
			case right: paragraph.setTextAlign(TextAlign.RIGHT); break;
			}
		}
		
		//verticalAlign
		String vAlign = String.valueOf(_item.get("verticalAlign"));
		if( isValid(vAlign) ){
			switch ( EVerticalAlign.valueOf(vAlign)) {
			case top: resultR.setVerticalAlignment(VerticalAlignment.TOP); break;
			case middle: resultR.setVerticalAlignment(VerticalAlignment.MIDDLE); break;
			case bottom: resultR.setVerticalAlignment(VerticalAlignment.BOTTOM); break;
			}
		}
		// vAlign 속성이 빠져있어서 임시로 처리해놓음 추후 제거!
		else{
			resultR.setVerticalAlignment(VerticalAlignment.MIDDLE);
		}
		
		double oWidth = 0;
		double oHeight = 0;
		double oX = 0;
		double oY = 0;

		//width
		if( _item.get("width") != null ){
			oWidth = stringToDouble(_item.get("width").toString());
		}
		//height
		if( _item.get("height") != null ){
			oHeight = stringToDouble(_item.get("height").toString());
		}
		//x
		if( _item.get("x") != null ){
			oX = stringToDouble(_item.get("x").toString());
			/*if(resultR.getRotation() == Double.valueOf(90)){
				oX = Double.valueOf(_item.get("x").toString()) - Double.valueOf(_item.get("height").toString()) + (Double.valueOf(_item.get("height").toString())/2);
			}*/
		}
		//y
		if( _item.get("y") != null ){
			oY = stringToDouble(_item.get("y").toString());
		}
		
		
		resultR.setAnchor(new Rectangle2D.Double(oX,oY,oWidth,oHeight));
//		resultR.setTextAutofit(TextAutofit.NORMAL); text 자동 맞춤

		//backgroundColor
		if( _item.get("backgroundColorInt") != null ){
			Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));		
			float _bgAlpha = 1;
			
			if( _item.get("backgroundAlpha") != null ){		
				_bgAlpha = Float.valueOf(_item.get("backgroundAlpha").toString());
			}					
			float _fAlpha = _bgAlpha * 255;	
			if( _isAlpha ){	
				 _fAlpha = _bgAlpha * _alpha * 255;		
			}
			_bgColor = new Color(_bgColor.getRed(),_bgColor.getGreen(),_bgColor.getBlue(),Integer.valueOf(String.valueOf(Math.round(_fAlpha))));
			resultR.setFillColor(_bgColor);			
		}		

		return resultR;
	}
	
	private boolean isValid( String item ) {
		if( item == null ){
			return false;
		}
		item = item.trim();
		if( item.equals("") || item.equals("null")){
			return false;
		}
		
		return true;
	}

	public byte[] changeColorToByteAr(int _color)
	{
		java.awt.Color _c = new java.awt.Color(_color);
		byte[] color = {(byte) _c.getRed(), (byte) _c.getGreen(),(byte) _c.getBlue() };
		return color;
	}
	
	/** 
	 * <pre>
	 *  테이블 아이템 생성 함수.
	 * </pre>
	 * @param _item 테이블 아이템.
	 * @return XSLFTable
	 * @throws Exception 
	 * */
	protected XSLFTable makeTable( HashMap<String, Object> _item ) throws Exception {

		log.info(getClass().getName() + "::makeTable::Call makeTable()");
		
		XSLFTable table = _mXSLFSlide.createTable();
		
        double oWidth = 0;
        double oHeight = 0;
        double oX = 0;
        double oY = 0;
        
        if( _item.get("width") != null ){
			oWidth = stringToDouble(_item.get("width").toString());
		}
		//height
		if( _item.get("height") != null ){
			oHeight = stringToDouble(_item.get("height").toString());
		}
		//x
		if( _item.get("x") != null ){
			oX = stringToDouble(_item.get("x").toString());
		}
		//y
		if( _item.get("y") != null ){
			oY = stringToDouble(_item.get("y").toString());
		}
		
		table.setAnchor(new Rectangle2D.Double(oX,oY,oWidth,oHeight));
		
		// _item의 속성에 들어있는 컬럼갯수정보.
		int colCnt = 0;
		// rowspan 빈 셀이 필요한지 여부를 저장하는 배열.
		ArrayList<HashMap<String, Object>> rowspanEndIdxAr = new ArrayList<HashMap<String, Object>>();
		if( _item.containsKey("columnCount") ){
			colCnt = toNum(_item.get("columnCount"));
		}
		
		// 아이템에서 rows 에 담긴 셀 아이템을 가져와서 그린다.
		ArrayList<ArrayList<HashMap<String, Object>>> rowAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("rows");
		ArrayList<Float> _rowHeightAr = (ArrayList<Float>) _item.get("heightInfo");		// Row의 Height List
		int rowSize = rowAr.size();
		
		HashMap<String, Object> rowspanInfo = null;
		
		HashMap<Integer, XSLFTableCell> _argoCells = new HashMap<Integer, XSLFTableCell>();
		XSLFTableCell _emptyCell = null;
		
		for (int rIdx = 0; rIdx < rowSize; rIdx++) {

			XSLFTableRow tableRow = table.addRow();
			
			//Row의 Height값을 맵핑
			if( _rowHeightAr.size() > rIdx ){
				tableRow.setHeight( stringToDouble(_rowHeightAr.get(rIdx).toString()) );
			}
			
			ArrayList<HashMap<String, Object>> colAr = rowAr.get(rIdx);
			
			int _currentColPosition = 0;	// 현재 colAr의 인덱스
			for (int colIdx = 0; colIdx < colCnt; colIdx++) {
				
				// 빈셀 정보 받아오기.
				if(rowspanEndIdxAr.size() <= colIdx)
				{
					rowspanEndIdxAr.add(null);
				}
				
				rowspanInfo = rowspanEndIdxAr.get(colIdx);
				
				// 현재 인덱스와 colAr의 인덱스가 같을경우 셀 생성 아닐경우 에는 세로 병합된 셀인지 여부를 체크/ 병합된 셀일경우 빈 셀 생성
				if( colAr.size() > _currentColPosition && colAr.get(_currentColPosition).containsKey("colIndex") 
						&& colIdx == Integer.valueOf(colAr.get(_currentColPosition).get("colIndex").toString()) )
				{
					HashMap<String, Object> cellItem = colAr.get(_currentColPosition);
					
					XSLFTableCell tableCell = makeTableCell(cellItem, tableRow, rowSize, rIdx);
					
					if( cellItem.containsKey("CELL_TYPE") && cellItem.get("CELL_TYPE").toString().equals("EMPTY_CELL") )
					{
						_emptyCell = tableCell;
					}
					else if( _emptyCell != null )
					{
						if(tableCell.getBorderColor(BorderEdge.top) != null)_emptyCell.setBorderColor(BorderEdge.bottom, tableCell.getBorderColor(BorderEdge.top));
						if(tableCell.getBorderWidth(BorderEdge.top) != null)_emptyCell.setBorderWidth(BorderEdge.bottom, tableCell.getBorderWidth(BorderEdge.top));
						if(tableCell.getBorderDash(BorderEdge.top) != null)_emptyCell.setBorderDash(BorderEdge.bottom, tableCell.getBorderDash(BorderEdge.top));
						
						_emptyCell = null;
					}
					
					int _colIDX = Integer.valueOf(colAr.get(_currentColPosition).get("colIndex").toString());
					if( tableCell != null ){
						// 먼저 rowspan 체크
						if( cellItem.containsKey("rowSpan") ){
							int rowspanVal = toNum(cellItem.get("rowSpan"));
							if( rowspanVal > 1 ){
								int colspanVal = 0;
								if( cellItem.containsKey("colSpan") ){
									colspanVal = toNum(cellItem.get("colSpan"));
								}
								
								rowspanInfo = new HashMap<String, Object>();
								
								// null 값으로 들어있는 자리에 rowspan 인덱스 값으로 변경.
								rowspanInfo.put("index", rIdx+rowspanVal);
								if( colspanVal > 1 ){
									rowspanInfo.put("colspan", colspanVal);
								}
								if( cellItem.containsKey("colIndex") ){
									rowspanEndIdxAr.set(toNum(cellItem.get("colIndex")), rowspanInfo);
								}
							}
						}
						
						// Cell의 Border업데이트 처리 
						if( _argoCells.containsKey(_colIDX) )
						{
							if( tableCell.getBorderColor(BorderEdge.top) != null )
							{
//								_argoCells.get(_colIDX).removeBorder(BorderEdge.bottom);
//								
//								_argoCells.get(_colIDX).setBorderColor(BorderEdge.bottom, tableCell.getBorderColor(BorderEdge.top) );
//								_argoCells.get(_colIDX).setBorderWidth(BorderEdge.bottom, tableCell.getBorderWidth(BorderEdge.top) );
//								_argoCells.get(_colIDX).setBorderDash(BorderEdge.bottom, tableCell.getBorderDash(BorderEdge.top) );
							}
						}
						_argoCells.put(_colIDX, tableCell );
					} 
					
					_currentColPosition++;	// 셀 아이템포지션의 인덱스 증가
				}
				else if( rowspanEndIdxAr.get(colIdx) != null )
				{
					// 세로 병합되어있는 셀일경우 빈셀 Add
					// index = rIdx + rowspan
					Integer rowspanEndIdx = (Integer) rowspanInfo.get("index");
					if( rIdx < rowspanEndIdx ){
						// 빈 셀을 생성
						tableRow.addCell();
						
						if( rowspanInfo.containsKey("colspan") ){
							int colspanVal = toNum(rowspanInfo.get("colspan"));
							if(colspanVal > 1){
								for(int _c = 1; _c < colspanVal; _c++ )
								{
									tableRow.addCell();
								}
							}
						}
						
						// 병합이 모두 끝난경우, 해당 원소를 초기화.
						if( rowspanEndIdx - rIdx == 1 ){
							rowspanEndIdxAr.set(colIdx, null);
						}
					}
				}
				
			}
			//column width
			ArrayList<Float> _colHeightAr = (ArrayList<Float>) _item.get("widthInfo");
			List<XSLFTableCell> tableCellIdx = tableRow.getCells();
			for(int cellIndex = 0; cellIndex< tableCellIdx.size(); cellIndex++)
			{
				table.setColumnWidth(cellIndex, ( stringToDouble(_colHeightAr.get(cellIndex).toString()) ));
			}

		}
		
		return table;
	}
	
	/** 
	 * <pre>
	 *  테이블 셀 아이템 생성 함수.
	 * </pre>
	 * @return XSLFTableCell
	 * @throws Exception 
	 * */
	protected XSLFTableCell makeTableCell( HashMap<String, Object> _item, XSLFTableRow _tableRow, int _rowSize, int _rIdx ) throws Exception {
		XSLFTableCell tableCell = _tableRow.addCell();		
		String currentTableId = String.valueOf( _item.get("TABLE_ID") );
		if(currentTableId == null){
			currentTableId = String.valueOf( _item.get("id") );
		}
		
		if( _item.containsKey("colSpan") ){
			int colspanVal = toNum(_item.get("colSpan"));
			if( colspanVal > 1 ){
				//colspan 갯수 만큼 셀을 생성
				for(int i=0; i<colspanVal-1; i++){
					_tableRow.addCell();
					
				}
			}
		}
		
		XSLFTextParagraph tbCellParagraph = tableCell.addNewTextParagraph();
		XSLFTextRun tbCellTextRun = tbCellParagraph.addNewTextRun();
		//textAlign
		if( _item.containsKey("textAlign") && _item.get("textAlign") != null && _item.get("textAlign").equals("null") == false  ){			
			
			switch ( ETextAlign.valueOf(String.valueOf(_item.get("textAlign")))) {
			case left: 
				tbCellParagraph.setTextAlign(TextAlign.LEFT);
				break;
			case center:
				tbCellParagraph.setTextAlign(TextAlign.CENTER);
				break;
			case right: 
				tbCellParagraph.setTextAlign(TextAlign.RIGHT);
				break;
			}
			
		}
		else
		{
			//  null 인 셀의 경우 기본값으로 텍스트를 지정해준다
			tbCellParagraph.setTextAlign(TextAlign.CENTER);
			tbCellTextRun.setText("");
		}

		
		//TODO lineHeight Test
		if( _item.get("lineHeight") != null ){
			tbCellParagraph.setLineSpacing(Double.parseDouble(_item.get("lineHeight").toString())*100);//%로 설정
		}	
		
		
		if( _item.containsKey("text") ){
			
			String itemTxt = "";
			
			if( _item.get("text") != null)
			{
				itemTxt = String.valueOf(_item.get("text"));
				if(itemTxt.trim().length() == 0 || tbCellParagraph.getTextAlign() == TextAlign.CENTER){
					tableCell.setLeftInset(0d);
					tableCell.setRightInset(0d);
					tableCell.setTopInset(0d);
					tableCell.setBottomInset(0d); 	
					if(itemTxt.trim().length() == 0)
						itemTxt = " ";
				}				
			}
			
			// 줄바꿈 문자가 들어있는 경우.
			if(  itemTxt.indexOf("\\n") != -1 || itemTxt.indexOf("\\r") != -1 ){
				itemTxt = itemTxt.replace("\\n", "\n").replaceAll("\\r", "\r");
				tbCellTextRun.setText(itemTxt);
			}
			// 줄바꿈 없는 경우.
			else{
				tbCellTextRun.setText(itemTxt);
			}
		}
		
		//fontWeight
		if( _item.containsKey("fontWeight") ){
			switch (EFontWeight.valueOf(String.valueOf(_item.get("fontWeight")))) {
			case normal: 
				tbCellTextRun.setBold(false);
				break;
			case bold: 
				tbCellTextRun.setBold(true); 
				break;
			}
		}
		//fontStyle
		if( _item.containsKey("fontStyle") ){
			if( String.valueOf(_item.get("fontStyle")).equals("italic") ){
				tbCellTextRun.setItalic(true);
			}
		}
		//fontSize
		if( _item.containsKey("fontSize") ){
			tbCellTextRun.setFontSize( getPointToPixel( Double.valueOf(_item.get("fontSize").toString()) ) );			
		}
		//fontFamily
		if( _item.containsKey("fontFamily") ){
			String fname = getFontName(String.valueOf(_item.get("fontFamily")));
			tbCellTextRun.setFontFamily(fname);
		}
		//textDecoration
		if( _item.containsKey("textDecoration") ){
			switch (ETextDecoration.valueOf(String.valueOf(_item.get("textDecoration")))) {
			case none: 
				tbCellTextRun.setUnderlined(false);
				break;
			case normal: 
				tbCellTextRun.setUnderlined(false);
				break;
			case underline: 
				tbCellTextRun.setUnderlined(true);
				break;
			}
		}
		
		float _alpha = 1;
		boolean _isAlpha = false;
		if( _item.get("alpha") != null ){				
			_alpha =  Float.valueOf(_item.get("alpha").toString()) ;	
			if(_alpha < 1){
				_isAlpha = true;
			}
		}			
		
		//fontColor
		if( _item.containsKey("fontColor") && _item.get("fontColor") != null ){
			Color _fontColor = new Color(ValueConverter.getInteger(_item.get("fontColorInt")));			
			//fontAlpha
			if( _isAlpha ){	
				_fontColor = new Color(_fontColor.getRed(),_fontColor.getGreen(),_fontColor.getBlue(),Integer.valueOf(String.valueOf(Math.round(_alpha * 255))));
			}			
			tbCellTextRun.setFontColor(_fontColor);
		}
		
		// Border : color, type, width 체크
		
		String bdSide = String.valueOf(_item.get("borderSide"));
		String bdType = "solid";
		if( bdSide.equals("null") || bdSide.equals("[]")){
			bdType = "none";
			/*tableCell.removeBorder(BorderEdge.top);
			tableCell.removeBorder(BorderEdge.left);
			tableCell.removeBorder(BorderEdge.bottom);
			tableCell.removeBorder(BorderEdge.right);*/
		}
		else{
			// 테두리는 TOP의 타입을 따른다.
			bdSide = bdSide.replace("[", "");
			bdSide = bdSide.replace("]", "");
			bdSide = bdSide.replaceAll(" ", "");
			ArrayList<String> bdSideAr = new ArrayList<String>(Arrays.asList(bdSide.split(",")));
			int bdTopIdx = bdSideAr.indexOf("top");
			// TOP이 없는 경우 LEFT를 가져온다. right, bottom 은 없는 경우가 많으므로 따로 체크하진 않는다.
			if( bdTopIdx == -1 ) bdTopIdx = bdSideAr.indexOf("left");
			
			// 각속성을 저장할 배열들.
			String[] bdTypeAr = null;
//			String[] bdColorAr =  { "000000","000000","000000","000000" };
//			String[] bdWidthAr =  { "1","1","1","1"};
			String[] bdColorAr =  null;
			String[] bdWidthAr =  null;
			
			//borderTypes
			String bdTypes = String.valueOf(_item.get("borderTypes"));
			if( !bdTypes.equals("null") && !bdTypes.equals("") ){
				bdTypes = bdTypes.replace("[", "");
				bdTypes = bdTypes.replace("]", "");
				bdTypes = bdTypes.replaceAll(" ", "");
				bdTypeAr = bdTypes.split(",");
				if(mPreBdTypeAr == null){
					mPreBdTypeAr = bdTypeAr;
					mTableId = currentTableId;
				}
			}
			
			//band form 에서 bottom 의 border 가 none 인데  table 전체 row 값 현재 row 값 그리고 rowSpan값을 이용하여 테이블 border bottom 을 설정 하는 부분 
			
			// Border 처리 이유
			/**
			int _rowSpan = Integer.valueOf(_item.get("rowSpan").toString());
			if(mTableId.equals(currentTableId) && _rIdx < (_rowSize-1) && (_rIdx + _rowSpan) < _rowSize ){
				if(bdTypeAr[3].equals( bdTypeAr[3].toString())&&mPreBdTypeAr[0].equals("none")){
					String bdTypeV = bdTypeAr[3].toString();
					bdTypeAr[0] = bdTypeV;
					mPreBdTypeAr = bdTypeAr;
				}
				else if(bdTypeAr[0].equals("none")&&mPreBdTypeAr[0].equals(mPreBdTypeAr[0].toString())){
					String bdTypeV = mPreBdTypeAr[0].toString();
					bdTypeAr[0] = bdTypeV;
					mPreBdTypeAr = bdTypeAr;
				}
				else if(bdTypeAr[3].equals("none")&&mPreBdTypeAr[0].equals(mPreBdTypeAr[0].toString())){
					String bdTypeV = "none";
					bdTypeAr[3] = bdTypeV;
					mPreBdTypeAr = bdTypeAr;
				}
				
				mTableId = currentTableId;
			}
			else{
				mPreBdTypeAr = bdTypeAr;
				mTableId = currentTableId;
			}
			*/
			mPreBdTypeAr = bdTypeAr;
			mTableId = currentTableId;
			
			if( !bdType.equals("none") ){
				// borderColors
				String bdColors = String.valueOf(_item.get("borderColors"));
				if( !bdColors.equals("null") && !bdColors.equals("")  ){
					bdColors = bdColors.replace("[", "");
					bdColors = bdColors.replace("#", "");
					bdColors = bdColors.replace("]", "");
					bdColors = bdColors.replaceAll(" ", "");
					bdColorAr = bdColors.split(",");
				}
				//borderWidths
				String bdWidths = String.valueOf(_item.get("borderWidths"));
				if( !bdWidths.equals("null") && !bdWidths.equals("") ){
					bdWidths = bdWidths.replace("[", "");
					bdWidths = bdWidths.replace("]", "");
					bdWidths = bdWidths.replaceAll(" ", "");
					bdWidthAr = bdWidths.split(",");
				}
			}
			if( bdColorAr != null && bdTypeAr != null ){
				for (int i = 0; i < bdSideAr.size(); i++) {
					String _side = bdSideAr.get(i);
					String colorHex = String.valueOf(bdColorAr[i]);
					int colorInt = Integer.parseInt(colorHex,16);
					Color bdColor = new Color(colorInt);
					if( _isAlpha ){	
						bdColor = new Color(bdColor.getRed(),bdColor.getGreen(),bdColor.getBlue(),Integer.valueOf(String.valueOf(Math.round(_alpha * 255))));
					}
					int bdWidth = Integer.parseInt(bdWidthAr[i]);
					LineDash borderLineDash = setTableBorderStyleDash( bdTypeAr[i], bdWidth );
					LineCompound borderLineComp = null;
					if(borderLineDash == null){
						borderLineComp = setTableBorderStyleComp( bdTypeAr[i], bdWidth );
					}
					if( "top".equals(_side)){
						if(!(bdTypeAr[i].equals( "none"))){
							tableCell.setBorderColor(BorderEdge.top, bdColor);
							tableCell.setBorderWidth(BorderEdge.top, bdWidth);
						}
						
						if(borderLineDash != null){
							tableCell.setBorderDash(BorderEdge.top, borderLineDash);
						}
						else{
							tableCell.setBorderCompound(BorderEdge.top, borderLineComp);
							if(borderLineComp.equals(LineCompound.DOUBLE)){
								tableCell.setBorderWidth(BorderEdge.top, Double.valueOf(5));
							}
						}
					}
					else if( "left".equals(_side) ){
						if(!(bdTypeAr[i].equals( "none"))){
							tableCell.setBorderColor(BorderEdge.left, bdColor);
							tableCell.setBorderWidth(BorderEdge.left, bdWidth);
						}
						
						if(borderLineDash != null){
							tableCell.setBorderDash(BorderEdge.left, borderLineDash);
						}
						else{
							tableCell.setBorderCompound(BorderEdge.left, borderLineComp);
							if(borderLineComp.equals(LineCompound.DOUBLE)){
								tableCell.setBorderWidth(BorderEdge.left, Double.valueOf(5));
							}
						}
					}
					else if( "bottom".equals(_side) ){
						if(!(bdTypeAr[i].equals( "none"))){
							tableCell.setBorderColor(BorderEdge.bottom, bdColor);
							tableCell.setBorderWidth(BorderEdge.bottom, bdWidth);
						}
						
						if(borderLineDash != null){
							tableCell.setBorderDash(BorderEdge.bottom, borderLineDash);
						}
						else{
							tableCell.setBorderCompound(BorderEdge.bottom, borderLineComp);
							if(borderLineComp.equals(LineCompound.DOUBLE)){
								tableCell.setBorderWidth(BorderEdge.bottom, Double.valueOf(5));
							}
						}
					}
					else if( "right".equals(_side) ){
						if(!(bdTypeAr[i].equals( "none"))){
							tableCell.setBorderColor(BorderEdge.right, bdColor);
							tableCell.setBorderWidth(BorderEdge.right, bdWidth);
						}
						
						if(borderLineDash != null){
							tableCell.setBorderDash(BorderEdge.right, borderLineDash);
						}
						else{
							tableCell.setBorderCompound(BorderEdge.right, borderLineComp);
							if(borderLineComp.equals(LineCompound.DOUBLE)){
								tableCell.setBorderWidth(BorderEdge.right, Double.valueOf(5));
							}
					
						}
					}
				}
			}
			else{
				//log.error(getClass().getName()+"::>>>>>>>>>> border value is wrong : \nbdColorAr" +bdColorAr+", bdTypeAr : "+bdTypeAr);
			}
		}
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			switch (EVerticalAlign.valueOf(String.valueOf(_item.get("verticalAlign")))) {
			case top:	 
				tableCell.setVerticalAlignment(VerticalAlignment.TOP);
				break;
			case middle:
				tableCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
				break;
			case bottom: 
				tableCell.setVerticalAlignment(VerticalAlignment.BOTTOM);
				break;
			default:
				tableCell.setVerticalAlignment(VerticalAlignment.TOP);
				break;
			}
		}
		
		//backgroundColor
		if( _item.containsKey("backgroundColor")  && _item.get("backgroundColor")!= null){
			String colorHex = String.valueOf(_item.get("backgroundColor")).replace("#", "");
			int colorInt = Integer.parseInt(colorHex,16);
			Color cellBgColor = new Color(colorInt);
			float _bgAlpha = 1;			
			if(  _item.containsKey("backgroundAlpha")  && _item.get("backgroundAlpha") != null ){		
				_bgAlpha = Float.valueOf(_item.get("backgroundAlpha").toString());
			}					
			float _fAlpha = _bgAlpha * 255;	
			if( _isAlpha ){	
				 _fAlpha = _bgAlpha * _alpha * 255;		
			}
			cellBgColor = new Color(cellBgColor.getRed(),cellBgColor.getGreen(),cellBgColor.getBlue(),Integer.valueOf(String.valueOf(Math.round(_fAlpha))));
			
			tableCell.setFillColor(cellBgColor);
		}	
		
		return tableCell;
		
	}
	
	/** 
	 * <pre>
	 *  테이블 셀 병합 함수.
	 * </pre>
	 * 
	 * @throws Exception 
	 * */
	protected  void tableCellMerge( HashMap<String, Object> _item, XSLFTable _table ) throws Exception {

		log.info(getClass().getName() + "::makeTable::Call makeTable()");
		
		// _item의 속성에 들어있는 컬럼갯수정보.
		int colCnt = 0;
		// rowspan 빈 셀이 필요한지 여부를 저장하는 배열.
		ArrayList<HashMap<String, Object>> rowspanEndIdxAr = new ArrayList<HashMap<String, Object>>();
		if( _item.containsKey("columnCount") ){
			colCnt = toNum(_item.get("columnCount"));
		}
		
		ArrayList<ArrayList<HashMap<String, Object>>> rowAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("rows");
		int rowSize = rowAr.size();
		
		for (int rIdx = 0; rIdx < rowSize; rIdx++) {
			
			ArrayList<HashMap<String, Object>> colAr = rowAr.get(rIdx);
			
			int _currentColPosition = 0;	// 현재 colAr의 인덱스
			for (int colIdx = 0; colIdx < colCnt; colIdx++) {
				
				// 빈셀 정보 받아오기.
				if(rowspanEndIdxAr.size() <= colIdx)
				{
					rowspanEndIdxAr.add(null);
				}
				
				if( colAr.size() > _currentColPosition && colAr.get(_currentColPosition).containsKey("colIndex") 
						&& colIdx == Integer.valueOf(colAr.get(_currentColPosition).get("colIndex").toString()) )
				{
					HashMap<String, Object> cellItem = colAr.get(_currentColPosition);
					
					if( _table != null ){
						// 먼저 rowspan 체크
						if( cellItem.containsKey("rowSpan") ){
							int rowspanVal = toNum(cellItem.get("rowSpan"));
							int colspanVal = toNum(cellItem.get("colSpan"));
							if( rowspanVal > 1 || colspanVal > 1 ){
								int lastRow = rIdx + rowspanVal - 1;
								int lastCol = colIdx + colspanVal - 1;
								
								_table.mergeCells(rIdx, lastRow, colIdx, lastCol);
							}
						} 
					} 
					_currentColPosition++;	// 셀 아이템포지션의 인덱스 증가
				}

			}
			
		}
		
	}
	
	/** 
	 * <pre>
	 *  테이블 셀 테두리 설정 함수.
	 * </pre>
	 * @return LineDash
	 * 
	 * */
	private LineDash setTableBorderStyleDash(  String borderType, int borderThick ){
		
		borderType = borderType.replace("_share", "");
		
		if( borderType == null || borderType.equals("") || borderThick <= 0){
			borderType = "none";
		}
		//double 이 예약어라 enum에서 dbl로 사용.
		if( borderType.equals("double") ){
			borderType = "dbl";
		}
		// thick의 굵기를 반영하여 타입지정.
		else if( borderType.equals("solid") ){
			if( borderThick > 3) borderType = "thick";
			else if( borderThick > 1) borderType = "medium";
		}
		else if( borderType.equals("dash") ){
			if( borderThick > 1) borderType = "m_dash";
		}
		else if( borderType.equals("dash_dot") ){
			if( borderThick > 1) borderType = "m_dash_dot";
		}
		else if( borderType.equals("dash_dot_dot") ){
			if( borderThick > 1) borderType = "m_dash_dot_dot";
		}
		LineDash _strokeStyle = null;
		try {
			switch (EBorderType.valueOf(borderType)) {
			case solid: _strokeStyle =StrokeStyle.LineDash.SOLID;  break;
			case none: _strokeStyle =StrokeStyle.LineDash.SOLID;  break;
			case dot: _strokeStyle =StrokeStyle.LineDash.DOT; break;
			case dash: _strokeStyle =StrokeStyle.LineDash.DASH; break;
			case dash_dot:  _strokeStyle =StrokeStyle.LineDash.DASH_DOT; break;
			case dash_dot_dot:  _strokeStyle =StrokeStyle.LineDash.LG_DASH_DOT_DOT; break;
			case m_dash: _strokeStyle =StrokeStyle.LineDash.SYS_DASH; break;
			case m_dash_dot: _strokeStyle =StrokeStyle.LineDash.SYS_DASH_DOT; break;
			case m_dash_dot_dot: _strokeStyle =StrokeStyle.LineDash.SYS_DASH_DOT; break;
			default: _strokeStyle = null; break;
			
			}
		} catch (IllegalArgumentException e) {
			log.error(getClass().getName()+"::getBorderTypeIdx::"+"borderType argument is wrong. >>> " + borderType);
		}
		return _strokeStyle;
	}
	
	/** 
	 * <pre>
	 *  테이블 셀 테두리 설정 함수.
	 * </pre>
	 * @return LineDash
	 * 
	 * */
	private LineCompound setTableBorderStyleComp(  String borderType, int borderThick ){
		
		if( borderType == null || borderType.equals("") || borderThick <= 0){
			borderType = "none";
		}
		//double 이 예약어라 enum에서 dbl로 사용.
		if( borderType.equals("double") ){
			borderType = "dbl";
		}
		// thick의 굵기를 반영하여 타입지정.
		else if( borderType.equals("solid") ){
			if( borderThick > 3) borderType = "thick";
			else if( borderThick > 1) borderType = "medium";
		}
		else if( borderType.equals("dash") ){
			if( borderThick > 1) borderType = "m_dash";
		}
		else if( borderType.equals("dash_dot") ){
			if( borderThick > 1) borderType = "m_dash_dot";
		}
		else if( borderType.equals("dash_dot_dot") ){
			if( borderThick > 1) borderType = "m_dash_dot_dot";
		}
		LineCompound _strokeStyle = null;
		try {
			switch (EBorderType.valueOf(borderType)) {
			case SOLD:  _strokeStyle = StrokeStyle.LineCompound.THICK_THIN; break;
			case dbl: _strokeStyle = StrokeStyle.LineCompound.DOUBLE; break;
			case thick: _strokeStyle = StrokeStyle.LineCompound.THIN_THICK; break;
			case medium: _strokeStyle = StrokeStyle.LineCompound.SINGLE; break;
			
			}
		} catch (IllegalArgumentException e) {
			log.error(getClass().getName()+"::getBorderTypeIdx::"+"borderType argument is wrong. >>> " + borderType);
		}
		return _strokeStyle;
	}
	
	/**
	 * <pre>
	 *  SVG태그 형식의 텍스트 아이템 생성 함수.
	 * </pre>
	 * @return R
	 * @throws Exception 
	 * 
	*/
	/*protected String makeSVGText(HashMap<String, Object> _item) throws Exception{

		log.debug(getClass().getName() + "::makeSVGText...");
		
		XSLFTextBox svgTextBox = _mXSLFSlide.createTextBox();
		
		

		// 도형의 style 문자열로 속성 세팅.
		String styles = "position:absolute;" +
				"visibility:visible;" +
				"padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt;" +
				"mso-position-horizontal:absolute;" +
				"mso-position-vertical:absolute";

		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			String vAlign = String.valueOf(_item.get("verticalAlign"));
			if( isValid(vAlign) ){
				switch ( EVerticalAlign.valueOf(vAlign)) {
				case top: svgTextBox.setVerticalAlignment(VerticalAlignment.TOP); break;
				case middle: svgTextBox.setVerticalAlignment(VerticalAlignment.MIDDLE); break;
				case bottom: svgTextBox.setVerticalAlignment(VerticalAlignment.BOTTOM); break;
				}
			}
		}
		double oWidth = 0;
		double oHeight = 0;
		double oX = 0;
		double oY = 0;
		
		//width
		if( _item.get("width") != null ){
			oWidth = Double.valueOf(_item.get("width").toString());
		}
		//height
		if( _item.get("height") != null ){
			oHeight = Double.valueOf(_item.get("height").toString());
		}
		//x
		if( _item.get("x") != null ){
			oX = Double.valueOf(_item.get("x").toString());
		}
		//y
		if( _item.get("y") != null ){
			oY = Double.valueOf(_item.get("y").toString());
		}
//		svgTextBox.setAnchor(new Rectangle2D.Double(oX,oY,oWidth,oHeight));

		//backgroundColor
		if( _item.get("backgroundAlpha") != null ){
			String bgAlpha = String.valueOf(_item.get("backgroundAlpha"));
			if( bgAlpha.equals("0") ){
//				shape.setFilled(STTrueFalse.F);
			}
			else{
				if( _item.get("backgroundColorInt") != null ){
					String[] colorRGB = convertRGBtoInt(_item.get("backgroundColorInt"));
					int colorR = Integer.valueOf(colorRGB[0].toString());
					int colorG = Integer.valueOf(colorRGB[1].toString());
					int colorB = Integer.valueOf(colorRGB[2].toString());

					Color BgColor = new Color(colorR, colorG, colorB);
					
//					svgTextBox.setFillColor(BgColor);

					// 기본 속성
					if( !bgAlpha.equals("1") ){
						CTFill fill = vmlObjectFactory.createCTFill();
						fill.setOpacity(bgAlpha);
						JAXBElement<CTFill> fillWrapped = vmlObjectFactory.createFill(fill); 
						shape.getEGShapeElements().add(fillWrapped);
					}
				}
			}
		}
		
//		shape.setStyle(styles);

		
		String _dataStr = URLDecoder.decode( String.valueOf(_item.get("data")) , "UTF-8");
		_dataStr = _dataStr.replaceAll("%20", " ");
		log.debug(getClass().getName() + "::makeSVGText\n"+_dataStr);
		
		TranscoderInput input_svg_image = new TranscoderInput( _dataStr );    
		OutputStream png_ostream = new ByteArrayOutputStream();
		TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);        
        
		PNGTranscoder my_converter = new PNGTranscoder();
		my_converter.transcode(input_svg_image, output_png_image);
        		
		png_ostream.flush();
        
		String base64 = new String(Base64.encodeBase64(((ByteArrayOutputStream) png_ostream).toByteArray()));
		return base64;
	
		
		
		// 현재는 사이즈 무조건 1.
		int lineGap = 10;
		try {
			lineGap = Integer.parseInt(String.valueOf(_item.get("lineGap")));
		} catch (Exception e) {
			// integer 변환 에러시 default로 사용.
		}
		
//		makeSVGArea(_item, _dataStr);
		
		ArrayList<ArrayList<HashMap<String, Object>>> lineAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("children");
		for (ArrayList<HashMap<String, Object>> textAr : lineAr) {
			makeSVGTspan(textAr,lineGap, svgTextBox);
//			for (HashMap<String, Object> textItem : textAr) {
//				txbxcontent.getContent().add( makeSVGTspan(textItem) );
//			}
		}
		
//		return svgTextBox;
	}*/
	
	/**
	 * <pre>
	 *  SVG태그 형식의 텍스트 아이템 내부의 span을 각각 생성하는 함수.
	 * </pre>
	 * @return R
	 * @throws Exception 
	 * 
	 */
/*	protected XSLFTextRun makeSVGTspan(ArrayList<HashMap<String, Object>> textAr, int lineGap, XSLFTextBox _svgTextBox ) throws Exception{
		
		XSLFTextParagraph svgTextParagraph = _svgTextBox.addNewTextParagraph();
		XSLFTextRun svgTextRun = svgTextParagraph.addNewTextRun();
		
		//------------tspan에 따라 여러개의 R생성 필요(Strong 적용)
		for (HashMap<String, Object> _item : textAr) {
			
			//indent ------------임시. 추후 indent 속성값이 들어오는 경우 변경.
			if( false || _item.get("indent") != null ){
				int indent = 1;
				svgTextParagraph.setIndent(Double.valueOf(indent));
			}
			// 글머리 기호
			if( _item.get("bullet") != null ){
				svgTextParagraph.setBullet(true);
			}
			
			//lineGap ------------임시. 추후 lineGap 속성값이 들어오는 경우 사용.
			if( _item.get("lineGap") != null ){
				svgTextParagraph.setLineSpacing(Double.valueOf(_item.get("lineGap").toString()));
			}
			if( _item.get("text") != null ){
				String itemTxt = String.valueOf(_item.get("text"));
				if( itemTxt.trim().equals("") ){
					// 공백인 경우 이걸 써줘야 나타남.
				}
				svgTextRun.setText(itemTxt);
			}
			//fontWeight
			if( _item.get("fontWeight") != null ){
				switch (EFontWeight.valueOf(String.valueOf(_item.get("fontWeight")))) {
				case normal: 
					svgTextRun.setBold(false);
					break;
				case bold: 
					svgTextRun.setBold(true);
					break;
				}
			}
			//fontSize
			if( _item.get("fontSize") != null ){
		        svgTextRun.setFontSize(Double.valueOf(_item.get("fontSize").toString()));
			}
			//fontFamily
			if( _item.get("fontFamily") != null ){
				String fname = getFontName(String.valueOf(_item.get("fontFamily")));
		        svgTextRun.setFontFamily(fname);
			}
			//--------------------Prudential 은 사용안함.
			//fontStyle
			if( _item.get("fontStyle") != null ){
				if( String.valueOf(_item.get("fontStyle")).equals("italic") ){
					svgTextRun.setItalic(true);
				}
			}
			//textAlign
			if( _item.get("textAlign") != null ){
				
				switch ( ETextAlign.valueOf(String.valueOf(_item.get("textAlign")))) {
				case left: 
					svgTextParagraph.setTextAlign(TextAlign.LEFT);
					break;
				case center: 
					svgTextParagraph.setTextAlign(TextAlign.CENTER);
					break;
				case right: 
					svgTextParagraph.setTextAlign(TextAlign.RIGHT);
					break;
				}
			}
			//textDecoration
			if( _item.get("textDecoration") != null ){
				switch (ETextDecoration.valueOf(String.valueOf(_item.get("textDecoration")))) {
				case none: 
					svgTextRun.setUnderlined(false);
					break;
				case normal: 
					svgTextRun.setUnderlined(false);
					break;
				case underline: 
					svgTextRun.setUnderlined(true);
					break;
				}
			}
			//fontColor
			if( _item.get("fontColor") != null ){
				Color _fontColor = new Color(ValueConverter.getInteger(_item.get("fontColor")));
				svgTextRun.setFontColor(_fontColor);
			}
		}

		return svgTextRun;
	}*/
	
	
	/** 
	 * <pre>
	 *  원 아이템 생성 함수.
	 * </pre>
	 * @return XSLFAutoShape
	 * @throws Exception 
	 * */
	protected XSLFAutoShape makeCircle( HashMap<String, Object> _item ) throws Exception {
		
		XSLFAutoShape circle = _mXSLFSlide.createAutoShape();
		circle.setShapeType(ShapeType.ELLIPSE);
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			String vAlign = String.valueOf(_item.get("verticalAlign"));
			if( isValid(vAlign) ){
				switch ( EVerticalAlign.valueOf(vAlign)) {
				case top: circle.setVerticalAlignment(VerticalAlignment.TOP); break;
				case middle: circle.setVerticalAlignment(VerticalAlignment.MIDDLE); break;
				case bottom: circle.setVerticalAlignment(VerticalAlignment.BOTTOM); break;
				}
			}
		}
		double oWidth = 0;
		double oHeight = 0;
		double oX = 0;
		double oY = 0;
		
		//width
		if( _item.get("width") != null ){
			oWidth = stringToDouble(_item.get("width").toString());
		}
		//height
		if( _item.get("height") != null ){
			oHeight = stringToDouble(_item.get("height").toString());
		}
		//x
		if( _item.get("x") != null ){
			oX = stringToDouble(_item.get("x").toString());
		}
		//y
		if( _item.get("y") != null ){
			oY = stringToDouble(_item.get("y").toString());
		}
		circle.setAnchor(new Rectangle2D.Double(oX,oY,oWidth,oHeight));
		if( _item.get("contentBackgroundAlpha") != null ){
			String bgAlpha = String.valueOf(_item.get("contentBackgroundAlpha")).replace("0.", ".");
			if( bgAlpha.equals("0") ){
//				oval.setFilled(STTrueFalse.F);
			}
			else{
				
				// backgroundAlpha
				float _fAlpha = Float.valueOf(_item.get("contentBackgroundAlpha").toString()) * 255;		
				// backgroundColor
				if( _item.get("contentBackgroundColorInt") != null ){
					String[] colorRGB = convertRGBtoInt(_item.get("contentBackgroundColorInt"));
					int colorR = Integer.valueOf(colorRGB[0].toString());
					int colorG = Integer.valueOf(colorRGB[1].toString());
					int colorB = Integer.valueOf(colorRGB[2].toString());

					Color BgColor = new Color(colorR, colorG, colorB,Integer.valueOf(String.valueOf(Math.round(_fAlpha))) );
					
					circle.setFillColor(BgColor);
				}
			}
		}
		//borderColor
		if( _item.get("borderColorInt") != null ){
			String[] colorRGB = convertRGBtoInt(_item.get("borderColorInt"));
			int colorR = Integer.valueOf(colorRGB[0].toString());
			int colorG = Integer.valueOf(colorRGB[1].toString());
			int colorB = Integer.valueOf(colorRGB[2].toString());
			int colorA = 255;
			if( _item.get("borderAlpha") != null ){
				colorA = (int)( Float.parseFloat(_item.get("borderAlpha").toString()) * 255);
			}
			Color lineColor = new Color(colorR, colorG, colorB,colorA);
			circle.setLineColor(lineColor);
		}
		//strokeWidth
		if( _item.get("strokeWidth") != null ){
			circle.setLineWidth(Double.valueOf(_item.get("strokeWidth").toString()));
		}
		
		return circle;
	}
	
	/** 
	 * <pre>
	 *  사각형 아이템 생성 함수.
	 * </pre>
	 * @return XSLFAutoShape
	 * @throws Exception 
	 * */
	protected XSLFAutoShape makeRect( HashMap<String, Object> _item ) throws Exception {
		
		XSLFAutoShape rect = _mXSLFSlide.createAutoShape();
		rect.setShapeType(ShapeType.RECT);
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			String vAlign = String.valueOf(_item.get("verticalAlign"));
			if( isValid(vAlign) ){
				switch ( EVerticalAlign.valueOf(vAlign)) {
				case top: rect.setVerticalAlignment(VerticalAlignment.TOP); break;
				case middle: rect.setVerticalAlignment(VerticalAlignment.MIDDLE); break;
				case bottom: rect.setVerticalAlignment(VerticalAlignment.BOTTOM); break;
				}
			}
		}
		double oWidth = 0;
		double oHeight = 0;
		double oX = 0;
		double oY = 0;
		
		//width
		if( _item.get("width") != null ){
			oWidth = stringToDouble(_item.get("width").toString());
		}
		//height
		if( _item.get("height") != null ){
			oHeight = stringToDouble(_item.get("height").toString());
		}
		//x
		if( _item.get("x") != null ){
			oX = stringToDouble(_item.get("x").toString());
		}
		//y
		if( _item.get("y") != null ){
			oY = stringToDouble(_item.get("y").toString());
		}
		//z-index
		if( _item.get("isBackground") != null ){
			boolean isBackground = Boolean.valueOf(String.valueOf(_item.get("isBackground")));
			if(isBackground){
			}
		}
		
		rect.setAnchor(new Rectangle2D.Double(oX,oY,oWidth,oHeight));
		
		if( _item.get("contentBackgroundAlpha") != null ){
			String bgAlpha = String.valueOf(_item.get("contentBackgroundAlpha")).replace("0.", ".");
			
			if(_item.get("className").equals("UBGraphicsRectangle")){
				if( bgAlpha.equals("0") ){
//					oval.setFilled(STTrueFalse.F);
				}
				else{				
									
					float _fAlpha = Float.valueOf(_item.get("contentBackgroundAlpha").toString()) * 255;					
					
					// backgroundColor
					if( _item.get("contentBackgroundColorInt") != null ){
						String[] colorRGB = convertRGBtoInt(_item.get("contentBackgroundColorInt"));
						int colorR = Integer.valueOf(colorRGB[0].toString());
						int colorG = Integer.valueOf(colorRGB[1].toString());
						int colorB = Integer.valueOf(colorRGB[2].toString());
		
						//_bgColor = new Color(_bgColor.getRed(),_bgColor.getGreen(),_bgColor.getBlue(),Integer.valueOf(String.valueOf(Math.round(_fAlpha))));
						//resultR.setFillColor(_bgColor);	
						
						Color BgColor = new Color(colorR, colorG, colorB,Integer.valueOf(String.valueOf(Math.round(_fAlpha))) );
						
						rect.setFillColor(BgColor);
					}
				}
			}else{// gradient
				
		        CTShape cs = (CTShape)rect.getXmlObject();
		        CTGradientFillProperties gFill = cs.getSpPr().addNewGradFill();
		       //그라데이션 각도 조절 ***0000 *위치에 108의 배수 입력시 각도 조절 가능
		        gFill.addNewLin().setAng(5400000);
		        CTGradientStopList list = gFill.addNewGsLst();
		        
		        // set the start pos
		        CTGradientStop stop = list.addNewGs();
		       
				String[] gradient = null;
				if( _item.get("contentBackgroundColorsInt") != null ){
					String colorArStr = String.valueOf(_item.get("contentBackgroundColorsInt"));
					colorArStr = colorArStr.replace("[", "");
					colorArStr = colorArStr.replace("]", "");
					colorArStr = colorArStr.replaceAll(" ", "");
					gradient = colorArStr.split(",");
				}				
				
				CTPositiveFixedPercentage alphaPct = null;
				// 첫번째 색
				if(bgAlpha.equals("0")){
				}
				else{	
					stop.setPos(0);//첫번째 색의 채우기 양 100000 -> 100%			        
			        if( !bgAlpha.equals("1") ){
			        	java.awt.Color _c = new java.awt.Color(Integer.valueOf(gradient[0].replace("#", "")));
			    		byte[] color = {(byte) _c.getRed(), (byte) _c.getGreen(),(byte) _c.getBlue() };
			    		CTScRgbColor rgb = stop.addNewScrgbClr();
			        	rgb.setR(100000/255 *_c.getRed());
			            rgb.setG(100000/255 *_c.getGreen());
			            rgb.setB(100000/255 *_c.getBlue());
			        	alphaPct = rgb.addNewAlpha();
			        	alphaPct.setVal((int) (100000 * Float.parseFloat(bgAlpha)));
			        }else{
			        	byte[] byteColorAr = changeColorToByteAr(Integer.valueOf(gradient[0].replace("#", "")));			        	
			        	stop.addNewSrgbClr().setVal(byteColorAr);	
			        }
			        
				}
				// 두번째 색				
				if(bgAlpha.equals("0")){  
				}
				else{
					
					stop = list.addNewGs();
			        stop.setPos(100000);//첫번째 색의 채우기 양 100000 -> 100%			        
			        
			        if( !bgAlpha.equals("1") ){
			        	java.awt.Color _c = new java.awt.Color(Integer.valueOf(gradient[1].replace("#", "")));
			        	CTScRgbColor rgb = stop.addNewScrgbClr();
			        	rgb.setR(100000/255 *_c.getRed());
			            rgb.setG(100000/255 *_c.getGreen());
			            rgb.setB(100000/255 *_c.getBlue());
			        	alphaPct = rgb.addNewAlpha();
			        	alphaPct.setVal((int) (100000 * Float.parseFloat(bgAlpha)));
						
					}else{
						byte[] byteColorAr = changeColorToByteAr(Integer.valueOf(gradient[1].replace("#", "")));		
						stop.addNewSrgbClr().setVal(byteColorAr);
					}
			       
				}
			}			
		}		
		// backgroundAlphas 
		else if( _item.get("contentBackgroundAlphas") != null ){
			
			CTPositiveFixedPercentage alphaPct = null;
			String alphaArStr = String.valueOf(_item.get("contentBackgroundAlphas"));
			alphaArStr = alphaArStr.replace("[", "");
			alphaArStr = alphaArStr.replace("]", "");
			alphaArStr = alphaArStr.replaceAll(" ", "");
			String[] gradientalpha = alphaArStr.split(",");
			
	        CTShape cs = (CTShape)rect.getXmlObject();
	        CTGradientFillProperties gFill = cs.getSpPr().addNewGradFill();
	       //그라데이션 각도 조절 ***0000 *위치에 108의 배수 입력시 각도 조절 가능
	        gFill.addNewLin().setAng(5400000);
	        CTGradientStopList list = gFill.addNewGsLst();

	        // set the start pos
	        CTGradientStop stop = list.addNewGs();
	       
			String[] gradient = null;
			if( _item.get("contentBackgroundColorsInt") != null ){
				String colorArStr = String.valueOf(_item.get("contentBackgroundColorsInt"));
				colorArStr = colorArStr.replace("[", "");
				colorArStr = colorArStr.replace("]", "");
				colorArStr = colorArStr.replaceAll(" ", "");
				gradient = colorArStr.split(",");
			}
			
			if( gradientalpha.length < 2 ){
				log.error(getClass().getName()+"::makeRect::"+">>>>>>>>>>>>>>>>>>>>contentBackgroundAlphas value is wrong!");
			}
			else{
				String bgAlpha = String.valueOf(gradientalpha[0]).replace("0.", ".");
				// 첫번째 색
				if(bgAlpha.equals("0")){
				}
				else{									
					stop.setPos(0);//첫번째 색의 채우기 양 100000 -> 100%					
					
			        if( !gradientalpha[0].equals("1") ){	
			        	java.awt.Color _c = new java.awt.Color(Integer.valueOf(gradient[0].replace("#", "")));
			        	CTScRgbColor rgb = stop.addNewScrgbClr();
			        	rgb.setR(100000/255 *_c.getRed());
			            rgb.setG(100000/255 *_c.getGreen());
			            rgb.setB(100000/255 *_c.getBlue());
			        	alphaPct = rgb.addNewAlpha();
			        	alphaPct.setVal((int) (100000 * Float.parseFloat(bgAlpha)));
			        }else{
			        	byte[] byteColorAr = changeColorToByteAr(Integer.valueOf(gradient[0].replace("#", "")));
			        	stop.addNewSrgbClr().setVal( byteColorAr);
			        }
				}
				// 두번째 색
				bgAlpha = String.valueOf(gradientalpha[1]).replace("0.", ".");
				stop = list.addNewGs();
		        stop.setPos(100000);//첫번째 색의 채우기 양 100000 -> 100%
		        
				if(bgAlpha.equals("0")){
				}
				else{
					if( !bgAlpha.equals("1") ){
						java.awt.Color _c = new java.awt.Color(Integer.valueOf(gradient[1].replace("#", "")));
			        	CTScRgbColor rgb = stop.addNewScrgbClr();
			        	rgb.setR(100000/255 *_c.getRed());
			            rgb.setG(100000/255 *_c.getGreen());
			            rgb.setB(100000/255 *_c.getBlue());
			        	alphaPct = rgb.addNewAlpha();
			        	alphaPct.setVal((int) (100000 * Float.parseFloat(bgAlpha)));
					}else{
						byte[] byteColorAr = changeColorToByteAr(Integer.valueOf(gradient[1]));
						  stop.addNewSrgbClr().setVal(byteColorAr);
					}			      
				}
			}
		}
		
		//borderColor
		if( _item.get("borderColorInt") != null ){
			String[] colorRGB = convertRGBtoInt(_item.get("borderColorInt"));
			int colorR = Integer.valueOf(colorRGB[0].toString());
			int colorG = Integer.valueOf(colorRGB[1].toString());
			int colorB = Integer.valueOf(colorRGB[2].toString());
			int colorA = 255;
			if( _item.get("borderAlpha") != null ){
				colorA = (int)( Float.parseFloat(_item.get("borderAlpha").toString()) * 255);
			}
			Color lineColor = new Color(colorR, colorG, colorB,colorA);			
			rect.setLineColor(lineColor);
		}
		//strokeWidth
		if( _item.get("borderThickness") != null ){
			rect.setLineWidth(Double.valueOf(_item.get("borderThickness").toString()));
		}
		
		return rect;
	}
	
	/**
	 * 라인 아이템 생성
	 */
	protected XSLFSimpleShape makeLine( HashMap<String, Object> _item ) throws Exception {
		
		XSLFAutoShape line = _mXSLFSlide.createAutoShape();
		//line.setShapeType(ShapeType.LINE);
		
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			String vAlign = String.valueOf(_item.get("verticalAlign"));
			if( isValid(vAlign) ){
				switch ( EVerticalAlign.valueOf(vAlign)) {
				case top: line.setVerticalAlignment(VerticalAlignment.TOP); break;
				case middle: line.setVerticalAlignment(VerticalAlignment.MIDDLE); break;
				case bottom: line.setVerticalAlignment(VerticalAlignment.BOTTOM); break;
				}
			}
		}
		double oWidth = 0;
		double oHeight = 0;
		double oX = 0;
		double oY = 0;
		
		double x1 = stringToDouble(_item.get("x1").toString());
		double x2 = stringToDouble(_item.get("x2").toString());
		double y1 = stringToDouble(_item.get("y1").toString());
		double y2 = stringToDouble(_item.get("y2").toString());
		
		//width
		oWidth = Math.abs(x2-x1);
		oHeight = Math.abs(y2-y1);
//		if( x1 < x2 )
//		{
//			line.setShapeType(ShapeType.LINE);
//		}
//		else
//		{
//			line.setShapeType(ShapeType.LINE_INV);
//		}
		
		if( (x1 > x2 && y1 < y2) || ( y1 > y2 && x2 > x1 )  )
		{
			line.setShapeType(ShapeType.LINE_INV);
		}
		else
		{
			line.setShapeType(ShapeType.LINE);
		}
		
		
		
		double xPt = stringToDouble(_item.get("x").toString());
		//x
		if( xPt != -1 ){
			oX = xPt;
		}
		double yPt = stringToDouble(_item.get("y").toString());
		//y
		if( yPt != -1 ){
			oY += yPt;
		}
		line.setAnchor(new Rectangle2D.Double(oX,oY,oWidth,oHeight));
		
		//borderColor
		if( _item.get("lineColorInt") != null ){
			String[] colorRGB = convertRGBtoInt(_item.get("lineColorInt"));
			int colorR = Integer.valueOf(colorRGB[0].toString());
			int colorG = Integer.valueOf(colorRGB[1].toString());
			int colorB = Integer.valueOf(colorRGB[2].toString());

			Color LineColor = new Color(colorR, colorG, colorB);
			
			line.setLineColor(LineColor);
		}
		//strokeWidth
		if( _item.get("thickness") != null ){
			line.setLineWidth(Double.valueOf(_item.get("thickness").toString()));
		}
		
		
		return line;
	}
	
	/**
	 * ConnectLine 아이템 생성
	 */
	protected XSLFSimpleShape makeConnectLine( HashMap<String, Object> _item ) throws Exception {
		XSLFAutoShape connectLine = _mXSLFSlide.createAutoShape();
		connectLine.setShapeType(ShapeType.BENT_CONNECTOR_3);
		
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			String vAlign = String.valueOf(_item.get("verticalAlign"));
			if( isValid(vAlign) ){
				switch ( EVerticalAlign.valueOf(vAlign)) {
				case top: connectLine.setVerticalAlignment(VerticalAlignment.TOP); break;
				case middle: connectLine.setVerticalAlignment(VerticalAlignment.MIDDLE); break;
				case bottom: connectLine.setVerticalAlignment(VerticalAlignment.BOTTOM); break;
				}
			}
		}
		
		if(_item.get("startButtonType") != null){
			String _startButtonType = String.valueOf(_item.get("startButtonType"));
			if( isValid(_startButtonType) ){
				switch( EButtonType.valueOf(_startButtonType) ){
				case circle: connectLine.setLineHeadDecoration(DecorationShape.OVAL);break;
				case rectagle: connectLine.setLineHeadDecoration(DecorationShape.DIAMOND);break;
				case tryangle: connectLine.setLineHeadDecoration(DecorationShape.TRIANGLE);break;
				}
			}
		}
		
		if(_item.get("endButtonType") != null){
			String _endButtonType = String.valueOf(_item.get("endButtonType"));
			if( isValid(_endButtonType) ){
				switch( EButtonType.valueOf(_endButtonType) ){
				case circle: connectLine.setLineTailDecoration(DecorationShape.OVAL);break;
				case rectagle: connectLine.setLineTailDecoration(DecorationShape.DIAMOND);break;
				case tryangle: connectLine.setLineTailDecoration(DecorationShape.TRIANGLE);break;
				}
			}
		}
		
		double oWidth = 0;
		double oHeight = 0;
		double oX = 0;
		double oY = 0;
		//width
		if( _item.get("x2") != null ){
			oWidth = stringToDouble(_item.get("x2").toString());
		}
		double y1 = stringToDouble(_item.get("y1").toString());
		double y2 = stringToDouble(_item.get("y2").toString());
		if( y1 < y2){
			oHeight = y2;
		}
		else{
			oHeight = y1;
		}
		double xPt = stringToDouble(_item.get("x").toString());
		//x
		if( xPt != -1 ){
			oX = xPt;
		}
		double yPt = stringToDouble(_item.get("y").toString());
		//y
		if( yPt != -1 ){
			oY = yPt;
		}
		connectLine.setAnchor(new Rectangle2D.Double(oX,oY,oWidth,oHeight));
		//borderColor
		if( _item.get("lineColorInt") != null ){
			String[] colorRGB = convertRGBtoInt(_item.get("lineColorInt"));
			int colorR = Integer.valueOf(colorRGB[0].toString());
			int colorG = Integer.valueOf(colorRGB[1].toString());
			int colorB = Integer.valueOf(colorRGB[2].toString());

			Color LineColor = new Color(colorR, colorG, colorB);
			connectLine.setLineColor(LineColor);
		}
		//strokeWidth
		if( _item.get("thickness") != null ){
			connectLine.setLineWidth(Double.valueOf(_item.get("thickness").toString()));
		}
		
		return connectLine;
	}

	/**
	 * <pre>
	 * Key : Encoded Font name
	 * Value : Decoded Font name 
	 * </pre>
	 * */
	private HashMap<String, String> encodedFont = new HashMap<String, String>(); 
	
	/**
	 * <pre>
	 * URLDecoder 실행하는 경우 시간이 오래걸려서, 폰트명을 저장해두고 쓴다.
	 * </pre>
	 * */
	private String getFontName( String fname ) throws Exception {
		if( encodedFont.containsKey(fname) ){
			return encodedFont.get(fname);
		}
		else{
			String val = URLDecoder.decode(fname, "UTF-8");
			encodedFont.put(fname, val);
			return val;
		}
	}
	
	/**
	 * 이미지 타입 아이템 생성.
	 * @param _item item정보를 담고있는 HashMap
	 * 
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws BadElementException 
	 * */
	private void makeImage( HashMap<String, Object> _item ) throws URISyntaxException, IOException, BadElementException{
		
		String _imageUrl = ValueConverter.getString(_item.get("src"));
		if( _imageUrl.equals("") || _imageUrl.equals("null") ){
			log.error(getClass().getName()+"::makeImage::"+">>>>>>>> Item's src property is not exist.");
			return;
		}
		//byte[] bAr = common.getBytesRemoteImageFile(_imageUrl);
		byte[] bAr = common.getBytesLocalImageFile(_imageUrl);
		Image _image;
		if(bAr != null)
		{
			/* Create the drawing container */
			XSLFPictureData img = _mXMLSlideShow.addPicture(bAr,  org.apache.poi.sl.usermodel.PictureData.PictureType.PNG);
			XSLFPictureShape imageShape = _mXSLFSlide.createPicture(img);			
			//verticalAlign
			/*if( _item.get("verticalAlign") != null ){
				String vAlign = String.valueOf(_item.get("verticalAlign"));
				if( isValid(vAlign) ){
					switch ( EVerticalAlign.valueOf(vAlign)) {
					case top: imageShape.setVerticalAlignment(VerticalAlignment.TOP); break;
					case middle: connectLine.setVerticalAlignment(VerticalAlignment.MIDDLE); break;
					case bottom: connectLine.setVerticalAlignment(VerticalAlignment.BOTTOM); break;
					}
				}
			}*/
			double oWidth = 0;
			double oHeight = 0;
			double oX = 0;
			double oY = 0;
			int _rotate = 0;
			
			if(_item.containsKey("rotate"))
			{
				_rotate = Float.valueOf( _item.get("rotate").toString() ).intValue();
			}
			
			//width
			//width
			if( _item.get("width") != null ){
				oWidth = stringToDouble(_item.get("width").toString());
			}
			//height
			if( _item.get("height") != null ){
				oHeight = stringToDouble(_item.get("height").toString());
			}
			//x
			if( _item.get("x") != null ){
				oX = stringToDouble(_item.get("x").toString());
			}
			//y
			if( _item.get("y") != null ){
				oY = stringToDouble(_item.get("y").toString());
			}
			
			if( _rotate != 0 )
			{
				float _w = (float) (oWidth / 2) * -1; 
				float _h = (float) (oHeight / 2) * -1; 

				Point _rotPosition = common.rotationPosition(_w , _h,_rotate);
				
				oX = Double.valueOf( oX + ( _w - _rotPosition.getX() ) ).floatValue();
				oY = Double.valueOf( oY + ( _h - _rotPosition.getY() ) ).floatValue();
			}
			
			
			boolean _isOriginSize = false;
			if( _item.containsKey("isOriginalSize") &&  !_item.get("isOriginalSize").equals(""))
			{
				_isOriginSize = Boolean.valueOf(_item.get("isOriginalSize").toString());
			}
			if(_isOriginSize){
				_image = Image.getInstance(bAr);
				HashMap<String,Float> _orignSize = common.getOriginSize(oWidth,oHeight,_image);
				oWidth = Double.valueOf( _orignSize.get("width"));
				oHeight = Double.valueOf(_orignSize.get("height"));	
				oX = oX + Double.valueOf(Math.round(_orignSize.get("marginX")));
				oY = oY +  Double.valueOf(Math.round(_orignSize.get("marginY")));				
			}
			imageShape.setAnchor(new Rectangle2D.Double(oX,oY,oWidth,oHeight));
			
			if( _rotate != 0 )
			{
				imageShape.setRotation(_rotate);
			}
			
			String _fileDownloadUrl = ValueConverter.getString(_item.get("fileDownloadUrl"));
			if( _fileDownloadUrl != null && !(_fileDownloadUrl.equals("")) ){
				XSLFHyperlink link = imageShape.createHyperlink();
				link.setAddress(_fileDownloadUrl);
			}
		}
	}
	
	
	
	/**
	 * 이미지 타입 아이템 생성.
	 * @param _item item정보를 담고있는 HashMap
	 * 
	 * @throws URISyntaxException 
	 * @throws UnsupportedEncodingException 
	 * */
	private void makeImageBase64( HashMap<String, Object> _item) throws URISyntaxException, UnsupportedEncodingException{
		
		String _imageUrl = ValueConverter.getString(_item.get("src"));
		
		//_imageUrl = URLDecoder.decode(_imageUrl, "UTF-8").replaceAll(" ", "+");
		
		if( _imageUrl.equals("") || _imageUrl.equals("null") ){
			log.error(getClass().getName()+"::addImage::"+">>>>>>>> Item's src property is not exist.");
			return;
		}
		if(_imageUrl.contains("http://")){
			String[] _replaceimageUrl = _imageUrl.split(",");
			_imageUrl = _replaceimageUrl[1];
		}
		
		byte[] bAr = common.getBytesLocalImageFile(_imageUrl);
		//byte[] bAr = Base64.decodeBase64(_imageUrl.getBytes("UTF-8"));
		if(bAr != null)
		{
			
			/* Create the drawing container */
			XSLFPictureData img = _mXMLSlideShow.addPicture(bAr,  org.apache.poi.sl.usermodel.PictureData.PictureType.PNG);
			XSLFPictureShape imageShape = _mXSLFSlide.createPicture(img);
			//verticalAlign
			/*if( _item.get("verticalAlign") != null ){
				String vAlign = String.valueOf(_item.get("verticalAlign"));
				if( isValid(vAlign) ){
					switch ( EVerticalAlign.valueOf(vAlign)) {
					case top: .setVerticalAlignment(VerticalAlignment.TOP); break;
					case middle: connectLine.setVerticalAlignment(VerticalAlignment.MIDDLE); break;
					case bottom: connectLine.setVerticalAlignment(VerticalAlignment.BOTTOM); break;
					}
				}
			}*/
			double oWidth = 0;
			double oHeight = 0;
			double oX = 0;
			double oY = 0;
			//width
			if( _item.get("width") != null ){
				oWidth = stringToDouble(_item.get("width").toString());
			}
			//height
			if( _item.get("height") != null ){
				oHeight = stringToDouble(_item.get("height").toString());
			}
			//x
			if( _item.get("x") != null ){
				oX = stringToDouble(_item.get("x").toString());
			}
			//y
			if( _item.get("y") != null ){
				oY = stringToDouble(_item.get("y").toString());
			}
			imageShape.setAnchor(new Rectangle2D.Double(oX,oY,oWidth,oHeight));
			
			
			String _fileDownloadUrl = ValueConverter.getString(_item.get("fileDownloadUrl"));
			if( _fileDownloadUrl != null && !(_fileDownloadUrl.equals("")) ){
				XSLFHyperlink link = imageShape.createHyperlink();
				link.setAddress(_fileDownloadUrl);
			}
			
		}
	}
	
	/**
	 * <pre>
	 * PPT 저장시 SVG 차트 이미지 저장
	 * @param _item
	 * </pre>
	 * @throws DocumentException 
	 * @throws MalformedURLException 
	 * @throws IOException 
	 * @throws TranscoderException 
	 * @return base64문자열
	 * */
	protected String makeSVGArea(HashMap<String, Object> _item) throws DocumentException, MalformedURLException, IOException, TranscoderException{

		float  oWidth = 64f;
		float  oHeight = 64f;
		
		//width
		if( _item.get("width") != null ){
			oWidth = stringToFloat( _item.get("width").toString() );
	        oWidth = _item.get("width").toString().indexOf(".") != -1 ? oWidth : oWidth + 0.6f;
		}
		//height
		if( _item.get("height") != null ){
			oHeight = stringToFloat( _item.get("height").toString() );
	        oHeight = _item.get("height").toString().indexOf(".") != -1 ? oHeight : oHeight + 0.6f;
		}
        
		log.debug(getClass().getName()+"::makeSVGArea::" + "oWidth=" + oWidth + ",oHeight=" + oHeight);
        
		//String _svgTag = URLDecoder.decode( _item.get("data").toString() );
		String _svgTag = URLDecoder.decode( _item.get("data").toString(), "UTF-8");
		_svgTag = _svgTag.replaceAll("%20", " ");
		
		String itemClassName = (String) _item.get("className");
		if( itemClassName != null && itemClassName.contains("UBQRCode") )
		{
			_svgTag = Base64Decoder.decode(_svgTag);
			log.debug(getClass().getName()+"::makeSVGArea:UBQRCode:svg=[" + _svgTag + "]");
		}
		else
		{
//			_svgTag = StringUtil.convertSvgStyle(_svgTag);
			_svgTag = StringUtil.convertSvgStyleXPath(_svgTag);
		}
		
		
		InputStream _is = new ByteArrayInputStream( _svgTag.getBytes("UTF-8") );
		
        TranscoderInput input_svg_image = new TranscoderInput( _is ); 

        OutputStream png_ostream = new ByteArrayOutputStream();
        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);        
        
        PNGTranscoder my_converter = new PNGTranscoder();

        my_converter.addTranscodingHint(PNGTranscoder.KEY_WIDTH, oWidth);
        my_converter.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, oHeight);

        my_converter.transcode(input_svg_image, output_png_image);
        
        png_ostream.flush();
        
        String base64 = new String(Base64.encodeBase64(((ByteArrayOutputStream) png_ostream).toByteArray()));
        return base64;
	        
	}
	
	private void makeWaterMark(String watermarkTxt) throws Exception {
         
		XSLFSlideMaster slideMaster = _mXMLSlideShow.getSlideMasters().get(0);
         XSLFSlideLayout slidelayout = slideMaster.getLayout(SlideLayout.BLANK);
         XSLFTextBox waterMarkTB = slidelayout.createTextBox();
         XSLFTextParagraph xSLFTextParagraph = waterMarkTB.addNewTextParagraph();
         XSLFTextRun xSlfTextRun = xSLFTextParagraph.addNewTextRun();
         
         
         waterMarkTB.setAnchor(new Rectangle2D.Double(250, 400, 700, 700));
         xSlfTextRun.setFontColor(new Color(203, 203, 203));
         xSlfTextRun.setText(watermarkTxt);
         xSlfTextRun.setFontSize(Double.valueOf(130));
         waterMarkTB.setRotation(Double.valueOf(-45));
	}
	
	/**
	 * Floor( Object to String to Double ) to Integer
	 * @param o : numberValue
	 * @return Integer number 
	 * */
	private int toNum( Object o ){
		if( o == null ){
			return -1;
		}
		else if( o instanceof Integer){
			return (Integer) o;
		}
		else{
			String oVal = String.valueOf(o).trim();
			
			if( oVal.equals("") ) return -1;
			
			if( oVal.contains(".") ){
				return (int) Math.floor(Double.parseDouble(oVal));
			}
			else{
				return Integer.valueOf(oVal);
			}
		}
	}
	
	/**
	 * Int color to RGB String
	 * @return RGB(255,0,0) 
	 * */
	public String changeColorToRGB(int _color)
	{
		java.awt.Color _c = new java.awt.Color(_color);
		return "RGB(" + _c.getRed() + "," + _c.getGreen() + "," + _c.getBlue() + ")";
	}
	
	
	private String CreateClipImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException, TranscoderException
	{
		String _clipName = ValueConverter.getString(_item.get("clipArtData"));
		
		if( !_clipName.substring(_clipName.length() - 3, _clipName.length()).toUpperCase().equals("SVG"))
		{
			_clipName = _clipName+".svg";
		}
		
		
		//Step -1: We read the input SVG document into Transcoder Input
        //We use Java NIO for this purpose
		String _urlStr = Log.basePath + "UView5/assets/images/svg/" + _clipName;
		
		java.io.File _file = new java.io.File(_urlStr);
		
        TranscoderInput input_svg_image = new TranscoderInput( _file.toURL().toString() );    
        OutputStream png_ostream = new ByteArrayOutputStream();
        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);        
        
        PNGTranscoder my_converter = new PNGTranscoder();
        my_converter.transcode(input_svg_image, output_png_image);
        		
        png_ostream.flush();
        
        String base64 = new String(Base64.encodeBase64(((ByteArrayOutputStream) png_ostream).toByteArray()));
        return base64;
        
	}

	public ImageDictionary getmImageDictionary() {
		return mImageDictionary;
	}

	public void setmImageDictionary(ImageDictionary mImageDictionary) {
		this.mImageDictionary = mImageDictionary;
	}
	
	public String[] convertRGBtoInt(Object object){
		String colorHex = changeColorToRGB(Integer.valueOf(String.valueOf(object)));
		colorHex = colorHex.replace("RGB", "");
		colorHex = colorHex.replace("(", "");
		colorHex = colorHex.replace(")", "");
		String[] colorRGB = colorHex.split(",");
		
		return colorRGB;
	}
	
	public String get_mSVGfileName() {
		return _mSVGfileName;
	}

	public void set_mSVGfileName(String _mSVGfileName) {
		this._mSVGfileName = _mSVGfileName;
	}
	
	public HashMap<String, Object> getChangeItemList() {
		return mChangeItemList;
	}

	public void setChangeItemList(HashMap<String, Object> value) {
		this.mChangeItemList = value;
	}
	
	/*
	 * ComboBox, DataField change data Mapping
	 */
	private void pptChangeDataMapping( HashMap<String, Object> _change, ArrayList<HashMap<String,Object>> _pageData, int pageNo2 )
	{
		HashMap<String, ArrayList<HashMap<String, Object>>> _changePageData;
		ArrayList<HashMap<String, Object>> _itemList;
		ArrayList<HashMap<String, Object>> _changeItemList;
		HashMap<String, Object> _oItem;
		HashMap<String, Object> _cItem;
		
		if( _change.containsKey(String.valueOf(pageNo2)) == false )
		{
			return;
		}
		
		// change Data
		_changePageData = (HashMap<String, ArrayList<HashMap<String,Object>>>) _change.get(String.valueOf(pageNo2));
		_changeItemList = _changePageData.get("Controls");

		_itemList = _pageData;
		
		String _oID = "";
		String _oClassName = "";

		String _cID = "";
		for (int j = 0; j < _changeItemList.size(); j++) 
		{
			_cItem = _changeItemList.get(j);
			_cID = (String) _cItem.get("CtrlId");

			for (int i = 0; i < _itemList.size(); i++)
			{
				_oItem = _itemList.get(i);
				_oID = (String) _oItem.get("id");
		
				_oClassName = (String) _oItem.get("className");					
				
				if(_oClassName == null)
				{
					continue;
				}
				if( _oClassName.equals("UBSignature") || _oClassName.equals("UBPicture") || _oClassName.equals("UBTextSignature") )		
				{
					if( _oID.equals(_cID))
					{
						String _cData = (String) _cItem.get("Value");

						if( _oClassName.equals("UBSignature") )
						{ 
							_oItem.put("src", _cData.substring( _cData.indexOf(",") + 1 , _cData.length()));
							break;
						}
						else if( _oClassName.equals("UBPicture") )
						{ 
							_oItem.put("src", _cData.substring( _cData.indexOf(",") + 1 , _cData.length()));
							break;
						}
						else if( _oClassName.equals("UBTextSignature") )
						{ 
							_oItem.put("src", _cData.substring( _cData.indexOf(",") + 1 , _cData.length()));
							break;
						}
					}
				}
				else if(_oItem.get("cells") != null){
					ArrayList<HashMap<String,Object>> cells = (ArrayList<HashMap<String,Object>>)_oItem.get("cells");
					for(int cIdx = 0; cIdx < cells.size(); cIdx++){
						HashMap<String, Object> cell = cells.get(cIdx);
						String cellId = (String) cell.get("id");
						
						if(cellId.equals(_cID)){
							String _cData = (String) _cItem.get("Value");
							cell.put("text", _cData);
							break;
						}
					}
				}
				else{
					if( _oID.equals(_cID)){
						String _cData = (String) _cItem.get("Value");
						_oItem.put("text", _cData);
						break;
					}
				}
				
			}// for orignal ItemList
		}
	}
	
	private double getPointToPixel( double _fontSize )
	{
		_fontSize = Math.round( _fontSize * 72 / 96 );
		
		return _fontSize;
	}
	
	/**
	 * functionName :	stringToDouble
	 * desc			:	사이즈 문자를 double로 변경 
	 * 					( dpi를 96를 72dpi로 변경하는 부분을 추가 794 -> 595 로 변경 )
	 * @param _size
	 * @return
	 */
	private double stringToDouble( String _size )
	{
		double _num = Double.valueOf(_size);
		_num = _num / 96  * 72;
		
		return _num;
	}
	
	/**
	 * functionName :	stringToFloat
	 * desc			:	사이즈 문자를 float로 변경 
	 * 					( dpi를 96를 72dpi로 변경하는 부분을 추가 794 -> 595 로 변경 )
	 * @param _size
	 * @return
	 */
	private float stringToFloat( String _size )
	{
		double _num = Double.valueOf(_size);
		_num = _num / 96  * 72;
		
		return  Double.valueOf(_num ).floatValue();
	}
	
	private void drawBackGroundImage( JSONObject _backgroundImage ) throws BadElementException, MalformedURLException, IOException, URISyntaxException
	{
		String _type = "";
		String _imageUrl = "";

		if( _backgroundImage != null && _backgroundImage.isEmpty() == false )
		{
			if( _backgroundImage.containsKey("type"))
			{
				_type = _backgroundImage.get("type").toString();
				
				if( _type.equals("base64"))
				{
					_imageUrl= _backgroundImage.get("data").toString();
					_imageUrl = URLEncoder.encode( _imageUrl, "UTF-8");
				}
				else if( _type.equals("url") )
				{
					_imageUrl= _backgroundImage.get("url").toString();
				}
			}
			
			if( _imageUrl != null && _imageUrl.equals("") == false && _imageUrl.equals("null") == false )
			{
				
				HashMap<String, Object> _imageMap = new HashMap<String, Object>();
				_imageMap.put("src", _imageUrl);
				_imageMap.put("x", "0");
				_imageMap.put("y", "0");
				_imageMap.put("width", stringToDouble(_backgroundImage.get("pageWidth").toString() 	) );
				_imageMap.put("height",stringToDouble( _backgroundImage.get("pageHeight").toString() 	) );
				_imageMap.put("isOriginalSize", "true");
				makeImage(_imageMap);
				
			}
			
		}
			
	}
	
	
}
// class End
