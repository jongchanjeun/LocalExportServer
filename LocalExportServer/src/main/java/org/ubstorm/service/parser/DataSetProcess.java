package org.ubstorm.service.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
//import org.ubstorm.service.DataServiceManager;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.formparser.info.DataSetInfo;
import org.ubstorm.service.parser.queue.IQueue;
import org.ubstorm.service.parser.queue.JobDataSetQueue;
import org.ubstorm.service.parser.thread.JobDataSetConsumer;
import org.ubstorm.service.parser.thread.JobDataSetConsumerJson;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Data 통신 및 DataSet 생성 함수. 
 */
public class DataSetProcess {

	protected Logger log = Logger.getLogger(getClass());
	private ItemPropertyProcess mPropertyFn = new ItemPropertyProcess();
	
	/**
	 * xml의 DataSet 부분과 Viewer에서 받은 Param으로 Data를 가저 옴.
	 * 전체 데이터셋의 쿼리를 통해 전체 Row를 다 가져온다.
	 * ==> Multi Thread로 동작하도록 개선함 (jhlmr2)
	 * @throws Exception 
	 */
	public HashMap<String, ArrayList<HashMap<String, Object>>> dataSetLoad(String _serverUrl, HashMap<String, Element> _xmlDataSetItems, HashMap<String, HashMap<String, String>> _param , HashMap<String, Element> _dataSetMerged, String _clientEditMode ) throws Exception
	{
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _resultDataSet = new HashMap<String, ArrayList<HashMap<String, Object>>>();

		//큐를 생성한다.
		//final IQueue jobQueue = JobDataSetQueue.getInstance();
		final IQueue jobQueue = new JobDataSetQueue();
		ArrayList jobThreadList = new ArrayList();
		
		log.debug(getClass().getName() + "::" + " _xmlDataSetItems.size()=" + _xmlDataSetItems.size());
		
		// thread 생성숫자를 변경 ( Sap의 경우 쓰레드를 한번만 생성하도록 처리 )
		// 1. DataSet정보를 로드하여 Sap은 하나의 쓰레드로 묶어서 진행 
		
		// 2. Sap데이터셋처리 방법
		//	1-1 총 데이터셋을 리스트형태로 가지고 있도록 처리
		//	1-2 조회 후 Object형태일경우 데이터셋 리스트에 이름이 있을경우 담기
		//	1-3 조회 후 Array형태일경우 현재 데이터셋명으로 담고 다음 데이터 조회 진행		
		
		// 1) 데이터셋 정보를 미리 읽어들이기
		ArrayList<HashMap<String, Object>> _dSParams = new ArrayList<HashMap<String, Object>>();
		ArrayList<HashMap<String, Object>> _sapDSParams = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> _sapUDM = null;
		
		for(Entry<String, Element> entryChk : _xmlDataSetItems.entrySet())
		{
		    String key = entryChk.getKey();
		    Element _udm = entryChk.getValue();
		    
		    if( _udm != null)
			{
				HashMap<String, Object> param = new HashMap();
				param.put("resultDataSet", _resultDataSet);
				param.put("udm", _udm);
//				param.put("param", _param.clone());
//				param.put("dataService", _oservice);
				param.put("clientEditMode", _clientEditMode);
				param.put("serverUrl", _serverUrl);
			
			    NodeList _properties = _udm.getElementsByTagName("property");
			    
			    NodeList _paramEList = _udm.getElementsByTagName("params");
			    NodeList _columnEList = _udm.getElementsByTagName("columns");
			    Element _paramEl = null;
			    Element _columnEl = null;

			    HashMap<String, Object> _dataParam = new HashMap<String, Object>();
			    HashMap<String, String> _dataFieldAlias = new HashMap<String, String>();
			    
			    int _size = _properties.getLength();
			    String _fileType = "";
			    String _type = "";
			    for (int i = 0; i < _size; i++) {
					Element _propertiesEl = (Element) _properties.item(i);
					if( _propertiesEl.getAttribute("name") != null && _propertiesEl.getAttribute("name").toString().equals("fileType")  )
					{
						_fileType = _propertiesEl.getAttribute("value").toString();
						break;
					}
					else if(  _propertiesEl.getAttribute("name") != null && _propertiesEl.getAttribute("name").toString().equals("typeName")  )
					{
						_type = _propertiesEl.getAttribute("value").toString();
					}
					
					if( _fileType.equals("")==false && _type.equals("") == false ) break;
				}
			    
			    // 컬럼정보를 담아두기
			    if(_columnEList.getLength() > 0 )
			    {
			    	_columnEl = (Element) _columnEList.item(0);
			    	NodeList _columnNodes = _columnEl.getElementsByTagName("column");
			    	boolean _useFlag = true;
			    	String _columnName = "";
			    	String _columnValue = "";

			    	String _dataField = "";
			    	String _oriDataField = "";
			    	
			    	NodeList _columnProperties = null;
			    	Element _columnElement;
			    	
			    	for(int i=0; i < _columnNodes.getLength(); i++ )
			    	{
			    		_columnName = "";
			    		_columnProperties = ((Element)_columnNodes.item(i)).getElementsByTagName("property");
			    		
			    		for( int j = 0; j < _columnProperties.getLength(); j++)
			    		{
			    			_columnElement = (Element) _columnProperties.item(j);
			    			_columnName = _columnElement.getAttribute("name");
			    			_columnValue = _columnElement.getAttribute("value");
			    			
			    			if( _columnName.equals("dataField") )
			    			{
			    				_dataField = _columnValue;
			    			}
			    			else if( _columnName.equals("dataFieldOri") )
			    			{
			    				_oriDataField = _columnValue;
			    			}
			    		}
			    		
			    		if( _dataField.equals("") == false && _oriDataField.equals("") == false ) _dataFieldAlias.put(_oriDataField, _dataField);
			    	}
			    	
			    	param.put("DATAFILED_ALIAS", _dataFieldAlias);
			    }
	
			    if( _paramEList.getLength() > 0 )
			    {
			    	_paramEl = (Element) _paramEList.item(0);
			    	NodeList _paramProperties = _paramEl.getElementsByTagName("property");
			    	Element _paramElement;
			    	String _paramId = "";
			    	
			    	NodeList _paramNodes = _paramEl.getElementsByTagName("param");
			    	boolean _useFlag = true;
			    	
			    	//udm 데이터셋에서 사용중인 파라미터중 사용여부를 체크하여 사용하지 않는 파라미터일경우 담지 않도록 처리 2018-11-14 최명진
			    	for(int i=0; i < _paramNodes.getLength(); i++ )
			    	{
			    		_useFlag = true;
			    		_paramId = "";
			    		_paramProperties = ((Element)_paramNodes.item(i)).getElementsByTagName("property");
			    		
			    		for( int j = 0; j < _paramProperties.getLength(); j++)
			    		{
			    			_paramElement = (Element) _paramProperties.item(j);
			    			if( _paramElement.getAttribute("name").equals("id"))
				    		{
				    			_paramId = _paramElement.getAttribute("value");
				    		}
			    			else if( _paramElement.getAttribute("name").equals("useParam") && _paramElement.getAttribute("value").toString().equals("N") )
			    			{
			    				_useFlag = false;
			    			}
			    		}
			    		
			    		if( _useFlag && _param.containsKey(_paramId) )
			    		{
			    			_dataParam.put(_paramId, _param.get(_paramId).clone() );
			    		}
			    	}
			    	
			    	
//			    	for (int i = 0; i < _paramProperties.getLength(); i++) {
//			    		_paramElement = (Element) _paramProperties.item(i);
//			    		
//			    		if( _paramElement.getAttribute("name").equals("id"))
//			    		{
//			    			_paramId = _paramElement.getAttribute("value");
//			    			
//			    			if( _param.containsKey(_paramId) )
//			    			{
//			    				_dataParam.put(_paramId, _param.get(_paramId).clone() );
//			    			}
//			    		}
//					}
			    }
			    
			    // param데이터셋일경우 parameter에서 udm의 id값에 해당하는 parameter을 담아서 전달
			    if(_type.equals("param") && _param.containsKey( _udm.getAttribute("id").toString()) )
			    {
			    	_dataParam.put(_udm.getAttribute("id").toString() , _param.get( _udm.getAttribute("id").toString()) );
			    }
			    param.put("param", _dataParam);
			    
			    param.put("fileType", _fileType);
			    
			    if( _fileType.equals("sap"))
			    {
			    	if(_sapUDM == null) _sapUDM = param;
			    	_sapDSParams.add(param);
			    }
			    else
			    {
			    	_dSParams.add(param);
			    }
			}
		    
		}
		
		if( _sapUDM != null )
		{
			_sapUDM.put("children", _sapDSParams);
			_dSParams.add(_sapUDM);
		}
		
		
		// 소비자 쓰레드를 생성하고 시작한다.
//		for(int i=0; i< _xmlDataSetItems.size(); i++)
		for(int i=0; i< _dSParams.size(); i++)
		{
			Thread conThr = new Thread(new JobDataSetConsumer(jobQueue, Integer.toString(i)));
			conThr.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					// TODO Auto-generated method stub
					System.out.println(t.getName() + " throws exception: " + e);
					jobQueue.clear();
				}
			});
			
			// 데몸 스레드로 설정한다.
			if(!conThr.isDaemon()) conThr.setDaemon(true);
			conThr.start();
			
			jobThreadList.add(conThr);
		}
		
		/**
		for(Entry<String, Element> entry : _xmlDataSetItems.entrySet()) {
			
		    String key = entry.getKey();
		    Element _udm = entry.getValue();
		    
		    // 작업 쓰레드로 DataSet 처리 로직을 넘겨서 작업하도로 한다.
			if( _udm != null)
			{
				HashMap<String, Object> param = new HashMap();
				param.put("resultDataSet", _resultDataSet);
				param.put("udm", _udm);
				param.put("param", _param);
				param.put("dataService", _oservice);
				param.put("clientEditMode", _clientEditMode);
				
				jobQueue.put(param);
				
				Thread.sleep(10);		

			} // if( _udm != null)
			
		} // End of for		
		 */
		for (HashMap<String, Object> _params : _dSParams) {
			
			jobQueue.put(_params);
			
			Thread.sleep(10);	
		}
		
		
		// 큐에 있는 모든 작업이 완료될때 까지 기다린다.
		while(jobQueue.getJobCount() > 0)
		{
			Thread.sleep(100);
			//log.debug(getClass().getName() + "::toWord::\n" + "waiting for loadDataset completed!! jobCount=" + jobQueue.getJobCount());
		}
		jobQueue.clear();
		
		for(int i=0; i< jobThreadList.size(); i++ )
		{
			Thread conThr = (Thread) jobThreadList.get(i);
			if(conThr.isAlive()) conThr.interrupt();
		}
		jobThreadList.clear();
		
		if(_resultDataSet.containsKey("ERR_STACK"))
		{
			String err_msg = (String)_resultDataSet.get("ERR_STACK").get(0).get("ERR_MSG");
			throw new Exception(err_msg);
		}		
		
		if( _dataSetMerged.size() > 0 )
		{
			
			for(String key : _dataSetMerged.keySet()) 
			{
				
				ArrayList<HashMap<String, String>> _merged;
				
				Element _mergedElement = _dataSetMerged.get(key);
				
				_merged = getMergedProperty(_mergedElement);
				
				
				for (int i = 0; i < _merged.size(); i++) 
				{
					HashMap<String, String> _merg = _merged.get(i);
					
					String _sourceDataSet 			= _merg.get("sourceDataSet");
					String _sourceDataSetColumn 	= _merg.get("sourceDataSetColumn");
					
					String _targetDataSet 			= _merg.get("targetDataSet");
					String _targetDataSetColumn 	= _merg.get("targetDataSetColumn");
					
					String _mergedType				= _merg.get("type");
					String _operation 				= _merg.get("operation").replace("{", "").replace("}", "");
					
					String _fDataId = "";
					String _fDataColumn = "";
					
					String _tDataId = "";
					String _tDataColumn = "";		
					
						
					if( _mergedType.equals("Join") )
					{
						//join
						
						String _f = _operation.split("=")[0];
						String _t = _operation.split("=")[1];
						
						_fDataId = _f.split("\\.")[0];
						_fDataColumn = _f.split("\\.")[1];
						
						String[] _tS = _t.split("\\.");
						
						if( _tS.length == 1 )
						{
							_tDataId = _tS[0];
							_tDataColumn = "";
						}
						else
						{
							_tDataId = _tS[0];
							_tDataColumn = _tS[1];
						}
						
						
					}
						
					
					ArrayList<HashMap<String, Object>> _sdataSet = _resultDataSet.get(_sourceDataSet);
					ArrayList<HashMap<String, Object>> _tdataSet = _resultDataSet.get(_targetDataSet);
					
					ArrayList<HashMap<String, Object>> _oFDataSet = _resultDataSet.get(_fDataId);
					ArrayList<HashMap<String, Object>> _oTDataSet = _resultDataSet.get(_tDataId);
					
					for (int j = 0; j < _sdataSet.size(); j++) 
					{
						HashMap<String, Object> _sDataRow = _sdataSet.get(j);
						HashMap<String, Object> _tDataRow;
						
						if( _targetDataSetColumn.equals("") )
						{
							if( _oFDataSet != null )
							{
								if( _oFDataSet.size() <= j )
								{
									_sDataRow.put(_sourceDataSetColumn , "");
									continue;			
								}
								
								// join map
								HashMap<String, Object> _oFDataRow = _oFDataSet.get(j);
								HashMap<String, Object> _oTDataRow = _oTDataSet.get(0);
								
								if(  _oTDataRow.get(_oFDataRow.get(_fDataColumn)) == null )
								{
									_sDataRow.put(_sourceDataSetColumn , ""); 
									continue;
								}
								else
								{
									_sDataRow.put(_sourceDataSetColumn , _oTDataRow.get(_oFDataRow.get(_fDataColumn)));
								}
							}
							else
							{
								// 1:1 map 방식
								_sDataRow.put(_sourceDataSetColumn , "");
							}
								
						}
						else
						{
							
							if( _tdataSet == null )
							{
								_sDataRow.put(_sourceDataSetColumn , "");
								continue;							
							}
							
							if( _oFDataSet != null )
							{
								HashMap<String, Object> _oFDataRow = _oFDataSet.get(j);
								
								HashMap<String, Object> _oTDataRow;
								
								for (int k = 0; k < _oTDataSet.size(); k++) 
								{
									_oTDataRow = _oTDataSet.get(k);
									
									if( String.valueOf(_oFDataRow.get(_fDataColumn)).equals(String.valueOf(_oTDataRow.get(_tDataColumn))) )
									{
										_tDataRow = _tdataSet.get(k);
										_sDataRow.put(_sourceDataSetColumn , _tDataRow.get(_targetDataSetColumn));
										break;
									}
									else
									{
										_sDataRow.put(_sourceDataSetColumn , "");
									}
									
								}
							}
							else
							{
								_tDataRow = _tdataSet.get(j);
								_sDataRow.put(_sourceDataSetColumn , _tDataRow.get(_targetDataSetColumn));
							}
						}
						
					}
					
					
				}
				
			} // for _mergedCnt
			
		} // if _dataSetMerged
		
		
		
		return _resultDataSet;
	}
	
	
	/**
	 * xml의 DataSet 부분과 Viewer에서 받은 Param으로 Data를 가저 옴.
	 * 전체 데이터셋의 쿼리를 통해 전체 Row를 다 가져온다.
	 * ==> Multi Thread로 동작하도록 개선함 (jhlmr2)
	 * @throws Exception 
	 */
	public ArrayList<HashMap<String, Object>> dataSetLoadAndMakeCsvFile(String _sessionID, ArrayList<HashMap<String, Object>> _resultSet, HashMap<String, Element> _xmlDataSetItems, HashMap<String, HashMap<String, String>> _param , HashMap<String, Element> _dataSetMerged, String _clientEditMode, String _exportFilePath ) throws Exception
	{
		
		HashMap<String, ArrayList<HashMap<String, Object>>> _resultDataSet = new HashMap<String, ArrayList<HashMap<String, Object>>>();

		//큐를 생성한다.
		//final IQueue jobQueue = JobDataSetQueue.getInstance();
		final IQueue jobQueue = new JobDataSetQueue();
		ArrayList jobThreadList = new ArrayList();
		
		log.debug(getClass().getName() + "::" + " _xmlDataSetItems.size()=" + _xmlDataSetItems.size());
		
		// thread 생성숫자를 변경 ( Sap의 경우 쓰레드를 한번만 생성하도록 처리 )
		// 1. DataSet정보를 로드하여 Sap은 하나의 쓰레드로 묶어서 진행 
		
		// 2. Sap데이터셋처리 방법
		//	1-1 총 데이터셋을 리스트형태로 가지고 있도록 처리
		//	1-2 조회 후 Object형태일경우 데이터셋 리스트에 이름이 있을경우 담기
		//	1-3 조회 후 Array형태일경우 현재 데이터셋명으로 담고 다음 데이터 조회 진행		
		
		// 1) 데이터셋 정보를 미리 읽어들이기
		ArrayList<HashMap<String, Object>> _dSParams = new ArrayList<HashMap<String, Object>>();
		ArrayList<HashMap<String, Object>> _sapDSParams = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> _sapUDM = null;
		
		_resultDataSet.put("CSV_RESULT_SET", _resultSet);
		
		for(Entry<String, Element> entryChk : _xmlDataSetItems.entrySet())
		{
		    String key = entryChk.getKey();
		    Element _udm = entryChk.getValue();
		    
		    if( _udm != null)
			{
				HashMap<String, Object> param = new HashMap();
				
				param.put("resultDataSet", _resultDataSet);
				param.put("udm", _udm);
//				param.put("param", _param.clone());
//				param.put("dataService", _oservice);
				param.put("dataServiceSessionID", _sessionID);
				param.put("clientEditMode", _clientEditMode);
				param.put("exportFilePath", _exportFilePath);
				
			    NodeList _properties = _udm.getElementsByTagName("property");
			    
			    NodeList _paramEList = _udm.getElementsByTagName("params");
			    Element _paramEl = null;
			    HashMap<String, Object> _dataParam = new HashMap<String, Object>();
			    
			    int _size = _properties.getLength();
			    String _fileType = "";
			    String _type = "";
			    for (int i = 0; i < _size; i++) {
					Element _propertiesEl = (Element) _properties.item(i);
					if( _propertiesEl.getAttribute("name") != null && _propertiesEl.getAttribute("name").toString().equals("fileType")  )
					{
						_fileType = _propertiesEl.getAttribute("value").toString();
						break;
					}
					else if(  _propertiesEl.getAttribute("name") != null && _propertiesEl.getAttribute("name").toString().equals("typeName")  )
					{
						_type = _propertiesEl.getAttribute("value").toString();
					}
					
					if( _fileType.equals("")==false && _type.equals("") == false ) break;
				}
			    
			    if( _paramEList.getLength() > 0 )
			    {
			    	_paramEl = (Element) _paramEList.item(0);
			    	NodeList _paramProperties = _paramEl.getElementsByTagName("property");
			    	Element _paramElement;
			    	String _paramId = "";
			    	
			    	for (int i = 0; i < _paramProperties.getLength(); i++) {
			    		_paramElement = (Element) _paramProperties.item(i);
			    		
			    		if( _paramElement.getAttribute("name").equals("id"))
			    		{
			    			_paramId = _paramElement.getAttribute("value");
			    			
			    			_dataParam.put(_paramId, _param.get(_paramId).clone() );
			    		}
					}
			    }
			    
			    // param데이터셋일경우 parameter에서 udm의 id값에 해당하는 parameter을 담아서 전달
			    if(_type.equals("param") && _param.containsKey( _udm.getAttribute("id").toString()) )
			    {
			    	_dataParam.put(_udm.getAttribute("id").toString() , _param.get( _udm.getAttribute("id").toString()) );
			    }
			    param.put("param", _dataParam);
			    param.put("fileType", "csv");	// csv로 고정
	
			    _dSParams.add(param);
			}
		    
		}
		
		// 소비자 쓰레드를 생성하고 시작한다.
//		for(int i=0; i< _xmlDataSetItems.size(); i++)
		for(int i=0; i< _dSParams.size(); i++)
		{
			Thread conThr = new Thread(new JobDataSetConsumer(jobQueue, Integer.toString(i)));
			conThr.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					// TODO Auto-generated method stub
					System.out.println(t.getName() + " throws exception: " + e);
					jobQueue.clear();
				}
			});
			
			// 데몸 스레드로 설정한다.
			if(!conThr.isDaemon()) conThr.setDaemon(true);
			conThr.start();
			
			jobThreadList.add(conThr);
		}
		
		/**
		for(Entry<String, Element> entry : _xmlDataSetItems.entrySet()) {
			
		    String key = entry.getKey();
		    Element _udm = entry.getValue();
		    
		    // 작업 쓰레드로 DataSet 처리 로직을 넘겨서 작업하도로 한다.
			if( _udm != null)
			{
				HashMap<String, Object> param = new HashMap();
				param.put("resultDataSet", _resultDataSet);
				param.put("udm", _udm);
				param.put("param", _param);
				param.put("dataService", _oservice);
				param.put("clientEditMode", _clientEditMode);
				
				jobQueue.put(param);
				
				Thread.sleep(10);		

			} // if( _udm != null)
			
		} // End of for		
		 */
		for (HashMap<String, Object> _params : _dSParams) {
			
			jobQueue.put(_params);
			
			Thread.sleep(10);	
		}
		
		
		// 큐에 있는 모든 작업이 완료될때 까지 기다린다.
		while(jobQueue.getJobCount() > 0)
		{
			Thread.sleep(100);
			//log.debug(getClass().getName() + "::toWord::\n" + "waiting for loadDataset completed!! jobCount=" + jobQueue.getJobCount());
		}
		jobQueue.clear();
		
		for(int i=0; i< jobThreadList.size(); i++ )
		{
			Thread conThr = (Thread) jobThreadList.get(i);
			if(conThr.isAlive()) conThr.interrupt();
		}
		jobThreadList.clear();
		
		if(_resultDataSet.containsKey("ERR_STACK"))
		{
			String err_msg = (String)_resultDataSet.get("ERR_STACK").get(0).get("ERR_MSG");
			throw new Exception(err_msg);
		}		
		
		if(_resultDataSet.containsKey("CSV_RESULT_SET"))
		{
			_resultSet = (ArrayList<HashMap<String, Object>>)_resultDataSet.get("CSV_RESULT_SET");
		}
		
		return _resultSet;
	}

	/**
	 * xml의 DataSet 부분과 Viewer에서 받은 Param으로 Data를 가저 옴.
	 * 특정 데이터셋의 쿼리를 통해 정해진 Row 수 만큼만 데이터를 가져온다.
	 */
	public ArrayList<HashMap<String, Object>> dataSetLoadData(Element _udm , HashMap<String, HashMap<String, String>> _param, int rowStartIndex, int fetchRowCount)
	{
		ArrayList<HashMap<String, Object>> _arList = new ArrayList<HashMap<String,Object>>();
		if( _udm != null)
		{
			try {

				HashMap<String, Object> _hmItem = mPropertyFn.changeElementToHashMap(_udm);

				String _type = String.valueOf(_hmItem.get("typeName"));


				if( !_type.equals("ubi") )
				{
					if( _type.equals("File"))
					{
						return _arList;
					}
					_arList = connection(_hmItem, _param, rowStartIndex, fetchRowCount);
				}
				else
				{
					// Local EXE에서는 암것두 안한다.
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

		return _arList;
	}
	
	
	private String mRetStr = "";
	
	private ArrayList<HashMap<String, Object>> connection( HashMap<String, Object> _hm , HashMap<String, HashMap<String, String>> _param, int rowStartIndex, int fetchRowCount) throws Exception
	{
		URL _url = null;
		URLConnection _urlConnection = null;
		InputStream _inputS = null;
		InputStreamReader _inputR = null;
		BufferedReader _buf = null;

		ArrayList<HashMap<String, Object>> _data = new ArrayList<HashMap<String, Object>>();
		
		String _type = String.valueOf(_hm.get("typeName"));
		
		String _urlStr = String.valueOf(_hm.get("url"));
		if(!(_type.equals("user") || _type.equals("param"))  && "null".equals(_urlStr))
			return _data;
		
		String _colType = String.valueOf(_hm.get("paramEvent"));
		
		boolean _firstRow = Boolean.valueOf(String.valueOf(_hm.get("firstRow")));
		String _timeOutStr = common.getPropertyValue("dataOption.httpTimeOut");
		int _timeOut = (_timeOutStr==null)?300:Integer.valueOf(_timeOutStr) ;
		
		
		String _dataSetUrl = Log.serverContext != null && Log.serverContext.length() > 0 ? Log.dataSetURL + Log.serverContext :  Log.dataSetURL;
		String _sURL = _urlStr.indexOf("http://") != -1 ? _urlStr : _dataSetUrl + _urlStr;
		String _filepath = "";
		String _locahhostPath = "".equals(Log.serverPort) ? "http://localhost" : "http://localhost:" + Log.serverPort;	
		if(_sURL.indexOf(Log.rootURL) != -1)
		{
			_filepath = Log.basePath + _sURL.substring(_sURL.indexOf(Log.rootURL) + Log.rootURL.length() + 1);
			log.info(getClass().getName() + "::connection()" + " type=local file. path=" + _filepath); 
		}
		else if(_sURL.indexOf(_locahhostPath) != -1)
		{
			if(Log.serverContext != null && Log.serverContext.length() > 0)
				_locahhostPath = _locahhostPath + "/" + Log.serverContext;
				
			_filepath = Log.basePath + _sURL.substring(_sURL.indexOf(_locahhostPath) + _locahhostPath.length() + 1);
			log.info(getClass().getName() + "::connection()" + " type=local file. path=" + _filepath);  
		}
		else
		{
			_filepath = "";
			log.info(getClass().getName() + "::connection()" + " type=remote file. path=" + _sURL); 
		}  
		
		// 파라미터 셋팅
		
		String _pString = "";
		int _pC = 0;
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
		
		try {
			if( _type.equals("user"))
			{
				Element _xmlItem = (Element) _hm.get("element");
				
				NodeList _dataTag = _xmlItem.getElementsByTagName("data");
				
				Element _dataS = (Element) _dataTag.item(0);
						
				String _dataStr = common.base64_decode_uncompress(_dataS.getTextContent(), "UTF-8");
				
				if( !_dataStr.equals(""))
				{
					ArrayList<HashMap<String, Object>> _tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse(_dataStr);
					
					int _dataListLength = fetchRowCount != -1 ? fetchRowCount : _tmpdata.size();
					int _startRow = rowStartIndex != -1 ? rowStartIndex : 0;
					
					log.debug(getClass().getName() + "::connection()-user " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);  
					
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
			else
			{
				if( _type.equals("Post"))
				{
					_url = new URL(_sURL);
					_urlConnection = _url.openConnection();
					_urlConnection.setConnectTimeout(_timeOut*1000);
					_urlConnection.setReadTimeout(_timeOut*1000);
					_urlConnection.setDoOutput(true);
//					printToOutputStream(_urlConnection.getOutputStream(),"msg입니다.");
//					printToInputStream(_urlConnection.getInputStream());
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
//					printToInputStream(_urlConnection.getInputStream());
				}
				
				_inputS = _urlConnection.getInputStream();
				_inputR = new InputStreamReader(_inputS , "UTF-8");
				
				_buf = new BufferedReader(_inputR);

				String _str = null;
				String resultStr = "";
				StringBuilder resultStrb = new StringBuilder();
				
				if( _type.equals("Http"))
				{
					ArrayList<HashMap<String, Object>> _tmpdata = new ArrayList<HashMap<String, Object>>();
					
					String _delimiter = changeDelimiter(String.valueOf(_hm.get("delimiter")));
					
					while(true)
					{
						_str = _buf.readLine();
						if( _str == null) break;
						
						_tmpdata =	setCsvDataSetMapping(_str.split(_delimiter) , _tmpdata );
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
					
					log.debug(getClass().getName() + "::connection()-Http " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);    
					
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
				else if( _type.equals("json"))
				{
					ArrayList<HashMap<String, Object>> _tmpdata = new ArrayList<HashMap<String, Object>>();
					
					if("".equals(_filepath) )
					{	
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
					if( resultStr.substring(0,1).equals("["))
					{
						_tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse(resultStr);
					}
					else
					{
						_tmpdata = (ArrayList<HashMap<String,Object>>) JSONValue.parse("[" + resultStr);
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
					
					log.debug(getClass().getName() + "::connection()-JSON " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);    
					
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
						
						InputSource _is = new InputSource(new StringReader(resultStr));
						Document _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
						
						String _rootE = String.valueOf(_hm.get("rootElement"));
						String _dataE = String.valueOf(_hm.get("dataElement"));
						
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
							
							log.debug(getClass().getName() + "::connection()-xmlObj " + " fetchRowCount=" + _dataListLength + ",rowStartIndex=" + _startRow);    
							
							if( rowStartIndex >= _dataList.getLength()) 
							{
								return _data;
							}
							
							for (int j = _startRow; j < _startRow + _dataListLength; j++) 
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
		
		return _data;
	}
	
	private int getConnectionRowCount( HashMap<String, Object> _hm , HashMap<String, HashMap<String, String>> _param) throws Exception
	{
		URL _url = null;
		URLConnection _urlConnection = null;
		InputStream _inputS = null;
		InputStreamReader _inputR = null;
		BufferedReader _buf = null;

		ArrayList<HashMap<String, Object>> _data = new ArrayList<HashMap<String, Object>>();
		int _rowCount = 0;
		
		String _type = String.valueOf(_hm.get("typeName"));
		
		String _urlStr = String.valueOf(_hm.get("url"));
		if(!(_type.equals("user") || _type.equals("param"))  && "null".equals(_urlStr))
			return 0;

		String _colType = String.valueOf(_hm.get("paramEvent"));
		
		boolean _firstRow = Boolean.valueOf(String.valueOf(_hm.get("firstRow")));
		
		String _dataSetUrl = Log.serverContext != null && Log.serverContext.length() > 0 ? (_urlStr.startsWith(Log.serverContext) ? Log.dataSetURL : Log.dataSetURL + Log.serverContext) :  Log.dataSetURL;
		String _sURL = _urlStr.indexOf("http://") != -1 ? _urlStr : _dataSetUrl + _urlStr;
		String _filepath = "";
		String _locahhostPath = "".equals(Log.serverPort) ? "http://localhost" : "http://localhost:" + Log.serverPort;	
		if(_sURL.indexOf(Log.rootURL) != -1)
		{
			_filepath = Log.basePath + _sURL.substring(_sURL.indexOf(Log.rootURL) + Log.rootURL.length() + 1);
			log.info(getClass().getName() + "::connection()" + " type=local file. path=" + _filepath); 
		}
		else if(_sURL.indexOf(_locahhostPath) != -1)
		{
			if(Log.serverContext != null && Log.serverContext.length() > 0)
				_locahhostPath = _locahhostPath + "/" + Log.serverContext;
				
			_filepath = Log.basePath + _sURL.substring(_sURL.indexOf(_locahhostPath) + _locahhostPath.length() + 1);
			log.info(getClass().getName() + "::connection()" + " type=local file. path=" + _filepath);  
		}
		else
		{
			_filepath = "";
			log.info(getClass().getName() + "::connection()" + " type=remote file. path=" + _sURL); 
		}  
		
		log.info(getClass().getName() + "::getConnectionRowCount()" + " _filepath=" + _filepath);    
		 
		// 파라미터 셋팅
		
		String _pString = "";
		int _pC = 0;
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
				if( _value.equalsIgnoreCase("") ){
					_pString += "&" + _keySet + "=" + "\'\'";
				}else{
					_pString += "&" + _keySet + "=" + _value;	
				}
				
			}
			_pC++;
		}
		
		try {
			
			if( _type.equals("user"))
			{
				Element _xmlItem = (Element) _hm.get("element");
				NodeList _dataTag = _xmlItem.getElementsByTagName("data");
				Element _dataS = (Element) _dataTag.item(0);
						
				String _dataStr = common.base64_decode_uncompress(_dataS.getTextContent(), "UTF-8");
				if( !_dataStr.equals(""))
				{
					_data = (ArrayList<HashMap<String,Object>>) JSONValue.parse(_dataStr);
				}
				_rowCount = _data.size();
			}
			else
			{
				if( _type.equals("Post"))
				{
					_url = new URL(_sURL);
					_urlConnection = _url.openConnection();
					_urlConnection.setDoOutput(true);
					OutputStreamWriter wr = new OutputStreamWriter(_urlConnection.getOutputStream());
					wr.write(_pString);
					wr.flush();
				}
				else
				{
					_url = new URL(_sURL + "?" + _pString);
					_urlConnection = _url.openConnection();
				}
				
				_inputS = _urlConnection.getInputStream();
				_inputR = new InputStreamReader(_inputS , "UTF-8");
				
				_buf = new BufferedReader(_inputR);
				
				String _str = null;
				String resultStr = "";
				StringBuilder resultStrb = new StringBuilder();
				
				if( _type.equals("Http"))
				{
					String _delimiter = changeDelimiter(String.valueOf(_hm.get("delimiter")));
					
					while(true)
					{
						_str = _buf.readLine();
						if( _str == null) break;
						_data =	setCsvDataSetMapping(_str.split(_delimiter) , _data );						
					}
					
					_rowCount = _data.size();
				}
				else if( _type.equals("json"))
				{
					if("".equals(_filepath) )
					{	
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
					if( resultStr.substring(0,1).equals("["))
					{
						_data = (ArrayList<HashMap<String,Object>>) JSONValue.parse(resultStr);
					}
					else
					{
						_data = (ArrayList<HashMap<String,Object>>) JSONValue.parse("[" + resultStr);
					}
					_rowCount = _data.size();
				}
				else if( _type.equals("xmlObj"))
				{
					if("".equals(_filepath) )
					{	
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
						InputSource _is = new InputSource(new StringReader(resultStr));
						Document _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
						
						String _rootE = String.valueOf(_hm.get("rootElement"));
						String _dataE = String.valueOf(_hm.get("dataElement"));
						
						NodeList _xmlDataList = _doc.getElementsByTagName(_rootE);
						
						// xml Project
						int _xmlDataListLength = _xmlDataList.getLength();
						for (int i = 0; i < _xmlDataListLength; i++) 
						{
							Element _childRoot = (Element) _xmlDataList.item(i);							
							NodeList _dataList = _childRoot.getElementsByTagName(_dataE);
							int _dataListLength = _dataList.getLength();
							
							_rowCount = _rowCount + _dataListLength;
						}
						
					}
				}
				
				if(_inputS != null)
					_inputS.close();
			}
			
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception(e);
		}
		
		log.info(getClass().getName() + "::getConnectionRowCount()" + " _rowCount=" + _rowCount);    
		
		return _rowCount;
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
    		return "\t";
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
    		return "^"; 
    	}
       
       return "";
    }
	
    
    private ArrayList<HashMap<String, String>> getMergedProperty( Element _dataSetMerged)
    {
    	ArrayList<HashMap<String, String>> _retList = new ArrayList<HashMap<String,String>>();
    	
    	HashMap<String, String> _hm;
    	
    	NodeList _mergedList = _dataSetMerged.getElementsByTagName("merged");
    	
    	for (int i = 0; i < _mergedList.getLength(); i++) 
    	{
    		Element _mergedProp = (Element) _mergedList.item(i);
    		NodeList _propertyList = _mergedProp.getElementsByTagName("property");
    		
    		_hm = new HashMap<String, String>();
    		for (int j = 0; j < _propertyList.getLength(); j++) 
    		{
    			Element _prop = (Element) _propertyList.item(j);
    			
    			String _pName = _prop.getAttribute("name");
    			String _pValue = _prop.getAttribute("value");
    			
    			_hm.put(_pName, _pValue);
			}
    		_retList.add(_hm);
		}
    	
    	return _retList;
    }
    
    
    //// JSON Parser 
 	/**
 	 * xml의 DataSet 부분과 Viewer에서 받은 Param으로 Data를 가저 옴.
 	 * 전체 데이터셋의 쿼리를 통해 전체 Row를 다 가져온다.
 	 * ==> Multi Thread로 동작하도록 개선함 (jhlmr2)
 	 * @throws Exception 
 	 */
 	public HashMap<String, ArrayList<HashMap<String, Object>>> dataSetLoadJson(String _serverUrl, HashMap<String, DataSetInfo> _xmlDataSetItems, HashMap<String, HashMap<String, String>> _param , HashMap<String, ArrayList<HashMap<String, String>>> _dataSetMerged, String _clientEditMode ) throws Exception
 	{
 		
 		HashMap<String, ArrayList<HashMap<String, Object>>> _resultDataSet = new HashMap<String, ArrayList<HashMap<String, Object>>>();

 		//큐를 생성한다.
 		final IQueue jobQueue = new JobDataSetQueue();
 		ArrayList jobThreadList = new ArrayList();
 		
 		log.debug(getClass().getName() + "::" + " _xmlDataSetItems.size()=" + _xmlDataSetItems.size());
 		
 		// thread 생성숫자를 변경 ( Sap의 경우 쓰레드를 한번만 생성하도록 처리 )
 		// 1. DataSet정보를 로드하여 Sap은 하나의 쓰레드로 묶어서 진행 
 		
 		// 2. Sap데이터셋처리 방법
 		//	1-1 총 데이터셋을 리스트형태로 가지고 있도록 처리
 		//	1-2 조회 후 Object형태일경우 데이터셋 리스트에 이름이 있을경우 담기
 		//	1-3 조회 후 Array형태일경우 현재 데이터셋명으로 담고 다음 데이터 조회 진행		
 		
 		// 1) 데이터셋 정보를 미리 읽어들이기
 		ArrayList<HashMap<String, Object>> _dSParams = new ArrayList<HashMap<String, Object>>();
 		ArrayList<HashMap<String, Object>> _sapDSParams = new ArrayList<HashMap<String, Object>>();
 		HashMap<String, Object> _sapUDM = null;
 		
 		for(Entry<String, DataSetInfo> entryChk : _xmlDataSetItems.entrySet())
 		{
 		    String key = entryChk.getKey();
 		    DataSetInfo _udm = entryChk.getValue();
 		    
 		    if( _udm != null)
 			{
 				HashMap<String, Object> param = new HashMap();
 				param.put("resultDataSet", _resultDataSet);
 				param.put("udm", _udm);
 				//param.put("dataService", _oservice);
 				param.put("serverUrl", _serverUrl);
 				param.put("clientEditMode", _clientEditMode);
 			
 			    HashMap<String, Object> _dataParam = new HashMap<String, Object>();
 			    
 			    String _fileType = "";
 			    String _type = "";
 			    
 			    if( _udm.getFileType() != null) _fileType	= _udm.getFileType();
 			    if( _udm.getTypeName() != null) _type 	= _udm.getTypeName();
 			    
 			    ArrayList<HashMap<String, String>> _params = _udm.getParams();
 			    
 			    if( _params != null)
 			    {
 			    	String _paramId = "";
 			    	
 			    	for(int i = 0; i < _params.size(); i++)
 			    	{
 			    		_paramId = _params.get(i).get("id");
 			    		_dataParam.put( _paramId, _param.get(_paramId).clone() );
 			    	}
 			    }
 			    
 			    // param데이터셋일경우 parameter에서 udm의 id값에 해당하는 parameter을 담아서 전달
 			    if(_type.equals("param") && _param.containsKey( _udm.getId() ) )
 			    {
 			    	_dataParam.put( _udm.getId() , _param.get( _udm.getId()  ) );
 			    }
 			    param.put("param", _dataParam);
 			    
 			    param.put("fileType", _fileType);
 			    
 			    if( _fileType.equals("sap"))
 			    {
 			    	if(_sapUDM == null) _sapUDM = param;
 			    	_sapDSParams.add(param);
 			    }
 			    else
 			    {
 			    	_dSParams.add(param);
 			    }
 			}
 		    
 		}
 		
 		if( _sapUDM != null )
 		{
 			_sapUDM.put("children", _sapDSParams);
 			_dSParams.add(_sapUDM);
 		}
 		
 		
 		// 소비자 쓰레드를 생성하고 시작한다.
// 		for(int i=0; i< _xmlDataSetItems.size(); i++)
 		for(int i=0; i< _dSParams.size(); i++)
 		{
 			Thread conThr = new Thread(new JobDataSetConsumerJson(jobQueue, Integer.toString(i)));
 			conThr.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
 				
 				@Override
 				public void uncaughtException(Thread t, Throwable e) {
 					// TODO Auto-generated method stub
 					System.out.println(t.getName() + " throws exception: " + e);
 					jobQueue.clear();
 				}
 			});
 			
 			// 데몸 스레드로 설정한다.
 			if(!conThr.isDaemon()) conThr.setDaemon(true);
 			conThr.start();
 			
 			jobThreadList.add(conThr);
 		}
 		
 		for (HashMap<String, Object> _params : _dSParams) {
 			jobQueue.put(_params);
 			Thread.sleep(10);	
 		}
 		
 		// 큐에 있는 모든 작업이 완료될때 까지 기다린다.
 		while(jobQueue.getJobCount() > 0)
 		{
 			Thread.sleep(100);
 		}
 		jobQueue.clear();
 		
 		for(int i=0; i< jobThreadList.size(); i++ )
 		{
 			Thread conThr = (Thread) jobThreadList.get(i);
 			if(conThr.isAlive()) conThr.interrupt();
 		}
 		jobThreadList.clear();
 		
 		if(_resultDataSet.containsKey("ERR_STACK"))
 		{
 			String err_msg = (String)_resultDataSet.get("ERR_STACK").get(0).get("ERR_MSG");
 			throw new Exception(err_msg);
 		}		
 		
 		if( _dataSetMerged.size() > 0 )
 		{
 			
 			for(String key : _dataSetMerged.keySet()) 
 			{
 				ArrayList<HashMap<String, String>> _merged = _dataSetMerged.get(key);
 				
 				for (int i = 0; i < _merged.size(); i++) 
 				{
 					HashMap<String, String> _merg = _merged.get(i);
 					
 					String _sourceDataSet 			= _merg.get("sourceDataSet");
 					String _sourceDataSetColumn 	= _merg.get("sourceDataSetColumn");
 					
 					String _targetDataSet 			= _merg.get("targetDataSet");
 					String _targetDataSetColumn 	= _merg.get("targetDataSetColumn");
 					
 					String _mergedType				= _merg.get("type");
 					String _operation 				= _merg.get("operation").replace("{", "").replace("}", "");
 					
 					String _fDataId = "";
 					String _fDataColumn = "";
 					
 					String _tDataId = "";
 					String _tDataColumn = "";		
 					
 						
 					if( _mergedType.equals("Join") )
 					{
 						//join
 						
 						String _f = _operation.split("=")[0];
 						String _t = _operation.split("=")[1];
 						
 						_fDataId = _f.split("\\.")[0];
 						_fDataColumn = _f.split("\\.")[1];
 						
 						String[] _tS = _t.split("\\.");
 						
 						if( _tS.length == 1 )
 						{
 							_tDataId = _tS[0];
 							_tDataColumn = "";
 						}
 						else
 						{
 							_tDataId = _tS[0];
 							_tDataColumn = _tS[1];
 						}
 					}
 					
 					ArrayList<HashMap<String, Object>> _sdataSet = _resultDataSet.get(_sourceDataSet);
 					ArrayList<HashMap<String, Object>> _tdataSet = _resultDataSet.get(_targetDataSet);
 					
 					ArrayList<HashMap<String, Object>> _oFDataSet = _resultDataSet.get(_fDataId);
 					ArrayList<HashMap<String, Object>> _oTDataSet = _resultDataSet.get(_tDataId);
 					
 					for (int j = 0; j < _sdataSet.size(); j++) 
 					{
 						HashMap<String, Object> _sDataRow = _sdataSet.get(j);
 						HashMap<String, Object> _tDataRow;
 						
 						if( _targetDataSetColumn.equals("") )
 						{
 							if( _oFDataSet != null )
 							{
 								if( _oFDataSet.size() <= j )
 								{
 									_sDataRow.put(_sourceDataSetColumn , "");
 									continue;			
 								}
 								
 								// join map
 								HashMap<String, Object> _oFDataRow = _oFDataSet.get(j);
 								HashMap<String, Object> _oTDataRow = _oTDataSet.get(0);
 								
 								if(  _oTDataRow.get(_oFDataRow.get(_fDataColumn)) == null )
 								{
 									_sDataRow.put(_sourceDataSetColumn , ""); 
 									continue;
 								}
 								else
 								{
 									_sDataRow.put(_sourceDataSetColumn , _oTDataRow.get(_oFDataRow.get(_fDataColumn)));
 								}
 							}
 							else
 							{
 								// 1:1 map 방식
 								_sDataRow.put(_sourceDataSetColumn , "");
 							}
 								
 						}
 						else
 						{
 							
 							if( _tdataSet == null )
 							{
 								_sDataRow.put(_sourceDataSetColumn , "");
 								continue;							
 							}
 							
 							if( _oFDataSet != null )
 							{
 								HashMap<String, Object> _oFDataRow = _oFDataSet.get(j);
 								
 								HashMap<String, Object> _oTDataRow;
 								
 								for (int k = 0; k < _oTDataSet.size(); k++) 
 								{
 									_oTDataRow = _oTDataSet.get(k);
 									
 									if( String.valueOf(_oFDataRow.get(_fDataColumn)).equals(String.valueOf(_oTDataRow.get(_tDataColumn))) )
 									{
 										_tDataRow = _tdataSet.get(k);
 										_sDataRow.put(_sourceDataSetColumn , _tDataRow.get(_targetDataSetColumn));
 										break;
 									}
 									else
 									{
 										_sDataRow.put(_sourceDataSetColumn , "");
 									}
 									
 								}
 							}
 							else
 							{
 								_tDataRow = _tdataSet.get(j);
 								_sDataRow.put(_sourceDataSetColumn , _tDataRow.get(_targetDataSetColumn));
 							}
 						}
 					}
 					
 					
 				}
 				
 			} // for _mergedCnt
 			
 		} // if _dataSetMerged
 		
 		return _resultDataSet;
 	}
}
