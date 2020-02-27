package org.ubstorm.server.websocket;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.PropertyConfigurator;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubstorm.server.commnuication.UDPClient;
import org.ubstorm.server.commnuication.UDPServer;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.queue.IQueue;
import org.ubstorm.service.parser.queue.JobDataSetQueue;
import org.ubstorm.service.utils.WinRegistry;
import org.ubstorm.service.utils.common;

import com.lowagie.text.FontFactory;

public class UBWebSocketServer extends WebSocketServer implements Serializable {
	private static final long serialVersionUID = -1L;
	private static final Logger log = LoggerFactory.getLogger(UBWebSocketServer.class);
	
	//private InetSocketAddress gConSocketAddress = null;
	private StringBuffer gParamsData = null;
	private Properties properties = null;
	
	/*
	 * 72(dpi) / 25 (mm/inch) = 0.352 mm/px = (default value of transcoder) 
	 */ 
	private static final double ppmm = 72 / 25.4; 
	
	private static UBWebSocketServer mServer = null;
	
	private static UDPClient mUdpClient = null;
		
	//큐를 생성한다.
	private static IQueue jobQueue = null;
	
	private Thread conThr = null;
	
	public UBWebSocketServer(InetSocketAddress address) {
		super(address);
		
		log.info("Constructor~~~~");
		
		if(jobQueue == null)
			jobQueue = new JobDataSetQueue();
		
		if(conThr == null)
		{
			conThr = new Thread(new JobSokcetConsumer(jobQueue, "0"));
			conThr.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					// TODO Auto-generated method stub
					System.out.println(t.getName() + " throws exception: " + e);
					jobQueue.clear();
				}
			});		
		
			// 데몸 스레드로 설정한다.
			//if(!conThr.isDaemon()) conThr.setDaemon(true);
			conThr.start();		
		}
		
		String _path = Log.basePath + "fonts";
	
		log.info("Resource fonts url=" + _path);
		
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	    Log.g2d = img.createGraphics();
	    
	   	try {
	   		loadFontList(_path);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed to loadFontList::" + e.getLocalizedMessage());
		}
	   	
//	   	try {
//			if(!isRunning()){
//				Runtime rt = Runtime.getRuntime();
//				String exeFile = Log.basePath + "UBLocalExporterState.exe";
//				log.info("UBLocalExporterState.exe starting... : " + exeFile);
//				Process p;
//				             
//				try {
//				    p = rt.exec(exeFile);
//				    //p.waitFor();
//				} catch (Exception e) {
//				    //e.printStackTrace();
//				    log.error("UBLocalExporterState.exe start fail. " + e.getMessage());
//				}
//			}
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	    
	   	String cfilePath = Log.basePath + "data/params.txt";
	   	
	   	//디렉토리 생성
		File _directory = new File((new File( cfilePath )).getParent());
		if(!_directory.exists()){
			_directory.mkdirs(); 
		}
	
	}
	
	private boolean isRunning() throws Exception
    {
        Process listTasksProcess = Runtime.getRuntime().exec("tasklist");
        BufferedReader tasksListReader = new BufferedReader(
                new InputStreamReader(listTasksProcess.getInputStream()));

        String tasksLine;

        while ((tasksLine = tasksListReader.readLine()) != null)
        {
            if (tasksLine.contains("UBLocalExporterState.exe"))
            {
                return true;
            }
        }

        return false;
    }

	private void loadFontList(String _path) throws FontFormatException, IOException
    {
		File dirFile=new File(_path);
		
		File []fileList=dirFile.listFiles();
		
		String _fontLocale = "en,ko";
		String[] _fontLocaleList = (_fontLocale != null )?_fontLocale.split(","):null;
		
		// 실제 폰트명을 담아둘 객체 
		GlobalVariableData.M_FONTNAME_LIST = new ArrayList<String>();
		// local별로 fontFamily를 담아둘 객체 
		GlobalVariableData.M_FONTFAMILY_LIST = new HashMap<String, ArrayList<String>>();
	    String _fontFamilyName = "";
		
		for(File tempFile : fileList) {
		  if(tempFile.isFile()) {
		    String tempPath=tempFile.getParent();
		    String tempFileName=tempFile.getName();
		    
		    
		    InputStream in = new FileInputStream(tempFile);
		    Font _font = Font.createFont(Font.TRUETYPE_FONT, in);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(_font);
		    
		    // local값이 없을경우 
		    {
		    	
			    GlobalVariableData.M_FONTNAME_LIST.add(tempFileName);

			    ArrayList<String> _localeAr;
			    
			    if(_fontLocaleList != null && _fontLocaleList.length > 0 )
			    {
			    	for(String _locale : _fontLocaleList) {
			    		
			    		if( GlobalVariableData.M_FONTFAMILY_LIST.containsKey(_locale) == false )
			    		{
			    			_localeAr = new ArrayList<String>();
			    			GlobalVariableData.M_FONTFAMILY_LIST.put(_locale,_localeAr );
			    		}
			    		else
			    		{
			    			_localeAr = GlobalVariableData.M_FONTFAMILY_LIST.get(_locale);
			    		}
			    		
			    		
			    		Locale lc=stringToLocale(_locale);
			    		//Locale lc=LocaleUtils.toLocale(_locale);
			    		//Locale lc=Locale.forLanguageTag(_locale);
			    		
			    		_fontFamilyName = _font.getFamily( lc  );
			    		
			    		if( _localeAr.indexOf(_fontFamilyName) == -1 ) _localeAr.add( _fontFamilyName );
			    		
			    	}
			    }
		    }
		  }
		}
		
		//if(_loc.equals(""))
		{
			// 신촌 세브란스 병원의 경우 Flash 버전을 사용. itext.jar파일이 존재 하지 않아 기동시 오류가 발생함 class가 없을경우 처리 하지 않도록 지정해둠. 2016-05-24 
			try {
				Class.forName("com.lowagie.text.FontFactory"); 
				FontFactory.registerDirectory(_path);
			} catch (Exception e) {
				log.debug("com.lowagie.text.FontFactory not USED");
			} 
		}

    }
	
	public static String decodeHtmlSpecialChars( String src )
	{
//		pvalue = pvalue.replaceAll("&", "&#38;");
//		pvalue = pvalue.replaceAll("<", "&lt;").replaceAll(">", "&gt;");	// 보안취약점패치 (XSS)
//		pvalue = pvalue.replaceAll("\\.\\./", "").replaceAll("\\./", "").replaceAll("\\\\", "");
//		pvalue = pvalue.replaceAll("\\\"", "&#039;").replaceAll("'", "&quot;").replaceAll("/", "&#047;");
		
		return src.replaceAll("&#38;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&#039;", "\\\"").replaceAll("&quot;", "'").replaceAll("&#047;", "/");
	}
	
	 private Locale stringToLocale(String s) {
        StringTokenizer tempStringTokenizer = new StringTokenizer(s,",");
        String l = "";
        String c = "";
        
        if(tempStringTokenizer.hasMoreTokens()){
        	l = (String) tempStringTokenizer.nextElement();	
        }
        
        if(tempStringTokenizer.hasMoreTokens()){
        	c = (String) tempStringTokenizer.nextElement();	
        }
        
        return new Locale(l,c);
	}
	    

	public void setLogBasePath(String basPath)
	{
		Log.basePath = basPath;
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		// TODO Auto-generated method stub
		InetSocketAddress _conSocketAddress = conn.getRemoteSocketAddress();
		log.info("onOpen() New connection to " + _conSocketAddress);
		
		//LocalState 미리보기화면이 떠 있으면 닫도록 메시지를 날린다.		
		jobQueue.clear();
	}
	
	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		// TODO Auto-generated method stub
		log.info("onClose() Closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
		
		// LocalState 미리보기화면이 떠 있으면 닫도록 메시지를 날린다.
		//Log.previewClose = true;
		//jobQueue.clear();
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		// TODO Auto-generated method stub
		log.info("onMessage() Received message from "	+ conn.getRemoteSocketAddress() + ": " + message.length());
		
		if(jobQueue.size() > 0)
		{
			InetSocketAddress _conSocketAddress = conn.getRemoteSocketAddress();
			log.error("onMessage()"	+ _conSocketAddress + ": Other job is already processed. jobQueue is busy.");
			this.SendMessage(conn, JSONConverter("FAIL", "OPEN", Log.MSG_LP_JOB_ALEADY_ALLOCATED, Log.getMessage(Log.MSG_LP_JOB_ALEADY_ALLOCATED)));
			return;
		}		
		
		if(Log.gConSocketAcceptReady==false)
		{
			InetSocketAddress _conSocketAddress = conn.getRemoteSocketAddress();
			log.error("onMessage()"	+ _conSocketAddress + ": Other job is already processed. ConSocketAcceptReady is not true.");
			this.SendMessage(conn, JSONConverter("FAIL", "OPEN", Log.MSG_LP_JOB_ALEADY_ALLOCATED, Log.getMessage(Log.MSG_LP_JOB_ALEADY_ALLOCATED)));
			return;
		}
		
		
		Object body = new Object();				
	   	Object ubObj;
	   	JSONObject ubobjParam;
	   	String _reqCmdName = "";
	   	
		try 
		{
			ubObj = JSONValue.parseWithException((String)message);
			ubobjParam = (JSONObject)ubObj;
			
			_reqCmdName = (String) ubobjParam.get("COMMAND");
			
			log.debug("onMessage() COMMAND=" + _reqCmdName);
			if(_reqCmdName != null && _reqCmdName.equals("SENDPARAMS"))
			{
				// UBPARAMS
				long szUBParamsArrayLength =  Long.valueOf(ubobjParam.get("UBPARAMS_ARRAY_LENGTH").toString());		
				long szUBParamsArrayIndex = Long.valueOf(ubobjParam.get("UBPARAMS_ARRAY_INDEX").toString());				
				String szUBParamsArrayData = (String)ubobjParam.get("UBPARAMS_ARRAY_DATA");		

				if(szUBParamsArrayIndex==0)
				{
					gParamsData = new StringBuffer();
				}
				// 파라미터를 넘기지 않는 경우, 여러번 호출시 계속 null이 파라미터에 붙어서 오류가 발생함
				if(szUBParamsArrayData != null)
				{
					gParamsData.append(szUBParamsArrayData);
				}
				
				if(szUBParamsArrayIndex >= szUBParamsArrayLength-1)
					conn.send(JSONConverter("SUCCESS", "SENDPARAMSEND", Log.MSG_LP_SEND_PARAMS_END, Log.getMessage(Log.MSG_LP_SEND_PARAMS_END)));
				else
					conn.send(JSONConverter("SUCCESS", "SENDPARAMS", Log.MSG_LP_SEND_PARAMS_CONTINUE, Log.getMessage(Log.MSG_LP_SEND_PARAMS_CONTINUE)));
			}
			else
			{
				HashMap<String, Object> param = new HashMap();
				param.put("UdpClient", mUdpClient);
				param.put("conn", conn);
				param.put("message", message);		
				param.put("paramData", gParamsData);		
				
				jobQueue.put(param);	
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			log.error("onMessage() " + Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + e.getMessage());
			
			conn.send(JSONConverter("FAIL", _reqCmdName, Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + e.getMessage()));
		} 
		catch (OutOfMemoryError exp) {
			exp.printStackTrace();
			log.error(getClass().getName() + "::" + " OutOfMemoryError !!!");
			
			conn.send(JSONConverter("FAIL", _reqCmdName, Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + " OutOfMemoryError !!!"));
		}
	}
	
	@Override
	public void onMessage( WebSocket conn, ByteBuffer message ) {
		
		Object body = new Object();		
		byte[] buffer = message.array();
		
		log.debug("onMessage() Received ByteBuffer from "	+ conn.getRemoteSocketAddress() + ": mesage.length=" + buffer.length + " bytes");
		
		if(buffer != null && buffer.length > 0)
    	{
			Object ubObj;
    	   	JSONObject ubobjParam;
			try {
				String stMessage = new String(buffer, "UTF-8");
				int lastCloseIndex = stMessage.lastIndexOf("}");
				stMessage = stMessage.substring(0, lastCloseIndex+1);
				
				message.clear();
				
				log.debug("onMessage() stMessage.length=" + stMessage.length());
				
				if(stMessage instanceof String) {
	
					ubObj = JSONValue.parseWithException(stMessage);
					ubobjParam = (JSONObject)ubObj;
					
					String reqCmdMethodName = (String) ubobjParam.get("METHOD_NAME");

					log.debug("onMessage() METHOD_NAME=" + reqCmdMethodName);
				}		
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("onMessage() ParseException::" + e.getMessage());
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				log.error("onMessage() UnsupportedEncodingException::" + e1.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("onMessage() IOException::" + e.getMessage());
			}
			
		}

		byte[] resultBody = ((String) body).getBytes();
		
		log.info("onMessage() Send resultBody.");
		conn.send(resultBody);
	}
	
	private void SendMessage(WebSocket conn, String messageData)
	{
		mUdpClient.sendMessage(messageData);
		conn.send(messageData);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		// TODO Auto-generated method stub
		if(conn != null)
		{
			log.error("onError() An error occured on connection " + conn.getRemoteSocketAddress()  + ":" + ex.getMessage());
		}
		else
		{
			log.error("onError() " + ex.getMessage());
		}
	}
	
	public String JSONConverter(String type, String command, String code, String message) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("port", Log.wsPort);
		jsonObject.put("type", type);
		jsonObject.put("command", command);
		jsonObject.put("code", code);
		jsonObject.put("message", message);
		return jsonObject.toString();
	}
	
	public JSONObject JSONMessageConverter(String type, String command, String code, String message) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("port", Log.wsPort);
		jsonObject.put("type", type);
		jsonObject.put("command", command);
		jsonObject.put("code", code);
		jsonObject.put("message", message);
		return jsonObject;
	}
	
	public static void main(String[] args) {
		String host = "127.0.0.1";
		int port = 37000;		
		
		/*
		if(args.length == 2)
		{
			host = args[0];
			port = Integer.valueOf(args[0]);
		}
		*/
		
		boolean is64bit = false;
		String appBasePath;
		try {
			if (System.getProperty("os.name").contains("Windows")) {
			    is64bit = (System.getenv("ProgramFiles(x86)") != null);
			} else {
			    is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
			}
			
			/*
			appBasePath = WinRegistry.readString (
				    WinRegistry.HKEY_CURRENT_USER,                             		//HKEY
				   "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run",           	//Key
				   "LocalPDFServer");
			*/
			appBasePath = WinRegistry.readString (
				    WinRegistry.HKEY_CURRENT_USER,                             		//HKEY
				   "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run",           	//Key
				   "UBLocalExporter");
			
			appBasePath = appBasePath != null ? appBasePath.substring(1, appBasePath.lastIndexOf("\\")) : "";
			
			//System.out.println(Runtime.getRuntime().maxMemory());
			System.out.println("UBLocalExporter Install Folder=[" + appBasePath + "], OS Platform is64bit =[" + is64bit + "]"); 
			
			Log.is64bit = is64bit;
						
			Log.basePath = appBasePath + "/";			
		 	Properties ubiProps = new Properties();
		   	try {
		   		PropertyConfigurator.configure(Log.basePath + "config/" +  "log4j.properties");
		   		
		   		ubiProps.load(new FileInputStream(Log.basePath + "config/" +  "ubiform.properties"));
		   		
		   		Log.ubiformProps = ubiProps;
				
			   	String _wsocketPort = args.length == 1 ? args[0] : common.getPropertyValue("WebSocket.Port");		   	
			   	if(_wsocketPort != null)
				{
			   		Log.wsPort = _wsocketPort;
					port = Integer.valueOf(_wsocketPort);
				}
			    
				String _formFileGetFromServer = common.getPropertyValue("Form.GetFileFromServer");		   	
				if(_formFileGetFromServer != null)
				{
					Log.formFileGetFromServer = "false".equals(_formFileGetFromServer) ? false : true;
				}
				
				String _pdfExportDivCount = common.getPropertyValue("Form.PdfExportDivCount");		   	
				if(_pdfExportDivCount != null)
				{
					Log.pdfExportDivCount = Integer.valueOf(_pdfExportDivCount);
				}

				String _udpServerPort = common.getPropertyValue("TrayServer.Port");		
				if(_udpServerPort != null)
				{
					mUdpClient = new UDPClient(Integer.valueOf(_udpServerPort));
				}
				else
				{
					mUdpClient = new UDPClient();
				}
							
				if(common.getPropertyValue("Preview.PageBuffer") != null){
					Log.prePageBuff = Integer.parseInt( common.getPropertyValue("Preview.PageBuffer"));
				}
				
				
				String _messageLang = common.getPropertyValue("Form.MessageLang");	
				_messageLang = _messageLang == null ? "ko" : _messageLang; 
				
			 	Properties props = new Properties();
				try {
				   	//classpath상 파일에 InputStream을 가져온다
			 		InputStream is = new FileInputStream(Log.basePath + "message/" + _messageLang + ".xml");
			 	    //파일 InputStream을 Properties 객체로 읽어온다
			    	props.loadFromXML(is);
				} catch (IOException e) {
					e.printStackTrace();
				}
			    Log.props = props;		
				
				BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			    Log.g2d = img.createGraphics();				
			    
			    log.debug("UBIFORM_LocalExportServer Start with IP=" + host + ", port=" + port + ", FormFileGetFromServer=" + Log.formFileGetFromServer + ", pdfExportDivCount=" + Log.pdfExportDivCount + ", MessageLang=" + _messageLang); 
			    
			    mServer = new UBWebSocketServer(new InetSocketAddress(host, port));
			    
			  //Thread udpThr = new Thread(new UDPServer(port+1));
				Thread udpThr = new Thread( UDPServer.getInstance(port+1));
				udpThr.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						// TODO Auto-generated method stub
						System.out.println(t.getName() + " throws exception: " + e);
					}
				});
				
				// 데몸 스레드로 설정한다.
				if(!udpThr.isDaemon()) udpThr.setDaemon(true);
				udpThr.start();
				
				mServer.run();
										   	
		   	} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("main() FileNotFoundException::" + e.getMessage() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("main() IOException::" + e.getMessage() );
			}
		   
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	
}
