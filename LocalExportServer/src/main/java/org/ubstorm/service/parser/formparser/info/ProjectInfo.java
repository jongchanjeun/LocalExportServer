package org.ubstorm.service.parser.formparser.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.TempletItemInfo;
import org.ubstorm.service.parser.formparser.data.Value;
//import org.ubstorm.service.request.ServiceRequestManager;
import org.ubstorm.service.utils.common;

public class ProjectInfo {
	
	// 문서별 타입
	public static final String FORM_TYPE_COVERPAGE = "0";
	public static final String FORM_TYPE_FREE_FORM = "1";
	public static final String FORM_TYPE_MASTERBAND = "2";
	public static final String FORM_TYPE_CONTINUEBNAD = "3";
	public static final String FORM_TYPE_LABELBAND = "4";
	public static final String FORM_TYPE_MOBILE = "7";
	public static final String FORM_TYPE_LASTPAGE = "8";
	public static final String FORM_TYPE_WEB = "9";
	public static final String FORM_TYPE_CONNECT_LINKED = "12";
	public static final String FORM_TYPE_LINKED_PAGE = "14";
	public static final String FORM_TYPE_REPEAT_PAGE = "16";
	public static final String TYPE_NORMAL = "JSON";
	public static final String TYPE_SIMPLE = "SIMPLE";
	
	private String TYPE = "normal";		// s
	
	public static final int LOAD_TYPE_XML = 0;
	public static final int LOAD_TYPE_JSON = 1;
	
	protected HashMap<String, TempletItemInfo> mTempletInfo;
	
//	protected ServiceRequestManager mServiceReqMng;
	
	private int mProjectIndex = 0;
	
	protected ArrayList<PageInfo> mPages;				// Page들의 정보를 담아두는 배열
	protected ArrayList<DataSetInfo> mDataSetInfos;			// 데이터셋의 정보를 담아두는 배열
	protected ArrayList<HashMap<String, String>> mParams;
	
	protected String mProjectType = "";
	protected String mClientEditMode = "";
	protected String mFnVersion = "2.0";
	protected String mWaterMark = "";
	
	protected float mMarginTop  	= 0;
	protected float mMarginLeft  	= 0;
	protected float mMarginRight  	= 0;
	protected float mMarginBottom  	= 0;

	protected String mPageContinue	= "false";
	protected int mPageCount = -1;
	
	
	protected String mProjectName = "";
	protected String mFormName = "";
	protected String mDescription = "";

	protected String mPassWord = "";

	protected String mPageBackgroundColor = "";
	
	protected HashMap<String, String> mWaterMarkInfo;
	
	protected String mFontUnit = "px";

	protected String mExcelIncludeImage = "true";

	protected String mExcelUsePageHeight = "true";
	
	protected ArrayList<PageInfo> mPageInfoList;
	
	protected ArrayList<PageInfoSimple> mPageInfoSimpleList;	// 속성을 필요한 속성만 전달을 위하여 수정
	
	protected HashMap<String, ArrayList<HashMap<String, Object>>> mDataSet;
	
	protected HashMap<String, HashMap<String, String>> mParameter = new HashMap<String, HashMap<String, String>>();
	
	protected JSONObject mParam;
	
	protected String mIsExportType = "";
	
	protected Function mFunction;
	
	protected HashMap<String, Object> mPdfSet;
	
	protected HashMap<String, Object> mPageGroup;
	
	interface IMethodBar {
		void callMethod( Object value );
	}
	 
	// project정보를 담기
	// pages / dataset / parameter 을 담고 있는 객체
	
	HashMap<String, IMethodBar> propertyMapping = new HashMap<String, IMethodBar>();
		
	public ProjectInfo() {
		// TODO Auto-generated constructor stub
	}

	public ProjectInfo(JSONObject _param, String _jsonStr) {
		// TODO Auto-generated constructor stub
		JSONObject _projectInfo = (JSONObject) JSONValue.parse(  _jsonStr );
		mParam = _param;
		
		propertyMappingFn();
		
		setProperties((JSONObject) _projectInfo.get("project"));
	}
	
	public ProjectInfo(JSONObject _param, File _jsonFile, String _isExport ) throws FileNotFoundException, UnsupportedEncodingException 
	{
		TYPE = GlobalVariableData.M_FILE_LOAD_TYPE_JSON;
		initFileLoad(_param, _jsonFile, _isExport);
	}
	public ProjectInfo(JSONObject _param, File _jsonFile, String _isExport, String _type ) throws FileNotFoundException, UnsupportedEncodingException 
	{
		TYPE = _type;
		initFileLoad(_param, _jsonFile, _isExport);
	}
	public ProjectInfo(JSONObject _param, String _jsonStr, String _isExport, String _type ) throws FileNotFoundException, UnsupportedEncodingException 
	{
		TYPE = _type;
		initFileLoad(_param, _jsonStr, _isExport);
	}
	
	public void initFileLoad(JSONObject _param, File _jsonFile, String _isExport ) throws FileNotFoundException, UnsupportedEncodingException 
	{
		// TODO Auto-generated constructor stub
		FileInputStream inputStream= new FileInputStream(_jsonFile);
		mParam = _param;
		mIsExportType = _isExport;
		
		JSONObject _projectInfo = null;
		try {
			_projectInfo = (JSONObject) JSONValue.parse( common.readJsonReader(inputStream,"UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		propertyMappingFn();
		setProperties(_projectInfo);
		
		// 템플릿 생성필요 
		
		// 템플릿의 parameter값 가져오기
	}
	
	public void initFileLoad(JSONObject _param, String _jsonStr, String _isExport ) throws FileNotFoundException, UnsupportedEncodingException 
	{
		// TODO Auto-generated constructor stub
		mParam = _param;
		mIsExportType = _isExport;
		
		JSONObject _projectInfo = (JSONObject) JSONValue.parse( _jsonStr );
		
		propertyMappingFn();
		setProperties(_projectInfo);
		
		// 템플릿 생성필요 
		
		// 템플릿의 parameter값 가져오기
	}
	
	
	public void setIsExportType( String _isExport )
	{
		mIsExportType = _isExport;
	}
	public String getIsExportType()
	{
		return mIsExportType;
	}
	
	//project기본 셋팅
	private void setProperties( JSONObject _jObj )
	{
		for (Object key : _jObj.keySet()) {
	        String keyStr = (String)key;
	        Object keyvalue = _jObj.get(keyStr);
	        
	        if( propertyMapping.containsKey(keyStr) )
	        {
				propertyMapping.get(keyStr).callMethod( keyvalue );
	        }
	    }
		
	}
	
	public void setFunction( Function _value )
	{
		mFunction = _value;
	}
	public Function getFunction()
	{
		return mFunction;
	}
	
	public PageInfo getPageInfo( int _idx )
	{
		if(mPageInfoList != null && mPageInfoList.size() > _idx )
		{
			return mPageInfoList.get(_idx);
		}
		
		return null;
	}
	
	public PageInfoSimple getPage( int _idx )
	{
		if(mPageInfoSimpleList != null && mPageInfoSimpleList.size() > _idx )
		{
			return mPageInfoSimpleList.get(_idx);
		}
		
		return null;
	}
	
	protected void setPages(List<Object> _pageList )
	{
		HashMap<String, Object > _pageItem;
		
		if( TYPE.equals(ProjectInfo.TYPE_SIMPLE) )
		{
			if(mPageInfoSimpleList == null )mPageInfoSimpleList = new ArrayList<PageInfoSimple>();
			
			if( mPageCount == -1 ) mPageCount = _pageList.size();
			
			// 페이지 수만큼 pageInfo생성 하여 배열에 담기 
			for( int i = 0; i < _pageList.size(); i++ )
			{
				_pageItem = (HashMap<String, Object >) _pageList.get(i);
				
				if(mPageInfoSimpleList.size() > 0 && _pageItem.containsKey("isConnect") && _pageItem.get("isConnect") != null && _pageItem.get("isConnect").toString().equals("true") )	
				{
					mPageInfoSimpleList.get(mPageInfoSimpleList.size()-1).setItems( (List<Object>) _pageItem.get("items") );
				}
				else
				{
					PageInfoSimple _page = new PageInfoSimple(this, _pageItem, null );
					_page.setIsExportType( mIsExportType );
					mPageInfoSimpleList.add(_page);
				}
			}
		}
		else
		{
			if(mPageInfoList == null )mPageInfoList = new ArrayList<PageInfo>();
			
			if( mPageCount == -1 ) mPageCount = _pageList.size();
			
			// 페이지 수만큼 pageInfo생성 하여 배열에 담기 
			for( int i = 0; i < _pageList.size(); i++ )
			{
				_pageItem = (HashMap<String, Object >) _pageList.get(i);
				
				if(mPageInfoList.size() > 0 && _pageItem.containsKey("isConnect") && _pageItem.get("isConnect") != null && _pageItem.get("isConnect").toString().equals("true") )
				{
					mPageInfoList.get(mPageInfoList.size()-1).setItems( (List<Object>) _pageItem.get("items") );
				}
				else
				{
					PageInfo _page = new PageInfo(this, _pageItem, null );
					_page.setIsExportType( mIsExportType );
					mPageInfoList.add(_page);
				}
			}
		}
		
		
	}
	
/*
	public void setServiceReqMng(ServiceRequestManager _value)
	{
		mServiceReqMng = _value;
	}

	public ServiceRequestManager getServiceReqMng()
	{
		return mServiceReqMng;
	}
*/	
	
	private void propertyMappingFn()
	{
		propertyMapping.put("clientEditMode", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setClientMode(value.toString()); } } );

		propertyMapping.put("id", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setClientMode(value.toString()); } } );

		propertyMapping.put("desc", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDescription(value.toString()); } } );

		propertyMapping.put("waterMark", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setWaterMark(value.toString()); } } );
		
		propertyMapping.put("watermark", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setWaterMarkMap( (HashMap<String, String>) value); } } );
		
		propertyMapping.put("marginTop", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setMarginTop( value.toString() ); } } );
		propertyMapping.put("marginLeft", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setMarginLeft( value.toString() ); } } );
		propertyMapping.put("marginRight", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setMarginRight(value.toString() ); } } );
		propertyMapping.put("marginBottom", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setMarginBottom(value.toString() ); } } );
		propertyMapping.put("pages", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setPages( (List<Object>) value); } } );

		propertyMapping.put("pw", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setPassword( value.toString() ); } } );

		propertyMapping.put("fnVersion", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setFnVersion( value.toString() ); } } );

		propertyMapping.put("pageBackgroundColor", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setPageBackgroundColor( value.toString() ); } } );

		propertyMapping.put("excelIncludeImage", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setExcelIncludeImage( value.toString() ); } } );

		propertyMapping.put("excelUsePageHeight", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setExcelUsePageHeight( value.toString() ); } } );
		

		propertyMapping.put("projectType", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setProjectType( value.toString() ); } } );
		
		propertyMapping.put("datasets", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDataSets( (List<Object>) value ); } } );

		propertyMapping.put("pageContinue", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setPageContinue( value.toString() ); } } );
		
		propertyMapping.put("params", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setParameter( (ArrayList<HashMap<String, String>>) value ); } } );

		propertyMapping.put("pdfset", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setPdfSet( (HashMap<String, Object>) value ); } } );
		
		propertyMapping.put("pageGroup", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setPageGroup( (HashMap<String, Object>) value ); } } );

		propertyMapping.put("pageCount", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setPageCount( value.toString() ); } } );
		
	}
	
	public void setParameter( ArrayList<HashMap<String, String>> _value )
	{
		if( _value != null && _value.size() > 0 )
		{
			if(mParameter == null) mParameter = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> _param;
			String _id = "";
			
			// 프로젝트의 params 의 param값을 가져오기 
			// id / type / desc / parameter / parameterDefault / usrParam 
			for(int i = 0; i < _value.size(); i++ )
			{
				_param = _value.get(i);
				_id = _param.get("id");
				
				if( mParameter.containsKey(_id) == false ) mParameter.put( _id, _param );
			}
		}
		
	}
	
	public void setPdfSet( HashMap<String, Object> _pdfSet )
	{
		mPdfSet = _pdfSet;
	}
	public HashMap<String, Object> getPdfSet()
	{
		return mPdfSet;
	}
	
	public HashMap<String, HashMap<String, String>> getParameter()
	{
		return mParameter;
	}
	
	public void setFnVersion( String _value )
	{
		mFnVersion = _value;
	}
	
	public String getFnVersion()
	{
		return mFnVersion;
	}

	
	public void setPageContinue(String _value )
	{
		mPageContinue = _value;
	}
	public String getPageContinue()
	{
		return mPageContinue;
	}
	
	public void setPageCount(String _value)
	{
		mPageCount  = Integer.parseInt( _value );
	}
	
	public int getPageCount()
	{
		return mPageCount;
	}
	
	public void setProjectType( String _value )
	{
		mProjectType = _value;
	}
	
	public String getProjectType()
	{
		return mProjectType;
	}

	public void setExcelIncludeImage( String _value )
	{
		mExcelIncludeImage = _value;
	}
	
	public String getExcelIncludeImage()
	{
		return mExcelIncludeImage;
	}

	public void setExcelUsePageHeight( String _value )
	{
		mExcelUsePageHeight = _value;
	}
	
	public String getExcelUsePageHeight()
	{
		return mExcelUsePageHeight;
	}

	public void setPageBackgroundColor( String _value )
	{
		mPageBackgroundColor = _value;
	}
	
	public String getPageBackgroundColor()
	{
		return mPageBackgroundColor;
	}

	public void setClientMode( String _value )
	{
		mClientEditMode = _value;
	}
	
	public String getClientMode()
	{
		return mClientEditMode;
	}
		
	public void setWaterMark( String _value )
	{
		mWaterMark = _value;
	}
	
	public String getWaterMark()
	{
		return mWaterMark;
	}
	
	public void setDescription( String _value )
	{
		mDescription = _value;
	}
	public String getDescription()
	{
		return mDescription;
	}
	

	public void setMarginTop( String _value )
	{
		mMarginTop = Float.valueOf(_value);
	}
	
	public float getMarginTop()
	{
		return mMarginTop;
	}
	
	protected void setMarginLeft( String _value )
	{
		mMarginLeft = Float.valueOf(_value);
	}
	public float getMarginLeft()
	{
		return mMarginLeft;
	}
	
	
	protected void setMarginRight( String _value )
	{
		mMarginRight = Float.valueOf(_value);
	}
	public float getMarginRight()
	{
		return mMarginRight;
	}
	
	
	
	protected void setMarginBottom( String _value )
	{
		mMarginBottom = Float.valueOf(_value);
	}
	public float getMarginBottom()
	{
		return mMarginBottom;
	}
	
	
	protected void setPassword(String _value)
	{
		mPassWord = _value;
	}
	public String getPassword()
	{
		return mPassWord;
	}
	
	public void setFontUint(String _value)
	{
		mFontUnit = _value;
	}
	public String getFontUnit()
	{
		return mFontUnit;
	}
	 
	public void setWaterMarkMap( HashMap<String, String> _value )
	{
		//alpha, data , x, y, text, type, height
		
		mWaterMarkInfo = _value;
	}
	public HashMap<String, String> getWaterMarkMap()
	{
		return mWaterMarkInfo;
	}
	
	public void setDataSets( List<Object> _value )
	{
		// ubdmc들을 가져와서 datasetInfo객체를 생성 ( 생성후 파라미터를 parameter에 담기 )
		
		if(mDataSetInfos == null ) mDataSetInfos = new ArrayList<DataSetInfo>();
		
		for( int i=0; i < _value.size(); i++ )
		{
			DataSetInfo _ds = new DataSetInfo( (HashMap<String, Object>) _value.get(i));
			mDataSetInfos.add(_ds);
			
			setParameter( _ds.getParams() );
		}
		
	}
	
	
	public ArrayList<DataSetInfo> getDataSets()
	{
		return mDataSetInfos;
	}
	
	public void setParam( JSONObject _value)
	{
		mParam = _value;
	}
	
	public JSONObject getParam()
	{
		return mParam;
	}
	
	public void setDataSet( HashMap<String, ArrayList<HashMap<String, Object>>> _value )
	{
		mDataSet = _value;
	}
	public HashMap<String, ArrayList<HashMap<String, Object>>> getDataSet()
	{
		return mDataSet;
	}
	
	public ArrayList<PageInfo> getPageList()
	{
		return mPageInfoList;
	}
	public void setPageList( ArrayList<PageInfo> _value )
	{
		mPageInfoList = _value;
	}
	
	public ArrayList<PageInfoSimple> getPages()
	{
		return mPageInfoSimpleList;
	}
	
	public void setPages( ArrayList<PageInfoSimple> _value )
	{
		mPageInfoSimpleList = _value;
	}
	
	public int getProjectIndex()
	{
		return mProjectIndex;
	}
	
	public void setProjectIndex( int _value )
	{
		mProjectIndex = _value;
	}
	
	public void setProjectName( String _value)
	{
		mProjectName = _value;
	}
	public void setFormName( String _value)
	{
		mFormName = _value;
	}
	
	public String getProjectName()
	{
		return mProjectName;
	}
	public String getFormName()
	{
		return mFormName;
	}
	
	
	
	public HashMap<String, TempletItemInfo> getTempletInfo()
	{
		if( mTempletInfo == null ) mTempletInfo = new HashMap<String, TempletItemInfo>();
		return mTempletInfo;
	}
	
	public void setTempletInfo( HashMap<String, Object> _templetInfo)
	{
		//templet 생성 
		//this.getTempletInfo().put( _templetInfo.get("id").toString() , new TempletItemInfo( _templetInfo, mParam, mServiceReqMng, mIsExportType, false ));
	}
	
	public void setPageGroup( HashMap<String, Object> _pageGroup )
	{
		JSONParser _jparser = new JSONParser();
		JSONArray _dataAr;
		ArrayList<HashMap<String, Object>> _resultPageGroup = new ArrayList<HashMap<String, Object>>();
		
		try {
			
			if(_pageGroup.containsKey("pageGroupData"))
			{
				_dataAr = (JSONArray) _jparser.parse( URLDecoder.decode( _pageGroup.get("pageGroupData").toString(), "UTF-8") );
				
				JSONObject _data;
				
				HashMap<String, Object> _groupColumn;
				for( int j=0; j < _dataAr.size(); j++ )
				{
					_groupColumn = new HashMap<String, Object>();
					_data = (JSONObject) _dataAr.get(j);
					_groupColumn.put("dataset", _data.get("dataset") );
					_groupColumn.put("columns", new ArrayList<String>(Arrays.asList(_data.get("groupColumn").toString().split(","))) );
					_resultPageGroup.add( _groupColumn );
				}
				
				_pageGroup.put("pageGroupData", _resultPageGroup);
			
			}
			if( _pageGroup.containsKey("usePageGroup") )
			{
				_pageGroup.put("usePageGroup", _pageGroup.get("usePageGroup").toString().equals("true"));
			}
			if( _pageGroup.containsKey("useFileSplit") )
			{
				_pageGroup.put("useFileSplit", _pageGroup.get("useFileSplit").toString().equals("true"));
			}
			if( _pageGroup.containsKey("downLoadFileName") )
			{
				_pageGroup.put("downLoadFileName", URLDecoder.decode( _pageGroup.get("downLoadFileName").toString(), "UTF-8") );
			}
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mPageGroup = _pageGroup;
	}
	public HashMap<String, Object> getPageGroup()
	{
		return mPageGroup;
	}
	
	public String getType()
	{
		return TYPE;
	}
	
	
	public JSONObject getProjectMap()
	{
		JSONObject _resultMap = new JSONObject();
		
		_resultMap.put("fnVersion", mFnVersion );
		_resultMap.put("pageBackgroundColor", mPageBackgroundColor );
		_resultMap.put("pw", mPassWord );
		_resultMap.put("projectType", mProjectType );
		
		/**
			"fnVersion":"2.0",
			"pageBackgroundColor":"#dadada",
			"pw":"",
			"excelIncludeImage":"true",
			"projectType":"1",
			"pageHeight":"1123",
			"pageWidth":"794",
			"marginRight":"56",
			"editMenuEnable":"true",
			"maxHeight":"1123",
			"option3":"2019-01-31",
			"option1":"1",
			"maxWidth":"794",
			"editorVersion":"2.0",
			"pageContinue":"false",
			"pageCount":"2",
			"docType":"0",
			"totalPage":"2",
			"pageBackgroundColorInt":"14342874",
			"backgroundTempletID":"backid",
			"marginLeft":"56",
			"clientEditMode":"Off",
			"saveASMenuEnable":"true",
			"marginBottom":"40",
			"writer":"ubstorm",
			"projectName":"CSFNCFM605R01",
			"waterMark":"",
			"xmlVersion":"1.0",
			"marginTop":"0",
			"desc":"Confirmation Letter to Car Dealer_ENG_KBFG"
		**/
		
		return _resultMap;
	}
	
	public void createItems()
	{
		int i;
		int cnt = mPages.size();
		
		for( i=0;i <cnt;i++ )
		{
			mPages.get(i).createItem();
		}
		
	}
	
	public void clear()
	{
		if(mTempletInfo!=null) mTempletInfo.clear();
		if(mWaterMarkInfo!=null) mWaterMarkInfo.clear();
		if(mDataSet!=null) mDataSet.clear();
		if(mDataSetInfos!=null) mDataSetInfos.clear();
		if(mPages!=null) mPages.clear();
		
		if(mPageInfoList!=null)
		{
			for( int i = 0; i < mPageInfoList.size(); i++ )
			{
				mPageInfoList.get(i).clear();
			}
			
			mPageInfoList.clear();
		}

		if(mPageInfoSimpleList  !=null)
		{
			for( int i = 0; i < mPageInfoSimpleList.size(); i++ )
			{
				mPageInfoSimpleList.get(i).clear();
			}
			
			mPageInfoSimpleList.clear();
		}

		mTempletInfo = null;
		mPageInfoList = null;
	}
	
	public void setType(String _value)
	{
		TYPE = _value;
	}
	
}
