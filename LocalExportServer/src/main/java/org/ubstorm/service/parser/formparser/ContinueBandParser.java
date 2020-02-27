/**
 * continueBand를 처리하여 페이지별 아이템을 내보내는 Class
 * 작성자 : 최명진
 * 작성일 : 2015-10-06
 * 수정일 : 2015-10-06
 */
package org.ubstorm.service.parser.formparser;

import java.awt.Dimension;
import java.awt.FontFormatException;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.poi.ss.usermodel.Workbook;
import org.fit.cssbox.demo.ImageRenderer;
import org.jdom2.JDOMException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.GroupingDataSetProcess;
import org.ubstorm.service.parser.ubFormToExcelBase;
import org.ubstorm.service.parser.ubFormToPDF;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.BandInfoMapDataSimple;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.TempletItemInfo;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.PageInfo;
import org.ubstorm.service.parser.formparser.info.PageInfoSimple;
import org.ubstorm.service.parser.formparser.info.ProjectInfo;
import org.ubstorm.service.parser.formparser.info.item.UBComponent;
import org.ubstorm.service.parser.svg.SvgRichTextObjectHandler;
import org.ubstorm.service.parser.svg.SvgSplit;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;

public class ContinueBandParser {

	
	private UDMParamSet m_appParams = null;
	private String mChartDataFileName = "chartdata.dat";
	
	private HashMap<String,String> mImageData;
	private HashMap<String,Object> mChartData;
	private Function mFunction;
	private int mMinimumResizeFontSize = 0;	// resizeFont 사용시 최소값 지정
	

	// Export여부를 판단하기 위한 변수
	private String isExportType = "";
	private String isExcelOption = "";
	private boolean mFitOnePage = false;
	private float mFitOnePageHeight = 0;
	
	// crossTab의 max Width값을 담기 위한 객체
	private float mBandMaxWidth = 0;
	
	private float mPageMarginTop = 0;
	private float mPageMarginLeft = 0;
	private float mPageMarginRight = 0;
	private float mPageMarginBottom = 0;
	private HashMap<String, TempletItemInfo> mTempletInfo;
	
	protected HashMap<String, Object> changeItemList;
	public void setChangeItemList( HashMap<String, Object> _value)
	{
		changeItemList = _value;
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
	
	public ContinueBandParser() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public ContinueBandParser(UDMParamSet appParams) {
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
	
	public void setFitOnePage(boolean _fitOnePage )
	{
		mFitOnePage = _fitOnePage;
	}
	
	public float getFitOnePageHeight()
	{
		return mFitOnePageHeight;
	}
	
	
	public HashMap<String, String> mOriginalDataMap = null;			// originalData값으 가지고 있는 객체
	public ArrayList<ArrayList<String>> mGroupDataNamesAr = null;	// 그룹핑된 데이터명을 가지고 있는 객체
	
	ArrayList<ArrayList<String>> groupingDataSetNameAr = new ArrayList<ArrayList<String>>();
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
	
	HashMap<String, ArrayList<HashMap<String, Object>>> DataSet;
	 HashMap<String, Object> mParam;
	
	ArrayList<String> OriginalRequiredItemList = new ArrayList<String>();	// 페이지의 필수 값을 가진 객체의 ID를 담아둔 배열
	ArrayList<String> OriginalTabIndexItemList = new ArrayList<String>();	// 페이지별 탭 인덱스를 가진 객체의 ID를 담아둔 배열
	ArrayList<Integer> _pageXArr;
	
	public ArrayList<Object> loadTotalPage( Object _page, HashMap<String, ArrayList<HashMap<String, Object>>> _data,float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr, Object _allPageList, HashMap<String, Object> _param  ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		HashMap<String, String> originalDataMap = new HashMap<String, String>();
		Boolean repeatPageHeader = false;
		Boolean repeatPageFooter = false;
		NodeList _propertys;
//		NodeList _child = _page.getElementsByTagName("item");
		ArrayList<BandInfoMapData> bandList = new ArrayList<BandInfoMapData>();
		HashMap<String, BandInfoMapData> bandInfoData = new HashMap<String, BandInfoMapData>();
		DataSet = _data;
		mParam = _param;
		String groupName = "";
		String prevHeaderBandName = "";
		String _dataBandName = "";
		String _summeryBandName = "";
		String _itemId = "";
		String _className = "";
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		
		mPageHeight = pageHeight;
		mPageWidth = pageWidth;
		
		ArrayList<String> _grpSubDataNameAr = new ArrayList<String>();
		ArrayList<ArrayList<String>> groupDataNamesAr = new ArrayList<ArrayList<String>>();
		ArrayList<Element> _child = null;
		
		NodeList _allPageNodes = null;
		ArrayList<Element> _allPageElements = null;
		
		
		// groupBand 의 Group Count를 담기위한 객체
		HashMap<String, Integer> _groupBandCntMap = new HashMap<String, Integer>();
		
		if( _allPageList != null && _page instanceof Element )
		{
			// 총 페이지에서 현재 페이지의 인덱스를 구한후 이후 페이지중 Continue값이 연속되는 페이지의 경우 _child에 추가 시키는 작업 진행
			int _m = 0;
			int _max = 0;
			int _pageIdx = -1;
			int _maxIdx = -1;
			int _startIdx = 0;
			
			if( ((Element)_page).hasAttribute("isConnect") && ((Element)_page).getAttribute("isConnect") != null )
			{
				if(_allPageList instanceof NodeList)
				{
					_allPageNodes = (NodeList) _allPageList;
					_max = _allPageNodes.getLength();
					_startIdx = 0;
				}
				else
				{
					_allPageElements = (ArrayList<Element>) _allPageList;
					_max = _allPageElements.size();
					_startIdx = _allPageElements.indexOf(_page);
				}
				
				Element _tempElement = null;
				for ( _m = _startIdx;_m < _max; _m++) {
					if( _allPageNodes != null) _tempElement = (Element) _allPageNodes.item(_m);
					else if( _allPageElements != null ) _tempElement = _allPageElements.get(_m);
					if( _pageIdx == -1 )
					{
						if( _tempElement == _page)
						{
							_pageIdx = _m;
							_maxIdx  = _pageIdx;
						}
						
					}
					else
					{
						if(_tempElement.hasAttribute("isConnect") == false || _tempElement.getAttribute("isConnect") == null || !_tempElement.getAttribute("isConnect").equals("true") )
						{
							break;
						}
						else
						{
							_maxIdx = _maxIdx + 1;
						}
					}
				}
				
			}
			
			_child = new ArrayList<Element>();
			NodeList _nodes = ((Element) _page).getElementsByTagName("item");
			int _nodesLength = _nodes.getLength();
			for ( i = 0; i < _nodesLength; i++) {
				_child.add((Element) _nodes.item(i) );
			}
			
			if( _maxIdx > _pageIdx )
			{
				NodeList _subNodes = null;
				boolean _chkFlag = true;
				Element _tmpPageElement = null;
				
				for( _m = _pageIdx+1; _m < _maxIdx+1; _m++  )
				{
					
					if(_allPageNodes != null)_tmpPageElement = (Element) _allPageNodes.item(_m);
					else if(_allPageElements != null) _tmpPageElement = _allPageElements.get(_m);
					
					_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck( _data, _tmpPageElement, _param,mFunction);
					
					if(_chkFlag )
					{
						OriginalRequiredItemList = getRequiredItemDataInfo( _tmpPageElement, OriginalRequiredItemList);
						OriginalTabIndexItemList = getTabIndexItemDataInfo( _tmpPageElement, OriginalTabIndexItemList);
						
						_subNodes = _tmpPageElement.getElementsByTagName("item");
						
						_nodesLength =  _subNodes.getLength();
						for ( i = 0; i < _nodesLength; i++) {
							_child.add((Element) _subNodes.item(i) );
						}
					}
					
				}
			}
			else
			{
				OriginalRequiredItemList = getRequiredItemDataInfo( _page, OriginalRequiredItemList);
				OriginalTabIndexItemList = getTabIndexItemDataInfo( _page, OriginalTabIndexItemList);
			}
			
		}
		else
		{
			// masterBand일경우 page Element가 아니라 아이템이 담긴 Array가 넘어오게됨
			_child = (ArrayList<Element>) _page;
			if( _child.get(0).getTagName().equals("page") )
			{
				Element _pageElement = _child.remove(0);
				OriginalRequiredItemList = getRequiredItemDataInfo( _pageElement, OriginalRequiredItemList);
				OriginalTabIndexItemList = getTabIndexItemDataInfo( _pageElement, OriginalTabIndexItemList);
			}
//			_child.remove(0);
			
		}
		
		ArrayList<BandInfoMapData> grpList = new ArrayList<BandInfoMapData>();
		
		Boolean _subFooterBandFlag = false;
		
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
		
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		
		float headerBandHeight = 0f;
		ArrayList<String> bandSequenceAr = new ArrayList<String>();
		
		// 그룹밴드용 아이템처리
		int _grpDataIndex = 0;
		float _grpDefaultHeight = 0;
		float _grpHeight = 0;
		int _grpSubMaxLength = 0;
//		float _pageWidth = Float.valueOf( _page.getAttribute("width") );
		float _pageWidth = pageWidth;
		
		String _grpDataName = "";
		String _grpSubDataName = "";
		String _grpSubBandName2 = "";
		String _grpSubBandName  = "";
		BandInfoMapData _subCloneBandData;
		String _subFooterBandStr = "";
		ArrayList<ArrayList<HashMap<String, Object>>> _grpSubDataListAr = new ArrayList<ArrayList<HashMap<String,Object>>>();
		
		
		// xml Item
		int _childSize = _child.size();
//		for(i = 0; i < _child.getLength() ; i++)
		for(i = 0; i < _childSize ; i++)
		{
			_childItem = (Element) _child.get(i);
	
			_itemId = _childItem.getAttribute("id");
			_className = _childItem.getAttribute("className");
			
			
			// xml의 모든 Band정보를 뽑아서 밴드별 정보를 담기
			if( _className.length() > 4 && _className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) {
					
				BandInfoMapData  bandData = new BandInfoMapData(_childItem);
				
				bandInfoData.put(bandData.getId(), bandData);	// 생성된 밴드 데이터를 ID를 Key값으로 하여 맵에 담아두기
				
				if( !bandData.getVisible() && !bandData.getClassName().equals(BandInfoMapData.GROUP_FOOTER_BAND) )
				{
					continue;
				}
				
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
//					groupName = "";
//					prevHeaderBandName = "";
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
					bandSequenceAr =  new ArrayList<String>();
					
					prevHeaderBandName = bandData.getId();
					
					if(groupName.equals("") == false )
					{
						bandData.setGroupBand(groupName);
					}
				}
				else if(  _className.equals("UBEmptyBand")  )
				{
					bandSequenceAr =  new ArrayList<String>();
					
					if(groupName.equals("") == false )
					{
						bandData.setGroupBand(groupName);
					}
				}
				else if(  _className.equals("UBGroupHeaderBand")  )
				{
					groupName = bandData.getId();
					prevHeaderBandName = "";
					
					bandSequenceAr =  new ArrayList<String>();
					
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
					
					ArrayList<Object> retData = GroupingDataSetProcess.changeGroupDataSet(DataSet, groupName, bandData.getDataSet(), bandData.getColumnAr().get(0), orderBy.get(0), bandData.getSort(), filterColAr, filterOperatorAr, filterTextAr, bandData.getOriginalOrder(), originalDataMap );
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
					
					bandSequenceAr =  new ArrayList<String>();
				}
				else if(  _className.equals("UBCrossTabBand")  )
				{
					
					bandSequenceAr =  new ArrayList<String>();
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
					if( i+1 < _child.size() )
					{
//						Element nextItem = (Element) _child.item(i+1);
						Element nextItem = (Element) _child.get(i+1);
						nextChildClass = nextItem.getAttribute("className");
					}
					
					boolean _isGroupEndFlag = false;
						
					if( bandData.getClassName().equals("UBGroupFooterBand") && nextChildClass.equals("") == false  && nextChildClass.equals("UBGroupFooterBand") == false )
					{
						_isGroupEndFlag = true;
					}
					else if( nextChildClass.equals(BandInfoMapData.PAGE_FOOTER_BAND) )
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
						BandInfoMapData _bandInfoMapData = bandInfoData.get(groupName);
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
								_grpSubDataListAr = GroupingDataSetProcess.changeGroupDataSetSub(_data, groupName, _grpDataName, _bandInfoMapData.getColumnAr(), bandInfoData.get(groupName).getOrderBy(), _bandInfoMapData.getSort(), _bandInfoMapData.getOriginalOrder(), originalDataMap);
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
							ArrayList<BandInfoMapData> _argoDataBands = null;
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
												
												DataSet.put(_grpSubDataName, _grpSubDataListAr.get(j));
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
													_argoDataBands = new ArrayList<BandInfoMapData>();
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
											if( _subCloneBandData.getVisible() ) bandList.add(_subCloneBandData);
											
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
									if( _subCloneBandData.getVisible() ) bandList.add(_subCloneBandData);
									
									if( _subCloneBandData.getNewPage() && _argoDataBands != null )
									{
										for (BandInfoMapData _argoDataBand : _argoDataBands) {
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
									if( _subCloneBandData.getVisible() ) bandList.add(_subCloneBandData);
									
									if( _subCloneBandData.getNewPage() && _argoDataBands != null )
									{
										for (BandInfoMapData _argoDataBand : _argoDataBands) {
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
						grpList = new ArrayList<BandInfoMapData>();
						
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
					
					if( bandData.getVisible() ) bandList.add( bandData );
					
				}
				
				
				
			}
			
		}
		
		
		if( groupName.equals("") == false && grpList.size() > 0 )
		{
			_grpDefaultHeight = 0;
			_grpSubDataNameAr = new ArrayList<String>();
			BandInfoMapData _bandInfoMapData = bandInfoData.get(groupName);
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
					_grpSubDataListAr = GroupingDataSetProcess.changeGroupDataSetSub(_data, groupName, _grpDataName, _bandInfoMapData.getColumnAr(), _bandInfoMapData.getOrderBy(), _bandInfoMapData.getSort(), _bandInfoMapData.getOriginalOrder(), originalDataMap);
					_bandInfoMapData.setSubPage( String.valueOf( _grpSubDataListAr.size() ) );
					_grpSubMaxLength = _grpSubDataListAr.size();
				}
				else
				{
					_grpSubMaxLength = 1;
				}
				
				
				// newPage속성을 지정하기 위해 담아두기
//				BandInfoMapData _argoDataBand = null;
				ArrayList<BandInfoMapData> _argoDataBands = null;
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
									
									DataSet.put(_grpSubDataName, _grpSubDataListAr.get(j));
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
										_argoDataBands = new ArrayList<BandInfoMapData>();
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
							for (BandInfoMapData _argoDataBand : _argoDataBands) {
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
							for (BandInfoMapData _argoDataBand : _argoDataBands) {
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
		
		
		
		
		// 생성된 밴드리스트를 이용하여 그룹핑 및 총 페이지 구하기
		
		
		HashMap<String, Value> itemProperty = new HashMap<String, Value>();
		HashMap<String, Value> tableMapProperty = new HashMap<String, Value>();
 		HashMap<String, Value> tableProperty;// = new HashMap<String, Value>()
		String _itemDataSetName = "";
		String _propertyName = "";
		String _propertyValue = "";
		String _propertyType = "";
		float _tableArogHeight = 0f;
		// page의 아이템들을 로드하여 각 아이템별 band명을 이용하여 밴드의 데이터셋과 row수를 구하기
		
//		for(i = 0; i < _child.getLength() ; i++)
		int colIndex = 0;
		int rowIndex = 0;
		float updateX = 0;
		float updateY = 0;
		int _childSize2 = _child.size();
		for(i = 0; i < _childSize2 ; i++)
		{
//			_childItem = (Element) _child.item(i);
			_childItem = (Element) _child.get(i);
	
			_itemId = _childItem.getAttribute("id");
			_className = _childItem.getAttribute("className");
			
			if( _className.equals("UBTable") || _className.equals("UBApproval"))
			{
				
				String _includeLayoutType = "";
				ArrayList<Integer> _lastCellIdx = new ArrayList<Integer>();
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
				
				// visible 값이 false일때 cell생성하지 않도록 수정
				if( tableProperty.containsKey("visible") && tableProperty.get("visible").getStringValue().equals("false")) continue;
				
				if( tableProperty.containsKey("version") &&  tableProperty.get("version").getStringValue().equals(ItemConvertParser.TABLE_VERSION_NEW))
				{
					_newTalbeFlag = true;
				}
				
				if( tableProperty.containsKey("includeLayoutType"))
				{
					_includeLayoutType = tableProperty.get("includeLayoutType").getStringValue();
				}
				
				tableProperty.put("tableId", new Value( _itemId,"string"));
				
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
					int _colSize = 0;
					if( _allTableMap.size() > 0 )
					{
						_colSize = _allTableMap.get(0).size();
					}
					for ( l = 0; l < _colSize; l++) {
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
						for ( l = 0; l < _colSize-1; l++) {
							for ( k = 1; k < _allTableMap.size(); k++) {					
								tableMapProperty = _allTableMap.get(k).get(l);
								if(!tableMapProperty.get("colSpan").getStringValue().equals("1") && _exColIdx.indexOf(l) != -1){
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
							
							if( _allTableMap.size() > 0 )
							{
								_colSize = _allTableMap.get(0).size();
							}
							
							for ( l = 0; l < _colSize; l++)
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
					
					for ( k = 0; k < _allTableMap.size(); k++) {	
						ArrayList<HashMap<String, Value>> _tableRow = _allTableMap.get(k);
						HashMap<String, Value> _tableMap = null;
						_lastCellIdx.add(0);
						
						for ( l = 0; l < _tableRow.size(); l++) {
							_tableMap = _tableRow.get(l);
							
//							if(_tableMap.get("cell") != null && ((Element) _tableMap.get("cell").getElementValue()).getElementsByTagName("property").getLength() > 0 )
							if( _tableMap.get("status").getStringValue().equals("NORMAL")|| _tableMap.get("status").getStringValue().equals("MS")||  _tableMap.get("status").getStringValue().equals("MR") )
							{
								if( _exColIdx.indexOf(l) == -1 && _tableMap.get("cell").getElementValue() != null )
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
					 
					if( _allTableMap.size() > 0 )
					{
						_colSize = _allTableMap.get(0).size();
					}
					 
					for ( l = 0; l < _colSize-1; l++) {
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
				
				for ( k = 0; k < _allTableMap.size(); k++) {
					
					colIndex = 0;
					
//					_tableMapItem = (Element) _tableMaps.item(k);
//					
//					if(_newTalbeFlag)
//					{
//						_tableMapDatas = _tableMapItem.getElementsByTagName("tableMapData");
//					}
//					else
//					{
//						_tableMapDatas = _tableMapItem.getElementsByTagName("cell");
//					}
					
					// column 값 처리 ( UBApproval 일경우 데이터셋의 row수만큼 컬럼을 증가 ) 
					
//					_tableMapDatasLength = _tableMapDatas.getLength();
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
						
						if(_exColIdx.size() > 0 && _includeLayoutType.equals( GlobalVariableData.M_TABLE_INCLUDE_LAYOUT_TYPE_AUTO )){
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
						
						
						if(_newTalbeFlag)
						{
							
							if( _lastCellIdx.size() > 0 )
							{
								int _columnIndex = 0;
								
								if( _allTableMap.get(k).get(l).containsKey("columnIndex") ) _columnIndex = Integer.valueOf(_allTableMap.get(k).get(l).get("columnIndex").getStringValue());

								if( _lastCellIdx.get(k) ==  _columnIndex )
								{
									useRightBorder = true;
								}
							}
							else if( colIndex == _allTableMap.get(k).size() )
							{
								useRightBorder = true;
							}
							if( rowIndex == _tableMaps.getLength() ) useBottomBorder = true;
							
//							if( colIndex == _allTableMap.get(k).size()) useRightBorder = true;
//							if( rowIndex == _tableMaps.getLength() ) useBottomBorder = true;
						}
						else
						{
							if( l+ tableMapProperty.get("colSpan").getIntValue() >= _allTableMap.get(k).size()-1 ) useRightBorder 	= true;
							if( k+ tableMapProperty.get("rowSpan").getIntValue() >= _tableMaps.getLength()-1 ) useBottomBorder 		= true;
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
//						if( _xAr.size() > 0 )
//						{
//							_spanP = Integer.valueOf(tableMapProperty.get("colSpan").getIntegerValue());
//							_indexP = Integer.valueOf(tableMapProperty.get("columnIndex").getIntegerValue());
//							itemProperty.put("x", new Value(_xAr.get(_indexP), "number") );
//							_updateP = (float) Math.floor((_xAr.get(_indexP + _spanP ) - _xAr.get(_indexP)) *10)/10;
//							itemProperty.put("width",   new Value( _updateP, "number") );
//						}
						
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
									
									if( bandInfoData.containsKey(  tableProperty.get("band").getStringValue() ) )
									{
										float _chkBandH = bandInfoData.get( tableProperty.get("band").getStringValue()).getHeight();
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
						// <property name="rowCount" value="3" type="number"/>
				        // <property name="columnCount" value="3" type="number"/>
						
						/**
						if( !GlobalVariableData.mExcelFlag )
						{
							Integer _chkBorderW = 1;
							if( !useRightBorder && borderAr.get(3) != null )
							{
								_chkBorderW = ((ArrayList<Integer>) borderAr.get(3)).get(0);
								itemProperty.put("width", new Value( itemProperty.get("width").getIntegerValue() - _chkBorderW ));
							}
							
							if( !useBottomBorder && borderAr.get(3) != null )
							{
								_chkBorderW = ((ArrayList<Integer>) borderAr.get(3)).get(0);
								itemProperty.put("height", new Value( itemProperty.get("height").getIntegerValue() - _chkBorderW ));
							}
						}*/
						
						
						itemProperty.put("borderSide", 		Value.fromString( borderAr.get(0).toString() ) );
						itemProperty.put("borderTypes", 	Value.fromArrayString( (ArrayList<String>) borderAr.get(1) ));
						itemProperty.put("borderColors",  	Value.fromArrayString( (ArrayList<String>) borderAr.get(2) ));
						itemProperty.put("borderWidths",  	Value.fromArrayInteger( (ArrayList<Integer>) borderAr.get(3) ));
						itemProperty.put("borderColorsInt",  	Value.fromArrayInteger( (ArrayList<Integer>) borderAr.get(4) ));
						
						// Border Original Type담아두기
						if( borderAr.size() > 5 ) itemProperty.put("borderOriginalTypes",  	Value.fromArrayString( (ArrayList<String>) borderAr.get(5) ));
						
						updateX = Float.valueOf(tableProperty.get("band_x").getStringValue()) +_newXValue.get(l);
						updateY = Float.valueOf(tableProperty.get("band_y").getStringValue()) + itemProperty.get("y").getIntegerValue();
						
						itemProperty.put(  "band_x", new Value( updateX, "string"));
						itemProperty.put(  "x", new Value(updateX , "string"));
						//itemProperty.put(  "x", new Value( updateX, "string"));
						itemProperty.put(  "band_y", new Value( updateY, "string"));
						
						float _itemX = updateX;
						float _itemWidth = itemProperty.get("width").getIntegerValue();
						float _itemHeight = itemProperty.get("width").getIntegerValue();
						int _rotate = 0;
						
						if(itemProperty.containsKey("rotation") )
						{
							_rotate = itemProperty.get("rotation").getIntValue();
							
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
						
						if( mXAr.indexOf( Math.round(_itemX) ) == -1 )
						{
							mXAr.add(Math.round(_itemX));
						}
						if( mXAr.indexOf( Math.round(_itemX+ _itemWidth) ) == -1 )
						{
							mXAr.add(Math.round(_itemX + _itemWidth ) );
						}
						
						// 2016-05-02 보더 만큼 빼던 부분 제거
//						if(!useRightBorder)
//						{
//							itemProperty.put("rWidth", Value.fromInteger( itemProperty.get("width").getIntegerValue()  - ((ArrayList<Integer>) borderAr.get(3)).get(0) ) );
//						}
//						
//						if(!useBottomBorder)
//						{
//							itemProperty.put("rHeight", Value.fromInteger(  itemProperty.get("height").getIntegerValue()  - ((ArrayList<Integer>)borderAr.get(3)).get(0) ) );
//						}
						
						if( itemProperty.containsKey("id") == false || itemProperty.get("id").getStringValue().equals("") ) itemProperty.put(  "id", new Value( _childItem.getAttribute("id") + "_" + k + l, "string"));
//						itemProperty.put(  "id", new Value( _cellItem.getAttribute("id") , "string"));
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
							
							if(tableProperty.containsKey("band") && tableProperty.get("band").getStringValue()!="" && bandInfoData.containsKey(tableProperty.get("band").getStringValue()) )
							{
								bandInfoData.get(tableProperty.get("band").getStringValue()).getChildren().add(itemProperty);
								
								int _requiredItemIndex = OriginalRequiredItemList.indexOf(itemProperty.get("id").getStringValue());
								int _tabIndexItemIndex = OriginalTabIndexItemList.indexOf(itemProperty.get("id").getStringValue());
								
								if( _requiredItemIndex != -1 )
								{
									bandInfoData.get(tableProperty.get("band").getStringValue()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").getStringValue() );
								}
								
								if( _tabIndexItemIndex != -1 )
								{
									bandInfoData.get(tableProperty.get("band").getStringValue()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").getStringValue() );
								}
							}
							
						}
						
						if(l == 0 )
						{
//							rowHeightNode = (Node) _xpath.evaluate("./property[@name='rowHeight']", _tableMapDatas.item(l), XPathConstants.NODE);
							//NodeList _rowHeightP = ((Element) _tableMapDatas.item(l)).getElementsByTagName("property");							
//							Element _rowHeightElement;
//							for ( j = 0; j < _rowHeightP.getLength(); j++) {
//								_rowHeightElement = (Element) _rowHeightP.item(j);
//								if( _rowHeightElement.getAttribute("neme").equals("rowHeight") )
//								{
//									_tableArogHeight = Float.valueOf( _rowHeightElement.getAttribute("value") );
//									break;
//								}
//							}
							_tableArogHeight = _allTableMap.get(k).get(l).get("rowHeight").getIntegerValue();
//							_tableArogHeight = Float.valueOf( rowHeightNode.getAttributes().getNamedItem("value").getNodeValue() );  
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
					convertTableMapToApprovalTbl(_ubApprovalAr, bandInfoData,mXAr );
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
				
				float _itemX = itemProperty.get("x").getIntegerValue();
				float _itemWidth = itemProperty.get("width").getIntegerValue();
				float _itemHeight = itemProperty.get("height").getIntegerValue();
				int _rotate = 0;
				
				if(itemProperty.containsKey("rotation") )
				{
					_rotate = itemProperty.get("rotation").getIntValue();
					
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
				
				if( mXAr.indexOf( Math.round(_itemX) ) == -1 )
				{
					mXAr.add(Math.round(_itemX));
				}
				if( mXAr.indexOf( Math.round(_itemX+ _itemWidth) ) == -1 )
				{
					mXAr.add(Math.round(_itemX + _itemWidth ) );
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
				
				int _requiredItemIndex = OriginalRequiredItemList.indexOf(itemProperty.get("id").getStringValue());
				int _tabIndexItemIndex = OriginalTabIndexItemList.indexOf(itemProperty.get("id").getStringValue());
				
				if( _requiredItemIndex != -1 )
				{
					bandInfoData.get(itemProperty.get("band").getStringValue()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").getStringValue() );
				}
				
				if( _tabIndexItemIndex != -1 )
				{
					bandInfoData.get(itemProperty.get("band").getStringValue()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").getStringValue() );
				}
				
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
					
//					bandInfoData.get(itemProperty.get("band").getStringValue()).getChildren().add(itemProperty);
				}
				
			}
			
			
		}// 각각의 아이템들의 데이터셋정보를 밴드리스트에 맵핑하고 생성할 로우별 아이템을 children에 담기완료
		
		
		boolean isAutoTableHeight  =  false;
		
        for( String key : bandInfoData.keySet() ){
        	isAutoTableHeight = false;
        	
			isAutoTableHeight = bandInfoData.get( key ).getAutoTableHeight();
			// autoTableHeight를 설정한 경우 Cell이 아닌경우 자신이 포함된 Cell의 정보를 담아놓는다.
			if(isAutoTableHeight){			
				//cell아닌 경우 table 포함 여부 확인 by IHJ
				Iterator<String> nameItr = (Iterator)bandInfoData.keySet().iterator();       
				ArrayList<HashMap<String, Value>> chilrden;
				HashMap<String, Value> currentItemData;
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
						ArrayList<HashMap<String, Value>> arTableProperties = bandInfoData.get(bandID).getTableProperties();            	
						chilrden = bandInfoData.get(bandID).getChildren();
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
										
										int hh = 0;
										//@TEST 아이템의 가운데 정렬속성이 있을경우 셀의 한가운데로 이동하기 위해 겹치는 셀의 시작인덱스와 ROWSPAN값을 담아둔다.
										boolean _isAutoVerticalPosition = bandInfoData.get(bandID).getAutoVerticalPosition();
										
										if( _isAutoVerticalPosition )
										{
											ArrayList<HashMap<String, Value>> _cellItems = (ArrayList<HashMap<String, Value>>) arTableProperties.get(kk).get("CELLS").getObjectValue();
											
											
											float _cxPos = 0;
											float _cyPos = 0;
											float _cwPos = 0;
											float _chPos = 0;
											
											for( hh = 0; hh < _cellItems.size(); hh++ )
											{
												_cxPos = _cellItems.get(hh).get("x").getIntegerValue();
												_cyPos = _cellItems.get(hh).get("band_y").getIntegerValue();
												_cwPos = _cellItems.get(hh).get("width").getIntegerValue();
												_chPos = _cellItems.get(hh).get("height").getIntegerValue();
												
												if((_cxPos <= xOtherPos && xOtherPos <= (_cxPos + _cwPos))
														&& (_cyPos <= yOtherPos && yOtherPos <= (_cyPos + _chPos)) ){
													currentItemData.put("cellRowIndex", new Value(_cellItems.get(hh).get("rowIndex").getIntValue(),"int"));
													currentItemData.put("tableId", new Value(arTableProperties.get(kk).get("tableId").getStringValue(),"string"));
													currentItemData.put("cellPadding", new Value(0 ,"float"));
													currentItemData.put("cellRowSpan", new Value( _cellItems.get(hh).get("rowSpan").getIntValue()  ,"float"));
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
													currentItemData.put("cellRowIndex", new Value(hh,"int"));
													currentItemData.put("tableId", new Value(arTableProperties.get(kk).get("tableId").getStringValue(),"string"));
													currentItemData.put("cellPadding", new Value(yOtherPos - (sumHeight- arTempTableRowH.get(hh)) ,"float"));
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
	
		// 밴드의 데이터 숫자와 height 값을 이용하여 총 페이지수를 구하기
//		ArrayList<Object> rowHeightListData =  makeRowHeightList( bandList, bandInfoData, defaultY, defaultY, pageHeight, _pageWidth, mXAr, originalDataMap);
		 
		mFunction.setGroupBandCntMap(_groupBandCntMap);

		mFunction.setOriginalDataMap(originalDataMap);
		
		mFunction.setDatasetList(_data);
		
		ArrayList<Object> rowHeightListData;
		if( isExcelOption.equals("BAND") || mFitOnePage ) rowHeightListData =  makeRowHeightListExcel( bandList, bandInfoData, defaultY, defaultY, -1, _pageWidth, mXAr, originalDataMap, pageHeight );
		else rowHeightListData =  makeRowHeightList( bandList, bandInfoData, defaultY, defaultY, pageHeight, _pageWidth, mXAr, originalDataMap);
		
		ArrayList<ArrayList<HashMap<String, Value>>> pagesCountList = (ArrayList<ArrayList<HashMap<String, Value>>>) rowHeightListData.get(0);
		HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) rowHeightListData.get(1);
		
		
		// 밴드별 row 를 이용하여 페이지별 아이템을 생성하여 리턴
		
		
		ArrayList<Object> _retAr = new ArrayList<Object>();
		
		_retAr.add(bandInfoData);		
		_retAr.add(bandList);			
		_retAr.add(pagesCountList);		
		_retAr.add(crossTabData);		// 크로스탭정보가 담긴 배열
		_retAr.add(originalDataMap);		// OrizinalDataSet정보를 담은 map
		_retAr.add(groupDataNamesAr);		// 각 그룹별 그룹핑된 데이터리스트 [ [ string, string ] ] 형태

		
		_retAr.add(rowHeightListData.get(2));		// 필수목록 페이지별 리스트
		_retAr.add(rowHeightListData.get(3));		// 탭인덱스 페이지별 리스트

		
		_retAr.add(_groupBandCntMap);				//  그룹밴드별 총 그룹핑 갯수가 담긴 객체  { 밴드 ID : 그룹수, 밴드 ID2:......  }
		
		_pageXArr = mXAr;
		
		return _retAr;
	}
	
	public ArrayList<Object> loadTotalPageJson( PageInfo _page,HashMap<String, ArrayList<HashMap<String, Object>>> _data, float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr, HashMap<String, Object> _param  ) throws ScriptException
	{
		return loadTotalPageJson(_page,_data, _page.getBandList(), defaultY, pageHeight, pageWidth, mXAr, _param );
	}
	
	public ArrayList<Object> loadTotalPageJson(  PageInfo _page,HashMap<String, ArrayList<HashMap<String, Object>>> _data, ArrayList<BandInfoMapData> _bandList, float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr, HashMap<String, Object> _param  ) throws ScriptException
	{
		DataSet = _data;
		mParam = _param;
		mFunction.setGroupBandCntMap(_page.getGgroupBandCntMap());
		mFunction.setOriginalDataMap(_page.getOriginalDataMap());
		mFunction.setDatasetList(_data);
		mPageHeight = pageHeight;
		mPageWidth = pageWidth;
		
		HashMap<String, BandInfoMapData> _bandInfo = _page.getBandInfoData();
		
		ArrayList<Object> rowHeightListData;
		if( isExcelOption.equals("BAND") || mFitOnePage ) rowHeightListData =  makeRowHeightListExcel( _bandList, _bandInfo, defaultY, defaultY, -1, pageWidth, mXAr, _page.getOriginalDataMap(), pageHeight);
		else rowHeightListData =  makeRowHeightList( _bandList, _bandInfo, defaultY, defaultY, pageHeight, pageWidth, mXAr, _page.getOriginalDataMap());
		
		ArrayList<ArrayList<HashMap<String, Value>>> pagesCountList = (ArrayList<ArrayList<HashMap<String, Value>>>) rowHeightListData.get(0);
		HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) rowHeightListData.get(1);
		
		// 밴드별 row 를 이용하여 페이지별 아이템을 생성하여 리턴
		
		ArrayList<Object> _retAr = new ArrayList<Object>();
		
		_retAr.add(pagesCountList);		
		_retAr.add(crossTabData);					// 크로스탭정보가 담긴 배열
		_retAr.add(rowHeightListData.get(2));		// 필수목록 페이지별 리스트
		_retAr.add(rowHeightListData.get(3));		// 탭인덱스 페이지별 리스트
		
		_pageXArr = mXAr;
		
		return _retAr;
	}
	
	public ArrayList<Object> loadTotalPageSimple( PageInfoSimple _page,HashMap<String, ArrayList<HashMap<String, Object>>> _data, float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr, HashMap<String, Object> _param  ) throws ScriptException
	{
		return loadTotalPageSimple(_page,_data, _page.getBandList(), defaultY, pageHeight, pageWidth, mXAr, _param );
	}
	
	public ArrayList<Object> loadTotalPageSimple(  PageInfoSimple _page,HashMap<String, ArrayList<HashMap<String, Object>>> _data, ArrayList<BandInfoMapDataSimple> _bandList, float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr, HashMap<String, Object> _param  ) throws ScriptException
	{
		/**
		return null;
		 */
		DataSet = _data;
		mParam = _param;
		mFunction.setGroupBandCntMap(_page.getGgroupBandCntMap());
		mFunction.setOriginalDataMap(_page.getOriginalDataMap());
		mFunction.setDatasetList(_data);
		mPageHeight = pageHeight;
		mPageWidth = pageWidth;
		
		HashMap<String, BandInfoMapDataSimple> _bandInfo = _page.getBandInfoData();
		
		ArrayList<Object> rowHeightListData;
		
//		if( isExcelOption.equals("BAND") || mFitOnePage ) rowHeightListData =  makeRowHeightListExcel( _bandList, _bandInfo, defaultY, defaultY, -1, pageWidth, mXAr, _page.getOriginalDataMap(), pageHeight);
//		else rowHeightListData =  makeRowHeightListSimple( _bandList, _bandInfo, defaultY, defaultY, pageHeight, pageWidth, mXAr, _page.getOriginalDataMap());
		//TEST
		rowHeightListData =  makeRowHeightListSimple( _bandList, _bandInfo, defaultY, defaultY, pageHeight, pageWidth, mXAr, _page.getOriginalDataMap());
		
		ArrayList<ArrayList<HashMap<String, Value>>> pagesCountList = (ArrayList<ArrayList<HashMap<String, Value>>>) rowHeightListData.get(0);
		HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) rowHeightListData.get(1);
		
		// 밴드별 row 를 이용하여 페이지별 아이템을 생성하여 리턴
		
		ArrayList<Object> _retAr = new ArrayList<Object>();
		
		_retAr.add(pagesCountList);		
		_retAr.add(crossTabData);					// 크로스탭정보가 담긴 배열
		_retAr.add(rowHeightListData.get(2));		// 필수목록 페이지별 리스트
		_retAr.add(rowHeightListData.get(3));		// 탭인덱스 페이지별 리스트
		
		_pageXArr = mXAr;
		
		return _retAr;
		
	}
	
	
	/**
	 * functinoName	:	makeRowHeightList</br>
	 * desc			:	각 페이지별로 화면에 표시된 밴드와 로우 카운트를 구하여 담기
	 * @param bandList
	 * @param currentY
	 * @param defaultY
	 * @param maxHeight
	 * @throws ScriptException 
	 */
//	private ArrayList<ArrayList<HashMap<String, Value>>> makeRowHeightList( ArrayList<BandInfoMapData> bandList, HashMap<String, BandInfoMapData> bandInfoData, float currentY, float defaultY, float defaultHeight, float pageWidth )
	public ArrayList<Object> makeRowHeightList( ArrayList<BandInfoMapData> bandList, HashMap<String, BandInfoMapData> bandInfoData, float currentY, float defaultY, float defaultHeight, float pageWidth, ArrayList<Integer> mXAr, HashMap<String, String> originalDataMap ) throws ScriptException
	{
		boolean pageHeaderRepeat = false;	// 페이지 헤더 밴드의 repeat여부 체크
		boolean pageFooterRepeat = false;	// 페이지 푸터 밴드의 reepat여부 체크
		float pageHeaderHeight = 0f;		// 페이지 헤더 밴드의 height
		float pageFooterHeight = 0f;		// 페이지 푸터 밴드의 height
		float currnetItemY = 0f;			// 다음 아이템의 Y값을 담는 값
		float currentPageMaxHeight = 0f;	// 현재 페이지의 최대 Height값을 담기
		float repeatPageFooterHeight = 0f;	// 현재 페이지의 최대 Height값을 담기
		float maxHeight = defaultHeight;
		int i = 0;
		int j = 0;
		
		HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = null;
		
		ArrayList<String> pageHeaderList = new ArrayList<String>();
		ArrayList<String> pageFooterList = new ArrayList<String>();
		String bandID = "";
		String bandClassName = "";
		
		ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = new ArrayList<ArrayList<HashMap<String, Value>>>(); 	// 모든 페이지의 페이지별 밴드명과 row정보를 담기
		ArrayList<HashMap<String, Value>> pageRowList = new ArrayList<HashMap<String, Value>>();							// 한페이지별로 밴드명과 밴드 cnt값을 담아두기
		
		
		ArrayList<ArrayList<HashMap<String, Object>>> _requiredItemListPages = new ArrayList<ArrayList<HashMap<String, Object>>>();	// 페이지별로 필수 요소값을 가진 아이템의 ID를 담기
		ArrayList<ArrayList<HashMap<String, Object>>> _tabIndexItemListPages = new ArrayList<ArrayList<HashMap<String, Object>>>();	// 페이지별로 탭 Index를 가진 아이템의 순서대로 담기
		ArrayList<HashMap<String, Object>> _requiredItemList = new ArrayList<HashMap<String, Object>>();							// 한페이지의 필수 요소값을 가진 아이템의 ID를 담은 배열
		ArrayList<HashMap<String, Object>> _tabIndexItemList = new ArrayList<HashMap<String, Object>>();							// 한페이지의 TabIndex값을 가진 배열
		
		currentPageMaxHeight = defaultHeight;
		
		for ( i = 0; i < bandList.size(); i++ ) {
			
			bandClassName = bandList.get(i).getClassName();
			bandID = bandList.get(i).getId();
			
			if( bandInfoData.containsKey(bandID) == false ) continue;		// 밴드 정보에 해당 ID가 없을경우 continue 
			
			// band별 ubfx처리 ( visible값이 false일경우 continue시키고 밴드의 visible 속성을 false로 지정 )
			if( bandList.get(i).getVisible() != false && bandList.get(i).getUbFunction() != null && bandList.get(i).getUbFunction().size() > 0 )
			{
				boolean _isBandVisible = true;
				
				int _ubfxSize = bandList.get(i).getUbFunction().size();
				
				for (int k = 0; k < _ubfxSize; k++) {
					HashMap<String, String> _fxMap = bandList.get(i).getUbFunction().get(k);
					
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_fxMap.get("value") , 0,0,0, -1, -1, bandList.get(i).getDataSet());
					}else{
						_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(i).getDataSet() );
					}
					
					
					_fnValue = _fnValue.trim();
					
					if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
					{
						bandList.get(i).setVisible(false);
						
					    _isBandVisible = false;
						  
						break;
					}
				}
				
				if( !_isBandVisible ){ continue; }
			}
			else if( !bandList.get(i).getVisible() )
			{
				continue;
			}
			
			
			if( bandClassName.equals( BandInfoMapData.PAGE_HEADER_BAND ) )
			{
				
				pageHeaderList.add(bandID);			// 페이지 헤더의 repeat값이 Y일경우 repeatFlag값을 true로 지정하고 headerHeight값을 담기
				if( bandInfoData.get(bandID).getRepeat().equals("y") )
				{
					pageHeaderRepeat = true;
					pageHeaderHeight = pageHeaderHeight + bandInfoData.get(bandID).getHeight();
				}
				
				currnetItemY = currnetItemY + bandInfoData.get(bandID).getHeight();
			}
			else if( bandClassName.equals( BandInfoMapData.PAGE_FOOTER_BAND ) )
			{
				
				pageFooterHeight = pageFooterHeight + bandInfoData.get(bandID).getHeight();
				
//				if( !GlobalVariableData.GROUP_FOOTER_POSITION_LAST ) currentPageMaxHeight = maxHeight - pageFooterHeight;
				if( !GlobalVariableData.GROUP_FOOTER_POSITION_LAST ) currentPageMaxHeight = maxHeight - bandInfoData.get(bandID).getHeight();
				
				pageFooterList.add(bandID);
				if( bandInfoData.get(bandID).getRepeat().equals("y") )
				{
					pageFooterRepeat = true;
					repeatPageFooterHeight = repeatPageFooterHeight + bandInfoData.get(bandID).getHeight();
					
//					if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST ) currentPageMaxHeight = maxHeight - pageFooterHeight;
					if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST ) currentPageMaxHeight = maxHeight - bandInfoData.get(bandID).getHeight();
					maxHeight = currentPageMaxHeight;
					
					
				}
				
			}
			
		}
		// 페이지 헤더 와 푸터의 height값을 담아두기
		
		int _startIndex = 0;
		int _maxIndex = 0;
		int _dataCnt = 0;
		
		float _dataHeaderHeight = 0f;
		float _dataPageFooterHeight = 0f;
		String _checkNextBandClass = "";
		float _groupingFootHeight = 0;
		float _groupingNextPageH = 0;
		Boolean grpAutoHeight = false;
		Boolean firstBand = true;
		Boolean adjustableFlag = false;
		Boolean isAutoTableHeight = false;
		int _groupMaxRowCnt = 0;
		
		ArrayList<Float> _rowHeightAr = new ArrayList<Float>();		// adjustableHeight 각 Row별 Height값을 담아두는 배열
		HashMap<String, Value> bandPageInfo = new HashMap<String,Value>(); 
		float _chkRowHeight = 0;
		mFunction.setDatasetList(DataSet);
		mFunction.setOriginalDataMap(originalDataMap);
		
		// 밴드별 adjustableHeight값 업데이트
		float _chkAutherHeight = 0;
		for ( i = 0; i < bandList.size(); i++ ) {
			
			_chkAutherHeight = 0;
			
			if(bandList.get(i).getAutoTableHeight()) bandList.get(i).setResizeText(false);	
			
			if((bandList.get(i).getAutoTableHeight() || bandList.get(i).getAdjustableHeight()) && bandList.get(i).getResizeText() == false )
			{
				_rowHeightAr = new ArrayList<Float>();
				if(bandList.get(i).getClassName().equals(BandInfoMapData.DATA_BAND))
				{
					if( bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.containsKey( bandList.get(i).getHeaderBand() )  )
					{
						_chkAutherHeight = _chkAutherHeight +  bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
					}
					if(bandList.get(i).getSummery().equals("") == false && bandInfoData.containsKey( bandList.get(i).getSummery() ) )
					{
						_chkAutherHeight = _chkAutherHeight +  bandInfoData.get( bandList.get(i).getSummery() ).getHeight();
					}
					
				}
				else
				{
					_chkAutherHeight = 0;
				}
				
				try {
					_dataCnt = DataSet.get(bandList.get(i).getDataSet() ).size();
				} catch (Exception e) {
					// TODO: handle exception
//					System.out.print(" ContinueBandParser 1028 line ");
				}
				
				if( bandList.get(i).getClassName().equals(BandInfoMapData.DATA_BAND) == false )
				{
					_dataCnt = 1;
				}
				ArrayList<HashMap<String , ArrayList<Float>>> _tableRowHeight = new ArrayList<HashMap<String , ArrayList<Float>>>();
				for ( j = 0; j <_dataCnt; j++) {
					
					if(bandList.get(i).getAutoTableHeight()){						
						
						HashMap<String , ArrayList<Float>> hmTableRowHeight = getRowAdjustableHeightArray(bandList.get(i), j, maxHeight - _chkAutherHeight-10, maxHeight - _chkAutherHeight-10, bandInfoData);
						
						ArrayList<Float> arTableRowHeight;
						
						Iterator<String> keys = hmTableRowHeight.keySet().iterator();
						float tempBandHeight = 0;
						while( keys.hasNext() ){		
							_chkRowHeight = 0;
							String key = keys.next();		
							arTableRowHeight = hmTableRowHeight.get(key);
							for(int v = 0; v<arTableRowHeight.size();v++){
								_chkRowHeight = _chkRowHeight + arTableRowHeight.get(v);
							}
							
							if(bandList.get(i).getGroupBand() == null || bandList.get(i).getGroupBand().equals("")){
								_chkRowHeight = _chkRowHeight + bandInfoData.get(bandList.get(i).getId().toString()).getTableBandY().get(key);
							}else{
								_chkRowHeight = _chkRowHeight + bandInfoData.get(bandList.get(i).getDefaultBand().toString()).getTableBandY().get(key);
							}
							
							//table이 여러개 인경우 height 값이 큰 table을 기준으로 band Height 를 설정한다.(위아래로 디자인 된 테이블은 고려하지 않는다.) 
							if(tempBandHeight>_chkRowHeight){
								_chkRowHeight = tempBandHeight;
							}
							tempBandHeight = _chkRowHeight;
						}					
						
						//_chkRowHeight = getRowAdjustableHeight(bandList.get(i), j, maxHeight - _chkAutherHeight-10, maxHeight - _chkAutherHeight-10, bandInfoData);
						
						if( bandList.get(i).getHeight() > _chkRowHeight )
						{
							_chkRowHeight = bandList.get(i).getHeight();
						}
						
						_rowHeightAr.add(_chkRowHeight);	// band의 Row별 Height값을 담기
						_tableRowHeight.add(hmTableRowHeight);
					}else {
						_chkRowHeight = getRowAdjustableHeight(bandList.get(i), j, maxHeight - _chkAutherHeight-10, maxHeight - _chkAutherHeight-10, bandInfoData);
						
						if( bandList.get(i).getHeight() > _chkRowHeight )
						{
							_chkRowHeight = bandList.get(i).getHeight();
						}
						
						_rowHeightAr.add(_chkRowHeight);	// band의 Row별 Height값을 담기
					}
					                                        
				}
				
				bandList.get(i).setAdjustableHeightListAr(_rowHeightAr);
				if(bandList.get(i).getAutoTableHeight()){
					bandList.get(i).setTableRowHeight(_tableRowHeight);				
				}
			}
		}
		
		boolean _isNextNewPage = false;
		String _newPageGroupName = "";
		int _maxBandSize = bandList.size();
		
		// groupCurrentIndex를 위해 시작 IDX를 담아둔다
		int _groupStartIdx = 0;
		int _groupTotalCnt = 0;
		String _currentGroupDSName = "";
		ArrayList<String> _groupListAr = new ArrayList<String>();
		// groupTotalPageCnt 를 위해 시작 page에서 마지막 page수를 이용하여 총 페이지수를 구하기
		
		HashMap<Integer, Integer> _grpStartPageMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> _grpTotalPageMap = new HashMap<Integer, Integer>();
		
		for ( i = 0; i < _maxBandSize; i++ ) {
			
			bandClassName = bandList.get(i).getClassName();
			bandID = bandList.get(i).getId();
			grpAutoHeight = false;
			adjustableFlag = false;
			_groupMaxRowCnt = 0;
			_rowHeightAr = new ArrayList<Float>();
			
			// band별 ubfx처리 ( visible값이 false일경우 continue시키고 밴드의 visible 속성을 false로 지정 )
			if( bandList.get(i).getVisible() != false && bandList.get(i).getUbFunction() != null && bandList.get(i).getUbFunction().size() > 0 )
			{
				boolean _isBandVisible = true;
				
				int _ubfxSize = bandList.get(i).getUbFunction().size();
				
				for (int k = 0; k < _ubfxSize; k++) {
					HashMap<String, String> _fxMap = bandList.get(i).getUbFunction().get(k);
					
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_fxMap.get("value") , 0,0,0, -1, -1, bandList.get(i).getDataSet());
					}else{
						_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(i).getDataSet() );
					}
					
					
					_fnValue = _fnValue.trim();
					
					if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
					{
						bandList.get(i).setVisible(false);
						
					    _isBandVisible = false;
						  
						break;
					}
				}
				
				if( !_isBandVisible ){ continue; }
			}
			else if( bandList.get(i).getVisible() == false )
			{
				continue;
			}
			
			// 페이지 헤더와 푸터는 모든 페이지정보를 만든후 add처리 ( repeat값에 따라 add위치를 지정해야함 )  
			if( bandClassName.equals( BandInfoMapData.PAGE_HEADER_BAND ) || 
					bandClassName.equals( BandInfoMapData.DATA_PAGE_FOOTER_BAND ) )
			{
				continue;
			}
			else if( bandClassName.equals( BandInfoMapData.PAGE_FOOTER_BAND )  )
			{
				// Page Footer Band가 현재 영역에 포함이 될수 없을경우 
				if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST && bandList.get(i).getRepeat().equals("n") && currnetItemY + pageFooterHeight > maxHeight )
				{
					pagesRowList.add(pageRowList);
					pageRowList = new ArrayList< HashMap<String,Value>>();
					pagesRowList.add(pageRowList);
					currnetItemY = defaultY + pageHeaderHeight;
				}
				continue;
			}
			else
			{
				int _groupPageNewPage = 0;
				// GroupFooterBand이고 newPage속성이 true일경우 다음 아이템의  CurrentItemY값을 다음페이지로 보낸다.
				// 다음 밴드의 GruopBand가 같아야만 newPage속성을 사용
				// 다음 밴드가 GroupFooterBand가 아닐경우에만 newPage처리 필요. 
				if( bandList.get(i).getClassName().equals("UBGroupFooterBand") && bandList.get(i).getNewPage() )
				{
					_newPageGroupName = bandList.get(i).getGroupHeader();
					_isNextNewPage = true;
				}
				else if(  !bandList.get(i).getClassName().equals("UBGroupFooterBand") && _isNextNewPage )
				{
					if( bandList.get(i).getGroupBand().equals(_newPageGroupName))
					{
						pagesRowList.add(pageRowList);
						pageRowList = new ArrayList< HashMap<String,Value>>();
						currnetItemY = defaultY + pageHeaderHeight;
						
					}
					_isNextNewPage = false;
					_newPageGroupName = "";
				}
				/**
				// 현재 Band가 groupBand인지 여부를 체크 그룹밴드일경우 이전 GroupBand와 다를경우 DataSet을 담아두고 기존에 담겨있던 밴드들의 GroupBandIdx와 GroupTotalPageCnt를 담아둔다. 
				if( bandList.get(i).getGroupBand().equals("") == false)
				{
					if(_currentGroupDSName.equals( bandList.get(i).getDataSet() ) == false )
					{
						int _TotSize = (pagesRowList.size()+1 ) - _groupStartIdx;
						int _grpMaxSize = _groupListAr.size();
						for (int k = 0; k < _grpMaxSize; k++) {
							bandInfoData.get(_groupListAr.get(k)).setGroupStartPageIdx( _groupStartIdx );
							bandInfoData.get(_groupListAr.get(k)).setGroupTotalPageCnt( _TotSize);
						}
						
						if(_groupListAr.size() > 0)
						{
							for (int k = _groupStartIdx; k < _TotSize+_groupStartIdx; k++) {
								_grpStartPageMap.put(k, k - _groupStartIdx );
								_grpTotalPageMap.put(k, _TotSize );
							}
						}
						_currentGroupDSName = bandList.get(i).getDataSet();
						_groupStartIdx = pagesRowList.size();
						
						_groupListAr.clear();
					}

					_groupListAr.add( bandID );
					
				}
				else if( _currentGroupDSName.equals("") == false )
				{
					int _TotSize = (pagesRowList.size()+1 + _groupPageNewPage ) - _groupStartIdx;

					int _grpMaxSize = _groupListAr.size();
					for (int k = 0; k < _grpMaxSize; k++) {
						bandInfoData.get(_groupListAr.get(k)).setGroupStartPageIdx( _groupStartIdx );
						bandInfoData.get(_groupListAr.get(k)).setGroupTotalPageCnt(_TotSize );
					}
					
					if(_groupListAr.size() > 0)
					{
						for (int k = _groupStartIdx; k < _TotSize+_groupStartIdx; k++) {
							_grpStartPageMap.put(k, k - _groupStartIdx );
							_grpTotalPageMap.put(k, _TotSize );
						}
					}
					_groupListAr.clear();
					_currentGroupDSName = "";
				}*/
				
				// 데이터 밴드일경우 Height값을 이용하여 페이지별로 화면에 표시할 아이템을 담아두기
				if( bandClassName.equals( BandInfoMapData.DATA_BAND ) && DataSet.containsKey(bandList.get(i).getDataSet()) )
				{
					try {
						_dataCnt = DataSet.get(bandList.get(i).getDataSet() ).size();
						
						if(bandList.get(i).getUseLabelBand())
						{
							_dataCnt = (int) Math.ceil( (float) _dataCnt / bandList.get(i).getLabelBandColCount() ); 
						}
						
						// 데이터 밴드의 Min Row 지정
						if(  bandList.get(i).getAutoHeight() == false && bandList.get(i).getMinRowCount() > 0 && bandList.get(i).getMinRowCount() > _dataCnt )
						{
							_dataCnt = bandList.get(i).getMinRowCount();
						}
						
					} catch (Exception e) {
						// TODO: handle exception
//						System.out.print(" ContinueBandParser 1603 line ");
					}
					
					if(_dataCnt < 1 ) continue;
					
					_startIndex = 0;
					_dataHeaderHeight = 0;
					_dataPageFooterHeight = 0;
					_groupingFootHeight = 0;
					
					if(bandList.get(i).getSummery().equals(""))
					{
						_dataPageFooterHeight = 0;
					}
					else
					{
						_dataPageFooterHeight = bandInfoData.get(bandList.get(i).getSummery()).getHeight();
					}
					
					if( currentPageMaxHeight - currnetItemY < bandList.get(i).getHeight()+_dataPageFooterHeight )
					{
						pagesRowList.add(pageRowList);
						pageRowList = new ArrayList< HashMap<String,Value>>();
						currnetItemY = defaultY + pageHeaderHeight;
					}
					
					// adjustableHeight값이 true일경우 각 Row별 Height값을 구하여 담기.
					//bandList.get(i).getChildren()
					// 현재 currentItemY값과 defautlPageHeight값을 구하여 처리
					if( bandList.get(i).getAdjustableHeight() && bandList.get(i).getResizeText() )
					{
						// adjustHeight값이 true이고 resizeText값이 true일경우 Row별 Height값을 담고, 현재 페이지를 벗어난 text의 경우 분할된 데이터를 원본 데이터에 추가시켜 처리
						// 추후 구현 해야함. 현재 각 Row별 Height값만 받는 형태로 되어잇음, 추후 분할된 데이터의 컬럼을받고 분할된 데이터의 총 페이지를 넘겨받아서 처리 해야함.
						
						//	adjustableHeight & resize 
						//  원본 데이터셋을 clone 처리 하고 originalDataMap에  원본 데이터셋명과 쿼리를 같이 지정
						ArrayList<HashMap<String, Object>>	_secondDs = (ArrayList<HashMap<String, Object>>) DataSet.get( bandList.get(i).getDataSet() ).clone();
						
						String _originalDSName = bandList.get(i).getDataSet();
						String _newDS_ID = "N_" + i + bandList.get(i).getId()+ "_" + _originalDSName;
						DataSet.put( _newDS_ID , _secondDs);
						bandList.get(i).setDataSet(_newDS_ID);
						
						// GroupBand일경우 originalDataMap에 데이터셋명과 원본 데이터셋명을 담기
//						String _originalDSName = bandList.get(i).getDataSet();
						
						if( bandList.get(i).getGroupBand() != null && bandList.get(i).getGroupBand().equals("") == false )
						{
							_originalDSName = bandInfoData.get( bandList.get(i).getDefaultBand() ).getDataSet();
						}
						
						originalDataMap.put( _newDS_ID, _originalDSName );
						
						float _chkHeaderHeight = 0;
						if( bandList.get(i).getHeaderBand().equals("") == false  )
						{
							_chkHeaderHeight =  bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
						}
						_chkRowHeight = 0;
						adjustableFlag = true;
						
						float _adjustCurrentPageMaxHeight = currentPageMaxHeight - currnetItemY;
						
						// bandList.get(i).getAdjustableHeight() 값이 있을경우 데이터셋을 새로 생성하고 새로 생성한 데이터셋을 Band의 DataSet로 지정하여 진행
						
						int _addDataCnt = 0;
						for ( j = 0; j < _dataCnt; j++) {
							
//							_chkRowHeight = getRowAdjustableHeight(bandList.get(i), j, currentPageMaxHeight - _dataPageFooterHeight, maxHeight - _chkHeaderHeight - _dataPageFooterHeight, bandInfoData);
							if(_adjustCurrentPageMaxHeight < bandList.get(i).getHeight() )
							{
								_adjustCurrentPageMaxHeight = maxHeight - _chkHeaderHeight - _dataPageFooterHeight - pageHeaderHeight;
							}
							// resizeText처리시 
							ArrayList<Object> adjustListAr = getRowAdjustableHeightResizeText(bandList.get(i), j + _addDataCnt, _adjustCurrentPageMaxHeight - _dataPageFooterHeight, maxHeight - pageHeaderHeight - _chkHeaderHeight - _dataPageFooterHeight, bandInfoData);
							
							ArrayList<Float> chkMaxHeightAr = new ArrayList<Float>();
							ArrayList<Object> chkData;
							
							String _chkID = "";
							String _chkDATA = "";
//							ArrayList<String> _resultTextAr;
							ArrayList<Object> _resultTextAr;
							ArrayList<Float> _resultHeightAr; 
							
							ArrayList<HashMap<String, Object>> _resultDataSet = new ArrayList<HashMap<String,Object>>(); 
							
							for (int k = 0; k < adjustListAr.size(); k++) {
								
								chkData = (ArrayList<Object>) adjustListAr.get(k);
								
								_chkID = (String) chkData.get(0);
//								_resultTextAr = (ArrayList<String>) chkData.get(1);
								_resultTextAr = (ArrayList<Object>) chkData.get(1);
								_resultHeightAr = (ArrayList<Float>) chkData.get(2);
								
								if(_resultHeightAr.size() > chkMaxHeightAr.size() )
								{
									chkMaxHeightAr = _resultHeightAr;
								}
								else if( _resultHeightAr.size() > 0 && (_resultHeightAr.size() == chkMaxHeightAr.size()) )
								{
									if(  _resultHeightAr.get(_resultHeightAr.size() -1) > chkMaxHeightAr.get(chkMaxHeightAr.size() -1) )
									{
										chkMaxHeightAr = _resultHeightAr;
									}
								}
								
								HashMap<String, Object> addMap = new HashMap<String, Object>();
								addMap.put("ID", _chkID);
								addMap.put("DATA", _resultTextAr);
								_resultDataSet.add( addMap );
							}
								
							for (int k = 0; k < chkMaxHeightAr.size(); k++) {
								
								if( chkMaxHeightAr.size() == 1 && chkMaxHeightAr.get(k) < bandList.get(i).getHeight() )
								{
									_rowHeightAr.add(bandList.get(i).getHeight());	// band의 Row별 Height값을 담기 
								}
								else
								{
									if( chkMaxHeightAr.get(k) < bandList.get(i).getHeight() )
									{
										_rowHeightAr.add(bandList.get(i).getHeight());	// band의 Row별 Height값을 담기 
									}
									else
									{
										_rowHeightAr.add(chkMaxHeightAr.get(k));	// band의 Row별 Height값을 담기
									}
								}
							}
							ArrayList<HashMap<String, Object>> addArray = new ArrayList<HashMap<String,Object>>();
							HashMap<String, Object> cloneData = null;
							
							for (int k = 0; k < _resultDataSet.size(); k++) {
								
								_chkID = (String) _resultDataSet.get(k).get("ID");
								ArrayList<String> _chkArray = (ArrayList<String>) _resultDataSet.get(k).get("DATA");
								
//								for (int k2 = 0; k2 < _chkArray.size(); k2++) {
								for (int k2 = 0; k2 < chkMaxHeightAr.size(); k2++) {
									
									if(addArray.size() <= k2)
									{
										if( DataSet.get(bandList.get(i).getDataSet() ).size() > j + _addDataCnt )
										{
											cloneData = (HashMap<String, Object>) DataSet.get(bandList.get(i).getDataSet() ).get(j + _addDataCnt).clone();
										}
										else
										{
											cloneData = new HashMap<String, Object>();
										}
										addArray.add(cloneData);
									}
									
									if(_chkArray.size() > k2)
									{
										addArray.get(k2).put(_chkID,  _chkArray.get(k2));
									}
									else
									{
										addArray.get(k2).put(_chkID,  "");
									}
									
								}
								
							}
							
							for (int k = 0; k < addArray.size(); k++) {
								if(k == 0 && DataSet.get(bandList.get(i).getDataSet() ).size() > 0 )
								{
									DataSet.get(bandList.get(i).getDataSet() ).set( j + _addDataCnt + k, addArray.get(k) );
								}
								else
								{
									DataSet.get(bandList.get(i).getDataSet() ).add(j + _addDataCnt + k, addArray.get(k) );
									
									// ResizeFont정보를 담아둔 객체를 추가된 Row만큼 Add
									if( bandList.get(i).getResizeFontData().size() > j + _addDataCnt )
									{
										bandList.get(i).getResizeFontData().add(j + _addDataCnt + k, (HashMap<String, Float>) bandList.get(i).getResizeFontData().get(j + _addDataCnt).clone());
									}
								}
							}
							
							_addDataCnt = _addDataCnt + chkMaxHeightAr.size()-1;
							if(chkMaxHeightAr.size() > 1)
							{
								_adjustCurrentPageMaxHeight = maxHeight - pageHeaderHeight - _chkHeaderHeight - _dataPageFooterHeight - _rowHeightAr.get(_rowHeightAr.size()-1);
							}
							else
							{
								if(_adjustCurrentPageMaxHeight < _rowHeightAr.get(_rowHeightAr.size()-1) )
								{
									_adjustCurrentPageMaxHeight = maxHeight - pageHeaderHeight - _chkHeaderHeight - _dataPageFooterHeight - _rowHeightAr.get(_rowHeightAr.size()-1);
								}
								else
								{
									_adjustCurrentPageMaxHeight = _adjustCurrentPageMaxHeight - _rowHeightAr.get(_rowHeightAr.size()-1);
								}
							}
							
						}
						
						_dataCnt = DataSet.get(bandList.get(i).getDataSet() ).size();
						bandList.get(i).setAdjustableHeightListAr(_rowHeightAr);
					}
					else if( bandList.get(i).getAdjustableHeight() )
					{
						adjustableFlag = true;
					}
					
					// autoTableHeight 처리 
					if( bandList.get(i).getAutoTableHeight() ) adjustableFlag = true;
					
					// Height값이 현재 페이지에 표시 할수 없을경우 resizeText속성이 잇을경우 분할하여 처리 아닐경우 다음 페이지에 위치( Height값이 defaultHeight값보다 
					// 클경우 defaultHeight값으로 변경 )
					
					// autoHeight처리시 pageHeight값이 -1일경우에는 기능을 사용하지 않는다 ( Excel의 밴드 내보내기 형태이기 때문에 AutoHeight속성이 의미가 없어짐 )
					// autoHeight값 처리( groupBand일경우에는 groupFooter를 만나는 부분까지의 모든 Height값을 구해서 마지막 페이지의 인덱스를 구하기 )
//					if( bandList.get(i).getAutoHeight() && ( bandList.get(i).getGroupBand().equals("") ||
//							(  bandList.get(i).getGroupBand().equals("") == false && bandInfoData.get(bandList.get(i).getGroupBand()).getNewPage() ) )  )
					if( bandList.get(i).getAutoHeight() && ( bandList.get(i).getGroupBand().equals("") || bandList.get(i).getNewPage() ) )
					{
						if( bandList.get(i).getAutoHeight() &&  bandList.get(i).getGroupBand().equals("") )
						{
							grpAutoHeight = true;
							
							for ( j = (i+1); j < bandList.size(); j++) {
								_checkNextBandClass = bandList.get(j).getClassName();
								
								// 다음 밴드가 GroupHeaderBand일 경우에도 포함
								if(  _checkNextBandClass.equals("UBGroupHeaderBand") || _checkNextBandClass.equals("UBDataHeaderBand") ||  _checkNextBandClass.equals("UBDataBand")  || _checkNextBandClass.equals("UBEmptyBand") )
								{
									grpAutoHeight = true;
									break;
								}
								else if( _checkNextBandClass.equals("UBDataPageFooterBand") == false &&  _checkNextBandClass.equals("UBPageFooterBand") == false)
								{
									boolean _isBandVisible = true;
									
									if( bandList.get(j).getUbFunction() != null && bandList.get(j).getUbFunction().size() > 0 )
									{
										int _ubfxSize = bandList.get(j).getUbFunction().size();
										
										for (int k = 0; k < _ubfxSize; k++) {
											HashMap<String, String> _fxMap = bandList.get(j).getUbFunction().get(k);
											
											
											String _fnValue;
											
											if( mFunction.getFunctionVersion().equals("2.0") ){
												_fnValue = mFunction.testFN(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(j).getDataSet());
											}else{
												_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(j).getDataSet() );
											}
											
											
											_fnValue = _fnValue.trim();
											
											if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
											{
												bandList.get(j).setVisible(false);
											    _isBandVisible = false;
												break;
											}
										}
									}
									
									if( _isBandVisible ) _groupingFootHeight = _groupingFootHeight + bandList.get(j).getHeight();
								}
								else if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST && _checkNextBandClass.equals("UBPageFooterBand") && bandList.get(j).getRepeat().equals("n") )
								{
									 _groupingFootHeight = _groupingFootHeight + bandList.get(j).getHeight();
								}
							}
							
						}
						else
						{
							grpAutoHeight = true;
							// 그룹밴드의 autoHeight값이 true이고 newPage값이 true일경우 데이터 푸터 밴드를 찾아서 Height값을 처리한다
							for ( j = (i+1); j < bandList.size(); j++) {
								_checkNextBandClass = bandList.get(j).getClassName();
								if( _checkNextBandClass.equals("UBGroupHeaderBand") )
								{
									grpAutoHeight = true;
									break;
								}
								else if( _checkNextBandClass.equals("UBDataHeaderBand") )
								{
									grpAutoHeight = true;
									break;
								}
								else if(  _checkNextBandClass.equals("UBDataPageFooterBand") == false &&  _checkNextBandClass.equals("UBPageFooterBand") == false )
								{
									
									boolean _isBandVisible = true;
									
									if( bandList.get(j).getUbFunction() != null && bandList.get(j).getUbFunction().size() > 0 )
									{
										int _ubfxSize = bandList.get(j).getUbFunction().size();
										
										for (int k = 0; k < _ubfxSize; k++) {
											HashMap<String, String> _fxMap = bandList.get(j).getUbFunction().get(k);
											
											
											String _fnValue;
											
											if( mFunction.getFunctionVersion().equals("2.0") ){
												_fnValue = mFunction.testFN(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(j).getDataSet());
											}else{
												_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(j).getDataSet() );
											}
											
											
											_fnValue = _fnValue.trim();
											
											if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
											{
												bandList.get(j).setVisible(false);
											    _isBandVisible = false;
												break;
											}
										}
									}
									
									if(_isBandVisible)
									{
										if( _checkNextBandClass.equals("UBDataBand") || _checkNextBandClass.equals("UBEmptyBand") )
										{
											grpAutoHeight = false;
										}
										_groupingFootHeight = _groupingFootHeight + bandList.get(j).getHeight();
									}
								}
							}
						}
					}
					
					
					int _lastCnt = 0;
					float _dataBandHeight = 0;
					int _grpPageCnt = 0;
					boolean chkAdjustablePageFlag = false;
					boolean _isLastPage = false;
					boolean _isFistNoShow = false;
					
					ArrayList<HashMap<String, Value>> chkBandList = new ArrayList<HashMap<String,Value>>();
					
					boolean _useBandFlag = true;
					
					if( bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName().equals("")==false )
					{
						for (HashMap<String, Value> _bandlist : pageRowList) {
							if( _bandlist.containsKey("groupName") && _bandlist.get("groupName").getStringValue().equals( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName() ) )
							{
								_useBandFlag = false;
								break;
							}
						}
					}
					else
					{
						_useBandFlag = false;
					}
					
					
					// header밴드와 data밴드의 합이 페이지의 표현 영역보다 클경우 헤더밴드를 제거한다.
					if( bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight()+ bandList.get(i).getHeight() > (currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight))  )
					{
						bandList.get(i).setHeaderBand("");
					}
					
					while( _dataCnt > 0 ){
						
						chkAdjustablePageFlag = false;
						_isFistNoShow = false;
						float _HeaderHeight = 0;
						
						if( (_useBandFlag || _startIndex > 0) && bandList.get(i).getHeaderBand().equals("") == false )
						{
							bandPageInfo = new HashMap<String, Value>();
							bandPageInfo.put("startIndex", new Value( 0,"int"));
							bandPageInfo.put("lastIndex",  new Value( 1,"int"));
							bandPageInfo.put("y", new Value( currnetItemY,"number"));
							bandPageInfo.put("id", new Value( bandList.get(i).getHeaderBand(),"string"));
							bandPageInfo.put("gprCurrentPageNum", new Value( _grpPageCnt,"number"));
							
							if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName().equals("") == false )
							{
								bandPageInfo.put("groupName", new Value( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName(),"string"));
							}
							
							pageRowList.add(bandPageInfo);
							_dataHeaderHeight = bandList.get(i).getHeight();
							
							if( ( bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeight() || bandInfoData.get( bandList.get(i).getHeaderBand() ).getAutoTableHeight()) && bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().size() > 0 )
							{
								currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
							}
							else
							{
								currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
							}
							
							// tabIndex 담기
							if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getRequiredItems().size() > 0 )
							{
								_requiredItemList = getBandRequiredItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue() ,bandInfoData.get( bandList.get(i).getHeaderBand() ), _requiredItemList );
							}
							// 필수값 담기
							if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getTabIndexItem().size() > 0 )
							{
								_tabIndexItemList = getBandTabIndexItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue(),bandInfoData.get( bandList.get(i).getHeaderBand() ), _tabIndexItemList );
							}
							
							chkBandList.add(bandPageInfo);
						}
						
						_dataBandHeight = 0;
						if( adjustableFlag  )
						{
							
							_rowHeightAr = bandList.get(i).getAdjustableHeightListAr();
							
							if( bandList.get(i).getClassName().equals(BandInfoMapData.DATA_BAND) && bandInfoData.get( bandList.get(i).getHeaderBand() ) != null )
							{
								if( ( bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeight() || bandInfoData.get( bandList.get(i).getHeaderBand() ).getAutoTableHeight()) && bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().size() > 0 )
								{
									_HeaderHeight = bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
								}
								else
								{
									_HeaderHeight = bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
								}
							}else
							{
								_HeaderHeight = 0;
							}
							
							if( defaultY + pageHeaderHeight + _HeaderHeight < currnetItemY &&  _lastCnt == 0 && ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY ) < _rowHeightAr.get(_lastCnt) && _dataCnt > 0 )
							{
								_isFistNoShow = true;
							}
							
							
							_maxIndex = 0;
							for ( j = _lastCnt; j < _rowHeightAr.size(); j++) {
								
								if( !( defaultY + pageHeaderHeight + _HeaderHeight == currnetItemY && _dataBandHeight == 0 ) && ( (currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY ) - (_dataBandHeight + _rowHeightAr.get(j)) < 0 )
								{
									break;
								}
								else
								{
									_maxIndex++;
									_lastCnt++;
									_dataBandHeight = _dataBandHeight + _rowHeightAr.get(j);
								}
							}
							
							if( _rowHeightAr.size() < _startIndex + _dataCnt && _lastCnt == _rowHeightAr.size() )
							{
								float _chkP = ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY ) - _dataBandHeight;
								if( _chkP/bandList.get(i).getHeight() > 1 )
								{
									_maxIndex = _maxIndex + (int) Math.floor( _chkP/bandList.get(i).getHeight() );
								}
								
								if( _maxIndex > _dataCnt)
								{
									_maxIndex = _dataCnt;
								}
								
								chkAdjustablePageFlag = true;
							}
							
						}
						else
						{
							_dataBandHeight = _dataCnt*bandList.get(i).getHeight();
							_maxIndex = (int) Math.floor( ( (currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY)/bandList.get(i).getHeight() ); 
						}
						boolean chkNextPageFlag = false;
						
						
						if(  _dataCnt - _maxIndex <= 0 && ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY) - _dataBandHeight > 0 && _groupingFootHeight > ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY) - _dataBandHeight )
						{
							chkNextPageFlag = true;
						}
						
						if( _maxIndex == _dataCnt && adjustableFlag && grpAutoHeight )
						{
							chkAdjustablePageFlag = true;
						}
						
						// 마지막 페이지일 경우
						if( _dataCnt - _maxIndex <= 0 || chkNextPageFlag || chkAdjustablePageFlag )
						{
							if( grpAutoHeight &&  ( bandList.get(i).getGroupBand()==null || bandList.get(i).getGroupBand().equals("") ) &&  _isLastPage == false && chkAdjustablePageFlag == false && _dataCnt - _maxIndex == 0 && _dataPageFooterHeight == 0 )
							{
								float _headerHeight = 0;
								if( bandList.get(i).getHeaderBand() != null && bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.containsKey( bandList.get(i).getHeaderBand() ) )
								{
									_headerHeight = bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
								}
								_groupingNextPageH = currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight) - _headerHeight - _groupingFootHeight;
								_dataCnt = _dataCnt + (int) Math.floor( _groupingNextPageH/bandList.get(i).getHeight() ); 
								_isLastPage  = true;
							}
							else
							{
								// autoHeight값 처리( groupBand일경우에는 groupFooter를 만나는 부분까지의 모든 Height값을 구해서 마지막 페이지의 인덱스를 구하기 )
								if( grpAutoHeight  )
								{
									if( grpAutoHeight )
									{
										if( adjustableFlag )
										{
//											_groupMaxRowCnt = _maxIndex + (int) Math.floor( (maxHeight - ( _dataBandHeight + _dataPageFooterHeight + currnetItemY + _groupingFootHeight ))/ bandList.get(i).getHeight() ) ;
											_groupMaxRowCnt = _maxIndex + (int) Math.floor( (currentPageMaxHeight - ( _dataBandHeight + _dataPageFooterHeight + currnetItemY + _groupingFootHeight ))/ bandList.get(i).getHeight() ) ;
										}
										else
										{
//											_groupMaxRowCnt = (int) Math.floor( ( maxHeight - _dataPageFooterHeight - currnetItemY - _groupingFootHeight )/ bandList.get(i).getHeight() );
											_groupMaxRowCnt = (int) Math.floor( ( currentPageMaxHeight - _dataPageFooterHeight - currnetItemY - _groupingFootHeight )/ bandList.get(i).getHeight() );
										}
										
										
										if( _groupMaxRowCnt < _dataCnt )
//											if( (_maxIndex - _dataCnt)* bandList.get(i).getHeight() < _groupingFootHeight )
										{
											//현재 페이지에 푸터밴드를 그릴수 없을경우 데이터 카운트를 다음페이지의 푸터 만큼 빠진 영역으로 인덱스를 늘린다 
											_dataCnt = _maxIndex;
											if(bandList.get(i).getHeaderBand().equals("") == false )
											{
												if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeight() && bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().size() > 0 )
												{
//													_groupingNextPageH = maxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight) - bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
													_groupingNextPageH = currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight) - bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
												}
												else
												{
//													_groupingNextPageH = maxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight) - bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
													_groupingNextPageH = currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight) - bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
												}
											}
											else
											{
//												_groupingNextPageH = maxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight);
												_groupingNextPageH = currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight);
											}
											
											_groupingNextPageH = _groupingNextPageH - _groupingFootHeight;
											
											_dataCnt = _dataCnt + (int) Math.floor( _groupingNextPageH/bandList.get(i).getHeight() ); 
										}
										else
										{
											_maxIndex = _groupMaxRowCnt;
//											_maxIndex = _dataCnt + (int) Math.floor( ((_maxIndex - _dataCnt)* bandList.get(i).getHeight()-_groupingFootHeight)/bandList.get(i).getHeight()  );
										}
										
										grpAutoHeight = false;
										
									}
										
								}
								else
								{
									_maxIndex = _dataCnt;
								}
								
								
							}
							
						}
							
						
						
						for ( j = 0; j < bandList.get(i).getSequence().size(); j++) {
							
							if(bandList.get(i).getSequence().get(j).equals("s"))
							{
								bandPageInfo = new HashMap<String, Value>();
								bandPageInfo.put("startIndex", new Value( 0,"int"));
								bandPageInfo.put("lastIndex", new Value( 1,"int"));

								bandPageInfo.put("summeryStartIndex", new Value( _startIndex,"int"));
								bandPageInfo.put("summeryEndIndex", new Value( _startIndex + _maxIndex,"int"));
								
								bandPageInfo.put("y", new Value( currnetItemY,"number"));
								bandPageInfo.put("id", new Value( bandList.get(i).getSummery(),"string"));
								bandPageInfo.put("gprCurrentPageNum", new Value( _grpPageCnt,"number"));
								pageRowList.add(bandPageInfo);
								
								_dataPageFooterHeight = bandInfoData.get( bandList.get(i).getSummery() ).getHeight();
								currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getSummery() ).getHeight();
								
								chkBandList.add(bandPageInfo);
							}
							else 
							{
								bandPageInfo = new HashMap<String, Value>();
								bandPageInfo.put("startIndex", new Value( _startIndex,"int") );
								bandPageInfo.put("lastIndex", new Value(  _startIndex + _maxIndex,"int"));
								bandPageInfo.put("y", new Value( currnetItemY,"number"));
								bandPageInfo.put("id", new Value( bandList.get(i).getId(),"string"));
								// group Page 함수사용을 위하여 그룹별 groupCurrentPage값을 담기
								bandPageInfo.put("gprCurrentPageNum", new Value( _grpPageCnt,"number"));
								pageRowList.add(bandPageInfo);
								
								chkBandList.add(bandPageInfo);
								
								if( adjustableFlag )
								{
									if( (_startIndex+_maxIndex) > _rowHeightAr.size() )
									{
										_dataBandHeight = _dataBandHeight + ((_startIndex+_maxIndex)-_rowHeightAr.size() )* bandList.get(i).getHeight();
									}
									currnetItemY = currnetItemY + _dataBandHeight;
								}
								else 
								{
									currnetItemY = currnetItemY + (_maxIndex*bandList.get(i).getHeight());
								}
							}
							
							// tabIndex 담기
							if( bandList.get(i).getRequiredItems().size() > 0 )
							{
								_requiredItemList = getBandRequiredItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue() ,bandList.get(i), _requiredItemList );
							}
							// 필수값 담기
							// 데이터 밴드의 경우 데이터 사이즈만큼만 처리 할지 autoHeight로 늘어난 row만큼 할지 처리
							if( bandList.get(i).getTabIndexItem().size() > 0 )
							{
								int _lastSize = bandPageInfo.get("lastIndex").getIntValue();
								if(DataSet.get(bandList.get(i).getDataSet() ).size() < _lastSize )
								{
									_lastSize = DataSet.get(bandList.get(i).getDataSet() ).size();
								}
								_tabIndexItemList = getBandTabIndexItems( bandPageInfo.get("startIndex").getIntValue(), _lastSize ,bandList.get(i), _tabIndexItemList );
							}
							
						}
						
						if(_maxIndex == 0 && _isFistNoShow == false ) _maxIndex = 1;
						
						currentPageMaxHeight = maxHeight;
						_dataCnt = _dataCnt - _maxIndex;
						_startIndex = _startIndex + _maxIndex;
						
						// 원본 PAGE_INFO_TIDX 값을 이용하여 데이터밴드의 아이템을 실제 사용할 ID를 생성하여 페이지별로 담기
						
						if( _dataCnt > 0 )
						{
							pagesRowList.add(pageRowList);
							pageRowList = new ArrayList< HashMap<String,Value>>();
							currnetItemY = defaultY + pageHeaderHeight;
							
							if( OriginalTabIndexItemList.size() > 0 )
							{
								// tabIndex 담긴 배열 처리
								_tabIndexItemListPages.add(_tabIndexItemList);
								_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
							}
							if( OriginalRequiredItemList.size() > 0 )
							{
								// 필수 목록 처리
								_requiredItemListPages.add(_requiredItemList);
								_requiredItemList = new ArrayList<HashMap<String, Object>>();
							}
						}
						
						_grpPageCnt++;
					}
					
					// group Page 함수사용을 위하여 그룹별 groupTotalPage값을 담기
					if( chkBandList.size() > 0 )
					{
						for ( j = 0; j <chkBandList.size(); j++) {
							chkBandList.get(j).put("gprTotalPageNum", new Value( _grpPageCnt,"number"));
						}
					}
					
				}
				else if( bandClassName.equals( BandInfoMapData.CROSSTAB_BAND )  )
				{
					CrossTabBandParser mcrossTabBand = new CrossTabBandParser();
					mcrossTabBand.setFunctionVersion(mFunction.getFunctionVersion());
					
					if( currentPageMaxHeight - currnetItemY < bandList.get(i).getHeight() )
					{
						pagesRowList.add(pageRowList);
						pageRowList = new ArrayList< HashMap<String,Value>>();
						currnetItemY = defaultY + pageHeaderHeight;
						
						if( OriginalTabIndexItemList.size() > 0 )
						{
							// tabIndex 담긴 배열 처리
							_tabIndexItemListPages.add(_tabIndexItemList);
							_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
						}
						if( OriginalRequiredItemList.size() > 0 )
						{
							// 필수 목록 처리
							_requiredItemListPages.add(_requiredItemList);
							_requiredItemList = new ArrayList<HashMap<String, Object>>();
						}
					}
					
					mcrossTabBand.setExportData(isExportData);
					
					try {
						if( crossTabData == null )
						{
								crossTabData = new HashMap<String, ArrayList<ArrayList<HashMap<String,Value>>>>();
						}
						
						ArrayList<ArrayList<HashMap<String, Value>>> crossTabPages = null; 
								
						if(bandList.get(i).getFileLoadType().equals(GlobalVariableData.M_FILE_LOAD_TYPE_JSON))
						{
							crossTabPages = mcrossTabBand.convertCrossTabObjecttoItem(bandList.get(i).getOriginalItemData(),  DataSet, pageWidth, defaultY + pageHeaderHeight, maxHeight, currnetItemY, mXAr );
						}
						else
						{
							crossTabPages = mcrossTabBand.convertCrossTabXmltoItem(bandList.get(i).getItemXml(),  DataSet, pageWidth, defaultY + pageHeaderHeight, maxHeight, currnetItemY, mXAr );
						}
						
						if( crossTabPages == null ) continue;
						
						if( bandList.get(i).getVisibleType().equals(BandInfoMapData.VISIBLE_TYPE_ALL) && mcrossTabBand.getMaxPageWidth() > bandList.get(i).getWidth() )
						{
							bandList.get(i).setWidth(mcrossTabBand.getMaxPageWidth());
						}
						
						crossTabData.put(bandList.get(i).getId(), crossTabPages );
						
						// 리턴받은 크로스탭 데이터를 이용하여 총 페이지에 맞춰서 크로스탭정보를 지정
						int _crossTabPageSize = crossTabPages.size();
						float _crossTabMaxWidth = 0;
						
						for ( j = 0; j < _crossTabPageSize; j++) {
							
							if( j > 0 )
							{
								pagesRowList.add(pageRowList);
								pageRowList = new ArrayList< HashMap<String,Value>>();
								currnetItemY = defaultY + pageHeaderHeight;
							}
							
							bandPageInfo = new HashMap<String, Value>();
							bandPageInfo.put("startIndex",new Value( j,"int") );
							bandPageInfo.put("lastIndex",new Value( j+1,"int") );
							bandPageInfo.put("y", new Value( currnetItemY,"number"));
							bandPageInfo.put("id", new Value(bandList.get(i).getId(),"string"));
							bandPageInfo.put("crossTabStartIndex", new Value(pagesRowList.size()-j, "int"));
							pageRowList.add(bandPageInfo);
							
							if(mcrossTabBand.getMaxPageWidthAr().size() > 0 ) _crossTabMaxWidth = mcrossTabBand.getMaxPageWidthAr().get( j%mcrossTabBand.getMaxPageWidthAr().size() );
							
							if( bandList.get(i).getVisibleType().equals(BandInfoMapData.VISIBLE_TYPE_ALL) && _crossTabMaxWidth > bandList.get(i).getWidth() )
							{
								bandPageInfo.put("MAX_PAGE_WIDTH", new Value( _crossTabMaxWidth, "number" ));
							}
							else
							{
								bandPageInfo.put("MAX_PAGE_WIDTH", new Value( bandList.get(i).getWidth(), "number" ));
							}
							
							currnetItemY = crossTabPages.get(j).get( crossTabPages.get(j).size()-1 ).get("y").getIntegerValue() + crossTabPages.get(j).get( crossTabPages.get(j).size()-1 ).get("height").getIntegerValue();
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					}
				}
				else
				{
					// 
					boolean _useBandFlag = true;
					
					if( bandList.get(i).getClassName().equals("UBDataHeaderBand") && bandList.get(i).getUseHeaderBandGroupName().equals("")==false )
					{
						for (HashMap<String, Value> _bandlist : pageRowList) {
							if( _bandlist.containsKey("groupName") && _bandlist.get("groupName").getStringValue().equals( bandList.get(i).getUseHeaderBandGroupName() ) )
							{
								_useBandFlag = false;
								break;
							}
						}
					}
					
					if( bandList.get(i).getClassName().equals("UBGroupHeaderBand") && bandList.get(i).getGroupName().equals("") == false && 
							currnetItemY != defaultY + pageHeaderHeight && firstBand == false && bandList.get(i).getNewPage()  )
					{
						pagesRowList.add(pageRowList);
						pageRowList = new ArrayList< HashMap<String,Value>>();
						currnetItemY = defaultY + pageHeaderHeight;
						
						if( OriginalTabIndexItemList.size() > 0 )
						{
							// tabIndex 담긴 배열 처리
							_tabIndexItemListPages.add(_tabIndexItemList);
							_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
						}
						if( OriginalRequiredItemList.size() > 0 )
						{
							// 필수 목록 처리
							_requiredItemListPages.add(_requiredItemList);
							_requiredItemList = new ArrayList<HashMap<String, Object>>();
						}
						
					}
					else if(_useBandFlag && currentPageMaxHeight - currnetItemY < bandList.get(i).getHeight() )
					{
						if(pageRowList.size() > 0 )
						{
							pagesRowList.add(pageRowList);
							pageRowList = new ArrayList< HashMap<String,Value>>();
							currnetItemY = defaultY + pageHeaderHeight;
							
							if( OriginalTabIndexItemList.size() > 0 )
							{
								// tabIndex 담긴 배열 처리
								_tabIndexItemListPages.add(_tabIndexItemList);
								_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
							}
							if( OriginalRequiredItemList.size() > 0 )
							{
								// 필수 목록 처리
								_requiredItemListPages.add(_requiredItemList);
								_requiredItemList = new ArrayList<HashMap<String, Object>>();
							}
						}
						
						///@TEST
						if( bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.containsKey(bandList.get(i).getHeaderBand()) )
						{
							boolean _isChk = true;
							for (HashMap<String, Value> _bandlist : pageRowList) {
								if( _bandlist.containsKey("groupName") && _bandlist.get("groupName").getStringValue().equals( bandInfoData.get(bandList.get(i).getHeaderBand()).getUseHeaderBandGroupName() ) )
								{
									_isChk = false;
									break;
								}
							}
							
							if( _isChk )
							{
								
								if( (_useBandFlag || _startIndex > 0) && bandList.get(i).getHeaderBand().equals("") == false )
								{
									bandPageInfo = new HashMap<String, Value>();
									bandPageInfo.put("startIndex", new Value( 0,"int"));
									bandPageInfo.put("lastIndex",  new Value( 1,"int"));
									bandPageInfo.put("y", new Value( currnetItemY,"number"));
									bandPageInfo.put("id", new Value( bandList.get(i).getHeaderBand(),"string"));
//									bandPageInfo.put("gprCurrentPageNum", new Value( _grpPageCnt,"number"));
									
									if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName().equals("") == false )
									{
										bandPageInfo.put("groupName", new Value( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName(),"string"));
									}
									
									pageRowList.add(bandPageInfo);
									if( ( bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeight() || bandInfoData.get( bandList.get(i).getHeaderBand() ).getAutoTableHeight()) && bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().size() > 0 )
									{
										currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
									}
									else
									{
										currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
									}
									// tabIndex 담기
									if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getRequiredItems().size() > 0 )
									{
										_requiredItemList = getBandRequiredItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue() ,bandInfoData.get( bandList.get(i).getHeaderBand() ), _requiredItemList );
									}
									// 필수값 담기
									if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getTabIndexItem().size() > 0 )
									{
										_tabIndexItemList = getBandTabIndexItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue(),bandInfoData.get( bandList.get(i).getHeaderBand() ), _tabIndexItemList );
									}
									
//									chkBandList.add(bandPageInfo);
								}
								
							}
						}
						
						
					}
					else if(_useBandFlag && bandList.get(i).getClassName().equals("UBDataHeaderBand") )
					{
						
						String _argoGroupName = "";
						if( bandList.get(i).getGroupBand().equals("") == false )
						{
							
						}
						
						float dataBandHeight = 0;
						float headerHeight = 0;
						if( bandList.get(i).getDataBand().equals("") == false )
						{
							if( ( bandInfoData.get( bandList.get(i).getDataBand() ).getAdjustableHeight() || bandInfoData.get( bandList.get(i).getDataBand() ).getAutoTableHeight() ) && bandInfoData.get( bandList.get(i).getDataBand() ).getResizeText() == false &&
									bandInfoData.get( bandList.get(i).getDataBand() ).getAdjustableHeightListAr().size() > 0 )
							{
								dataBandHeight = bandInfoData.get( bandList.get(i).getDataBand() ).getAdjustableHeightListAr().get(0);
							}
							else
							{
								dataBandHeight = bandInfoData.get( bandList.get(i).getDataBand() ).getHeight();
							}
							
							if( bandInfoData.get( bandList.get(i).getDataBand() ).getSummery().equals("") == false )
							{
								dataBandHeight = dataBandHeight + bandInfoData.get(  bandInfoData.get( bandList.get(i).getDataBand() ).getSummery() ).getHeight();
							}
							
						}
						if( (bandList.get(i).getAdjustableHeight() || bandList.get(i).getAutoTableHeight()) && bandList.get(i).getAdjustableHeightListAr().size() > 0 )
						{
							headerHeight = bandList.get(i).getAdjustableHeightListAr().get(0);
						}
						else
						{
							headerHeight = bandList.get(i).getHeight();
						}
						
						if( defaultY + pageHeaderHeight < currnetItemY && currentPageMaxHeight - currnetItemY < headerHeight + dataBandHeight )
						{
							pagesRowList.add(pageRowList);
							pageRowList = new ArrayList< HashMap<String,Value>>();
							currnetItemY = defaultY + pageHeaderHeight;
							
							if( OriginalTabIndexItemList.size() > 0 )
							{
								// tabIndex 담긴 배열 처리
								_tabIndexItemListPages.add(_tabIndexItemList);
								_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
							}
							if( OriginalRequiredItemList.size() > 0 )
							{
								// 필수 목록 처리
								_requiredItemListPages.add(_requiredItemList);
								_requiredItemList = new ArrayList<HashMap<String, Object>>();
							}
						}
						
						
					}
					
					// 헤더의 adjustableHeight값 업데이트
					if(_useBandFlag)
					{
						bandPageInfo = new HashMap<String, Value>();
						bandPageInfo.put("startIndex",new Value( 0,"int") );
						bandPageInfo.put("lastIndex",new Value( 1,"int") );
						bandPageInfo.put("y", new Value( currnetItemY,"number"));
						bandPageInfo.put("id", new Value(bandList.get(i).getId(),"string"));
						
						if( bandList.get(i).getClassName().equals("UBDataHeaderBand") && bandList.get(i).getUseHeaderBandGroupName().equals("") == false )
						{
							bandPageInfo.put("groupName", new Value(bandList.get(i).getUseHeaderBandGroupName(),"string"));
						}
						
						pageRowList.add(bandPageInfo);
						
						if( (bandList.get(i).getAdjustableHeight() || bandList.get(i).getAutoTableHeight()) && bandList.get(i).getAdjustableHeightListAr().size() > 0 )
						{
							currnetItemY = currnetItemY + bandList.get(i).getAdjustableHeightListAr().get(0);
						}
						else
						{
							currnetItemY = currnetItemY + bandList.get(i).getHeight();
						}
						
						// tabIndex 담기
						if( bandList.get(i).getRequiredItems().size() > 0 )
						{
							_requiredItemList = getBandRequiredItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue() ,bandList.get(i), _requiredItemList );
						}
						// 필수값 담기
						if( bandList.get(i).getTabIndexItem().size() > 0 )
						{
							_tabIndexItemList = getBandTabIndexItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue(),bandList.get(i), _tabIndexItemList );
						}
						
					}
				}
				
				// 현재 Band가 groupBand인지 여부를 체크 그룹밴드일경우 이전 GroupBand와 다를경우 DataSet을 담아두고 기존에 담겨있던 밴드들의 GroupBandIdx와 GroupTotalPageCnt를 담아둔다. 
				if( bandList.get(i).getGroupBand().equals("") == false)
				{
					if(_currentGroupDSName.equals( bandList.get(i).getDataSet() ) == false )
					{
//						int _TotSize = ( pagesRowList.size() ) - _groupStartIdx;
						if( _groupTotalCnt == 0 ) _groupTotalCnt = 1;
						int _grpMaxSize = _groupListAr.size();
						for (int k = 0; k < _grpMaxSize; k++) {
							bandInfoData.get(_groupListAr.get(k)).setGroupStartPageIdx( _groupStartIdx );
							bandInfoData.get(_groupListAr.get(k)).setGroupTotalPageCnt( _groupTotalCnt);
						}
						
						if(_groupListAr.size() > 0)
						{
							for (int k = _groupStartIdx; k < _groupTotalCnt+_groupStartIdx; k++) {
								_grpStartPageMap.put(k, k - _groupStartIdx );
								_grpTotalPageMap.put(k, _groupTotalCnt );
							}
						}
						_currentGroupDSName = bandList.get(i).getDataSet();
						_groupStartIdx = pagesRowList.size();
						
						_groupTotalCnt = 0;
						_groupListAr.clear();
					}
					else
					{
						_groupTotalCnt = ( pagesRowList.size() +1 ) - _groupStartIdx;
					}

					_groupListAr.add( bandID );
					
				}
				else if( _currentGroupDSName.equals("") == false )
				{
//					int _TotSize = (pagesRowList.size() + _groupPageNewPage ) - _groupStartIdx;
					if( _groupTotalCnt == 0 ) _groupTotalCnt = 1;
					int _grpMaxSize = _groupListAr.size();
					for (int k = 0; k < _grpMaxSize; k++) {
						bandInfoData.get(_groupListAr.get(k)).setGroupStartPageIdx( _groupStartIdx );
						bandInfoData.get(_groupListAr.get(k)).setGroupTotalPageCnt(_groupTotalCnt );
					}
					
					if(_groupListAr.size() > 0)
					{
						for (int k = _groupStartIdx; k < _groupTotalCnt+_groupStartIdx; k++) {
							_grpStartPageMap.put(k, k - _groupStartIdx );
							_grpTotalPageMap.put(k, _groupTotalCnt );
						}
					}
					_groupListAr.clear();
					_groupTotalCnt = 0;
					_currentGroupDSName = "";
				}
				
				
				
				firstBand = false;
			}
			
		}
		
		if( pageRowList.size() > 0 )
		{
			pagesRowList.add(pageRowList);
			
			if( OriginalTabIndexItemList.size() > 0 )
			{
				// tabIndex 담긴 배열 처리
				_tabIndexItemListPages.add(_tabIndexItemList);
			}
			if( OriginalRequiredItemList.size() > 0 )
			{
				// 필수 목록 처리
				_requiredItemListPages.add(_requiredItemList);
			}
			
		}
		
		// 현재 Band가 groupBand인지 여부를 체크 그룹밴드일경우 이전 GroupBand와 다를경우 DataSet을 담아두고 기존에 담겨있던 밴드들의 GroupBandIdx와 GroupTotalPageCnt를 담아둔다. 
		if( _currentGroupDSName.equals("") == false )
		{
			int _TotSize =  pagesRowList.size() - _groupStartIdx;
			int _grpMaxSize = _groupListAr.size();
			for (int k = 0; k < _grpMaxSize; k++) {
				bandInfoData.get(_groupListAr.get(k)).setGroupStartPageIdx( _groupStartIdx );
				bandInfoData.get(_groupListAr.get(k)).setGroupTotalPageCnt( (pagesRowList.size()) - _groupStartIdx );
			}
			
			if(_groupListAr.size() > 0)
			{
				for (int k = _groupStartIdx; k < _TotSize+_groupStartIdx; k++) {
					_grpStartPageMap.put(k, k - _groupStartIdx );
					_grpTotalPageMap.put(k, _TotSize );
				}
			}
			
			_currentGroupDSName = "";
		}
		
		
		
		if( pagesRowList.size() == 0 && ( pageHeaderList.size() > 0 || pageFooterList.size() > 0 )  )
		{
			pageRowList = new ArrayList<HashMap<String,Value>>();
			pagesRowList.add(pageRowList);
			

			if( OriginalTabIndexItemList.size() > 0 )
			{
				// tabIndex 담긴 배열 처리
				_tabIndexItemListPages.add(_tabIndexItemList);
			}
			if( OriginalRequiredItemList.size() > 0 )
			{
				// 필수 목록 처리
				_requiredItemListPages.add(_requiredItemList);
			}
		}
		
		int maxCnt = 1;
		
		if( pageHeaderRepeat )
		{
			maxCnt = pagesRowList.size();
		}
		// 모드 밴드 완료후 페이지 헤더와 페이지 푸터를 각 페이지별로 Add
		float itemH = 0f;		//헤더가 2개 이상일경우 헤더별 Y값을 담기위하여 지정
		float _firstPageItemH = 0f;
		for ( i = 0; i < pageHeaderList.size(); i++) {
			
			for ( j = 0; j < maxCnt; j++) {
				
				if( j == 0 ||  bandInfoData.get(pageHeaderList.get(i)).getRepeat().equals("y") )
				{
					float _chkY = _firstPageItemH;
					if( j == 0 )
					{
						_chkY = _firstPageItemH;
					}
					else
					{
						_chkY = itemH;
					}
					
					bandPageInfo = new HashMap<String, Value>();
					bandPageInfo.put("startIndex",new Value( 0, "int") );
					bandPageInfo.put("lastIndex",new Value( 1, "int") );
					bandPageInfo.put("y", new Value( _chkY, "number"));
					bandPageInfo.put("id", new Value(pageHeaderList.get(i),"string"));
					
					if( _grpStartPageMap.containsKey(j) ) bandPageInfo.put("gprCurrentPageNum", new Value( _grpStartPageMap.get(j), "int") );
					if( _grpTotalPageMap.containsKey(j) ) bandPageInfo.put("gprTotalPageNum", new Value( _grpTotalPageMap.get(j), "int") );
					
					pagesRowList.get(j).add(i, bandPageInfo);
					
					// tabIndex 담기
					if( bandList.get(i).getRequiredItems().size() > 0 )
					{
						getBandRequiredItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue() ,bandInfoData.get(pageHeaderList.get(i)), _requiredItemListPages.get(j) );
					}
					// 필수값 담기
					if( bandList.get(i).getTabIndexItem().size() > 0 )
					{
						getBandTabIndexItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue(),bandInfoData.get(pageHeaderList.get(i)), _tabIndexItemListPages.get(j) );
					}
				}
				else
				{
					break;
				}
			}
			
			if(bandInfoData.get(pageHeaderList.get(i)).getRepeat().equals("y") )
			{
				itemH = itemH + bandInfoData.get(pageHeaderList.get(i)).getHeight();			
			}
			_firstPageItemH = _firstPageItemH + bandInfoData.get(pageHeaderList.get(i)).getHeight();
		}		
		
		maxCnt = 1;
		maxCnt = pagesRowList.size();
		
//		if( pageFooterRepeat )
//		{
//			maxCnt = pagesRowList.size();
//		}
		
//		float argoFooterH = 0f;
//		for ( i = 0; i < pageFooterList.size(); i++) {
//			
//			itemH = defaultHeight - pageFooterHeight;
//			int _stIdx = 0;
//			if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST && pageFooterRepeat == false)
//			{
//				_stIdx = maxCnt-1;
//			}
//			
//			for ( j = _stIdx ; j < maxCnt; j++) {
//				
//				if( ( !GlobalVariableData.GROUP_FOOTER_POSITION_LAST && j == 0 ) || (GlobalVariableData.GROUP_FOOTER_POSITION_LAST  && j == maxCnt-1) ||  bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
//				{
//					bandPageInfo = new HashMap<String, Value>();
//					bandPageInfo.put("startIndex",new Value( 0, "int") );
//					bandPageInfo.put("lastIndex",new Value( 1, "int") );
//					bandPageInfo.put("y", new Value( itemH, "number"));
//					bandPageInfo.put("id", new Value(pageFooterList.get(i),"string"));
//					
//					if( _grpStartPageMap.containsKey(j) ) bandPageInfo.put("gprCurrentPageNum", new Value( _grpStartPageMap.get(j), "int") );
//					if( _grpTotalPageMap.containsKey(j) ) bandPageInfo.put("gprTotalPageNum", new Value( _grpTotalPageMap.get(j), "int") );
//
//					
//					itemH = defaultHeight - repeatPageFooterHeight - argoFooterH;
//					
//					pagesRowList.get(j).add( bandPageInfo );
//					
//					// tabIndex 담기
//					if( bandList.get(i).getRequiredItems().size() > 0 )
//					{
//						getBandRequiredItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue() ,bandInfoData.get(pageFooterList.get(i)), _requiredItemListPages.get(j) );
//					}
//					// 필수값 담기
//					if( bandList.get(i).getTabIndexItem().size() > 0 )
//					{
//						getBandTabIndexItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue(),bandInfoData.get(pageFooterList.get(i)), _tabIndexItemListPages.get(j) );
//					}
//				}
//				else
//				{
//					break;
//				}
//			}
//			
//			
//			if( bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
//			{
//				argoFooterH = argoFooterH + bandInfoData.get(pageFooterList.get(i)).getHeight();			
//			}
//			
//		}	
		
		
		float _lastPageFHeight = defaultHeight;
		float _pageFHeight = defaultHeight;
		float _firstFHeight = defaultHeight;
		for ( i = 0; i < pageFooterList.size(); i++) {
			
			if( bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
			{
				_pageFHeight 		= _pageFHeight - bandInfoData.get(pageFooterList.get(i)).getHeight();
				_lastPageFHeight	= _lastPageFHeight -  bandInfoData.get(pageFooterList.get(i)).getHeight();
				_firstFHeight 		= _firstFHeight - bandInfoData.get(pageFooterList.get(i)).getHeight();
			}
			else
			{
				if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST )
				{
					_lastPageFHeight = _lastPageFHeight -  bandInfoData.get(pageFooterList.get(i)).getHeight();
					
				}
				else
				{
					_firstFHeight = _firstFHeight - bandInfoData.get(pageFooterList.get(i)).getHeight();
				}
			}
		}
		
		for ( i = 0; i < pageFooterList.size(); i++) {
			
			int _stIdx = 0;
			if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST && !bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
			{
				_stIdx = maxCnt-1;
				
				if( _stIdx == 0 ) _firstFHeight = _lastPageFHeight;
			}
			
			for ( j = _stIdx ; j < maxCnt; j++) {
				
				if( ( !GlobalVariableData.GROUP_FOOTER_POSITION_LAST && j == 0 ) || (GlobalVariableData.GROUP_FOOTER_POSITION_LAST  && j == maxCnt-1) ||  bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
				{
					if( j == 0 )itemH =	_firstFHeight; 
					else if( j == maxCnt-1 )itemH =	_lastPageFHeight; 
					else itemH =	_pageFHeight; 
					
					bandPageInfo = new HashMap<String, Value>();
					bandPageInfo.put("startIndex",new Value( 0, "int") );
					bandPageInfo.put("lastIndex",new Value( 1, "int") );
					bandPageInfo.put("y", new Value( itemH, "number"));
					bandPageInfo.put("id", new Value(pageFooterList.get(i),"string"));
					
					if( _grpStartPageMap.containsKey(j) ) bandPageInfo.put("gprCurrentPageNum", new Value( _grpStartPageMap.get(j), "int") );
					if( _grpTotalPageMap.containsKey(j) ) bandPageInfo.put("gprTotalPageNum", new Value( _grpTotalPageMap.get(j), "int") );
					
					pagesRowList.get(j).add( bandPageInfo );
					
					// tabIndex 담기
					if( bandList.get(i).getRequiredItems().size() > 0 )
					{
						getBandRequiredItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue() ,bandInfoData.get(pageFooterList.get(i)), _requiredItemListPages.get(j) );
					}
					// 필수값 담기
					if( bandList.get(i).getTabIndexItem().size() > 0 )
					{
						getBandTabIndexItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue(),bandInfoData.get(pageFooterList.get(i)), _tabIndexItemListPages.get(j) );
					}
				}
				else
				{
					break;
				}
			}
			
			if( bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
			{
				_pageFHeight 		= _pageFHeight + bandInfoData.get(pageFooterList.get(i)).getHeight();
				_lastPageFHeight	= _lastPageFHeight +  bandInfoData.get(pageFooterList.get(i)).getHeight();
				_firstFHeight 		= _firstFHeight + bandInfoData.get(pageFooterList.get(i)).getHeight();
			}
			else
			{
				if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST )
				{
					_lastPageFHeight = _lastPageFHeight +  bandInfoData.get(pageFooterList.get(i)).getHeight();
					
				}
				else
				{
					_firstFHeight = _firstFHeight + bandInfoData.get(pageFooterList.get(i)).getHeight();
				}
			}
			
		}	
		
		
		ArrayList<Object> returnArrayList = new ArrayList<Object>();
		
		returnArrayList.add(pagesRowList);
		returnArrayList.add(crossTabData);

		returnArrayList.add(_requiredItemListPages);
		returnArrayList.add(_tabIndexItemListPages);
		
//		return pagesRowList;
		return returnArrayList;
	}
	
	/** makeRowHeightListSimple st
	 	makeRowHeightListSimple ed	*/
	public ArrayList<Object> makeRowHeightListSimple( ArrayList<BandInfoMapDataSimple> bandList, HashMap<String, BandInfoMapDataSimple> bandInfoData, float currentY, float defaultY, float defaultHeight, float pageWidth, ArrayList<Integer> mXAr, HashMap<String, String> originalDataMap ) throws ScriptException
	{
		boolean pageHeaderRepeat = false;	// 페이지 헤더 밴드의 repeat여부 체크
		boolean pageFooterRepeat = false;	// 페이지 푸터 밴드의 reepat여부 체크
		float pageHeaderHeight = 0f;		// 페이지 헤더 밴드의 height
		float pageFooterHeight = 0f;		// 페이지 푸터 밴드의 height
		float currnetItemY = 0f;			// 다음 아이템의 Y값을 담는 값
		float currentPageMaxHeight = 0f;	// 현재 페이지의 최대 Height값을 담기
		float repeatPageFooterHeight = 0f;	// 현재 페이지의 최대 Height값을 담기
		float maxHeight = defaultHeight;
		int i = 0;
		int j = 0;
		
		HashMap<String, ArrayList<ArrayList<HashMap<String, Object>>> > crossTabData = null;
		
		ArrayList<String> pageHeaderList = new ArrayList<String>();
		ArrayList<String> pageFooterList = new ArrayList<String>();
		String bandID = "";
		String bandClassName = "";
		
		ArrayList<ArrayList<HashMap<String, Object>>> pagesRowList = new ArrayList<ArrayList<HashMap<String, Object>>>(); 	// 모든 페이지의 페이지별 밴드명과 row정보를 담기
		ArrayList<HashMap<String, Object>> pageRowList = new ArrayList<HashMap<String, Object>>();							// 한페이지별로 밴드명과 밴드 cnt값을 담아두기
		
		
		ArrayList<ArrayList<HashMap<String, Object>>> _requiredItemListPages = new ArrayList<ArrayList<HashMap<String, Object>>>();	// 페이지별로 필수 요소값을 가진 아이템의 ID를 담기
		ArrayList<ArrayList<HashMap<String, Object>>> _tabIndexItemListPages = new ArrayList<ArrayList<HashMap<String, Object>>>();	// 페이지별로 탭 Index를 가진 아이템의 순서대로 담기
		ArrayList<HashMap<String, Object>> _requiredItemList = new ArrayList<HashMap<String, Object>>();							// 한페이지의 필수 요소값을 가진 아이템의 ID를 담은 배열
		ArrayList<HashMap<String, Object>> _tabIndexItemList = new ArrayList<HashMap<String, Object>>();							// 한페이지의 TabIndex값을 가진 배열
		
		currentPageMaxHeight = defaultHeight;
		
		for ( i = 0; i < bandList.size(); i++ ) {
			
			bandClassName = bandList.get(i).getClassName();
			bandID = bandList.get(i).getId();
			
			if( bandInfoData.containsKey(bandID) == false ) continue;		// 밴드 정보에 해당 ID가 없을경우 continue 
			
			// band별 ubfx처리 ( visible값이 false일경우 continue시키고 밴드의 visible 속성을 false로 지정 )
			if( bandList.get(i).getVisible() != false && bandList.get(i).getUbFunction() != null && bandList.get(i).getUbFunction().size() > 0 )
			{
				boolean _isBandVisible = true;
				
				int _ubfxSize = bandList.get(i).getUbFunction().size();
				
				for (int k = 0; k < _ubfxSize; k++) {
					HashMap<String, String> _fxMap = bandList.get(i).getUbFunction().get(k);
					
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_fxMap.get("value") , 0,0,0, -1, -1, bandList.get(i).getDataSet());
					}else{
						_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(i).getDataSet() );
					}
					
					
					_fnValue = _fnValue.trim();
					
					if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
					{
						bandList.get(i).setVisible(false);
						
					    _isBandVisible = false;
						  
						break;
					}
				}
				
				if( !_isBandVisible ){ continue; }
			}
			else if( !bandList.get(i).getVisible() )
			{
				continue;
			}
			
			
			if( bandClassName.equals( BandInfoMapData.PAGE_HEADER_BAND ) )
			{
				
				pageHeaderList.add(bandID);			// 페이지 헤더의 repeat값이 Y일경우 repeatFlag값을 true로 지정하고 headerHeight값을 담기
				if( bandInfoData.get(bandID).getRepeat().equals("y") )
				{
					pageHeaderRepeat = true;
					pageHeaderHeight = pageHeaderHeight + bandInfoData.get(bandID).getHeight();
				}
				
				currnetItemY = currnetItemY + bandInfoData.get(bandID).getHeight();
			}
			else if( bandClassName.equals( BandInfoMapData.PAGE_FOOTER_BAND ) )
			{
				
				pageFooterHeight = pageFooterHeight + bandInfoData.get(bandID).getHeight();
				
//				if( !GlobalVariableData.GROUP_FOOTER_POSITION_LAST ) currentPageMaxHeight = maxHeight - pageFooterHeight;
				if( !GlobalVariableData.GROUP_FOOTER_POSITION_LAST ) currentPageMaxHeight = maxHeight - bandInfoData.get(bandID).getHeight();
				
				pageFooterList.add(bandID);
				if( bandInfoData.get(bandID).getRepeat().equals("y") )
				{
					pageFooterRepeat = true;
					repeatPageFooterHeight = repeatPageFooterHeight + bandInfoData.get(bandID).getHeight();
					
//					if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST ) currentPageMaxHeight = maxHeight - pageFooterHeight;
					if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST ) currentPageMaxHeight = maxHeight - bandInfoData.get(bandID).getHeight();
					maxHeight = currentPageMaxHeight;
				}
				
			}
			
		}
		// 페이지 헤더 와 푸터의 height값을 담아두기
		
		int _startIndex = 0;
		int _maxIndex = 0;
		int _dataCnt = 0;
		
		float _dataHeaderHeight = 0f;
		float _dataPageFooterHeight = 0f;
		String _checkNextBandClass = "";
		float _groupingFootHeight = 0;
		float _groupingNextPageH = 0;
		Boolean grpAutoHeight = false;
		Boolean firstBand = true;
		Boolean adjustableFlag = false;
		Boolean isAutoTableHeight = false;
		int _groupMaxRowCnt = 0;
		
		ArrayList<Float> _rowHeightAr = new ArrayList<Float>();		// adjustableHeight 각 Row별 Height값을 담아두는 배열
		HashMap<String, Object> bandPageInfo = new HashMap<String,Object>(); 
		float _chkRowHeight = 0;
		mFunction.setDatasetList(DataSet);
		mFunction.setOriginalDataMap(originalDataMap);
		
		// 밴드별 adjustableHeight값 업데이트
		float _chkAutherHeight = 0;
		for ( i = 0; i < bandList.size(); i++ ) {
			
			_chkAutherHeight = 0;
			
			if(bandList.get(i).getAutoTableHeight()) bandList.get(i).setResizeText(false);	
			
			if((bandList.get(i).getAutoTableHeight() || bandList.get(i).getAdjustableHeight()) && bandList.get(i).getResizeText() == false )
			{
				_rowHeightAr = new ArrayList<Float>();
				if(bandList.get(i).getClassName().equals(BandInfoMapData.DATA_BAND))
				{
					if( bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.containsKey( bandList.get(i).getHeaderBand() )  )
					{
						_chkAutherHeight = _chkAutherHeight +  bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
					}
					if(bandList.get(i).getSummery().equals("") == false && bandInfoData.containsKey( bandList.get(i).getSummery() ) )
					{
						_chkAutherHeight = _chkAutherHeight +  bandInfoData.get( bandList.get(i).getSummery() ).getHeight();
					}
					
				}
				else
				{
					_chkAutherHeight = 0;
				}
				
				try {
					_dataCnt = DataSet.get(bandList.get(i).getDataSet() ).size();
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				if( bandList.get(i).getClassName().equals(BandInfoMapData.DATA_BAND) == false )
				{
					_dataCnt = 1;
				}
				ArrayList<HashMap<String , ArrayList<Float>>> _tableRowHeight = new ArrayList<HashMap<String , ArrayList<Float>>>();
				for ( j = 0; j <_dataCnt; j++) {
					
					if(bandList.get(i).getAutoTableHeight()){						
						
						HashMap<String , ArrayList<Float>> hmTableRowHeight = getRowAdjustableHeightArraySimple(bandList.get(i), j, maxHeight - _chkAutherHeight-10, maxHeight - _chkAutherHeight-10, bandInfoData);
						
						ArrayList<Float> arTableRowHeight;
						
						Iterator<String> keys = hmTableRowHeight.keySet().iterator();
						float tempBandHeight = 0;
						while( keys.hasNext() ){		
							_chkRowHeight = 0;
							String key = keys.next();		
							arTableRowHeight = hmTableRowHeight.get(key);
							for(int v = 0; v<arTableRowHeight.size();v++){
								_chkRowHeight = _chkRowHeight + arTableRowHeight.get(v);
							}
							
							if(bandList.get(i).getGroupBand() == null || bandList.get(i).getGroupBand().equals("")){
								_chkRowHeight = _chkRowHeight + bandInfoData.get(bandList.get(i).getId().toString()).getTableBandY().get(key);
							}else{
								_chkRowHeight = _chkRowHeight + bandInfoData.get(bandList.get(i).getDefaultBand().toString()).getTableBandY().get(key);
							}
							
							//table이 여러개 인경우 height 값이 큰 table을 기준으로 band Height 를 설정한다.(위아래로 디자인 된 테이블은 고려하지 않는다.) 
							if(tempBandHeight>_chkRowHeight){
								_chkRowHeight = tempBandHeight;
							}
							tempBandHeight = _chkRowHeight;
						}					
						
						//_chkRowHeight = getRowAdjustableHeight(bandList.get(i), j, maxHeight - _chkAutherHeight-10, maxHeight - _chkAutherHeight-10, bandInfoData);
						
						if( bandList.get(i).getHeight() > _chkRowHeight )
						{
							_chkRowHeight = bandList.get(i).getHeight();
						}
						
						_rowHeightAr.add(_chkRowHeight);	// band의 Row별 Height값을 담기
						_tableRowHeight.add(hmTableRowHeight);
					}else {
						_chkRowHeight = getRowAdjustableHeightSimple(bandList.get(i), j, maxHeight - _chkAutherHeight-10, maxHeight - _chkAutherHeight-10, bandInfoData);
						
						if( bandList.get(i).getHeight() > _chkRowHeight )
						{
							_chkRowHeight = bandList.get(i).getHeight();
						}
						
						_rowHeightAr.add(_chkRowHeight);	// band의 Row별 Height값을 담기
					}
					                                        
				}
				
				bandList.get(i).setAdjustableHeightListAr(_rowHeightAr);
				if(bandList.get(i).getAutoTableHeight()){
					bandList.get(i).setTableRowHeight(_tableRowHeight);				
				}
			}
		}
		
		boolean _isNextNewPage = false;
		String _newPageGroupName = "";
		int _maxBandSize = bandList.size();
		
		// groupCurrentIndex를 위해 시작 IDX를 담아둔다
		int _groupStartIdx = 0;
		int _groupTotalCnt = 0;
		String _currentGroupDSName = "";
		ArrayList<String> _groupListAr = new ArrayList<String>();
		// groupTotalPageCnt 를 위해 시작 page에서 마지막 page수를 이용하여 총 페이지수를 구하기
		
		HashMap<Integer, Integer> _grpStartPageMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> _grpTotalPageMap = new HashMap<Integer, Integer>();
		
		for ( i = 0; i < _maxBandSize; i++ ) {
			
			bandClassName = bandList.get(i).getClassName();
			bandID = bandList.get(i).getId();
			grpAutoHeight = false;
			adjustableFlag = false;
			_groupMaxRowCnt = 0;
			_rowHeightAr = new ArrayList<Float>();
			
			// band별 ubfx처리 ( visible값이 false일경우 continue시키고 밴드의 visible 속성을 false로 지정 )
			if( bandList.get(i).getVisible() != false && bandList.get(i).getUbFunction() != null && bandList.get(i).getUbFunction().size() > 0 )
			{
				boolean _isBandVisible = true;
				
				int _ubfxSize = bandList.get(i).getUbFunction().size();
				
				for (int k = 0; k < _ubfxSize; k++) {
					HashMap<String, String> _fxMap = bandList.get(i).getUbFunction().get(k);
					
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_fxMap.get("value") , 0,0,0, -1, -1, bandList.get(i).getDataSet());
					}else{
						_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(i).getDataSet() );
					}
					
					
					_fnValue = _fnValue.trim();
					
					if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
					{
						bandList.get(i).setVisible(false);
						
					    _isBandVisible = false;
						  
						break;
					}
				}
				
				if( !_isBandVisible ){ continue; }
			}
			else if( bandList.get(i).getVisible() == false )
			{
				continue;
			}
			
			// 페이지 헤더와 푸터는 모든 페이지정보를 만든후 add처리 ( repeat값에 따라 add위치를 지정해야함 )  
			if( bandClassName.equals( BandInfoMapData.PAGE_HEADER_BAND ) || 
					bandClassName.equals( BandInfoMapData.DATA_PAGE_FOOTER_BAND ) )
			{
				continue;
			}
			else if( bandClassName.equals( BandInfoMapData.PAGE_FOOTER_BAND )  )
			{
				// Page Footer Band가 현재 영역에 포함이 될수 없을경우 
				if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST && bandList.get(i).getRepeat().equals("n") && currnetItemY + pageFooterHeight > maxHeight )
				{
					pagesRowList.add(pageRowList);
					pageRowList = new ArrayList< HashMap<String,Object>>();
					pagesRowList.add(pageRowList);
					currnetItemY = defaultY + pageHeaderHeight;
				}
				continue;
			}
			else
			{
				int _groupPageNewPage = 0;
				// GroupFooterBand이고 newPage속성이 true일경우 다음 아이템의  CurrentItemY값을 다음페이지로 보낸다.
				// 다음 밴드의 GruopBand가 같아야만 newPage속성을 사용
				// 다음 밴드가 GroupFooterBand가 아닐경우에만 newPage처리 필요. 
				if( bandList.get(i).getClassName().equals("UBGroupFooterBand") && bandList.get(i).getNewPage() )
				{
					_newPageGroupName = bandList.get(i).getGroupHeader();
					_isNextNewPage = true;
				}
				else if(  !bandList.get(i).getClassName().equals("UBGroupFooterBand") && _isNextNewPage )
				{
					if( bandList.get(i).getGroupBand().equals(_newPageGroupName))
					{
						pagesRowList.add(pageRowList);
						pageRowList = new ArrayList< HashMap<String,Object>>();
						currnetItemY = defaultY + pageHeaderHeight;
						
					}
					_isNextNewPage = false;
					_newPageGroupName = "";
				}
				
				// 데이터 밴드일경우 Height값을 이용하여 페이지별로 화면에 표시할 아이템을 담아두기
				if( bandClassName.equals( BandInfoMapData.DATA_BAND ) && DataSet.containsKey(bandList.get(i).getDataSet()) )
				{
					try {
						_dataCnt = DataSet.get(bandList.get(i).getDataSet() ).size();
						
						if(bandList.get(i).getUseLabelBand())
						{
							_dataCnt = (int) Math.ceil( (float) _dataCnt / bandList.get(i).getLabelBandColCount() ); 
						}
						
						// 데이터 밴드의 Min Row 지정
						if(  bandList.get(i).getAutoHeight() == false && bandList.get(i).getMinRowCount() > 0 && bandList.get(i).getMinRowCount() > _dataCnt )
						{
							_dataCnt = bandList.get(i).getMinRowCount();
						}
						
					} catch (Exception e) {
						// TODO: handle exception
//						System.out.print(" ContinueBandParser 1603 line ");
					}
					
					if(_dataCnt < 1 ) continue;
					
					_startIndex = 0;
					_dataHeaderHeight = 0;
					_dataPageFooterHeight = 0;
					_groupingFootHeight = 0;
					
					if(bandList.get(i).getSummery().equals(""))
					{
						_dataPageFooterHeight = 0;
					}
					else
					{
						_dataPageFooterHeight = bandInfoData.get(bandList.get(i).getSummery()).getHeight();
					}
					
					if( currentPageMaxHeight - currnetItemY < bandList.get(i).getHeight()+_dataPageFooterHeight )
					{
						pagesRowList.add(pageRowList);
						pageRowList = new ArrayList< HashMap<String,Object>>();
						currnetItemY = defaultY + pageHeaderHeight;
					}
					
					// adjustableHeight값이 true일경우 각 Row별 Height값을 구하여 담기.
					//bandList.get(i).getChildren()
					// 현재 currentItemY값과 defautlPageHeight값을 구하여 처리
					if( bandList.get(i).getAdjustableHeight() && bandList.get(i).getResizeText() )
					{
						// adjustHeight값이 true이고 resizeText값이 true일경우 Row별 Height값을 담고, 현재 페이지를 벗어난 text의 경우 분할된 데이터를 원본 데이터에 추가시켜 처리
						// 추후 구현 해야함. 현재 각 Row별 Height값만 받는 형태로 되어잇음, 추후 분할된 데이터의 컬럼을받고 분할된 데이터의 총 페이지를 넘겨받아서 처리 해야함.
						
						//	adjustableHeight & resize 
						//  원본 데이터셋을 clone 처리 하고 originalDataMap에  원본 데이터셋명과 쿼리를 같이 지정
						ArrayList<HashMap<String, Object>>	_secondDs = (ArrayList<HashMap<String, Object>>) DataSet.get( bandList.get(i).getDataSet() ).clone();
						
						String _originalDSName = bandList.get(i).getDataSet();
						String _newDS_ID = "N_" + i + bandList.get(i).getId()+ "_" + _originalDSName;
						DataSet.put( _newDS_ID , _secondDs);
						bandList.get(i).setDataSet(_newDS_ID);
						
						// GroupBand일경우 originalDataMap에 데이터셋명과 원본 데이터셋명을 담기
//						String _originalDSName = bandList.get(i).getDataSet();
						
						if( bandList.get(i).getGroupBand() != null && bandList.get(i).getGroupBand().equals("") == false )
						{
							_originalDSName = bandInfoData.get( bandList.get(i).getDefaultBand() ).getDataSet();
						}
						
						originalDataMap.put( _newDS_ID, _originalDSName );
						
						float _chkHeaderHeight = 0;
						if( bandList.get(i).getHeaderBand().equals("") == false  )
						{
							_chkHeaderHeight =  bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
						}
						_chkRowHeight = 0;
						adjustableFlag = true;
						
						float _adjustCurrentPageMaxHeight = currentPageMaxHeight - currnetItemY;
						
						// bandList.get(i).getAdjustableHeight() 값이 있을경우 데이터셋을 새로 생성하고 새로 생성한 데이터셋을 Band의 DataSet로 지정하여 진행
						
						int _addDataCnt = 0;
						for ( j = 0; j < _dataCnt; j++) {
							
							if(_adjustCurrentPageMaxHeight < bandList.get(i).getHeight() )
							{
								_adjustCurrentPageMaxHeight = maxHeight - _chkHeaderHeight - _dataPageFooterHeight - pageHeaderHeight;
							}
							// resizeText처리시 
							ArrayList<Object> adjustListAr = getRowAdjustableHeightResizeTextSimple(bandList.get(i), j + _addDataCnt, _adjustCurrentPageMaxHeight - _dataPageFooterHeight, maxHeight - pageHeaderHeight - _chkHeaderHeight - _dataPageFooterHeight, bandInfoData);
							
							ArrayList<Float> chkMaxHeightAr = new ArrayList<Float>();
							ArrayList<Object> chkData;
							
							String _chkID = "";
							String _chkDATA = "";
//							ArrayList<String> _resultTextAr;
							ArrayList<Object> _resultTextAr;
							ArrayList<Float> _resultHeightAr; 
							
							ArrayList<HashMap<String, Object>> _resultDataSet = new ArrayList<HashMap<String,Object>>(); 
							
							for (int k = 0; k < adjustListAr.size(); k++) {
								
								chkData = (ArrayList<Object>) adjustListAr.get(k);
								
								_chkID = (String) chkData.get(0);
//								_resultTextAr = (ArrayList<String>) chkData.get(1);
								_resultTextAr = (ArrayList<Object>) chkData.get(1);
								_resultHeightAr = (ArrayList<Float>) chkData.get(2);
								
								if(_resultHeightAr.size() > chkMaxHeightAr.size() )
								{
									chkMaxHeightAr = _resultHeightAr;
								}
								else if( _resultHeightAr.size() > 0 && (_resultHeightAr.size() == chkMaxHeightAr.size()) )
								{
									if(  _resultHeightAr.get(_resultHeightAr.size() -1) > chkMaxHeightAr.get(chkMaxHeightAr.size() -1) )
									{
										chkMaxHeightAr = _resultHeightAr;
									}
								}
								
								HashMap<String, Object> addMap = new HashMap<String, Object>();
								addMap.put("ID", _chkID);
								addMap.put("DATA", _resultTextAr);
								_resultDataSet.add( addMap );
							}
								
							for (int k = 0; k < chkMaxHeightAr.size(); k++) {
								
								if( chkMaxHeightAr.size() == 1 && chkMaxHeightAr.get(k) < bandList.get(i).getHeight() )
								{
									_rowHeightAr.add(bandList.get(i).getHeight());	// band의 Row별 Height값을 담기 
								}
								else
								{
									if( chkMaxHeightAr.get(k) < bandList.get(i).getHeight() )
									{
										_rowHeightAr.add(bandList.get(i).getHeight());	// band의 Row별 Height값을 담기 
									}
									else
									{
										_rowHeightAr.add(chkMaxHeightAr.get(k));	// band의 Row별 Height값을 담기
									}
								}
							}
							ArrayList<HashMap<String, Object>> addArray = new ArrayList<HashMap<String,Object>>();
							HashMap<String, Object> cloneData = null;
							
							for (int k = 0; k < _resultDataSet.size(); k++) {
								
								_chkID = (String) _resultDataSet.get(k).get("ID");
								ArrayList<String> _chkArray = (ArrayList<String>) _resultDataSet.get(k).get("DATA");
								
//								for (int k2 = 0; k2 < _chkArray.size(); k2++) {
								for (int k2 = 0; k2 < chkMaxHeightAr.size(); k2++) {
									
									if(addArray.size() <= k2)
									{
										if( DataSet.get(bandList.get(i).getDataSet() ).size() > j + _addDataCnt )
										{
											cloneData = (HashMap<String, Object>) DataSet.get(bandList.get(i).getDataSet() ).get(j + _addDataCnt).clone();
										}
										else
										{
											cloneData = new HashMap<String, Object>();
										}
										addArray.add(cloneData);
									}
									
									if(_chkArray.size() > k2)
									{
										addArray.get(k2).put(_chkID,  _chkArray.get(k2));
									}
									else
									{
										addArray.get(k2).put(_chkID,  "");
									}
									
								}
								
							}
							
							for (int k = 0; k < addArray.size(); k++) {
								if(k == 0 && DataSet.get(bandList.get(i).getDataSet() ).size() > 0  )
								{
									DataSet.get(bandList.get(i).getDataSet() ).set( j + _addDataCnt + k, addArray.get(k) );
								}
								else
								{
									DataSet.get(bandList.get(i).getDataSet() ).add(j + _addDataCnt + k, addArray.get(k) );
									
									// ResizeFont정보를 담아둔 객체를 추가된 Row만큼 Add
									if( bandList.get(i).getResizeFontData().size() > j + _addDataCnt )
									{
										bandList.get(i).getResizeFontData().add(j + _addDataCnt + k, (HashMap<String, Float>) bandList.get(i).getResizeFontData().get(j + _addDataCnt).clone());
									}
								}
							}
							
							_addDataCnt = _addDataCnt + chkMaxHeightAr.size()-1;
							if(chkMaxHeightAr.size() > 1)
							{
								_adjustCurrentPageMaxHeight = maxHeight - pageHeaderHeight - _chkHeaderHeight - _dataPageFooterHeight - _rowHeightAr.get(_rowHeightAr.size()-1);
							}
							else
							{
								if(_adjustCurrentPageMaxHeight < _rowHeightAr.get(_rowHeightAr.size()-1) )
								{
									_adjustCurrentPageMaxHeight = maxHeight - pageHeaderHeight - _chkHeaderHeight - _dataPageFooterHeight - _rowHeightAr.get(_rowHeightAr.size()-1);
								}
								else
								{
									_adjustCurrentPageMaxHeight = _adjustCurrentPageMaxHeight - _rowHeightAr.get(_rowHeightAr.size()-1);
								}
							}
							
						}
						
						_dataCnt = DataSet.get(bandList.get(i).getDataSet() ).size();
						bandList.get(i).setAdjustableHeightListAr(_rowHeightAr);
					}
					else if( bandList.get(i).getAdjustableHeight() )
					{
						adjustableFlag = true;
					}
					
					// autoTableHeight 처리 
					if( bandList.get(i).getAutoTableHeight() ) adjustableFlag = true;
					
					// Height값이 현재 페이지에 표시 할수 없을경우 resizeText속성이 잇을경우 분할하여 처리 아닐경우 다음 페이지에 위치( Height값이 defaultHeight값보다 
					// 클경우 defaultHeight값으로 변경 )
					
					// autoHeight처리시 pageHeight값이 -1일경우에는 기능을 사용하지 않는다 ( Excel의 밴드 내보내기 형태이기 때문에 AutoHeight속성이 의미가 없어짐 )
					// autoHeight값 처리( groupBand일경우에는 groupFooter를 만나는 부분까지의 모든 Height값을 구해서 마지막 페이지의 인덱스를 구하기 )
					if( bandList.get(i).getAutoHeight() && ( bandList.get(i).getGroupBand().equals("") || bandList.get(i).getNewPage() ) )
					{
						if( bandList.get(i).getAutoHeight() &&  bandList.get(i).getGroupBand().equals("") )
						{
							grpAutoHeight = true;
							
							for ( j = (i+1); j < bandList.size(); j++) {
								_checkNextBandClass = bandList.get(j).getClassName();
								
								// 다음 밴드가 GroupHeaderBand일 경우에도 포함
								if(  _checkNextBandClass.equals("UBGroupHeaderBand") || _checkNextBandClass.equals("UBDataHeaderBand") ||  _checkNextBandClass.equals("UBDataBand")  || _checkNextBandClass.equals("UBEmptyBand") )
								{
									grpAutoHeight = true;
									break;
								}
								else if( _checkNextBandClass.equals("UBDataPageFooterBand") == false &&  _checkNextBandClass.equals("UBPageFooterBand") == false)
								{
									boolean _isBandVisible = true;
									
									if( bandList.get(j).getUbFunction() != null && bandList.get(j).getUbFunction().size() > 0 )
									{
										int _ubfxSize = bandList.get(j).getUbFunction().size();
										
										for (int k = 0; k < _ubfxSize; k++) {
											HashMap<String, String> _fxMap = bandList.get(j).getUbFunction().get(k);
											
											
											String _fnValue;
											
											if( mFunction.getFunctionVersion().equals("2.0") ){
												_fnValue = mFunction.testFN(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(j).getDataSet());
											}else{
												_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(j).getDataSet() );
											}
											
											
											_fnValue = _fnValue.trim();
											
											if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
											{
												bandList.get(j).setVisible(false);
											    _isBandVisible = false;
												break;
											}
										}
									}
									
									if( _isBandVisible ) _groupingFootHeight = _groupingFootHeight + bandList.get(j).getHeight();
								}
								else if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST && _checkNextBandClass.equals("UBPageFooterBand") && bandList.get(j).getRepeat().equals("n") )
								{
									 _groupingFootHeight = _groupingFootHeight + bandList.get(j).getHeight();
								}
							}
							
						}
						else
						{
							grpAutoHeight = true;
							// 그룹밴드의 autoHeight값이 true이고 newPage값이 true일경우 데이터 푸터 밴드를 찾아서 Height값을 처리한다
							for ( j = (i+1); j < bandList.size(); j++) {
								_checkNextBandClass = bandList.get(j).getClassName();
								if( _checkNextBandClass.equals("UBGroupHeaderBand") )
								{
									grpAutoHeight = true;
									break;
								}
								else if( _checkNextBandClass.equals("UBDataHeaderBand") )
								{
									grpAutoHeight = true;
									break;
								}
								else if(  _checkNextBandClass.equals("UBDataPageFooterBand") == false &&  _checkNextBandClass.equals("UBPageFooterBand") == false )
								{
									
									boolean _isBandVisible = true;
									
									if( bandList.get(j).getUbFunction() != null && bandList.get(j).getUbFunction().size() > 0 )
									{
										int _ubfxSize = bandList.get(j).getUbFunction().size();
										
										for (int k = 0; k < _ubfxSize; k++) {
											HashMap<String, String> _fxMap = bandList.get(j).getUbFunction().get(k);
											
											
											String _fnValue;
											
											if( mFunction.getFunctionVersion().equals("2.0") ){
												_fnValue = mFunction.testFN(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(j).getDataSet());
											}else{
												_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(j).getDataSet() );
											}
											
											
											_fnValue = _fnValue.trim();
											
											if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
											{
												bandList.get(j).setVisible(false);
											    _isBandVisible = false;
												break;
											}
										}
									}
									
									if(_isBandVisible)
									{
										if( _checkNextBandClass.equals("UBDataBand") || _checkNextBandClass.equals("UBEmptyBand") )
										{
											grpAutoHeight = false;
										}
										_groupingFootHeight = _groupingFootHeight + bandList.get(j).getHeight();
									}
								}
							}
						}
					}
					
					
					int _lastCnt = 0;
					float _dataBandHeight = 0;
					int _grpPageCnt = 0;
					boolean chkAdjustablePageFlag = false;
					boolean _isLastPage = false;
					boolean _isFistNoShow = false;
					
					ArrayList<HashMap<String, Object>> chkBandList = new ArrayList<HashMap<String,Object>>();
					
					boolean _useBandFlag = true;
					
					if( bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName().equals("")==false )
					{
						for (HashMap<String, Object> _bandlist : pageRowList) {
							if( _bandlist.containsKey("groupName") && _bandlist.get("groupName").toString().equals( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName() ) )
							{
								_useBandFlag = false;
								break;
							}
						}
					}
					else
					{
						_useBandFlag = false;
					}
					
					// header밴드와 data밴드의 합이 페이지의 표현 영역보다 클경우 헤더밴드를 제거한다.
					if( bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight()+ bandList.get(i).getHeight() > (currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight))  )
					{
						bandList.get(i).setHeaderBand("");
					}
					
					while( _dataCnt > 0 ){
						
						chkAdjustablePageFlag = false;
						_isFistNoShow = false;
						float _HeaderHeight = 0;
						
						if( (_useBandFlag || _startIndex > 0) && bandList.get(i).getHeaderBand().equals("") == false )
						{
							bandPageInfo = new HashMap<String, Object>();
							bandPageInfo.put("startIndex", 0);
							bandPageInfo.put("lastIndex",  1);
							bandPageInfo.put("y", currnetItemY);
							bandPageInfo.put("id",  bandList.get(i).getHeaderBand());
							bandPageInfo.put("gprCurrentPageNum", _grpPageCnt);
							
							if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName().equals("") == false )
							{
								bandPageInfo.put("groupName", bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName());
							}
							
							pageRowList.add(bandPageInfo);
							_dataHeaderHeight = bandList.get(i).getHeight();
							
							if( ( bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeight() || bandInfoData.get( bandList.get(i).getHeaderBand() ).getAutoTableHeight()) && bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().size() > 0 )
							{
								currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
							}
							else
							{
								currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
							}
							
							// tabIndex 담기
							if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getRequiredItems().size() > 0 )
							{
								_requiredItemList = getBandRequiredItems( Integer.valueOf( bandPageInfo.get("startIndex").toString()), Integer.valueOf(bandPageInfo.get("lastIndex").toString()) ,bandInfoData.get( bandList.get(i).getHeaderBand() ), _requiredItemList );
							}
							// 필수값 담기
							if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getTabIndexItem().size() > 0 )
							{
								_tabIndexItemList = getBandTabIndexItems( Integer.valueOf( bandPageInfo.get("startIndex").toString()), Integer.valueOf(bandPageInfo.get("lastIndex").toString()),bandInfoData.get( bandList.get(i).getHeaderBand() ), _tabIndexItemList );
							}
							
							chkBandList.add(bandPageInfo);
						}
						
						_dataBandHeight = 0;
						if( adjustableFlag  )
						{
							
							_rowHeightAr = bandList.get(i).getAdjustableHeightListAr();
							
							if( bandList.get(i).getClassName().equals(BandInfoMapData.DATA_BAND) && bandInfoData.get( bandList.get(i).getHeaderBand() ) != null )
							{
								if( ( bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeight() || bandInfoData.get( bandList.get(i).getHeaderBand() ).getAutoTableHeight()) && bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().size() > 0 )
								{
									_HeaderHeight = bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
								}
								else
								{
									_HeaderHeight = bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
								}
							}else
							{
								_HeaderHeight = 0;
							}
							
							if( defaultY + pageHeaderHeight + _HeaderHeight < currnetItemY &&  _lastCnt == 0 && ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY ) < _rowHeightAr.get(_lastCnt) && _dataCnt > 0 )
							{
								_isFistNoShow = true;
							}
							
							
							_maxIndex = 0;
							for ( j = _lastCnt; j < _rowHeightAr.size(); j++) {
								
								if( !( defaultY + pageHeaderHeight + _HeaderHeight == currnetItemY && _dataBandHeight == 0 ) && ( (currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY ) - (_dataBandHeight + _rowHeightAr.get(j)) < 0 )
								{
									break;
								}
								else
								{
									_maxIndex++;
									_lastCnt++;
									_dataBandHeight = _dataBandHeight + _rowHeightAr.get(j);
								}
							}
							
							if( _rowHeightAr.size() < _startIndex + _dataCnt && _lastCnt == _rowHeightAr.size() )
							{
								float _chkP = ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY ) - _dataBandHeight;
								if( _chkP/bandList.get(i).getHeight() > 1 )
								{
									_maxIndex = _maxIndex + (int) Math.floor( _chkP/bandList.get(i).getHeight() );
								}
								
								if( _maxIndex > _dataCnt)
								{
									_maxIndex = _dataCnt;
								}
								
								chkAdjustablePageFlag = true;
							}
							
						}
						else
						{
							_dataBandHeight = _dataCnt*bandList.get(i).getHeight();
							_maxIndex = (int) Math.floor( ( (currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY)/bandList.get(i).getHeight() ); 
						}
						boolean chkNextPageFlag = false;
						
						if(_maxIndex < 0 ) _maxIndex = 1;
						
						if(  _dataCnt - _maxIndex <= 0 && ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY) - _dataBandHeight > 0 && _groupingFootHeight > ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY) - _dataBandHeight )
						{
							chkNextPageFlag = true;
						}
						
						if( _maxIndex == _dataCnt && adjustableFlag && grpAutoHeight )
						{
							chkAdjustablePageFlag = true;
						}
						
						// 마지막 페이지일 경우
						if( _dataCnt - _maxIndex <= 0 || chkNextPageFlag || chkAdjustablePageFlag )
						{
							if( grpAutoHeight &&  ( bandList.get(i).getGroupBand()==null || bandList.get(i).getGroupBand().equals("") ) &&  _isLastPage == false && chkAdjustablePageFlag == false && _dataCnt - _maxIndex == 0 && _dataPageFooterHeight == 0 )
							{
								float _headerHeight = 0;
								if( bandList.get(i).getHeaderBand() != null && bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.containsKey( bandList.get(i).getHeaderBand() ) )
								{
									_headerHeight = bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
								}
								_groupingNextPageH = currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight) - _headerHeight - _groupingFootHeight;
								_dataCnt = _dataCnt + (int) Math.floor( _groupingNextPageH/bandList.get(i).getHeight() ); 
								_isLastPage  = true;
							}
							else
							{
								// autoHeight값 처리( groupBand일경우에는 groupFooter를 만나는 부분까지의 모든 Height값을 구해서 마지막 페이지의 인덱스를 구하기 )
								if( grpAutoHeight  )
								{
									if( grpAutoHeight )
									{
										if( adjustableFlag )
										{
											_groupMaxRowCnt = _maxIndex + (int) Math.floor( (currentPageMaxHeight - ( _dataBandHeight + _dataPageFooterHeight + currnetItemY + _groupingFootHeight ))/ bandList.get(i).getHeight() ) ;
										}
										else
										{
											_groupMaxRowCnt = (int) Math.floor( ( currentPageMaxHeight - _dataPageFooterHeight - currnetItemY - _groupingFootHeight )/ bandList.get(i).getHeight() );
										}
										
										
										if( _groupMaxRowCnt < _dataCnt )
										{
											//현재 페이지에 푸터밴드를 그릴수 없을경우 데이터 카운트를 다음페이지의 푸터 만큼 빠진 영역으로 인덱스를 늘린다 
											_dataCnt = _maxIndex;
											if(bandList.get(i).getHeaderBand().equals("") == false )
											{
												if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeight() && bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().size() > 0 )
												{
													_groupingNextPageH = currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight) - bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
												}
												else
												{
													_groupingNextPageH = currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight) - bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
												}
											}
											else
											{
												_groupingNextPageH = currentPageMaxHeight - _dataPageFooterHeight - (defaultY + pageHeaderHeight);
											}
											
											_groupingNextPageH = _groupingNextPageH - _groupingFootHeight;
											
											_dataCnt = _dataCnt + (int) Math.floor( _groupingNextPageH/bandList.get(i).getHeight() ); 
										}
										else
										{
											_maxIndex = _groupMaxRowCnt;
										}
										
										grpAutoHeight = false;
										
									}
										
								}
								else
								{
									_maxIndex = _dataCnt;
								}
								
								
							}
							
						}
						
						
						for ( j = 0; j < bandList.get(i).getSequence().size(); j++) {
							
							if(bandList.get(i).getSequence().get(j).equals("s"))
							{
								bandPageInfo = new HashMap<String, Object>();
								bandPageInfo.put("startIndex",0);
								bandPageInfo.put("lastIndex",  1);

								bandPageInfo.put("summeryStartIndex", _startIndex);
								bandPageInfo.put("summeryEndIndex",  _startIndex + _maxIndex);
								
								bandPageInfo.put("y", currnetItemY);
								bandPageInfo.put("id",  bandList.get(i).getSummery());
								bandPageInfo.put("gprCurrentPageNum", _grpPageCnt);
								pageRowList.add(bandPageInfo);
								
								_dataPageFooterHeight = bandInfoData.get( bandList.get(i).getSummery() ).getHeight();
								currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getSummery() ).getHeight();
								
								chkBandList.add(bandPageInfo);
							}
							else 
							{
								bandPageInfo = new HashMap<String, Object>();
								bandPageInfo.put("startIndex", _startIndex);
								bandPageInfo.put("lastIndex",   _startIndex + _maxIndex);
								bandPageInfo.put("y", currnetItemY);
								bandPageInfo.put("id",  bandList.get(i).getId());
								// group Page 함수사용을 위하여 그룹별 groupCurrentPage값을 담기
								bandPageInfo.put("gprCurrentPageNum", _grpPageCnt);
								pageRowList.add(bandPageInfo);
								
								chkBandList.add(bandPageInfo);
								
								if( adjustableFlag )
								{
									if( (_startIndex+_maxIndex) > _rowHeightAr.size() )
									{
										_dataBandHeight = _dataBandHeight + ((_startIndex+_maxIndex)-_rowHeightAr.size() )* bandList.get(i).getHeight();
									}
									currnetItemY = currnetItemY + _dataBandHeight;
								}
								else 
								{
									currnetItemY = currnetItemY + (_maxIndex*bandList.get(i).getHeight());
								}
							}
							
							// tabIndex 담기
							if( bandList.get(i).getRequiredItems().size() > 0 )
							{
								_requiredItemList = getBandRequiredItems( Integer.valueOf(bandPageInfo.get("startIndex").toString()),Integer.valueOf( bandPageInfo.get("lastIndex").toString()) ,bandList.get(i), _requiredItemList );
							}
							// 필수값 담기
							// 데이터 밴드의 경우 데이터 사이즈만큼만 처리 할지 autoHeight로 늘어난 row만큼 할지 처리
							if( bandList.get(i).getTabIndexItem().size() > 0 )
							{
								int _lastSize = Integer.valueOf(bandPageInfo.get("lastIndex").toString());
								if(DataSet.get(bandList.get(i).getDataSet() ).size() < _lastSize )
								{
									_lastSize = DataSet.get(bandList.get(i).getDataSet() ).size();
								}
								_tabIndexItemList = getBandTabIndexItems( Integer.valueOf(bandPageInfo.get("startIndex").toString()), _lastSize ,bandList.get(i), _tabIndexItemList );
							}
							
						}
						
						if(_maxIndex == 0 && _isFistNoShow == false ) _maxIndex = 1;
						
						currentPageMaxHeight = maxHeight;
						_dataCnt = _dataCnt - _maxIndex;
						_startIndex = _startIndex + _maxIndex;
						
						// 원본 PAGE_INFO_TIDX 값을 이용하여 데이터밴드의 아이템을 실제 사용할 ID를 생성하여 페이지별로 담기
						
						if( _dataCnt > 0 )
						{
							pagesRowList.add(pageRowList);
							pageRowList = new ArrayList< HashMap<String,Object>>();
							currnetItemY = defaultY + pageHeaderHeight;
							
							if( OriginalTabIndexItemList.size() > 0 )
							{
								// tabIndex 담긴 배열 처리
								_tabIndexItemListPages.add(_tabIndexItemList);
								_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
							}
							if( OriginalRequiredItemList.size() > 0 )
							{
								// 필수 목록 처리
								_requiredItemListPages.add(_requiredItemList);
								_requiredItemList = new ArrayList<HashMap<String, Object>>();
							}
						}
						
						_grpPageCnt++;
					}
					
					// group Page 함수사용을 위하여 그룹별 groupTotalPage값을 담기
					if( chkBandList.size() > 0 )
					{
						for ( j = 0; j <chkBandList.size(); j++) {
							chkBandList.get(j).put("gprTotalPageNum", _grpPageCnt );
						}
					}
					
				}
				else if( bandClassName.equals( BandInfoMapData.CROSSTAB_BAND )  )
				{
					CrossTabBandParser mcrossTabBand = new CrossTabBandParser();
					mcrossTabBand.setFunctionVersion(mFunction.getFunctionVersion());
					
					if( currentPageMaxHeight - currnetItemY < bandList.get(i).getHeight() )
					{
						pagesRowList.add(pageRowList);
						pageRowList = new ArrayList< HashMap<String,Object>>();
						currnetItemY = defaultY + pageHeaderHeight;
						
						if( OriginalTabIndexItemList.size() > 0 )
						{
							// tabIndex 담긴 배열 처리
							_tabIndexItemListPages.add(_tabIndexItemList);
							_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
						}
						if( OriginalRequiredItemList.size() > 0 )
						{
							// 필수 목록 처리
							_requiredItemListPages.add(_requiredItemList);
							_requiredItemList = new ArrayList<HashMap<String, Object>>();
						}
					}
					
					mcrossTabBand.setExportData(isExportData);
					
					if( crossTabData == null )
					{
							crossTabData = new HashMap<String, ArrayList<ArrayList<HashMap<String,Object>>>>();
					}
					
					ArrayList<ArrayList<HashMap<String, Object>>> crossTabPages = null; 
					
					// js SIMPLE 처리 필요 
					//crossTabPages = mcrossTabBand.convertCrossTabObjecttoItem( bandList.get(i).getOriginalItemData(),  DataSet, pageWidth, defaultY + pageHeaderHeight, maxHeight, currnetItemY, mXAr );
					
					if( crossTabPages == null ) continue;
					
					if( bandList.get(i).getVisibleType().equals(BandInfoMapData.VISIBLE_TYPE_ALL) && mcrossTabBand.getMaxPageWidth() > bandList.get(i).getWidth() )
					{
						bandList.get(i).setWidth(mcrossTabBand.getMaxPageWidth());
					}
					
					crossTabData.put(bandList.get(i).getId(), crossTabPages );
					
					// 리턴받은 크로스탭 데이터를 이용하여 총 페이지에 맞춰서 크로스탭정보를 지정
					int _crossTabPageSize = crossTabPages.size();
					float _crossTabMaxWidth = 0;
						
					for ( j = 0; j < _crossTabPageSize; j++) {
						
						if( j > 0 )
						{
							pagesRowList.add(pageRowList);
							pageRowList = new ArrayList< HashMap<String,Object>>();
							currnetItemY = defaultY + pageHeaderHeight;
						}
						
						bandPageInfo = new HashMap<String, Object>();
						bandPageInfo.put("startIndex", j );
						bandPageInfo.put("lastIndex", j+1 );
						bandPageInfo.put("y", currnetItemY);
						bandPageInfo.put("id", bandList.get(i).getId());
						bandPageInfo.put("crossTabStartIndex", pagesRowList.size()-j);
						pageRowList.add(bandPageInfo);
						
						if(mcrossTabBand.getMaxPageWidthAr().size() > 0 ) _crossTabMaxWidth = mcrossTabBand.getMaxPageWidthAr().get( j%mcrossTabBand.getMaxPageWidthAr().size() );
						
						if( bandList.get(i).getVisibleType().equals(BandInfoMapData.VISIBLE_TYPE_ALL) && _crossTabMaxWidth > bandList.get(i).getWidth() )
						{
							bandPageInfo.put("MAX_PAGE_WIDTH", _crossTabMaxWidth);
						}
						else
						{
							bandPageInfo.put("MAX_PAGE_WIDTH",  bandList.get(i).getWidth());
						}
						
						currnetItemY = Float.valueOf( crossTabPages.get(j).get( crossTabPages.get(j).size()-1 ).get("y").toString() ) + Float.valueOf( crossTabPages.get(j).get( crossTabPages.get(j).size()-1 ).get("height").toString() );
					}
				}
				else
				{
					// 
					boolean _useBandFlag = true;
					
					if( bandList.get(i).getClassName().equals("UBDataHeaderBand") && bandList.get(i).getUseHeaderBandGroupName().equals("")==false )
					{
						for (HashMap<String, Object> _bandlist : pageRowList) {
							if( _bandlist.containsKey("groupName") && _bandlist.get("groupName").toString().equals( bandList.get(i).getUseHeaderBandGroupName() ) )
							{
								_useBandFlag = false;
								break;
							}
						}
					}
					
					if( bandList.get(i).getClassName().equals("UBGroupHeaderBand") && bandList.get(i).getGroupName().equals("") == false && 
							currnetItemY != defaultY + pageHeaderHeight && firstBand == false && bandList.get(i).getNewPage()  )
					{
						pagesRowList.add(pageRowList);
						pageRowList = new ArrayList< HashMap<String,Object>>();
						currnetItemY = defaultY + pageHeaderHeight;
						
						if( OriginalTabIndexItemList.size() > 0 )
						{
							// tabIndex 담긴 배열 처리
							_tabIndexItemListPages.add(_tabIndexItemList);
							_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
						}
						if( OriginalRequiredItemList.size() > 0 )
						{
							// 필수 목록 처리
							_requiredItemListPages.add(_requiredItemList);
							_requiredItemList = new ArrayList<HashMap<String, Object>>();
						}
						
					}
					else if(_useBandFlag && currentPageMaxHeight - currnetItemY < bandList.get(i).getHeight() )
					{
						if(pageRowList.size() > 0 )
						{
							pagesRowList.add(pageRowList);
							pageRowList = new ArrayList< HashMap<String,Object>>();
							currnetItemY = defaultY + pageHeaderHeight;
							
							if( OriginalTabIndexItemList.size() > 0 )
							{
								// tabIndex 담긴 배열 처리
								_tabIndexItemListPages.add(_tabIndexItemList);
								_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
							}
							if( OriginalRequiredItemList.size() > 0 )
							{
								// 필수 목록 처리
								_requiredItemListPages.add(_requiredItemList);
								_requiredItemList = new ArrayList<HashMap<String, Object>>();
							}
						}
						
						///@TEST
						if( bandList.get(i).getHeaderBand().equals("") == false && bandInfoData.containsKey(bandList.get(i).getHeaderBand()) )
						{
							boolean _isChk = true;
							for (HashMap<String, Object> _bandlist : pageRowList) {
								if( _bandlist.containsKey("groupName") && _bandlist.get("groupName").toString().equals( bandInfoData.get(bandList.get(i).getHeaderBand()).getUseHeaderBandGroupName() ) )
								{
									_isChk = false;
									break;
								}
							}
							
							if( _isChk )
							{
								
								if( (_useBandFlag || _startIndex > 0) && bandList.get(i).getHeaderBand().equals("") == false )
								{
									bandPageInfo = new HashMap<String, Object>();
									bandPageInfo.put("startIndex", 0);
									bandPageInfo.put("lastIndex",  1);
									bandPageInfo.put("y", currnetItemY);
									bandPageInfo.put("id", bandList.get(i).getHeaderBand());
									
									if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName().equals("") == false )
									{
										bandPageInfo.put("groupName",  bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName());
									}
									
									pageRowList.add(bandPageInfo);
									if( ( bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeight() || bandInfoData.get( bandList.get(i).getHeaderBand() ).getAutoTableHeight()) && bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().size() > 0 )
									{
										currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
									}
									else
									{
										currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
									}
									// tabIndex 담기
									if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getRequiredItems().size() > 0 )
									{
										_requiredItemList = getBandRequiredItems( Integer.valueOf( bandPageInfo.get("startIndex").toString() ) , Integer.valueOf( bandPageInfo.get("lastIndex").toString() ) ,bandInfoData.get( bandList.get(i).getHeaderBand() ), _requiredItemList );
									}
									// 필수값 담기
									if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getTabIndexItem().size() > 0 )
									{
										_tabIndexItemList = getBandTabIndexItems( Integer.valueOf( bandPageInfo.get("startIndex").toString() ), Integer.valueOf( bandPageInfo.get("lastIndex").toString() ),bandInfoData.get( bandList.get(i).getHeaderBand() ), _tabIndexItemList );
									}
									
								}
								
							}
						}
						
						
					}
					else if(_useBandFlag && bandList.get(i).getClassName().equals("UBDataHeaderBand") )
					{
						
						String _argoGroupName = "";
						if( bandList.get(i).getGroupBand().equals("") == false )
						{
							
						}
						
						float dataBandHeight = 0;
						float headerHeight = 0;
						if( bandList.get(i).getDataBand().equals("") == false )
						{
							if( ( bandInfoData.get( bandList.get(i).getDataBand() ).getAdjustableHeight() || bandInfoData.get( bandList.get(i).getDataBand() ).getAutoTableHeight() ) && bandInfoData.get( bandList.get(i).getDataBand() ).getResizeText() == false &&
									bandInfoData.get( bandList.get(i).getDataBand() ).getAdjustableHeightListAr().size() > 0 )
							{
								dataBandHeight = bandInfoData.get( bandList.get(i).getDataBand() ).getAdjustableHeightListAr().get(0);
							}
							else
							{
								dataBandHeight = bandInfoData.get( bandList.get(i).getDataBand() ).getHeight();
							}
							
							if( bandInfoData.get( bandList.get(i).getDataBand() ).getSummery().equals("") == false )
							{
								dataBandHeight = dataBandHeight + bandInfoData.get(  bandInfoData.get( bandList.get(i).getDataBand() ).getSummery() ).getHeight();
							}
							
						}
						if( (bandList.get(i).getAdjustableHeight() || bandList.get(i).getAutoTableHeight()) && bandList.get(i).getAdjustableHeightListAr().size() > 0 )
						{
							headerHeight = bandList.get(i).getAdjustableHeightListAr().get(0);
						}
						else
						{
							headerHeight = bandList.get(i).getHeight();
						}
						
						if( defaultY + pageHeaderHeight < currnetItemY && currentPageMaxHeight - currnetItemY < headerHeight + dataBandHeight )
						{
							pagesRowList.add(pageRowList);
							pageRowList = new ArrayList< HashMap<String,Object>>();
							currnetItemY = defaultY + pageHeaderHeight;
							
							if( OriginalTabIndexItemList.size() > 0 )
							{
								// tabIndex 담긴 배열 처리
								_tabIndexItemListPages.add(_tabIndexItemList);
								_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
							}
							if( OriginalRequiredItemList.size() > 0 )
							{
								// 필수 목록 처리
								_requiredItemListPages.add(_requiredItemList);
								_requiredItemList = new ArrayList<HashMap<String, Object>>();
							}
						}
						
						
					}
					
					// 헤더의 adjustableHeight값 업데이트
					if(_useBandFlag)
					{
						bandPageInfo = new HashMap<String, Object>();
						bandPageInfo.put("startIndex", 0 );
						bandPageInfo.put("lastIndex", 1 );
						bandPageInfo.put("y", currnetItemY);
						bandPageInfo.put("id", bandList.get(i).getId());
						
						if( bandList.get(i).getClassName().equals("UBDataHeaderBand") && bandList.get(i).getUseHeaderBandGroupName().equals("") == false )
						{
							bandPageInfo.put("groupName", bandList.get(i).getUseHeaderBandGroupName());
						}
						
						pageRowList.add(bandPageInfo);
						
						if( (bandList.get(i).getAdjustableHeight() || bandList.get(i).getAutoTableHeight()) && bandList.get(i).getAdjustableHeightListAr().size() > 0 )
						{
							currnetItemY = currnetItemY + bandList.get(i).getAdjustableHeightListAr().get(0);
						}
						else
						{
							currnetItemY = currnetItemY + bandList.get(i).getHeight();
						}
						
						// tabIndex 담기
						if( bandList.get(i).getRequiredItems().size() > 0 )
						{
							_requiredItemList = getBandRequiredItems( Integer.valueOf( bandPageInfo.get("startIndex").toString() ) , Integer.valueOf( bandPageInfo.get("lastIndex").toString() ) ,bandList.get(i), _requiredItemList );
						}
						// 필수값 담기
						if( bandList.get(i).getTabIndexItem().size() > 0 )
						{
							_tabIndexItemList = getBandTabIndexItems(  Integer.valueOf( bandPageInfo.get("startIndex").toString() ) , Integer.valueOf( bandPageInfo.get("lastIndex").toString() ),bandList.get(i), _tabIndexItemList );
						}
						
					}
				}
				
				// 현재 Band가 groupBand인지 여부를 체크 그룹밴드일경우 이전 GroupBand와 다를경우 DataSet을 담아두고 기존에 담겨있던 밴드들의 GroupBandIdx와 GroupTotalPageCnt를 담아둔다. 
				if( bandList.get(i).getGroupBand().equals("") == false)
				{
					if(_currentGroupDSName.equals( bandList.get(i).getDataSet() ) == false )
					{
//						int _TotSize = ( pagesRowList.size() ) - _groupStartIdx;
						if( _groupTotalCnt == 0 ) _groupTotalCnt = 1;
						int _grpMaxSize = _groupListAr.size();
						for (int k = 0; k < _grpMaxSize; k++) {
							bandInfoData.get(_groupListAr.get(k)).setGroupStartPageIdx( _groupStartIdx );
							bandInfoData.get(_groupListAr.get(k)).setGroupTotalPageCnt( _groupTotalCnt);
						}
						
						if(_groupListAr.size() > 0)
						{
							for (int k = _groupStartIdx; k < _groupTotalCnt+_groupStartIdx; k++) {
								_grpStartPageMap.put(k, k - _groupStartIdx );
								_grpTotalPageMap.put(k, _groupTotalCnt );
							}
						}
						_currentGroupDSName = bandList.get(i).getDataSet();
						_groupStartIdx = pagesRowList.size();
						
						_groupTotalCnt = 0;
						_groupListAr.clear();
					}
					else
					{
						_groupTotalCnt = ( pagesRowList.size() +1 ) - _groupStartIdx;
					}

					_groupListAr.add( bandID );
					
				}
				else if( _currentGroupDSName.equals("") == false )
				{
//					int _TotSize = (pagesRowList.size() + _groupPageNewPage ) - _groupStartIdx;
					if( _groupTotalCnt == 0 ) _groupTotalCnt = 1;
					int _grpMaxSize = _groupListAr.size();
					for (int k = 0; k < _grpMaxSize; k++) {
						bandInfoData.get(_groupListAr.get(k)).setGroupStartPageIdx( _groupStartIdx );
						bandInfoData.get(_groupListAr.get(k)).setGroupTotalPageCnt(_groupTotalCnt );
					}
					
					if(_groupListAr.size() > 0)
					{
						for (int k = _groupStartIdx; k < _groupTotalCnt+_groupStartIdx; k++) {
							_grpStartPageMap.put(k, k - _groupStartIdx );
							_grpTotalPageMap.put(k, _groupTotalCnt );
						}
					}
					_groupListAr.clear();
					_groupTotalCnt = 0;
					_currentGroupDSName = "";
				}
				
				
				
				firstBand = false;
			}
			
		}
		
		if( pageRowList.size() > 0 )
		{
			pagesRowList.add(pageRowList);
			
			if( OriginalTabIndexItemList.size() > 0 )
			{
				// tabIndex 담긴 배열 처리
				_tabIndexItemListPages.add(_tabIndexItemList);
			}
			if( OriginalRequiredItemList.size() > 0 )
			{
				// 필수 목록 처리
				_requiredItemListPages.add(_requiredItemList);
			}
			
		}
		
		// 현재 Band가 groupBand인지 여부를 체크 그룹밴드일경우 이전 GroupBand와 다를경우 DataSet을 담아두고 기존에 담겨있던 밴드들의 GroupBandIdx와 GroupTotalPageCnt를 담아둔다. 
		if( _currentGroupDSName.equals("") == false )
		{
			int _TotSize =  pagesRowList.size() - _groupStartIdx;
			int _grpMaxSize = _groupListAr.size();
			for (int k = 0; k < _grpMaxSize; k++) {
				bandInfoData.get(_groupListAr.get(k)).setGroupStartPageIdx( _groupStartIdx );
				bandInfoData.get(_groupListAr.get(k)).setGroupTotalPageCnt( (pagesRowList.size()) - _groupStartIdx );
			}
			
			if(_groupListAr.size() > 0)
			{
				for (int k = _groupStartIdx; k < _TotSize+_groupStartIdx; k++) {
					_grpStartPageMap.put(k, k - _groupStartIdx );
					_grpTotalPageMap.put(k, _TotSize );
				}
			}
			
			_currentGroupDSName = "";
		}
		
		
		
		if( pagesRowList.size() == 0 && ( pageHeaderList.size() > 0 || pageFooterList.size() > 0 )  )
		{
			pageRowList = new ArrayList<HashMap<String,Object>>();
			pagesRowList.add(pageRowList);
			

			if( OriginalTabIndexItemList.size() > 0 )
			{
				// tabIndex 담긴 배열 처리
				_tabIndexItemListPages.add(_tabIndexItemList);
			}
			if( OriginalRequiredItemList.size() > 0 )
			{
				// 필수 목록 처리
				_requiredItemListPages.add(_requiredItemList);
			}
		}
		
		int maxCnt = 1;
		
		if( pageHeaderRepeat )
		{
			maxCnt = pagesRowList.size();
		}
		// 모드 밴드 완료후 페이지 헤더와 페이지 푸터를 각 페이지별로 Add
		float itemH = 0f;		//헤더가 2개 이상일경우 헤더별 Y값을 담기위하여 지정
		float _firstPageItemH = 0f;
		for ( i = 0; i < pageHeaderList.size(); i++) {
			
			for ( j = 0; j < maxCnt; j++) {
				
				if( j == 0 ||  bandInfoData.get(pageHeaderList.get(i)).getRepeat().equals("y") )
				{
					float _chkY = _firstPageItemH;
					if( j == 0 )
					{
						_chkY = _firstPageItemH;
					}
					else
					{
						_chkY = itemH;
					}
					
					bandPageInfo = new HashMap<String, Object>();
					bandPageInfo.put("startIndex", 0 );
					bandPageInfo.put("lastIndex", 1 );
					bandPageInfo.put("y",  _chkY);
					bandPageInfo.put("id", pageHeaderList.get(i));
					
					if( _grpStartPageMap.containsKey(j) ) bandPageInfo.put("gprCurrentPageNum", _grpStartPageMap.get(j) );
					if( _grpTotalPageMap.containsKey(j) ) bandPageInfo.put("gprTotalPageNum",  _grpTotalPageMap.get(j) );
					
					pagesRowList.get(j).add(i, bandPageInfo);
					
					// tabIndex 담기
					if( bandList.get(i).getRequiredItems().size() > 0 )
					{
						getBandRequiredItems( Integer.valueOf(bandPageInfo.get("startIndex").toString() ),  Integer.valueOf(bandPageInfo.get("lastIndex").toString() ) ,bandInfoData.get(pageHeaderList.get(i)), _requiredItemListPages.get(j) );
					}
					// 필수값 담기
					if( bandList.get(i).getTabIndexItem().size() > 0 )
					{
						getBandTabIndexItems( Integer.valueOf(bandPageInfo.get("startIndex").toString() ),  Integer.valueOf(bandPageInfo.get("lastIndex").toString() ) ,bandInfoData.get(pageHeaderList.get(i)), _tabIndexItemListPages.get(j) );
					}
				}
				else
				{
					break;
				}
			}
			
			if(bandInfoData.get(pageHeaderList.get(i)).getRepeat().equals("y") )
			{
				itemH = itemH + bandInfoData.get(pageHeaderList.get(i)).getHeight();			
			}
			_firstPageItemH = _firstPageItemH + bandInfoData.get(pageHeaderList.get(i)).getHeight();
		}		
		
		maxCnt = 1;
		maxCnt = pagesRowList.size();
		
		float _lastPageFHeight = defaultHeight;
		float _pageFHeight = defaultHeight;
		float _firstFHeight = defaultHeight;
		for ( i = 0; i < pageFooterList.size(); i++) {
			
			if( bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
			{
				_pageFHeight 		= _pageFHeight - bandInfoData.get(pageFooterList.get(i)).getHeight();
				_lastPageFHeight	= _lastPageFHeight -  bandInfoData.get(pageFooterList.get(i)).getHeight();
				_firstFHeight 		= _firstFHeight - bandInfoData.get(pageFooterList.get(i)).getHeight();
			}
			else
			{
				if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST )
				{
					_lastPageFHeight = _lastPageFHeight -  bandInfoData.get(pageFooterList.get(i)).getHeight();
					
				}
				else
				{
					_firstFHeight = _firstFHeight - bandInfoData.get(pageFooterList.get(i)).getHeight();
				}
			}
		}
		
		for ( i = 0; i < pageFooterList.size(); i++) {
			
			int _stIdx = 0;
			if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST && !bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
			{
				_stIdx = maxCnt-1;
				
				if( _stIdx == 0 ) _firstFHeight = _lastPageFHeight;
			}
			
			for ( j = _stIdx ; j < maxCnt; j++) {
				
				if( ( !GlobalVariableData.GROUP_FOOTER_POSITION_LAST && j == 0 ) || (GlobalVariableData.GROUP_FOOTER_POSITION_LAST  && j == maxCnt-1) ||  bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
				{
					if( j == 0 )itemH =	_firstFHeight; 
					else if( j == maxCnt-1 )itemH =	_lastPageFHeight; 
					else itemH =	_pageFHeight; 
					
					bandPageInfo = new HashMap<String, Object>();
					bandPageInfo.put("startIndex", 0 );
					bandPageInfo.put("lastIndex", 1 );
					bandPageInfo.put("y",  itemH);
					bandPageInfo.put("id", pageFooterList.get(i));
					
					if( _grpStartPageMap.containsKey(j) ) bandPageInfo.put("gprCurrentPageNum", _grpStartPageMap.get(j) );
					if( _grpTotalPageMap.containsKey(j) ) bandPageInfo.put("gprTotalPageNum",  _grpTotalPageMap.get(j) );

					pagesRowList.get(j).add( bandPageInfo );
					
					// tabIndex 담기
					if( bandList.get(i).getRequiredItems().size() > 0 )
					{
						getBandRequiredItems( Integer.valueOf(bandPageInfo.get("startIndex").toString()),Integer.valueOf(bandPageInfo.get("lastIndex").toString()) ,bandInfoData.get(pageFooterList.get(i)), _requiredItemListPages.get(j) );
					}
					// 필수값 담기
					if( bandList.get(i).getTabIndexItem().size() > 0 )
					{
						getBandTabIndexItems( Integer.valueOf(bandPageInfo.get("startIndex").toString()),Integer.valueOf(bandPageInfo.get("lastIndex").toString()) ,bandInfoData.get(pageFooterList.get(i)), _tabIndexItemListPages.get(j) );
					}
				}
				else
				{
					break;
				}
			}
			
			if( bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
			{
				_pageFHeight 		= _pageFHeight + bandInfoData.get(pageFooterList.get(i)).getHeight();
				_lastPageFHeight	= _lastPageFHeight +  bandInfoData.get(pageFooterList.get(i)).getHeight();
				_firstFHeight 		= _firstFHeight + bandInfoData.get(pageFooterList.get(i)).getHeight();
			}
			else
			{
				if( GlobalVariableData.GROUP_FOOTER_POSITION_LAST )
				{
					_lastPageFHeight = _lastPageFHeight +  bandInfoData.get(pageFooterList.get(i)).getHeight();
					
				}
				else
				{
					_firstFHeight = _firstFHeight + bandInfoData.get(pageFooterList.get(i)).getHeight();
				}
			}
			
		}	
		
		ArrayList<Object> returnArrayList = new ArrayList<Object>();
		
		returnArrayList.add(pagesRowList);
		returnArrayList.add(crossTabData);

		returnArrayList.add(_requiredItemListPages);
		returnArrayList.add(_tabIndexItemListPages);
		
//		return pagesRowList;
		return returnArrayList;
	}

	
	
	//
	public ArrayList<HashMap<String, Object>> createContinueBandItems( int _page, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, BandInfoMapData> bandInfo, ArrayList<BandInfoMapData> bandList, 
			ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList , HashMap<String, Object> _param, HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum, boolean isPivot) throws UnsupportedEncodingException, ScriptException
	{
		
		int i = 0;
		int j = 0;
		
//		ArrayList<HashMap<String, Object>> _objects = new ArrayList<HashMap<String, Object>>();
		
		// page Width 담아두기
		// crossTab maxPageWidth 값에 따라서 pageWidth 변경 
		// pageWidth
		
		// band max Width 초기화
		mBandMaxWidth = 0;
		
		String bandName = "";
		HashMap<String, Value> currentBandCntMap;
		HashMap<String, Value> currentItemData;
		String _itemId = "";
		String _className = "";
				
		String _dataSetName = "";
		
		float itemY = 0f;
		int _currRowIndex = 0;
		int _maxRowIndex = 0;
		ArrayList<HashMap<String, Value>> _child = null;
		String crossTabClassName = "UBLabelBorder";
		Boolean crossTabItems = false;
		
		DataSet = _dataSet;
		mParam = _param;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> repeatValueCheckMap = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		
		// dataConvertParser 생성
		ItemConvertParser dataItemParser = new ItemConvertParser(DataSet, mChartDataFileName, m_appParams);
		dataItemParser.setImageData(mImageData);
		dataItemParser.setChartData(mChartData);
		dataItemParser.setFunction(mFunction);
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		
		dataItemParser.setChangeItemList(changeItemList);
		
		// Group함수를 위한 객체
		dataItemParser.mOriginalDataMap = mOriginalDataMap;
		dataItemParser.mGroupDataNamesAr = mGroupDataNamesAr;
		
		dataItemParser.setIsExportType(isExportType);
		int _crossTabStartIndex = 0;
		
		mBandMaxWidth = 0;
		
		_cloneY = _cloneY + mPageMarginTop;
		
		for ( i = 0; i < pagesRowList.get(_page).size(); i++) {
			
			currentBandCntMap = pagesRowList.get(_page).get(i);
			bandName = currentBandCntMap.get("id").getStringValue();
			_dataSetName = "";
			crossTabItems = false;
			_crossTabStartIndex = 0;
			
			repeatValueCheckMap = new HashMap<String, ArrayList<HashMap<String, Object>>>();
			
			//crossTab band일경우 visibleType가 all일때 band의 Width를 담는다
			if( bandInfo.get(bandName).getVisibleType().equals( BandInfoMapData.VISIBLE_TYPE_ALL ) )
			{
				if( currentBandCntMap.containsKey("MAX_PAGE_WIDTH") && mBandMaxWidth < currentBandCntMap.get("MAX_PAGE_WIDTH").getIntegerValue() )
				{
					mBandMaxWidth = currentBandCntMap.get("MAX_PAGE_WIDTH").getIntegerValue();
				}
//				if( mBandMaxWidth < bandInfo.get(bandName).getWidth() ) mBandMaxWidth = bandInfo.get(bandName).getWidth();
			}
			
			if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.CROSSTAB_BAND) )
			{
				if( crossTabData.containsKey(bandName) )
				{
					if(currentBandCntMap.containsKey("crossTabStartIndex"))
					{
						_crossTabStartIndex = currentBandCntMap.get("crossTabStartIndex").getIntegerValue().intValue();
					}
					_child = crossTabData.get(bandName).get(_page - _crossTabStartIndex);
					crossTabItems = true;
				}
			}
			else
			{
				if( bandInfo.get(bandName).getDefaultBand().equals(""))
				{
					_child = bandInfo.get(bandName).getChildren();
				}
				else
				{
					_child = bandInfo.get( bandInfo.get(bandName).getDefaultBand() ).getChildren();
					_dataSetName = bandInfo.get(bandName).getDataSet();
				}
			}
			
			
			_currRowIndex = (int) currentBandCntMap.get("startIndex").getIntValue();
			_maxRowIndex =  (int)  currentBandCntMap.get("lastIndex").getIntValue();
			
			// Group함수의 페이징함수를 위한 값을 담아두기
			if( currentBandCntMap.containsKey("gprCurrentPageNum") && currentBandCntMap.get("gprCurrentPageNum") != null )
			{
				dataItemParser.mGroupCurrentPageIndex = (int) currentBandCntMap.get("gprCurrentPageNum").getIntValue();
			}
			if( bandInfo.get(bandName).getGroupStartPageIdx() > -1 )
			{
				dataItemParser.mGroupCurrentPageIndex = _page - bandInfo.get(bandName).getGroupStartPageIdx();
			}
			
			if( currentBandCntMap.containsKey("gprTotalPageNum") && currentBandCntMap.get("gprTotalPageNum") != null )
			{
				dataItemParser.mGroupTotalPageIndex = (int) currentBandCntMap.get("gprTotalPageNum").getIntValue();
			}
			if( bandInfo.get(bandName).getGroupTotalPageCnt() > -1 )
			{
				dataItemParser.mGroupTotalPageIndex = bandInfo.get(bandName).getGroupTotalPageCnt();
			}
			
			// 밴드에 useLabelBand속성이 true일경우 수만큼 반복하도록 지정
			int _labelBandCnt = 1;	
			boolean _useLabelBand = bandInfo.get(bandName).getUseLabelBand();
			int _dataIdx = 0;
			float _addLabelPosition = 0;
			float _labelBandWidth = bandInfo.get(bandName).getWidth();
			
			if( _useLabelBand )
			{
				_labelBandCnt = bandInfo.get(bandName).getLabelBandColCount();
//				_labelBandWidth = _labelBandWidth - (bandInfo.get(bandName).getLabelBandPadding()*2);
			}
			
			int _maxDsSize = 0;
			
			// xml Item
			for( j = 0; j < _child.size() ; j++){
				
				for (int _cRowIndex = _currRowIndex; _cRowIndex < _maxRowIndex; _cRowIndex++) {
					
					// labelBand 반복된 수만큼 반복
					for (int _lbIdx = 0; _lbIdx < _labelBandCnt; _lbIdx++) {
						
						currentItemData = _child.get(j);
						
						currentItemData.put("currentAdjustableHeight", new Value(-1,"number"));
						if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
						{
							if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
							{
								currentItemData.put("currentAdjustableHeight", new Value( bandInfo.get(bandName).getAdjustableHeightListAr().get(_cRowIndex) - currentItemData.get("band_y").getIntegerValue() ,"number" ));
							}
						}
						
						_dataIdx = _cRowIndex;
						// 라벨밴드의 경우 하나의 밴드에 여러건의 데이터가 표현됨
						if(_useLabelBand){
							if( "horizontal".equals( bandInfo.get(bandName).getLabelBandDirection() ) )
							{
								_dataIdx = ( _cRowIndex * _labelBandCnt ) + _lbIdx ;
							}
							else
							{
								_dataIdx = (_currRowIndex*_labelBandCnt) +  (_cRowIndex-_currRowIndex) +  ( (_maxRowIndex-_currRowIndex) * _lbIdx);
							}
							
							
							float _displayWidth=bandInfo.get(bandName).getLabelBandDisplayWidth();
							if( _displayWidth != 0 ){
								_addLabelPosition = (float) Math.floor( ( _displayWidth / _labelBandCnt) * _lbIdx );
							}else{
								_addLabelPosition = (float) Math.floor( (_labelBandWidth / _labelBandCnt) * _lbIdx );
							}
							

							//_addLabelPosition = (float) Math.floor( (_labelBandWidth / _labelBandCnt) * _lbIdx );
							
//							_addLabelPosition = _addLabelPosition + bandInfo.get(bandName).getLabelBandPadding() + bandInfo.get(bandName).getLabelBandGap();
							
							if( BandInfoMapData.DATA_BAND.equals(bandInfo.get(bandName).getClassName()) &&  "".equals(bandInfo.get(bandName).getDataSet()) ==false && !bandInfo.get(bandName).getAutoHeight())
							{
								_maxDsSize = DataSet.get(bandInfo.get(bandName).getDataSet() ).size();
								
								// 데이터의 Length보다 클경우 그리지 않도록 지정되어있음
								if(bandInfo.get(bandName).getMinRowCount() > 1 && bandInfo.get(bandName).getMinRowCount()*_labelBandCnt > _maxDsSize )
								{
									_maxDsSize = bandInfo.get(bandName).getMinRowCount()*_labelBandCnt;
								}
								
								if( _maxDsSize <= _dataIdx )
								{
									continue;
								}
							}
						}
						
						
						if( currentItemData == null ) continue;
						if( currentItemData.containsKey("id") )_itemId = currentItemData.get("id").getStringValue();
						
						if( crossTabItems )
						{
							// crossTab아이템일경우 클래스명 변경
							_className = crossTabClassName;
							currentItemData.put("className", Value.fromString(crossTabClassName));
						}
						else
						{
							_className = currentItemData.get("className").getStringValue();
						}
						
						//테스트 
						HashMap<String, Object> _propList = new HashMap<String, Object>();
						
						// 
//						if(bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_BAND) )
						
						if( _className.equals("UBTemplateArea") && mTempletInfo != null && mTempletInfo.containsKey(_itemId) )
						{
							_objects = mTempletInfo.get(_itemId).convertItemData(_dataIdx, _cloneX, _cloneY + currentBandCntMap.get("y").getIntegerValue(), _objects, mFunction, bandInfo.get(bandName), _currentPageNum, _totalPageNum, -1, -1, null, currentBandCntMap, dataItemParser);
							continue;
						}
						else
						{
							if(bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_PAGE_FOOTER_BAND) )
							{
								int _summeryStartIndex = (int) currentBandCntMap.get("summeryStartIndex").getIntValue();
								int _summeryEndIndex = (int) currentBandCntMap.get("summeryEndIndex").getIntValue();
								
								if(currentItemData.containsKey("LOAD_TYPE") && currentItemData.get("LOAD_TYPE").getIntValue().equals(ProjectInfo.LOAD_TYPE_JSON) )
								{
									_propList = dataItemParser.convertItemDataJson(bandInfo.get(bandName), currentItemData, DataSet, _dataIdx, _param, _summeryStartIndex, _summeryEndIndex,_totalPageNum,_currentPageNum, _dataSetName,0, 0,  -1);
								}
								else
								{
									_propList = dataItemParser.convertItemData(bandInfo.get(bandName), currentItemData, _dataSetName, _dataIdx, _param, _summeryStartIndex, _summeryEndIndex,_totalPageNum,_currentPageNum);
								}
								
							}
							else
							{
								if(currentItemData.containsKey("LOAD_TYPE") && currentItemData.get("LOAD_TYPE").getIntValue().equals(ProjectInfo.LOAD_TYPE_JSON) )
								{
									_propList = dataItemParser.convertItemDataJson(bandInfo.get(bandName), currentItemData, DataSet, _dataIdx, _param, -1, -1 , _totalPageNum , _currentPageNum, _dataSetName, 0, 0,  -1);
								}
								else
								{
									_propList = dataItemParser.convertItemData(bandInfo.get(bandName), currentItemData, _dataSetName, _dataIdx, _param, -1, -1 , _totalPageNum , _currentPageNum);
								}
								if( isExportType.equals(GlobalVariableData.EXPORT_TYPE_TEXT))
								{
									if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_HEADER_BAND) )
									{
										_propList.put("BAND_NAME", bandName);
									}
									else if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_BAND) && bandInfo.containsKey( bandInfo.get(bandName).getHeaderBand() )  )
									{
										_propList.put("BAND_NAME", bandName);
										_propList.put("HEADER_BAND_NAME",  bandInfo.get(bandName).getHeaderBand() );
									}
								}
								
							}
						}
						
						// 아이템이 null일경우 add시키지 않음 2015-12-02
						if( _propList == null ) continue;
						
						if( crossTabItems == false  )
						{
							
							if(bandInfo.get(bandName).getAutoTableHeight()){
								float bandY = currentItemData.get("band_y").getIntegerValue();
								float currTableRowY = 0;
															
								//bandInfo.get(bandName).getTableRowHeight();	
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									itemY = currentBandCntMap.get("y").getIntegerValue() +  getCurrentYpositionBand(bandInfo.get(bandName).getAdjustableHeightListAr(), _cRowIndex, currentBandCntMap.get("startIndex").getIntValue() , bandInfo.get(bandName));
									
									if(!currentItemData.containsKey("isCell")){
										if(currentItemData.containsKey("tableId")){
											String tableId = currentItemData.get("tableId").getStringValue();
											int cellRowIndex = currentItemData.get("cellRowIndex").getIntValue();
											float cellPadding = 0;
											
											int _cellRowSpan = -1;
											float _cellRowHeight = 0;
											
											if( currentItemData.containsKey("cellRowSpan"))
											{
												_cellRowSpan = currentItemData.get("cellRowSpan").getIntValue();
												
												if(_cellRowSpan > 1){
													for(int ii = cellRowIndex; ii < cellRowIndex + _cellRowSpan; ii++ ){
														_cellRowHeight = _cellRowHeight +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(ii);			
													}										
												}else{
													_cellRowHeight = bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(cellRowIndex);
												}
												
												_cellRowHeight = _cellRowHeight / 2 - ( currentItemData.get("height").getIntegerValue() / 2 ) ;
												
											}
											
											if(bandInfo.get(bandName).getGroupBand().equals("") == false )
											{
												cellPadding = currentItemData.get("cellPadding").getIntegerValue() +  bandInfo.get( bandInfo.get(bandName).getDefaultBand() ).getTableBandY().get(tableId);									
											}
											else
											{
												cellPadding = currentItemData.get("cellPadding").getIntegerValue() +  bandInfo.get( bandName ).getTableBandY().get(tableId);
											}
											
											
											for(int ii = 0; ii < cellRowIndex; ii++ ){
												currTableRowY = currTableRowY +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(ii) + 1;			
											}											
											currTableRowY = currTableRowY + cellPadding + _cellRowHeight ;																				
											
										}else{
											currTableRowY = currentItemData.get("band_y").getIntegerValue();
										}
										
									}else{
										
										int currTableRowIdx = currentItemData.get("rowIndex").getIntValue();
										int currTableRowSpan = currentItemData.get("rowSpan").getIntValue();
										float currTableRowHeight = 0;								
										String orgTableId = currentItemData.get("ORIGINAL_TABLE_ID").getStringValue();								
										
										if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
										{
											//_propList.put("height", bandInfo.get(bandName).getAdjustableHeightListAr().get(_cRowIndex) - currentItemData.get("band_y").getIntegerValue() );
											//bandY = 																		
//											if(currTableRowIdx == 0){
//												currTableRowY = bandY;
//											}else{
											
											if(bandInfo.get(bandName).getGroupBand().equals("") == false )
											{
												currTableRowY =  bandInfo.get(bandInfo.get(bandName).getDefaultBand()).getTableBandY().get(orgTableId);
											}
											else
											{
												currTableRowY =  bandInfo.get(bandName).getTableBandY().get(orgTableId);
											}
											
											for(int ii = 0; ii < currTableRowIdx; ii++ ){
												currTableRowY = currTableRowY +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(ii);			
											}	
//											}
											
											if(currTableRowSpan > 1){
												for(int ii = currTableRowIdx; ii < currTableRowIdx + currTableRowSpan; ii++ ){
													currTableRowHeight = currTableRowHeight +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(ii);			
												}										
											}else{
												currTableRowHeight = bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(currTableRowIdx);
											}	
											_propList.put("height",currTableRowHeight);
											//_propList.put("tooltip"," H : " + bandInfo.get(bandName).getAdjustableHeightListAr().toString()  );
										}
										else
										{
											currTableRowY = currentItemData.get("y").getIntegerValue();
										}
									}
								}
								else
								{
									currTableRowY = currentItemData.get("band_y").getIntegerValue();								
									// 일반 아이템의 y좌표는 인덱스*밴드height+ 아이템의 band_y값을 이용해서 이동 ( resizedHeight값이 잇는 아이템은 height별로 따로 처리 )
									itemY = currentBandCntMap.get("y").getIntegerValue()+ ( bandInfo.get(currentBandCntMap.get("id").getStringValue()).getHeight() * ( _cRowIndex - currentBandCntMap.get("startIndex").getIntValue() ) );
								}
								
								itemY = itemY + currTableRowY;
							}else {
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									itemY = currentBandCntMap.get("y").getIntegerValue() +  getCurrentYpositionBand(bandInfo.get(bandName).getAdjustableHeightListAr(), _cRowIndex, currentBandCntMap.get("startIndex").getIntValue() , bandInfo.get(bandName));
									if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
									{
										_propList.put("height", bandInfo.get(bandName).getAdjustableHeightListAr().get(_cRowIndex) - currentItemData.get("band_y").getIntegerValue() );
										
										//_propList.put("tooltip"," H : " + bandInfo.get(bandName).getAdjustableHeightListAr().toString()  );
									}
								}
								else
								{
									// 일반 아이템의 y좌표는 인덱스*밴드height+ 아이템의 band_y값을 이용해서 이동 ( resizedHeight값이 잇는 아이템은 height별로 따로 처리 )
									itemY = currentBandCntMap.get("y").getIntegerValue()+ ( bandInfo.get(currentBandCntMap.get("id").getStringValue()).getHeight() * ( _cRowIndex - currentBandCntMap.get("startIndex").getIntValue() ) );
								}
							
								itemY = itemY + currentItemData.get("band_y").getIntegerValue();
							}							
							
							itemY = ((float) Math.round(  itemY*100) / 100);
							_propList.put("y", itemY + _cloneY );
							
							if( _propList.get("x") != null ){
								_propList.put("x", Float.valueOf(_propList.get("x").toString()) + _cloneX + _addLabelPosition );
							}
							
							if(_propList.containsKey("id") && _propList.get("id").equals("") == false )_itemId =  _propList.get("id").toString();
							
							_propList.put("className" , _className );
							_propList.put("id" , _itemId );
							
							if( currentItemData.containsKey("realClassName") && "TABLE".equals(currentItemData.get("realClassName").getStringValue() ) )
							{
								if( _itemId.equals("") )_propList.put("id", "TB_" + _cRowIndex + "_" + _itemId);
								
								// Export시 테이블로 내보내기 위한 작업
								_propList.put("isTable", "true" );
								_propList.put("TABLE_ID", bandName + "_" + currentItemData.get("realTableID").getStringValue() );	// 테이블아이템생성을 위한 밴드명+테이블 id를 담아둔다(2016-03-07)
								
								
								// 테이블이 가로반복된 경우 반복된 테이블끼리 합치지 않도록 아이디를 분할처리 
								if( _lbIdx > 0 && _propList.containsKey("TABLE_ID") )
								{
									_propList.put("TABLE_ID", _propList.get("TABLE_ID")+"_"+_lbIdx );
								}
								
								// 아이템의 Height값이 밴드보다 클경우 밴드의 사이즈와 동일하게 맞추도록 수정
								
//								float _cellBadnY = (  currentItemData.get("band_y").getIntegerValue() < 0 )? 0 :  currentItemData.get("band_y").getIntegerValue();
//								float _maxH = (bandInfo.get(currentBandCntMap.get("id").getStringValue()).getHeight() - _cellBadnY);
//								float _updateBadnY = (  currentItemData.get("band_y").getIntegerValue() < 0 )? currentItemData.get("band_y").getIntegerValue():0;
								
								// cell 의 over만큼 값을 담기
//								_propList.put("cellOverHeight", 0 - _updateBadnY );
//								_propList.put("cellOutHeight",(Float.valueOf(_propList.get("height").toString()) + _updateBadnY) - _maxH );
								_propList.put("cellHeight", _propList.get("height").toString());
//								_propList.put("cellY", itemY );
								_propList.put("cellY", _propList.get("y") ); // 명우;
								
								if( currentItemData.containsKey("cellOverHeight") )
								{
									_propList.put("cellOverHeight", currentItemData.get("cellOverHeight").getIntegerValue() );
								}
								if( currentItemData.containsKey("cellOutHeight") )
								{
									_propList.put("cellOutHeight", currentItemData.get("cellOutHeight").getIntegerValue() );
								}
								
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									_propList.put("cellOutHeight", 0 );
								}
								
//								if( Float.valueOf(_propList.get("height").toString()) < _maxH)
//								{
//									_propList.put("cellHeight", _propList.get("height").toString());
//									_propList.put("cellOverHeight", 0 - _updateBadnY );
//								}
//								else
//								{
//									if( bandInfo.get(bandName).getAdjustableHeightListAr().size() == 0 )
//									{
//										_propList.put("cellHeight", _maxH);
//										_propList.put("cellOverHeight", Float.valueOf(_propList.get("height").toString()) - _maxH );
//									}
//									else
//									{
//										_propList.put("cellHeight", _propList.get("height").toString());
//										_propList.put("cellOverHeight", 0 - _updateBadnY );
//									}
//									
//								}
								
								if( currentItemData.containsKey("repeatedValue") && currentItemData.get("repeatedValue").getBooleanValue() )
								{
									_propList.put( "repeatedValue", currentItemData.get("repeatedValue").getBooleanValue());
								}
								
								
//								_propList.put("cellY", itemY - _updateBadnY );
								
							}
							
							if( currentItemData.containsKey("repeatedValue") && currentItemData.get("repeatedValue").getBooleanValue() )
							{
								String chkDataID = "";
								if( _dataSetName.equals("") && !currentItemData.get("dataSet").getStringValue().equals("") && !currentItemData.get("dataSet").getStringValue().equals("null") )
								{
									chkDataID =  currentItemData.get("dataSet").getStringValue();
								}
								else
								{
									chkDataID = _dataSetName;
								}
								if( bandInfo.get(bandName).getDataSet().equals("") == false && DataSet.containsKey(chkDataID) == false )
								{
									chkDataID = bandInfo.get(bandName).getDataSet();
								}
								
								if( DataSet.containsKey(chkDataID) == false )
								{
									chkDataID = "";
								}
								
								if( ( currentItemData.get("dataType").getStringValue() != null && currentItemData.get("dataType").getStringValue().equals("0") ) || ( chkDataID.equals("") == false  && DataSet.containsKey(chkDataID) && DataSet.get(chkDataID).size() > _cRowIndex ) )
								{
									if( repeatValueCheckMap.containsKey(_itemId) == false ){ 
										repeatValueCheckMap.put(_itemId, new ArrayList<HashMap<String, Object>>());
									}
									
									HashMap<String, Object> repeatObj = new HashMap<String, Object>();
									repeatObj.put("item", _propList);
									repeatObj.put("datatext", getRepeatValueCheckStr(currentItemData, _propList.get("text").toString(), chkDataID, _cRowIndex)  );
									repeatValueCheckMap.get(_itemId).add(repeatObj);
								}
								
							}
							
							// isPivot값이 true일경우 
							if(isPivot)
							{
								float _argoX 		= Float.valueOf( String.valueOf(_propList.get("x")) );
								float _argoY 		= Float.valueOf( String.valueOf(_propList.get("y")) );
								float _argoWidth  	= Float.valueOf( String.valueOf(_propList.get("x2")) );
								float _argoHeight 	= Float.valueOf( String.valueOf(_propList.get("y2")) );
								
								_propList.put("x", _argoY);
								_propList.put("y", mPageWidth - _argoX - _argoWidth);
								_propList.put("width", _argoHeight);
								_propList.put("height", _argoWidth );
								
								
								if(_propList.containsKey("borderSide"))
								{
									//_propList의 border 업데이트( right : t->l,r->t,l->b,r->b ) 
									String borderSide = String.valueOf(_propList.get("borderSide"));
									borderSide = borderSide.replace("[", "").replace("]", "").replace(" ", ""); 
									
									String[] convertBorderSide = borderSide.split(",");
									ArrayList<String> newBorderSide = new ArrayList<String>();
									
									for (int k = 0; k < convertBorderSide.length; k++) {
										
										if(convertBorderSide[k].equals("top"))
										{
											newBorderSide.add("left");
										}
										else if(convertBorderSide[k].equals("left"))
										{
											newBorderSide.add("bottom");
										}
										else if(convertBorderSide[k].equals("right"))
										{
											newBorderSide.add("top");
										}
										else if(convertBorderSide[k].equals("bottom"))
										{
											newBorderSide.add("right");
										}
									}
									
									_propList.put("borderSide", newBorderSide);
								}
								
							}
							
						}
						else
						{
							_propList.put("className" , crossTabClassName ); 
							
							_propList.put("y", Float.valueOf(_propList.get("y").toString()) + _cloneY );
							
							if( _propList.get("x") != null ){
								_propList.put("x", Float.valueOf(_propList.get("x").toString()) + _cloneX );
							}
						}
						
						_propList.put("top", _propList.get("y"));
						_propList.put("left", _propList.get("x"));
						
						//아이템의 id를 탭 인덱스에 사용된 id로 변경
						if(_propList.containsKey("TABINDEX_ID") && _propList.get("TABINDEX_ID").equals("")==false)
						{
							_propList.put("id", _propList.get("TABINDEX_ID"));
						}
						
//						if(_propList.containsKey("SUFFIX_ID") && _propList.get("SUFFIX_ID").equals("")==false)
//						{
//							if(_className.equals("UBRadioBorder")){
//								String _groupName= _propList.get("groupName").toString();
//								_propList.put("groupName", _groupName +_propList.get("SUFFIX_ID") );
//							}
//							_propList.remove("SUFFIX_ID");
//						}

						
						_objects.add(_propList);
						
						
					}
					// 라벨밴드 반복 종료
					
				}

			}
			
			
			//repeatValue 를 이용하여 아이템의 height값을 업데이트 하고 필요없는 아이템을 리스트에서 제거
			repeatValueCheck(repeatValueCheckMap, _objects );
			
		}
			
		return _objects;
	}
	
	
	private BandInfoMapData cloneBandInfoData( BandInfoMapData originalBandData, String _grpName, String _defaultBand, String _dataSet, int _grpIndex)
	{
		BandInfoMapData cloneBandData = originalBandData.cloneBandInfo();
		
		cloneBandData.setGroupName( _grpName );
		cloneBandData.setGroupBand( _grpName  );
		cloneBandData.setDefaultBand(_defaultBand);
		cloneBandData.setDataSet(_dataSet);
		cloneBandData.setGroupDataIndex(_grpIndex);
		
		// 데이터 밴드에 데이터의 Row수를 담아두기
		if( cloneBandData.getClassName().equals(BandInfoMapData.DATA_BAND) && DataSet.containsKey(_dataSet) ) 
		{ 
			if( DataSet.get(_dataSet).size() > 0 && cloneBandData.getRowCount() < DataSet.get(_dataSet).size() )
			{
				cloneBandData.setRowCount( DataSet.get(_dataSet).size() );
			}
		}
		
		return cloneBandData;
	}
	
	
	/**
	 * 각 Row별 Height값을 담기
	 * @throws ScriptException 
	 * 
	 */
	private float getRowAdjustableHeight( BandInfoMapData bandInfo, int rowIndex, float firstPageHeight, float defaultPageHeight, HashMap<String, BandInfoMapData> bandData ) throws ScriptException
	{
		// height width fontSize lineHeight float[] 형태로 담기
		
		// DataSet 정보
		// 데이터의 Row수만큼 children을 반복하여 각 Row별 Height값을 가져와서 담기
		
		//
		String _dataID = "";
		String _systemFunction = "";
		HashMap<String, Value> currentItemData;
		ArrayList<HashMap<String, Value>> children = null;
		Object _dataValue = "";
		
		float _RowHeight = 0;
		
		float[] _heightAr = {firstPageHeight,defaultPageHeight};
		
		
		if( bandInfo.getDefaultBand().equals("") )
		{
			children = bandInfo.getChildren();
		}
		else
		{
			children = bandData.get(bandInfo.getDefaultBand()).getChildren();
		}
		
		if( DataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
		
		for (int i = 0; i < children.size(); i++) {
			
			_systemFunction = "";
			_dataValue = "";
			currentItemData = children.get(i);
			
			if( bandInfo.getGroupBand().equals("") && !currentItemData.get("dataSet").getStringValue().equals("") )
			{
				_dataID =  currentItemData.get("dataSet").getStringValue();
			}
			else
			{
				if( bandInfo.getDataSet().equals("") && !currentItemData.get("dataSet").getStringValue().equals("") )
				{
					_dataID =  currentItemData.get("dataSet").getStringValue();
				}
				else
				{
					_dataID = bandInfo.getDataSet();
				}
			}
			
			
			if(currentItemData.containsKey("systemFunction") && currentItemData.get("systemFunction") != null)
			{
				_systemFunction = currentItemData.get("systemFunction").getStringValue();
			}
			
			if( currentItemData.containsKey("dataType") && currentItemData.containsKey("dataSet") && !currentItemData.get("dataSet").equals("") )
			{
				if( currentItemData.get("dataType").getStringValue().equals("1") )
				{
					ArrayList<HashMap<String, Object>> _list;
					
					_list = DataSet.get( _dataID );

					if( _list != null && rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						_dataValue = _dataHm.get( currentItemData.get("column").getStringValue() );
					}
					//_dataValue
				}
				else if(currentItemData.get("dataType").getStringValue().equals("2"))
				{
					// rowIndex : 현재 Row Index
					// dataSet : 그룹핑된 데이터셋
					
					if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
						try {
							_systemFunction = URLDecoder.decode(_systemFunction, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						ArrayList<HashMap<String, Object>> _pList = DataSet.get( _dataID );
						String _datasetColumnName = currentItemData.get("column").getStringValue();
						mFunction.setDatasetList(DataSet);
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_dataValue = mFunction.testFN(_systemFunction, rowIndex, 0, 0,-1,-1 , "");
						}else{
							_dataValue = mFunction.function(_systemFunction, rowIndex, 0, 0,-1,-1);	
						}
						
					}
					
				}
				else if( currentItemData.get("dataType").getStringValue().equals("3"))
				{
					String _txt = currentItemData.get("text").getStringValue();
					
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					
					if( _inOf != -1 )
					{
						mFunction.setParam(mParam);
						_txt=mFunction.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								//_fnValue = mFunction.testFN(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
								_dataValue = _txt;
							}else{
								_dataValue = mFunction.function(_txt,rowIndex, 0, 0,-1,-1 , "");
							}
							
						}else{
							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);
							HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_pKey);

							_dataValue = _pList.get("parameter");
						}
					}
					
				}
				
				
			}
			
			HashMap<String,Object> resultMap;
			ArrayList<Object> _resultTextAr;
			ArrayList<Float> _resultHeightAr;
			
			// _dataValue값을 포맷터가 존재할경우 포맷팅 한후 사이즈를 체크하도록 수정
			if( _dataValue != null && currentItemData.get("formatter") != null &&  !currentItemData.get("formatter").getStringValue().equalsIgnoreCase("null") && !currentItemData.get("formatter").getStringValue().equalsIgnoreCase("") && currentItemData.get("Element") != null )
			{
				try {
					_dataValue = ItemConvertParser.formatItemData( currentItemData.get("formatter").getStringValue(), currentItemData.get("Element").getElementValue(), _dataValue.toString() );
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
			}
			
			if(_dataValue != null && _dataValue.equals("") == false  )
			{
				// converting된 String값을 이용하여 변환된Height값을 담기
				
				HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
				
				
				if( currentItemData.get("className").getStringValue().equals("UBSVGRichText") )
				{
					resultMap = convertSvgRichText( _dataValue.toString() ,  currentItemData, _heightAr);
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
				}
				else if( currentItemData.get("className").getStringValue().equals("UBSVGArea") )
				{
					// SVGArea의 아이템의 가변 height 처리
					resultMap = convertSvgArea( _dataValue.toString() ,  currentItemData, _heightAr);
					
					if( resultMap == null ) continue;
					
					currentItemData.put("PRESERVE_ASPECT_RATIO", new Value("xMinYMin","string"));
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
					
				}
				else
				{
					
					// font Resize 기능 사용 여부 확인 - 사용시 fontSize의 변경여부를 체크후 adjustableHeight값 처리
					// 폰트 사이즈 변경시 최소값만큼만 작아지도록 지정.
					// 2016-04-11 최명진
					float _fontSize = currentItemData.get("fontSize").getIntegerValue();
					String _fontFamily = currentItemData.get("fontFamily").getStringValue();
					String _fontWeight = currentItemData.get("fontWeight").getStringValue();
					float _padding = (currentItemData.containsKey("padding"))? currentItemData.get("padding").getIntegerValue():3;
					
					float _maxBorderSize = 0;
					if(currentItemData.containsKey("borderWidths"))
					{
						ArrayList<Integer> _borderWidths = (ArrayList<Integer>) currentItemData.get("borderWidths").getArrayIntegerValue();
						
						for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
							if(_maxBorderSize < _borderWidths.get(_bIndex))
							{
								_maxBorderSize = _borderWidths.get(_bIndex);
							}
						}
						_padding = _maxBorderSize + _padding;
					}
					
					float _itemWidth = currentItemData.get("width").getIntegerValue() - (2 * _padding);
					
					if( currentItemData.containsKey("resizeFont") && currentItemData.get("resizeFont").getBooleanValue() )
					{
						// fontSize 지정
						//	 bandInfo 에 아이템의 id값을 기준으로 각 row별 fontsize를 담아두기
						
						_fontSize = StringUtil.getTextMatchWidthFontSize(_dataValue.toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize );
						
						HashMap<String, Float> _resizeFontMap;
						if(bandInfo.getResizeFontData().size() <= rowIndex)
						{
							_resizeFontMap = new HashMap<String, Float>();
//							bandInfo.getResizeFontData().set(rowIndex, _resizeFontMap );
							bandInfo.getResizeFontData().add( _resizeFontMap );
						}
						else
						{
							_resizeFontMap = bandInfo.getResizeFontData().get(rowIndex);
						}
						
						_resizeFontMap.put( currentItemData.get("id").getStringValue(), _fontSize); 
							
					}
					
					optionMap.put("width", new float[]{ _itemWidth });
					optionMap.put("height", _heightAr);
					optionMap.put("fontSize", new float[]{_fontSize});
//					optionMap.put("fontSize", new float[]{currentItemData.get("fontSize").getIntegerValue()});
					optionMap.put("lineHeight", new float[]{currentItemData.get("lineHeight").getIntegerValue()});
//					optionMap.put("lineHeight", new float[]{(float) 1.2});//TODO LineHeightTEST
					if( currentItemData.containsKey("padding") == false ){
						optionMap.put("padding", new float[]{ 3 });
					}else{
						optionMap.put("padding", new float[]{currentItemData.get("padding").getIntegerValue()});
					}
					
					resultMap = StringUtil.getSplitCharacter( _dataValue.toString(), optionMap, 
							_fontWeight , 
							_fontFamily , 
							_fontSize,
							bandInfo.getAdjustableHeightMargin() );
//						currentItemData.get("fontSize").getIntegerValue()  );
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
					
				}
				
				float _resultHeight = _resultHeightAr.get(0);
				if(_resultTextAr.size() > 1)
				{
					_resultHeight = _heightAr[0];
				}
				
				if( _RowHeight < _resultHeight + currentItemData.get("band_y").getIntegerValue() )
				{
					_RowHeight = currentItemData.get("band_y").getIntegerValue() + _resultHeight;
				}
				
			}
			else
			{
				if(_RowHeight < bandInfo.getHeight() ) 
				{
					_RowHeight = bandInfo.getHeight();
				}
			}
			
			
		}
		
		return _RowHeight;
	}
	
	
	/**
	 * 각 Row별 Height array 값을 담기
	 * @throws ScriptException 
	 * 
	 */
	private HashMap<String , ArrayList<Float>> getRowAdjustableHeightArray( BandInfoMapData bandInfo, int rowIndex, float firstPageHeight, float defaultPageHeight, HashMap<String, BandInfoMapData> bandData ) throws ScriptException
	{
		// height width fontSize lineHeight float[] 형태로 담기
		
		// DataSet 정보
		// 데이터의 Row수만큼 children을 반복하여 각 Row별 Height값을 가져와서 담기		
		//
		String _dataID = "";
		String _systemFunction = "";
		HashMap<String, Value> currentItemData;
		ArrayList<HashMap<String, Value>> children = null;
		Object _dataValue = "";
		
		//Row별 height 정보를 리턴하는 함수(getRowAdjustableHeight copy본) by IHJ			
		HashMap<String, Value> nextItemData = null;		
		
		HashMap<String, ArrayList<Float>> hmReturnInfo = null;	
		
		float _RowHeight = 0;		
		float[] _heightAr = {firstPageHeight,defaultPageHeight};
		
		
			
		if( bandInfo.getDefaultBand().equals("") )
		{
			children = bandInfo.getChildren();
		}
		else
		{
			children = bandData.get(bandInfo.getDefaultBand()).getChildren();
		}
		
		if( DataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
				
		ArrayList <HashMap<String,Value>> tableInfo = bandInfo.getTableProperties();		
		HashMap<String,float[][]> hmCellInfo;				
		HashMap<String, HashMap<String,float[][]>> hmCellInfoList  = new HashMap<String, HashMap<String,float[][]>>();
		
		if( bandInfo.getGroupBand().equals("") == false )
		{
			tableInfo = bandData.get( bandInfo.getDefaultBand() ).getTableProperties();		
		}
		
		
		float[][] arCellHeight;
		float[][] arRowSpan;
		int maxRow = 0;
		int maxCol = 0;
		
		if(tableInfo != null){
			for(int i = 0; i < tableInfo.size(); i++){
				hmCellInfo = new HashMap<String,float[][]>();
				maxRow = tableInfo.get(i).get("rowCount").getIntValue();
				maxCol = tableInfo.get(i).get("columnCount").getIntValue();
				arCellHeight = new float[maxRow][maxCol];
				arRowSpan = new float[maxRow][maxCol];
				
				hmCellInfo.put("H", arCellHeight);//height
				hmCellInfo.put("S", arRowSpan);//span			
				hmCellInfoList.put(tableInfo.get(i).get("tableId").getStringValue() , hmCellInfo);
			}	
		}

		int rowIdx = 0;
		int colIdx = 0;
		int rowSpan = 0;				
		String tableId = "";
		for (int i = 0; i < children.size(); i++) {
						
			_systemFunction = "";
			_dataValue = "";
			currentItemData = children.get(i);	
			
			if(!currentItemData.containsKey("isCell")) continue;
			
			if( bandInfo.getGroupBand().equals("") && !currentItemData.get("dataSet").getStringValue().equals("") )
			{
				_dataID =  currentItemData.get("dataSet").getStringValue();
			}
			else
			{
				if( mFunction.getOriginalDataMap() != null && !currentItemData.get("dataSet").getStringValue().equals("") && mFunction.getOriginalDataMap().containsKey(bandInfo.getDataSet()) && mFunction.getOriginalDataMap().get(bandInfo.getDataSet()).equals(currentItemData.get("dataSet").getStringValue() ) == false )
				{
					_dataID =  currentItemData.get("dataSet").getStringValue();
				}else if( bandInfo.getDataSet().equals("") && !currentItemData.get("dataSet").getStringValue().equals("") )
				{
					_dataID =  currentItemData.get("dataSet").getStringValue();
				}
				else
				{
					_dataID = bandInfo.getDataSet();
				}
			}
			
			
			if(currentItemData.containsKey("systemFunction") && currentItemData.get("systemFunction") != null)
			{
				_systemFunction = currentItemData.get("systemFunction").getStringValue();
			}
			
			if( currentItemData.containsKey("dataType") && currentItemData.containsKey("dataSet") && !currentItemData.get("dataSet").equals("") )
			{
				if( currentItemData.get("dataType").getStringValue().equals("1") )
				{
					ArrayList<HashMap<String, Object>> _list;
					
					_list = DataSet.get( _dataID );

					if( _list != null && rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						_dataValue = _dataHm.get( currentItemData.get("column").getStringValue() );
					}
					//_dataValue
				}
				else if(currentItemData.get("dataType").getStringValue().equals("2"))
				{
					// rowIndex : 현재 Row Index
					// dataSet : 그룹핑된 데이터셋
					
					if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
//						try {
//							_systemFunction = URLDecoder.decode(_systemFunction, "UTF-8");
//						} catch (UnsupportedEncodingException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						
						ArrayList<HashMap<String, Object>> _pList = DataSet.get( _dataID );
						String _datasetColumnName = currentItemData.get("column").getStringValue();
						mFunction.setDatasetList(DataSet);
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_dataValue = mFunction.testFN(_systemFunction, rowIndex, 0, 0,-1,-1 , "");
						}else{
							_dataValue = mFunction.function(_systemFunction, rowIndex, 0, 0,-1,-1);	
						}
						
					}
					
				}
				else if( currentItemData.get("dataType").getStringValue().equals("3"))
				{
					String _txt = currentItemData.get("text").getStringValue();
					
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					
					if( _inOf != -1 )
					{
						mFunction.setParam(mParam);
						_txt=mFunction.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								//_fnValue = mFunction.testFN(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
								_dataValue = _txt;
							}else{
								_dataValue = mFunction.function(_txt,rowIndex, 0, 0,-1,-1 , "");
							}
							
						}else{
							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);
							HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_pKey);

							_dataValue = _pList.get("parameter");
						}
					}
					
				}
				
			}
			
			// _dataValue값을 포맷터가 존재할경우 포맷팅 한후 사이즈를 체크하도록 수정
			if( _dataValue != null && currentItemData.get("formatter") != null && !currentItemData.get("formatter").getStringValue().equalsIgnoreCase("null") 
					&& !currentItemData.get("formatter").getStringValue().equalsIgnoreCase("") && currentItemData.get("Element") != null )
			{
				try {
					_dataValue = ItemConvertParser.formatItemData( currentItemData.get("formatter").getStringValue(), currentItemData.get("Element").getElementValue(), _dataValue.toString() );
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
			}
			
			
			HashMap<String,Object> resultMap;
			ArrayList<Object> _resultTextAr;
			ArrayList<Float> _resultHeightAr;
			float cellHeight = 0;
			if(_dataValue != null && _dataValue.equals("") == false  )
			{
				// converting된 String값을 이용하여 변환된Height값을 담기
				
				
				if( currentItemData.get("className").getStringValue().equals("UBSVGRichText") )
				{
					resultMap = convertSvgRichText( _dataValue.toString() ,  currentItemData, _heightAr);
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
				}
				else if( currentItemData.get("className").getStringValue().equals("UBSVGArea") )
				{
					// SVGArea의 아이템의 가변 height 처리
					resultMap = convertSvgArea( _dataValue.toString() ,  currentItemData, _heightAr);
					
					if( resultMap == null ) continue;
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
				}
				else
				{
					HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
					
					// font Resize 기능 사용 여부 확인 - 사용시 fontSize의 변경여부를 체크후 adjustableHeight값 처리
					// 폰트 사이즈 변경시 최소값만큼만 작아지도록 지정.
					// 2016-04-11 최명진
					float _fontSize = currentItemData.get("fontSize").getIntegerValue();
					String _fontFamily = currentItemData.get("fontFamily").getStringValue();
					String _fontWeight = currentItemData.get("fontWeight").getStringValue();
					float _padding = (currentItemData.containsKey("padding"))? currentItemData.get("padding").getIntegerValue():3;
					
					float _maxBorderSize = 0;
					if(currentItemData.containsKey("borderWidths"))
					{
						ArrayList<Integer> _borderWidths = (ArrayList<Integer>) currentItemData.get("borderWidths").getArrayIntegerValue();
						
						for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
							if(_maxBorderSize < _borderWidths.get(_bIndex))
							{
								_maxBorderSize = _borderWidths.get(_bIndex);
							}
						}
						_padding = _maxBorderSize + _padding;
					}
					
					float _itemWidth = currentItemData.get("width").getIntegerValue() - (2 * _padding);
					
					if( currentItemData.containsKey("resizeFont") && currentItemData.get("resizeFont").getBooleanValue() )
					{
						// fontSize 지정
						//	 bandInfo 에 아이템의 id값을 기준으로 각 row별 fontsize를 담아두기
						
						_fontSize = StringUtil.getTextMatchWidthFontSize(_dataValue.toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize );
						
						HashMap<String, Float> _resizeFontMap;
						if(bandInfo.getResizeFontData().size() <= rowIndex)
						{
							_resizeFontMap = new HashMap<String, Float>();
//							bandInfo.getResizeFontData().set(rowIndex, _resizeFontMap );
							bandInfo.getResizeFontData().add( _resizeFontMap );
						}
						else
						{
							_resizeFontMap = bandInfo.getResizeFontData().get(rowIndex);
						}
						
						_resizeFontMap.put( currentItemData.get("id").getStringValue(), _fontSize); 
							
					}
					
					optionMap.put("width", new float[]{ _itemWidth });
					optionMap.put("height", _heightAr);
					optionMap.put("fontSize", new float[]{_fontSize});
					optionMap.put("lineHeight", new float[]{currentItemData.get("lineHeight").getIntegerValue()});
					if( currentItemData.containsKey("padding") == false ){
						optionMap.put("padding", new float[]{ 3 });
					}else{
						optionMap.put("padding", new float[]{currentItemData.get("padding").getIntegerValue()});
					}
					
					resultMap = StringUtil.getSplitCharacter( _dataValue.toString(), optionMap, 
							_fontWeight , 
							_fontFamily , 
							_fontSize,
							bandInfo.getAdjustableHeightMargin() );
//						currentItemData.get("fontSize").getIntegerValue()  );
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
					
				}
				
				//Cell당 height 값
				float _resultHeight = _resultHeightAr.get(0);
				
				
				if(_resultTextAr.size() > 1)
				{
					_resultHeight = _heightAr[0];
				}
				
				if(_resultHeight < currentItemData.get("height").getIntegerValue() && currentItemData.get("rowSpan").getIntValue() == 1){
					_resultHeight =currentItemData.get("height").getIntegerValue(); 
				}
				
				if( _RowHeight < _resultHeight + currentItemData.get("band_y").getIntegerValue() )
				{
					_RowHeight = currentItemData.get("band_y").getIntegerValue() + _resultHeight;
				}
				
				
				
				cellHeight = _resultHeight;
				
			}
			else
			{
				if(_RowHeight < bandInfo.getHeight() ) 
				{
					_RowHeight = bandInfo.getHeight();
				}				
				cellHeight = currentItemData.get("height").getIntegerValue();
			}			
			
			if(tableInfo != null){				
				//TODO : 테이블이 밴들 안에 1개 이상 있을 시는 org_Table_id로 구분해야 함				
				rowIdx = currentItemData.get("rowIndex").getIntValue();
				colIdx = currentItemData.get("columnIndex").getIntValue();
				rowSpan = currentItemData.get("rowSpan").getIntValue();
				tableId =  currentItemData.get("ORIGINAL_TABLE_ID").getStringValue();		
				
				hmCellInfoList.get(tableId).get("H")[rowIdx][colIdx]  = cellHeight;			
				hmCellInfoList.get(tableId).get("S")[rowIdx][colIdx]  = rowSpan;	
				
				if(rowSpan >1){
					for(int v = 1; v < rowSpan; v++){
						hmCellInfoList.get(tableId).get("H")[rowIdx + v][colIdx] = -1 * v;// height 값에 원값을 찾을 수 있는 index정보를 넣오준다
					}
				}
			}
		}	
		
		
		
		HashMap<String , ArrayList<Float>> hmReturnRowHeight = new HashMap<String , ArrayList<Float>>();
		
		ArrayList<Float> maxHeight = new ArrayList<Float>();	
		if(tableInfo != null){			
			float preRowHeight = 0;
			float rowHeight = 0;
			int  reIdx = 0;
			
			Iterator<String> keys = hmCellInfoList.keySet().iterator();
			while( keys.hasNext() ){
				ArrayList<Float> arRowHeight = new ArrayList<Float>();
				String key = keys.next();
				arCellHeight = hmCellInfoList.get(key).get("H");
				arRowSpan = hmCellInfoList.get(key).get("S");
				
//				System.err.println("arCellHeight 리턴값 >>>>>>>>>>>>>>>>" + arCellHeight);
//				System.err.println("arRowSpan 리턴값 >>>>>>>>>>>>>>>>" + arRowSpan);
				int lastRowSpanIdx = -1;
				float tempIdx = 0;
				for(int i = 0; i<arCellHeight.length;i++){//rowCount			
					int cellSpan = 0;					
					float cellHeight = 0;	
					maxHeight = new ArrayList<Float>();	
					
					for(int k = 0; k<arCellHeight[i].length;k++){//해당 Row의 colCount
						cellSpan = (int) arRowSpan[i][k];
						tempIdx = cellHeight = arCellHeight[i][k];						
						if(cellHeight < 0){//상당 Cell에서 rowspan된 경우의 cell의 height 값이 원본 값을 찾아 갈 수 있도록 보정 Index를 담아놓았다.										
							
							reIdx = i + Math.round(cellHeight); // 현재 rowIndex에서 보정 Index 값을 더해 원값의 rowIndex값을 구한다. 
							
							cellHeight = arCellHeight[reIdx][k];//보정 rowindex로 
							
							cellSpan = (int) (arRowSpan[reIdx][k] + tempIdx);	//보정된 인덱스로 원값의 rowspan값을 가져와 현재 rowindex를 뺸 값이 해당 cell의 rowspan 값이 된다.					
							
							cellHeight = cellHeight - preRowHeight; //상단Row값이 결정된 경우 상단 값을 뺀 값으로 평균값을 구한다 
							if(cellSpan>1){
								cellHeight = cellHeight/cellSpan;
							}
						}else{
							//해당 cell이 RowSpan이 있는경우 rowspan으로 평군 값을 낸다.
							if(cellSpan > 1){
								cellHeight = cellHeight/cellSpan;
								lastRowSpanIdx = i  + cellSpan-1;
							}	
						}				
						maxHeight.add(cellHeight);				
					}
					
					rowHeight = Collections.max(maxHeight);//해당 Row의 최대값을 구한다
					if(lastRowSpanIdx == i){ // rowspan 마지막 인덱스인경우는 이전까지의 높이합계값을 초기화 시킨다 (다음 rowspan에 영향받지 않도록)
						preRowHeight = 0;
						lastRowSpanIdx = -1;
					}
					if(lastRowSpanIdx > 0){
						preRowHeight = preRowHeight + rowHeight;//
					}
					arRowHeight.add( Double.valueOf(Math.ceil(rowHeight)).floatValue() );
//					arRowHeight.add( rowHeight );
					
				}	
				
				hmReturnRowHeight.put(key, arRowHeight);
			}	
		}		
		
		return hmReturnRowHeight;
	}
	
	
	/**
	 * 각 Row별 Height array 값을 담기
	 * @throws ScriptException 
	 * 
	 */
	private HashMap<String , ArrayList<Float>> getRowAdjustableHeightArraySimple( BandInfoMapDataSimple bandInfo, int rowIndex, float firstPageHeight, float defaultPageHeight, HashMap<String, BandInfoMapDataSimple> bandData ) throws ScriptException
	{
		// height width fontSize lineHeight float[] 형태로 담기
		
		// DataSet 정보
		// 데이터의 Row수만큼 children을 반복하여 각 Row별 Height값을 가져와서 담기		
		//
		String _dataID = "";
		String _systemFunction = "";
		HashMap<String, Object> currentItemData;
		ArrayList<HashMap<String, Object>> children = null;
		Object _dataValue = "";
		String _className = "";
		
		//Row별 height 정보를 리턴하는 함수(getRowAdjustableHeight copy본) by IHJ			
		HashMap<String, Value> nextItemData = null;		
		
		HashMap<String, ArrayList<Float>> hmReturnInfo = null;	
		
		float _RowHeight = 0;		
		float[] _heightAr = {firstPageHeight,defaultPageHeight};
		
		
			
		if( bandInfo.getDefaultBand().equals("") )
		{
			children = bandInfo.getChildren();
		}
		else
		{
			children = bandData.get(bandInfo.getDefaultBand()).getChildren();
		}
		
		if( DataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
				
		ArrayList <HashMap<String,Object>> tableInfo = bandInfo.getTableProperties();		
		HashMap<String,float[][]> hmCellInfo;				
		HashMap<String, HashMap<String,float[][]>> hmCellInfoList  = new HashMap<String, HashMap<String,float[][]>>();
		
		if( bandInfo.getGroupBand().equals("") == false )
		{
			tableInfo = bandData.get( bandInfo.getDefaultBand() ).getTableProperties();		
		}
		
		
		float[][] arCellHeight;
		float[][] arRowSpan;
		int maxRow = 0;
		int maxCol = 0;
		
		if(tableInfo != null){
			for(int i = 0; i < tableInfo.size(); i++){
				hmCellInfo = new HashMap<String,float[][]>();
				maxRow =Integer.valueOf(tableInfo.get(i).get("rowCount").toString());
				maxCol =Integer.valueOf( tableInfo.get(i).get("columnCount").toString());
				arCellHeight = new float[maxRow][maxCol];
				arRowSpan = new float[maxRow][maxCol];
				
				hmCellInfo.put("H", arCellHeight);//height
				hmCellInfo.put("S", arRowSpan);//span			
				hmCellInfoList.put(tableInfo.get(i).get("tableId").toString() , hmCellInfo);
			}	
		}

		int rowIdx = 0;
		int colIdx = 0;
		int rowSpan = 0;				
		String tableId = "";
		Object _dsType = null;
		Object _dataSet = null;
		Object _column = null;
		
		float _fontSize = 0;
		String _fontFamily = "";
		String _fontWeight = "";
		float _padding = 3;
		float _width = 0;
		float _height = 0;
		float _bandY = 0;
		
		for (int i = 0; i < children.size(); i++) {
						
			_systemFunction = "";
			_dataValue = "";
			currentItemData = children.get(i);	
			
			if(!currentItemData.containsKey("isCell")) continue;
			
			_className = currentItemData.get("className").toString();
			
			if( bandInfo.getGroupBand().equals("") && !currentItemData.get("dataSet").toString().equals("") )
			{
				_dataID =  currentItemData.get("dataSet").toString();
			}
			else
			{
				if( mFunction.getOriginalDataMap() != null && !currentItemData.get("dataSet").toString().equals("") && mFunction.getOriginalDataMap().containsKey(bandInfo.getDataSet()) && mFunction.getOriginalDataMap().get(bandInfo.getDataSet()).equals(currentItemData.get("dataSet").toString() ) == false )
				{
					_dataID =  currentItemData.get("dataSet").toString();
				}else if( bandInfo.getDataSet().equals("") && !currentItemData.get("dataSet").toString().equals("") )
				{
					_dataID =  currentItemData.get("dataSet").toString();
				}
				else
				{
					_dataID = bandInfo.getDataSet();
				}
			}
			
			
			if(currentItemData.containsKey("systemFunction") && currentItemData.get("systemFunction") != null)
			{
				_systemFunction = currentItemData.get("systemFunction").toString();
			}
			
			_dsType = currentItemData.get("dataType");
			_dataSet = currentItemData.get("dataSet");
			_column = currentItemData.get("column");
			
			if( _dsType != null && _dataSet != null && !_dataSet.toString().equals("") )
			{
				if( _dsType.toString().equals("1") )
				{
					ArrayList<HashMap<String, Object>> _list;
					
					_list = DataSet.get( _dataID );

					if( _list != null && rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						if( _dataHm.get( _column.toString() ) != null )
						{
							_dataValue = _dataHm.get( currentItemData.get("column").toString() );
						}
					}
					//_dataValue
				}
				else if( _dsType.toString().equals("2"))
				{
					// rowIndex : 현재 Row Index
					// dataSet : 그룹핑된 데이터셋
					
					if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
						
						ArrayList<HashMap<String, Object>> _pList = DataSet.get( _dataID );
						String _datasetColumnName = _column.toString();
						mFunction.setDatasetList(DataSet);
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_dataValue = mFunction.testFN(_systemFunction, rowIndex, 0, 0,-1,-1 , "");
						}else{
							_dataValue = mFunction.function(_systemFunction, rowIndex, 0, 0,-1,-1);	
						}
						
					}
					
				}
				else if( _dsType.toString().equals("3"))
				{
					String _txt = currentItemData.get("text").toString();
					
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					
					if( _inOf != -1 )
					{
						mFunction.setParam(mParam);
						_txt=mFunction.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								_dataValue = _txt;
							}else{
								_dataValue = mFunction.function(_txt,rowIndex, 0, 0,-1,-1 , "");
							}
							
						}else{
							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);
							HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_pKey);

							_dataValue = _pList.get("parameter");
						}
					}
					
				}
				
			}
			
			// _dataValue값을 포맷터가 존재할경우 포맷팅 한후 사이즈를 체크하도록 수정
			if( _dataValue != null && currentItemData.get("formatter") != null && !currentItemData.get("formatter").toString().equalsIgnoreCase("null") 
					&& !currentItemData.get("formatter").toString().equalsIgnoreCase("") && currentItemData.get("formatValue") != null )
			{
				try {
					
					// json형태일때 formatter 처리 필요 
					_dataValue = ItemConvertParser.formatItemDataJS( currentItemData.get("formatter").toString(), (HashMap<String, Object>) currentItemData.get("formatValue"), _dataValue.toString() );
				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
				}
			}
			
			
			HashMap<String,Object> resultMap;
			ArrayList<Object> _resultTextAr;
			ArrayList<Float> _resultHeightAr;
			float cellHeight = 0;
			Object _tempObj;
			
			if(_dataValue != null && _dataValue.equals("") == false  )
			{
				// converting된 String값을 이용하여 변환된Height값을 담기
				
				if( currentItemData.get("className").toString().equals("UBSVGRichText") )
				{
					resultMap = convertSvgRichTextSimple( _dataValue.toString() ,  currentItemData, _heightAr);
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
				}
				else if( currentItemData.get("className").toString().equals("UBSVGArea") )
				{
					// SVGArea의 아이템의 가변 height 처리
					resultMap = convertSvgAreaSimple( _dataValue.toString() ,  currentItemData, _heightAr);
					
					if( resultMap == null ) continue;
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
				}
				else
				{
					HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
					
					// font Resize 기능 사용 여부 확인 - 사용시 fontSize의 변경여부를 체크후 adjustableHeight값 처리
					// 폰트 사이즈 변경시 최소값만큼만 작아지도록 지정.
					// 2016-04-11 최명진
					_fontSize = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "fontSize" ).toString() );
					_fontFamily = UBComponent.getProperties(currentItemData, _className, "fontFamily" ).toString();
					_fontWeight = UBComponent.getProperties(currentItemData, _className, "fontWeight" ).toString();
					_padding = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "padding","3" ).toString() );
					
					float _maxBorderSize = 0;
					_tempObj = currentItemData.get("borderWidths");
					if( _tempObj != null )
					{
						ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _tempObj;
						
						for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
							if(_maxBorderSize < _borderWidths.get(_bIndex))
							{
								_maxBorderSize = _borderWidths.get(_bIndex);
							}
						}
						_padding = _maxBorderSize + _padding;
					}
					 _width =  Float.valueOf( UBComponent.getProperties(currentItemData, _className, "width" ).toString() );
					
					float _itemWidth = _width - (2 * _padding);
					
					_tempObj = currentItemData.get("resizeFont");
					if( _tempObj != null && _tempObj.toString().equals("true") )
					{
						// fontSize 지정
						//	 bandInfo 에 아이템의 id값을 기준으로 각 row별 fontsize를 담아두기
						
						_fontSize = StringUtil.getTextMatchWidthFontSize(_dataValue.toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize );
						
						HashMap<String, Float> _resizeFontMap;
						if(bandInfo.getResizeFontData().size() <= rowIndex)
						{
							_resizeFontMap = new HashMap<String, Float>();
							bandInfo.getResizeFontData().add( _resizeFontMap );
						}
						else
						{
							_resizeFontMap = bandInfo.getResizeFontData().get(rowIndex);
						}
						
						_resizeFontMap.put( currentItemData.get("id").toString(), _fontSize); 
							
					}
					
					optionMap.put("width", new float[]{ _itemWidth });
					optionMap.put("height", _heightAr);
					optionMap.put("fontSize", new float[]{_fontSize});
					optionMap.put("lineHeight", new float[]{ Float.valueOf( currentItemData.get("lineHeight").toString() ) });
					if( currentItemData.containsKey("padding") == false ){
						optionMap.put("padding", new float[]{ 3 });
					}else{
						optionMap.put("padding", new float[]{ Float.valueOf(currentItemData.get("padding").toString() ) });
					}
					
					resultMap = StringUtil.getSplitCharacter( _dataValue.toString(), optionMap, 
							_fontWeight , 
							_fontFamily , 
							_fontSize,
							bandInfo.getAdjustableHeightMargin() );
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
					
				}
				
				//Cell당 height 값
				float _resultHeight = _resultHeightAr.get(0);
				
				
				if(_resultTextAr.size() > 1)
				{
					_resultHeight = _heightAr[0];
				}
				
				_height = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "height" ).toString() );
				rowSpan = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "rowSpan" ).toString() );
				if(_resultHeight < _height && rowSpan == 1){
					_resultHeight =_height; 
				}
				
				_bandY = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "band_y" ).toString() );
				if( _RowHeight < _resultHeight + _bandY )
				{
					_RowHeight = _bandY + _resultHeight;
				}
				
				cellHeight = _resultHeight;
				
			}
			else
			{
				if(_RowHeight < bandInfo.getHeight() ) 
				{
					_RowHeight = bandInfo.getHeight();
				}				
				
				cellHeight = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "height" ).toString() );
			}			
			
			if(tableInfo != null){				
				//TODO : 테이블이 밴들 안에 1개 이상 있을 시는 org_Table_id로 구분해야 함	
				
				rowIdx = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "rowIndex" ).toString() );
				colIdx = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "columnIndex" ).toString() );
				rowSpan = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "rowSpan" ).toString() );
				tableId =  currentItemData.get("ORIGINAL_TABLE_ID").toString();		
				
				hmCellInfoList.get(tableId).get("H")[rowIdx][colIdx]  = cellHeight;			
				hmCellInfoList.get(tableId).get("S")[rowIdx][colIdx]  = rowSpan;	
				
				if(rowSpan >1){
					for(int v = 1; v < rowSpan; v++){
						hmCellInfoList.get(tableId).get("H")[rowIdx + v][colIdx] = -1 * v;// height 값에 원값을 찾을 수 있는 index정보를 넣오준다
					}
				}
			}
		}	
		
		
		HashMap<String , ArrayList<Float>> hmReturnRowHeight = new HashMap<String , ArrayList<Float>>();
		
		ArrayList<Float> maxHeight = new ArrayList<Float>();	
		if(tableInfo != null){			
			float preRowHeight = 0;
			float rowHeight = 0;
			int  reIdx = 0;
			
			Iterator<String> keys = hmCellInfoList.keySet().iterator();
			while( keys.hasNext() ){
				ArrayList<Float> arRowHeight = new ArrayList<Float>();
				String key = keys.next();
				arCellHeight = hmCellInfoList.get(key).get("H");
				arRowSpan = hmCellInfoList.get(key).get("S");
				
				int lastRowSpanIdx = -1;
				float tempIdx = 0;
				for(int i = 0; i<arCellHeight.length;i++){//rowCount			
					int cellSpan = 0;					
					float cellHeight = 0;	
					maxHeight = new ArrayList<Float>();	
					
					for(int k = 0; k<arCellHeight[i].length;k++){//해당 Row의 colCount
						cellSpan = (int) arRowSpan[i][k];
						tempIdx = cellHeight = arCellHeight[i][k];						
						if(cellHeight < 0){//상당 Cell에서 rowspan된 경우의 cell의 height 값이 원본 값을 찾아 갈 수 있도록 보정 Index를 담아놓았다.										
							
							reIdx = i + Math.round(cellHeight); // 현재 rowIndex에서 보정 Index 값을 더해 원값의 rowIndex값을 구한다. 
							
							cellHeight = arCellHeight[reIdx][k];//보정 rowindex로 
							
							cellSpan = (int) (arRowSpan[reIdx][k] + tempIdx);	//보정된 인덱스로 원값의 rowspan값을 가져와 현재 rowindex를 뺸 값이 해당 cell의 rowspan 값이 된다.					
							
							cellHeight = cellHeight - preRowHeight; //상단Row값이 결정된 경우 상단 값을 뺀 값으로 평균값을 구한다 
							if(cellSpan>1){
								cellHeight = cellHeight/cellSpan;
							}
						}else{
							//해당 cell이 RowSpan이 있는경우 rowspan으로 평군 값을 낸다.
							if(cellSpan > 1){
								cellHeight = cellHeight/cellSpan;
								lastRowSpanIdx = i  + cellSpan-1;
							}	
						}				
						maxHeight.add(cellHeight);				
					}
					
					rowHeight = Collections.max(maxHeight);//해당 Row의 최대값을 구한다
					if(lastRowSpanIdx == i){ // rowspan 마지막 인덱스인경우는 이전까지의 높이합계값을 초기화 시킨다 (다음 rowspan에 영향받지 않도록)
						preRowHeight = 0;
						lastRowSpanIdx = -1;
					}
					if(lastRowSpanIdx > 0){
						preRowHeight = preRowHeight + rowHeight;//
					}
					arRowHeight.add( Double.valueOf(Math.ceil(rowHeight)).floatValue() );
					
				}	
				
				hmReturnRowHeight.put(key, arRowHeight);
			}	
		}		
		
		return hmReturnRowHeight;
	}
	
	/**
	 * 각 Row별 Height값을 담기
	 * @throws ScriptException 
	 * 
	 */
	private float getRowAdjustableHeightSimple( BandInfoMapDataSimple bandInfo, int rowIndex, float firstPageHeight, float defaultPageHeight, HashMap<String, BandInfoMapDataSimple> bandData ) throws ScriptException
	{
		// height width fontSize lineHeight float[] 형태로 담기
		
		// DataSet 정보
		// 데이터의 Row수만큼 children을 반복하여 각 Row별 Height값을 가져와서 담기
		
		//
		String _dataID = "";
		String _systemFunction = "";
		HashMap<String, Object> currentItemData;
		ArrayList<HashMap<String, Object>> children = null;
		Object _dataValue = "";
		
		float _RowHeight = 0;
		
		float[] _heightAr = {firstPageHeight,defaultPageHeight};
		
		
		if( bandInfo.getDefaultBand().equals("") )
		{
			children = bandInfo.getChildren();
		}
		else
		{
			children = bandData.get(bandInfo.getDefaultBand()).getChildren();
		}
		
		if( DataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
		
		String _className = "";
		String _dataSet = "";
		String _dataType = "";
		String _column = "";
		String _text = "";
		
		float _fontSize = 0;
		String _fontFamily = "";
		String _fontWeight = "";
		float _padding = 3;
		float _width = 0;
		float _height = 0;
		float _bandY = 0;
		boolean _resizeFont = false;
		float _lineHeight = 0;
		String _id = "";
		
		Object _tempObj;
		
		for (int i = 0; i < children.size(); i++) {
			
			_systemFunction = "";
			_dataValue = "";
			currentItemData = children.get(i);
			
			_className = currentItemData.get("className").toString();
			
			_dataSet = UBComponent.getProperties(currentItemData, _className, "dataSet", "").toString();
			_dataType = UBComponent.getProperties(currentItemData, _className, "dataType", "").toString();
			_column = UBComponent.getProperties(currentItemData, _className, "column", "").toString();
			_text = UBComponent.getProperties(currentItemData, _className, "text", "").toString();
			
			_id = currentItemData.get("id").toString();
			
			if( bandInfo.getGroupBand().equals("") && !_dataSet.equals("") )
			{
				_dataID =  _dataSet;
			}
			else
			{
				if( bandInfo.getDataSet().equals("") && !_dataSet.equals("") )
				{
					_dataID =  _dataSet;
				}
				else
				{
					_dataID = bandInfo.getDataSet();
				}
			}
			
			
			_systemFunction = UBComponent.getProperties(currentItemData, _className, "systemFunction", "").toString();
			
			if( !_dataType.equals("") && !_dataSet.equals("") )
			{
				if( _dataType.equals("1") )
				{
					ArrayList<HashMap<String, Object>> _list;
					
					_list = DataSet.get( _dataID );

					if( _list != null && rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						if( !_column.equals("") && _dataHm.get(_column) != null ) 	_dataValue = _dataHm.get( _column );
					}
					//_dataValue
				}
				else if(_dataType.equals("2"))
				{
					// rowIndex : 현재 Row Index
					// dataSet : 그룹핑된 데이터셋
					
					if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
						try {
							_systemFunction = URLDecoder.decode(_systemFunction, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						ArrayList<HashMap<String, Object>> _pList = DataSet.get( _dataID );
						String _datasetColumnName = _column;
						mFunction.setDatasetList(DataSet);
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_dataValue = mFunction.testFN(_systemFunction, rowIndex, 0, 0,-1,-1 , "");
						}else{
							_dataValue = mFunction.function(_systemFunction, rowIndex, 0, 0,-1,-1);	
						}
						
					}
					
				}
				else if( _dataType.equals("3"))
				{
					String _txt = _text;
					
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					
					if( _inOf != -1 )
					{
						mFunction.setParam(mParam);
						_txt=mFunction.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								_dataValue = _txt;
							}else{
								_dataValue = mFunction.function(_txt,rowIndex, 0, 0,-1,-1 , "");
							}
							
						}else{
							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);
							HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_pKey);

							_dataValue = _pList.get("parameter");
						}
					}
					
				}
				
				
			}
			
			HashMap<String,Object> resultMap;
			ArrayList<Object> _resultTextAr;
			ArrayList<Float> _resultHeightAr;
			
			// _dataValue값을 포맷터가 존재할경우 포맷팅 한후 사이즈를 체크하도록 수정
			// json타입의 포맷터 처리 필요
			if( _dataValue != null && currentItemData.get("formatter") != null &&  !currentItemData.get("formatter").toString().equalsIgnoreCase("null") && !currentItemData.get("formatter").toString().equalsIgnoreCase("") && currentItemData.get("formatValue") != null )
			{
				try {
					_dataValue = ItemConvertParser.formatItemDataJS( currentItemData.get("formatter").toString(),(HashMap<String, Object>) currentItemData.get("formatValue"), _dataValue.toString() );
				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
				}
			}
			
			_width = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "width" ).toString() );
			_height = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "height" ).toString() );
			
			if(_dataValue != null && _dataValue.equals("") == false  )
			{
				// converting된 String값을 이용하여 변환된Height값을 담기
				
				HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
				
				
				if( _className.equals("UBSVGRichText") )
				{
					resultMap = convertSvgRichTextSimple( _dataValue.toString() ,  currentItemData, _heightAr);
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
				}
				else if( _className.equals("UBSVGArea") )
				{
					// SVGArea의 아이템의 가변 height 처리
					resultMap = convertSvgAreaSimple( _dataValue.toString() ,  currentItemData, _heightAr);
					
					if( resultMap == null ) continue;
					
					currentItemData.put("PRESERVE_ASPECT_RATIO", new Value("xMinYMin","string"));
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
					
				}
				else
				{
					
					// font Resize 기능 사용 여부 확인 - 사용시 fontSize의 변경여부를 체크후 adjustableHeight값 처리
					// 폰트 사이즈 변경시 최소값만큼만 작아지도록 지정.
					// 2016-04-11 최명진
					_fontSize = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "fontSize" ).toString() );
					_fontFamily = UBComponent.getProperties(currentItemData, _className, "fontFamily" ).toString();
					_fontWeight = UBComponent.getProperties(currentItemData, _className, "fontWeight" ).toString();
					_padding = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "padding","3" ).toString() );
					_width = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "width","0" ).toString() );
					_resizeFont = UBComponent.getProperties(currentItemData, _className, "resizeFont" ).toString().equals("true");
					float _maxBorderSize = 0;
					_lineHeight = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "lineHeight","1.2" ).toString() );
					_bandY = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "band_y","-1" ).toString() );
					
					_tempObj = currentItemData.get("borderWidths");
					if(_tempObj != null)
					{
						ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _tempObj;
						
						for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
							if(_maxBorderSize < _borderWidths.get(_bIndex))
							{
								_maxBorderSize = _borderWidths.get(_bIndex);
							}
						}
						_padding = _maxBorderSize + _padding;
					}
					
					float _itemWidth = _width - (2 * _padding);
					
					if( _resizeFont )
					{
						// fontSize 지정
						//	 bandInfo 에 아이템의 id값을 기준으로 각 row별 fontsize를 담아두기
						_fontSize = StringUtil.getTextMatchWidthFontSize(_dataValue.toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize );
						
						HashMap<String, Float> _resizeFontMap;
						if(bandInfo.getResizeFontData().size() <= rowIndex)
						{
							_resizeFontMap = new HashMap<String, Float>();
							bandInfo.getResizeFontData().add( _resizeFontMap );
						}
						else
						{
							_resizeFontMap = bandInfo.getResizeFontData().get(rowIndex);
						}
						
						_resizeFontMap.put( _id, _fontSize); 
							
					}
					
					optionMap.put("width", new float[]{ _itemWidth });
					optionMap.put("height", _heightAr);
					optionMap.put("fontSize", new float[]{_fontSize});
					optionMap.put("lineHeight", new float[]{_lineHeight});
					if( currentItemData.containsKey("padding") == false ){
						optionMap.put("padding", new float[]{ 3 });
					}else{
						optionMap.put("padding", new float[]{_padding});
					}
					
					resultMap = StringUtil.getSplitCharacter( _dataValue.toString(), optionMap, 
							_fontWeight , 
							_fontFamily , 
							_fontSize,
							bandInfo.getAdjustableHeightMargin() );
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
					
				}
				
				float _resultHeight = _resultHeightAr.get(0);
				if(_resultTextAr.size() > 1)
				{
					_resultHeight = _heightAr[0];
				}
				
				if( _RowHeight < _resultHeight + _bandY )
				{
					_RowHeight = _bandY + _resultHeight;
				}
				
			}
			else
			{
				if(_RowHeight < bandInfo.getHeight() ) 
				{
					_RowHeight = bandInfo.getHeight();
				}
			}
			
			
		}
		
		return _RowHeight;
	}
	
	
	/**
	 * 각 Row별 Height값을 담기
	 * @throws ScriptException 
	 * 
	 */
	private ArrayList<Object> getRowAdjustableHeightResizeText( BandInfoMapData bandInfo, int rowIndex, float firstPageHeight, float defaultPageHeight, HashMap<String, BandInfoMapData> bandData ) throws ScriptException
	{
		// height width fontSize lineHeight float[] 형태로 담기
		
		// DataSet 정보
		// 데이터의 Row수만큼 children을 반복하여 각 Row별 Height값을 가져와서 담기
		
		//
		String _dataID = "";
		String _systemFunction = "";
		HashMap<String, Value> currentItemData;
		ArrayList<HashMap<String, Value>> children = null;
		Object _dataValue = "";
		
		ArrayList<Object> returnData = new ArrayList<Object>();
		float _minHeightPadding = 0;
		float _RowHeight = 0;
		
		float[] _heightAr = {firstPageHeight-_minHeightPadding,defaultPageHeight-_minHeightPadding};
		
		
		if( bandInfo.getDefaultBand().equals("") )
		{
			children = bandInfo.getChildren();
		}
		else
		{
			children = bandData.get(bandInfo.getDefaultBand()).getChildren();
		}
		
		if( DataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
		boolean _useDataItem = false;
		for (int i = 0; i < children.size(); i++) {
			
			_useDataItem = false;
			_systemFunction = "";
			_dataValue = "";
			currentItemData = children.get(i);
			
			if( bandInfo.getDataSet().equals("") && !currentItemData.get("dataSet").getStringValue().equals("") )
			{
				_dataID =  currentItemData.get("dataSet").getStringValue();
			}
			else
			{
				_dataID = bandInfo.getDataSet();
			}
			
			if(currentItemData.containsKey("systemFunction") && currentItemData.get("systemFunction") != null)
			{
				_systemFunction = currentItemData.get("systemFunction").getStringValue();
			}
			
			if( currentItemData.containsKey("dataType") && currentItemData.containsKey("dataSet") && !currentItemData.get("dataSet").equals("") )
			{
				if( currentItemData.get("dataType").getStringValue().equals("1") )
				{
					ArrayList<HashMap<String, Object>> _list;
					
					_list = DataSet.get( _dataID );

					if( rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						_dataValue = _dataHm.get( currentItemData.get("column").getStringValue() );
					}
					//_dataValue
				}
				else if(currentItemData.get("dataType").getStringValue().equals("2"))
				{
					// rowIndex : 현재 Row Index
					// dataSet : 그룹핑된 데이터셋
					
					if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
						try {
							_systemFunction = URLDecoder.decode(_systemFunction, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						ArrayList<HashMap<String, Object>> _pList = DataSet.get( _dataID );
						String _datasetColumnName = currentItemData.get("column").getStringValue();
						mFunction.setDatasetList(DataSet);
						
						
						if( bandInfo.getDataSet() != null && bandInfo.getDataSet().equals("") == false )
						{
							if( mFunction.getFunctionVersion().equals("2.0") ){
								_dataValue = mFunction.testFN(_systemFunction, rowIndex, 0, 0,-1,-1, bandInfo.getDataSet() );
							}else{
								_dataValue = mFunction.function(_systemFunction, rowIndex, 0, 0,-1,-1, bandInfo.getDataSet() );
							}
							
							
						}
						else
						{
							if( mFunction.getFunctionVersion().equals("2.0") ){
								_dataValue = mFunction.testFN(_systemFunction, rowIndex, 0, 0,-1,-1 , "");
							}else{
								_dataValue = mFunction.function(_systemFunction, rowIndex, 0, 0,-1,-1);	
							}
							
						}
					}
					
				}
				else if( currentItemData.get("dataType").getStringValue().equals("3"))
				{
					String _txt = currentItemData.get("text").getStringValue();
					
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					
					if( _inOf != -1 )
					{
						mFunction.setParam(mParam);
						_txt=mFunction.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								//_fnValue = mFunction.testFN(_txt,rowIndex,_totalPageNum,_currentPage, _startIndex , _lastIndex , dataSet );
								_dataValue = _txt;
							}else{
								_dataValue = mFunction.function(_txt,rowIndex, 0, 0,-1,-1 , "");
							}
							
						}else{
							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);
							HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_pKey);

							_dataValue = _pList.get("parameter");
						}
					}
					
				}
				
				_useDataItem = true;
			}
			
			HashMap<String,Object> resultMap;
//			ArrayList<String> _resultTextAr;
			ArrayList<Object> _resultTextAr;
			ArrayList<Float> _resultHeightAr;
			
			// _dataValue값을 포맷터가 존재할경우 포맷팅 한후 사이즈를 체크하도록 수정
			if( _dataValue != null && currentItemData.get("formatter") != null && !currentItemData.get("formatter").getStringValue().equalsIgnoreCase("null") 
					&& !currentItemData.get("formatter").getStringValue().equalsIgnoreCase("") && currentItemData.get("Element") != null )
			{
				try {
					_dataValue = ItemConvertParser.formatItemData( currentItemData.get("formatter").getStringValue(), currentItemData.get("Element").getElementValue(), _dataValue.toString() );
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
			}
			else if(  _dataValue != null && currentItemData.get("formatter") != null && !currentItemData.get("formatter").getStringValue().equalsIgnoreCase("null") 
					&& !currentItemData.get("formatter").getStringValue().equalsIgnoreCase("") && currentItemData.get("formatValue") != null )
			{
				try {
					_dataValue = ItemConvertParser.formatItemDataJS( currentItemData.get("formatter").getStringValue(), (HashMap<String, Object> ) currentItemData.get("formatValue").getMapValue(), _dataValue.toString() );
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(_dataValue != null && _dataValue.equals("") == false )
			{
				
				int _bandY =(int) Math.floor( currentItemData.get("band_y").getIntegerValue() );
				
				if(_bandY < 0 ) _bandY = 0;
				
				float[] _heightArTemp = {firstPageHeight-_minHeightPadding - _bandY ,defaultPageHeight-_minHeightPadding - _bandY};
				
				// item 이 svgArea일경에는 위에서 태그를 만들어 두고 height만큼 row를 잘라서 담아두도록 지정
				// 아이템의 svg객체를 이용하여 총 line수를 기준으로 Height값만큼의 row값을 가져와서 담기( 부모태그값도 같이 지정하여야 함 )
				if( currentItemData.get("className").getStringValue().equals("UBSVGRichText") )
				{
					resultMap = convertSvgRichText( _dataValue.toString() ,  currentItemData, _heightArTemp);
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
				}
				else if( currentItemData.get("className").getStringValue().equals("UBSVGArea") )
				{
					// SVGArea의 아이템의 가변 height 처리
					resultMap = convertSvgArea( _dataValue.toString() ,  currentItemData, _heightAr);
					
					if( resultMap == null ) continue;
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
					
					currentItemData.put("PRESERVE_ASPECT_RATIO", new Value("xMinYMin","string"));
					
					// dataType 이 param일경우 해당 _resultTextAr의 값을 dataSet으로 생성하고 
					// svgArea에 dataType를 1로 dataset과 column을 신규로 생성한 데이터셋으로 변경한다 
					// 이후 _resultTextAr은 clear 시킨다.
					if(currentItemData.get("dataType").getStringValue().equals("3") && _resultTextAr.size() > 1 )
					{
						String _paramDsName = "";
						HashMap<String, Object> _paramDs;
						ArrayList<HashMap<String, Object>> _newParamDS = new ArrayList<HashMap<String, Object>>();
						for( int _p = 0; _p < _resultTextAr.size();_p++ )
						{
							_paramDs = new HashMap<String, Object>();
							_paramDs.put("PARAM_COL0", _resultTextAr.get(_p));
							
							_newParamDS.add(_paramDs);
						}
						
						DataSet.put( currentItemData.get("id").getStringValue(), _newParamDS);
						currentItemData.put("dataType", new Value("1","string"));
						currentItemData.put("dataSet", new Value(currentItemData.get("id").getStringValue(),"string"));
						currentItemData.put("column", new Value("PARAM_COL0","string"));
						
						_resultTextAr.clear();
						
						_useDataItem = false;
					}
					
				}
				else
				{
					// converting된 String값을 이용하여 변환된Height값을 담기
					
					// font Resize 기능 사용 여부 확인 - 사용시 fontSize의 변경여부를 체크후 adjustableHeight값 처리
					// 폰트 사이즈 변경시 최소값만큼만 작아지도록 지정.
					// 2016-04-11 최명진
					float _fontSize = currentItemData.get("fontSize").getIntegerValue();
					String _fontFamily = currentItemData.get("fontFamily").getStringValue();
					String _fontWeight = currentItemData.get("fontWeight").getStringValue();
					
					float _padding = (currentItemData.containsKey("padding"))? currentItemData.get("padding").getIntegerValue():3;
							
					float _maxBorderSize = 0;
					if(currentItemData.containsKey("borderWidths"))
					{
						ArrayList<Integer> _borderWidths = (ArrayList<Integer>) currentItemData.get("borderWidths").getArrayIntegerValue();
						
						for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
							if(_maxBorderSize < _borderWidths.get(_bIndex))
							{
								_maxBorderSize = _borderWidths.get(_bIndex);
							}
						}
						_padding = _maxBorderSize + _padding;
					}
					
					float _itemWidth = currentItemData.get("width").getIntegerValue() - (2 * _padding);
					
					if( currentItemData.containsKey("resizeFont") && currentItemData.get("resizeFont").getBooleanValue() )
					{
						// fontSize 지정
						//	 bandInfo 에 아이템의 id값을 기준으로 각 row별 fontsize를 담아두기
						
						_fontSize = StringUtil.getTextMatchWidthFontSize(_dataValue.toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize );
						
						HashMap<String, Float> _resizeFontMap;
						if(bandInfo.getResizeFontData().size() <= rowIndex)
						{
							_resizeFontMap = new HashMap<String, Float>();
							bandInfo.getResizeFontData().add( _resizeFontMap );
						}
						else
						{
							_resizeFontMap = bandInfo.getResizeFontData().get(rowIndex);
						}
						
						_resizeFontMap.put( currentItemData.get("id").getStringValue(), _fontSize); 
							
					}
					
					
					HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
					
					
					optionMap.put("width", new float[]{ _itemWidth });
					optionMap.put("height", _heightArTemp);
//					optionMap.put("height", _heightAr);
					optionMap.put("fontSize", new float[]{ _fontSize });
					optionMap.put("lineHeight", new float[]{currentItemData.get("lineHeight").getIntegerValue()}); 
//					optionMap.put("lineHeight", new float[]{(float) 1.2}); //TODO LineHeightTEST
					
					
					if( currentItemData.containsKey("padding") == false ){
						optionMap.put("padding", new float[]{ 3 });
					}else{
						optionMap.put("padding", new float[]{currentItemData.get("padding").getIntegerValue()});
					}
					
					resultMap = StringUtil.getSplitCharacter( _dataValue.toString(), optionMap , _fontWeight  , _fontFamily, _fontSize ,bandInfo.getAdjustableHeightMargin() );
//					_resultTextAr = (ArrayList<String>) resultMap.get("Text");
					_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
				}
				
				
				if( _bandY > 0 )
				{
					for (int j = 0; j < _resultHeightAr.size(); j++) {
						_resultHeightAr.set(j, _resultHeightAr.get(j)+_bandY);
					}
				}
				
				if(_useDataItem)
				{
					currentItemData.put("dataType_N", new Value("1", "string"));
					currentItemData.put("column_N", new Value( currentItemData.get("id").getStringValue() , "string"));
				}
//				if( _resultHeightAr.size() > 1 &&  _useDataItem )
//				{
//					currentItemData.put("dataType_N", new Value("1", "string"));
//					currentItemData.put("column_N", new Value( currentItemData.get("id").getStringValue() , "string"));
//				}
				
				ArrayList<Object> returnArray = new ArrayList<Object>();
				
				returnArray.add(currentItemData.get("id").getStringValue());
				returnArray.add(_resultTextAr);
				returnArray.add(_resultHeightAr);
				
				returnData.add(returnArray);
			}
			else
			{
				_resultTextAr = new ArrayList<Object>();
				_resultTextAr.add("");
				_resultHeightAr =new ArrayList<Float>();
				_resultHeightAr.add(bandInfo.getHeight());
				
				ArrayList<Object> returnArray = new ArrayList<Object>();
				
				returnArray.add(currentItemData.get("id").getStringValue());
				returnArray.add(_resultTextAr);
				returnArray.add(_resultHeightAr);
				
				returnData.add(returnArray);
			}
			
			
		}
		
		return returnData;
	}
	
	
	/**
	 * 각 Row별 Height값을 담기
	 * @throws ScriptException 
	 * 
	 */
	private ArrayList<Object> getRowAdjustableHeightResizeTextSimple( BandInfoMapDataSimple bandInfo, int rowIndex, float firstPageHeight, float defaultPageHeight, HashMap<String, BandInfoMapDataSimple> bandData ) throws ScriptException
	{
		// height width fontSize lineHeight float[] 형태로 담기
		
		// DataSet 정보
		// 데이터의 Row수만큼 children을 반복하여 각 Row별 Height값을 가져와서 담기
		
		//
		String _dataID = "";
		String _systemFunction = "";
		HashMap<String, Object> currentItemData;
		ArrayList<HashMap<String, Object>> children = null;
		Object _dataValue = "";
		
		ArrayList<Object> returnData = new ArrayList<Object>();
		float _minHeightPadding = 0;
		float _RowHeight = 0;
		
		float[] _heightAr = {firstPageHeight-_minHeightPadding,defaultPageHeight-_minHeightPadding};
		
		
		if( bandInfo.getDefaultBand().equals("") )
		{
			children = bandInfo.getChildren();
		}
		else
		{
			children = bandData.get(bandInfo.getDefaultBand()).getChildren();
		}
		
		if( DataSet.containsKey(_dataID) == false )
		{
			_dataID = "";
		}
		
		String _className = "";
		String _dataSet = "";
		String _dataType = "";
		String _column = "";
		String _text = "";
		
		float _fontSize = 0;
		String _fontFamily = "";
		String _fontWeight = "";
		float _padding = 3;
		float _width = 0;
		float _height = 0;
		float _bandY = 0;
		float _lineHeight = 1.2f;
		boolean _resizeFont = false;
		
		Object _tempObj;
		String _id = "";
		
		boolean _useDataItem = false;
		for (int i = 0; i < children.size(); i++) {
			
			_useDataItem = false;
			_systemFunction = "";
			_dataValue = "";
			currentItemData = children.get(i);
			
			_className = currentItemData.get("className").toString();
			
			_dataSet = UBComponent.getProperties(currentItemData, _className, "dataSet", "").toString();
			_dataType = UBComponent.getProperties(currentItemData, _className, "dataType", "").toString();
			_column = UBComponent.getProperties(currentItemData, _className, "column", "").toString();
			_text = UBComponent.getProperties(currentItemData, _className, "text", "").toString();
			_systemFunction = UBComponent.getProperties(currentItemData, _className, "systemFunction", "").toString();
			_id = UBComponent.getProperties(currentItemData, _className, "id", "").toString();
			
			_bandY =(int) Math.floor(  Float.valueOf( UBComponent.getProperties(currentItemData, _className, "band_y", "-1").toString() ) );
			
			if( bandInfo.getDataSet().equals("") && !_dataSet.equals("") )
			{
				_dataID =  _dataSet.toString();
			}
			else
			{
				_dataID = bandInfo.getDataSet();
			}
			
			if( !_dataType.equals("") && !_dataSet.equals("") )
			{
				if( _dataType.equals("1") )
				{
					ArrayList<HashMap<String, Object>> _list;
					
					_list = DataSet.get( _dataID );

					if( rowIndex < _list.size() )
					{
						HashMap<String, Object> _dataHm = _list.get(rowIndex);
						
						if( _dataHm.get(_column) != null  )	_dataValue = _dataHm.get( _column );
					}
				}
				else if(_dataType.equals("2"))
				{
					// rowIndex : 현재 Row Index
					// dataSet : 그룹핑된 데이터셋
					
					if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
						try {
							_systemFunction = URLDecoder.decode(_systemFunction, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						ArrayList<HashMap<String, Object>> _pList = DataSet.get( _dataID );
						String _datasetColumnName = _column;
						mFunction.setDatasetList(DataSet);
						
						
						if( bandInfo.getDataSet() != null && bandInfo.getDataSet().equals("") == false )
						{
							if( mFunction.getFunctionVersion().equals("2.0") ){
								_dataValue = mFunction.testFN(_systemFunction, rowIndex, 0, 0,-1,-1, bandInfo.getDataSet() );
							}else{
								_dataValue = mFunction.function(_systemFunction, rowIndex, 0, 0,-1,-1, bandInfo.getDataSet() );
							}
							
							
						}
						else
						{
							if( mFunction.getFunctionVersion().equals("2.0") ){
								_dataValue = mFunction.testFN(_systemFunction, rowIndex, 0, 0,-1,-1 , "");
							}else{
								_dataValue = mFunction.function(_systemFunction, rowIndex, 0, 0,-1,-1);	
							}
							
						}
					}
					
				}
				else if( _dataType.equals("3"))
				{
					String _txt = _text;
					
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					
					if( _inOf != -1 )
					{
						mFunction.setParam(mParam);
						_txt=mFunction.replaceParameterValue(_txt);
						_inOf = _txt.indexOf("{param:");
						if( _inOf != 0 ){
							
							if( mFunction.getFunctionVersion().equals("2.0") ){
								_dataValue = _txt;
							}else{
								_dataValue = mFunction.function(_txt,rowIndex, 0, 0,-1,-1 , "");
							}
							
						}else{
							int _keyIndex=_txt.lastIndexOf("}");
							_pKey = _txt.substring(_inOf + 7 , _keyIndex);
							HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_pKey);

							_dataValue = _pList.get("parameter");
						}
					}
					
				}
				
				_useDataItem = true;
			}
			
			HashMap<String,Object> resultMap;
			ArrayList<Object> _resultTextAr;
			ArrayList<Float> _resultHeightAr;
			
			// _dataValue값을 포맷터가 존재할경우 포맷팅 한후 사이즈를 체크하도록 수정
			// Json 타입일때 처리 필요 Element 사용X
			if( _dataValue != null && currentItemData.get("formatter") != null && !currentItemData.get("formatter").toString().equalsIgnoreCase("null") 
					&& !currentItemData.get("formatter").toString().equalsIgnoreCase("") && currentItemData.get("Element") != null )
			{
				try {
					_dataValue = ItemConvertParser.formatItemData( currentItemData.get("formatter").toString(),(Element) currentItemData.get("Element"), _dataValue.toString() );
				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
				}
			}
			
			if(_dataValue != null && _dataValue.equals("") == false )
			{
				
				if(_bandY < 0 ) _bandY = 0;
				
				float[] _heightArTemp = {firstPageHeight-_minHeightPadding - _bandY ,defaultPageHeight-_minHeightPadding - _bandY};
				
				// item 이 svgArea일경에는 위에서 태그를 만들어 두고 height만큼 row를 잘라서 담아두도록 지정
				// 아이템의 svg객체를 이용하여 총 line수를 기준으로 Height값만큼의 row값을 가져와서 담기( 부모태그값도 같이 지정하여야 함 )
				if( _className.equals("UBSVGRichText") )
				{
					resultMap = convertSvgRichTextSimple( _dataValue.toString() ,  currentItemData, _heightArTemp);
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
				}
				else if( _className.equals("UBSVGArea") )
				{
					// SVGArea의 아이템의 가변 height 처리
					resultMap = convertSvgAreaSimple( _dataValue.toString() ,  currentItemData, _heightAr);
					
					if( resultMap == null ) continue;
					
					_resultTextAr = (ArrayList<Object>) resultMap.get("RESULT_DATA");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("RESULT_HEIGHT");
					
					currentItemData.put("PRESERVE_ASPECT_RATIO", new Value("xMinYMin","string"));
					
					// dataType 이 param일경우 해당 _resultTextAr의 값을 dataSet으로 생성하고 
					// svgArea에 dataType를 1로 dataset과 column을 신규로 생성한 데이터셋으로 변경한다 
					// 이후 _resultTextAr은 clear 시킨다.
					if(_dataType.equals("3") && _resultTextAr.size() > 1 )
					{
						String _paramDsName = "";
						HashMap<String, Object> _paramDs;
						ArrayList<HashMap<String, Object>> _newParamDS = new ArrayList<HashMap<String, Object>>();
						for( int _p = 0; _p < _resultTextAr.size();_p++ )
						{
							_paramDs = new HashMap<String, Object>();
							_paramDs.put("PARAM_COL0", _resultTextAr.get(_p));
							
							_newParamDS.add(_paramDs);
						}
						
						DataSet.put( _id, _newParamDS);
						currentItemData.put("dataType", "1");
						currentItemData.put("dataSet", _id);
						currentItemData.put("column", "PARAM_COL0");
						
						_resultTextAr.clear();
						
						_useDataItem = false;
					}
					
				}
				else
				{
					// converting된 String값을 이용하여 변환된Height값을 담기
					
					// font Resize 기능 사용 여부 확인 - 사용시 fontSize의 변경여부를 체크후 adjustableHeight값 처리
					// 폰트 사이즈 변경시 최소값만큼만 작아지도록 지정.
					// 2016-04-11 최명진
					_fontSize = Float.valueOf( UBComponent.getProperties( currentItemData,  _className, "fontSize", "10").toString() );  
					_fontFamily = UBComponent.getProperties( currentItemData,  _className, "fontFamily", "10").toString();  
					_fontWeight = UBComponent.getProperties( currentItemData,  _className, "fontWeight", "10").toString();  
						
					_padding = Float.valueOf( UBComponent.getProperties( currentItemData,  _className, "padding", "3").toString() );  
					_width = Float.valueOf( UBComponent.getProperties( currentItemData,  _className, "width", "0").toString() );  
					_lineHeight = Float.valueOf( UBComponent.getProperties( currentItemData,  _className, "lineHeight", "1.2").toString() );  
					
					_resizeFont = UBComponent.getProperties( currentItemData,  _className, "resizeFont", "").toString().equals("true");
					
					float _maxBorderSize = 0;
					_tempObj = currentItemData.get("borderWidths");
					if( _tempObj != null )
					{
						ArrayList<Integer> _borderWidths = (ArrayList<Integer>) _tempObj;
						
						for (int _bIndex = 0; _bIndex < _borderWidths.size(); _bIndex++) {
							if(_maxBorderSize < _borderWidths.get(_bIndex))
							{
								_maxBorderSize = _borderWidths.get(_bIndex);
							}
						}
						_padding = _maxBorderSize + _padding;
					}
					
					float _itemWidth = _width - (2 * _padding);
					
					if( _resizeFont )
					{
						// fontSize 지정
						//	 bandInfo 에 아이템의 id값을 기준으로 각 row별 fontsize를 담아두기
						_fontSize = StringUtil.getTextMatchWidthFontSize(_dataValue.toString(), _itemWidth, _fontFamily, _fontWeight, _fontSize, mMinimumResizeFontSize );
						
						HashMap<String, Float> _resizeFontMap;
						if(bandInfo.getResizeFontData().size() <= rowIndex)
						{
							_resizeFontMap = new HashMap<String, Float>();
							bandInfo.getResizeFontData().add( _resizeFontMap );
						}
						else
						{
							_resizeFontMap = bandInfo.getResizeFontData().get(rowIndex);
						}
						
						_resizeFontMap.put( _id, _fontSize); 
							
					}
					
					
					HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
					
					
					optionMap.put("width", new float[]{ _itemWidth });
					optionMap.put("height", _heightArTemp);
					optionMap.put("fontSize", new float[]{ _fontSize });
					optionMap.put("lineHeight", new float[]{ _lineHeight }); 
					
					
					if( currentItemData.containsKey("padding") == false ){
						optionMap.put("padding", new float[]{ 3 });
					}else{
						optionMap.put("padding", new float[]{_padding});
					}
					
					resultMap = StringUtil.getSplitCharacter( _dataValue.toString(), optionMap , _fontWeight  , _fontFamily, _fontSize ,bandInfo.getAdjustableHeightMargin() );
					_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
					_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
				}
				
				
				if( _bandY > 0 )
				{
					for (int j = 0; j < _resultHeightAr.size(); j++) {
						_resultHeightAr.set(j, _resultHeightAr.get(j)+_bandY);
					}
				}
				
				if(_useDataItem)
				{
					currentItemData.put("dataType_N", "1");
					currentItemData.put("column_N", _id );
				}
				
				ArrayList<Object> returnArray = new ArrayList<Object>();
				
				returnArray.add(_id);
				returnArray.add(_resultTextAr);
				returnArray.add(_resultHeightAr);
				
				returnData.add(returnArray);
			}
			else
			{
				_resultTextAr = new ArrayList<Object>();
				_resultTextAr.add("");
				_resultHeightAr =new ArrayList<Float>();
				_resultHeightAr.add(bandInfo.getHeight());
				
				ArrayList<Object> returnArray = new ArrayList<Object>();
				
				returnArray.add(_id);
				returnArray.add(_resultTextAr);
				returnArray.add(_resultHeightAr);
				
				returnData.add(returnArray);
			}
			
			
		}
		
		return returnData;
	}
	
	
	//TEST UBSVGRichText
	private HashMap<String,Object> convertSvgRichText( String _data, HashMap<String, Value> _property , float[] _heightList )
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
        int _gap = 10;
        
        _width = Float.valueOf( _property.get("width").getStringValue() ).intValue();
        _height = Float.valueOf( _property.get("height").getStringValue() ).intValue();
        _lineGap =  (_property.containsKey("lineGap"))?Float.valueOf( _property.get("lineGap").getStringValue() ).intValue():10;
        _useWordWrap = (_property.containsKey("wordWrap"))?Boolean.valueOf( _property.get("wordWrap").getStringValue() ).booleanValue():true;
 
        if(Log.pageFontUnit.equals("pt"))
        {
        	_fontSizeUnit = 1;
        }
        if(_property.containsKey("fontSize"))
        {
        	_fontSize =  (int) Math.round(Double.parseDouble(_property.get("fontSize").getStringValue()));
        }
        if(_property.containsKey("fontFamily"))
        {
        	_fontFamily =  _property.get("fontFamily").getStringValue();
        }
        
        svgObjhandler = new SvgRichTextObjectHandler();
        
		try {
			
			SAXParser saxParser = factory.newSAXParser();
//				_height =  Float.valueOf(_heightList[0]).intValue();
	        svgObjhandler.init(_width, _height, _fontFamily, _fontSize, _lineGap, false, _useWordWrap, _heightList, true, _fontSizeUnit);
	         
	        InputSource inputSource = new InputSource(new StringReader(_data));
			saxParser.parse(  inputSource , svgObjhandler);
	        
		 } catch (SAXException e) {
	    	  // TODO Auto-generated catch block
	    	  if(e.getMessage().equals("HeightOverflowException"))
	    	  {
	    		  if(svgObjhandler != null)
	    		  {
	    			   
	    		  }
	    	  }
	    	  else
	    	  {
	    		  e.printStackTrace();
	    	  }
	      } catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	      }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HashMap<String,Object> _retMap = new HashMap<String,Object>();
		_retMap.put("RESULT_DATA", svgObjhandler.svgTblList );
		_retMap.put("RESULT_HEIGHT", svgObjhandler.resultHeightList );
		
		return _retMap;
	}
		
		
	private HashMap<String,Object> convertSvgArea( String _data, HashMap<String, Value> _property , float[] _heightList )
	{
		if( _data == null || _data.toString().equals("")) return null;
		
		String _tmpDataValue = _data.toString();
		boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
		
		// 가변 Row지정시 해당 svg의 높이만큼 자동으로 늘어나는 기능이므로 fixedToSize는 false로 지저한다.
		_property.put("fixedToSize", new Value(false,"boolean"));
		
		if(_bSVG)
		{
			_data = StringUtil.replaceSVGTag(_tmpDataValue, false, false, null);	
		}
		else
		{
			boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
			if(!_bHasHtmlTag)
				_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
				
			_tmpDataValue =  convertHtmlToSvgText( _tmpDataValue, _property, _heightList[0] );
			_data = StringUtil.replaceSVGTag(_tmpDataValue, false, false, null);
		}
								
//	    try {
//	    	BufferedWriter writer = new BufferedWriter(new FileWriter("d:/web/testSVG.txt"));
//			writer.write(_data);
//			writer.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		
//		_data = _data.toString().replace(" ", "%20");
		
		ArrayList<Object> _resultTextAr = new ArrayList<Object>();
		ArrayList<Float> _resultHeightAr = new ArrayList<Float>();

		SvgSplit _svgSp = new SvgSplit();
		// svg태그, height,  첫페이지의 y포지션 ( height - currnetH )
		List<Object> _resultAr = null;
		
		int _pageMaxWidth = Float.valueOf( mPageWidth ).intValue();
				
		if( _property.containsKey("x") )
		{
			//_pageMaxWidth = Float.valueOf( mPageWidth - Float.valueOf( _property.get("x").getStringValue() ) ).intValue();
			_pageMaxWidth = Float.valueOf( mPageWidth - (Float.valueOf( _property.get("x").getStringValue() ) - mPageMarginLeft) ).intValue();
		}
				
		try {
			_resultAr = _svgSp.splitSVG(_data, Float.valueOf( _heightList[1]).intValue() , Float.valueOf(_heightList[1] - _heightList[0]).intValue(), _pageMaxWidth);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( _resultAr != null)
		{
			
			HashMap<String, Object> _resultObj;
			for( int i=0; i < _resultAr.size(); i++ )
			{
				_resultObj = (HashMap<String, Object>) _resultAr.get(i);
				
				_resultTextAr.add( _resultObj.get("svg") );
				_resultHeightAr.add( Float.valueOf( _resultObj.get("height").toString()) );
			}
		}
		// 변환된 SVG태그를 이용하여 페이지벼 분할 처리 
		
		if( !_data.toString().equals("") )
		{
			HashMap<String,Object> _retMap = new HashMap<String,Object>();
			_retMap.put("RESULT_DATA", _resultTextAr); 
			_retMap.put("RESULT_HEIGHT", _resultHeightAr);
			
			return _retMap;
		}
		else
		{
			return null;
		}
		
	}

	private HashMap<String,Object> convertSvgRichTextSimple( String _data, HashMap<String, Object> _property , float[] _heightList )
	{
		String _retStr = "";
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SvgRichTextObjectHandler svgObjhandler;
		String _className = _property.get("className").toString();
		int _width = 0;
		int _height = 0;
		String _fontFamily = "돋움";
		int _fontSizeUnit = 0;
		int _fontSize = 10;
		int _lineGap = 10;
		boolean _useWordWrap = true;
		int _gap = 10;
		
		_fontSize = (int) Math.round( Double.parseDouble( UBComponent.getProperties(_property, _className, "fontSize","10").toString() )  );
		_fontFamily = UBComponent.getProperties(_property, _className, "fontFamily","돋움").toString();
		
		_width = Float.valueOf(  UBComponent.getProperties(_property, _className, "width").toString() ).intValue();
		_height = Float.valueOf(  UBComponent.getProperties(_property, _className, "height").toString() ).intValue();
		_lineGap =  Float.valueOf( UBComponent.getProperties(_property, _className, "lineGap","10").toString() ).intValue();
		_useWordWrap = UBComponent.getProperties(_property, _className, "wordWrap","true").toString().equals("true");
		
		
		if(Log.pageFontUnit.equals("pt"))
		{
			_fontSizeUnit = 1;
		}
		
		svgObjhandler = new SvgRichTextObjectHandler();
		
		try {
			
			SAXParser saxParser = factory.newSAXParser();
			svgObjhandler.init(_width, _height, _fontFamily, _fontSize, _lineGap, false, _useWordWrap, _heightList, true, _fontSizeUnit);
			
			InputSource inputSource = new InputSource(new StringReader(_data));
			saxParser.parse(  inputSource , svgObjhandler);
			
		} catch (SAXException e) {
			if(e.getMessage().equals("HeightOverflowException"))
			{
				if(svgObjhandler != null)
				{
					
				}
			}
			else
			{
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		HashMap<String,Object> _retMap = new HashMap<String,Object>();
		_retMap.put("RESULT_DATA", svgObjhandler.svgTblList );
		_retMap.put("RESULT_HEIGHT", svgObjhandler.resultHeightList );
		
		return _retMap;
	}
	
	
	private HashMap<String,Object> convertSvgAreaSimple( String _data, HashMap<String, Object> _property , float[] _heightList )
	{
		if( _data == null || _data.toString().equals("")) return null;
		
		String _tmpDataValue = _data.toString();
		boolean _bSVG = (_tmpDataValue != null && (_tmpDataValue.indexOf("<svg") != -1 || _tmpDataValue.indexOf("<SVG") != -1 || _tmpDataValue.indexOf("<Svg") != -1) ) ? true : false;
		
		// 가변 Row지정시 해당 svg의 높이만큼 자동으로 늘어나는 기능이므로 fixedToSize는 false로 지저한다.
		_property.put("fixedToSize", false);
		float _x = Float.valueOf( UBComponent.getProperties(_property, _property.get("className").toString(), "x", "-1").toString() );
		
		if(_bSVG)
		{
			_data = StringUtil.replaceSVGTag(_tmpDataValue, false, false, null);	
		}
		else
		{
			boolean _bHasHtmlTag = (_tmpDataValue != null && (_tmpDataValue.indexOf("<html") != -1 || _tmpDataValue.indexOf("<HTML") != -1 || _tmpDataValue.indexOf("<Html") != -1) ) ? true : false;
			if(!_bHasHtmlTag)
				_tmpDataValue = "<html><body>" + _tmpDataValue + "</body></html>";
			
			_tmpDataValue =  convertHtmlToSvgTextSimple( _tmpDataValue, _property, _heightList[0] );
			_data = StringUtil.replaceSVGTag(_tmpDataValue, false, false, null);
		}
		
		ArrayList<Object> _resultTextAr = new ArrayList<Object>();
		ArrayList<Float> _resultHeightAr = new ArrayList<Float>();
		
		SvgSplit _svgSp = new SvgSplit();
		// svg태그, height,  첫페이지의 y포지션 ( height - currnetH )
		List<Object> _resultAr = null;
		
		int _pageMaxWidth = Float.valueOf( mPageWidth ).intValue();
		
		if( _x > -1 )
		{
			_pageMaxWidth = Float.valueOf( mPageWidth - (_x - mPageMarginLeft) ).intValue();
		}
		
		try {
			_resultAr = _svgSp.splitSVG(_data, Float.valueOf( _heightList[1]).intValue() , Float.valueOf(_heightList[1] - _heightList[0]).intValue(), _pageMaxWidth);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		if( _resultAr != null)
		{
			
			HashMap<String, Object> _resultObj;
			for( int i=0; i < _resultAr.size(); i++ )
			{
				_resultObj = (HashMap<String, Object>) _resultAr.get(i);
				
				_resultTextAr.add( _resultObj.get("svg") );
				_resultHeightAr.add( Float.valueOf( _resultObj.get("height").toString()) );
			}
		}
		// 변환된 SVG태그를 이용하여 페이지벼 분할 처리 
		
		if( !_data.toString().equals("") )
		{
			HashMap<String,Object> _retMap = new HashMap<String,Object>();
			_retMap.put("RESULT_DATA", _resultTextAr); 
			_retMap.put("RESULT_HEIGHT", _resultHeightAr);
			
			return _retMap;
		}
		else
		{
			return null;
		}
		
	}
	
	private Float getCurrentYpositionBand( ArrayList<Float> heightAr, int _currentIndex, int _startIndex, BandInfoMapData bandInfo )
	{
		float retPoistion = 0;
		
		for (int i = _startIndex; i < _currentIndex; i++) {
			
			if( i > heightAr.size()-1 )
			{
				retPoistion = retPoistion + bandInfo.getHeight();
			}
			else
			{
				retPoistion = retPoistion + heightAr.get(i);
			}
		}
		
		return retPoistion;
	}
	
	private Float getCurrentYpositionBand( ArrayList<Float> heightAr, int _currentIndex, int _startIndex, BandInfoMapDataSimple bandInfo )
	{
		float retPoistion = 0;
		
		for (int i = _startIndex; i < _currentIndex; i++) {
			
			if( i > heightAr.size()-1 )
			{
				retPoistion = retPoistion + bandInfo.getHeight();
			}
			else
			{
				retPoistion = retPoistion + heightAr.get(i);
			}
		}
		
		return retPoistion;
	}
	
	
	
	private String getRepeatValueCheckStr( HashMap<String, Value> currentItem, String dataText, String dataSet, int rowIndex)
	{
		String returnStr = dataText;
		
		String column = "";
		if( currentItem.containsKey("repeatedColumn") && currentItem.get("repeatedColumn").getArrayCollection() != null && currentItem.get("repeatedColumn").getArrayCollection().size() > 0  )
		{
			List<Object> repeatColumnAr =  currentItem.get("repeatedColumn").getArrayCollection();
			HashMap<String, String> repeatColumn = null;
			
			returnStr = "";
			
			for (int i = 0; i < repeatColumnAr.size(); i++) {
				
				repeatColumn = (HashMap<String, String>) repeatColumnAr.get(i);
				
				
				if( DataSet.containsKey(dataSet) && DataSet.get(dataSet).size() > rowIndex )
				{
					column = repeatColumn.get("column");
					
					
					if( DataSet.get(dataSet).get(rowIndex).containsKey(column) && DataSet.get(dataSet).get(rowIndex).get(column) != null )
					{
						returnStr = returnStr + DataSet.get(dataSet).get(rowIndex).get(column);
					}
				}
			}
			
		}
		
		
		return returnStr;
	}
	private String getRepeatValueCheckStrSimple( HashMap<String, Object> currentItem, String dataText, String dataSet, int rowIndex)
	{
		String returnStr = dataText;
		
		String column = "";
		if( currentItem.containsKey("repeatedColumn") && currentItem.get("repeatedColumn") != null )
		{
			List<Object> repeatColumnAr;
			try {
				if( currentItem.get("repeatedColumn") instanceof String )
				{
					repeatColumnAr = (List<Object>) JSONValue.parse( URLDecoder.decode(currentItem.get("repeatedColumn").toString(), "UTF-8") );
				}
				else
				{
					repeatColumnAr = (List<Object>) currentItem.get("repeatedColumn");
				}
				
				if( repeatColumnAr.size() > 0 )
				{
					HashMap<String, String> repeatColumn = null;
					
					returnStr = "";
					
					for (int i = 0; i < repeatColumnAr.size(); i++) {
						
						repeatColumn = (HashMap<String, String>) repeatColumnAr.get(i);
						
						
						if( DataSet.containsKey(dataSet) && DataSet.get(dataSet).size() > rowIndex )
						{
							column = repeatColumn.get("column");
							
							
							if( DataSet.get(dataSet).get(rowIndex).containsKey(column) && DataSet.get(dataSet).get(rowIndex).get(column) != null )
							{
								returnStr = returnStr + DataSet.get(dataSet).get(rowIndex).get(column);
							}
						}
					}
				}
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		return returnStr;
	}
	
	
	private boolean repeatValueCheck( HashMap<String, ArrayList<HashMap<String, Object>>> repeatItem, ArrayList<HashMap<String, Object>> pageAr )
	{
		
		Collection<?> keys = repeatItem.keySet();
		HashMap<String, Object> argoItem = null;
		HashMap<String, Object> checkItem = null;
		float updateY = 0;
		String argoText = "";
		boolean _resultFlag = false;
		
		for(Object key: keys){
			
			argoItem = null;
			argoText = "";
			updateY = 0;
			
			for (int i = 0; i < repeatItem.get(key).size(); i++) {
				
				checkItem = (HashMap<String, Object>) repeatItem.get(key).get(i).get("item");
				
				if( argoItem == null || argoText.equals(repeatItem.get(key).get(i).get("datatext").toString()) == false )
				{
					argoItem = checkItem;
					argoText = repeatItem.get(key).get(i).get("datatext").toString();
				}
				else
				{
					updateY = Float.valueOf(  checkItem.get("y").toString() ) +  Float.valueOf(  checkItem.get("height").toString() ) - Float.valueOf( argoItem.get("y").toString() );
					argoItem.put("height", updateY);
					
//					int addIndex = pageAr.indexOf(checkItem);
//					pageAr.add(addIndex, argoItem);
					pageAr.remove(checkItem);
				}
//				HashMap<String, Object> repeatObj = new HashMap<String, Object>();
//				repeatObj.put("item", _propList);
//				repeatObj.put("datatext", getRepeatValueCheckStr(currentItemData, _propList.get("text").toString(), _dataSetName, _cRowIndex)  );
//				repeatValueCheckMap.get(_itemId).add(repeatObj); 
				
			}
			
			if( repeatItem.get(key).size() > 1 && repeatItem.get(key).get(repeatItem.get(key).size() -1).get("datatext").toString().equals(repeatItem.get(key).get(repeatItem.get(key).size() -2).get("datatext").toString()) )
			{
				_resultFlag = true;
			}
			
		}
		
		return _resultFlag;
	}
	
	
	private void convertTableMapToApprovalTbl( ArrayList<HashMap<String, Value>> arraylist, HashMap<String, BandInfoMapData> bandInfoData, ArrayList<Integer> _xArr )
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
			dataCnt = DataSet.get(_dataSet).size();
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
		
		if( _xArr.indexOf( Math.round(_firstPosition) ) == -1 )
		{
			_xArr.add(Math.round(_firstPosition ) );
		}
		
		float _addPosition = _firstPosition;
		HashMap<String, Value> cloneItems = null;
		
		for (int i = 1; i < cnt; i++) {
			if(i == 1)
			{
				arraylist.get(i).put("x", new Value( _firstPosition, "number") );
				bandInfoData.get(bandName).getChildren().add( arraylist.get(i) );
				
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
						cloneItems.put("text", new Value( DataSet.get(_dataSet).get(j).get(cloneItems.get("column").getStringValue()), "string" ) );
					}
					
					// 보더 업데이트를 위하여 오른쪽보더를 제거
					if(j < dataCnt-1)
					{
						String borderSide = cloneItems.get("borderSide").getStringValue();
						ArrayList<String> borderType = (ArrayList<String>) cloneItems.get("borderTypes").getArrayStringValue().clone();
						String[] _sideAr = borderSide.split(",");
						borderSide = "";
						if( !isExportType.equals("PPT") )
						{
							for (int k = 0; k < _sideAr.length; k++) {
								if( "right".equals( _sideAr[k] ) )
								{
									borderType.set(k, "none");
								}
							}
						}
						
						cloneItems.put("borderTypes", new Value( borderType, "arraystr" ) );
					}
					
					cloneItems.put("x", new Value( _addPosition, "number") );
					bandInfoData.get(bandName).getChildren().add( cloneItems );
					
					if( _xArr.indexOf( Math.round(_addPosition) ) == -1 )
					{
						_xArr.add(Math.round(_addPosition));
					}
					_addPosition = _addPosition + cloneItems.get("width").getIntegerValue();
					if( _xArr.indexOf( Math.round(_addPosition) ) == -1 )
					{
						_xArr.add(Math.round(_addPosition ) );
					}
					
				}
			}
		}
		
		
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
	
	/**
	private void convertSvgRichText( String _data , float[] _height )
//	private void convertSvgRichText( String _data , float[] _height , String _fontFamily, int _fontSize )
	{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SvgRichTextObjectHandler svgObjhandler;
		try {
			SAXParser saxParser = factory.newSAXParser();
			
	        svgObjhandler = new SvgRichTextObjectHandler();
	        svgObjhandler.init(725, 320, "돋움", 10, 10, true);
	         
			saxParser.parse(_data, svgObjhandler);
	        svgTable svgTbl = svgObjhandler.getSvgTableObject();
	        
	        System.out.println("\n\nSVG2:row count=[" + svgObjhandler.getRowCount() + "]");
	        System.out.println("SVG2=[" + svgObjhandler.getSvg() + "]");
	        
	        
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	*/
	
	
	private ArrayList<HashMap<String, Object>> getBandRequiredItems( int _startIndex, int _lastIndex, BandInfoMapData _bandInfo, ArrayList<HashMap<String, Object>> _list)
	{
		// 밴드에 필수값 목록이 있을경우
		for(int _dsRowLength = _startIndex; _dsRowLength  < _lastIndex; _dsRowLength++ )
		{
			for( int _subIx = 0; _subIx < _bandInfo.getRequiredItems().size(); _subIx++ )
			{
				HashMap<String, Object> _currItem = new HashMap<String, Object>();
				if( _bandInfo.getRequiredItems().get(_subIx).equals("") == false )
				{
					String _item_req_id = _bandInfo.getRequiredItems().get(_subIx) + "_"+ _bandInfo.getId() + "_ROW"+_dsRowLength;
					_currItem.put("id", _item_req_id );
					
					_list.add(_currItem);
				}
			}
		}
		
		return _list;
	}
	
	private ArrayList<HashMap<String, Object>> getBandTabIndexItems( int _startIndex, int _lastIndex, BandInfoMapData _bandInfo, ArrayList<HashMap<String, Object>> _list)
	{
		// 밴드에 필수값 목록이 있을경우
		for(int _dsRowLength = _startIndex; _dsRowLength  < _lastIndex; _dsRowLength++ )
		{
			for( int _subIx = 0; _subIx < _bandInfo.getTabIndexItem().size(); _subIx++ )
			{
				// childItem을 뒤져서 requestID, tabIndexID를 처리 ( 페이지별로 담기 )
				
				HashMap<String, Object> _currItem = new HashMap<String, Object>();
				if( _bandInfo.getTabIndexItem().get(_subIx).equals("") == false )
				{
					String _item_req_id = _bandInfo.getTabIndexItem().get(_subIx) + "_"+ _bandInfo.getId() + "_ROW"+_dsRowLength;
					_currItem.put("id", _item_req_id );
					_list.add(_currItem);
				}
			}
		}
		
		return _list;
	}
	
	private ArrayList<HashMap<String, Object>> getBandRequiredItems( int _startIndex, int _lastIndex, BandInfoMapDataSimple _bandInfo, ArrayList<HashMap<String, Object>> _list)
	{
		// 밴드에 필수값 목록이 있을경우
		for(int _dsRowLength = _startIndex; _dsRowLength  < _lastIndex; _dsRowLength++ )
		{
			for( int _subIx = 0; _subIx < _bandInfo.getRequiredItems().size(); _subIx++ )
			{
				HashMap<String, Object> _currItem = new HashMap<String, Object>();
				if( _bandInfo.getRequiredItems().get(_subIx).equals("") == false )
				{
					String _item_req_id = _bandInfo.getRequiredItems().get(_subIx) + "_"+ _bandInfo.getId() + "_ROW"+_dsRowLength;
					_currItem.put("id", _item_req_id );
					
					_list.add(_currItem);
				}
			}
		}
		
		return _list;
	}
	
	private ArrayList<HashMap<String, Object>> getBandTabIndexItems( int _startIndex, int _lastIndex, BandInfoMapDataSimple _bandInfo, ArrayList<HashMap<String, Object>> _list)
	{
		// 밴드에 필수값 목록이 있을경우
		for(int _dsRowLength = _startIndex; _dsRowLength  < _lastIndex; _dsRowLength++ )
		{
			for( int _subIx = 0; _subIx < _bandInfo.getTabIndexItem().size(); _subIx++ )
			{
				// childItem을 뒤져서 requestID, tabIndexID를 처리 ( 페이지별로 담기 )
				
				HashMap<String, Object> _currItem = new HashMap<String, Object>();
				if( _bandInfo.getTabIndexItem().get(_subIx).equals("") == false )
				{
					String _item_req_id = _bandInfo.getTabIndexItem().get(_subIx) + "_"+ _bandInfo.getId() + "_ROW"+_dsRowLength;
					_currItem.put("id", _item_req_id );
					_list.add(_currItem);
				}
			}
		}
		
		return _list;
	}
	
	
	public void getExcelBandData(  Object _page, HashMap<String, ArrayList<HashMap<String, Object>>> _data,float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr, Object _allPageList, HashMap<String, Object> _param  ) throws UnsupportedEncodingException, ScriptException
	{
		HashMap<String, String> originalDataMap = new HashMap<String, String>();
		Boolean repeatPageHeader = false;
		Boolean repeatPageFooter = false;
		NodeList _propertys;
//			NodeList _child = _page.getElementsByTagName("item");
		ArrayList<BandInfoMapData> bandList = new ArrayList<BandInfoMapData>();
		HashMap<String, BandInfoMapData> bandInfoData = new HashMap<String, BandInfoMapData>();
		DataSet = _data;
		String groupName = "";
		String prevHeaderBandName = "";
		String _dataBandName = "";
		String _summeryBandName = "";
		String _itemId = "";
		String _className = "";
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		
		mPageHeight = pageHeight;
		mPageWidth = pageWidth;
		
		ArrayList<String> _grpSubDataNameAr = new ArrayList<String>();
		ArrayList<ArrayList<String>> groupDataNamesAr = new ArrayList<ArrayList<String>>();
		ArrayList<Element> _child = null;
		
		NodeList _allPageNodes = null;
		ArrayList<Element> _allPageElements = null;
		
		if( _allPageList != null && _page instanceof Element )
		{
			// 총 페이지에서 현재 페이지의 인덱스를 구한후 이후 페이지중 Continue값이 연속되는 페이지의 경우 _child에 추가 시키는 작업 진행
			int _m = 0;
			int _max = 0;
			int _pageIdx = -1;
			int _maxIdx = -1;
			int _startIdx = 0;
			
			if( ((Element)_page).hasAttribute("isConnect") && ((Element)_page).getAttribute("isConnect") != null )
			{
				if(_allPageList instanceof NodeList)
				{
					_allPageNodes = (NodeList) _allPageList;
					_max = _allPageNodes.getLength();
					_startIdx = 0;
				}
				else
				{
					_allPageElements = (ArrayList<Element>) _allPageList;
					_max = _allPageElements.size();
					_startIdx = _allPageElements.indexOf(_page);
				}
				
				Element _tempElement = null;
				for ( _m = _startIdx;_m < _max; _m++) {
					if( _allPageNodes != null) _tempElement = (Element) _allPageNodes.item(_m);
					else if( _allPageElements != null ) _tempElement = _allPageElements.get(_m);
					if( _pageIdx == -1 )
					{
						if( _tempElement == _page)
						{
							_pageIdx = _m;
							_maxIdx  = _pageIdx;
						}
						
					}
					else
					{
						if(_tempElement.hasAttribute("isConnect") == false || _tempElement.getAttribute("isConnect") == null || !_tempElement.getAttribute("isConnect").equals("true") )
						{
							break;
						}
						else
						{
							_maxIdx = _maxIdx + 1;
						}
					}
				}
				
			}
			
			_child = new ArrayList<Element>();
			NodeList _nodes = ((Element) _page).getElementsByTagName("item");
			int _nodesLength = _nodes.getLength();
			for ( i = 0; i < _nodesLength; i++) {
				_child.add((Element) _nodes.item(i) );
			}
			
			if( _maxIdx > _pageIdx )
			{
				NodeList _subNodes = null;
				boolean _chkFlag = true;
				Element _tmpPageElement = null;
				
				for( _m = _pageIdx+1; _m < _maxIdx+1; _m++  )
				{
					
					if(_allPageNodes != null)_tmpPageElement = (Element) _allPageNodes.item(_m);
					else if(_allPageElements != null) _tmpPageElement = _allPageElements.get(_m);
					
					_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck( _data, _tmpPageElement, _param,mFunction);
					
					if(_chkFlag )
					{
						_subNodes = _tmpPageElement.getElementsByTagName("item");
						
						_nodesLength =  _subNodes.getLength();
						for ( i = 0; i < _nodesLength; i++) {
							_child.add((Element) _subNodes.item(i) );
						}
					}
					
				}
			}
			
		}
		else
		{
			// masterBand일경우 page Element가 아니라 아이템이 담긴 Array가 넘어오게됨
			_child = (ArrayList<Element>) _page;
			if( _child.get(0).getTagName().equals("page") )
			{
				Element _pageElement = _child.remove(0);
			}
			
		}
		
		ArrayList<BandInfoMapData> grpList = new ArrayList<BandInfoMapData>();
		
		Boolean _subFooterBandFlag = false;
		
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
		
		float headerBandHeight = 0f;
		ArrayList<String> bandSequenceAr = new ArrayList<String>();
		
		// 그룹밴드용 아이템처리
		int _grpDataIndex = 0;
		float _grpDefaultHeight = 0;
		float _grpHeight = 0;
		int _grpSubMaxLength = 0;
		float _pageWidth = pageWidth;
		
		String _grpDataName = "";
		String _grpSubDataName = "";
		String _grpSubBandName2 = "";
		String _grpSubBandName  = "";
		BandInfoMapData _subCloneBandData;
		String _subFooterBandStr = "";
		ArrayList<ArrayList<HashMap<String, Object>>> _grpSubDataListAr = new ArrayList<ArrayList<HashMap<String,Object>>>();
		
		// xml Item
		int _childSize = _child.size();
		for(i = 0; i < _childSize ; i++)
		{
			_childItem = (Element) _child.get(i);
	
			_itemId = _childItem.getAttribute("id");
			_className = _childItem.getAttribute("className");
			
			// xml의 모든 Band정보를 뽑아서 밴드별 정보를 담기
			if( _className.length() > 4 && _className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) {
					
				BandInfoMapData  bandData = new BandInfoMapData(_childItem);
				
				bandInfoData.put(bandData.getId(), bandData);	// 생성된 밴드 데이터를 ID를 Key값으로 하여 맵에 담아두기
				
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
					
				}
				else if(  _className.equals("UBDataFooterBand")  )
				{
					if(groupName.equals("") == false )
					{
						bandData.setGroupBand(groupName);
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
				}
				else if(  _className.equals("UBEmptyBand")  )
				{
					if(groupName.equals("") == false )
					{
						bandData.setGroupBand(groupName);
					}
				}
				else if(  _className.equals("UBGroupHeaderBand")  )
				{
					groupName = bandData.getId();
					prevHeaderBandName = "";
					
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
					
					ArrayList<Object> retData = GroupingDataSetProcess.changeGroupDataSet(DataSet, groupName, bandData.getDataSet(), bandData.getColumnAr().get(0), orderBy.get(0), bandData.getSort(), filterColAr, filterOperatorAr, filterTextAr, bandData.getOriginalOrder(), originalDataMap );
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
						if(bandData.getGroupHeader().equals("") == false  && bandData.getColumnsAR()!=null && bandData.getColumnsAR().size() > 0 && bandData.getColumnsAR().get(0).equals("") == false  )
						{
							bandInfoData.get(groupName).getFooterAr().set(bandInfoData.get(groupName).getColumnAr().indexOf(  bandData.getColumnsAR().get(0) ), bandData.getId());
						}
						else
						{
							bandInfoData.get(groupName).getFooterAr().set(0, bandData.getId());
						}
						bandData.setGroupHeader(groupName);
					}
					else
					{
						bandData.setGroupHeader("");
					}
					
					
				}
				else if(  _className.equals("UBCrossTabBand")  )
				{
					
					
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
				if(groupName.equals("") == false )
				{
					String nextChildClass = "";
//						if( i+1 < _child.getLength() )
					if( i+1 < _child.size() )
					{
//							Element nextItem = (Element) _child.item(i+1);
						Element nextItem = (Element) _child.get(i+1);
						nextChildClass = nextItem.getAttribute("className");
						
						
					}
							
					if( bandData.getClassName().equals("UBGroupFooterBand") && nextChildClass.equals("") == false  && nextChildClass.equals("UBGroupFooterBand") == false  )
					{
						_grpDataIndex 		= 0;
						_grpHeight 			= 0;
						_grpSubMaxLength 	= 0;
						_grpDataName 		= "";
						
						_grpSubDataNameAr = new ArrayList<String>();
						BandInfoMapData _bandInfoMapData = bandInfoData.get(groupName);
						int _bandInfoMapDataRowCount = _bandInfoMapData.getRowCount();
						// 그룹밴드의 그룹핑된 데이터의 건수만큼 돌아서 처리
						for ( _grpDataIndex = 0; _grpDataIndex < _bandInfoMapDataRowCount; _grpDataIndex++) {
							
							_grpHeight = 0f;
							
							_grpDataName = groupName +  "grp_" + _grpDataIndex + "_" + _bandInfoMapData.getDataSet();
							
							if( _bandInfoMapData.getColumnAr().size() > 1 )
							{
								_grpSubDataListAr = GroupingDataSetProcess.changeGroupDataSetSub(_data, groupName, _grpDataName, _bandInfoMapData.getColumnAr(), bandInfoData.get(groupName).getOrderBy(), _bandInfoMapData.getSort(), _bandInfoMapData.getOriginalOrder(), originalDataMap);
								_bandInfoMapData.setSubPage( String.valueOf( _grpSubDataListAr.size() ) );
								_grpSubMaxLength = _grpSubDataListAr.size();
							}
							else
							{
								_grpSubMaxLength = 1;
							}
							
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
												
												DataSet.put(_grpSubDataName, _grpSubDataListAr.get(j));
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
											
											_grpSubDataNameAr.add(_grpDataName);
											
											_grpHeight = _grpHeight + grpList.get(k).getHeight();
											
											
											_subCloneBandData = cloneBandInfoData(grpList.get(k), groupName, grpList.get(k).getId(), _grpDataName, _grpDataIndex );
											
											// 그룹헤더 밴드에서 헤더를 한번만 사용할경우 아래 IF문을 처리
											if( _subCloneBandData.getClassName().equals(BandInfoMapData.DATA_HEADER_BAND) && _bandInfoMapData.getHeaderVisibleDepth() > -1  &&  _bandInfoMapData.getColumnAr().size() > 1 )
											{
												String _tempGroupName = groupName;
												int _tempIndex = _bandInfoMapData.getHeaderVisibleDepth() + 1;
												for (int m = 0; m < _tempIndex; m++) {
													if( _bandInfoMapData.getColumnAr().size() > m )
													{
														_tempGroupName = _tempGroupName +"_$_";
														_tempGroupName = _tempGroupName + _grpSubDataListAr.get(j).get(0).get(_bandInfoMapData.getColumnAr().get(m)) ;
													}
												}
												_subCloneBandData.setUseHeaderBandGroupName( _tempGroupName + "_$_" + grpList.get(k).getId() );
												
											}
											else if( _subCloneBandData.getClassName().equals(BandInfoMapData.DATA_HEADER_BAND) && _bandInfoMapData.getHeaderVisibleDepth() == -2 )
											{
												_subCloneBandData.setUseHeaderBandGroupName( grpList.get(k).getId() );
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
									
									originalDataMap.put( String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ), bandInfoData.get(groupName).getDataSet() );
									
									bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
									bandList.add(_subCloneBandData);
								}
								
								
								if( _subFooterBandFlag && j == _grpSubMaxLength-1 && bandInfoData.get(groupName).getFooterAr().size() > 0 &&
										bandInfoData.get(groupName).getFooterAr().get(0).equals("") == false )
								{
									
									_grpSubBandName2 = "grp_" + _grpDataIndex + "_";
									_grpSubBandName = "grp_" + _grpDataIndex + "_" + bandInfoData.get(groupName).getFooterAr().get(0);
									_grpDataName = groupName + "grp_" + _grpDataIndex + "_" + bandInfoData.get(groupName).getDataSet();
									
									_subCloneBandData = cloneBandInfoData(bandInfoData.get( bandInfoData.get(groupName).getFooterAr().get(0) ), groupName, bandInfoData.get(groupName).getFooterAr().get(0), _grpDataName, _grpDataIndex);
									_subCloneBandData.setId(_grpDataName);
									
//										originalDataMap.put( String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ), bandInfoData.get(groupName).getDataSet() );
									originalDataMap.put( _grpDataName, bandInfoData.get(groupName).getDataSet() );
									
									bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
									bandList.add(_subCloneBandData);
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
						grpList = new ArrayList<BandInfoMapData>();
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
					
					bandList.add( bandData );
					
				}
				
				
				
			}
			
		}
		
		
		if( groupName.equals("") == false && grpList.size() > 0 )
		{
			_grpDefaultHeight = 0;
			_grpSubDataNameAr = new ArrayList<String>();
			BandInfoMapData _bandInfoMapData = bandInfoData.get(groupName);
			int _bandInfoMapDataRowCount = _bandInfoMapData.getRowCount();
			// 그룹밴드의 그룹핑된 데이터의 건수만큼 돌아서 처리
			for ( _grpDataIndex = 0; _grpDataIndex < _bandInfoMapDataRowCount; _grpDataIndex++) {
				
				_grpHeight = 0f;
				
				_grpDataName = groupName +  "grp_" + _grpDataIndex + "_" + _bandInfoMapData.getDataSet();
				
				if( _bandInfoMapData.getColumnAr().size() > 1 )
				{
					_grpSubDataListAr = GroupingDataSetProcess.changeGroupDataSetSub(_data, groupName, _grpDataName, _bandInfoMapData.getColumnAr(), _bandInfoMapData.getOrderBy(), _bandInfoMapData.getSort(), _bandInfoMapData.getOriginalOrder(), originalDataMap);
					_bandInfoMapData.setSubPage( String.valueOf( _grpSubDataListAr.size() ) );
					_grpSubMaxLength = _grpSubDataListAr.size();
				}
				else
				{
					_grpSubMaxLength = 1;
				}
				
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
									
									DataSet.put(_grpSubDataName, _grpSubDataListAr.get(j));
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
								
								_grpSubDataNameAr.add(_grpDataName);
								
								_grpHeight = _grpHeight + grpList.get(k).getHeight();
								
								
								_subCloneBandData = cloneBandInfoData(grpList.get(k), groupName, grpList.get(k).getId(), _grpDataName, _grpDataIndex );
								
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
						
						originalDataMap.put( String.valueOf( _grpSubDataListAr.get(j).get(0).get("dataSet") ), bandInfoData.get(groupName).getDataSet() );
						
						bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
						bandList.add(_subCloneBandData);
					}
					
					
					if( _subFooterBandFlag && j == _grpSubMaxLength-1 && bandInfoData.get(groupName).getFooterAr().size() > 0 &&
							bandInfoData.get(groupName).getFooterAr().get(0).equals("") == false )
					{
						
						_grpSubBandName2 = "grp_" + _grpDataIndex + "_";
						_grpSubBandName = "grp_" + _grpDataIndex + "_" + bandInfoData.get(groupName).getFooterAr().get(0);
						_grpDataName = groupName + "grp_" + _grpDataIndex + "_" + bandInfoData.get(groupName).getDataSet();
						
						_subCloneBandData = cloneBandInfoData(bandInfoData.get( bandInfoData.get(groupName).getFooterAr().get(0) ), groupName, bandInfoData.get(groupName).getFooterAr().get(0), _grpDataName, _grpDataIndex);
						_subCloneBandData.setId(_grpSubBandName);

						originalDataMap.put( _grpDataName, bandInfoData.get(groupName).getDataSet() );
						
						bandInfoData.put(_subCloneBandData.getId(), _subCloneBandData);
						bandList.add(_subCloneBandData);
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
			
		}
		
		
		
		
		// 생성된 밴드리스트를 이용하여 그룹핑 및 총 페이지 구하기
		
		
		HashMap<String, Value> itemProperty = new HashMap<String, Value>();
		HashMap<String, Value> tableMapProperty = new HashMap<String, Value>();
 		HashMap<String, Value> tableProperty = new HashMap<String, Value>();
		String _itemDataSetName = "";
		String _propertyName = "";
		String _propertyValue = "";
		String _propertyType = "";
		float _tableArogHeight = 0f;
		// page의 아이템들을 로드하여 각 아이템별 band명을 이용하여 밴드의 데이터셋과 row수를 구하기
		
//			for(i = 0; i < _child.getLength() ; i++)
		int colIndex = 0;
		int rowIndex = 0;
		float updateX = 0;
		float updateY = 0;
		int _childSize2 = _child.size();
		for(i = 0; i < _childSize2 ; i++)
		{
//				_childItem = (Element) _child.item(i);
			_childItem = (Element) _child.get(i);
	
			_itemId = _childItem.getAttribute("id");
			_className = _childItem.getAttribute("className");
			
			if( _className.equals("UBTable") || _className.equals("UBApproval"))
			{
				ArrayList<HashMap<String, Value>> _ubApprovalAr = null;
				String _includeLayoutType = "";
				ArrayList<Integer> _lastCellIdx = new ArrayList<Integer>();
				
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
				
				if( tableProperty.containsKey("includeLayoutType"))
				{
					_includeLayoutType = tableProperty.get("includeLayoutType").getStringValue();
				}
				
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
				ArrayList<Integer> _exColIdx = new ArrayList<Integer>();		
				ArrayList<Float> _newXValue = new ArrayList<Float>();//최종  컬럼의 x값
				ArrayList<Float> arTempTableRowH = new ArrayList<Float>();
				boolean  _ubFxChkeck = true;
				
				float _removeCellW = 0;
				float _convertW = 0;
				ArrayList<Float> _tblColumnWdithAr = new ArrayList<Float>();
				float _cellW = 0;
				
				float _pos = 0;
				float _mapW = 0;	// 테이블맵의 X좌료
				float _mapH = 0;	// 테이블 맵의 Y좌표
				
				if( _newTalbeFlag )
				{
					for ( k = 0; k < _tableMapsLength; k++) {
						
						_tableMapItem = (Element) _tableMaps.item(k);
						_tableMapDatas = _tableMapItem.getElementsByTagName("tableMapData");
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
								if(_ItemProperty.getParentNode().getNodeName().equals("tableMapData"))
								{
									_propertyName = _ItemProperty.getAttribute("name");
									_propertyValue = URLDecoder.decode(_ItemProperty.getAttribute("value"), "UTF-8") ;
									_propertyType = _ItemProperty.getAttribute("type");
									
									itemProperty.put(  _propertyName, new Value(_propertyValue, _propertyType));
								}
							}
							
							_tableMapRow.add(itemProperty);
							
							if( k == 0 )
							{
								// X좌표 담기 (소숫점 1자리까지만 )
//									_pos = (float) Math.floor( itemProperty.get("columnWidth").getIntegerValue()*10 ) /10 ;
								_pos = (float) Math.round( itemProperty.get("columnWidth").getIntegerValue()) ;
								_xAr.add( _mapW  );
								_mapW = _mapW + _pos;
							}
							
						}
						
						if( k == 0 )
						{
							_xAr.add( _mapW  );		// 마지막 Width 값 담기
						}
						
//							_pos = (float) Math.floor( itemProperty.get("rowHeight").getIntegerValue()*10 ) /10 ;
						_pos = (float) Math.round( itemProperty.get("rowHeight").getIntegerValue() ) ;
						_yAr.add( _mapH  );
						_mapH = _mapH + _pos;
						
						_allTableMap.add(_tableMapRow);
						arTempTableRowH.add(_pos);
						
					}
					
					
					tableProperty.put("tempTableRowHeight", new Value(arTempTableRowH));
					_yAr.add( _mapH  );		// 마지막 Height 값 담기
					
					//column visible 속성관련 추가 작업 ================================================================================================
					//1. 첫번째 Row에서 column visible 속성찾아 array 추가
					int _colSize = 0;
					if( _allTableMap.size() > 0 )
					{
						_colSize = _allTableMap.get(0).size();
					}
					for ( l = 0; l < _colSize; l++) {
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
						for ( l = 0; l < _colSize-1; l++) {
							for ( k = 1; k < _allTableMap.size(); k++) {					
								tableMapProperty = _allTableMap.get(k).get(l);
								if(!tableMapProperty.get("colSpan").getStringValue().equals("1") && _exColIdx.indexOf(l) != -1){
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
							
							if( _allTableMap.size() > 0 )
							{
								_colSize = _allTableMap.get(0).size();
							}
							
							for ( l = 0; l < _colSize; l++)
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
					
					for ( k = 0; k < _allTableMap.size(); k++) {	
						ArrayList<HashMap<String, Value>> _tableRow = _allTableMap.get(k);
						HashMap<String, Value> _tableMap = null;
						_lastCellIdx.add(0);
						
						for ( l = 0; l < _tableRow.size(); l++) {
							_tableMap = _tableRow.get(l);
							
							if(_tableMap.get("cell") != null  && ((Element) _tableMap.get("cell").getElementValue()).getElementsByTagName("property").getLength() > 0 )
							{
								if( _exColIdx.indexOf(l) == -1 && _tableMap.get("cell").getElementValue() != null )
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
					 
					if( _allTableMap.size() > 0 )
					{
						_colSize = _allTableMap.get(0).size();
					}
					 
					for ( l = 0; l < _colSize-1; l++) {
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
				
				for ( k = 0; k < _tableMapsLength; k++) {
					
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
					
					// column 값 처리 ( UBApproval 일경우 데이터셋의 row수만큼 컬럼을 증가 ) 
					
					_tableMapDatasLength = _tableMapDatas.getLength();
					for ( l = 0; l < _tableMapDatasLength; l++) {
						
						tableMapProperty = _allTableMap.get(k).get(l);
						
						itemProperty = new HashMap<String, Value>();
						
						_itemDataSetName = "";
						 
						if(_newTalbeFlag)
						{
							_cellItem = (Element) ((Element)_tableMapDatas.item(l)).getElementsByTagName("cell").item(0);
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
							
						}
						else
						{
							_cellItem = (Element) _tableMapDatas.item(l);
							itemProperty = _allTableMap.get(k).get(l);
							
							rowIndex =  k + Integer.valueOf( tableMapProperty.get("rowSpan").getStringValue() );
							colIndex = colIndex + Integer.valueOf( tableMapProperty.get("colSpan").getStringValue() );
						}
						
						boolean useRightBorder = false;
						boolean useBottomBorder = false;
						boolean useLeftBorder = false;
						boolean useTopBorder = false;
						
						if(l>0)useLeftBorder = true;
						if(k>0)useTopBorder = true;
						
						if(_newTalbeFlag)
						{
							if( _lastCellIdx.size() > 0 )
							{
								int _columnIndex = 0;
								
								if( _allTableMap.get(k).get(l).containsKey("columnIndex") ) _columnIndex = Integer.valueOf(_allTableMap.get(k).get(l).get("columnIndex").getStringValue());

								if( _lastCellIdx.get(k) == _columnIndex )
								{
									useRightBorder = true;
								}
							}
							else if( colIndex == _allTableMap.get(k).size() )
							{
								useRightBorder = true;
							}
//							if( colIndex == _tableMapDatas.getLength() ) useRightBorder = true;
							if( rowIndex == _tableMaps.getLength() ) useBottomBorder = true;
						}
						else
						{
							if( l+ tableMapProperty.get("colSpan").getIntValue() >= _tableMapDatas.getLength()-1 ) useRightBorder = true;
							if( k+ tableMapProperty.get("rowSpan").getIntValue() >= _tableMaps.getLength()-1 ) useBottomBorder = true;
						}
						
						String _rightBorderStr = "";
						String _bottomBorderStr = "";
						
						
						if(useRightBorder)
						{
							if(_newTalbeFlag) _rightBorderStr =  _allTableMap.get(k).get(_tableMapDatas.getLength()-1 ).get("borderString").getStringValue();
							else  _rightBorderStr =  tableMapProperty.get("borderString").getStringValue();
						}
						if(useBottomBorder)
						{
							if(_newTalbeFlag)_bottomBorderStr =  _allTableMap.get(_tableMaps.getLength()-1).get(l).get("borderString").getStringValue();
							else  _bottomBorderStr =  tableMapProperty.get("borderString").getStringValue();
						}
						
						itemProperty.put("ORIGINAL_TABLE_ID", new Value(_itemId,"string") );
						if(!(isExportType.equals("PPT"))){
							borderAr = ItemConvertParser.convertCellBorder( tableMapProperty.get("borderString").getStringValue(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr,useLeftBorder,useTopBorder,isExportType  );
						}
						else{
							borderAr = ItemConvertParser.convertCellBorderForPPT( tableMapProperty.get("borderString").getStringValue(), useRightBorder, useBottomBorder,_rightBorderStr,_bottomBorderStr );
						}
						
						
						// 아이템의 Height값이 다음 Row의 Y값보다 클경우 사이즈를 수정. ( 신규 테이블일경우에만 지정 ) - 이전 테이블의 경우 rowSpan값이 부정확하여 정확한 위치가 잡히지 않을수 있음.
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
									
									if( bandInfoData.containsKey(  tableProperty.get("band").getStringValue() ) )
									{
										float _chkBandH = bandInfoData.get( tableProperty.get("band").getStringValue()).getHeight();
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
						itemProperty.put("borderSide", 		Value.fromString( borderAr.get(0).toString() ) );
						itemProperty.put("borderTypes", 	Value.fromArrayString( (ArrayList<String>) borderAr.get(1) ));
						itemProperty.put("borderColors",  	Value.fromArrayString( (ArrayList<String>) borderAr.get(2) ));
						itemProperty.put("borderWidths",  	Value.fromArrayInteger( (ArrayList<Integer>) borderAr.get(3) ));
						itemProperty.put("borderColorsInt",  	Value.fromArrayInteger( (ArrayList<Integer>) borderAr.get(4) ));
						
						updateX = Float.valueOf(tableProperty.get("band_x").getStringValue()) + itemProperty.get("x").getIntegerValue();
						updateY = Float.valueOf(tableProperty.get("band_y").getStringValue()) + itemProperty.get("y").getIntegerValue();
						
						itemProperty.put(  "band_x", new Value( updateX, "string"));
						itemProperty.put(  "x", new Value( updateX, "string"));
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
							if( _itemDataSetName.equals("") == false  && _data.containsKey(_itemDataSetName) && _data.get(_itemDataSetName) != null && tableProperty.containsKey("band")  && bandInfoData.containsKey(tableProperty.get("band").getStringValue()) )
							{
								if( bandInfoData.get( tableProperty.get("band").getStringValue()).getDataSet().equals("") || _data.get( bandInfoData.get( tableProperty.get("band").getStringValue()).getDataSet() ).size() < _data.get(_itemDataSetName).size() )
								{
									bandInfoData.get(tableProperty.get("band").getStringValue()).setDataSet(_itemDataSetName);
									bandInfoData.get(tableProperty.get("band").getStringValue()).setRowCount(_data.get(_itemDataSetName).size());
								}
							}
							
							if(tableProperty.containsKey("band") && tableProperty.get("band").getStringValue()!="" && bandInfoData.containsKey(tableProperty.get("band").getStringValue()) )
							{
								bandInfoData.get(tableProperty.get("band").getStringValue()).getChildren().add(itemProperty);
								
								int _requiredItemIndex = OriginalRequiredItemList.indexOf(itemProperty.get("id").getStringValue());
								int _tabIndexItemIndex = OriginalTabIndexItemList.indexOf(itemProperty.get("id").getStringValue());
								
								if( _requiredItemIndex != -1 )
								{
									bandInfoData.get(tableProperty.get("band").getStringValue()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").getStringValue() );
								}
								
								if( _tabIndexItemIndex != -1 )
								{
									bandInfoData.get(tableProperty.get("band").getStringValue()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").getStringValue() );
								}
							}
							
						}
						
						if(l == 0 )
						{
							NodeList _rowHeightP = ((Element) _tableMapDatas.item(l)).getElementsByTagName("property");
							Element _rowHeightElement;
							for ( j = 0; j < _rowHeightP.getLength(); j++) {
								_rowHeightElement = (Element) _rowHeightP.item(j);
								if( _rowHeightElement.getAttribute("neme").equals("rowHeight") )
								{
									_tableArogHeight = Float.valueOf( _rowHeightElement.getAttribute("value") );
									break;
								}
							}
						}
						
					}
					
				}
				
				if(_className.equals("UBApproval") && _ubApprovalAr != null)
				{
					convertTableMapToApprovalTbl(_ubApprovalAr, bandInfoData, mXAr );
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
				
				
				if(itemProperty.containsKey("band") && itemProperty.get("band").getStringValue().equals("") == false   && itemProperty.get("band").getStringValue().equals("null") == false && bandInfoData.containsKey(itemProperty.get("band").getStringValue()) )
				{
					bandInfoData.get(itemProperty.get("band").getStringValue()).getChildren().add(itemProperty);
				}
				
			}
			
			
			
		}// 각각의 아이템들의 데이터셋정보를 밴드리스트에 맵핑하고 생성할 로우별 아이템을 children에 담기완료
		
		// 밴드의 데이터 숫자와 height 값을 이용하여 총 페이지수를 구하기

		ArrayList<Object> rowHeightListData =  makeRowHeightList( bandList, bandInfoData, defaultY, defaultY, pageHeight, _pageWidth, mXAr, originalDataMap);
		ArrayList<ArrayList<HashMap<String, Value>>> pagesCountList = (ArrayList<ArrayList<HashMap<String, Value>>>) rowHeightListData.get(0);
		HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) rowHeightListData.get(1);
		
		
		// 밴드별 row 를 이용하여 페이지별 아이템을 생성하여 리턴
		
		
		ArrayList<Object> _retAr = new ArrayList<Object>();
		
		_retAr.add(bandInfoData);		
		_retAr.add(bandList);			
		_retAr.add(pagesCountList);		
		_retAr.add(crossTabData);		// 크로스탭정보가 담긴 배열
		_retAr.add(originalDataMap);		// OrizinalDataSet정보를 담은 map
		_retAr.add(groupDataNamesAr);		// 각 그룹별 그룹핑된 데이터리스트 [ [ string, string ] ] 형태

		
		_retAr.add(rowHeightListData.get(2));		// 필수목록 페이지별 리스트
		_retAr.add(rowHeightListData.get(3));		// 탭인덱스 페이지별 리스트
		
	}
	
	
	
	
	////////// EXCEL 밴드타입 Export시  테스트
	/**
	 * functinoName	:	makeRowHeightListExcel</br>
	 * desc			:	각 페이지별로 화면에 표시된 밴드와 로우 카운트를 구하여 담기
	 * @param bandList
	 * @param currentY
	 * @param defaultY
	 * @param maxHeight
	 * @throws ScriptException 
	 */
	public ArrayList<Object> makeRowHeightListExcel( ArrayList<BandInfoMapData> bandList, HashMap<String, BandInfoMapData> bandInfoData, float currentY, float defaultY, float defaultHeight, float pageWidth, ArrayList<Integer> mXAr, HashMap<String, String> originalDataMap, float _defH ) throws ScriptException
	{
		boolean pageHeaderRepeat = false;	// 페이지 헤더 밴드의 repeat여부 체크
		boolean pageFooterRepeat = false;	// 페이지 푸터 밴드의 reepat여부 체크
		float pageHeaderHeight = 0f;		// 페이지 헤더 밴드의 height
		float pageFooterHeight = 0f;		// 페이지 푸터 밴드의 height
		float currnetItemY = 0f;			// 다음 아이템의 Y값을 담는 값
		float currentPageMaxHeight = 0f;	// 현재 페이지의 최대 Height값을 담기
		float repeatPageFooterHeight = 0f;	// 현재 페이지의 최대 Height값을 담기
		float maxHeight = defaultHeight;
		int i = 0;
		int j = 0;
		
		HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = null;
		
		ArrayList<String> pageHeaderList = new ArrayList<String>();
		ArrayList<String> pageFooterList = new ArrayList<String>();
		String bandID = "";
		String bandClassName = "";
		
		ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = new ArrayList<ArrayList<HashMap<String, Value>>>(); 	// 모든 페이지의 페이지별 밴드명과 row정보를 담기
		ArrayList<HashMap<String, Value>> pageRowList = new ArrayList<HashMap<String, Value>>();							// 한페이지별로 밴드명과 밴드 cnt값을 담아두기
		
		
		ArrayList<ArrayList<HashMap<String, Object>>> _requiredItemListPages = new ArrayList<ArrayList<HashMap<String, Object>>>();	// 페이지별로 필수 요소값을 가진 아이템의 ID를 담기
		ArrayList<ArrayList<HashMap<String, Object>>> _tabIndexItemListPages = new ArrayList<ArrayList<HashMap<String, Object>>>();	// 페이지별로 탭 Index를 가진 아이템의 순서대로 담기
		ArrayList<HashMap<String, Object>> _requiredItemList = new ArrayList<HashMap<String, Object>>();							// 한페이지의 필수 요소값을 가진 아이템의 ID를 담은 배열
		ArrayList<HashMap<String, Object>> _tabIndexItemList = new ArrayList<HashMap<String, Object>>();							// 한페이지의 TabIndex값을 가진 배열
		
		currentPageMaxHeight = defaultHeight;
		
		for ( i = 0; i < bandList.size(); i++ ) {
			
			bandClassName = bandList.get(i).getClassName();
			bandID = bandList.get(i).getId();
			
			if( bandInfoData.containsKey(bandID) == false ) continue;		// 밴드 정보에 해당 ID가 없을경우 continue 
			
			// band별 ubfx처리 ( visible값이 false일경우 continue시키고 밴드의 visible 속성을 false로 지정 )
			if( bandList.get(i).getVisible() != false && bandList.get(i).getUbFunction() != null && bandList.get(i).getUbFunction().size() > 0 )
			{
				boolean _isBandVisible = true;
				
				int _ubfxSize = bandList.get(i).getUbFunction().size();
				
				for (int k = 0; k < _ubfxSize; k++) {
					HashMap<String, String> _fxMap = bandList.get(i).getUbFunction().get(k);
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_fxMap.get("value") , 0,0,0, -1, -1, bandList.get(i).getDataSet());
					}else{
						_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(i).getDataSet() );
					}
					
					_fnValue = _fnValue.trim();
					
					if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
					{
						bandList.get(i).setVisible(false);
						
					    _isBandVisible = false;
						  
						break;
					}
				}
				if( !_isBandVisible ){ continue; }
			}
			else if( bandList.get(i).getVisible() == false  )
			{
				continue;
			}
			
			if( bandClassName.equals( BandInfoMapData.PAGE_HEADER_BAND ) )
			{
				
				pageHeaderList.add(bandID);			// 페이지 헤더의 repeat값이 Y일경우 repeatFlag값을 true로 지정하고 headerHeight값을 담기
				if( bandInfoData.get(bandID).getRepeat().equals("y") )
				{
					pageHeaderRepeat = true;
					pageHeaderHeight = pageHeaderHeight + bandInfoData.get(bandID).getHeight();
				}
				
				currnetItemY = currnetItemY + bandInfoData.get(bandID).getHeight();
			}
			else if( bandClassName.equals( BandInfoMapData.PAGE_FOOTER_BAND ) )
			{
				
				pageFooterHeight = pageFooterHeight + bandInfoData.get(bandID).getHeight();
				
				currentPageMaxHeight = maxHeight - pageFooterHeight;
				
				pageFooterList.add(bandID);
				if( bandInfoData.get(bandID).getRepeat().equals("y") )
				{
					pageFooterRepeat = true;
					repeatPageFooterHeight = repeatPageFooterHeight + bandInfoData.get(bandID).getHeight();
					maxHeight = currentPageMaxHeight;
				}
				
			}
			
		}
		// 페이지 헤더 와 푸터의 height값을 담아두기
		
		int _startIndex = 0;
		int _maxIndex = 0;
		int _dataCnt = 0;
		
		float _dataHeaderHeight = 0f;
		float _dataPageFooterHeight = 0f;
		String _checkNextBandClass = "";
		float _groupingFootHeight = 0;
		float _groupingNextPageH = 0;
		Boolean grpAutoHeight = false;
		Boolean firstBand = true;
		Boolean adjustableFlag = false;
		int _groupMaxRowCnt = 0;
		
		ArrayList<Float> _rowHeightAr = new ArrayList<Float>();		// adjustableHeight 각 Row별 Height값을 담아두는 배열
		HashMap<String, Value> bandPageInfo = new HashMap<String,Value>(); 
		float _chkRowHeight = 0;
		mFunction.setDatasetList(DataSet);
		mFunction.setOriginalDataMap(originalDataMap);
		
		// 밴드별 adjustableHeight값 업데이트
		float _chkAutherHeight = 0;
		for ( i = 0; i < bandList.size(); i++ ) {
			
			_chkAutherHeight = 0;
			
			if(bandList.get(i).getAutoTableHeight()) bandList.get(i).setResizeText(false);	
			
			if((bandList.get(i).getAutoTableHeight() || bandList.get(i).getAdjustableHeight()) && bandList.get(i).getResizeText() == false )
			{
				_rowHeightAr = new ArrayList<Float>();
				if(bandList.get(i).getClassName().equals(BandInfoMapData.DATA_BAND))
				{
					if( bandList.get(i).getHeaderBand().equals("") == false  )
					{
						_chkAutherHeight = _chkAutherHeight +  bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
					}
					if(bandList.get(i).getSummery().equals("") == false )
					{
						_chkAutherHeight = _chkAutherHeight +  bandInfoData.get( bandList.get(i).getSummery() ).getHeight();
					}
					
				}
				else
				{
					_chkAutherHeight = 0;
				}
				
				try {
					_dataCnt = DataSet.get(bandList.get(i).getDataSet() ).size();
				} catch (Exception e) {
					// TODO: handle exception
//					System.out.print(" ContinueBandParser 1028 line ");
				}
				
				if( bandList.get(i).getClassName().equals(BandInfoMapData.DATA_BAND) == false )
				{
					_dataCnt = 1;
				}
				
				ArrayList<HashMap<String , ArrayList<Float>>> _tableRowHeight = new ArrayList<HashMap<String , ArrayList<Float>>>();
				
				for ( j = 0; j <_dataCnt; j++) {

					if(bandList.get(i).getAutoTableHeight()){						
						
						HashMap<String , ArrayList<Float>> hmTableRowHeight = getRowAdjustableHeightArray(bandList.get(i), j, maxHeight - _chkAutherHeight-10, maxHeight - _chkAutherHeight-10, bandInfoData);
						
						ArrayList<Float> arTableRowHeight;
						
						Iterator<String> keys = hmTableRowHeight.keySet().iterator();
						float tempBandHeight = 0;
						while( keys.hasNext() ){		
							_chkRowHeight = 0;
							String key = keys.next();		
							arTableRowHeight = hmTableRowHeight.get(key);
							for(int v = 0; v<arTableRowHeight.size();v++){
								_chkRowHeight = _chkRowHeight + arTableRowHeight.get(v);
							}
							
							if(bandList.get(i).getGroupBand() == null || bandList.get(i).getGroupBand().equals("")){
								_chkRowHeight = _chkRowHeight + bandInfoData.get(bandList.get(i).getId().toString()).getTableBandY().get(key);
							}else{
								_chkRowHeight = _chkRowHeight + bandInfoData.get(bandList.get(i).getDefaultBand().toString()).getTableBandY().get(key);
							}
							
							//table이 여러개 인경우 height 값이 큰 table을 기준으로 band Height 를 설정한다.(위아래로 디자인 된 테이블은 고려하지 않는다.) 
							if(tempBandHeight>_chkRowHeight){
								_chkRowHeight = tempBandHeight;
							}
							tempBandHeight = _chkRowHeight;
						}					
						
						if( bandList.get(i).getHeight() > _chkRowHeight )
						{
							_chkRowHeight = bandList.get(i).getHeight();
						}
						
						_rowHeightAr.add(_chkRowHeight);	// band의 Row별 Height값을 담기
						_tableRowHeight.add(hmTableRowHeight);
					}else {
						_chkRowHeight = getRowAdjustableHeight(bandList.get(i), j, -1, -1, bandInfoData);
						
						if( bandList.get(i).getHeight() > _chkRowHeight )
						{
							_chkRowHeight = bandList.get(i).getHeight();
						}
						
						_rowHeightAr.add(_chkRowHeight);	// band의 Row별 Height값을 담기
					}
				}
				
				bandList.get(i).setAdjustableHeightListAr(_rowHeightAr);
				if(bandList.get(i).getAutoTableHeight()){
					bandList.get(i).setTableRowHeight(_tableRowHeight);				
				}
				
			}
		}
		
		for ( i = 0; i < bandList.size(); i++ ) {
			
			bandClassName = bandList.get(i).getClassName();
			bandID = bandList.get(i).getId();
			grpAutoHeight = false;
			adjustableFlag = false;
			_groupMaxRowCnt = 0;
			_rowHeightAr = new ArrayList<Float>();
			
			// band별 ubfx처리 ( visible값이 false일경우 continue시키고 밴드의 visible 속성을 false로 지정 )
			if( bandList.get(i).getVisible() != false && bandList.get(i).getUbFunction() != null && bandList.get(i).getUbFunction().size() > 0 )
			{
				boolean _isBandVisible = true;
				
				int _ubfxSize = bandList.get(i).getUbFunction().size();
				
				for (int k = 0; k < _ubfxSize; k++) {
					HashMap<String, String> _fxMap = bandList.get(i).getUbFunction().get(k);
					
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(i).getDataSet());
					}else{
						_fnValue = mFunction.function(_fxMap.get("value"),0,0,0, -1, -1, bandList.get(i).getDataSet() );
					}
					
					_fnValue = _fnValue.trim();
					
					if( "visible".equals( _fxMap.get("property")) && "false".equals(_fnValue) )
					{
						bandList.get(i).setVisible(false);
						
					    _isBandVisible = false;
						  
						break;
					}
				}
				
				if( !_isBandVisible ){ continue; }
			}
			else if( bandList.get(i).getVisible() == false )
			{
				continue;
			}
			
			// 페이지 헤더와 푸터는 모든 페이지정보를 만든후 add처리 ( repeat값에 따라 add위치를 지정해야함 )
			if( bandClassName.equals( BandInfoMapData.PAGE_HEADER_BAND ) ||  bandClassName.equals( BandInfoMapData.PAGE_FOOTER_BAND ) || 
					bandClassName.equals( BandInfoMapData.DATA_PAGE_FOOTER_BAND ) )
			{
				continue;
			}
			else
			{
				// 데이터 밴드일경우 Height값을 이용하여 페이지별로 화면에 표시할 아이템을 담아두기
				if( bandClassName.equals( BandInfoMapData.DATA_BAND ) && DataSet.containsKey(bandList.get(i).getDataSet()) )
				{
					try {
						_dataCnt = DataSet.get(bandList.get(i).getDataSet() ).size();
						
						if(bandList.get(i).getUseLabelBand())
						{
							_dataCnt = (int) Math.ceil( (float) _dataCnt / bandList.get(i).getLabelBandColCount() ); 
						}
						
						// 데이터 밴드의 Min Row 지정
						if(  bandList.get(i).getAutoHeight() == false && bandList.get(i).getMinRowCount() > 0 && bandList.get(i).getMinRowCount() > _dataCnt )
						{
							_dataCnt = bandList.get(i).getMinRowCount();
						}
						
					} catch (Exception e) {
						// TODO: handle exception
//						System.out.print(" ContinueBandParser 1603 line ");
					}
					
					if(_dataCnt < 1 ) continue;
					
					_startIndex = 0;
					_dataHeaderHeight = 0;
					_dataPageFooterHeight = 0;
					_groupingFootHeight = 0;
					
					if(bandList.get(i).getSummery().equals(""))
					{
						_dataPageFooterHeight = 0;
					}
					else
					{
						_dataPageFooterHeight = bandInfoData.get(bandList.get(i).getSummery()).getHeight();
					}
					
					if( bandList.get(i).getAdjustableHeight() || bandList.get(i).getAutoTableHeight() )
					{
						adjustableFlag = true;
					}
					
					int _lastCnt = 0;
					float _dataBandHeight = 0;
					int _grpPageCnt = 0;
					boolean chkAdjustablePageFlag = false;
					
					ArrayList<HashMap<String, Value>> chkBandList = new ArrayList<HashMap<String,Value>>();
					while( _dataCnt > 0 ){
						
						chkAdjustablePageFlag = false;
						
						if( _startIndex > 0 && bandList.get(i).getHeaderBand().equals("") == false )
						{
							bandPageInfo = new HashMap<String, Value>();
							bandPageInfo.put("startIndex", new Value( 0,"int"));
							bandPageInfo.put("lastIndex",  new Value( 1,"int"));
							bandPageInfo.put("y", new Value( currnetItemY,"number"));
							bandPageInfo.put("id", new Value( bandList.get(i).getHeaderBand(),"string"));
							bandPageInfo.put("gprCurrentPageNum", new Value( _grpPageCnt,"number"));
							
							if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName().equals("") == false )
							{
								bandPageInfo.put("groupName", new Value( bandInfoData.get( bandList.get(i).getHeaderBand() ).getUseHeaderBandGroupName(),"string"));
							}
							
							pageRowList.add(bandPageInfo);
							_dataHeaderHeight = bandList.get(i).getHeight();
							
							if(  bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeight() && bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().size() > 0 )
							{
								currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getAdjustableHeightListAr().get(0);
							}
							else
							{
								currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getHeaderBand() ).getHeight();
							}
							
							// tabIndex 담기
							if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getRequiredItems().size() > 0 )
							{
								_requiredItemList = getBandRequiredItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue() ,bandInfoData.get( bandList.get(i).getHeaderBand() ), _requiredItemList );
							}
							// 필수값 담기
							if( bandInfoData.get( bandList.get(i).getHeaderBand() ).getTabIndexItem().size() > 0 )
							{
								_tabIndexItemList = getBandTabIndexItems( bandPageInfo.get("startIndex").getIntValue(), bandPageInfo.get("lastIndex").getIntValue(),bandInfoData.get( bandList.get(i).getHeaderBand() ), _tabIndexItemList );
							}
							
							chkBandList.add(bandPageInfo);
						}
						
						_dataBandHeight = 0;
						if( adjustableFlag )
						{
							
							_rowHeightAr = bandList.get(i).getAdjustableHeightListAr();
							
							_maxIndex = 0;
							for ( j = _lastCnt; j < _rowHeightAr.size(); j++) {
								_maxIndex++;
								_lastCnt++;
								_dataBandHeight = _dataBandHeight + _rowHeightAr.get(j);
							}
							
							if( _rowHeightAr.size() < _startIndex + _dataCnt && _lastCnt == _rowHeightAr.size() )
							{
								_maxIndex = _dataCnt;
								chkAdjustablePageFlag = true;
							}
							
						}
						else
						{
							_dataBandHeight = _dataCnt*bandList.get(i).getHeight();
//							_maxIndex = (int) Math.floor( ( (currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY)/bandList.get(i).getHeight() ); 
							_maxIndex = _dataCnt; 
						}

						boolean chkNextPageFlag = false;
						
						if(  _dataCnt - _maxIndex <= 0 && ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY) - _dataBandHeight > 0 && _groupingFootHeight > ((currentPageMaxHeight-_dataPageFooterHeight)-currnetItemY) - _dataBandHeight )
						{
							chkNextPageFlag = true;
						}
						
						if( _maxIndex == _dataCnt && adjustableFlag && grpAutoHeight )
						{
							chkAdjustablePageFlag = true;
						}
						
						for ( j = 0; j < bandList.get(i).getSequence().size(); j++) {
							
							if(bandList.get(i).getSequence().get(j).equals("s"))
							{
								bandPageInfo = new HashMap<String, Value>();
								bandPageInfo.put("startIndex", new Value( 0,"int"));
								bandPageInfo.put("lastIndex", new Value( 1,"int"));

								bandPageInfo.put("summeryStartIndex", new Value( _startIndex,"int"));
								bandPageInfo.put("summeryEndIndex", new Value( _startIndex + _maxIndex,"int"));
								
								bandPageInfo.put("y", new Value( currnetItemY,"number"));
								bandPageInfo.put("id", new Value( bandList.get(i).getSummery(),"string"));
								bandPageInfo.put("gprCurrentPageNum", new Value( _grpPageCnt,"number"));
								pageRowList.add(bandPageInfo);
								
								_dataPageFooterHeight = bandInfoData.get( bandList.get(i).getSummery() ).getHeight();
								currnetItemY = currnetItemY + bandInfoData.get( bandList.get(i).getSummery() ).getHeight();
								
								chkBandList.add(bandPageInfo);
							}
							else 
							{
								bandPageInfo = new HashMap<String, Value>();
								bandPageInfo.put("startIndex", new Value( _startIndex,"int") );
								bandPageInfo.put("lastIndex", new Value(  _startIndex + _maxIndex,"int"));
								bandPageInfo.put("y", new Value( currnetItemY,"number"));
								bandPageInfo.put("id", new Value( bandList.get(i).getId(),"string"));
								// group Page 함수사용을 위하여 그룹별 groupCurrentPage값을 담기
								bandPageInfo.put("gprCurrentPageNum", new Value( _grpPageCnt,"number"));
								pageRowList.add(bandPageInfo);
								
								chkBandList.add(bandPageInfo);
								
								if( adjustableFlag )
								{
									if( (_startIndex+_maxIndex) > _rowHeightAr.size() )
									{
										_dataBandHeight = _dataBandHeight + ((_startIndex+_maxIndex)-_rowHeightAr.size() )* bandList.get(i).getHeight();
									}
									currnetItemY = currnetItemY + _dataBandHeight;
								}
								else 
								{
									currnetItemY = currnetItemY + (_maxIndex*bandList.get(i).getHeight());
								}
							}
							
						}
						
						if(_maxIndex == 0 ) _maxIndex = 1;
						
						currentPageMaxHeight = maxHeight;
						_dataCnt = _dataCnt - _maxIndex;
						_startIndex = _startIndex + _maxIndex;
						
						_grpPageCnt++;
					}
					
					// group Page 함수사용을 위하여 그룹별 groupTotalPage값을 담기
					if( chkBandList.size() > 0 )
					{
						for ( j = 0; j <chkBandList.size(); j++) {
							chkBandList.get(j).put("gprTotalPageNum", new Value( _grpPageCnt,"number"));
						}
					}
					
				}
				else if( bandClassName.equals( BandInfoMapData.CROSSTAB_BAND )  )
				{
					CrossTabBandParser mcrossTabBand = new CrossTabBandParser();
					
					mcrossTabBand.setExportData(isExportData);
					
					mcrossTabBand.setFunctionVersion(mFunction.getFunctionVersion());
					
					mcrossTabBand.setIsExcelOption(isExcelOption);
					mcrossTabBand.setIsExportType(isExportType);
					
					try {
						if( crossTabData == null )
						{
								crossTabData = new HashMap<String, ArrayList<ArrayList<HashMap<String,Value>>>>();
						}
						ArrayList<ArrayList<HashMap<String, Value>>> crossTabPages;
						
						if(bandList.get(i).getFileLoadType().equals(GlobalVariableData.M_FILE_LOAD_TYPE_JSON))
						{
							crossTabPages = mcrossTabBand.convertCrossTabObjecttoItem(bandList.get(i).getOriginalItemData(),  DataSet, pageWidth, defaultY + pageHeaderHeight, maxHeight, currnetItemY, mXAr );
						}
						else
						{
							crossTabPages = mcrossTabBand.convertCrossTabXmltoItem(bandList.get(i).getItemXml(),  DataSet, pageWidth, defaultY + pageHeaderHeight, maxHeight, currnetItemY, mXAr );
						}
						
						if( crossTabPages == null ) continue;
						
						float _crossTabMaxWidth = 0;
						
						
						crossTabData.put(bandList.get(i).getId(), crossTabPages );
						
						// 리턴받은 크로스탭 데이터를 이용하여 총 페이지에 맞춰서 크로스탭정보를 지정
						for ( j = 0; j < crossTabPages.size(); j++) {
							
							bandPageInfo = new HashMap<String, Value>();
							bandPageInfo.put("startIndex",new Value( j,"int") );
							bandPageInfo.put("lastIndex",new Value( j+1,"int") );
							bandPageInfo.put("y", new Value( currnetItemY,"number"));
							bandPageInfo.put("id", new Value(bandList.get(i).getId(),"string"));
//							bandPageInfo.put("crossTabStartIndex", new Value(pagesRowList.size()-j, "int"));
							bandPageInfo.put("crossTabStartIndex", new Value(0, "int"));
							pageRowList.add(bandPageInfo);
							
							if(mcrossTabBand.getMaxPageWidthAr().size() > 0 ) _crossTabMaxWidth = mcrossTabBand.getMaxPageWidthAr().get( j%mcrossTabBand.getMaxPageWidthAr().size() );
							
							if( bandList.get(i).getVisibleType().equals(BandInfoMapData.VISIBLE_TYPE_ALL) && _crossTabMaxWidth > bandList.get(i).getWidth() )
							{
								bandList.get(i).setWidth(_crossTabMaxWidth);
								bandPageInfo.put("MAX_PAGE_WIDTH", new Value( _crossTabMaxWidth, "number" ));
							}
							else
							{
								bandPageInfo.put("MAX_PAGE_WIDTH", new Value( bandList.get(i).getWidth(), "number" ));
							}
							
							currnetItemY = crossTabPages.get(j).get( crossTabPages.get(j).size()-1 ).get("y").getIntegerValue() + crossTabPages.get(j).get( crossTabPages.get(j).size()-1 ).get("height").getIntegerValue();
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					}
				}
				else
				{
					boolean _useBandFlag = true;
					
					if( bandList.get(i).getClassName().equals("UBDataHeaderBand") )
					{
						
						String _argoGroupName = "";
						if( bandList.get(i).getGroupBand().equals("") == false )
						{
							
						}
						
						float dataBandHeight = 0;
						float headerHeight = 0;
						if( bandList.get(i).getDataBand().equals("") == false )
						{
							if(bandInfoData.get( bandList.get(i).getDataBand() ).getAdjustableHeight() && bandInfoData.get( bandList.get(i).getDataBand() ).getResizeText() == false &&
									bandInfoData.get( bandList.get(i).getDataBand() ).getAdjustableHeightListAr().size() > 0 )
							{
								dataBandHeight = bandInfoData.get( bandList.get(i).getDataBand() ).getAdjustableHeightListAr().get(0);
							}
							else
							{
								dataBandHeight = bandInfoData.get( bandList.get(i).getDataBand() ).getHeight();
							}
							
							if( bandInfoData.get( bandList.get(i).getDataBand() ).getSummery().equals("") == false )
							{
								dataBandHeight = dataBandHeight + bandInfoData.get(  bandInfoData.get( bandList.get(i).getDataBand() ).getSummery() ).getHeight();
							}
							
						}
						if( bandList.get(i).getAdjustableHeight() && bandList.get(i).getAdjustableHeightListAr().size() > 0 )
						{
							headerHeight = bandList.get(i).getAdjustableHeightListAr().get(0);
						}
						else
						{
							headerHeight = bandList.get(i).getHeight();
						}
						
						if( bandList.get(i).getUseHeaderBandGroupName().equals("")==false )
						{
							for (HashMap<String, Value> _bandlist : pageRowList) {
								if( _bandlist.containsKey("groupName") && _bandlist.get("groupName").getStringValue().equals( bandList.get(i).getUseHeaderBandGroupName() ) )
								{
									_useBandFlag = false;
									break;
								}
							}
							
						}
						
					}
					
					// 헤더의 adjustableHeight값 업데이트
					if(_useBandFlag)
					{
						
						bandPageInfo = new HashMap<String, Value>();
						bandPageInfo.put("startIndex",new Value( 0,"int") );
						bandPageInfo.put("lastIndex",new Value( 1,"int") );
						bandPageInfo.put("y", new Value( currnetItemY,"number"));
						bandPageInfo.put("id", new Value(bandList.get(i).getId(),"string"));
						
						if( bandList.get(i).getClassName().equals("UBDataHeaderBand") && bandList.get(i).getUseHeaderBandGroupName().equals("") == false )
						{
							bandPageInfo.put("groupName", new Value(bandList.get(i).getUseHeaderBandGroupName(),"string"));
						}
						
						pageRowList.add(bandPageInfo);
						
						if(  (bandList.get(i).getAdjustableHeight() || bandList.get(i).getAutoTableHeight())  && bandList.get(i).getAdjustableHeightListAr().size() > 0 )
						{
							currnetItemY = currnetItemY + bandList.get(i).getAdjustableHeightListAr().get(0);
						}
						else
						{
							currnetItemY = currnetItemY + bandList.get(i).getHeight();
						}
						
					}
				}
			
				firstBand = false;
			}
			
		}
		
		if( pageRowList.size() > 0 )
		{
			pagesRowList.add(pageRowList);
		}
		
		if( pagesRowList.size() == 0 && ( pageHeaderList.size() > 0 || pageFooterList.size() > 0 )  )
		{
			pageRowList = new ArrayList<HashMap<String,Value>>();
			pagesRowList.add(pageRowList);
		}
		
		mFitOnePageHeight = currnetItemY;
		
		int maxCnt = 1;
		
		if( pageHeaderRepeat )
		{
			maxCnt = pagesRowList.size();
		}
		// 모드 밴드 완료후 페이지 헤더와 페이지 푸터를 각 페이지별로 Add
		float itemH = 0f;		//헤더가 2개 이상일경우 헤더별 Y값을 담기위하여 지정
		float _firstPageItemH = 0f;
		for ( i = 0; i < pageHeaderList.size(); i++) {
			
			for ( j = 0; j < maxCnt; j++) {
				
				if( j == 0 ||  bandInfoData.get(pageHeaderList.get(i)).getRepeat().equals("y") )
				{
					float _chkY = _firstPageItemH;
					if( j == 0 )
					{
						_chkY = _firstPageItemH;
					}
					else
					{
						_chkY = itemH;
					}
					
					bandPageInfo = new HashMap<String, Value>();
					bandPageInfo.put("startIndex",new Value( 0, "int") );
					bandPageInfo.put("lastIndex",new Value( 1, "int") );
					bandPageInfo.put("y", new Value( _chkY, "number"));
					bandPageInfo.put("id", new Value(pageHeaderList.get(i),"string"));
					
					pagesRowList.get(j).add(i, bandPageInfo);
				}
				else
				{
					break;
				}
			}
			
			if(bandInfoData.get(pageHeaderList.get(i)).getRepeat().equals("y") )
			{
				itemH = itemH + bandInfoData.get(pageHeaderList.get(i)).getHeight();			
			}
			_firstPageItemH = _firstPageItemH + bandInfoData.get(pageHeaderList.get(i)).getHeight();
		}		
		
		maxCnt = 1;
		if( pageFooterRepeat )
		{
			maxCnt = pagesRowList.size();
		}
		
		float argoFooterH = 0f;
		for ( i = 0; i < pageFooterList.size(); i++) {
			
			if( !isExcelOption.equals("BAND") &&  mFitOnePage && currnetItemY < mPageHeight ) itemH = _defH - pageFooterHeight;
			else itemH = currnetItemY;
			
			for ( j = 0; j < maxCnt; j++) {
				
				if( j == 0 ||  bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
				{
					bandPageInfo = new HashMap<String, Value>();
					bandPageInfo.put("startIndex",new Value( 0, "int") );
					bandPageInfo.put("lastIndex",new Value( 1, "int") );
					bandPageInfo.put("y", new Value( itemH, "number"));
					bandPageInfo.put("id", new Value(pageFooterList.get(i),"string"));
					
					if( !isExcelOption.equals("BAND") &&  mFitOnePage && currnetItemY < mPageHeight ) itemH = _defH - repeatPageFooterHeight - argoFooterH;
					else itemH = currnetItemY - repeatPageFooterHeight - argoFooterH;
					
					pagesRowList.get(j).add( bandPageInfo );
					
					if( j == 0 ) mFitOnePageHeight = mFitOnePageHeight + bandInfoData.get(pageFooterList.get(i)).getHeight();
				}
				else
				{
					break;
				}
			}
			
			if( bandInfoData.get(pageFooterList.get(i)).getRepeat().equals("y") )
			{
				argoFooterH = argoFooterH + bandInfoData.get(pageFooterList.get(i)).getHeight();			
			}
			
		}	
		
		if( mFitOnePage && mFitOnePageHeight < mPageHeight) mFitOnePageHeight = 0;
		
		ArrayList<Object> returnArrayList = new ArrayList<Object>();
		
		returnArrayList.add(pagesRowList);
		returnArrayList.add(crossTabData);

		returnArrayList.add(_requiredItemListPages);
		returnArrayList.add(_tabIndexItemListPages);
		
		return returnArrayList;
	}
	
	

	//
	public ArrayList<HashMap<String, Object>> createContinueBandItemsExcelList( Element _pageEl, int _page, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, BandInfoMapData> bandInfo, ArrayList<BandInfoMapData> bandList, 
			ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList , HashMap<String, Object> _param, HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum, boolean isPivot, ubFormToExcelBase _ubformExcel
			 , Workbook _wb , String _formName, boolean _isNewDoc, int _documentIdx ) throws UnsupportedEncodingException, ScriptException
	{
		
		int i = 0;
		int j = 0;
		
		String bandName = "";
		HashMap<String, Value> currentBandCntMap;
		HashMap<String, Value> currentItemData;
		String _itemId = "";
		String _className = "";
				
		String _dataSetName = "";
		
		float itemY = 0f;
		int _currRowIndex = 0;
		int _maxRowIndex = 0;
		ArrayList<HashMap<String, Value>> _child = null;
		String crossTabClassName = "UBLabelBorder";
		Boolean crossTabItems = false;
		
		DataSet = _dataSet;
		mParam = _param;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> repeatValueCheckMap = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		
		// dataConvertParser 생성
		ItemConvertParser dataItemParser = new ItemConvertParser(DataSet, mChartDataFileName, m_appParams);
		dataItemParser.setImageData(mImageData);
		dataItemParser.setChartData(mChartData);
		dataItemParser.setFunction(mFunction);
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		dataItemParser.setChangeItemList(changeItemList);
		
		// Group함수를 위한 객체
		dataItemParser.mOriginalDataMap = mOriginalDataMap;
		dataItemParser.mGroupDataNamesAr = mGroupDataNamesAr;
		
		boolean isExcelBandType = false;
		
		if( _ubformExcel != null ) isExcelBandType = true;
		
		ArrayList<HashMap<String, Object>> _lastAddItems = new ArrayList<HashMap<String, Object>>();
		
		dataItemParser.setIsExportType(isExportType);
		int _crossTabStartIndex = 0;
		
		float _addY = 0;
		
		// 페이지별로 x좌표업데이트
		ArrayList<Integer> _pageXAr = _ubformExcel.getDocumentXArrayOnePage( pagesRowList, bandInfo, DataSet, _pageXArr );
		_ubformExcel.getDocXArray().set(_documentIdx, _pageXAr);
		
		boolean _isFirst = true;
		
		for ( i = 0; i < pagesRowList.get(_page).size(); i++) {
			
			currentBandCntMap = pagesRowList.get(_page).get(i);				// 현재 화면에 Add할 아이템이 담긴 Band정보를 들고있는 객체
			bandName = currentBandCntMap.get("id").getStringValue();
			_dataSetName = "";
			crossTabItems = false;
			_crossTabStartIndex = 0;
			
			repeatValueCheckMap = new HashMap<String, ArrayList<HashMap<String, Object>>>();
			
			if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.CROSSTAB_BAND) )
			{
				if( crossTabData.containsKey(bandName) )
				{
					if(currentBandCntMap.containsKey("crossTabStartIndex"))
					{
						_crossTabStartIndex = currentBandCntMap.get("crossTabStartIndex").getIntegerValue().intValue();
					}
					_child = crossTabData.get(bandName).get(_page - _crossTabStartIndex);
					crossTabItems = true;
				}
			}
			else
			{
				if( bandInfo.get(bandName).getDefaultBand().equals(""))
				{
					_child = bandInfo.get(bandName).getChildren();
				}
				else
				{
					_child = bandInfo.get( bandInfo.get(bandName).getDefaultBand() ).getChildren();
					_dataSetName = bandInfo.get(bandName).getDataSet();
				}
			}
			
			
			_currRowIndex = (int) currentBandCntMap.get("startIndex").getIntValue();
			_maxRowIndex =  (int)  currentBandCntMap.get("lastIndex").getIntValue();
			
			_addY = currentBandCntMap.get("y").getIntegerValue();
			
			// Group함수의 페이징함수를 위한 값을 담아두기
			if( currentBandCntMap.containsKey("gprCurrentPageNum") && currentBandCntMap.get("gprCurrentPageNum") != null )
			{
				dataItemParser.mGroupCurrentPageIndex = (int) currentBandCntMap.get("gprCurrentPageNum").getIntValue();
			}
			
			if( currentBandCntMap.containsKey("gprTotalPageNum") && currentBandCntMap.get("gprTotalPageNum") != null )
			{
				dataItemParser.mGroupTotalPageIndex = (int) currentBandCntMap.get("gprTotalPageNum").getIntValue();
			}
			
			// 밴드에 useLabelBand속성이 true일경우 수만큼 반복하도록 지정
			int _labelBandCnt = 1;	
			boolean _useLabelBand = bandInfo.get(bandName).getUseLabelBand();
			int _dataIdx = 0;
			float _addLabelPosition = 0;
			float _labelBandWidth = bandInfo.get(bandName).getWidth();
			
			if( _useLabelBand )
			{
				_labelBandCnt = bandInfo.get(bandName).getLabelBandColCount();
			}
			
			for (int _cRowIndex = _currRowIndex; _cRowIndex < _maxRowIndex; _cRowIndex++) {
				
				// xml Item
				for( j = 0; j < _child.size() ; j++){
					// labelBand 반복된 수만큼 반복
					for (int _lbIdx = 0; _lbIdx < _labelBandCnt; _lbIdx++) {
						
						currentItemData = _child.get(j);
						
						_dataIdx = _cRowIndex;
						// 라벨밴드의 경우 하나의 밴드에 여러건의 데이터가 표현됨
						if(_useLabelBand){
							if( "horizontal".equals( bandInfo.get(bandName).getLabelBandDirection() ) )
							{
								_dataIdx = ( _cRowIndex * _labelBandCnt ) + _lbIdx ;
							}
							else
							{
								_dataIdx = (_currRowIndex*_labelBandCnt) +  (_cRowIndex-_currRowIndex) +  ( (_maxRowIndex-_currRowIndex) * _lbIdx);
							}
							
							
							float _displayWidth=bandInfo.get(bandName).getLabelBandDisplayWidth();
							if( _displayWidth != 0 ){
								_addLabelPosition = (float) Math.floor( ( _displayWidth / _labelBandCnt) * _lbIdx );
							}else{
								_addLabelPosition = (float) Math.floor( (_labelBandWidth / _labelBandCnt) * _lbIdx );
							}
							
							if( BandInfoMapData.DATA_BAND.equals(bandInfo.get(bandName).getClassName()) &&  "".equals(bandInfo.get(bandName).getDataSet()) ==false && !bandInfo.get(bandName).getAutoHeight() && DataSet.get(bandInfo.get(bandName).getDataSet() ).size() <= _dataIdx )
							{
								continue;
							}
						}
						
						
						if( currentItemData == null ) continue;
						if( currentItemData.containsKey("id") )_itemId = currentItemData.get("id").getStringValue();
						
						if( crossTabItems )
						{
							// crossTab아이템일경우 클래스명 변경
							_className = crossTabClassName;
							currentItemData.put("className", Value.fromString(crossTabClassName));
						}
						else
						{
							_className = currentItemData.get("className").getStringValue();
						}
						
						//테스트 
						HashMap<String, Object> _propList = new HashMap<String, Object>();
						
						// 
//						if(bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_BAND) )
						if( _className.equals("UBTemplateArea") && mTempletInfo != null && mTempletInfo.containsKey(_itemId) )
						{
							_objects = mTempletInfo.get(_itemId).convertItemData(_dataIdx, _cloneX, _cloneY + currentBandCntMap.get("y").getIntegerValue(), _objects, mFunction, bandInfo.get(bandName), _currentPageNum, _totalPageNum, -1, -1, null, currentBandCntMap, dataItemParser);
							continue;
						}
						else
						{
							if(bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_PAGE_FOOTER_BAND) )
							{
								int _summeryStartIndex = (int) currentBandCntMap.get("summeryStartIndex").getIntValue();
								int _summeryEndIndex = (int) currentBandCntMap.get("summeryEndIndex").getIntValue();
								
								if(currentItemData.containsKey("LOAD_TYPE") && currentItemData.get("LOAD_TYPE").getIntValue().equals(ProjectInfo.LOAD_TYPE_JSON) )
								{
									_propList = dataItemParser.convertItemDataJson( bandInfo.get(bandName), currentItemData, DataSet, _dataIdx, _param, _summeryStartIndex, _summeryEndIndex,_totalPageNum,_currentPageNum, _dataSetName,0, 0,  -1);
								}
								else
								{
									_propList = dataItemParser.convertItemData(bandInfo.get(bandName), currentItemData, _dataSetName, _dataIdx, _param, _summeryStartIndex, _summeryEndIndex,_totalPageNum,_currentPageNum);
								}
								
							}
							else
							{
								if(currentItemData.containsKey("LOAD_TYPE") && currentItemData.get("LOAD_TYPE").getIntValue().equals(ProjectInfo.LOAD_TYPE_JSON) )
								{
									_propList = dataItemParser.convertItemDataJson(bandInfo.get(bandName), currentItemData, DataSet, _dataIdx, _param, -1, -1 , _totalPageNum , _currentPageNum, _dataSetName,0, 0, -1);
								}
								else
								{
									_propList = dataItemParser.convertItemData(bandInfo.get(bandName), currentItemData, _dataSetName, _dataIdx, _param, -1, -1 , _totalPageNum , _currentPageNum);
								}
								
							}
						}
						
						// 아이템이 null일경우 add시키지 않음 2015-12-02
						if( _propList == null ) continue;
						
						if( crossTabItems == false  )
						{
							if(bandInfo.get(bandName).getAutoTableHeight()){
								float bandY = currentItemData.get("band_y").getIntegerValue();
								float currTableRowY = 0;
															
								//bandInfo.get(bandName).getTableRowHeight();	
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									itemY = currentBandCntMap.get("y").getIntegerValue() +  getCurrentYpositionBand(bandInfo.get(bandName).getAdjustableHeightListAr(), _cRowIndex, currentBandCntMap.get("startIndex").getIntValue() , bandInfo.get(bandName));
									
									if(!currentItemData.containsKey("isCell")){
										if(currentItemData.containsKey("tableId")){
											String tableId = currentItemData.get("tableId").getStringValue();
											int cellRowIndex = currentItemData.get("cellRowIndex").getIntValue();
											float cellPadding = 0;
											
											int _cellRowSpan = -1;
											float _cellRowHeight = 0;
											
											if( currentItemData.containsKey("cellRowSpan"))
											{
												_cellRowSpan = currentItemData.get("cellRowSpan").getIntValue();
												
												if(_cellRowSpan > 1){
													for(int ii = cellRowIndex; ii < cellRowIndex + _cellRowSpan; ii++ ){
														_cellRowHeight = _cellRowHeight +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(ii);			
													}										
												}else{
													_cellRowHeight = bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(cellRowIndex);
												}
												
												_cellRowHeight = _cellRowHeight / 2 - ( currentItemData.get("height").getIntegerValue() / 2 ) ;
												
											}
											
											if(bandInfo.get(bandName).getGroupBand().equals("") == false )
											{
												cellPadding = currentItemData.get("cellPadding").getIntegerValue() +  bandInfo.get( bandInfo.get(bandName).getDefaultBand() ).getTableBandY().get(tableId);									
											}
											else
											{
												cellPadding = currentItemData.get("cellPadding").getIntegerValue() +  bandInfo.get( bandName ).getTableBandY().get(tableId);
											}
											
											
											for(int ii = 0; ii < cellRowIndex; ii++ ){
												currTableRowY = currTableRowY +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(ii) + 1;			
											}											
											currTableRowY = currTableRowY + cellPadding + _cellRowHeight ;																				
											
										}else{
											currTableRowY = currentItemData.get("band_y").getIntegerValue();
										}
										
									}else{
										
										int currTableRowIdx = currentItemData.get("rowIndex").getIntValue();
										int currTableRowSpan = currentItemData.get("rowSpan").getIntValue();
										float currTableRowHeight = 0;								
										String orgTableId = currentItemData.get("ORIGINAL_TABLE_ID").getStringValue();								
										
										if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
										{
											if(bandInfo.get(bandName).getGroupBand().equals("") == false )
											{
												currTableRowY =  bandInfo.get(bandInfo.get(bandName).getDefaultBand()).getTableBandY().get(orgTableId);
											}
											else
											{
												currTableRowY =  bandInfo.get(bandName).getTableBandY().get(orgTableId);
											}
											
											for(int ii = 0; ii < currTableRowIdx; ii++ ){
												currTableRowY = currTableRowY +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(ii);			
											}	
											
											if(currTableRowSpan > 1){
												for(int ii = currTableRowIdx; ii < currTableRowIdx + currTableRowSpan; ii++ ){
													currTableRowHeight = currTableRowHeight +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(ii);			
												}										
											}else{
												currTableRowHeight = bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(currTableRowIdx);
											}	
											_propList.put("height",currTableRowHeight);
										}
										else
										{
											currTableRowY = currentItemData.get("y").getIntegerValue();
										}
									}
								}
								else
								{
									currTableRowY = currentItemData.get("band_y").getIntegerValue();								
									// 일반 아이템의 y좌표는 인덱스*밴드height+ 아이템의 band_y값을 이용해서 이동 ( resizedHeight값이 잇는 아이템은 height별로 따로 처리 )
									itemY = currentBandCntMap.get("y").getIntegerValue()+ ( bandInfo.get(currentBandCntMap.get("id").getStringValue()).getHeight() * ( _cRowIndex - currentBandCntMap.get("startIndex").getIntValue() ) );
								}
								
								itemY = itemY + currTableRowY;
							}else {
								
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									itemY = currentBandCntMap.get("y").getIntegerValue() +  getCurrentYpositionBand(bandInfo.get(bandName).getAdjustableHeightListAr(), _cRowIndex, currentBandCntMap.get("startIndex").getIntValue() , bandInfo.get(bandName));
									if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
									{
										_propList.put("height", bandInfo.get(bandName).getAdjustableHeightListAr().get(_cRowIndex) - currentItemData.get("band_y").getIntegerValue() );
									}
								}
								else
								{
									// 일반 아이템의 y좌표는 인덱스*밴드height+ 아이템의 band_y값을 이용해서 이동 ( resizedHeight값이 잇는 아이템은 height별로 따로 처리 )
									itemY = currentBandCntMap.get("y").getIntegerValue()+ ( bandInfo.get(currentBandCntMap.get("id").getStringValue()).getHeight() * ( _cRowIndex - currentBandCntMap.get("startIndex").getIntValue() ) );
								}
								
								itemY = itemY + currentItemData.get("band_y").getIntegerValue();
							}
							
							itemY = ((float) Math.round(  itemY*100) / 100);
							_propList.put("y", itemY + _cloneY );
							
							if( _propList.get("x") != null ){
								_propList.put("x", Float.valueOf(_propList.get("x").toString()) + _cloneX + _addLabelPosition );
							}
							
							if(_propList.containsKey("id") && _propList.get("id").equals("") == false )_itemId =  _propList.get("id").toString();
							
							_propList.put("className" , _className );
							_propList.put("id" , _itemId );
							
							if( currentItemData.containsKey("realClassName") && "TABLE".equals(currentItemData.get("realClassName").getStringValue() ) )
							{
								if( _itemId.equals("") )_propList.put("id", "TB_" + _cRowIndex + "_" + _itemId);
								
								// Export시 테이블로 내보내기 위한 작업
								_propList.put("isTable", "true" );
								_propList.put("TABLE_ID", bandName + "_" + currentItemData.get("realTableID").getStringValue() );	// 테이블아이템생성을 위한 밴드명+테이블 id를 담아둔다(2016-03-07)
								// 아이템의 Height값이 밴드보다 클경우 밴드의 사이즈와 동일하게 맞추도록 수정
								
								// cell 의 over만큼 값을 담기
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
								
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									_propList.put("cellOutHeight", 0 );
								}
								
								if( currentItemData.containsKey("repeatedValue") && currentItemData.get("repeatedValue").getBooleanValue() )
								{
									_propList.put( "repeatedValue", currentItemData.containsKey("repeatedValue"));
								}
								
							}
							
							if( currentItemData.containsKey("repeatedValue") && currentItemData.get("repeatedValue").getBooleanValue() )
							{
								String chkDataID = "";
								if( _dataSetName.equals("") && !currentItemData.get("dataSet").getStringValue().equals("") && !currentItemData.get("dataSet").getStringValue().equals("null") )
								{
									chkDataID =  currentItemData.get("dataSet").getStringValue();
								}
								else
								{
									chkDataID = _dataSetName;
								}
								
								if( bandInfo.get(bandName).getDataSet().equals("") == false && DataSet.containsKey(chkDataID) == false )
								{
									chkDataID = bandInfo.get(bandName).getDataSet();
								}
								
								if( DataSet.containsKey(chkDataID) == false )
								{
									chkDataID = "";
								}
								
								if( ( currentItemData.get("dataType").getStringValue() != null && currentItemData.get("dataType").getStringValue().equals("0") ) || ( chkDataID.equals("") == false  && DataSet.containsKey(chkDataID) && DataSet.get(chkDataID).size() > _cRowIndex ) )
								{
									if( repeatValueCheckMap.containsKey(_itemId) == false ){ 
										repeatValueCheckMap.put(_itemId, new ArrayList<HashMap<String, Object>>());
									}
									
									HashMap<String, Object> repeatObj = new HashMap<String, Object>();
									repeatObj.put("item", _propList);
									repeatObj.put("datatext", getRepeatValueCheckStr(currentItemData, _propList.get("text").toString(), chkDataID, _cRowIndex)  );
									repeatValueCheckMap.get(_itemId).add(repeatObj);
								
									_propList.put("datatext",  getRepeatValueCheckStr(currentItemData, _propList.get("text").toString(), chkDataID, _cRowIndex));
								}
								
							}
							
							// isPivot값이 true일경우 
							if(isPivot)
							{
								float _argoX 		= Float.valueOf( String.valueOf(_propList.get("x")) );
								float _argoY 		= Float.valueOf( String.valueOf(_propList.get("y")) );
								float _argoWidth  	= Float.valueOf( String.valueOf(_propList.get("x2")) );
								float _argoHeight 	= Float.valueOf( String.valueOf(_propList.get("y2")) );
								
								_propList.put("x", _argoY);
								_propList.put("y", mPageWidth - _argoX - _argoWidth);
								_propList.put("width", _argoHeight);
								_propList.put("height", _argoWidth );
								
								
								if(_propList.containsKey("borderSide"))
								{
									//_propList의 border 업데이트( right : t->l,r->t,l->b,r->b ) 
									String borderSide = String.valueOf(_propList.get("borderSide"));
									borderSide = borderSide.replace("[", "").replace("]", "").replace(" ", ""); 
									
									String[] convertBorderSide = borderSide.split(",");
									ArrayList<String> newBorderSide = new ArrayList<String>();
									
									for (int k = 0; k < convertBorderSide.length; k++) {
										
										if(convertBorderSide[k].equals("top"))
										{
											newBorderSide.add("left");
										}
										else if(convertBorderSide[k].equals("left"))
										{
											newBorderSide.add("bottom");
										}
										else if(convertBorderSide[k].equals("right"))
										{
											newBorderSide.add("top");
										}
										else if(convertBorderSide[k].equals("bottom"))
										{
											newBorderSide.add("right");
										}
									}
									
									_propList.put("borderSide", newBorderSide);
								}
								
							}
							
						}
						else
						{
							_propList.put("className" , crossTabClassName );
						}
						
						_propList.put("top", _propList.get("y"));
						_propList.put("left", _propList.get("x"));
						
						//아이템의 id를 탭 인덱스에 사용된 id로 변경
						if(_propList.containsKey("TABINDEX_ID") && _propList.get("TABINDEX_ID").equals("")==false)
						{
							_propList.put("id", _propList.get("TABINDEX_ID"));
						}
						
						_objects.add(_propList);
					}
					// 라벨밴드 반복 종료
					
				}
				
				float _argoY = 0;
				_argoY = _addY;
				
				if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
				{
					_addY = _addY + bandInfo.get(bandName).getAdjustableHeightListAr().get(_cRowIndex);
				}
				else
				{
					_addY = _addY +  bandInfo.get(currentBandCntMap.get("id").getStringValue()).getHeight() ;
				}
				
				if(  isExcelBandType )
				{
					HashMap<String, Object> pageProp = new HashMap<String, Object>();
					ArrayList<ArrayList<HashMap<String, Object>>> _objectsAr = new ArrayList<ArrayList<HashMap<String, Object>>>();
					
					//band Type일때는 백그라운드 이미지를 내보내지 않는다
					_ubformExcel.setBackgroundImage(null);
					
					pageProp.put("cPHeight", _addY );
					pageProp.put("cPStartHeight", _argoY  );
					
					if( _isFirst ) pageProp.put("bPageHeight", 0 );
					
					_objects.add(pageProp);
					_objectsAr.add(_objects);
					try {
						_wb = _ubformExcel.xmlParsingExcel(_objectsAr, _wb, _currentPageNum, _formName, _isNewDoc, _documentIdx);
						_objects.clear();
						_objectsAr.clear();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if( _cRowIndex < _maxRowIndex -1 )
					{
						_objects = _lastAddItems;
					}
					
					if(_isNewDoc) _isNewDoc = false;
					_isFirst = false;
					
				}
				
			}
			
			//repeatValue 를 이용하여 아이템의 height값을 업데이트 하고 필요없는 아이템을 리스트에서 제거
//			repeatValueCheck(repeatValueCheckMap, _objects );
			// 밴드하나가 완료되면 repeatValue를 체크
			
			// 셀 병합처리 ( 밴드가 완료되면 각 밴드별로 병합을 처리한다 )
			_ubformExcel.mergedCellList();
			_ubformExcel.repeatValueCheck();
			
//			if( BandInfoMapData.DATA_BAND.equals(bandInfo.get(bandName).getClassName()) )
//			{
//				_ubformExcel.repeatValueCheck();
//			}
			/**
			// band 종료
			if( isExcelBandType )
			{
				HashMap<String, Object> pageProp = new HashMap<String, Object>();
				ArrayList<ArrayList<HashMap<String, Object>>> _objectsAr = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				
				if(pagesRowList.get(_page).size() > i+1 )
				{
					HashMap<String, Value> currentBandNextCntMap = pagesRowList.get(_page).get(i + 1);
					String nextBandName = currentBandNextCntMap.get("id").getStringValue();
					pageProp.put("cPHeight", currentBandNextCntMap.get("y").getIntegerValue() );
				}
				
				if( i == 0 ) pageProp.put("bPageHeight", 0 );
				
				_objects.add(pageProp);
				_objectsAr.add(_objects);
				try {
					_wb = _ubformExcel.xmlParsingExcel(_objectsAr, _wb, _currentPageNum, _formName, _isNewDoc, _documentIdx);
					_objects.clear();
					_objectsAr.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
			
			
		}
			
		return _objects;
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
	
	// Html to SVG Convert
	public static String convertHtmlToSvgText( String _data, HashMap<String, Value> _property, float _rowHeight )
	{
		String _retStr = "";
		int _width = 0;
        int _height = 0;
        String _fontFamily = "돋움";
        int _fontSizeUnit = 0;
        int _fontSize = 10;
        int _lineGap = 10;
        boolean _useWordWrap = true;
        
        _width =  _property.get("width").getIntegerValue().intValue();
        //_height = _property.get("height").getIntegerValue().intValue();
        
        _height = (_property.get("height").getIntegerValue() > _rowHeight)? _property.get("height").getIntegerValue().intValue() : Float.valueOf(_rowHeight).intValue();
        
        if(_property.containsKey("fixedToSize") && !_property.get("fixedToSize").getBooleanValue() )
        {
        	_height = 1123;
        }
        
        if( _property.containsKey("lineGap")  )
        {
        	_lineGap = _property.get("lineGap").getIntValue();
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
        	_fontSize =  (int) Math.round(Double.parseDouble(_property.get("fontSize").getStringValue()));
        }
        if(_property.get("fontFamily")!=null)
        {
        	_fontFamily =  _property.get("fontFamily").getStringValue();
        }
		
        String _style = "font-size:" + _fontSize + "px; font-family:" + _fontFamily + ";";
        
        ImageRenderer.Type type = ImageRenderer.Type.SVG;
        String media = "screen";
        Dimension windowSize = new Dimension(_width, _height );
        boolean cropWindow = false;
        
        boolean isAutoSizeUpdate = true;
//        if( _property.containsKey("fixedToSize")  )
//        {
//        	isAutoSizeUpdate = _property.get("fixedToSize").getBooleanValue();
//        }
        
        ImageRenderer r = new ImageRenderer();    
        r.setMediaType(media);
        r.setWindowSize(windowSize, cropWindow);
        r.setAutoSizeUpdate(isAutoSizeUpdate);
        r.setTextSplitByWord(false);
        
        try {
//        	r.loadFontList(Log.ufilePath);
        	 
        	String _srcXHtml = _data.toString();
     		_retStr = r.renderXHTML(_srcXHtml, type);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return _retStr;
	}
	
	public static String convertHtmlToSvgTextSimple( String _data, HashMap<String, Object> _property, float _rowHeight )
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
        
        _width = Float.valueOf( UBComponent.getProperties(_property, _className, "width").toString()  ).intValue();
        _height = Float.valueOf( UBComponent.getProperties(_property, _className, "height").toString()  ).intValue();
        
        String _fixedToSize = UBComponent.getProperties(_property, _className, "fixedToSize","").toString();
        
        _height = (_height > _rowHeight)? _height : Float.valueOf(_rowHeight).intValue();
        
        _lineGap = Integer.valueOf( UBComponent.getProperties(_property, _className, "lineGap", 10).toString() );
        _lineGap = (int) Math.round(Double.parseDouble( UBComponent.getProperties(_property, _className, "fontSize", 10).toString() ));
        _fontFamily = UBComponent.getProperties(_property, _className, "fontFamily","돋움").toString();
        
        
        if( _fixedToSize.equals("false") )
        {
        	_height = 1123;
        }
        
        if(Log.pageFontUnit.equals("pt"))
        {
        	_fontSizeUnit = 1;
        }
        
        String _style = "font-size:" + _fontSize + "px; font-family:" + _fontFamily + ";";
        
        ImageRenderer.Type type = ImageRenderer.Type.SVG;
        String media = "screen";
        Dimension windowSize = new Dimension(_width, _height );
        boolean cropWindow = false;
        
        boolean isAutoSizeUpdate = true;
        
        ImageRenderer r = new ImageRenderer();    
        r.setMediaType(media);
        r.setWindowSize(windowSize, cropWindow);
        r.setAutoSizeUpdate(isAutoSizeUpdate);
        r.setTextSplitByWord(false);
        
        try {
        	 
        	String _srcXHtml = _data.toString();
     		_retStr = r.renderXHTML(_srcXHtml, type);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
        
		return _retStr;
	}
	
	public HashMap<String, Object> makeDefaultBandList( Object _page,   HashMap<String, ArrayList<HashMap<String, Object>>> _data,float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr, Object _allPageList, HashMap<String, Object> _param ) throws ScriptException, UnsupportedEncodingException
	{
		// default band정보 담기 
		mPageHeight = pageHeight;
		mPageWidth = pageWidth;
		
		int i = 0;
		int j = 0;
		int l = 0;
		int k = 0;
		
		ArrayList<String> _grpSubDataNameAr = new ArrayList<String>();
		ArrayList<ArrayList<String>> groupDataNamesAr = new ArrayList<ArrayList<String>>();
		ArrayList<Element> _child = null;
		
		NodeList _allPageNodes = null;
		ArrayList<Element> _allPageElements = null;
		
		
		// groupBand 의 Group Count를 담기위한 객체
		HashMap<String, Integer> _groupBandCntMap = new HashMap<String, Integer>();
		
		if( _allPageList != null && _page instanceof Element )
		{
			// 총 페이지에서 현재 페이지의 인덱스를 구한후 이후 페이지중 Continue값이 연속되는 페이지의 경우 _child에 추가 시키는 작업 진행
			int _m = 0;
			int _max = 0;
			int _pageIdx = -1;
			int _maxIdx = -1;
			int _startIdx = 0;
			
			if( ((Element)_page).hasAttribute("isConnect") && ((Element)_page).getAttribute("isConnect") != null )
			{
				if(_allPageList instanceof NodeList)
				{
					_allPageNodes = (NodeList) _allPageList;
					_max = _allPageNodes.getLength();
					_startIdx = 0;
				}
				else
				{
					_allPageElements = (ArrayList<Element>) _allPageList;
					_max = _allPageElements.size();
					_startIdx = _allPageElements.indexOf(_page);
				}
				
				Element _tempElement = null;
				for ( _m = _startIdx;_m < _max; _m++) {
					if( _allPageNodes != null) _tempElement = (Element) _allPageNodes.item(_m);
					else if( _allPageElements != null ) _tempElement = _allPageElements.get(_m);
					if( _pageIdx == -1 )
					{
						if( _tempElement == _page)
						{
							_pageIdx = _m;
							_maxIdx  = _pageIdx;
						}
						
					}
					else
					{
						if(_tempElement.hasAttribute("isConnect") == false || _tempElement.getAttribute("isConnect") == null || !_tempElement.getAttribute("isConnect").equals("true") )
						{
							break;
						}
						else
						{
							_maxIdx = _maxIdx + 1;
						}
					}
				}
				
			}
			
			_child = new ArrayList<Element>();
			NodeList _nodes = ((Element) _page).getElementsByTagName("item");
			int _nodesLength = _nodes.getLength();
			for ( i = 0; i < _nodesLength; i++) {
				_child.add((Element) _nodes.item(i) );
			}
			
			if( _maxIdx > _pageIdx )
			{
				NodeList _subNodes = null;
				boolean _chkFlag = true;
				Element _tmpPageElement = null;
				
				for( _m = _pageIdx+1; _m < _maxIdx+1; _m++  )
				{
					
					if(_allPageNodes != null)_tmpPageElement = (Element) _allPageNodes.item(_m);
					else if(_allPageElements != null) _tmpPageElement = _allPageElements.get(_m);
					
					//_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck( _data, _tmpPageElement, _param ,mFunction);
					
					if(_chkFlag )
					{
						OriginalRequiredItemList = getRequiredItemDataInfo( _tmpPageElement, OriginalRequiredItemList);
						OriginalTabIndexItemList = getTabIndexItemDataInfo( _tmpPageElement, OriginalTabIndexItemList);
						
						_subNodes = _tmpPageElement.getElementsByTagName("item");
						
						_nodesLength =  _subNodes.getLength();
						for ( i = 0; i < _nodesLength; i++) {
							_child.add((Element) _subNodes.item(i) );
						}
					}
					
				}
			}
			else
			{
				OriginalRequiredItemList = getRequiredItemDataInfo( _page, OriginalRequiredItemList);
				OriginalTabIndexItemList = getTabIndexItemDataInfo( _page, OriginalTabIndexItemList);
			}
			
		}
		else
		{
			// masterBand일경우 page Element가 아니라 아이템이 담긴 Array가 넘어오게됨
			_child = (ArrayList<Element>) _page;
			if( _child.get(0).getTagName().equals("page") )
			{
				Element _pageElement = _child.remove(0);
				OriginalRequiredItemList = getRequiredItemDataInfo( _pageElement, OriginalRequiredItemList);
				OriginalTabIndexItemList = getTabIndexItemDataInfo( _pageElement, OriginalTabIndexItemList);
			}
			
		}
		
		ArrayList<BandInfoMapData> grpList = new ArrayList<BandInfoMapData>();
		
		Boolean _subFooterBandFlag = false;
		
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
		
		float headerBandHeight = 0f;
		ArrayList<String> bandSequenceAr = new ArrayList<String>();
		
		// 그룹밴드용 아이템처리
		int _grpDataIndex = 0;
		float _grpDefaultHeight = 0;
		float _grpHeight = 0;
		int _grpSubMaxLength = 0;
		float _pageWidth = pageWidth;
		
		String _subFooterBandStr = "";
		
		String _className = "";
		String _itemId = "";
		
		NodeList _propertys;
		HashMap<String, BandInfoMapData> bandInfoData = new HashMap<String, BandInfoMapData>();
		ArrayList<BandInfoMapData> bandInfoList = new ArrayList<BandInfoMapData>();
		String groupName = "";
		
		
		// xml band 담기
		int _childSize = _child.size();
		for(i = 0; i < _childSize ; i++)
		{
			_childItem = (Element) _child.get(i);
	
			_itemId = _childItem.getAttribute("id");
			_className = _childItem.getAttribute("className");
			
			// xml의 모든 Band정보를 뽑아서 밴드별 정보를 담기
			if( _className.length() > 4 && _className.substring(_className.length()-4, _className.length() ).toUpperCase().equals("BAND") ) {
					
				BandInfoMapData  bandData = new BandInfoMapData(_childItem);
				bandInfoData.put(bandData.getId(), bandData);	// 생성된 밴드 데이터를 ID를 Key값으로 하여 맵에 담아두기
				
				bandInfoList.add(bandData);
				
				if(  _className.equals("UBGroupHeaderBand")  )
				{
					bandSequenceAr =  new ArrayList<String>();
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
				
			}
		}
		
		// 생성된 밴드리스트를 이용하여 그룹핑 및 총 페이지 구하기
		
		HashMap<String, Value> itemProperty = new HashMap<String, Value>();
		HashMap<String, Value> tableMapProperty = new HashMap<String, Value>();
 		HashMap<String, Value> tableProperty;// = new HashMap<String, Value>()
		String _itemDataSetName = "";
		String _propertyName = "";
		String _propertyValue = "";
		String _propertyType = "";
		float _tableArogHeight = 0f;
		// page의 아이템들을 로드하여 각 아이템별 band명을 이용하여 밴드의 데이터셋과 row수를 구하기
		
		int colIndex = 0;
		int rowIndex = 0;
		float updateX = 0;
		float updateY = 0;
		int _childSize2 = _child.size();
		for(i = 0; i < _childSize2 ; i++)
		{
			_childItem = (Element) _child.get(i);
	
			_itemId = _childItem.getAttribute("id");
			_className = _childItem.getAttribute("className");
			
			if( _className.equals("UBTable") || _className.equals("UBApproval"))
			{
				
				String _includeLayoutType = "";
				ArrayList<Integer> _lastCellIdx = new ArrayList<Integer>();
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
				
				if( tableProperty.containsKey("includeLayoutType"))
				{
					_includeLayoutType = tableProperty.get("includeLayoutType").getStringValue();
				}
				
				tableProperty.put("tableId", new Value( _itemId,"string"));
				
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
					int _colSize = 0;
					if( _allTableMap.size() > 0 )
					{
						_colSize = _allTableMap.get(0).size();
					}
					for ( l = 0; l < _colSize; l++) {
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
						for ( l = 0; l < _colSize-1; l++) {
							for ( k = 1; k < _allTableMap.size(); k++) {					
								tableMapProperty = _allTableMap.get(k).get(l);
								if(!tableMapProperty.get("colSpan").getStringValue().equals("1") && _exColIdx.indexOf(l) != -1){
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
							
							if( _allTableMap.size() > 0 )
							{
								_colSize = _allTableMap.get(0).size();
							}
							
							for ( l = 0; l < _colSize; l++)
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
					
					for ( k = 0; k < _allTableMap.size(); k++) {	
						ArrayList<HashMap<String, Value>> _tableRow = _allTableMap.get(k);
						HashMap<String, Value> _tableMap = null;
						_lastCellIdx.add(0);
						
						for ( l = 0; l < _tableRow.size(); l++) {
							_tableMap = _tableRow.get(l);
							
							if( _tableMap.get("status").getStringValue().equals("NORMAL")|| _tableMap.get("status").getStringValue().equals("MS")||  _tableMap.get("status").getStringValue().equals("MR") )
							{
								if( _exColIdx.indexOf(l) == -1 && _tableMap.get("cell").getElementValue() != null )
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
					 
					if( _allTableMap.size() > 0 )
					{
						_colSize = _allTableMap.get(0).size();
					}
					 
					for ( l = 0; l < _colSize-1; l++) {
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
						
						if(_exColIdx.size() > 0 && _includeLayoutType.equals( GlobalVariableData.M_TABLE_INCLUDE_LAYOUT_TYPE_AUTO )){
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
						
						
						if(_newTalbeFlag)
						{
							
							if( _lastCellIdx.size() > 0 )
							{
								int _columnIndex = 0;
								
								if( _allTableMap.get(k).get(l).containsKey("columnIndex") ) _columnIndex = Integer.valueOf(_allTableMap.get(k).get(l).get("columnIndex").getStringValue());

								if( _lastCellIdx.get(k) ==  _columnIndex )
								{
									useRightBorder = true;
								}
							}
							else if( colIndex == _allTableMap.get(k).size() )
							{
								useRightBorder = true;
							}
							if( rowIndex == _tableMaps.getLength() ) useBottomBorder = true;
						}
						else
						{
							if( l+ tableMapProperty.get("colSpan").getIntValue() >= _allTableMap.get(k).size()-1 ) useRightBorder 	= true;
							if( k+ tableMapProperty.get("rowSpan").getIntValue() >= _tableMaps.getLength()-1 ) useBottomBorder 		= true;
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
									
									if( bandInfoData.containsKey(  tableProperty.get("band").getStringValue() ) )
									{
										float _chkBandH = bandInfoData.get( tableProperty.get("band").getStringValue()).getHeight();
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
						
						updateX = Float.valueOf(tableProperty.get("band_x").getStringValue()) +_newXValue.get(l);
						updateY = Float.valueOf(tableProperty.get("band_y").getStringValue()) + itemProperty.get("y").getIntegerValue();
						
						itemProperty.put(  "band_x", new Value( updateX, "string"));
						itemProperty.put(  "x", new Value(updateX , "string"));
						itemProperty.put(  "band_y", new Value( updateY, "string"));
						
						float _itemX = updateX;
						float _itemWidth = itemProperty.get("width").getIntegerValue();
						float _itemHeight = itemProperty.get("width").getIntegerValue();
						int _rotate = 0;
						
						if(itemProperty.containsKey("rotation") )
						{
							_rotate = itemProperty.get("rotation").getIntValue();
							
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
						
						if( mXAr.indexOf( Math.round(_itemX) ) == -1 )
						{
							mXAr.add(Math.round(_itemX));
						}
						if( mXAr.indexOf( Math.round(_itemX+ _itemWidth) ) == -1 )
						{
							mXAr.add(Math.round(_itemX + _itemWidth ) );
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
							
							if(tableProperty.containsKey("band") && tableProperty.get("band").getStringValue()!="" && bandInfoData.containsKey(tableProperty.get("band").getStringValue()) )
							{
								bandInfoData.get(tableProperty.get("band").getStringValue()).getChildren().add(itemProperty);
								
								int _requiredItemIndex = OriginalRequiredItemList.indexOf(itemProperty.get("id").getStringValue());
								int _tabIndexItemIndex = OriginalTabIndexItemList.indexOf(itemProperty.get("id").getStringValue());
								
								if( _requiredItemIndex != -1 )
								{
									bandInfoData.get(tableProperty.get("band").getStringValue()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").getStringValue() );
								}
								
								if( _tabIndexItemIndex != -1 )
								{
									bandInfoData.get(tableProperty.get("band").getStringValue()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").getStringValue() );
								}
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
					convertTableMapToApprovalTbl(_ubApprovalAr, bandInfoData,mXAr );
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
				
				float _itemX = itemProperty.get("x").getIntegerValue();
				float _itemWidth = itemProperty.get("width").getIntegerValue();
				float _itemHeight = itemProperty.get("height").getIntegerValue();
				int _rotate = 0;
				
				if(itemProperty.containsKey("rotation") )
				{
					_rotate = itemProperty.get("rotation").getIntValue();
					
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
				
				if( mXAr.indexOf( Math.round(_itemX) ) == -1 )
				{
					mXAr.add(Math.round(_itemX));
				}
				if( mXAr.indexOf( Math.round(_itemX+ _itemWidth) ) == -1 )
				{
					mXAr.add(Math.round(_itemX + _itemWidth ) );
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
				
				int _requiredItemIndex = OriginalRequiredItemList.indexOf(itemProperty.get("id").getStringValue());
				int _tabIndexItemIndex = OriginalTabIndexItemList.indexOf(itemProperty.get("id").getStringValue());
				
				if( _requiredItemIndex != -1 )
				{
					bandInfoData.get(itemProperty.get("band").getStringValue()).setRequiredItemAt( _requiredItemIndex, itemProperty.get("id").getStringValue() );
				}
				
				if( _tabIndexItemIndex != -1 )
				{
					bandInfoData.get(itemProperty.get("band").getStringValue()).setTabIndexItemAt( _tabIndexItemIndex, itemProperty.get("id").getStringValue() );
				}
				
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
			
			
		}// 각각의 아이템들의 데이터셋정보를 밴드리스트에 맵핑하고 생성할 로우별 아이템을 children에 담기완료
		
		
		boolean isAutoTableHeight  =  false;
		
        for( String key : bandInfoData.keySet() ){
        	isAutoTableHeight = false;
        	
			isAutoTableHeight = bandInfoData.get( key ).getAutoTableHeight();
			// autoTableHeight를 설정한 경우 Cell이 아닌경우 자신이 포함된 Cell의 정보를 담아놓는다.
			if(isAutoTableHeight){			
				//cell아닌 경우 table 포함 여부 확인 by IHJ
				Iterator<String> nameItr = (Iterator)bandInfoData.keySet().iterator();       
				ArrayList<HashMap<String, Value>> chilrden;
				HashMap<String, Value> currentItemData;
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
						ArrayList<HashMap<String, Value>> arTableProperties = bandInfoData.get(bandID).getTableProperties();            	
						chilrden = bandInfoData.get(bandID).getChildren();
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
										
										int hh = 0;
										//@TEST 아이템의 가운데 정렬속성이 있을경우 셀의 한가운데로 이동하기 위해 겹치는 셀의 시작인덱스와 ROWSPAN값을 담아둔다.
										boolean _isAutoVerticalPosition = bandInfoData.get(bandID).getAutoVerticalPosition();
										
										if( _isAutoVerticalPosition )
										{
											ArrayList<HashMap<String, Value>> _cellItems = (ArrayList<HashMap<String, Value>>) arTableProperties.get(kk).get("CELLS").getObjectValue();
											
											
											float _cxPos = 0;
											float _cyPos = 0;
											float _cwPos = 0;
											float _chPos = 0;
											
											for( hh = 0; hh < _cellItems.size(); hh++ )
											{
												_cxPos = _cellItems.get(hh).get("x").getIntegerValue();
												_cyPos = _cellItems.get(hh).get("band_y").getIntegerValue();
												_cwPos = _cellItems.get(hh).get("width").getIntegerValue();
												_chPos = _cellItems.get(hh).get("height").getIntegerValue();
												
												if((_cxPos <= xOtherPos && xOtherPos <= (_cxPos + _cwPos))
														&& (_cyPos <= yOtherPos && yOtherPos <= (_cyPos + _chPos)) ){
													currentItemData.put("cellRowIndex", new Value(_cellItems.get(hh).get("rowIndex").getIntValue(),"int"));
													currentItemData.put("tableId", new Value(arTableProperties.get(kk).get("tableId").getStringValue(),"string"));
													currentItemData.put("cellPadding", new Value(0 ,"float"));
													currentItemData.put("cellRowSpan", new Value( _cellItems.get(hh).get("rowSpan").getIntValue()  ,"float"));
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
													currentItemData.put("cellRowIndex", new Value(hh,"int"));
													currentItemData.put("tableId", new Value(arTableProperties.get(kk).get("tableId").getStringValue(),"string"));
													currentItemData.put("cellPadding", new Value(yOtherPos - (sumHeight- arTempTableRowH.get(hh)) ,"float"));
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
        // autoTableHeight 속성 담기
		
        
        HashMap<String, Object> _resultHashMap = new HashMap<String, Object>();
        
        _resultHashMap.put("BAND_LIST", bandInfoList);
        _resultHashMap.put("BAND_INFO", bandInfoData);
		
        return _resultHashMap;
	}
	
	/////////////////// load total page simple Test
	public ArrayList<Object> loadTotalPageSimple( HashMap<String, Object> _defaultBandInfo, Object _page, HashMap<String, ArrayList<HashMap<String, Object>>> _data,float defaultY, float pageHeight, float pageWidth, ArrayList<Integer> mXAr, Object _allPageList, HashMap<String, Object> _param  ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		
		// 1. xml을 파싱하여 아이템파싱
		// 2. default Band List 와 default Item을 각 band에 add
		// 3. 생성된 default bandList를 이용하여 group처리까지 완료된 bandList를 생성
		// 4. 생성된 bandList를 이용하여 페이지구성 하여 결과값 전달
		// 5. default BandList를 전달하여 이후의 총 페이지수 구성은 xml이 아니라 bandList를 이용하여 진행
		
		
		HashMap<String, String> originalDataMap = new HashMap<String, String>();
		Boolean repeatPageHeader = false;
		Boolean repeatPageFooter = false;
		NodeList _propertys;
//		NodeList _child = _page.getElementsByTagName("item");
		ArrayList<BandInfoMapData> bandList = new ArrayList<BandInfoMapData>();
		HashMap<String, BandInfoMapData> bandInfoData = new HashMap<String, BandInfoMapData>();
		DataSet = _data;
		mParam = _param;
		String groupName = "";
		String prevHeaderBandName = "";
		String _dataBandName = "";
		String _summeryBandName = "";
		String _itemId = "";
		String _className = "";
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		
		mPageHeight = pageHeight;
		mPageWidth = pageWidth;
		
		ArrayList<String> _grpSubDataNameAr = new ArrayList<String>();
		ArrayList<ArrayList<String>> groupDataNamesAr = new ArrayList<ArrayList<String>>();
		ArrayList<Element> _child = null;
		
		NodeList _allPageNodes = null;
		ArrayList<Element> _allPageElements = null;
		
		// groupBand 의 Group Count를 담기위한 객체
		HashMap<String, Integer> _groupBandCntMap = new HashMap<String, Integer>();
		ArrayList<BandInfoMapData> grpList = new ArrayList<BandInfoMapData>();
		Boolean _subFooterBandFlag = false;
		
		NodeList _tablePropertys;
		NodeList _cellPropertys;
		NodeList _tableMaps;
		NodeList _tableMapDatas;
		NodeList _cells;
		Node rowHeightNode;
		
		float headerBandHeight = 0f;
		ArrayList<String> bandSequenceAr = new ArrayList<String>();
		
		// 그룹밴드용 아이템처리
		int _grpDataIndex = 0;
		float _grpDefaultHeight = 0;
		float _grpHeight = 0;
		int _grpSubMaxLength = 0;
		float _pageWidth = pageWidth;
		
		String _grpDataName = "";
		String _grpSubDataName = "";
		String _grpSubBandName2 = "";
		String _grpSubBandName  = "";
		BandInfoMapData _subCloneBandData;
		String _subFooterBandStr = "";
		ArrayList<ArrayList<HashMap<String, Object>>> _grpSubDataListAr = new ArrayList<ArrayList<HashMap<String,Object>>>();
		
		
		ArrayList<BandInfoMapData> _defaultBandList = (ArrayList<BandInfoMapData>) _defaultBandInfo.get("BAND_LIST");
		HashMap<String, BandInfoMapData> _defaultBandData = (HashMap<String, BandInfoMapData>) _defaultBandInfo.get("BAND_INFO");
		
//		bandInfoData = (HashMap<String, BandInfoMapData>) _defaultBandInfo.get("BAND_INFO").clone();
		
		// xml Item
		int _childSize = _defaultBandList.size();
		BandInfoMapData _childBandItem;
		
		for(i = 0; i < _childSize ; i++)
		{
			_childBandItem = _defaultBandList.get(i);
	
			_itemId = _childBandItem.getId();
			_className = _childBandItem.getClassName();
			
			BandInfoMapData  bandData = _childBandItem.cloneBandInfo();
			
			bandInfoData.put(bandData.getId(), bandData);	// 생성된 밴드 데이터를 ID를 Key값으로 하여 맵에 담아두기
			
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
				groupName = "";
				prevHeaderBandName = "";
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
				bandSequenceAr =  new ArrayList<String>();
				
				prevHeaderBandName = bandData.getId();
				
				if(groupName.equals("") == false )
				{
					bandData.setGroupBand(groupName);
				}
			}
			else if(  _className.equals("UBEmptyBand")  )
			{
				bandSequenceAr =  new ArrayList<String>();
				
				if(groupName.equals("") == false )
				{
					bandData.setGroupBand(groupName);
				}
			}
			else if(  _className.equals("UBGroupHeaderBand")  )
			{
				groupName = bandData.getId();
				prevHeaderBandName = "";
				
				bandSequenceAr =  new ArrayList<String>();
				
				ArrayList<Boolean> orderBy = bandData.getOrderBy();
				
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
				
				ArrayList<Object> retData = GroupingDataSetProcess.changeGroupDataSet(DataSet, groupName, bandData.getDataSet(), bandData.getColumnAr().get(0), orderBy.get(0), bandData.getSort(), filterColAr, filterOperatorAr, filterTextAr, bandData.getOriginalOrder(), originalDataMap );
				DataSet = ( HashMap<String, ArrayList<HashMap<String, Object>>> ) retData.get(0);
				originalDataMap = ( HashMap<String, String> )  retData.get(1);
				int grpDataCnt = (Integer)  retData.get(2);
				//DataSet
				
				if( DataSet.size() > 0 )
				{
					bandData.setRowCount(grpDataCnt);
				}
				
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
				
				bandSequenceAr =  new ArrayList<String>();
			}
			else if(  _className.equals("UBCrossTabBand")  )
			{
				
				bandSequenceAr =  new ArrayList<String>();
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
				if( i+1 < _defaultBandList.size() )
				{
					nextChildClass = _defaultBandList.get(i+1).getClassName();
				}
				
				boolean _isGroupEndFlag = false;
					
				if( bandData.getClassName().equals("UBGroupFooterBand") && nextChildClass.equals("") == false  && nextChildClass.equals("UBGroupFooterBand") == false )
				{
					_isGroupEndFlag = true;
				}
				else if( nextChildClass.equals(BandInfoMapData.PAGE_FOOTER_BAND) )
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
					BandInfoMapData _bandInfoMapData = bandInfoData.get(groupName);
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
							_grpSubDataListAr = GroupingDataSetProcess.changeGroupDataSetSub(_data, groupName, _grpDataName, _bandInfoMapData.getColumnAr(), bandInfoData.get(groupName).getOrderBy(), _bandInfoMapData.getSort(), _bandInfoMapData.getOriginalOrder(), originalDataMap);
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
						ArrayList<BandInfoMapData> _argoDataBands = null;
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
											
											DataSet.put(_grpSubDataName, _grpSubDataListAr.get(j));
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
												_argoDataBands = new ArrayList<BandInfoMapData>();
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
									for (BandInfoMapData _argoDataBand : _argoDataBands) {
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
								bandList.add(_subCloneBandData);
								
								if( _subCloneBandData.getNewPage() && _argoDataBands != null )
								{
									for (BandInfoMapData _argoDataBand : _argoDataBands) {
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
					grpList = new ArrayList<BandInfoMapData>();
					
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
				
				bandList.add( bandData );
				
			}
			
		}
		
		if( groupName.equals("") == false && grpList.size() > 0 )
		{
			_grpDefaultHeight = 0;
			_grpSubDataNameAr = new ArrayList<String>();
			BandInfoMapData _bandInfoMapData = bandInfoData.get(groupName);
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
					_grpSubDataListAr = GroupingDataSetProcess.changeGroupDataSetSub(_data, groupName, _grpDataName, _bandInfoMapData.getColumnAr(), _bandInfoMapData.getOrderBy(), _bandInfoMapData.getSort(), _bandInfoMapData.getOriginalOrder(), originalDataMap);
					_bandInfoMapData.setSubPage( String.valueOf( _grpSubDataListAr.size() ) );
					_grpSubMaxLength = _grpSubDataListAr.size();
				}
				else
				{
					_grpSubMaxLength = 1;
				}
				
				
				// newPage속성을 지정하기 위해 담아두기
//				BandInfoMapData _argoDataBand = null;
				ArrayList<BandInfoMapData> _argoDataBands = null;
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
									
									DataSet.put(_grpSubDataName, _grpSubDataListAr.get(j));
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
										_argoDataBands = new ArrayList<BandInfoMapData>();
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
							for (BandInfoMapData _argoDataBand : _argoDataBands) {
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
							for (BandInfoMapData _argoDataBand : _argoDataBands) {
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
		
		// 생성된 밴드리스트를 이용하여 그룹핑 및 총 페이지 구하기
		
		HashMap<String, Value> itemProperty = new HashMap<String, Value>();
		HashMap<String, Value> tableMapProperty = new HashMap<String, Value>();
 		HashMap<String, Value> tableProperty;// = new HashMap<String, Value>()
		String _itemDataSetName = "";
		String _propertyName = "";
		String _propertyValue = "";
		String _propertyType = "";
		float _tableArogHeight = 0f;
		// page의 아이템들을 로드하여 각 아이템별 band명을 이용하여 밴드의 데이터셋과 row수를 구하기
		
		mFunction.setGroupBandCntMap(_groupBandCntMap);
		mFunction.setOriginalDataMap(originalDataMap);
		mFunction.setDatasetList(_data);
		
		ArrayList<Object> rowHeightListData;
		if( isExcelOption.equals("BAND") || mFitOnePage ) rowHeightListData =  makeRowHeightListExcel( bandList, bandInfoData, defaultY, defaultY, -1, _pageWidth, mXAr, originalDataMap, pageHeight );
		else rowHeightListData =  makeRowHeightList( bandList, bandInfoData, defaultY, defaultY, pageHeight, _pageWidth, mXAr, originalDataMap);
		
		ArrayList<ArrayList<HashMap<String, Value>>> pagesCountList = (ArrayList<ArrayList<HashMap<String, Value>>>) rowHeightListData.get(0);
		HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) rowHeightListData.get(1);
		
		// 밴드별 row 를 이용하여 페이지별 아이템을 생성하여 리턴
		
		ArrayList<Object> _retAr = new ArrayList<Object>();
		
		_retAr.add(bandInfoData);		
		_retAr.add(bandList);			
		_retAr.add(pagesCountList);		
		_retAr.add(crossTabData);		// 크로스탭정보가 담긴 배열
		_retAr.add(originalDataMap);		// OrizinalDataSet정보를 담은 map
		_retAr.add(groupDataNamesAr);		// 각 그룹별 그룹핑된 데이터리스트 [ [ string, string ] ] 형태
		
		_retAr.add(rowHeightListData.get(2));		// 필수목록 페이지별 리스트
		_retAr.add(rowHeightListData.get(3));		// 탭인덱스 페이지별 리스트

		
		_retAr.add(_groupBandCntMap);				//  그룹밴드별 총 그룹핑 갯수가 담긴 객체  { 밴드 ID : 그룹수, 밴드 ID2:......  }
		
		_pageXArr = mXAr;
		
		return _retAr;
	}
	
	
	public ArrayList<HashMap<String, Object>> createContinueBandItemsSimple( int _page, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, BandInfoMapDataSimple> bandInfo, ArrayList<BandInfoMapDataSimple> bandList, 
			ArrayList<ArrayList<HashMap<String, Object>>> pagesRowList , HashMap<String, Object> _param, HashMap<String, ArrayList<ArrayList<HashMap<String, Object>>> > crossTabData, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum, boolean isPivot) throws UnsupportedEncodingException, ScriptException
	{
		
		int i = 0;
		int j = 0;
		
//		ArrayList<HashMap<String, Object>> _objects = new ArrayList<HashMap<String, Object>>();
		
		// page Width 담아두기
		// crossTab maxPageWidth 값에 따라서 pageWidth 변경 
		// pageWidth
		
		// band max Width 초기화
		mBandMaxWidth = 0;
		
		String bandName = "";
		HashMap<String, Object> currentBandCntMap;
		HashMap<String, Object> currentItemData;
		String _itemId = "";
		String _className = "";
				
		String _dataSetName = "";
		
		float itemY = 0f;
		int _currRowIndex = 0;
		int _maxRowIndex = 0;
		ArrayList<HashMap<String, Object>> _child = null;
		String crossTabClassName = "UBLabelBorder";
		Boolean crossTabItems = false;
		
		DataSet = _dataSet;
		mParam = _param;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> repeatValueCheckMap = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		
		// dataConvertParser 생성
		ItemConvertParser dataItemParser = new ItemConvertParser(DataSet, mChartDataFileName, m_appParams);
		dataItemParser.setImageData(mImageData);
		dataItemParser.setChartData(mChartData);
		dataItemParser.setFunction(mFunction);
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		
		dataItemParser.setChangeItemList(changeItemList);
		
		// Group함수를 위한 객체
		dataItemParser.mOriginalDataMap = mOriginalDataMap;
		dataItemParser.mGroupDataNamesAr = mGroupDataNamesAr;
		
		dataItemParser.setIsExportType(isExportType);
		int _crossTabStartIndex = 0;
		
		mBandMaxWidth = 0;
		
		_cloneY = _cloneY + mPageMarginTop;
		
		for ( i = 0; i < pagesRowList.get(_page).size(); i++) {
			
			currentBandCntMap = pagesRowList.get(_page).get(i);
			bandName = currentBandCntMap.get("id").toString();
			_dataSetName = "";
			crossTabItems = false;
			_crossTabStartIndex = 0;
			
			repeatValueCheckMap = new HashMap<String, ArrayList<HashMap<String, Object>>>();
			
			//crossTab band일경우 visibleType가 all일때 band의 Width를 담는다
			if( bandInfo.get(bandName).getVisibleType().equals( BandInfoMapData.VISIBLE_TYPE_ALL ) )
			{
				if( currentBandCntMap.containsKey("MAX_PAGE_WIDTH") && mBandMaxWidth < Integer.valueOf( currentBandCntMap.get("MAX_PAGE_WIDTH").toString() ) )
				{
					mBandMaxWidth = Integer.valueOf(  currentBandCntMap.get("MAX_PAGE_WIDTH").toString());
				}
			}
			
			if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.CROSSTAB_BAND) )
			{
				if( crossTabData.containsKey(bandName) )
				{
					if(currentBandCntMap.containsKey("crossTabStartIndex"))
					{
						_crossTabStartIndex = Integer.valueOf(currentBandCntMap.get("crossTabStartIndex").toString());
					}
					_child = crossTabData.get(bandName).get(_page - _crossTabStartIndex);
					crossTabItems = true;
				}
			}
			else
			{
				if( bandInfo.get(bandName).getDefaultBand().equals(""))
				{
					_child = bandInfo.get(bandName).getChildren();
				}
				else
				{
					_child = bandInfo.get( bandInfo.get(bandName).getDefaultBand() ).getChildren();
					_dataSetName = bandInfo.get(bandName).getDataSet();
				}
			}
			
			
			_currRowIndex = Integer.valueOf( currentBandCntMap.get("startIndex").toString());
			_maxRowIndex =  Integer.valueOf( currentBandCntMap.get("lastIndex").toString());
			
			// Group함수의 페이징함수를 위한 값을 담아두기
			if( currentBandCntMap.containsKey("gprCurrentPageNum") && currentBandCntMap.get("gprCurrentPageNum") != null )
			{
				dataItemParser.mGroupCurrentPageIndex = Integer.valueOf( currentBandCntMap.get("gprCurrentPageNum").toString());
			}
			if( bandInfo.get(bandName).getGroupStartPageIdx() > -1 )
			{
				dataItemParser.mGroupCurrentPageIndex = _page - bandInfo.get(bandName).getGroupStartPageIdx();
			}
			
			if( currentBandCntMap.containsKey("gprTotalPageNum") && currentBandCntMap.get("gprTotalPageNum") != null )
			{
				dataItemParser.mGroupTotalPageIndex = Integer.valueOf( currentBandCntMap.get("gprTotalPageNum").toString());
			}
			if( bandInfo.get(bandName).getGroupTotalPageCnt() > -1 )
			{
				dataItemParser.mGroupTotalPageIndex = bandInfo.get(bandName).getGroupTotalPageCnt();
			}
			
			// 밴드에 useLabelBand속성이 true일경우 수만큼 반복하도록 지정
			int _labelBandCnt = 1;	
			boolean _useLabelBand = bandInfo.get(bandName).getUseLabelBand();
			int _dataIdx = 0;
			float _addLabelPosition = 0;
			float _labelBandWidth = bandInfo.get(bandName).getWidth();
			
			if( _useLabelBand )
			{
				_labelBandCnt = bandInfo.get(bandName).getLabelBandColCount();
			}
			
			int _maxDsSize = 0;
			float _bandY = 0;
			float _y = 0;
			float _x = 0;
			// xml Item
			for( j = 0; j < _child.size() ; j++){
				
				for (int _cRowIndex = _currRowIndex; _cRowIndex < _maxRowIndex; _cRowIndex++) {
					
					// labelBand 반복된 수만큼 반복
					for (int _lbIdx = 0; _lbIdx < _labelBandCnt; _lbIdx++) {
						
						currentItemData = _child.get(j);
						
						if( currentItemData == null ) continue;
						
						if( crossTabItems )
						{
							// crossTab아이템일경우 클래스명 변경
							_className = crossTabClassName;
							currentItemData.put("className", Value.fromString(crossTabClassName));
						}
						else
						{
							_className = currentItemData.get("className").toString();
						}
						
						_bandY = Float.valueOf(  UBComponent.getProperties(currentItemData, _className, "band_y", "0").toString() );
						
						currentItemData.put("currentAdjustableHeight", -1 );
						if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
						{
							
							if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
							{
								currentItemData.put("currentAdjustableHeight", bandInfo.get(bandName).getAdjustableHeightListAr().get(_cRowIndex) - _bandY );
							}
						}
						
						_dataIdx = _cRowIndex;
						// 라벨밴드의 경우 하나의 밴드에 여러건의 데이터가 표현됨
						if(_useLabelBand){
							if( "horizontal".equals( bandInfo.get(bandName).getLabelBandDirection() ) )
							{
								_dataIdx = ( _cRowIndex * _labelBandCnt ) + _lbIdx ;
							}
							else
							{
								_dataIdx = (_currRowIndex*_labelBandCnt) +  (_cRowIndex-_currRowIndex) +  ( (_maxRowIndex-_currRowIndex) * _lbIdx);
							}
							
							
							float _displayWidth=bandInfo.get(bandName).getLabelBandDisplayWidth();
							if( _displayWidth != 0 ){
								_addLabelPosition = (float) Math.floor( ( _displayWidth / _labelBandCnt) * _lbIdx );
							}else{
								_addLabelPosition = (float) Math.floor( (_labelBandWidth / _labelBandCnt) * _lbIdx );
							}
							
							
							if( BandInfoMapData.DATA_BAND.equals(bandInfo.get(bandName).getClassName()) &&  "".equals(bandInfo.get(bandName).getDataSet()) ==false && !bandInfo.get(bandName).getAutoHeight())
							{
								_maxDsSize = DataSet.get(bandInfo.get(bandName).getDataSet() ).size();
								
								// 데이터의 Length보다 클경우 그리지 않도록 지정되어있음
								if(bandInfo.get(bandName).getMinRowCount() > 1 && bandInfo.get(bandName).getMinRowCount()*_labelBandCnt > _maxDsSize )
								{
									_maxDsSize = bandInfo.get(bandName).getMinRowCount()*_labelBandCnt;
								}
								
								if( _maxDsSize <= _dataIdx )
								{
									continue;
								}
							}
						}
						
						_itemId = UBComponent.getProperties(currentItemData, _className, "id","").toString();
						
						//테스트 
						HashMap<String, Object> _propList = new HashMap<String, Object>();
						
						if( _className.equals("UBTemplateArea") && mTempletInfo != null && mTempletInfo.containsKey(_itemId) )
						{
							// TEST @@ CMJ
//							_objects = mTempletInfo.get(_itemId).convertItemData(_dataIdx, _cloneX, _cloneY + Float.valueOf( currentBandCntMap.get("y").toString()) , _objects, mFunction, bandInfo.get(bandName), _currentPageNum, _totalPageNum, -1, -1, null, currentBandCntMap, dataItemParser);
							continue;
						}
						else
						{
							if(bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_PAGE_FOOTER_BAND) )
							{
								int _summeryStartIndex = Integer.valueOf( currentBandCntMap.get("summeryStartIndex").toString());
								int _summeryEndIndex = Integer.valueOf( currentBandCntMap.get("summeryEndIndex").toString());
								
								_propList = dataItemParser.convertItemDataSimple( bandInfo.get(bandName), currentItemData, DataSet, _dataIdx, _param, _summeryStartIndex, _summeryEndIndex,_totalPageNum,_currentPageNum, _dataSetName,0, 0,  -1);
								
							}
							else
							{
								_propList = dataItemParser.convertItemDataSimple(bandInfo.get(bandName), currentItemData, DataSet, _dataIdx, _param, -1, -1 , _totalPageNum , _currentPageNum, _dataSetName, 0, 0,  -1);
								
								if( isExportType.equals(GlobalVariableData.EXPORT_TYPE_TEXT))
								{
									if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_HEADER_BAND) )
									{
										_propList.put("BAND_NAME", bandName);
									}
									else if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_BAND) && bandInfo.containsKey( bandInfo.get(bandName).getHeaderBand() )  )
									{
										_propList.put("BAND_NAME", bandName);
										_propList.put("HEADER_BAND_NAME",  bandInfo.get(bandName).getHeaderBand() );
									}
								}
								
							}
						}
						
						// 아이템이 null일경우 add시키지 않음 2015-12-02
						if( _propList == null ) continue;
						
						_y = Float.valueOf( UBComponent.getProperties(currentBandCntMap, _className, "y", "0").toString() );
						_x = Float.valueOf( UBComponent.getProperties(_propList, _className, "x", "-1" ).toString() );
						
						if( crossTabItems == false  )
						{
							if(bandInfo.get(bandName).getAutoTableHeight()){
//								float bandY = Float.valueOf( currentItemData.get("band_y").toString() );
								float currTableRowY = 0;
															
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									itemY = _y +  getCurrentYpositionBand(bandInfo.get(bandName).getAdjustableHeightListAr(), _cRowIndex, Integer.valueOf( currentBandCntMap.get("startIndex").toString()) , bandInfo.get(bandName));
									
									String tableId = UBComponent.getProperties(currentItemData, _className, "tableId", "").toString();
									
									if(!currentItemData.containsKey("isCell")){
										if( !tableId.equals("") ){
											//String tableId = currentItemData.get("tableId").toString();
											int cellRowIndex = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "cellRowIndex", "0").toString() );
											float cellPadding = 0;
											
											int _cellRowSpan = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "cellRowSpan", "-1").toString() );
											float _cellRowHeight = 0;
											float _cellPadding = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "cellPadding", "0").toString() );
											float _cellHeight = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "height", "0").toString() );
											if( _cellRowSpan != -1 )
											{
//												_cellRowSpan =Integer.valueOf( currentItemData.get("cellRowSpan").toString() );
												
												if(_cellRowSpan > 1){
													for(int ii = cellRowIndex; ii < cellRowIndex + _cellRowSpan; ii++ ){
														_cellRowHeight = _cellRowHeight +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(ii);			
													}										
												}else{
													_cellRowHeight = bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(cellRowIndex);
												}
												
												_cellRowHeight = _cellRowHeight / 2 - ( _cellHeight / 2 ) ;
												
											}
											
											if(bandInfo.get(bandName).getGroupBand().equals("") == false )
											{
												cellPadding = _cellPadding +  bandInfo.get( bandInfo.get(bandName).getDefaultBand() ).getTableBandY().get(tableId);									
											}
											else
											{
												cellPadding = _cellPadding +  bandInfo.get( bandName ).getTableBandY().get(tableId);
											}
											
											
											for(int ii = 0; ii < cellRowIndex; ii++ ){
												currTableRowY = currTableRowY +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(ii) + 1;			
											}											
											currTableRowY = currTableRowY + cellPadding + _cellRowHeight ;																				
											
										}else{
//											currTableRowY = Float.valueOf( currentItemData.get("band_y").toString());
											currTableRowY = _bandY;
										}
										
									}else{
										
										int currTableRowIdx =  Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "rowIndex", "").toString() );
										int currTableRowSpan = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "rowSpan", "").toString() );
										float currTableRowHeight = 0;								
										String orgTableId = UBComponent.getProperties(currentItemData, _className, "ORIGINAL_TABLE_ID", "").toString();								
										
										if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
										{
											if(bandInfo.get(bandName).getGroupBand().equals("") == false )
											{
												currTableRowY =  bandInfo.get(bandInfo.get(bandName).getDefaultBand()).getTableBandY().get(orgTableId);
											}
											else
											{
												currTableRowY =  bandInfo.get(bandName).getTableBandY().get(orgTableId);
											}
											
											for(int ii = 0; ii < currTableRowIdx; ii++ ){
												currTableRowY = currTableRowY +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(ii);			
											}	
//											}
											
											if(currTableRowSpan > 1){
												for(int ii = currTableRowIdx; ii < currTableRowIdx + currTableRowSpan; ii++ ){
													currTableRowHeight = currTableRowHeight +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(ii);			
												}										
											}else{
												currTableRowHeight = bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(currTableRowIdx);
											}	
											_propList.put("height",currTableRowHeight);
										}
										else
										{
											currTableRowY = _y;
										}
									}
								}
								else
								{
//									currTableRowY = Float.valueOf( currentItemData.get("band_y").toString() );						
									currTableRowY = _bandY;						
									// 일반 아이템의 y좌표는 인덱스*밴드height+ 아이템의 band_y값을 이용해서 이동 ( resizedHeight값이 잇는 아이템은 height별로 따로 처리 )
									itemY = _y+ ( bandInfo.get(currentBandCntMap.get("id").toString()).getHeight() * ( _cRowIndex - Integer.valueOf( currentBandCntMap.get("startIndex").toString()) ) );
								}
								
								itemY = itemY + currTableRowY;
							}else {
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									itemY = Float.valueOf( currentBandCntMap.get("y").toString() ) +  getCurrentYpositionBand(bandInfo.get(bandName).getAdjustableHeightListAr(), _cRowIndex, Integer.valueOf(currentBandCntMap.get("startIndex").toString()) , bandInfo.get(bandName));
									if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
									{
										_propList.put("height", bandInfo.get(bandName).getAdjustableHeightListAr().get(_cRowIndex) - _bandY );
										
										//_propList.put("tooltip"," H : " + bandInfo.get(bandName).getAdjustableHeightListAr().toString()  );
									}
								}
								else
								{
									// 일반 아이템의 y좌표는 인덱스*밴드height+ 아이템의 band_y값을 이용해서 이동 ( resizedHeight값이 잇는 아이템은 height별로 따로 처리 )
									itemY = _y + ( bandInfo.get(currentBandCntMap.get("id").toString()).getHeight() * ( _cRowIndex - Integer.valueOf(currentBandCntMap.get("startIndex").toString()) ) );
								}
							
								itemY = itemY + _bandY;
							}							
							
							itemY = ((float) Math.round(  itemY*100) / 100);
							_propList.put("y", itemY + _cloneY );
							
							if( _x != -1 ){
								_propList.put("x", _x + _cloneX + _addLabelPosition );
							}
							
							if(_propList.containsKey("id") && _propList.get("id").equals("") == false )_itemId =  _propList.get("id").toString();
							
							_propList.put("className" , _className );
							_propList.put("id" , _itemId );
							boolean _repeatValue = UBComponent.getProperties( currentItemData, _className, "repeatedValue", "").toString().equals("true");
							
							if( currentItemData.containsKey("realClassName") && "TABLE".equals(currentItemData.get("realClassName").toString() ) )
							{
								
								
								if( _itemId.equals("") )_propList.put("id", "TB_" + _cRowIndex + "_" + _itemId);
								
								// Export시 테이블로 내보내기 위한 작업
								_propList.put("isTable", "true" );
								_propList.put("TABLE_ID", bandName + "_" + currentItemData.get("realTableID").toString() );	// 테이블아이템생성을 위한 밴드명+테이블 id를 담아둔다(2016-03-07)
								
								
								// 테이블이 가로반복된 경우 반복된 테이블끼리 합치지 않도록 아이디를 분할처리 
								if( _lbIdx > 0 && _propList.containsKey("TABLE_ID") )
								{
									_propList.put("TABLE_ID", _propList.get("TABLE_ID")+"_"+_lbIdx );
								}
								
								// 아이템의 Height값이 밴드보다 클경우 밴드의 사이즈와 동일하게 맞추도록 수정
								
								// cell 의 over만큼 값을 담기
//								_propList.put("cellHeight", _propList.get("height").toString());
								_propList.put("cellHeight", UBComponent.getProperties(_propList, _className, "height" ).toString());
								_propList.put("cellY", _propList.get("y") ); // 명우;
								
								if( currentItemData.containsKey("cellOverHeight") )
								{
									_propList.put("cellOverHeight", Float.valueOf( currentItemData.get("cellOverHeight").toString() ) );
								}
								if( currentItemData.containsKey("cellOutHeight") )
								{
									_propList.put("cellOutHeight", Float.valueOf(currentItemData.get("cellOutHeight").toString() ) );
								}
								
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									_propList.put("cellOutHeight", 0 );
								}
								
								if( _repeatValue )
								{
									_propList.put( "repeatedValue", _repeatValue );
								}
								
							}
							
							if( _repeatValue )
							{
								String chkDataID = "";
								String _tempStr = UBComponent.getProperties(currentItemData, _className, "dataSet", "").toString();
								
								if( _dataSetName.equals("") && !_tempStr.equals("") && !_tempStr.equals("null") )
								{
									chkDataID =  _tempStr;
								}
								else
								{
									chkDataID = _dataSetName;
								}
								if( bandInfo.get(bandName).getDataSet().equals("") == false && DataSet.containsKey(chkDataID) == false )
								{
									chkDataID = bandInfo.get(bandName).getDataSet();
								}
								
								if( DataSet.containsKey(chkDataID) == false )
								{
									chkDataID = "";
								}
								
								_tempStr = UBComponent.getProperties(currentItemData, _className, "dataType", "").toString();
								
								if( _tempStr.equals("0") || ( chkDataID.equals("") == false  && DataSet.containsKey(chkDataID) && DataSet.get(chkDataID).size() > _cRowIndex ) )
								{
									if( repeatValueCheckMap.containsKey(_itemId) == false ){ 
										repeatValueCheckMap.put(_itemId, new ArrayList<HashMap<String, Object>>());
									}
									
									HashMap<String, Object> repeatObj = new HashMap<String, Object>();
									repeatObj.put("item", _propList);
									repeatObj.put("datatext", getRepeatValueCheckStrSimple(currentItemData, _propList.get("text").toString(), chkDataID, _cRowIndex)  );
									repeatValueCheckMap.get(_itemId).add(repeatObj);
								}
								
							}
							
							// isPivot값이 true일경우 
							if(isPivot)
							{
								float _argoX 		= Float.valueOf( String.valueOf(_propList.get("x")) );
								float _argoY 		= Float.valueOf( String.valueOf(_propList.get("y")) );
								float _argoWidth  	= Float.valueOf( String.valueOf(_propList.get("x2")) );
								float _argoHeight 	= Float.valueOf( String.valueOf(_propList.get("y2")) );
								
								_propList.put("x", _argoY);
								_propList.put("y", mPageWidth - _argoX - _argoWidth);
								_propList.put("width", _argoHeight);
								_propList.put("height", _argoWidth );
								
								Object _tempObj = _propList.get("borderSide");
								if(_tempObj != null)
								{
									//_propList의 border 업데이트( right : t->l,r->t,l->b,r->b ) 
									String borderSide = String.valueOf(_tempObj);
									borderSide = borderSide.replace("[", "").replace("]", "").replace(" ", ""); 
									
									String[] convertBorderSide = borderSide.split(",");
									ArrayList<String> newBorderSide = new ArrayList<String>();
									
									for (int k = 0; k < convertBorderSide.length; k++) {
										
										if(convertBorderSide[k].equals("top"))
										{
											newBorderSide.add("left");
										}
										else if(convertBorderSide[k].equals("left"))
										{
											newBorderSide.add("bottom");
										}
										else if(convertBorderSide[k].equals("right"))
										{
											newBorderSide.add("top");
										}
										else if(convertBorderSide[k].equals("bottom"))
										{
											newBorderSide.add("right");
										}
									}
									
									_propList.put("borderSide", newBorderSide);
								}
								
							}
							
						}
						else
						{
							_propList.put("className" , crossTabClassName ); 
							
							_propList.put("y", Float.valueOf(_propList.get("y").toString()) + _cloneY );
							
							if( _propList.get("x") != null ){
								_propList.put("x", Float.valueOf(_propList.get("x").toString()) + _cloneX );
							}
						}
						
						_propList.put("top", _propList.get("y"));
						_propList.put("left", _propList.get("x"));
						
						//아이템의 id를 탭 인덱스에 사용된 id로 변경
						if(_propList.containsKey("TABINDEX_ID") && _propList.get("TABINDEX_ID").equals("")==false)
						{
							_propList.put("id", _propList.get("TABINDEX_ID"));
						}
						
						_objects.add(_propList);
						
					}
					// 라벨밴드 반복 종료
					
				}

			}
			
			
			//repeatValue 를 이용하여 아이템의 height값을 업데이트 하고 필요없는 아이템을 리스트에서 제거
			repeatValueCheck(repeatValueCheckMap, _objects );
			
		}
			
		return _objects;
	}
	
	
	public ArrayList<HashMap<String, Object>> createContinueBandItemsSimplePDF( int _page, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, BandInfoMapDataSimple> bandInfo, ArrayList<BandInfoMapDataSimple> bandList, 
			ArrayList<ArrayList<HashMap<String, Object>>> pagesRowList , HashMap<String, Object> _param, HashMap<String, ArrayList<ArrayList<HashMap<String, Object>>> > crossTabData, float _cloneX, float _cloneY , ArrayList<HashMap<String, Object>> _objects , int _totalPageNum, int _currentPageNum, boolean isPivot
			,ubFormToPDF _ubPDF, int _exportPageIdx ) throws UnsupportedEncodingException, ScriptException
	{
		
		int i = 0;
		int j = 0;
		
		// page Width 담아두기
		// crossTab maxPageWidth 값에 따라서 pageWidth 변경 
		// pageWidth
		
		// band max Width 초기화
		mBandMaxWidth = 0;
		
		String bandName = "";
		HashMap<String, Object> currentBandCntMap;
		HashMap<String, Object> currentItemData;
		String _itemId = "";
		String _className = "";
				
		String _dataSetName = "";
		
		float itemY = 0f;
		int _currRowIndex = 0;
		int _maxRowIndex = 0;
		ArrayList<HashMap<String, Object>> _child = null;
		String crossTabClassName = "UBLabelBorder";
		Boolean crossTabItems = false;
		
		DataSet = _dataSet;
		mParam = _param;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> repeatValueCheckMap = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		
		// dataConvertParser 생성
		ItemConvertParser dataItemParser = new ItemConvertParser(DataSet, mChartDataFileName, m_appParams);
		dataItemParser.setImageData(mImageData);
		dataItemParser.setChartData(mChartData);
		dataItemParser.setFunction(mFunction);
		dataItemParser.setMinimumResizeFontSize(mMinimumResizeFontSize);
		
		dataItemParser.setChangeItemList(changeItemList);
		
		// Group함수를 위한 객체
		dataItemParser.mOriginalDataMap = mOriginalDataMap;
		dataItemParser.mGroupDataNamesAr = mGroupDataNamesAr;
		
		dataItemParser.setIsExportType(isExportType);
		int _crossTabStartIndex = 0;
		
		mBandMaxWidth = 0;
		
		_cloneY = _cloneY + mPageMarginTop;
		
		for ( i = 0; i < pagesRowList.get(_page).size(); i++) {
			
			currentBandCntMap = pagesRowList.get(_page).get(i);
			bandName = currentBandCntMap.get("id").toString();
			_dataSetName = "";
			crossTabItems = false;
			_crossTabStartIndex = 0;
			
			repeatValueCheckMap = new HashMap<String, ArrayList<HashMap<String, Object>>>();
			
			//crossTab band일경우 visibleType가 all일때 band의 Width를 담는다
			if( bandInfo.get(bandName).getVisibleType().equals( BandInfoMapData.VISIBLE_TYPE_ALL ) )
			{
				if( currentBandCntMap.containsKey("MAX_PAGE_WIDTH") && mBandMaxWidth < Integer.valueOf( currentBandCntMap.get("MAX_PAGE_WIDTH").toString() ) )
				{
					mBandMaxWidth = Integer.valueOf(  currentBandCntMap.get("MAX_PAGE_WIDTH").toString());
				}
			}
			
			if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.CROSSTAB_BAND) )
			{
				if( crossTabData.containsKey(bandName) )
				{
					if(currentBandCntMap.containsKey("crossTabStartIndex"))
					{
						_crossTabStartIndex = Integer.valueOf(currentBandCntMap.get("crossTabStartIndex").toString());
					}
					_child = crossTabData.get(bandName).get(_page - _crossTabStartIndex);
					crossTabItems = true;
				}
			}
			else
			{
				if( bandInfo.get(bandName).getDefaultBand().equals(""))
				{
					_child = bandInfo.get(bandName).getChildren();
				}
				else
				{
					_child = bandInfo.get( bandInfo.get(bandName).getDefaultBand() ).getChildren();
					_dataSetName = bandInfo.get(bandName).getDataSet();
				}
			}
			
			
			_currRowIndex = Integer.valueOf( currentBandCntMap.get("startIndex").toString());
			_maxRowIndex =  Integer.valueOf( currentBandCntMap.get("lastIndex").toString());
			
			// Group함수의 페이징함수를 위한 값을 담아두기
			if( currentBandCntMap.containsKey("gprCurrentPageNum") && currentBandCntMap.get("gprCurrentPageNum") != null )
			{
				dataItemParser.mGroupCurrentPageIndex = Integer.valueOf( currentBandCntMap.get("gprCurrentPageNum").toString());
			}
			if( bandInfo.get(bandName).getGroupStartPageIdx() > -1 )
			{
				dataItemParser.mGroupCurrentPageIndex = _page - bandInfo.get(bandName).getGroupStartPageIdx();
			}
			
			if( currentBandCntMap.containsKey("gprTotalPageNum") && currentBandCntMap.get("gprTotalPageNum") != null )
			{
				dataItemParser.mGroupTotalPageIndex = Integer.valueOf( currentBandCntMap.get("gprTotalPageNum").toString());
			}
			if( bandInfo.get(bandName).getGroupTotalPageCnt() > -1 )
			{
				dataItemParser.mGroupTotalPageIndex = bandInfo.get(bandName).getGroupTotalPageCnt();
			}
			
			// 밴드에 useLabelBand속성이 true일경우 수만큼 반복하도록 지정
			int _labelBandCnt = 1;	
			boolean _useLabelBand = bandInfo.get(bandName).getUseLabelBand();
			int _dataIdx = 0;
			float _addLabelPosition = 0;
			float _labelBandWidth = bandInfo.get(bandName).getWidth();
			
			if( _useLabelBand )
			{
				_labelBandCnt = bandInfo.get(bandName).getLabelBandColCount();
			}
			
			int _maxDsSize = 0;
			float _bandY = 0;
			float _y = 0;
			float _x = 0;
			// xml Item
			for( j = 0; j < _child.size() ; j++){
				
				for (int _cRowIndex = _currRowIndex; _cRowIndex < _maxRowIndex; _cRowIndex++) {
					
					// labelBand 반복된 수만큼 반복
					for (int _lbIdx = 0; _lbIdx < _labelBandCnt; _lbIdx++) {
						
						currentItemData = _child.get(j);
						
						if( currentItemData == null ) continue;
						
						if( crossTabItems )
						{
							// crossTab아이템일경우 클래스명 변경
							_className = crossTabClassName;
							currentItemData.put("className", Value.fromString(crossTabClassName));
						}
						else
						{
							_className = currentItemData.get("className").toString();
						}
						
						_bandY = Float.valueOf(  UBComponent.getProperties(currentItemData, _className, "band_y", "0").toString() );
						
						currentItemData.put("currentAdjustableHeight", -1 );
						if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
						{
							
							if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
							{
								currentItemData.put("currentAdjustableHeight", bandInfo.get(bandName).getAdjustableHeightListAr().get(_cRowIndex) - _bandY );
							}
						}
						
						_dataIdx = _cRowIndex;
						// 라벨밴드의 경우 하나의 밴드에 여러건의 데이터가 표현됨
						if(_useLabelBand){
							if( "horizontal".equals( bandInfo.get(bandName).getLabelBandDirection() ) )
							{
								_dataIdx = ( _cRowIndex * _labelBandCnt ) + _lbIdx ;
							}
							else
							{
								_dataIdx = (_currRowIndex*_labelBandCnt) +  (_cRowIndex-_currRowIndex) +  ( (_maxRowIndex-_currRowIndex) * _lbIdx);
							}
							
							
							float _displayWidth=bandInfo.get(bandName).getLabelBandDisplayWidth();
							if( _displayWidth != 0 ){
								_addLabelPosition = (float) Math.floor( ( _displayWidth / _labelBandCnt) * _lbIdx );
							}else{
								_addLabelPosition = (float) Math.floor( (_labelBandWidth / _labelBandCnt) * _lbIdx );
							}
							
							
							if( BandInfoMapData.DATA_BAND.equals(bandInfo.get(bandName).getClassName()) &&  "".equals(bandInfo.get(bandName).getDataSet()) ==false && !bandInfo.get(bandName).getAutoHeight())
							{
								_maxDsSize = DataSet.get(bandInfo.get(bandName).getDataSet() ).size();
								
								// 데이터의 Length보다 클경우 그리지 않도록 지정되어있음
								if(bandInfo.get(bandName).getMinRowCount() > 1 && bandInfo.get(bandName).getMinRowCount()*_labelBandCnt > _maxDsSize )
								{
									_maxDsSize = bandInfo.get(bandName).getMinRowCount()*_labelBandCnt;
								}
								
								if( _maxDsSize <= _dataIdx )
								{
									continue;
								}
							}
						}
						
						_itemId = UBComponent.getProperties(currentItemData, _className, "id","").toString();
						
						//테스트 
						HashMap<String, Object> _propList = new HashMap<String, Object>();
						
						if( _className.equals("UBTemplateArea") && mTempletInfo != null && mTempletInfo.containsKey(_itemId) )
						{
							// TEST @@ CMJ
//							_objects = mTempletInfo.get(_itemId).convertItemData(_dataIdx, _cloneX, _cloneY + Float.valueOf( currentBandCntMap.get("y").toString()) , _objects, mFunction, bandInfo.get(bandName), _currentPageNum, _totalPageNum, -1, -1, null, currentBandCntMap, dataItemParser);
							continue;
						}
						else
						{
							if(bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_PAGE_FOOTER_BAND) )
							{
								int _summeryStartIndex = Integer.valueOf( currentBandCntMap.get("summeryStartIndex").toString());
								int _summeryEndIndex = Integer.valueOf( currentBandCntMap.get("summeryEndIndex").toString());
								
								_propList = dataItemParser.convertItemDataSimple( bandInfo.get(bandName), currentItemData, DataSet, _dataIdx, _param, _summeryStartIndex, _summeryEndIndex,_totalPageNum,_currentPageNum, _dataSetName,0, 0,  -1);
								
							}
							else
							{
								_propList = dataItemParser.convertItemDataSimple(bandInfo.get(bandName), currentItemData, DataSet, _dataIdx, _param, -1, -1 , _totalPageNum , _currentPageNum, _dataSetName, 0, 0,  -1);
								
								if( isExportType.equals(GlobalVariableData.EXPORT_TYPE_TEXT))
								{
									if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_HEADER_BAND) )
									{
										_propList.put("BAND_NAME", bandName);
									}
									else if( bandInfo.get(bandName).getClassName().equals(BandInfoMapData.DATA_BAND) && bandInfo.containsKey( bandInfo.get(bandName).getHeaderBand() )  )
									{
										_propList.put("BAND_NAME", bandName);
										_propList.put("HEADER_BAND_NAME",  bandInfo.get(bandName).getHeaderBand() );
									}
								}
								
							}
						}
						
						// 아이템이 null일경우 add시키지 않음 2015-12-02
						if( _propList == null ) continue;
						
						_y = Float.valueOf( UBComponent.getProperties(currentBandCntMap, _className, "y", "0").toString() );
						_x = Float.valueOf( UBComponent.getProperties(_propList, _className, "x", "-1" ).toString() );
						
						if( crossTabItems == false  )
						{
							if(bandInfo.get(bandName).getAutoTableHeight()){
//								float bandY = Float.valueOf( currentItemData.get("band_y").toString() );
								float currTableRowY = 0;
															
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									itemY = _y +  getCurrentYpositionBand(bandInfo.get(bandName).getAdjustableHeightListAr(), _cRowIndex, Integer.valueOf( currentBandCntMap.get("startIndex").toString()) , bandInfo.get(bandName));
									
									String tableId = UBComponent.getProperties(currentItemData, _className, "tableId", "").toString();
									
									if(!currentItemData.containsKey("isCell")){
										if( !tableId.equals("") ){
											//String tableId = currentItemData.get("tableId").toString();
											int cellRowIndex = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "cellRowIndex", "0").toString() );
											float cellPadding = 0;
											
											int _cellRowSpan = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "cellRowSpan", "-1").toString() );
											float _cellRowHeight = 0;
											float _cellPadding = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "cellPadding", "0").toString() );
											float _cellHeight = Float.valueOf( UBComponent.getProperties(currentItemData, _className, "height", "0").toString() );
											if( _cellRowSpan != -1 )
											{
												if(_cellRowSpan > 1){
													for(int ii = cellRowIndex; ii < cellRowIndex + _cellRowSpan; ii++ ){
														_cellRowHeight = _cellRowHeight +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(ii);			
													}										
												}else{
													_cellRowHeight = bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(cellRowIndex);
												}
												
												_cellRowHeight = _cellRowHeight / 2 - ( _cellHeight / 2 ) ;
												
											}
											
											if(bandInfo.get(bandName).getGroupBand().equals("") == false )
											{
												cellPadding = _cellPadding +  bandInfo.get( bandInfo.get(bandName).getDefaultBand() ).getTableBandY().get(tableId);									
											}
											else
											{
												cellPadding = _cellPadding +  bandInfo.get( bandName ).getTableBandY().get(tableId);
											}
											
											
											for(int ii = 0; ii < cellRowIndex; ii++ ){
												currTableRowY = currTableRowY +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(tableId).get(ii) + 1;			
											}											
											currTableRowY = currTableRowY + cellPadding + _cellRowHeight ;																				
											
										}else{
											currTableRowY = _bandY;
										}
										
									}else{
										
										int currTableRowIdx =  Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "rowIndex", "").toString() );
										int currTableRowSpan = Integer.valueOf( UBComponent.getProperties(currentItemData, _className, "rowSpan", "").toString() );
										float currTableRowHeight = 0;								
										String orgTableId = UBComponent.getProperties(currentItemData, _className, "ORIGINAL_TABLE_ID", "").toString();								
										
										if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
										{
											if(bandInfo.get(bandName).getGroupBand().equals("") == false )
											{
												currTableRowY =  bandInfo.get(bandInfo.get(bandName).getDefaultBand()).getTableBandY().get(orgTableId);
											}
											else
											{
												currTableRowY =  bandInfo.get(bandName).getTableBandY().get(orgTableId);
											}
											
											for(int ii = 0; ii < currTableRowIdx; ii++ ){
												currTableRowY = currTableRowY +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(ii);			
											}	
//											}
											
											if(currTableRowSpan > 1){
												for(int ii = currTableRowIdx; ii < currTableRowIdx + currTableRowSpan; ii++ ){
													currTableRowHeight = currTableRowHeight +  bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(ii);			
												}										
											}else{
												currTableRowHeight = bandInfo.get(bandName).getTableRowHeight().get(_cRowIndex).get(orgTableId).get(currTableRowIdx);
											}	
											_propList.put("height",currTableRowHeight);
										}
										else
										{
											currTableRowY = _y;
										}
									}
								}
								else
								{
									currTableRowY = _bandY;						
									// 일반 아이템의 y좌표는 인덱스*밴드height+ 아이템의 band_y값을 이용해서 이동 ( resizedHeight값이 잇는 아이템은 height별로 따로 처리 )
									itemY = _y+ ( bandInfo.get(currentBandCntMap.get("id").toString()).getHeight() * ( _cRowIndex - Integer.valueOf( currentBandCntMap.get("startIndex").toString()) ) );
								}
								
								itemY = itemY + currTableRowY;
							}else {
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									itemY = Float.valueOf( currentBandCntMap.get("y").toString() ) +  getCurrentYpositionBand(bandInfo.get(bandName).getAdjustableHeightListAr(), _cRowIndex, Integer.valueOf(currentBandCntMap.get("startIndex").toString()) , bandInfo.get(bandName));
									if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > _cRowIndex )
									{
										_propList.put("height", bandInfo.get(bandName).getAdjustableHeightListAr().get(_cRowIndex) - _bandY );
									}
								}
								else
								{
									// 일반 아이템의 y좌표는 인덱스*밴드height+ 아이템의 band_y값을 이용해서 이동 ( resizedHeight값이 잇는 아이템은 height별로 따로 처리 )
									itemY = _y + ( bandInfo.get(currentBandCntMap.get("id").toString()).getHeight() * ( _cRowIndex - Integer.valueOf(currentBandCntMap.get("startIndex").toString()) ) );
								}
							
								itemY = itemY + _bandY;
							}							
							
							itemY = ((float) Math.round(  itemY*100) / 100);
							_propList.put("y", itemY + _cloneY );
							
							if( _x != -1 ){
								_propList.put("x", _x + _cloneX + _addLabelPosition );
							}
							
							if(_propList.containsKey("id") && _propList.get("id").equals("") == false )_itemId =  _propList.get("id").toString();
							
							_propList.put("className" , _className );
							_propList.put("id" , _itemId );
							boolean _repeatValue = UBComponent.getProperties( currentItemData, _className, "repeatedValue", "").toString().equals("true");
							
							if( currentItemData.containsKey("realClassName") && "TABLE".equals(currentItemData.get("realClassName").toString() ) )
							{
								
								
								if( _itemId.equals("") )_propList.put("id", "TB_" + _cRowIndex + "_" + _itemId);
								
								// Export시 테이블로 내보내기 위한 작업
								_propList.put("isTable", "true" );
								_propList.put("TABLE_ID", bandName + "_" + currentItemData.get("realTableID").toString() );	// 테이블아이템생성을 위한 밴드명+테이블 id를 담아둔다(2016-03-07)
								
								
								// 테이블이 가로반복된 경우 반복된 테이블끼리 합치지 않도록 아이디를 분할처리 
								if( _lbIdx > 0 && _propList.containsKey("TABLE_ID") )
								{
									_propList.put("TABLE_ID", _propList.get("TABLE_ID")+"_"+_lbIdx );
								}
								
								// 아이템의 Height값이 밴드보다 클경우 밴드의 사이즈와 동일하게 맞추도록 수정
								
								// cell 의 over만큼 값을 담기
								_propList.put("cellHeight", UBComponent.getProperties(_propList, _className, "height" ).toString());
								_propList.put("cellY", _propList.get("y") ); // 명우;
								
								if( currentItemData.containsKey("cellOverHeight") )
								{
									_propList.put("cellOverHeight", Float.valueOf( currentItemData.get("cellOverHeight").toString() ) );
								}
								if( currentItemData.containsKey("cellOutHeight") )
								{
									_propList.put("cellOutHeight", Float.valueOf(currentItemData.get("cellOutHeight").toString() ) );
								}
								
								if( bandInfo.get(bandName).getAdjustableHeightListAr().size() > 0 )
								{
									_propList.put("cellOutHeight", 0 );
								}
								
								if( _repeatValue )
								{
									_propList.put( "repeatedValue", _repeatValue );
								}
								
							}
							
							if( _repeatValue )
							{
								String chkDataID = "";
								String _tempStr = UBComponent.getProperties(currentItemData, _className, "dataSet", "").toString();
								
								if( _dataSetName.equals("") && !_tempStr.equals("") && !_tempStr.equals("null") )
								{
									chkDataID =  _tempStr;
								}
								else
								{
									chkDataID = _dataSetName;
								}
								if( bandInfo.get(bandName).getDataSet().equals("") == false && DataSet.containsKey(chkDataID) == false )
								{
									chkDataID = bandInfo.get(bandName).getDataSet();
								}
								
								if( DataSet.containsKey(chkDataID) == false )
								{
									chkDataID = "";
								}
								
								_tempStr = UBComponent.getProperties(currentItemData, _className, "dataType", "").toString();
								
								if( _tempStr.equals("0") || ( chkDataID.equals("") == false  && DataSet.containsKey(chkDataID) && DataSet.get(chkDataID).size() > _cRowIndex ) )
								{
									if( repeatValueCheckMap.containsKey(_itemId) == false ){ 
										repeatValueCheckMap.put(_itemId, new ArrayList<HashMap<String, Object>>());
									}
									
									HashMap<String, Object> repeatObj = new HashMap<String, Object>();
									repeatObj.put("item", _propList);
									repeatObj.put("datatext", getRepeatValueCheckStrSimple(currentItemData, _propList.get("text").toString(), chkDataID, _cRowIndex)  );
									repeatValueCheckMap.get(_itemId).add(repeatObj);
								}
								
							}
							
							// isPivot값이 true일경우 
							if(isPivot)
							{
								float _argoX 		= Float.valueOf( String.valueOf(_propList.get("x")) );
								float _argoY 		= Float.valueOf( String.valueOf(_propList.get("y")) );
								float _argoWidth  	= Float.valueOf( String.valueOf(_propList.get("x2")) );
								float _argoHeight 	= Float.valueOf( String.valueOf(_propList.get("y2")) );
								
								_propList.put("x", _argoY);
								_propList.put("y", mPageWidth - _argoX - _argoWidth);
								_propList.put("width", _argoHeight);
								_propList.put("height", _argoWidth );
								
								Object _tempObj = _propList.get("borderSide");
								if(_tempObj != null)
								{
									//_propList의 border 업데이트( right : t->l,r->t,l->b,r->b ) 
									String borderSide = String.valueOf(_tempObj);
									borderSide = borderSide.replace("[", "").replace("]", "").replace(" ", ""); 
									
									String[] convertBorderSide = borderSide.split(",");
									ArrayList<String> newBorderSide = new ArrayList<String>();
									
									for (int k = 0; k < convertBorderSide.length; k++) {
										
										if(convertBorderSide[k].equals("top"))
										{
											newBorderSide.add("left");
										}
										else if(convertBorderSide[k].equals("left"))
										{
											newBorderSide.add("bottom");
										}
										else if(convertBorderSide[k].equals("right"))
										{
											newBorderSide.add("top");
										}
										else if(convertBorderSide[k].equals("bottom"))
										{
											newBorderSide.add("right");
										}
									}
									
									_propList.put("borderSide", newBorderSide);
								}
								
							}
							
						}
						else
						{
							_propList.put("className" , crossTabClassName ); 
							
							_propList.put("y", Float.valueOf(_propList.get("y").toString()) + _cloneY );
							
							if( _propList.get("x") != null ){
								_propList.put("x", Float.valueOf(_propList.get("x").toString()) + _cloneX );
							}
						}
						
						_propList.put("top", _propList.get("y"));
						_propList.put("left", _propList.get("x"));
						
						//아이템의 id를 탭 인덱스에 사용된 id로 변경
						if(_propList.containsKey("TABINDEX_ID") && _propList.get("TABINDEX_ID").equals("")==false)
						{
							_propList.put("id", _propList.get("TABINDEX_ID"));
						}
						
						try {
							if(repeatValueCheckMap.containsKey(_itemId) == false) _ubPDF.toPdfOneItem(_propList, String.valueOf(_exportPageIdx));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						_propList = null;
						
					}
					// 라벨밴드 반복 종료
					
				}

			}
			
			//repeatValue 를 이용하여 아이템의 height값을 업데이트 하고 필요없는 아이템을 리스트에서 제거
			if( !repeatValueCheckMap.isEmpty() )repeatValueCheckSimplePDF(repeatValueCheckMap, _ubPDF , String.valueOf(_exportPageIdx) );
			
		}
			
		return _objects;
	}
	
	/**
	 *	PDF변환시 repeatValue 처리 
	 *	페이지의 아이템이 생성 완료후 repeatValue된 아이템을 pdf에 생성한다.   
	 **/
	private boolean repeatValueCheckSimplePDF( HashMap<String, ArrayList<HashMap<String, Object>>> repeatItem, ubFormToPDF _ubPDF,String _pageStr )
	{
		
		Collection<?> keys = repeatItem.keySet();
		HashMap<String, Object> argoItem = null;
		HashMap<String, Object> checkItem = null;
		float updateY = 0;
		String argoText = "";
		boolean _resultFlag = false;
		int _size = 0;
		
		ArrayList<HashMap<String, Object>> _addItemList = new ArrayList<HashMap<String,Object>>();
		
		for(Object key: keys){
			
			argoItem = null;
			argoText = "";
			updateY = 0;
			
			_size = repeatItem.get(key).size();
			
			for (int i = 0; i < _size; i++) {
				
				checkItem = (HashMap<String, Object>) repeatItem.get(key).get(i).get("item");
				
				if( argoItem == null || argoText.equals(repeatItem.get(key).get(i).get("datatext").toString()) == false )
				{
					argoItem = checkItem;
					argoText = repeatItem.get(key).get(i).get("datatext").toString();
					
					_addItemList.add(argoItem);
				}
				else
				{
					updateY = Float.valueOf(  checkItem.get("y").toString() ) +  Float.valueOf(  checkItem.get("height").toString() ) - Float.valueOf( argoItem.get("y").toString() );
					argoItem.put("height", updateY);
					
					checkItem = null;
				}
			}
			
			repeatItem.get(key).clear();
		}
		
		repeatItem.clear();
		repeatItem = null;
		
		if( _addItemList.size() > 0 )
		{
			_size = _addItemList.size();
			
			for( int i = 0; i < _size; i++ )
			{
				 try {
					_ubPDF.toPdfOneItem(_addItemList.get(i), _pageStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			_addItemList.clear();
			_resultFlag = true;
		}
		
		return _resultFlag;
	}
	
	
}
