package org.ubstorm.service.parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.utils.common;
import org.xml.sax.SAXException;


public class xmlLoadParser {
	
	String _resultFileName = "";
	private xmlToUbForm mXmlToUBF;
	
	public xmlLoadParser() {
		// TODO Auto-generated constructor stub
	}

	// 1. json문자열을 받아와서 parser 처리
	// 2. xml을 저장할 파일 생성 ( 파일생성 경로 및 파일명 생성 규칙 필요 )
	// 3. 각 jsonObject 의 객체에서 문서경로 추출 
	// 4. xml을 읽어들인후 파일에 Append 처리 
	// 5. 문서 로드 완료 후 xml파싱
	// 6. Document 생성 후 리턴 
	
	public String mergeMultiFormFile( xmlToUbForm _xmlToUbform,HashMap<String, Object> _params,  String _multiFormStr, String _fileName , String _openerLocation,String _client_ssid, String _projectId) throws IOException, ParserConfigurationException, SAXException, DataFormatException
	{
		mXmlToUBF = _xmlToUbform;
		
		// MultiForm 가 담긴 Json문자열을 parsing하여 jsonArray로 컨버전
		_multiFormStr = _multiFormStr.replaceAll("\\\\\"", "\"");
		JSONArray _jrr = (JSONArray) JSONValue.parse( URLDecoder.decode( _multiFormStr,"UTF-8") );
		
		_resultFileName = mergeMultiXmlFileWriter(_jrr,_params, _fileName ,_openerLocation,_client_ssid, _projectId);
		
		return _resultFileName;
	}
	
	private String getXmlStr(String _projectName, String _formName, String _fileName , String _openerLocation,String _client_ssid, String _projectId ) throws IOException
	{
		String _realPath = common.getPropertyValue("ubform.realDir");
		String _realPathStr = "";
		if( _realPath != null && _realPath.equals("") == false && _openerLocation != null &&  _openerLocation.indexOf("/UEditor") == -1 )
		{
			_realPathStr = _realPath;
		}
		
		// projectName/formName에 맞춰서 프로젝트 로드
		File sFile = common.getProjectFilePath(_projectName, _formName, _fileName, _realPathStr ,"", "", _openerLocation, _client_ssid, _projectId );
		
		String line = null;
		int idx=0;
		FileInputStream is = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		
		StringBuffer strBuffer = new StringBuffer();
		
	 	if(sFile.isFile()){
	 		is = new FileInputStream(sFile);
	 		
	 		isr = new InputStreamReader(is);

	 		reader = new BufferedReader(isr);
	 		
	 		while((line = reader.readLine()) != null) {
	 	        if(idx == 0) {
	 	        	strBuffer.append(line);
	 	        }
	 	        else {
	 	        	strBuffer.append("\r\n").append(line);
	 	        }
	 	        idx++;
	 	    }   
	 	}
	 	
	 	return strBuffer.toString();
		
	}
	
	
	public String mergeMultiXmlFileWriter(JSONArray _jrr, HashMap<String, Object> _params, String _fileName , String _openerLocation,String _client_ssid, String _projectId ) throws IOException, ParserConfigurationException, SAXException, DataFormatException
	{
		StringBuffer strBuffer = new StringBuffer();
		
		ArrayList<JSONObject> _docParams = new ArrayList<JSONObject>();
		int _listCnt = _jrr.size();
		
		String _xml = "";
		HashMap<String, Object> _formInfo;
		String _openStr = "<projects xmlVersion=\"1.0\" pageHeight=\"1123\" >";
		String _endStr = "</projects>";
		ArrayList<HashMap<String, String>> _documentInfos = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> _tempMap;
		String EXPORT_DIR = common.getExportDirPath();

		
		
		Date _date = new Date();
		EXPORT_DIR = EXPORT_DIR != null && EXPORT_DIR.length() > 0 ? EXPORT_DIR : Log.ufilePath + "UFile/sys/temp/";
		File _directory = new File( EXPORT_DIR );
		if(!_directory.exists()){
			_directory.mkdirs(); 
		}
		
		String _xmlFileName = EXPORT_DIR + _client_ssid + "_" +  new SimpleDateFormat("yyyy-MM-dd HHmmssSSS").format(_date) +"_"+ Math.round((Math.random()*1000)) + ".xml";
		File _xmlFile = new File(_xmlFileName);
		
		if( _xmlFile.isFile() ) _xmlFile.delete();
		
		FileWriterWithEncoding _xmlFileWR = new FileWriterWithEncoding(_xmlFile, "UTF-8", true);
		
		_xmlFileWR.append(_openStr);
		
		for (int i = 0; i < _listCnt; i++) {
			_formInfo = (HashMap<String, Object>) _jrr.get(i);
			_tempMap = new HashMap<String, String>();
			
			_tempMap.put("PROEJCT", _formInfo.get("projectName").toString() );
			_tempMap.put("FORMID", _formInfo.get("formName").toString() );
			
			_xml = getXmlStr( _formInfo.get("projectName").toString(), _formInfo.get("formName").toString() , _fileName, _openerLocation, _client_ssid, _projectId );
			
			_xml = common.base64_decode_uncompress(_xml, "UTF-8");
			String[] lines = _xml.split("\n");
			
			for( int j = 0; j < lines.length; j++ )
			{
				_xmlFileWR.append(lines[j]).append("\n");
			}
			
			
			JSONObject _paramObj = new JSONObject();
			
			if( _formInfo.containsKey("parameter") )
			{
				JSONObject _param = (JSONObject) _formInfo.get("parameter");
				
				for (Object key : _param.keySet()) {
					String keyStr = (String)key;
					Object keyvalue = _param.get(keyStr);
					JSONObject _paramData = new JSONObject();
					_paramData.put("parameter", keyvalue);
					_paramData.put("type", "string");
					_paramObj.put(keyStr, _paramData);
				}
			}
			 
			_docParams.add( _paramObj );
			_documentInfos.add(_tempMap);
		}
		
		if(mXmlToUBF != null)
		{
			mXmlToUBF.setDocumentParams(_docParams);
			mXmlToUBF.setUseMultiFormType(true);
			mXmlToUBF.setDocumentInfos(_documentInfos);
		}
		
		_xmlFileWR.append(_endStr);
		
		_xmlFileWR.close();
		
		
		return _xmlFileName;
		
//		Document _doc;
//		// XML 파서의 XML 외부 개체와 DTD 처리를 비활성화 합니다.
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		factory.setExpandEntityReferences(false);
//		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//		
//		InputStream inputStream= new FileInputStream(_xmlFile);
//		Reader reader = new InputStreamReader(inputStream,"UTF-8");
//      
//		InputSource _is = new InputSource(reader);
//		_doc = factory.newDocumentBuilder().parse(_is);
	}
	

	
}
