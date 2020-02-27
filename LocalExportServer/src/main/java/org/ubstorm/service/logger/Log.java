
package org.ubstorm.service.logger;

import java.awt.Graphics2D;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Properties;

import org.json.simple.JSONObject;

public abstract class Log {

	public static final String SERVER_JAR_VER = "3.0.0-20200211-01";
	
	public abstract boolean log(String path, int logLevel, String jspName, StringBuffer sb);
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss.SS");

    // DEFINE MESAGE TYPE
	public static final String MSG_ERROR = "1";
	public static final String MSG_INFO = "2";
	public static final String MSG_SUCC = "3";

	
    // DEFINE MESAGE CODE
	public static final String MSG_COMMON_SUCCESS = "1000";
	public static final String MSG_COMMON_FAILED = "1001";
	
	public static final String MSG_DATABASE_CONNECT_SUCCESS = "2001";
	
	public static final String MSG_SYSTEM_EXCEPTION = "9000";
	public static final String MSG_DATABASE_EXCEPTION = "9001";
	public static final String MSG_LOGIN_PASSWD_FAIL = "9002";
	public static final String MSG_LOGIN_USERID_FAIL = "9003";
	public static final String MSG_SYSTEM_METHOD_NOTFOUND = "9004";
	public static final String MSG_SYSTEM_METHOD_INVOKE_FAIL = "9005";
	public static final String MSG_DATABASE_CONNECTION_NULL = "9006";
	public static final String MSG_SYSTEM_METHOD_UNIMPLEMENTED = "9007";
	public static final String MSG_FAIL_TO_FIND_PUBLISH_SERVER_LIST = "9008";
	public static final String MSG_REQUET_URL_IS_EMPTY = "9009";
	public static final String MSG_FAILED_TO_GET_XMLDATA = "9010";
	public static final String MSG_NOT_EXISTS_FORM_INFO_FILE = "9011";
	public static final String MSG_NOT_EXISTS_PATH = "9012";
	public static final String MSG_NOT_EXISTS_TEMPLATE_FILE = "9013";
	public static final String MSG_NOT_EXISTS_VIEWLOG_FILE = "9014";
	public static final String MSG_LICENSE_SERVER_IP_INVALID = "9015";
	public static final String MSG_LOGIN_FAILED = "9016";
	public static final String MSG_CHECK_YOUR_DB_CONNECTION = "9017";
	public static final String MSG_FAILED_TO_MAKE_FOLDER = "9018";
	public static final String MSG_ALREADY_FILE_EXISTS = "9019";
	public static final String MSG_WRONG_APPROACH = "9020";
	public static final String MSG_FAILED_TO_UPLOAD = "9021";
	public static final String MSG_NOT_EXISTS_DATASET_FILE = "9022";
	public static final String MSG_NOT_EXISTS_ERD_FILE = "9023";
	public static final String MSG_NOT_EXISTS_SQL_FILE = "9024";
	public static final String MSG_NOT_EXISTS_TNS_FILE = "9025";
	public static final String MSG_FAILED_TO_DELETE_FILE = "9026";
	public static final String MSG_IS_NOT_SELECT_QUERY = "9027";
	public static final String MSG_UNDEFINED_DBMS_TYPE = "9028";
	public static final String MSG_DATABASE_NO_DATA_FOUND = "9029";
	public static final String MSG_DATABASE_UNDEFINED_COMMAND = "9030";
	public static final String MSG_DATABASE_UNSUPPORTED_SYNTAX = "9031";
	public static final String MSG_CLASS_NOT_FOUND_EXCEPTION = "9032";
	public static final String MSG_FAIL_TO_FIND_PROJECT_HISTORY_LIST = "9033";
	public static final String MSG_EXCUTION_SERVICE_FAILED = "9034";
	public static final String MSG_PARAMETER_NOT_DEFINED = "9035";
	public static final String MSG_METHOD_NOT_FOUND = "9036";
	public static final String MSG_CONTROLLER_NOT_FOUND = "9037";
	public static final String MSG_NOT_EXISTS_DEVICE_FILE = "9038";
	public static final String MSG_NOT_EXISTS_LICENSE_FILE = "9039";
	public static final String MSG_DOC_GEN_EXCEPTION = "9040";
	public static final String MSG_WRONG_PASSWORD = "9041";
	public static final String MSG_PARAMETERS_NOT_EXIT = "9042";
	public static final String MSG_LICENSE_SERVER_CPU_COUNT_INVALID = "9043";
	public static final String MSG_LICENSE_SERVER_USER_COUNT_INVALID = "9044";
	public static final String MSG_LICENSE_SERVER_DATE_INVALID = "9045";
	public static final String MSG_INVALIDATE_CLIENT_CONNECTION = "9046";
	public static final String MSG_LICENSE_SERVER_INSTANCE_INVALID = "9047";
	public static final String MSG_LICENSE_SERVER_ACCOUNT_INVALID = "9048";
	public static final String MSG_SERVER_SESSION_INVALID = "9049";
	public static final String MSG_SESSION_INVALID_COMMAND_GUIDE = "9050";

	public static final String MSG_INVALID_FILE_PATH = "9051";
	public static final String MSG_FUNCTION_ERROR = "9052";

	public static final String MSG_LICENSE_KEY_ERROR = "9053";
	public static final String MSG_USE_EDIT_METHOD = "9054";
	
	/******************************************************
	 * LocalExpoprter Messages
	 ******************************************************/
	public static final String MSG_LP_SEND_PARAMS_END = "10001";
	public static final String MSG_LP_SEND_PARAMS_CONTINUE = "10002";
	public static final String MSG_LP_SUUCESS_GET_FORMFILE_CONTENT = "10003";
	public static final String MSG_LP_FAIL_GET_FORMFILE_CONTENT = "10004";
	public static final String MSG_LP_FAIL_GET_SERVER_TOKEN = "10005";
	public static final String MSG_LP_NOTFOUND_PRINTSERVICE = "10006";
	public static final String MSG_LP_PRINTJOB_CANCEL = "10007";
	public static final String MSG_LP_PRINTJOB_COMPLETED = "10008";
	public static final String MSG_LP_MAKEPDF_COMPLETED = "10009";
	public static final String MSG_LP_MAKEPDF_CANCELED = "10010";	
	public static final String MSG_LP_START_MAKEPRINT = "10011";
	public static final String MSG_LP_END_MAKEPRINT = "10012";	
	public static final String MSG_LP_START_MAKEPDF = "10013";
	public static final String MSG_LP_END_MAKEPDF = "10014";
	public static final String MSG_LP_START_PRINT = "10015";
	public static final String MSG_LP_END_MERGEPDF = "10016";
	public static final String MSG_LP_JOB_ALEADY_ALLOCATED = "10017";
	public static final String MSG_LP_PRINT_PREVIEW = "10018";
	public static final String MSG_LP_PREVIEWJOB_COMPLETE = "10019";
	public static final String MSG_LP_END_CREATE_EXCEL = "10020";
	public static final String MSG_LP_MAKEEXCEL_COMPLETED = "10021";
	public static final String MSG_LP_MAKETIFF_COMPLETED = "10022";
	public static final String MSG_LP_MAKETIFF_START = "10023";
	public static final String MSG_LP_LOCALEXPORTER_REQUIRED_UPDATE = "10024";
	public static final String MSG_LP_GET_UPDATER_URL = "10025";
	public static final String MSG_LP_FAIL_PROJECT_TYPE = "10026";
	public static final String MSG_LP_MAKETIFF_CANCELED = "10027";
	public static final String MSG_LP_PRINTJOB_GET_PAGECOUNT_END = "10028";
	public static final String MSG_LP_MAKEEXCEL_CANCELED = "10029";
	
    public static String basePath;
    public static Properties props = null;
    public static String logdbpath;
    public static String mapperBasePath;
    public static String serverURL;
    public static String dataSetURL;
    public static String rootURL;
    public static String serverPort;
    public static String serverContext;
    public static String exportPath;
    public static String pdfExportPrinterType="";
    public static String clientLicenseType="";
    
    public static Properties ubiformProps = null;
    
    public static Connection LogDbConnection = null;    
    public static Graphics2D g2d = null;
    
    public static Object scheduler = null;
    
    public static String pageFontUnit = "px";    
    public static String ufilePath;
    public static String debugLevel;
    
    public static String wsPort = "37000";
    public static boolean formFileGetFromServer = true;
    public static int pdfExportDivCount = 100;
    
    public static boolean printStop = false;
    public static boolean printCompleted = false;
    public static boolean printCancel = false;
    public static int currPageIdx = 0;
    public static boolean previewFlag = false;
    public static boolean execPrintFlag = false;    
    public static String execPrintPage = "";
    public static boolean previewPrintCompleated = false;
    public static boolean previewPrintStop = false;
    public static boolean pdfDownFlag = false;
    public static boolean previewClose = false;
    public static boolean excelDownFlag = false;
    public static String excelDownOption = "";    
    
    public static int prePageBuff = 5;
    
    public static boolean is64bit = false;
    
    public static InetSocketAddress gConSocketAddress = null;
    
    public static boolean gConSocketAcceptReady = true; 
    
    public static String PDF_EXPORT_TYPE = "";
    
    public static String ubiformUserProps = null;
    
    public static JSONObject ubiformUserPropJson = null;
    
    public static HashMap<String, String> externalPathInfo = new HashMap<String, String>();
    
    public static String getMessage(String key)
    {
    	String stMsg = "";
    	
    	if(props != null)
    	{
    		//try {
				//stMsg = new String(props.getProperty(key).getBytes("8859_1"), "UTF-8");
				stMsg = (String) props.get(key);
				stMsg = stMsg == null ? key : stMsg;
			//} catch (UnsupportedEncodingException e) {
			//	e.printStackTrace();
			//}    		
    	}

    	return stMsg;
    }
     
}
