package org.ubstorm.service.parser;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.parser.formparser.ConnectLinkParser;
import org.ubstorm.service.parser.formparser.ContinueBandParser;
import org.ubstorm.service.parser.formparser.ItemConvertParser;
import org.ubstorm.service.parser.formparser.MasterBandParser;
import org.ubstorm.service.parser.formparser.RepeatedFormParser;
import org.ubstorm.service.parser.formparser.UBIDataUtilPraser;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.TempletItemInfo;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.PageInfo;
import org.ubstorm.service.parser.formparser.info.PageInfoSimple;
import org.ubstorm.service.parser.formparser.info.ProjectInfo;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class TotalPageCheckParser {

	
	private UDMParamSet m_appParams;
	private PageNumProcess mPageNumFn = null;
	private ArrayList<Element> mPageAr = null;
	private HashMap<String, ArrayList<HashMap<String, Object>>> mDataSet;
	private HashMap mParam;
	private Document mDocument;
	private Function mFunction;
	
	private float mPageMarginTop = 0;
	private float mPageMarginLeft = 0;
	private float mPageMarginRight = 0;
	private float mPageMarginBottom = 0;
	private String mIsExport = "";
	private HashMap<String, String> mPDFProtectionInfo;

	private HashMap<String, TempletItemInfo> mTempletInfo;
	protected String mExportFileName = "";
	protected String mExportMethodType = "";
	protected boolean mUseFileSplit = false;
	
	public TotalPageCheckParser(UDMParamSet appParams, float _marginTop,float _marginleft,float _marginRight,float _marginBottom ) {	
		super();
		m_appParams = appParams;
				
		mPageMarginTop = _marginTop;
		mPageMarginLeft = _marginleft;
		mPageMarginRight = _marginRight;
		mPageMarginBottom = _marginBottom;
	}
	
	public void setFunction( Function _fn)
	{
		mFunction = _fn;
	}
	
	public void setIsExport(String _value)
	{
		mIsExport = _value;
	}
	
	public void setTempletInfo( HashMap<String, TempletItemInfo> _templetInfo )
	{
		mTempletInfo = _templetInfo;
	}
	public HashMap<String, TempletItemInfo> getTempletInfo()
	{
		return mTempletInfo;
	}
		
	public HashMap<String, String> getPdfProtectionInfo()
	{
		return mPDFProtectionInfo;
	}
	
	public HashMap<String, Object> getPageGroupInfo( Element _projectXml )
	{
		HashMap<String, Object> _resultMap = null;
		if( _projectXml.getElementsByTagName("pageGroup").getLength() > 0 )
		{
			Element _pageGroup =(Element) _projectXml.getElementsByTagName("pageGroup").item(0);
			NodeList _pageGroupPropList = _pageGroup.getElementsByTagName("property");
			Element _pageGroupProp = null;
			int i=0;
			int max = _pageGroupPropList.getLength();
			_resultMap = new HashMap<String, Object>();
			String _value = "";
			String _name = "";
			Object _valueObj = null;
			JSONParser _jparser = new JSONParser();
			ArrayList<HashMap<String, Object>> _resultPageGroup = new ArrayList<HashMap<String, Object>>();
			for( i =0 ; i < max; i++)
			{
				_pageGroupProp = (Element) _pageGroupPropList.item(i);
				
				_name = _pageGroupProp.getAttribute("name");
				_value = _pageGroupProp.getAttribute("value");
				
				try {
					_value = URLDecoder.decode(_value, "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if( _name.equals("usePageGroup") || _name.equals("useFileSplit") )
				{
					_valueObj = _value.equals("true");
				}
				else if( _name.equals("pageGroupData") )
				{
					JSONArray _dataAr;
					try {
						_dataAr = (JSONArray) _jparser.parse( _value );
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
						
						_valueObj = _resultPageGroup;
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						_valueObj = _value;
					}
					
				}
				else
				{
					_valueObj = _value;
				}
 
				_resultMap.put( _name, _valueObj );
			}
			
		}
		
		
		return _resultMap;
	}

	public HashMap<String, Object> getTotalPage( HashMap _param, Document _document, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, Function _function ) throws Exception
	{
		// xml page;
		
		mPageNumFn = new PageNumProcess();
		mDocument = _document;
		mParam = _param;
		mDataSet = _dataSet;
		mFunction = _function;
		
		mFunction.setParam(mParam);						// parameter 값을 function에 Set 2016-11-01 최명진
		
		mPageAr = new ArrayList<Element>();
		
		NodeList _pageList = mDocument.getElementsByTagName("page");
		
		Object loadType = _param.get("LOAD_TYPE");
		
		ArrayList<Element> _pages = null;
		boolean _chkFlag = true;
		HashMap<String, Object> pageMapData = null;
		int totalPage = 0;
		ArrayList<Integer> mPageNumRealList = new ArrayList<Integer>();
		ArrayList<Integer> mPageNumList = new ArrayList<Integer>();
		ArrayList<Object> pageInfoData = new ArrayList<Object>();
		ArrayList<Object> pageInfoClassData = new ArrayList<Object>();
		
		HashMap<String, Object> returnMap = new HashMap<String, Object>();
		
		ArrayList<Integer> mXArray = new ArrayList<Integer>();
		ArrayList<Object> tempDataSets = new ArrayList<Object>();		// 변경된 데이터셋이 존재할경우 데이터셋을 담아두는 배열
		
		ArrayList<Object> pageReqItemData = new ArrayList<Object>();

		ArrayList<Object> pageTabIndexItemData = new ArrayList<Object>();
		
		boolean isGrouping = false;
		
		if(mPDFProtectionInfo == null || mPDFProtectionInfo.isEmpty() )
		{
			setPdfProtectionInfo( (Element) mDocument.getElementsByTagName("project").item(0), _dataSet );
		}
		
		// project에 pageGroup가 있고 pageGroup에 지정한 데이터셋이 있을경우 해당 데이터로 group처리하여 그룹갯수만큼 pageList를 반복처리한다. 
		// 전체 그룹데이터를 그룹핑 처리하기 위한 그룹컬럼들을 담기
		
		
		boolean _isPageGruop = false;
		boolean _useDownloadFileName = false;
		boolean _useFileSplit = false;
		
		ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _totalGroupPageData = null;
		ArrayList<String> downLoadFileNameAr = new ArrayList<String>();
		HashMap<String, ArrayList<HashMap<String, Object>>> _currentGrpData = mDataSet;		//그룹별 전체 데이터를 담아둘 객체 
		int _grpCnt = 1;			// 총 그룹의 갯수
		int _grpIdx = 0;			
		
		HashMap<String, Object> _pageGroupInfo = getPageGroupInfo(  (Element) mDocument.getElementsByTagName("project").item(0) );
		
		if(_pageGroupInfo!=null)
		{
			if( _pageGroupInfo.containsKey("usePageGroup") ) _isPageGruop = _pageGroupInfo.get("usePageGroup").toString().equals("true");
			if( _pageGroupInfo.containsKey("useFileSplit") && mUseFileSplit ) _useFileSplit = _pageGroupInfo.get("useFileSplit").toString().equals("true");
			if( _pageGroupInfo.containsKey("downLoadFileName") && _pageGroupInfo.get("downLoadFileName").toString().trim().equals("") == false && mUseFileSplit ) _useDownloadFileName = true;
			
			if( _pageGroupInfo != null && _pageGroupInfo.containsKey("usePageGroup") && _pageGroupInfo.get("usePageGroup").toString().equals("true")  )
			{
				ArrayList<HashMap<String, Object>> _pageGroupData = (ArrayList<HashMap<String, Object>>) _pageGroupInfo.get("pageGroupData");
				if( _pageGroupData != null && _pageGroupData.size() > 0 )
				{
					_totalGroupPageData = getGroupingDataSet( mDataSet, _pageGroupData ); 
					_grpCnt = _totalGroupPageData.size();
				}
			}
			
			if( _useFileSplit && _grpCnt < 2 ) _useFileSplit = false;
			
			if( _useDownloadFileName && (mExportMethodType.equals("exportServerSide")||mExportMethodType.equals("PRINT")) ) _useDownloadFileName = false;
		}
		
		// 총 그룹의 수만큼 반복하여 페이지전체를 반복시켜서 처리
		for( _grpIdx = 0; _grpIdx < _grpCnt; _grpIdx++ )
		{
			String _downFileName = "";
			
			if( _totalGroupPageData != null ) _currentGrpData = _totalGroupPageData.get(_grpIdx);
			else _currentGrpData = mDataSet;
			
			if( _grpIdx > 0 && !_useFileSplit ) _useDownloadFileName = false;
			
			if(_useDownloadFileName)
			{
				_downFileName = UBIDataUtilPraser.getUbfunction( _pageGroupInfo.get("downLoadFileName").toString(), _currentGrpData, mParam,mFunction );
				
				if( _grpCnt == 1 )
				{
					_downFileName = mExportFileName + "_" + _downFileName;
				}
			}
			
			// 속성중에 downLoadFileName속성이 있을경우 이름을따로 담는다 ( useFileSplit 이 true일경우 매 그룹마다 담고 아닐경우 빈값으로 담는다 )
			
			for(int i = 0 ; i < _pageList.getLength() ; i ++)
			{
				Element _page = (Element) _pageList.item(i);
				isGrouping = false;
				int _reportType = Integer.parseInt(_page.getAttribute("reportType") );
				//pageVisible값을 체크하여 페이지 사용여부를 결정
				ArrayList<HashMap<String, Object>> _dataList = null;
				
				if( i > 0 ) _downFileName = "";
				
				if( _page.getAttribute("isGroup") != null && _page.getAttribute("isGroup").equals("true") )
				{
					_dataList = checkGroupingDataInfo(_page);
					if( _dataList != null && _dataList.size() > 0 )
					{
						isGrouping = true;
					}
				}
				
				if( isGrouping )
				{
					// page의 타입이 continueBand이고 그룹핑 처리가 되잇을경우 데이터셋을 그룹핑 처리하고 총 그룹수만큼 반복하여 페이지수 구하기 
					
					ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _groupingData = getGroupingDataSet( _currentGrpData, _dataList);
					
					// grouping된 데이터가 없을경우 기본 데이터셋을 넘겨서 이후 진행 하도록 처리
					if( _groupingData.size() == 0 )
					{
						_groupingData.add(_currentGrpData);
					}
					
					int _cnt = _groupingData.size();
					int _realTotPageCnt = 0;
					HashMap<String, Object> _defBandInfoData  = null;
					// page가 continueBand일경우 밴드정보를 뽑아두고 재활용 할수 있도록 처리한다. 
					if( _reportType == 3)
					{
						ContinueBandParser _continueBandParser = new ContinueBandParser();
						float pageWidth = Float.valueOf(_page.getAttribute("width"));
						float pageHeight = Float.valueOf(_page.getAttribute("height"));
						_continueBandParser.setFunction( this.mFunction );
						_defBandInfoData = _continueBandParser.makeDefaultBandList(_page, _groupingData.get(0), 0, pageHeight, pageWidth, mXArray, _pageList, _param);
					}
					int _curTotPageCnt = 0;
					
					for (int j = 0; j < _cnt; j++) {
						
						if( j > 0 ) _downFileName = "";
						_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck(_groupingData.get(j), _page, mParam,mFunction);
						
						if( _chkFlag ){
							mPageAr.add(_page);
							
							// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
							pageMapData = pageTypeToTotalNum( _reportType , _page, loadType, mXArray, _groupingData.get(j), _pageList, _defBandInfoData);	
							
//							totalPage = totalPage + ( (Integer)  pageMapData.get("TOTALPAGE") );
							_curTotPageCnt = _curTotPageCnt + ( (Integer)  pageMapData.get("TOTALPAGE") );
							
							mPageNumRealList.add(  (Integer) pageMapData.get("REAL_PAGE_NUM") );
							mPageNumList.add(  (Integer) pageMapData.get("PAGE_NUM") );
							
							_realTotPageCnt = _realTotPageCnt + (Integer) pageMapData.get("REAL_PAGE_NUM");
							
							pageInfoData.add( pageMapData.get("PAGE_INFO_DATA") );
							pageInfoClassData.add( pageMapData.get("PAGE_INFO_CLASS"));
							
							pageReqItemData.add( pageMapData.get("PAGE_INFO_REQ"));
							pageTabIndexItemData.add( pageMapData.get("PAGE_INFO_TIDX"));
							
							tempDataSets.add(_groupingData.get(j));
							
							downLoadFileNameAr.add(_downFileName);
						}
					}
					
					// Clone Page값 담기
					String cloneData = _page.getAttribute("clone");
					
					if( _curTotPageCnt > 0 && _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
					{
						
						int _cloneColCnt = 1;
						int _cloneRowCnt = 1;
						int _cloneRepCnt = 1;
						
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
							
							_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
						}
						
//						totalPage = Double.valueOf( Math.ceil( Float.valueOf(_realTotPageCnt)/Float.valueOf(_cloneRepCnt) ) ).intValue();
						_curTotPageCnt = Double.valueOf( Math.ceil( Float.valueOf(_realTotPageCnt)/Float.valueOf(_cloneRepCnt) ) ).intValue();
					}
					
					totalPage = totalPage + _curTotPageCnt;
				}
				else
				{
					if( ((Element)_page).hasAttribute("isConnect") && ((Element)_page).getAttribute("isConnect") != null  && ((Element)_page).getAttribute("isConnect").equals("true") )
					{
						continue;
					}
					else if( _reportType == 13 )
					{
						// Linked Page일경우 나머지 페이지의 페이지수를 계산할 필요 없음.
						continue;
					}
					
					//_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck(mDataSet, _page, mParam,mFunction);
					_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck(_currentGrpData, _page, mParam, mFunction);
					
					if( _chkFlag ){
						mPageAr.add(_page);
						
						// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
						pageMapData = pageTypeToTotalNum( _reportType , _page, loadType, mXArray, _currentGrpData, _pageList );
						
						totalPage = totalPage + ( (Integer)  pageMapData.get("TOTALPAGE") );
						mPageNumRealList.add(  (Integer) pageMapData.get("REAL_PAGE_NUM") );
						mPageNumList.add(  (Integer) pageMapData.get("PAGE_NUM") );
						
						pageInfoData.add( pageMapData.get("PAGE_INFO_DATA") );
						pageInfoClassData.add( pageMapData.get("PAGE_INFO_CLASS"));
						
						pageReqItemData.add( pageMapData.get("PAGE_INFO_REQ"));
						pageTabIndexItemData.add( pageMapData.get("PAGE_INFO_TIDX"));
						
						downLoadFileNameAr.add(_downFileName);
						
						tempDataSets.add( _currentGrpData );
					}
				}
			}
			
			
			// group 별 반복 end
		}
		
		returnMap.put("TOTALPAGE", totalPage);
		returnMap.put("REAL_PAGE_NUMLIST", mPageNumRealList);
		returnMap.put("PAGE_NUMLIST", mPageNumList);
		returnMap.put("PAGE_INFO_DATA_LIST", pageInfoData);
		returnMap.put("PAGE_INFO_CLASS_LIST", pageInfoClassData);
		returnMap.put("PAGE_AR", mPageAr);
		returnMap.put("EXCEL_X_ARR", mXArray);
		returnMap.put("TEMP_DATASET_LIST", tempDataSets);

		returnMap.put("PAGE_INFO_TIDX_LIST", pageTabIndexItemData);
		returnMap.put("PAGE_INFO_REQ_LIST", pageReqItemData);
		
		returnMap.put("DOWNLOAD_FILE_NAMES", downLoadFileNameAr);
		returnMap.put("USE_SPLIT_FILE", _useFileSplit);
		
//		m_appParams 		= null;
//		mPageNumFn 			= null;
//		mPageAr 			= null;
//		mDataSet 			= null;
//		mParam 				= null;
//		mDocument 			= null;
		
		return returnMap;
	}
	
	public HashMap<String, Object> getTotalPageMulti( ArrayList<JSONObject> _params, ArrayList<String> _documents,ArrayList< HashMap<String, ArrayList<HashMap<String, Object>>> > _dataSets, Function _function,  ArrayList<HashMap<String, String>> _docInfos ) throws Exception
	{
		// xml page;
		
		mPageNumFn = new PageNumProcess();
		
		mFunction = _function;
		
		mPageAr = new ArrayList<Element>();
		
		NodeList _pageList;
		
//		Object loadType = _param.get("LOAD_TYPE");
		Object loadType = "";
		
		ArrayList<Element> _pages = null;
		boolean _chkFlag = true;
		HashMap<String, Object> pageMapData = null;
		int totalPage = 0;
		
		int _docTotalPage = 0;
		ArrayList<Integer> mPageNumRealList = new ArrayList<Integer>();
		ArrayList<Integer> mPageNumList = new ArrayList<Integer>();
		ArrayList<Object> pageInfoData = new ArrayList<Object>();
		ArrayList<Object> pageInfoClassData = new ArrayList<Object>();
		
		HashMap<String, Object> returnMap = new HashMap<String, Object>();
		
		ArrayList<Integer> mXArray = new ArrayList<Integer>();
		ArrayList<Object> tempDataSets = new ArrayList<Object>();		// 변경된 데이터셋이 존재할경우 데이터셋을 담아두는 배열
		
		ArrayList<ArrayList<Integer>> mTotXArray = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> mDocTotalPages = new ArrayList<Integer>();			// 각 문서별 총 페이지를 담아두기
		ArrayList<Integer> mDocStartIdxs = new ArrayList<Integer>();			// 각 문서별 시작 페이지 담기
		
		ArrayList<Object> pageReqItemData = new ArrayList<Object>();

		ArrayList<Object> pageTabIndexItemData = new ArrayList<Object>();
		
		int _pageSize = _docInfos.size();
		
		Document _doc;
		InputSource _is = null;
		HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet;
		
		for (int _pjIdx = 0; _pjIdx < _pageSize; _pjIdx++) {
			
			mParam = _params.get(_pjIdx);
			mFunction.setParam(mParam);						// parameter 값을 function에 Set 2016-11-01 최명진

			boolean isGrouping = false;
			
			_is = new InputSource(new StringReader( _documents.get( Integer.parseInt( _docInfos.get(_pjIdx).get("IDX") )  ) ));
			_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
			
//			_doc = _documents.get( _pjIdx );
			_dataSet = _dataSets.get( _pjIdx );
			NodeList _pjList = _doc.getElementsByTagName("project");
			Element _pj = (Element) _pjList.item(0);
			
			String _pjName = "";
			String _fmName = "";
			
			if( _docInfos != null && _docInfos.size() > _pjIdx )
			{
				_pjName = _docInfos.get(_pjIdx).get("PROEJCT");			//프로젝트명
				_fmName = _docInfos.get(_pjIdx).get("FORMID");				//문서
			}
//			if( _pj.hasAttribute("PROEJCT"))
//			{
//				_pjName = _pj.getAttribute("PROEJCT");			//프로젝트명
//				_fmName = _pj.getAttribute("FORMID");				//문서
//			}
			
			//Doc리스트 중에서 첫번째 리스트를 담기 위해 
			if(mPDFProtectionInfo == null || mPDFProtectionInfo.isEmpty() )
			{
				setPdfProtectionInfo( (Element) _doc.getElementsByTagName("project").item(0), _dataSet );
			}
			
			_pageList = _doc.getElementsByTagName("page");
			
			mXArray = new ArrayList<Integer>();
			_docTotalPage = 0;
			mDocStartIdxs.add(totalPage );
			
			for(int i = 0 ; i < _pageList.getLength() ; i ++)
			{
				Element _page = (Element) _pageList.item(i);
				
				// 프로젝트정보를 각 페이지마다 Add시켜둔다.
				_page.setAttribute("PROJECT_NAME", _pjName);
				_page.setAttribute("FORM_NAME", _fmName);
				_page.setAttribute("FORM_IDX", String.valueOf( _pjIdx ) );
				
				isGrouping = false;
				int _reportType = Integer.parseInt(_page.getAttribute("reportType") );
				HashMap<String, ArrayList<HashMap<String, Object>>> tempDataSet = null;
				//pageVisible값을 체크하여 페이지 사용여부를 결정
				
					
				ArrayList<HashMap<String, Object>> _dataList = null;
				
				if( _page.getAttribute("isGroup") != null && _page.getAttribute("isGroup").equals("true") )
				{
					_dataList = checkGroupingDataInfo(_page);
					if( _dataList != null && _dataList.size() > 0 )
					{
						isGrouping = true;
					}
				}
				
				if( isGrouping )
				{
					// page의 타입이 continueBand이고 그룹핑 처리가 되잇을경우 데이터셋을 그룹핑 처리하고 총 그룹수만큼 반복하여 페이지수 구하기 
					
					ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _groupingData = getGroupingDataSet( _dataSet, _dataList);
					
					// grouping된 데이터가 없을경우 기본 데이터셋을 넘겨서 이후 진행 하도록 처리
					if( _groupingData.size() == 0 )
					{
						_groupingData.add(_dataSet);
					}
					
					int _cnt = _groupingData.size();
					int _realTotPageCnt = 0;
					int _curTotPageCnt = 0;
					
					for (int j = 0; j < _cnt; j++) {
						
						_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck(_groupingData.get(j), _page, mParam,mFunction);
						
						if( _chkFlag ){
							mPageAr.add(_page);
							
							// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
							pageMapData = pageTypeToTotalNum( _reportType , _page, loadType, mXArray, _groupingData.get(j), _pageList);
							
//							totalPage = totalPage + ( (Integer)  pageMapData.get("TOTALPAGE") );
							_curTotPageCnt = _curTotPageCnt + ( (Integer)  pageMapData.get("TOTALPAGE") );
							
							_docTotalPage = _docTotalPage +  ( (Integer)  pageMapData.get("TOTALPAGE") );
							
							mPageNumRealList.add(  (Integer) pageMapData.get("REAL_PAGE_NUM") );
							mPageNumList.add(  (Integer) pageMapData.get("PAGE_NUM") );
							
							_realTotPageCnt = _realTotPageCnt + (Integer) pageMapData.get("REAL_PAGE_NUM");
							
							pageInfoData.add( pageMapData.get("PAGE_INFO_DATA") );
							pageInfoClassData.add( pageMapData.get("PAGE_INFO_CLASS"));
							tempDataSets.add(_groupingData.get(j));
						}
					}
					
					// Clone Page값 담기
					String cloneData = _page.getAttribute("clone");
					
					if( _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
					{
						
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
							_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
						}
						
//						totalPage = Double.valueOf( Math.ceil( Float.valueOf(_realTotPageCnt)/Float.valueOf(_cloneRepCnt) ) ).intValue();
						_curTotPageCnt = Double.valueOf( Math.ceil( Float.valueOf(_realTotPageCnt)/Float.valueOf(_cloneRepCnt) ) ).intValue();
						
					}
					totalPage = totalPage + _curTotPageCnt;
				}
				else
				{
					if( ((Element)_page).hasAttribute("isConnect") && ((Element)_page).getAttribute("isConnect") != null  && ((Element)_page).getAttribute("isConnect").equals("true") )
					{
						continue;
					}
					
					_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck(_dataSet, _page, mParam,mFunction);
					
					if( _chkFlag ){
						mPageAr.add(_page);
						
						// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
						pageMapData = pageTypeToTotalNum( _reportType , _page, loadType, mXArray, _dataSet, _pageList);
						
						totalPage = totalPage + ( (Integer)  pageMapData.get("TOTALPAGE") );
						
						_docTotalPage = _docTotalPage +  ( (Integer)  pageMapData.get("TOTALPAGE") );
						
						mPageNumRealList.add(  (Integer) pageMapData.get("REAL_PAGE_NUM") );
						mPageNumList.add(  (Integer) pageMapData.get("PAGE_NUM") );
						
						pageInfoData.add( pageMapData.get("PAGE_INFO_DATA") );
						pageInfoClassData.add( pageMapData.get("PAGE_INFO_CLASS"));
						
						pageReqItemData.add( pageMapData.get("PAGE_INFO_REQ"));
						pageTabIndexItemData.add( pageMapData.get("PAGE_INFO_TIDX"));
						
						tempDataSets.add(_dataSet);
					}
				}
			}
			
			// 프로젝트 하나가 끝날경우 XArray를 담아둔다
			mTotXArray.add(mXArray);
			mDocTotalPages.add(_docTotalPage);
			
		}
		
		
		returnMap.put("TOTALPAGE", totalPage);
		returnMap.put("REAL_PAGE_NUMLIST", mPageNumRealList);
		returnMap.put("PAGE_NUMLIST", mPageNumList);
		returnMap.put("PAGE_INFO_DATA_LIST", pageInfoData);
		returnMap.put("PAGE_INFO_CLASS_LIST", pageInfoClassData);
		returnMap.put("PAGE_AR", mPageAr);
		returnMap.put("EXCEL_X_ARR", mXArray);
		returnMap.put("TEMP_DATASET_LIST", tempDataSets);

		returnMap.put("PAGE_INFO_TIDX_LIST", pageTabIndexItemData);
		returnMap.put("PAGE_INFO_REQ_LIST", pageReqItemData);
		returnMap.put("EXCEL_X_ARR_TOT", mTotXArray);
		returnMap.put("DOCUMENT_TOTAL_PAGE", mDocTotalPages);
		returnMap.put("DOCUMENT_START_IDX", mDocStartIdxs);
		
		return returnMap;
	}
	
	public HashMap<String, Object> getTotalPageMergeForm( ArrayList<JSONObject> _params, Document _document,ArrayList< HashMap<String, ArrayList<HashMap<String, Object>>> > _dataSets, Function _function,  ArrayList<HashMap<String, String>> _docInfos ) throws Exception
	{
		// xml page;
		
		mPageNumFn = new PageNumProcess();
		
		mFunction = _function;
		
		mPageAr = new ArrayList<Element>();
		
		NodeList _pageList;
		
//		Object loadType = _param.get("LOAD_TYPE");
		Object loadType = "";
		
		ArrayList<Element> _pages = null;
		boolean _chkFlag = true;
		HashMap<String, Object> pageMapData = null;
		int totalPage = 0;
		
		int _docTotalPage = 0;
		ArrayList<Integer> mPageNumRealList = new ArrayList<Integer>();
		ArrayList<Integer> mPageNumList = new ArrayList<Integer>();
		ArrayList<Object> pageInfoData = new ArrayList<Object>();
		ArrayList<Object> pageInfoClassData = new ArrayList<Object>();
		
		HashMap<String, Object> returnMap = new HashMap<String, Object>();
		
		ArrayList<Integer> mXArray = new ArrayList<Integer>();
		ArrayList<Object> tempDataSets = new ArrayList<Object>();		// 변경된 데이터셋이 존재할경우 데이터셋을 담아두는 배열
		
		ArrayList<ArrayList<Integer>> mTotXArray = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> mDocTotalPages = new ArrayList<Integer>();			// 각 문서별 총 페이지를 담아두기
		ArrayList<Integer> mDocStartIdxs = new ArrayList<Integer>();			// 각 문서별 시작 페이지 담기
		
		ArrayList<Object> pageReqItemData = new ArrayList<Object>();

		ArrayList<Object> pageTabIndexItemData = new ArrayList<Object>();
		
		int _pageSize = _docInfos.size();
		
		NodeList _projectList = _document.getElementsByTagName("project");
		Document _doc;
		InputSource _is = null;
		HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet;
		
		for (int _pjIdx = 0; _pjIdx < _pageSize; _pjIdx++) {
			
			mParam = _params.get(_pjIdx);
			mFunction.setParam(mParam);						// parameter 값을 function에 Set 2016-11-01 최명진

			boolean isGrouping = false;
			
			_dataSet = _dataSets.get( _pjIdx );
			Element _pj =(Element) _projectList.item(_pjIdx);
			
			String _pjName = "";
			String _fmName = "";
			
			if( _docInfos != null && _docInfos.size() > _pjIdx )
			{
				_pjName = _docInfos.get(_pjIdx).get("PROEJCT");			//프로젝트명
				_fmName = _docInfos.get(_pjIdx).get("FORMID");				//문서
			}
			
			//Doc리스트 중에서 첫번째 리스트를 담기 위해 
			if(mPDFProtectionInfo == null || mPDFProtectionInfo.isEmpty() )
			{
				setPdfProtectionInfo( (Element) _pj, _dataSet );
			}
			
			_pageList = _pj.getElementsByTagName("page");
			
			mXArray = new ArrayList<Integer>();
			_docTotalPage = 0;
			mDocStartIdxs.add(totalPage );
			
			for(int i = 0 ; i < _pageList.getLength() ; i ++)
			{
				Element _page = (Element) _pageList.item(i);
				
				// 프로젝트정보를 각 페이지마다 Add시켜둔다.
				_page.setAttribute("PROJECT_NAME", _pjName);
				_page.setAttribute("FORM_NAME", _fmName);
				_page.setAttribute("FORM_IDX", String.valueOf( _pjIdx ) );
				
				isGrouping = false;
				int _reportType = Integer.parseInt(_page.getAttribute("reportType") );
				HashMap<String, ArrayList<HashMap<String, Object>>> tempDataSet = null;
				//pageVisible값을 체크하여 페이지 사용여부를 결정
					
				ArrayList<HashMap<String, Object>> _dataList = null;
				
				if( _page.getAttribute("isGroup") != null && _page.getAttribute("isGroup").equals("true") )
				{
					_dataList = checkGroupingDataInfo(_page);
					if( _dataList != null && _dataList.size() > 0 )
					{
						isGrouping = true;
					}
				}
				
				if( isGrouping )
				{
					// page의 타입이 continueBand이고 그룹핑 처리가 되잇을경우 데이터셋을 그룹핑 처리하고 총 그룹수만큼 반복하여 페이지수 구하기 
					
					ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _groupingData = getGroupingDataSet( _dataSet, _dataList);
					
					// grouping된 데이터가 없을경우 기본 데이터셋을 넘겨서 이후 진행 하도록 처리
					if( _groupingData.size() == 0 )
					{
						_groupingData.add(_dataSet);
					}
					
					int _cnt = _groupingData.size();
					int _realTotPageCnt = 0;
					int _curTotPageCnt = 0;
					
					for (int j = 0; j < _cnt; j++) {
						
						_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck(_groupingData.get(j), _page, mParam,mFunction);
						
						if( _chkFlag ){
							mPageAr.add(_page);
							
							// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
							pageMapData = pageTypeToTotalNum( _reportType , _page, loadType, mXArray, _groupingData.get(j), _pageList);
							
//							totalPage = totalPage + ( (Integer)  pageMapData.get("TOTALPAGE") );
							_curTotPageCnt = _curTotPageCnt + ( (Integer)  pageMapData.get("TOTALPAGE") );
							
							_docTotalPage = _docTotalPage +  ( (Integer)  pageMapData.get("TOTALPAGE") );
							
							mPageNumRealList.add(  (Integer) pageMapData.get("REAL_PAGE_NUM") );
							mPageNumList.add(  (Integer) pageMapData.get("PAGE_NUM") );
							
							_realTotPageCnt = _realTotPageCnt + (Integer) pageMapData.get("REAL_PAGE_NUM");
							
							pageInfoData.add( pageMapData.get("PAGE_INFO_DATA") );
							pageInfoClassData.add( pageMapData.get("PAGE_INFO_CLASS"));
							tempDataSets.add(_groupingData.get(j));
						}
					}
					
					// Clone Page값 담기
					String cloneData = _page.getAttribute("clone");
					
					if( _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
//						if( true )
					{
						
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
							_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
						}
						
//						totalPage = Double.valueOf( Math.ceil( Float.valueOf(_realTotPageCnt)/Float.valueOf(_cloneRepCnt) ) ).intValue();
						_curTotPageCnt = Double.valueOf( Math.ceil( Float.valueOf(_realTotPageCnt)/Float.valueOf(_cloneRepCnt) ) ).intValue();
						
					}
					totalPage = totalPage + _curTotPageCnt;
				}
				else
				{
					if( ((Element)_page).hasAttribute("isConnect") && ((Element)_page).getAttribute("isConnect") != null  && ((Element)_page).getAttribute("isConnect").equals("true") )
					{
						continue;
					}
					
					_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck(_dataSet, _page, mParam,mFunction);
					
					if( _chkFlag ){
						mPageAr.add(_page);
						
						// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
						pageMapData = pageTypeToTotalNum( _reportType , _page, loadType, mXArray, _dataSet, _pageList);
						
						totalPage = totalPage + ( (Integer)  pageMapData.get("TOTALPAGE") );
						
						_docTotalPage = _docTotalPage +  ( (Integer)  pageMapData.get("TOTALPAGE") );
						
						mPageNumRealList.add(  (Integer) pageMapData.get("REAL_PAGE_NUM") );
						mPageNumList.add(  (Integer) pageMapData.get("PAGE_NUM") );
						
						pageInfoData.add( pageMapData.get("PAGE_INFO_DATA") );
						pageInfoClassData.add( pageMapData.get("PAGE_INFO_CLASS"));
						
						pageReqItemData.add( pageMapData.get("PAGE_INFO_REQ"));
						pageTabIndexItemData.add( pageMapData.get("PAGE_INFO_TIDX"));
						
						tempDataSets.add(_dataSet);
					}
				}

			}
			
			// 프로젝트 하나가 끝날경우 XArray를 담아둔다
			mTotXArray.add(mXArray);
			mDocTotalPages.add(_docTotalPage);
		}
		
		
		returnMap.put("TOTALPAGE", totalPage);
		returnMap.put("REAL_PAGE_NUMLIST", mPageNumRealList);
		returnMap.put("PAGE_NUMLIST", mPageNumList);
		returnMap.put("PAGE_INFO_DATA_LIST", pageInfoData);
		returnMap.put("PAGE_INFO_CLASS_LIST", pageInfoClassData);
		returnMap.put("PAGE_AR", mPageAr);
		returnMap.put("EXCEL_X_ARR", mXArray);
		returnMap.put("TEMP_DATASET_LIST", tempDataSets);

		returnMap.put("PAGE_INFO_TIDX_LIST", pageTabIndexItemData);
		returnMap.put("PAGE_INFO_REQ_LIST", pageReqItemData);
		returnMap.put("EXCEL_X_ARR_TOT", mTotXArray);
		//returnMap.put("DOCUMENT_TOTAL_PAGE", mDocTotalPages);
		//returnMap.put("DOCUMENT_START_IDX", mDocStartIdxs);
		
		return returnMap;
	}
	
	public HashMap<String, Object> getTotalPageSimple( ArrayList<JSONObject> _params, ArrayList<ProjectInfo> _pageInfos, Function _function ) throws Exception
	{
		mPageNumFn = new PageNumProcess();
		mFunction = _function;
		
		ArrayList<PageInfoSimple> mPageAr = new ArrayList<PageInfoSimple>();
		ArrayList<PageInfoSimple> _pageList;
		
		Object loadType = "";
		
		boolean _chkFlag = true;
		HashMap<String, Object> pageMapData = null;
		int totalPage = 0;
		
		int _docTotalPage = 0;
		ArrayList<Integer> mPageNumRealList = new ArrayList<Integer>();
		ArrayList<Integer> mPageNumList = new ArrayList<Integer>();
		ArrayList<Object> pageInfoData = new ArrayList<Object>();
		ArrayList<Object> pageInfoClassData = new ArrayList<Object>();
		
		HashMap<String, Object> returnMap = new HashMap<String, Object>();
		
		ArrayList<Integer> mXArray = new ArrayList<Integer>();
		ArrayList<Object> tempDataSets = new ArrayList<Object>();		// 변경된 데이터셋이 존재할경우 데이터셋을 담아두는 배열
		
		ArrayList<ArrayList<Integer>> mTotXArray = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> mDocTotalPages = new ArrayList<Integer>();			// 각 문서별 총 페이지를 담아두기
		ArrayList<Integer> mDocStartIdxs = new ArrayList<Integer>();			// 각 문서별 시작 페이지 담기
		
		ArrayList<Object> pageReqItemData = new ArrayList<Object>();

		ArrayList<Object> pageTabIndexItemData = new ArrayList<Object>();
		
		int _pageSize = _pageInfos.size();
		
		Document _doc;
		InputSource _is = null;
		HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet;
		ProjectInfo _projectInfo;
		PageInfoSimple _pageInfo;
		ArrayList<String> downLoadFileNameAr = new ArrayList<String>();
		// 페이지 그룹이 존재할경우 처리 
		boolean _isPageGruop = false;
		boolean _useDownloadFileName = false;
		boolean _useFileSplit = false;
		
		for (int _pjIdx = 0; _pjIdx < _pageSize; _pjIdx++) {
			
			mParam = _params.get(_pjIdx);
			mFunction.setParam(mParam);						// parameter 값을 function에 Set 2016-11-01 최명진

			boolean isGrouping = false;
			_projectInfo = _pageInfos.get(_pjIdx);
			_projectInfo.setFunction(mFunction);
			_projectInfo.setIsExportType( mIsExport );
			_dataSet = _projectInfo.getDataSet();
			
			//Doc리스트 중에서 첫번째 리스트를 담기 위해 
			if(mPDFProtectionInfo == null || mPDFProtectionInfo.isEmpty() )
			{
				setPdfProtectionInfo(  _projectInfo, _dataSet );
			}
			
			// 페이지 그룹이 존재할경우 처리 
			_isPageGruop = false;
			_useDownloadFileName = false;
			_useFileSplit = false;
			int _grpCnt = 1;			// 총 그룹의 갯수
			int _grpIdx = 0;			
			ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _totalGroupPageData = null;
			HashMap<String, ArrayList<HashMap<String, Object>>> _currentGrpData = _dataSet;		//그룹별 전체 데이터를 담아둘 객체 
			HashMap<String, Object> _pageGroupInfo = null;
			
			if(_projectInfo.getPageGroup() != null)
			{
				_pageGroupInfo = _projectInfo.getPageGroup();
				if( _pageGroupInfo.containsKey("usePageGroup") ) _isPageGruop = _pageGroupInfo.get("usePageGroup").toString().equals("true");
				if( _pageGroupInfo.containsKey("useFileSplit") && mUseFileSplit ) _useFileSplit = _pageGroupInfo.get("useFileSplit").toString().equals("true");
				if( _pageGroupInfo.containsKey("downLoadFileName") && _pageGroupInfo.get("downLoadFileName").toString().trim().equals("") == false  && mUseFileSplit ) _useDownloadFileName = true;
				
				if( _pageGroupInfo != null && _pageGroupInfo.containsKey("usePageGroup") && _pageGroupInfo.get("usePageGroup").toString().equals("true")  )
				{
					ArrayList<HashMap<String, Object>> _pageGroupData = (ArrayList<HashMap<String, Object>>) _pageGroupInfo.get("pageGroupData");
					if( _pageGroupData != null && _pageGroupData.size() > 0 )
					{
						_totalGroupPageData = getGroupingDataSet( _dataSet, _pageGroupData ); 
						_grpCnt = _totalGroupPageData.size();
					}
				}
				
				if( _useFileSplit && _grpCnt < 2 ) _useFileSplit = false;
				
				if( _useDownloadFileName && (mExportMethodType.equals("exportServerSide")||mExportMethodType.equals("PRINT")) ) _useDownloadFileName = false;
			}
			// 페이지 그룹이 존재할경우 처리 set Ed
			
			_pageList = _projectInfo.getPages();
			
			mXArray = new ArrayList<Integer>();
			
			_docTotalPage = 0;
			mDocStartIdxs.add(totalPage );
			// 총 그룹의 수만큼 반복하여 페이지전체를 반복시켜서 처리
			for( _grpIdx = 0; _grpIdx < _grpCnt; _grpIdx++ )
			{
				String _downFileName = "";
				
				if( _totalGroupPageData != null ) _currentGrpData = _totalGroupPageData.get(_grpIdx);
				else _currentGrpData = _dataSet;
				
				if(_pageGroupInfo!=null)
				{
					if( _grpIdx > 0 && !_useFileSplit ) _useDownloadFileName = false;
					
					if(_useDownloadFileName)
					{
						_downFileName = UBIDataUtilPraser.getUbfunction( _pageGroupInfo.get("downLoadFileName").toString(), _currentGrpData, mParam,mFunction );
						
						if( _grpCnt == 1 )
						{
							_downFileName = mExportFileName + "_" + _downFileName;
						}
					}
				}
				
				// 속성중에 downLoadFileName속성이 있을경우 이름을따로 담는다 ( useFileSplit 이 true일경우 매 그룹마다 담고 아닐경우 빈값으로 담는다 )
				
				int _pageListSize = _pageList.size();
				for(int i = 0 ; i < _pageListSize ; i ++)
				{
					_pageInfo = _pageList.get(i);
					
					// 프로젝트정보를 각 페이지마다 Add시켜둔다.
					isGrouping = false;
					int _reportType = Integer.parseInt( _pageInfo.getReportType()  );
					
					if( i > 0 ) _downFileName = "";
					
					mXArray = _pageInfo.getXArr();
					
					HashMap<String, ArrayList<HashMap<String, Object>>> tempDataSet = null;
					//pageVisible값을 체크하여 페이지 사용여부를 결정
						
					ArrayList<HashMap<String, Object>> _dataList = null;
					
					if( _pageInfo.getIsGroup() != null && _pageInfo.getIsGroup().equals("true") )
					{
						_dataList = checkGroupingDataInfo(_pageInfo);
						if( _dataList != null && _dataList.size() > 0 )
						{
							isGrouping = true;
						}
					}
					PageInfoSimple _clonePage;
					
					if( isGrouping )
					{
						// page의 타입이 continueBand이고 그룹핑 처리가 되잇을경우 데이터셋을 그룹핑 처리하고 총 그룹수만큼 반복하여 페이지수 구하기 
						
						ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _groupingData = getGroupingDataSet( _currentGrpData, _dataList);
						
						// grouping된 데이터가 없을경우 기본 데이터셋을 넘겨서 이후 진행 하도록 처리
						if( _groupingData.size() == 0 )
						{
							_groupingData.add(_currentGrpData);
						}
						
						int _cnt = _groupingData.size();
						int _realTotPageCnt = 0;
						int _curTotPageCnt = 0;
						
						HashMap<String, Object> _defBandInfoData  = null;
						// page가 continueBand일경우 밴드정보를 뽑아두고 재활용 할수 있도록 처리한다. 
						if( _reportType == 3)
						{
							_pageInfo.makeDefaultBand();
						}
						
						ArrayList<PageInfoSimple> _clonePageList = new ArrayList<PageInfoSimple>();
						for (int j = 0; j < _cnt; j++) {
							
							_clonePage = _pageInfo.clone();
							_clonePage.setDataSet( _groupingData.get(j) );
							
							if( j > 0 ) _downFileName = "";
							
							_chkFlag = UBIDataUtilPraser.getPageInfoVisibleChkeck( _groupingData.get(j), _clonePage, mParam,mFunction);
							
							if( _chkFlag ){
								mPageAr.add(_clonePage);
								_clonePage.createItem();
								
								_clonePage.setXArr(mXArray);
								mXArray = _clonePage.getXArr();
								
								_clonePageList.add(_clonePage);
								
								// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
								pageMapData = pageTypeToTotalNumSimple( _reportType , _clonePage, loadType, mXArray, _pageList);
								
//								totalPage = totalPage + ( (Integer)  pageMapData.get("TOTALPAGE") );
								_curTotPageCnt = _curTotPageCnt + ( (Integer)  pageMapData.get("TOTALPAGE") );
								
								_docTotalPage = _docTotalPage +  ( (Integer)  pageMapData.get("TOTALPAGE") );
								
								mPageNumRealList.add(  (Integer) pageMapData.get("REAL_PAGE_NUM") );
								mPageNumList.add(  (Integer) pageMapData.get("PAGE_NUM") );
								
								_realTotPageCnt = _realTotPageCnt + (Integer) pageMapData.get("REAL_PAGE_NUM");
								
								pageInfoData.add( pageMapData.get("PAGE_INFO_DATA") );
								pageInfoClassData.add( pageMapData.get("PAGE_INFO_CLASS"));
								tempDataSets.add(_groupingData.get(j));
								
								downLoadFileNameAr.add(_downFileName);
								
							}
							
						}
						
						// Clone Page값 담기
						String cloneData = _pageInfo.getClone();
						
						if( _pageInfo.getUseGroupPageClone() != null && _pageInfo.getUseGroupPageClone().equals("true") )
						{
							
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
									_cloneColCnt = Float.valueOf(_pageInfo.getCloneColCount()).intValue();
									_cloneRowCnt = Float.valueOf(_pageInfo.getCloneRowCount()).intValue();
								}
								
								if(_pageInfo.getDirection() != null && _pageInfo.getDirection().equals("")== false )
								{
									_cloneDirect = _pageInfo.getDirection();
								}
								_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
							}
							
							//totalPage = Double.valueOf( Math.ceil( Float.valueOf(_realTotPageCnt)/Float.valueOf(_cloneRepCnt) ) ).intValue();
							_curTotPageCnt = Double.valueOf( Math.ceil( Float.valueOf(_realTotPageCnt)/Float.valueOf(_cloneRepCnt) ) ).intValue();
							
						}
						totalPage = totalPage + _curTotPageCnt;
						
						_projectInfo.setPages( _clonePageList );
					}
					else
					{
						if( _pageInfo.getIsConnect()!=null && _pageInfo.getIsConnect().equals("true") )
						{
							continue;
						}

						_chkFlag = UBIDataUtilPraser.getPageInfoVisibleChkeck(_dataSet, _pageInfo, mParam,mFunction);
						
						if( _chkFlag ){
							_clonePage = _pageInfo.clone();
							_clonePage.setDataSet( _currentGrpData );

							mPageAr.add(_clonePage);
							
							_clonePage.setXArr(mXArray);
							_clonePage.createItem();
							mXArray = _clonePage.getXArr();
							
							// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
							pageMapData = pageTypeToTotalNumSimple( _reportType , _clonePage, loadType, mXArray, _pageList);
							
							totalPage = totalPage + ( (Integer)  pageMapData.get("TOTALPAGE") );
							
							_docTotalPage = _docTotalPage +  ( (Integer)  pageMapData.get("TOTALPAGE") );
							
							mPageNumRealList.add(  (Integer) pageMapData.get("REAL_PAGE_NUM") );
							mPageNumList.add(  (Integer) pageMapData.get("PAGE_NUM") );
							
							pageInfoData.add( pageMapData.get("PAGE_INFO_DATA") );
							pageInfoClassData.add( pageMapData.get("PAGE_INFO_CLASS"));
							
							pageReqItemData.add( pageMapData.get("PAGE_INFO_REQ"));
							pageTabIndexItemData.add( pageMapData.get("PAGE_INFO_TIDX"));
							
							tempDataSets.add(_currentGrpData);
							downLoadFileNameAr.add(_downFileName);
						}
							
					}
				}
				
				// 프로젝트 하나가 끝날경우 XArray를 담아둔다
				mTotXArray.add(mXArray);
				mDocTotalPages.add(_docTotalPage);
				
				
			}
			
		}
		
		returnMap.put("TOTALPAGE", totalPage);
		returnMap.put("REAL_PAGE_NUMLIST", mPageNumRealList);
		returnMap.put("PAGE_NUMLIST", mPageNumList);
		returnMap.put("PAGE_INFO_DATA_LIST", pageInfoData);
		returnMap.put("PAGE_INFO_CLASS_LIST", pageInfoClassData);
		returnMap.put("PAGE_AR", mPageAr);
		returnMap.put("EXCEL_X_ARR", mXArray);
		returnMap.put("TEMP_DATASET_LIST", tempDataSets);

		returnMap.put("PAGE_INFO_TIDX_LIST", pageTabIndexItemData);
		returnMap.put("PAGE_INFO_REQ_LIST", pageReqItemData);
		returnMap.put("EXCEL_X_ARR_TOT", mTotXArray);
//		returnMap.put("DOCUMENT_TOTAL_PAGE", mDocTotalPages);
//		returnMap.put("DOCUMENT_START_IDX", mDocStartIdxs);
		
		returnMap.put("DOWNLOAD_FILE_NAMES", downLoadFileNameAr);
		returnMap.put("USE_SPLIT_FILE", _useFileSplit);
		
		return returnMap;
	}
	
	
	public HashMap<String, Object> pageTypeToTotalNum(int _pageType , Element _page, Object _lodeType, ArrayList<Integer> mXArr, HashMap<String, ArrayList<HashMap<String, Object>>> _subDataSet, NodeList _allPageList ) throws Exception
	{
		return pageTypeToTotalNum( _pageType , _page, _lodeType, mXArr,_subDataSet, _allPageList, null);
	}
	
	public HashMap<String, Object> pageTypeToTotalNum(int _pageType , Element _page, Object _lodeType, ArrayList<Integer> mXArr, HashMap<String, ArrayList<HashMap<String, Object>>> _subDataSet, NodeList _allPageList, HashMap<String, Object> _defBandInfoData) throws Exception
	{
		
		int _typePageNum = 0;
		int _typeRealPageNum = 0;
		float pageWidth = Float.valueOf(_page.getAttribute("width"));
		float pageHeight = Float.valueOf(_page.getAttribute("height"));
		String cloneData = "";
		boolean clonePage = false;
		
		int mTOTAL_PAGE_NUM = 0;
		
		ArrayList<Object> pageDataList = new ArrayList<Object>();
		
		HashMap<String, Object> returnMap = new HashMap<String, Object>();
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _dataset = null;
		
		// Export Type를 
		String isExportType = m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE();
		if(isExportType.equals("LOCAL_PRINT"))
			isExportType = "PRINT";
		
		String isExcelOption = m_appParams.getREQ_INFO().getEXCEL_OPTION();
		
		if( _subDataSet == null ) _dataset = mDataSet;
		else  _dataset = _subDataSet;
		
		// Clone Page값 담기
		cloneData = _page.getAttribute("clone");
		clonePage = false;

		ArrayList<HashMap<String, Object>> _requiredItemList = null;
		ArrayList<HashMap<String, Object>> _tabIndexItemList = null;

		int _cloneColCnt = 1;
		int _cloneRowCnt = 1;
		int _cloneRepCnt = 1;
		String _cloneDirect = "";	// cloneDirect Across Down: 가로, Down Across : 세로
		
		// pageMargin이 있을경우 TotalPage를 구하는 부분에서는 
		// pageHeight와 pageWidth를 변경한다. 
		
		pageWidth = pageWidth - mPageMarginLeft - mPageMarginRight;
		pageHeight = pageHeight - mPageMarginTop - mPageMarginBottom;
		
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
		  
		int _minimumResizeFontSize = 0;
		if( _page.hasAttribute("minimumResizeFontSize") && StringUtil.isInteger(_page.getAttribute("minimumResizeFontSize")) )
		{
			_minimumResizeFontSize = Integer.valueOf(_page.getAttribute("minimumResizeFontSize"));
		}
		
		// Clone 속성이 존재할경우 페이지의 사이즈를 변경하여 총 그려지는 화면을 생성
		//1. cloneHorizontal, cloneVertical 값을 이용해서 실제 한화면의 사이즈를 구하기
		
		//2. 총 화면수 구하기
		
		//3. 실제 페이지의 수 구하기
		
		//4. 페이지 생성시 cloneX, cloneY값을 변경해가며 화면의 아이템 생성
		
		//5. 내보내기 및 connectPage 도 clone 처리
		
		switch (_pageType) {
		case 0: //coverPage
			_typePageNum = 1;
			
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
			
		case 1: //freeform
		case 7: //mobile타입
		case 9: //webPage
			_typePageNum = mPageNumFn.getFreeFormTotalNum( _page , _dataset);
			_typeRealPageNum = _typePageNum;
			if(clonePage)
			{
				_typePageNum = (int) Math.ceil((float) _typePageNum/_cloneRepCnt);
			}
			//NodeList _requiredItemList = _page.getElementsByTagName("requiredItem");
			_requiredItemList = getRequiredItemDataInfo(_page);
			_tabIndexItemList = getTabIndexItemDataInfo(_page);
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			returnMap.put("PAGE_INFO_REQ", _requiredItemList);
			returnMap.put("PAGE_INFO_TIDX", _tabIndexItemList);
			break;
			
		case 2: //masterBand
			// 로드 타입이 one일경우에만 masterBand의 총 페이지수를 구하기
			MasterBandParser masterParser = new MasterBandParser(this.m_appParams);
			masterParser.setFunction(mFunction);
			masterParser.setMinimumResizeFontSize(_minimumResizeFontSize);
			
			masterParser.setPageMarginTop(mPageMarginTop);
			masterParser.setPageMarginLeft(mPageMarginLeft);
			masterParser.setPageMarginRight(mPageMarginRight);
			masterParser.setPageMarginBottom(mPageMarginBottom);
			masterParser.setIsExportType(isExportType);

			ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
			ArrayList<Object> masterInfo = null;
			int _masterTotPage = 1;
			try {
				masterInfo = masterParser.loadTotalPage(_page, _dataset,0, pageHeight, pageWidth , mXArr);
				_masterTotPage 								= (Integer) masterInfo.get(0);
				ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
				
			} catch (XPathExpressionException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			
			_typePageNum = _masterTotPage;
			_typeRealPageNum = _typePageNum;
			
			if( clonePage )
			{
				_typePageNum =  (int) Math.ceil((float) _typePageNum/_cloneRepCnt);
			}
			else
			{
				_typePageNum = _typePageNum;
			}
			returnMap.put("PAGE_INFO_DATA", masterInfo);
			returnMap.put("PAGE_INFO_CLASS", masterParser);
			
			returnMap.put("PAGE_INFO_REQ", masterInfo.get(2));
			returnMap.put("PAGE_INFO_TIDX", masterInfo.get(3));
			
			break;
			
		case 3: //continueBand
			// 로드 타입이 one일경우에만 continueBand의 총 페이지수를 구하기
			ContinueBandParser continueBandParser = new ContinueBandParser(m_appParams);
			continueBandParser.setFunction(mFunction);
			continueBandParser.setMinimumResizeFontSize(_minimumResizeFontSize);
			continueBandParser.setIsExportType(isExportType);
			continueBandParser.setIsExcelOption(isExcelOption);		// Excel 내보내기 옵션을 담기
			
			continueBandParser.setPageMarginTop(mPageMarginTop);
			continueBandParser.setPageMarginLeft(mPageMarginLeft);
			continueBandParser.setPageMarginRight(mPageMarginRight);
			continueBandParser.setPageMarginBottom(mPageMarginBottom);

			if( _page.getAttribute("fitOnePage") != null && _page.getAttribute("fitOnePage").equals("true"))
			{
				continueBandParser.setFitOnePage( true );
			}
			
			ArrayList<Object> bandAr = new ArrayList<Object>();
			
			// 그룹핑 여부 체크
			try {
				bandAr = continueBandParser.loadTotalPage(_page, _dataset,0, pageHeight, pageWidth, mXArr, _allPageList, mParam );
			} catch (NumberFormatException e) {
				
			} catch (XPathExpressionException e) {
				
			} catch (UnsupportedEncodingException e) {
				
			}
			@SuppressWarnings("unchecked")
			ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
			
			if( clonePage )
			{
				_typePageNum =  (int) Math.ceil((float) pagesRowList.size()/_cloneRepCnt);
			}
			else
			{
				_typePageNum = pagesRowList.size();
			}
			_typeRealPageNum = pagesRowList.size();
			returnMap.put("PAGE_INFO_DATA", bandAr);
			returnMap.put("PAGE_INFO_CLASS", continueBandParser);
			
			_requiredItemList = new ArrayList<HashMap<String, Object>>();
			_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
			if(bandAr.size() > 6)
			{
				_requiredItemList =  (ArrayList<HashMap<String, Object>>) bandAr.get(6);
				_tabIndexItemList =  (ArrayList<HashMap<String, Object>>) bandAr.get(7);
			}
			
			returnMap.put("PAGE_INFO_REQ", _requiredItemList);
			returnMap.put("PAGE_INFO_TIDX", _tabIndexItemList);
			
			// 데이터셋 그룹핑 처리
			
			// 그룹핑 된 데이터셋을 이용하여 총 페이지수 구하기 
			
			break;
			
		case 4: //labelBand
			//_typePageNum = mPageNumFn.getLabelBandTotalNum( _page , mDataSetRowCountInfo);
//			_typePageNum = mPageNumFn.getLabelBandTotalNum( _page , mDataSet);
			
			
			// param의 startIndex값이 잇을경우 _pageMaxCnt값과 _dataStNum에서 -처리 시작 bandIndex값을 _dataStNum값이 이 0일경우 startIndex값으로 셋팅
			int _pageStartIndex = 0;
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
			
			_typePageNum = mPageNumFn.getLabelBandTotalNum( _page , _dataset, _pageColumnIndex, _pageRowIndex, _pageStIndex);
			_typeRealPageNum = _typePageNum;
			
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			
			break;
			
		case 5: //barcodePage
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
			
//		case 7: //mobile타입
//			returnMap.put("PAGE_INFO_DATA", null);
//			returnMap.put("PAGE_INFO_CLASS", null);
//			break;
			
		case 8: //lastPage
			_typePageNum = 1;
			_typeRealPageNum = 1;
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
			
//		case 9: //webPage
//			returnMap.put("PAGE_INFO_DATA", null);
//			returnMap.put("PAGE_INFO_CLASS", null);
//			break;
			
		case 10: //subPage
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
			
		case 11: //GroupChart         메뉴 SaveAs중 PDF와 HTML만 사용
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
		case 12: //LinkForm
			// connectLink타입일경우  
			/*
			ConnectLinkParser connectLink = new ConnectLinkParser(this.mServiceReqMng, m_appParams);
			connectLink.setFunction(mFunction);
			connectLink.setMinimumResizeFontSize(_minimumResizeFontSize);
			
			HashMap<String, Object> connectData = null;
			int _totalRelaPageCnt = 1;
			try {
				connectData = connectLink.loadPagesData(_page, mParam, mXArr);
				int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
				_totalRelaPageCnt = Integer.valueOf( connectData.get("totalRealPage").toString() );
				_typePageNum = _connectTotPage;
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_typeRealPageNum = _totalRelaPageCnt;
			returnMap.put("PAGE_INFO_DATA", connectData);
			returnMap.put("PAGE_INFO_CLASS", connectLink);
			*/
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
			
		case 14: //Linked Form Type 
			NodeList _pageList = mDocument.getElementsByTagName("page");
			ArrayList<Element> _tempPageAr = new ArrayList<Element>();
			for (int j = 0; j < _pageList.getLength(); j++) {
				_tempPageAr.add((Element) _pageList.item(j));
			}
			
			if(_page.equals(_pageList.item(0)) == false )
			{
				_typePageNum = 0;
				break;
			}
			
			// Linked Project type 문서 타입
			LinkedPageParser linkedParser = new LinkedPageParser(m_appParams);
			linkedParser.setFunction(mFunction);
			
			linkedParser.setPageMarginTop(mPageMarginTop);
			linkedParser.setPageMarginLeft(mPageMarginLeft);
			linkedParser.setPageMarginRight(mPageMarginRight);
			linkedParser.setPageMarginBottom(mPageMarginBottom);

			linkedParser.setMinimumResizeFontSize(_minimumResizeFontSize);
			
			HashMap<String, Object> linkedData = linkedParser.loadTotalPage(_tempPageAr,_dataset, mXArr );
			int _totPage = (Integer) linkedData.get("totPage");
			ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
			HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
			
			if(clonePage)
			{
				_typePageNum = (int) Math.ceil((float) _totPage/_cloneRepCnt );
			}
			else
			{
				_typePageNum = _totPage;
			}
			_typeRealPageNum = _totPage;
			
			returnMap.put("PAGE_INFO_DATA", linkedData);		// 페이지를 그리는데 필요한 정보를 담아둔 객체( FreeForm 데이터는 null 값 )
			returnMap.put("PAGE_INFO_CLASS", linkedParser);
			break;
		case 16:
			RepeatedFormParser _repeatFormParser = new RepeatedFormParser(m_appParams);
			_repeatFormParser.setFunction(mFunction);
			_repeatFormParser.setPageMarginTop(mPageMarginTop);
			_repeatFormParser.setPageMarginLeft(mPageMarginLeft);
			_repeatFormParser.setPageMarginRight(mPageMarginRight);
			_repeatFormParser.setPageMarginBottom(mPageMarginBottom);
			
			HashMap<String, Object> _resultMap = _repeatFormParser.loadTotalPage(_page, _dataset, 0, pageHeight, pageWidth, mXArr, mParam);
			
			int _pageSize =  Integer.valueOf( _resultMap.get("TOTAL_PAGE").toString() );
			
			if(clonePage)
			{
				_typePageNum = (int) Math.ceil((float) _pageSize/_cloneRepCnt );
			}
			else
			{
				_typePageNum = _pageSize;
			}
			_typeRealPageNum = _pageSize;
			
			returnMap.put("PAGE_INFO_DATA", _resultMap);		// 페이지를 그리는데 필요한 정보를 담아둔 객체( FreeForm 데이터는 null 값 )
			returnMap.put("PAGE_INFO_CLASS", _repeatFormParser);
			break;			
		case 99: //사용자 저장 문서
			
			break;

		}
		mTOTAL_PAGE_NUM = mTOTAL_PAGE_NUM + _typePageNum;		// 전체 페이지
		
		returnMap.put("TOTALPAGE", mTOTAL_PAGE_NUM);
		returnMap.put("REAL_PAGE_NUM", _typeRealPageNum);
		returnMap.put("PAGE_NUM", _typePageNum);
		
		
//		if( _cloneColCnt > 1)
//		{
//			
//			for (int i = 0; i < _cloneColCnt; i++) {
//				pageWidth*i;
//				mXArr.add(arg0)
//			}
//		}
		
		// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
		return returnMap;
	}
	
	
	public HashMap<String, Object> pageTypeToTotalNumSimple(int _pageType , PageInfoSimple _page, Object _lodeType, ArrayList<Integer> mXArr, ArrayList<PageInfoSimple> _pageList) throws Exception
	{
		
		int _typePageNum = 0;
		int _typeRealPageNum = 0;
		float pageWidth = _page.getWidth();
		float pageHeight = _page.getHeight();
		String cloneData = "";
		boolean clonePage = false;
		
		int mTOTAL_PAGE_NUM = 0;
		
		ArrayList<Object> pageDataList = new ArrayList<Object>();
		HashMap<String, Object> returnMap = new HashMap<String, Object>();
		
		// Export Type를 
		String isExportType = m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE();
		
		String isExcelOption = m_appParams.getREQ_INFO().getEXCEL_OPTION();
		
		// Clone Page값 담기
		cloneData = _page.getClone();
		clonePage = false;

		ArrayList<HashMap<String, Object>> _requiredItemList = null;
		ArrayList<HashMap<String, Object>> _tabIndexItemList = null;

		int _cloneColCnt = 1;
		int _cloneRowCnt = 1;
		int _cloneRepCnt = 1;
		String _cloneDirect = "";	// cloneDirect Across Down: 가로, Down Across : 세로
		// pageMargin이 있을경우 TotalPage를 구하는 부분에서는 
		// pageHeight와 pageWidth를 변경한다. 
		
		pageWidth = pageWidth - mPageMarginLeft - mPageMarginRight;
		pageHeight = pageHeight - mPageMarginTop - mPageMarginBottom;
		
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
			  _cloneColCnt = _page.getCloneColCount();
			  _cloneRowCnt = _page.getCloneRowCount();
			}
			
			if(_page.getDirection() != null && _page.getDirection().equals("") == false)
			{
			  _cloneDirect = _page.getDirection();
			}
			
			pageWidth = pageWidth / _cloneColCnt;
			pageHeight = pageHeight / _cloneRowCnt;
			
			_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
			clonePage = true;
		}
		  
		int _minimumResizeFontSize = 0;
		if( _page.getMinimumResizeFontSize() > 0 )
		{
			_minimumResizeFontSize = _page.getMinimumResizeFontSize();
		}
		
		// Clone 속성이 존재할경우 페이지의 사이즈를 변경하여 총 그려지는 화면을 생성
		//1. cloneHorizontal, cloneVertical 값을 이용해서 실제 한화면의 사이즈를 구하기
		
		//2. 총 화면수 구하기
		
		//3. 실제 페이지의 수 구하기
		
		//4. 페이지 생성시 cloneX, cloneY값을 변경해가며 화면의 아이템 생성
		
		//5. 내보내기 및 connectPage 도 clone 처리
		
		switch (_pageType) {
		case 0: //coverPage
			_typePageNum = 1;
			
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
			
		case 1: //freeform
		case 7: //mobile타입
		case 9: //webPage
			_typePageNum = mPageNumFn.getFreeFormTotalNumSimple( _page , _page.getDataSet());
			_typeRealPageNum = _typePageNum;
			if(clonePage)
			{
				_typePageNum = (int) Math.ceil((float) _typePageNum/_cloneRepCnt);
			}
			_requiredItemList = getRequiredItemDataInfo(_page);
			_tabIndexItemList = getTabIndexItemDataInfo(_page);
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			returnMap.put("PAGE_INFO_REQ", _requiredItemList);
			returnMap.put("PAGE_INFO_TIDX", _tabIndexItemList);
			break;
			
		case 2: //masterBand
			// 로드 타입이 one일경우에만 masterBand의 총 페이지수를 구하기
			MasterBandParser masterParser = new MasterBandParser(this.m_appParams);
			masterParser.setFunction(mFunction);
			masterParser.setMinimumResizeFontSize(_minimumResizeFontSize);
			
			masterParser.setPageMarginTop(mPageMarginTop);
			masterParser.setPageMarginLeft(mPageMarginLeft);
			masterParser.setPageMarginRight(mPageMarginRight);
			masterParser.setPageMarginBottom(mPageMarginBottom);
			masterParser.setIsExportType(isExportType);

			masterParser.setTempletInfo(_page.getProjectInfo().getTempletInfo());
			
			ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
			ArrayList<Object> masterInfo = null;
			int _masterTotPage = 1;
			try {
				masterInfo = masterParser.loadTotalPage(_page,  _page.getDataSet(),0, pageHeight, pageWidth , mXArr);
				_masterTotPage 								= (Integer) masterInfo.get(0);
				ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
				
			} catch (XPathExpressionException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			
			_typePageNum = _masterTotPage;
			_typeRealPageNum = _typePageNum;
			
			if( clonePage )
			{
				_typePageNum =  (int) Math.ceil((float) _typePageNum/_cloneRepCnt);
			}
			else
			{
				_typePageNum = _typePageNum;
			}
			
			_page.setPageParserClass(masterParser);
			
			returnMap.put("PAGE_INFO_DATA", masterInfo);
			
			returnMap.put("PAGE_INFO_REQ", masterInfo.get(2));
			returnMap.put("PAGE_INFO_TIDX", masterInfo.get(3));
			break;
		case 3: //continueBand
			// 로드 타입이 one일경우에만 continueBand의 총 페이지수를 구하기
			ContinueBandParser continueBandParser = new ContinueBandParser(m_appParams);
			continueBandParser.setFunction(mFunction);
			continueBandParser.setMinimumResizeFontSize(_minimumResizeFontSize);
			continueBandParser.setIsExportType(isExportType);
			continueBandParser.setIsExcelOption(isExcelOption);		// Excel 내보내기 옵션을 담기
			
			continueBandParser.setPageMarginTop(mPageMarginTop);
			continueBandParser.setPageMarginLeft(mPageMarginLeft);
			continueBandParser.setPageMarginRight(mPageMarginRight);
			continueBandParser.setPageMarginBottom(mPageMarginBottom);
			
			continueBandParser.setTempletInfo(_page.getProjectInfo().getTempletInfo());
			
			ArrayList<Object> bandAr = new ArrayList<Object>();
			
			if( _page.getFitOnePage() )
			{
				continueBandParser.setFitOnePage( _page.getFitOnePage() );
			}
			
			// 그룹핑 여부 체크
			try {
				bandAr = continueBandParser.loadTotalPageSimple(_page, _page.getDataSet(), 0, pageHeight, pageWidth, mXArr, mParam );
			} catch (NumberFormatException e) {
				
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(0);
			
			if( clonePage )
			{
				_typePageNum =  (int) Math.ceil((float) pagesRowList.size()/_cloneRepCnt);
			}
			else
			{
				_typePageNum = pagesRowList.size();
			}
			_typeRealPageNum = pagesRowList.size();
			returnMap.put("PAGE_INFO_DATA", bandAr);
			returnMap.put("PAGE_INFO_CLASS", continueBandParser);
			
			_page.setPageParserClass(continueBandParser);
			
			_requiredItemList = new ArrayList<HashMap<String, Object>>();
			_tabIndexItemList = new ArrayList<HashMap<String, Object>>();
			if(bandAr.size() > 3)
			{
				_requiredItemList =  (ArrayList<HashMap<String, Object>>) bandAr.get(2);
				_tabIndexItemList =  (ArrayList<HashMap<String, Object>>) bandAr.get(3);
			}
			
			returnMap.put("PAGE_INFO_REQ", _requiredItemList);
			returnMap.put("PAGE_INFO_TIDX", _tabIndexItemList);
			
			// 데이터셋 그룹핑 처리
			
			// 그룹핑 된 데이터셋을 이용하여 총 페이지수 구하기 
			break;
		case 4: //labelBand
			// param의 startIndex값이 잇을경우 _pageMaxCnt값과 _dataStNum에서 -처리 시작 bandIndex값을 _dataStNum값이 이 0일경우 startIndex값으로 셋팅
			int _pageStartIndex = 0;
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
			
			_typePageNum = mPageNumFn.getLabelBandTotalNumSimple( _page , _page.getDataSet(), _pageColumnIndex, _pageRowIndex, _pageStIndex);
			_typeRealPageNum = _typePageNum;
			
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			
			break;
		case 8: //lastPage
			_typePageNum = 1;
			_typeRealPageNum = 1;
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
/**
//		case 9: //webPage
//			returnMap.put("PAGE_INFO_DATA", null);
//			returnMap.put("PAGE_INFO_CLASS", null);
//			break;
			
		case 10: //subPage
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
			
		case 11: //GroupChart         메뉴 SaveAs중 PDF와 HTML만 사용
			returnMap.put("PAGE_INFO_DATA", null);
			returnMap.put("PAGE_INFO_CLASS", null);
			break;
		case 12: //LinkForm
			// connectLink타입일경우  
			ConnectLinkParser connectLink = new ConnectLinkParser(this.mServiceReqMng, m_appParams);
			connectLink.setFunction(mFunction);
			connectLink.setMinimumResizeFontSize(_minimumResizeFontSize);
			
			HashMap<String, Object> connectData = null;
			int _totalRelaPageCnt = 1;
			try {
				connectData = connectLink.loadPagesData(_page, mParam, mXArr);
				int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
				_totalRelaPageCnt = Integer.valueOf( connectData.get("totalRealPage").toString() );
				_typePageNum = _connectTotPage;
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_typeRealPageNum = _totalRelaPageCnt;
			returnMap.put("PAGE_INFO_DATA", connectData);
			returnMap.put("PAGE_INFO_CLASS", connectLink);
			break;*/
		case 14: //Linked Form Type 
			
			// Linked Project type 문서 타입
			LinkedPageParser linkedParser = new LinkedPageParser(m_appParams);
			linkedParser.setFunction(mFunction);
			linkedParser.setIsExportType(isExportType);
			
			linkedParser.setPageMarginTop(mPageMarginTop);
			linkedParser.setPageMarginLeft(mPageMarginLeft);
			linkedParser.setPageMarginRight(mPageMarginRight);
			linkedParser.setPageMarginBottom(mPageMarginBottom);
			
			linkedParser.setMinimumResizeFontSize(_minimumResizeFontSize);
			
			linkedParser.setTempletInfo(mTempletInfo);
			
			HashMap<String, Object> linkedData = linkedParser.loadTotalPageSimple( _pageList,_page.getProjectInfo().getDataSet(), mXArr );
			int _totPage = (Integer) linkedData.get("totPage");
			ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
			HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
			
			if(clonePage)
			{
				_typePageNum = (int) Math.ceil((float) _totPage/_cloneRepCnt );
			}
			else
			{
				_typePageNum = _totPage;
			}
			_typeRealPageNum = _totPage;
			
			returnMap.put("PAGE_INFO_DATA", linkedData);		// 페이지를 그리는데 필요한 정보를 담아둔 객체( FreeForm 데이터는 null 값 )
			returnMap.put("PAGE_INFO_CLASS", linkedParser);
			break;
	/**	case 16:
			RepeatedFormParser _repeatFormParser = new RepeatedFormParser();
			_repeatFormParser.setFunction(mFunction);
			
			HashMap<String, Object> _resultMap = _repeatFormParser.loadTotalPage(_page, _dataset, 0, pageHeight, pageWidth, mXArr, mParam);
			
			int _pageSize =  Integer.valueOf( _resultMap.get("TOTAL_PAGE").toString() );
			
			if(clonePage)
			{
				_typePageNum = (int) Math.ceil((float) _pageSize/_cloneRepCnt );
			}
			else
			{
				_typePageNum = _pageSize;
			}
			_typeRealPageNum = _pageSize;
			
			returnMap.put("PAGE_INFO_DATA", _resultMap);		// 페이지를 그리는데 필요한 정보를 담아둔 객체( FreeForm 데이터는 null 값 )
			returnMap.put("PAGE_INFO_CLASS", _repeatFormParser);
			break;
			*/
		case 99: //사용자 저장 문서
			
			break;

		}
		mTOTAL_PAGE_NUM = mTOTAL_PAGE_NUM + _typePageNum;		// 전체 페이지
		
		returnMap.put("TOTALPAGE", mTOTAL_PAGE_NUM);
		returnMap.put("REAL_PAGE_NUM", _typeRealPageNum);
		returnMap.put("PAGE_NUM", _typePageNum);
		
		if( mTempletInfo != null)
		{
			mXArr = TempletItemInfo.updateTempletXPosition( _page.getId(), mXArr, mTempletInfo );
		}
		
		// TOTALPAGE : 총 페이지, REAL_PAGE_NUMLIST : 실제각 페이지별 인덱스, PAGE_NUMLIST: 화면상의각 페이지, PAGE_INFO_DATA : 페이지를 그리는데 사용되는 객체정보
		return returnMap;
	}

	
	// 데이터셋을 그룹핑하여 내보내기
	
	
	
	/**
	 * functionName : getGroupingDataSet</br>
	 * @param _dataList : ArrayList<String>  데이터셋들의 리스트
	 * @return
	 */
	public static ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> getGroupingDataSet( HashMap<String, ArrayList<HashMap<String, Object>>> _dataS, ArrayList<HashMap<String, Object>> _dataList )
	{
		// dataSet = {dataSet:데이터셋명, columns:[컬럼,컬럼]},{dataSet:데이터셋명, columns:[컬럼,컬럼]} 
		// sort  = { dataSet:데이터 셋명, columns:[컬럼,컬럼], orderBy:[true,true], isNumeric:[true,false] }
		
		
		HashMap<String, HashMap<String, ArrayList<HashMap<String,Object>>>> _groupingData = new HashMap<String, HashMap<String,ArrayList<HashMap<String,Object>>>>();
		ArrayList<String> groupList = new ArrayList<String>();
		
		int i = 0;
		int j = 0;
		int max2 = 0;
		int max = 0;
		max =  _dataList.size();
		String _currentDataName = "";
		ArrayList<String> _columns = new ArrayList<String>();
		ArrayList<Object> _retData = new ArrayList<Object>();
		ArrayList<String> _noGrpData = new ArrayList<String>();
		ArrayList<String> _grpingDatanames = new ArrayList<String>();
		
		// 컬럼값을 이용하여 그룹핑 처리
		for ( i = 0; i < max; i++) {
			_currentDataName = _dataList.get(i).get("dataset").toString();
			_columns = (ArrayList<String>) _dataList.get(i).get("columns");
			_retData = GroupingDataSetProcess.groupingDataSetProcess( _currentDataName, _dataS.get(_currentDataName), _columns, _groupingData,groupList);
			
			_groupingData = (HashMap<String, HashMap<String, ArrayList<HashMap<String,Object>>>>) _retData.get(0);
			groupList = (ArrayList<String>) _retData.get(1);
			
			if(_grpingDatanames.indexOf(_currentDataName) == -1) _grpingDatanames.add(_currentDataName);
		}
		
		// 원본 데이터에서 그룹핑 되지 않은 데이터가 존재하는지 체크
		Set key = _dataS.keySet();
		
		for (Iterator iterator = key.iterator(); iterator.hasNext();) {
			String keyName = (String) iterator.next();
			if(_grpingDatanames.indexOf(keyName) == -1)
			{
				_noGrpData.add(keyName);
			}
		}
		// 원본 데이터셋과 비교 종료
		
		max  = groupList.size();
		max2 = _dataList.size();
		
		// 그룹으로 만들어진 맵에서 데이터셋이 없을경우 신규로 빈 데이터를 생성하여 add
		for ( i = 0; i < max; i++) {
			
			for ( j = 0; j < max2; j++) {
				_currentDataName = _dataList.get(j).get("dataset").toString();
				if( _groupingData.get(groupList.get(i)).containsKey( _currentDataName ) == false )
				{
					_groupingData.get(groupList.get(i)).put( _currentDataName, new ArrayList<HashMap<String,Object>>());
				}
			}
			
		}
		
		ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> retDataAr = new ArrayList<HashMap<String,ArrayList<HashMap<String,Object>>>>();
		for ( i = 0; i < max; i++) {
			
			max2 = _noGrpData.size();
			for ( j = 0; j < max2; j++) {
				_groupingData.get(groupList.get(i)).put( _noGrpData.get(j), _dataS.get(_noGrpData.get(j)) );
			}
			
			retDataAr.add(_groupingData.get(groupList.get(i)));
		}
		
		_groupingData = null;
		
		return retDataAr;
	}
	
	public static ArrayList<HashMap<String, Object>> checkGroupingDataInfo( Element _page )
	{
		
//		<groupData>
//	      <property name="groupData" value="%7B%22groupColumn%22%3A%22col_0%2Ccol_1%22%2C%22dataset%22%3A%22dataset_0%22%7D" type="HashTable"/>
//	      <property name="groupData" value="%7B%22groupColumn%22%3A%22col_1%2Ccol_2%22%2C%22dataset%22%3A%22dataset_1%22%7D" type="HashTable"/>
//	      <property name="groupData" value="%7B%22groupColumn%22%3A%22col_3%2Ccol_4%22%2C%22dataset%22%3A%22dataset_2%22%7D" type="HashTable"/>
//	    </groupData>
	    
		NodeList _groupDataInfo = _page.getElementsByTagName("groupData");
		
		ArrayList<HashMap<String, Object>> _dataList = new ArrayList<HashMap<String, Object>>();
		
		if( _groupDataInfo == null || _groupDataInfo.getLength() < 1)
		{
			return _dataList;
		}
		
		Element _groupDataEl = (Element) _groupDataInfo.item(0);
		
		NodeList _propertis = _groupDataEl.getElementsByTagName("property");
		Element _property;
		String _value = "";
		boolean returnFlag = true;
		
		for (int i = 0; i < _propertis.getLength(); i++) {
			
			_property = (Element) _propertis.item(i);
			_value = "";
			
			try {
				
				_value = URLDecoder.decode(_property.getAttribute("value").toString(), "UTF-8");
				JSONParser _jparser = new JSONParser();
				JSONObject _data = (JSONObject) _jparser.parse(_value);
				
				HashMap<String, Object> _temp = new HashMap<String, Object>();
				_temp.put("dataset", _data.get("dataset"));
				ArrayList<String> _tempColumns = Value.setArrayString( _data.get("groupColumn").toString() );
				_temp.put("columns", _tempColumns);
				_dataList.add(_temp);
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
		
		
		return _dataList;
	}
	
	
	private ArrayList<HashMap<String, Object>> getRequiredItemDataInfo( PageInfo _page )
	{
		ArrayList<String> _groupDataInfo = _page.getRequiredValueList();
		
		return getRequiredItemDataInfo(_groupDataInfo);
	}
	
	private ArrayList<HashMap<String, Object>> getRequiredItemDataInfo( PageInfoSimple _page )
	{
		ArrayList<String> _groupDataInfo = _page.getRequiredValueList();
		
		return getRequiredItemDataInfo(_groupDataInfo);
	}
	
	private ArrayList<HashMap<String, Object>> getRequiredItemDataInfo( ArrayList<String> _groupDataInfo )
	{
		ArrayList<HashMap<String, Object>> _dataList = new ArrayList<HashMap<String, Object>>();

		int _groupDataInfoSize = _groupDataInfo!=null ? _groupDataInfo.size() : 0;
		if( _groupDataInfoSize < 1)
		{
			return _dataList;
		}

		String _value = "";
		
		for (int i = 0; i < _groupDataInfoSize; i++) {
			
			_value = "";
			
			try {
				_value = URLDecoder.decode( _groupDataInfo.get(i), "UTF-8");
				HashMap<String, Object> _temp = new HashMap<String, Object>();
				_temp.put("id", _value );
				_dataList.add(_temp);
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} 
			
		}
		
		return _dataList;
	}
	
	private ArrayList<HashMap<String, Object>> getRequiredItemDataInfo( Element _page )
	{
		NodeList _groupDataInfo = _page.getElementsByTagName("requiredItem");
		
		ArrayList<HashMap<String, Object>> _dataList = new ArrayList<HashMap<String, Object>>();
		
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
				
				HashMap<String, Object> _temp = new HashMap<String, Object>();
				_temp.put("id", _value );
				_dataList.add(_temp);
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} 
			
		}
		
		
		return _dataList;
	}
	
	
	private ArrayList<HashMap<String, Object>> getTabIndexItemDataInfo( PageInfo _page )
	{
		ArrayList<String> _groupDataInfo = _page.getTabIndexList();
		
		return getTabIndexItemDataInfo(_groupDataInfo);
	}
	private ArrayList<HashMap<String, Object>> getTabIndexItemDataInfo( PageInfoSimple _page )
	{
		ArrayList<String> _groupDataInfo = _page.getTabIndexList();
		
		return getTabIndexItemDataInfo(_groupDataInfo);
	}
	private ArrayList<HashMap<String, Object>> getTabIndexItemDataInfo( ArrayList<String> _groupDataInfo )
	{
		ArrayList<HashMap<String, Object>> _dataList = new ArrayList<HashMap<String, Object>>();
		
		int _groupDataInfoSize = _groupDataInfo!=null ? _groupDataInfo.size() : 0;
		if( _groupDataInfoSize < 1)
		{
			return _dataList;
		}
		
		Element _property;
		String _value = "";
		
		for (int i = 0; i < _groupDataInfoSize; i++) {
			
			_value = "";
			
			try {
				_value = URLDecoder.decode(_groupDataInfo.get(i), "UTF-8");
				
				HashMap<String, Object> _temp = new HashMap<String, Object>();
				_temp.put("id", _value );
				_dataList.add(_temp);
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} 
		}
		
		return _dataList;
	}
	
	private ArrayList<HashMap<String, Object>> getTabIndexItemDataInfo( Element _page )
	{
		NodeList _groupDataInfo = _page.getElementsByTagName("tabIndexItem");
		
		ArrayList<HashMap<String, Object>> _dataList = new ArrayList<HashMap<String, Object>>();
		
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
				
				HashMap<String, Object> _temp = new HashMap<String, Object>();
				_temp.put("id", _value );
				_dataList.add(_temp);
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} 
			
		}
		
		
		return _dataList;
	}
	
	private void setPdfProtectionInfo(Element _doc, HashMap<String, ArrayList<HashMap<String, Object>>> _dataset )
	{
		
		NodeList _pdfSet = _doc.getElementsByTagName("pdfset");
		int i = 0;
		int cnt = 0;
		HashMap<String, String> _pdfProtectionInfo;
		String _pdfPassword = "";
		String _pdfProtection = "";
		
		if( _pdfSet != null && _pdfSet.getLength() > 0 )
		{
			Element _pdfSetEl = (Element) _pdfSet.item(0);
			NodeList _properties = _pdfSetEl.getElementsByTagName("property");
			Element _property;
			cnt = _properties.getLength();
			
			_pdfProtectionInfo = new HashMap<String, String>();
			
			for( i=0; i < cnt; i++ )
			{
				_property = (Element) _properties.item(i);
				_pdfProtectionInfo.put(_property.getAttribute("name"), _property.getAttribute("value"));
			}
			
			//	pdf protected 가 dataset일때는 데이터셋의 특정 컬럼값을 내보내도록 한다.
			if( _pdfProtectionInfo.containsKey("type") )
			{
				if( _pdfProtectionInfo.get("type").equals("dataset"))
				{
					String _dsName = _pdfProtectionInfo.get("passwordDataset");
					String _dsColumn = _pdfProtectionInfo.get("passwordColumn");
					
					if( _dataset != null && _dataset.containsKey(_dsName) && _dataset.get(_dsName).size() > 0 && _dataset.get(_dsName).get(0).containsKey(_dsColumn) )
					{
						_pdfPassword = _dataset.get(_dsName).get(0).get(_dsColumn).toString();
					}
					
				}
				else if( _pdfProtectionInfo.containsKey("passwordText") )
				{
					_pdfPassword = _pdfProtectionInfo.get("passwordText");
				}
				
			}
			
			if( _pdfProtectionInfo.containsKey("protection") )
			{
				_pdfProtection = _pdfProtectionInfo.get("protection");
			}
			
			mPDFProtectionInfo = new HashMap<String, String>();
			mPDFProtectionInfo.put("PDF_PASSWORD", _pdfPassword);
			mPDFProtectionInfo.put("USE_PROTECTION", _pdfProtection);
		}
		
	}
	
	
	private void setPdfProtectionInfo(ProjectInfo _doc, HashMap<String, ArrayList<HashMap<String, Object>>> _dataset )
	{
		
		HashMap<String, Object> _pdfSet = _doc.getPdfSet();
		int i = 0;
		int cnt = 0;
		HashMap<String, String> _pdfProtectionInfo;
		String _pdfPassword = "";
		String _pdfProtection = "";
		
		if( _pdfSet != null && !_pdfSet.isEmpty() )
		{
			
			//	pdf protected 가 dataset일때는 데이터셋의 특정 컬럼값을 내보내도록 한다.
			if( _pdfSet.containsKey("type") )
			{
				if( _pdfSet.get("type").equals("dataset"))
				{
					String _dsName = _pdfSet.get("passwordDataset").toString();
					String _dsColumn = _pdfSet.get("passwordColumn").toString();
					
					if( _dataset != null && _dataset.containsKey(_dsName) && _dataset.get(_dsName).size() > 0 && _dataset.get(_dsName).get(0).containsKey(_dsColumn) )
					{
						_pdfPassword = _dataset.get(_dsName).get(0).get(_dsColumn).toString();
					}
					
				}
				else if( _pdfSet.containsKey("passwordText") )
				{
					_pdfPassword = _pdfSet.get("passwordText").toString();;
				}
				
			}
			
			if( _pdfSet.containsKey("protection") )
			{
				_pdfProtection = _pdfSet.get("protection").toString();
			}
			
			mPDFProtectionInfo = new HashMap<String, String>();
			mPDFProtectionInfo.put("PDF_PASSWORD", _pdfPassword);
			mPDFProtectionInfo.put("USE_PROTECTION", _pdfProtection);
		}
		
	}
	
	
	public static ArrayList<HashMap<String, Object>> checkGroupingDataInfo( ArrayList<HashMap<String, Object>> _groupDataInfo, ArrayList<HashMap<String, Object>> _dataList )
	{
		int _groupDataInfoSize = _groupDataInfo!=null ? _groupDataInfo.size() : 0;
		if( _groupDataInfoSize < 1)
		{
			return _dataList;
		}
		
		HashMap<String, Object> _groupDataMap;
		String _value = "";
		
		for (int i = 0; i < _groupDataInfo.size(); i++) {
			
			_groupDataMap = _groupDataInfo.get(i);
			_value = "";
			
			HashMap<String, Object> _temp = new HashMap<String, Object>();
			_temp.put("dataset", _groupDataMap.get("dataset"));
			ArrayList<String> _tempColumns = Value.setArrayString( _groupDataMap.get("groupColumn").toString() );
			_temp.put("columns", _tempColumns);
			_dataList.add(_temp);
		}
		
		return _dataList;
	}
	
	public static ArrayList<HashMap<String, Object>> checkGroupingDataInfo( PageInfo _page )
	{
		ArrayList<HashMap<String, Object>> _groupDataInfo = _page.getGroupData();
		ArrayList<HashMap<String, Object>> _dataList = new ArrayList<HashMap<String, Object>>();
		
		return checkGroupingDataInfo(_groupDataInfo, _dataList );
	}

	public static ArrayList<HashMap<String, Object>> checkGroupingDataInfo( PageInfoSimple _page )
	{
		ArrayList<HashMap<String, Object>> _groupDataInfo = _page.getGroupData();
		ArrayList<HashMap<String, Object>> _dataList = new ArrayList<HashMap<String, Object>>();
		
		return checkGroupingDataInfo(_groupDataInfo, _dataList );
	}
	
	public void setExportFileName( String _value )
	{
		mExportFileName = _value;
	}
	public String getExportFileName()
	{
		return mExportFileName;
	}
	
	public void setExportMethodType(String _value)
	{
		mExportMethodType = _value;
	}
	
	public String getExportMethodType()
	{
		return mExportMethodType;
	}
	
	public void setUseFileSplit(boolean _value)
	{
		mUseFileSplit = _value;
	}
	
}
