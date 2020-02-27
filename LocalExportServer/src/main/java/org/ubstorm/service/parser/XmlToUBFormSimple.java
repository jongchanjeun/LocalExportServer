package org.ubstorm.service.parser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.ubstorm.server.printable.FORMFile;
//import org.ubstorm.service.DataServiceManager;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.formparser.ConnectLinkParser;
import org.ubstorm.service.parser.formparser.ContinueBandParser;
import org.ubstorm.service.parser.formparser.ItemConvertParser;
import org.ubstorm.service.parser.formparser.MasterBandParser;
import org.ubstorm.service.parser.formparser.RepeatedFormParser;
import org.ubstorm.service.parser.formparser.UBIDataUtilPraser;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.BandInfoMapDataSimple;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.TempletItemInfo;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.DataSetInfo;
import org.ubstorm.service.parser.formparser.info.PageInfo;
import org.ubstorm.service.parser.formparser.info.PageInfoSimple;
import org.ubstorm.service.parser.formparser.info.ProjectInfo;
//import org.ubstorm.service.request.ServiceRequestManager;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlToUBFormSimple extends xmlToUbForm {

	public XmlToUBFormSimple(UDMParamSet appParams) {
		super(appParams);
		// TODO Auto-generated constructor stub
	}
	
	protected ArrayList<PageInfoSimple> mPageAr;
	
	@Override
	public String formPreviewManager(String _xml , HashMap _param, FORMFile formfile , boolean isMakePage) throws XPathExpressionException, SAXException, Exception, IOException, ParserConfigurationException, ParseException
	{
		// 초기화
		resetVariable();
		
		_startTimeDate = new Date();
		_checkStartTime = _startTimeDate.getTime();
		
		mPDF_EXPORT_TYPE = (String) _param.get("PDF_EXPORT_TYPE");
		
		mSERVER_URL = (String) _param.get("serverUrl");
		
		mCLIENT_IP = (String) _param.get("CLIENT_IP");
		mPAGE_NUM = (Integer) _param.get("PAGE_NUM");
		Object loadType = _param.get("LOAD_TYPE");
		String _client_ssid =  m_appParams.getREQ_INFO().getCLIENT_SESSION_ID();
		
		Object _params = _param.get("PARAMS");
		if( _params.hashCode() != 0 )
		{
		 	Object ubObj = JSONValue.parseWithException((String)_params);	
		 	mParam = (JSONObject)ubObj;
		}
		
		// xml 을 document 형태로 변경.
		// Project 중 필요한 속성을 담음.
		xmlToDocument(_xml);
		
		if( GlobalVariableData.USE_TIME_LOG )
		{
			Date _chkDate = new Date();
			long _chkTime = _chkDate.getTime() -_checkStartTime ;
			
			long chkms = _chkTime % 1000;
			long chkSec = _chkTime / 1000% 60;         
			long chkMin = _chkTime / (60 * 1000)% 60;     
			
			log.info("XML_LOAD_TIME : " + chkMin +"min " + chkSec + "sec " + chkms + "ms ");
			_checkStartTime = _chkDate.getTime();
		}
		
		Date _date = new Date();  
		log.debug("[" + _client_ssid + "] XML_LOAD_TIME : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_date) +"]" );
		
//		if( getPasswordCheck( loadType ) == false ) return null;
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _changeDataSet = null;
		
		if(documents == null && _param.containsKey("CHANGE_DATASET") && _param.get("CHANGE_DATASET") != null  && _param.get("CHANGE_DATASET").equals("") == false )
		{
			_changeDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>)_param.get("CHANGE_DATASET");
			mProjectInfos.get(0).setDataSet(_changeDataSet);	// 데이터셋을 가져와서 프로젝트 정보에 담아두기
		}
		else
		{
			getXmlToDataSet();
		}		
		
		TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser( m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );
		
		mFunction.setFunctionVersion(FUNCTION_VERSION);		
		
		// 여러건의 Form을 로드하기 위한 분기 @최명진
		if(mUseMultiFormType)
		{
			_pageInfoMap = _totPageCheckParser.getTotalPageMergeForm(documentParams, mDocument, dataSets, mFunction, documentInfos  );
		}
		else if(documents != null && documents.size() > 0)
		{
			_pageInfoMap = _totPageCheckParser.getTotalPageMulti(documentParams, documents, dataSets, mFunction, documentInfos  );
		}
		else
		{
			 //_pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
			_pageInfoMap = _totPageCheckParser.getTotalPageSimple(documentParams, mProjectInfos, mFunction );
		}
				
//		xmlToDataSet();		// 여러건의 Form의 데이터 셋을 로드하기 위하여 지정
		
		if( GlobalVariableData.USE_TIME_LOG )
		{
			Date _chkDate = new Date();
			long _chkTime = _chkDate.getTime() -_checkStartTime ;
			
			long chkms = _chkTime % 1000;
			long chkSec = _chkTime / 1000% 60;         
			long chkMin = _chkTime / (60 * 1000)% 60;     
			
			log.info("QUERY_LOAD_TIME : " + chkMin +"min " + chkSec + "sec " + chkms + "ms ");
			_checkStartTime = _chkDate.getTime();
		}
		
		_date = new Date();  
		log.debug("[" + _client_ssid + "] QUERY_LOAD_TIME : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_date) +"]" );
		
		// project에 password값이 잇을경우 패스워드를 입력받도록 처리
		
		mHashMap.put("pageSize", mPageSize);		
		
		// mHashMap 에 프린트 옵션을 지정 2016-04-21 최명진
		mHashMap = getPrintOption(mHashMap, mParam);
		
		mHashMap = getUsedParameterPopup(mHashMap);
		
		// Project info send
		Object clientEditMode =  CLIENT_EDIT_MODE ;
		if(clientEditMode.equals("ON"))
		{
			String _dataSets = JSONObject.toJSONString(mDataSet);
			formfile.addPageB64(-1, ("{\"project\":" + ((JSONObject) mHashMap.get("project")).toJSONString() + ",\"dataSets\":" + _dataSets +  ",\"pageSize\":" + mPageSize + ",\"resultType\":\"FORM\",\"printOption\":" +  ((JSONObject) mHashMap.get("printOption")).toJSONString() + "}").getBytes("UTF-8"));
		}
		else
		{
			formfile.addPageB64(-1, ("{\"project\":" + ((JSONObject) mHashMap.get("project")).toJSONString() + ",\"pageSize\":" + mPageSize + ",\"resultType\":\"FORM\",\"printOption\":" +  ((JSONObject) mHashMap.get("printOption")).toJSONString() + "}").getBytes("UTF-8"));
		}
		//(HashMap<String,Object>)
		formfile.addProjectInfo((JSONObject)mHashMap.get("project"));
		
		//한장만 생성
		int pageIdx = 0;
		String result = "";
		
		if(isMakePage){
			result = _formExportManagerOne( formfile);	
			if(Log.printStop){
				//Log.printStop = false;
				//merge message
				result = Log.getMessage(Log.MSG_LP_PRINTJOB_CANCEL);				
	
				Thread.sleep(100);
				Log.printStop = false;
	
				return "CANCEL:" + result;
			}
			_totPageCheckParser = null;
		}
		return result;
	}	
	
	//페이지 범위 설정 시 해당 페이지 만큼  생성
	public String _formExportManagerPageRange(FORMFile formfile, String pageRange) throws Exception
	{
		String _hashStr = null;
	//		Object clientEditMode = _param.get("CLIENT_EDIT_MODE");
		Object clientEditMode = CLIENT_EDIT_MODE;
		
		log.debug(getClass().getName() + "::" + "Call _formExportManagerPageRange...clientEditMode=" + clientEditMode);
		if( !mParamResult )
		{
	//			getTotalPage(_param);
			getBinDivCompReportPagesRange(true, "ON".equals(clientEditMode), formfile , pageRange );	
		}
		else
		{
			_hashStr = mHashMap.toJSONString();
			//this.mServiceReqMng.getServiceManager().sendResultB64(_hashStr.getBytes("UTF-8"), true, true, -1, -1);
			//this.mServiceReqMng.getServiceManager().end();
			
			System.out.println("_formExportManagerPageRange::mParamResult == true!!!");			
		}			
		return null;
	}
	
	//c#호출 용 한페이지 생성
	public String _formExportManagerOne(FORMFile formfile) throws Exception
	{		
		return _formExportManagerPageRange(formfile ,"1");
	}
	
	// 전체 페이지를 페이지 별로 생성하여, 페이지 별로 클라이언트로 분할 전송한다. (데이터는 전체 데이터를 가져와서 작업함)
	/* (non-Javadoc)
	 * @see org.ubstorm.service.parser.xmlToUbForm#getBinDivCompReportPagesRange(boolean, boolean, org.ubstorm.server.printable.FORMFile, java.lang.String)
	 */
	public void getBinDivCompReportPagesRange(boolean bSupportIE9, boolean bSupportEform, FORMFile formfile, String pageRange ) throws UnsupportedEncodingException , Exception
	{
		//mHashMap.put("resultType","FORM");
		int _reportType = 0;
		int _argoPage = 0;
		HashMap<String, String> _pageHash = new HashMap<String, String>();
		HashMap< String, HashMap<String, Object> > pages = new HashMap< String, HashMap<String, Object> >();
		HashMap<String, Object > pageInfoData = null;
		
		String _client_ssid = m_appParams.getREQ_INFO().getCLIENT_SESSION_ID();		
		
		String stSendData = "";
		int _pageCnt = 0;
		int j = 0;
		
		float pageWidth = 0;
		float pageHeight = 0;
		
		float originalPageWidth = 0;
		float originalPageHeight = 0;
		ArrayList<Float> _clonePositionList = null;
		
		String cloneData = "";
		float cloneX = 0;
		float cloneY = 0;
		ArrayList<HashMap<String, Object>> _objects = null;
		
		boolean clonePage = false;
		boolean isFirstPage = true;
		//DataServiceManager oService = this.mServiceReqMng.getServiceManager();
		
		// groupData일때 클론페이지시 연결여부 체크
		boolean isConnectGroupPage = false;
		boolean isLastConnectGroupPage = false;
		// groupData일때 클론페이지시 연결시 인덱스 체크
		int _clonePageCnt = 0;
		// clonePage의 페이지 인덱스값
		int _pageRepeatCnt = 0;
		
		// previewPage여부
		Boolean isPreviewPage = true;
		
//		// Project info send
//		if(bSupportEform)
//		{
//			String _dataSets = JSONObject.toJSONString(mDataSet);
//			formfile.addPageB64(-1, ("{\"project\":" + ((JSONObject) mHashMap.get("project")).toJSONString() + ",\"dataSets\":" + _dataSets +  ",\"pageSize\":" + mPageSize + ",\"resultType\":\"FORM\",\"printOption\":" +  ((JSONObject) mHashMap.get("printOption")).toJSONString() + "}").getBytes("UTF-8"));
//		}
//		else
//		{
//			formfile.addPageB64(-1, ("{\"project\":" + ((JSONObject) mHashMap.get("project")).toJSONString() + ",\"pageSize\":" + mPageSize + ",\"resultType\":\"FORM\",\"printOption\":" +  ((JSONObject) mHashMap.get("printOption")).toJSONString() + "}").getBytes("UTF-8"));
//		}
//		//(HashMap<String,Object>)
//		formfile.addProjectInfo((JSONObject)mHashMap.get("project"));
		//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(this.mServiceReqMng, m_appParams);
		//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(m_appParams);
//		TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser( m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );
//		
//		mFunction.setFunctionVersion(FUNCTION_VERSION);
//		
//		HashMap<String, Object> _pageInfoMap;
//		
//		// 여러건의 Form을 로드하기 위한 분기 @최명진
//		if(documents != null && documents.size() > 0)
//		{
//			_pageInfoMap = _totPageCheckParser.getTotalPageMulti(documentParams, documents, dataSets, mFunction, documentInfos  );
//		}
//		else
//		{
//			 _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
//		}
//		HashMap<String, Object> _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
//		HashMap<String, Object> _pageInfoMap = _totPageCheckParser.getTotalPageMulti(mParam, documents, dataSets, mFunction );
		
		mPageNumList 		= (ArrayList<Integer>) _pageInfoMap.get("PAGE_NUMLIST");
		mPageAr 			= (ArrayList<PageInfoSimple>) _pageInfoMap.get("PAGE_AR");
		mPageNumRealList 	=  (ArrayList<Integer>) _pageInfoMap.get("REAL_PAGE_NUMLIST");
		mTOTAL_PAGE_NUM 	= (Integer) _pageInfoMap.get("TOTALPAGE");
		ArrayList<Object> _pageInfoArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_DATA_LIST"); 
		ArrayList<Object> _pageInfoClassArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_CLASS_LIST"); 
		
		ArrayList<Object> _pageInfoREQList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_REQ_LIST");
		ArrayList<Object> _pageInfoTIDXList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_TIDX_LIST");
		
		ArrayList<Object> _reqlist = new ArrayList<Object>();
		ArrayList<Object> _tabIndexlist = new ArrayList<Object>();
		
		String isExportType = m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE();
		if(isExportType.equals("LOCAL_PRINT"))isExportType = "PRINT";
		mCreateFormFn.setIsExportType(isExportType);
		
		
		int _docTotalPage = mTOTAL_PAGE_NUM;
		
		log.debug("========== VIEWER TOTAL_PAGE : " + mTOTAL_PAGE_NUM );
		
		int _argoDocCnt = 0;
		int _newProjectIdx = 0;
		
		//page Start IDX
		int _startIndex = 0;
		//page End IDX
		int _endIndex = 0;
		int _pageMaxCnt = 0;
		
		int _printS = -1;
		int _printM = -1;
		int _pageStartCnt = 0;
		int _pageMaxinumCnt = 0;
		
		// 페이지 속성의 그룹핑된 데이터가 존재할경우 담겨있는 배열 ( 없을경우 페이지별로 null이 담겨있음 )
		ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _pageSubDataSet = (ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>>) _pageInfoMap.get("TEMP_DATASET_LIST");
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _tempDataSet = null;
		
		if( m_appParams.getREQ_INFO().getSTART_PAGE() != null && m_appParams.getREQ_INFO().getSTART_PAGE().equals("") == false)
		{
			try {
				_printS = Integer.valueOf( m_appParams.getREQ_INFO().getSTART_PAGE() );
			} catch (Exception e) {
				log.debug(getClass().getName() + "::" + "Start Page NumberFormatException");
			}
		}
		
		if(m_appParams.getREQ_INFO().getEND_PAGE() != null && m_appParams.getREQ_INFO().getEND_PAGE().equals("") == false)
		{
			try {
				_printM = Integer.valueOf( m_appParams.getREQ_INFO().getEND_PAGE() );	
			} catch (Exception e) {
				log.debug(getClass().getName() + "::" + "End Page NumberFormatException");
			}
		}
		
		_startIndex = Log.currPageIdx;
		_endIndex = _startIndex + 1;	
		//인쇄 페이지 범위가 있는 경우
		if(!pageRange.equals("1")){
			String [] range = pageRange.split("-");
			if(range.length>1){
				_startIndex = Integer.parseInt(range[0])-1;
				_endIndex = Integer.parseInt(range[1]);		
				isPreviewPage = false;
			}
		}	
		_argoPage = _startIndex;		
		
		int _printTotalPage = _endIndex - _startIndex;
		int _printIndex = 0;
//		if( _printS > -1 )
//		{
//			_startIndex = _printS;
//			_argoPage = _startIndex;
//		}
//		else
//		{
//			_startIndex = 0;
//		}
//		if( _printM > -1 )
//		{
//			if( _endIndex > mTOTAL_PAGE_NUM)
//			{
//				_endIndex = mTOTAL_PAGE_NUM;
//			}
//			else
//			{
//				_endIndex = _printM;
//			}
//		}
//		else 
//		{
//			_endIndex = mTOTAL_PAGE_NUM; 
//		}
//		
//		if(_startIndex > _endIndex)
//		{
//			_startIndex = _endIndex -1;
//		}
		
		
		if( GlobalVariableData.USE_TIME_LOG )
		{
			Date _chkDate = new Date();
			long _chkTime = _chkDate.getTime() -_checkStartTime ;
			
			long chkms = _chkTime % 1000;
			long chkSec = _chkTime / 1000% 60;         
			long chkMin = _chkTime / (60 * 1000)% 60;     
			
			log.info("FORM_PARSING_TIME : " + chkMin +"min " + chkSec + "sec " + chkms + "ms ");
			_checkStartTime = _chkDate.getTime();
		}
		
		ProjectInfo _projectInfo;
		
		float _pagePrintMarginTop 	= 0;
		float _pagePrintMarginLeft 	= 0;
		float _pagePrintMarginRight = 0;
		float _pagePrintMarginBottom = 0;
		
		if( m_appParams.getREQ_INFO().getPAGE_MARGIN_TOP()>0 ) _pagePrintMarginTop = m_appParams.getREQ_INFO().getPAGE_MARGIN_TOP();
		if( m_appParams.getREQ_INFO().getPAGE_MARGIN_LEFT()>0 ) _pagePrintMarginLeft = m_appParams.getREQ_INFO().getPAGE_MARGIN_LEFT();
		if( m_appParams.getREQ_INFO().getPAGE_MARGIN_RIGHT()>0 ) _pagePrintMarginRight = m_appParams.getREQ_INFO().getPAGE_MARGIN_RIGHT();
		if( m_appParams.getREQ_INFO().getPAGE_MARGIN_BOTTOM()>0 ) _pagePrintMarginBottom = m_appParams.getREQ_INFO().getPAGE_MARGIN_BOTTOM();
		
		this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT_START", Log.MSG_LP_START_MAKEPRINT, Log.getMessage(Log.MSG_LP_START_MAKEPRINT)));
		
		// 구간별 시간 확인을 위하여 시간 로그 처리 2  pagecnt = [2,3]  mPageAr =  [ 1page, 2page  ]
		Date _resultTimeStr = null;
		String _BmtLogTitle = "";
		String _heightArrStr = "";
		String _widthArrStr = "";
		
		int pageNumListSize = mPageNumList.size();
		for(int i = 0 ; i < pageNumListSize; i ++){
			if(Log.printStop){
				break;					
			}			
			
			pages.clear();
			
			PageInfoSimple _page = mPageAr.get(i);
			
			_projectInfo = _page.getProjectInfo();
			
			_reportType = Integer.parseInt(_page.getReportType());
			_pageHash = _page.getPageProperty();
			
			_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
			
			// C# Preview에서 반드시 필요한 속성들을 설정한다.
			_pageHash.put("reportType", String.valueOf(_reportType));
			
			
			// Clone 페이지의 경우 width와 height값을 담아두기
			originalPageWidth = pageWidth = _page.getWidth();
			originalPageHeight = pageHeight = _page.getHeight();

			// _pagePrintMargin값이 있을경우 page의 width/height값을 업데이트한다
			if( _pagePrintMarginTop > 0  || _pagePrintMarginBottom > 0 )
			{
				_pageHash.put("height", String.valueOf( pageHeight + _pagePrintMarginTop + _pagePrintMarginBottom ) );
				_pageHash.put("originalHeight", String.valueOf( pageHeight ) );
			}
			if( _pagePrintMarginLeft > 0  || _pagePrintMarginRight > 0 )
			{
				_pageHash.put("width", String.valueOf( pageWidth + _pagePrintMarginLeft + _pagePrintMarginRight ) );
				_pageHash.put("originalWidth", String.valueOf( pageWidth ) );
			}
			
			//전체 페이지의 height를 담아주기 0페이지에만 담도록 한다.
			if( i == 0 )
			{
				_pageHash = getPageHeightList( _pageHash, _pagePrintMarginTop , _pagePrintMarginLeft, _pagePrintMarginRight , _pagePrintMarginBottom ); 
				
				if( _page.getUseGroupPageClone() != null && _page.getUseGroupPageClone().equals("true") )
				{
					_heightArrStr = _pageHash.get("heightList");
					_widthArrStr = _pageHash.get("widthList");
				}
			}
			else if( _argoPage == 0  && isConnectGroupPage && !_heightArrStr.equals("") )
			{
				_pageHash.put("heightList", _heightArrStr);
				_pageHash.put("widthList", _widthArrStr);
			}
			else if( !_heightArrStr.equals("") )
			{
				_heightArrStr = _widthArrStr = "";
			}
			
			if( _page.getUseGroupPageClone()!= null && _page.getUseGroupPageClone().equals("true") )
			{
				isConnectGroupPage = true;
				isLastConnectGroupPage = false;
				
				if( i > 0  && mPageAr.get(i-1).getId().toString().equals(_page.getId().toString()) == false )
				{
					_clonePageCnt = 0;
					//isConnectGroupPage = false;
				}

				if( i == pageNumListSize -1  || mPageAr.get(i+1).getId().toString().equals(_page.getId().toString()) == false )
				{
					isLastConnectGroupPage = true;
				}
			}
			else
			{
				_clonePageCnt = 0;
			}
			
			// Clone Page값 담기
			cloneData = _page.getClone();
			clonePage = false;
			
			// 데이터셋을 매칭
			if( _pageSubDataSet.get(i) == null )
			{
				_tempDataSet = mDataSet;
			}
			else
			{
				_tempDataSet = _pageSubDataSet.get(i);
			}
			
			//JSONObject _backgroundImageObj = ItemPropertyProcess.getPageBackgroundImage(_page, _tempDataSet, mParam, mFunction, oService);
			JSONObject _backgroundImageObj = ItemPropertyProcess.getPageBackgroundImage(_page, _tempDataSet, mParam, mFunction);
			_pageHash.put("backgroundImage", JSONValue.toJSONString(_backgroundImageObj) );
			
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
					_cloneColCnt = _page.getCloneColCount();
					_cloneRowCnt = _page.getCloneRowCount();
				}
				
				if(_page.getDirection()!=null)
				{
					_cloneDirect = _page.getDirection();
				}
				
				pageWidth = pageWidth / _cloneColCnt;
				pageHeight = pageHeight / _cloneRowCnt;
				
				_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
				clonePage = true;
			}
			
			// 파라미터를 페이지별로 셋팅하기 위하여 작업
			if( !mUseMultiFormType && _page.getProjectIndex() != -1 )
			{
				int _DocumentIdx = _page.getProjectIndex();
				mParam = documentParams.get(_DocumentIdx);
				
				if(_pageInfoMap.containsKey("DOCUMENT_TOTAL_PAGE") ) _docTotalPage = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_TOTAL_PAGE")).get(_DocumentIdx);
				if(_pageInfoMap.containsKey("DOCUMENT_START_IDX") ) _argoDocCnt = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_START_IDX")).get(_DocumentIdx);
			}
			
			// Clone Page
			_pageMaxCnt = mPageNumList.get(i);
			
			if(isPreviewPage){
				int orgMaxCnt = mTOTAL_PAGE_NUM ;
				//임시버퍼 데이타 조회 현재 선택한 인텍스의 좌우 5 씩 10데이타 조회
				if(_startIndex < Log.prePageBuff){	
					_endIndex = _startIndex+ Log.prePageBuff;
					_startIndex = 0;
					
				}else{
					_endIndex = _startIndex + (Log.prePageBuff == 0?1:Log.prePageBuff);
					_startIndex = _startIndex - Log.prePageBuff;						
				}
				//최대페이지 넘지 않도록 조정			-- orgMaxCnt 값이 최대페이지 일경우로 제한해야함. 
				if(_endIndex > orgMaxCnt){
					_endIndex = orgMaxCnt;
				}
				//_argoPage = _startIndex;
			}					
			_pageCnt = mPageNumList.get(i);			
			
			
			// START_PAGE ENDPAGE 지정
			if( i > 0 )
			{
				if(isConnectGroupPage)
				{
					int _argoPageCnt = Double.valueOf( Math.floor(_clonePageCnt/_cloneRepCnt) ).intValue();
					if( _argoPageCnt > 0 )
					{
						_startIndex = _startIndex - _argoPageCnt;
						_endIndex = _endIndex - _argoPageCnt;
						
						_clonePageCnt = _clonePageCnt%_cloneRepCnt;
					}
				}
				else
				{
					_startIndex = _startIndex - mPageNumList.get(i-1);
					_endIndex = _endIndex - mPageNumList.get(i-1);
				}
			}
			
			if( _pageMaxCnt > _endIndex )
			{
				_pageMaxCnt = _endIndex;
			}
			
			if( _pageCnt > _endIndex )
			{
				_pageCnt = _endIndex;
			}
			
			if( _startIndex >= mPageNumList.get(i) )
			{
				continue;
			}
			
			if( _startIndex < 0 )
			{
				_startIndex = 0;
			}
			
			if( _endIndex < _startIndex)
			{
				break; 
			}
			
							
			String _command = "MAKEPRINT";
			String _message = "Make Print Page : ";
			if(isPreviewPage){
				_command = "MAKEPRINT_ONE";
				_message = "Make Preview Page : ";
			}else{
				_pageMaxCnt = _endIndex - _startIndex ;
			}
			
			mFunction.setFunctionVersion(FUNCTION_VERSION);
						
			////////////// PAGE 범위 지정 처리 속성 지정 /////////////////////
			
			
			switch (_reportType) {
			case 0: //coverPage
				pageInfoData = new HashMap<String, Object >();
				
				//pageInfoData.put("pageData", mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam , 0 , _docTotalPage , _argoPage - _argoDocCnt) );
				pageInfoData.put("pageData", mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , 0, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt) );
				pageInfoData.put("page", _pageHash);
				
				// Section Page 지정
				mFunction.setSectionCurrentPageNum(0);
				mFunction.setSectionTotalPageNum(1);
				
				mFunction.setCloneIndex( 0 );
				
				pages.put( String.valueOf(_argoPage), pageInfoData);
				
				// 1 Page send to client
				formfile.addPageB64One(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"), isPreviewPage);			
				if(isPreviewPage)this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  (_argoPage + 1) + "/" + _pageMaxCnt));
				else this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  ( 1  + _printIndex++) + "/" + _printTotalPage));				
				
				
				_argoPage = _argoPage + 1;
				
				//20160525 초기화 최명진
				pages.clear();
				pageInfoData = null;
				_objects = null;
				
				break;
			case 1: //freeform
			case 7: //mobile타입
			case 9: //webPage
//				if(isPreviewPage){
//					int orgMaxCnt = _pageMaxCnt;
//					//임시버퍼 데이타 조회 현재 선택한 인텍스의 좌우 5 씩 10데이타 조회
//					if(_startIndex < Log.prePageBuff){	
//						_endIndex = _startIndex+ Log.prePageBuff;
//						_startIndex = 0;
//						
//					}else{
//						_endIndex = _startIndex + (Log.prePageBuff == 0?1:Log.prePageBuff);
//						_startIndex = _startIndex - Log.prePageBuff;						
//					}
//					//최대페이지 넘지 않도록 조정
//					if(_endIndex > orgMaxCnt){
//						_endIndex = orgMaxCnt;
//					}
//					//_argoPage = _startIndex;
//				}					
//				_pageCnt = mPageNumList.get(i);
				
				int _start = 0;
				int _max = 0;

//				for ( j = 0; j < _pageCnt; j++) {
				for ( j = _startIndex; j < _pageCnt; j++) {
					
					if(Log.printStop){
						break;					
					}
					
					pages.clear();
					
					_pageRepeatCnt = (j + _clonePageCnt)%_cloneRepCnt;
					
					// Section Page 지정
					mFunction.setSectionCurrentPageNum(j);
					mFunction.setSectionTotalPageNum(_pageCnt);
					mFunction.setCloneIndex( _pageRepeatCnt );
					if(clonePage)
					{
						_start = j * _cloneRepCnt;
						_max = _start + _cloneRepCnt;
						//마지막 페이지가 max값보다 작을경우 처
						if( _max >= mPageNumRealList.get(i) )
						{
							_max = mPageNumRealList.get(i);
						}
						mFunction.setCloneIndex( 1 );
					}
					else
					{
						_start = j;
						_max = j + 1;
						mFunction.setCloneIndex( 1 );
					}
					
					for (int k = _start; k < _max; k++) {
						if( !clonePage || k%_cloneRepCnt == 0 )
						{
							pageInfoData = new HashMap<String, Object >();
//							_objects.clear();
							_objects = new ArrayList<HashMap<String, Object>>();
						}
						mFunction.setCloneIndex( _pageRepeatCnt );
						if(clonePage && k%_cloneRepCnt > 0 )
						{
							_clonePositionList = ItemConvertParser.getClonePosition( k, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
							cloneX = _clonePositionList.get(0);
							cloneY = _clonePositionList.get(1);
						}
						else
						{
							cloneY = 0;
							cloneX = 0;
						}
						
						// pageConeinue값 체크  Document pageContinue="true", pageCount="3" 을 이용하여 체크
						if( mDocument.getDocumentElement().getAttribute("pageContinue").equals("true") )
						{
							int _pageContinueCnt = Integer.valueOf( String.valueOf( mDocument.getDocumentElement().getAttribute("pageCount") ) );
							int _pageContinuePageDataCnt = (int)  Math.floor( ((i*_pageCnt)+k)/_pageContinueCnt ) ;
							int _pageContinuePageCnt = (int)  ((i*_pageCnt)+k)%_pageContinueCnt;
							if(clonePage)
							{
								_pageContinuePageCnt = (int)  ((i*(_pageCnt*2))+k)%_pageContinueCnt;
								_pageContinuePageDataCnt = (int)  Math.floor( ((i*(_pageCnt*2))+k)/_pageContinueCnt ) ;
							}
							PageInfoSimple _chkPage = mPageAr.get(_pageContinuePageCnt);
							
							_objects = mCreateFormFn.createFreeFormConnect(_chkPage , _tempDataSet , mParam , _pageContinuePageDataCnt, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt);
						}
						else
						{
							_objects = mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , k, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt);
						}
						
					}
					pageInfoData = new HashMap<String, Object >();
//					pageInfoData.put("pageData", mCreateFormFn.CreateFreeFormAll(_page , mDataSet , mParam , j, mTOTAL_PAGE_NUM , _argoPage) );
					pageInfoData.put("pageData", _objects );
					
					_reqlist = new ArrayList<Object>();
					if( _pageInfoREQList.size() > j )
					{
						//_reqlist = (ArrayList<Object>) _pageInfoREQList.get(j);
						_reqlist = (ArrayList<Object>) _pageInfoREQList.get(i);
					}
					
					pageInfoData.put("pageReqData", _reqlist );
					
					_tabIndexlist = new ArrayList<Object>();
					if( _pageInfoTIDXList.size() > j )
					{
//						_tabIndexlist = (ArrayList<Object>) _pageInfoTIDXList.get(j);	// j가 초기화되어 계속 0 값의 index를 가져온다.
						_tabIndexlist = (ArrayList<Object>) _pageInfoTIDXList.get(i);
					}
					
					pageInfoData.put("pageTIdxData", _tabIndexlist );
					
					pageInfoData.put("page", _pageHash);
					
					pages.put( String.valueOf(_argoPage), pageInfoData);

					// 1 Page send to client
					formfile.addPageB64One(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8") ,isPreviewPage);	
					if(isPreviewPage)this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  (_argoPage + 1) + "/" + _pageMaxCnt));
					else this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  ( 1  + _printIndex++) + "/" + _printTotalPage));		
					
					_argoPage = _argoPage + 1;
				}
				
				//20160525 초기화 최명진
				pages.clear();
				pageInfoData = null;
				_objects = null;

				break;
			case 2: //masterBand
 				MasterBandParser masterParser = (MasterBandParser) _pageInfoClassArrayList.get(i);
 				masterParser.setFunction(mFunction);
				ArrayList<Object> masterInfo 					= (ArrayList<Object>) _pageInfoArrayList.get(i);
				int _masterTotPage 								= (Integer) masterInfo.get(0);
				ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
				
				if( _objects == null ) _objects = new ArrayList<HashMap<String,Object>>();
				// masterBand일경우 totalPage 업데이트
				
				_reqlist = new ArrayList<Object>();
				_tabIndexlist = new ArrayList<Object>();
				
				_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
				
				if(  masterInfo.size() > 4 )
				{
					mFunction.setGroupBandCntMap((HashMap<String, Integer>) masterInfo.get(4));
				}
				
				_pageStartCnt   = _startIndex * _cloneRepCnt;
				_pageMaxinumCnt = _pageStartCnt + ( ( _pageMaxCnt - _startIndex ) * _cloneRepCnt);
				if( _pageMaxinumCnt > _masterTotPage ) _pageMaxinumCnt = _masterTotPage;
				
				for ( j = _pageStartCnt; j < _pageMaxinumCnt; j++) {
//				for ( j = 0; j < _masterTotPage; j++) {
					if(Log.printStop){
						break;					
					}
					
					// Section Page 지정
					mFunction.setSectionCurrentPageNum(j);
					mFunction.setSectionTotalPageNum(_masterTotPage);
					
					// clone페이지의 포지션 인덱스값
					_pageRepeatCnt = (j + _clonePageCnt)%_cloneRepCnt;
					
					mFunction.setCloneIndex(_pageRepeatCnt);
					
					if( !clonePage || _pageRepeatCnt == 0 )
					{
						pageInfoData = new HashMap<String, Object >();
						pages.clear();
						_objects.clear();
						
						_reqlist.clear();
						_tabIndexlist.clear();
					}
					
					int k=0;
					if( _pageInfoREQList != null && _pageInfoREQList.size()>i &&  _pageInfoREQList.get(i) != null && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(i)).size() > j)
					{
							ArrayList<HashMap<String, Object>> _pageInfoReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(i)).get(j);
						if(_pageInfoReq != null )
						{
							for ( k = 0; k < _pageInfoReq.size(); k++) {
								_reqlist.add( _pageInfoReq.get(k) );
							}
						}
					}
					if( _pageInfoTIDXList != null && _pageInfoTIDXList.size()>i && _pageInfoTIDXList.get(i) != null && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(i)).size() > j)
					{
						ArrayList<HashMap<String, Object>> _pageTIDXReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(i)).get(j);
						if(_pageTIDXReq != null )
						{
							for ( k = 0; k < _pageTIDXReq.size(); k++) {
								_tabIndexlist.add( _pageTIDXReq.get(k) );
							}
						}
					}
					
					
					if(clonePage && _pageRepeatCnt > 0 )
					{
						_clonePositionList = ItemConvertParser.getClonePosition( _pageRepeatCnt, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
						cloneX = _clonePositionList.get(0);
						cloneY = _clonePositionList.get(1);
					}
					else
					{
						cloneY = 0;
						cloneX = 0;
					}
					
					_objects = masterParser.createMasterBandData(j, masterList, pageWidth, pageHeight, mParam, cloneX, cloneY, _objects , _docTotalPage, _argoPage - _argoDocCnt );
					
					if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1)|| ( !isConnectGroupPage && (j == _masterTotPage-1 )) || (isLastConnectGroupPage && (j == _masterTotPage-1 ) ) )
					{
						pageInfoData.put("pageData", _objects );
						pageInfoData.put("page", _pageHash);
						
						pageInfoData.put("pageReqData", _reqlist );
						pageInfoData.put("pageTIdxData", _tabIndexlist );
						
						pages.put( String.valueOf(_argoPage), pageInfoData);
						
						// 1 Page send to client
						formfile.addPageB64One(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"), isPreviewPage);			
						//formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));	
						this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  (1 + _printIndex++) + "/" + _pageMaxCnt));				
						
						_argoPage = _argoPage + 1;
					}
					
					
				}
//				if( pageInfoData!=null ) pageInfoData.clear();
//				masterInfo.clear();
//				masterList.clear();
//				_objects.clear();
//				masterParser = null;
			
				//20160525 초기화 최명진
				if( !clonePage || ( !isConnectGroupPage || _pageRepeatCnt == (_cloneRepCnt-1) ) )
				{
					pages.clear();
					pageInfoData = null;
					_objects = null;
				}
				
				if(isConnectGroupPage)
				{
					_clonePageCnt = _clonePageCnt + _masterTotPage;
				}
				
				masterInfo = null;
				masterList = null;
				masterParser = null;
				
				break;
			case 3: //continueBand
				ContinueBandParser continueBandParser  = (ContinueBandParser) _page.getPageParserClass();
				continueBandParser.setImageData(this.mImgData);
				continueBandParser.setChartData(this.mChartData);
				continueBandParser.setFunction(mFunction);
				ArrayList<Object> bandAr = (ArrayList<Object>) _pageInfoArrayList.get(i);

				HashMap<String, BandInfoMapDataSimple> bandInfo = (HashMap<String, BandInfoMapDataSimple>) _page.getBandInfoData();
				ArrayList<BandInfoMapDataSimple> bandList =(ArrayList<BandInfoMapDataSimple>) _page.getBandList();
				
				ArrayList<ArrayList<HashMap<String, Object>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Object>>>) bandAr.get(0);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Object>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Object>>> >) bandAr.get(1);
				
				continueBandParser.mOriginalDataMap = _page.getOriginalDataMap();
				continueBandParser.mGroupDataNamesAr = _page.getGroupDataNamesAr();
				
				_reqlist = new ArrayList<Object>();
				_tabIndexlist = new ArrayList<Object>();
				
				if(  _page.getGgroupBandCntMap() != null )
				{
					mFunction.setGroupBandCntMap( _page.getGgroupBandCntMap()  );
				}
				
				boolean isPivot = false;
				
				if( _objects == null )_objects = new ArrayList<HashMap<String,Object>>();
				
				if(_page.getIsPivot().equals("true"))
				{
					isPivot = true;
					// Project info send
					_pageHash.put("height", String.valueOf( _page.getWidth() + _pagePrintMarginLeft + _pagePrintMarginRight  ));
					_pageHash.put("width",  String.valueOf( _page.getHeight() + _pagePrintMarginTop + _pagePrintMarginBottom ));
				}
				
				_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
				
				if( _page.getFitOnePage() )
				{
					continueBandParser.setFitOnePage( _page.getFitOnePage() );
					if(continueBandParser.getFitOnePageHeight() > 0 ) _pageHash.put("height", String.valueOf(continueBandParser.getFitOnePageHeight()) );
				}
				
				int _pagesRowListSize = pagesRowList.size();
				float _bandMaxWidth = 0;
				
				// 페이지 범위 지정
				_pageStartCnt   = _startIndex * _cloneRepCnt;
				_pageMaxinumCnt = _pageStartCnt + ( ( _pageMaxCnt - _startIndex ) * _cloneRepCnt);
				if( _pageMaxinumCnt > _pagesRowListSize ) _pageMaxinumCnt = _pagesRowListSize;
				
				for ( j = _pageStartCnt; j < _pageMaxinumCnt; j++) {
					
					if(Log.printStop){
						break;					
					}
					
					// Section Page 지정
					mFunction.setSectionCurrentPageNum(j);
					mFunction.setSectionTotalPageNum(_pagesRowListSize);
					
					// clone페이지의 포지션 인덱스값
					_pageRepeatCnt = (j + _clonePageCnt)%_cloneRepCnt;
					
					mFunction.setCloneIndex(_pageRepeatCnt);
					
					if( !clonePage || _pageRepeatCnt == 0 )
					{
						pageInfoData = new HashMap<String, Object >();
						pages.clear();
						_objects.clear();
						
						_tabIndexlist.clear();
						_reqlist.clear();
						_bandMaxWidth = 0;
						//_pageHash.put("width", String.valueOf( _page.getWidth()+ _pagePrintMarginLeft + _pagePrintMarginRight ));
						_pageHash.put("width", String.valueOf( (int) ( _page.getWidth()+ _pagePrintMarginLeft + _pagePrintMarginRight) ));
					}
					
					int k=0;
					if(_pageInfoREQList.size()>i && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(i)).size() > j)
					{
						ArrayList<HashMap<String, Object>> _pageInfoReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(i)).get(j);
						if(_pageInfoReq != null )
						{
							for ( k = 0; k < _pageInfoReq.size(); k++) {
								_reqlist.add( _pageInfoReq.get(k) );
							}
						}
					}
					
					if(_pageInfoTIDXList.size()>i && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(i)).size() > j)
					{
						ArrayList<HashMap<String, Object>> _pageTIDXReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(i)).get(j);
						if(_pageTIDXReq != null )
						{
							for ( k = 0; k < _pageTIDXReq.size(); k++) {
								_tabIndexlist.add( _pageTIDXReq.get(k) );
							}
						}
					}
					
					if(clonePage && _pageRepeatCnt > 0 )
					{
						_clonePositionList = ItemConvertParser.getClonePosition( _pageRepeatCnt, _cloneColCnt, _cloneRowCnt, originalPageWidth - mPageMarginLeft- mPageMarginRight, originalPageHeight - mPageMarginTop - mPageMarginBottom , cloneData, _cloneDirect );
						cloneX = _clonePositionList.get(0);
						cloneY = _clonePositionList.get(1);

					}
					else
					{
						cloneY = 0;
						cloneX = 0;
					}

                    if( _pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
					if( _pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;
					
					_objects = continueBandParser.createContinueBandItemsSimple(j, _tempDataSet, bandInfo, bandList, pagesRowList, mParam, crossTabData,cloneX,cloneY,_objects,_docTotalPage, _argoPage - _argoDocCnt , isPivot);
					
					_bandMaxWidth = _bandMaxWidth + continueBandParser.getBandMaxWidth();
					
					if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) || ( !isConnectGroupPage && (j == _pagesRowListSize-1 ) )  || (isLastConnectGroupPage && (j == _pagesRowListSize-1 )) )
					{
						if( Float.valueOf( _pageHash.get("width") ) < _bandMaxWidth )
						{
							_pageHash.put("width", Float.valueOf(_bandMaxWidth ).toString() );
						}
						
						_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
						
						pageInfoData.put("pageData", _objects );
						pageInfoData.put("page", _pageHash);
						
						pageInfoData.put("pageReqData", _reqlist );
						pageInfoData.put("pageTIdxData", _tabIndexlist );
						
						pages.put( String.valueOf(_argoPage), pageInfoData);
					
						
						formfile.addPageB64One(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"), isPreviewPage);			
						if(isPreviewPage)this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  (_argoPage + 1) + "/" + _pageMaxCnt));
						else this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  ( 1  + _printIndex++) + "/" + _printTotalPage));
						_argoPage = _argoPage + 1;					
					}
				}
						
				if( !clonePage || ( !isConnectGroupPage || _pageRepeatCnt == (_cloneRepCnt-1) ) )
				{
					//20160525 초기화 최명진
					pageInfoData = null;
					_objects.clear();
					//pagesRowList.clear();
					pages.clear();
				}
				
				if(isConnectGroupPage)
				{
					_clonePageCnt = _clonePageCnt + _pagesRowListSize;
				}
//				continueBandParser = null;
//				bandAr = null;
//				bandInfo = null;
//				bandList = null;
//				crossTabData = null;
				
				break;
			case 4: //labelBand
				_pageCnt = mPageNumList.get(i);
				for ( j = _startIndex; j < _pageMaxCnt; j++) {
//				for ( j = 0; j < _pageCnt; j++) {
					if(Log.printStop){
						break;					
					}
					
					// Section Page 지정
					mFunction.setSectionCurrentPageNum(j);
					mFunction.setSectionTotalPageNum(_pageCnt);
					
					mFunction.setCloneIndex(0);
					
					pages.clear();
					
					pageInfoData = new HashMap<String, Object >();
					//TEST
					pageInfoData.put("pageData", mCreateFormFn.CreateLabelBandAll(_page , _tempDataSet , mParam ,j,_docTotalPage , _argoPage - _argoDocCnt, cloneX, cloneY ) );
					pageInfoData.put("page", _pageHash);
					
					pages.put( String.valueOf(_argoPage), pageInfoData);
					
					// 1 Page send to client
					formfile.addPageB64One(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"), isPreviewPage);			
					//formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));						
					this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  (_argoPage+1) + "/" + _pageMaxCnt));				
					
					if(j < _pageCnt-1)
					{
						_argoPage = _argoPage + 1;
					}
				}
				
				//20160525 초기화 최명진
				pages.clear();
				pageInfoData = null;
				masterInfo = null;
				masterList = null;
				_objects = null;
				masterParser = null;

				break;
			case 8: //lastPage
				_objects = new ArrayList<HashMap<String, Object>>();
				
				cloneX = (_pagePrintMarginLeft > 0)?_pagePrintMarginLeft:0;
				cloneY = (_pagePrintMarginTop > 0)?_pagePrintMarginTop:0;

				
				pageInfoData.put("pageData", mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , 0, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt) );
				pageInfoData.put("page", _pageHash);

				pages.put( String.valueOf(_argoPage), pageInfoData);
				
				// Section Page 지정
				mFunction.setSectionCurrentPageNum(0);
				mFunction.setSectionTotalPageNum(1);
				mFunction.setCloneIndex(0);
				
				// 1 Page send to client
				formfile.addPageB64One(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"), isPreviewPage);			
				//formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));
				this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  (_argoPage+1) + "/" + _pageMaxCnt));				
				
				_argoPage = _argoPage + 1;
				
				//20160525 초기화 최명진
				pages.clear();
				pageInfoData = null;
				_objects = null;
				
				break;
/**			case 12:	//ConnectLink
				// connectLink타입일경우  
//				ConnectLinkParser connectLink = new ConnectLinkParser(m_appParams);
//				connectLink.setFunction(mFunction);
				
				ConnectLinkParser connectLink = (ConnectLinkParser) _pageInfoClassArrayList.get(i);
				connectLink.setFunction(mFunction);
				
//				HashMap<String, Object> connectData = connectLink.loadPagesData(_page, mParam);
				HashMap<String, Object> connectData =  (HashMap<String, Object>) _pageInfoArrayList.get(i);
				int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
				// totalPage를 담아두고 각 페이지별로 for문을 돌면서 page를 리턴받기
				
//				mTOTAL_PAGE_NUM = mTOTAL_PAGE_NUM + _connectTotPage;
				
				_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
				
				for ( j = _startIndex; j < _pageMaxCnt; j++) {
//				for ( j = 0; j < _connectTotPage; j++) {
					if(Log.printStop){
						break;					
					}
					
					pages.clear();
					_objects = new ArrayList<HashMap<String,Object>>();
					connectLink.makeConnectPage(j, connectData.get("pageInfoData"), _objects,_docTotalPage, _argoPage - _argoDocCnt );
					
					pageInfoData = new HashMap<String, Object >();  
					pageInfoData.put("pageData", _objects );
					pageInfoData.put("page", _pageHash);
					pages.put( String.valueOf(_argoPage), pageInfoData);
					
					// 1 Page send to client
					formfile.addPageB64One(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"), isPreviewPage);
					//formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));
					this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  (_argoPage+1) + "/" + _pageMaxCnt));				
					
					_argoPage = _argoPage + 1;
				}
				
				//20160525 초기화 최명진
				connectLink = null;
				pages.clear();
				pageInfoData = null;
				_objects = null;
				connectData = null;
				break;
**/				
			case 14:
				// Linked Project type 문서 타입
//				LinkedPageParser linkedParser = new LinkedPageParser(m_appParams);
				
				LinkedPageParser linkedParser = (LinkedPageParser) _pageInfoClassArrayList.get(i);
				linkedParser.setFunction(mFunction);
				
//				HashMap<String, Object> linkedData = linkedParser.loadTotalPage(mPageAr,mDataSet );
				HashMap<String, Object> linkedData =   (HashMap<String, Object>) _pageInfoArrayList.get(i);
				int _totPage = (Integer) linkedData.get("totPage");
				ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
				HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
				
				cloneData = _page.getDivide();
				clonePage = false;
				
				// Clone 여부 확인
				_cloneColCnt = 1;
				_cloneRowCnt = 1;
				_cloneRepCnt = 1;
				_cloneDirect = "";	// cloneDirect Across Down: 가로, Down Across : 세로

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

					if(_page.getDirection() !=null && _page.getDirection().equals("") == false )
					{
					  _cloneDirect = _page.getDirection();
					}

					pageWidth = pageWidth / _cloneColCnt;
					pageHeight = pageHeight / _cloneRowCnt;

					_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
					clonePage = true;
				}
				
				_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
				
				for ( j = _startIndex; j < _pageMaxCnt; j++) {
//				for ( j = 0; j < _totPage; j++) {
					if(Log.printStop){
						break;					
					}
					
					// Section Page 지정
					mFunction.setSectionCurrentPageNum(j);
					mFunction.setSectionTotalPageNum(_totPage);
					mFunction.setCloneIndex(j%_cloneRepCnt);
					if( !clonePage || j%_cloneRepCnt == 0 )
					{
						pageInfoData = new HashMap<String, Object >();
						pages.clear();
						_objects = new ArrayList<HashMap<String,Object>>();
					}
					
					if(clonePage && j%_cloneRepCnt > 0 )
					{
						  _clonePositionList = ItemConvertParser.getClonePosition( j, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
						  cloneX = _clonePositionList.get(0);
						  cloneY = _clonePositionList.get(1);
					}
					else
					{
						cloneY = 0;
						cloneX = 0;
					}
					
					_objects = linkedParser.createLinkedPageItem(j, retArr, newDataSet, mParam, cloneX, cloneY, _objects, _docTotalPage,_argoPage - _argoDocCnt );
					
					if( !clonePage || j%_cloneRepCnt == (_cloneRepCnt-1) || (j == _totPage-1) )
					{
						pageInfoData.put("pageData", _objects );
						pageInfoData.put("page", _pageHash);
						
						pages.put( String.valueOf(_argoPage), pageInfoData);
						
						// 1 Page send to client
						formfile.addPageB64One(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"), isPreviewPage);
						//formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));
						this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", _command, "99999", _message +  (_argoPage+1) + "/" + _pageMaxCnt));				
						
						_argoPage = _argoPage + 1;
					}
					
					//log.debug(getClass().getName() + "::" + "Continue Doc  "  + j);
				}

				// LinkedForm의 경우 한페이지로 모든 페이지생성이 완료됨
				i = mPageNumList.size();
				
				linkedParser = null;
				linkedData = null;
				
				//20160525 초기화 최명진
				pages.clear();
				pageInfoData = null;
				_objects = null;
				retArr = null;
				newDataSet = null;
				
				break;
				
/**			case 16:
				RepeatedFormParser _repeatFormParser = (RepeatedFormParser)  _pageInfoClassArrayList.get(i);
				
				HashMap<String, Object> _repeatInfoData = (HashMap<String, Object>) _pageInfoArrayList.get(i);
				
				_reqlist = new ArrayList<Object>();
				_tabIndexlist = new ArrayList<Object>();
				
				if( _objects == null )_objects = new ArrayList<HashMap<String,Object>>();
				
				_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
				
				int _pageTotLength = Integer.valueOf( _repeatInfoData.get("TOTAL_PAGE").toString() );
				
				// 페이지 범위 지정
				_pageStartCnt   = _startIndex * _cloneRepCnt;
				_pageMaxinumCnt = _pageStartCnt + ( ( _pageMaxCnt - _startIndex ) * _cloneRepCnt);
				if( _pageMaxinumCnt > _pageTotLength ) _pageMaxinumCnt = _pageTotLength;
				
				for ( j = _pageStartCnt; j < _pageMaxinumCnt; j++) {
					// Section Page 지정
					mFunction.setSectionCurrentPageNum(j);
					mFunction.setSectionTotalPageNum(_pageTotLength);
					
					// clone페이지의 포지션 인덱스값
					_pageRepeatCnt = (j + _clonePageCnt)%_cloneRepCnt;
					
					mFunction.setCloneIndex(_pageRepeatCnt);
					
					if( !clonePage || _pageRepeatCnt == 0 )
					{
						pageInfoData = new HashMap<String, Object >();
						pages.clear();
						_objects.clear();
						
						_tabIndexlist.clear();
						_reqlist.clear();
						_bandMaxWidth = 0;
						_pageHash.put("width", _page.getAttribute("width"));
					}
					
//					int k=0;
//					if(_pageInfoREQList.size()>i && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(i)).size() > j)
//					{
//						ArrayList<HashMap<String, Object>> _pageInfoReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(i)).get(j);
//						if(_pageInfoReq != null )
//						{
//							for ( k = 0; k < _pageInfoReq.size(); k++) {
//								_reqlist.add( _pageInfoReq.get(k) );
//							}
//						}
//					}
//					
//					if(_pageInfoTIDXList.size()>i && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(i)).size() > j)
//					{
//						ArrayList<HashMap<String, Object>> _pageTIDXReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(i)).get(j);
//						if(_pageTIDXReq != null )
//						{
//							for ( k = 0; k < _pageTIDXReq.size(); k++) {
//								_tabIndexlist.add( _pageTIDXReq.get(k) );
//							}
//						}
//					}
					
					if(clonePage && _pageRepeatCnt > 0 )
					{
						_clonePositionList = ItemConvertParser.getClonePosition( _pageRepeatCnt, _cloneColCnt, _cloneRowCnt, originalPageWidth - mPageMarginLeft- mPageMarginRight, originalPageHeight - mPageMarginTop - mPageMarginBottom , cloneData, _cloneDirect );
						cloneX = _clonePositionList.get(0);
						cloneY = _clonePositionList.get(1);

					}
					else
					{
						cloneY = 0;
						cloneX = 0;
					}
					
                    if( _pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
					if( _pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;

					_objects = _repeatFormParser.createRepeatFormItems(j, _repeatInfoData, originalPageWidth, originalPageHeight, mParam, cloneX, cloneY, _objects, _docTotalPage, _argoPage - _argoDocCnt );
					
					
					if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) || ( !isConnectGroupPage && (j == _pageMaxinumCnt-1 ) )  || (isLastConnectGroupPage && (j == _pageMaxinumCnt-1 )) )
					{
						pageInfoData.put("pageData", _objects );
						pageInfoData.put("page", _pageHash);
						
						pageInfoData.put("pageReqData", _reqlist );
						pageInfoData.put("pageTIdxData", _tabIndexlist );
						
						pages.put( String.valueOf(_argoPage), pageInfoData);
						_argoPage = _argoPage + 1;

						// 1 Page send to client
						formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));
						this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT", "99999", "Make Print Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));				
					}
				}
				
				if( !clonePage || ( !isConnectGroupPage || _pageRepeatCnt == (_cloneRepCnt-1) ) )
				{
					//20160525 초기화 최명진
					pageInfoData = null;
					_objects.clear();
					pages.clear();
				}
				
				if(isConnectGroupPage)
				{
					_clonePageCnt = _clonePageCnt + _pageMaxinumCnt;
				}
				continueBandParser = null;
				bandAr = null;
				bandInfo = null;
				bandList = null;
				crossTabData = null;
				
				
				break;
**/				
				
			default:
				
				break;
			}
		}
		if(!Log.printStop){
			
			WriteChartData( this.mChartData );
			WriteImageData( this.mImgData );				
		}		
		
		//변수 clear			
		//_pageInfoMap.clear();
		//mPageAr.clear();
		//mPageNumList.clear();
		//mPageNumRealList.clear();
		//_pageInfoArrayList = null;
		//_pageInfoClassArrayList = null;
		//mDataSet = null;
		
		
		Date _chkDate = new Date();  		
		log.debug("[" + _client_ssid + "] FORM PARSING COMPLETE  : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_chkDate) +"]" );
		
		if(!Log.printStop){	
			this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT_END", Log.MSG_LP_END_MAKEPRINT, Log.getMessage(Log.MSG_LP_END_MAKEPRINT)));
		}
		
		// 페이지들의 배열이 담긴 결과값 리턴
		//mHashMap.put("pageDatas", hashmapToJsonStr(pages) );
		//oService.end();
	}
	
	
	
	
	
	
	private ArrayList<ProjectInfo> mProjectInfos;
	@Override
	protected void xmlToDocument(String _xml) throws SAXException, IOException,
			ParserConfigurationException {
		
		String _exportType = m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE();
		
		mProjectInfos = new ArrayList<ProjectInfo>();
		ProjectInfo _projectInfo;
    	File _jsonFile;
    	int docSize = documents != null ? documents.size() : 0;
    	int formNamesSize = mFormNames != null ? mFormNames.size() : 0;
    	if(docSize > 0 )
    	{
    		for(int i = 0; i < docSize; i++ )
    		{
    			//_jsonFile = new File(documents.get(i));
    			//_projectInfo = new ProjectInfo(documentParams.get(i), _jsonFile, _exportType, GlobalVariableData.M_FILE_LOAD_TYPE_SIMPLE);
    			String _jsonStr = documents.get(i);
    			_projectInfo = new ProjectInfo(documentParams.get(i), _jsonStr, _exportType, GlobalVariableData.M_FILE_LOAD_TYPE_SIMPLE);
    			//_projectInfo.setServiceReqMng( this.mServiceReqMng );
    			_projectInfo.setProjectIndex(i);
    			if(formNamesSize > i )
    			{
    				_projectInfo.setProjectName(mFormNames.get(i).get("PROJECT_NAME"));
    				_projectInfo.setFormName(mFormNames.get(i).get("FORM_NAME"));
    			}
    			
    			mProjectInfos.add(_projectInfo);
    		}
    	}
    	else
    	{
			//_jsonFile = new File( _xml );
			//_projectInfo = new ProjectInfo(mParam, _jsonFile, _exportType, GlobalVariableData.M_FILE_LOAD_TYPE_SIMPLE);
			_projectInfo = new ProjectInfo(mParam, _xml, _exportType, GlobalVariableData.M_FILE_LOAD_TYPE_SIMPLE);
			//_projectInfo.setServiceReqMng( this.mServiceReqMng );
			_projectInfo.setProjectIndex(0);
			mProjectInfos.add(_projectInfo);
			
			if(formNamesSize > 0)
			{
				_projectInfo.setProjectName(mFormNames.get(0).get("PROJECT_NAME"));
				_projectInfo.setFormName(mFormNames.get(0).get("FORM_NAME"));
			}
			
			documentParams = new ArrayList<JSONObject>();
			documentParams.add(mParam);
    	}
    	
    	_projectInfo = mProjectInfos.get(0);
    	
		JSONObject projectHm = new JSONObject();
		projectHm = _projectInfo.getProjectMap();

		Log.pageFontUnit = _projectInfo.getFontUnit();
		
		
		if( _projectInfo.getClientMode() != null ){
			CLIENT_EDIT_MODE = _projectInfo.getClientMode().toUpperCase();
			projectHm.put("clientEditMode", CLIENT_EDIT_MODE);
		}
		
		if( _projectInfo.getFnVersion() != null ){
			FUNCTION_VERSION = _projectInfo.getFnVersion();
		}
		
		// ISPIVOT
		// page속성의 isPivot속성이 있을경우 처리
		PageInfoSimple _page =  _projectInfo.getPage(0);
		
		if( _page.getIsPivot().equals("true") )
		{
			projectHm.put("pageWidth", _page.getHeight() );
			projectHm.put("pageHeight", _page.getWidth() );
		}
		
		if( _projectInfo != null ){
			if( _projectInfo.getWaterMarkMap() != null )
			{
				projectHm.put("WATER_MARK", _projectInfo.getWaterMarkMap() );
			}
			
			if("SIMPLE".equals(_projectInfo.getType()))
			{
				projectHm.put("pageWidth", _page.getHeight() );
				projectHm.put("pageHeight", _page.getWidth() );
			}
		}
		
		HashMap<String, Object> _pagegroupInfo = _projectInfo.getPageGroup();
		boolean _useFileSplit = false;
		String _downLoadFileName = "";
		
		if( _pagegroupInfo != null && !_pagegroupInfo.isEmpty() )
		{
			_useFileSplit =_pagegroupInfo.get("useFileSplit").toString().equals("true");
			_downLoadFileName = _pagegroupInfo.get("downLoadFileName").toString();
			
			if(_useFileSplit && _downLoadFileName.equals(""))
			{
				_useFileSplit = false;
			}
		}
		
		projectHm.put("USE_FILE_SPLIT", _useFileSplit);
		
		// margin지정 
		if( _projectInfo.getMarginTop() > 0 ){
			mPageMarginTop = _projectInfo.getMarginTop();
		}
		if( _projectInfo.getMarginLeft() > 0){
			mPageMarginLeft = _projectInfo.getMarginLeft();
		}
		if( _projectInfo.getMarginRight() > 0){
			mPageMarginRight = _projectInfo.getMarginRight();
		}
		if( _projectInfo.getMarginBottom() > 0 ){
			mPageMarginBottom = _projectInfo.getMarginBottom();
		}
	
/*		
		try {
			ArrayList<String> _userAuthList = common.getUserFileAuthority( m_appParams.getREQ_INFO().getLOGIN_ID() );
			if( _userAuthList != null ) projectHm.put("downloadAuthList", _userAuthList);
		} catch (SQLException e) {
//			e.printStackTrace();
		}
*/		
		mHashMap.put("project", projectHm);
		
		//TEMPLET 생성하여 담아두기 
//		mTempletInfo = TempletItemInfo.checkTempletItem( mDocument , mParam, this.mServiceReqMng, m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE() );
		
	}
	
	
	protected void xmlToDataSet() throws Exception {
		// TODO Auto-generated method stub
		
		int _docSize = mProjectInfos.size();
		ProjectInfo _pj;
		DataSetInfo _dsInfo;
		
		for (int i = 0; i < _docSize; i++) {
			marDataSetItems.clear();
			mDataSetParam.clear();
			
			_pj = mProjectInfos.get(i);
			
			mParam = documentParams.get(i);
			_pj.setParam(mParam);
			
			ArrayList<DataSetInfo> _dsList =  _pj.getDataSets();
			marDataSetItems.clear();
			
			int leng = (_dsList != null)?_dsList.size():0;
			int j = 0;
			DataSetInfo _tmpDS;
			
			HashMap<String, DataSetInfo> _dataSetItems = new HashMap<String, DataSetInfo>();
			HashMap<String, ArrayList<HashMap<String,String>> > _mergedDs = new HashMap<String, ArrayList<HashMap<String,String>>>();
			
			for( j = 0 ; j < leng; j++ )
			{
				_tmpDS = _dsList.get(j);
				_dataSetItems.put(_tmpDS.getId(), _tmpDS);
				
				if( _tmpDS.getMergedInfoList() != null && _tmpDS.getMergedInfoList().size() > 0 )
				{
					_mergedDs.put( _tmpDS.getId() , _tmpDS.getMergedInfoList());
				}
			}
			
			mDataSetParam = paramCheck_HandlerJson( _pj, mDataSetParam );
			
			if( mDataSetParam == null )
			{
				mParamResult = true;
				return;
			}
			else if( !mParamResult )
			{
				mDataSet = new HashMap< String, ArrayList<HashMap<String, Object>> >();
				//DataServiceManager oService = (this.mServiceReqMng != null)?this.mServiceReqMng.getServiceManager():null;	// null 처리 2016-03-10 최명진
				//mDataSet = mDataSetFn.dataSetLoadJson(oService, _dataSetItems , mDataSetParam , _mergedDs, CLIENT_EDIT_MODE);	
				mDataSet = mDataSetFn.dataSetLoadJson(mSERVER_URL, _dataSetItems , mDataSetParam , _mergedDs, CLIENT_EDIT_MODE);	
				
			}
			
			_pj.setDataSet(mDataSet);
		}
		
		
	}
	
	
	protected HashMap<String, HashMap<String, String>> paramCheck_HandlerJson(ProjectInfo _projectInfo,  HashMap<String, HashMap<String, String>> _param ) throws UnsupportedEncodingException
	{
		// param Check; 
		HashMap<String, HashMap<String, String>> _paramMap = new HashMap<String, HashMap<String, String>>();
		if(_param != null ) _paramMap = _param;
		
		HashMap<String, String> _paramProp = null;
		String _paramID = "";
		
		_paramMap = _projectInfo.getParameter();
		
		int _size = mProjectInfos.size();
		for( int i=0; i < _size; i++ )
		{
			TempletItemInfo.getTempletDataParam( mProjectInfos.get(i).getTempletInfo() , _paramMap);
		}
		
		if( mUseParameterPopup && _paramMap.size() != 0 && checkSystemParam(mParam) )
		{
			mHashMap.put("resultType","PARAM");
			mHashMap.put("param", _paramMap);
			return null;
		}
		else
		{
			if( _paramMap.size() != 0)
			{
				for(String key : _paramMap.keySet())
				{
					if( mParam.containsKey(key))
					{
						HashMap<String, String> _paValue = (HashMap<String, String>) mParam.get(key);
						
						if( mIsOpenParamPopup )
						{
							_paValue.put("parameterDefault", (_paramMap.containsKey(key))?_paramMap.get(key).get("parameterDefault"):"");
						}
						_paramMap.put(key, _paValue );
					}
				}
			}
			//else
			if( mParam.size() != 0)
			{
				for(Object key : mParam.keySet())
				{
					if( mParam.containsKey(key))
					{
						if( isSystemParameter( key.toString() ) == false )
						{
							HashMap<String, String> _paValue = (HashMap<String, String>) mParam.get(key);
							
							if( mIsOpenParamPopup )
							{
								if(_paramMap.containsKey(key))
								{
									_paValue.put("parameterDefault", (_paramMap.containsKey(key))?_paramMap.get(key).get("parameterDefault"):"");
									_paramMap.put((String) key, _paValue );
								}
							}
							else
							{
								_paramMap.put((String) key, _paValue );
							}
						}
					}
				}
			}
			
			if( mUseParameterPopup && mIsOpenParamPopup && _paramMap.size() > 0 )
			{
				mHashMap.put("resultType","PARAM");
				mHashMap.put("param", _paramMap);
				return null;
			}
		}

		return _paramMap;
		// param Check;
	}
	
	private HashMap<String, String> getPageHeightList( HashMap<String, String> _pageHash, float _marginTop, float _marginLeft, float _marginRight, float _marginBottom )
	{
		JSONArray _heightArr = new JSONArray();
		JSONArray _widthArr = new JSONArray();
		float _height = 0;
		float _width = 0;
		float _h = 0;
		PageInfoSimple _page;
		String cloneData;
		int _cloneRowCnt = 1;
		int pageNumListSize = mPageNumList.size();
		int pageNumRealListSize = mPageNumRealList.size();
		for(int i = 0; i < pageNumListSize; i++ )
		{
			int _pageCnt = mPageNumList.get(i);
			_height = mPageAr.get(i).getHeight();
			_width = mPageAr.get(i).getWidth();
			_h = 0; 
			_page = mPageAr.get(i);
			
			if( _page.getFitOnePage() && Integer.parseInt( _page.getReportType() ) == 3 )
			{
				_h = ((ContinueBandParser) mPageAr.get(i).getPageParserClass()).getFitOnePageHeight();
				
				if( _h > 0 ) _height = _h;
			}
			
			cloneData = _page.getClone();
			
			if( cloneData.equals(GlobalVariableData.CLONE_PAGE_NORMAL) == false && _page.getUseGroupPageClone() != null && _page.getUseGroupPageClone().equals("true") )
			{
				if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
				{
					_cloneRowCnt = 2;
				}
				else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM) )
				{
					_cloneRowCnt = _page.getCloneRowCount();
				}
				else
				{
					_cloneRowCnt = 1;
				}
				_pageCnt = 0;
				
				for( int k=i; k < pageNumRealListSize; k++ )
				{
					if( mPageAr.get(k).getId().equals( _page.getId() ) == false )
					{
						break;
					}
					else
					{
						_pageCnt = _pageCnt + mPageNumRealList.get(k);
						i = k;
					}
				}
				
//				_pageCnt = Double.valueOf( Math.ceil( _pageCnt / _cloneRowCnt ) ).intValue();
				_pageCnt = Double.valueOf( Math.ceil( Integer.valueOf(_pageCnt).doubleValue() / Integer.valueOf(_cloneRowCnt).doubleValue() ) ).intValue();
				
			}
			
			
			for( int j=0; j < _pageCnt; j++ )
			{
				_heightArr.add(_height + _marginTop + _marginBottom );
				_widthArr.add(_width + _marginLeft + _marginRight );
			}
		}
		
		_pageHash.put("heightList", _heightArr.toJSONString());
		_pageHash.put("widthList", _widthArr.toJSONString());
		
		return _pageHash;
	}
	
	
	private ArrayList<ArrayList<HashMap<String,Object>>> getExportAllPagesJS( Boolean isExport, HashMap<String, Object> _pInfo, boolean bSupportIE9, HashMap<String, Object> _changeItemList ) throws UnsupportedEncodingException , Exception
	{
		mHashMap.put("resultType","FORM");
		int _reportType = 0;
		int _argoPage = 0;
		
		int i = 0;
		int j = 0;
		int k = 0;
		int ca = 0;
		int caMax = 1;
		
		int _printS = -1;
		int _printM = -1;
		
		boolean printMode = false;
		// parameter값 중에서 인쇄 포지션 관련 파라미터가 존재할경우 처리
		float UBIPRINTPOSITIONX = 0;
		float UBIPRINTPOSITIONX2 = 0;
		float UBIPRINTPOSITIONY = 0;
		float UBIPRINTPOSITIONY2 = 0;

		boolean isExcelExport = ( isExport && _ubFormToExcel != null )? true : false;
		boolean isPPTExport = ( isExport && _ubFormToPPT != null )? true : false;
		boolean isWordExport = ( isExport && _ubFormToWord != null )? true : false;
//		boolean isHTMLExport = ( isExport && _ubFormToHTML != null )? true : false;
		boolean isTEXTExport = ( isExport && _ubformToText != null )? true : false;
		
		String _client_ssid = m_appParams.getREQ_INFO().getCLIENT_SESSION_ID();	
		String _PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
		String _FORM_ID = m_appParams.getREQ_INFO().getFORM_ID();
		String _CLIENT_IP = m_appParams.getREQ_INFO().getCLIENT_IP();
		
		boolean mUseFileSplit =  m_appParams.getREQ_INFO().getUSE_FILE_SPLIT();
		
		String _exportType = "";
		String _excelOption = "";
		String _html_option = "";
		
		Date start = new Date();
		Date end = null;
		int _argoDocCnt = 0;
		int _newProjectIdx = 0;
		
		log.debug("getExportAllPages Start ");
		
		_exportType = m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE();
		_excelOption = m_appParams.getREQ_INFO().getEXCEL_OPTION();
		_html_option = m_appParams.getREQ_INFO().getHTML_OPTION();
		if(m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE().equals("PRINT"))
		{
			if( m_appParams.getREQ_INFO().getCIRCULATION() != null && m_appParams.getREQ_INFO().getCIRCULATION().equals("") == false)
			{
				try {
					caMax = Integer.valueOf( m_appParams.getREQ_INFO().getCIRCULATION() );
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
				
			printMode = true;
			
			if(mParam.containsKey(GlobalVariableData.UB_PRINT_POSITION_X))
			{
				HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_PRINT_POSITION_X);
				UBIPRINTPOSITIONX = Float.valueOf( _pList.get("parameter") );
				UBIPRINTPOSITIONX2 = UBIPRINTPOSITIONX;
			}
			if(mParam.containsKey(GlobalVariableData.UB_PRINT_POSITION_X2))
			{
				HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_PRINT_POSITION_X2);
				UBIPRINTPOSITIONX2 = Float.valueOf( _pList.get("parameter") );
			}
			if(mParam.containsKey(GlobalVariableData.UB_PRINT_POSITION_Y))
			{
				HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_PRINT_POSITION_Y);
				UBIPRINTPOSITIONY = Float.valueOf( _pList.get("parameter") );
				UBIPRINTPOSITIONY2 = UBIPRINTPOSITIONY;
			}
			if(mParam.containsKey(GlobalVariableData.UB_PRINT_POSITION_Y2))
			{
				HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_PRINT_POSITION_Y2);
				UBIPRINTPOSITIONY2 = Float.valueOf( _pList.get("parameter") );
			}
			
		}
		
		if( m_appParams.getREQ_INFO().getSTART_PAGE() != null && m_appParams.getREQ_INFO().getSTART_PAGE().equals("") == false)
		{
			try {
				_printS = Integer.valueOf( m_appParams.getREQ_INFO().getSTART_PAGE() );
			} catch (Exception e) {
				log.debug(getClass().getName() + "::" + "Start Page NumberFormatException");
			}
			
			if( isExcelExport ) _ubFormToExcel.setSTART_PAGE(_printS);
			if( isPPTExport ) _ubFormToPPT.setSTART_PAGE(_printS);
			if( isWordExport ) _ubFormToWord.setSTART_PAGE(_printS);
		}
		
		if(m_appParams.getREQ_INFO().getEND_PAGE() != null && m_appParams.getREQ_INFO().getEND_PAGE().equals("") == false)
		{
			try {
				_printM = Integer.valueOf( m_appParams.getREQ_INFO().getEND_PAGE() );	
			} catch (Exception e) {
				log.debug(getClass().getName() + "::" + "End Page NumberFormatException");
			}
		}
		
		// 모든 페이지의 총 페이지수를 구하고 각 페이지별 화면을 그리기 위한 객체를 담아두기
		HashMap<String, Object> _pageInfoMap = null;
		TotalPageCheckParser _totPageCheckParser = null;
		
		mFunction.setFunctionVersion(FUNCTION_VERSION);
		mCreateFormFn.setTempletInfo(mTempletInfo);
		
		if(_pInfo == null)
		{
			_totPageCheckParser = new TotalPageCheckParser( m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );
			_totPageCheckParser.setIsExport(_exportType);
			//TEMPLET 처리를 위해 추가
			_totPageCheckParser.setTempletInfo(mTempletInfo);
//			_totPageCheckParser.setExportFileName(mExportFileName);
			_totPageCheckParser.setUseFileSplit(mUseFileSplit);

			_pageInfoMap = _totPageCheckParser.getTotalPageSimple(documentParams, mProjectInfos, mFunction );
			
			if( mPDFProtectionInfo == null || mPDFProtectionInfo.isEmpty() ) mPDFProtectionInfo = _totPageCheckParser.getPdfProtectionInfo();
		}
		else
		{
			_pageInfoMap = _pInfo;
		}
		
		
		if( _pageInfoMap.containsKey("TOTALPAGE") ) log.debug("EXPORT  TOTAL_PAGE_CNT : " + _pageInfoMap.get("TOTALPAGE") );
		
		mPageNumList 		= (ArrayList<Integer>) _pageInfoMap.get("PAGE_NUMLIST");
		mPageAr 			= (ArrayList<PageInfoSimple>) _pageInfoMap.get("PAGE_AR");
		mPageNumRealList 	=  (ArrayList<Integer>) _pageInfoMap.get("REAL_PAGE_NUMLIST");
		mTOTAL_PAGE_NUM 	= (Integer) _pageInfoMap.get("TOTALPAGE");
		ArrayList<Object> _pageInfoArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_DATA_LIST"); 
		ArrayList<Object> _pageInfoClassArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_CLASS_LIST"); 
		ArrayList<Object> _pageInfoREQList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_REQ_LIST");
		ArrayList<Object> _pageInfoTIDXList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_TIDX_LIST");
		
		// PageGroup 옵션 
		ArrayList<String> _downLoadFileNames = (ArrayList<String>) _pageInfoMap.get("DOWNLOAD_FILE_NAMES");
		
		boolean _useFileSplit = (_pageInfoMap!=null&&_pageInfoMap.containsKey("USE_SPLIT_FILE"))? _pageInfoMap.get("USE_SPLIT_FILE").toString().equals("true"):false;
//		boolean _useFileSplit = _pageInfoMap.get("USE_SPLIT_FILE").toString().equals("true");
		
		ArrayList<Object> _reqlist = new ArrayList<Object>();
		ArrayList<Object> _tabIndexlist = new ArrayList<Object>();

		// 페이지 속성의 그룹핑된 데이터가 존재할경우 담겨있는 배열 ( 없을경우 페이지별로 null이 담겨있음 )
		ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _pageSubDataSet = (ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>>) _pageInfoMap.get("TEMP_DATASET_LIST");

		HashMap<String, ArrayList<HashMap<String, Object>>> _tempDataSet = null;
		
		HashMap<String, String> _pageHash = new HashMap<String, String>();
		HashMap< String, HashMap<String, Object> > pages = new HashMap< String, HashMap<String, Object> >();
		HashMap<String, Object > pageInfoData = null;
		
		// export 에 사용할 아이템 리스트. 2015-10-22 공혜지.
		ArrayList<ArrayList<HashMap<String, Object>>> pagesForExport = new ArrayList<ArrayList<HashMap<String,Object>>>();
		// item ArrayList 를 저장할 변수. 2015-10-22 공혜지.
		ArrayList<HashMap<String, Object>> pageObj = null;
		// 엑셀 y값 계산을 위하여 받아온 아이템 정보에 이전 페이지 높이를 추가.
		int beforePageHeight = 0;
		
//		DataServiceManager oService = (this.mServiceReqMng != null)? this.mServiceReqMng.getServiceManager():null;
		
		String cloneData = "";
		float pageWidth = 0;
		float pageHeight = 0;
		
		int _docTotalPage = mTOTAL_PAGE_NUM;

		float originalPageWidth = 0;
		float originalPageHeight = 0;
		ArrayList<Float> _clonePositionList = null;
		  
		float cloneX = 0;
		float cloneY = 0;
		ArrayList<HashMap<String, Object>> _objects = null;
		boolean clonePage = false;
		
		// groupData일때 클론페이지시 연결여부 체크
		boolean isConnectGroupPage = false;
		boolean isLastConnectGroupPage = false;
		// groupData일때 클론페이지시 연결시 인덱스 체크
		int _clonePageCnt = 0;
		// clonePage의 페이지 인덱스값
		int _pageRepeatCnt = 0;
		String _formNameStr = "";
		String _sheetName = "";
		// Water Mark
		_formNameStr = mFormNames != null ? mFormNames.get(0).get("FORM_NAME") : "";
		String _waterMark = mProjectInfos.get(0).getWaterMark();
		
		HashMap<String, Object> pageProp = new HashMap<String, Object>();
		
		// 모든 페이지의 총 페이지수를 구하고 각 페이지별 화면을 그리기 위한 객체를 담아두기
		int mTOTAL_PAGE_NUM  = (Integer) _pageInfoMap.get("TOTALPAGE");
		
		String mChartDataFileName = "chartdata.dat";
		ItemConvertParser mItemConvertFn = new ItemConvertParser(null, mChartDataFileName, m_appParams);
		
		mItemConvertFn.setChangeItemList(_changeItemList);
		
		mItemConvertFn.setIsExportType(_exportType);
		
		mCreateFormFn.setChangeItemList(_changeItemList);
		
		//Excel의 경우 이미지 사용여부를 담아둘 속성을 셋팅
		if( isExcelExport )
		{
			String _excelIncludeImage = mProjectInfos.get(0).getExcelIncludeImage();
			_ubFormToExcel.setExcelIncludeImage(!_excelIncludeImage.equals("false"));
			
			String _excelUsePageHeight = mProjectInfos.get(0).getExcelUsePageHeight();
			_ubFormToExcel.setExcelUsePageHeight(!_excelUsePageHeight.equals("false"));
		}
		
		if( _ubFormToPDF != null && !_useFileSplit )
		{
			_ubFormToPDF.chageSplitFilePath();
		}
		else if( _ubFormToExcel != null && !_useFileSplit )
		{
			_ubFormToExcel.chageSplitFilePath();
		}
		
		if( _ubFormToExcel != null )
		{
			ArrayList<ArrayList<Integer>> _docXArr =  _ubFormToExcel.getDocumentXArrayJS(mProjectInfos, _pageInfoMap );
			_ubFormToExcel.setDcoXArray( _docXArr );
		}
			
		
		int _startIndex = 0;
		int _endIndex = 0;
		int _lastIndex = 0;
		int _pageMaxCnt = 0;
		
		int _pageStartCnt = 0;
		int _pageMaxinumCnt = 0;
		
		// Export시 처리
		mCreateFormFn.setIsExportType(_exportType);
		
		String _formName = "";
		
		int _DocumentIdx = 0;
		int _argoDocumentIdx = 0;
		boolean _isNextDocument = false;
		if(mDataSet != null)
		{
			int _printCurrentPageIndex = 0;
			
			for ( ca = 0; ca < caMax; ca++) {
				
				
				if( _printS > -1 )
				{
					_startIndex = _printS;
				}
				else
				{
					_startIndex = 0;
				}
				if( _printM > 0 )
				{
					if( _endIndex > mTOTAL_PAGE_NUM)
					{
						_endIndex = mTOTAL_PAGE_NUM;
					}
					else
					{
						_endIndex = _printM;
					}
				}
				else 
				{
					_endIndex = mTOTAL_PAGE_NUM; 
				}
				
				_lastIndex = _endIndex;
				
				if(_startIndex >= _endIndex)
				{
					_startIndex = _endIndex -1;
				}
				
				_argoPage = _startIndex;
				String _downFileName = "";
				int pageNumListSize = mPageNumList.size();
				for( i = 0 ; i < pageNumListSize; i ++){
					
					PageInfoSimple _page = mPageAr.get(i);
					// 데이터셋을 매칭
					_tempDataSet = _page.getDataSet();
					
					_pageHash = new HashMap<String, String>();
					_pageHash.put("pageWidth", String.valueOf( _page.getWidth() ) );
					_pageHash.put("pageHeight",String.valueOf(  _page.getHeight() ) );
					
					//JSONObject _backgroundImageObj = ItemPropertyProcess.getPageBackgroundImage(_page, _tempDataSet, mParam, mFunction, oService );
					JSONObject _backgroundImageObj = ItemPropertyProcess.getPageBackgroundImage(_page, _tempDataSet, mParam, mFunction );
					
					if( _downLoadFileNames != null && _downLoadFileNames.size() > i )
					{
						_downFileName = _downLoadFileNames.get(i);
					}
					else
					{
						_downFileName = "";
					}
					
					// Export시 Background Image의 경우 PDF를 제외하고 나머지는 각각의 Class에 전달한다
					if( isExcelExport ){
						if( _downFileName.equals("") == false )
						{
							if( wb != null && i > 0 )
							{
								FileOutputStream out = new FileOutputStream( _ubFormToExcel.getExcelFileName() );
								wb.write(out);
								out.close();
								
								if(wb instanceof SXSSFWorkbook)
								{
									((SXSSFWorkbook) wb).dispose();
								}
								wb = null;
								_ubFormToExcel.sheet = null;
								_ubFormToExcel.clearStyleTable();
								
							}
							_ubFormToExcel.setSplitFileName(_downFileName);
						}
						
						_ubFormToExcel.setBackgroundImage(_backgroundImageObj);
					}
					else if( isPPTExport ){
						_ubFormToPPT.setBackgroundImage(_backgroundImageObj);
						_ubFormToPPT.setBackgroundColor(_page.getBackgroundColorInt());
					}
					else if( isWordExport ){
						_ubFormToWord.setBackgroundImage(_backgroundImageObj);
						_ubFormToWord.setBackgroundColor( _page.getBackgroundColorInt() );
					}
					else if( _ubFormToPDF != null )
					{
						if( _ubFormToPDF.isStarting() || _downFileName.equals("") == false )
						{
							
							if( _downFileName.equals("") == false )
							{
								if( !_ubFormToPDF.isStarting() )
								{
									_ubFormToPDF.getPdfByteData();
								}
								
								_ubFormToPDF.setSplitFileName(_downFileName);
							}
							_ubFormToPDF.xmlParsingpdfStart(mHashMap);
						}
						
						_pageHash.put("backgroundImage", JSONValue.toJSONString(_backgroundImageObj) );
						_ubFormToPDF.setBackgroundColor(_page.getBackgroundColorInt());
					}else if("HWP".equals(_exportType)){
						_ubFormToHwp.setBackgroundImage(_backgroundImageObj);
						
						_pageHash.put("backgroundColor", String.valueOf( _page.getBackgroundColorInt() ) );
						_pageHash.put("backgroundAlpha", String.valueOf( _page.getBackgroundAlpha() ) );
					}					
					
					if( _argoDocumentIdx != _page.getProjectIndex() )
					{
						_isNextDocument = true;
					}
					else
					{
						_isNextDocument = false;		// 프로젝트가 이전페이지와 다름을 담아두는 Boolean값
					}
					_formName = _page.getProjectInfo().getFormName();
					_argoDocumentIdx = _page.getProjectIndex();
					
					if( _page.getProjectIndex() != -1)
					{
						_DocumentIdx = _page.getProjectIndex();
						mParam = documentParams.get(_DocumentIdx);
						
						if(_pageInfoMap.containsKey("DOCUMENT_TOTAL_PAGE") ) _docTotalPage = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_TOTAL_PAGE")).get(_DocumentIdx);
						if(_pageInfoMap.containsKey("DOCUMENT_START_IDX") ) _argoDocCnt = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_START_IDX")).get(_DocumentIdx);
						
						mParam = documentParams.get(_DocumentIdx);
						
					}
					else
					{
						_sheetName = _formNameStr;
						_docTotalPage = mTOTAL_PAGE_NUM;
					}
					
					_sheetName = _page.getProjectInfo().getFormName();
					
					// page속성의 isPivot속성이 있을경우 처리
					HashMap<String, String> _pjMap = (HashMap<String, String>)  mHashMap.get("project");
					if( _page.getIsPivot() != null &&  _page.getIsPivot().equals("true") )
					{
						_pjMap.put("pageWidth", String.valueOf( _page.getHeight() ));
						_pjMap.put("pageHeight",String.valueOf( _page.getWidth()  ));
					}
					else
					{
						_pjMap.put("pageWidth", String.valueOf(_page.getWidth()  ));
						_pjMap.put("pageHeight", String.valueOf(_page.getHeight()));
					}
					
					// GroupData 사용시 Clone연속보기를 설정시 처리
					if( _page.getUseGroupPageClone() != null && _page.getUseGroupPageClone().equals("true") )
					{
						isConnectGroupPage = true;
						isLastConnectGroupPage = false;
						
						if( i > 0  && mPageAr.get(i-1).getId().equals( _page.getId() ) == false )
						{
							_clonePageCnt = 0;
							//isConnectGroupPage = false;
						}

						if( i == pageNumListSize -1  || mPageAr.get(i+1).getId().equals(_page.getId()) == false )
						{
							isLastConnectGroupPage = true;
						}
					}
					else
					{
						_clonePageCnt = 0;
					}
					
					_reportType = Integer.parseInt(_page.getReportType());
					cloneData = _page.getClone();
					
					clonePage = false;
					
					// Clone 페이지 일경우 Width/Height값을 나누워서 담는다
					pageWidth = _page.getWidth();
					pageHeight = _page.getHeight();
					
					originalPageWidth = pageWidth;
					originalPageHeight = pageHeight;
					
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
						  _cloneColCnt = _page.getCloneColCount();
						  _cloneRowCnt = _page.getCloneRowCount();
						}
	
						if( _page.getDirection() != null && _page.getDirection().equals("") == false )
						{
						  _cloneDirect = _page.getDirection();
						}
	
						pageWidth = pageWidth / _cloneColCnt;
						pageHeight = pageHeight / _cloneRowCnt;
	
						_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
						clonePage = true;
					}
					
					
					if( i > 0 )
					{
						if(isConnectGroupPage)
						{
							int _argoPageCnt = Double.valueOf( Math.floor(_clonePageCnt/_cloneRepCnt) ).intValue();
							if( _argoPageCnt > 0 )
							{
								_startIndex = _startIndex - _argoPageCnt;
								_endIndex = _endIndex - _argoPageCnt;
								
								_clonePageCnt = _clonePageCnt%_cloneRepCnt;
							}
						}
						else
						{
							_startIndex = _startIndex - mPageNumList.get(i-1);
							_endIndex = _endIndex - mPageNumList.get(i-1);
						}
					}
					
					_pageMaxCnt = mPageNumList.get(i);
					
					if( _pageMaxCnt > _endIndex )
					{
						_pageMaxCnt = _endIndex;
					}
					
					if( pageNumListSize-1 == i )
					{
						isConnectGroupPage = false;
					}
					
					if( _startIndex >= mPageNumList.get(i) )
					{
						continue;
					}
					
					if( _startIndex < 0 )
					{
						_startIndex = 0;
					}
					
					if( _endIndex < _startIndex)
					{
						break; 
					}
					
					int _minimumResizeFontSize = 0;
					if( _page.getMinimumResizeFontSize() > 0 )
					{
						_minimumResizeFontSize = _page.getMinimumResizeFontSize();
						mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
					}
					
					float _pageExportWidth = _page.getWidth();
					float _pageExportHeight = _page.getHeight();
					
					if( "WORD".equals(_exportType) )
					{
						if(  _page.getWidth() < _page.getHeight() )
						{
							if(  _page.getWidth() > 794)
							{
								_pageExportWidth = 794;
								_pageExportHeight = 1123;
							}
						}
						else
						{
							if( _page.getHeight() > 794)
							{
								_pageExportWidth = 1123;
								_pageExportHeight = 794;
							}
						}
						
						if(_ubFormToWord != null) _ubFormToWord.setPageSize( _pageExportWidth, _pageExportHeight );
					}

					//Excel일경우 
					if( isExport && isExcelExport && _ubFormToExcel != null  )
					{
						// Excel 옵션이 BAND일경우 타입이 continueBand일경우에만 BAND로 지정이 필요.
						if( _excelOption != null && _excelOption.equals("BAND") )
						{
							if(_reportType == 3) _ubFormToExcel.setIsExcelOption(_excelOption);
							else  _ubFormToExcel.setIsExcelOption("NORMAL");
						}
						
						_ubFormToExcel.setSheetName( getSheetName(_sheetName, _tempDataSet, mParam ) );
					}
					
					// item ArrayList 받아오는 부분을 변수(pageObj)로 처리하고, isExport 인 경우에 사용하도록 변경. 2015-10-22 공혜지.
					switch (_reportType) {
					case 0: //coverPage
						
						// Section Page 지정
						mFunction.setSectionCurrentPageNum(0);
						mFunction.setSectionTotalPageNum(1);
						mFunction.setCloneIndex(0);
						
						_objects = new ArrayList<HashMap<String, Object>>();
						_objects = mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , 0, UBIPRINTPOSITIONX, UBIPRINTPOSITIONY, _objects, _docTotalPage , _argoPage - _argoDocCnt );
						
						mCreateFormFn.setFunction(mFunction);
						
						_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
						
						pageInfoData = new HashMap<String, Object >();
						pageInfoData.put("pageData", _objects);
						pageInfoData.put("page", _pageHash);
						
						
						if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
						
						if( isExport ){
							pageProp.clear();
							pageProp.put("cPHeight", _pageExportHeight);
							pageProp.put("cPWidth", _pageExportWidth);
							
							pageProp.put("bPageHeight", beforePageHeight);
							beforePageHeight += pageHeight;
							_objects.add(pageProp);
							pagesForExport.add(_objects);
							
							if( isExcelExport ){
								wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, 0, _formName, _isNextDocument, _DocumentIdx);
								pagesForExport.clear();
								Thread.sleep(10);
							}
							else if( isPPTExport ){
								ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, 0);
								pagesForExport.clear();
								Thread.sleep(10);
							}
							else if( isWordExport ){
								if( _lastIndex == _argoPage)
								{
									_ubFormToWord.setIsLast(true);
								}
								else
								{
									_ubFormToWord.setIsLast(false);
								}
								
								resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, 0);
								pagesForExport.clear();
								Thread.sleep(10);
							}
							else if( _ubFormToPDF != null )
							{
								_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
								pagesForExport.clear();
							}
							else if( isTEXTExport )
							{
								_ubformToText.xmlPasingtoText(pagesForExport);
								pagesForExport.clear();
							}
						}
						
						_argoPage++;
						_printCurrentPageIndex = _printCurrentPageIndex + 1;
						
						break;
					case 1: //freeform
					case 7: //mobile타입
					case 9: //webPage
						int _start = 0;
						int _max = 0;
						
						for ( j = _startIndex; j < _pageMaxCnt; j++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(_pageMaxCnt);
							
							if(clonePage)
							{
								_start = j*_cloneRepCnt;
								_max = _start + _cloneRepCnt;
								//마지막 페이지가 max값보다 작을경우 처
								if( _max >= mPageNumRealList.get(i) )
								{
									_max = mPageNumRealList.get(i);
								}
							}
							else
							{
								_start = j;
								_max = j + 1;
							}
							// pdf일때 페이지 open처리 
							if( isExport && _ubFormToPDF != null ){
								_ubFormToPDF.toPdfPageOpen(_pageHash);
							}
							
							for ( k = _start; k < _max; k++) {
								
								if( !clonePage || k%_cloneRepCnt == 0 )
								{
									pageInfoData = new HashMap<String, Object >();
		//							_objects.clear();
									_objects = new ArrayList<HashMap<String, Object>>();
								}
								
								_pageRepeatCnt = (k + _clonePageCnt)%_cloneRepCnt;
								mFunction.setCloneIndex( _pageRepeatCnt );
								if(clonePage && k%_cloneRepCnt > 0 )
								{
									_clonePositionList = ItemConvertParser.getClonePosition( k, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
									cloneX = _clonePositionList.get(0);
									cloneY = _clonePositionList.get(1);
									
									cloneX = cloneX;
									cloneY = cloneY;
									
									cloneX = cloneX + UBIPRINTPOSITIONX2;
									cloneY = cloneY + UBIPRINTPOSITIONY2;
								}
								else
								{
									cloneY = 0;
									cloneX = 0;
									
									cloneX = cloneX + UBIPRINTPOSITIONX;
									cloneY = cloneY + UBIPRINTPOSITIONY;
								}
								
								// pageConeinue값 체크  Document pageContinue="true", pageCount="3" 을 이용하여 체크
								if( _page.getProjectInfo().getPageContinue().equals("true") )
								{
									int _pageCnt = mPageNumList.get(i);
									int _pageContinueCnt = _page.getProjectInfo().getPageList().size();
									int _pageContinuePageDataCnt = (int)  Math.floor( ((i*_pageCnt)+k)/_pageContinueCnt ) ;
									int _pageContinuePageCnt = (int)  ((i*_pageCnt)+k)%_pageContinueCnt;
									if(clonePage)
									{
										_pageContinuePageCnt = (int)  ((i*(_pageCnt*2))+k)%_pageContinueCnt;
										_pageContinuePageDataCnt = (int)  Math.floor( ((i*(_pageCnt*2))+k)/_pageContinueCnt ) ;
									}
									PageInfoSimple _chkPage = mPageAr.get(_pageContinuePageCnt); 
									
									if( isExport && _ubFormToPDF != null )_objects = mCreateFormFn.createFreeFormConnectPdf(_chkPage , _tempDataSet , mParam , _pageContinuePageDataCnt, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt, _ubFormToPDF, _argoPage-1 );
									else _objects = mCreateFormFn.createFreeFormConnect(_chkPage , _tempDataSet , mParam , _pageContinuePageDataCnt, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt );
								}
								else
								{
									if( isExport && _ubFormToPDF != null )_objects = mCreateFormFn.createFreeFormConnectPdf(_page , _tempDataSet , mParam , k, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt, _ubFormToPDF, _argoPage-1 ); 
									else _objects = mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , k, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt );
								}
								
								_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
								
								mCreateFormFn.setFunction(mFunction);
							}
							
							pageInfoData = new HashMap<String, Object >();
							pageInfoData.put("pageData", _objects);
							pageInfoData.put("page", _pageHash);
							
							if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
							
							
							
							_argoPage = _argoPage + 1;
							
							_printCurrentPageIndex = _printCurrentPageIndex + 1;
							
							// pdf일때 페이지 close처리 
							// pdf일때 페이지 open처리 
							if( isExport && _ubFormToPDF != null ){
								
								_ubFormToPDF.toPdfPageClose();
								pagesForExport.clear();
								
							}else if( isExport && _ubFormToPDF == null ){
								pageProp.clear();
								pageProp.put("cPHeight", _pageExportHeight);
								pageProp.put("cPWidth", _pageExportWidth);
								pageProp.put("backgroundColor", _page.getBackgroundColor());
								
								pageProp.put("bPageHeight", beforePageHeight);
								beforePageHeight += pageHeight;
								_objects.add(pageProp);
								pagesForExport.add(_objects);
								if( isExcelExport ){
									wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, _formName, _isNextDocument, _DocumentIdx);
									pagesForExport.clear();
									_isNextDocument = false;
									Thread.sleep(10);
								}
								else if( isPPTExport ){
									ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
									pagesForExport.clear();
									_isNextDocument = false;
									Thread.sleep(10);
								}
								else if( isWordExport ){
									
									if( _lastIndex == _argoPage)
									{
										_ubFormToWord.setIsLast(true);
									}
									else
									{
										_ubFormToWord.setIsLast(false);
									}
									
									resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( _ubFormToPDF != null )
								{
									_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
									pagesForExport.clear();
								}
								else if( isTEXTExport )
								{
									_ubformToText.xmlPasingtoText(pagesForExport);
									pagesForExport.clear();
								}
								
							}
							
						}
						
						break;
					case 2: //masterBand
						
						MasterBandParser masterParser 	= (MasterBandParser) _page.getPageParserClass();
						ArrayList<Object> masterInfo 	= (ArrayList<Object>) _pageInfoArrayList.get(i);
						int _masterTotPage 				= (Integer) masterInfo.get(0);
						ArrayList<Object> masterList 	= (ArrayList<Object>) masterInfo.get(1);
						
						masterParser.setImageData(mImgData);
						masterParser.setChartData(mChartData);
						masterParser.setFunction(mFunction);
						masterParser.setChangeItemList(_changeItemList);
						
						if(  masterInfo.size() > 4 )
						{
							mFunction.setGroupBandCntMap((HashMap<String, Integer>) masterInfo.get(4));
						}
						//Export시 ExportType를 담아두기
						masterParser.setIsExportType(_exportType);
						
						ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
						if( _objects == null ) _objects = new ArrayList<HashMap<String,Object>>();
						
						_pageStartCnt   = _startIndex * _cloneRepCnt;
						_pageMaxinumCnt = _pageStartCnt + ( ( _pageMaxCnt - _startIndex ) * _cloneRepCnt);
						if( _pageMaxinumCnt > _masterTotPage ) _pageMaxinumCnt = _masterTotPage;
						
						for ( j = _pageStartCnt; j < _pageMaxinumCnt; j++) {
							
							// clone페이지의 포지션 인덱스값
					        _pageRepeatCnt = (j + _clonePageCnt)%_cloneRepCnt;

							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(_masterTotPage);
							
							mFunction.setCloneIndex(_pageRepeatCnt);
							
							if( !clonePage || _pageRepeatCnt == 0 )
							{
								pageInfoData = new HashMap<String, Object >();
								_objects = new ArrayList<HashMap<String, Object>>();
							}
							
							if(clonePage && _pageRepeatCnt > 0 )
							{
								_clonePositionList = ItemConvertParser.getClonePosition( _pageRepeatCnt, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
								cloneX = _clonePositionList.get(0);
								cloneY = _clonePositionList.get(1);
								
								cloneX = cloneX + UBIPRINTPOSITIONX2;
								cloneY = cloneY + UBIPRINTPOSITIONY2;
							}
							else
							{
								cloneY = 0;
								cloneX = 0;
								
								cloneX = cloneX + UBIPRINTPOSITIONX;
								cloneY = cloneY + UBIPRINTPOSITIONY;
							}
							_objects = masterParser.createMasterBandDataSimple(j, _page, masterList, pageWidth, pageHeight, mParam, cloneX, cloneY, _objects,_docTotalPage,  _argoPage - _argoDocCnt );
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
							
							if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) || ( !isConnectGroupPage && (j == _masterTotPage-1 )) || ( isLastConnectGroupPage && (j == _masterTotPage-1 ))  )
							{
								pageInfoData.put("pageData", _objects );
								pageInfoData.put("page", _pageHash);
								
								if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
								
								_argoPage = _argoPage + 1;
								
								_printCurrentPageIndex = _printCurrentPageIndex + 1;
								
								if( isExport ){
									pageProp.clear();
									pageProp.put("cPHeight", _pageExportHeight);
									pageProp.put("cPWidth", _pageExportWidth);
									
									pageProp.put("bPageHeight", beforePageHeight);
									beforePageHeight += pageHeight;
									_objects.add(pageProp);
									pagesForExport.add(_objects);
									if( isExcelExport ){
										wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, _formName, _isNextDocument, _DocumentIdx);
										pagesForExport.clear();
										_isNextDocument = false;
										Thread.sleep(10);
									}
									else if( isPPTExport ){
										ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
										pagesForExport.clear();
										_isNextDocument = false;
										Thread.sleep(10);
									}
									else if( isWordExport ){
										
										if( _lastIndex == _argoPage)
										{
											_ubFormToWord.setIsLast(true);
										}
										else
										{
											_ubFormToWord.setIsLast(false);
										}
										
										resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
										pagesForExport.clear();
										Thread.sleep(10);
									}
									else if( _ubFormToPDF != null )
									{
										_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
										pagesForExport.clear();
									}
									else if( isTEXTExport )
									{
										_ubformToText.xmlPasingtoText(pagesForExport);
										pagesForExport.clear();
									}
								}
							}
							
						}

						if(isConnectGroupPage)
						{
						  _clonePageCnt = _clonePageCnt + _masterTotPage;
						}

						break;
					case 3: //continueBand 
						
						ContinueBandParser continueBandParser = (ContinueBandParser) _page.getPageParserClass();
						ArrayList<Object> bandAr 					= (ArrayList<Object>) _pageInfoArrayList.get(i);
						
						continueBandParser.setImageData(mImgData);
						continueBandParser.setChartData(mChartData);
						continueBandParser.setFunction(mFunction);
						continueBandParser.setChangeItemList(_changeItemList);
						
						//Export시 ExportType를 담아두기
						continueBandParser.setIsExportType(_exportType);
						//Excel 내보내기시 Excel Option을 담아두기 ( NORMAL / BAND )
						continueBandParser.setIsExcelOption(_excelOption);
						
						if(  bandAr.size() > 8 )
						{
							mFunction.setGroupBandCntMap((HashMap<String, Integer>) bandAr.get(8));
						}
						
						HashMap<String, BandInfoMapDataSimple> bandInfo = (HashMap<String, BandInfoMapDataSimple>) _page.getBandInfoData();
						ArrayList<BandInfoMapDataSimple> bandList =(ArrayList<BandInfoMapDataSimple>) _page.getBandList();
						
						ArrayList<ArrayList<HashMap<String, Object>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Object>>>) bandAr.get(0);
						HashMap<String, ArrayList<ArrayList<HashMap<String, Object>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Object>>> >) bandAr.get(1);
						
						continueBandParser.mOriginalDataMap = _page.getOriginalDataMap();
						continueBandParser.mGroupDataNamesAr = _page.getGroupDataNamesAr();
						
						boolean isPivot = false;
						
						if(_page.getIsPivot().equals("true"))
						{
							isPivot = true;
						}
						
						if( _page.getFitOnePage() )
						{
							continueBandParser.setFitOnePage( _page.getFitOnePage() );
							if( continueBandParser.getFitOnePageHeight() > 0 )
							{
								_pageHash.put("height", String.valueOf(continueBandParser.getFitOnePageHeight()) );
							}
						}
						
						int _pagesRowListSize = pagesRowList.size();
						_pageStartCnt   = _startIndex * _cloneRepCnt;
						_pageMaxinumCnt = _pageStartCnt + ( ( _pageMaxCnt - _startIndex ) * _cloneRepCnt);
						if( _pageMaxinumCnt > _pagesRowListSize ) _pageMaxinumCnt = _pagesRowListSize;
						
						float _bandMaxWidth = 0;
						String htmlBand = "";
						for ( j = _pageStartCnt; j < _pageMaxinumCnt; j++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(_pagesRowListSize);
							

							// clone페이지의 포지션 인덱스값
							_pageRepeatCnt = (j + _clonePageCnt)%_cloneRepCnt;
							
							mFunction.setCloneIndex(_pageRepeatCnt);
							
							if( !clonePage || _pageRepeatCnt == 0 )
							{
								_objects = new ArrayList<HashMap<String,Object>>();
								pageInfoData = new HashMap<String, Object >();
								
								_bandMaxWidth = 0;
								_pageHash.put("width", String.valueOf( _page.getWidth() ) );
								
								// pdf일때 페이지 open처리 
								if( isExport && _ubFormToPDF != null ){
									_ubFormToPDF.toPdfPageOpen(_pageHash);
	                            }
								
							}
							
							if(clonePage && _pageRepeatCnt > 0 )
							{
								_clonePositionList = ItemConvertParser.getClonePosition( _pageRepeatCnt, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
								cloneX = _clonePositionList.get(0);
								cloneY = _clonePositionList.get(1);
								
								cloneX = cloneX + UBIPRINTPOSITIONX2;
								cloneY = cloneY + UBIPRINTPOSITIONY2;
							}
							else
							{
								cloneX = 0;
								cloneY = 0;
								
								cloneX = cloneX + UBIPRINTPOSITIONX;
								cloneY = cloneY + UBIPRINTPOSITIONY;
							}
							
							/** */
							// Excel Band형태 내보내기 처리를 위해 사용 
							if( isExport && isExcelExport && _excelOption != null && _excelOption.equals("BAND") )
							{
								//@@처리 예정
								// Excel 내보내기 기능 사용시
								// TEST @@ CMJ
//								_objects = continueBandParser.createContinueBandItemsExcelList( null, j, _tempDataSet, bandInfo, bandList, pagesRowList, mParam, crossTabData,cloneX,cloneY,_objects,_docTotalPage,  _argoPage - _argoDocCnt , isPivot, _ubFormToExcel, wb, _formName, true, _DocumentIdx);
							}
							else
							{
								_objects = continueBandParser.createContinueBandItemsSimplePDF(j, _tempDataSet, bandInfo, bandList, pagesRowList, mParam, crossTabData,cloneX,cloneY,_objects,
										_docTotalPage,  _argoPage - _argoDocCnt , isPivot, _ubFormToPDF, _argoPage-1 );
							}
							
							_bandMaxWidth = _bandMaxWidth + continueBandParser.getBandMaxWidth();
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
							
							if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) || ( !isConnectGroupPage && (j == pagesRowList.size()-1)) || ( isLastConnectGroupPage && (j ==  pagesRowList.size() -1 )) )
							{
								Object _widthObj = _pageHash.get("width");
								if( _widthObj == null && _pageHash.get("pageWidth") != null ) _widthObj = _pageHash.get("pageWidth");
								
								if(_widthObj != null && Float.valueOf( _widthObj.toString() ) < _bandMaxWidth )
								{
									_pageHash.put("width", Float.valueOf(_bandMaxWidth ).toString() );
								}
								
								pageInfoData.put("pageData", _objects );
								pageInfoData.put("page", _pageHash);
								
								if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
								_argoPage = _argoPage + 1;
								
								_printCurrentPageIndex = _printCurrentPageIndex + 1;
								
	                            // pdf일때 페이지 close처리 
								// pdf일때 페이지 open처리 
								if( isExport && _ubFormToPDF != null ){
									_ubFormToPDF.toPdfPageClose();
									pagesForExport.clear();
									
								}else if( isExport ){
									pageProp.clear();

									pageProp.put("cPHeight", _pageExportHeight);
									pageProp.put("cPWidth", _pageExportWidth);
									
									pageProp.put("backgroundColor", _page.getBackgroundColor());
									
									pageProp.put("bPageHeight", beforePageHeight);
									beforePageHeight += pageHeight;
									_objects.add(pageProp);
									pagesForExport.add(_objects);
									if( isExcelExport && ( _excelOption == null || _excelOption.equals("BAND") == false ) ){
										wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, _formName, _isNextDocument, _DocumentIdx);
										pagesForExport.clear();
										_isNextDocument = false;
										Thread.sleep(10);
									}
									else if( isPPTExport ){
										ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
										pagesForExport.clear();
										_isNextDocument = false;
										Thread.sleep(10);
									}
									else if( isWordExport ){
										if( _lastIndex == _argoPage)
										{
											_ubFormToWord.setIsLast(true);
										}
										else
										{
											_ubFormToWord.setIsLast(false);
										}
										
										resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
										pagesForExport.clear();
										Thread.sleep(10);
									}
									else if( _ubFormToPDF != null )
									{
										_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
										pagesForExport.clear();
									}
									else if( isTEXTExport )
									{
										_ubformToText.xmlPasingtoText(pagesForExport);
										pagesForExport.clear();
									}
								}
								
							}
							
						}

						if(isConnectGroupPage)
						{
						  _clonePageCnt = _clonePageCnt + _pagesRowListSize;
						}
						
						break;
					case 4: //labelBand
						for ( j = _startIndex; j < _pageMaxCnt; j++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(_pageMaxCnt);
							
							mFunction.setCloneIndex(0);
							
							cloneX = 0;
							cloneY = 0;
							pageObj = new ArrayList<HashMap<String,Object>>();
							pageInfoData = new HashMap<String, Object >();
							
							cloneX = cloneX + UBIPRINTPOSITIONX;
							cloneY = cloneY + UBIPRINTPOSITIONY;
							
							mCreateFormFn.setFunction(mFunction);
//							pageObj = mCreateFormFn.CreateLabelBandConnect(_page , _tempDataSet , mParam,j, cloneX, cloneY, pageObj ,_docTotalPage , _argoPage - _argoDocCnt );
							// TEST @@ CMJ
//							pageObj = mCreateFormFn.CreateLabelBandAll(_page , _tempDataSet , mParam ,j,_docTotalPage , _argoPage - _argoDocCnt );
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn, pageObj, _page, _exportType, _html_option);
							
							pageInfoData.put("pageData", pageObj);
							pageInfoData.put("page", _pageHash);
							if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
							
							_argoPage = _argoPage + 1;
							
							_printCurrentPageIndex = _printCurrentPageIndex + 1;
							
							if( isExport ){
								pageProp.clear();
								pageProp.put("cPHeight", _pageExportHeight);
								pageProp.put("cPWidth", _pageExportWidth);
								
								pageProp.put("bPageHeight", beforePageHeight);
								beforePageHeight += pageHeight;
								pageObj.add(pageProp);
								pagesForExport.add(pageObj);
								if( isExcelExport ){
									wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, _formName, _isNextDocument, _DocumentIdx);
									pagesForExport.clear();
									_isNextDocument = false;
									Thread.sleep(10);
								}
								else if( isPPTExport ){
									ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
									pagesForExport.clear();
									_isNextDocument = false;
									Thread.sleep(10);
								}
								else if( isWordExport ){
									if( _lastIndex == _argoPage)
									{
										_ubFormToWord.setIsLast(true);
									}
									else
									{
										_ubFormToWord.setIsLast(false);
									}
									resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( _ubFormToPDF != null )
								{
									_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
									pagesForExport.clear();
								}
								
								else if( isTEXTExport )
								{
									_ubformToText.xmlPasingtoText(pagesForExport);
									pagesForExport.clear();
								}
							}
							
						}
						break;
					case 8: //lastPage
						
						// Section Page 지정
						mFunction.setSectionCurrentPageNum(0);
						mFunction.setSectionTotalPageNum(1);
						
						_objects = new ArrayList<HashMap<String, Object>>();
						_objects = mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , 0, UBIPRINTPOSITIONX, UBIPRINTPOSITIONY, _objects, _docTotalPage ,  _argoPage - _argoDocCnt );

						pageInfoData = new HashMap<String, Object >();
						mCreateFormFn.setFunction(mFunction);
						
						mFunction.setCloneIndex(0);
						
						//TEST 테이블 내보내기를 위하여 테스트 
						_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
						
						pageInfoData.put("pageData", _objects);
						pageInfoData.put("page", _pageHash);
						if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
						
						_argoPage = _argoPage + 1;
						
						_printCurrentPageIndex = _printCurrentPageIndex + 1;
						
						if( isExport ){
							pageProp.clear();
							pageProp.put("cPHeight", _pageExportHeight);
							pageProp.put("cPWidth", _pageExportWidth);
							
							pageProp.put("bPageHeight", beforePageHeight);
							beforePageHeight += pageHeight;
							_objects.add(pageProp);
							pagesForExport.add(_objects);
							if( isExcelExport ){
								wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, _formName, _isNextDocument, _DocumentIdx);
								pagesForExport.clear();
								_isNextDocument = false;
								Thread.sleep(10);
							}
							else if( isPPTExport ){
								ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
								pagesForExport.clear();
								_isNextDocument = false;
								Thread.sleep(10);
							}
							else if( isWordExport ){
								if( _lastIndex == _argoPage)
								{
									_ubFormToWord.setIsLast(true);
								}
								else
								{
									_ubFormToWord.setIsLast(false);
								}
								resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
								pagesForExport.clear();
								Thread.sleep(10);
							}
							else if( _ubFormToPDF != null )
							{
								_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
								pagesForExport.clear();
							}
							
							else if( isTEXTExport )
							{
								_ubformToText.xmlPasingtoText(pagesForExport);
								pagesForExport.clear();
							}
						}
						
						break;
/**					case 12:	//ConnectLink
						// connectLink타입일경우  처리
						ConnectLinkParser connectLink 			= (ConnectLinkParser) _pageInfoClassArrayList.get(i);
						HashMap<String, Object> connectData		= (HashMap<String, Object>) _pageInfoArrayList.get(i);
						
						int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
						// totalPage를 담아두고 각 페이지별로 for문을 돌면서 page를 리턴받기
						
						//Export시 ExportType를 담아두기
						connectLink.setIsExportType(_exportType);
						
						for ( j = _startIndex; j < _pageMaxCnt; j++) {
							
							pageInfoData = new HashMap<String, Object >();
							_objects = new ArrayList<HashMap<String,Object>>();
							
							connectLink.makeConnectPage(j, connectData.get("pageInfoData"), _objects, _docTotalPage,  _argoPage - _argoDocCnt );
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
							
							pageInfoData.put("pageData", _objects);
							pageInfoData.put("page", _pageHash);
							if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
							
							_argoPage = _argoPage + 1;
							
							_printCurrentPageIndex = _printCurrentPageIndex + 1;
							
							if( isExport ){
								pageProp.clear();
								pageProp.put("cPHeight", _pageExportHeight);
								pageProp.put("cPWidth", _pageExportWidth);
								
								pageProp.put("bPageHeight", beforePageHeight);
								beforePageHeight += pageHeight;
								_objects.add(pageProp);
								pagesForExport.add(_objects);
								if( isExcelExport ){
									wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, _formName, _isNextDocument, _DocumentIdx);
									pagesForExport.clear();
									_isNextDocument = false;
									Thread.sleep(10);
								}
								else if( isPPTExport ){
									ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
									pagesForExport.clear();
									_isNextDocument = false;
									Thread.sleep(10);
								}
								else if( isWordExport ){
									resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( _ubFormToPDF != null )
								{
									_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
									pagesForExport.clear();
								}
								else if( isHTMLExport ){
									resultHtml = _ubFormToHTML.xmlParsingHTML(pagesForExport);
									pagesForExport.clear();
								}
							}
							
						}
						
						break; */
					case 14:
						// Linked Project type 문서 타입
						
						LinkedPageParser linkedParser 			= (LinkedPageParser) _pageInfoClassArrayList.get(i);
						HashMap<String, Object> linkedData		= (HashMap<String, Object>) _pageInfoArrayList.get(i);
						
						//Export시 ExportType를 담아두기
						linkedParser.setIsExportType(_exportType);
						
						int _totPage = (Integer) linkedData.get("totPage");
						ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
						HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
						
						cloneData = _page.getDivide();
						clonePage = false;
						
						// Clone 여부 확인
						_cloneColCnt = 1;
						_cloneRowCnt = 1;
						_cloneRepCnt = 1;
						_cloneDirect = "";	// cloneDirect Across Down: 가로, Down Across : 세로

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
	
							if(_page.getDirection()!=null && _page.getDirection().equals("") == false)
							{
							  _cloneDirect = _page.getDirection();
							}
	
							pageWidth = pageWidth / _cloneColCnt;
							pageHeight = pageHeight / _cloneRowCnt;
	
							_cloneRepCnt = _cloneColCnt*_cloneRowCnt;
							clonePage = true;
						}

						_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
						for ( j = _startIndex; j < _pageMaxCnt; j++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(_pageMaxCnt);
							
							mFunction.setCloneIndex(j%_cloneRepCnt);
							
							if( !clonePage || j%_cloneRepCnt == 0 )
							{
								pageInfoData = new HashMap<String, Object >();
								_objects = new ArrayList<HashMap<String,Object>>();
							}
							
							if(clonePage && j%_cloneRepCnt > 0 )
							{
								_clonePositionList = ItemConvertParser.getClonePosition( j, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
								cloneX = _clonePositionList.get(0);
								cloneY = _clonePositionList.get(1);
								
								cloneX = cloneX + UBIPRINTPOSITIONX2;
								cloneY = cloneY + UBIPRINTPOSITIONY2;
							}
							else
							{
								cloneY = 0;
								cloneX = 0;
								
								cloneX = cloneX + UBIPRINTPOSITIONX;
								cloneY = cloneY + UBIPRINTPOSITIONY;
							}
							
							_objects = linkedParser.createLinkedPageItemJson(j, retArr, newDataSet, mParam, cloneX, cloneY, _objects, _docTotalPage,  _argoPage - _argoDocCnt );
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
							
							if( !clonePage || j%_cloneRepCnt > 0 || (j == _totPage-1) )
							{
								pageInfoData.put("pageData", _objects );
								pageInfoData.put("page", _pageHash);
								
								if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
								
								_argoPage = _argoPage + 1;
								
								_printCurrentPageIndex = _printCurrentPageIndex + 1;
								
								if( isExport ){
									pageProp.clear();
									pageProp.put("cPHeight", _pageExportHeight);
									pageProp.put("cPWidth", _pageExportWidth);
									
									pageProp.put("bPageHeight", beforePageHeight);
									beforePageHeight += pageHeight;
									_objects.add(pageProp);
									pagesForExport.add(_objects);
									if( isExcelExport ){
										wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, _formName, _isNextDocument, _DocumentIdx);
										pagesForExport.clear();
										_isNextDocument = false;
										Thread.sleep(10);
									}
									else if( isPPTExport ){
										ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
										pagesForExport.clear();
										_isNextDocument = false;
										Thread.sleep(10);
									}
									else if( isWordExport ){
										if( _lastIndex == _argoPage)
										{
											_ubFormToWord.setIsLast(true);
										}
										else
										{
											_ubFormToWord.setIsLast(false);
										}
										resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
										pagesForExport.clear();
										Thread.sleep(10);
									}
									else if( _ubFormToPDF != null )
									{
										_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
										pagesForExport.clear();
									}
									
									else if( isTEXTExport )
									{
										_ubformToText.xmlPasingtoText(pagesForExport);
										pagesForExport.clear();
									}
								}
							}
							
							//log.debug(getClass().getName() + "::" + "Continue Doc  "  + j);
						}
						i = mPageNumList.size();
						// LinkedForm의 경우 한페이지로 모든 페이지생성이 완료됨
						break;
					default:
						_argoPage = _argoPage + 1;
						_printCurrentPageIndex = _printCurrentPageIndex + 1;
						break;
					}
				}
			}
			
			WriteChartData( mChartData );
			WriteImageData( mImgData );
		}
		
		
		_totPageCheckParser = null;
		_pageInfoMap.clear();
		mPageAr.clear();
		mPageNumList.clear();
		mPageNumRealList.clear();
		_pageInfoArrayList = null;
		_pageInfoClassArrayList = null;
		mDataSet = null;
		
		TempletItemInfo.destroy(mTempletInfo);
		
		Date _chkDate = new Date();  
		log.info("[" + _client_ssid + "^" + _PROJECT_NAME + "/" + _FORM_ID + "^" + _CLIENT_IP + "] FORM PARSING COMPLETE  : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_chkDate) +"]" );

		if( isExport ){
			return null;
		}

		mHashMap.put("pageDatas", pages );
		return null;
	}
	
	
	public ArrayList<String> mSplitFileNames = null;
	public String mSplitFilePath = "";
	
	public String pdfExportManager_pages(String _xml , HashMap _param, String _path, int _divCount) throws Exception
	{
		Object clientEditMode = _param.get("CLIENT_EDIT_MODE");
		log.debug(getClass().getName() + "::" + "Call pdfExportManager_pages...clientEditMode=" + clientEditMode);
		
		// 초기화;
		resetVariable();
	
		//_param.put("httpclient", gHttpclient);
		//_param.put("sessionID", gClientSessionId);
		//_param.put("serverUrl", gServerUrl);
		
		String _pdfFileName = (String) _param.get("PDF_FILE_NAME");
		
		mPDF_EXPORT_TYPE = (String) _param.get("PDF_EXPORT_TYPE");
		
		mSERVER_URL = (String) _param.get("serverUrl");		
		
		mCLIENT_IP = (String) _param.get("CLIENT_IP");
		mPAGE_NUM = (Integer) _param.get("PAGE_NUM");
		
		Object loadType = _param.get("LOAD_TYPE");
		
		boolean _useFileSplit = m_appParams.getREQ_INFO().getUSE_FILE_SPLIT();
		
		boolean bSupportIE9 = false;
		if( loadType != null && loadType.equals("div") )
		{
			bSupportIE9 = true;
		}
		
		String _pdfPrintVersion = GlobalVariableData.UB_DEF_PDF_PRINT_VERSION;
		if(_param.containsKey( GlobalVariableData.UB_PDF_PRINT_VERSION ) && _param.get(GlobalVariableData.UB_PDF_PRINT_VERSION) != null  )
		{
			_pdfPrintVersion = _param.get(GlobalVariableData.UB_PDF_PRINT_VERSION).toString();
		}
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _changeDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>)_param.get("CHANGE_DATASET");
		HashMap<String, Object> _changeItemList = (HashMap<String, Object>) _param.get("CHANGE_ITEM_DATA");

		Object _params = _param.get("PARAMS");
		if( _params != null && _params.hashCode() != 0 )
		{
		 	Object ubObj = JSONValue.parseWithException((String)_params);	
		 	mParam = (JSONObject)ubObj;
		}
		
		if(_param.containsKey( GlobalVariableData.UB_PRINT_USE_UI ) && _param.get(GlobalVariableData.UB_PRINT_USE_UI) != null  )
		{
			log.debug(getClass().getName() + "::" + "pdfExportManager_pages...UB_PRINT_USE_UI=" + _param.get(GlobalVariableData.UB_PRINT_USE_UI));
			mParam.put(GlobalVariableData.UB_PRINT_USE_UI, _param.get(GlobalVariableData.UB_PRINT_USE_UI));
		}
		
		// xml 을 document 형태로 변경.
		// Project 중 필요한 속성을 담음.
		xmlToDocument(_xml);
		
		if("ON".equals(clientEditMode) && _changeDataSet != null)	
		{
			log.debug(getClass().getName() + "::" + "Call pdfExportManager..._changeDataSet != null");
			mDataSet = _changeDataSet;
		}
		else
		{
			log.debug(getClass().getName() + "::" + "Call pdfExportManager..._changeDataSet == null");
			getXmlToDataSet();
//			xmlToDataSet();		// 여러건의 Form의 데이터 셋을 로드하기 위하여 지정

		}
		
		// mHashMap 에 프린트 옵션을 지정 2016-04-21 최명진
		mHashMap = getPrintOption(mHashMap, mParam);
		
		boolean usePrint = false;
		float _printScale = 1;
		String _fitSize = "";
		String _useSize = "";
		// 인쇄시 비율옵션
		//if(m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE().equals("PRINT") || m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE().equals("LOCAL_PRINT"))
		if(m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE().equals("LOCAL_PDF") || m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE().equals("LOCAL_PRINT"))
		{
			if( mParam.containsKey( GlobalVariableData.UB_PRINT_SCALE_X) || mParam.containsKey(GlobalVariableData.UB_PRINT_SCALE_Y))
			{
				float _scaleX = 1;
				float _scaleY = 1;
				if( mParam.containsKey(GlobalVariableData.UB_PRINT_SCALE_X) )
				{
					HashMap<String, String> _scaleXParam = (HashMap<String, String>) mParam.get( GlobalVariableData.UB_PRINT_SCALE_X);
					_scaleX = Float.valueOf(String.valueOf(_scaleXParam.get("parameter")));
				}
				
				if( mParam.containsKey(GlobalVariableData.UB_PRINT_SCALE_Y) )
				{
					HashMap<String, String> _scaleYParam = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_PRINT_SCALE_Y);
					_scaleY = Float.valueOf(String.valueOf(_scaleYParam.get("parameter")));
				}
				
				if( _scaleY > 0 && _scaleX > _scaleY )
				{
					_printScale =  _scaleY;
				}
				else if( _scaleX > 0 )
				{
					_printScale = _scaleX;
				}
			}
			
			JSONObject printOption = (JSONObject) mHashMap.get("printOption");
			boolean usePrintUI = false;
			if( mParam.containsKey( GlobalVariableData.UB_PRINT_FIT_SIZE ) )
			{
				HashMap<String, String> _fitParam = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_PRINT_FIT_SIZE);
				printOption.put( GlobalVariableData.UB_PRINT_FIT_SIZE , String.valueOf(_fitParam.get("parameter")) );
				
				mHashMap.put("printOption", printOption);
			}
			
			if( mParam.containsKey( GlobalVariableData.UB_PRINT_USE_UI) )
			{
				HashMap<String, String> _useUiParam = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_PRINT_USE_UI);
				printOption.put( GlobalVariableData.UB_PRINT_USE_UI , String.valueOf(_useUiParam.get("parameter")) );
				
				usePrintUI = String.valueOf(_useUiParam.get("parameter")).equals("true");
				
				mHashMap.put("printOption", printOption);
			}
			
			if( mParam.containsKey( GlobalVariableData.UB_PRINT_VIEW_USE_UI) )
			{
				HashMap<String, String> _useUiParam = (HashMap<String, String>) mParam.get(GlobalVariableData.UB_PRINT_VIEW_USE_UI);
				printOption.put( GlobalVariableData.UB_PRINT_VIEW_USE_UI , String.valueOf(_useUiParam.get("parameter")) );
				
				mHashMap.put("printOption", printOption);
			}
			
			usePrint = m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE().equals("LOCAL_PDF") ? usePrintUI : true;
		}
		
		int _caMax = 1;
		_ubFormToPDF = new ubFormToPDF();
		
		_ubFormToPDF.setUdpClient(this.m_appParams.getUDPClient());
		_ubFormToPDF.setPdfFileName(_pdfFileName);
		
		_ubFormToPDF.setExportDivCount(_divCount);
		_ubFormToPDF.setExportPath(_path);
		_ubFormToPDF.setPDF_PRINT_VERSION(_pdfPrintVersion);		//PDF Print Version 지정
		
//		if(this.mServiceReqMng != null) _ubFormToPDF.setSESSION_ID( this.mServiceReqMng.getServiceManager().getHttpRequest().getSession().getId() );
//		else _ubFormToPDF.setSESSION_ID("");
		
		_ubFormToPDF.setmImageDictionary(mImageDictionary);
		
		if( "ON".equals(clientEditMode) && _changeItemList != null && _changeItemList.size() > 0)
		{
//			testPdfChangeMapping(_changeItemList);
			_ubFormToPDF.setChangeItemList(_changeItemList);
		}
		
		if( !mParamResult )
		{
			if(_useFileSplit) {
				_ubFormToPDF.setSplitFilePath(_path);
				_ubFormToPDF.setResultFileName(_path);
			}
			
			if( _ubFormToPDF.isStarting() && _useFileSplit == false ) _ubFormToPDF.xmlParsingpdfStart(mHashMap, usePrint, _caMax, _printScale);
			
			//private ArrayList<ArrayList<HashMap<String,Object>>> getExportAllPagesJS( Boolean isExport, HashMap<String, Object> _pInfo, boolean bSupportIE9, HashMap<String, Object> _changeItemList )
			getExportAllPagesJS(true, null, bSupportIE9, _changeItemList);		//TEST
			
			if(Log.printStop){
				//저장 된 PDF 파일이 있다면 삭제한다.
				ArrayList<String> mSaveFilePathAr = _ubFormToPDF.getPdfFilePaths();
				File sFile;
				if(mSaveFilePathAr.size()>0){
					for(int i = 0; i<mSaveFilePathAr.size();i++ ){
						sFile = new File(mSaveFilePathAr.get(i));	
						if(sFile.exists()){							
							//getPdfFileStream							
							if( !sFile.delete()){
								//현재 열여있는 파일인경우 fileStream 닫은 후  삭제한다.
								_ubFormToPDF.getPdfFileStream().close();
								sFile.delete();
							}
						}
					}
				}
				Log.printStop = false;
				//merge message
				String Msg = Log.getMessage(Log.MSG_LP_MAKEPDF_CANCELED);				
				return "CANCEL:" + Msg;
			}			
		}
		else
		{
			return null;
		}
		
		if(mImageDictionary != null )
		{
			clearImageDictionary();
		}
		
		_ubFormToPDF.getPdfByteData();
		
		//merge message
		this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MERGEPDF", "99999", "Merging Pdf Pages..."));		
		
		String _retByte = _ubFormToPDF.makeMergePdfFiles(usePrint);		

		_ubFormToPDF.clear();
		
		mUseFitWindow = _ubFormToPDF.getUseFitWindow();
		mUuseUiDialog = _ubFormToPDF.getUseUiDialog();
		mPrintCirculation = _ubFormToPDF.getPrintCirculation();
	
		return _retByte;
	}
}
