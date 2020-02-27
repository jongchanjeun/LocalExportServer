package org.ubstorm.service.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;
import org.json.simple.JSONObject;
import org.ubstorm.service.barcode.BaseBarcode;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;

import com.oreilly.servlet.Base64Decoder;

public class ubFormToHTMLByDIV {

	private Logger log = Logger.getLogger(getClass());
	private ArrayList<ArrayList<HashMap<String, Object>>> mTotalItems;
	private HashMap<String, String> mProjectHm;
	private String resultHtml = "";
	private htmlComponent mComponent;
	private StringBuilder resultBuilder = new StringBuilder();

	
	// 기본 값 설정. //
	private ArrayList<String> mFontList;
	private ArrayList<HashMap<String, Object>> mStyleList;
	private ArrayList<HashMap<String, Object>> mParashapeList;
	private ArrayList<HashMap<String, HashMap<String, String>>> mBorderList;
	private ArrayList<String> mMatchingBorder;
	private ArrayList<String> mMatchingStyle;
	private ArrayList<String> mMatchingAlign;
//	private ArrayList mIdList;
	private HashMap<String, String>	mCompID;
	private int mTotalPage = 0;
	private int mCurrntPage = 0;	
	private int mPageWidth = 0;	
	private int mPageHeight = 0;
	private String mProjectName = "";
	private int mImgIdx = 0;
	private HashMap<String, String> mImageListHm = new HashMap<String, String>();
	// 기본 값 설정. //
	
	// HTML 초기값 설정	
	private String mHeaderTxt = "<!DOCTYPE HTML>\n<HTML>\n";
	private String mFooterTxt = "</BODY></HTML>";
	// HTML 초기 값 설정. //
	
	/**
	 *  최초 HTML 로드시 설정 값 지정.
	 * 
	 * @param _hm 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String xmlPasingtoHtml(JSONObject _hm, String _path) 
	{
		log.info(getClass().getName() + "::" + "Call xmlParsing HTML()...");
		
		mProjectHm = (HashMap<String, String>) _hm.get("project"); //project 정보	
		mTotalItems = new ArrayList<ArrayList<HashMap<String, Object>>>();
		project_Setting();
		
		// HWP font , header,footer 셋팅 
		FileExport_Handler( (HashMap<String, HashMap<String,  ArrayList<HashMap<String, Object>>>>) _hm.get("pageDatas"));
		
		String exportReplaceCharacter = "";
			
		exportReplaceCharacter = common.getPropertyValue("export.ReplaceCharacter");
		
		mComponent = new htmlComponent();
		mComponent.init();
		
		mComponent.replaceText = (exportReplaceCharacter!=null)?exportReplaceCharacter:"";
		
		// hwp Item 화면 그리기.
		if ( toHtmlFile(_path) ){
			return resultHtml;
		}
		else{
			return null;
		}
		
	}

	
	private void project_Setting()
	{
		mPageWidth = ValueConverter.getInteger(mProjectHm.get("pageWidth"));
		mPageHeight = ValueConverter.getInteger(mProjectHm.get("pageHeight"));
		mProjectName = ValueConverter.getString(mProjectHm.get("projectName"));
	}	
	
	
	/**
	 * 기본 font 및 style border 등을 지정.
	 */
	@SuppressWarnings("serial")
	private void FileExport_Handler( HashMap<String, HashMap<String,  ArrayList<HashMap<String, Object>>>> _totalItems)
	{
		mStyleList = new ArrayList<HashMap<String,Object>>();
		mParashapeList = new ArrayList<HashMap<String,Object>>();
		mBorderList = new ArrayList<HashMap<String, HashMap<String, String>>>();
		mFontList = new ArrayList<String>();
		mMatchingStyle = new ArrayList<String>();
		mMatchingAlign = new ArrayList<String>();
		mMatchingBorder = new ArrayList<String>();
		mCompID = new HashMap<String, String>();
		
		ArrayList<HashMap<String, Object>> _pageItem = new ArrayList<HashMap<String,Object>>();
		
		mTotalPage = _totalItems.size();
		
		for( int i = 0 ;i < mTotalPage ; i ++)
		{
			
			log.info(getClass().getName() + "::" + "Call FileExport_Handler Html()...   " + i);
			
			String _key = ValueConverter.getString(i);
			
			if( !_totalItems.containsKey(_key))
			{
				continue;
			}
			
			HashMap<String, ArrayList<HashMap<String, Object>>> _pageData = _totalItems.get(_key);
			
			_pageItem = _pageData.get("pageData");
			mTotalItems.add(_pageItem);
			
			htmlStyleSetting(_pageItem);
		}
	}
	
	/**
	 *<TYPEINFO ArmStyle="1" Contrast="0" FamilyType="2" Letterform="1" Midline="1" Proportion="0" StrokeVariation="1" Weight="6" XHeight="1" /> 
	*/
	/**
	 * FONTID		// 언어별 글꼴 [글꼴 ID]
	 * == 기본 == RATIO		// 언어별 장평
	 * == 기본 == CHARSPACING	// 언어별 자간
	 * == 기본 == RELSIZE		// 언어별 글자의 상대크기
	 * == 기본 == CHAROFFSET	// 언어별 글자위치 0%기준 100% 글자가 아래로 -100% 글자가 위로.		 
	 * ITALIC		// 기울임
	 * BOLD			// 진하게
	 * UNDERLINE	// 밑줄   (Type:밑줄 종류[Bottom(글자 아래),Center(글자 중간),Top(글자 위)], Shape:밑줄 모양[solid], Color:밑줄 색[0])
	 * =============STRIKEOUT	// 취소선 (Type:취소선 종류[None(없음),Continuous(연속선)], Shape:선 모양[solid], Color:취소선 색[0])
	 * =============OUTLINE		// 외곽선 (Type:외곽선 종류[solid])
	 * =============SHADOW		// 그림자 (Type:그림자 종류[Drop(비속선),Cont(연속)], Color:그림자 색, OffsetX:그림자 간격 X[10], OffsetY:그림자 간격 Y[10], Alpha:알파)
	 * =============EMBOSS		// 양각
	 * =============ENGRAVE		// 음각
	 * SUPERSCRIPT	// 위 첨자
	 * SUBSCRIPT	// 아래첨자
	 */
	private void htmlStyleSetting( ArrayList<HashMap<String, Object>> _pItem )
	{
		
		int j = 0;
		HashMap<String, Object> _itemG;
		
		// _fontList == 폰트명:폰트ID 저장 <FACENAMELIST> 만들때 사용함.
		// _styleList == {폰트스타일:Value},{폰트스타일:Value} 형식으로 저장 <CHARSHAPELIST> 만들때 사용함.
		// _MatchingStyle = Array 안에 String 형태로 속성 나열.
		// Str = 'fontFamilyID|fontSize|fontWeight|fontStyle|textDecoration|fontColor';
		// ex = ['1|2000|1|0|0|0','1|1400|0|0|0|0','2|1400|0|0|0|0']; 형태
		// mCompId[_tId] = 'sID|bID'; ( 1|1 이런식)
		// mCompId 에 Item의 id가 있을경우  sid와 bid를 해당 Item에 넣어준다.
		
		for( j = 0 ; j < _pItem.size() ; j++ )
		{
			_itemG = _pItem.get(j);
			
			String _className = ValueConverter.getString(_itemG.get("className"));
		
			if( _className.equals("UBLabel") || _className.equals("UBRotateLabel") || _className.equals("UBLabelBorder") || _className.equals("UBStretchLabel") || _className.equals("UBLabelV") ||
					_className.equals("UBTextArea") || _className.equals("UBTextInput") || _className.equals("UBLinkButton") || _className.equals("UBLabelBorderV") || _className.equals("UBLabelBand") )
			{
				hwpCreateDefaultStyle( _itemG );
			}
			else if( _className.equals("UBRichTextLabel"))
			{
//				hwpCreateRichTextStyle( _itemG );
			}
			else if( _className.equals("UBSVGRichText"))
			{
//				hwpCreateSVGRichTextStyle( _itemG );
			}
			else if( _className.equals("UBGraphicsRectangle") || _className.equals("UBGraphicsGradiantRectangle") || _className.equals("UBGraphicsCircle") || _className.equals("UBGraphicsLine"))
			{
				hwpCreateDefaultStyle(_itemG);
			}
			else if( _className.equals("UBTable") )
			{
//				hwpCreateTableStyle( _itemG );
			}
			else if( _className.equals("UBCheckBox") || _className.equals("UBRadioBorder") || _className.equals("UBComboBox") || _className.equals("UBDateFiled") )
			{
//				_itemG = setEformItemAttr(_itemG);
//				hwpCreateDefaultStyle( _itemG );
			}
			else if( _className.equals("UBSignature") || _className.equals("UBPicture") )
			{
				hwpCreateImageSrc( _itemG );
			}
//			else if( _className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBAreaChart") || 
//					_className.equals("UBLineChart") || _className.equals("UBPieChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBCombinedColumnChart"))
//			{
//				hwpCreateImageSrc( _itemG );
//			}
			else
			{
				hwpCreateImageSrc( _itemG );
			}
			
		}
	}
	
	
	
	
	private void hwpCreateImageSrc( HashMap<String, Object> _item )
	{
		String _code = "";
		
		String _className = ValueConverter.getString( _item.get("className") );
		
		if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBPicture") )
		{
			String _url = URLDecoder.decode( ValueConverter.getString(_item.get("src")) );
			
			if( _url.startsWith("http") ){
				byte[] _imgData= common.getBytesLocalImageFile( _url );
				if(_imgData == null )
				{
					return;
				}
				String base64 = new String(org.apache.commons.codec.binary.Base64.encodeBase64(_imgData));
				_code = "data:image/png;base64, " + base64;
			}else{
				_code = "data:image/png;base64, " + _url;
			}			
		}
		else if( _className.indexOf("Code") != -1 || _className.indexOf("Chart") != -1)
		{
			if( _className.equals("UBQRCode") )
			{
				String _dataStr = ValueConverter.getString(_item.get("src"));
				
				try {
					_dataStr = _dataStr.substring(4);
					_code = "data:image/svg+xml;base64, " + getSVGAreaImg(_dataStr, _className);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else
				_code = "data:image/png;base64, " + URLDecoder.decode( ValueConverter.getString(_item.get("src")));
		}
		else if( _className.equals("UBClipArtContainer") )
		{
			String _clipName = ValueConverter.getString(_item.get("clipArtData"));
			
			if( !_clipName.substring(_clipName.length() - 3, _clipName.length()).toUpperCase().equals("SVG"))
			{
				_clipName = _clipName+".svg";
			}
			
			String _urlStr = Log.basePath + "UView5/assets/images/svg/" + _clipName;
			try {
				_code = "data:image/svg+xml;base64, " + getSVGImg(_urlStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if( _className.equals("UBSVGArea") )
		{
			String _dataStr = ValueConverter.getString(_item.get("data"));
			
			try {
				_code = "data:image/svg+xml;base64, " + getSVGAreaImg(_dataStr, _className);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if( _code.length() == 3 ) return;
		mImgIdx++;
		mImageListHm.put(ValueConverter.getString(mImgIdx), _code);
		_item.put("imgId", mImgIdx);
	}
	
	/**
	 * Default Item의 Style 변수를 만듭니다.
	 * FontFamily 가 있을경우만 생성 합니다. 
	 * @param _item
	 * 
	 */
	private void hwpCreateDefaultStyle( HashMap<String, Object> _item )
	{
		if( _item.containsKey("fontFamily") == false ) return;
		
		String _id = "";
		
		_id = ValueConverter.getString(_item.get("id"));
		
		if( !mCompID.containsKey(_id) )
		{
			getItemFont( _item );
			getItemStyle( _item );
			getItemAlign( _item );
//			getItemBorder( _item );
			
			mCompID.put(_id, ValueConverter.getString(_item.get("fID")) + "#" + ValueConverter.getString(_item.get("sID")) + "#" + ValueConverter.getString(_item.get("pID"))); 
		}
		else
		{
			String[] _ar = new String[3];
			_ar = mCompID.get(_id).split("#");
			
			if( _ar.length == 3 )
			{
				_item.put("fID", _ar[0]);
				_item.put("sID", _ar[1]);
				_item.put("pID", _ar[2]);
			}
			else
			{
				log.info(getClass().getName() + "::" + "DefaultStyle Array Length 가 3이 아닙니다. ==>  length : " + _ar.length);
			}
			
		}
		
	}
	
	/**
	 * Table의 Style 변수를 만듭니다. 
	 * @param _item
	 * 
	 */
	private void hwpCreateTableStyle( HashMap<String, Object> _item )
	{
		String _id = ValueConverter.getString(_item.get("id"));
		HashMap<String, Object> _cell;
		ArrayList<ArrayList<HashMap<String, Object>>> _rows = new ArrayList<ArrayList<HashMap<String,Object>>>();
		ArrayList<HashMap<String, Object>> _cells = new ArrayList<HashMap<String,Object>>();
//		ArrayList<HashMap<String, Object>> _childs = _item.get("cells");
		String _cID = "";
		
		// cells 가 들어 오면.. 필요 없음.
		_rows = (ArrayList<ArrayList<HashMap<String,Object>>>) _item.get("rows");
		
		int _rowsSize = _rows.size();
		
		for (int i = 0; i < _rowsSize; i++) 
		{
			
//			_cells = (ArrayList<HashMap<String,Object>>) _item.get("cells");
			_cells = _rows.get(i);
			
			int _cellsSize = _cells.size();
			
			for (int j = 0; j < _cellsSize; j++) 
			{
				_cell = _cells.get(j);
				
				if( _cell == null ) continue;
				
				if( _cell.containsKey("colIndex") && _cell.containsKey("rowIndex") )
				{
					_cID = _id + "_" + _cell.get("rowIndex").toString() + "_" + _cell.get("colIndex").toString();
				}
				else
				{
					_cID = _id + "_" + i + "_" + j;
				}
				
				if( !mCompID.containsKey(_cID) )
				{
					getItemFont( _cell );
					getItemStyle( _cell);
					getItemBorder( _cell );
					getItemAlign( _cell );
					
					mCompID.put(_cID, _cell.get("fID") + "#" + _cell.get("sID") + "#" + _cell.get("bID") + "#" + _cell.get("pID"));
					
				}
				else
				{
					
					String[] _ar = mCompID.get(_cID).split("#");
					
					if( _ar.length == 4 )
					{
						_cell.put("fID", _ar[0]);
						_cell.put("sID", _ar[1]);
						_cell.put("bID", _ar[2]);
						_cell.put("pID", _ar[3]);
					}
					else
					{
						log.info(getClass().getName() + "::" + "Table Array Length 가 4이 아닙니다. ==>  length : " + _ar.length);
					}
					
				} // if mCompID;
				
			}// for _cellsSize;
			
		}// for _rowsSize;
		
	}
	
	
	/**
	 * RichTextLabel의 Style 변수를 만듭니다. 
	 * @param _item
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void hwpCreateRichTextStyle( HashMap<String, Object> _item )
	{
		HashMap<String, Object> _rtlObj = new HashMap<String, Object>();
		
		ArrayList<HashMap<String, Object>> _pChilds = new ArrayList<HashMap<String, Object>>();
		ArrayList<HashMap<String, Object>> _sChilds = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> _span = new HashMap<String, Object>();
		HashMap<String, Object> _paragraph = new HashMap<String, Object>();
		String _spanID = "";
		
		String _id = "";
		_rtlObj = changeRTLObject( _item );
		
		_id = ValueConverter.getString(_rtlObj.get("id"));
		int i = 0;
		int j = 0;
		int _pChildSize = 0;
		int _sChildSize = 0;
		if( !mCompID.containsKey(_id) )
		{
			
			mCompID.put(_id, _id);
			
			_pChilds.add((HashMap<String, Object>) _rtlObj.get("childs"));
			_pChildSize = _pChilds.size();
			for( i = 0 ; i < _pChildSize ; i ++ )
			{
				_paragraph = _pChilds.get(i);
				
				_sChilds.add((HashMap<String, Object>) _paragraph.get("childs"));
				_sChildSize = _sChilds.size();
				for ( j = 0; j < _sChildSize; j++) 
				{
					_spanID = _id + "_" + i + "_" + j;
					_span = _sChilds.get(j);
					
					getItemFont( _span );
					getItemStyle( _span );
					
					mCompID.put(_spanID, _span.get("fID") + "#" + _span.get("sID")); 
				}
			}
		}
		else
		{
			_pChilds = (ArrayList<HashMap<String,Object>>) _rtlObj.get("childs");
			_pChildSize = _pChilds.size();
			for( i = 0 ; i < _pChildSize ; i ++ )
			{
				_paragraph = _pChilds.get(i);
				_sChilds = (ArrayList<HashMap<String,Object>>) _paragraph.get("childs");
				_sChildSize = _sChilds.size();
				for( j = 0; j < _sChildSize ; j++) 
				{
					_spanID = _id + "_" + i + "_" + j;
					_span = _sChilds.get(j);
					
					String[] _ar = mCompID.get(_spanID).split("#");
					
					if( _ar.length == 2 )
					{
						_span.put("fID", _ar[0]);
						_span.put("sID", _ar[1]);
					}
					else
					{
						log.info(getClass().getName() + "::" + "RichTextLabel Array Length 가 2이 아닙니다. ==>  length : " + _ar.length);
					}
				}
			}
		}
		
		_item.put("childs", _rtlObj.get("childs"));
	}
	
	// 명우 수정해야 함.//
	private final String openTagRE = "/<(.w*).s*([^>]*)>/";
	/**
	 * Rich Text Labal의 Html 형태를 변경함.
	 * 
	 * object(RTL) 형태로 변경.
	 * x,y,width,height,border,background,className등 기본속성은 가지고 있음,
	 * 추가 속성 childs 에 object(P) 형태로 추가.
	 * object(P) 속성 기본 Font관련 속성과 lineHeight, letterSpacing 속성
	 * 추가 속성 childs 에 object(Span) 형태로 추가.
	 * object(Span) 속성 Font관련 속성 사용. 
	 * 
	 * @param _xmlTxt
	 * @return 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> changeRTLObject( HashMap<String, Object> _rtl )
	{
		HashMap<String, Object> _rtlObj = new HashMap<String, Object>();
		HashMap<String, Object> _pObj = new HashMap<String, Object>();
		HashMap<String, Object> _sObj = new HashMap<String, Object>();
		String _txt = "";
		
		// 좌표(?) '<' ~ '>'; 
		int _sPoint = 0;
		int _ePoint = 0;
		
		ArrayList<Object> _pChilds = new ArrayList<Object>();
		ArrayList<Object> _sChilds = new ArrayList<Object>();
		
		// html text 내용
		String _richText = "";
		
		// tag 내용.
		String _t = "";
		
		String[] _tList = {};
		
		HashMap<String, Object> _defStyle = null;
		
		boolean _fontFlg = false;
		
		// richTextLabel 기본 속성 담기.
		_rtlObj = (HashMap<String, Object>) _rtl.clone();
		_rtlObj.put("childs", _pChilds);
		
		
		String _fullTxt = ValueConverter.getString( _rtlObj.get("text")); 
		
		_richText = _fullTxt.substring( 12 , ( _fullTxt.length() - 14 ));
		
		while (true)
		{
			
			_sPoint = _richText.indexOf("<");
			_ePoint = _richText.indexOf( ">" ) + 1;
			
			_t = _richText.substring( _sPoint , _ePoint );
			
			Pattern p = Pattern.compile(openTagRE, Pattern.MULTILINE);
			Matcher m = p.matcher(_t);
			int cnt = 0;
			
			while (m.find())
			{
				_tList[cnt] = ValueConverter.getString(m.group(1));
				cnt++;
			}
			
			String _tagName = "";
			String _sString = "";
			
//			String _sName = "";
//			String _sValue = "";
			
//			int i = 0;
			
//			var _param:Array = [];
			
			_tagName = _tList[1];
			_sString = _tList[2];
			
			if( _tagName.equals("P") )
			{
				log.info(getClass().getName() + "::" + "P 인가요!? ==> " + _t);
				_pObj = new HashMap<String, Object>();
				_sChilds = new ArrayList<Object>();
				_pObj.put("childs", _sChilds);
				
				_pObj = getStyleMap( _sString , _pObj );
			}
			else if( _tagName.equals("FONT") )
			{
				log.info(getClass().getName() + "::" + "FONT 인가요!? ==> " + _t);
				
				if( !_fontFlg )
				{
					_sObj = new HashMap<String, Object>();
				}
				else
				{
					if( _defStyle == null )
					{
						_defStyle = _sObj;
					}
					
					_sObj = new HashMap<String, Object>();
				}
				
				if( _sPoint != 0 && _defStyle != null )
				{
					_sObj = _defStyle;
					
					_txt = _richText.substring( 0, _sPoint );
					
					_sObj.put("text", _txt);
					_sChilds.add(_sObj);
//					_pObj.put("childs",_sChilds );
					
					_sObj = new HashMap<String, Object>();
					
				}
				
				
				_fontFlg = true;
				
				
				_sObj = getStyleMap( _sString , _sObj );
				
				if( _defStyle != null )
				{
					if( !_sObj.containsKey("fontFamily" ) )
					{
						_sObj.put("fontFamily", _defStyle.get("fontFamily"));
					}
					
					if( !_sObj.containsKey("fontSize" ) )
					{
						_sObj.put("fontSize", _defStyle.get("fontSize"));
					}
					
					if( !_sObj.containsKey("fontColor" ) )
					{
						_sObj.put("fontColor", _defStyle.get("fontColor"));
					}
				}
				
				
			}
			else if( _tagName.equals("B") )
			{
				log.info(getClass().getName() + "::" + "B 인가요!? ==> " + _t);
				_sObj.put("fontWeight", "bold");
			}
			else if( _tagName.equals("I") )
			{
				log.info(getClass().getName() + "::" + "I 인가요!? ==> " + _t);
				_sObj.put("fontStyle" , "italic");
			}
			else if( _tagName.equals("U") )
			{
				log.info(getClass().getName() + "::" + "U 인가요!? ==> " + _t);
				_sObj.put("textDecoration" , "underline");
			}
			else
			{
				if( _sPoint >= 1 )
				{
					log.info(getClass().getName() + "::" + "text 인가요!? ==> " + _richText.substring( 0 , _sPoint ));
					log.info(getClass().getName() + "::" + "닫는 테그 인가요!? ==> " + _t);
					
					if( _fontFlg )
					{
						_fontFlg = false;
						_sObj.put("text", _richText.substring( 0 , _sPoint));
						_sChilds.add(_sObj);
//						_pObj.put("childs", _sChilds);
					}
					else
					{
						if( _defStyle != null )
						{
							_sObj = _defStyle;
							
							_txt = _richText.substring( 0, _sPoint );
							
							_sObj.put("text", _txt);
							
							_sChilds.add(_sObj);
//							_pObj.put("childs", _sChilds);
							
							_sObj = new HashMap<String, Object>();
							
						}
					}
					
				}
				else
				{
					log.info(getClass().getName() + "::" + "닫는 테그 인가요!? ==> " + _t);
					
					if( _sString.equals("/P") )
					{
						_defStyle = null;
						_pChilds.add(_pObj);
//						_rtlObj.put("childs", _pChilds);
					}
					else if( _sString.equals("/FONT") )
					{
						if( _fontFlg )
						{
							log.info(getClass().getName() + "::" + "true 면 text에 공백 넣기" + _t);
							_fontFlg = false;
							_sObj.put("text", " ");
							_sChilds.add(_sObj);
//							_pObj.put("childs", _sChilds);
						}
						else
						{
							log.info(getClass().getName() + "::" + "false 면 Text가 들어갔음." + _t);
						}
					}
					else
					{
						
					}
					
				}
			}
			
			log.info(getClass().getName() + "::" + "a = " + _t.length() + " || body = " + _richText.length() + "\n===================================================\n");
			if( _t.length() == _richText.length() )
			{
				break; 
			}
			else
			{
				_richText = _richText.substring( _ePoint , _richText.length() );
			}
			
		}
		// 위 부분이 걸러 지면 Tag나 Text를 바로 만들어야 함..ㄷㄷㄷㄷㄷ
		return _rtlObj;
	}
	
	
	/**
	 * SVGRichText의 Style 변수를 만듭니다. 
	 * @param _item
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void hwpCreateSVGRichTextStyle( HashMap<String, Object> _item )
	{
		HashMap<String, Object> _rtlObj = new HashMap<String, Object>();
		
		ArrayList< ArrayList< HashMap<String, Object> > > _pChilds = new ArrayList<ArrayList<HashMap<String,Object>>>(); 
		ArrayList<HashMap<String, Object>> _sChilds = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> _span = new HashMap<String, Object>();
		String _spanID = "";
		
//		_rtlObj = changeRTLObject( _item );
		_rtlObj = _item;
		
		int i = 0;
		int j = 0;
		int _pChildSize = 0;
		int _sChildSize = 0;
		getItemAlign( _rtlObj );
		_pChilds = (ArrayList<ArrayList<HashMap<String,Object>>>) _rtlObj.get("children");
		_pChildSize = _pChilds.size();
		for( i = 0 ; i < _pChildSize ; i ++ )
		{
			_sChilds = _pChilds.get(i);
			_sChildSize = _sChilds.size();
			for ( j = 0; j < _sChildSize; j++) 
			{
				_span = _sChilds.get(j);
				
				getItemFont( _span );
				getItemStyle( _span );
			}
		}
		
		_item.put("childs", _rtlObj.get("children"));
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> changeSVGRtObject( HashMap<String, Object> _rtl )
	{
		HashMap<String, Object> _rtlObj = new HashMap<String, Object>();
		HashMap<String, Object> _childObj = new HashMap<String, Object>();
		// 좌표(?) '<' ~ '>'; 
		int _sPoint = 0;
		int _ePoint = 0;
		
		ArrayList<Object> _childAr = new ArrayList<Object>();
		
		// html text 내용
		String _richText = "";
		
		// tag 내용.
		String _t = "";
		
		String[] _tList = {};
		
		HashMap<String, Object> _defStyle = null;
		
		boolean _childFlg = false;
		
		// richTextLabel 기본 속성 담기.
		_rtlObj = (HashMap<String, Object>) _rtl.clone();
		_rtlObj.put("childs", _childAr);
		
		String _fullTxt = ValueConverter.getString( _rtlObj.get("text")); 
		
		while(true)
		{
			break;
		}
		
		return _rtlObj;
	}
	
	
	private HashMap<String, Object> getStyleMap( String _s , HashMap<String, Object> _defObj)
	{
		int _currentIdx = 0;
		String _name = "";

		while (true)
		{
			int _idx1 = _s.indexOf( "=" , _currentIdx );
			
			if(_idx1 != -1 )
			{
				_name = _s.substring( _currentIdx, _idx1 ).toLowerCase();
			}
			else
			{
				_name = _s.substring( _currentIdx ).toLowerCase();
			}
			
			if( _name == "" ) break;

			if( _name.equals("face") )
			{
				_name = "fontFamily";
			}
			else if( _name.equals("size") )
			{
				_name = "fontSize";
			}
			else if( _name.equals("color") )
			{
				_name = "fontColor";
			}
			else
			{
				log.info(getClass().getName() + "::" + "_name이 뭐지!?!? ===>" + _name );
				break;
			}
			
			_currentIdx = _idx1 + 1;
			
			while (true)
			{
				if( _currentIdx >= _s.length() )
				{
					break;
				}
				
				String t = String.valueOf( _s.charAt(_currentIdx));
				if( (t.equals("\"")) || (t.equals("'")) )
				{
					int _idx2 = _s.indexOf( t , _currentIdx + 1);
					if( _idx2 == -1 )
					{
						_idx2 = _s.length();
					}
					
					_defObj.put(_name, _s.substring( _currentIdx + 1 , _idx2 ));
					_currentIdx = _idx2 + 1;
				}
				else
				{
					_idx1 = _currentIdx;
					
					if ( _currentIdx >= _s.length() )
					{
						break;
					}
					
					if( _currentIdx != _s.length() ){
						_defObj.put(_name, _s.substring( _idx1 , _currentIdx -1 )) ;
					}else{
						_defObj.put(_name, _s.substring( _idx1 , _currentIdx));
					}
				}
				
				if( _name.equals("fontColor") )
				{
					String _str = ValueConverter.getString( _defObj.get(_name));
					
					_str = _str.substring( 1 );
					
					_defObj.put(_name, Integer.parseInt( _str , 16 ));
				}
				
				
				break;
			}
			_currentIdx++;
		}
		
		return _defObj;			
	}
	
	
	
	/**
	 * font ID를 만들어 줍니다.
	 */
	private void getItemFont( HashMap<String, Object> _item )
	{
		boolean _matchingFlg = false;
		
		String _family = "";
		
		_family = ValueConverter.getString(_item.get("fontFamily"));
		
		if(_family == "�������" || _family.equals("null") )
		{
			_family = "굴림";
			_item.put("fontFamily", "굴림");
		}
		
		if( mFontList.size() > 0 )
		{
			int _idx = mFontList.indexOf(_family);
			if( _idx != -1 )
			{
				_item.put("fID", _idx + 1);
				_matchingFlg = true;
			}
		}
		
		if( !_matchingFlg )
		{
			mFontList.add( _family );
			_item.put("fID", mFontList.size());
		}
		
	}
	
	/**
	 * Setting 할 Style을 담는다. 
	 * @param _item
	 * 1. Style을 Object 형태로 담기.<br>
	 * 2. Object 형태를 String으로 변환.<br>
	 * 3. mMatchingStyle에 해당 String이 있는지 찾기.<br><br>
	 * True <br>
	 * 4. _item에 sID 값으로 해당 idx를 넘김. 끗<br>
	 * <br>
	 * False<br>
	 * 4. mMatchingStyle에 String을 Push 함.<br>
	 * 5. 같은 idx의 mStyleList에는 Object를 Push 함.<br>
	 * 6. _item에 sID 값으로 해당 idx를 넘김. 끗<br>
	 * 
	 */
	private void getItemStyle( HashMap<String, Object> _item )
	{
		String[] _styleKeyList = {"fID","fontSize","fontWeight","fontStyle","textDecoration","fontColorInt"};
		
		boolean _matchingFlg = false;
		
		HashMap<String, Object> _styleObj = new HashMap<String, Object>();
		
		int i = 0;
		String _key = "";
		
		StringBuilder _styleStrB = new StringBuilder();
		
		for( i = 0 ; i < _styleKeyList.length ; i ++)
		{
			_key = _styleKeyList[i];
		
			if( _item.containsKey(_key) )
			{
				if( _key.equals("fontWeight") )
				{
					if( ValueConverter.getString(_item.get( _key )).equals("normal") )
					{
						_styleObj.put(_key, false);
						_item.put(_key, false);
					}
					else
					{
						_styleObj.put(_key, true);
						_item.put(_key, true);
					}
				}
				else if ( _key.equals("fontStyle") )
				{
					if( ValueConverter.getString(_item.get( _key )).equals("normal") )
					{
						_styleObj.put(_key, false);
						_item.put(_key, false);
					}
					else
					{
						_styleObj.put(_key, true);
						_item.put(_key, true);
					}
				}
				else if ( _key.equals("textDecoration") )
				{
					if( ValueConverter.getString(_item.get( _key )).equals("none") )
					{
						_styleObj.put(_key, false);
						_item.put(_key, false);
					}
					else
					{
						_styleObj.put(_key, true);
						_item.put(_key, true);
					}
				}
				else
				{
					_styleObj.put(_key, _item.get(_key));
				}
			}
			else
			{
				if ( _key.equals("fontSize") )
				{
					_styleObj.put(_key, 1);
				}
				else if ( _key.equals("fontColorInt") )
				{
					_styleObj.put(_key, 0);
				}
				else
				{
					_styleObj.put(_key, false);
				}
			}
			
			
			_styleStrB.append(ValueConverter.getString(_styleObj.get(_key)) + "#");
		}

		
		if( mMatchingStyle.size() > 0 )
		{
			int _idx = mMatchingStyle.indexOf(_styleStrB.toString());
			if( _idx != -1 )
			{
				_item.put("sID", _idx + 1);
				_matchingFlg = true;
			}
		}
		
		
		if( !_matchingFlg )
		{
			mStyleList.add( _styleObj );
			mMatchingStyle.add( _styleStrB.toString() );
			_item.put("sID", mMatchingStyle.size());
		}
	}
	
	/**
	 * Setting 할 Align 과 indent 정보를 담는다.
	 * @param _item
	 */
	private void getItemAlign( HashMap<String, Object> _item )
	{
		String[] _alignKeyList = {"textAlign","indent","lineHeight"};
		
		boolean _matchingFlg = false;
		
		HashMap<String, Object> _alignObj = new HashMap<String, Object>();
		
		int i = 0;
		String _key = "";
		
		StringBuilder _alignStrB = new StringBuilder();
		
		for( i = 0 ; i < _alignKeyList.length ; i ++)
		{
			_key = _alignKeyList[i];
			String _value = "";
			if( _item.containsKey(_key) )
			{
				if( _key.equals("textAlign") )
				{
					_value = ValueConverter.getUpperString(_item.get( _key )); 
					_alignObj.put(_key, _value);
				}
				else if ( _key.equals("indent") )
				{
					_value = ValueConverter.getString(transPixcel2Hwpunit(ValueConverter.getFloat(_item.get( _key )))); 
					_alignObj.put(_key, _value);
				}
				else if ( _key.equals("lineHeight") )
				{
					_value = ValueConverter.getString(ValueConverter.getFloat(_item.get( _key )) * 100); //%
					_alignObj.put(_key, _value);
					_alignObj.put(_key+"Type", "Percent");
				}
			}
			else
			{
				if ( _key.equals("indent") )
				{
					_alignObj.put(_key, "0");
				}
				else if ( _key.equals("textAlign") )
				{
					_alignObj.put(_key, "Left");
				}
				else if ( _key.equals("lineHeight") )
				{
					
					if( _item.containsKey("lineGap"))
					{
						_value = ValueConverter.getString(ValueConverter.getFloat(_item.get("lineGap")) * 150f);
						_alignObj.put(_key, _value);
						_alignObj.put(_key+"Type", "BetweenLines");
					}
					else
					{						
						_value = ValueConverter.getString(ValueConverter.getFloat(_item.get( _key )) * 100); //%
						_alignObj.put(_key, _value);
						//_alignObj.put(_key, "120");
						_alignObj.put(_key+"Type", "Percent");		
					}
				}
				
			}
			
			
			_alignStrB.append(ValueConverter.getString(_alignObj.get(_key)) + "#");
		}

		
		if( mMatchingAlign.size() > 0 )
		{
			int _idx = mMatchingAlign.indexOf(_alignStrB.toString());
			if( _idx != -1 )
			{
				_item.put("pID", _idx);
				_alignObj.put("pID", _idx);
				_matchingFlg = true;
			}
		}
		
		
		if( !_matchingFlg )
		{
			mMatchingAlign.add( _alignStrB.toString() );
			_alignObj.put("pID", mMatchingAlign.size() - 1);
			mParashapeList.add( _alignObj );
			_item.put("pID", mMatchingAlign.size() - 1);
		}
		
	}
	
	/**
	 * Setting 할 borderStyle을 담는다.<br> 
	 * @param _item<br>
	 * 1. border를 Object 형태로 담기.<br>
	 * 2. Object 형태를 String으로 변환.<br>
	 * 3. mMatchingBorder에 해당 String이 있는지 찾기.<br><br>
	 * True <br>
	 * 4. _item에 bID 값으로 해당 idx를 넘김. 끗<br>
	 * <br>
	 * False<br>
	 * 4. mMatchingBorder에 String을 Push 함.<br>
	 * 5. 같은 idx의 mBorderList에는 Object를 Push 함.<br>
	 * 6. _item에 bID 값으로 해당 idx를 넘김. 끗<br>
	 */
	@SuppressWarnings("unchecked")
	private void getItemBorder( HashMap<String, Object> _item )
	{
		HashMap<String, HashMap<String, String>> _borderObj = new HashMap<String, HashMap<String, String>>();
		
		boolean _matchingFlg = false;
		
//		String _className = "";
//		 
//		_className = ValueConverter.getString(_item.get("className"));
		
		int i = 0;
		String key = "";
		HashMap<String, String> _obj = new HashMap<String, String>();
		// borderStr = 'LeftStyle|TopStyle|RightStyle|BottomStyle';
		// LeftStyle = 'Type|Width|Color|Fillbruse';
		String _borderStr = "";
		StringBuilder _borderStrB = new StringBuilder();
		
//		className	"Cell"	
//		
//		topBorderColor	0	
//		topBorderThickness	0.3	
//		topBorderType	"SOLD"	
//		rightBorderColor	0	
//		rightBorderThickness	0.3	
//		rightBorderType	"SOLD"	
//		bottomBorderColor	0	
//		bottomBorderThickness	0.3	
//		bottomBorderType	"SOLD"	
//		leftBorderColor	0	
//		leftBorderThickness	0.3	
//		leftBorderType	"SOLD"	
//		backgroundColor	16777215 [0xffffff]	
		
		ArrayList<String> _borderSide = (ArrayList<String>) _item.get("borderSide");
		ArrayList<String> _borderType = (ArrayList<String>) _item.get("borderTypes");
		ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _item.get("borderWidths");
		ArrayList<Integer> _borderColors = (ArrayList<Integer>) _item.get("borderColorsInt");
		
		for( i = 0 ; i < _borderSide.size() ; i ++ )
		{
			key = _borderSide.get(i);
			
			_obj = new HashMap<String, String>();
			
			_obj.put("Type", getBorderType(_borderType.get(i).toUpperCase()));
			_obj.put("Width", ValueConverter.getString(_borderWidths.get(i)));
			_obj.put("Color", ValueConverter.getString(_borderColors.get(i)));
			
			if( _item.get("backgroundAlpha").equals("0") == false )
			{
				_obj.put("Fillbrush", ValueConverter.getString(_item.get("backgroundColorInt")) + '#' + ValueConverter.getString(_item.get("backgroundAlpha"))); 
			}
			
			_borderObj.put(key, _obj);
			if( _item.get("backgroundAlpha").equals("0") == false )
			{
				_borderStrB.append(_obj.get("Type") + '#' + _obj.get("Width") + '#' + _obj.get("Color") + '#' + _obj.get("Fillbrush") + '#');
			}
			else
			{
				_borderStrB.append(_obj.get("Type") + '#' + _obj.get("Width") + '#' + _obj.get("Color") + '#');
			}
			
		}
		
		if( mMatchingBorder.size() > 0 )
		{
			int _idx = mMatchingBorder.indexOf(_borderStrB.toString());
			if( _idx != -1 )
			{
				_item.put("bID", _idx + 1);
				_matchingFlg = true;
			}
		}
		
		
		if( !_matchingFlg )
		{
			mBorderList.add( _borderObj );
			mMatchingBorder.add( _borderStrB.toString() );
			_item.put("bID", mMatchingBorder.size());
		}
		
	}
	
	/**
	 * 페이지 시작 Tag
	 * @return
	 */
	private void PageStart_Setting()
	{
		resultBuilder.append("<pageArea id=\"page" + mCurrntPage + "\" style=\"position:relative;\">");

		mCurrntPage++;
	}
	
	/**
	 * 페이지 끝 Tag
	 * @return
	 */
	private void PageEnd_Setting()
	{
		//resultBuilder.append("</DIV>\n");
		resultBuilder.append("</pageArea>\n");
	}
	
	/**
	 * 페이지 기본 Tag 
	 */
	private void PageParhing_Handler( HashMap<String, Object> _item , int _zOrder)
	{
		
		String _className = "";

		_className = ValueConverter.getString(_item.get("className"));
		
		if( _className.equals("UBLabel") || _className.equals("UBRotateLabel") || _className.equals("UBLabelBorder") || _className.equals("UBStretchLabel") || _className.equals("UBLabelV") ||
				_className.equals("UBTextArea") || _className.equals("UBTextInput") || _className.equals("UBLinkButton") || _className.equals("UBLabelBorderV") || _className.equals("UBLabelBand") )
		{
			resultBuilder.append(mComponent.RectangleMatching_handler(_item, _zOrder));
		}
		else if( _className.equals("UBRichTextLabel"))
		{
//			resultBuilder.append(mComponent.RichTextlabelComponentInWord( _item , mPageHeight ));
		}
		else if( _className.equals("UBSVGRichText"))
		{
			resultBuilder.append(mComponent.SVGRichTextLabelMatching_handler( _item ));
		}
		else if( _className.equals("UBGraphicsRectangle") || _className.equals("UBGraphicsGradiantRectangle") || _className.equals("UBGraphicsCircle") || _className.equals("UBGraphicsLine"))
		{
			resultBuilder.append(mComponent.GraphicMatching_handler( _item, _zOrder ));
		}
		else if( _className.equals("UBTable") )
		{
			resultBuilder.append(mComponent.TableMatching_handlerDiv( _item, _zOrder ));
		}
		else if( _className.equals("UBCheckBox") || _className.equals("UBRadioBorder") || _className.equals("UBComboBox") || _className.equals("UBDateFiled") )
		{
			_item = setEformItemAttr(_item);
			resultBuilder.append(mComponent.RectangleMatching_handler(_item, _zOrder));
		}
		else if( _className.equals("UBSignature")|| _className.equals("UBPicture") )
		{
			resultBuilder.append(mComponent.ImagesMatching_handler( _item, _zOrder, mImageListHm ));
		}
//		else if( _className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBAreaChart") || 
//				_className.equals("UBLineChart") || _className.equals("UBPieChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBCombinedColumnChart"))
//		{
//			resultBuilder.append(mComponent.ImagesMatching_handler( _item ));
//		}
		else
		{
			resultBuilder.append(mComponent.ImagesMatching_handler( _item, _zOrder, mImageListHm ));
		}
	}

	/**
	 * <pre>
	 * eForm 아이템(라디오/체크/콤보박스)을 라벨로 그리기 위한 세팅 함수.
	 * 라디오버튼/체크박스 아이템은 선택된 상태에 따라 문자로 치환하여 표시한다.
	 * 라디오 :  "⊙ ":"○ "
	 * 체크 : "▣ ":"□ "
	 * 체크박스의 경우 체크모양 딩벳기호는 깨져 네모로 대체한다.
	 * 2016-10-05 추가
	 * </pre>
	 * @param _item 라벨로 변환할 eForm 아이템
	 * @return _item 라벨속성 추가된 아이템.
	*/
	private HashMap<String, Object> setEformItemAttr( HashMap<String, Object> _item ) {
		
		String itemClassName = ValueConverter.getString(_item.get("className"));
		boolean isSelected = ValueConverter.getBoolean(_item.get("selected"));
		String iconTxt = "";
		
		String symbolType = ValueConverter.getString(_item.get("symbolType"));

		if( itemClassName.equals("UBRadioBorder") ){
			if( symbolType.equalsIgnoreCase("check") ){
				iconTxt = (isSelected)? "▣ ":"□ ";
			}else{
				iconTxt = (isSelected)? "⊙ ":"○ ";	
			}
			
			_item.put("text", iconTxt+ValueConverter.getString(_item.get("label")) );
		}
		else if( itemClassName.equals("UBCheckBox") ){
			iconTxt = (isSelected)? "▣ ":"□ ";
			_item.put("text", iconTxt+ValueConverter.getString(_item.get("label")) );
		}
		else if( itemClassName.equals("UBComboBox") || itemClassName.equals("UBDateFiled") ){
			// 콤보, 데이트필드의 경우 테두리를 그리지 않도록 한다. - 이장환이사님의견 반영.
			_item.put("borderTypes", new ArrayList<String>( Arrays.asList("none","none","none","none")) );
			_item.put("borderWidths", new ArrayList<Integer>( Arrays.asList(0,0,0,0)) );
		}
		
		
		// 라벨 생성시 필요한 속성 값을 기본으로 세팅.
		_item.put("lineHeight", 1.16 );
		_item.put("padding", 3 );
		_item.put("verticalAlign", "middle" );
		_item.put("formatter", null );
		_item.put("alpha", 1 );
		_item.put("textAlign", "left" );
		
		return _item;
	}
	
	public String getSVGImg(String _url) throws Exception 
	{
        
        File file = new File(_url);
        
        FileInputStream is = new FileInputStream(file);
        byte[] _imgData = new byte[(int) file.length()];
        
        is.read(_imgData);
        
        String _result = common.base64_encode_byte(_imgData);
        is.close();
        return _result;
    }
	
	public String getSVGAreaImg(String _data, String itemClassName) throws Exception
	{
		
		String _dataStr = URLDecoder.decode(_data, "UTF-8");
		
		_dataStr = _dataStr.replaceAll("%20", " ");
		
		if( itemClassName != null && itemClassName.contains("UBQRCode") )
		{
			_dataStr = Base64Decoder.decode(_dataStr);
			System.out.print("UBQRCode:svg=[" + _dataStr + "]");
		}
		
		StringInputStream is = new StringInputStream(_dataStr , "UTF-8");
		byte[] _imgData = _dataStr.getBytes();
		
		is.read(_imgData);
		
		String _result = common.base64_encode_byte(_imgData);
		is.close();
		return _result;
	}
	
	/**
	 * Body 의 상단 부분 설정
	 * @return
	 */
	private String headHtmlInit_handler()
	{
		StringBuilder _headBuilder = new StringBuilder();
				
		_headBuilder.append("<HEAD>\n");
		
		// TITLE = 문서명, DATE = 날짜
		_headBuilder.append("<TITLE>" + mProjectName + "</TITLE>\n");
		
		_headBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n");
		
/*
<style>
body {
  background: rgb(204,204,204); 
}
pageArea {
  background: white;
  width: 21cm;
  height: 29.7cm;
  display: block;
  margin: 0 auto;
  margin-bottom: 0.5cm;
  box-shadow: 0 0 0.5cm rgba(0,0,0,0.5);
}
@media print {
  body, pageArea {
    margin: 0;
    box-shadow: 0;
  }
}
</style>
 */
		
		_headBuilder.append("<style>\n");
		_headBuilder.append("body {\n");
		_headBuilder.append("  background: rgb(204,204,204); \n");
		_headBuilder.append("}\n");
		_headBuilder.append("pageArea {\n");
		_headBuilder.append("  background: white;\n");
		_headBuilder.append("  width:" + mPageWidth + "px;\n");
		_headBuilder.append("  height:" + mPageHeight + "px;\n");
		_headBuilder.append("  display: block;\n");
		_headBuilder.append(" margin: 0 auto;\n");		
		_headBuilder.append("  margin-bottom: 20px;\n");				
		_headBuilder.append("  box-shadow: 0 0 20px rgba(0,0,0,0.5);\n");			
		_headBuilder.append("}\n");		
		
		_headBuilder.append("@media print {\n");	
		_headBuilder.append("body, pageArea {\n");			
		_headBuilder.append("    margin: 0;\n");			
		_headBuilder.append("    box-shadow: 0;\n");			
		_headBuilder.append("  }\n");			
		_headBuilder.append("}\n");			
		_headBuilder.append("</style>\n");			

		_headBuilder.append("</HEAD><BODY>\n");
		
		return _headBuilder.toString();
	}
	
	
	
	/**
	 * 이미지 데이터 셋팅
	 * @return
	 */
	private String ImageDataTagSetting()
	{
		StringBuilder _imgDataStrB = new StringBuilder();
		HashMap<String, String> _imgAr = mImageListHm;
//		ArrayList _imgAr = mImageListAr;
		int _cnt = 0;
		String _code = "";
		String fullCode = "";
		
		_imgDataStrB.append("</BODY>\n");
		_imgDataStrB.append("<TAIL>\n");
		_imgDataStrB.append("<BINDATASTORAGE>\n");
		
		int _imgArSize = _imgAr.size();
		
		for( _cnt = 0 ; _cnt < _imgArSize ; _cnt++ )
		{
			String _idx = ValueConverter.getString(_cnt + 1);
			
			if( _imgAr.containsKey(_idx) )
			{
				fullCode = _imgAr.get(_idx);
				
				if( fullCode.length() <  3 )
				{
					_code = fullCode;
				}
				else
				{
					_code = fullCode.substring(3);
				}
				
				_imgDataStrB.append("<BINDATA Encoding=\"Base64\" Id=\"" + _idx + "\" Size=\"" + _code.length() + "\">");	
				_imgDataStrB.append(_code);
				_imgDataStrB.append("</BINDATA>\n");
			}
		}
		_imgDataStrB.append("</BINDATASTORAGE>\n");
		_imgDataStrB.append("</TAIL>\n");
		
		mImageListHm = new HashMap<String, String>();
		return _imgDataStrB.toString();
	}
	
	
	
	/**
	 *  HWP 에서 사용하는 사이즈로 변경
	 *  Pixcel => Hwpunit
	 * @param _num
	 * @return
	 */
	private float transPixcel2Hwpunit( float _num )
	{
		float _mm = _num * 0.26458333331386f;
		
		float _inch = _mm * 0.03937007874f; 
		
		float _hwpunit = (float) Math.floor( _inch * 7200f );  
		
		return _hwpunit;
	}
	
	
	/**
	 * HWP 에서 사용하는 컬러로 변경
	 * unit => MSAColor
	 * @return
	 */
	private String ChangeColorToMSA( Integer _uint )
	{
		
		if( _uint == 0 ) return "0";
		
		String _color = Integer.toString(_uint, 16);
		
		while(_color.length() < 6){
			_color = "0" + _color;
		}

		return ValueConverter.getString( Integer.parseInt( ( _color.substring( 4, 6 ) + _color.substring( 2, 4 ) + _color.substring( 0, 2 ) )  , 16 ));
	}
	
	
	/**
	 * Alpha 값을 HWP 에 사용하는 형식으로 변경
	 *  1 = 255;
	 * @param _alpha
	 * @return
	 */
	private float getGraphicAlpha( float _alpha )
	{
		float _a = 0f;
		float _b = 0f;
		float _c = 0f;
		
		if( _alpha == 0f ) return 255f;
		
		if( _alpha != 1f )
		{
			_b = 1f / _alpha; 
			_c = 255f / _b;
			_a = 255f - _c;
		}
		else
		{
			_a = 0f;
		}
		
		return _a;
	}
	
	/**
	 * borderType을 변경해서 돌려줌
	 * @param _side
	 * @return
	 */
	private String getBorderType( String _side )
	{
		String _type = "";
		
		if( _side.equals("SOLD") || _side.equals("SOLID") )
		{
			_type = "Solid";
		}
		else if( _side.equals("DASH") )
		{
			_type = "Dash";
		}
		else if( _side.equals("DASH_DOT") )
		{
			_type = "DashDot";
		}
		else if( _side.equals("DASH_DOT_DOT") )
		{
			_type = "DashDotDot";
		}
		else if( _side.equals("DOT") )
		{
			_type = "Dot";
		}
		else if( _side.equals("DOUBLE") )
		{
			_type = "DoubleSlim";
		}
		else
		{
			_type = "None";
		}
		
		return _type;
	}
	
	
	/**
	 * Component 생성.
	 */
	/**
	 * functionName :	toHtmlFile</br>
	 * desc			:	HWP파일생성 - 파일을 서버에 생성후 한페이지씩 append처리 
	 * @param _filePath
	 */
	private boolean toHtmlFile( String _filePath )
	{
		FileWriterWithEncoding fw = null;
		
		try {
			// HTML 헤더 정보 확인후 잠시 텀을 둠
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		  try{
		     fw = new FileWriterWithEncoding( _filePath,"UTF-8", true ); 
		     
		     fw.write(mHeaderTxt);
		     fw.write(headHtmlInit_handler());
		     
		     Date end = new Date();
		     Date start = new Date();
		    long diff = end.getTime() - start.getTime();
			long diffSec = diff / 1000% 60;         
			long diffMin = diff / (60 * 1000)% 60;  
		     
		     int _pageSize = mTotalItems.size();
				for (int i = 0; i < _pageSize; i++) 
				{
					end = new Date();
					diff = end.getTime() - start.getTime();
					diffSec = diff / 1000% 60;         
					diffMin = diff / (60 * 1000)% 60;        
					log.debug(" PageStart_Setting Start   >>>>> [ PAGE : " + i + "    TIME : "+ diffMin +":"+diffSec+"."+diff%1000+" ]");
					resultBuilder = new StringBuilder();
					
					PageStart_Setting();
					
					ArrayList<HashMap<String, Object>> _itemList = mTotalItems.get(i);
					
					for (int j = 0; j < _itemList.size(); j++) 
					{
						HashMap<String, Object> _item = _itemList.get(j);
						PageParhing_Handler(_item, j);
					} 

					end = new Date();
					diff = end.getTime() - start.getTime();
					diffSec = diff / 1000% 60;         
					diffMin = diff / (60 * 1000)% 60;        
					log.debug(" PageStart_Setting End     >>>>> [ PAGE : " + i + "    TIME : " + diffMin +":"+diffSec+"."+diff%1000+" ]");
					
					PageEnd_Setting();
					fw.write(resultBuilder.toString());
				}
				
				//fw.write(ImageDataTagSetting());
				fw.write(mFooterTxt);
				
				//fw.close();
				
				end = new Date();
				diff = end.getTime() - start.getTime();
				diffSec = diff / 1000% 60;         
				diffMin = diff / (60 * 1000)% 60;        
				log.debug("HTMLFile Close  >>>>> [ Time : "  + diffMin +":"+diffSec+"."+diff%1000+" ]");
				
				return true;
		  }catch (Exception e) {
			  log.error(" toHtmlFile ===== > " + e.getMessage());  
			  return false;
		  }
		  finally {
				if(fw!=null)
					try {
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
		  }
	}
	
	
}
