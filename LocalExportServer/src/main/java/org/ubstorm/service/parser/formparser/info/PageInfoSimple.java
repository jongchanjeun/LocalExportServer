package org.ubstorm.service.parser.formparser.info;

import java.awt.Color;
import java.awt.Point;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.GroupingDataSetProcess;
import org.ubstorm.service.parser.ItemPropertyProcess;
import org.ubstorm.service.parser.ItemPropertyVariable;
import org.ubstorm.service.parser.UBIDataParser;
import org.ubstorm.service.parser.formparser.ItemConvertParser;
import org.ubstorm.service.parser.formparser.UBIDataUtilPraser;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.BandInfoMapDataSimple;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.info.item.UBComponent;
import org.ubstorm.service.utils.common;


/**
 *	페이지별 정보를 담아두는 객체 
 *  페이지 속성
 *  아이템
 *  밴드
 *  필수값/TabIndex
 *  등의 정보를 담아두는 객체 
 **/
public class PageInfoSimple{
	
	HashMap<String, Object > mPageMap;
	
	protected int mWidth = 794;					// 페이지의 width
	protected int mHeight = 1123;					// 페이지의 height
	protected float mBackgroundAlpha = 0;			// 페이지의 Background Alpha
	protected int mCloneRowCount = 0;				// 클론페이지의 Row 분할 수
	protected int mCloneColCount = 0;				// 클론페이지의 Column 분할 수
	protected int mMinimumResizeFontSize = 0;		// ResizeFont의 최소값 

	protected String mReportType = "1";					// 페이지의 ReportType 
	protected String mId = "page1";						// 페이지 ID
	protected String mDivide;							// 
	protected String mBackgroundColor = "#ffffff";	// 페이지의 Background Color
	protected int mBackgroundColorInt = 16777215;				// 페이지의 Background Color
	protected String mClone = "0";						// 페이지의 Clone의 타입
	protected String mIsPivot = "";						// 페이지 Pivot 사용 여부
	protected String mIsGroup	= "false";						// 페이지의 IsGroup 기능 사용 여부
	protected String mIsConnect = "false";					// 이전 페이지와 연결 여부 지정
	protected String mDirection = "crossDown";					// 클론페이지의 진행방향 
	protected String mUseGroupPageClone	=	"false";			// Clone페이지와 IsGroup사용시 같은 페이지에 표시 여부
	protected String mPageBackgroundImage = "";			// 백그라운드 이미지
	protected Object mPageParserClass;
	protected int mDataRowCount = 0;
	
	protected boolean mFitOnePage = false;
	
	protected String[] mPropertyAr = {"width","height","backgroundColor","backgroundAlpha"};
	protected ArrayList<String> mPropertyListAr = new ArrayList<String>();
	protected HashMap<String, String> mPageProperty = new HashMap<String, String>();
	protected ArrayList<HashMap<String, String>> mUbfunction;
	protected ArrayList<HashMap<String, Object>> mGroupData;
	protected JSONObject mBackgroundImage = new JSONObject();
	protected ProjectInfo mProjectInfo;
	protected ArrayList<String> mRequiredValueList = new ArrayList<String>();
	protected ArrayList<String> mTabIndexList = new ArrayList<String>();
	private ArrayList<HashMap<String, Object>> mItems;	// 페이지의 아이템들을 담아두는 배열
	protected ArrayList<HashMap<String, Object>> mPageItemList = new ArrayList<HashMap<String, Object>>();
	protected HashMap<String, BandInfoMapDataSimple> bandInfoData = new HashMap<String, BandInfoMapDataSimple>();
	protected ArrayList<BandInfoMapDataSimple> bandList = new ArrayList<BandInfoMapDataSimple>();
	protected HashMap<String, Integer> mGgroupBandCntMap;
	protected HashMap<String, ArrayList<HashMap<String,Object>>> mDataSet;
	private ArrayList<List<Object>> mItemList;
	private List<Object>  mItem;
	private ArrayList<Integer> mXArr = new ArrayList<Integer>();
	
	private boolean mIsCreate = false;
	protected int mProjectIndex = 0;
	protected String isExportType = "";

	protected ArrayList<HashMap<String, String>> mGroupColumn = null;
	protected ArrayList<HashMap<String, String>> mVisibleParam = null;
	protected String mVisibleResultFlag = "true";
	
	protected HashMap<String, Integer> mLabelBandInfo = new HashMap<String, Integer>();
	
	// Group사용시 기본 밴드리스트를 담아두는 객체
	protected  ArrayList<BandInfoMapDataSimple> mDefaultBandList= null;
	// Group사용시 기본 밴드들의 정보를 담아두는 객체 
	protected  HashMap<String, BandInfoMapDataSimple> mDefaultBandInfo = null;
	
	interface IMethodBar {
		void callMethod( Object value );
	}
	 
	// page 정보를 담고 있는 객체  items( item/band ) / ubfx/ groupData/ requiredValueList/ tabIndexList 
	
	HashMap<String, IMethodBar> propertyMapping = new HashMap<String, IMethodBar>();
	
	public PageInfoSimple() {
		// TODO Auto-generated constructor stub
	}
	
	public PageInfoSimple(ProjectInfo _project,  HashMap<String, Object > _pageMap,HashMap<String, ArrayList<HashMap<String,Object>>> _data ) {
		// TODO Auto-generated constructor stub
		
		setProjectInfo(_project);
		
		mPropertyListAr = new ArrayList<String>(Arrays.asList(mPropertyAr));
		
		propertyMappingFn();
		
		if(_data != null ) mDataSet = _data;
		else mDataSet = _project.getDataSet();
		
		mPageMap = _pageMap;
		
		setProperties(_pageMap);
	}
	
	public void setIsExportType(String _value)
	{
		isExportType = _value;
	}
	

	//project기본 셋팅
	private void setProperties( HashMap<String, Object> _jObj )
	{
		String _value = "";
		
		for (Object key : _jObj.keySet()) {
	        String keyStr = (String)key;
	        Object keyvalue = _jObj.get(keyStr);
	        
	        if( propertyMapping.containsKey(keyStr) )
	        {
				propertyMapping.get(keyStr).callMethod( keyvalue );
	        }
	        
	        if( mPropertyListAr.indexOf(keyStr) != -1 )
	        {
	        	_value = keyvalue.toString();
	        	
				if(keyStr.indexOf("Color") != -1)
				{
					if(keyStr.equals("backgroundColor")) mBackgroundColorInt = Integer.parseInt(keyvalue.toString());
					
					mPageProperty.put(keyStr + "Int", keyvalue.toString());
					_value = changeColorToHex(Integer.parseInt(_value));
				}
				
				if( propertyMapping.containsKey(keyStr) )
		        {
					propertyMapping.get(keyStr).callMethod( _value );
		        }
				
	        	mPageProperty.put(keyStr, _value);
	        }
	    }
	}
	
	
	private void propertyMappingFn()
	{
		propertyMapping.put("width", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setWidth( value.toString()  ); } } );

		propertyMapping.put("height", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setHeight( value.toString() ); } } );

		propertyMapping.put("backgroundAlpha", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setBackgroundAlpha( value.toString() ); } } );
		
		propertyMapping.put("id", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setId( value.toString()  ); } } );

		propertyMapping.put("divide", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDivide( value.toString()  ); } } );

		propertyMapping.put("backgroundColor", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setBackgroundColor( value.toString()  ); } } );

		propertyMapping.put("reportType", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setReportType( value.toString()  ); } } );

		propertyMapping.put("clone", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setClone( value.toString()  ); } } );

		propertyMapping.put("isPivot", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setIsPivot( value.toString()  ); } } );

		propertyMapping.put("isGroup", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setIsGroup( value.toString()  ); } } );

		propertyMapping.put("isConnect", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setIsConnect( value.toString()  ); } } );

		propertyMapping.put("cloneRowCount", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setCloneRowCount( value.toString() ); } } );

		propertyMapping.put("cloneColCount", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setCloneColCount( value.toString() ); } } );

		propertyMapping.put("direction", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDirection( value.toString() ); } } );
		
		propertyMapping.put("useGroupPageClone", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setUseGroupPageClone( value.toString() ); } } );
		
		propertyMapping.put("minimumResizeFontSize", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setMinimumResizeFontSize( value.toString()  ); } } );

		propertyMapping.put("items", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setItems( (List<Object>) value  ); } } );

		propertyMapping.put("requiredValueList", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setRequiredValueList( (ArrayList<String>) value  ); } } );
		
		propertyMapping.put("tabIndexList", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setTabIndexList( (ArrayList<String>) value  ); } } );

		propertyMapping.put("groupData", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setGroupData( (ArrayList<String>) value  ); } } );

		propertyMapping.put("backgroundImage", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setBackgroundImage( ( HashMap<String, String> ) value  ); } } );

		propertyMapping.put("ubfx", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setUbfunction( ( ArrayList<HashMap<String, String>>) value  ); } } );
		
		propertyMapping.put("visibleParam", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setVisibleParam( value  ); } } );
		
		propertyMapping.put("groupColumn", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setGroupColumn( (ArrayList<String>) value ); } } );

		propertyMapping.put("fitOnePage", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setFitOnePage( (String) value ); } } );
		
	}
	public void setItems( List<Object> _value )
	{
		mItem = _value;
		
		if(mItemList == null ) mItemList = new ArrayList<List<Object>>();
		
		HashMap<String, Object> _item;
		
		for( int i=0; i < _value.size(); i++ )
		{
			_item = (HashMap<String, Object>) _value.get(i);
			
			if( _item.get("className") != null && _item.get("className").toString().equals("UBTemplateArea") )
			{
				getProjectInfo().setTempletInfo(_item);
			}
		}
		
		mItemList.add(_value);
	}
	
	public void createItem()
	{
		int i = 0;  
		int j = 0;  
		int max = 0;
		int pageCnt = 0;
		HashMap<String, Object> _item;
		String _className = "";
		
		if(mIsCreate) return;
		
		if( mDataSet == null )
		{
			if( getProjectInfo().getDataSet() == null ) mDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
			else mDataSet = getProjectInfo().getDataSet(); 
		}
		
		// list를 돌아서 item을 생성 
		if( mItemList != null )
		{
			 List<Object> _pageItems;
			
			if( !this.getReportType().equals(ProjectInfo.FORM_TYPE_LABELBAND) )
			{
				ArrayList<HashMap<String, Object>> _bandItems = new ArrayList<HashMap<String, Object>>();
				//Band로드 
				pageCnt = mItemList.size();
				
				for( j =0; j < pageCnt; j++ )
				{
					_pageItems = mItemList.get(j);
					max = _pageItems.size();
					
					for( i=0; i < max; i++ )
					{
						_item = (HashMap<String, Object>) _pageItems.get(i);
						
						if( _item.containsKey("className"))
						{
							_className = _item.get("className").toString();
							
							//BAND일때 
							if(  _className.length() > 4 && _className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") )
							{
								_bandItems.add(_item);
							}
						}
					}
					
				}
				// band list sort
				/**
				ArrayList<String> _columnSort = new ArrayList<String>();
				_columnSort.add("y");
				ArrayList<String> _numericSort = new ArrayList<String>();
				_numericSort.add("true");
				
				_bandItems = UBIDataParser.sortDataSet(_bandItems, _columnSort, null, _numericSort);
				*/
				
				if( _bandItems.size() > 0 ) convertBandItemList(_bandItems);
				
				_bandItems.clear();
				_bandItems = null;
			}
			
			if( mDefaultBandInfo == null && mDefaultBandList == null )
			{
				pageCnt = mItemList.size();
				
				for( j =0; j < pageCnt; j++ )
				{
					
					_pageItems = mItemList.get(j);
					max = _pageItems.size();
					
					//아이템 로드
					for( i=0; i < max; i++ )
					{
						_item = (HashMap<String, Object>) _pageItems.get(i);
						
						if(_item.containsKey("visible") && _item.get("visible").toString().equals("false")) continue;
						
						if( _item.containsKey("className"))
						{
							_className = _item.get("className").toString();
							
							//BAND일때 
							if( !this.getReportType().equals(ProjectInfo.FORM_TYPE_LABELBAND) && _className.length() > 4 && _className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") )
							{
								
							}
							else if( _className.equals("UBTable") || _className.equals("UBApproval") )
							{
								//Table일때 
								try {
									convertTableItem( _item , null);
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ScriptException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							else
							{
								//그외 Item
								try {
									convertItem( _item , null );
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								
							}
						}
					}
				}
			}
			
		}
		
		updateBandInfoTable();
		// Table일경우 테이블 객체 생성로직 필요
		
		// 밴드일경우 bandInfo에 담도록 처리한다.
		
		
		mIsCreate = true;
	}

	public float getWidth()
	{
		return mWidth;
	}
	public void setWidth( String _value )
	{
		mWidth = Float.valueOf(_value).intValue();
	}
	
	public int getCloneRowCount()
	{
		return mCloneRowCount;
	}
	
	public void setCloneRowCount( String _value )
	{
		mCloneRowCount = Integer.valueOf(_value);
	}
	
	
	public int getCloneColCount()
	{
		return mCloneColCount;
	}
	public void setCloneColCount( String _value )
	{
		mCloneColCount=  Integer.valueOf(_value);
	}
	
	
	public float getHeight()
	{
		return mHeight;
	}
	public void setHeight( String _value )
	{
		mHeight = Float.valueOf(_value).intValue();
	}
	
	public float getBackgroundAlpha()
	{
		return mBackgroundAlpha;
	}
	public void setBackgroundAlpha( String _value )
	{
		mBackgroundAlpha = Float.valueOf(_value);
	}
	
	public String getId()
	{
		return mId;
	}
	public void setId(String _value )
	{
		mId = _value;
	}

	public String getDivide()
	{
		return mDivide;
	}
	public void setDivide(String _value )
	{
		mDivide = _value;
	}
	
	public int getBackgroundColorInt()
	{
		return mBackgroundColorInt;
	}
	public String getBackgroundColor()
	{
		return mBackgroundColor;
	}
	public void setBackgroundColor(String _value )
	{
		mBackgroundColor = _value;
	}
	
	public String getReportType()
	{
		return mReportType;
	}
	public void setReportType(String _value )
	{
		mReportType = _value;
	}

	public String getClone()
	{
		return mClone;
	}
	public void setClone(String _value )
	{
		mClone = _value;
	}

	public String getIsPivot()
	{
		return mIsPivot;
	}
	public void setIsPivot(String _value )
	{
		mIsPivot = _value;
	}
	
	public String getIsGroup()
	{
		return mIsGroup;
	}
	public void setIsGroup(String _value )
	{
		mIsGroup = _value;
	}
	
	public String getIsConnect()
	{
		return mIsConnect;
	}
	public void setIsConnect(String _value )
	{
		mIsConnect = _value;
	}

	public String getDirection()
	{
		return mDirection;
	}
	public void setDirection(String _value )
	{
		mDirection = _value;
	}
	
	public String getUseGroupPageClone()
	{
		return mUseGroupPageClone;
	}
	public void setUseGroupPageClone(String _value )
	{
		mUseGroupPageClone = _value;
	}
	
	public int getMinimumResizeFontSize()
	{
		return mMinimumResizeFontSize;
	}
	public void setMinimumResizeFontSize(String _value )
	{
		mMinimumResizeFontSize = Integer.valueOf(_value);
	}
	
	public ArrayList<HashMap<String, String>> getUbfunction()
	{
		return mUbfunction;
	}

	public void setUbfunction( ArrayList<HashMap<String, String>> _value )
	{
		mUbfunction = _value;
	}

	
	public void setGroupData( ArrayList<String> _value )
	{
		ArrayList<HashMap<String, Object>> _grpData = new ArrayList<HashMap<String, Object>>();
		
		String _grpStr = "";
		for(int i=0; i < _value.size(); i++ )
		{
			try {
				_grpStr = URLDecoder.decode(_value.get(i), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				_grpStr = _value.get(i);
			}
			_grpData.add( (HashMap<String, Object>) JSONValue.parse(_grpStr) );
		}
		
		mGroupData = _grpData;
	}
	
	public ArrayList<HashMap<String, Object>> getGroupData()
	{
		return mGroupData;
	}
	
	public ArrayList<HashMap<String, Object>> getItems()
	{
		return mPageItemList;
	}
	
	public void setRequiredValueList( ArrayList<String> _value )
	{
		mRequiredValueList = _value;
	}
	
	public ArrayList<String> getRequiredValueList()
	{
		return mRequiredValueList;
	}

	public void setTabIndexList( ArrayList<String> _value )
	{
		mTabIndexList = _value;
	}
	
	public ArrayList<String> getTabIndexList()
	{
		return mTabIndexList;
	}
	
	public HashMap<String, String> getPageProperty()
	{
		return mPageProperty;
	}
	
	public void setProjectInfo(ProjectInfo _projectInfo)
	{
		mProjectInfo = _projectInfo;
	}

	public ProjectInfo getProjectInfo()
	{
		return mProjectInfo;
	}

	public int getProjectIndex()
	{
		return getProjectInfo().getProjectIndex();
	}
	
	public void setProjectIndex( int _projectIndex )
	{
		mProjectIndex = _projectIndex;
	}
	
	public void convertTableItem( HashMap<String, Object> _tblItem, HashMap<String, BandInfoMapDataSimple> _defBandInfo) throws UnsupportedEncodingException, ScriptException
	{
		
		String _itemID = _tblItem.get("id").toString();
		int rowIndex = 0;
		ArrayList<HashMap<String, String>> _tableUbfx = null;
		HashMap<String, ArrayList<HashMap<String, Object>>> _data = mDataSet;
		
		String _className = _tblItem.get("className").toString();
		
		HashMap<String, Object> tableProperty = new HashMap<String, Object>();
		ArrayList<HashMap<String, Object>> _ubApprovalAr = null;
		ArrayList<Object> borderAr = null;
		
		HashMap<String, BandInfoMapDataSimple> _bandInfoData = null;
		if( _defBandInfo != null  ) _bandInfoData = _defBandInfo;
		else _bandInfoData = bandInfoData;
		
		for( Object _key : _tblItem.keySet() )
		{
			if( !_key.equals("table"))
			{
				tableProperty.put(_key.toString(), _tblItem.get(_key) );
			}
		}
		
		// visible 값이 false일때 cell생성하지 않도록 수정
		if( tableProperty.containsKey("visible") && tableProperty.get("visible").toString().equals("false")) return;
		
		tableProperty.put("tableId", _itemID );
		tableProperty.put("className", "UBTable" );
		
		//band에 table속성 저장 by IHJ
		try
		{
			String _bandNM = UBComponent.getProperties(tableProperty,  "UBTable", "band","").toString();
			if(_bandInfoData!=null) _bandInfoData.get( tableProperty.get("band") ).setTableProperty(tableProperty);
		}
		catch(NullPointerException nexp){}
		
		if( _tblItem.containsKey("ubfx")  )
		{
			_tableUbfx = (ArrayList<HashMap<String, String>>) _tblItem.get("ubfx");
		}
		
		JSONArray _tableMaps = (JSONArray) _tblItem.get("table");
		JSONArray _tableColumns;
		JSONObject _tableMap;
		JSONObject _cell;

		String _itemDataSetName;
		
		int _tableMapsLength = _tableMaps.size();
		int _tableMapDatasLength = 0;
		
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
		
		int k = 0;
		int l = 0;
		ArrayList<Integer> _lastCellIdx = new ArrayList<Integer>();
		
		
		float updateX = 0;
		float updateY = 0;
		float _tableArogHeight = 0;
		String _includeLayoutType = "";
		//Row
		for ( k = 0; k < _tableMapsLength; k++) {
			
			_tableColumns = (JSONArray) _tableMaps.get(k);//row가져 오기
			_tableMapDatasLength = _tableColumns.size();
			
			ArrayList<HashMap<String, Object>> _tableMapRow = new ArrayList<HashMap<String,Object>>();
			//Column
			for ( l = 0; l < _tableMapDatasLength; l++) {
				_ubFxChkeck = true;
				
				_tableMap = (JSONObject) _tableColumns.get(l);
				_itemDataSetName = "";
				
				if( k == 0 )
				{
					// X좌표 담기 (소숫점 1자리까지만 )
					_pos = (float) Math.round( Float.valueOf( _tableMap.get("columnWidth").toString() ) ) ;
					_xAr.add( _mapW  );
					_mapW = _mapW + _pos;
				}
			}
			
			if( k == 0 )
			{
				_xAr.add( _mapW  );		// 마지막 Width 값 담기
			}
			_tableMap = (JSONObject) _tableColumns.get(0);
			_pos = (float) Math.round( Float.valueOf( _tableMap.get("rowHeight").toString() ) ) ;
			_yAr.add( _mapH  );
			_mapH = _mapH + _pos;
			
			arTempTableRowH.add(_pos);
		}
		//TablePropery에 rowheightarray 정보 담기[속성명:tempTableRowHeight] by IHJ
		tableProperty.put("tempTableRowHeight", arTempTableRowH);						
		_yAr.add( _mapH  );		// 마지막 Height 값 담기
		
		
		Object _chekProp = "";
		_chekProp = tableProperty.get("includeLayoutType");
		if( _chekProp != null )
		{
			_includeLayoutType = _chekProp.toString();
		}
		
		//column visible 속성관련 추가 작업 ================================================================================================
		//1. 첫번째 Row에서 column visible 속성찾아 array 추가
		
		_tableColumns = (JSONArray) _tableMaps.get(0);
		HashMap<String, Object> _cellItem;
		float _removeCellW = 0;
		float _convertW = 0;
		ArrayList<Float> _tblColumnWdithAr = new ArrayList<Float>();
		
		for ( l = 0; l < _tableColumns.size(); l++) {
			
			_ubFxChkeck = true;
			
			_tableMap = (JSONObject) _tableColumns.get(l);	
			
			if(_tableMap.get("cell") != null){
				
				_cellItem = (HashMap<String, Object>) _tableMap.get("cell");
				
				if(_cellItem.containsKey("includeLayout") &&  _cellItem.get("includeLayout").toString().equals("false") ){
					_ubFxChkeck = false;
				}else if( _cellItem.containsKey("ubfx") && _cellItem.get("ubfx")!=null ){
					_ubFxChkeck = UBIDataUtilPraser.getUBFxChkeckJson(_data,_cellItem ,(HashMap<String, Object>) getProjectInfo().getParam(), "includeLayout", getProjectInfo().getFunction() );
				}
			}
			if(!_ubFxChkeck){									
				_exColIdx.add(l);					
				if(!_tableMap.get("colSpan").toString().equals("1")){
					int _colSpan = Integer.parseInt(_tableMap.get("colSpan").toString());
					for(int tmp = l+1; tmp < l+_colSpan; tmp++)
					{
						_exColIdx.add(tmp);		
					}
				}
				_removeCellW = _removeCellW + Float.valueOf( _tableMap.get("width").toString() );
			}
			else if(_exColIdx.indexOf(l) == -1)
			{
				_convertW = _convertW + Float.valueOf( _tableMap.get("columnWidth").toString() );
			}
			_tblColumnWdithAr.add(Float.valueOf( _tableMap.get("columnWidth").toString() ) );
		}

		//2. column visible array에 해당하는 컬럼들을 row별로 돌면서 merge여부에 따라 해당 인덱스 추가
		if(_exColIdx.size() > 0){
			for ( l = 0; l < _tableColumns.size()-1; l++) {
				for ( k = 1; k < _tableMaps.size(); k++) {					
					_tableMap = (JSONObject) ( (JSONArray) _tableMaps.get(k)).get(l);
					if(!_tableMap.get("colSpan").toString().equals("1") && _exColIdx.indexOf(l)!= -1 ){
						int _colSpan = Integer.parseInt(_tableMap.get("colSpan").toString());
						for(int tmp = l; tmp < l+_colSpan; tmp++){
							if(_exColIdx.indexOf(tmp) == -1){
								_exColIdx.add(tmp);				
							}
						}
					}
				}
			}
			
			if(_includeLayoutType.equals(GlobalVariableData.M_TABLE_INCLUDE_LAYOUT_TYPE_AUTO))
			{
				float _addW = 0;
				float _lastW = _removeCellW;
				int _lastAddPosition = 0;
				double _tempW = 0;
				double _addWFloat = 0;
				
				for ( l = 0; l < _tableColumns.size(); l++)
				{
					_tableMap = (JSONObject) ( (JSONArray) _tableMaps.get(0)).get(l);
					
					if( _exColIdx.indexOf(l) != -1 )
					{
						_tblColumnWdithAr.set( l,0f );
						
						if( l == _tableColumns.size() -1 )
						{
							_tblColumnWdithAr.set( _lastAddPosition, _tblColumnWdithAr.get(_lastAddPosition) + _lastW );
						}
					}
					else
					{
						if( l == _tableColumns.size() -1 )
						{
							if( _tempW > 1 )
							{
								_lastW = _lastW + 1;
								_tempW = _tempW-1;
							}
							
							_tblColumnWdithAr.set( l,  Float.valueOf( _tableMap.get("columnWidth").toString() ) + _lastW );
						}
						else
						{
							_lastAddPosition = l;
							_addWFloat = _removeCellW * ( Float.valueOf( _tableMap.get("columnWidth").toString() ) / _convertW );
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
				
				// table map column Width업데이트 / cell width 업데이트
				float _cellW = 0;
				for ( k = 0; k < _tableMaps.size(); k++) {	
					JSONArray _tableRow = (JSONArray) _tableMaps.get(k);
					JSONObject _cItem = null;
					
					for ( l = 0; l < _tableRow.size(); l++) {
						_cellW = 0;
						_tableMap = (JSONObject) _tableRow.get(l);
						_tableMap.put("columnWidth", _tblColumnWdithAr.get(l));
						
						if(!_tableMap.get("colSpan").toString().equals("1")){
							int _colSpan = Integer.parseInt(_tableMap.get("colSpan").toString());
							for(int tmp = l; tmp < l+_colSpan; tmp++){
								_cellW = _cellW +  _tblColumnWdithAr.get(tmp);
							}
						}
						else
						{
							_cellW = _tblColumnWdithAr.get(l);
						}
						
						if(_tableMap.get("cell") != null ) _cItem = (JSONObject) _tableMap.get("cell");
						
						
						
						if(_cItem != null && !_cItem.isEmpty() )
						{
							((JSONObject) _tableMap.get("cell")).put("width", _cellW);
						}
						
					}
				}
			}
			
		}
		
		for ( k = 0; k < _tableMaps.size(); k++) {	
			JSONArray _tableRow = (JSONArray) _tableMaps.get(k);
			_lastCellIdx.add(0);
			for ( l = 0; l < _tableRow.size(); l++) {
				_tableMap = (JSONObject) _tableRow.get(l);
				
				String _status = UBComponent.getProperties(_tableMap, "UBTable", "status","").toString();
				
				if( _status.equals("NORMAL")|| _status.equals("MS")||  _status.equals("MR") )
				{
					if( _exColIdx.indexOf(l) == -1)
					{
						_lastCellIdx.set(k, l);
					}
				}
			}
		}
		
		Collections.sort(_exColIdx);
		
		
		//3.새로운 xValue값 저장;
		float _tempWidth = 0;	
		float _tblColumnW =  0;
		
		_newXValue.add(0f);	
		for ( l = 0; l < _tableColumns.size()-1; l++) {
			_tableMap = (JSONObject) _tableColumns.get(l);				
			if(_exColIdx.indexOf(l) == -1){		
				_tblColumnW = Float.parseFloat( _tableMap.get("columnWidth").toString());
				_tempWidth = _tempWidth + _tblColumnW;
				_newXValue.add(_tempWidth);
			}	
		}			
		
		//4. _allTableMap에서 column visible에 해당하는 colunm을 모두 제거한다.
		for ( k = 0; k < _tableMaps.size(); k++) {//row
			for ( l = _exColIdx.size()-1; l >= 0; l--) {//column									
				((JSONArray)_tableMaps.get(k)).remove(Integer.parseInt(_exColIdx.get(l).toString()));
			}
		}	
		
		
		// 각 맵별BorderString담아두기작업 종료
		
		//Table의 UBFX를 담아두기
		ArrayList<String> _tableUbfunction = (ArrayList<String> ) _tblItem.get("ubfunction");
		
		ArrayList<HashMap<String, Object>> _cellItems = new ArrayList<HashMap<String, Object>>();
		tableProperty.put("CELLS", _cellItems );
		
		int colIndex = 0;
		HashMap<String, Object> itemProperty;
		Object _checkProp;
		
		for ( k = 0; k < _tableMaps.size(); k++) {
			
			colIndex = 0;
			_tableColumns = (JSONArray) _tableMaps.get(k);
			
			for ( l = 0; l <  _tableColumns.size(); l++) {
				
				_tableMap = (JSONObject) _tableColumns.get(l);
				
				itemProperty = new HashMap<String, Object>();
				
				_itemDataSetName = "";
				itemProperty = (HashMap<String, Object>) _tableMap.get("cell");
				
				if( itemProperty == null || itemProperty.isEmpty() || itemProperty.get("width").toString().equals("0") || itemProperty.get("height").toString().equals("0") ) continue;
				

				itemProperty.put("className", "UBLabel" );
				itemProperty.put("LOAD_TYPE", ProjectInfo.LOAD_TYPE_JSON );
				_checkProp = UBComponent.getProperties(_tableMap, "UBTable", "status", "");
				
				if( _checkProp.toString().equals("MC") == false )
				{
					colIndex = Integer.valueOf(UBComponent.getProperties(_tableMap, "UBTable", "columnIndex", "").toString()) + Integer.valueOf( UBComponent.getProperties(_tableMap, "UBTable", "colSpan", "").toString() );
				}
				for( Object _key : _tableMap.keySet() )
				{
					if( _tableMap.get(_key) instanceof String )
					{
						_tableMap.put(_key,  URLDecoder.decode(_tableMap.get(_key).toString(), "UTF-8") );
					}
				}
				
				String _valueStr = "";
				
				rowIndex =  Integer.valueOf(_tableMap.get("rowIndex").toString()) + Integer.valueOf( _tableMap.get("rowSpan").toString() );
				// 테이블의 모든 셀을 뒤져 데이터셋 정보를 가져오기
				
				//아이템의 LineHeight값을 수정 120% => 1.2로 변경
				
				_checkProp = itemProperty.get("lineHeight");
				if( _checkProp != null && _checkProp.toString().equals("") == false  )
				{
					String _value = _checkProp.toString();
					String _convertValue = _value.toString().replace("%25", "").replace("%", "");
					
					if( !_value.equals(_convertValue))
					{
						_value = _convertValue;
						_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
					}
					else
					{
						_value = _convertValue;
					}
					
					itemProperty.put("lineHeight",  Float.valueOf( _value ) );
				}
				
				//cell index정보 저장
				itemProperty.put( "rowIndex",  _tableMap.get("rowIndex") );	
				itemProperty.put( "columnIndex", _tableMap.get("columnIndex") );	
				
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
				
				JSONObject _tempCell;
				
				String _tempBorderStr = _tableMap.get("borderString").toString();				
				if(useLeftBorder){	//left border가 공유일 경우 top의 변경여부를 저장한다.					
					_patt = Pattern.compile("borderTop,borderType:[^,]+");
					_matcher =  _patt.matcher(_tempBorderStr);
					if(_matcher.find()){
						_topBorderStr = _tempBorderStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
					}			
					_topBorderBefStr = ((JSONObject) ((JSONArray) _tableMaps.get(k)).get(l-1)).get("borderString").toString();			
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
					_bottomBorderBefStr = ((JSONObject) ((JSONArray) _tableMaps.get(k)).get(l-1)).get("borderString").toString();			
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
					_leftBorderBefStr = ((JSONObject) ((JSONArray) _tableMaps.get(k-1)).get(l)).get("borderString").toString();		
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
					_rightBorderBefStr = ((JSONObject) ((JSONArray) _tableMaps.get(k-1)).get(l)).get("borderString").toString();
					_rightBorderStr = URLDecoder.decode(_rightBorderStr, "UTF-8");
					
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
				
				if( _lastCellIdx.size() > 0 )
				{
					if( _lastCellIdx.get(k).equals(Integer.valueOf(_tableMap.get("columnIndex").toString())) )
					{
						useRightBorder = true;
					}
				}else if( colIndex == Integer.parseInt( tableProperty.get("columnCount").toString() ) ) useRightBorder = true;
				
				if( rowIndex == Integer.parseInt( tableProperty.get("rowCount").toString() )  ) useBottomBorder = true;
				
				if(useRightBorder)
				{
					_rightBorderStr =   ((JSONObject) _tableColumns.get( ((JSONArray)_tableMaps.get(k)).size()-1 )).get("borderString").toString();
				}
				if(useBottomBorder)
				{
					_bottomBorderStr =  ((JSONObject) ((JSONArray) _tableMaps.get(_tableMaps.size()-1)).get(l)).get("borderString").toString();
				}
				
				//이전 Cell과의 top,left borderType 변경 여부  정보 담기				
				itemProperty.put("beforeBorderType", beforeBorderType);						
				itemProperty.put("ORIGINAL_TABLE_ID", _itemID );
				itemProperty.put("TABLE_ID", _itemID );
				
				_rightBorderStr = URLDecoder.decode(_rightBorderStr, "UTF-8");
				_bottomBorderStr = URLDecoder.decode(_bottomBorderStr, "UTF-8");
				_tableMap.put("borderString", URLDecoder.decode(_tableMap.get("borderString").toString(), "UTF-8") );
				
				if(!(isExportType.equals("PPT"))){
					borderAr = ItemConvertParser.convertCellBorder( _tableMap.get("borderString").toString(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr,useLeftBorder,useTopBorder ,isExportType );
				}
				else{
					borderAr = ItemConvertParser.convertCellBorderForPPT( _tableMap.get("borderString").toString(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr );
				}
				
				// 아이템의 Height값이 다음 Row의 Y값보다 클경우 사이즈를 수정. ( 신규 테이블일경우에만 지정 ) - 이전 테이블의 경우 rowSpan값이 부정확하여 정확한 위치가 잡히지 않을수 있음.
				//X좌표와 Width값 업데이트
				int _spanP = 0;
				int _indexP = 0;
				float _updateP = 0;
				//Y좌표와 Height값 업데이트
				if(_yAr.size() > 0 )
				{
					_spanP =  Integer.valueOf(_tableMap.get("rowSpan").toString());
					_indexP = Integer.valueOf(_tableMap.get("rowIndex").toString());
					
					float _newY = _yAr.get(_indexP);
					float _chkBandY = 0;
					float _chkOutHeight = 0;
					float _chkBandYposition = 0;
					float _bandYTemp = 0;
					
					String _band = "";
					
					
					itemProperty.put("y", _newY  );
					_updateP = (float) Math.round((_yAr.get(_indexP + _spanP ) - _yAr.get(_indexP)) );
					itemProperty.put("height",  _updateP );
					
					// band_y값이 존재할경우 table의 Y와 Height값을 이용하여 overHeight와 outHeight값을 담기.
					_checkProp = _tblItem.get("band");
					
					if( _checkProp != null && _checkProp.toString().equals("") == false )
					{
						_checkProp = _tblItem.get("band_y");
						if( _checkProp != null && _checkProp.toString().equals("") == false )
						{
							_bandYTemp= Math.round( Float.valueOf(tableProperty.get("band_y").toString()) );
							tableProperty.put("band_y", _bandYTemp );
							
							_chkBandYposition = _bandYTemp;
							
							if( _newY < ( _bandYTemp*-1 ) )
							{
								_chkBandY = _bandYTemp;
								itemProperty.put("cellOverHeight",  _chkBandY*-1 );
							}
							else
							{
								_chkBandY = 0;
								itemProperty.put("cellOverHeight",  0 );
							}
							
							_checkProp = tableProperty.get("band");
							
							
							if(_checkProp != null && _bandInfoData.containsKey( _checkProp.toString()  ) )
							{
								float _chkBandH = _bandInfoData.get( _checkProp.toString() ).getHeight();
								if( _newY + _updateP > _chkBandH )
								{
									if( _chkBandY == 0 ) _chkBandY = _chkBandYposition;
									_chkOutHeight = (_newY + _updateP) + _chkBandY - _chkBandH;
									itemProperty.put("cellOutHeight", _chkOutHeight );
								}
								else
								{
									itemProperty.put("cellOutHeight",  0 );
								}
							}
						}
						else
						{
							itemProperty.put("cellOutHeight",  0 );
						}
						
					}
					
				}
				
				itemProperty.put( "isCell", true );
				itemProperty.put("TableMap", tableProperty);
				itemProperty.put("type", "borderLabel");
				
				ArrayList<String> _borderSide = new ArrayList<String>(Arrays.asList(borderAr.get(0).toString().split(",")));
				
				itemProperty.put("borderSide", 		_borderSide );
				itemProperty.put("borderTypes", 	borderAr.get(1) );
				itemProperty.put("borderColors",  	borderAr.get(2) );
				itemProperty.put("borderWidths",  	  borderAr.get(3) );
				itemProperty.put("borderColorsInt",   borderAr.get(4) );
				
				if( _tableUbfx != null )
				{
					itemProperty.put("tableUbfunction", _tableUbfx);
				}
				
				_checkProp = tableProperty.get("band");
				String _tblBand = (_checkProp != null)?_checkProp.toString():"";
				
				// Border Original Type담아두기
				if( borderAr.size() > 5 ) itemProperty.put("borderOriginalTypes",  	borderAr.get(5) );
				
				float _tblBandX = 0;
				float _tblBandY = 0;
				float _tempPosition = 0;
				
				if( _tblBand != null && !_tblBand.equals("null") && !_tblBand.equals("") )
				{
					_checkProp = tableProperty.get("band_x");
					if( _checkProp != null )
					{
						_tempPosition = Float.parseFloat(_checkProp.toString());
						if(  Float.isNaN( _tempPosition ) == false )
						{
							_tblBandX = _tempPosition;
						}
					}
					
					_checkProp = tableProperty.get("band_y");
					if( _checkProp != null  )
					{
						_tempPosition = Float.parseFloat(_checkProp.toString());
						if( Float.isNaN(_tempPosition) == false )
						{
							_tblBandY = _tempPosition;
						}
					}
				}
				else
				{
					_tblBandX = Float.parseFloat(tableProperty.get("x").toString());
					_tblBandY = Float.parseFloat(tableProperty.get("y").toString());
				}
				
				updateX = _tblBandX +_newXValue.get(l);
				updateY = _tblBandY + Float.parseFloat( itemProperty.get("y").toString());
				
				itemProperty.put(  "band_x",  updateX );
				itemProperty.put(  "x", updateX );
				itemProperty.put(  "band_y", updateY );
				itemProperty.put(  "y", updateY );
				
				if( mXArr.indexOf( Math.round(updateX) ) == -1 )
				{
					mXArr.add(Math.round(updateX));
				}
				int _tmpW = Math.round(updateX + Float.parseFloat( itemProperty.get("width").toString()) );
				if( mXArr.indexOf( _tmpW ) == -1 )
				{
					mXArr.add( _tmpW );
				}
				
				if( !_tblBand.equals("") && bandInfoData.containsKey( _tblBand)&& bandInfoData.get(_tblBand).getUseLabelBand() )
				{
					int tbBandCnt = bandInfoData.get(_tblBand).getLabelBandColCount();
					float tbBandWidth = bandInfoData.get(_tblBand).getLabelBandDisplayWidth();
					int _addW = 0;
					
					for (int _bandRep = 1; _bandRep < tbBandCnt+1; _bandRep++) {
			                
			            _addW = Float.valueOf( (float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep) ).intValue();
			                
			            if( !mXArr.contains( Math.round(updateX) +(_addW)) )	mXArr.add( Math.round(updateX) +(_addW));
			            if( !mXArr.contains(_tmpW+(_addW)) )	mXArr.add(_tmpW+(_addW));
		                
		            }
					
				}
				
				// band에 useLabelBand 가 존재할경우 반복하여 넣을수 있도록 갯수만큼 반복처리 
				
				_checkProp = itemProperty.get("id");
				if( _checkProp == null || _checkProp.toString().equals("") ) itemProperty.put(  "id", _tblItem.get("id") + "_" + k + l );
				
				itemProperty.put(  "realClassName", "TABLE" );
				itemProperty.put(  "realTableID",  _tblItem.get("id") );			// 원본 테이블의 ID를 담아두는 객체
				itemProperty.put(  "className",  "UBLabel" );
				
				_checkProp = itemProperty.get("dataType");
				if( _checkProp != null && _checkProp.toString().equals("") == false  &&  !_checkProp.toString().equals("0") &&
						_checkProp.toString().equals("") == false && itemProperty.get("dataSet") != null )
				{
					_itemDataSetName = itemProperty.get("dataSet").toString();
				}
				
				if( _tableUbfunction != null && _tableUbfunction.size() > 0 )
				{
					itemProperty.put( "tableUbfunction", _tableUbfunction );
				}
				
				if( _className.equals("UBApproval"))
				{
					if(_ubApprovalAr == null)
					{
						_ubApprovalAr = new ArrayList<HashMap<String, Object>>();
						_ubApprovalAr.add(tableProperty);
					}
					_ubApprovalAr.add(itemProperty);
				}
				else
				{
					_checkProp = tableProperty.get("band");
					if( _itemDataSetName.equals("") == false  && _data.containsKey(_itemDataSetName) && _data.get(_itemDataSetName) != null && _checkProp != null && _bandInfoData.containsKey( _checkProp.toString() ) )
						
					{
						if( _bandInfoData.get( _checkProp.toString() ).getDataSet().equals("") || _data.get( _bandInfoData.get( _checkProp.toString() ).getDataSet() ).size() < _data.get(_itemDataSetName).size() )
						{
							_bandInfoData.get(_checkProp.toString()).setDataSet(_itemDataSetName);
							_bandInfoData.get(_checkProp.toString()).setRowCount(_data.get(_itemDataSetName).size());
						}
					}
					
					if(_checkProp != null && _checkProp.toString().equals("") == false && _bandInfoData.containsKey(_checkProp.toString()) )
					{
						_bandInfoData.get(_checkProp.toString()).getChildren().add(itemProperty);
						
						int _requiredItemIndex = mRequiredValueList.indexOf(itemProperty.get("id").toString());
						int _tabIndexItemIndex = mTabIndexList.indexOf(itemProperty.get("id").toString());
						
						if( _requiredItemIndex != -1 )
						{
							_bandInfoData.get(tableProperty.get("band").toString()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").toString() );
						}
						
						if( _tabIndexItemIndex != -1 )
						{
							_bandInfoData.get(tableProperty.get("band").toString()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").toString() );
						}
					}
					
				}
				
				if(l == 0 )
				{
					_tableArogHeight =  Float.valueOf( _tableMap.get("rowHeight").toString() );
				}
				
				itemProperty.put("band", _tblBand);
				itemProperty.put("isTable", "true");
				
				_checkProp = tableProperty.get("band");
				if( _tblBand.equals("") == false && bandInfoData.containsKey( _checkProp.toString() ) && 
						_bandInfoData.get( _checkProp.toString() ).getAutoVerticalPosition() )
				{
					// Table의 Cell들을 담아둔다.
					_cellItems.add(itemProperty);
				}
				
				
				if( !this.getReportType().equals(ProjectInfo.FORM_TYPE_CONTINUEBNAD) && !this.getReportType().equals(ProjectInfo.FORM_TYPE_MASTERBAND) )
				{
					if( itemProperty.containsKey("dataType") && !itemProperty.get("dataType").equals("0") && itemProperty.get("dataSet") != null && mDataSet.containsKey( itemProperty.get("dataSet").toString() ) )
					{
						if( mDataRowCount < mDataSet.get( itemProperty.get("dataSet").toString() ).size() )
						{
							mDataRowCount = mDataSet.get( itemProperty.get("dataSet").toString() ).size();
						}
					}
				}
				
				if( !_className.equals("UBApproval") )mPageItemList.add(itemProperty);
			}
			
		}
		
		if(_className.equals("UBApproval") && _ubApprovalAr != null)
		{
			convertTableMapToApprovalTbl(_ubApprovalAr, _bandInfoData );
		}
		
	}
	
	private void convertTableMapToApprovalTbl( ArrayList<HashMap<String, Object>> arraylist, HashMap<String, BandInfoMapDataSimple> bandInfoData )
	{
		
		HashMap<String, Object> tblProperty = arraylist.get(0); 
		
		String _cellFixed = "left";	// UBApproval 아이템의 fixed포지션 left,right 
		int cnt = arraylist.size();		// UBApproval 아이템의 갯수 
		float _firstPosition = 0;		// 아이템의 처음 시작 X포지션
		String _dataSet = "";
		int dataCnt = 1;
		float _defaultW = 0;
		Object _chkProp;
		
		_chkProp = tblProperty.get("band");
		
		String bandName = ( _chkProp != null )? _chkProp.toString():"";
		
		_chkProp = tblProperty.get("cellFix");
		if(_chkProp != null)
		{
			_cellFixed = _chkProp.toString();
		}
		
		if(arraylist.get(2).containsKey("dataSet") && arraylist.get(2).get("dataType").toString().equals("1") && arraylist.get(2).get("dataSet").equals("") == false )
		{
			_dataSet = arraylist.get(2).get("dataSet").toString();
			dataCnt = mDataSet.get(_dataSet).size();
		}
		else
		{
			dataCnt = 1;
		}
		
		if( "right".equals(_cellFixed) )
		{
			_firstPosition = Float.valueOf(tblProperty.get("x").toString()) - ( Float.valueOf( arraylist.get(2).get("width").toString()) * (dataCnt-1));
		}
		else
		{
			_firstPosition = Float.valueOf(tblProperty.get("x").toString());
		}
		
		
		if( !mXArr.contains(Float.valueOf(_firstPosition).intValue()) )	mXArr.add( Float.valueOf(_firstPosition).intValue());
		
		float _addPosition = _firstPosition;
		HashMap<String, Object> cloneItems = null;
		
		for (int i = 1; i < cnt; i++) {
			if(i == 1)
			{
				arraylist.get(i).put("x",  _firstPosition );
				if( bandInfoData != null && bandInfoData.containsKey(bandName) ) bandInfoData.get(bandName).getChildren().add( arraylist.get(i) );
				
				if( mPageItemList.indexOf( arraylist.get(i) ) == -1 ) mPageItemList.add( arraylist.get(i) );
				
				_firstPosition = _firstPosition +Float.valueOf( arraylist.get(i).get("width").toString() );
			}
			else
			{
				_addPosition = _firstPosition;
				
				for (int j = 0; j < dataCnt; j++) {
					
					cloneItems = (HashMap<String, Object>)  arraylist.get(i).clone();
					
					if( "".equals(_dataSet) == false && !cloneItems.get("column").toString().equals("") && !cloneItems.get("column").toString().equals("null") ) 
					{
						cloneItems.put("dataType", "0" );
						cloneItems.put("text", mDataSet.get(_dataSet).get(j).get( cloneItems.get("column").toString() ) );
					}
					
					// 보더 업데이트를 위하여 오른쪽보더를 제거
					if(j < dataCnt-1)
					{
						String borderSide = cloneItems.get("borderSide").toString();
						ArrayList<String> borderType = (ArrayList<String>) ((ArrayList<String>) cloneItems.get("borderTypes")).clone();
						String[] _sideAr = borderSide.split(",");
						borderSide = "";
						if( !this.isExportType.equals("PPT"))
						{
							for (int k = 0; k < _sideAr.length; k++) {
								if( "right".equals( _sideAr[k] ) )
								{
									borderType.set(k, "none");
								}
							}
						}
						
						cloneItems.put("borderTypes",  borderType );
					}
					
					cloneItems.put("x",  _addPosition );
					
					if( bandInfoData != null && bandInfoData.containsKey(bandName) ) bandInfoData.get(bandName).getChildren().add( cloneItems );
					
					mPageItemList.add(cloneItems);
					
					if( !mXArr.contains(Float.valueOf(_addPosition).intValue()) )	mXArr.add( Float.valueOf(_addPosition).intValue());
					_addPosition = _addPosition + Float.valueOf( cloneItems.get("width").toString());
					if( !mXArr.contains(Float.valueOf(_addPosition).intValue()) )	mXArr.add( Float.valueOf(_addPosition).intValue());
					
				}
			}
		}
		
		
	}
	
	
	public void setBackgroundImage( HashMap<String, String> _value )
	{
		mBackgroundImage = new JSONObject();
		
		for( String _key : _value.keySet() )
		{
			if( _key.equals("url") )
			{
				mBackgroundImage.put( "url", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+  _value.get(_key) );
				mBackgroundImage.put( "originalUrl", _value.get(_key) );
			}
			else
			{
				try {
					if( _key.equals("data")) mBackgroundImage.put( _key, URLDecoder.decode(_value.get(_key) , "UTF-8"));
					else mBackgroundImage.put( _key, _value.get(_key) );
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public JSONObject getBackgroundImage()
	{
		return mBackgroundImage;
	}
	
	
	public void convertItem( HashMap<String, Object> _item, HashMap<String, BandInfoMapDataSimple> _defBandInfo ) throws UnsupportedEncodingException
	{
		HashMap<String, Object> itemProperty = _item;
		
		Object _chkProp;
		
		itemProperty.put("LOAD_TYPE", ProjectInfo.LOAD_TYPE_JSON );
		
		String _className = _item.get("className").toString();
		String _itemDataSetName = "";
		HashMap<String, ArrayList<HashMap<String, Object>>> _data = mDataSet;
		boolean _useLabelBand = false;
		int tbBandCnt = 0;
		float tbBandWidth = 0;
		String _dataType = "";
		String _dataSetName = "";
		
		HashMap<String, BandInfoMapDataSimple> _bandInfoData = null;
		if( _defBandInfo != null) _bandInfoData = _defBandInfo;
		else _bandInfoData = bandInfoData;
		
		//아이템의 LineHeight값을 수정 120% => 1.2로 변경
		_chkProp = UBComponent.getProperties(itemProperty, _className, "lineHeight","");
		if( _chkProp != null && _chkProp.toString().equals("") == false  )
		{
			String _value = _chkProp.toString();
			String _convertValue = _value.toString().replace("%25", "").replace("%", "");
			if( !_value.equals(_convertValue))
			{
				_value = _convertValue;
				_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
			}
			else
			{
				_value = _convertValue;
			}
			
			itemProperty.put("lineHeight",  Float.valueOf( _value ) );
		}
		
		itemProperty.put(  "id",  _item.get("id").toString());
		itemProperty.put(  "className", _className );
		
		int _requiredItemIndex = mRequiredValueList.indexOf(itemProperty.get("id").toString());
		int _tabIndexItemIndex = mTabIndexList.indexOf(itemProperty.get("id").toString());
		
		_dataType= UBComponent.getProperties(itemProperty, _className, "dataType","").toString();
		_dataSetName= UBComponent.getProperties(itemProperty, _className, "dataType","").toString();
		if( _dataType.equals("") == false  &&  !_dataType.equals("0") &&
				_dataSetName.equals("") == false  )
		{
			_itemDataSetName = _dataSetName;
		}
		
		_chkProp = itemProperty.get("band");	// 아이템의 밴드명
		if( _bandInfoData != null && _chkProp != null )
		{
			if( _itemDataSetName.equals("") == false  && _data.containsKey(_itemDataSetName) && _data.get(_itemDataSetName) != null && _bandInfoData.containsKey(_chkProp.toString()) )
			{
				if( _bandInfoData.get( _chkProp.toString()).getDataSet().equals("") || _data.get( _bandInfoData.get( _chkProp.toString()).getDataSet() ).size() < _data.get(_itemDataSetName).size() )
				{
					_bandInfoData.get(_chkProp.toString()).setDataSet(_itemDataSetName); 
					_bandInfoData.get(_chkProp.toString()).setRowCount(_data.get(_itemDataSetName).size());
				}
			}

			if( _chkProp.toString().equals("") == false  && _chkProp.toString().equals("null") == false && _bandInfoData.containsKey(_chkProp.toString()) )
			{
				// RadioButtonGroup일경우 라디오버튼보다 먼저 생성 되어야 이후 라디오버튼에 값을 지정할수 있다.
				if( _className.equals("UBRadioButtonGroup") )
				{
					_bandInfoData.get(_chkProp.toString()).getChildren().add(0, itemProperty);
				}
				else
				{
					_bandInfoData.get(_chkProp.toString()).getChildren().add(itemProperty);
				}
			}

			if( _requiredItemIndex != -1 && _bandInfoData.containsKey(_chkProp.toString()) )
			{
				_bandInfoData.get(_chkProp.toString()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").toString() );
			}
			
			if( _tabIndexItemIndex != -1 && _bandInfoData.containsKey(_chkProp.toString()) )
			{
				_bandInfoData.get(_chkProp.toString()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").toString() );
			}
			
			//TEMPLET 처리
			if( _className.equals("UBTemplateArea") )
			{
				_chkProp = itemProperty.get("autoHeight");	// 아이템의 AutoHeight 속성
				boolean _autoHFlag = (_chkProp != null && _chkProp.toString().equals("true") )? true:false;
				//Templet일경우 Band의 Height가 UBTempletArea보다 작을 경우 Band의 사이즈 업데이트
				if( _autoHFlag && getProjectInfo().getTempletInfo() != null && getProjectInfo().getTempletInfo().containsKey(itemProperty.get("id").toString() ) )
				{
					float _templetHeight = getProjectInfo().getTempletInfo().get( itemProperty.get("id").toString() ).getHeight() + Float.valueOf(itemProperty.get("band_y").toString());
					if( _bandInfoData.get(itemProperty.get("band").toString()) != null && _templetHeight > _bandInfoData.get(itemProperty.get("band").toString()).getHeight() )
					{
						_bandInfoData.get(itemProperty.get("band").toString()).setHeight(_templetHeight);
					}
				}
			}
			
			_chkProp = itemProperty.get("band");	// 아이템의 Band명
			// 밴드의 useLabelBand값이 존재할경우 해당 x, width 값을 lableBand반복 갯수만큼 반복한다.
			if( _bandInfoData != null && _bandInfoData.containsKey( _chkProp.toString() ) ) _useLabelBand = _bandInfoData.get(_chkProp.toString()).getUseLabelBand();
			
			if(_useLabelBand)
			{
				tbBandCnt = _bandInfoData.get(_chkProp.toString()).getLabelBandColCount();
				tbBandWidth = _bandInfoData.get(_chkProp.toString()).getLabelBandDisplayWidth();
			}
			
		}
		
		if( !this.getReportType().equals(ProjectInfo.FORM_TYPE_CONTINUEBNAD) && !this.getReportType().equals(ProjectInfo.FORM_TYPE_MASTERBAND) )
		{
			if( itemProperty.containsKey("dataType") && !itemProperty.get("dataType").equals("0") && itemProperty.get("dataSet") != null && mDataSet.containsKey( itemProperty.get("dataSet").toString() ) )
			{
				if( mDataRowCount < mDataSet.get( itemProperty.get("dataSet").toString() ).size() )
				{
					mDataRowCount = mDataSet.get( itemProperty.get("dataSet").toString() ).size();
				}
			}
		}
		
		float _itemX = Float.valueOf( UBComponent.getProperties(itemProperty, _className, "x" , 0).toString() );
		float _itemWidth = Float.valueOf( UBComponent.getProperties(itemProperty, _className, "width" , 0).toString() );
		float _itemHeight = Float.valueOf( UBComponent.getProperties(itemProperty, _className, "height" , 0).toString() );
		int _rotate = 0;
		
		_chkProp = itemProperty.get("rotation");
		if(_chkProp != null && !_chkProp.equals("") )
		{
			_rotate = Integer.valueOf( _chkProp.toString());
			
			if(_rotate != 0 )
			{
				Point _edPoint = common.rotationPosition( _itemWidth,_itemHeight, _rotate);
				
				if( 0 > _edPoint.x )
				{
					_itemX = _itemX + _edPoint.x;
					_itemWidth =  Math.abs( _edPoint.x );
				}
				else
				{
					_itemWidth = Math.abs( _edPoint.x );
				}
			}
		}
		
		if( mXArr.indexOf( Math.round(_itemX) ) == -1 )
		{
			mXArr.add(Math.round(_itemX));
		}
		int _tmpW = Math.round(_itemX + _itemWidth );
		if( mXArr.indexOf( _tmpW ) == -1 )
		{
			mXArr.add( _tmpW );
		}
		
		//// UBLabel일경우 Border처리 필요 ( 속성이 존재할경우  )
		if( _className.equals("UBLabel"))
		{
			_chkProp = UBComponent.getProperties( itemProperty, _className, "borderSide");
			if( _chkProp != null )
			{
				itemProperty.put("borderSide", 		_chkProp );
				itemProperty = convertItemToBorder(itemProperty);
			}
		}
		
		Object _typeName = UBComponent.typeInfo.get(_className);
		if( _typeName != null )
		{
			itemProperty.put("type", _typeName.toString() );
		}
		
		if(_useLabelBand)
		{
			int _addW = 0;
			for (int _bandRep = 1; _bandRep < tbBandCnt+1; _bandRep++) {
	                
				_addW = Float.valueOf( (float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep) ).intValue();
				
				if( !mXArr.contains(Math.round(_itemX)+_addW ) ) mXArr.add(Math.round(_itemX)+_addW);
				if( !mXArr.contains(_tmpW +_addW ) )	mXArr.add( _tmpW+_addW);
            }
		}
		
		if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
		{
	    	itemProperty.put("barcodeType", UBComponent.getProperties(itemProperty, _className, "barcodeType" , "").toString());
	    	itemProperty.put("showLabel", UBComponent.getProperties(itemProperty, _className, "showLabel" , "").toString());
	    	itemProperty.put("widthMatch", UBComponent.getProperties(itemProperty, _className, "widthMatch" , "").toString());
		}
		else if( _className.equals("UBLabelBand"))
		{
			int _borderX = Float.valueOf( UBComponent.getProperties(itemProperty, _className, "border_x" , 0).toString() ).intValue();
			int _borderWidth = Float.valueOf( UBComponent.getProperties(itemProperty, _className, "border_width" , 0).toString() ).intValue();

			mLabelBandInfo.put("columns", Float.valueOf( _itemWidth ).intValue() );
			mLabelBandInfo.put("border_x",  _borderX );
			mLabelBandInfo.put("border_width",  _borderWidth );
		}
		
		mPageItemList.add(itemProperty);
	}
	
	public HashMap<String, String> mOriginalDataMap = null;			// originalData값으 가지고 있는 객체
	public ArrayList<ArrayList<String>> mGroupDataNamesAr = null;	// 그룹핑된 데이터명을 가지고 있는 객체

	private void convertBandItemList( ArrayList<HashMap<String, Object>> _bands )
	{
		HashMap<String, Object> _childItem;
		int i = 0;
		int j = 0;
		int k = 0;
		int cnt = _bands.size();
		String _className;
		String _itemId;
		ArrayList<BandInfoMapDataSimple> grpList = new ArrayList<BandInfoMapDataSimple>();		
		// 그룹밴드용 아이템처리
		int _grpDataIndex = 0;
		float _grpDefaultHeight = 0;
		float _grpHeight = 0;
		float headerBandHeight = 0;
		Boolean repeatPageHeader = false;
		Boolean repeatPageFooter = false;
		
		int _grpSubMaxLength = 0;
		float _pageWidth = this.mWidth;
		
		String _dataBandName = "";
		String prevHeaderBandName = "";
		String _summeryBandName = "";
		String groupName = "";
		String _grpDataName = "";
		String _grpSubDataName = "";
		String _grpSubBandName2 = "";
		String _grpSubBandName  = "";
		BandInfoMapDataSimple _subCloneBandData;
		String _subFooterBandStr = "";
		ArrayList<ArrayList<HashMap<String, Object>>> _grpSubDataListAr = new ArrayList<ArrayList<HashMap<String,Object>>>();
		ArrayList<String> bandSequenceAr = new ArrayList<String>();
		HashMap<String, String> originalDataMap = new HashMap<String, String>();
		HashMap<String, ArrayList<HashMap<String, Object>>> DataSet;
		ArrayList<String> _grpSubDataNameAr = new ArrayList<String>();
		ArrayList<ArrayList<String>> groupDataNamesAr = new ArrayList<ArrayList<String>>();

		Boolean _subFooterBandFlag = false;
		
		// groupBand 의 Group Count를 담기위한 객체
		HashMap<String, Integer> _groupBandCntMap = new HashMap<String, Integer>();
		
		ArrayList<BandInfoMapDataSimple> _pageHeaderList = new ArrayList<BandInfoMapDataSimple>();
		ArrayList<BandInfoMapDataSimple> _pageFooterList = new ArrayList<BandInfoMapDataSimple>();
		
		boolean _useDefaultBand = false;
		if( mDefaultBandList != null )
		{
			cnt = mDefaultBandList.size();
			_useDefaultBand = true;
		}
		
		for(i = 0; i < cnt ; i++)
		{

			BandInfoMapDataSimple  bandData = null;
			
			if(_useDefaultBand)
			{
				bandData 	= mDefaultBandList.get(i).cloneBandInfo();
				_className 	= mDefaultBandList.get(i).getClassName();
				_itemId 	= mDefaultBandList.get(i).getId();
				_childItem = null;
			}
			else
			{
				_childItem = _bands.get(i);
				
				_itemId = _childItem.get("id").toString();
				_className = _childItem.get("className").toString();
				
				bandData = new BandInfoMapDataSimple(_childItem);
			}
			
			if( !bandData.getVisible() && !bandData.getClassName().equals(BandInfoMapDataSimple.GROUP_FOOTER_BAND) )
			{
				continue;
			}
			
			bandInfoData.put(bandData.getId(), bandData);	// 생성된 밴드 데이터를 ID를 Key값으로 하여 맵에 담아두기
			bandData.setFileLoadType(GlobalVariableData.M_FILE_LOAD_TYPE_JSON);
			if( _className.equals("UBPageHeaderBand") )
			{
				if( bandData.getRepeat().equals("y") )
				{
					headerBandHeight = headerBandHeight + bandData.getHeight();
					repeatPageHeader = true;
				}
				
			}
			else if(  _className.equals("UBPageFooterBand")  )
			{
//				groupName = "";
//				prevHeaderBandName = "";
			}
			else if(  _className.equals("UBDataFooterBand")  )
			{
				if(groupName.equals("") == false )
				{
					bandData.setGroupBand(groupName);
				}
				
				if(bandData.getUseDataHeaderBand())
				{
					//@TEST use
					if( _dataBandName.equals("") == false && bandInfoData.containsKey(_dataBandName) && bandInfoData.get(_dataBandName).getHeaderBand().equals("") == false ) 
					{
						bandData.setHeaderBand(bandInfoData.get(_dataBandName).getHeaderBand());
					}
				}
				
			}	
			else if(  _className.equals("UBDataPageFooterBand")  )
			{
				if(groupName.equals("") == false )
				{
					bandData.setGroupBand(groupName);
				}
				
				bandSequenceAr.add("s");
				
				_summeryBandName = bandData.getId();
				
				if( _dataBandName.equals("") == false  ) bandInfoData.get(_dataBandName).setSummery(_summeryBandName);
			}
			else if(  _className.equals("UBDataBand")  )
			{
				if(groupName.equals("") == false )
				{
					bandData.setGroupBand(groupName);
				}
				
				_dataBandName = bandData.getId();
				bandSequenceAr.add("d");
				if(bandData.getSequence() == null )
				{
					bandData.setSequence(new ArrayList<String>());
				}
				bandData.getSequence().add("d");
				
				if( prevHeaderBandName.equals("") == false  )
				{
					bandInfoData.get(prevHeaderBandName).setDataBand( bandData.getId() );		// 헤더밴드에 DataBand매칭
					bandData.setHeaderBand(prevHeaderBandName);
					prevHeaderBandName = "";
				}
			}
			else if(  _className.equals("UBDataHeaderBand")  )
			{
				prevHeaderBandName = bandData.getId();
				
				if(groupName.equals("") == false )
				{
					bandData.setGroupBand(groupName);
				}
				bandSequenceAr = new ArrayList<String>();
			}
			else if(  _className.equals("UBEmptyBand")  )
			{
				if(groupName.equals("") == false )
				{
					bandData.setGroupBand(groupName);
				}
				bandSequenceAr = new ArrayList<String>();
				
			}
			else if(  _className.equals("UBGroupHeaderBand")  )
			{
				groupName = bandData.getId();
				prevHeaderBandName = "";
				
				bandSequenceAr = new ArrayList<String>();
				
				// sort옵션여부 체크
				ArrayList<Boolean> orderBy = new ArrayList<Boolean>();
				ArrayList<String> grpColumnAr = new ArrayList<String>();
				if( bandData.getColumnAr() != null && bandData.getColumnAr().size() > 0 )
				{
					grpColumnAr = bandData.getColumnAr();
				}
				
				// 각각의 컬럼별 orderBy값을 가지고 있는 columns속성이 존재할경우
				if( bandData.getColumnsAC() != null && bandData.getColumnsAC().size() > 0 )
				{
					int grpColumnArSize = grpColumnAr.size();
					for ( j = 0; j < grpColumnArSize; j++) {
						
						for (HashMap<String, String> _column : bandData.getColumnsAC()) {
							
							if(_column.containsKey("column") && _column.get("column").equals(grpColumnAr.get(j)))
							{
								if( _column.get("orderBy").toLowerCase().equals("true"))
								{
									orderBy.add(true);
								}
								else
								{
									orderBy.add(false);
								}
							}
							
						}
						
					}
				}
				else
				{
					orderBy.add( bandData.getOrderFlag() );
				}
				
				bandData.setOrderBy(orderBy);
				
				ArrayList<String> filterColAr = new ArrayList<String>();
				ArrayList<String> filterOperatorAr = new ArrayList<String>();
				ArrayList<String> filterTextAr = new ArrayList<String>();
				if( bandData.getFilter() != null )
				{
					ArrayList<HashMap<String,String>> bandDataFilter = bandData.getFilter();
					int bandDataFilterSize = bandDataFilter.size();
					for ( j = 0; j < bandDataFilterSize; j++) {
						
						if( bandDataFilter.get(j).containsKey("column")) filterColAr.add( bandDataFilter.get(j).get("column") );
						if( bandDataFilter.get(j).containsKey("operation")) filterOperatorAr.add( bandDataFilter.get(j).get("operation") );
						if( bandDataFilter.get(j).containsKey("text")) filterTextAr.add( bandDataFilter.get(j).get("text") );
						
					}
				}
				
				ArrayList<Object> retData = GroupingDataSetProcess.changeGroupDataSet( mDataSet, groupName, bandData.getDataSet(), bandData.getColumnAr().get(0), orderBy.get(0), bandData.getSort(), filterColAr, filterOperatorAr, filterTextAr, bandData.getOriginalOrder(), originalDataMap );
				getProjectInfo().setDataSet(  ( HashMap<String, ArrayList<HashMap<String, Object>>> ) retData.get(0) );
				
				DataSet = ( HashMap<String, ArrayList<HashMap<String, Object>>> ) retData.get(0);
				originalDataMap = ( HashMap<String, String> )  retData.get(1);
				int grpDataCnt = (Integer)  retData.get(2);
				//DataSet
				
				if( DataSet.size() > 0 )
				{
					bandData.setRowCount(grpDataCnt);
				}
				
				// originalOrder 속성 지정
				ArrayList<String> grpFooterAr = new ArrayList<String>();
				int bandDataColumnArSize = bandData.getColumnAr().size();
				for ( j = 0; j < bandDataColumnArSize; j++) {
					grpFooterAr.add("");
				}
				
				
				bandData.setFooterAr(grpFooterAr);
				bandData.setGroupBand(groupName);
				
				
			}
			else if(  _className.equals("UBGroupFooterBand")  )
			{
				
				if( groupName.equals("") == false  )
				{
					if(bandData.getGroupHeader().equals("") == false  && bandData.getColumnsAR()!=null && bandData.getColumnsAR().size() > 0 && bandData.getColumnsAR().get(0).equals("") == false && bandInfoData.get(groupName).getColumnAr().indexOf(  bandData.getColumnsAR().get(0) ) != -1 )
					{
						bandInfoData.get(groupName).getFooterAr().set(bandInfoData.get(groupName).getColumnAr().indexOf(  bandData.getColumnsAR().get(0) ), bandData.getId());
					}
					else
					{
						bandInfoData.get(groupName).getFooterAr().set(0, bandData.getId());
					}
					bandData.setGroupHeader(groupName);
					
					bandData.setGroupBand(groupName);
				}
				else
				{
					bandData.setGroupHeader("");
				}
				bandSequenceAr = new ArrayList<String>();
				
			}
			else if(  _className.equals("UBCrossTabBand")  )
			{
				if(_childItem != null) bandData.setOriginalItemData(_childItem);
				bandSequenceAr = new ArrayList<String>();
			}
			
			
			// DataBand에 ubfx정보가 잇을시 처리
			if( bandData.getUbfx().size() > 0 )
			{
				
			}
			
			if( (_className.equals("UBDataPageFooterBand") && _dataBandName.equals("") == false ) || ( _className.equals("UBDataBand") && _summeryBandName.equals("") == false ))
			{
				
				if( bandSequenceAr.size() > 0 && bandSequenceAr.indexOf("d") != -1 )
				{
					if( _summeryBandName.equals("") == false ){
						bandInfoData.get( _dataBandName ).setSummery(_summeryBandName);
						bandInfoData.get( _summeryBandName ).setDataBand(_dataBandName);
					}
					
					bandInfoData.get( _dataBandName ).setSequence( bandSequenceAr );
					
					bandSequenceAr = new ArrayList<String>();
					_summeryBandName = "";
					_dataBandName = "";
					
				}
				
			}
			
			
			/// 그룹에 속한 밴드일경우 그룹지어진 데이터의 총 수만큼 밴드를 복제하여 그룹을 생성
			if(groupName.equals("") == false  )
			{
				String nextChildClass = "";
//					if( i+1 < _child.getLength() )
				if( i+1 < _bands.size() )
				{
					HashMap<String, Object> nextItem = _bands.get(i+1);
					nextChildClass = nextItem.get("className").toString();
				}
				
				boolean _isGroupEndFlag = false;
					
				if( bandData.getClassName().equals("UBGroupFooterBand") && nextChildClass.equals("") == false  && nextChildClass.equals("UBGroupFooterBand") == false )
				{
					_isGroupEndFlag = true;
				}
				else if( nextChildClass.equals(BandInfoMapData.PAGE_FOOTER_BAND) || nextChildClass.equals(BandInfoMapData.PAGE_HEADER_BAND) )
				{
					// 그룹밴드명은 존재하지만 그룹푸터가 아닐경우 그룹밴드에 담기
					grpList.add(bandData);
					_grpDefaultHeight = 0f;
					
					_isGroupEndFlag = true;
				}
				
				if( _isGroupEndFlag  )
				{
					_grpDataIndex 		= 0;
					_grpHeight 			= 0;
					_grpSubMaxLength 	= 0;
					_grpDataName 		= "";
					
					boolean _isNewPage = bandData.getNewPage();
					
					_grpSubDataNameAr = new ArrayList<String>();
					BandInfoMapDataSimple _bandInfoMapData = bandInfoData.get(groupName);
					int _bandInfoMapDataRowCount = _bandInfoMapData.getRowCount();
					
					if(_bandInfoMapDataRowCount == 0 )
					{
						int _grpSize = grpList.size();
						
						for (int m = 0; m < _grpSize; m++) {
							if( grpList.get(m).getClassName().equals("UBDataBand") && grpList.get(m).getMinRowCount() > 0 )
							{
								_bandInfoMapDataRowCount = 1;
								break;
							}
						}
					}

					
					// 그룹밴드의 그룹핑된 데이터의 건수만큼 돌아서 처리
					for ( _grpDataIndex = 0; _grpDataIndex < _bandInfoMapDataRowCount; _grpDataIndex++) {
						
						_grpHeight = 0f;
						
						_grpDataName = groupName +  "grp_" + _grpDataIndex + "_" + _bandInfoMapData.getDataSet();
						
						if( _bandInfoMapData.getColumnAr().size() > 1 )
						{
							_grpSubDataListAr = GroupingDataSetProcess.changeGroupDataSetSub( mDataSet , groupName, _grpDataName, _bandInfoMapData.getColumnAr(), bandInfoData.get(groupName).getOrderBy(), _bandInfoMapData.getSort(), _bandInfoMapData.getOriginalOrder(), originalDataMap);
							_bandInfoMapData.setSubPage( String.valueOf( _grpSubDataListAr.size() ) );
							_grpSubMaxLength = _grpSubDataListAr.size();
						}
						else
						{
							_grpSubMaxLength = 1;
						}
						
						//GroupHeaderBand에 총 밴드의 그룹핑 된 수를 담아두기
						_bandInfoMapData.setGroupLength( _bandInfoMapData.getGroupLength() + _grpSubMaxLength );
						
						// newPage속성을 지정하기 위해 담아두기
//							BandInfoMapData _argoDataBand = null;
						ArrayList<BandInfoMapDataSimple> _argoDataBands = null;
						String _lastGrpName = "";
						// HeaderBand의 이름 담아두기
						String _headerBandName = "";
						
						for ( j = 0; j < _grpSubMaxLength; j++) {

							_subFooterBandFlag = true;
							
							_grpSubDataName = groupName + "grp_" + _grpDataIndex + "_" + j + "_" + _bandInfoMapData.getDataSet();
							if( _grpSubDataListAr.size() == 0 || _grpSubDataListAr.get(j).get(0).containsKey("groupFooterIndex")  == false )
							{
								
								int grpListSize = grpList.size();
								// 그룹핑된 리스트를 가져와서 그룹갯수만큼 반복하여 밴드리스트에 Add처리
								for ( k = 0; k < grpListSize; k++) {
									
									if ( !(grpList.get(k).getClassName().equals("UBGroupHeaderBand") && j > 0 ) && grpList.get(k).getClassName().equals("UBGroupFooterBand") == false ) {
										
										
										if (_bandInfoMapData.getColumnAr().size() > 1 && grpList.get(k).getClassName().equals("UBGroupHeaderBand") == false && grpList.get(k).getClassName().equals("UBGroupFooterBand") == false ) {
											
											mDataSet.put(_grpSubDataName, _grpSubDataListAr.get(j));
											originalDataMap.put( _grpSubDataName, _bandInfoMapData.getDataSet() );
											_grpSubBandName2 = "grp_" + _grpDataIndex + "_" + j + "_";
											_grpSubBandName  = "grp_" + _grpDataIndex + "_" + j + "_" + grpList.get(k).getId();
											_grpDataName = _grpSubDataName;
										}
										else
										{
											
											_subFooterBandFlag = true;
											_grpSubBandName2 = "grp_" + _grpDataIndex + "_";
											_grpSubBandName  = "grp_" + _grpDataIndex + "_" + grpList.get(k).getId();
											_grpDataName = groupName + "grp_" + _grpDataIndex + "_" +  _bandInfoMapData.getDataSet();
											
										}
										
										originalDataMap.put( _grpDataName, bandInfoData.get(groupName).getDataSet() );
										
										if( _grpSubDataNameAr.indexOf(_grpDataName) == -1 ) _grpSubDataNameAr.add(_grpDataName);
										
										_grpHeight = _grpHeight + grpList.get(k).getHeight();
										
										
										_subCloneBandData = cloneBandInfoData(grpList.get(k), groupName, grpList.get(k).getId(), _grpDataName, _grpDataIndex );
										
										
										// 그룹헤더 밴드에서 헤더를 한번만 사용할경우 아래 IF문을 처리
										if( _subCloneBandData.getClassName().equals(BandInfoMapData.DATA_HEADER_BAND) && _bandInfoMapData.getHeaderVisibleDepth() > -1 &&  _bandInfoMapData.getColumnAr().size() > 1 )
										{
											String _tempGroupName = groupName;
											int _tempIndex = _bandInfoMapData.getHeaderVisibleDepth() + 1;
											for (int m = 0; m < _tempIndex; m++) {
												if( _bandInfoMapData.getColumnAr().size() > m )
												{
													_tempGroupName = _tempGroupName +"_$_";
													_tempGroupName = _tempGroupName + _grpSubDataListAr.get(j).get(0).get(_bandInfoMapData.getColumnAr().get(m));
												}
											}
											_subCloneBandData.setUseHeaderBandGroupName( _tempGroupName + "_$_" + grpList.get(k).getId() );
											
										}
										else if( _subCloneBandData.getClassName().equals(BandInfoMapData.DATA_HEADER_BAND) && _bandInfoMapData.getHeaderVisibleDepth() == -2 )
										{
											_subCloneBandData.setUseHeaderBandGroupName( grpList.get(k).getId() );
										}
										
										if( _subCloneBandData.getClassName().equals(BandInfoMapData.DATA_HEADER_BAND) )
										{
											_headerBandName = _grpSubBandName;
										}
										
										_subCloneBandData.setId(_grpSubBandName);
										
										if( grpList.get(k).getHeaderBand().equals("") == false )
										{
											_subCloneBandData.setHeaderBand( _grpSubBandName2 + grpList.get(k).getHeaderBand() );
										}
										
										if( grpList.get(k).getDataBand().equals("") == false )
										{
											_subCloneBandData.setDataBand( _grpSubBandName2 + grpList.get(k).getDataBand() );
										}
										
										if ( grpList.get(k).getSummery().equals("") == false ) 
										{
											_subCloneBandData.setSummery( _grpSubBandName2 + grpList.get(k).getSummery() );
										}
										
										if ( grpList.get(k).getFooter().equals("") == false ) 
										{
											_subCloneBandData.setFooter( _grpSubBandName2 + grpList.get(k).getFooter() );
										}
										
										if( _subCloneBandData.getClassName().equals(BandInfoMapData.DATA_BAND) )
										{
											if(_argoDataBands == null|| _lastGrpName.equals("") || _lastGrpName.equals( _grpDataName ) == false )
											{
												_argoDataBands = new ArrayList<BandInfoMapDataSimple>();
												_lastGrpName = _grpDataName;
											}
											_argoDataBands.add(_subCloneBandData);
//												_argoDataBand = _subCloneBandData;
											if( bandInfoData.get(groupName).getNewPage() )
											{
												_subCloneBandData.setNewPage(bandInfoData.get(groupName).getNewPage());
											}
										}
										
										bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
										if( _subCloneBandData.getVisible() )bandList.add(_subCloneBandData);
										
									}
									
								}
								
							}
							else if( _grpSubDataListAr.get(j).get(0).containsKey("groupFooterIndex") && 
									bandInfoData.get(groupName).getFooterAr().get( Integer.valueOf(  _grpSubDataListAr.get(j).get(0).get("groupFooterIndex").toString() ) ) != null &&
									bandInfoData.get(groupName).getFooterAr().get( Integer.valueOf(  _grpSubDataListAr.get(j).get(0).get("groupFooterIndex").toString() ) ).equals("") == false  )
							{
								
								_subFooterBandStr = bandInfoData.get(groupName).getFooterAr().get( Integer.valueOf(  _grpSubDataListAr.get(j).get(0).get("groupFooterIndex").toString() )  );
								_subCloneBandData = cloneBandInfoData( bandInfoData.get(_subFooterBandStr), groupName, _subFooterBandStr, String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ), _grpDataIndex );
								_subCloneBandData.setId( String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ) + groupName );
								
								if( _subCloneBandData.getUseDataHeaderBand() && _headerBandName.equals("") == false ) _subCloneBandData.setHeaderBand(_headerBandName);
								
								originalDataMap.put( String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ), bandInfoData.get(groupName).getDataSet() );
								
								bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
								if( _subCloneBandData.getVisible() )bandList.add(_subCloneBandData);
								
								if( _subCloneBandData.getNewPage() && _argoDataBands != null )
								{
									for (BandInfoMapDataSimple _argoDataBand : _argoDataBands) {
										_argoDataBand.setNewPage(_subCloneBandData.getNewPage());
									}
									_lastGrpName = "";
								}
							}
							
							
							if( _subFooterBandFlag && j == _grpSubMaxLength-1 && bandInfoData.get(groupName).getFooterAr().size() > 0 &&
									bandInfoData.get(groupName).getFooterAr().get(0).equals("") == false )
							{
								
								_grpSubBandName2 = "grp_" + _grpDataIndex + "_";
								_grpSubBandName = "grp_" + _grpDataIndex + "_" + bandInfoData.get(groupName).getFooterAr().get(0);
								_grpDataName = groupName + "grp_" + _grpDataIndex + "_" + bandInfoData.get(groupName).getDataSet();
								
								_subCloneBandData = cloneBandInfoData(bandInfoData.get( bandInfoData.get(groupName).getFooterAr().get(0) ), groupName, bandInfoData.get(groupName).getFooterAr().get(0), _grpDataName, _grpDataIndex);
								_subCloneBandData.setId(_grpDataName);
								
								if( _subCloneBandData.getUseDataHeaderBand() && _headerBandName.equals("") == false ) _subCloneBandData.setHeaderBand(_headerBandName);
								
//									originalDataMap.put( String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ), bandInfoData.get(groupName).getDataSet() );
								originalDataMap.put( _grpDataName, bandInfoData.get(groupName).getDataSet() );
								
								bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
								if( _subCloneBandData.getVisible() )bandList.add(_subCloneBandData);
								
								if( _subCloneBandData.getNewPage() && _argoDataBands != null )
								{
									for (BandInfoMapDataSimple _argoDataBand : _argoDataBands) {
										_argoDataBand.setNewPage(_subCloneBandData.getNewPage());
									}
									_lastGrpName = "";
								}
							}
							
							
						}
						
						
					}
					//그룹의 한건의 기본 Height를 체크하여 그룹밴드에 입력
					
					// 여기까지 처리
					_grpDefaultHeight = _grpHeight;
					if( _grpSubMaxLength < 2 ) bandInfoData.get(groupName).setDefaultHeight(_grpDefaultHeight);
					else bandInfoData.get(groupName).setDefaultHeight(bandInfoData.get(groupName).getHeight());
					
					_grpDefaultHeight = 0;
					
					groupName = "";
					
					groupDataNamesAr.add(_grpSubDataNameAr);
					grpList = new ArrayList<BandInfoMapDataSimple>();
					
					// GroupBand의 총 그룹핑 갯수를 담기
					_groupBandCntMap.put(_bandInfoMapData.getId(), _bandInfoMapData.getGroupLength());
				}
				else
				{
					// 그룹밴드명은 존재하지만 그룹푸터가 아닐경우 그룹밴드에 담기
					grpList.add(bandData);
					_grpDefaultHeight = 0f;
					
				}
				
			}
			else
			{
				// 그룹밴드가 아닐경우
				if( bandData.getVisible() )
				{
					if( bandData.getClassName().equals( BandInfoMapDataSimple.PAGE_HEADER_BAND ) )
					{
						_pageHeaderList.add(bandData);
					}
					else if( bandData.getClassName().equals(BandInfoMapDataSimple.PAGE_FOOTER_BAND ) )
					{
						_pageFooterList.add(bandData);
					}
					else
					{
						bandList.add( bandData );
					}
				}
				
			}
			
		}
		
		
		if( groupName.equals("") == false && grpList.size() > 0 )
		{
			_grpDefaultHeight = 0;
			_grpSubDataNameAr = new ArrayList<String>();
			BandInfoMapDataSimple _bandInfoMapData = bandInfoData.get(groupName);
			int _bandInfoMapDataRowCount = _bandInfoMapData.getRowCount();
			
			if(_bandInfoMapDataRowCount == 0 )
			{
				int _grpSize = grpList.size();
				
				for (int m = 0; m < _grpSize; m++) {
					if( grpList.get(m).getClassName().equals("UBDataBand") && grpList.get(m).getMinRowCount() > 0 )
					{
						_bandInfoMapDataRowCount = 1;
						break;
					}
				}
			}
			
			// 그룹밴드의 그룹핑된 데이터의 건수만큼 돌아서 처리
			for ( _grpDataIndex = 0; _grpDataIndex < _bandInfoMapDataRowCount; _grpDataIndex++) {
				
				_grpHeight = 0f;
				
				_grpDataName = groupName +  "grp_" + _grpDataIndex + "_" + _bandInfoMapData.getDataSet();
				
				if( _bandInfoMapData.getColumnAr().size() > 1 )
				{
					_grpSubDataListAr = GroupingDataSetProcess.changeGroupDataSetSub( mDataSet, groupName, _grpDataName, _bandInfoMapData.getColumnAr(), _bandInfoMapData.getOrderBy(), _bandInfoMapData.getSort(), _bandInfoMapData.getOriginalOrder(), originalDataMap);
					_bandInfoMapData.setSubPage( String.valueOf( _grpSubDataListAr.size() ) );
					_grpSubMaxLength = _grpSubDataListAr.size();
				}
				else
				{
					_grpSubMaxLength = 1;
				}
				
				
				// newPage속성을 지정하기 위해 담아두기
//				BandInfoMapData _argoDataBand = null;
				ArrayList<BandInfoMapDataSimple> _argoDataBands = null;
				String _lastGrpName = "";
				// HeaderBand의 이름 담아두기
				String _headerBandName = "";
				
				//GroupHeaderBand에 총 밴드의 그룹핑 된 수를 담아두기
				_bandInfoMapData.setGroupLength( _bandInfoMapData.getGroupLength() + _grpSubMaxLength );
				
				for ( j = 0; j < _grpSubMaxLength; j++) {
					
					_subFooterBandFlag = true;
					_grpSubDataName = groupName + "grp_" + _grpDataIndex + "_" + j + "_" + _bandInfoMapData.getDataSet();
					if( _grpSubDataListAr.size() == 0 || _grpSubDataListAr.get(j).get(0).containsKey("groupFooterIndex")  == false )
					{
						
						int grpListSize = grpList.size();
						// 그룹핑된 리스트를 가져와서 그룹갯수만큼 반복하여 밴드리스트에 Add처리
						for ( k = 0; k < grpListSize; k++) {
							
							if ( !(grpList.get(k).getClassName().equals("UBGroupHeaderBand") && j > 0 ) && grpList.get(k).getClassName().equals("UBGroupFooterBand") == false ) {
								
								
								if (_bandInfoMapData.getColumnAr().size() > 1 && grpList.get(k).getClassName().equals("UBGroupHeaderBand") == false && grpList.get(k).getClassName().equals("UBGroupFooterBand") == false ) {
									
									mDataSet.put(_grpSubDataName, _grpSubDataListAr.get(j));
									originalDataMap.put( _grpSubDataName, _bandInfoMapData.getDataSet() );
									_grpSubBandName2 = "grp_" + _grpDataIndex + "_" + j + "_";
									_grpSubBandName  = "grp_" + _grpDataIndex + "_" + j + "_" + grpList.get(k).getId();
									_grpDataName = _grpSubDataName;
									
								}
								else
								{
									
									_subFooterBandFlag = true;
									_grpSubBandName2 = "grp_" + _grpDataIndex + "_";
									_grpSubBandName  = "grp_" + _grpDataIndex + "_" + grpList.get(k).getId();
									_grpDataName = groupName + "grp_" + _grpDataIndex + "_" +  _bandInfoMapData.getDataSet();
									
								}
								
								originalDataMap.put( _grpDataName, bandInfoData.get(groupName).getDataSet() );
								
								if( _grpSubDataNameAr.indexOf(_grpDataName) == -1 ) _grpSubDataNameAr.add(_grpDataName);
								
								_grpHeight = _grpHeight + grpList.get(k).getHeight();
								
								
								_subCloneBandData = cloneBandInfoData(grpList.get(k), groupName, grpList.get(k).getId(), _grpDataName, _grpDataIndex );
								
								_subCloneBandData.setId(_grpSubBandName);
								
								if( _subCloneBandData.getClassName().equals(BandInfoMapData.DATA_HEADER_BAND) )
								{
									_headerBandName = _grpSubBandName;
								}
								
								if( grpList.get(k).getHeaderBand().equals("") == false )
								{
									_subCloneBandData.setHeaderBand( _grpSubBandName2 + grpList.get(k).getHeaderBand() );
								}
								
								if( grpList.get(k).getDataBand().equals("") == false )
								{
									_subCloneBandData.setDataBand( _grpSubBandName2 + grpList.get(k).getDataBand() );
								}
								
								if ( grpList.get(k).getSummery().equals("") == false ) 
								{
									_subCloneBandData.setSummery( _grpSubBandName2 + grpList.get(k).getSummery() );
								}
								
								if ( grpList.get(k).getFooter().equals("") == false ) 
								{
									_subCloneBandData.setFooter( _grpSubBandName2 + grpList.get(k).getFooter() );
								}
								
								if( _subCloneBandData.getClassName().equals(BandInfoMapData.DATA_BAND) )
								{
									if(_argoDataBands == null|| _lastGrpName.equals("") || _lastGrpName.equals( _grpDataName ) == false )
									{
										_argoDataBands = new ArrayList<BandInfoMapDataSimple>();
										_lastGrpName = _grpDataName;
									}
									_argoDataBands.add(_subCloneBandData);
//									_argoDataBand = _subCloneBandData;
									if( bandInfoData.get(groupName).getNewPage() )
									{
										_subCloneBandData.setNewPage(bandInfoData.get(groupName).getNewPage());
									}
								}
								
								bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
								bandList.add(_subCloneBandData);
							}
							
						}
						
						
					}
					else if( _grpSubDataListAr.get(j).get(0).containsKey("groupFooterIndex") && 
							bandInfoData.get(groupName).getFooterAr().get( Integer.valueOf(  _grpSubDataListAr.get(j).get(0).get("groupFooterIndex").toString() ) ) != null &&
							bandInfoData.get(groupName).getFooterAr().get( Integer.valueOf(  _grpSubDataListAr.get(j).get(0).get("groupFooterIndex").toString() ) ).equals("") == false  )
					{
						
						_subFooterBandStr = bandInfoData.get(groupName).getFooterAr().get( Integer.valueOf(  _grpSubDataListAr.get(j).get(0).get("groupFooterIndex").toString() )  );
						_subCloneBandData = cloneBandInfoData( bandInfoData.get(_subFooterBandStr), groupName, _subFooterBandStr, String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ), _grpDataIndex );
						_subCloneBandData.setId( String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ) + groupName );
						
						if( _subCloneBandData.getUseDataHeaderBand() && _headerBandName.equals("") == false ) _subCloneBandData.setHeaderBand(_headerBandName);
						
						originalDataMap.put( String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ), bandInfoData.get(groupName).getDataSet() );
						
						bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
						bandList.add(_subCloneBandData);
						
						if( _subCloneBandData.getNewPage() && _argoDataBands != null )
						{
							for (BandInfoMapDataSimple _argoDataBand : _argoDataBands) {
								_argoDataBand.setNewPage(_subCloneBandData.getNewPage());
							}
							_lastGrpName = "";
						}
					}
					
					
					if( _subFooterBandFlag && j == _grpSubMaxLength-1 && bandInfoData.get(groupName).getFooterAr().size() > 0 &&
							bandInfoData.get(groupName).getFooterAr().get(0).equals("") == false )
					{
						
						_grpSubBandName2 = "grp_" + _grpDataIndex + "_";
						_grpSubBandName = "grp_" + _grpDataIndex + "_" + bandInfoData.get(groupName).getFooterAr().get(0);
						_grpDataName = groupName + "grp_" + _grpDataIndex + "_" + bandInfoData.get(groupName).getDataSet();
						
						_subCloneBandData = cloneBandInfoData(bandInfoData.get( bandInfoData.get(groupName).getFooterAr().get(0) ), groupName, bandInfoData.get(groupName).getFooterAr().get(0), _grpDataName, _grpDataIndex);
						_subCloneBandData.setId(_grpSubBandName);

						if( _subCloneBandData.getUseDataHeaderBand() && _headerBandName.equals("") == false ) _subCloneBandData.setHeaderBand(_headerBandName);
						
						originalDataMap.put( _grpDataName, bandInfoData.get(groupName).getDataSet() );
						
						bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
						bandList.add(_subCloneBandData);
						
						if( _subCloneBandData.getNewPage() && _argoDataBands != null )
						{
							for (BandInfoMapDataSimple _argoDataBand : _argoDataBands) {
								_argoDataBand.setNewPage(_subCloneBandData.getNewPage());
							}
							_lastGrpName = "";
						}
					}
					
				}
				
				
			}
			//그룹의 한건의 기본 Height를 체크하여 그룹밴드에 입력
			_grpDefaultHeight = _grpHeight;
			if( _grpSubMaxLength < 2 ) bandInfoData.get(groupName).setDefaultHeight(_grpDefaultHeight);
			else bandInfoData.get(groupName).setDefaultHeight(bandInfoData.get(groupName).getHeight());
			
			_grpDefaultHeight = 0;
			
			groupDataNamesAr.add(_grpSubDataNameAr);
			
			groupName = "";
			
			
			// GroupBand의 총 그룹핑 갯수를 담기
			_groupBandCntMap.put(_bandInfoMapData.getId(), _bandInfoMapData.getGroupLength());
		}
		
		
		// 페이지 헤더밴드와 푸터 밴드는 bandList 생성 완료후에 Add한다  
		if(_pageHeaderList.size() > 0 || _pageFooterList.size() > 0 )
		{  
			int _size = _pageHeaderList.size();
			int _s = 0;
			for(int s=0; s < _size; s++ )
			{
				bandList.add(s, _pageHeaderList.get(s));
			}
			
			
			_size = _pageFooterList.size();
			for(int s=0; s < _size; s++ )
			{
				bandList.add(_pageFooterList.get(s));
			}
			
			_pageHeaderList = null;
			_pageFooterList = null;
		}
		
		
		mOriginalDataMap = originalDataMap;
		mGroupDataNamesAr = groupDataNamesAr;
	}
	
	public int getDataRowCount()
	{
		return mDataRowCount;
	}
	
	/**
	 * int로 된 color를 Hex 로 변경 한다. 
	 * @return String _hex
	 * 
	 */
	public String changeColorToHex(int _color)
	{
		String _hex = "";

		Color _c = new Color(_color);

		String _red = "";
		
		if(Integer.toHexString(_c.getRed()).length() != 2 )
		{
			_red = "0" + Integer.toHexString(_c.getRed()); 
		}
		else
		{
			_red = Integer.toHexString(_c.getRed());
		}
		
		String _blue = "";
		
		if(Integer.toHexString(_c.getBlue()).length() != 2 )
		{
			_blue = "0" + Integer.toHexString(_c.getBlue()); 
		}
		else
		{
			_blue = Integer.toHexString(_c.getBlue());
		}
		
		String _green = "";
		
		if(Integer.toHexString(_c.getGreen()).length() != 2 )
		{
			_green = "0" + Integer.toHexString(_c.getGreen()); 
		}
		else
		{
			_green = Integer.toHexString(_c.getGreen());
		}
		
		
		
		_hex = "#" + _red + _green + _blue;

		return _hex;
	}
	
	public HashMap<String, Integer> getGgroupBandCntMap()
	{
		return mGgroupBandCntMap;
	}
	
	public HashMap<String, String> getOriginalDataMap()
	{
		return mOriginalDataMap;
	}
	public ArrayList<BandInfoMapDataSimple> getBandList()
	{
		return bandList;
	}

	public HashMap<String, BandInfoMapDataSimple> getBandInfoData()
	{
		return bandInfoData;
	}
	
	public ArrayList<ArrayList<String>> getGroupDataNamesAr()
	{
		return mGroupDataNamesAr;
	}
	
	public void setPageParserClass( Object _value )
	{
		mPageParserClass = _value;
	}
	public Object getPageParserClass()
	{
		return mPageParserClass;
	}
	
	public void setDataSet( HashMap<String, ArrayList<HashMap<String,Object>>> _dataset)
	{
		mDataSet = _dataset;
	}
	
	public HashMap<String, ArrayList<HashMap<String,Object>>> getDataSet()
	{
		return mDataSet;
	}
	public void setItems( ArrayList<List<Object>> _itemList )
	{
		mItemList = _itemList;
	}
	
	@SuppressWarnings("unchecked")
	public void setVisibleParam( Object _value )
	{
		// 문자열을 JsonParsing 하여 담아둔다
		
		if( _value instanceof JSONObject )
		{
			try {
				mVisibleParam = new ArrayList<HashMap<String, String>>();
				ArrayList<String> _visibleParamStr = (ArrayList<String>) ((JSONObject)_value).get("visibleParams");
				int _size =  _visibleParamStr.size();
				if( _size > 0 )
				{
					for( int i = 0; i < _size; i++ )
					{
						mVisibleParam.add( (HashMap<String, String>) JSONValue.parse( URLDecoder.decode( _visibleParamStr.get(i) , "UTF-8")) );
					}
					
					if( ((JSONObject)_value).containsKey("result") && ((JSONObject)_value).get("result").equals("false") ) mVisibleResultFlag = "false";
					else mVisibleResultFlag = "true";
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if( _value instanceof JSONArray )
		{
			mVisibleParam = (ArrayList<HashMap<String, String>>) _value;
		}
		
	}
	public ArrayList<HashMap<String, String>> getVisibleParam()
	{
		return mVisibleParam;
	}

	public String getVisibleResult()
	{
		return mVisibleResultFlag;
	}
	
	@SuppressWarnings("unchecked")
	public void setGroupColumn( ArrayList<String> _value )
	{
		// 문자열을 JsonParsing 하여 담아둔다
		try {
			
			mGroupColumn = new ArrayList<HashMap<String, String>>();
			int _size =  _value.size();
			if( _size > 0 )
			{
				for( int i = 0; i < _size; i++ )
				{
					mGroupColumn.add( (HashMap<String, String>) JSONValue.parse( URLDecoder.decode( _value.get(i) , "UTF-8")) );
				}
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ArrayList<HashMap<String, String>> getGroupColumn()
	{
		return mGroupColumn;
	}
	
	public void setFitOnePage( String _value )
	{
		mFitOnePage = _value.equals("true");
	}

	public void setFitOnePage( boolean _value )
	{
		mFitOnePage = _value;
	}
	
	public boolean getFitOnePage()
	{
		return mFitOnePage;
	}
	
	public void setXArr(ArrayList<Integer> _value)
	{
		mXArr = _value;
	}
	public ArrayList<Integer> getXArr()
	{
		if( mXArr == null ) mXArr = new ArrayList<Integer>();
		return mXArr;
	}
	
	public HashMap<String, Integer> getLabelBandInfo()
	{
		return mLabelBandInfo;
	}
	
	
	public PageInfoSimple clone()
	{
		PageInfoSimple _clonePage = new PageInfoSimple( getProjectInfo(), (HashMap<String, Object >) mPageMap.clone() , mDataSet );
		_clonePage.mPageMap = this.mPageMap;
		_clonePage.mWidth = this.mWidth;
		_clonePage.mHeight = this.mHeight;
		_clonePage.mBackgroundAlpha = this.mBackgroundAlpha;
		_clonePage.mCloneRowCount = this.mCloneRowCount;
		_clonePage.mCloneColCount = this.mCloneColCount;
		_clonePage.mMinimumResizeFontSize = this.mMinimumResizeFontSize;
		_clonePage.mReportType = this.mReportType;
		_clonePage.mId = this.mId;
		_clonePage.mDivide = this.mDivide;
		_clonePage.mBackgroundColor = this.mBackgroundColor;
		_clonePage.mClone = this.mClone;
		_clonePage.mIsPivot = this.mIsPivot;
		_clonePage.mIsGroup = this.mIsGroup;
		_clonePage.mIsConnect = this.mIsConnect;
		_clonePage.mDirection = this.mDirection;
		_clonePage.mUseGroupPageClone = this.mUseGroupPageClone;
		_clonePage.mPageBackgroundImage = this.mPageBackgroundImage;
		_clonePage.mPageParserClass = this.mPageParserClass;
		_clonePage.mDataRowCount = this.mDataRowCount;
		_clonePage.mUbfunction = this.mUbfunction;
		_clonePage.mGroupData = this.mGroupData;
		_clonePage.mPropertyAr = this.mPropertyAr;
		_clonePage.mProjectIndex = this.mProjectIndex;
		_clonePage.isExportType = this.isExportType;
		_clonePage.mRequiredValueList = this.mRequiredValueList;
		_clonePage.mTabIndexList = this.mTabIndexList;
		_clonePage.mItems = this.mItems;
		_clonePage.mItemList = this.mItemList;
		_clonePage.mFitOnePage = this.mFitOnePage;
		_clonePage.mXArr = this.mXArr;
		_clonePage.mLabelBandInfo = this.mLabelBandInfo;
		
		_clonePage.mDefaultBandInfo = this.mDefaultBandInfo;
		_clonePage.mDefaultBandList = this.mDefaultBandList;
		
		// default Band정보가 있을경우 clone시 담아서 전달
		
		return _clonePage;
	}
	
	private BandInfoMapDataSimple cloneBandInfoData( BandInfoMapDataSimple originalBandData, String _grpName, String _defaultBand, String _dataSet, int _grpIndex)
	{
		BandInfoMapDataSimple cloneBandData = originalBandData.cloneBandInfo();
		
		cloneBandData.setGroupName( _grpName );
		cloneBandData.setGroupBand( _grpName  );
		cloneBandData.setDefaultBand(_defaultBand);
		cloneBandData.setDataSet(_dataSet);
		cloneBandData.setGroupDataIndex(_grpIndex);
		
		// 데이터 밴드에 데이터의 Row수를 담아두기
		if( cloneBandData.getClassName().equals(BandInfoMapData.DATA_BAND) && mDataSet.containsKey(_dataSet) ) 
		{ 
			if( mDataSet.get(_dataSet).size() > 0 && cloneBandData.getRowCount() < mDataSet.get(_dataSet).size() )
			{
				cloneBandData.setRowCount( mDataSet.get(_dataSet).size() );
			}
		}
		
		return cloneBandData;
	}
	
	private void updateBandInfoTable()
	{
		if( bandInfoData == null || bandInfoData.isEmpty() ) return;
		// bandInfo가 있을경우 
		boolean isAutoTableHeight  =  false;
		
        for( String key : bandInfoData.keySet() ){
        	isAutoTableHeight = false;
        	
			isAutoTableHeight = bandInfoData.get( key ).getAutoTableHeight();
			// autoTableHeight를 설정한 경우 Cell이 아닌경우 자신이 포함된 Cell의 정보를 담아놓는다.
			if(isAutoTableHeight){			
				//cell아닌 경우 table 포함 여부 확인 by IHJ
				Iterator<String> nameItr = (Iterator)bandInfoData.keySet().iterator();       
				ArrayList<HashMap<String, Object>> chilrden;
				HashMap<String, Object> currentItemData;
				float xTblPos = 0;
				float yTblPos = 0;
				float tblWidth = 0;
				float tblHeight = 0;
				float xOtherPos = 0;
				float yOtherPos = 0;
				
				ArrayList<Float> arTempTableRowH;
				float sumHeight = 0;
				while(nameItr.hasNext()) {
					String bandID = nameItr.next();
					if(bandInfoData.get(bandID).getTableProperties() != null){
						ArrayList<HashMap<String, Object>> arTableProperties = bandInfoData.get(bandID).getTableProperties();            	
						chilrden = bandInfoData.get(bandID).getChildren();
						for(int jj = 0; jj < chilrden.size(); jj++){
							currentItemData = chilrden.get(jj);				
							if(!currentItemData.containsKey("isCell")){//cell 이 아니면 테이블 포함 여부 체크 및 cell 정보 저장
								for(int kk = 0; kk<arTableProperties.size(); kk++){
									xTblPos = Float.valueOf( arTableProperties.get(kk).get("x").toString() );
									yTblPos = Float.valueOf(arTableProperties.get(kk).get("band_y").toString() );
									tblWidth = Float.valueOf(arTableProperties.get(kk).get("width").toString() );
									tblHeight = Float.valueOf(arTableProperties.get(kk).get("height").toString() );
									sumHeight = yTblPos;
									
									xOtherPos = Float.valueOf(currentItemData.get("x").toString() );
									yOtherPos = Float.valueOf(currentItemData.get("band_y").toString() );
									
									//테이블 안에 있는 확인
									if((xTblPos <= xOtherPos && xOtherPos <= (xTblPos + tblWidth))
											&& (yTblPos <= yOtherPos && yOtherPos <= (yTblPos + tblHeight)) ){
										arTempTableRowH = (ArrayList<Float>) arTableProperties.get(kk).get("tempTableRowHeight");
										
										int hh = 0;
										//@TEST 아이템의 가운데 정렬속성이 있을경우 셀의 한가운데로 이동하기 위해 겹치는 셀의 시작인덱스와 ROWSPAN값을 담아둔다.
										boolean _isAutoVerticalPosition = bandInfoData.get(bandID).getAutoVerticalPosition();
										
										if( _isAutoVerticalPosition )
										{
											ArrayList<HashMap<String, Object>> _cellItems = (ArrayList<HashMap<String, Object>>) arTableProperties.get(kk).get("CELLS");
											
											
											float _cxPos = 0;
											float _cyPos = 0;
											float _cwPos = 0;
											float _chPos = 0;
											
											for( hh = 0; hh < _cellItems.size(); hh++ )
											{
												_cxPos = Float.valueOf( _cellItems.get(hh).get("x").toString() );
												_cyPos = Float.valueOf(_cellItems.get(hh).get("band_y").toString() );
												_cwPos = Float.valueOf(_cellItems.get(hh).get("width").toString() );
												_chPos = Float.valueOf(_cellItems.get(hh).get("height").toString() );
												
												if((_cxPos <= xOtherPos && xOtherPos <= (_cxPos + _cwPos))
														&& (_cyPos <= yOtherPos && yOtherPos <= (_cyPos + _chPos)) ){
													currentItemData.put("cellRowIndex",  Integer.valueOf( _cellItems.get(hh).get("rowIndex").toString()));
													currentItemData.put("tableId", arTableProperties.get(kk).get("tableId").toString() );
													currentItemData.put("cellPadding",0 );
													currentItemData.put("cellRowSpan",  _cellItems.get(hh).get("rowSpan")  );
													break;
												}
											}
											
										}
										else
										{
											for( hh = 0;  hh< arTempTableRowH.size(); hh++){
												sumHeight = sumHeight + arTempTableRowH.get(hh);
												if(sumHeight>=yOtherPos){
													//item이 속한 Cell의 RowIndex 정보(cellRowIndex)저장 by IHJ
													currentItemData.put("cellRowIndex", hh );
													currentItemData.put("tableId", arTableProperties.get(kk).get("tableId") );
													currentItemData.put("cellPadding", yOtherPos - (sumHeight- arTempTableRowH.get(hh)) );
													break;
												}
											}
										}
										
									}
								}
							}
						}//band 내 Item	
					}
				}
			}			
        }
	}
	
	public void makeDefaultBand()
	{
		int i = 0;  
		int j = 0;  
		int max = 0;
		int pageCnt = 0;
		HashMap<String, Object> _item;
		String _className = "";
		
		if(mIsCreate) return;
		
		mDefaultBandInfo = new HashMap<String, BandInfoMapDataSimple>();
		mDefaultBandList = new ArrayList<BandInfoMapDataSimple>();
		
		if( mDataSet == null )
		{
			if( getProjectInfo().getDataSet() == null ) mDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
			else mDataSet = getProjectInfo().getDataSet(); 
		}
		
		// list를 돌아서 item을 생성 
		if( mItemList != null )
		{
			 List<Object> _pageItems;
			
			if( !this.getReportType().equals(ProjectInfo.FORM_TYPE_LABELBAND) )
			{
				ArrayList<HashMap<String, Object>> _bandItems = new ArrayList<HashMap<String, Object>>();
				//Band로드 
				pageCnt = mItemList.size();
				
				for( j =0; j < pageCnt; j++ )
				{
					_pageItems = mItemList.get(j);
					max = _pageItems.size();
					
					for( i=0; i < max; i++ )
					{
						_item = (HashMap<String, Object>) _pageItems.get(i);
						
						if( _item.containsKey("className"))
						{
							_className = _item.get("className").toString();
							
							//BAND일때 
							if(  _className.length() > 4 && _className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") )
							{
								_bandItems.add(_item);
							}
						}
					}
					
				}
				if( _bandItems.size() > 0 ) convertBandDefaultItemList(_bandItems);
				
				_bandItems.clear();
				_bandItems = null;
			}
			
			
			pageCnt = mItemList.size();
			
			for( j =0; j < pageCnt; j++ )
			{
				
				_pageItems = mItemList.get(j);
				max = _pageItems.size();
				
				//아이템 로드
				for( i=0; i < max; i++ )
				{
					_item = (HashMap<String, Object>) _pageItems.get(i);
					
					if(_item.containsKey("visible") && _item.get("visible").toString().equals("false")) continue;
					
					if( _item.containsKey("className"))
					{
						_className = _item.get("className").toString();
						
						//BAND일때 
						if( !this.getReportType().equals(ProjectInfo.FORM_TYPE_LABELBAND) && _className.length() > 4 && _className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") )
						{
							
						}
						else if( _className.equals("UBTable") || _className.equals("UBApproval") )
						{
							//Table일때 
							try {
								convertTableItem( _item, mDefaultBandInfo );
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ScriptException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else
						{
							//그외 Item
							try {
								convertItem( _item, mDefaultBandInfo  );
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							
						}
					}
				}
			}
		}
		
		updateBandInfoTable();
		// Table일경우 테이블 객체 생성로직 필요
		// 밴드일경우 bandInfo에 담도록 처리한다.
	}
	
	private void convertBandDefaultItemList( ArrayList<HashMap<String, Object>> _bands )
	{
		HashMap<String, Object> _childItem;
		int i = 0;
		int j = 0;
		int k = 0;
		int cnt = _bands.size();
		String _className;
		
		String groupName = "";
		HashMap<String, String> originalDataMap = new HashMap<String, String>();
		HashMap<String, ArrayList<HashMap<String, Object>>> DataSet;
		ArrayList<String> _grpSubDataNameAr = new ArrayList<String>();
		ArrayList<ArrayList<String>> groupDataNamesAr = new ArrayList<ArrayList<String>>();
		
		for(i = 0; i < cnt ; i++)
		{
			_childItem = _bands.get(i);
			_className = _childItem.get("className").toString();
			
			BandInfoMapDataSimple  bandData = new BandInfoMapDataSimple(_childItem);
			
			mDefaultBandInfo.put(bandData.getId(), bandData);	// 생성된 밴드 데이터를 ID를 Key값으로 하여 맵에 담아두기
			bandData.setFileLoadType(GlobalVariableData.M_FILE_LOAD_TYPE_JSON);
			mDefaultBandList.add(bandData);
			
			if(  _className.equals("UBGroupHeaderBand")  )
			{
				groupName = bandData.getId();
				
				// sort옵션여부 체크
				ArrayList<Boolean> orderBy = new ArrayList<Boolean>();
				ArrayList<String> grpColumnAr = new ArrayList<String>();
				if( bandData.getColumnAr() != null && bandData.getColumnAr().size() > 0 )
				{
					grpColumnAr = bandData.getColumnAr();
				}
				
				// 각각의 컬럼별 orderBy값을 가지고 있는 columns속성이 존재할경우
				if( bandData.getColumnsAC() != null && bandData.getColumnsAC().size() > 0 )
				{
					int grpColumnArSize = grpColumnAr.size();
					for ( j = 0; j < grpColumnArSize; j++) {
						
						for (HashMap<String, String> _column : bandData.getColumnsAC()) {
							
							if(_column.containsKey("column") && _column.get("column").equals(grpColumnAr.get(j)))
							{
								if( _column.get("orderBy").toLowerCase().equals("true"))
								{
									orderBy.add(true);
								}
								else
								{
									orderBy.add(false);
								}
							}
							
						}
						
					}
				}
				else
				{
					orderBy.add( bandData.getOrderFlag() );
				}
				
				bandData.setOrderBy(orderBy);
				
				// originalOrder 속성 지정
				ArrayList<String> grpFooterAr = new ArrayList<String>();
				int bandDataColumnArSize = bandData.getColumnAr().size();
				for ( j = 0; j < bandDataColumnArSize; j++) {
					grpFooterAr.add("");
				}
				
				bandData.setFooterAr(grpFooterAr);
				bandData.setGroupBand(groupName);
				
			}
			else if(  _className.equals("UBCrossTabBand")  )
			{
				bandData.setOriginalItemData(_childItem);
			}

		}

		mOriginalDataMap = originalDataMap;
		mGroupDataNamesAr = groupDataNamesAr;
	}
	
	public void clear()
	{
		if(mPageProperty !=null) mPageProperty.clear();
		if(mGroupData !=null) mGroupData.clear();
		if(mBackgroundImage !=null) mBackgroundImage.clear();
		if(mBackgroundImage !=null) mBackgroundImage.clear();
		if(mTabIndexList !=null) mTabIndexList.clear();
		if(mItems !=null) mItems.clear();
		if(mPageItemList != null) mPageItemList.clear();
		if(bandInfoData != null) bandInfoData.clear();
		if(bandList != null) bandList.clear();
		if(mGgroupBandCntMap != null) mGgroupBandCntMap.clear();
		if(mDataSet != null) mDataSet.clear();
		if(mItemList != null) mItemList.clear();
		if(mItem != null) mItemList.clear();

		if(mDefaultBandInfo != null) mDefaultBandInfo.clear();
		if(mDefaultBandList != null) mDefaultBandList.clear();
		
		mPropertyAr = null;
		mUbfunction = null;
		mGroupData = null;
		mBackgroundImage = null;
		mRequiredValueList = null;
		mTabIndexList = null;
		mProjectInfo = null;
		mItems = null;
		mPageItemList = null;
		bandInfoData = null;
		bandList = null;
		mGgroupBandCntMap = null;
		mDataSet = null;
		mItemList = null;
		mItem = null;
		mXArr = null;
	}
	
	
	
	public ArrayList<String> getColorArrayString(String _value)
	{
		ArrayList<String> _borderSideAr = new ArrayList<String>();
		
		String[] _strAr = _value.split(",");

		for(String _bStr : _strAr )
		{
			_borderSideAr.add( changeColorToHex(Integer.parseInt(_bStr)) );
		}

		return _borderSideAr;
	}
	
	
	public ArrayList<String> getPathArrayString(String _value)
	{
		ArrayList<String> _borderSideAr = new ArrayList<String>();
		
		JSONArray _jsonObj=(JSONArray) JSONValue.parse(_value);
		
		
		Object _nextX="";
		Object _nextY="";
		
		for( int i=_jsonObj.size(); i>0 ; i-- ){
			JSONObject _jo=(JSONObject) _jsonObj.get(i-1);
			
			Object _x=_jo.get("x");
			Object _y=_jo.get("y");
			
			if( i == _jsonObj.size() ){
				String _pathArray="L" + "," + _x.toString() + "," + _y.toString();
				_borderSideAr.add(0,_pathArray.toString());
			}else if( i == 1 ){
				String _pathArray="M"+ "," + _x.toString() + "," + _y.toString();
				_borderSideAr.add(0,_pathArray.toString());
			}else{
				String _pathArray="Q"+ "," +  _x.toString() + "," +  _y.toString() + "," +  _nextX.toString() + "," +  _nextY.toString();
				_borderSideAr.add(0,_pathArray.toString());
			}
			_nextX=_jo.get("x");
			_nextY=_jo.get("y");
		}

		return _borderSideAr;
	}
	
	/**
	 * String인 borderSide 를 ArrayList로 변환. 
	 * @return ArrayList _borderSideAr
	 * 
	 */
	public ArrayList<String> getBorderSideToArrayList(String _bSide)
	{
		ArrayList<String> _borderSideAr = new ArrayList<String>();

		String[] _strAr = _bSide.split(",");

		for(String _bStr : _strAr )
		{
			_borderSideAr.add(_bStr);
		}

		return _borderSideAr;
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
			
			borderColorIntStr 		=  UBComponent.getProperties(_item, _item.get("className").toString(), "borderColor", "0").toString();
			borderColorStr = ItemConvertParser.changeColorToHex( Integer.parseInt(borderColorIntStr) );
			borderThicknessStr 	=  UBComponent.getProperties(_item, _item.get("className").toString(), "borderWidth", "").toString();
			if( borderThicknessStr.equals("")) borderThicknessStr 	= UBComponent.getProperties(_item, _item.get("className").toString(), "borderThickness", "").toString();
			
			String _borSideName = "";
			ArrayList<String> borderSide  = null;
			if( _item.get("borderSide") instanceof String ) 
			{
				borderSide = new ArrayList<String>();

				try {
					String[] _border = URLDecoder.decode(_item.get("borderSide").toString(), "UTF-8").split(",");
					borderSide = new ArrayList<String>(Arrays.asList(_border));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			else
			{
				borderSide = (ArrayList<String>) _item.get("borderSide");
			}
			String _defBorderType =  getBorderType( UBComponent.getProperties(_item, _item.get("className").toString(), "borderType", "null").toString() );
			for ( i = 0; i < borderSide.size(); i++) {
				
				borderTypeStr = "";
				_borSideName = borderSide.get(i);
				if( _borSideName.equals("top") )
				{
					_borderTypeStr = UBComponent.getProperties(_item, _item.get("className").toString(), "topBorderType", "null").toString();
					if( "null".equals(_borderTypeStr) == false )
					{
						borderTypeStr = getBorderType(_borderTypeStr);
					}
					else
					{
						borderTypeStr = _defBorderType;
					}
				}
				else if( _borSideName.equals("left") )
				{
					_borderTypeStr = UBComponent.getProperties(_item, _item.get("className").toString(), "leftBorderType", "null").toString();
					if( "null".equals(_borderTypeStr) == false )
					{
						borderTypeStr = getBorderType(_item.get("leftBorderType").toString());
					}
					else
					{
						borderTypeStr =_defBorderType;
					}
				}
				else if( _borSideName.equals("right") )
				{
					_borderTypeStr = UBComponent.getProperties(_item, _item.get("className").toString(), "rightBorderType", "null").toString();
					if( "null".equals(_borderTypeStr) == false )
					{
						borderTypeStr = getBorderType(_item.get("rightBorderType").toString());
					}
					else
					{
						borderTypeStr = _defBorderType;
					}
				}
				else if( _borSideName.equals("bottom") )
				{
					_borderTypeStr = UBComponent.getProperties(_item, _item.get("className").toString(), "bottomBorderType", "null").toString();
					if( "null".equals(_borderTypeStr) == false )
					{
						borderTypeStr = getBorderType(_item.get("bottomBorderType").toString());
					}
					else
					{
						borderTypeStr = _defBorderType;
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
	
	
}	
