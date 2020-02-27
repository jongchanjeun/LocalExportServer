/**
 * LinkedPageParser
 * 유비폼 문서타입중 Linked Page 작업을 처리
 * 2015-11-05
 * 최명진
 */
package org.ubstorm.service.parser;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;
import javax.xml.xpath.XPathExpressionException;

import org.jdom2.output.XMLOutputter;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.parser.formparser.ContinueBandParser;
import org.ubstorm.service.parser.formparser.UBIDataUtilPraser;
import org.ubstorm.service.parser.formparser.UBIMapComparator;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.TempletItemInfo;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.PageInfo;
import org.ubstorm.service.parser.formparser.info.PageInfoSimple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xpath.internal.operations.Bool;

public class LinkedPageParser {

	private UDMParamSet m_appParams = null;
	private String mChartDataFileName = "chartdata.dat";
	
	private HashMap<String,String> mImageData;
	private HashMap<String,Object> mChartData;
	private Function mFunction;
	private float mPageHeight = 0f;
	private int mMinimumResizeFontSize = 0;	// resizeFont 사용시 최소값 지정
	
	private float mPageMarginTop = 0;
	private float mPageMarginLeft = 0;
	private float mPageMarginRight = 0;
	private float mPageMarginBottom = 0;

	private HashMap<String, TempletItemInfo> mTempletInfo;
	
	// Export여부를 판단하기 위한 변수
	private String isExportType = "";
	
	public LinkedPageParser() {
		super();
	}

	public LinkedPageParser(UDMParamSet appParams) {
		super();
		
		m_appParams = appParams;
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
	
	private ArrayList<HashMap<String, Object>> makeColumnData(ArrayList<Element> mPageAr) throws UnsupportedEncodingException
	{
		int i = 0;
		int j = 0;
		
//		NodeList _pageNodes = _project.getElementsByTagName("page");
		Element _page = null;
		NodeList _groupPropertys = null;
		Element _grpElement = null;
		Element _subElement = null;
		
		ArrayList<HashMap<String, Object>> returnAr = new ArrayList<HashMap<String,Object>>();
		int _subLength = 0;
		int _pageLength = mPageAr.size();
		HashMap<String, String> _propertyMap;
		String _visibleResult = "";
		
		for ( i = 1; i < _pageLength; i++) {
			
			_page = mPageAr.get(i);
			
			HashMap<String, Object> _returnHashMap = new HashMap<String, Object>();
			ArrayList<HashMap<String, String>> _columnAr = new ArrayList<HashMap<String,String>>();
			ArrayList<HashMap<String, String>> _ar = new ArrayList<HashMap<String,String>>();
			ArrayList<String> _dataSetAr = new ArrayList<String>();
			
			_grpElement = (Element) _page.getElementsByTagName("groupColumn").item(0);
			if( _grpElement != null )
			{
				_groupPropertys = _grpElement.getElementsByTagName("property");
				_subLength = _groupPropertys.getLength();
				
				for (j = 0; j <_subLength; j++) {
					_subElement = (Element) _groupPropertys.item(j);
					_propertyMap = Value.jsonString2Map( URLDecoder.decode( _subElement.getAttribute("value"), "UTF-8")  );
					_columnAr.add(_propertyMap);
				}
			
			}
			_returnHashMap.put("columns", _columnAr);
			
			
			_subLength = 0;
			if( _page.getElementsByTagName("visible").getLength() > 0 )
			{
				_grpElement = (Element) _page.getElementsByTagName("visible").item(0);
				_groupPropertys = _grpElement.getElementsByTagName("property");
				_subLength = _groupPropertys.getLength();
			}
			
			_visibleResult = _grpElement.getAttribute("result");
			
			for ( j = 0; j < _subLength; j++) {
				
				_subElement = (Element) _groupPropertys.item(j);
				_propertyMap = new HashMap<String, String>();
				try {
					_propertyMap = Value.jsonString2Map( URLDecoder.decode( _subElement.getAttribute("value"), "UTF-8") );
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				_propertyMap.put("visible", _visibleResult);
				_ar.add(_propertyMap);
				
				if(_propertyMap.containsKey("dataset") && _dataSetAr.indexOf(_propertyMap.get("dataset")) == -1 )
				{
					_dataSetAr.add( _propertyMap.get("dataset") );
				}
				
			}
			
			
			if(_ar.size() == 0 && _columnAr.size() > 0 )
			{
				int _columnLenght = 0;
				
				_columnLenght = _columnAr.size();
				_propertyMap = new HashMap<String, String>();
				for( j = 0; j < _columnLenght; j++ )
				{
					if(_dataSetAr.indexOf(_columnAr.get(j).get("dataset")) == -1)
					{
						if( j == 0 )
						{
							_propertyMap.put("dataset", _columnAr.get(j).get("dataset"));
							_propertyMap.put("value", "");
							_propertyMap.put("operation", "");
							_propertyMap.put("column", "");
							_ar.add(_propertyMap);
						}
						if( _dataSetAr.indexOf( _propertyMap.get("dataset") ) == -1 )
						{
							_dataSetAr.add(_propertyMap.get("dataset"));
						}
					}
				}
			}
			
			_returnHashMap.put("visible", _visibleResult );
			_returnHashMap.put("xml", _page);
			_returnHashMap.put("dataSetAr", _dataSetAr);
			_returnHashMap.put("visibleParams", _ar);
			_returnHashMap.put("pageIndex", i);
			
			returnAr.add(_returnHashMap);
			
		}
		
		
		return returnAr;
	}
	
	/**	*/
	private ArrayList<HashMap<String, Object>> makeColumnDataJson(ArrayList<PageInfo> mPageAr) throws UnsupportedEncodingException
	{
		int i = 0;
		int j = 0;
		
		PageInfo _page = null;
		NodeList _groupPropertys = null;
		Element _grpElement = null;
		Element _subElement = null;
		
		ArrayList<HashMap<String, Object>> returnAr = new ArrayList<HashMap<String,Object>>();
		int _subLength = 0;
		int _pageLength = mPageAr.size();
		HashMap<String, String> _propertyMap;
		String _visibleResult = "";
		
		for ( i = 1; i < _pageLength; i++) {
			
			_page = mPageAr.get(i);
			
			HashMap<String, Object> _returnHashMap = new HashMap<String, Object>();
			if( _page.getGroupColumn() == null )
			{
				continue;
			}
			
			ArrayList<HashMap<String, String>> _columnAr = new ArrayList<HashMap<String,String>>();
			ArrayList<HashMap<String, String>> _ar = new ArrayList<HashMap<String,String>>();
			ArrayList<String> _dataSetAr = new ArrayList<String>();

			ArrayList<HashMap<String, String>> _grpColumn = _page.getGroupColumn();
			_subLength = _grpColumn.size();
			
			for (j = 0; j <_subLength; j++) {
				_columnAr.add( _grpColumn.get(j) );
			}
			
			_returnHashMap.put("columns", _columnAr);
			
			_subLength = 0;
			if( _page.getVisibleParam().size() > 0 )
			{
				_subLength = _page.getVisibleParam().size();
			}
			
			_visibleResult = _page.getVisibleResult();
			
			for ( j = 0; j < _subLength; j++) {
				
				_propertyMap = _page.getVisibleParam().get(j);
				_propertyMap.put("visible", _visibleResult);
				_ar.add(_propertyMap);
				
				if(_propertyMap.containsKey("dataset") && _dataSetAr.indexOf(_propertyMap.get("dataset")) == -1 )
				{
					_dataSetAr.add( _propertyMap.get("dataset") );
				}
				
			}
			
			
			if(_ar.size() == 0 && _columnAr.size() > 0 )
			{
				int _columnLenght = 0;
				
				_columnLenght = _columnAr.size();
				_propertyMap = new HashMap<String, String>();
				for( j = 0; j < _columnLenght; j++ )
				{
					if(_dataSetAr.indexOf(_columnAr.get(j).get("dataset")) == -1)
					{
						if( j == 0 )
						{
							_propertyMap.put("dataset", _columnAr.get(j).get("dataset"));
							_propertyMap.put("value", "");
							_propertyMap.put("operation", "");
							_propertyMap.put("column", "");
							_ar.add(_propertyMap);
						}
						if( _dataSetAr.indexOf( _propertyMap.get("dataset") ) == -1 )
						{
							_dataSetAr.add(_propertyMap.get("dataset"));
						}
					}
				}
			}
			
			_returnHashMap.put("visible", _visibleResult );
			_returnHashMap.put("xml", _page);
			_returnHashMap.put("dataSetAr", _dataSetAr);
			_returnHashMap.put("visibleParams", _ar);
			_returnHashMap.put("pageIndex", i);
			
			returnAr.add(_returnHashMap);
			
		}
		
		
		return returnAr;
	}

	
	@SuppressWarnings("unchecked")
	private ArrayList<Object> makeSplitData( ArrayList<HashMap<String, Object>> columnList, HashMap<String, ArrayList<HashMap<String, Object>>> _data)
	{
		int columnLength = columnList.size();
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int dataSetCount = 0;
		HashMap<String, Object> columnData = null;
		int columnDataLength = 0;
		int dataLength = 0;
		String dataSet = "";
		boolean columnChkFlag = false;
		boolean flag = false;
		boolean continueFlag = false;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		
		ArrayList<HashMap<String, String>> visibleParam;
		
		ArrayList<HashMap<String, String>> columnAr;
		
		ArrayList<String> columnsData = new ArrayList<String>();
		ArrayList<String> columnsCheckData = new ArrayList<String>();
		
		ArrayList<HashMap<String, Object>> returnArray = new ArrayList<HashMap<String,Object>>();
		
		int columnsColumnLenght = 0;
		ArrayList<String> convertDataNames = new ArrayList<String>();
		
		for ( i = 0; i < columnLength; i++) {
			
			columnData = columnList.get(i);
			columnData.put("count", 0);
			visibleParam = (ArrayList<HashMap<String, String>>) columnData.get("visibleParams");
			columnDataLength = ((ArrayList<String>) columnData.get("dataSetAr")).size();
			
			
			columnAr = (ArrayList<HashMap<String, String>>) columnData.get("columns");
			
			// group key값이 하나일때, 2번째 페이지 안나오는 현상 처리.kyh	
			dataSetCount = dataSetCount + 1;
			continueFlag = false;
			columnsData.clear();
			//
			
			for ( j = 0; j < columnDataLength; j++) {
				
				dataSetCount = dataSetCount + 1;
				dataSet = ((ArrayList<String>) columnData.get("dataSetAr")).get(j);
				
				dataLength = 0;
				if(_data.containsKey(dataSet))
				{
					dataLength = _data.get(dataSet).size();
				}
				
				for ( k = 0; k < dataLength; k++) {
					
					columnChkFlag = true;
					
					if(visibleParam.size() == 1 && visibleParam.get(0).get("value").equals("") &&
							visibleParam.get(0).get("operation").equals("") && visibleParam.get(0).get("column").equals("") )
					{
						flag = true;
					}
					else
					{
						flag = checkRowData( visibleParam, _data.get(dataSet).get(k), dataSet );
					}
						
					if( k > 0 && continueFlag && !flag )
					{
						dataSetCount = dataSetCount + 1;
						continueFlag = false;
						columnsData.clear();
					}
					
					columnsColumnLenght = columnAr.size();
					
					if( flag && columnsColumnLenght > 0 )
					{
						columnsCheckData = new ArrayList<String>();
						
						for ( l = 0; l <columnsColumnLenght; l++) {
							
							if( dataSet.equals(columnAr.get(l).get("dataset")) && _data.get(dataSet).get(k).containsKey( columnAr.get(l).get("column") ) )
							{
								columnsCheckData.add( _data.get(dataSet).get(k).get( columnAr.get(l).get("column") ).toString() );
								if( columnsData.size() > l && columnsData.get(l).equals(columnsCheckData.get(l)) == false  )
								{
									columnChkFlag = false;
								}
							}
							
						}
						
						if( columnsData.size() > 0 && columnChkFlag == false )
						{
							continueFlag = false;
							dataSetCount = dataSetCount + 1;
						}
						
						columnsData = columnsCheckData;
					}
					
					if(flag)
					{	
						String addDataSetName = "";
						try {
							 addDataSetName = dataSet + "_"+  URLDecoder.decode(BandInfoMapData.M_COLUMN_SEPARATOR,"UTF-8")  + dataSetCount;
						} catch (Exception e) {
							addDataSetName = dataSet + "_"+  BandInfoMapData.M_COLUMN_SEPARATOR  + dataSetCount;
						}
						
						if( continueFlag == false )
						{
							HashMap<String, Object> _addData = new HashMap<String, Object>();
							_addData.put("index", k);
							_addData.put("datasetName", addDataSetName );
							_addData.put("orizinalDataName", dataSet );
							_addData.put("columnIndex", columnData.get("pageIndex"));
							
							if(convertDataNames.indexOf(dataSet)==-1)
							{
								convertDataNames.add(dataSet);
							}
							
							columnData.put("count", Integer.valueOf( columnData.get("count").toString() ) + 1);
							
							returnArray.add(_addData);
						}
						continueFlag = true;
						
						if( newDataSet.containsKey(addDataSetName) == false )
						{
							newDataSet.put(addDataSetName, new ArrayList<HashMap<String,Object>>());
							// orizinalDataSet명 담기
						}
						
						newDataSet.get(addDataSetName).add( _data.get(dataSet).get(k));
					}
					
				
					
				}
				
			}
			
			
		}
		
		if(returnArray.size() > 0 )
		{
			ArrayList<String> sortCol = new ArrayList<String>();
			ArrayList<String> sortDesc = new ArrayList<String>();
			ArrayList<String> sortNumeric = new ArrayList<String>();
			sortCol.add("index");
			sortDesc.add("false");
			sortNumeric.add("true");
			returnArray = sortDataSet(returnArray, sortCol, sortDesc, sortNumeric);
		}
		
		ArrayList<Object> returnLinkedData = new ArrayList<Object>();
		
		returnLinkedData.add(returnArray);
		returnLinkedData.add(newDataSet);
		returnLinkedData.add(convertDataNames);
		
		return returnLinkedData;
	}
	
	
	private Boolean checkRowData(ArrayList<HashMap<String, String>> visibleParams, HashMap<String, Object> rowData, String dataName)
	{
		int i = 0;
		int j = 0;
		
		boolean retFlag = false;
		boolean argoFlag = true;
		String chkFlag = "";
		
		int _int = 0;
		int paramSize = visibleParams.size();
		HashMap<String, String> chkObj;
		boolean _isChkDS = false;
		
		for ( i = 0; i < paramSize; i++) {
		
			chkObj = visibleParams.get(i);
			
			if(chkObj.get("dataset").equals(dataName))
			{ 
				
				_int = checkCompare(rowData.get( chkObj.get("column") ).toString() , chkObj.get("value"), chkObj.get("operation") );
				
				if( _int == 0 )
				{
					retFlag = true;
				}
				else
				{
					retFlag = false;
				}
				
				chkFlag = chkObj.get("visible");
				
				if( _isChkDS )
				{
					
					if( chkObj.get("logical").equals("&&") )
					{
						
						if( retFlag == false || argoFlag == false)
						{
							retFlag = false;
						}
						
					}
					else if( chkObj.get("logical").equals("||") )
					{
						if( retFlag || argoFlag )
						{
							retFlag = true;
						}
					}
					
				}
				
				argoFlag = retFlag;
				_isChkDS = true;
			}
			
		}
		
		
		if( chkFlag.equals("false"))
		{
			if(retFlag) retFlag = false;
			else retFlag = true;
		}
		
		return retFlag;
	}
	
	
	
	
	private Integer checkCompare( String resultValue, String compare, String operator )
	{
		int returnData = 0;
		
		if( operator.equals("!=") )
		{
			if( resultValue.equals(compare) )
			{
				returnData = -1;
			}
			else
			{
				returnData = 0;
			}
		}
		else if( operator.equals("=") || operator.equals("=="))
		{
			if( resultValue.equals(compare) )
			{
				returnData = 0;
			}
			else
			{
				returnData = -1;
			}
		}
		else if( operator.equals(">") )
		{

			if( Float.isNaN( Float.valueOf(resultValue) ) || Float.isNaN( Float.valueOf(compare) ) )
			{
				returnData = -1;
			}
			else if( Float.valueOf(resultValue) > Float.valueOf(compare) )
			{
				returnData = 0;
			}
			else
			{
				returnData = -1;
			}
			
		}
		else if( operator.equals(">=") || operator.equals("=>"))
		{
			if( Float.isNaN( Float.valueOf(resultValue) ) || Float.isNaN( Float.valueOf(compare) ) )
			{
				returnData = -1;
			}
			else if( Float.valueOf(resultValue) >= Float.valueOf(compare) )
			{
				returnData = 0;
			}
			else
			{
				returnData = -1;
			}
		}
		else if( operator.equals("<") )
		{
			if( Float.isNaN( Float.valueOf(resultValue) ) || Float.isNaN( Float.valueOf(compare) ) )
			{
				returnData = -1;
			}
			else if( Float.valueOf(resultValue) < Float.valueOf(compare) )
			{
				returnData = 0;
			}
			else
			{
				returnData = -1;
			}
		}
		else if( operator.equals("<=") || operator.equals("=<"))
		{
			if( Float.isNaN( Float.valueOf(resultValue) ) || Float.isNaN( Float.valueOf(compare) ) )
			{
				returnData = -1;
			}
			else if( Float.valueOf(resultValue) <= Float.valueOf(compare) )
			{
				returnData = 0;
			}
			else
			{
				returnData = -1;
			}
		}
		else
		{
			returnData = -1;
		}
		
		return returnData;
	}
	
	
	
	
	/**
	 * ArrayList를 Sort시키기 위한 함수.
	 * @param _dataAr	Sort할 데이터ArrayList
	 * @param colAr		Sort시킬 컬럼리스트
	 * @param descending	Sort시킬 컬럼별 Descading여부
	 * @param sortNumeric	Sort시킬 컬럼의 Numeric여부
	 * @return
	 */
	private  ArrayList<HashMap<String, Object>> sortDataSet( ArrayList<HashMap<String, Object>> _dataAr, ArrayList<String> colAr,ArrayList<String> descending, ArrayList<String> sortNumeric)
	{
		ArrayList<HashMap<String, Object>> retAr = new ArrayList<HashMap<String,Object>>();

		//sort 시 descend와 numeric값을 이용하여 처리
		if( colAr.size() > 0 ){
			Collections.sort(_dataAr, new UBIMapComparator(colAr, descending, sortNumeric){
				
				@Override
				public int compare(Map<String, Object> o1,
						Map<String, Object> o2) {
					// TODO Auto-generated method stub
					c = 0;
					BigDecimal _o1Dec;
					BigDecimal _o2Dec;

					for (int i = 0; i < mColAr.size(); i++) {
						
						column = mColAr.get(i);
						
						if( mDesAr == null || mDesAr.size() == 0 || mDesAr.get(i) == "false" )
						{
							if( mNumericAr != null && mNumericAr.size() > 0 && mNumericAr.get(i) == "true")
							{
								_o1Dec = new BigDecimal(String.valueOf(o1.get (column)));
								_o2Dec = new BigDecimal(String.valueOf(o2.get (column)));
								c =  _o1Dec.compareTo(_o2Dec);
//								c =  Integer.valueOf( String.valueOf(o1.get (column)) ).compareTo( Integer.valueOf(String.valueOf(o2.get (column)))  );
							}
							else
							{
								c = String.valueOf(o1.get (column)).compareTo(String.valueOf(o2.get (column)) );
							}
						}
						else
						{
							if( mNumericAr != null && mNumericAr.size() > 0 && mNumericAr.get(i) == "true")
							{
								_o1Dec = new BigDecimal(String.valueOf(o1.get (column)));
								_o2Dec = new BigDecimal(String.valueOf(o2.get (column)));
								c =  _o2Dec.compareTo(_o1Dec);
//								c =  Integer.valueOf( String.valueOf(o2.get (column)) ).compareTo( Integer.valueOf(String.valueOf(o1.get (column)))  );
							}
							else
							{
								c = String.valueOf(o2.get (column)).compareTo(String.valueOf(o1.get (column)) );
							}
						}
						if( c != 0 ) return c;
					}
					
					return super.compare(o1, o2);
				}
				
			});
		}
		
		return _dataAr;
	};
	
	
	
	private String mLinkedFormType 			= "BAND"; 
	private String LINK_FORM_TYPE_BAND 		= "BAND"; 
	private String LINK_FORM_TYPE_FREEFORM 	= "FREEFORM"; 
	
	public HashMap<String, Object> loadTotalPage( ArrayList<Element> mPageAr,  HashMap<String, ArrayList<HashMap<String, Object>>> dataSet, ArrayList<Integer> mXAr ) throws UnsupportedEncodingException, XPathExpressionException, ScriptException
	{
		ArrayList<HashMap<String, Object>> pageList;
		pageList = makeColumnData(mPageAr);
		
		ArrayList<Object> _splitData = makeSplitData(pageList, dataSet); 
		
		ArrayList<HashMap<String, Object>> returnArray = (ArrayList<HashMap<String, Object>>) _splitData.get(0);
		HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet =  (HashMap<String, ArrayList<HashMap<String, Object>>>) _splitData.get(1);
		ArrayList<String> converDatas = (ArrayList<String>) _splitData.get(2);
		
		// 각 페이지별 데이터셋과 xml을 이용하여 페이지 구성처리( band타입과 freeform타입을 체크하여 각 페이지별 타입을 생성 )
		
		System.out.print("Linked");
		
		
		int i = 0;
		int j = 0;
		int k = 0;
		int _makePageLength = 0;
		
		if( returnArray.size() > 0 )
		{
			_makePageLength = returnArray.size();
		}
		
		ContinueBandParser continueBandParser = new ContinueBandParser( m_appParams );
		
		continueBandParser.setExportData(isExportData);
		continueBandParser.setIsExportType(isExportType);
		
		continueBandParser.setPageMarginTop(mPageMarginTop);
		continueBandParser.setPageMarginLeft(mPageMarginLeft);
		continueBandParser.setPageMarginRight(mPageMarginRight);
		continueBandParser.setPageMarginBottom(mPageMarginBottom);
		
		continueBandParser.setFunction(mFunction);
		
		continueBandParser.setTempletInfo(mTempletInfo);
		
		Element _page = mPageAr.get(0);
		float pageWidth = Float.valueOf(_page.getAttribute("width"));
		float pageHeight = Float.valueOf(_page.getAttribute("height"));
		
		pageWidth = pageWidth - mPageMarginLeft - mPageMarginRight;
		pageHeight = pageHeight - mPageMarginTop - mPageMarginBottom;
		
		// Clone Page값 담기
		String cloneData = _page.getAttribute("divide");
		boolean clonePage = false;
		
		int _totPage = 0;
		
		// Clone 여부 확인
		if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL))
		{
			if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
			{
				pageHeight = pageHeight/2;
			}
			else
			{
				pageWidth = pageWidth/2;
			}
			clonePage = true;
		}
		mPageHeight = pageHeight;
		String pageDataName = "";
		String orizinalDataName = "";
		int columnIndex = 0;
		HashMap<String, ArrayList<HashMap<String, Object>>> _cloneDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		
		for ( i = 0; i < _makePageLength; i++) {
			
			_cloneDataSet.clear();
			
			pageDataName 		= returnArray.get(i).get("datasetName").toString();
			orizinalDataName  	= returnArray.get(i).get("orizinalDataName").toString();
			columnIndex = Integer.valueOf(returnArray.get(i).get("columnIndex").toString());
			_cloneDataSet.put( orizinalDataName, newDataSet.get(pageDataName));
			
			// 링크폼 타입이 밴드형식일경우
			if( mLinkedFormType.equals(LINK_FORM_TYPE_BAND) )
			{
				
				Element _pageElement= mPageAr.get(columnIndex);
				ArrayList<Element> _elementDatas = new ArrayList<Element>();
				_elementDatas.add(_pageElement);
				
				ArrayList<Object> bandAr = continueBandParser.loadTotalPage(mPageAr.get(columnIndex), _cloneDataSet,0, pageHeight, pageWidth, mXAr , _elementDatas,null );
				HashMap<String, BandInfoMapData> bandInfo = (HashMap<String, BandInfoMapData>) bandAr.get(0);
				ArrayList<BandInfoMapData> bandList =(ArrayList<BandInfoMapData>) bandAr.get(1);
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
				
				
				// group
				HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);				// originalData값으 가지고 있는 객체
				ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
				
				continueBandParser.mOriginalDataMap = originalDataMap;
				continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
				
				returnArray.get(i).put("infoData", bandAr);
				returnArray.get(i).put("pageCnt", pagesRowList.size());
				returnArray.get(i).put("currentDataSet", _cloneDataSet.clone());
				returnArray.get(i).put("currentFunction", continueBandParser.getFunction() );
				
				_totPage = _totPage + pagesRowList.size();
			}
			else
			{
				// 없을경우 FreeForm타입으로 계산하여 처리
				
			}
			
		}
		
		
		
		
		// 전체 페이지 리턴 
		
		// 페이지별 정보값담기
		
		HashMap<String, Object> returnData = new HashMap<String,Object>();
		returnData.put("totPage", _totPage);
		returnData.put("pageDataAr", returnArray);
		returnData.put("newData", newDataSet);
		
		return returnData;
	}
	
	/**	*/
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> loadTotalPageJson( ArrayList<PageInfo> mPageList,  HashMap<String, ArrayList<HashMap<String, Object>>> dataSet, ArrayList<Integer> mXAr ) throws UnsupportedEncodingException, XPathExpressionException, ScriptException
	{
		ArrayList<HashMap<String, Object>> pageList;
		pageList = makeColumnDataJson(mPageList);
		
		ArrayList<Object> _splitData = makeSplitData(pageList, dataSet); 
		
		ArrayList<HashMap<String, Object>> returnArray = (ArrayList<HashMap<String, Object>>) _splitData.get(0);
		HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet =  (HashMap<String, ArrayList<HashMap<String, Object>>>) _splitData.get(1);
		ArrayList<String> converDatas = (ArrayList<String>) _splitData.get(2);
		
		// 각 페이지별 데이터셋과 xml을 이용하여 페이지 구성처리( band타입과 freeform타입을 체크하여 각 페이지별 타입을 생성 )
		
		System.out.print("Linked");
		
		
		int i = 0;
		int j = 0;
		int k = 0;
		int _makePageLength = 0;
		
		if( returnArray.size() > 0 )
		{
			_makePageLength = returnArray.size();
		}
		
		ContinueBandParser continueBandParser = new ContinueBandParser( m_appParams );
		
		continueBandParser.setExportData(isExportData);
		continueBandParser.setIsExportType(isExportType);
		
		continueBandParser.setPageMarginTop(mPageMarginTop);
		continueBandParser.setPageMarginLeft(mPageMarginLeft);
		continueBandParser.setPageMarginRight(mPageMarginRight);
		continueBandParser.setPageMarginBottom(mPageMarginBottom);
		
		continueBandParser.setFunction(mFunction);
		
		continueBandParser.setTempletInfo(mTempletInfo);
		
		PageInfo _page = mPageList.get(0);
		float pageWidth = _page.getWidth();
		float pageHeight = _page.getHeight();
		
		pageWidth = pageWidth - mPageMarginLeft - mPageMarginRight;
		pageHeight = pageHeight - mPageMarginTop - mPageMarginBottom;
		
		// Clone Page값 담기
		String cloneData = _page.getDivide();
		boolean clonePage = false;
		
		int _totPage = 0;
		
		// Clone 여부 확인
		if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL))
		{
			if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
			{
				pageHeight = pageHeight/2;
			}
			else
			{
				pageWidth = pageWidth/2;
			}
			clonePage = true;
		}
		mPageHeight = pageHeight;
		String pageDataName = "";
		String orizinalDataName = "";
		int columnIndex = 0;
		HashMap<String, ArrayList<HashMap<String, Object>>> _cloneDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		
		for ( i = 0; i < _makePageLength; i++) {
			
			_cloneDataSet.clear();
			
			pageDataName 		= returnArray.get(i).get("datasetName").toString();
			orizinalDataName  	= returnArray.get(i).get("orizinalDataName").toString();
			columnIndex = Integer.valueOf(returnArray.get(i).get("columnIndex").toString());
			_cloneDataSet.put( orizinalDataName, newDataSet.get(pageDataName));
			
			// 링크폼 타입이 밴드형식일경우
			if( mLinkedFormType.equals(LINK_FORM_TYPE_BAND) )
			{
				
				PageInfo _pageElement= mPageList.get(columnIndex).clone();
				_pageElement.createItem();
				
				ArrayList<PageInfo> _elementDatas = new ArrayList<PageInfo>();
				_elementDatas.add(_pageElement);
				
				
				ArrayList<Object> bandAr = continueBandParser.loadTotalPageJson( _pageElement, _cloneDataSet,0, pageHeight, pageWidth, mXAr , _page.getProjectInfo().getParam() );
				
				HashMap<String, BandInfoMapData> bandInfo = _pageElement.getBandInfoData();
				ArrayList<BandInfoMapData> bandList = _pageElement.getBandList();
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(0);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(1);
				
				// group
				HashMap<String, String> originalDataMap =  _pageElement.getOriginalDataMap();				// originalData값으 가지고 있는 객체
				ArrayList<ArrayList<String>> groupDataNamesAr = _pageElement.getGroupDataNamesAr();	// 그룹핑된 데이터명을 가지고 있는 객체
				
				continueBandParser.mOriginalDataMap = originalDataMap;
				continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
				
				returnArray.get(i).put("infoData", bandAr);
				returnArray.get(i).put("pageCnt", pagesRowList.size());
				returnArray.get(i).put("currentDataSet", _cloneDataSet.clone());
				returnArray.get(i).put("currentFunction", continueBandParser.getFunction() );
				returnArray.get(i).put("currentPageInfo", _pageElement );
				
				_totPage = _totPage + pagesRowList.size();
			}
			else
			{
				// 없을경우 FreeForm타입으로 계산하여 처리
				
			}
			
		}
		
		// 전체 페이지 리턴 
		
		// 페이지별 정보값담기
		
		HashMap<String, Object> returnData = new HashMap<String,Object>();
		returnData.put("totPage", _totPage);
		returnData.put("pageDataAr", returnArray);
		returnData.put("newData", newDataSet);
		
		return returnData;
	}	

	
	
	
	public ArrayList<HashMap<String, Object>> createLinkedPageItem(int _page, ArrayList<HashMap<String, Object>> retArr, HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet,
			HashMap<String, Object> _param, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum ) throws UnsupportedEncodingException, ScriptException
	{
		
		int i = 0;
		int j = 0;
		int k = 0;
		
		int _pageDataSize = 0;
		
		int _currentPageCnt = _page;
		int _pageTotPageCtn = 0;
		_pageDataSize = retArr.size();
		
		ContinueBandParser continueParser = null;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _cloneDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		
		String pageDataName = "";
		String orizinalDataName = "";
		boolean isPivot = false;
		
		HashMap<String, Object> currentMapData = null;
		// 총 페이지중에서 현재 그려지는 페이지를 찾아서 생성하여 아이템을 담은 리스트를 리턴
		for ( i = 0; i < _pageDataSize; i++) {
			
			currentMapData = retArr.get(i);
			
			_pageTotPageCtn = (Integer) currentMapData.get("pageCnt");
			
			
			
			// 각 페이지중 현재 페이지에 표현할 아이템찾기
			// 타입이 컨티뉴 밴드일경우 밴드형식으로 화면을 처리 아닐경우 프리폼타입으로 처리
			if( _currentPageCnt < _pageTotPageCtn )
			{
				_cloneDataSet.clear();
				
				pageDataName 		= currentMapData.get("datasetName").toString();
				orizinalDataName  	= currentMapData.get("orizinalDataName").toString();
				_cloneDataSet.put( orizinalDataName, newDataSet.get(pageDataName));
				ArrayList<Object> bandAr = (ArrayList<Object>) currentMapData.get("infoData");
				
				_cloneDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) currentMapData.get("currentDataSet");
				
				HashMap<String, BandInfoMapData> bandInfo 	= (HashMap<String, BandInfoMapData>) bandAr.get(0);
				ArrayList<BandInfoMapData> bandList 		= (ArrayList<BandInfoMapData>) bandAr.get(1);
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList 	= (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
				// group
				HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
				ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
				
				if(continueParser == null)
				{
					continueParser = new ContinueBandParser(m_appParams);
				}
				
				continueParser.setIsExportType(isExportType);
				
				continueParser.setFunction( (Function) currentMapData.get("currentFunction") );

				continueParser.setPageMarginTop(mPageMarginTop);
				continueParser.setPageMarginLeft(mPageMarginLeft);
				continueParser.setPageMarginRight(mPageMarginRight);
				continueParser.setPageMarginBottom(mPageMarginBottom);
				
				continueParser.mOriginalDataMap = originalDataMap;
				continueParser.mGroupDataNamesAr = groupDataNamesAr;
				
				continueParser.setTempletInfo(mTempletInfo);
				
				_objects = continueParser.createContinueBandItems(_currentPageCnt,_cloneDataSet,  bandInfo, bandList, pagesRowList, _param, crossTabData,_cloneX,_cloneY,_objects,_totalPageNum, _currentPageNum, isPivot);
				return _objects;
			}
			else
			{
				_currentPageCnt = _currentPageCnt - _pageTotPageCtn;
			}
			
			
		}
		
		
		return _objects;
	}
	
	
	
	public HashMap<String, HashMap<String,ArrayList<Object>>> getAllReportData( ArrayList<Element> _pageAr, ArrayList<HashMap<String, Object>> retArr, HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet )
	{
		
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		
		int _pageDataSize = 0;
		int _subMax = 0;
		
		int _pageCnt = 0;
		
		_pageDataSize = retArr.size();
		
		ContinueBandParser continueParser = null;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _cloneDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		
		String pageDataName = "";
		String orizinalDataName = "";

		String _dataType = "";
		String _dataSetName = "";
		String _dataSetColumn = "";
		String dataName = "";
		
		HashMap<String, Object> currentMapData = null;
		
		HashMap<Integer, HashMap<String,ArrayList<String>>> pageColumnData = new HashMap<Integer, HashMap<String,ArrayList<String>>>();
		HashMap<String,ArrayList<String>> dataColumnDatas = new HashMap<String,ArrayList<String>>();
		HashMap<String, ArrayList<String>> realDataCoumns = new HashMap<String,ArrayList<String>>();
		
		HashMap<String, HashMap<String,ArrayList<Object>>> resultDataSet = new HashMap<String, HashMap<String,ArrayList<Object>>>();
		
		NodeList itemPropertys = null;
		Element itemProperty = null;
		// 총 페이지중에서 현재 그려지는 페이지를 찾아서 생성하여 아이템을 담은 리스트를 리턴
		for ( i = 0; i < _pageDataSize; i++) {
			
			currentMapData = retArr.get(i);
			
			realDataCoumns.clear();
			
			HashMap<String, HashMap<String, ArrayList<Object>>> _crossTabDataSet = null;
			
			_cloneDataSet.clear();
			
			pageDataName 		= currentMapData.get("datasetName").toString();
			orizinalDataName  	= currentMapData.get("orizinalDataName").toString();
			_cloneDataSet.put( orizinalDataName, newDataSet.get(pageDataName));
			ArrayList<Object> bandAr = (ArrayList<Object>) currentMapData.get("infoData");
			
			HashMap<String, BandInfoMapData> bandInfo 	= (HashMap<String, BandInfoMapData>) bandAr.get(0);
			ArrayList<BandInfoMapData> bandList 		= (ArrayList<BandInfoMapData>) bandAr.get(1);
			ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList 	= (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
			HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
			// group
			HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
			ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
			
			if(continueParser == null)
			{
				continueParser = new ContinueBandParser(m_appParams);
			}
			continueParser.setExportData(isExportData);
			continueParser.mOriginalDataMap = originalDataMap;
			continueParser.mGroupDataNamesAr = groupDataNamesAr;
			
			
			_pageCnt = Integer.valueOf(currentMapData.get("columnIndex").toString()); 
			
			if(pageColumnData.containsKey( _pageCnt ) == false )
			{
				// 아이템의 property에서 데이터셋과 Column값을 추출하기( 이미 담긴 페이지일경우 담긴데이터를 사용 )
				dataColumnDatas = new HashMap<String,ArrayList<String>>();
				
				NodeList _items = _pageAr.get(_pageCnt).getElementsByTagName("item");
				
				Element _item = null;
				_subMax = _items.getLength();
				for ( k = 0; k < _subMax; k++) {
					_item = (Element) _items.item(k);
					
					try {
						dataColumnDatas = UBIDataUtilPraser.getItemDataProperty( _item, dataColumnDatas);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				pageColumnData.put(_pageCnt, dataColumnDatas);
			}
			else
			{
				dataColumnDatas = pageColumnData.get(_pageCnt);
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
						_crossTabSubDataSet = UBIDataUtilPraser.convertCrossTabToExcelData(_pagelist);
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
//						for ( m = 0; m < dataColumnDatas.get(_dataSetName).size(); m++) {
						int _subMax2 = dataColumnDatas.get(_dataSetName).size();
						for (int n = 0; n < _subMax2; n++) {
							if( realDataCoumns.get(dataNameAr.get(l)).contains( dataColumnDatas.get(_dataSetName).get(n) ) == false )
							{
								realDataCoumns.get(dataNameAr.get(l)).add( dataColumnDatas.get(_dataSetName).get(n) );
							}
						}
//						}
						
					}
				}
				
			}
			
			ArrayList<Object> _headerAr = null;
			ArrayList<Object> _dataAr = null;
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
				
				if( _cloneDataSet.containsKey(_str) == false ) continue;
				
				_dataMaxRowCnt = _cloneDataSet.get(_str).size();
				for ( j = 0; j < _dataMaxRowCnt; j++) {
					
					_rowData = new ArrayList<String>();
					
					for ( k = 0; k < _dataMaxCnt; k++) {
						_colName = realDataCoumns.get(_str).get(k);
						
						if( _cloneDataSet.get(_str).get(j).containsKey(_colName) == false ) continue;
						
						if(j == 0 )
						{
							_headerAr.add(_colName);
						}
					
						_rowData.add( _cloneDataSet.get(_str).get(j).get(_colName).toString()); 
						
					}
					_dataAr.add(_rowData);
				}
				
				_resultDataMap.put("header",  _headerAr);
				_resultDataMap.put("data",  _dataAr);

				resultDataSet.put("LinkedDataSet"+ i + "_" + _str, _resultDataMap);
			}
			
			//ConnectPage 또는 LinkedPage 의 경우
			if( _crossTabDataSet != null && _crossTabDataSet.size() > 0  ){
				
				for (String _dName : _crossTabDataSet.keySet()) {
					resultDataSet.put("LinkedDataSet"+ i + "_" + _dName, _crossTabDataSet.get(_dName) );
				}
					
			}
			
			
		} //for I 종료 
		

		
		
		return resultDataSet;
	}
	
	
	public HashMap<String, HashMap<String,ArrayList<Object>>> getAllReportDataJson( ArrayList<PageInfo> _pageAr, ArrayList<HashMap<String, Object>> retArr, HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet )
	{
		
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		
		int _pageDataSize = 0;
		int _subMax = 0;
		
		int _pageCnt = 0;
		
		_pageDataSize = retArr.size();
		
		ContinueBandParser continueParser = null;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _cloneDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		
		String pageDataName = "";
		String orizinalDataName = "";

		String _dataType = "";
		String _dataSetName = "";
		String _dataSetColumn = "";
		String dataName = "";
		
		HashMap<String, Object> currentMapData = null;
		
		HashMap<Integer, HashMap<String,ArrayList<String>>> pageColumnData = new HashMap<Integer, HashMap<String,ArrayList<String>>>();
		HashMap<String,ArrayList<String>> dataColumnDatas = new HashMap<String,ArrayList<String>>();
		HashMap<String, ArrayList<String>> realDataCoumns = new HashMap<String,ArrayList<String>>();
		
		HashMap<String, HashMap<String,ArrayList<Object>>> resultDataSet = new HashMap<String, HashMap<String,ArrayList<Object>>>();
		
		NodeList itemPropertys = null;
		Element itemProperty = null;
		// 총 페이지중에서 현재 그려지는 페이지를 찾아서 생성하여 아이템을 담은 리스트를 리턴
		for ( i = 0; i < _pageDataSize; i++) {
			
			currentMapData = retArr.get(i);
			
			realDataCoumns.clear();
			
			HashMap<String, HashMap<String, ArrayList<Object>>> _crossTabDataSet = null;
			
			_cloneDataSet.clear();
			
			pageDataName 		= currentMapData.get("datasetName").toString();
			orizinalDataName  	= currentMapData.get("orizinalDataName").toString();
			_cloneDataSet.put( orizinalDataName, newDataSet.get(pageDataName));
			ArrayList<Object> bandAr = (ArrayList<Object>) currentMapData.get("infoData");
			
			_cloneDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) currentMapData.get("currentDataSet");
			PageInfo _currentPage = (PageInfo) currentMapData.get("currentPageInfo");
			
			HashMap<String, BandInfoMapData> bandInfo 	= _currentPage.getBandInfoData();
			ArrayList<BandInfoMapData> bandList 		=  _currentPage.getBandList();
			
			ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList 	= (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(0);
			HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(1);
			// group
			HashMap<String, String> originalDataMap = _currentPage.getOriginalDataMap();		// originalData값으 가지고 있는 객체
			ArrayList<ArrayList<String>> groupDataNamesAr = _currentPage.getGroupDataNamesAr();	// 그룹핑된 데이터명을 가지고 있는 객체
			///////////////
			
			
			if(continueParser == null)
			{
				continueParser = new ContinueBandParser(m_appParams);
			}
			continueParser.setExportData(isExportData);
			continueParser.mOriginalDataMap = originalDataMap;
			continueParser.mGroupDataNamesAr = groupDataNamesAr;
			
			
			_pageCnt = Integer.valueOf(currentMapData.get("columnIndex").toString()); 
			
			if(pageColumnData.containsKey( _pageCnt ) == false )
			{
				// 아이템의 property에서 데이터셋과 Column값을 추출하기( 이미 담긴 페이지일경우 담긴데이터를 사용 )
				dataColumnDatas = new HashMap<String,ArrayList<String>>();
				
				if( _pageAr.get(_pageCnt).getItems().size() == 0 )  _pageAr.get(_pageCnt).createItem();
				
				ArrayList<HashMap<String, Value>> _items = _pageAr.get(_pageCnt).getItems();
				
				HashMap<String, Value> _item = null;
				_subMax = _items.size();
				
				for ( k = 0; k < _subMax; k++) {
					_item = _items.get(k);
					
					try {
						dataColumnDatas = UBIDataUtilPraser.getItemDataPropertyJS( _item, dataColumnDatas);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				pageColumnData.put(_pageCnt, dataColumnDatas);
			}
			else
			{
				dataColumnDatas = pageColumnData.get(_pageCnt);
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
						_crossTabSubDataSet = UBIDataUtilPraser.convertCrossTabToExcelData(_pagelist);
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
//						for ( m = 0; m < dataColumnDatas.get(_dataSetName).size(); m++) {
						int _subMax2 = dataColumnDatas.get(_dataSetName).size();
						for (int n = 0; n < _subMax2; n++) {
							if( realDataCoumns.get(dataNameAr.get(l)).contains( dataColumnDatas.get(_dataSetName).get(n) ) == false )
							{
								realDataCoumns.get(dataNameAr.get(l)).add( dataColumnDatas.get(_dataSetName).get(n) );
							}
						}
//						}
						
					}
				}
				
			}
			
			ArrayList<Object> _headerAr = null;
			ArrayList<Object> _dataAr = null;
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
				
				if( _cloneDataSet.containsKey(_str) == false ) continue;
				
				_dataMaxRowCnt = _cloneDataSet.get(_str).size();
				for ( j = 0; j < _dataMaxRowCnt; j++) {
					
					_rowData = new ArrayList<String>();
					
					for ( k = 0; k < _dataMaxCnt; k++) {
						_colName = realDataCoumns.get(_str).get(k);
						
						if( _cloneDataSet.get(_str).get(j).containsKey(_colName) == false ) continue;
						
						if(j == 0 )
						{
							_headerAr.add(_colName);
						}
					
						_rowData.add( _cloneDataSet.get(_str).get(j).get(_colName).toString()); 
						
					}
					_dataAr.add(_rowData);
				}
				
				_resultDataMap.put("header",  _headerAr);
				_resultDataMap.put("data",  _dataAr);

				resultDataSet.put("LinkedDataSet"+ i + "_" + _str, _resultDataMap);
			}
			
			//ConnectPage 또는 LinkedPage 의 경우
			if( _crossTabDataSet != null && _crossTabDataSet.size() > 0  ){
				
				for (String _dName : _crossTabDataSet.keySet()) {
					resultDataSet.put("LinkedDataSet"+ i + "_" + _dName, _crossTabDataSet.get(_dName) );
				}
					
			}
			
			
		} //for I 종료 
		
		
		return resultDataSet;
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
	

	
	
	public ArrayList<HashMap<String, Object>> createLinkedPageItemJson(int _page, ArrayList<HashMap<String, Object>> retArr, HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet,
			HashMap<String, Object> _param, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum ) throws UnsupportedEncodingException, ScriptException
	{
		
		int i = 0;
		int j = 0;
		int k = 0;
		
		int _pageDataSize = 0;
		
		int _currentPageCnt = _page;
		int _pageTotPageCtn = 0;
		_pageDataSize = retArr.size();
		
		ContinueBandParser continueParser = null;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _cloneDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		
		String pageDataName = "";
		String orizinalDataName = "";
		boolean isPivot = false;
		
		HashMap<String, Object> currentMapData = null;
		// 총 페이지중에서 현재 그려지는 페이지를 찾아서 생성하여 아이템을 담은 리스트를 리턴
		for ( i = 0; i < _pageDataSize; i++) {
			
			currentMapData = retArr.get(i);
			
			_pageTotPageCtn = (Integer) currentMapData.get("pageCnt");
			
			
			
			// 각 페이지중 현재 페이지에 표현할 아이템찾기
			// 타입이 컨티뉴 밴드일경우 밴드형식으로 화면을 처리 아닐경우 프리폼타입으로 처리
			if( _currentPageCnt < _pageTotPageCtn )
			{
				_cloneDataSet.clear();
				
				pageDataName 		= currentMapData.get("datasetName").toString();
				orizinalDataName  	= currentMapData.get("orizinalDataName").toString();
				_cloneDataSet.put( orizinalDataName, newDataSet.get(pageDataName));
				ArrayList<Object> bandAr = (ArrayList<Object>) currentMapData.get("infoData");
				
				_cloneDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) currentMapData.get("currentDataSet");
				PageInfo _currentPage = (PageInfo) currentMapData.get("currentPageInfo");
				
				HashMap<String, BandInfoMapData> bandInfo 	= _currentPage.getBandInfoData();
				ArrayList<BandInfoMapData> bandList 		=  _currentPage.getBandList();
				
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList 	= (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(0);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(1);
				// group
				HashMap<String, String> originalDataMap = _currentPage.getOriginalDataMap();		// originalData값으 가지고 있는 객체
				ArrayList<ArrayList<String>> groupDataNamesAr = _currentPage.getGroupDataNamesAr();	// 그룹핑된 데이터명을 가지고 있는 객체
				
				if(continueParser == null)
				{
					continueParser = new ContinueBandParser(m_appParams);
				}
				
				continueParser.setIsExportType(isExportType);
				
				continueParser.setFunction( (Function) currentMapData.get("currentFunction") );

				continueParser.setPageMarginTop(mPageMarginTop);
				continueParser.setPageMarginLeft(mPageMarginLeft);
				continueParser.setPageMarginRight(mPageMarginRight);
				continueParser.setPageMarginBottom(mPageMarginBottom);
				
				continueParser.mOriginalDataMap = originalDataMap;
				continueParser.mGroupDataNamesAr = groupDataNamesAr;
				
				continueParser.setTempletInfo(mTempletInfo);
				
				_objects = continueParser.createContinueBandItems(_currentPageCnt,_cloneDataSet,  bandInfo, bandList, pagesRowList, _param, crossTabData,_cloneX,_cloneY,_objects,_totalPageNum, _currentPageNum, isPivot);
				return _objects;
			}
			else
			{
				_currentPageCnt = _currentPageCnt - _pageTotPageCtn;
			}
			
			
		}
		
		
		return _objects;
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> loadTotalPageSimple( ArrayList<PageInfoSimple> mPageList,  HashMap<String, ArrayList<HashMap<String, Object>>> dataSet, ArrayList<Integer> mXAr ) throws UnsupportedEncodingException, XPathExpressionException, ScriptException
	{
		
		return null;
	}
	
	
	
	
}
