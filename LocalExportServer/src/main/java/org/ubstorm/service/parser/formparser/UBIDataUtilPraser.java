package org.ubstorm.service.parser.formparser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.script.ScriptException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.model.datastorage.XPathEnhancerParser.functionCall_return;
import org.json.simple.JSONObject;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.formatter.UBFormatter;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.parser.LinkedPageParser;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.PageInfo;
import org.ubstorm.service.parser.formparser.info.PageInfoSimple;
//import org.ubstorm.service.request.ServiceRequestManager;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class UBIDataUtilPraser {
	
	
	/**
	 * functionName	:	getCanvasVisibleChkeck</br>
	 * desc			:	페이지별 pageVisible값을 이용하여 page Element를 사용여부를 결정
	 * @param _dataSet
	 * @param _page
	 * @throws ScriptException 
	 */
	public static Boolean getCanvasVisibleChkeck(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, Element _page, HashMap<String, Object>  _param ,Function mFunction ) throws ScriptException
	{
		NodeList _pageVisibleNode = _page.getElementsByTagName("ubfunction");
		Element _pageVisibleElement;
		//Function mFunction = new Function(_dataSet, null ); 
		mFunction.setDatasetList(_dataSet);
		String _value = "";
		boolean returnFlag = true;
		
		for (int i = 0; i < _pageVisibleNode.getLength(); i++) {
			
			_pageVisibleElement = (Element) _pageVisibleNode.item(i);
			_value = "";
			
			if( _pageVisibleElement.getAttribute("property").equals("pageVisible") )
			{
				
				try {
					_value = URLDecoder.decode(_pageVisibleElement.getAttribute("value").toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String _fnValue;
				_fnValue = getUbfunction(_value, _dataSet,_param, mFunction  );
				
				if( _fnValue.equals("false") ){
					returnFlag = false;
					break;
				}
				
			}
			
		}
		
		return returnFlag;
	}
	
	/**
	 * functionName	:	getCanvasVisibleChkeck</br>
	 * desc			:	페이지별 pageVisible값을 이용하여 page Element를 사용여부를 결정
	 * @param _dataSet
	 * @param _page
	 * @throws ScriptException 
	 */
	public static String getCanvasBackgroundFX(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, Element _page, HashMap<String, Object>  _param ,Function mFunction ) throws ScriptException
	{
		NodeList _pageVisibleNode = _page.getElementsByTagName("ubfunction");
		Element _pageVisibleElement;
		//Function mFunction = new Function(_dataSet, null ); 
		mFunction.setDatasetList(_dataSet);
		String _value = "";
		String _retStr = "";
		boolean returnFlag = true;
		
		for (int i = 0; i < _pageVisibleNode.getLength(); i++) {
			
			_pageVisibleElement = (Element) _pageVisibleNode.item(i);
			_value = "";
			
			if( _pageVisibleElement.getAttribute("property").equals("pageBackgroundImage") )
			{
				
				try {
					_value = URLDecoder.decode(_pageVisibleElement.getAttribute("value").toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String _fnValue;
				_fnValue = getUbfunction(_value, _dataSet,_param, mFunction  );
				
				_retStr = _fnValue;
			}
			
		}
		
		return _retStr;
	}
	
	public static String getUbfunction( String _fnStr,  HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, Object>  _param ,Function mFunction ) throws ScriptException
	{
		mFunction.setParam(_param);
		mFunction.setDatasetList(_dataSet);
		
		String _fnValue;
		
		if( mFunction.getFunctionVersion().equals("2.0") ){
			_fnValue = mFunction.testFN(_fnStr,0, -1 , 0, -1, -1, "" );
		}else{
			_fnValue = mFunction.function(_fnStr,0, -1 , 0, -1, -1);
		}
		_fnValue = _fnValue.trim();
		
		return _fnValue;
	}
	
	/**
	 * functionName	:	getCanvasVisibleChkeck</br>
	 * desc			:	페이지별 pageVisible값을 이용하여 page Element를 사용여부를 결정
	 * @param _dataSet
	 * @param _page
	 * @throws ScriptException 
	 */
	public static Boolean getUBFxChkeck(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, Element _page, HashMap<String, Object>  _param , String propNm, Function _function ) throws ScriptException
	{		
		return getUBFxChkeck(_dataSet, _page, _param , propNm , 0,-1, 0, _function);
	}
	
	
	
	/**
	 * functionName	:	getCanvasVisibleChkeck</br>
	 * desc			:	페이지별 pageVisible값을 이용하여 page Element를 사용여부를 결정 
	 * desc			: FreeFormd에서 페이지 별 Check 값 리턴
	 * @param _dataSet
	 * @param _page
	 * @throws ScriptException 
	 */
	public static Boolean getUBFxChkeck(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, Element _page, HashMap<String, Object>  _param , String propNm ,int _rowIndex, int _totalpageNum, int _currentPageNum, Function _function  ) throws ScriptException
	{
		NodeList _pageVisibleNode = _page.getElementsByTagName("ubfunction");
		Element _pageVisibleElement;
		Function mFunction;
		
		if(_function == null ) mFunction = new Function(_dataSet, null ); 
		else mFunction = _function;
		
		String _value = "";
		boolean returnFlag = true;
		
		for (int i = 0; i < _pageVisibleNode.getLength(); i++) {
			
			_pageVisibleElement = (Element) _pageVisibleNode.item(i);
			_value = "";
			
			if( _pageVisibleElement.getAttribute("property").equals(propNm) )
			{
				
				try {
					_value = URLDecoder.decode(_pageVisibleElement.getAttribute("value").toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mFunction.setParam(_param);
				mFunction.setDatasetList(_dataSet);
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_value,_rowIndex, _totalpageNum , _currentPageNum, -1, -1, "" );
				}else{
					_fnValue = mFunction.function(_value,_rowIndex, _totalpageNum , _currentPageNum, -1, -1);
				}
				
				_fnValue = _fnValue.trim();
				
				if( _fnValue.equals("false") ){
					returnFlag = false;
					break;
				}
				
			}
			
		}
		
		return returnFlag;
	}
	
	
	/**
	 * 엑셀 Data 저장을 위한 함수
	 * Header:ArrayList<String>
	 * data  :ArrayList< ArrayList<string> > 
	 * 형태로 전달
	 * @throws UnsupportedEncodingException 
	 * @throws XPathExpressionException 
	 * @throws ScriptException 
	 */
	//public static HashMap<String, HashMap<String, ArrayList<Object>>> getAllExportDatas(ServiceRequestManager srm, ArrayList<Element> _pageAr, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, UDMParamSet appParams,JSONObject mParam, Function _function ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	public static HashMap<String, HashMap<String, ArrayList<Object>>> getAllExportDatas(String serverUrl, ArrayList<Element> _pageAr, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, UDMParamSet appParams,JSONObject mParam, Function _function ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		// 각 페이지별로 체크하여 밴드일경우 화면에 사용되는 모든 데이터와 컬럼들을 각각가져오기
		
		
		// connectPage일경우 각 프로젝트별로 동일하게 돌아서 처리
		
		
		// LinekdPage일경우 데이터분할된 데이터를 가져와서 처리
		
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int m = 0;
		int n = 0;
		
		float pageWidth = 0;
		float pageHeight = 0;
		
		int _reportType = 0;
		Element _page = null;
		
		ArrayList<Integer> mXAr = new ArrayList<Integer>();
		
		HashMap<String, HashMap<String, ArrayList<Object>>> returnDataMap = null;
		String dataName = "";
		ArrayList<Element> childrenList = null;
		
		HashMap<String, HashMap<String, String>> headerColumnMap = null;
		
		HashMap<String, ArrayList<String>> realDataCoumns = new HashMap<String,ArrayList<String>>();
		HashMap<String, ArrayList<String>> dataColumnDatas = new HashMap<String,ArrayList<String>>();
		
		ArrayList< HashMap<String, HashMap<String,ArrayList<Object>>> > anotherDataList = null;
		
		NodeList itemPropertys;
		Element itemProperty;
		String _dataType = "";
		String _dataSetName = "";
		String _dataSetColumn = "";
		NodeList _items;
		
		int _subMax = 0;
		Element _item = null;
		
		HashMap<String, HashMap<String, ArrayList<Object>>> _crossTabDataSet = null;
		
		for ( i = 0; i < _pageAr.size(); i++) {
			
			_page = _pageAr.get(i);
			
			_reportType = Integer.parseInt(_page.getAttribute("reportType"));
			pageWidth = Float.valueOf(_page.getAttribute("width"));
			pageHeight = Float.valueOf(_page.getAttribute("height"));
			
			switch (_reportType) {
			case 0: //coverPage
				
				break;

			case 1: //freeform
			case 4: //freeform
				// 아이템별 dataSet와 Column값을 담기
				_dataType = "";
				_dataSetName = "";
				_dataSetColumn = "";
				dataColumnDatas = new HashMap<String,ArrayList<String>>();
				_items = _page.getElementsByTagName("item");
				_subMax = _items.getLength();
				for ( k = 0; k < _subMax; k++) {
					
					itemPropertys = ((Element) _items.item(k)).getElementsByTagName("property");
					_item = (Element) _items.item(k);
					
					dataColumnDatas = getItemDataProperty( _item, dataColumnDatas);
					
				}
				
				for (String key : dataColumnDatas.keySet()) {
					int _subMax3 = dataColumnDatas.get(key).size();
					for ( n = 0; n < _subMax3; n++) {
						if( realDataCoumns.get(key).contains( dataColumnDatas.get(key).get(n) ) == false )
						{
							realDataCoumns.get(key).add( dataColumnDatas.get(key).get(n) );
						}
					}
				}
				
				break;
				
			case 2: //masterBand
				MasterBandParser masterParser = new MasterBandParser( appParams );
				masterParser.setFunction(_function);
				masterParser.setExportData(true);
				
				ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
				ArrayList<Object> masterInfo 					= masterParser.loadTotalPage(_page, _dataSet,0, pageHeight, pageWidth, mXAr);
				int _masterTotPage 								= (Integer) masterInfo.get(0);
				ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
				
				HashMap<String, Object> _totalItem = null;
				
				String _type = "";
				int _max = masterList.size();
				
				for ( j = 0; j < _max; j++) {
					
					_totalItem = (HashMap<String, Object>) masterList.get(j);
					_type = _totalItem.get("type").toString();
					ArrayList<Element> pageItems = (ArrayList<Element>) _totalItem.get("pageItems");
					
					// 아이템별 dataSet와 Column값을 담기
					_dataType = "";
					_dataSetName = "";
					_dataSetColumn = "";
					dataColumnDatas = new HashMap<String,ArrayList<String>>();
					
					_subMax = pageItems.size();
					for ( k = 0; k < _subMax; k++) {
						
						_item = pageItems.get(k);
						
						dataColumnDatas = getItemDataProperty( _item, dataColumnDatas);
						
					}
					
					
					// continueBand타입의 경우 밴드 리스트에서 데이터셋과 아이템별 Column값을 찾아서 담기
					if( _type.equals("continue") )
					{
						ArrayList<Object> bandAr = (ArrayList<Object>) _totalItem.get("bandInfoData");
						HashMap<String, BandInfoMapData> bandInfo = (HashMap<String, BandInfoMapData>) bandAr.get(0);
						ArrayList<BandInfoMapData> bandList =(ArrayList<BandInfoMapData>) bandAr.get(1);
						HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
						HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
						
						ArrayList<HashMap<String, Value>> children = null; 
						
						// crossTabData가 null이 아닐경우 
						if( crossTabData != null )
						{
							// crossTabData.columnList
							// crossTabData.rowList
							
						}
						
						ArrayList<String> dataNameAr = new ArrayList<String>();
						// ContinueBand 의 밴드별 데이터셋을 가져와서 담기
						
						_subMax = bandList.size();
						
						for ( k = 0; k < _subMax; k++) 
						{
							if(bandList.get(k).getClassName().equals(BandInfoMapData.DATA_BAND))
							{
								dataName = bandList.get(k).getDataSet();
								
								if( dataNameAr.indexOf(dataName) == -1 )
								{
									dataNameAr.add(dataName);
								}
							}
							else if( bandList.get(k).getClassName().equals(BandInfoMapData.CROSSTAB_BAND) )
							{
								ArrayList<ArrayList<HashMap<String, Value>>>  _pagelist = crossTabData.get( bandList.get(k).getId() );
								HashMap<String, ArrayList<Object>> _crossTabSubDataSet = new HashMap<String, ArrayList<Object>>();
								
								if( _crossTabDataSet == null ) _crossTabDataSet = new HashMap<String, HashMap<String, ArrayList<Object>>>();
								
								try {
									_crossTabSubDataSet = convertCrossTabToExcelData(_pagelist);
								} catch (Exception e) {
									// TODO: handle exception
								}
								_crossTabDataSet.put(bandList.get(k).getId(), _crossTabSubDataSet);
							}
						}
						
						// 모든 아이템의 DataSet을 찾아서 실제 표시되는 Column값을 담아둔후 그룹핑 된 데이터셋별로 Column값을 담기
						_subMax = dataNameAr.size();
						
						for ( l = 0; l < _subMax; l++) {
							_dataSetName = dataNameAr.get(l);
							if( originalDataMap.containsKey( _dataSetName ) )
							{
								_dataSetName = originalDataMap.get(_dataSetName);
							}
							
							if( dataColumnDatas.containsKey( _dataSetName ) )
							{
								if(realDataCoumns.containsKey(_dataSetName) == false )
								{
									realDataCoumns.put( dataNameAr.get(l), dataColumnDatas.get(_dataSetName));
								}
								else
								{
									int _subMax2 = dataColumnDatas.get(_dataSetName).size();
									for ( n = 0; n < _subMax2; n++) {
										if( realDataCoumns.get(dataNameAr.get(l)).contains( dataColumnDatas.get(_dataSetName).get(n) ) == false )
										{
											realDataCoumns.get(dataNameAr.get(l)).add( dataColumnDatas.get(_dataSetName).get(n) );
										}
									}
									
								}
							}
							
						}
						
						
					}
					else if( _type.equals("band") )	// 	그 외 Empty밴드일경우
					{
						
						for (String key : dataColumnDatas.keySet()) {
							int _subMax3 = dataColumnDatas.get(key).size();
							for ( n = 0; n < _subMax3; n++) {
								if( realDataCoumns.get(key).contains( dataColumnDatas.get(key).get(n) ) == false )
								{
									realDataCoumns.get(key).add( dataColumnDatas.get(key).get(n) );
								}
							}
						}
						
					}
					
				}
				
				break;
			case 3:// ContinueBand의 경우 
				
				ContinueBandParser continueBandParser = new ContinueBandParser(appParams);
				continueBandParser.setFunction(_function);
				continueBandParser.setExportData(true);
				
				ArrayList<Object> bandAr = continueBandParser.loadTotalPage(_page, _dataSet,0, pageHeight, pageWidth, mXAr, _pageAr, mParam );
				HashMap<String, BandInfoMapData> bandInfo = (HashMap<String, BandInfoMapData>) bandAr.get(0);
				ArrayList<BandInfoMapData> bandList =(ArrayList<BandInfoMapData>) bandAr.get(1);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
				HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
				
				// crossTabData가 null이 아닐경우 
				if( crossTabData != null )
				{
					// crossTabData.columnList
					// crossTabData.rowList
					
				}
				
				dataColumnDatas = new HashMap<String,ArrayList<String>>();
				_items = _page.getElementsByTagName("item");
				
				_subMax = _items.getLength();
				for ( k = 0; k < _subMax; k++) {
					_item = (Element) _items.item(k);
					
					dataColumnDatas = getItemDataProperty( _item, dataColumnDatas);
				}
				
				
				ArrayList<String> dataNameAr = new ArrayList<String>();
				// ContinueBand 의 밴드별 데이터셋을 가져와서 담기
				
				_subMax = bandList.size();
				
				for ( k = 0; k < _subMax; k++) 
				{
					if(bandList.get(k).getClassName().equals(BandInfoMapData.DATA_BAND))
					{
						dataName = bandList.get(k).getDataSet();
						
						if( dataNameAr.indexOf(dataName) == -1 )
						{
							dataNameAr.add(dataName);
						}
					}
					else if( bandList.get(k).getClassName().equals(BandInfoMapData.CROSSTAB_BAND) )
					{
						ArrayList<ArrayList<HashMap<String, Value>>>  _pagelist = crossTabData.get( bandList.get(k).getId() );
						HashMap<String, ArrayList<Object>> _crossTabSubDataSet = new HashMap<String, ArrayList<Object>>();
						
						if( _crossTabDataSet == null ) _crossTabDataSet = new HashMap<String, HashMap<String, ArrayList<Object>>>();
						
						try {
							_crossTabSubDataSet = convertCrossTabToExcelData(_pagelist);
						} catch (Exception e) {
							// TODO: handle exception
						}
						_crossTabDataSet.put(bandList.get(k).getId(), _crossTabSubDataSet);
					}
				}
				
				// 모든 아이템의 DataSet을 찾아서 실제 표시되는 Column값을 담아둔후 그룹핑 된 데이터셋별로 Column값을 담기
				_subMax = dataNameAr.size();
				
				for ( l = 0; l < _subMax; l++) {
					_dataSetName = dataNameAr.get(l);
					if( originalDataMap.containsKey( _dataSetName ) )
					{
						_dataSetName = originalDataMap.get(_dataSetName);
					}
					
					if( dataColumnDatas.containsKey( _dataSetName ) )
					{
						if(realDataCoumns.containsKey(dataNameAr.get(l)) == false )
						{
							realDataCoumns.put( dataNameAr.get(l), dataColumnDatas.get(_dataSetName));
						}
						else
						{
//							for ( m = 0; m < dataColumnDatas.get(_dataSetName).size(); m++) {
							int _subMax2 = dataColumnDatas.get(_dataSetName).size();
							for ( n = 0; n < _subMax2; n++) {
								if( realDataCoumns.get(dataNameAr.get(l)).contains( dataColumnDatas.get(_dataSetName).get(n) ) == false )
								{
									realDataCoumns.get(dataNameAr.get(l)).add( dataColumnDatas.get(_dataSetName).get(n) );
								}
							}
//							}
							
						}
					}
					
				}
				
				break;
				
			case 12:
				ConnectLinkParser connectLink = new ConnectLinkParser(serverUrl, appParams);
				
				connectLink.setFunction(_function);
				connectLink.setExportData(true);
				
				HashMap<String, Object> connectData = connectLink.loadPagesData(_page, mParam, mXAr);
				int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
				
				if(anotherDataList == null )
				{
					anotherDataList = new ArrayList<HashMap<String,HashMap<String,ArrayList<Object>>>>();
				}
				
				HashMap<String, HashMap<String,ArrayList<Object>>> _connectDataMap = connectLink.getAllExportDatas( i, connectData );
				anotherDataList.add(_connectDataMap);
				break;
			case 14:
				// Linked Project type 문서 타입
				LinkedPageParser linkedParser = new LinkedPageParser(appParams);
				
				linkedParser.setFunction(_function);
				linkedParser.setExportData(true);
				
				HashMap<String, Object> linkedData = linkedParser.loadTotalPage(_pageAr,_dataSet, mXAr );
				int _totPage = (Integer) linkedData.get("totPage");
				ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
				HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
				
				i = _pageAr.size();
				
				if(anotherDataList == null )
				{
					anotherDataList = new ArrayList<HashMap<String,HashMap<String,ArrayList<Object>>>>();
				}
				
				HashMap<String, HashMap<String,ArrayList<Object>>> _linkedDataMap = linkedParser.getAllReportData(_pageAr, retArr, newDataSet);
				anotherDataList.add(_linkedDataMap);
				break;
				
			default:
				
				break;
			}
			
		}
		
		// 결과로 만들어진 데이터를 이용하여 데이터 파싱하여 리턴
		
		HashMap<String, HashMap<String, ArrayList<Object>>> resultDataSet = new HashMap<String, HashMap<String, ArrayList<Object>>>();
		ArrayList<Object> _headerAr = null;
//		ArrayList<HashMap<String, String>> _dataAr = null;
		ArrayList<Object> _dataAr = null;
//		HashMap<String, String> _rowData = null;
		ArrayList<String> _rowData = null;
		int _dataMaxCnt = 0;
		int _dataMaxRowCnt = 0;
		String _colName = "";
		
		HashMap<String, ArrayList<Object>> _resultDataMap;
		
		for (String _str : realDataCoumns.keySet()) {
			
			_resultDataMap = new HashMap<String, ArrayList<Object>>();
			
			_dataMaxCnt = realDataCoumns.get(_str).size();
			_headerAr = new ArrayList<Object>();
			_dataAr = new ArrayList<Object>();
			
			if( _dataSet.containsKey(_str) == false ) continue;
			
			_dataMaxRowCnt = _dataSet.get(_str).size();
			for ( j = 0; j < _dataMaxRowCnt; j++) {
				
//				_rowData = new HashMap<String, String>();
				_rowData = new ArrayList<String>();
				
				for ( i = 0; i < _dataMaxCnt; i++) {
					_colName = realDataCoumns.get(_str).get(i);
					
					if( _dataSet.get(_str).get(j).containsKey(_colName) == false ) continue;
					
					if(j == 0 )
					{
						_headerAr.add(_colName);
					}
					
					_rowData.add( String.valueOf(_dataSet.get(_str).get(j).get(_colName))); 
//					_rowData.put(_colName, _dataSet.get(_str).get(j).get(_colName).toString()); 
					
					
				}
				_dataAr.add(_rowData);
			}
			
			_resultDataMap.put("header",  _headerAr);
			_resultDataMap.put("data",  _dataAr);

			resultDataSet.put(_str, _resultDataMap);
		}
		
		//ConnectPage 또는 LinkedPage 의 경우
		if( anotherDataList != null && anotherDataList.size() > 0  ){
			int _anotherSize = anotherDataList.size();
			
			for ( n = 0; n < _anotherSize; n++) {
				
				for (String _dName : anotherDataList.get(n).keySet()) {
					
					resultDataSet.put(_dName, anotherDataList.get(n).get(_dName) );
					
				}
				
			}
			
		}
		
		//ConnectPage 또는 LinkedPage 의 경우
		if( _crossTabDataSet != null && _crossTabDataSet.size() > 0  ){
			
			for (String _dName : _crossTabDataSet.keySet()) {
				resultDataSet.put(_dName, _crossTabDataSet.get(_dName) );
			}
				
		}
		
		
		return resultDataSet;
	}

	/**
	 * 엑셀 Data 저장을 위한 함수
	 * Header:ArrayList<String>
	 * data  :ArrayList< ArrayList<string> > 
	 * 형태로 전달
	 * @throws UnsupportedEncodingException 
	 * @throws XPathExpressionException 
	 * @throws ScriptException 
	 */
	//public static HashMap<String, HashMap<String, ArrayList<Object>>> getAllExportDatasJS(ServiceRequestManager srm, ArrayList<PageInfo> _pageAr, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, UDMParamSet appParams,JSONObject mParam, Function _function ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	public static HashMap<String, HashMap<String, ArrayList<Object>>> getAllExportDatasJS(String serverUrl, ArrayList<PageInfo> _pageAr, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, UDMParamSet appParams,JSONObject mParam, Function _function ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		// 각 페이지별로 체크하여 밴드일경우 화면에 사용되는 모든 데이터와 컬럼들을 각각가져오기
		
		
		// connectPage일경우 각 프로젝트별로 동일하게 돌아서 처리
		
		
		// LinekdPage일경우 데이터분할된 데이터를 가져와서 처리
		
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int m = 0;
		int n = 0;
		
		float pageWidth = 0;
		float pageHeight = 0;
		
		int _reportType = 0;
		PageInfo _page = null;
		
		ArrayList<Integer> mXAr = new ArrayList<Integer>();
		
		HashMap<String, HashMap<String, ArrayList<Object>>> returnDataMap = null;
		String dataName = "";
		ArrayList<Element> childrenList = null;
		
		HashMap<String, HashMap<String, String>> headerColumnMap = null;
		
		HashMap<String, ArrayList<String>> realDataCoumns = new HashMap<String,ArrayList<String>>();
		HashMap<String, ArrayList<String>> dataColumnDatas = new HashMap<String,ArrayList<String>>();
		
		ArrayList< HashMap<String, HashMap<String,ArrayList<Object>>> > anotherDataList = null;
		
		HashMap<String, Value> itemPropertys;
		String _dataType = "";
		String _dataSetName = "";
		String _dataSetColumn = "";
		ArrayList<HashMap<String, Value>> _items;
		
		int _subMax = 0;
		HashMap<String, Value> _item = null;
		
		HashMap<String, HashMap<String, ArrayList<Object>>> _crossTabDataSet = null;
		
		for ( i = 0; i < _pageAr.size(); i++) {
			
			_page = _pageAr.get(i);
			_page.createItem();
			
			_reportType = Integer.parseInt(_page.getReportType());
			pageWidth = _page.getWidth();
			pageHeight = _page.getHeight();
			
			switch (_reportType) {
			case 0: //coverPage
				
				break;
				
			case 1: //freeform
			case 4: //freeform
				// 아이템별 dataSet와 Column값을 담기
				_dataType = "";
				_dataSetName = "";
				_dataSetColumn = "";
				dataColumnDatas = new HashMap<String,ArrayList<String>>();
				_items = _page.getItems();
				_subMax = _items.size();
				for ( k = 0; k < _subMax; k++) {
					
					_item = _items.get(k);
					
					dataColumnDatas = getItemDataPropertyJS( _item, dataColumnDatas);
					
				}
				
				for (String key : dataColumnDatas.keySet()) {
					int _subMax3 = dataColumnDatas.get(key).size();
					for ( n = 0; n < _subMax3; n++) {
						if( realDataCoumns.get(key).contains( dataColumnDatas.get(key).get(n) ) == false )
						{
							realDataCoumns.get(key).add( dataColumnDatas.get(key).get(n) );
						}
					}
				}
				
				break;
				
			case 2: //masterBand
				MasterBandParser masterParser = new MasterBandParser( appParams );
				masterParser.setFunction(_function);
				masterParser.setExportData(true);
				
				ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
				ArrayList<Object> masterInfo 					= masterParser.loadTotalPage(_page, _dataSet,0, pageHeight, pageWidth, mXAr);
				int _masterTotPage 								= (Integer) masterInfo.get(0);
				ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
				
				HashMap<String, Object> _totalItem = null;
				
				String _type = "";
				int _max = masterList.size();
				
				for ( j = 0; j < _max; j++) {
					
					_totalItem = (HashMap<String, Object>) masterList.get(j);
					_type = _totalItem.get("type").toString();
					ArrayList<Element> pageItems = (ArrayList<Element>) _totalItem.get("pageItems");
					
					// 아이템별 dataSet와 Column값을 담기
					_dataType = "";
					_dataSetName = "";
					_dataSetColumn = "";
					dataColumnDatas = new HashMap<String,ArrayList<String>>();
					
					_items = _page.getItems();
					_subMax = _items.size();
					for ( k = 0; k < _subMax; k++) {
						
						_item = _items.get(k);
						
						dataColumnDatas = getItemDataPropertyJS( _item, dataColumnDatas);
						
					}
					
					// continueBand타입의 경우 밴드 리스트에서 데이터셋과 아이템별 Column값을 찾아서 담기
					if( _type.equals("continue") )
					{
						ArrayList<Object> bandAr = (ArrayList<Object>) _totalItem.get("bandInfoData");
						HashMap<String, BandInfoMapData> bandInfo = _page.getBandInfoData();
						ArrayList<BandInfoMapData> bandList = (ArrayList<BandInfoMapData>) _totalItem.get("item");;
						ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(0);
						HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(1);
						
						// group함수를 위한 객체 function처리하기 위하여 페이지 이동 함수로 전달하여 진행
						HashMap<String, String> originalDataMap = _page.getOriginalDataMap();				// originalData값으 가지고 있는 객체
						ArrayList<ArrayList<String>> groupDataNamesAr = _page.getGroupDataNamesAr();					// 그룹핑된 데이터명을 가지고 있는 객체
						
						ArrayList<HashMap<String, Value>> children = null; 
						
						// crossTabData가 null이 아닐경우 
						if( crossTabData != null )
						{
							// crossTabData.columnList
							// crossTabData.rowList
							
						}
						
						ArrayList<String> dataNameAr = new ArrayList<String>();
						// ContinueBand 의 밴드별 데이터셋을 가져와서 담기
						
						_subMax = bandList.size();
						
						for ( k = 0; k < _subMax; k++) 
						{
							if(bandList.get(k).getClassName().equals(BandInfoMapData.DATA_BAND))
							{
								dataName = bandList.get(k).getDataSet();
								
								if( dataNameAr.indexOf(dataName) == -1 )
								{
									dataNameAr.add(dataName);
								}
							}
							else if( bandList.get(k).getClassName().equals(BandInfoMapData.CROSSTAB_BAND) )
							{
								ArrayList<ArrayList<HashMap<String, Value>>>  _pagelist = crossTabData.get( bandList.get(k).getId() );
								HashMap<String, ArrayList<Object>> _crossTabSubDataSet = new HashMap<String, ArrayList<Object>>();
								
								if( _crossTabDataSet == null ) _crossTabDataSet = new HashMap<String, HashMap<String, ArrayList<Object>>>();
								
								try {
									_crossTabSubDataSet = convertCrossTabToExcelData(_pagelist);
								} catch (Exception e) {
									// TODO: handle exception
								}
								_crossTabDataSet.put(bandList.get(k).getId(), _crossTabSubDataSet);
							}
						}
						
						// 모든 아이템의 DataSet을 찾아서 실제 표시되는 Column값을 담아둔후 그룹핑 된 데이터셋별로 Column값을 담기
						_subMax = dataNameAr.size();
						
						for ( l = 0; l < _subMax; l++) {
							_dataSetName = dataNameAr.get(l);
							if( originalDataMap.containsKey( _dataSetName ) )
							{
								_dataSetName = originalDataMap.get(_dataSetName);
							}
							
							if( dataColumnDatas.containsKey( _dataSetName ) )
							{
								if(realDataCoumns.containsKey(_dataSetName) == false )
								{
									realDataCoumns.put( dataNameAr.get(l), dataColumnDatas.get(_dataSetName));
								}
								else
								{
									int _subMax2 = dataColumnDatas.get(_dataSetName).size();
									for ( n = 0; n < _subMax2; n++) {
										if( dataColumnDatas.get(_dataSetName) != null && realDataCoumns.get(dataNameAr.get(l)).contains( dataColumnDatas.get(_dataSetName).get(n) ) == false )
										{
											realDataCoumns.get(dataNameAr.get(l)).add( dataColumnDatas.get(_dataSetName).get(n) );
										}
									}
									
								}
							}
							
						}
						
						
					}
					else if( _type.equals("band") )	// 	그 외 Empty밴드일경우
					{
						
						for (String key : dataColumnDatas.keySet()) {
							int _subMax3 = dataColumnDatas.get(key).size();
							for ( n = 0; n < _subMax3; n++) {
								if( dataColumnDatas.get(key) != null && realDataCoumns.get(key).contains( dataColumnDatas.get(key).get(n) ) == false )
								{
									realDataCoumns.get(key).add( dataColumnDatas.get(key).get(n) );
								}
							}
						}
						
					}
					
				}
				
				break;
			case 3:// ContinueBand의 경우 
				
				ContinueBandParser continueBandParser = new ContinueBandParser(appParams);
				continueBandParser.setFunction(_function);
				continueBandParser.setExportData(true);
				
				ArrayList<Object> bandAr = continueBandParser.loadTotalPageJson(_page, _dataSet,0, pageHeight, pageWidth, mXAr, mParam );
				
				HashMap<String, BandInfoMapData> bandInfo = _page.getBandInfoData();
				ArrayList<BandInfoMapData> bandList = (ArrayList<BandInfoMapData>) _page.getBandList();
				HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(1);
				
				HashMap<String, String> originalDataMap = _page.getOriginalDataMap();		// originalData값으 가지고 있는 객체
				
				// crossTabData가 null이 아닐경우 
				if( crossTabData != null )
				{
					// crossTabData.columnList
					// crossTabData.rowList
					
				}
				
				dataColumnDatas = new HashMap<String,ArrayList<String>>();
				_items = _page.getItems();
				_subMax = _items.size();
				
				for ( k = 0; k < _subMax; k++) {
					_item = _items.get(k);
					
					dataColumnDatas = getItemDataPropertyJS( _item, dataColumnDatas);
					
				}
				
				ArrayList<String> dataNameAr = new ArrayList<String>();
				// ContinueBand 의 밴드별 데이터셋을 가져와서 담기
				
				_subMax = bandList.size();
				
				for ( k = 0; k < _subMax; k++) 
				{
					if(bandList.get(k).getClassName().equals(BandInfoMapData.DATA_BAND))
					{
						dataName = bandList.get(k).getDataSet();
						
						if( dataNameAr.indexOf(dataName) == -1 )
						{
							dataNameAr.add(dataName);
						}
					}
					else if( bandList.get(k).getClassName().equals(BandInfoMapData.CROSSTAB_BAND) )
					{
						ArrayList<ArrayList<HashMap<String, Value>>>  _pagelist = crossTabData.get( bandList.get(k).getId() );
						HashMap<String, ArrayList<Object>> _crossTabSubDataSet = new HashMap<String, ArrayList<Object>>();
						
						if( _crossTabDataSet == null ) _crossTabDataSet = new HashMap<String, HashMap<String, ArrayList<Object>>>();
						
						try {
							_crossTabSubDataSet = convertCrossTabToExcelData(_pagelist);
						} catch (Exception e) {
							// TODO: handle exception
						}
						_crossTabDataSet.put(bandList.get(k).getId(), _crossTabSubDataSet);
					}
				}
				
				// 모든 아이템의 DataSet을 찾아서 실제 표시되는 Column값을 담아둔후 그룹핑 된 데이터셋별로 Column값을 담기
				_subMax = dataNameAr.size();
				
				for ( l = 0; l < _subMax; l++) {
					_dataSetName = dataNameAr.get(l);
					if( originalDataMap.containsKey( _dataSetName ) )
					{
						_dataSetName = originalDataMap.get(_dataSetName);
					}
					
					if( dataColumnDatas.containsKey( _dataSetName ) )
					{
						if(realDataCoumns.containsKey(dataNameAr.get(l)) == false )
						{
							realDataCoumns.put( dataNameAr.get(l), dataColumnDatas.get(_dataSetName));
						}
						else
						{
//							for ( m = 0; m < dataColumnDatas.get(_dataSetName).size(); m++) {
							int _subMax2 = dataColumnDatas.get(_dataSetName).size();
							for ( n = 0; n < _subMax2; n++) {
								if( dataColumnDatas.get(_dataSetName) != null && realDataCoumns.get(dataNameAr.get(l)).contains( dataColumnDatas.get(_dataSetName).get(n) ) == false )
								{
									realDataCoumns.get(dataNameAr.get(l)).add( dataColumnDatas.get(_dataSetName).get(n) );
								}
							}
//							}
							
						}
					}
					
				}
				
				break;
				
			case 12:
//				ConnectLinkParser connectLink = new ConnectLinkParser(srm, appParams);
//				
//				connectLink.setFunction(_function);
//				connectLink.setExportData(true);
//				
//				HashMap<String, Object> connectData = connectLink.loadPagesData(_page, mParam, mXAr);
//				int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
//				
//				if(anotherDataList == null )
//				{
//					anotherDataList = new ArrayList<HashMap<String,HashMap<String,ArrayList<Object>>>>();
//				}
//				
//				HashMap<String, HashMap<String,ArrayList<Object>>> _connectDataMap = connectLink.getAllExportDatas( i, connectData );
//				anotherDataList.add(_connectDataMap);
				break;
			case 14:
				// Linked Project type 문서 타입
				LinkedPageParser linkedParser = new LinkedPageParser(appParams);
				
				linkedParser.setFunction(_function);
				linkedParser.setExportData(true);
				
				HashMap<String, Object> linkedData = linkedParser.loadTotalPageJson(_pageAr,_dataSet, mXAr );
				int _totPage = (Integer) linkedData.get("totPage");
				ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
				HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
				
				i = _pageAr.size();
				
				if(anotherDataList == null )
				{
					anotherDataList = new ArrayList<HashMap<String,HashMap<String,ArrayList<Object>>>>();
				}
				
				HashMap<String, HashMap<String,ArrayList<Object>>> _linkedDataMap = linkedParser.getAllReportDataJson(_pageAr, retArr, newDataSet);
				anotherDataList.add(_linkedDataMap);
				break;
				
			default:
				
				break;
			}
			
		}
		
		// 결과로 만들어진 데이터를 이용하여 데이터 파싱하여 리턴
		
		HashMap<String, HashMap<String, ArrayList<Object>>> resultDataSet = new HashMap<String, HashMap<String, ArrayList<Object>>>();
		ArrayList<Object> _headerAr = null;
//		ArrayList<HashMap<String, String>> _dataAr = null;
		ArrayList<Object> _dataAr = null;
//		HashMap<String, String> _rowData = null;
		ArrayList<String> _rowData = null;
		int _dataMaxCnt = 0;
		int _dataMaxRowCnt = 0;
		String _colName = "";
		
		HashMap<String, ArrayList<Object>> _resultDataMap;
		
		for (String _str : realDataCoumns.keySet()) {
			
			_resultDataMap = new HashMap<String, ArrayList<Object>>();
			
			_dataMaxCnt = realDataCoumns.get(_str).size();
			_headerAr = new ArrayList<Object>();
			_dataAr = new ArrayList<Object>();
			
			if( _dataSet.containsKey(_str) == false ) continue;
			
			_dataMaxRowCnt = _dataSet.get(_str).size();
			for ( j = 0; j < _dataMaxRowCnt; j++) {
				
//				_rowData = new HashMap<String, String>();
				_rowData = new ArrayList<String>();
				
				for ( i = 0; i < _dataMaxCnt; i++) {
					_colName = realDataCoumns.get(_str).get(i);
					
					if( _dataSet.get(_str).get(j).containsKey(_colName) == false ) continue;
					
					if(j == 0 )
					{
						_headerAr.add(_colName);
					}
					
					_rowData.add( String.valueOf(_dataSet.get(_str).get(j).get(_colName))); 
//					_rowData.put(_colName, _dataSet.get(_str).get(j).get(_colName).toString()); 
					
					
				}
				_dataAr.add(_rowData);
			}
			
			_resultDataMap.put("header",  _headerAr);
			_resultDataMap.put("data",  _dataAr);
			
			resultDataSet.put(_str, _resultDataMap);
		}
		
		//ConnectPage 또는 LinkedPage 의 경우
		if( anotherDataList != null && anotherDataList.size() > 0  ){
			int _anotherSize = anotherDataList.size();
			
			for ( n = 0; n < _anotherSize; n++) {
				
				for (String _dName : anotherDataList.get(n).keySet()) {
					
					resultDataSet.put(_dName, anotherDataList.get(n).get(_dName) );
					
				}
				
			}
			
		}
		
		//ConnectPage 또는 LinkedPage 의 경우
		if( _crossTabDataSet != null && _crossTabDataSet.size() > 0  ){
			
			for (String _dName : _crossTabDataSet.keySet()) {
				resultDataSet.put(_dName, _crossTabDataSet.get(_dName) );
			}
			
		}
		
		
		return resultDataSet;
	}
	
	
	
	
	public static HashMap<String,ArrayList<String>> getItemDataProperty( Element _item, HashMap<String,ArrayList<String>> dataColumnDatas ) throws UnsupportedEncodingException
	{
		
		int n = 0;
		int l = 0;
		
		NodeList itemPropertys;
		String _dataType = "";
		String _dataSetName = "";
		String _dataSetColumn = "";
		
		Element itemProperty;
		
		if( _item.getAttribute("className").equals("UBTable") )
		{
			NodeList _cells = _item.getElementsByTagName("cell");
			Element _cell;
			int _cellLength = _cells.getLength();
			for ( n = 0; n < _cellLength; n++) {
				_cell = (Element) _cells.item(n);
				itemPropertys = _cell.getElementsByTagName("property");
				_dataType = "";
				_dataSetName = "";
				int _subMax2 = itemPropertys.getLength();
				for ( l = 0; l < _subMax2; l++) {
					itemProperty = (Element) itemPropertys.item(l);
					
					if( itemProperty.getAttribute("name").equals("dataType") )
					{
						_dataType = itemProperty.getAttribute("value");
					}
					else if( itemProperty.getAttribute("name").equals("dataSet") )
					{
						_dataSetName = URLDecoder.decode(itemProperty.getAttribute("value"), "UTF-8");
					}
					else if( itemProperty.getAttribute("name").equals("column") )
					{
						_dataSetColumn = URLDecoder.decode(itemProperty.getAttribute("value"), "UTF-8");
					}
				}
				if( _dataType != "" && _dataType != "0" && _dataSetName != "")
				{
					if( dataColumnDatas.containsKey(_dataSetName) == false )
					{
						dataColumnDatas.put(_dataSetName, new ArrayList<String>());
					}
					if( dataColumnDatas.get(_dataSetName).contains(_dataSetColumn) == false )
					{
						dataColumnDatas.get(_dataSetName).add(_dataSetColumn);
					}
				}
			}
				
		}
		else
		{
			itemPropertys = _item.getElementsByTagName("property");
			_dataType = "";
			_dataSetName = "";
			int _subMax2 = itemPropertys.getLength();
			for ( l = 0; l < _subMax2; l++) {
				itemProperty = (Element) itemPropertys.item(l);
				
				if( itemProperty.getAttribute("name").equals("dataType") )
				{
					_dataType = itemProperty.getAttribute("value");
				}
				else if( itemProperty.getAttribute("name").equals("dataSet") )
				{
					_dataSetName = URLDecoder.decode(itemProperty.getAttribute("value"), "UTF-8");
				}
				else if( itemProperty.getAttribute("name").equals("column") )
				{
					_dataSetColumn = URLDecoder.decode(itemProperty.getAttribute("value"), "UTF-8");
				}
			}
			if( _dataType != "" && _dataType != "0" && _dataSetName != "")
			{
				if( dataColumnDatas.containsKey(_dataSetName) == false )
				{
					dataColumnDatas.put(_dataSetName, new ArrayList<String>());
				}
				if( dataColumnDatas.get(_dataSetName).contains(_dataSetColumn) == false )
				{
					dataColumnDatas.get(_dataSetName).add(_dataSetColumn);
				}
			}
		}
	
		return dataColumnDatas;
	}

	public static HashMap<String,ArrayList<String>> getItemDataPropertyJS( HashMap<String, Value> _item, HashMap<String,ArrayList<String>> dataColumnDatas ) throws UnsupportedEncodingException
	{
		
		int n = 0;
		int l = 0;
		
		NodeList itemPropertys;
		String _dataType = "";
		String _dataSetName = "";
		String _dataSetColumn = "";
		
		_dataType = "";
		_dataSetName = "";
		
		if( _item.containsKey("dataType") )
		{
			_dataType = _item.get("dataType").getStringValue();
		}
		if( _item.containsKey("dataSet") )
		{
			_dataSetName = _item.get("dataSet").getStringValue();
		}
		if( _item.containsKey("column") )
		{
			_dataSetColumn = _item.get("column").getStringValue();
		}
		
		if( _dataType != "" && _dataType != "0" && !_dataSetName.equals("")  && !_dataSetName.equals("null") && !_dataSetColumn.equals("") && !_dataSetColumn.equals("null") )
		{
			if( dataColumnDatas.containsKey(_dataSetName) == false )
			{
				dataColumnDatas.put(_dataSetName, new ArrayList<String>());
			}
			if( dataColumnDatas.get(_dataSetName).contains(_dataSetColumn) == false )
			{
				dataColumnDatas.get(_dataSetName).add(_dataSetColumn);
			}
		}
		
		return dataColumnDatas;
	}
	
	
	
	public static HashMap<String, ArrayList<Object>> convertCrossTabToExcelData( ArrayList<ArrayList<HashMap<String, Value>>>  _pagelist ) throws UnsupportedEncodingException
	{
		ArrayList<Object> _crossDataArr = new ArrayList<Object>();
		HashMap<String, ArrayList<Object>> _crossTabSubDataSet = new HashMap<String, ArrayList<Object>>();
		ArrayList<Object> _subCrossTabData = new ArrayList<Object>();
		int l = 0;
		
		for( l = 0; l < _pagelist.size(); l++ )
		{
			_subCrossTabData = new ArrayList<Object>();
			
			for (int o = 0; o < _pagelist.get(l).size(); o++) {
				if(_pagelist.get(l).get(o) == null)
				{
					_subCrossTabData.add("");
				}
				else if( _pagelist.get(l).get(o).get("text").getStringValue().equalsIgnoreCase("&empty") )
				{
					_subCrossTabData.add("");
				}
				else if( _pagelist.get(l).get(o).get("text").getStringValue().equalsIgnoreCase("summery") )
				{
					if(_crossDataArr.size() > 0 )
					{
						ArrayList<Object> _temp = (ArrayList<Object>) _crossDataArr.get(_crossDataArr.size()-1);
						if(_temp.get(o) != null ) _subCrossTabData.add( _temp.get(o).toString() );
						else _subCrossTabData.add("");
					}
					else
					{
						_subCrossTabData.add("");
					}
				}
				else
				{
					// formatter set
					if( !_pagelist.get(l).get(o).get("formatter").getStringValue().equalsIgnoreCase("null") && !_pagelist.get(l).get(o).get("formatter").getStringValue().equalsIgnoreCase("") )
					{
						String _nation = "";
						String _mask = "";
						String _align = "";
						String _formatString = "";
						int _decimalPointLength = 0;
						boolean _useThousandComma = false;
						boolean _isDecimal = false;
						
						if( _pagelist.get(l).get(o).get("nation") != null )
						{
							_nation = URLDecoder.decode( _pagelist.get(l).get(o).get("nation").getStringValue(), "UTF-8");
						}
						
						//else if( _name.equals("mask") ){
						if( _pagelist.get(l).get(o).get("mask") != null ){	
							_mask = URLDecoder.decode( _pagelist.get(l).get(o).get("mask").getStringValue() , "UTF-8");
						}
						if( _pagelist.get(l).get(o).get("align") != null ){	
							_align =  _pagelist.get(l).get(o).get("align").getStringValue();
						}
						
						if( _pagelist.get(l).get(o).get("decimalPointLength") != null ){
							_decimalPointLength = _pagelist.get(l).get(o).get("decimalPointLength").getIntValue();
						}				
						
						if( _pagelist.get(l).get(o).get("useThousandComma") != null ){	
							_useThousandComma =_pagelist.get(l).get(o).get("useThousandComma").getBooleanValue();
						}		
						if( _pagelist.get(l).get(o).get("isDecimal") != null ){
							_isDecimal = _pagelist.get(l).get(o).get("isDecimal").getBooleanValue();
						}		
						if( _pagelist.get(l).get(o).get("formatString") != null ){
							_formatString = _pagelist.get(l).get(o).get("formatString").getStringValue();
						}	
						
						Object _propValue;
						String _formatValue="";
						_propValue=  _pagelist.get(l).get(o).get("text").getStringValue();
						_formatValue = _propValue.toString();
						try {
							if( _pagelist.get(l).get(o).get("formatter").getStringValue().equalsIgnoreCase("Currency") ){
								_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
							}else if( _pagelist.get(l).get(o).get("formatter").getStringValue().equalsIgnoreCase("Date") ){
								_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
							}else if( _pagelist.get(l).get(o).get("formatter").getStringValue().equalsIgnoreCase("MaskNumber") ){
								_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
							}else if( _pagelist.get(l).get(o).get("formatter").getStringValue().equalsIgnoreCase("MaskString") ){
								_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
							}
						} catch (ParseException e) {
							//e.printStackTrace();
						}
						_subCrossTabData.add( _formatValue );
					}
					else
					{
						_subCrossTabData.add( _pagelist.get(l).get(o).get("text").getStringValue() );
					}
				}
			
			
			}
			if( l == 0)
			{
				_crossTabSubDataSet.put("header", _subCrossTabData);
			}
			else
			{
				_crossDataArr.add(_subCrossTabData);
			}
		}
		
		_crossTabSubDataSet.put("data", _crossDataArr);
		
		return _crossTabSubDataSet;
	}
	
	
	/**
	 * functionName	:	getCanvasVisibleChkeck</br>
	 * desc			:	페이지별 pageVisible값을 이용하여 page Element를 사용여부를 결정
	 * @param _dataSet
	 * @param _page
	 * @throws ScriptException 
	 */
	public static Boolean getPageInfoVisibleChkeck(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, PageInfo _page, HashMap<String, Object>  _param ,Function mFunction ) throws ScriptException
	{
		ArrayList<HashMap<String, String>> _pageVisibleFxList = _page.getUbfunction();
		
		HashMap<String, String> _pageVisibleFx;
		//Function mFunction = new Function(_dataSet, null ); 
		mFunction.setDatasetList(_dataSet);
		String _value = "";
		boolean returnFlag = true;
		
		int _ubfxSize = 0;
		if( _pageVisibleFxList != null )_ubfxSize = _pageVisibleFxList.size();
		
		for (int i = 0; i < _ubfxSize; i++) {
			
			_pageVisibleFx = _pageVisibleFxList.get(i);
			_value = "";
			
			if( _pageVisibleFx.get("property").equals("pageVisible") )
			{
				
				try {
					_value = URLDecoder.decode(_pageVisibleFx.get("value"), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mFunction.setParam(_param);
				mFunction.setDatasetList(_dataSet);
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_value,0, -1 , 0, -1, -1, "" );
				}else{
					_fnValue = mFunction.function(_value,0, -1 , 0, -1, -1);
				}
				
				_fnValue = _fnValue.trim();
				
				if( _fnValue.equals("false") ){
					returnFlag = false;
					break;
				}
				
			}
			
		}
		
		return returnFlag;
	}
	
	public static Boolean getPageInfoVisibleChkeck(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, PageInfoSimple _page, HashMap<String, Object>  _param ,Function mFunction ) throws ScriptException
	{
		ArrayList<HashMap<String, String>> _pageVisibleFxList = _page.getUbfunction();
		
		HashMap<String, String> _pageVisibleFx;
		//Function mFunction = new Function(_dataSet, null ); 
		mFunction.setDatasetList(_dataSet);
		String _value = "";
		boolean returnFlag = true;
		
		int _ubfxSize = 0;
		if( _pageVisibleFxList != null )_ubfxSize = _pageVisibleFxList.size();
		
		for (int i = 0; i < _ubfxSize; i++) {
			
			_pageVisibleFx = _pageVisibleFxList.get(i);
			_value = "";
			
			if( _pageVisibleFx.get("property").equals("pageVisible") )
			{
				
				try {
					_value = URLDecoder.decode(_pageVisibleFx.get("value"), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mFunction.setParam(_param);
				mFunction.setDatasetList(_dataSet);
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_value,0, -1 , 0, -1, -1, "" );
				}else{
					_fnValue = mFunction.function(_value,0, -1 , 0, -1, -1);
				}
				
				_fnValue = _fnValue.trim();
				
				if( _fnValue.equals("false") ){
					returnFlag = false;
					break;
				}
				
			}
			
		}
		
		return returnFlag;
	}
	
	/**
	 * functionName	:	getCanvasVisibleChkeck</br>
	 * desc			:	페이지별 pageVisible값을 이용하여 page Element를 사용여부를 결정
	 * @param _dataSet
	 * @param _page
	 * @throws ScriptException 
	 */
	public static String getCanvasBackgroundFX(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, PageInfo _page, HashMap<String, Object>  _param ,Function mFunction ) throws ScriptException
	{
		ArrayList<HashMap<String, String>> _pageVisibleFxList = _page.getUbfunction();
		HashMap<String, String> _pageVisibleFx;

		mFunction.setDatasetList(_dataSet);
		String _value = "";
		String _retStr = "";
		boolean returnFlag = true;
		
		int _ubfxSize = 0;
		if( _pageVisibleFxList != null )_ubfxSize = _pageVisibleFxList.size();

		for (int i = 0; i < _ubfxSize; i++) {
			
			_pageVisibleFx = _pageVisibleFxList.get(i);
			_value = "";
			
			if( _pageVisibleFx.get("property").equals("pageBackgroundImage") )
			{
				try {
					_value = URLDecoder.decode(_pageVisibleFx.get("value"), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mFunction.setParam(_param);
				mFunction.setDatasetList(_dataSet);
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_value,0, -1 , 0, -1, -1, "" );
				}else{
					_fnValue = mFunction.function(_value,0, -1 , 0, -1, -1);
				}
				
				_fnValue = _fnValue.trim();
				
				_retStr = _fnValue;
			}
			
		}
		
		return _retStr;
	}
	/**
	 * functionName	:	getCanvasVisibleChkeck</br>
	 * desc			:	페이지별 pageVisible값을 이용하여 page Element를 사용여부를 결정
	 * @param _dataSet
	 * @param _page
	 * @throws ScriptException 
	 */
	public static String getCanvasBackgroundFX(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, PageInfoSimple _page, HashMap<String, Object>  _param ,Function mFunction ) throws ScriptException
	{
		ArrayList<HashMap<String, String>> _pageVisibleFxList = _page.getUbfunction();
		HashMap<String, String> _pageVisibleFx;

		mFunction.setDatasetList(_dataSet);
		String _value = "";
		String _retStr = "";
		boolean returnFlag = true;
		
		int _ubfxSize = 0;
		if( _pageVisibleFxList != null )_ubfxSize = _pageVisibleFxList.size();

		for (int i = 0; i < _ubfxSize; i++) {
			
			_pageVisibleFx = _pageVisibleFxList.get(i);
			_value = "";
			
			if( _pageVisibleFx.get("property").equals("pageBackgroundImage") )
			{
				try {
					_value = URLDecoder.decode(_pageVisibleFx.get("value"), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mFunction.setParam(_param);
				mFunction.setDatasetList(_dataSet);
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_value,0, -1 , 0, -1, -1, "" );
				}else{
					_fnValue = mFunction.function(_value,0, -1 , 0, -1, -1);
				}
				
				_fnValue = _fnValue.trim();
				
				_retStr = _fnValue;
			}
			
		}
		
		return _retStr;
	}
	
	/**
	 * functionName	:	getCanvasVisibleChkeck</br>
	 * desc			:	페이지별 pageVisible값을 이용하여 page Element를 사용여부를 결정
	 * @param _dataSet
	 * @param _page
	 * @throws ScriptException 
	 */
	public static Boolean getUBFxChkeckJson(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, Object> _item, HashMap<String, Object>  _param , String propNm, Function _function ) throws ScriptException
	{		
		return getUBFxChkeckJson(_dataSet, _item, _param , propNm , 0,-1, 0, _function);
	}
	
	
	
	/**
	 * functionName	:	getCanvasVisibleChkeck</br>
	 * desc			:	페이지별 pageVisible값을 이용하여 page Element를 사용여부를 결정 
	 * desc			: FreeFormd에서 페이지 별 Check 값 리턴
	 * @param _dataSet
	 * @param _page
	 * @throws ScriptException 
	 */
	public static Boolean getUBFxChkeckJson(HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, Object> _item, HashMap<String, Object>  _param , String propNm ,int _rowIndex, int _totalpageNum, int _currentPageNum, Function _function  ) throws ScriptException
	{
		ArrayList<HashMap<String, String>> _ubfxList  = ( ArrayList<HashMap<String, String>> ) _item.get("ubfx");
		HashMap<String, String> _pageVisibleStr;
		Function mFunction;
		
		if(_function == null ) mFunction = new Function(_dataSet, null ); 
		else mFunction = _function;
		
		String _value = "";
		boolean returnFlag = true;
		
		for (int i = 0; i < _ubfxList.size(); i++) {
			
			_pageVisibleStr = (HashMap<String, String>) _ubfxList.get(i);
			_value = "";
			
			if( _pageVisibleStr.get("property").equals(propNm) )
			{
				
				try {
					_value = URLDecoder.decode(_pageVisibleStr.get("value").toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mFunction.setParam(_param);
				mFunction.setDatasetList(_dataSet);
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_value,_rowIndex, _totalpageNum , _currentPageNum, -1, -1, "" );
				}else{
					_fnValue = mFunction.function(_value,_rowIndex, _totalpageNum , _currentPageNum, -1, -1);
				}
				
				_fnValue = _fnValue.trim();
				
				if( _fnValue.equals("false") ){
					returnFlag = false;
					break;
				}
				
			}
			
		}
		
		return returnFlag;
	}
	
	
}
