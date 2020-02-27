/**
 * masterBand를 처리하여 페이지별 아이템을 내보내는 Class
 * 작성자 : 최명진
 * 작성일 : 2015-10-20
 * 수정일 : 2015-10-20
 */
package org.ubstorm.service.parser.formparser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import javax.script.ScriptException;
import javax.xml.xpath.XPathExpressionException;

import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.BandInfoMapDataSimple;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.TempletItemInfo;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.PageInfo;
import org.ubstorm.service.parser.formparser.info.PageInfoSimple;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MasterBandParser {

	private UDMParamSet m_appParams = null;
	private HashMap<String,String> mImageData;
	private HashMap<String,Object> mChartData;
	private Function mFunction;
	private int mMinimumResizeFontSize = 0;	// resizeFont 사용시 최소값 지정
	
	// Export여부를 판단하기 위한 변수
	private String isExportType = "";
	
	private float mPageMarginTop = 0;
	private float mPageMarginLeft = 0;
	private float mPageMarginRight = 0;
	private float mPageMarginBottom = 0;
	
	private HashMap<String, TempletItemInfo> mTempletInfo;
	
	public MasterBandParser() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	protected HashMap<String, Object> changeItemList;
	public void setChangeItemList( HashMap<String, Object> _value)
	{
		changeItemList = _value;
		
	}
	
	
	public MasterBandParser(UDMParamSet appParams) {
		super();
		// TODO Auto-generated constructor stub
		this.m_appParams = appParams;
	}
	
	public void setImageData(HashMap<String,String> imgData)
	{
		this.mImageData = imgData;
	}
	
	public void setChartData(HashMap<String,Object> chartData)
	{
		this.mChartData = chartData;
	}
	
	public void setFunction( Function _function )
	{
		this.mFunction = _function;
	}
	
	public String getIsExportType() {
		return isExportType;
	}

	public void setIsExportType(String isExportType) {
		this.isExportType = isExportType;
	}
	
	
	boolean isExportData = false;
	
	public boolean isExportData() {
		return isExportData;
	}

	public void setExportData(boolean isExportData) {
		this.isExportData = isExportData;
	}
	public int getMinimumResizeFontSize() {
		return mMinimumResizeFontSize;
	}

	public void setMinimumResizeFontSize(int mMinimumResizeFontSize) {
		this.mMinimumResizeFontSize = mMinimumResizeFontSize;
	}
	
	public void setTempletInfo( HashMap<String, TempletItemInfo> _templetInfo )
	{
		mTempletInfo = _templetInfo;
	}
	public HashMap<String, TempletItemInfo> getTempletInfo()
	{
		return mTempletInfo;
	}
	
	HashMap<String, ArrayList<HashMap<String, Object>>> DataSet;
	ContinueBandParser continueBandParser = null;
	
	public ArrayList<Object> loadTotalPage( Element _page, HashMap<String, ArrayList<HashMap<String, Object>>> _data,float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		int i = 0;
		int j = 0;
		int k = 0;
		HashMap< String, HashMap<String, String>> bandInfo = new HashMap< String, HashMap<String, String>>();
		HashMap<String, Integer> _groupBandCntMap = null;			// 밴드의 총 그룹갯수를 담아두는 객체
		
		NodeList _child = _page.getElementsByTagName("item");
		Element item = null;
		ArrayList<Element> bandList = new ArrayList<Element>();
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
		
		HashMap<String, ArrayList<String>> bandSequencyMap = new HashMap<String, ArrayList<String>>();
		ArrayList<String> bandSequency = new ArrayList<String>();
		String _dataBandName = "";
		String _dataSummeryBandName = "";
		String _dataFooterBandName = "";
		
		
		String cloneData = _page.getAttribute("clone");
		int _cloneRowCnt = 1;
		boolean clonePage = false;
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
		
		
		DataSet = _data;	
		int _propertyLength = 0;
		
		int _childLength = 0;
		_childLength = _child.getLength();
		//Element를 그룹핑처리
		for ( i = 0; i < _childLength; i++) {
			
			item = (Element) _child.item(i);
			
			_className = item.getAttribute("className");
			if( _className.length() > 4 && _className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) 
			{
				bandList.add(item);
				
				propertyMap = new HashMap<String, String>();
				
//				_propertys = (NodeList) _xpath.evaluate("./property", item, XPathConstants.NODESET);
				_propertys = item.getElementsByTagName("property");
				_propertyLength = _propertys.getLength();
				for ( j = 0; j < _propertyLength; j++) {
					_propertyElement = (Element) _propertys.item(j);
					propertyMap.put(_propertyElement.getAttribute("name"), URLDecoder.decode( _propertyElement.getAttribute("value"), "UTF-8" ) );
				}
				bandElementData.put(item.getAttribute("id"), item);
				bandInfo.put(item.getAttribute("id"), propertyMap);
				
				if(_className.equals("UBDataBand"))
				{
					if(bandSequency.size() > 0 && _dataBandName != "" )
					{
						bandSequencyMap.put(_dataBandName, bandSequency);
						bandSequency = new ArrayList<String>();
					}
					bandSequency.add("d");
					_dataBandName = item.getAttribute("id");
					bandSequencyMap.put(_dataBandName, bandSequency);
				}
				else if( _className.equals("UBDataFooterBand") )
				{
					if( _dataBandName != "" )
					{
						bandInfo.get(_dataBandName).put("footer",  item.getAttribute("id"));
						_dataFooterBandName = "";
						bandSequencyMap.get(_dataBandName).add("f");
					}
					else
					{
						_dataFooterBandName = item.getAttribute("id");
						bandSequency.add("f");
					}
						
				}else if( _className.equals("UBDataPageFooterBand") )
				{
					if( _dataBandName != "" )
					{
						bandInfo.get(_dataBandName).put("summery",  item.getAttribute("id"));
						_dataFooterBandName = "";
						bandSequencyMap.get(_dataBandName).add("s");
					}
					else
					{
						_dataSummeryBandName = item.getAttribute("id");
						bandSequency.add("s");
					}
				}
				else if( _className.equals("UBDataHeaderBand") ||  _className.equals("UBEmptyBand") )
				{
					if(bandSequency.size() > 0 && _dataBandName != "" )
					{
						bandSequencyMap.put(_dataBandName, bandSequency);
						bandSequency = new ArrayList<String>();
					}
				}
				
				
				
			}
			else
			{
//				bandProperty = (Element) _xpath.evaluate("./property[@name='band']", item, XPathConstants.NODE);
//				_bandName = bandProperty.getAttribute("value");
				_bandName = "";
				NodeList _bP = item.getElementsByTagName("property");
				int _bpPropertyLength = 0;
				float _bandY = 0;
				if( _bP != null ) _bpPropertyLength = _bP.getLength();
				
				for ( j = 0; j < _bpPropertyLength; j++) {
					bandProperty = (Element) _bP.item(j);
					if(bandProperty.getAttribute("name").equals("band"))
					{
						_bandName = bandProperty.getAttribute("value");
						break;
					}
				}
				
				// Templet 아이템일 경우 밴드의 Height를 변경
				if( item.getAttribute("className").toString().equals("UBTemplateArea") )
				{
					String _itemID = item.getAttribute("id").toString();
					if( mTempletInfo.containsKey(_itemID) && mTempletInfo.get(_itemID).getAutoHeight() && _bandName.equals("") == false && bandInfo.containsKey(_bandName) )
					{
						if( mTempletInfo.get(_itemID).getBandY() + mTempletInfo.get(_itemID).getHeight() > Float.valueOf( bandInfo.get(_bandName).get("height") ) )
						{
							bandInfo.get(_bandName).put("height", Float.valueOf( mTempletInfo.get(_itemID).getBandY() + mTempletInfo.get(_itemID).getHeight() ).toString() );
						}
					}
				}
				
				if(bandItems.containsKey( _bandName ) == false )
				{
					bandItems.put(_bandName, new ArrayList<Element>());
				}
				
				bandItems.get(_bandName).add(item);
				
			}
			
		}
		
		
		if(bandSequency.size() > 0 && _dataBandName != "" )
		{
			bandSequencyMap.put(_dataBandName, bandSequency);
			bandSequency = new ArrayList<String>();
		}
		// 밴드리스트를 가져와서 그룹핑
		
		
		boolean _grpFlg = false;
		boolean _dataFlag = false;
		ArrayList<Object> totalList = new ArrayList<Object>();	// 그룹핑된 배열을 담아둘 객체
		ArrayList<Element> arr = new ArrayList<Element>();
		float _argoY 		= 0;
		float _argoBandY 	= 0;
		float _pageHeaderHeight	= 0;
		HashMap<String, Object> addBandInfo = new HashMap<String, Object>();
		float _headerPosition = 0;
		int _bandListLength = 0;
		
		_bandListLength = bandList.size();
		
		for ( i = 0; i < _bandListLength; i++) {
			
			item = (Element) bandList.get(i);
			_className = item.getAttribute("className");
			
			if( _grpFlg && _className.equals("UBGroupFooterBand") ) _grpFlg = false;
			
			
			if( _className.equals("UBDataFooterBand") == false && _className.equals("UBDataPageFooterBand") == false )
			{
				
				if( _className.equals("UBPageHeaderBand") )
				{
					arr.add(item);
					
					addBandInfo = new HashMap<String, Object>();
					
					addBandInfo.put("type", "pageHeader");
					addBandInfo.put("item", arr);
					addBandInfo.put("height", Float.valueOf( bandInfo.get(item.getAttribute("id")).get("height") ));
					addBandInfo.put("yPosition", _headerPosition );
					addBandInfo.put("repeat", bandInfo.get(item.getAttribute("id")).get("repeat") );
					
					if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
					{
							totalList.add( addBandInfo );
					}
//					totalList.add( addBandInfo );
					
					arr = new ArrayList<Element>();
					
					_headerPosition = _headerPosition +  Float.valueOf( bandInfo.get(item.getAttribute("id")).get("height") );
					_pageHeaderHeight = _pageHeaderHeight +  Float.valueOf( bandInfo.get(item.getAttribute("id")).get("height") );
					_argoY = Float.valueOf( bandInfo.get(item.getAttribute("id")).get("y") ) + Float.valueOf( bandInfo.get(item.getAttribute("id")).get("height") );
				}
//				else if( i > 0 && _className.equals("UBPageHeaderBand") == false && bandList.get(i-1).getAttribute("className").equals("UBPageHeaderBand") )
//				{
//					addBandInfo = new HashMap<String, Object>();
//					
//					addBandInfo.put("type", "pageHeader");
//					addBandInfo.put("item", arr);
//					addBandInfo.put("height", _pageHeaderHeight);
//					addBandInfo.put("yPosition", Float.valueOf( bandInfo.get(arr.get(0).getAttribute("id")).get("y")) );
//					totalList.add( addBandInfo );
//					
//					arr = new ArrayList<Element>();
//					_pageHeaderHeight = 0;
//				}
				
				if( _className.equals("UBPageFooterBand") || _className.equals("UBEmptyBand") )
				{
					if( arr.size() > 0 )
					{
						if( bandList.get(i-1).getAttribute("className").equals("UBGroupFooterBand") )
						{
//							_argoBandY =  Float.valueOf(bandInfo.get(bandList.get(i-1).getAttribute("id")).get("y"))  -  Float.valueOf( bandInfo.get(item.getAttribute("id")).get("height") );
							_argoBandY =  Float.valueOf(bandInfo.get(bandList.get(i-1).getAttribute("id")).get("y"))  +  Float.valueOf( bandInfo.get(bandList.get(i-1).getAttribute("id")).get("height") );
						}
						else
						{
							_argoBandY = Float.valueOf( bandInfo.get(item.getAttribute("id")).get("y") );
						}
						
						addBandInfo = new HashMap<String, Object>();
						
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", _argoBandY-_argoY);
						addBandInfo.put("yPosition", Float.valueOf( bandInfo.get(arr.get(0).getAttribute("id")).get("y")) );
						
						if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
						{
								totalList.add( addBandInfo );
						}
//						totalList.add( addBandInfo );
						
						arr = new ArrayList<Element>();
						
					}
					
					addBandInfo = new HashMap<String, Object>();
					
					arr.add(item);
					addBandInfo.put("type", "band");
					addBandInfo.put("item", arr);
					addBandInfo.put("height", Float.valueOf( bandInfo.get(item.getAttribute("id")).get("height") ) );
					addBandInfo.put("yPosition",Float.valueOf( bandInfo.get(item.getAttribute("id")).get("y") ) );
					
					if(bandInfo.get(item.getAttribute("id")).containsKey("repeat")) addBandInfo.put("repeat", bandInfo.get(item.getAttribute("id")).get("repeat"));
					else addBandInfo.put("repeat", "n" );
					
					if( _className.equals("UBPageFooterBand") )
					{
						addBandInfo.put("type", "pageFooter");
						if( clonePage )
						{
							float _footerBandYposition = _pageHeight -  Float.valueOf( addBandInfo.get("height").toString() );
							addBandInfo.put("yPosition",_footerBandYposition);
						}
					}
					else
					{
						addBandInfo.put("type", "band");
					}
					
					if( _className.equals("UBPageFooterBand") || (  Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() ) ) )
					{
							totalList.add( addBandInfo );
					}
//					totalList.add(addBandInfo);
					
					_dataFlag = false;
					_grpFlg = false;
					arr = new ArrayList<Element>();
					
				}
				else if( _className.equals("UBCrossTabBand") )
				{
					if( arr.size() > 0 )
					{
						if( bandList.get(i-1).getAttribute("className").equals("UBGroupFooterBand") )
						{
							_argoBandY =  Float.valueOf(bandInfo.get(bandList.get(i-1).getAttribute("id")).get("y"))  +  Float.valueOf( bandInfo.get(item.getAttribute("id")).get("height") );
						}
						else
						{
							_argoBandY = Float.valueOf( bandInfo.get(item.getAttribute("id")).get("y") );
						}
						
						addBandInfo = new HashMap<String, Object>();
						
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", _argoBandY- Float.valueOf( bandInfo.get(arr.get(0).getAttribute("id")).get("y")) );
						addBandInfo.put("yPosition", Float.valueOf( bandInfo.get(arr.get(0).getAttribute("id")).get("y")) );
						
						if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
						{
								totalList.add( addBandInfo );
						}
//						totalList.add( addBandInfo );
						
						arr = new ArrayList<Element>();
						
					}
					addBandInfo = new HashMap<String, Object>();
					
					if( i == bandList.size()-1 )
					{
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", pageHeight );
						addBandInfo.put("yPosition", Float.valueOf( bandInfo.get(item.getAttribute("id")).get("y")) );
					}
					else
					{
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", Float.valueOf( bandInfo.get(bandList.get(i+1).getAttribute("id")).get("y")) - Float.valueOf( bandInfo.get(item.getAttribute("id")).get("y")) );
						addBandInfo.put("yPosition", Float.valueOf( bandInfo.get(item.getAttribute("id")).get("y")) );
					}

					if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
					{
							totalList.add( addBandInfo );
					}
//					totalList.add( addBandInfo );
					
					
				}
				else if( _className.equals("UBPageHeaderBand") == false )
				{
					if( (!_grpFlg && _className.equals("UBDataHeaderBand")) || (!_grpFlg&&_className.equals("UBDataBand")&&_dataFlag) ||
							_className.equals("UBGroupHeaderBand"))
					{
						
						if( _className.equals("UBGroupHeaderBand") )
						{
							_dataFlag = false;
							_grpFlg = true;
						}
						
						if( arr.size() > 0 )
						{
							
							if( bandList.get(i-1).getAttribute("className").equals("UBGroupFooterBand") )
							{
								_argoBandY = Float.valueOf( bandInfo.get(bandList.get(i-1).getAttribute("id")).get("y")) + Float.valueOf( bandInfo.get(bandList.get(i-1).getAttribute("id")).get("height"));
							}
							else
							{
								_argoBandY = Float.valueOf( bandInfo.get(item.getAttribute("id")).get("y"));
							}
							
							addBandInfo = new HashMap<String, Object>();
							
							addBandInfo.put("type", "continue");
							addBandInfo.put("item", arr);
							addBandInfo.put("height", _argoBandY - _argoY );
							addBandInfo.put("yPosition", Float.valueOf( bandInfo.get(arr.get(0).getAttribute("id")).get("y")) );
							
							if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )							
							{
									totalList.add( addBandInfo );
							}
//							totalList.add( addBandInfo );
							
							_dataFlag = false;
							
						}
						
						arr = new ArrayList<Element>();
						_argoY = Float.valueOf( bandInfo.get(item.getAttribute("id")).get("y") );
						
					}
					
					if( _className.equals("UBDataBand") )
					{
						ArrayList<String> bandSequence = bandSequencyMap.get(item.getAttribute("id"));
						int _sequenceLength = bandSequence.size();
						for ( j = 0; j < _sequenceLength; j++) {
							
							if( bandSequence.get(j).equals("f") && bandInfo.get(item.getAttribute("id")).get("footer")!= null  && bandInfo.get(item.getAttribute("id")).get("footer").equals("") == false )
							{
								arr.add( bandElementData.get( bandInfo.get(item.getAttribute("id")).get("footer") ) );
							}
							else if( bandSequence.get(j).equals("d") )
							{
								arr.add(item);
							}
							else if( bandSequence.get(j).equals("s") && bandInfo.get(item.getAttribute("id")).get("summery")!= null  && bandInfo.get(item.getAttribute("id")).get("summery").equals("") == false )
							{
								arr.add( bandElementData.get( bandInfo.get(item.getAttribute("id")).get("summery") ) );
							}
							
						}
						
						_dataFlag = true;
					}
					else
					{
						arr.add(item);
					}
					
				}
				
			}
			
		}
		
		
		
		
		if( arr.size() > 0 )
		{
			addBandInfo = new HashMap<String, Object>();
			if( arr.get(arr.size()-1).getAttribute("className").equals("UBGroupFooterBand")  )
			{
				addBandInfo.put("height", (  Float.valueOf( bandInfo.get(arr.get(arr.size()-1).getAttribute("id")).get("y")) + Float.valueOf( bandInfo.get(arr.get(arr.size()-1).getAttribute("id")).get("height")) )- Float.valueOf( bandInfo.get(arr.get(0).getAttribute("id")).get("y")) );
			}
			else
			{
				addBandInfo.put("height", pageHeight  - Float.valueOf( bandInfo.get(arr.get(0).getAttribute("id")).get("y")) );
			}
			addBandInfo.put("type", "continue");
			addBandInfo.put("item", arr);
//			addBandInfo.put("height", _argoBandY- Float.valueOf( bandInfo.get(arr.get(0).getAttribute("id")).get("y")) );
			addBandInfo.put("yPosition", Float.valueOf( bandInfo.get(arr.get(0).getAttribute("id")).get("y")) );
			
			if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
			{
					totalList.add( addBandInfo );
			}
			
		}
		
		
		// 그룹핑 된 Element를 ContinueBand처리
		
		String _type = "";
		HashMap<String, Object> _totalItem;
		ArrayList<Element> pageItems = new ArrayList<Element>();
		
		int _pageTotPage = 0;		// 각 페이지별 최대 페이지를 담기
		int _currentTotPage = 0;	// 각 페이지별 최대 페이지를 담기
		int _max = totalList.size();
		int _subMax = 0;
		ArrayList<String> _tabIndexList = null; 
		ArrayList<String> _requiredList = null; 
				
		_tabIndexList = getTabIndexItemDataInfo(_page, _tabIndexList);
		_requiredList = getRequiredItemDataInfo(_page, _requiredList);

		ArrayList<String> _tabIndexElementList = null;
		ArrayList<String> _requiredElementList = null;
		
		ArrayList<Object> _tabIndexPages = null;
		ArrayList<Object> _requiredPages = null;
		
		for ( i = 0; i < _max; i++) {

			_currentTotPage = 0;
			_totalItem = (HashMap<String, Object>) totalList.get(i);
			_type = _totalItem.get("type").toString();
			pageItems = new ArrayList<Element>();
			ArrayList<Element> items = (ArrayList<Element>) _totalItem.get("item");
			
			// 각 밴드별 아이템을 담기
			ArrayList<String> bands = new ArrayList<String>();
			_subMax = items.size();
			for ( j = 0; j < _subMax; j++) {
				
				if( bandItems.containsKey( items.get(j).getAttribute("id") ) )
				{
					bands.add(items.get(j).getAttribute("id"));
				}
				pageItems.add(items.get(j));
			}
			_subMax = bands.size();
			for ( j = 0; j < _subMax; j++) {
				for ( k = 0; k < bandItems.get( bands.get(j) ).size(); k++) {
					pageItems.add( bandItems.get( bands.get(j) ).get(k) );
				}
			}
			
			_totalItem.put("pageItems", pageItems);
			//bandItems
			
			// ContinueBand 타입일경우 처리
			if( _type.equals("continue") )
			{
				pageItems.add(0, _page);
				ArrayList<Object> bandAr = continueBandParser.loadTotalPage(pageItems, DataSet,0, (Float) _totalItem.get("height"), pageWidth, mXAr, null,null );
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
				
				_totalItem.put("bandInfoData", bandAr);
				_currentTotPage = pagesRowList.size();
				_totalItem.put("totPage", _currentTotPage);
				
				// GroupBand의 숫자만큼 id와 GroupBandLength를 가져오기
				if(bandAr.size() > 8 )
				{
					HashMap<String, Integer> _grpCntMap = (HashMap<String, Integer>) bandAr.get(8);
					for(String _groupID : _grpCntMap.keySet() )
					{
						if(_groupBandCntMap == null) _groupBandCntMap = new HashMap<String, Integer>();
						_groupBandCntMap.put( _groupID , _grpCntMap.get(_groupID));
					}
					
				}
				
				if(bandAr.size() > 6)
				{
					ArrayList<ArrayList<HashMap<String, Object>>> _requiredBandList = (ArrayList<ArrayList<HashMap<String, Object>>>) bandAr.get(6);
					ArrayList<ArrayList<HashMap<String, Object>>> _tabIndexBandList = (ArrayList<ArrayList<HashMap<String, Object>>>) bandAr.get(7);
					
					ArrayList<HashMap<String, Object>> _chkPage;
					int _idx = _tabIndexBandList.size();
					int _subIdx = 0;
					int l = 0;
					int n = 0;
					for ( l = 0; l < _idx; l++) {
						_subIdx = _tabIndexBandList.get(l).size();
						if( _tabIndexPages == null )
						{
							_tabIndexPages = new ArrayList<Object>();
						}
						if(_tabIndexPages.size() <= l ) _tabIndexPages.add( new ArrayList<HashMap<String, Object>>());
						_chkPage = (ArrayList<HashMap<String, Object>>) _tabIndexPages.get(l);
						
						for ( n = 0; n < _subIdx; n++) {
							_chkPage.add(_tabIndexBandList.get(l).get(n) );
						}
					}
					
					_idx = _requiredBandList.size();
					
					for ( l = 0; l < _idx; l++) {
						_subIdx = _requiredBandList.get(l).size();
						if( _requiredPages == null )
						{
							_requiredPages = new ArrayList<Object>();
						}
						if(_requiredPages.size() <= l ) _requiredPages.add( new ArrayList<HashMap<String, Object>>());
						_chkPage = (ArrayList<HashMap<String, Object>>) _requiredPages.get(l);
						
						for ( n = 0; n < _subIdx; n++) {
							_chkPage.add(_requiredBandList.get(l).get(n) );
						}
					}
					
				}
				
				// tabindex, 및 필수값 처리 ( 밴드의 경우 continueBand에서 내보내주는 아이템을 사용 )
				
			}
			else if( _type.equals("band") )	// 	그 외 Empty밴드일경우
			{
				if(_totalItem.containsKey("repeat") && _totalItem.get("repeat").equals("y") )
				{
					_currentTotPage = bandPageGetTotalPage(pageItems);
				}
				else
				{
					_currentTotPage = 1;
				}
				_totalItem.put("totPage", _currentTotPage);
				
			}
			else if( _type.equals("pageHeader") || _type.equals("pageFooter") )	// PageHeader 일경우 
			{
				_totalItem.put("totPage", 0);
			}
			
			// 필수값및 탭 인덱스 담기
			if( _type.equals("continue") == false && pageItems.size() > 0 )
			{
				if( _tabIndexList.size() > 0 ) _tabIndexElementList = getReqIndexItems(pageItems, _tabIndexList,_tabIndexElementList );
				if( _requiredList.size() > 0 ) _requiredElementList = getReqIndexItems(pageItems, _requiredList,_requiredElementList );
			}
			
			if( _currentTotPage > _pageTotPage )
			{
				_pageTotPage = _currentTotPage;
			}
			
		}
		
		for ( i = 0; i < _max; i++) {
			_totalItem = (HashMap<String, Object>) totalList.get(i);
			_type = _totalItem.get("type").toString();
			
			if( _type.equals("band") )
			{
				// 	그 외 Empty밴드일경우
				if( _totalItem.containsKey("repeat") == false ||  _totalItem.get("repeat").equals("y") ==  false )
				{
					_totalItem.put("totPage", _pageTotPage);
				}
			}
		}
		
		// 필수값 담기
		if( _tabIndexList.size() > 0 || _requiredList.size() > 0 )
		{
			int _subIdx = 0;
			int l = 0;
			int n = 0;
			ArrayList<HashMap<String, Object>> _chkPage;
			
			for ( l = 0; l < _pageTotPage; l++) {
				HashMap<String, Object> _tempMap;
				
				if(_tabIndexElementList != null )
				{
					_subIdx = _tabIndexElementList.size();
					
					if( _tabIndexPages == null )
					{
						_tabIndexPages = new ArrayList<Object>();
					}
					
					if(_tabIndexPages.size() <= l ) _tabIndexPages.add( new ArrayList<HashMap<String, Object>>());
					_chkPage = (ArrayList<HashMap<String, Object>>) _tabIndexPages.get(l);
					
					for ( n = 0; n < _subIdx; n++) {
						
						_tempMap = new HashMap<String, Object>();
						_tempMap.put("id", _tabIndexElementList.get(n));
						
						_chkPage.add( _tempMap );
					}
				}
				
				if(_requiredElementList != null )
				{
					_subIdx = _requiredElementList.size();
					
					if( _requiredPages == null )
					{
						_requiredPages = new ArrayList<Object>();
					}
					
					if(_requiredPages.size() <= l ) _requiredPages.add( new ArrayList<HashMap<String, Object>>());
					_chkPage = (ArrayList<HashMap<String, Object>>) _requiredPages.get(l);
					
					for ( n = 0; n < _subIdx; n++) {
						
						_tempMap = new HashMap<String, Object>();
						_tempMap.put("id", _requiredElementList.get(n));
						_chkPage.add( _tempMap );
					}
				}
					
				
			}
		}
		
		ArrayList<Object> retAr = new ArrayList<Object>();
		retAr.add(_pageTotPage);
		retAr.add(totalList);
		
		retAr.add(_requiredPages);
		retAr.add(_tabIndexPages);

		retAr.add(_groupBandCntMap);
		
		return retAr;
	}
	
	public ArrayList<Object> loadTotalPage( PageInfo _page, HashMap<String, ArrayList<HashMap<String, Object>>> _data,float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		int i = 0;
		int j = 0;
		int k = 0;
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
		
		HashMap<String, ArrayList<String>> bandSequencyMap = new HashMap<String, ArrayList<String>>();
		ArrayList<String> bandSequency = new ArrayList<String>();
		String _dataBandName = "";
		String _dataSummeryBandName = "";
		String _dataFooterBandName = "";
		
		String cloneData = _page.getClone();
		int _cloneRowCnt = 1;
		boolean clonePage = false;
		float _pageHeight = _page.getHeight();
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
				_cloneRowCnt = _page.getCloneRowCount();
			}
			
			_pageHeight = _pageHeight / _cloneRowCnt;
			clonePage = true;
		}
		
		
		DataSet = _data;	
		int _propertyLength = 0;
		
		// 밴드리스트를 가져와서 그룹핑
		
		boolean _grpFlg = false;
		boolean _dataFlag = false;
		ArrayList<Object> totalList = new ArrayList<Object>();	// 그룹핑된 배열을 담아둘 객체
		ArrayList<BandInfoMapData> arr = new ArrayList<BandInfoMapData>();
		float _argoY 		= 0;
		float _argoBandY 	= 0;
		float _pageHeaderHeight	= 0;
		HashMap<String, Object> addBandInfo = new HashMap<String, Object>();
		float _headerPosition = 0;
		int _bandListLength = 0;
		ArrayList<BandInfoMapData> bandList = _page.getBandList();
		BandInfoMapData item;
		HashMap<String, BandInfoMapData> bandInfo = _page.getBandInfoData();
		HashMap<String, Integer> _groupBandCntMap = _page.getGgroupBandCntMap();			// 밴드의 총 그룹갯수를 담아두는 객체
		
		
		_bandListLength = bandList.size();
		String _itemID = "";
		
		for ( i = 0; i < _bandListLength; i++) {
			
			item = bandList.get(i);
			_className = item.getClassName();
			_itemID = item.getId();
			
			if( _grpFlg && _className.equals("UBGroupFooterBand") ) _grpFlg = false;
			
			
			if( _className.equals("UBDataPageFooterBand") == false )
			{
				
				if( _className.equals("UBPageHeaderBand") )
				{
					arr.add(item);
					
					addBandInfo = new HashMap<String, Object>();
					
					addBandInfo.put("type", "pageHeader");
					addBandInfo.put("item", arr);
					addBandInfo.put("height", Float.valueOf( bandInfo.get( _itemID ).getHeight() ));
					addBandInfo.put("yPosition", _headerPosition );
					addBandInfo.put("repeat", bandInfo.get( _itemID ).getRepeat() );
					
					if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
					{
							totalList.add( addBandInfo );
					}
					
					arr = new ArrayList<BandInfoMapData>();
					
					_headerPosition = _headerPosition +  bandInfo.get(_itemID).getHeight();
					_pageHeaderHeight = _pageHeaderHeight +  bandInfo.get(_itemID).getHeight();
					_argoY = bandInfo.get(_itemID).getY() + bandInfo.get(_itemID).getHeight();
				}
				
				if( _className.equals("UBPageFooterBand") || _className.equals("UBEmptyBand") )
				{
					if( arr.size() > 0 )
					{
						if( bandList.get(i-1).getClassName().equals("UBGroupFooterBand") )
						{
							_argoBandY =  bandInfo.get(bandList.get(i-1).getId()).getY()  +  bandInfo.get(bandList.get(i-1).getId()).getHeight();
						}
						else
						{
							_argoBandY = bandInfo.get(_itemID ).getY();
						}
						
						addBandInfo = new HashMap<String, Object>();
						
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", _argoBandY-_argoY);
						addBandInfo.put("yPosition", bandInfo.get(arr.get(0).getId()).getY());
						
						if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
						{
								totalList.add( addBandInfo );
						}
						arr = new ArrayList<BandInfoMapData>();
					}
					
					addBandInfo = new HashMap<String, Object>();
					
					arr.add(item);
					addBandInfo.put("type", "band");
					addBandInfo.put("item", arr);
					addBandInfo.put("height", bandInfo.get(_itemID).getHeight() );
					addBandInfo.put("yPosition", bandInfo.get(_itemID).getY() );
					
					if( bandInfo.get( _itemID ).getRepeat() != null &&  !bandInfo.get( _itemID ).getRepeat().equals("") ) addBandInfo.put("repeat", bandInfo.get( _itemID ).getRepeat() );
					else addBandInfo.put("repeat", "n" );
					
					if( _className.equals("UBPageFooterBand") )
					{
						addBandInfo.put("type", "pageFooter");
						if( clonePage )
						{
							float _footerBandYposition = _pageHeight -  Float.valueOf( addBandInfo.get("height").toString() );
							addBandInfo.put("yPosition",_footerBandYposition);
						}
					}
					else
					{
						addBandInfo.put("type", "band");
					}
					
					if( _className.equals("UBPageFooterBand") || (  Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() ) ) )
					{
							totalList.add( addBandInfo );
					}
					
					_dataFlag = false;
					_grpFlg = false;
					arr = new ArrayList<BandInfoMapData>();
					
				}
				else if( _className.equals("UBCrossTabBand") )
				{
					if( arr.size() > 0 )
					{
						if( bandList.get(i-1).getClassName().equals("UBGroupFooterBand") )
						{
							_argoBandY =  bandInfo.get(bandList.get(i-1).getId()).getY()  +  bandInfo.get(_itemID).getHeight();
						}
						else
						{
							_argoBandY = bandInfo.get(_itemID).getY();
						}
						
						addBandInfo = new HashMap<String, Object>();
						
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", _argoBandY- bandInfo.get(arr.get(0).getId()).getY() );
						addBandInfo.put("yPosition", bandInfo.get(arr.get(0).getId()).getY() );
						
						if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
						{
								totalList.add( addBandInfo );
						}
						
						arr = new ArrayList<BandInfoMapData>();
						
					}
					addBandInfo = new HashMap<String, Object>();
					
					if( i == bandList.size()-1 )
					{
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", pageHeight );
						addBandInfo.put("yPosition", bandInfo.get(_itemID).getY() );
					}
					else
					{
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", bandInfo.get(bandList.get(i+1).getId()).getY() - bandInfo.get(_itemID).getY() );
						addBandInfo.put("yPosition", bandInfo.get(_itemID).getY() );
					}

					if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
					{
							totalList.add( addBandInfo );
					}
					
				}
				else if( _className.equals("UBPageHeaderBand") == false )
				{
					if( (!_grpFlg && _className.equals("UBDataHeaderBand")) || (!_grpFlg&&_className.equals("UBDataBand")&&_dataFlag) ||
							_className.equals("UBGroupHeaderBand"))
					{
						
						if( _className.equals("UBGroupHeaderBand") )
						{
							_dataFlag = false;
							_grpFlg = true;
						}
						
						if( arr.size() > 0 )
						{
							
							if( bandList.get(i-1).getClassName().equals("UBGroupFooterBand") )
							{
								_argoBandY = bandInfo.get(bandList.get(i-1).getId()).getY() + bandInfo.get(bandList.get(i-1).getId()).getHeight();
							}
							else
							{
								_argoBandY = bandInfo.get(_itemID).getY();
							}
							
							addBandInfo = new HashMap<String, Object>();
							
							addBandInfo.put("type", "continue");
							addBandInfo.put("item", arr);
							addBandInfo.put("height", _argoBandY - _argoY );
							addBandInfo.put("yPosition", bandInfo.get(arr.get(0).getId()).getY() );
							
							if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )							
							{
									totalList.add( addBandInfo );
							}
							
							_dataFlag = false;
							
						}
						
						arr = new ArrayList<BandInfoMapData>();
						_argoY = bandInfo.get(_itemID).getY();
						
					}
					
					if( _className.equals("UBDataBand") )
					{
						ArrayList<String> bandSequence = bandInfo.get(_itemID).getSequence();
						int _sequenceLength = bandSequence.size();
						for ( j = 0; j < _sequenceLength; j++) {
							
							if( bandSequence.get(j).equals("f") && bandInfo.get(_itemID).getFooter()!= null  && bandInfo.get(_itemID).getFooter().equals("") == false )
							{
								arr.add( bandInfo.get( bandInfo.get(_itemID).getFooter() ) );
							}
							else if( bandSequence.get(j).equals("d") )
							{
								arr.add(item);
							}
							else if( bandSequence.get(j).equals("s") && bandInfo.get(_itemID).getSummery() != null  && bandInfo.get(_itemID).getSummery() .equals("") == false )
							{
								arr.add( bandInfo.get( bandInfo.get(_itemID).getSummery() ) );
							}
							
						}
						
						_dataFlag = true;
					}
					else
					{
						arr.add(item);
					}
					
				}
				
			}
			
		}
		
		
		
		
		if( arr.size() > 0 )
		{
			addBandInfo = new HashMap<String, Object>();
			if( arr.get(arr.size()-1).getClassName().equals("UBGroupFooterBand")  )
			{
				addBandInfo.put("height", (  bandInfo.get(arr.get(arr.size()-1).getId()).getY() + bandInfo.get(arr.get(arr.size()-1).getId()).getHeight() )-  bandInfo.get(arr.get(0).getId()).getY() );
			}
			else
			{
				addBandInfo.put("height", pageHeight  - bandInfo.get(arr.get(0).getId()).getY() );
			}
			addBandInfo.put("type", "continue");
			addBandInfo.put("item", arr);
			addBandInfo.put("yPosition", bandInfo.get(arr.get(0).getId()).getY() );
			
			if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
			{
					totalList.add( addBandInfo );
			}
			
		}
		
		
		// 그룹핑 된 Element를 ContinueBand처리
		
		String _type = "";
		HashMap<String, Object> _totalItem;
		
		int _pageTotPage = 0;		// 각 페이지별 최대 페이지를 담기
		int _currentTotPage = 0;	// 각 페이지별 최대 페이지를 담기
		int _max = totalList.size();
		int _subMax = 0;
		ArrayList<String> _tabIndexList = null; 
		ArrayList<String> _requiredList = null; 
				
		_tabIndexList = _page.getTabIndexList();
		_requiredList = _page.getRequiredValueList();

		ArrayList<String> _tabIndexElementList = null;
		ArrayList<String> _requiredElementList = null;
		
		ArrayList<Object> _tabIndexPages = null;
		ArrayList<Object> _requiredPages = null;
		
		for ( i = 0; i < _max; i++) {

			_currentTotPage = 0;
			_totalItem = (HashMap<String, Object>) totalList.get(i);
			_type = _totalItem.get("type").toString();
			ArrayList<BandInfoMapData> items = (ArrayList<BandInfoMapData>) _totalItem.get("item");
			
			// ContinueBand 타입일경우 처리
			if( _type.equals("continue") )
			{
				// page없이 bandList와 bandInfo를 이용하여 총 페이지를 구하도록 변경
				ArrayList<Object> bandAr = continueBandParser.loadTotalPageJson( _page, _data, items, 0f, Float.valueOf(_totalItem.get("height").toString()), pageWidth, mXAr, null );
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(0);
				
				_totalItem.put("bandInfoData", bandAr);
				_currentTotPage = pagesRowList.size();
				_totalItem.put("totPage", _currentTotPage);
				
				if(bandAr.size() > 3)
				{
					ArrayList<ArrayList<HashMap<String, Object>>> _requiredBandList = (ArrayList<ArrayList<HashMap<String, Object>>>) bandAr.get(2);
					ArrayList<ArrayList<HashMap<String, Object>>> _tabIndexBandList = (ArrayList<ArrayList<HashMap<String, Object>>>) bandAr.get(3);
					
					ArrayList<HashMap<String, Object>> _chkPage;
					int _idx = _tabIndexBandList.size();
					int _subIdx = 0;
					int l = 0;
					int n = 0;
					for ( l = 0; l < _idx; l++) {
						_subIdx = _tabIndexBandList.get(l).size();
						if( _tabIndexPages == null )
						{
							_tabIndexPages = new ArrayList<Object>();
						}
						if(_tabIndexPages.size() <= l ) _tabIndexPages.add( new ArrayList<HashMap<String, Object>>());
						_chkPage = (ArrayList<HashMap<String, Object>>) _tabIndexPages.get(l);
						
						for ( n = 0; n < _subIdx; n++) {
							_chkPage.add(_tabIndexBandList.get(l).get(n) );
						}
					}
					
					_idx = _requiredBandList.size();
					
					for ( l = 0; l < _idx; l++) {
						_subIdx = _requiredBandList.get(l).size();
						if( _requiredPages == null )
						{
							_requiredPages = new ArrayList<Object>();
						}
						if(_requiredPages.size() <= l ) _requiredPages.add( new ArrayList<HashMap<String, Object>>());
						_chkPage = (ArrayList<HashMap<String, Object>>) _requiredPages.get(l);
						
						for ( n = 0; n < _subIdx; n++) {
							_chkPage.add(_requiredBandList.get(l).get(n) );
						}
					}
					
				}
				
				// tabindex, 및 필수값 처리 ( 밴드의 경우 continueBand에서 내보내주는 아이템을 사용 )
				
			}
			else if( _type.equals("band") )	// 	그 외 Empty밴드일경우
			{
				if(_totalItem.containsKey("repeat") && _totalItem.get("repeat").equals("y") )
				{
					_currentTotPage = bandPageGetTotalPageJson( items );
				}
				else
				{
					_currentTotPage = 1;
				}
				_totalItem.put("totPage", _currentTotPage);
				
			}
			else if( _type.equals("pageHeader") || _type.equals("pageFooter") )	// PageHeader 일경우 
			{
				_totalItem.put("totPage", 0);
			}
			
			// 필수값및 탭 인덱스 담기
			if( _type.equals("continue") == false && items.size() > 0 )
			{
				if( _tabIndexList.size() > 0 ) _tabIndexElementList = getReqIndexItemsJson( items, _tabIndexList,_tabIndexElementList );
				if( _requiredList.size() > 0 ) _requiredElementList = getReqIndexItemsJson( items, _requiredList,_requiredElementList );
			}
			
			if( _currentTotPage > _pageTotPage )
			{
				_pageTotPage = _currentTotPage;
			}
			
		}
		
		for ( i = 0; i < _max; i++) {
			_totalItem = (HashMap<String, Object>) totalList.get(i);
			_type = _totalItem.get("type").toString();
			
			if( _type.equals("band") )
			{
				// 	그 외 Empty밴드일경우
				if( _totalItem.containsKey("repeat") == false ||  _totalItem.get("repeat").equals("y") ==  false )
				{
					_totalItem.put("totPage", _pageTotPage);
				}
			}
		}
		
		// 필수값 담기
		if( _tabIndexList.size() > 0 || _requiredList.size() > 0 )
		{
			int _subIdx = 0;
			int l = 0;
			int n = 0;
			ArrayList<HashMap<String, Object>> _chkPage;
			
			for ( l = 0; l < _pageTotPage; l++) {
				HashMap<String, Object> _tempMap;
				
				if(_tabIndexElementList != null )
				{
					_subIdx = _tabIndexElementList.size();
					
					if( _tabIndexPages == null )
					{
						_tabIndexPages = new ArrayList<Object>();
					}
					
					if(_tabIndexPages.size() <= l ) _tabIndexPages.add( new ArrayList<HashMap<String, Object>>());
					_chkPage = (ArrayList<HashMap<String, Object>>) _tabIndexPages.get(l);
					
					for ( n = 0; n < _subIdx; n++) {
						
						_tempMap = new HashMap<String, Object>();
						_tempMap.put("id", _tabIndexElementList.get(n));
						
						_chkPage.add( _tempMap );
					}
				}
				
				if(_requiredElementList != null )
				{
					_subIdx = _requiredElementList.size();
					
					if( _requiredPages == null )
					{
						_requiredPages = new ArrayList<Object>();
					}
					
					if(_requiredPages.size() <= l ) _requiredPages.add( new ArrayList<HashMap<String, Object>>());
					_chkPage = (ArrayList<HashMap<String, Object>>) _requiredPages.get(l);
					
					for ( n = 0; n < _subIdx; n++) {
						
						_tempMap = new HashMap<String, Object>();
						_tempMap.put("id", _requiredElementList.get(n));
						_chkPage.add( _tempMap );
					}
				}
					
				
			}
		}
		
		ArrayList<Object> retAr = new ArrayList<Object>();
		retAr.add(_pageTotPage);
		retAr.add(totalList);
		
		retAr.add(_requiredPages);
		retAr.add(_tabIndexPages);

		retAr.add(_groupBandCntMap);
		
		return retAr;
	}
	
	
	public ArrayList<Object> loadTotalPage( PageInfoSimple _page, HashMap<String, ArrayList<HashMap<String, Object>>> _data,float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		return null;
	}
	
	/**
	public ArrayList<Object> loadTotalPage( PageInfoSimple _page, HashMap<String, ArrayList<HashMap<String, Object>>> _data,float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		int i = 0;
		int j = 0;
		int k = 0;
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
		
		HashMap<String, ArrayList<String>> bandSequencyMap = new HashMap<String, ArrayList<String>>();
		ArrayList<String> bandSequency = new ArrayList<String>();
		String _dataBandName = "";
		String _dataSummeryBandName = "";
		String _dataFooterBandName = "";
		
		String cloneData = _page.getClone();
		int _cloneRowCnt = 1;
		boolean clonePage = false;
		float _pageHeight = _page.getHeight();
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
				_cloneRowCnt = _page.getCloneRowCount();
			}
			
			_pageHeight = _pageHeight / _cloneRowCnt;
			clonePage = true;
		}
		
		
		DataSet = _data;	
		int _propertyLength = 0;
		
		// 밴드리스트를 가져와서 그룹핑
		
		boolean _grpFlg = false;
		boolean _dataFlag = false;
		ArrayList<Object> totalList = new ArrayList<Object>();	// 그룹핑된 배열을 담아둘 객체
		ArrayList<BandInfoMapDataSimple> arr = new ArrayList<BandInfoMapDataSimple>();
		float _argoY 		= 0;
		float _argoBandY 	= 0;
		float _pageHeaderHeight	= 0;
		HashMap<String, Object> addBandInfo = new HashMap<String, Object>();
		float _headerPosition = 0;
		int _bandListLength = 0;
		ArrayList<BandInfoMapDataSimple> bandList = _page.getBandList();
		BandInfoMapDataSimple item;
		HashMap<String, BandInfoMapDataSimple> bandInfo = _page.getBandInfoData();
		HashMap<String, Integer> _groupBandCntMap = _page.getGgroupBandCntMap();			// 밴드의 총 그룹갯수를 담아두는 객체
		
		
		_bandListLength = bandList.size();
		String _itemID = "";
		
		for ( i = 0; i < _bandListLength; i++) {
			
			item = bandList.get(i);
			_className = item.getClassName();
			_itemID = item.getId();
			
			if( _grpFlg && _className.equals("UBGroupFooterBand") ) _grpFlg = false;
			
			
			if( _className.equals("UBDataPageFooterBand") == false )
			{
				
				if( _className.equals("UBPageHeaderBand") )
				{
					arr.add(item);
					
					addBandInfo = new HashMap<String, Object>();
					
					addBandInfo.put("type", "pageHeader");
					addBandInfo.put("item", arr);
					addBandInfo.put("height", Float.valueOf( bandInfo.get( _itemID ).getHeight() ));
					addBandInfo.put("yPosition", _headerPosition );
					addBandInfo.put("repeat", bandInfo.get( _itemID ).getRepeat() );
					
					if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
					{
							totalList.add( addBandInfo );
					}
					
					arr = new ArrayList<BandInfoMapDataSimple>();
					
					_headerPosition = _headerPosition +  bandInfo.get(_itemID).getHeight();
					_pageHeaderHeight = _pageHeaderHeight +  bandInfo.get(_itemID).getHeight();
					_argoY = bandInfo.get(_itemID).getY() + bandInfo.get(_itemID).getHeight();
				}
				
				if( _className.equals("UBPageFooterBand") || _className.equals("UBEmptyBand") )
				{
					if( arr.size() > 0 )
					{
						if( bandList.get(i-1).getClassName().equals("UBGroupFooterBand") )
						{
							_argoBandY =  bandInfo.get(bandList.get(i-1).getId()).getY()  +  bandInfo.get(bandList.get(i-1).getId()).getHeight();
						}
						else
						{
							_argoBandY = bandInfo.get(_itemID ).getY();
						}
						
						addBandInfo = new HashMap<String, Object>();
						
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", _argoBandY-_argoY);
						addBandInfo.put("yPosition", bandInfo.get(arr.get(0).getId()).getY());
						
						if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
						{
								totalList.add( addBandInfo );
						}
						arr = new ArrayList<BandInfoMapDataSimple>();
					}
					
					addBandInfo = new HashMap<String, Object>();
					
					arr.add(item);
					addBandInfo.put("type", "band");
					addBandInfo.put("item", arr);
					addBandInfo.put("height", bandInfo.get(_itemID).getHeight() );
					addBandInfo.put("yPosition", bandInfo.get(_itemID).getY() );
					
					if( bandInfo.get( _itemID ).getRepeat() != null &&  !bandInfo.get( _itemID ).getRepeat().equals("") ) addBandInfo.put("repeat", bandInfo.get( _itemID ).getRepeat() );
					else addBandInfo.put("repeat", "n" );
					
					if( _className.equals("UBPageFooterBand") )
					{
						addBandInfo.put("type", "pageFooter");
						if( clonePage )
						{
							float _footerBandYposition = _pageHeight -  Float.valueOf( addBandInfo.get("height").toString() );
							addBandInfo.put("yPosition",_footerBandYposition);
						}
					}
					else
					{
						addBandInfo.put("type", "band");
					}
					
					if( _className.equals("UBPageFooterBand") || (  Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() ) ) )
					{
							totalList.add( addBandInfo );
					}
					
					_dataFlag = false;
					_grpFlg = false;
					arr = new ArrayList<BandInfoMapDataSimple>();
					
				}
				else if( _className.equals("UBCrossTabBand") )
				{
					if( arr.size() > 0 )
					{
						if( bandList.get(i-1).getClassName().equals("UBGroupFooterBand") )
						{
							_argoBandY =  bandInfo.get(bandList.get(i-1).getId()).getY()  +  bandInfo.get(_itemID).getHeight();
						}
						else
						{
							_argoBandY = bandInfo.get(_itemID).getY();
						}
						
						addBandInfo = new HashMap<String, Object>();
						
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", _argoBandY- bandInfo.get(arr.get(0).getId()).getY() );
						addBandInfo.put("yPosition", bandInfo.get(arr.get(0).getId()).getY() );
						
						if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
						{
								totalList.add( addBandInfo );
						}
						
						arr = new ArrayList<BandInfoMapDataSimple>();
						
					}
					addBandInfo = new HashMap<String, Object>();
					
					if( i == bandList.size()-1 )
					{
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", pageHeight );
						addBandInfo.put("yPosition", bandInfo.get(_itemID).getY() );
					}
					else
					{
						addBandInfo.put("type", "continue");
						addBandInfo.put("item", arr);
						addBandInfo.put("height", bandInfo.get(bandList.get(i+1).getId()).getY() - bandInfo.get(_itemID).getY() );
						addBandInfo.put("yPosition", bandInfo.get(_itemID).getY() );
					}

					if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
					{
							totalList.add( addBandInfo );
					}
					
				}
				else if( _className.equals("UBPageHeaderBand") == false )
				{
					if( (!_grpFlg && _className.equals("UBDataHeaderBand")) || (!_grpFlg&&_className.equals("UBDataBand")&&_dataFlag) ||
							_className.equals("UBGroupHeaderBand"))
					{
						
						if( _className.equals("UBGroupHeaderBand") )
						{
							_dataFlag = false;
							_grpFlg = true;
						}
						
						if( arr.size() > 0 )
						{
							
							if( bandList.get(i-1).getClassName().equals("UBGroupFooterBand") )
							{
								_argoBandY = bandInfo.get(bandList.get(i-1).getId()).getY() + bandInfo.get(bandList.get(i-1).getId()).getHeight();
							}
							else
							{
								_argoBandY = bandInfo.get(_itemID).getY();
							}
							
							addBandInfo = new HashMap<String, Object>();
							
							addBandInfo.put("type", "continue");
							addBandInfo.put("item", arr);
							addBandInfo.put("height", _argoBandY - _argoY );
							addBandInfo.put("yPosition", bandInfo.get(arr.get(0).getId()).getY() );
							
							if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )							
							{
									totalList.add( addBandInfo );
							}
							
							_dataFlag = false;
							
						}
						
						arr = new ArrayList<BandInfoMapDataSimple>();
						_argoY = bandInfo.get(_itemID).getY();
						
					}
					
					if( _className.equals("UBDataBand") )
					{
						ArrayList<String> bandSequence = bandInfo.get(_itemID).getSequence();
						int _sequenceLength = bandSequence.size();
						for ( j = 0; j < _sequenceLength; j++) {
							
							if( bandSequence.get(j).equals("f") && bandInfo.get(_itemID).getFooter()!= null  && bandInfo.get(_itemID).getFooter().equals("") == false )
							{
								arr.add( bandInfo.get( bandInfo.get(_itemID).getFooter() ) );
							}
							else if( bandSequence.get(j).equals("d") )
							{
								arr.add(item);
							}
							else if( bandSequence.get(j).equals("s") && bandInfo.get(_itemID).getSummery() != null  && bandInfo.get(_itemID).getSummery() .equals("") == false )
							{
								arr.add( bandInfo.get( bandInfo.get(_itemID).getSummery() ) );
							}
							
						}
						
						_dataFlag = true;
					}
					else
					{
						arr.add(item);
					}
					
				}
				
			}
			
		}
		
		
		
		
		if( arr.size() > 0 )
		{
			addBandInfo = new HashMap<String, Object>();
			if( arr.get(arr.size()-1).getClassName().equals("UBGroupFooterBand")  )
			{
				addBandInfo.put("height", (  bandInfo.get(arr.get(arr.size()-1).getId()).getY() + bandInfo.get(arr.get(arr.size()-1).getId()).getHeight() )-  bandInfo.get(arr.get(0).getId()).getY() );
			}
			else
			{
				addBandInfo.put("height", pageHeight  - bandInfo.get(arr.get(0).getId()).getY() );
			}
			addBandInfo.put("type", "continue");
			addBandInfo.put("item", arr);
			addBandInfo.put("yPosition", bandInfo.get(arr.get(0).getId()).getY() );
			
			if( Float.valueOf( addBandInfo.get("height").toString() ) > 0 && pageHeight > Float.valueOf( addBandInfo.get("yPosition").toString() )  )
			{
					totalList.add( addBandInfo );
			}
			
		}
		
		
		// 그룹핑 된 Element를 ContinueBand처리
		
		String _type = "";
		HashMap<String, Object> _totalItem;
		
		int _pageTotPage = 0;		// 각 페이지별 최대 페이지를 담기
		int _currentTotPage = 0;	// 각 페이지별 최대 페이지를 담기
		int _max = totalList.size();
		int _subMax = 0;
		ArrayList<String> _tabIndexList = null; 
		ArrayList<String> _requiredList = null; 
				
		_tabIndexList = _page.getTabIndexList();
		_requiredList = _page.getRequiredValueList();

		ArrayList<String> _tabIndexElementList = null;
		ArrayList<String> _requiredElementList = null;
		
		ArrayList<Object> _tabIndexPages = null;
		ArrayList<Object> _requiredPages = null;
		
		for ( i = 0; i < _max; i++) {

			_currentTotPage = 0;
			_totalItem = (HashMap<String, Object>) totalList.get(i);
			_type = _totalItem.get("type").toString();
			ArrayList<BandInfoMapData> items = (ArrayList<BandInfoMapData>) _totalItem.get("item");
			
			// ContinueBand 타입일경우 처리
			if( _type.equals("continue") )
			{
				// page없이 bandList와 bandInfo를 이용하여 총 페이지를 구하도록 변경
				ArrayList<Object> bandAr = continueBandParser.loadTotalPages( _page, _data, items, 0f, Float.valueOf(_totalItem.get("height").toString()), pageWidth, mXAr, null );
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(0);
				
				_totalItem.put("bandInfoData", bandAr);
				_currentTotPage = pagesRowList.size();
				_totalItem.put("totPage", _currentTotPage);
				
				if(bandAr.size() > 3)
				{
					ArrayList<ArrayList<HashMap<String, Object>>> _requiredBandList = (ArrayList<ArrayList<HashMap<String, Object>>>) bandAr.get(2);
					ArrayList<ArrayList<HashMap<String, Object>>> _tabIndexBandList = (ArrayList<ArrayList<HashMap<String, Object>>>) bandAr.get(3);
					
					ArrayList<HashMap<String, Object>> _chkPage;
					int _idx = _tabIndexBandList.size();
					int _subIdx = 0;
					int l = 0;
					int n = 0;
					for ( l = 0; l < _idx; l++) {
						_subIdx = _tabIndexBandList.get(l).size();
						if( _tabIndexPages == null )
						{
							_tabIndexPages = new ArrayList<Object>();
						}
						if(_tabIndexPages.size() <= l ) _tabIndexPages.add( new ArrayList<HashMap<String, Object>>());
						_chkPage = (ArrayList<HashMap<String, Object>>) _tabIndexPages.get(l);
						
						for ( n = 0; n < _subIdx; n++) {
							_chkPage.add(_tabIndexBandList.get(l).get(n) );
						}
					}
					
					_idx = _requiredBandList.size();
					
					for ( l = 0; l < _idx; l++) {
						_subIdx = _requiredBandList.get(l).size();
						if( _requiredPages == null )
						{
							_requiredPages = new ArrayList<Object>();
						}
						if(_requiredPages.size() <= l ) _requiredPages.add( new ArrayList<HashMap<String, Object>>());
						_chkPage = (ArrayList<HashMap<String, Object>>) _requiredPages.get(l);
						
						for ( n = 0; n < _subIdx; n++) {
							_chkPage.add(_requiredBandList.get(l).get(n) );
						}
					}
					
				}
				
				// tabindex, 및 필수값 처리 ( 밴드의 경우 continueBand에서 내보내주는 아이템을 사용 )
				
			}
			else if( _type.equals("band") )	// 	그 외 Empty밴드일경우
			{
				if(_totalItem.containsKey("repeat") && _totalItem.get("repeat").equals("y") )
				{
					_currentTotPage = bandPageGetTotalPageJson( items );
				}
				else
				{
					_currentTotPage = 1;
				}
				_totalItem.put("totPage", _currentTotPage);
				
			}
			else if( _type.equals("pageHeader") || _type.equals("pageFooter") )	// PageHeader 일경우 
			{
				_totalItem.put("totPage", 0);
			}
			
			// 필수값및 탭 인덱스 담기
			if( _type.equals("continue") == false && items.size() > 0 )
			{
				if( _tabIndexList.size() > 0 ) _tabIndexElementList = getReqIndexItemsJson( items, _tabIndexList,_tabIndexElementList );
				if( _requiredList.size() > 0 ) _requiredElementList = getReqIndexItemsJson( items, _requiredList,_requiredElementList );
			}
			
			if( _currentTotPage > _pageTotPage )
			{
				_pageTotPage = _currentTotPage;
			}
			
		}
		
		for ( i = 0; i < _max; i++) {
			_totalItem = (HashMap<String, Object>) totalList.get(i);
			_type = _totalItem.get("type").toString();
			
			if( _type.equals("band") )
			{
				// 	그 외 Empty밴드일경우
				if( _totalItem.containsKey("repeat") == false ||  _totalItem.get("repeat").equals("y") ==  false )
				{
					_totalItem.put("totPage", _pageTotPage);
				}
			}
		}
		
		// 필수값 담기
		if( _tabIndexList.size() > 0 || _requiredList.size() > 0 )
		{
			int _subIdx = 0;
			int l = 0;
			int n = 0;
			ArrayList<HashMap<String, Object>> _chkPage;
			
			for ( l = 0; l < _pageTotPage; l++) {
				HashMap<String, Object> _tempMap;
				
				if(_tabIndexElementList != null )
				{
					_subIdx = _tabIndexElementList.size();
					
					if( _tabIndexPages == null )
					{
						_tabIndexPages = new ArrayList<Object>();
					}
					
					if(_tabIndexPages.size() <= l ) _tabIndexPages.add( new ArrayList<HashMap<String, Object>>());
					_chkPage = (ArrayList<HashMap<String, Object>>) _tabIndexPages.get(l);
					
					for ( n = 0; n < _subIdx; n++) {
						
						_tempMap = new HashMap<String, Object>();
						_tempMap.put("id", _tabIndexElementList.get(n));
						
						_chkPage.add( _tempMap );
					}
				}
				
				if(_requiredElementList != null )
				{
					_subIdx = _requiredElementList.size();
					
					if( _requiredPages == null )
					{
						_requiredPages = new ArrayList<Object>();
					}
					
					if(_requiredPages.size() <= l ) _requiredPages.add( new ArrayList<HashMap<String, Object>>());
					_chkPage = (ArrayList<HashMap<String, Object>>) _requiredPages.get(l);
					
					for ( n = 0; n < _subIdx; n++) {
						
						_tempMap = new HashMap<String, Object>();
						_tempMap.put("id", _requiredElementList.get(n));
						_chkPage.add( _tempMap );
					}
				}
					
				
			}
		}
		
		ArrayList<Object> retAr = new ArrayList<Object>();
		retAr.add(_pageTotPage);
		retAr.add(totalList);
		
		retAr.add(_requiredPages);
		retAr.add(_tabIndexPages);

		retAr.add(_groupBandCntMap);
		
		return retAr;
	}
	*/
	
	
	private Integer bandPageGetTotalPage( ArrayList<Element> pageItems  )
	{
		int i = 0;
		int j = 0;
		int k = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		Element bandProperty = null;
		Element cellProperty = null;
		NodeList bandPropertys = null;
		
		String _dataType = "";
		String _dataSet = "";
		String _className = "";
		
		int totalPage = 0;
		int _bandPropertyLength = 0;
		int _cellPropertyLength = 0;
		
		for ( i = 0; i < pageItems.size(); i++) {
			
			_dataType = "";
			_dataSet = "";
			
			_className = pageItems.get(i).getAttribute("className");
			if( _className.length() < 4 || !_className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) 
			{
				
				if(_className.equals("UBTable"))
				{
					bandPropertys = ((Element) pageItems.get(i)).getElementsByTagName("cell");
					_bandPropertyLength = bandPropertys.getLength();
					for ( j = 0; j < _bandPropertyLength; j++) {
						
						NodeList cellList = ((Element) bandPropertys.item(j)).getElementsByTagName("property");
						_cellPropertyLength = cellList.getLength();
						for ( k = 0; k < _cellPropertyLength; k++) {
							
							cellProperty = (Element) cellList.item(k);
							if( cellProperty.getAttribute("name").equals("dataType") )
							{
								_dataType = cellProperty.getAttribute("value");
							}
							else if(  cellProperty.getAttribute("name").equals("dataSet") )
							{
								_dataSet = cellProperty.getAttribute("value");
							}
							
						}
						
						if( _dataType.equals("1") || _dataType.equals("2") )
						{
							if( DataSet.containsKey(_dataSet) && DataSet.get(_dataSet).size() > totalPage )
							{
								totalPage = DataSet.get(_dataSet).size();
							}
						}
					}
					
					
					
				}
				else
				{
					try {
//					bandPropertys = (NodeList) _xpath.evaluate("./property[@name='dataType']|./property[@name='dataSet']", pageItems.get(i), XPathConstants.NODESET);
						bandPropertys = ((Element) pageItems.get(i)).getElementsByTagName("property");
						_bandPropertyLength = bandPropertys.getLength();
						for ( j = 0; j < _bandPropertyLength; j++) {
							
							bandProperty =(Element) bandPropertys.item(j);
							if( bandProperty.getAttribute("name").equals("dataType") )
							{
								_dataType = bandProperty.getAttribute("value");
							}
							else if(  bandProperty.getAttribute("name").equals("dataSet") )
							{
								_dataSet = bandProperty.getAttribute("value");
							}
							
						}
						
						if( _dataType.equals("1") || _dataType.equals("2") )
						{
							if( DataSet.containsKey(_dataSet) && DataSet.get(_dataSet).size() > totalPage )
							{
								totalPage = DataSet.get(_dataSet).size();
							}
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				
			}// Band가 아닐경우 처리
			
		}
		
		
		return totalPage;
	}
	

	
	private Integer bandPageGetTotalPageJson( ArrayList<BandInfoMapData> _bands  )
	{
		int i = 0;
		int j = 0;
		
		String _dataType = "";
		String _dataSet = "";
		String _className = "";
		
		int totalPage = 0;
		HashMap<String, Value> _item;
		ArrayList<HashMap<String, Value>> pageItems;
		
		for( j=0; j < _bands.size(); j++ )
		{
			pageItems = _bands.get(j).getChildren();
			
			for ( i = 0; i < pageItems.size(); i++) {
				
				_dataType = "";
				_dataSet = "";
				
				_className = pageItems.get(i).get("className").getStringValue();
				if( _className.length() < 4 || !_className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) 
				{
					_item = pageItems.get(i);
					
					if( _item.containsKey("dataType") )
						_dataType 	= _item.get("dataType").getStringValue();
					_dataSet 	= _item.get("dataSet").getStringValue();
					
					if( _dataType.equals("1") || _dataType.equals("2") )
					{
						if( DataSet.containsKey(_dataSet) && DataSet.get(_dataSet).size() > totalPage )
						{
							totalPage = DataSet.get(_dataSet).size();
						}
					}
					
				}// Band가 아닐경우 처리
			}
			
		}
		
		
		
		
		return totalPage;
	}
	
	
	public ArrayList<HashMap<String, Object>> createMasterBandData(int _page, ArrayList<Object> totalList, float pageWidth, float pageHeight, HashMap<String, Object> _param, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		
		String _type = "";
		HashMap<String, Object> _totalItem = null;
		int currentTotPage = 0;
		float _headerPosition = 0;
		float _yPosition = 0;
		_headerPosition = _cloneY;
		int _max = totalList.size();
		boolean isPivot = false;
		
		for (int i = 0; i < _max; i++) {
			
			_totalItem = (HashMap<String, Object>) totalList.get(i);
			_type = _totalItem.get("type").toString();
			currentTotPage = (Integer) _totalItem.get("totPage");
			_yPosition = _cloneY +  (Float) _totalItem.get("yPosition");
			ArrayList<Element> pageItems = (ArrayList<Element>) _totalItem.get("pageItems");
			
			if( _type.equals("continue") )
			{
				ArrayList<Object> bandAr = (ArrayList<Object>) _totalItem.get("bandInfoData");
				HashMap<String, BandInfoMapData> bandInfo = (HashMap<String, BandInfoMapData>) bandAr.get(0);
				ArrayList<BandInfoMapData> bandList =(ArrayList<BandInfoMapData>) bandAr.get(1);
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
				
				// group함수를 위한 객체 function처리하기 위하여 페이지 이동 함수로 전달하여 진행
				HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);				// originalData값으 가지고 있는 객체
				ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
				
				if( _page < pagesRowList.size() )
				{
					if(continueBandParser == null)
					{
						continueBandParser = new ContinueBandParser(m_appParams);
						continueBandParser.DataSet = this.DataSet;
					}
					
					// 그룹합수용 데이터를 매핑
					continueBandParser.mOriginalDataMap = originalDataMap;
					continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
					continueBandParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
					continueBandParser.setChangeItemList(changeItemList);
					continueBandParser.setIsExportType( isExportType );
					continueBandParser.setExportData(isExportData);
					
					_objects = continueBandParser.createContinueBandItems(_page, DataSet, bandInfo, bandList, pagesRowList, _param, crossTabData, _cloneX, _yPosition,_objects,_totalPageNum, _currentPageNum,isPivot);
				}
				
			}
			else if( _type.equals("band") )	// 	그 외 Empty밴드일경우
			{
				if( _page <= currentTotPage  )
				{
					_objects = createFreeFormType(_page, pageItems, _param, _cloneX, _yPosition, _objects, -1 , _totalPageNum, _currentPageNum );
				}
			}
			else if( _type.equals("pageHeader") ||  _type.equals("pageFooter") )	// PageHeader 일경우 
			{
				
				if( _type.equals("pageHeader") )
				{
					if( ( _page == 0 || _totalItem.get("repeat").equals("y") ) )
					{
						_headerPosition = _headerPosition + mPageMarginTop;
						
						_objects = createFreeFormType(0, pageItems, _param, _cloneX, _yPosition, _objects, _headerPosition , _totalPageNum , _currentPageNum);
						_headerPosition = _headerPosition + (Float) _totalItem.get("height");
					}
				}
				else
				{
					_objects = createFreeFormType(0, pageItems, _param, _cloneX, _yPosition, _objects, -1 , _totalPageNum  , _currentPageNum);
				}
			}
			
		}
		
		
		
		return _objects;
	}
	
	public ArrayList<HashMap<String, Object>> createMasterBandDataJson(int _page, PageInfo _pageInfo, ArrayList<Object> totalList, float pageWidth, float pageHeight, HashMap<String, Object> _param, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		
		String _type = "";
		HashMap<String, Object> _totalItem = null;
		int currentTotPage = 0;
		float _headerPosition = 0;
		float _yPosition = 0;
		_headerPosition = _cloneY;
		int _max = totalList.size();
		boolean isPivot = false;
		ArrayList<BandInfoMapData> _itemList;
		
		for (int i = 0; i < _max; i++) {
			
			_totalItem = (HashMap<String, Object>) totalList.get(i);
			_type = _totalItem.get("type").toString();
			currentTotPage = (Integer) _totalItem.get("totPage");
			_yPosition = _cloneY +  (Float) _totalItem.get("yPosition");
			
			_itemList = (ArrayList<BandInfoMapData>) _totalItem.get("item");
			
			if( _type.equals("continue") )
			{
				ArrayList<Object> bandAr = (ArrayList<Object>) _totalItem.get("bandInfoData");
				HashMap<String, BandInfoMapData> bandInfo = _pageInfo.getBandInfoData();
				ArrayList<BandInfoMapData> bandList =_itemList;
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(0);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(1);
				
				// group함수를 위한 객체 function처리하기 위하여 페이지 이동 함수로 전달하여 진행
				HashMap<String, String> originalDataMap = _pageInfo.getOriginalDataMap();				// originalData값으 가지고 있는 객체
				ArrayList<ArrayList<String>> groupDataNamesAr = _pageInfo.getGroupDataNamesAr();					// 그룹핑된 데이터명을 가지고 있는 객체
				
				if( _page < pagesRowList.size() )
				{
					if(continueBandParser == null)
					{
						continueBandParser = new ContinueBandParser(m_appParams);
						continueBandParser.DataSet = this.DataSet;
					}
					
					// 그룹합수용 데이터를 매핑
					continueBandParser.mOriginalDataMap = originalDataMap;
					continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
					continueBandParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
					continueBandParser.setChangeItemList(changeItemList);
					continueBandParser.setIsExportType( isExportType );
					continueBandParser.setExportData(isExportData);
					
					_objects = continueBandParser.createContinueBandItems(_page, DataSet, bandInfo, bandList, pagesRowList, _param, crossTabData, _cloneX, _yPosition,_objects,_totalPageNum, _currentPageNum,isPivot);
				}
				
			}
			else if( _type.equals("band") )	// 	그 외 Empty밴드일경우
			{
				if( _page <= currentTotPage  )
				{
					_objects = createFreeFormTypeJson(_page, _itemList, _param, _cloneX, _yPosition, _objects, -1 , _totalPageNum, _currentPageNum );
				}
			}
			else if( _type.equals("pageHeader") ||  _type.equals("pageFooter") )	// PageHeader 일경우 
			{
				
				if( _type.equals("pageHeader") )
				{
					if( ( _page == 0 || _totalItem.get("repeat").equals("y") ) )
					{
						_headerPosition = _headerPosition + mPageMarginTop;
						
						_objects = createFreeFormTypeJson(_page, _itemList, _param, _cloneX, _yPosition, _objects, _headerPosition , _totalPageNum, _currentPageNum );
						_headerPosition = _headerPosition + (Float) _totalItem.get("height");
					}
				}
				else
				{
					_objects = createFreeFormTypeJson(_page, _itemList, _param, _cloneX, _yPosition, _objects, -1 , _totalPageNum, _currentPageNum );
				}
			}
			
		}
		
		return _objects;
	}

	public ArrayList<HashMap<String, Object>> createMasterBandDataSimple(int _page, PageInfoSimple _pageInfo, ArrayList<Object> totalList, float pageWidth, float pageHeight, HashMap<String, Object> _param, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		
		String _type = "";
		HashMap<String, Object> _totalItem = null;
		int currentTotPage = 0;
		float _headerPosition = 0;
		float _yPosition = 0;
		_headerPosition = _cloneY;
		int _max = totalList.size();
		boolean isPivot = false;
		ArrayList<BandInfoMapDataSimple> _itemList;
		
		for (int i = 0; i < _max; i++) {
			
			_totalItem = (HashMap<String, Object>) totalList.get(i);
			_type = _totalItem.get("type").toString();
			currentTotPage = (Integer) _totalItem.get("totPage");
			_yPosition = _cloneY +  (Float) _totalItem.get("yPosition");
			
			_itemList = (ArrayList<BandInfoMapDataSimple>) _totalItem.get("item");
			
			if( _type.equals("continue") )
			{
				ArrayList<Object> bandAr = (ArrayList<Object>) _totalItem.get("bandInfoData");
				HashMap<String, BandInfoMapDataSimple> bandInfo = _pageInfo.getBandInfoData();
				ArrayList<BandInfoMapDataSimple> bandList =_itemList;
				ArrayList<ArrayList<HashMap<String, Object>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Object>>>) bandAr.get(0);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Object>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Object>>> >) bandAr.get(1);
				
				// group함수를 위한 객체 function처리하기 위하여 페이지 이동 함수로 전달하여 진행
				HashMap<String, String> originalDataMap = _pageInfo.getOriginalDataMap();				// originalData값으 가지고 있는 객체
				ArrayList<ArrayList<String>> groupDataNamesAr = _pageInfo.getGroupDataNamesAr();					// 그룹핑된 데이터명을 가지고 있는 객체
				
				if( _page < pagesRowList.size() )
				{
					if(continueBandParser == null)
					{
						continueBandParser = new ContinueBandParser(m_appParams);
						continueBandParser.DataSet = this.DataSet;
					}
					
					// 그룹합수용 데이터를 매핑
					continueBandParser.mOriginalDataMap = originalDataMap;
					continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
					continueBandParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
					continueBandParser.setChangeItemList(changeItemList);
					continueBandParser.setIsExportType( isExportType );
					continueBandParser.setExportData(isExportData);
					
					_objects = continueBandParser.createContinueBandItemsSimple(_page, DataSet, bandInfo, bandList, pagesRowList, _param, crossTabData, _cloneX, _yPosition,_objects,_totalPageNum, _currentPageNum,isPivot);
				}
				
			}
			else if( _type.equals("band") )	// 	그 외 Empty밴드일경우
			{
				if( _page <= currentTotPage  )
				{
					_objects = createFreeFormTypeSimple(_page, _itemList, _param, _cloneX, _yPosition, _objects, -1 , _totalPageNum, _currentPageNum );
				}
			}
			else if( _type.equals("pageHeader") ||  _type.equals("pageFooter") )	// PageHeader 일경우 
			{
				
				if( _type.equals("pageHeader") )
				{
					if( ( _page == 0 || _totalItem.get("repeat").equals("y") ) )
					{
						_headerPosition = _headerPosition + mPageMarginTop;
						
						_objects = createFreeFormTypeSimple(_page, _itemList, _param, _cloneX, _yPosition, _objects, _headerPosition , _totalPageNum, _currentPageNum );
						_headerPosition = _headerPosition + (Float) _totalItem.get("height");
					}
				}
				else
				{
					_objects = createFreeFormTypeSimple(_page, _itemList, _param, _cloneX, _yPosition, _objects, -1 , _totalPageNum, _currentPageNum );
				}
			}
			
		}
		
		return _objects;
	}
	
	
	
	private ArrayList<HashMap<String, Object>> createFreeFormType( int _page, ArrayList<Element> itemList, HashMap<String, Object> _param, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects, float _headerPosition , int _totalPageNum , int _currentPageNum ) throws ScriptException
	{
		
		String _className = "";
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		Element propertyData = null;
		float _updateY = 0;
		// dataConvertParser 생성
		ItemConvertParser dataItemParser = new ItemConvertParser(DataSet, "", m_appParams);
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		if(mFunction != null)
		{
			mFunction.setParam(_param);
			dataItemParser.setFunction(this.mFunction);
		}
		dataItemParser.setChangeItemList(changeItemList);
		dataItemParser.setIsExportType(isExportType);
		
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		
		//Table용 변수
		int colIndex = 0;
		int rowIndex = 0;
		float updateX = 0;
		float updateY = 0;
		
		NodeList _tablePropertys = null;
		Element _ItemProperty = null;
		String _propertyName;
		//Table용 변수 End
		
		NodeList _propertyAr = null;
		
		HashMap<String, Object> convertItemData = null;
		
		int _max = itemList.size();
		int _subMax = 0;
		
		ArrayList<HashMap<String, Object>> _radioButtons = new ArrayList<HashMap<String, Object>>();
		
		for ( i = 0; i < _max; i++) {
			
			_className = itemList.get(i).getAttribute("className");
			if( _className.length() < 4 || !_className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) 
			{
				try {
					if( _headerPosition > -1 ) _cloneY = _headerPosition;
					
					// Band_y값을 가져와서 y포지션 업데이트
					
					_propertyAr = ((Element) itemList.get(i)).getElementsByTagName("property");
					_updateY = -1;
//					_subMax = _propertyAr.getLength();
//					for ( j = 0; j < _subMax; j++) {
//						propertyData = (Element) _propertyAr.item(j);
//						if(propertyData.getAttribute("name").equals("band_y"))
//						{
//							_updateY = _cloneY + Float.valueOf( propertyData.getAttribute("value") );
//							break;
//						}
//					}
					
//					_updateY = _cloneY;
					/**
					try {
						propertyData = (Element) _xpath.evaluate("./property[@name='band_y']",(Element) itemList.get(i), XPathConstants.NODE);
					} catch (XPathExpressionException e) {
						e.printStackTrace();
						propertyData = null;
						_updateY = -1;
					}
					
					if( propertyData != null )
					{
						_updateY = _cloneY + Float.valueOf( propertyData.getAttribute("value") );
					}
					**/
					//Table 처리
					if( _className.equals("UBTable")  || _className.equals("UBApproval"))
					{
							_objects = dataItemParser.convertElementTableToItem(itemList.get(i), _page, DataSet, _param, _cloneX, _cloneY, _updateY, _objects, _totalPageNum , _currentPageNum, false);
					}
					else if( _className.equals("UBTemplateArea") )
					{	
						_objects = mTempletInfo.get( ((Element) itemList.get(i)).getAttribute("id") ).convertItemData( _page, _cloneX, _cloneY, _objects, mFunction, null, _currentPageNum, _totalPageNum, -1, -1, changeItemList, null, dataItemParser);
					}
					else
					{
						//라벨 아이템 처리
						convertItemData = dataItemParser.convertElementToItem(itemList.get(i), _page, DataSet, _param, _cloneX, _cloneY, _updateY  , _totalPageNum , _currentPageNum, false);
						//Y좌표 업데이트 
						if( convertItemData != null ){
							convertItemData.put("y", _cloneY +Float.valueOf(convertItemData.get("band_y").toString()) );		// Empty 나 pageFooter밴드의 경우 Y좌표가 아이템의 Y좌표가 아니라 Band의 Y좌표에 맞춰서 이동이 되어야함.
						}
						
						// 아이템이 null일경우 add시키지 않음 2015-12-02 
						if( convertItemData != null )
						{
							//RadioButtonGroup은 RadioButton보다 앞서서 생성되어야 한다
							if(_className.equals("UBRadioButtonGroup")) _objects.add(0, convertItemData);
							else _objects.add(convertItemData);
							
							if( _className.equals("UBRadioBorder") )
							{
								_radioButtons.add(convertItemData);
							}
						}
						
					}
					
						
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
			}
			
		}
		
		if( _radioButtons.size() > 0 )
		{
			for ( i = 0; i < _radioButtons.size(); i++) {
				Boolean _isSelected = dataItemParser.radiobuttonHandler(_radioButtons.get(i));
				_radioButtons.get(i).put("selected", _isSelected);
			}
			_radioButtons.clear();
		}
		
		return _objects;
	}
	
	private ArrayList<HashMap<String, Object>> createFreeFormTypeJson( int _page, ArrayList<BandInfoMapData> bandList, HashMap<String, Object> _param, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects, float _headerPosition , int _totalPageNum , int _currentPageNum ) throws ScriptException
	{
		
		String _className = "";
		int i = 0;
		int j = 0;
		float _updateY = 0;
		// dataConvertParser 생성
		ItemConvertParser dataItemParser = new ItemConvertParser(DataSet, "", m_appParams);
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		if(mFunction != null)
		{
			mFunction.setParam(_param);
			dataItemParser.setFunction(this.mFunction);
		}
		dataItemParser.setChangeItemList(changeItemList);
		dataItemParser.setIsExportType(isExportType);
		
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		
		//Table용 변수
		int colIndex = 0;
		int rowIndex = 0;
		float updateX = 0;
		float updateY = 0;
		
		NodeList _tablePropertys = null;
		Element _ItemProperty = null;
		String _propertyName;
		//Table용 변수 End
		
		NodeList _propertyAr = null;
		
		HashMap<String, Object> convertItemData = null;
		
		int _bandSize =  bandList.size();
		
		int _subMax = 0;
		
		ArrayList<HashMap<String, Object>> _radioButtons = new ArrayList<HashMap<String, Object>>();
		
		ArrayList<HashMap<String, Value>> _items;
		for(j=0; j < _bandSize; j++ )
		{
			_items = bandList.get(j).getChildren();
			int _max = _items.size();
			
			for ( i = 0; i < _max; i++) {
				
				_className = _items.get(i).get("className").getStringValue();
				if( _className.length() < 4 || !_className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) 
				{
					try {
						if( _headerPosition > -1 ) _cloneY = _headerPosition;
						
						// Band_y값을 가져와서 y포지션 업데이트
						_updateY = -1;
						
						//Table 처리
						if( _className.equals("UBTemplateArea") )
						{	
							_objects = mTempletInfo.get( _items.get(i).get("id").getStringValue() ).convertItemData( _page, _cloneX, _cloneY, _objects, mFunction, null, _currentPageNum, _totalPageNum, -1, -1, changeItemList, null, dataItemParser);
						}
						else
						{
							//라벨 아이템 처리
							convertItemData = dataItemParser.convertItemDataJson(null, _items.get(i), DataSet, _page, _param, -1, -1  , _totalPageNum , _currentPageNum, "", 0, 0, -1);
							//Y좌표 업데이트 
							if( convertItemData != null ){
								convertItemData.put("y", _cloneY +Float.valueOf(convertItemData.get("band_y").toString()) );		// Empty 나 pageFooter밴드의 경우 Y좌표가 아이템의 Y좌표가 아니라 Band의 Y좌표에 맞춰서 이동이 되어야함.
							}
							
							// 아이템이 null일경우 add시키지 않음 2015-12-02 
							if( convertItemData != null )
							{
								//RadioButtonGroup은 RadioButton보다 앞서서 생성되어야 한다
								if(_className.equals("UBRadioButtonGroup")) _objects.add(0, convertItemData);
								else _objects.add(convertItemData);
								
								if( _className.equals("UBRadioBorder") )
								{
									_radioButtons.add(convertItemData);
								}
							}
						}
						
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
				}
				
			}
			
			
			
		}
		
		
		
		
		if( _radioButtons.size() > 0 )
		{
			for ( i = 0; i < _radioButtons.size(); i++) {
				Boolean _isSelected = dataItemParser.radiobuttonHandler(_radioButtons.get(i));
				_radioButtons.get(i).put("selected", _isSelected);
			}
			_radioButtons.clear();
		}
		
		return _objects;
	}

	private ArrayList<HashMap<String, Object>> createFreeFormTypeSimple( int _page, ArrayList<BandInfoMapDataSimple> bandList, HashMap<String, Object> _param, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects, float _headerPosition , int _totalPageNum , int _currentPageNum ) throws ScriptException
	{
		
		String _className = "";
		int i = 0;
		int j = 0;
		float _updateY = 0;
		// dataConvertParser 생성
		ItemConvertParser dataItemParser = new ItemConvertParser(DataSet, "", m_appParams);
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		if(mFunction != null)
		{
			mFunction.setParam(_param);
			dataItemParser.setFunction(this.mFunction);
		}
		dataItemParser.setChangeItemList(changeItemList);
		dataItemParser.setIsExportType(isExportType);
		
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		
		//Table용 변수
		int colIndex = 0;
		int rowIndex = 0;
		float updateX = 0;
		float updateY = 0;
		
		NodeList _tablePropertys = null;
		Element _ItemProperty = null;
		String _propertyName;
		//Table용 변수 End
		
		NodeList _propertyAr = null;
		
		HashMap<String, Object> convertItemData = null;
		
		int _bandSize =  bandList.size();
		
		int _subMax = 0;
		
		ArrayList<HashMap<String, Object>> _radioButtons = new ArrayList<HashMap<String, Object>>();
		
		ArrayList<HashMap<String, Object>> _items;
		for(j=0; j < _bandSize; j++ )
		{
			_items = bandList.get(j).getChildren();
			int _max = _items.size();
			
			for ( i = 0; i < _max; i++) {
				
				_className = _items.get(i).get("className").toString();
				if( _className.length() < 4 || !_className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) 
				{
					try {
						if( _headerPosition > -1 ) _cloneY = _headerPosition;
						
						// Band_y값을 가져와서 y포지션 업데이트
						_updateY = -1;
						
						//Table 처리
						if( _className.equals("UBTemplateArea") )
						{	
							_objects = mTempletInfo.get( _items.get(i).get("id").toString() ).convertItemData( _page, _cloneX, _cloneY, _objects, mFunction, null, _currentPageNum, _totalPageNum, -1, -1, changeItemList, null, dataItemParser);
						}
						else
						{
							//라벨 아이템 처리
							convertItemData = dataItemParser.convertItemDataSimple(null, _items.get(i), DataSet, _page, _param, -1, -1  , _totalPageNum , _currentPageNum, "", 0, 0, -1);
							//Y좌표 업데이트 
							if( convertItemData != null ){
								convertItemData.put("y", _cloneY +Float.valueOf(convertItemData.get("band_y").toString()) );		// Empty 나 pageFooter밴드의 경우 Y좌표가 아이템의 Y좌표가 아니라 Band의 Y좌표에 맞춰서 이동이 되어야함.
							}
							
							// 아이템이 null일경우 add시키지 않음 2015-12-02 
							if( convertItemData != null )
							{
								//RadioButtonGroup은 RadioButton보다 앞서서 생성되어야 한다
								if(_className.equals("UBRadioButtonGroup")) _objects.add(0, convertItemData);
								else _objects.add(convertItemData);
								
								if( _className.equals("UBRadioBorder") )
								{
									_radioButtons.add(convertItemData);
								}
							}
						}
						
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
			
		}
		
		if( _radioButtons.size() > 0 )
		{
			for ( i = 0; i < _radioButtons.size(); i++) {
				Boolean _isSelected = dataItemParser.radiobuttonHandler(_radioButtons.get(i));
				_radioButtons.get(i).put("selected", _isSelected);
			}
			_radioButtons.clear();
		}
		
		return _objects;
	}
	
	
	
	
	
	private ArrayList<String> getRequiredItemDataInfo( Object _page,  ArrayList<String> _dataList )
	{
		NodeList _groupDataInfo = ((Element) _page).getElementsByTagName("requiredItem");
		
		if( _dataList == null ) _dataList = new ArrayList<String>();
		
		if( _groupDataInfo == null || _groupDataInfo.getLength() < 1)
		{
			return _dataList;
		}
		
		Element _property;
		String _value = "";
		
		for (int i = 0; i < _groupDataInfo.getLength(); i++) {
			
			_property = (Element) _groupDataInfo.item(i);
			_value = "";
			
			try {
				
				_value = URLDecoder.decode(_property.getAttribute("id").toString(), "UTF-8");
				_dataList.add(_value);
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} 
			
		}
		
		
		return _dataList;
	}
	
	
	private ArrayList<String> getTabIndexItemDataInfo( Object _page,  ArrayList<String> _dataList )
	{
		NodeList _groupDataInfo = ((Element) _page).getElementsByTagName("tabIndexItem");
		
		if( _dataList == null ) _dataList = new ArrayList<String>();
		
		if( _groupDataInfo == null || _groupDataInfo.getLength() < 1)
		{
			return _dataList;
		}
		
		Element _property;
		String _value = "";
		
		for (int i = 0; i < _groupDataInfo.getLength(); i++) {
			
			_property = (Element) _groupDataInfo.item(i);
			_value = "";
			
			try {
				
				_value = URLDecoder.decode(_property.getAttribute("id").toString(), "UTF-8");
				_dataList.add(_value);
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} 
			
		}
		
		return _dataList;
	}

	
	private ArrayList<String> getReqIndexItems( ArrayList<Element> _pageItems, ArrayList<String> _targetItems,  ArrayList<String> _dataList )
	{
		
		if( _dataList == null )
		{
			_dataList = new ArrayList<String>();
		}
		
		HashMap<String, String> _tempMap = new HashMap<String, String>();
		
		int _idx = _pageItems.size();
		int _targetSize = _targetItems.size();
		int _chk = 0;
		for (int i = 0; i < _idx; i++) {
			
			int _targetIndex = _targetItems.indexOf( _pageItems.get(i).getAttribute("id") );
			if(_targetIndex > -1 )
			{
				_tempMap.put( String.valueOf( _targetIndex ),  _pageItems.get(i).getAttribute("id"));
				_chk = _chk + 1;
			}
		} 
		
		if( _chk > 0 )
		{
			for (int i = 0; i < _targetSize; i++) {
				
				if( _tempMap.containsKey( String.valueOf(i)) )
				{
					_dataList.add(  _tempMap.get( String.valueOf(i)) );
				}
				
			}
			
		}
		
		return _dataList;
	}

	private ArrayList<String> getReqIndexItemsJson( ArrayList<BandInfoMapData> _bands , ArrayList<String> _targetItems,  ArrayList<String> _dataList )
	{
		
		if( _dataList == null )
		{
			_dataList = new ArrayList<String>();
		}
		
		HashMap<String, String> _tempMap = new HashMap<String, String>();
		ArrayList<HashMap<String, Value>> _pageItems;
		int _targetSize = _targetItems.size();
		
		for(int j=0; j < _bands.size(); j++ )
		{
			_pageItems = _bands.get(j).getChildren();
			
			int _idx = _pageItems.size();
			int _chk = 0;
			for (int i = 0; i < _idx; i++) {
				
				int _targetIndex = _targetItems.indexOf( _pageItems.get(i).get("id").getStringValue() );
				if(_targetIndex > -1 )
				{
					_tempMap.put( String.valueOf( _targetIndex ),  _pageItems.get(i).get("id").getStringValue());
					_chk = _chk + 1;
				}
			} 
			
			if( _chk > 0 )
			{
				for (int i = 0; i < _targetSize; i++) {
					
					if( _tempMap.containsKey( String.valueOf(i)) )
					{
						_dataList.add(  _tempMap.get( String.valueOf(i)) );
					}
					
				}
				
			}
		}
		
		return _dataList;
	}
	
	
	public void setPageMarginTop( float _value )
	{
		mPageMarginTop = _value;
	}
	public void setPageMarginLeft( float _value )
	{
		mPageMarginLeft = _value;
	}
	public void setPageMarginRight( float _value )
	{
		mPageMarginRight = _value;
	}
	public void setPageMarginBottom( float _value )
	{
		mPageMarginBottom = _value;
	}
	
}
