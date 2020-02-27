package org.ubstorm.service.parser.formparser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;
import javax.xml.xpath.XPathExpressionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.TempletItemInfo;
import org.ubstorm.service.parser.formparser.data.Value;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RepeatedFormParser {
	
	private String DATA_TABLE_TYPE = "1";
	private String NORMAL_TABLE_TYPE = "0";
	
	
	private UDMParamSet m_appParams = null;
	private String mChartDataFileName = "chartdata.dat";
	
	private HashMap<String,String> mImageData;
	private HashMap<String,Object> mChartData;
	private Function mFunction;
	private int mMinimumResizeFontSize = 0;	// resizeFont 사용시 최소값 지정
	

	// Export여부를 판단하기 위한 변수
	private String isExportType = "";
	private String isExcelOption = "";
	
	// crossTab의 max Width값을 담기 위한 객체
	private float mBandMaxWidth = 0;
	
	private float mPageMarginTop = 0;
	private float mPageMarginLeft = 0;
	private float mPageMarginRight = 0;
	private float mPageMarginBottom = 0;
	private HashMap<String, TempletItemInfo> mTempletInfo;
	
	protected HashMap<String, Object> changeItemList;
	
	ArrayList<String> OriginalRequiredItemList = new ArrayList<String>();	// 페이지의 필수 값을 가진 객체의 ID를 담아둔 배열
	ArrayList<String> OriginalTabIndexItemList = new ArrayList<String>();	// 페이지별 탭 인덱스를 가진 객체의 ID를 담아둔 배열
	ArrayList<Integer> _pageXArr;
	
	public void setChangeItemList( HashMap<String, Object> _value)
	{
		changeItemList = _value;
	}
	
	public void setPageMarginTop( float _marginTop )
	{
		mPageMarginTop = _marginTop;
	}
	public void setPageMarginLeft( float _marginLeft )
	{
		mPageMarginLeft = _marginLeft;
	}
	public void setPageMarginRight( float _marginRight )
	{
		mPageMarginRight = _marginRight;
	}
	public void setPageMarginBottom( float _marginBottom )
	{
		mPageMarginBottom = _marginBottom;
	}
	
	boolean isExportData = false;
	
	public boolean isExportData() {
		return isExportData;
	}
	
	public void setExportData(boolean isExportData) {
		this.isExportData = isExportData;
	}
	
	public void setIsExcelOption( String _option )
	{
		if(_option != null ) isExcelOption = _option;
	}
	public String getIsExcelOption()
	{
		if( isExcelOption == null ) isExcelOption = "";
		return isExcelOption;
	}
	public void setTempletInfo( HashMap<String, TempletItemInfo> _templetInfo )
	{
		mTempletInfo = _templetInfo;
	}
	public HashMap<String, TempletItemInfo> getTempletInfo()
	{
		return mTempletInfo;
	}
	
	public RepeatedFormParser() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public RepeatedFormParser(UDMParamSet appParams) {
		super();
		// TODO Auto-generated constructor stub
		this.m_appParams = appParams;
	}
	
	public void setChartDataFileName(String file_name)
	{
		this.mChartDataFileName = file_name;
	}
	
	public void setImageData( HashMap<String,String> _imageData )
	{
		this.mImageData = _imageData;
	}
	public void setFunction( Function _function )
	{
		this.mFunction = _function;
	}
	
	public Function getFunction()
	{
		return this.mFunction;
	}
	
	public String getIsExportType() {
		return isExportType;
	}

	public void setIsExportType(String isExportType) {
		this.isExportType = isExportType;
	}
	
	public int getMinimumResizeFontSize() {
		return mMinimumResizeFontSize;
	}

	public void setMinimumResizeFontSize(int mMinimumResizeFontSize) {
		this.mMinimumResizeFontSize = mMinimumResizeFontSize;
	}
	
	public void setChartData( HashMap<String,Object> _chartData )
	{
		this.mChartData = _chartData;
	}
	
	public float getBandMaxWidth() {
		return mBandMaxWidth;
	}

	public void setBandMaxWidth(float mBandMaxWidth) {
		this.mBandMaxWidth = mBandMaxWidth;
	}
	
	private float mPageHeight = 0f;
	private float mPageWidth = 0f;
	
	// FreeForm 타입의 아이템을 담아둘 객체
	private ArrayList<BandInfoMapData> mPageBandList = new ArrayList<BandInfoMapData>();
	// Band타입의 아이템을 담아두는 객체
	private ArrayList<BandInfoMapData> mDataBandList = new ArrayList<BandInfoMapData>();
	// page Header / Footer 객체 
	private ArrayList<BandInfoMapData> mEmptyBandList = new ArrayList<BandInfoMapData>();
	
	
	//Table의 Type값이 1일때 Row중에서 H : headerBand로 D : DataBand F:FooterBand를 
	// 생성하고 이후 테이블을 각 Row로 끊어서 각 밴드의 children에 담는다 
	// 전체 아이템을 돌면서 FreeForm Type아이템리스트에 담는다 
	// Table일때만 tableType을 찾아서 1일경우 BandForm Type아이템 목록에 담기
	HashMap<String, ArrayList<HashMap<String, Object>>> DataSet;
	ContinueBandParser continueBandParser = null;
	
	public HashMap<String, Object> loadTotalPage( Element _page, HashMap<String, ArrayList<HashMap<String, Object>>> _data,float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr , HashMap<String, Object> _param  ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		
		int i = 0;
		int j = 0;
		int k = 0;
		HashMap< String, HashMap<String, String>> bandInfo = new HashMap< String, HashMap<String, String>>();
		HashMap<String, Integer> _groupBandCntMap = null;			// 밴드의 총 그룹갯수를 담아두는 객체
		
		NodeList _child = _page.getElementsByTagName("item");
		Element item = null;
		String _className = "";
		HashMap<String, ArrayList<Element>> bandItems = new HashMap<String, ArrayList<Element>>();
		Element bandProperty = null;
		String _bandName = "";
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		NodeList _propertys;
		Element _propertyElement;
		HashMap<String, String> propertyMap;
		HashMap<String, Element> bandElementData = new HashMap<String, Element>();
		continueBandParser = new ContinueBandParser(m_appParams);
		
		continueBandParser.setExportData(isExportData);
		continueBandParser.setImageData(this.mImageData);
		continueBandParser.setChartData(this.mChartData);
		continueBandParser.setFunction(this.mFunction);
		continueBandParser.setIsExportType(this.isExportType );
		continueBandParser.setTempletInfo(mTempletInfo);
		
		continueBandParser.DataSet = _data;
		
		ArrayList<BandInfoMapData> bandList = new ArrayList<BandInfoMapData>();
		HashMap<String, BandInfoMapData> bandInfoData = new HashMap<String, BandInfoMapData>();
		
		String cloneData = _page.getAttribute("clone");
		int _cloneRowCnt = 1;
		boolean clonePage = false;
		float _pageWidth = Float.valueOf(_page.getAttribute("width"));
		float _pageHeight = Float.valueOf(_page.getAttribute("height"));
		if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL)  || cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM))
		{
			if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
			{
				_cloneRowCnt = 2;
			}
			else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) )
			{
				_cloneRowCnt = 1;
			}
			else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM) )
			{
				_cloneRowCnt = Integer.parseInt(_page.getAttribute("cloneRowCount"));
			}
			
			_pageHeight = _pageHeight / _cloneRowCnt;
			clonePage = true;
		}
		
		//empty 밴드를 미리 생성해둠 ( clone일때 밴드의 사이즈 변경 필요 기본값은 페이지 전체 사이즈로 생성 ( freeForm타입을 위한 아이템만 담을 예정 ) ) 
		BandInfoMapData  _emptyBand = new BandInfoMapData();
		_emptyBand.setClassName(BandInfoMapData.EMPTY_BAND);
		_emptyBand.setWidth( _pageWidth );
		_emptyBand.setHeight( _pageHeight );
		_emptyBand.setId("EMPTY_BAND");
		
		mEmptyBandList.add( _emptyBand );
		
		// 생성해둔 emptyBand를 bandInfo에 담기
		bandInfoData.put(_emptyBand.getId(), _emptyBand);
		
		DataSet = _data;	
		int _propertyLength = 0;
		int _childLength = 0;
		
		HashMap<String, Value> itemProperty;
		Element _ItemProperty;
		String _propertyName;
		String _propertyValue;
		String _propertyType;
		
		_childLength = _child.getLength();
		float _pageHeaderBandHeight = 0;
		float _pageFooterBandHeight = 0;
		float _continueBandStartPosition = 0;
		float _emptyYPosition = 0;				// _empty밴드별로 y위치를 담기
		float _lastEmptyItemPos = 0;			// pageBand에 포함되지 않는 마지막 Item의 Y+height ( 마지막 Empty Band의 height값을 지정하기 위한 변수 )
		
		ArrayList<Float> _bandYPoistionAr = new ArrayList<Float>();
		ArrayList<String> _emptyBandNames = new ArrayList<String>();
		String _emptyBandID = "EMPTY_";
		BandInfoMapData _emptyBandItem = new BandInfoMapData();
		_emptyBandItem.setId(_emptyBandID + _emptyBandNames.size() );
		_emptyBandItem.setHeight(_pageHeight -  mPageMarginTop);
		_emptyBandItem.setClassName(BandInfoMapData.EMPTY_BAND);
		
		_emptyBandNames.add(_emptyBandID + _emptyBandNames.size());
		_bandYPoistionAr.add(0f);
		
		bandInfoData.put(_emptyBandItem.getId(), _emptyBandItem);
		
		
		//Element를 그룹핑처리
		for ( i = 0; i < _childLength; i++) {
			
			item = (Element) _child.item(i);
			String _itemId = item.getAttribute("id");
			_className = item.getAttribute("className");
			
			// PAGE HEADER BAND 와 PAGE FOOTER BAND의 정보를 담다둔다.
			if( _className.length() > 4 && _className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) 
			{
				
				BandInfoMapData  bandData = new BandInfoMapData(item);
				//bandList.add(bandData);
				
				if( _className.equals(BandInfoMapData.PAGE_HEADER_BAND))
				{
					_pageHeaderBandHeight = _pageHeaderBandHeight + bandData.getHeight();
					
					_bandYPoistionAr.set(0, _pageHeaderBandHeight);
					
					_emptyBandItem.setY(_pageHeaderBandHeight+ mPageMarginTop);
				}
				else if( _className.equals(BandInfoMapData.PAGE_FOOTER_BAND) )
				{
					_pageFooterBandHeight = _pageFooterBandHeight + bandData.getHeight();
				}
				
				bandInfoData.put(bandData.getId(), bandData);
				mPageBandList.add( bandData );
				
				//emptyBand의 height 변경
				_emptyBandItem.setHeight( _emptyBandItem.getHeight() - bandData.getHeight() );
			}
			else
			{
				//property에 band가 있을경우 bandInfo의 children에 담기 
				//band가 없을경우 
				
				// TABLE의 TYPE가 1일때는 데이터 밴드로 그 외에는 일반 밴드로 지정하여 FREEFORM형태로 구현
				if( _className.equals("UBTable") )
				{
					
					//properties 담기
					propertyMap = new HashMap<String, String>();
					
					_propertys = item.getElementsByTagName("property");
					_propertyLength = _propertys.getLength();
					for ( j = 0; j < _propertyLength; j++) {
						
						_propertyElement = (Element) _propertys.item(j);
						
						if( _propertyElement.getParentNode().getNodeName().equals("item"))
						{
							propertyMap.put(_propertyElement.getAttribute("name"), URLDecoder.decode( _propertyElement.getAttribute("value"), "UTF-8" ) );
						}
					}
					
					//Type를 체크( H / D / F ) 
					//Table의 id를 각 구분별로 담기 type ( H_,D_,F_ )  + TableID로 테이블 아이디를 구성 
					
					ArrayList<String> _sequence = new ArrayList<String>();
					_sequence.add("d");
					String _bandID = "";
					if(propertyMap.containsKey("tableType") && propertyMap.get("tableType").equals(DATA_TABLE_TYPE) && propertyMap.containsKey("rowTypeInfo"))
					{
						if( _emptyBandItem != null )
						{
							_emptyBandItem.setHeight( _emptyBandItem.getHeight() - (_pageHeight - Float.valueOf( propertyMap.get("y") ) - _pageFooterBandHeight ) );
							mDataBandList.add(_emptyBandItem);
							
							bandList.add(_emptyBandItem);
						}
						
						float _tblYPosition = Float.valueOf( propertyMap.get("y"));
						if( _continueBandStartPosition == 0 || _continueBandStartPosition > _tblYPosition )
						{
							_continueBandStartPosition = _tblYPosition;
						}
						
						int _rowTypeInfoCnt = 0;
						JSONArray _rowTypeInfoAr = (JSONArray) JSONValue.parse(propertyMap.get("rowTypeInfo"));
						
						_rowTypeInfoCnt = _rowTypeInfoAr.size();
						String _argoBandName = "";
						
						String _headerBandName = "";
						
						for( j = 0; j < _rowTypeInfoCnt; j++  )
						{
							BandInfoMapData  bandData = new BandInfoMapData();
							
							_bandID = _rowTypeInfoAr.get(j) + "_" + _itemId;
							
							if( _rowTypeInfoAr.get(j).equals("H"))
							{
								bandData.setClassName(BandInfoMapData.DATA_HEADER_BAND);
								_headerBandName = _bandID;
							}
							else if( _rowTypeInfoAr.get(j).equals("D") )
							{
								bandData.setClassName(BandInfoMapData.DATA_BAND);
								bandData.setSequence(_sequence);
								
								if( _headerBandName.equals("") == false	)
								{
									bandInfoData.get(_headerBandName).setDataBand(_bandID);
									bandData.setHeaderBand(_headerBandName);
								}
							}
							else if( _rowTypeInfoAr.get(j).equals("F") )
							{
								bandData.setClassName(BandInfoMapData.DATA_FOOTER_BAND);
							}
							
							if(_argoBandName.equals(_bandID) == false)
							{
								//band 정보를 지정
								bandData.setId(_bandID);
								bandInfoData.put(bandData.getId(), bandData);	// 생성된 밴드 데이터를 ID를 Key값으로 하여 맵에 담아두기
								bandList.add(bandData);
								
								mDataBandList.add( bandData );
							}
							_argoBandName = _bandID;
						}
						
						// 이전 empty밴드의 height를 지정하고 신규 empty밴드를 생성한다.
						if( _emptyBandItem != null )
						{
							_emptyYPosition = Float.valueOf( propertyMap.get("y") ) + Float.valueOf( propertyMap.get("height") );
							
							_emptyBandItem = new BandInfoMapData();
							_emptyBandItem.setClassName(BandInfoMapData.EMPTY_BAND);
							_emptyBandItem.setId(_emptyBandID + _emptyBandNames.size() );
							_emptyBandItem.setHeight(_pageHeight - _emptyYPosition - _pageFooterBandHeight  );
							_emptyBandNames.add(_emptyBandID + _emptyBandNames.size());
							_bandYPoistionAr.add(_emptyYPosition);
							_emptyBandItem.setY(_emptyYPosition);
							
							bandInfoData.put(_emptyBandItem.getId(), _emptyBandItem);
						}
					}
					else
					{
						if( Float.valueOf( propertyMap.get("y") ) > _pageHeaderBandHeight && Float.valueOf( propertyMap.get("y"))  < (_pageHeight-_pageFooterBandHeight) && Float.valueOf( propertyMap.get("y") ) + Float.valueOf( propertyMap.get("height") ) > _lastEmptyItemPos )
						{
							_lastEmptyItemPos =  Float.valueOf( propertyMap.get("y") ) + Float.valueOf( propertyMap.get("height") );
						}
					}
					
					//type별로 테이블을 구분하여 생성 
					//임의로 생성된 각 Header/Data/Footer밴드에 테이블 아이템을 Add 및 band의 height 값 지정
					convertElementToITem(item, bandInfoData, _data, _param );
					
				}
				else
				{
					// EMPTY 밴드를 담기 위해서는 데이터 테이블의 시작/마지막 위치를 가지고 있어야 함
					// EMPTY 밴드를 CONTINUEBAND 타입으로 처리하는 옵션을 위해서 아이템을 
					
					itemProperty = new HashMap<String, Value>();
					itemProperty.put("Element", new Value(item,"element") );
					_propertys = (NodeList) item.getElementsByTagName("property");
					String _itemDataSetName = "";
					
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
					
					if( itemProperty.get("y").getIntegerValue() > _pageHeaderBandHeight+ mPageMarginTop && itemProperty.get("y").getIntegerValue()  < (_pageHeight-_pageFooterBandHeight) && itemProperty.get("y").getIntegerValue() + itemProperty.get("height").getIntegerValue() > _lastEmptyItemPos )
					{
						_lastEmptyItemPos =  itemProperty.get("y").getIntegerValue() + itemProperty.get("height").getIntegerValue();
						
						float _itemYpos = Float.valueOf( itemProperty.get("y").getIntegerValue() );
						if( _continueBandStartPosition == 0 || _continueBandStartPosition > _itemYpos )
						{
							_continueBandStartPosition = _itemYpos;
						}
						
					}
					
					//아이템의 LineHeight값을 수정 120% => 1.2로 변경
					if( itemProperty.get("lineHeight") != null && itemProperty.get("lineHeight").getStringValue().equals("") == false  )
					{
						String _value = itemProperty.get("lineHeight").getStringValue();
						_value = _value.toString().replace("%25", "").replace("%", "");
						_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
						
						itemProperty.put("lineHeight",  new Value( Float.valueOf( _value ), "number") );
					}
					
					itemProperty.put(  "id", new Value( item.getAttribute("id"), "string"));
					itemProperty.put(  "className", new Value(_className, "string"));
					
					// Empty Band의 아이디를 맵핑해준다. 
					//itemProperty.put("band", Value.fromString("EMPTY_BAND"));
					
					if( itemProperty.get("band").getStringValue().equals("") || bandInfoData.containsKey( itemProperty.get("band").getStringValue() ) == false )
					{
						itemProperty.put("band", Value.fromString("EMPTY_BAND"));
					}
					
					if( itemProperty.containsKey("dataType") && itemProperty.get("dataType").getStringValue().equals("") == false  &&  !itemProperty.get("dataType").getStringValue().equals("0") &&
							itemProperty.get("dataSet").getStringValue().equals("") == false  )
					{
						_itemDataSetName = itemProperty.get("dataSet").getStringValue();
					}
					
					if( _itemDataSetName.equals("") == false  && _data.containsKey(_itemDataSetName) && _data.get(_itemDataSetName) != null && itemProperty.containsKey("band") && bandInfoData.containsKey(itemProperty.get("band").getStringValue()) )
					{
						if( bandInfoData.get( itemProperty.get("band").getStringValue()).getDataSet().equals("") || _data.get( bandInfoData.get( itemProperty.get("band").getStringValue()).getDataSet() ).size() < _data.get(_itemDataSetName).size() )
						{
							bandInfoData.get(itemProperty.get("band").getStringValue()).setDataSet(_itemDataSetName); 
							bandInfoData.get(itemProperty.get("band").getStringValue()).setRowCount(_data.get(_itemDataSetName).size());
						}
					}
					
					
					if(itemProperty.containsKey("band") && itemProperty.get("band").getStringValue().equals("") == false  && itemProperty.get("band").getStringValue().equals("null") == false && bandInfoData.containsKey(itemProperty.get("band").getStringValue()) )
					{
						// RadioButtonGroup일경우 라디오버튼보다 먼저 생성 되어야 이후 라디오버튼에 값을 지정할수 있다.
						if( _className.equals("UBRadioButtonGroup") )
						{
							bandInfoData.get(itemProperty.get("band").getStringValue()).getChildren().add(0, itemProperty);
						}
						else
						{
							bandInfoData.get(itemProperty.get("band").getStringValue()).getChildren().add(itemProperty);
						}
					}
					
					
//					int _requiredItemIndex = OriginalRequiredItemList.indexOf(itemProperty.get("id").getStringValue());
//					int _tabIndexItemIndex = OriginalTabIndexItemList.indexOf(itemProperty.get("id").getStringValue());
//					
//					if( _requiredItemIndex != -1 )
//					{
//						bandInfoData.get(itemProperty.get("band").getStringValue()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").getStringValue() );
//					}
//					
//					if( _tabIndexItemIndex != -1 )
//					{
//						bandInfoData.get(itemProperty.get("band").getStringValue()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").getStringValue() );
//					}
					
					//TEMPLET 처리
					if( _className.equals("UBTemplateArea") )
					{
						//Templet일경우 Band의 Height가 UBTempletArea보다 작을 경우 Band의 사이즈 업데이트
						if( itemProperty.get("autoHeight").getBooleanValue() && mTempletInfo != null && mTempletInfo.containsKey(itemProperty.get("id").getStringValue() ) )
						{
							float _templetHeight = mTempletInfo.get( itemProperty.get("id").getStringValue() ).getHeight() + itemProperty.get("band_y").getIntegerValue();
							if( bandInfoData.get(itemProperty.get("band").getStringValue()) != null && _templetHeight > bandInfoData.get(itemProperty.get("band").getStringValue()).getHeight() )
							{
								bandInfoData.get(itemProperty.get("band").getStringValue()).setHeight(_templetHeight);
							}
						}
						bandInfoData.get(itemProperty.get("band").getStringValue()).getChildren().add(itemProperty);
					}
					
				}
				// EMPTY BAND형태로 지정 
				
			}
			
		}
		
		_continueBandStartPosition = _pageHeaderBandHeight + mPageMarginTop;
		
		
		// 마지막 Empty밴드를 추가한다.
		if( _emptyBandItem != null && mDataBandList.contains(_emptyBandItem) == false)
		{
			mDataBandList.add(_emptyBandItem);
			bandList.add(_emptyBandItem);
		}
		
		if( bandList.get( bandList.size() - 1).getClassName().equals(BandInfoMapData.EMPTY_BAND) )
		{
			bandList.get( bandList.size() - 1).setHeight( _lastEmptyItemPos - bandList.get( bandList.size() - 1).getY() );
		}
		
		_bandYPoistionAr.add( _pageHeight - _pageFooterBandHeight - mPageMarginTop );
		
		//EMPTY 밴드의 아이템들을 반복하여 각각의 continueBand의  Empty밴드로 이동처리 
		if( bandInfoData.containsKey("EMPTY_BAND"))
		{
			ArrayList<HashMap<String, Value>> _childrens = bandInfoData.get("EMPTY_BAND").getChildren();
			float _yPos = 0;
			BandInfoMapData _empBand = null;
			float _bandH = mPageMarginTop;
			
			for( i = 0; i < _childrens.size(); i++ )
			{
				HashMap<String, Value> _item = _childrens.get(i);
				_yPos = _item.get("y").getIntegerValue();
				_empBand = getCurrentEmptyBand(bandInfoData, _bandYPoistionAr, _emptyBandNames, _yPos );
				
				if( _empBand != null )
				{
					_empBand.getChildren().add(_item);
					_item.put("band", Value.fromString( _empBand.getId() ) );
					_item.put("band_y",Value.fromInteger( _item.get("y").getIntegerValue() - _empBand.getY() ) );
				}
				else if( _pageHeaderBandHeight+mPageMarginTop > _yPos || _pageHeight - _pageFooterBandHeight - mPageMarginTop  < _yPos )
				{
					for( j = 0; j < mPageBandList.size(); j++ )
					{
						if( mPageBandList.get(j).getClassName().equals(BandInfoMapData.PAGE_HEADER_BAND) )
						{
							if( _yPos < _bandH + mPageBandList.get(j).getHeight() )
							{
								mPageBandList.get(j).getChildren().add(_item);
								break;
							}
							_bandH = _bandH + mPageBandList.get(j).getHeight();
						}
						else if( mPageBandList.get(j).getClassName().equals(BandInfoMapData.PAGE_FOOTER_BAND) )
						{
							if( _yPos > _pageHeight - _pageFooterBandHeight - mPageMarginBottom )
							{
								mPageBandList.get(j).getChildren().add(_item);
								break;
							}
						}
					}
				}
				
				
			}
		}
		
		
		int _totalPageSize = 1;
		
		HashMap<String, String> originalDataMap = new HashMap<String, String>();
		float _maxHeight = _pageHeight; // 밴드가 반복될 영역의 height값 
		float _startY = 0;		// 밴드의 시작 Y값 
		
		_maxHeight = _maxHeight - _continueBandStartPosition -_pageFooterBandHeight - mPageMarginBottom;
		
		/// BandForm의 총 페이지를 구하기
		mFunction.setGroupBandCntMap(_groupBandCntMap);
//		mFunction.setOriginalDataMap(originalDataMap);
		
		ArrayList<Object> rowHeightListData;
		if( isExcelOption.equals("BAND")) rowHeightListData =  continueBandParser.makeRowHeightListExcel( bandList, bandInfoData, _startY, defaultY, -1, _pageWidth, mXAr, originalDataMap, _maxHeight );
		else rowHeightListData =  continueBandParser.makeRowHeightList( bandList, bandInfoData, _startY, defaultY, _maxHeight, _pageWidth, mXAr, originalDataMap);
		
		ArrayList<ArrayList<HashMap<String, Value>>> pagesCountList = (ArrayList<ArrayList<HashMap<String, Value>>>) rowHeightListData.get(0);
		HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) rowHeightListData.get(1);
		
		
		// 밴드별 row 를 이용하여 페이지별 아이템을 생성하여 리턴
		
		ArrayList<Object> _retAr = new ArrayList<Object>();
		
		_retAr.add(bandInfoData);		
		_retAr.add(bandList);			
		_retAr.add(pagesCountList);		
		_retAr.add(crossTabData);			// 크로스탭정보가 담긴 배열
		_retAr.add(originalDataMap);		// OrizinalDataSet정보를 담은 map
		_retAr.add(null);					// 각 그룹별 그룹핑된 데이터리스트 [ [ string, string ] ] 형태

		
		_retAr.add(rowHeightListData.get(2));		// 필수목록 페이지별 리스트
		_retAr.add(rowHeightListData.get(3));		// 탭인덱스 페이지별 리스트

		
		_retAr.add(_groupBandCntMap);				//  그룹밴드별 총 그룹핑 갯수가 담긴 객체  { 밴드 ID : 그룹수, 밴드 ID2:......  }
		
		_totalPageSize = pagesCountList.size();
		
		_pageXArr = mXAr;
		//Band폼의 총 페이징 구하기 완료 
		
		// 총 페이지수는 pagesCountList 의 사이즈를 기본으로 ./,l 
		
		//return 객체 생성
		HashMap<String, Object> _resultMap = new HashMap<String, Object>();
		
		_resultMap.put( "CONTINUE_BAND_AR", _retAr);
		_resultMap.put( "PAGE_BAND_INFO", mPageBandList);
		_resultMap.put( "EMPTY_BAND_INFO", mEmptyBandList);
		_resultMap.put( "DATA_BAND_INFO", mDataBandList);
		_resultMap.put("TOTAL_PAGE", _totalPageSize );
		_resultMap.put("BAND_INFO", bandInfoData );
		
		// continueBand 아이템의 시작 Y값과 Height값을 담아두기
		_resultMap.put("PAGE_HEADER_HEIGHT", _pageHeaderBandHeight );
		_resultMap.put("PAGE_FOOTER_HEIGHT", _pageFooterBandHeight );
		_resultMap.put("CONTINUE_BAND_Y_POSITION", _continueBandStartPosition );
		
		return _resultMap;
	}
	
	
	private BandInfoMapData getCurrentEmptyBand( HashMap<String, BandInfoMapData> bandInfoData, ArrayList<Float> _bandYPos, ArrayList<String> _bandNMs, float _itemY )
	{
		float _currentY = 0;
		float _maxY = 0;
		BandInfoMapData _retBandInfo = null;
		
		for( int i =0; i < _bandYPos.size(); i++ )
		{
			_currentY = _bandYPos.get(i);
			_maxY = (i == _bandYPos.size() -1 )?_currentY:_bandYPos.get(i+1);
			
			if( _itemY >= _currentY && _itemY < _maxY && bandInfoData.containsKey(_bandNMs.get(i)) )
			{
				_retBandInfo = bandInfoData.get(_bandNMs.get(i));
				break;
			}
		}
		
		return _retBandInfo;
	}

	
	public ArrayList<HashMap<String, Object>> createRepeatFormItems(int _page, HashMap<String, Object> _bandInfoMap, float pageWidth, float pageHeight, HashMap<String, Object> _param, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		
		ArrayList<BandInfoMapData> _pageBandList = (ArrayList<BandInfoMapData>) _bandInfoMap.get("PAGE_BAND_INFO");
		ArrayList<BandInfoMapData> _emptyBandList = (ArrayList<BandInfoMapData>) _bandInfoMap.get("EMPTY_BAND_INFO");
		ArrayList<BandInfoMapData> _dataBandList = (ArrayList<BandInfoMapData>) _bandInfoMap.get("DATA_BAND_INFO");
		ArrayList<Object> _retAr = (ArrayList<Object>)  _bandInfoMap.get("DATA_BAND_INFO");
		HashMap<String, BandInfoMapData> bandInfoData = (HashMap<String, BandInfoMapData>) _bandInfoMap.get("BAND_INFO");
		ArrayList<Object> _continueBandAr = (ArrayList<Object>)  _bandInfoMap.get("CONTINUE_BAND_AR");
		
		float _pageHeaderHeight = Float.valueOf(_bandInfoMap.get("PAGE_HEADER_HEIGHT").toString());
		float _pageFooterHeight = Float.valueOf(_bandInfoMap.get("PAGE_FOOTER_HEIGHT").toString());
		float _continueBandStartPosition = Float.valueOf(_bandInfoMap.get("CONTINUE_BAND_Y_POSITION").toString());
		
		ItemConvertParser dataItemParser = new ItemConvertParser(DataSet, "", m_appParams);
		dataItemParser.setFunction( mFunction );
		
		ArrayList<HashMap<String, Value>> _items;
		
		int i = 0;
		int _cnt = 0;
		int j = 0;
		int _itemCnt = 0;
		HashMap<String, Object> _item;
		
		//1.  page Header Band의 아이템 부터 생성
		if( _pageBandList != null)
		{
			_cnt = _pageBandList.size();
			for( i=0; i < _cnt; i++ )
			{
				_items = _pageBandList.get(i).getChildren();
				
				if( _items != null && _items.size() > 0 )
				{
					_itemCnt =  _items.size();
					
					for (j = 0; j < _itemCnt; j++) {
						_item = dataItemParser.convertItemData( _pageBandList.get(i) , _items.get(j), "", 0, _param, -1, -1, _totalPageNum, _currentPageNum);
						_objects.add(_item);
					}
					
				}
			}
		}
		
		//2. Empty Band 아이템 생성
		if( _emptyBandList != null && false )
		{
			_cnt = _emptyBandList.size();
			for( i=0; i < _cnt; i++ )
			{
				
				_items = _emptyBandList.get(i).getChildren();
				
				if( _items != null && _items.size() > 0 )
				{
					_itemCnt =  _items.size();
					
					for (j = 0; j < _itemCnt; j++) {
						_item = dataItemParser.convertItemData( _emptyBandList.get(i) , _items.get(j), "", 0, _param, -1, -1, _totalPageNum, _currentPageNum);
						_objects.add(_item);
					}
					
				}
			}
		}
		
		//3. DataBand 아이템 생성 
		if( _dataBandList != null)
		{
			HashMap<String, BandInfoMapData> bandInfo = (HashMap<String, BandInfoMapData>) _continueBandAr.get(0);
			ArrayList<BandInfoMapData> bandList =(ArrayList<BandInfoMapData>) _continueBandAr.get(1);
			ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) _continueBandAr.get(2);
			HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) _continueBandAr.get(3);
			// group
			HashMap<String, String> originalDataMap = (HashMap<String, String>) _continueBandAr.get(4);		// originalData값으 가지고 있는 객체
			ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) _continueBandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
			
			_cloneY = _cloneY + _pageHeaderHeight + mPageMarginTop;
			
			// continueBand 아이템 처리
			_objects = continueBandParser.createContinueBandItems(_page, DataSet, bandInfo, bandList, pagesRowList, _param, crossTabData,_cloneX, _cloneY,_objects,_totalPageNum, _currentPageNum , false);
		}

		
		
		return _objects;
	}
	
	
	
	
	
	
	
	private void convertElementToITem( Element _childItem, HashMap<String, BandInfoMapData> bandInfoData,  HashMap<String, ArrayList<HashMap<String, Object>>> _data, HashMap<String, Object> _param ) throws UnsupportedEncodingException, ScriptException
	{
		
		String _itemId  = "";
		String _className  = "";
		NodeList _tablePropertys;
		Element _ItemProperty;
		int  l = 0;
		int  k = 0;
		int  j = 0;
		NodeList _tableMaps;
		Element _tableMapItem;
		NodeList _tableMapDatas;
		NodeList _cellPropertys;
		HashMap<String, Value> itemProperty = new HashMap<String, Value>();
		HashMap<String, Value> tableMapProperty = new HashMap<String, Value>();
 		HashMap<String, Value> tableProperty;// = new HashMap<String, Value>()
		String _itemDataSetName = "";
		String _propertyName = "";
		String _propertyValue = "";
		String _propertyType = "";
		float _tableArogHeight = 0f;
		int colIndex = 0;
		int rowIndex = 0;
		Element _cellItem;
		
		float updateX = 0;
		float updateY = 0;
		
		
//		_childItem = (Element) _child.get(i);

		_itemId = _childItem.getAttribute("id");
		_className = _childItem.getAttribute("className");
		
		if( _className.equals("UBTable") || _className.equals("UBApproval"))
		{
			tableProperty = new HashMap<String, Value>();
			ArrayList<HashMap<String, Value>> _ubApprovalAr = null;
			
			_tableArogHeight = 0;
			ArrayList<Object> borderAr = null;
			ArrayList<String> _rowTypeInfoAr = null; 
			
			String _includeLayoutType = "";
			
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
			
			if( tableProperty.containsKey("includeLayoutType"))
			{
				_includeLayoutType = tableProperty.get("includeLayoutType").getStringValue();
			}
			
			tableProperty.put("tableId", new Value( _itemId,"string"));
			
			
			 
			if( tableProperty.containsKey("tableType") && tableProperty.get("tableType").getStringValue().equals(DATA_TABLE_TYPE) )
			{
				// row별 타입을 담아두기 ( H, D, F )
				_rowTypeInfoAr = tableProperty.get("rowTypeInfo").getArrayStringValue();
			}
			
			
			//band에 table속성 저장 by IHJ
			try
			{
				bandInfoData.get(tableProperty.get("band").getStringValue()).setTableProperty(tableProperty);		
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
			
			float _removeCellW = 0;
			float _convertW = 0;
			ArrayList<Float> _tblColumnWdithAr = new ArrayList<Float>();
			float _cellW = 0;

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
						_ubFxChkeck = UBIDataUtilPraser.getUBFxChkeck(_data,  tableMapProperty.get("cell").getElementValue(), _param, "includeLayout", mFunction );
					}
					if(!_ubFxChkeck){									
						_exColIdx.add(l);					
						if(!tableMapProperty.get("colSpan").getStringValue().equals("1")  && _exColIdx.indexOf(l) != -1 ){
							int _colSpan = Integer.parseInt(tableMapProperty.get("colSpan").getStringValue());
							for(int tmp = l+1; tmp < l+_colSpan; tmp++){
								_exColIdx.add(tmp);							
							}
						}
						_removeCellW = _removeCellW + Float.valueOf( tableMapProperty.get("width").getStringValue() );
					}
					else if(_exColIdx.indexOf(l) == -1)
					{
						_convertW = _convertW + Float.valueOf( tableMapProperty.get("columnWidth").getStringValue() );
					}
					_tblColumnWdithAr.add(Float.valueOf( tableMapProperty.get("columnWidth").getStringValue() ) );
					
				}	

				//2. column visible array에 해당하는 컬럼들을 row별로 돌면서 merge여부에 따라 해당 인덱스 추가
				if(_exColIdx.size() > 0){
					for ( l = 0; l < _allTableMap.get(0).size()-1; l++) {
						for ( k = 1; k < _allTableMap.size(); k++) {					
							tableMapProperty = _allTableMap.get(k).get(l);
							if(!tableMapProperty.get("colSpan").getStringValue().equals("1")   && _exColIdx.indexOf(l) != -1){
								int _colSpan = Integer.parseInt(tableMapProperty.get("colSpan").getStringValue());
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
									
									_tblColumnWdithAr.set( l,  tableMapProperty.get("columnWidth").getIntegerValue() + _lastW );
								}
								else
								{
									_lastAddPosition = l;
									_addWFloat = _removeCellW * ( tableMapProperty.get("columnWidth").getIntegerValue() / _convertW );
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
						_cellW = 0;
						for ( k = 0; k < _allTableMap.size(); k++) {	
							ArrayList<HashMap<String, Value>> _tableRow = _allTableMap.get(k);
							HashMap<String, Value> _tableMap = null;
							for ( l = 0; l < _tableRow.size(); l++) {
								_cellW = 0;
								_tableMap = (HashMap<String, Value>) _tableRow.get(l);
								_tableMap.put("columnWidth", new Value(_tblColumnWdithAr.get(l)));
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
			
			ArrayList<HashMap<String, Value>> _cellItems = new ArrayList<HashMap<String, Value>>();
			tableProperty.put("CELLS", new Value(_cellItems , "object"));
			float _bandYChkNum = 0;
			float _minusBandY = 0;
			int _minusRowSpan = 0;
			String _rowBandType = "";
			float _bandHeight = 0;
			float _addY = 0;
			
			if(_rowTypeInfoAr == null)
			{
				tableProperty.put("band", new Value("EMPTY_BAND" , "string"));
				_addY = tableProperty.get("y").getIntegerValue();
			}
			
			for ( k = 0; k < _allTableMap.size(); k++) {
				
				colIndex = 0;
				
				if( _rowTypeInfoAr != null && _rowTypeInfoAr.size() == _allTableMap.size() )
				{
					if( _rowBandType.equals("") == false && _rowBandType.equals(_rowTypeInfoAr.get(k)) == false )
					{
						_minusBandY = _bandYChkNum;
						_minusRowSpan = k;
						
						_bandHeight = 0;
					}
					_rowBandType = _rowTypeInfoAr.get(k).toString();
					
					if( bandInfoData.containsKey(_rowBandType+"_"+_itemId) )
					{
						float _addHeight = _yAr.get(k+1) - _yAr.get(k);
						bandInfoData.get( _rowBandType +"_"+ _itemId ).setHeight( bandInfoData.get( _rowBandType+"_"+_itemId ).getHeight() + _addHeight);
					}
					
				}
				
				_bandYChkNum = _yAr.get(k+1);
				
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
						
						//아이템의 LineHeight값을 수정 120% => 1.2로 변경
						if( itemProperty.get("lineHeight") != null && itemProperty.get("lineHeight").getStringValue().equals("") == false  )
						{
							String _value = itemProperty.get("lineHeight").getStringValue();
							_value = _value.toString().replace("%25", "").replace("%", "");
							_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
							
							itemProperty.put("lineHeight",  new Value( Float.valueOf( _value ), "number") );
						}
						
						//cell index정보 저장
						itemProperty.put( "rowIndex", new Value(tableMapProperty.get("rowIndex").getIntValue() - _minusRowSpan ) );	
						itemProperty.put( "columnIndex", new Value(tableMapProperty.get("columnIndex").getIntValue()) );	
					}
					else
					{
						//_cellItem = (Element) _tableMapDatas.item(l);
						itemProperty = _allTableMap.get(k).get(l);
						
						rowIndex =  k + Integer.valueOf( tableMapProperty.get("rowSpan").getStringValue() ) - _minusRowSpan;
						colIndex = colIndex + Integer.valueOf( tableMapProperty.get("colSpan").getStringValue() );
					}
					
					
					if(_exColIdx.size() > 0 && _includeLayoutType.equals( GlobalVariableData.M_TABLE_INCLUDE_LAYOUT_TYPE_AUTO ) ){
						_cellW = 0;
						if(!itemProperty.get("colSpan").getStringValue().equals("1")){
							int _colSpan = Integer.parseInt(itemProperty.get("colSpan").getStringValue());
							int _stidx= itemProperty.get("columnIndex").getIntValue();
							for(int tmp = _stidx; tmp < _stidx+_colSpan; tmp++){
								_cellW = _cellW +  _tblColumnWdithAr.get(tmp);
							}
						}
						else
						{
							_cellW = _tblColumnWdithAr.get(itemProperty.get("columnIndex").getIntValue());
						}
						itemProperty.put("width", new Value(_cellW));
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
					
					useTopBorder = false;
					
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
					
					String _tempBorderStr = tableMapProperty.get("borderString").getStringValue();				
					if(useLeftBorder){	//left border가 공유일 경우 top의 변경여부를 저장한다.					
						_patt = Pattern.compile("borderTop,borderType:[^,]+");
						_matcher =  _patt.matcher(_tempBorderStr);
						if(_matcher.find()){
							_topBorderStr = _tempBorderStr.substring(_matcher.start(), _matcher.end()).split(":")[1];
						}			
						_topBorderBefStr = _allTableMap.get(k).get(l-1).get("borderString").getStringValue();			
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
						_bottomBorderBefStr = _allTableMap.get(k).get(l-1).get("borderString").getStringValue();			
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
						_leftBorderBefStr = _allTableMap.get(k-1).get(l).get("borderString").getStringValue();		
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
						_rightBorderBefStr = _allTableMap.get(k-1).get(l).get("borderString").getStringValue();		
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
					
					useRightBorder = true;
					useBottomBorder = true;
					
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
						if(_newTalbeFlag) _rightBorderStr =  _allTableMap.get(rowIndex-1).get(colIndex-1 ).get("borderString").getStringValue();
						else  _rightBorderStr =  tableMapProperty.get("borderString").getStringValue();
//						 _rightBorderStr =  tableMapProperty.get("borderString").getStringValue();
					}
					
					if(useBottomBorder)
					{
						if(_newTalbeFlag)_bottomBorderStr =  _allTableMap.get(rowIndex-1).get(colIndex-1).get("borderString").getStringValue();
						else  _bottomBorderStr =  tableMapProperty.get("borderString").getStringValue();
//						_bottomBorderStr =  tableMapProperty.get("borderString").getStringValue();
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
						itemProperty.put("cellOutHeight", new Value( 0, "number") );
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
					
					boolean _isNotDataBand = false;
					
					if( _rowBandType.equals("") == false )
					{
						tableProperty.put("band", new Value(_rowBandType+"_"+_itemId , "string"));
						_isNotDataBand = true;
					}
					
					// Border Original Type담아두기
					if( borderAr.size() > 5 ) itemProperty.put("borderOriginalTypes",  	Value.fromArrayString( (ArrayList<String>) borderAr.get(5) ));
					
					updateX = Float.valueOf(tableProperty.get("x").getStringValue()) +_newXValue.get(l);
					updateY = itemProperty.get("y").getIntegerValue() - _minusBandY;
					
					if( _isNotDataBand == false )
					{
						itemProperty.put(  "y", new Value( updateY + _addY, "string"));
					}
					
					itemProperty.put(  "band_x", new Value( updateX, "string"));
					itemProperty.put(  "x", new Value(updateX , "string"));
					itemProperty.put(  "band_y", new Value( updateY, "string"));
					
					if( tableProperty.containsKey("band") == false ||tableProperty.get("band").getStringValue().equals("null") || tableProperty.get("band").getStringValue().equals("") )
					{
						itemProperty.put(  "y", new Value( updateY, "string"));
					}
					
					
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
						if( _itemDataSetName.equals("") == false  && _data.containsKey(_itemDataSetName) && _data.get(_itemDataSetName) != null && tableProperty.containsKey("band")  && bandInfoData.containsKey(tableProperty.get("band").getStringValue()) )
						{
							if( bandInfoData.get( tableProperty.get("band").getStringValue()).getDataSet().equals("") || _data.get( bandInfoData.get( tableProperty.get("band").getStringValue()).getDataSet() ).size() < _data.get(_itemDataSetName).size() )
							{
								bandInfoData.get(tableProperty.get("band").getStringValue()).setDataSet(_itemDataSetName);
								bandInfoData.get(tableProperty.get("band").getStringValue()).setRowCount(_data.get(_itemDataSetName).size());
							}
						}
						
						if(tableProperty.containsKey("band") && tableProperty.get("band").getStringValue().equals("") == false && bandInfoData.containsKey(tableProperty.get("band").getStringValue()) )
						{
							bandInfoData.get(tableProperty.get("band").getStringValue()).getChildren().add(itemProperty);
							
//							int _requiredItemIndex = OriginalRequiredItemList.indexOf(itemProperty.get("id").getStringValue());
//							int _tabIndexItemIndex = OriginalTabIndexItemList.indexOf(itemProperty.get("id").getStringValue());
//							
//							if( _requiredItemIndex != -1 )
//							{
//								bandInfoData.get(tableProperty.get("band").getStringValue()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").getStringValue() );
//							}
//							
//							if( _tabIndexItemIndex != -1 )
//							{
//								bandInfoData.get(tableProperty.get("band").getStringValue()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").getStringValue() );
//							}
						}
						
					}
					
					if(l == 0 )
					{
						_tableArogHeight = _allTableMap.get(k).get(l).get("rowHeight").getIntegerValue();
					}
					
					String _tblBand = (tableProperty.containsKey("band") && tableProperty.get("band") != null)?tableProperty.get("band").getStringValue():"";
					if( _tblBand.equals("") == false && bandInfoData.containsKey(tableProperty.get("band").getStringValue()) && 
							bandInfoData.get(tableProperty.get("band").getStringValue()).getAutoVerticalPosition() )
					{
						// Table의 Cell들을 담아둔다.
						_cellItems.add(itemProperty);
					}
					
				}
				
			}
			
			if(_className.equals("UBApproval") && _ubApprovalAr != null)
			{
//				convertTableMapToApprovalTbl(_ubApprovalAr, bandInfoData );
			}
			
			
		}
		else
		{
			itemProperty = new HashMap<String, Value>();
			itemProperty.put("Element", new Value(_childItem,"element") );
			NodeList _propertys = (NodeList) _childItem.getElementsByTagName("property");
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
			
			//아이템의 LineHeight값을 수정 120% => 1.2로 변경
			if( itemProperty.get("lineHeight") != null && itemProperty.get("lineHeight").getStringValue().equals("") == false  )
			{
				String _value = itemProperty.get("lineHeight").getStringValue();
				_value = _value.toString().replace("%25", "").replace("%", "");
				_value = String.valueOf((Float.parseFloat(_value.toString())/100));		
				
				itemProperty.put("lineHeight",  new Value( Float.valueOf( _value ), "number") );
			}
			
			itemProperty.put(  "id", new Value( _childItem.getAttribute("id"), "string"));
			itemProperty.put(  "className", new Value(_className, "string"));
			
			if( itemProperty.containsKey("dataType") && itemProperty.get("dataType").getStringValue().equals("") == false  &&  !itemProperty.get("dataType").getStringValue().equals("0") &&
					itemProperty.get("dataSet").getStringValue().equals("") == false  )
			{
				_itemDataSetName = itemProperty.get("dataSet").getStringValue();
			}
			
			if( _itemDataSetName.equals("") == false  && _data.containsKey(_itemDataSetName) && _data.get(_itemDataSetName) != null && itemProperty.containsKey("band") && bandInfoData.containsKey(itemProperty.get("band").getStringValue()) )
			{
				if( bandInfoData.get( itemProperty.get("band").getStringValue()).getDataSet().equals("") || _data.get( bandInfoData.get( itemProperty.get("band").getStringValue()).getDataSet() ).size() < _data.get(_itemDataSetName).size() )
				{
					bandInfoData.get(itemProperty.get("band").getStringValue()).setDataSet(_itemDataSetName); 
					bandInfoData.get(itemProperty.get("band").getStringValue()).setRowCount(_data.get(_itemDataSetName).size());
				}
			}
			
			
			if(itemProperty.containsKey("band") && itemProperty.get("band").getStringValue().equals("") == false  && itemProperty.get("band").getStringValue().equals("null") == false && bandInfoData.containsKey(itemProperty.get("band").getStringValue()) )
			{
				// RadioButtonGroup일경우 라디오버튼보다 먼저 생성 되어야 이후 라디오버튼에 값을 지정할수 있다.
				if( _className.equals("UBRadioButtonGroup") )
				{
					bandInfoData.get(itemProperty.get("band").getStringValue()).getChildren().add(0, itemProperty);
				}
				else
				{
					bandInfoData.get(itemProperty.get("band").getStringValue()).getChildren().add(itemProperty);
				}
			}
			
//			int _requiredItemIndex = OriginalRequiredItemList.indexOf(itemProperty.get("id").getStringValue());
//			int _tabIndexItemIndex = OriginalTabIndexItemList.indexOf(itemProperty.get("id").getStringValue());
//			
//			if( _requiredItemIndex != -1 )
//			{
//				bandInfoData.get(itemProperty.get("band").getStringValue()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").getStringValue() );
//			}
//			
//			if( _tabIndexItemIndex != -1 )
//			{
//				bandInfoData.get(itemProperty.get("band").getStringValue()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").getStringValue() );
//			}
			
			//TEMPLET 처리
			if( _className.equals("UBTemplateArea") )
			{
				//Templet일경우 Band의 Height가 UBTempletArea보다 작을 경우 Band의 사이즈 업데이트
				if( itemProperty.get("autoHeight").getBooleanValue() && mTempletInfo != null && mTempletInfo.containsKey(itemProperty.get("id").getStringValue() ) )
				{
					float _templetHeight = mTempletInfo.get( itemProperty.get("id").getStringValue() ).getHeight() + itemProperty.get("band_y").getIntegerValue();
					if( bandInfoData.get(itemProperty.get("band").getStringValue()) != null && _templetHeight > bandInfoData.get(itemProperty.get("band").getStringValue()).getHeight() )
					{
						bandInfoData.get(itemProperty.get("band").getStringValue()).setHeight(_templetHeight);
					}
				}
				
			}
			
		}
		
	}
	
	
}
