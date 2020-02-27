package org.ubstorm.service.parser.formparser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.fit.cssbox.demo.ImageRenderer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.formatter.UBFormatter;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.logger.Log;
//import org.ubstorm.service.method.ViewerInfo5;
import org.ubstorm.service.parser.ItemPropertyProcess;
import org.ubstorm.service.parser.ItemPropertyVariable;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.BandInfoMapDataSimple;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.ProjectInfo;
import org.ubstorm.service.parser.formparser.info.item.UBComponent;
import org.ubstorm.service.parser.svg.SvgRichTextObjectHandler;
import org.ubstorm.service.parser.svg.object.svgTable;
import org.ubstorm.service.utils.ImageUtil;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ItemConvertParser {

	//TEST
	protected Logger log = Logger.getLogger(getClass()); 
	
	public static final String TABLE_VERSION_NEW = "2.0"; 
	public static final String TABLE_VERSION_OLD = "1.0"; 
	
	ItemPropertyVariable mItemPropVar;
	ItemPropertyProcess mPropertyFn;
	HashMap<String, ArrayList<HashMap<String, Object>>> DataSet;
	private UDMParamSet m_appParams = null;
	private String mChartDataFileName = "chartdata.dat";
	private HashMap<String,String> mImageData;
	private HashMap<String,Object> mChartData;
	private Function mFunction;
	private int mMinimumResizeFontSize = 0;	// resizeFont 사용시 최소값 지정
	
	// Export여부를 판단하기 위한 변수
	private String isExportType = "";
	
	public HashMap<String, String> mOriginalDataMap = null;			// originalData값으 가지고 있는 객체
	public ArrayList<ArrayList<String>> mGroupDataNamesAr = null;	// 그룹핑된 데이터명을 가지고 있는 객체

	public int mGroupCurrentPageIndex	= 0;
	public int mGroupTotalPageIndex = 0;
	
	public HashMap<String , String> mRadioGroupList = new HashMap<String , String>();
	
	private HashMap<String, Object> mChangeItemList = new HashMap<String, Object>();
	
	private boolean mUseSimpleExcel = true;			// Excel문서 저장시 속도개선을 위하여 속성정리를 사용 여부 ( true:사용, false:미사용-기존과 동일하게 동작 )
	
	private String mReportType = "";				// 해당 문서의 reportType값 
	
	public String getIsExportType() {
		return isExportType;
	}

	public void setIsExportType(String isExportType) {
		this.isExportType = isExportType;
	}
	
	public String getReportType() {
		return mReportType;
	}
	
	public void setReportType(String _reportType) {
		this.mReportType = _reportType;
	}
	
	public int getMinimumResizeFontSize() {
		return mMinimumResizeFontSize;
	}

	public void setMinimumResizeFontSize(int mMinimumResizeFontSize) {
		this.mMinimumResizeFontSize = mMinimumResizeFontSize;
	}
	
	public void setChangeItemList( HashMap<String, Object> _value)
	{
		if( _value == null ) return;
		
		mChangeItemList = new HashMap<String, Object>();
		HashMap<String, String> _changePageItem = new HashMap<String, String>();
        Iterator<String> keys = _value.keySet().iterator();
        while( keys.hasNext() ){
            String key = keys.next();
            HashMap<String, ArrayList<HashMap<String,Object>>> _changePageData = (HashMap<String, ArrayList<HashMap<String,Object>>>) _value.get(key);
            ArrayList<HashMap<String, Object>> _changeList =  _changePageData.get("Controls");
            
            for (HashMap<String, Object> _cItem : _changeList) {
            	
            	if( _cItem != null && _cItem.get("Value") != null && _cItem.get("CtrlId") != null )
            	{
            		_changePageItem.put(_cItem.get("CtrlId").toString(), _cItem.get("Value").toString());
            	}
            	
			}
            
            mChangeItemList.put(key, _changePageItem);
        }
	}
	
	public HashMap<String, Object> convertChangeItemDataText( int _currnetPage, HashMap<String, Object> _item, String _itemID )
	{
		 if( mChangeItemList.isEmpty() == false && _item.containsKey("dataType") && !( _item.get("dataType").equals("1") ||_item.get("dataType").equals("2") ||_item.get("dataType").equals("3") ) )
		 {
			if( mChangeItemList.containsKey( String.valueOf(_currnetPage ) ) )
			{
				String _id = _itemID;
				if( _item.containsKey("id") && _item.get("id") != null && _itemID.equals("")) _id =  _item.get("id").toString();
				
				HashMap<String, String> _changePageItem  = (HashMap<String, String>) mChangeItemList.get(String.valueOf(_currnetPage )  );
				if(_changePageItem.containsKey( _id ) )
				{
					if( _item.containsKey("text") )
					{
						_item.put("text", _changePageItem.get( _id ));
					}
				}
			}
		 }
		
		return _item;
	}
	
	public ItemConvertParser( HashMap<String, ArrayList<HashMap<String, Object>>> _data , String chartDataFileName,UDMParamSet appParam ) {
		super();

		// TODO Auto-generated constructor stub
		mItemPropVar = new ItemPropertyVariable();
		
		mPropertyFn = new ItemPropertyProcess();
		DataSet = _data;
		m_appParams = appParam;
		mChartDataFileName = chartDataFileName;
		
		if( m_appParams != null )
		{
			String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
			boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);		
			
			mItemPropVar.setIsMarkAny(_isMarkAny);
		}
	}
	
	public HashMap<String, Object> convertItemData(BandInfoMapData _bandInfo, HashMap<String, Value> currentItemData, String dataSet, int rowIndex ,HashMap<String, Object> _param, int _startIndex, int _lastIndex , int _totalPageNum , int _currentPage ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);	
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _className 		= "";
		String _name 		= "";
//		String _value 		= "";
		Object _value 		= null;
		String _dataID 		= "";
		int k = 0;
		
		String _model_type = "";
		String _barcode_type = "";
		String _itemId = "";
		
//		속도개선을 위한 테스트 함수
//		if( mUseSimpleExcel )
		if( isExportType.equals("EXCEL") && mUseSimpleExcel )
		{
			return convertItemDataSimpleExport( _bandInfo, currentItemData, dataSet, rowIndex , _param, _startIndex, _lastIndex , _totalPageNum , _currentPage);
		}
		
		if(currentItemData.containsKey("id"))
		{
			_itemId = currentItemData.get("id").getStringValue();
		}
		
		HashMap<String, Object> _propList = new HashMap<String, Object>();
		
		_className = currentItemData.get("className").getStringValue();
		// image variable
		String _prefix="";
		String _suffix="";
					
		Boolean _bType = false;

		if(_className.equals("UBLabelBorder"))
		{
			_bType = true;
		}
		else
		{
			_bType = false;
		}
		
		if( dataSet == null || dataSet.equals("") )
//		if( dataSet.equals("") && !currentItemData.get("dataSet").getStringValue().equals("") )
		{
				
			if( currentItemData.get("dataSet") != null ){
				if(   currentItemData.get("dataSet").getStringValue() != null ){
					if(  !currentItemData.get("dataSet").getStringValue().equals("") ){
						_dataID =  currentItemData.get("dataSet").getStringValue();
					}
				}
			}
			
			if( _bandInfo.getDataSet().equals("") == false && _bandInfo.getAdjustableHeight() && _bandInfo.getResizeText() )
			{
				_dataID = _bandInfo.getDataSet();
				
				if( mOriginalDataMap.get( _dataID ) != null && currentItemData.get("dataSet") != null && currentItemData.get("dataSet").getStringValue() != null && currentItemData.get("dataSet").getStringValue().equals("null") == false )
				{
					if( mOriginalDataMap.get( _dataID ).equals(currentItemData.get("dataSet").getStringValue()) == false )
					{
						_dataID = currentItemData.get("dataSet").getStringValue();
					}
				}
			}

		}
		else
		{
			if( mOriginalDataMap.get( dataSet ) != null && currentItemData.get("dataSet") != null && currentItemData.get("dataSet").getStringValue() != null && currentItemData.get("dataSet").getStringValue().equals("null") == false )
			{
				if( mOriginalDataMap.get( dataSet ).equals(currentItemData.get("dataSet").getStringValue()) == false )
				{
					_dataID = currentItemData.get("dataSet").getStringValue();
				}
				else
				{
					_dataID = dataSet;	
				}
			}
			else
			{
				_dataID = dataSet;
			}
		}
		
		if( DataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
			

		Set<String> _keySet = currentItemData.keySet();
		Object[] hmKeys = _keySet.toArray();
		
		_propList = mItemPropVar.getItemName(_className);
		
		if(_propList == null ) return null;
		
		// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
		if( _propList.containsKey("rowId") )
		{
			_propList.put("rowId", rowIndex);
		}
		
		// system function
		String _systemFunction="";
		
		// formatter variables 
		String _formatter="";
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";
		String _inputForamtString = "";
		String _outputFormatString = "";
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		
		// edit formatter variables (e-form)
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";
		
		// 1. 실제 Export 할때 필요한 최소한의 속성만을 담아두기
		// 2. 최소한의 속성을 이용하여 생성된 hashmap를 담기
		// 3. currentItemData에 해당 hashMap를 담아두고 처리한다
		
		
		for ( k = 0; k < hmKeys.length; k++) {
			
			_name = (String) hmKeys[k];
			_value = currentItemData.get(_name).getValue();
			
			if(_propList.containsKey(_name))
			{
				if( _name.equals("fontFamily"))
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					if(common.isValidateFontFamily((String)_value))
						_propList.put(_name, _value);
					else
						_propList.put(_name, "Arial");
				}
				else if( _name.equals("contentBackgroundColors")  )
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					
					ArrayList<String> _arrStr = new ArrayList<String>();
					_arrStr = mPropertyFn.getColorArrayString( (String)_value );
					_propList.put(_name, _arrStr);
					
					_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
					_propList.put((_name + "Int"), _arrStr);
					
				}
				else if( _name.equals("contentBackgroundAlphas") )
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					
					ArrayList<String> _arrStr = new ArrayList<String>();
					_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
					_propList.put(_name, _arrStr);
					
				}
				else if( _name.indexOf("Color") != -1 && _name.equals("borderColors") == false && _name.equals("borderColorsInt") == false)
				{	
					//backgroundColor/fontColor 과 같이 color값이 ArrayList로 생성되어 있을경우 rowIndex값에 맞춰 color값을 변경
					if( _value.toString().contains(",") )
					{
						ArrayList<String> _valueArray = Value.setArrayString( _value.toString() );
						_value = _valueArray.get(rowIndex%_valueArray.size());
						_propList.put((_name + "Int"), _value);
						
						_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
						_propList.put(_name, _value);
					}
					else
					{
						_propList.put((_name + "Int"), _value);
						
						_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
						_propList.put(_name, _value);
					}
				}
				else if( _name.equals("lineHeight"))
				{
					//_value = "1.16"; //TODO LineHeight Test
					if( _value.toString().indexOf("%") != -1 )
					{
						_value = _value.toString().replace("%25", "").replace("%", "");
						_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
					}
					_propList.put(_name, _value);
				}
				else if( _name.equals("label"))
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("borderType"))
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("text"))
				{
					_propList.put(_name, _value == null ? "" : _value);
				}
				else if( _name.equals("borderSide"))
				{
					ArrayList<String> _bSide = new ArrayList<String>();
					if( currentItemData.get(_name).getStringValue().equals("none") == false )
					{
						
						_bSide = mPropertyFn.getBorderSideToArrayList( currentItemData.get(_name).getStringValue() );

						if( _bSide.size() > 0)
						{
							String _type = (String) _propList.get("borderType");
							_type = mPropertyFn.getBorderType(_type);
							_propList.put("borderType", _type);
						}

					}

					_propList.put(_name, _bSide);
				}
				else if( _name.equals("type") )
				{
					_model_type = _value.toString();
					_propList.put(_name, _value);
				}
				else if( _name.equals("barcodeType") )
				{
					_barcode_type = _value.toString();
					_propList.put(_name, _value);
				}
				else if( _name.equals("clipArtData") )
				{
					_propList.put(_name, _value + ".svg");
				}
				else
				{
					_propList.put(_name, _value);
				}
			}
			else if( _name.equals("checked") )
			{
				_propList.put("selected", _value);
			}
			else if( _name.equals("conerRadius") )
			{
				_propList.put("rx", _value);
				_propList.put("ry", _value);
			}
			else if(_name.equals("borderThickness"))
			{
				_propList.put("borderWidth", _value);
			}
			else if(_name.equals("borderWeight"))
			{
				_propList.put("borderWidth", _value);
			}else if( _name.equals("formatter") ){
				_formatter = _value.toString();
				_propList.put("formatter", _formatter);
			}else if( _name.equals("systemFunction") ){
				_systemFunction = _value.toString();
			}else if( _name.equals("editItemFormatter") )
			{
				_formatterE = _value.toString();
				_propList.put("editItemFormatter", _formatterE);
			}
			else if(_name.equals("dataType"))
			{
				_dataType = _value.toString();
				_propList.put(_name, _value);
			}
			
			else if( _name.equals("data") )
			{
				if(_className.equals("UBImage") || _className.equals("UBSignature") || _className.equals("UBTextSignature") || _className.equals("UBPicture"))
				{
					/*
					String projName = m_appParams.getREQ_INFO().getPROJECT_NAME();
					String formName = m_appParams.getREQ_INFO().getFORM_ID();
					
					String	_url = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getImageData&PROJECT_NAME=" + projName + "&FORM_ID=" + formName + "&ITEM_ID="+_itemId;
					_propList.put("src", _url);
					
					if(!mImageData.containsKey(_itemId))
					{
						this.mImageData.put(_itemId, _value.toString());
					}
					*/
					
//					String _stvalue = URLDecoder.decode(_value.toString(), "UTF-8");
//					_propList.put("src",  URLEncoder.encode(_stvalue, "UTF-8"));
					_propList.put("src",  URLEncoder.encode(_value.toString(), "UTF-8"));
				}
			}
			else if( _name.equals("prefix") ){
				try {
					_prefix=URLDecoder.decode(_value.toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			else if( _name.equals("suffix") ){
				//_suffix=URLDecoder.decode(_value, "UTF-8");
				_suffix=_value.toString();
			}
			else if( _name.equals("leftBorderType") ||  _name.equals("rightBorderType") ||  _name.equals("topBorderType") ||  _name.equals("bottomBorderType") )
			{
				_propList.put(_name, _value);
			}
			// 명우 추가 
			else if(_name.equals("startPoint"))
			{

				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				String[] _sPoint = _value.toString().split(",");
				_propList.put("x1", Float.valueOf(_sPoint[0]));
				_propList.put("y1", Float.valueOf(_sPoint[1]));
				
			}
			else if(_name.equals("endPoint"))
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				String[] _ePoint = _value.toString().split(",");

				_propList.put("x2", Float.valueOf(_ePoint[0]));
				_propList.put("y2", Float.valueOf(_ePoint[1]));
				
			}
			else if( _name.equals("width"))
			{
				//if( !_propList.containsKey("x2") ) _propList.put("x2", _value);
				_propList.put(_name, _value);
			}
			else if( _name.equals("height"))
			{
				//if( !_propList.containsKey("y2") )_propList.put("y2", _value);
				_propList.put(_name, _value);
			}
			else if(_name.equals("lineThickness"))
			{
				_propList.put("thickness", _value);
			}
			else if( _name.equals("rWidth") ||  _name.equals("rHeight") )
			{
				_propList.put(_name, _value);
			}
			else if( _name.equals("printVisible") )
			{
				if( ("PRINT".equals(isExportType) || "PDF".equals(isExportType)) && "false".equals(_value) )
				{
					return null;
				}
			}
			else if( _name.equals("markanyVisible") )
			{
				if( _isMarkAny && "PRINT".equals(isExportType) && "false".equals(_value) )
				{
					return null;
				}
			}
			else if( _name.equals("rotation") )
			{
				_propList.put(_name, _value);
				_propList.put("rotate", _value);
			}
		}
		
		if( _className.toUpperCase().indexOf("LINE") == -1)
		{
			_propList.put("x1", _propList.get("x"));
			_propList.put("y1", _propList.get("y"));
			_propList.put("x2", _propList.get("width"));
			_propList.put("y2", _propList.get("height"));
		}
		
		// Item의 changeData가 있는지 확인
		if(mChangeItemList != null )
		{
			String _chkID = _itemId + "_"+ _bandInfo.getId() + "_ROW"+rowIndex;
			
			_propList = convertChangeItemDataText( _currentPage ,_propList, _chkID );
		}
		
		// 보더업데이트
		if( _propList.containsKey("isCell") && _propList.get("isCell").toString().equals("false") )
		{
			_propList = convertItemToBorder(_propList);
		}
		
		if(currentItemData.containsKey("ORIGINAL_TABLE_ID"))
		{
			_propList.put("ORIGINAL_TABLE_ID", currentItemData.get("ORIGINAL_TABLE_ID").getStringValue() );
		}
		
		if(currentItemData.containsKey("beforeBorderType"))
		{
			_propList.put("beforeBorderType", currentItemData.get("beforeBorderType").getArrayBooleanValue());
		}
		
		if(currentItemData.containsKey("PRESERVE_ASPECT_RATIO"))
		{
			_propList.put("PRESERVE_ASPECT_RATIO", currentItemData.get("PRESERVE_ASPECT_RATIO").getStringValue());
		}
		
		/**
		if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ){
			String _url="";
			String _txt = "";
			String	_servicesUrl = "";
			
			if( _dataType.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list;
				
				if( dataSet.equals("") )
				{
					_list = DataSet.get(currentItemData.get("dataSet").getStringValue());
				}
				else
				{
					_list = DataSet.get( dataSet );
				}
				Object _dataValue = "";
				if( rowIndex < _list.size() )
				{
					HashMap<String, Object> _dataHm = _list.get(rowIndex);
					
					_dataValue = _dataHm.get( currentItemData.get("column").getStringValue() );
				}
				
				if( _dataValue != null ){
					_txt = _dataValue.toString();
				}
				
				_servicesUrl = convertImageData(_txt, _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("width").toString()).intValue() );
				
				_propList.put("src", _servicesUrl);
			}
			else if( _dataType.equals("2") && _propList.containsKey("src") && _propList.get("src") != null )
			{
				_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+ _propList.get("src").toString();
				_propList.put("src", _servicesUrl);
			}
			
			else if( _dataType.equals("3"))
			{
				_txt = _propList.get("text").toString();
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					if( _inOf != 0 ){
						String _fnValue = mFunction.function(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
						_propList.put("text", _fnValue);
					}else{

						int _keyIndex=_txt.lastIndexOf("}");
						_pKey = _txt.substring(_inOf + 7 , _keyIndex);

						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_txt = "";
						}
						else
						{
							_txt = _pValue;
						}
					}
				}
				else
				{
					_txt = "";
				}
				
				_servicesUrl = convertImageData(_txt, _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("width").toString()).intValue() );
				
				_propList.put("src", _servicesUrl);
			}
		}
		*/
		
		// kyh formatter property setting
		Element ItemElement = currentItemData.get("Element").getElementValue();
		NodeList _formatterNode = ItemElement.getElementsByTagName("formatter");
		Element _formatterItem = (Element)_formatterNode.item(0);
		
		if( _formatterItem != null )
		{
			NodeList _formatterItemPropertyList = _formatterItem.getElementsByTagName("property");
			for( int _formatterIndex=0;  _formatterIndex < _formatterItemPropertyList.getLength(); _formatterIndex++ ){
				Element _formatterItemProperty = (Element)_formatterItemPropertyList.item(_formatterIndex);
				String _formatPropertyName = _formatterItemProperty.getAttribute("name");
				String _formatPropertyValue = _formatterItemProperty.getAttribute("value");
				try {
					_formatPropertyValue =URLDecoder.decode(_formatPropertyValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(_formatPropertyName.equals("nation"))
				{
					_nation = _formatPropertyValue;
				}
				else if(_formatPropertyName.equals("align"))
				{
					_align=_formatPropertyValue;
				}
				else if( _formatPropertyName.equals("mask") ){
					_mask = _formatPropertyValue;
				}
				
				else if( _formatPropertyName.equals("decimalPointLength") ){
					if(  _formatPropertyValue.equalsIgnoreCase("NaN") ){
						_decimalPointLength = 0;
					}else{
						_decimalPointLength = common.ParseIntNullChk(_formatPropertyValue, 0);	
					}
				}				
				
				else if( _formatPropertyName.equals("useThousandComma") ){
					_useThousandComma = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("isDecimal") ){
					_isDecimal = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("formatString") ){
					_formatString = _formatPropertyValue;
				}
				else if( _formatPropertyName.equals("inputFormatString") )
				{
					_inputForamtString = URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
				else if( _formatPropertyName.equals("outputFormatString") )
				{
					_outputFormatString =  URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
			}
		}
		
		// 에디트 포맷터 값이존재할경우각각의 데이터를 담는 처리
		
		// kyh formatter property setting
		Element ItemElementE = currentItemData.get("Element").getElementValue();
		NodeList editFormatterItem = ItemElement.getElementsByTagName("editItemFormatter");
		
		if( editFormatterItem != null && editFormatterItem.getLength() > 0 )
		{	
			String _eformatDataset=null;
			String _eformatKeyField=null;
			String _eformatLabelField=null;
			Element _propItem;
			
			NodeList formatterProperty = ((Element) editFormatterItem.item(0)).getElementsByTagName("property");
			int propertySize = formatterProperty.getLength();
			for (int i = 0; i < propertySize; i++) {
				
				_propItem = (Element) formatterProperty.item(i);
				
				String _propName = _propItem.getAttribute("name");
				String _propValue = _propItem.getAttribute("value");
				
				if( _propName.equals("nation") ){
					_nationE = URLDecoder.decode(_propValue, "UTF-8");
					_propList.put("eformnation", 	_nationE );
				}
				
				else if( _propName.equals("mask") ){
					_maskE = URLDecoder.decode(_propValue, "UTF-8");
					_propList.put("eformmask", 	_maskE );
				}
				else if( _propName.equals("decimalPointLength") ){
					_decimalPointLengthE = common.ParseIntNullChk(_propValue, 0);
					
					_propList.put("eformdecimalPointLength", 	_decimalPointLengthE );
					
				}				
				else if( _propName.equals("useThousandComma") ){
					_useThousandCommaE = Boolean.parseBoolean(_propValue);
					
					_propList.put("eformuseThousandComma", 	_useThousandCommaE );
				}		
				else if( _propName.equals("isDecimal") ){
					_isDecimalE = Boolean.parseBoolean(_propValue);
					_propList.put("eformisDecimal", _isDecimalE	 );
				}		
				else if( _propName.equals("formatString") ){
					_formatStringE = _propValue;
					_propList.put("eformformatString", _formatStringE	 );
				}else if( _propName.equals("dataProvider") ){
					_propValue = URLDecoder.decode(_propValue, "UTF-8");
					_propList.put("eformDataProvider", _propValue	 );
				}else if( _propName.equals("dataset") ){
					_eformatDataset = _propValue;
				}else if( _propName.equals("keyField") ){
					_eformatKeyField = _propValue;
				}else if( _propName.equals("valueField") ){
					_eformatLabelField = _propValue;
				}
			}
			
			if( _formatterE.equals("SelectMenu") ){
				
				// dataset으로 comboBox 표현.
				if( _eformatDataset != null &&  (!_eformatDataset.equals("null")) && DataSet.containsKey(_eformatDataset) ){
					
					String _efText = _propList.get("text").toString();
					Boolean _hasValueKey = false;
					
					ArrayList<HashMap<String, Object>> _list = DataSet.get(_eformatDataset);
					
					HashMap<String, Object> _dataHm;
					Object _keyData;
					Object _labelData;
					
					JSONArray ja = new JSONArray();
					String _jsonStr=null;
					JSONObject jo;
					String _keyStr=null;
					String _labelStr=null;
					
					for( int _eformatIdx=0; _eformatIdx<_list.size(); _eformatIdx++ ){
						_dataHm = _list.get(_eformatIdx);
						_keyData = _dataHm.get(_eformatKeyField);
						_labelData = _dataHm.get(_eformatLabelField);
						_keyStr=_keyData.toString();
						_labelStr = _labelData.toString();
						
						if( _efText.equals(_keyStr) && _hasValueKey == false ){
							_hasValueKey = true;
							_propList.put("text", _labelStr );
						}
						
						jo = new JSONObject();
						jo.put("label", _labelStr);
						jo.put("value",_keyStr );
						ja.add(jo);
					}
					
					_jsonStr = ja.toJSONString();
					
					_propList.put("eformDataProvider", _jsonStr	 );
				}else{
					
					if( _propList.containsKey("eformDataProvider") == false && _propList.get("eformDataProvider") == null )
					{
						_propList.put("eformDataProvider", "[]");
					}
					
					String _jsonStr = _propList.get("eformDataProvider").toString(); 
					JSONParser jsonParser = new JSONParser();
					try {
						
						JSONArray ja = (JSONArray) jsonParser.parse(_jsonStr);
						
						String _efText = _propList.get("text").toString();
						JSONObject oj;
						for( int jsonIdx=0; jsonIdx<ja.size(); jsonIdx++ ){
							oj=(JSONObject) ja.get(jsonIdx);
							if( oj.get("value").equals(_efText) ){
								_propList.put("text", oj.get("label") );
								break;
							}
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}

		
		//Table의 UBFX가 존재할경우 처리( Table의 ubfx를 먼저 처리후 Cell의 ubfx를 처리 )
		NodeList _ubfunction = ItemElement.getElementsByTagName("ubfunction"); 
		
		ArrayList<NodeList> _ubfxNodes = new ArrayList<NodeList>();
		int _nodeCnts = 0;
		
		if( currentItemData.get("tableUbfunction") != null && currentItemData.get("tableUbfunction").getNodeListValue().getLength() > 0 )
		{
			_ubfxNodes.add(currentItemData.get("tableUbfunction").getNodeListValue());
		}
		_ubfxNodes.add(_ubfunction);
		
		_nodeCnts = _ubfxNodes.size();
		
		for(int _ubfxListIndex= 0; _ubfxListIndex < _nodeCnts; _ubfxListIndex++)
		{
			NodeList _selectNodeList = _ubfxNodes.get(_ubfxListIndex);
			for(int _ubfxIndex = 0; _ubfxIndex < _selectNodeList.getLength(); _ubfxIndex++)
			{
				Element _ubfxItem = (Element) _selectNodeList.item(_ubfxIndex);
				String _ubfxProperty = _ubfxItem.getAttribute("property");
				String _ubfxValue = _ubfxItem.getAttribute("value");
				try {
					_ubfxValue = URLDecoder.decode(_ubfxValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ArrayList<HashMap<String, Object>> _pList = DataSet.get(dataSet);
				String _datasetColumnName = currentItemData.get("column")==null ? "" : currentItemData.get("column").getStringValue();
				mFunction.setDatasetList(DataSet);
				mFunction.setParam(_param);

				mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
				if(mGroupTotalPageIndex>0) mFunction.setSectionCurrentPageNum(mGroupCurrentPageIndex);
				mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
				if(mGroupTotalPageIndex>0) mFunction.setSectionTotalPageNum(mGroupTotalPageIndex);
				mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
				mFunction.setOriginalDataMap(mOriginalDataMap);
				
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,dataSet);
				}else{
					_fnValue = mFunction.function(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,dataSet);
				}
				
				
				_fnValue = _fnValue.trim();
				
				if(_ubfxProperty.equals("text") || _fnValue.equals("") == false)
				{
					_propList = convertUbfxStyle(_ubfxProperty, _fnValue, _propList );
				}
			}
		}
		
		_ubfxNodes = null;
		
		if( _className.equals("UBSVGRichText") && _bandInfo.getAdjustableHeightListAr().size() > 0 )
		{
			if( _bandInfo.getAdjustableHeightListAr().size() > rowIndex )
			{
				_propList.put("height", _bandInfo.getAdjustableHeightListAr().get(rowIndex) - currentItemData.get("band_y").getIntegerValue() );
			}
		}
		
		//hyperLinkedParam처리
		if( _propList.containsKey("ubHyperLinkType") && "2".equals( _propList.get("ubHyperLinkType") )  )
		{
			NodeList _hyperLinkedParam = ItemElement.getElementsByTagName("ubHyperLinkParm");
			if( _hyperLinkedParam != null && _hyperLinkedParam.getLength() > 0 )
			{
				Element _hyperLinkEl = (Element) _hyperLinkedParam.item(0);
				NodeList _hyperLinkedParams = _hyperLinkEl.getElementsByTagName("param");
				int _hyperLinkedParamSize = _hyperLinkedParams.getLength();
				
				HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
				
				for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
				{
					Element _hyperParam = (Element) _hyperLinkedParams.item(_hyperIdx);
					NodeList _hyperPropertys = _hyperParam.getElementsByTagName("property");
					int _hyperPropertysSize = _hyperPropertys.getLength();
					String _hyperParamKey = "";
					String _hyperParamValue = "";
					String _hyperParamType = "";
					
					for (int _hyperProIdx = 0; _hyperProIdx <  _hyperPropertysSize; _hyperProIdx++) 
					{
						Element _hyperProperty = (Element) _hyperPropertys.item(_hyperProIdx);
						if( "id".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamKey = _hyperProperty.getAttribute("value").toString();
						}
						else if( "value".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamValue = _hyperProperty.getAttribute("value").toString();
						}
						else if( "type".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamType = _hyperProperty.getAttribute("value").toString();
						}
					}
					
					if( "DataSet".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						
						_hyperParamValue = "";
						
						if( dataSet != null && dataSet.equals("") == false && mOriginalDataMap != null && mOriginalDataMap.get(dataSet).equals(_hyperLinkedDataSetId) )
						{
							_hyperLinkedDataSetId = dataSet;
						}
						
						if(DataSet.containsKey(_hyperLinkedDataSetId))
						{
							ArrayList<HashMap<String, Object>> _list = DataSet.get( _hyperLinkedDataSetId );
							Object _dataValue = "";
							if( _list != null ){
								if( rowIndex < _list.size() )
								{
									HashMap<String, Object> _dataHm = _list.get(rowIndex);
									_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
								}
							}
						}
					}
					else if("Parameter".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_hyperParamValue = "";
						}
						else
						{
							_hyperParamValue = _pValue;
						}
					}
					
					_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
				}
				
				_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
			}
			
		}
		
		
		if( currentItemData.containsKey("dataType") && currentItemData.containsKey("dataSet")  )	
//		if( currentItemData.containsKey("dataType") && currentItemData.containsKey("dataSet") && !_dataID.equals("") )	// _dataID를 비교하면 함수로 dataset 처리하는 경우 대응못함.	
		{
			String columnStr =  currentItemData.get("column").getStringValue();
			String dataTypeStr = currentItemData.get("dataType").getStringValue();
			
			if( currentItemData.containsKey("dataType_N") && currentItemData.get("dataType_N").getStringValue().equals("") == false )
			{
				dataTypeStr = currentItemData.get("dataType_N").getStringValue();
			}
			if( currentItemData.containsKey("column_N") && currentItemData.get("column_N").getStringValue().equals("") == false )
			{
				columnStr = currentItemData.get("column_N").getStringValue();
			}
			
			if( currentItemData.containsKey("currentAdjustableHeight") && currentItemData.get("currentAdjustableHeight").getIntegerValue() != -1 )
			{
				_propList.put("currentAdjustableHeight", currentItemData.get("currentAdjustableHeight").getIntegerValue());
			}
			
			if( dataTypeStr.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list = DataSet.get( _dataID );
				Object _dataValue = "";
				if( _list != null ){
					if( rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						_dataValue = _dataHm.get( columnStr );
					}
				}
				
				// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐.. 
				if(_className.equals("UBSVGArea") ){
					
					if(_dataValue==null || _dataValue.toString().equals("")) return null;
					
					String _tmpDataValue = _dataValue.toString();
					if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
					{
						_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
					}
					
					boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
					
					boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
					boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
					
					if(_bSVG)
					{
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
					}
					else
					{
						
						boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
						if(!_bHasHtmlTag)
							_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
							
						_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							
					}
					log.debug("1078-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _dataValue.toString() + "]");
					
					_dataValue = _dataValue.toString().replace(" ", "%20");
					
					if( !_dataValue.toString().equals("") )
					{
						_propList.put("data",  URLEncoder.encode(_dataValue.toString(), "UTF-8"));
					}
					else
					{
						return null;
					}
					
					
				}
				else if( _className.equals("UBSVGRichText") )
				{
					// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
					if( _dataValue != null && _dataValue.toString().equals("") == false )
					{
						_propList = convertUBSvgItem(_dataValue,_propList);
						
						if(_propList == null ) return null;
					}
				}
				
				
				else if("UBCheckBox".equals(_className) )
				{
					String _selectedText=_propList.get("selectedText").toString();
					//String _deSelectedText=_propList.get("deSelectedText").toString();
					if( _dataValue != null && _selectedText.equalsIgnoreCase(_dataValue.toString()) ){
						_propList.put("selected", "true");
					}else{
						_propList.put("selected", "false");	
					}
					
				}	
				else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") )
				{
					String _servicesUrl = convertImageData( _dataValue.toString() , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
					_propList.put("src", _servicesUrl);
				}
				else
				{
					_propList.put("text", _dataValue == null ? "" : _dataValue);
					
					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					_propList.put("tooltip", _propList.get("text"));
				}
				
			}
			else if( dataTypeStr.equals("2"))
			{
				// rowIndex : 현재 Row Index
				// dataSet : 그룹핑된 데이터셋
				boolean _chkRowIndex = true;
				if( DataSet.containsKey(_dataID))
				{
					// 함수 조회시 데이터 밴드의 Row수만큼만 만큼만 처리 하도록 변경 ( 데이터셋의 Row 수 만큼만 처리 하도록 하였으나 특정 함수의 경우 모든 Row에 필요 )
//					if( rowIndex > 0 && rowIndex >= DataSet.get( _dataID ).size() )
					if( rowIndex > 0 && rowIndex >= _bandInfo.getRowCount() )
					{
						_chkRowIndex = false;
						
						/** RowIndex와 같은 특정 함수는 모든 Row를 반복하도록 처리가 필요.*/
						if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") )
						{
							for (int _si = 0; _si < GlobalVariableData.GLOBAL_FUNCTION_LIST.length; _si++) {
								if( _systemFunction.indexOf( GlobalVariableData.GLOBAL_FUNCTION_LIST[_si]+"(" ) != -1 )
								{
									_chkRowIndex = true;
									break;
								}
							}
						}
						
					}
				}
				
				if( _chkRowIndex )
				{
					if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
						
						ArrayList<HashMap<String, Object>> _pList = DataSet.get( dataSet );
						String _datasetColumnName = columnStr;
						mFunction.setDatasetList(DataSet);
						mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
						mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
						mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
						mFunction.setOriginalDataMap(mOriginalDataMap);
						
						
						String _fnValue;
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = mFunction.testFN(_systemFunction , rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet);
						}else{
							_fnValue = mFunction.function(_systemFunction,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
						}
						

						if( _className.equals("UBSVGRichText") )
						{
							if( _fnValue != null && _fnValue.equals("") == false )
							{
								// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
								_propList = convertUBSvgItem(_fnValue,_propList);
								
								if(_propList == null ) return null;
							}

						}
						else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
							//_fnValue = URLDecoder.decode(_fnValue, "UTF-8");
							
							String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
							_propList.put("src", _servicesUrl);
							
						}
						
						else if(_className.equals("UBCheckBox") ) {
							_propList.put("selected", _fnValue);
						}
						else
						{
							_propList.put("text", _fnValue == null ? "" : _fnValue);
						}
						
					}
				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			else if( dataTypeStr.equals("3"))
			{
				String _txt = _propList.get("text").toString();
				
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				String _fnValue = "";
				
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					if( _inOf != 0 ){
						
						// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
						if(_className.equals("UBSVGArea")  ){
							
							String _tmpDataValue = String.valueOf(_txt);
							if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
							{
								_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
							}
							
							boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
							boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
							boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
							
							String _svgTag = null;
							if(_bSVG)
							{
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
							}
							else
							{
								boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
								if(!_bHasHtmlTag)
									_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
									
								_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
									
							}
													
							log.debug("1258-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _svgTag + "]");
							
							_svgTag = _svgTag.toString().replace(" ", "%20");
							if( !_svgTag.equals("") )
							{
								_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8"));
							}
							else
							{
								return null;
							}
							_txt = "";
						}
						else if( _className.equals("UBSVGRichText") )
						{
							if( _txt != null && _txt.equals("") == false )
							{
//								_propList = convertUBSvgItem( _propList.get("text").toString(), _propList);
								_propList = convertUBSvgItem( _txt, _propList);
								
								if(_propList == null ) return null;
							}
							_txt = "";
						}
						
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							//_fnValue = mFunction.testFN(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
							_fnValue = _txt;
						}else{
							_fnValue = mFunction.function(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
						}
						
					}else{

						int _keyIndex=_txt.lastIndexOf("}");
						_pKey = _txt.substring(_inOf + 7 , _keyIndex);
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						_fnValue = _pList.get("parameter");

					}
					
					if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
//						_fnValue = URLDecoder.decode(_fnValue, "UTF-8");
						
						String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
						_propList.put("src", _servicesUrl);
					}
					else
					{
						if( _fnValue.equals("undefined"))
						{
							_propList.put("text", "");
						}
						else
						{
							_propList.put("text", _fnValue);
						}
					}

				}
				else
				{
					_propList.put("text", "");
				}
			}
			
		}
		
		// formatter set	// ITem의 Formatter 처리 ( export처리를 위하여 속성값 담기 )
		if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
			Object _propValue;
			String _formatValue="";
			_propValue=_propList.get("text");
			_formatValue = _propValue.toString();
			String _excelFormatterStr = "";
			try {
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					_excelFormatterStr = _nation  + "§" + _align;
					
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					_excelFormatterStr = _formatString;
					
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					_excelFormatterStr = _mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal;
					
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
                else if( _formatter.equalsIgnoreCase("CustomDate") )
				{
					_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
					_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
					
					_propList.put("inputFormatString", _inputForamtString);
					_propList.put("outputFormatString", _outputFormatString);
				}
				
			} catch (ParseException e) {
				//e.printStackTrace();
			}
			
			if( isExportType.equals("EXCEL") && _excelFormatterStr.equals("") == false && common.getPropertyValue("excelExport.useFormatter") != null && common.getPropertyValue("excelExport.useFormatter").equals("true") ) 
			{
				_propList.put("EX_FORMATTER", _formatter);
				_propList.put("EX_FORMAT_DATA_STR", _excelFormatterStr);
				_propList.put("EX_FORMAT_ORIGINAL_STR", _propValue.toString() );
			}
			
			_propList.put("text", _formatValue);
			
			// format이 label band에서 안들어간다.
			_propList.put("formatter", _formatter);
			_propList.put("mask", _mask);
			_propList.put("decimalPointLength", _decimalPointLength);
			_propList.put("useThousandComma", _useThousandComma);
			_propList.put("isDecimal", _isDecimal);
			_propList.put("formatString", _formatString);
			_propList.put("nation", _nation);
			_propList.put("currencyAlign", _align);
			_propList.put("inputFormatString", _inputForamtString);
			_propList.put("outputFormatString", _outputFormatString);
			//
			
		}
		
		
		//ResizeFont 값이 true이고 adjustableHeight값이 true 일경우 처리 
		if( _propList.containsKey("text") &&  "".equals(_propList.get("text").toString()) == false && currentItemData.containsKey("resizeFont") && currentItemData.get("resizeFont").getBooleanValue() )
		{
			if( _bandInfo.getResizeFontData().size() > rowIndex )
			{
				_propList.put("fontSize", _bandInfo.getResizeFontData().get(rowIndex).get( currentItemData.get("id").getStringValue() ));
			}
			else
			{
				float _fontSize 	= Float.valueOf( _propList.get("fontSize").toString() );
				String _fontFamily 	= _propList.get("fontFamily").toString();
				String _fontWeight 	= _propList.get("fontWeight").toString();
				float _padding = (_propList.containsKey("padding"))? Float.valueOf( _propList.get("padding").toString()):3;
				
				float _maxBorderSize = 0;
				if(_propList.containsKey("borderWidths"))
				{
					ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
					
					for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
						if(_maxBorderSize < _borderWidths.get(_bIndex))
						{
							_maxBorderSize = _borderWidths.get(_bIndex);
						}
					}
					_padding = _maxBorderSize + _padding;
				}
				
				float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
				
				
				_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
				_propList.put("fontSize",  _fontSize);
			}
		}
		
		
		if( _itemId.equals("") == false )
		{
//			_itemId = _itemId + "_"+ _bandInfo.getId() + "_ROW"+rowIndex;
			_propList.put("TABINDEX_ID", _itemId + "_"+ _bandInfo.getId() + "_ROW"+rowIndex);
			_propList.put("SUFFIX_ID", "_"+ _bandInfo.getId() + "_ROW"+rowIndex);
			
//			if(_className.equals("UBRadioBorder")){
//				String _groupName= _propList.get("groupName").toString();
//				_propList.put("groupName", _groupName + "_"+ _bandInfo.getId() + "_ROW"+rowIndex);
//			}
			
		}
		
		
		if(_propList.containsKey("text"))
			_propList.put("tooltip", _propList.get("text").toString());
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _propList.get("text").toString());
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//_barcodeValue = URLDecoder.decode(_barcodeValue, "UTF-8");
				
				if( _barcodeValue == null || _barcodeValue.equals("")) return null;
				else _propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 8 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("upc") && _barcodeData.length() != 11 ){
					_barcodeSrc="";
				}
				/*
				else if( _barcode_type.equalsIgnoreCase("itf14") && _barcodeData.length() != 14 ){
					_barcodeSrc="";
				}
				*/
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				//_propList.put("src" , _barcodeSrc );
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//_barcodeValue = URLDecoder.decode(_barcodeValue, "UTF-8");
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") || _className.equals("UBTaximeter")|| _className.equals("UBCandleStickChart")|| _className.equals("UBPlotChart") || _className.equals("UBRadarChart") )
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			
			/*
			String _seriesXField = null;
			String[] _yFieldName = null;
			String[] _yFieldDisplayName = null;
			boolean _crossTab = false; 
			String _form = "segment";
			boolean _gridLIne = true; 
			int _gridLineWeight = 1;
			String _gridLIneDirection = "both";
			int _gridLIneColor = 0xd8d3d3;
			String _legendDirection = "vertical";
			String _legendLabelPlacement = "right";
			int _legendMarkHeight = 10;
			int _legendMarkWeight = 10;
			String _legendLocation = "bottom";
			String _dataLabelPosition = "inside";
			boolean _DubplicateAllow = false; 
			int [] _yFieldFillColor = null;
			String _seriexCloseField = null;
			String _seriesHighField = null;
			String _seriesLowField = null;
			String _seriesOpenField = null;
			*/
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			
			if(_className.equals("UBTaximeter")){
				PARAM = getChartParamToElement4Taximeter(currentItemData.get("Element").getElementValue() );	
				PARAM+=","+rowIndex;
			}else if(_className.equals("UBLineChart")){
				PARAM = getLineChartParamToElement( currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBRadarChart")){
				PARAM = getChartParamToElement4Radar(currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBCandleStickChart")){
				PARAM = getCandleChartParamToElement(currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBColumnChart")){
				PARAM = getColumnChartParamToElement(currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBBarChart")){
				PARAM = getColumnChartParamToElement(currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBPieChart")){
				PARAM = getPieChartParamToElement(currentItemData.get("Element").getElementValue() );
			}else{
				PARAM = getChartParamToElement(currentItemData.get("Element").getElementValue() );
			}
			
			
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToElement2(currentItemData.get("Element").getElementValue() );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			else if(_className.equals("UBTaximeter"))
			{
				_propList.put("type" , "TaximeterCtl");
				IMG_TYPE = "taximeter";
			}
			else if(_className.equals("UBRadarChart"))
			{
				_propList.put("type" , "radarChartCtl");
				IMG_TYPE = "radar";
			}
			else if(_className.equals("UBCandleStickChart"))
			{
				_propList.put("type" , "candleChartCtl");
				IMG_TYPE = "candle";
			}
			else if(_className.equals("UBPlotChart"))
			{
				_propList.put("type" , "plotChartCtl");
				IMG_TYPE = "plot";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				String _dataIDs = currentItemData.get("dataSets").getStringValue();				
				String [] arrDataId = _dataIDs.split(",");
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = DataSet.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _model_type + "&PARAM=" + PARAM + "&FILE_NAME=" + this.mChartDataFileName + "&PROJECT_NAME=" + PROJECT_NAME + "&FORM_ID=" + FOLDER_NAME + "&DATASET=" + _dataID );
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			{			
				ArrayList<HashMap<String, Object>> _list = DataSet.get( _dataID );		
				/*
				if(!mChartData.containsKey(_itemId) && _list != null && _list.size() > 0)
				{
					mChartData.put(_dataID, _list);
				}
				*/		
						
				//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _model_type + "&PARAM=" + PARAM + "&FILE_NAME=" + this.mChartDataFileName + "&PROJECT_NAME=" + PROJECT_NAME + "&FORM_ID=" + FOLDER_NAME + "&DATASET=" + _dataID );
				
				if(!"".equals(IMG_TYPE) && _list != null )
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			//_barcodeValue = URLDecoder.decode(_chartValue, "UTF-8");
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		else if( "UBStretchLabel".equals(_className) )
		{
			// StretchLabel일때 height계산하여 height를 업데이트하고 
			// text를 줄바꿈 처리하고 진행
			_propList = convertStrechLabel(_propList);
			
		}
		
		// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
		if(_propList.containsKey("text"))
		{
			String _svalue = _propList.get("text").toString();
			_propList.put("tooltip", _svalue);
		}
		
		
		if("UBGraphicsRectangle".equals(_className) || "UBGraphicsCircle".equals(_className) || "UBGraphicsGradiantRectangle".equals(_className) )
		{
			_propList.put("angle" , _propList.get("rotation") );
			_propList.put("stroke" , _propList.get("borderColor").toString() );
			_propList.put("strokeWidth" , Integer.valueOf( _propList.get("borderThickness").toString() ) );
			_propList.put("scaleX" , 1);
			_propList.put("scaleY" , 1);
			
			_propList.put("width", Float.valueOf(_propList.get("width").toString()));
			_propList.put("height", Float.valueOf(_propList.get("height").toString()));
			
			if( "UBGraphicsCircle".equals(_className) )
			{
				_propList.put("radius", Float.valueOf( Float.valueOf(_propList.get("width").toString())/2 ));
				_propList.put("scaleY", Float.valueOf( Float.valueOf(_propList.get("height").toString())/Float.valueOf(_propList.get("width").toString()) ));
			}
		}
		
		//crossTab을 table로 내보내기 테스트
		if( currentItemData.containsKey("isTable"))
		{
			_propList.put("isTable", currentItemData.get("isTable").getStringValue());
		}
		if( currentItemData.containsKey("TABLE_ID"))
		{
			_propList.put("TABLE_ID", currentItemData.get("TABLE_ID").getStringValue());
		}
		
		if("UBRotateLabel".equals(_className))
		{
			_propList.put("rotate" , _propList.get("rotation") );
		}
		
		if(_propList.containsKey("visible") && _propList.get("visible").equals("false"))
		{
			return null;
		}
		
		_propList.put("className" , _className );
		_propList.put("id" , _itemId );
		
		
		// radioButtonGroup
		if( _className.equals("UBRadioBorder") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			String _groupName= _propList.get("groupName").toString();
			_propList.put("groupName", _groupName +_propList.get("SUFFIX_ID") );
			
			Boolean _isSelected = radiobuttonHandler(_propList);
			_propList.put("selected", _isSelected);
		}else if( _className.equals("UBRadioButtonGroup") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			
			radiobuttonGroupHandler(_propList);
		}
		
		// 아이템의 사용 여부 확인 
		_propList = ItemPropertyProcess.checkedItemProperties(_propList);
		
		
		return _propList;
	}
	
	
	private static ArrayList<String> getChartPropertys()
	{
		String chartPropertyStr = "seriesXField,yFieldName,yFieldDisplayName,isCrossTabData,form,gridLine,gridLineWeight,gridLIneDirection,gridLIneColor," +
				"legendDirection,legendLabelPlacement,legendMarkHeight,legendMarkWidthlegendLocation,dataLabelPostion," +
				"isCrossTabData,isDuplication,yFieldFillColor,closeField,highField,lowField,openField,colorFieldName,legendLocation,seriesXFieldFontSize,seriesYFieldFontSize,rotateCategoryLabel,rangeMax," +
				"xFieldVisible,yFieldVisible,backgroundColor";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}
	
	private static ArrayList<String> getPieChartPropertys()
	{
		String chartPropertyStr = "seriesXField,yFieldName,yFieldDisplayName,isCrossTabData,form,gridLine,gridLineWeight,gridLIneDirection,gridLIneColor," +
				"legendDirection,legendLabelPlacement,legendMarkHeight,legendMarkWidthlegendLocation,dataLabelPostion," +
				"isCrossTabData,isDuplication,yFieldFillColor,closeField,highField,lowField,openField,colorFieldName,legendLocation,seriesXFieldFontSize,seriesYFieldFontSize,rotateCategoryLabel,showLabelType," +
				"backgroundColor,outLineWeight";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}
	
	
	private static ArrayList<String> getColumnChartPropertys()
	{
		String chartPropertyStr = "seriesXField,yFieldName,yFieldDisplayName,isCrossTabData,form,gridLine,gridLineWeight,gridLIneDirection,gridLIneColor," +
				"legendDirection,legendLabelPlacement,legendMarkHeight,legendMarkWidthlegendLocation,dataLabelPostion," +
				"isCrossTabData,isDuplication,yFieldFillColor,closeField,highField,lowField,openField,colorFieldName,legendLocation,seriesXFieldFontSize,seriesYFieldFontSize,rotateCategoryLabel,"+
				"xLineWeight,yLineWeight,outLineWeight,rangeMax,xFieldVisible,yFieldVisible,backgroundColor,categoryMargin,itemMargin";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}
	
	private static ArrayList<String> getCandleChartPropertys()
	{
		String chartPropertyStr = "seriesXField,yFieldName,yFieldDisplayName,isCrossTabData,form,gridLine,gridLineWeight,gridLIneDirection,gridLIneColor," +
				"legendDirection,legendLabelPlacement,legendMarkHeight,legendMarkWidthlegendLocation,dataLabelPostion," +
				"isCrossTabData,isDuplication,yFieldFillColor,closeField,highField,lowField,openField,colorFieldName,legendLocation,seriesXFieldFontSize,seriesYFieldFontSize,rotateCategoryLabel," +
				"candleWidth,upColor,downColor,rangeMax,xFieldVisible,yFieldVisible,backgroundColor";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}
	
	private static ArrayList<String> getLineChartPropertys()
	{
		String chartPropertyStr = "seriesXField,yFieldName,yFieldDisplayName,isCrossTabData,form,gridLine,gridLineWeight,gridLIneDirection,gridLIneColor," +
				"legendDirection,legendLabelPlacement,legendMarkHeight,legendMarkWidthlegendLocation,dataLabelPostion," +
				"isCrossTabData,isDuplication,yFieldFillColor,closeField,highField,lowField,openField,colorFieldName,legendLocation,seriesXFieldFontSize,seriesYFieldFontSize,rotateCategoryLabel,"+
				"xLineWeight,yLineWeight,outLineWeight,valueWeight,valueDisplayType,rangeMax,noDataType,xFieldVisible,yFieldVisible,backgroundColor";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}

	
	private static ArrayList<String> getChartPropertys2()
	{
		String chartPropertyStr = "seriesXFields,yFieldName,yFieldDisplayName,isCrossTabData,form,gridLine,gridLineWeight,gridLIneDirection,gridLIneColor," +
				"legendDirection,legendLabelPlacement,legendMarkHeight,legendMarkWidthlegendLocation,dataLabelPostion," +
				"isCrossTabData,isDuplication,yFieldFillColor,closeField,highField,lowField,openField";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}
	
	
	private static ArrayList<String> getChartPropertys4Radar()
	{
		String chartPropertyStr = "title,valueField,legendField,categoryField,interiorGap,titleVisible,legendVisible,seriesColors";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}
	
	
	private static ArrayList<String> getChartPropertys4CombinedColumn()
	{
		String chartPropertyStr = "seriesXField,yFieldName,yFieldDisplayName,isCrossTabData,form,gridLine,gridLineWeight,gridLIneDirection,gridLIneColor," +
				"legendDirection,legendLabelPlacement,legendMarkHeight,legendMarkWidthlegendLocation,dataLabelPostion," +
				"isCrossTabData,isDuplication,yFieldFillColor,closeField,highField,lowField,openField,colorFieldName,legendLocation,seriesXFieldFontSize,seriesYFieldFontSize,rotateCategoryLabel,rangeMax," +
				"xFieldVisible,yFieldVisible,columnAxisName,lineAxisName,backgroundColor,categoryMargin,itemMargin";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}
	
	/**
	 * 
	 **/
	public static String getChartParamToElement( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		ArrayList<String> chartList = getChartPropertys();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		NodeList list;
		NodeList displaylist;
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";

		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		try {
//			list = (NodeList) _xpath.evaluate("./property", _element, XPathConstants.NODESET);
			list = _element.getElementsByTagName("property");
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				
				if( chartList.indexOf( _itemData.getAttribute("name") ) != -1  )
				{
					chartData.put(_itemData.getAttribute("name") , URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8")  );
				}
			}
//			list = (NodeList) _xpath.evaluate("./displayName/column", _element, XPathConstants.NODESET);
			list = ((Element)_element.getElementsByTagName("displayName").item(0)).getElementsByTagName("column");
			_max = list.getLength();
			
			for ( i = 0; i < _max; i++) {
				
				_itemData = (Element) list.item(i);
//				displaylist = (NodeList) _xpath.evaluate("./property", _itemData, XPathConstants.NODESET);
				displaylist = _itemData.getElementsByTagName("property");
				displayMap.clear();
				
				_subMax = displaylist.getLength();
				
				for ( j = 0; j < _subMax; j++) {
					_displayItemData = (Element) displaylist.item(j);
					displayMap.put(_displayItemData.getAttribute("name") , URLDecoder.decode(_displayItemData.getAttribute("value") , "UTF-8") );
				}
				
				if(displayMap.containsKey("visible") && displayMap.get("visible").equals("true") )
				{
					if( yFieldsStr.equals("") == false )
					{
						yFieldsStr = yFieldsStr + "~";
					}
					if( yFieldsDisplayStr.equals("") == false )
					{
						yFieldsDisplayStr = yFieldsDisplayStr + "~";
					}
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr = yFieldsColorStr + "~";
					}
					
					yFieldsStr = yFieldsStr + displayMap.get("column");
					yFieldsDisplayStr = yFieldsDisplayStr + displayMap.get("text");
					yFieldsColorStr = yFieldsColorStr + displayMap.get("color");
				}
				
			}
			
			_max = chartList.size();
//			for ( i = 0; i < chartList.size(); i++) {
			for ( i = 0; i < _max; i++) {
				if( returnString.equals("") == false ) returnString = returnString + ",";
				if( chartList.get(i).equals("yFieldName") )
				{
					returnString = returnString + yFieldsStr;
				}
				else if( chartList.get(i).equals("yFieldDisplayName") )
				{
					returnString = returnString + yFieldsDisplayStr;
				}
				else if( chartList.get(i).equals("yFieldFillColor") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendLabelPlacement") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendMarkHeight") )
				{
					returnString = returnString + _legendMarkHeight;
				}
				else if( chartList.get(i).equals("legendMarkWidth") )
				{
					returnString = returnString + _legendMarkWeight;
				}
				else if( chartData.containsKey(chartList.get(i)))
				{
					returnString = returnString + chartData.get(chartList.get(i));
				}
				else
				{
					returnString = returnString + "";
				}
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnString;
	}
	
	/**
	 * 
	 **/
	public static String getPieChartParamToElement( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		ArrayList<String> chartList = getPieChartPropertys();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		NodeList list;
		NodeList displaylist;
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";

		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		try {
//			list = (NodeList) _xpath.evaluate("./property", _element, XPathConstants.NODESET);
			list = _element.getElementsByTagName("property");
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				
				if( chartList.indexOf( _itemData.getAttribute("name") ) != -1  )
				{
					chartData.put(_itemData.getAttribute("name") , URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8")  );
				}
			}
//			list = (NodeList) _xpath.evaluate("./displayName/column", _element, XPathConstants.NODESET);
			list = ((Element)_element.getElementsByTagName("displayName").item(0)).getElementsByTagName("column");
			_max = list.getLength();
			
			for ( i = 0; i < _max; i++) {
				
				_itemData = (Element) list.item(i);
//				displaylist = (NodeList) _xpath.evaluate("./property", _itemData, XPathConstants.NODESET);
				displaylist = _itemData.getElementsByTagName("property");
				displayMap.clear();
				
				_subMax = displaylist.getLength();
				
				for ( j = 0; j < _subMax; j++) {
					_displayItemData = (Element) displaylist.item(j);
					displayMap.put(_displayItemData.getAttribute("name") , URLDecoder.decode(_displayItemData.getAttribute("value") , "UTF-8") );
				}
				
				if(displayMap.containsKey("visible") && displayMap.get("visible").equals("true") )
				{
					if( yFieldsStr.equals("") == false )
					{
						yFieldsStr = yFieldsStr + "~";
					}
					if( yFieldsDisplayStr.equals("") == false )
					{
						yFieldsDisplayStr = yFieldsDisplayStr + "~";
					}
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr = yFieldsColorStr + "~";
					}
					
					yFieldsStr = yFieldsStr + displayMap.get("column");
					yFieldsDisplayStr = yFieldsDisplayStr + displayMap.get("text");
					yFieldsColorStr = yFieldsColorStr + displayMap.get("color");
				}
				
			}
			
			_max = chartList.size();
//			for ( i = 0; i < chartList.size(); i++) {
			for ( i = 0; i < _max; i++) {
				if( returnString.equals("") == false ) returnString = returnString + ",";
				if( chartList.get(i).equals("yFieldName") )
				{
					returnString = returnString + yFieldsStr;
				}
				else if( chartList.get(i).equals("yFieldDisplayName") )
				{
					returnString = returnString + yFieldsDisplayStr;
				}
				else if( chartList.get(i).equals("yFieldFillColor") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendLabelPlacement") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendMarkHeight") )
				{
					returnString = returnString + _legendMarkHeight;
				}
				else if( chartList.get(i).equals("legendMarkWidth") )
				{
					returnString = returnString + _legendMarkWeight;
				}
				else if( chartData.containsKey(chartList.get(i)))
				{
					returnString = returnString + chartData.get(chartList.get(i));
				}
				else
				{
					returnString = returnString + "";
				}
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnString;
	}
	
	/**
	 * 
	 **/
	public static String getColumnChartParamToElement( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		ArrayList<String> chartList = getColumnChartPropertys();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		NodeList list;
		NodeList displaylist;
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";

		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		try {
//			list = (NodeList) _xpath.evaluate("./property", _element, XPathConstants.NODESET);
			list = _element.getElementsByTagName("property");
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				
				if( chartList.indexOf( _itemData.getAttribute("name") ) != -1  )
				{
					chartData.put(_itemData.getAttribute("name") , URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8")  );
				}
			}
//			list = (NodeList) _xpath.evaluate("./displayName/column", _element, XPathConstants.NODESET);
			list = ((Element)_element.getElementsByTagName("displayName").item(0)).getElementsByTagName("column");
			_max = list.getLength();
			
			for ( i = 0; i < _max; i++) {
				
				_itemData = (Element) list.item(i);
//				displaylist = (NodeList) _xpath.evaluate("./property", _itemData, XPathConstants.NODESET);
				displaylist = _itemData.getElementsByTagName("property");
				displayMap.clear();
				
				_subMax = displaylist.getLength();
				
				for ( j = 0; j < _subMax; j++) {
					_displayItemData = (Element) displaylist.item(j);
					displayMap.put(_displayItemData.getAttribute("name") , URLDecoder.decode(_displayItemData.getAttribute("value") , "UTF-8") );
				}
				
				if(displayMap.containsKey("visible") && displayMap.get("visible").equals("true") )
				{
					if( yFieldsStr.equals("") == false )
					{
						yFieldsStr = yFieldsStr + "~";
					}
					if( yFieldsDisplayStr.equals("") == false )
					{
						yFieldsDisplayStr = yFieldsDisplayStr + "~";
					}
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr = yFieldsColorStr + "~";
					}
					
					yFieldsStr = yFieldsStr + displayMap.get("column");
					yFieldsDisplayStr = yFieldsDisplayStr + displayMap.get("text");
					yFieldsColorStr = yFieldsColorStr + displayMap.get("color");
				}
				
			}
			
			_max = chartList.size();
//			for ( i = 0; i < chartList.size(); i++) {
			for ( i = 0; i < _max; i++) {
				if( returnString.equals("") == false ) returnString = returnString + ",";
				if( chartList.get(i).equals("yFieldName") )
				{
					returnString = returnString + yFieldsStr;
				}
				else if( chartList.get(i).equals("yFieldDisplayName") )
				{
					returnString = returnString + yFieldsDisplayStr;
				}
				else if( chartList.get(i).equals("yFieldFillColor") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendLabelPlacement") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendMarkHeight") )
				{
					returnString = returnString + _legendMarkHeight;
				}
				else if( chartList.get(i).equals("legendMarkWidth") )
				{
					returnString = returnString + _legendMarkWeight;
				}
				else if( chartData.containsKey(chartList.get(i)))
				{
					returnString = returnString + chartData.get(chartList.get(i));
				}
				else
				{
					returnString = returnString + "";
				}
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnString;
	}
	
	public static String getCandleChartParamToElement( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		ArrayList<String> chartList = getCandleChartPropertys();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		NodeList list;
		NodeList displaylist;
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";

		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		try {
//			list = (NodeList) _xpath.evaluate("./property", _element, XPathConstants.NODESET);
			list = _element.getElementsByTagName("property");
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				
				if( chartList.indexOf( _itemData.getAttribute("name") ) != -1  )
				{
					chartData.put(_itemData.getAttribute("name") , URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8")  );
				}
			}
//			list = (NodeList) _xpath.evaluate("./displayName/column", _element, XPathConstants.NODESET);
			list = ((Element)_element.getElementsByTagName("displayName").item(0)).getElementsByTagName("column");
			_max = list.getLength();
			
			for ( i = 0; i < _max; i++) {
				
				_itemData = (Element) list.item(i);
//				displaylist = (NodeList) _xpath.evaluate("./property", _itemData, XPathConstants.NODESET);
				displaylist = _itemData.getElementsByTagName("property");
				displayMap.clear();
				
				_subMax = displaylist.getLength();
				
				for ( j = 0; j < _subMax; j++) {
					_displayItemData = (Element) displaylist.item(j);
					displayMap.put(_displayItemData.getAttribute("name") , URLDecoder.decode(_displayItemData.getAttribute("value") , "UTF-8") );
				}
				
				if(displayMap.containsKey("visible") && displayMap.get("visible").equals("true") )
				{
					if( yFieldsStr.equals("") == false )
					{
						yFieldsStr = yFieldsStr + "~";
					}
					if( yFieldsDisplayStr.equals("") == false )
					{
						yFieldsDisplayStr = yFieldsDisplayStr + "~";
					}
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr = yFieldsColorStr + "~";
					}
					
					yFieldsStr = yFieldsStr + displayMap.get("column");
					yFieldsDisplayStr = yFieldsDisplayStr + displayMap.get("text");
					yFieldsColorStr = yFieldsColorStr + displayMap.get("color");
				}
				
			}
			
			_max = chartList.size();
//			for ( i = 0; i < chartList.size(); i++) {
			for ( i = 0; i < _max; i++) {
				if( returnString.equals("") == false ) returnString = returnString + ",";
				if( chartList.get(i).equals("yFieldName") )
				{
					returnString = returnString + yFieldsStr;
				}
				else if( chartList.get(i).equals("yFieldDisplayName") )
				{
					returnString = returnString + yFieldsDisplayStr;
				}
				else if( chartList.get(i).equals("yFieldFillColor") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendLabelPlacement") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendMarkHeight") )
				{
					returnString = returnString + _legendMarkHeight;
				}
				else if( chartList.get(i).equals("legendMarkWidth") )
				{
					returnString = returnString + _legendMarkWeight;
				}
				else if( chartData.containsKey(chartList.get(i)))
				{
					returnString = returnString + chartData.get(chartList.get(i));
				}
				else
				{
					returnString = returnString + "";
				}
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnString;
	}
	
	
	public static String getLineChartParamToElement( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		ArrayList<String> chartList = getLineChartPropertys();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		NodeList list;
		NodeList displaylist;
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";
		String yFieldsValueWeightStr = "";

		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		try {
//			list = (NodeList) _xpath.evaluate("./property", _element, XPathConstants.NODESET);
			list = _element.getElementsByTagName("property");
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				
				if( chartList.indexOf( _itemData.getAttribute("name") ) != -1  )
				{
					chartData.put(_itemData.getAttribute("name") , URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8")  );
				}
			}
//			list = (NodeList) _xpath.evaluate("./displayName/column", _element, XPathConstants.NODESET);
			list = ((Element)_element.getElementsByTagName("displayName").item(0)).getElementsByTagName("column");
			_max = list.getLength();
			
			for ( i = 0; i < _max; i++) {
				
				_itemData = (Element) list.item(i);
//				displaylist = (NodeList) _xpath.evaluate("./property", _itemData, XPathConstants.NODESET);
				displaylist = _itemData.getElementsByTagName("property");
				displayMap.clear();
				
				_subMax = displaylist.getLength();
				
				for ( j = 0; j < _subMax; j++) {
					_displayItemData = (Element) displaylist.item(j);
					displayMap.put(_displayItemData.getAttribute("name") , URLDecoder.decode(_displayItemData.getAttribute("value") , "UTF-8") );
				}
				
				if(displayMap.containsKey("visible") && displayMap.get("visible").equals("true") )
				{
					if( yFieldsStr.equals("") == false )
					{
						yFieldsStr = yFieldsStr + "~";
					}
					if( yFieldsDisplayStr.equals("") == false )
					{
						yFieldsDisplayStr = yFieldsDisplayStr + "~";
					}
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr = yFieldsColorStr + "~";
					}
					if( yFieldsValueWeightStr.equals("") == false )
					{
						yFieldsValueWeightStr = yFieldsValueWeightStr + "~";
					}
					
					yFieldsStr = yFieldsStr + displayMap.get("column");
					yFieldsDisplayStr = yFieldsDisplayStr + displayMap.get("text");
					yFieldsColorStr = yFieldsColorStr + displayMap.get("color");
					yFieldsValueWeightStr = yFieldsValueWeightStr + displayMap.get("valueWeight");
				}
				
			}
			
			_max = chartList.size();
//			for ( i = 0; i < chartList.size(); i++) {
			for ( i = 0; i < _max; i++) {
				if( returnString.equals("") == false ) returnString = returnString + ",";
				if( chartList.get(i).equals("yFieldName") )
				{
					returnString = returnString + yFieldsStr;
				}
				else if( chartList.get(i).equals("yFieldDisplayName") )
				{
					returnString = returnString + yFieldsDisplayStr;
				}
				else if( chartList.get(i).equals("yFieldFillColor") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendLabelPlacement") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendMarkHeight") )
				{
					returnString = returnString + _legendMarkHeight;
				}
				else if( chartList.get(i).equals("legendMarkWidth") )
				{
					returnString = returnString + _legendMarkWeight;
				}
				else if( chartList.get(i).equals("valueWeight") )
				{
					returnString = returnString + yFieldsValueWeightStr;
				}
				else if( chartData.containsKey(chartList.get(i)))
				{
					returnString = returnString + chartData.get(chartList.get(i));
				}
				else
				{
					returnString = returnString + "";
				}
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnString;
	}
	
	
	
	public static HashMap<Integer, String> getChartParamToElement2( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
//		ArrayList<String> chartList = getChartPropertys2();
		//ArrayList<String> chartList = getChartPropertys();
		ArrayList<String> chartList = getChartPropertys4CombinedColumn();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		NodeList list;
		NodeList displaylist;
		
		
		HashMap<Integer, String> displayNamesMap = new HashMap<Integer, String>();
		NodeList list_displayNames;
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";

		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		try {
//			list = (NodeList) _xpath.evaluate("./property", _element, XPathConstants.NODESET);
			list = _element.getElementsByTagName("property");
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				
				if( chartList.indexOf( _itemData.getAttribute("name") ) != -1  )
				{
					
					if( _itemData.getAttribute("name").equalsIgnoreCase("seriesXFields")  ){
						String _xfieldsValue= URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8");
						_xfieldsValue=_xfieldsValue.replaceAll("," , "~" );
						chartData.put(_itemData.getAttribute("name") , _xfieldsValue  );	
					}else{
						chartData.put(_itemData.getAttribute("name") , URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8")  );	
					}
					
				}
			}
//			list = (NodeList) _xpath.evaluate("./displayName/column", _element, XPathConstants.NODESET);
			
			
			
			
			
			
			
			list_displayNames = ((Element)_element.getElementsByTagName("displayNames").item(0)).getElementsByTagName("displayName");
			int _max_displayNames=list_displayNames.getLength();
			
			//Element _itemData_displayNames;
			for( int dispIndex=0; dispIndex<_max_displayNames;  dispIndex++ ){
				
				returnString="";
				yFieldsStr="";
				yFieldsDisplayStr="";
				yFieldsColorStr="";
				
				list = ((Element)list_displayNames.item(dispIndex)).getElementsByTagName("column");
				
				//list = ((Element)_element.getElementsByTagName("displayName").item(0)).getElementsByTagName("column");
				_max = list.getLength();
				
				for ( i = 0; i < _max; i++) {
					
					_itemData = (Element) list.item(i);
//					displaylist = (NodeList) _xpath.evaluate("./property", _itemData, XPathConstants.NODESET);
					displaylist = _itemData.getElementsByTagName("property");
					displayMap.clear();
					
					_subMax = displaylist.getLength();
					
					for ( j = 0; j < _subMax; j++) {
						_displayItemData = (Element) displaylist.item(j);
						displayMap.put(_displayItemData.getAttribute("name") , URLDecoder.decode(_displayItemData.getAttribute("value") , "UTF-8") );	
					}
					
					if(displayMap.containsKey("visible") && displayMap.get("visible").equals("true") )
					{
						if( yFieldsStr.equals("") == false )
						{
							yFieldsStr = yFieldsStr + "~";
						}
						if( yFieldsDisplayStr.equals("") == false )
						{
							yFieldsDisplayStr = yFieldsDisplayStr + "~";
						}
						if( yFieldsColorStr.equals("") == false )
						{
							yFieldsColorStr = yFieldsColorStr + "~";
						}
						
						yFieldsStr = yFieldsStr + displayMap.get("column");
						yFieldsDisplayStr = yFieldsDisplayStr + displayMap.get("text");
						yFieldsColorStr = yFieldsColorStr + displayMap.get("color");
					}
					
				}
				
				_max = chartList.size();
//				for ( i = 0; i < chartList.size(); i++) {
				for ( i = 0; i < _max; i++) {
					if( returnString.equals("") == false ) returnString = returnString + ",";
					if( chartList.get(i).equals("yFieldName") )
					{
						returnString = returnString + yFieldsStr;
					}
					else if( chartList.get(i).equals("yFieldDisplayName") )
					{
						returnString = returnString + yFieldsDisplayStr;
					}
					else if( chartList.get(i).equals("yFieldFillColor") )
					{
						returnString = returnString + yFieldsColorStr;
					}
					else if( chartList.get(i).equals("legendLabelPlacement") )
					{
						returnString = returnString + yFieldsColorStr;
					}
					else if( chartList.get(i).equals("legendMarkHeight") )
					{
						returnString = returnString + _legendMarkHeight;
					}
					else if( chartList.get(i).equals("legendMarkWidth") )
					{
						returnString = returnString + _legendMarkWeight;
					}
					else if( chartData.containsKey(chartList.get(i)))
					{
						returnString = returnString + chartData.get(chartList.get(i));
					}
					else
					{
						returnString = returnString + "";
					}
				}
				
				
				displayNamesMap.put(dispIndex, returnString);
			}
			
			
			
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return displayNamesMap;
	}
	
	public static String getChartParamToElement4Taximeter( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		ArrayList<String> chartList = getChartPropertys();
		HashMap<String, String> chartData = new HashMap<String, String>();
		NodeList list;
		Element _itemData;
		String returnString = "";
		
		String _value;
		String _property;
		
		try {
			list = _element.getElementsByTagName("property");
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				_property = _itemData.getAttribute("name");
				
				if( _property.equals("dataSet") || _property.equals("column") || _property.equals("interval") 
						|| _property.equals("minimumRange") || _property.equals("maximumRange") || _property.equals("chartAngle") || _property.equals("viewType")){
					
					if( returnString.equals("") == false ) returnString = returnString + ",";
					
					_value = URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8");
					
					returnString = returnString +_value;
				}
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnString;
	}
	
	
	public static String getChartParamToElement4Radar( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		ArrayList<String> chartList = getChartPropertys4Radar();
		HashMap<String, String> chartData = new HashMap<String, String>();
		NodeList list;
		Element _itemData;
		String returnString = "";
		
		String _value;
		String _property;
		
		
		
		HashMap<String, String> _seriesColorMap = new HashMap<String, String>();
		
		Element _seriesColorItemData;
		
		String yFieldsColorStr = "";
		
		NodeList list_seriesColors;
		NodeList _seriesColorlist;
		int _max = 0;
		int _subMax = 0;
		int j = 0;
		
		try {
			list = _element.getElementsByTagName("property");
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				_property = _itemData.getAttribute("name");
				
				if( _property.equals("title") || _property.equals("valueField") || _property.equals("legendField") || _property.equals("categoryField")  
						|| _property.equals("interiorGap") || _property.equals("titleVisible") || _property.equals("legendVisible") ){
					
					if( returnString.equals("") == false ) returnString = returnString + ",";
					
					_value = URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8");
					
					returnString = returnString +_value;
				}
			}
			
			NodeList seriesColorsNodeList=_element.getElementsByTagName("seriesColors");
			if( seriesColorsNodeList.getLength() > 0 ){
				list_seriesColors = ((Element)_element.getElementsByTagName("seriesColors").item(0)).getElementsByTagName("column");	
				_max = list_seriesColors.getLength();
				
				
				for ( i = 0; i < _max; i++) {
					
					_itemData = (Element) list_seriesColors.item(i);
					_seriesColorlist = _itemData.getElementsByTagName("property");
					_seriesColorMap.clear();
					
					_subMax = _seriesColorlist.getLength();
					
					for ( j = 0; j < _subMax; j++) {
						_seriesColorItemData = (Element) _seriesColorlist.item(j);
						_seriesColorMap.put(_seriesColorItemData.getAttribute("name") , URLDecoder.decode(_seriesColorItemData.getAttribute("value") , "UTF-8") );
					}
					
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr = yFieldsColorStr + "~";
					}
					
					yFieldsColorStr = yFieldsColorStr + _seriesColorMap.get("color");
					
				}
				
				
				_max = chartList.size();
				for ( i = 0; i < _max; i++) {
					
					if( chartList.get(i).equals("seriesColors") )
					{
						if( returnString.equals("") == false ) returnString = returnString + ",";
						
						returnString = returnString + yFieldsColorStr;
					}
				}
			}
			
			
			
			
			
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnString;
	}
	
	
	
	public HashMap<String, Object> convertElementToItem(Element _childItem, int _dCnt,  HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param, float _cloneX, float _cloneY, float _updateY , int _totalPageNum , int _currentPageNum, boolean labelProjectFlag ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _itemId = _childItem.getAttribute("id");
		String _className = _childItem.getAttribute("className");

		NodeList _propertys = _childItem.getElementsByTagName("property");
		NodeList _ubfunction = _childItem.getElementsByTagName("ubfunction");
		HashMap<String, Object> _propList = new HashMap<String, Object>();

		String _dataTypeStr = "";
		String _dataColumn = "";
		String _dataID = "";
		String _model_type = "";
		String _barcode_type = "";
		
		// formatter variables
		String _formatter="";
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";
		String _inputForamtString = "";
		String _outputFormatString = "";

		// edit formatter variables (e-form)
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";

		
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		// image variable
		String _prefix="";
		String _suffix="";
		String[] _datasets={"",""};
		String _systemFunction = "";			
		
		boolean _resizeFont = false;
		
		_propList = mItemPropVar.getItemName(_className);
		if( _propList == null) return null;
		
		// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
		if( _propList.containsKey("rowId") )
		{
			_propList.put("rowId", _dCnt);
		}
		
		//if( _propList == null) return null;
		
		Element _propItem;
		// xml Item propertys 
		for(int p = 0; p < _propertys.getLength(); p++)
		{
			_propItem = (Element) _propertys.item(p);
			if( _propItem.getParentNode().getNodeName().equals("item") == false )
			{
				continue;
			}
			
			String _name = _propItem.getAttribute("name");
			String _value = _propItem.getAttribute("value");
			
			if(_propList.containsKey(_name))
			{
				if( _name.equals("fontFamily"))
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					if(common.isValidateFontFamily(_value))
						_propList.put(_name, _value);
					else
						_propList.put(_name, "Arial");
				}
				else if( _name.equals("contentBackgroundColors")  )
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					
					ArrayList<String> _arrStr = new ArrayList<String>();
					_arrStr = mPropertyFn.getColorArrayString( _value );
					_propList.put(_name, _arrStr);
					
					_arrStr = mPropertyFn.getBorderSideToArrayList(_value);
					_propList.put((_name + "Int"), _arrStr);
					
				}
				else if( _name.equals("contentBackgroundAlphas") )
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					
					ArrayList<String> _arrStr = new ArrayList<String>();
					_arrStr = mPropertyFn.getBorderSideToArrayList(_value);
					_propList.put(_name, _arrStr);
					
				}
				else if( _name.indexOf("Color") != -1)
				{
					_propList.put((_name + "Int"), _value);
					_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value));
					_propList.put(_name, _value);
				}
				else if( _name.equals("lineHeight"))
				{
					//_value = "1.16"; //TODO LineHeight Test
					
					_value = _value.replace("%25", "");
					_value = String.valueOf((Float.parseFloat(_value)/100));					
					
					_propList.put(_name, _value);
				}
				else if( _name.equals("borderType"))
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("text"))
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("prompt"))
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("label"))
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("value"))
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("borderSide"))
				{
					ArrayList<String> _bSide = new ArrayList<String>();
					_value = URLDecoder.decode(_value, "UTF-8");
					if( !_value.equals("none") )
					{
						_bSide = mPropertyFn.getBorderSideToArrayList(_value);

						if( _bSide.size() > 0)
						{
							String _type = (String) _propList.get("borderType");
							_type = mPropertyFn.getBorderType(_type);
							_propList.put("borderType", _type);
						}

					}

					_propList.put(_name, _bSide);
				}
				else if( _name.equals("x"))
				{
					_propList.put(_name, _value);
					_propList.put("left", _value);
				}
				else if( _name.equals("y"))
				{
					_propList.put(_name, _value);
					_propList.put("top", _value);
				}
				else if( _name.equals("type") )
				{
					_model_type = _value;
					_propList.put(_name, _value);
				}
				else if( _name.equals("barcodeType") )
				{
					_barcode_type = _value;
					_propList.put(_name, _value);
				}
				else if( _name.equals("clipArtData") )
				{
					_propList.put(_name, _value + ".svg");
				}
				else if( _name.equals("dataProvider") )
				{
					//HashMap<String, String> _dataProvider=new HashMap<String, String>();
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("ubHyperLinkUrl") )
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("ubHyperLinkText") )
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("fileDownloadUrl") )
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("selectedDate") )
				{
					/*
					 * DateField 의 날짜 값을 순수 숫자로 치환한다.
					 * 웹에디터에 dateFormat 속성을 변경할 수 없어 "yyyy-MM-dd"로 고정. 변경될 경우 수정필요.
					 * ex)
					 * 원래 값 : Thu Oct 6 13:31:12 GMT+0900 2016
					 * 변경된 값 : 2016-10-06
					*/
					_value = URLDecoder.decode(_value, "UTF-8");
					String ubDateFormat = "yyyy-MM-dd";
					SimpleDateFormat beforeFormat = new SimpleDateFormat("EEE MMM d kk:mm:ss 'GMT'Z yyyy",Locale.US);
					SimpleDateFormat afterFormat = new SimpleDateFormat(ubDateFormat);
					String convertedDateString = "";
					try {
						Date gmtDate = beforeFormat.parse(_value);
						convertedDateString = afterFormat.format(gmtDate);
					} catch (ParseException e) {
						log.error(getClass().getName()+"::convertElementToItem::"+"DateField selectedDate parsing Fail.>>>"+e.getMessage());
					} finally{
						_propList.put(_name, convertedDateString);
						_propList.put("text", convertedDateString);
					}
				}
				else if(_name.equals("column"))
				{
//					_dataColumn = _value;
					_propList.put(_name, URLDecoder.decode(_value, "UTF-8") );
				}
				else if(_name.equals("dataSet"))
				{
//					_dataID = _value;
					_propList.put(_name, URLDecoder.decode(_value, "UTF-8") );
				}
				else if( _name.equals("systemFunction") ){
					_systemFunction = URLDecoder.decode(_value, "UTF-8");
				}
				else
				{
					_propList.put(_name, _value);
				}
			}
			
			else if( _name.equals("rotation") )
			{
				_propList.put("rotate", _value);
				_propList.put(_name, _value);		// roatation 값 담아두기
			}
			
			else if( _name.equals("points")  )
			{
				_value = URLDecoder.decode(_value, "UTF-8");
				
				ArrayList<String> _arrStr = new ArrayList<String>();
				_arrStr = mPropertyFn.getPathArrayString( _value );
				_propList.put("path", _arrStr);
			}
			
			else if( _name.equals("checked") )
			{
				_propList.put("selected", _value);
			}
			else if( _name.equals("conerRadius") )
			{
				_propList.put("rx", _value);
				_propList.put("ry", _value);
			}
			else if( _name.equals("borderThickness") ) 
			{
				_propList.put("borderWidth", _value);
			}
			else if( _name.equals("borderWeight") ) 
			{
				_propList.put("borderWidth", _value);
			}			
			else if( _name.equals("printVisible") )
			{
				if( ("PRINT".equals(isExportType) || "PDF".equals(isExportType) || "PPT".equals(isExportType)) && "false".equals(_value) )
				{
					return null;
				}
			}
			else if( _name.equals("markanyVisible") )
			{
				if( _isMarkAny && "PRINT".equals(isExportType) && "false".equals(_value) )
				{
					return null;
				}
			}
			else if(_name.equals("dataType"))
			{
//				_dataType=_value;
//				
//				if( _value.equals("1") )
//				{
//					ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
//					HashMap<String, Object> _dataHm = _list.get(_dCnt);
//					
//					if( _dCnt >= _list.size()) 
//					{
//						_propList.put("text", "");
//						continue;
//					}
//					
//					Object _dataValue = _dataHm.get(_dataColumn);
//					
//					if( _dataValue == null ){
//						_dataValue = "null";
//					}
//					_propList.put("text", _dataValue);
//				}
//				else if( _value.equals("3"))
//				{
//					String _txt = (String) _propList.get("text");
//					int _inOf = _txt.indexOf("{param:");
//					String _pKey = "";
//					if( _inOf != -1 )
//					{
//						_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);
//
//						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);
//
//						String _pValue = _pList.get("parameter");
//
//						if( _pValue.equals("undefined"))
//						{
//							_propList.put("text", "");
//						}
//						else
//						{
//							_propList.put("text", _pValue);
//						}
//
//					}
//					else
//					{
//						_propList.put("text", "");
//					}
//				}
				
				_propList.put(_name, _value);
			}
			
			else if( _name.equals("data") )
			{
				if(_className.equals("UBImage") || _className.equals("UBSignature") || _className.equals("UBPicture") || _className.equals("UBTextSignature"))
				{
					// test
					/*
					String projName = m_appParams.getREQ_INFO().getPROJECT_NAME();
					String formName = m_appParams.getREQ_INFO().getFORM_ID();
					String	_url = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getImageData&PROJECT_NAME=" + projName + "&FORM_ID=" + formName + "&ITEM_ID="+_itemId;
					_propList.put("src", _url);
				
					if(!mImageData.containsKey(_itemId))
					{
						_value = URLDecoder.decode(_value, "UTF-8");
						mImageData.put(_itemId, _value);
					}							
					*/
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put("src",  URLEncoder.encode(_value, "UTF-8"));
				}
			}
			
			else if( _name.equals("ubToolTip") ){
				// tooltip 현재는 text가 아닌 ubToolTip 속성값을 이용시 데이터셋의 경우 dataset_0.col_0 과 같이 담겨서 tooltip가 제대로 표시안되는 현상으로 임시 주석. 2016-11-17 최명진
				
				if(_className.equals("UBImage") ) {
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put("tooltip",  _value);
					//_propList.put("tooltip",  URLEncoder.encode(_value, "UTF-8"));
				}
			}
			else if( _name.equals("prefix") ){
				_prefix=URLDecoder.decode(_value, "UTF-8");
			}
			
			else if( _name.equals("suffix") ){
				//_suffix=URLDecoder.decode(_value, "UTF-8");
				_suffix=_value;
			}
			else if( _name.equals("systemFunction") ){
				_systemFunction = URLDecoder.decode(_value, "UTF-8");
			}
			else if( _name.equals("systemFunction") ){
				
				if( !_value.equalsIgnoreCase("null") && !_value.equalsIgnoreCase("") ){
					_value = URLDecoder.decode(_value, "UTF-8");
					mFunction.setDatasetList(_data);
					

					String _fnValue;
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_value.toString() , _dCnt,_totalPageNum,_currentPageNum, -1,-1 , "");
					}else{
						_fnValue = mFunction.function(_value.toString(),_dCnt, _totalPageNum , _currentPageNum,-1,-1);
					}
					 
					
					
					
					_propList.put(_name, _value);
					
					if( _className.equals("UBImage") ) {
						_fnValue = URLDecoder.decode(_fnValue, "UTF-8");
						_propList.put("src",  URLEncoder.encode(_fnValue, "UTF-8"));
					}else if(_className.equals("UBCheckBox") ) {
						_propList.put("selected", _fnValue);
					}else{
						_propList.put("text", _fnValue);
					}
					
				}
			}
			
			else if(_name.equals("column"))
			{
				_dataColumn = _value;
				_propList.put(_name, _value);
			}
			else if(_name.equals("dataSet"))
			{
				_dataID = _value;
				_propList.put(_name, _value);
			}
			else if(_name.equals("dataSets"))
			{
				//_propList.put(_name,  );
				_datasets=_value.split("%2C");
			}
			
			else if(_name.equals("startPoint"))
			{

				_value = URLDecoder.decode(_value, "UTF-8");
				String[] _sPoint = _value.split(",");
				_propList.put("x1", Float.valueOf(_sPoint[0]) + _cloneX);
				
				
				if( _updateY > -1 )
				{
					_propList.put("y1", Float.valueOf(_sPoint[1]) + _updateY);
				}
				else
				{
					_propList.put("y1", Float.valueOf(_sPoint[1]) + _cloneY);
				}
				
			}
			else if(_name.equals("endPoint"))
			{
				_value = URLDecoder.decode(_value, "UTF-8");
				String[] _ePoint = _value.split(",");

				
				if(_className.equals("UBConnectLine"))
				{

					_propList.put("x3", Float.valueOf(_ePoint[0]) + _cloneX);
					if( _updateY > -1 )
					{
						_propList.put("y3", Float.valueOf(_ePoint[1]) + _updateY);
					}
					else
					{
						_propList.put("y3", Float.valueOf(_ePoint[1]) + _cloneY);
					}

					
				}else{

					_propList.put("x2", Float.valueOf(_ePoint[0]) + _cloneX);
					if( _updateY > -1 )
					{
						_propList.put("y2", Float.valueOf(_ePoint[1]) + _updateY);
					}
					else
					{
						_propList.put("y2", Float.valueOf(_ePoint[1]) + _cloneY);
					}

				}
				
				
			}
			else if(_name.equals("centerPoint"))
			{
				_value = URLDecoder.decode(_value, "UTF-8");
				String[] _ePoint = _value.split(",");
				
				_propList.put("x2", Float.valueOf(_ePoint[0]) + _cloneX);
				if( _updateY > -1 )
				{
					_propList.put("y2", Float.valueOf(_ePoint[1]) + _updateY);
				}
				else
				{
					_propList.put("y2", Float.valueOf(_ePoint[1]) + _cloneY);
				}
				
			}
			

			else if( _name.equals("startButtonColor"))
			{
				_propList.put((_name + "Int"), _value);
				_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value));
				_propList.put(_name, _value);
			}
			else if( _name.equals("endButtonColor"))
			{
				_propList.put((_name + "Int"), _value);
				_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value));
				_propList.put(_name, _value);
			}
			
			
			
			else if( _name.equals("width"))
			{
				//_propList.put("x2", _value);
				_propList.put(_name, _value);
			}
			else if( _name.equals("height"))
			{
				//_propList.put("y2", _value);
				_propList.put(_name, _value);
			}
			else if(_name.equals("lineThickness"))
			{
				_propList.put("thickness", _value);
			}
			
			else if(_name.equals("formatter"))
			{
				_formatter = _value;
				_propList.put("formatter", _formatter);
			}

			else if(_name.equals("editItemFormatter"))
			{
				_formatterE = _value;
				_propList.put("editItemFormatter", _formatterE);
			}
			
			else if(_name.equals("nation"))
			{
				_nation = URLDecoder.decode(_value, "UTF-8");
			}
			
			else if(_name.equals("align"))
			{
				_align=_value;
			}
			else if( _name.equals("mask") ){
				_mask = URLDecoder.decode(_value, "UTF-8");
			}
			
			else if( _name.equals("decimalPointLength") ){
				
				_decimalPointLength = common.ParseIntNullChk(_value, 0);
			}				
			
			else if( _name.equals("useThousandComma") ){
				_useThousandComma = Boolean.parseBoolean(_value);
			}		
			else if( _name.equals("isDecimal") ){
				_isDecimal = Boolean.parseBoolean(_value);
			}		
			else if( _name.equals("formatString") ){
				_formatString = _value;
			}
			else if( _name.equals("band_x"))
			{
				_propList.put(_name, _value);
			}
			else if( _name.equals("band_y"))
			{
				_propList.put(_name, _value);
			}
			else if( _name.equals("leftBorderType") ||  _name.equals("rightBorderType") ||  _name.equals("topBorderType") ||  _name.equals("bottomBorderType") )
			{
				_propList.put(_name, _value);
			}
			else if( _name.equals("resizeFont") && "true".equals(_value) )
			{
				_resizeFont = true;
			}
			else if( _name.equals("colorFieldName"))
			{
				_propList.put(_name, _value);
			}
			else if( _name.equals("selectedText"))
			{
				_propList.put(_name, _value);
			}
			else if( _name.equals("deSelectedText"))
			{
				_propList.put(_name, _value);
			}
		}
		
		
		if( _className.toUpperCase().indexOf("LINE") == -1)
		{
			_propList.put("x1", _propList.get("x"));
			_propList.put("y1", _propList.get("y"));
			_propList.put("x2", _propList.get("width"));
			_propList.put("y2", _propList.get("height"));
		}
		
		
		// Item의 changeData가 있는지 확인
		if(mChangeItemList != null )
		{
			_propList = convertChangeItemDataText( _currentPageNum ,_propList, "");
		}
		
		// 포맷터 값이존재할경우각각의 데이터를 담는 처리
		NodeList formatterItem = _childItem.getElementsByTagName("formatter");
		if( formatterItem != null && formatterItem.getLength() > 0 )
		{	
			NodeList formatterProperty = ((Element) formatterItem.item(0)).getElementsByTagName("property");
			int propertySize = formatterProperty.getLength();
			for (int i = 0; i < propertySize; i++) {
				
				_propItem = (Element) formatterProperty.item(i);

				String _name = _propItem.getAttribute("name");
				String _value = _propItem.getAttribute("value");
				
				if( _name.equals("nation") ){
					_nation = URLDecoder.decode(_value, "UTF-8");
				}
				
				else if( _name.equals("mask") ){
					_mask = URLDecoder.decode(_value, "UTF-8");
				}

				else if( _name.equals("decimalPointLength") ){
					
					_decimalPointLength = common.ParseIntNullChk(_value, 0);
				}				
				
				else if( _name.equals("useThousandComma") ){
					_useThousandComma = Boolean.parseBoolean(_value);
				}		
				else if( _name.equals("isDecimal") ){
					_isDecimal = Boolean.parseBoolean(_value);
				}		
				else if( _name.equals("formatString") ){
					_formatString = _value;
				}
				else if(_name.equals("align"))
				{
					_align = _value;
				}
				else if( _name.equals("inputFormatString") )
				{
					_inputForamtString =  URLDecoder.decode(_value , "UTF-8");
				}
				else if( _name.equals("outputFormatString") )
				{
					_outputFormatString = URLDecoder.decode(_value , "UTF-8");
				}
				
			}
		}
		// 에디트 포맷터 값이존재할경우각각의 데이터를 담는 처리
		NodeList editFormatterItem = _childItem.getElementsByTagName("editItemFormatter");
		if( editFormatterItem != null && editFormatterItem.getLength() > 0 )
		{	
			String _eformatDataset=null;
			String _eformatKeyField=null;
			String _eformatLabelField=null;
			
			NodeList formatterProperty = ((Element) editFormatterItem.item(0)).getElementsByTagName("property");
			int propertySize = formatterProperty.getLength();
			for (int i = 0; i < propertySize; i++) {
				
				_propItem = (Element) formatterProperty.item(i);
				
				String _name = _propItem.getAttribute("name");
				String _value = _propItem.getAttribute("value");
				
				if( _name.equals("nation") ){
					_nationE = URLDecoder.decode(_value, "UTF-8");
					_propList.put("eformnation", 	_nationE );
				}
				
				else if( _name.equals("mask") ){
					_maskE = URLDecoder.decode(_value, "UTF-8");
					_propList.put("eformmask", 	_maskE );
				}
				else if( _name.equals("decimalPointLength") ){
					
					_decimalPointLengthE = common.ParseIntNullChk(_value, 0);
					
					_propList.put("eformdecimalPointLength", 	_decimalPointLengthE );
					
				}				
				else if( _name.equals("useThousandComma") ){
					_useThousandCommaE = Boolean.parseBoolean(_value);
					
					_propList.put("eformuseThousandComma", 	_useThousandCommaE );
				}		
				else if( _name.equals("isDecimal") ){
					_isDecimalE = Boolean.parseBoolean(_value);
					_propList.put("eformisDecimal", _isDecimalE	 );
				}		
				else if( _name.equals("formatString") ){
					_formatStringE = _value;
					_propList.put("eformformatString", _formatStringE	 );
				}else if( _name.equals("dataProvider") ){
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put("eformDataProvider", _value	 );
				}else if( _name.equals("dataset") ){
					_eformatDataset = _value;
				}else if( _name.equals("keyField") ){
					_eformatKeyField = _value;
				}else if( _name.equals("valueField") ){
					_eformatLabelField = _value;
				}
			}
			
			if( _formatterE.equals("SelectMenu") ){
				
				// dataset으로 comboBox 표현.
				if( _eformatDataset != null &&  (!_eformatDataset.equals("null")) && _data.containsKey(_eformatDataset) ){
					
					String _efText = _propList.get("text").toString();
					Boolean _hasValueKey = false;
					
					ArrayList<HashMap<String, Object>> _list = _data.get(_eformatDataset);
					
					HashMap<String, Object> _dataHm;
					Object _keyData;
					Object _labelData;
					
					JSONArray ja = new JSONArray();
					String _jsonStr=null;
					JSONObject jo;
					String _keyStr=null;
					String _labelStr=null;
					
					for( int _eformatIdx=0; _eformatIdx<_list.size(); _eformatIdx++ ){
						_dataHm = _list.get(_eformatIdx);
						_keyData = _dataHm.get(_eformatKeyField);
						_labelData = _dataHm.get(_eformatLabelField);
						_keyStr=_keyData.toString();
						_labelStr = _labelData.toString();
						
						if( _efText.equals(_keyStr) && _hasValueKey == false ){
							_hasValueKey = true;
							_propList.put("text", _labelStr );
						}
						
						jo = new JSONObject();
						jo.put("label", _labelStr);
						jo.put("value",_keyStr );
						ja.add(jo);
					}
					
					_jsonStr = ja.toJSONString();
					
					_propList.put("eformDataProvider", _jsonStr	 );
				}else{
					
					if( _propList.containsKey("eformDataProvider") == false && _propList.get("eformDataProvider") == null )
					{
						_propList.put("eformDataProvider", "[]");
					}
					
					String _jsonStr = _propList.get("eformDataProvider").toString(); 
					JSONParser jsonParser = new JSONParser();
					try {
						
						JSONArray ja = (JSONArray) jsonParser.parse(_jsonStr);
						
						String _efText = _propList.get("text").toString();
						JSONObject oj;
						for( int jsonIdx=0; jsonIdx<ja.size(); jsonIdx++ ){
							oj=(JSONObject) ja.get(jsonIdx);
							if( oj.get("value").equals(_efText) ){
								_propList.put("text", oj.get("label") );
								break;
							}
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
		}

		
		
		//hyperLinkedParam처리
		if( _propList.containsKey("ubHyperLinkType") && "2".equals( _propList.get("ubHyperLinkType") )  )
		{
			NodeList _hyperLinkedParam = _childItem.getElementsByTagName("ubHyperLinkParm");
			if( _hyperLinkedParam != null && _hyperLinkedParam.getLength() > 0 )
			{
				Element _hyperLinkEl = (Element) _hyperLinkedParam.item(0);
				NodeList _hyperLinkedParams = _hyperLinkEl.getElementsByTagName("param");
				int _hyperLinkedParamSize = _hyperLinkedParams.getLength();
				
				HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
				
				ArrayList<HashMap<String,String>> _ubHyperLinkTypeArr = new ArrayList<HashMap<String,String>>();
				
				for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
				{
					Element _hyperParam = (Element) _hyperLinkedParams.item(_hyperIdx);
					NodeList _hyperPropertys = _hyperParam.getElementsByTagName("property");
					int _hyperPropertysSize = _hyperPropertys.getLength();
					String _hyperParamKey = "";
					String _hyperParamValue = "";
					String _hyperParamType = "";
					
					for (int _hyperProIdx = 0; _hyperProIdx <  _hyperPropertysSize; _hyperProIdx++) 
					{
						Element _hyperProperty = (Element) _hyperPropertys.item(_hyperProIdx);
						if( "id".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamKey = _hyperProperty.getAttribute("value").toString();
						}
						else if( "value".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamValue = _hyperProperty.getAttribute("value").toString();
						}
						else if( "type".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamType = _hyperProperty.getAttribute("value").toString();
						}
					}
					
					// thread 처리시 Map를 재활용 하기 위해 속성을 담아둔다.
					if( isExportType.equals("PRINT") || isExportType.equals("PDF") )
					{
						HashMap<String, String> _ubhyperLinkedM = new HashMap<String, String>();
						_ubhyperLinkedM.put("id", _hyperParamKey);
						_ubhyperLinkedM.put("value", _hyperParamValue);
						_ubhyperLinkedM.put("type", _hyperParamType);
						_ubHyperLinkTypeArr.add(_ubhyperLinkedM);
					}
					
					if( "DataSet".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						
						_hyperParamValue = "";
						
						if(_data.containsKey(_hyperLinkedDataSetId))
						{
							ArrayList<HashMap<String, Object>> _list = _data.get( _hyperLinkedDataSetId );
							Object _dataValue = "";
							if( _list != null ){
								if( _dCnt < _list.size() )
								{
									HashMap<String, Object> _dataHm = _list.get(_dCnt);
									_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
								}
							}
						}
					}
					else if("Parameter".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						if( _param.containsKey(_hyperLinkedDataSetColumn))
						{
							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

							String _pValue = _pList.get("parameter");

							if( _pValue.equals("undefined"))
							{
								_hyperParamValue = "";
							}
							else
							{
								_hyperParamValue = _pValue;
							}
						}
						else
						{
							_hyperParamValue = "";
						}
					}
					
					_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
				}
				
				_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
				
				if( _ubHyperLinkTypeArr.size() > 0 ) _propList.put("ubHyperLinkTypeArr", _ubHyperLinkTypeArr);
			}
			
		}

		
		
		// 보더업데이트
		_propList = convertItemToBorder(_propList);
		
		/**
		//DataSet정보 처리
		if( _propList.containsKey("dataType") && (_propList.get("dataType").equals("1") || _propList.get("dataType").equals("3"))  )
		{
			_dataType=_propList.get("dataType").toString();
			_dataID = _propList.get("dataSet").toString();
			_dataColumn = _propList.get("column").toString();
			
			if( _dataType.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				//HashMap<String, Object> _dataHm = _list.get(_dCnt);
				
				if( _list == null || _dCnt >= _list.size() ) 
				{
					_propList.put("text", "");
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					
					if( _dataValue == null ){
						_dataValue = "null";
					}
					
					// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
					if(_className.equals("UBSVGArea") ){
						
						String _tmpDataValue = _dataValue.toString();
						boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
						
						if(_bSVG)
						{
							_dataValue = StringUtil.replaceSVGTag(_tmpDataValue);	
						}
						else
						{
							boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
							if(!_bHasHtmlTag)
								_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
								
							_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
							_dataValue = StringUtil.replaceSVGTag(_tmpDataValue);
						}
								
						_dataValue = _dataValue.toString().replace(" ", "%20");
						
						if( !_dataValue.toString().equals("") )
						{
							_propList.put("data",  URLEncoder.encode(_dataValue.toString(), "UTF-8"));
						}
						else
						{
							return null;
						}
						
					}
					else if( _className.equals("UBSVGRichText") )
					{
						if( _dataValue != null && _dataValue.toString().equals("") == false )
						{
							_propList = convertUBSvgItem( _dataValue.toString(), _propList);
							
							if(_propList == null ) return null;
						}
//						String _svgTag = convertSvgRichText(_dataValue.toString(),_propList);
//						if( "".equals( _svgTag ) ) return null;
//						
//						//변환된 svg태그를 인코딩하기 위하여 처리
//						_svgTag = StringUtil.replaceSVGTag(_svgTag.toString());	
//						_svgTag = _svgTag.replace(" ", "%20");
//						
//						_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8") );
					}
					
					if("UBImage".equals(_className) || _className.equals("UBSignature") || _className.equals("UBPicture") || _className.equals("UBTextSignature"))
					{
//						_propList.put("src", _dataValue);
						if( _dataValue != null ) _dataValue =  URLEncoder.encode( _dataValue.toString() , "UTF-8");
						_propList.put("src", _dataValue );
					}
					else if("UBRadioBorder".equals(_className))
					{
						_propList.put("selected", _dataValue);
					}					
					else if("UBCheckBox".equals(_className) )
					{
						String _selectedText=_propList.get("selectedText").toString();
						//String _deSelectedText=_propList.get("deSelectedText").toString();
						if( _selectedText.equalsIgnoreCase(_dataValue.toString()) ){
							_propList.put("selected", "true");
						}else{
							_propList.put("selected", "false");	
						}
						
					}					
					else
					{
						_propList.put("text", _dataValue);
					}
				}
				
			}
			else if( _dataType.equals("3"))
			{
				String _txt = _propList.get("text").toString();
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					
					if( _inOf != 0 ){
						
						// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
						if(_className.equals("UBSVGArea") ){
							
							String _tmpDataValue = String.valueOf(_txt);;
							boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
							
							String _svgTag = null;
							if(_bSVG)
							{
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue);	
							}
							else
							{
								boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
								if(!_bHasHtmlTag)
									_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
									
								_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue);
							}
												
							log.debug("3874-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _svgTag + "]");
							
							_svgTag = _svgTag.replace(" ", "%20");
							
							if( !_svgTag.equals("") )
							{
								_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8"));
							}
							else
							{
								return null;
							}
							
						}
						else if( _className.equals("UBSVGRichText") )
						{
							if( _txt != null && _txt.equals("") == false )
							{
								_propList = convertUBSvgItem( _txt, _propList);
								
								if(_propList == null ) return null;
							}
								
						}
						
						
						String _fnValue;
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							//_fnValue = mFunction.testFN(_txt,_dCnt,_totalPageNum,_currentPageNum, -1 , -1, "" );
							_fnValue = _txt;
						}else{
							_fnValue = mFunction.function(_txt,_dCnt,_totalPageNum,_currentPageNum, -1 , -1 );
						}
						
						
						if(_className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture")){
							_propList.put("src",  URLEncoder.encode(_fnValue, "UTF-8"));
						}else{
							_propList.put("text", _fnValue);
						}
						
						
						
					}else{
						
						int _paramFnBraketIndex =_txt.indexOf("}",_inOf); 
						if( _paramFnBraketIndex != -1 ){
							_pKey = _txt.substring(_inOf + 7 , _paramFnBraketIndex );
						}else{
							_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);	
						}

						
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_propList.put("text", "");
						}
						else
						{
							_propList.put("text", _pValue);
						}
						
					}
					

				}
				else
				{
					_propList.put("text", "");
				}
			}
		}
		else if( _propList.containsKey("dataSet") && _propList.get("dataSet") != null )
		{
			_dataID = _propList.get("dataSet").toString();
		}
		*/
		
		if( _propList.containsKey("dataSet") && _propList.get("dataSet") != null )
		{
			_dataID = _propList.get("dataSet").toString();
			
			if( _propList.containsKey("column") && _propList.get("column") != null )
			{
				_dataColumn = _propList.get("column").toString();
			}
		}
		
		
		/**
		//TEST UBSVGRichText
		if(  _className.equals("UBSVGRichText") )
		{
			float[] _height = {100,500};
			convertSvgRichText(_propList.get("data").toString(), _height);
		}
		*/
		
		
		float _updateX = 0;
		if(  _updateY > -1 )
		{
			_propList.put("y", _updateY);
			_propList.put("top", _updateY);
		}
		else if( _cloneY != 0 )
		{
			_propList.put("y", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
			_propList.put("top", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
		}
		
		if( _cloneX != 0 )
		{
			_updateX = _cloneX + Float.valueOf( _propList.get("x").toString() );
			_propList.put("x", _updateX);
			_propList.put("left", _updateX);
		}
		
		//Image 
		if( _className.equals("UBSVGArea") || _className.equals("UBSVGRichText") )
		{
			// UBSVGArea에 값 Add
			if( _dataType.equals("1") || _dataType.equals("3") )
			{
				_propList.put("src", _propList.get("text"));
				_propList.remove("text");
			}
		}
		
		
		//DATA Type에 따른 처리 추가 2019-06-03 
		if( _propList.containsKey("dataType") )	
		{
			_propList.put("angle" , _propList.get("rotation") );
			String	_servicesUrl = "";
			
			_dataType=_propList.get("dataType").toString();
			String columnStr =  _propList.get("column").toString();
			String dataTypeStr = _propList.get("dataType").toString();
			_dataID = _propList.get("dataSet").toString();
			
			if( dataTypeStr.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list = _data.get( _dataID );
				Object _dataValue = "";
				if( _list != null ){
					if( _dCnt < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(_dCnt);
						
						_dataValue = _dataHm.get( columnStr );
					}
				}
				
				// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐.. 
				if(_className.equals("UBSVGArea") ){
					
					if(_dataValue==null || _dataValue.toString().equals("")) return null;
					
					String _tmpDataValue = _dataValue.toString();
					if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
					{
						_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
					}
					
					boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
					boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
					boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
					
					if(_bSVG)
					{
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList );	
					} 
					else
					{
						boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
						if(!_bHasHtmlTag)
							_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
							
						_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							
					}
					log.debug("13168-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _dataValue.toString() + "]");
					
					_dataValue = _dataValue.toString().replace(" ", "%20");
					
					if( !_dataValue.toString().equals("") )
					{
						_propList.put("data",  URLEncoder.encode(_dataValue.toString(), "UTF-8"));
					}
					else
					{
						return null;
					}
					
					
				}
				else if( _className.equals("UBSVGRichText") )
				{
					// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
					if( _dataValue != null && _dataValue.toString().equals("") == false )
					{
						_propList = convertUBSvgItem(_dataValue,_propList);
						
						if(_propList == null ) return null;
					}
				}
				else if("UBCheckBox".equals(_className) )
				{
					String _selectedText=_propList.get("selectedText").toString();
					//String _deSelectedText=_propList.get("deSelectedText").toString();
					if( _dataValue != null && _selectedText.equalsIgnoreCase(_dataValue.toString()) ){
						_propList.put("selected", "true");
					}else{
						_propList.put("selected", "false");	
					}
					
				}
				else if( _className.equals("UBImage")  || _className.equals("UBSignature") || _className.equals("UBPicture") || _className.equals("UBTextSignature") )
				{
					String _url="";
					String _txt = "";
					if( _dataValue != null ){
						_txt=_dataValue.toString();
					}
					
					_servicesUrl = convertImageData( _txt , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
					_propList.put("src", _servicesUrl);
					
					_propList.put("src", _servicesUrl);
				}
				else
				{
					_propList.put("text", _dataValue == null ? "" : _dataValue);
					
					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					_propList.put("tooltip", _propList.get("text"));
				}
				
			}
			else if( dataTypeStr.equals("2"))
			{
				// rowIndex : 현재 Row Index
				// dataSet : 그룹핑된 데이터셋
				if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
					
					ArrayList<HashMap<String, Object>> _pList = _data.get( _dataID );
					String _datasetColumnName = columnStr;
					mFunction.setDatasetList(_data);
					mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
					mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
					mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
					mFunction.setOriginalDataMap(mOriginalDataMap);
					
					String _fnValue;
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_systemFunction , _dCnt,_totalPageNum,_currentPageNum, -1 , -1, "" );
					}else{
						_fnValue = mFunction.function(_systemFunction,_dCnt,_totalPageNum,_currentPageNum, -1 , -1 );
					}
					

					if( _className.equals("UBSVGRichText") )
					{
						if( _fnValue != null && _fnValue.equals("") == false )
						{
							// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
							_propList = convertUBSvgItem(_fnValue,_propList);
							
							if(_propList == null ) return null;
						}

					}
					else if( _className.equals("UBImage") || _className.equals("UBSignature") || _className.equals("UBPicture") || _className.equals("UBTextSignature") ) {
						
						if( _fnValue != null )
						{
							_servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
							
							_propList.put("src", _servicesUrl);
						}
						
					}
					
					else if(_className.equals("UBCheckBox") ) {
						_propList.put("selected", _fnValue);
					}
					else
					{
						_propList.put("text", _fnValue == null ? "" : _fnValue);
					}
				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			else if( dataTypeStr.equals("3"))
			{
				String _txt = _propList.get("text").toString();
				
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				String _fnValue = "";
				
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					if( _inOf != 0 ){
						
						// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
						if(_className.equals("UBSVGArea")  ){
							
							String _tmpDataValue = String.valueOf(_txt);
							if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
							{
								_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
							}
							
							boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
							boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
							boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
							
							String _svgTag = null;
							if(_bSVG)
							{
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							}
							else
							{
								boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
								if(!_bHasHtmlTag)
									_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
									
								_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
									
							}
													
							log.debug("13315-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _svgTag + "]");
							
							_svgTag = _svgTag.toString().replace(" ", "%20");
							if( !_svgTag.equals("") )
							{
								_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8"));
							}
							else
							{
								return null;
							}
							
							_txt = "";
						}
						else if( _className.equals("UBSVGRichText") )
						{
							if( _txt != null && _txt.equals("") == false )
							{
//								_propList = convertUBSvgItem( _propList.get("text").toString(), _propList);
								_propList = convertUBSvgItem( _txt, _propList);
								
								if(_propList == null ) return null;
							}
							
							_txt = "";
						}
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							//_fnValue = mFunction.testFN(_txt,_dCnt,_totalPageNum,_currentPageNum, -1 , -1 , "" );
							_fnValue = _txt;
						}else{
							_fnValue = mFunction.function(_txt,_dCnt,_totalPageNum,_currentPageNum, -1 , -1 );
						}
						
					}else{

						int _keyIndex=_txt.lastIndexOf("}");
						_pKey = _txt.substring(_inOf + 7 , _keyIndex);

						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						_fnValue = _pList.get("parameter");
					}
					
					//
					if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
						_fnValue = URLDecoder.decode(_fnValue.replace(" ", "%20"), "UTF-8");
						
						_servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
						_propList.put("src", _servicesUrl);
					}
					else
					{
						if( _fnValue.equals("undefined"))
						{
							_propList.put("text", "");
						}
						else
						{
							_propList.put("text", _fnValue);
						}
					}

				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			
		}
		
		
		///////////////////////////////// 추가 완료 
		
		
		ArrayList<String> _ubfunctionList = new ArrayList<String>();
		for(int _ubfxIndex = 0; _ubfxIndex < _ubfunction.getLength(); _ubfxIndex++)
		{
			Element _ubfxItem = (Element) _ubfunction.item(_ubfxIndex);

			String _name = _ubfxItem.getAttribute("property");
			String _value = _ubfxItem.getAttribute("value");
			
			_value = URLDecoder.decode(_value, "UTF-8");
			mFunction.setDatasetList(_data);
			mFunction.setParam(_param);
			
			String _fnValue;
			
			if( mFunction.getFunctionVersion().equals("2.0") ){
				_fnValue = mFunction.testFN(_value,_dCnt, _totalPageNum , _currentPageNum,-1,-1, "" );
			}else{
				_fnValue = mFunction.function(_value,_dCnt, _totalPageNum , _currentPageNum,-1,-1);
			}
			
			
			
			_fnValue = _fnValue.trim();
			
			if( _name.equals("text") || _fnValue.equals("") == false)
			{
//				if(_name.indexOf("Color") != -1 )
//				{
//					_propList.put((_name + "Int"), mPropertyFn.changeColorHexToInt(_fnValue) );
//				}
//				_propList.put(_name, _fnValue);
//				
//				// color 속성은 color + Int 속성을 넣어줘야 한다.
//				if( _name.contains("Color") ){
//					_propList.put((_name + "Int"), common.getIntClor(_fnValue) );
//				}
				
				_propList = convertUbfxStyle(_name, _fnValue, _propList );
				
			}
			if(isExportType.equals("PRINT") || isExportType.equals("PDF") ) _ubfunctionList.add( _name + "§" + _value );
			
		}
		if( _ubfunctionList.size() > 0 ) _propList.put("ubfunctionList", _ubfunctionList);
		
		if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
			String _formatValue="";
			String _excelFormatterStr = "";
			
			if( _dataType.equalsIgnoreCase("1") ){
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				if( _list == null || _dCnt >= _list.size()) 
				{
					_formatValue = "";
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					_formatValue = _dataValue != null ? _dataValue.toString() : "";
				}
			}else{
				_formatValue = _propList.get("text").toString();
			}
			try {
				
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					_excelFormatterStr = _nation  + "§" + _align;
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					_excelFormatterStr = _formatString;
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					_excelFormatterStr = _mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal;
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
                else if( _formatter.equalsIgnoreCase("CustomDate") )
				{
					_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
					_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
				}
				
			} catch (ParseException e) {
//				e.printStackTrace();
			}
			
			if( isExportType.equals("EXCEL") && _excelFormatterStr.equals("") == false && common.getPropertyValue("excelExport.useFormatter") != null && common.getPropertyValue("excelExport.useFormatter").equals("true") ) 
			{
				_propList.put("EX_FORMATTER", _formatter);
				_propList.put("EX_FORMAT_DATA_STR", _excelFormatterStr);
				_propList.put("EX_FORMAT_ORIGINAL_STR", _propList.get("text").toString() );
			}
			
			_propList.put("text", _formatValue);
			
			
			// format이 label band에서 안들어간다.
			_propList.put("formatter", _formatter);
			_propList.put("mask", _mask);
			_propList.put("decimalPointLength", _decimalPointLength);
			_propList.put("useThousandComma", _useThousandComma);
			_propList.put("isDecimal", _isDecimal);
			_propList.put("formatString", _formatString);
			_propList.put("nation", _nation);
			_propList.put("inputFormatString", _inputForamtString);
			_propList.put("outputFormatString", _outputFormatString);
		}
		
		
		
		
		
		if( !_formatterE.equalsIgnoreCase("null") && !_formatterE.equalsIgnoreCase("") ){
			String _formatValue="";
			if( _dataType.equalsIgnoreCase("1") ){
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				if( _dCnt >= _list.size()) 
				{
					_formatValue = "";
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					_formatValue = _dataValue != null ? _dataValue.toString() : "";
				}
			}else{
				_formatValue = _propList.get("text").toString();
			}
			try {
				
				if( _formatterE.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nationE, _alignE, _formatValue);
				}else if( _formatterE.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatStringE, _formatValue);
				}else if( _formatterE.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberEditFormat(_mask, _decimalPointLengthE, _useThousandCommaE, _isDecimalE, _formatValue);
				}else if( _formatterE.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringEditFormat(_maskE, _formatValue,"*");
				}
			} catch (ParseException e) {
//				e.printStackTrace();
			}
			_propList.put("textE", _formatValue);
			
			
			_propList.put("editItemFormatter", _formatterE);
			_propList.put("eformmask", _maskE);
			_propList.put("eformdecimalPointLength", _decimalPointLengthE);
			_propList.put("eformuseThousandComma", _useThousandCommaE);
			_propList.put("eformisDecimal", _isDecimalE);
			_propList.put("eformformatString", _formatValue);
		}
		
		
		// resizeFont���� true�ϰ�� ó��
		if( _resizeFont && _propList.containsKey("text") && !"".equals(_propList.get("text")) )
		{
			float _fontSize 	= Float.valueOf( _propList.get("fontSize").toString() );
			String _fontFamily 	= _propList.get("fontFamily").toString();
			String _fontWeight 	= _propList.get("fontWeight").toString();
			
			float _padding = (_propList.containsKey("padding"))? Float.valueOf( _propList.get("padding").toString()):3;
			
			float _maxBorderSize = 0;
			if(_propList.containsKey("borderWidths"))
			{
				ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
				
				for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
					if(_maxBorderSize < _borderWidths.get(_bIndex))
					{
						_maxBorderSize = _borderWidths.get(_bIndex);
					}
				}
				_padding = _maxBorderSize + _padding;
			}
			
			float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
			
			_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
			_propList.put("fontSize",  _fontSize);

		}
		

		// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
		Boolean _hasTooltip=false;
		if( _propList.containsKey("tooltip") ){
			if( _propList.get("tooltip") != null && !(_propList.get("tooltip").equals("")) ){
				_hasTooltip = true;
			}
		}
		
		if( _hasTooltip == false ){
			if(_propList.containsKey("text"))
				_propList.put("tooltip", _propList.get("text").toString());
		}
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _propList.get("text").toString());
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
			    		_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//_barcodeValue = URLDecoder.decode(_barcodeValue, "UTF-8");
				
				if( _barcodeValue == null || _barcodeValue.equals("")) return null;
				else _propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 8 ){
					_barcodeSrc="";
				}
				/*
				else if( _barcode_type.equalsIgnoreCase("itf14") && _barcodeData.length() != 14 ){
					_barcodeSrc="";
				}
				*/
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				//_propList.put("src" , _barcodeSrc );
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//_barcodeValue = URLDecoder.decode(_barcodeValue, "UTF-8");
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart")|| _className.equals("UBTaximeter")|| _className.equals("UBCandleStickChart")|| _className.equals("UBPlotChart") || _className.equals("UBRadarChart") )
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			if(_className.equals("UBTaximeter")){
				PARAM = getChartParamToElement4Taximeter(_childItem );	
				PARAM+=","+_dCnt;
			}else if(_className.equals("UBLineChart")){
				PARAM = getLineChartParamToElement(_childItem );	
			}else if(_className.equals("UBRadarChart")){
				PARAM = getChartParamToElement4Radar(_childItem );	
			}else if(_className.equals("UBCandleStickChart")){
				PARAM = getCandleChartParamToElement(_childItem );	
			}else if(_className.equals("UBColumnChart")){
				PARAM = getColumnChartParamToElement(_childItem );	
			}else if(_className.equals("UBBarChart")){
				PARAM = getColumnChartParamToElement(_childItem );	
			}else if(_className.equals("UBPieChart")){
				PARAM = getPieChartParamToElement(_childItem );
			}else{
				PARAM = getChartParamToElement(_childItem );
			}
			
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToElement2(_childItem );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			else if(_className.equals("UBTaximeter"))
			{
				_propList.put("type" , "TaximeterCtl");
				IMG_TYPE = "taximeter";
			}
			else if(_className.equals("UBCandleStickChart"))
			{
				_propList.put("type" , "candleChartCtl");
				IMG_TYPE = "candle";
			}
			else if(_className.equals("UBPlotChart"))
			{
				_propList.put("type" , "plotChartCtl");
				IMG_TYPE = "plot";
			}
			else if(_className.equals("UBRadarChart"))
			{
				_propList.put("type" , "radarChartCtl");
				IMG_TYPE = "radar";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				//String [] arrDataId = _dataID.split(":");
				String [] arrDataId =_datasets;
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = _data.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _model_type + "&PARAM=" + PARAM + "&FILE_NAME=" + this.mChartDataFileName + "&PROJECT_NAME=" + PROJECT_NAME + "&FORM_ID=" + FOLDER_NAME + "&DATASET=" + _dataID );
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			{
				ArrayList<HashMap<String, Object>> _list = _data.get( _dataID );			
				/*
				if(!mChartData.containsKey(_itemId) && _list != null && _list.size() > 0)
				{
					mChartData.put(_dataID, _list);
				}
				*/
				
				//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _model_type + "&PARAM=" + PARAM + "&FILE_NAME=" + this.mChartDataFileName + "&PROJECT_NAME=" + PROJECT_NAME + "&FORM_ID=" + FOLDER_NAME + "&DATASET=" + _dataID );
			
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();

			    	try {
			    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//_barcodeValue = URLDecoder.decode(_chartValue, "UTF-8");
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		else if( "UBStretchLabel".equals(_className) )
		{
			// StretchLabel일때 height계산하여 height를 업데이트하고 
			// text를 줄바꿈 처리하고 진행
			_propList = convertStrechLabel(_propList);
			
		}
		
		if("UBGraphicsRectangle".equals(_className) || "UBGraphicsCircle".equals(_className) || "UBGraphicsGradiantRectangle".equals(_className) )
		{
			_propList.put("angle" , _propList.get("rotation") );
			_propList.put("stroke" , _propList.get("borderColor").toString() );
			_propList.put("strokeWidth" , Integer.valueOf( _propList.get("borderThickness").toString() ) );
			_propList.put("scaleX" , 1);
			_propList.put("scaleY" , 1);
			
			_propList.put("width", Float.valueOf(_propList.get("width").toString()));
			_propList.put("height", Float.valueOf(_propList.get("height").toString()));
			
			if( "UBGraphicsCircle".equals(_className) )
			{
				_propList.put("radius", Float.valueOf( Float.valueOf(_propList.get("width").toString())/2 ));
				_propList.put("scaleY", Float.valueOf( Float.valueOf(_propList.get("height").toString())/Float.valueOf(_propList.get("width").toString()) ));
			}
			
		}
		
//		if("UBRotateLabel".equals(_className))
//		{
//			_propList.put("rotate" , _propList.get("rotation") );
//		}
		
		if( labelProjectFlag == false && _propList.containsKey("visible") && _propList.get("visible").equals("false"))
		{
			return null;
		}
		
		// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
		if( _propList.containsKey("rowId") && (_propList.get("dataType") == null || !( _propList.get("dataType").equals("1") || _propList.get("dataType").equals("2") ) ) )
		{
//			_propList.put("rowId", _currentPageNum);
		}
		
		_propList.put("className" , _className );
		_propList.put("id" , _itemId );
		
		// radioButtonGroup 
		if( _className.equals("UBRadioBorder") ){
			Boolean _isSelected = radiobuttonHandler(_propList);
			_propList.put("selected", _isSelected);
		}else if( _className.equals("UBRadioButtonGroup") ){
			radiobuttonGroupHandler(_propList);
		}
		
		// 아이템의 사용 여부 확인 
		_propList = ItemPropertyProcess.checkedItemProperties(_propList);
		
		return _propList;
	}
	
	
	

	public  ArrayList<HashMap<String, Object>> convertElementTableToItem(Element _childItem, int _dCnt,  HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param, float _cloneX, float _cloneY, float _updateY,  ArrayList<HashMap<String, Object>> _objects, int _totalPageNum , int _currentPageNum, boolean labelProjectFlag ) throws UnsupportedEncodingException, XPathExpressionException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);

		String _itemId = _childItem.getAttribute("id");
		String _className = _childItem.getAttribute("className");

		NodeList _ubfunction = _childItem.getElementsByTagName("ubfunction");
		NodeList _tableUbfunction = null;
		
		HashMap<String, Object> _propList = new HashMap<String, Object>();

		String _dataTypeStr = "";
		String _dataColumn = "";
		String _dataID = "";
		String _model_type = "";
		String _barcode_type = "";
		
		// formatter variables
		String _formatter="";
		String _nation="";
		String _formatterAlign = "";
		String _align="";
		String _dataType="";
		String _mask="";
		String _inputForamtString = "";
		String _outputFormatString = "";
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
//		XPath _xpath = XPathFactory.newInstance().newXPath(); 
		
		
		// edit format variables
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";
		
		
		_propList = mItemPropVar.getItemName("UBLabel");
		NodeList _tablePropertys;
		NodeList _tableMaps;
		NodeList _tableMapDatas;
		NodeList _cellPropertys;
		Element _ItemProperty;
		Element _tableMapItem;
		Element _cellItem;
		String _propertyName = "";
		String _propertyValue = "";
		String _propertyType = "";
		String _itemDataSetName = "";
		HashMap<String, Object> tableProperty = new HashMap<String, Object>();
		HashMap<String, Object> itemProperty = new HashMap<String, Object>();
		HashMap<String, Object> tableMapProperty = new HashMap<String, Object>();
		
		ArrayList<Object> borderAr = null;
		
		int colIndex = 0;
		int rowIndex = 0;
		
		int l = 0;
		int i = 0;
		int j = 0;
		int k = 0;
		float updateX = 0;
		float updateY = 0;
		
		mFunction.setDatasetList(_data); 
		mFunction.setParam(_param);
		
		boolean _newTalbeFlag = false;
		
		boolean _resizeFont = false;
		
		_tablePropertys = _childItem.getChildNodes();
		int _tablePropertysLength = _tablePropertys.getLength();
		
		String _includeLayoutType = "";
		
		ArrayList<HashMap<String, Object>> _ubApprovalAr = null;
		Node _tablePropertyesItem;
		
		for ( l = 0; l < _tablePropertysLength; l++) {
			
			_tablePropertyesItem = _tablePropertys.item(l);
			
			if( _tablePropertyesItem instanceof Element )
			{
				_ItemProperty = (Element) _tablePropertyesItem;
				if(_ItemProperty.getTagName().equals("property"))
				{
					_propertyName = _ItemProperty.getAttribute("name");
					_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
					_propertyType = _ItemProperty.getAttribute("type");
					tableProperty.put( _propertyName, _propertyValue );
				}
				
				if( _tablePropertyesItem.getNodeName().equals("ubfx") )
				{
					_tableUbfunction = _ItemProperty.getElementsByTagName("ubfunction");
				}
			}
		}
		
		// visible 값이 false일때 cell생성하지 않도록 수정
		if( tableProperty.containsKey("visible") && tableProperty.get("visible").toString().equals("false")) return _objects;

		// 신규 테이블 여부를 판단
		if( tableProperty.containsKey("version") &&  tableProperty.get("version").equals(ItemConvertParser.TABLE_VERSION_NEW))
		{
			_newTalbeFlag = true;
		}
		if( tableProperty.containsKey("includeLayoutType"))
		{
			_includeLayoutType = tableProperty.get("includeLayoutType").toString();
		}
		
		NodeList _tableML = _childItem.getElementsByTagName("table");
		Element _tableM = (Element) _tableML.item(0);
		
		if(_newTalbeFlag) _tableMaps = _tableM.getElementsByTagName("tableMap");
		else _tableMaps = _tableM.getElementsByTagName("row");
		
		int _tableMapsLength = _tableMaps.getLength();
		int _tableMapDatasLength = 0;
		
		ArrayList<ArrayList<HashMap<String, Object>>> _allTableMap = new ArrayList<ArrayList<HashMap<String,Object>>>();
		//TableMap을 이용하여 TableMapData로 모든 테이블의 맵을 담아두기
		// 각 맵별BorderString담아두기작업
		
		ArrayList<Float> _xAr = new ArrayList<Float>();	//각 row별 Height값 담아두기
		ArrayList<Float> _yAr = new ArrayList<Float>();	//각 row별 Height값 담아두기
		float _pos = 0;
		float _mapW = 0;	// 테이블맵의 X좌료
		float _mapH = 0;	// 테이블 맵의 Y좌표		
		ArrayList<Integer> _exColIdx = new ArrayList<Integer>();		
		ArrayList<Float> _newXValue = new ArrayList<Float>();//최종  컬럼의 x값
		
		float _removeCellW = 0;
		float _convertW = 0;
		ArrayList<Float> _tblColumnWdithAr = new ArrayList<Float>();
		float _cellW = 0;
		
		ArrayList<Integer> _lastCellIdx = new ArrayList<Integer>();
		Element _cell = null;

		if( _newTalbeFlag )
		{	
			
			for ( k = 0; k < _tableMapsLength; k++) {
				
				_tableMapItem = (Element) _tableMaps.item(k);
				_tableMapDatas = _tableMapItem.getElementsByTagName("tableMapData");
				_tableMapDatasLength = _tableMapDatas.getLength();
				
				ArrayList<HashMap<String, Object>> _tableMapRow = new ArrayList<HashMap<String,Object>>();
				
				for ( l = 0; l < _tableMapDatasLength; l++) {	
					itemProperty = new HashMap<String, Object>();
					
					_itemDataSetName = "";
					_cellPropertys = ((Element) _tableMapDatas.item(l)).getElementsByTagName("property");						
					
					// 테이블맵의 Property담기
					int _cellPropertysLength = _cellPropertys.getLength();
					for ( j = 0; j < _cellPropertysLength; j++) {
						_ItemProperty = (Element) _cellPropertys.item(j);
						if(_ItemProperty.getParentNode().getNodeName().equals("tableMapData"))
						{
							_propertyName = _ItemProperty.getAttribute("name");
							_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
							_propertyType = _ItemProperty.getAttribute("type");
							
							itemProperty.put(  _propertyName, _propertyValue );
						}else if(_ItemProperty.getAttribute("name").equals("includeLayout")){
							itemProperty.put(  _ItemProperty.getAttribute("name"), URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") );
						}
					}			
					
					if( k == 0 )
					{											
							// X좌표 담기 (소숫점 1자리까지만 )
//						_pos = (float) Math.floor( Float.valueOf( itemProperty.get("columnWidth").toString() )*10 ) /10 ;
							_pos = (float) Math.round( Float.valueOf( itemProperty.get("columnWidth").toString() ) );
							_xAr.add( _mapW  );
							_mapW = _mapW + _pos;						
					}	
					_cell = (Element)((Element) _tableMapDatas.item(l)).getElementsByTagName("cell").item(0);
					itemProperty.put("cell", _cell);
					_tableMapRow.add(itemProperty);
				}
				
				if( k == 0 )
				{
					_xAr.add( _mapW  );		// 마지막 Width 값 담기
				}
				
//				_pos = (float) Math.floor( Float.valueOf( itemProperty.get("rowHeight").toString() )*10 ) /10 ;
				_pos = (float) Math.round( Float.valueOf( itemProperty.get("rowHeight").toString() ) );
				_yAr.add( _mapH  );
				_mapH = _mapH + _pos;				
				_allTableMap.add(_tableMapRow);
			}			
			_yAr.add( _mapH  );		// 마지막 Height 값 담기		
			
			boolean  _ubFxChkeck = true;
			
			
			//column visible 속성관련 추가 작업 ================================================================================================
			//1. 첫번째 Row에서 column visible 속성찾아 array 추가
			for ( l = 0; l < _allTableMap.get(0).size(); l++) {
				tableMapProperty = _allTableMap.get(0).get(l);	
				if(tableMapProperty.containsKey("includeLayout") &&  tableMapProperty.get("includeLayout").toString().equals("false")){	
					_ubFxChkeck = false;
				}else{
					_ubFxChkeck = UBIDataUtilPraser.getUBFxChkeck(_data, (Element)tableMapProperty.get("cell"), _param, "includeLayout" , _dCnt , _totalPageNum , _currentPageNum, mFunction );
				}
				if(!_ubFxChkeck){									
					_exColIdx.add(l);					
					if(!tableMapProperty.get("colSpan").toString().equals("1") && _exColIdx.indexOf(l) != -1){
						int _colSpan = Integer.parseInt(tableMapProperty.get("colSpan").toString());
						for(int tmp = l+1; tmp < l+_colSpan; tmp++){
							_exColIdx.add(tmp);							
						}
					}
					
					_removeCellW = _removeCellW + Float.valueOf( tableMapProperty.get("width").toString() );
				}
				else if(_exColIdx.indexOf(l) == -1)
				{
					_convertW = _convertW + Float.valueOf( tableMapProperty.get("columnWidth").toString() );
				}
				
				if( _includeLayoutType.equals( GlobalVariableData.M_TABLE_INCLUDE_LAYOUT_TYPE_AUTO ) || _ubFxChkeck )
				{
					_tblColumnWdithAr.add(Float.valueOf( tableMapProperty.get("columnWidth").toString() ) );
				}
				else
				{
					_tblColumnWdithAr.add( 0f );
				}
			}	

			//2. column visible array에 해당하는 컬럼들을 row별로 돌면서 merge여부에 따라 해당 인덱스 추가
			if(_exColIdx.size() > 0){
				for ( l = 0; l < _allTableMap.get(0).size()-1; l++) {
					for ( k = 1; k < _allTableMap.size(); k++) {					
						tableMapProperty = _allTableMap.get(k).get(l);
						if(!tableMapProperty.get("colSpan").toString().equals("1") && _exColIdx.indexOf(l) != -1){
							int _colSpan = Integer.parseInt(tableMapProperty.get("colSpan").toString());
							for(int tmp = l; tmp < l+_colSpan; tmp++){
								if(_exColIdx.indexOf(tmp) == -1){
									_exColIdx.add(tmp);				
								}
							}
						}
					}
				}
				
				
				if( _includeLayoutType.equals( GlobalVariableData.M_TABLE_INCLUDE_LAYOUT_TYPE_AUTO ) )
				{
					float _addW = 0;
					float _lastW = _removeCellW;
					int _lastAddPosition = 0;
					double _tempW = 0;
					double _addWFloat = 0;
					
					for ( l = 0; l < _allTableMap.get(0).size(); l++)
					{
						tableMapProperty = _allTableMap.get(0).get(l);	
						
						if( _exColIdx.indexOf(l) != -1 )
						{
							_tblColumnWdithAr.set( l,0f );
							
							if( l == _allTableMap.get(0).size() -1 )
							{
								_tblColumnWdithAr.set( _lastAddPosition, _tblColumnWdithAr.get(_lastAddPosition) + _lastW );
							}
						}
						else
						{
							if( l == _allTableMap.get(0).size() -1 )
							{
								if( _tempW > 1 )
								{
									_lastW = _lastW + 1;
									_tempW = _tempW-1;
								}
								
								_tblColumnWdithAr.set( l,  Float.valueOf( tableMapProperty.get("columnWidth").toString() ) + _lastW );
							}
							else
							{
								_lastAddPosition = l;
								_addWFloat = _removeCellW * ( Float.valueOf( tableMapProperty.get("columnWidth").toString() ) / _convertW );
								_tempW = _tempW +( _addWFloat - Math.floor(_addWFloat) );
								
								_addW = Double.valueOf( Math.floor( _addWFloat )).floatValue();
								
								if( _tempW > 1 )
								{
									_addW = _addW + 1;
									_tempW = _tempW-1;
								}
								_tblColumnWdithAr.set( l,_tblColumnWdithAr.get(l) + _addW );
								_lastW = _lastW - _addW;
							}
							
						}
					}
					
					
				}
				
				// table map column Width업데이트 / cell width 업데이트
				_cellW = 0;
				for ( k = 0; k < _allTableMap.size(); k++) {	
					ArrayList<HashMap<String, Object>> _tableRow = _allTableMap.get(k);
					HashMap<String, Object> _tableMap = null;
					for ( l = 0; l < _tableRow.size(); l++) {
						_cellW = 0;
						_tableMap =  _tableRow.get(l);
						_tableMap.put("columnWidth", _tblColumnWdithAr.get(l));
					}
				}
			}
			
			for ( k = 0; k < _allTableMap.size(); k++) {	
				ArrayList<HashMap<String, Object>> _tableRow = _allTableMap.get(k);
				HashMap<String, Object> _tableMap = null;
				_lastCellIdx.add(0);
				
				for ( l = 0; l < _tableRow.size(); l++) {
					_tableMap = _tableRow.get(l);
					
					//if(_tableMap.get("cell") != null && ((Element) _tableMap.get("cell")).getElementsByTagName("property").getLength() > 0 )
					if( _tableMap.get("status").equals("NORMAL")|| _tableMap.get("status").equals("MS")||  _tableMap.get("status").equals("MR") )
					{
						if( _exColIdx.indexOf(l) == -1 )
						{
							_lastCellIdx.set(k, l);
						}
					}
				}
			}
			
			Collections.sort(_exColIdx);

			
			//3.새로운 xValue값 저장;
			float _tempWidth = 0;		
			_newXValue.add(0f);	
			for ( l = 0; l < _allTableMap.get(0).size()-1; l++) {
				tableMapProperty = _allTableMap.get(0).get(l);				
				//if(_exColIdx.indexOf(l) == -1){	
					_tempWidth = _tempWidth + Float.parseFloat( tableMapProperty.get("columnWidth").toString());
					_newXValue.add(_tempWidth);
				//}
				
			}			
			
			//4. _allTableMap에서 column visible에 해당하는 colunm을 모두 제거한다.
//			for ( k = 0; k < _allTableMap.size(); k++) {//row
//				for ( l = _exColIdx.size()-1; l >= 0; l--) {//column									
//					_allTableMap.get(k).remove(Integer.parseInt(_exColIdx.get(l).toString()));
//				}
//			}	
		}
		else
		{
			float _argoHeight = 0;
			int _rowPropertyLength = 0;
			for ( k = 0; k < _tableMapsLength; k++) {
				
				_tableMapItem = (Element) _tableMaps.item(k);
				
				_tableMapDatas = _tableMapItem.getElementsByTagName("cell");
				_tableMapDatasLength = _tableMapDatas.getLength();
				
				ArrayList<HashMap<String, Object>> _tableMapRow = new ArrayList<HashMap<String,Object>>();
				
				for ( l = 0; l < _tableMapDatasLength; l++) {
					
					itemProperty = new HashMap<String, Object>();
					_itemDataSetName = "";
					_cellPropertys = ((Element) _tableMapDatas.item(l)).getElementsByTagName("property");
					// 테이블맵의 Property담기
					int _cellPropertysLength = _cellPropertys.getLength();
					for ( j = 0; j < _cellPropertysLength; j++) {
						_ItemProperty = (Element) _cellPropertys.item(j);
						if(_ItemProperty.getParentNode().getNodeName().equals("cell"))
						{
							_propertyName = _ItemProperty.getAttribute("name");
							_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
							_propertyType = _ItemProperty.getAttribute("type");
							
							if( (_propertyName.equals("colSpan") ||  _propertyName.equals("rowSpan")) && _propertyValue.equals("0") )
							{
								_propertyValue = "1";
							}
							
							itemProperty.put(  _propertyName, _propertyValue );
						}
					}
					itemProperty.put(  "y", _argoHeight );	
					_tableMapRow.add(itemProperty);
				}
				
				NodeList _rowPropertyList = _tableMapItem.getElementsByTagName("property");
				_rowPropertyLength = _rowPropertyList.getLength();
				for( l = 0; l < _rowPropertyLength; l++) {
					_ItemProperty = (Element) _rowPropertyList.item(l);
					if(_ItemProperty.getParentNode().getNodeName().equals("row"))
					{
						if( _ItemProperty.getAttribute("name").equals("height"))
						{
							_argoHeight = _argoHeight + Float.valueOf(_ItemProperty.getAttribute("value").toString());
							break;
						}
					}
				}
				
				_allTableMap.add(_tableMapRow);
			}
		}
		// 각 맵별BorderString담아두기작업 종료
		
		for ( k = 0; k < _allTableMap.size(); k++) {//row
			
			colIndex = 0;
			
			//_tableMapItem = (Element) _tableMaps.item(k);
			
//			if(_newTalbeFlag)
//			{
//				_tableMapDatas = _tableMapItem.getElementsByTagName("tableMapData");
//			}
//			else
//			{
//				_tableMapDatas = _tableMapItem.getElementsByTagName("cell");
//			}		
			
			for ( l = 0; l < _allTableMap.get(k).size(); l++) {//column
				
				_resizeFont = false;
				
				tableMapProperty = _allTableMap.get(k).get(l);
				
				itemProperty = new HashMap<String, Object>();
				
				_itemDataSetName = "";
				_cellItem = (Element)tableMapProperty.get("cell");
				
//				if(_newTalbeFlag)
//				{
//					_cellItem = (Element)tableMapProperty.get("cell");
//				}
//				else
//				{
//					_cellItem = (Element) _tableMapDatas.item(l);
//				}
				
				if(_cellItem == null)
					continue;
				
				_cellPropertys = (NodeList) _cellItem.getElementsByTagName("property");			
				
				if( !_newTalbeFlag || tableMapProperty.get("status").toString().equals("MC") == false )
				{
					colIndex = colIndex + Integer.valueOf( tableMapProperty.get("colSpan").toString() );
				}
				
				if(_cellPropertys.getLength() == 0 ) continue;			
				
				
				_propList = mItemPropVar.getItemName("UBLabel");
				if(_propList == null ) return null;
				
				// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
				if( _propList.containsKey("rowId") )
				{
					_propList.put("rowId", _dCnt);
				}
				
				
				rowIndex =  k + Integer.valueOf( tableMapProperty.get("rowSpan").toString() );
				
				Matcher _matcher = null;
				Pattern _patt = null;	
				String _replaceType = "";
				//Merge Cell인경우 right, bottom 의 borderString 교체
				if( Integer.valueOf( tableMapProperty.get("colSpan").toString())>1){
					String _csRightBorder = _allTableMap.get(k).get(colIndex-1).get("borderString").toString();	
					_replaceType = "";					
					_patt = Pattern.compile("borderRight,borderType:[^,]+");
					_matcher =  _patt.matcher(_csRightBorder);
					if(_matcher.find()){
						_replaceType = _csRightBorder.substring(_matcher.start(), _matcher.end());
					}			
					_matcher =  _patt.matcher(tableMapProperty.get("borderString").toString());
					tableMapProperty.put("borderString", _matcher.replaceFirst(_replaceType));					
				}
				
				if(Integer.valueOf( tableMapProperty.get("rowSpan").toString())>1){
					String _rsBottomBorder = _allTableMap.get(rowIndex-1).get(l).get("borderString").toString();					
					_replaceType = "";					
					_patt = Pattern.compile("borderBottom,borderType:[^,]+");
					_matcher =  _patt.matcher(_rsBottomBorder);
					if(_matcher.find()){
						_replaceType = _rsBottomBorder.substring(_matcher.start(), _matcher.end());
					}			
					_matcher =  _patt.matcher(tableMapProperty.get("borderString").toString());
					tableMapProperty.put("borderString", _matcher.replaceFirst(_replaceType));	
				}
				
				
				
				// xml Item propertys
				for(int p = 0; p < _cellPropertys.getLength(); p++)
				{
					Element _propItem = (Element) _cellPropertys.item(p);

					if( _propItem.getParentNode().getNodeName().equals("cell") == false )
					{
						continue;
					}
					
					String _name = _propItem.getAttribute("name");
					String _value = _propItem.getAttribute("value");
					
					if(_propList.containsKey(_name))
					{
						if( _name.equals("fontFamily"))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							if(common.isValidateFontFamily(_value))
								_propList.put(_name, _value);
							else
								_propList.put(_name, "Arial");
						}
						else if( _name.indexOf("Color") != -1)
						{
							_propList.put((_name + "Int"), _value);
							_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value));
							_propList.put(_name, _value);
						}
						else if( _name.equals("lineHeight"))
						{
							//_value = "1.16"; //TODO LineHeight Test
							_value = _value.replace("%25", "");
							_value = String.valueOf((Float.parseFloat(_value)/100));		
							_propList.put(_name, _value);
						}
						else if( _name.equals("borderType"))
						{
							_propList.put(_name, _value);
						}
						else if( _name.equals("text"))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name, _value);
						}
						else if( _name.equals("prompt"))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name, _value);
						}
						else if( _name.equals("validateScript"))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name, _value);
						}
						else if( _name.equals("borderSide") )
						{
							ArrayList<String> _bSide = new ArrayList<String>();
							_value = URLDecoder.decode(_value, "UTF-8");
							if( !_value.equals("none") )
							{
								_bSide = mPropertyFn.getBorderSideToArrayList(_value);

								if( _bSide.size() > 0)
								{
									String _type = (String) _propList.get("borderType");
									_type = mPropertyFn.getBorderType(_type);
									_propList.put("borderType", _type);
								}

							}

							_propList.put(_name, _bSide);
						}
						else if( _name.equals("x"))
						{
							_propList.put(_name, _value);
							_propList.put("left", _value);
						}
						else if( _name.equals("y"))
						{
							_propList.put(_name, _value);
							_propList.put("top", _value);
						}
						else if( _name.equals("type") )
						{
							_model_type = _value;
							_propList.put(_name, _value);
						}
						else if( _name.equals("barcodeType") )
						{
							_barcode_type = _value;
							_propList.put(_name, _value);
						}
						else if(_name.equals("column"))
						{
							_propList.put(_name,URLDecoder.decode(_value, "UTF-8"));
							_dataColumn = URLDecoder.decode(_value, "UTF-8");
						}
						else if(_name.equals("dataSet"))
						{
							_dataID = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name,URLDecoder.decode(_value, "UTF-8"));
						}
						else if( _name.equals("ubHyperLinkUrl") )
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name, _value);
						}
						else if( _name.equals("ubHyperLinkText") )
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name, _value);
						}
						else if( _name.equals("scriptEvent") )
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name, _value);
						}
						else
						{
							_propList.put(_name, _value);
						}
					}
					else if(_name.equals("dataType"))
					{
						_dataType=_value;
						_propList.put(_name,_value);
					}
					
					else if( _name.equals("systemFunction") ){
						
						if( !_value.equalsIgnoreCase("null") && !_value.equalsIgnoreCase("") ){
							_value = URLDecoder.decode(_value, "UTF-8");
							mFunction.setDatasetList(_data);
							
							
							String _fnValue;
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								_fnValue = mFunction.testFN(_value.toString(),_dCnt, _totalPageNum , _currentPageNum , -1 , -1, "" );
							}else{
								_fnValue = mFunction.function(_value.toString(),_dCnt, _totalPageNum , _currentPageNum , -1 , -1);
							}
							
							
							_propList.put("text", _fnValue);
							_propList.put(_name, _value);
						}
					}
					else if( _name.equals("width"))
					{
						//_propList.put("x2", _value);
						_propList.put(_name,_value);
					}
					else if( _name.equals("height"))
					{
						//_propList.put("y2", _value);
						_propList.put(_name,_value);
					}
					else if(_name.equals("lineThickness"))
					{
						_propList.put("thickness", _value);
					}
					else if(_name.equals("column"))
					{
						_propList.put(_name,_value);
						_dataColumn = _value;
					}
					else if(_name.equals("dataSet"))
					{
						_dataID = _value;
						_propList.put(_name,_value);
					}
					else if(_name.equals("formatter"))
					{
						_formatter = _value;
						_propList.put("formatter", _formatter);
					}
					else if(_name.equals("editItemFormatter"))
					{
						_formatterE = _value;
						_propList.put("editItemFormatter", _formatterE);
					}
					else if(_name.equals("nation"))
					{
						_nation = URLDecoder.decode(_value, "UTF-8");
					}
					
					else if(_name.equals("align"))
					{
						_align=_value;
					}
					else if( _name.equals("mask") ){
						_mask = URLDecoder.decode(_value, "UTF-8");
					}
					
					else if( _name.equals("decimalPointLength") ){
						
						_decimalPointLength =  common.ParseIntNullChk(_value, 0);
					}				
					
					else if( _name.equals("useThousandComma") ){
						_useThousandComma = Boolean.parseBoolean(_value);
					}		
					else if( _name.equals("isDecimal") ){
						_isDecimal = Boolean.parseBoolean(_value);
					}		
					else if( _name.equals("formatString") ){
						_formatString = _value;
					}	
					else if( _name.equals("resizeFont") && "true".equals(_value)  )
					{
						_resizeFont = true;
					}				
						
//					else if(_name.equals("padding"))
//					{
//						_propList.put(_name, _value);
//					}
					
				}
				
				if( _className.toUpperCase().indexOf("LINE") == -1)
				{
					_propList.put("x1", _propList.get("x"));
					_propList.put("y1", _propList.get("y"));
					_propList.put("x2", _propList.get("width"));
					_propList.put("y2", _propList.get("height"));
				}
				
				// Item의 changeData가 있는지 확인
				if(mChangeItemList != null  )
				{
					_propList = convertChangeItemDataText( _currentPageNum ,_propList, "");
				}
				
				
				if( _dataType.equals("1") )
				{
					ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
					if( _list == null || _dCnt >= _list.size()) 
					{
						_propList.put("text", "");
					}
					else
					{
						HashMap<String, Object> _dataHm = _list.get(_dCnt);
						
						Object _dataValue = _dataHm.get(_dataColumn);
						
						_propList.put("text", _dataValue == null ? "" : _dataValue);
						
					}
					
				}
				else if( _dataType.equals("3"))
				{
					String _txt = _propList.get("text").toString();
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					
					if( _inOf != -1 )
					{
						mFunction.setParam(_param);
						_txt=mFunction.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							
							
							String _fnValue;
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								//_fnValue = mFunction.testFN(_txt,rowIndex,_totalPageNum,_currentPageNum, -1,-1 , "" );
								_fnValue = _txt;
							}else{
								_fnValue = mFunction.function(_txt,rowIndex,_totalPageNum,_currentPageNum, -1,-1 , "" );
							}
							
							
							_propList.put("text", _fnValue);
						}else{
							
							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);
							
							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);
							
							String _pValue = _pList.get("parameter");
							
							if( _pValue.equals("undefined"))
							{
								_propList.put("text", "");
							}
							else
							{
								_propList.put("text", _pValue);
							}
						}
						
					}
					
				}
				
				if(_exColIdx.size() > 0 ){
					_cellW = 0;
					if(!tableMapProperty.get("colSpan").toString().equals("1")){
						int _colSpan = Integer.parseInt(tableMapProperty.get("colSpan").toString());
						int _stidx= Integer.parseInt(tableMapProperty.get("columnIndex").toString());
						for(int tmp = _stidx; tmp < _stidx+_colSpan; tmp++){
							_cellW = _cellW +  _tblColumnWdithAr.get(tmp);
						}
					}
					else
					{
						_cellW = _tblColumnWdithAr.get(Integer.parseInt(tableMapProperty.get("columnIndex").toString()));
					}
					_propList.put("width",_cellW);
					if( _cellW == 0 ) continue;
				} 
				
				//cell 속성 완료 : x값 업데이트
				_propList.put("x", _newXValue.get(l));				
				
				boolean useRightBorder = false;
				boolean useBottomBorder = false;
				
				boolean useLeftBorder = false;
				boolean useTopBorder = false;
				
				ArrayList<Boolean> beforeBorderType = new ArrayList<Boolean>();
				boolean isChangeTop = false;
				boolean isChangeLeft = false;				
				boolean isChangeBottom = false;
				boolean isChangeRight = false;
				
				if(l>0)useLeftBorder = true;
				if(k>0)useTopBorder = true;
				
				String _topBorderStr = "";
				String _topBorderBefStr = "";
				String _leftBorderStr = "";
				String _leftBorderBefStr = "";
				String _bottomBorderStr = "";
				String _bottomBorderBefStr = "";
				String _rightBorderStr = "";
				String _rightBorderBefStr = "";				
				
				String _tempBorderStr = tableMapProperty.get("borderString").toString();				
				if(useLeftBorder){	//left border가 공유일 경우 top의 변경여부를 저장한다.					
					_patt = Pattern.compile("borderTop,borderType:[^,]+");
					_matcher =  _patt.matcher(_tempBorderStr);
					if(_matcher.find()){
						_topBorderStr = _tempBorderStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
					}			
					_topBorderBefStr = _allTableMap.get(k).get(l-1).get("borderString").toString();			
					_matcher =  _patt.matcher(_topBorderBefStr);
					if(_matcher.find()){
						_topBorderBefStr = _topBorderBefStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
					}					
					if(!_topBorderStr.equals(_topBorderBefStr)){
						isChangeTop = true;
					}
					
					_patt = Pattern.compile("borderBottom,borderType:[^,]+");
					_matcher =  _patt.matcher(_tempBorderStr);
					if(_matcher.find()){
						_bottomBorderStr = _tempBorderStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
					}			
					_bottomBorderBefStr = _allTableMap.get(k).get(l-1).get("borderString").toString();			
					_matcher =  _patt.matcher(_bottomBorderBefStr);
					if(_matcher.find()){
						_bottomBorderBefStr = _bottomBorderBefStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
					}					
					if(!_bottomBorderStr.equals(_bottomBorderBefStr)){
						isChangeBottom = true;
					}
				}	
				
				
				if(useTopBorder){	//top border가 공유일 경우 left의 변경여부를 저장한다.				
					_patt = Pattern.compile("borderLeft,borderType:[^,]+");
					_matcher =  _patt.matcher(_tempBorderStr);
					if(_matcher.find()){
						_leftBorderStr = _tempBorderStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
					}			
					_leftBorderBefStr = _allTableMap.get(k-1).get(l).get("borderString").toString();		
					_matcher =  _patt.matcher(_leftBorderBefStr);
					if(_matcher.find()){
						_leftBorderBefStr = _leftBorderBefStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
					}					
					if(!_leftBorderStr.equals(_leftBorderBefStr)){
						isChangeLeft = true;
					}
					
					_patt = Pattern.compile("borderRight,borderType:[^,]+");
					_matcher =  _patt.matcher(_tempBorderStr);
					if(_matcher.find()){
						_rightBorderStr = _tempBorderStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
					}			
					_rightBorderBefStr = _allTableMap.get(k-1).get(l).get("borderString").toString();		
					_matcher =  _patt.matcher(_rightBorderBefStr);
					if(_matcher.find()){
						_rightBorderBefStr = _rightBorderBefStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
					}					
					if(!_rightBorderStr.equals(_rightBorderBefStr)){
						isChangeRight = true;
					}
				}
				beforeBorderType.add(isChangeTop);
				beforeBorderType.add(isChangeLeft);
				beforeBorderType.add(isChangeRight);
				beforeBorderType.add(isChangeBottom);
				
				if(_newTalbeFlag)
				{
					int _columnIndex = 0;
					
					if( _allTableMap.get(k).get(l).containsKey("columnIndex") ) _columnIndex = Integer.valueOf(_allTableMap.get(k).get(l).get("columnIndex").toString());
					
					if( _lastCellIdx.size() > 0 )
					{
						if( _lastCellIdx.get(k).intValue() == _columnIndex  )
						{
							useRightBorder = true;
						}
					}else if( colIndex == _allTableMap.get(k).size() )
					{
						useRightBorder = true;
					}
					if( rowIndex == _tableMaps.getLength() ) useBottomBorder = true;
				}
				else
				{
					if( l+ Integer.valueOf(tableMapProperty.get("colSpan").toString()) >= _allTableMap.get(k).size()-1 ) useRightBorder = true;
					if( k+ Integer.valueOf(tableMapProperty.get("rowSpan").toString()) >= _allTableMap.size()-1 ) useBottomBorder = true;
				}
				
				_rightBorderStr = "";
				_bottomBorderStr = "";
				
				if(useRightBorder)
				{
					if(_newTalbeFlag) _rightBorderStr =  _allTableMap.get(k).get(_allTableMap.get(k).size()-1 ).get("borderString").toString();
					else  _rightBorderStr =  tableMapProperty.get("borderString").toString();
					
				}
				
				if(useBottomBorder)
				{ 
					if(_newTalbeFlag) _bottomBorderStr =  _allTableMap.get(_tableMaps.getLength()-1).get(l).get("borderString").toString();
					else  _bottomBorderStr =  tableMapProperty.get("borderString").toString();
				}
				if(!(isExportType.endsWith("PPT"))){
					borderAr = ItemConvertParser.convertCellBorder( tableMapProperty.get("borderString").toString(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr,useLeftBorder,useTopBorder, isExportType );
				}
				else{
					borderAr = ItemConvertParser.convertCellBorderForPPT( tableMapProperty.get("borderString").toString(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr );
				}

				// 아이템의 Height값이 다음 Row의 Y값보다 클경우 사이즈를 수정. ( 신규 테이블일경우에만 지정 ) - 이전 테이블의 경우 rowSpan값이 부정확하여 정확한 위치가 잡히지 않을수 있음.
				//X좌표와 Width값 업데이트
				int _spanP = 0;
				int _indexP = 0;
				float _updateP = 0;
				
				int colIdx = Integer.parseInt(tableMapProperty.get("columnIndex").toString());
				
//				if( _xAr.size() > 0 )
//				{
//					_spanP = Integer.valueOf(tableMapProperty.get("colSpan").toString());
//					_indexP = Integer.valueOf(tableMapProperty.get("columnIndex").toString());
//					_propList.put("x", _xAr.get(_indexP) );
//					_updateP = (float) Math.floor((_xAr.get(_indexP + _spanP ) - _xAr.get(_indexP)) *10)/10;
//					_propList.put("width",  _updateP );
//				}
				
				//제외죈 컬럼의 width만큰 x값 업데이트			
				
				
				
				//Y좌표와 Height값 업데이트
				if(_yAr.size() > 0 )
				{
					_spanP = Integer.valueOf(tableMapProperty.get("rowSpan").toString());
					_indexP = Integer.valueOf(tableMapProperty.get("rowIndex").toString());
					
					_propList.put("y", _yAr.get(_indexP) );
//					_updateP = (float) Math.floor((_yAr.get(_indexP + _spanP ) - _yAr.get(_indexP)) *10)/10;
					_updateP = (float) Math.round(_yAr.get(_indexP + _spanP ) - _yAr.get(_indexP) );
					_propList.put("height", _updateP);
				}
				
				
				// 오른쪽과 하단 보더값을 미사용시 보더두께만큼 width와 height값을 줄이도록 처리 
				/** 
				if( !GlobalVariableData.mExcelFlag )
				{
					Integer _chkBorderW = 1;
					if( !useRightBorder && borderAr.get(3) != null )
					{
						_chkBorderW = ((ArrayList<Integer>) borderAr.get(3)).get(0);
						_propList.put("width",(Float.valueOf(String.valueOf( _propList.get("width") )) - _chkBorderW) );
					}
					
					if( !useBottomBorder && borderAr.get(3) != null )
					{
						_chkBorderW = ((ArrayList<Integer>) borderAr.get(3)).get(0);
						_propList.put("height", (Float.valueOf( String.valueOf( _propList.get("height")) ) - _chkBorderW) );
					}
				}
				*/
				
				_propList.put( "isCell",true );	
				
				_propList.put("borderSide", 	mPropertyFn.getBorderSideToArrayList( borderAr.get(0).toString() ) );
				_propList.put("borderTypes", 	borderAr.get(1) );
				_propList.put("borderColors",  	borderAr.get(2) );
				_propList.put("borderWidths",  	borderAr.get(3) );
				_propList.put("borderColorsInt", 	borderAr.get(4) );
				
				// border Original Type 담아두기 
				if( borderAr.size() > 5 )_propList.put("borderOriginalTypes", borderAr.get(5));
				
				//이전 Cell과의 top,left borderType 변경 여부  정보 담기				
				_propList.put("beforeBorderType", beforeBorderType);
				
				_propList.put("ORIGINAL_TABLE_ID", _itemId);
				
				// 2016-05-02 보더 만큼 빼던 부분 제거
//				if(!useRightBorder)
//				{
//					_propList.put("rWidth", Float.valueOf( _propList.get("width").toString() )  - ((ArrayList<Integer>) borderAr.get(3)).get(0) );
//				}
//				
//				if(!useBottomBorder)
//				{
//					_propList.put("rHeight", Float.valueOf( _propList.get("height").toString() )  - ((ArrayList<Integer>)borderAr.get(3)).get(0) );
//				}
				
				// 포맷터 값이존재할경우각각의 데이터를 담는 처리
				NodeList formatterItem = _cellItem.getElementsByTagName("formatter");
				if( formatterItem != null && formatterItem.getLength() > 0 )
				{
					NodeList formatterProperty = ((Element) formatterItem.item(0)).getElementsByTagName("property");
					int propertySize = formatterProperty.getLength();
					for (int p = 0; p < propertySize; p++) {
						
						Element _propItem = (Element) formatterProperty.item(p);

						String _name = _propItem.getAttribute("name");
						String _value = _propItem.getAttribute("value");
						
						if( _name.equals("nation") ){
							_nation = URLDecoder.decode(_value, "UTF-8");
						}
						
						else if( _name.equals("mask") ){
							_mask = URLDecoder.decode(_value, "UTF-8");
						}
						else if( _name.equals("decimalPointLength") ){
							
							_decimalPointLength =  common.ParseIntNullChk(_value, 0);
						}				
						else if( _name.equals("useThousandComma") ){
							_useThousandComma = Boolean.parseBoolean(_value);
						}		
						else if( _name.equals("isDecimal") ){
							_isDecimal = Boolean.parseBoolean(_value);
						}		
						else if( _name.equals("formatString") ){
							_formatString = _value;
						}
						else if(_name.equals("align"))
						{
							_align = _value;
						}
						else if( _name.equals("inputFormatString") )
						{
							_inputForamtString =  URLDecoder.decode(_value , "UTF-8");
						}
						else if( _name.equals("outputFormatString") )
						{
							_outputFormatString =  URLDecoder.decode(_value , "UTF-8");
						}
						
					}
					
					if(labelProjectFlag) _propList.put("formatterElement", 	formatterProperty );
				}
				
				
				
				// 포맷터 값이존재할경우각각의 데이터를 담는 처리
				NodeList formatterEditItem = _cellItem.getElementsByTagName("editItemFormatter");
				if( formatterEditItem != null && formatterEditItem.getLength() > 0 )
				{
					String _eformatDataset=null;
					String _eformatKeyField=null;
					String _eformatLabelField=null;

					
					NodeList formatterProperty = ((Element) formatterEditItem.item(0)).getElementsByTagName("property");
					int propertySize = formatterProperty.getLength();
					for (int p = 0; p < propertySize; p++) {
						
						Element _propItem = (Element) formatterProperty.item(p);

						String _name = _propItem.getAttribute("name");
						String _value = _propItem.getAttribute("value");
						
						if( _name.equals("nation") ){
							_nationE = URLDecoder.decode(_value, "UTF-8");
							_propList.put("eformnation", 	_nationE );
						}
						
						else if( _name.equals("mask") ){
							_maskE = URLDecoder.decode(_value, "UTF-8");
							_propList.put("eformmask", 	_maskE );
						}
						else if( _name.equals("decimalPointLength") ){
							
							_decimalPointLengthE =  common.ParseIntNullChk(_value, 0);
							
							_propList.put("eformdecimalPointLength", 	_decimalPointLengthE );
							
						}				
						else if( _name.equals("useThousandComma") ){
							_useThousandCommaE = Boolean.parseBoolean(_value);
							
							_propList.put("eformuseThousandComma", 	_useThousandCommaE );
						}		
						else if( _name.equals("isDecimal") ){
							_isDecimalE = Boolean.parseBoolean(_value);
							_propList.put("eformisDecimal", _isDecimalE	 );
						}		
						else if( _name.equals("formatString") ){
							_formatStringE = _value;
							_propList.put("eformformatString", _formatStringE	 );
						}
						else if( _name.equals("align") ){
							_alignE = _value;
							_propList.put("eformcurrencyAlign", _alignE	 );
						}
						else if( _name.equals("dataProvider") ){
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put("eformDataProvider", _value	 );
						}else if( _name.equals("dataset") ){
							_eformatDataset = _value;
						}else if( _name.equals("keyField") ){
							_eformatKeyField = _value;
						}else if( _name.equals("valueField") ){
							_eformatLabelField = _value;
						}

					}

					if( _formatterE.equals("SelectMenu") ){
						
						// dataset으로 comboBox 표현.
						if( _eformatDataset != null &&  (!_eformatDataset.equals("null")) && _data.containsKey(_eformatDataset) ){
							
							String _efText = _propList.get("text").toString();
							Boolean _hasValueKey = false;
							
							ArrayList<HashMap<String, Object>> _list = _data.get(_eformatDataset);
							
							HashMap<String, Object> _dataHm;
							Object _keyData;
							Object _labelData;
							
							JSONArray ja = new JSONArray();
							String _jsonStr=null;
							JSONObject jo;
							String _keyStr=null;
							String _labelStr=null;
							
							for( int _eformatIdx=0; _eformatIdx<_list.size(); _eformatIdx++ ){
								_dataHm = _list.get(_eformatIdx);
								_keyData = _dataHm.get(_eformatKeyField);
								_labelData = _dataHm.get(_eformatLabelField);
								_keyStr=_keyData.toString();
								_labelStr = _labelData.toString();
								
								if( _efText.equals(_keyStr) && _hasValueKey == false ){
									_hasValueKey = true;
									_propList.put("text", _labelStr );
								}
								
								jo = new JSONObject();
								jo.put("label", _labelStr);
								jo.put("value",_keyStr );
								ja.add(jo);
							}
							
							_jsonStr = ja.toJSONString();
							
							_propList.put("eformDataProvider", _jsonStr	 );
						}else{
							
							if( _propList.containsKey("eformDataProvider") == false && _propList.get("eformDataProvider") == null )
							{
								_propList.put("eformDataProvider", "[]");
							}
							
							String _jsonStr = _propList.get("eformDataProvider").toString(); 
							JSONParser jsonParser = new JSONParser();
							try {
								
								JSONArray ja = (JSONArray) jsonParser.parse(_jsonStr);
								
								String _efText = _propList.get("text").toString();
								JSONObject oj;
								for( int jsonIdx=0; jsonIdx<ja.size(); jsonIdx++ ){
									oj=(JSONObject) ja.get(jsonIdx);
									if( oj.get("value").equals(_efText) ){
										_propList.put("text", oj.get("label") );
										break;
									}
								}
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					}
//					if(labelProjectFlag) _propList.put("editItemFormatterElement", 	formatterProperty );
				}
				
				
				
				
				
				
				if(tableProperty.containsKey("band_x") && tableProperty.get("band_x")!= "" && tableProperty.get("band_x").equals("NaN") == false)
				{
					updateX = Float.valueOf(tableProperty.get("band_x").toString()) + Float.valueOf(_propList.get("x").toString());
				}
				else
				{
					updateX = Float.valueOf(tableProperty.get("x").toString()) + Float.valueOf(_propList.get("x").toString());
				}
				
				if( !mReportType.equals(ProjectInfo.FORM_TYPE_FREE_FORM) && !mReportType.equals(ProjectInfo.FORM_TYPE_COVERPAGE) && !mReportType.equals(ProjectInfo.FORM_TYPE_LASTPAGE) &&  tableProperty.containsKey("band_y") && tableProperty.get("band_y")!= "" && tableProperty.get("band_y").equals("NaN") == false )
				{
					updateY = Float.valueOf(tableProperty.get("band_y").toString()) + Float.valueOf(_propList.get("y").toString());
				}
				else
				{
					if(_newTalbeFlag) updateY = Float.valueOf(tableProperty.get("y").toString()) + Float.valueOf(_propList.get("y").toString());
					else updateY = Float.valueOf(tableProperty.get("y").toString()) + Float.valueOf(tableMapProperty.get("y").toString());
				}
				
				_propList.put(  "band_x", updateX );
				_propList.put(  "band_y", updateY );
				
				
				// 2017-02-28 기존에는 cell 아이디를 여기서 생성했지만,
				// 에디터에서 cell 아이디를 생성하도록 변경하여 아래와 같이 구분함.
				//if( _propList.get("id").equals("") ){
				if( _propList.get("id") == null || "".equals(_propList.get("id")) ){		
					_propList.put(  "id", "TB_0_" + _childItem.getAttribute("id")+ "_" + k + l );	
				}
				_propList.put(  "className", "UBLabel");
				
				
				
				float _updateX = 0;
				if(  _updateX > -1 ) updateX = _updateX + updateX;
				if(  _updateY > -1 ) updateY = _updateY + updateY;
				
				_propList.put("x", updateX);
				_propList.put("left", updateX);
				_propList.put("y", updateY);
				_propList.put("top", updateY);
					
				if( _cloneX != 0 )
				{
					_updateX = _cloneX + Float.valueOf( _propList.get("x").toString() );
					_propList.put("x", _updateX);
					_propList.put("left", _updateX);
				}
				
				if( _cloneY != 0 )
				{
					_propList.put("y", _cloneY + Float.valueOf( _propList.get("y").toString() ));
					_propList.put("top", _cloneY + Float.valueOf( _propList.get("y").toString() ));
				}
				
				//cell의 ubfx값이 있을경우 cell의 ubfx사용 아닐경우 table의 ubfx사용
				_ubfunction = _cellItem.getElementsByTagName("ubfunction");
				
				ArrayList<NodeList> _ubfxNodes = new ArrayList<NodeList>();
				int _nodeCnts = 0;
				
				//Table의 UBFX가 존재할경우 처리( Table의 ubfx를 먼저 처리후 Cell의 ubfx를 처리 )
				if( _tableUbfunction != null && _tableUbfunction.getLength() > 0 )
				{
					_ubfxNodes.add( _tableUbfunction );
				}
				_ubfxNodes.add(_ubfunction);
				_nodeCnts = _ubfxNodes.size();
				
				ArrayList<String> _ubfunctionList = new ArrayList<String>();
				for(int _ubfxListIndex= 0; _ubfxListIndex < _nodeCnts; _ubfxListIndex++)
				{
					NodeList _selectNodeList = _ubfxNodes.get(_ubfxListIndex);
					
					for(int _ubfxIndex = 0; _ubfxIndex < _selectNodeList.getLength(); _ubfxIndex++)
					{
						Element _ubfxItem = (Element) _selectNodeList.item(_ubfxIndex);

						String _name = _ubfxItem.getAttribute("property");
						String _value = _ubfxItem.getAttribute("value");
						
						_value = URLDecoder.decode(_value, "UTF-8");
						
						
						String _fnValue;
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = mFunction.testFN(_value.toString() , _dCnt,_totalPageNum,_currentPageNum,-1,-1 , "");
						}else{
							_fnValue = mFunction.function(_value.toString(),_dCnt, _totalPageNum , _currentPageNum ,-1,-1);
						}
						
						
						_fnValue = _fnValue.trim();
						
						if( _name.equals("text") || _fnValue.equals("") == false)
						{
							_propList = convertUbfxStyle(_name, _fnValue, _propList );
						}
						
						if(isExportType.equals("PRINT") || isExportType.equals("PDF") ) _ubfunctionList.add( _name + "§" + _value );
					}
				}
				if( _ubfunctionList.size() > 0 ) _propList.put("ubfunctionList", _ubfunctionList);
				
				_ubfxNodes = null;
				
				if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
					String _formatValue="";
					String _excelFormatterStr = "";
					
					if( _dataType.equalsIgnoreCase("1") ){
						ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
						if( _dCnt >= _list.size()) 
						{
							_formatValue = "";
						}
						else
						{
							HashMap<String, Object> _dataHm = _list.get(_dCnt);
							Object _dataValue = _dataHm.get(_dataColumn);
							_formatValue = _dataValue != null ? _dataValue.toString() : "";
						}
					}else{
						_formatValue = _propList.get("text").toString();
					}
					try {
						
						if( _formatter.equalsIgnoreCase("Currency") ){
							_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
							_excelFormatterStr = _nation  + "§" + _align;
						}else if( _formatter.equalsIgnoreCase("Date") ){
							_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
							_excelFormatterStr = _formatString;
						}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
							_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
							_excelFormatterStr = _mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal;
						}else if( _formatter.equalsIgnoreCase("MaskString") ){
							_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
						}
						else if( _formatter.equalsIgnoreCase("CustomDate") )
						{
							_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
							_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
						}
					} catch (ParseException e) {
//						e.printStackTrace();
					}
					if( isExportType.equals("EXCEL") && _excelFormatterStr.equals("") == false && common.getPropertyValue("excelExport.useFormatter") != null && common.getPropertyValue("excelExport.useFormatter").equals("true") ) 
					{
						_propList.put("EX_FORMATTER", _formatter);
						_propList.put("EX_FORMAT_DATA_STR", _excelFormatterStr);
						_propList.put("EX_FORMAT_ORIGINAL_STR", _propList.get("text").toString() );
					}
					
					_propList.put("text", _formatValue);
					
					// format이 label band에서 안들어간다.
					_propList.put("formatter", _formatter);
					_propList.put("mask", _mask);
					_propList.put("decimalPointLength", _decimalPointLength);
					_propList.put("useThousandComma", _useThousandComma);
					_propList.put("isDecimal", _isDecimal);
					_propList.put("formatString", _formatString);
					_propList.put("nation", _nation);
					_propList.put("currencyAlign", _align);
					_propList.put("inputFormatString", _inputForamtString);
					_propList.put("outputFormatString", _outputFormatString);
					//
					
				}
				
				//hyperLinkedParam처리
				if( _propList.containsKey("ubHyperLinkType") && "2".equals( _propList.get("ubHyperLinkType") )  )
				{
					NodeList _hyperLinkedParam = _cellItem.getElementsByTagName("ubHyperLinkParm");
					if( _hyperLinkedParam != null && _hyperLinkedParam.getLength() > 0 )
					{
						Element _hyperLinkEl = (Element) _hyperLinkedParam.item(0);
						NodeList _hyperLinkedParams = _hyperLinkEl.getElementsByTagName("param");
						int _hyperLinkedParamSize = _hyperLinkedParams.getLength();
						
						HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
						
						ArrayList<HashMap<String,String>> _ubHyperLinkTypeArr = new ArrayList<HashMap<String,String>>();
						
						for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
						{
							Element _hyperParam = (Element) _hyperLinkedParams.item(_hyperIdx);
							NodeList _hyperPropertys = _hyperParam.getElementsByTagName("property");
							int _hyperPropertysSize = _hyperPropertys.getLength();
							String _hyperParamKey = "";
							String _hyperParamValue = "";
							String _hyperParamType = "";
							
							for (int _hyperProIdx = 0; _hyperProIdx <  _hyperPropertysSize; _hyperProIdx++) 
							{
								Element _hyperProperty = (Element) _hyperPropertys.item(_hyperProIdx);
								if( "id".equals(_hyperProperty.getAttribute("name")) )
								{
									_hyperParamKey = _hyperProperty.getAttribute("value").toString();
								}
								else if( "value".equals(_hyperProperty.getAttribute("name")) )
								{
									_hyperParamValue = _hyperProperty.getAttribute("value").toString();
								}
								else if( "type".equals(_hyperProperty.getAttribute("name")) )
								{
									_hyperParamType = _hyperProperty.getAttribute("value").toString();
								}
							}
							
							// thread 처리시 Map를 재활용 하기 위해 속성을 담아둔다.
							if( isExportType.equals("PRINT") || isExportType.equals("PDF") )
							{
								HashMap<String, String> _ubhyperLinkedM = new HashMap<String, String>();
								_ubhyperLinkedM.put("id", _hyperParamKey);
								_ubhyperLinkedM.put("value", _hyperParamValue);
								_ubhyperLinkedM.put("type", _hyperParamType);
								_ubHyperLinkTypeArr.add(_ubhyperLinkedM);
							}
							
							
							if( "DataSet".equals(_hyperParamType) )
							{
								String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
								String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
								
								_hyperParamValue = "";
								
								if(_data.containsKey(_hyperLinkedDataSetId))
								{
									ArrayList<HashMap<String, Object>> _list = _data.get( _hyperLinkedDataSetId );
//									Object _dataValue = "";
									if( _list != null ){
										if( _dCnt < _list.size() )
										{
											HashMap<String, Object> _dataHm = _list.get(_dCnt);
											_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
										}
									}
								}
							}
							else if("Parameter".equals(_hyperParamType) )
							{
//								String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
								String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
								HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

								String _pValue = _pList.get("parameter");

								if( _pValue.equals("undefined"))
								{
									_hyperParamValue = "";
								}
								else
								{
									_hyperParamValue = _pValue;
								}
							}
							
							_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
							
						}
						
						if( _hyperLinkedParamMap.isEmpty() == false ) _propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
						
						if( _ubHyperLinkTypeArr.size() > 0 ) _propList.put("ubHyperLinkTypeArr", _ubHyperLinkTypeArr);
					}
				}
				
				if( !_formatterE.equalsIgnoreCase("null") && !_formatterE.equalsIgnoreCase("") ){
					String _formatValue="";
					if( _dataType.equalsIgnoreCase("1") ){
						ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
						if( _dCnt >= _list.size()) 
						{
							_formatValue = "";
						}
						else
						{
							HashMap<String, Object> _dataHm = _list.get(_dCnt);
							Object _dataValue = _dataHm.get(_dataColumn);
							_formatValue = _dataValue != null ? _dataValue.toString() : "";
						}
					}else{
						_formatValue = _propList.get("text").toString();
					}
					try {
						
						if( _formatterE.equalsIgnoreCase("Currency") ){
							_formatValue =UBFormatter.currencyFormat("", _nationE, _alignE, _formatValue);
						}else if( _formatterE.equalsIgnoreCase("Date") ){
							_formatValue=UBFormatter.dateFormat(_formatStringE, _formatValue);
						}else if( _formatterE.equalsIgnoreCase("MaskNumber") ){
							_formatValue =UBFormatter.maskNumberEditFormat(_mask, _decimalPointLengthE, _useThousandCommaE, _isDecimalE, _formatValue);
						}else if( _formatterE.equalsIgnoreCase("MaskString") ){
							_formatValue=UBFormatter.maskStringEditFormat(_maskE, _formatValue,"*");
						}
					} catch (ParseException e) {
//						e.printStackTrace();
					}
					_propList.put("textE", _formatValue);
					
					// format이 label band에서 안들어간다.
					
					_propList.put("editItemFormatter", _formatterE);
					_propList.put("eformmask", _maskE);
					_propList.put("eformdecimalPointLength", _decimalPointLengthE);
					_propList.put("eformuseThousandComma", _useThousandCommaE);
					_propList.put("eformisDecimal", _isDecimalE);
					_propList.put("eformformatString", _formatValue);
					_propList.put("eformcurrencyAlign", _alignE);
					//
				}
				
				
				
				// resize Font값이 true일경우에 만 진행 2016-04-12 최명진
				if( _resizeFont && !"".equals(_propList.get("text")) )
				{
					float _fontSize 	= Float.valueOf( _propList.get("fontSize").toString() );
					String _fontFamily 	= _propList.get("fontFamily").toString();
					String _fontWeight 	= _propList.get("fontWeight").toString();
					
					float _padding = (_propList.containsKey("padding"))? Float.valueOf( _propList.get("padding").toString()):3;
					
					float _maxBorderSize = 0;
					if(_propList.containsKey("borderWidths"))
					{
						ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
						
						for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
							if(_maxBorderSize < _borderWidths.get(_bIndex))
							{
								_maxBorderSize = _borderWidths.get(_bIndex);
							}
						}
						_padding = _maxBorderSize + _padding;
					}
					
					float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
					
					_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
					_propList.put("fontSize",  _fontSize);

				}
				// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
				if(_propList.containsKey("text"))
					_propList.put("tooltip", _propList.get("text").toString());
				
				//Export시 테이블형태로 내보내기 위한 속성추가
				if(_newTalbeFlag)
				{
					_propList.put("TABLE_ID", _childItem.getAttribute("id"));
					_propList.put("cellHeight", _propList.get("height"));
					_propList.put("cellY", _propList.get("y"));
					_propList.put("isTable", "true" );
				}
				
				// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
				if( _propList.containsKey("rowId") && (_propList.get("dataType") == null || !( _propList.get("dataType").equals("1") || _propList.get("dataType").equals("2") ) ) )
				{
//					_propList.put("rowId", _currentPageNum);
				}
				
//				_propList.put("className" , _className );
//				_propList.put("id" , _itemId );
				
				if(_className.equals("UBApproval"))
				{
					if(_ubApprovalAr == null)
					{
						_ubApprovalAr = new ArrayList<HashMap<String, Object>>();
						_ubApprovalAr.add(tableProperty);
					}
					_ubApprovalAr.add(_propList);
				}
				else if( labelProjectFlag == true || !_propList.containsKey("visible") || !_propList.get("visible").equals("false") )
				{
					_objects.add(_propList);
				}				
			}
			
			
		}
		
		if(_className.equals("UBApproval") && _ubApprovalAr != null)
		{
			convertTableMapToApprovalTbl(_ubApprovalAr, _objects, _data );
		}
		
		return _objects;
	}
	
	
	
	
	
	public static ArrayList< Object > convertCellBorder( String _borderString, boolean useRight, boolean useBottom, String _rightBorderString, String _bottomBorderString , boolean useLeft, boolean useTop, String isExportType)
	{
		// TableMap정보를 불러와서 보더 업데이트
		if( _borderString.contains("$") )
		{
			_borderString = _borderString.replace("$", "&");
		}
		
		String[] borderStr = _borderString.split("&");
		
		int _borderWidth = 0;
		ArrayList<String> borderTypes = new ArrayList<String>();
		ArrayList<String> borderColors = new ArrayList<String>();
		ArrayList<Integer> borderColorInts = new ArrayList<Integer>();
		ArrayList<Integer> borderWidths = new ArrayList<Integer>();
		ArrayList<String> borderOriginalTypes = new ArrayList<String>();
		
		String borderSide = "";
		String chkStr = "";
		String valueStr = "";
		
		String typeStr 			= "";
		String borderTypeStr 		= "";
		String borderColorStr		= "";
		String borderColorIntStr		= "";
		String borderThicknessStr 	= "";
		String borderOriginalType  = "";
		
		// bottom과 right값 사용시 먼저 각각의 정보를 담아두기
		if( _rightBorderString.contains("$") )
		{
			_rightBorderString = _rightBorderString.replace("$", "&");
		}
		
		String[] borderRightStr = _rightBorderString.split("&");
		
		if( _bottomBorderString.contains("$") )
		{
			_bottomBorderString = _bottomBorderString.replace("$", "&");
		}
		
		String[] borderBottomStr = _bottomBorderString.split("&");
		
		
		ArrayList<String> rightBorderListAr = null;
		ArrayList<String> bottomBorderListAr = null;
		
		int i=0;
		int j=0;
		int k=0;
		
		if(useRight && borderRightStr.length > 0 )
		{
			for ( i = 0; i < borderRightStr.length; i++) {
				String borderTypeR = borderRightStr[i];
				String[] borderB = borderTypeR.split(",");
				typeStr	 			= "";
				borderTypeStr 		= "";
				borderColorStr 		= "";
				borderThicknessStr 	= "";
				borderColorIntStr 	= "";
				for ( j = 0; j < borderB.length; j++) {
					
					String[] subBorderR = borderB[j].split(":");
					
					try {
						chkStr 		= subBorderR[0];
						valueStr 	= subBorderR[1];
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println("=====================");
					}
					
					if(chkStr.equals("startPoint"))
					{
						String[] _positionStartR = null;
						String[] _positionEndR = null;
						String _globalX = "";
						String _globalY = "";
						String _positionStr = "";
						for ( k = 0; k < borderB.length; k++) {
							String[] chkR = borderB[k].split(":"); 
							if(chkR[0].equals("startPoint"))
							{
								_positionStartR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("endPoint"))
							{
								_positionEndR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("globalX"))
							{
								_globalX = chkR[1];
							}
							else if(chkR[0].equals("globalY"))
							{
								_globalY = chkR[1];
							}
						}
						
						if(_positionStartR[0].equals(_positionEndR[0]))
						{
							if(_positionStartR[0].equals(_globalX))
							{
								_positionStr = "borderTop";
							}
							else
							{
								_positionStr = "borderBottom";
							}
						}
						else
						{
							if(_positionStartR[1].equals(_globalY))
							{
								_positionStr = "borderLeft";
							}
							else
							{
								_positionStr = "borderRight";
							}
						}
						
						if( _positionStr != "" )
						{
							chkStr = "type";
							valueStr = _positionStr;
						}
						
					}
					
					if( chkStr.equals("type") )
					{
						if( valueStr.equals("borderRight") )
						{
							typeStr = "right";
						}
					}
					else if( chkStr.equals("borderType") )
					{
						borderTypeStr = getBorderType(valueStr);						
					}
					else if( chkStr.equals("borderColor") )
					{
						borderColorStr = changeColorToHex( Integer.parseInt(valueStr) );
						borderColorIntStr = valueStr;
					}
					else if( chkStr.equals("borderThickness") )
					{
						borderThicknessStr = valueStr;
					}
					
				}
				
				if(typeStr.equals("right"))
				{
					rightBorderListAr = new ArrayList<String>();
					rightBorderListAr.add(typeStr);
					rightBorderListAr.add(borderTypeStr);
					rightBorderListAr.add(borderColorStr);
					rightBorderListAr.add(borderThicknessStr);
					rightBorderListAr.add(borderColorIntStr);
					break;
				}
			}
			
			
		}

		if(useBottom  && borderBottomStr.length > 0 )
		{
			for ( i = 0; i < borderBottomStr.length; i++) {
				String borderTypeB = borderBottomStr[i];
				String[] borderB = borderTypeB.split(",");
				typeStr	 			= "";
				borderTypeStr 		= "";
				borderColorStr 		= "";
				borderThicknessStr 	= "";
				for ( j = 0; j < borderB.length; j++) {
					
					String[] subBorderB = borderB[j].split(":");
					chkStr 		= subBorderB[0];
					valueStr 	= subBorderB[1];

					if(chkStr.equals("startPoint"))
					{
						String[] _positionStartR = null;
						String[] _positionEndR = null;
						String _globalX = "";
						String _globalY = "";
						String _positionStr = "";
						for ( k = 0; k < borderB.length; k++) {
							String[] chkR = borderB[k].split(":");
							if(chkR[0].equals("startPoint"))
							{
								_positionStartR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("endPoint"))
							{
								_positionEndR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("globalX"))
							{
								_globalX = chkR[1];
							}
							else if(chkR[0].equals("globalY"))
							{
								_globalY = chkR[1];
							}
						}
						
						if(_positionStartR[0].equals(_positionEndR[0]))
						{
							if(_positionStartR[0].equals(_globalX))
							{
								_positionStr = "borderTop";
							}
							else
							{
								_positionStr = "borderBottom";
							}
						}
						else
						{
							if(_positionStartR[1].equals(_globalY))
							{
								_positionStr = "borderLeft";
							}
							else
							{
								_positionStr = "borderRight";
							}
						}
						
						if( _positionStr != "" )
						{
							chkStr = "type";
							valueStr = _positionStr;
						}
						
					}
					
					if( chkStr.equals("type") )
					{
						if( valueStr.equals("borderBottom") )
						{
							typeStr = "bottom";
						}
					}
					else if( chkStr.equals("borderType") )
					{
						borderTypeStr = getBorderType(valueStr);						
					}
					else if( chkStr.equals("borderColor") )
					{
						borderColorStr = changeColorToHex( Integer.parseInt(valueStr) );
						borderColorIntStr = valueStr;
					}
					else if( chkStr.equals("borderThickness") )
					{
						borderThicknessStr = valueStr;
					}
					
				}
				
				if(typeStr.equals("bottom"))
				{
					bottomBorderListAr = new ArrayList<String>();
					bottomBorderListAr.add(typeStr);
					bottomBorderListAr.add(borderTypeStr);
					bottomBorderListAr.add(borderColorStr);
					bottomBorderListAr.add(borderThicknessStr); 
					bottomBorderListAr.add(borderColorIntStr); 
					break;
				}
				
			}
		}
		
		for ( i = 0; i < borderStr.length; i++) {
			
			String borderType = borderStr[i];
			typeStr	 			= "";
//			borderTypeStr 		= "";
			borderTypeStr 		= "";
			borderColorStr 		= "";
			borderThicknessStr 	= "";
			borderOriginalType  = "";
			
			String[] subBorder = borderType.split(",");
			
			for( j = 0; j < subBorder.length; j++ )
			{
				String[] subBorder2 = subBorder[j].split(":");
				chkStr 		= subBorder2[0];
				valueStr 	= subBorder2[1];

				if(chkStr.equals("startPoint"))
				{
					String[] _positionStartR = null;
					String[] _positionEndR = null;
					String _globalX = "";
					String _globalY = "";
					String _positionStr = "";
					for ( k = 0; k < subBorder.length; k++) {
						String[] chkR = subBorder[k].split(":");
						if(chkR[0].equals("startPoint"))
						{
							_positionStartR = chkR[1].split(" ");
						}
						else if(chkR[0].equals("endPoint"))
						{
							_positionEndR = chkR[1].split(" ");
						}
						else if(chkR[0].equals("globalX"))
						{
							_globalX = chkR[1];
						}
						else if(chkR[0].equals("globalY"))
						{
							_globalY = chkR[1];
						}
					}
					
					if(_positionStartR[0].equals(_positionEndR[0]))
					{
						if(_positionStartR[0].equals(_globalX))
						{
							_positionStr = "borderTop";
						}
						else
						{
							_positionStr = "borderBottom";
						}
					}
					else
					{
						if(_positionStartR[1].equals(_globalY))
						{
							_positionStr = "borderLeft";
						}
						else
						{
							_positionStr = "borderRight";
						}
					}
					
					if( _positionStr != "" )
					{
						chkStr = "type";
						valueStr = _positionStr;
					}
					
				}
				
				if( chkStr.equals("type") )
				{
					if( valueStr.equals("borderBottom") )
					{
						typeStr = "bottom";
					}
					else if( valueStr.equals("borderLeft") )
					{
						typeStr = "left";
					}
					else if( valueStr.equals("borderRight") )
					{
						typeStr = "right";
					}
					else if(  valueStr.equals("borderTop") )
					{
						typeStr = "top";
					}
				}
				else if( chkStr.equals("borderType") )
				{
					
					borderTypeStr = getBorderType(valueStr);
					borderOriginalType = borderTypeStr;					
				}
				else if( chkStr.equals("borderColor") )
				{
					borderColorStr = changeColorToHex( Integer.parseInt(valueStr) );
					borderColorIntStr = valueStr;
				}
				else if( chkStr.equals("borderThickness") )
				{
					borderThicknessStr = valueStr;
				}
				
			}
			
			if(typeStr != "")
			{
				if(typeStr.equals("bottom"))
				{
					if( useBottom && bottomBorderListAr != null  )
					{
						borderTypeStr = bottomBorderListAr.get(1);
						borderColorStr = bottomBorderListAr.get(2);
						borderThicknessStr = bottomBorderListAr.get(3);
						borderColorIntStr = bottomBorderListAr.get(4);
					}
					else
					{
						borderTypeStr = "none";
					}
				}
				else if(typeStr.equals("right") )
				{
					if( useRight && rightBorderListAr != null )
					{
						borderTypeStr 		= rightBorderListAr.get(1);
						borderColorStr 		= rightBorderListAr.get(2);
						borderThicknessStr 	= rightBorderListAr.get(3);
						borderColorIntStr 	= rightBorderListAr.get(4);
					}
					else
					{
						borderTypeStr = "none";
					}
				}
				
				if( borderSide != "" ) borderSide = borderSide + ",";
				borderSide = borderSide + typeStr;
				borderColors.add( borderColorStr );
				
				if(isExportType.equals("")|| isExportType.equals("PDF")|| isExportType.equals("PRINT")){
					if(typeStr == "left" &&  useLeft && borderTypeStr != "none"){
						borderTypes.add( borderTypeStr + "_share");
					}else if(typeStr == "top"  && useTop && borderTypeStr != "none"){
						borderTypes.add( borderTypeStr + "_share");
					}else if(typeStr == "right"  && !useRight && borderTypeStr != "none"){
						borderTypes.add( borderTypeStr + "_share");
					}else if(typeStr == "bottom"  && !useBottom && borderTypeStr != "none"){
						borderTypes.add( borderTypeStr + "_share");
					}else{
						borderTypes.add( borderTypeStr);
					}
				}else{
					borderTypes.add( borderTypeStr);
				}
				
//				
//				if(typeStr == "left" && borderTypeStr == "double" && useLeft){
//					borderTypes.add( borderTypeStr + "_share");
//				}else if(typeStr == "top" && borderTypeStr == "double" && useTop){
//					borderTypes.add( borderTypeStr + "_share");
//				}else if(typeStr == "right" && borderTypeStr == "double" && !useRight){
//					borderTypes.add( borderTypeStr + "_share");
//				}else if(typeStr == "bottom" && borderTypeStr == "double" && !useBottom){
//					borderTypes.add( borderTypeStr + "_share");
//				}else{
//					borderTypes.add( borderTypeStr);
//				}
				
				borderWidths.add( Integer.valueOf((int) Math.ceil( Double.parseDouble(borderThicknessStr) ) ) );
				borderColorInts.add( Integer.valueOf( borderColorIntStr ) );
				borderOriginalTypes.add(borderOriginalType);
			}
			
		}
		
		ArrayList< Object > retBorderAr = new ArrayList<Object>();
		
		retBorderAr.add(borderSide);
		retBorderAr.add(borderTypes);
		retBorderAr.add(borderColors);
		retBorderAr.add(borderWidths);
		retBorderAr.add(borderColorInts);
		retBorderAr.add(borderOriginalTypes);
		
		return retBorderAr;
	}
	
	
	public static ArrayList< Object > convertCellBorderForPPT( String _borderString, boolean useRight, boolean useBottom, String _rightBorderString, String _bottomBorderString )
	{
		// TableMap정보를 불러와서 보더 업데이트
		if( _borderString.contains("$") )
		{
			_borderString = _borderString.replace("$", "&");
		}
		
		String[] borderStr = _borderString.split("&");
		
		int _borderWidth = 0;
		ArrayList<String> borderTypes = new ArrayList<String>();
		ArrayList<String> borderColors = new ArrayList<String>();
		ArrayList<Integer> borderColorInts = new ArrayList<Integer>();
		ArrayList<Integer> borderWidths = new ArrayList<Integer>();
		String borderSide = "";
		String chkStr = "";
		String valueStr = "";
		
		String typeStr 			= "";
		String borderTypeStr 		= "";
		String borderColorStr		= "";
		String borderColorIntStr		= "";
		String borderThicknessStr 	= "";
		
		// bottom과 right값 사용시 먼저 각각의 정보를 담아두기
		if( _rightBorderString.contains("$") )
		{
			_rightBorderString = _rightBorderString.replace("$", "&");
		}
		
		String[] borderRightStr = _rightBorderString.split("&");
		
		if( _bottomBorderString.contains("$") )
		{
			_bottomBorderString = _bottomBorderString.replace("$", "&");
		}
		
		String[] borderBottomStr = _bottomBorderString.split("&");
		String[] borderTopStr = _bottomBorderString.split("&");
		
		
		ArrayList<String> rightBorderListAr = null;
		ArrayList<String> bottomBorderListAr = null;
		ArrayList<String> topBorderListAr = null;
		
		int i=0;
		int j=0;
		int k=0;
		
		if(useRight && borderRightStr.length > 0 )
		{
			for ( i = 0; i < borderRightStr.length; i++) {
				String borderTypeR = borderRightStr[i];
				String[] borderB = borderTypeR.split(",");
				typeStr	 			= "";
				borderTypeStr 		= "";
				borderColorStr 		= "";
				borderThicknessStr 	= "";
				borderColorIntStr 	= "";
				for ( j = 0; j < borderB.length; j++) {
					
					String[] subBorderR = borderB[j].split(":");
					
					chkStr 		= subBorderR[0];
					valueStr 	= subBorderR[1];
					
					if(chkStr.equals("startPoint"))
					{
						String[] _positionStartR = null;
						String[] _positionEndR = null;
						String _globalX = "";
						String _globalY = "";
						String _positionStr = "";
						for ( k = 0; k < borderB.length; k++) {
							String[] chkR = borderB[k].split(":"); 
							if(chkR[0].equals("startPoint"))
							{
								_positionStartR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("endPoint"))
							{
								_positionEndR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("globalX"))
							{
								_globalX = chkR[1];
							}
							else if(chkR[0].equals("globalY"))
							{
								_globalY = chkR[1];
							}
						}
						
						if(_positionStartR[0].equals(_positionEndR[0]))
						{
							if(_positionStartR[0].equals(_globalX))
							{
								_positionStr = "borderTop";
							}
							else
							{
								_positionStr = "borderBottom";
							}
						}
						else
						{
							if(_positionStartR[1].equals(_globalY))
							{
								_positionStr = "borderLeft";
							}
							else
							{
								_positionStr = "borderRight";
							}
						}
						
						if( _positionStr != "" )
						{
							chkStr = "type";
							valueStr = _positionStr;
						}
						
					}
					
					if( chkStr.equals("type") )
					{
						if( valueStr.equals("borderRight") )
						{
							typeStr = "right";
						}
					}
					else if( chkStr.equals("borderType") )
					{
						borderTypeStr = getBorderType(valueStr);
					}
					else if( chkStr.equals("borderColor") )
					{
						borderColorStr = changeColorToHex( Integer.parseInt(valueStr) );
						borderColorIntStr = valueStr;
					}
					else if( chkStr.equals("borderThickness") )
					{
						borderThicknessStr = valueStr;
					}
					
				}
				
				if(typeStr.equals("right"))
				{
					rightBorderListAr = new ArrayList<String>();
					rightBorderListAr.add(typeStr);
					rightBorderListAr.add(borderTypeStr);
					rightBorderListAr.add(borderColorStr);
					rightBorderListAr.add(borderThicknessStr);
					rightBorderListAr.add(borderColorIntStr);
					break;
				}
			}
			
			
		}
		
		if(useBottom  && borderBottomStr.length > 0 )
		{
			for ( i = 0; i < borderBottomStr.length; i++) {
				String borderTypeB = borderBottomStr[i];
				String[] borderB = borderTypeB.split(",");
				typeStr	 			= "";
				borderTypeStr 		= "";
				borderColorStr 		= "";
				borderThicknessStr 	= "";
				for ( j = 0; j < borderB.length; j++) {
					
					String[] subBorderB = borderB[j].split(":");
					chkStr 		= subBorderB[0];
					valueStr 	= subBorderB[1];
					
					if(chkStr.equals("startPoint"))
					{
						String[] _positionStartR = null;
						String[] _positionEndR = null;
						String _globalX = "";
						String _globalY = "";
						String _positionStr = "";
						for ( k = 0; k < borderB.length; k++) {
							String[] chkR = borderB[k].split(":");
							if(chkR[0].equals("startPoint"))
							{
								_positionStartR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("endPoint"))
							{
								_positionEndR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("globalX"))
							{
								_globalX = chkR[1];
							}
							else if(chkR[0].equals("globalY"))
							{
								_globalY = chkR[1];
							}
						}
						
						if(_positionStartR[0].equals(_positionEndR[0]))
						{
							if(_positionStartR[0].equals(_globalX))
							{
								_positionStr = "borderTop";
							}
							else
							{
								_positionStr = "borderBottom";
							}
						}
						else
						{
							if(_positionStartR[1].equals(_globalY))
							{
								_positionStr = "borderLeft";
							}
							else
							{
								_positionStr = "borderRight";
							}
						}
						
						if( _positionStr != "" )
						{
							chkStr = "type";
							valueStr = _positionStr;
						}
						
					}
					
					if( chkStr.equals("type") )
					{
						if( valueStr.equals("borderBottom") )
						{
							typeStr = "bottom";
						}
					}
					else if( chkStr.equals("borderType") )
					{
						borderTypeStr = getBorderType(valueStr);
					}
					else if( chkStr.equals("borderColor") )
					{
						borderColorStr = changeColorToHex( Integer.parseInt(valueStr) );
						borderColorIntStr = valueStr;
					}
					else if( chkStr.equals("borderThickness") )
					{
						borderThicknessStr = valueStr;
					}
					
				}
				
				if(typeStr.equals("bottom"))
				{
					bottomBorderListAr = new ArrayList<String>();
					bottomBorderListAr.add(typeStr);
					bottomBorderListAr.add(borderTypeStr);
					bottomBorderListAr.add(borderColorStr);
					bottomBorderListAr.add(borderThicknessStr); 
					bottomBorderListAr.add(borderColorIntStr); 
					break;
				}
			}
		}
		if(useBottom  && borderTopStr.length > 0 )
		{
			for ( i = 0; i < borderTopStr.length; i++) {
				String borderTypeB = borderTopStr[i];
				String[] borderB = borderTypeB.split(",");
				typeStr	 			= "";
				borderTypeStr 		= "";
				borderColorStr 		= "";
				borderThicknessStr 	= "";
				for ( j = 0; j < borderB.length; j++) {
					
					String[] subBorderB = borderB[j].split(":");
					chkStr 		= subBorderB[0];
					valueStr 	= subBorderB[1];
					
					if(chkStr.equals("startPoint"))
					{
						String[] _positionStartR = null;
						String[] _positionEndR = null;
						String _globalX = "";
						String _globalY = "";
						String _positionStr = "";
						for ( k = 0; k < borderB.length; k++) {
							String[] chkR = borderB[k].split(":");
							if(chkR[0].equals("startPoint"))
							{
								_positionStartR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("endPoint"))
							{
								_positionEndR = chkR[1].split(" ");
							}
							else if(chkR[0].equals("globalX"))
							{
								_globalX = chkR[1];
							}
							else if(chkR[0].equals("globalY"))
							{
								_globalY = chkR[1];
							}
						}
						
						if(_positionStartR[0].equals(_positionEndR[0]))
						{
							if(_positionStartR[0].equals(_globalX))
							{
								_positionStr = "borderTop";
							}
							else
							{
								_positionStr = "borderBottom";
							}
						}
						else
						{
							if(_positionStartR[1].equals(_globalY))
							{
								_positionStr = "borderLeft";
							}
							else
							{
								_positionStr = "borderRight";
							}
						}
						
						if( _positionStr != "" )
						{
							chkStr = "type";
							valueStr = _positionStr;
						}
						
					}
					
					if( chkStr.equals("type") )
					{
						if( valueStr.equals("borderTop") )
						{
							typeStr = "top";
						}
					}
					else if( chkStr.equals("borderType") )
					{
						borderTypeStr = getBorderType(valueStr);
					}
					else if( chkStr.equals("borderColor") )
					{
						borderColorStr = changeColorToHex( Integer.parseInt(valueStr) );
						borderColorIntStr = valueStr;
					}
					else if( chkStr.equals("borderThickness") )
					{
						borderThicknessStr = valueStr;
					}
					
				}
				
				if(typeStr.equals("top"))
				{
					topBorderListAr = new ArrayList<String>();
					topBorderListAr.add(typeStr);
					topBorderListAr.add(borderTypeStr);
					topBorderListAr.add(borderColorStr);
					topBorderListAr.add(borderThicknessStr); 
					topBorderListAr.add(borderColorIntStr); 
					break;
				}
			}
		}
		
		ArrayList<String> preBottomBorderListAr = null;
		preBottomBorderListAr = bottomBorderListAr;
		
		
		for ( i = 0; i < borderStr.length; i++) {
			
			String borderType = borderStr[i];
			typeStr	 			= "";
//			borderTypeStr 		= "";
			borderTypeStr 		= "";
			borderColorStr 		= "";
			borderThicknessStr 	= "";
			
			String[] subBorder = borderType.split(",");
			
			for( j = 0; j < subBorder.length; j++ )
			{
				String[] subBorder2 = subBorder[j].split(":");
				chkStr 		= subBorder2[0];
				valueStr 	= subBorder2[1];
				
				if(chkStr.equals("startPoint"))
				{
					String[] _positionStartR = null;
					String[] _positionEndR = null;
					String _globalX = "";
					String _globalY = "";
					String _positionStr = "";
					for ( k = 0; k < subBorder.length; k++) {
						String[] chkR = subBorder[k].split(":");
						if(chkR[0].equals("startPoint"))
						{
							_positionStartR = chkR[1].split(" ");
						}
						else if(chkR[0].equals("endPoint"))
						{
							_positionEndR = chkR[1].split(" ");
						}
						else if(chkR[0].equals("globalX"))
						{
							_globalX = chkR[1];
						}
						else if(chkR[0].equals("globalY"))
						{
							_globalY = chkR[1];
						}
					}
					
					if(_positionStartR[0].equals(_positionEndR[0]))
					{
						if(_positionStartR[0].equals(_globalX))
						{
							_positionStr = "borderTop";
						}
						else
						{
							_positionStr = "borderBottom";
						}
					}
					else
					{
						if(_positionStartR[1].equals(_globalY))
						{
							_positionStr = "borderLeft";
						}
						else
						{
							_positionStr = "borderRight";
						}
					}
					
					if( _positionStr != "" )
					{
						chkStr = "type";
						valueStr = _positionStr;
					}
					
				}
				
				if( chkStr.equals("type") )
				{
					if( valueStr.equals("borderBottom") )
					{
						typeStr = "bottom";
					}
					else if( valueStr.equals("borderLeft") )
					{
						typeStr = "left";
					}
					else if( valueStr.equals("borderRight") )
					{
						typeStr = "right";
					}
					else if(  valueStr.equals("borderTop") )
					{
						typeStr = "top";
					}
				}
				else if( chkStr.equals("borderType") )
				{
					
					borderTypeStr = getBorderType(valueStr);
					
				}
				else if( chkStr.equals("borderColor") )
				{
					borderColorStr = changeColorToHex( Integer.parseInt(valueStr) );
					borderColorIntStr = valueStr;
				}
				else if( chkStr.equals("borderThickness") )
				{
					borderThicknessStr = valueStr;
				}
				
			}
			
			if(typeStr != "")
			{
				if(typeStr.equals("bottom"))
				{
					if( useBottom && bottomBorderListAr != null  )
					{
						borderTypeStr = bottomBorderListAr.get(1);
						borderColorStr = bottomBorderListAr.get(2);
						borderThicknessStr = bottomBorderListAr.get(3);
						borderColorIntStr = bottomBorderListAr.get(4);
						/*if(borderTypeStr.equals("none") && !(topBorderListAr.get(1).equals("none"))){
							borderTypeStr = topBorderListAr.get(1);
						}*/
					}
					
				}
				else if(typeStr.equals("right") )
				{
					if(rightBorderListAr != null )
					{
						borderTypeStr 		= rightBorderListAr.get(1);
						borderColorStr 		= rightBorderListAr.get(2);
						borderThicknessStr 	= rightBorderListAr.get(3);
						borderColorIntStr 	= rightBorderListAr.get(4);
					}
				}
				/*else if(typeStr.equals("top") )
				{
					if(topBorderListAr != null )
					{
						borderTypeStr 		= topBorderListAr.get(1);
						borderColorStr 		= topBorderListAr.get(2);
						borderThicknessStr 	= topBorderListAr.get(3);
						borderColorIntStr 	= topBorderListAr.get(4);
						if(!(borderTypeStr.equals("none")) && bottomBorderListAr.get(1).equals("none")){
							borderTypeStr = bottomBorderListAr.get(1);
						}
					}
				}*/
				
				if( borderSide != "" ) borderSide = borderSide + ",";
				borderSide = borderSide + typeStr;
				borderColors.add( borderColorStr );
				borderTypes.add( borderTypeStr );
				borderWidths.add( Integer.valueOf((int) Math.ceil( Double.parseDouble(borderThicknessStr) ) ) );
				borderColorInts.add( Integer.valueOf( borderColorIntStr ) );
			}
			
		}
		
		ArrayList< Object > retBorderAr = new ArrayList<Object>();
		
		retBorderAr.add(borderSide);
		retBorderAr.add(borderTypes);
		retBorderAr.add(borderColors);
		retBorderAr.add(borderWidths);
		retBorderAr.add(borderColorInts);
		
		return retBorderAr;
	}
	
	
	public static HashMap<String, Object> convertItemToBorder( HashMap<String, Object> _item )
	{
		int i = 0;
		String _borderTypeStr = "";
		
		if( _item.containsKey("borderSide"))
		{
			ArrayList<String> borderTypes = new ArrayList<String>();
			ArrayList<String> borderColors = new ArrayList<String>();
			ArrayList<Integer> borderColorInts = new ArrayList<Integer>();
			ArrayList<Integer> borderWidths = new ArrayList<Integer>();
			
			String borderTypeStr 		= "";
			String borderColorStr		= "";
			String borderColorIntStr	= "";
			String borderThicknessStr 	= "";
			
			borderColorStr 		= _item.get("borderColor")== null ? "#000000" :  _item.get("borderColor").toString();
			borderColorIntStr 	= _item.get("borderColorInt")==null ? "0" : _item.get("borderColorInt").toString();
			borderThicknessStr 	= _item.get("borderWidth").toString();
			
			String _borSideName = "";
			
			ArrayList<String> borderSide = (ArrayList<String>) _item.get("borderSide");
			
			for ( i = 0; i < borderSide.size(); i++) {
				
				borderTypeStr = "";
				_borSideName = borderSide.get(i);
				if( _borSideName.equals("top") )
				{
					_borderTypeStr = getPropertyValueString(_item, "topBorderType", "null");
					if( "null".equals(_borderTypeStr) == false )
					{
						borderTypeStr = getBorderType(_item.get("topBorderType").toString());
					}
					else
					{
						borderTypeStr = getBorderType(_item.get("borderType").toString());
					}
				}
				else if( _borSideName.equals("left") )
				{
					_borderTypeStr = getPropertyValueString(_item, "leftBorderType", "null");
					if( "null".equals(_borderTypeStr) == false )
					{
						borderTypeStr = getBorderType(_item.get("leftBorderType").toString());
					}
					else
					{
						borderTypeStr = getBorderType(_item.get("borderType").toString());
					}
				}
				else if( _borSideName.equals("right") )
				{
					_borderTypeStr = getPropertyValueString(_item, "rightBorderType", "null");
					if( "null".equals(_borderTypeStr) == false )
					{
						borderTypeStr = getBorderType(_item.get("rightBorderType").toString());
					}
					else
					{
						borderTypeStr = getBorderType(_item.get("borderType").toString());
					}
				}
				else if( _borSideName.equals("bottom") )
				{
					_borderTypeStr = getPropertyValueString(_item, "rightBorderType", "null");
					if( "null".equals(_borderTypeStr) == false )
					{
						borderTypeStr = getBorderType(_item.get("bottomBorderType").toString());
					}
					else
					{
						borderTypeStr = getBorderType(_item.get("borderType").toString());
					}
				}
				
				borderTypes.add(borderTypeStr);
				borderColors.add(borderColorStr);
				borderWidths.add( Integer.valueOf((int) Math.ceil( Double.parseDouble(borderThicknessStr) ) ) );
				borderColorInts.add( Integer.valueOf( borderColorIntStr ) );
			}
			
			if(borderSide.size() < 4)
			{
				ArrayList<String> defaultBorder = new ArrayList<String>();
				defaultBorder.add("left");
				defaultBorder.add("right");
				defaultBorder.add("bottom");
				defaultBorder.add("top");
				_item.put("borderType", "none");
				
				for ( i = 0; i < 4; i++) {
					if( borderSide.contains( defaultBorder.get(i) ) == false )
					{
						borderSide.add( defaultBorder.get(i) );
						borderTypes.add( "none" );
						borderColors.add( "#000000" );
						borderWidths.add( 0 );
						borderColorInts.add( 0 );
						
						if(defaultBorder.get(i).equals("top") ) _item.put("bottomBorderType", "none");
						else if(defaultBorder.get(i).equals("left") ) _item.put("leftBorderType", "none");
						else if(defaultBorder.get(i).equals("right") ) _item.put("rightBorderType", "none");
						else if(defaultBorder.get(i).equals("bottom") ) _item.put("topBorderType", "none");
					
					}
				}
			}
			
			if( borderTypes.size() > 0 )
			{
				_item.put( "isCell",true );	
				_item.put("borderTypes", 	borderTypes );
				_item.put("borderColors",  	borderColors );
				_item.put("borderWidths",  	borderWidths );
				_item.put("borderColorsInt", borderColorInts ); 
				_item.put("borderSide", borderSide ); 
				
			}
			
		}
		
		
		return _item;
	}
	
	
	
	/**
	 * 대문자로 들어오는 보더 속성을 소문자로 변경해 줌. 
	 * @return String _bStr
	 * 
	 */
	public static String getBorderType(String _bType)
	{
		String _bStr = "";

		if( _bType.equals("SOLD"))
		{
			_bStr = "solid";
		}
		else if( _bType.equals("solid"))
		{
			_bStr = "solid";
//			_bStr = "none";			//@BIT 비트 테이블의 보더값이 solid로 담겨있으나 실제 none값으로 처리해야함.
		}
		else if( _bType.equals("DASH"))
		{
			_bStr = "dash";
		}
		else if( _bType.equals("DASH_DOT"))
		{
			_bStr = "dash_dot";
		}
		else if( _bType.equals("DASH_DOT_DOT"))
		{
			_bStr = "dash_dot_dot";
		}
		else if( _bType.equals("DOT"))
		{
			_bStr = "dot";
		}
		else if( _bType.equals("DOUBLE"))
		{
			_bStr = "double";
		}
		else
		{
			_bStr = "none";
		}

		return _bStr;
	}
	
	/**
	 * int로 된 color를 Hex 로 변경 한다. 
	 * @return String _hex
	 * 
	 */
	public static String changeColorToHex(int _color)
	{
		String _hex = "";

		Color _c = new Color(_color);
		String _red = Integer.toHexString(_c.getRed());
		if( _red.length() == 1 ) _red = "0" +  _red;
		
		String _green = Integer.toHexString(_c.getGreen());
		if( _green.length() == 1 ) _green = "0" +  _green;
		
		String _blue = Integer.toHexString(_c.getBlue());
		if( _blue.length() == 1 ) _blue = "0" +  _blue;
		
//		_hex = "#" + Integer.toHexString(_c.getRed()) + Integer.toHexString(_c.getGreen()) + Integer.toHexString(_c.getBlue());
		_hex = "#" + _red + _green + _blue;
		
		return _hex;
	}
	
	public void setImageData( HashMap<String,String> _imageData)
	{
		this.mImageData = _imageData;
	}
	
	public void setChartData( HashMap<String,Object> _chartData)
	{
		this.mChartData = _chartData;
	}
	
	public void setFunction( Function _function )
	{
		this.mFunction = _function;
	}
	
	
	public HashMap<String, Object> changeLabelBandImgItem( HashMap<String, Object> _propList, Element _element,  HashMap<String, ArrayList<HashMap<String, Object>>> _data ) throws UnsupportedEncodingException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _className = String.valueOf( _propList.get("className") );
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			String _barcode_type = String.valueOf( _propList.get("barcodeType") );
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
				if(_barcodeValue == null || _barcodeValue.equals(""))
				{
					return null;
				}
				else
				{
					_propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
				}
					
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 7 ){
					_barcodeSrc="";
				}
				/*
				else if( _barcode_type.equalsIgnoreCase("itf14") && _barcodeData.length() != 14 ){
					_barcodeSrc="";
				}
				*/
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart"))
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			
			String _dataID = _propList.get("dataSet").toString();
			
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			PARAM = getChartParamToElement(_element );			
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToElement2(_element );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				String _dataIDs = _propList.get("dataSets").toString();
				String [] arrDataId = _dataIDs.split("%2C");
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = _data.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = String.valueOf( _propList.get("type") );
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else
			{
				ArrayList<HashMap<String, Object>> _list = _data.get( _dataID );			
			
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = String.valueOf( _propList.get("type") );
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		
		return _propList;
	}
	
	public HashMap<String, Object> changeLabelBandImgItemJson( HashMap<String, Object> _propList, HashMap<String, Value> _item,  HashMap<String, ArrayList<HashMap<String, Object>>> _data ) throws UnsupportedEncodingException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _className = String.valueOf( _propList.get("className") );
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			String _barcode_type = String.valueOf( _propList.get("barcodeType") );
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(_barcodeValue == null || _barcodeValue.equals(""))
				{
					return null;
				}
				else
				{
					_propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
				}
					
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 7 ){
					_barcodeSrc="";
				}
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") )
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			
			String _dataID = _propList.get("dataSet").toString();
			
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			PARAM = getChartParamToJson( _item );			
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToJson2( _item );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				String _dataIDs = _propList.get("dataSets").toString();
				String [] arrDataId = _dataIDs.split("%2C");
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = _data.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = String.valueOf( _propList.get("type") );
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else
			{
				ArrayList<HashMap<String, Object>> _list = _data.get( _dataID );			
			
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = String.valueOf( _propList.get("type") );
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		
		return _propList;
	}
	

	public HashMap<String, Object> changeLabelBandImgItemSimple( HashMap<String, Object> _propList, HashMap<String, Object> _item,  HashMap<String, ArrayList<HashMap<String, Object>>> _data ) throws UnsupportedEncodingException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _className = String.valueOf( _propList.get("className") );
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			String _barcode_type = String.valueOf( _propList.get("barcodeType") );
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String SHOW_LABEL = "false";
					String IMG_TYPE = "qrcode";
					String MODEL_TYPE = _barcode_type;
					String FILE_CONTENT = _propList.get("text").toString();
					
					try {
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(_barcodeValue == null || _barcodeValue.equals(""))
				{
					return null;
				}
				else
				{
					_propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
				}
				
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 7 ){
					_barcodeSrc="";
				}
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String SHOW_LABEL = _showLabel ? "true" : "false";
					String IMG_TYPE = "barcode";
					String MODEL_TYPE = _barcode_type;
					String FILE_CONTENT = _barcodeData;
					
					try {
						if("datamatrix".equals(MODEL_TYPE))
						{	
							MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") )
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			
			String _dataID = _propList.get("dataSet").toString();
			
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			PARAM = getChartParamToSimple(_item);
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToSimple2( _item );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				String _dataIDs = _propList.get("dataSets").toString();
				String [] arrDataId = _dataIDs.split("%2C");
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = _data.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = String.valueOf( _propList.get("type") );
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
					try {
						_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else
			{
				ArrayList<HashMap<String, Object>> _list = _data.get( _dataID );			
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = String.valueOf( _propList.get("type") );
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
					try {
						_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		
		return _propList;
	}

	private void convertTableMapToApprovalTbl( ArrayList<HashMap<String, Object>> arraylist, ArrayList<HashMap<String, Object>> _objects, HashMap<String, ArrayList<HashMap<String, Object>>> _data )
	{
		
		HashMap<String, Object> tblProperty = arraylist.get(0); 
		
		String _cellFixed = "left";	// UBApproval 아이템의 fixed포지션 left,right 
		int cnt = arraylist.size();		// UBApproval 아이템의 갯수 
		float _firstPosition = 0;		// 아이템의 처음 시작 X포지션
		String _dataSet = "";
		int dataCnt = 1;
		float _defaultW = 0;
		String bandName = String.valueOf(tblProperty.get("band"));
		
		if(tblProperty.containsKey("cellFix"))
		{
			_cellFixed = String.valueOf(tblProperty.get("cellFix"));
		}
		
		if(arraylist.get(2).containsKey("dataSet") && arraylist.get(2).get("dataType").equals("1") && arraylist.get(2).get("dataSet").equals("") == false )
		{
			_dataSet = String.valueOf(arraylist.get(2).get("dataSet"));
			dataCnt = _data.get(_dataSet).size();
		}
		else
		{
			dataCnt = 1;
		}
		
		if( "right".equals(_cellFixed) )
		{
			_firstPosition = Float.valueOf( String.valueOf(tblProperty.get("x")) ) -   ( Float.valueOf( String.valueOf(arraylist.get(2).get("width") ) ) * (dataCnt-1));
		}
		else
		{
			_firstPosition = Float.valueOf( String.valueOf(tblProperty.get("x")) );
		}
		
		float _addPosition = _firstPosition;
		HashMap<String, Object> cloneItems = null;
		
		for (int i = 1; i < cnt; i++) {
			if(i == 1)
			{
				arraylist.get(i).put("x",_firstPosition );
				_objects.add( arraylist.get(i) );
				
				_firstPosition = _firstPosition +  Float.valueOf( String.valueOf(arraylist.get(i).get("width")));
			}
			else
			{
				_addPosition = _firstPosition;
				
				for (int j = 0; j < dataCnt; j++) {
					
					cloneItems = (HashMap<String, Object>) arraylist.get(i).clone();
					
					if( "".equals(_dataSet) == false && !cloneItems.get("column").equals("") && !cloneItems.get("column").equals("null") )
					{
						cloneItems.put("dataType", "0" );
						cloneItems.put("text", _data.get(_dataSet).get(j).get(cloneItems.get("column")) );
					}
					
					// 보더 업데이트를 위하여 오른쪽보더를 제거
					if(j < dataCnt-1)
					{
						String borderSide = String.valueOf(cloneItems.get("borderSide"));
						ArrayList<String> borderType =(ArrayList<String>) ((ArrayList<String>) cloneItems.get("borderTypes")).clone();
						String[] _sideAr = borderSide.split(",");
						borderSide = "";
						for (int k = 0; k < _sideAr.length; k++) {
							if( "right".equals( _sideAr[k] ) )
							{
								borderType.set(k, "none");
							}
						}
						
						cloneItems.put("borderTypes", borderType );
					}
					
					cloneItems.put("x", _addPosition );
					_objects.add( cloneItems );
					_addPosition = _addPosition + Float.valueOf( String.valueOf( cloneItems.get("width")));
				}
			}
		}
		
		
	}
	
	
	public ArrayList<HashMap<String, Object>> convertExportTableItems( ArrayList<HashMap<String, Object>> _listAr, float _pageW, float _pageH, String _exportType )
	{
		
		int i = 0;
		int max = 0;
		int j = 0;
		int max2 = 0;
		int _position = 0;
		
		ArrayList<ArrayList<HashMap<String, Object>>> _tablesAr = new ArrayList<ArrayList<HashMap<String,Object>>>();
		ArrayList<HashMap<String, Object>> _tablese = null;
		ArrayList<String> _tableNames = new ArrayList<String>();
		String _tableName = "";
		ArrayList<String> _noUseCellBorderType = new ArrayList<String>();
		ArrayList<String> _noUseCellBorderSide = new ArrayList<String>();
		ArrayList<Integer> _noUseCellColorInt = new ArrayList<Integer>();
		ArrayList<Integer> _noUseCellBorderWidths = new ArrayList<Integer>();
		String[] _defaultBorderSide = {"left","right","top","bottom"};
		max = _listAr.size();
		
		float _scale = 1;
		
		if( "WORD".equals(_exportType))
		{
			if( _pageW < _pageH )
			{
				if(_pageW > 794) _scale = 794 / _pageW; 
			}
			else
			{
				if(_pageH > 794) _scale = 794 / _pageH; 
			}
		}
		
		
		for ( i = 0; i < _defaultBorderSide.length; i++) {
			_noUseCellBorderType.add("none");
			_noUseCellBorderSide.add(_defaultBorderSide[i]);
			_noUseCellColorInt.add(0);
			_noUseCellBorderWidths.add(1);
		}
		
//		String[] _scaleAr = {"x","y","width","height","cellOverHeight","cellOutHeight","cellY","cellHeight","fontSize","padding"};
		String[] _scaleAr = {"fontSize","padding"};
		
		// 아이템들을 돌아서 Table타입의 아이템을 가져오기
		for ( i = 0; i < max; i++) {
			
			if( _scale < 1 )
			{
				for (int k = 0; k < _scaleAr.length; k++) {
					if( _listAr.get(i).containsKey( _scaleAr[k]) )
					{
						_listAr.get(i).put(_scaleAr[k], Float.valueOf(_listAr.get(i).get(_scaleAr[k]).toString()) * _scale );
					}
					
				}
			}
			
			if( _listAr.get(i).containsKey("isTable") && "true".equals( _listAr.get(i).get("isTable") ) ){
				
				_tableName = _listAr.get(i).get("TABLE_ID").toString();
				_position = _tableNames.indexOf(_tableName);
				if( _position == -1 )
				{	
					_tableNames.add(_tableName);
					_tablese = new ArrayList<HashMap<String,Object>>();
					_tablesAr.add(_tablese);
					_position = _tablesAr.size()-1;
				}
				
				_tablesAr.get(_position).add(_listAr.get(i));
				
			}
		}
		
		// 가져온 아이템을 Export할수 있도록 Table형태로 구성하고 기존 담겨있는 아이템은 제거 처리
		max = _tablesAr.size();
		
		float _x = 0;
		float _width = 0;
		float _y = 0;
		float _height = 0;
		float _maxW = 0;
		float _maxH = 0;
		
		for ( i = 0; i < max; i++) {

			_tablese = _tablesAr.get(i);
			
			ArrayList<Float> _xAr = new ArrayList<Float>();
			ArrayList<Float> _yAr = new ArrayList<Float>();
			
			max2 = _tablese.size();
			
			_maxH = 0;
			_maxW = 0;
			
			float _overPosition = 0;
			float _outPosition = 0;
			float _convertHeight = 0;
			
			for ( j = 0; j < max2; j++) {
				
				_overPosition = 0;
				_outPosition = 0;
				
				if( _tablese.get(j).containsKey("cellOverHeight") && Float.valueOf(_tablese.get(j).get("cellOverHeight").toString()) > 0 )
				{
					_overPosition = Float.valueOf(_tablese.get(j).get("cellOverHeight").toString());
				}
				
				if( _tablese.get(j).containsKey("cellOutHeight") && Float.valueOf(_tablese.get(j).get("cellOutHeight").toString()) > 0 )
				{
					_outPosition =  Float.valueOf(_tablese.get(j).get("cellOutHeight").toString());
				}
				
				_x 		= (float) Math.floor( Float.valueOf(_tablese.get(j).get("x").toString() ) );
				_width 	= (float) Math.floor( Float.valueOf(_tablese.get(j).get("width").toString() )) ;
				
				if( _tablese.get(j).containsKey("cellY") )
				{
					_y 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("cellY").toString() )*10 ) / 10 ) + _overPosition;
				}
				else
				{
					_y 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("y").toString() )*10 ) / 10 ) + _overPosition;
				}
				// 20170123
				if( _tablese.get(j).containsKey("cellHeight") )
				{
					_height 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("cellHeight").toString() )*10 ) / 10 ) - _overPosition; 
				}
				else
				{
					_height 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("height").toString() )*10 ) / 10 ) - _overPosition; 
				}
				
//				if( _height < 0  )
//				{
//					if( _tablese.get(j).containsKey("cellY") )
//					{
//						_y 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("cellY").toString() )*10 ) / 10 ) - _overPosition;
//					}
//					else
//					{
//						_y 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("y").toString() )*10 ) / 10 ) - _overPosition;
//					}
//					
//					if( _tablese.get(j).containsKey("cellHeight") )
//					{
//						_height 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("cellHeight").toString() )*10 ) / 10 ); 
//					}
//					else
//					{
//						_height 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("height").toString() )*10 ) / 10 ); 
//					}
//				}
				
				
				if( _maxW < _x + _width)
				{
					_maxW = _x + _width; 
				}
				 
				_tablese.get(j).put("x", _x );
				_tablese.get(j).put("width", _width );
				_tablese.get(j).put("cellHeight", _height);
				_tablese.get(j).put("cellY", _y );
				
//				float _cellRealH = (float) Math.floor( Float.valueOf(_tablese.get(j).get("height").toString() ) );
				float _cellRealH = Float.valueOf(_tablese.get(j).get("height").toString() );
						
				if( _tablese.get(j).containsKey("repeatedValue") && "true".equals(_tablese.get(j).get("repeatedValue").toString()) )
				{
					_height = Float.valueOf(_tablese.get(j).get("height").toString() )  - _overPosition - _outPosition;
					_tablese.get(j).put("height", _height );
				}
				
				_convertHeight =  ((float)  Math.round(  (_y + _height) * 10 ) )/10; 
				
				
				if(_xAr.contains( _x ) == false  )
				{
					_xAr.add( _x );
				}
//				if(_xAr.contains( _x + _width ) == false  )
//				{
//					_xAr.add( _x + _width );
//				}
				if(_yAr.contains( _y ) == false  )
				{
					_yAr.add( _y );
				}
				if(_yAr.contains( _convertHeight ) == false  )
				{
					_yAr.add( _convertHeight );
				}
//				if(_yAr.contains( _y  + _height ) == false  )
//				{
//					_yAr.add( _y  + _height );
//				}
			}
			
			_xAr.add(_maxW);
			
			_xAr = sortFloatArrayList(_xAr,"ASC");
			_yAr = sortFloatArrayList(_yAr,"ASC");
			
			if( _scale < 1 )
			{
				int _colSpanStIdx = 0;
				int _colSpanEdIdx = 0;
				int _rowSpanStIdx = 0;
				int _rowSpanEdIdx = 0;
				int k = 0;
				String[] _chkAr = {"x","y","width","height","cellOverHeight","cellOutHeight","cellY","cellHeight"};
				for ( k = 0; k < _tablese.size(); k++) {
					
					_colSpanStIdx = _xAr.indexOf( Float.valueOf( _tablese.get(k).get("x").toString()  ) );
					_colSpanEdIdx = _xAr.indexOf( Float.valueOf( _tablese.get(k).get("x").toString() ) +  Float.valueOf( _tablese.get(k).get("width").toString()  ) );
					_rowSpanStIdx = _yAr.indexOf( Float.valueOf( _tablese.get(k).get("y").toString() ) );
					_rowSpanEdIdx = _yAr.indexOf( Float.valueOf( _tablese.get(k).get("y").toString() ) +  Float.valueOf( _tablese.get(k).get("height").toString()  ));
					
					if( _colSpanStIdx > -1 ) _tablese.get(k).put("colIndex", _colSpanStIdx);
					if( _colSpanEdIdx > -1 )  _tablese.get(k).put("colSpan", _colSpanEdIdx - _colSpanStIdx );
					if( _rowSpanStIdx > -1 ) _tablese.get(k).put("rowIndex", _rowSpanStIdx);
					if( _colSpanEdIdx > -1 )  _tablese.get(k).put("rowSpan", _rowSpanEdIdx - _rowSpanStIdx );
					
					for (int l = 0; l < _chkAr.length; l++) {
						
						if( _tablese.get(k).containsKey(_chkAr[l]) )
						{	
							_tablese.get(k).put( _chkAr[l] , Math.round( Float.valueOf( _tablese.get(k).get(_chkAr[l]).toString() ) * _scale ) );
						}
					}
					
					
				}
				
				for ( k = 0; k < _xAr.size(); k++) {
					_xAr.set(k, (float) Math.round( _xAr.get(k) * _scale) );
				}
				
				for ( k = 0; k < _yAr.size(); k++) {
					_yAr.set(k, (float) Math.round( _yAr.get(k) * _scale) );
				}
				
			}
			
			
			// 담긴 XAr과 YAr값을 이용하여 아이템별로 colIndex,rowIndex, colSpan, rowSpan 값을 구하여 담기
			HashMap<String, Object> _tableInfo = mItemPropVar.getItemName("UBTable");
			
			//Table Width, Height, Colsapn, Rowspan , Rows 속성 지정
			_tableInfo.put("width"		, _xAr.get(_xAr.size()-1)-_xAr.get(0) );
			_tableInfo.put("height"		, _yAr.get(_yAr.size()-1)-_yAr.get(0) );
			_tableInfo.put("columnCount", _xAr.size()-1 );
			_tableInfo.put("rowCount"	, _yAr.size()-1 );
			
			ArrayList<ArrayList<HashMap<String, Object>>> _rows = new ArrayList<ArrayList<HashMap<String,Object>>>();
			
			ArrayList<HashMap<String, Object>> _cellAr = new ArrayList<HashMap<String, Object>>();

			ArrayList<HashMap<String, Object>> _tblCells = new ArrayList<HashMap<String, Object>>();
			
			HashMap<String, Object> _tempCell = new HashMap<String, Object>();
			_tempCell.put("TYPE", "NONE");
			
			ArrayList<Float> _widthInfo = new ArrayList<Float>();
			ArrayList<Float> _heightInfo = new ArrayList<Float>();
			
			// Rows에 각 Row별 아이템을 생성하여 담기
			float _size = 0;
			for ( j = 0; j < (_yAr.size()-1); j++) {

				_cellAr = new ArrayList<HashMap<String,Object>>();
				
				for (int k = 0; k < (_xAr.size()-1); k++) {
					_cellAr.add( (HashMap<String, Object> ) _tempCell.clone() );
					
					if( j == 0  )
					{
						_size = _xAr.get(k+1) - _xAr.get(k);
						_widthInfo.add(_size);
					}
					
					if( k == 0 )
					{
						_size = _yAr.get(j+1) - _yAr.get(j);
						_heightInfo.add(_size);
					}
					
				}
				
				_rows.add( _cellAr );
			}
			
			int _colspan = 1;
			int _colindex = 1;
			int _rowspan = 1;
			int _rowindex = 1;
			
			// 각 Cell별 정보 업데이트 및 Row에 Add
			for ( j = 0; j < max2; j++) {
				
				_x = Float.valueOf(_tablese.get(j).get("x").toString() );
				_width = Float.valueOf(_tablese.get(j).get("width").toString() );
				_y = Float.valueOf(_tablese.get(j).get("cellY").toString() );
				_height = Float.valueOf(_tablese.get(j).get("cellHeight").toString() );
				
				if( _tablese.get(j).containsKey("repeatedValue") && "true".equals(_tablese.get(j).get("repeatedValue").toString()) )
				{
					_height = Float.valueOf(_tablese.get(j).get("height").toString() );
				}
				
				_convertHeight =  ((float)  Math.round(  (_y + _height) * 10 ) )/10; 
				
				_colindex = _xAr.indexOf(_x);
				_rowindex = _yAr.indexOf(_y);
				
				int _rowCnt = _yAr.indexOf( _convertHeight );
				if( _yAr.indexOf(_y) > _yAr.indexOf( _convertHeight ) )
				{
					_rowindex = _yAr.indexOf( _convertHeight );
					_rowCnt = _yAr.indexOf(_y);
				}
				
//				_colspan = _xAr.indexOf( _x + _width )  - _colindex;
				_colspan = 1;
				_rowspan = _rowCnt - _rowindex;
				
				if( _tablese.get(j).containsKey("colSpan") == false )_tablese.get(j).put("colSpan", _colspan);
				if( _tablese.get(j).containsKey("rowSpan") == false )_tablese.get(j).put("rowSpan", _rowspan);
				if( _tablese.get(j).containsKey("colIndex") == false )_tablese.get(j).put("colIndex", _colindex);
				if( _tablese.get(j).containsKey("rowIndex") == false )_tablese.get(j).put("rowIndex", _rowindex);
				
				_rows.get(_rowindex).set(_colindex, _tablese.get(j));
				
				if(_colspan > 1 || _rowspan > 1 )
				{
					for (int k = _colindex; k < _colindex + _colspan; k++) {
						for (int k2 = _rowindex; k2 < _rowindex + _rowspan ; k2++) {
							if(k==_colindex && k2 == _rowindex) continue;
							
							if( _rows.get(k2).get(k) != null && _rows.get(k2).get(k).containsKey("TYPE") && "NONE".equals( _rows.get(k2).get(k).get("TYPE") )  )
							{
								_rows.get(k2).set(k, null);
							}
						}
					}
				}
				
			}
			
			int _rowMaxCnt = _rows.size();
			int _rowSubMaxCnt = 0;
			HashMap<String, Object> _currentCell;
			
			int _chkR = 0;
			int _chkC = 0;
			int _maxR = 0;
			int _maxC = 0;

			String _bandName = "";
			String _headerBandName = "";

			for ( j = 0; j < _rowMaxCnt; j++) {
				
				_rowSubMaxCnt = _rows.get(j).size();
				
				for (int k = 0; k < _rowSubMaxCnt; k++) {
					
					_currentCell = _rows.get(j).get(k); 
					
					
					if(_currentCell != null)
					{
						if( _bandName.equals("") && _currentCell.containsKey("BAND_NAME"))
						{
							_bandName = _currentCell.get("BAND_NAME").toString();
						}
						if(  _headerBandName.equals("") && _currentCell.containsKey("HEADER_BAND_NAME"))
						{
							_headerBandName = _currentCell.get("HEADER_BAND_NAME").toString();
						}
					}
					
					if( _currentCell != null && _currentCell.containsKey("TYPE") == false  )
					{
						_maxR = Integer.valueOf( _currentCell.get("rowSpan").toString() ) + j;
//						_maxC = Integer.valueOf( _currentCell.get("colSpan").toString() );
						_maxC = _rowSubMaxCnt;
						
						if(_maxC > 1 )
						{
							for ( _chkR = j;  _chkR <_maxR; _chkR++) {
								for ( _chkC = k+1; _chkC < _maxC; _chkC++) {
//									if(_chkC == k && _chkR == j ) continue;
									
									if( _rows.get(_chkR).get(_chkC) == null || _rows.get(_chkR).get(_chkC).containsKey("TYPE") == false )
									{
										_maxC = _chkC;
										_currentCell.put("colSpan", _chkC - k );
										_currentCell.put("width", _xAr.get(_chkC) - _xAr.get(k) );
									}
									else if(  _rows.get(_chkR).get(_chkC) != null &&  _rows.get(_chkR).get(_chkC).containsKey("TYPE")  && "NONE".equals( _rows.get(_chkR).get(_chkC).get("TYPE") ) )
									{
										if( _chkC == _rowSubMaxCnt-1 )
										{
											_currentCell.put("colSpan", _chkC - k+1 );
											_currentCell.put("width", _xAr.get(_chkC+1) - _xAr.get(k) );
										}
										_rows.get(_chkR).set(_chkC, null);
									}
									
								}
							}
						}
					}
					
				}
				
			}
			
			
			// 정렬을 위한 변수 지정
//			ArrayList<String> _cols = new ArrayList<String>();
//			_cols.add("colIndex");
//			ArrayList<String> _desc = new ArrayList<String>();
//			_desc.add("false");
//			ArrayList<String> _numeric = new ArrayList<String>();
//			_numeric.add("true");
			
			int _rowSize = _rows.size();
			int _cellSize = 0;
			HashMap<String, Object> _curMap = null;
			HashMap<String, Object> _noCellMap = null;
			HashMap<String, Object> _defaultItem = null;

			// 필요없는 셀 제거 및 빈 셀에 아이템 채우기 
			int _colIndex = 0;
			for (int k = 0; k < _rowSize; k++) {
				_cellSize = _rows.get(k).size();
				
				_noCellMap = null;
				_colIndex = 0;
				
				for (int k2 = 0; k2 < _cellSize; k2++) {
					_curMap = _rows.get(k).get(k2);
					
					if( _curMap != null && _curMap.containsKey("TYPE") &&  _curMap.get("TYPE").equals("NONE")  )
					{
						if( _noCellMap != null )
						{
							_noCellMap.put("colSpan", Integer.valueOf( _noCellMap.get("colSpan").toString() ) + 1 );
							_noCellMap.put("width", Float.valueOf( _noCellMap.get("width").toString() ) + _widthInfo.get(_colIndex) );
							
							_rows.get(k).set(k2,  null );
							
							_rows.get(k).remove( _rows.get(k).get(k2) );
							k2 = k2 - 1;
							_cellSize = _cellSize -1;
						}
						else
						{
							_noCellMap = new HashMap<String, Object>();
							_noCellMap.put("colSpan","1");
							_noCellMap.put("rowSpan","1");
							_noCellMap.put("colIndex",_colIndex);
							_noCellMap.put("rowIndex",k);

							_noCellMap.put("width",_widthInfo.get(_colIndex));
							_noCellMap.put("height",_heightInfo.get(k));
							
							_noCellMap.put("borderTypes",_noUseCellBorderType);
							_noCellMap.put("borderSide",_noUseCellBorderSide);
							_noCellMap.put("borderWidths",_noUseCellBorderWidths);
							_noCellMap.put("borderColorsInt",_noUseCellColorInt);
							_noCellMap.put("backgroundColorInt",16777215);
							_noCellMap.put("backgroundAlpha",0);
							
							// 빈셀의 기본값을 최소로 지정 ( 지정되지 않을경우 ppt에서 빈 여백에 공백값이 화면과 다른 현상이 발생
							_noCellMap.put("lineHeight",1);
							_noCellMap.put("fontSize",5);
							_noCellMap.put("text"," ");
							_noCellMap.put("CELL_TYPE","EMPTY_CELL");
							
							_rows.get(k).set(k2, _noCellMap);
//							_tblCells.add( _noCellMap );
						}
					}
					else
					{					
						if(k == 0 && k2 == 0 )
						{
							_defaultItem = _rows.get(k).get(k2);
						}
						else if(_rows.get(k).get(k2) != null )
						{
							_listAr.remove(_rows.get(k).get(k2));
						}
						
						if( _rows.get(k).get(k2) == null )
						{
							_rows.get(k).remove(k2);
							_cellSize = _cellSize - 1;
							k2 = k2 - 1;
						}
						else
						{
							_tblCells.add(_rows.get(k).get(k2));
						}
						_noCellMap = null;
						
					}
					_colIndex++;
					
				}
				
			}
			
//			for ( j = 0; j < _rows.size(); j++) {
//				UBIDataParser.sortDataSet(_rows.get(j), _cols, _desc, _numeric);
//			}
			
			_tableInfo.put("id",  _tableNames.get(i) );
			_tableInfo.put("className", "UBTable" );
			_tableInfo.put("rows", _rows );
			_tableInfo.put("cells", _tblCells );
			_tableInfo.put("widthInfo", _widthInfo );
			_tableInfo.put("heightInfo", _heightInfo );
			_tableInfo.put("x", _xAr.get(0) );
			_tableInfo.put("y", _yAr.get(0) );
			
			if( !_bandName.equals("") ) _tableInfo.put("BAND_NAME", _bandName );
			if( !_headerBandName.equals("") ) _tableInfo.put("HEADER_BAND_NAME", _headerBandName );
			
			_listAr.set( _listAr.indexOf(_defaultItem), _tableInfo);
			_listAr.remove(_defaultItem);
			
		}
		// 테이블 맵 생성 종료
		
		// 테이블 맵 리턴
		
		return _listAr;
	}
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> convertExportHtmlTableItems( ArrayList<HashMap<String, Object>> _listAr, float _pageW, float _pageH, String _exportType )
	{
		
		int i = 0;
		int max = 0;
		int j = 0;
		int max2 = 0;
		int _position = 0;
		
		ArrayList<HashMap<String, Object>> inclusiveItem = new ArrayList<HashMap<String, Object>>();

		ArrayList<ArrayList<HashMap<String, Object>>> _tablesAr = new ArrayList<ArrayList<HashMap<String,Object>>>();
		ArrayList<HashMap<String, Object>> _tablese = null;
		ArrayList<String> _tableNames = new ArrayList<String>();
		String _tableName = "";
		ArrayList<String> _noUseCellBorderType = new ArrayList<String>();
		ArrayList<String> _noUseCellBorderSide = new ArrayList<String>();
		ArrayList<Integer> _noUseCellColorInt = new ArrayList<Integer>();
		ArrayList<Integer> _noUseCellBorderWidths = new ArrayList<Integer>();
		String[] _defaultBorderSide = {"left","right","top","bottom"};
		max = _listAr.size();
		
		float _scale = 1;
		
		if( "WORD".equals(_exportType))
		{
			if( _pageW < _pageH )
			{
				if(_pageW > 794) _scale = 794 / _pageW; 
			}
			else
			{
				if(_pageH > 794) _scale = 794 / _pageH; 
			}
		}
		
		
		for ( i = 0; i < _defaultBorderSide.length; i++) {
			_noUseCellBorderType.add("none");
			_noUseCellBorderSide.add(_defaultBorderSide[i]);
			_noUseCellColorInt.add(0);
			_noUseCellBorderWidths.add(1);
		}
		
		String[] _scaleAr = {"x","y","width","height","cellOverHeight","cellOutHeight","cellY","cellHeight","fontSize","padding"};
		
		// 아이템들을 돌아서 Table타입의 아이템을 가져오기
		for ( i = 0; i < max; i++) {
			
			if( _scale < 1 )
			{
				for (int k = 0; k < _scaleAr.length; k++) {
					if( _listAr.get(i).containsKey( _scaleAr[k]) )
					{
						_listAr.get(i).put(_scaleAr[k], Float.valueOf(_listAr.get(i).get(_scaleAr[k]).toString()) * _scale );
					}
					
				}
			}
			
			if( _listAr.get(i).containsKey("isTable") && "true".equals( _listAr.get(i).get("isTable") ) ){
				
				_tableName = _listAr.get(i).get("TABLE_ID").toString();
				_position = _tableNames.indexOf(_tableName);
				if( _position == -1 )
				{	
					_tableNames.add(_tableName);
					_tablese = new ArrayList<HashMap<String,Object>>();
					_tablesAr.add(_tablese);
					_position = _tablesAr.size()-1;
				}
				
				_tablesAr.get(_position).add(_listAr.get(i));
				
			}else{
				inclusiveItem.add(_listAr.get(i));
			}
		}
		
		// 가져온 아이템을 Export할수 있도록 Table형태로 구성하고 기존 담겨있는 아이템은 제거 처리
		max = _tablesAr.size();
		
		float _x = 0;
		float _width = 0;
		float _y = 0;
		float _height = 0;
		float _maxW = 0;
		float _maxH = 0;
		
		ArrayList<HashMap<String, Object>> tableInfo = new ArrayList<HashMap<String, Object>>();
		
		for ( i = 0; i < max; i++) {

			_tablese = _tablesAr.get(i);
			
			ArrayList<Float> _xAr = new ArrayList<Float>();
			ArrayList<Float> _yAr = new ArrayList<Float>();
			
			max2 = _tablese.size();
			
			_maxH = 0;
			_maxW = 0;
			
			float _overPosition = 0;
			float _outPosition = 0;
			float _convertHeight = 0;
			
			for ( j = 0; j < max2; j++) {
				
				_overPosition = 0;
				_outPosition = 0;
				
				if( _tablese.get(j).containsKey("cellOverHeight") && Float.valueOf(_tablese.get(j).get("cellOverHeight").toString()) > 0 )
				{
					_overPosition = Float.valueOf(_tablese.get(j).get("cellOverHeight").toString());
				}
				
				if( _tablese.get(j).containsKey("cellOutHeight") && Float.valueOf(_tablese.get(j).get("cellOutHeight").toString()) > 0 )
				{
					_outPosition =  Float.valueOf(_tablese.get(j).get("cellOutHeight").toString());
				}
				
//				_x 		= getItemPositionChk( Float.valueOf(_tablese.get(j).get("x").toString() ),0, _xAr ) ;
//				_width 	= getItemPositionChk( Float.valueOf(_tablese.get(j).get("width").toString() ), _x, _xAr ) - _x;
//				_y 		= getItemPositionChk( Float.valueOf(_tablese.get(j).get("cellY").toString() ),0, _yAr );
//				_height = getItemPositionChk( Float.valueOf(_tablese.get(j).get("cellHeight").toString() ) ,_y, _yAr ) - _y;
				
//				_x 		= (float) Math.floor( Float.valueOf(_tablese.get(j).get("x").toString() ) );
//				_width 	= (float) Math.floor( Float.valueOf(_tablese.get(j).get("width").toString() )) ;
//				_y 		= (float) Math.floor(  Float.valueOf(_tablese.get(j).get("cellY").toString() )) - _overPosition;
//				_height = (float) Math.floor( Float.valueOf(_tablese.get(j).get("cellHeight").toString() )) - _overPosition - _outPosition;  

				_x 		= (float) Math.floor( Float.valueOf(_tablese.get(j).get("x").toString() ) );
				_width 	= (float) Math.floor( Float.valueOf(_tablese.get(j).get("width").toString() )) ;
				
				_y 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("cellY").toString() )*10 ) / 10 ) + _overPosition;
				// 20170123
//				_height 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("cellHeight").toString() )*10 ) / 10 ) - _overPosition - _outPosition; 
				_height 		=  ( (float)  Math.round(  Float.valueOf(_tablese.get(j).get("cellHeight").toString() )*10 ) / 10 ) - _overPosition; 
				
				
				_convertHeight =  ((float)  Math.round(  (_y + _height) * 10 ) )/10; 
				
//				_y 		= (float) Float.valueOf(_tablese.get(j).get("cellY").toString() ) + _overPosition;
//				_height = (float) Float.valueOf(_tablese.get(j).get("cellHeight").toString() ) - _overPosition - _outPosition;  
				
//				_height = getItemPositionChk( (float) Math.floor( Float.valueOf(_tablese.get(j).get("cellHeight").toString() ) ) ,_y, _yAr ) - _y ;
				
				if( _maxW < _x + _width)
				{
					_maxW = _x + _width; 
				}
				 
				_tablese.get(j).put("x", _x );
				_tablese.get(j).put("width", _width );
				_tablese.get(j).put("cellHeight", _height);
				_tablese.get(j).put("cellY", _y );
				
//				float _cellRealH = (float) Math.floor( Float.valueOf(_tablese.get(j).get("height").toString() ) );
				float _cellRealH = Float.valueOf(_tablese.get(j).get("height").toString() );
						
//				_tablese.get(j).put("height", getItemPositionChk( _cellRealH, _y, _yAr ) - _y );
				
				if( _tablese.get(j).containsKey("repeatedValue") && "true".equals(_tablese.get(j).get("repeatedValue").toString()) )
				{
					_height = Float.valueOf(_tablese.get(j).get("height").toString() )  - _overPosition - _outPosition;
					_tablese.get(j).put("height", _height );
				}
				
				
				if(_xAr.contains( _x ) == false  )
				{
					_xAr.add( _x );
				}
//				if(_xAr.contains( _x + _width ) == false  )
//				{
//					_xAr.add( _x + _width );
//				}
				if(_yAr.contains( _y ) == false  )
				{
					_yAr.add( _y );
				}
				if(_yAr.contains( _convertHeight ) == false  )
				{
					_yAr.add( _convertHeight );
				}
//				if(_yAr.contains( _y  + _height ) == false  )
//				{
//					_yAr.add( _y  + _height );
//				}
			}
			
			_xAr.add(_maxW);
			
			_xAr = sortFloatArrayList(_xAr,"ASC");
			_yAr = sortFloatArrayList(_yAr,"ASC");
			
			// 담긴 XAr과 YAr값을 이용하여 아이템별로 colIndex,rowIndex, colSpan, rowSpan 값을 구하여 담기
			HashMap<String, Object> _tableInfo = mItemPropVar.getItemName("UBTable");
			
			//Table Width, Height, Colsapn, Rowspan , Rows 속성 지정
			_tableInfo.put("width"		, _xAr.get(_xAr.size()-1)-_xAr.get(0) );
			_tableInfo.put("height"		, _yAr.get(_yAr.size()-1)-_yAr.get(0) );
			_tableInfo.put("columnCount", _xAr.size()-1 );
			_tableInfo.put("rowCount"	, _yAr.size()-1 );
			
			ArrayList<ArrayList<HashMap<String, Object>>> _rows = new ArrayList<ArrayList<HashMap<String,Object>>>();
			
			ArrayList<HashMap<String, Object>> _cellAr = new ArrayList<HashMap<String, Object>>();

			ArrayList<HashMap<String, Object>> _tblCells = new ArrayList<HashMap<String, Object>>();
			
			HashMap<String, Object> _tempCell = new HashMap<String, Object>();
			_tempCell.put("TYPE", "NONE");
			
			ArrayList<Float> _widthInfo = new ArrayList<Float>();
			ArrayList<Float> _heightInfo = new ArrayList<Float>();
			
			// Rows에 각 Row별 아이템을 생성하여 담기
			for ( j = 0; j < (_yAr.size()-1); j++) {

				_cellAr = new ArrayList<HashMap<String,Object>>();
				
				for (int k = 0; k < (_xAr.size()-1); k++) {
					_cellAr.add( (HashMap<String, Object> ) _tempCell.clone() );
					
					if( j == 0  )
					{
						_widthInfo.add(_xAr.get(k+1) - _xAr.get(k));
					}
					
					if( k == 0 )
					{
						_heightInfo.add(_yAr.get(j+1) - _yAr.get(j));
					}
					
				}
				
				_rows.add( _cellAr );
			}
			
			int _colspan = 1;
			int _colindex = 1;
			int _rowspan = 1;
			int _rowindex = 1;
			
			// 각 Cell별 정보 업데이트 및 Row에 Add
			for ( j = 0; j < max2; j++) {
				
				_x = Float.valueOf(_tablese.get(j).get("x").toString() );
				_width = Float.valueOf(_tablese.get(j).get("width").toString() );
				_y = Float.valueOf(_tablese.get(j).get("cellY").toString() );
				_height = Float.valueOf(_tablese.get(j).get("cellHeight").toString() );
				
				if( _tablese.get(j).containsKey("repeatedValue") && "true".equals(_tablese.get(j).get("repeatedValue").toString()) )
				{
					_height = Float.valueOf(_tablese.get(j).get("height").toString() );
				}
				
				_convertHeight =  ((float)  Math.round(  (_y + _height) * 10 ) )/10; 
				
				_colindex = _xAr.indexOf(_x);
				_rowindex = _yAr.indexOf(_y);
				
//				_colspan = _xAr.indexOf( _x + _width )  - _colindex;
				_colspan = 1;
				_rowspan = _yAr.indexOf( _convertHeight ) - _rowindex;
				
				_tablese.get(j).put("colSpan", _colspan);
				_tablese.get(j).put("rowSpan", _rowspan);
				_tablese.get(j).put("colIndex", _colindex);
				_tablese.get(j).put("rowIndex", _rowindex);
				
				_rows.get(_rowindex).set(_colindex, _tablese.get(j));
				
				if(_colspan > 1 || _rowspan > 1 )
				{
					for (int k = _colindex; k < _colindex + _colspan; k++) {
						for (int k2 = _rowindex; k2 < _rowindex + _rowspan ; k2++) {
							if(k==_colindex && k2 == _rowindex) continue;
							
							if( _rows.get(k2).get(k) != null && _rows.get(k2).get(k).containsKey("TYPE") && "NONE".equals( _rows.get(k2).get(k).get("TYPE") )  )
							{
								_rows.get(k2).set(k, null);
							}
						}
					}
				}
				
			}
			
			int _rowMaxCnt = _rows.size();
			int _rowSubMaxCnt = 0;
			HashMap<String, Object> _currentCell;
			
			int _chkR = 0;
			int _chkC = 0;
			int _maxR = 0;
			int _maxC = 0;
			
			for ( j = 0; j < _rowMaxCnt; j++) {
				
				_rowSubMaxCnt = _rows.get(j).size();
				
				for (int k = 0; k < _rowSubMaxCnt; k++) {
					
					_currentCell = _rows.get(j).get(k); 
					
					if( _currentCell != null && _currentCell.containsKey("TYPE") == false  )
					{
						_maxR = Integer.valueOf( _currentCell.get("rowSpan").toString() ) + j;
//						_maxC = Integer.valueOf( _currentCell.get("colSpan").toString() );
						_maxC = _rowSubMaxCnt;
						
						if(_maxC > 1 )
						{
							for ( _chkR = j;  _chkR <_maxR; _chkR++) {
								for ( _chkC = k+1; _chkC < _maxC; _chkC++) {
//									if(_chkC == k && _chkR == j ) continue;
									
									if( _rows.get(_chkR).get(_chkC) == null || _rows.get(_chkR).get(_chkC).containsKey("TYPE") == false )
									{
										_maxC = _chkC;
										_currentCell.put("colSpan", _chkC - k );
										_currentCell.put("width", _xAr.get(_chkC) - _xAr.get(k) );
									}
									else if(  _rows.get(_chkR).get(_chkC) != null &&  _rows.get(_chkR).get(_chkC).containsKey("TYPE")  && "NONE".equals( _rows.get(_chkR).get(_chkC).get("TYPE") ) )
									{
										if( _chkC == _rowSubMaxCnt-1 )
										{
											_currentCell.put("colSpan", _chkC - k+1 );
											_currentCell.put("width", _xAr.get(_chkC+1) - _xAr.get(k) );
										}
										_rows.get(_chkR).set(_chkC, null);
									}
									
								}
							}
						}
					}
					
				}
				
			}
			
			
			// 정렬을 위한 변수 지정
//			ArrayList<String> _cols = new ArrayList<String>();
//			_cols.add("colIndex");
//			ArrayList<String> _desc = new ArrayList<String>();
//			_desc.add("false");
//			ArrayList<String> _numeric = new ArrayList<String>();
//			_numeric.add("true");
			
			int _rowSize = _rows.size();
			int _cellSize = 0;
			HashMap<String, Object> _curMap = null;
			HashMap<String, Object> _noCellMap = null;
			HashMap<String, Object> _defaultItem = null;
			
			// 필요없는 셀 제거 및 빈 셀에 아이템 채우기 
			int _colIndex = 0;
			for (int k = 0; k < _rowSize; k++) {
				_cellSize = _rows.get(k).size();
				
				_noCellMap = null;
				_colIndex = 0;
				
				for (int k2 = 0; k2 < _cellSize; k2++) {
					_curMap = _rows.get(k).get(k2);
					
					if( _curMap != null && _curMap.containsKey("TYPE") &&  _curMap.get("TYPE").equals("NONE")  )
					{
						if( _noCellMap != null )
						{
							_noCellMap.put("colSpan", Integer.valueOf( _noCellMap.get("colSpan").toString() ) + 1 );
							_noCellMap.put("width", Float.valueOf( _noCellMap.get("width").toString() ) + _widthInfo.get(_colIndex) );
							
							_rows.get(k).set(k2,  null );
							
							_rows.get(k).remove( _rows.get(k).get(k2) );
							k2 = k2 - 1;
							_cellSize = _cellSize -1;
						}
						else
						{
							_noCellMap = new HashMap<String, Object>();
							_noCellMap.put("colSpan","1");
							_noCellMap.put("rowSpan","1");
							_noCellMap.put("colIndex",_colIndex);
							_noCellMap.put("rowIndex",k);

							_noCellMap.put("width",_widthInfo.get(_colIndex));
							_noCellMap.put("height",_heightInfo.get(k));
							
							_noCellMap.put("borderTypes",_noUseCellBorderType);
							_noCellMap.put("borderSide",_noUseCellBorderSide);
							_noCellMap.put("borderWidths",_noUseCellBorderWidths);
							_noCellMap.put("borderColorsInt",_noUseCellColorInt);
							_noCellMap.put("backgroundColorInt",16777215);
							_noCellMap.put("backgroundAlpha",0);
							
							_rows.get(k).set(k2, _noCellMap);
//							_tblCells.add( _noCellMap );
						}
					}
					else
					{
						if(k == 0 && k2 == 0 )
						{
							_defaultItem = _rows.get(k).get(k2);
						}
						else if(_rows.get(k).get(k2) != null )
						{
							_listAr.remove(_rows.get(k).get(k2));
						}
						
						if( _rows.get(k).get(k2) == null )
						{
							_rows.get(k).remove(k2);
							_cellSize = _cellSize - 1;
							k2 = k2 - 1;
						}
						else
						{
							_tblCells.add(_rows.get(k).get(k2));
						}
						_noCellMap = null;
					}
					_colIndex++;
					
				}
				
			}
			
//			for ( j = 0; j < _rows.size(); j++) {
//				UBIDataParser.sortDataSet(_rows.get(j), _cols, _desc, _numeric);
//			}
			
			_tableInfo.put("id",  _tableNames.get(i) );
			_tableInfo.put("className", "UBTable" );
			_tableInfo.put("rows", _rows );
			_tableInfo.put("cells", _tblCells );
			_tableInfo.put("widthInfo", _widthInfo );
			_tableInfo.put("heightInfo", _heightInfo );
			_tableInfo.put("x", _xAr.get(0) );
			_tableInfo.put("y", _yAr.get(0) );
			
			_listAr.set( _listAr.indexOf(_defaultItem), _tableInfo);
			_listAr.remove(_defaultItem);
			
			//table외 아이템들의 테이블 내 위치를 설정하기 위함 데이블 정보 저장
			tableInfo.add(_tableInfo);
			
		}
		// 테이블 맵 생성 종료
		
		// 테이블 맵 리턴
		HashMap<String, Object> _tableInfo;
		ArrayList<ArrayList<HashMap<String, Object>>> rowAr;
		
		HashMap<String, Object> currentItemData;
    	float xTblPos = 0;
		float yTblPos = 0;
		float tblWidth = 0;
		float tblHeight = 0;
		float xOtherPos = 0;
		float yOtherPos = 0;		
		
		for(int tt = 0; tt<tableInfo.size(); tt++){
			_tableInfo = (HashMap<String, Object>) tableInfo.get(tt);
			rowAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _tableInfo.get("rows");			
			int rowSize = rowAr.size();		
			for (int rIdx = 0; rIdx < rowSize; rIdx++) {//row count 				
				ArrayList<HashMap<String, Object>> colAr = rowAr.get(rIdx);
				int _colCnt = colAr.size();	// 현재 colAr의 인덱스				
				for (int colIdx = 0; colIdx < _colCnt; colIdx++) {//column count														
					HashMap<String, Object> cellItem = colAr.get(colIdx);
					if(!cellItem.containsKey("x"))continue;
					xTblPos = Float.parseFloat(cellItem.get("x").toString());
					yTblPos = Float.parseFloat(cellItem.get("y").toString());
					tblWidth = Float.parseFloat(cellItem.get("width").toString());
					tblHeight = Float.parseFloat(cellItem.get("height").toString());					
					
					for ( i = 0; i < inclusiveItem.size(); i++) {						
						currentItemData = (HashMap<String, Object>)inclusiveItem.get(i);
						xOtherPos = Float.parseFloat(currentItemData.get("x").toString());
						yOtherPos = Float.parseFloat(currentItemData.get("y").toString());
						//테이블 안에 있는 확인
						if((xTblPos <= xOtherPos && xOtherPos <= (xTblPos + tblWidth)) && (yTblPos <= yOtherPos && yOtherPos <= (yTblPos + tblHeight)) ){	
							currentItemData.put("leftPadding", xOtherPos - xTblPos );
							currentItemData.put("topPadding", yOtherPos - yTblPos );
							cellItem.put("inclusiveItem", currentItemData);
							break;
						}
					}		
					colAr.set(colIdx, cellItem);					
				}
				rowAr.set(rIdx, colAr);
			}
			tableInfo.set(tt, _tableInfo);
		}	
		
		Collections.sort(tableInfo, new Comparator<HashMap<String, Object>>() {	
			public int compare(HashMap<String, Object> b1, HashMap<String, Object> b2) {	
				return (Float.parseFloat(b1.get("y").toString()) < Float.parseFloat(b2.get("y").toString())) ? -1: (Float.parseFloat(b1.get("y").toString()) < Float.parseFloat(b2.get("y").toString())) ? 1:0 ;
			}		
		});

	
		
		return tableInfo;
	}
	
	private ArrayList<Float> sortFloatArrayList( ArrayList<Float> _ar, String _desc )
	{
		if(_desc.equals("DESC"))
		{
			//DESC 내림차순
			Collections.sort(_ar, new Comparator<Float>(){
				public int compare(Float obj1, Float obj2)
				{
					return (obj1 > obj2) ? -1: (obj1 > obj2) ? 1:0 ;
				}
			}); 
			
		}
		else
		{
			//ASC 오름차순
			Collections.sort( _ar, new Comparator<Float>(){
				public int compare(Float obj1, Float obj2)
				{
					return (obj1 < obj2) ? -1: (obj1 > obj2) ? 1:0 ;
				}
			}); 
		}
		
		return _ar;
	}
	
	private float getItemPositionChk(float _p, float _addP, ArrayList<Float> _Ar)
	{
		float _retFl = 0;
		float _range = 1;
		
//		_p = Math.round(_p); 

		_p = Math.round(_p) + _addP;
		float st =  _p - _range;
		float max = _p + _range;
		
		for (float  i = st ; i <=  max; i++) {
			
			if( _Ar.contains( i) )
			{
				return i;
			}
		}
		
		return _p;
	}
	
	public HashMap<String, Object> convertUBSvgItem( Object _value, HashMap<String, Object> _item )
	{
		if( "".equals( _value ) ) return null;
		
		if( "EXCEL".equals(isExportType) || "WORD".equals(isExportType) || "HWP".equals(isExportType)  || "PDF".equals(isExportType)  || "PRINT".equals(isExportType))
		{
			ArrayList<ArrayList<HashMap<Object, String>>> _rowList = (ArrayList<ArrayList<HashMap<Object, String>>>) convertSvgRichText(_value,_item);
			
			if(_rowList == null || _rowList.size() == 0 )  return null;
			
			//_rowList.get(0) ( g객체 {fontFamily:돋움,fontSize:11,lines:[[]]}  형태로 구성
			//_rowList.get(0).get("lines") // 라인별 객체를 담아둔 ArrayList 
			// lines = ArrayList<ArrayList<HashMap<Object, String>>> 형태로   [row[ column[] ] , row[ column[] ] ] 위와 같은 형태로 구성되어 있음.
			
			_item.put("children",  _rowList );		
		}
		else
		{
			String _svgTag = (String) convertSvgRichText(_value,_item);
			if( "".equals( _svgTag ) ) return null;
			
			boolean _preserveAspectRatio = (_item.containsKey("preserveAspectRatio") )?_item.get("preserveAspectRatio").toString().equals("true"):false;
			boolean _fixedToSize = (_item.containsKey("fixedToSize") )?_item.get("fixedToSize").toString().equals("true"):false;
			
			
			//변환된 svg태그를 인코딩하기 위하여 처리
			_svgTag = StringUtil.replaceSVGTag(_svgTag.toString(), _preserveAspectRatio, _fixedToSize, _item);	
			_svgTag = _svgTag.replace(" ", "%20");

			try {
				_item.put("data",  URLEncoder.encode(_svgTag, "UTF-8") );
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return _item;
	}

	public HashMap<String, Object> convertUBSvgItemSimple( Object _value, HashMap<String, Object> _item )
	{
		if( "".equals( _value ) ) return null;
		
		if( "EXCEL".equals(isExportType) || "WORD".equals(isExportType) || "HWP".equals(isExportType)  || "PDF".equals(isExportType)  || "PRINT".equals(isExportType))
		{
			ArrayList<ArrayList<HashMap<Object, String>>> _rowList = (ArrayList<ArrayList<HashMap<Object, String>>>) convertSvgRichText(_value,_item);
			
			if(_rowList == null || _rowList.size() == 0 )  return null;
			
			//_rowList.get(0) ( g객체 {fontFamily:돋움,fontSize:11,lines:[[]]}  형태로 구성
			//_rowList.get(0).get("lines") // 라인별 객체를 담아둔 ArrayList 
			// lines = ArrayList<ArrayList<HashMap<Object, String>>> 형태로   [row[ column[] ] , row[ column[] ] ] 위와 같은 형태로 구성되어 있음.
			
			_item.put("children",  _rowList );		
		}
		else
		{
			String _svgTag = (String) convertSvgRichTextSimple(_value,_item);
			if( "".equals( _svgTag ) ) return null;
			
			boolean _preserveAspectRatio = (_item.containsKey("preserveAspectRatio") )?_item.get("preserveAspectRatio").toString().equals("true"):false;
			boolean _fixedToSize = (_item.containsKey("fixedToSize") )?_item.get("fixedToSize").toString().equals("true"):false;
			
			
			//변환된 svg태그를 인코딩하기 위하여 처리
			_svgTag = StringUtil.replaceSVGTag(_svgTag.toString(), _preserveAspectRatio, _fixedToSize, _item);	
			_svgTag = _svgTag.replace(" ", "%20");
			
			try {
				_item.put("data",  URLEncoder.encode(_svgTag, "UTF-8") );
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return _item;
	}

	// Html to SVG Convert
	private String convertHtmlToSvgText( String _data, HashMap<String, Object> _property )
	{
		String _retStr = "";
		int _width = 0;
        int _height = 0;
        String _fontFamily = "돋움";
        int _fontSizeUnit = 0;
        int _fontSize = 10;
        int _lineGap = 10;
        boolean _useWordWrap = true;
        
        _width = Float.valueOf( _property.get("width").toString() ).intValue();
        _height = Float.valueOf( _property.get("height").toString() ).intValue();
        
        if( _property.containsKey("currentAdjustableHeight") )
        {
        	if( Float.valueOf( _property.get("currentAdjustableHeight").toString() ).intValue() != -1 )
        	{
        		_height = Float.valueOf( _property.get("currentAdjustableHeight").toString() ).intValue();
        	}
        }
        
        if( _property.containsKey("lineGap")  )
        {
        	_lineGap = Float.valueOf( _property.get("lineGap").toString() ).intValue();
        }
        else
        {
        	_lineGap = 10;
        }
        
        if(Log.pageFontUnit.equals("pt"))
        {
        	_fontSizeUnit = 1;
        }
        if(_property.containsKey("fontSize"))
        {
        	_fontSize =  (int) Math.round(Double.parseDouble(_property.get("fontSize").toString()));
        }
        if(_property.containsKey("fontFamily"))
        {
        	_fontFamily =  _property.get("fontFamily").toString();
        }
		
        String _style = "font-size:" + _fontSize + "px; font-family:" + _fontFamily + ";";
        
        ImageRenderer.Type type = ImageRenderer.Type.SVG;
        String media = "screen";
        Dimension windowSize = new Dimension(_width, _height);
        boolean cropWindow = false;
        
        boolean isAutoSizeUpdate = false;
        if( _property.containsKey("fixedToSize")  )
        {
        	isAutoSizeUpdate = Boolean.valueOf( _property.get("fixedToSize").toString() ).booleanValue();
        }
        
        
        ImageRenderer r = new ImageRenderer();
        r.setMediaType(media);
        r.setWindowSize(windowSize, cropWindow);
        r.setAutoSizeUpdate(isAutoSizeUpdate);
        
        r.setTextSplitByWord(false);
        
        //r.renderURL(args[0], os, type);
        //String _srcXHtml = "<html><body>     <h2 style=\"font-style:italic\">테스트1</h2>     <h2 style=\"font-style:italic\">테스트2</h2>     </body></html>";
        //String _srcXHtml = "<html><body>     <p>-. 당사 목표금액 미달시 유찰되며 별도 가격결정 진행임.</p>     <p>  </p>     <h2 style=\"font-style:italic\">테스트</h2>     <h2 style=\"font-style:italic\">테스트</h2>     <p><span class=\"marker\">테스트</span></p>     <p><code>테스트</code></p>     <h1><span style=\"color:#FF8C00\"><span style=\"font-size:9px\"><span style=\"font-family:malgun gothic\"><big><span style=\"background-color:#FFD700\">테스트</span></big></span></span></span></h1>     <p><strong>테스트</strong></p>     <p><u>테스트</u></p>     <p><s>테스트</s></p>     <p><sub>테스트</sub></p>     <p><sup>테스트</sup></p>     <ol>               <li>테스트</li>  </ol>     <ul>               <li>테스트</li>  </ul>     <p style=\"margin-left:40px\">테스트</p>     <blockquote>  <p>테스트</p>  </blockquote>     <p><a href=\"#책갈피1\">테스트</a></p>     <p><a href=\"http://www.navver.com\">테스트</a></p>     <p><a name=\"책갈피1\">테스트</a></p>     <hr />  <p><img alt=\"smiley\" height=\"18\" src=\"http://webmail.ubstorm.co.kr/skin/main/basic/img/btn_logout.gif\" title=\"smiley\" width=\"75\" />  <img alt=\"crying\" height=\"23\" src=\"http://localhost:8080/js/everuxf/lib/ckeditor/plugins/smiley/images/cry_smile.png\" title=\"crying\" width=\"23\" />  <img alt=\"yes\" height=\"23\" src=\"http://localhost:8080/js/everuxf/lib/ckeditor/plugins/smiley/images/thumbs_up.png\" title=\"yes\" width=\"23\" /></p>     <p>  </p>     <div style=\"page-break-after: always\"><span style=\"display:none\">  </span></div>     <p>테스트</p>     <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" dir=\"ltr\" id=\"A000\" style=\"width:500px\" summary=\"Sapmle 표 요약\">               <caption>Sample 표</caption>               <thead>                             <tr>                                          <th scope=\"col\">구분</th>                                          <th scope=\"col\">내용</th>                             </tr>               </thead>               <tbody>                             <tr>                                          <td>1</td>                                          <td>AA</td>                             </tr>                             <tr>                                          <td>2</td>                                          <td>BB</td>                             </tr>               </tbody>  </table>     <hr />  <p>  </p>     <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:280px\">               <tbody>                             <tr>                                          <td colspan=\"2\" style=\"height:22px; text-align:center; width:93px\">10</td>                                          <td colspan=\"2\" style=\"text-align:center; width:93px\">11</td>                                          <td colspan=\"2\" style=\"text-align:center; width:93px\">12</td>                             </tr>                             <tr>                                          <td style=\"height:22px\">　</td>                                          <td colspan=\"4\">분석/설계(2)</td>                                          <td>　</td>                             </tr>               </tbody>  </table>     <p>  </p>     <table border=\"1\" bordercolor=\"#000000\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; width:100%\">               <tbody>                             <tr>                                          <th>NO</th>                                          <th>금형구분</th>                                          <th>차종</th>                                          <th>품번</th>                                          <th>품명</th>                                          <th>SET</th>                                          <th>CVT</th>                                          <th>제품<br />                                          수량</th>                                          <th>결정가</th>                                          <th>정/가단가</th>                                          <th>비고</th>                             </tr>                             <tr>                                          <td>                                          <p>1.</p>                                          </td>                                          <td colspan=\"10\">                                          <p>&quot;갑&quot;과 &quot;을&quot;은 쌍방이 체결한 부품공급 기본계약서 제 9조에 의거 &quot;갑&quot;과 &quot;을&quot;이 상호원만히 합의가 이루어져 상기와 같이 가격 결정에 합의함.</p>                                          </td>                             </tr>                             <tr>                                          <td>                                          <p>2.</p>                                          </td>                                          <td colspan=\"10\">공급자 &quot;을&quot;은 &quot;갑&quot; 소유의 금형을 사용목적에 따라 &#39;신의성실&#39;의 원칙에 의거하여 사용자의 의무를 다하여야한다.</td>                             </tr>                             <tr>                                          <td>                                          <p>3.</p>                                          </td>                                          <td colspan=\"10\">상기 합의서는 동일 적용함.</td>                             </tr>                             <tr>                                          <td>                                          <p>4.</p>                                          </td>                                          <td colspan=\"10\">가격결정 합의서 작성일 :  </td>                             </tr>                             <tr>                                          <td colspan=\"5\">                                          <p>  </p>                                             <p>  공급받는자 :  </p>                                             <p>  대 표 이 사 :  </p>                                          </td>                                          <td colspan=\"6\">                                          <p>  </p>                                             <p>공     급     자 :  </p>                                             <p>대 표 이 사 :  </p>                                          </td>                             </tr>               </tbody>  </table>  </body></html>";
        //String _srcXHtml = "<!DOCTYPE html><html><head>    	<meta charset=\"utf-8\"/>    	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/>	<title>UBIForm TEST</title>    </head><body style=\"font-family: 맑은고딕;\"> <div style=\"text-align: center;\">TEST 제목</div><div style=\"text-align: center;\"><br/></div><div style=\"text-align: justify;\"><table width=\"690\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"border: 1px solid #e4881f;  border-right-style: none; font-family: verdana; font-size: 12px;\"><tbody><tr><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 84px;\"><p style=\"text-align: center;\"><font color=\"#400080\"> 번호</font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 136px;\"><p style=\"text-align: center;\"><font color=\"#400080\">금형구분 </font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 187px;\"><p style=\"text-align: center;\"><font color=\"#400080\">차종 </font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 98px;\"><p style=\"text-align: center;\"><font color=\"#400080\">품번 </font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 181px;\"><p style=\"text-align: center;\"><font color=\"#400080\">비고 </font></p></td></tr><tr><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 84px;\"><p> 1</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 136px;\"><p> 금형12</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 187px;\"><p> 가격결정 합의서 작성일 :</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 98px;\"><p> 2323232</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 181px;\"><p> 상기 합의서는 동일 적용함.</p></td></tr><tr><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 84px;\"><p> 2</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 136px;\"><p> 금형100</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); width: 465px;\" colspan=\"3\" rowspan=\"1\"><p style=\"\"><font color=\"#46586e\"> </font><span style=\"text-align: justify;\"><font color=\"#46586e\">\"갑\"과 \"을\"은 쌍방이 체결한 부품공급 기본계약서 제 9조에 의거 \"갑\"과 \"을\"이 상호원만히 합의가 이루</font></span><span style=\"color: rgb(70, 88, 110); text-align: justify;\">어져 상기와 같이 가격 결정에 합의함.</span></p></td></tr></tbody></table><p> </p><br/></div></body></html>";
        //String _srcXHtml = "<!DOCTYPE html> <html> <head>     	<meta charset=\"utf-8\"/>     	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/> 	<title>UBIForm TEST</title>      </head>  <body style=\"font-family: 맑은고딕;\">       <p>테스트</p>         <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" dir=\"ltr\" id=\"A000\" style=\"width:500px\" summary=\"Sapmle 표 요약\">                       <caption>Sample 표</caption>                       <thead>                                     <tr>                                                  <th scope=\"col\">구분</th>                                                  <th scope=\"col\">내용</th>                                     </tr>                       </thead>                       <tbody>                                     <tr>                                                  <td>1</td>                                                  <td>AA</td>                                     </tr>                                     <tr>                                                  <td>2</td>                                                  <td>BB</td>                                     </tr>                       </tbody>          </table>                     <hr />          <p> </p>                     <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:280px\">                       <tbody>                                     <tr>                                                  <td colspan=\"2\" style=\"height:22px; text-align:center; width:93px\">10</td>                                                  <td colspan=\"2\" style=\"text-align:center; width:93px\">11</td>                                                  <td colspan=\"2\" style=\"text-align:center; width:93px\">12</td>                                     </tr>                                     <tr>                                                  <td style=\"height:22px\">　</td>                                                  <td colspan=\"4\">분석/설계(2)</td>                                                  <td>　</td>                                     </tr>                       </tbody>          </table>                     <p> </p>                     <table border=\"1\" bordercolor=\"#000000\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; width:100%\">                       <tbody>                                     <tr>                                                  <th>NO</th>                                                  <th>금형구분</th>                                                  <th>차종</th>                                                  <th>품번</th>                                                  <th>품명</th>                                                  <th>SET</th>                                                  <th>CVT</th>                                                  <th>제품<br />                                                  수량</th>                                                  <th>결정가</th>                                                  <th>정/가단가</th>                                                  <th>비고</th>                                     </tr>                                     <tr>                                                  <td>                                                  <p>1.</p>                                                  </td>                                                  <td colspan=\"10\">                                                  <p>&quot;갑&quot;과 &quot;을&quot;은 쌍방이 체결한 부품공급 기본계약서 제 9조에 의거 &quot;갑&quot;과 &quot;을&quot;이 상호원만히 합의가 이루어져 상기와 같이 가격 결정에 합의함.</p>                                                  </td>                                     </tr>                                     <tr>                                                  <td>                                                  <p>2.</p>                                                  </td>                                                  <td colspan=\"10\">공급자 &quot;을&quot;은 &quot;갑&quot; 소유의 금형을 사용목적에 따라 &#39;신의성실&#39;의 원칙에 의거하여 사용자의 의무를 다하여야한다.</td>                                     </tr>                                     <tr>                                                  <td>                                                  <p>3.</p>                                                  </td>                                                  <td colspan=\"10\">상기 합의서는 동일 적용함.</td>                                     </tr>                                     <tr>                                                  <td>                                                  <p>4.</p>                                                  </td>                                                  <td colspan=\"10\">가격결정 합의서 작성일 : </td>                                     </tr>                                     <tr>                                                  <td colspan=\"5\">                                                  <p> </p>                                                             <p> 공급받는자 : </p>                                                             <p> 대 표 이 사 : </p>                                                  </td>                                                  <td colspan=\"6\">                                                  <p> </p>                                                             <p>공   급   자 : </p>                                                             <p>대 표 이 사 : </p>                                                  </td>                                     </tr>                       </tbody>          </table>   </body> </html>  ";
        //String _srcXHtml = "<!DOCTYPE html> <html> <head>     	<meta charset=\"utf-8\"/>     	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/> 	<title>UBIForm TEST</title>      </head>  <body style=\"font-family: 맑은고딕;\"> <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" dir=\"ltr\" id=\"A000\" style=\"width:500px\" summary=\"Sapmle 표 요약\"><caption>Sample 표</caption>                       <thead>                                     <tr>                                                  <th scope=\"col\">구분</th>                                                  <th scope=\"col\">내용</th>                                     </tr>                       </thead>                       <tbody>                                     <tr>                                                  <td>1</td>                                                  <td>AA</td>                                     </tr>                                     <tr>                                                  <td>2</td>                                                  <td>BB</td>                                     </tr>                       </tbody>          </table>  </body> </html>  ";
        //r.renderXHTML(_srcXHtml, os, type);
        try {
//        	r.loadFontList(Log.ufilePath);
        	 
        	String _srcXHtml = _data.toString();
        	log.debug(getClass().getName() + "::" + "_srcXHtml=[" + _srcXHtml + "]");
     		_retStr = r.renderXHTML(_srcXHtml, type);
			
			//System.err.println("Done=[" + _retStr + "]");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        log.debug(getClass().getName() + "::" + "~~~~~~ convertHtmlToSvgText!~~~~~~~~~");
        log.debug(getClass().getName() + "::" + "_retStr=[" + _retStr + "]");
		return _retStr;
	}
	
	//TEST UBSVGRichText
	private Object convertSvgRichText( Object _data, HashMap<String, Object> _property )
	{
		String _retStr = "";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SvgRichTextObjectHandler svgObjhandler;
        
        int _width = 0;
        int _height = 0;
        String _fontFamily = "돋움";
        int _fontSizeUnit = 0;
        int _fontSize = 10;
        int _lineGap = 10;
        boolean _useWordWrap = true;
        
        _width = Float.valueOf( _property.get("width").toString() ).intValue();
        _height = Float.valueOf( _property.get("height").toString() ).intValue();
        if( _property.containsKey("lineGap")  )
        {
        	_lineGap = Float.valueOf( _property.get("lineGap").toString() ).intValue();
        }
        else
        {
        	_lineGap = 10;
        }
        _useWordWrap = Boolean.valueOf( _property.get("wordWrap").toString() ).booleanValue();
 
        if(Log.pageFontUnit.equals("pt"))
        {
        	_fontSizeUnit = 1;
        }
        if(_property.containsKey("fontSize"))
        {
        	_fontSize =  (int) Math.round(Double.parseDouble(_property.get("fontSize").toString()));
        }
        if(_property.containsKey("fontFamily"))
        {
        	_fontFamily =  _property.get("fontFamily").toString();
        }
        
        svgObjhandler = new SvgRichTextObjectHandler();
        
		try {
			SAXParser saxParser = factory.newSAXParser();
			
			if( _data instanceof svgTable  )
			{
				 _height = ((svgTable) _data).getHeight(); 
				svgObjhandler.init(_width, _height, _fontFamily, _fontSize, _lineGap, true, _useWordWrap, _fontSizeUnit);
				
				svgObjhandler.setSvgTableObject( (svgTable) _data);
				if( "EXCEL".equals(isExportType) || "WORD".equals(isExportType) || "HWP".equals(isExportType) || "PDF".equals(isExportType) )
				{
					return  svgObjhandler.getSvgRowItems();
				}
				else
				{
					_retStr = svgObjhandler.getSvg(); 
				}
			}
			else
			{
				try {
					svgObjhandler.init(_width, _height, _fontFamily, _fontSize, _lineGap, true, _useWordWrap, _fontSizeUnit);
					
//					String _dataTxt = _data.toString().replaceAll("&", "&amp;");
//					InputSource inputSource = new InputSource(new StringReader(_dataTxt));
					
					InputSource inputSource = new InputSource(new StringReader(_data.toString()));
					saxParser.parse(  inputSource , svgObjhandler);
					svgTable svgTbl = svgObjhandler.getSvgTableObject();
					
					if( "EXCEL".equals(isExportType) || "WORD".equals(isExportType) || "HWP".equals(isExportType) || "PDF".equals(isExportType)  || "PRINT".equals(isExportType) )
					{
						return  svgObjhandler.getSvgRowItems();
					}
					else
					{
						_retStr = svgObjhandler.getSvg(); 
					}
					
				} catch (Exception e) {
					// TODO: handle exception
					 e.printStackTrace();
				}
				
			}
			
	        
		 } catch (SAXException e) {
	    	  // TODO Auto-generated catch block
	    	  if(e.getMessage().equals("HeightOverflowException"))
	    	  {
	    		  if(svgObjhandler != null)
	    		  {
	    			  if("EXCEL".equals(isExportType) || "WORD".equals(isExportType) || "HWP".equals(isExportType) || "PDF".equals(isExportType)  || "PRINT".equals(isExportType) )
	    		      {
	    				  return svgObjhandler.getSvgRowItems();
	    		      }
	    		      else
	    		      {
	    		    	  _retStr = svgObjhandler.getSvg();
	    		      }
	    			  _retStr = svgObjhandler.getSvg(); 
	    		  }
	    	  }
	    	  else
	    	  {
	    		  e.printStackTrace();
	    	  }
	      } catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	      }
		
		log.debug(getClass().getName() + "::" + "SVG=" + _retStr + "]");
		
		return _retStr;
	}

	// Html to SVG Convert
	private String convertHtmlToSvgTextSimple( String _data, HashMap<String, Object> _property )
	{
		String _retStr = "";
		int _width = 0;
		int _height = 0;
		String _fontFamily = "돋움";
		int _fontSizeUnit = 0;
		int _fontSize = 10;
		int _lineGap = 10;
		boolean _useWordWrap = true;
		String _className = _property.get("className").toString();
		boolean isAutoSizeUpdate = false;
		
		_width = Float.valueOf( UBComponent.getProperties(_property, _className, "width").toString() ).intValue();
		_height = Float.valueOf( UBComponent.getProperties(_property, _className, "height").toString() ).intValue();
		int currentAdjustableHeight = Float.valueOf( UBComponent.getProperties(_property, _className, "currentAdjustableHeight","-1").toString() ).intValue();
		_lineGap = Float.valueOf( UBComponent.getProperties(_property, _className, "lineGap",10).toString() ).intValue();
		
		_fontSize =  (int) Math.round(Double.parseDouble( UBComponent.getProperties(_property, _className, "fontSize", 10).toString() ));
		_fontFamily = UBComponent.getProperties(_property, _className, "fontFamily", "돋움").toString();
		isAutoSizeUpdate = UBComponent.getProperties(_property, _className, "fixedToSize", "").toString().equals("true");
		
		if( currentAdjustableHeight != -1 )
		{
			_height = currentAdjustableHeight;
		}
		
		if(Log.pageFontUnit.equals("pt"))
		{
			_fontSizeUnit = 1;
		}
		
		String _style = "font-size:" + _fontSize + "px; font-family:" + _fontFamily + ";";
		
		ImageRenderer.Type type = ImageRenderer.Type.SVG;
		String media = "screen";
		Dimension windowSize = new Dimension(_width, _height);
		boolean cropWindow = false;
		
		ImageRenderer r = new ImageRenderer();
		r.setMediaType(media);
		r.setWindowSize(windowSize, cropWindow);
		r.setAutoSizeUpdate(isAutoSizeUpdate);
		
		r.setTextSplitByWord(false);
		
		//r.renderURL(args[0], os, type);
		//String _srcXHtml = "<html><body>     <h2 style=\"font-style:italic\">테스트1</h2>     <h2 style=\"font-style:italic\">테스트2</h2>     </body></html>";
		//String _srcXHtml = "<html><body>     <p>-. 당사 목표금액 미달시 유찰되며 별도 가격결정 진행임.</p>     <p>  </p>     <h2 style=\"font-style:italic\">테스트</h2>     <h2 style=\"font-style:italic\">테스트</h2>     <p><span class=\"marker\">테스트</span></p>     <p><code>테스트</code></p>     <h1><span style=\"color:#FF8C00\"><span style=\"font-size:9px\"><span style=\"font-family:malgun gothic\"><big><span style=\"background-color:#FFD700\">테스트</span></big></span></span></span></h1>     <p><strong>테스트</strong></p>     <p><u>테스트</u></p>     <p><s>테스트</s></p>     <p><sub>테스트</sub></p>     <p><sup>테스트</sup></p>     <ol>               <li>테스트</li>  </ol>     <ul>               <li>테스트</li>  </ul>     <p style=\"margin-left:40px\">테스트</p>     <blockquote>  <p>테스트</p>  </blockquote>     <p><a href=\"#책갈피1\">테스트</a></p>     <p><a href=\"http://www.navver.com\">테스트</a></p>     <p><a name=\"책갈피1\">테스트</a></p>     <hr />  <p><img alt=\"smiley\" height=\"18\" src=\"http://webmail.ubstorm.co.kr/skin/main/basic/img/btn_logout.gif\" title=\"smiley\" width=\"75\" />  <img alt=\"crying\" height=\"23\" src=\"http://localhost:8080/js/everuxf/lib/ckeditor/plugins/smiley/images/cry_smile.png\" title=\"crying\" width=\"23\" />  <img alt=\"yes\" height=\"23\" src=\"http://localhost:8080/js/everuxf/lib/ckeditor/plugins/smiley/images/thumbs_up.png\" title=\"yes\" width=\"23\" /></p>     <p>  </p>     <div style=\"page-break-after: always\"><span style=\"display:none\">  </span></div>     <p>테스트</p>     <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" dir=\"ltr\" id=\"A000\" style=\"width:500px\" summary=\"Sapmle 표 요약\">               <caption>Sample 표</caption>               <thead>                             <tr>                                          <th scope=\"col\">구분</th>                                          <th scope=\"col\">내용</th>                             </tr>               </thead>               <tbody>                             <tr>                                          <td>1</td>                                          <td>AA</td>                             </tr>                             <tr>                                          <td>2</td>                                          <td>BB</td>                             </tr>               </tbody>  </table>     <hr />  <p>  </p>     <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:280px\">               <tbody>                             <tr>                                          <td colspan=\"2\" style=\"height:22px; text-align:center; width:93px\">10</td>                                          <td colspan=\"2\" style=\"text-align:center; width:93px\">11</td>                                          <td colspan=\"2\" style=\"text-align:center; width:93px\">12</td>                             </tr>                             <tr>                                          <td style=\"height:22px\">　</td>                                          <td colspan=\"4\">분석/설계(2)</td>                                          <td>　</td>                             </tr>               </tbody>  </table>     <p>  </p>     <table border=\"1\" bordercolor=\"#000000\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; width:100%\">               <tbody>                             <tr>                                          <th>NO</th>                                          <th>금형구분</th>                                          <th>차종</th>                                          <th>품번</th>                                          <th>품명</th>                                          <th>SET</th>                                          <th>CVT</th>                                          <th>제품<br />                                          수량</th>                                          <th>결정가</th>                                          <th>정/가단가</th>                                          <th>비고</th>                             </tr>                             <tr>                                          <td>                                          <p>1.</p>                                          </td>                                          <td colspan=\"10\">                                          <p>&quot;갑&quot;과 &quot;을&quot;은 쌍방이 체결한 부품공급 기본계약서 제 9조에 의거 &quot;갑&quot;과 &quot;을&quot;이 상호원만히 합의가 이루어져 상기와 같이 가격 결정에 합의함.</p>                                          </td>                             </tr>                             <tr>                                          <td>                                          <p>2.</p>                                          </td>                                          <td colspan=\"10\">공급자 &quot;을&quot;은 &quot;갑&quot; 소유의 금형을 사용목적에 따라 &#39;신의성실&#39;의 원칙에 의거하여 사용자의 의무를 다하여야한다.</td>                             </tr>                             <tr>                                          <td>                                          <p>3.</p>                                          </td>                                          <td colspan=\"10\">상기 합의서는 동일 적용함.</td>                             </tr>                             <tr>                                          <td>                                          <p>4.</p>                                          </td>                                          <td colspan=\"10\">가격결정 합의서 작성일 :  </td>                             </tr>                             <tr>                                          <td colspan=\"5\">                                          <p>  </p>                                             <p>  공급받는자 :  </p>                                             <p>  대 표 이 사 :  </p>                                          </td>                                          <td colspan=\"6\">                                          <p>  </p>                                             <p>공     급     자 :  </p>                                             <p>대 표 이 사 :  </p>                                          </td>                             </tr>               </tbody>  </table>  </body></html>";
		//String _srcXHtml = "<!DOCTYPE html><html><head>    	<meta charset=\"utf-8\"/>    	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/>	<title>UBIForm TEST</title>    </head><body style=\"font-family: 맑은고딕;\"> <div style=\"text-align: center;\">TEST 제목</div><div style=\"text-align: center;\"><br/></div><div style=\"text-align: justify;\"><table width=\"690\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"border: 1px solid #e4881f;  border-right-style: none; font-family: verdana; font-size: 12px;\"><tbody><tr><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 84px;\"><p style=\"text-align: center;\"><font color=\"#400080\"> 번호</font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 136px;\"><p style=\"text-align: center;\"><font color=\"#400080\">금형구분 </font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 187px;\"><p style=\"text-align: center;\"><font color=\"#400080\">차종 </font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 98px;\"><p style=\"text-align: center;\"><font color=\"#400080\">품번 </font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 181px;\"><p style=\"text-align: center;\"><font color=\"#400080\">비고 </font></p></td></tr><tr><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 84px;\"><p> 1</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 136px;\"><p> 금형12</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 187px;\"><p> 가격결정 합의서 작성일 :</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 98px;\"><p> 2323232</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 181px;\"><p> 상기 합의서는 동일 적용함.</p></td></tr><tr><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 84px;\"><p> 2</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 136px;\"><p> 금형100</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); width: 465px;\" colspan=\"3\" rowspan=\"1\"><p style=\"\"><font color=\"#46586e\"> </font><span style=\"text-align: justify;\"><font color=\"#46586e\">\"갑\"과 \"을\"은 쌍방이 체결한 부품공급 기본계약서 제 9조에 의거 \"갑\"과 \"을\"이 상호원만히 합의가 이루</font></span><span style=\"color: rgb(70, 88, 110); text-align: justify;\">어져 상기와 같이 가격 결정에 합의함.</span></p></td></tr></tbody></table><p> </p><br/></div></body></html>";
		//String _srcXHtml = "<!DOCTYPE html> <html> <head>     	<meta charset=\"utf-8\"/>     	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/> 	<title>UBIForm TEST</title>      </head>  <body style=\"font-family: 맑은고딕;\">       <p>테스트</p>         <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" dir=\"ltr\" id=\"A000\" style=\"width:500px\" summary=\"Sapmle 표 요약\">                       <caption>Sample 표</caption>                       <thead>                                     <tr>                                                  <th scope=\"col\">구분</th>                                                  <th scope=\"col\">내용</th>                                     </tr>                       </thead>                       <tbody>                                     <tr>                                                  <td>1</td>                                                  <td>AA</td>                                     </tr>                                     <tr>                                                  <td>2</td>                                                  <td>BB</td>                                     </tr>                       </tbody>          </table>                     <hr />          <p> </p>                     <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:280px\">                       <tbody>                                     <tr>                                                  <td colspan=\"2\" style=\"height:22px; text-align:center; width:93px\">10</td>                                                  <td colspan=\"2\" style=\"text-align:center; width:93px\">11</td>                                                  <td colspan=\"2\" style=\"text-align:center; width:93px\">12</td>                                     </tr>                                     <tr>                                                  <td style=\"height:22px\">　</td>                                                  <td colspan=\"4\">분석/설계(2)</td>                                                  <td>　</td>                                     </tr>                       </tbody>          </table>                     <p> </p>                     <table border=\"1\" bordercolor=\"#000000\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; width:100%\">                       <tbody>                                     <tr>                                                  <th>NO</th>                                                  <th>금형구분</th>                                                  <th>차종</th>                                                  <th>품번</th>                                                  <th>품명</th>                                                  <th>SET</th>                                                  <th>CVT</th>                                                  <th>제품<br />                                                  수량</th>                                                  <th>결정가</th>                                                  <th>정/가단가</th>                                                  <th>비고</th>                                     </tr>                                     <tr>                                                  <td>                                                  <p>1.</p>                                                  </td>                                                  <td colspan=\"10\">                                                  <p>&quot;갑&quot;과 &quot;을&quot;은 쌍방이 체결한 부품공급 기본계약서 제 9조에 의거 &quot;갑&quot;과 &quot;을&quot;이 상호원만히 합의가 이루어져 상기와 같이 가격 결정에 합의함.</p>                                                  </td>                                     </tr>                                     <tr>                                                  <td>                                                  <p>2.</p>                                                  </td>                                                  <td colspan=\"10\">공급자 &quot;을&quot;은 &quot;갑&quot; 소유의 금형을 사용목적에 따라 &#39;신의성실&#39;의 원칙에 의거하여 사용자의 의무를 다하여야한다.</td>                                     </tr>                                     <tr>                                                  <td>                                                  <p>3.</p>                                                  </td>                                                  <td colspan=\"10\">상기 합의서는 동일 적용함.</td>                                     </tr>                                     <tr>                                                  <td>                                                  <p>4.</p>                                                  </td>                                                  <td colspan=\"10\">가격결정 합의서 작성일 : </td>                                     </tr>                                     <tr>                                                  <td colspan=\"5\">                                                  <p> </p>                                                             <p> 공급받는자 : </p>                                                             <p> 대 표 이 사 : </p>                                                  </td>                                                  <td colspan=\"6\">                                                  <p> </p>                                                             <p>공   급   자 : </p>                                                             <p>대 표 이 사 : </p>                                                  </td>                                     </tr>                       </tbody>          </table>   </body> </html>  ";
		//String _srcXHtml = "<!DOCTYPE html> <html> <head>     	<meta charset=\"utf-8\"/>     	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/> 	<title>UBIForm TEST</title>      </head>  <body style=\"font-family: 맑은고딕;\"> <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" dir=\"ltr\" id=\"A000\" style=\"width:500px\" summary=\"Sapmle 표 요약\"><caption>Sample 표</caption>                       <thead>                                     <tr>                                                  <th scope=\"col\">구분</th>                                                  <th scope=\"col\">내용</th>                                     </tr>                       </thead>                       <tbody>                                     <tr>                                                  <td>1</td>                                                  <td>AA</td>                                     </tr>                                     <tr>                                                  <td>2</td>                                                  <td>BB</td>                                     </tr>                       </tbody>          </table>  </body> </html>  ";
		//r.renderXHTML(_srcXHtml, os, type);
		try {
//        	r.loadFontList(Log.ufilePath);
			
			String _srcXHtml = _data.toString();
			log.debug(getClass().getName() + "::" + "_srcXHtml=[" + _srcXHtml + "]");
			_retStr = r.renderXHTML(_srcXHtml, type);
			
			//System.err.println("Done=[" + _retStr + "]");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.debug(getClass().getName() + "::" + "~~~~~~ convertHtmlToSvgText!~~~~~~~~~");
		log.debug(getClass().getName() + "::" + "_retStr=[" + _retStr + "]");
		return _retStr;
	}
	
	//TEST UBSVGRichText
	private Object convertSvgRichTextSimple( Object _data, HashMap<String, Object> _property )
	{
		String _retStr = "";
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SvgRichTextObjectHandler svgObjhandler;
		
		int _width = 0;
		int _height = 0;
		String _fontFamily = "돋움";
		int _fontSizeUnit = 0;
		int _fontSize = 10;
		int _lineGap = 10;
		boolean _useWordWrap = true;
		String _className = _property.get("className").toString();
		
		_width = Float.valueOf( UBComponent.getProperties(_property, _className, "width", 0).toString() ).intValue();
		_height = Float.valueOf( UBComponent.getProperties(_property, _className, "height", 0).toString() ).intValue();
		_lineGap = Float.valueOf(  UBComponent.getProperties(_property, _className, "lineGap", 10).toString() ).intValue();
		_useWordWrap = UBComponent.getProperties(_property, _className, "wordWrap").toString().equals("true");
		
		_fontSize =  (int) Math.round(Double.parseDouble( UBComponent.getProperties(_property, _className, "fontSize", 10).toString() ));
		_fontFamily = UBComponent.getProperties(_property, _className, "fontFamily", "돋움").toString();
		
		if(Log.pageFontUnit.equals("pt"))
		{
			_fontSizeUnit = 1;
		}
		
		svgObjhandler = new SvgRichTextObjectHandler();
		
		try {
			SAXParser saxParser = factory.newSAXParser();
			
			if( _data instanceof svgTable  )
			{
				_height = ((svgTable) _data).getHeight(); 
				svgObjhandler.init(_width, _height, _fontFamily, _fontSize, _lineGap, true, _useWordWrap, _fontSizeUnit);
				
				svgObjhandler.setSvgTableObject( (svgTable) _data);
				if( "EXCEL".equals(isExportType) || "WORD".equals(isExportType) || "HWP".equals(isExportType) || "PDF".equals(isExportType) )
				{
					return  svgObjhandler.getSvgRowItems();
				}
				else
				{
					_retStr = svgObjhandler.getSvg(); 
				}
			}
			else
			{
				try {
					svgObjhandler.init(_width, _height, _fontFamily, _fontSize, _lineGap, true, _useWordWrap, _fontSizeUnit);
					
//					String _dataTxt = _data.toString().replaceAll("&", "&amp;");
//					InputSource inputSource = new InputSource(new StringReader(_dataTxt));
					
					InputSource inputSource = new InputSource(new StringReader(_data.toString()));
					saxParser.parse(  inputSource , svgObjhandler);
					svgTable svgTbl = svgObjhandler.getSvgTableObject();
					
					if( "EXCEL".equals(isExportType) || "WORD".equals(isExportType) || "HWP".equals(isExportType) || "PDF".equals(isExportType)  || "PRINT".equals(isExportType) )
					{
						return  svgObjhandler.getSvgRowItems();
					}
					else
					{
						_retStr = svgObjhandler.getSvg(); 
					}
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
			}
			
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			if(e.getMessage().equals("HeightOverflowException"))
			{
				if(svgObjhandler != null)
				{
					if("EXCEL".equals(isExportType) || "WORD".equals(isExportType) || "HWP".equals(isExportType) || "PDF".equals(isExportType)  || "PRINT".equals(isExportType) )
					{
						return svgObjhandler.getSvgRowItems();
					}
					else
					{
						_retStr = svgObjhandler.getSvg();
					}
					_retStr = svgObjhandler.getSvg(); 
				}
			}
			else
			{
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.debug(getClass().getName() + "::" + "SVG=" + _retStr + "]");
		
		return _retStr;
	}
	
	public Boolean radiobuttonHandler( HashMap<String, Object> _propList )
	{
		Boolean _isSelected = false;
		
		//String _id = (String) _propList.get("id"); 

		//if( _id.equals("UBRadioBorder") ){
			
			String _groupName = (String) _propList.get("groupName");
			String _value = (String) _propList.get("value");
			
			if( !mRadioGroupList.containsKey(_groupName) )
			{
				if( _propList.containsKey("selected") && _propList.get("selected") != null ) _isSelected = _propList.get("selected").toString().equals("true");
			}
			else
			{
				for( int i=0; i<mRadioGroupList.size(); i++ ){
					
					String _getGroupText = mRadioGroupList.get(_groupName);
					
					if( _getGroupText != null && _getGroupText != "" ){
						//String _groupText = mRadioGroupList.get(_getGroupName);
						if( _value.equals(_getGroupText) ){
							_isSelected = true;
						}
					}
					
				}
			}
			
			
		//}
		return _isSelected;
	}
	public void radiobuttonGroupHandler( HashMap<String, Object> _propList )
	{
		
		String _id = (String) _propList.get("id"); 

		
		
		for( int i=0; i<mRadioGroupList.size(); i++ ){
		
			String _groupName = mRadioGroupList.get(_id);
			
			if( _id.equals(_groupName) ){
				mRadioGroupList = new HashMap<String , String>();	
			}
		}
		
		String _text = (_propList.get("text") != null) ? _propList.get("text").toString() : "";
		
		mRadioGroupList.put(_id, _text);
		
	}
	
	
	
	/**
	 * functionName :	getClonePosition</br>
	 * des			:	클론 페이지의 위치를 가져오기
	 * @param _rowIndex
	 * @param _cloneColCnt
	 * @param _cloneRowCnt
	 * @param _pageW
	 * @param _pageH
	 * @param _cloneType
	 * @param _cloneDirect
	 * @return
	 */
	public static ArrayList<Float> getClonePosition( int _rowIndex, int _cloneColCnt, int _cloneRowCnt, float _pageW, float _pageH, String _cloneType, String _cloneDirect )
	{
		
		ArrayList<Float> _positionList = new ArrayList<Float>();
		float _xPositoin = 0;
		float _yPositoin = 0;
		int _colCnt = 1;
		int _rowCnt = 1;
		
		if( _cloneType.equals(GlobalVariableData.CLONE_PAGE_CUSTOM) && _cloneDirect.equals(GlobalVariableData.CLONE_DIRECT_DOWN_CROSS ) )
		{
			_rowCnt = _rowIndex%_cloneRowCnt;
			_colCnt = (_rowIndex/_cloneRowCnt)%_cloneColCnt;
		}
		else
		{
			_colCnt = _rowIndex%_cloneColCnt;
			_rowCnt = (_rowIndex/_cloneColCnt)%_cloneRowCnt;

		}
		
		_xPositoin = (_pageW/_cloneColCnt)*_colCnt;
		_yPositoin = (_pageH/_cloneRowCnt)*_rowCnt;
		
		_positionList.add(_xPositoin);
		_positionList.add(_yPositoin);
		
		return _positionList;
	}
	
	
	
	
	
	
	//////////TEST///////////////////
	
	public HashMap<String, Object> convertElementToItem_Clone( HashMap<String, Object> _childData, int _dCnt,  HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param, float _cloneX, float _cloneY, float _updateY , int _totalPageNum , int _currentPageNum, boolean labelProjectFlag ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _itemId = "";
		if( _childData.containsKey("id") )
		{
			_itemId = _childData.get("id").toString();
		}
		
		String _className = _childData.get("className").toString();
		Element _childItem = (Element) _childData.get("ELEMENT_XML");
		
		NodeList _propertys = _childItem.getElementsByTagName("property");
		NodeList _ubfunction = _childItem.getElementsByTagName("ubfunction");
		HashMap<String, Object> _propList = (HashMap<String, Object>) _childData.clone();
		String _dataTypeStr = "";
		String _dataColumn = "";
		String _dataID = "";
		String _model_type = "";
		String _barcode_type = "";
		
		// formatter variables
		String _formatter="";
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";
		String _inputForamtString = "";
		String _outputFormatString = "";
		
		// edit formatter variables (e-form)
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		// image variable
		String _prefix="";
		String _suffix="";
		String[] _datasets={"",""};
					
		boolean _resizeFont = false;
		
		_propList = mItemPropVar.getItemName(_className);
		if( _propList == null) return null;
		
		// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
		if( _propList.containsKey("rowId") )
		{
			_propList.put("rowId", _dCnt);
		}
		
		// 포맷터 값이존재할경우각각의 데이터를 담는 처리
		NodeList formatterItem = _childItem.getElementsByTagName("formatter");
		Element _propItem;
		if( formatterItem != null && formatterItem.getLength() > 0 )
		{	
			NodeList formatterProperty = ((Element) formatterItem.item(0)).getElementsByTagName("property");
			int propertySize = formatterProperty.getLength();
			for (int i = 0; i < propertySize; i++) {
				
				_propItem = (Element) formatterProperty.item(i);

				String _name = _propItem.getAttribute("name");
				String _value = _propItem.getAttribute("value");
				
				if( _name.equals("nation") ){
					_nation = URLDecoder.decode(_value, "UTF-8");
				}
				
				else if( _name.equals("mask") ){
					_mask = URLDecoder.decode(_value, "UTF-8");
				}

				else if( _name.equals("decimalPointLength") ){
					
					_decimalPointLength =  common.ParseIntNullChk(_value, 0);
				}				
				
				else if( _name.equals("useThousandComma") ){
					_useThousandComma = Boolean.parseBoolean(_value);
				}		
				else if( _name.equals("isDecimal") ){
					_isDecimal = Boolean.parseBoolean(_value);
				}		
				else if( _name.equals("formatString") ){
					_formatString = _value;
				}
				else if(_name.equals("align"))
				{
					_align = _value;
				}
				else if( _name.equals("inputFormatString") )
				{
					_inputForamtString =  URLDecoder.decode(_value , "UTF-8");
				}
				else if( _name.equals("outputFormatString") )
				{
					_outputFormatString =  URLDecoder.decode(_value , "UTF-8");
				}
				
			}
		}
		// 에디트 포맷터 값이존재할경우각각의 데이터를 담는 처리
		NodeList editFormatterItem = _childItem.getElementsByTagName("editItemFormatter");
		if( editFormatterItem != null && editFormatterItem.getLength() > 0 )
		{	
			String _eformatDataset=null;
			String _eformatKeyField=null;
			String _eformatLabelField=null;
			
			NodeList formatterProperty = ((Element) editFormatterItem.item(0)).getElementsByTagName("property");
			int propertySize = formatterProperty.getLength();
			for (int i = 0; i < propertySize; i++) {
				
				_propItem = (Element) formatterProperty.item(i);
				
				String _name = _propItem.getAttribute("name");
				String _value = _propItem.getAttribute("value");
				
				if( _name.equals("nation") ){
					_nationE = URLDecoder.decode(_value, "UTF-8");
					_propList.put("eformnation", 	_nationE );
				}
				
				else if( _name.equals("mask") ){
					_maskE = URLDecoder.decode(_value, "UTF-8");
					_propList.put("eformmask", 	_maskE );
				}
				else if( _name.equals("decimalPointLength") ){
					
					_decimalPointLengthE =  common.ParseIntNullChk(_value, 0);
					
					_propList.put("eformdecimalPointLength", 	_decimalPointLengthE );
					
				}				
				else if( _name.equals("useThousandComma") ){
					_useThousandCommaE = Boolean.parseBoolean(_value);
					
					_propList.put("eformuseThousandComma", 	_useThousandCommaE );
				}		
				else if( _name.equals("isDecimal") ){
					_isDecimalE = Boolean.parseBoolean(_value);
					_propList.put("eformisDecimal", _isDecimalE	 );
				}		
				else if( _name.equals("formatString") ){
					_formatStringE = _value;
					_propList.put("eformformatString", _formatStringE	 );
				}else if( _name.equals("dataProvider") ){
					_value = URLDecoder.decode(_value, "UTF-8");
					_propList.put("eformDataProvider", _value	 );
				}else if( _name.equals("dataset") ){
					_eformatDataset = _value;
				}else if( _name.equals("keyField") ){
					_eformatKeyField = _value;
				}else if( _name.equals("valueField") ){
					_eformatLabelField = _value;
				}
			}
			
			if( _formatterE.equals("SelectMenu") ){
				
				// dataset으로 comboBox 표현.
				if( _eformatDataset != null &&  (!_eformatDataset.equals("null")) && _data.containsKey(_eformatDataset) ){
					
					String _efText = _propList.get("text").toString();
					Boolean _hasValueKey = false;
					
					ArrayList<HashMap<String, Object>> _list = _data.get(_eformatDataset);
					
					HashMap<String, Object> _dataHm;
					Object _keyData;
					Object _labelData;
					
					JSONArray ja = new JSONArray();
					String _jsonStr=null;
					JSONObject jo;
					String _keyStr=null;
					String _labelStr=null;
					
					for( int _eformatIdx=0; _eformatIdx<_list.size(); _eformatIdx++ ){
						_dataHm = _list.get(_eformatIdx);
						_keyData = _dataHm.get(_eformatKeyField);
						_labelData = _dataHm.get(_eformatLabelField);
						_keyStr=_keyData.toString();
						_labelStr = _labelData.toString();
						
						if( _efText.equals(_keyStr) && _hasValueKey == false ){
							_hasValueKey = true;
							_propList.put("text", _labelStr );
						}
						
						jo = new JSONObject();
						jo.put("label", _labelStr);
						jo.put("value",_keyStr );
						ja.add(jo);
					}
					
					_jsonStr = ja.toJSONString();
					
					_propList.put("eformDataProvider", _jsonStr	 );
				}else{
					
					if( _propList.containsKey("eformDataProvider") == false && _propList.get("eformDataProvider") == null )
					{
						_propList.put("eformDataProvider", "[]");
					}
					
					String _jsonStr = _propList.get("eformDataProvider").toString(); 
					JSONParser jsonParser = new JSONParser();
					try {
						
						JSONArray ja = (JSONArray) jsonParser.parse(_jsonStr);
						
						String _efText = _propList.get("text").toString();
						JSONObject oj;
						for( int jsonIdx=0; jsonIdx<ja.size(); jsonIdx++ ){
							oj=(JSONObject) ja.get(jsonIdx);
							if( oj.get("value").equals(_efText) ){
								_propList.put("text", oj.get("label") );
								break;
							}
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
		}
		
		//hyperLinkedParam처리
		if( _propList.containsKey("ubHyperLinkType") && "2".equals( _propList.get("ubHyperLinkType") )  )
		{
			NodeList _hyperLinkedParam = _childItem.getElementsByTagName("ubHyperLinkParm");
			if( _hyperLinkedParam != null && _hyperLinkedParam.getLength() > 0 )
			{
				Element _hyperLinkEl = (Element) _hyperLinkedParam.item(0);
				NodeList _hyperLinkedParams = _hyperLinkEl.getElementsByTagName("param");
				int _hyperLinkedParamSize = _hyperLinkedParams.getLength();
				
				HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
				
				for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
				{
					Element _hyperParam = (Element) _hyperLinkedParams.item(_hyperIdx);
					NodeList _hyperPropertys = _hyperParam.getElementsByTagName("property");
					int _hyperPropertysSize = _hyperPropertys.getLength();
					String _hyperParamKey = "";
					String _hyperParamValue = "";
					String _hyperParamType = "";
					
					for (int _hyperProIdx = 0; _hyperProIdx <  _hyperPropertysSize; _hyperProIdx++) 
					{
						Element _hyperProperty = (Element) _hyperPropertys.item(_hyperProIdx);
						if( "id".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamKey = _hyperProperty.getAttribute("value").toString();
						}
						else if( "value".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamValue = _hyperProperty.getAttribute("value").toString();
						}
						else if( "type".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamType = _hyperProperty.getAttribute("value").toString();
						}
					}
					
					if( "DataSet".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						
						_hyperParamValue = "";
						
						if(_data.containsKey(_hyperLinkedDataSetId))
						{
							ArrayList<HashMap<String, Object>> _list = _data.get( _hyperLinkedDataSetId );
							Object _dataValue = "";
							if( _list != null ){
								if( _dCnt < _list.size() )
								{
									HashMap<String, Object> _dataHm = _list.get(_dCnt);
									_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
								}
							}
						}
					}
					else if("Parameter".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						if( _param.containsKey(_hyperLinkedDataSetColumn))
						{
							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

							String _pValue = _pList.get("parameter");

							if( _pValue.equals("undefined"))
							{
								_hyperParamValue = "";
							}
							else
							{
								_hyperParamValue = _pValue;
							}
						}
						else
						{
							_hyperParamValue = "";
						}
					}
					
					_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
				}
				
				_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
			}
			
		}

		
		
		// 보더업데이트
		_propList = convertItemToBorder(_propList);
		
		//DataSet정보 처리
		if( _propList.containsKey("dataType") && (_propList.get("dataType").equals("1") || _propList.get("dataType").equals("3"))  )
		{
			_dataType=_propList.get("dataType").toString();
			_dataID = _propList.get("dataSet").toString();
			_dataColumn = _propList.get("column").toString();
			
			if( _dataType.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				//HashMap<String, Object> _dataHm = _list.get(_dCnt);
				
				if( _list == null || _dCnt >= _list.size() ) 
				{
					_propList.put("text", "");
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					
					if( _dataValue == null ){
						_dataValue = "null";
					}
					
					// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
					if(_className.equals("UBSVGArea") ){
						
						if(_dataValue==null) return null;
						
						String _tmpDataValue =  _dataValue.toString();
						if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
						{
							_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
						}
						
						boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
						boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
						boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
						
						if(_bSVG)
						{
							_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
						}
						else
						{
							boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
							if(!_bHasHtmlTag)
								_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
								
							_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
							_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
						}
												
						_dataValue = _dataValue.toString().replace(" ", "%20");
						
						if( !_dataValue.toString().equals("") )
						{
							_propList.put("data",  URLEncoder.encode(_dataValue.toString(), "UTF-8"));
						}
						else
						{
							return null;
						}
						
					}
					else if( _className.equals("UBSVGRichText") )
					{
						if( _dataValue != null && _dataValue.toString().equals("") == false )
						{
							_propList = convertUBSvgItem( _dataValue.toString(), _propList);
							
							if(_propList == null ) return null;
						}
					}
					
					if("UBImage".equals(_className) || _className.equals("UBSignature") || _className.equals("UBTextSignature") || _className.equals("UBPicture"))
					{
						_propList.put("src", _dataValue);
					}
					else if("UBCheckBox".equals(_className) || "UBRadioBorder".equals(_className))
					{
						_propList.put("selected", _dataValue);
					}					
					else
					{
						_propList.put("text", _dataValue);
					}
				}
				
			}
			else if( _dataType.equals("3"))
			{
				String _txt = _propList.get("text").toString();
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					
					if( _inOf != 0 ){
						
						// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
						if(_className.equals("UBSVGArea") ){
							
							String _tmpDataValue = String.valueOf(_txt);
							if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
							{
								_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
							}
							
							boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
							boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
							boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
							
							String _svgTag = null;
							if(_bSVG)
							{
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
							}
							else
							{
								boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
								if(!_bHasHtmlTag)
									_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
									
								_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							}
												
							log.debug("9674-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _svgTag + "]");
							
							_svgTag = _svgTag.replace(" ", "%20");
							
							if( !_svgTag.equals("") )
							{
								_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8"));
							}
							else
							{
								return null;
							}
							
							_txt = "";
						}
						else if( _className.equals("UBSVGRichText") )
						{
							if( _txt != null && _txt.equals("") == false )
							{
								_propList = convertUBSvgItem( _txt, _propList);
								
								if(_propList == null ) return null;
							}
						}
						
						
						String _fnValue;
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							//_fnValue = mFunction.testFN(_txt,_dCnt,_totalPageNum,_currentPageNum, -1 , -1 , "" );
							_fnValue = _txt;
						}else{
							_fnValue = mFunction.function(_txt,_dCnt,_totalPageNum,_currentPageNum, -1 , -1 );
						}
						
						
						
						
						if(_className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture")){
							_propList.put("src",  URLEncoder.encode(_fnValue, "UTF-8"));
						}else{
							_propList.put("text", _fnValue);
						}
						
					}else{
						
						int _paramFnBraketIndex =_txt.indexOf("}",_inOf); 
						if( _paramFnBraketIndex != -1 ){
							_pKey = _txt.substring(_inOf + 7 , _paramFnBraketIndex );
						}else{
							_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);	
						}
						
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_propList.put("text", "");
						}
						else
						{
							_propList.put("text", _pValue);
						}
						
					}

				}
				else
				{
					_propList.put("text", "");
				}
			}
		}
		else if( _propList.containsKey("dataSet") && _propList.get("dataSet") != null )
		{
			_dataID = _propList.get("dataSet").toString();
		}
		
		float _updateX = 0;
		if(  _updateY > -1 )
		{
			_propList.put("y", _updateY);
			_propList.put("top", _updateY);
		}
		else if( _cloneY != 0 )
		{
			_propList.put("y", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
			_propList.put("top", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
		}
		
		if( _cloneX != 0 )
		{
			_updateX = _cloneX + Float.valueOf( _propList.get("x").toString() );
			_propList.put("x", _updateX);
			_propList.put("left", _updateX);
		}
		
		//Image 
		if(_className.equals("UBImage")  || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture")){
			if( _dataType.equals("1") )
			{
				
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				Object _dataValue;
				if( _list == null || _dCnt >= _list.size()) 
				{
					_dataValue = "";
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					_dataValue = _dataHm.get(_dataColumn);
				}
				String _url="";
				String _txt = "";
				if( _dataValue != null ){
					_txt=_dataValue.toString();
				}
				
				_url= _prefix + _txt + _suffix;

				String	_servicesUrl = "";
				if(_prefix.equalsIgnoreCase("BLOB://") )
				{
					// BLOB 이미지의 Resize처리
					try {
						if(_txt.length() > ImageUtil.MAXINUM_LENGTH )
						{
							_txt = ImageUtil.resizeBLOBData(_txt, Float.valueOf(_propList.get("width").toString()).intValue(),  Float.valueOf(_propList.get("width").toString()).intValue(), true); 
						}
					} catch (IOException e) {
						
					}
					_servicesUrl = URLEncoder.encode(_txt, "UTF-8");
					
				}
				else
				{
					_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+ URLEncoder.encode(_url,"UTF-8");
				}
				
				_propList.put("src", _servicesUrl);
			}
		}
		else if( _className.equals("UBSVGArea") || _className.equals("UBSVGRichText") )
		{
			// UBSVGArea에 값 Add
			
			if( _dataType.equals("1") || _dataType.equals("3") )
			{
				_propList.put("src", _propList.get("text"));
				_propList.remove("text");
			}
		}
		
		
		for(int _ubfxIndex = 0; _ubfxIndex < _ubfunction.getLength(); _ubfxIndex++)
		{
			Element _ubfxItem = (Element) _ubfunction.item(_ubfxIndex);

			String _name = _ubfxItem.getAttribute("property");
			String _value = _ubfxItem.getAttribute("value");
			
			_value = URLDecoder.decode(_value, "UTF-8");
			mFunction.setDatasetList(_data);
			mFunction.setParam(_param);
			
			
			String _fnValue;
			
			if( mFunction.getFunctionVersion().equals("2.0") ){
				_fnValue = mFunction.testFN(_value,_dCnt, _totalPageNum , _currentPageNum,-1,-1, "" );
			}else{
				_fnValue = mFunction.function(_value,_dCnt, _totalPageNum , _currentPageNum,-1,-1);
			}
			
			
			_fnValue = _fnValue.trim();
			
			if(  _name.equals("text") || _fnValue.equals("") == false)
			{
//				if(_name.indexOf("Color") != -1 )
//				{
//					_propList.put((_name + "Int"), mPropertyFn.changeColorHexToInt(_fnValue) );
//				}
//				_propList.put(_name, _fnValue);
				_propList = convertUbfxStyle(_name, _fnValue, _propList );
				
			}
		}
		
		if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
			String _formatValue="";
			if( _dataType.equalsIgnoreCase("1") ){
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				if( _list == null || _dCnt >= _list.size()) 
				{
					_formatValue = "";
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					_formatValue = _dataValue != null ? _dataValue.toString() : "";
				}
			}else{
				_formatValue = _propList.get("text").toString();
			}
			try {
				
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
                else if( _formatter.equalsIgnoreCase("CustomDate") )
				{
					_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
				}
				
			} catch (ParseException e) {
//				e.printStackTrace();
			}
			_propList.put("text", _formatValue);
			
			
			// format이 label band에서 안들어간다.
			_propList.put("formatter", _formatter);
			_propList.put("mask", _mask);
			_propList.put("decimalPointLength", _decimalPointLength);
			_propList.put("useThousandComma", _useThousandComma);
			_propList.put("isDecimal", _isDecimal);
			_propList.put("formatString", _formatString);
			_propList.put("nation", _nation);
			_propList.put("inputFormatString", _inputForamtString);
			_propList.put("outputFormatString", _outputFormatString);
		}
		
		
		if( !_formatterE.equalsIgnoreCase("null") && !_formatterE.equalsIgnoreCase("") ){
			String _formatValue="";
			if( _dataType.equalsIgnoreCase("1") ){
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				if( _dCnt >= _list.size()) 
				{
					_formatValue = "";
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					_formatValue = _dataValue != null ? _dataValue.toString() : "";
				}
			}else{
				_formatValue = _propList.get("text").toString();
			}
			try {
				
				if( _formatterE.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nationE, _alignE, _formatValue);
				}else if( _formatterE.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatStringE, _formatValue);
				}else if( _formatterE.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberEditFormat(_mask, _decimalPointLengthE, _useThousandCommaE, _isDecimalE, _formatValue);
				}else if( _formatterE.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringEditFormat(_maskE, _formatValue,"*");
				}
			} catch (ParseException e) {
//				e.printStackTrace();
			}
			_propList.put("textE", _formatValue);
			
			_propList.put("editItemFormatter", _formatterE);
			_propList.put("eformmask", _maskE);
			_propList.put("eformdecimalPointLength", _decimalPointLengthE);
			_propList.put("eformuseThousandComma", _useThousandCommaE);
			_propList.put("eformisDecimal", _isDecimalE);
			_propList.put("eformformatString", _formatValue);
		}
		
		
		// resizeFont���� true�ϰ�� ó��
		if( _resizeFont && _propList.containsKey("text") && !"".equals(_propList.get("text")) )
		{
			float _fontSize 	= Float.valueOf( _propList.get("fontSize").toString() );
			String _fontFamily 	= _propList.get("fontFamily").toString();
			String _fontWeight 	= _propList.get("fontWeight").toString();
			
			float _padding = (_propList.containsKey("padding"))? Float.valueOf( _propList.get("padding").toString()):3;
			
			float _maxBorderSize = 0;
			if(_propList.containsKey("borderWidths"))
			{
				ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
				
				for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
					if(_maxBorderSize < _borderWidths.get(_bIndex))
					{
						_maxBorderSize = _borderWidths.get(_bIndex);
					}
				}
				_padding = _maxBorderSize + _padding;
			}
			
			float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
			
			_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
			_propList.put("fontSize",  _fontSize);

		}

		// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
		Boolean _hasTooltip=false;
		if( _propList.containsKey("tooltip") ){
			if( _propList.get("tooltip") != null && !(_propList.get("tooltip").equals("")) ){
				_hasTooltip = true;
			}
		}
		
		if( _hasTooltip == false ){
			if(_propList.containsKey("text"))
				_propList.put("tooltip", _propList.get("text").toString());
		}
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
			    		_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8")); 
				_propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 8 ){
					_barcodeSrc="";
				}
				/*
				else if( _barcode_type.equalsIgnoreCase("itf14") && _barcodeData.length() != 14 ){
					_barcodeSrc="";
				}
				*/
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				//_propList.put("src" , _barcodeSrc );
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") )
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			PARAM = getChartParamToElement(_childItem );			
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToElement2(_childItem );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				//String [] arrDataId = _dataID.split(":");
				String [] arrDataId =_datasets;
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = _data.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			{
				ArrayList<HashMap<String, Object>> _list = _data.get( _dataID );			
			
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();

			    	try {
			    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		
		if("UBGraphicsRectangle".equals(_className) || "UBGraphicsCircle".equals(_className) || "UBGraphicsGradiantRectangle".equals(_className))
		{
			_propList.put("angle" , _propList.get("rotation") );
			_propList.put("stroke" , _propList.get("borderColor").toString() );
			_propList.put("strokeWidth" , Integer.valueOf( _propList.get("borderThickness").toString() ) );
			_propList.put("scaleX" , 1);
			_propList.put("scaleY" , 1);
			
			_propList.put("width", Float.valueOf(_propList.get("width").toString()));
			_propList.put("height", Float.valueOf(_propList.get("height").toString()));
			
			if( "UBGraphicsCircle".equals(_className) )
			{
				_propList.put("radius", Float.valueOf( Float.valueOf(_propList.get("width").toString())/2 ));
				_propList.put("scaleY", Float.valueOf( Float.valueOf(_propList.get("height").toString())/Float.valueOf(_propList.get("width").toString()) ));
			}
			
		}
		if( labelProjectFlag == false && _propList.containsKey("visible") && _propList.get("visible").equals("false"))
		{
			return null;
		}
		
		_propList.put("className" , _className );
		_propList.put("id" , _itemId );
		
		// radioButtonGroup 
		if( _className.equals("UBRadioBorder") ){
			Boolean _isSelected = radiobuttonHandler(_propList);
			_propList.put("selected", _isSelected);
		}else if( _className.equals("UBRadioButtonGroup") ){
			radiobuttonGroupHandler(_propList);
		}
		
		return _propList;
	}
	

	public  ArrayList<HashMap<String, Object>> convertElementTableToItemClone(Element _childItem, int _dCnt,  HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param, float _cloneX, float _cloneY, float _updateY,  ArrayList<HashMap<String, Object>> _objects, int _totalPageNum , int _currentPageNum, boolean labelProjectFlag ) throws UnsupportedEncodingException, XPathExpressionException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _itemId = _childItem.getAttribute("id");
		String _className = _childItem.getAttribute("className");

		NodeList _propertys = _childItem.getElementsByTagName("property");
		NodeList _ubfunction = _childItem.getElementsByTagName("ubfunction");
		NodeList _tableUbfunction = null;
		
		HashMap<String, Object> _propList = new HashMap<String, Object>();

		String _dataTypeStr = "";
		String _dataColumn = "";
		String _dataID = "";
		String _model_type = "";
		String _barcode_type = "";
		
		// formatter variables
		String _formatter="";
		String _nation="";
		String _formatterAlign = "";
		String _align="";
		String _dataType="";
		String _mask="";
		String _inputForamtString = "";
		String _outputFormatString = "";
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		
		// edit format variables
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";
		
		
		_propList = mItemPropVar.getItemName("UBLabel");
		NodeList _tablePropertys;
		NodeList _tableMaps;
		NodeList _tableMapDatas;
		NodeList _cellPropertys;
		Element _ItemProperty;
		Element _tableMapItem;
		Element _cellItem;
		String _propertyName = "";
		String _propertyValue = "";
		String _propertyType = "";
		String _itemDataSetName = "";
		HashMap<String, Object> tableProperty = new HashMap<String, Object>();
		HashMap<String, Object> itemProperty = new HashMap<String, Object>();
		HashMap<String, Object> tableMapProperty = new HashMap<String, Object>();
		
		ArrayList<Object> borderAr = null;
		
		int colIndex = 0;
		int rowIndex = 0;
		
		int l = 0;
		int i = 0;
		int j = 0;
		int k = 0;
		float updateX = 0;
		float updateY = 0;
		
		mFunction.setDatasetList(_data); 
		mFunction.setParam(_param);
		
		boolean _newTalbeFlag = false;
		
		boolean _resizeFont = false;
		
		_tablePropertys = _childItem.getChildNodes();
		
		int _tablePropertysLength = _tablePropertys.getLength();
		
		
		int _ubfxCnt = _childItem.getChildNodes().getLength();
		for (int m = 0; m < _ubfxCnt; m++) {
			if( _childItem.getChildNodes().item(m).getNodeName().equals("ubfx") )
			{
				Element _e = (Element) _childItem.getChildNodes().item(m);
				_tableUbfunction = _e.getElementsByTagName("ubfunction");
			}
		}
		
		ArrayList<HashMap<String, Object>> _ubApprovalAr = null;
		
		for ( l = 0; l < _tablePropertysLength; l++) {
			if( _tablePropertys.item(l) instanceof Element )
			{
				_ItemProperty = (Element) _tablePropertys.item(l);
				if(_ItemProperty.getTagName().equals("property"))
				{
					_propertyName = _ItemProperty.getAttribute("name");
					_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
					_propertyType = _ItemProperty.getAttribute("type");
					tableProperty.put( _propertyName, _propertyValue );
				}
			}
		}
		
		// visible 값이 false일때 cell생성하지 않도록 수정
		if( tableProperty.containsKey("visible") && tableProperty.get("visible").toString().equals("false")) return _objects;

		// 신규 테이블 여부를 판단
		if( tableProperty.containsKey("version") &&  tableProperty.get("version").equals(ItemConvertParser.TABLE_VERSION_NEW))
		{
			_newTalbeFlag = true;
		}
		
		NodeList _tableML = _childItem.getElementsByTagName("table");
		Element _tableM = (Element) _tableML.item(0);
		
		if(_newTalbeFlag) _tableMaps = _tableM.getElementsByTagName("tableMap");
		else _tableMaps = _tableM.getElementsByTagName("row");
		
		int _tableMapsLength = _tableMaps.getLength();
		int _tableMapDatasLength = 0;
		
		ArrayList<ArrayList<HashMap<String, Object>>> _allTableMap = new ArrayList<ArrayList<HashMap<String,Object>>>();
		//TableMap을 이용하여 TableMapData로 모든 테이블의 맵을 담아두기
		// 각 맵별BorderString담아두기작업
		
		ArrayList<Float> _xAr = new ArrayList<Float>();	//각 row별 Height값 담아두기
		ArrayList<Float> _yAr = new ArrayList<Float>();	//각 row별 Height값 담아두기
		float _pos = 0;
		float _mapW = 0;	// 테이블맵의 X좌료
		float _mapH = 0;	// 테이블 맵의 Y좌표
		
		if( _newTalbeFlag )
		{
			for ( k = 0; k < _tableMapsLength; k++) {
				
				_tableMapItem = (Element) _tableMaps.item(k);
				_tableMapDatas = _tableMapItem.getElementsByTagName("tableMapData");
				_tableMapDatasLength = _tableMapDatas.getLength();
				
				ArrayList<HashMap<String, Object>> _tableMapRow = new ArrayList<HashMap<String,Object>>();
				
				for ( l = 0; l < _tableMapDatasLength; l++) {
					
					itemProperty = new HashMap<String, Object>();
					
					_itemDataSetName = "";
					_cellPropertys = ((Element) _tableMapDatas.item(l)).getElementsByTagName("property");
					// 테이블맵의 Property담기
					int _cellPropertysLength = _cellPropertys.getLength();
					for ( j = 0; j < _cellPropertysLength; j++) {
						_ItemProperty = (Element) _cellPropertys.item(j);
						if(_ItemProperty.getParentNode().getNodeName().equals("tableMapData"))
						{
							_propertyName = _ItemProperty.getAttribute("name");
							_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
							_propertyType = _ItemProperty.getAttribute("type");
							
							itemProperty.put(  _propertyName, _propertyValue );
						}
					}
					_tableMapRow.add(itemProperty);
					
					if( k == 0 )
					{
						// X좌표 담기 (소숫점 1자리까지만 )
						_pos = (float) Math.round( Float.valueOf( itemProperty.get("columnWidth").toString() ) );
						_xAr.add( _mapW  );
						_mapW = _mapW + _pos;
					}
					
				}
				
				if( k == 0 )
				{
					_xAr.add( _mapW  );		// 마지막 Width 값 담기
				}
				
				_pos = (float) Math.round( Float.valueOf( itemProperty.get("rowHeight").toString() ) );
				_yAr.add( _mapH  );
				_mapH = _mapH + _pos;
				
				_allTableMap.add(_tableMapRow);
			}
			
			_yAr.add( _mapH  );		// 마지막 Height 값 담기
		}
		else
		{
			float _argoHeight = 0;
			int _rowPropertyLength = 0;
			for ( k = 0; k < _tableMapsLength; k++) {
				
				_tableMapItem = (Element) _tableMaps.item(k);
				
				_tableMapDatas = _tableMapItem.getElementsByTagName("cell");
				_tableMapDatasLength = _tableMapDatas.getLength();
				
				ArrayList<HashMap<String, Object>> _tableMapRow = new ArrayList<HashMap<String,Object>>();
				
				for ( l = 0; l < _tableMapDatasLength; l++) {
					
					itemProperty = new HashMap<String, Object>();
					_itemDataSetName = "";
					_cellPropertys = ((Element) _tableMapDatas.item(l)).getElementsByTagName("property");
					// 테이블맵의 Property담기
					int _cellPropertysLength = _cellPropertys.getLength();
					for ( j = 0; j < _cellPropertysLength; j++) {
						_ItemProperty = (Element) _cellPropertys.item(j);
						if(_ItemProperty.getParentNode().getNodeName().equals("cell"))
						{
							_propertyName = _ItemProperty.getAttribute("name");
							_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
							_propertyType = _ItemProperty.getAttribute("type");
							
							if( (_propertyName.equals("colSpan") ||  _propertyName.equals("rowSpan")) && _propertyValue.equals("0") )
							{
								_propertyValue = "1";
							}
							
							itemProperty.put(  _propertyName, _propertyValue );
						}
					}
					itemProperty.put(  "y", _argoHeight );	
					_tableMapRow.add(itemProperty);
				}
				
				NodeList _rowPropertyList = _tableMapItem.getElementsByTagName("property");
				_rowPropertyLength = _rowPropertyList.getLength();
				for( l = 0; l < _rowPropertyLength; l++) {
					_ItemProperty = (Element) _rowPropertyList.item(l);
					if(_ItemProperty.getParentNode().getNodeName().equals("row"))
					{
						if( _ItemProperty.getAttribute("name").equals("height"))
						{
							_argoHeight = _argoHeight + Float.valueOf(_ItemProperty.getAttribute("value").toString());
							break;
						}
					}
				}
				
				_allTableMap.add(_tableMapRow);
			}
		}
		// 각 맵별BorderString담아두기작업 종료
		
		for ( k = 0; k < _tableMaps.getLength(); k++) {
			
			colIndex = 0;
			
			_tableMapItem = (Element) _tableMaps.item(k);
			
			if(_newTalbeFlag)
			{
				_tableMapDatas = _tableMapItem.getElementsByTagName("tableMapData");
			}
			else
			{
				_tableMapDatas = _tableMapItem.getElementsByTagName("cell");
			}
			
			for ( l = 0; l < _tableMapDatas.getLength(); l++) {
				
				tableMapProperty = _allTableMap.get(k).get(l);
				
				itemProperty = new HashMap<String, Object>();
				
				_itemDataSetName = "";
				
				if(_newTalbeFlag)
				{
					_cellItem = (Element) ((Element) _tableMapDatas.item(l)).getElementsByTagName("cell").item(0);
				}
				else
				{
					_cellItem = (Element) _tableMapDatas.item(l);
				}
				
				_cellPropertys = (NodeList) _cellItem.getElementsByTagName("property");
				
				if( !_newTalbeFlag || tableMapProperty.get("status").toString().equals("MC") == false )
				{
					colIndex = colIndex + Integer.valueOf( tableMapProperty.get("colSpan").toString() );
				}
				
				if(_cellPropertys.getLength() == 0 ) continue;
				
				_propList = mItemPropVar.getItemName("UBLabel");
				if(_propList == null ) return null;
				
				// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
				if( _propList.containsKey("rowId") )
				{
					_propList.put("rowId", _dCnt);
				}
				
				rowIndex =  k + Integer.valueOf( tableMapProperty.get("rowSpan").toString() );
				
				// xml Item propertys
				for(int p = 0; p < _cellPropertys.getLength(); p++)
				{
					Element _propItem = (Element) _cellPropertys.item(p);

					String _name = _propItem.getAttribute("name");
					String _value = _propItem.getAttribute("value");
					
					if(_propList.containsKey(_name))
					{
						if( _name.equals("fontFamily"))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							if(common.isValidateFontFamily(_value))
								_propList.put(_name, _value);
							else
								_propList.put(_name, "Arial");
						}
						else if( _name.indexOf("Color") != -1)
						{
							_propList.put((_name + "Int"), _value);
							_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value));
							_propList.put(_name, _value);
						}
						else if( _name.equals("lineHeight"))
						{
							//_value = "1.16"; //TODO LineHeight Test
							_value = _value.replace("%25", "");
							_value = String.valueOf((Float.parseFloat(_value)/100));		
							_propList.put(_name, _value);
						}
						else if( _name.equals("borderType"))
						{
							_propList.put(_name, _value);
						}
						else if( _name.equals("text"))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name, _value);
						}
						else if( _name.equals("prompt"))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name, _value);
						}
						else if( _name.equals("validateScript"))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put(_name, _value);
						}
						else if( _name.equals("borderSide"))
						{
							ArrayList<String> _bSide = new ArrayList<String>();
							_value = URLDecoder.decode(_value, "UTF-8");
							if( !_value.equals("none") )
							{
								_bSide = mPropertyFn.getBorderSideToArrayList(_value);

								if( _bSide.size() > 0)
								{
									String _type = (String) _propList.get("borderType");
									_type = mPropertyFn.getBorderType(_type);
									_propList.put("borderType", _type);
								}

							}

							_propList.put(_name, _bSide);
						}
						else if( _name.equals("x"))
						{
							_propList.put(_name, _value);
							_propList.put("left", _value);
						}
						else if( _name.equals("y"))
						{
							_propList.put(_name, _value);
							_propList.put("top", _value);
						}
						else if( _name.equals("type") )
						{
							_model_type = _value;
							_propList.put(_name, _value);
						}
						else if( _name.equals("barcodeType") )
						{
							_barcode_type = _value;
							_propList.put(_name, _value);
						}
						else if(_name.equals("column"))
						{
							_propList.put(_name,_value);
							_dataColumn = _value;
						}
						else if(_name.equals("dataSet"))
						{
							_dataID = _value;
							_propList.put(_name,_value);
						}
						else
						{
							_propList.put(_name, _value);
						}
					}
					else if(_name.equals("dataType"))
					{
						_dataType=_value;
						_propList.put(_name,_value);
					}
					
					else if( _name.equals("systemFunction") ){
						
						if( !_value.equalsIgnoreCase("null") && !_value.equalsIgnoreCase("") ){
							_value = URLDecoder.decode(_value, "UTF-8");
							mFunction.setDatasetList(_data);
							
							String _fnValue;
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								_fnValue = mFunction.testFN(_value.toString(),_dCnt, _totalPageNum , _currentPageNum , -1 , -1, "" );
							}else{
								_fnValue = mFunction.function(_value.toString(),_dCnt, _totalPageNum , _currentPageNum , -1 , -1);
							}
							
							_propList.put("text", _fnValue);
							_propList.put(_name, _value);
						}
					}
					else if( _name.equals("width"))
					{
						_propList.put(_name,_value);
					}
					else if( _name.equals("height"))
					{
						_propList.put(_name,_value);
					}
					else if(_name.equals("lineThickness"))
					{
						_propList.put("thickness", _value);
					}
					else if(_name.equals("column"))
					{
						_propList.put(_name,_value);
						_dataColumn = _value;
					}
					else if(_name.equals("dataSet"))
					{
						_dataID = _value;
						_propList.put(_name,_value);
					}
					else if(_name.equals("formatter"))
					{
						_formatter = _value;
						_propList.put("formatter", _formatter);
					}
					else if(_name.equals("editItemFormatter"))
					{
						_formatterE = _value;
						_propList.put("editItemFormatter", _formatterE);
					}
					else if(_name.equals("nation"))
					{
						_nation = URLDecoder.decode(_value, "UTF-8");
					}
					
					else if(_name.equals("align"))
					{
						_align=_value;
					}
					else if( _name.equals("mask") ){
						_mask = URLDecoder.decode(_value, "UTF-8");
					}
					
					else if( _name.equals("decimalPointLength") ){
						
						_decimalPointLength =  common.ParseIntNullChk(_value, 0);
					}				
					
					else if( _name.equals("useThousandComma") ){
						_useThousandComma = Boolean.parseBoolean(_value);
					}		
					else if( _name.equals("isDecimal") ){
						_isDecimal = Boolean.parseBoolean(_value);
					}		
					else if( _name.equals("formatString") ){
						_formatString = _value;
					}	
					else if( _name.equals("resizeFont") && "true".equals(_value)  )
					{
						_resizeFont = true;
					}
					
				}
				
				if( _dataType.equals("1") )
				{
					ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
					if( _list == null || _dCnt >= _list.size()) 
					{
						_propList.put("text", "");
						continue;
					}
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					
					Object _dataValue = _dataHm.get(_dataColumn);
					
					_propList.put("text", _dataValue == null ? "" : _dataValue);
					

				}
				else if( _dataType.equals("3"))
				{
					String _txt = _propList.get("text").toString();
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					
					if( _inOf != -1 )
					{
						mFunction.setParam(_param);
						_txt=mFunction.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							
							String _fnValue;
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								//_fnValue = mFunction.testFN(_txt,rowIndex,_totalPageNum,_currentPageNum, -1,-1 , "" );
								_fnValue = _txt;
							}else{
								_fnValue = mFunction.function(_txt,rowIndex,_totalPageNum,_currentPageNum, -1,-1 , "" );
							}
							
							_propList.put("text", _fnValue);
						}else{
							
							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);
							
							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);
							
							String _pValue = _pList.get("parameter");
							
							if( _pValue.equals("undefined"))
							{
								_propList.put("text", "");
							}
							else
							{
								_propList.put("text", _pValue);
							}
						}
						
					}
					
				}
				
				
				boolean useRightBorder = false;
				boolean useBottomBorder = false;
				
				boolean useLeftBorder = false;
				boolean useTopBorder = false;
				
				if( l>0 ) useLeftBorder = true;
				if( k>0 ) useTopBorder = true;
				
				if(_newTalbeFlag)
				{
					if( colIndex == _tableMapDatas.getLength() ) useRightBorder = true;
					if( rowIndex == _tableMaps.getLength() ) useBottomBorder = true;
				}
				else
				{
					if( l+ Integer.valueOf(tableMapProperty.get("colSpan").toString()) >= _tableMapDatas.getLength()-1 ) useRightBorder = true;
					if( k+ Integer.valueOf(tableMapProperty.get("rowSpan").toString()) >= _tableMaps.getLength()-1 ) useBottomBorder = true;
				}
				
				String _rightBorderStr = "";
				String _bottomBorderStr = "";
				
				if(useRightBorder)
				{
					if(_newTalbeFlag) _rightBorderStr =  _allTableMap.get(k).get(_tableMapDatas.getLength()-1 ).get("borderString").toString();
					else  _rightBorderStr =  tableMapProperty.get("borderString").toString();
					
				}
				if(useBottomBorder)
				{ 
					if(_newTalbeFlag) _bottomBorderStr =  _allTableMap.get(_tableMaps.getLength()-1).get(l).get("borderString").toString();
					else  _bottomBorderStr =  tableMapProperty.get("borderString").toString();
				}
				
				if(!(isExportType.endsWith("PPT"))){
					borderAr = ItemConvertParser.convertCellBorder( tableMapProperty.get("borderString").toString(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr, useLeftBorder, useTopBorder,isExportType );
				}
				else{
					borderAr = ItemConvertParser.convertCellBorderForPPT( tableMapProperty.get("borderString").toString(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr );
				}
				// 아이템의 Height값이 다음 Row의 Y값보다 클경우 사이즈를 수정. ( 신규 테이블일경우에만 지정 ) - 이전 테이블의 경우 rowSpan값이 부정확하여 정확한 위치가 잡히지 않을수 있음.
				//X좌표와 Width값 업데이트
				int _spanP = 0;
				int _indexP = 0;
				float _updateP = 0;
				//Y좌표와 Height값 업데이트
				if(_yAr.size() > 0 )
				{
					_spanP = Integer.valueOf(tableMapProperty.get("rowSpan").toString());
					_indexP = Integer.valueOf(tableMapProperty.get("rowIndex").toString());
					
					_propList.put("y", _yAr.get(_indexP) );
//					_updateP = (float) Math.floor((_yAr.get(_indexP + _spanP ) - _yAr.get(_indexP)) *10)/10;
					_updateP = (float) Math.round(_yAr.get(_indexP + _spanP ) - _yAr.get(_indexP) );
					_propList.put("height", _updateP);
				}
				
				_propList.put( "isCell",true );	
				
				_propList.put("borderSide", 	mPropertyFn.getBorderSideToArrayList( borderAr.get(0).toString() ) );
				_propList.put("borderTypes", 	borderAr.get(1) );
				_propList.put("borderColors",  	borderAr.get(2) );
				_propList.put("borderWidths",  	borderAr.get(3) );
				_propList.put("borderColorsInt", 	borderAr.get(4) );
				
				_propList.put("ORIGINAL_TABLE_ID", _itemId);
				
				// 포맷터 값이존재할경우각각의 데이터를 담는 처리
				NodeList formatterItem = _cellItem.getElementsByTagName("formatter");
				if( formatterItem != null && formatterItem.getLength() > 0 )
				{
					NodeList formatterProperty = ((Element) formatterItem.item(0)).getElementsByTagName("property");
					int propertySize = formatterProperty.getLength();
					for (int p = 0; p < propertySize; p++) {
						
						Element _propItem = (Element) formatterProperty.item(p);

						String _name = _propItem.getAttribute("name");
						String _value = _propItem.getAttribute("value");
						
						if( _name.equals("nation") ){
							_nation = URLDecoder.decode(_value, "UTF-8");
						}
						
						else if( _name.equals("mask") ){
							_mask = URLDecoder.decode(_value, "UTF-8");
						}
						else if( _name.equals("decimalPointLength") ){
							
							_decimalPointLength = common.ParseIntNullChk(_value, 0);
						}				
						else if( _name.equals("useThousandComma") ){
							_useThousandComma = Boolean.parseBoolean(_value);
						}		
						else if( _name.equals("isDecimal") ){
							_isDecimal = Boolean.parseBoolean(_value);
						}		
						else if( _name.equals("formatString") ){
							_formatString = _value;
						}
						else if(_name.equals("align"))
						{
							_align = _value;
						}
						else if( _name.equals("inputFormatString") )
						{
							_inputForamtString =  URLDecoder.decode(_value , "UTF-8");
						}
						else if( _name.equals("outputFormatString") )
						{
							_outputFormatString =  URLDecoder.decode(_value , "UTF-8");
						}
						
					}
					
					if(labelProjectFlag) _propList.put("formatterElement", 	formatterProperty );
				}
				
				
				
				// 포맷터 값이존재할경우각각의 데이터를 담는 처리
				NodeList formatterEditItem = _cellItem.getElementsByTagName("editItemFormatter");
				if( formatterEditItem != null && formatterEditItem.getLength() > 0 )
				{
					String _eformatDataset=null;
					String _eformatKeyField=null;
					String _eformatLabelField=null;
					
					NodeList formatterProperty = ((Element) formatterEditItem.item(0)).getElementsByTagName("property");
					int propertySize = formatterProperty.getLength();
					for (int p = 0; p < propertySize; p++) {
						
						Element _propItem = (Element) formatterProperty.item(p);

						String _name = _propItem.getAttribute("name");
						String _value = _propItem.getAttribute("value");
						
						if( _name.equals("nation") ){
							_nationE = URLDecoder.decode(_value, "UTF-8");
							_propList.put("eformnation", 	_nationE );
						}
						
						else if( _name.equals("mask") ){
							_maskE = URLDecoder.decode(_value, "UTF-8");
							_propList.put("eformmask", 	_maskE );
						}
						else if( _name.equals("decimalPointLength") ){
							
							_decimalPointLengthE = common.ParseIntNullChk(_value, 0);
							
							_propList.put("eformdecimalPointLength", 	_decimalPointLengthE );
							
						}				
						else if( _name.equals("useThousandComma") ){
							_useThousandCommaE = Boolean.parseBoolean(_value);
							
							_propList.put("eformuseThousandComma", 	_useThousandCommaE );
						}		
						else if( _name.equals("isDecimal") ){
							_isDecimalE = Boolean.parseBoolean(_value);
							_propList.put("eformisDecimal", _isDecimalE	 );
						}		
						else if( _name.equals("formatString") ){
							_formatStringE = _value;
							_propList.put("eformformatString", _formatStringE	 );
						}
						else if( _name.equals("align") ){
							_alignE = _value;
							_propList.put("eformcurrencyAlign", _alignE	 );
						}
						else if( _name.equals("dataProvider") ){
							_value = URLDecoder.decode(_value, "UTF-8");
							_propList.put("eformDataProvider", _value	 );
						}else if( _name.equals("dataset") ){
							_eformatDataset = _value;
						}else if( _name.equals("keyField") ){
							_eformatKeyField = _value;
						}else if( _name.equals("valueField") ){
							_eformatLabelField = _value;
						}

					}
					
					 if( _formatterE.equals("SelectMenu") ){
							
						// dataset으로 comboBox 표현.
						if( _eformatDataset != null &&  (!_eformatDataset.equals("null")) && _data.containsKey(_eformatDataset) ){
							
							String _efText = _propList.get("text").toString();
							Boolean _hasValueKey = false;
							
							ArrayList<HashMap<String, Object>> _list = _data.get(_eformatDataset);
							
							HashMap<String, Object> _dataHm;
							Object _keyData;
							Object _labelData;
							
							JSONArray ja = new JSONArray();
							String _jsonStr=null;
							JSONObject jo;
							String _keyStr=null;
							String _labelStr=null;
							
							for( int _eformatIdx=0; _eformatIdx<_list.size(); _eformatIdx++ ){
								_dataHm = _list.get(_eformatIdx);
								_keyData = _dataHm.get(_eformatKeyField);
								_labelData = _dataHm.get(_eformatLabelField);
								_keyStr=_keyData.toString();
								_labelStr = _labelData.toString();
								
								if( _efText.equals(_keyStr) && _hasValueKey == false ){
									_hasValueKey = true;
									_propList.put("text", _labelStr );
								}
								
								jo = new JSONObject();
								jo.put("label", _labelStr);
								jo.put("value",_keyStr );
								ja.add(jo);
							}
							
							_jsonStr = ja.toJSONString();
							
							_propList.put("eformDataProvider", _jsonStr	 );
						}else{
							
							if( _propList.containsKey("eformDataProvider") == false && _propList.get("eformDataProvider") == null )
							{
								_propList.put("eformDataProvider", "[]");
							}
							
							String _jsonStr = _propList.get("eformDataProvider").toString(); 
							JSONParser jsonParser = new JSONParser();
							try {
								
								JSONArray ja = (JSONArray) jsonParser.parse(_jsonStr);
								
								String _efText = _propList.get("text").toString();
								JSONObject oj;
								for( int jsonIdx=0; jsonIdx<ja.size(); jsonIdx++ ){
									oj=(JSONObject) ja.get(jsonIdx);
									if( oj.get("value").equals(_efText) ){
										_propList.put("text", oj.get("label") );
										break;
									}
								}
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					}
					 
				}
				
				
				if(tableProperty.containsKey("band_x") && tableProperty.get("band_x")!= "" && tableProperty.get("band_x").equals("NaN") == false)
				{
					updateX = Float.valueOf(tableProperty.get("band_x").toString()) + Float.valueOf(_propList.get("x").toString());
				}
				else
				{
					updateX = Float.valueOf(tableProperty.get("x").toString()) + Float.valueOf(_propList.get("x").toString());
				}
				
				if(tableProperty.containsKey("band_y") && tableProperty.get("band_y")!= "" && tableProperty.get("band_y").equals("NaN") == false )
				{
					updateY = Float.valueOf(tableProperty.get("band_y").toString()) + Float.valueOf(_propList.get("y").toString());
				}
				else
				{
					if(_newTalbeFlag) updateY = Float.valueOf(tableProperty.get("y").toString()) + Float.valueOf(_propList.get("y").toString());
					else updateY = Float.valueOf(tableProperty.get("y").toString()) + Float.valueOf(tableMapProperty.get("y").toString());
				}
				
				_propList.put(  "band_x", updateX );
				_propList.put(  "band_y", updateY );
				
				
				// 2017-02-28 기존에는 cell 아이디를 여기서 생성했지만,
				// 에디터에서 cell 아이디를 생성하도록 변경하여 아래와 같이 구분함.
				if( _propList.get("id").equals("") ){
					_propList.put(  "id", "TB_0_" + _childItem.getAttribute("id")+ "_" + k + l );	
				}
				_propList.put(  "className", "UBLabel");
				
				
				
				float _updateX = 0;
				if(  _updateX > -1 ) updateX = _updateX + updateX;
				if(  _updateY > -1 ) updateY = _updateY + updateY;
				
				_propList.put("x", updateX);
				_propList.put("left", updateX);
				_propList.put("y", updateY);
				_propList.put("top", updateY);
					
				if( _cloneX != 0 )
				{
					_updateX = _cloneX + Float.valueOf( _propList.get("x").toString() );
					_propList.put("x", _updateX);
					_propList.put("left", _updateX);
				}
				
				if( _cloneY != 0 )
				{
					_propList.put("y", _cloneY + Float.valueOf( _propList.get("y").toString() ));
					_propList.put("top", _cloneY + Float.valueOf( _propList.get("y").toString() ));
				}
				
				//cell의 ubfx값이 있을경우 cell의 ubfx사용 아닐경우 table의 ubfx사용
				_ubfunction = _cellItem.getElementsByTagName("ubfunction");
				
				ArrayList<NodeList> _ubfxNodes = new ArrayList<NodeList>();
				int _nodeCnts = 0;
				
				//Table의 UBFX가 존재할경우 처리( Table의 ubfx를 먼저 처리후 Cell의 ubfx를 처리 )
				if( _tableUbfunction != null && _tableUbfunction.getLength() > 0 )
				{
					_ubfxNodes.add( _tableUbfunction );
				}
				_ubfxNodes.add(_ubfunction);
				_nodeCnts = _ubfxNodes.size();
				
				for(int _ubfxListIndex= 0; _ubfxListIndex < _nodeCnts; _ubfxListIndex++)
				{
					NodeList _selectNodeList = _ubfxNodes.get(_ubfxListIndex);
					
					for(int _ubfxIndex = 0; _ubfxIndex < _selectNodeList.getLength(); _ubfxIndex++)
					{
						Element _ubfxItem = (Element) _selectNodeList.item(_ubfxIndex);

						String _name = _ubfxItem.getAttribute("property");
						String _value = _ubfxItem.getAttribute("value");
						
						_value = URLDecoder.decode(_value, "UTF-8");
						
						String _fnValue;
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = mFunction.testFN(_value.toString(),_dCnt, _totalPageNum , _currentPageNum ,-1,-1, "" );
						}else{
							_fnValue = mFunction.function(_value.toString(),_dCnt, _totalPageNum , _currentPageNum ,-1,-1);
						}
						
						_fnValue = _fnValue.trim();
						
						if(  _name.equals("text") || _fnValue.equals("") == false)
						{
//							if(_name.indexOf("Color") != -1 )
//							{
//								_propList.put((_name + "Int"), mPropertyFn.changeColorHexToInt(_fnValue) );
//							}
//							_propList.put(_name, _fnValue);
							
							_propList = convertUbfxStyle(_name, _fnValue, _propList );
						}
					}
				}
				_ubfxNodes = null;
				
				if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
					String _formatValue="";
					if( _dataType.equalsIgnoreCase("1") ){
						ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
						if( _dCnt >= _list.size()) 
						{
							_formatValue = "";
						}
						else
						{
							HashMap<String, Object> _dataHm = _list.get(_dCnt);
							Object _dataValue = _dataHm.get(_dataColumn);
							_formatValue = _dataValue != null ? _dataValue.toString() : "";
						}
					}else{
						_formatValue = _propList.get("text").toString();
					}
					try {
						
						if( _formatter.equalsIgnoreCase("Currency") ){
							_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
						}else if( _formatter.equalsIgnoreCase("Date") ){
							_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
						}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
							_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
						}else if( _formatter.equalsIgnoreCase("MaskString") ){
							_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
						}
                        else if( _formatter.equalsIgnoreCase("CustomDate") )
						{
							_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
						}
						
					} catch (ParseException e) {
//						e.printStackTrace();
					}
					_propList.put("text", _formatValue);
					
					// format이 label band에서 안들어간다.
					_propList.put("formatter", _formatter);
					_propList.put("mask", _mask);
					_propList.put("decimalPointLength", _decimalPointLength);
					_propList.put("useThousandComma", _useThousandComma);
					_propList.put("isDecimal", _isDecimal);
					_propList.put("formatString", _formatString);
					_propList.put("nation", _nation);
					_propList.put("currencyAlign", _align);
					_propList.put("inputFormatString", _inputForamtString);
					_propList.put("outputFormatString", _outputFormatString);
					//
					
				}
				
				//hyperLinkedParam처리
				if( _propList.containsKey("ubHyperLinkType") && "2".equals( _propList.get("ubHyperLinkType") )  )
				{
					NodeList _hyperLinkedParam = _childItem.getElementsByTagName("ubHyperLinkParm");
					if( _hyperLinkedParam != null && _hyperLinkedParam.getLength() > 0 )
					{
						Element _hyperLinkEl = (Element) _hyperLinkedParam.item(0);
						NodeList _hyperLinkedParams = _hyperLinkEl.getElementsByTagName("param");
						int _hyperLinkedParamSize = _hyperLinkedParams.getLength();
						
						HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
						
						for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
						{
							Element _hyperParam = (Element) _hyperLinkedParams.item(_hyperIdx);
							NodeList _hyperPropertys = _hyperParam.getElementsByTagName("property");
							int _hyperPropertysSize = _hyperPropertys.getLength();
							String _hyperParamKey = "";
							String _hyperParamValue = "";
							String _hyperParamType = "";
							
							for (int _hyperProIdx = 0; _hyperProIdx <  _hyperPropertysSize; _hyperProIdx++) 
							{
								Element _hyperProperty = (Element) _hyperPropertys.item(_hyperProIdx);
								if( "id".equals(_hyperProperty.getAttribute("name")) )
								{
									_hyperParamKey = _hyperProperty.getAttribute("value").toString();
								}
								else if( "value".equals(_hyperProperty.getAttribute("name")) )
								{
									_hyperParamValue = _hyperProperty.getAttribute("value").toString();
								}
								else if( "type".equals(_hyperProperty.getAttribute("name")) )
								{
									_hyperParamType = _hyperProperty.getAttribute("value").toString();
								}
							}
							
							if( "DataSet".equals(_hyperParamType) )
							{
								String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
								String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
								
								_hyperParamValue = "";
								
								if(_data.containsKey(_hyperLinkedDataSetId))
								{
									ArrayList<HashMap<String, Object>> _list = _data.get( _hyperLinkedDataSetId );
									Object _dataValue = "";
									if( _list != null ){
										if( _dCnt < _list.size() )
										{
											HashMap<String, Object> _dataHm = _list.get(_dCnt);
											_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
										}
									}
								}
							}
							else if("Parameter".equals(_hyperParamType) )
							{
								String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
								String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
								HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

								String _pValue = _pList.get("parameter");

								if( _pValue.equals("undefined"))
								{
									_hyperParamValue = "";
								}
								else
								{
									_hyperParamValue = _pValue;
								}
							}
							
							_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
						}
						
						_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
					}
				}
				
				
				if( !_formatterE.equalsIgnoreCase("null") && !_formatterE.equalsIgnoreCase("") ){
					String _formatValue="";
					if( _dataType.equalsIgnoreCase("1") ){
						ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
						if( _dCnt >= _list.size()) 
						{
							_formatValue = "";
						}
						else
						{
							HashMap<String, Object> _dataHm = _list.get(_dCnt);
							Object _dataValue = _dataHm.get(_dataColumn);
							_formatValue = _dataValue != null ? _dataValue.toString() : "";
						}
					}else{
						_formatValue = _propList.get("text").toString();
					}
					try {
						
						if( _formatterE.equalsIgnoreCase("Currency") ){
							_formatValue =UBFormatter.currencyFormat("", _nationE, _alignE, _formatValue);
						}else if( _formatterE.equalsIgnoreCase("Date") ){
							_formatValue=UBFormatter.dateFormat(_formatStringE, _formatValue);
						}else if( _formatterE.equalsIgnoreCase("MaskNumber") ){
							_formatValue =UBFormatter.maskNumberEditFormat(_mask, _decimalPointLengthE, _useThousandCommaE, _isDecimalE, _formatValue);
						}else if( _formatterE.equalsIgnoreCase("MaskString") ){
							_formatValue=UBFormatter.maskStringEditFormat(_maskE, _formatValue,"*");
						}
					} catch (ParseException e) {
//						e.printStackTrace();
					}
					_propList.put("textE", _formatValue);
					
					// format이 label band에서 안들어간다.
					
					_propList.put("editItemFormatter", _formatterE);
					_propList.put("eformmask", _maskE);
					_propList.put("eformdecimalPointLength", _decimalPointLengthE);
					_propList.put("eformuseThousandComma", _useThousandCommaE);
					_propList.put("eformisDecimal", _isDecimalE);
					_propList.put("eformformatString", _formatValue);
					_propList.put("eformcurrencyAlign", _alignE);
					//
				}
				
				// resize Font값이 true일경우에 만 진행 2016-04-12 최명진
				if( _resizeFont && !"".equals(_propList.get("text")) )
				{
					float _fontSize 	= Float.valueOf( _propList.get("fontSize").toString() );
					String _fontFamily 	= _propList.get("fontFamily").toString();
					String _fontWeight 	= _propList.get("fontWeight").toString();
					
					float _padding = (_propList.containsKey("padding"))? Float.valueOf( _propList.get("padding").toString()):3;
					
					float _maxBorderSize = 0;
					if(_propList.containsKey("borderWidths"))
					{
						ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
						
						for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
							if(_maxBorderSize < _borderWidths.get(_bIndex))
							{
								_maxBorderSize = _borderWidths.get(_bIndex);
							}
						}
						_padding = _maxBorderSize + _padding;
					}
					
					float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
					
					_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
					_propList.put("fontSize",  _fontSize);

				}
				// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
				if(_propList.containsKey("text"))
					_propList.put("tooltip", _propList.get("text").toString());
				
				//Export시 테이블형태로 내보내기 위한 속성추가
				if(_newTalbeFlag)
				{
					_propList.put("TABLE_ID", _childItem.getAttribute("id"));
					_propList.put("cellHeight", _propList.get("height"));
					_propList.put("cellY", _propList.get("y"));
					_propList.put("isTable", "true" );
				}
				
				if(_className.equals("UBApproval"))
				{
					if(_ubApprovalAr == null)
					{
						_ubApprovalAr = new ArrayList<HashMap<String, Object>>();
						_ubApprovalAr.add(tableProperty);
					}
					_ubApprovalAr.add(_propList);
				}
				else
				{
					_propList.put("ELEMENT_XML", _cellItem);
					_objects.add(_propList);
				}
				
			}
			
		}
		
		if(_className.equals("UBApproval") && _ubApprovalAr != null)
		{
			convertTableMapToApprovalTbl(_ubApprovalAr, _objects, _data );
		}
		
		return _objects;
	}
	
	
	
	////////////////TEST  20171030

	public HashMap<String, Object> convertHashMapToItem(HashMap<String, Object> _childItem, int _dCnt,  HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param, float _cloneX, float _cloneY, float _updateY , int _totalPageNum , int _currentPageNum, boolean labelProjectFlag ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _itemId = (String) _childItem.get("id");
		String _className = (String) _childItem.get("className");

		HashMap<String, Object> _propList = _childItem;

		String _dataTypeStr = "";
		String _dataColumn = "";
		String _dataID = "";
		String _model_type = "";
		String _barcode_type = "";
		
		// formatter variables
		String _formatter="";
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";

		// edit formatter variables (e-form)
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";

		
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		// image variable
		String _prefix="";
		String _suffix="";
		String[] _datasets={"",""};
					
		boolean _resizeFont = false;
		
//		_propList = mItemPropVar.getItemName(_className);
//		if( _propList == null) return null;
		
		// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
		if( _propList.containsKey("rowId") )
		{
			_propList.put("rowId", _dCnt);
		}

		//hyperLinkedParam처리
		if( _propList.containsKey("ubHyperLinkTypeArr") && _propList.get("ubHyperLinkTypeArr") != null &&  "2".equals( _propList.get("ubHyperLinkType") )  )
		{
			ArrayList<HashMap<String,String>> _ubHyperLinkTypeArr = (ArrayList<HashMap<String,String>>) _propList.get("ubHyperLinkTypeArr");
			
			if( _ubHyperLinkTypeArr != null && _ubHyperLinkTypeArr.size() > 0 )
			{
				int _hyperLinkedParamSize = _ubHyperLinkTypeArr.size();
				
				HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
				
				for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
				{
					String _hyperParamKey = "";
					String _hyperParamValue = "";
					String _hyperParamType = "";
					
					if( _ubHyperLinkTypeArr.get(_hyperIdx).get("id") != null) _hyperParamKey = _ubHyperLinkTypeArr.get(_hyperIdx).get("id").toString();
					if( _ubHyperLinkTypeArr.get(_hyperIdx).get("value") != null) _hyperParamValue = _ubHyperLinkTypeArr.get(_hyperIdx).get("value").toString();
					if( _ubHyperLinkTypeArr.get(_hyperIdx).get("type") != null) _hyperParamType = _ubHyperLinkTypeArr.get(_hyperIdx).get("type").toString();
					
					if( "DataSet".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						
						_hyperParamValue = "";
						
						if(_data.containsKey(_hyperLinkedDataSetId))
						{
							ArrayList<HashMap<String, Object>> _list = _data.get( _hyperLinkedDataSetId );
							Object _dataValue = "";
							if( _list != null ){
								if( _dCnt < _list.size() )
								{
									HashMap<String, Object> _dataHm = _list.get(_dCnt);
									_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
								}
							}
						}
					}
					else if("Parameter".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						if( _param.containsKey(_hyperLinkedDataSetColumn))
						{
							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

							String _pValue = _pList.get("parameter");

							if( _pValue.equals("undefined"))
							{
								_hyperParamValue = "";
							}
							else
							{
								_hyperParamValue = _pValue;
							}
						}
						else
						{
							_hyperParamValue = "";
						}
					}
					
					_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
				}
				
				_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
			}
			
		}
		
//		_propList = convertItemToBorder(_propList);
		
		//DataSet정보 처리
		if( _propList.containsKey("dataType") && (_propList.get("dataType").equals("1") || _propList.get("dataType").equals("3"))  )
		{
			_dataType=_propList.get("dataType").toString();
			_dataID = _propList.get("dataSet").toString();
			_dataColumn = _propList.get("column").toString();
			
			if( _dataType.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				//HashMap<String, Object> _dataHm = _list.get(_dCnt);
				
				if( _list == null || _dCnt >= _list.size() ) 
				{
					_propList.put("text", "");
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					
					if( _dataValue == null ){
						_dataValue = "null";
					}
					
					// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
					if(_className.equals("UBSVGArea") ){
						
						if(_dataValue==null ||_dataValue.toString().equals("")) return null;
						
						String _tmpDataValue = _dataValue.toString();
						
						if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
						{
							_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
						}
						
						boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
						boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
						boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
						
						if(_bSVG)
						{
							_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
						}
						else
						{
							boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
							if(!_bHasHtmlTag)
								_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
								
							_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
							_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
						}
										
						_dataValue = _dataValue.toString().replace(" ", "%20");
						
						if( !_dataValue.toString().equals("") )
						{
							_propList.put("data",  URLEncoder.encode(_dataValue.toString(), "UTF-8"));
						}
						else
						{
							return null;
						}
						
					}
					else if( _className.equals("UBSVGRichText") )
					{
						if( _dataValue != null && _dataValue.toString().equals("") == false )
						{
							_propList = convertUBSvgItem( _dataValue.toString(), _propList);
							
							if(_propList == null ) return null;
						}
					}
					
					if("UBImage".equals(_className) || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture"))
					{
//						_propList.put("src", _dataValue);
						if( _dataValue != null ) _dataValue =  URLEncoder.encode( _dataValue.toString() , "UTF-8");
						_propList.put("src", _dataValue );
					}
					else if("UBCheckBox".equals(_className) || "UBRadioBorder".equals(_className))
					{
						_propList.put("selected", _dataValue);
					}					
					else
					{
						_propList.put("text", _dataValue);
					}
				}
				
			}
			else if( _dataType.equals("3"))
			{
				String _txt = _propList.get("text").toString();
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					
					if( _inOf != 0 ){
						
						// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
						if(_className.equals("UBSVGArea") ){
							
							String _tmpDataValue = String.valueOf(_txt);
							if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
							{
								_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
							}
							
							boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
							boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
							boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
							
							String _svgTag = null;
							if(_bSVG)
							{
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
							}
							else
							{
								boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
								if(!_bHasHtmlTag)
									_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
									
								_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							}
												
							log.debug("11649-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _svgTag + "]");
							
							_svgTag = _svgTag.replace(" ", "%20");
							
							if( !_svgTag.equals("") )
							{
								_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8"));
							}
							else
							{
								return null;
							}
							
							_txt = "";
							
						}
						else if( _className.equals("UBSVGRichText") )
						{
							if( _txt != null && _txt.equals("") == false )
							{
								_propList = convertUBSvgItem( _txt, _propList);
								
								if(_propList == null ) return null;
							}
						}
						
						
						String _fnValue;
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							//_fnValue = mFunction.testFN(_txt,_dCnt,_totalPageNum,_currentPageNum, -1 , -1, "" );
							_fnValue = _txt;
						}else{
							_fnValue = mFunction.function(_txt,_dCnt,_totalPageNum,_currentPageNum, -1 , -1 );
						}
						
						
						if(_className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture")){
							_propList.put("src",  URLEncoder.encode(_fnValue, "UTF-8"));
						}else{
							_propList.put("text", _fnValue);
						}
						
						
						
					}else{
						
						int _paramFnBraketIndex =_txt.indexOf("}",_inOf); 
						if( _paramFnBraketIndex != -1 ){
							_pKey = _txt.substring(_inOf + 7 , _paramFnBraketIndex );
						}else{
							_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);	
						}

						
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_propList.put("text", "");
						}
						else
						{
							_propList.put("text", _pValue);
						}
						
					}
					

				}
				else
				{
					_propList.put("text", "");
				}
			}
		}
		else if( _propList.containsKey("dataSet") && _propList.get("dataSet") != null )
		{
			_dataID = _propList.get("dataSet").toString();
		}
		
		
		float _updateX = 0;
		if(  _updateY > -1 )
		{
			_propList.put("y", _updateY);
			_propList.put("top", _updateY);
		}
		else if( _cloneY != 0 )
		{
			_propList.put("y", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
			_propList.put("top", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
		}
		
		if( _cloneX != 0 )
		{
			_updateX = _cloneX + Float.valueOf( _propList.get("x").toString() );
			_propList.put("x", _updateX);
			_propList.put("left", _updateX);
		}
		
		//Image 
		if(_className.equals("UBImage")  || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture")){
			if( _dataType.equals("1") )
			{
				
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				Object _dataValue;
				if( _list == null || _dCnt >= _list.size()) 
				{
					_dataValue = "";
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					_dataValue = _dataHm.get(_dataColumn);
				}
				String _url="";
				String _txt = "";
				if( _dataValue != null ){
					_txt=_dataValue.toString();
				}
				
				_url= _prefix + _txt + _suffix;

				String	_servicesUrl = "";
				if(_prefix.equalsIgnoreCase("BLOB://") )
				{
					// BLOB 이미지의 Resize처리
					try {
						if(_txt.length() > ImageUtil.MAXINUM_LENGTH )
						{
							_txt = ImageUtil.resizeBLOBData(_txt, Float.valueOf(_propList.get("width").toString()).intValue(),  Float.valueOf(_propList.get("width").toString()).intValue(), true); 
						}
					} catch (IOException e) {
						
					}
					_servicesUrl = URLEncoder.encode(_txt, "UTF-8");
					
				}
				else
				{
					_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+ URLEncoder.encode(_url,"UTF-8");
				}
				
				_propList.put("src", _servicesUrl);
			}
		}
		else if( _className.equals("UBSVGArea") || _className.equals("UBSVGRichText") )
		{
			// UBSVGArea에 값 Add
			
			if( _dataType.equals("1") || _dataType.equals("3") )
			{
				_propList.put("src", _propList.get("text"));
				_propList.remove("text");
			}
		}
		
		ArrayList<String> _ubfunctionList = (ArrayList<String>) _propList.get("ubfunctionList");
		if(_ubfunctionList != null )
		{
			for(int _ubfxIndex = 0; _ubfxIndex < _ubfunctionList.size(); _ubfxIndex++)
			{
				String[] _ubfxStrAr = _ubfunctionList.get(_ubfxIndex).split("§");
				String _name = _ubfxStrAr[0];
				String _value = _ubfxStrAr[1];
				
				_value = URLDecoder.decode(_value, "UTF-8");
				mFunction.setDatasetList(_data);
				mFunction.setParam(_param);
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_value,_dCnt, _totalPageNum , _currentPageNum,-1,-1, "" );
				}else{
					_fnValue = mFunction.function(_value,_dCnt, _totalPageNum , _currentPageNum,-1,-1);
				}
				
				
				_fnValue = _fnValue.trim();
				
				_propList = convertUbfxStyle(_name, _fnValue, _propList );
				
//				if(_name.indexOf("Color") != -1 )
//				{
//					_propList.put((_name + "Int"), mPropertyFn.changeColorHexToInt(_fnValue) );
//				}
//				_propList.put(_name, _fnValue);
			}
		}
		
		if( _propList.get("formatter") != null && _propList.get("formatter").toString().equals("null") == false && !_propList.get("formatter").toString().equals("null") == false ){
			String _formatValue="";
			
			// format이 label band에서 안들어간다.
			_mask = _propList.get("mask").toString();
			_decimalPointLength = Integer.parseInt( _propList.get("decimalPointLength").toString() );
			_useThousandComma =  _propList.get("useThousandComma").toString().equals("true");
			_isDecimal 	=  _propList.get("isDecimal").toString().equals("true");
			_formatString = _propList.get("formatString").toString();
			_nation = _propList.get("nation").toString();
			
			
			if( _dataType.equalsIgnoreCase("1") ){
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				if( _list == null || _dCnt >= _list.size()) 
				{
					_formatValue = "";
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					_formatValue = _dataValue != null ? _dataValue.toString() : "";
				}
			}else{
				_formatValue = _propList.get("text").toString();
			}
			try {
				
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
			} catch (ParseException e) {
//				e.printStackTrace();
			}
			_propList.put("text", _formatValue);
			
		}
		
		if( _propList.get("editItemFormatter") != null && _propList.get("editItemFormatter").toString().equals("null") == false  && !_propList.get("editItemFormatter").toString().equals("null") == false ){
			String _formatValue="";
			
			// format이 label band에서 안들어간다.
			_maskE = _propList.get("eformmask").toString();
			_decimalPointLengthE = ValueConverter.getInteger(_propList.get("eformdecimalPointLength"));
			_useThousandCommaE = ValueConverter.getBoolean(_propList.get("eformuseThousandComma"));
			_isDecimalE 	= ValueConverter.getBoolean(_propList.get("eformisDecimal"));
			_formatValue = ValueConverter.getString(_propList.get("eformformatString"));
			
			if( _dataType.equalsIgnoreCase("1") ){
				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
				if( _dCnt >= _list.size()) 
				{
					_formatValue = "";
				}
				else
				{
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					_formatValue = _dataValue != null ? _dataValue.toString() : "";
				}
			}else{
				_formatValue = _propList.get("text").toString();
			}
			try {
				
				if( _formatterE.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nationE, _alignE, _formatValue);
				}else if( _formatterE.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatStringE, _formatValue);
				}else if( _formatterE.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberEditFormat(_mask, _decimalPointLengthE, _useThousandCommaE, _isDecimalE, _formatValue);
				}else if( _formatterE.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringEditFormat(_maskE, _formatValue,"*");
				}
			} catch (ParseException e) {
//				e.printStackTrace();
			}
			_propList.put("textE", _formatValue);
		}
		
		
		// resizeFont���� true�ϰ�� ó��
		if( _resizeFont && _propList.containsKey("text") && !"".equals(_propList.get("text")) )
		{
			float _fontSize 	= Float.valueOf( _propList.get("fontSize").toString() );
			String _fontFamily 	= _propList.get("fontFamily").toString();
			String _fontWeight 	= _propList.get("fontWeight").toString();
			
			float _padding = (_propList.containsKey("padding"))? Float.valueOf( _propList.get("padding").toString()):3;
			
			float _maxBorderSize = 0;
			if(_propList.containsKey("borderWidths"))
			{
				ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
				
				for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
					if(_maxBorderSize < _borderWidths.get(_bIndex))
					{
						_maxBorderSize = _borderWidths.get(_bIndex);
					}
				}
				_padding = _maxBorderSize + _padding;
			}
			
			float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
			
			_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
			_propList.put("fontSize",  _fontSize);

		}
		

		// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
		Boolean _hasTooltip=false;
		if( _propList.containsKey("tooltip") ){
			if( _propList.get("tooltip") != null && !(_propList.get("tooltip").equals("")) ){
				_hasTooltip = true;
			}
		}
		
		if( _hasTooltip == false ){
			if(_propList.containsKey("text"))
				_propList.put("tooltip", _propList.get("text").toString());
		}
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
			    		_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//_barcodeValue = URLDecoder.decode(_barcodeValue, "UTF-8");
				_propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 8 ){
					_barcodeSrc="";
				}
				/*
				else if( _barcode_type.equalsIgnoreCase("itf14") && _barcodeData.length() != 14 ){
					_barcodeSrc="";
				}
				*/
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		
		if( labelProjectFlag == false && _propList.containsKey("visible") && _propList.get("visible").equals("false"))
		{
			return null;
		}
		
		// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
		if( _propList.containsKey("rowId") && (_propList.get("dataType") == null || !( _propList.get("dataType").equals("1") || _propList.get("dataType").equals("2") ) ) )
		{
//			_propList.put("rowId", _currentPageNum);
		}
		
		_propList.put("className" , _className );
		_propList.put("id" , _itemId );
		
		// radioButtonGroup 
		if( _className.equals("UBRadioBorder") ){
			Boolean _isSelected = radiobuttonHandler(_propList);
			_propList.put("selected", _isSelected);
		}else if( _className.equals("UBRadioButtonGroup") ){
			radiobuttonGroupHandler(_propList);
		}
		
		return _propList;
	}
	
	
	public static String formatItemData(String _formatter, Element _formatElement, String _itemValue ) throws UnsupportedEncodingException
	{
		
		// formatter variables
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";

		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		String _inputForamtString="";
		String _outputFormatString="";

		// kyh formatter property setting
		Element ItemElement = _formatElement;
		NodeList _formatterNode = ItemElement.getElementsByTagName("formatter");
		Element _formatterItem = (Element)_formatterNode.item(0);
		String _formatValue= _itemValue;
		
		if( _formatterItem != null )
		{
			NodeList _formatterItemPropertyList = _formatterItem.getElementsByTagName("property");
			for( int _formatterIndex=0;  _formatterIndex < _formatterItemPropertyList.getLength(); _formatterIndex++ ){
				Element _formatterItemProperty = (Element)_formatterItemPropertyList.item(_formatterIndex);
				String _formatPropertyName = _formatterItemProperty.getAttribute("name");
				String _formatPropertyValue = _formatterItemProperty.getAttribute("value");
				try {
					_formatPropertyValue =URLDecoder.decode(_formatPropertyValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(_formatPropertyName.equals("nation"))
				{
					_nation = _formatPropertyValue;
				}
				else if(_formatPropertyName.equals("align"))
				{
					_align=_formatPropertyValue;
				}
				else if( _formatPropertyName.equals("mask") ){
					_mask = _formatPropertyValue;
				}
				
				else if( _formatPropertyName.equals("decimalPointLength") ){
					if(  _formatPropertyValue.equalsIgnoreCase("NaN") ){
						_decimalPointLength = 0;
					}else{
//						_decimalPointLength = Integer.parseInt(_formatPropertyValue);	
						_decimalPointLength = common.ParseIntNullChk(_formatPropertyValue, 0);
					}
				}				
				
				else if( _formatPropertyName.equals("useThousandComma") ){
					_useThousandComma = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("isDecimal") ){
					_isDecimal = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("formatString") ){
					_formatString = _formatPropertyValue;
				}
				else if( _formatPropertyName.equals("inputFormatString") )
				{
					_inputForamtString = URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
				else if( _formatPropertyName.equals("outputFormatString") )
				{
					_outputFormatString =  URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
			}
		}



		// formatter set	// ITem의 Formatter 처리 ( export처리를 위하여 속성값 담기 )
		if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
			Object _propValue;
			_propValue= _itemValue;
			_formatValue = _propValue.toString();
			String _excelFormatterStr = "";
			try {
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					_excelFormatterStr = _nation  + "§" + _align;
					
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					_excelFormatterStr = _formatString;
					
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					_excelFormatterStr = _mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal;
					
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
                else if( _formatter.equalsIgnoreCase("CustomDate") )
				{
					_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
					_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
				}
				
			} catch (ParseException e) {
				//e.printStackTrace();
			}
			
		}
		
		return _formatValue;
	}

	public static String formatItemDataJS(String _formatter, HashMap<String, Object> _formatValueMap, String _itemValue ) throws UnsupportedEncodingException
	{
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";

		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		String _inputForamtString="";
		String _outputFormatString="";
		String _formatValue = "";
		
		if( _formatValueMap != null && !_formatValueMap.isEmpty() )
		{
			for(String _key : _formatValueMap.keySet())
			{
				String _formatPropertyName = _key;
				String _formatPropertyValue =_formatValueMap.get(_key).toString();
				
				try {
					_formatPropertyValue =URLDecoder.decode(_formatPropertyValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if(_formatPropertyName.equals("nation"))
				{
					_nation = _formatPropertyValue;
				}
				else if(_formatPropertyName.equals("align"))
				{
					_align=_formatPropertyValue;
				}
				else if( _formatPropertyName.equals("mask") ){
					_mask = _formatPropertyValue;
				}
				else if( _formatPropertyName.equals("decimalPointLength") ){
					if(  _formatPropertyValue.equalsIgnoreCase("NaN") ){
						_decimalPointLength = 0;
					}else{
						_decimalPointLength = common.ParseIntNullChk(_formatPropertyValue, 0);	
					}
				}				
				else if( _formatPropertyName.equals("useThousandComma") ){
					_useThousandComma = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("isDecimal") ){
					_isDecimal = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("formatString") ){
					_formatString = _formatPropertyValue;
				}
				else if( _formatPropertyName.equals("inputFormatString") )
				{
					_inputForamtString = URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
				else if( _formatPropertyName.equals("outputFormatString") )
				{
					_outputFormatString =  URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
			}
		}

		// formatter set	// ITem의 Formatter 처리 ( export처리를 위하여 속성값 담기 )
		if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
			Object _propValue;
			_propValue= _itemValue;
			_formatValue = _propValue.toString();
			String _excelFormatterStr = "";
			try {
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					_excelFormatterStr = _nation  + "§" + _align;
					
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					_excelFormatterStr = _formatString;
					
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					_excelFormatterStr = _mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal;
					
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
                else if( _formatter.equalsIgnoreCase("CustomDate") )
				{
					_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
					_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
				}
				
			} catch (ParseException e) {
				//e.printStackTrace();
			}
			
		}
		
		return _formatValue;
	}
	
	public HashMap<String, Object> convertStrechLabel(HashMap<String, Object> _item)
	{
		String _itemTxt = "";
		ArrayList<Object> _resultTextAr = null;
		ArrayList<Float> _resultHeightAr = null;
		
		if( _item.containsKey("text")&& _item.get("text").equals("") == false )
		{
			// converting된 String값을 이용하여 변환된Height값을 담기
			
			HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
			
			// font Resize 기능 사용 여부 확인 - 사용시 fontSize의 변경여부를 체크후 adjustableHeight값 처리
			// 폰트 사이즈 변경시 최소값만큼만 작아지도록 지정.
			// 2016-04-11 최명진
			float _fontSize = Float.valueOf( _item.get("fontSize").toString() );
			String _fontFamily = _item.get("fontFamily").toString();
			String _fontWeight = _item.get("fontWeight").toString();
			float _padding = (_item.containsKey("padding"))? Float.valueOf(_item.get("padding").toString()):3;
			
			float _maxBorderSize = 0;
			if(_item.containsKey("borderWidths"))
			{
				
				ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _item.get("borderWidths");
				
				for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
					if(_maxBorderSize < _borderWidths.get(_bIndex))
					{
						_maxBorderSize = _borderWidths.get(_bIndex);
					}
				}
				_padding = _maxBorderSize + _padding;
			}
			
			float _itemWidth = Float.valueOf(_item.get("width").toString()) - (2 * _padding);
			
			optionMap.put("width", new float[]{ _itemWidth });
			optionMap.put("height", new float[]{ -1, -1 });
			optionMap.put("fontSize", new float[]{_fontSize});
			optionMap.put("lineHeight", new float[]{(float) 1.2});
			if( _item.containsKey("padding") == false ){
				optionMap.put("padding", new float[]{ 3 });
			}else{
				optionMap.put("padding", new float[]{Float.valueOf(_item.get("padding").toString())});
			}
			
			HashMap<String, Object> resultMap = StringUtil.getSplitCharacterLine( _item.get("text").toString(), optionMap, 
					_fontWeight , 
					_fontFamily , 
					_fontSize,
					0 );
			
			String _resultTxt = "";
			_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
			_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
			
			for(Object _str : _resultTextAr)
			{	
				if( "".equals(_resultTxt) == false )_resultTxt = _resultTxt + "\n";
				
				_resultTxt = _resultTxt + _str.toString();
			}
			_item.put("padding", 3);
			_item.put("height", _resultHeightAr.get(0).intValue() );
			_item.put("text", _resultTxt);
			_item.put("className", "UBLabel");
			_item.put("type", "borderLabel");
		}
		
		return _item;
	}
	
	public HashMap<String, Object> convertItemDataJson( BandInfoMapData _bandInfo, HashMap<String, Value> currentItemData, HashMap<String, 
			ArrayList<HashMap<String, Object>>>  _dataSet, int rowIndex ,HashMap<String, Object> _param, int _startIndex, 
			int _lastIndex , int _totalPageNum , int _currentPage, String _dataName, float _cloneX, float _cloneY, float _updateY ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _className 		= "";
		String _name 		= "";
//		String _value 		= "";
		Object _value 		= null;
		String _dataID 		= "";
		int k = 0;
		
		String _model_type = "";
		String _barcode_type = "";
		String _itemId = "";
		
//		속도개선을 위한 테스트 함수
//		if( mUseSimpleExcel )
		if( isExportType.equals("EXCEL") && mUseSimpleExcel )
		{
			return convertItemDataJsonSimpleExport( _bandInfo, currentItemData, _dataSet, rowIndex , _param, _startIndex, _lastIndex , _totalPageNum , _currentPage, _dataName, _cloneX, _cloneY, _updateY);
		}
		
		if(_bandInfo != null)
		{
			if( _dataName == null || _dataName.equals("") )
			{
				
				if( currentItemData.get("dataSet") != null ){
					if(   currentItemData.get("dataSet").getStringValue() != null ){
						if(  !currentItemData.get("dataSet").getStringValue().equals("") ){
							_dataID =  currentItemData.get("dataSet").getStringValue();
						}
					}
				}
				
				if( _bandInfo.getDataSet().equals("") == false && _bandInfo.getAdjustableHeight() && _bandInfo.getResizeText() )
				{
					_dataID = _bandInfo.getDataSet();
					
					if( mOriginalDataMap.get( _dataID ) != null && currentItemData.get("dataSet") != null && currentItemData.get("dataSet").getStringValue() != null && currentItemData.get("dataSet").getStringValue().equals("null") == false )
					{
						if( mOriginalDataMap.get( _dataID ).equals(currentItemData.get("dataSet").getStringValue()) == false )
						{
							_dataID = currentItemData.get("dataSet").getStringValue();
						}
					}
				}
				
			}
			else
			{
				if( mOriginalDataMap.get( _dataName ) != null && currentItemData.get("dataSet") != null && currentItemData.get("dataSet").getStringValue() != null && currentItemData.get("dataSet").getStringValue().equals("null") == false )
				{
					if( mOriginalDataMap.get( _dataName ).equals(currentItemData.get("dataSet").getStringValue()) == false )
					{
						_dataID = currentItemData.get("dataSet").getStringValue();
					}
					else
					{
						_dataID = _dataName;	
					}
				}
				else
				{
					_dataID = _dataName;
				}
			}
		}
		else if( currentItemData.containsKey("dataSet") && _dataSet.containsKey(currentItemData.get("dataSet").getStringValue()) )
		{
			_dataID = currentItemData.get("dataSet").getStringValue();
		}

		if(currentItemData.containsKey("id"))
		{
			_itemId = currentItemData.get("id").getStringValue();
		}
		
		HashMap<String, Object> _propList = new HashMap<String, Object>();
		
		_className = currentItemData.get("className").getStringValue();
		// image variable
		String _prefix="";
		String _suffix="";
					
		Boolean _bType = false;

		if(_className.equals("UBLabelBorder"))
		{
			_bType = true;
		}
		else
		{
			_bType = false;
		}
		
		if( _dataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
			

		Set<String> _keySet = currentItemData.keySet();
		Object[] hmKeys = _keySet.toArray();
		
		_propList = mItemPropVar.getItemName(_className);
		
		if(_propList == null ) return null;
		
		// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
		if( _propList.containsKey("rowId") )
		{
			_propList.put("rowId", rowIndex);
		}
		
		// system function
		String _systemFunction="";
		
		// formatter variables 
		String _formatter="";
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";
		String _inputForamtString = "";
		String _outputFormatString = "";
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		
		// edit formatter variables (e-form)
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";
		
		for ( k = 0; k < hmKeys.length; k++) {
			
			_name = (String) hmKeys[k];
			_value = currentItemData.get(_name).getValue();
			
			if(_propList.containsKey(_name))
			{
				if( _name.equals("fontFamily"))
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					if(common.isValidateFontFamily((String)_value))
						_propList.put(_name, _value);
					else
						_propList.put(_name, "Arial");
				}
				else if( _name.equals("contentBackgroundColors")  )
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					
					ArrayList<String> _arrStr = new ArrayList<String>();
					_arrStr = mPropertyFn.getColorArrayString( (String)_value );
					_propList.put(_name, _arrStr);
					
					_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
					_propList.put((_name + "Int"), _arrStr);
					
				}
				else if( _name.equals("contentBackgroundAlphas") )
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					
					ArrayList<String> _arrStr = new ArrayList<String>();
					_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
					_propList.put(_name, _arrStr);
					
				}
				else if( _name.indexOf("Color") != -1 && _name.equals("borderColors") == false && _name.equals("borderColorsInt") == false)
				{	
					//backgroundColor/fontColor 과 같이 color값이 ArrayList로 생성되어 있을경우 rowIndex값에 맞춰 color값을 변경
					if( _value.toString().contains(",") )
					{
						ArrayList<String> _valueArray = Value.setArrayString( _value.toString() );
						_value = _valueArray.get(rowIndex%_valueArray.size());
						_propList.put((_name + "Int"), _value);
						
						_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
						_propList.put(_name, _value);
					}
					else
					{
						_propList.put((_name + "Int"), _value);
						
						_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
						_propList.put(_name, _value);
					}
				}
				else if( _name.equals("lineHeight"))
				{
					//_value = "1.16"; //TODO LineHeight Test
					if( _value.toString().indexOf("%") != -1 )
					{
						_value = _value.toString().replace("%25", "").replace("%", "");
						_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
					}
					_propList.put(_name, _value);
				}
				else if( _name.equals("label"))
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("borderType"))
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("text"))
				{
					_propList.put(_name, _value == null ? "" : _value);
				}
				else if( _name.equals("prompt"))
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("value"))
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("borderSide"))
				{
					ArrayList<String> _bSide = new ArrayList<String>();
					if( currentItemData.get(_name).getStringValue().equals("none") == false )
					{
						
						_bSide = mPropertyFn.getBorderSideToArrayList( currentItemData.get(_name).getStringValue() );

						if( _bSide.size() > 0)
						{
							String _type = (String) _propList.get("borderType");
							_type = mPropertyFn.getBorderType(_type);
							_propList.put("borderType", _type);
						}

					}

					_propList.put(_name, _bSide);
				}
				else if( _name.equals("type") )
				{
					_model_type = _value.toString();
					_propList.put(_name, _value);
				}
				else if( _name.equals("barcodeType") )
				{
					_barcode_type = _value.toString();
					_propList.put(_name, _value);
				}
				else if( _name.equals("clipArtData") )
				{
					_propList.put(_name, _value + ".svg");
				}
				else if( _name.equals("dataProvider") )
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("ubHyperLinkUrl") )
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("ubHyperLinkText") )
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("fileDownloadUrl") )
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("band_y") )
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("x"))
				{
					_propList.put(_name, _value);
					_propList.put("left", _value);
				}
				else if( _name.equals("y"))
				{
					_propList.put(_name, _value);
					_propList.put("top", _value);
				}
				else if( _name.equals("selectedDate") )
				{
					/*
					 * DateField 의 날짜 값을 순수 숫자로 치환한다.
					 * 웹에디터에 dateFormat 속성을 변경할 수 없어 "yyyy-MM-dd"로 고정. 변경될 경우 수정필요.
					 * ex)
					 * 원래 값 : Thu Oct 6 13:31:12 GMT+0900 2016
					 * 변경된 값 : 2016-10-06
					*/
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					String ubDateFormat = "yyyy-MM-dd";
					SimpleDateFormat beforeFormat = new SimpleDateFormat("EEE MMM d kk:mm:ss 'GMT'Z yyyy",Locale.US);
					SimpleDateFormat afterFormat = new SimpleDateFormat(ubDateFormat);
					String convertedDateString = "";
					try {
						Date gmtDate = beforeFormat.parse(_value.toString());
						convertedDateString = afterFormat.format(gmtDate);
					} catch (ParseException e) {
						log.error(getClass().getName()+"::convertElementToItem::"+"DateField selectedDate parsing Fail.>>>"+e.getMessage());
					} finally{
						_propList.put(_name, convertedDateString);
						_propList.put("text", convertedDateString);
					}
				}
				else if(_name.equals("column"))
				{
					_propList.put(_name, URLDecoder.decode(_value.toString(), "UTF-8") );
				}
				else if(_name.equals("dataSet"))
				{
					_propList.put(_name, URLDecoder.decode(_value.toString(), "UTF-8") );
				}
				else
				{
					_propList.put(_name, _value);
				}
			}
			else if( _name.equals("checked") )
			{
				_propList.put("selected", _value);
			}
			else if( _name.equals("conerRadius") )
			{
				_propList.put("rx", _value);
				_propList.put("ry", _value);
			}
			else if( _name.equals("points")  )
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				
				ArrayList<String> _arrStr = new ArrayList<String>();
				_arrStr = mPropertyFn.getPathArrayString( _value.toString() );
				_propList.put("path", _arrStr);
			}
			else if( _name.equals("band_y") )
			{
				_propList.put(_name, _value);
			}
			else if(_name.equals("borderThickness"))
			{
				_propList.put("borderWidth", _value);
			}
			else if(_name.equals("borderWeight"))
			{
				_propList.put("borderWidth", _value);
			}else if( _name.equals("formatter") ){
				_formatter = _value.toString();
				_propList.put("formatter", _formatter);
			}else if( _name.equals("systemFunction") ){
				_systemFunction = _value.toString();
			}else if( _name.equals("editItemFormatter") )
			{
				_formatterE = _value.toString();
				_propList.put("editItemFormatter", _formatterE);
			}
			else if(_name.equals("dataType"))
			{
				_dataType = _value.toString();
				_propList.put(_name, _value);
			}
			
			else if( _name.equals("data") )
			{
				if(_className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture"))
				{
					_propList.put("src",  URLEncoder.encode(_value.toString(), "UTF-8"));
				}
			}
			else if( _name.equals("ubToolTip") ){
				// tooltip 현재는 text가 아닌 ubToolTip 속성값을 이용시 데이터셋의 경우 dataset_0.col_0 과 같이 담겨서 tooltip가 제대로 표시안되는 현상으로 임시 주석. 2016-11-17 최명진
				
				if(_className.equals("UBImage")) {
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					_propList.put("tooltip",  _value);
					//_propList.put("tooltip",  URLEncoder.encode(_value, "UTF-8"));
				}
			}
			else if( _name.equals("prefix") ){
				try {
					_prefix=URLDecoder.decode(_value.toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			else if( _name.equals("suffix") ){
				_suffix=_value.toString();
			}
			else if( _name.equals("leftBorderType") ||  _name.equals("rightBorderType") ||  _name.equals("topBorderType") ||  _name.equals("bottomBorderType") )
			{
				_propList.put(_name, _value);
			}
			// 명우 추가 
			else if(_name.equals("startPoint"))
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				String[] _sPoint = _value.toString().split(",");
				_propList.put("x1", Float.valueOf(_sPoint[0]));
				_propList.put("y1", Float.valueOf(_sPoint[1]));
			}
			else if(_name.equals("endPoint"))
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				String[] _ePoint = _value.toString().split(",");
				
				_propList.put("x2", Float.valueOf(_ePoint[0]));
				_propList.put("y2", Float.valueOf(_ePoint[1]));
			}
			else if( _name.equals("width"))
			{
				//if( !_propList.containsKey("x2") )_propList.put("x2", _value);
				_propList.put(_name, _value);
			}
			else if( _name.equals("height"))
			{
				//if( !_propList.containsKey("y2") )_propList.put("y2", _value);
				_propList.put(_name, _value);
			}
			else if(_name.equals("lineThickness"))
			{
				_propList.put("thickness", _value);
			}
			else if( _name.equals("rWidth") ||  _name.equals("rHeight") )
			{
				_propList.put(_name, _value);
			}
			else if( _name.equals("printVisible") )
			{
				if( ("PRINT".equals(isExportType) || "PDF".equals(isExportType)) && "false".equals(_value) )
				{
					return null;
				}
			}
			else if( _name.equals("markanyVisible") )
			{
				if( _isMarkAny && "PRINT".equals(isExportType) && "false".equals(_value) )
				{
					return null;
				}
			}
			else if( _name.equals("rotation") )
			{
				_propList.put(_name, _value);
				_propList.put("rotate", _value);
			}
			
			else if( _name.equals("columnAxisName") || _name.equals("lineAxisName") )
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				_propList.put(_name, _value);
			}
			else if( _name.equals("categoryMargin") || _name.equals("itemMargin")|| _name.equals("xFieldVisible")|| _name.equals("yFieldVisible") )
			{
				_propList.put(_name, _value);
			}
		}
		
		if( _className.toUpperCase().indexOf("LINE") == -1)
		{
			_propList.put("x1", _propList.get("x"));
			_propList.put("y1", _propList.get("y"));
			_propList.put("x2", _propList.get("width"));
			_propList.put("y2", _propList.get("height"));
		}
		
		// Item의 changeData가 있는지 확인
		if(mChangeItemList != null )
		{
			_propList = convertChangeItemDataText( _currentPage ,_propList, "");
		}
		
		// 보더업데이트
		if( _propList.containsKey("isCell") && _propList.get("isCell").toString().equals("false") )
		{
			_propList = convertItemToBorder(_propList);
		}
		
		if(currentItemData.containsKey("ORIGINAL_TABLE_ID"))
		{
			_propList.put("ORIGINAL_TABLE_ID", currentItemData.get("ORIGINAL_TABLE_ID").getStringValue() );
		}
		
		if(currentItemData.containsKey("beforeBorderType"))
		{
			_propList.put("beforeBorderType", currentItemData.get("beforeBorderType").getArrayBooleanValue());
		}
		
		if(currentItemData.containsKey("PRESERVE_ASPECT_RATIO"))
		{
			_propList.put("PRESERVE_ASPECT_RATIO", currentItemData.get("PRESERVE_ASPECT_RATIO").getStringValue());
		}
		
		/**
		if(_className.equals("UBImage") || _className.equals("UBSignature") || _className.equals("UBTextSignature") || _className.equals("UBPicture")){  
			String _url="";
			String _txt = "";
			String	_servicesUrl = "";
			
			if( _dataType.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list;
				
				_list = _dataSet.get(_dataID);
				
				Object _dataValue = "";
				if( _list != null && rowIndex < _list.size() )
				{
					HashMap<String, Object> _dataHm = _list.get(rowIndex);
					
					_dataValue = _dataHm.get( currentItemData.get("column").getStringValue() );
				}
				
				if( _dataValue != null ){
					_txt = _dataValue.toString();
				}
				
				_url= _prefix + _txt + _suffix;

				if(_prefix.equalsIgnoreCase("BLOB://"))
				{
					// BLOB 이미지의 Resize처리
					try {
						if(_txt.length() > ImageUtil.MAXINUM_LENGTH )
						{
							_txt = ImageUtil.resizeBLOBData(_txt, Float.valueOf(_propList.get("width").toString()).intValue(),  Float.valueOf(_propList.get("width").toString()).intValue(), true); 
						}
					} catch (IOException e) {
						
					}
					_servicesUrl = URLEncoder.encode(_txt, "UTF-8");
				}
				else
				{
					_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+  URLEncoder.encode(_url, "UTF-8");
				}
				
				_propList.put("src", _servicesUrl);
			}
			else if( _dataType.equals("2") && _propList.containsKey("src") && _propList.get("src") != null )
			{
				_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+ _propList.get("src").toString();
				_propList.put("src", _servicesUrl);
			}
			
			else if( _dataType.equals("3"))
			{
				_txt = _propList.get("text").toString();
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					if( _inOf != 0 ){
						
						String _fnValue = "";
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = mFunction.testFN(_txt,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,_dataName);
						}else{
							_fnValue = mFunction.function(_txt,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,_dataName);
						}
						_propList.put("text", _fnValue);
					}else{

						int _keyIndex=_txt.lastIndexOf("}");
						_pKey = _txt.substring(_inOf + 7 , _keyIndex);

						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_txt = "";
						}
						else
						{
							_txt = _pValue;
						}
					}
				}
				else
				{
					_txt = "";
				}
				
				if(_prefix.equalsIgnoreCase("BLOB://"))
				{
					// BLOB 이미지의 Resize처리
					try {
						if(_txt.length() > ImageUtil.MAXINUM_LENGTH )
						{
							_txt = ImageUtil.resizeBLOBData(_txt, Float.valueOf(_propList.get("width").toString()).intValue(),  Float.valueOf(_propList.get("width").toString()).intValue(), true); 
						}
					} catch (IOException e) {
						
					}
					_servicesUrl = URLEncoder.encode(_txt, "UTF-8");
				}
				else
				{
					_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+  URLEncoder.encode(_txt, "UTF-8");
				}
				
				_propList.put("src", _servicesUrl);
			}
		}
		*/
		
		/**		*/
		HashMap<String, Object> _formatValueMap = null;
		
		if(currentItemData.containsKey("formatValue"))
		{
			_formatValueMap = (HashMap<String, Object>) currentItemData.get("formatValue").getMapValue();
		}
		// formatter -> formatValue Object
		
		if( _formatValueMap != null && !_formatValueMap.isEmpty() )
		{
			for(String _key : _formatValueMap.keySet())
			{
				String _formatPropertyName = _key;
				String _formatPropertyValue =_formatValueMap.get(_key).toString();
				
				try {
					_formatPropertyValue =URLDecoder.decode(_formatPropertyValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(_formatPropertyName.equals("nation"))
				{
					_nation = _formatPropertyValue;
				}
				else if(_formatPropertyName.equals("align"))
				{
					_align=_formatPropertyValue;
				}
				else if( _formatPropertyName.equals("mask") ){
					_mask = _formatPropertyValue;
				}
				else if( _formatPropertyName.equals("decimalPointLength") ){
					if(  _formatPropertyValue.equalsIgnoreCase("NaN") ){
						_decimalPointLength = 0;
					}else{
						_decimalPointLength = common.ParseIntNullChk(_formatPropertyValue, 0);	
					}
				}				
				else if( _formatPropertyName.equals("useThousandComma") ){
					_useThousandComma = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("isDecimal") ){
					_isDecimal = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("formatString") ){
					_formatString = _formatPropertyValue;
				}
				else if( _formatPropertyName.equals("inputFormatString") )
				{
					_inputForamtString = URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
				else if( _formatPropertyName.equals("outputFormatString") )
				{
					_outputFormatString =  URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
			}
		}
		
		// 에디트 포맷터 값이존재할경우각각의 데이터를 담는 처리
		// kyh formatter property setting
		HashMap<String, Object> _editFormatValueMap = null;
		
		if(currentItemData.containsKey("editItemFormatValue"))
		{
			_editFormatValueMap = (HashMap<String, Object>) currentItemData.get("editItemFormatValue").getMapValue();
		}
		
		if( _editFormatValueMap != null && !_editFormatValueMap.isEmpty() )
		{	
			String _eformatDataset=null;
			String _eformatKeyField=null;
			String _eformatLabelField=null;
			
			for( String _key : _editFormatValueMap.keySet() ){
				
				String _propName = _key;
				String _propValue = _editFormatValueMap.get(_key).toString();
				
				if( _propName.equals("nation") ){
					_nationE = URLDecoder.decode(_propValue, "UTF-8");
					_propList.put("eformnation", 	_nationE );
				}
				else if( _propName.equals("mask") ){
					_maskE = URLDecoder.decode(_propValue, "UTF-8");
					_propList.put("eformmask", 	_maskE );
				}
				else if( _propName.equals("decimalPointLength") ){
					_decimalPointLengthE = common.ParseIntNullChk(_propValue, 0);
					
					_propList.put("eformdecimalPointLength", 	_decimalPointLengthE );
					
				}				
				else if( _propName.equals("useThousandComma") ){
					_useThousandCommaE = Boolean.parseBoolean(_propValue);
					
					_propList.put("eformuseThousandComma", 	_useThousandCommaE );
				}		
				else if( _propName.equals("isDecimal") ){
					_isDecimalE = Boolean.parseBoolean(_propValue);
					_propList.put("eformisDecimal", _isDecimalE	 );
				}		
				else if( _propName.equals("formatString") ){
					_formatStringE = _propValue;
					_propList.put("eformformatString", _formatStringE	 );
				}else if( _propName.equals("dataProvider") ){
					_propValue = URLDecoder.decode(_propValue, "UTF-8");
					_propList.put("eformDataProvider", _propValue	 );
				}else if( _propName.equals("dataset") ){
					_eformatDataset = _propValue;
				}else if( _propName.equals("keyField") ){
					_eformatKeyField = _propValue;
				}else if( _propName.equals("valueField") ){
					_eformatLabelField = _propValue;
				}
			}
			
			if( _formatterE.equals("SelectMenu") ){
				
				// dataset으로 comboBox 표현.
				if( _eformatDataset != null &&  (!_eformatDataset.equals("null")) && _dataSet.containsKey(_eformatDataset) ){
					
					String _efText = _propList.get("text").toString();
					Boolean _hasValueKey = false;
					
					ArrayList<HashMap<String, Object>> _list = _dataSet.get(_eformatDataset);
					
					HashMap<String, Object> _dataHm;
					Object _keyData;
					Object _labelData;
					
					JSONArray ja = new JSONArray();
					String _jsonStr=null;
					JSONObject jo;
					String _keyStr=null;
					String _labelStr=null;
					
					for( int _eformatIdx=0; _eformatIdx<_list.size(); _eformatIdx++ ){
						_dataHm = _list.get(_eformatIdx);
						_keyData = _dataHm.get(_eformatKeyField);
						_labelData = _dataHm.get(_eformatLabelField);
						_keyStr=_keyData.toString();
						_labelStr = _labelData.toString();
						
						if( _efText.equals(_keyStr) && _hasValueKey == false ){
							_hasValueKey = true;
							_propList.put("text", _labelStr );
						}
						
						jo = new JSONObject();
						jo.put("label", _labelStr);
						jo.put("value",_keyStr );
						ja.add(jo);
					}
					
					_jsonStr = ja.toJSONString();
					
					_propList.put("eformDataProvider", _jsonStr	 );
				}else{
					
					if( _propList.containsKey("eformDataProvider") == false && _propList.get("eformDataProvider") == null )
					{
						_propList.put("eformDataProvider", "[]");
					}
					
					String _jsonStr = _propList.get("eformDataProvider").toString(); 
					JSONParser jsonParser = new JSONParser();
					try {
						
						JSONArray ja = (JSONArray) jsonParser.parse(_jsonStr);
						
						String _efText = _propList.get("text").toString();
						JSONObject oj;
						for( int jsonIdx=0; jsonIdx<ja.size(); jsonIdx++ ){
							oj=(JSONObject) ja.get(jsonIdx);
							if( oj.get("value").equals(_efText) ){
								_propList.put("text", oj.get("label") );
								break;
							}
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}

		
		//Table의 UBFX가 존재할경우 처리( Table의 ubfx를 먼저 처리후 Cell의 ubfx를 처리 )
		if( currentItemData.containsKey("ubfx") || currentItemData.containsKey("tableUbfunction") )
		{
			ArrayList<HashMap<String, String>> _ubfxs = new ArrayList<HashMap<String, String>>();
			
			ArrayList<ArrayList<HashMap<String, String>>> _ubfxsList = new ArrayList<ArrayList<HashMap<String, String>>>();
			
			int _ubfxListCnt = 0;
			
			if( currentItemData.get("tableUbfunction") != null  )
			{
				ArrayList<HashMap<String, String>> _tblUbfxs = (ArrayList<HashMap<String, String>>) currentItemData.get("tableUbfunction").getValue();
				_ubfxsList.add( _tblUbfxs );
			}
			
			if( currentItemData.get("ubfx") != null )
			{
				_ubfxs = (ArrayList<HashMap<String, String>>) currentItemData.get("ubfx").getValue();
				_ubfxsList.add( _ubfxs );
			}
			
			int _nodeCnts = _ubfxsList.size();
			
			for(int _ubfxListIndex= 0; _ubfxListIndex < _nodeCnts; _ubfxListIndex++)
			{
				ArrayList<HashMap<String, String>> _selectUbfxList = _ubfxsList.get(_ubfxListIndex);
				
				int nSelectUbfxListSize = _selectUbfxList !=null ? _selectUbfxList.size() : 0;
				for(int _ubfxIndex = 0; _ubfxIndex < nSelectUbfxListSize; _ubfxIndex++)
				{
					HashMap<String, String> _ubfxItem = _selectUbfxList.get(_ubfxIndex);
					String _ubfxProperty = _ubfxItem.get("property");
					String _ubfxValue = _ubfxItem.get("value");
					try {
						_ubfxValue = URLDecoder.decode(_ubfxValue, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					ArrayList<HashMap<String, Object>> _pList = _dataSet.get(_dataID);
					String _datasetColumnName = currentItemData.get("column")==null ? "" : currentItemData.get("column").getStringValue();
					mFunction.setDatasetList(_dataSet);
					mFunction.setParam(_param);
					
					mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
					if(mGroupTotalPageIndex>0) mFunction.setSectionCurrentPageNum(mGroupCurrentPageIndex);
					mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
					if(mGroupTotalPageIndex>0) mFunction.setSectionTotalPageNum(mGroupTotalPageIndex);
					mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
					mFunction.setOriginalDataMap(mOriginalDataMap);
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,_dataName);
					}else{
						_fnValue = mFunction.function(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,_dataName);
					}
					
					_fnValue = _fnValue.trim();
					
					if( _ubfxProperty.equals("text") || _fnValue.equals("") == false)
					{
//						if(_ubfxProperty.indexOf("Color") != -1 )
//						{
//							_propList.put((_ubfxProperty + "Int"), mPropertyFn.changeColorHexToInt(_fnValue) );
//						}
//						_propList.put(_ubfxProperty, _fnValue.trim());			// 20170531 true false에 공백이 붙어 나오는 현상이 있어 수정
//						
//						// color 속성은 color + Int 속성을 넣어줘야 한다.
//						if( _ubfxProperty.contains("Color") ){
//							_propList.put((_ubfxProperty + "Int"), common.getIntClor(_fnValue) );
//						}
						
						_propList = convertUbfxStyle(_ubfxProperty, _fnValue, _propList );
					}
				}
			}
			
		}
		
		//hyperLinkedParam처리
		if( currentItemData.containsKey("ubHyperLinkType") && "2".equals( currentItemData.get("ubHyperLinkType").getStringValue() )  )
		{
			ArrayList<HashMap<String, String>> _hyperLinkedParam = (ArrayList<HashMap<String, String>>) currentItemData.get("ubHyperLinkParm").getObjectValue();
			HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
			
			if( _hyperLinkedParam != null && _hyperLinkedParam.size() > 0 )
			{
				int _hyperLinkedParamSize = _hyperLinkedParam.size();
				
				HashMap<String, String> _hMap;
				
				for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
				{
					_hMap = _hyperLinkedParam.get(_hyperIdx);
					String _hyperParamKey = "";
					String _hyperParamValue = "";
					String _hyperParamType = "";
					
					if( _hMap.containsKey("id") )
					{
						_hyperParamKey = _hMap.get("id").toString();
					}
					if( _hMap.containsKey("value") )
					{
						_hyperParamValue = _hMap.get("value").toString();
					}
					if( _hMap.containsKey("type") )
					{
						_hyperParamType = _hMap.get("type").toString();
					}
					
					if( "DataSet".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						
						_hyperParamValue = "";
						
						if(_dataSet.containsKey(_hyperLinkedDataSetId))
						{
							ArrayList<HashMap<String, Object>> _list = _dataSet.get( _hyperLinkedDataSetId );
							Object _dataValue = "";
							if( _list != null ){
								if( rowIndex < _list.size() )
								{
									HashMap<String, Object> _dataHm = _list.get(rowIndex);
									_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
								}
							}
						}
					}
					else if("Parameter".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_hyperParamValue = "";
						}
						else
						{
							_hyperParamValue = _pValue;
						}
					}
					
					_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
				}
				
				_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
			}
			
		}
		/***/
		
		if( currentItemData.containsKey("dataType") && currentItemData.containsKey("dataSet")  )	
		{
			String columnStr =  currentItemData.get("column").getStringValue();
			String dataTypeStr = currentItemData.get("dataType").getStringValue();
			
			if( currentItemData.containsKey("dataType_N") && currentItemData.get("dataType_N").getStringValue().equals("") == false )
			{
				dataTypeStr = currentItemData.get("dataType_N").getStringValue();
			}
			if( currentItemData.containsKey("column_N") && currentItemData.get("column_N").getStringValue().equals("") == false )
			{
				columnStr = currentItemData.get("column_N").getStringValue();
			}
			
			if( dataTypeStr.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list = _dataSet.get( _dataID );
				Object _dataValue = "";
				if( _list != null ){
					if( rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						_dataValue = _dataHm.get( columnStr );
					}
				}
				
				// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐.. 
				if(_className.equals("UBSVGArea") ){
					
					if(_dataValue==null || _dataValue.toString().equals("")) return null;
					
					String _tmpDataValue = _dataValue.toString();
					
					if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
					{
						_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
					}
					
					boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
					boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
					boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
					
					if(_bSVG)
					{
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
					}
					else
					{
						boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
						if(!_bHasHtmlTag)
							_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
							
						_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							
					}
					log.debug("13168-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _dataValue.toString() + "]");
					
					_dataValue = _dataValue.toString().replace(" ", "%20");
					
					if( !_dataValue.toString().equals("") )
					{
						_propList.put("data",  URLEncoder.encode(_dataValue.toString(), "UTF-8"));
					}
					else
					{
						return null;
					}
					
					
				}
				else if( _className.equals("UBSVGRichText") )
				{
					// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
					if( _dataValue != null && _dataValue.toString().equals("") == false )
					{
						_propList = convertUBSvgItem(_dataValue,_propList);
						
						if(_propList == null ) return null;
					}
				}
				
				
				else if("UBCheckBox".equals(_className) )
				{
					String _selectedText=_propList.get("selectedText").toString();
					//String _deSelectedText=_propList.get("deSelectedText").toString();
					if( _dataValue != null && _selectedText.equalsIgnoreCase(_dataValue.toString()) ){
						_propList.put("selected", "true");
					}else{
						_propList.put("selected", "false");	
					}
					
				}	
				else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") )
				{
					String _servicesUrl = convertImageData( _dataValue.toString() , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
					_propList.put("src", _servicesUrl);
				}
				else
				{
					_propList.put("text", _dataValue == null ? "" : _dataValue);
					
					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					_propList.put("tooltip", _propList.get("text"));
				}
				
			}
			else if( dataTypeStr.equals("2"))
			{
				// rowIndex : 현재 Row Index
				// dataSet : 그룹핑된 데이터셋
				if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
					
					ArrayList<HashMap<String, Object>> _pList = _dataSet.get( _dataID );
					String _datasetColumnName = columnStr;
					mFunction.setDatasetList(_dataSet);
					mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
					mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
					mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
					mFunction.setOriginalDataMap(mOriginalDataMap);
					
					
					String _fnValue;
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_systemFunction , rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName);
					}else{
						_fnValue = mFunction.function(_systemFunction,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName );
					}
					

					if( _className.equals("UBSVGRichText") )
					{
						if( _fnValue != null && _fnValue.equals("") == false )
						{
							// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
							_propList = convertUBSvgItem(_fnValue,_propList);
							
							if(_propList == null ) return null;
						}

					}
					else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
//						_fnValue = URLDecoder.decode(_fnValue, "UTF-8");
						
						String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
						_propList.put("src", _servicesUrl);
						
					}
					else if(_className.equals("UBCheckBox") ) {
						_propList.put("selected", _fnValue);
					}
					else
					{
						_propList.put("text", _fnValue == null ? "" : _fnValue);
					}
				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			else if( dataTypeStr.equals("3"))
			{
				String _txt = _propList.get("text").toString();
				
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				String _fnValue = "";
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					if( _inOf != 0 ){
						
						// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
						if(_className.equals("UBSVGArea")  ){
							
							String _tmpDataValue = String.valueOf(_txt);
							
							if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
							{
								_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
							}
							
							boolean _bSVG = (_tmpDataValue != null && ( _tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
							boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )? _propList.get("preserveAspectRatio").toString().equals("true"):false;
							boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
							
							String _svgTag = null;
							if(_bSVG)
							{
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							}
							else
							{
								boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
								if(!_bHasHtmlTag)
									_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
									
								_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
									
							}
													
							log.debug("13315-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _svgTag + "]");
							
							_svgTag = _svgTag.toString().replace(" ", "%20");
							if( !_svgTag.equals("") )
							{
								_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8"));
							}
							else
							{
								return null;
							}
							
							_txt = "";
						}
						else if( _className.equals("UBSVGRichText") )
						{
							if( _txt != null && _txt.equals("") == false )
							{
//								_propList = convertUBSvgItem( _propList.get("text").toString(), _propList);
								_propList = convertUBSvgItem( _txt, _propList);
								
								if(_propList == null ) return null;
							}
							
							_txt = "";
						}
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							//_fnValue = mFunction.testFN(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName );
							_fnValue = _txt;
						}else{
							_fnValue = mFunction.function(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName );
						}
						
					}else{

						int _keyIndex=_txt.lastIndexOf("}");
						_pKey = _txt.substring(_inOf + 7 , _keyIndex);

						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						_fnValue = _pList.get("parameter");

					}
					
					if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
						_fnValue = URLDecoder.decode(_fnValue.replace(" ", "%20"), "UTF-8");
						
						String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
						_propList.put("src", _servicesUrl);
					}
					else
					{
						if( _fnValue.equals("undefined"))
						{
							_propList.put("text", "");
						}
						else
						{
							_propList.put("text", _fnValue);
						}
					}
					

				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			
		}
		
		// formatter set	// ITem의 Formatter 처리 ( export처리를 위하여 속성값 담기 )
		if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
			Object _propValue;
			String _formatValue="";
			_propValue=_propList.get("text");
			_formatValue = _propValue.toString();
			String _excelFormatterStr = "";
			try {
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					_excelFormatterStr = _nation  + "§" + _align;
					
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					_excelFormatterStr = _formatString;
					
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					_excelFormatterStr = _mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal;
					
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
                else if( _formatter.equalsIgnoreCase("CustomDate") )
				{
					_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
					_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
					
					_propList.put("inputFormatString", _inputForamtString);
					_propList.put("outputFormatString", _outputFormatString);
				}
				
			} catch (ParseException e) {
				//e.printStackTrace();
			}
			
			if( isExportType.equals("EXCEL") && _excelFormatterStr.equals("") == false && common.getPropertyValue("excelExport.useFormatter") != null && common.getPropertyValue("excelExport.useFormatter").equals("true") ) 
			{
				_propList.put("EX_FORMATTER", _formatter);
				_propList.put("EX_FORMAT_DATA_STR", _excelFormatterStr);
				_propList.put("EX_FORMAT_ORIGINAL_STR", _propValue.toString() );
			}
			
			_propList.put("text", _formatValue);
			
			// format이 label band에서 안들어간다.
			_propList.put("formatter", _formatter);
			_propList.put("mask", _mask);
			_propList.put("decimalPointLength", _decimalPointLength);
			_propList.put("useThousandComma", _useThousandComma);
			_propList.put("isDecimal", _isDecimal);
			_propList.put("formatString", _formatString);
			_propList.put("nation", _nation);
			_propList.put("currencyAlign", _align);
			_propList.put("inputFormatString", _inputForamtString);
			_propList.put("outputFormatString", _outputFormatString);
			//
			
		}
		
		
		//ResizeFont 값이 true이고 adjustableHeight값이 true 일경우 처리 
		if( _propList.containsKey("text") &&  "".equals(_propList.get("text").toString()) == false && currentItemData.containsKey("resizeFont") && currentItemData.get("resizeFont").getBooleanValue() )
		{
			float _fontSize 	= Float.valueOf( _propList.get("fontSize").toString() );
			String _fontFamily 	= _propList.get("fontFamily").toString();
			String _fontWeight 	= _propList.get("fontWeight").toString();
			float _padding = (_propList.containsKey("padding"))? Float.valueOf( _propList.get("padding").toString()):3;
			
			float _maxBorderSize = 0;
			if(_propList.containsKey("borderWidths"))
			{
				ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
				
				for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
					if(_maxBorderSize < _borderWidths.get(_bIndex))
					{
						_maxBorderSize = _borderWidths.get(_bIndex);
					}
				}
				_padding = _maxBorderSize + _padding;
			}
			
			float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
			
			
			_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
			_propList.put("fontSize",  _fontSize);
		}
		
		
		
		if( _itemId.equals("") == false )
		{
			_propList.put("TABINDEX_ID", _itemId + "_"+ "_ROW"+rowIndex);
			_propList.put("SUFFIX_ID", "_" + "_ROW"+rowIndex);
		}
		
		
		if(_propList.containsKey("text"))
			_propList.put("tooltip", _propList.get("text").toString());
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if( _barcodeValue == null || _barcodeValue.equals("")) return null;
				else _propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 8 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("upc") && _barcodeData.length() != 11 ){
					_barcodeSrc="";
				}
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") || _className.equals("UBTaximeter")|| _className.equals("UBCandleStickChart")|| _className.equals("UBPlotChart") || _className.equals("UBRadarChart") )
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			if(_className.equals("UBTaximeter")){
				PARAM = getChartParamToJson4Taximeter(currentItemData);	
				PARAM+=","+rowIndex;
			}else if(_className.equals("UBLineChart")){
				PARAM = getChartParamToJson(currentItemData );
			}else if(_className.equals("UBRadarChart")){
				PARAM = getChartParamToJson4Radar(currentItemData );	
			}else if(_className.equals("UBCandleStickChart")){
				PARAM = getChartParamToJson(currentItemData );
			}else if(_className.equals("UBColumnChart")){
				PARAM = getChartParamToJson(currentItemData );	
			}else if(_className.equals("UBBarChart")){
				PARAM = getChartParamToJson(currentItemData );	
			}else if(_className.equals("UBPieChart")){
				PARAM = getChartParamToJson(currentItemData );
			}else{
				PARAM = getChartParamToJson(currentItemData);
			}
			
			
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToJson2(currentItemData );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			else if(_className.equals("UBTaximeter"))
			{
				_propList.put("type" , "TaximeterCtl");
				IMG_TYPE = "taximeter";
			}
			else if(_className.equals("UBRadarChart"))
			{
				_propList.put("type" , "radarChartCtl");
				IMG_TYPE = "radar";
			}
			else if(_className.equals("UBCandleStickChart"))
			{
				_propList.put("type" , "candleChartCtl");
				IMG_TYPE = "candle";
			}
			else if(_className.equals("UBPlotChart"))
			{
				_propList.put("type" , "plotChartCtl");
				IMG_TYPE = "plot";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				String _dataIDs = currentItemData.get("dataSets").getStringValue();				
				String [] arrDataId = _dataIDs.split(",");
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = _dataSet.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else
			{			
				ArrayList<HashMap<String, Object>> _list = _dataSet.get( _dataID );		
				if(!"".equals(IMG_TYPE) && _list != null )
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		else if( "UBStretchLabel".equals(_className) )
		{
			// StretchLabel일때 height계산하여 height를 업데이트하고 
			// text를 줄바꿈 처리하고 진행
			_propList = convertStrechLabel(_propList);
			
		}
		
		// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
		if(_propList.containsKey("text"))
		{
			String _svalue = _propList.get("text").toString();
			_propList.put("tooltip", _svalue);
		}
		
		
		if("UBGraphicsRectangle".equals(_className) || "UBGraphicsCircle".equals(_className) || "UBGraphicsGradiantRectangle".equals(_className))
		{
			_propList.put("angle" , _propList.get("rotation") );
			_propList.put("stroke" , _propList.get("borderColor").toString() );
			_propList.put("strokeWidth" , Integer.valueOf( _propList.get("borderThickness").toString() ) );
			_propList.put("scaleX" , 1);
			_propList.put("scaleY" , 1);
			
			_propList.put("width", Float.valueOf(_propList.get("width").toString()));
			_propList.put("height", Float.valueOf(_propList.get("height").toString()));
			
			if( "UBGraphicsCircle".equals(_className) )
			{
				_propList.put("radius", Float.valueOf( Float.valueOf(_propList.get("width").toString())/2 ));
				_propList.put("scaleY", Float.valueOf( Float.valueOf(_propList.get("height").toString())/Float.valueOf(_propList.get("width").toString()) ));
			}
		}
		
		float _updateX = 0;
		if(  _updateY > -1 )
		{
			_propList.put("y", _updateY);
			_propList.put("top", _updateY);
		}
		else if( _cloneY != 0 )
		{
			_propList.put("y", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
			_propList.put("top", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
		}
		
		if( _cloneX != 0 )
		{
			_updateX = _cloneX + Float.valueOf( _propList.get("x").toString() );
			_propList.put("x", _updateX);
			_propList.put("left", _updateX);
		}
		
		//crossTab을 table로 내보내기 테스트
		if( currentItemData.containsKey("isTable"))
		{
			_propList.put("isTable", currentItemData.get("isTable").getStringValue());
		}
		if( currentItemData.containsKey("TABLE_ID"))
		{
			_propList.put("TABLE_ID", currentItemData.get("TABLE_ID").getStringValue());
		}
		
		if("UBRotateLabel".equals(_className))
		{
			_propList.put("rotate" , _propList.get("rotation") );
		}
		
		if(_propList.containsKey("visible") && _propList.get("visible").equals("false"))
		{
			return null;
		}
		
		_propList.put("className" , _className );
		_propList.put("id" , _itemId );
		
		// radioButtonGroup
		if( _className.equals("UBRadioBorder") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			String _groupName= _propList.get("groupName").toString();
			_propList.put("groupName", _groupName +_propList.get("SUFFIX_ID") );
			
			Boolean _isSelected = radiobuttonHandler(_propList);
			_propList.put("selected", _isSelected);
		}else if( _className.equals("UBRadioButtonGroup") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			
			radiobuttonGroupHandler(_propList);
		}
		
		// 아이템의 사용 여부 확인 
		_propList = ItemPropertyProcess.checkedItemProperties(_propList);
		
		
		return _propList;
	}
	
	public HashMap<String, Object> convertItemDataSimple( BandInfoMapDataSimple _bandInfo, HashMap<String, Object> currentItemData, HashMap<String, 
			ArrayList<HashMap<String, Object>>>  _dataSet, int rowIndex ,HashMap<String, Object> _param, int _startIndex, 
			int _lastIndex , int _totalPageNum , int _currentPage, String _dataName, float _cloneX, float _cloneY, float _updateY ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _className 		= "";
		String _name 		= "";
//		String _value 		= "";
		Object _value 		= null;
		String _dataID 		= "";
		int k = 0;
		
		String _model_type = "";
		String _barcode_type = "";
		String _itemId = "";
		
		Object _dsObj = currentItemData.get("dataSet");
		Object _tempObj;
		
		if(_bandInfo != null)
		{	
			if( _dataName == null || _dataName.equals("") )
			{
				
				if( _dsObj != null ){
					if(  !_dsObj.toString().equals("") ){
						_dataID =  _dsObj.toString();
					}
				}
				
				if( _bandInfo.getDataSet().equals("") == false && _bandInfo.getAdjustableHeight() && _bandInfo.getResizeText() )
				{
					_dataID = _bandInfo.getDataSet();
					
					if( mOriginalDataMap.get( _dataID ) != null && _dsObj != null && _dsObj.toString().equals("null") == false )
					{
						if( mOriginalDataMap.get( _dataID ).equals( _dsObj.toString()) == false )
						{
							_dataID = _dsObj.toString();
						}
					}
				}
				
			}
			else
			{
				if( mOriginalDataMap.get( _dataName ) != null && _dsObj != null && _dsObj.toString().equals("null") == false )
				{
					if( mOriginalDataMap.get( _dataName ).equals(_dsObj.toString()) == false )
					{
						_dataID = _dsObj.toString();
					}
					else
					{
						_dataID = _dataName;	
					}
				}
				else
				{
					_dataID = _dataName;
				}
			}
		}
		else if( _dsObj != null && _dataSet.containsKey(_dsObj.toString() ) )
		{
			_dataID = _dsObj.toString();
		}
		
		_tempObj = currentItemData.get("id");
		if( _tempObj != null )
		{
			_itemId = _tempObj.toString();
		}
		
		HashMap<String, Object> _propList = new HashMap<String, Object>();
		
		_className = currentItemData.get("className").toString();
		// image variable
		String _prefix="";
		String _suffix="";
					
		Boolean _bType = false;

		if(_className.equals("UBLabelBorder"))
		{
			_bType = true;
		}
		else
		{
			_bType = false;
		}
		
		if( _dataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
			

		Set<String> _keySet = currentItemData.keySet();
		Object[] hmKeys = _keySet.toArray();
		
		_propList = convertItemProp( currentItemData, rowIndex, _isMarkAny );
		
		_propList.put("isViewer", true);
		
		if(_propList == null ) return null;
		
		// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
//		if( _propList.containsKey("rowId") )
//		{
//		}
		_propList.put("rowId", rowIndex);
		
		// system function
		String _systemFunction="";
		
		// formatter variables 
		String _formatter="";
		String _nation="";
		String _align="";
		String _dataType="";
		String _dataSetName = "";
		String _columnName = "";
		String _mask="";
		String _inputForamtString = "";
		String _outputFormatString = "";
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		
		// edit formatter variables (e-form)
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";
		
		float _width = 0;
		float _height = 0;
		float _x = 0;
		float _y = 0;
		
		if( _className.toUpperCase().indexOf("LINE") == -1)
		{
			_propList.put("x1", _propList.get("x"));
			_propList.put("y1", _propList.get("y"));
			_propList.put("x2", _propList.get("width"));
			_propList.put("y2", _propList.get("height"));
		}
		
		// Item의 changeData가 있는지 확인
		if(mChangeItemList != null )
		{
			_propList = convertChangeItemDataText( _currentPage ,_propList, "");
		}
		
		// 변수값 셋팅 
		_tempObj = currentItemData.get("type");
		if( _tempObj != null ) _model_type = _tempObj.toString();
		
		_tempObj = currentItemData.get("barcodeType");
		if( _tempObj != null ) _barcode_type = _tempObj.toString();
		
		_tempObj = currentItemData.get("systemFunction");
		if( _tempObj != null ) _systemFunction = _tempObj.toString();
		
		_tempObj = currentItemData.get("editItemFormatter");
		if( _tempObj != null ) _formatterE = _tempObj.toString();
		
		_dataType = UBComponent.getProperties(currentItemData, _className, "dataType", "").toString();
		_dataSetName = UBComponent.getProperties(currentItemData, _className, "dataSet", "").toString();
		_columnName = UBComponent.getProperties(currentItemData, _className, "column", "").toString();
		
		_tempObj = currentItemData.get("prefix");
		if( _tempObj != null )
		{
			try {
				_prefix=URLDecoder.decode(_tempObj.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		_tempObj = currentItemData.get("suffix");
		if( _tempObj != null )
		{
			_suffix=_tempObj.toString();
		}
		
		
		// 보더업데이트
		_tempObj = currentItemData.get("isCell");
		if( _tempObj != null && _tempObj.toString().equals("false") )
		{
			_propList = convertItemToBorder(_propList);
		}
		
		_tempObj = currentItemData.get("ORIGINAL_TABLE_ID");
		if(_tempObj != null)
		{
			_propList.put("ORIGINAL_TABLE_ID", _tempObj.toString() );
		}
		
		_tempObj = currentItemData.get("beforeBorderType");
		if(_tempObj != null)
		{
			_propList.put("beforeBorderType", _tempObj);
		}
		
		_tempObj = currentItemData.get("PRESERVE_ASPECT_RATIO");
		if(_tempObj != null)
		{
			_propList.put("PRESERVE_ASPECT_RATIO", _tempObj.toString());
		}
		
		_x = Float.valueOf( UBComponent.getProperties(_propList, _className, "x").toString() );
		_y = Float.valueOf( UBComponent.getProperties(_propList, _className, "y").toString() );
		_width = Float.valueOf( UBComponent.getProperties(_propList, _className, "width").toString() );
		_height = Float.valueOf( UBComponent.getProperties(_propList, _className, "height").toString() );
		
		HashMap<String, Object> _formatValueMap = null;
		
		_formatter = UBComponent.getProperties( currentItemData, _className, "formatter","").toString();
		
		_tempObj = currentItemData.get("formatValue");
		if(_tempObj != null)
		{
			_formatValueMap = (HashMap<String, Object>) _tempObj;
		}
		
		if( _formatValueMap != null && !_formatValueMap.isEmpty() )
		{
			for(String _key : _formatValueMap.keySet())
			{
				String _formatPropertyName = _key;
				String _formatPropertyValue =_formatValueMap.get(_key).toString();
				
				try {
					_formatPropertyValue =URLDecoder.decode(_formatPropertyValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if(_formatPropertyName.equals("nation"))
				{
					_nation = _formatPropertyValue;
				}
				else if(_formatPropertyName.equals("align"))
				{
					_align=_formatPropertyValue;
				}
				else if( _formatPropertyName.equals("mask") ){
					_mask = _formatPropertyValue;
				}
				else if( _formatPropertyName.equals("decimalPointLength") ){
					if(  _formatPropertyValue.equalsIgnoreCase("NaN") ){
						_decimalPointLength = 0;
					}else{
						_decimalPointLength = common.ParseIntNullChk(_formatPropertyValue, 0);	
					}
				}				
				else if( _formatPropertyName.equals("useThousandComma") ){
					_useThousandComma = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("isDecimal") ){
					_isDecimal = Boolean.parseBoolean(_formatPropertyValue);
				}		
				else if( _formatPropertyName.equals("formatString") ){
					_formatString = _formatPropertyValue;
				}
				else if( _formatPropertyName.equals("inputFormatString") )
				{
					_inputForamtString = URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
				else if( _formatPropertyName.equals("outputFormatString") )
				{
					_outputFormatString =  URLDecoder.decode(_formatPropertyValue , "UTF-8");
				}
			}
		}
		
		// 에디트 포맷터 값이존재할경우각각의 데이터를 담는 처리
		// kyh formatter property setting
		HashMap<String, Object> _editFormatValueMap = null;
		_tempObj = currentItemData.get("editItemFormatValue");
		
		if(_tempObj!=null)
		{
			_editFormatValueMap = (HashMap<String, Object>) currentItemData.get("editItemFormatValue");
		}
		
		if( _editFormatValueMap != null && !_editFormatValueMap.isEmpty() )
		{	
			String _eformatDataset=null;
			String _eformatKeyField=null;
			String _eformatLabelField=null;
			
			for( String _key : _editFormatValueMap.keySet() ){
				
				String _propName = _key;
				String _propValue = _editFormatValueMap.get(_key).toString();
				
				if( _propName.equals("nation") ){
					_nationE = URLDecoder.decode(_propValue, "UTF-8");
					_propList.put("eformnation", 	_nationE );
				}
				else if( _propName.equals("mask") ){
					_maskE = URLDecoder.decode(_propValue, "UTF-8");
					_propList.put("eformmask", 	_maskE );
				}
				else if( _propName.equals("decimalPointLength") ){
					_decimalPointLengthE = common.ParseIntNullChk(_propValue, 0);
					
					_propList.put("eformdecimalPointLength", 	_decimalPointLengthE );
					
				}				
				else if( _propName.equals("useThousandComma") ){
					_useThousandCommaE = Boolean.parseBoolean(_propValue);
					
					_propList.put("eformuseThousandComma", 	_useThousandCommaE );
				}		
				else if( _propName.equals("isDecimal") ){
					_isDecimalE = Boolean.parseBoolean(_propValue);
					_propList.put("eformisDecimal", _isDecimalE	 );
				}		
				else if( _propName.equals("formatString") ){
					_formatStringE = _propValue;
					_propList.put("eformformatString", _formatStringE	 );
				}else if( _propName.equals("dataProvider") ){
					_propValue = URLDecoder.decode(_propValue, "UTF-8");
					_propList.put("eformDataProvider", _propValue	 );
				}else if( _propName.equals("dataset") ){
					_eformatDataset = _propValue;
				}else if( _propName.equals("keyField") ){
					_eformatKeyField = _propValue;
				}else if( _propName.equals("valueField") ){
					_eformatLabelField = _propValue;
				}
			}
			
			if( _formatterE.equals("SelectMenu") ){
				
				// dataset으로 comboBox 표현.
				if( _eformatDataset != null &&  (!_eformatDataset.equals("null")) && _dataSet.containsKey(_eformatDataset) ){
					
					String _efText = _propList.get("text").toString();
					Boolean _hasValueKey = false;
					
					ArrayList<HashMap<String, Object>> _list = _dataSet.get(_eformatDataset);
					
					HashMap<String, Object> _dataHm;
					Object _keyData;
					Object _labelData;
					
					JSONArray ja = new JSONArray();
					String _jsonStr=null;
					JSONObject jo;
					String _keyStr=null;
					String _labelStr=null;
					
					for( int _eformatIdx=0; _eformatIdx<_list.size(); _eformatIdx++ ){
						_dataHm = _list.get(_eformatIdx);
						_keyData = _dataHm.get(_eformatKeyField);
						_labelData = _dataHm.get(_eformatLabelField);
						_keyStr=_keyData.toString();
						_labelStr = _labelData.toString();
						
						if( _efText.equals(_keyStr) && _hasValueKey == false ){
							_hasValueKey = true;
							_propList.put("text", _labelStr );
						}
						
						jo = new JSONObject();
						jo.put("label", _labelStr);
						jo.put("value",_keyStr );
						ja.add(jo);
					}
					
					_jsonStr = ja.toJSONString();
					
					_propList.put("eformDataProvider", _jsonStr	 );
				}else{
					
					if( _propList.containsKey("eformDataProvider") == false && _propList.get("eformDataProvider") == null )
					{
						_propList.put("eformDataProvider", "[]");
					}
					
					String _jsonStr = _propList.get("eformDataProvider").toString(); 
					JSONParser jsonParser = new JSONParser();
					try {
						
						JSONArray ja = (JSONArray) jsonParser.parse(_jsonStr);
						
						String _efText = _propList.get("text").toString();
						JSONObject oj;
						for( int jsonIdx=0; jsonIdx<ja.size(); jsonIdx++ ){
							oj=(JSONObject) ja.get(jsonIdx);
							if( oj.get("value").equals(_efText) ){
								_propList.put("text", oj.get("label") );
								break;
							}
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}

		
		//Table의 UBFX가 존재할경우 처리( Table의 ubfx를 먼저 처리후 Cell의 ubfx를 처리 )
		if( currentItemData.containsKey("ubfx") || currentItemData.containsKey("tableUbfunction") )
		{
			ArrayList<HashMap<String, String>> _ubfxs = new ArrayList<HashMap<String, String>>();
			
			ArrayList<ArrayList<HashMap<String, String>>> _ubfxsList = new ArrayList<ArrayList<HashMap<String, String>>>();
			
			int _ubfxListCnt = 0;
			
			_tempObj = currentItemData.get("tableUbfunction");
			if( _tempObj != null  )
			{
				ArrayList<HashMap<String, String>> _tblUbfxs = (ArrayList<HashMap<String, String>>) _tempObj;
				_ubfxsList.add( _tblUbfxs );
			}
			
			_tempObj = currentItemData.get("ubfx");
			if( _tempObj != null )
			{
				_ubfxs = (ArrayList<HashMap<String, String>>) _tempObj;
				_ubfxsList.add( _ubfxs );
			}
			
			int _nodeCnts = _ubfxsList.size();
			
			for(int _ubfxListIndex= 0; _ubfxListIndex < _nodeCnts; _ubfxListIndex++)
			{
				ArrayList<HashMap<String, String>> _selectUbfxList = _ubfxsList.get(_ubfxListIndex);
				for(int _ubfxIndex = 0; _ubfxIndex < _selectUbfxList.size(); _ubfxIndex++)
				{
					HashMap<String, String> _ubfxItem = _selectUbfxList.get(_ubfxIndex);
					String _ubfxProperty = _ubfxItem.get("property");
					String _ubfxValue = _ubfxItem.get("value");
					try {
						_ubfxValue = URLDecoder.decode(_ubfxValue, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					ArrayList<HashMap<String, Object>> _pList = _dataSet.get(_dataID);
					String _datasetColumnName = _columnName;
					mFunction.setDatasetList(_dataSet);
					mFunction.setParam(_param);
					
					mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
					if(mGroupTotalPageIndex>0) mFunction.setSectionCurrentPageNum(mGroupCurrentPageIndex);
					mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
					if(mGroupTotalPageIndex>0) mFunction.setSectionTotalPageNum(mGroupTotalPageIndex);
					mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
					mFunction.setOriginalDataMap(mOriginalDataMap);
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,_dataName);
					}else{
						_fnValue = mFunction.function(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,_dataName);
					}
					
					_fnValue = _fnValue.trim();
					
					if( _ubfxProperty.equals("text") || _fnValue.equals("") == false)
					{
						_propList = convertUbfxStyle(_ubfxProperty, _fnValue, _propList );
					}
				}
			}
			
		}
		
		//hyperLinkedParam처리
		_tempObj = currentItemData.get("ubHyperLinkType");
		if( _tempObj != null && "2".equals( _tempObj.toString() )  )
		{
			ArrayList<HashMap<String, String>> _hyperLinkedParam = (ArrayList<HashMap<String, String>>) currentItemData.get("ubHyperLinkParm");
			HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
			
			if( _hyperLinkedParam != null && _hyperLinkedParam.size() > 0 )
			{
				int _hyperLinkedParamSize = _hyperLinkedParam.size();
				
				HashMap<String, String> _hMap;
				
				for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
				{
					_hMap = _hyperLinkedParam.get(_hyperIdx);
					String _hyperParamKey = "";
					String _hyperParamValue = "";
					String _hyperParamType = "";
					
					if( _hMap.containsKey("id") )
					{
						_hyperParamKey = _hMap.get("id").toString();
					}
					if( _hMap.containsKey("value") )
					{
						_hyperParamValue = _hMap.get("value").toString();
					}
					if( _hMap.containsKey("type") )
					{
						_hyperParamType = _hMap.get("type").toString();
					}
					
					if( "DataSet".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						
						_hyperParamValue = "";
						
						if(_dataSet.containsKey(_hyperLinkedDataSetId))
						{
							ArrayList<HashMap<String, Object>> _list = _dataSet.get( _hyperLinkedDataSetId );
							Object _dataValue = "";
							if( _list != null ){
								if( rowIndex < _list.size() )
								{
									HashMap<String, Object> _dataHm = _list.get(rowIndex);
									_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
								}
							}
						}
					}
					else if("Parameter".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_hyperParamValue = "";
						}
						else
						{
							_hyperParamValue = _pValue;
						}
					}
					
					_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
				}
				
				_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
			}
			
		}
		
		/***/
		if( !_dataType.equals("") && !_dataType.equals("0") )	
		{
			String columnStr = _columnName;
			String dataTypeStr = _dataType;
			
			_tempObj = currentItemData.get("dataType_N");
			if( _tempObj != null && _tempObj.toString().equals("") == false )
			{
				dataTypeStr = _tempObj.toString();
			}
			_tempObj = currentItemData.get("column_N");
			if( _tempObj != null && _tempObj.toString().equals("") == false )
			{
				columnStr = _tempObj.toString();
			}
			
			if( dataTypeStr.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list = _dataSet.get( _dataID );
				Object _dataValue = "";
				if( _list != null ){
					if( rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						_dataValue = _dataHm.get( columnStr );
					}
				}
				
				// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐.. 
				if(_className.equals("UBSVGArea") ){
					
					if(_dataValue==null || _dataValue.toString().equals("")) return null;
					
					String _tmpDataValue = _dataValue.toString();
					
					if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
					{
						_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
					}
					
					boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
					boolean _preserveAspectRatio = UBComponent.getProperties(_propList, _className, "preserveAspectRatio", "").toString().equals("true");
					boolean _fixedToSize = UBComponent.getProperties(_propList, _className, "fixedToSize", "").toString().equals("true");
					
					if(_bSVG)
					{
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
					}
					else
					{
						boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
						if(!_bHasHtmlTag)
							_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
							
						_tmpDataValue =  convertHtmlToSvgTextSimple( _tmpDataValue, _propList );
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							
					}
					log.debug("13168-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _dataValue.toString() + "]");
					
					_dataValue = _dataValue.toString().replace(" ", "%20");
					
					if( !_dataValue.toString().equals("") )
					{
						_propList.put("data",  URLEncoder.encode(_dataValue.toString(), "UTF-8"));
					}
					else
					{
						return null;
					}
					
					
				}
				else if( _className.equals("UBSVGRichText") )
				{
					// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
					if( _dataValue != null && _dataValue.toString().equals("") == false )
					{
						_propList = convertUBSvgItemSimple(_dataValue,_propList);
						
						if(_propList == null ) return null;
					}
				}
				
				
				else if("UBCheckBox".equals(_className) )
				{
					String _selectedText= UBComponent.getProperties(_propList, _className, "selectedText", "").toString();
					//String _deSelectedText=_propList.get("deSelectedText").toString();
					if( _dataValue != null && _selectedText.equalsIgnoreCase(_dataValue.toString()) ){
						_propList.put("selected", "true");
					}else{
						_propList.put("selected", "false");	
					}
					
				}	
				else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") )
				{
					String _servicesUrl = convertImageData( _dataValue.toString() , _prefix, _suffix, Float.valueOf(_width).intValue(), Float.valueOf(_height).intValue() );
					_propList.put("src", _servicesUrl);
				}
				else
				{
					_propList.put("text", _dataValue == null ? "" : _dataValue);
					
					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					_propList.put("tooltip", _propList.get("text"));
				}
				
			}
			else if( dataTypeStr.equals("2"))
			{
				// rowIndex : 현재 Row Index
				// dataSet : 그룹핑된 데이터셋
				if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
					
					ArrayList<HashMap<String, Object>> _pList = _dataSet.get( _dataID );
					String _datasetColumnName = columnStr;
					mFunction.setDatasetList(_dataSet);
					mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
					mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
					mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
					mFunction.setOriginalDataMap(mOriginalDataMap);
					
					
					String _fnValue;
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_systemFunction , rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName);
					}else{
						_fnValue = mFunction.function(_systemFunction,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName );
					}
					

					if( _className.equals("UBSVGRichText") )
					{
						if( _fnValue != null && _fnValue.equals("") == false )
						{
							// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
							_propList = convertUBSvgItemSimple(_fnValue,_propList);
							
							if(_propList == null ) return null;
						}

					}
					else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
//						_fnValue = URLDecoder.decode(_fnValue, "UTF-8");
						
						String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
						_propList.put("src", _servicesUrl);
						
					}
					else if(_className.equals("UBCheckBox") ) {
						_propList.put("selected", _fnValue);
					}
					else
					{
						_propList.put("text", _fnValue == null ? "" : _fnValue);
					}
				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			else if( dataTypeStr.equals("3"))
			{
				String _txt = _propList.get("text").toString();
				
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				String _fnValue = "";
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					if( _inOf != 0 ){
						
						// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
						if(_className.equals("UBSVGArea")  ){
							
							String _tmpDataValue = String.valueOf(_txt);
							
							if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
							{
								_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
							}
							
							boolean _bSVG = (_tmpDataValue != null && ( _tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
							boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )? _propList.get("preserveAspectRatio").toString().equals("true"):false;
							boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
							
							String _svgTag = null;
							if(_bSVG)
							{
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							}
							else
							{
								boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
								if(!_bHasHtmlTag)
									_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
									
								_tmpDataValue =  convertHtmlToSvgTextSimple( _tmpDataValue, _propList );
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
									
							}
													
							log.debug("13315-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _svgTag + "]");
							
							_svgTag = _svgTag.toString().replace(" ", "%20");
							if( !_svgTag.equals("") )
							{
								_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8"));
							}
							else
							{
								return null;
							}
							
							_txt = "";
						}
						else if( _className.equals("UBSVGRichText") )
						{
							if( _txt != null && _txt.equals("") == false )
							{
								_propList = convertUBSvgItemSimple( _txt, _propList);
								
								if(_propList == null ) return null;
							}
							
							_txt = "";
						}
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = _txt;
						}else{
							_fnValue = mFunction.function(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName );
						}
						
					}else{

						int _keyIndex=_txt.lastIndexOf("}");
						_pKey = _txt.substring(_inOf + 7 , _keyIndex);

						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						_fnValue = _pList.get("parameter");

					}
					
					if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
						_fnValue = URLDecoder.decode(_fnValue.replace(" ", "%20"), "UTF-8");
						
						String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
						_propList.put("src", _servicesUrl);
					}
					else
					{
						if( _fnValue.equals("undefined"))
						{
							_propList.put("text", "");
						}
						else
						{
							_propList.put("text", _fnValue);
						}
					}
					

				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			
		}
		
		// formatter set	// ITem의 Formatter 처리 ( export처리를 위하여 속성값 담기 )
		if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
			Object _propValue;
			String _formatValue="";
			_propValue=_propList.get("text");
			_formatValue = _propValue.toString();
			String _excelFormatterStr = "";
			try {
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					_excelFormatterStr = _nation  + "§" + _align;
					
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					_excelFormatterStr = _formatString;
					
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					_excelFormatterStr = _mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal;
					
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
                else if( _formatter.equalsIgnoreCase("CustomDate") )
				{
					_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
					_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
					
					_propList.put("inputFormatString", _inputForamtString);
					_propList.put("outputFormatString", _outputFormatString);
				}
				
			} catch (ParseException e) {
				//e.printStackTrace();
			}
			
			if( isExportType.equals("EXCEL") && _excelFormatterStr.equals("") == false && common.getPropertyValue("excelExport.useFormatter") != null && common.getPropertyValue("excelExport.useFormatter").equals("true") ) 
			{
				_propList.put("EX_FORMATTER", _formatter);
				_propList.put("EX_FORMAT_DATA_STR", _excelFormatterStr);
				_propList.put("EX_FORMAT_ORIGINAL_STR", _propValue.toString() );
			}
			
			_propList.put("text", _formatValue);
			
			// format이 label band에서 안들어간다.
			_propList.put("formatter", _formatter);
			_propList.put("mask", _mask);
			_propList.put("decimalPointLength", _decimalPointLength);
			_propList.put("useThousandComma", _useThousandComma);
			_propList.put("isDecimal", _isDecimal);
			_propList.put("formatString", _formatString);
			_propList.put("nation", _nation);
			_propList.put("currencyAlign", _align);
			_propList.put("inputFormatString", _inputForamtString);
			_propList.put("outputFormatString", _outputFormatString);
			
		}
		
		
		//ResizeFont 값이 true이고 adjustableHeight값이 true 일경우 처리 
		if( _propList.containsKey("text") &&  "".equals(_propList.get("text").toString()) == false && currentItemData.containsKey("resizeFont") && currentItemData.get("resizeFont").toString().equals("true") )
		{
			float _fontSize 	= Float.valueOf( UBComponent.getProperties(_propList, _className, "fontSize", "9").toString() );
			String _fontFamily 	= _propList.get("fontFamily").toString();
			String _fontWeight 	= UBComponent.getProperties(_propList, _className, "fontWeight", "normal").toString();
			float _padding = (_propList.containsKey("padding"))? Float.valueOf( UBComponent.getProperties(_propList, _className, "padding", "3").toString() ):3;
			
			float _maxBorderSize = 0;
			if(_propList.containsKey("borderWidths"))
			{
				ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
				
				for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
					if(_maxBorderSize < _borderWidths.get(_bIndex))
					{
						_maxBorderSize = _borderWidths.get(_bIndex);
					}
				}
				_padding = _maxBorderSize + _padding;
			}
			
			float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
			
			
			_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
			_propList.put("fontSize",  _fontSize);
		}
		
		
		
		if( _itemId.equals("") == false )
		{
			_propList.put("TABINDEX_ID", _itemId + "_"+ "_ROW"+rowIndex);
			_propList.put("SUFFIX_ID", "_" + "_ROW"+rowIndex);
		}
		
		
		if(_propList.containsKey("text"))
			_propList.put("tooltip", _propList.get("text").toString());
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if( _barcodeValue == null || _barcodeValue.equals("")) return null;
				else _propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf(_propList.get("showLabel").toString()) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 8 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("upc") && _barcodeData.length() != 11 ){
					_barcodeSrc="";
				}
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						
//						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
						_barcodeSrc= UBComponent.getProperties(_propList, _className, "src", "") + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") || _className.equals("UBTaximeter")|| _className.equals("UBCandleStickChart")|| _className.equals("UBPlotChart") || _className.equals("UBRadarChart") )
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			if(_className.equals("UBTaximeter")){
				PARAM = getChartParamToSimple4Taximeter(currentItemData);	
				PARAM+=","+rowIndex;
			}else if(_className.equals("UBLineChart")){
				PARAM = getChartParamToSimple(currentItemData );
			}else if(_className.equals("UBRadarChart")){
				PARAM = getChartParamToSimple4Radar(currentItemData );	
			}else if(_className.equals("UBCandleStickChart")){
				PARAM = getChartParamToSimple(currentItemData );
			}else if(_className.equals("UBColumnChart")){
				PARAM = getChartParamToSimple(currentItemData );	
			}else if(_className.equals("UBBarChart")){
				PARAM = getChartParamToSimple(currentItemData );	
			}else if(_className.equals("UBPieChart")){
				PARAM = getChartParamToSimple(currentItemData );
			}else{
				PARAM = getChartParamToSimple(currentItemData);
			}
			
			
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToSimple2(currentItemData );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			else if(_className.equals("UBTaximeter"))
			{
				_propList.put("type" , "TaximeterCtl");
				IMG_TYPE = "taximeter";
			}
			else if(_className.equals("UBRadarChart"))
			{
				_propList.put("type" , "radarChartCtl");
				IMG_TYPE = "radar";
			}
			else if(_className.equals("UBCandleStickChart"))
			{
				_propList.put("type" , "candleChartCtl");
				IMG_TYPE = "candle";
			}
			else if(_className.equals("UBPlotChart"))
			{
				_propList.put("type" , "plotChartCtl");
				IMG_TYPE = "plot";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				String _dataIDs = currentItemData.get("dataSets").toString();				
				String [] arrDataId = _dataIDs.split(",");
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = _dataSet.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else
			{			
				ArrayList<HashMap<String, Object>> _list = _dataSet.get( _dataID );		
				if(!"".equals(IMG_TYPE) && _list != null )
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		else if( "UBStretchLabel".equals(_className) )
		{
			// StretchLabel일때 height계산하여 height를 업데이트하고 
			// text를 줄바꿈 처리하고 진행
			_propList = convertStrechLabel(_propList);
			
		}
		
		// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
		if(_propList.containsKey("text"))
		{
			String _svalue = _propList.get("text").toString();
			_propList.put("tooltip", _svalue);
		}
		
		
		if("UBGraphicsRectangle".equals(_className) || "UBGraphicsCircle".equals(_className) || "UBGraphicsGradiantRectangle".equals(_className))
		{
			_propList.put("angle" , UBComponent.getProperties(_propList, _className, "rotation", "0").toString() );
			_propList.put("stroke" ,  UBComponent.getProperties(_propList, _className, "borderColor", "0").toString() );
			_propList.put("strokeWidth" , Integer.valueOf( UBComponent.getProperties(_propList, _className, "borderThickness", "0").toString() ) );
			_propList.put("scaleX" , 1);
			_propList.put("scaleY" , 1);
			
			_propList.put("width", Float.valueOf(_propList.get("width").toString()));
			_propList.put("height", Float.valueOf(_propList.get("height").toString()));
			
			if( "UBGraphicsCircle".equals(_className) )
			{
				_propList.put("radius", Float.valueOf( Float.valueOf(_propList.get("width").toString())/2 ));
				_propList.put("scaleY", Float.valueOf( Float.valueOf(_propList.get("height").toString())/Float.valueOf(_propList.get("width").toString()) ));
			}
		}
		
		float _updateX = 0;
		if(  _updateY > -1 )
		{
			_propList.put("y", _updateY);
			_propList.put("top", _updateY);
		}
		else if( _cloneY != 0 )
		{
			_propList.put("y", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
			_propList.put("top", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
		}
		
		if( _cloneX != 0 )
		{
			_updateX = _cloneX + Float.valueOf( _propList.get("x").toString() );
			_propList.put("x", _updateX);
			_propList.put("left", _updateX);
		}
		
		//crossTab을 table로 내보내기 테스트
		_tempObj = currentItemData.get("isTable");
		if( _tempObj != null )
		{
			_propList.put("isTable", _tempObj.toString());
		}
		_tempObj = currentItemData.get("TABLE_ID");
		if( _tempObj != null )
		{
			_propList.put("TABLE_ID", _tempObj.toString());
		}
		
		if("UBRotateLabel".equals(_className))
		{
			_propList.put("rotate" , _propList.get("rotation") );
		}
		_tempObj = currentItemData.get("visible");
		if(_tempObj != null && _tempObj.toString().equals("false"))
		{
			return null;
		}
		
		_propList.put("className" , _className );
		_propList.put("id" , _itemId );
		
		// radioButtonGroup
		if( _className.equals("UBRadioBorder") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			String _groupName= _propList.get("groupName").toString();
			_propList.put("groupName", _groupName +_propList.get("SUFFIX_ID") );
			
			Boolean _isSelected = radiobuttonHandler(_propList);
			_propList.put("selected", _isSelected);
		}else if( _className.equals("UBRadioButtonGroup") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			
			radiobuttonGroupHandler(_propList);
		}
		
		// 아이템의 사용 여부 확인 
		_propList = ItemPropertyProcess.checkedItemProperties(_propList);
		
		return _propList;
	}
	
	
	public static String getChartParamToJson( HashMap<String, Value> _chartItem )
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";
		String yFieldsValueWeightStr = "";
		
		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		ArrayList<String> chartList = null;
		
		String _className = _chartItem.get("className").getStringValue();
		
		if(_className.equals("UBLineChart")){
			chartList = getLineChartPropertys();
		}else if(_className.equals("UBCandleStickChart")){
			chartList = getCandleChartPropertys();
		}else if(_className.equals("UBColumnChart")){
			chartList = getColumnChartPropertys();	
		}else if(_className.equals("UBBarChart")){
			chartList = getColumnChartPropertys();	
		}else if(_className.equals("UBPieChart")){
			chartList = getPieChartPropertys();
		}else{
			 chartList = getChartPropertys();
		}
		
		try {
			
//			ArrayList<HashMap<String, String>> _displayList = (ArrayList<HashMap<String, String>>) _chartItem.get("displayName").getObjectValue();
//			
//			_max = _displayList.size();
			
			
			ArrayList<HashMap<String, String>> _displayList=null;
			
			if( _chartItem.containsKey("displayName") ){
				_displayList = (ArrayList<HashMap<String, String>>) _chartItem.get("displayName").getObjectValue();
				_max = _displayList.size();
			}else if( _chartItem.containsKey("displayNames") ){
				JSONArray _displayListJsonArray = (JSONArray) _chartItem.get("displayNames").getObjectValue();
				JSONArray _displayListJsonArraySub = (JSONArray) _displayListJsonArray.get(0);
				_displayList=(ArrayList<HashMap<String, String>>)_displayListJsonArray.get(0);
				_max = _displayListJsonArraySub.size();
			}
			
			
			for ( i = 0; i < _max; i++) {
				
				displayMap = _displayList.get(i);
				
				if(displayMap.containsKey("visible") && displayMap.get("visible").equals("true") )
				{
					if( yFieldsStr.equals("") == false )
					{
						yFieldsStr = yFieldsStr + "~";
					}
					if( yFieldsDisplayStr.equals("") == false )
					{
						yFieldsDisplayStr = yFieldsDisplayStr + "~";
					}
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr = yFieldsColorStr + "~";
					}
					if( yFieldsValueWeightStr.equals("") == false )
					{
						yFieldsValueWeightStr = yFieldsValueWeightStr + "~";
					}
					
					yFieldsStr = yFieldsStr + URLDecoder.decode( displayMap.get("column"),"UTF-8");
					yFieldsDisplayStr = yFieldsDisplayStr + URLDecoder.decode( displayMap.get("text"),"UTF-8");
					yFieldsColorStr = yFieldsColorStr + URLDecoder.decode( displayMap.get("color"),"UTF-8");
					yFieldsValueWeightStr = yFieldsValueWeightStr + displayMap.get("valueWeight");
				}
				
			}
			
			_max = chartList.size();
			for ( i = 0; i < _max; i++) {
				if( returnString.equals("") == false ) returnString = returnString + ",";
				if( chartList.get(i).equals("yFieldName") )
				{
					returnString = returnString + yFieldsStr;
				}
				else if( chartList.get(i).equals("yFieldDisplayName") )
				{
					returnString = returnString + yFieldsDisplayStr;
				}
				else if( chartList.get(i).equals("yFieldFillColor") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendLabelPlacement") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendMarkHeight") )
				{
					returnString = returnString + _legendMarkHeight;
				}
				else if( chartList.get(i).equals("legendMarkWidth") )
				{
					returnString = returnString + _legendMarkWeight;
				}
				else if( chartList.get(i).equals("valueWeight") )
				{
					returnString = returnString + yFieldsValueWeightStr;
				}
				else if( _chartItem.containsKey(chartList.get(i)))
				{
					returnString = returnString + _chartItem.get(chartList.get(i)).getStringValue();;
				}
				else
				{
					returnString = returnString + "";
				}
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnString;
	}

	public static String getChartParamToSimple( HashMap<String, Object> _chartItem )
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";
		String yFieldsValueWeightStr = "";
		
		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		ArrayList<String> chartList = null;
		
		String _className = _chartItem.get("className").toString();
		
		if(_className.equals("UBLineChart")){
			chartList = getLineChartPropertys();
		}else if(_className.equals("UBCandleStickChart")){
			chartList = getCandleChartPropertys();
		}else if(_className.equals("UBColumnChart")){
			chartList = getColumnChartPropertys();	
		}else if(_className.equals("UBBarChart")){
			chartList = getColumnChartPropertys();	
		}else if(_className.equals("UBPieChart")){
			chartList = getPieChartPropertys();
		}else{
			chartList = getChartPropertys();
		}
		
		try {
			
//			ArrayList<HashMap<String, String>> _displayList = (ArrayList<HashMap<String, String>>) _chartItem.get("displayName").getObjectValue();
//			
//			_max = _displayList.size();
			
			
			ArrayList<HashMap<String, String>> _displayList=null;
			
			if( _chartItem.containsKey("displayName") ){
				_displayList = (ArrayList<HashMap<String, String>>) _chartItem.get("displayName");
				_max = _displayList.size();
			}else if( _chartItem.containsKey("displayNames") ){
				JSONArray _displayListJsonArray = (JSONArray) _chartItem.get("displayNames");
				JSONArray _displayListJsonArraySub = (JSONArray) _displayListJsonArray.get(0);
				_displayList=(ArrayList<HashMap<String, String>>)_displayListJsonArray.get(0);
				_max = _displayListJsonArraySub.size();
			}
			
			
			for ( i = 0; i < _max; i++) {
				
				displayMap = _displayList.get(i);
				
				if(displayMap.containsKey("visible") && displayMap.get("visible").equals("true") )
				{
					if( yFieldsStr.equals("") == false )
					{
						yFieldsStr = yFieldsStr + "~";
					}
					if( yFieldsDisplayStr.equals("") == false )
					{
						yFieldsDisplayStr = yFieldsDisplayStr + "~";
					}
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr = yFieldsColorStr + "~";
					}
					if( yFieldsValueWeightStr.equals("") == false )
					{
						yFieldsValueWeightStr = yFieldsValueWeightStr + "~";
					}
					
					yFieldsStr = yFieldsStr + URLDecoder.decode( displayMap.get("column"),"UTF-8");
					yFieldsDisplayStr = yFieldsDisplayStr + URLDecoder.decode( displayMap.get("text"),"UTF-8");
					yFieldsColorStr = yFieldsColorStr + URLDecoder.decode( displayMap.get("color"),"UTF-8");
					yFieldsValueWeightStr = yFieldsValueWeightStr + displayMap.get("valueWeight");
				}
				
			}
			
			_max = chartList.size();
			for ( i = 0; i < _max; i++) {
				if( returnString.equals("") == false ) returnString = returnString + ",";
				if( chartList.get(i).equals("yFieldName") )
				{
					returnString = returnString + yFieldsStr;
				}
				else if( chartList.get(i).equals("yFieldDisplayName") )
				{
					returnString = returnString + yFieldsDisplayStr;
				}
				else if( chartList.get(i).equals("yFieldFillColor") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendLabelPlacement") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				else if( chartList.get(i).equals("legendMarkHeight") )
				{
					returnString = returnString + _legendMarkHeight;
				}
				else if( chartList.get(i).equals("legendMarkWidth") )
				{
					returnString = returnString + _legendMarkWeight;
				}
				else if( chartList.get(i).equals("valueWeight") )
				{
					returnString = returnString + yFieldsValueWeightStr;
				}
				else if( _chartItem.containsKey(chartList.get(i)))
				{
					returnString = returnString + _chartItem.get(chartList.get(i)).toString();
				}
				else
				{
					returnString = returnString + "";
				}
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnString;
	}
	
	public static String getChartParamToJson4Taximeter( HashMap<String, Value> _childItem)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		HashMap<String, String> chartData = new HashMap<String, String>();
		String returnString = "";
		String[] _chartProperties=  {"dataSet","column","interval","minimumRange","maximumRange","chartAngle","viewType"};
		String _value;
		
		try {
			
			int _chartPropertiesLength = _chartProperties.length;
			String _prop;
			
			for ( i = 0; i < _chartPropertiesLength; i++) {
				
				_prop = _chartProperties[i];
					
				if( returnString.equals("") == false ) returnString = returnString + ",";
				
				_value = "";
				if(_childItem.containsKey(_prop))
				{
					_value = URLDecoder.decode(_childItem.get(_prop).getStringValue() , "UTF-8");
				}
				
				returnString = returnString +_value;
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnString;
	}
	
	public static String getChartParamToSimple4Taximeter( HashMap<String, Object> _childItem )
	{
		// Chart의 데이터값을 추출
		int i = 0;
		HashMap<String, String> chartData = new HashMap<String, String>();
		String returnString = "";
		String[] _chartProperties=  {"dataSet","column","interval","minimumRange","maximumRange","chartAngle","viewType"};
		String _value;
		String _className = _childItem.get("className").toString();
		try {
			
			int _chartPropertiesLength = _chartProperties.length;
			String _prop;
			
			for ( i = 0; i < _chartPropertiesLength; i++) {
				
				_prop = _chartProperties[i];
					
				if( returnString.equals("") == false ) returnString = returnString + ",";
				
				_value = UBComponent.getProperties(_childItem, _className, _prop, "").toString();
				if( !_value.equals("") )
				{
					_value = URLDecoder.decode( _value , "UTF-8");
				}
				
				returnString = returnString +_value;
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnString;
	}
	
	
	public static HashMap<Integer, String> getChartParamToJson2( HashMap<String, Value> _chartItem )
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		ArrayList<String> chartList = getChartPropertys4CombinedColumn();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		
		HashMap<Integer, String> displayNamesMap = new HashMap<Integer, String>();
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";

		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		try {
			String _key = "";
			int max = chartList.size();
			for ( i = 0; i < max; i++) {
				
				_key = chartList.get(i);
				
				if( _chartItem.containsKey(_key)  )
				{
					if(_key.equalsIgnoreCase("seriesXFields") )
					{
						String _xfieldsValue= URLDecoder.decode( _chartItem.get("value").getStringValue() , "UTF-8");
						_xfieldsValue=_xfieldsValue.replaceAll("," , "~" );
						chartData.put( _key , _xfieldsValue  );	
					}
					else
					{
						//chartData.put( _key, URLDecoder.decode(_chartItem.get("value").getStringValue() , "UTF-8")  );						
						chartData.put( _key, URLDecoder.decode(_chartItem.get(_key).getStringValue() , "UTF-8")  );		
					}
				}
			}
			
			
			ArrayList<ArrayList<HashMap<String, String>>> _displayList = (ArrayList<ArrayList<HashMap<String, String>>>) _chartItem.get("displayNames").getObjectValue();
			ArrayList<HashMap<String, String>> list;
			HashMap<String, String> _displayItem;
			
			int _max_displayNames = _displayList.size();
			
			for( int dispIndex=0; dispIndex<_max_displayNames;  dispIndex++ ){
				
				returnString="";
				yFieldsStr="";
				yFieldsDisplayStr="";
				yFieldsColorStr="";
				
				list = _displayList.get(dispIndex);
				
				_max = list.size();
				
				for ( i = 0; i < _max; i++) {
					
					displayMap.clear();
					_displayItem = list.get(i);
					
					if(_displayItem.containsKey("visible") && _displayItem.get("visible").equals("true") )
					{
						if( yFieldsStr.equals("") == false )
						{
							yFieldsStr = yFieldsStr + "~";
						}
						if( yFieldsDisplayStr.equals("") == false )
						{
							yFieldsDisplayStr = yFieldsDisplayStr + "~";
						}
						if( yFieldsColorStr.equals("") == false )
						{
							yFieldsColorStr = yFieldsColorStr + "~";
						}
						
						yFieldsStr = yFieldsStr + URLDecoder.decode( _displayItem.get("column"),"UTF-8");
						yFieldsDisplayStr = yFieldsDisplayStr + URLDecoder.decode( _displayItem.get("text"),"UTF-8");
						yFieldsColorStr = yFieldsColorStr + URLDecoder.decode( _displayItem.get("color"),"UTF-8");
					}
					
				}
				
				_max = chartList.size();
				for ( i = 0; i < _max; i++) {
					if( returnString.equals("") == false ) returnString = returnString + ",";
					if( chartList.get(i).equals("yFieldName") )
					{
						returnString = returnString + yFieldsStr;
					}
					else if( chartList.get(i).equals("yFieldDisplayName") )
					{
						returnString = returnString + yFieldsDisplayStr;
					}
					else if( chartList.get(i).equals("yFieldFillColor") )
					{
						returnString = returnString + yFieldsColorStr;
					}
					else if( chartList.get(i).equals("legendLabelPlacement") )
					{
						returnString = returnString + yFieldsColorStr;
					}
					else if( chartList.get(i).equals("legendMarkHeight") )
					{
						returnString = returnString + _legendMarkHeight;
					}
					else if( chartList.get(i).equals("legendMarkWidth") )
					{
						returnString = returnString + _legendMarkWeight;
					}
					else if( chartData.containsKey(chartList.get(i)))
					{
						returnString = returnString + chartData.get(chartList.get(i));
					}
					else
					{
						returnString = returnString + "";
					}
				}
				
				displayNamesMap.put(dispIndex, returnString);
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return displayNamesMap;
	}

	public static HashMap<Integer, String> getChartParamToSimple2( HashMap<String, Object> _chartItem )
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		ArrayList<String> chartList = getChartPropertys4CombinedColumn();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		
		HashMap<Integer, String> displayNamesMap = new HashMap<Integer, String>();
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";
		
		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		try {
			String _key = "";
			int max = chartList.size();
			for ( i = 0; i < max; i++) {
				
				_key = chartList.get(i);
				
				if( _chartItem.containsKey(_key)  )
				{
					if(_key.equalsIgnoreCase("seriesXFields") )
					{
						String _xfieldsValue= URLDecoder.decode( _chartItem.get("value").toString() , "UTF-8");
						_xfieldsValue=_xfieldsValue.replaceAll("," , "~" );
						chartData.put( _key , _xfieldsValue  );	
					}
					else
					{
						chartData.put( _key, URLDecoder.decode(_chartItem.get(_key).toString() , "UTF-8")  );		
					}
				}
			}
			
			
			ArrayList<ArrayList<HashMap<String, String>>> _displayList = (ArrayList<ArrayList<HashMap<String, String>>>) _chartItem.get("displayNames");
			ArrayList<HashMap<String, String>> list;
			HashMap<String, String> _displayItem;
			
			int _max_displayNames = _displayList.size();
			
			for( int dispIndex=0; dispIndex<_max_displayNames;  dispIndex++ ){
				
				returnString="";
				yFieldsStr="";
				yFieldsDisplayStr="";
				yFieldsColorStr="";
				
				list = _displayList.get(dispIndex);
				
				_max = list.size();
				
				for ( i = 0; i < _max; i++) {
					
					displayMap.clear();
					_displayItem = list.get(i);
					
					if(_displayItem.containsKey("visible") && _displayItem.get("visible").equals("true") )
					{
						if( yFieldsStr.equals("") == false )
						{
							yFieldsStr = yFieldsStr + "~";
						}
						if( yFieldsDisplayStr.equals("") == false )
						{
							yFieldsDisplayStr = yFieldsDisplayStr + "~";
						}
						if( yFieldsColorStr.equals("") == false )
						{
							yFieldsColorStr = yFieldsColorStr + "~";
						}
						
						yFieldsStr = yFieldsStr + URLDecoder.decode( _displayItem.get("column"),"UTF-8");
						yFieldsDisplayStr = yFieldsDisplayStr + URLDecoder.decode( _displayItem.get("text"),"UTF-8");
						yFieldsColorStr = yFieldsColorStr + URLDecoder.decode( _displayItem.get("color"),"UTF-8");
					}
					
				}
				
				_max = chartList.size();
				for ( i = 0; i < _max; i++) {
					if( returnString.equals("") == false ) returnString = returnString + ",";
					if( chartList.get(i).equals("yFieldName") )
					{
						returnString = returnString + yFieldsStr;
					}
					else if( chartList.get(i).equals("yFieldDisplayName") )
					{
						returnString = returnString + yFieldsDisplayStr;
					}
					else if( chartList.get(i).equals("yFieldFillColor") )
					{
						returnString = returnString + yFieldsColorStr;
					}
					else if( chartList.get(i).equals("legendLabelPlacement") )
					{
						returnString = returnString + yFieldsColorStr;
					}
					else if( chartList.get(i).equals("legendMarkHeight") )
					{
						returnString = returnString + _legendMarkHeight;
					}
					else if( chartList.get(i).equals("legendMarkWidth") )
					{
						returnString = returnString + _legendMarkWeight;
					}
					else if( chartData.containsKey(chartList.get(i)))
					{
						returnString = returnString + chartData.get(chartList.get(i));
					}
					else
					{
						returnString = returnString + "";
					}
				}
				
				displayNamesMap.put(dispIndex, returnString);
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return displayNamesMap;
	}
	
	/**
	 *	구현 필요  
	 **/
	public static String getChartParamToJson4Radar( HashMap<String, Value> _chartItem )
	{
		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		int i = 0;
		int j = 0;
		
		HashMap<String, String> _seriesColorMap = new HashMap<String, String>();
		
		
		ArrayList<String> chartList = getChartPropertys4Radar();

		try {
			
			String _prop;
			String _value;
			
			for ( i = 0; i < chartList.size(); i++) {
				
				_prop = chartList.get(i);
					
				if( _prop.equals("seriesColors") ){
					continue;
				}
				
				if( returnString.equals("") == false ) returnString = returnString + ",";
				
				if(_chartItem.containsKey(_prop))
				{
					_value = URLDecoder.decode(_chartItem.get(_prop).getStringValue() , "UTF-8");
				}else{
					_value = "";
				}
				
				returnString = returnString +_value;
			}
			
			
			
			
			ArrayList<HashMap<String, String>> seriesColorsNodeList=null;
			
			if( _chartItem.containsKey("seriesColors") ){
				JSONArray _seriesColorsListJsonArray = (JSONArray) _chartItem.get("seriesColors").getObjectValue();
				_max = _seriesColorsListJsonArray.size();
			
				
				String yFieldsColorStr = "";
				String yFieldsColorValue = "";
				
				for ( i = 0; i < _max; i++) {
					yFieldsColorValue = (String) _seriesColorsListJsonArray.get(i);
					
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr += yFieldsColorValue;
					}else{
						yFieldsColorStr = yFieldsColorValue + "~";
					}
				}
				
				if( returnString.equals("") == false ) returnString = returnString + ",";
				
				returnString = returnString + yFieldsColorStr;
				
			}
			

			
			
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return returnString;
	}

	public static String getChartParamToSimple4Radar( HashMap<String, Object> _chartItem )
	{
		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		int i = 0;
		int j = 0;
		
		HashMap<String, String> _seriesColorMap = new HashMap<String, String>();
		
		
		ArrayList<String> chartList = getChartPropertys4Radar();
		
		try {
			
			String _prop;
			String _value;
			
			for ( i = 0; i < chartList.size(); i++) {
				
				_prop = chartList.get(i);
				
				if( _prop.equals("seriesColors") ){
					continue;
				}
				
				if( returnString.equals("") == false ) returnString = returnString + ",";
				
				if(_chartItem.containsKey(_prop))
				{
					_value = URLDecoder.decode(_chartItem.get(_prop).toString() , "UTF-8");
				}else{
					_value = "";
				}
				
				returnString = returnString +_value;
			}
			
			
			
			
			ArrayList<HashMap<String, String>> seriesColorsNodeList=null;
			
			if( _chartItem.containsKey("seriesColors") ){
				JSONArray _seriesColorsListJsonArray = (JSONArray) _chartItem.get("seriesColors");
				_max = _seriesColorsListJsonArray.size();
				
				
				String yFieldsColorStr = "";
				String yFieldsColorValue = "";
				
				for ( i = 0; i < _max; i++) {
					yFieldsColorValue = (String) _seriesColorsListJsonArray.get(i);
					
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr += yFieldsColorValue;
					}else{
						yFieldsColorStr = yFieldsColorValue + "~";
					}
				}
				
				if( returnString.equals("") == false ) returnString = returnString + ",";
				
				returnString = returnString + yFieldsColorStr;
				
			}
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnString;
	}

	public String convertImageData( String _data, String _prefix, String _suffix, int _itemW, int _itemH )
	{
		String _url="";
		String _txt = "";
		String _servicesUrl = "";
		
		if( _data != null ){
			_txt=_data.toString();
		}
		
		_url= _prefix + _txt + _suffix;

		if(_prefix.equalsIgnoreCase("BLOB://") )
		{
			// BLOB 이미지의 Resize처리
			try {
				if(_txt.length() > ImageUtil.MAXINUM_LENGTH )
				{
					_txt = ImageUtil.resizeBLOBData(_txt, _itemW,  _itemH, true); 
				}
			} catch (IOException e) {
				
			}
			try {
				_servicesUrl = URLEncoder.encode(_txt, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				_servicesUrl = _txt;
			}
			
		}
		else if( _txt.startsWith("data:image/png;base64") )
		{
			_servicesUrl = _txt.substring( _txt.indexOf(",") + 1 , _txt.length());
		}
		else
		{
			try {
				_url = URLEncoder.encode(_url,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+ _url;
		}
		
		return _servicesUrl;
	}
	
	protected HashMap<String, Object> convertUbfxStyle( String _name,  String _fnValue, HashMap<String, Object> _propList  )
	{
//		if(_name.indexOf("Color") != -1 )
//		{
//			_propList.put((_name + "Int"), mPropertyFn.changeColorHexToInt(_fnValue) );
//		}
		
		_propList.put(_name, _fnValue.trim());			// 20170531 true false에 공백이 붙어 나오는 현상이 있어 수정
		
		// color 속성은 color + Int 속성을 넣어줘야 한다.
		if( _name.contains("Color") ){
			_propList.put((_name + "Int"), common.getIntClor(_fnValue) );
		}
		else if( _name.equals("width") )
		{
			if( !_propList.containsKey("x2") )_propList.put("x2", _fnValue);
		}
		else if( _name.equals("height"))
		{
			if( !_propList.containsKey("y2") )_propList.put("y2", _fnValue);
		}
		else if(_name.equals("lineThickness"))
		{
			_propList.put("thickness", _fnValue);
		}
		
		
		return _propList;
	}
	
	
	
	public HashMap<String, Object> convertItemDataSimpleExport(BandInfoMapData _bandInfo, HashMap<String, Value> currentItemData, String dataSet, int rowIndex ,HashMap<String, Object> _param, int _startIndex, int _lastIndex , int _totalPageNum , int _currentPage ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);	
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _className 		= "";
		String _name 		= "";
//		String _value 		= "";
		Object _value 		= null;
		String _dataID 		= "";
		int k = 0;
		
		String _model_type = "";
		String _barcode_type = "";
		String _itemId = "";
		
		if(currentItemData.containsKey("id"))
		{
			_itemId = currentItemData.get("id").getStringValue();
		}
		
		HashMap<String, Object> _propList = new HashMap<String, Object>();
		
		_className = currentItemData.get("className").getStringValue();
		// image variable
		String _prefix="";
		String _suffix="";
					
		Boolean _bType = false;

		if(_className.equals("UBLabelBorder"))
		{
			_bType = true;
		}
		else
		{
			_bType = false;
		}
		
		if( dataSet == null || dataSet.equals("") )
		{
				
			if( currentItemData.get("dataSet") != null ){
				if(   currentItemData.get("dataSet").getStringValue() != null ){
					if(  !currentItemData.get("dataSet").getStringValue().equals("") ){
						_dataID =  currentItemData.get("dataSet").getStringValue();
					}
				}
			}
			
			if( _bandInfo.getDataSet().equals("") == false && _bandInfo.getAdjustableHeight() && _bandInfo.getResizeText() )
			{
				_dataID = _bandInfo.getDataSet();
				
				if( mOriginalDataMap.get( _dataID ) != null && currentItemData.get("dataSet") != null && currentItemData.get("dataSet").getStringValue() != null && currentItemData.get("dataSet").getStringValue().equals("null") == false )
				{
					if( mOriginalDataMap.get( _dataID ).equals(currentItemData.get("dataSet").getStringValue()) == false )
					{
						_dataID = currentItemData.get("dataSet").getStringValue();
					}
				}
			}

		}
		else
		{
			if( mOriginalDataMap.get( dataSet ) != null && currentItemData.get("dataSet") != null && currentItemData.get("dataSet").getStringValue() != null && currentItemData.get("dataSet").getStringValue().equals("null") == false )
			{
				if( mOriginalDataMap.get( dataSet ).equals(currentItemData.get("dataSet").getStringValue()) == false )
				{
					_dataID = currentItemData.get("dataSet").getStringValue();
				}
				else
				{
					_dataID = dataSet;	
				}
			}
			else
			{
				_dataID = dataSet;
			}
		}
		
		if( DataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
			

		Set<String> _keySet = currentItemData.keySet();
		Object[] hmKeys = _keySet.toArray();
		
		// system function
		String _systemFunction="";
		
		// formatter variables 
		String _formatter="";
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";
		String _inputForamtString = "";
		String _outputFormatString = "";
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		
		// edit formatter variables (e-form)
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";
		
		HashMap<String, Object> _editFormatValueMap = null;
		
		if(currentItemData.containsKey("editItemFormatValue"))
		{
			_editFormatValueMap = (HashMap<String, Object>) currentItemData.get("editItemFormatValue").getMapValue();
		}
		
		// kyh formatter property setting
		Element ItemElementE = currentItemData.get("Element").getElementValue();
		NodeList editFormatterItem = ItemElementE.getElementsByTagName("editItemFormatter");
		
		ArrayList<NodeList> _ubfxNodes = new ArrayList<NodeList>();
		// kyh formatter property setting
		Element ItemElement = currentItemData.get("Element").getElementValue();
		NodeList _formatterNode = ItemElement.getElementsByTagName("formatter");
		Element _formatterItem = (Element)_formatterNode.item(0);
		String _eformatDataset=null;
		String _eformatKeyField=null;
		String _eformatLabelField=null;
		
		if( currentItemData.containsKey("ITEM_DATA") )
		{
			_propList = (HashMap<String, Object>)  currentItemData.get("ITEM_DATA").getMapValue().clone();
			
			// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
			if( _propList.containsKey("rowId") )
			{
				_propList.put("rowId", rowIndex);
			}
			
			if( _propList.containsKey("systemFunction") ) _systemFunction = _propList.get("systemFunction").toString();
			if( _propList.containsKey("editItemFormatter") ) _formatterE = _propList.get("editItemFormatter").toString();
			if( _propList.containsKey("type") ) _model_type = _propList.get("type").toString();
			if( _propList.containsKey("barcodeType") ) _barcode_type = _propList.get("barcodeType").toString();
			if( _propList.containsKey("prefix") ) _prefix = _propList.get("prefix").toString();
			if( _propList.containsKey("dataType") ) _dataType = _propList.get("dataType").toString();
			if( _propList.containsKey("suffix") ) _suffix = _propList.get("suffix").toString();
			
			// formatter속성 담기
			if( _propList.containsKey("formatter") ) 			_formatter = _propList.get("formatter").toString();
			if( _propList.containsKey("mask") ) 				_mask = _propList.get("mask").toString();
			if( _propList.containsKey("decimalPointLength") ) 	_decimalPointLength = Integer.parseInt(_propList.get("decimalPointLength").toString());
			if( _propList.containsKey("useThousandComma") ) 	_useThousandComma = _propList.get("useThousandComma").toString().equals("true");
			if( _propList.containsKey("isDecimal") ) 			_isDecimal =  _propList.get("isDecimal").toString().equals("true");
			if( _propList.containsKey("formatString") ) 		_formatString = _propList.get("formatString").toString();
			if( _propList.containsKey("nation") ) 				_nation = _propList.get("nation").toString();
			if( _propList.containsKey("currencyAlign") ) 		_align = _propList.get("currencyAlign").toString();
			if( _propList.containsKey("inputFormatString") ) 	_inputForamtString = _propList.get("inputFormatString").toString();
			if( _propList.containsKey("outputFormatString") ) 	_outputFormatString = _propList.get("outputFormatString").toString();
			
			
			if( _propList.containsKey("eformnation") ) _nationE = _propList.get("eformnation").toString();
			if( _propList.containsKey("eformmask") ) _maskE = _propList.get("eformmask").toString();
			if( _propList.containsKey("eformdecimalPointLength") ) _decimalPointLengthE = Integer.valueOf( _propList.get("eformdecimalPointLength").toString() );
			if( _propList.containsKey("eformisDecimal") ) _isDecimalE = !_propList.get("eformisDecimal").toString().equals("false");
			if( _propList.containsKey("eformuseThousandComma") ) _useThousandCommaE = !_propList.get("eformuseThousandComma").toString().equals("false");
			if( _propList.containsKey("eformformatString") ) _formatStringE = _propList.get("eformformatString").toString();
			if( _propList.containsKey("dataProvider") ) _formatStringE = _propList.get("dataProvider").toString();
			if( _propList.containsKey("eDataset") ) _eformatDataset = _propList.get("eDataset").toString();
			if( _propList.containsKey("eKeyField") ) _eformatKeyField = _propList.get("eKeyField").toString();
			if( _propList.containsKey("eValueField") ) _eformatLabelField = _propList.get("eValueField").toString();
			
			
		}
		else
		{
			_propList = mItemPropVar.getItemName(_className);
			
			if(_propList == null ) return null;
			
			String[] _propArray = ItemPropertyVariable.getSimpleItemProperty(_className);

			
			// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
			if( _propList.containsKey("rowId") )
			{
				_propList.put("rowId", rowIndex);
			}
			
			for ( k = 0; k < _propArray.length; k++) {
				_name = (String) _propArray[k];
//			for ( k = 0; k < hmKeys.length; k++) {
//				_name = (String) hmKeys[k];	

				if( currentItemData.containsKey(_name) == false ||  currentItemData.get(_name) == null ) continue;
				
				_value = currentItemData.get(_name).getValue();
				
				if( _name.equals("fontFamily"))
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					if(common.isValidateFontFamily((String)_value))
						_propList.put(_name, _value);
					else
						_propList.put(_name, "Arial");
				}
				else if( _name.equals("contentBackgroundColors")  )
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					
					ArrayList<String> _arrStr = new ArrayList<String>();
					_arrStr = mPropertyFn.getColorArrayString( (String)_value );
					_propList.put(_name, _arrStr);
					
					_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
					_propList.put((_name + "Int"), _arrStr);
					
				}
				else if( _name.equals("contentBackgroundAlphas") )
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					
					ArrayList<String> _arrStr = new ArrayList<String>();
					_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
					_propList.put(_name, _arrStr);
					
				}
				else if( _name.indexOf("Color") != -1 && _name.equals("borderColors") == false && _name.equals("borderColorsInt") == false)
				{	
					if( _value.toString().contains(",") )
					{
						ArrayList<String> _valueArray = Value.setArrayString( _value.toString() );
						_value = _valueArray.get(rowIndex%_valueArray.size());
						_propList.put((_name + "Int"), _value);
						
						_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
						_propList.put(_name, _value);
					}
					else
					{
						_propList.put((_name + "Int"), _value);
						
						_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
						_propList.put(_name, _value);
					}
				}
				else if( _name.equals("lineHeight"))
				{
					if( _value.toString().indexOf("%") != -1 )
					{
						_value = _value.toString().replace("%25", "").replace("%", "");
						_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
					}
					_propList.put(_name, _value);
				}
				else if( _name.equals("label"))
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("borderType"))
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("text"))
				{
					_propList.put(_name, _value == null ? "" : _value);
				}
				else if( _name.equals("borderSide"))
				{
					ArrayList<String> _bSide = new ArrayList<String>();
					if( currentItemData.get(_name).getStringValue().equals("none") == false )
					{
						_bSide = mPropertyFn.getBorderSideToArrayList( currentItemData.get(_name).getStringValue() );

						if( _bSide.size() > 0)
						{
							String _type = (String) _propList.get("borderType");
							_type = mPropertyFn.getBorderType(_type);
							_propList.put("borderType", _type);
						}
					}
					_propList.put(_name, _bSide);
				}
				else if( _name.equals("type") )
				{
					_model_type = _value.toString();
					_propList.put(_name, _value);
				}
				else if( _name.equals("barcodeType") )
				{
					_barcode_type = _value.toString();
					_propList.put(_name, _value);
				}
				else if( _name.equals("clipArtData") )
				{
					_propList.put(_name, _value + ".svg");
				}
				else if( _name.equals("checked") )
				{
					_propList.put("selected", _value);
				}
				else if( _name.equals("conerRadius") )
				{
					_propList.put("rx", _value);
					_propList.put("ry", _value);
				}
				else if(_name.equals("borderThickness"))
				{
					_propList.put("borderWidth", _value);
				}
				else if(_name.equals("borderWeight"))
				{
					_propList.put("borderWidth", _value);
				}else if( _name.equals("formatter") ){
					_formatter = _value.toString();
					_propList.put("formatter", _formatter);
				}else if( _name.equals("systemFunction") ){
					_systemFunction = _value.toString();
					_propList.put("systemFunction", _systemFunction);
				}else if( _name.equals("editItemFormatter") )
				{
					_formatterE = _value.toString();
					_propList.put("editItemFormatter", _formatterE);
				}
				else if(_name.equals("dataType"))
				{
					_dataType = _value.toString();
					_propList.put(_name, _value);
				}
				
				else if( _name.equals("data") )
				{
					if(_className.equals("UBImage") || _className.equals("UBSignature") || _className.equals("UBTextSignature") || _className.equals("UBPicture"))
					{
						_propList.put("src",  URLEncoder.encode(_value.toString(), "UTF-8"));
					}
				}
				else if( _name.equals("prefix") ){
					try {
						_prefix=URLDecoder.decode(_value.toString(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					_propList.put(_name, _prefix);
					
				}
				else if( _name.equals("suffix") ){
					_suffix=_value.toString();
					
					_propList.put(_name, _suffix);
				}
				else if( _name.equals("leftBorderType") ||  _name.equals("rightBorderType") ||  _name.equals("topBorderType") ||  _name.equals("bottomBorderType") )
				{
					_propList.put(_name, _value);
				}
				// 명우 추가 
				else if(_name.equals("startPoint"))
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					String[] _sPoint = _value.toString().split(",");
					_propList.put("x1", Float.valueOf(_sPoint[0]));
					_propList.put("y1", Float.valueOf(_sPoint[1]));
					
				}
				else if(_name.equals("endPoint"))
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					String[] _ePoint = _value.toString().split(",");

					_propList.put("x2", Float.valueOf(_ePoint[0]));
					_propList.put("y2", Float.valueOf(_ePoint[1]));
					
				}
				else if( _name.equals("x"))
				{
					//_propList.put("x1", _value);
					_propList.put("x", _value);
				}
				else if( _name.equals("y"))
				{
					//_propList.put("y1", _value);
					_propList.put("y", _value);
				}
				else if( _name.equals("width"))
				{
					//_propList.put("x2", _value);
					_propList.put("width", _value);
				}
				else if( _name.equals("height"))
				{
					//_propList.put("y2", _value);
					_propList.put("height", _value);
				}
				else if(_name.equals("lineThickness"))
				{
					_propList.put("thickness", _value);
				}
				else if( _name.equals("rWidth") ||  _name.equals("rHeight") )
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("printVisible") )
				{
					if( ("PRINT".equals(isExportType) || "PDF".equals(isExportType)) && "false".equals(_value) )
					{
						return null;
					}
				}
				else if( _name.equals("markanyVisible") )
				{
					if( _isMarkAny && "PRINT".equals(isExportType) && "false".equals(_value) )
					{
						return null;
					}
				}
				else if( _name.equals("rotation") )
				{
					_propList.put(_name, _value);
					_propList.put("rotate", _value);
				}
				else
				{
					_propList.put(_name, _value);
				}
			}
			
			if( _className.toUpperCase().indexOf("LINE") == -1)
			{
				_propList.put("x1", _propList.get("x"));
				_propList.put("y1", _propList.get("y"));
				_propList.put("x2", _propList.get("width"));
				_propList.put("y2", _propList.get("height"));
			}
			else
			{
//				if( _propList.containsKey("x1") )
//				{
//					_propList.put("x1", Float.parseFloat(_propList.get("x").toString()) + Float.parseFloat(_propList.get("x1").toString()) );
//				}
//				if( _propList.containsKey("y1") )
//				{
//					_propList.put("y1", Float.parseFloat(_propList.get("y").toString()) + Float.parseFloat(_propList.get("y1").toString()) );
//				}
//				if( _propList.containsKey("x2") )
//				{
//					_propList.put("x2", Float.parseFloat(_propList.get("x").toString()) + Float.parseFloat(_propList.get("x2").toString()) );
//				}
//				if( _propList.containsKey("y2") )
//				{
//					_propList.put("y2", Float.parseFloat(_propList.get("y").toString()) + Float.parseFloat(_propList.get("y2").toString()) );
//				}
			}
			
			
			
			// Item의 changeData가 있는지 확인
			if(mChangeItemList != null )
			{
				String _chkID = _itemId + "_"+ _bandInfo.getId() + "_ROW"+rowIndex;
				
				_propList = convertChangeItemDataText( _currentPage ,_propList, _chkID );
			}
			// 보더업데이트
			if( _propList.containsKey("isCell") && _propList.get("isCell").toString().equals("false") )
			{
				_propList = convertItemToBorder(_propList);
			}
			if(currentItemData.containsKey("ORIGINAL_TABLE_ID"))
			{
				_propList.put("ORIGINAL_TABLE_ID", currentItemData.get("ORIGINAL_TABLE_ID").getStringValue() );
			}
			if(currentItemData.containsKey("beforeBorderType"))
			{
				_propList.put("beforeBorderType", currentItemData.get("beforeBorderType").getArrayBooleanValue());
			}
			
			if(currentItemData.containsKey("PRESERVE_ASPECT_RATIO"))
			{
				_propList.put("PRESERVE_ASPECT_RATIO", currentItemData.get("PRESERVE_ASPECT_RATIO").getStringValue());
			}
			
			if( _formatterItem != null )
			{
				NodeList _formatterItemPropertyList = _formatterItem.getElementsByTagName("property");
				for( int _formatterIndex=0;  _formatterIndex < _formatterItemPropertyList.getLength(); _formatterIndex++ ){
					Element _formatterItemProperty = (Element)_formatterItemPropertyList.item(_formatterIndex);
					String _formatPropertyName = _formatterItemProperty.getAttribute("name");
					String _formatPropertyValue = _formatterItemProperty.getAttribute("value");
					try {
						_formatPropertyValue =URLDecoder.decode(_formatPropertyValue, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(_formatPropertyName.equals("nation"))
					{
						_nation = _formatPropertyValue;
					}
					else if(_formatPropertyName.equals("align"))
					{
						_align=_formatPropertyValue;
					}
					else if( _formatPropertyName.equals("mask") ){
						_mask = _formatPropertyValue;
					}
					
					else if( _formatPropertyName.equals("decimalPointLength") ){
						if(  _formatPropertyValue.equalsIgnoreCase("NaN") ){
							_decimalPointLength = 0;
						}else{
							_decimalPointLength = common.ParseIntNullChk(_formatPropertyValue, 0);	
						}
					}				
					
					else if( _formatPropertyName.equals("useThousandComma") ){
						_useThousandComma = Boolean.parseBoolean(_formatPropertyValue);
					}		
					else if( _formatPropertyName.equals("isDecimal") ){
						_isDecimal = Boolean.parseBoolean(_formatPropertyValue);
					}		
					else if( _formatPropertyName.equals("formatString") ){
						_formatString = _formatPropertyValue;
					}
					else if( _formatPropertyName.equals("inputFormatString") )
					{
						_inputForamtString = URLDecoder.decode(_formatPropertyValue , "UTF-8");
					}
					else if( _formatPropertyName.equals("outputFormatString") )
					{
						_outputFormatString =  URLDecoder.decode(_formatPropertyValue , "UTF-8");
					}
				}
				
			}
			
			if( !_formatter.equals("") )
			{
				// format이 label band에서 안들어간다.
				_propList.put("formatter", _formatter);
				_propList.put("mask", _mask);
				_propList.put("decimalPointLength", _decimalPointLength);
				_propList.put("useThousandComma", _useThousandComma);
				_propList.put("isDecimal", _isDecimal);
				_propList.put("formatString", _formatString);
				_propList.put("nation", _nation);
				_propList.put("currencyAlign", _align);
				_propList.put("inputFormatString", _inputForamtString);
				_propList.put("outputFormatString", _outputFormatString);
			}
			
			if( editFormatterItem != null && editFormatterItem.getLength() > 0 )
			{	
				Element _propItem;
				
				NodeList formatterProperty = ((Element) editFormatterItem.item(0)).getElementsByTagName("property");
				int propertySize = formatterProperty.getLength();
				for (int i = 0; i < propertySize; i++) {
					
					_propItem = (Element) formatterProperty.item(i);
					
					String _propName = _propItem.getAttribute("name");
					String _propValue = _propItem.getAttribute("value");
					
					if( _propName.equals("nation") ){
						_nationE = URLDecoder.decode(_propValue, "UTF-8");
						_propList.put("eformnation", 	_nationE );
					}
					else if( _propName.equals("mask") ){
						_maskE = URLDecoder.decode(_propValue, "UTF-8");
						_propList.put("eformmask", 	_maskE );
					}
					else if( _propName.equals("decimalPointLength") ){
						_decimalPointLengthE = common.ParseIntNullChk(_propValue, 0);
						_propList.put("eformdecimalPointLength", 	_decimalPointLengthE );
					}				
					else if( _propName.equals("useThousandComma") ){
						_useThousandCommaE = Boolean.parseBoolean(_propValue);
						
						_propList.put("eformuseThousandComma", 	_useThousandCommaE );
					}		
					else if( _propName.equals("isDecimal") ){
						_isDecimalE = Boolean.parseBoolean(_propValue);
						_propList.put("eformisDecimal", _isDecimalE	 );
					}		
					else if( _propName.equals("formatString") ){
						_formatStringE = _propValue;
						_propList.put("eformformatString", _formatStringE	 );
					}else if( _propName.equals("dataProvider") ){
						_propValue = URLDecoder.decode(_propValue, "UTF-8");
						_propList.put("eformDataProvider", _propValue	 );
					}else if( _propName.equals("dataset") ){
						_eformatDataset = _propValue;
						_propList.put("eDataset", _propValue );
					}else if( _propName.equals("keyField") ){
						_eformatKeyField = _propValue;
						_propList.put("eKeyField", _propValue );
					}else if( _propName.equals("valueField") ){
						_eformatLabelField = _propValue;
						_propList.put("eValueField", _propValue );
					}
				}
				
			}
			
			currentItemData.put("ITEM_DATA", new Value(_propList.clone(), "map") );
			
			// currentItemData에 필요 속성들을 담기 systemFunction / formatter관련 속성 / 
		}
		
		
		//Table의 UBFX가 존재할경우 처리( Table의 ubfx를 먼저 처리후 Cell의 ubfx를 처리 )
		NodeList _ubfunction = ItemElement.getElementsByTagName("ubfunction"); 
		
		int _nodeCnts = 0;
		
		if( currentItemData.get("tableUbfunction") != null && currentItemData.get("tableUbfunction").getNodeListValue().getLength() > 0 )
		{
			_ubfxNodes.add(currentItemData.get("tableUbfunction").getNodeListValue());
		}
		_ubfxNodes.add(_ubfunction);
		
		_nodeCnts = _ubfxNodes.size();
		
		for(int _ubfxListIndex= 0; _ubfxListIndex < _nodeCnts; _ubfxListIndex++)
		{
			NodeList _selectNodeList = _ubfxNodes.get(_ubfxListIndex);
			for(int _ubfxIndex = 0; _ubfxIndex < _selectNodeList.getLength(); _ubfxIndex++)
			{
				Element _ubfxItem = (Element) _selectNodeList.item(_ubfxIndex);
				String _ubfxProperty = _ubfxItem.getAttribute("property");
				String _ubfxValue = _ubfxItem.getAttribute("value");
				try {
					_ubfxValue = URLDecoder.decode(_ubfxValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ArrayList<HashMap<String, Object>> _pList = DataSet.get(dataSet);
				String _datasetColumnName = currentItemData.get("column")==null ? "" : currentItemData.get("column").getStringValue();
				mFunction.setDatasetList(DataSet);
				mFunction.setParam(_param);

				mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
				if(mGroupTotalPageIndex>0) mFunction.setSectionCurrentPageNum(mGroupCurrentPageIndex);
				mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
				if(mGroupTotalPageIndex>0) mFunction.setSectionTotalPageNum(mGroupTotalPageIndex);
				mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
				mFunction.setOriginalDataMap(mOriginalDataMap);
				
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,dataSet);
				}else{
					_fnValue = mFunction.function(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,dataSet);
				}
				
				
				_fnValue = _fnValue.trim();
				
				if(_ubfxProperty.equals("text") || _fnValue.equals("") == false)
				{
					_propList = convertUbfxStyle(_ubfxProperty, _fnValue, _propList );
				}
			}
		}
		
		_ubfxNodes = null;
		
		if( _className.equals("UBSVGRichText") && _bandInfo.getAdjustableHeightListAr().size() > 0 )
		{
			if( _bandInfo.getAdjustableHeightListAr().size() > rowIndex )
			{
				_propList.put("height", _bandInfo.getAdjustableHeightListAr().get(rowIndex) - currentItemData.get("band_y").getIntegerValue() );
			}
		}
		
		//hyperLinkedParam처리
		if( _propList.containsKey("ubHyperLinkType") && "2".equals( _propList.get("ubHyperLinkType") )  )
		{
			NodeList _hyperLinkedParam = ItemElement.getElementsByTagName("ubHyperLinkParm");
			if( _hyperLinkedParam != null && _hyperLinkedParam.getLength() > 0 )
			{
				Element _hyperLinkEl = (Element) _hyperLinkedParam.item(0);
				NodeList _hyperLinkedParams = _hyperLinkEl.getElementsByTagName("param");
				int _hyperLinkedParamSize = _hyperLinkedParams.getLength();
				
				HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
				
				for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
				{
					Element _hyperParam = (Element) _hyperLinkedParams.item(_hyperIdx);
					NodeList _hyperPropertys = _hyperParam.getElementsByTagName("property");
					int _hyperPropertysSize = _hyperPropertys.getLength();
					String _hyperParamKey = "";
					String _hyperParamValue = "";
					String _hyperParamType = "";
					
					for (int _hyperProIdx = 0; _hyperProIdx <  _hyperPropertysSize; _hyperProIdx++) 
					{
						Element _hyperProperty = (Element) _hyperPropertys.item(_hyperProIdx);
						if( "id".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamKey = _hyperProperty.getAttribute("value").toString();
						}
						else if( "value".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamValue = _hyperProperty.getAttribute("value").toString();
						}
						else if( "type".equals(_hyperProperty.getAttribute("name")) )
						{
							_hyperParamType = _hyperProperty.getAttribute("value").toString();
						}
					}
					
					if( "DataSet".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						
						_hyperParamValue = "";
						
						if( dataSet != null && dataSet.equals("") == false && mOriginalDataMap != null && mOriginalDataMap.get(dataSet).equals(_hyperLinkedDataSetId) )
						{
							_hyperLinkedDataSetId = dataSet;
						}
						
						if(DataSet.containsKey(_hyperLinkedDataSetId))
						{
							ArrayList<HashMap<String, Object>> _list = DataSet.get( _hyperLinkedDataSetId );
							Object _dataValue = "";
							if( _list != null ){
								if( rowIndex < _list.size() )
								{
									HashMap<String, Object> _dataHm = _list.get(rowIndex);
									_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
								}
							}
						}
					}
					else if("Parameter".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_hyperParamValue = "";
						}
						else
						{
							_hyperParamValue = _pValue;
						}
					}
					
					_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
				}
				
				_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
			}
			
		}
		
		boolean _useCK = true;
		if( _useCK && currentItemData.containsKey("dataType") && currentItemData.containsKey("dataSet")  )	
		{
			String columnStr =  currentItemData.get("column").getStringValue();
			String dataTypeStr = currentItemData.get("dataType").getStringValue();
			
			if( currentItemData.containsKey("dataType_N") && currentItemData.get("dataType_N").getStringValue().equals("") == false )
			{
				dataTypeStr = currentItemData.get("dataType_N").getStringValue();
			}
			if( currentItemData.containsKey("column_N") && currentItemData.get("column_N").getStringValue().equals("") == false )
			{
				columnStr = currentItemData.get("column_N").getStringValue();
			}
			
			if( currentItemData.containsKey("currentAdjustableHeight") && currentItemData.get("currentAdjustableHeight").getIntegerValue() != -1 )
			{
				_propList.put("currentAdjustableHeight", currentItemData.get("currentAdjustableHeight").getIntegerValue());
			}
			
			if( dataTypeStr.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list = DataSet.get( _dataID );
				Object _dataValue = "";
				if( _list != null ){
					if( rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						_dataValue = _dataHm.get( columnStr );
					}
				}
				
				// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐.. 
				if(_className.equals("UBSVGArea") ){
					
					if(_dataValue==null || _dataValue.toString().equals("")) return null;
					
					String _tmpDataValue = _dataValue.toString();
					if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
					{
						_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
					}
					
					boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
					
					boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
					boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
					
					if(_bSVG)
					{
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
					}
					else
					{
						
						boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
						if(!_bHasHtmlTag)
							_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
							
						_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							
					}
					log.debug("1078-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _dataValue.toString() + "]");
					
					_dataValue = _dataValue.toString().replace(" ", "%20");
					
					if( !_dataValue.toString().equals("") )
					{
						_propList.put("data",  URLEncoder.encode(_dataValue.toString(), "UTF-8"));
					}
					else
					{
						return null;
					}
					
					
				}
				else if( _className.equals("UBSVGRichText") )
				{
					// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
					if( _dataValue != null && _dataValue.toString().equals("") == false )
					{
						_propList = convertUBSvgItem(_dataValue,_propList);
						
						if(_propList == null ) return null;
					}
				}
				
				
				else if("UBCheckBox".equals(_className) )
				{
					String _selectedText=_propList.get("selectedText").toString();
					//String _deSelectedText=_propList.get("deSelectedText").toString();
					if( _dataValue != null && _selectedText.equalsIgnoreCase(_dataValue.toString()) ){
						_propList.put("selected", "true");
					}else{
						_propList.put("selected", "false");	
					}
					
				}	
				else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") )
				{
					String _servicesUrl = convertImageData( _dataValue.toString() , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
					_propList.put("src", _servicesUrl);
				}
				else
				{
					_propList.put("text", _dataValue == null ? "" : _dataValue);
					
					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					_propList.put("tooltip", _propList.get("text"));
				}
				
			}
			else if( dataTypeStr.equals("2"))
			{
				// rowIndex : 현재 Row Index
				// dataSet : 그룹핑된 데이터셋
				boolean _chkRowIndex = true;
				if( DataSet.containsKey(_dataID))
				{
					// 함수 조회시 데이터 밴드의 Row수만큼만 만큼만 처리 하도록 변경 ( 데이터셋의 Row 수 만큼만 처리 하도록 하였으나 특정 함수의 경우 모든 Row에 필요 )
//					if( rowIndex > 0 && rowIndex >= DataSet.get( _dataID ).size() )
					if( rowIndex > 0 && rowIndex >= _bandInfo.getRowCount() )
					{
						_chkRowIndex = false;
						
						/** RowIndex와 같은 특정 함수는 모든 Row를 반복하도록 처리가 필요.*/
						if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") )
						{
							for (int _si = 0; _si < GlobalVariableData.GLOBAL_FUNCTION_LIST.length; _si++) {
								if( _systemFunction.indexOf( GlobalVariableData.GLOBAL_FUNCTION_LIST[_si]+"(" ) != -1 )
								{
									_chkRowIndex = true;
									break;
								}
							}
						}
						
					}
				}
				
				if( _chkRowIndex )
				{
					if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
						
						ArrayList<HashMap<String, Object>> _pList = DataSet.get( dataSet );
						String _datasetColumnName = columnStr;
						mFunction.setDatasetList(DataSet);
						mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
						mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
						mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
						mFunction.setOriginalDataMap(mOriginalDataMap);
						
						
						String _fnValue;
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = mFunction.testFN(_systemFunction , rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet);
						}else{
							_fnValue = mFunction.function(_systemFunction,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
						}
						

						if( _className.equals("UBSVGRichText") )
						{
							if( _fnValue != null && _fnValue.equals("") == false )
							{
								// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
								_propList = convertUBSvgItem(_fnValue,_propList);
								
								if(_propList == null ) return null;
							}

						}
						else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
							//_fnValue = URLDecoder.decode(_fnValue, "UTF-8");
							
							String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
							_propList.put("src", _servicesUrl);
							
						}
						
						else if(_className.equals("UBCheckBox") ) {
							_propList.put("selected", _fnValue);
						}
						else
						{
							_propList.put("text", _fnValue == null ? "" : _fnValue);
						}
						
					}
				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			else if( dataTypeStr.equals("3"))
			{
				String _txt = _propList.get("text").toString();
				
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				String _fnValue = "";
				
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					if( _inOf != 0 ){
						
						// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
						if(_className.equals("UBSVGArea")  ){
							
							String _tmpDataValue = String.valueOf(_txt);
							if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
							{
								_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
							}
							
							boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
							boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
							boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
							
							String _svgTag = null;
							if(_bSVG)
							{
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
							}
							else
							{
								boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
								if(!_bHasHtmlTag)
									_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
									
								_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
									
							}
													
							log.debug("1258-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _svgTag + "]");
							
							_svgTag = _svgTag.toString().replace(" ", "%20");
							if( !_svgTag.equals("") )
							{
								_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8"));
							}
							else
							{
								return null;
							}
							_txt = "";
						}
						else if( _className.equals("UBSVGRichText") )
						{
							if( _txt != null && _txt.equals("") == false )
							{
								_propList = convertUBSvgItem( _txt, _propList);
								
								if(_propList == null ) return null;
							}
							_txt = "";
						}
						
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							//_fnValue = mFunction.testFN(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
							_fnValue = _txt;
						}else{
							_fnValue = mFunction.function(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
						}
						
					}else{

						int _keyIndex=_txt.lastIndexOf("}");
						_pKey = _txt.substring(_inOf + 7 , _keyIndex);
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						_fnValue = _pList.get("parameter");

					}
					
					if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
						
						String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
						_propList.put("src", _servicesUrl);
					}
					else
					{
						if( _fnValue.equals("undefined"))
						{
							_propList.put("text", "");
						}
						else
						{
							_propList.put("text", _fnValue);
						}
					}

				}
				else
				{
					_propList.put("text", "");
				}
			}
			
		}
		
		// formatter set	// ITem의 Formatter 처리 ( export처리를 위하여 속성값 담기 )
		if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
			Object _propValue;
			String _formatValue="";
			_propValue=_propList.get("text");
			_formatValue = _propValue.toString();
			String _excelFormatterStr = "";
			try {
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					_excelFormatterStr = _nation  + "§" + _align;
					
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					_excelFormatterStr = _formatString;
					
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					_excelFormatterStr = _mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal;
					
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
                else if( _formatter.equalsIgnoreCase("CustomDate") )
				{
					_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
					_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
					
					_propList.put("inputFormatString", _inputForamtString);
					_propList.put("outputFormatString", _outputFormatString);
				}
				
			} catch (ParseException e) {
				//e.printStackTrace();
			}
			
			if( isExportType.equals("EXCEL") && _excelFormatterStr.equals("") == false && common.getPropertyValue("excelExport.useFormatter") != null && common.getPropertyValue("excelExport.useFormatter").equals("true") ) 
			{
				_propList.put("EX_FORMATTER", _formatter);
				_propList.put("EX_FORMAT_DATA_STR", _excelFormatterStr);
				_propList.put("EX_FORMAT_ORIGINAL_STR", _propValue.toString() );
			}
			
			_propList.put("text", _formatValue);
			
		}
		
		if( editFormatterItem != null && editFormatterItem.getLength() > 0 )
		{	
			if( _formatterE.equals("SelectMenu") ){
				
				// dataset으로 comboBox 표현.
				if( _eformatDataset != null &&  (!_eformatDataset.equals("null")) && DataSet.containsKey(_eformatDataset) ){
					
					String _efText = _propList.get("text").toString();
					Boolean _hasValueKey = false;
					
					ArrayList<HashMap<String, Object>> _list = DataSet.get(_eformatDataset);
					
					HashMap<String, Object> _dataHm;
					Object _keyData;
					Object _labelData;
					
					JSONArray ja = new JSONArray();
					String _jsonStr=null;
					JSONObject jo;
					String _keyStr=null;
					String _labelStr=null;
					
					for( int _eformatIdx=0; _eformatIdx<_list.size(); _eformatIdx++ ){
						_dataHm = _list.get(_eformatIdx);
						_keyData = _dataHm.get(_eformatKeyField);
						_labelData = _dataHm.get(_eformatLabelField);
						_keyStr=_keyData.toString();
						_labelStr = _labelData.toString();
						
						if( _efText.equals(_keyStr) && _hasValueKey == false ){
							_hasValueKey = true;
							_propList.put("text", _labelStr );
						}
						
						jo = new JSONObject();
						jo.put("label", _labelStr);
						jo.put("value",_keyStr );
						ja.add(jo);
					}
					
					_jsonStr = ja.toJSONString();
					
					_propList.put("eformDataProvider", _jsonStr	 );
				}else{
					
					if( _propList.containsKey("eformDataProvider") == false && _propList.get("eformDataProvider") == null )
					{
						_propList.put("eformDataProvider", "[]");
					}
					
					String _jsonStr = _propList.get("eformDataProvider").toString(); 
					JSONParser jsonParser = new JSONParser();
					try {
						
						JSONArray ja = (JSONArray) jsonParser.parse(_jsonStr);
						
						String _efText = _propList.get("text").toString();
						JSONObject oj;
						for( int jsonIdx=0; jsonIdx<ja.size(); jsonIdx++ ){
							oj=(JSONObject) ja.get(jsonIdx);
							if( oj.get("value").equals(_efText) ){
								_propList.put("text", oj.get("label") );
								break;
							}
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}
		
		
		//ResizeFont 값이 true이고 adjustableHeight값이 true 일경우 처리 
		if( _propList.containsKey("text") &&  "".equals(_propList.get("text").toString()) == false && currentItemData.containsKey("resizeFont") && currentItemData.get("resizeFont").getBooleanValue() )
		{
			if( _bandInfo.getResizeFontData().size() > rowIndex )
			{
				_propList.put("fontSize", _bandInfo.getResizeFontData().get(rowIndex).get( currentItemData.get("id").getStringValue() ));
			}
			else
			{
				float _fontSize 	= Float.valueOf( _propList.get("fontSize").toString() );
				String _fontFamily 	= _propList.get("fontFamily").toString();
				String _fontWeight 	= _propList.get("fontWeight").toString();
				float _padding = (_propList.containsKey("padding"))? Float.valueOf( _propList.get("padding").toString()):3;
				
				float _maxBorderSize = 0;
				if(_propList.containsKey("borderWidths"))
				{
					ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
					
					for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
						if(_maxBorderSize < _borderWidths.get(_bIndex))
						{
							_maxBorderSize = _borderWidths.get(_bIndex);
						}
					}
					_padding = _maxBorderSize + _padding;
				}
				
				float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
				
				
				_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
				_propList.put("fontSize",  _fontSize);
			}
		}
		
		
		if( _itemId.equals("") == false )
		{
			_propList.put("TABINDEX_ID", _itemId + "_"+ _bandInfo.getId() + "_ROW"+rowIndex);
			_propList.put("SUFFIX_ID", "_"+ _bandInfo.getId() + "_ROW"+rowIndex);
			
		}
		
		if(_propList.containsKey("text")) _propList.put("tooltip", _propList.get("text").toString());
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if( _barcodeValue == null || _barcodeValue.equals("")) return null;
				else _propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? _propList.get("showLabel").toString().equals("true") : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 8 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("upc") && _barcodeData.length() != 11 ){
					_barcodeSrc="";
				}
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") || _className.equals("UBTaximeter")|| _className.equals("UBCandleStickChart")|| _className.equals("UBPlotChart") || _className.equals("UBRadarChart") )
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			
			if(_className.equals("UBTaximeter")){
				PARAM = getChartParamToElement4Taximeter(currentItemData.get("Element").getElementValue() );	
				PARAM+=","+rowIndex;
			}else if(_className.equals("UBLineChart")){
				PARAM = getLineChartParamToElement( currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBRadarChart")){
				PARAM = getChartParamToElement4Radar(currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBCandleStickChart")){
				PARAM = getCandleChartParamToElement(currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBColumnChart")){
				PARAM = getColumnChartParamToElement(currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBBarChart")){
				PARAM = getColumnChartParamToElement(currentItemData.get("Element").getElementValue() );	
			}else if(_className.equals("UBPieChart")){
				PARAM = getPieChartParamToElement(currentItemData.get("Element").getElementValue() );
			}else{
				PARAM = getChartParamToElement(currentItemData.get("Element").getElementValue() );
			}
			
			
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToElement2(currentItemData.get("Element").getElementValue() );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			else if(_className.equals("UBTaximeter"))
			{
				_propList.put("type" , "TaximeterCtl");
				IMG_TYPE = "taximeter";
			}
			else if(_className.equals("UBRadarChart"))
			{
				_propList.put("type" , "radarChartCtl");
				IMG_TYPE = "radar";
			}
			else if(_className.equals("UBCandleStickChart"))
			{
				_propList.put("type" , "candleChartCtl");
				IMG_TYPE = "candle";
			}
			else if(_className.equals("UBPlotChart"))
			{
				_propList.put("type" , "plotChartCtl");
				IMG_TYPE = "plot";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				String _dataIDs = currentItemData.get("dataSets").getStringValue();				
				String [] arrDataId = _dataIDs.split(",");
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = DataSet.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			{			
				ArrayList<HashMap<String, Object>> _list = DataSet.get( _dataID );		
				
				if(!"".equals(IMG_TYPE) && _list != null )
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			//_barcodeValue = URLDecoder.decode(_chartValue, "UTF-8");
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		else if( "UBStretchLabel".equals(_className) )
		{
			// StretchLabel일때 height계산하여 height를 업데이트하고 
			// text를 줄바꿈 처리하고 진행
			_propList = convertStrechLabel(_propList);
			
		}
		
		// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
		if(_propList.containsKey("text"))
		{
			String _svalue = _propList.get("text").toString();
			_propList.put("tooltip", _svalue);
		}
		
		
		if("UBGraphicsRectangle".equals(_className) || "UBGraphicsCircle".equals(_className) || "UBGraphicsGradiantRectangle".equals(_className) )
		{
			_propList.put("angle" , _propList.get("rotation") );
			_propList.put("stroke" , _propList.get("borderColor").toString() );
			_propList.put("strokeWidth" , Integer.valueOf( _propList.get("borderThickness").toString() ) );
			_propList.put("scaleX" , 1);
			_propList.put("scaleY" , 1);
			
			_propList.put("width", Float.valueOf(_propList.get("width").toString()));
			_propList.put("height", Float.valueOf(_propList.get("height").toString()));
			
			if( "UBGraphicsCircle".equals(_className) )
			{
				_propList.put("radius", Float.valueOf( Float.valueOf(_propList.get("width").toString())/2 ));
				_propList.put("scaleY", Float.valueOf( Float.valueOf(_propList.get("height").toString())/Float.valueOf(_propList.get("width").toString()) ));
			}
		}
		
		//crossTab을 table로 내보내기 테스트
		if( currentItemData.containsKey("isTable"))
		{
			_propList.put("isTable", currentItemData.get("isTable").getStringValue());
		}
		if( currentItemData.containsKey("TABLE_ID"))
		{
			_propList.put("TABLE_ID", currentItemData.get("TABLE_ID").getStringValue());
		}
		
		if("UBRotateLabel".equals(_className))
		{
			_propList.put("rotate" , _propList.get("rotation") );
		}
		
		if(_propList.containsKey("visible") && _propList.get("visible").equals("false"))
		{
			return null;
		}
		
		_propList.put("className" , _className );
		_propList.put("id" , _itemId );
		
		
		// radioButtonGroup
		if( _className.equals("UBRadioBorder") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			String _groupName= _propList.get("groupName").toString();
			_propList.put("groupName", _groupName +_propList.get("SUFFIX_ID") );
			
			Boolean _isSelected = radiobuttonHandler(_propList);
			_propList.put("selected", _isSelected);
		}else if( _className.equals("UBRadioButtonGroup") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			
			radiobuttonGroupHandler(_propList);
		}
		
		// 아이템의 사용 여부 확인 
		_propList = ItemPropertyProcess.checkedItemProperties(_propList);
		
		
		return _propList;
	}
	
	public HashMap<String, Object> convertItemDataJsonSimpleExport( BandInfoMapData _bandInfo, HashMap<String, Value> currentItemData, HashMap<String, 
			ArrayList<HashMap<String, Object>>>  _dataSet, int rowIndex ,HashMap<String, Object> _param, int _startIndex, 
			int _lastIndex , int _totalPageNum , int _currentPage, String _dataName, float _cloneX, float _cloneY, float _updateY ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		String _className 		= "";
		String _name 		= "";
		Object _value 		= null;
		String _dataID 		= "";
		int k = 0;
		
		String _model_type = "";
		String _barcode_type = "";
		String _itemId = "";
		
	    Date _resultTimeStr = null;
	    Value _tempValue;
	    String _tempString;
	    
	    _tempValue = currentItemData.get("id");
		if(_tempValue != null)
		{
			_itemId = _tempValue.getStringValue();
		}
		
		if(GlobalVariableData.USE_DEBUG_LOG)
		{
			_resultTimeStr = new Date();
			log.debug("=============== ITEM " + _itemId + " PARSING START : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_resultTimeStr) +"]" );
		}
		
		if(_bandInfo != null)
		{
			if( _dataName == null || _dataName.equals("") )
			{
				
				if( currentItemData.get("dataSet") != null ){
					if(   currentItemData.get("dataSet").getStringValue() != null ){
						if(  !currentItemData.get("dataSet").getStringValue().equals("") ){
							_dataID =  currentItemData.get("dataSet").getStringValue();
						}
					}
				}
				
				if( _bandInfo.getDataSet().equals("") == false && _bandInfo.getAdjustableHeight() && _bandInfo.getResizeText() )
				{
					_dataID = _bandInfo.getDataSet();
					
					if( mOriginalDataMap.get( _dataID ) != null && currentItemData.get("dataSet") != null && currentItemData.get("dataSet").getStringValue() != null && currentItemData.get("dataSet").getStringValue().equals("null") == false )
					{
						if( mOriginalDataMap.get( _dataID ).equals(currentItemData.get("dataSet").getStringValue()) == false )
						{
							_dataID = currentItemData.get("dataSet").getStringValue();
						}
					}
				}
				
			}
			else
			{
				if( mOriginalDataMap.get( _dataName ) != null && currentItemData.get("dataSet") != null && currentItemData.get("dataSet").getStringValue() != null && currentItemData.get("dataSet").getStringValue().equals("null") == false )
				{
					if( mOriginalDataMap.get( _dataName ).equals(currentItemData.get("dataSet").getStringValue()) == false )
					{
						_dataID = currentItemData.get("dataSet").getStringValue();
					}
					else
					{
						_dataID = _dataName;	
					}
				}
				else
				{
					_dataID = _dataName;
				}
			}
		}
		else if( currentItemData.containsKey("dataSet") && _dataSet.containsKey(currentItemData.get("dataSet").getStringValue()) )
		{
			_dataID = currentItemData.get("dataSet").getStringValue();
		}

		HashMap<String, Object> _propList = new HashMap<String, Object>();
		
		_className = currentItemData.get("className").getStringValue();
		// image variable
		String _prefix="";
		String _suffix="";
					
		Boolean _bType = false;

		if(_className.equals("UBLabelBorder"))
		{
			_bType = true;
		}
		else
		{
			_bType = false;
		}
		
		if( _dataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
			

		Set<String> _keySet = currentItemData.keySet();
		Object[] hmKeys = _keySet.toArray();
		
		// system function
		String _systemFunction="";
		
		// formatter variables 
		String _formatter="";
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";
		String _inputForamtString = "";
		String _outputFormatString = "";
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		
		// edit formatter variables (e-form)
		String _formatterE="";
		String _nationE="";
		String _alignE="";
		String _maskE="";
		int _decimalPointLengthE=0;
		Boolean _useThousandCommaE=false;
		Boolean _isDecimalE=false;
		String _formatStringE="";

		
		// 1. 실제 Export 할때 필요한 최소한의 속성만을 담아두기
		// 2. 최소한의 속성을 이용하여 생성된 hashmap를 담기
		// 3. currentItemData에 해당 hashMap를 담아두고 처리한다
		// 4. hashmap이 이미 존재할경우 해당 map을 clone후 text/ x / y / width / height 변경  
		
//		String[] _propArray = {"fontFamily","fontColor","fontSize","textAlign","verticalAlign","borderColors","lineHeight","borderType","text","systemFunction",
//				"borderSide","type","barcodeType","borderSide","checked","borderThickness","borderWeight","formatter","systemFunction","dataType", "band_y"
//				,"width","height","x","y","isCell","src","data","rotation","contentBackgroundColors", "contentBackgroundColor", "clipArtData","checked","prefix","suffix"
//				,"leftBorderType","rightBorderType","topBorderType","bottomBorderType", "startPoint" ,"borderColor", "endPoint","showLabel","label" };
		
		// 에디트 포맷터 값이존재할경우각각의 데이터를 담는 처리
		// kyh formatter property setting
		HashMap<String, Object> _editFormatValueMap = null;
		_tempValue = currentItemData.get("editItemFormatValue");
		
		if( _tempValue != null )
		{
			_editFormatValueMap = (HashMap<String, Object>) _tempValue.getMapValue();
		}	
		
		_tempValue = currentItemData.get("ITEM_DATA");
		if(  _tempValue != null  )
		{
			_propList = (HashMap<String, Object>)  _tempValue.getMapValue().clone();
			
			// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
			Object _temp;
			_temp = _propList.get("rowId");
			if( _temp != null )
			{
				_propList.put("rowId", rowIndex);
			}
			
			_systemFunction = getPropertyValueString(_propList,"systemFunction","");
			_formatterE = getPropertyValueString(_propList,"editItemFormatter","");
			_model_type = getPropertyValueString(_propList,"type","");
			_barcode_type = getPropertyValueString(_propList,"barcodeType","");
			_prefix = getPropertyValueString(_propList,"prefix","");
			_dataType = getPropertyValueString(_propList,"dataType","");
			_suffix = getPropertyValueString(_propList,"suffix","");
			
			// formatter속성 담기
			_formatter = getPropertyValueString(_propList,"formatter","");
			_mask = getPropertyValueString(_propList,"mask","");
			_decimalPointLength = Integer.parseInt( getPropertyValueString(_propList,"decimalPointLength","0") );
			_useThousandComma = getPropertyValueString(_propList,"useThousandComma","").equals("true");
			_isDecimal = getPropertyValueString(_propList,"isDecimal","").equals("true");
			_formatString = getPropertyValueString(_propList,"formatString","");
			_nation = getPropertyValueString(_propList,"nation","");
			_align = getPropertyValueString(_propList,"currencyAlign","");
			_inputForamtString = getPropertyValueString(_propList,"inputFormatString","");
			_outputFormatString = getPropertyValueString(_propList,"outputFormatString","");
		}
		else
		{
			_propList = mItemPropVar.getItemName(_className);
			
			if(_propList == null ) return null;
			
			String[] _propArray = ItemPropertyVariable.getSimpleItemProperty(_className);

			// E-Form 컨텐츠 아이템일 경우 자신의 rowIndex값을 담아두기
			if( _propList.containsKey("rowId") )
			{
				_propList.put("rowId", rowIndex);
			}
			
			for ( k = 0; k < _propArray.length; k++) {
				_name = (String) _propArray[k];
				
				_tempValue = currentItemData.get(_name);
				if( _tempValue == null ) continue;
				
				_value = _tempValue.getValue();
				
				if(_propList.containsKey(_name))
				{
					if( _name.equals("fontFamily"))
					{
						_value = URLDecoder.decode((String)_value, "UTF-8");
						if(common.isValidateFontFamily((String)_value))
							_propList.put(_name, _value);
						else
							_propList.put(_name, "Arial");
					}
					else if( _name.equals("contentBackgroundColors")  )
					{
						_value = URLDecoder.decode((String)_value, "UTF-8");
						
						ArrayList<String> _arrStr = new ArrayList<String>();
						_arrStr = mPropertyFn.getColorArrayString( (String)_value );
						_propList.put(_name, _arrStr);
						
						_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
						_propList.put((_name + "Int"), _arrStr);
						
					}
					else if( _name.equals("contentBackgroundAlphas") )
					{
						_value = URLDecoder.decode((String)_value, "UTF-8");
						
						ArrayList<String> _arrStr = new ArrayList<String>();
						_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
						_propList.put(_name, _arrStr);
						
					}
					else if( _name.indexOf("Color") != -1 && _name.equals("borderColors") == false && _name.equals("borderColorsInt") == false)
					{	
						//backgroundColor/fontColor 과 같이 color값이 ArrayList로 생성되어 있을경우 rowIndex값에 맞춰 color값을 변경
						if( _value.toString().contains(",") )
						{
							ArrayList<String> _valueArray = Value.setArrayString( _value.toString() );
							_value = _valueArray.get(rowIndex%_valueArray.size());
							_propList.put((_name + "Int"), _value);
							
							_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
							_propList.put(_name, _value);
						}
						else
						{
							_propList.put((_name + "Int"), _value);
							
							_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
							_propList.put(_name, _value);
						}
					}
					else if( _name.equals("lineHeight"))
					{
						//_value = "1.16"; //TODO LineHeight Test
						if( _value.toString().indexOf("%") != -1 )
						{
							_value = _value.toString().replace("%25", "").replace("%", "");
							_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
						}
						_propList.put(_name, _value);
					}
					else if( _name.equals("label"))
					{
						_propList.put(_name, _value);
					}
					else if( _name.equals("borderType"))
					{
						_propList.put(_name, _value);
					}
					else if( _name.equals("text"))
					{
						_propList.put(_name, _value == null ? "" : _value);
					}
					else if( _name.equals("prompt"))
					{
						_value = URLDecoder.decode(_value.toString(), "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.equals("value"))
					{
						_value = URLDecoder.decode(_value.toString(), "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.equals("borderSide"))
					{
						ArrayList<String> _bSide = new ArrayList<String>();
						if( currentItemData.get(_name).getStringValue().equals("none") == false )
						{
							
							_bSide = mPropertyFn.getBorderSideToArrayList( currentItemData.get(_name).getStringValue() );

							if( _bSide.size() > 0)
							{
								String _type = (String) _propList.get("borderType");
								_type = mPropertyFn.getBorderType(_type);
								_propList.put("borderType", _type);
							}
						}

						_propList.put(_name, _bSide);
					}
					else if( _name.equals("type") )
					{
						_model_type = _value.toString();
						_propList.put(_name, _value);
					}
					else if( _name.equals("barcodeType") )
					{
						_barcode_type = _value.toString();
						_propList.put(_name, _value);
					}
					else if( _name.equals("clipArtData") )
					{
						_propList.put(_name, _value + ".svg");
					}
					else if( _name.equals("dataProvider") )
					{
						_value = URLDecoder.decode(_value.toString(), "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.equals("ubHyperLinkUrl") )
					{
						_value = URLDecoder.decode(_value.toString(), "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.equals("ubHyperLinkText") )
					{
						_value = URLDecoder.decode(_value.toString(), "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.equals("fileDownloadUrl") )
					{
						_value = URLDecoder.decode(_value.toString(), "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.equals("band_y") )
					{
						_propList.put(_name, _value);
					}
					else if( _name.equals("x"))
					{
						_propList.put(_name, _value);
						_propList.put("left", _value);
					}
					else if( _name.equals("y"))
					{
						_propList.put(_name, _value);
						_propList.put("top", _value);
					}
					else if( _name.equals("selectedDate") )
					{
						/*
						 * DateField 의 날짜 값을 순수 숫자로 치환한다.
						 * 웹에디터에 dateFormat 속성을 변경할 수 없어 "yyyy-MM-dd"로 고정. 변경될 경우 수정필요.
						 * ex)
						 * 원래 값 : Thu Oct 6 13:31:12 GMT+0900 2016
						 * 변경된 값 : 2016-10-06
						*/
						_value = URLDecoder.decode(_value.toString(), "UTF-8");
						String ubDateFormat = "yyyy-MM-dd";
						SimpleDateFormat beforeFormat = new SimpleDateFormat("EEE MMM d kk:mm:ss 'GMT'Z yyyy",Locale.US);
						SimpleDateFormat afterFormat = new SimpleDateFormat(ubDateFormat);
						String convertedDateString = "";
						try {
							Date gmtDate = beforeFormat.parse(_value.toString());
							convertedDateString = afterFormat.format(gmtDate);
						} catch (ParseException e) {
							log.error(getClass().getName()+"::convertElementToItem::"+"DateField selectedDate parsing Fail.>>>"+e.getMessage());
						} finally{
							_propList.put(_name, convertedDateString);
							_propList.put("text", convertedDateString);
						}
					}
					else if(_name.equals("column"))
					{
						_propList.put(_name, URLDecoder.decode(_value.toString(), "UTF-8") );
					}
					else if(_name.equals("dataSet"))
					{
						_propList.put(_name, URLDecoder.decode(_value.toString(), "UTF-8") );
					}
					else
					{
						_propList.put(_name, _value);
					}
				}
				else if( _name.equals("checked") )
				{
					_propList.put("selected", _value);
				}
				else if( _name.equals("conerRadius") )
				{
					_propList.put("rx", _value);
					_propList.put("ry", _value);
				}
				else if( _name.equals("points")  )
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					
					ArrayList<String> _arrStr = new ArrayList<String>();
					_arrStr = mPropertyFn.getPathArrayString( _value.toString() );
					_propList.put("path", _arrStr);
				}
				else if( _name.equals("band_y") )
				{
					_propList.put(_name, _value);
				}
				else if(_name.equals("borderThickness"))
				{
					_propList.put("borderWidth", _value);
				}
				else if(_name.equals("borderWeight"))
				{
					_propList.put("borderWidth", _value);
				}else if( _name.equals("formatter") ){
					_formatter = _value.toString();
					_propList.put("formatter", _formatter);
				}else if( _name.equals("systemFunction") ){
					_systemFunction = _value.toString();
					_propList.put("systemFunction", _systemFunction);
				}else if( _name.equals("editItemFormatter") )
				{
					_formatterE = _value.toString();
					_propList.put("editItemFormatter", _formatterE);
				}
				else if(_name.equals("dataType"))
				{
					_dataType = _value.toString();
					_propList.put(_name, _value);
				}
				
				else if( _name.equals("data") )
				{
					if(_className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture"))
					{
						_propList.put("src",  URLEncoder.encode(_value.toString(), "UTF-8"));
					}
				}
				else if( _name.equals("ubToolTip") ){
					// tooltip 현재는 text가 아닌 ubToolTip 속성값을 이용시 데이터셋의 경우 dataset_0.col_0 과 같이 담겨서 tooltip가 제대로 표시안되는 현상으로 임시 주석. 2016-11-17 최명진
					
					if(_className.equals("UBImage")) {
						_value = URLDecoder.decode(_value.toString(), "UTF-8");
						_propList.put("tooltip",  _value);
					}
				}
				else if( _name.equals("prefix") ){
					try {
						_prefix=URLDecoder.decode(_value.toString(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				else if( _name.equals("suffix") ){
					_suffix=_value.toString();
				}
				else if( _name.equals("leftBorderType") ||  _name.equals("rightBorderType") ||  _name.equals("topBorderType") ||  _name.equals("bottomBorderType") )
				{
					_propList.put(_name, _value);
				}
				// 명우 추가 
				else if(_name.equals("startPoint"))
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					String[] _sPoint = _value.toString().split(",");
					_propList.put("x1", Float.valueOf(_sPoint[0]));
					_propList.put("y1", Float.valueOf(_sPoint[1]));
					
				}
				else if(_name.equals("endPoint"))
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					String[] _ePoint = _value.toString().split(",");

					_propList.put("x2", Float.valueOf(_ePoint[0]));
					_propList.put("y2", Float.valueOf(_ePoint[1]));
					
				}
				else if( _name.equals("width"))
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("height"))
				{
					_propList.put(_name, _value);
				}
				else if(_name.equals("lineThickness"))
				{
					_propList.put("thickness", _value);
				}
				else if( _name.equals("rWidth") ||  _name.equals("rHeight") )
				{
					_propList.put(_name, _value);
				}
				else if( _name.equals("printVisible") )
				{
					if( ("PRINT".equals(isExportType) || "PDF".equals(isExportType)) && "false".equals(_value) )
					{
						return null;
					}
				}
				else if( _name.equals("markanyVisible") )
				{
					if( _isMarkAny && "PRINT".equals(isExportType) && "false".equals(_value) )
					{
						return null;
					}
				}
				else if( _name.equals("rotation") )
				{
					_propList.put(_name, _value);
					_propList.put("rotate", _value);
				}
				
				else if( _name.equals("columnAxisName") || _name.equals("lineAxisName") )
				{
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					_propList.put(_name, _value);
				}
				else if( _name.equals("categoryMargin") || _name.equals("itemMargin")|| _name.equals("xFieldVisible")|| _name.equals("yFieldVisible") )
				{
					_propList.put(_name, _value);
				}
			}
			
			if( _className.toUpperCase().indexOf("LINE") != -1)
			{
//				if( _propList.containsKey("x1") )
//				{
//					_propList.put("x1", Float.parseFloat(_propList.get("x").toString()) + Float.parseFloat(_propList.get("x1").toString()) );
//				}
//				if( _propList.containsKey("y1") )
//				{
//					_propList.put("y1", Float.parseFloat(_propList.get("y").toString()) + Float.parseFloat(_propList.get("y1").toString()) );
//				}
//				if( _propList.containsKey("x2") )
//				{
//					_propList.put("x2", Float.parseFloat(_propList.get("x").toString()) + Float.parseFloat(_propList.get("x2").toString()) );
//				}
//				if( _propList.containsKey("y2") )
//				{
//					_propList.put("y2", Float.parseFloat(_propList.get("y").toString()) + Float.parseFloat(_propList.get("y2").toString()) );
//				}
			}
			else
			{
				_propList.put("x1", _propList.get("x"));
				_propList.put("y1", _propList.get("y"));
				_propList.put("x2", _propList.get("width"));
				_propList.put("y2", _propList.get("height"));
			}
			
			_tempValue = currentItemData.get("PRESERVE_ASPECT_RATIO");
			
			if(_tempValue != null)
			{
				_propList.put("PRESERVE_ASPECT_RATIO", _tempValue.getStringValue());
			}
			
			if( _editFormatValueMap != null && !_editFormatValueMap.isEmpty() )
			{	
				String _eformatDataset=null;
				String _eformatKeyField=null;
				String _eformatLabelField=null;
				
				for( String _key : _editFormatValueMap.keySet() ){
					
					String _propName = _key;
					String _propValue = _editFormatValueMap.get(_key).toString();
					
					if( _propName.equals("nation") ){
						_nationE = URLDecoder.decode(_propValue, "UTF-8");
						_propList.put("eformnation", 	_nationE );
					}
					else if( _propName.equals("mask") ){
						_maskE = URLDecoder.decode(_propValue, "UTF-8");
						_propList.put("eformmask", 	_maskE );
					}
					else if( _propName.equals("decimalPointLength") ){
						_decimalPointLengthE = common.ParseIntNullChk(_propValue, 0);
						
						_propList.put("eformdecimalPointLength", 	_decimalPointLengthE );
						
					}				
					else if( _propName.equals("useThousandComma") ){
						_useThousandCommaE = Boolean.parseBoolean(_propValue);
						
						_propList.put("eformuseThousandComma", 	_useThousandCommaE );
					}		
					else if( _propName.equals("isDecimal") ){
						_isDecimalE = Boolean.parseBoolean(_propValue);
						_propList.put("eformisDecimal", _isDecimalE	 );
					}		
					else if( _propName.equals("formatString") ){
						_formatStringE = _propValue;
						_propList.put("eformformatString", _formatStringE	 );
					}else if( _propName.equals("dataProvider") ){
						_propValue = URLDecoder.decode(_propValue, "UTF-8");
						_propList.put("eformDataProvider", _propValue	 );
					}else if( _propName.equals("dataset") ){
						_eformatDataset = _propValue;
					}else if( _propName.equals("keyField") ){
						_eformatKeyField = _propValue;
					}else if( _propName.equals("valueField") ){
						_eformatLabelField = _propValue;
					}
				}
				
			}
			
			HashMap<String, Object> _formatValueMap = null;
			
			_tempValue = currentItemData.get("formatValue");
			if( _tempValue!=null )
			{
				_formatValueMap = (HashMap<String, Object>) _tempValue.getMapValue();
			}
			
			if( _formatValueMap != null && !_formatValueMap.isEmpty() )
			{
				for(String _key : _formatValueMap.keySet())
				{
					String _formatPropertyName = _key;
					String _formatPropertyValue =_formatValueMap.get(_key).toString();
					
					try {
						_formatPropertyValue =URLDecoder.decode(_formatPropertyValue, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(_formatPropertyName.equals("nation"))
					{
						_nation = _formatPropertyValue;
					}
					else if(_formatPropertyName.equals("align"))
					{
						_align=_formatPropertyValue;
					}
					else if( _formatPropertyName.equals("mask") ){
						_mask = _formatPropertyValue;
					}
					else if( _formatPropertyName.equals("decimalPointLength") ){
						if(  _formatPropertyValue.equalsIgnoreCase("NaN") ){
							_decimalPointLength = 0;
						}else{
							_decimalPointLength = common.ParseIntNullChk(_formatPropertyValue, 0);	
						}
					}				
					else if( _formatPropertyName.equals("useThousandComma") ){
						_useThousandComma = Boolean.parseBoolean(_formatPropertyValue);
					}		
					else if( _formatPropertyName.equals("isDecimal") ){
						_isDecimal = Boolean.parseBoolean(_formatPropertyValue);
					}		
					else if( _formatPropertyName.equals("formatString") ){
						_formatString = _formatPropertyValue;
					}
					else if( _formatPropertyName.equals("inputFormatString") )
					{
						_inputForamtString = URLDecoder.decode(_formatPropertyValue , "UTF-8");
					}
					else if( _formatPropertyName.equals("outputFormatString") )
					{
						_outputFormatString =  URLDecoder.decode(_formatPropertyValue , "UTF-8");
					}
				}
			}
			
			if( !_formatter.equals("") )
			{
				// format이 label band에서 안들어간다.
				_propList.put("formatter", _formatter);
				_propList.put("mask", _mask);
				_propList.put("decimalPointLength", _decimalPointLength);
				_propList.put("useThousandComma", _useThousandComma);
				_propList.put("isDecimal", _isDecimal);
				_propList.put("formatString", _formatString);
				_propList.put("nation", _nation);
				_propList.put("currencyAlign", _align);
				_propList.put("inputFormatString", _inputForamtString);
				_propList.put("outputFormatString", _outputFormatString);
			}
			
			currentItemData.put("ITEM_DATA", new Value(_propList.clone(), "map") );
		}
		
		if(GlobalVariableData.USE_DEBUG_LOG)
		{
			_resultTimeStr = new Date();
			log.debug("=============== ITEM " + _itemId + " GET PROPERTIES END : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_resultTimeStr) +"]" );
		}
		
		// Item의 changeData가 있는지 확인
		if(mChangeItemList != null )
		{
			_propList = convertChangeItemDataText( _currentPage ,_propList, "");
		}
		
		// 보더업데이트
		_tempString = getPropertyValueString(_propList, "isCell", "");
		
		if( _tempString.equals("false") )
		{
			_propList = convertItemToBorder(_propList);
		}
		
		_tempValue = currentItemData.get("ORIGINAL_TABLE_ID");
		if( _tempValue != null )
		{
			_propList.put("ORIGINAL_TABLE_ID", _tempValue.getStringValue() );
		}
		
		_tempValue = currentItemData.get("beforeBorderType");
		if( _tempValue != null )
		{
			_propList.put("beforeBorderType", _tempValue.getArrayBooleanValue());
		}
		
		if(GlobalVariableData.USE_DEBUG_LOG)
		{
			_resultTimeStr = new Date();
			log.debug("=============== ITEM " + _itemId + " GET BORDER END : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_resultTimeStr) +"]" );
		}
		
		//Table의 UBFX가 존재할경우 처리( Table의 ubfx를 먼저 처리후 Cell의 ubfx를 처리 )
		Value _tempUbfxValue = currentItemData.get("ubfx");
		Value _tempTableUbfxValue = currentItemData.get("tableUbfunction");
		if( _tempUbfxValue != null || _tempTableUbfxValue != null )
		{
			ArrayList<HashMap<String, String>> _ubfxs = new ArrayList<HashMap<String, String>>();
			
			ArrayList<ArrayList<HashMap<String, String>>> _ubfxsList = new ArrayList<ArrayList<HashMap<String, String>>>();
			
			int _ubfxListCnt = 0;
			
			if( _tempTableUbfxValue != null  )
			{
				ArrayList<HashMap<String, String>> _tblUbfxs = (ArrayList<HashMap<String, String>>) _tempTableUbfxValue.getValue();
				_ubfxsList.add( _tblUbfxs );
			}
			
			if( _tempUbfxValue != null )
			{
				_ubfxs = (ArrayList<HashMap<String, String>>) _tempUbfxValue.getValue();
				_ubfxsList.add( _ubfxs );
			}
			
			int _nodeCnts = _ubfxsList.size();
			
			for(int _ubfxListIndex= 0; _ubfxListIndex < _nodeCnts; _ubfxListIndex++)
			{
				ArrayList<HashMap<String, String>> _selectUbfxList = _ubfxsList.get(_ubfxListIndex);
				int _ubfxListSize = (_selectUbfxList!=null)?_selectUbfxList.size():0;
				
				for(int _ubfxIndex = 0; _ubfxIndex < _ubfxListSize; _ubfxIndex++)
				{
					HashMap<String, String> _ubfxItem = _selectUbfxList.get(_ubfxIndex);
					String _ubfxProperty = _ubfxItem.get("property");
					String _ubfxValue = _ubfxItem.get("value");
					try {
						_ubfxValue = URLDecoder.decode(_ubfxValue, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					ArrayList<HashMap<String, Object>> _pList = _dataSet.get(_dataID);
					String _datasetColumnName = currentItemData.get("column")==null ? "" : currentItemData.get("column").getStringValue();
					mFunction.setDatasetList(_dataSet);
					mFunction.setParam(_param);
					
					mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
					if(mGroupTotalPageIndex>0) mFunction.setSectionCurrentPageNum(mGroupCurrentPageIndex);
					mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
					if(mGroupTotalPageIndex>0) mFunction.setSectionTotalPageNum(mGroupTotalPageIndex);
					mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
					mFunction.setOriginalDataMap(mOriginalDataMap);
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,_dataName);
					}else{
						_fnValue = mFunction.function(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,_dataName);
					}
					
					_fnValue = _fnValue.trim();
					
					if( _ubfxProperty.equals("text") || _fnValue.equals("") == false)
					{
						_propList = convertUbfxStyle(_ubfxProperty, _fnValue, _propList );
					}
				}
			}
			
		}
		
		_tempValue = currentItemData.get("ubHyperLinkType");
		//hyperLinkedParam처리
		if( _tempValue != null && "2".equals( _tempValue.getStringValue() )  )
		{
			ArrayList<HashMap<String, String>> _hyperLinkedParam = (ArrayList<HashMap<String, String>>) currentItemData.get("ubHyperLinkParm").getObjectValue();
			HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
			
			if( _hyperLinkedParam != null && _hyperLinkedParam.size() > 0 )
			{
				int _hyperLinkedParamSize = _hyperLinkedParam.size();
				
				HashMap<String, String> _hMap;
				
				for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
				{
					_hMap = _hyperLinkedParam.get(_hyperIdx);
					String _hyperParamKey = "";
					String _hyperParamValue = "";
					String _hyperParamType = "";
					
					_hyperParamKey = getPropertyValueString(_hMap, "id", "");
					_hyperParamValue = getPropertyValueString(_hMap, "value", "");
					_hyperParamType = getPropertyValueString(_hMap, "type", "");
					
					
					if( "DataSet".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						
						_hyperParamValue = "";
						ArrayList<HashMap<String, Object>> _list = _dataSet.get( _hyperLinkedDataSetId );
						
						Object _dataValue = "";
						if( _list != null ){
							if( rowIndex < _list.size() )
							{
								HashMap<String, Object> _dataHm = _list.get(rowIndex);
								_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
							}
						}
					}
					else if("Parameter".equals(_hyperParamType) )
					{
						String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
						String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

						String _pValue = _pList.get("parameter");

						if( _pValue.equals("undefined"))
						{
							_hyperParamValue = "";
						}
						else
						{
							_hyperParamValue = _pValue;
						}
					}
					
					_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
				}
				
				_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
			}
			
		}
		
		Value _tmpDataType = currentItemData.get("dataType");
		Value _tmpDataColumn = currentItemData.get("column");
		Value _tmpDataSet= currentItemData.get("dataSet");
				
		if( _tmpDataType != null && _tmpDataSet != null )	
		{
			String columnStr =  _tmpDataColumn.getStringValue();
			String dataTypeStr = _tmpDataType.getStringValue();
			
			_tmpDataType = currentItemData.get("dataType_N");
			if( _tmpDataType != null && _tmpDataType.getStringValue().equals("") == false )
			{
				dataTypeStr = _tmpDataType.getStringValue();
			}
			
			 _tmpDataColumn = currentItemData.get("column_N");
			if( _tmpDataColumn != null && _tmpDataColumn.getStringValue().equals("") == false )
			{
				columnStr = _tmpDataColumn.getStringValue();
			}
			
			if( dataTypeStr.equals("1") )
			{
				ArrayList<HashMap<String, Object>> _list = _dataSet.get( _dataID );
				Object _dataValue = "";
				if( _list != null ){
					if( rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						_dataValue = _dataHm.get( columnStr );
					}
				}
				
				// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐.. 
				if(_className.equals("UBSVGArea") ){
					
					if(_dataValue==null || _dataValue.toString().equals("")) return null;
					
					String _tmpDataValue = _dataValue.toString();
					
					if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
					{
						_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
					}
					
					boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
					boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
					boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
					
					if(_bSVG)
					{
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);	
					}
					else
					{
						boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
						if(!_bHasHtmlTag)
							_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
							
						_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
						_dataValue = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							
					}
					log.debug("13168-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _dataValue.toString() + "]");
					
					_dataValue = _dataValue.toString().replace(" ", "%20");
					
					if( !_dataValue.toString().equals("") )
					{
						_propList.put("data",  URLEncoder.encode(_dataValue.toString(), "UTF-8"));
					}
					else
					{
						return null;
					}
					
					
				}
				else if( _className.equals("UBSVGRichText") )
				{
					// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
					if( _dataValue != null && _dataValue.toString().equals("") == false )
					{
						_propList = convertUBSvgItem(_dataValue,_propList);
						
						if(_propList == null ) return null;
					}
				}
				
				
				else if("UBCheckBox".equals(_className) )
				{
					String _selectedText=_propList.get("selectedText").toString();
					//String _deSelectedText=_propList.get("deSelectedText").toString();
					if( _dataValue != null && _selectedText.equalsIgnoreCase(_dataValue.toString()) ){
						_propList.put("selected", "true");
					}else{
						_propList.put("selected", "false");	
					}
					
				}	
				else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") )
				{
					String _servicesUrl = convertImageData( _dataValue.toString() , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
					_propList.put("src", _servicesUrl);
				}
				else
				{
					_propList.put("text", _dataValue == null ? "" : _dataValue);
					
					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					_propList.put("tooltip", _propList.get("text"));
				}
				
			}
			else if( dataTypeStr.equals("2"))
			{
 				if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
					
					ArrayList<HashMap<String, Object>> _pList = _dataSet.get( _dataID );
					String _datasetColumnName = columnStr;
					mFunction.setDatasetList(_dataSet);
					mFunction.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
					mFunction.setGroupTotalPageIndex(mGroupTotalPageIndex);
					mFunction.setGroupDataNamesAr(mGroupDataNamesAr);
					mFunction.setOriginalDataMap(mOriginalDataMap);
					
					
					String _fnValue;
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_systemFunction , rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName);
					}else{
						_fnValue = mFunction.function(_systemFunction,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName );
					}
					

					if( _className.equals("UBSVGRichText") )
					{
						if( _fnValue != null && _fnValue.equals("") == false )
						{
							// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
							_propList = convertUBSvgItem(_fnValue,_propList);
							
							if(_propList == null ) return null;
						}

					}
					else if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
						
						String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
						_propList.put("src", _servicesUrl);
						
					}
					else if(_className.equals("UBCheckBox") ) {
						_propList.put("selected", _fnValue);
					}
					else
					{
						_propList.put("text", _fnValue == null ? "" : _fnValue);
					}
				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			else if( dataTypeStr.equals("3"))
			{
				String _txt = _propList.get("text").toString();
				
				int _inOf = _txt.indexOf("{param:");
				String _pKey = "";
				String _fnValue = "";
				if( _inOf != -1 )
				{
					mFunction.setParam(_param);
					_txt=mFunction.replaceParameterValue(_txt);
					_inOf = _txt.indexOf("{param:");
					if( _inOf != 0 ){
						
						// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
						if(_className.equals("UBSVGArea")  ){
							
							String _tmpDataValue = String.valueOf(_txt);
							
							if( _tmpDataValue.indexOf("&lt;") != -1 || _tmpDataValue.indexOf("&gt;") != -1 )
							{
								_tmpDataValue = common.decodeHtmlSpecialChars( _tmpDataValue );
							}
							
							boolean _bSVG = (_tmpDataValue != null && ( _tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
							boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )? _propList.get("preserveAspectRatio").toString().equals("true"):false;
							boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
							
							String _svgTag = null;
							if(_bSVG)
							{
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
							}
							else
							{
								boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
								if(!_bHasHtmlTag)
									_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
									
								_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
								_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
									
							}
													
							log.debug("13315-" + getClass().getName()+"::convertElementToItem::"+"SVG=[" + _svgTag + "]");
							
							_svgTag = _svgTag.toString().replace(" ", "%20");
							if( !_svgTag.equals("") )
							{
								_propList.put("data",  URLEncoder.encode(_svgTag, "UTF-8"));
							}
							else
							{
								return null;
							}
							
							_txt = "";
						}
						else if( _className.equals("UBSVGRichText") )
						{
							if( _txt != null && _txt.equals("") == false )
							{
								_propList = convertUBSvgItem( _txt, _propList);
								
								if(_propList == null ) return null;
							}
							
							_txt = "";
						}
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = _txt;
						}else{
							_fnValue = mFunction.function(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , _dataName );
						}
						
					}else{

						int _keyIndex=_txt.lastIndexOf("}");
						_pKey = _txt.substring(_inOf + 7 , _keyIndex);

						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

						_fnValue = _pList.get("parameter");

					}
					
					if( _className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture") ) {
						_fnValue = URLDecoder.decode(_fnValue.replace(" ", "%20"), "UTF-8");
						
						String _servicesUrl = convertImageData( _fnValue , _prefix, _suffix, Float.valueOf(_propList.get("width").toString()).intValue(), Float.valueOf(_propList.get("height").toString()).intValue() );
						_propList.put("src", _servicesUrl);
					}
					else
					{
						if( _fnValue.equals("undefined"))
						{
							_propList.put("text", "");
						}
						else
						{
							_propList.put("text", _fnValue);
						}
					}
					

				}
				else
				{
					_propList.put("text", "");
				}
				
				
			}
			
		}
		
		if(GlobalVariableData.USE_DEBUG_LOG)
		{
			_resultTimeStr = new Date();
			log.debug("=============== ITEM " + _itemId + " DATASET PARSING END : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_resultTimeStr) +"]" );
			
		}
		
		// formatter set	// ITem의 Formatter 처리 ( export처리를 위하여 속성값 담기 )
		if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
			Object _propValue;
			String _formatValue="";
			_propValue=_propList.get("text");
			_formatValue = _propValue.toString();
			String _excelFormatterStr = "";
			try {
				if( _formatter.equalsIgnoreCase("Currency") ){
					_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					_excelFormatterStr = _nation  + "§" + _align;
					
				}else if( _formatter.equalsIgnoreCase("Date") ){
					_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					_excelFormatterStr = _formatString;
					
				}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
					_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					_excelFormatterStr = _mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal;
					
				}else if( _formatter.equalsIgnoreCase("MaskString") ){
					_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
				}
                else if( _formatter.equalsIgnoreCase("CustomDate") )
				{
					_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
					_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
					
					_propList.put("inputFormatString", _inputForamtString);
					_propList.put("outputFormatString", _outputFormatString);
				}
				
			} catch (ParseException e) {
				//e.printStackTrace();
			}
			
			if( isExportType.equals("EXCEL") && _excelFormatterStr.equals("") == false && common.getPropertyValue("excelExport.useFormatter") != null && common.getPropertyValue("excelExport.useFormatter").equals("true") ) 
			{
				_propList.put("EX_FORMATTER", _formatter);
				_propList.put("EX_FORMAT_DATA_STR", _excelFormatterStr);
				_propList.put("EX_FORMAT_ORIGINAL_STR", _propValue.toString() );
			}
			
			_propList.put("text", _formatValue);
		}
		
		if( _editFormatValueMap != null && !_editFormatValueMap.isEmpty() )
		{	
			String _eformatDataset=null;
			String _eformatKeyField=null;
			String _eformatLabelField=null;
			
			if( _formatterE.equals("SelectMenu") ){
				
				// dataset으로 comboBox 표현.
				if( _eformatDataset != null &&  (!_eformatDataset.equals("null")) && _dataSet.containsKey(_eformatDataset) ){
					
					String _efText = _propList.get("text").toString();
					Boolean _hasValueKey = false;
					
					ArrayList<HashMap<String, Object>> _list = _dataSet.get(_eformatDataset);
					
					HashMap<String, Object> _dataHm;
					Object _keyData;
					Object _labelData;
					
					JSONArray ja = new JSONArray();
					String _jsonStr=null;
					JSONObject jo;
					String _keyStr=null;
					String _labelStr=null;
					
					for( int _eformatIdx=0; _eformatIdx<_list.size(); _eformatIdx++ ){
						_dataHm = _list.get(_eformatIdx);
						_keyData = _dataHm.get(_eformatKeyField);
						_labelData = _dataHm.get(_eformatLabelField);
						_keyStr=_keyData.toString();
						_labelStr = _labelData.toString();
						
						if( _efText.equals(_keyStr) && _hasValueKey == false ){
							_hasValueKey = true;
							_propList.put("text", _labelStr );
						}
						
						jo = new JSONObject();
						jo.put("label", _labelStr);
						jo.put("value",_keyStr );
						ja.add(jo);
					}
					
					_jsonStr = ja.toJSONString();
					
					_propList.put("eformDataProvider", _jsonStr	 );
				}else{
					
					if( _propList.containsKey("eformDataProvider") == false && _propList.get("eformDataProvider") == null )
					{
						_propList.put("eformDataProvider", "[]");
					}
					
					String _jsonStr = _propList.get("eformDataProvider").toString(); 
					JSONParser jsonParser = new JSONParser();
					try {
						
						JSONArray ja = (JSONArray) jsonParser.parse(_jsonStr);
						
						String _efText = _propList.get("text").toString();
						JSONObject oj;
						for( int jsonIdx=0; jsonIdx<ja.size(); jsonIdx++ ){
							oj=(JSONObject) ja.get(jsonIdx);
							if( oj.get("value").equals(_efText) ){
								_propList.put("text", oj.get("label") );
								break;
							}
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		if(GlobalVariableData.USE_DEBUG_LOG)
		{
			_resultTimeStr = new Date();
			log.debug("=============== ITEM " + _itemId + " FORMATTER END : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_resultTimeStr) +"]" );
		}
		
		//ResizeFont 값이 true이고 adjustableHeight값이 true 일경우 처리
		_tempValue = currentItemData.get("resizeFont");
		String _propText = getPropertyValueString(_propList, "text", "");
		if(  "".equals(_propText) == false && _tempValue != null && _tempValue.getBooleanValue() )
		{
			
			_tempString = getPropertyValueString(_propList, "padding", "");
			
			float _fontSize 	= Float.valueOf( _propList.get("fontSize").toString() );
			String _fontFamily 	= _propList.get("fontFamily").toString();
			String _fontWeight 	= _propList.get("fontWeight").toString();
			float _padding = ( "".equals(_tempString) == false )? Float.valueOf( _tempString ):3;
			
			float _maxBorderSize = 0;
			
			ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
			
			if( _borderWidths != null )
			{
				//ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _propList.get("borderWidths");
				
				for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
					if(_maxBorderSize < _borderWidths.get(_bIndex))
					{
						_maxBorderSize = _borderWidths.get(_bIndex);
					}
				}
				_padding = _maxBorderSize + _padding;
			}
			
			float _itemWidth 	= Float.valueOf( _propList.get("width").toString() )- (2 * _padding);
			
			
			_fontSize = StringUtil.getTextMatchWidthFontSize( _propList.get("text").toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize);
			_propList.put("fontSize",  _fontSize);
		}
		
		
		
		if( _itemId.equals("") == false )
		{
			_propList.put("TABINDEX_ID", _itemId + "_"+ "_ROW"+rowIndex);
			_propList.put("SUFFIX_ID", "_" + "_ROW"+rowIndex);
		}
		
		
		if( _propText.equals("") == false )
			_propList.put("tooltip", _propText);
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
			int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
			int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
			
			if(_className.equals("UBQRCode"))
			{
				_propList.put("type" , "qrcodeSvgCtl");
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = "false";
			    	String IMG_TYPE = "qrcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _propList.get("text").toString();
			    	
			    	try {
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if( _barcodeValue == null || _barcodeValue.equals("")) return null;
				else _propList.put("src", "svg:" + URLEncoder.encode(_barcodeValue, "UTF-8")); 
			}
			else
			{
				boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
				
				String _barcodeData = _propList.get("text").toString();
				String _barcodeSrc;
				if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 8 ){
					_barcodeSrc="";
				}else if( _barcode_type.equalsIgnoreCase("upc") && _barcodeData.length() != 11 ){
					_barcodeSrc="";
				}
				else
				{
					if(StringUtil.containsKorean(_barcodeData))
					{
						_barcodeSrc="";
					}
					else
					{
						if("datamatrix".equals(_barcode_type))
						{	
							_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
						}
						_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
					}
				}
				
				// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
				String _barcodeValue = "";
				if(!"".equals(_barcodeSrc))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
			    	String SHOW_LABEL = _showLabel ? "true" : "false";
			    	String IMG_TYPE = "barcode";
			    	String MODEL_TYPE = _barcode_type;
			    	String FILE_CONTENT = _barcodeData;
			    	
			    	try {
			    		if("datamatrix".equals(MODEL_TYPE))
						{	
			    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
						}
						_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
			}		
		}
		else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") || _className.equals("UBTaximeter")|| _className.equals("UBCandleStickChart")|| _className.equals("UBPlotChart") || _className.equals("UBRadarChart") )
		{
			String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
			String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
			String IMG_TYPE = "";
			String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
			
			HashMap<Integer, String> displayNamesMap=null;
			
			if(_className.equals("UBTaximeter")){
				PARAM = getChartParamToJson4Taximeter(currentItemData);	
				PARAM+=","+rowIndex;
			}else if(_className.equals("UBLineChart")){
				PARAM = getChartParamToJson(currentItemData );
			}else if(_className.equals("UBRadarChart")){
				PARAM = getChartParamToJson4Radar(currentItemData );	
			}else if(_className.equals("UBCandleStickChart")){
				PARAM = getChartParamToJson(currentItemData );
			}else if(_className.equals("UBColumnChart")){
				PARAM = getChartParamToJson(currentItemData );	
			}else if(_className.equals("UBBarChart")){
				PARAM = getChartParamToJson(currentItemData );	
			}else if(_className.equals("UBPieChart")){
				PARAM = getChartParamToJson(currentItemData );
			}else{
				PARAM = getChartParamToJson(currentItemData);
			}
			
			
			if(_className.equals("UBPieChart"))
			{
				_propList.put("type" , "pieChartCtl");
				IMG_TYPE = "pie";
			}
			else if(_className.equals("UBLineChart"))
			{
				_propList.put("type" , "lineChartCtl");
				IMG_TYPE = "line";
			}
			else if(_className.equals("UBBarChart"))
			{
				_propList.put("type" , "barChartCtl");
				IMG_TYPE = "bar";
			}
			else if(_className.equals("UBColumnChart"))
			{
				_propList.put("type" , "columnChartCtl");
				IMG_TYPE = "column";
			}
			else if(_className.equals("UBAreaChart"))
			{
				_propList.put("type" , "areaChartCtl");
				IMG_TYPE = "area";
			}
			else if(_className.equals("UBCombinedColumnChart"))
			{
				displayNamesMap  = getChartParamToJson2(currentItemData );
				_propList.put("type" , "combinedColumnChartCtl");
				IMG_TYPE = "combcolumn";
			}
			else if(_className.equals("UBBubbleChart"))
			{
				_propList.put("type" , "bubbleChartCtl");
				IMG_TYPE = "bubble";
			}
			else if(_className.equals("UBTaximeter"))
			{
				_propList.put("type" , "TaximeterCtl");
				IMG_TYPE = "taximeter";
			}
			else if(_className.equals("UBRadarChart"))
			{
				_propList.put("type" , "radarChartCtl");
				IMG_TYPE = "radar";
			}
			else if(_className.equals("UBCandleStickChart"))
			{
				_propList.put("type" , "candleChartCtl");
				IMG_TYPE = "candle";
			}
			else if(_className.equals("UBPlotChart"))
			{
				_propList.put("type" , "plotChartCtl");
				IMG_TYPE = "plot";
			}
			
			String _chartValue = "";
			if(IMG_TYPE.equals("combcolumn"))
			{
				String _dataIDs = currentItemData.get("dataSets").getStringValue();				
				String [] arrDataId = _dataIDs.split(",");
				
				ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				for(int i=0; i< arrDataId.length; i++)
				{
					ArrayList<HashMap<String, Object>> _list = _dataSet.get(arrDataId[i]);
					_dslist.add(_list);
				}
				
				if(!"".equals(IMG_TYPE))
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else
			{			
				ArrayList<HashMap<String, Object>> _list = _dataSet.get( _dataID );		
				if(!"".equals(IMG_TYPE) && _list != null )
				{
					//ViewerInfo5 vi5 = new ViewerInfo5();
					String FILE_NAME = this.mChartDataFileName;
					//String DATA_ID = _dataID;
					String MODEL_TYPE = _model_type;
			    	
					int _itmWidth = Float.valueOf(_propList.get("width").toString()).intValue();
					int _itmheight = Float.valueOf(_propList.get("height").toString()).intValue();
					
			    	try {
			    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
		}
		else if( "UBStretchLabel".equals(_className) )
		{
			// StretchLabel일때 height계산하여 height를 업데이트하고 
			// text를 줄바꿈 처리하고 진행
			_propList = convertStrechLabel(_propList);
			
		}
		
		// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
		if(_propList.containsKey("text"))
		{
			String _svalue = _propList.get("text").toString();
			_propList.put("tooltip", _svalue);
		}
		
		
		if("UBGraphicsRectangle".equals(_className) || "UBGraphicsCircle".equals(_className) || "UBGraphicsGradiantRectangle".equals(_className))
		{
			_propList.put("angle" , _propList.get("rotation") );
			_propList.put("stroke" , _propList.get("borderColor").toString() );
			_propList.put("strokeWidth" , Integer.valueOf( _propList.get("borderThickness").toString() ) );
			_propList.put("scaleX" , 1);
			_propList.put("scaleY" , 1);
			
			_propList.put("width", Float.valueOf(_propList.get("width").toString()));
			_propList.put("height", Float.valueOf(_propList.get("height").toString()));
			
			if( "UBGraphicsCircle".equals(_className) )
			{
				_propList.put("radius", Float.valueOf( Float.valueOf(_propList.get("width").toString())/2 ));
				_propList.put("scaleY", Float.valueOf( Float.valueOf(_propList.get("height").toString())/Float.valueOf(_propList.get("width").toString()) ));
			}
		}
		
		float _updateX = 0;
		if(  _updateY > -1 )
		{
			_propList.put("y", _updateY);
			_propList.put("top", _updateY);
		}
		else if( _cloneY != 0 )
		{
			_propList.put("y", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
			_propList.put("top", _cloneY + Float.valueOf( _propList.get("y").toString() ) );
		}
		
		if( _cloneX != 0 )
		{
			_updateX = _cloneX + Float.valueOf( _propList.get("x").toString() );
			_propList.put("x", _updateX);
			_propList.put("left", _updateX);
		}
		
		//crossTab을 table로 내보내기 테스트
		_tempValue = currentItemData.get("isTable");
		if( _tempValue != null )
		{
			_propList.put("isTable", currentItemData.get("isTable").getStringValue());
		}
		
		_tempValue = currentItemData.get("TABLE_ID");
		if( _tempValue != null )
		{
			_propList.put("TABLE_ID", _tempValue.getStringValue());
		}
		
		if("UBRotateLabel".equals(_className))
		{
			_propList.put("rotate" , _propList.get("rotation") );
		}
		
		_tempString = getPropertyValueString(_propList, "visible", "");
		if(_tempString.equals("false"))
		{
			return null;
		}
		
		_propList.put("className" , _className );
		_propList.put("id" , _itemId );
		
		// radioButtonGroup
		if( _className.equals("UBRadioBorder") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			String _groupName= _propList.get("groupName").toString();
			_propList.put("groupName", _groupName +_propList.get("SUFFIX_ID") );
			
			Boolean _isSelected = radiobuttonHandler(_propList);
			_propList.put("selected", _isSelected);
		}else if( _className.equals("UBRadioButtonGroup") ){
			
			_propList.put("id", _propList.get("TABINDEX_ID"));
			
			radiobuttonGroupHandler(_propList);
		}
		
		// 아이템의 사용 여부 확인 
		_propList = ItemPropertyProcess.checkedItemProperties(_propList);
		
		if( GlobalVariableData.USE_DEBUG_LOG )
		{
			_resultTimeStr = new Date();
			log.debug("=============== ITEM " + _itemId + " PARSING END : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_resultTimeStr) +"]");
		}
		
		return _propList;
	}
	
	public static String getPropertyValueString( HashMap _prop, String _key, String _defValue )
	{
		Object _result = _prop.get(_key);
		
		if( _result != null )
		{
			return _result.toString();
		}
		else
		{
			return _defValue;
		}
	}
	

	private static final String[] removePropoerties = {"CELLS","TableMap","isTable","rotate","borderType","LOAD_TYPE","band","TABLE_ID","rotation","realClassName","SUFFIX_ID","className","borderOriginalTypes","realTableID","ORIGINAL_TABLE_ID","TABINDEX_ID","band_x","band_y","rowIndex","columnIndex"};
	private static final String[] removeViewPropoerties = {"cellOutHeight","cellOverHeight","currentAdjustableHeight","CELLS","TableMap","isTable","rotate","borderType","LOAD_TYPE","band","TABLE_ID","rotation","realClassName","SUFFIX_ID","className","borderOriginalTypes","realTableID","ORIGINAL_TABLE_ID","TABINDEX_ID","band_x","band_y","rowIndex","columnIndex"};
	private static final ArrayList<String> removePropoertiesList = new ArrayList<String>(Arrays.asList(removePropoerties));
	private static final ArrayList<String> removeViewerPropoertiesList = new ArrayList<String>(Arrays.asList(removeViewPropoerties));
	
	private HashMap<String, Object> convertItemProp( HashMap<String, Object> _propList, int rowIndex, boolean _isMarkAny ) throws UnsupportedEncodingException
	{
		Object _value;
		String _className = _propList.get("className").toString();
		HashMap<String, Object> _cloneProp = null;
		
		if( _propList.containsKey("OUTPUT_ITEM_DATA") )
		{
			_cloneProp = ( HashMap<String, Object>) (( HashMap<String, Object>) _propList.get("OUTPUT_ITEM_DATA")).clone();
			
			return convertCloneItem( _propList , _cloneProp );
			//return _cloneProp;
		}
		
        Iterator<String> names = _propList.keySet().iterator();
        _cloneProp = ( HashMap<String, Object>) _propList.clone();
        
        Object _typeObj = UBComponent.getProperties(_cloneProp, _className, "type", "");
        if( _typeObj != null ) _cloneProp.put("type", _typeObj);
        
        while( names.hasNext() ){
        		
        	String _name = names.next();
        	_value = _propList.get(_name);
			if( _value != null && _value instanceof String ) _value = URLDecoder.decode(_value.toString(), "UTF-8");
			
			if( _value == null )
			{
				continue;
			}
			
//			if( removePropoertiesList.indexOf(_name) != -1 )
			if( removePropoertiesList.indexOf(_name) != -1 || ( isExportType.equals("") && removeViewerPropoertiesList.indexOf(_name) != -1 ) )
			{
					_cloneProp.remove(_name);
					continue;
			}
			
			if( _name.equals("fontFamily"))
			{
				_value = URLDecoder.decode((String)_value, "UTF-8");
				if(common.isValidateFontFamily((String)_value))
					_cloneProp.put(_name, _value);
				else
					_cloneProp.put(_name, "Arial");
			}
			else if( _name.equals("contentBackgroundColors")  )
			{
				ArrayList<String> _arrStr = new ArrayList<String>();
				
				if( _value instanceof String )
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					
					
					_arrStr = mPropertyFn.getColorArrayString( (String)_value );
					_cloneProp.put(_name, _arrStr);
					
					_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
					_cloneProp.put((_name + "Int"), _arrStr);
				}
				else
				{
					_arrStr = mPropertyFn.getColorArrayString( (ArrayList<String>)_value );
					_cloneProp.put(_name, _arrStr);
					
					_arrStr = mPropertyFn.getBorderSideToArrayList((ArrayList<String>)_value);
					_cloneProp.put((_name + "Int"), _arrStr);
				}
//				_value = URLDecoder.decode((String)_value, "UTF-8");
//				
//				ArrayList<String> _arrStr = new ArrayList<String>();
//				_arrStr = mPropertyFn.getColorArrayString( (String)_value );
//				_cloneProp.put(_name, _arrStr);
//				
//				_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
//				_cloneProp.put((_name + "Int"), _arrStr);
				
			}
			else if( _name.equals("contentBackgroundAlphas") )
			{
				ArrayList<String> _arrStr = new ArrayList<String>();
				if( _value instanceof String )
				{
					_value = URLDecoder.decode((String)_value, "UTF-8");
					_arrStr = mPropertyFn.getBorderSideToArrayList((String)_value);
				}
				else
				{
					_arrStr = mPropertyFn.getBorderSideToArrayList( (ArrayList<String>)_value );
				}
				_cloneProp.put(_name, _arrStr);
				
			}
			else if( _name.indexOf("Color") != -1 && _name.equals("borderColors") == false && _name.equals("borderColorsInt") == false)
			{	
				//backgroundColor/fontColor 과 같이 color값이 ArrayList로 생성되어 있을경우 rowIndex값에 맞춰 color값을 변경
				if( _value.toString().contains(",") )
				{
					ArrayList<String> _valueArray = Value.setArrayString( _value.toString() );
					_value = _valueArray.get(rowIndex%_valueArray.size());
					_cloneProp.put((_name + "Int"), _value);
					
					_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
					_cloneProp.put(_name, _value);
				}
				else
				{
					_cloneProp.put((_name + "Int"), _value);
					
					_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value.toString()));
					_cloneProp.put(_name, _value);
				}
			}
			else if( _name.equals("lineHeight"))
			{
				//_value = "1.16"; //TODO LineHeight Test
				if( _value.toString().indexOf("%") != -1 )
				{
					_value = _value.toString().replace("%25", "").replace("%", "");
					_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
				}
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("label"))
			{
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("borderType"))
			{
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("text"))
			{
				_cloneProp.put(_name, _value == null ? "" : _value);
			}
			else if( _name.equals("prompt"))
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("value"))
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("borderSide"))
			{
				ArrayList<String> _bSide = new ArrayList<String>();
				
				if( _value != null && _value instanceof String )
				{
					if( _value.toString().equals("none") == false )
					{
						_bSide = mPropertyFn.getBorderSideToArrayList(_value.toString());
						
						String _type = ( _propList.get("borderType")!=null )?_propList.get("borderType").toString():"";
						_type = mPropertyFn.getBorderType(_type);
						_cloneProp.put("borderType", _type);
					}
				}
				else if( _value != null && _value.toString().equals("none") == false )
				{
					_bSide = (ArrayList<String>) _value;

					if( _bSide.size() > 0)
					{
						String _type = ( _propList.get("borderType")!=null )?_propList.get("borderType").toString():"";
						_type = mPropertyFn.getBorderType(_type);
						_cloneProp.put("borderType", _type);
					}
				}

				_cloneProp.put(_name, _bSide);
			}
			else if( _name.equals("type") )
			{
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("barcodeType") )
			{
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("clipArtData") )
			{
				_cloneProp.put(_name, _value + ".svg");
			}
			else if( _name.equals("dataProvider") )
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("ubHyperLinkUrl") )
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("ubHyperLinkText") )
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("fileDownloadUrl") )
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("band_y") )
			{
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("x"))
			{
				_cloneProp.put(_name, _value);
				_cloneProp.put("left", _value);
			}
			else if( _name.equals("y"))
			{
				_cloneProp.put(_name, _value);
				_cloneProp.put("top", _value);
			}
			else if( _name.equals("selectedDate") )
			{
				/*
				 * DateField 의 날짜 값을 순수 숫자로 치환한다.
				 * 웹에디터에 dateFormat 속성을 변경할 수 없어 "yyyy-MM-dd"로 고정. 변경될 경우 수정필요.
				 * ex)
				 * 원래 값 : Thu Oct 6 13:31:12 GMT+0900 2016
				 * 변경된 값 : 2016-10-06
				*/
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				String ubDateFormat = "yyyy-MM-dd";
				SimpleDateFormat beforeFormat = new SimpleDateFormat("EEE MMM d kk:mm:ss 'GMT'Z yyyy",Locale.US);
				SimpleDateFormat afterFormat = new SimpleDateFormat(ubDateFormat);
				String convertedDateString = "";
				try {
					Date gmtDate = beforeFormat.parse(_value.toString());
					convertedDateString = afterFormat.format(gmtDate);
				} catch (ParseException e) {
					log.error(getClass().getName()+"::convertElementToItem::"+"DateField selectedDate parsing Fail.>>>"+e.getMessage());
				} finally{
					_cloneProp.put(_name, convertedDateString);
					_cloneProp.put("text", convertedDateString);
				}
			}
			else if(_name.equals("column"))
			{
				_cloneProp.put(_name, URLDecoder.decode(_value.toString(), "UTF-8") );
			}
			else if(_name.equals("dataSet"))
			{
				_cloneProp.put(_name, URLDecoder.decode(_value.toString(), "UTF-8") );
			}
			else if( _name.equals("checked") )
			{
				_cloneProp.put("selected", _value);
			}
			else if( _name.equals("conerRadius") )
			{
				_cloneProp.put("rx", _value);
				_cloneProp.put("ry", _value);
			}
			else if( _name.equals("points")  )
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				
				ArrayList<String> _arrStr = new ArrayList<String>();
				_arrStr = mPropertyFn.getPathArrayString( _value.toString() );
				_cloneProp.put("path", _arrStr);
			}
			else if( _name.equals("band_y") )
			{
				_cloneProp.put(_name, _value);
			}
			else if(_name.equals("borderThickness"))
			{
				_cloneProp.put("borderWidth", _value);
			}
			else if(_name.equals("borderWeight"))
			{
				_cloneProp.put("borderWidth", _value);
			}
			else if( _name.equals("formatter") ){
				_cloneProp.put("formatter",  _value.toString());
			}
			else if( _name.equals("editItemFormatter") )
			{
				_cloneProp.put("editItemFormatter", _value.toString());
			}
			else if(_name.equals("dataType"))
			{
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("data") )
			{
				if(_className.equals("UBImage") || _className.equals("UBSignature")|| _className.equals("UBTextSignature")  || _className.equals("UBPicture"))
				{
					_cloneProp.put("src",  URLEncoder.encode(_value.toString(), "UTF-8"));
				}
			}
			else if( _name.equals("ubToolTip") ){
				// tooltip 현재는 text가 아닌 ubToolTip 속성값을 이용시 데이터셋의 경우 dataset_0.col_0 과 같이 담겨서 tooltip가 제대로 표시안되는 현상으로 임시 주석. 2016-11-17 최명진
				
				if(_className.equals("UBImage")) {
					_value = URLDecoder.decode(_value.toString(), "UTF-8");
					_cloneProp.put("tooltip",  _value);
				}
			}
			else if( _name.equals("leftBorderType") ||  _name.equals("rightBorderType") ||  _name.equals("topBorderType") ||  _name.equals("bottomBorderType") )
			{
				_cloneProp.put(_name, _value);
			}
			// 명우 추가 
			else if(_name.equals("startPoint"))
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				String[] _sPoint = _value.toString().split(",");
				_cloneProp.put("x1", Float.valueOf(_sPoint[0]));
				_cloneProp.put("y1", Float.valueOf(_sPoint[1]));
			}
			else if(_name.equals("endPoint"))
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				String[] _ePoint = _value.toString().split(",");
				
				_cloneProp.put("x2", Float.valueOf(_ePoint[0]));
				_cloneProp.put("y2", Float.valueOf(_ePoint[1]));
			}
			else if( _name.equals("width"))
			{
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("height"))
			{
				_cloneProp.put(_name, _value);
			}
			else if(_name.equals("lineThickness"))
			{
				_cloneProp.put("thickness", _value);
			}
			else if( _name.equals("rWidth") ||  _name.equals("rHeight") )
			{
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("printVisible") )
			{
				if( ("PRINT".equals(isExportType) || "PDF".equals(isExportType)) && "false".equals(_value) )
				{
					return null;
				}
			}
			else if( _name.equals("markanyVisible") )
			{
				if( _isMarkAny && "PRINT".equals(isExportType) && "false".equals(_value) )
				{
					return null;
				}
			}
			else if( _name.equals("rotation") )
			{
				_cloneProp.put(_name, _value);
				_cloneProp.put("rotate", _value);
			}
			
			else if( _name.equals("columnAxisName") || _name.equals("lineAxisName") )
			{
				_value = URLDecoder.decode(_value.toString(), "UTF-8");
				_cloneProp.put(_name, _value);
			}
			else if( _name.equals("categoryMargin") || _name.equals("itemMargin")|| _name.equals("xFieldVisible")|| _name.equals("yFieldVisible") )
			{
				_cloneProp.put(_name, _value);
			}
			else
			{
				_cloneProp.put(_name, _value);
			}
		}
		
        _cloneProp.put("className", _propList.get("className"));
        _propList.put("OUTPUT_ITEM_DATA", _cloneProp);
        
		return convertCloneItem(_propList, _cloneProp );
	}
	
	
	/**
	 * String타입의 데이터이외에 데이터가 clone햇음에도 동일한 주소값을 참조하는 경우가 발생하여 
	 * 별도로 값이 변경되는 속성은 따로 데이터 지정하는 방법으로 처리 
	 **/
	private HashMap<String, Object> convertCloneItem( HashMap<String, Object> _item , HashMap<String, Object> _cloneItem )
	{
		if( _item.containsKey("height"))
		{
			_cloneItem.put("height", cloneFloatValue(_item.get("height")) );
		}

		if( _item.containsKey("width"))
		{
			_cloneItem.put("width", cloneFloatValue(_item.get("width")) );
		}
		
		if( _item.containsKey("fontSize"))
		{
			_cloneItem.put("fontSize", cloneFloatValue(_item.get("fontSize")) );
		}
		
		return _cloneItem;
	}
	
	
	private float cloneFloatValue( Object _value )
	{
		float _retVal = -1;
		
		_retVal = Float.valueOf(_value.toString());
		
		return _retVal;
	}
	
	
}	
