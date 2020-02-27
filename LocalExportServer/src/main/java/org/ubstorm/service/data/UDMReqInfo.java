package org.ubstorm.service.data;

public class UDMReqInfo {
	
	public String getDOMAIN() {
		return DOMAIN;
	}
	public void setDOMAIN(String dOMAIN) {
		DOMAIN = dOMAIN;
	}
	public String getFILE_TYPE() {
		return FILE_TYPE;
	}
	public void setFILE_TYPE(String fILE_TYPE) {
		FILE_TYPE = fILE_TYPE;
	}
	public String getFORM_ID() {
		return FORM_ID;
	}
	public void setFORM_ID(String fORM_ID) {
		FORM_ID = fORM_ID;
	}
	public String getCALL() {
		return CALL;
	}
	public void setCALL(String cALL) {
		CALL = cALL;
	}

	public String getIS_TEST() {
		return IS_TEST;
	}
	public void setIS_TEST(String iS_TEST) {
		IS_TEST = iS_TEST;
	}

	public String getUBPARAMS() {
		return UBPARAMS;
	}
	public void setUBPARAMS(String uBPARAMS) {
		UBPARAMS = uBPARAMS;
	}

	private String UBPARAMS;		// JSON형태의 파라미터 집합
	
	private String IS_TEST;			// 요청 패킷 타입이 TEST용인지?
	
	private String DOMAIN;			// 도메인 이름 : Service config에 정의된 이름을 사용, 없으면 "default"를 사용
	private String FILE_TYPE;		// 요청 패킷 타입 : udm / udmv2 / exec
	private String FORM_ID;			// 폼 아이디
	private String CALL;			// 요청클라이언트 구분 값 : UQUERY / VIEWER / EDITOR / UBIFORM / VIEWER5

	private String METHOD_NAME;		// 요청 메소드의 이름
	private String FOLDER_NM;		// 폴더명
	private String ADMINID;			// UBForm Editor 로그인 아이디
	private String ADMINPW;			// UBForm Editor 로그인 패스워드
	private String PROJECT_NAME;	// 문서(프로젝트)명
	private String FILE_NAME;		// 파일명 
	private String LINK;			// 이미지를 다운로드 받기 위한 URL Link 
	private String URL;				// 원격지 XML파일의 경로
	private String FILE_CONTENT;	// base64로 인코딩된 file 내용
	private String FILE_XML;		// base64로 인코딩된 XML file 내용
	private String FILE_INFO_XML;	// base64로 인코딩된 INFO XML file 내용
	private String PATH;			// sample 문서가 저장되어 있는 경로
	private String SAVE_NM;			// 저장되는 파일의 이름
	private String PROJECT_ID;		// view log 파일 저장을 위한 프로젝트 아이디
	private String PARAM;			// TNS or UDM 구분명
	private String USER;			// UBIForm 사용자명
	private String TOOL;			// 요청 클라이언트 구분 명 (UBIFORM 인지 아닌지 구분하기 위해사용)
	private String NAME;			// UDM ID 또는 파일명
	
	private String LOAD_TYPE;		// 페이지 로드 형식 지정
	private String CLIENT_EDIT_MODE;		// 클라이언트에서 데이터 변경을 수행할 것인지 여부 형식 지정
	private String CLIENT_IP;		// 요청 클라이언트 IP주소
	private String ITEM_ID;		// 
	private String IMG_URL;		// 

	private String PDF_EXPORT_TYPE;		// 
	
	private boolean USE_FILE_SPLIT;
	
	private String MULTI_FORM_TYPE;		// 멀티폼 사용시 방법 ( A : 폼파일 합쳐서 생성, M : 폼파일별로 새로 조회하여 MERGE ) 
	
	public String getMULTI_FORM_TYPE() {
		return MULTI_FORM_TYPE;
	}
	public void setMULTI_FORM_TYPE(String mULTI_FORM_TYPE) {
		MULTI_FORM_TYPE = mULTI_FORM_TYPE;
	}
	
	public boolean getUSE_FILE_SPLIT() {
		return USE_FILE_SPLIT;
	}
	public void setUSE_FILE_SPLIT(boolean uSE_FILE_SPLIT) {
		USE_FILE_SPLIT = uSE_FILE_SPLIT;
	}

	private String PAGE_RANGE_GUBUN = "0";		// 0: 전체, 1:현재, 2:페이지범위지정
	public String getPAGE_RANGE_GUBUN() {
		return PAGE_RANGE_GUBUN;
	}
	public void setPAGE_RANGE_GUBUN(String pAGE_RANGE_GUBUN) {
		PAGE_RANGE_GUBUN = pAGE_RANGE_GUBUN;
	}

	private String START_PAGE;		// 
	private String END_PAGE;		// 
	private String CIRCULATION;		// 
	
	private String CHANGE_DATASET; 
	private String CHANGE_ITEM_DATA; 
	
	private boolean USE_PREVIEW;
	
	
	private String IS_UBPARAMS_ENCODED;	//암호화 여부
	
	private String EXCEL_OPTION;		// excel export option [ NORMAL:문서대로, DATA_ONLY:데이터만 ]
	private String EXCEL_FMT_OPTION;	// excel export file format option [ EXCEL2007:xlsx, EXCEL97:xls]
	private String HTML_OPTION;		// html export option [ DIV:div layout, TABLE : table layout]
	private boolean USE_TIMESTAMP;		//PDF Export 에 Timestamp를 삽입할 것인지 여부
	
	private String UB_PDF_PRINT_VERSION;	// pdf파일 생성시에 한페이지씩 처리할지 한번에 일괄 변환할지 여부를 지정
	
	private String UB_FORMLIST_INFO;		// MultiForm로드시 넘겨받을 여러건의 문서의 정보를 담아둔 문자열
	
	private String UB_EXPORT_FILE_PATH;		// 서버의 파일 저장 경로
	private String UB_EXPORT_FILE_NAME;		// 서버의 파일 이름 	
	
	private String PDF_EXPORT_PRINTER_TYPE;
	
	private String PDF_EXPORT_PRINTER_NAME;	// 프린터 이름
	
	private String PDF_SHOW_PRINTER_DIALOG; // 프린트시 dialog를 띄울지, 곧바로 프린트 할지 여부
	
	private String PDF_PRINTER_MARGIN;
	
	private String EDITOR_FONT_LOCALE;
	
	private String CSV_DELIMITER = ",";
	
	private String UB_EXPORT_TYPE = "";
	
	private String EXCEL_SHEET_SPLIT_TYPE = "";	// Excel의 sheet를 구분하는 값 none(기본과 동일, page (페이지별로 구분), formPage( editor의 page별로 ) ) 
	
	private String EXCEL_SHEET_NAMES = "";	// Excel의 sheet 의 이름들을 ,로 구분하여 사용
	
	private String PDF_PASSWORD = "";

	private String UB_OPEN_PARAM_POPUP = "";	// Excel의 sheet 의 이름들을 ,로 구분하여 사용
	
	private String PRINT_LANDSCAPE = "";
	
	private String PAGE_FIT = "";
	
	private String USE_JSON_FORM_FILE = "";
	
	private String USE_SIMPLE_JSON_FORM_FILE = "";
	
	private String PDF_READER_OPEN="";
	
	public String getPDF_READER_OPEN() {
		return PDF_READER_OPEN;
	}
	public void setPDF_READER_OPEN(String pDF_READER_OPEN) {
		PDF_READER_OPEN = pDF_READER_OPEN;
	}
	public String getUSE_JSON_FORM_FILE() {
		return USE_JSON_FORM_FILE;
	}
	public void setUSE_JSON_FORM_FILE(String uSE_JSON_FORM_FILE) {
		USE_JSON_FORM_FILE = uSE_JSON_FORM_FILE;
	}
	public void setUSE_SIMPLE_JSON_FORM_FILE(String uSE_SIMPLE_JSON_FORM_FILE) {
		USE_SIMPLE_JSON_FORM_FILE = uSE_SIMPLE_JSON_FORM_FILE;
	}
	public String getUSE_SIMPLE_JSON_FORM_FILE() {
		return USE_SIMPLE_JSON_FORM_FILE;
	}
	
	public String getPAGE_FIT() {
		return PAGE_FIT;
	}
	public void setPAGE_FIT(String pAGE_FIT) {
		PAGE_FIT = pAGE_FIT;
	}
	public String getUB_EXPORT_TYPE() {
		return UB_EXPORT_TYPE;
	}
	public void setUB_EXPORT_TYPE(String uB_EXPORT_TYPE) {
		UB_EXPORT_TYPE = uB_EXPORT_TYPE;
	}
	public String getCSV_DELIMITER() {
		return CSV_DELIMITER;
	}
	public void setCSV_DELIMITER(String cSV_DELIMITER) {
		CSV_DELIMITER = cSV_DELIMITER;
	}
	
	public String getPDF_EXPORT_PRINTER_TYPE() {
		return PDF_EXPORT_PRINTER_TYPE;
	}
	public void setPDF_EXPORT_PRINTER_TYPE(String pDF_EXPORT_PRINTER_TYPE) {
		PDF_EXPORT_PRINTER_TYPE = pDF_EXPORT_PRINTER_TYPE;
	}
	
	public String getPDF_PRINTER_MARGIN() {
		return PDF_PRINTER_MARGIN;
	}
	public void setPDF_PRINTER_MARGIN(String pDF_PRINTER_MARGIN) {
		PDF_PRINTER_MARGIN = pDF_PRINTER_MARGIN;
	}
	
	private String LICENSE_TYPE; //
	
	public String getLICENSE_TYPE() {
		return LICENSE_TYPE;
	}
	public void setLICENSE_TYPE(String lICENSE_TYPE) {
		LICENSE_TYPE = lICENSE_TYPE;
	}
	public String getPDF_SHOW_PRINTER_DIALOG() {
		return PDF_SHOW_PRINTER_DIALOG;
	}
	public void setPDF_SHOW_PRINTER_DIALOG(String pDF_SHOW_PRINTER_DIALOG) {
		PDF_SHOW_PRINTER_DIALOG = pDF_SHOW_PRINTER_DIALOG;
	}
	
	public String getPDF_EXPORT_PRINTER_NAME() {
		return PDF_EXPORT_PRINTER_NAME;
	}
	public void setPDF_EXPORT_PRINTER_NAME(String pDF_EXPORT_PRINTER_NAME) {
		PDF_EXPORT_PRINTER_NAME = pDF_EXPORT_PRINTER_NAME;
	}

	private String CLIENT_SESSION_ID;
	
	public String getCLIENT_SESSION_ID() {
		return CLIENT_SESSION_ID;
	}
	public void setCLIENT_SESSION_ID(String cLIENT_SESSION_ID) {
		CLIENT_SESSION_ID = cLIENT_SESSION_ID;
	}	

	public boolean getUSE_TIMESTAMP() {
		return USE_TIMESTAMP;
	}
	public void setUSE_TIMESTAMP(boolean uSE_TIMESTAMP) {
		USE_TIMESTAMP = uSE_TIMESTAMP;
	}
	
	public String getEXCEL_OPTION() {
		return EXCEL_OPTION;
	}
	public void setEXCEL_OPTION(String eXCEL_OPTION) {
		EXCEL_OPTION = eXCEL_OPTION;
	}
	
	public String getEXCEL_FMT_OPTION() {
		return EXCEL_FMT_OPTION;
	}
	public void setEXCEL_FMT_OPTION(String eXCEL_FMT_OPTION) {
		EXCEL_FMT_OPTION = eXCEL_FMT_OPTION;
	}
	
	public String getCHANGE_DATASET() {
		return CHANGE_DATASET;
	}
	public void setCHANGE_DATASET(String cHANGE_DATASET) {
		CHANGE_DATASET = cHANGE_DATASET;
	}
	
	public String getCHANGE_ITEM_DATA() {
		return CHANGE_ITEM_DATA;
	}
	public void setCHANGE_ITEM_DATA(String cHANGE_ITEM_DATA) {
		CHANGE_ITEM_DATA = cHANGE_ITEM_DATA;
	}
	
	public String getSHOW_LABEL() {
		return SHOW_LABEL;
	}
	public void setSHOW_LABEL(String sHOW_LABEL) {
		SHOW_LABEL = sHOW_LABEL;
	}

	private String SHOW_LABEL;
	
	private String MODEL_TYPE;		// AreaChart/LineChart의 chart_type, barcode의 code_type
	
	public String getMODEL_TYPE() {
		return MODEL_TYPE;
	}
	public void setMODEL_TYPE(String mODEL_TYPE) {
		MODEL_TYPE = mODEL_TYPE;
	}
	public String getIMG_TYPE() {
		return IMG_TYPE;
	}
	public void setIMG_TYPE(String iMG_TYPE) {
		IMG_TYPE = iMG_TYPE;
	}

	private String IMG_TYPE;		// Image Type
	
	public String getLOAD_TYPE() {
		return LOAD_TYPE;
	}
	public void setLOAD_TYPE(String lOAD_TYPE) {
		LOAD_TYPE = lOAD_TYPE;
	}
	
	public String getCLIENT_EDIT_MODE() {
		return CLIENT_EDIT_MODE;
	}
	public void setCLIENT_EDIT_MODE(String cLIENT_EDIT_MODE) {
		CLIENT_EDIT_MODE = cLIENT_EDIT_MODE;
	}

	private int PAGE_NUM;			// pageNumber
	
	public int getPAGE_NUM() {
		return PAGE_NUM;
	}
	public void setPAGE_NUM(int pAGE_NUM) {
		PAGE_NUM = pAGE_NUM;
	}
	
	public String getDEVICE_ID() {
		return DEVICE_ID;
	}
	public void setDEVICE_ID(String dEVICE_ID) {
		DEVICE_ID = dEVICE_ID;
	}
	public String getDEVICE_NAME() {
		return DEVICE_NAME;
	}
	public void setDEVICE_NAME(String dEVICE_NAME) {
		DEVICE_NAME = dEVICE_NAME;
	}

	private String DEVICE_ID;			// DEVICE ID
	private String DEVICE_NAME;			// DEVICE NAME
	
	public String getDATA_TYPE() {
		return DATA_TYPE;
	}
	public void setDATA_TYPE(String dATA_TYPE) {
		DATA_TYPE = dATA_TYPE;
	}

	private String DATA_TYPE;		// 요청 데이터 타입 (xml or json)

	public String getUSE_FIRSTLINE() {
		return USE_FIRSTLINE;
	}
	public void setUSE_FIRSTLINE(String uSE_FIRSTLINE) {
		USE_FIRSTLINE = uSE_FIRSTLINE;
	}
	private String USE_FIRSTLINE;
	
	public String getCTR_ID() {
		return CTR_ID;
	}
	public void setCTR_ID(String cTR_ID) {
		CTR_ID = cTR_ID;
	}
	public String getSVC_ID() {
		return SVC_ID;
	}
	public void setSVC_ID(String sVC_ID) {
		SVC_ID = sVC_ID;
	}

	private String CTR_ID;			// Spring controller ID
	private String SVC_ID;			// Spring service ID
	
	public String getMETHOD_NAME() {
		return METHOD_NAME;
	}
	public void setMETHOD_NAME(String mETHOD_NAME) {
		METHOD_NAME = mETHOD_NAME;
	}
	public String getFOLDER_NM() {
		return FOLDER_NM;
	}
	public void setFOLDER_NM(String fOLDER_NM) {
		FOLDER_NM = fOLDER_NM;
	}
	public String getADMINID() {
		return ADMINID;
	}
	public void setADMINID(String aDMINID) {
		ADMINID = aDMINID;
	}
	public String getADMINPW() {
		return ADMINPW;
	}
	public void setADMINPW(String aDMINPW) {
		ADMINPW = aDMINPW;
	}
	public String getPROJECT_NAME() {
		return PROJECT_NAME;
	}
	public void setPROJECT_NAME(String pROJECT_NAME) {
		PROJECT_NAME = pROJECT_NAME;
	}
	public String getFILE_NAME() {
		return FILE_NAME;
	}
	public void setFILE_NAME(String fILE_NAME) {
		FILE_NAME = fILE_NAME;
	}
	public String getLINK() {
		return LINK;
	}
	public void setLINK(String lINK) {
		LINK = lINK;
	}
	public String getURL() {
		return URL;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}
	public String getFILE_CONTENT() {
		return FILE_CONTENT;
	}
	public void setFILE_CONTENT(String fILE_CONTENT) {
		FILE_CONTENT = fILE_CONTENT;
	}
	public String getFILE_XML() {
		return FILE_XML;
	}
	public void setFILE_XML(String fILE_XML) {
		FILE_XML = fILE_XML;
	}
	public String getFILE_INFO_XML() {
		return FILE_INFO_XML;
	}
	public void setFILE_INFO_XML(String fILE_INFO_XML) {
		FILE_INFO_XML = fILE_INFO_XML;
	}
	public String getPATH() {
		return PATH;
	}
	public void setPATH(String pATH) {
		PATH = pATH;
	}
	public String getSAVE_NM() {
		return SAVE_NM;
	}
	public void setSAVE_NM(String sAVE_NM) {
		SAVE_NM = sAVE_NM;
	}
	public String getPROJECT_ID() {
		return PROJECT_ID;
	}
	public void setPROJECT_ID(String pROJECT_ID) {
		PROJECT_ID = pROJECT_ID;
	}
	public String getPARAM() {
		return PARAM;
	}
	public void setPARAM(String pARAM) {
		PARAM = pARAM;
	}
	public String getUSER() {
		return USER;
	}
	public void setUSER(String uSER) {
		USER = uSER;
	}
	public String getTOOL() {
		return TOOL;
	}
	public void setTOOL(String tOOL) {
		TOOL = tOOL;
	}
	public String getNAME() {
		return NAME;
	}
	public void setNAME(String nAME) {
		NAME = nAME;
	}
	
	public String getCLIENT_IP() {
		return CLIENT_IP;
	}
	public void setCLIENT_IP(String cLIENT_IP) {
		CLIENT_IP = cLIENT_IP;
	}

	public String getITEM_ID() {
		return ITEM_ID;
	}
	public void setITEM_ID(String cITEM_ID) {
		ITEM_ID = cITEM_ID;
	}
	
	public String getIMG_URL() {
		return IMG_URL;
	}
	public void setIMG_URL(String cIMG_URL) {
		IMG_URL = cIMG_URL;
	}
	
	
	public String getPDF_EXPORT_TYPE() {
		return PDF_EXPORT_TYPE;
	}
	public void setPDF_EXPORT_TYPE(String pDF_EXPORT_TYPE) {
		PDF_EXPORT_TYPE = pDF_EXPORT_TYPE;
	}
	public String getSTART_PAGE() {
		return START_PAGE;
	}
	public void setSTART_PAGE(String sTART_PAGE) {
		START_PAGE = sTART_PAGE;
	}
	public String getEND_PAGE() {
		return END_PAGE;
	}
	public void setEND_PAGE(String eND_PAGE) {
		END_PAGE = eND_PAGE;
	}
	public String getCIRCULATION() {
		return CIRCULATION;
	}
	public void setCIRCULATION(String cIRCULATION) {
		CIRCULATION = cIRCULATION;
	}
	
	/***
	 * PREVIEW 형태 판단
	 * */
	public boolean getUSE_PREVIEW() {
		return USE_PREVIEW;
	}
	public void setUSE_PREVIEW(boolean uSE_PREVIEW) {
		USE_PREVIEW = uSE_PREVIEW;
	}
	
	
	public String getIS_UBPARAMS_ENCODED() {
		return IS_UBPARAMS_ENCODED;
	}
	public void setIS_UBPARAMS_ENCODED(String iS_UBPARAMS_ENCODED) {
		IS_UBPARAMS_ENCODED = iS_UBPARAMS_ENCODED;
	}
	
	public String getUB_PDF_PRINT_VERSION() {
		return UB_PDF_PRINT_VERSION;
	}
	public void setUB_PDF_PRINT_VERSION(String uB_PDF_PRINT_VERSION) {
		UB_PDF_PRINT_VERSION = uB_PDF_PRINT_VERSION;
	}
	
	/**
	 * 1건 이상의 문서를 하나의 문서로 오픈하기 위한 파라미터 값( json 형태의 문자열 )
	 * @return
	 */
	public String getUB_FORMLIST_INFO() {
		return UB_FORMLIST_INFO;
	}
	public void setUB_FORMLIST_INFO(String uB_FORMLIST_INFO) {
		UB_FORMLIST_INFO = uB_FORMLIST_INFO;
	}
	
	public String getHTML_OPTION() {
		return HTML_OPTION;
	}
	public void setHTML_OPTION(String hTml_OPTION) {
		HTML_OPTION = hTml_OPTION;
	}
	
	
	public String getUB_EXPORT_FILE_PATH() {
		return UB_EXPORT_FILE_PATH;
	}
	public void setUB_EXPORT_FILE_PATH(String uB_EXPORT_FILE_PATH) {
		UB_EXPORT_FILE_PATH = uB_EXPORT_FILE_PATH;
	}
	public String getUB_EXPORT_FILE_NAME() {
		return UB_EXPORT_FILE_NAME;
	}
	public void setUB_EXPORT_FILE_NAME(String uB_EXPORT_FILE_NAME) {
		UB_EXPORT_FILE_NAME = uB_EXPORT_FILE_NAME;
	}
	
	public String getEDITOR_FONT_LOCALE() {
		return EDITOR_FONT_LOCALE;
	}
	public void setEDITOR_FONT_LOCALE(String fONT_LOCALE) {
		EDITOR_FONT_LOCALE = fONT_LOCALE;
	}
	
	public String getEXCEL_SHEET_SPLIT_TYPE() {
		return EXCEL_SHEET_SPLIT_TYPE;
	}
	public void setEXCEL_SHEET_SPLIT_TYPE(String eXCEL_SHEET_SPLIT_TYPE) {
		EXCEL_SHEET_SPLIT_TYPE = eXCEL_SHEET_SPLIT_TYPE;
	}

	public String getEXCEL_SHEET_NAMES() {
		return EXCEL_SHEET_NAMES;
	}
	public void setEXCEL_SHEET_NAMES(String eXCEL_SHEET_NAMES) {
		EXCEL_SHEET_NAMES = eXCEL_SHEET_NAMES;
	}
	
	public String getPDF_PASSWORD() {
		return PDF_PASSWORD;
	}
	public void setPDF_PASSWORD(String pDF_PASSWORD) {
		PDF_PASSWORD = pDF_PASSWORD;
	}	

	public String getUB_OPEN_PARAM_POPUP() {
		return UB_OPEN_PARAM_POPUP;
	}
	public void setUB_OPEN_PARAM_POPUP(String uB_OPEN_PARAM_POPUP) {
		UB_OPEN_PARAM_POPUP = uB_OPEN_PARAM_POPUP;
	}
	public String getPRINT_LANDSCAPE() {
		return PRINT_LANDSCAPE;
	}
	public void setPRINT_LANDSCAPE(String pRINT_LANDSCAPE) {
		PRINT_LANDSCAPE = pRINT_LANDSCAPE;
	}
	
	private float PAGE_MARGIN_TOP = 0;
	private float PAGE_MARGIN_LEFT = 0;
	private float PAGE_MARGIN_RIGHT = 0;
	private float PAGE_MARGIN_BOTTOM = 0;

	private String LOGIN_ID;			// UBForm viewer 접속 로그인 아이디

	public float getPAGE_MARGIN_TOP() {
		return PAGE_MARGIN_TOP;
	}
	public void setPAGE_MARGIN_TOP(float pAGE_MARGIN_TOP) {
		PAGE_MARGIN_TOP = pAGE_MARGIN_TOP;
	}
	public float getPAGE_MARGIN_LEFT() {
		return PAGE_MARGIN_LEFT;
	}
	public void setPAGE_MARGIN_LEFT(float pAGE_MARGIN_LEFT) {
		PAGE_MARGIN_LEFT = pAGE_MARGIN_LEFT;
	}
	public float getPAGE_MARGIN_RIGHT() {
		return PAGE_MARGIN_RIGHT;
	}
	public void setPAGE_MARGIN_RIGHT(float pAGE_MARGIN_RIGHT) {
		PAGE_MARGIN_RIGHT = pAGE_MARGIN_RIGHT;
	}
	public float getPAGE_MARGIN_BOTTOM() {
		return PAGE_MARGIN_BOTTOM;
	}
	public void setPAGE_MARGIN_BOTTOM(float pAGE_MARGIN_BOTTOM) {
		PAGE_MARGIN_BOTTOM = pAGE_MARGIN_BOTTOM;
	}
	
	
	public String getLOGIN_ID() {
		return LOGIN_ID;
	}
	public void setLOGIN_ID(String lOGIN_ID) {
		LOGIN_ID = lOGIN_ID;
	}
	
	private String EXTERNAL_PROJECT_CODE = "";	// 프로젝트와 데이터셋의 경로가 외부설정되잇을때 찾아갈수 있는 키값

	public String getEXTERNAL_PROJECT_CODE() {
		return EXTERNAL_PROJECT_CODE;
	}
	public void setEXTERNAL_PROJECT_CODE(String eXTERNAL_PROJECT_CODE) {
		EXTERNAL_PROJECT_CODE = eXTERNAL_PROJECT_CODE;
	}
		
}
