package org.ubstorm.service.function;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.utils.StringUtil;



public class Function {

	private final String IS_NOT_FUNCTION="is_not_function";

	/**
	 * dataset list
	 * */
	private HashMap<String, ArrayList<HashMap<String, Object>>> mDatasetList;
	
	/**
	 * dataset
	 * */
	private ArrayList<HashMap<String, Object>> mDataset;
	
	public  void setDatasetList(HashMap<String, ArrayList<HashMap<String, Object>>> _datasetList) {
		mDatasetList = _datasetList;
	}
	
	private HashMap<String, Object> mParam;
	
	// GroupBand의 총 그룹수를 담고있는 객체 { bandID : 그룹총 카운트 , band2 ID: 그룹 총 카운트 ... } 
	private HashMap<String, Integer> mGroupBandCntMap;
	
	public HashMap<String, Integer> getGroupBandCntMap() {
		return mGroupBandCntMap;
	}

	public void setGroupBandCntMap(HashMap<String, Integer> mGroupBandCntMap) {
		this.mGroupBandCntMap = mGroupBandCntMap;
	}

	public HashMap<String, Object> getParam() {
		return mParam;
	}

	public void setParam(HashMap<String, Object> _param) {
		mParam = _param;
	}
	
	
	/**
	 * group 처리 변수
	 * 
	 * */
	
	public HashMap<String, String> mOriginalDataMap = null;		// originalData값으 가지고 있는 객체
	
	public void setOriginalDataMap(HashMap<String, String> _originalDataMap) {
		mOriginalDataMap = _originalDataMap;
	}

	public HashMap<String, String> getOriginalDataMap() {
		return mOriginalDataMap;
	}
	
	public ArrayList<ArrayList<String>> mGroupDataNamesAr = null;	// 그룹핑된 데이터명을 가지고 있는 객체
	
	public void setGroupDataNamesAr(ArrayList<ArrayList<String>> _groupDataNamesAr) {
		mGroupDataNamesAr = _groupDataNamesAr;
	}

	public ArrayList<ArrayList<String>> getGroupDataNamesAr() {
		return mGroupDataNamesAr;
	}
	
	public int mGroupCurrentPageIndex	= 0;
	
	public void setGroupCurrentPageIndex( int _groupCurrentPageIndex ) {
		mGroupCurrentPageIndex = _groupCurrentPageIndex;
	}
	
	public int mGroupTotalPageIndex = 0;

	public void setGroupTotalPageIndex( int _groupTotalPageIndex ) {
		mGroupTotalPageIndex = _groupTotalPageIndex;
	}
	
	/**
	 * 참조할 data의 row index
	 * */
	private int mRowIndex=0;
	
	/**
	 * 전체 페이지 count
	 * */
	private int mTotalPageNum=0;

	/**
	 * 현재 페이지 count
	 * */
	private int mCurrentPageNum=0;
	
	/**
	 * clone page의 현재 인덱스 값
	 */
	private int mCloneIndex = 0;
	
	
	
	/**
	 * 현재 페이지 Section count
	 * */
	private int mSectionCurrentPageNum=0;
	
	
	private int mSectionTotalPageNum=0;
	
	
	// band 처리 관련 속성
	private int mStartIndex	=-1;
	private int mLastIndex		=-1;
	private String mGroupDataName="";
	
	
	private final String REPLACEMENT_TEXT_1="§UF1§";
	private final String REPLACEMENT_TEXT_2="§UF2§";
	private final String REPLACEMENT_TEXT_3="§UF3§";
	private final String REPLACEMENT_TEXT_4="§UF4§";
	
	
	private String replacementOldChar( String _value )
	{
		String _result=_value;
		
		_result = _result.replaceAll("\"-\"", REPLACEMENT_TEXT_1);
		_result = _result.replaceAll("\"/\"", REPLACEMENT_TEXT_2);

		_result = _result.replaceAll("\"<\"", REPLACEMENT_TEXT_3);
		_result = _result.replaceAll("\">\"", REPLACEMENT_TEXT_4);
		
		return _result;
	}
	
	private String replacementNewChar( String _value )
	{
		String _result=_value;
		
		_result = _result.replaceAll(REPLACEMENT_TEXT_1, "-");
		_result = _result.replaceAll(REPLACEMENT_TEXT_2, "/");
		
		_result = _result.replaceAll(REPLACEMENT_TEXT_3, "<");
		_result = _result.replaceAll(REPLACEMENT_TEXT_4, ">");
		
		return _result;
	}
	
	
	 // create a script engine manager
    ScriptEngineManager factory;
    // create a JavaScript engine
    ScriptEngine engine;
	
    Function2 mFn2;
	
	
	/**
	 * 생성자: dataset or 변수는 받아둔다.
	 * Eval Class 초기화
	 * */
	public Function( HashMap<String, ArrayList<HashMap<String, Object>>> _datasetList  , HashMap<String, Object> _param) 
	{
		mDatasetList	=_datasetList;
		mParam		=_param;
		
		 // create a script engine manager
        factory = new ScriptEngineManager();
        // create a JavaScript engine
        engine = factory.getEngineByName("JavaScript");
		
		
        mFn2 = new Function2();
		
	}
	
	public String function( String _value  , int _rowIndex , int _totalpageNum , int _currentPageNum , int _startIndex , int _lastIndex )
	{
		this.mRowIndex				=_rowIndex;
		this.mTotalPageNum		=_totalpageNum;
		this.mCurrentPageNum	=_currentPageNum;
		this.mStartIndex				=_startIndex;
		this.mLastIndex				=_lastIndex;
		
		String _result="";
		
		if( _value.startsWith("UBF") ){
			_value = _value.substring(4, _value.length()-1 );
		}
		
		_value = replaceParameterValueFn(_value);
		
		String _temp = replacementOldChar( _value );
		
		_result = fn(_temp);
		
		_result = replacementNewChar( _result );
		
		return _result;
	}
	
	public String function( String _value  , int _rowIndex , int _totalpageNum , int _currentPageNum , int _startIndex , int _lastIndex , String _groupDataName )
	{
		this.mRowIndex				=_rowIndex;
		this.mTotalPageNum		=_totalpageNum;
		this.mCurrentPageNum	=_currentPageNum;
		this.mStartIndex				=_startIndex;
		this.mLastIndex				=_lastIndex;
		this.mGroupDataName		=_groupDataName;

		String _result="";
		
		if( _value.startsWith("UBF") ){
			_value = _value.substring(4, _value.length()-1 );
		}
		
		_value = replaceParameterValueFn(_value);
		
		String _temp = replacementOldChar( _value );
		
		_result = fn(_temp);
		
		_result = replacementNewChar( _result );

		return _result;
	}
	
	private String fn( String _value )
	{
		String _result="";
		String _clone=_value.toString();
		_result = getFunctionProcess( _clone );
		if( _result == IS_NOT_FUNCTION ){
			_result = getDataSetProcess2( _clone );
		}
		String _type=getType(_result);  
		if( _type.equals("") == false && _type != VALUE_TYPE ){
//		if( _type.equals("") == false && _type.equals("value") == false ){ 
			
			if( _type == FUNCTION_TYPE ){
//			if( _type.equalsIgnoreCase("function") ){
				_result = fn( _result );	
			}else if( _type == DATASET_TYPE ){
//			}else if( _type.equalsIgnoreCase("dataset") ){
				_result = fn( _result );	
			}else if( _type == OPERATION_TYPE ){
//			}else if( _type.equalsIgnoreCase("operation") ){
				_result = getOperation( _result );	
			}else{
				_result = fn( _result );	 
			}
			
		}
		return _result;
	}
	
	/**
	 * 연산자 체크 변수
	 * */
	private final String[] operations={ "!=" , "==" , ">=" , "<=" , "<" , ">", "=" , "+"  , "'" , "\"",  "*"};
	
	
	
	private String getOperation( String _value )
	{
		//String[] _operations = { "!=" , "==" , ">=" , "<=" , "<" , ">", "=" , "+"  , "'" , "\""};
		String _operationName="";
		
		for( String _operation : operations ){
			if( _value.contains(_operation) ){
				_operationName = _operation;
				if( _operationName.equalsIgnoreCase("") == false ){
//					String[] _values=_value.split(_operationName);
					// operation 타입에 따라 분류하여 계산한다.
					//_value = _value.replace(_operationName, "");
					
					ScriptEngineManager mgr = new ScriptEngineManager();
				    ScriptEngine engine = mgr.getEngineByName("JavaScript");
				    
				    try {
				    	
				    	if( _value.indexOf("\"") > -1 ){
				    		
				    		
							if( _operationName.equalsIgnoreCase("+") ){
								// 연산자 앞 뒤, 공백은 모두 제거시킨다.
								// 유비스톰 + "  (" +연구소 + ")"
								// 유비스톰    ( 연구소  )
								// 유비스톰  (연구소)
								
								// 연산자를 먼저 찾고 연산자 앞에 공백을 제거

								// 연산자 찾기.
								int _operIdx=_value.indexOf(_operationName);
								
								while(_operIdx>-1)
								{
									String _char;
									int _braketIdx=-1;
									// 연산자 앞에 공백찾기. 무조건 찾는게 아니라, 공백이 아닌 문자가 나올때 까지만...
									int i=0;
									for( i=_operIdx-1; i>-1; i-- ){
										_char=_value.substring(i, i+1);
										if( _char.equals(" ") == false ){
											break;
										}
										_braketIdx = i;
									}
									
									String _frontStr="";
									// 공백 위치를 찾았으므로 공백을 제거한다.
									if(_braketIdx > -1 ){
										_frontStr =_value.substring(0, _braketIdx);
									}else{
										_frontStr =_value.substring(0, _operIdx);
									}
									
									i=0;
									String _sizeStr=_value.substring(_operIdx+1, _value.length());
									_braketIdx=-1;
									for( i=_operIdx+1; i<_value.length(); i++ ){
										_char=_value.substring(i, i+1);
										if( _char.equals(" ") == false ){
											break;
										}
										_braketIdx = i;
									}
									String _backStr="";
									// 공백 위치를 찾았으므로 공백을 제거한다.
									if(_braketIdx > -1 ){
										_backStr =_value.substring(_braketIdx+1, _value.length());
									}else{
										_backStr =_value.substring(_operIdx+1, _value.length());
									}
									_value = _frontStr + _backStr;
									
									_operIdx=_value.indexOf(_operationName);
								}
							}
							
							_value = _value.replace(_operationName, "");
				    		
				    	}else{
					    	Object _evalObject = engine.eval(_value);
					    	_value = _evalObject.toString();
				    	}
				    	

					} catch (ScriptException e) {
						
						if( _operationName.equalsIgnoreCase("+") ){
							// 연산자 앞 뒤, 공백은 모두 제거시킨다.
							// 유비스톰 + "  (" +연구소 + ")"
							// 유비스톰    ( 연구소  )
							// 유비스톰  (연구소)
							
							// 연산자를 먼저 찾고 연산자 앞에 공백을 제거

							// 연산자 찾기.
							int _operIdx=_value.indexOf(_operationName);
							
							while(_operIdx>-1)
							{
								String _char;
								int _braketIdx=-1;
								// 연산자 앞에 공백찾기. 무조건 찾는게 아니라, 공백이 아닌 문자가 나올때 까지만...
								int i=0;
								for( i=_operIdx-1; i>-1; i-- ){
									_char=_value.substring(i, i+1);
									if( _char.equals(" ") == false ){
										break;
									}
									_braketIdx = i;
								}
								
								String _frontStr="";
								// 공백 위치를 찾았으므로 공백을 제거한다.
								if(_braketIdx > -1 ){
									_frontStr =_value.substring(0, _braketIdx);
								}else{
									_frontStr =_value.substring(0, _operIdx);
								}
								
								i=0;
								String _sizeStr=_value.substring(_operIdx+1, _value.length());
								_braketIdx=-1;
								for( i=_operIdx+1; i<_value.length(); i++ ){
									_char=_value.substring(i, i+1);
									if( _char.equals(" ") == false ){
										break;
									}
									_braketIdx = i;
								}
								String _backStr="";
								// 공백 위치를 찾았으므로 공백을 제거한다.
								if(_braketIdx > -1 ){
									_backStr =_value.substring(_braketIdx+1, _value.length());
								}else{
									_backStr =_value.substring(_operIdx+1, _value.length());
								}
								_value = _frontStr + _backStr;
								
								_operIdx=_value.indexOf(_operationName);
							}
						}
						
						_value = _value.replace(_operationName, "");
					}
					
				}
			}
		}
		return _value;
	}
	
	
	
	private final String[] operations2={ "!=" , "==" , ">=" , "<=" , "<" , ">", "=" , "+"  , "'" , "\"", "-", "*", "/", "%"};
	
	private String getOperation2( String _value )
	{
		//String[] _operations = { "!=" , "==" , ">=" , "<=" , "<" , ">", "=" , "+"  , "'" , "\""};
		String _operationName="";
		
		for( String _operation : operations2 ){
			if( _value.contains(_operation) ){
				_operationName = _operation;
				if( _operationName.equalsIgnoreCase("") == false ){
//					String[] _values=_value.split(_operationName);
					// operation 타입에 따라 분류하여 계산한다.
					//_value = _value.replace(_operationName, "");
					
					ScriptEngineManager mgr = new ScriptEngineManager();
					ScriptEngine engine = mgr.getEngineByName("JavaScript");
					
					try {
						// --를 제거하기 위해 
						while( _value.indexOf("--") != -1)
						{
							_value = _value.replaceAll("--", "+");
						}
						Object _evalObject = engine.eval(_value);
						
						_value = _evalObject.toString();
						if( _value.equalsIgnoreCase("-Infinity") || _value.equalsIgnoreCase("Infinity") || _value.equalsIgnoreCase("NaN") ){
							_value = "0";
						}
						else
						{
							BigDecimal _bigDeciaml = new BigDecimal(_evalObject.toString());
							_value = _bigDeciaml.toEngineeringString();
						}
						
					} catch (ScriptException e) {
						
						if( _operationName.equalsIgnoreCase("+") ){
							// 연산자 앞 뒤, 공백은 모두 제거시킨다.
							// 유비스톰 + "  (" +연구소 + ")"
							// 유비스톰    ( 연구소  )
							// 유비스톰  (연구소)
							
							// 연산자를 먼저 찾고 연산자 앞에 공백을 제거
							
							// 연산자 찾기.
							int _operIdx=_value.indexOf(_operationName);
							
							while(_operIdx>-1)
							{
								String _char;
								int _braketIdx=-1;
								// 연산자 앞에 공백찾기. 무조건 찾는게 아니라, 공백이 아닌 문자가 나올때 까지만...
								int i=0;
								for( i=_operIdx-1; i>-1; i-- ){
									_char=_value.substring(i, i+1);
									if( _char.equals(" ") == false ){
										break;
									}
									_braketIdx = i;
								}
								
								String _frontStr="";
								// 공백 위치를 찾았으므로 공백을 제거한다.
								if(_braketIdx > -1 ){
									_frontStr =_value.substring(0, _braketIdx);
								}else{
									_frontStr =_value.substring(0, _operIdx);
								}
								
								i=0;
								String _sizeStr=_value.substring(_operIdx+1, _value.length());
								_braketIdx=-1;
								for( i=_operIdx+1; i<_value.length(); i++ ){
									_char=_value.substring(i, i+1);
									if( _char.equals(" ") == false ){
										break;
									}
									_braketIdx = i;
								}
								String _backStr="";
								// 공백 위치를 찾았으므로 공백을 제거한다.
								if(_braketIdx > -1 ){
									_backStr =_value.substring(_braketIdx+1, _value.length());
								}else{
									_backStr =_value.substring(_operIdx+1, _value.length());
								}
								_value = _frontStr + _backStr;
								
								_operIdx=_value.indexOf(_operationName);
							}
						}
						
						_value = _value.replace(_operationName, "");
					}
					
				}
			}
		}
		return _value;
	}
	
	
	private final String VALUE_TYPE			="value";
	private final String FUNCTION_TYPE	="function";
	private final String DATASET_TYPE		="dataset";
	private final String OPERATION_TYPE	="operation";
	private final String PARAM_TYPE		="parameter";
	
	private String getType( String _value )
	{
		String _result=VALUE_TYPE;
		
		if( getBracketString(_value) == true ){
			_result=FUNCTION_TYPE;
		}else if( _value.contains("param:") ){
			_result=PARAM_TYPE;
		}else{
			
			Boolean _isDataset = hasDatasetArg(_value);
			if( _isDataset == true ){
				_result=DATASET_TYPE;
			}else{
				
				Boolean _hasOperation=false;
				String[] _operations = { "!=" , "==" , ">=" , "<=" , "<" , ">", "="   , "+"  , "-"  , "/"  , "*" };
				for( String _oper : _operations ){
					if( _value.contains(_oper) ){
						_hasOperation=true;
						break;
					}
				}
				if( _hasOperation == true ){
					_result=OPERATION_TYPE;
				}
			}
			
		}
		
		return _result;
	}
	
	
	private Boolean hasDatasetArg( String _clone )
	{
		Boolean _hasDataset=false;
		
		int _checkIndex=0;
		
		while( _hasDataset == false )
		{
			// 1. dataset을 찾는다.   comma
			int _commaIndex=_clone.indexOf("." , _checkIndex);
			
			_checkIndex = _commaIndex+1;
			
			if( _commaIndex < 0 ){
				break;
				//return _hasDataset;
			}
			
			//"1"
			if( _checkIndex >= _clone.length() ){
				break;
				//return _hasDataset;
			}
			
			// 공백, 시작괄호( , index 0 , 기타 구분자로 추가될 문자를 시작인덱스로 찾는다.
			int _startDatasetIndex=-1;
			
			// dataset name
			String _datasetName="";
			

			// 공백을 구분으로 dataset을 찾는다.
			_startDatasetIndex=_clone.lastIndexOf(" ", _commaIndex);
			
			// 공백 구분으로 dataset 시작 인덱스를 못 찾았다면.
			// 시작 인덱스를  시작 괄호문자로 찾는다.
			if( _startDatasetIndex < 0 ){
				_startDatasetIndex=_clone.lastIndexOf("(", _commaIndex);	
			}else{
				_datasetName = _clone.substring(_startDatasetIndex, _commaIndex);
				
				// dataset 찾기전에 공백 제거
				_datasetName = _datasetName.trim();
				
				// 찾은  datasetName이 맞는지 체크,  아니라면 다시 찾도록 변수 초기화
				_hasDataset = hasDataset( _datasetName );
				if(  _hasDataset == false ){
					_startDatasetIndex = -1;
				}

			}

			if( _hasDataset == false ){
				
				// 시작 괄호 구분으로 dataset 시작 인덱스를 못 찾았다면,
				// 인덱스 0을 시작 괄호로 지정한다.
				if( _startDatasetIndex < 0 ){
					_startDatasetIndex=0;
				}
				_datasetName=_clone.substring( _startDatasetIndex , _commaIndex);
				
				// dataset 찾기전에 공백 제거
				_datasetName = _datasetName.trim();

				// 찾은  datasetName이 맞는지 체크,  아니라면 다시 찾도록 변수 초기화
				_hasDataset = hasDataset( _datasetName );
				if(  _hasDataset == false ){
					_startDatasetIndex = -1;
				}
			}
		}
		
		
		
		return _hasDataset;
	}
	
	
	private int getDatasetCommaIndex( String _clone )
	{
		Boolean _hasDataset=false;
		
		// dataset name
		String _datasetName="";
		
		int _commaIndex=-1;
		
		int _checkIndex=0;
		
		while( _hasDataset == false )
		{
			// 1. dataset을 찾는다.   comma
			_commaIndex=_clone.indexOf("." , _checkIndex);
			
			_checkIndex = _commaIndex+1;
			
			if( _commaIndex < 0 ){
				break;
				//return _hasDataset;
			}
			
			//"1"
			if( _checkIndex >= _clone.length() ){
				break;
				//return _hasDataset;
			}
			
			// 공백, 시작괄호( , index 0 , 기타 구분자로 추가될 문자를 시작인덱스로 찾는다.
			int _startDatasetIndex=-1;
			
			
			

			// 공백을 구분으로 dataset을 찾는다.
			_startDatasetIndex=_clone.lastIndexOf(" ", _commaIndex);
			
			// 공백 구분으로 dataset 시작 인덱스를 못 찾았다면.
			// 시작 인덱스를  시작 괄호문자로 찾는다.
			if( _startDatasetIndex < 0 ){
				_startDatasetIndex=_clone.lastIndexOf("(", _commaIndex);	
			}else{
				_datasetName = _clone.substring(_startDatasetIndex, _commaIndex);
				
				// dataset 찾기전에 공백 제거
				_datasetName = _datasetName.trim();
				
				// 찾은  datasetName이 맞는지 체크,  아니라면 다시 찾도록 변수 초기화
				_hasDataset = hasDataset( _datasetName );
				if(  _hasDataset == false ){
					_startDatasetIndex = -1;
				}

			}

			if( _hasDataset == false ){
				
				// 시작 괄호 구분으로 dataset 시작 인덱스를 못 찾았다면,
				// 인덱스 0을 시작 괄호로 지정한다.
				if( _startDatasetIndex < 0 ){
					_startDatasetIndex=0;
				}
				_datasetName=_clone.substring( _startDatasetIndex , _commaIndex);
				
				// dataset 찾기전에 공백 제거
				_datasetName = _datasetName.trim();

				// 찾은  datasetName이 맞는지 체크,  아니라면 다시 찾도록 변수 초기화
				_hasDataset = hasDataset( _datasetName );
				if(  _hasDataset == false ){
					_startDatasetIndex = -1;
					_datasetName="";
				}
			}
		}
		
		if( _hasDataset == false ){
			return -1;
		}
		
		return _commaIndex;
	}
	
	
	
//	private String getParamDataValue( String _value )
//	{
//		String _result="";
//		
//		int _startIndex=_value.indexOf(".");
//		
//		String _pKey=_value.substring(_startIndex+1, _value.length());
//
//		HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_pKey);
//
//		_result = _pList.get("parameter");
//		
//		return _result;
//	}
	
	
	private String getParamProcess( String _value )
	{
		String _result="";
		
		int _startIndex=_value.indexOf(".");
		
		// dot을 기준으로 공백 , 연산자, 인덱스를 찾는다.
		String[] _operations={  "!=" , "==" , ">=" , "<=" , "<" , ">", "=" ," "  };
		String _operName="";
		for( String _operation :  _operations){
			if( _value.contains(_operation) ){
				_operName = _operation;
				break;
			}
		}
		
		if( _operName.equalsIgnoreCase("") == false ){
			int _paramKeyIndex = _value.indexOf(_operName,_startIndex);
			String _paramKey= _value.substring(_startIndex+1, _paramKeyIndex).trim(); 
			/* _paramKey에 trim 처리 추가. 2016-10-05 공혜지
			 * key 값과 연산자 사이에 공백이 있는경우 공백까지 인식되어 value 값을 가져오지 못하는 문제 발생. */
			HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_paramKey);
			String _paramValue = "";
			if(_pList != null) _paramValue = StringUtil.convertObjectToString(_pList.get("parameter"));
			
			int _paramStartIndex=_value.indexOf("param");
			String _oldStr=_value.substring(_paramStartIndex, _paramKeyIndex);

			_result =_value.replace(_oldStr, _paramValue);

		}
		
		
		return _result;
	}
	
	
	private String getDatasetProcess( String _clone )
	{
		String _result="";
		
		String _oldStr="";
		String _newStr="";
		
		// dataset process start
		
		// 1. dataset을 찾는다.   comma
		int _commaIndex=getDatasetCommaIndex(_clone);
		
//		int _commaIndex=_clone.indexOf(".");
		
		if( _commaIndex < 0 ){
			return _clone;
		}
		
		// 공백, 시작괄호( , index 0 , 기타 구분자로 추가될 문자를 시작인덱스로 찾는다.
		int _startDatasetIndex=-1;
		
		// dataset name
		String _datasetName="";
		Boolean _hasDataset=false;

		// 공백을 구분으로 dataset을 찾는다.
		_startDatasetIndex=_clone.lastIndexOf(" ", _commaIndex);
		
		// 공백 구분으로 dataset 시작 인덱스를 못 찾았다면.
		// 시작 인덱스를  시작 괄호문자로 찾는다.
		if( _startDatasetIndex < 0 ){
			_startDatasetIndex=_clone.lastIndexOf("(", _commaIndex);	
		}else{
			_datasetName = _clone.substring(_startDatasetIndex, _commaIndex);
			
			// dataset 찾기전에 공백 제거
			_datasetName = _datasetName.trim();
			
			// 찾은  datasetName이 맞는지 체크,  아니라면 다시 찾도록 변수 초기화
			_hasDataset = hasDataset( _datasetName );
			if(  _hasDataset == false ){
				_startDatasetIndex = -1;
			}

		}

		if( _hasDataset == false ){
			
			// 시작 괄호 구분으로 dataset 시작 인덱스를 못 찾았다면,
			// 인덱스 0을 시작 괄호로 지정한다.
			if( _startDatasetIndex < 0 ){
				_startDatasetIndex=0;
			}
			_datasetName=_clone.substring( _startDatasetIndex , _commaIndex);
			
			// dataset 찾기전에 공백 제거
			_datasetName = _datasetName.trim();

			// 찾은  datasetName이 맞는지 체크,  아니라면 다시 찾도록 변수 초기화
			_hasDataset = hasDataset( _datasetName );
			if(  _hasDataset == false ){
				_startDatasetIndex = -1;
			}
		}
		
		
		// dataset을 못 찾았다면 column을 찾을 필요가 없다.
		// dataset을 찾았을 경우 column을 찾는다.
		if( _hasDataset == true ){
			
			// dataset column name을 찾는다.
			int _lastColumnIndex=-1;
			
			String _columnName="";
			Boolean _hasColumn =false;
			
			// 공백 , 닫힘괄호) , lastIndex , 연산자
			_lastColumnIndex=_clone.indexOf(" ", _commaIndex );
			
			if( _lastColumnIndex < 0 ){
				_lastColumnIndex = _clone.indexOf(")" , _commaIndex );
			}else{
				_columnName = _clone.substring(_commaIndex+1, _lastColumnIndex);
				
				// column name 찾기 전에 공백 제거
				_columnName = _columnName.trim();
				
				_hasColumn =hasDatasetColumn(_datasetName, _columnName); 
				
				if( _hasColumn == false ){
					_lastColumnIndex = -1;
				}
				
			}
			
			
			if( _hasColumn == false ){
				
				// 종료 괄호 구분으로 못찼았다면 마지막 인덱스를 기준으로 찾는다.
				if( _lastColumnIndex < 0 ){
					_lastColumnIndex = _clone.length();
				}
				_columnName = _clone.substring(_commaIndex+1, _lastColumnIndex);
				// column name 찾기 전에 공백 제거
				_columnName = _columnName.trim();
				_hasColumn =hasDatasetColumn(_datasetName, _columnName); 
				if( _hasColumn == false ){
					_lastColumnIndex = -1;
				}
				
			}
			
//			if( _hasColumn == false ){
//				_lastColumnIndex = _clone.lastIndexOf(" ", _commaIndex);
//				_columnName = _clone.substring(_commaIndex+1, _lastColumnIndex);
//				// column name 찾기 전에 공백 제거
//				_columnName = _columnName.trim();
//				_hasColumn =hasDatasetColumn(_datasetName, _columnName); 
//			}
			
			
			
			// ***인덱스 결과값이 나왔더라도 column 이 아닐수가 있다는 것을 감안해야함.

			// 그래도 dataset column을 못찾았다면 연산자 구분으로 찾는다.
			if( _hasColumn == false ){
				String[] _operations2 = { "<" , ">", "=" , "!=" , "==" , ">=" , "<="  };
				for( String _operation : _operations2 ){
					
					_lastColumnIndex = _clone.indexOf( _operation  , _commaIndex );
					
					if( _lastColumnIndex < 0 ){
						continue;
					}else{
						
						_columnName = _clone.substring(_commaIndex+1, _lastColumnIndex);
						// column name 찾기 전에 공백 제거
						_columnName = _columnName.trim();
						_hasColumn =hasDatasetColumn(_datasetName, _columnName); 
						if( _hasColumn == false ){
							_lastColumnIndex = -1;
						}else{
							break;
						}
						
					}
					
				}
			}
			
			
			// dataset과 column을 모두 찾았다면 dataset value를 가져온다.
			if(  _hasDataset && _hasColumn ){
				_oldStr = _clone.substring(_startDatasetIndex, _lastColumnIndex);
				Object _datasetValue = getDataSetValue(_datasetName, _columnName);
				if( _datasetValue == null ){
					_newStr = "null";
				}else{
					_newStr = _datasetValue.toString();	
				}
				
				_clone = _clone.replace(_oldStr, _newStr);
			}
			else if( _hasDataset && !_hasColumn )
			{
				_clone = _clone.replace(_datasetName+"."+_columnName , "");
			}
		}
		_result = _clone;
		
		// dataset process end
		return _result;
	}
	
	
	
	private Boolean getBracketString( String _value )
	{
		Boolean _hasFunction=false;
		
		String _result="";
		
		int _startBracketIndex	=-1;
		int _endBracketIndex	=-1;
		
		_endBracketIndex	=	_value.indexOf(")");
		_startBracketIndex	=	_value.lastIndexOf("(", _endBracketIndex);

		if( _startBracketIndex > -1 && _endBracketIndex > _startBracketIndex ){
			_result = _value.substring(_startBracketIndex, _endBracketIndex+1);	
		}

		if( _result.equalsIgnoreCase("") == false ){
			
			//  (공백) , (콤마 , )  , ( 시작인덱스 0 ) ,  ( 시작괄호'(' ) , (끝괄호')' ) , (연산자) 
			String[] _targets={  "+", "," , "(" , ")" , "!=" , "==" , ">=" , "<=" , "<" , ">", "="  ," "  };
			String _fnName="";
			int _startFnIndex=-1;
			
			
			// 가장 많이 사용되는 케이스는 먼저 돌려본다. 시간 단축 용도
			_fnName = _value.substring(0, _startBracketIndex);
			_fnName = _fnName.trim();
			_hasFunction = hasFunction( _fnName );
			
			if( _hasFunction == false ){
				
				for( String _target : _targets ){
					
					_startFnIndex = _value.lastIndexOf(_target, _startBracketIndex);
					if( _startFnIndex > -1 ){
						
						if( _startFnIndex < _startBracketIndex ){
							_startFnIndex+=1;
						}
						
						_fnName = _value.substring(_startFnIndex, _startBracketIndex);
						_fnName = _fnName.trim();
						_hasFunction = hasFunction( _fnName );
						if( _hasFunction ==  true ){
							break;
						}
					}
					
				}
				
			}else{
				_startFnIndex=0;
			}
		}
		
		return _hasFunction;
	}
	
	private ArrayList<Integer> getFunctionPosition(String _value )
	{
		
		ArrayList<String> _targetAr = new ArrayList<String>();
		String[] _targets={ "+" , "," , "(" , ")" , "!=" , "==" , ">=" , "<=" , "<" , ">", "=" , " "  };
		ArrayList<Integer> _resultAr = new ArrayList<Integer>();
		_targetAr.addAll(Arrays.asList(_targets));
		
		int _startBracketIndex	=0;
		int _endBracketIndex	=-1;
		
		String _argoStr = "";
		int _chkStartPosition = -1;
		int _chkEndPosition = 0;
		int _argoPosition = -1;
		int _argoEndPosition = -1;
		
		// 반복하며 (문자 앞쪽의 문자가 특수문자인지 일반문자인지 체크하여 특수문자 일경우 함수가 아닌걸로 판단하여 그 앞쪽의 (문자를 다시 찾도록 지정
		
		while( _startBracketIndex > -1 )
		{
			_endBracketIndex	=	_value.indexOf(")",_chkEndPosition);
			
			if(_chkStartPosition == -1 ) _chkStartPosition = _endBracketIndex;
			
			_startBracketIndex	=	_value.lastIndexOf("(", _chkStartPosition);
			
			// 이전과 같은값이면 리턴
			if(_argoPosition == _startBracketIndex || _argoEndPosition == _endBracketIndex ) break;
			if( _startBracketIndex == 0 ) break;
			
			if( _startBracketIndex != -1 )
			{
				_argoStr = String.valueOf( _value.charAt(_startBracketIndex - 1) );
				
				if( _targetAr.indexOf( _argoStr ) != -1 )
				{
					_chkEndPosition = _endBracketIndex+1;
					_chkStartPosition = _startBracketIndex-1;
				}
				else
				{
					break;
				}
				
				_argoPosition 	= _startBracketIndex;
				_argoEndPosition = _endBracketIndex;
			}
			
		}
		
		_resultAr.add(_startBracketIndex);
		_resultAr.add(_endBracketIndex);
		
		return _resultAr;
	}
	
	
	//if( dataset.col1 > 100 , if( dataset.col1 > 100 , 200 , 300 ) , if( dataset.col1 > 100 , 200 , 300 ) )
	/**
	 * 1. 괄호 구문을 찾는다.
	 * ) 끝 괄호가 나오기 전까지의  ( 시작 괄호를 찾는다.
	 * 예외처리: 괄호가 없다면 연산자를 찾고, 이마저도 없다면 원래 값을 return 시킨다.
	 * */
	private String getFunctionProcess( String _value )
	{
		String _result="";
		String[] _targets={ "+" , "," , "(" , ")" , "!=" , "==" , ">=" , "<=" , "<" , ">", "=" , " "  };
		
		int _startBracketIndex	=-1;
		int _endBracketIndex	=-1;
		
		_endBracketIndex	=	_value.indexOf(")");
		_startBracketIndex	=	_value.lastIndexOf("(", _endBracketIndex);
		
//		ArrayList<Integer> _resultTest = getFunctionPosition( _value );
//		if( _resultTest != null )
//		{
//			_startBracketIndex 	= _resultTest.get(0);
//			_endBracketIndex 	= _resultTest.get(1);
//		}
		
		if( _startBracketIndex > -1 && _endBracketIndex > -1 && _endBracketIndex > _startBracketIndex ){
//		if( _endBracketIndex > _startBracketIndex ){
			_result = _value.substring(_startBracketIndex, _endBracketIndex+1);	
		}else{
			return IS_NOT_FUNCTION;
		}
		
		// 여기까지 괄호 문법 찾기.
		
		
		
		// 함수명 찾기
		// 함수명이 없고, 그냥 괄호만 사용되었을 수도 있다는 것을 감안해야 한다.
		if( _result.equalsIgnoreCase("") == false ){
			
			//  (공백) , (콤마 , )  , ( 시작인덱스 0 ) ,  ( 시작괄호'(' ) , (끝괄호')' ) , (연산자) 
//			String[] _targets={ "+" , "," , "(" , ")" , "!=" , "==" , ">=" , "<=" , "<" , ">", "=" , " "  };
			String _fnName="";
			int _startFnIndex=-1;
			Boolean _hasFunction=false;
			
			// 가장 많이 사용되는 케이스는 먼저 돌려본다. 시간 단축 용도
			_fnName = _value.substring(0, _startBracketIndex);
			_fnName = _fnName.trim();
			_hasFunction = hasFunction( _fnName );
			
			if( _hasFunction == false ){
				
				for( String _target : _targets ){
					
					_startFnIndex = _value.lastIndexOf(_target, _startBracketIndex-1);
//					_startFnIndex = _value.lastIndexOf(_target, _startBracketIndex); // 첫번째 괄호를 현재 괄호와 같이 찾는 문제가 있었다.
					if( _startFnIndex > -1 ){
						
						if( _startFnIndex < _startBracketIndex ){
							_startFnIndex+=1;
						}

						_fnName = _value.substring(_startFnIndex, _startBracketIndex);
						_fnName = _fnName.trim();
						_hasFunction = hasFunction( _fnName );
						if( _hasFunction ==  true ){
							break;
						}
					}
					
				}
				
			}else{
				_startFnIndex=0;
			}
			

			if( _hasFunction == true ){
				String _fnPart=_value.substring(_startFnIndex, _endBracketIndex+1);

				// _result 에서 괄호를 제거한 내부 매개변수로 함수 처리
				String _resultFnData = "";
				String _oldStr=_value.substring(_startBracketIndex+1, _endBracketIndex);
				
				// 매개변수가 있는 함수인지 아닌지 확인하고 건너뛰어야 한다.
				
				// 여기는 문제가 있으므로 고쳐야 함. ****
				// 매개변수 없이 사용할 수 있는 함수를 찾아야함.
				if( _oldStr.trim().equalsIgnoreCase("")      &&    _fnName.equalsIgnoreCase("GroupCurrentPage") == false
						&&    _fnName.equalsIgnoreCase("GroupTotalPage") == false 
						&&    _fnName.equalsIgnoreCase("CurrentPage") == false
						&&    _fnName.equalsIgnoreCase("TotalPage") == false
						&&    _fnName.equalsIgnoreCase("Now") == false
						&&    _fnName.equalsIgnoreCase("toDay") == false
						&&    _fnName.equalsIgnoreCase("SectionCurrentPage") == false
						&&    _fnName.equalsIgnoreCase("SectionTotalPage") == false
						&&    _fnName.equalsIgnoreCase("CloneIndex") == false
						){
					_resultFnData="";
				}else{
					if( _fnName.equalsIgnoreCase("if") ){
						_resultFnData = getIfFunctionValue(_oldStr);
					}else if( _fnName.equalsIgnoreCase("replace") ){
						_resultFnData = getReplaceFunctionValue( _oldStr ); 
					}else if( _fnName.equalsIgnoreCase("sum") ){
						_resultFnData = getSumFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("avg") ){
						_resultFnData = getAvgFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("max") ){
						_resultFnData = getMaxFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("min") ){
						_resultFnData = getMinFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("round") ){
						_resultFnData = getRoundFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("ceil") ){
						_resultFnData = getCeilFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("floor") ){
						_resultFnData = getFloorFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("SubString") ){
						_resultFnData = getSubStringFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("DayDiff") ){
						_resultFnData = getDayDiffFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("secondDiff") ){
						_resultFnData = getSecondDiffFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("minuteDiff") ){
						_resultFnData = getMinuteDiffFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("hourDiff") ){
						_resultFnData = getHourDiffFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("weekDiff") ){
						_resultFnData = getWeekDiffFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("monthDiff") ){
						_resultFnData = getMonthDiffFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("yearDiff") ){
						_resultFnData = getYearDiffFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("dayOfYear") ){
						_resultFnData = getDayOfYearFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("weekOfYear") ){
						_resultFnData = getWeekOfYearFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("year") ){
						_resultFnData = getYearFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("month") ){
						_resultFnData = getMonthFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("monthEn") ){
						_resultFnData = getMonthEnFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("day") ){
						_resultFnData = getDayFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("DayOfLast") ){
						_resultFnData = getDayoflastFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("week") ){
						_resultFnData = getWeekFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("weekKr") ){
						_resultFnData = getWeekKrFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("time") ){
						_resultFnData = getTimeFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("minute") ){
						_resultFnData = getMinuteFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("second") ){
						_resultFnData = getSecondFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("count") ){
						_resultFnData = getCountFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("Length") ){
						_resultFnData = getLengthFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("ToUpperCase") ){
						_resultFnData = getToUpperCaseFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("ToLowerCase") ){
						_resultFnData = getToLowerCaseFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("ToString") ){
						_resultFnData = getToStringFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("Now") ){
						_resultFnData = getNowFunctionValue( );
					}else if( _fnName.equalsIgnoreCase("ParseInt") ){
						_resultFnData = getParseIntFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("ParseFloat") ){
						_resultFnData = getParseFloatFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("Case") ){
						_resultFnData = getCaseFunctionValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("CurrentPage") ){
						_resultFnData = getCurrentPageValue(_oldStr);
					}else if( _fnName.equalsIgnoreCase("CumulativeSum") ){
						_resultFnData = getCumulativeSumValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("TotalPage") ){
						_resultFnData = getTotalPageValue(_oldStr);
					}else if( _fnName.equalsIgnoreCase("GroupDataColumn") ){
						_resultFnData = getGroupDataColumnValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("GroupCurrentPage") ){
						_resultFnData = getGroupCurrentPageValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("GroupTotalPage") ){
						_resultFnData = getGroupTotalPageValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("GroupAvg") ){
						_resultFnData = getGroupAvgValue(_oldStr, 0, 1);
					}else if( _fnName.equalsIgnoreCase("GroupCount") ){
						_resultFnData = getGroupCountValue(_oldStr, 0, 1);
					}else if( _fnName.equalsIgnoreCase("GroupMax") ){
						_resultFnData =getGroupMaxValue(_oldStr, 0, 1);
					}else if( _fnName.equalsIgnoreCase("GroupMin") ){
						_resultFnData = getGroupMinValue(_oldStr, 0, 1);
					}else if( _fnName.equalsIgnoreCase("GroupSum") ){
						_resultFnData=getGroupSumValue(_oldStr, -1, -1);
					}else if( _fnName.equalsIgnoreCase("GroupRowNum") ){
						_resultFnData=getGroupRowNumValue( _oldStr , mRowIndex );
					}else if( _fnName.equalsIgnoreCase("RowNum") ){
						_resultFnData = getRowNumValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("totalRowCount") ){
						_resultFnData = getTotalRowCountValue( _oldStr );
					}else if( _fnName.equalsIgnoreCase("toDay") ){
						_resultFnData = getTodayFunctionValue(  );
					}else if( _fnName.equalsIgnoreCase("SectionCurrentPage") ){
						_resultFnData = getSectionCurrentPageValue(_oldStr);
					}else if( _fnName.equalsIgnoreCase("SectionTotalPage") ){
						_resultFnData = getSectionTotalPageValue(_oldStr);
					}else if( _fnName.equalsIgnoreCase("CloneIndex") ){
						_resultFnData = String.valueOf( getCloneIndex() );
					}else if( _fnName.equalsIgnoreCase("RowDataValue") ){
						_resultFnData = getRowDataValue(_oldStr);
					}else if( _fnName.equalsIgnoreCase("BoundarySum") || _fnName.equalsIgnoreCase("BoundaryMax") 
							|| _fnName.equalsIgnoreCase("BoundaryMin") || _fnName.equalsIgnoreCase("BoundaryAvg") )
					{
						_resultFnData = getBoundaryCalCulate(_fnName, _oldStr);
					}else if( _fnName.equalsIgnoreCase("DateCalculation") ){
						_resultFnData = getDateCalculation( _oldStr);
					}else if( _fnName.equalsIgnoreCase("getDataSetValue") ){
						_resultFnData = getDataSetValueFunction(_oldStr);
					}else if( _fnName.equalsIgnoreCase("getConditionValue") ){
						_resultFnData = getConditionValueFunction(_oldStr);
					}else if( _fnName.equalsIgnoreCase("numberFormatter") ){
						_resultFnData = getFormatterData(_oldStr);
					}else if(_fnName.equalsIgnoreCase("GroupLength")){
						_resultFnData = getGroupLength(_oldStr);
					}else if(_fnName.equalsIgnoreCase("RowIndex")){
						_resultFnData = getRowIndex(_oldStr);
					}else if(_fnName.equalsIgnoreCase("IsEmpty")){
						_resultFnData = isEmptyData(_oldStr);
					}else if(_fnName.equalsIgnoreCase("DateFormat")){
						_resultFnData = getDateFormat(_oldStr);
					}
					
					
				}
				
				_value=_value.replace(_fnPart, _resultFnData);
				_result=_value;
			}else{
				// 함수가 아니다.
				return IS_NOT_FUNCTION;
			}
			
		}else{
			// 괄호 구문이 없으므로 함수가 아니라고 판단한다.
			return IS_NOT_FUNCTION;
		}
		
		
		return _result;
	}
	
	
	private Object getDataSetValue( String _dataset , String _column )
	{
		String _datasetName = _dataset;
		// group dataset control
		if( mOriginalDataMap != null ){
			if( mOriginalDataMap.containsKey(mGroupDataName) ){
				String _groupKey=mOriginalDataMap.get(mGroupDataName);
				if( _datasetName.equalsIgnoreCase(_groupKey)){
					_datasetName=mGroupDataName;
				}
			}
		}
		//
		
		ArrayList<HashMap<String, Object>> _list = mDatasetList.get(_datasetName);

		if( _list == null || mRowIndex >= _list.size() ){
			
			return "";
		}
		HashMap<String, Object> _dataHm = _list.get( mRowIndex );
		
		Object _dataValue = _dataHm.get(_column);
		
		// dataset 리스트에서 해당하는 column 값을 찾아서 리턴해준다.
		return _dataValue;
	}
	
	private ArrayList<HashMap<String, Object>> getDataSet( String _dataset )
	{
		String _datasetName = _dataset;
		// group dataset control
		if( mOriginalDataMap != null ){
			if( mOriginalDataMap.containsKey(mGroupDataName) ){
				String _groupKey=mOriginalDataMap.get(mGroupDataName);
				if( _datasetName.equalsIgnoreCase(_groupKey)){
					_datasetName=mGroupDataName;
				}
			}
		}
		//
		if(mDatasetList.containsKey(_datasetName))
		{
			ArrayList<HashMap<String, Object>> _list = mDatasetList.get(_datasetName);
			
			// dataset 리스트에서 해당하는 column 값을 찾아서 리턴해준다.
			return _list;
		}
		
		return null;
	}
	
	private ArrayList<HashMap<String, Object>> getDataSetGrp( String _dataset, boolean _isGroup )
	{
		String _datasetName = _dataset;
		// group dataset control
		if( _isGroup && mOriginalDataMap != null ){
			if( mOriginalDataMap.containsKey(mGroupDataName) ){
				String _groupKey=mOriginalDataMap.get(mGroupDataName);
				if( _datasetName.equalsIgnoreCase(_groupKey)){
					_datasetName=mGroupDataName;
				}
			}
		}
		//
		if(mDatasetList.containsKey(_datasetName))
		{
			ArrayList<HashMap<String, Object>> _list = mDatasetList.get(_datasetName);
			
			// dataset 리스트에서 해당하는 column 값을 찾아서 리턴해준다.
			return _list;
		}
		
		return null;
	}
	
	/**
	 * dataset name이 존재하는지 찾는다.
	 * */
	private Boolean hasDataset( String _datasetName )
	{
		Boolean _has=false;
		_has = mDatasetList.containsKey(_datasetName);
		return _has;
	}
	
	/**
	 * dataset column name이 존재하는지 찾는다.
	 * */
	private Boolean hasDatasetColumn( String _datasetname , String _columnName )
	{
		Boolean _has=false;
		
		String _datasetName = _datasetname;
		// group dataset control
		if( mOriginalDataMap != null ){
			if( mOriginalDataMap.containsKey(mGroupDataName) ){
				String _groupKey=mOriginalDataMap.get(mGroupDataName);
				if( _datasetName.equalsIgnoreCase(_groupKey)){
					_datasetName=mGroupDataName;
				}
			}
		}
		//
		
		ArrayList<HashMap<String, Object>> _list = mDatasetList.get(_datasetName);
		
		if( _list == null || mRowIndex >= _list.size() ){
			
			if( _list.size() == 0 ){
				return true;
			}
			
			//TEST
			if( _list != null )
			{
				return _list.get(0).containsKey(_columnName);
			}
			
			return _has;
		}
		
		HashMap<String, Object> _dataHm = _list.get( mRowIndex );
		
		_has = _dataHm.containsKey(_columnName);
		
		return _has;
	}
	
	
	private Boolean hasFunction( String _functionName )
	{
		Boolean _has=false;
		
		if( _functionName.equalsIgnoreCase("") ){
			return _has;
		}
		
		String _targets="Count,Sum,Avg,Max,Min,ToUpperCase,ToLowerCase,SubString,Replace,Hour,Date,RowNum,ToString,Length,TotalRowCount,Round,Ceil,Floor,Today,SecondDiff,MinuteDiff,HourDiff,DayDiff,WeekDiff,MonthDiff,YearDiff,DayOfYear,WeekOfYear,Year,Month,MonthEn,Day,Week,WeekKR,Time,Minute,Second,CurrentPage,TotalPage,Now,Case,if,If,ParseInt,ParseFloat,GroupAvg,GroupCount,GroupMax,GroupMin,GroupSum,GroupRowNum,GroupDataColumn,GroupCurrentPage,GroupTotalPage,CumulativeSum,SectionCurrentPage,SectionTotalPage,CloneIndex,RowDataValue,BoundarySum,BoundaryMax,BoundaryMin,BoundaryAvg,DayOfLast,DateCalculation,getDataSetValue,getConditionValue,numberFormatter,GroupLength,RowIndex,IsEmpty,DateFormat";
		
		_has = _targets.contains(_functionName);
		
		return _has;
	}

	
	
	
	/**
	 * 비교문을 값, 참|거짓을 반환한다.
	 * */
	private String getIfFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr	= _value;
		String[] _args	= _cloneStr.split(",");
		
		String _expression=_args[0];
		_expression=getDatasetProcess(_expression);
		
		if( _expression.contains("param") ){
			_expression =getParamProcess(_expression);	
		}else if( _expression.contains("dataset") ){
			_expression=getDatasetProcess(_expression);
		}
		
		String _result = getOperationValue( _expression );
		
		if( _result == "true" ){
			_ret = _args[1];
		}else if( _result == "false" ){
			if(_args.length>2) _ret = _args[2];
			else  _ret = "";
		}else{
			_ret = "";
		}
		
		//결과 값의 " 제거 처리
//		_ret = _ret.replace("\"", "");
		
		return _ret;
	}
	
	
	/**
	 * 비교 연산자 처리
	 * */
	private String getOperationValue( String _value )
	{
		String _ret="";
		
		//dataset_0.col_11 > 2000
		String[] _operations = { "!=" , "==" , ">=" , "<=", "=" , "<" , ">"  };
		Boolean _hasOper=false;
		String _operName="";
		
		// 연산자를 찾는다.
		for( String _oper : _operations ){
			if( _value.contains(_oper) ){
				_hasOper=true;
				_operName=_oper;
				break;
			}
		}

		if( _hasOper == true ){
			String[] _values =_value.split(_operName); 
			Object[] _oValues = new Object[ _values.length ];
			
			int i;
			int _length=_values.length;
			for( i=0; i<_length; i++ ){
				_oValues[i]=_values[i].trim();
			}
			
			//TEST 조건문에 연산자가 있을경우 계산후 진행
			if( _oValues[0].toString().contains("%")|| _oValues[0].toString().contains("/")||_oValues[0].toString().contains("*")||_oValues[0].toString().contains("+")||_oValues[0].toString().contains("-") ){
				ScriptEngineManager mgr = new ScriptEngineManager();
				ScriptEngine engine = mgr.getEngineByName("JavaScript");
				
				try {
					Object _evalObject = engine.eval(_oValues[0].toString());
					_oValues[0] = _evalObject.toString();
				} catch (ScriptException e) {
					
				}
			}
			
			// Data Type이 String인지 int인지 판단한다.
			String _dataType1 = getDataType( _oValues[0]  );
			String _dataType2 = getDataType( _oValues[1]  );
			
			// data type이 다른 변수는 비교할 수 없음.
			if( _dataType1.equals(_dataType2) == false ){
//				return "undifined";
				_dataType1 = "String";
			}
			
			if( _dataType1.equals("String") ){
				
				String _sValue1 =  _oValues[0].toString();
				String _sValue1_type2 =  "\'"+_sValue1+"\'";
				String _sValue1_type3 =  "\""+_sValue1+"\"";
				String _sValue2 =  _oValues[1].toString();
				
				if( _hasOper == true ){
					
					if( _operName == "!=" ){
						
//						if( _sValue1.equals(_sValue2) == false || _sValue1_type2.equals(_sValue2) == false || _sValue1_type3.equals(_sValue2) == false ){
						if( _sValue1.equals(_sValue2) == false && _sValue1_type2.equals(_sValue2) == false && _sValue1_type3.equals(_sValue2) == false ){
							_ret="true";
						}else{
							_ret="false";
						}
						
					}else if( _operName == "==" ){
						
						if( _sValue1.equals(_sValue2) == true || _sValue1_type2.equals(_sValue2) == true || _sValue1_type3.equals(_sValue2) == true ){
							_ret="true";
						}else{
							_ret="false";
						}
					}else if( _operName == "=" ){
						
						if( _sValue1.equals(_sValue2) == true || _sValue1_type2.equals(_sValue2) == true || _sValue1_type3.equals(_sValue2) == true ){
							_ret="true";
						}else{
							_ret="false";
						}
					}
					
				}
				
				
			}else{
				
				// 비교 조건문에 0.0013 과 같은 소숫점값이 있을때 integer로 변환할경우 0.0001 > 0 과 같은 조건이 false로 떨어지는 현상이 발생  2017-12-08 최명진
//				int _value1 =  0;
				BigDecimal _value1 = null;
				
				try{
//					_value1 =  Integer.parseInt( _oValues[0].toString() );
					_value1 =  new BigDecimal(_oValues[0].toString());
				}catch( Exception e ){
					
				}
				
//				int _value2 =  0;
				BigDecimal _value2 = null;
				try{
//					_value2 =  Integer.parseInt( _oValues[1].toString() );
					_value2 =  new BigDecimal(_oValues[1].toString());
				}catch( Exception e ){
					
				}
				
				if( _hasOper == true ){
					
					if( _operName == "!=" ){
						
						if( _value1.equals(_value2) == false ){
//						if( _value1 != _value2 ){
							_ret="true";
						}else{
							_ret="false";
						}
						
					}else if( _operName == "==" ){

						
						if( _value1.equals(_value2) ){
//							if( _value1 == _value2 ){
							_ret="true";
						}else{
							_ret="false";
						}
						
						
					}else if( _operName == ">=" ){
						
						if( _value1.compareTo(_value2)  >= 0 ){
//							if( _value1 >= _value2 ){
							_ret="true";
						}else{
							_ret="false";
						}
						
						
					}else if( _operName == "<=" ){
						
						if( _value1.compareTo(_value2)  <= 0 ){
//							if( _value1 <= _value2 ){
							_ret="true";
						}else{
							_ret="false";
						}
						
						
					}else if( _operName == "<" ){
						
						if( _value1.compareTo(_value2)  < 0 ){
//							if( _value1 < _value2 ){
							_ret="true";
						}else{
							_ret="false";
						}
						
						
					}else if( _operName == ">" ){
						
						if( _value1.compareTo(_value2)  > 0 ){
//						if( _value1 > _value2 ){
							_ret="true";
						}else{
							_ret="false";
						}
						
						
					}else if( _operName == "=" ){
						
						if( _value1.equals(_value2) ){
//						if( _value1 == _value2 ){
							_ret="true";
						}else{
							_ret="false";
						}
						
						
					}
					
					
				}
				
			}
			
			
		}
		
		return _ret;
	}
	
	private String getDataType( Object _value )
	{
		String _ret = "int";
		
		
		try {
			BigDecimal _bd = new BigDecimal(_value.toString());
//			Integer.parseInt( _value.toString());	
//			Integer.parseInt( _value.toString());	
		} catch (Exception e) {
			_ret = "String";
		}
		
//		if( _value instanceof String ){
//			_ret = "String";
//		}
		return _ret;
	}
	
	
	/**
	 * dataset list에서 해당 column 값을 찾아서 반환한다. 
	 * */
	private String[] getDataSetList( ArrayList<HashMap<String, Object>> _list  , String _dataColumn )
	{
		if( _list == null ){
			return null;
		}
		String[] _valueList=new String[ _list.size() ];
		boolean isChk = false;
		int _count=0;
		for( HashMap<String, Object> _dataHm : _list ){
			Object _dataValue = _dataHm.get(_dataColumn);
			
			String _value="0";
			try {
//				_value = Float.parseFloat(_dataValue.toString());
				_value = _dataValue.toString().replaceAll("[^\\d.-]", "");
				isChk = true;
			} catch (Exception e) {
				//return null;
				_value = "0";
			}
			
			_valueList[ _count ]=_value;
			_count++;
		}
		
		if(isChk == false) return null;
		
		return _valueList;
	}
	
	
	private String[] getDataSetList( ArrayList<HashMap<String, Object>> _list  , String _dataColumn , int _startIndex , int _lastIndex )
	{
		if( _list == null ){
			return null;
		}
		
		int _length=_lastIndex-_startIndex;
		
		if( _list.size() - _startIndex <  _length )
		{
			_length = _list.size() - _startIndex;
		}
		
		String[] _valueList=new String[ _length ];
		boolean isChk = false;
		
		HashMap<String, Object> _dataHm;
		Object _dataValue;
		
		int _count=0;
		// last index에  +1을 해야하나?
		int i;
		for( i=_startIndex; i< _lastIndex;  i++ ){
			
			if( i >= _list.size())
			{
				continue;
			}
			
			_dataHm = _list.get(i);
			_dataValue = _dataHm.get(_dataColumn);
			
			String _value="0";
			try {
//				_value = Float.parseFloat(_dataValue.toString());
				_value = _dataValue.toString().replaceAll("[^\\d.-]", "");
				isChk = true;
				
			} catch (Exception e) {
				_value = "0";
			}
			
			_valueList[ _count ]=_value;
			_count++;
		}
		
		if(isChk == false) return null;
		
		return _valueList;
	}
	
	
	
	
	
	/**
	 * dataset의 count 함수 
	 * */
	private String getCountFunctionValue( String _value )
	{
		String _ret="";
		
		// param으로는 count를 확인할 방법이 없다.
		if( isParam(_value) ){
			return _ret;
		}
		
		
		String _datasetName = getDataSetName( _value );
		
		mDataset = mDatasetList.get(_datasetName);
		
		int _size=-1;
		if(  mDataset != null ){
			_size = mDataset.size();
			_ret= String.valueOf(_size);
		}
		
		if( mStartIndex > -1 && mLastIndex > -1 ){
			_size = mLastIndex-mStartIndex;
			_ret= String.valueOf(_size);
		}

		
		return _ret;
	}
	
	
	
	private String getTotalRowCountValue( String _value )
	{
		String _ret="";
		
		if( isParam(_value) ){
			return _ret;
		}
		
		String _datasetName = getDataSetName( _value );
		
		mDataset = mDatasetList.get(_datasetName);
		
		int _size=-1;
		if(  mDataset != null ){
			
			_size = mDataset.size();
			
			if( _size < mLastIndex ){
				mLastIndex=_size;
			}
			_ret= String.valueOf(_size);
		}
		
		if( mStartIndex > -1 && mLastIndex > -1 ){
			_size = mLastIndex-mStartIndex;
			_ret= String.valueOf(_size);
		}

		
		return _ret;
	}
	
	
	/**
	 * length 함수 
	 * */
	private String getLengthFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else if( _typeValue == PARAM_TYPE ){
			_targetStr = getParameterValue(_datasetFieldStr1);
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		_ret = String.valueOf(  _targetStr.length()  );
		
		return _ret;
	}
	
	
	
	/**
	 * UpperCase 함수 처리
	 * */
	private String getToUpperCaseFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			
			if( isParam(_datasetFieldStr1) ){
				_datasetFieldStr1 = getParameterValue(_datasetFieldStr1);
			}
			
			_targetStr = _datasetFieldStr1;
		}
		
		_ret = _targetStr.toUpperCase();
		
		return _ret;
	}
	
	
	/**
	 * LowerCase 함수 처리
	 * */
	private String getToLowerCaseFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			if( isParam(_datasetFieldStr1) ){
				_datasetFieldStr1 = getParameterValue(_datasetFieldStr1);
			}
			_targetStr = _datasetFieldStr1;
		}
		
		_ret = _targetStr.toLowerCase();
		
		return _ret;
	}
	
	
	/**
	 * toString 함수처리
	 * */
	private String getToStringFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		_ret = _targetStr;
		
		return _ret;
	}
	
	/**
	 * parseInt 함수 처리
	 * */
	private String getParseIntFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		//@TEST ParseInt와 ParseFloat 내부에서 연산을 진행후 처리
		_datasetFieldStr1 = getDataSetProcess2(_datasetFieldStr1);
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		if(_args.length > 1 && _args[1].equals("true") )
		{
			// 문자가 있을경우 문자를 제거 (연산자는 남겨두고 제거해야ㅎ 함)
			_targetStr = _targetStr.replaceAll("[^\\d.-.\\-+*/%]", "");
		}
		else if( _args.length > 1 )
		{
			// ar
			_targetStr = _targetStr.replaceAll(_args[1], "");
		}
			
		// 조회된 값에 연산자가 있을시 처리 2016-06-08 최명진
		_targetStr = getOperation2( _targetStr ); 
		
		int _targetStrInt=0;
		try {
//			_targetStrInt = Integer.parseInt(_targetStr);
			_targetStrInt = Float.valueOf(_targetStr).intValue();
			
			BigDecimal _bd = new BigDecimal(_targetStr);
			
			_ret = String.valueOf(  _bd.toBigInteger()  );
			
		} catch (Exception e) {
			// TODO: handle exception
		}
//		_ret = String.valueOf(  _targetStrInt  );
		
		return _ret;
	}
	
	
	/**
	 * parseInt 함수 처리
	 * */
	private String getParseFloatFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		
		String _targetStr = "";
		
//		if(_cloneStr.indexOf("/") != -1){
//			_args = _cloneStr.split("/");
//			String _targetStr_1 =getDatasetProcess( _args[0]  );
//			String _targetStr_2 =getDatasetProcess( _args[1]  );
//			_targetStr = _targetStr_1 + "/" + _targetStr_2;
//		}else{
			//@TEST ParseInt와 ParseFloat 내부에서 연산을 진행후 처리
			_datasetFieldStr1 = getDataSetProcess2(_datasetFieldStr1);
			
			// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
			String _typeValue =getType(_datasetFieldStr1); 
			
			if( _typeValue == DATASET_TYPE ){
//			if( _typeValue.equalsIgnoreCase("dataset") == true ){
				_targetStr =getDatasetProcess( _datasetFieldStr1  );
			}else{
				_targetStr = _datasetFieldStr1;
			}
//		}
		
		// 2번째 인자값이 true일경우 한글을 숫자로 처리
		if(_args.length > 1 && _args[1].equals("true") )
		{
			// 문자가 있을경우 문자를 제거 (연산자는 남겨두고 제거해야ㅎ 함)
			_targetStr = _targetStr.replaceAll("[^\\d.-.\\-+*/%]", "");
		}
		
		// 조회된 값에 연산자가 있을시 처리 2016-06-08 최명진
		_targetStr = getOperation2( _targetStr ); 
		
		Float _targetStrInt=0f;
		try {
//			_targetStrInt = Float.parseFloat(_targetStr);
//			
//			BigDecimal _bd = new BigDecimal(_targetStrInt);
			BigDecimal _bd = new BigDecimal(_targetStr);
			
			_ret = String.valueOf(  _bd  );
			
		} catch (Exception e) {
			// TODO: handle exception
			_ret = "";
		}
		
		
		
		return _ret;
	}
	
	
	/**
	 * sum 함수처리
	 * */
	private String getSumFunctionValue( String _value )
	{
		String _ret="";
		
		String[] _valueList;
		if( isParam(_value) ){
			String _paramValue = getParameterValue(_value);
//			float _param = Float.parseFloat(_paramValue);
			String _param = _paramValue;
			_valueList=new String[1];
			_valueList[0]=_param;
		}else{
			_valueList=getDataSetValueList( _value , true );	
		}
		
		if( _valueList != null ){
			BigDecimal _sumValue =Evaluate.sum2(_valueList);
			_ret= _sumValue.toString();
		}
		return _ret;
	}

	
	/**
	 * avg 함수처리
	 * */
	private String getAvgFunctionValue( String _value )
	{
		String _ret="";

		String[] _valueList;
		
		if( isParam(_value) ){
			String _paramValue = getParameterValue( _value );
			
			String _param= "0";
			try {
//				_param= Float.parseFloat(_paramValue);
				_param= _paramValue;
			} catch (Exception e) {
			}
			_valueList = new String[1];
			_valueList[0]=_param;
		}else{
			_valueList=getDataSetValueList( _value , true );
		}
		
		if( _valueList != null ){
			BigDecimal _avgValue =Evaluate.avg(_valueList); 
			//_ret= String.valueOf(_avgValue);
			_ret = _avgValue.toString();
		}
		
		return _ret;
	}
	
	
	private Boolean isParam( String _value )
	{
		return _value.contains("{param:");
	}
	
	private String getParameterValue( String _value )
	{
		String _paramValue="";
		//{param:Key_101},0,2
		int _pStartIndex		= _value.indexOf("{param:");
		int _keyIndex		=_value.lastIndexOf("}");
		String _paramKey	= _value.substring(_pStartIndex + 7 , _keyIndex);
		HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_paramKey);
		if( _pList != null ) _paramValue = StringUtil.convertObjectToString(_pList.get("parameter"));
		
		return _paramValue;
	}
	
	
	public String replaceParameterValue( String _value )
	{
		String _result="";
		
		// {param:Key_101} + ':'+ {param:Key_102}
		String _paramValue="";
		while( _value.indexOf("{param:") != -1 )
		{
			int _pStartIndex		= _value.indexOf("{param:");
			int _keyIndex		=_value.indexOf("}",_pStartIndex);
			String _paramKey	= _value.substring(_pStartIndex + 7 , _keyIndex);
			HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_paramKey);
			if( _pList != null )
			{
				_paramValue = StringUtil.convertObjectToString(_pList.get("parameter"));
			}
			else
			{
				_paramValue = "";
			}
			
//			String _oldChar=	"param:"+_paramKey;
			String _oldChar=	"{param:"+_paramKey+"}";
			
//			_value=_value.replaceAll(	_oldChar  , _paramValue);
			_value=_value.replace(	_oldChar  , _paramValue);
		}
		_result=_value;
		
		return _result;
	}
	
	private String replaceParameterValueFn( String _value )
	{
		int _leng = _value.length();
		String _targetsStr = "+,()!=>< \n";
		String _convertStr = "";
		String _lastStr = "";
		String _matchStr = "";
		String _paramStr = "";
		
		if( _value.indexOf("param.") == -1 ) return _value;
		
		while( _value.indexOf("param.") != -1 )
		{
			int _pStartIndex		= _value.indexOf("param.");
			_matchStr = "";
			_paramStr = "";
			_convertStr = "";
			_lastStr = "";
			
			_leng = _value.length();
			
			for (int i = _pStartIndex+6; i < _leng; i++) {
				
				if( _targetsStr.indexOf(String.valueOf( _value.charAt(i) )) != -1 )
				{
					_matchStr = _value.substring(_pStartIndex, i);
					_paramStr = _value.substring(_pStartIndex+6, i);
					_lastStr = String.valueOf( _value.charAt(i) );
					break;
				}
				else if( i == _leng -1 )
				{
					_matchStr = _value.substring(_pStartIndex, _leng);
					_paramStr = _value.substring(_pStartIndex+6, _leng);
					break;
				}
					
			}
			
			if( mParam.containsKey(_paramStr) )
			{
				HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_paramStr);
				_convertStr = StringUtil.convertObjectToString(_pList.get("parameter"));
			}
			
			if(_convertStr.equals(_matchStr)) _convertStr = "";
			
			_value = _value.replaceAll( _matchStr + _lastStr, _convertStr + _lastStr );
			
		}
		
		return _value;
	}
	
	
	/**
	 * max 함수처리
	 * */
	private String getMaxFunctionValue( String _value )
	{
		String _ret="";

		String[] _valueList;
				
		if( isParam(_value) ){
			String _paramValue = getParameterValue(_value);
//			float _param=Float.valueOf(_paramValue);
			String _param=_paramValue;
			_valueList=new String[1];
			_valueList[0] = _param;
		}else{
			_valueList=getDataSetValueList( _value , true );	
		}
		
		if( _valueList != null ){
			String _maxValue =Evaluate.max(_valueList);
			BigDecimal _bd = new BigDecimal(_maxValue);
			_ret= _bd.toString();
//			_ret= String.valueOf(_maxValue);
		}
		return _ret;
	}
	
	/**
	 * min 함수처리
	 * */
	private String getMinFunctionValue( String _value )
	{
		String _ret="";
		
		String[] _valueList;
		if( isParam(_value) ){
			String _paramValue = getParameterValue(_value);
//			float _param = Float.parseFloat(_paramValue);
			String _param = _paramValue;
			_valueList=new String[1];
			_valueList[0]=_param;
		}else{
			_valueList=getDataSetValueList( _value , true );	
		}
		
		if( _valueList != null ){
			String _minValue =Evaluate.min(_valueList);
			
			BigDecimal _bd = new BigDecimal(_minValue);
			_ret= _bd.toString();
//			_ret= String.valueOf(_minValue);
		}
		
		return _ret;
	}
	
	
	/**
	 * round 함수처리
	 * */
	private String getRoundFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		String _datasetName = "";
		String _datasetColumnName = "";
		String _datasetFieldStr = _args[0];
		
		String _roundValue="";
		if( isParam(_datasetFieldStr) ){
			_roundValue = getParameterValue(_datasetFieldStr);
		}else{
			int _commaIndex = _datasetFieldStr.indexOf("."); 
			
			if(_commaIndex != -1) 
			{
				_datasetName = _datasetFieldStr.substring(0, _commaIndex);
				_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
			}
			
			if( mDatasetList.containsKey(_datasetName) )
			{
				Object _ds = getDataSetValue( _datasetName ,  _datasetColumnName );
				
				if( _ds == null ){
					return _ret;
				}
				_roundValue = _ds.toString();
			}
			else
			{
				_roundValue = _datasetFieldStr;
			}
		}
		
		try {
			BigDecimal _bd = new BigDecimal(_roundValue);
			
			int _count = 0; 
			try {
				_count = Integer.parseInt(_args[1]);
			} catch (Exception e) {
			}
			
			int _exNum=1;
			int i=0;
			int _length=Math.abs(_count);
			
			for( i=0; i<_length; i++ ){
				_exNum = _exNum * 10;
			}
			
			if( _count < 0 ){
				_bd = _bd.divide( new BigDecimal(_exNum));
				_bd = _bd.setScale(0, _bd.ROUND_HALF_UP);
				_bd = _bd.multiply( new BigDecimal(_exNum));
			}else{
				_bd = _bd.setScale(_count, _bd.ROUND_HALF_UP);
			}
			
			_ret = _bd.toString();
			
		} catch (Exception e) {
			// TODO: handle exception
			_ret = "";
		}
		
		return _ret;
	}
	
	
	/**
	 * ceil 함수처리
	 * */
	private String getCeilFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		String _datasetName = "";
		String _datasetColumnName = "";
		String _datasetFieldStr = _args[0];
		
		String _ceilValue="";
		
		if( isParam(_datasetFieldStr) ){
			_ceilValue = getParameterValue( _datasetFieldStr );
		}else{

			int _commaIndex = _datasetFieldStr.indexOf("."); 
			
			if(_commaIndex != -1)
			{
				_datasetName = _datasetFieldStr.substring(0, _commaIndex);
				_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
			}
			
			if( mDatasetList.containsKey(_datasetName) )
			{
				Object _ds = getDataSetValue( _datasetName ,  _datasetColumnName );
				
				if( _ds == null ){
					return _ret;
				}
				_ceilValue = _ds.toString();
			}
			else
			{
				_ceilValue = _datasetFieldStr;
			}
			
		}
		
		try {
			BigDecimal _bd = new BigDecimal(_ceilValue);
			
			// 소숫점 round 처리
			// 소숫점 count *  10
			// round 처리 후, 다시  나누기 count * 10
			// 123.123 , -2
			int _count = 0; 
			try {
				_count = Integer.parseInt(_args[1]);
			} catch (Exception e) {
			}
			
			int _exNum=1;
			int i=0;
			int _length=Math.abs(_count);
			
			for( i=0; i<_length; i++ ){
				_exNum = _exNum * 10;
			}
			
			if( _count < 0 ){
				_bd = _bd.divide( new BigDecimal(_exNum));
				_bd = _bd.setScale(0, _bd.ROUND_UP);
				_bd = _bd.multiply( new BigDecimal(_exNum));
			}else{
				_bd = _bd.setScale(_count, _bd.ROUND_UP);
			}
			
			_ret = _bd.toString();
		} catch (Exception e) {
			// TODO: handle exception
			_ret = "";
		}
		
		return _ret;
	}
	
	
	/**
	 * floor 함수처리
	 * */
	private String getFloorFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		String _datasetName = "";
		String _datasetColumnName = "";
		String _datasetFieldStr = _args[0];
		
		String _floorValue="";
		if( isParam(_datasetFieldStr) ){
			_floorValue = getParameterValue(_datasetFieldStr);
		}else{
			int _commaIndex = _datasetFieldStr.indexOf("."); 
			
			if(_commaIndex != -1)
			{
				_datasetName = _datasetFieldStr.substring(0, _commaIndex);
				_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
			}
			
			if( mDatasetList.containsKey(_datasetName) )
			{
				Object _ds = getDataSetValue( _datasetName ,  _datasetColumnName );
				
				if( _ds == null ){
					return _ret;
				}
				_floorValue = _ds.toString();
			}
			else
			{
				_floorValue = _datasetFieldStr;
			}
		}
		
		
		try {
			BigDecimal _bd = new BigDecimal(_floorValue);
			
			// 소숫점 round 처리
			// 소숫점 count *  10
			// round 처리 후, 다시  나누기 count * 10
			// 123.123 , -2
			int _count = 0; 
			try {
				_count = Integer.parseInt(_args[1]);
			} catch (Exception e) {
			}
			
			int _exNum=1;
			int i=0;
			int _length=Math.abs(_count);
			
			for( i=0; i<_length; i++ ){
				_exNum = _exNum * 10;
			}
			
			if( _count < 0 ){
				_bd = _bd.divide( new BigDecimal(_exNum));
				_bd = _bd.setScale(0, _bd.ROUND_DOWN);
				_bd = _bd.multiply( new BigDecimal(_exNum));
			}else{
				_bd = _bd.setScale(_count, _bd.ROUND_DOWN);
			}

			_ret = _bd.toString();
		} catch (Exception e) {
			// TODO: handle exception
			_ret = "";
		}
		
		
		return _ret;
	}
	
	
	/**
	 * subString 함수처리
	 * */
	private String getSubStringFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		String _targetStr = "";
		String _datasetFieldStr = _args[0];
		
		if( _datasetFieldStr.contains("{param:") ){
			//{param:Key_101},0,2
			int _pStartIndex= _datasetFieldStr.indexOf("{param:");
			int _keyIndex=_datasetFieldStr.lastIndexOf("}");
			String _paramKey = _datasetFieldStr.substring(_pStartIndex + 7 , _keyIndex);
			HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_paramKey);
			if(_pList != null)_targetStr = StringUtil.convertObjectToString(_pList.get("parameter"));
		}else if( _datasetFieldStr.contains("dataset") ){
			String _datasetName = "";
			String _datasetColumnName = "";
			
			int _commaIndex = _datasetFieldStr.indexOf("."); 
			_datasetName = _datasetFieldStr.substring(0, _commaIndex);
			_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
			
			Object _ds = getDataSetValue( _datasetName  , _datasetColumnName );
			
			if( _ds == null ){
				return _ret;
			}
			_targetStr = _ds.toString();
		}else{
			_targetStr=_datasetFieldStr;
		}

		
		
		int _startIndex = 0;
		try{
			_startIndex = Integer.parseInt(_args[1]);
		}catch( Exception e ){
			
		}
		
		int _endIndex = 0;
		try{
			_endIndex = Integer.parseInt(_args[2]);
		}catch( Exception e ){
			
		}
		
		if( _startIndex > _endIndex )
		{
			_endIndex = _targetStr.length();
		}
		
		try {
			_ret = Evaluate.substr(_targetStr, _startIndex, _endIndex); 
		} catch (Exception e) {
			// TODO: handle exception
			
		}
		
		return _ret;
	}
	
	
	/**
	 * ex) dataset_0.col_1 에서  dataset name을 반환
	 * */
//	private String getDatasetName( String _str )
//	{
//		String _result="";
//		
//		int _commaIndex = _str.indexOf("."); 
//		_result = _str.substring(0, _commaIndex);
//		
//		return _result;
//	}
	
	
	/**
	 * ex) dataset_0.col_1 에서  column name을 반환
	 * */
//	private String getDatasetColumnName( String _str )
//	{
//		String _result="";
//		
//		int _commaIndex = _str.indexOf("."); 
//		_result = _str.substring(_commaIndex+1 , _str.length() );
//		
//		return _result;
//	}
	
	
	/**
	 * _str: dataset_0.col_0
	 * dataset과  column을 찾아서 value값을 반환한다.
	 * */
//	private String getDatasetData( String _str )
//	{
//		String _result="";
//		
//		String _dsName		=	getDatasetName(_str);
//		String _colName	=	getDatasetColumnName(_str);
//		
//		Object _data = getDatasetValue( _dsName , mRowIndex , _colName ); 
//		
//		if( _data == null ){
//			return _result;
//		}
//		_result = _data.toString();
//		
//		return _result;
//	}
	
	
	/**
	 * 두 날짜의 차를 day로 반환한다.
	 * */
	private String getDayDiffFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		String _targetStr = "";
		if( isParam(_datasetFieldStr1) ){
			_targetStr = getParameterValue(_datasetFieldStr1);
		}else{
			_targetStr = getDatasetProcess( _datasetFieldStr1 );
		}
		
		// 두번째 dataset 가져오기
		String _datasetFieldStr2 = _args[1];
		String _targetStr2 = "";
		if( isParam(_datasetFieldStr2) ){
			_targetStr2 = getParameterValue(_datasetFieldStr2);
		}else{
			_targetStr2 = getDatasetProcess( _datasetFieldStr2  );	
		}
		
		String _dateFormat = _args[2];
		
		long _deff=0;
		try {
			_deff = Evaluate.dayDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * 두 날짜의 차를 second로 반환한다.
	 * */
	private String getSecondDiffFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		String _targetStr = getDatasetProcess( _datasetFieldStr1  );
		
		// 두번째 dataset 가져오기
		String _datasetFieldStr2 = _args[1];
		String _targetStr2 = getDatasetProcess( _datasetFieldStr2  );
		
		String _dateFormat = _args[2];
		
		long _deff=0;
		try {
			_deff = Evaluate.secondDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	/**
	 * 두 날짜의 차를 minute로 반환한다.
	 * */
	private String getMinuteDiffFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		String _targetStr = getDatasetProcess( _datasetFieldStr1  );
		
		// 두번째 dataset 가져오기
		String _datasetFieldStr2 = _args[1];
		String _targetStr2 = getDatasetProcess( _datasetFieldStr2  );
		
		String _dateFormat = _args[2];
		
		long _deff=0;
		try {
			_deff = Evaluate.minuteDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * 두 날짜의 차를 hour로 반환한다.
	 * */
	private String getHourDiffFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		String _targetStr = getDatasetProcess( _datasetFieldStr1  );
		
		// 두번째 dataset 가져오기
		String _datasetFieldStr2 = _args[1];
		String _targetStr2 = getDatasetProcess( _datasetFieldStr2  );
		
		String _dateFormat = _args[2];
		
		long _deff=0;
		try {
			_deff = Evaluate.hourDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * 두 날짜의 차를 week로 반환한다.
	 * */
	private String getWeekDiffFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		String _targetStr = getDatasetProcess( _datasetFieldStr1  );
		
		// 두번째 dataset 가져오기
		String _datasetFieldStr2 = _args[1];
		String _targetStr2 = getDatasetProcess( _datasetFieldStr2  );
		
		String _dateFormat = _args[2];
		
		long _deff=0;
		try {
			_deff = Evaluate.weekDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * 두 날짜의 차를 month로 반환한다.
	 * */
	private String getMonthDiffFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		String _targetStr = getDatasetProcess( _datasetFieldStr1  );
		
		// 두번째 dataset 가져오기
		String _datasetFieldStr2 = _args[1];
		String _targetStr2 = getDatasetProcess( _datasetFieldStr2  );
		
		String _dateFormat = _args[2];
		
		long _deff=0;
		try {
			_deff = Evaluate.monthDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * 두 날짜의 차를 year로 반환
	 * */
	private String getYearDiffFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		String _targetStr = getDatasetProcess( _datasetFieldStr1  );
		
		// 두번째 dataset 가져오기
		String _datasetFieldStr2 = _args[1];
		String _targetStr2 = getDatasetProcess( _datasetFieldStr2  );
		
		String _dateFormat = _args[2];
		
		long _deff=0;
		try {
			_deff = Evaluate.yearDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	
	
	
	/**
	 * 이 인스턴스가 나타내는 일 수를 정수로 가져옵니다
	 * */
	private String getDayOfYearFunctionValue( String _value )
	{
		String _ret="";
		
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		long _deff=0;
		try {
			_deff = Evaluate.dayOfYear(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	/**
	 * 이 인스턴스가 나타내는 주 수를 정수로 가져옵니다.
	 * */
	private String getWeekOfYearFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		long _deff=0;
		try {
			_deff = Evaluate.weekOfYear(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * value에 정의된 날짜의 year를 반환
	 * */
	private String getYearFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		long _deff=0;
		try {
			_deff = Evaluate.year(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	/**
	 * value에 정의된 날짜를 month로 반환
	 * */
	private  String getMonthFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		long _deff=0;
		try {
			_deff = Evaluate.month(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	/**
	 * value에 정의된 날짜를 month로 반환
	 * */
	private  String getMonthEnFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		try {
			_ret = Evaluate.monthEn(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
		return _ret;
	}
	
	
	/**
	 * value에 정의된 날짜를 day로 반환
	 * */
	private String getDayFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		long _deff=0;
		try {
			_deff = Evaluate.day(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	/**
	 * value에 정의된 날짜를 day로 반환
	 * */
	private String getDayoflastFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		long _deff=0;
		try {
			_deff = Evaluate.dayoflast(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * value에 정의된 날짜를 week(en)로 반환
	 * */
	private String getWeekFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		try {
			_ret = Evaluate.week(_targetStr, _dateFormat);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return _ret;
	}
	
	
	/**
	 * value에 정의된 날짜를 week(kr)로 반환
	 * */
	private String getWeekKrFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		
		String _dateFormat = "YYYYMMDD";
		if( _args.length > 1 ){	// _args[1]이 null인 상황이 있음.
			_dateFormat = _args[1];
		}
		
		try {
			_ret = Evaluate.weekKr(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
		return _ret;
	}
	
	
	
	/**
	 * value에 정의된 날짜를 시간으로 반환
	 * */
	private String getTimeFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		long _deff=0;
		try {
			_deff = Evaluate.time(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	
	
	/**
	 * value에 정의된 날짜를 minute로 반환
	 * */
	private String getMinuteFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		long _deff=0;
		try {
			_deff = Evaluate.minute(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	
	
	/**
	 * value에 정의된 날짜를 second로 반환
	 * */
	private String getSecondFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetFieldStr1 = _args[0];
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _typeValue =getType(_datasetFieldStr1); 
		String _targetStr = "";
		if( _typeValue == DATASET_TYPE ){
//		if( _typeValue.equalsIgnoreCase("dataset") == true ){
			_targetStr =getDatasetProcess( _datasetFieldStr1  );
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		String _dateFormat = _args[1];
		
		long _deff=0;
		try {
			_deff = Evaluate.second(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	
	
	
	/**
	 * 현재 시간을 시분초(HHMMSS)로 변환
	 * */
	private String getNowFunctionValue(  )
	{
		String _ret="";
		try {
			_ret = Evaluate.now();	
		} catch (Exception e) {
			return _ret;
		}
		return _ret;
	}
	
	private String getTodayFunctionValue(  )
	{
		String _ret="";
		try {
			_ret = Evaluate.toDay();	
		} catch (Exception e) {
			return _ret;
		}
		return _ret;
	}
	
	
	
	
	
	/**
	 * dataset의 첫번째 인자값을 두번째 인자값으로 변환하여, 반환
	 * */
	private String getReplaceFunctionValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		String _datasetName = "";
		String _datasetColumnName = "";
		String _datasetFieldStr = _args[0];
		
		String _replaceTarget="";
		if( isParam(_datasetFieldStr) ){
			_replaceTarget = getParameterValue(_datasetFieldStr);
		}else{
			int _commaIndex = _datasetFieldStr.indexOf("."); 
			
			if( _commaIndex == -1 )
			{
				_replaceTarget = _datasetFieldStr;
			}
			else
			{
				_datasetName = _datasetFieldStr.substring(0, _commaIndex);
				_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
				
				Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
				
				if( _ds == null || _ds.toString().equals("") ){
					_replaceTarget=_datasetFieldStr;
//					return _ret;
				}
				else
				{
					_replaceTarget=_ds.toString();
				}
			}
		}
		if(_args.length < 2) return _ret;
		
		String _oldStr = _args[1];
		String _newStr = (_args.length>2)?_args[2]:"";
		
		_ret = Evaluate.replace(_replaceTarget , _oldStr, _newStr);
		
		return _ret;
	}
	
	
	/**
	 * case문 처리
	 * */
	private String getCaseFunctionValue( String _value )
	{
		String _ret="";
		
		// Case( dataset_0.col_3 , when 부장 then 바보 when 차장 then 보바 else 메롱 )
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
			

		  // dataset value
		  String _datasetStr = _args[0]; 
		  String _dsData = getDatasetProcess( _datasetStr  );

		  String _caseStr = _args[1]; 
		  
		  _caseStr=_caseStr.replaceAll("WHEN", "when");
		  _caseStr=_caseStr.replaceAll("THEN", "then");
		  _caseStr=_caseStr.replaceAll("ELSE", "else");
		  _caseStr=_caseStr.replaceAll("\n", "");
		  
		  // when index를 찾음.
		  int _wIdx = 0;
		  // then index를 찾음.
		  int _tIdx = 0;
	
		  List<Integer> _wArray = new ArrayList<Integer>();		
		  List<Integer> _tArray = new ArrayList<Integer>();		
		  while( true )
		  {
			  _wIdx = _caseStr.indexOf("when",_wIdx);
			  _tIdx =  _caseStr.indexOf("then",_tIdx);
			  if( _wIdx == -1 ){
				  break;
			  }else{
				  _wIdx+=1;
				  _tIdx+=1;
				  _wArray.add(_wIdx);
				  _tArray.add(_tIdx);
			  }
		  }
		  
			
		  int _elseIdx = _caseStr.indexOf("else");
		  
		  int _size=_wArray.size();
		  String _wValue="";
		  String _tValue="";
		  List<String> _whenArray = new ArrayList<String>();
		  List<String> _thenArray = new ArrayList<String>();
		  
		  int i;
		  
		  Boolean _hasDataset = false;
		  for( i=0; i<_size; i++ ){
			  _hasDataset = false;
			  
			  _wValue = _caseStr.substring(_wArray.get(i) + 3 , _tArray.get(i) -1 );
			  
			  int _commaIndex = _wValue.indexOf(".");
			  if( _commaIndex > -1 ){
				  String _dsName= getDataSetName(_wValue);
				  if( _dsName.equalsIgnoreCase("") == false ){
						String _datasetName = _wValue.substring(0, _commaIndex);
						
						_hasDataset = hasDataset( _datasetName );
						if( _hasDataset == true ){
							String _datasetColumnName = _wValue.substring(_commaIndex+1 , _wValue.length());
							Object _wDataValue =getDataSetValue(_datasetName, _datasetColumnName);
							_wValue=_wDataValue.toString();
						}
				  }
			  }
			  
			  
			  if( _wArray.size() > i+1 ){
				  _tValue = _caseStr.substring(_tArray.get(i) +3 , _wArray.get(i+1) -1  );  
			  }else{
				  if( _elseIdx > -1 ){
					  _tValue = _caseStr.substring(_tArray.get(i) +3  , _elseIdx );  
				  }
			  }
			  
			  int _commaIndexw = _tValue.indexOf(".");
			  if( _commaIndexw > -1 ){
				  
				  _hasDataset = false;
				  
				  String _dsName= getDataSetName(_tValue);
				  if( _dsName.equalsIgnoreCase("") == false ){
						String _datasetName = _tValue.substring(0, _commaIndexw);
						
						_hasDataset = hasDataset( _datasetName );
						
						if( _hasDataset == true ){
							String _datasetColumnName = _tValue.substring(_commaIndexw+1 , _tValue.length());
							Object _wDataValue =getDataSetValue(_datasetName, _datasetColumnName);
							_tValue=_wDataValue.toString();
						}
				  }
			  }
			  
			  
			  _whenArray.add(_wValue);
			  _thenArray.add(_tValue);
		  }
		  
		  String _elseStr = _caseStr.substring(_elseIdx+4, _caseStr.length());

		  
		  // 문자열 = '문자열' = "문자열" ( 동일한 조건으로 판단한다. )
		  String _type2="\""+_dsData+"\"";
		  String _type3="\'"+_dsData+"\'";
		  
		  Boolean _hasData=false;
		  
		  int j;
		  int _whenSize=_whenArray.size();
		  String _whenStr="";
		  for( j=0; j< _whenSize; j++ ){
			  
			  _whenStr = _whenArray.get(j);
			  
			  if( _dsData.equals( _whenStr  ) == true ||   _type2.equals( _whenStr  ) == true ||   _type3.equals( _whenStr  ) == true ){
				  _hasData = true;
				  // result text는 
				  _ret = _thenArray.get(j);
				  break;
			  }
		  }
		  
		  if( _hasData == false ){
			  _ret = _elseStr;
		  }
		
		return _ret;
	}
	
	
	/**
	 * current page number
	 * */
	private String getCurrentPageValue( String _value )
	{
		String _result="";

		int _currentNum = mCurrentPageNum+1;
		_result = String.valueOf(_currentNum);
		
		return _result;
	}
	
	
	
	private String getSectionCurrentPageValue( String _value )
	{
		String _result="";

		int _currentNum = mSectionCurrentPageNum+1;
		_result = String.valueOf(_currentNum);
		
		return _result;
	}
	
	private String getSectionTotalPageValue( String _value )
	{
		// total page 값이 필요하다.
		String _result="";
		_result = String.valueOf(mSectionTotalPageNum);
		
		return _result;
	}
	
	/**
	 * cumulative sum of the specified Dataset Column
	 * */
	private String getCumulativeSumValue( String _value )
	{
		String _result="";
		
		if( isParam(_value) ){
			String _paramValue = getParameterValue(_value);
			float _param=0;
			try {
				_param = Float.parseFloat(_paramValue);
			} catch (Exception e) {
			}
			_result = String.valueOf(_param);
			return _result;
		}
		
		String[] _valueList = getDataSetValueList( _value , true );

		int _count = mRowIndex+1;
		
		if( _valueList != null ){
			
			if( _valueList.length < _count ){
				_count = _valueList.length;
			}
			
			String[] _arr= new String[ _count  ];
			int i;
			for( i=0; i<_count; i++ ){
				_arr[i] = _valueList[i];
			}
			BigDecimal _bd=Evaluate.sum2(_arr);
			
			_result= _bd.toString();
//			_result= String.valueOf(_sumValue);
		}
		return _result;
	}
	
	/**
	 * total page number
	 * */
	private String getTotalPageValue( String _value )
	{
		// total page 값이 필요하다.
		String _result="";
		_result = String.valueOf(mTotalPageNum);
		
		return _result;
	}
	
	
	
	/**
	 * sum value of the group Dataset Column
	 * */
	private String getGroupSumValue( String _value  , int _startIndex , int _lastIndex )
	{
		// 그룹 데이터셋이 필요한가? 확인 필요.
		String _result="";
//		String _sumValue="0"; 
		
		String[] _args=_value.split(",");
		
		// group header name & dataset name 이 필요한 함수이다.
		if( _args.length < 2 ){
			return _result;
		}
		
		
		String _groupHeaderName=_args[0];
		String _datasetStr=_args[1];
		
		String[] _valueList=getDataSetValueList(_datasetStr, false);
		
//		DecimalFormat d=new DecimalFormat("###");
//		_sumValue =Evaluate.sum2(_valueList).toString();
		
		if( _valueList != null ){
			BigDecimal _sumValue =Evaluate.sum2(_valueList);
			_result= _sumValue.toString();
		}
		
		return _result;
	}
	
	/**
	 * row number of the group Dataset Column
	 * */
	private String getGroupRowNumValue( String _value , int _itemCount )
	{
		// 그룹 리스트 값이 있어야 함.
		String _result="";
		
		int rowNum=mRowIndex + 1;
		
		if( mGroupDataNamesAr == null || mGroupDataName.equals("") )
		{
			return "";
		}
		
		int _cnt = mGroupDataNamesAr.indexOf(mGroupDataName);
		int _grpIdx = -1;
		
		// 전체 그룹별 그룹핑된 데이터셋명 리스트
		// 현재 그룹데이터셋이 어느 Row에 포함되어있는지 판단 / 몃번째 데이터셋 인지 판단 
		for (int i = 0; i < mGroupDataNamesAr.size(); i++) {
			if( mGroupDataNamesAr.get(i).indexOf(mGroupDataName) > -1)
			{
				_grpIdx = i;
				_cnt = mGroupDataNamesAr.get(i).indexOf(mGroupDataName);
				break;
			}
		}
		
		if( _grpIdx > -1 && _cnt > 0)
		for (int i = 0; i < _cnt; i++) {
			// 이전 데이터의 Row수를 가져와서 index에 추가
			rowNum = rowNum + mDatasetList.get(mGroupDataNamesAr.get(_grpIdx).get(i)).size();
		}
		
		_result = String.valueOf(rowNum);
//		mGroupDataNamesAr
//		mOriginalDataMap;
		
		return _result;
	}
	
	/**
	 * row number of the Dataset Column
	 * */
	private String getRowNumValue( String _value  )
	{
		String _result="";
		
		if( isParam(_value) ){
			return _result;
		}
		
		String _datasetName = getDataSetName( _value );
		
		mDataset = mDatasetList.get(_datasetName);
		
		if( mDataset.size() > mRowIndex ){
			int rowNum=mRowIndex + 1;
			_result = String.valueOf(rowNum);
		}
		
		return _result;
	}
	
	/**
	 * */
	private String getGroupDataColumnValue( String _value )
	{
		// 대문자 치환하는 함수가 맞는가?
		String _result="";
		return _result;
	}

	/**
	 * */
	private String getGroupCurrentPageValue( String _value )
	{
		// group current page 값이 필요하다.
		String _result="";
		
		_result = String.valueOf( mGroupCurrentPageIndex+1 );
		
		return _result;
	}
	
	/**
	 * 
	 * */
	private String getGroupTotalPageValue( String _value )
	{
		// group total page 값이 필요하다.
		String _result="";
		_result = String.valueOf( mGroupTotalPageIndex ); 
		return _result;
	}
	
	/**
	 * 
	 * */
	private String getGroupAvgValue( String _value , int _startIndex , int _lastIndex )
	{
		String _result="";
		
		String[] _args=_value.split(",");
		
		// group header name & dataset name 이 필요한 함수이다.
		if( _args.length < 2 ){
			return _result;
		}
		
		String _groupHeaderName = _args[0];
		String _datasetStr = _args[1];

		String[] _valueList=getDataSetValueList(_datasetStr, false);
		
		BigDecimal _avgValue =Evaluate.avg(_valueList); 
		//_result= String.valueOf(_avgValue);
		
		_result= _avgValue.toString();
		
		return _result;
	}
	
	
	/**
	 * 
	 * */
	private String getGroupCountValue( String _value , int _startIndex , int _lastIndex )
	{
		String _result="";
		
		String[] _args=_value.split(",");
		
		// group header name & dataset name 이 필요한 함수이다.
		if( _args.length < 2 ){
			return _result;
		}
		
		String _groupHeaderName= _args[0];
		_groupHeaderName = _groupHeaderName.trim();
		_groupHeaderName = "§" + _groupHeaderName + "§";
		
		String _datasetStr=_args[1];
		
		String _datasetName = getDataSetName(_datasetStr);
		
		mDataset = mDatasetList.get(_groupHeaderName);

		int _size=-1;
		if(  mDataset != null ){
			_size = mDataset.size();
			_result= String.valueOf(_size);
		}
		
		if( mStartIndex > -1 && mLastIndex > -1 ){
			_size = mLastIndex-mStartIndex;
			_result= String.valueOf(_size);
		}
		
		return _result;
	}
	
	/**
	 * 
	 * */
	private String getGroupMaxValue( String _value , int _startIndex , int _lastIndex )
	{
		String _result="";
		String _maxValue="0"; 
		
		String[] _args= _value.split(",");
		
		// group header name & dataset name 이 필요한 함수이다.
		if( _args.length < 2 ){
			return _result;
		}
		
		String _groupHeaderName = _args[0];
		String _datasetStr=_args[1];
		
		String[] _valueList = getDataSetValueList( _datasetStr , false );
		
		_maxValue =Evaluate.max(_valueList);
		
		BigDecimal _bd = new BigDecimal(_maxValue);
		
		_result= _bd.toString();
//		_result= String.valueOf(_maxValue);
		
		return _result;
	}
	
	/**
	 *  minimum value of the group Dataset Column.
	 * */
	private String getGroupMinValue( String _value ,  int _startIndex , int _lastIndex )
	{
		String _result="";
		String _minValue="0"; 
		
		String[] _args= _value.split(",");
		
		// group header name & dataset name 이 필요한 함수이다.
		if( _args.length < 2 ){
			return _result;
		}
		
		String _groupHeaderName = _args[0];
		String _datasetStr=_args[1];
		
		String[] _valueList = getDataSetValueList( _datasetStr , false );
		
		_minValue =Evaluate.min(_valueList);
		BigDecimal _bd = new BigDecimal(_minValue);
		_result= _bd.toString();
//		_result= String.valueOf(_minValue);
		
		return _result;
	}
	


	/**
	 * 
	 * */
	private String[] getDataSetValueList( String _value , Boolean _checkGroup )
	{
		String[] _result=null;
		
		int _dotIndex = _value.indexOf("."); 
		String _datasetName = _value.substring(0, _dotIndex);
		_datasetName = _datasetName.trim();
		String _datasetColumnName = _value.substring(_dotIndex+1,_value.length());
		_datasetColumnName = _datasetColumnName.trim();
		
		if( _checkGroup == true ){
		
			// group dataset control
			if( mOriginalDataMap != null ){
				if( mOriginalDataMap.containsKey(mGroupDataName) ){
					String _groupKey=mOriginalDataMap.get(mGroupDataName);
					if( _datasetName.equalsIgnoreCase(_groupKey)){
						_datasetName=mGroupDataName;
					}
				}
			}
			//
		}
		
		mDataset = mDatasetList.get(_datasetName);
		
		if( mStartIndex > -1 && mLastIndex > -1 ){
			_result=getDataSetList( mDataset , _datasetColumnName , mStartIndex , mLastIndex );
		}else{
			_result=getDataSetList( mDataset , _datasetColumnName );
		}
		
		return _result;
	}
	
	/**
	 * 
	 * */
	private String[] getDataSetValueListBoundray( String _value , Boolean _checkGroup )
	{
		String[] _result=null;
		String[] _values = _value.split(",");
		
		int _startIndex = 0;
		int _lastIndex = 0;
		
		if( _values.length == 3 )
		{
			_startIndex = Integer.valueOf( _values[1].trim() );
			_lastIndex  = Integer.valueOf( _values[2].trim() );
		}
		else if(_values.length == 2 )
		{
			_startIndex = Integer.valueOf( _values[1].trim() );
			_lastIndex  = mLastIndex;
		}
		else
		{
			_startIndex = mStartIndex;
			_lastIndex  = mLastIndex;

		}
		
		int _dotIndex = _values[0].indexOf("."); 
		
		if( _dotIndex < 0 ) return null;
		
		String _datasetName = _values[0].substring(0, _dotIndex);
		_datasetName = _datasetName.trim();
		String _datasetColumnName = _values[0].substring(_dotIndex+1,_values[0].length());
		_datasetColumnName = _datasetColumnName.trim();
		
		if( _checkGroup == true ){
		
			// group dataset control
			if( mOriginalDataMap != null ){
				if( mOriginalDataMap.containsKey(mGroupDataName) ){
					String _groupKey=mOriginalDataMap.get(mGroupDataName);
					if( _datasetName.equalsIgnoreCase(_groupKey)){
						_datasetName=mGroupDataName;
					}
				}
			}
			//
		}
		
		mDataset = mDatasetList.get(_datasetName);
		
		if( _startIndex > -1 && _lastIndex > -1 ){
			_result=getDataSetList( mDataset , _datasetColumnName , _startIndex , _lastIndex );
		}else{
			_result=getDataSetList( mDataset , _datasetColumnName );
		}
		
		return _result;
	}
	
	/**
	 * 문자열에서 dataset name을 반환한다.
	 * */
	private String getDataSetName( String _value )
	{
		String _datasetName="";
		
		int _dotIndex = _value.indexOf("."); 
		_datasetName = _value.substring(0, _dotIndex);
		_datasetName = _datasetName.trim();
		String _datasetColumnName = _value.substring(_dotIndex+1,_value.length());
		_datasetColumnName = _datasetColumnName.trim();
		
		// group dataset control
		if( mOriginalDataMap != null ){
			if( mOriginalDataMap.containsKey(mGroupDataName) ){
				String _groupKey=mOriginalDataMap.get(mGroupDataName);
				if( _datasetName.equalsIgnoreCase(_groupKey)){
					_datasetName=mGroupDataName;
				}
			}
		}
		
		return _datasetName;
	}
	
	/**
	 * 문자열에서 dataset name을 반환한다.
	 * */
	private String getDataSetNameGroup( String _value, boolean _isGroup )
	{
		String _datasetName="";
		
		int _dotIndex = _value.indexOf("."); 
		_datasetName = _value.substring(0, _dotIndex);
		_datasetName = _datasetName.trim();
		String _datasetColumnName = _value.substring(_dotIndex+1,_value.length());
		_datasetColumnName = _datasetColumnName.trim();
		
		// group dataset control
		if( _isGroup && mOriginalDataMap != null ){
			if( mOriginalDataMap.containsKey(mGroupDataName) ){
				String _groupKey=mOriginalDataMap.get(mGroupDataName);
				if( _datasetName.equalsIgnoreCase(_groupKey)){
					_datasetName=mGroupDataName;
				}
			}
		}
		
		return _datasetName;
	}
	

	public int getSectionCurrentPageNum() {
		return mSectionCurrentPageNum;
	}

	public void setSectionCurrentPageNum(int sectionCurrentPageNum) {
		this.mSectionCurrentPageNum = sectionCurrentPageNum;
	}

	public int getSectionTotalPageNum() {
		return mSectionTotalPageNum;
	}

	public void setSectionTotalPageNum(int SectionTotalPageNum) {
		this.mSectionTotalPageNum = SectionTotalPageNum;
	}
	
	private String getDataSetProcess2( String _value )
	{
		boolean _hasDataSet = false;
		String _columnName = "";
		String _replaceString = "";
		String _originalString = "";
		
		ArrayList<String> _dataSetNames = new ArrayList<String>();
		for( String _key : mDatasetList.keySet() )
		{
			_dataSetNames.add( _key );
		}
		
		int _startIndex = 0;
		int _endIndex = 0;
		String _operationStr = "+-*/%=!><";
		String _chkDsName = "";
		for (String _dsName : _dataSetNames) {
			_chkDsName = _dsName + ".";
			while (_value.indexOf( _chkDsName  )  > -1  ) {
				
				_startIndex = _value.indexOf( _chkDsName );
				String _lastChar = "";
				
				for (int i = _startIndex; i < _value.length(); i++) {
					_lastChar = String.valueOf( _value.charAt(i));
					_endIndex = i + 1;
					if( _lastChar.equals(" ") || _operationStr.indexOf(_lastChar) != -1 )
					{
						_endIndex = i;
						_lastChar = String.valueOf(_value.charAt(i));
						break;
					}
					else
					{
						_lastChar = "";
					}
				}
				
				_originalString = _value.substring(_startIndex, _endIndex) + _lastChar;
				_columnName = _value.substring(_startIndex+_chkDsName.length(), _endIndex);
				Object _resultData = getDataSetValue(_dsName, _columnName);
				
				if(_resultData != null )_replaceString  = _resultData.toString();
				else _replaceString = "";
				
//				if( _originalString.indexOf("+") != -1 ) 
//				{
//					_originalString = _originalString.replaceAll("[+]", "[+]");
//				}
				
				_value = _value.replace(_originalString, _replaceString + _lastChar );
			}
			
		}
		
		return _value;
	}
	
	public void setCloneIndex( int value )
	{
		mCloneIndex = value;
	}
	public int getCloneIndex()
	{
		return mCloneIndex;
	}
	
	/**
	 * dataset의 첫번째 인자값을 두번째 인자값으로 변환하여, 반환
	 * */
	private String getRowDataValue( String _value )
	{
		String _ret="";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		String _datasetName = "";
		String _datasetColumnName = "";
		String _datasetFieldStr = _args[0];
		int _rowIndex = 0;
		
		if(_args.length < 2) return "";
		
		_rowIndex = Integer.valueOf(_args[1]);
		
		boolean _isOriDataFlag = false;
		
		//2017-11-30 (  3번째 인자 추가  true : 그룹밴드이더라도 1번째 인자값으로 받은 데이터셋 그대로 사용  )
		if( _args.length > 2 && _args[2].trim().equals("true") ) _isOriDataFlag = true;
		
		int _commaIndex = _datasetFieldStr.indexOf("."); 
		_datasetName = _datasetFieldStr.substring(0, _commaIndex);
		_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
		
		// group dataset control
		if( mOriginalDataMap != null && !_isOriDataFlag  ){
			if( mOriginalDataMap.containsKey(mGroupDataName) ){
				String _groupKey=mOriginalDataMap.get(mGroupDataName);
				if( _datasetName.equalsIgnoreCase(_groupKey)){
					_datasetName=mGroupDataName;
				}
			}
		}
		//
		
		ArrayList<HashMap<String, Object>> _list = mDatasetList.get(_datasetName);

		if( _list == null || _rowIndex >= _list.size() || _rowIndex < 0 ){
			
			return "";
		}
		HashMap<String, Object> _dataHm = _list.get( _rowIndex );
		
		// column이 없거나 값이 null이면 빈값으로 리턴
		if( _dataHm.containsKey(_datasetColumnName)==false || _dataHm.get(_datasetColumnName) == null ) return "";
		
		Object _dataValue = _dataHm.get(_datasetColumnName);
		
		_ret = String.valueOf( _dataValue );
		
		return _ret;
	}
	
	private String getBoundaryCalCulate( String _fnName, String _value )
	{
		String _ret="";
		
		String[] _valueList;
		if( isParam(_value) ){
			String _paramValue = getParameterValue(_value);
			String _param = _paramValue;
			_valueList=new String[1];
			_valueList[0]=_param;
		}else{
			_valueList=getDataSetValueListBoundray( _value , true );	
		}
		
		if( _valueList != null ){
			BigDecimal _sumValue = null;
			if( _fnName.equalsIgnoreCase("BoundarySum") )	_sumValue =Evaluate.sum2(_valueList);
			else if( _fnName.equalsIgnoreCase("BoundaryMax") ) _sumValue = new BigDecimal( Evaluate.max(_valueList) );
			else if( _fnName.equalsIgnoreCase("BoundaryMin") ) _sumValue = new BigDecimal( Evaluate.min(_valueList) );
			else if( _fnName.equalsIgnoreCase("BoundaryAvg") ) _sumValue = Evaluate.avg(_valueList);
			
			if(_sumValue != null)_ret= _sumValue.toString();
			else _ret= "0";
		}
		return _ret;
	}
	
	/**
	 * Date의 일자를 + - 처리하여 변경된 일자를 Return시켜주는 기능
	 * @param _value  ( 데이터, 변경할 일자, 입력데이트 포맷, 결과 데이트 포맷, 변경할 일자 타입(YEAR,MONTH,DATE) )
	 * @return
	 */
	private String getDateCalculation( String _value )
	{
		String _ret="";
		String[] _args = _value.split(",");
		
		if( _args.length > 2)
		{
			String _datasetName = "";
			String _datasetColumnName = "";
			String _datasetFieldStr = _args[0];
			
			String _dataSetValue="";
			if( isParam(_datasetFieldStr) ){
				_dataSetValue = getParameterValue(_datasetFieldStr);
			}else{
				_datasetFieldStr = _datasetFieldStr.trim();
				int _commaIndex = _datasetFieldStr.indexOf("."); 
				if( _commaIndex > -1)
				{
					_datasetName = _datasetFieldStr.substring(0, _commaIndex);
					_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
					
					Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
					if( _ds == null ){
						return _ret;
					}
					_dataSetValue=_ds.toString();
				}
				else 
				{
					_dataSetValue = _datasetFieldStr;
				}
			}
			
			String _dateType = _args[2].trim();
			String _resultDateType = _args[2].trim();
			
			if(_args.length > 3 )
			{
				_resultDateType = _args[3].trim();
			}
			
			// 연산할 일자 값
			int _addDate = Integer.valueOf(_args[1].trim() );
			
			//기본값으로 일자로 연산하도록 셋팅
			int _calenderAddType = Calendar.DATE;
			
			// 년/월/일 중 연산할값이 따로 있을경우
			if( _args.length == 5 )
			{
				String _AddType = _args[4].trim();
				
				if( _AddType.equals("YEAR"))
				{
					_calenderAddType = Calendar.YEAR;
				}
				else if( _AddType.equals("MONTH") )
				{
					_calenderAddType = Calendar.MONTH;
				}
				else
				{
					_calenderAddType = Calendar.DATE;
				}
			}
			
			try {
				_ret = Evaluate.dateCalculation(_dataSetValue, _addDate, _dateType, _resultDateType, _calenderAddType );
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				_ret = "";
			}
			
		}
		
		
		return _ret;
	}
	
	/***
	 * functionName : getDataSetValueFunction : 특정 데이터셋에서 지정한 값과 지정한 컬럼의 값과 같은 Row의 결과 컬럼의 값을 리턴
	 * @param _value ex) getDataSetValue( 데이터셋명, 기준값(dataset_0.col_1) , 비교할 컬럼, 결과값 컬럼 )
	 * @return
	 */
	private String getDataSetValueFunction( String _value )
	{
		String _ret="";
		String[] _args = _value.split(",");
		
		if( _args.length  == 4)
		{
			String _datasetName = "";
			String _datasetColumnName = "";
			String _datasetFieldStr = _args[1];
			
			String _dataSetValue="";
			if( isParam(_datasetFieldStr) ){
				_dataSetValue = getParameterValue(_datasetFieldStr);
			}else{
				_datasetFieldStr = _datasetFieldStr.trim();
				int _commaIndex = _datasetFieldStr.indexOf("."); 
				if( _commaIndex > -1)
				{
					_datasetName = _datasetFieldStr.substring(0, _commaIndex);
					_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
					
					Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
					if( _ds == null ){
						return _ret;
					}
					_dataSetValue=_ds.toString();
				}
				else 
				{
					_dataSetValue = _datasetFieldStr;
				}
			}
			
			_ret = getDataSetRowData(_args[0].trim(), _dataSetValue, _args[2].trim(), _args[3].trim() );
			
		}
		
		return _ret;
	}
	
	private String getDataSetRowData( String _dataSetName, String _originalValue, String _checkColumn, String _resultColumn )
	{
		String _resultDataStr = "";
		ArrayList<HashMap<String, Object>> _list = getDataSet(_dataSetName);
		
		if( _list != null )
		{
			int _size = _list.size();
			for (int i = 0; i < _size; i++) {
				
				if( _list.get(i).get(_checkColumn).equals(_originalValue))
				{
					_resultDataStr = _list.get(i).get(_resultColumn).toString();
					break;
					
				}
				
			}
		}
		
		return _resultDataStr;
	}
	
	
	private String getFormatterData(  String _value )
	{
		String _ret="";
		
		_value = _value.replace("#,", "#§§");
		
		String[] _args = _value.split(",");
		
		if( _args.length > 1)
		{
			String _formatterStr = "";
			String _usePercentData = "";
			String _dataSetValue="";
			for (int i = 0; i < _args.length; i++) {
				
				String _datasetName = "";
				String _datasetColumnName = "";
				String _datasetFieldStr = _args[i];
				String _dataValue = "";
				
				if(_datasetFieldStr.contains("#§§"))
				{
					_datasetFieldStr = _datasetFieldStr.replace("#§§", "#,");
				}
				
				if( isParam(_datasetFieldStr) ){
					_dataSetValue = getParameterValue(_datasetFieldStr);
				}else{
					_datasetFieldStr = _datasetFieldStr.trim();
					int _commaIndex = _datasetFieldStr.indexOf("."); 
					if( _commaIndex > -1)
					{
						_datasetName = _datasetFieldStr.substring(0, _commaIndex);
						_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
						
						Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
						
						if(_ds == null) return "";
	
						if( _ds == "" && mDatasetList.containsKey(_datasetName) == false ){
							_dataValue = _datasetFieldStr;
						}
						else
						{
							_dataValue=_ds.toString();
						}
						
					}
					else 
					{
						_dataValue = _datasetFieldStr;
					}
				}
				
				if( i == 0 ) _dataSetValue = _dataValue;
				else if( i == 1 ) _formatterStr = _dataValue;
				else _usePercentData = _dataValue;
				
			}
			
			try {
				
				if( _usePercentData.equals("false") )
				{
					_formatterStr = _formatterStr.replace("%", "§");
				}
				
				DecimalFormat _decFm2 = new DecimalFormat(_formatterStr);
				BigDecimal _bigDecimalValue2 = new BigDecimal(_dataSetValue);
				String _formatData = _decFm2.format(_bigDecimalValue2);
				
				if( _bigDecimalValue2.compareTo(BigDecimal.ZERO) == 0  && _formatterStr.contains("0") == false )
				{
					
					if(_formatterStr.contains(".")) _formatData = _formatData.replace("0", ".");
					else _formatData = _formatData.replace("0", "");
				}
				
				if( _usePercentData.equals("false") )
				{
					_formatData = _formatData.replace("§", "%");
				}
				
				_ret = _formatData;
				
			} catch (Exception e) {
				// TODO: handle exception
			}
		
		}
		
		return _ret;
	}
	
	/***
	 * functionName : getConditionValueFunction : 특정 콘디션에서 지정한 값과 같은 결과 컬럼의 값을 리턴
	 * 콘디숀 맵파일은 UFile/sys/SYS/conditionMap.xml 파일에 정의되어 있다.
	 * @param _value ex) getConditionValue(콘디숀아이디, 기준값(dataset_0.col_1))
	 * @return
	 */
	private String getConditionValueFunction( String _value )
	{
		String _ret="";
		String[] _args = _value.split(",");
		
		if( _args.length  == 2)
		{
			String _datasetName = "";
			String _datasetColumnName = "";
			String _datasetFieldStr = _args[1];
			
			String _conditionValue="";
			if( isParam(_datasetFieldStr) ){
				_conditionValue = getParameterValue(_datasetFieldStr);
			}else{
				_datasetFieldStr = _datasetFieldStr.trim();
				int _commaIndex = _datasetFieldStr.indexOf("."); 
				if( _commaIndex > -1)
				{
					_datasetName = _datasetFieldStr.substring(0, _commaIndex);
					_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
					
					Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
					if( _ds == null ){
						return _ret;
					}
					_conditionValue=_ds.toString();
				}
				else 
				{
					_conditionValue = _datasetFieldStr;
				}
			}
			
			_ret = getConditionMapData(_args[0].trim(), _conditionValue );
			
		}
		
		return _ret;
	}
	
	/**
	 * conditionMap.xml에서 해당하는 결과 문자열을 찾아 리턴한다.
	 * 콘디숀 맵파일은 UFile/sys/SYS/conditionMap.xml 파일에 정의되어 있다.
	 * @param _conditionFileName
	 * @param _conditionId
	 * @param _conditionValue
	 * @return
	 */
	private String getConditionMapData(String _conditionId, String _conditionValue)
	{
		String _searchString = "";
		String _resultDataStr = "";
		String file_path = Log.ufilePath + "UFile/sys/SYS/conditionMap.xml";
		
		try {
			File file = new File(file_path);
			if(file != null)
			{
				SAXBuilder oBuilder = new SAXBuilder();
		  		Document doc = oBuilder.build( file );
		  		
				_searchString = _searchString + "./condition[@id='" + _conditionId + "']/list[@id='" + _conditionValue + "']";
//				System.out.println("getConditionMapData():_searchString=" + _searchString);
//				Element _itemElement = (Element)_xpath.evaluate(_searchString, conditionlist.item(0), XPathConstants.NODE);
				
				XPathFactory xFactory = XPathFactory.instance();
				XPathExpression<Element> _expr = xFactory.compile(_searchString, Filters.element());
				List<Element> _itemElementList = _expr.evaluate( doc.getRootElement()  );
				Element _itemElement = null;
				
				if( _itemElementList.size() > 0 ) _itemElement = _itemElementList.get(0);
				
				if(_itemElement != null)
				{
					_resultDataStr = _itemElement.getValue();
				}
				else if( "DEFAULT".equals(_conditionValue) == false )
				{
					// 없을경우 default를 뒤지도록 처리
					_searchString = "./condition[@id='" + _conditionId + "']/list[@id='DEFAULT']";
//					_itemElement = (Element)_xpath.evaluate(_searchString, conditionlist.item(0), XPathConstants.NODE);
					
					_expr = xFactory.compile(_searchString, Filters.element());
					
					_itemElementList = _expr.evaluate( doc.getRootElement()  );
					_itemElement = null;
					
					if( _itemElementList.size() > 0 ) _itemElement = _itemElementList.get(0);
					
					if(_itemElement != null)
					{
						_resultDataStr = _itemElement.getValue();
					}
					
				}
			}
		
		}
		catch(Exception exp){
			exp.printStackTrace();
		}
		
		return _resultDataStr;
	}
	
	private String getGroupLength( String _value  )
	{
		String _ret="";
		
		if(mGroupBandCntMap != null && mGroupBandCntMap.isEmpty() == false )
		{
			
			String _groupBandId = _value.trim();
			
			if( mGroupBandCntMap.containsKey(_groupBandId))
			{
				_ret = mGroupBandCntMap.get(_groupBandId).toString();
			}
			
		}
		
		return _ret;
	}
	
	private String getRowIndex( String _value )
	{
		String _result = String.valueOf( mRowIndex + 1 );
		
		return _result;
	}
	
	
	
	
	/**
	 * data에 빈값이 존재하는지 판단한다.
	 * */
	private String isEmptyData( String _value )
	{
		String _ret="true";
		
		String _cloneStr = _value.replace(" ", "");
		String[] _args = _cloneStr.split(",");
		
		// 첫번째 dataset 가져오기
		String _datasetStr = _args[0];
		
		// 	현재 Row비교 : none , 현재 데이터셋 비교 : group, 전체 데이터셋 비교 :  all 
		String _type = "all";
		
		if( _args.length > 1 )
		{
			_type = _args[1];
		}
		
		String _datasetName	=	null;
		String _columnName	=	null;
		ArrayList<HashMap<String, Object>> _list = null;
		boolean _isGroup = false;
		
		if( _type.toUpperCase().equals("NONE") ||  _type.toUpperCase().equals("GROUP") )
		{
			_isGroup = true;
		}
		else
		{
			_isGroup = false;
		}
		
		// dataset name
		_datasetName = getDataSetNameGroup(_datasetStr, _isGroup);
		
		// column name
		_columnName = getDatasetColumnName(_datasetStr , _datasetName  );

		_list = getDataSetGrp(_datasetName, _isGroup);
	
		HashMap<String, Object>  _rowItem=null;
		Object _rowItemValue = null;
		
		if( _list != null )
		{
			
			if( _type.equals("none") ){
				
				_rowItem =_list.get(this.mRowIndex);
				
				if( _rowItem != null ){
					_rowItemValue = _rowItem.get(_columnName);
				}
				
				if( _rowItemValue != null ){

					if( !_rowItemValue.equals("") )
					{
						_ret = "false";
					}
				}
				
			}
			else
			{
				int _size = _list.size();
				for (int i = 0; i < _size; i++) {
					
					_rowItem = _list.get(i);
					
					if( _rowItem != null ){
						_rowItemValue = _rowItem.get(_columnName);
					}
					
					
					if( _rowItemValue != null ){

						if( !_rowItemValue.equals("") )
						{
							_ret = "false";
							break;
						}
					}
				}
			}
			
			
		}
		
		return _ret;
	}
	
	
	
	private String getDatasetColumnName(String _clone , String _datasetName)
	{
		
		int _commaIndex = _clone.indexOf(".");
		
		// dataset column name을 찾는다.
		int _lastColumnIndex=-1;
		
		String _columnName="";
		Boolean _hasColumn =false;
		
		// 공백 , 닫힘괄호) , lastIndex , 연산자
		_lastColumnIndex=_clone.indexOf(" ", _commaIndex );
		
		if( _lastColumnIndex < 0 ){
			_lastColumnIndex = _clone.indexOf(")" , _commaIndex );
		}else{
			_columnName = _clone.substring(_commaIndex+1, _lastColumnIndex);
			
			// column name 찾기 전에 공백 제거
			_columnName = _columnName.trim();
			
			_hasColumn =hasDatasetColumn(_datasetName, _columnName); 
			
			if( _hasColumn == false ){
				_lastColumnIndex = -1;
			}
			
		}
		
		
		if( _hasColumn == false ){
			
			// 종료 괄호 구분으로 못찼았다면 마지막 인덱스를 기준으로 찾는다.
			if( _lastColumnIndex < 0 ){
				_lastColumnIndex = _clone.length();
			}
			_columnName = _clone.substring(_commaIndex+1, _lastColumnIndex);
			// column name 찾기 전에 공백 제거
			_columnName = _columnName.trim();
			_hasColumn =hasDatasetColumn(_datasetName, _columnName); 
			if( _hasColumn == false ){
				_lastColumnIndex = -1;
			}
			
		}
		
		
		// ***인덱스 결과값이 나왔더라도 column 이 아닐수가 있다는 것을 감안해야함.

		// 그래도 dataset column을 못찾았다면 연산자 구분으로 찾는다.
		if( _hasColumn == false ){
			String[] _operations2 = { "<" , ">", "=" , "!=" , "==" , ">=" , "<="  };
			for( String _operation : _operations2 ){
				
				_lastColumnIndex = _clone.indexOf( _operation  , _commaIndex );
				
				if( _lastColumnIndex < 0 ){
					continue;
				}else{
					
					_columnName = _clone.substring(_commaIndex+1, _lastColumnIndex);
					// column name 찾기 전에 공백 제거
					_columnName = _columnName.trim();
					_hasColumn =hasDatasetColumn(_datasetName, _columnName); 
					if( _hasColumn == false ){
						_lastColumnIndex = -1;
					}else{
						break;
					}
					
				}
				
			}
		}
		return _columnName;
	}
	
	/**
	 * DateFormat 함수 ( 데이터셋, 입력 포멧, 출력 포멧, Locale (en, ja, ko,....) )
	 * @return
	 */
	private String getDateFormat( String _value )
	{
		
		String _ret="";
		String resultDate="";
		String[] _args = _value.split(",");
		
		if( _args.length > 2)
		{
			String _datasetName = "";
			String _datasetColumnName = "";
			String _datasetFieldStr = _args[0];
			
			String _dataSetValue="";
			if( isParam(_datasetFieldStr) ){
				_dataSetValue = getParameterValue(_datasetFieldStr);
			}else{
				_datasetFieldStr = _datasetFieldStr.trim();
				int _commaIndex = _datasetFieldStr.indexOf("."); 
				if( _commaIndex > -1)
				{
					_datasetName = _datasetFieldStr.substring(0, _commaIndex);
					_datasetColumnName = _datasetFieldStr.substring(_commaIndex+1 , _datasetFieldStr.length());
					
					Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
					if( _ds == null ){
						return _ret;
					}
					_dataSetValue=_ds.toString();
				}
				else 
				{
					_dataSetValue = _datasetFieldStr;
				}
			}
			
			String _dateType =  StringEscapeUtils.unescapeHtml( _args[1].trim() );
			String _resultDateType = StringEscapeUtils.unescapeHtml( _args[2].trim() );
			String _locale = "";
			
			_dateType = Evaluate.convertDateFormatTypeOriginal(_dateType);
			_resultDateType = Evaluate.convertDateFormatTypeOriginal(_resultDateType);
			
			SimpleDateFormat formatter = new SimpleDateFormat( _dateType );
			
		    Date _date = new Date();
		    try{
		    	_date = formatter.parse(_dataSetValue);	
		    }catch(Exception e){
		    	return "";
		    }
		    
		    DateFormat df;
		    
		    if( _args.length > 3) 
			{
				_locale = _args[3].trim();
				df = new SimpleDateFormat( _resultDateType ,  new Locale( _locale ) );
			}
			else
			{
				df = new SimpleDateFormat( _resultDateType );
			}
	        
	        resultDate = df.format(_date.getTime());
		}
		

        return resultDate;	
	}
	
	
	public String testFN(String fnString, int _rowIndex , int _totalpageNum , int _currentPageNum , int _startIndex , int _lastIndex , String _groupDataName ) throws ScriptException
	{
		String _result="";

		
		mFn2.initialize(this.mDatasetList,this.mParam,this.mGroupCurrentPageIndex
        		,this.mGroupTotalPageIndex,this.mGroupDataNamesAr,this.mOriginalDataMap
        		,_rowIndex,_totalpageNum,_currentPageNum,_startIndex,_lastIndex,_groupDataName, this.mCloneIndex, this.mSectionCurrentPageNum, this.mSectionTotalPageNum, mGroupBandCntMap );
        
        engine.put("FN", mFn2);
        
        
//        engine.put("FN", new Function2(this.mDatasetList,this.mParam,this.mGroupCurrentPageIndex
//        		,this.mGroupTotalPageIndex,this.mGroupDataNamesAr,this.mOriginalDataMap
//        		,_rowIndex,_totalpageNum,_currentPageNum,_startIndex,_lastIndex,_groupDataName, this.mCloneIndex, this.mSectionCurrentPageNum, this.mSectionTotalPageNum)); 
        
		Object test2 = null;
		try {
			test2 = engine.eval(fnString);
			
		} catch (ScriptException e) {
			//e.printStackTrace();
			ScriptException ex = new ScriptException(fnString);
			throw ex;
		}
		
		if( test2 != null ){
			_result =test2.toString();	
		}
		
        
        return _result;
	}
	
	
	
	
	
	
	
	public String getFunctionVersion() {
		return FunctionVersion;
	}

	public void setFunctionVersion(String functionVersion) {
		FunctionVersion = functionVersion;
	}

	private String FunctionVersion="1.0";
	
	
	
}
