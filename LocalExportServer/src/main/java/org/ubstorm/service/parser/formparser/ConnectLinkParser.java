package org.ubstorm.service.parser.formparser;

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.docx4j.docProps.variantTypes.Array;
import org.json.simple.JSONObject;
//import org.ubstorm.service.DataServiceManager;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.CreateFormProcess;
import org.ubstorm.service.parser.DataSetProcess;
import org.ubstorm.service.parser.ItemPropertyProcess;
import org.ubstorm.service.parser.PageNumProcess;
import org.ubstorm.service.parser.TotalPageCheckParser;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.TempletItemInfo;
import org.ubstorm.service.parser.formparser.data.Value;
//import org.ubstorm.service.request.ServiceRequestManager;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.corba.se.impl.oa.poa.ActiveObjectMap.Key;

public class ConnectLinkParser {

	
	private UDMParamSet m_appParams = null;
	private String mChartDataFileName = "chartdata.dat";
	private Function mFunction;
//	private ServiceRequestManager mServiceReqMng;
	private String mServerURL;
	
	private ItemPropertyProcess mPropertyFn;
	
	// Export여부를 판단하기 위한 변수
	private String isExportType = "";

	private HashMap<String, TempletItemInfo> mTempletInfo;
	
	private int mMinimumResizeFontSize = 0;	// resizeFont 사용시 최소값 지정
	boolean isExportData = false;
	
	public boolean isExportData() {
		return isExportData;
	}

	public void setExportData(boolean isExportData) {
		this.isExportData = isExportData;
	}
	
	
	
	public ConnectLinkParser() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ConnectLinkParser(String srvUrl, UDMParamSet appParams) {
		super();
		// TODO Auto-generated constructor stub
		this.mServerURL = srvUrl;
		this.m_appParams = appParams;
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
	
	// 각 페이지 id 를 키값으로 각 페이지별 데이터셋을 담아두는 객체
	private HashMap<String, HashMap<String, ArrayList<HashMap<String, Object>>>> pagesData = new HashMap<String, HashMap<String,ArrayList<HashMap<String,Object>>>>();
	private ArrayList<HashMap<String, Object>> pagesInfoData = new ArrayList<HashMap<String,Object>>();
//	private Document mDocument;
	
	
	HashMap<String, Element> marDataSetItems;
	HashMap<String, HashMap<String, String>> mDataSetParam;
	HashMap<String, Element> marDataSetMerged;
	
	private HashMap<String, Object> mParam;
	HashMap<String, Object> pageInfoData = new HashMap<String, Object>();
	private DataSetProcess mDataSetFn = new DataSetProcess();
	
	//1. xml을 읽어들여 UBDocArea 아이템을 읽어들여 각 프로젝트를 읽어들이기.
	public HashMap<String, Object> loadPagesData(Element _page, HashMap<String, Object> _param, ArrayList<Integer> mXAr ) throws XPathExpressionException, UnsupportedEncodingException, ScriptException
	{
		int i = 0;
		int j = 0;
		int k = 0;
		Element itemElement = null;
		Element paramElement = null;
		Element propertyElement = null;
		
		NodeList _nodes = ((Element) _page).getElementsByTagName("item");
		NodeList _propertys = null;
		NodeList _paramPropertys = null;
		
		mParam = _param;
		
		String name = "";
		String value = "";
		XPath _xpath = XPathFactory.newInstance().newXPath();
		
		// 각 페이지별 몃개의 문서가 로드 되는지 확인
		
		ArrayList<HashMap<String, Object>> projectList = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> paramData = new HashMap<String, Object>();
		
		for ( i = 0; i < _nodes.getLength(); i++) {
			itemElement = (Element) _nodes.item(i);
			paramData = new HashMap<String, Object>();
			
			_propertys = (NodeList)_xpath.evaluate("./property", itemElement, XPathConstants.NODESET);
//			_propertys = itemElement.getElementsByTagName("property");
			
			for ( j = 0; j < _propertys.getLength(); j++) {
				
				propertyElement = (Element) _propertys.item(j);
				
				name = propertyElement.getAttribute("name");
				value = propertyElement.getAttribute("value");
				
				// param정보 담기
				paramData.put(name, value);
			}
			
			ArrayList<HashMap<String, String>> paramList = new ArrayList<HashMap<String,String>>();
			HashMap<String, String> paramMap = new HashMap<String, String>();
			HashMap<String, HashMap<String, String>> paramInfo = new HashMap<String, HashMap<String, String>>();
			
			_propertys = (NodeList)_xpath.evaluate("./docParam/params/param", itemElement, XPathConstants.NODESET);
			for ( j = 0; j < _propertys.getLength(); j++) {
				
				paramMap = new HashMap<String, String>();
				
				paramElement = (Element) _propertys.item(j); 
				_paramPropertys =  (NodeList)_xpath.evaluate("./property", paramElement, XPathConstants.NODESET);
				for ( k = 0; k < _paramPropertys.getLength(); k++) {
					
					propertyElement = (Element) _paramPropertys.item(k);
					
					name = propertyElement.getAttribute("name");
					value = propertyElement.getAttribute("value");
					
					paramMap.put(name, value);
				}
				
				//{id=dataset_0, desc=Desc, value=undefined, type=String, key=ds0}
				if( paramMap.containsKey("key") && mParam.containsKey( paramMap.get("key") ) )
				{
					paramInfo.put( paramMap.get("id").toString()  , (HashMap<String, String>) mParam.get( paramMap.get("key") ) );
				}
				paramList.add(paramMap);
				
			}
			
			for(Object key : mParam.keySet())
			{
				if( mParam.containsKey(key) && paramInfo.containsKey(key) == false )
				{
					HashMap<String, String> _paValue = (HashMap<String, String>) mParam.get(key);
					paramInfo.put((String) key, _paValue );
				}
			}
			
			
			paramData.put("param", paramList);
			paramData.put("parameter", paramInfo);
			loadXmlData( itemElement.getAttribute("id"), paramData );
		}
		
		
		
		
		return loadProjects(mXAr);
		
	}
	// projectList  를 이용하여 프로젝트 xml을 로드하고 dataSet정보도 로드
	private void loadXmlData( String itemID,HashMap<String, Object> paramData )
	{
		pageInfoData = new HashMap<String, Object>();
		String TMP_FILE_PATH = "";
		String filePath = "";
		String fileContents = "";
		String XML_DATA = "";
		String PROJECT_NAME = "";
		String FOLDER_NAME = "";
		String FILE_NAME = m_appParams.getREQ_INFO().getFILE_NAME();
		File sFile = null;
		mPropertyFn = new ItemPropertyProcess();
		
		try {
			
//			if(paramData.get("projectName").toString().indexOf("/sample/") >= 0)
//				TMP_FILE_PATH = "UFile/sys" + paramData.get("projectName");
//			else
//				TMP_FILE_PATH = "UFile/project/" + paramData.get("projectName").toString() + "/" + paramData.get("formId").toString();
//							
//			if(FILE_NAME.lastIndexOf(".ubs") > 0)
//				TMP_FILE_PATH = TMP_FILE_PATH + "/" + FILE_NAME;
//			else
//				TMP_FILE_PATH = TMP_FILE_PATH + "/Mview.ubx";
//			
//			filePath = Log.ufilePath + TMP_FILE_PATH;
			
			XML_DATA = common.getUBFXmlData(paramData.get("projectName").toString(), paramData.get("formId").toString(), FILE_NAME, null, "", "", "", "", "");
			
			sFile = new File(filePath);

			if(XML_DATA == null)
			{
				return;
			}
			
			InputSource _is = new InputSource(new StringReader(XML_DATA));
			
			Document mDocument;
			mDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
			
			HashMap<String, HashMap<String, String>> paramInfo = (HashMap<String, HashMap<String, String>>) paramData.get("parameter");
			
			pageInfoData.put("document", mDocument);
			pageInfoData.put("id", itemID);
			pageInfoData.put("PARAMETER", paramInfo);
			pageInfoData.put("itemProperty", paramData);
			
			marDataSetMerged = new HashMap<String, Element>();
			
			pagesInfoData.add(pageInfoData);
			
			xmlToDataSet(itemID, mDocument, paramInfo);
			
		
		}
		catch(Exception e) {
			e.printStackTrace();
//			this.m_reqManager.loadCrossDomainEvent(Log.MSG_ERROR, Log.MSG_SYSTEM_EXCEPTION, e.getMessage());
		}
			
	}
	
	//2. 프로젝트별 ubDmc를 이용하여 데이터셋을 로드하여 프로젝트별 데이터셋을 담기
	// 전체 데이터셋의 Row수를 포함한 정보를 가져온다.
		private void xmlToDataSet( String pageID, Document document, HashMap<String, HashMap<String, String>> _paramInfo ) throws Exception
		{
			// xml DATASET;
			NodeList _itemList = document.getElementsByTagName("item");
			HashMap<String, Element> marDataSetItems = new HashMap<String, Element>();
			int leng = _itemList.getLength();
			//HashMap<String, Integer> mDataSetRowCountInfo = new HashMap<String, Integer>();
			String _client_Edt_Mode = "false";
			NodeList _projectList = document.getElementsByTagName("project");
			Element _project = (Element) _projectList.item(0);
			
			if( _project.hasAttribute("clientEditMode") )_client_Edt_Mode = _project.getAttribute("clientEditMode");
			
			for( int d = leng - 1 ; d > 0 ; d--)
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
					
					if( _mergedInfo.hasChildNodes() )
					{
						// merged 테그 담기.!!!;
						marDataSetMerged.put(_dataId, _mergedInfo);
					}
					
					marDataSetItems.put(_dataId, _itemE);
				}
				else
				{
					break;
				}
			}
			
			mDataSetParam = paramCheck_Handler(document, _paramInfo);
			if( mDataSetParam == null )
			{
				return;
			}
			
			//DataServiceManager oService = this.mServiceReqMng.getServiceManager();
			HashMap<String, ArrayList<HashMap<String, Object>>> dataSet = mDataSetFn.dataSetLoad(this.mServerURL, marDataSetItems , mDataSetParam, marDataSetMerged, _client_Edt_Mode);		
			
			pagesData.put(pageID, dataSet);	// 각각의 아이템별로 데이터셋을 로드
		}
	
		private HashMap<String, HashMap<String, String>> paramCheck_Handler( Document document, HashMap<String, HashMap<String, String>> _param ) throws UnsupportedEncodingException
		{
			// param Check;
			NodeList _paramList = document.getElementsByTagName("param");

			HashMap<String, HashMap<String, String>> _paramMap = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> _paramProp = null;

			for(int _p = 0; _p < _paramList.getLength() ; _p++)
			{
				Element _paramItem = (Element) _paramList.item(_p);

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

			if( _paramMap.size() != 0)
			{
				for(String key : _paramMap.keySet())
				{	
					if(_param.containsKey(key))
					{
						HashMap<String, String> _paValue = (HashMap<String, String>) _param.get(key);

						_paramMap.put(key, _paValue );
					}
					else if( mParam.containsKey(key))
					{
						HashMap<String, String> _paValue = (HashMap<String, String>) mParam.get(key);

						_paramMap.put(key, _paValue );
					}
				}
			}
			
			for(Object key : mParam.keySet())
			{
				if( mParam.containsKey(key))
				{
					HashMap<String, String> _paValue = (HashMap<String, String>) mParam.get(key);

					_paramMap.put((String) key, _paValue );
				}
			}

			for(Object key : _param.keySet())
			{
				if( _param.containsKey(key))
				{
					HashMap<String, String> _paValue = (HashMap<String, String>) _param.get(key);
					
					_paramMap.put((String) key, _paValue );
				}
			}

			return _paramMap;
		}
		
		
	//3. 각 프로젝트별 페이지데이터를  가져와서 프로젝트 타입별로 화면에 그리기
	
	//NodeList _pageList = mDocument.getElementsByTagName("page");
	private HashMap<String, Object> loadProjects(ArrayList<Integer> mXAr) throws UnsupportedEncodingException, XPathExpressionException, ScriptException
	{
		
		Element _page;
		Document _doc;
		NodeList _pages;
		NodeList _items;
		
		String cloneData = "";
		boolean clonePage = false;
		
		int _reportType = 0;
		
		float pageWidth = 0;
		float pageHeight = 0;
		
		int i = 0;
		int j = 0;
		int k = 0;
		
		int pageTotalNum = 0;
		int pageTotalRealNum = 0;
		PageNumProcess mPageNumFn = new PageNumProcess();
		
		ArrayList<HashMap<String, Object>> pageObj = null;
		HashMap<Integer, Object> InfoDataMap = null;
		CreateFormProcess mCreateFormFn = null;
		ArrayList<ArrayList<HashMap<String, Object>>> allPageObj = new ArrayList<ArrayList<HashMap<String,Object>>>();
		ArrayList<Integer> pageCnt = new ArrayList<Integer>();
		ArrayList<Integer> pageRealCnt = new ArrayList<Integer>();
		ArrayList<Object> tempDataSets = new ArrayList<Object>();
		ArrayList<Element> pagesArr = new ArrayList<Element>();
		int _realPageCnt = 0;
		
		//pageInfoData
		int totalPageNum = 0;
		int totalRealPageNum = 0;
		
		for ( i = 0; i < pagesInfoData.size(); i++) {
			// pagesInfoData 이용하여 안쪽의 Element값으로 화면데이터를 가져오고 width와 height값을 이용하여 전체 화면사이즈를 x,y값을 이용하여 좌표를 지정
			
			_doc = (Document) pagesInfoData.get(i).get("document");
			
			_pages = _doc.getElementsByTagName("page");
			InfoDataMap = new HashMap<Integer, Object>();
			pageCnt = new ArrayList<Integer>();
			tempDataSets = new ArrayList<Object>();
			pagesArr = new ArrayList<Element>();
			HashMap<String, Object> _pageParam = (HashMap<String, Object>) pagesInfoData.get(i).get("parameter");
			HashMap<String, Object> _parameterInfo = (HashMap<String, Object>) pagesInfoData.get(i).get("PARAMETER");
			pageTotalNum = 0;
			pageTotalRealNum = 0;
			
			// 각 페이지별 총 페이지수를 구하기
			// 이후 총 페이지에 맞춰 각각의 페이지아이템 생성

			int _addIndex = 0;
			
			for ( j = 0; j < _pages.getLength(); j++) {
				
				_page = (Element) _pages.item(j);
				
				_reportType = Integer.parseInt(_page.getAttribute("reportType"));
				cloneData = _page.getAttribute("clone");
				
				clonePage = false;
				
				// Clone 페이지 일경우 Width/Height값을 나누워서 담는다
				pageWidth = Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("width").toString() );
				pageHeight = Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("height").toString() );
				
				int _minimumResizeFontSize = 0;
				if( _page.hasAttribute("minimumResizeFontSize") && StringUtil.isInteger(_page.getAttribute("minimumResizeFontSize")) )
				{
					_minimumResizeFontSize = Integer.valueOf(_page.getAttribute("minimumResizeFontSize"));
				}

				float originalPageWidth = pageWidth;
				float originalPageHeight = pageHeight;
				ArrayList<Float> _clonePositionList = null;


				int _cloneColCnt = 1;
				int _cloneRowCnt = 1;
				int _cloneRepCnt = 1;
				String _cloneDirect = "";	// cloneDirect Across Down: 가로, Down Across : 세로

				if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL)  || cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM))
				{
					if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
					{
					  _cloneColCnt = 1;
					  _cloneRowCnt = 2;
					}
					else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) )
					{
					  _cloneColCnt = 2;
					  _cloneRowCnt = 1;
					}
					else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM) )
					{
					  _cloneColCnt = Integer.parseInt(_page.getAttribute("cloneColCount"));
					  _cloneRowCnt = Integer.parseInt(_page.getAttribute("cloneRowCount"));
					}
	
					if(_page.hasAttribute("direction"))
					{
					  _cloneDirect = _page.getAttribute("direction").toString();
					}
	
					pageWidth = pageWidth / _cloneColCnt;
					pageHeight = pageHeight / _cloneRowCnt;
	
					_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
					clonePage = true;
				}
				
				// 그룹핑 속성이 존재할경우 데이터를 그룹핑하여 그룹수만큼 반복하여 Add시킨다.
				boolean isGrouping = false;
				int _grpCnt = 1;
				
				HashMap<String, ArrayList<HashMap<String, Object>>> _tempData = pagesData.get( pagesInfoData.get(i).get("id").toString() );
				
				mFunction.setDatasetList(_tempData);
				mFunction.setParam(_parameterInfo);
				
				_grpCnt = 1;
				
				ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _groupingData = null;
				
				if( _page.getAttribute("isGroup") != null && _page.getAttribute("isGroup").equals("true") )
				{
					ArrayList<HashMap<String, Object>> _dataList = TotalPageCheckParser.checkGroupingDataInfo(_page);
					if( _dataList != null && _dataList.size() > 0 )
					{
						isGrouping = true;
						
						_groupingData = TotalPageCheckParser.getGroupingDataSet( _tempData, _dataList);
						_grpCnt = _groupingData.size();
					}
				}
				
				
				for (int l = 0; l < _grpCnt; l++) {
					
					if(isGrouping)
					{
						_tempData = _groupingData.get(l);
						tempDataSets.add(_groupingData.get(l));
					}
					else
					{
						_tempData = pagesData.get( pagesInfoData.get(i).get("id").toString() );
						tempDataSets.add(null);
					}
					
					if( _page.getAttribute("isConnect").equals("true") ) continue;
					
					pagesArr.add(_page);
					
					// item ArrayList 받아오는 부분을 변수(pageObj)로 처리하고, isExport 인 경우에 사용하도록 변경. 2015-10-22 공혜지.
					switch (_reportType) {
						case 0: //coverPage
							if( mCreateFormFn == null )
							{
								mCreateFormFn = new CreateFormProcess();
								mCreateFormFn.init(this.m_appParams);
							}
							
							pageObj = mCreateFormFn.CreateFreeFormAll(_page , _tempData , (HashMap<String, Object>) _parameterInfo , 0,0,0);
							
							pageTotalNum = pageTotalNum + 1;
							
							pageTotalRealNum = pageTotalRealNum + 1;
							
							pageInfoData = new HashMap<String, Object >();
							
							InfoDataMap.put(_addIndex, null);
							pageCnt.add(1);
							pageRealCnt.add(1);
							
							break;
						case 1: //freeform
							
							if( mCreateFormFn == null )
							{
								mCreateFormFn = new CreateFormProcess();
								mCreateFormFn.init(this.m_appParams);
							}
//							int _freeTotCnt =  getFreeFormTotalPages( _page, _tempData);
							
							_realPageCnt = mPageNumFn.getFreeFormTotalNum( _page , _tempData);
							int _freeTotCnt = _realPageCnt;
							
							if(clonePage)
							{
								_freeTotCnt = (int) Math.ceil((float) _freeTotCnt/_cloneRepCnt);
							}
							
							InfoDataMap.put(_addIndex, null);
							pageCnt.add(_freeTotCnt);
							pageRealCnt.add(_realPageCnt);
							
							pageTotalRealNum = pageTotalRealNum + _realPageCnt;
							
							pageTotalNum = pageTotalNum + _freeTotCnt;
							InfoDataMap.put(_addIndex, null);
	//						for ( k = 0; k < _freeTotCnt; k++) {
	//							pageObj = mCreateFormFn.CreateFreeFormAll(_page , pagesData.get( pagesInfoData.get(i).get("id").toString() ) , mParam , k);
	//							
	//							totalPageNum++;
	//						}
							break;
						case 2:
							// masterBand
							MasterBandParser masterParser = new MasterBandParser(this.m_appParams);
							masterParser.setFunction(mFunction);
							
							masterParser.setExportData(isExportData);
							
							masterParser.setMinimumResizeFontSize(_minimumResizeFontSize);
							
							ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
							ArrayList<Object> masterInfo 					= masterParser.loadTotalPage(_page, _tempData,0, pageHeight, pageWidth, mXAr);
							_realPageCnt 								= (Integer) masterInfo.get(0);
							ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
							
							int _masterTotPage  = _realPageCnt;
							
							if(clonePage)
							{
								_masterTotPage = (int) Math.ceil((float) _masterTotPage/_cloneRepCnt);
							}
							
							InfoDataMap.put(_addIndex, masterInfo);
							pageCnt.add(_masterTotPage);
							pageRealCnt.add(_realPageCnt);
							
							pageTotalNum = pageTotalNum + _masterTotPage;
							pageTotalRealNum = pageTotalRealNum + _realPageCnt;
							
							break;
							
						case 3:
							// ContinueBand 처리
							ContinueBandParser continueBandParser = new ContinueBandParser(m_appParams);
							continueBandParser.setFunction(mFunction);
							
							continueBandParser.setExportData(isExportData);
							continueBandParser.setMinimumResizeFontSize(_minimumResizeFontSize);
							
							ArrayList<Object> bandAr = continueBandParser.loadTotalPage(_page,  _tempData,0, pageHeight, pageWidth, mXAr, _pages, _parameterInfo);
							
							HashMap<String, BandInfoMapData> bandInfo 	= (HashMap<String, BandInfoMapData>) bandAr.get(0);
							ArrayList<BandInfoMapData> bandList 		= (ArrayList<BandInfoMapData>) bandAr.get(1);
							ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList 	= (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
							HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
							
							_realPageCnt = pagesRowList.size();
							int _continueTotPage = _realPageCnt;
							if(clonePage)
							{
								_continueTotPage = (int) Math.ceil((float) _continueTotPage/_cloneRepCnt);
							}
							
							InfoDataMap.put(_addIndex, bandAr);
							pageCnt.add(_continueTotPage);
							pageRealCnt.add(_realPageCnt);
							
							pageTotalNum = pageTotalNum + _continueTotPage;
							pageTotalRealNum = pageTotalRealNum + _realPageCnt;
							break;
						case 4:
							if( mCreateFormFn == null )
							{
								mCreateFormFn = new CreateFormProcess();
								mCreateFormFn.init(this.m_appParams);
							}
							
							int _pageColumnIndex 	= 0;
							int _pageRowIndex 		= 0;
							int _pageStIndex 		= 0;
							HashMap<String, String> _pList;
							if(mParam.containsKey(GlobalVariableData.UB_LABEL_COL_INDEX))
							{
								_pList = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_LABEL_COL_INDEX);
								_pageColumnIndex = Integer.valueOf( _pList.get("parameter") );
							}
							if(mParam.containsKey(GlobalVariableData.UB_LABEL_ROW_INDEX))
							{
								_pList = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_LABEL_ROW_INDEX);
								_pageRowIndex = Integer.valueOf( _pList.get("parameter") );
							}
							if(mParam.containsKey(GlobalVariableData.UB_LABEL_ST_INDEX))
							{
								_pList = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_LABEL_ST_INDEX);
								_pageStIndex = Integer.valueOf( _pList.get("parameter") );
								_pageColumnIndex = _pageRowIndex = 0;
							}
							
							_realPageCnt =  getLabelBandTotalNum( _page, _tempData, _pageColumnIndex, _pageRowIndex, _pageStIndex );
							int _typePageNum = _realPageCnt;
							
							InfoDataMap.put(_addIndex, null);
							
							if(clonePage)
							{
								_typePageNum = (int) Math.ceil((float) _typePageNum/_cloneRepCnt);
							}
							
							pageCnt.add(_typePageNum);
							pageRealCnt.add(_realPageCnt);
							
							pageTotalNum = pageTotalNum + _typePageNum;
							pageTotalRealNum = pageTotalRealNum + _realPageCnt;
							break;
						case 8:
							
							InfoDataMap.put(_addIndex, null);
							pageCnt.add(1);
							pageRealCnt.add(1);
							_realPageCnt = 1;
							
							pageTotalNum = pageTotalNum + 1;
							pageTotalRealNum = pageTotalRealNum + _realPageCnt;
							break;
						default:
							break;
					}
					
					_addIndex++;
				}
			}
			
			pagesInfoData.get(i).put("PAGE_AR", pagesArr);
			pagesInfoData.get(i).put("pageCnt", pageTotalNum);
			pagesInfoData.get(i).put("pageRealCnt", pageTotalRealNum);
			pagesInfoData.get(i).put("infoData", InfoDataMap);
			pagesInfoData.get(i).put("pagesCntList", pageCnt);
			pagesInfoData.get(i).put("pagesRealCntList", pageRealCnt);
			pagesInfoData.get(i).put("TEMP_DATASET_LIST", tempDataSets);
		
			if(pageTotalNum > totalPageNum )
			{
				totalPageNum = pageTotalNum;
			}
			
			if( pageTotalRealNum >  totalRealPageNum )
			{
				totalRealPageNum = pageTotalRealNum;
			}
		}
		
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("totalpage", totalPageNum);
		retMap.put("totalRealPage", totalRealPageNum);
		retMap.put("pageInfoData", pagesInfoData);
		
		return retMap;
	}
	
	public void makeConnectPage( int _pageNum, Object pageInfoData, ArrayList<HashMap<String, Object>> _objects, int _totalPageNum, int _currentPageNum ) throws UnsupportedEncodingException, XPathExpressionException, ScriptException
	{
		makeConnectPage( _pageNum, pageInfoData, _objects, _totalPageNum, _currentPageNum, 0, 0);
	}
	
	public void makeConnectPage( int _pageNum, Object pageInfoData, ArrayList<HashMap<String, Object>> _objects, int _totalPageNum, int _currentPageNum, float _pagePrintMarginLeft,  float _pagePrintMarginTop ) throws UnsupportedEncodingException, XPathExpressionException, ScriptException
	{
		Element _page;
		Document _doc;
		NodeList _pages;
		NodeList _items;
		
		String cloneData = "";
		boolean clonePage = false;
		
		int _reportType = 0;
		
		float pageWidth = 0;
		float pageHeight = 0;

		float originalPageWidth = 0;
		float originalPageHeight = 0;
		ArrayList<Float> _clonePositionList = null;
		
		int i = 0;
		int j = 0;
		int k = 0;
		
		float cloneY = 0;
		float cloneX = 0;
		
		ArrayList<HashMap<String, Object>> pageObj = null;
		ArrayList<ArrayList<HashMap<String, Object>>> allPageObj = new ArrayList<ArrayList<HashMap<String,Object>>>();
		
		int _start = 0;
		int _max = 0;
		
		int _pageStartIndex =  0;
		
		int _pageCount = 0;
		
		ArrayList<Integer> pagesCntList = null;
		HashMap<Integer, Object> InfoDataMap = null;
		ArrayList<Object> _tempDatas = null;
		ArrayList<Element> _pagesArr = null;
		HashMap<String, Object> _pageParam = null;
		HashMap<String, Object> _parameterInfo = null;
		
		CreateFormProcess mCreateFormFn = new CreateFormProcess();
		mCreateFormFn.init(this.m_appParams);
		mCreateFormFn.setFunction(mFunction);
		
		mCreateFormFn.setIsExportType(isExportType);
		
		for ( i = 0; i < pagesInfoData.size(); i++) {
			// pagesInfoData 이용하여 안쪽의 Element값으로 화면데이터를 가져오고 width와 height값을 이용하여 전체 화면사이즈를 x,y값을 이용하여 좌표를 지정
			_pageStartIndex = _pageNum;
			_doc = (Document) pagesInfoData.get(i).get("document");
			
			 _pageParam = (HashMap<String, Object>) pagesInfoData.get(i).get("parameter");
			 _parameterInfo = (HashMap<String, Object>) pagesInfoData.get(i).get("PARAMETER");
			 
			_pagesArr = (ArrayList<Element>) pagesInfoData.get(i).get("PAGE_AR");
			_pageCount = _pagesArr.size();
			
//			_pages = _doc.getElementsByTagName("page");
			
			pagesCntList = (ArrayList<Integer>) pagesInfoData.get(i).get("pagesRealCntList");
			
			InfoDataMap = (HashMap<Integer, Object>) pagesInfoData.get(i).get("infoData");
			
			if( Integer.valueOf( pagesInfoData.get(i).get("pageRealCnt").toString() ) < _pageNum )
			{
				continue;
			}
			
			if( pagesInfoData.get(i).containsKey("TEMP_DATASET_LIST") )
			{
				_tempDatas = (ArrayList<Object>) pagesInfoData.get(i).get("TEMP_DATASET_LIST");
			}
			
			HashMap<String, ArrayList<HashMap<String, Object>>> _tempData = null;
			
			
			// 넘겨받은 페이지에 맞춰서 각 페이지의 화면을 생성 
			for ( j = 0; j < _pageCount; j++) {
				
				if( _pageStartIndex >= pagesCntList.get(i) )
				{
					_pageStartIndex = _pageStartIndex - pagesCntList.get(i);
					continue;
				}
				
				if(_tempDatas.size() > 0 && _tempDatas.get(j) != null)
				{
					_tempData = (HashMap<String, ArrayList<HashMap<String,Object>>>) _tempDatas.get(j);
				}
				else
				{
					_tempData = pagesData.get( pagesInfoData.get(i).get("id").toString() );
				}
				
				mFunction.setDatasetList(_tempData);
				mFunction.setParam(_parameterInfo);

//				_page = (Element) _pages.item(j);
				_page = _pagesArr.get(j);
				
				_reportType = Integer.parseInt(_page.getAttribute("reportType"));
				cloneData = _page.getAttribute("clone");
				
				clonePage = false;
				
				// Clone 페이지 일경우 Width/Height값을 나누워서 담는다
				pageWidth = Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("width").toString() );
				pageHeight = Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("height").toString() );
				originalPageWidth = pageWidth;
				originalPageHeight = pageHeight;
				
				int _cloneColCnt = 1;
				int _cloneRowCnt = 1;
				int _cloneRepCnt = 1;
				String _cloneDirect = "";	// cloneDirect Across Down: 가로, Down Across : 세로
				int _pageRepeatCnt = 0;
				
				if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL)  || cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM))
				{
					if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
					{
						_cloneColCnt = 1;
						_cloneRowCnt = 2;
					}
					else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) )
					{
						_cloneColCnt = 2;
						_cloneRowCnt = 1;
					}
					else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM) )
					{
						_cloneColCnt = Integer.parseInt(_page.getAttribute("cloneColCount"));
						_cloneRowCnt = Integer.parseInt(_page.getAttribute("cloneRowCount"));
					}
					
					if(_page.hasAttribute("direction"))
					{
						_cloneDirect = _page.getAttribute("direction").toString();
					}
					
					pageWidth = pageWidth / _cloneColCnt;
					pageHeight = pageHeight / _cloneRowCnt;
					
					_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
					clonePage = true;
				}
				
				// item ArrayList 받아오는 부분을 변수(pageObj)로 처리하고, isExport 인 경우에 사용하도록 변경. 2015-10-22 공혜지.
				switch (_reportType) {
					case 0: //coverPage
						
						// Section Page 지정
						mFunction.setSectionCurrentPageNum(0);
						mFunction.setSectionTotalPageNum(1);
						
						cloneY = cloneY + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("y").toString() );
						cloneX = cloneX + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("x").toString() );
						
						if(_pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
						if(_pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;
						
						_objects = mCreateFormFn.createFreeFormConnect( _page, _tempData, _parameterInfo, 0, cloneX, cloneY, _objects,_totalPageNum,_currentPageNum);
						break;
					case 1: //freeform
						if(clonePage)
						{
							_start = _pageStartIndex*_cloneRepCnt;
							
							_max = _start + _cloneRepCnt;
							//마지막 페이지가 max값보다 작을경우 처
							if( _max >= pagesCntList.get(j) )
							{
								_max = pagesCntList.get(j);
							}
						}
						else
						{
							_start = _pageStartIndex;
							_max = _pageStartIndex + 1;
						}
						
						for ( k = _start; k < _max; k++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(k);
							mFunction.setSectionTotalPageNum(_max);
							
							// clone페이지의 포지션 인덱스값
							_pageRepeatCnt = k%_cloneRepCnt;
							
							mFunction.setCloneIndex(_pageRepeatCnt);
							
							if(clonePage && k%_cloneRepCnt > 0 )
							{
								// pageInfo의 width와 height값을 이용하여 
								  _clonePositionList = ItemConvertParser.getClonePosition( k, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
								  cloneX = _clonePositionList.get(0);
								  cloneY = _clonePositionList.get(1);
							}
							else
							{
								cloneY = 0;	// pageInfo의 x,y값을 더하여 처리 
								cloneX = 0;
							}
							
							cloneX = cloneX + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("x").toString() );
							cloneY = cloneY + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("y").toString() );
							
							if(_pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
							if(_pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;
							
							_objects = mCreateFormFn.createFreeFormConnect( _page, _tempData , _parameterInfo, k, cloneX, cloneY, _objects,_totalPageNum,_currentPageNum);
						}
						
						break;
					case 2:
						// masterBand
						MasterBandParser masterParser = new MasterBandParser(this.m_appParams);
						masterParser.setFunction(mFunction);
						ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
						masterParser.DataSet 							= _tempData;
						ArrayList<Object> masterInfo 					= (ArrayList<Object>) InfoDataMap.get(j);
//						ArrayList<Object> masterInfo 					= masterParser.loadTotalPage(_page, pagesData.get( pagesInfoData.get(i).get("id").toString() ),0, pageHeight, pageWidth);
						int _masterTotPage 								= (Integer) masterInfo.get(0);
						ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
						
						masterParser.setIsExportType(isExportType);
						
						if(clonePage)
						{
							_start = _pageStartIndex*_cloneRepCnt;
							
							_max = _start + _cloneRepCnt;
							//마지막 페이지가 max값보다 작을경우 처 
							if( _max >= _masterTotPage )
							{
								_max = _masterTotPage;
							}
						}
						else
						{
							_start = _pageStartIndex;
							_max = _pageStartIndex + 1;
						}
						
						for ( k = _start; k < _max; k++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(k);
							mFunction.setSectionTotalPageNum(_max);
							
							// clone페이지의 포지션 인덱스값
							_pageRepeatCnt = k%_cloneRepCnt;
							
							mFunction.setCloneIndex(_pageRepeatCnt);
							
							if(clonePage && j%_cloneRepCnt > 0 )
							{
							  _clonePositionList = ItemConvertParser.getClonePosition( k, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
							  cloneX = _clonePositionList.get(0);
							  cloneY = _clonePositionList.get(1);

							}
							else
							{
								cloneY = 0;	// pageInfo의 x,y값을 더하여 처리 
								cloneX = 0;
							}
							if(_pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
							if(_pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;
							
							cloneY = cloneY + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("y").toString() );
							cloneX = cloneX + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("x").toString() );
							
							_objects = masterParser.createMasterBandData(k, masterList, pageWidth, pageHeight, _parameterInfo, cloneX, cloneY, _objects,_totalPageNum,_currentPageNum );
						}
						
						break;
						
					case 3:
						// ContinueBand 처리
						ContinueBandParser continueBandParser = new ContinueBandParser(m_appParams);
						continueBandParser.setFunction(mFunction);
						continueBandParser.DataSet 					= _tempData;
						ArrayList<Object> bandAr 					= (ArrayList<Object>)(ArrayList<Object>) InfoDataMap.get(j);
//						ArrayList<Object> bandAr = continueBandParser.loadTotalPage(_page,  pagesData.get( pagesInfoData.get(i).get("id").toString() ),0, pageHeight, pageWidth);
						
						HashMap<String, BandInfoMapData> bandInfo 	= (HashMap<String, BandInfoMapData>) bandAr.get(0);
						ArrayList<BandInfoMapData> bandList 		= (ArrayList<BandInfoMapData>) bandAr.get(1);
						ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList 	= (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
						HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
						
						// group함수를 위한 객체 function처리하기 위하여 페이지 이동 함수로 전달하여 진행
						HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
						ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
						
						// 그룹합수용 데이터를 매핑
						continueBandParser.mOriginalDataMap = originalDataMap;
						continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
						
						continueBandParser.setIsExportType(isExportType);
						
						if(  bandAr.size() > 8 )
						{
							mFunction.setGroupBandCntMap((HashMap<String, Integer>) bandAr.get(8));
						}
						
						boolean isPivot = false;
						
						if(clonePage)
						{
							_start = _pageStartIndex*_cloneRepCnt;
							
							_max = _start + _cloneRepCnt;
							//마지막 페이지가 max값보다 작을경우 처
							if( _max >= pagesRowList.size() )
							{
								_max = pagesRowList.size();
							}
						}
						else
						{
							_start = _pageStartIndex;
							_max = _pageStartIndex + 1;
						}
						
						for ( k = _start; k < _max; k++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(k);
							mFunction.setSectionTotalPageNum(_max);
							
							// clone페이지의 포지션 인덱스값
							_pageRepeatCnt = k%_cloneRepCnt;
							
							mFunction.setCloneIndex(_pageRepeatCnt);
							
							if(clonePage && k%_cloneRepCnt > 0 )
							{
								// pageInfo의 width와 height값을 이용하여 
								  _clonePositionList = ItemConvertParser.getClonePosition( k, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
								  cloneX = _clonePositionList.get(0);
								  cloneY = _clonePositionList.get(1);
							}
							else
							{
								cloneY = 0;	// pageInfo의 x,y값을 더하여 처리 
								cloneX = 0;
							}
							
							if(_pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
							if(_pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;
							
							cloneY = cloneY + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("y").toString() );
							cloneX = cloneX + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("x").toString() );
							
							if( k < pagesRowList.size() ) _objects = continueBandParser.createContinueBandItems( k, _tempData,  bandInfo, bandList, pagesRowList, _parameterInfo,crossTabData,cloneX,cloneY,_objects ,_totalPageNum,_currentPageNum, isPivot);
						}
						break;
					case 4: //labelBand
						if(clonePage)
						{
							_start = _pageStartIndex*_cloneRepCnt;
							
							_max = _start + _cloneRepCnt;
							//마지막 페이지가 max값보다 작을경우 처
							if( _max >= pagesCntList.get(j) )
							{
								_max = pagesCntList.get(j);
							}
						}
						else
						{
							_start = _pageStartIndex;
							_max = _pageStartIndex + 1;
						}
						
						for ( k = _start; k < _max; k++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(k);
							mFunction.setSectionTotalPageNum(_max);
							
							if(clonePage && i%_cloneRepCnt > 0 )
							{
								// pageInfo의 width와 height값을 이용하여 
								  _clonePositionList = ItemConvertParser.getClonePosition( j, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
								  cloneX = _clonePositionList.get(0);
								  cloneY = _clonePositionList.get(1);

							}
							else
							{
								cloneY = 0;	// pageInfo의 x,y값을 더하여 처리 
								cloneX = 0;
							}
							
							if(_pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
							if(_pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;
							
							cloneY = cloneY + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("y").toString() );
							cloneX = cloneX + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("x").toString() );
							
							_objects = mCreateFormFn.CreateLabelBandConnect( _page, _tempData, _parameterInfo, k, cloneX, cloneY, _objects, _totalPageNum, _currentPageNum);
						}
						break;
					case 8:

						cloneY = cloneY + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("y").toString() );
						cloneX = cloneX + Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("x").toString() );
						
						if(_pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
						if(_pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;
						
						_objects = mCreateFormFn.createFreeFormConnect( _page, _tempData , _parameterInfo, 0, cloneX, cloneY, _objects, _totalPageNum, _currentPageNum);
						break;
					default:
						break;
				}

				
				break;
			}
		}
	}
	
	
	
	
	
	
	
	
	private int getFreeFormTotalPages( Element _page, HashMap<String,ArrayList<HashMap<String, Object>>> _dataSet ) throws XPathExpressionException
	{
		int _totPageNum = 1;
		
		NodeList _items = _page.getElementsByTagName("item") ;
		Element _itemElement;
		Element _property;
		NodeList _propertys;
		XPath _xpath = XPathFactory.newInstance().newXPath();
		String dataType = "";
		String dataSet = "";
		
		
		for (int i = 0; i < _items.getLength(); i++) {
			
			_itemElement = (Element) _items.item(i);
			
			_propertys = (NodeList)_xpath.evaluate("./property[@name='dataType']|./property[@name='dataSet']", _itemElement, XPathConstants.NODESET);
			
			for (int j = 0; j < _propertys.getLength(); j++) {
				
				_property = (Element) _propertys.item(j);
				
				if( _property.getAttribute("name").equals("dataType") )
				{
					dataType = _property.getAttribute("value");
				}
				else if( _property.getAttribute("name").equals("dataSet") )
				{
					dataSet = _property.getAttribute("value");
				}
				
			}
			
			if( (dataType.equals("1") ||  dataType.equals("2")) && _dataSet.containsKey(dataSet)&& _dataSet.get(dataSet) != null )
			{
				if( _totPageNum < _dataSet.get(dataSet).size() )
				{
					_totPageNum = _dataSet.get(dataSet).size();
				}
			}
			
		}
		
		return _totPageNum;
	}
	
	
	
	
	
	public int getLabelBandTotalNum(Element _page, HashMap<String,ArrayList<HashMap<String, Object>>> _dataSet, int _startColIndex , int _startRowIndex, int _startIndex)
	{
		int _pageNum = 0;
		
		NodeList _child = _page.getElementsByTagName("item");
		String _dataId = "";
		int _colCnt = 0;
		int _rowCnt = 0;
		
		String _direction = "";
		
		// xml Item
		for(int j = 0; j < _child.getLength() ; j++)
		{
			Element _childItem = (Element) _child.item(j);
			
			String className = _childItem.getAttribute("className");
			
			NodeList _propertys = _childItem.getElementsByTagName("property");
			
			for(int p = 0; p < _propertys.getLength(); p++)
			{
				Element _propItem = (Element) _propertys.item(p);
				
				String _name = _propItem.getAttribute("name");
				String _value = _propItem.getAttribute("value");
				
				if( className.equals("UBLabelBand"))
				{
					if( _name.equals("columns"))
					{
						_colCnt = Integer.valueOf(_value);
					}
					else if( _name.equals("rows"))
					{
						_rowCnt = Integer.valueOf(_value);
					}
					else if( _name.equals("direction"))
					{
						_direction = _value;
					}

					
					if( _colCnt > 0 && _rowCnt > 0)
					{
						break;
					}
					
				}
				else
				{

					if("dataType".equals(_name))
					{
						if( _value.equals("1") )
						{

							if( _dataSet.containsKey(_dataId))
							{
								Integer dataCnt = _dataSet.get(_dataId).size();
								
								if( _startIndex > 0 || ( _startColIndex > 0 && _startColIndex <= _colCnt &&  _startRowIndex > 0 && _startRowIndex <= _rowCnt ) )
								{
									int _pageStartIndex = 0;
									if( _startIndex == 0 )
									{
										if("downCross".equals(_direction)) _pageStartIndex = (_rowCnt * (_startColIndex-1)) + _startRowIndex -1;
										else _pageStartIndex = (_colCnt * (_startRowIndex-1)) + _startColIndex -1;
									}
									else
									{
										_pageStartIndex = _startIndex;
									}
									
									if(dataCnt > 0 ) dataCnt = dataCnt + _pageStartIndex;
								}
								
								float _pCnt = (float) dataCnt / (_colCnt * _rowCnt);
								
								if( _pageNum < Math.ceil(_pCnt) )
								{
									_pageNum =  (int) Math.ceil(_pCnt);
								}
							}
							else
							{
								continue;
							}
						}
					}
					else if( "dataSet".equals(_name))
					{

						if( _value.equals(_dataId))
						{
							continue;
						}
						else
						{
							_dataId = _value;
						}
					}
				}
			}
		}
		
		return _pageNum;
	}
	
	
	
	public HashMap<String, HashMap<String,ArrayList<Object>>> getAllExportDatas( Integer _pageCnt, HashMap<String, Object> connectData )
	{
		Element _page;
		Document _doc;
		NodeList _pages;
		NodeList _items;
		
		String cloneData = "";
		boolean clonePage = false;
		
		int _reportType = 0;
		
		float pageWidth = 0;
		float pageHeight = 0;
		
		int i = 0;
		int j = 0;
		int k = 0;
		int m = 0;
		int l = 0;
		int n = 0;
		
		float cloneY = 0;
		float cloneX = 0;
		
		ArrayList<HashMap<String, Object>> pageObj = null;
		ArrayList<ArrayList<HashMap<String, Object>>> allPageObj = new ArrayList<ArrayList<HashMap<String,Object>>>();
		
		int _start = 0;
		int _max = 0;
		
		HashMap<Integer, Object> InfoDataMap = null;
		
		HashMap<String, HashMap<String, ArrayList<Object>>> returnDataMap = null;
		String dataName = "";
		ArrayList<Element> childrenList = null;
		
		HashMap<String, HashMap<String, String>> headerColumnMap = null;
		
		HashMap<String, ArrayList<String>> realDataCoumns = new HashMap<String,ArrayList<String>>();
		HashMap<String, ArrayList<String>> dataColumnDatas = new HashMap<String,ArrayList<String>>();
		
		NodeList itemPropertys;
		Element itemProperty;
		String _dataType = "";
		String _dataSetName = "";
		String _dataSetColumn = "";
		
		HashMap<String, HashMap<String,ArrayList<Object>>> resultDataSet = new HashMap<String, HashMap<String,ArrayList<Object>>>();
		
		for ( i = 0; i < pagesInfoData.size(); i++) {
			
			HashMap<String, HashMap<String, ArrayList<Object>>> _crossTabDataSet = null;
			
			// pagesInfoData 이용하여 안쪽의 Element값으로 화면데이터를 가져오고 width와 height값을 이용하여 전체 화면사이즈를 x,y값을 이용하여 좌표를 지정
			_doc = (Document) pagesInfoData.get(i).get("document");
			
			_pages = _doc.getElementsByTagName("page");
			
			InfoDataMap = (HashMap<Integer, Object>) pagesInfoData.get(i).get("infoData");
			
			// 넘겨받은 페이지에 맞춰서 각 페이지의 화면을 생성
			for ( j = 0; j < _pages.getLength(); j++) {
				
				_page = (Element) _pages.item(j);
				
				_reportType = Integer.parseInt(_page.getAttribute("reportType"));
				cloneData = _page.getAttribute("clone");
				
				clonePage = false;
				
				// Clone 페이지 일경우 Width/Height값을 나누워서 담는다
				pageWidth = Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("width").toString() );
				pageHeight = Float.valueOf( ((HashMap<String, Object>) pagesInfoData.get(i).get("itemProperty")).get("height").toString() );
				
				// item ArrayList 받아오는 부분을 변수(pageObj)로 처리하고, isExport 인 경우에 사용하도록 변경. 2015-10-22 공혜지.
				switch (_reportType) {
				
					case 1: //freeform
					case 4: //freeform
						// 아이템별 dataSet와 Column값을 담기
						_dataType = "";
						_dataSetName = "";
						_dataSetColumn = "";
						dataColumnDatas = new HashMap<String,ArrayList<String>>();
						_items = _page.getElementsByTagName("item");
						
						int _subMax = 0;
						Element _item;
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
						
						for (String key : dataColumnDatas.keySet()) {
							int _subMax3 = dataColumnDatas.get(key).size();
							for ( n = 0; n < _subMax3; n++) {
								
								if(realDataCoumns.containsKey(key) == false )
								{
									realDataCoumns.put( key, new ArrayList<String>());
								}
								
								if( realDataCoumns.get(key).contains( dataColumnDatas.get(key).get(n) ) == false )
								{
									realDataCoumns.get(key).add( dataColumnDatas.get(key).get(n) );
								}
							}
						}
						
						break;
					
					case 2: //Master Band 처리
						MasterBandParser masterParser = new MasterBandParser(this.m_appParams);
						
						masterParser.setExportData(true);
						
						masterParser.setFunction(mFunction);
						ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
						masterParser.DataSet 							= (HashMap<String, ArrayList<HashMap<String, Object>>>) pagesData.get( pagesInfoData.get(i).get("id").toString() );
						ArrayList<Object> masterInfo 					= (ArrayList<Object>) InfoDataMap.get(j);
//						ArrayList<Object> masterInfo 					= masterParser.loadTotalPage(_page, pagesData.get( pagesInfoData.get(i).get("id").toString() ),0, pageHeight, pageWidth);
						int _masterTotPage 								= (Integer) masterInfo.get(0);
						ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);	
						HashMap<String, Object> _totalItem = null;
						
						String _type = "";
						_max = masterList.size();
						_subMax = 0;
						
						for ( j = 0; j < _max; j++) {
							
							_totalItem = (HashMap<String, Object>) masterList.get(i);
							_type = _totalItem.get("type").toString();
							ArrayList<Element> pageItems = (ArrayList<Element>) _totalItem.get("pageItems");
							
							// 아이템별 dataSet와 Column값을 담기
							_dataType = "";
							_dataSetName = "";
							_dataSetColumn = "";
							dataColumnDatas = new HashMap<String,ArrayList<String>>();
							
							_subMax = pageItems.size();
							for ( k = 0; k < _subMax; k++) {
								
								try {
									dataColumnDatas = UBIDataUtilPraser.getItemDataProperty( pageItems.get(k), dataColumnDatas);
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
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
					case 3: //Continue Band 처리
						// ContinueBand 처리
						ContinueBandParser continueBandParser = new ContinueBandParser(m_appParams);
						
						continueBandParser.setExportData(true);
						
						continueBandParser.setFunction(mFunction);
						continueBandParser.DataSet 					= (HashMap<String, ArrayList<HashMap<String, Object>>>) pagesData.get( pagesInfoData.get(i).get("id").toString() );
						ArrayList<Object> bandAr 					= (ArrayList<Object>)(ArrayList<Object>) InfoDataMap.get(j);
//						ArrayList<Object> bandAr = continueBandParser.loadTotalPage(_page,  pagesData.get( pagesInfoData.get(i).get("id").toString() ),0, pageHeight, pageWidth);
						
						HashMap<String, BandInfoMapData> bandInfo 	= (HashMap<String, BandInfoMapData>) bandAr.get(0);
						ArrayList<BandInfoMapData> bandList 		= (ArrayList<BandInfoMapData>) bandAr.get(1);
						ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList 	= (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
						HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
						
						// group함수를 위한 객체 function처리하기 위하여 페이지 이동 함수로 전달하여 진행
						HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
						ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
						
						// crossTabData가 null이 아닐경우 
						if( crossTabData != null )
						{
							// crossTabData.columnList
							// crossTabData.rowList
							
						}
						
						dataColumnDatas = new HashMap<String,ArrayList<String>>();
						_items = _page.getElementsByTagName("item");
						_item = null;
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
//									for ( m = 0; m < dataColumnDatas.get(_dataSetName).size(); m++) {
									int _subMax2 = dataColumnDatas.get(_dataSetName).size();
									for ( n = 0; n < _subMax2; n++) {
										if( realDataCoumns.get(dataNameAr.get(l)).contains( dataColumnDatas.get(_dataSetName).get(n) ) == false )
										{
											realDataCoumns.get(dataNameAr.get(l)).add( dataColumnDatas.get(_dataSetName).get(n) );
										}
									}
//									}
									
								}
							}
							
						}
						
						
						break;
					default:
						break;
				}
				
			}
			
			// 결과로 만들어진 데이터를 이용하여 데이터 파싱하여 리턴
			
			ArrayList<Object> _headerAr = null;
//			ArrayList<HashMap<String, String>> _dataAr = null;
//			HashMap<String, String> _rowData = null;
			ArrayList<Object> _dataAr = null;
			ArrayList<String> _rowData = null;
			int _dataMaxCnt = 0;
			int _dataMaxRowCnt = 0;
			String _colName = "";
			
			HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) pagesData.get( pagesInfoData.get(i).get("id").toString() );
			
			HashMap<String, ArrayList<Object>> _resultDataMap;
			
			for (String _str : realDataCoumns.keySet()) {
				
				_resultDataMap = new HashMap<String, ArrayList<Object>>();
				
				_dataMaxCnt = realDataCoumns.get(_str).size();
				_headerAr = new ArrayList<Object>();
				_dataAr = new ArrayList<Object>();
				
				if(_dataSet.containsKey(_str) == false ) continue;
				
				_dataMaxRowCnt = _dataSet.get(_str).size();
				for ( j = 0; j < _dataMaxRowCnt; j++) {
					
					_rowData = new ArrayList<String>();
					
					for ( k = 0; k < _dataMaxCnt; k++) {
						_colName = realDataCoumns.get(_str).get(k);
						
						if( _dataSet.get(_str).get(j).containsKey(_colName) == false ) continue;
						
						if(j == 0 )
						{
							_headerAr.add(_colName);
						}
					
						_rowData.add( String.valueOf(_dataSet.get(_str).get(j).get(_colName))); 
//						_rowData.add( _dataSet.get(_str).get(j).get(_colName).toString()); 
						
					}
					_dataAr.add(_rowData);
				}
				
				_resultDataMap.put("header",  _headerAr);
				_resultDataMap.put("data",  _dataAr);

				resultDataSet.put("ConnectData_" + _pageCnt + "_"+ i + "_" + _str, _resultDataMap);
			}
			
			
			//ConnectPage 또는 LinkedPage 의 경우
			if( _crossTabDataSet != null && _crossTabDataSet.size() > 0  ){
				
				for (String _dName : _crossTabDataSet.keySet()) {
					resultDataSet.put("ConnectData_" + _pageCnt + "_"+ i + "_" + _dName , _crossTabDataSet.get(_dName) );
				}
					
			}
			
		}
		

		
		
		
		return resultDataSet;
	}
	
	
	
	
	
	
	
}
