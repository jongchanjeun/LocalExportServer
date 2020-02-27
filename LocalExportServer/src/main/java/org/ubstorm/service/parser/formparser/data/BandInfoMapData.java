package org.ubstorm.service.parser.formparser.data;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

	
public class BandInfoMapData {
	public static String PAGE_HEADER_BAND 			= "UBPageHeaderBand";
	public static String PAGE_FOOTER_BAND 			= "UBPageFooterBand";
	public static String GROUP_HEADER_BAND 			= "UBGroupHeaderBand";
	public static String DATA_HEADER_BAND 			= "UBDataHeaderBand";
	public static String DATA_BAND 					= "UBDataBand";
	public static String DATA_PAGE_FOOTER_BAND 		= "UBDataPageFooterBand";
	public static String GROUP_FOOTER_BAND 			= "UBGroupFooterBand";
	public static String DATA_FOOTER_BAND 			= "UBDataFooterBand";
	public static String EMPTY_BAND 				= "UBEmptyBand";
	public static String CROSSTAB_BAND 				= "UBCrossTabBand";
	
	public static String M_COLUMN_SEPARATOR			= "§";			// 그룹헤더의 COLUMN값 구분자
	
	public static String VISIBLE_TYPE_NORMNAL 		= "normal";
	public static String VISIBLE_TYPE_ALL	 		= "all";
	public static float BAND_MAX_WIDTH				= 10000;
	
	 interface IMethodBar {
		    void callMethod( String value, String type );
	 }
	 
	
	float mX = 0f;
	float mY = 0f;
	float mWidth = 0f;
	float mHeight = 0f;
	float mLimiteHeight = 0f;
	float mDefaultHeight = 0f;
	
	int mRowCount = 0;
	int mGroupDataIndex = 0;
	
	Boolean mAdjustableHeight = false;
	Boolean mAutoHeight = false;
	Boolean mNewPage = false;
	Boolean mOriginalOrder = false;
	Boolean mOrderByFlag = false;
	
	ArrayList<String> mSequence = new ArrayList<String>();
	
	ArrayList<HashMap<String, String>> mSort;
	ArrayList<HashMap<String, String>> mFilter;
	ArrayList<String> mColumnAr;
	ArrayList<String> mFooterAr;
	ArrayList<Boolean> mOrderBy;
	String mSubPage;
	ArrayList<String> mColumnsAr;
	ArrayList<HashMap<String, String>> mColumns;
	ArrayList<HashMap<String, String>> mUbfx = new ArrayList<HashMap<String,String>>();
	ArrayList<String> mRowCountList;
	ArrayList<HashMap<String, ArrayList<Float>>> mTableRowHeight;	
	ArrayList<HashMap<String, Value>> mTableProperties;
	
	HashMap<String, Float> mTableBandY;
	
	boolean mUseDataHeaderBand = false;

	public HashMap<String, Float> getTableBandY() {
		return mTableBandY;
	}
	
	public void setTableBandY(HashMap<String, Float> _tableBandY) {
		mTableBandY = _tableBandY;
	}
	

	// Resize Font 각 row별 아이템의 id를 키값으로 하여 fontSize 값을 담아두기
	ArrayList<HashMap<String, Float>> mResizeFontData = new ArrayList<HashMap<String, Float>>();
	
	ArrayList<HashMap<String, String>> mUbFxList;
	
	int mGroupLength = 0;
	
	String mClassName = "";
	String mSubPageID = "";
	String mRepeat = "";
	String mGroupBand = "";
	String mHeaderBand = "";
	String mDefaultBand = "";
	String mDataSet = "";
	String mGroupHeader = "";
	String mGroupName = "";
	String msummery = "";
	String mFooter = "";
	String mId = "";
	float mAdjustableHeightMargin = -1;
	
	// 데이터 밴드에 라벨밴드 기능 사용여부 지정
	// 데이터 밴드에 라벨밴드 기능 구현시 반복할 갯수
	// 데이터 밴드에 라벨밴드 기능 구현시 반복할 방향( 가로,세로 )
	// 데이터 밴드에 라벨밴드 기능 구현시 좌우 padding 과 사이 Gap
	boolean mUseLabelBand = false;
	int mLabelBandColCount = 1;
	String mLabelBandDirection = "horizontal";
	float mLabelBandPadding = 0;
	float mLabelBandGap = 0;
	
	boolean mVisible = true;
	
	boolean mUseGroupDataHeaderBand = true;
	int mHeaderVisibleDepth = -1;
	private float mLabelBandDisplayWidth = 0;
	String mUseHeaderBandGroupName = "";
	
	int mGroupTotalPageCnt = -1;
	int mGroupStartPageIdx = -1;
	
	//AutoHeight기능용 
	
	// AutoTableHeight값이 true일때 셀 내부의 아이템의 가운데 정렬 여부
	boolean mAutoVerticalPosition = false;
	public Boolean getAutoVerticalPosition() {
		return mAutoVerticalPosition;
	}

	public void setAutoVerticalPosition(Boolean mAutoVerticalPosition) {
		this.mAutoVerticalPosition = mAutoVerticalPosition;
	}
	
	public void setAutoVerticalPosition(String value) {
		if( value.equals("true")) value = "True";
		this.mAutoVerticalPosition = Boolean.valueOf(value);
	}	
	
	Boolean mAutoTableHeight = false;
	public Boolean getAutoTableHeight() {
		return mAutoTableHeight;
	}

	public void setAutoTableHeight(Boolean mAutoTableHeight) {
		this.mAutoTableHeight = mAutoTableHeight;
	}
	
	
	public void setAutoTableHeight(String value) {
		if( value.equals("true")) value = "True";
		this.mAutoTableHeight = Boolean.valueOf(value);
	}
	

	ArrayList<Float> mHeightList;
	ArrayList<HashMap<String, Float>> mItemRowHeight;
	ArrayList<Float> adjustableHeightListAr = new ArrayList<Float>();
	ArrayList<HashMap<String, Value>> children = new ArrayList<HashMap<String,Value>>();		// 생성될 아이템들의 정보를 담아둘 배열(한Row에 생성될 아이템의 정보를 담는 객체)

	
	// 필수값 을 가진 아이템의 ID를 담아둘 리스트
	ArrayList<String> mRequiredItems = new ArrayList<String>();

	// 탭 인덱스를 가진 아이템의 ID를 담아둘 리스트
	ArrayList<String> mTabIndexItem = new ArrayList<String>();
	
	public ArrayList<HashMap<String, Value>> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<HashMap<String, Value>> children) {
		this.children = children;
	}

	//MultiFormBand 타입용
	int mStartPage = 0;
	int mEndPage = 0;
	
	float mFooterHeight = 0f;
	float mRealHeight = 0f;
	
	String mDataBand = "";
	
	HashMap<String, Object> mResizeTextStyleItems;
	Boolean mUseBand = true;
	Boolean mResizeText = false;
	
	Element itemXml = null;
	
	int mMinRowCount = 0;
	
	String mFileLoadType = "XML";
	
	HashMap<String, Object> mOriginalItemData = null;
	
	// CrossTab의 Visible 타입 지정( normal : 기준과 동일, all : 아이템의 Width에 맞춰서 페이지의 Width 변경 )
	String mVisibleType = VISIBLE_TYPE_NORMNAL;
//	String mVisibleType = VISIBLE_TYPE_ALL;
	
	public Element getItemXml()
	{
		return itemXml;
	}
	
	public void setItemXml( Element value )
	{
		itemXml = value;
	}
	
	
	public BandInfoMapData() {
		super();
		
		String _pageMaxW = common.getPropertyValue("ubform.pageMaxWidth");
		
		if( _pageMaxW != null && _pageMaxW.equals("") == false)
		{
			BAND_MAX_WIDTH = Float.valueOf(_pageMaxW);
		}
		// TODO Auto-generated constructor stub
	}

	public BandInfoMapData( Element e ) {
		super();
		// TODO Auto-generated constructor stub
		
		String _pageMaxW = common.getPropertyValue("ubform.pageMaxWidth");
		
		if( _pageMaxW != null && _pageMaxW.equals("") == false)
		{
			BAND_MAX_WIDTH = Float.valueOf(_pageMaxW);
		}
		//GroupBand용 속성은 추후 처리
		propertyMappingFn();
		
		try {
			setItemXml(e);
			elementToData(e);
		} catch (Exception e2) {
			// TODO: handle exception
		}
		
	}

	public BandInfoMapData( HashMap<String, Object> _item ) {
		super();
		// TODO Auto-generated constructor stub
		
		String _pageMaxW = common.getPropertyValue("ubform.pageMaxWidth");
		
		if( _pageMaxW != null && _pageMaxW.equals("") == false)
		{
			BAND_MAX_WIDTH = Float.valueOf(_pageMaxW);
		}
		//GroupBand용 속성은 추후 처리
		propertyMappingFn();
		
		try {
			hashMapToData(_item);
		} catch (Exception e2) {
			// TODO: handle exception
		}
		
	}
	
	HashMap<String, IMethodBar> propertyMapping = new HashMap<String, IMethodBar>();
	
	private void propertyMappingFn()
	{
		
		propertyMapping.put("id", new IMethodBar(){@Override 
			public void callMethod(String value, String type) {
			setId(value); } } );

		propertyMapping.put("className", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
					setClassName(value); } } );
			
			propertyMapping.put("x", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setX(value); } } );
			
			propertyMapping.put("y", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setY(value); } } );
			
			propertyMapping.put("width", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setWidth(value); } } );
			
			propertyMapping.put("height", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setHeight(value); } } );
			
			propertyMapping.put("adjustableHeight", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setAdjustableHeight(value); } } );
			propertyMapping.put("autoTableHeight", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setAutoTableHeight(value); } } );
			
			propertyMapping.put("repeat", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setRepeat(value); } } );
			
			propertyMapping.put("autoHeight", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setAutoHeight(value); } } );
			
			propertyMapping.put("sequence", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setSequence(value); } } );
			
			propertyMapping.put("dataBand", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setDataBand(value); } } );
			
			propertyMapping.put("groupBand", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setGroupBand(value); } } );
			
			propertyMapping.put("subPageId", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setSubPageID(value); } } );

			propertyMapping.put("newPage", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setNewPage(value); } } );

			propertyMapping.put("sort", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setSort(value); } } );

			propertyMapping.put("column", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setColumnAr(value); } } );

			propertyMapping.put("columns", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setColumns(value, type); } } );
			
			propertyMapping.put("filter", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setFilter(value); } } );

			propertyMapping.put("orderBy", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setOrderFlag(value); } } );

			propertyMapping.put("dataSet", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setDataSet(value); } } );
			
			propertyMapping.put("groupHeader", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setGroupHeader(value); } } );

			propertyMapping.put("resizeText", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setResizeText(value); } } );

			propertyMapping.put("originalOrder", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setOriginalOrder(value); } } );
			
			propertyMapping.put("useLabelBand", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setUseLabelBand(value); } } );
			propertyMapping.put("labelBandDirection", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setLabelBandDirection(value); } } );
			propertyMapping.put("labelBandColCount", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setLabelBandColCount(value); } } );
			propertyMapping.put("labelBandPadding", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setLabelBandPadding(value); } } );
			propertyMapping.put("labelBandGap", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setLabelBandGap(value); } } );
			propertyMapping.put("headerVisibleDepth", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setHeaderVisibleDepth(value); } } );
			propertyMapping.put("labelBandDisplayWidth", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setLabelBandDisplayWidth(value); } } );
			propertyMapping.put("minRowCount", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setMinRowCount(value); } } );
			propertyMapping.put("adjustableHeightMargin", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setAdjustableHeightMargin(value); } } );
			propertyMapping.put("visibleType", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setVisibleType(value); } } );
			
			propertyMapping.put("useDataHeaderBand", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setUseDataHeaderBand(value); } } );
			
			propertyMapping.put("autoVerticalPosition", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setAutoVerticalPosition(value); } } );
			
			propertyMapping.put("visible", new IMethodBar(){@Override 
				public void callMethod(String value, String type) {
				setVisible( value ); } } );
		
	}
	
	
	private void elementToData( Element e) throws UnsupportedEncodingException
	{
//		XPath _xpath = XPathFactory.newInstance().newXPath();
//		NodeList _propertys2;
//		try {
//			_propertys2 = (NodeList) _xpath.evaluate("property", e, XPathConstants.NODESET);
//		} catch (XPathExpressionException e1) {
//			return;
//		}
		
		NodeList _propertys = e.getElementsByTagName("property");
		
		Element childItem;
		propertyMapping.get("className").callMethod( URLDecoder.decode( e.getAttribute("className"), "UTF-8" ),"string" );
		propertyMapping.get("id").callMethod( URLDecoder.decode( e.getAttribute("id"), "UTF-8" ),"string" );
		int _propertiesSize = _propertys.getLength();
		for (int i = 0; i < _propertiesSize; i++) {
			
			childItem = (Element) _propertys.item(i);
			if( propertyMapping.containsKey(childItem.getAttribute("name") ) )
			{
				
				try {
					propertyMapping.get(childItem.getAttribute("name")).callMethod( URLDecoder.decode( childItem.getAttribute("value"), "UTF-8" ),childItem.getAttribute("type") );
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}
		
		//ubfx담기
		NodeList _nodes = e.getElementsByTagName("ubfunction");
		if( _nodes != null && _nodes.getLength() > 0 )
		{
			mUbFxList = new ArrayList<HashMap<String, String>>();
			int _nodesLength = _nodes.getLength();
			for (int i = 0; i < _nodesLength; i++) {
				HashMap<String, String> _ubfxMap = new HashMap<String, String>();
				childItem = (Element) _nodes.item(i);
				
				if( this.mClassName.equals("UBCrossTabBand") )
				{
					Element _parentEl = (Element) childItem.getParentNode().getParentNode();
					
					if( _parentEl.getAttribute("className").equals(this.mClassName) )
					{
						String _property 	= URLDecoder.decode( childItem.getAttribute("property"), "UTF-8");
						String _value 		= URLDecoder.decode( childItem.getAttribute("value"), "UTF-8");
						
						_ubfxMap.put("property", _property);
						_ubfxMap.put("value", _value);
						
						mUbFxList.add(_ubfxMap);
					}
				}
				else
				{
					String _property 	= URLDecoder.decode( childItem.getAttribute("property"), "UTF-8");
					String _value 		= URLDecoder.decode( childItem.getAttribute("value"), "UTF-8");
					
					_ubfxMap.put("property", _property);
					_ubfxMap.put("value", _value);
					
					mUbFxList.add(_ubfxMap);
				}
				
			}
		}
		
	}
	
	private void hashMapToData( HashMap<String, Object> _item) throws UnsupportedEncodingException
	{
		propertyMapping.get("className").callMethod( URLDecoder.decode( _item.get("className").toString(), "UTF-8" ),"string" );
		propertyMapping.get("id").callMethod( URLDecoder.decode( _item.get("id").toString(), "UTF-8" ),"string" );
		
		for( String _key : _item.keySet() )
		{
			if(_key.equals("ubfx"))
			{
				if( _item.get(_key) != null)
				{
					@SuppressWarnings("unchecked")
					ArrayList<HashMap<String, String>> _list = (ArrayList<HashMap<String, String>>) _item.get(_key);
					for( int i = 0; i < _list.size(); i++ )
					{
						_list.get(i).put("property", URLDecoder.decode( _list.get(i).get("property").toString(), "UTF-8" ));
						_list.get(i).put("value", URLDecoder.decode( _list.get(i).get("value").toString(), "UTF-8" ));
					}
					
					setUbfx( _list );
					setUbFunction( _list );
				}
			}
			else if( _key.equals("columns")&& this.mClassName.equals(BandInfoMapData.GROUP_HEADER_BAND) )
			{
				try {
					propertyMapping.get(_key).callMethod( URLDecoder.decode( _item.get(_key).toString(), "UTF-8" ), "ArrayCollection" );
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else
			{
				if( propertyMapping.containsKey( _key ) )
				{
					try {
						propertyMapping.get(_key).callMethod( URLDecoder.decode( _item.get(_key).toString(), "UTF-8" ), "string" );
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
		
	}
	
	
	
	
	public float getX() {
		return mX;
	}
	public void setX(float mX) {
		this.mX = mX;
	}
	
	public void setX(String value) {
		this.mX = Float.valueOf( value ); 
	}
	
	public float getY() {
		return mY;
	}
	
	public void setY(float mY) {
		this.mY = mY;
	}
	
	public void setY(String value) {
		this.mY = Float.valueOf( value ); 
	}
	
	public float getWidth() {
		return mWidth;
	}
	public void setWidth(float mWidth) {
		this.mWidth = mWidth;
	}
	
	public void setWidth(String value) {
		this.mWidth = Float.valueOf( value );
	}
	
	public float getHeight() {
		return mHeight;
	}
	public void setHeight(float mHeight) {
		this.mHeight = Math.round(mHeight);
	}
	
	public void setHeight(String value) {
		this.mHeight =  Math.round( Float.valueOf( value ) );
	}

	public float getLimiteHeight() {
		return mLimiteHeight;
	}
	public void setLimiteHeight(float mLimiteHeight) {
		this.mLimiteHeight = mLimiteHeight;
	}
	public void setLimiteHeight(String value) {
		this.mLimiteHeight = Float.valueOf( value );
	}
	
	public float getDefaultHeight() {
		return mDefaultHeight;
	}
	public void setDefaultHeight(float mDefaultHeight) {
		this.mDefaultHeight = mDefaultHeight;
	}
	public void setDefaultHeight(String value) {
		this.mDefaultHeight = Float.valueOf( value );
	}
	
	public int getRowCount() {
		return mRowCount;
	}
	public void setRowCount(int mRowCount) {
		this.mRowCount = mRowCount;
	}
	public void setRowCount(String value) {
		this.mRowCount = Integer.valueOf( value );
	}
	
	public int getGroupDataIndex() {
		return mGroupDataIndex;
	}
	public void setGroupDataIndex(int mGroupDataIndex) {
		this.mGroupDataIndex = mGroupDataIndex;
	}
	public void setGroupDataIndex(String value) {
		this.mGroupDataIndex = Integer.valueOf( value );
	}
	
	public Boolean getAdjustableHeight() {
		return mAdjustableHeight;
	}
	public void setAdjustableHeight(Boolean mAdjustableHeight) {
		this.mAdjustableHeight = mAdjustableHeight;
	}
	public void setAdjustableHeight(String value) {
		if( value.equals("true")) value = "True";
		
		this.mAdjustableHeight = Boolean.valueOf(value);
	}
	
	public Boolean getAutoHeight() {
		return mAutoHeight;
	}
	public void setAutoHeight(Boolean mAutoHeight) {
		this.mAutoHeight = mAutoHeight;
	}
	public void setAutoHeight(String value) {
		if( value.equals("true")) value = "True";
		
		this.mAutoHeight = Boolean.valueOf(value);
	}
	
	public Boolean getNewPage() {
		return mNewPage;
	}
	public void setNewPage(Boolean mNewPage) {
		this.mNewPage = mNewPage;
	}
	public void setNewPage(String value) {
		if( value.equals("true")) value = "True";
		
		this.mNewPage = Boolean.valueOf(value);
	}

	public Boolean getOrderFlag() {
		return mOrderByFlag;
	}
	public void setOrderFlag(Boolean mOrderByFlag) {
		this.mOrderByFlag = mOrderByFlag;
	}
	public void setOrderFlag(String value) {
		if( value.equals("true")) value = "True";
		
		this.mOrderByFlag = Boolean.valueOf(value);
	}
	
	public Boolean getOriginalOrder() {
		return mOriginalOrder;
	}
	public void setOriginalOrder(Boolean mOriginalOrder) {
		this.mOriginalOrder = mOriginalOrder;
	}
	public void setOriginalOrder(String value) {
		if( value.equals("true")) value = "True";
		
		this.mOriginalOrder = Boolean.valueOf(value);
	}

	public ArrayList<String> getSequence() {
		return mSequence;
	}
	public void setSequence(ArrayList<String> mSequence) {
		this.mSequence = mSequence;
	}
	public void setSequence(String value) {
		this.mSequence = new ArrayList<String>();
	}
	
	public ArrayList<HashMap<String, String>> getSort() {
		return this.mSort;
	}
	public void setSort(ArrayList<HashMap<String, String>> mSort) {
		this.mSort = mSort;
	}
	
	@SuppressWarnings("unchecked")
	public void setSort(String value) {
		ArrayList<HashMap<String,String>> _sort = new ArrayList<HashMap<String,String>>();
		if( value != null && value.equals("") == false && value.equals("null") == false )
		{
			Object _val;
			try {
				_val = JSONValue.parseWithException(value);
				JSONArray jsonArray = (JSONArray) _val;
				List<Object> retData = Value.jsonArray2List( jsonArray );
				for (int i = 0; i < retData.size(); i++) {
					_sort.add( (HashMap<String, String>) retData.get(i) );
				}
			} catch (ParseException e) {
//				e.printStackTrace();
			}
			
		}
		this.mSort = _sort;
	}

	public ArrayList<HashMap<String, String>> getFilter() {
		return mFilter;
	}
	public void setFilter(ArrayList<HashMap<String, String>> mFilter) {
		this.mFilter = mFilter;
	}
	
	@SuppressWarnings("unchecked")
	public void setFilter(String value) {
		ArrayList<HashMap<String,String>> _filter = new ArrayList<HashMap<String,String>>();
		
		if( value != null && value.equals("") == false && value.equals("null") == false  )
		{
			Object _val;
			try {
				_val = JSONValue.parseWithException(value);
				JSONArray jsonArray = (JSONArray) _val;
				List<Object> retData = Value.jsonArray2List( jsonArray );
				for (int i = 0; i < retData.size(); i++) {
					_filter.add( (HashMap<String, String>) retData.get(i) );
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
		}
		this.mFilter = _filter;
	}
	
	public ArrayList<String> getColumnAr() {
		return mColumnAr;
	}
	public void setColumnAr(ArrayList<String> mColumnAr) {
		this.mColumnAr = mColumnAr;
	}
	public void setColumnAr(String value) {
		ArrayList<String> colAr;
		
		try {
			M_COLUMN_SEPARATOR =  URLDecoder.decode(M_COLUMN_SEPARATOR,"UTF-8");
		} catch (Exception e) {
			M_COLUMN_SEPARATOR = "§";
		}
		
		try {
			colAr = new ArrayList<String>( Arrays.asList( URLDecoder.decode(value,"UTF-8").split(M_COLUMN_SEPARATOR) ) );
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			colAr = new ArrayList<String>();
		}
		this.mColumnAr = colAr;
	}
	
	public ArrayList<String> getFooterAr() {
		return mFooterAr;
	}
	public void setFooterAr(ArrayList<String> mFooterAr) {
		this.mFooterAr = mFooterAr;
	}
	
	public ArrayList<Boolean> getOrderBy() {
		return mOrderBy;
	}
	public void setOrderBy(ArrayList<Boolean> mOrderBy) {
		this.mOrderBy = mOrderBy;
	}
	
	public String getId() {
		return mId;
	}
	
	public void setId(String value) {
		this.mId = value;
	}
	
	public String getSubPage() {
		return mSubPage;
	}
	public void setSubPage(String mSubPage) {
		this.mSubPage = mSubPage;
	}
	public void setColumnsAR( ArrayList<String> _columnsAr )
	{
		mColumnsAr = _columnsAr;
	}
	public ArrayList<String> getColumnsAR() {
		return mColumnsAr;
	}
	public ArrayList<HashMap<String, String>> getColumnsAC() {
		return mColumns;
	}
	public void setColumns(ArrayList<HashMap<String, String>> mColumns) {
		this.mColumns = mColumns;
	}
	
	public void setColumns(String value, String type) {
		ArrayList<HashMap<String,String>> _columns = new ArrayList<HashMap<String,String>>();
		if( value != null && value.equals("") == false && type.equals("ArrayCollection") && value.equals("null") == false )
		{
			Object _val;
			try {
				_val = JSONValue.parseWithException(value);
				JSONArray jsonArray = (JSONArray) _val;
				List<Object> retData = Value.jsonArray2List( jsonArray );
				for (int i = 0; i < retData.size(); i++) {
					_columns.add( (HashMap<String, String>) retData.get(i) );
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			this.mColumns = _columns;
		}
		else if( value.equals("") == false )
		{
			ArrayList<String> colAr;
			
			try {
				M_COLUMN_SEPARATOR =  URLDecoder.decode(M_COLUMN_SEPARATOR,"UTF-8");
			} catch (Exception e) {
				M_COLUMN_SEPARATOR = "§";
			}
			
			try {
				colAr = new ArrayList<String>( Arrays.asList( URLDecoder.decode(value,"UTF-8").split( M_COLUMN_SEPARATOR ) ) );
			} catch (UnsupportedEncodingException e) {
				colAr = new ArrayList<String>();
			}
			
			this.mColumnsAr = colAr;
		}
		
	}
	
	
	public ArrayList<HashMap<String, String>> getUbfx() {
		return mUbfx;
	}
	public void setUbfx(ArrayList<HashMap<String, String>> mUbfx) {
		this.mUbfx = mUbfx;
	}
	
	public ArrayList<String> getRowCountList() {
		return mRowCountList;
	}
	public void setRowCountList(ArrayList<String> mRowCountList) {
		this.mRowCountList = mRowCountList;
	}
	public String getClassName() {
		return mClassName;
	}
	public void setClassName(String mClassName) {
		this.mClassName = mClassName;
	}
	public String getSubPageID() {
		return mSubPageID;
	}
	public void setSubPageID(String mSubPageID) {
		this.mSubPageID = mSubPageID;
	}
	public String getRepeat() {
		return mRepeat;
	}
	public void setRepeat(String mRepeat) {

		if( mRepeat.equals("true") || mRepeat.equals("y")) {
		    this.mRepeat = "y";
		} else {
			this.mRepeat = "n";
		}
		
		//this.mRepeat = mRepeat;
	}
	public String getGroupBand() {
		return mGroupBand;
	}
	public void setGroupBand(String mGroupBand) {
		this.mGroupBand = mGroupBand;
	}
	public String getHeaderBand() {
		return mHeaderBand;
	}
	public void setHeaderBand(String mHeaderBand) {
		this.mHeaderBand = mHeaderBand;
	}
	public String getDefaultBand() {
		return mDefaultBand;
	}
	public void setDefaultBand(String mDefaultBand) {
		this.mDefaultBand = mDefaultBand;
	}
	public String getDataSet() {
		return mDataSet;
	}
	public void setDataSet(String mDataSet) {
		this.mDataSet = mDataSet;
	}
	public String getGroupHeader() {
		return mGroupHeader;
	}
	public void setGroupHeader(String mGroupHeader) {
		this.mGroupHeader = mGroupHeader;
	}
	public String getGroupName() {
		return mGroupName;
	}
	public void setGroupName(String mGroupName) {
		this.mGroupName = mGroupName;
	}
	public String getSummery() {
		return msummery;
	}
	public void setSummery(String msummery) {
		this.msummery = msummery;
	}
	public String getFooter() {
		return mFooter;
	}
	public void setFooter(String mFooter) {
		this.mFooter = mFooter;
	}
	public ArrayList<Float> getHeightList() {
		return mHeightList;
	}
	
	
	public void setHeightList(ArrayList<Float> mHeightList) {
		this.mHeightList = mHeightList;
	}
	public ArrayList<HashMap<String, Float>> getItemRowHeight() {
		return mItemRowHeight;
	}
	public void setItemRowHeight(ArrayList<HashMap<String, Float>> mItemRowHeight) {
		this.mItemRowHeight = mItemRowHeight;
	}
	
	public int getStartPage() {
		return mStartPage;
	}
	public void setStartPage(int mStartPage) {
		this.mStartPage = mStartPage;
	}

	public void setStartPage(String value) {
		this.mStartPage = Integer.valueOf( value );
	}
	public int getEndPage() {
		return mEndPage;
	}
	public void setEndPage(int mEndPage) {
		this.mEndPage = mEndPage;
	}
	public void setEndPage(String value) {
		this.mEndPage = Integer.valueOf( value );
	}
	
	public float getFooterHeight() {
		return mFooterHeight;
	}
	public void setFooterHeight(float mFooterHeight) {
		this.mFooterHeight = mFooterHeight;
	}
	public void setFooterHeight(String value) {
		this.mFooterHeight = Float.valueOf(value);
	}
	
	
	public float getRealHeight() {
		return mRealHeight;
	}
	public void setRealHeight(float mRealHeight) {
		this.mRealHeight = mRealHeight;
	}
	public void setRealHeight(String value) {
		this.mRealHeight = Float.valueOf(value);
	}
	
	public String getDataBand() {
		return mDataBand;
	}
	public void setDataBand(String mDataBand) {
		this.mDataBand = mDataBand;
	}
	public HashMap<String, Object> getResizeTextStyleItems() {
		return mResizeTextStyleItems;
	}
	public void setResizeTextStyleItems(HashMap<String, Object> mResizeTextStyleItems) {
		this.mResizeTextStyleItems = mResizeTextStyleItems;
	}
	public Boolean getUseBand() {
		return mUseBand;
	}
	public void setUseBand(Boolean mUseBand) {
		this.mUseBand = mUseBand;
	}
	public void setUseBand(String value) {
		if( value.equals("true")) value = "True";
		this.mUseBand = Boolean.valueOf(value);
	}
	
	public Boolean getResizeText() {
		return mResizeText;
	}
	public void setResizeText(Boolean mResizeText) {
		this.mResizeText = mResizeText;
	}
	public void setResizeText(String value) {
		if( value.equals("true")) value = "True";
		this.mResizeText = Boolean.valueOf(value);
	}
	
	public void setAdjustableHeightListAr( ArrayList<Float> value )
	{
		adjustableHeightListAr = value;
	}
	
	public ArrayList<Float> getAdjustableHeightListAr()
	{
		return adjustableHeightListAr;
	}
	
	
	public ArrayList<HashMap<String, Float>> getResizeFontData() {
		return mResizeFontData;
	}

	public void setResizeFontData(ArrayList<HashMap<String, Float>> mResizeFontData) {
		this.mResizeFontData = mResizeFontData;
	}
	
	/// 데이터 밴드의 라벨밴드 관련 속성
	public boolean getUseLabelBand() {
		return mUseLabelBand;
	}

	public void setUseLabelBand(boolean value) {
		this.mUseLabelBand = value;
	}
	public void setUseLabelBand(String value) {
		if( value.equals("true")) value = "True";
		this.mUseLabelBand = Boolean.valueOf(value);
	}

	public int getLabelBandColCount() {
		return mLabelBandColCount;
	}

	public void setLabelBandColCount(int value) {
		this.mLabelBandColCount = value;
	}
	public void setLabelBandColCount(String value) {
		this.mLabelBandColCount = Integer.valueOf(value);
	}

	public String getLabelBandDirection() {
		return mLabelBandDirection;
	}

	public void setLabelBandDirection(String value) {
		this.mLabelBandDirection = value;
	}

	public float getLabelBandPadding() {
		return mLabelBandPadding;
	}

	public void setLabelBandPadding(float value) {
		this.mLabelBandPadding = value;
	}

	public void setLabelBandPadding(String value) {
		this.mLabelBandPadding = Float.valueOf( value );
	}
	
	public float getLabelBandGap() {
		return mLabelBandGap;
	}

	public void setLabelBandGap(float value) {
		this.mLabelBandGap = value;
	}
	public void setLabelBandGap(String value) {
		this.mLabelBandGap = Float.valueOf( value );
	}
	
	
	
	/// 데이터 밴드의 라벨밴드 관련 속성
	public boolean getUseGroupDataHeaderBand() {
		return mUseGroupDataHeaderBand;
	}

	public void setUseGroupDataHeaderBand(boolean value) {
		this.mUseGroupDataHeaderBand = value;
	}
	public void setUseGroupDataHeaderBand(String value) {
		if( value.equals("true")) value = "True";
		this.mUseGroupDataHeaderBand = Boolean.valueOf(value);
	}
	
	public String getUseHeaderBandGroupName() {
		return mUseHeaderBandGroupName;
	}

	public void setUseHeaderBandGroupName(String value) {
		this.mUseHeaderBandGroupName = value;
	}

	public int getHeaderVisibleDepth() {
		return mHeaderVisibleDepth;
	}
	
	public void setHeaderVisibleDepth(int value) {
		this.mHeaderVisibleDepth = value;
	}
	
	public void setHeaderVisibleDepth(String value) {
		this.mHeaderVisibleDepth = Integer.valueOf( value );
	}
		
	public void setLabelBandDisplayWidth(String value) {
		this.mLabelBandDisplayWidth = Float.valueOf( value );
	}
	
	public int getMinRowCount()
	{
		return mMinRowCount;
	}
	public void setMinRowCount( String value )
	{
		try {
			mMinRowCount = Integer.valueOf(value);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public void setMinRowCount( int value )
	{
			mMinRowCount = value;
	}
	
	
    public BandInfoMapData cloneBandInfo()
    {
    	BandInfoMapData retBandData = new BandInfoMapData();
    	
    	retBandData.setX( mX );
    	retBandData.setY( mY );
    	retBandData.setWidth( mWidth );
    	retBandData.setHeight( mHeight );
    	retBandData.setLimiteHeight( mLimiteHeight );
    	retBandData.setDefaultHeight(mDefaultHeight );
    	retBandData.setRowCount( mRowCount );
    	retBandData.setGroupDataIndex( mGroupDataIndex );
    	retBandData.setAdjustableHeight( mAdjustableHeight );
    	retBandData.setAutoHeight(mAutoHeight);
    	retBandData.setNewPage(mNewPage);
    	retBandData.setOriginalOrder(mOriginalOrder);
    	retBandData.setOrderFlag(mOrderByFlag);
    	
    	retBandData.setVisible(mVisible);
    	
    	if( mSequence != null ) retBandData.setSequence((ArrayList<String>) mSequence.clone());
    	if( mSort != null ) retBandData.setSort( mSort );
    	if( mFilter != null ) retBandData.setFilter(mFilter);
    	if( mColumnAr != null ) retBandData.setColumnAr((ArrayList<String>) mColumnAr.clone());
    	if( mFooterAr != null ) retBandData.setFooterAr((ArrayList<String>) mFooterAr.clone());
    	if( mOrderBy != null ) retBandData.setOrderBy((ArrayList<Boolean>) mOrderBy.clone());
    	
    	retBandData.setSubPage(mSubPage);
    	
    	if( mColumns != null ) retBandData.setColumns(mColumns);
    	if( mUbfx != null ) retBandData.setUbfx(mUbfx);
    	if( mRowCountList != null ) retBandData.setRowCountList(mRowCountList);
    	
    	retBandData.setClassName(mClassName);
    	retBandData.setSubPageID(mSubPageID);
    	retBandData.setRepeat(mRepeat);
    	retBandData.setGroupBand(mGroupBand);
    	retBandData.setHeaderBand(mHeaderBand);
    	retBandData.setDefaultBand(mDefaultBand);
    	retBandData.setDataSet(mDataSet);
    	retBandData.setGroupHeader(mGroupHeader);
    	retBandData.setGroupName(mGroupName);
    	retBandData.setSummery(msummery);
    	retBandData.setFooter(mFooter);
    	retBandData.setId(mId);
    	
    	retBandData.setResizeText(mResizeText);
    	
    	retBandData.setLabelBandColCount(mLabelBandColCount);
    	retBandData.setLabelBandDirection(mLabelBandDirection);
    	retBandData.setLabelBandGap(mLabelBandGap);
    	retBandData.setLabelBandPadding(mLabelBandPadding);
    	retBandData.setUseLabelBand(mUseLabelBand);
    	retBandData.setLabelBandDisplayWidth(mLabelBandDisplayWidth);
    	
    	retBandData.setMinRowCount(mMinRowCount);
    	
    	if( mHeightList != null ) retBandData.setHeightList((ArrayList<Float>) mHeightList.clone());
    	if( mItemRowHeight != null ) retBandData.setItemRowHeight((ArrayList<HashMap<String, Float>>) mItemRowHeight.clone());
    	if( children != null ) retBandData.setChildren(children);
    	
    	//TEST
    	retBandData.setUseGroupDataHeaderBand(mUseGroupDataHeaderBand);
    	
    	// 신규 속성 adjustableHeight지정시 margin값
    	retBandData.setAdjustableHeightMargin(mAdjustableHeightMargin);
    	
    	retBandData.setUbFunction(mUbFxList);
    	
    	retBandData.setAutoTableHeight(mAutoTableHeight);
    	
    	retBandData.setTableProperties(mTableProperties);
    	retBandData.setTableRowHeight(mTableRowHeight);

    	retBandData.setTableBandY(mTableBandY);
    	
    	retBandData.setUseDataHeaderBand(mUseDataHeaderBand);
    	
    	retBandData.setAutoVerticalPosition(mAutoVerticalPosition);
    	
    	retBandData.setRequiredItems((ArrayList<String>) mRequiredItems);
    	retBandData.setTabIndexItem((ArrayList<String>) mTabIndexItem);
    	retBandData.setFileLoadType(mFileLoadType);
    	retBandData.setOriginalItemData(mOriginalItemData);
    	
    	retBandData.setColumnAr(mColumnAr);
    	retBandData.setColumns(mColumns);
    	retBandData.setColumnsAR(mColumnsAr);
    	retBandData.setFooterAr(mFooterAr);
    	retBandData.setFooter(mFooter);
    	
    	return retBandData;
    }

	public float getLabelBandDisplayWidth() {
		return mLabelBandDisplayWidth;
	}

	public void setLabelBandDisplayWidth(float labelBandDisplayWidth) {
		this.mLabelBandDisplayWidth = labelBandDisplayWidth;
	}
	
	
	public ArrayList<String> getRequiredItems() {
		return mRequiredItems;
	}

	public void setRequiredItems(ArrayList<String> mRequiredItems) {
		this.mRequiredItems = mRequiredItems;
	}
	
	public void setRequiredItemAt( int _index, String _id )
	{
		while( this.mRequiredItems.size() <= _index )
		{
			 this.mRequiredItems.add("");
		}
		
		this.mRequiredItems.set(_index, _id);
	}
	
	public ArrayList<String> getTabIndexItem() {
		return mTabIndexItem;
	}

	public void setTabIndexItem(ArrayList<String> mTabIndexItem) {
		this.mTabIndexItem = mTabIndexItem;
	}
	
	public void setTabIndexItemAt( int _index, String _id )
	{
		while( this.mTabIndexItem.size() <= _index )
		{
			 this.mTabIndexItem.add("");
		}
		
		this.mTabIndexItem.set(_index, _id);
	}
	
	public float getAdjustableHeightMargin() {
		return mAdjustableHeightMargin;
	}

	public void setAdjustableHeightMargin(float _adjustableHeightMargin) {
		mAdjustableHeightMargin = _adjustableHeightMargin;
	}

	public void setAdjustableHeightMargin(String value) {
		try {
			mAdjustableHeightMargin = Float.valueOf(value);
		} catch (Exception e) {
			// TODO: handle exception
			mAdjustableHeightMargin = -1;
		}
	}
	
	public boolean getVisible()
	{
		return mVisible;
	}
	public void setVisible( String _value )
	{
		mVisible = !_value.equals("false");
	}
	public void setVisible( boolean _value )
	{
		mVisible = _value;
	}
	
	public ArrayList<HashMap<String, String>> getUbFunction()
	{
		return mUbFxList;
	}

	public void setUbFunction(ArrayList<HashMap<String, String>> _fx)
	{
		mUbFxList = _fx;
	}
	
	public void setOriginalItemData( HashMap<String, Object> _value )
	{
		mOriginalItemData = _value;
	}
	public HashMap<String, Object> getOriginalItemData()
	{
		return mOriginalItemData;
	}

	
	public ArrayList<HashMap<String, ArrayList<Float>>> getTableRowHeight() {
		return mTableRowHeight;
	}

	public void setTableRowHeight(ArrayList<HashMap<String, ArrayList<Float>>> mTableRowHeight) {
		this.mTableRowHeight = mTableRowHeight;
	}
	
	public ArrayList<HashMap<String, Value>> getTableProperties() {
		return mTableProperties;
	}

	public void setTableProperties(ArrayList<HashMap<String, Value>> mTableProperties) {
		this.mTableProperties = mTableProperties;
	}
	
	public void setTableProperty(HashMap<String, Value> mTableProperty) {
		if(this.mTableProperties == null ){
			this.mTableProperties = new ArrayList<HashMap<String, Value>>();
		}
		this.mTableProperties.add(mTableProperty);
		
		if(this.mTableBandY == null){
			this.mTableBandY = new HashMap<String, Float>();
		}
		this.mTableBandY.put(mTableProperty.get("tableId").getStringValue(), mTableProperty.get("band_y").getIntegerValue());
		
	}
	
	public void setFileLoadType(String _value )
	{
		mFileLoadType = _value;
	}
	public String getFileLoadType()
	{
		return mFileLoadType;
	}
	
	public void setGroupLength( int _value )
	{
		mGroupLength = _value;
	}
	public int getGroupLength()
	{
		return mGroupLength;
	}
	
	public void setVisibleType(String _value)
	{
		mVisibleType = _value;
	}
	
	public String getVisibleType()
	{
		return mVisibleType;
	}
	
	public void setGroupStartPageIdx( int _value )
	{
		mGroupStartPageIdx = _value;
	}
	public int getGroupStartPageIdx()
	{
		return mGroupStartPageIdx;
	}
	public void setGroupTotalPageCnt( int _value )
	{
		mGroupTotalPageCnt = _value;
	}
	
	public int getGroupTotalPageCnt()
	{
		return mGroupTotalPageCnt;
	}
	
	/// 데이터 밴드의 라벨밴드 관련 속성
	public boolean getUseDataHeaderBand() {
		return mUseDataHeaderBand;
	}

	public void setUseDataHeaderBand(boolean value) {
		this.mUseDataHeaderBand = value;
	}
	public void setUseDataHeaderBand(String value) {
		if( value.equals("true")) value = "True";
		this.mUseDataHeaderBand = Boolean.valueOf(value);
	}
	
	
}
