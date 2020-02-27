package org.ubstorm.service.parser.thread;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
//import org.ubstorm.service.DataServiceManager;
//import org.ubstorm.service.context.config.ConnectionInfo;
//import org.ubstorm.service.context.config.IServiceConfig;
//import org.ubstorm.service.context.config.ServiceConfigContext;
//import org.ubstorm.service.context.config.ServiceConfigProperty;
import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.data.packet.PKDataStream;
import org.ubstorm.service.data.packet.PKDeSerializer;
import org.ubstorm.service.logger.Log;
//import org.ubstorm.service.method.ViewerInfo5;
import org.ubstorm.service.parser.ItemPropertyProcess;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.info.DataSetInfo;
import org.ubstorm.service.parser.queue.IQueue;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;
import org.ubstorm.service.utils.crypto.AesCrypto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JobDataSetConsumerJson implements Runnable {

	private Logger log = Logger.getLogger(getClass());
	
	private HttpClient mHttpclient = null;
	private String mServiceSessionID = null;
	private String mServerUrl = null;
	
	private IQueue queue = null;
	private String threadName = null;
	
	public JobDataSetConsumerJson(IQueue queue, String index)
	{
		this.mHttpclient = new DefaultHttpClient();
		this.queue = queue;
		this.threadName = "[Child-" + index + "]";
	}
	
	@Override
	public void run() {
		log.info(" Start!!!...");
		
		try{
			
			while(!Thread.currentThread().isInterrupted()) {
				//(1) 작업큐에서 수행할 작업을 하나 가져온다.
				HashMap param = (HashMap) queue.pop();
				
				// (2) 얻어온 작업을 실제 수행한다.
				boolean bResult = true;
				if( param.get("fileType").equals("sap") )
				{
					//bResult = getConnectSapDataSet(param);
					log.debug(" Local EXE is not support SAP connection.");
				} 
				else
				{
					bResult = loadDatasetProcess(param);
				}
				
				if(bResult == true)
				{
					log.info(" loadDatasetProcess success!");
				}
				else
				{
					log.info(" loadDatasetProcess fail!");
				}
								
				queue.reduceJobCount();
				
				// CPU의 부담을 덜기 위해서 잠간 쉬게 한다.
				Thread.sleep(10);
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
			log.error(" OutOfMemoryError !!!");
			try {
				queue.clear();
			} catch (Exception e) {}
        }
		finally {
			log.info(" End...");
			this.mHttpclient.getConnectionManager().shutdown();
		}
		
	}
	
	
	private boolean loadDatasetProcess(HashMap param) {
		boolean bResult = false;
		
		ItemPropertyProcess mPropertyFn = new ItemPropertyProcess();
		
//		DataServiceManager _oService = (DataServiceManager) param.get("dataService");
//		String _oServiceSessionID = (String) param.get("dataServiceSessionID");
//		HttpClient gHttpclient = (HttpClient) param.get("dataService");		
//		String _serverUrl = (String) param.get("serverUrl");
		this.mServerUrl = (String) param.get("serverUrl");
		
		HashMap _resultDataSet = (HashMap) param.get("resultDataSet");
		DataSetInfo _udm = (DataSetInfo) param.get("udm");
		HashMap<String, HashMap<String, String>> _param = (HashMap<String, HashMap<String, String>>) param.get("param");
		
		String _dataClass = _udm.getClassName();
		String _dataId = _udm.getId();
		String _type = _udm.getTypeName();
		int _dataCount = 0;
		
		String _clientEditMode = param.get("clientEditMode").toString();
		String _fileType =  param.get("fileType").toString();	
		String _exportFilePath =  (String) param.get("exportFilePath");
				
		try
		{
			if( !_type.equals("ubi") )
			{
				ArrayList<HashMap<String, Object>> _dataList = connection(_udm, _param, -1, -1);
				_resultDataSet.put(_dataId, _dataList);
				
				_dataCount = _dataList.size();
			}
			else
			{
				// DB연결을 통한 DataLoad
				log.debug(" DB연결을 통해 서버로 부터 데이터를 받아온다...udm2 방식");
				
				ArrayList<HashMap<String, Object>> _dataList = new ArrayList();
	
				getLicenseText();			
				_dataList = getServerDataset(_fileType, this.mServerUrl, _udm, _param, -1, -1);
				
				_resultDataSet.put(_dataId, _dataList);
				
				_dataCount = _dataList.size();
			}
			
			// dataSet의 Length값이 0건일경우 columnList를 이용하여 빈 Row한건을 생성한다. ( E-Form일경우 처리 )
			
			if( "ON".equals(_clientEditMode) && _resultDataSet.containsKey( _dataId ) &&  _dataCount == 0)
			{
				ArrayList<String> _colList = (ArrayList<String>) _udm.getDateFieldList();
				
				HashMap<String, Object> _tempMap = new HashMap<String, Object>();
				ArrayList<HashMap<String, Object>> _tempAr = new ArrayList<HashMap<String, Object>>();
				for (int i = 0; i < _colList.size(); i++) {
					_tempMap.put( _colList.get(i), "");
				}
				
				_tempAr.add(_tempMap);
				_resultDataSet.put(_dataId, _tempAr);
			}
			
			
			bResult = true;
		}
		catch (OutOfMemoryError exp) {
			//log.error(getClass().getName() + "::" + this.threadName + " OutOfMemoryError !!!");
			bResult = false;
			
			ArrayList<HashMap<String, Object>> _errList = new ArrayList<HashMap<String, Object>>();
			HashMap<String, Object> _errInfo = new HashMap<String, Object>();
			_errInfo.put("ERR_MSG", "OutOfMemoryError : " + exp.getMessage());
			_errList.add(_errInfo);
			_resultDataSet.put("ERR_STACK", _errList);
        }
		catch(Exception exp)
		{
			//exp.printStackTrace();
			bResult = false;
			
			ArrayList<HashMap<String, Object>> _errList = new ArrayList<HashMap<String, Object>>();
			HashMap<String, Object> _errInfo = new HashMap<String, Object>();
			_errInfo.put("ERR_MSG", exp.getMessage());
			_errList.add(_errInfo);
			_resultDataSet.put("ERR_STACK", _errList);
		}
		
		return bResult;
	}
	
	
	private boolean getLicenseText() throws UnsupportedEncodingException
	{
		boolean isSuccess = true;
		
		//String url = "http://192.168.0.127:8080/UBIServerWeb/ubiform.do"; //server url (https로 되어있는)
		String url = this.mServerUrl; //server url 
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
            
		    HttpResponse response = mHttpclient.execute(httppost);
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
	            String useJsonFormFile = "";
	         
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
					  
					  useJsonFormFile = (String) ubObj.get("useJsonFormFile");
			    }
	            
	            log.debug("getLicenseText() SessionId=" + sessionId + ", licenseType=" + licenseType + ", useJsonFormFile=" + useJsonFormFile);
				
			    if( sessionId != null  )
				{
					//udmParams.getREQ_INFO().setCLIENT_SESSION_ID(sessionId);
			    	this.mServiceSessionID = sessionId;
				}
		    }
	        else
	        {
	            log.info("getLicenseText() Failed to get ServerToken. Response entity is null.");
	
	    		isSuccess = false;
	        }
		}
 		catch(Exception e)
		{
			e.printStackTrace();
			
			isSuccess = false;
			
            log.error("getLicenseText() Exception::" + e.getMessage());
		}
		finally
		{
			if(httppost != null)
			{
				httppost.abort();
			}
			//gHttpclient.getConnectionManager().shutdown();
		}
        
        return isSuccess;
	}
	
	
//	private ArrayList<HashMap<String, Object>> getServerDataset(String _fileType, String _serverUrl, HashMap<String, Object> _hm , HashMap<String, HashMap<String, String>> _param, NodeList _udmProps, int rowStartIndex, int fetchRowCount) throws Exception
	private ArrayList<HashMap<String, Object>> getServerDataset(String _fileType, String _serverUrl, DataSetInfo _udm , HashMap<String, HashMap<String, String>> _param, int rowStartIndex, int fetchRowCount) throws Exception
	{
		ArrayList<HashMap<String, Object>> _data = new ArrayList<HashMap<String, Object>>();

		if(this.mHttpclient == null)
			this.mHttpclient = new DefaultHttpClient();		
		
		//String url = "http://192.168.0.127:8080/UBIServerWeb/ubiform.do"; //server url (https로 되어있는)
		String url = _serverUrl; //server url 
		HttpPost httppost = null;
        try
		{
        	httppost = new HttpPost(url); 
            
        	log.info("getServerDataset() executing request : serverUrl=" + url + ", fileType=" + _fileType);
        
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
    		nameValuePairs.add(new BasicNameValuePair("FILE_TYPE", _fileType));
    		nameValuePairs.add(new BasicNameValuePair("CALL", "VIEWER"));
            
 			String TYPE = "";
			String SERVER = "";
			String DATABASE = "";
			String ID = "";
			String PW = "";
			String PORT = "";
			String sQuery = "";
			String DOMAIN = "";
			String QUERY_VERSION="";
			String isEncodingSQL="";
			
			//for(int p = 0; p < _udmProps.getLength(); p++)
			{
				TYPE = _udm.getDb_type();
				SERVER = _udm.getDb_server();
				DATABASE = _udm.getDb_database();
				ID = _udm.getDb_id();
				PW = _udm.getDb_pw();
				sQuery = _udm.getSql();
				PORT = _udm.getDb_port();
				DOMAIN = _udm.getDomain();
				QUERY_VERSION = _udm.getQueryVersion();
				isEncodingSQL = _udm.getIsEncodingSQL();
			}
			
			JSONObject objQparam = null;
			if(_param != null && !_param.isEmpty())
			{
				// param ===> {rowcount={"type":"string","parameter":"2000"}}
				objQparam = new JSONObject();
				JSONArray arrayParams = new JSONArray();
				
				// 방법3
		        for( String key : _param.keySet() ){
		        	
		        	//System.out.println( String.format("키 : %s, 값 : %s", key, _param.get(key)) );
		            
		        	HashMap<String, String> inParam = _param.get(key);	        	
		        	
		        	JSONObject qparam = new JSONObject();
		        	
		        	qparam.put("NAME", key);
		        	qparam.put("TYPE", inParam.get("type"));
		        	qparam.put("VALUE", inParam.get("parameter"));
		        	
		        	arrayParams.add(qparam);
		        }
				objQparam.put("PARAMLIST", arrayParams);
			}
			
			String QPARAMS = objQparam != null ? objQparam.toJSONString() : null;
            
            nameValuePairs.add(new BasicNameValuePair("DOMAIN", DOMAIN));
    		nameValuePairs.add(new BasicNameValuePair("QPARAMS", QPARAMS));  
    		nameValuePairs.add(new BasicNameValuePair("ENCODING", "false"));  
    		
            nameValuePairs.add(new BasicNameValuePair("TEXT", sQuery));
       		nameValuePairs.add(new BasicNameValuePair("TYPE", TYPE));
       		nameValuePairs.add(new BasicNameValuePair("SERVER", SERVER));
       		nameValuePairs.add(new BasicNameValuePair("DATABASE", DATABASE));
       		nameValuePairs.add(new BasicNameValuePair("ID", ID));
       		nameValuePairs.add(new BasicNameValuePair("PW", PW));
       		nameValuePairs.add(new BasicNameValuePair("PORT", PORT));
       		nameValuePairs.add(new BasicNameValuePair("QUERY_VERSION", QUERY_VERSION));
       		nameValuePairs.add(new BasicNameValuePair("ENCODING_SQL", isEncodingSQL));
       	    		
    		/*
    		UDMParamSet _appParams = m_reqManager.getServiceManager().getUdmParams();
    		String TEXT        	= _appParams.getDATA_QUERY().getTEXT();
    		String DATASET     	= _appParams.getDATA_QUERY().getDATASET();
    		String FORM_ID		= _appParams.getREQ_INFO().getFORM_ID();
    		String CALL        	= _appParams.getREQ_INFO().getCALL();
    			
    		String DOMAIN = _appParams.getREQ_INFO().getDOMAIN();
    		String QPARAMS = _appParams.getDATA_QUERY().getQPARAMS();

    		String TYPE        	= "";
    		String SERVER      	= "";
    		String DATABASE    	= "";
    		String ID         	= "";
    		String PW          	= "";
    		String PORT			= "";
    		Boolean ENCODING 	= true;
    		Boolean COMPRESS 	= true;
    		
    		if(DOMAIN == null || DOMAIN.equals("null") || DOMAIN.isEmpty() )
    		{
    			TYPE        	= _appParams.getDATA_QUERY().getTYPE();
    			SERVER      	= _appParams.getDATA_QUERY().getSERVER();
    			DATABASE    	= _appParams.getDATA_QUERY().getDATABASE();
    			ID         		= _appParams.getDATA_QUERY().getID();
    			PW          	= _appParams.getDATA_QUERY().getPW();
    			PORT			= _appParams.getDATA_QUERY().getPORT();
    			
    			ENCODING 		= _appParams.getDATA_QUERY().getENCODING()=="false" ? false : true;
    			COMPRESS 		= _appParams.getDATA_QUERY().getCOMPRESS()=="false" ? false : true;
    		}
    		*/
    		
    		
            UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            httppost.setEntity(entity1);
            
//		    HttpResponse response = gHttpclient.execute(httppost);
		    HttpResponse response = this.mHttpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        if (entity != null) 
	        {
	        	PKDataStream m_asdStream = new PKDataStream();
            	PKDeSerializer m_pkDeSerializer = new PKDeSerializer();
            	
	        	// paramDataset 처리
				String datasetId = _udm.getId();
				
	        	File someFile = new File(Log.basePath + "data/" + datasetId + ".dat");
	        	FileOutputStream fos = new FileOutputStream(someFile);
	        	entity.writeTo(fos);	        	  
	            
	        	FileInputStream fis = new FileInputStream(someFile);
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            byte[] buf = new byte[1024];
	            try {
	            	
	                for (int readNum; (readNum = fis.read(buf)) != -1;) {
	                    
	                	int nMaxOffset = m_asdStream.pk_offset + 1024;
	                
	                	bos.write(buf, 0, readNum); //no doubt here is 0
	                    //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
	                    System.out.println("read " + readNum + " bytes,");
	                    
	                    m_asdStream.parsePkData(buf);
	                    
	                    m_asdStream.pk_offset = m_pkDeSerializer.deSerialize(m_asdStream);
						if(!m_pkDeSerializer.isUsefulPacket)
						{
							if(m_pkDeSerializer.getErorMsg() != null)
							{
								System.out.println("m_pkDeSerializer::error=" + m_pkDeSerializer.getErorMsg());
								break;
							}
							continue;
						}
						else // 분할전송시, 다음 패킷으로 넘어가는 경우의 처리
						{
							int nRemainLength = m_asdStream.pk_buffer.length_-m_asdStream.pk_offset;
							System.out.println("m_asdStream.pk_offset > nMaxOffset : nRemainLength=" + nRemainLength);
							
							byte[] _tmpBuff = new byte[nRemainLength];
							System.arraycopy(m_asdStream.pk_buffer.getBuffer(), m_asdStream.pk_offset, _tmpBuff, 0, nRemainLength);
							
							m_asdStream.clearBuffer();
						 
							m_asdStream.parsePkData(_tmpBuff);
							
							m_asdStream.pk_offset = m_pkDeSerializer.deSerialize(m_asdStream);
							if(!m_pkDeSerializer.isUsefulPacket)
							{
								if(m_pkDeSerializer.getErorMsg() != null)
								{
									System.out.println("m_pkDeSerializer::error=" + m_pkDeSerializer.getErorMsg());
									break;
								}
								continue;
							}
						}
						
	                }
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	            
	            byte[] fileArray = bos.toByteArray();
	            bos.close();
	            fos.close();

	        	log.debug("getServerDataset() Success to get server dataset data. fileArray.length=" + fileArray.length);
	            
	            _data = m_pkDeSerializer.getDataSet();
		    }
	        else
	        {
	        	log.info("getServerDataset() Failed to get server dataset data. Response entity is null.");
	        }
		}
 		catch(Exception e)
		{
			e.printStackTrace();
			log.error("getServerDataset() Exception::" + e.getMessage());
		}
		finally
		{
			if(httppost != null)
			{
				httppost.abort();
			}
			//gHttpclient.getConnectionManager().shutdown();
		}
		
		
		return _data;
	}
	
	private ArrayList<HashMap<String, Object>> connection(DataSetInfo _udm , HashMap<String, HashMap<String, String>> _param, int rowStartIndex, int fetchRowCount) throws Exception
	{
		URL _url = null;
		URLConnection _urlConnection = null;
		InputStream _inputS = null;
		InputStreamReader _inputR = null;
		BufferedReader _buf = null;

		ArrayList<HashMap<String, Object>> _data = new ArrayList<HashMap<String, Object>>();
		
		String _type = _udm.getTypeName();
		
		String _timeOutStr = common.getPropertyValue("dataOption.httpTimeOut");
		int _timeOut = (_timeOutStr==null)?300:Integer.valueOf(_timeOutStr) ;

		String _urlStr = _udm.getUrl();
		if(!(_type.equals("user") || _type.equals("param"))  && "null".equals(_urlStr))
			return _data;

		String _colType = _udm.getParamEvent();
		
		boolean _firstRow = Boolean.valueOf( _udm.getFirstRow() );
		
		
		String _dataSetUrl = Log.dataSetURL;
		if(_dataSetUrl == null && Log.formFileGetFromServer == true)
		{
			// local exe모듈에서는 전체경로가 다 있어야 한다.
			URL _tmp = new URL(this.mServerUrl);
			String _protocol = _tmp.getProtocol();
			String _host = _tmp.getHost();
			int _port = _tmp.getPort();
			_dataSetUrl = _port != -1 ? _protocol + "://" + _host + ":" + _port : _protocol + "://" + _host;
		}		
		
//		String _dataSetUrl = Log.dataSetURL;		// 기존 문서의 경우 컨텍스트명이 호출 URL에 담겨있어서 context가 다른곳을 호출시 오류가 발생함. 2016-02-17
		String _sURL = _urlStr.indexOf("http://") != -1 ? _urlStr : _dataSetUrl + _urlStr;
		String _filepath = "";
		
		log.debug("connection() DATASET_LOAD_TYPE =" + _type);
			
		try {
			if( _type.equals("user"))
			{
				String _dataStr = common.base64_decode_uncompress( _udm.getData(), "UTF-8");
				
				if( !_dataStr.equals(""))
				{
					ArrayList<HashMap<String, Object>> _tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse(_dataStr);
					
					int _dataListLength = fetchRowCount != -1 ? fetchRowCount : _tmpdata.size();
					int _startRow = rowStartIndex != -1 ? rowStartIndex : 0;
					
					log.debug("connection() - user " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);  
					
					if( rowStartIndex >= _tmpdata.size()) 
					{
						return _data;
					}
					
					for (int j = _startRow; j < _startRow + _dataListLength; j++)
					{
						HashMap<String, Object> _dataRow = (HashMap) _tmpdata.get(j);								
						_data.add(_dataRow);
					}
				}
				
			}
			else if( _type.equals("File"))
			{
				_data = getFileDataset(_udm, _param, rowStartIndex, fetchRowCount);
			}
			else if( _type.equals("param"))
			{
				// paramDataset 처리
				String datasetId = _udm.getId();
						
				// 파라미터 셋팅
				String _str = "";			
				
				if(_param.containsKey(datasetId) && _param.get(datasetId) != null)
				{
					HashMap<String, String> _valueHm = _param.get(datasetId);
					String resultStr = _valueHm.get("parameter");
					
					_data = convertParamDataSet( _udm, resultStr);
				
					log.debug("connection() ============= PARAM DATASET ID :  "+  datasetId + ",	DATASET COUNT : " + _data.size() );
				}
				
			}
			else	// HTTP call 방식의 경우
			{
				// 파라미터 셋팅
				String _pString = "";
				int _pC = 0;
				
				getLicenseText();				
				String _stConnectSessionID = this.mServiceSessionID;
				
				for(String _keySet : _param.keySet())
				{
					HashMap<String, String> _valueHm = _param.get(_keySet);
					
					String _value = _valueHm.get("parameter");

					if( _pC == 0 )
					{
						_pString += _keySet + "=" + _value;				
					}
					else
					{
						_pString += "&" + _keySet + "=" + _value;
					}
					_pC++;
				}
				
				// dataset이 sap일경우 paramter에 CALL값을 같이 전달한다
				if( _type.equals("sap"))
				{
					// Sap은 무시
					System.out.println("Local EXE is not support SAP connection.");
				}


				String _str = null;
				String resultStr = "";
				StringBuilder resultStrb = new StringBuilder();
				
				if( _type.equals("Http"))
				{
					ArrayList<HashMap<String, Object>> _tmpdata = new ArrayList<HashMap<String, Object>>();
					
					String _delimiter = changeDelimiter(_udm.getDelimiter());
					

					if("".equals(_filepath) )
					{	
						if( _colType.equals("Post"))
						{
							_url = new URL(_sURL);
							_urlConnection = _url.openConnection();
							_urlConnection.setConnectTimeout(_timeOut*1000);
							_urlConnection.setReadTimeout(_timeOut*1000);
							_urlConnection.setDoOutput(true);
							
//							setHttpConnectSessionID(_oService, _url, _urlConnection);
							setHttpConnectSessionID(_stConnectSessionID, _url, _urlConnection);
							
//							printToOutputStream(_urlConnection.getOutputStream(),"msg입니다.");
//							printToInputStream(_urlConnection.getInputStream());
							OutputStreamWriter wr = new OutputStreamWriter(_urlConnection.getOutputStream());
							wr.write(_pString);
							wr.flush();
						}
						else
						{
							_url = new URL(_sURL + "?" + _pString);
							_urlConnection = _url.openConnection();
							_urlConnection.setConnectTimeout(_timeOut*1000);
							_urlConnection.setReadTimeout(_timeOut*1000);

//							setHttpConnectSessionID(_oService, _url, _urlConnection);
							setHttpConnectSessionID(_stConnectSessionID, _url, _urlConnection);

//							printToInputStream(_urlConnection.getInputStream());
						}
						
						_inputS = _urlConnection.getInputStream();
						_inputR = new InputStreamReader(_inputS , "UTF-8");
						
						_buf = new BufferedReader(_inputR);
						
						while(true)
						{
							_str = _buf.readLine();
							if( _str == null) break;
							
							_tmpdata =	setCsvDataSetMapping(_str.split(_delimiter) , _tmpdata );
						}
						
						log.debug("connection() ============= CSV DATASET ID : Load Http " );
					}
					else
					{
						File _file = new File(_filepath);
						resultStrb.append(common.file_get_contents(_file, "UTF-8"));
						if(resultStrb.toString().equals("") == false)
						{
							String[] _splitList = resultStrb.toString().replace("\r", "").split("\n");
							for (int i = 0; i < _splitList.length; i++) {
								_tmpdata =	setCsvDataSetMapping(_splitList[i].split(_delimiter) , _tmpdata );
							}
						}
						
						log.debug("connection() ============= CSV DATASET ID : Load File " );
					}
					
					if( !_firstRow ) 
					{
						if( _tmpdata.size() > 0 )
						{
							_tmpdata.remove(0);
						}
					}
					
					int _dataListLength = fetchRowCount != -1 ? fetchRowCount : _tmpdata.size();
					int _startRow = rowStartIndex != -1 ? rowStartIndex : 0;
					
					log.debug("connection() - Http " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);    
					
					if( rowStartIndex >= _tmpdata.size()) 
					{
						return _data;
					}
					
					for (int j = _startRow; j < _startRow + _dataListLength; j++)
					{
						HashMap<String, Object> _dataRow = (HashMap) _tmpdata.get(j);								
						_data.add(_dataRow);
					}					
				}
				else if( _type.equals("json")  || _type.equals("sap"))
				{
					String _jsonDataType = _udm.getJsonDataType();
					String _jsonObjectKey = _udm.getJsonObjectKey();
					
					ArrayList<HashMap<String, Object>> _tmpdata = new ArrayList<HashMap<String, Object>>();
					
					if("".equals(_filepath) )
					{	
						
						if( _colType.equals("Post"))
						{
							_url = new URL(_sURL);
							_urlConnection = _url.openConnection();
							_urlConnection.setConnectTimeout(_timeOut*1000);
							_urlConnection.setReadTimeout(_timeOut*1000);
							_urlConnection.setDoOutput(true);
							
							setHttpConnectSessionID(_stConnectSessionID, _url, _urlConnection);
							
//							printToOutputStream(_urlConnection.getOutputStream(),"msg입니다.");
//							printToInputStream(_urlConnection.getInputStream());
							OutputStreamWriter wr = new OutputStreamWriter(_urlConnection.getOutputStream());
							wr.write(_pString);
							wr.flush();
						}
						else
						{
							_url = new URL(_sURL + "?" + _pString);
							_urlConnection = _url.openConnection();
							_urlConnection.setConnectTimeout(_timeOut*1000);
							_urlConnection.setReadTimeout(_timeOut*1000);

							setHttpConnectSessionID(_stConnectSessionID, _url, _urlConnection);

//							printToInputStream(_urlConnection.getInputStream());
						}
						
						_inputS = _urlConnection.getInputStream();
						_inputR = new InputStreamReader(_inputS , "UTF-8");
						
						_buf = new BufferedReader(_inputR);						
						
						while (true)
						{
							_str = _buf.readLine();
							if( _str == null) break;
							resultStrb.append(_str);
						}
						
					}
					else
					{
						File _file = new File(_filepath);
						resultStrb.append(common.file_get_contents(_file, "UTF-8"));
						
					}
					
					resultStr = resultStrb.toString();
					
					
					// 특정 파일의 경우 맨 앞자리에 zero-width no-break space. 문자가 존재하여 xml로 파싱이 실패하는 경우가발생하여 처리 2016-03-04 최명진
					if( resultStr.substring(0, 1).hashCode() == 65279)
					{
						resultStr = resultStr.substring(1, resultStr.length());
					}
					
					
					if( _jsonDataType.equals("object") ){
						
						JSONParser jsonParser = new JSONParser();
			            JSONObject jsonObject = (JSONObject) jsonParser.parse(resultStr);
			            Object _jsonObjectDataStr=jsonObject.get( _jsonObjectKey );
			            _tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse(_jsonObjectDataStr.toString());
						
					}else{
						
						
						if( resultStr.substring(0,1).equals("["))
						{
							_tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse(resultStr);
							
						}
						else
						{
							_tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse("[" + resultStr);
						}
						
					}

					
					if( !_firstRow ) 
					{
						if( _tmpdata.size() > 0 )
						{
							_tmpdata.remove(0);
						}
					}
					
					
					int _dataListLength = fetchRowCount != -1 ? fetchRowCount : _tmpdata.size();
					int _startRow = rowStartIndex != -1 ? rowStartIndex : 0;
					
					log.debug("connection() - JSON " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);    
					
					if( rowStartIndex >= _tmpdata.size()) 
					{
						return _data;
					}
					
					for (int j = _startRow; j < _startRow + _dataListLength; j++)
					{
						HashMap<String, Object> _dataRow = (HashMap) _tmpdata.get(j);								
						_data.add(_dataRow);
					}			
					
				}
				else if( _type.equals("xmlObj"))
				{
					if("".equals(_filepath) )
					{
						
						if( _colType.equals("Post"))
						{
							_url = new URL(_sURL);
							_urlConnection = _url.openConnection();
							_urlConnection.setConnectTimeout(_timeOut*1000);
							_urlConnection.setReadTimeout(_timeOut*1000);
							_urlConnection.setDoOutput(true);
							
							setHttpConnectSessionID(_stConnectSessionID, _url, _urlConnection);
							
//							printToOutputStream(_urlConnection.getOutputStream(),"msg입니다.");
//							printToInputStream(_urlConnection.getInputStream());
							OutputStreamWriter wr = new OutputStreamWriter(_urlConnection.getOutputStream());
							wr.write(_pString);
							wr.flush();
						}
						else
						{
							_url = new URL(_sURL + "?" + _pString);
							_urlConnection = _url.openConnection();
							_urlConnection.setConnectTimeout(_timeOut*1000);
							_urlConnection.setReadTimeout(_timeOut*1000);

							setHttpConnectSessionID(_stConnectSessionID, _url, _urlConnection);

//							printToInputStream(_urlConnection.getInputStream());
						}
						
						_inputS = _urlConnection.getInputStream();
						_inputR = new InputStreamReader(_inputS , "UTF-8");
						
						_buf = new BufferedReader(_inputR);
						
						while (true)
						{
							_str = _buf.readLine();
							if( _str == null) break;
							resultStrb.append(_str);
						}
					}
					else
					{
						File _file = new File(_filepath);
						resultStrb.append(common.file_get_contents(_file, "UTF-8"));
					}
					
					resultStr = resultStrb.toString();					
					if( !resultStr.equals("") )
					{
//						resultStr = resultStr.replace("\t", "");
						// 특정 파일의 경우 맨 앞자리에 zero-width no-break space. 문자가 존재하여 xml로 파싱이 실패하는 경우가발생하여 처리 2015-12-28 최명진
						if( resultStr.substring(0, 1).hashCode() == 65279)
						{
							resultStr = resultStr.substring(1, resultStr.length());
						}
						
						InputSource _is = new InputSource(new StringReader(resultStr));
						Document _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
						
						String _rootE = _udm.getRootElement();
						String _dataE = _udm.getDataElement();

						String _tagRefID = _udm.getTagRefID();
						Boolean _isRefTag = false;
						if( _tagRefID != null && (!_tagRefID.equals("")) && (!_tagRefID.equals("null"))  ){
							_isRefTag = true;
						}
						
						NodeList _xmlDataList;
						HashMap<String, Object> _dataRow;
						_xmlDataList = _doc.getElementsByTagName(_rootE);
						
						// xml Project
						int _xmlDataListLength = _xmlDataList.getLength();
						for (int i = 0; i < _xmlDataListLength; i++) 
						{
							Element _childRoot = (Element) _xmlDataList.item(i);							
							NodeList _dataList = _childRoot.getElementsByTagName(_dataE);
							int _dataListLength = fetchRowCount != -1 ? fetchRowCount : _dataList.getLength();
							int _startRow = rowStartIndex != -1 ? rowStartIndex : 0;
							
							log.debug("connection() - xmlObj " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);    
							
							if( rowStartIndex >= _dataList.getLength()) 
							{
								return _data;
							}
							
							for (int j = _startRow; j < _startRow + _dataListLength; j++) 
							{
								_dataRow = new HashMap<String, Object>();
								Element _childData = (Element) _dataList.item(j);			
								
								if( _isRefTag ){
									String _childID = ((Element) _childData).getAttribute("id");
									if( _tagRefID.equals(_childID) ){
										NodeList _childList = _childData.getChildNodes();
										int _childListLength = _childList.getLength();
										for (int k = 0; k < _childListLength; k++) 
										{
											Node _rowData = _childList.item(k);
											if( _rowData instanceof Element )
											{
												String _nodeName = ((Element) _rowData).getAttribute("id");
												String _nodeValue = _rowData.getTextContent();
												
												_dataRow.put(_nodeName, _nodeValue);
											}
										}
										_data.add(_dataRow);
									}else{
										continue;
									}
								}else{
									
									NodeList _childList = _childData.getChildNodes();
									int _childListLength = _childList.getLength();
									for (int k = 0; k < _childListLength; k++) 
									{
										Node _rowData = _childList.item(k);
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
							
							
						}
						
						_doc = null;	
						_is = null;
					}
					
//					if( !_firstRow ) 
//					{
//						if( _data.size() > 0 )
//						{
//							_data.remove(0);
//						}
//					}
					
				}
				
				if(_inputS != null)
					_inputS.close();
			}
			
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception(e);
		}
		
		if( _data != null ) log.debug("connection() ============== DATASET COUNT : " + _data.size() );
		
		return _data;
	}
	
	private ArrayList<HashMap<String, Object>> setCsvDataSetMapping(String[] _strList , ArrayList<HashMap<String, Object>> _dataList )
	{
		ArrayList<HashMap<String, Object>> _retList = _dataList;
		
		HashMap<String, Object> _dataRow = new HashMap<String, Object>();
		
		for (int i = 0; i < _strList.length; i++) 
		{
			_dataRow.put("col_" + (i+1) ,_strList[i]);
		}
		
		_retList.add(_dataRow);
		
		return _retList;
	}
	
	private ArrayList<HashMap<String, Object>> setCsvDataSetMapping(String[] _strList , ArrayList<HashMap<String, Object>> _dataList, ArrayList<String> _colList )
	{
		ArrayList<HashMap<String, Object>> _retList = _dataList;
		
		HashMap<String, Object> _dataRow = new HashMap<String, Object>();
		
		for (int i = 0; i < _strList.length; i++) 
		{
			if( _colList !=null && _colList.size() > i)
			{
				_dataRow.put(_colList.get(i) ,_strList[i]);
			}
			else
			{
				_dataRow.put("col_" + (i+1) ,_strList[i]);
			}
		}
		
		_retList.add(_dataRow);
		
		return _retList;
	}
	
	 /**
     * functionName   :   changeDelimiter</br>
     * desc         :   구분자 문자열 리턴   
     * @param _delimiter
     * @return 
     * 
     */      
    private String changeDelimiter( String _delimiter )
    {
    	if( _delimiter.equals("Tab"))
    	{
    		return "\\\t";
    	}
    	else if( _delimiter.equals("Comma"))
    	{
    		return ",";
    	}
    	else if( _delimiter.equals("Slash"))
    	{
    		return "/"; 
    	}
    	else if( _delimiter.equals("^"))
    	{
    		return "\\^"; 
    	}
       
       return "";
    }
    
    
    
    private ArrayList<HashMap<String, Object>> convertParamDataSet(DataSetInfo _udm, String dataString ) throws SAXException, IOException, ParserConfigurationException
    {
    	
    	int i = 0;
    	int j = 0;
     	
    	ArrayList<HashMap<String, Object>> _data = new ArrayList<HashMap<String, Object>>();
    	int _dataListLength = 0; 
    	int _startRow = 0;
    	
    	if( "true".equals(_udm.getBase64Encode()) )
    	{
    		try {
//    			dataString = common.base64_decode(dataString.replace(" ","+") );
    			dataString = common.base64_decode(dataString.replace(" ","+"), "UTF-8" );
    			//dataString = common.base64_decode(dataString);
    		} catch (Exception e) {
    			// TODO: handle exception
    		}
    	}
    	
    	if( "true".equals(_udm.getUriEncode()) )
    	{
    		try {
    			if( "json".equals(_udm.getFileType()) == false || (dataString != null && dataString.charAt(0)=='%') )	// URL Encoding이 안돠어 있는 경우의 예외처리
    				dataString = URLDecoder.decode(dataString, "UTF-8");
			} catch (Exception e) {
				// TODO: handle exception
			}
    		
    	}
    	
		boolean _firstRow = true;
    	
    	String mType = "";
    	
    	if( _udm.getFirstRow() != null )
    	{
    		_firstRow = "true".equals(_udm.getFirstRow());
    	}
		
    	//담긴 정보를 이용하여 Parameter에서 값을 가져와서 파싱하여 리턴
    	mType = _udm.getFileType();
    	
    	if( "csv".equals(mType) || "json".equals(mType) )
    	{
    		ArrayList<HashMap<String, Object>> _tmpdata = new ArrayList<HashMap<String, Object>>();
			
    		if( "csv".equals(mType) )
    		{
    			String _delimiter = changeDelimiter(_udm.getDelimiter());
    			
    			String[] _str = dataString.replaceAll("\r", "").split("\n"); 
    			
    			ArrayList<String> _dsFieldList = (ArrayList<String>) _udm.getDateFieldList();
    			
    			for ( i = 0; i < _str.length; i++) {
    				if( _dsFieldList != null && _dsFieldList.size() > 0 )
    				{
    					_tmpdata =	setCsvDataSetMapping(_str[i].split(_delimiter) , _tmpdata, _dsFieldList );
    				}
    				else
    				{
    					_tmpdata =	setCsvDataSetMapping(_str[i].split(_delimiter) , _tmpdata );
    				}
    			}
    		}
    		else
    		{
       			//String _decodeStr = URLDecoder.decode(dataString, "UTF-8");
    			
    			if( dataString.substring(0,1).equals("["))
    			{
    				try {
						_tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parseWithException(dataString);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();  
					}
    			}
    			else
    			{
    				_tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse("[" + dataString);
    			}
    			
    		}
			
			if( !_firstRow && _tmpdata != null ) 
			{
				if( _tmpdata.size() > 0 )
				{
					_tmpdata.remove(0);
				}
			}
			
			if( _tmpdata != null )
			{
				_dataListLength = _tmpdata.size();
				_startRow = 0;
				
				for ( j = _startRow; j < _startRow + _dataListLength; j++)
				{
					HashMap<String, Object> _dataRow = (HashMap) _tmpdata.get(j);								
					_data.add(_dataRow);
				}
			}
    	}
    	else if( "xmlObj".equals(mType) )
    	{
    		
    		InputSource _is = new InputSource(new StringReader(dataString));
			Document _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
			
			String _rootE = _udm.getRootElement();
			String _dataE = _udm.getDataElement();
			
			NodeList _xmlDataList;
			HashMap<String, Object> _dataRow;
			_xmlDataList = _doc.getElementsByTagName(_rootE);
			
			// xml Project
			int _xmlDataListLength = _xmlDataList.getLength();
			for ( i = 0; i < _xmlDataListLength; i++) 
			{
				Element _childRoot = (Element) _xmlDataList.item(i);							
				NodeList _dataList = _childRoot.getElementsByTagName(_dataE);
				_dataListLength = _dataList.getLength();
				_startRow 		= 0;
				
				for ( j = _startRow; j < _startRow + _dataListLength; j++) 
				{
					_dataRow = new HashMap<String, Object>();
					Element _childData = (Element) _dataList.item(j);								
					NodeList _childList = _childData.getChildNodes();
					int _childListLength = _childList.getLength();
					for (int k = 0; k < _childListLength; k++) 
					{
						Node _rowData = _childList.item(k);
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
    		
    		
    	}
    	
    	
    	return _data;
    }
    
    /**
     * HTTP URL Connection으로 외부 서비스를 호출하는 경우 세션에 걸려서 오류가 리턴될 수 있다.
     * 이러한 경우, tomcat의 Context.xml에 crossContext=true 로 설정하고, 세션을 체크하는 쪽의 SessionID 값을 Context의 attribute에 추가한다.
     * UBIForm Server에선 해당 Context에 접근하기 위해 Context의 attribute에 저장된 공유 SessionID를 이용한다.
     * 
     * @param _oService
     * @param _url
     * @param _urlConnection
     */
    private void setHttpConnectSessionID(String _stConnectSessionID, URL _url, URLConnection _urlConnection)
    {
    	try
		{
    		if(_stConnectSessionID != null && _stConnectSessionID.length() > 0)
			{
				_urlConnection.setRequestProperty("Cookie","JSESSIONID=" + _stConnectSessionID);
				log.debug("setHttpConnectSessionID() _stConnectSessionID ==============================>" + _stConnectSessionID);
			}
		}
		catch(Exception e)
		{
			//
		}
    }
    

	protected ArrayList<HashMap<String, Object>> getJsonData( HashMap<String, Object> _hmItem, String _clientEditMode, String _dsID, ArrayList<HashMap<String, Object>> _tmpdata , boolean _firstRow, int fetchRowCount, int rowStartIndex)
	{
		ArrayList<HashMap<String, Object>> _data = new ArrayList<HashMap<String, Object>>();
		
		if( !_firstRow ) 
		{
			if( _tmpdata.size() > 0 )
			{
				_tmpdata.remove(0);
			}
		}
		
		int _dataListLength = fetchRowCount != -1 ? fetchRowCount : _tmpdata.size();
		int _startRow = rowStartIndex != -1 ? rowStartIndex : 0;
		
		if( rowStartIndex >= _tmpdata.size()) 
		{
			return _tmpdata;
		}
		
		for (int j = _startRow; j < _startRow + _dataListLength; j++)
		{
			HashMap<String, Object> _dataRow = (HashMap) _tmpdata.get(j);								
			_data.add(_dataRow);
		}
		
//		_resultDataSet.put(_dsID, _data);
		
		return _data;
	}
	
	
	/**
	 * 로컬 PC에서 데이터 파일을 통해 데이터셋을 불러온다.
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws ParseException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * */
	public ArrayList<HashMap<String, Object>> getFileDataset( DataSetInfo _udm,  HashMap<String, HashMap<String, String>> _param, int rowStartIndex, int fetchRowCount) throws JDOMException, IOException, ParseException, SAXException, ParserConfigurationException
	{
		//String file_path = String.valueOf(_hm.get("path"));
		String file_path = _udm.getPath();
		
		ArrayList<HashMap<String, Object>> _data = new ArrayList<HashMap<String, Object>>();
		
		String _str = null;
		String resultStr = "";
		StringBuilder resultStrb = new StringBuilder();
		String _fileType = _udm.getFileType();
		
		String _colType = _udm.getParamEvent();
		boolean _firstRow = "true".equals(_udm.getFirstRow());
		
		if(_fileType.equals("json"))
		{
			String _jsonDataType = _udm.getJsonDataType();
			String _jsonObjectKey = _udm.getJsonObjectKey();
			
			ArrayList<HashMap<String, Object>> _tmpdata = new ArrayList<HashMap<String, Object>>();
			
			File _file = new File(file_path);
			resultStrb.append(common.file_get_contents(_file, "UTF-8"));
			
			resultStr = resultStrb.toString();			
			
			// 특정 파일의 경우 맨 앞자리에 zero-width no-break space. 문자가 존재하여 xml로 파싱이 실패하는 경우가발생하여 처리 2016-03-04 최명진
			if( resultStr.substring(0, 1).hashCode() == 65279)
			{
				resultStr = resultStr.substring(1, resultStr.length());
			}
			
			if( _jsonDataType.equals("object") ){
				
				JSONParser jsonParser = new JSONParser();
	            JSONObject jsonObject = (JSONObject) jsonParser.parse(resultStr);
	            Object _jsonObjectDataStr=jsonObject.get( _jsonObjectKey );
	            _tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse(_jsonObjectDataStr.toString());
				
			}else{
				
				if( resultStr.substring(0,1).equals("["))
				{
					_tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse(resultStr);
				}
				else
				{
					_tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse("[" + resultStr);
				}
			}
			
			if( !_firstRow ) 
			{
				if( _tmpdata.size() > 0 )
				{
					_tmpdata.remove(0);
				}
			}
			
			
			int _dataListLength = fetchRowCount != -1 ? fetchRowCount : _tmpdata.size();
			int _startRow = rowStartIndex != -1 ? rowStartIndex : 0;
			
			log.debug("getFileDataset() - JSON " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);    
			
			if( rowStartIndex >= _tmpdata.size()) 
			{
				return _data;
			}
			
			for (int j = _startRow; j < _startRow + _dataListLength; j++)
			{
				HashMap<String, Object> _dataRow = (HashMap) _tmpdata.get(j);								
				_data.add(_dataRow);
			}	
		}	
		else if(_fileType.equals("xmlObj"))
		{
			File _file = new File(file_path);
			resultStrb.append(common.file_get_contents(_file, "UTF-8"));
			
			resultStr = resultStrb.toString();					
			if( !resultStr.equals("") )
			{
//				resultStr = resultStr.replace("\t", "");
				// 특정 파일의 경우 맨 앞자리에 zero-width no-break space. 문자가 존재하여 xml로 파싱이 실패하는 경우가발생하여 처리 2015-12-28 최명진
				if( resultStr.substring(0, 1).hashCode() == 65279)
				{
					resultStr = resultStr.substring(1, resultStr.length());
				}
				
				InputSource _is = new InputSource(new StringReader(resultStr));
				Document _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
				
				String _rootE = _udm.getRootElement();
				String _dataE = _udm.getDataElement();

				String _tagRefID = _udm.getTagRefID();
				Boolean _isRefTag = false;
				if( _tagRefID != null && (!_tagRefID.equals("")) ){
					_isRefTag = true;
				}
				
				NodeList _xmlDataList;
				HashMap<String, Object> _dataRow;
				_xmlDataList = _doc.getElementsByTagName(_rootE);
				
				// xml Project
				int _xmlDataListLength = _xmlDataList.getLength();
				for (int i = 0; i < _xmlDataListLength; i++) 
				{
					Element _childRoot = (Element) _xmlDataList.item(i);							
					NodeList _dataList = _childRoot.getElementsByTagName(_dataE);
					int _dataListLength = fetchRowCount != -1 ? fetchRowCount : _dataList.getLength();
					int _startRow = rowStartIndex != -1 ? rowStartIndex : 0;
					
					log.debug("getFileDataset() - xmlObj " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);    
					
					if( rowStartIndex >= _dataList.getLength()) 
					{
						return _data;
					}
					
					for (int j = _startRow; j < _startRow + _dataListLength; j++) 
					{
						_dataRow = new HashMap<String, Object>();
						Element _childData = (Element) _dataList.item(j);			
						
						if( _isRefTag ){
							String _childID = ((Element) _childData).getAttribute("id");
							if( _tagRefID.equals(_childID) ){
								NodeList _childList = _childData.getChildNodes();
								int _childListLength = _childList.getLength();
								for (int k = 0; k < _childListLength; k++) 
								{
									Node _rowData = _childList.item(k);
									if( _rowData instanceof Element )
									{
										String _nodeName = ((Element) _rowData).getAttribute("id");
										String _nodeValue = _rowData.getTextContent();
										
										_dataRow.put(_nodeName, _nodeValue);
									}
								}
								_data.add(_dataRow);
							}else{
								continue;
							}
						}else{
							
							NodeList _childList = _childData.getChildNodes();
							int _childListLength = _childList.getLength();
							for (int k = 0; k < _childListLength; k++) 
							{
								Node _rowData = _childList.item(k);
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
					
					
				}
				
				_doc = null;	
				_is = null;
			}
			
//			if( !_firstRow ) 
//			{
//				if( _data.size() > 0 )
//				{
//					_data.remove(0);
//				}
//			}
			
		}
		else	// CSV
		{
			ArrayList<HashMap<String, Object>> _tmpdata = new ArrayList<HashMap<String, Object>>();			
			String _delimiter = changeDelimiter(_udm.getDelimiter());
			
			File _file = new File(file_path);
			resultStrb.append(common.file_get_contents(_file, "UTF-8"));
			if(resultStrb.toString().equals("") == false)
			{
				String[] _splitList = resultStrb.toString().replaceAll("\r", "").split("\n");
				for (int i = 0; i < _splitList.length; i++) {
					_tmpdata =	setCsvDataSetMapping(_splitList[i].split(_delimiter) , _tmpdata );
				}
			}
			
			log.debug("getFileDataset() ============= CSV DATASET : Load File " );
			
			if( !_firstRow ) 
			{
				if( _tmpdata.size() > 0 )
				{
					_tmpdata.remove(0);
				}
			}
			
			int _dataListLength = fetchRowCount != -1 ? fetchRowCount : _tmpdata.size();
			int _startRow = rowStartIndex != -1 ? rowStartIndex : 0;
			
			log.debug("getFileDataset() - Http " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);    
			
			if( rowStartIndex >= _tmpdata.size()) 
			{
				return _data;
			}
			
			for (int j = _startRow; j < _startRow + _dataListLength; j++)
			{
				HashMap<String, Object> _dataRow = (HashMap) _tmpdata.get(j);								
				_data.add(_dataRow);
			}					
		}
		
		return _data;
	}
	
	
	/**
	 * 사용자가 생성하는 자주 사용하는 문자열 데이터셋을 불러온다.
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws ParseException 
	 * */
	public ArrayList<HashMap<String, Object>> getGlobalDataset( HashMap<String, HashMap<String, String>> _param ) throws JDOMException, IOException, ParseException
	{
		
//		String ubParams = m_appParams.getREQ_INFO().getUBPARAMS();
		JSONParser jparser = new JSONParser();
//		JSONObject _paramJO = (JSONObject) jparser.parse(ubParams);
		
		
		HashMap<String, HashMap<String, String>> _paramJO = new HashMap<String, HashMap<String, String>>() ;
		HashMap<String, String> _paramMap;
		 for (String _key : _param.keySet()) {
			 
			 HashMap<String, String> _subParam = new HashMap<String, String>();
			 _paramMap = _param.get(_key);
			 for (String _subKey : _paramMap.keySet()) {
				 _subParam.put(_subKey, _paramMap.get(_subKey));
			 }
			 
			 _paramJO.put(_key, _subParam)	;
		 }
		
		String file_path = Log.ufilePath + "UFile/sys/SYS/globalDataset.xml";
		
		String _sysDirPath = common.getPropertyValue("ubform.sysDir");
		
		if(_sysDirPath != null && "".equals(_sysDirPath) == false )
		{
			file_path = _sysDirPath + "globalDataset.xml";
		}
		
		SAXBuilder oBuilder = new SAXBuilder();
		org.jdom2.Document oDoc = oBuilder.build(new File(file_path));
		org.jdom2.Element xmlRoot = oDoc.getRootElement(); // root element

		// parameter size가 0이면.  root의 list값을 json으로 변환한다.
		List nodeChildren = xmlRoot.getChildren("node");
		nodeChildren = getGlobalDatasetList(nodeChildren, _paramJO);	
		
		Attribute _attr;
		
		ArrayList<HashMap<String, Object>> _dataAr = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> _data = new HashMap<String, Object>();
		
//		JSONArray ja = new JSONArray();
//		JSONObject jo = new JSONObject();
//		JSONObject headerJo = new JSONObject();
		
		String _name=null;
		String _value=null;
		String _desc=null;
		
		if(nodeChildren == null) return _dataAr;
		
		int _nodeSize = nodeChildren.size();
		
		for(int i=0; i<_nodeSize; i++){
			org.jdom2.Element dataElement =(org.jdom2.Element) nodeChildren.get(i);
			
			_attr = dataElement.getAttribute("name"); 
			_name = _attr.getValue();
			
			_attr = dataElement.getAttribute("value"); 
			_value = _attr.getValue();
			
			_attr = dataElement.getAttribute("desc"); 
			if(_attr!=null)_desc = _attr.getValue();
			
			//json dataset를 만든다.
			_data.put(_name, _value);
//			headerJo.put(_name, _desc);
		}
//		ja.add(headerJo);
		_dataAr.add(_data);
		
		return _dataAr;
	}
	
	
	
	private List getGlobalDatasetList( List nodeChildren , HashMap<String, HashMap<String, String>> _hm )
	{
		List _dataList=null;
		
		List paramChildren=null;
		
		Attribute _attr;

		if( _hm.size() == 0 ){
			return nodeChildren;
		}
		
		String _name	=null;
		String _value	=null;
		String _desc	=null;
		
		for(int i=0; i<nodeChildren.size(); i++){
			org.jdom2.Element dataElement =(org.jdom2.Element) nodeChildren.get(i);
			
			_attr = dataElement.getAttribute("name"); 
			_name = _attr.getValue();
			
			_attr = dataElement.getAttribute("value"); 
			_value = _attr.getValue();
			
			_attr = dataElement.getAttribute("desc"); 
			if(_attr!=null)_desc = _attr.getValue();
			
				
			//일치하는 값을 찾는다.
			if( _hm.containsKey(_name) && _value.equals( _hm.get(_name).get("parameter") ) ){
				paramChildren = dataElement.getChildren("node");
				_hm.remove(_name);
				if( _hm.size() == 0 ){
					_dataList = paramChildren;
				}else{
					_dataList = getGlobalDatasetList(paramChildren, _hm);	
				}
				break;
			}
		}
		
		if( _dataList == null )
		{
			for(int i=0; i<nodeChildren.size(); i++){
				org.jdom2.Element dataElement =(org.jdom2.Element) nodeChildren.get(i);
				
				_attr = dataElement.getAttribute("name"); 
				_name = _attr.getValue();
				
				_attr = dataElement.getAttribute("value"); 
				_value = _attr.getValue();
				
				_attr = dataElement.getAttribute("desc"); 
				if(_attr!=null)_desc = _attr.getValue();
				
				//일치하는 값을 찾는다.
				if( _hm.containsKey(_name) && _value.equals( "DEFAULT" ) ){
					paramChildren = dataElement.getChildren("node");
					_hm.remove(_name);
					if( _hm.size() == 0 ){
						_dataList = paramChildren;
					}else{
						_dataList = getGlobalDatasetList(paramChildren, _hm);	
					}
					break;
				}
				
			}
		}
		
		return _dataList;
	}
	
    
}


