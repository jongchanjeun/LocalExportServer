package org.ubstorm.service.parser;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;
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
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.dictionary.ImageDictionary;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.logger.Log;
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
import org.ubstorm.service.parser.queue.JobDataSetQueue;
import org.ubstorm.service.utils.JsonUtils;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class xmlToUbForm {

	protected Logger log = Logger.getLogger(getClass());

	//private HashMap<String, Object> mHashMap;
	protected JSONObject mHashMap;

	//private HashMap<String, Object> mParam;
	protected JSONObject mParam;

	protected String mSERVER_URL;
	
	protected String mCLIENT_IP;
	
	protected int mPAGE_NUM;

	private JsonUtils _jsonUtils;

	protected Document mDocument;
	protected boolean mUseMultiFormType = false;			// Multi문서호출 여부 
	
	private HashMap<String, Object> mPargingParam;
	
	private ArrayList<Element> mPageAr;
	
	protected HashMap<String, ArrayList<HashMap<String, Object>>> mDataSet;
	private HashMap<String, Integer> mDataSetRowCountInfo;
	
	protected int mTOTAL_PAGE_NUM;	
	private int mDATA_CNT;
	
	protected ArrayList<Integer> mPageNumList;
	protected ArrayList<Integer> mPageNumRealList;
	
	protected Boolean mParamResult = false;
	private Boolean mCoverFlg = false;
	private Boolean mLastFlg = false;
	
	// class
	private ItemPropertyVariable mItemPropVar;
	protected ItemPropertyProcess mPropertyFn;
	protected DataSetProcess mDataSetFn;
	private PageNumProcess mPageNumFn;
	protected CreateFormProcess mCreateFormFn;
	
	HashMap<String, Element> marDataSetItems;
	HashMap<String, Element> marDataSetMerged;
	HashMap<String, HashMap<String, String>> mDataSetParam;
	
	protected UDMParamSet m_appParams;
	protected HashMap<String, String> mImgData;
	protected HashMap<String, Object> mChartData;
	protected Function mFunction;
	
	public Float mPageSize = 0f;
	
	protected ubFormToPDF _ubFormToPDF;
	
	protected String CLIENT_EDIT_MODE = "OFF"; 
	
	protected ArrayList<String> documents = null;
	protected ArrayList< HashMap<String,String> > documentInfos = null;
	protected ArrayList<JSONObject> documentParams = null;
	protected ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>> dataSets;
	protected ArrayList<HashMap<String, String>> mFormNames;
	
	protected ImageDictionary mImageDictionary;
	
	private boolean mIsExportMulti = false;
	
	protected String mPDF_EXPORT_TYPE = "LOCAL_PDF";
	
	protected String FUNCTION_VERSION="1.0";
	
	protected float mPageMarginTop = 0;
	protected float mPageMarginLeft = 0;
	protected float mPageMarginRight = 0;
	protected float mPageMarginBottom = 0;
	protected HashMap<String, TempletItemInfo> mTempletInfo;
	
	protected HashMap<String, Object> _pageInfoMap;
	
	protected HashMap<String, String> mPDFProtectionInfo;
	
	protected boolean mIsOpenParamPopup = false;
	protected boolean mUseParameterPopup = true;
	
	protected boolean mUseParameter = true;
	
	protected String mUseFitWindow = "true";
	protected String mUuseUiDialog = "true";
	protected int mPrintCirculation = 1;
	protected boolean mIsLogDebug = false;
	
	public String getUseFitWindow()
	{
		return mUseFitWindow;
	}
	public String getUseUiDialog()
	{
		return mUuseUiDialog;
	}
	public int getPrintCirculation()
	{
		return mPrintCirculation;
	}
	
	public xmlToUbForm(UDMParamSet appParams)
	{
		this.m_appParams = appParams;
	}
	
	public HashMap<String, Object> getPageInfoMap()
	{
		return _pageInfoMap;
	}
	protected void resetVariable()
	{
		mHashMap = new JSONObject();
		mParam = new JSONObject();
		mPAGE_NUM = 0;
		mCLIENT_IP = "";
		mTOTAL_PAGE_NUM = 0;
		mDATA_CNT = 0;
		
		mPageAr = new ArrayList<Element>();
		mPargingParam = new HashMap<String, Object>();
		mPageNumList = new ArrayList<Integer>();
		mPageNumRealList = new ArrayList<Integer>();
		mDataSet = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		
		mCoverFlg = false;
		mLastFlg = false;
		mParamResult = false;
		
		_jsonUtils = new JsonUtils();
		mCreateFormFn = new CreateFormProcess();
		mCreateFormFn.init(this.m_appParams);
		this.mImgData = new HashMap<String,String>();
		mCreateFormFn.setImageData(this.mImgData);
		this.mChartData = new HashMap<String,Object>();
		mCreateFormFn.setChartData(this.mChartData);
		this.mFunction=new Function(mDataSet, mParam);
		mCreateFormFn.setFunction(this.mFunction);
		
		mPargingParam = new HashMap<String, Object>();
		mPropertyFn = new ItemPropertyProcess();
		mDataSetFn = new DataSetProcess();
		mPageNumFn = new PageNumProcess(); 
		mItemPropVar = new ItemPropertyVariable();
		marDataSetItems = new HashMap<String, Element>();
		mDataSetParam = new HashMap<String, HashMap<String, String>>();
		
		marDataSetMerged = new HashMap<String, Element>();
		
		mImageDictionary = new ImageDictionary();
//		mItemPropVar.init();
	}
	
	protected Date _startTimeDate;
	protected long _checkStartTime = 0;
	
	public void clear()
	{
		if(mHashMap!= null)
		{
			mHashMap.clear();
			mHashMap = null;
		}
		
		if(mParam != null)
		{
			mParam.clear();
			mParam = null;
		}
		
		if(mDataSet != null)
		{
			mDataSet.clear();
			mDataSet = null;
		}
		
		if(marDataSetItems != null)
		{
			marDataSetItems.clear();
			marDataSetItems = null;
		}
		
		if(mDataSetParam != null)
		{
			mDataSetParam.clear();
			mDataSetParam = null;
		}
		
		if(marDataSetMerged != null)
		{
			marDataSetMerged.clear();
			marDataSetMerged = null;
		}
		
		if(mPageAr != null)
		{
			mPageAr.clear();
			mPageAr = null;
		}
		
	}
	
	public String formExportManager(String _xml , HashMap _param, FORMFile formfile) throws XPathExpressionException, SAXException, Exception, IOException, ParserConfigurationException, ParseException
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
			mDataSet = _changeDataSet;
		}
		else
		{
			getXmlToDataSet();
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
		//String result = _formExportManagerDiv9(_xml, _param, formfile);	
		String result = _formExportManagerDiv9(formfile);	
		
		if(Log.printStop){
			//Log.printStop = false;
			//merge message
			result = Log.getMessage(Log.MSG_LP_PRINTJOB_CANCEL);				
	
			Thread.sleep(100);
			Log.printStop = false;

			return "CANCEL:" + result;
		}
		
		return result;
	}
	
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
			mDataSet = _changeDataSet;
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
			 _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
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
			
//			// Project info send
//			if(bSupportEform)
//			{
//				String _dataSets = JSONObject.toJSONString(mDataSet);
//				formfile.addPageB64(-1, ("{\"project\":" + ((JSONObject) mHashMap.get("project")).toJSONString() + ",\"dataSets\":" + _dataSets +  ",\"pageSize\":" + mPageSize + ",\"resultType\":\"FORM\",\"printOption\":" +  ((JSONObject) mHashMap.get("printOption")).toJSONString() + "}").getBytes("UTF-8"));
//			}
//			else
//			{
//				formfile.addPageB64(-1, ("{\"project\":" + ((JSONObject) mHashMap.get("project")).toJSONString() + ",\"pageSize\":" + mPageSize + ",\"resultType\":\"FORM\",\"printOption\":" +  ((JSONObject) mHashMap.get("printOption")).toJSONString() + "}").getBytes("UTF-8"));
//			}
//			//(HashMap<String,Object>)
//			formfile.addProjectInfo((JSONObject)mHashMap.get("project"));
			//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(this.mServiceReqMng, m_appParams);
			//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(m_appParams);
//			TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser( m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );
//			
//			mFunction.setFunctionVersion(FUNCTION_VERSION);
//			
//			HashMap<String, Object> _pageInfoMap;
//			
//			// 여러건의 Form을 로드하기 위한 분기 @최명진
//			if(documents != null && documents.size() > 0)
//			{
//				_pageInfoMap = _totPageCheckParser.getTotalPageMulti(documentParams, documents, dataSets, mFunction, documentInfos  );
//			}
//			else
//			{
//				 _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
//			}
//			HashMap<String, Object> _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
//			HashMap<String, Object> _pageInfoMap = _totPageCheckParser.getTotalPageMulti(mParam, documents, dataSets, mFunction );
			
			mPageNumList 		= (ArrayList<Integer>) _pageInfoMap.get("PAGE_NUMLIST");
			mPageAr 			= (ArrayList<Element>) _pageInfoMap.get("PAGE_AR");
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
//			if( _printS > -1 )
//			{
//				_startIndex = _printS;
//				_argoPage = _startIndex;
//			}
//			else
//			{
//				_startIndex = 0;
//			}
//			if( _printM > -1 )
//			{
//				if( _endIndex > mTOTAL_PAGE_NUM)
//				{
//					_endIndex = mTOTAL_PAGE_NUM;
//				}
//				else
//				{
//					_endIndex = _printM;
//				}
//			}
//			else 
//			{
//				_endIndex = mTOTAL_PAGE_NUM; 
//			}
//			
//			if(_startIndex > _endIndex)
//			{
//				_startIndex = _endIndex -1;
//			}
			
			
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
			
			float _pagePrintMarginTop 	= 0;
			float _pagePrintMarginLeft 	= 0;
			float _pagePrintMarginRight = 0;
			float _pagePrintMarginBottom = 0;
			
			if( m_appParams.getREQ_INFO().getPAGE_MARGIN_TOP()>0 ) _pagePrintMarginTop = m_appParams.getREQ_INFO().getPAGE_MARGIN_TOP();
			if( m_appParams.getREQ_INFO().getPAGE_MARGIN_LEFT()>0 ) _pagePrintMarginLeft = m_appParams.getREQ_INFO().getPAGE_MARGIN_LEFT();
			if( m_appParams.getREQ_INFO().getPAGE_MARGIN_RIGHT()>0 ) _pagePrintMarginRight = m_appParams.getREQ_INFO().getPAGE_MARGIN_RIGHT();
			if( m_appParams.getREQ_INFO().getPAGE_MARGIN_BOTTOM()>0 ) _pagePrintMarginBottom = m_appParams.getREQ_INFO().getPAGE_MARGIN_BOTTOM();
			
			this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT_START", Log.MSG_LP_START_MAKEPRINT, Log.getMessage(Log.MSG_LP_START_MAKEPRINT)));
			
			for(int i = 0 ; i < mPageNumList.size(); i ++){
				if(Log.printStop){
					break;					
				}			
				
				pages.clear();
				
				Element _page = mPageAr.get(i);
				
				_reportType = Integer.parseInt(_page.getAttribute("reportType"));
				_pageHash = mPropertyFn.getAttrObject(_page.getAttributes());
				
				_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
				
				// Clone 페이지의 경우 width와 height값을 담아두기
				originalPageWidth = pageWidth = Float.valueOf(_page.getAttribute("width"));
				originalPageHeight = pageHeight = Float.valueOf(_page.getAttribute("height"));
				
				if( _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
				{
//					if( mPageNumList.size()-1 == i )
//					{
//						isConnectGroupPage = false;
//					}
//					else
//					{
//						isConnectGroupPage = true;
//					}
//					
					isConnectGroupPage = true;
					isLastConnectGroupPage = false;
					
					if( i > 0  && mPageAr.get(i-1).getAttribute("id").toString().equals(_page.getAttribute("id").toString()) == false )
					{
						_clonePageCnt = 0;
						//isConnectGroupPage = false;
					}

					if( i == mPageNumList.size() -1  || mPageAr.get(i+1).getAttribute("id").toString().equals(_page.getAttribute("id").toString()) == false )
					{
						isLastConnectGroupPage = true;
					}
				}
				else
				{
					_clonePageCnt = 0;
				}
				
				// Clone Page값 담기
				cloneData = _page.getAttribute("clone");
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
				
				// 파라미터를 페이지별로 셋팅하기 위하여 작업
				if( !mUseMultiFormType && _page.hasAttribute("FORM_IDX") )
				{
					int _DocumentIdx = Integer.parseInt(_page.getAttribute("FORM_IDX").toString() );
					mParam = documentParams.get(_DocumentIdx);
					
					_docTotalPage = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_TOTAL_PAGE")).get(_DocumentIdx);
					_argoDocCnt = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_START_IDX")).get(_DocumentIdx);
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
					
					pageInfoData.put("pageData", mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam , 0 , _docTotalPage , _argoPage - _argoDocCnt) );
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
//					if(isPreviewPage){
//						int orgMaxCnt = _pageMaxCnt;
//						//임시버퍼 데이타 조회 현재 선택한 인텍스의 좌우 5 씩 10데이타 조회
//						if(_startIndex < Log.prePageBuff){	
//							_endIndex = _startIndex+ Log.prePageBuff;
//							_startIndex = 0;
//							
//						}else{
//							_endIndex = _startIndex + (Log.prePageBuff == 0?1:Log.prePageBuff);
//							_startIndex = _startIndex - Log.prePageBuff;						
//						}
//						//최대페이지 넘지 않도록 조정
//						if(_endIndex > orgMaxCnt){
//							_endIndex = orgMaxCnt;
//						}
//						//_argoPage = _startIndex;
//					}					
//					_pageCnt = mPageNumList.get(i);
					
					int _start = 0;
					int _max = 0;

					if(_argoPage >_startIndex  && _pageCnt > 1){
						_argoPage = _startIndex;
					}
//					for ( j = 0; j < _pageCnt; j++) {
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
//								_objects.clear();
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
								Element _chkPage = mPageAr.get(_pageContinuePageCnt);
								
								_objects = mCreateFormFn.createFreeFormConnect(_chkPage , _tempDataSet , mParam , _pageContinuePageDataCnt, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt);
							}
							else
							{
								_objects = mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , k, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt);
							}
							
						}
						pageInfoData = new HashMap<String, Object >();
//						pageInfoData.put("pageData", mCreateFormFn.CreateFreeFormAll(_page , mDataSet , mParam , j, mTOTAL_PAGE_NUM , _argoPage) );
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
//							_tabIndexlist = (ArrayList<Object>) _pageInfoTIDXList.get(j);	// j가 초기화되어 계속 0 값의 index를 가져온다.
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
//					for ( j = 0; j < _masterTotPage; j++) {
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
//					if( pageInfoData!=null ) pageInfoData.clear();
//					masterInfo.clear();
//					masterList.clear();
//					_objects.clear();
//					masterParser = null;
				
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
					
					ContinueBandParser continueBandParser  = (ContinueBandParser) _pageInfoClassArrayList.get(i);
					continueBandParser.setImageData(this.mImgData);
					continueBandParser.setChartData(this.mChartData);
					continueBandParser.setFunction(mFunction);
					ArrayList<Object> bandAr = (ArrayList<Object>) _pageInfoArrayList.get(i);

					HashMap<String, BandInfoMapData> bandInfo = (HashMap<String, BandInfoMapData>) bandAr.get(0);
					ArrayList<BandInfoMapData> bandList =(ArrayList<BandInfoMapData>) bandAr.get(1);
					ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
					HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
					
					// group
					HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
					ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
					
					continueBandParser.mOriginalDataMap = originalDataMap;
					continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
					
//					ArrayList<Object> _pageInfoREQList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_REQ_LIST");
//					ArrayList<Object> _pageInfoTIDXList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_TIDX_LIST");
					_reqlist = new ArrayList<Object>();
					_tabIndexlist = new ArrayList<Object>();
					
					if(  bandAr.size() > 8 )
					{
						mFunction.setGroupBandCntMap((HashMap<String, Integer>) bandAr.get(8));
					}
					
					boolean isPivot = false;
					
					if( _objects == null )_objects = new ArrayList<HashMap<String,Object>>();
					
					if(_page.getAttribute("isPivot").equals("true"))
					{
						isPivot = true;
						// Project info send
						_pageHash.put("height", _page.getAttribute("width"));
						_pageHash.put("width", _page.getAttribute("height"));
					}
					
					_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
					
					int _pagesRowListSize = pagesRowList.size();
					float _bandMaxWidth = 0;
					
					// 페이지 범위 지정
					_pageStartCnt   = _startIndex * _cloneRepCnt;
					_pageMaxinumCnt = _pageStartCnt + ( ( _endIndex - _startIndex ) * _cloneRepCnt);
					if( _pageMaxinumCnt > _pagesRowListSize ) _pageMaxinumCnt = _pagesRowListSize;
					
//					if(isPreviewPage){
//						int orgMaxCnt = _pageMaxinumCnt;
//						//임시버퍼 데이타 조회 현재 선택한 인텍스의 좌우 5 씩 10데이타 조회
//						if(_pageStartCnt < Log.prePageBuff){	
//							_pageMaxinumCnt = _pageStartCnt + Log.prePageBuff;
//							_pageStartCnt = 0;
//							
//						}else{
//							_pageMaxinumCnt = _pageStartCnt + (Log.prePageBuff == 0?1:Log.prePageBuff);							
//							_pageStartCnt = _pageStartCnt - Log.prePageBuff;						
//						}
//						//최대페이지 넘지 않도록 조정
//						if(_pageMaxinumCnt > orgMaxCnt){
//							_pageMaxinumCnt = orgMaxCnt;
//						}
//					}
					
//					if(isPreviewPage)_argoPage = _pageStartCnt;
					//_argoPage = _pageStartCnt + (mTOTAL_PAGE_NUM - mPageNumList.get(i));			
					
					//_pageMaxinumCnt = _endIndex;
					for ( j = _pageStartCnt; j < _pageMaxinumCnt; j++) {
//					for ( j = 0; j < _pagesRowListSize; j++) {
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
							_pageHash.put("width", _page.getAttribute("width"));
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
							//_clonePositionList = ItemConvertParser.getClonePosition( _pageRepeatCnt, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
							_clonePositionList = ItemConvertParser.getClonePosition( _pageRepeatCnt, _cloneColCnt, _cloneRowCnt, originalPageWidth - mPageMarginLeft- mPageMarginRight, originalPageHeight - mPageMarginTop - mPageMarginBottom , cloneData, _cloneDirect );
							cloneX = _clonePositionList.get(0);
							cloneY = _clonePositionList.get(1);

						}
						else
						{
							cloneY = 0;
							cloneX = 0;
						}
						
						_objects = continueBandParser.createContinueBandItems(j, _tempDataSet, bandInfo, bandList, pagesRowList, mParam, crossTabData,cloneX,cloneY,_objects,_docTotalPage, _argoPage - _argoDocCnt , isPivot);
						
						_bandMaxWidth = _bandMaxWidth + continueBandParser.getBandMaxWidth();
						
						if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) || ( !isConnectGroupPage && (j == _pagesRowListSize-1 ) )  || (isLastConnectGroupPage && (j == _pagesRowListSize-1 )) )
						{
							if( Float.valueOf( _pageHash.get("width") ) < _bandMaxWidth )
							{
								_pageHash.put("width", Float.valueOf(_bandMaxWidth ).toString() );
							}
							
							pageInfoData.put("pageData", _objects );
							pageInfoData.put("page", _pageHash);
							
							pageInfoData.put("pageReqData", _reqlist );
							pageInfoData.put("pageTIdxData", _tabIndexlist );
							
							pages.put( String.valueOf(_argoPage), pageInfoData);
							
							// 1 Page send to client
							//formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));	
							
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
//					continueBandParser = null;
//					bandAr = null;
//					bandInfo = null;
//					bandList = null;
//					crossTabData = null;
					
					break;
				case 4: //labelBand
					_pageCnt = mPageNumList.get(i);
					for ( j = _startIndex; j < _pageMaxCnt; j++) {
//					for ( j = 0; j < _pageCnt; j++) {
						if(Log.printStop){
							break;					
						}
						
						// Section Page 지정
						mFunction.setSectionCurrentPageNum(j);
						mFunction.setSectionTotalPageNum(_pageCnt);
						
						mFunction.setCloneIndex(0);
						
						pages.clear();
						
						pageInfoData = new HashMap<String, Object >();
						pageInfoData.put("pageData", mCreateFormFn.CreateLabelBandAll(_page , _tempDataSet , mParam ,j,_docTotalPage , _argoPage - _argoDocCnt ) );
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
					pageInfoData = new HashMap<String, Object >();
					pageInfoData.put("pageData", mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet, mParam , 0, _docTotalPage , _argoPage - _argoDocCnt ) );
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
				case 12:	//ConnectLink
					// connectLink타입일경우  
//					ConnectLinkParser connectLink = new ConnectLinkParser(m_appParams);
//					connectLink.setFunction(mFunction);
					
					ConnectLinkParser connectLink = (ConnectLinkParser) _pageInfoClassArrayList.get(i);
					connectLink.setFunction(mFunction);
					
//					HashMap<String, Object> connectData = connectLink.loadPagesData(_page, mParam);
					HashMap<String, Object> connectData =  (HashMap<String, Object>) _pageInfoArrayList.get(i);
					int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
					// totalPage를 담아두고 각 페이지별로 for문을 돌면서 page를 리턴받기
					
//					mTOTAL_PAGE_NUM = mTOTAL_PAGE_NUM + _connectTotPage;
					
					_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
					
					for ( j = _startIndex; j < _pageMaxCnt; j++) {
//					for ( j = 0; j < _connectTotPage; j++) {
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
				case 14:
					// Linked Project type 문서 타입
//					LinkedPageParser linkedParser = new LinkedPageParser(m_appParams);
					
					LinkedPageParser linkedParser = (LinkedPageParser) _pageInfoClassArrayList.get(i);
					linkedParser.setFunction(mFunction);
					
//					HashMap<String, Object> linkedData = linkedParser.loadTotalPage(mPageAr,mDataSet );
					HashMap<String, Object> linkedData =   (HashMap<String, Object>) _pageInfoArrayList.get(i);
					int _totPage = (Integer) linkedData.get("totPage");
					ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
					HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
					
					cloneData = _page.getAttribute("divide");
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
					
					_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
					
					for ( j = _startIndex; j < _pageMaxCnt; j++) {
//					for ( j = 0; j < _totPage; j++) {
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
					
				case 16:
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
						
//						int k=0;
//						if(_pageInfoREQList.size()>i && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(i)).size() > j)
//						{
//							ArrayList<HashMap<String, Object>> _pageInfoReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(i)).get(j);
//							if(_pageInfoReq != null )
//							{
//								for ( k = 0; k < _pageInfoReq.size(); k++) {
//									_reqlist.add( _pageInfoReq.get(k) );
//								}
//							}
//						}
//						
//						if(_pageInfoTIDXList.size()>i && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(i)).size() > j)
//						{
//							ArrayList<HashMap<String, Object>> _pageTIDXReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(i)).get(j);
//							if(_pageTIDXReq != null )
//							{
//								for ( k = 0; k < _pageTIDXReq.size(); k++) {
//									_tabIndexlist.add( _pageTIDXReq.get(k) );
//								}
//							}
//						}
						
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
	
	
	// 신규  전체  페이지 정보를 생성하는 메소드. (내부적로 1page가 완성되면 곧바로 뷰어롤 전송한다.) - For Support IE9
	//public String _formExportManagerDiv9(String _xml , HashMap _param, FORMFile formfile) throws Exception
	public String _formExportManagerDiv9(FORMFile formfile) throws Exception
	{
		String _hashStr = null;
//		Object clientEditMode = _param.get("CLIENT_EDIT_MODE");
		Object clientEditMode = CLIENT_EDIT_MODE;
		
		log.debug(getClass().getName() + "::" + "Call _formExportManagerDiv9...clientEditMode=" + clientEditMode);
		if( !mParamResult )
		{
//			getTotalPage(_param);
			getBinDivCompReportPagesNew(true, "ON".equals(clientEditMode), formfile);	
		}
		else
		{
			_hashStr = mHashMap.toJSONString();
			//this.mServiceReqMng.getServiceManager().sendResultB64(_hashStr.getBytes("UTF-8"), true, true, -1, -1);
			//this.mServiceReqMng.getServiceManager().end();
			
			System.out.println("_formExportManagerDiv9::mParamResult == true!!!");			
		}

		return null;
	}
	
	protected String JSONConverter(String type, String command, String code, String message) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("port", Log.wsPort);
		jsonObject.put("type", type);
		jsonObject.put("command", command);
		jsonObject.put("code", code);
		jsonObject.put("message", message);
		return jsonObject.toString();
	}	
	
	// 전체 페이지를 페이지 별로 생성하여, 페이지 별로 클라이언트로 분할 전송한다. (데이터는 전체 데이터를 가져와서 작업함)
	private void getBinDivCompReportPagesNew(boolean bSupportIE9, boolean bSupportEform, FORMFile formfile) throws UnsupportedEncodingException , Exception
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
		
		// Project info send
		if(bSupportEform)
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
		//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(this.mServiceReqMng, m_appParams);
		//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(m_appParams);
		TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser( m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );
		
		mFunction.setFunctionVersion(FUNCTION_VERSION);
		
		HashMap<String, Object> _pageInfoMap;
		String _usePdfPassword = "true";
		String _usePdfProtection = "true";
		
		// 여러건의 Form을 로드하기 위한 분기 @최명진
		if(documents != null && documents.size() > 0)
		{
			_pageInfoMap = _totPageCheckParser.getTotalPageMulti(documentParams, documents, dataSets, mFunction, documentInfos  );
		}
		else
		{
			 _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
		}
		
		if( _totPageCheckParser.getPdfProtectionInfo() != null && _totPageCheckParser.getPdfProtectionInfo().containsKey("PDF_PASSWORD") && _totPageCheckParser.getPdfProtectionInfo().get("PDF_PASSWORD").equals("")== false )
		{
			_usePdfPassword = "false";
		}

		if( _totPageCheckParser.getPdfProtectionInfo() != null && _totPageCheckParser.getPdfProtectionInfo().containsKey("USE_PROTECTION") && _totPageCheckParser.getPdfProtectionInfo().get("USE_PROTECTION").equals("")== false )
		{
			_usePdfProtection = "false";
		}
		
//		HashMap<String, Object> _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
//		HashMap<String, Object> _pageInfoMap = _totPageCheckParser.getTotalPageMulti(mParam, documents, dataSets, mFunction );
		
		mPageNumList 		= (ArrayList<Integer>) _pageInfoMap.get("PAGE_NUMLIST");
		mPageAr 			= (ArrayList<Element>) _pageInfoMap.get("PAGE_AR");
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
		
		if( _printS > -1 )
		{
			_startIndex = _printS;
			_argoPage = _startIndex;
		}
		else
		{
			_startIndex = 0;
		}
		if( _printM > -1 )
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
		
		if(_startIndex > _endIndex)
		{
			_startIndex = _endIndex -1;
		}
		
		
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
		
		float _pagePrintMarginTop 	= 0;
		float _pagePrintMarginLeft 	= 0;
		float _pagePrintMarginRight = 0;
		float _pagePrintMarginBottom = 0;
		
		if( m_appParams.getREQ_INFO().getPAGE_MARGIN_TOP()>0 ) _pagePrintMarginTop = m_appParams.getREQ_INFO().getPAGE_MARGIN_TOP();
		if( m_appParams.getREQ_INFO().getPAGE_MARGIN_LEFT()>0 ) _pagePrintMarginLeft = m_appParams.getREQ_INFO().getPAGE_MARGIN_LEFT();
		if( m_appParams.getREQ_INFO().getPAGE_MARGIN_RIGHT()>0 ) _pagePrintMarginRight = m_appParams.getREQ_INFO().getPAGE_MARGIN_RIGHT();
		if( m_appParams.getREQ_INFO().getPAGE_MARGIN_BOTTOM()>0 ) _pagePrintMarginBottom = m_appParams.getREQ_INFO().getPAGE_MARGIN_BOTTOM();
		
		this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT_START", Log.MSG_LP_START_MAKEPRINT, Log.getMessage(Log.MSG_LP_START_MAKEPRINT)));
		
		String _heightArrStr = "";
		String _widthArrStr = "";
		
		for(int i = 0 ; i < mPageNumList.size(); i ++){
			if(Log.printStop){
				break;					
			}			
			
			pages.clear();
			
			Element _page = mPageAr.get(i);
			
			_reportType = Integer.parseInt(_page.getAttribute("reportType"));
			_pageHash = mPropertyFn.getAttrObject(_page.getAttributes());
			
			_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
			_pageHash.put("usePdfPassword", _usePdfPassword);
			_pageHash.put("usePdfProtection", _usePdfProtection);
			
			// Clone 페이지의 경우 width와 height값을 담아두기
			originalPageWidth = pageWidth = Float.valueOf(_page.getAttribute("width"));
			originalPageHeight = pageHeight = Float.valueOf(_page.getAttribute("height"));
						
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
				_pageHash = getPageHeightList( _pageHash, _pageInfoClassArrayList ,_pagePrintMarginTop , _pagePrintMarginLeft, _pagePrintMarginRight, _pagePrintMarginBottom );
				
				if( _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
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
					
			
			// Clone 페이지의 경우 width와 height값을 담아두기
			originalPageWidth = pageWidth = Float.valueOf(_page.getAttribute("width"));
			originalPageHeight = pageHeight = Float.valueOf(_page.getAttribute("height"));
			
			if( _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
			{
				isConnectGroupPage = true;
				isLastConnectGroupPage = false;
				
				if( i > 0  && mPageAr.get(i-1).getAttribute("id").toString().equals(_page.getAttribute("id").toString()) == false )
				{
					_clonePageCnt = 0;
					//isConnectGroupPage = false;
				}

				if( i == mPageNumList.size() -1  || mPageAr.get(i+1).getAttribute("id").toString().equals(_page.getAttribute("id").toString()) == false )
				{
					isLastConnectGroupPage = true;
				}
			}
			else
			{
				_clonePageCnt = 0;
			}
			
			// Clone Page값 담기
			cloneData = _page.getAttribute("clone");
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
			
			// 파라미터를 페이지별로 셋팅하기 위하여 작업
			if( _page.hasAttribute("FORM_IDX") )
			{
				int _DocumentIdx = Integer.parseInt(_page.getAttribute("FORM_IDX").toString() );
				mParam = documentParams.get(_DocumentIdx);
				
				_docTotalPage = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_TOTAL_PAGE")).get(_DocumentIdx);
				_argoDocCnt = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_START_IDX")).get(_DocumentIdx);
			}
			
			// Clone Page
			
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
			
			_pageMaxCnt = mPageNumList.get(i);
			
			if( _pageMaxCnt > _endIndex )
			{
				_pageMaxCnt = _endIndex;
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
			
			
			mFunction.setFunctionVersion(FUNCTION_VERSION);
						
			////////////// PAGE 범위 지정 처리 속성 지정 /////////////////////
			
			
			switch (_reportType) {
			case 0: //coverPage
				pageInfoData = new HashMap<String, Object >();
				
				cloneX = (_pagePrintMarginLeft > 0)?_pagePrintMarginLeft:0;
				cloneY = (_pagePrintMarginTop > 0)?_pagePrintMarginTop:0;
				
				//pageInfoData.put("pageData", mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam , 0 , _docTotalPage , _argoPage - _argoDocCnt) );
				pageInfoData.put("pageData", mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam , 0 , _docTotalPage , _argoPage - _argoDocCnt, cloneX , cloneY ) );
				pageInfoData.put("page", _pageHash);
				
				// Section Page 지정
				mFunction.setSectionCurrentPageNum(0);
				mFunction.setSectionTotalPageNum(1);
				
				mFunction.setCloneIndex( 0 );
				
				pages.put( String.valueOf(_argoPage), pageInfoData);
				
				// 1 Page send to client
				formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));			
				this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT", "99999", "Make Print Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));				
				
				_argoPage = _argoPage + 1;
				
				//20160525 초기화 최명진
				pages.clear();
				pageInfoData = null;
				_objects = null;
				
				break;
			case 1: //freeform
			case 7: //mobile타입
			case 9: //webPage
				_pageCnt = mPageNumList.get(i);
				
				int _start = 0;
				int _max = 0;

//				for ( j = 0; j < _pageCnt; j++) {
				for ( j = _startIndex; j < _pageMaxCnt; j++) {
					
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
						
						if( _pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
						if( _pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;
						

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
							Element _chkPage = mPageAr.get(_pageContinuePageCnt);
							
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
					formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));			
					this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT", "99999", "Make Print Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));				
					
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
					
					if( _pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
					if( _pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;

					_objects = masterParser.createMasterBandData(j, masterList, pageWidth, pageHeight, mParam, cloneX, cloneY, _objects , _docTotalPage, _argoPage - _argoDocCnt );
					
					if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1)|| ( !isConnectGroupPage && (j == _masterTotPage-1 )) || (isLastConnectGroupPage && (j == _masterTotPage-1 ) ) )
					{
						pageInfoData.put("pageData", _objects );
						pageInfoData.put("page", _pageHash);
						
						pageInfoData.put("pageReqData", _reqlist );
						pageInfoData.put("pageTIdxData", _tabIndexlist );
						
						pages.put( String.valueOf(_argoPage), pageInfoData);
						
						// 1 Page send to client
						formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));	
						this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT", "99999", "Make Print Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));				
						
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
				
				ContinueBandParser continueBandParser  = (ContinueBandParser) _pageInfoClassArrayList.get(i);
				continueBandParser.setImageData(this.mImgData);
				continueBandParser.setChartData(this.mChartData);
				continueBandParser.setFunction(mFunction);
				ArrayList<Object> bandAr = (ArrayList<Object>) _pageInfoArrayList.get(i);

				HashMap<String, BandInfoMapData> bandInfo = (HashMap<String, BandInfoMapData>) bandAr.get(0);
				ArrayList<BandInfoMapData> bandList =(ArrayList<BandInfoMapData>) bandAr.get(1);
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
				HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
				
				// group
				HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
				ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
				
				continueBandParser.mOriginalDataMap = originalDataMap;
				continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
				
//				ArrayList<Object> _pageInfoREQList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_REQ_LIST");
//				ArrayList<Object> _pageInfoTIDXList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_TIDX_LIST");
				_reqlist = new ArrayList<Object>();
				_tabIndexlist = new ArrayList<Object>();
				
				if(  bandAr.size() > 8 )
				{
					mFunction.setGroupBandCntMap((HashMap<String, Integer>) bandAr.get(8));
				}
				
				boolean isPivot = false;
				
				if( _objects == null )_objects = new ArrayList<HashMap<String,Object>>();
				
				if(_page.getAttribute("isPivot").equals("true"))
				{
					isPivot = true;
					// Project info send
					_pageHash.put("height", String.valueOf( Float.valueOf( _page.getAttribute("width") )  + _pagePrintMarginLeft + _pagePrintMarginRight ) );
					_pageHash.put("width", String.valueOf( Float.valueOf( _page.getAttribute("height") )  + _pagePrintMarginTop + _pagePrintMarginBottom )  );
				}
				
				_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
				
				int _pagesRowListSize = pagesRowList.size();
				float _bandMaxWidth = 0;
				
				if( _page.getAttribute("fitOnePage") != null && _page.getAttribute("fitOnePage").equals("true"))
				{
					continueBandParser.setFitOnePage( true );
					if(continueBandParser.getFitOnePageHeight() > 0 ) _pageHash.put("height", String.valueOf(continueBandParser.getFitOnePageHeight()) );
				}
								
				// 페이지 범위 지정
				_pageStartCnt   = _startIndex * _cloneRepCnt;
				_pageMaxinumCnt = _pageStartCnt + ( ( _pageMaxCnt - _startIndex ) * _cloneRepCnt);
				if( _pageMaxinumCnt > _pagesRowListSize ) _pageMaxinumCnt = _pagesRowListSize;
				
				for ( j = _pageStartCnt; j < _pageMaxinumCnt; j++) {
//				for ( j = 0; j < _pagesRowListSize; j++) {
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
						_pageHash.put("width", String.valueOf( Float.valueOf( _page.getAttribute("width") )  + _pagePrintMarginLeft + _pagePrintMarginRight ) );
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
						//_clonePositionList = ItemConvertParser.getClonePosition( _pageRepeatCnt, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
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

					_objects = continueBandParser.createContinueBandItems(j, _tempDataSet, bandInfo, bandList, pagesRowList, mParam, crossTabData,cloneX,cloneY,_objects,_docTotalPage, _argoPage - _argoDocCnt , isPivot);
					
					_bandMaxWidth = _bandMaxWidth + continueBandParser.getBandMaxWidth();
					
					if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) || ( !isConnectGroupPage && (j == _pagesRowListSize-1 ) )  || (isLastConnectGroupPage && (j == _pagesRowListSize-1 )) )
					{
						if( Float.valueOf( _pageHash.get("width") )+ _pagePrintMarginLeft + _pagePrintMarginRight  < _bandMaxWidth )
						{
							_pageHash.put("width", Float.valueOf(_bandMaxWidth ).toString() );
						}
						
						pageInfoData.put("pageData", _objects );
						pageInfoData.put("page", _pageHash);
						
						pageInfoData.put("pageReqData", _reqlist );
						pageInfoData.put("pageTIdxData", _tabIndexlist );
						
						pages.put( String.valueOf(_argoPage), pageInfoData);
						
						// 1 Page send to client
						formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));	
						this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT", "99999", "Make Print Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));				
						
						_argoPage = _argoPage + 1;
					}
				}
				
				if( !clonePage || ( !isConnectGroupPage || _pageRepeatCnt == (_cloneRepCnt-1) ) )
				{
					//20160525 초기화 최명진
					pageInfoData = null;
					_objects.clear();
					pagesRowList.clear();
					pages.clear();
				}
				
				if(isConnectGroupPage)
				{
					_clonePageCnt = _clonePageCnt + _pagesRowListSize;
				}
				continueBandParser = null;
				bandAr = null;
				bandInfo = null;
				bandList = null;
				crossTabData = null;
				
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
					cloneX = (_pagePrintMarginLeft > 0)?_pagePrintMarginLeft:0;
					cloneY = (_pagePrintMarginTop > 0)?_pagePrintMarginTop:0;
					
					pageInfoData = new HashMap<String, Object >();
					pageInfoData.put("pageData", mCreateFormFn.CreateLabelBandAll(_page , _tempDataSet , mParam ,j,_docTotalPage , _argoPage - _argoDocCnt, cloneX, cloneY ) );
					pageInfoData.put("page", _pageHash);
					
					pages.put( String.valueOf(_argoPage), pageInfoData);
					
					// 1 Page send to client
					formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));						
					this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT", "99999", "Make Print Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));				
					
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
				pageInfoData = new HashMap<String, Object >();
				cloneX = (_pagePrintMarginLeft > 0)?_pagePrintMarginLeft:0;
				cloneY = (_pagePrintMarginTop > 0)?_pagePrintMarginTop:0;
				
				pageInfoData.put("pageData", mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet, mParam , 0, _docTotalPage , _argoPage - _argoDocCnt, cloneX , cloneY ) );
				pageInfoData.put("page", _pageHash);
				pages.put( String.valueOf(_argoPage), pageInfoData);
				
				// Section Page 지정
				mFunction.setSectionCurrentPageNum(0);
				mFunction.setSectionTotalPageNum(1);
				mFunction.setCloneIndex(0);
				
				// 1 Page send to client
				formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));
				this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT", "99999", "Make Print Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));				
				
				_argoPage = _argoPage + 1;
				
				//20160525 초기화 최명진
				pages.clear();
				pageInfoData = null;
				_objects = null;
				
				break;
			case 12:	//ConnectLink
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
					cloneX = (_pagePrintMarginLeft > 0)?_pagePrintMarginLeft:0;
					cloneY = (_pagePrintMarginTop > 0)?_pagePrintMarginTop:0;
					
					_objects = new ArrayList<HashMap<String,Object>>();
//					connectLink.makeConnectPage(j, connectData.get("pageInfoData"), _objects,_docTotalPage, _argoPage - _argoDocCnt );
					connectLink.makeConnectPage(j, connectData.get("pageInfoData"), _objects,_docTotalPage, _argoPage - _argoDocCnt, cloneX, cloneY );
					
					pageInfoData = new HashMap<String, Object >();  
					pageInfoData.put("pageData", _objects );
					pageInfoData.put("page", _pageHash);
					pages.put( String.valueOf(_argoPage), pageInfoData);
					
					// 1 Page send to client
					formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));
					this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT", "99999", "Make Print Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));				
					
					_argoPage = _argoPage + 1;
				}
				
				//20160525 초기화 최명진
				connectLink = null;
				pages.clear();
				pageInfoData = null;
				_objects = null;
				connectData = null;
				break;
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
				
				cloneData = _page.getAttribute("divide");
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
					
                    if( _pagePrintMarginLeft > 0 ) cloneX = cloneX + _pagePrintMarginLeft;
					if( _pagePrintMarginTop > 0 ) cloneY = cloneY + _pagePrintMarginTop;

					_objects = linkedParser.createLinkedPageItem(j, retArr, newDataSet, mParam, cloneX, cloneY, _objects, _docTotalPage,_argoPage - _argoDocCnt );
					
					if( !clonePage || j%_cloneRepCnt == (_cloneRepCnt-1) || (j == _totPage-1) )
					{
						pageInfoData.put("pageData", _objects );
						pageInfoData.put("page", _pageHash);
						
						pages.put( String.valueOf(_argoPage), pageInfoData);
						
						// 1 Page send to client
						formfile.addPageB64(_argoPage, ("{\"pageInfo\":" + JSONObject.toJSONString(pages) + "}").getBytes("UTF-8"));
						this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT", "99999", "Make Print Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));				
						
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
			case 16:
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
			default:
				
				break;
			}
		}
		if(!Log.printStop){
			
			WriteChartData( this.mChartData );
			WriteImageData( this.mImgData );				
		}		
		
		//변수 clear
		_totPageCheckParser = null;
		_pageInfoMap.clear();
		mPageAr.clear();
		mPageNumList.clear();
		mPageNumRealList.clear();
		_pageInfoArrayList = null;
		_pageInfoClassArrayList = null;
		mDataSet = null;
		
		
		Date _chkDate = new Date();  		
		log.debug("[" + _client_ssid + "] FORM PARSING COMPLETE  : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_chkDate) +"]" );
		
		if(!Log.printStop){	
			this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPRINT_END", Log.MSG_LP_END_MAKEPRINT, Log.getMessage(Log.MSG_LP_END_MAKEPRINT)));
		}
		
		// 페이지들의 배열이 담긴 결과값 리턴
		//mHashMap.put("pageDatas", hashmapToJsonStr(pages) );
		//oService.end();
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
			getExportAllPages(true, null, bSupportIE9, _changeItemList);		//TEST
			
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
	
	
	protected void testPdfChangeMapping( HashMap<String, Object> _change)
	{
		HashMap<String, HashMap<String, ArrayList<HashMap<String,Object>>>> _orignal  = (HashMap<String, HashMap<String, ArrayList<HashMap<String,Object>>>>) mHashMap.get("pageDatas");
		HashMap<String, ArrayList<HashMap<String, Object>>> _pageData;
		HashMap<String, ArrayList<HashMap<String, Object>>> _changePageData;
		ArrayList<HashMap<String, Object>> _itemList;
		ArrayList<HashMap<String, Object>> _changeItemList;
		HashMap<String, Object> _oItem;
		HashMap<String, Object> _cItem;

		for (String _key : _change.keySet()) 
		{
			// change Data
			_changePageData = (HashMap<String, ArrayList<HashMap<String,Object>>>) _change.get(_key);
			_changeItemList = _changePageData.get("Controls");

			// orignal Data
			_pageData = _orignal.get(_key);
			_itemList = _pageData.get("pageData");
			
			String _oID = "";
			String _oClassName = "";

			String _cID = "";
			for (int j = 0; j < _changeItemList.size(); j++) 
			{
				_cItem = _changeItemList.get(j);
				_cID = (String) _cItem.get("CtrlId");

				for (int i = 0; i < _itemList.size(); i++)
				{
					_oItem = _itemList.get(i);
					_oID = (String) _oItem.get("id");
					_oClassName = (String) _oItem.get("className");					

					if( _oClassName.equals("UBSignature") || _oClassName.equals("UBPicture") || _oClassName.equals("UBCheckBox") || _oClassName.equals("UBTextSignature") )		
					{
						if( _oID.equals(_cID))
						{
							String _cData = (String) _cItem.get("Value");

							if( _oClassName.equals("UBSignature") )
							{ 
								_oItem.put("src", _cData.substring( _cData.indexOf(",") + 1 , _cData.length()));
								break;
							}
							else if( _oClassName.equals("UBPicture") )
							{ 
								_oItem.put("src", _cData.substring( _cData.indexOf(",") + 1 , _cData.length()));
								break;
							}
							else if( _oClassName.equals("UBTextSignature") )
							{ 
								_oItem.put("src", _cData.substring( _cData.indexOf(",") + 1 , _cData.length()));
								break;
							}
							/*
							else if( _oClassName.equals("UBTextInput") )
							{
								_oItem.put("text", _cData);
								break;
							}
							else if( _oClassName.equals("UBTextArea") )
							{
								_oItem.put("text", _cData);
								break;
							}
							else if( _oClassName.equals("UBRadioBorder") )
							{
								_oItem.put("selected", _cData);
								break;
							}
							else if( _oClassName.equals("UBCheckBox") )
							{
								_oItem.put("selected", _cData);
								break;
							}
							*/
						} // if id 비교
					} // if className 체크
//					else if( _oItem.containsKey("editable") &&  ValueConverter.getBoolean(_oItem.get("editable")) )  		// 임시로 주석 처리 ( 아이템에 editable속성은 없음 ) 
					else
					{
						if( _oID.equals(_cID))
						{
							//String _cData = (String) _cItem.get("Value");
							//_oItem.put("text", _cData);
							break;
						}
					}
				}// for orignal ItemList
			} // for changeItemList
		}
	}
	
	
	// 아이템 스타일 속성들 정의. word, excel에서 공통으로 사용.
	/**
	 * Item Text Properties 
	 * */
	public enum EVerticalAlign{ top, middle, bottom};
	/**
	 * Item Text Properties 
	 * */
	public enum ETextAlign{ left, center, right};
	/**
	 * Item Text Properties 
	 * */
	public enum EBorderType{ solid, dot, dash, dash_dot, dash_dot_dot, SOLD, dbl, thick, medium, m_dash, m_dash_dot, m_dash_dot_dot, none };
	/**
	 * Item Text Properties 
	 * */
	public enum ETextDecoration{ none, normal, underline};
	/**
	 * Item Text Properties 
	 * */
	public enum EFontStyle{ italic, normal};
	/**
	 * Item Text Properties 
	 * */
	public enum EFontWeight{ bold, normal};
	
	/**
	 * Item Start/End ButtonType Properties 
	 * */
	public enum EButtonType{ circle, rectagle, tryangle};
	
	// -------- 페이지 분할 전송을 위해 필요한 변수들을 전역변수로 변경함.
	protected Workbook wb = null;
//	private ubFormToExcel _ubFormToExcel = null;
	protected ubFormToExcelBase _ubFormToExcel = null;
	public Workbook excelExportManager(String _xml , HashMap _param, String _fileName , ubFormToExcelBase _ubfExcelBase, Workbook _workBook ) throws Exception
	{
		Object clientEditMode = _param.get("CLIENT_EDIT_MODE");
		log.debug(getClass().getName() + "::" + "Call excelExportManager...clientEditMode=" + clientEditMode);
		
//		Date start = new Date();

		// 초기화
		resetVariable();
		
		mSERVER_URL = (String) _param.get("serverUrl");		
		
		mCLIENT_IP = (String) _param.get("CLIENT_IP");
		mPAGE_NUM = (Integer) _param.get("PAGE_NUM");

		Object loadType = _param.get("LOAD_TYPE");
		
		boolean bSupportIE9 = false;
		if( loadType != null && loadType.equals("div") )
		{
			bSupportIE9 = true;
		}
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _changeDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>)_param.get("CHANGE_DATASET");
		HashMap<String, Object> _changeItemList = (HashMap<String, Object>) _param.get("CHANGE_ITEM_DATA");

		Object _params = _param.get("PARAMS");
		if( _params.hashCode() != 0 )
		{
			//mParam = _jsonUtils.jsonToMap((String) _params);
		 	Object ubObj = JSONValue.parseWithException((String)_params);	
		 	mParam = (JSONObject)ubObj;
		}

		// xml 을 document 형태로 변경.
		// Project 중 필요한 속성을 담음
		xmlToDocument(_xml);
		
		if("ON".equals(clientEditMode) && _changeDataSet != null)	
		{
			log.debug(getClass().getName() + "::" + "Call excelExportManager..._changeDataSet != null");
			mDataSet = _changeDataSet;
		}
		else
		{
			log.debug(getClass().getName() + "::" + "Call excelExportManager..._changeDataSet == null");
			getXmlToDataSet();
//			xmlToDataSet();		// 여러건의 Form의 데이터 셋을 로드하기 위하여 지정
		}		
		
		if( !mParamResult )
		{
			
			HashMap<String, Object> _pageInfoMap = null;
			ArrayList<Element> _pageElements = new ArrayList<Element>();
			ArrayList<Float> _pageMarginList = new ArrayList<Float>();
			Document _doc = null;
			NodeList pages = null;
			int i = 0;
			int j = 0;
			int _docPagesSize = 0;

			String xlsFmtOption = String.valueOf(_param.get("EXCEL_FMT_OPTION"));
			String xlsOption = String.valueOf(_param.get("EXCEL_OPTION"));
			String excelSheetSplitType = String.valueOf(_param.get("EXCEL_SHEET_SPLIT_TYPE"));
			String setExcelSheetNames = String.valueOf(_param.get("EXCEL_SHEET_NAMES"));
			log.debug(getClass().getName() + "::" + "Call excelExportManager...EXCEL_FMT_OPTION=" + xlsFmtOption);

			// Connect Page를 처리하기 위하여 미리 총페이지를 구한후 document들의 page를 가져와서 처리
			//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(this.mServiceReqMng, m_appParams);
			//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(m_appParams);
			TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );

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
			   _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
			}
			
			if( _ubfExcelBase == null )
			{
				if(xlsFmtOption.equals("EXCEL2007"))
				{
					_ubFormToExcel = new ubFormToExcel2007();
					_ubFormToExcel.setmImageDictionary(mImageDictionary);
					wb = new SXSSFWorkbook(100);
				}
				else if(xlsFmtOption.equals("EXCEL97"))
				{
					_ubFormToExcel = new ubFormToExcel97();
					wb = new HSSFWorkbook();
				}
			}
			else
			{
				_ubFormToExcel = _ubfExcelBase;
				wb = _workBook;
			}
			
			_ubFormToExcel.setExcelSheetSplitType(excelSheetSplitType);
			_ubFormToExcel.setExcelSheetNames(setExcelSheetNames);
			
			_ubFormToExcel.setExcelFileName(_fileName);
			
			// 문서 그대로.
			if( xlsOption.equals("NORMAL") ||  xlsOption.equals("BAND")  ){
				
				/**X배열 생성 해서 세팅.
				ArrayList<Integer> mXAr = (ArrayList<Integer>)_pageInfoMap.get("EXCEL_X_ARR");
				ArrayList<Integer> xAr = makeXArray(_pageElements, _pageMarginList, mXAr);
				**/
				if( xlsOption.equals("BAND") )
				{
					_ubFormToExcel.setMaxSheetSize(-1);			// sheet의 size를 -1로 지정하여 밴드가 한페이지에 모두 표현이 되도록 처리
//					_ubFormToExcel.setMaxSheetSize(3000);		// sheet의 size를 -1로 지정하여 밴드가 한페이지에 모두 표현이 되도록 처리
					_ubFormToExcel.setUseRepeatValue(true); 	// 밴드의 repeatValue를 처리하기 위하여 true로 지정
					_ubFormToExcel.setIsExcelOption(xlsOption);
				}
				
				ArrayList<ArrayList<Integer>> _docXArr =  _ubFormToExcel.getDocumentXArray(documents, mDocument, _pageInfoMap, dataSets, mDataSet, documentInfos );
				_ubFormToExcel.setDcoXArray( _docXArr );
				
				// 아이템 데이타 추출. - 엑셀의 경우는 페이지 별로 바로 엑셀 생성함.
				getExportAllPages(true, _pageInfoMap, bSupportIE9, _changeItemList);
				
				if( _ubfExcelBase == null && mImageDictionary != null )
				{
					clearImageDictionary();
				}
			}
			// 데이터만.
			else if( xlsOption.equals("DATA_ONLY")){
				HashMap<String, HashMap<String, ArrayList<Object>>> datasetMap = null;
				
				datasetMap = makeExcelDataOnly();
				wb = _ubFormToExcel.xmlParsingExcel(datasetMap);
			}
			
		}
		_ubFormToExcel = null;
		
		return wb;
	}
	
	/**
	 * <pre>
	 * 엑셀 데이터만 내보내기 를 위한 데이터 파싱.
	 * 1. XML의 아이템 정보에서 사용된 데이터셋과 컬럼 정보를 추출한다.
	 * 2. mDataSet에서 dsColMap 의 키 값과 동일한 dataset id 를 가진 데이터를 불러온다.
	 * 3. 사용된 컬럼들의 값 배열을 가지고, 데이터를 뽑아낸다.
	 * 4. 결과 데이터 생성.
	 * 		[ {dsId : { "header":[ colVal, val,val], "data":[ [val,val,val],[val,val,val] ]}}
	 * 		, {dsId : { "header":[ colVal, val,val], "data":[ [val,val,val],[val,val,val] ]}}]
	 * </pre> 
	 * @return [ {dsId : { "header":[], "data":[ [],[] ]}} , {dsId : { "header":[], "data":[ [],[] ]}}]
	 * */
	private HashMap<String, HashMap<String, ArrayList<Object>>> makeExcelDataOnly() throws Exception {
		HashMap<String, HashMap<String, ArrayList<Object>>> datasetMap = new HashMap<String, HashMap<String,ArrayList<Object>>>();
		ArrayList<Object> resultDataAr = null;
		
		Element projectNode = mDocument.getDocumentElement();
		String reportType = "";
//		reportType = String.valueOf(projectNode.getAttribute("reportType"));
		reportType = String.valueOf(projectNode.getAttribute("projectType"));
		
		String MASTERBAND = "2";
		String CONTINUEBAND = "3";
		String LINKFORM = "12";
		String MULTIFORMBAND = "13";
		
		// Band Type인 경우, Grouping 된 데이터를 받아와야 함.
		if( MASTERBAND.equals(reportType) || CONTINUEBAND.equals(reportType) || LINKFORM.equals(reportType) || MULTIFORMBAND.equals(reportType) ){
			
			log.debug(getClass().getName()+"::"+"makeExcelDataOnly::"+"Make band item's dataset.");
			
			// mPageAr 이 null임
			// mDocument 의  pages
			NodeList _nodes = mDocument.getElementsByTagName("page");
			ArrayList<Element> _pagesList = new ArrayList<Element>();
			for (int i = 0; i < _nodes.getLength(); i++) {
				_pagesList.add((Element) _nodes.item(i)); 
			}
			
			//datasetMap =  UBIDataUtilPraser.getAllExportDatas(this.mServiceReqMng, _pagesList, mDataSet, m_appParams, mParam, mFunction );
			datasetMap =  UBIDataUtilPraser.getAllExportDatas(this.mSERVER_URL, _pagesList, mDataSet, m_appParams, mParam, mFunction );

		}
		// 아닌 경우.
		else{
			
			if(mDataSet == null){
				log.debug(getClass().getName()+"::"+"makeExcelDataOnly::"+"mDataSet is empty.");
				return null;
			}
			
			log.debug(getClass().getName()+"::"+"makeExcelDataOnly::"+"Serach item's dataset info from pure xml.");
			
			// key : Dataset Id, Value : Columns.
			HashMap<String, ArrayList<Object>> dsColMap = new HashMap<String, ArrayList<Object>>();
			
			NodeList itemList = mDocument.getElementsByTagName("item");
			HashMap<String, HashMap<String, String>> fieldHeaderMap = new HashMap<String, HashMap<String,String>>();
			for (int i = 0; i < itemList.getLength(); i++) {
				Element itemEl = (Element)itemList.item(i);
				String itemClassName = itemEl.getAttribute("className");
				
				if( itemClassName.equals("") || itemClassName.equals("null") ) continue;
				
				// Dataset Item.
				if( itemClassName.contains("Dmc") ){
					// 데이터 필드명과 헤더명을 맵에 저장해두었다가 추후에 col id 대신 헤더명으로 변경한다.
					String itemId = itemEl.getAttribute("id");
					NodeList columnNodeList = itemEl.getElementsByTagName("column");
					HashMap<String, String> fieldHeader = new HashMap<String, String>();
					for (int j = 0; j < columnNodeList.getLength(); j++) {
						Element colNode = (Element)columnNodeList.item(j);
						NodeList propNode = colNode.getElementsByTagName("property");
						String fieldVal = "";
						String headerVal = "";
						for (int k = 0; k < propNode.getLength(); k++) {
							NamedNodeMap itemPropNode = propNode.item(k).getAttributes();
							
							if( itemPropNode.getNamedItem("name").getNodeValue().equals("dataField") ){
								fieldVal = itemPropNode.getNamedItem("value").getNodeValue();
							}
							else if( itemPropNode.getNamedItem("name").getNodeValue().equals("header") ){
								headerVal = itemPropNode.getNamedItem("value").getNodeValue();
							}
						}
						fieldHeader.put(fieldVal, headerVal);
					}
					fieldHeaderMap.put(itemId, fieldHeader);
					continue;
				}// dataSet end
				
				// 차트 아이템은 사용된 데이터셋의 전체 컬럼을 모두 가져온다.
				else if( itemClassName.contains("Chart") ){
					NodeList itemPropList = itemEl.getElementsByTagName("property");
					
					// property name=dataSet 을 찾는다.
					for (int j = 0; j < itemPropList.getLength(); j++) {
						NamedNodeMap itemPropNode = itemPropList.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						propVal = URLDecoder.decode(propVal, "UTF-8");
						if( !propVal.equals("null") && propName.equals("dataSet")){
							ArrayList<HashMap<String, Object>> mDataValue = mDataSet.get(propVal);
							if( mDataValue != null && mDataValue.size()>0 ){
								ArrayList<Object> cols = new ArrayList<Object>(mDataValue.get(0).keySet());
								dsColMap.put(propVal, cols);
							}
							break;
						}
					}// item prop Node List For
					
				}// chart end.
				
				// item > cell > property[dataSet, column]
				else if( itemClassName.equals("UBTable") || itemClassName.equals("UBApproval") ){
					
					NodeList cellNodeList = itemEl.getElementsByTagName("cell");
					
					int cellLen = cellNodeList.getLength();
					for (int cIdx = 0; cIdx < cellLen ; cIdx++) {
						Element cellEl = (Element)cellNodeList.item(cIdx);
						NodeList itemPropList = cellEl.getElementsByTagName("property");
						
						String dsId = "";
						String colId = "";
						int propLen = itemPropList.getLength();
						for (int pIdx = 0; pIdx < propLen ; pIdx++) {
							
							NamedNodeMap itemPropNode = itemPropList.item(pIdx).getAttributes();
							Node namedItem = itemPropNode.getNamedItem("name");
							if( namedItem == null ) continue;
							String propName = namedItem.getNodeValue();
							namedItem = itemPropNode.getNamedItem("value");
							if( namedItem == null ) continue;
							String propVal = namedItem.getNodeValue();
							
							
							if( !propVal.equals("null") ){
								if(propName.equals("dataSet")){
									dsId = URLDecoder.decode(propVal, "UTF-8");
									if( !colId.equals("") ) break;
								}
								else if(propName.equals("column")){
									colId = URLDecoder.decode(propVal, "UTF-8");
									if( !dsId.equals("") ) break;
								}
							}
						}// prop For End.
						
						// item prop Node List For
						
						if( !( dsId.equals("") || colId.equals("")) ){
							if( dsColMap.containsKey(dsId) ){
								ArrayList<Object> cols = dsColMap.get(dsId);
								if( !cols.contains(colId) ){
									cols.add(colId);
									dsColMap.put(dsId, cols);
								}
							}
							else{
								ArrayList<Object> cols = new ArrayList<Object>();
								cols.add(colId);
								dsColMap.put(dsId, cols);
							}
							
						}
					}// cell For End.
					
				}// table End.
				
				// item > [ property:dataSet, column > property[dataField] ]
				else if( itemClassName.equals("UBDataGrid") ){
					NodeList itemPropList = itemEl.getElementsByTagName("property");

					String dsId = "";
					
					// 1. property name=dataSet 을 찾는다.
					for (int j = 0; j < itemPropList.getLength(); j++) {
						if( itemPropList.item(j) == null ) continue;
						
						NamedNodeMap itemPropNode = itemPropList.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						
						if( !propVal.equals("null") && propName.equals("dataSet")){
							dsId = propVal;
							break;
						}
					}
					
					ArrayList<Object> cols = new ArrayList<Object>();
					if( dsColMap.containsKey(dsId) ){
						cols = dsColMap.get(dsId);
					}
					
					// 2. column 태그 별 property 에서 dataField(=column)를 찾는다.
					if( !dsId.equals("") ){
						NodeList columnNodeList = itemEl.getElementsByTagName("column");
						
						int colLen = columnNodeList.getLength();
						for (int cIdx = 0; cIdx < colLen ; cIdx++) {
							Element colEl = (Element)columnNodeList.item(cIdx);
							NodeList colPropList = colEl.getElementsByTagName("property");
							
							int propLen = colPropList.getLength();
							for (int pIdx = 0; pIdx < propLen ; pIdx++) {
								if( columnNodeList.item(pIdx) == null ) continue;
								NamedNodeMap itemPropNode = columnNodeList.item(pIdx).getAttributes();
								String propName = itemPropNode.getNamedItem("name").getNodeValue();
								String propVal = itemPropNode.getNamedItem("value").getNodeValue();
								
								if( !propVal.equals("null") && propName.equals("dataField")){
									if( !cols.contains(propVal) ) cols.add(propVal);
								}
							}// prop For End.
							
						}// column For End.
						
						dsColMap.put(dsId, cols);
					}
				}// dataGrid end.
				
				// the others...
				else{
					NodeList itemPropList = itemEl.getElementsByTagName("property");/* itemList.item(i).getChildNodes();*/
					
					String dsId = "";
					String colId = "";
					
					for (int j = 0; j < itemPropList.getLength(); j++) {
						NamedNodeMap itemPropNode = itemPropList.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						
						if( !propVal.equals("null") ){
							if( propName.equals("dataSet")){
								dsId = propVal;
								if( !colId.equals("") ) break;
							}
							else if( propName.equals("column")){
								colId = propVal;
								if( !dsId.equals("") ) break;
							}
						}
						
					}// item prop Node List For
					
					// dataset id 와 column id 를 HashMap 에 저장.
					if( !( dsId.equals("") || colId.equals("")) ){
						
						if( dsColMap.containsKey(dsId) ){
							ArrayList<Object> cols = dsColMap.get(dsId);
							if( !cols.contains(colId) ){
								cols.add(colId);
								dsColMap.put(dsId, cols);
							}
						}
						else{
							ArrayList<Object> cols = new ArrayList<Object>();
							cols.add(colId);
							dsColMap.put(dsId, cols);
						}
						
					}
				}// other items end.
				
			}// item Node List For
			
			
			// 세팅된 dsId, cols 정보를 바탕으로 데이터 추출.
			for (String dsId : dsColMap.keySet()){
				if( mDataSet.containsKey(dsId) ){
					// 현재 데이터셋에서 사용된 컬럼들의 아이디 값들.
					ArrayList<Object> usedColAr = dsColMap.get(dsId);
					ArrayList<Object> headers = new ArrayList<Object>();
					// key : col id, val : data 
					// sample: [{"col_4":"1","col_3":"1","col_2":"1","col_1":"1","col_0":"1"}, {"col_4":"1","col_3":"1","col_2":"1","col_1":"1","col_0":"1"}]
					ArrayList<HashMap<String, Object>> mDataValue = mDataSet.get(dsId);
					
					resultDataAr = new ArrayList<Object>();
					
					for (int j = 0; j < mDataValue.size(); j++) {
						HashMap<String, Object> rowData = mDataValue.get(j);
						// 데이터만 담을 배열.
						ArrayList<String> rowVal = new ArrayList<String>();
						for (Object usedCol : usedColAr) {
							if( j == 0 ){
								// colid를 header 로 변경.
								Object headStr = fieldHeaderMap.get(dsId).get(String.valueOf(usedCol));
								headers.add(headStr);
							}
							rowVal.add( String.valueOf(rowData.get(String.valueOf(usedCol))) );
						}
						resultDataAr.add(rowVal);
					}// mDataValue For
					
					HashMap<String, ArrayList<Object>> resultMap = new HashMap<String, ArrayList<Object>>();
					

					resultMap.put("header", headers);
					resultMap.put("data", resultDataAr);
					
					datasetMap.put(dsId, resultMap);
				}
				
			}// dsColMap For
			
			log.info(getClass().getName()+"::"+"makeExcelDataOnly::"+"Make result data completely!");
		}
		
		return datasetMap;
	}
	
	/**
	 * <pre>
	 * XML 정보에서 x 좌표를 뽑아 xArrayGlobal 에 저장한다.
	 * For Excel Export : 페이지 별 분할전송으로 변경되면서 X좌표 계산 부분이 중복되어 이 방식으로 변경함.
	 * 2015-12-02
	 * </pre>
	 * */
	private ArrayList<Integer> makeXArray(ArrayList<Element> pages, ArrayList<Float> _pageMarginX, ArrayList<Integer> mXArr ) {

		log.info(getClass().getName() + "::" + "Call xmlToUbForm makeXArray()...");
		
		ArrayList<Integer> xAr = new ArrayList<Integer>();
		ArrayList<Float> xArFloat = new ArrayList<Float>();
		ArrayList<Integer> cloneXAr = new ArrayList<Integer>();
		
		if(mXArr.size() > 0)
		{
			xAr = mXArr;
			if(xAr.contains(0) == false )xAr.add(0);
		}
		else
		{
			xAr.add(0);
			xArFloat.add(0f);
		}
		
		int _pageCnt = pages.size();
		float _pageXPosition = 0f;
		int _cloneCountArgo = 1;
		boolean _isConnect = false;
		//NodeList pages = mDocument.getElementsByTagName("page");
		
//		for (Element _page : mPageAr) {
//		for (int i2 = 0; i2 < pages.getLength(); i2++) {
		for (int i2 = 0; i2 < _pageCnt; i2++) {
//			Element _page =(Element) pages.item(i2);
			Element _page = pages.get(i2);
			NodeList _items = _page.getElementsByTagName("item");
			int itemLen = _items.getLength();
			
			if( _page.getAttribute("isConnect") != null &&  _page.getAttribute("isConnect").equals("true"))
			{
				_isConnect = true;
			}
			_pageXPosition = _pageMarginX.get(i2);
			
			ArrayList<Integer> _xAr = new ArrayList<Integer>();

			ArrayList<Float> _xArOri = new ArrayList<Float>();
			
			// label band 에 필요한 속성을 담을 맵.
			HashMap<String, Integer> labelBandProp = null;
			HashMap<String, HashMap<String, String>> bandData = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> bandInfo;
			for (int i = 0; i < itemLen; i++) {
				Element _item = ((Element) _items.item(i));
				String className = _item.getAttribute("className");
				
				if(className.indexOf("Band") != -1)
				{
					if( "UBLabelBand".equals(className) )
					{
						NodeList _props = _item.getElementsByTagName("property");
						int propLen = _props.getLength();
						labelBandProp = new HashMap<String, Integer>();
						// property
						for (int j = 0; j < propLen; j++) {
							NamedNodeMap itemPropNode = _props.item(j).getAttributes();
							
							if( itemPropNode.getNamedItem("name") == null ) continue;
							
							String propName = itemPropNode.getNamedItem("name").getNodeValue();
							String propVal = itemPropNode.getNamedItem("value").getNodeValue();
							if( propName.equals("columns") && !propVal.equals("null") && !propVal.equals("") ){
								labelBandProp.put("columns", Integer.parseInt(propVal));
							}
							else if( propName.equals("border_x") && !propVal.equals("null") && !propVal.equals("") ){
								labelBandProp.put("border_x", Integer.parseInt(propVal));
							}
							else if( propName.equals("border_width") && !propVal.equals("null") && !propVal.equals("") ){
								labelBandProp.put("border_width", Integer.parseInt(propVal));
							}
						}// item prop Node List For
					}
					else
					{
						NodeList _props = _item.getElementsByTagName("property");
						int propLen = _props.getLength();
						bandInfo = new HashMap<String, String>();
						// property
						for (int j = 0; j < propLen; j++) {
							NamedNodeMap itemPropNode = _props.item(j).getAttributes();
							if( itemPropNode.getNamedItem("name") == null ) continue;
							
							String propName = itemPropNode.getNamedItem("name").getNodeValue();
							String propVal = itemPropNode.getNamedItem("value").getNodeValue();
							if( propName.equals("useLabelBand") && !propVal.equals("null") && !propVal.equals("") ){
								bandInfo.put("useLabelBand", propVal);
							}
							else if( propName.equals("labelBandColCount") && !propVal.equals("null") && !propVal.equals("") ){
								bandInfo.put("labelBandColCount", propVal);
							}
							else if( propName.equals("labelBandDisplayWidth") && !propVal.equals("null") && !propVal.equals("") ){
								bandInfo.put("labelBandDisplayWidth", propVal);
							}
						}// item prop Node List For
						if(bandInfo.containsKey("useLabelBand") && bandInfo.get("useLabelBand").equals("true"))
						{
							bandData.put(_item.getAttribute("id").toString(), bandInfo);
						}
					}
					continue;
				}
				
				// 라벨 밴드면 columns, border_x, border_width 값을 저장해 두었다가, 컬럼수 만큼 x 값을 추가함.
				if( "UBLabelBand".equals(className) ){
					NodeList _props = _item.getElementsByTagName("property");
					int propLen = _props.getLength();
					labelBandProp = new HashMap<String, Integer>();
					// property
					for (int j = 0; j < propLen; j++) {
						NamedNodeMap itemPropNode = _props.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						if( propName.equals("columns") && !propVal.equals("null") && !propVal.equals("") ){
							labelBandProp.put("columns", Integer.parseInt(propVal));
						}
						else if( propName.equals("border_x") && !propVal.equals("null") && !propVal.equals("") ){
							labelBandProp.put("border_x", Integer.parseInt(propVal));
						}
						else if( propName.equals("border_width") && !propVal.equals("null") && !propVal.equals("") ){
							labelBandProp.put("border_width", Integer.parseInt(propVal));
						}
					}// item prop Node List For
				}
				// 테이블이면 -- 셀 탐색.
				else if( "UBTable".equals(className) || "UBApproval".equals(className) ){
					// Table x 추출
					NodeList _tableProps = _item.getElementsByTagName("property");
					int tbPropLen = _tableProps.getLength();
					int tbBandCnt = 1;
					float tbBandWidth = 0;
					boolean _useBand = true;
					if( bandData.isEmpty() ) _useBand = false;
					String _dsName = "";
					
					int tbX = -1;
					
					float _tbXOri = -1;
					for (int j = 0; j < tbPropLen; j++) {
						NamedNodeMap itemPropNode = _tableProps.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						
						if( _tableProps.item(j).getParentNode()	.getNodeName().equals("item") == false ) break; 
						
						if( propName.equals("x") && !propVal.equals("null") && !propVal.equals("") ){
							try {
								int dotIdx = propVal.indexOf(".");
								if( dotIdx != -1 ){
									tbX = (int) (Math.round(Double.parseDouble(propVal)) + _pageXPosition);
								}
								else{
//									tbX = Integer.valueOf(propVal) + _pageXPosition;
									tbX = (int) Math.round( Float.valueOf(propVal) + _pageXPosition );
								}
								tbX = Integer.valueOf(propVal);
								
								_tbXOri = Float.valueOf(propVal) + _pageXPosition;
//								if(_useBand == false) break;
							} catch (Exception e) {
								continue;
							}
						}
						else if( bandData.isEmpty()== false && propName.equals("band") && !propVal.equals("null") && !propVal.equals("") )
						{
							if(bandData.containsKey(propVal))
							{
								tbBandCnt = Integer.parseInt( bandData.get(propVal).get("labelBandColCount") );
								tbBandWidth = Float.valueOf(bandData.get(propVal).get("labelBandDisplayWidth"));
							}
							_useBand = false;
//							if(tbX > -1 ) break;
						}
						else if( propName.equals("dataSet") &&  !propVal.equals("null") &&  !propVal.equals("") )
						{
							_dsName = propVal;
						}
					}
					if( tbX == -1 ) continue;
					
					NodeList _cells = _item.getElementsByTagName("cell");
					
					int cellLen = _cells.getLength();
					for (int cIdx = 0; cIdx < cellLen ; cIdx++) {
						Element _cell = (Element)_cells.item(cIdx);
						NodeList _props = _cell.getElementsByTagName("property");
						int propLen = _props.getLength();
						
						int xVal = -1;
						int widthVal = -1;
						int dotIdx = -1;

						float xValFloat = -1;
						float widthValFloat = -1;
						// property
						for (int j = 0; j < propLen; j++) {
							NamedNodeMap itemPropNode = _props.item(j).getAttributes();
							String propName = itemPropNode.getNamedItem("name").getNodeValue();
							String propVal = itemPropNode.getNamedItem("value").getNodeValue();
							
							if( propName.equals("x") && !propVal.equals("null") && !propVal.equals("") ){
								try {
									dotIdx = propVal.indexOf(".");
									if( dotIdx != -1 ){
										xVal = (int) Math.round((float) Math.round(Double.parseDouble(propVal)*100)/100 );
									}
									else{
										xVal = Integer.valueOf(propVal);
									}
									
									xValFloat = Float.valueOf(propVal);
									if( widthVal != -1 ) break;
								} catch (Exception e) {
									continue;
								}
							}
							else if( propName.equals("width") && !propVal.equals("null") && !propVal.equals("") ){
								try {
									dotIdx = propVal.indexOf(".");
									if( dotIdx != -1 ){
										widthVal = (int) Math.round( (float) Math.round(Double.parseDouble(propVal)*100)/100 );
									}
									else{
										widthVal = Integer.valueOf(propVal);
									}
									
									widthValFloat = Float.valueOf(propVal);
									if( xVal != -1 ) break;
								} catch (Exception e) {
									continue;
								}
							}
						}// item prop Node List For
						
						if( xVal != -1 && widthVal != -1 ){
							if("UBApproval".equals(className) && !_dsName.equals("") && xVal > 0)
							{
								int _dsSize = 0;
								if( mDataSet.get(_dsName) != null )
								{
									_dsSize = mDataSet.get(_dsName).size();
								}
								for (int j = 0; j < _dsSize; j++) 
								{
									int _cellX = xVal + ( widthVal *  j );
									if( !_xAr.contains( _cellX +tbX) )	_xAr.add( _cellX +tbX);
									if( !_xAr.contains( _cellX +widthVal+tbX) )	_xAr.add( _cellX +widthVal+tbX);		
									
									float _cellXFl = xValFloat + (widthValFloat * j );
									if( !_xArOri.contains( _cellXFl +_tbXOri) )	_xArOri.add( _cellXFl +_tbXOri);
									if( !_xArOri.contains( _cellXFl +widthVal+_tbXOri) )	_xArOri.add( _cellXFl +widthVal+_tbXOri);		
								}
							}
							else
							{
								if( !_xAr.contains(xVal+tbX) )	_xAr.add(xVal+tbX);
								if( !_xAr.contains(xVal+widthVal+tbX) )	_xAr.add(xVal+widthVal+tbX);
								
								if( !_xArOri.contains( xValFloat +_tbXOri) )	_xArOri.add( xValFloat +_tbXOri);
								if( !_xArOri.contains( xValFloat +widthVal+_tbXOri) )	_xArOri.add( xValFloat +widthVal+_tbXOri);		
							}
						}
						if(tbBandCnt > 1)
						{	
							int _addW = 0;
							float _addWFl = 0f;
							for (int _bandRep = 1; _bandRep < tbBandCnt; _bandRep++) {
								
								_addW = Float.valueOf( (float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep) ).intValue();
								_addWFl =(float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep);
								
								
								
								if( !_xAr.contains(xVal+tbX+(_addW)) )	_xAr.add(xVal+tbX+(_addW));
								if( !_xAr.contains(xVal+widthVal+tbX+(_addW)) )	_xAr.add(xVal+widthVal+tbX+(_addW));
								
								if( !_xArOri.contains(xValFloat+_tbXOri+(_addWFl)) )	_xArOri.add(xValFloat+_tbXOri+(_addWFl));
								if( !_xArOri.contains(xValFloat+widthVal+_tbXOri+(_addWFl)) )	_xArOri.add(xVal+widthVal+_tbXOri+(_addWFl));
								
							}
						}
					}// cell For End.
					
				}
				else if( "UBDataGrid~~~".equals(className) ){
					//----------Data grid 추가되면??
				}
				else{
					
					NodeList _props = _item.getElementsByTagName("property");
					int propLen = _props.getLength();
					
					int xVal = -1;
					int widthVal = -1;
					int dotIdx = -1;
					float xValFloat = -1;
					float widthValFloat = -1;
					
					int tbBandCnt = 1;
					float tbBandWidth = 0;
					boolean _useBand = true;
					if( bandData.isEmpty() ) _useBand = false;
					
					// property
					for (int j = 0; j < propLen; j++) {
						NamedNodeMap itemPropNode = _props.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						
						if( propName.equals("x") && !propVal.equals("null") && !propVal.equals("") ){
							try {
								dotIdx = propVal.indexOf(".");
								if( dotIdx != -1 ){
									xVal = (int) (Math.round(Double.parseDouble(propVal)) + _pageXPosition);
								}
								else{
									xVal = (int) Math.round( Float.valueOf(propVal) + _pageXPosition );
								}
								
								xValFloat = Float.valueOf(propVal) + _pageXPosition;
								if( widthVal != -1 && _useBand == false ) break;
							} catch (Exception e) {
								continue;
							}
						}
						else if( propName.equals("width") && !propVal.equals("null") && !propVal.equals("") ){
							try {
								dotIdx = propVal.indexOf(".");
								if( dotIdx != -1 ){
									widthVal = (int)Math.round(Double.parseDouble(propVal));
								}
								else{
									widthVal = Integer.valueOf(propVal);
								}
								widthValFloat = Float.valueOf(propVal);
								if( xVal != -1  && _useBand == false ) break;
							} catch (Exception e) {
								continue;
							}
						}
						else if( bandData.isEmpty()== false && propName.equals("band") && !propVal.equals("null") && !propVal.equals("") )
						{
							if(bandData.containsKey(propVal))
							{
								tbBandCnt = Integer.parseInt( bandData.get(propVal).get("labelBandColCount") );
								tbBandWidth = Float.valueOf(bandData.get(propVal).get("width"));
							}
							if(xVal != -1 && widthVal != -1 )
							{
								break;
							}
						}
					}// item prop Node List For
					
					if( xVal != -1 && widthVal != -1 ){
						if( !_xAr.contains(xVal) )	_xAr.add(xVal);
						if( !_xAr.contains(xVal+widthVal) )	_xAr.add(xVal+widthVal);
						
						if( !_xArOri.contains( xValFloat) )	_xArOri.add( xValFloat );
						if( !_xArOri.contains( xValFloat + widthVal ) )	_xArOri.add( xValFloat + widthVal );		
					}
					
					if(tbBandCnt > 1)
					{	
						int _addW = 0;
						for (int _bandRep = 1; _bandRep < tbBandCnt+1; _bandRep++) {
							
							_addW = Float.valueOf( (float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep) ).intValue();
							
							if( !_xAr.contains(xVal+(_addW)) )	_xAr.add(xVal+(_addW));
							if( !_xAr.contains(xVal+widthVal+(_addW)) )	_xAr.add(xVal+widthVal+(_addW));
							
						}
					}
					
				}
			}// item for
			
			int xLen = 0;
			// 문서가 라벨밴드라면,
			if( labelBandProp != null ){
				int columns = labelBandProp.get("columns");
				int labelWidth = labelBandProp.get("border_x") + labelBandProp.get("border_width");
				
				// 위에서 만든 x array 에 column 개수만큼 더해준다.
				xLen = _xAr.size();
				for (int i = 0; i < xLen; i++) {
					for (int j = 1; j < columns; j++) {
						int newPos = _xAr.get(i)+labelWidth*j;
						if( !_xAr.contains(newPos) )	_xAr.add(newPos);
						
						float newPosFl = _xArOri.get(i)+labelWidth*j;
						if( !_xArOri.contains( newPosFl) )	_xArOri.add( newPosFl );
					}
				}
			}
			
			// 페이지 별 x 배열을 누적시킨다.
			xLen = _xAr.size();
			for (int i = 0; i < xLen; i++) {
				int _xVal = _xAr.get(i);
				if( !xAr.contains(_xVal) )	xAr.add(_xVal);
				
				xArFloat.add(_xArOri.get(i));
				
			}
			
			xLen = _xArOri.size();
			for (int i = 0; i < xLen; i++) {
				float _xValF = _xArOri.get(i);
				if( !xArFloat.contains(_xValF) )	xArFloat.add(_xValF);
			}
			
			
			// Clone Page값 담기
			String cloneData = _page.getAttribute("clone");
			int _cloneColCnt = 1;
			
			if( _isConnect == false )
			{
				if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL)  || cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM))
				{
					if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
					{
						_cloneColCnt = 1;
					}
					else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) )
					{
						_cloneColCnt = 2;
					}
					else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM) )
					{
						_cloneColCnt = Integer.parseInt(_page.getAttribute("cloneColCount"));
					}
				}
			}
			else
			{
				_cloneColCnt = _cloneCountArgo;
			}
			
			if( _cloneColCnt > 1)
			{
				float pageWidth = Float.valueOf(_page.getAttribute("width")) / _cloneColCnt;
				
				int _size = xArFloat.size();
				
				for (int j = 0; j < _cloneColCnt; j++) {
					for (int i = 0; i < _size; i++) {
						int _itemX = Math.round((float) Math.round( ( xArFloat.get(i) + ( pageWidth * j ) )*100)/100 );
						if( cloneXAr.indexOf(_itemX) == -1 ) cloneXAr.add(_itemX);
					}
				}
				xAr = cloneXAr;
//			 int _size = xAr.size();
//			for (int j = 1; j < _cloneColCnt; j++) {
//				for (int i = 0; i < _size; i++) {
//					int _xVal = xAr.get(i) + Float.valueOf( pageWidth*j ).intValue();
//					if( !xAr.contains(_xVal) )	xAr.add(_xVal);
//				}
//			}
				_cloneCountArgo = _cloneColCnt;
			}
			
			
			
		}// page for
		
		Collections.sort(xAr);

		log.info(getClass().getName() + "::" + "make X Array Complete!");
		
		return xAr;
	}
	
	WordprocessingMLPackage resultDoc = null;
	ubFormToWord _ubFormToWord = null;

//	ArrayList<WordprocessingMLPackage> docList = null;
//	public ArrayList<WordprocessingMLPackage> wordExportManager(String _xml , HashMap _param) throws Exception
	public WordprocessingMLPackage wordExportManager(String _xml , HashMap _param, ubFormToWord _ubfToWord, WordprocessingMLPackage _resultDoc) throws Exception
	{
		Object clientEditMode = _param.get("CLIENT_EDIT_MODE");
		log.debug(getClass().getName() + "::" + "Call wordExportManager...clientEditMode=" + clientEditMode);
		
		// 초기화
		resetVariable();
		
		mCLIENT_IP = (String) _param.get("CLIENT_IP");
		mPAGE_NUM = (Integer) _param.get("PAGE_NUM");

		Object loadType = _param.get("LOAD_TYPE");
		
		boolean bSupportIE9 = false;
		if( loadType != null && loadType.equals("div") )
		{
			bSupportIE9 = true;
		}
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _changeDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>)_param.get("CHANGE_DATASET");
		HashMap<String, Object> _changeItemList = (HashMap<String, Object>) _param.get("CHANGE_ITEM_DATA");

		Object _params = _param.get("PARAMS");
		if( _params.hashCode() != 0 )
		{
			//mParam = _jsonUtils.jsonToMap((String) _params);
		 	Object ubObj = JSONValue.parseWithException((String)_params);	
		 	mParam = (JSONObject)ubObj;
		}
		
		// xml 을 document 형태로 변경.
		// Project 중 필요한 속성을 담음.
		xmlToDocument(_xml);
		
		if("ON".equals(clientEditMode) && _changeDataSet != null)	
		{
			log.debug(getClass().getName() + "::" + "Call wordExportManager..._changeDataSet != null");
			mDataSet = _changeDataSet;
		}
		else
		{
			log.debug(getClass().getName() + "::" + "Call wordExportManager..._changeDataSet == null");
			getXmlToDataSet();
//			xmlToDataSet();		// 여러건의 Form의 데이터 셋을 로드하기 위하여 지정
		}
		
		ArrayList<ArrayList<HashMap<String, Object>>> dataMap = null;
		if( !mParamResult )
		{
			if(_ubfToWord == null)	_ubFormToWord = new ubFormToWord();
			else _ubFormToWord = _ubfToWord;
			
			log.debug(getClass().getName() + "::toWord::" + "Strart creating WordprocessingMLPackage...");
			
			String _word2013Package = common.getPropertyValue("wordExport.word2013DefaultFilePath");
			
			//TEST
			if( _word2013Package != null && _word2013Package.equals("") == false )
			{
				File _defStyleFile = new File(_word2013Package);
				if( _defStyleFile.isFile() )
				{
					resultDoc =	WordprocessingMLPackage.load( _defStyleFile );
					resultDoc.getMainDocumentPart().getContent().clear();
					
					log.debug(getClass().getName() + "::toWord::" + " WordprocessingMLPackage create Word2013 Type");
				}
				else
				{
					if( _resultDoc == null ) resultDoc = WordprocessingMLPackage.createPackage();
					else resultDoc = _resultDoc;
				}
			}
			else
			{
				if( _resultDoc == null ) resultDoc = WordprocessingMLPackage.createPackage();
				else resultDoc = _resultDoc;
			}
			
			_ubFormToWord.settingWord(resultDoc);
			_ubFormToWord.setmImageDictionary(mImageDictionary);

//			log.debug(getClass().getName() + "::toWord::" + "Create WordprocessingMLPackage complete. [ "+diffMin +":"+diffSec+"." + diff%1000+" ]");
			//docList : 전역변수. -- export page 에서 array 에 add 시키기.
//			docList = new ArrayList<WordprocessingMLPackage>();
			getExportAllPages(true, null,bSupportIE9, _changeItemList);
//			return docList; // file 구분
//			resultDoc = new WordDocument();
			
			if(_ubfToWord == null)	_ubFormToWord = null;
			
			if( _ubfToWord == null && mImageDictionary != null )
			{
				clearImageDictionary();
			}
			
			return resultDoc;
			 
		}
		else{
			return null;
		}
	}
	
	XMLSlideShow ss = null;
	ubFormToPPT _ubFormToPPT = null;
	
	public XMLSlideShow pptExportManager(String _xml , HashMap _param, ubFormToPPT _ubfToPPT, XMLSlideShow _pptSlideShow) throws Exception
	{
		Object clientEditMode = _param.get("CLIENT_EDIT_MODE");
		log.debug(getClass().getName() + "::" + "Call PPTExportManager...clientEditMode=" + clientEditMode);
		
		// 초기화
		resetVariable();
		
		mCLIENT_IP = (String) _param.get("CLIENT_IP");
		mPAGE_NUM = (Integer) _param.get("PAGE_NUM");
		
		Object loadType = _param.get("LOAD_TYPE");
		
		boolean bSupportIE9 = false;
		if( loadType != null && loadType.equals("div") )
		{
			bSupportIE9 = true;
		}

		HashMap<String, ArrayList<HashMap<String, Object>>> _changeDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>)_param.get("CHANGE_DATASET");
		HashMap<String, Object> _changeItemList = (HashMap<String, Object>) _param.get("CHANGE_ITEM_DATA");

		Object _params = _param.get("PARAMS");
		if( _params.hashCode() != 0 )
		{
			//mParam = _jsonUtils.jsonToMap((String) _params);
		 	Object ubObj = JSONValue.parseWithException((String)_params);	
		 	mParam = (JSONObject)ubObj;
		}
		
		// xml 을 document 형태로 변경.
		// Project 중 필요한 속성을 담음.
		xmlToDocument(_xml);
		
		if("ON".equals(clientEditMode) && _changeDataSet != null)	
		{
			log.debug(getClass().getName() + "::" + "Call PPTExportManager..._changeDataSet != null");
			mDataSet = _changeDataSet;
		}
		else
		{
			log.debug(getClass().getName() + "::" + "Call PPTExportManager..._changeDataSet == null");
			getXmlToDataSet();
//			xmlToDataSet();		// 여러건의 Form의 데이터 셋을 로드하기 위하여 지정

		}
		ArrayList<ArrayList<HashMap<String, Object>>> dataMap = null;
		if( !mParamResult )
		{
			if(_ubfToPPT == null)
			{
				_ubFormToPPT = new ubFormToPPT();
				ss = new XMLSlideShow();
			}
			else
			{
				_ubFormToPPT = _ubfToPPT;
				ss = _pptSlideShow;
			}
			
			if( "ON".equals(clientEditMode) && _changeItemList != null && _changeItemList.size() > 0)
			{
//			testPdfChangeMapping(_changeItemList);
				_ubFormToPPT.setChangeItemList(_changeItemList);
			}
			
			log.debug(getClass().getName() + "::toPPT::" + "Strart creating XMLSlideShow...");

//			ss = new XMLSlideShow();

//			log.debug(getClass().getName() + "::toWord::" + "Create WordprocessingMLPackage complete. [ "+diffMin +":"+diffSec+"." + diff%1000+" ]");
			//docList : 전역변수. -- export page 에서 array 에 add 시키기.
			getExportAllPages(true, null, bSupportIE9, _changeItemList); 
			
			if(_ubfToPPT == null)_ubFormToPPT = null;
			
			if( _ubfToPPT == null && mImageDictionary != null )
			{
				clearImageDictionary();
			}
			
			return ss;
			 
		}else{
			return null;
		}
		
	}
	protected String resultHwp = "";
	protected ubFormToHWP _ubFormToHwp = null;
	public String hwpExportManager(String _xml , HashMap _param, String _path) throws Exception
	{
		Object clientEditMode = _param.get("CLIENT_EDIT_MODE");
		log.debug(getClass().getName() + "::" + "Call hwpExportManager...clientEditMode=" + clientEditMode);
		
		// 초기화;
		resetVariable();

		mCLIENT_IP = (String) _param.get("CLIENT_IP");
		mPAGE_NUM = (Integer) _param.get("PAGE_NUM");
		
		Object loadType = _param.get("LOAD_TYPE");
		
		boolean bSupportIE9 = false;
		if( loadType != null && loadType.equals("div") )
		{
			bSupportIE9 = true;
		}
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _changeDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>)_param.get("CHANGE_DATASET");
		HashMap<String, Object> _changeItemList = (HashMap<String, Object>) _param.get("CHANGE_ITEM_DATA");
		
		Object _params = _param.get("PARAMS");
		if( _params.hashCode() != 0 )
		{
			//mParam = _jsonUtils.jsonToMap((String) _params); 
		 	Object ubObj = JSONValue.parseWithException((String)_params);	
		 	mParam = (JSONObject)ubObj;
		}

		// xml 을 document 형태로 변경.
		// Project 중 필요한 속성을 담음.
		xmlToDocument(_xml);
		
		if("ON".equals(clientEditMode) && _changeDataSet != null)	
		{
			log.debug(getClass().getName() + "::" + "Call hwpExportManager..._changeDataSet != null");
			mDataSet = _changeDataSet;
		}
		else
		{
			log.debug(getClass().getName() + "::" + "Call hwpExportManager..._changeDataSet == null");
			getXmlToDataSet();
//			xmlToDataSet();		// 여러건의 Form의 데이터 셋을 로드하기 위하여 지정

		}
		
		// 로그 시간표시
		log.info( "SAVE HWP START " );
		_ubFormToHwp = new ubFormToHWP();
		if( !mParamResult )
		{
//			getTotalPage(_param);
//			getAllReportPages(false);
			getExportAllPages(false, null, bSupportIE9, _changeItemList);		//TEST
		}
		else
		{
			return null;
		}
		
		if( mImageDictionary != null )
		{
			clearImageDictionary();
		}
				
		log.debug(getClass().getName() + "::toHWP::" + "Strart creating HwpprocessingMLPackage...");
		
		resultHwp = _ubFormToHwp.xmlPasingtoHwp(mHashMap, _path);
				
		return resultHwp;
	}
	
	private HashMap<String, Object> mProjectInfoMap;
	
	public HashMap<String, Object> getProjectInfo()
	{
		return mProjectInfoMap;
	}
	
	public void setProjectInfo( HashMap<String, Object> _projectInfoMap )
	{
		mProjectInfoMap = _projectInfoMap;
	}
	
	protected void xmlToDocument(String _xml) throws SAXException, IOException, ParserConfigurationException
	{
		// XML 파서의 XML 외부 개체와 DTD 처리를 비활성화 합니다.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		        
        System.out.println("xmlToUbForm::xmlToDocument() - mUseMultiFormType=" + mUseMultiFormType);
        
		if( mUseMultiFormType )
        {
    		//InputStream inputStream= new FileInputStream(_xml);
    		//Reader reader = new InputStreamReader(inputStream,"UTF-8");
          
    		//InputSource _is = new InputSource(reader);
    		//mDocument = factory.newDocumentBuilder().parse(_is);
    		
    		//File _tmpFile = new File(_xml);
    		//if( _tmpFile.isFile() ) _tmpFile.delete();
			InputSource _is = new InputSource(new StringReader(_xml));
			mDocument = factory.newDocumentBuilder().parse(_is);
    		
    		mDocument = updateMergeProjectInfo(mDocument);
    		
        }
        else
        {
			if(documents != null && documents.size() > 0 )
			{
	//			mDocument = documents.get(0);
				InputSource _is = new InputSource(new StringReader( documents.get(0) ));
				mDocument = factory.newDocumentBuilder().parse(_is);
	
			}
			else
			{
				InputSource _is = new InputSource(new StringReader(_xml));
				mDocument = factory.newDocumentBuilder().parse(_is);
			}
        }
		
		// xml Project
		NodeList _projectList = mDocument.getElementsByTagName("project");
		Element _project = (Element) _projectList.item(0);
		
		JSONObject projectHm = new JSONObject();
		
		projectHm = mPropertyFn.getAttrObject(_project.getAttributes());

		Log.pageFontUnit = projectHm.get("fontUnit") != null ? projectHm.get("fontUnit").toString() : "px";
		
		if( projectHm.containsKey("clientEditMode")){
			CLIENT_EDIT_MODE = projectHm.get("clientEditMode").toString().toUpperCase();
			projectHm.put("clientEditMode", CLIENT_EDIT_MODE);
		}
		
		if( projectHm.containsKey("fnVersion")){
			FUNCTION_VERSION = projectHm.get("fnVersion").toString();
		}
		
		// ISPIVOT
		// page속성의 isPivot속성이 있을경우 처리
		NodeList pages = _project.getElementsByTagName("page");
		Element _page = (Element) pages.item(0);
		if( _page.hasAttribute("isPivot") && _page.getAttribute("isPivot").equals("true") )
		{
			projectHm.put("pageWidth", _page.getAttribute("height"));
			projectHm.put("pageHeight", _page.getAttribute("width"));
		}
		
		if( mProjectInfoMap != null ){
			if( mProjectInfoMap.containsKey("WATER_MARK"))
			{
				projectHm.put("WATER_MARK", mProjectInfoMap.get("WATER_MARK"));
			}
		}
		
		Element pageGrp = (Element) _project.getElementsByTagName("pageGroup").item(0);
		boolean _useFileSplit = false;
		String _downLoadFileName = "";
		
		if( pageGrp != null )
		{
			Element pageGrpProp ;
			NodeList _pageGrpList = pageGrp.getElementsByTagName("property");
			for(int i = 0; i < _pageGrpList.getLength(); i++ )
			{
				pageGrpProp = (Element) _pageGrpList.item(i);
				if(pageGrpProp.getAttribute("name").equals("useFileSplit") && pageGrpProp.getAttribute("value").equals("true") )
				{
					_useFileSplit = true;
				}
				if(pageGrpProp.getAttribute("name").equals("downLoadFileName") )
				{
					_downLoadFileName = pageGrpProp.getAttribute("value");
				}
			}
			if(_useFileSplit && _downLoadFileName.equals(""))
			{
				_useFileSplit = false;
			}
		}
		
		projectHm.put("USE_FILE_SPLIT", _useFileSplit);
		
		// margin지정 
		if( projectHm.containsKey("marginTop")){
			mPageMarginTop = Float.parseFloat( projectHm.get("marginTop").toString() );
		}
		if( projectHm.containsKey("marginLeft")){
			mPageMarginLeft = Float.parseFloat( projectHm.get("marginLeft").toString() );
		}
		if( projectHm.containsKey("marginRight")){
			mPageMarginRight = Float.parseFloat( projectHm.get("marginRight").toString() );
		}
		if( projectHm.containsKey("marginBottom")){
			mPageMarginBottom = Float.parseFloat( projectHm.get("marginBottom").toString() );
		}
		
/*		
		try {
			ArrayList<String> _userAuthList = common.getUserFileAuthority( m_appParams.getREQ_INFO().getLOGIN_ID() );
			if( _userAuthList != null ) projectHm.put("downloadAuthList", _userAuthList);
		} catch (SQLException e) {
//			e.printStackTrace();
		}
*/		
		setProjectInfo(projectHm);		
		//mHashMap.put("project", hashmapToJsonStr(projectHm));
		mHashMap.put("project", projectHm);
		
		mTempletInfo = TempletItemInfo.checkTempletItem( mDocument , mParam, m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE(), false, m_appParams.getREQ_INFO().getEXTERNAL_PROJECT_CODE() );
	}
	
	private void getTotalPage( HashMap _param ) throws Exception
	{
		// xml page;
		NodeList _pageList = mDocument.getElementsByTagName("page");
		
		Object loadType = _param.get("LOAD_TYPE");
		
		ArrayList<Element> _pages = null;
		boolean _chkFlag = true;
		for(int i = 0 ; i < _pageList.getLength() ; i ++)
		{
			Element _page = (Element) _pageList.item(i);
			
			int _reportType = Integer.parseInt(_page.getAttribute("reportType") );
			
			//pageVisible값을 체크하여 페이지 사용여부를 결정
			_chkFlag = UBIDataUtilPraser.getCanvasVisibleChkeck(mDataSet, _page, mParam,mFunction);
			
			if( _chkFlag ){
				mPageAr.add(_page);
				pageTypeToTotalNum( _reportType , _page, loadType);
			}
			
		}
	}
	
	// 전체 데이터셋의 Row수를 포함한 정보를 가져온다.
	protected void xmlToDataSet() throws Exception
	{
		marDataSetItems.clear();
		// xml DATASET;
		NodeList _itemList = mDocument.getElementsByTagName("item");
		
		int leng = _itemList.getLength();
		
		//mDataSetRowCountInfo = new HashMap<String, Integer>();
		
		for( int d = leng - 1 ; d > -1 ; d--)
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
				
				if( _mergedInfo != null  )
				{
					if( _mergedInfo.hasChildNodes() )
					{
						// merged 테그 담기.!!!;
						marDataSetMerged.put(_dataId, _mergedInfo);
					}
				}
				
				marDataSetItems.put(_dataId, _itemE);
			}
			else
			{
				break;
			}
		}
		
		mDataSetParam = paramCheck_Handler(mDataSetParam);
		
		if( mDataSetParam == null )
		{
			mParamResult = true;
			return;
		}
		else if( !mParamResult )
		{
			mDataSet = new HashMap< String, ArrayList<HashMap<String, Object>> >();
			//DataServiceManager oService = (this.mServiceReqMng != null)?this.mServiceReqMng.getServiceManager():null;	// null 처리 2016-03-10 최명진
			//mDataSet = mDataSetFn.dataSetLoad(oService, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);	
			//mDataSet = mDataSetFn.dataSetLoad(null, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);	
			mDataSet = mDataSetFn.dataSetLoad(mSERVER_URL, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);	
		}
			
	}
	
	
	// 전체 데이터셋의 Row수를 포함한 정보를 가져온다.
	protected void xmlToDataSetMulti() throws Exception
	{
		int _docSize = documentInfos.size();
		for (int i = 0; i < _docSize; i++) {
			marDataSetItems.clear();
			mDataSetParam.clear();
			
//			mDocument = documents.get(i);
			InputSource _is = new InputSource(new StringReader(  documents.get(  Integer.parseInt( documentInfos.get(i).get("IDX") ) ) ));
			mDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
			
			mParam = documentParams.get(i);
			// xml DATASET;
			NodeList _itemList = mDocument.getElementsByTagName("item");
//			mParam = documentParams.get(i);
			int leng = _itemList.getLength();
			
			for( int d = leng - 1 ; d > -1 ; d--)
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
					
					if( _mergedInfo != null  )
					{
						if( _mergedInfo.hasChildNodes() )
						{
							// merged 테그 담기.!!!;
							marDataSetMerged.put(_dataId, _mergedInfo);
						}
					}
					
					marDataSetItems.put(_dataId, _itemE);
				}
				else
				{
					break;
				}
			}
			
			HashMap<String, HashMap<String, String>> _resultParam = paramCheck_Handler(mDataSetParam);
			
			if( _resultParam == null )
			{
				mParamResult = true;
//				return;
			}
			else if( !mParamResult )
			{
				mDataSet = new HashMap< String, ArrayList<HashMap<String, Object>> >();
				//DataServiceManager oService = (this.mServiceReqMng != null)?this.mServiceReqMng.getServiceManager():null;	// null 처리 2016-03-10 최명진
				//mDataSet = mDataSetFn.dataSetLoad(oService, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);	
				//mDataSet = mDataSetFn.dataSetLoad(null, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);	
				mDataSet = mDataSetFn.dataSetLoad(mSERVER_URL, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);		
				
				if(dataSets==null) dataSets = new ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>>();
				
				dataSets.add(mDataSet);
			}
			
			/**
			// 멀티폼 로드시에는 파라미터를 입력 받지 않도록 처리
			mDataSet = new HashMap< String, ArrayList<HashMap<String, Object>> >();
			DataServiceManager oService = (this.mServiceReqMng != null)?this.mServiceReqMng.getServiceManager():null;	// null 처리 2016-03-10 최명진
			mDataSet = mDataSetFn.dataSetLoad(oService, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);		
			
			if(dataSets==null) dataSets = new ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>>();
			
			dataSets.add(mDataSet);
			*/
			
		}
		
	}
	
	// 전체 데이터셋의 Row수를 포함한 정보를 가져온다.
	private void xmlToDataSetMergeForm() throws Exception
	{
		NodeList _projects = mDocument.getDocumentElement().getElementsByTagName("project");
		int _docSize = _projects.getLength();
		Element _projectEl;
		
		for (int i = 0; i < _docSize; i++) {
			marDataSetItems.clear();
			mDataSetParam.clear();
			
			_projectEl = (Element) _projects.item(i);
			
			mParam = documentParams.get(i);
			
			NodeList _itemList = _projectEl.getElementsByTagName("item");
//			
			int leng = _itemList.getLength();
			
			for( int d = leng - 1 ; d > -1 ; d--)
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
					
					if( _mergedInfo != null  )
					{
						if( _mergedInfo.hasChildNodes() )
						{
							// merged 테그 담기.!!!;
							marDataSetMerged.put(_dataId, _mergedInfo);
						}
					}
					
					marDataSetItems.put(_dataId, _itemE);
				}
				else
				{
					break;
				}
			}
			
			HashMap<String, HashMap<String, String>> _resultParam = paramCheck_Handler(mDataSetParam);
			
			if( _resultParam == null )
			{
				mParamResult = true;
//				return;
			}
			else if( !mParamResult )
			{
				mDataSet = new HashMap< String, ArrayList<HashMap<String, Object>> >();
				//DataServiceManager oService = (this.mServiceReqMng != null)?this.mServiceReqMng.getServiceManager():null;	// null 처리 2016-03-10 최명진
				//mDataSet = mDataSetFn.dataSetLoad(oService, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);	
				//mDataSet = mDataSetFn.dataSetLoad(null, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);	
				mDataSet = mDataSetFn.dataSetLoad(mSERVER_URL, marDataSetItems , mDataSetParam , marDataSetMerged, CLIENT_EDIT_MODE);	
				
				if(dataSets==null) dataSets = new ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>>();
				
				dataSets.add(mDataSet);
			}
			
		}
		
	}
	
	private HashMap<String, HashMap<String, String>> paramCheck_Handler( HashMap<String, HashMap<String, String>> _param ) throws UnsupportedEncodingException
	{
		// param Check; 
		NodeList _paramList = mDocument.getElementsByTagName("param");

		HashMap<String, HashMap<String, String>> _paramMap = new HashMap<String, HashMap<String, String>>();
		if(_param != null ) _paramMap = _param;
		
		HashMap<String, String> _paramProp = null;

		for(int _p = 0; _p < _paramList.getLength() ; _p++)
		{
			Element _paramItem = (Element) _paramList.item(_p);
			
			if( "item".equals( _paramItem.getParentNode().getParentNode().getNodeName() ) ||  "project".equals( _paramItem.getParentNode().getParentNode().getNodeName() ) )
			{
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
						//_paramValue = URLEncoder.encode(_paramValue, "UTF-8");			// 인코딩을 제거 2016-02-25
						_paramProp.put(_paramName, _paramValue);
					}
					
				}
			}
			

		}

//		if( _paramMap.size() != 0 && mParam.size() == 0)
		if( mUseParameter && _paramMap.size() != 0 && checkSystemParam(mParam) )
		{
			mHashMap.put("resultType","PARAM");
			//{\"CONTRY_DIV\":{\"type\":\"String\",\"value\":\"국내\",\"desc\":\"\"}}
			// KEY : {TYPE:,VALUE:,DESE:}
			//mHashMap.put("param", hashmapToJsonStr(_paramMap));
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
			
			if( mIsOpenParamPopup && _paramMap.size() > 0 )
			{
				mHashMap.put("resultType","PARAM");
				mHashMap.put("param", _paramMap);
				return null;
			}
		}

		return _paramMap;
		// param Check;
	}
	
	protected Boolean checkSystemParam( JSONObject _param)
	{
		int _chkIndex = 0;
		
		ArrayList<String> mNewList = new ArrayList<String>(Arrays.asList(GlobalVariableData.mSystemParams));
		
		for(Object key : _param.keySet())
		{
			if(mNewList.contains(key) == false)
			{
				_chkIndex = _chkIndex + 1;
			}
		}
		
		if(_chkIndex == 0 ) return true;
		
		return false;
	}
	
	protected boolean isSystemParameter(String _paramKey )
	{
		ArrayList<String> _systemParams = new ArrayList<String>(Arrays.asList(GlobalVariableData.mSystemParams));
		
		if(  _systemParams.indexOf(_paramKey) == -1 )
		{
			return false;
		}
		return true;
	}
	
	
	/** 
	 * 전체 페이지를 한꺼번에 생성하여, 한꺼번에 클라이언트로 전송한다.
	 * 
	 * @param isExport [ 2015-10-22 공혜지 ] Export에서 이 메서드를 사용하는 경우 Arraylist에 데이터를 추가하여 리턴한다.
	 * @return if isExport == true then ArrayList else = null
	 * */
	private ArrayList<ArrayList<HashMap<String,Object>>> getAllReportPages( Boolean isExport ) throws UnsupportedEncodingException , Exception
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
		//UBIPRINTPOSITIONX,UBIPRINTPOSITIONX2, UBIPRINTPOSITIONY, UBIPRINTPOSITIONY2
		float UBIPRINTPOSITIONX = 0;
		float UBIPRINTPOSITIONX2 = 0;
		float UBIPRINTPOSITIONY = 0;
		float UBIPRINTPOSITIONY2 = 0;
				
		if(m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE().equals("PRINT"))
		{
			if( m_appParams.getREQ_INFO().getCIRCULATION().equals("") == false)
			{
				caMax = Integer.valueOf( m_appParams.getREQ_INFO().getCIRCULATION() );
			} 

			if( m_appParams.getREQ_INFO().getSTART_PAGE().equals("") == false)
			{
				_printS = Integer.valueOf( m_appParams.getREQ_INFO().getSTART_PAGE() );
			}
			
			if( m_appParams.getREQ_INFO().getEND_PAGE().equals("") == false)
			{
				_printM = Integer.valueOf( m_appParams.getREQ_INFO().getEND_PAGE() );
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
		
		// 모든 페이지의 총 페이지수를 구하고 각 페이지별 화면을 그리기 위한 객체를 담아두기
		//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(this.mServiceReqMng, m_appParams);
		//TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(m_appParams);
		TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );
		HashMap<String, Object> _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
		
		mPageAr 			= (ArrayList<Element>) _pageInfoMap.get("PAGE_AR");
		mPageNumList 		= (ArrayList<Integer>) _pageInfoMap.get("PAGE_NUMLIST");
		mPageNumRealList 	=  (ArrayList<Integer>) _pageInfoMap.get("REAL_PAGE_NUMLIST");
		mTOTAL_PAGE_NUM 	= (Integer) _pageInfoMap.get("TOTALPAGE");
		ArrayList<Object> _pageInfoArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_DATA_LIST"); 
		ArrayList<Object> _pageInfoClassArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_CLASS_LIST"); 

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
		boolean isExcelExport = ( isExport && _ubFormToExcel != null )? true : false;
		boolean isPPTExport = ( isExport && _ubFormToPPT != null )? true : false;
		boolean isWordExport = ( isExport && _ubFormToWord != null )? true : false;
		
		Date start = new Date();
		Date end = null;
		
		String cloneData = "";
		float pageWidth = 0;
		float pageHeight = 0;

		float cloneX = 0;
		float cloneY = 0;
		ArrayList<HashMap<String, Object>> _objects = null;
		boolean clonePage = false;

		float originalPageWidth = 0;
		float originalPageHeight = 0;
		ArrayList<Float> _clonePositionList = null;
		
		// groupData일때 클론페이지시 연결여부 체크
		boolean isConnectGroupPage = false;
		// groupData일때 클론페이지시 연결시 인덱스 체크
		int _clonePageCnt = 0;
		// clonePage의 페이지 인덱스값
		int _pageRepeatCnt = 0;
		
		// Water Mark
		try {
			NamedNodeMap projAttr = mDocument.getElementsByTagName("project").item(0).getAttributes();
			String _waterMark = projAttr.getNamedItem("waterMark").getNodeValue();
			if( _waterMark != null && !_waterMark.equals("") ){
				HashMap<String, Object> pageProp = new HashMap<String, Object>();
				pageProp.put("waterMark", _waterMark);
				pageObj = new ArrayList<HashMap<String,Object>>();
				pageObj.add(pageProp);
				pagesForExport.add(pageObj);
			}
		} catch (Exception e) {
		}
		
		HashMap<String, Object> pageProp = new HashMap<String, Object>();
		
		if(mDataSet != null)
		{
			for ( ca = 0; ca < caMax; ca++) {
			
				int _printCurrentPageIndex = 0;
				
				for( i = 0 ; i < mPageNumList.size(); i ++){
					
					// 데이터셋을 매칭
					if( _pageSubDataSet.get(i) == null )
					{
						_tempDataSet = mDataSet;
					}
					else
					{
						_tempDataSet = _pageSubDataSet.get(i);
					}
					
					Element _page = mPageAr.get(i);
					
					_reportType = Integer.parseInt(_page.getAttribute("reportType"));
					_pageHash = mPropertyFn.getAttrObject(_page.getAttributes());
					
					if( _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
					{
						if( mPageNumList.size()-1 == i )
						{
							isConnectGroupPage = false;
						}
						else
						{
							isConnectGroupPage = true;
						}
						
						if( i > 0  && mPageAr.get(i-1).getAttribute("id").toString().equals(_page.getAttribute("id").toString()) == false )
						{
							_clonePageCnt = 0;
						}
					}
					else
					{
						_clonePageCnt = 0;
					}
					
					_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
					
					cloneData = _page.getAttribute("clone");
					
					clonePage = false;
					
					// Clone 페이지 일경우 Width/Height값을 나누워서 담는다
					pageWidth = Float.valueOf(_page.getAttribute("width"));
					pageHeight = Float.valueOf(_page.getAttribute("height"));
					
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
						if( printMode == false || ( _printCurrentPageIndex >= _printS && _printCurrentPageIndex < _printM ) )
						{
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(0);
							mFunction.setSectionTotalPageNum(1);
							
							mFunction.setCloneIndex(0);
							
							pageObj = mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam , 0 , mTOTAL_PAGE_NUM , _argoPage);
							mCreateFormFn.setFunction(mFunction);
							
							pageInfoData = new HashMap<String, Object >();
							pageInfoData.put("pageData", pageObj);
							pageInfoData.put("page", _pageHash);
							
							if( !isExport ) pages.put( String.valueOf(_argoPage), pageInfoData);
							
							if( isExport ){
								pageProp.clear();
								pageProp.put("cPHeight", _page.getAttribute("height"));
								pageProp.put("cPWidth", _page.getAttribute("width"));
								pageProp.put("bPageHeight", beforePageHeight);
								beforePageHeight += pageHeight;
								pageObj.add(pageProp);
								pagesForExport.add(pageObj);
								if( isExcelExport ){
									wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage, "", false, 0);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( isPPTExport ){
									ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( isWordExport ){
									resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage);
									pagesForExport.clear();
									Thread.sleep(10);
									end = new Date();
									long diff = end.getTime() - start.getTime();
									long diffSec = diff / 1000% 60;         
									long diffMin = diff / (60 * 1000)% 60;        
									log.info(getClass().getName() + "::" +  ">>>>> Word Export Total : [ " + diffMin +":"+diffSec+"."+diff%1000+" ]");
									
//									if( _argoPage % 100 == 0 ){
//										docList.add(resultDoc);
//										resultDoc = WordprocessingMLPackage.createPackage();
//										_ubFormToWord.settingWord(resultDoc);
//									}
								}
							}
							
							log.debug(getClass().getName() + "::" + "FreeForm Cover");
							_argoPage++;
							
						}
						_printCurrentPageIndex++;
						
						break;
					case 1: //freeform
					case 7: //mobile타입
					case 9: //webPage
						int _start = 0;
						int _max = 0;
						for ( j = 0; j < mPageNumList.get(i); j++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(mPageNumList.get(i));
							
							if(clonePage)
							{
								_start = j*_cloneRepCnt;
								_max = _start + _cloneRepCnt;
								//마지막 페이지가 max값보다 작을경우 처
								if( _max >= mPageNumRealList.get(i) )
								{
									_max = mPageNumRealList.get(i);
								}
								mFunction.setCloneIndex(1);
							}
							else
							{
								_start = j;
								_max = j + 1;
								mFunction.setCloneIndex(0);
							}
							
							for ( k = _start; k < _max; k++) {
								if( !clonePage || k%2 == 0 )
								{
									pageInfoData = new HashMap<String, Object >();
		//							_objects.clear();
									_objects = new ArrayList<HashMap<String, Object>>();
								}
								
								if(clonePage && k%_cloneRepCnt > 0 )
								{
									_clonePositionList = ItemConvertParser.getClonePosition( j, _cloneColCnt, _cloneRowCnt, originalPageWidth, originalPageHeight, cloneData, _cloneDirect );
									cloneX = _clonePositionList.get(0);
									cloneY = _clonePositionList.get(1);
									
									cloneX = cloneX + UBIPRINTPOSITIONX2;
									cloneY = cloneY + UBIPRINTPOSITIONY2;
									
								}
								else
								{
									cloneY = UBIPRINTPOSITIONY;
									cloneX = UBIPRINTPOSITIONX;
								}
								
								if( printMode == false || ( _printCurrentPageIndex >= _printS && _printCurrentPageIndex < _printM ) )
								{
									
									// pageConeinue값 체크  Document pageContinue="true", pageCount="3" 을 이용하여 체크
									if( mDocument.getDocumentElement().getAttribute("pageContinue").equals("true") )
									{
										int _pageCnt = mPageNumList.get(i);
										int _pageContinueCnt = Integer.valueOf( String.valueOf( mDocument.getDocumentElement().getAttribute("pageCount") ) );
										int _pageContinuePageDataCnt = (int)  Math.floor( ((i*_pageCnt)+k)/_pageContinueCnt ) ;
										int _pageContinuePageCnt = (int)  ((i*_pageCnt)+k)%_pageContinueCnt;
										if(clonePage)
										{
											_pageContinuePageCnt = (int)  ((i*(_pageCnt*2))+k)%_pageContinueCnt;
											_pageContinuePageDataCnt = (int)  Math.floor( ((i*(_pageCnt*2))+k)/_pageContinueCnt ) ;
										}
										Element _chkPage = mPageAr.get(_pageContinuePageCnt);
										
										_objects = mCreateFormFn.createFreeFormConnect(_chkPage , _tempDataSet , mParam , _pageContinuePageDataCnt, cloneX, cloneY, _objects, mTOTAL_PAGE_NUM , _argoPage);
									}
									else
									{
										_objects = mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , k, cloneX, cloneY, _objects, mTOTAL_PAGE_NUM , _argoPage);
									}
									
								}
							}
							
							mCreateFormFn.setFunction(mFunction);
							pageInfoData = new HashMap<String, Object >();
							pageInfoData.put("pageData", _objects);
							pageInfoData.put("page", _pageHash);
							
							if( !isExport ) pages.put( String.valueOf(_argoPage), pageInfoData);
							
							_argoPage = _argoPage + 1;
							_printCurrentPageIndex = _printCurrentPageIndex+1;
							
							log.debug(getClass().getName() + "::" + "FreeForm Doc  "  + j);
							
							if( isExport ){
								pageProp.clear();
								pageProp.put("cPHeight", _page.getAttribute("height"));
								pageProp.put("cPWidth", _page.getAttribute("width"));
								pageProp.put("bPageHeight", beforePageHeight);
								beforePageHeight += pageHeight;
								_objects.add(pageProp);
								pagesForExport.add(_objects);
								if( isExcelExport ){
									wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, "", false, 0);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( isPPTExport ){
									ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( isWordExport ){
									resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
									pagesForExport.clear();
									Thread.sleep(10);
//									if( _argoPage % 100 == 0 ){
//										docList.add(resultDoc);
//										resultDoc = WordprocessingMLPackage.createPackage();
//										_ubFormToWord.settingWord(resultDoc);
//									}
								}
								
								end = new Date();
								long diff = end.getTime() - start.getTime();
								long diffSec = diff / 1000% 60;         
								long diffMin = diff / (60 * 1000)% 60;        
								log.info(getClass().getName() + "::" +  ">>>>> Export Total : [ page : " + _argoPage + " time :  " + diffMin +":"+diffSec+"."+diff%1000+" ]");
								
							}
							
						}
						
						
						break;
					case 2: //masterBand
						
						MasterBandParser masterParser = (MasterBandParser) _pageInfoClassArrayList.get(i);
						ArrayList<Object> masterInfo 					= (ArrayList<Object>) _pageInfoArrayList.get(i);
						int _masterTotPage 								= (Integer) masterInfo.get(0);
						ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
						
//						MasterBandParser masterParser = new MasterBandParser(this.m_appParams);
						masterParser.setImageData(this.mImgData);
						masterParser.setChartData(this.mChartData);
						masterParser.setFunction(this.mFunction);
	
						ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
//						ArrayList<Object> masterInfo 					= masterParser.loadTotalPage(_page, mDataSet,0, pageHeight, pageWidth);
//						int _masterTotPage 								= (Integer) masterInfo.get(0);
//						ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
						
						if( _objects == null ) _objects = new ArrayList<HashMap<String,Object>>();
						
						// Clone페이지일경우 총 페이지수를 절반으로 나눠야한다.
						_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
						
						for ( j = 0; j < _masterTotPage; j++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(_masterTotPage);
							
							// clone페이지의 포지션 인덱스값
					        _pageRepeatCnt = (j + _clonePageCnt)%_cloneRepCnt;
					        
					        mFunction.setCloneIndex(_pageRepeatCnt);
					        
							if( !clonePage || _pageRepeatCnt == 0 )
							{
								pageInfoData = new HashMap<String, Object >();
	//							_objects.clear();
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
								cloneY = UBIPRINTPOSITIONY;
								cloneX = UBIPRINTPOSITIONX;
							}
							
							_objects = masterParser.createMasterBandData(j, masterList, pageWidth, pageHeight, mParam, cloneX, cloneY, _objects,mTOTAL_PAGE_NUM, _argoPage);
							
							if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) || ( !isConnectGroupPage && (j == _masterTotPage-1 )) )
							{
								if( printMode == false || ( _printCurrentPageIndex >= _printS && _printCurrentPageIndex < _printM ) )
								{
									pageInfoData.put("pageData", _objects );
									pageInfoData.put("page", _pageHash);
									
									if( !isExport ) pages.put( String.valueOf(_argoPage), pageInfoData);
									_argoPage = _argoPage + 1;
								}
								_printCurrentPageIndex = _printCurrentPageIndex+1;
								
								if( isExport ){
									pageProp.clear();
									pageProp.put("cPHeight", _page.getAttribute("height"));
									pageProp.put("cPWidth", _page.getAttribute("width"));
									pageProp.put("bPageHeight", beforePageHeight);
									beforePageHeight += pageHeight;
									_objects.add(pageProp);
									pagesForExport.add(_objects);
									if( isExcelExport ){
										wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, "", false, 0);
										pagesForExport.clear();
										Thread.sleep(10);
									}
									else if( isPPTExport ){
										ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
										pagesForExport.clear();
										Thread.sleep(10);
									}
									else if( isWordExport ){
										resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
										pagesForExport.clear();
										Thread.sleep(10);
										end = new Date();
										long diff = end.getTime() - start.getTime();
										long diffSec = diff / 1000% 60;         
										long diffMin = diff / (60 * 1000)% 60;        
										log.info(getClass().getName() + "::" +  ">>>>> Word Export Total : [ " + diffMin +":"+diffSec+"."+diff%1000+" ]");
										
//										if( _argoPage % 100 == 0 ){
//											docList.add(resultDoc);
//											resultDoc = WordprocessingMLPackage.createPackage();
//											_ubFormToWord.settingWord(resultDoc);
//										}
									}
								}
							}
							
						}

						if(isConnectGroupPage)
						{
						  _clonePageCnt = _clonePageCnt + _masterTotPage;
						}
						//if( isExport ) pagesForExport.add(pageObj);
						break;
					case 3: //continueBand 
						log.debug(getClass().getName() + "::" + "Continue Start    " + i);
						
						ContinueBandParser continueBandParser = (ContinueBandParser) _pageInfoClassArrayList.get(i);
						ArrayList<Object> bandAr 					= (ArrayList<Object>) _pageInfoArrayList.get(i);
						
						boolean isPivot = false;
						
						if(_page.getAttribute("isPivot").equals("true"))
						{
							isPivot = true;
						}
						
//						ContinueBandParser continueBandParser = new ContinueBandParser(m_appParams);
						continueBandParser.setImageData(this.mImgData);
						continueBandParser.setChartData(this.mChartData);
						continueBandParser.setFunction(this.mFunction);
						
//						ArrayList<Object> bandAr = continueBandParser.loadTotalPage(_page, mDataSet,0, pageHeight, pageWidth);
						
						HashMap<String, BandInfoMapData> bandInfo 	= (HashMap<String, BandInfoMapData>) bandAr.get(0);
						ArrayList<BandInfoMapData> bandList 		= (ArrayList<BandInfoMapData>) bandAr.get(1);
						ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList 	= (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
						HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
						if( _objects == null ) _objects = new ArrayList<HashMap<String,Object>>();
						
						// group
						HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
						ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
						
						continueBandParser.mOriginalDataMap = originalDataMap;
						continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
						
						log.debug(getClass().getName() + "::" + "Continue load Totpage  " + i);
						
						// Clone페이지일경우 총 페이지수를 절반으로 나눠야한다.
//						if(clonePage)
//						{
//							mTOTAL_PAGE_NUM = mTOTAL_PAGE_NUM + (int) Math.ceil((float) pagesRowList.size()/2); 
//						}
//						else
//						{
//							mTOTAL_PAGE_NUM = mTOTAL_PAGE_NUM + pagesRowList.size();
//						}
						_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
							
						for ( j = 0; j < pagesRowList.size(); j++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(pagesRowList.size());
							
							// clone페이지의 포지션 인덱스값
					        _pageRepeatCnt = (j + _clonePageCnt)%_cloneRepCnt;
					        
					        mFunction.setCloneIndex(_pageRepeatCnt);
					        
							if( !clonePage || _pageRepeatCnt == 0 )
							{
								pageInfoData = new HashMap<String, Object >();
								_objects = new ArrayList<HashMap<String,Object>>();
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
//								cloneY = 0;
//								cloneX = 0;
								cloneX = UBIPRINTPOSITIONX;
								cloneY = UBIPRINTPOSITIONY;
							}
							
							_objects = continueBandParser.createContinueBandItems(j, _tempDataSet, bandInfo, bandList, pagesRowList, mParam, crossTabData,cloneX,cloneY,_objects,mTOTAL_PAGE_NUM, _argoPage, isPivot);
							
							if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) ||  (!isConnectGroupPage && (j == pagesRowList.size()-1)) )
							{
								if( printMode == false || ( _printCurrentPageIndex >= _printS && _printCurrentPageIndex < _printM ) )
								{
									pageInfoData.put("pageData", _objects );
									pageInfoData.put("page", _pageHash);
									
									if( !isExport ) pages.put( String.valueOf(_argoPage), pageInfoData);
									_argoPage = _argoPage + 1;
								}
								_printCurrentPageIndex = _printCurrentPageIndex+1;
								
								if( isExport ){
									pageProp.clear();
									pageProp.put("cPHeight", _page.getAttribute("height"));
									pageProp.put("cPWidth", _page.getAttribute("width"));
									pageProp.put("bPageHeight", beforePageHeight);
									beforePageHeight += pageHeight;
									_objects.add(pageProp);
									pagesForExport.add(_objects);
									if( isExcelExport ){
										wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, "", false, 0 );
										pagesForExport.clear();
										Thread.sleep(10);
									}
									else if( isPPTExport ){
										ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1 );
										pagesForExport.clear();
										Thread.sleep(10);
									}
									else if( isWordExport ){
										resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
										pagesForExport.clear();
										Thread.sleep(10);
										end = new Date();
										long diff = end.getTime() - start.getTime();
										long diffSec = diff / 1000% 60;         
										long diffMin = diff / (60 * 1000)% 60;        
										log.info(getClass().getName() + "::" +  ">>>>> Word Export Total : [ " + diffMin +":"+diffSec+"."+diff%1000+" ]");
										
//										if( _argoPage % 100 == 0 ){
//											docList.add(resultDoc);
//											resultDoc = WordprocessingMLPackage.createPackage();
//											_ubFormToWord.settingWord(resultDoc);
//										}
									}
								}
							}
							
							//log.debug(getClass().getName() + "::" + "Continue Doc  "  + j);
						}
						
						if(isConnectGroupPage)
						{
						  _clonePageCnt = _clonePageCnt + pagesRowList.size();
						}
						break;
					case 4: //labelBand
						for ( j = 0; j < mPageNumList.get(i); j++) {
//							pageObj = mCreateFormFn.CreateLabelBandAll(_page , mDataSet , mParam ,j,mTOTAL_PAGE_NUM , _argoPage);
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(mPageNumList.get(i));
							mFunction.setCloneIndex(0);
							
							cloneX = UBIPRINTPOSITIONX;
							cloneY = UBIPRINTPOSITIONY;
							pageObj = new ArrayList<HashMap<String,Object>>();
							pageObj = mCreateFormFn.CreateLabelBandConnect(_page , _tempDataSet , mParam,j, cloneX, cloneY, pageObj ,mTOTAL_PAGE_NUM , _argoPage);
							mCreateFormFn.setFunction(mFunction);
							
							pageInfoData = new HashMap<String, Object >();

							if( printMode == false || ( _printCurrentPageIndex >= _printS && _printCurrentPageIndex < _printM ) )
							{
								pageInfoData.put("pageData", pageObj);
								pageInfoData.put("page", _pageHash);
								if( !isExport ) pages.put( String.valueOf(_argoPage), pageInfoData);
								_argoPage = _argoPage + 1;
							}
							_printCurrentPageIndex = _printCurrentPageIndex+1;
	
							if( isExport ){
								pageProp.clear();
								pageProp.put("cPHeight", _page.getAttribute("height"));
								pageProp.put("cPWidth", _page.getAttribute("width"));
								pageProp.put("bPageHeight", beforePageHeight);
								beforePageHeight += pageHeight;
								pageObj.add(pageProp);
								pagesForExport.add(pageObj);
								if( isExcelExport ){
									wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, "", false, 0);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( isPPTExport ){
									ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( isWordExport ){
									resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
									pagesForExport.clear();
									Thread.sleep(10);
									end = new Date();
									long diff = end.getTime() - start.getTime();
									long diffSec = diff / 1000% 60;         
									long diffMin = diff / (60 * 1000)% 60;        
									log.info(getClass().getName() + "::" +  ">>>>> Word Export Total : [ " + diffMin +":"+diffSec+"."+diff%1000+" ]");
									
//									if( _argoPage % 100 == 0 ){
//										docList.add(resultDoc);
//										resultDoc = WordprocessingMLPackage.createPackage();
//										_ubFormToWord.settingWord(resultDoc);
//									}
								}
							}
						}
						break;
					case 8: //lastPage
						
						// Section Page 지정
						mFunction.setSectionCurrentPageNum(0);
						mFunction.setSectionTotalPageNum(1);
						
						mFunction.setCloneIndex(0);
						
						pageObj = mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam , 0,mTOTAL_PAGE_NUM , _argoPage);
						
						pageInfoData = new HashMap<String, Object >();
						if( printMode == false || ( _printCurrentPageIndex >= _printS && _printCurrentPageIndex < _printM ) )
						{
							pageInfoData.put("pageData", pageObj);
							pageInfoData.put("page", _pageHash);
							if( !isExport ) pages.put( String.valueOf(_argoPage), pageInfoData);
							
							_argoPage = _argoPage + 1;
						}
						_printCurrentPageIndex = _printCurrentPageIndex+1;
						if( isExport ){
							pageProp.clear();
							pageProp.put("cPHeight", _page.getAttribute("height"));
							pageProp.put("cPWidth", _page.getAttribute("width"));
							pageProp.put("bPageHeight", beforePageHeight);
							beforePageHeight += pageHeight;
							pageObj.add(pageProp);
							pagesForExport.add(pageObj);
							if( isExcelExport ){
								wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, "", false, 0);
								pagesForExport.clear();
								Thread.sleep(10);
							}
							else if( isPPTExport ){
								ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
								pagesForExport.clear();
								Thread.sleep(10);
							}
							else if( isWordExport ){
								resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
								pagesForExport.clear();
								Thread.sleep(10);
								end = new Date();
								long diff = end.getTime() - start.getTime();
								long diffSec = diff / 1000% 60;         
								long diffMin = diff / (60 * 1000)% 60;        
								log.info(getClass().getName() + "::" +  ">>>>> Word Export Total : [ " + diffMin +":"+diffSec+"."+diff%1000+" ]");
								
//								if( _argoPage % 100 == 0 ){
//									docList.add(resultDoc);
//									resultDoc = WordprocessingMLPackage.createPackage();
//									_ubFormToWord.settingWord(resultDoc);
//								}
							}
						}
						
						break;
					case 12:	//ConnectLink
						// connectLink타입일경우  처리
						/*
						ConnectLinkParser connectLink 			= (ConnectLinkParser) _pageInfoClassArrayList.get(i);
						HashMap<String, Object> connectData		= (HashMap<String, Object>) _pageInfoArrayList.get(i);
						
//						ConnectLinkParser connectLink = new ConnectLinkParser(m_appParams);
//						
//						HashMap<String, Object> connectData = connectLink.loadPagesData(_page, mParam);
						int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
						// totalPage를 담아두고 각 페이지별로 for문을 돌면서 page를 리턴받기
						
						mTOTAL_PAGE_NUM = mTOTAL_PAGE_NUM + _connectTotPage;
						
						_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
						
						for ( j = 0; j < _connectTotPage; j++) {
							
							_objects = new ArrayList<HashMap<String,Object>>();
							connectLink.makeConnectPage(j, connectData.get("pageInfoData"), _objects, mTOTAL_PAGE_NUM, _argoPage);
							
							pageInfoData = new HashMap<String, Object >();  
							if( printMode == false || ( _printCurrentPageIndex >= _printS && _printCurrentPageIndex < _printM ) )
							{
								pageInfoData.put("pageData", _objects);
								pageInfoData.put("page", _pageHash);
								if( !isExport ) pages.put( String.valueOf(_argoPage), pageInfoData);
								_argoPage = _argoPage + 1;
							}
							if( isExport ){
								pageProp.clear();
								pageProp.put("cPHeight", _page.getAttribute("height"));
								pageProp.put("cPWidth", _page.getAttribute("width"));
								pageProp.put("bPageHeight", beforePageHeight);
								beforePageHeight += pageHeight;
								_objects.add(pageProp);
								pagesForExport.add(pageObj);
								if( isExcelExport ){
									wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, "", false, 0);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( isPPTExport ){
									ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
									pagesForExport.clear();
									Thread.sleep(10);
								}
								else if( isWordExport ){
									resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
									pagesForExport.clear();
									Thread.sleep(10);
									end = new Date();
									long diff = end.getTime() - start.getTime();
									long diffSec = diff / 1000% 60;         
									long diffMin = diff / (60 * 1000)% 60;        
									log.info(getClass().getName() + "::" +  ">>>>> Word Export Total : [ " + diffMin +":"+diffSec+"."+diff%1000+" ]");
									
//									if( _argoPage % 100 == 0 ){
//										docList.add(resultDoc);
//										resultDoc = WordprocessingMLPackage.createPackage();
//										_ubFormToWord.settingWord(resultDoc);
//									}
								}
							}
							_printCurrentPageIndex = _printCurrentPageIndex+1;
						}
						*/
						break;
					case 14:
						// Linked Project type 문서 타입
						
						LinkedPageParser linkedParser 			= (LinkedPageParser) _pageInfoClassArrayList.get(i);
						HashMap<String, Object> linkedData		= (HashMap<String, Object>) _pageInfoArrayList.get(i);
						
//						LinkedPageParser linkedParser = new LinkedPageParser(m_appParams);
						
//						HashMap<String, Object> linkedData = linkedParser.loadTotalPage(mPageAr,mDataSet );
						int _totPage = (Integer) linkedData.get("totPage");
						ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
						HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
						
						cloneData = _page.getAttribute("divide");
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
						
						_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
							
						for ( j = 0; j < _totPage; j++) {
							
							// Section Page 지정
							mFunction.setSectionCurrentPageNum(j);
							mFunction.setSectionTotalPageNum(_totPage);

							
							if( !clonePage || j%_cloneRepCnt == 0 )
							{
								pageInfoData = new HashMap<String, Object >();
								_objects = new ArrayList<HashMap<String,Object>>();
							}
							
							mFunction.setCloneIndex( j%_cloneRepCnt);
							
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
								cloneX = UBIPRINTPOSITIONX;
								cloneY = UBIPRINTPOSITIONY;
							}
							
							_objects = linkedParser.createLinkedPageItem(j, retArr, newDataSet, mParam, cloneX, cloneY, _objects, mTOTAL_PAGE_NUM, _argoPage);
							
							if( !clonePage || j%_cloneRepCnt == (_cloneRepCnt-1) || (j == _totPage-1) )
							{
								if( printMode == false || ( _printCurrentPageIndex >= _printS && _printCurrentPageIndex < _printM ) )
								{
									pageInfoData.put("pageData", _objects );
									pageInfoData.put("page", _pageHash);
									
									if( !isExport ) pages.put( String.valueOf(_argoPage), pageInfoData);
									_argoPage = _argoPage + 1;
								}
								_printCurrentPageIndex = _printCurrentPageIndex+1;
								
								//log.debug(getClass().getName() + "::" + "Continue Doc  "  + j);
								
								if( isExport ){
									pageProp.clear();
									pageProp.put("cPHeight", _page.getAttribute("height"));
									pageProp.put("cPWidth", _page.getAttribute("width"));
									pageProp.put("bPageHeight", beforePageHeight);
									beforePageHeight += pageHeight;
									_objects.add(pageProp);
									pagesForExport.add(_objects);
									if( isExcelExport ){
										wb = _ubFormToExcel.xmlParsingExcel(pagesForExport, wb, _argoPage-1, "", false, 0);
										pagesForExport.clear();
										Thread.sleep(10);
									}
									else if( isPPTExport ){
										ss = _ubFormToPPT.xmlParsingPPT(pagesForExport, ss, _argoPage-1);
										pagesForExport.clear();
										Thread.sleep(10);
									}
									else if( isWordExport ){
										resultDoc = _ubFormToWord.xmlParsingWord(pagesForExport, resultDoc, _argoPage-1);
										pagesForExport.clear();
										Thread.sleep(10);
										end = new Date();
										long diff = end.getTime() - start.getTime();
										long diffSec = diff / 1000% 60;         
										long diffMin = diff / (60 * 1000)% 60;        
										log.info(getClass().getName() + "::" +  ">>>>> Word Export Total : [ " + diffMin +":"+diffSec+"."+diff%1000+" ]");
										
//										if( _argoPage % 100 == 0 ){
//											docList.add(resultDoc);
//											resultDoc = WordprocessingMLPackage.createPackage();
//											_ubFormToWord.settingWord(resultDoc);
//										}
									}
								}
							}
						}
						i = mPageNumList.size();
						// LinkedForm의 경우 한페이지로 모든 페이지생성이 완료됨
						break;
					default:
	//					if( isExport ) pagesForExport.add(pageObj);
						_argoPage = _argoPage + 1;
						break;
					}
				}
			}
			WriteChartData( this.mChartData );
			WriteImageData( this.mImgData );
		}
		
		
		//변수 clear
		_totPageCheckParser = null;
		_pageInfoMap.clear();
		mPageAr.clear();
		mPageNumList.clear();
		mPageNumRealList.clear();
		_pageInfoArrayList = null;
		_pageInfoClassArrayList = null;
		mDataSet = null;
		
		if( isExport ){
//			if( _argoPage % 100 != 0 ){
//				docList.add(resultDoc);
//			}
			return null;
//			return pagesForExport;
		}
		
		// 페이지들의 배열이 담긴 결과값 리턴
		//mHashMap.put("pageDatas", hashmapToJsonStr(pages) );
		mHashMap.put("pageDatas", pages );
		return null;
	}
	
	
	// 하나의 페이지를 생성하여, 생선된 하나의 페이지를 클라이언트로 전송한다.
	private void reportTypeSearch() throws UnsupportedEncodingException , Exception
	{
		mHashMap.put("resultType","FORM");

		ArrayList<HashMap<String, Object>> _objects = null;
		HashMap<String, String> _pageHash = new HashMap<String, String>();
		int _reportType = 0;
		int i = 0;
		int max = 0;
		
		String _client_ssid = m_appParams.getREQ_INFO().getCLIENT_SESSION_ID();		
		
		String _PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
		String _FORM_ID = m_appParams.getREQ_INFO().getFORM_ID();
		String _CLIENT_IP = m_appParams.getREQ_INFO().getCLIENT_IP();
		
//		TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(this.mServiceReqMng, m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );
		TotalPageCheckParser _totPageCheckParser = new TotalPageCheckParser(m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );
		HashMap<String, Object> _pageInfoMap = null;
		
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
			 _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
		}
		
//		HashMap<String, Object> _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
		
		mPageAr 			= (ArrayList<Element>) _pageInfoMap.get("PAGE_AR");
		mPageNumList 		= (ArrayList<Integer>) _pageInfoMap.get("PAGE_NUMLIST");
		mPageNumRealList 	=  (ArrayList<Integer>) _pageInfoMap.get("REAL_PAGE_NUMLIST");
		mTOTAL_PAGE_NUM 	= (Integer) _pageInfoMap.get("TOTALPAGE");
		ArrayList<Object> _pageInfoArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_DATA_LIST"); 
		ArrayList<Object> _pageInfoClassArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_CLASS_LIST"); 
		
		ArrayList<Object> _pageInfoREQList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_REQ_LIST");
		ArrayList<Object> _pageInfoTIDXList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_TIDX_LIST");
		ArrayList<Object> _reqlist = new ArrayList<Object>();
		ArrayList<Object> _tabIndexlist = new ArrayList<Object>();
		
		// 페이지 속성의 그룹핑된 데이터가 존재할경우 담겨있는 배열 ( 없을경우 페이지별로 null이 담겨있음 )
		ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>> _pageSubDataSet = (ArrayList<HashMap<String, ArrayList<HashMap<String,Object>>>>) _pageInfoMap.get("TEMP_DATASET_LIST");
		HashMap<String, ArrayList<HashMap<String, Object>>> _tempDataSet = null;
		
		Element _page = getPage();
		
		if( _page != null &&  mPageAr.indexOf(_page) != -1 )
		{
			i = getPageIndex();
//			i = mPageAr.indexOf(_page);
		}
		
		_pageHash = mPropertyFn.getAttrObject(_page.getAttributes());
		_reportType = Integer.parseInt(_page.getAttribute("reportType"));
		
		String cloneData = "";
		
		float cloneX = 0;
		float cloneY = 0;
		boolean clonePage = false;
		
		float originalPageWidth = 0;
		float originalPageHeight = 0;
		ArrayList<Float> _clonePositionList = null;
		
		float pageWidth = Float.valueOf(_page.getAttribute("width"));
		float pageHeight = Float.valueOf(_page.getAttribute("height"));
		
		// Clone 페이지의 경우 width와 height값을 담아두기
		originalPageWidth = pageWidth = Float.valueOf(_page.getAttribute("width"));
		originalPageHeight = pageHeight = Float.valueOf(_page.getAttribute("height"));
		// groupData일때 클론페이지시 연결여부 체크
		boolean isConnectGroupPage = false;
		// groupData일때 클론페이지시 연결시 인덱스 체크
		int _clonePageCnt = 0;
		// clonePage의 페이지 인덱스값
		int _pageRepeatCnt = 0;
		
		int _argoDocCnt = 0;
		int _docTotalPage = mTOTAL_PAGE_NUM;
		
		if( _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
		{
			if( mPageNumList.size()-1 == i )
			{
				isConnectGroupPage = false;
			}
			else
			{
				isConnectGroupPage = true;
			}
			
			if( i > 0  && mPageAr.get(i-1).getAttribute("id").toString().equals(_page.getAttribute("id").toString()) == false )
			{
				_clonePageCnt = 0;
			}
		}
		else
		{
			_clonePageCnt = 0;
		}
		
		// Clone Page값 담기
		cloneData = _page.getAttribute("clone");
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
		mFunction.setDatasetList(_tempDataSet);
		
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
		
		// 파라미터를 페이지별로 셋팅하기 위하여 작업
		if( _page.hasAttribute("FORM_IDX") )
		{
			int _DocumentIdx = Integer.parseInt(_page.getAttribute("FORM_IDX").toString() );
			mParam = documentParams.get(_DocumentIdx);
			
			_docTotalPage = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_TOTAL_PAGE")).get(_DocumentIdx);
			_argoDocCnt = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_START_IDX")).get(_DocumentIdx);
		}
		
		
		
		switch (_reportType) {
		case 0: //coverPage
			// Section Page 지정
			mFunction.setSectionCurrentPageNum(0);
			mFunction.setSectionTotalPageNum(1);
			mFunction.setCloneIndex( 0 );
			_objects = mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam ,0, _docTotalPage , mPAGE_NUM-_argoDocCnt);
//			_objects = mCreateFormFn.CreateFreeForm(mDataSetFn, _page , marDataSetItems , mParam , 0, 1,mDataSet,mTOTAL_PAGE_NUM);
			break;
		case 1: //freeform
		case 7: //mobile타입
		case 9: //webPage
			int _start = 0;
			int _max = 0;
			
			if(clonePage)
			{
				_start = mDATA_CNT*_cloneRepCnt;
				_max = _start + _cloneRepCnt;
				//마지막 페이지가 max값보다 작을경우 처
				if( _max >= mPageNumRealList.get( mPageAr.indexOf(_page)) )
				{
					_max = mPageNumRealList.get( mPageAr.indexOf(_page) );
				}
			}
			else
			{
				_start = mDATA_CNT;
				_max = mDATA_CNT + 1;
			}
			
			for (int k = _start; k < _max; k++) {
				
				mFunction.setSectionCurrentPageNum(k);
				mFunction.setSectionTotalPageNum(_max);
				_pageRepeatCnt = (k + _clonePageCnt)%_cloneRepCnt;
				mFunction.setCloneIndex(_pageRepeatCnt);
				
				if( !clonePage || k%2 == 0 )
				{
					_objects = new ArrayList<HashMap<String, Object>>();
				}
				
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
					int _pageIndex = mPageAr.indexOf(_page); 
					int _pageCnt = mPageNumList.get( _pageIndex ); 
					
					int _pageContinueCnt = Integer.valueOf( String.valueOf( mDocument.getDocumentElement().getAttribute("pageCount") ) );
					int _pageContinuePageDataCnt = (int)  Math.floor( ((_pageIndex*_pageCnt)+k)/_pageContinueCnt ) ;
					int _pageContinuePageCnt = (int)  ((_pageIndex*_pageCnt)+k)%_pageContinueCnt;
					if(clonePage)
					{
						_pageContinuePageCnt = (int)  ((_pageIndex*(_pageCnt*2))+k)%_pageContinueCnt;
						_pageContinuePageDataCnt = (int)  Math.floor( ((_pageIndex*(_pageCnt*2))+k)/_pageContinueCnt ) ;
					}
					Element _chkPage = mPageAr.get(_pageContinuePageCnt);
					
					_objects = mCreateFormFn.createFreeFormConnect(_chkPage , _tempDataSet , mParam , _pageContinuePageDataCnt, cloneX, cloneY, _objects, _docTotalPage ,  mPAGE_NUM-_argoDocCnt);
				}
				else
				{
					_objects = mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , k, cloneX, cloneY, _objects, _docTotalPage , mPAGE_NUM-_argoDocCnt);
				}
				
//				_objects = mCreateFormFn.createFreeFormConnect(_page , mDataSet , mParam , k, cloneX, cloneY, _objects, mTOTAL_PAGE_NUM , mPAGE_NUM);
			}
			
//			_objects = mCreateFormFn.CreateFreeFormAll(_page , mDataSet , mParam ,mDATA_CNT, mTOTAL_PAGE_NUM , mPAGE_NUM);
//			_objects = mCreateFormFn.CreateFreeForm(mDataSetFn, _page , marDataSetItems , mParam , mDATA_CNT, 1,mDataSet,mTOTAL_PAGE_NUM);
			break;
			
		case 2: //masterBand
			
			MasterBandParser masterParser = (MasterBandParser) _pageInfoClassArrayList.get(i);
			masterParser.setFunction(mFunction);
			ArrayList<Object> masterInfo 					= (ArrayList<Object>) _pageInfoArrayList.get(i);
			
//			MasterBandParser masterParser = new MasterBandParser(this.m_appParams);
//			masterParser.setFunction(mFunction);
//			ArrayList<Object> masterInfo 					= masterParser.loadTotalPage(_page, mDataSet,0, pageHeight, pageWidth);
			int _masterTotPage 								= (Integer) masterInfo.get(0);
			ArrayList<Object> masterList 					= (ArrayList<Object>) masterInfo.get(1);
			
			if(  masterInfo.size() > 4 )
			{
				mFunction.setGroupBandCntMap((HashMap<String, Integer>) masterInfo.get(4));
			}
			
			// Section Page 지정
			mFunction.setSectionCurrentPageNum(mDATA_CNT);
			mFunction.setSectionTotalPageNum(_masterTotPage);
			// clone페이지의 포지션 인덱스값
			
			_objects = new ArrayList<HashMap<String,Object>>();
			if(clonePage)
			{
				mDATA_CNT = mDATA_CNT*_cloneRepCnt;
				
				max = mDATA_CNT + _cloneRepCnt;
				//마지막 페이지가 max값보다 작을경우 처리
				if( max >= _masterTotPage )
				{
					max = _masterTotPage;
				}
			}
			else
			{
				max = mDATA_CNT + 1;
			}
			
			_objects = new ArrayList<HashMap<String,Object>>();
			for ( i = mDATA_CNT; i < max; i++) {
				
				_pageRepeatCnt = (i + _clonePageCnt)%_cloneRepCnt;
				mFunction.setCloneIndex(_pageRepeatCnt);
				
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
				
				_objects = masterParser.createMasterBandData(i, masterList, pageWidth, pageHeight, mParam, cloneX, cloneY, _objects , _docTotalPage,mPAGE_NUM - _argoDocCnt );
				
			}
			
			break;
			
		case 3: //continueBand
			// test
			
			ContinueBandParser continueBandParser = (ContinueBandParser) _pageInfoClassArrayList.get(i);
			ArrayList<Object> bandAr 					= (ArrayList<Object>) _pageInfoArrayList.get(i);
			
//			ContinueBandParser continueBandParser = new ContinueBandParser(m_appParams);
			continueBandParser.setImageData(this.mImgData);
			continueBandParser.setChartData(this.mChartData);
			continueBandParser.setFunction(mFunction);
//			ArrayList<Object> bandAr = continueBandParser.loadTotalPage(_page, mDataSet,0, pageHeight, pageWidth );
			HashMap<String, BandInfoMapData> bandInfo = (HashMap<String, BandInfoMapData>) bandAr.get(0);
			ArrayList<BandInfoMapData> bandList =(ArrayList<BandInfoMapData>) bandAr.get(1);
			ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
			HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
			
			if(  bandAr.size() > 8 )
			{
				mFunction.setGroupBandCntMap((HashMap<String, Integer>) bandAr.get(8));
			}
			
			// group
			HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		
			ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	
			
			continueBandParser.mOriginalDataMap = originalDataMap;
			continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
			
			boolean isPivot = false;
			float _bandMaxWidth = 0;
			
			if(_page.getAttribute("isPivot").equals("true"))
			{
				isPivot = true;
			}
			
			mFunction.setCloneIndex(_pageRepeatCnt);
			int _pagesRowListSize = pagesRowList.size();
			
			// Section Page 지정
			mFunction.setSectionCurrentPageNum(mDATA_CNT);
			mFunction.setSectionTotalPageNum(_pagesRowListSize);
			
			if(clonePage)
			{
				mDATA_CNT = mDATA_CNT*_cloneRepCnt;
				
				max = mDATA_CNT + _cloneRepCnt;
				//마지막 페이지가 max값보다 작을경우 처
				if( max >= pagesRowList.size() )
				{
					max = pagesRowList.size();
				}
			}
			else
			{
				max = mDATA_CNT + 1;
			}
			
			if( _page.getAttribute("fitOnePage") != null && _page.getAttribute("fitOnePage").equals("true"))
			{
				continueBandParser.setFitOnePage( true );
				if(continueBandParser.getFitOnePageHeight() > 0 ) _pageHash.put("height", String.valueOf(continueBandParser.getFitOnePageHeight()) );
			}
			
			_objects = new ArrayList<HashMap<String,Object>>();
			for ( i = mDATA_CNT; i < max; i++) {
				
				// clone페이지의 포지션 인덱스값
				_pageRepeatCnt = (mDATA_CNT + _clonePageCnt)%_cloneRepCnt;
				mFunction.setCloneIndex(_pageRepeatCnt);
				int k=0;
				int _pageIndex = mPageAr.indexOf( _page );
				if(_pageInfoREQList.size()>_pageIndex && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(_pageIndex)).size() > i)
				{
						ArrayList<HashMap<String, Object>> _pageInfoReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoREQList.get(i)).get(i);
					if(_pageInfoReq != null )
					{
						for ( k = 0; k < _pageInfoReq.size(); k++) {
							_reqlist.add( _pageInfoReq.get(k) );
						}
					}
				}
				
				if(_pageInfoTIDXList.size()>_pageIndex && ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(_pageIndex)).size() > i)
				{
					ArrayList<HashMap<String, Object>> _pageTIDXReq = ((ArrayList<ArrayList<HashMap<String, Object>>>) _pageInfoTIDXList.get(i)).get(i);
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

					_bandMaxWidth = 0;
					_pageHash.put("width", _page.getAttribute("width"));
				}
				else
				{
					cloneY = 0;
					cloneX = 0;
				}
				
				_objects =  continueBandParser.createContinueBandItems( i, _tempDataSet, bandInfo, bandList, pagesRowList, mParam,crossTabData,cloneX,cloneY,_objects,_docTotalPage, mPAGE_NUM - _argoDocCnt , isPivot);
				
				_bandMaxWidth = _bandMaxWidth + continueBandParser.getBandMaxWidth();

				if( Float.valueOf( _pageHash.get("width") ) < _bandMaxWidth )
				{
					_pageHash.put("width", Float.valueOf(_bandMaxWidth ).toString() );
				}
				
			}
			
			break;
			
		case 4: //labelBand
			_objects = mCreateFormFn.CreateLabelBandAll(_page , _tempDataSet , mParam ,mDATA_CNT,mTOTAL_PAGE_NUM , mPAGE_NUM);
			break;
			
		case 5: //barcodePage
			
			break;
			
//		case 7: //mobile타입
			
//			break;
			
		case 8: //lastPage
			_objects = mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam ,0, _docTotalPage , mPAGE_NUM - _argoDocCnt );
//			_objects = mCreateFormFn.CreateFreeForm(mDataSetFn, _page , marDataSetItems , mParam , 0, 1,mDataSet,mTOTAL_PAGE_NUM);
			break;
			
//		case 9: //webPage
			
//			break;
			
		case 10: //subPage
			
			break;
			
		case 11: //GroupChart          메뉴 SaveAs중 PDF와 HTML만 사용
			
			break;
			
		case 12:	//ConnectLink
			// connectLink타입일경우  
			
			ConnectLinkParser connectLink		 = (ConnectLinkParser) _pageInfoClassArrayList.get(i);
			HashMap<String, Object> connectData  = (HashMap<String, Object>) _pageInfoArrayList.get(i);
			
//			ConnectLinkParser connectLink = new ConnectLinkParser(m_appParams);
			connectLink.setFunction(mFunction);
//			HashMap<String, Object> connectData = connectLink.loadPagesData(_page, mParam);
			int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
			// totalPage를 담아두고 각 페이지별로 for문을 돌면서 page를 리턴받기
			
			_objects = new ArrayList<HashMap<String,Object>>();
			connectLink.makeConnectPage(mDATA_CNT, connectData.get("pageInfoData"), _objects, _docTotalPage, mPAGE_NUM - _argoDocCnt );
			break;
			
		case 13: //신규폼
			
			break;
			
		case 14: //신규폼
			// Linked Project type 문서 타입
			LinkedPageParser linkedParser		 = (LinkedPageParser) _pageInfoClassArrayList.get(i);
			HashMap<String, Object> linkedData  = (HashMap<String, Object>) _pageInfoArrayList.get(i);
			
//			LinkedPageParser linkedParser = new LinkedPageParser(m_appParams);
			
//			HashMap<String, Object> linkedData = linkedParser.loadTotalPage(mPageAr,mDataSet );
			int _totPage = (Integer) linkedData.get("totPage");
			ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
			HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
			
			cloneData = _page.getAttribute("divide");
			clonePage = false;
			
			_objects = new ArrayList<HashMap<String,Object>>();
			
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
			
			if(clonePage)
			{
				mDATA_CNT = mDATA_CNT*2;
				
				max = mDATA_CNT + 2;
				//마지막 페이지가 max값보다 작을경우 처
				if( max >= _totPage )
				{
					max = _totPage;
				}
			}
			else
			{
				max = mDATA_CNT + 1;
			}
			
			for ( i = mDATA_CNT; i < max; i++) {
				
				if(clonePage && i%2 > 0 )
				{
					if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
					{
						cloneY = Float.valueOf( _page.getAttribute("height") ) / 2;
					}
					else
					{
						cloneX = Float.valueOf( _page.getAttribute("width") ) / 2;
					}
				}
				else
				{
					cloneY = 0;
					cloneX = 0;
				}
				
				_objects = linkedParser.createLinkedPageItem(i, retArr, newDataSet, mParam, cloneX, cloneY, _objects, _docTotalPage,  mPAGE_NUM - _argoDocCnt );
				
			}
			// LinkedForm의 경우 한페이지로 모든 페이지생성이 완료됨 
			break;
		case 99: //사용자 저장 문서
			
			break;

		}
		
		//mHashMap.put("objects", arrayListToJsonStr(_objects) );
		mHashMap.put("objects", _objects );
		_pageHash.put("totalPage", String.valueOf(mTOTAL_PAGE_NUM));
		//mHashMap.put("page", hashmapToJsonStr(_pageHash));
		mHashMap.put("page", _pageHash);
		
		
		//변수 clear
		_totPageCheckParser = null;
		_pageInfoMap.clear();
		mPageAr.clear();
		mPageNumList.clear();
		mPageNumRealList.clear();
		_pageInfoArrayList = null;
		_pageInfoClassArrayList = null;
		
		Date _chkDate = new Date();  
		log.info("[" + _client_ssid + "^" + _PROJECT_NAME + "/" + _FORM_ID + "^" + _CLIENT_IP + "] FORM PARSING COMPLETE  : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_chkDate) +"]" );
	
	}

	private String hashmapToJsonStr(HashMap _hashData) throws UnsupportedEncodingException
	{
		String _mapStr = JSONObject.toJSONString(_hashData);

		String _enCodeingStr = URLEncoder.encode(_mapStr, "UTF-8");

		return _enCodeingStr;
	}
	
	private String arrayListToJsonStr(ArrayList _arrayList) throws UnsupportedEncodingException
	{
		String _listStr = JSONArray.toJSONString(_arrayList);
		
		String _enCodeingStr = URLEncoder.encode(_listStr, "UTF-8");
		
		return _enCodeingStr;
	}
	
	
	private void pageTypeToTotalNum(int _pageType , Element _page, Object _lodeType) throws Exception
	{
		
		int _typePageNum = 0;
		int _typeRealPageNum = 0;
		float pageWidth = Float.valueOf(_page.getAttribute("width"));
		float pageHeight = Float.valueOf(_page.getAttribute("height"));
		String cloneData = "";
		boolean clonePage = false;
		
		ArrayList<Integer> mXArray = new ArrayList<Integer>();
		
		// Clone Page값 담기
		cloneData = _page.getAttribute("clone");
		clonePage = false;
		if( cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL))
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
		
		switch (_pageType) {
		case 0: //coverPage
			_typePageNum = 1;
			mCoverFlg = true;
			
			break;
			
		case 1: //freeform
		case 7: //mobile타입
		case 9: //webPage
			_typePageNum = mPageNumFn.getFreeFormTotalNum( _page , mDataSet);
			_typeRealPageNum = _typePageNum;
			
			if(clonePage)
			{
				_typePageNum = (int) Math.ceil((float) _typePageNum/2);
			}
			//_typePageNum = mPageNumFn.getFreeFormTotalNum( _page , mDataSetRowCountInfo);
			break;
			
		case 2: //masterBand
			// 로드 타입이 one일경우에만 masterBand의 총 페이지수를 구하기
			if( _lodeType != null && _lodeType.equals("one") )
			{
				
				MasterBandParser masterParser = new MasterBandParser(this.m_appParams);
				
				ArrayList<HashMap<String, Object>> _objects2 	= new ArrayList<HashMap<String,Object>>();
				ArrayList<Object> masterInfo;
				int _masterTotPage = 1;
				try {
					masterInfo = masterParser.loadTotalPage(_page, mDataSet,0, pageHeight, pageWidth, mXArray);
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
					_typePageNum =  (int) Math.ceil((float) _typePageNum/2);
				}
				else
				{
					_typePageNum = _typePageNum;
				}
				
			}
			break;
			
		case 3: //continueBand
			// 로드 타입이 one일경우에만 continueBand의 총 페이지수를 구하기
			if( _lodeType != null && _lodeType.equals("one") )
			{
				
				ContinueBandParser continueBandParser = new ContinueBandParser(m_appParams);
				ArrayList<Object> bandAr = new ArrayList<Object>();
				try {
					bandAr = continueBandParser.loadTotalPage(_page, mDataSet,0, pageHeight, pageWidth, mXArray, null, mParam );
				} catch (NumberFormatException e) {
					
				} catch (XPathExpressionException e) {
					
				} catch (UnsupportedEncodingException e) {
					
				}
				@SuppressWarnings("unchecked")
				ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList = (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
				
				if( clonePage )
				{
					_typePageNum =  (int) Math.ceil((float) pagesRowList.size()/2);
				}
				else
				{
					_typePageNum = pagesRowList.size();
				}
				_typeRealPageNum = pagesRowList.size();
				
			}
			break;
			
		case 4: //labelBand
			//_typePageNum = mPageNumFn.getLabelBandTotalNum( _page , mDataSetRowCountInfo);
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
			
			_typePageNum = mPageNumFn.getLabelBandTotalNum( _page , mDataSet, _pageColumnIndex, _pageRowIndex, _pageStIndex);
			_typeRealPageNum = _typePageNum;
			break;
			
		case 5: //barcodePage
			
			break;
			
//		case 7: //mobile타입
//			
//			break;
			
		case 8: //lastPage
			_typePageNum = 1;
			_typeRealPageNum = 1;
			mLastFlg = true;
			
			break;
			
//		case 9: //webPage
//			
//			break;
			
		case 10: //subPage
			
			break;
			
		case 11: //GroupChart         메뉴 SaveAs중 PDF와 HTML만 사용
			
			break;
			
		case 12: //LinkForm
			// 로드 타입이 one일경우에만 masterBand의 총 페이지수를 구하기
			if( _lodeType != null && _lodeType.equals("one") )
			{
				// connectLink타입일경우  
				/*
				ConnectLinkParser connectLink = new ConnectLinkParser(this.mServiceReqMng, m_appParams);
				
				HashMap<String, Object> connectData;
				try {
					connectData = connectLink.loadPagesData(_page, mParam, mXArray);
					int _connectTotPage = Integer.valueOf( connectData.get("totalpage").toString() );
					_typePageNum = _connectTotPage;
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				_typeRealPageNum = _typePageNum;
				*/
			}
			break;
			
		case 13: //신규폼
			
			break;
		case 14: //Linked Form Type 
			if( _lodeType != null && _lodeType.equals("one") )
			{

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
				
				cloneData = _page.getAttribute("divide");
				clonePage = false;
				
				// Clone 여부 확인
				if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL))
				{
					clonePage = true;
				}
				
				// Linked Project type 문서 타입
				LinkedPageParser linkedParser = new LinkedPageParser(m_appParams);
				
				HashMap<String, Object> linkedData = linkedParser.loadTotalPage(_tempPageAr,mDataSet, mXArray );
				int _totPage = (Integer) linkedData.get("totPage");
				ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
				HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
				
				if(clonePage)
				{
					_typePageNum = (int) Math.ceil((float) _totPage/2 );
				}
				else
				{
					_typePageNum = _totPage;
				}
				_typeRealPageNum = _totPage;
			}
			break;
			
		case 99: //사용자 저장 문서
			
			break;

		}
		mPageNumRealList.add(_typeRealPageNum);
		mTOTAL_PAGE_NUM = mTOTAL_PAGE_NUM + _typePageNum;
		mPageNumList.add(_typePageNum);
	}
	
	private Element getPage()
	{
		Element _page = null;
		
		int _currentNum = 0;
		
		_currentNum = mPAGE_NUM;
		
		for(int i = 0 ; i < mPageNumList.size(); i ++)
		{
			int _pNum = mPageNumList.get(i);
			
			if( _currentNum - _pNum < 0)
			{
				_page = mPageAr.get(i);
				mDATA_CNT = _currentNum;
				break;
			}
			else
			{
				_currentNum = _currentNum - _pNum;
			}
		}
			
		return _page;
	}
	
	private int getPageIndex()
	{
		int _currentNum = 0;
		int _resultPageIndex = 0;
		
		_currentNum = mPAGE_NUM;
		
		for(int i = 0 ; i < mPageNumList.size(); i ++)
		{
			int _pNum = mPageNumList.get(i);
			
			if( _currentNum - _pNum < 0)
			{
				_resultPageIndex = i;
				break;
			}
			else
			{
				_currentNum = _currentNum - _pNum;
			}
		}
		
		return _resultPageIndex;
	}
	
	private String getURL(HttpServletRequest req) 
	{

	    String scheme = req.getScheme();             // http
	    String serverName = req.getServerName();     // hostname.com
	    int serverPort = req.getServerPort();        // 80
//	    String contextPath = req.getContextPath();   // /mywebapp
//	    String servletPath = req.getServletPath();   // /servlet/MyServlet
//	    String pathInfo = req.getPathInfo();         // /a/b;c=123
//	    String queryString = req.getQueryString();          // d=789

	    // Reconstruct original requesting URL
	    StringBuffer url =  new StringBuffer();
	    url.append(scheme).append("://").append(serverName);

	    if ((serverPort != 80) && (serverPort != 443)) {
	        url.append(":").append(serverPort);
	    }

//	    url.append(contextPath).append(servletPath);
//
//	    if (pathInfo != null) {
//	        url.append(pathInfo);
//	    }
//	    if (queryString != null) {
//	        url.append("?").append(queryString);
//	    }
	    return url.toString();
	}
	
	
	public String WriteChartData(HashMap chartdata) throws Exception {
		
		if(chartdata.isEmpty())
			return null;
		
		String file_content = "";
		String file_path = "";
		String file_nm = "";     
		String write_type = "";
		String txt_nm = "chartdata";		
		
		String projName = m_appParams.getREQ_INFO().getPROJECT_NAME();
		String formName = m_appParams.getREQ_INFO().getFORM_ID();

		log.info(getClass().getName() + "::" +  "Call WriteChartData()...");
		try {
			file_path = Log.ufilePath + "UFile/project/" + projName + "/" + formName + "/";
			log.debug(getClass().getName() + "::" +  "filePath >>>>> " + file_path);
			
			File dir = new File(file_path);
			File[] listFile = null;
			int fileListSize = 0;
			
			if(dir.exists()) {
				listFile = dir.listFiles();
				if(listFile != null) fileListSize = listFile.length;
				
				for(int i = 0 ; i < fileListSize ; i++){
					File tmpFile = listFile[i];
					
					if(tmpFile.isDirectory()) continue;

					if(tmpFile.getName().indexOf(txt_nm) > -1) {
						FileInputStream in = new FileInputStream(tmpFile);
						ObjectInputStream s = new ObjectInputStream(in);
						HashMap fileObj = (HashMap) s.readObject();
						s.close();
						in.close();
						if(chartdata.equals(fileObj)){
							file_nm = tmpFile.getName();     
							write_type = "NO";
							break;
						}
					}
				
				}
			}
			
			if(!"NO".equals(write_type)){
				BufferedWriter wt = null;
//				file_nm = txt_nm + Long.toString(System.currentTimeMillis()/1000) + ".dat";
				file_nm = txt_nm+ ".dat";
				File sFile = new File(file_path + file_nm);
				
				if(!sFile.isFile()) {
					sFile.createNewFile();
					FileOutputStream  f = new FileOutputStream(sFile);
					ObjectOutputStream  s = new ObjectOutputStream (f);
			        s.writeObject(chartdata);
			        s.close();
			        f.close();
				}
				else {
					sFile.delete();
					sFile.createNewFile();
					FileOutputStream  f = new FileOutputStream(sFile);
					ObjectOutputStream  s = new ObjectOutputStream (f);
			        s.writeObject(chartdata);
			        s.close();
			        f.close();
				}
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
		
		return file_nm;
	}
	
	public String WriteImageData(HashMap imagedata) throws Exception {
		
		if(imagedata.isEmpty())
			return null;

		String file_content = "";
		String file_path = "";
		String file_nm = "";     
		String write_type = "";
		String txt_nm = "imagedata";
		
		
		String projName = m_appParams.getREQ_INFO().getPROJECT_NAME();
		String formName = m_appParams.getREQ_INFO().getFORM_ID();

		log.info(getClass().getName() + "::" +  "Call WriteImageData()...");
		try {
			file_path = Log.ufilePath + "UFile/project/" + projName + "/" + formName + "/";
			log.debug(getClass().getName() + "::" +  "filePath >>>>> " + file_path);
			
			File dir = new File(file_path);
			File[] listFile = null;
			int fileListSize = 0;
			
			if(dir.exists()) {
				listFile = dir.listFiles();
				if(listFile != null) fileListSize = listFile.length;
				
				for(int i = 0 ; i < fileListSize ; i++){
					File tmpFile = listFile[i];
					
					if(tmpFile.isDirectory()) continue;

					if(tmpFile.getName().indexOf(txt_nm) > -1) {
						FileInputStream in = new FileInputStream(tmpFile);
						ObjectInputStream s = new ObjectInputStream(in);
						HashMap fileObj = (HashMap) s.readObject();
						s.close();
						in.close();
						if(imagedata.equals(fileObj)){
							file_nm = tmpFile.getName();     
							write_type = "NO";
							break;
						}
					}
				
				}
			}
			
			if(!"NO".equals(write_type)){
				BufferedWriter wt = null;
//				file_nm = txt_nm + Long.toString(System.currentTimeMillis()/1000) + ".dat";
				file_nm = txt_nm+ ".dat";
				File sFile = new File(file_path + file_nm);
				
				if(!sFile.isFile()) {
					sFile.createNewFile();
					FileOutputStream  f = new FileOutputStream(sFile);
					ObjectOutputStream  s = new ObjectOutputStream (f);
			        s.writeObject(imagedata);
			        s.close();
			        f.close();
				}
				else {
					sFile.delete();
					sFile.createNewFile();
					FileOutputStream  f = new FileOutputStream(sFile);
					ObjectOutputStream  s = new ObjectOutputStream (f);
			        s.writeObject(imagedata);
			        s.close();
			        f.close();
				}
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
		
		return file_nm;
	}
	

	
	
	
	private ArrayList<ArrayList<HashMap<String,Object>>> getExportAllPages( Boolean isExport, HashMap<String, Object> _pInfo, boolean bSupportIE9, HashMap<String, Object> _changeItemList ) throws UnsupportedEncodingException , Exception
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
		boolean isTEXTExport = ( isExport && _ubformToText != null )? true : false;

		boolean mUseFileSplit =  m_appParams.getREQ_INFO().getUSE_FILE_SPLIT();
		
		String _exportType = "";
		String _excelOption = "";
		String _html_option = "";
		
		Date start = new Date();
		Date end = null;
		int _argoDocCnt = 0;
		int _newProjectIdx = 0;
		int _docTotalPage = mTOTAL_PAGE_NUM;
		
		log.debug("getExportAllPages Start ");
		
		_exportType = m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE();
		_excelOption = m_appParams.getREQ_INFO().getEXCEL_OPTION();
		_html_option = m_appParams.getREQ_INFO().getHTML_OPTION();
		
		if(m_appParams.getREQ_INFO().getPDF_EXPORT_TYPE().equals("PRINT"))
		{
			if( m_appParams.getREQ_INFO().getCIRCULATION().equals("") == false)
			{
				caMax = Integer.valueOf( m_appParams.getREQ_INFO().getCIRCULATION() );
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
			//_totPageCheckParser = new TotalPageCheckParser(this.mServiceReqMng, m_appParams);
			//_totPageCheckParser = new TotalPageCheckParser(m_appParams);
			_totPageCheckParser = new TotalPageCheckParser( m_appParams, mPageMarginTop, mPageMarginLeft, mPageMarginRight, mPageMarginBottom );
			
			_totPageCheckParser.setUseFileSplit(mUseFileSplit);
			
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
			   _pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
			}
			
//			_pageInfoMap = _totPageCheckParser.getTotalPage(mParam, mDocument, mDataSet, mFunction );
			
			if( mPDFProtectionInfo == null || mPDFProtectionInfo.isEmpty() ) mPDFProtectionInfo = _totPageCheckParser.getPdfProtectionInfo();
		}
		else
		{
			_pageInfoMap = _pInfo;
		}
		
		mPageAr 			= (ArrayList<Element>) _pageInfoMap.get("PAGE_AR");
		mPageNumList 		= (ArrayList<Integer>) _pageInfoMap.get("PAGE_NUMLIST");
		mPageNumRealList 	=  (ArrayList<Integer>) _pageInfoMap.get("REAL_PAGE_NUMLIST");
		mTOTAL_PAGE_NUM 	= (Integer) _pageInfoMap.get("TOTALPAGE");
		ArrayList<Object> _pageInfoArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_DATA_LIST"); 
		ArrayList<Object> _pageInfoClassArrayList = (ArrayList<Object>) _pageInfoMap.get("PAGE_INFO_CLASS_LIST"); 

		ArrayList<String> _downLoadFileNames = (ArrayList<String>) _pageInfoMap.get("DOWNLOAD_FILE_NAMES");
		boolean _useFileSplit = (_pageInfoMap!=null&&_pageInfoMap.containsKey("USE_SPLIT_FILE"))? _pageInfoMap.get("USE_SPLIT_FILE").toString().equals("true"):false;

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
		
//		DataServiceManager oService = this.mServiceReqMng.getServiceManager();
		
		String cloneData = "";
		float pageWidth = 0;
		float pageHeight = 0;


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
		try {
			NamedNodeMap projAttr = mDocument.getElementsByTagName("project").item(0).getAttributes();
			
			// formID가 있는지 체크
			String _formProp = "formId";
			if( projAttr.getNamedItem(_formProp) == null )
			{
				_formProp = "projectName";
			}
			_formNameStr = projAttr.getNamedItem(_formProp).getNodeValue();
			
			String _waterMark = projAttr.getNamedItem("waterMark").getNodeValue();
			if( _waterMark != null && !_waterMark.equals("") ){
				HashMap<String, Object> pageProp = new HashMap<String, Object>();
				pageProp.put("waterMark", _waterMark);
				pageObj = new ArrayList<HashMap<String,Object>>();
				pageObj.add(pageProp);
				pagesForExport.add(pageObj);
			}
		} catch (Exception e) {
			
		}
		
		HashMap<String, Object> pageProp = new HashMap<String, Object>();
		
		// 모든 페이지의 총 페이지수를 구하고 각 페이지별 화면을 그리기 위한 객체를 담아두기
		int mTOTAL_PAGE_NUM  = (Integer) _pageInfoMap.get("TOTALPAGE");
		
		this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPDF_START", Log.MSG_LP_START_MAKEPDF, Log.getMessage(Log.MSG_LP_START_MAKEPDF)));
		
		log.debug(getClass().getName() + "::" + "TOTAL PAGE =========>" + mTOTAL_PAGE_NUM);
		if( _ubFormToPDF != null ) _ubFormToPDF.setTotalPageCount(mTOTAL_PAGE_NUM);
		
		String mChartDataFileName = "chartdata.dat";
		ItemConvertParser mItemConvertFn = new ItemConvertParser(null, mChartDataFileName, m_appParams);
		
		mItemConvertFn.setChangeItemList(_changeItemList);
		
		mItemConvertFn.setIsExportType(_exportType);
		
		mCreateFormFn.setChangeItemList(_changeItemList);
		
		//Excel의 경우 이미지 사용여부를 담아둘 속성을 셋팅
		if( isExcelExport )
		{
			String _excelIncludeImage = mDocument.getDocumentElement().getAttribute("excelIncludeImage");
			_ubFormToExcel.setExcelIncludeImage(!_excelIncludeImage.equals("false"));
			
			String _excelUsePageHeight = mDocument.getDocumentElement().getAttribute("excelUsePageHeight");
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
		
		int _startIndex = 0;
		int _endIndex = 0;
		int _pageMaxCnt = 0;
		int _lastIndex = 0;
		
		int _pageStartCnt = 0;
		int _pageMaxinumCnt = 0;
		
		
		/*
		 *  //바로 인쇄 팝업 시 필요한 전체 페이지 정보 추가
            HashMap<String, Object> pageInfo = _xmlUbForm.getPageInfoMap();
			String totalPage = "0";
            if(pageInfo != null){
            	totalPage= pageInfo.get("TOTALPAGE").toString();
			}
            reqParam.put("TOTAL_PAGE", totalPage);
            
            String sPageRangeGubun = udmParams.getREQ_INFO().getPAGE_RANGE_GUBUN();
			String sStartPage =	udmParams.getREQ_INFO().getSTART_PAGE();
			String sEndPage = udmParams.getREQ_INFO().getEND_PAGE();

			if("0".equals(sPageRangeGubun) && Integer.parseInt(totalPage) > 0) {
				if("-1".equals(sStartPage)) {
					sStartPage = "1";
					sEndPage = totalPage;
				}
			}
			
            reqParam.put("PAGE_RANGE_GUBUN",  sPageRangeGubun);	
			reqParam.put("START_PAGE",  sStartPage);	
            reqParam.put("END_PAGE",  sEndPage);	
		 * 
		 */
		String _pageRangeGubun = m_appParams.getREQ_INFO().getPAGE_RANGE_GUBUN() != null ? m_appParams.getREQ_INFO().getPAGE_RANGE_GUBUN() : "0";
		if(mTOTAL_PAGE_NUM > 0) {
			switch(_pageRangeGubun) {
				case "1" :
					_printS = _printS - 1;
					_printM = _printS;
					break;
				case "2" :
					_printS = _printS - 1;
					_printM = _printM - 1;
					break;	
				default:
					if(_printS > -1)
						_printS = _printS - 1;
					_printM = mTOTAL_PAGE_NUM;
					break;			
			}
		}
		
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
				if( _printM > -1 )
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
				
				if(_startIndex > _endIndex)
				{
					_startIndex = _endIndex -1;
				}
				
				_argoPage = _startIndex;
				String _downFileName = "";
				
				for( i = 0 ; i < mPageNumList.size(); i ++){					
					
					Element _page = mPageAr.get(i);
					// 데이터셋을 매칭
					if( _pageSubDataSet.get(i) == null )
					{
						_tempDataSet = mDataSet;
					}
					else
					{
						_tempDataSet = _pageSubDataSet.get(i);
					}
					
					if( _downLoadFileNames != null && _downLoadFileNames.size() > i )
					{
						_downFileName = _downLoadFileNames.get(i);
					}
					else
					{
						_downFileName = "";
					}
					
					JSONObject _backgroundImageObj = ItemPropertyProcess.getPageBackgroundImage(_page, _tempDataSet, mParam, mFunction);
					
					// Export시 Background Image의 경우 PDF를 제외하고 나머지는 각각의 Class에 전달한다
					if( isExcelExport ){
						_ubFormToExcel.setBackgroundImage(_backgroundImageObj);
					}
					else if( isPPTExport ){
						_ubFormToPPT.setBackgroundImage(_backgroundImageObj);
					}
					else if( isWordExport ){
						_ubFormToWord.setBackgroundImage(_backgroundImageObj);
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
						_ubFormToPDF.setBackgroundColor(Integer.valueOf(_page.getAttribute("backgroundColor")));
					}else if("HWP".equals(_exportType)){
						_ubFormToHwp.setBackgroundImage(_backgroundImageObj);
					}						

					if( i == 0 ) _isNextDocument = true;
					else _isNextDocument = false;		// 프로젝트가 이전페이지와 다름을 담아두는 Boolean값
					
					if( !mUseMultiFormType && _page.hasAttribute("FORM_IDX") )
					{
						_DocumentIdx = Integer.parseInt(_page.getAttribute("FORM_IDX").toString() );
						_formName = _page.getAttribute("FORM_NAME").toString()+"_";
						if( _DocumentIdx != _argoDocumentIdx )
						{
							_argoDocumentIdx = _DocumentIdx;
							_isNextDocument = true;
						}
						
						_docTotalPage = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_TOTAL_PAGE")).get(_DocumentIdx);
						_argoDocCnt = ((ArrayList<Integer>) _pageInfoMap.get("DOCUMENT_START_IDX")).get(_DocumentIdx);
						
						mParam = documentParams.get(_DocumentIdx);
						
						// Excel 의 sheet에 이름을 지정하기 위해 FormID를 담아둔다. 
						_sheetName = _page.getAttribute("FORM_NAME").toString();
					}
					else
					{
						_sheetName = _formNameStr;
						_docTotalPage = mTOTAL_PAGE_NUM;
					}
					
					// page속성의 isPivot속성이 있을경우 처리
					HashMap<String, String> _pjMap = (HashMap<String, String>)  mHashMap.get("project");
					if( _page.hasAttribute("isPivot") && _page.getAttribute("isPivot").equals("true") )
					{
						_pjMap.put("pageWidth", _page.getAttribute("height"));
						_pjMap.put("pageHeight", _page.getAttribute("width"));
					}
					else
					{
						_pjMap.put("pageWidth", _page.getAttribute("width"));
						_pjMap.put("pageHeight", _page.getAttribute("height"));
					}
					
					// GroupData 사용시 Clone연속보기를 설정시 처리
					if( _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
					{
						isConnectGroupPage = true;
						isLastConnectGroupPage = false;
						
						if( i > 0  && mPageAr.get(i-1).getAttribute("id").toString().equals(_page.getAttribute("id").toString()) == false )
						{
							_clonePageCnt = 0;
							isConnectGroupPage = false;
						}

						if( i == mPageNumList.size() -1  || mPageAr.get(i+1).getAttribute("id").toString().equals(_page.getAttribute("id").toString()) == false )
						{
							isLastConnectGroupPage = true;
						}
					}
					else
					{
						_clonePageCnt = 0;
					}
					
					_reportType = Integer.parseInt(_page.getAttribute("reportType"));
					cloneData = _page.getAttribute("clone");
					
					clonePage = false;
					
					// Clone 페이지 일경우 Width/Height값을 나누워서 담는다
					pageWidth = Float.valueOf(_page.getAttribute("width"));
					pageHeight = Float.valueOf(_page.getAttribute("height"));
					
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
					
					if( _endIndex >= 0 && _pageMaxCnt > _endIndex )
					{
						_pageMaxCnt = _endIndex + 1;
					}
					
					if( mPageNumList.size()-1 == i )
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
					if( _page.hasAttribute("minimumResizeFontSize") && StringUtil.isInteger(_page.getAttribute("minimumResizeFontSize")) )
					{
						_minimumResizeFontSize = Integer.valueOf(_page.getAttribute("minimumResizeFontSize"));
						mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
					}
					
					float _pageExportWidth = Float.valueOf(_page.getAttribute("width"));
					float _pageExportHeight = Float.valueOf(_page.getAttribute("height"));
					
					if( "WORD".equals(_exportType) )
					{
						if( Float.valueOf(_page.getAttribute("width")) < Float.valueOf(_page.getAttribute("height")) )
						{
							if(Float.valueOf(_page.getAttribute("width")) > 794)
							{
								_pageExportWidth = 794;
								_pageExportHeight = 1123;
							}
						}
						else
						{
							if(Float.valueOf(_page.getAttribute("height")) > 794)
							{
								_pageExportWidth = 1123;
								_pageExportHeight = 794;
							}
						}
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
						
//						pageObj = mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam , 0 , mTOTAL_PAGE_NUM , _argoPage);
						mCreateFormFn.setFunction(mFunction);
						
							
						//TEST 테이블 내보내기를 위하여 테스트 
//						_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
//						if( "HWP".equals(_exportType) )
//						{
//							_objects =  mItemConvertFn.convertExportTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
//						}					
						
						pageInfoData = new HashMap<String, Object >();
						pageInfoData.put("pageData", _objects);
						pageInfoData.put("page", _pageHash);
						
						
						if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
						
						if( isExport ){
							pageProp.clear();
							pageProp.put("cPHeight", _pageExportHeight);
							pageProp.put("cPWidth", _pageExportWidth);
//							pageProp.put("cPHeight", _page.getAttribute("height"));
//							pageProp.put("cPWidth", _page.getAttribute("width"));
							
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
								if( _lastIndex == 0)
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
							else if( isTEXTExport )
							{
								_ubformToText.xmlPasingtoText(pagesForExport);
								pagesForExport.clear();
							}
							else if( _ubFormToPDF != null )
							{
								_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
								pagesForExport.clear();
							}
						}
						
						//this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPDF", "99999", "Make PDF Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));		
						_argoPage++;
						_printCurrentPageIndex = _printCurrentPageIndex + 1;
								
						break;
					case 1: //freeform
					case 7: //mobile타입
					case 9: //webPage
						int _start = 0;
						int _max = 0;
						
						for ( j = _startIndex; j < _pageMaxCnt; j++) {
							
							if(Log.printStop){
								break;					
							}
							
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
							
							for ( k = _start; k < _max; k++) {
								if( !clonePage || k%_cloneRepCnt == 0 )
								{
									pageInfoData = new HashMap<String, Object >();
		//							_objects.clear();
									_objects = new ArrayList<HashMap<String, Object>>();
								}
								
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
								if( mDocument.getDocumentElement().getAttribute("pageContinue").equals("true") )
								{
									int _pageCnt = mPageNumList.get(i);
									int _pageContinueCnt = Integer.valueOf( String.valueOf( mDocument.getDocumentElement().getAttribute("pageCount") ) );
									int _pageContinuePageDataCnt = (int)  Math.floor( ((i*_pageCnt)+k)/_pageContinueCnt ) ;
									int _pageContinuePageCnt = (int)  ((i*_pageCnt)+k)%_pageContinueCnt;
									if(clonePage)
									{
										_pageContinuePageCnt = (int)  ((i*(_pageCnt*2))+k)%_pageContinueCnt;
										_pageContinuePageDataCnt = (int)  Math.floor( ((i*(_pageCnt*2))+k)/_pageContinueCnt ) ;
									}
									Element _chkPage = mPageAr.get(_pageContinuePageCnt); 
									
									_objects = mCreateFormFn.createFreeFormConnect(_chkPage , _tempDataSet , mParam , _pageContinuePageDataCnt, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt );
								}
								else
								{
									_objects = mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , k, cloneX, cloneY, _objects, _docTotalPage , _argoPage - _argoDocCnt );
								}
								
								//TEST 테이블 내보내기를 위하여 테스트 
//								if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType))// || "HTML".equals(_exportType)
////								if( "HWP".equals(_exportType) )
//								{
//									_objects =  mItemConvertFn.convertExportTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
//								}else if( "HTML".equals(_exportType)){//테이블 내 아이템을 테이블의 하위 속성으로 설정 하여 리턴
//									_objects =  mItemConvertFn.convertExportHtmlTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
//								}
								
								_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
								
								mCreateFormFn.setFunction(mFunction);
							}
							
							pageInfoData = new HashMap<String, Object >();
							pageInfoData.put("pageData", _objects);
							pageInfoData.put("page", _pageHash);
							
							if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
							
							//this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPDF", "99999", "Make PDF Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));	
							_argoPage = _argoPage + 1;
							
							_printCurrentPageIndex = _printCurrentPageIndex + 1;
							
							if( isExport ){
								pageProp.clear();
								pageProp.put("cPHeight", _pageExportHeight);
								pageProp.put("cPWidth", _pageExportWidth);
								pageProp.put("backgroundColor", _page.getAttribute("backgroundColor"));
//								pageProp.put("cPHeight", _page.getAttribute("height"));
//								pageProp.put("cPWidth", _page.getAttribute("width"));
								
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
								else if( isTEXTExport )
								{
									_ubformToText.xmlPasingtoText(pagesForExport);
									pagesForExport.clear();
								}
								else if( _ubFormToPDF != null )
								{
									_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
									pagesForExport.clear();
								}
								
//								end = new Date();
//								long diff = end.getTime() - start.getTime();
//								long diffSec = diff / 1000% 60;         
//								long diffMin = diff / (60 * 1000)% 60;        
//								log.info(getClass().getName() + "::" +  ">>>>> Export Total : PAGE = " + _argoPage + " [ " + diffMin +":"+diffSec+"."+diff%1000+" ]");
								
							}
							
						}
						
						break;
					case 2: //masterBand
						
						MasterBandParser masterParser 	= (MasterBandParser) _pageInfoClassArrayList.get(i);
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
							
							if(Log.printStop){
								break;					
							}
//						for ( j = _startIndex; j < _masterTotPage; j++) {
//						for ( j = _startIndex; j < _pageMaxCnt; j++) {
							
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
							
							_objects = masterParser.createMasterBandData(j, masterList, pageWidth, pageHeight, mParam, cloneX, cloneY, _objects,_docTotalPage,  _argoPage - _argoDocCnt );
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
//							if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType) )
////							if( "HWP".equals(_exportType) )
//							{
//								_objects =  mItemConvertFn.convertExportTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
//							}
							
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
//									pageProp.put("cPHeight", _page.getAttribute("height"));
//									pageProp.put("cPWidth", _page.getAttribute("width"));
									
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
									else if( isTEXTExport )
									{
										_ubformToText.xmlPasingtoText(pagesForExport);
										pagesForExport.clear();
									}
									else if( _ubFormToPDF != null )
									{
										_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
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
						
						ContinueBandParser continueBandParser = (ContinueBandParser) _pageInfoClassArrayList.get(i);
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
						
						HashMap<String, BandInfoMapData> bandInfo 	= (HashMap<String, BandInfoMapData>) bandAr.get(0);
						ArrayList<BandInfoMapData> bandList 		= (ArrayList<BandInfoMapData>) bandAr.get(1);
						ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList 	= (ArrayList<ArrayList<HashMap<String, Value>>>) bandAr.get(2);
						HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> > crossTabData = (HashMap<String, ArrayList<ArrayList<HashMap<String, Value>>> >) bandAr.get(3);
						if( _objects == null ) _objects = new ArrayList<HashMap<String,Object>>();
						
						// group
						HashMap<String, String> originalDataMap = (HashMap<String, String>) bandAr.get(4);		// originalData값으 가지고 있는 객체
						ArrayList<ArrayList<String>> groupDataNamesAr = (ArrayList<ArrayList<String>>) bandAr.get(5);	// 그룹핑된 데이터명을 가지고 있는 객체
						
						continueBandParser.mOriginalDataMap = originalDataMap;
						continueBandParser.mGroupDataNamesAr = groupDataNamesAr;
						
						boolean isPivot = false;
						
						if(_page.getAttribute("isPivot").equals("true"))
						{
							isPivot = true;
						}
						
						if( _page.getAttribute("fitOnePage") != null && _page.getAttribute("fitOnePage").equals("true"))
						{
							continueBandParser.setFitOnePage( true );
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
							if(Log.printStop){
								break;					
							}
//						for ( j = 0; j < _pagesRowListSize; j++) {
//						for ( j = _startIndex; j < _pageMaxCnt; j++) {
							
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
							
//							_objects = continueBandParser.createContinueBandItems(j, _tempDataSet, bandInfo, bandList, pagesRowList, mParam, crossTabData,cloneX,cloneY,_objects,mTOTAL_PAGE_NUM, _argoPage, isPivot);
							
							/** */
							// Excel Band형태 내보내기 처리를 위해 사용 
							if( isExport && isExcelExport && _excelOption != null && _excelOption.equals("BAND") )
							{
								// Excel 내보내기 기능 사용시
								_objects = continueBandParser.createContinueBandItemsExcelList(_page, j, _tempDataSet, bandInfo, bandList, pagesRowList, mParam, crossTabData,cloneX,cloneY,_objects,_docTotalPage,  _argoPage - _argoDocCnt , isPivot, _ubFormToExcel, wb, _formName, true, _DocumentIdx);
							}
							else
							{
								_objects = continueBandParser.createContinueBandItems(j, _tempDataSet, bandInfo, bandList, pagesRowList, mParam, crossTabData,cloneX,cloneY,_objects,_docTotalPage,  _argoPage - _argoDocCnt , isPivot);
							}
							
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
//							if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType) )
//							{
//								_objects =  mItemConvertFn.convertExportTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType );
//							}
							
							if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) || ( !isConnectGroupPage && (j == pagesRowList.size()-1)) || ( isLastConnectGroupPage && (j ==  pagesRowList.size() -1 )) )
							{
								pageInfoData.put("pageData", _objects );
								pageInfoData.put("page", _pageHash);
								
								//this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPDF", "99999", "Make PDF Page : " +  (_argoPage+1) + "/" + _pageMaxCnt));	
								
								if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
								_argoPage = _argoPage + 1;
								
								_printCurrentPageIndex = _printCurrentPageIndex + 1;
								
								if( isExport ){
									pageProp.clear();
//									pageProp.put("cPHeight", _page.getAttribute("height"));
//									pageProp.put("cPWidth", _page.getAttribute("width"));

									pageProp.put("cPHeight", _pageExportHeight);
									pageProp.put("cPWidth", _pageExportWidth);
									
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
									else if( isTEXTExport )
									{
										_ubformToText.xmlPasingtoText(pagesForExport);
										pagesForExport.clear();
									}
									else if( _ubFormToPDF != null )
									{
										_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
										pagesForExport.clear();
									}
									
								}
								
							}
							
							//log.debug(getClass().getName() + "::" + "Continue Doc  "  + j);
						}

						if(isConnectGroupPage)
						{
						  _clonePageCnt = _clonePageCnt + _pagesRowListSize;
						}

						break;
					case 4: //labelBand
						for ( j = _startIndex; j < _pageMaxCnt; j++) {
							if(Log.printStop){
								break;					
							}
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
							pageObj = mCreateFormFn.CreateLabelBandConnect(_page , _tempDataSet , mParam,j, cloneX, cloneY, pageObj ,_docTotalPage , _argoPage - _argoDocCnt );
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
//							if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType) )
////							if( "HWP".equals(_exportType) )
//							{
//								pageObj =  mItemConvertFn.convertExportTableItems(pageObj, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
//							}
							
							pageInfoData.put("pageData", pageObj);
							pageInfoData.put("page", _pageHash);
							if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
							
							_argoPage = _argoPage + 1;
							
							_printCurrentPageIndex = _printCurrentPageIndex + 1;
							
							if( isExport ){
								pageProp.clear();
								pageProp.put("cPHeight", _pageExportHeight);
								pageProp.put("cPWidth", _pageExportWidth);
//								pageProp.put("cPHeight", _page.getAttribute("height"));
//								pageProp.put("cPWidth", _page.getAttribute("width"));
								
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
								else if( isTEXTExport )
								{
									_ubformToText.xmlPasingtoText(pagesForExport);
									pagesForExport.clear();
								}
								else if( _ubFormToPDF != null )
								{
									_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
									pagesForExport.clear();
								}
								
							}
							
						}
						break;
					case 8: //lastPage
						if(Log.printStop){
							break;					
						}
						// Section Page 지정
						mFunction.setSectionCurrentPageNum(0);
						mFunction.setSectionTotalPageNum(1);
						
						_objects = new ArrayList<HashMap<String, Object>>();
						_objects = mCreateFormFn.createFreeFormConnect(_page , _tempDataSet , mParam , 0, UBIPRINTPOSITIONX, UBIPRINTPOSITIONY, _objects, _docTotalPage ,  _argoPage - _argoDocCnt );

						pageInfoData = new HashMap<String, Object >();
//						pageObj = mCreateFormFn.CreateFreeFormAll(_page , _tempDataSet , mParam , 0,mTOTAL_PAGE_NUM , _argoPage);
						mCreateFormFn.setFunction(mFunction);
						
						mFunction.setCloneIndex(0);
						
						//TEST 테이블 내보내기를 위하여 테스트 
						_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
//						if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType) )
////						if( "HWP".equals(_exportType) )
//						{
//							_objects =  mItemConvertFn.convertExportTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
//						}
						
						pageInfoData.put("pageData", _objects);
						pageInfoData.put("page", _pageHash);
						if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
						
						_argoPage = _argoPage + 1;
						
						_printCurrentPageIndex = _printCurrentPageIndex + 1;
						
						if( isExport ){
							pageProp.clear();
							pageProp.put("cPHeight", _pageExportHeight);
							pageProp.put("cPWidth", _pageExportWidth);
//							pageProp.put("cPHeight", _page.getAttribute("height"));
//							pageProp.put("cPWidth", _page.getAttribute("width"));
							
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
							else if( isTEXTExport )
							{
								_ubformToText.xmlPasingtoText(pagesForExport);
								pagesForExport.clear();
							}
							else if( _ubFormToPDF != null )
							{
								_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
								pagesForExport.clear();
							}
							
						}
						
						break;
					case 12:	//ConnectLink
						// connectLink타입일경우  처리
						/*
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
//							if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType) )
////							if( "HWP".equals(_exportType) )
//							{
//								_objects =  mItemConvertFn.convertExportTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
//							}
							
							pageInfoData.put("pageData", _objects);
							pageInfoData.put("page", _pageHash);
							if( !isExport ) pages.put( String.valueOf(_printCurrentPageIndex), pageInfoData);
							
							_argoPage = _argoPage + 1;
							
							_printCurrentPageIndex = _printCurrentPageIndex + 1;
							
							if( isExport ){
								pageProp.clear();
								pageProp.put("cPHeight", _pageExportHeight);
								pageProp.put("cPWidth", _pageExportWidth);
//								pageProp.put("cPHeight", _page.getAttribute("height"));
//								pageProp.put("cPWidth", _page.getAttribute("width"));
								
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
								else if( isHTMLExport ){
									resultHtml = _ubFormToHTML.xmlParsingHTML(pagesForExport);
									pagesForExport.clear();
								}
							}
							
						}
						*/
						break;
					case 14:
						// Linked Project type 문서 타입
						
						LinkedPageParser linkedParser 			= (LinkedPageParser) _pageInfoClassArrayList.get(i);
						HashMap<String, Object> linkedData		= (HashMap<String, Object>) _pageInfoArrayList.get(i);
						
						//Export시 ExportType를 담아두기
						linkedParser.setIsExportType(_exportType);
						
						int _totPage = (Integer) linkedData.get("totPage");
						ArrayList<HashMap<String, Object>> retArr = (ArrayList<HashMap<String, Object>>) linkedData.get("pageDataAr");
						HashMap<String, ArrayList<HashMap<String, Object>>> newDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>) linkedData.get("newData");
						
						cloneData = _page.getAttribute("divide");
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

						_pageHash.put("totalPage", String.valueOf( mTOTAL_PAGE_NUM ));
						for ( j = _startIndex; j < _pageMaxCnt; j++) {
							if(Log.printStop){
								break;					
							}
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
							
							_objects = linkedParser.createLinkedPageItem(j, retArr, newDataSet, mParam, cloneX, cloneY, _objects, _docTotalPage,  _argoPage - _argoDocCnt );
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
//							if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType) )
////							if( "HWP".equals(_exportType) )
//							{
//								_objects =  mItemConvertFn.convertExportTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
//							}
							
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
//									pageProp.put("cPHeight", _page.getAttribute("height"));
//									pageProp.put("cPWidth", _page.getAttribute("width"));
									
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
									else if( isTEXTExport )
									{
										_ubformToText.xmlPasingtoText(pagesForExport);
										pagesForExport.clear();
									}
									else if( _ubFormToPDF != null )
									{
										_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
										pagesForExport.clear();
									}
									
								}
							}
							
							//log.debug(getClass().getName() + "::" + "Continue Doc  "  + j);
						}
						i = mPageNumList.size();
						// LinkedForm의 경우 한페이지로 모든 페이지생성이 완료됨
						break;
					case 16:
					
						RepeatedFormParser _repeatFormParser = (RepeatedFormParser)  _pageInfoClassArrayList.get(i);
						
						HashMap<String, Object> _repeatInfoData = (HashMap<String, Object>) _pageInfoArrayList.get(i);
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
								_objects = new ArrayList<HashMap<String,Object>>();
								
								_bandMaxWidth = 0;
								_pageHash.put("width", _page.getAttribute("width"));
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

							_objects = _repeatFormParser.createRepeatFormItems(j, _repeatInfoData, originalPageWidth, originalPageHeight, mParam, cloneX, cloneY, _objects, _docTotalPage, _argoPage - _argoDocCnt );
							
							//TEST 테이블 내보내기를 위하여 테스트 
							_objects = this.getConvertExportTableItems(mItemConvertFn,_objects, _page, _exportType, _html_option);
							
							if( !clonePage || _pageRepeatCnt == (_cloneRepCnt-1) || ( !isConnectGroupPage && (j == _pageMaxinumCnt-1 ) )  || (isLastConnectGroupPage && (j == _pageMaxinumCnt-1 )) )
							{
								pageInfoData.put("pageData", _objects );
								pageInfoData.put("page", _pageHash);
								
								pages.put( String.valueOf(_argoPage), pageInfoData);
								_argoPage = _argoPage + 1;

								
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
									else if( isTEXTExport )
									{
										_ubformToText.xmlPasingtoText(pagesForExport);
										pagesForExport.clear();
									}
									else if( _ubFormToPDF != null )
									{
										_ubFormToPDF.toPdfOnePage(pageInfoData, String.valueOf(_argoPage-1));
										pagesForExport.clear();
									}
									/*
									else if( isHTMLExport ){
										resultHtml = _ubFormToHTML.xmlParsingHTML(pagesForExport);
										pagesForExport.clear();
									}
									*/
								}
								
							}
						}
												
						if(isConnectGroupPage)
						{
							_clonePageCnt = _clonePageCnt + _pageMaxinumCnt;
						}
						
						break;

					default:
						_argoPage = _argoPage + 1;
						_printCurrentPageIndex = _printCurrentPageIndex + 1;
						break;
					}
					
					if(Log.printStop){
						break;					
					}
				}
				
				if(Log.printStop){
					break;					
				}
			}
			
			if(!Log.printStop){
				WriteChartData( mChartData );
				WriteImageData( mImgData );
			}
		}	
		
		if(!Log.previewPrintStop && !Log.pdfDownFlag){
			_totPageCheckParser = null;
			_pageInfoMap.clear();
			mPageAr.clear();
			mPageNumList.clear();
			mPageNumRealList.clear();
			_pageInfoArrayList = null;
			_pageInfoClassArrayList = null;
			mDataSet = null;
		}
//		if( !( ("PDF".equals(_exportType)||"PRINT".equals(_exportType)) && _ubFormToPDF == null ) )
//		{
//			mImageDictionary.destroy();
//			mImageDictionary=null;
//		}
		if(!Log.printStop){	
			this.m_appParams.getUDPClient().sendMessage(JSONConverter("SUCCESS", "MAKEPDF_END", Log.MSG_LP_END_MAKEPDF, Log.getMessage(Log.MSG_LP_END_MAKEPDF)));
		}
		if( isExport ){
			return null;
		}

		mHashMap.put("pageDatas", pages );
		return null;
	}	

	/**
	 * functino Name : getConvertExportTableItems</br>
	 * @return
	 */
	private ArrayList<HashMap<String, Object>>  getConvertExportTableItems(ItemConvertParser mItemConvertFn, ArrayList<HashMap<String, Object>> _objects ,Element _page , String _exportType, String _htmlOption  ){
				
		if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType)|| "TEXT".equals(_exportType) )	
		{
			_objects =  mItemConvertFn.convertExportTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
		}else if ("HTML".equals(_exportType)){
			
			if(_htmlOption.equals("DIV")){
				_objects =  mItemConvertFn.convertExportTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);
			}else{
				_objects =  mItemConvertFn.convertExportHtmlTableItems(_objects, Float.valueOf(  _page.getAttribute("width").toString()), Float.valueOf( _page.getAttribute("height").toString() ), _exportType);	
			}
		}		
		return _objects;
	}
	
	
	/**
	 * functino Name : getConvertExportTableItems</br>
	 * @return
	 */
	protected ArrayList<HashMap<String, Object>>  getConvertExportTableItems(ItemConvertParser mItemConvertFn, ArrayList<HashMap<String, Object>> _objects ,PageInfo _page , String _exportType, String _htmlOption  ){
				
		if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType) || "TEXT".equals(_exportType))
		{
			_objects =  mItemConvertFn.convertExportTableItems(_objects, _page.getWidth(), _page.getHeight(), _exportType);
		}else if ("HTML".equals(_exportType)){
			
			if(_htmlOption.equals("DIV")){
				_objects =  mItemConvertFn.convertExportTableItems(_objects,  _page.getWidth(), _page.getHeight(), _exportType);
			}else{
				_objects =  mItemConvertFn.convertExportHtmlTableItems(_objects,  _page.getWidth(), _page.getHeight(), _exportType);	
			}
		}		
		return _objects;
	}
	
	
	/**
	 * functino Name : getConvertExportTableItems</br>
	 * @return
	 */
	protected ArrayList<HashMap<String, Object>>  getConvertExportTableItems(ItemConvertParser mItemConvertFn, ArrayList<HashMap<String, Object>> _objects ,PageInfoSimple _page , String _exportType, String _htmlOption  ){
		
		if( "HWP".equals(_exportType) || "WORD".equals(_exportType) || "PPT".equals(_exportType) || "TEXT".equals(_exportType))
		{
			_objects =  mItemConvertFn.convertExportTableItems(_objects, _page.getWidth(), _page.getHeight(), _exportType);
		}else if ("HTML".equals(_exportType)){
			
			if(_htmlOption.equals("DIV")){
				_objects =  mItemConvertFn.convertExportTableItems(_objects,  _page.getWidth(), _page.getHeight(), _exportType);
			}else{
				_objects =  mItemConvertFn.convertExportHtmlTableItems(_objects,  _page.getWidth(), _page.getHeight(), _exportType);	
			}
		}		
		return _objects;
	}
	
	
	/**
	 * functino Name : getPrintOption</br>
	 * @return
	 */
	protected JSONObject getPrintOption( JSONObject _obj, JSONObject _param )
	{
		Properties properties = new Properties();
		JSONObject _printOptionObj = new JSONObject();
		
		try {
			properties.load(new FileInputStream(Log.basePath + "/WEB-INF/classes/ubiform.properties"));		//파일 경로 지정
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			// param 과 properties 에 속성이 없으면 true로 담는다.
			if(_param.containsKey("UBIPRINT_FIT_SIZE"))
			{
				String bShrinkToFit =  ((JSONObject) JSONValue.parse( _param.get("UBIPRINT_FIT_SIZE").toString() ) ).get("parameter").toString(); 
				_printOptionObj.put("UBIPRINT_FIT_SIZE", bShrinkToFit);
			}
			else if( properties.containsKey("printOption.bShrinkToFit") )
			{
				String bShrinkToFit = properties.getProperty("printOption.bShrinkToFit").toString();
				_printOptionObj.put("UBIPRINT_FIT_SIZE", bShrinkToFit);
			}
			else
			{
				_printOptionObj.put("UBIPRINT_FIT_SIZE", "true");
			}
			
			if(_param.containsKey("UBIPRINT_USE_UI"))
			{
				String bShrinkToFit =  ((JSONObject) JSONValue.parse( _param.get("UBIPRINT_USE_UI").toString() ) ).get("parameter").toString(); 
				_printOptionObj.put("UBIPRINT_USE_UI", bShrinkToFit);
			}
			else if( properties.containsKey("printOption.bUI") )
			{
				String bUI = properties.getProperty("printOption.bUI").toString();
				_printOptionObj.put("UBIPRINT_USE_UI", bUI);
			}
			else
			{
				_printOptionObj.put("UBIPRINT_USE_UI", "true");
			}
			
			if(_param.containsKey(GlobalVariableData.UB_PRINT_VIEW_USE_UI))
			{
				String vUi =  ((JSONObject) JSONValue.parse( _param.get(GlobalVariableData.UB_PRINT_VIEW_USE_UI).toString() ) ).get("parameter").toString(); 
				_printOptionObj.put(GlobalVariableData.UB_PRINT_VIEW_USE_UI, vUi);
			}
			else if( properties.containsKey("printOption.vUI") )
			{
				String vUi = properties.getProperty("printOption.vUI").toString();
				_printOptionObj.put(GlobalVariableData.UB_PRINT_VIEW_USE_UI, vUi);
			}
			else
			{
				_printOptionObj.put(GlobalVariableData.UB_PRINT_VIEW_USE_UI, "true");
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		_obj.put("printOption", _printOptionObj);
		
		return _obj;
	}
	
	
	/**
	 * functino Name : getUsedParameterPopup</br>
	 * @return
	 */
	protected JSONObject getUsedParameterPopup( JSONObject _obj )
	{
		Properties properties = new Properties();
		JSONObject _printOptionObj = new JSONObject();
		
		try {
			properties.load(new FileInputStream(Log.basePath + "/WEB-INF/classes/ubiform.properties"));		//파일 경로 지정
			// param 과 properties 에 속성이 없으면 true로 담는다.
			if( properties.containsKey("parameter.usePopup") )
			{
				String _usePopup = properties.getProperty("parameter.usePopup").toString();
				
				if( _usePopup.equals("false") )
				{
					_obj.remove("param");								// 파라미터를 제거.
					_obj.put("MESSAGE", Log.getMessage(Log.MSG_PARAMETERS_NOT_EXIT));	// 메시지를 담아서 전달.
				}
				
			}
						
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return _obj;
	}
	
	public void setDocument( ArrayList<String> _docs)
	{
		documents = _docs;
	}
	
	//  여러건의 폼을 하나로 표현시에 각각의 폼마다의 Param값을 담아둠
	public void setDocumentParams( ArrayList<JSONObject> _docParams )
	{
		documentParams = _docParams;
	}
	public ArrayList<JSONObject> getDocumentParams( )
	{
		return documentParams;
	}
	
	public void setDocumentInfos( ArrayList< HashMap<String,String> > _docInfos)
	{
		documentInfos = _docInfos;
	}
	public ArrayList<HashMap<String,String>> getDocumentInfos()
	{
		return documentInfos;
	}
	
	/**
	 * xml 을 이용하여 데이터 셋을 로드 
	 * 여러건의 폼을 이용하여 데이터를 로드시와 하나의 Form의 Dataset을 로드하는 분기를 둔다
	 * 
	 * 추후에는 데이터셋 이름을 문서명 + _ + 데이터셋명 으로 지정하도록 처리할 예정임
	 **/
	protected void getXmlToDataSet() throws Exception
	{
		//xmlToDataSet();
		if(mUseMultiFormType)
		{
			xmlToDataSetMergeForm();
		}
		else if(documents != null && documents.size() > 0)
		{
			xmlToDataSetMulti();
		}
		else
		{
			xmlToDataSet();
		}
	}
	

	private ArrayList<HashMap<String, Object>> getExportDataSetData( HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet , ArrayList<HashMap<String, Object>> _resultSet, String _TYPE, String _columnSeparator, String _formName )
	{
		
		HashMap<String, Object> _temp;
		
		for( String _key : _dataSet.keySet() )
		{
			String _resultStr = "";
			ArrayList<HashMap<String, Object>> _data = _dataSet.get(_key);
			int _rowSize = _data.size();
			
			_temp = new HashMap<String, Object>();
			
			for (int i = 0; i < _rowSize; i++) {
				
				HashMap<String, Object> _rowData = _data.get(i);
				String _resultRowStr = "";
				for( String _column : _rowData.keySet() )
				{
					if( "".equals(_resultRowStr) == false ) _resultRowStr = _resultRowStr + _columnSeparator;
					
					_resultRowStr = _resultRowStr + _rowData.get(_column);
				}
				
				if(_resultStr.equals("")==false) _resultStr = _resultStr + "\n";
				_resultStr = _resultStr + _resultRowStr;
				
			}
			
			_temp.put("FILE_NAME", _formName + _key+"."+ _TYPE.toLowerCase() );				
			_temp.put("FILE", _resultStr.getBytes() );
			_resultSet.add(_temp);
		}
		
		return _resultSet;
	}
	
	
	private String getNodeString(Node node) {
	    try {
	        StringWriter writer = new StringWriter();
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.transform(new DOMSource(node), new StreamResult(writer));
	        String output = writer.toString();
	        return output.substring(output.indexOf("?>") + 2);//remove <?xml version="1.0" encoding="UTF-8"?>
	    } catch (TransformerException e) {
	        e.printStackTrace();
	    }
	    return node.getTextContent();
	}
	
	private Element getStringNode( String _xml)
	{
		Element node = null;
		try {
			node = DocumentBuilderFactory
				    .newInstance()
				    .newDocumentBuilder()
				    .parse(new ByteArrayInputStream(_xml.getBytes()))
				    .getDocumentElement();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return node;
	}
	
	protected String getSheetName( String _formID, HashMap<String, ArrayList<HashMap<String, Object>>> _tempDataSet, JSONObject _param  )
	{
		String _sheetType = common.getPropertyValue("excelExport.sheetNameType");
		String _sheetName = common.getPropertyValue("excelExport.sheetName");
		
		String _resultSheetName = _formID;
		
		if( _sheetName != null && _sheetType != null && _sheetName.indexOf(".") != -1 && ( _sheetType.equals("PARAM") || _sheetType.equals("DATASET") ) )
		{
			
			try {
				String _dsName = _sheetName.substring(0,_sheetName.indexOf(".") );
				String _column = _sheetName.substring(_sheetName.indexOf(".") +1, _sheetName.length() );
				
				if( _sheetType.equals("PARAM") )
				{
					if(_param.containsKey(_column))
					{
						_resultSheetName =  ((JSONObject) JSONValue.parse( _param.get(_column).toString() ) ).get("parameter").toString(); 
					}
				}
				else
				{
					if( _tempDataSet.containsKey(_dsName) && _tempDataSet.get(_dsName) != null && _tempDataSet.get(_dsName).size() > 0  )
					{
						_resultSheetName = _tempDataSet.get(_dsName).get(0).get(_column).toString();
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				_resultSheetName = _formID;
			}
		}
		
		return _resultSheetName;
	}
	
	public void clearImageDictionary()
	{
		if(mImageDictionary != null)
		{
			mImageDictionary.destroy();
			mImageDictionary=null;
		}
	}
	
	private void setPdfProtectionInfo(Element _doc, HashMap<String, ArrayList<HashMap<String, Object>>> _dataset  )
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
	
	public HashMap<String, String> getPdfProtectedInfo()
	{
		return mPDFProtectionInfo;
	}
	
	
//	private HashMap<String, String> getPageHeightList( HashMap<String, String> _pageHash , ArrayList<Object> _pageParserList )
	private HashMap<String, String> getPageHeightList( HashMap<String, String> _pageHash , ArrayList<Object> _pageParserList, float _marginTop, float _marginLeft, float _marginRight, float _marginBottom )
	{
		JSONArray _heightArr = new JSONArray();
		JSONArray _widthArr = new JSONArray();
		float _height = 0;
		float _width = 0;
		float _h = 0;

		String cloneData;
		Element _page;
		int _cloneRowCnt = 1;
		
		// _pageCnt 구하기 페이지
		// 1. mPageNumRealList 를 이용하여 페이지 총 갯수 가져오기
		// 2. clone일경우 해당 mPageNumRealList 의 clone페이지count에 맞춰서 height담기
		// 3. 다음페이지가 isGroup사용에 useGroupPageClone값이 true일경우 페이지 카운트를 합쳐서 계산한다. 
		//////
		
		for(int i = 0; i < mPageNumList.size(); i++ )
		{
			_h = 0;
			
			int _pageCnt = mPageNumList.get(i);
			_height = Float.valueOf( mPageAr.get(i).getAttribute("height").toString() );
			_width = Float.valueOf( mPageAr.get(i).getAttribute("width").toString() );
			_page = mPageAr.get(i);
			
			if( _page.getAttribute("fitOnePage") != null  && _page.getAttribute("fitOnePage").equals("true") &&
			Integer.parseInt( _page.getAttribute("reportType") ) == 3 )
			{
				_h = ((ContinueBandParser) _pageParserList.get(i)).getFitOnePageHeight();
				
				if( _h > 0 ) _height = _h;
			}
			
			
			// 
			if( _page.hasAttribute("useGroupPageClone") && _page.getAttribute("useGroupPageClone").equals("true") )
			{
				cloneData = _page.getAttribute("clone");

				if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
				{
					_cloneRowCnt = 2;
				}
				else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM) )
				{
					_cloneRowCnt = Integer.parseInt(_page.getAttribute("cloneRowCount"));
				}
				_pageCnt = 0;
				
				for( int k=i; k < mPageNumRealList.size(); k++ )
				{
					if( mPageAr.get(k).getAttribute("id").toString().equals( _page.getAttribute("id").toString() ) == false )
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
	
	protected ubFormToTEXT _ubformToText;
	public String textExportManager(String _xml , HashMap _param, String _path) throws Exception
	{
		Object clientEditMode = _param.get("CLIENT_EDIT_MODE");
		log.debug(getClass().getName() + "::" + "Call hwpExportManager...clientEditMode=" + clientEditMode);
		
		// 초기화;
		resetVariable();

		mCLIENT_IP = (String) _param.get("CLIENT_IP");
		mPAGE_NUM = (Integer) _param.get("PAGE_NUM");
		
		Object loadType = _param.get("LOAD_TYPE");
		
		boolean bSupportIE9 = false;
		if( loadType != null && loadType.equals("div") )
		{
			bSupportIE9 = true;
		}
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _changeDataSet = (HashMap<String, ArrayList<HashMap<String, Object>>>)_param.get("CHANGE_DATASET");
		HashMap<String, Object> _changeItemList = (HashMap<String, Object>) _param.get("CHANGE_ITEM_DATA");
		
		Object _params = _param.get("PARAMS");
		if( _params.hashCode() != 0 )
		{
		 	Object ubObj = JSONValue.parseWithException((String)_params);	
		 	mParam = (JSONObject)ubObj;
		}

		// xml 을 document 형태로 변경.
		// Project 중 필요한 속성을 담음.
		xmlToDocument(_xml);
		
		if("ON".equals(clientEditMode) && _changeDataSet != null)	
		{
			log.debug(getClass().getName() + "::" + "Call hwpExportManager..._changeDataSet != null");
			mDataSet = _changeDataSet;
		}
		else
		{
			log.debug(getClass().getName() + "::" + "Call hwpExportManager..._changeDataSet == null");
			getXmlToDataSet();
		}
		
		// 로그 시간표시
		log.info( "SAVE TEXT START " );
		_ubformToText = new ubFormToTEXT(_path);
		if( !mParamResult )
		{
			getExportAllPages(true, null, bSupportIE9, _changeItemList);		//TEST
			_ubformToText.closeFileWriter();
		}
		else
		{
			_ubformToText.closeFileWriter();
			return null;
		}
		
		if( mImageDictionary != null )
		{
			clearImageDictionary();
		}
		
		log.debug(getClass().getName() + "::toHWP::" + "Strart creating HwpprocessingMLPackage...");		
			
		return "";
	}
	
	
	public UDMParamSet getAppParam()
	{
		return m_appParams;
	}
		
	
	public void setUseMultiFormType( boolean _flag )
	{
		mUseMultiFormType = _flag;
	}
	public boolean getUseMultiFormType()
	{
		return mUseMultiFormType;
	}
	
	private Document updateMergeProjectInfo( Document _doc )
	{
		NodeList _list = _doc.getDocumentElement().getElementsByTagName("project");
		NodeList _pages;
		int _cnt = _list.getLength();
		int _pageCnt = 0;
		HashMap<String, String> _documentInfo;
		Element _project; 
		Element _page;
		
		for(int i = 0; i < _cnt ; i++ )
		{
			_documentInfo = documentInfos.get(i);
			
			_project = (Element) _list.item(i);
			_project.getAttribute("");
			
			_pages = _project.getElementsByTagName("page");
			
			_pageCnt = _pages.getLength();
			
			for (int j = 0; j < _pageCnt; j++) {
				_page = (Element) _pages.item(j);
				_page.setAttribute("PROJECT_INDEX", String.valueOf(i));
				
				// 프로젝트정보를 각 페이지마다 Add시켜둔다.
				_page.setAttribute("PROJECT_NAME", _documentInfo.get("PROEJCT"));
				_page.setAttribute("FORM_NAME", _documentInfo.get("FORMID"));
				_page.setAttribute("FORM_IDX", String.valueOf( i ) );
			}
		}
		
		return mDocument;
	}

}
