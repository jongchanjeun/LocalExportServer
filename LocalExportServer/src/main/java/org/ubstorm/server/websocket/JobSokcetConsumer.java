package org.ubstorm.server.websocket;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PageRanges;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.java_websocket.WebSocket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ubstorm.server.commnuication.UDPClient;
import org.ubstorm.server.printable.FORMFile;
import org.ubstorm.server.printable.PrintableImpl;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.ItemPropertyProcess;
import org.ubstorm.service.parser.XmlToUBFormSimple;
import org.ubstorm.service.parser.xmlLoadParser;
import org.ubstorm.service.parser.xmlToUbForm;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.queue.IQueue;
import org.ubstorm.service.utils.Base64Coder;
import org.ubstorm.service.utils.WinRegistry;
import org.ubstorm.service.utils.common;
import org.ubstorm.service.utils.crypto.AesUtil5;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import com.sun.media.jai.codec.TIFFEncodeParam;

import cz.vutbr.web.css.CSSProperty.Height;

public class JobSokcetConsumer implements Runnable {
	private Logger log = Logger.getLogger(getClass());
	
	private IQueue queue = null;
	private String threadName = null;
	
	private FORMFile formfile = null;
	
	private Properties properties = null;
	
	private String gClientSessionId = "";
	private String gServerUrl = "";
	
	private CloseableHttpClient gHttpclient = null;
	private int gPageWidth = 794;
	private int gPageHeight = 1123;
	private String gXML_DATA = "";
	
	private StringBuffer gParamsData = null;
	private String gParamsDataFilePath = null;
	
	private int gTotalPageCount = 0;
	private int gPdfileCount=0;
	private String gClientLicenseType = "";
	
	private Date _exportStartDate = null;
	private Date _exportEndDate = null;
	
	private JFileChooser gFileChooser = null;
	private xmlToUbForm _xmlUbForm = null;
	
	private xmlToUbForm _tmpXmlUbForm = null;
	
	/*
	 * 72(dpi) / 25 (mm/inch) = 0.352 mm/px = (default value of transcoder) 
	 */ 
	private static final double ppmm = 72 / 25.4; 	
	
	private UDPClient mUdpClient = null;
	private WebSocket mWsConn = null;
	
	public JobSokcetConsumer(IQueue queue, String index)
	{
		this.queue = queue;
		this.threadName = "[Child-" + index + "]";
	}
	
	@Override
	public void run() {
		log.info(getClass().getName() + "::" + this.threadName + " Start!!!...");
		
		try{
			
			while(!Thread.currentThread().isInterrupted()) {
				//(1) 작업큐에서 수행할 작업을 하나 가져온다.
				Log.gConSocketAcceptReady = true;
				HashMap param = (HashMap) queue.pop();
				Log.gConSocketAcceptReady = false;

				loadSocketProcess(param);
			}
		}
		catch(InterruptedException exp)
		{
			// 무시한다.
		}
		catch(NoSuchElementException exp)
		{
			// 무시한다.
		}
		catch (OutOfMemoryError exp) {
			log.error(getClass().getName() + "::" + this.threadName + " OutOfMemoryError !!!");
			try {
				queue.clear();
				this.SendMessage(mWsConn, JSONConverter("CANCEL", "PRINTCANCEL", Log.MSG_LP_PRINTJOB_CANCEL, Log.getMessage(Log.MSG_LP_PRINTJOB_CANCEL) + ", OutOfMemoryError!!!", ""), true);
			} catch (Exception e) {}
        }
		finally {
			log.info(getClass().getName() + "::" + this.threadName + " End...");
		}
	}

	
	private boolean loadSocketProcess(HashMap param) {
		boolean bResult = false;
		
		log.info(getClass().getName() + "::" + this.threadName + " loadSocketProcess!!!...");
		
		mUdpClient = (UDPClient) param.get("UdpClient");
		WebSocket conn = (WebSocket) param.get("conn");
		mWsConn = conn;
		String message = (String) param.get("message");
		gParamsData = (StringBuffer) param.get("paramData");
		
		try
		{
			loadMessageProcess(conn, message);
			bResult = true;
		}
		catch(Exception exp)
		{
			exp.printStackTrace();
			bResult = false;		
		}
		
		return bResult;
	}	
	
	public void loadMessageProcess(WebSocket conn, String message) {
		// TODO Auto-generated method stub
		log.info("loadMessageProcess() Received message from "	+ conn.getRemoteSocketAddress() + ": " + message.length());
		
		Object body = new Object();		
		
		if(message instanceof String)
    	{
			Log.gConSocketAddress = conn.getRemoteSocketAddress();			
			
		   	Object ubObj;
    	   	JSONObject ubobjParam;
		   
    	   	try 
			{
				ubObj = JSONValue.parseWithException((String)message);
				ubobjParam = (JSONObject)ubObj;
				
				String _reqCmdName = (String) ubobjParam.get("COMMAND");
				String _serverUrl = (String) ubobjParam.get("SERVER_URL");
				if(_serverUrl != null)
					gServerUrl = URLDecoder.decode(_serverUrl, "UTF-8");
				
				log.debug("onMessage() COMMAND=" + _reqCmdName);
				Log.serverURL = gServerUrl;
				if(_reqCmdName != null)
				{
					try
					{
						UDMParamSet udmParams = new UDMParamSet();
						udmParams.setUDPClient(mUdpClient);
						
			    		// UBPARAMS
						String szIsEncodedUBParams = (String)ubobjParam.get("IS_UBPARAMS_ENCODED");		
						if(szIsEncodedUBParams != null && szIsEncodedUBParams.equals("YES"))
						{
							AesUtil5 util = new AesUtil5();

							String szDecUBParams = "";
							if(gParamsData != null && gParamsData.length() > 0)
							{
								String _tmpParamData = gParamsData.toString();
								//log.debug("1111=>gParamsData=[" + gParamsData + "]");
								
								szDecUBParams = util.decrypt(_tmpParamData);	
								//szDecUBParams = _tmpParamData;		
							}
							else
								szDecUBParams = util.decrypt((String)ubobjParam.get("UBPARAMS"));		
							
							log.debug("loadMessageProcess() UBParams=[" + URLDecoder.decode(szDecUBParams, "UTF-8") + "]");
							
							udmParams.getREQ_INFO().setUBPARAMS(szDecUBParams);
				 			
				 			udmParams.getREQ_INFO().setIS_UBPARAMS_ENCODED(szIsEncodedUBParams);
						}
						else
						{
							if(gParamsData != null && gParamsData.length() > 0)
							{		
								String _tmpParamData = gParamsData.toString();
								
								log.debug("loadMessageProcess() UBParams=[" + URLDecoder.decode(_tmpParamData, "UTF-8") + "]");
								
								udmParams.getREQ_INFO().setUBPARAMS(_tmpParamData);
							}
							else
								udmParams.getREQ_INFO().setUBPARAMS((String)ubobjParam.get("UBPARAMS"));
						}
						
						if( udmParams.getREQ_INFO().getUBPARAMS() != null ) 
						{
							String _tmpReqParams = URLDecoder.decode(udmParams.getREQ_INFO().getUBPARAMS(), "UTF-8");
							_tmpReqParams = _tmpReqParams.replace("%2B","+").replace("%25", "%");
							
							udmParams.getREQ_INFO().setUBPARAMS( _tmpReqParams);
						}
			    		
			 			udmParams.getREQ_INFO().setIS_TEST((String)ubobjParam.get("IS_TEST"));
			 			
			 			udmParams.getREQ_INFO().setDOMAIN((String)ubobjParam.get("DOMAIN"));
			 			
			 			//udmParams.getREQ_INFO().setFILE_TYPE(req.getParameter("FILE_TYPE"));
			 			String _tmpFileType = (String)ubobjParam.get("FW_TYPE");	// Framework Type
			 			_tmpFileType = (_tmpFileType == null || _tmpFileType.length() == 0) ? (String)ubobjParam.get("FILE_TYPE") : _tmpFileType.toLowerCase();
			 			udmParams.getREQ_INFO().setFILE_TYPE(_tmpFileType);
			 			
			 			udmParams.getREQ_INFO().setFORM_ID((String)ubobjParam.get("FORM_ID"));
			 			udmParams.getREQ_INFO().setCALL((String)ubobjParam.get("CALL"));
			 			//udmParams.getREQ_INFO().setARGS((String)ubobjParam.get("ARGS"));
			 			//udmParams.getREQ_INFO().setMETHOD_NAME((String)ubobjParam.get("METHOD_NAME"));
			 			udmParams.getREQ_INFO().setFOLDER_NM((String)ubobjParam.get("FOLDER_NM"));
			 			udmParams.getREQ_INFO().setADMINID((String)ubobjParam.get("ADMINID"));
			 			udmParams.getREQ_INFO().setADMINPW((String)ubobjParam.get("ADMINPW"));
			 			udmParams.getREQ_INFO().setPROJECT_NAME((String)ubobjParam.get("PROJECT_NAME"));
			 			udmParams.getREQ_INFO().setFILE_NAME((String)ubobjParam.get("FILE_NAME"));
			 			udmParams.getREQ_INFO().setLINK((String)ubobjParam.get("LINK"));
			 			udmParams.getREQ_INFO().setURL((String)ubobjParam.get("URL"));
			 			udmParams.getREQ_INFO().setFILE_CONTENT((String)ubobjParam.get("FILE_CONTENT"));
			 			udmParams.getREQ_INFO().setFILE_XML((String)ubobjParam.get("FILE_XML"));
			 			udmParams.getREQ_INFO().setFILE_INFO_XML((String)ubobjParam.get("FILE_INFO_XML"));
			 			udmParams.getREQ_INFO().setPATH((String)ubobjParam.get("PATH"));
			 			udmParams.getREQ_INFO().setSAVE_NM((String)ubobjParam.get("SAVE_NM"));
			 			udmParams.getREQ_INFO().setPROJECT_ID((String)ubobjParam.get("PROJECT_ID"));
			 			udmParams.getREQ_INFO().setPARAM((String)ubobjParam.get("PARAM"));
			 			//udmParams.getREQ_INFO().setUSER((String)ubobjParam.get("USER"));
			 			udmParams.getREQ_INFO().setTOOL((String)ubobjParam.get("TOOL"));
			 			udmParams.getREQ_INFO().setNAME((String)ubobjParam.get("NAME"));
						udmParams.getREQ_INFO().setCTR_ID((String)ubobjParam.get("CTR_ID"));
			 			udmParams.getREQ_INFO().setSVC_ID((String)ubobjParam.get("SVC_ID"));
			 			udmParams.getREQ_INFO().setDATA_TYPE((String)ubobjParam.get("DATA_TYPE"));
			 			udmParams.getREQ_INFO().setUSE_FIRSTLINE((String)ubobjParam.get("USE_FIRSTLINE"));
						udmParams.getREQ_INFO().setDEVICE_ID((String)ubobjParam.get("DEVICE_ID"));
			 			udmParams.getREQ_INFO().setDEVICE_NAME((String)ubobjParam.get("DEVICE_NAME"));
			 			udmParams.getREQ_INFO().setITEM_ID((String)ubobjParam.get("ITEM_ID"));
			 			udmParams.getREQ_INFO().setIMG_URL((String)ubobjParam.get("IMG_URL"));
			 			//int nPageNum = (String)ubobjParam.get("PAGE_NUM") != null ? Integer.parseInt( (String)ubobjParam.get("PAGE_NUM")) : 0;
			 			Long nPageNum = ubobjParam.get("PAGE_NUM") != null ? ((Long.valueOf(ubobjParam.get("PAGE_NUM").toString()))) : 0;
			 			udmParams.getREQ_INFO().setPAGE_NUM( nPageNum.intValue() );
			 			udmParams.getREQ_INFO().setCSV_DELIMITER((String)ubobjParam.get("CSV_DELIMITER"));
						
						udmParams.getREQ_INFO().setLOAD_TYPE((String)ubobjParam.get("LOAD_TYPE"));		
						udmParams.getREQ_INFO().setCLIENT_EDIT_MODE((String)ubobjParam.get("CLIENT_EDIT_MODE"));			
						udmParams.getREQ_INFO().setCLIENT_IP("");
						udmParams.getREQ_INFO().setIMG_TYPE((String)ubobjParam.get("IMG_TYPE"));
						udmParams.getREQ_INFO().setMODEL_TYPE((String)ubobjParam.get("MODEL_TYPE"));
						udmParams.getREQ_INFO().setSHOW_LABEL((String)ubobjParam.get("SHOW_LABEL"));
						udmParams.getREQ_INFO().setEXCEL_OPTION((String)ubobjParam.get("EXCEL_OPTION"));
						udmParams.getREQ_INFO().setEXCEL_FMT_OPTION((String)ubobjParam.get("EXCEL_FMT_OPTION"));
						udmParams.getREQ_INFO().setHTML_OPTION((String)ubobjParam.get("HTML_OPTION"));
						
						if( ubobjParam.get("OPENER_LOCATION") != null )
						{
							//udmParams.getREQ_INFO().setOsetOPENER_LOCATION( (String) ubobjParam.get("OPENER_LOCATION") );
						}
						
						//preview 체크
						if( ubobjParam.get("USE_PREVIEW") != null && ubobjParam.get("USE_PREVIEW").equals("true"))
						{
							udmParams.getREQ_INFO().setUSE_PREVIEW(true);
						}
						else
						{
							udmParams.getREQ_INFO().setUSE_PREVIEW(false);
						}
						
						if( ubobjParam.get("UB_FORMLIST_INFO") != null )
						{
							udmParams.getREQ_INFO().setUB_FORMLIST_INFO((String)ubobjParam.get("UB_FORMLIST_INFO"));
						}
	
						if( ubobjParam.get("MULTI_FORM_TYPE") != null )
						{
							udmParams.getREQ_INFO().setMULTI_FORM_TYPE( (String)ubobjParam.get("MULTI_FORM_TYPE") );
						}
						
						if( ubobjParam.get("EDITOR_FONT_LOCALE") != null  )
						{
							udmParams.getREQ_INFO().setEDITOR_FONT_LOCALE( (String)ubobjParam.get("EDITOR_FONT_LOCALE") );
						}
							
						// 인쇄 옵션 처리를 위한 값 맵핑
			 			if( ubobjParam.get("PDF_EXPORT_TYPE") != null)
			 			{
			 				udmParams.getREQ_INFO().setPDF_EXPORT_TYPE( (String)ubobjParam.get("PDF_EXPORT_TYPE") );
			 				udmParams.getREQ_INFO().setPDF_EXPORT_PRINTER_NAME( (String)ubobjParam.get("PDF_EXPORT_PRINTER_NAME") );
			 				
			 				if(ubobjParam.containsKey("PDF_EXPORT_PRINTER_TYPE") && ubobjParam.get("PDF_EXPORT_PRINTER_TYPE") != null){
			 					udmParams.getREQ_INFO().setPDF_EXPORT_PRINTER_TYPE( (String)ubobjParam.get("PDF_EXPORT_PRINTER_TYPE") );
			 					Log.pdfExportPrinterType = (String)ubobjParam.get("PDF_EXPORT_PRINTER_TYPE");
			 				}else{
			 					Log.pdfExportPrinterType = "";
			 				}
			 				
			 				if(ubobjParam.containsKey("PDF_SHOW_PRINTER_DIALOG")){
			 					udmParams.getREQ_INFO().setPDF_SHOW_PRINTER_DIALOG( ubobjParam.get("PDF_SHOW_PRINTER_DIALOG").toString() );
			 				}
			 				
			 				udmParams.getREQ_INFO().setPDF_PRINTER_MARGIN( (String)ubobjParam.get("PDF_PRINTER_MARGIN") );	
			 				
			 				udmParams.getREQ_INFO().setPAGE_RANGE_GUBUN( (String)ubobjParam.get("PAGE_RANGE_GUBUN") );
			 				udmParams.getREQ_INFO().setSTART_PAGE( (String)ubobjParam.get("START_PAGE") );
			 				udmParams.getREQ_INFO().setEND_PAGE( (String)ubobjParam.get("END_PAGE") );
			 				udmParams.getREQ_INFO().setCIRCULATION( (String)ubobjParam.get("CIRCULATION") );
			 				udmParams.getREQ_INFO().setCHANGE_DATASET( (String)ubobjParam.get("CHANGE_DATASET") );
			 				udmParams.getREQ_INFO().setCHANGE_ITEM_DATA( (String)ubobjParam.get("CHANGE_ITEM_DATA") );
			 				udmParams.getREQ_INFO().setUSE_TIMESTAMP( "true".equals((String)ubobjParam.get("USE_TIMESTAMP")) );
			 				udmParams.getREQ_INFO().setUB_PDF_PRINT_VERSION((String)ubobjParam.get(GlobalVariableData.UB_PDF_PRINT_VERSION));
			 				udmParams.getREQ_INFO().setPRINT_LANDSCAPE((String)ubobjParam.get("PRINT_LANDSCAPE") );		
			 				udmParams.getREQ_INFO().setPAGE_FIT((String)ubobjParam.get("PAGE_FIT") );	
			 				
			 				//KB카드 LOCAL_PDF의 경우에 PDF_READER_OPEN 속성에 따라 다운로드 만 하거나 다운로드 후 해당 파일을 오픈한다.			 					
			 				udmParams.getREQ_INFO().setPDF_READER_OPEN((String)ubobjParam.get("PDF_READER_OPEN"));	
			 				
			 				if(  ubobjParam.get("UB_EXPORT_FILE_NAME") != null ) udmParams.getREQ_INFO().setUB_EXPORT_FILE_NAME((String)ubobjParam.get("UB_EXPORT_FILE_NAME") );
			 				if(  ubobjParam.get("UB_EXPORT_FILE_PATH") != null ) udmParams.getREQ_INFO().setUB_EXPORT_FILE_PATH((String)ubobjParam.get("UB_EXPORT_FILE_PATH") );
			 			
			 				if(ubobjParam.containsKey("USE_FILE_SPLIT")){
			 					udmParams.getREQ_INFO().setUSE_FILE_SPLIT( ((boolean)ubobjParam.get("USE_FILE_SPLIT")) );
			 				}
			 				
				 		}
			 			else
			 			{
			 				if( ubobjParam.get("LOAD_TYPE") != null && ubobjParam.get("LOAD_TYPE").equals("one") )
			 				{
			 					udmParams.getREQ_INFO().setCHANGE_DATASET( (String)ubobjParam.get("CHANGE_DATASET") );	
			 				}
			 				
			 				udmParams.getREQ_INFO().setPDF_EXPORT_TYPE("");
			 				udmParams.getREQ_INFO().setPAGE_RANGE_GUBUN("");
			 				udmParams.getREQ_INFO().setSTART_PAGE("");
			 				udmParams.getREQ_INFO().setEND_PAGE("");
			 				udmParams.getREQ_INFO().setCIRCULATION("");
			 				udmParams.getREQ_INFO().setUSE_TIMESTAMP(false);
			 				udmParams.getREQ_INFO().setUB_PDF_PRINT_VERSION("");
			 			}
			 			
			 			if (_reqCmdName.equals("OPEN"))
						{
			 				//롯데 BMT 테스트를 위하여 [세션 ID] + 시작 / 종료 시간 로그에 표시 
			 				_exportStartDate = new Date();
			 				String _BmtLogTitle = "============= START PRINT PROCESS :";
			 				log.debug("onMessage() " +_BmtLogTitle +  " [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_exportStartDate) +"]" );
			 				
			 				gPdfileCount = 0;
			 				
			 				udmParams.getREQ_INFO().setMETHOD_NAME("GetViewXmlData");
			 				
			 				//_tmpXmlUbForm = new xmlToUbForm(udmParams);
			 				_tmpXmlUbForm = null;
			 				
			 				JSONObject joMsg = getLicenseText(_tmpXmlUbForm, _reqCmdName, gServerUrl, udmParams);
			 				if(joMsg.get("type").equals("SUCCESS"))
			 				{
								gClientSessionId = udmParams.getREQ_INFO().getCLIENT_SESSION_ID();
								gClientLicenseType = udmParams.getREQ_INFO().getLICENSE_TYPE();
								Log.clientLicenseType = gClientLicenseType;
			 				}
			 				else if(joMsg.get("type").equals("FAIL"))		
			 				{
			 					//Log.gConSocketAddress = null;
			 				}

			 				body = joMsg.toString();
			 				
			 				this.SendMessage(conn, (String) body, true);
						} 
			 			else if(_reqCmdName.equals("MAKEPDF") || _reqCmdName.equals("PRINT"))
			 			{
			 				String _pdfExportType = udmParams.getREQ_INFO().getPDF_EXPORT_TYPE();
			 				udmParams.getREQ_INFO().setCLIENT_SESSION_ID(gClientSessionId);
			 				udmParams.getREQ_INFO().setLICENSE_TYPE(gClientLicenseType);
			 				udmParams.getREQ_INFO().setMETHOD_NAME("GetViewXmlData");
			 				String sShowPrinterDialog = udmParams.getREQ_INFO().getPDF_SHOW_PRINTER_DIALOG();
			 				
			 				String _exportFileName = udmParams.getREQ_INFO().getUB_EXPORT_FILE_NAME();
			 				String _exportFilePath = udmParams.getREQ_INFO().getUB_EXPORT_FILE_PATH();
			 				String _pdfReaderOpen =  udmParams.getREQ_INFO().getPDF_READER_OPEN();
			 							 				
			 				// (1) Local Data file로부터 DataSet에 필요한 데이터를 얻어온다.
			 				// local file에서 data를 읽어서 처리하는 형태는 Form에서 file DataSet으로 설정한 경우에만 가능
			 				// 나머지의 경우에는 서버로 데이터를 요청하여 별도로 내려받는 로직을 구현해야 할듯하다.(Flash Viewer에서 처럼)
			 				_xmlUbForm = _tmpXmlUbForm instanceof XmlToUBFormSimple ? new XmlToUBFormSimple(udmParams) : new xmlToUbForm(udmParams);
			 				
			 				if(_tmpXmlUbForm.getUseMultiFormType()==true)
			 				{
				 				_xmlUbForm.setDocumentParams(_tmpXmlUbForm.getDocumentParams());
				 				_xmlUbForm.setDocumentInfos(_tmpXmlUbForm.getDocumentInfos());
				 				_xmlUbForm.setUseMultiFormType(_tmpXmlUbForm.getUseMultiFormType());
			 				}
			 				
			 				String _resultFileName = "";
			 				String PARAMS = "";
			 				PARAMS = udmParams.getREQ_INFO().getUBPARAMS();
			 				//PARAMS = URLDecoder.decode(PARAMS, "UTF-8");	// 여기서 이미 URLDecoding을 하였으므로, DataSetLoad시에는 반복으로 Decoding을 하지 않는다.
			 				
			 				HashMap<String, Object> _param = new HashMap<String, Object>();
			 				_param.put("PDF_FILE_NAME", udmParams.getREQ_INFO().getFORM_ID());
			 				_param.put("PDF_EXPORT_TYPE", _pdfExportType);
			 				_param.put("PAGE_NUM", 0);
			 				_param.put("PARAMS", PARAMS);
			 				_param.put("LOAD_TYPE", udmParams.getREQ_INFO().getLOAD_TYPE());
			 				_param.put("CLIENT_EDIT_MODE", udmParams.getREQ_INFO().getCLIENT_EDIT_MODE());
			 				_param.put("CHANGE_DATASET", udmParams.getREQ_INFO().getCHANGE_DATASET() );
			 				_param.put("CHANGE_ITEM_DATA", udmParams.getREQ_INFO().getCHANGE_ITEM_DATA() );
			 				_param.put(GlobalVariableData.UB_PDF_PRINT_VERSION, udmParams.getREQ_INFO().getUB_PDF_PRINT_VERSION());
			 				
			 				HashMap<String, String> _prnParam = new HashMap<String, String>();
			 				_prnParam.put("type", "String");
			 				if(sShowPrinterDialog !=null && sShowPrinterDialog.equalsIgnoreCase("true"))
			 				{
			 					_prnParam.put("parameter", "true");
			 				}
			 				else
			 				{
			 					_prnParam.put("parameter", "false");
			 				}
		 					_param.put(GlobalVariableData.UB_PRINT_USE_UI, _prnParam);
			 							 				
			 				//gHttpclient, _sessionID, _serverUrl
			 				_param.put("httpclient", gHttpclient);
			 				_param.put("sessionID", gClientSessionId);
			 				_param.put("serverUrl", gServerUrl);
			 				
			 				String _userHome = System.getProperty("user.home");
		 					String _exportFolder = _userHome + "/Downloads/ubformLocal/" + Log.wsPort + "/";
			 				
			 				try {
			 				
			 					Log.PDF_EXPORT_TYPE = _pdfExportType;

			 					//String _userHome = System.getProperty("user.home");
			 					//String _exportFolder = _userHome + "/Downloads/ubformLocal/" + Log.wsPort + "/";
								
			 					if("LOCAL_PREVIEW".equals(_pdfExportType) || "LOCAL_PRINT#".equals(_pdfExportType) || "LOCAL_PRINT".equals(_pdfExportType) || "LOCAL_GET_PAGECOUNT".equals(_pdfExportType))
								{
									String _filename = _exportFolder + "tmpFormFile" + ".txt";	
									
									formfile = new FORMFile(_filename);
									
									String mangeResult = "";
									//_param.put("PDF_EXPORT_TYPE", "LOCAL_PRINT");
									 Log.currPageIdx = 0;
									if("LOCAL_PREVIEW".equals(_pdfExportType)){
										 mangeResult = _xmlUbForm.formPreviewManager(gXML_DATA , _param, formfile, true);
									}else if( "LOCAL_PRINT#".equals(_pdfExportType)){
										 mangeResult = _xmlUbForm.formPreviewManager(gXML_DATA , _param, formfile, false);
									}else{
										 mangeResult = _xmlUbForm.formExportManager(gXML_DATA , _param, formfile);
									}
									
									_resultFileName = _filename;
									formfile.close();
									if(mangeResult != null && mangeResult.startsWith("CANCEL")){
										_xmlUbForm.clear();
										_xmlUbForm = null;
										
										this.SendMessage(conn, JSONConverter("CANCEL", "PRINTCANCEL", Log.MSG_LP_PRINTJOB_CANCEL, Log.getMessage(Log.MSG_LP_PRINTJOB_CANCEL), _resultFileName), true);
										return;
									}
									
									if("LOCAL_GET_PAGECOUNT".equals(_pdfExportType)){
										_xmlUbForm.clear();
										_xmlUbForm = null;

										int _totPageCount = formfile.getNumPages();
										this.SendMessage(conn, JSONConverter("SUCCESS", "GET_PAGECOUNT_END", Log.MSG_LP_PRINTJOB_GET_PAGECOUNT_END, Log.getMessage(Log.MSG_LP_PRINTJOB_GET_PAGECOUNT_END), _totPageCount + ""), true);
										return;
									}
								}
			 					else if("LOCAL_EXCEL".equals(_pdfExportType))
								{
			 						//_param.put("EXCEL_OPTION", "NORMAL");
			 						//_param.put("EXCEL_FMT_OPTION", "EXCEL2007");
			 						//_param.put("EXCEL_SHEET_SPLIT_TYPE", "" );
			 						//_param.put("EXCEL_SHEET_NAMES", "" );
			 						
			 						_param.put("EXCEL_OPTION", udmParams.getREQ_INFO().getEXCEL_OPTION()==null ? "NORMAL" : udmParams.getREQ_INFO().getEXCEL_OPTION());
			 						_param.put("EXCEL_FMT_OPTION", udmParams.getREQ_INFO().getEXCEL_FMT_OPTION()==null ? "EXCEL2007" : udmParams.getREQ_INFO().getEXCEL_FMT_OPTION());
			 						_param.put("EXCEL_SHEET_SPLIT_TYPE", udmParams.getREQ_INFO().getEXCEL_SHEET_SPLIT_TYPE());
			 						_param.put("EXCEL_SHEET_NAMES", udmParams.getREQ_INFO().getEXCEL_SHEET_NAMES());
			 						
			 						// Call Excel Export Manager
			 						Workbook resultWB = null;
			 									 						
			 						int _randomInt = (int) (Math.random()*1000) + 1;
			 						String fname = udmParams.getREQ_INFO().getPROJECT_NAME() + "_" + udmParams.getREQ_INFO().getFORM_ID() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date()) +"_"+ _randomInt;
			 									 						
			 						String PATH = _exportFolder + "/" + fname + ".xlsx";
			 						resultWB = _xmlUbForm.excelExportManager(gXML_DATA, _param, PATH , null, null);
			 						if( resultWB == null){
			 							_xmlUbForm.clear();
										_xmlUbForm = null;

										this.SendMessage(conn, JSONConverter("FAIL", _reqCmdName, Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + _resultFileName, PATH), true);										
			 							return;
			 						}
			 						
			 						FileOutputStream out = new FileOutputStream(PATH);
			 						resultWB.write(out);
			 						out.close();
			 						
			 						if(resultWB instanceof SXSSFWorkbook)
			 						{
			 							// dispose of temporary files backing this workbook on disk
			 							((SXSSFWorkbook) resultWB).dispose();
			 						}
			 						
			 						_resultFileName = PATH;
			 						
			 						this.SendMessage(conn, JSONConverter("SUCCESS", "CREATEEXCEL_END", Log.MSG_LP_END_CREATE_EXCEL, Log.getMessage(Log.MSG_LP_END_CREATE_EXCEL), _resultFileName), false);	
								}
								else	// LOCAL_PDF or LOCAL_IMAGE_TIF
								{
									_resultFileName = _xmlUbForm.pdfExportManager_pages(gXML_DATA, _param, _exportFolder, Log.pdfExportDivCount);
									
									if(_resultFileName.startsWith("FAIL")){										
										this.SendMessage(conn, JSONConverter("FAIL", _reqCmdName, Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + _resultFileName, ""), true);										
										return;
									}else if(_resultFileName.startsWith("CANCEL")){
										//저장 된 파일이 있다면 삭제											
										this.SendMessage(conn, JSONConverter("CANCEL", "PDFCANCEL", Log.MSG_LP_MAKEPDF_CANCELED, Log.getMessage(Log.MSG_LP_MAKEPDF_CANCELED), ""), true);	
										return;
									}else{
										this.SendMessage(conn, JSONConverter("SUCCESS", "MERGEPDF_END", Log.MSG_LP_END_MERGEPDF, Log.getMessage(Log.MSG_LP_END_MERGEPDF), _resultFileName), false);	
									}
								}
								
								//_xmlUbForm = null;
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();

								_xmlUbForm.clear();
								_xmlUbForm = null;

								this.SendMessage(conn, JSONConverter("FAIL", _reqCmdName, Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + e.getMessage(), ""), true);
								
								return;
							} 			 				 
			 				
 			 				if("LOCAL_PREVIEW".equals(_pdfExportType) || "LOCAL_PRINT#".equals(_pdfExportType))
				            {
			 					boolean _isFirstPrint = true;
			 					HashMap hmPrintService = new HashMap();			 					
			 					
		 						String sPrinterName = udmParams.getREQ_INFO().getPDF_EXPORT_PRINTER_NAME();
								String sPrintCommand = sShowPrinterDialog !=null && sShowPrinterDialog.equalsIgnoreCase("true") ? "DIALOG_PRT" : "DIRECT_PRT";
								String sMargin = udmParams.getREQ_INFO().getPDF_PRINTER_MARGIN();	
								String sLandScape = udmParams.getREQ_INFO().getPRINT_LANDSCAPE();	
								String sPageFit = udmParams.getREQ_INFO().getPAGE_FIT();

								sPrintCommand = _isFirstPrint ? sPrintCommand : "DIRECT_PRT";
								_isFirstPrint = false;
								
					            JSONObject reqParam = new JSONObject();
					            reqParam.put("COMMAND", sPrintCommand);
					            reqParam.put("PRINTER", sPrinterName);
					            reqParam.put("INFO_DIR", formfile.getOutFileDirectory());	
					            reqParam.put("MARGIN",  sMargin);	
					            reqParam.put("LANDSCAPE",  sLandScape);
					            reqParam.put("PAGE_FIT",  sPageFit);
					            reqParam.put("EXPORT_TYPE",  _pdfExportType);	
					            reqParam.put("EXPORT_FILE_NAME",  _exportFileName);	
					            reqParam.put("EXPORT_FILE_PATH",  _exportFilePath);	
					            
					            reqParam.put("PROJECT_NAME",  udmParams.getREQ_INFO().getPROJECT_NAME());	
					            reqParam.put("FORM_ID",  udmParams.getREQ_INFO().getFORM_ID());	
					            
					            //바로 인쇄 팝업 시 필요한 전체 페이지 정보 추가
					            HashMap<String, Object> pageInfo = _xmlUbForm.getPageInfoMap();
								String totalPage = "0";
					            if(pageInfo != null){
					            	totalPage= pageInfo.get("TOTALPAGE").toString();
								}
					            reqParam.put("TOTAL_PAGE", totalPage);
					            
					            String sPageRangeGubun = udmParams.getREQ_INFO().getPAGE_RANGE_GUBUN() == null ? "0" : udmParams.getREQ_INFO().getPAGE_RANGE_GUBUN();
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
					            
					            HashMap<String, Object> projectInfo = _xmlUbForm.getProjectInfo();
					            double pageWidth = 0;
					            double pageHeight = 0;
					            String pageType = "";
					            if(projectInfo != null){
					            	try {
					            		pageWidth= Double.parseDouble( projectInfo.get("pageWidth").toString());
						            	pageHeight= Double.parseDouble( projectInfo.get("pageHeight").toString());
									
						            	if(pageWidth / pageHeight > 1)pageType = "H";
						            	else pageType = "V";
									} catch (Exception e) {
										// TODO: handle exception
									}
					            }					            
					            reqParam.put("PAGE_TYPE", pageType);
					            
					            body = printPreview(reqParam, hmPrintService , conn ,_param );		
					            
				            	if(hmPrintService.containsKey("RESULT") && "FAIL".equals(hmPrintService.get("RESULT")))
					            {
					            	log.debug("onMessage() LOCAL_PREVIEW()::Result FAIL - print break.");
					 		    }						          
					            else
					            	log.debug("onMessage() LOCAL_PREVIEW()::pdfFile=" + _resultFileName);	
			 					
			 			        File file = new File(_resultFileName);
	 							file.delete();
			 			        
			 			        //test를 위한 Debug 시간 표시 
			 					_exportEndDate = new Date();
			 					long diff = _exportEndDate.getTime() - _exportStartDate.getTime();
			 					long diffSec = diff / 1000% 60;         
			 					long diffMin = diff / (60 * 1000)% 60;      
			 					
			 					String _BmtLogTitle = "============= END PRINT PROCESS :";
			 					
			 					log.debug("onMessage() " + _BmtLogTitle +  " [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_exportEndDate) +"]" );
			 					log.debug("onMessage() " + _BmtLogTitle +  " TOTAL_TIME : [" + diffMin +":"+diffSec+"."+diff%1000 +"]" );
			 			        
			 					log.info("onMessage()" + body);

			 					JSONParser parser = new JSONParser();
			 					JSONObject jsonObject = (JSONObject)parser.parse(body.toString());
			 					String reType = jsonObject.get("type").toString();
			 					if(!reType.equals("STOP")){
			 						this.SendMessage(conn, (String) body, true);
			 					}
			 		        }			 				
			 				else if("LOCAL_PRINT".equals(_pdfExportType))
				            {
			 					boolean _isFirstPrint = true;
			 					HashMap hmPrintService = new HashMap();			 					
			 					
			 					{
			 						String sPrinterName = udmParams.getREQ_INFO().getPDF_EXPORT_PRINTER_NAME();
									String sPrintCommand = sShowPrinterDialog !=null && sShowPrinterDialog.equalsIgnoreCase("true") ? "DIALOG_PRT" : "DIRECT_PRT";
									String sMargin = udmParams.getREQ_INFO().getPDF_PRINTER_MARGIN();									
									
									sPrintCommand = _isFirstPrint ? sPrintCommand : "DIRECT_PRT";
									_isFirstPrint = false;
									
						            JSONObject reqParam = new JSONObject();
						            reqParam.put("COMMAND", sPrintCommand);
						            reqParam.put("PRINTER", sPrinterName);
						            reqParam.put("URL", _resultFileName);	
						            reqParam.put("MARGIN",  sMargin);							          
						            
						            body = printPdfFile(reqParam, hmPrintService);
						            if(hmPrintService.containsKey("RESULT") && "FAIL".equals(hmPrintService.get("RESULT")))
						            {
						            	log.debug("onMessage() LOCAL_PRINT()::Result FAIL - print break.");
						 		    }
						            else
						            	log.debug("onMessage() LOCAL_PRINT()::pdfFile=" + _resultFileName);
			 			        }
			 					
			 			        File file = new File(_resultFileName);
	 							//file.delete();
			 			        
			 			        //test를 위한 Debug 시간 표시 
			 					_exportEndDate = new Date();
			 					long diff = _exportEndDate.getTime() - _exportStartDate.getTime();
			 					long diffSec = diff / 1000% 60;         
			 					long diffMin = diff / (60 * 1000)% 60;      
			 					
			 					String _BmtLogTitle = "============= END PRINT PROCESS :";
			 					
			 					log.debug("onMessage() " + _BmtLogTitle +  " [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(_exportEndDate) +"]" );
			 					log.debug("onMessage() " + _BmtLogTitle +  " TOTAL_TIME : [" + diffMin +":"+diffSec+"."+diff%1000 +"]" );
			 			        
			 					log.info("onMessage()" + body);

			 					this.SendMessage(conn, (String) body, true);
			 		        }
			 				else if("LOCAL_EXCEL".equals(_pdfExportType))
			 				{
			 					File pdfFile = new File(_resultFileName);			
				            	if (Desktop.isDesktopSupported()) 
				            	{
				            		String _message = ""; 
			            			File fileToSave = null;
			            			String _pdfExportFileFullPath = ""; 
			            			
				            		log.debug("onMessage() Desktop.isDesktopSupported()!!~~~_pdfExportFilePath=" + _exportFilePath);
	
				            		if(_exportFileName != null && _exportFilePath != null 
				            				&& _exportFileName.length() > 0 && _exportFilePath.length() > 0)
				            		{
				            			//디렉토리 생성
				            			File _directory = new File( _exportFilePath );
				            			if(!_directory.exists()){
				            				_directory.mkdirs(); 
				            			}
				            			
			            				_pdfExportFileFullPath = _exportFilePath.endsWith("\\") ? _exportFilePath + _exportFileName : _exportFilePath + File.separator + _exportFileName;

				            			if(!(_exportFileName.endsWith(".xlsx") || _exportFileName.endsWith(".XLSX")))
										{
										   	fileToSave = new File(_pdfExportFileFullPath + ".xlsx");
										}
				            			else
				            			{
				            				fileToSave = new File(_pdfExportFileFullPath);
				            			}
				            			 
				            			if(fileMove(pdfFile, fileToSave)==true)
									    {
									    	Desktop.getDesktop().open(fileToSave);
									    	//Desktop.getDesktop().print(fileToSave);
									    }
									    
									    _message = Log.getMessage(Log.MSG_LP_MAKEEXCEL_COMPLETED);
									    log.info("onMessage() SUCCESS - " + _message);
									    
									   this.SendMessage(conn, JSONConverter("SUCCESS", "EXCELEND", Log.MSG_LP_MAKEEXCEL_COMPLETED, _message, fileToSave.getAbsolutePath()), true);

				            		}
				            		else
				            		{
										// parent component of the dialog
										JFrame parentFrame = new JFrame();
										parentFrame.toFront();
	//									JFileChooser fileChooser = new JFileChooser();
	//									JFileChooser fileChooser = new JFileChooser() {
										if(gFileChooser==null) 
											gFileChooser = new JFileChooser() {
												@Override
											    protected JDialog createDialog(Component parent) throws HeadlessException {
											       // intercept the dialog created by JFileChooser
											       JDialog dialog = super.createDialog(parent);
											       dialog.setModal(true);  	// set modality (or setModalityType)
											       
											       if( dialog.isAlwaysOnTopSupported() ) dialog.setAlwaysOnTop(true);
											       dialog.toFront();
											       dialog.repaint();
											       
											       return dialog;
											   }
										};
										
										gFileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("EXCEL File","xlsx"));
										gFileChooser.setDialogTitle("Specify a file to save");   
										gFileChooser.setSelectedFile(new File(gFileChooser.getCurrentDirectory() + File.separator + pdfFile.getName()));
									
										int userSelection = gFileChooser.showSaveDialog(parentFrame);
										if (userSelection == JFileChooser.APPROVE_OPTION) {
										    fileToSave = gFileChooser.getSelectedFile();
										    String saveFilePath = fileToSave.getAbsolutePath();
										    log.debug("onMessage() Save as file: " + saveFilePath);
										    
										    if(!(saveFilePath.endsWith(".xlsx") || saveFilePath.endsWith(".XLSX")))
										    {
										    	fileToSave = new File(saveFilePath + ".xlsx");
										    	gFileChooser.setCurrentDirectory(fileToSave);
										    }
										    
										    if(fileMove(pdfFile, fileToSave)==true)
										    {
										    	Desktop.getDesktop().open(fileToSave);
										    	//Desktop.getDesktop().print(fileToSave);
										    }
										    
										    _message = Log.getMessage(Log.MSG_LP_MAKEEXCEL_COMPLETED);
										    log.info("onMessage() SUCCESS - " + _message);
										    
										   this.SendMessage(conn, JSONConverter("SUCCESS", "EXCELEND", Log.MSG_LP_MAKEEXCEL_COMPLETED, _message, fileToSave.getAbsolutePath()), true);

									
										//} else if (userSelection == JFileChooser.CANCEL_OPTION) {
										} else {	
										    log.debug("onMessage() delete file: " + pdfFile.getAbsolutePath());
											pdfFile.delete();
											
											 _message = Log.getMessage(Log.MSG_LP_MAKEPDF_CANCELED);
										    log.info("onMessage() FAIL - " + _message);
										    
										    this.SendMessage(conn, JSONConverter("FAIL", "EXCELCANCEL", Log.MSG_LP_MAKEEXCEL_CANCELED, _message, ""), true);
										}
										
									    //log.info("onMessage() SUCCESS - MakePdf End.");
										//conn.send(JSONConverter("SUCCESS", "PDFEND", "MakePdf End"));
									}
				            	}
			 				}
			 				else if("LOCAL_IMAGE_TIF".equals(_pdfExportType))
							{
			 					String _message = ""; 
			 					
						        String sourceDir = _resultFileName; // Pdf files are read from this folder
						        //String destinationDir = _exportFolder + "/Converted_PdfFiles_to_Image/"; // converted images from pdf document are saved here
						        String destinationDir = "";
						        
			            		log.debug("onMessage() LOCAL_IMAGE_TIF~~~_pdfExportFilePath=" + _exportFilePath);

			            		if(_exportFilePath != null && _exportFilePath.length() > 0)
			            		{
			            			destinationDir = _exportFilePath;
			            		}
			            		else
			            		{
			            			destinationDir = _exportFolder;
			            		}
			            		
						        File sourceFile = new File(sourceDir);
						        File destinationFolder = new File(destinationDir + File.separator + "Converted_PdfFiles_to_Image" + File.separator);
						    	
						    	try {

							        if (!destinationFolder.exists()) {
							        	if(destinationFolder.mkdirs())
							        		System.out.println("Folder Created -> "+ destinationFolder.getAbsolutePath());
							        	else
							        		System.out.println("Failed to create the Folder -> "+ destinationFolder.getAbsolutePath());
							        }
							        else
							        {
							        	destinationFolder.delete();
							        	destinationFolder.mkdirs();
							        }
							        if (sourceFile.exists()) {
							            
							        	System.out.println("Images copied to Folder: "+ destinationFolder.getAbsolutePath());             
							            PDDocument document = PDDocument.load(sourceFile);
							            //List<PDPage> list = document.getDocumentCatalog().getAllPages();	// 1.8.0
							            //System.out.println("Total files to be converted -> "+ list.size());
							            org.apache.pdfbox.rendering.PDFRenderer pdfRenderer = new org.apache.pdfbox.rendering.PDFRenderer(document);
							            
							            String fileName = sourceFile.getName().replace(".pdf", "");             
							            int pageNumber = 1;
							            
							            String _zipFileName = destinationFolder.getParent() + File.separator + fileName + ".zip";
							    		FileOutputStream fout = new FileOutputStream(_zipFileName);
							    		ZipOutputStream zout = new ZipOutputStream(fout);
							    		
							            // 한페이지씩 만들어진 이미지파일을 하나의 ZIP파일로 묶어서 내보내기 할 수 있도록 구현해야 한다,
							            String _tifFileName = "";
							            
							            _message = Log.getMessage(Log.MSG_LP_MAKETIFF_START);
							            this.SendMessage(conn, JSONConverter("SUCCESS", "TIFFSTART", Log.MSG_LP_MAKETIFF_START, _message, _zipFileName), false);
							        	
							    		//for (PDPage page : list) {
							            for (PDPage page : document.getPages()) {
							            	_tifFileName = destinationDir + File.separator + "Converted_PdfFiles_to_Image" + File.separator + fileName +"_"+ pageNumber +".tiff";
							                //BufferedImage image = page.convertToImage();
							            	// note that the page number parameter is zero based
							            	BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNumber-1, 300, ImageType.RGB);
							                //File outputfile = new File(destinationDir + fileName +"_"+ pageNumber +".png");
							                File outputfile = new File(_tifFileName);
							                System.out.println("Image Created -> "+ outputfile.getName());
							                //ImageIO.write(image, "png", outputfile);
							                //ImageIO.write(image, "TIFF", outputfile); 
							                
							                TIFFEncodeParam params = new TIFFEncodeParam();
							                params.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
							                FileOutputStream outs = new FileOutputStream(outputfile);
							                javax.media.jai.JAI.create("encode", bim, outs, "TIFF", params);
							                bim.flush();
							                outs.close();
							                
							                FileInputStream ins = new FileInputStream(outputfile);
							                ZipEntry ze = new ZipEntry( outputfile.getName() );
							                zout.putNextEntry(ze);
							    		    zout.write(IOUtils.toByteArray(ins));
							    		    zout.closeEntry();
							    		    ins.close();
							               								    		    
							                pageNumber++;
							            }
							        	document.close();
							    		zout.close();
							    		fout.close();

							            System.out.println("Converted Images are saved at -> "+ destinationFolder.getAbsolutePath());
							            
							            _message = Log.getMessage(Log.MSG_LP_MAKETIFF_COMPLETED);
									    log.info("onMessage() SUCCESS - " + _message);
									    
									    // 완료되면 폴더채로 제거하도록 처리 
										//File file = new File(destinationDir);
										//폴더내 파일을 배열로 가져온다.
										File[] tempFile = destinationFolder.listFiles();
										
										if(tempFile.length >0){
											for (int i = 0; i < tempFile.length; i++) {
												tempFile[i].delete();
												System.out.println("Delete file -> "+ tempFile[i].getAbsolutePath());
											}
										}
									    
										System.out.println("Completed convert images files to zip -> "+ _zipFileName);
								
										destinationFolder.delete();
										sourceFile.delete();
										
										this.SendMessage(conn, JSONConverter("SUCCESS", "TIFFEND", Log.MSG_LP_MAKETIFF_COMPLETED, _message, _zipFileName), true);
							        } 
						    		else 
						    		{
							            System.err.println(sourceFile.getName() +" File not exists");
							        }

							    } catch (Exception e) {
							        e.printStackTrace();
							        
							        _message = Log.getMessage(Log.MSG_LP_MAKETIFF_CANCELED) + " : " + e.getMessage();
								    log.error("onMessage() FAIL - " + _message);
								    
								    this.SendMessage(conn, JSONConverter("FAIL", "TIFFCANCEL", Log.MSG_LP_MAKETIFF_CANCELED, _message, ""), true);
							    }
							}
			 				else if("LOCAL_FAX_TIF".equals(_pdfExportType))
							{
			 					//Name of the pdf/A converted 
			 					String tiffFilePath = ""; 
			 				  			 					
			 				    double FAX_RESOLUTION = 200.0;
			 				    double PRINTER_RESOLUTION = 300.0;
			 				    // This compression type may be wpecific to JAI ImageIO Tools
			 				    String COMPRESSION_TYPE_GROUP4FAX = "CCITT T.6";			 					
			 					
			 					String _message = ""; 
			 					
						        String sourceDir = _resultFileName; // Pdf files are read from this folder
						        String destinationDir = "";
						        
			            		log.debug("onMessage() LOCAL_FAX_TIF~~~_pdfExportFilePath=" + _exportFilePath);

			            		if(_exportFilePath != null && _exportFilePath.length() > 0)
			            		{
			            			destinationDir = _exportFilePath;
			            		}
			            		else
			            		{
			            			destinationDir = _exportFolder;
			            		}
			            		
						        File sourceFile = new File(sourceDir);
						        File destinationFolder = new File(destinationDir);
						    	
						        org.icepdf.core.pobjects.Document pobjDocument = null;
						        ImageWriter writer = null;
						    	try {

							        if (!destinationFolder.exists()) {
							        	if(destinationFolder.mkdirs())
							        		System.out.println("Folder Created -> "+ destinationFolder.getAbsolutePath());
							        	else
							        		System.out.println("Failed to create the Folder -> "+ destinationFolder.getAbsolutePath());
							        }
							        
							        if (sourceFile.exists()) {
							            
							        	String fileName = sourceFile.getName().replace(".pdf", "");         
							        	tiffFilePath = destinationFolder.getAbsolutePath() + File.separator + fileName + ".tiff";
							        	
							        	_message = Log.getMessage(Log.MSG_LP_MAKETIFF_START);
									    log.info("onMessage() SUCCESS - " + _message);						    
									    this.SendMessage(conn, JSONConverter("SUCCESS", "TIFFSTART", Log.MSG_LP_MAKETIFF_START, _message, ""), false);	
						               
							        	System.out.println("Create tiff image file for FAX : "+ tiffFilePath);             
							            
							        	 // Verify that ImageIO can output TIFF
							            Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName("tiff");
							            if (!iterator.hasNext()) {
							            	_message = "ImageIO missing required plug-in to write TIFF files. " +
							                    "You can download the JAI ImageIO Tools from: " +
							                    "https://jai-imageio.dev.java.net/";
							                
							                _message = Log.getMessage(Log.MSG_LP_MAKETIFF_CANCELED) + " : " + _message;
										    log.error("onMessage() FAIL - " + _message);
										    
										    this.SendMessage(conn, JSONConverter("FAIL", "TIFFCANCEL", Log.MSG_LP_MAKETIFF_CANCELED, _message, ""), true);	
							                
							                return;
							            }
							            boolean foundCompressionType = false;
							            for(String type : iterator.next().getDefaultWriteParam().getCompressionTypes()) {
							                if (COMPRESSION_TYPE_GROUP4FAX.equals(type)) {
							                    foundCompressionType = true;
							                    break;
							                }
							            }
							            if (!foundCompressionType) {
							                System.out.println(
							                    "TIFF ImageIO plug-in does not support Group 4 Fax " +
							                    "compression type ("+COMPRESSION_TYPE_GROUP4FAX+")");
							                return;
							            }
							        	
							            // open the url
							            pobjDocument = new org.icepdf.core.pobjects.Document();
							            
							            pobjDocument.setFile(sourceDir);
							            
							            // save page caputres to file.
							            File tiffFile = new File(tiffFilePath);
							            ImageOutputStream ios = ImageIO.createImageOutputStream(tiffFile);
							            writer = ImageIO.getImageWritersByFormatName("tiff").next();
							            writer.setOutput(ios);
							            
							            // Paint each pages content to an image and write the image to file
							            for (int i = 0; i < pobjDocument.getNumberOfPages(); i++) {
							                final double targetDPI = PRINTER_RESOLUTION;
							                float scale = 1.0f;
							                float rotation = 0f;
							                // Given no initial zooming, calculate our natural DPI when
							                // printed to standard US Letter paper
							                PDimension size = pobjDocument.getPageDimension(i, rotation, scale);
							                double dpi = Math.sqrt((size.getWidth()*size.getWidth()) +
							                                       (size.getHeight()*size.getHeight()) ) /
							                             Math.sqrt((8.5*8.5)+(11*11));
							                // Calculate scale required to achieve at least our target DPI
							                if (dpi < (targetDPI-0.1)) {
							                    scale = (float) (targetDPI / dpi);
							                    size = pobjDocument.getPageDimension(i, rotation, scale);
							                }
							                int pageWidth = (int) size.getWidth();
							                int pageHeight = (int) size.getHeight();
							                
							                int[] cmap = new int[] { 0xFF000000, 0xFFFFFFFF };
							                IndexColorModel cm = new IndexColorModel(
							                    1, cmap.length, cmap, 0, true, Transparency.BITMASK,
							                    DataBuffer.TYPE_BYTE);
							                BufferedImage image = new BufferedImage(
							                    pageWidth, pageHeight, BufferedImage.TYPE_BYTE_BINARY, cm);
							                /*
							                BufferedImage image = new BufferedImage(
								                    pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB);
							                */
							                Graphics g = image.createGraphics();
							                pobjDocument.paintPage(
							                    i, g, GraphicsRenderingHints.PRINT, Page.BOUNDARY_CROPBOX,
							                    rotation, scale);
							                g.dispose();              
							                // capture the page image to file
							                IIOImage img = new IIOImage(image, null, null);
							                ImageWriteParam param = writer.getDefaultWriteParam();
							                param.setCompressionMode(param.MODE_EXPLICIT);
							                param.setCompressionType(COMPRESSION_TYPE_GROUP4FAX);
							                //param.setCompressionType("LZW");
							                //param.setCompressionQuality(0.7f);
							                if (i == 0) {
							                    writer.write(null, img, param);
							                }
							                else {
							                    writer.writeInsert(-1, img, param);
							                }
							                image.flush();
							                cm.finalize();
							            }            
							            ios.flush();
							            ios.close();
							            writer.dispose();
							            writer = null;
							            
							            // clean up resources
							            pobjDocument.dispose();
							            pobjDocument = null;
							            
							            sourceFile.delete();
							            
							            _message = Log.getMessage(Log.MSG_LP_MAKETIFF_COMPLETED);
							            System.out.println(_message + "->" + tiffFilePath);
							            
							            this.SendMessage(conn, JSONConverter("SUCCESS", "TIFFEND", Log.MSG_LP_MAKETIFF_COMPLETED, _message, tiffFilePath), true);
							        } 
						    		else 
						    		{
							            System.err.println(sourceFile.getName() +" File not exists");
							        }

							    } catch (Exception e) {
							        e.printStackTrace();
							        _message = Log.getMessage(Log.MSG_LP_MAKETIFF_CANCELED) + " : " + e.getMessage();
								    log.error("onMessage() FAIL - " + _message);
								    
								    this.SendMessage(conn, JSONConverter("FAIL", "TIFFCANCEL", Log.MSG_LP_MAKETIFF_CANCELED, _message, ""), true);							        
							    } finally {
							    	if(writer != null)
							    		writer.dispose();
							    	if(pobjDocument != null)
							    		pobjDocument.dispose();
							    }
							}
				            else
				            {
				            	boolean isPdfReaderOpen = true;
				            	if(_pdfReaderOpen != null && _pdfReaderOpen.toUpperCase().equals("FALSE")){				            		
				            		isPdfReaderOpen = false;
				            	}
				            	File pdfFile = new File(_resultFileName);			
				            	if (Desktop.isDesktopSupported()) 
				            	{
				            		String _message = ""; 
			            			File fileToSave = null;
			            			String _pdfExportFileFullPath = ""; 
			            			
				            		log.debug("onMessage() Desktop.isDesktopSupported()!!~~~_pdfExportFilePath=" + _exportFilePath);
	
				            		if(_exportFileName != null && _exportFilePath != null 
				            				&& _exportFileName.length() > 0 && _exportFilePath.length() > 0)
				            		{
				            			//디렉토리 생성
				            			File _directory = new File( _exportFilePath );
				            			if(!_directory.exists()){
				            				_directory.mkdirs(); 
				            			}
				            			
			            				_pdfExportFileFullPath = _exportFilePath.endsWith("\\") ? _exportFilePath + _exportFileName : _exportFilePath + "\\" + _exportFileName;

				            			if(!(_exportFileName.endsWith(".pdf") || _exportFileName.endsWith(".PDF")))
										{
										   	fileToSave = new File(_pdfExportFileFullPath + ".pdf");
										}
				            			else
				            			{
				            				fileToSave = new File(_pdfExportFileFullPath);
				            			}
				            			 
				            			if(fileMove(pdfFile, fileToSave)==true)
									    {
									    	if(isPdfReaderOpen){//KB카드 요청으로 해당 속성이 true일때만 오픈한다.
									    		Desktop.getDesktop().open(fileToSave);
									    	}
									    	//Desktop.getDesktop().print(fileToSave);
									    }
									    
									    _message = Log.getMessage(Log.MSG_LP_MAKEPDF_COMPLETED);
									    log.info("onMessage() SUCCESS - " + _message);
									    
									   this.SendMessage(conn, JSONConverter("SUCCESS", "PDFEND", Log.MSG_LP_MAKEPDF_COMPLETED, _message, fileToSave.getAbsolutePath()), true);

				            		}
				            		else
				            		{
										// parent component of the dialog
										JFrame parentFrame = new JFrame();
										parentFrame.toFront();
	//									JFileChooser fileChooser = new JFileChooser();
	//									JFileChooser fileChooser = new JFileChooser() {
										if(gFileChooser==null) 
											gFileChooser = new JFileChooser() {
												@Override
											    protected JDialog createDialog(Component parent) throws HeadlessException {
											       // intercept the dialog created by JFileChooser
											       JDialog dialog = super.createDialog(parent);
											       dialog.setModal(true);  	// set modality (or setModalityType)
											       
											       if( dialog.isAlwaysOnTopSupported() ) dialog.setAlwaysOnTop(true);
											       dialog.toFront();
											       dialog.repaint();
											       
											       return dialog;
											   }
										};
										
										String _fileExt = pdfFile.getName().endsWith(".zip") || pdfFile.getName().endsWith(".ZIP") ? "zip" : "pdf";
										if("zip".equals(_fileExt))
											gFileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF ZIP File","zip"));
										else
											gFileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF File","pdf"));
										gFileChooser.setDialogTitle("Specify a file to save");   
										gFileChooser.setSelectedFile(new File(gFileChooser.getCurrentDirectory() + "\\" + pdfFile.getName()));
									
										int userSelection = gFileChooser.showSaveDialog(parentFrame);
										if (userSelection == JFileChooser.APPROVE_OPTION) {
										    fileToSave = gFileChooser.getSelectedFile();
										    String saveFilePath = fileToSave.getAbsolutePath();
										    log.debug("onMessage() Save as file: " + saveFilePath);
										    
										    if(!(saveFilePath.endsWith(".pdf") || saveFilePath.endsWith(".PDF")) && !(saveFilePath.endsWith(".zip") || saveFilePath.endsWith(".ZIP")))
										    {
										    	fileToSave = new File(saveFilePath + ".pdf");
										    	gFileChooser.setCurrentDirectory(fileToSave);
										    }
										    
										    if(fileMove(pdfFile, fileToSave)==true)
										    {
										    	if(isPdfReaderOpen){//KB카드 요청으로 해당 속성이 true일때만 오픈한다.
										    		Desktop.getDesktop().open(fileToSave);
										    	}
										    	//Desktop.getDesktop().print(fileToSave);
										    }
										    
										    _message = Log.getMessage(Log.MSG_LP_MAKEPDF_COMPLETED);
										    log.info("onMessage() SUCCESS - " + _message);
										    
										   this.SendMessage(conn, JSONConverter("SUCCESS", "PDFEND", Log.MSG_LP_MAKEPDF_COMPLETED, _message, fileToSave.getAbsolutePath()), true);

									
										//} else if (userSelection == JFileChooser.CANCEL_OPTION) {
										} else {	
										    log.debug("onMessage() delete file: " + pdfFile.getAbsolutePath());
											pdfFile.delete();
											
											 _message = Log.getMessage(Log.MSG_LP_MAKEPDF_CANCELED);
										    log.info("onMessage() FAIL - " + _message);
										    
										    this.SendMessage(conn, JSONConverter("FAIL", "PDFCANCEL", Log.MSG_LP_MAKEPDF_CANCELED, _message, ""), true);
										}
										
									    //log.info("onMessage() SUCCESS - MakePdf End.");
										//conn.send(JSONConverter("SUCCESS", "PDFEND", "MakePdf End"));
									}
				            	}
				            }
			 				
			 				_xmlUbForm.clear();
							_xmlUbForm = null;
			 			}
			 			
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
						log.error("onMessage() " + Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + e.getMessage());
						
						body = JSONConverter("FAIL", _reqCmdName, Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + e.getMessage(), "");
					} 
					finally
					{
						if(_xmlUbForm != null)
						{
							_xmlUbForm.clear();
							_xmlUbForm = null;
						}
					}
				}					
								
			} catch (ParseException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				log.error("onMessage() " + Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + e.getMessage());
				
				body = JSONConverter("FAIL", "OPEN", Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + e.getMessage(), "");
			} 
    	}
		
		//conn.send((String) body);
	}
	
	private void SendMessage(WebSocket conn, String messageData, boolean bSetConSocketAcceptReady)
	{
		mUdpClient.sendMessage(messageData);
		if(bSetConSocketAcceptReady == true)
			Log.gConSocketAcceptReady = true;	// OPEN, PRINTEND, PDFEND 일경우 사전 처리
		conn.send(messageData);
	}
	
	public String JSONConverter(String type, String command, String code, String message, String fileUrl) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("port", Log.wsPort);
		jsonObject.put("type", type);
		jsonObject.put("command", command);
		jsonObject.put("code", code);
		jsonObject.put("message", message);
		jsonObject.put("url", fileUrl);
		return jsonObject.toString();
	}
	
	public JSONObject JSONMessageConverter(String type, String command, String code, String message, String fileUrl) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("port", Log.wsPort);
		jsonObject.put("type", type);
		jsonObject.put("command", command);
		jsonObject.put("code", code);
		jsonObject.put("message", message);
		jsonObject.put("url", fileUrl);
		return jsonObject;
	}
	
	private String getCurrentVersion()
	{
		String _curVer = "";
		String _regKey = Log.is64bit ? "SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\UBIForm Local Exporter" : "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\UBIForm Local Exporter";
		
		try {
			_curVer = WinRegistry.readString (
				    WinRegistry.HKEY_LOCAL_MACHINE,  	//HKEY
				    _regKey,           					//Key
				   "DisplayVersion");
		} catch (IllegalArgumentException | IllegalAccessException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return _curVer;
	}
	
	private boolean isUpdateCheck(String curVer, String localExporterUpdateUrl)
	{
		boolean isUpdate = false;
		
		URL _url = null;
		URLConnection _urlConnection = null;
		InputStream _inputS = null;
		InputStreamReader _inputR = null;
		BufferedReader _buf = null;
		String _str = null;
		StringBuilder resultStrb = new StringBuilder();
		
		ArrayList<HashMap<String, Object>> _data = new ArrayList<HashMap<String, Object>>();
		
		if(localExporterUpdateUrl != null && localExporterUpdateUrl.length() > 0)
		{
			try {
				
				String propsFileName = Log.basePath + "config/ubiform.properties";
			    try {
			    	//modifies existing or adds new property
			    	Log.ubiformProps.remove("Update.Url");
			    	Log.ubiformProps.put("Update.Url", localExporterUpdateUrl);		

   			      	//save modified property file
			    	FileOutputStream output = new FileOutputStream(propsFileName);
			    	Log.ubiformProps.store(output, "Change Update.Url");
			    	output.close();

			    	mUdpClient.sendMessage(JSONConverter("SUCCESS", "GET_UPDATE_URL", Log.MSG_LP_GET_UPDATER_URL, localExporterUpdateUrl, ""));
			    	
			    } catch (IOException ex) {
			    	ex.printStackTrace();
			    }
				
				_url = new URL(localExporterUpdateUrl);
				_urlConnection = _url.openConnection();
				_urlConnection.setConnectTimeout(5*1000);
				_urlConnection.setReadTimeout(5*1000);
				
				_inputS = _urlConnection.getInputStream();
				_inputR = new InputStreamReader(_inputS , "UTF-8");
				
				_buf = new BufferedReader(_inputR);				
				while (true)
				{
					_str = _buf.readLine();
					if( _str == null) break;
					resultStrb.append(_str);
				}
				
				String resultStr = resultStrb.toString();
				if(resultStr != null && !resultStr.equals("") )
				{
					resultStr = resultStr.trim().replaceFirst("^([\\W]+)<","<");
					System.out.println("update_exporter.xml=[" + resultStr + "]");
					InputSource _is = new InputSource(new StringReader(resultStr));
					Document _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
					
					NodeList _exeList;
					HashMap<String, Object> _dataRow;
					_exeList = _doc.getElementsByTagName("exe");
					
					// exe Modules
					int _exeListLength = _exeList.getLength();
					for (int i = 0; i < _exeListLength; i++) 
					{
						_dataRow = new HashMap<String, Object>();
						
						Element _childData = (Element) _exeList.item(i);								
						NodeList _childList = _childData.getChildNodes();
						int _childListLength = _childList.getLength();
						for (int j = 0; j < _childListLength; j++) 
						{
							Node _rowData = _childList.item(j);
							if( _rowData instanceof Element )
							{
								String _nodeName = _rowData.getNodeName();
								String _nodeValue = _rowData.getTextContent();
								
								_dataRow.put(_nodeName, _nodeValue);
							}
						}
						
						_data.add(_dataRow);
					}
					
				}
				
				
				String _updateInfo_version = (String) _data.get(0).get("version");
				String _updateInfo_url = (String) _data.get(0).get("url");
				
				System.out.println("==================> curVer=" + curVer + ",update_exporter.version=" + _updateInfo_version);
				
				if(curVer.equals(_updateInfo_version))
				{
					isUpdate = false;
				}
				else
				{
					String[] currVers = curVer.split("\\.");
					String[] updateVers = _updateInfo_version.split("\\.");
					if(currVers.length == 3 && currVers.length == updateVers.length)
					{
						for(int k=0; k < currVers.length; k++)
						{
							if(Integer.parseInt(currVers[k]) < Integer.parseInt(updateVers[k]))
							{
								isUpdate = true;
								break;
							}
							else if(Integer.parseInt(currVers[k]) > Integer.parseInt(updateVers[k]))
							{
								isUpdate = false;
								break;
							}
						}
					}
					else
					{
						isUpdate = false;
					}
				}
				
				
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(_inputR != null) {
					try {
						_inputR.close();
						_inputR = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			
		}
		
		return isUpdate;
	}
	
	private JSONObject getLicenseText(xmlToUbForm tXmlToUbForm, String reqCmdName, String serverUrl, UDMParamSet udmParams) throws UnsupportedEncodingException
	{
		String _type = "SUCCESS";
		String _code = "00000";
		String _message = "";
		
		String _projectName = udmParams.getREQ_INFO().getPROJECT_NAME();
		String _formName = udmParams.getREQ_INFO().getFORM_ID();
		
		//String url = "http://192.168.0.127:8080/UBIServerWeb/ubiform.do"; //server url (https로 되어있는)
		String url = serverUrl; //server url 
		
		if(url != null && url.toUpperCase().startsWith("LOCAL://"))
			Log.formFileGetFromServer = false;
		else
		{
			String _formFileGetFromServer = common.getPropertyValue("Form.GetFileFromServer");		   	
			if(_formFileGetFromServer != null)
			{
				Log.formFileGetFromServer = "false".equals(_formFileGetFromServer) ? false : true;
			}
		}
		
		if(Log.formFileGetFromServer)
		{
			if(gHttpclient == null) 
			{
				org.apache.http.impl.client.HttpClientBuilder clb = org.apache.http.impl.client.HttpClientBuilder.create();
				gHttpclient =  clb.build();
				//gHttpclient = new DefaultHttpClient();
				
				//HttpClient timeout 셋팅
	            /*
	            gHttpclient.getParams().setParameter("http.protocol.expect-continue", false);//HttpClient POST 요청시 Expect 헤더정보 사용 x
	            gHttpclient.getParams().setParameter("http.connection.timeout", 3 * 1000);// 원격 호스트와 연결을 설정하는 시간
	            gHttpclient.getParams().setParameter("http.socket.timeout",  3 * 1000);//데이터를 기다리는 시간
	            gHttpclient.getParams().setParameter("http.connection-manager.timeout",  3 * 1000);// 연결 및 소켓 시간 초과 
	            gHttpclient.getParams().setParameter("http.protocol.head-body-timeout",  3 * 1000);
				*/
			}
			
			HttpPost httppost = null;
	        try
			{
	            httppost = new HttpPost(url); 
	            
	            log.info("getLicenseText() executing request" + httppost.getRequestLine());
	        
	            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	    		nameValuePairs.add(new BasicNameValuePair("DOMAIN", ""));
	    		nameValuePairs.add(new BasicNameValuePair("FILE_TYPE", "exec"));
	    		nameValuePairs.add(new BasicNameValuePair("CALL", "VIEWER5"));
	    		nameValuePairs.add(new BasicNameValuePair("METHOD_NAME", "getServerToken"));
	    		
	            UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
	            httppost.setEntity(entity1);
	            
			    HttpResponse response = gHttpclient.execute(httppost);
		        HttpEntity entity = response.getEntity();
		        StringBuilder stbSessionInfo = new StringBuilder();
		        if (entity != null) 
		        {
		            InputStream inputStream = entity.getContent();
		            InputStreamReader ir = new InputStreamReader(inputStream);
		            BufferedReader br = new BufferedReader(ir);  
		            String line = null;  
		            while ((line = br.readLine()) != null) {  
		                 System.out.println(line);  
		                 stbSessionInfo.append(line);
		            }  
		            br.close();
		            ir.close();
		            inputStream.close();
			            
		            String resdata = stbSessionInfo.toString();
		            String sessionId = "";
		            String licenseType = "";
		            String localExporterUpdateUrl = "";
		            String useJsonFormFile = "";
		            String useSimpleJsonFormFile = "";
		         
		            // UBICrossDomainCallback({"MSG":"0E5FFF5DB5025B57CAEB67A66DF589B4","RESULT":"SUCCESS"})
		            int nMsgStartIndex = resdata != "" ? resdata.indexOf("UBICrossDomainCallback") : -1;
				    if(nMsgStartIndex != -1)
				    {
				    	  resdata = resdata.substring(nMsgStartIndex);
				    						  
						  int nLastIdx = resdata.lastIndexOf(")");
						  resdata = resdata.substring(23, nLastIdx);
						  
						  JSONObject ubObj = (JSONObject) JSONValue.parseWithException(resdata);
						  sessionId = (String) ubObj.get("MSG");
						  licenseType = (String) ubObj.get("LICENSE_TYPE");
						  
						  localExporterUpdateUrl = (String) ubObj.get("localExporter_updateUrl");
						  
						  useJsonFormFile = (String) ubObj.get("useJsonFormFile");
						  
						  // For Local exe test, useSimpleJsonFormFile is fixed to false;
//						  useSimpleJsonFormFile = (String) ubObj.get("useSimpleJsonFormFile");
						  useSimpleJsonFormFile = "false";
						  
						  // server에서 내려받은  user.properties가 있을경우 담아둔다. 
						  if( ubObj.containsKey("SRV_USER_PROPS"))
						  {
							  AesUtil5 aesUtil = new AesUtil5();
							  String _userProp = aesUtil.decrypt( ubObj.get("SRV_USER_PROPS").toString() );
							 
							  if( _userProp != null )
							  {
								  Log.ubiformUserProps = _userProp;
								  Log.ubiformUserPropJson = (JSONObject) JSONValue.parseWithException(_userProp);
							  }
							  
						  }
				    }
		            
		            log.debug("getLicenseText() SessionId=" + sessionId + ", licenseType=" + licenseType + ", localExporterUpdateUrl=" + localExporterUpdateUrl + ", useSimpleJsonFormFile=" + useSimpleJsonFormFile);
					 
				    if( sessionId != null  )
					{
						udmParams.getREQ_INFO().setCLIENT_SESSION_ID(sessionId);
					}
				    
				    if( licenseType != null )
				    {
				    	udmParams.getREQ_INFO().setLICENSE_TYPE(licenseType.toUpperCase());
				    }
				    
				    if( useJsonFormFile != null  )
					{
						udmParams.getREQ_INFO().setUSE_JSON_FORM_FILE(useJsonFormFile);
					}
				    
				    if( useSimpleJsonFormFile != null  )
					{
						udmParams.getREQ_INFO().setUSE_SIMPLE_JSON_FORM_FILE(useSimpleJsonFormFile);
					}
	
				    // Exporter의 update할 내용이 있으면, 사용자에게 업데이트를 유도하고 더 이상 진행되지 않도록 한다. 
		            // 그리고 설치디렉토리에 위치한 ubiform.properties의 Update.Url 값을 수정한다.			    			    
				    // 현재버전 조회
				    String currentVersion = getCurrentVersion();
				    
				    // 업데이트 채크
				    boolean isUpdate = isUpdateCheck(currentVersion, localExporterUpdateUrl);
				    if(isUpdate)
				    {
				    	_message = Log.getMessage(Log.MSG_LP_LOCALEXPORTER_REQUIRED_UPDATE);
						_type = "FAIL";
						_code = Log.MSG_LP_LOCALEXPORTER_REQUIRED_UPDATE;
						
						log.info(_message);
				    }
				    else
				    {
					    boolean isSuccess = true;
					    if(Log.formFileGetFromServer)
							isSuccess = getFormXmlData(tXmlToUbForm, reqCmdName, serverUrl, udmParams);
						else
							isSuccess = getLocalFormXmlData(reqCmdName, serverUrl, udmParams);
						if(isSuccess)
						{
				            log.info("getLicenseText() Success to get form file contents. " + _projectName + "/" + _formName);
			
					        _message = Log.getMessage(Log.MSG_LP_SUUCESS_GET_FORMFILE_CONTENT) + _projectName + "/" + _formName;
							_type = "SUCCESS";
							_code = Log.MSG_LP_SUUCESS_GET_FORMFILE_CONTENT;
						}
						else
						{
				            log.info("getLicenseText() Failed to get form file contents." + _projectName + "/" + _formName);
							
					        _message = Log.getMessage(Log.MSG_LP_FAIL_GET_FORMFILE_CONTENT) + _projectName + "/" + _formName;
							_type = "FAIL";
							_code = Log.MSG_LP_FAIL_GET_FORMFILE_CONTENT;
						}
				    }
			    }
		        else
		        {
		            log.info("getLicenseText() Failed to get ServerToken. Response entity is null.");
		
			        _message = Log.getMessage(Log.MSG_LP_FAIL_GET_SERVER_TOKEN);
					_type = "FAIL";
					_code = Log.MSG_LP_FAIL_GET_SERVER_TOKEN;
		        }
			}
	 		catch(Exception e)
			{
				e.printStackTrace();
				_type = "FAIL";
				_message = e.getMessage();
				_code = Log.MSG_SYSTEM_EXCEPTION;
				
				log.error("getLicenseText() Exception::" + e.getMessage());
			}
			finally
			{
				if(httppost != null)
				{
					httppost.abort();
				}
				//gHttpclient.getConnectionManager().shutdown();
				/*
				try {
					gHttpclient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
			}
        
		}
		else
		{
			boolean isSuccess = true;
			isSuccess = getLocalFormXmlData(reqCmdName, serverUrl, udmParams);
			if(isSuccess)
			{
	            log.info("getLicenseText() Success to get form file contents. " + _projectName + "/" + _formName);

		        _message = Log.getMessage(Log.MSG_LP_SUUCESS_GET_FORMFILE_CONTENT) + _projectName + "/" + _formName;
				_type = "SUCCESS";
				_code = Log.MSG_LP_SUUCESS_GET_FORMFILE_CONTENT;
			}
			else
			{
	            log.info("getLicenseText() Failed to get form file contents." + _projectName + "/" + _formName);
				
		        _message = Log.getMessage(Log.MSG_LP_FAIL_GET_FORMFILE_CONTENT) + _projectName + "/" + _formName;
				_type = "FAIL";
				_code = Log.MSG_LP_FAIL_GET_FORMFILE_CONTENT;
			}
		}
		
        return JSONMessageConverter(_type, reqCmdName, _code, _message, "");
	}
	
	// UBIForm Server로 데이터 조회와 폼파일정보를 요청하고 받는다.(loadtype이 one인 경우에만 호출된다)
	private boolean getFormXmlData(xmlToUbForm tXmlToUbForm, String reqCmdName, String serverUrl, UDMParamSet udmParams) throws UnsupportedEncodingException
	{
		boolean _result = true;
		String _message = "";
		
		String _projectType = "1";
		
		String _methodName = "GetViewXmlData";
		String _serverUrl = serverUrl;
		String _projectName = udmParams.getREQ_INFO().getPROJECT_NAME();
		String _formName = udmParams.getREQ_INFO().getFORM_ID();
		String _fileName = udmParams.getREQ_INFO().getFILE_NAME();
		String _clientSessionId = udmParams.getREQ_INFO().getCLIENT_SESSION_ID();
		//String _useJsonFormFile = udmParams.getREQ_INFO().getUSE_JSON_FORM_FILE();
		String _useSimpleJsonFormFile = udmParams.getREQ_INFO().getUSE_SIMPLE_JSON_FORM_FILE();
		
		String _ubFileLoadType = "true".equals(_useSimpleJsonFormFile) ? "SIMPLE" : "XML";
		_fileName = "SIMPLE".equals(_ubFileLoadType) ? "simpleForm.json" : _fileName;		
		
		String _multiFromList = "";
		if( udmParams.getREQ_INFO().getUB_FORMLIST_INFO() != null && "".equals( udmParams.getREQ_INFO().getUB_FORMLIST_INFO() ) == false )
		{
			_multiFromList = udmParams.getREQ_INFO().getUB_FORMLIST_INFO();
		}
		String MULTI_FORM_TYPE =  udmParams.getREQ_INFO().getMULTI_FORM_TYPE();
		
		if(_multiFromList != null && _multiFromList.length() > 0)
		{
			_methodName = "GetViewMultiFormXmlData";
			MULTI_FORM_TYPE = "S"; // 일당은 'S'로 고정사용

			log.debug("getFormXmlData() METHOD_NAME=" + _methodName + ", SESSION_ID=" + _clientSessionId + ", _ubFileLoadType=" + _ubFileLoadType);
		}
		else
		{
			_projectName = URLDecoder.decode(_projectName, "UTF-8");
			_formName = URLDecoder.decode(_formName, "UTF-8");
			
			log.debug("getFormXmlData() METHOD_NAME=" + _methodName + ", PROJECT_NAME=" + _projectName + ", FORM_NAME=" + _formName + ", SESSION_ID=" + _clientSessionId + ", _ubFileLoadType=" + _ubFileLoadType);
		}
		
		if(gHttpclient==null) 
		{
			org.apache.http.impl.client.HttpClientBuilder clb = org.apache.http.impl.client.HttpClientBuilder.create();
			gHttpclient =  clb.build();
			//gHttpclient = new DefaultHttpClient();
		}
		
		//String url = "http://192.168.0.127:8080/UBIServerWeb/ubiform.do"; //server url (https로 되어있는)
		String url = _serverUrl; //server url
		HttpPost httppost = null;
        try
		{
            httppost = new HttpPost(url);
            
    		log.info("getFormXmlData() Executing request " + httppost.getRequestLine());
            
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
    		nameValuePairs.add(new BasicNameValuePair("DOMAIN", ""));	
    		nameValuePairs.add(new BasicNameValuePair("FILE_TYPE", "exec"));
    		nameValuePairs.add(new BasicNameValuePair("CALL", "VIEWER"));
    		nameValuePairs.add(new BasicNameValuePair("METHOD_NAME", _methodName));
    		
    		//nameValuePairs.add(new BasicNameValuePair("UBPARAMS", _paramsJson));
    		
    		if(_multiFromList != null && _multiFromList.length() > 0)
    		{
    			nameValuePairs.add(new BasicNameValuePair("UB_FORMLIST_INFO", _multiFromList));
        		nameValuePairs.add(new BasicNameValuePair("MULTI_FORM_TYPE", MULTI_FORM_TYPE));
    		}
    		else
    		{
    			nameValuePairs.add(new BasicNameValuePair("PROJECT_NAME", _projectName));
        		nameValuePairs.add(new BasicNameValuePair("FORM_ID", _formName));
    		}
    		    		
    		nameValuePairs.add(new BasicNameValuePair("FILE_NAME", _fileName));
    		nameValuePairs.add(new BasicNameValuePair("CLIENT_SESSION_ID", _clientSessionId));
    		nameValuePairs.add(new BasicNameValuePair("UB_FILE_LOAD_TYPE", _ubFileLoadType));
    		               
            UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            httppost.setEntity(entity1);
            
		    HttpResponse response = gHttpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        if (entity != null) {
	            long len = entity.getContentLength();
	            InputStream inputStream = entity.getContent();
	            InputStreamReader ir = new InputStreamReader(inputStream);
	            BufferedReader br = new BufferedReader(ir);  
	           
	            StringBuffer _fileContents = new StringBuffer();
	            String line = null;  
	            while ((line = br.readLine()) != null) {  
	                //System.out.println(line); 
	                _fileContents.append(line);
	            }
	            br.close();
	            ir.close();
	            inputStream.close();
	            
	            String resContent = _fileContents.toString();
	            
	    		log.debug("getFormXmlData() XMLDATA CONTENTS_SIZE=" + resContent.length());
	            
	    		if(resContent.startsWith("FAIL-"))
	    		{
	    			_result = false;
					_message = resContent;
					
					log.error("getFormXmlData() : " + resContent);
	    		}
	    		else
	    		{	 
	    			if(resContent.length() > 0)
	    			{
			            //udmParams.getREQ_INFO().setFILE_CONTENT(_fileContents.toString());
			            //exportPDF(udmParams);
			    		String decXml = "";
			    		String XML_DATA = "";
			    		if(_multiFromList != null && _multiFromList.length() > 0)
			    		{
			    			decXml = common.base64_decode(resContent, "UTF-8");
			    			
			    			Object ubObj = JSONValue.parseWithException((String)decXml);	
			    			JSONObject objMutiFormInfo = (JSONObject)ubObj;
			    			
			    			String _decProjXml = (String) objMutiFormInfo.get("PROJECTS_XML");
			    			XML_DATA = common.base64_decode(_decProjXml, "UTF-8");
			    			
			    			//BufferedWriter out = new BufferedWriter(new FileWriter( "D:\\UBIWorkspaceNew\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\UBIServerWeb\\UFile\\sys\\temp\\MergedXmlFile.txt" ));
							//out.write( XML_DATA );
							//out.close();
			    			
				            JSONObject objProjectInfo = xmlToDocument(XML_DATA);
					           
				    		log.debug("getFormXmlData() pageWidth=" + objProjectInfo.get("pageWidth") + ", pageHeight=" + objProjectInfo.get("pageHeight"));
				    		_projectType = String.valueOf(objProjectInfo.get("projectType"));
				    		
				            gPageWidth = Integer.valueOf((String)objProjectInfo.get("pageWidth"));
				            gPageHeight = Integer.valueOf((String)objProjectInfo.get("pageHeight"));
				            
				            
				            ArrayList<JSONObject> _docParams = (ArrayList<JSONObject>) objMutiFormInfo.get("DOCUMENT_PARAMS");
				            ArrayList<HashMap<String, String>> _documentInfos = (ArrayList<HashMap<String, String>>) objMutiFormInfo.get("DOCUMENT_INFOS");		
				            
				           if("SIMPLE".equals(_ubFileLoadType)) 
				           {
				        	   _tmpXmlUbForm = new XmlToUBFormSimple(udmParams);   
				           }
				           else
				           {
				        	   _tmpXmlUbForm = new xmlToUbForm(udmParams);
				           }
				           
				            tXmlToUbForm.setDocumentParams(_docParams);
				            tXmlToUbForm.setDocumentInfos(_documentInfos);
				            tXmlToUbForm.setUseMultiFormType(true);

			    		}
			    		else
			    		{
			    			decXml = common.base64_decode(resContent);
			    			XML_DATA = common.base64_decode_uncompress( decXml, "UTF-8");
			    			
			    			JSONObject objProjectInfo = null;
			    			
			    			if("SIMPLE".equals(_ubFileLoadType)) {
			    				
			    				 _tmpXmlUbForm = new XmlToUBFormSimple(udmParams);   
			    				 
			    				Object ubObj = JSONValue.parseWithException((String)XML_DATA);	
			    				objProjectInfo = (JSONObject)ubObj;
			    				
			    				JSONArray pageE = objProjectInfo != null ? (JSONArray)objProjectInfo.get("pages") : null;
			    				if(pageE != null && pageE.size() > 0)
			    				{
			    					 gPageWidth = ((Long)((JSONObject)pageE.get(0)).get("width")).intValue();
							         gPageHeight = ((Long)((JSONObject)pageE.get(0)).get("height")).intValue();
							    }
			    				else
			    				{
			    					gPageWidth = 0;
			    					gPageHeight = 0;
			    				}
			    			}
			    			else
			    			{
			    				 _tmpXmlUbForm = new xmlToUbForm(udmParams);
			    				 
			    				objProjectInfo = xmlToDocument(XML_DATA);
					           
				    			//log.debug("getFormXmlData() pageWidth=" + objProjectInfo.get("pageWidth") + ", pageHeight=" + objProjectInfo.get("pageHeight"));
					    		_projectType = String.valueOf(objProjectInfo.get("projectType"));
					    		
					            gPageWidth = Integer.valueOf((String)objProjectInfo.get("pageWidth"));
					            gPageHeight = Integer.valueOf((String)objProjectInfo.get("pageHeight"));
			    			}
			    			
			    			log.debug("getFormXmlData() pageWidth=" + gPageWidth + ", pageHeight=" + gPageHeight);
			    		}
			            
			            gXML_DATA = XML_DATA; // 메모리 문제가 발생하면, File로 Write하는 것을 고려해야 함
	    			}
	    			else
	    			{
	    				gXML_DATA = "";
	    			}
	    			
	    			
	    			if("12".equals(_projectType))
	    	        {
	    	        	_result = false;
	    	        	_message = Log.getMessage(Log.MSG_LP_FAIL_PROJECT_TYPE) + " - LINKFORM";
	    	        	//_message = "The following projectType is not supported - LINKFORM.";
	    	        	
	    	        	log.error("getFormXmlData() : FAIL - " + _message);
	    	        }
	    	        else
	    	        {
	    		        _result = true;
	    				//_message = "true".equals(_useJsonFormFile) ? "Get form json data." : "Get form xml data.";
	    				_message = "Get form xml data.";
	    	        }
	    		}
	        }
	        
	        /*
	        if("12".equals(_projectType))
	        {
	        	_result = false;
	        	_message = Log.getMessage(Log.MSG_LP_FAIL_PROJECT_TYPE) + " - LINKFORM";
	        	//_message = "The following projectType is not supported - LINKFORM.";
	        	
	        	log.error("getFormXmlData() : FAIL - " + _message);
	        }
	        else
	        {
		        _result = true;
				//_message = "true".equals(_useJsonFormFile) ? "Get form json data." : "Get form xml data.";
				_message = "Get form xml data.";
	        }
	        */
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
	        _result = false;
			_message = e.getMessage();
		} 
		finally
		{
			log.info("getFormXmlData() result : " + _result + " - " + _message);
			
			if(httppost != null)
			{
				httppost.abort();
			}
			//gHttpclient.getConnectionManager().shutdown();
			/*
			try {
				gHttpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		
		return _result;
	}
	
	private boolean getLocalFormXmlData(String reqCmdName, String serverUrl, UDMParamSet udmParams) throws UnsupportedEncodingException
	{
		boolean _result = true;
		String _message = "";
		
		File sFile = null;
		String filePath = "";
		String TMP_FILE_PATH = "";
		String fileContents = "";
		
		String _serverUrl = serverUrl;
		String _projectName = udmParams.getREQ_INFO().getPROJECT_NAME();
		String _formName = udmParams.getREQ_INFO().getFORM_ID();
		String _fileName = udmParams.getREQ_INFO().getFILE_NAME();
		String _useJsonFormFile = udmParams.getREQ_INFO().getUSE_JSON_FORM_FILE();
		
		log.debug("getLocalFormXmlData() PROJECT_NAME=" + _projectName + ", FORM_NAME=" + _formName + ", _useJsonFormFile=" + _useJsonFormFile);
		
	    try
		{
			TMP_FILE_PATH = "repository/project/" + _projectName + "/" + _formName;
							
			if(_fileName.lastIndexOf(".ubs5") > 0)
				TMP_FILE_PATH = TMP_FILE_PATH + "/" + _fileName;
			else
				TMP_FILE_PATH = TMP_FILE_PATH + "/Mview.ubx";
			
			filePath = Log.basePath + TMP_FILE_PATH;
			log.debug("getLocalFormXmlData() filePath >>>>> " + filePath);

			sFile = new File(filePath);
			if(sFile.isFile()){
				fileContents = common.file_get_contents(sFile);				
				
				log.debug("getLocalFormXmlData() XMLDATA CONTENTS_SIZE=" + fileContents.length());
		            
	            //String decXml = common.base64_decode(fileContents);
	            String XML_DATA = common.base64_decode_uncompress( fileContents, "UTF-8");
	            JSONObject objProjectInfo = xmlToDocument(XML_DATA);
	           
				log.debug("getLocalFormXmlData() pageWidth=" + objProjectInfo.get("pageWidth") + ", pageHeight=" + objProjectInfo.get("pageHeight"));
	            
	            gPageWidth = Integer.valueOf((String)objProjectInfo.get("pageWidth"));
	            gPageHeight = Integer.valueOf((String)objProjectInfo.get("pageHeight"));
	            
	            gXML_DATA = XML_DATA; // 메모리 문제가 발생하면, File로 Write하는 것을 고려해야 함
	        
	            _result = true;
				_message = "Get form xml data.";
			}
			else 
			{
				_result = false;
				_message = "Failed to get form xml data.";
			}
	 	}
		catch(Exception e)
		{
			e.printStackTrace();
			
			_result = false;
			_message = e.getMessage();
		} 
		finally
		{
		}
		
		log.info("getLocalFormXmlData() " + _message);
		
		return _result;
	}
	
	private JSONObject xmlToDocument(String _xml) throws SAXException, IOException, ParserConfigurationException
	{
		// XML 파서의 XML 외부 개체와 DTD 처리를 비활성화 합니다.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        
		InputSource _is = new InputSource(new StringReader(_xml));
		//mDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
		Document mDocument = factory.newDocumentBuilder().parse(_is);
		
		
		// xml Project
		NodeList _projectList = mDocument.getElementsByTagName("project");
		Element _project = (Element) _projectList.item(0);
		
		JSONObject projectHm = new JSONObject();
		
		ItemPropertyProcess mPropertyFn = new ItemPropertyProcess();
		projectHm = mPropertyFn.getAttrObject(_project.getAttributes());

		Log.pageFontUnit = projectHm.get("fontUnit") != null ? projectHm.get("fontUnit").toString() : "px";
		
		if( projectHm.containsKey("clientEditMode")){
			projectHm.put("clientEditMode", projectHm.get("clientEditMode").toString().toUpperCase());
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
		
		//mHashMap.put("project", hashmapToJsonStr(projectHm));
		return projectHm;
		
	}

	private JSONObject getPageData(String _pageItem, String _pageNo) throws ParseException, IOException, DataFormatException
	{
		JSONObject objPageData = null;
	
		//if(properties != null && properties.containsKey("page" + _pageNo))
    	{
    		//String pageItem = (String) properties.get("page" + _pageNo);
    		String pageItem = _pageItem;
    		
    		byte[] b1 = Base64Coder.decodeLines(pageItem);
    		byte[] inflate = common.decompress(b1);
    		String resdata = new String(inflate, "utf-8");
    		
    		JSONObject objPageObjects = (JSONObject) JSONValue.parseWithException((String)resdata);	
    		JSONObject objPageInfo = (JSONObject) objPageObjects.get("pageInfo");
    		objPageData = (JSONObject) objPageInfo.get("" + (Integer.valueOf(_pageNo)-1));
    	}
		
		return objPageData;
	}
	
	private long getTotalPageCount(String _pageItem) throws ParseException, IOException, DataFormatException
	{
		long nTotalPageCount = 0;
	
		try
    	{
    		String pageItem = _pageItem;
    		
    		byte[] b1 = Base64Coder.decodeLines(pageItem);
    		byte[] inflate = common.decompress(b1);
    		String resdata = new String(inflate, "utf-8");
    		
    		JSONObject objPageObjects = (JSONObject) JSONValue.parseWithException((String)resdata);	
    		nTotalPageCount = Long.parseLong(objPageObjects.get("TOTAL_PAGE").toString());
    	} 
		catch(Exception e) {
    		e.printStackTrace();
    	}     	
		
		return nTotalPageCount;
	}
	
	private String printPreview(JSONObject rq, HashMap hmInService, WebSocket conn , HashMap<String, Object> _param) throws IOException
	{
		boolean _isPrintCancel = false;
		boolean _isDirectPrint = false;
		
		String _type = "SUCCESS";
		String _command = (String) rq.get("COMMAND");
		String _printer = (String) rq.get("PRINTER");
		String _code = "00000";
		String _message = "";		
		String _totalPage = "";
		String _pageRangeGubun = "";
		String _startPage = "";
		String _endPage = "";
		
		String _marginInfo = "";
		if(rq.containsKey("MARGIN")){
			_marginInfo =  rq.get("MARGIN") == null ? "" :  (String)rq.get("MARGIN");
		}
		
		String _landScape = "";
		if(rq.containsKey("LANDSCAPE")){
			_landScape =  rq.get("LANDSCAPE") == null ? "" :  (String)rq.get("LANDSCAPE");
		}
		
		String _pageFit = "";
		if(rq.containsKey("PAGE_FIT")){
			_pageFit =  rq.get("PAGE_FIT") == null ? "" :  (String)rq.get("PAGE_FIT");
		}
		
		String _info_dir = "";
		if(rq.containsKey("INFO_DIR")){
			_info_dir =  rq.get("INFO_DIR") == null ? "" :  (String)rq.get("INFO_DIR");
		}
		
		String _exportType = "";
		if(rq.containsKey("EXPORT_TYPE")){
			_exportType =  rq.get("EXPORT_TYPE") == null ? "LOCAL_PREVIEW" :  (String)rq.get("EXPORT_TYPE");
		}
		
		
		if(rq.containsKey("TOTAL_PAGE")){
			_totalPage =  rq.get("TOTAL_PAGE") == null ? "" :  (String)rq.get("TOTAL_PAGE");
		}
		
		if(rq.containsKey("PAGE_RANGE_GUBUN")){
			_pageRangeGubun =  rq.get("PAGE_RANGE_GUBUN") == null ? "" :  (String)rq.get("PAGE_RANGE_GUBUN");
		}
		
		if(rq.containsKey("START_PAGE")){
			_startPage =  rq.get("START_PAGE") == null ? "" :  (String)rq.get("START_PAGE");
		}
		
		if(rq.containsKey("END_PAGE")){
			_endPage =  rq.get("END_PAGE") == null ? "" :  (String)rq.get("END_PAGE");
		}
		
		String _pageType = "";
		if(rq.containsKey("PAGE_TYPE")){
			_pageType =  rq.get("PAGE_TYPE") == null ? "" :  (String)rq.get("PAGE_TYPE");
		}
		
		log.info("printPreview() COMMAND=" + _command + ", PRINTER_NAME=" + _printer + ", info_dir=" + _info_dir + ",export_type=" + _exportType);
		
		if(hmInService.containsKey("RESULT"))
			hmInService.remove("RESULT");
		
		try 
		{
	        if("DIRECT_PRT".endsWith(_command))
	        	_isDirectPrint = true;
	        else
	        	_isDirectPrint = false;			        
	        
			// C# preview 모듈을 호출한다.
			JSONObject oParams = new JSONObject();
			String filePath = _info_dir + "\\tmpFormFile" + ".txt";	
			oParams.put("INFO_FILE_DIR", _info_dir);
			oParams.put("IS_DIRECT_PRINT", _isDirectPrint);
			oParams.put("PRINTER_NAME", _printer);
			oParams.put("TOTAL_PAGE", _totalPage);
			oParams.put("PAGE_RANGE_GUBUN", _pageRangeGubun);
			oParams.put("START_PAGE", _startPage);
			oParams.put("END_PAGE", _endPage);
			oParams.put("MARGIN", _marginInfo);
			oParams.put("LANDSCAPE", _landScape);
			oParams.put("PAGE_FIT", _pageFit);
			oParams.put("PAGE_TYPE", _pageType);
			
			
			if(_isDirectPrint==false)
				Log.previewClose = false;
			
	        this.mUdpClient.sendMessage(JSONConverter("SUCCESS", _exportType, Log.MSG_LP_PRINT_PREVIEW, oParams.toJSONString(), ""));
	        
	        while(true)
	        {	        	
	        	if(!Log.previewPrintStop && (Log.printCompleted || Log.printStop || Log.printCancel))
	        	{
	        		log.info("printPreview() Log.printCompleted || Log.printStop || Log.printCancel!!!");
	        		Log.printCompleted = false;
	        		
	        		if(Log.printCancel || Log.printStop)
	        		{
	        			Log.printStop = false;
        		        Log.printCancel = false;        		       
	        			_isPrintCancel = true;
	        		}	        		
	        		Log.previewFlag = false;
     	        	Log.currPageIdx = 0;
	        		break;
	        		
	        	}else if(Log.previewPrintStop){
	        		//미리보기 상태에서 인쇄 하다가 인쇄 취소한 경우
	        		Log.printCompleted = false;
	        		if(Log.printCancel || Log.printStop)
	        		{
	        			Log.printStop = false;
        		        Log.printCancel = false; 	        		
        		    }	        		
	        		Log.previewFlag = false;
     	        	Log.currPageIdx = 0;	        		
	        		Log.previewPrintStop = false;
	        		_type = "FAIL";
					//_message = "Print End";
					_message = Log.getMessage(Log.MSG_LP_PRINTJOB_CANCEL);
					_code = Log.MSG_LP_PRINTJOB_CANCEL;
					//String body =  JSONConverter(_type, "PRINTEND", _code, _message);	        		
					this.mUdpClient.sendMessage(JSONConverter(_type, "PRINTEND", Log.MSG_LP_PRINTJOB_CANCEL, _message, ""));					
					//this.SendMessage(conn, body);
					
	        	}else if(Log.previewPrintCompleated){
	        		
	        		//미리보기 상태에서 인쇄 완료인 경우 
	        		Log.previewPrintCompleated = false;
	        		_type = "SUCCESS";
					//_message = "Print End";
					_message = Log.getMessage(Log.MSG_LP_PRINTJOB_COMPLETED);
					_code = Log.MSG_LP_PRINTJOB_COMPLETED;
					String body =  JSONConverter(_type, "PRINTEND", _code, _message, "");	        		
	        		this.SendMessage(conn, body, true);	  
	        		Log.currPageIdx = 0;	
	        	}else if(Log.previewFlag){  //미리보기 데이타 가져오기	
	        		
	        		formfile = new FORMFile(filePath);		
	        		_xmlUbForm._formExportManagerOne(formfile);
					formfile.close();
					Log.previewFlag = false;
					this.mUdpClient.sendMessage(JSONConverter("SUCCESS", _exportType, Log.MSG_LP_PRINT_PREVIEW, oParams.toJSONString(), ""));
					Log.currPageIdx = 0;	
					
	        	}else if(Log.execPrintFlag){//인쇄 대상 데이타 가져오기  	        		
	        		formfile = new FORMFile(filePath);
	        		_xmlUbForm._formExportManagerPageRange(formfile , Log.execPrintPage);	        							
					formfile.close();
					Log.execPrintFlag = false;
					if(!Log.printStop){
						this.mUdpClient.sendMessage(JSONConverter("SUCCESS", "PRINT_START", Log.MSG_LP_START_PRINT, Log.getMessage(Log.MSG_LP_START_PRINT), ""));					
					}	
					Log.currPageIdx = 0;	
					
		        }else if(Log.pdfDownFlag){//인쇄 대상 데이타 가져오기  
	        		previewToPdfDown(rq,_param, conn);
	        		Log.pdfDownFlag = false;
	        		Log.currPageIdx = 0;	
		        }else if(Log.excelDownFlag){//인쇄 대상 데이타 가져오기  
	        		previewToExcelDown(rq,_param, conn);
	        		Log.excelDownFlag = false;
	        		Log.currPageIdx = 0;	
		        }else if(Log.previewClose){//인쇄 대상 데이타 가져오기  	
		        
		        	log.info("printPreview() Log.previewClose~~~~~~~~~~~~~~~~~~~~~~~~~~~!!!");
		        	
		        	_isPrintCancel = false;
		        	Log.currPageIdx = 0;			        
		        	break;
				}
//		        else if(Log.previewCancel){//인쇄 대상 데이타 가져오기  	
//		        
//		        	log.info("printPreview() Log.previewCancel~~~~~~~~~~~~~~~~~~~~~~~~~~~!!!");
//		        	
//		        	_isPrintCancel = false;
//		        	Log.currPageIdx = 0;			        
//		        	break;
//				} 
	        	Thread.sleep(100);
	        }
	        
	        formfile.clear();				
	                	
        	if(_isPrintCancel)
			{
				_type = "FAIL";
				//_message = "Print Cancel";
				_message = Log.getMessage(Log.MSG_LP_PRINTJOB_CANCEL);
				_code = Log.MSG_LP_PRINTJOB_CANCEL;
				
				log.info("printPreview() " + _message);
			}
			else
			{
				if(	Log.previewClose){
					Log.previewClose = false;
					_type = "SUCCESS";					
					_message = Log.getMessage(Log.MSG_LP_PREVIEWJOB_COMPLETE);
					_code = Log.MSG_LP_PREVIEWJOB_COMPLETE;
//				}else if(Log.previewCancel){
//					Log.previewCancel = false;
//					_type = "SUCCESS";					
//					_message = Log.getMessage(Log.MSG_LP_PREVIEWJOB_CANCEL);
//					_code = Log.MSG_LP_PREVIEWJOB_CANCEL;	
				}else{
						_type = "SUCCESS";					
						_message = Log.getMessage(Log.MSG_LP_PRINTJOB_COMPLETED);
						_code = Log.MSG_LP_PRINTJOB_COMPLETED;
				}
				log.info("printPreview() " + _message);
			}
         	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			_type = "FAIL";
			_code = Log.MSG_SYSTEM_EXCEPTION;
			
			_message = Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + e.getMessage();
		} 
		finally 
		{

		}
					
		return JSONConverter(_type, "PRINTEND", _code, _message, "");
	}
	
	private String printPdfFile(JSONObject rq, HashMap hmInService) throws IOException
	{
		boolean _isPrintCancel = false;
		String _type = "SUCCESS";
		String _command = (String) rq.get("COMMAND");
		String _printer = (String) rq.get("PRINTER");
		String _code = "00000";
		String _message = "";	
		String _marginInfo = "";
		if(rq.containsKey("MARGIN")){
			_marginInfo =  rq.get("MARGIN") == null ? "" :  (String)rq.get("MARGIN");
		}	
		
		log.info("printPdfFile() COMMAND=" + _command + ", PRINTER_NAME=" + _printer);
		
		if(hmInService.containsKey("RESULT"))
			hmInService.remove("RESULT");
		
		try 
		{
			DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
			
			PrintService myPrinter = null;
			PrintService[] pservices = PrintServiceLookup.lookupPrintServices(psInFormat, aset);
		    if (pservices.length > 0) {
		        for (PrintService ps : pservices) {
		            //System.out.println("Service found:: " + ps.getName());

		            if (!"".equals(_printer) && ps.getName().contains(_printer)) {
		        		log.debug("printPdfFile() My printer service found: "+_printer);
		        		myPrinter = ps;
		                break;
		            }
		        }
		    }
		    
		    if(myPrinter == null)
	        {
	        	if(pservices.length > 0)
	        	{
	        		log.debug("printPdfFile() Your request Printer is not found(" + _printer + ") and so your printer is setted by default printer.");
		        	//myPrinter = pservices[0];
	        		myPrinter = PrintServiceLookup.lookupDefaultPrintService();
	        	}
	        	else
	        	{
		        	_type = "FAIL";
					_message = Log.getMessage(Log.MSG_LP_NOTFOUND_PRINTSERVICE) + _printer;
					
	        		log.error(_message);
		        	return JSONConverter(_type, "PRINT", Log.MSG_LP_NOTFOUND_PRINTSERVICE, _message, "");
	        	}
	        }
		    			    
		    if (myPrinter != null) 
		    {
		    	PrinterJob pjob = PrinterJob.getPrinterJob();
		    	pjob.setPrintService(myPrinter);

		        PageFormat pf = PrinterJob.getPrinterJob().defaultPage();
		        Paper paper = new Paper();
		        
		        if(Log.pdfExportPrinterType != null && Log.pdfExportPrinterType.length()>0){
		        	//labelPrinter
		        	paper.setSize(PxlToMm(gPageWidth) * ppmm, PxlToMm(gPageHeight) * ppmm);
		        }else{
			        if(gPageWidth > gPageHeight )
			        {
			        	paper.setSize(PxlToMm(gPageHeight) * ppmm, PxlToMm(gPageWidth) * ppmm);
			        	pf.setOrientation(PageFormat.LANDSCAPE);
			        }
			        else
			        {
			        	paper.setSize(PxlToMm(gPageWidth) * ppmm, PxlToMm(gPageHeight) * ppmm);
			        	pf.setOrientation(PageFormat.PORTRAIT);
			        }	
		        }
		        
		        PageFormat minPf = getMinimumMarginPageFormat(pjob);    
		        
		        double marginX = minPf.getImageableX();
		        double marginY = minPf.getImageableY();		        
		        if(!_marginInfo.equals("")){
		        	JSONObject marginInfo = (JSONObject) JSONValue.parse(_marginInfo);
		        	if(marginInfo != null){
		        		marginX = (float) (Float.parseFloat(marginInfo.get("marginX").toString())*ppmm);
			        	marginY = (float) (Float.parseFloat(marginInfo.get("marginY").toString())*ppmm);
		        	}
		        }	     
		        
		        if(Log.pdfExportPrinterType != null && Log.pdfExportPrinterType.length()>0){//labelPrinter
		        	 paper.setImageableArea(marginX, marginY, paper.getWidth(), paper.getHeight());
		        }else{
		        	 paper.setImageableArea(marginX, marginY, paper.getWidth() - (marginX * 2), paper.getHeight() - (marginY * 2));
		        }
		        
		        log.debug("printPdfFile() =================>PaperSize.Width=" + paper.getWidth() + ", PaperSize.Height=" + paper.getHeight());
		        
		        pf.setPaper(paper);
		        
		        PrintableImpl pages = new PrintableImpl(mUdpClient, pjob, formfile, pf);

		        Book book = new Book();
		        book.append(pages, pf, formfile.getNumPages());
		        pjob.setPageable(book);

		        if("DIRECT_PRT".endsWith(_command))
		        	_isPrintCancel = pages.print(false);
		        else
		        	_isPrintCancel = pages.print(true);			        
		        
		        formfile.clear();	
		    }
			
			if(_isPrintCancel)
			{
				_type = "FAIL";
				//_message = "Print Cancel";
				_message = Log.getMessage(Log.MSG_LP_PRINTJOB_CANCEL);
				_code = Log.MSG_LP_PRINTJOB_CANCEL;
				
				log.info("printPdfFile() " + _message);
			}
			else
			{
				_type = "SUCCESS";
				//_message = "Print End";
				_message = Log.getMessage(Log.MSG_LP_PRINTJOB_COMPLETED);
				_code = Log.MSG_LP_PRINTJOB_COMPLETED;
				
				log.info("printPdfFile() " + _message);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			_type = "FAIL";
			_code = Log.MSG_SYSTEM_EXCEPTION;
			
			_message = Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + e.getMessage();
		} 
		finally 
		{

		}
					
		return JSONConverter(_type, "PRINTEND", _code, _message, "");
	}
	
	private PageFormat getMinimumMarginPageFormat(PrinterJob printJob) {
	    PageFormat pf0 = printJob.defaultPage();
	    PageFormat pf1 = (PageFormat) pf0.clone();
	    Paper p = pf0.getPaper();
	    p.setImageableArea(0, 0,pf0.getWidth(), pf0.getHeight());
	    pf1.setPaper(p);
	    PageFormat pf2 = printJob.validatePage(pf1);
	    return pf2;     
	}
	
	
	private float PxlToMm(double pxl) {            
        return (float) ((pxl / 96) * 25.4);        
    }
	
	private int getNumberOfPages(PageRanges pageRanges) {
	    int pages = 0;
	    int[][] ranges = pageRanges.getMembers();
	    for (int i = 0; i < ranges.length; i++) {
	        pages += 1;
	        if (ranges[i].length == 2) {
	            pages += ranges[i][1] - ranges[i][0];
	        }
	    }
	    //pages = Math.min(pages, totalPagesOfDocument);
	    return pages;
	}
	
	
	/**
     * Merge multiple pdf into one pdf
     * 
     * @param list
     *            of pdf input file Names
     * @throws IOException
     * @throws DocumentException
     */
	public String mergePdfFiles( ArrayList<String> _fileNames ) throws IOException, DocumentException
	{
		String _resultFileName = "";
		
		// 합쳐진 결과 pdf파일명 
		String _userHome = System.getProperty("user.home");
		_resultFileName = _userHome + "/Downloads/ubformLocal_All.pdf";	
		FileOutputStream _fos = null;
		
		try
		{
			_fos = new FileOutputStream( _resultFileName );	
		}
		catch(FileNotFoundException exp)
		{
			int dRandom = (int) getCountUbLocalPdfFiles(_resultFileName);			
			_resultFileName = _resultFileName.substring(0, _resultFileName.lastIndexOf(".pdf")) + "_" + dRandom + ".pdf";
			
			_fos = new FileOutputStream(_resultFileName);
		}
		
		com.lowagie.text.Document document = new com.lowagie.text.Document();
 		PdfCopy cp = new PdfCopy(document, _fos);
 		document.open();
        
        int pageNo = 0;
        for (String inPath : _fileNames) 
        {
        	FileInputStream ips = new FileInputStream(inPath);
        	PdfReader r = new PdfReader(ips);
        	
            for (int k = 1; k <= r.getNumberOfPages(); ++k) {
                cp.addPage(cp.getImportedPage(r, k));
            } 
            
            cp.freeReader(r);
            r.close();
            ips.close();

			log.debug("mergePdfFiles() doMerge()::document.PageNumber=" + (pageNo++));
        }
        
        cp.close();
        document.close();
        _fos.close();
		
		
		if(_fileNames.size() >0){
			
			for (int i = 0; i < _fileNames.size(); i++) {
				
				File file = new File(_fileNames.get(i));
				
				file.delete();
			}
		}
		
		return _resultFileName;
	}
	
	
	private int getCountUbLocalPdfFiles(String filePath)
	{
		int count = 0;
		
		File[] files = null;
		File f = new File( filePath );
		if(f.isDirectory())
		{
			files = f.listFiles();
		}
		else
		{
			files = new File(f.getParent()).listFiles();
		}

		// files
		String _curFileName = "";
		for (int i = 0; i < files.length; i++) 
		{
			if ( files[i].isFile() ) {
				
				_curFileName = files[i].getName();
				if(_curFileName.contains("ubformLocal_All") && _curFileName.lastIndexOf(".pdf") > 0)
				{
					log.debug("getCountUbLocalPdfFiles() fileName : " + files[i].getName() );
					count++;
				}
			} else {
				log.debug("getCountUbLocalPdfFiles() directoryName : " + files[i].getName() );
			}
		} // end of for
		
		return count;
	}
	
	
	private void previewToPdfDown(JSONObject reqParam, HashMap<String, Object> _param,  WebSocket conn){
		
		String _userHome = System.getProperty("user.home");
		String _exportFolder = _userHome + "/Downloads/ubformLocal/" + Log.wsPort + "/";
		
		try {
			
			String _pdfExportFilePath = reqParam.get("EXPORT_PDF_FILE_PATH")!= null? reqParam.get("EXPORT_PDF_FILE_PATH").toString():"";
			String _pdfExportFileName = reqParam.get("EXPORT_PDF_FILE_NAME")!= null? reqParam.get("EXPORT_PDF_FILE_NAME").toString():"";
			
			resetAppParam(_xmlUbForm.getAppParam());
			
			String _resultFileName = _xmlUbForm.pdfExportManager_pages(gXML_DATA, _param, _exportFolder, Log.pdfExportDivCount);
			
    		if(_resultFileName.startsWith("FAIL")){			
    			this.mUdpClient.sendMessage(JSONConverter("FAIL","MAKEPDF", Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + _resultFileName, ""));
    			//this.SendMessage(conn, JSONConverter("FAIL", "MAKEPDF", Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + _resultFileName));	
    			return;
			}else if(_resultFileName.startsWith("CANCEL")){
				//저장 된 파일이 있다면 삭제
				this.mUdpClient.sendMessage(JSONConverter("CANCEL", "PDFCANCEL", Log.MSG_LP_MAKEPDF_CANCELED, Log.getMessage(Log.MSG_LP_MAKEPDF_CANCELED), ""));
				//this.SendMessage(conn, JSONConverter("CANCEL", "PDFCANCEL", Log.MSG_LP_MAKEPDF_CANCELED, Log.getMessage(Log.MSG_LP_MAKEPDF_CANCELED)));				
				return;
			}
    		
			File pdfFile = new File(_resultFileName);			
	    	if (Desktop.isDesktopSupported()) 
	    	{
	    		String _message = ""; 
				File fileToSave = null;
				String _pdfExportFileFullPath = ""; 
				
	    		log.debug("onMessage() Desktop.isDesktopSupported()!!~~~_pdfExportFilePath=" + _pdfExportFilePath);

	    		if(_pdfExportFileName != null && _pdfExportFilePath != null 
	    				&& _pdfExportFileName.length() > 0 && _pdfExportFilePath.length() > 0)
	    		{
	    			//디렉토리 생성
	    			File _directory = new File( _pdfExportFilePath );
	    			if(!_directory.exists()){
	    				_directory.mkdirs(); 
	    			}
	    			
					_pdfExportFileFullPath = _pdfExportFilePath.endsWith("\\") ? _pdfExportFilePath + _pdfExportFileName : _pdfExportFilePath + "\\" + _pdfExportFileName;

	    			if(!(_pdfExportFileName.endsWith(".pdf") || _pdfExportFileName.endsWith(".PDF")))
					{
					   	fileToSave = new File(_pdfExportFileFullPath + ".pdf");
					}
	    			else
	    			{
	    				fileToSave = new File(_pdfExportFileFullPath);
	    			}
	    			 
	    			if(fileMove(pdfFile, fileToSave)==true)
				    {
				    	Desktop.getDesktop().open(fileToSave);
				    	//Desktop.getDesktop().print(fileToSave);
				    }
				    
				    _message = Log.getMessage(Log.MSG_LP_END_MERGEPDF);
				    log.info("onMessage() SUCCESS - " + _message);
				    
				    this.SendMessage(conn, JSONConverter("SUCCESS", "MERGEPDF_END", Log.MSG_LP_END_MERGEPDF, _message, fileToSave.getAbsolutePath()), false);	
				   //this.SendMessage(conn, JSONConverter("SUCCESS", "PDFEND", Log.MSG_LP_MAKEPDF_COMPLETED, _message));

	    		}	    		
	    		else
	    		{
					// parent component of the dialog
					JFrame parentFrame = new JFrame();
					parentFrame.toFront();
//										JFileChooser fileChooser = new JFileChooser();
//										JFileChooser fileChooser = new JFileChooser() {
					if(gFileChooser==null) 
						gFileChooser = new JFileChooser() {
							@Override
						    protected JDialog createDialog(Component parent) throws HeadlessException {
						       // intercept the dialog created by JFileChooser
						       JDialog dialog = super.createDialog(parent);
						       dialog.setModal(true);  	// set modality (or setModalityType)
						       
						       if( dialog.isAlwaysOnTopSupported() ) dialog.setAlwaysOnTop(true);
						       dialog.toFront();
						       dialog.repaint();
						       
						       return dialog;
						   }
					};
					
					gFileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF File","pdf"));
					gFileChooser.setDialogTitle("Specify a file to save");   
					if( _pdfExportFileName != null && _pdfExportFileName.length() > 0  ){
						gFileChooser.setSelectedFile(new File(gFileChooser.getCurrentDirectory() + "\\" + _pdfExportFileName));
					}else{
						gFileChooser.setSelectedFile(new File(gFileChooser.getCurrentDirectory() + "\\" + pdfFile.getName()));
					}
					
				
					int userSelection = gFileChooser.showSaveDialog(parentFrame);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
					    fileToSave = gFileChooser.getSelectedFile();
					    String saveFilePath = fileToSave.getAbsolutePath();
					    log.debug("onMessage() Save as file: " + saveFilePath);
					    
					    if(!(saveFilePath.endsWith(".pdf") || saveFilePath.endsWith(".PDF")))
					    {
					    	fileToSave = new File(saveFilePath + ".pdf");
					    	gFileChooser.setCurrentDirectory(fileToSave);
					    }
					    
					    if(fileMove(pdfFile, fileToSave)==true)
					    {
					    	Desktop.getDesktop().open(fileToSave);
					    	//Desktop.getDesktop().print(fileToSave);
					    }
					    
					    _message = Log.getMessage(Log.MSG_LP_END_MERGEPDF);
					    log.info("onMessage() SUCCESS - " + _message);
					    
					    this.SendMessage(conn, JSONConverter("SUCCESS", "MERGEPDF_END", Log.MSG_LP_END_MERGEPDF, _message, fileToSave.getAbsolutePath()), false);	

			
					//} else if (userSelection == JFileChooser.CANCEL_OPTION) {
					} else {	
					    log.debug("onMessage() delete file: " + pdfFile.getAbsolutePath());
						pdfFile.delete();
						
						 _message = Log.getMessage(Log.MSG_LP_MAKEPDF_CANCELED);
					    log.info("onMessage() FAIL - " + _message);
					    
					    this.SendMessage(conn, JSONConverter("FAIL", "PDFCANCEL", Log.MSG_LP_MAKEPDF_CANCELED, _message, ""), true);
					    
					}
					
				    //log.info("onMessage() SUCCESS - MakePdf End.");
					//conn.send(JSONConverter("SUCCESS", "PDFEND", "MakePdf End"));
				}
	    	}
		} catch (Exception e) {
			// TODO: handle exception
		}		
	}
	
	private void previewToExcelDown(JSONObject param, HashMap<String, Object> udmParams,  WebSocket conn){
		String [] excelOption = Log.excelDownOption.split("\\|");//fileFormat | exportType | startPage | endPage
		Log.excelDownOption = "";
		
		String _userHome = System.getProperty("user.home");
		String _exportFolder = _userHome + "/Downloads/ubformLocal/" + Log.wsPort + "/";		
		
		String _exportFilePath = param.get("EXPORT_FILE_PATH")!= null? param.get("EXPORT_FILE_PATH").toString():"";
		String _exportFileName = param.get("EXPORT_FILE_NAME")!= null? param.get("EXPORT_FILE_NAME").toString():"";
		
		//엑셀 옵션 기본값 
		String fmtOption = "EXCEL2007";
		String excelOptiion = "NORMAL";
		String startPage = "0";
		String endPage = "-1";
		String pageGubun = "0";
		
		if(excelOption.length>0 && excelOption[0].length()>0){
			fmtOption = excelOption[0];
		}
		if(excelOption.length>1 && excelOption[1].length()>0){
			excelOptiion = excelOption[1];
		}
		if(excelOption.length>2 && excelOption[2].length()>0){
			startPage = excelOption[2];
		}		
		if(excelOption.length>3 && excelOption[3].length()>0){
			endPage = excelOption[3];
		}
		if(excelOption.length>4 && excelOption[4].length()>0){
			pageGubun = excelOption[4];
		}
		udmParams.put("EXCEL_FMT_OPTION", fmtOption);
		udmParams.put("EXCEL_OPTION", excelOptiion );
		udmParams.put("START_PAGE", startPage );
		udmParams.put("END_PAG ", endPage);		
		udmParams.put("EXCEL_SHEET_SPLIT_TYPE", "normal");
		udmParams.put("EXCEL_SHEET_NAMES", "");
		
		UDMParamSet _appParam = _xmlUbForm.getAppParam();
		
		if( _appParam != null )
		{
			// 0 : 전체 페이지 , 1: 현재 페이지, 2: 페이지 범위 페이지   값지정이 필요
			_appParam.getREQ_INFO().setPAGE_RANGE_GUBUN(pageGubun);
			
			_appParam.getREQ_INFO().setEXCEL_OPTION(excelOptiion);
			_appParam.getREQ_INFO().setEXCEL_FMT_OPTION(fmtOption);
			_appParam.getREQ_INFO().setSTART_PAGE(startPage);
			_appParam.getREQ_INFO().setEND_PAGE(endPage);
			_appParam.getREQ_INFO().setEXCEL_SHEET_SPLIT_TYPE("normal");
			_appParam.getREQ_INFO().setEXCEL_SHEET_NAMES("");
		}
		
		// Call Excel Export Manager
		Workbook resultWB = null;
					 						
		int _randomInt = (int) (Math.random()*1000) + 1;
		String fname = param.get("PROJECT_NAME") + "_" + param.get("FORM_ID") + "_" + new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date()) +"_"+ _randomInt;
					 						
		String PATH = _exportFolder + "/" + fname + ".xlsx";
		
		
		try {
			resultWB = _xmlUbForm.excelExportManager(gXML_DATA, udmParams, PATH , null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( resultWB == null){
			resetAppParam(_appParam);
			_xmlUbForm.clear();
			_xmlUbForm = null;

		
			this.SendMessage(conn, JSONConverter("FAIL", "MAKEEXCEL", Log.MSG_SYSTEM_EXCEPTION, Log.getMessage(Log.MSG_SYSTEM_EXCEPTION) + "-" + fname, PATH), true);										
			return;
		}
		
		FileOutputStream out;
		try {
			out = new FileOutputStream(PATH);
			resultWB.write(out);
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		
		if(resultWB instanceof SXSSFWorkbook)
		{
			// dispose of temporary files backing this workbook on disk
			((SXSSFWorkbook) resultWB).dispose();
		}
		
		String _resultFileName = PATH;
		
		this.SendMessage(conn, JSONConverter("SUCCESS", "CREATEEXCEL_END", Log.MSG_LP_END_CREATE_EXCEL, Log.getMessage(Log.MSG_LP_END_CREATE_EXCEL), _resultFileName), false);
		
		resetAppParam(_appParam);
		
		File excelFile = new File(_resultFileName);			
    	if (Desktop.isDesktopSupported()) 
    	{
    		String _message = ""; 
			File fileToSave = null;
			String _excelExportFileFullPath = ""; 
			
    		log.debug("onMessage() Desktop.isDesktopSupported()!!~~~_excelExportFilePath=" + _exportFilePath);

    		if(_exportFileName != null && _exportFilePath != null 
    				&& _exportFileName.length() > 0 && _exportFilePath.length() > 0)
    		{
    			//디렉토리 생성
    			File _directory = new File( _exportFilePath );
    			if(!_directory.exists()){
    				_directory.mkdirs(); 
    			}
    			
				_excelExportFileFullPath = _exportFilePath.endsWith("\\") ? _exportFilePath + _exportFileName : _exportFilePath + File.separator + _exportFileName;

    			if(!(_exportFileName.endsWith(".xlsx") || _exportFileName.endsWith(".XLSX")))
				{
				   	fileToSave = new File(_excelExportFileFullPath + ".xlsx");
				}
    			else
    			{
    				fileToSave = new File(_excelExportFileFullPath);
    			}
    			 
    			if(fileMove(excelFile, fileToSave)==true)
			    {
			    	try {
						Desktop.getDesktop().open(fileToSave);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	//Desktop.getDesktop().print(fileToSave);
			    }
			    
			    _message = Log.getMessage(Log.MSG_LP_MAKEEXCEL_COMPLETED);
			    log.info("onMessage() SUCCESS - " + _message);
			    
			   this.SendMessage(conn, JSONConverter("SUCCESS", "EXCELEND", Log.MSG_LP_MAKEEXCEL_COMPLETED, _message, fileToSave.getAbsolutePath()), true);

    		}
    		else
    		{
				// parent component of the dialog
				JFrame parentFrame = new JFrame();
				parentFrame.toFront();
//									JFileChooser fileChooser = new JFileChooser();
//									JFileChooser fileChooser = new JFileChooser() {
				try {
					if(gFileChooser==null) 
						gFileChooser = new JFileChooser() {
							@Override
						    protected JDialog createDialog(Component parent) throws HeadlessException {
						       // intercept the dialog created by JFileChooser
						       JDialog dialog = super.createDialog(parent);
						       dialog.setModal(true);  	// set modality (or setModalityType)
						       
						       if( dialog.isAlwaysOnTopSupported() ) dialog.setAlwaysOnTop(true);
						       dialog.toFront();
						       dialog.repaint();
						       
						       return dialog;
						   }
					};
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				gFileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("EXCEL File","xlsx"));
				gFileChooser.setDialogTitle("Specify a file to save");   
				gFileChooser.setSelectedFile(new File(gFileChooser.getCurrentDirectory() + File.separator + excelFile.getName()));
			
				int userSelection = gFileChooser.showSaveDialog(parentFrame);
				if (userSelection == JFileChooser.APPROVE_OPTION) {
				    fileToSave = gFileChooser.getSelectedFile();
				    String saveFilePath = fileToSave.getAbsolutePath();
				    log.debug("onMessage() Save as file: " + saveFilePath);
				    
				    if(!(saveFilePath.endsWith(".xlsx") || saveFilePath.endsWith(".XLSX")))
				    {
				    	fileToSave = new File(saveFilePath + ".xlsx");
				    	gFileChooser.setCurrentDirectory(fileToSave);
				    }
				    
				    if(fileMove(excelFile, fileToSave)==true)
				    {
				    	try {
							Desktop.getDesktop().open(fileToSave);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	//Desktop.getDesktop().print(fileToSave);
				    }
				    
				    _message = Log.getMessage(Log.MSG_LP_MAKEEXCEL_COMPLETED);
				    log.info("onMessage() SUCCESS - " + _message);
				    
				   this.SendMessage(conn, JSONConverter("SUCCESS", "EXCELEND", Log.MSG_LP_MAKEEXCEL_COMPLETED, _message, fileToSave.getAbsolutePath()), true);

			
				//} else if (userSelection == JFileChooser.CANCEL_OPTION) {
				} else {	
				    log.debug("onMessage() delete file: " + excelFile.getAbsolutePath());
				    excelFile.delete();
					
					 _message = Log.getMessage(Log.MSG_LP_MAKEPDF_CANCELED);
				    log.info("onMessage() FAIL - " + _message);
				    
				    this.SendMessage(conn, JSONConverter("FAIL", "EXCELCANCEL", Log.MSG_LP_MAKEEXCEL_CANCELED, _message, ""), true);
				}
				
			    //log.info("onMessage() SUCCESS - MakePdf End.");
				//conn.send(JSONConverter("SUCCESS", "PDFEND", "MakePdf End"));
			}
    	}
		
	}
	
	
	//파일을 삭제하는 메소드
	private void fileDelete(String deleteFileName) {
		File I = new File(deleteFileName);
		I.delete();
	}
		
	//파일을 이동하는 메소드
	private boolean fileMove(File inFile, File outFile) {
	  
		boolean isSuccess = true;
		String inFileName = inFile.getAbsolutePath();
		String outFileName = outFile.getAbsolutePath();
		
		try 
		{
			if(inFileName.equals(outFileName))
				return isSuccess;
	
			Path movefrom = FileSystems.getDefault().getPath(inFileName);
	        Path target = FileSystems.getDefault().getPath(outFileName);
	  	
			Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
			
		} catch (IOException e) {
		  	// TODO Auto-generated catch block
		  	e.printStackTrace();
		  	isSuccess = false;
	  	}
		
		return isSuccess;
   }
	
	private void resetAppParam( UDMParamSet _appParam )
	{
		
		if( _appParam != null )
		{
			// 0 : 전체 페이지 , 1: 현재 페이지, 2: 페이지 범위 페이지   값지정이 필요
			_appParam.getREQ_INFO().setPAGE_RANGE_GUBUN("0");
			_appParam.getREQ_INFO().setEXCEL_OPTION("");
			_appParam.getREQ_INFO().setEXCEL_FMT_OPTION("");
			_appParam.getREQ_INFO().setSTART_PAGE("-1");
			_appParam.getREQ_INFO().setEND_PAGE("-1");
			_appParam.getREQ_INFO().setEXCEL_SHEET_SPLIT_TYPE("");
			_appParam.getREQ_INFO().setEXCEL_SHEET_NAMES("");
		}
		
	}

}
