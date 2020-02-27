package org.ubstorm.service.parser.formparser.data;

import java.awt.Dimension;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.fit.cssbox.demo.ImageRenderer;
import org.json.simple.JSONObject;
//import org.ubstorm.service.DataServiceManager;
import org.ubstorm.service.formatter.UBFormatter;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.logger.Log;
//import org.ubstorm.service.method.ViewerInfo5;
import org.ubstorm.service.parser.DataSetProcess;
import org.ubstorm.service.parser.ItemPropertyProcess;
import org.ubstorm.service.parser.ItemPropertyVariable;
import org.ubstorm.service.parser.formparser.ItemConvertParser;
import org.ubstorm.service.parser.formparser.UBIDataUtilPraser;
//import org.ubstorm.service.request.ServiceRequestManager;
import org.ubstorm.service.utils.ImageUtil;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TempletItemInfo {
	// Templet 

	private String mSERVER_URL;
	private String CLIENT_EDIT_MODE = "OFF"; 
	
	private String mProjectName = "";
	private String mFormName = "";
	private String mFx = "";
	private float mX = 0;
	private float mY = 0;
	private float mWidth = 0;
	private float mHeight = 0;
	private String mFileName = "Mview.ubx";
	private JSONObject mParam;
	
	private String mBandID = "";
	private float mBand_Y = 0;
	
	private int mMinimumResizeFontSize = -1;
	
	private Document _document;
	private String mIsType = "";
	private HashMap<String, ArrayList<HashMap<String, Object>>> mDataSet;
	
	private DataSetProcess mDataSetFn;
//	private ServiceRequestManager mServiceReqMng;
	private BandInfoMapData mBandInfoMap = null;
	private String isExportType = "";
	
	private boolean mIsMarkAny = false;
	
	private boolean mIsAutoHeight = false;
	
	private ArrayList<HashMap<String, Value>> mItems = new ArrayList<HashMap<String, Value>>();
	private ItemPropertyVariable mItemPropVar = new ItemPropertyVariable();
	private ItemPropertyProcess mPropertyFn = new ItemPropertyProcess();
	
	private String mChartDataFileName = "chartdata.dat";
	
	private JSONObject mHashMap;
	
	private String mPageID = "";
	
	private boolean useDataParameter = false;
	
	private JSONObject mBackgroundImageObj = null;
	private String EXTERNAL_PROJECT_ID = "";
	
	public float getHeight()
	{
		return mHeight;
	}
	public float getWidth()
	{
		return mWidth;
	}
	public float getBandY()
	{
		return mBand_Y;
	}
	public boolean getAutoHeight()
	{
		return mIsAutoHeight;
	}
	public void setPageID( String _value )
	{
		mPageID = _value;
	}
	public String getPageID()
	{
		return mPageID;
	}
	
	public boolean getUseDataParameter()
	{
		return useDataParameter;
	}
	
	public HashMap<String, HashMap<String, String>> getParameterData()
	{
		if( mHashMap != null && mHashMap.containsKey("param"))
		{
			return (HashMap<String, HashMap<String, String>>) mHashMap.get("param");
		}

		return null; 
	}
	
	/**
	public JSONObject getBackgroundImage()
	{
		if( mBackgroundImageObj == null || mBackgroundImageObj.isEmpty() ) return null;
		
		return mBackgroundImageObj;
	}
	*/
	
	
	// 1. templet 을 읽어들여서 projectName와 formName을 가져오기
	//public TempletItemInfo( Element _element , JSONObject _param , ServiceRequestManager _serviceReq, String _isExportType, boolean _isMarkAny, String _externalProjectId ) {
	public TempletItemInfo( Element _element , JSONObject _param , String _isExportType, boolean _isMarkAny, String _externalProjectId ) {		
		// TODO Auto-generated constructor stub
		mDataSetFn = new DataSetProcess();
		//mServiceReqMng = _serviceReq;
		mSERVER_URL = (String) _param.get("serverUrl");		
		mIsMarkAny = _isMarkAny;
		
		EXTERNAL_PROJECT_ID = _externalProjectId;
				
		NodeList _properties = _element.getElementsByTagName("property");
		Element _property;
		int _cnt = _properties.getLength();
		String _name = "";
		String _value = "";
		
		for (int i = 0; i < _cnt; i++) {
			
			_property = (Element) _properties.item(i);
			
			_name = _property.getAttribute("name");
			_value = _property.getAttribute("value");
			
			if( _name.equals("projectName") )
			{
				mProjectName = _value;
			}
			else if( _name.equals("formId") )
			{
				mFormName = _value;
			}
			else if( _name.equals("x") )
			{
				mX = Float.valueOf(_value);
			}
			else if( _name.equals("y") )
			{
				mY = Float.valueOf(_value);
			}
			else if( _name.equals("width") )
			{
				mWidth = Float.valueOf(_value);
			}
			else if( _name.equals("height") )
			{
				mHeight = Float.valueOf(_value);
			}
			else if( _name.equals("autoHeight") )
			{
				mIsAutoHeight = _value.equals("true");
			}
			else if( _name.equals("band_y") )
			{
				if( _value.equals("") == false && _value.equals("null") == false && !Float.valueOf( _value ).isNaN() )
				{
					mBand_Y = Float.valueOf( _value );
				}
			}
			else if( _name.equals("band") )
			{
				if(_value.equals("") == false && _value.equals("null") == false ) mBandID = _value;
			}
			
		}
		
//		mFx 			= _element.getAttribute("fx");
		mParam 			= _param;
		
		isExportType = _isExportType;
		
		try {
			loadDataSet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// 1. templet 을 읽어들여서 projectName와 formName을 가져오기
	//public TempletItemInfo( HashMap<String, Object> _item , JSONObject _param , ServiceRequestManager _serviceReq, String _isExportType, boolean _isMarkAny ) {
	public TempletItemInfo( HashMap<String, Object> _item , JSONObject _param , String _isExportType, boolean _isMarkAny ) {
		// TODO Auto-generated constructor stub
		mDataSetFn = new DataSetProcess();
		//mServiceReqMng = _serviceReq;
		mSERVER_URL = (String) _param.get("serverUrl");		
		mIsMarkAny = _isMarkAny;
		
		String _name = "";
		String _value = "";
		
			if( _item.containsKey("projectName") )
			{
				mProjectName = _item.get("projectName").toString();
			}
			
			if( _item.containsKey("formId") )
			{
				mFormName =  _item.get("formId").toString();
			}
			
			if( _item.containsKey("x") )
			{
				mX =  Float.valueOf(_item.get("x").toString());
			}
			
			if( _item.containsKey("y") )
			{
				mY =  Float.valueOf(_item.get("y").toString());
			}
			
			if( _item.containsKey("width") )
			{
				mWidth =  Float.valueOf(_item.get("width").toString());
			}
			
			if( _item.containsKey("height") )
			{
				mHeight =  Float.valueOf(_item.get("height").toString());
			}

			if( _item.containsKey("autoHeight") )
			{
				mIsAutoHeight = _item.get("autoHeight").toString().equals("true");
			}
			
			if( _item.containsKey("band_y") )
			{
				if( _item.get("band_y").toString().equals("") == false &&  _item.get("band_y").toString().equals("null") == false && !Float.valueOf(  _item.get("band_y").toString() ).isNaN() )
				{
					mBand_Y =  Float.valueOf( _item.get("band_y").toString() );
				}
			}
			
			if( _item.containsKey("band") )
			{
				if(_item.get("band").toString().equals("") == false && _item.get("band").toString().equals("null") == false ) mBandID = _item.get("band").toString();
			}
			
		mParam 			= _param;
		isExportType = _isExportType;
		
		try {
			loadDataSet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	// 2. xml로드
	private void loadTempletXml() throws IOException, DataFormatException, SAXException, ParserConfigurationException
	{
		String _xmlStr = common.getUBFormXmlData(mProjectName, mFormName, mFileName, null, null, null, "/UEditor", "", "", EXTERNAL_PROJECT_ID);
		
		// XML 파서의 XML 외부 개체와 DTD 처리를 비활성화 합니다.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        
		InputSource _is = new InputSource(new StringReader( _xmlStr ));
		_document = factory.newDocumentBuilder().parse(_is);
		
		if( mIsAutoHeight)
		{
			mHeight = Float.valueOf(_document.getDocumentElement().getAttribute("pageHeight"));
		}
	}
	
	// 3. Item/Dataset 읽어들이기
	private boolean loadDataSet() throws Exception
	{
		// form명과 projectName를 이용하여 템플릿 읽어들이기
		loadTempletXml();
		
		// xml의 데이터셋 담아두기
		boolean _isChk = xmlToDataSet();
		
		// param타입일경우 리턴처리 
		if( !_isChk )
		{
			// Document 제거
			_document = null;
			return false;
		}
		// xml의 아이템정보들을 담아두기
		xmlToItemConvert();

		// Document 제거
		_document = null;
		
		return true;
	}
	
	HashMap<String, Element> marDataSetItems = new HashMap<String, Element>();
	HashMap<String, Element> marDataSetMerged = new HashMap<String, Element>();
	HashMap<String, HashMap<String, String>> mDataSetParam;
	
	// 전체 데이터셋의 Row수를 포함한 정보를 가져온다.
	private boolean xmlToDataSet() throws Exception
	{
		// xml DATASET;
		NodeList _itemList = _document.getElementsByTagName("item");
		
		int leng = _itemList.getLength();
		
		//mDataSetRowCountInfo = new HashMap<String, Integer>();
		
		for( int d = leng - 1 ; d > -1 ; d--)
		{
			Element _itemE = (Element) _itemList.item(d);
			
			String _className5 = _itemE.getAttribute("className").substring(0, 5); 
			String _dataId = _itemE.getAttribute("id");
			
			Element _mergedInfo = null;
			
			if( _className5.equals("UBDmc") )
			{
				if( _itemE.getElementsByTagName("mergedinfo") != null)
				{
					_mergedInfo = (Element) _itemE.getElementsByTagName("mergedinfo").item(0);
				}
				
				if( _mergedInfo != null  )
				{
					if( _mergedInfo.hasChildNodes() )
					{
						// merged 테그 담기.!!!;
						if( marDataSetMerged == null ) marDataSetMerged= new HashMap<String, Element>();
						marDataSetMerged.put(_dataId, _mergedInfo);
					}
				}
				
				if(marDataSetItems == null ) marDataSetItems = new HashMap<String, Element>();
				marDataSetItems.put(_dataId, _itemE);
			}
			else
			{
				break;
			}
		}
		
		mDataSetParam = paramCheck_Handler(mDataSetParam);
		
		if( mDataSetParam == null )
		{
			mIsType = "PARAM";
			useDataParameter = true;
			return false;
		}
		else
		{
			mIsType = "";
			useDataParameter = false;
			mDataSet = new HashMap< String, ArrayList<HashMap<String, Object>> >();
			//DataServiceManager oService = (this.mServiceReqMng != null)?this.mServiceReqMng.getServiceManager():null;	// null 처리 2016-03-10 최명진
			//mDataSet = mDataSetFn.dataSetLoad(oService, marDataSetItems , mDataSetParam , marDataSetMerged, "");		
			mDataSet = mDataSetFn.dataSetLoad(mSERVER_URL, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);	
		}
		return true;
		
	}
	
	
	// 4. Item전체를 담아다기
	
	// 5. DataSet 담아두기
	
	// 6. 페이지번호와 ArrayList를 넘겨받아서 아이템을 Add시키서 리턴하는 함수 생성
	
	
	
	/////////ETC
	private HashMap<String, HashMap<String, String>> paramCheck_Handler( HashMap<String, HashMap<String, String>> _param ) throws UnsupportedEncodingException
	{
		// param Check; 
		NodeList _paramList = _document.getElementsByTagName("param");

		HashMap<String, HashMap<String, String>> _paramMap = new HashMap<String, HashMap<String, String>>();
		if(_param != null ) _paramMap = _param;
		
		HashMap<String, String> _paramProp = null;

		for(int _p = 0; _p < _paramList.getLength() ; _p++)
		{
			Element _paramItem = (Element) _paramList.item(_p);
			
			if( "item".equals( _paramItem.getParentNode().getParentNode().getNodeName() ) ||  "project".equals( _paramItem.getParentNode().getParentNode().getNodeName() ) )
			{
				NodeList _paramPropertys = _paramItem.getElementsByTagName("property");
				
				_paramProp = new HashMap<String, String>();
				for(int _pp = 0; _pp < _paramPropertys.getLength(); _pp++)
				{
					Element _paramElement = (Element) _paramPropertys.item(_pp);
					
					String _paramName = _paramElement.getAttribute("name");
					String _paramValue = _paramElement.getAttribute("value");
					
					if( _paramName.equals("id"))
					{
						_paramMap.put(_paramValue, _paramProp);
					}
					else
					{
						_paramProp.put(_paramName, _paramValue);
					}
				}
			}

		}

		// Templet에 데이터셋이 사용되었을경우 파라미터를 체크하여 값이 없을경우 파라미터 입력 팝업이 뜨도록 처리 
		if( _paramMap.size() != 0 && checkSystemParam(mParam) )
		{
			mHashMap = new JSONObject();
			mHashMap.put("resultType","PARAM");
			mHashMap.put("param", _paramMap);
			return null;
		}
		else
		{
			if( _paramMap.size() != 0)
			{
				for(String key : _paramMap.keySet())
				{
					if( mParam.containsKey(key))
					{
						HashMap<String, String> _paValue = (HashMap<String, String>) mParam.get(key);

						_paramMap.put(key, _paValue );
					}
				}
			}
			//else
			if( mParam.size() != 0)
			{
				for(Object key : mParam.keySet())
				{
					if( mParam.containsKey(key))
					{
						if( isSystemParameter( key.toString() ) == false )
						{
							HashMap<String, String> _paValue = (HashMap<String, String>) mParam.get(key);
							_paramMap.put((String) key, _paValue );
						}
					}
				}
			}
		}

		return _paramMap;
	}
	
	private Boolean checkSystemParam( JSONObject _param)
	{
		int _chkIndex = 0;
		
		ArrayList<String> mNewList = new ArrayList<String>(Arrays.asList(GlobalVariableData.mSystemParams));
		
		for(Object key : _param.keySet())
		{
			if(mNewList.contains(key) == false)
			{
				_chkIndex = _chkIndex + 1;
			}
		}
		
		if(_chkIndex == 0 ) return true;
		
		return false;
	}
	
	private boolean isSystemParameter(String _paramKey )
	{
		ArrayList<String> _systemParams = new ArrayList<String>(Arrays.asList(GlobalVariableData.mSystemParams));
		
		if(  _systemParams.indexOf(_paramKey) == -1 )
		{
			return false;
		}
		return true;
	}
	
	private ArrayList<Float> mXAr = new ArrayList<Float>();
	
	private HashMap<String, Object> xmlToItemConvert() throws UnsupportedEncodingException, ScriptException
	{
		//Templet는 한페이지만
		NodeList _pageList = _document.getElementsByTagName("page");
		
		NodeList _projectList = _document.getElementsByTagName("project");
		Element _project = (Element) _projectList.item(0);
		String _fnVersion = _project.getAttribute("fnVersion");
		
		Element _page = (Element) _pageList.item(0);
		NodeList _child = _page.getElementsByTagName("item");
		int _childSize = _child.getLength();
		int i = 0;
		int l = 0;
		int k = 0;
		int j = 0;
		
		String _itemId = "";
		String _className = "";
		
		HashMap<String, Value> itemProperty = new HashMap<String, Value>();
		HashMap<String, Value> tableMapProperty = new HashMap<String, Value>();
 		HashMap<String, Value> tableProperty;// = new HashMap<String, Value>()
		String _itemDataSetName = "";
		String _propertyName = "";
		String _propertyValue = "";
		String _propertyType = "";
		float _tableArogHeight = 0f;
		NodeList _tablePropertys;
		NodeList _cellPropertys;
		NodeList _tableMaps;
		NodeList _tableMapDatas;
		NodeList _cells;
		Node rowHeightNode;
		Element _childItem;
		Element _tableMapItem;
		Element _cellItem;
		Element _ItemProperty;
		Element _tableMapPropertyElement;
		NodeList _propertys;
		
		int colIndex = 0;
		int rowIndex = 0;
		
		float updateX = 0;
		float updateY = 0;
		float bandY = 0;
		Function _fn = new Function( mDataSet , mParam);
		_fn.setFunctionVersion(_fnVersion);
		
		for(i = 0; i < _childSize ; i++)
		{
			_childItem = (Element) _child.item(i);
	
			_itemId = _childItem.getAttribute("id");
			_className = _childItem.getAttribute("className");
			
			if( _className.equals("UBTable") || _className.equals("UBApproval"))
			{
				tableProperty = new HashMap<String, Value>();
				ArrayList<HashMap<String, Value>> _ubApprovalAr = null;
				
				_tableArogHeight = 0;
				ArrayList<Object> borderAr = null;
				//Table의 Property를 담기
				_tablePropertys = _childItem.getChildNodes();
				boolean _newTalbeFlag = false;		// 신규 테이블인지 예전 테이블인지 판단을 위한 Flag값(true : 신규 테이블, false : 예전 테이블 )
				
				int _tablePropertysLength = _tablePropertys.getLength();
				for ( l = 0; l < _tablePropertysLength; l++) {
					if( _tablePropertys.item(l) instanceof Element )
					{
						_ItemProperty = (Element) _tablePropertys.item(l);
						if(_ItemProperty.getTagName().equals("property"))
						{
							_propertyName = _ItemProperty.getAttribute("name");
							_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
							_propertyType = _ItemProperty.getAttribute("type");
							tableProperty.put( _propertyName, new Value( _propertyValue,_propertyType));
						}
					}
				}
				
				if( tableProperty.containsKey("version") &&  tableProperty.get("version").getStringValue().equals(ItemConvertParser.TABLE_VERSION_NEW))
				{
					_newTalbeFlag = true;
				}
				
				tableProperty.put("tableId", new Value( _itemId,"string"));
				
				//band에 table속성 저장 by IHJ
				try
				{
					mBandInfoMap.setTableProperty(tableProperty);		
				}
				catch(NullPointerException nexp){}
				
				NodeList _tableML = _childItem.getElementsByTagName("table");
				Element _tableM = (Element) _tableML.item(0);
				
				if(_newTalbeFlag) _tableMaps = _tableM.getElementsByTagName("tableMap");
				else _tableMaps = _tableM.getElementsByTagName("row");
				
				int _tableMapsLength = _tableMaps.getLength();
				int _tableMapDatasLength = 0;
				
				ArrayList<ArrayList<HashMap<String, Value>>> _allTableMap = new ArrayList<ArrayList<HashMap<String,Value>>>();
				//TableMap을 이용하여 TableMapData로 모든 테이블의 맵을 담아두기
				// 각 맵별BorderString담아두기작업
				
				ArrayList<Float> _xAr = new ArrayList<Float>();	//각 row별 Height값 담아두기
				ArrayList<Float> _yAr = new ArrayList<Float>();	//각 row별 Height값 담아두기
				float _pos = 0;
				float _mapW = 0;	// 테이블맵의 X좌료
				float _mapH = 0;	// 테이블 맵의 Y좌표
				
				ArrayList<Integer> _exColIdx = new ArrayList<Integer>();		
				ArrayList<Float> _newXValue = new ArrayList<Float>();//최종  컬럼의 x값
				ArrayList<Float> arTempTableRowH = new ArrayList<Float>();
				boolean  _ubFxChkeck = true;
				Element _cell = null;
				if( _newTalbeFlag )
				{
					for ( k = 0; k < _tableMapsLength; k++) {
						
						_tableMapItem = (Element) _tableMaps.item(k);
						_tableMapDatas = _tableMapItem.getElementsByTagName("tableMapData");
						_tableMapDatasLength = _tableMapDatas.getLength();
						
						ArrayList<HashMap<String, Value>> _tableMapRow = new ArrayList<HashMap<String,Value>>();
						
						for ( l = 0; l < _tableMapDatasLength; l++) {
							_ubFxChkeck = true;
							itemProperty = new HashMap<String, Value>();
							
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
									
									itemProperty.put(  _propertyName, new Value(_propertyValue, _propertyType));
								}else if(_ItemProperty.getAttribute("name").equals("includeLayout")){
									itemProperty.put(  _ItemProperty.getAttribute("name"),  new Value(URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8"), "string") );
								}
							}
							
							//_tableMapRow.add(itemProperty);
							
							if( k == 0 )
							{
								// X좌표 담기 (소숫점 1자리까지만 )
//								_pos = (float) Math.floor( itemProperty.get("columnWidth").getIntegerValue()*10 ) /10 ;
								_pos = (float) Math.round( itemProperty.get("columnWidth").getIntegerValue()) ;
								_xAr.add( _mapW  );
								_mapW = _mapW + _pos;
							}
							_cell = (Element)((Element) _tableMapDatas.item(l)).getElementsByTagName("cell").item(0);
							itemProperty.put("cell", new Value(_cell , "element"));
							_tableMapRow.add(itemProperty);
							
						}
						
						if( k == 0 )
						{
							_xAr.add( _mapW  );		// 마지막 Width 값 담기
						}
						
//						_pos = (float) Math.floor( itemProperty.get("rowHeight").getIntegerValue()*10 ) /10 ;
						_pos = (float) Math.round( itemProperty.get("rowHeight").getIntegerValue() ) ;
						_yAr.add( _mapH  );
						_mapH = _mapH + _pos;
						
						_allTableMap.add(_tableMapRow);
						arTempTableRowH.add(_pos);
					}
					//TablePropery에 rowheightarray 정보 담기[속성명:tempTableRowHeight] by IHJ
					tableProperty.put("tempTableRowHeight", new Value(arTempTableRowH));						
					_yAr.add( _mapH  );		// 마지막 Height 값 담기
					
					//column visible 속성관련 추가 작업 ================================================================================================
					//1. 첫번째 Row에서 column visible 속성찾아 array 추가
					for ( l = 0; l < _allTableMap.get(0).size(); l++) {
						tableMapProperty = _allTableMap.get(0).get(l);	
						if(tableMapProperty.containsKey("includeLayout") &&  tableMapProperty.get("includeLayout").getBooleanValue() == false){	
							_ubFxChkeck = false;
						}else{
							_ubFxChkeck = UBIDataUtilPraser.getUBFxChkeck(mDataSet,  tableMapProperty.get("cell").getElementValue(), mParam, "includeLayout", _fn );
						}
						if(!_ubFxChkeck){									
							_exColIdx.add(l);					
							if(!tableMapProperty.get("colSpan").getStringValue().equals("1")){
								int _colSpan = Integer.parseInt(tableMapProperty.get("colSpan").getStringValue());
								for(int tmp = l+1; tmp < l+_colSpan; tmp++){
									_exColIdx.add(tmp);							
								}
							}
						}
					}	

					//2. column visible array에 해당하는 컬럼들을 row별로 돌면서 merge여부에 따라 해당 인덱스 추가
					if(_exColIdx.size() > 0){
						for ( l = 0; l < _allTableMap.get(0).size()-1; l++) {
							for ( k = 1; k < _allTableMap.size(); k++) {					
								tableMapProperty = _allTableMap.get(k).get(l);
								if(!tableMapProperty.get("colSpan").getStringValue().equals("1")){
									int _colSpan = Integer.parseInt(tableMapProperty.get("colSpan").getStringValue());
									for(int tmp = l; tmp < l+_colSpan; tmp++){
										if(_exColIdx.indexOf(tmp) == -1){
											_exColIdx.add(tmp);				
										}
									}
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
						if(_exColIdx.indexOf(l) == -1){		
							_tempWidth = _tempWidth + Float.parseFloat( tableMapProperty.get("columnWidth").getStringValue());
							_newXValue.add(_tempWidth);
						}	
					}			
					
					//4. _allTableMap에서 column visible에 해당하는 colunm을 모두 제거한다.
					for ( k = 0; k < _allTableMap.size(); k++) {//row
						for ( l = _exColIdx.size()-1; l >= 0; l--) {//column									
							_allTableMap.get(k).remove(Integer.parseInt(_exColIdx.get(l).toString()));
						}
					}	
					
				}
				else
				{
					float _argoHeight = 0;
					int _rowPropertyLength = 0;
					
					for ( k = 0; k < _tableMapsLength; k++) {
						
						_tableMapItem = (Element) _tableMaps.item(k);
						_tableMapDatas = _tableMapItem.getElementsByTagName("cell");
						_tableMapDatasLength = _tableMapDatas.getLength();
						
						ArrayList<HashMap<String, Value>> _tableMapRow = new ArrayList<HashMap<String,Value>>();
						
						for ( l = 0; l < _tableMapDatasLength; l++) {
							
							itemProperty = new HashMap<String, Value>();
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
									itemProperty.put(  _propertyName, new Value(_propertyValue, _propertyType));
								}
							}
							itemProperty.put(  "y", new Value(_argoHeight, "number") );	
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
				
				//Table의 UBFX를 담아두기
				NodeList _tableUbfunction = null;
				int _ubfxCnt = _childItem.getChildNodes().getLength();
				for (int m = 0; m < _ubfxCnt; m++) {
					if( _childItem.getChildNodes().item(m).getNodeName().equals("ubfx") )
					{
						Element _e = (Element) _childItem.getChildNodes().item(m);
						_tableUbfunction = _e.getElementsByTagName("ubfunction");
					}
				}
				
				for ( k = 0; k < _allTableMap.size(); k++) {
					
					colIndex = 0;
					
					// column 값 처리 ( UBApproval 일경우 데이터셋의 row수만큼 컬럼을 증가 ) 
					for ( l = 0; l <  _allTableMap.get(k).size(); l++) {
						
						tableMapProperty = _allTableMap.get(k).get(l);
						
						itemProperty = new HashMap<String, Value>();
						
						_itemDataSetName = "";
						_cellItem = tableMapProperty.get("cell").getElementValue();
						if(_newTalbeFlag)
						{
							//_cellItem = (Element) ((Element)_tableMapDatas.item(l)).getElementsByTagName("cell").item(0);
							if( _cellItem == null ) continue;
							_cellPropertys = (NodeList) _cellItem.getElementsByTagName("property");
							
							if( tableMapProperty.get("status").getStringValue().equals("MC") == false )
							{
								colIndex = colIndex + Integer.valueOf( tableMapProperty.get("colSpan").getStringValue() );
							}
							
							int _cellPropertysLength = _cellPropertys.getLength();
							if(_cellPropertysLength == 0 ) continue;
							
							
							rowIndex =  k + Integer.valueOf( tableMapProperty.get("rowSpan").getStringValue() );
							// 테이블의 모든 셀을 뒤져 데이터셋 정보를 가져오기
							for ( j = 0; j < _cellPropertysLength; j++) {
								
								_ItemProperty = (Element) _cellPropertys.item(j);
								
								if( _ItemProperty.getParentNode().getParentNode().getParentNode() != null && _ItemProperty.getParentNode().getParentNode().getParentNode().getNodeName() == "ubHyperLinkParm" )
								{
									continue;
								}
								
								itemProperty.put("Element", new Value(_cellPropertys,"element") );
								_propertyName = _ItemProperty.getAttribute("name");
								_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
								_propertyType = _ItemProperty.getAttribute("type");
								
								if( _ItemProperty.getAttribute("name").equals("y") && _ItemProperty.getAttribute("value").equals("") == false  )
								{
									itemProperty.put(  _propertyName, new Value( Float.valueOf( _propertyValue ), _propertyType));
								}
								else
								{
									itemProperty.put(  _propertyName, new Value( _propertyValue, _propertyType));
								}
							}
							//cell index정보 저장
							itemProperty.put( "rowIndex", new Value(tableMapProperty.get("rowIndex").getIntValue()) );	
							itemProperty.put( "columnIndex", new Value(tableMapProperty.get("columnIndex").getIntValue()) );	
						}
						else
						{
							//_cellItem = (Element) _tableMapDatas.item(l);
							itemProperty = _allTableMap.get(k).get(l);
							
							rowIndex =  k + Integer.valueOf( tableMapProperty.get("rowSpan").getStringValue() );
							colIndex = colIndex + Integer.valueOf( tableMapProperty.get("colSpan").getStringValue() );
						}
						
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
						
						Matcher _matcher = null;
						Pattern _patt = null;	
						
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
							if( colIndex == _allTableMap.get(k).size()) useRightBorder = true;
							if( rowIndex == _tableMaps.getLength() ) useBottomBorder = true;
						}
						else
						{
							if( l+ tableMapProperty.get("colSpan").getIntValue() >= _allTableMap.get(k).size()-1 ) useRightBorder = true;
							if( k+ tableMapProperty.get("rowSpan").getIntValue() >= _tableMaps.getLength()-1 ) useBottomBorder = true;
						}						
						
						if(useRightBorder)
						{
							if(_newTalbeFlag) _rightBorderStr =  _allTableMap.get(k).get(_allTableMap.get(k).size()-1 ).get("borderString").getStringValue();
							else  _rightBorderStr =  tableMapProperty.get("borderString").getStringValue();
						}
						if(useBottomBorder)
						{
							if(_newTalbeFlag)_bottomBorderStr =  _allTableMap.get(_tableMaps.getLength()-1).get(l).get("borderString").getStringValue();
							else  _bottomBorderStr =  tableMapProperty.get("borderString").getStringValue();
						}
						
						//이전 Cell과의 top,left borderType 변경 여부  정보 담기				
						itemProperty.put("beforeBorderType", Value.fromArrayBoolean(beforeBorderType));						
						itemProperty.put("ORIGINAL_TABLE_ID", new Value(_itemId,"string") );
						
						if(!(isExportType.equals("PPT"))){
							borderAr = ItemConvertParser.convertCellBorder( tableMapProperty.get("borderString").getStringValue(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr,useLeftBorder,useTopBorder ,isExportType );
						}
						else{
							borderAr = ItemConvertParser.convertCellBorderForPPT( tableMapProperty.get("borderString").getStringValue(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr );
						}
						
						
						// 아이템의 Height값이 다음 Row의 Y값보다 클경우 사이즈를 수정. ( 신규 테이블일경우에만 지정 ) - 이전 테이블의 경우 rowSpan값이 부정확하여 정확한 위치가 잡히지 않을수 있음.
						//X좌표와 Width값 업데이트
						int _spanP = 0;
						int _indexP = 0;
						float _updateP = 0;
						
						//Y좌표와 Height값 업데이트
						if(_yAr.size() > 0 )
						{
							_spanP = tableMapProperty.get("rowSpan").getIntValue();
							_indexP = tableMapProperty.get("rowIndex").getIntValue();
							
							float _newY = _yAr.get(_indexP);
							float _chkBandY = 0;
							float _chkOutHeight = 0;
							float _chkBandYposition = 0;
							
							itemProperty.put("y", new Value(_newY, "number")  );
							_updateP = (float) Math.round((_yAr.get(_indexP + _spanP ) - _yAr.get(_indexP)) );
							itemProperty.put("height", new Value( _updateP, "number") );
							
							// band_y값이 존재할경우 table의 Y와 Height값을 이용하여 overHeight와 outHeight값을 담기.
							if( tableProperty.containsKey("band") && tableProperty.get("band").toString().equals("") == false )
							{
								if(tableProperty.containsKey("band_y") && tableProperty.get("band_y").toString().equals("") == false)
								{
									
									tableProperty.put("band_y", new Value( Math.round( tableProperty.get("band_y").getIntegerValue() ) , "number") );
									
									_chkBandYposition = tableProperty.get("band_y").getIntegerValue();
									
									if( _newY < ( tableProperty.get("band_y").getIntegerValue()*-1 ) )
									{
										_chkBandY = tableProperty.get("band_y").getIntegerValue();
										itemProperty.put("cellOverHeight", new Value( _chkBandY*-1, "number") );
									}
									else
									{
										_chkBandY = 0;
										itemProperty.put("cellOverHeight", new Value( 0, "number") );
									}
									
									if( mBandInfoMap != null )
									{
										float _chkBandH = mBandInfoMap.getHeight();
										if( _newY + _updateP > _chkBandH )
										{
											if( _chkBandY == 0 ) _chkBandY = _chkBandYposition;
											_chkOutHeight = (_newY + _updateP) + _chkBandY - _chkBandH;
											itemProperty.put("cellOutHeight", new Value( _chkOutHeight, "number") );
										}
										else
										{
											itemProperty.put("cellOutHeight", new Value( 0, "number") );
										}
									}
								}
								else
								{
									itemProperty.put("cellOutHeight", new Value( 0, "number") );
								}
								
							}
							
						}
						
						// Cell 아이템의 Formatter 이나 UBFx를 위하여 Element 값을 담아둔다.
						itemProperty.put( "Element", new Value(_cellItem,"element") );	
						
						itemProperty.put( "isCell", new Value(true,"boolean") );
						itemProperty.put("TableMap", new Value(tableProperty,"object"));
						
						itemProperty.put("borderSide", 		Value.fromString( borderAr.get(0).toString() ) );
						itemProperty.put("borderTypes", 	Value.fromArrayString( (ArrayList<String>) borderAr.get(1) ));
						itemProperty.put("borderColors",  	Value.fromArrayString( (ArrayList<String>) borderAr.get(2) ));
						itemProperty.put("borderWidths",  	Value.fromArrayInteger( (ArrayList<Integer>) borderAr.get(3) ));
						itemProperty.put("borderColorsInt",  	Value.fromArrayInteger( (ArrayList<Integer>) borderAr.get(4) ));
						
						// Border Original Type담아두기
						if( borderAr.size() > 5 ) itemProperty.put("borderOriginalTypes",  	Value.fromArrayString( (ArrayList<String>) borderAr.get(5) ));
						
						
						//if( tableProperty.get("band_x").getIntegerValue().isNaN() == false ) updateX = Float.valueOf(tableProperty.get("band_x").getStringValue()) +_newXValue.get(l);
						
						updateX = mX + _newXValue.get(l) + tableProperty.get("x").getIntegerValue();
						updateY = mBand_Y + itemProperty.get("y").getIntegerValue();
						
						if( mBandID == null || mBandID.equals("") )
						{
//							updateX = updateX + tableProperty.get("x").getIntegerValue();
							updateY = mY + updateY + tableProperty.get("y").getIntegerValue();
						}
						else
						{
							updateY = updateY + tableProperty.get("y").getIntegerValue();
						}
						
						itemProperty.put(  "band_x", new Value( updateX, "string"));
						itemProperty.put(  "x", new Value(updateX , "string"));
						itemProperty.put(  "y", new Value(updateY , "string"));
						itemProperty.put(  "band_y", new Value( updateY, "string"));
						
						
						if( itemProperty.containsKey("id") == false || itemProperty.get("id").getStringValue().equals("") ) itemProperty.put(  "id", new Value( _childItem.getAttribute("id") + "_" + k + l, "string"));
						if(_newTalbeFlag)
						{
							itemProperty.put(  "realClassName", new Value("TABLE", "string"));
						}
						itemProperty.put(  "realTableID", new Value(_childItem.getAttribute("id"), "string"));			// 원본 테이블의 ID를 담아두는 객체
						itemProperty.put(  "className", new Value( "UBLabel", "string"));
						
						if( itemProperty.get("dataType").getStringValue().equals("") == false  &&  !itemProperty.get("dataType").getStringValue().equals("0") &&
								itemProperty.get("dataSet").getStringValue().equals("") == false  )
						{
							_itemDataSetName = itemProperty.get("dataSet").getStringValue();
						}
						
						if( _tableUbfunction != null && _tableUbfunction.getLength() > 0 )
						{
							itemProperty.put( "tableUbfunction", new Value(_tableUbfunction,"nodelist") );
						}
						
						if(_className.equals("UBApproval"))
						{
							if(_ubApprovalAr == null)
							{
								_ubApprovalAr = new ArrayList<HashMap<String, Value>>();
								_ubApprovalAr.add(tableProperty);
							}
							_ubApprovalAr.add(itemProperty);
						}
						else
						{
							if( _itemDataSetName.equals("") == false  && mDataSet.containsKey(_itemDataSetName) && mDataSet.get(_itemDataSetName) != null && tableProperty.containsKey("band")  && mBandInfoMap != null)
							{
								if( mBandInfoMap.getDataSet().equals("") || mDataSet.get( mBandInfoMap.getDataSet() ).size() < mDataSet.get(_itemDataSetName).size() )
								{
									mBandInfoMap.setDataSet(_itemDataSetName);
									mBandInfoMap.setRowCount(mDataSet.get(_itemDataSetName).size());
								}
							}
							
							mItems.add(itemProperty);
							// BandInfo에는 Templet객체를 담아두고 실제 아이템 생성시에 Templet객체의 경우 TempletInfo에서 아이템을 생성하도록 처리 필요
//							if(tableProperty.containsKey("band") && tableProperty.get("band").getStringValue()!="" && mBandInfoMap != null )
//							{
//								mBandInfoMap.getChildren().add(itemProperty);
//								
//								int _requiredItemIndex = OriginalRequiredItemList.indexOf(itemProperty.get("id").getStringValue());
//								int _tabIndexItemIndex = OriginalTabIndexItemList.indexOf(itemProperty.get("id").getStringValue());
//								
//								if( _requiredItemIndex != -1 )
//								{
//									mBandInfoMap.setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").getStringValue() );
//								}
//								
//								if( _tabIndexItemIndex != -1 )
//								{
//									mBandInfoMap.setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").getStringValue() );
//								}
//							}
							
						}
						
						if( mXAr.indexOf( itemProperty.get("x").getIntegerValue() ) == -1 ) 
						{
							mXAr.add(itemProperty.get("x").getIntegerValue() );
						}
						
						if( mXAr.indexOf( itemProperty.get("x").getIntegerValue() + itemProperty.get("width").getIntegerValue()   ) == -1 ) 
						{
							mXAr.add( itemProperty.get("x").getIntegerValue() + itemProperty.get("width").getIntegerValue() );
						}
						
						if(l == 0 )
						{
							_tableArogHeight = _allTableMap.get(k).get(l).get("rowHeight").getIntegerValue();
						}
						
					}
					
				}
				
				if(_className.equals("UBApproval") && _ubApprovalAr != null)
				{
					convertTableMapToApprovalTbl( _ubApprovalAr );
				}
				
				
			}
			else
			{
				itemProperty = new HashMap<String, Value>();
				itemProperty.put("Element", new Value(_childItem,"element") );
				_propertys = (NodeList) _childItem.getElementsByTagName("property");
				_itemDataSetName = "";
				
				int _propertysLength = _propertys.getLength();
				for (j = 0; j < _propertysLength; j++) {
					
					_ItemProperty = (Element) _propertys.item(j);
					
					if( _ItemProperty.getParentNode().getNodeName().equals("item") == false )
					{
						continue;
					}
					
					_propertyName  = _ItemProperty.getAttribute("name");
					_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
					_propertyType  = _ItemProperty.getAttribute("type");
					
					itemProperty.put(  _propertyName, new Value( _propertyValue, _propertyType));
				}
				
				itemProperty.put(  "id", new Value( _childItem.getAttribute("id"), "string"));
				itemProperty.put(  "className", new Value(_className, "string"));
				
				updateX = mX + itemProperty.get("x").getIntegerValue();
//				updateY = mBand_Y + itemProperty.get("y").getIntegerValue();
				
				if( mBandID == null || mBandID.equals("") )
				{
					updateY = mY + itemProperty.get("y").getIntegerValue();
				}
				else
				{
					updateY = mBand_Y + itemProperty.get("y").getIntegerValue();
				}
				
				
				itemProperty.put(  "band_x", new Value( updateX, "string"));
				itemProperty.put(  "x", new Value(updateX , "string"));
				itemProperty.put(  "y", new Value(updateY , "string"));
				itemProperty.put(  "band_y", new Value( updateY, "string"));
				
				if( itemProperty.containsKey("dataType") && itemProperty.get("dataType").getStringValue().equals("") == false  &&  !itemProperty.get("dataType").getStringValue().equals("0") &&
						itemProperty.get("dataSet").getStringValue().equals("") == false  )
				{
					_itemDataSetName = itemProperty.get("dataSet").getStringValue();
				}
				
				if( _itemDataSetName.equals("") == false  && mDataSet.containsKey(_itemDataSetName) && mDataSet.get(_itemDataSetName) != null && itemProperty.containsKey("band") && mBandInfoMap != null )
				{
					if( mBandInfoMap.getDataSet().equals("") || mDataSet.get( mBandInfoMap.getDataSet() ).size() < mDataSet.get(_itemDataSetName).size() )
					{
						mBandInfoMap.setDataSet(_itemDataSetName);
						mBandInfoMap.setRowCount(mDataSet.get(_itemDataSetName).size());
					}
				}
				
				if( mXAr.indexOf( itemProperty.get("x").getIntegerValue() ) == -1 ) 
				{
					mXAr.add(itemProperty.get("x").getIntegerValue() );
				}
				
				if( mXAr.indexOf( itemProperty.get("x").getIntegerValue() + itemProperty.get("width").getIntegerValue()   ) == -1 ) 
				{
					mXAr.add( itemProperty.get("x").getIntegerValue() + itemProperty.get("width").getIntegerValue() );
				}
				
				mItems.add(itemProperty);
//				if(itemProperty.containsKey("band") && itemProperty.get("band").getStringValue()!=""  && itemProperty.get("band").getStringValue().equals("null") == false && bandInfoData.containsKey(itemProperty.get("band").getStringValue()) )
//				{
//					bandInfoData.get(itemProperty.get("band").getStringValue()).getChildren().add(itemProperty);
//				}
			}
			
		}// 각각의 아이템들의 데이터셋정보를 밴드리스트에 맵핑하고 생성할 로우별 아이템을 children에 담기완료
		
		
		boolean isAutoTableHeight  =  false;
		ArrayList<HashMap<String, Value>> chilrden;
		HashMap<String, Value> currentItemData; 
		isAutoTableHeight = (mBandInfoMap!=null)? mBandInfoMap.getAutoTableHeight():false;
		// autoTableHeight를 설정한 경우 Cell이 아닌경우 자신이 포함된 Cell의 정보를 담아놓는다.
		if(isAutoTableHeight){
			
			float xTblPos = 0;
			float yTblPos = 0;
			float tblWidth = 0;
			float tblHeight = 0;
			float sumHeight = 0;
			float xOtherPos = 0;
			float yOtherPos = 0;
			ArrayList<Float> arTempTableRowH;
			
			if(mBandInfoMap.getTableProperties() != null){
				ArrayList<HashMap<String, Value>> arTableProperties = mBandInfoMap.getTableProperties();            	
				
				chilrden = mItems;
				
				for(int jj = 0; jj < chilrden.size(); jj++){
					currentItemData = chilrden.get(jj);				
					if(!currentItemData.containsKey("isCell")){//cell 이 아니면 테이블 포함 여부 체크 및 cell 정보 저장
						for(int kk = 0; kk<arTableProperties.size(); kk++){
							xTblPos = arTableProperties.get(kk).get("x").getIntegerValue();
							yTblPos = arTableProperties.get(kk).get("band_y").getIntegerValue();
							tblWidth = arTableProperties.get(kk).get("width").getIntegerValue();
							tblHeight = arTableProperties.get(kk).get("height").getIntegerValue();
							sumHeight = yTblPos;
							
							xOtherPos = currentItemData.get("x").getIntegerValue();
							yOtherPos = currentItemData.get("band_y").getIntegerValue();
							
							//테이블 안에 있는 확인
							if((xTblPos <= xOtherPos && xOtherPos <= (xTblPos + tblWidth))
									&& (yTblPos <= yOtherPos && yOtherPos <= (yTblPos + tblHeight)) ){
								arTempTableRowH = (ArrayList<Float>)arTableProperties.get(kk).get("tempTableRowHeight").getObjectValue();
								for(int hh = 0;  hh< arTempTableRowH.size(); hh++){
									sumHeight = sumHeight + arTempTableRowH.get(hh);
									if(sumHeight>=yOtherPos){
										//item이 속한 Cell의 RowIndex 정보(cellRowIndex)저장 by IHJ
										currentItemData.put("cellRowIndex", new Value(hh,"int"));
										currentItemData.put("tableId", new Value(arTableProperties.get(kk).get("tableId").getStringValue(),"string"));
										currentItemData.put("cellPadding", new Value(yOtherPos - (sumHeight- arTempTableRowH.get(hh)) ,"float"));
										break;
									}
								}
							}
						}
					}
				}//band 내 Item	
			}
			
		}			
		
		return null;
	}
	
	
	private void convertTableMapToApprovalTbl( ArrayList<HashMap<String, Value>> arraylist )
	{
		
		HashMap<String, Value> tblProperty = arraylist.get(0); 
		
		String _cellFixed = "left";	// UBApproval 아이템의 fixed포지션 left,right 
		int cnt = arraylist.size();		// UBApproval 아이템의 갯수 
		float _firstPosition = 0;		// 아이템의 처음 시작 X포지션
		String _dataSet = "";
		int dataCnt = 1;
		float _defaultW = 0;
		String bandName = tblProperty.get("band").getStringValue();
		
		if(tblProperty.containsKey("cellFix"))
		{
			_cellFixed = tblProperty.get("cellFix").getStringValue();
		}
		
		if(arraylist.get(2).containsKey("dataSet") && arraylist.get(2).get("dataType").getStringValue().equals("1") && arraylist.get(2).get("dataSet").equals("") == false )
		{
			_dataSet = arraylist.get(2).get("dataSet").getStringValue();
			dataCnt = mDataSet.get(_dataSet).size();
		}
		else
		{
			dataCnt = 1;
		}
		
		if( "right".equals(_cellFixed) )
		{
			_firstPosition = tblProperty.get("x").getIntegerValue() - (arraylist.get(2).get("width").getIntegerValue() * (dataCnt-1));
		}
		else
		{
			_firstPosition = tblProperty.get("x").getIntegerValue();
		}
		
		float _addPosition = _firstPosition;
		HashMap<String, Value> cloneItems = null;
		
		for (int i = 1; i < cnt; i++) {
			if(i == 1)
			{
				arraylist.get(i).put("x", new Value( _firstPosition, "number") );
				
				mItems.add(arraylist.get(i));
				_firstPosition = _firstPosition + arraylist.get(i).get("width").getIntegerValue();
			}
			else
			{
				_addPosition = _firstPosition;
				
				for (int j = 0; j < dataCnt; j++) {
					
					cloneItems = (HashMap<String, Value>) arraylist.get(i).clone();
					
					if( "".equals(_dataSet) == false && !cloneItems.get("column").getStringValue().equals("") && !cloneItems.get("column").getStringValue().equals("null") ) 
					{
						cloneItems.put("dataType", new Value("0", "string") );
						cloneItems.put("text", new Value( mDataSet.get(_dataSet).get(j).get(cloneItems.get("column").getStringValue()), "string" ) );
					}
					
					// 보더 업데이트를 위하여 오른쪽보더를 제거
					if(j < dataCnt-1)
					{
						String borderSide = cloneItems.get("borderSide").getStringValue();
						ArrayList<String> borderType = (ArrayList<String>) cloneItems.get("borderTypes").getArrayStringValue().clone();
						String[] _sideAr = borderSide.split(",");
						borderSide = "";
						for (int k = 0; k < _sideAr.length; k++) {
							if( "right".equals( _sideAr[k] ) )
							{
								borderType.set(k, "none");
							}
						}
						
						cloneItems.put("borderTypes", new Value( borderType, "arraystr" ) );
					}
					
					cloneItems.put("x", new Value( _addPosition, "number") );
					mItems.add(cloneItems);
					_addPosition = _addPosition + cloneItems.get("width").getIntegerValue();
					
				}
			}
		}
		
		
	}
	
	
	//public static HashMap<String, TempletItemInfo> checkTempletItem( Document _doc, JSONObject _param, ServiceRequestManager _serviceReq, String _isExport, boolean _isMarkAny, String _externalProjectId )
	public static HashMap<String, TempletItemInfo> checkTempletItem( Document _doc, JSONObject _param, String _isExport, boolean _isMarkAny, String _externalProjectId )
	{
		Element _projectEl = _doc.getDocumentElement();
		HashMap<String, TempletItemInfo> _templetMap = new HashMap<String, TempletItemInfo>();
		NodeList _list = _projectEl.getElementsByTagName("item");
		int _cnt = _list.getLength();
		Element _item;
		String _className = "";
		for(int i = 0; i < _cnt; i++ )
		{
			_item = (Element) _list.item(i);
			_className = _item.getAttribute("className");
			if( _className.equals("UBTemplateArea") )
			{
				//TempletItemInfo _tmpInfo = new TempletItemInfo(_item, _param, _serviceReq, _isExport, _isMarkAny, _externalProjectId );
				TempletItemInfo _tmpInfo = new TempletItemInfo(_item, _param, _isExport, _isMarkAny, _externalProjectId );
				
				_templetMap.put( _item.getAttribute("id").toString(), _tmpInfo );
				
				if( _item.getParentNode().getParentNode().getNodeName().equals("page") )
				{
					_tmpInfo.setPageID( ((Element) _item.getParentNode().getParentNode()).getAttribute("id") );
				}
			}
			
		}
		
		return _templetMap;
	}
	
	public static HashMap<String, HashMap<String, String>> getTempletDataParam( HashMap<String, TempletItemInfo> _templets, HashMap<String, HashMap<String, String>> _paramMap )
	{
		HashMap<String, HashMap<String, String>> _tmp;
		
		for (String key : _templets.keySet())
		{
			if( _templets.get(key).getUseDataParameter() )
			{
				_tmp = _templets.get(key).getParameterData();
				
				for (String paramKey : _tmp.keySet())
				{
					if(_paramMap == null)_paramMap = new HashMap<String, HashMap<String, String>>();
					
					if( _paramMap.containsKey(paramKey) == false )
					{
						_paramMap.put( paramKey, _tmp.get(paramKey));
					}
				}
				
			}
			
		}
		
		return _paramMap;
		
	}
	
	
	// 페이지별로 Templet의 아이템을 내보내기 처리
	public ArrayList<HashMap<String, Object>>  convertItemData( int rowIndex, float _moveX, float _moveY, ArrayList<HashMap<String, Object>> _addListAr, Function _fn, 
			BandInfoMapData _bandInfo, int _currentPage, int _totalPageNum, int _startIndex , int _lastIndex , HashMap<String, Object> mChangeItemList, HashMap<String, Value> currentBandCntMap, ItemConvertParser _itemParser ) throws UnsupportedEncodingException, ScriptException
	{	
		int _itemLength = mItems.size();
		HashMap<String, Value> currentItemData;
		HashMap<String, Object> _propList;
		String _className = "";
		String _name = "";
		Object _value 	= null;
		int k = 0;
		String _model_type = "";
		String _barcode_type = "";
		String _prefix = "";
		String _suffix = "";
		String dataSet = "";
		String _dataID = "";
		String _itemId = "";
		Boolean _bType = false;
		
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
		
		int mGroupCurrentPageIndex = -1;
		int mGroupTotalPageIndex = -1;
		
		
		// Group함수의 페이징함수를 위한 값을 담아두기
		if( currentBandCntMap != null && currentBandCntMap.containsKey("gprCurrentPageNum") && currentBandCntMap.get("gprCurrentPageNum") != null )
		{
			mGroupCurrentPageIndex = (int) currentBandCntMap.get("gprCurrentPageNum").getIntValue();
		}
		if( _bandInfo != null && _bandInfo.getGroupStartPageIdx() > -1 )
		{
			mGroupCurrentPageIndex = _currentPage - _bandInfo.getGroupStartPageIdx();
		}
		
		if( currentBandCntMap != null && currentBandCntMap.containsKey("gprTotalPageNum") && currentBandCntMap.get("gprTotalPageNum") != null )
		{
			mGroupTotalPageIndex = (int) currentBandCntMap.get("gprTotalPageNum").getIntValue();
		}
		if( _bandInfo != null && _bandInfo.getGroupTotalPageCnt() > -1 )
		{
			mGroupTotalPageIndex = _bandInfo.getGroupTotalPageCnt();
		}
		
		for (int i = 0; i < _itemLength; i++) {
			
			currentItemData = mItems.get(i);
			_bType = _className.equals("UBLabelBorder")?true:false;
			
			if(currentItemData.containsKey("id"))
			{
				_itemId = currentItemData.get("id").getStringValue();
			}
			_dataID = currentItemData.get("dataSet").getStringValue();
			_className = currentItemData.get("className").getStringValue();
			
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
			_systemFunction="";
			
			// formatter variables 
			_formatter="";
			_nation="";
			_align="";
			_dataType="";
			_mask="";
			_inputForamtString = "";
			_outputFormatString = "";
			
			_decimalPointLength=0;
			_useThousandComma=false;
			_isDecimal=false;
			_formatString="";
			
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
						_value = "1.16";
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
				}
				
				if( _name.equals("formatter") ){
					_formatter = _value.toString();
				}else if( _name.equals("systemFunction") ){
					_systemFunction = _value.toString();
				}
				
				
				else if(_name.equals("dataType"))
				{
					_dataType = _value.toString();
					_propList.put(_name, _value);
				}
				
				else if( _name.equals("data") )
				{
					if(_className.equals("UBImage") || _className.equals("UBSignature") || _className.equals("UBPicture"))
					{
						_propList.put("src",  URLEncoder.encode(_value.toString(), "UTF-8"));
					}
				}
				else if( _name.equals("prefix") ){
					try {
						_prefix=URLDecoder.decode(_value.toString(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
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
					_propList.put("x2", _value);
				}
				else if( _name.equals("height"))
				{
					_propList.put("y2", _value);
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
					if( mIsMarkAny && "PRINT".equals(isExportType) && "false".equals(_value) )
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
			
			// Item의 changeData가 있는지 확인
			if(mChangeItemList != null )
			{
				String _chkID = _itemId + "_"+ _bandInfo.getId() + "_ROW"+rowIndex;
				
				_propList = _itemParser.convertChangeItemDataText( _currentPage ,_propList, _chkID );
			}
			
			
			
			// 보더업데이트
			if( _propList.containsKey("isCell") && _propList.get("isCell").toString().equals("false") )
			{
				_propList = ItemConvertParser.convertItemToBorder(_propList);
			}
			
			if(currentItemData.containsKey("ORIGINAL_TABLE_ID"))
			{
				_propList.put("ORIGINAL_TABLE_ID", currentItemData.get("ORIGINAL_TABLE_ID").getStringValue() );
			}
			
			if(currentItemData.containsKey("beforeBorderType"))
			{
				_propList.put("beforeBorderType", currentItemData.get("beforeBorderType").getArrayBooleanValue());
			}
			
			if( _className.equals("UBImage") || _className.equals("UBSignature") || _className.equals("UBPicture")){
				String _url="";
				String _txt = "";
				String	_servicesUrl = "";
				
				if( _dataType.equals("1") )
				{
					ArrayList<HashMap<String, Object>> _list;
					
					if( dataSet.equals("") )
					{
						_list = mDataSet.get(currentItemData.get("dataSet").getStringValue());
					}
					else
					{
						_list = mDataSet.get( dataSet );
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
				else if( _dataType.equals("3"))
				{
					_txt = _propList.get("text").toString();
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					if( _inOf != -1 )
					{
						_fn.setParam(mParam);
						_txt=_fn.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							String _fnValue = _fn.function(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
							_propList.put("text", _fnValue);
						}else{

							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);

							HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_pKey);

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
							_decimalPointLength = Integer.parseInt(_formatPropertyValue);	
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
					ArrayList<HashMap<String, Object>> _pList = mDataSet.get(dataSet);
					String _datasetColumnName = currentItemData.get("column").getStringValue();
					_fn.setDatasetList(mDataSet);
					_fn.setParam(mParam);

					_fn.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
					if(mGroupTotalPageIndex>0) _fn.setSectionCurrentPageNum(mGroupCurrentPageIndex);
					_fn.setGroupTotalPageIndex(mGroupTotalPageIndex);
					if(mGroupTotalPageIndex>0) _fn.setSectionTotalPageNum(mGroupTotalPageIndex);
//					_fn.setGroupDataNamesAr(mGroupDataNamesAr);
//					_fn.setOriginalDataMap(mOriginalDataMap);
					
					String _fnValue;
					
					if( _fn.getFunctionVersion().equals("2.0") ){
						_fnValue = _fn.testFN(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,dataSet);
					}else{
						_fnValue = _fn.function(_ubfxValue,rowIndex,_totalPageNum,_currentPage , _startIndex,_lastIndex,dataSet);
					}
					
					
					_fnValue = _fnValue.trim();
					
					if(_ubfxProperty.indexOf("Color") != -1 )
					{
						_propList.put((_ubfxProperty + "Int"), mPropertyFn.changeColorHexToInt(_fnValue) );
					}
					_propList.put(_ubfxProperty, _fnValue.trim());			// 20170531 true false에 공백이 붙어 나오는 현상이 있어 수정
					
					// color 속성은 color + Int 속성을 넣어줘야 한다.
					if( _ubfxProperty.contains("Color") ){
						_propList.put((_ubfxProperty + "Int"), common.getIntClor(_fnValue) );
					}
				}
			}
			
			_ubfxNodes = null;
			
			if( _className.equals("UBSVGRichText") && mBandInfoMap != null && _bandInfo.getAdjustableHeightListAr().size() > 0 )
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
							
							if(mDataSet.containsKey(_hyperLinkedDataSetId))
							{
								ArrayList<HashMap<String, Object>> _list = mDataSet.get( _hyperLinkedDataSetId );
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
							HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_hyperLinkedDataSetColumn);

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
					ArrayList<HashMap<String, Object>> _list = mDataSet.get( _dataID );
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
						
						String _tmpDataValue = _dataValue.toString();
						boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
						boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
						boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
						
						if(_bSVG)
						{
							_dataValue = StringUtil.replaceSVGTag(_tmpDataValue,_preserveAspectRatio, _fixedToSize, _propList);	
						}
						else
						{
							boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
							if(!_bHasHtmlTag)
								_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
								
							_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
							_dataValue = StringUtil.replaceSVGTag(_tmpDataValue,_preserveAspectRatio, _fixedToSize, _propList);
								
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
						// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
						_propList = _itemParser.convertUBSvgItem(_dataValue,_propList);
						
						if(_propList == null ) return null;
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
					if( mDataSet.containsKey(_dataID))
					{
						// 함수 조회시 데이터 밴드의 Row수만큼만 만큼만 처리 하도록 변경 ( 데이터셋의 Row 수 만큼만 처리 하도록 하였으나 특정 함수의 경우 모든 Row에 필요 )
//						if( rowIndex > 0 && rowIndex >= DataSet.get( _dataID ).size() )
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
							
							ArrayList<HashMap<String, Object>> _pList = mDataSet.get( dataSet );
							String _datasetColumnName = columnStr;
							_fn.setDatasetList(mDataSet);
							_fn.setGroupCurrentPageIndex(mGroupCurrentPageIndex);
							_fn.setGroupTotalPageIndex(mGroupTotalPageIndex);
//							_fn.setGroupDataNamesAr(mGroupDataNamesAr);
//							_fn.setOriginalDataMap(mOriginalDataMap);
							
							String _fnValue;
							if( _fn.getFunctionVersion().equals("2.0") ){
								_fnValue = _fn.testFN(_systemFunction , rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet);
							}else{
								_fnValue = _fn.function(_systemFunction,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
							}

							if( _className.equals("UBSVGRichText") )
							{
								// 아이템의 text를 이용하여 svg태그 생성하고 key 값 data에 svg태그를 담아서 리턴
								_propList = _itemParser.convertUBSvgItem(_fnValue,_propList);
								
								if(_propList == null ) return null;
							}
							else if(_className.equals("UBImage") ) {
								_fnValue = URLDecoder.decode(_fnValue, "UTF-8");
								_propList.put("src",  URLEncoder.encode(_fnValue, "UTF-8"));
								
								
								if( _dataType.equals("2") && _propList.containsKey("src") && _propList.get("src") != null )
								{
									String	_servicesUrl = "";
									_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+ _propList.get("src").toString();
									_propList.put("src", _servicesUrl);
								}
								
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
					if( _inOf != -1 )
					{
						_fn.setParam(mParam);
						_txt=_fn.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							
							// 위에서 파라미터값을 치환하였는데, 밑에서 다시 치환하면 태그가 깨짐..
							if(_className.equals("UBSVGArea")  ){
								
								String _tmpDataValue = String.valueOf(_txt);
								boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
								boolean _preserveAspectRatio = (_propList.containsKey("preserveAspectRatio") )?_propList.get("preserveAspectRatio").toString().equals("true"):false;
								boolean _fixedToSize = (_propList.containsKey("fixedToSize") )?_propList.get("fixedToSize").toString().equals("true"):false;
								
								String _svgTag = null;
								if(_bSVG)
								{
									_svgTag = StringUtil.replaceSVGTag(_tmpDataValue , _preserveAspectRatio, _fixedToSize, _propList);
								}
								else
								{
									boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
									if(!_bHasHtmlTag)
										_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
										
									_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _propList );
									_svgTag = StringUtil.replaceSVGTag(_tmpDataValue, _preserveAspectRatio, _fixedToSize, _propList);
										
								}
																
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
								
								_propList = _itemParser.convertUBSvgItem( _txt, _propList);
								
								if(_propList == null ) return null;
							}
							
							
							String _fnValue;
							
							if( _fn.getFunctionVersion().equals("2.0") ){
								_fnValue = _txt;
							}else{
								_fnValue = _fn.function(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
							}
							
							
							_propList.put("text", _fnValue);
						}else{

							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);

							HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_pKey);

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
			
			
			if( _bandInfo != null && _itemId.equals("") == false )
			{
				_propList.put("TABINDEX_ID", _itemId + "_"+ _bandInfo.getId() + "_ROW"+rowIndex);
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
							//_barcodeValue = vi5.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
							_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
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
			else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") || _className.equals("UBTaximeter")|| _className.equals("UBRadarChart") )
			{
				String PROJECT_NAME = mProjectName;
				String FOLDER_NAME = mFormName;
				
				String IMG_TYPE = "";
				String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
				
				HashMap<Integer, String> displayNamesMap=null;
				
				
				if(_className.equals("UBTaximeter")){
					PARAM = ItemConvertParser.getChartParamToElement4Taximeter(currentItemData.get("Element").getElementValue() );	
					PARAM+=","+rowIndex;
				}else if(_className.equals("UBLineChart")){
					PARAM = ItemConvertParser.getLineChartParamToElement( currentItemData.get("Element").getElementValue() );	
				}else if(_className.equals("UBRadarChart")){
					PARAM = ItemConvertParser.getChartParamToElement4Radar(currentItemData.get("Element").getElementValue() );	
				}else{
					PARAM = ItemConvertParser.getChartParamToElement(currentItemData.get("Element").getElementValue() );
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
					displayNamesMap  = ItemConvertParser.getChartParamToElement2(currentItemData.get("Element").getElementValue() );
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
				
				String _chartValue = "";
				if(IMG_TYPE.equals("combcolumn"))
				{
					String _dataIDs = currentItemData.get("dataSets").getStringValue();				
					String [] arrDataId = _dataIDs.split(",");
					
					ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
					
					for( i=0; i< arrDataId.length; i++)
					{
						ArrayList<HashMap<String, Object>> _list = mDataSet.get(arrDataId[i]);
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
					ArrayList<HashMap<String, Object>> _list = mDataSet.get( _dataID );		
					
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
				_propList = _itemParser.convertStrechLabel(_propList);
				
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
			
			_propList.put("y", Float.valueOf(_propList.get("y").toString()) + _moveY);  
			_propList.put("x", Float.valueOf(_propList.get("x").toString()) + _moveX);  
			
			_propList.put("top",  _propList.get("y") );
			_propList.put("left", _propList.get("x") );
			
			if( currentItemData.containsKey("realClassName") && "TABLE".equals(currentItemData.get("realClassName").getStringValue() ) )
			{
				if( _itemId.equals("") )_propList.put("id", "TB_" + rowIndex + "_" + _itemId);
				// Export시 테이블로 내보내기 위한 작업
				_propList.put("isTable", "true" );
				_propList.put("TABLE_ID", mBandID + "_" + currentItemData.get("realTableID").getStringValue() );	// 테이블아이템생성을 위한 밴드명+테이블 id를 담아둔다(2016-03-07)
				
				_propList.put("cellHeight", _propList.get("height").toString());
				_propList.put("cellY", _propList.get("y") ); // 명우;
				
				if( currentItemData.containsKey("cellOverHeight") )
				{
					_propList.put("cellOverHeight", currentItemData.get("cellOverHeight").getIntegerValue() );
				}
				if( currentItemData.containsKey("cellOutHeight") )
				{
					_propList.put("cellOutHeight", currentItemData.get("cellOutHeight").getIntegerValue() );
				}
				
				if( currentItemData.containsKey("repeatedValue") && currentItemData.get("repeatedValue").getBooleanValue() )
				{
					_propList.put( "repeatedValue", currentItemData.containsKey("repeatedValue"));
				}
			}
			
			
			if("UBRotateLabel".equals(_className))
			{
				_propList.put("rotate" , _propList.get("rotation") );
			}
			
			if(_propList.containsKey("visible") == false || _propList.get("visible").equals("false") == false)
			{
				_propList.put("className" , _className );
				_propList.put("id" , _itemId );
				
				_addListAr.add(_propList);
			}
			
		}
		
		
		return _addListAr;
	}
	
	
	public ArrayList<Integer> addExcelXArrPosition( ArrayList<Integer> _xArr )
	{
		ArrayList<Integer> _retAr = new ArrayList<Integer>();
		
		for (Float float1 : mXAr) {
			if( _xArr.indexOf( float1 ) == -1)
			{
				_xArr.add( float1.intValue() );
			}
		}
		
		Collections.sort(_xArr);
		
		return _xArr;
	}
	
	
	
	public static  ArrayList<Integer> updateTempletXPosition( String _pageID, ArrayList<Integer> _xArr  , HashMap<String, TempletItemInfo> _templetInfoMap )
	{
		
		for (String key : _templetInfoMap.keySet())
		{
		   
			if( _templetInfoMap.get(key).getPageID().equals(_pageID) )
			{
				_xArr = _templetInfoMap.get(key).addExcelXArrPosition(_xArr);
			}
		}
		
		return _xArr;
	}
	
	public static void destroy(HashMap<String, TempletItemInfo> _templets)
	{
		if( _templets != null )
		{
			for (String key : _templets.keySet())
			{
				_templets.get(key).destroy();
				_templets.remove(_templets.get(key));
			}
		}
		
		_templets = null;
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
        //r.renderURL(args[0], os, type);
        //String _srcXHtml = "<html><body>     <h2 style=\"font-style:italic\">테스트1</h2>     <h2 style=\"font-style:italic\">테스트2</h2>     </body></html>";
        //String _srcXHtml = "<html><body>     <p>-. 당사 목표금액 미달시 유찰되며 별도 가격결정 진행임.</p>     <p>  </p>     <h2 style=\"font-style:italic\">테스트</h2>     <h2 style=\"font-style:italic\">테스트</h2>     <p><span class=\"marker\">테스트</span></p>     <p><code>테스트</code></p>     <h1><span style=\"color:#FF8C00\"><span style=\"font-size:9px\"><span style=\"font-family:malgun gothic\"><big><span style=\"background-color:#FFD700\">테스트</span></big></span></span></span></h1>     <p><strong>테스트</strong></p>     <p><u>테스트</u></p>     <p><s>테스트</s></p>     <p><sub>테스트</sub></p>     <p><sup>테스트</sup></p>     <ol>               <li>테스트</li>  </ol>     <ul>               <li>테스트</li>  </ul>     <p style=\"margin-left:40px\">테스트</p>     <blockquote>  <p>테스트</p>  </blockquote>     <p><a href=\"#책갈피1\">테스트</a></p>     <p><a href=\"http://www.navver.com\">테스트</a></p>     <p><a name=\"책갈피1\">테스트</a></p>     <hr />  <p><img alt=\"smiley\" height=\"18\" src=\"http://webmail.ubstorm.co.kr/skin/main/basic/img/btn_logout.gif\" title=\"smiley\" width=\"75\" />  <img alt=\"crying\" height=\"23\" src=\"http://localhost:8080/js/everuxf/lib/ckeditor/plugins/smiley/images/cry_smile.png\" title=\"crying\" width=\"23\" />  <img alt=\"yes\" height=\"23\" src=\"http://localhost:8080/js/everuxf/lib/ckeditor/plugins/smiley/images/thumbs_up.png\" title=\"yes\" width=\"23\" /></p>     <p>  </p>     <div style=\"page-break-after: always\"><span style=\"display:none\">  </span></div>     <p>테스트</p>     <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" dir=\"ltr\" id=\"A000\" style=\"width:500px\" summary=\"Sapmle 표 요약\">               <caption>Sample 표</caption>               <thead>                             <tr>                                          <th scope=\"col\">구분</th>                                          <th scope=\"col\">내용</th>                             </tr>               </thead>               <tbody>                             <tr>                                          <td>1</td>                                          <td>AA</td>                             </tr>                             <tr>                                          <td>2</td>                                          <td>BB</td>                             </tr>               </tbody>  </table>     <hr />  <p>  </p>     <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:280px\">               <tbody>                             <tr>                                          <td colspan=\"2\" style=\"height:22px; text-align:center; width:93px\">10</td>                                          <td colspan=\"2\" style=\"text-align:center; width:93px\">11</td>                                          <td colspan=\"2\" style=\"text-align:center; width:93px\">12</td>                             </tr>                             <tr>                                          <td style=\"height:22px\">　</td>                                          <td colspan=\"4\">분석/설계(2)</td>                                          <td>　</td>                             </tr>               </tbody>  </table>     <p>  </p>     <table border=\"1\" bordercolor=\"#000000\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; width:100%\">               <tbody>                             <tr>                                          <th>NO</th>                                          <th>금형구분</th>                                          <th>차종</th>                                          <th>품번</th>                                          <th>품명</th>                                          <th>SET</th>                                          <th>CVT</th>                                          <th>제품<br />                                          수량</th>                                          <th>결정가</th>                                          <th>정/가단가</th>                                          <th>비고</th>                             </tr>                             <tr>                                          <td>                                          <p>1.</p>                                          </td>                                          <td colspan=\"10\">                                          <p>&quot;갑&quot;과 &quot;을&quot;은 쌍방이 체결한 부품공급 기본계약서 제 9조에 의거 &quot;갑&quot;과 &quot;을&quot;이 상호원만히 합의가 이루어져 상기와 같이 가격 결정에 합의함.</p>                                          </td>                             </tr>                             <tr>                                          <td>                                          <p>2.</p>                                          </td>                                          <td colspan=\"10\">공급자 &quot;을&quot;은 &quot;갑&quot; 소유의 금형을 사용목적에 따라 &#39;신의성실&#39;의 원칙에 의거하여 사용자의 의무를 다하여야한다.</td>                             </tr>                             <tr>                                          <td>                                          <p>3.</p>                                          </td>                                          <td colspan=\"10\">상기 합의서는 동일 적용함.</td>                             </tr>                             <tr>                                          <td>                                          <p>4.</p>                                          </td>                                          <td colspan=\"10\">가격결정 합의서 작성일 :  </td>                             </tr>                             <tr>                                          <td colspan=\"5\">                                          <p>  </p>                                             <p>  공급받는자 :  </p>                                             <p>  대 표 이 사 :  </p>                                          </td>                                          <td colspan=\"6\">                                          <p>  </p>                                             <p>공     급     자 :  </p>                                             <p>대 표 이 사 :  </p>                                          </td>                             </tr>               </tbody>  </table>  </body></html>";
        //String _srcXHtml = "<!DOCTYPE html><html><head>    	<meta charset=\"utf-8\"/>    	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/>	<title>UBIForm TEST</title>    </head><body style=\"font-family: 맑은고딕;\"> <div style=\"text-align: center;\">TEST 제목</div><div style=\"text-align: center;\"><br/></div><div style=\"text-align: justify;\"><table width=\"690\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"border: 1px solid #e4881f;  border-right-style: none; font-family: verdana; font-size: 12px;\"><tbody><tr><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 84px;\"><p style=\"text-align: center;\"><font color=\"#400080\"> 번호</font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 136px;\"><p style=\"text-align: center;\"><font color=\"#400080\">금형구분 </font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 187px;\"><p style=\"text-align: center;\"><font color=\"#400080\">차종 </font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 98px;\"><p style=\"text-align: center;\"><font color=\"#400080\">품번 </font></p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(247, 154, 0); border-right: 1px solid rgb(228, 136, 31); font-weight: normal; width: 181px;\"><p style=\"text-align: center;\"><font color=\"#400080\">비고 </font></p></td></tr><tr><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 84px;\"><p> 1</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 136px;\"><p> 금형12</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 187px;\"><p> 가격결정 합의서 작성일 :</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 98px;\"><p> 2323232</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 181px;\"><p> 상기 합의서는 동일 적용함.</p></td></tr><tr><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 84px;\"><p> 2</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); color: rgb(70, 88, 110); width: 136px;\"><p> 금형100</p></td><td width=\"138\" height=\"24\" style=\"padding: 3px 4px 2px; background-color: rgb(255, 255, 255); border-top: 1px solid rgb(228, 136, 31); border-right: 1px solid rgb(228, 136, 31); width: 465px;\" colspan=\"3\" rowspan=\"1\"><p style=\"\"><font color=\"#46586e\"> </font><span style=\"text-align: justify;\"><font color=\"#46586e\">\"갑\"과 \"을\"은 쌍방이 체결한 부품공급 기본계약서 제 9조에 의거 \"갑\"과 \"을\"이 상호원만히 합의가 이루</font></span><span style=\"color: rgb(70, 88, 110); text-align: justify;\">어져 상기와 같이 가격 결정에 합의함.</span></p></td></tr></tbody></table><p> </p><br/></div></body></html>";
        //String _srcXHtml = "<!DOCTYPE html> <html> <head>     	<meta charset=\"utf-8\"/>     	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/> 	<title>UBIForm TEST</title>      </head>  <body style=\"font-family: 맑은고딕;\">       <p>테스트</p>         <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" dir=\"ltr\" id=\"A000\" style=\"width:500px\" summary=\"Sapmle 표 요약\">                       <caption>Sample 표</caption>                       <thead>                                     <tr>                                                  <th scope=\"col\">구분</th>                                                  <th scope=\"col\">내용</th>                                     </tr>                       </thead>                       <tbody>                                     <tr>                                                  <td>1</td>                                                  <td>AA</td>                                     </tr>                                     <tr>                                                  <td>2</td>                                                  <td>BB</td>                                     </tr>                       </tbody>          </table>                     <hr />          <p> </p>                     <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:280px\">                       <tbody>                                     <tr>                                                  <td colspan=\"2\" style=\"height:22px; text-align:center; width:93px\">10</td>                                                  <td colspan=\"2\" style=\"text-align:center; width:93px\">11</td>                                                  <td colspan=\"2\" style=\"text-align:center; width:93px\">12</td>                                     </tr>                                     <tr>                                                  <td style=\"height:22px\">　</td>                                                  <td colspan=\"4\">분석/설계(2)</td>                                                  <td>　</td>                                     </tr>                       </tbody>          </table>                     <p> </p>                     <table border=\"1\" bordercolor=\"#000000\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse; width:100%\">                       <tbody>                                     <tr>                                                  <th>NO</th>                                                  <th>금형구분</th>                                                  <th>차종</th>                                                  <th>품번</th>                                                  <th>품명</th>                                                  <th>SET</th>                                                  <th>CVT</th>                                                  <th>제품<br />                                                  수량</th>                                                  <th>결정가</th>                                                  <th>정/가단가</th>                                                  <th>비고</th>                                     </tr>                                     <tr>                                                  <td>                                                  <p>1.</p>                                                  </td>                                                  <td colspan=\"10\">                                                  <p>&quot;갑&quot;과 &quot;을&quot;은 쌍방이 체결한 부품공급 기본계약서 제 9조에 의거 &quot;갑&quot;과 &quot;을&quot;이 상호원만히 합의가 이루어져 상기와 같이 가격 결정에 합의함.</p>                                                  </td>                                     </tr>                                     <tr>                                                  <td>                                                  <p>2.</p>                                                  </td>                                                  <td colspan=\"10\">공급자 &quot;을&quot;은 &quot;갑&quot; 소유의 금형을 사용목적에 따라 &#39;신의성실&#39;의 원칙에 의거하여 사용자의 의무를 다하여야한다.</td>                                     </tr>                                     <tr>                                                  <td>                                                  <p>3.</p>                                                  </td>                                                  <td colspan=\"10\">상기 합의서는 동일 적용함.</td>                                     </tr>                                     <tr>                                                  <td>                                                  <p>4.</p>                                                  </td>                                                  <td colspan=\"10\">가격결정 합의서 작성일 : </td>                                     </tr>                                     <tr>                                                  <td colspan=\"5\">                                                  <p> </p>                                                             <p> 공급받는자 : </p>                                                             <p> 대 표 이 사 : </p>                                                  </td>                                                  <td colspan=\"6\">                                                  <p> </p>                                                             <p>공   급   자 : </p>                                                             <p>대 표 이 사 : </p>                                                  </td>                                     </tr>                       </tbody>          </table>   </body> </html>  ";
        //String _srcXHtml = "<!DOCTYPE html> <html> <head>     	<meta charset=\"utf-8\"/>     	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/> 	<title>UBIForm TEST</title>      </head>  <body style=\"font-family: 맑은고딕;\"> <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" dir=\"ltr\" id=\"A000\" style=\"width:500px\" summary=\"Sapmle 표 요약\"><caption>Sample 표</caption>                       <thead>                                     <tr>                                                  <th scope=\"col\">구분</th>                                                  <th scope=\"col\">내용</th>                                     </tr>                       </thead>                       <tbody>                                     <tr>                                                  <td>1</td>                                                  <td>AA</td>                                     </tr>                                     <tr>                                                  <td>2</td>                                                  <td>BB</td>                                     </tr>                       </tbody>          </table>  </body> </html>  ";
        //r.renderXHTML(_srcXHtml, os, type);
        try {
        	String _srcXHtml = _data.toString();
			_retStr = r.renderXHTML(_srcXHtml, type);
			//System.err.println("Done=[" + _retStr + "]");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return _retStr;
	}
	
	
	
	public void destroy()
	{
		_document = null;
		if(mDataSet != null)mDataSet.clear();
		mDataSet = null;
		mDataSetFn = null;
		//mServiceReqMng = null;
		mBandInfoMap = null;
		if(mItems != null)mItems.clear();
		mItemPropVar = null;
		mPropertyFn = null;
	}
	
}
