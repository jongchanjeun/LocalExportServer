
package org.ubstorm.service.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Node;


public class StringUtil {

	public StringUtil() {
	}

	public static final boolean checkChars(String comp,
										   String str) {
		if(str == null) {
			return true;
		}
		char[] c = str.toCharArray();
		for( int i = 0; i < str.length(); i++ ) {
			if(comp.indexOf(c[i]) < 0) {
				return false;
			}
		}
		return true;
	}

	public static final int[] dateToIntArrary(String ymd) {
		return new int[] { Integer.parseInt(ymd.substring(0, 4)),
						   Integer.parseInt(ymd.substring(4, 6)),
						   Integer.parseInt(ymd.substring(6))    };
	}

	public static final String[] dateToStringArrary(String ymd) {
		return new String[] { ymd.substring(0, 4),
							  ymd.substring(4, 6),
							  ymd.substring(6)    };
	}

	public static final boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	public static final boolean isFloat(String s) {
		try {
			Float.parseFloat(s);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	public static final boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	public static final boolean isLong(String s) {
		try {
			Long.parseLong(s);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}


	public static final String replace(String src,
									   String oldstr,
									   String newstr) {
		if (src == null) {
			return null;
		}
		StringBuffer dest = new StringBuffer("");
		int  len = oldstr.length();
		int  srclen = src.length();
		int  pos = 0;
		int  oldpos = 0;

		while ((pos = src.indexOf(oldstr, oldpos)) >= 0) {
			dest.append(src.substring(oldpos, pos));
			dest.append(newstr);
			oldpos = pos + len;
		}
		if (oldpos < srclen) {
			dest.append(src.substring(oldpos, srclen));
		}
		return dest.toString();
	}


	public static final String[] toStringArray( String str,
												int columnSize ) {
		StringBuffer sb = new StringBuffer();
		ArrayList al = new ArrayList();

		String c = null;
		int    size = 0;
		int    len = 0;

		for (int i = 0; i < str.length(); i++){
			c = String.valueOf(str.charAt(i));
			len = c.getBytes().length;

			if(len == 2) {
				size += 2;
			}else{
				size++;
			}

			if(size > columnSize){
				al.add(sb.toString());
				sb = new StringBuffer();
				size = len;
			}
			sb.append(c);
		}
		if(sb.length() > 0) {
			al.add(sb.toString());
		}
		String[] re = new String[al.size()];
		for (int i = 0; i < re.length; i++) {
			re[i] = al.get(i).toString();
		}
		return re;
	}

	public static final String[] toStringArray( String str,
												String deliminater ) {
		java.util.StringTokenizer st = new java.util.StringTokenizer(str, deliminater);
		String[] returnStr = new String[st.countTokens()];
		for (int i = 0; i < returnStr.length; i++){
			returnStr[i] = st.nextToken();
		}
		return returnStr;
	}

	public static final List toList( String str,
									 String delim ) {
		  List list = new ArrayList();
		  String sDefault = "";
		  int iLenDelim = delim.length();

		  int now=0;
		  int next=0;

		  if(str.startsWith(delim)) {
			  list.add("");
			  now=delim.length();
		  }
		  while ( (next = str.indexOf(delim,now)) > 0 ){
			  list.add(str.substring(now,next));
			  now = next + iLenDelim;
		  }
		  if(str.substring(now).length() > 0) {
			  list.add(str.substring(now));
		  }else if (str.endsWith(delim)) {
			  list.add(sDefault);
		  }
		  return list;
	}

	public static final String[] toListStringArray( String str,
													String delim) {
		  List list =  toList(str, delim);

		  String[] returnStr = new String[list.size()];
		  if(list.size() > 0) {
			  for(int i = 0; i < list.size(); i++) {
				  returnStr[i] = list.get(i).toString();
			  }
		  }
		  return returnStr;
	}

	public static final String[] unduplicate(String[] src) {
		try {
				Hashtable hash = new Hashtable(10);
				Vector vct = new Vector(10);
				for (int i = 0; i < src.length; i++){
						hash.put(src[i], i+"");
				}
				Enumeration enumer = hash.keys();
				while(enumer.hasMoreElements()){
						vct.addElement(enumer.nextElement().toString());
				}
				String[] re = new String[vct.size()];
				for (int i = 0; i < re.length; i++){
						re[i] = vct.elementAt(i).toString();
				}
				return re;
		} catch ( Exception e ) {
				e.printStackTrace();


		}
		return null;
	}

	public static String nullChk(String str) {
		if(str == null) {
			return "";
		}
		return str.trim();
	}

	public static String removeAllWhitespace(String str) {
		String sText = nullChk(str);
		if(sText.length() > 0)
		{
		     sText= sText.replaceAll("\\s+", "");		   
		}		
		return sText;
	}
	
	/**
	 * functionName	:	getSplitCharacter</br>
	 * desc			:	넘겨받은 문자를 width와 height를 이용하여 분할하여 리턴 
	 * @param _text
	 * @param _option	[ width값(Number), Height값( Array(첫페이지 Height, 이후 페이지Height ), 폰트 사이즈(Number), LineHeight(Number) ]
	 * @return 
	 */
	public static HashMap<String,Object> getSplitCharacter2( String _texts  , HashMap<String , float[]> _option )
	{
		ArrayList<String> _result = new ArrayList<String>();
		ArrayList<Float> _resultHeight = new ArrayList<Float>();
		float[] _heightList		=null;
		float[] _widthList		=null;
		float[] _fontSizeList		=null;
		float[] _lineHeightList	=null;
		
		float ITEM_WIDTH	=0;
		float ITEM_HEIGHT	=0;
		float FONT_SIZE		=0;
		float LINE_HEIGHT	=0;
		
		if(_option.containsKey("height")){
			_heightList =_option.get("height");
		}
		
		if(_option.containsKey("width")){
			_widthList =_option.get("width");
		}
		
		if(_option.containsKey("fontSize")){
			_fontSizeList =_option.get("fontSize");
		}
		
		if(_option.containsKey("lineHeight")){
			_lineHeightList =_option.get("lineHeight");
		}
		
		ITEM_WIDTH		=_widthList[0];
		ITEM_HEIGHT	=_heightList[0];
		FONT_SIZE		=_fontSizeList[0];
		LINE_HEIGHT		=_lineHeightList[0];
		LINE_HEIGHT		=FONT_SIZE * LINE_HEIGHT;

		LINE_HEIGHT=FONT_SIZE*1;
		ITEM_WIDTH-=150;
		ITEM_HEIGHT-=5;
		
		_texts = _texts.replace("\\n", "\n").replace("\\r", "\r");
		
		String[] _array=_texts.split("");
		String _str="";

		int _charCode=0;
		
		float _width=0;
		float _temp=0;
		float _height=0;
		
		String _text="";
		_height += LINE_HEIGHT;
		
		// flex와 다르게, java에서 split은 맨앞에 공백이 포함된 배열을 반환한다.
		// 때문에 배열에서 맨앞의 element를 제거하여, flex source와 동일하게 만든다. 
		int _stPosition = 0;
		if( _array[0].equals("") ) _stPosition = 1;		// 20160226 - 최명진 - 첫글자가 공백이 아닌경우가 발생하여 공백일때만 한칸 지우도록 처리
		
		_array = Arrays.copyOfRange(_array, _stPosition, _array.length);
		
		for( String _codeStr : _array ){
			_str=_codeStr;
			_charCode = _codeStr.hashCode();
			
//			if( 47 <  _charCode &&  _charCode < 58 ){
			if( 42 <  _charCode &&  _charCode < 58 ){
				_temp=(FONT_SIZE/2);
			}else if( 64 < _charCode && _charCode < 123 ){
				_temp=(FONT_SIZE/2);
			}else if( 12592 < _charCode ){
				_temp=FONT_SIZE;
			}else if( 9 == _charCode ){
				_temp=(FONT_SIZE*5);
			}else if( 32 == _charCode ){
				_temp=(FONT_SIZE/2);
			}else if( 10 == _charCode ||  13 == _charCode  ){
				
				if( ITEM_HEIGHT > -1 && (_height + LINE_HEIGHT)  > ITEM_HEIGHT ){
					_resultHeight.add(_height);  
					
					_height = LINE_HEIGHT;
					_result.add(_text);
					_text = "";
					ITEM_HEIGHT = _heightList[1];
				}else{
					_text += _str;
					_height += LINE_HEIGHT;
				}
				_width=0;
				continue;
				
			}else{
				_temp=FONT_SIZE;
			}
			
			if( (_width + _temp)  > ITEM_WIDTH ){
				_width = _temp;
				
				if( ITEM_HEIGHT > -1 &&  (_height + LINE_HEIGHT)  > ITEM_HEIGHT ){
					_resultHeight.add(_height);
					
					_height = LINE_HEIGHT;
					_result.add(_text);
					_text = _str;
					ITEM_HEIGHT = _heightList[1];
				}else{
					_text += _str;
					_height += LINE_HEIGHT;
				}
				
			}else{
				_text += _str;
				_width += _temp;
			}
			
		}
		
		if("".equals(_texts) == false && "".equals(_text))
		{
			_text = _texts;
		}
		// 반복문 종료 후, 남은 텍스트를 배열에 담는다.
		_result.add(_text);
		
		// return values =  _result , height
		HashMap<String,Object> _resultMap = new HashMap<String, Object>();

		// height set
		_resultHeight.add(_height);
		
		// return text
		_resultMap.put( "Text",  _result );
		// return height
		_resultMap.put( "Height",  _resultHeight );
		
		return _resultMap;
	}
	
	
	
	public static HashMap<String,Object> getSplitCharacter( String _texts  , HashMap<String , float[]> _option , String fontWeight , String _fontFamily , float _fontSize , float _margin )
	{
		ArrayList<String> _result 		= new ArrayList<String>();
		ArrayList<Float> _resultHeight	= new ArrayList<Float>();
		float[] _heightList		=null;
		float[] _widthList		=null;
		float[] _fontSizeList	=null;
		float[] _lineHeightList	=null;
		float[] _paddingList	=null;
		
		float ITEM_WIDTH	=0;
		float ITEM_HEIGHT	=0;
		float FONT_SIZE		=0;
		float LINE_HEIGHT	=0;
		float PADDING		=3;
		
		if(_option.containsKey("height")){
			_heightList =_option.get("height");
		}
		
		if(_option.containsKey("width")){
			_widthList =_option.get("width");
		}
		
		if(_option.containsKey("fontSize")){
			_fontSizeList =_option.get("fontSize");
		}
		
		if(_option.containsKey("lineHeight")){
			_lineHeightList =_option.get("lineHeight");
		}

		if(_option.containsKey("padding")){
			_paddingList =_option.get("padding");
		}
		
		ITEM_WIDTH		=_widthList[0];
		ITEM_HEIGHT		=_heightList[0];
		FONT_SIZE		=_fontSizeList[0];
		LINE_HEIGHT		=_lineHeightList[0];
		LINE_HEIGHT		=FONT_SIZE * LINE_HEIGHT;
		PADDING			=_paddingList[0];

		int _fontStyle = fontWeight.equalsIgnoreCase("bold") ? Font.BOLD : Font.PLAIN;
		
//		Font font = new Font(_fontFamily, _fontStyle , Math.round(FONT_SIZE)  );

//		Font font = new Font(_fontFamily, _fontStyle , Math.round(FONT_SIZE*720/96)  );
		
		if( GlobalVariableData.M_REGISTER_FONT_LISE.indexOf(_fontFamily) == -1 && GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.containsKey(_fontFamily) )
		{
			_fontFamily = GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.get(_fontFamily);
		}
		
		Font font = new Font(_fontFamily, _fontStyle , Math.round(FONT_SIZE*10)  );
		
		FontMetrics fm = getFontMatrix(font);

		float _defaultHeight = (LINE_HEIGHT*2);
		if( _margin > -1) _defaultHeight = LINE_HEIGHT + _margin + 1;		// 0으로 지정시 pdf내보내기시에 라인이 표시 되지 않을수 있어 기본적으로 1을 더하여 지정
		
		final float DEFAULT_HEIGHT=_defaultHeight;
//		final float DEFAULT_HEIGHT=LINE_HEIGHT+1;		
//		final float DEFAULT_PADDING=(20 + PADDING*2);
//		final float DEFAULT_PADDING=(PADDING*2);
		final float DEFAULT_PADDING=0.0f;
		
		// javascirpt padding 
		ITEM_WIDTH	-=DEFAULT_PADDING;			//TEST
		ITEM_HEIGHT	-=DEFAULT_PADDING;
		
		_texts = _texts.replace("\\n", "\n").replace("\\r", "\r");
		
		String[] _array=_texts.split("");
		
		
		float _width	=0;
		float _temp		=0;
		float _height	=DEFAULT_HEIGHT;
		
		String _text="";
		//_height += (LINE_HEIGHT*2);
		
		// flex와 다르게, java에서 split은 맨앞에 공백이 포함된 배열을 반환한다.
		// 때문에 배열에서 맨앞의 element를 제거하여, flex source와 동일하게 만든다. 
		int _stPosition = 0;
		if( _array[0].equals("") ) _stPosition = 1;		// 20160226 - 최명진 - 첫글자가 공백이 아닌경우가 발생하여 공백일때만 한칸 지우도록 처리
		
		_array = Arrays.copyOfRange(_array, _stPosition, _array.length);
			
		for( String _str : _array ){
			
			_temp = fm.stringWidth(_str);
			
//			_temp = (float) Math.round( _temp * 96/ 720 );
//			_temp = (float) Math.round( _temp / 10 );
			_temp = (float) Math.round( _temp / 10 * 10 ) / 10;
			
			if( _str.equalsIgnoreCase("\n") ){
				
				if( ITEM_HEIGHT > -1 &&  (_height + LINE_HEIGHT)  >= ITEM_HEIGHT ){
					
					_height = (float) Math.floor(_height);
					_resultHeight.add(_height);
					
					//_height = LINE_HEIGHT;
					_height = DEFAULT_HEIGHT;
					
					_result.add(_text);
					_text = "";
					ITEM_HEIGHT = _heightList[1];
					
					ITEM_HEIGHT	-=DEFAULT_PADDING;
					
				}else{
					_text += _str;
					_height += LINE_HEIGHT;
				}
				_width=0;
				continue;
				
				
			}else{
				if( (_width + _temp)  > ITEM_WIDTH ){
					
					//_width = 0;
					_width = _temp;
					
					if( ITEM_HEIGHT > -1 &&  (_height + LINE_HEIGHT)  >= ITEM_HEIGHT ){
						
						_height = (float) Math.floor(_height);
						_resultHeight.add(_height);
						
						//_height = LINE_HEIGHT;
						
						_height = DEFAULT_HEIGHT;
						
						_result.add(_text);
						_text = _str;
						ITEM_HEIGHT = _heightList[1];
						
						ITEM_HEIGHT	-=DEFAULT_PADDING;
						
					}else{

						_text += _str;
						_height += LINE_HEIGHT;
					}
					
				}else{
					_text += _str;
					_width += _temp;
				}
			}
			
		}
		
		if( _result.size() == 0 && "".equals(_texts) == false && "".equals(_text))
		{
			_text = _texts;
		}
		// 반복문 종료 후, 남은 텍스트를 배열에 담는다.
		_result.add(_text);
		
		// return values =  _result , height
		HashMap<String,Object> _resultMap = new HashMap<String, Object>();

		// height set
		_height = (float) Math.floor(_height);
		_resultHeight.add(_height);
		// return text
		_resultMap.put( "Text",  _result );
		// return height
		_resultMap.put( "Height",  _resultHeight );
		
		return _resultMap;
	}
	
	public static HashMap<String,Object> getSplitCharacterLine( String _texts  , HashMap<String , float[]> _option , String fontWeight , String _fontFamily , float _fontSize , float _margin )
	{
		ArrayList<String> _result 		= new ArrayList<String>();
		ArrayList<Float> _resultHeight	= new ArrayList<Float>();
		float[] _heightList		=null;
		float[] _widthList		=null;
		float[] _fontSizeList	=null;
		float[] _lineHeightList	=null;
		float[] _paddingList	=null;
		
		float ITEM_WIDTH	=0;
		float ITEM_HEIGHT	=0;
		float FONT_SIZE		=0;
		float LINE_HEIGHT	=0;
		float PADDING		=3;
		
		if(_option.containsKey("height")){
			_heightList =_option.get("height");
		}
		
		if(_option.containsKey("width")){
			_widthList =_option.get("width");
		}
		
		if(_option.containsKey("fontSize")){
			_fontSizeList =_option.get("fontSize");
		}
		
		if(_option.containsKey("lineHeight")){
			_lineHeightList =_option.get("lineHeight");
		}

		if(_option.containsKey("padding")){
			_paddingList =_option.get("padding");
		}
		
		ITEM_WIDTH		=_widthList[0];
		ITEM_HEIGHT		=_heightList[0];
		FONT_SIZE		=_fontSizeList[0];
		LINE_HEIGHT		=_lineHeightList[0];
		LINE_HEIGHT		=FONT_SIZE * LINE_HEIGHT;
		PADDING			=_paddingList[0];

		int _fontStyle = fontWeight.equalsIgnoreCase("bold") ? Font.BOLD : Font.PLAIN;
		
		if( GlobalVariableData.M_REGISTER_FONT_LISE.indexOf(_fontFamily) == -1 && GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.containsKey(_fontFamily) )
		{
			_fontFamily = GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.get(_fontFamily);
		}
		
		Font font = new Font(_fontFamily, _fontStyle , Math.round(FONT_SIZE*10)  );
		FontMetrics fm = getFontMatrix(font);

		float _defaultHeight = (LINE_HEIGHT*2);
		if( _margin > -1) _defaultHeight = LINE_HEIGHT + _margin + 1;		// 0으로 지정시 pdf내보내기시에 라인이 표시 되지 않을수 있어 기본적으로 1을 더하여 지정
		
		final float DEFAULT_HEIGHT=_defaultHeight;
		final float DEFAULT_PADDING=0.0f;
		
		// javascirpt padding 
		ITEM_WIDTH	-=DEFAULT_PADDING;			//TEST
		ITEM_HEIGHT	-=DEFAULT_PADDING;
		
		_texts = _texts.replace("\\n", "\n").replace("\\r", "\r");
		
		String[] _array=_texts.split("");
		
		
		float _width	=0;
		float _temp		=0;
		float _height	=DEFAULT_HEIGHT;
		
		String _text="";
		//_height += (LINE_HEIGHT*2);
		
		// flex와 다르게, java에서 split은 맨앞에 공백이 포함된 배열을 반환한다.
		// 때문에 배열에서 맨앞의 element를 제거하여, flex source와 동일하게 만든다. 
		int _stPosition = 0;
		if( _array[0].equals("") ) _stPosition = 1;		// 20160226 - 최명진 - 첫글자가 공백이 아닌경우가 발생하여 공백일때만 한칸 지우도록 처리
		
		_array = Arrays.copyOfRange(_array, _stPosition, _array.length);
			
		for( String _str : _array ){
			
			_temp = fm.stringWidth(_str);
			
			_temp = (float) Math.round( _temp / 10 * 10 ) / 10;
			
			if( _str.equalsIgnoreCase("\n") ){
				
				_result.add(_text);
				_text = "";
				_height += LINE_HEIGHT;
				_width=0;
				continue;
			}else{
				if( (_width + _temp)  > ITEM_WIDTH ){
					
					_width = _temp;
					
					_result.add(_text);
					_text = _str;
					_height += LINE_HEIGHT;
					
				}else{
					_text += _str;
					_width += _temp;
				}
			}
		}
		
		if( _result.size() == 0  && "".equals(_texts) == false )
		{
			_text = _texts;
		}
		// 반복문 종료 후, 남은 텍스트를 배열에 담는다.
		_result.add(_text);
		
		// return values =  _result , height
		HashMap<String,Object> _resultMap = new HashMap<String, Object>();

		// height set
		_height = (float) Math.floor(_height);
		_resultHeight.add(_height);
		// return text
		_resultMap.put( "Text",  _result );
		// return height
		_resultMap.put( "Height",  _resultHeight );
		
		return _resultMap;
	}
	
	
	
	// 문자열 내에 한글이 포함되었는지 여부를 체크 한다.
	public static boolean containsKorean(String _text)
	{
		boolean contains = false;
		
		if(_text == null) return contains;
		
		for(int i=0;i <_text.length(); i++){
            char c = _text.charAt(i);
            //한글 ( 한글자 || 자음 , 모음 )
            if( ( 0xAC00 <= c && c <= 0xD7A3 ) || ( 0x3131 <= c && c <= 0x318E ) ){
            	contains = true;
            	break;
            }
		}
		
		return contains;
	}
	
	
	public static int measureTextWidth(String text, Font font)
	{
		AffineTransform affinetransform = new AffineTransform();     
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
		
		int textwidth = (int)(font.getStringBounds(text, frc).getWidth());
		//int textheight = (int)(font.getStringBounds(text, frc).getHeight());

		return textwidth;
	}
	
	public static int measureTextHeight(String text, Font font)
	{
		AffineTransform affinetransform = new AffineTransform();     
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
		
		//int textwidth = (int)(font.getStringBounds(text, frc).getWidth());
		int textheight = (int)(font.getStringBounds(text, frc).getHeight());

		return textheight;
	}
	
	public static Graphics2D CAL_CHAR_GRAPHIC;
	
	public static int measureCharWidth( FontMetrics fm , String str, Font font)
	{
	     int width = fm.stringWidth(str);
	     //int height = fm.getHeight();
	     CAL_CHAR_GRAPHIC.dispose();
	     
		/*
         Because font metrics is based on a graphics context, we need to create
         a small, temporary image so we can ascertain the width and height
         of the final image
		*/
	     //BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	     //Graphics2D g2d = img.createGraphics();
	     //Font font = new Font("Arial", Font.PLAIN, 48);
	     //g2d.setFont(font);
	     //FontMetrics fm = g2d.getFontMetrics();
	     //int width = fm.stringWidth(str);
	     //int height = fm.getHeight();
	     //g2d.dispose();
	     
	     return width;

	}
	
	
	
	/**
	 * Font가 적용된 문자열의 넓이와 높이를 구하기 위한 FontMetrics를 얻어온다.
	 * 
	 * @param font
	 * @return
	 */
	public static FontMetrics getFontMatrix(Font font)
	{
		 Graphics2D g2d = Log.g2d;
	     g2d.setFont(font);
	     FontMetrics fm = g2d.getFontMetrics();
	     
	     //int width = fm.stringWidth(text);
	     //int height = fm.getHeight();
	     
	     return fm;
	}
	
	
	
	/**
	 * SVG TAG - Fablic 간에 호환되지 않는 태그를 교체한다.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * */
	public static String replaceSVGTag( String _str )
	{
		return replaceSVGTag( _str, false, false, null );
		
/*		
		String _result=_str;
		
		int _svgStartPos = _str.indexOf("<svg");
		if(_svgStartPos != -1)
		{
			_str = _str.substring(_svgStartPos);
			InputSource _is = new InputSource(new StringReader(_str));
			DocumentBuilderFactory _dbf;
			Document document;
			try {
				_dbf = DocumentBuilderFactory.newInstance();
				document = _dbf.newDocumentBuilder().parse(_is);
				
				NodeList textLastList = document.getElementsByTagName("text");
				for (int i = 0; i < textLastList.getLength(); i++) 
				{
					Element itemEl = (Element)textLastList.item(i);
					String itemTransform = itemEl.getAttribute("transform");
					if( itemTransform.equals("") || itemTransform.equals("null") ) continue;
					
					int _rotatePosition	= itemTransform.indexOf("rotate");
					if(_rotatePosition != -1)
					{
						// x갑 가져오기
						String _xValue	= itemEl.getAttribute("x");
						// y값 가져오기
						String _yValue	= itemEl.getAttribute("y");
						
						// rotate값 가져오기	rotate(270 28.03 197)"
						int _rotateEndTagPosition	= itemTransform.indexOf(")", _rotatePosition+1);
						String _rotateString =	itemTransform.substring(_rotatePosition+7 , _rotateEndTagPosition);
						String[] _ratateValues = _rotateString.split(" ");
						
						double _rotateDegree =  Math.toRadians(360 - Double.parseDouble(_ratateValues[0]));
						//System.out.println("_rotateDegree=[" + _rotateDegree + "]");
						double _dx = Double.parseDouble(_xValue);
						double _dy = Double.parseDouble(_yValue);
						if(_rotateDegree != 0 || _rotateDegree != 2*Math.PI)
						{
							_dx = Double.parseDouble(_xValue) * Math.cos(_rotateDegree) - Double.parseDouble(_yValue) * Math.sin(_rotateDegree);
							_dy = Double.parseDouble(_xValue) * Math.sin(_rotateDegree) + Double.parseDouble(_yValue) * Math.cos(_rotateDegree);
						}
					
						itemEl.setAttribute("x", String.valueOf(_dx));
						itemEl.setAttribute("y", String.valueOf(_dy));
					}
					
					// 불필요한 title 태그 삭제.
					NodeList titleList = itemEl.getElementsByTagName("title");
					int ttlLen = titleList.getLength();
					for (int tIdx = 0; tIdx < ttlLen; tIdx++) {
						Element el = (Element)titleList.item(0);
						el.getParentNode().removeChild(el);
					}
				}
								
				// 불필요한 rect 나오는 문제 처리.  이미지 태그의 stroke-width 를 0 처리
				//<image fill="#434348" stroke="#666666" stroke-width="1" 
				NodeList imageList = document.getElementsByTagName("image");
				for (int i = 0; i < imageList.getLength(); i++) 
				{
					Element itemEl = (Element)imageList.item(i);
					String strokeWidth = itemEl.getAttribute("stroke-width");
					if( strokeWidth.equals("") || strokeWidth.equals("null") ) continue;
					
					itemEl.setAttribute("stroke-width", "0");
				}
				
				// path에 d 속성값이 없는 경우  Radar Chart가 그려지지 않는 문제 처리. (d가 없으면 무시하도록 한다.)
				// <path fill="none" stroke="#D8D8D8" stroke-width="1" ></path>
				NodeList pathList = document.getElementsByTagName("path");
				for (int i = 0; i < pathList.getLength(); i++) 
				{
					Element itemEl = (Element)pathList.item(i);
					if(itemEl != null)
					{
						String strokeWidth = itemEl.getAttribute("d");
						if( strokeWidth == null || strokeWidth.equals("") || strokeWidth.equals("null"))
						{
							//System.err.println("org.ubstorm.service.util.StringUtil - path element d arrtibute is NULL.");
							itemEl.getParentNode().removeChild(itemEl);
						}
					}
				}
				
				_result = docToString(document);
				//System.out.println("SVG=[" + _result + "]");
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return _result;
*/		
	}
	
	
	public static String replaceSVGTag( String _str, boolean _preserveAspectRatio, boolean _fixedToSize, HashMap<String, Object> _item )
	{
		String _result=_str;
		String _preserveAspectRatioString = _preserveAspectRatio?"xMidYMid meet":"none";
		
		
		int _svgStartPos = _str.indexOf("<svg");
		if(_svgStartPos != -1)
		{
			_str = _str.substring(_svgStartPos);
			
			_str = _str.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFF]+", "");	
			
			InputSource _is = new InputSource(new StringReader(_str));
			DocumentBuilderFactory _dbf;
			Document document;
			try {
				_dbf = DocumentBuilderFactory.newInstance();
				document = _dbf.newDocumentBuilder().parse(_is);
				
				if(_fixedToSize) document.getDocumentElement().setAttribute("preserveAspectRatio", _preserveAspectRatioString);
				
				NodeList textLastList = document.getElementsByTagName("text");
				for (int i = 0; i < textLastList.getLength(); i++) 
				{
					Element itemEl = (Element)textLastList.item(i);
					String itemTransform = itemEl.getAttribute("transform");
					if( itemTransform.equals("") || itemTransform.equals("null") ) continue;
					
					int _rotatePosition	= itemTransform.indexOf("rotate");
					if(_rotatePosition != -1)
					{
						// x갑 가져오기
						String _xValue	= itemEl.getAttribute("x");
						// y값 가져오기
						String _yValue	= itemEl.getAttribute("y");
						
						// rotate값 가져오기	rotate(270 28.03 197)"
						int _rotateEndTagPosition	= itemTransform.indexOf(")", _rotatePosition+1);
						String _rotateString =	itemTransform.substring(_rotatePosition+7 , _rotateEndTagPosition);
						String[] _ratateValues = _rotateString.split(" ");
						
						if( _ratateValues.length == 1 )
						{
							
							double _rotateDegree =  Math.toRadians(360 - Double.parseDouble(_ratateValues[0]));
							double _dx = Double.parseDouble(_xValue);
							double _dy = Double.parseDouble(_yValue);
							if(_rotateDegree != 0 || _rotateDegree != 2*Math.PI)
							{
								_dx = Double.parseDouble(_xValue) * Math.cos(_rotateDegree) - Double.parseDouble(_yValue) * Math.sin(_rotateDegree);
								_dy = Double.parseDouble(_xValue) * Math.sin(_rotateDegree) + Double.parseDouble(_yValue) * Math.cos(_rotateDegree);
							}
						
							itemEl.setAttribute("x", String.valueOf(_dx));
							itemEl.setAttribute("y", String.valueOf(_dy));
						}
					}
					
					// 불필요한 title 태그 삭제.
					NodeList titleList = itemEl.getElementsByTagName("title");
					int ttlLen = titleList.getLength();
					for (int tIdx = 0; tIdx < ttlLen; tIdx++) {
						Element el = (Element)titleList.item(0);
						el.getParentNode().removeChild(el);
					}
				}
								
				// 불필요한 rect 나오는 문제 처리.  이미지 태그의 stroke-width 를 0 처리
				//<image fill="#434348" stroke="#666666" stroke-width="1" 
				NodeList imageList = document.getElementsByTagName("image");
				for (int i = 0; i < imageList.getLength(); i++) 
				{
					Element itemEl = (Element)imageList.item(i);
					String strokeWidth = itemEl.getAttribute("stroke-width");
					if( strokeWidth.equals("") || strokeWidth.equals("null") ) continue;
					
					itemEl.setAttribute("stroke-width", "0");
				}
				
				// g속성에 visiblity값이 hidden일때 표시되는 현상으로 제거처리. (d가 없으면 무시하도록 한다.)
				NodeList gList = document.getElementsByTagName("g");
				for (int i = 0; i < gList.getLength(); i++) 
				{
					Element itemEl = (Element)gList.item(i);
					if(itemEl != null)
					{
						String visibility = itemEl.getAttribute("visibility");
						if( visibility != null && visibility.equals("hidden") )
						{
							itemEl.getParentNode().removeChild(itemEl);
						}
					}
				}
				
				// path에 d 속성값이 없는 경우  Radar Chart가 그려지지 않는 문제 처리. (d가 없으면 무시하도록 한다.)
				// <path fill="none" stroke="#D8D8D8" stroke-width="1" ></path>
				NodeList pathList = document.getElementsByTagName("path");
				for (int i = 0; i < pathList.getLength(); i++) 
				{
					Element itemEl = (Element)pathList.item(i);
					if(itemEl != null)
					{
						String strokeWidth = itemEl.getAttribute("d");
						if( strokeWidth == null || strokeWidth.equals("") || strokeWidth.equals("null"))
						{
							//System.err.println("org.ubstorm.service.util.StringUtil - path element d arrtibute is NULL.");
							itemEl.getParentNode().removeChild(itemEl);
						}
						/** Test
						strokeWidth = itemEl.getAttribute("stroke-width");
						
						if(  strokeWidth != null && !strokeWidth.equals("") )
						{
							if(!ValueConverter.isInt(strokeWidth))
							{
								itemEl.setAttribute("stroke-width", "0");
							}
						} */
						
					}
				}
				
				// fixedToSize가 false일때 item의 사이즈를 svg의 사이즈로 변경한다
				if(_item!=null && !_fixedToSize )
				{
					String _widthStr = document.getDocumentElement().getAttribute("width");
					String _heightStr = document.getDocumentElement().getAttribute("height");
					
					if( _widthStr != null && _heightStr!= null && !_widthStr.equals("") && !_heightStr.equals("")  )
					{
						_widthStr = _widthStr.replaceAll("[^\\d]", "");
						_heightStr = _heightStr.replaceAll("[^\\d]", "");

						if( isFloat(_widthStr) && isFloat(_heightStr) )
						{
							_item.put("ORI_WIDTH", _item.get("width"));
							_item.put("ORI_HEIGHT", _item.get("height"));
							
							_item.put("width", Float.valueOf(_widthStr));
							_item.put("height", Float.valueOf(_heightStr));
						}
						
					}
				}
				
				_result = docToString(document);
				//System.out.println("SVG=[" + _result + "]");
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return _result;
	}
	
	
	/**
	 * SVG 태그의 RGBA를 Export시 처리가 안되기때문에 태그를 수정 ( fill / stroke ) 
	 * @param _dataStr
	 * @return
	 */
	public static String convertSvgStyle(String _dataStr )
	{
		
		if( _dataStr.indexOf("rgba") == -1) return _dataStr;
		
		try {
			InputStream stream = new ByteArrayInputStream(_dataStr.getBytes("UTF-8"));
			org.jdom2.Document doc;
			doc = (org.jdom2.Document) new SAXBuilder().build(stream);
			
			org.jdom2.Element xmlRoot = (org.jdom2.Element) doc.getRootElement(); //<svg>
	   	 	List<org.jdom2.Element> firstItemList = xmlRoot.getChildren();
	   	 	
	   	 	for (org.jdom2.Element firstItem : firstItemList) 
		   	{
		   		List<org.jdom2.Element> secondItemList = firstItem.getChildren(); // Item
		   		for (org.jdom2.Element secondItem : secondItemList) {
					 String secondItemFillAttr = secondItem.getAttributeValue("fill");
					 
					 if( secondItemFillAttr != null && secondItemFillAttr.contains("rgba") ){
						 int beginIndex = secondItemFillAttr.indexOf("(");
						 int endIndex = secondItemFillAttr.indexOf(")");
						 String fillAttRgbaValue = secondItemFillAttr.substring(beginIndex+1, endIndex);
						 String[] arrfillAttRgbaValues = fillAttRgbaValue.split(",");
						 
						 secondItem.setAttribute("fill", "rgb(" + arrfillAttRgbaValues[0] + "," + arrfillAttRgbaValues[1] + "," + arrfillAttRgbaValues[2] + ")");
						 secondItem.setAttribute("fill-opacity", arrfillAttRgbaValues[3]);
					 }

					 String secondItemStrokeAttr = secondItem.getAttributeValue("stroke");
					 
					 if( secondItemStrokeAttr != null && secondItemStrokeAttr.contains("rgba") ){
						 int beginIndex = secondItemStrokeAttr.indexOf("(");
						 int endIndex = secondItemStrokeAttr.indexOf(")");
						 String fillAttRgbaValue = secondItemStrokeAttr.substring(beginIndex+1, endIndex);
						 String[] arrfillAttRgbaValues = fillAttRgbaValue.split(",");
						 
						 secondItem.setAttribute("stroke", "rgb(" + arrfillAttRgbaValues[0] + "," + arrfillAttRgbaValues[1] + "," + arrfillAttRgbaValues[2] + ")");
						 secondItem.setAttribute("stroke-opacity", arrfillAttRgbaValues[3]);
					 }
					 
				}
			}
	   	 	_dataStr = new XMLOutputter().outputString(doc);   	 	
	   
			//String _svgTag = _dataStr.substring(_startIdx, _dataStr.indexOf(">" , _startIdx) + 1);
			
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return _dataStr;
	}
	
	
	public static String convertSvgStyleXPath(String _dataStr )
	{
		
		if( _dataStr.indexOf("rgba") == -1) return _dataStr;
		
		try {
			InputSource is = new InputSource(new StringReader(_dataStr)); 
			   
			DocumentBuilderFactory doc = DocumentBuilderFactory.newInstance();
			doc.setNamespaceAware(false);
			doc.setValidating(false);
			doc.setFeature("http://xml.org/sax/features/namespaces", false);
			doc.setFeature("http://xml.org/sax/features/validation", false);
			doc.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			doc.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			
			Document document = null;
			NodeList list=null;
			Element _currentElement;
			XPath _xpath = XPathFactory.newInstance().newXPath();

			try {
				document = doc.newDocumentBuilder().parse(is);
			} catch (SAXException e1) {
				// TODO Auto-generated catch blocks
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	   	 	
			
			list = (NodeList) _xpath.evaluate("//*[contains(.,'rgba')]", document.getDocumentElement(), XPathConstants.NODESET);
			list = (NodeList) _xpath.evaluate("//*[contains(@fill,'rgba') or contains(@stroke,'rgba') ]", document.getDocumentElement(), XPathConstants.NODESET);
			System.out.println( list.getLength() );
			Node _node;
	   	 	
			String[] _styleNames = {"fill","stroke"};
			String _currnetStyleAttr = "";
			
			for( int i =0; i < list.getLength(); i++ )
			{
				_node =  list.item(i);
				_currentElement = (Element) _node;
				
				for( int j =0; j < _styleNames.length; j ++ )
				{
					
					_currnetStyleAttr = _currentElement.getAttribute(_styleNames[j]);
					if( _currnetStyleAttr != null && _currnetStyleAttr.contains("rgba") ){
						 int beginIndex = _currnetStyleAttr.indexOf("(");
						 int endIndex = _currnetStyleAttr.indexOf(")");
						 String fillAttRgbaValue = _currnetStyleAttr.substring(beginIndex+1, endIndex);
						 String[] arrfillAttRgbaValues = fillAttRgbaValue.split(",");
						 
						 _currentElement.setAttribute(_styleNames[j], "rgb(" + arrfillAttRgbaValues[0] + "," + arrfillAttRgbaValues[1] + "," + arrfillAttRgbaValues[2] + ")");
						 _currentElement.setAttribute(_styleNames[j] + "-opacity", arrfillAttRgbaValues[3]);
					 }
				}
				 
			}
			
	   	 	_dataStr = docToString(document);
			
		}catch( Exception e )
		{
			System.out.println(e.getMessage());
		}
		
		return _dataStr;
	}
	
	
	/**
	 * XML DOM Dccument를 String으로 변환하여 반환한다.
	 * */
	public static String docToString(Document doc) {
	    try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "no");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        
	        // SVG Text에서 &nbsp;를 여러개 주어도 공백 하나만 표시되는 문제 수정
	        String _rtnStr = sw.toString();
	        _rtnStr = _rtnStr.replace("\u00A0","&#160;");
	        
	        return _rtnStr;
	    } catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
	}
	
	/**
	 * area에 맞는 font size를 구한다. 
	 * */
	public static int getTextMatchWidthFontSize( String _text , float _width , String fontFamily , String fontWeight , float fontSize, int minResizeFontSize )
	{
		float MAX_WIDTH=_width;
		
		String _fontFamily=fontFamily;
		
		if( GlobalVariableData.M_REGISTER_FONT_LISE.indexOf(_fontFamily) == -1 && GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.containsKey(_fontFamily) )
		{
			_fontFamily = GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.get(_fontFamily);
		}
	
		String _fontWeight=fontWeight;
		float _fontSize=fontSize;
		
		int _fontSizeRound = Math.round(_fontSize);
		int _fontStyle = _fontWeight.equalsIgnoreCase("bold") ? Font.BOLD : Font.PLAIN;
		
		int _minFontSize = 6;		// mininum Font Size를 임시로 6으로 지정 (2016-06-28 최명진)
		
		if( minResizeFontSize > 0 )_minFontSize = minResizeFontSize;
		
		Boolean _isNext=true;
		
		Font font;
		FontMetrics fm;
		float _textWidth=0;
		while( _isNext )
		{
			font = new Font(_fontFamily, _fontStyle , _fontSizeRound  );	
			fm = getFontMatrix(font);
			_textWidth = fm.stringWidth( _text );
			
			if( _fontSizeRound <= _minFontSize )
			{
				_fontSizeRound = _minFontSize;
				_isNext = false;
				break;
			}else if( _textWidth >= MAX_WIDTH ){
				_fontSizeRound--;
			}else{
				_isNext = false;
				break;
			}
		}
		
		return _fontSizeRound;
	}
	
	/**
	 * dbconnection.SourceEncCharSet, dbconnection.TargetEncCharSet에 설정된 값을 기준으로 
	 * us7-ascii의 db데이터를 utf-8의 was에 맞게 변환
	 * 또는  utf-8의 was데이터를db의 us7-ascii에 맞게 변환
	 * 
	 * @param srcString
	 * @return
	 */
	public static String convertToEncCharset(String srcString) {
		
		String sValue = null;
		String srcEncCharSet = "";
		String tarEncCharSet = "";
		
		String _srcEncCharSet = common.getPropertyValue("dbconnection.SourceEncCharSet");
		if(_srcEncCharSet != null && "".equals(_srcEncCharSet) == false )
		{
			srcEncCharSet = _srcEncCharSet;
		}
		String _tarEncCharSet = common.getPropertyValue("dbconnection.TargetEncCharSet");
		if(_tarEncCharSet != null && "".equals(_tarEncCharSet) == false )
		{
			tarEncCharSet = _tarEncCharSet;
		}
		
		if(!"".equals(srcEncCharSet) && !"".equals(tarEncCharSet))
			sValue = convertToKorCharset(srcString, srcEncCharSet, tarEncCharSet);
		else
			sValue = srcString;
		
		return sValue;
	}
	
	/**
	 * srcString문자열에 대하여 srcEncCharSet으로 인코딩되어 있는 문자열을 targetCharSet으로 인코딩 처리한다.
	 * 오라클 DB에서 한글 인코딩 문제를 처리하기 위해 주로 사용하는 메소드이다.
	 * 
	 * @param srcString
	 * @param srcEncCharSet
	 * @param tarEncCharSet
	 * @return
	 */
	public static String convertToKorCharset(String srcString, String srcEncCharSet, String tarEncCharSet) {
		
		String sValue = null;
		try {
			//if (srcString != null) sValue = new String(srcString.getBytes("8859_1"), "MS949");
			if (srcString != null) sValue = new String(srcString.getBytes(srcEncCharSet), tarEncCharSet);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sValue;
	}
	
	public static String wrapLineInto(String line, FontMetrics fm, int maxWidth) {
		int _strWidth = 0;		
		String [] arrStr = line.split("");
		ArrayList<String> alLine =  new ArrayList<String>();
	    int width;
	    String _tempStr = "";
	    String _str ="";
	    String _line = "";
	    for(int i = 0; i<arrStr.length;i++){
	    	_str = arrStr[i];
	    	if(_str.equals("\n")){
	    		alLine.add(_line);
	    		_tempStr = "";
	    		_line = "";
	    		continue;
	    	}
	    	_tempStr+=_str;
	    	_strWidth = fm.stringWidth(_tempStr);	    	
	    	
	    	if(_strWidth>maxWidth ){
	    		alLine.add(_line);
	    		_tempStr = _str;
	    		_line = "";
	    	} 	
	    	_line += _str;
	    	
	    	if(i == arrStr.length-1){
	    		alLine.add(_line);
	    	}
	    }	
	    
	    String retStr = "";
	    
	    for(int i = 0; i<alLine.size();i++){
	    	if(i == alLine.size()-1){
	    		retStr += alLine.get(i);
	    	}else{
	    		retStr += alLine.get(i) + "\n";
	    	}
	    }
	    return retStr;
	   
	  }
	
	
	public static String convertObjectToString( Object _value )
	{
		String _retValue = "";
		
		if( _value instanceof String ) _retValue = _value.toString();
		else _retValue = String.valueOf(_value);
		
		return _retValue;
	}
}

