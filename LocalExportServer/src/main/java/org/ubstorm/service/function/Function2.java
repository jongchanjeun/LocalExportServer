package org.ubstorm.service.function;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.utils.StringUtil;


public class Function2 {


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
	
	public ArrayList<ArrayList<String>> mGroupDataNamesAr = null;	// 그룹핑된 데이터명을 가지고 있는 객체
	
	public void setGroupDataNamesAr(ArrayList<ArrayList<String>> _groupDataNamesAr) {
		mGroupDataNamesAr = _groupDataNamesAr;
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
	
	
	
	/**
	 * 생성자: dataset or 변수는 받아둔다.
	 * Eval Class 초기화
	 * */
//	public Function2( HashMap<String, ArrayList<HashMap<String, Object>>> _datasetList  , HashMap<String, Object> _param) 
//	{
//		mDatasetList	=_datasetList;
//		mParam		=_param;
//	}

	
	
	public Function2() 
	{
		
	}
	
	
//	public Function2( HashMap<String, ArrayList<HashMap<String, Object>>> _datasetList  , HashMap<String, Object> _param , int _groupCurrentPageIndex , int _groupTotalPageIndex, ArrayList<ArrayList<String>> _groupDataNamesAr,HashMap<String, String> _originalDataMap
//			, int _rowIndex , int _totalpageNum , int _currentPageNum , int _startIndex , int _lastIndex , String _groupDataName) 
//	{
//		mDatasetList	=_datasetList;
//		mParam		=_param;
//		mGroupCurrentPageIndex = _groupCurrentPageIndex;
//		mGroupTotalPageIndex = _groupTotalPageIndex;
//		mGroupDataNamesAr = _groupDataNamesAr;
//		mOriginalDataMap = _originalDataMap;
//		
//		
//		
//		this.mRowIndex				=_rowIndex;
//		this.mTotalPageNum		=_totalpageNum;
//		this.mCurrentPageNum	=_currentPageNum;
//		this.mStartIndex				=_startIndex;
//		this.mLastIndex				=_lastIndex;
//		this.mGroupDataName		=_groupDataName;
//	}
//
//	public Function2( HashMap<String, ArrayList<HashMap<String, Object>>> _datasetList  , HashMap<String, Object> _param , int _groupCurrentPageIndex , int _groupTotalPageIndex, ArrayList<ArrayList<String>> _groupDataNamesAr,HashMap<String, String> _originalDataMap
//			, int _rowIndex , int _totalpageNum , int _currentPageNum , int _startIndex , int _lastIndex , String _groupDataName, int _cloneIndex, int _sectionCurrentPageNum, int _sectionTotalPageNum ) 
//	{
//		mDatasetList	=_datasetList;
//		mParam		=_param;
//		mGroupCurrentPageIndex = _groupCurrentPageIndex;
//		mGroupTotalPageIndex = _groupTotalPageIndex;
//		mGroupDataNamesAr = _groupDataNamesAr;
//		mOriginalDataMap = _originalDataMap;
//		
//		
//		
//		this.mRowIndex				=_rowIndex;
//		this.mTotalPageNum		=_totalpageNum;
//		this.mCurrentPageNum	=_currentPageNum;
//		this.mStartIndex				=_startIndex;
//		this.mLastIndex				=_lastIndex;
//		this.mGroupDataName		=_groupDataName;
//		this.mCloneIndex = _cloneIndex;
//		this.mSectionCurrentPageNum = _sectionCurrentPageNum;
//		this.mSectionTotalPageNum = _sectionTotalPageNum;
//	}

	
	
	
	public void initialize( HashMap<String, ArrayList<HashMap<String, Object>>> _datasetList  , HashMap<String, Object> _param , int _groupCurrentPageIndex , int _groupTotalPageIndex, ArrayList<ArrayList<String>> _groupDataNamesAr,HashMap<String, String> _originalDataMap
			, int _rowIndex , int _totalpageNum , int _currentPageNum , int _startIndex , int _lastIndex , String _groupDataName, int _cloneIndex, int _sectionCurrentPageNum, int _sectionTotalPageNum, HashMap<String, Integer> _groupBandCnt ) 
	{
		mDatasetList	=_datasetList;
		mParam		=_param;
		mGroupCurrentPageIndex = _groupCurrentPageIndex;
		mGroupTotalPageIndex = _groupTotalPageIndex;
		mGroupDataNamesAr = _groupDataNamesAr;
		mOriginalDataMap = _originalDataMap;
		
		mGroupBandCntMap = _groupBandCnt;
		
		this.mRowIndex				=_rowIndex;
		this.mTotalPageNum		=_totalpageNum;
		this.mCurrentPageNum	=_currentPageNum;
		this.mStartIndex				=_startIndex;
		this.mLastIndex				=_lastIndex;
		this.mGroupDataName		=_groupDataName;
		this.mCloneIndex = _cloneIndex;
		this.mSectionCurrentPageNum = _sectionCurrentPageNum;
		this.mSectionTotalPageNum = _sectionTotalPageNum;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Object getDataSetValue( String _dataset , String _column )
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
	public String Count( String _datasetName )
	{
		String _ret="";
		
		_datasetName = getDataSetName( _datasetName );
		
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
	
	
	
	public String TotalRowCount( String _datasetName )
	{
		String _ret="";

//		_datasetName = getDataSetName( _datasetName );

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
	public String Length( String _datast , String _column )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject =getDataSetValue(_datast,_column);
		
		_targetStr = _valueObject.toString();
		
		_ret = String.valueOf(  _targetStr.length()  );
		
		return _ret;
	}
	
	
	
	public String Length( String _paramKey )
	{
		String _ret="";
		
		String _targetStr = "";

		_targetStr = getParameterValue(_paramKey);
		
		_ret = String.valueOf(  _targetStr.length()  );
		
		return _ret;
	}
	
	
	
	/**
	 * UpperCase 함수 처리
	 * */
	public String ToUpperCase( String _dataset,String _column )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject =getDataSetValue(_dataset, _column);
		
		_targetStr = _valueObject.toString();
		
		_ret = _targetStr.toUpperCase();
		
		return _ret;
	}
	
	
	public String ToUpperCase( String _paramKey )
	{
		String _ret="";
		
		String _targetStr = "";
			
		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
		_ret = _targetStr.toUpperCase();
		
		return _ret;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * LowerCase 함수 처리
	 * */
	public String ToLowerCase( String _dataset,String _column )
	{
		String _ret="";
		
		String _targetStr = "";
		
		Object _valueObject =getDataSetValue(_dataset, _column);
		
		_targetStr = _valueObject.toString();
		
		_ret = _targetStr.toLowerCase();
		
		return _ret;
	}
	

	public String ToLowerCase( String _paramKey )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
		_ret = _targetStr.toLowerCase();
		
		return _ret;
	}

	
	
	
	
	
	
	
	
	/**
	 * toString 함수처리
	 * */
	public String ToString( String _dataset , String _column )
	{
		String _ret="";
		
		String _targetStr = "";
		_targetStr =getDataSetValue(_dataset, _column).toString();
		_ret = _targetStr;
		
		return _ret;
	}
	
	/**
	 * parseInt 함수 처리
	 * */
	public String ParseInt( String _dataset , String _column )
	{
		String _ret="";
		
		String _targetStr = "";
			
		_targetStr =getDataSetValue(_dataset, _column).toString();
		
		try {
			
			BigDecimal _bd = new BigDecimal(_targetStr);
			
			_ret = String.valueOf(  _bd.toBigInteger()  );
			
		} catch (Exception e) {
		}
		
		return _ret;
	}
	
	public String ParseInt( String _value )
	{
		String _ret="";
		
		try {
			
			BigDecimal _bd = new BigDecimal(_value);
			
			_ret = String.valueOf(  _bd.toBigInteger()  );
			
		} catch (Exception e) {
		}
		
		return _ret;
	}
	
	
	
	
	/**
	 * parseInt 함수 처리
	 * */
	public String ParseFloat( String _dataset , String _column )
	{
		String _ret="";
		String _targetStr = "";
		
		_targetStr =getDataSetValue(_dataset, _column).toString();
		
		
		try {
			BigDecimal _bd = new BigDecimal(_targetStr);
			
			_ret = String.valueOf(  _bd  );
			
		} catch (Exception e) {
			// TODO: handle exception
			_ret = "";
		}
		
		return _ret;
	}
	
	public String ParseFloat( String _value )
	{
		String _ret="";
		
		try {
			BigDecimal _bd = new BigDecimal(_value);
			
			_ret = String.valueOf(  _bd  );
			
		} catch (Exception e) {
			// TODO: handle exception
			_ret = "";
		}
		
		return _ret;
	}
	
	public String Add( String... args )
	{
		String _ret="";
		
		String[] _valueList = args;
			
		if( _valueList != null ){
			BigDecimal _sumValue =Evaluate.sum2(_valueList);
			_ret= _sumValue.toString();
		}
		
		return _ret;
	}
	
	
	
	
	public String Subtract( String... args )
	{
		String _ret="";
		
		String[] _valueList = args;
		BigDecimal d;
		
		if( _valueList != null ){
			
			if( _valueList.length > 1 ){
				
				try {
					d = new BigDecimal( _valueList[0].trim() );
				} catch (Exception e) {
					// TODO: handle exception
					d = new BigDecimal(0);
				}
				
				String _value;
				
				for( int i=1; i<_valueList.length; i++ ){
					
					_value = _valueList[i];
					
					if(_value.equals("")) _value = "0";
					try {
						BigDecimal d2 = new BigDecimal( _value.trim() );
						d = d.subtract(d2);
					} catch (Exception e) {
						d = d.subtract( new BigDecimal(0));
					}
				}
				
				_ret= d.toString();
				
			}
		}
		
		return _ret;
	}
	
	
	public String Multiply( String... args )
	{
		String _ret="";
		
		String[] _valueList = args;
			
		if( _valueList != null ){
			
			if( _valueList.length > 1 ){
				
				BigDecimal d = new BigDecimal( _valueList[0].trim() );
				
				String _value;
				
				for( int i=1; i<_valueList.length; i++ ){
					
					_value = _valueList[i];
					
					if(_value.equals("")) _value = "0";
					try {
						BigDecimal d2 = new BigDecimal( _value.trim() );
						d = d.multiply(d2);
					} catch (Exception e) {
						d = d.multiply( new BigDecimal(0));
					}
				}
				
				_ret= d.toString();
				
			}
		}
		
		return _ret;
	}
	
	
	
	public String Divide( String... args )
	{
		String _ret="";
		
		String[] _valueList = args;
			
		if( _valueList != null ){
			
			if( _valueList.length > 1 ){
				
				BigDecimal d = new BigDecimal( _valueList[0].trim() );
				
				String _value;
				
				for( int i=1; i<_valueList.length; i++ ){
					
					_value = _valueList[i];
					
					if(_value.equals("")) _value = "0";
					try {
						BigDecimal d2 = new BigDecimal( _value.trim() );
						d = d.divide(d2);
					} catch (Exception e) {
						d = d.divide( new BigDecimal(0));
					}
				}
				
				_ret= d.toString();
				
			}
		}
		
		return _ret;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * sum 함수처리
	 * */
	public String Sum( String dataset , String column )
	{
		String _ret="";
		
		String[] _valueList;
			
		_valueList=getDataSetValueList2( dataset , column , true );	
		
		if( _valueList != null ){
			BigDecimal _sumValue =Evaluate.sum2(_valueList);
			_ret= _sumValue.toString();
		}
		return _ret;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	/**
	 * avg 함수처리
	 * */
	public String Avg( String dataset , String column )
	{
		String _ret="";

		String[] _valueList;
		
		_valueList=getDataSetValueList2( dataset , column , true );	
		
		if( _valueList != null ){
			BigDecimal _avgValue =Evaluate.avg(_valueList); 
			_ret = _avgValue.toString();
		}
		
		return _ret;
	}
	
	
	private Boolean isParam( String _value )
	{
		return _value.contains("{param:");
	}
	
	public String getParameterValue( String _value )
	{
		String _paramValue="";
		//{param:Key_101},0,2
		HashMap<String, String> _pList = (HashMap<String, String>) mParam.get(_value);
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
	public String Max( String dataset , String column )
	{
		String _ret="";

		String[] _valueList;

		_valueList=getDataSetValueList2( dataset , column , true );
		
		if( _valueList != null ){
			String _maxValue =Evaluate.max(_valueList);
			BigDecimal _bd = new BigDecimal(_maxValue);
			_ret= _bd.toString();
		}
		return _ret;
	}
	
	/**
	 * min 함수처리
	 * */
	public String Min( String dataset , String column)
	{
		String _ret="";
		
		String[] _valueList;
		
		_valueList=getDataSetValueList2( dataset , column , true );	
		
		if( _valueList != null ){
			String _minValue =Evaluate.min(_valueList);
			
			BigDecimal _bd = new BigDecimal(_minValue);
			_ret= _bd.toString();
		}
		
		return _ret;
	}
	
	
	/**
	 * round 함수처리
	 * */
	public String Round( String _datasetName , String _datasetColumnName , int _count )
	{
		String _ret="";
		
		String _roundValue="";
		
		if( mDatasetList.containsKey(_datasetName) )
		{
			Object _ds = getDataSetValue( _datasetName ,  _datasetColumnName );
			
			if( _ds == null ){
				return _ret;
			}
			_roundValue = _ds.toString();
		}
		
		try {
			BigDecimal _bd = new BigDecimal(_roundValue);
			
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
	
	
	public String Round( String _paramKey , int _count )
	{
		String _ret="";
		
		String _roundValue="";
		if( isParam(_paramKey) ){
			_roundValue = getParameterValue(_paramKey);
		}else{
			_roundValue = _paramKey;
		}
		
		try {
			BigDecimal _bd = new BigDecimal(_roundValue);
			
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
			_ret = "";
		}
		
		return _ret;
	}
	
	
	
	
	
	/**
	 * ceil 함수처리
	 * */
	public String Ceil( String _datasetName , String _datasetColumnName , int _count )
	{
		String _ret="";
		
		
		String _ceilValue="";
			
		if( mDatasetList.containsKey(_datasetName) )
		{
			Object _ds = getDataSetValue( _datasetName ,  _datasetColumnName );
			
			if( _ds == null ){
				return _ret;
			}
			_ceilValue = _ds.toString();
		}
			
		
		try {
			BigDecimal _bd = new BigDecimal(_ceilValue);
			
			// 소숫점 round 처리
			// 소숫점 count *  10
			// round 처리 후, 다시  나누기 count * 10
			// 123.123 , -2
			
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
	
	
	
	
	public String Ceil( String _paramKey , int _count )
	{
		String _ret="";
		
		String _ceilValue="";
		
		_ceilValue = getParameterValue( _paramKey );
		
		try {
			BigDecimal _bd = new BigDecimal(_ceilValue);
			
			// 소숫점 round 처리
			// 소숫점 count *  10
			// round 처리 후, 다시  나누기 count * 10
			// 123.123 , -2
			
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
	public String Floor( String _datasetName , String _datasetColumnName ,int _count)
	{
		String _ret="";
		
		String _floorValue="";
		
		if( mDatasetList.containsKey(_datasetName) )
		{
			Object _ds = getDataSetValue( _datasetName ,  _datasetColumnName );
			
			if( _ds == null ){
				return _ret;
			}
			_floorValue = _ds.toString();
		}
		
		
		try {
			BigDecimal _bd = new BigDecimal(_floorValue);
			
			// 소숫점 round 처리
			// 소숫점 count *  10
			// round 처리 후, 다시  나누기 count * 10
			// 123.123 , -2
			
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
	
	
	public String Floor( String _paramKey ,int _count)
	{
		String _ret="";
		String _floorValue="";
		
		_floorValue = getParameterValue(_paramKey);
		
		try {
			BigDecimal _bd = new BigDecimal(_floorValue);
			
			// 소숫점 round 처리
			// 소숫점 count *  10
			// round 처리 후, 다시  나누기 count * 10
			// 123.123 , -2
			
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
	public String SubString( String _datasetName , String _datasetColumnName ,int _startIndex,int _endIndex)
	{
		String _ret="";
		
		String _targetStr = "";
		
		Object _ds = getDataSetValue( _datasetName  , _datasetColumnName );
		
		if( _ds == null ){
			return _ret;
		}
		_targetStr = _ds.toString();
		
		if( _startIndex > _endIndex )
		{
			_endIndex = _targetStr.length();
		}
		
		try {
			_ret = Evaluate.substr(_targetStr, _startIndex, _endIndex); 
		} catch (Exception e) {
			
		}
		
		return _ret;
	}
	
	
	
	
	/**
	 * subString 함수처리
	 * */
	public String SubString( String _paramKey ,int _startIndex,int _endIndex)
	{
		String _ret="";
		
		String _targetStr = "";

		_targetStr = getParameterValue(_paramKey);
		
		if( _startIndex > _endIndex )
		{
			_endIndex = _targetStr.length();
		}
		
		try {
			_ret = Evaluate.substr(_targetStr, _startIndex, _endIndex); 
		} catch (Exception e) {
			
		}
		
		return _ret;
	}
	
	
	
	
	
	
	
	/**
	 * 두 날짜의 차를 day로 반환한다.
	 * */
	public String DayDiff( String _dataset , String _column , String _dataset2 , String _column2 ,String _dateFormat )
	{
		String _ret="";
		
		
		// 첫번째 dataset 가져오기
		Object _valueObject = getDataSetValue(_dataset, _column); 
		String _targetStr = _valueObject.toString();
		
		
		// 두번째 dataset 가져오기
		Object _valueObject2 = getDataSetValue(_dataset2, _column2); 
		String _targetStr2 = _valueObject2.toString();
		
		
		long _deff=0;
		try {
			_deff = Evaluate.dayDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	public String DayDiff( String _datasetFieldStr1 , String _datasetFieldStr2 ,String _dateFormat )
	{
		String _ret="";
		
		
		// 첫번째 dataset 가져오기
		String _targetStr = "";
		if( isParam(_datasetFieldStr1) ){
			_targetStr = getParameterValue(_datasetFieldStr1);
		}else{
			_targetStr = _datasetFieldStr1;
		}
		
		// 두번째 dataset 가져오기
		String _targetStr2 = "";
		if( isParam(_datasetFieldStr2) ){
			_targetStr2 = getParameterValue(_datasetFieldStr2);
		}else{
			_targetStr2 = _datasetFieldStr2;
		}
		
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
	public String SecondDiff( String _dataset , String _column , String _dataset2 , String _column2 , String _dateFormat )
	{
		String _ret="";
		
		
		// 첫번째 dataset 가져오기
		Object _valueObject = getDataSetValue(_dataset, _column); 
		String _targetStr = _valueObject.toString();
		
		
		// 두번째 dataset 가져오기
		Object _valueObject2 = getDataSetValue(_dataset2, _column2); 
		String _targetStr2 = _valueObject2.toString();
		
		
		long _deff=0;
		try {
			_deff = Evaluate.secondDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String SecondDiff( String _paramKey , String _paramKey2 , String _dateFormat )
	{
		String _ret="";
		
		
		String _dataSetValue="";
		if( isParam(_paramKey) ){
			_dataSetValue = getParameterValue(_paramKey);
		}else{
			_dataSetValue = _paramKey;
		}
		
		
		String _dataSetValue2="";
		if( isParam(_paramKey2) ){
			_dataSetValue2 = getParameterValue(_paramKey2);
		}else{
			_dataSetValue2=_paramKey2;
		}
		
		
		long _deff=0;
		try {
			_deff = Evaluate.secondDiff(_dataSetValue, _dataSetValue2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	
	
	
	
	/**
	 * 두 날짜의 차를 minute로 반환한다.
	 * */
	public String MinuteDiff( String _dataset , String _column , String _dataset2 , String _column2 , String _dateFormat )
	{
		String _ret="";
		
		
		// 첫번째 dataset 가져오기
		Object _valueObject = getDataSetValue(_dataset, _column); 
		String _targetStr = _valueObject.toString();
		
		
		// 두번째 dataset 가져오기
		Object _valueObject2 = getDataSetValue(_dataset2, _column2); 
		String _targetStr2 = _valueObject2.toString();
		
		
		long _deff=0;
		try {
			_deff = Evaluate.minuteDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	public String MinuteDiff( String _paramKey , String _paramKey2 , String _dateFormat )
	{
		String _ret="";
		
		
		// 첫번째 dataset 가져오기
		String _dataSetValue="";
		if( isParam(_paramKey) ){
			_dataSetValue = getParameterValue(_paramKey);
		}else{
			_dataSetValue = _paramKey;
		}
		
		
		// 두번째 dataset 가져오기
		String _dataSetValue2="";
		if( isParam(_paramKey2) ){
			_dataSetValue2 = getParameterValue(_paramKey2);
		}else{
			_dataSetValue2 = _paramKey2;
		}
		
		
		long _deff=0;
		try {
			_deff = Evaluate.minuteDiff(_dataSetValue, _dataSetValue2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * 두 날짜의 차를 hour로 반환한다.
	 * */
	public String HourDiff( String _dataset , String _column , String _dataset2 , String _column2 , String _dateFormat)
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		Object _valueObject = getDataSetValue(_dataset, _column); 
		String _targetStr = _valueObject.toString();
		
		
		// 두번째 dataset 가져오기
		Object _valueObject2 = getDataSetValue(_dataset2, _column2); 
		String _targetStr2 = _valueObject2.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.hourDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	public String HourDiff( String _paramKey, String _paramKey2 , String _dateFormat)
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		String _dataSetValue="";
		if( isParam(_paramKey) ){
			_dataSetValue = getParameterValue(_paramKey);
		}else{
			_dataSetValue = _paramKey;
		}
		
		
		// 두번째 dataset 가져오기
		String _dataSetValue2="";
		if( isParam(_paramKey2) ){
			_dataSetValue2 = getParameterValue(_paramKey2);
		}else{
			_dataSetValue2 = _paramKey2;
		}
		
		long _deff=0;
		try {
			_deff = Evaluate.hourDiff(_dataSetValue, _dataSetValue2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * 두 날짜의 차를 week로 반환한다.
	 * */
	public String WeekDiff( String _dataset , String _column , String _dataset2 , String _column2  , String _dateFormat )
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		Object _valueObject = getDataSetValue(_dataset, _column); 
		String _targetStr = _valueObject.toString();
		
		
		// 두번째 dataset 가져오기
		Object _valueObject2 = getDataSetValue(_dataset2, _column2); 
		String _targetStr2 = _valueObject2.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.weekDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String WeekDiff( String _paramKey, String _paramKey2  , String _dateFormat )
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		String _dataSetValue="";
		if( isParam(_paramKey) ){
			_dataSetValue = getParameterValue(_paramKey);
		}else{
			_dataSetValue = _paramKey;
		}
		
		
		// 두번째 dataset 가져오기
		String _dataSetValue2="";
		if( isParam(_paramKey2) ){
			_dataSetValue2 = getParameterValue(_paramKey2);
		}else{
			_dataSetValue2 = _paramKey2;
		}
		
		long _deff=0;
		try {
			_deff = Evaluate.weekDiff(_dataSetValue, _dataSetValue2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * 두 날짜의 차를 month로 반환한다.
	 * */
	public String MonthDiff( String _dataset , String _column , String _dataset2 , String _column2,String _dateFormat)
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		Object _valueObject = getDataSetValue(_dataset, _column); 
		String _targetStr = _valueObject.toString();
		
		
		// 두번째 dataset 가져오기
		Object _valueObject2 = getDataSetValue(_dataset2, _column2); 
		String _targetStr2 = _valueObject2.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.monthDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String MonthDiff( String _paramKey , String _paramKey2,String _dateFormat)
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		String _dataSetValue="";
		if( isParam(_paramKey) ){
			_dataSetValue = getParameterValue(_paramKey);
		}else{
			_dataSetValue = _paramKey;
		}
		
		
		// 두번째 dataset 가져오기
		String _dataSetValue2="";
		if( isParam(_paramKey2) ){
			_dataSetValue2 = getParameterValue(_paramKey2);
		}else{
			_dataSetValue2 = _paramKey2;
		}
		
		long _deff=0;
		try {
			_deff = Evaluate.monthDiff(_dataSetValue, _dataSetValue2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	/**
	 * 두 날짜의 차를 year로 반환
	 * */
	public String YearDiff( String _dataset , String _column , String _dataset2 , String _column2 ,String _dateFormat)
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		Object _valueObject = getDataSetValue(_dataset, _column); 
		String _targetStr = _valueObject.toString();
		
		
		// 두번째 dataset 가져오기
		Object _valueObject2 = getDataSetValue(_dataset2, _column2); 
		String _targetStr2 = _valueObject2.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.yearDiff(_targetStr, _targetStr2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String YearDiff( String _paramKey , String _paramKey2 ,String _dateFormat)
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		String _dataSetValue="";
		if( isParam(_paramKey) ){
			_dataSetValue = getParameterValue(_paramKey);
		}else{
			_dataSetValue = _paramKey;
		}
		
		
		// 두번째 dataset 가져오기
		String _dataSetValue2="";
		if( isParam(_paramKey2) ){
			_dataSetValue2 = getParameterValue(_paramKey2);
		}else{
			_dataSetValue2 = _paramKey2;
		}
		
		long _deff=0;
		try {
			_deff = Evaluate.yearDiff(_dataSetValue, _dataSetValue2, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	
	
	/**
	 * 이 인스턴스가 나타내는 일 수를 정수로 가져옵니다
	 * */
	public String DayOfYear( String _dataset ,String _column, String _dateFormat )
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		
		long _deff=0;
		try {
			_deff = Evaluate.dayOfYear(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String DayOfYear( String _paramKey, String _dateFormat )
	{
		String _ret="";
		
		// 첫번째 dataset 가져오기
		
		// 원래는 dataset이 있어야 하지만, value가 들어있을수도 있다.
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
		
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
	public String WeekOfYear( String _dataset , String _column, String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";
			
		Object _valueObject = getDataSetValue(_dataset, _column);
			
		_targetStr =_valueObject.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.weekOfYear(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String WeekOfYear( String _paramKey, String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";
			
		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public String Year( String _dataset , String _column, String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.year(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	public String Year( String _paramKey, String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public  String Month( String _dataset , String _column , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.month(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	public  String Month( String _paramKey , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public  String MonthEn( String _dataset , String _column ,String _dateFormat)
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		try {
			_ret = Evaluate.monthEn(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
		return _ret;
	}
	
	
	public  String MonthEn( String _paramKey ,String _dateFormat)
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public String Day( String _dataset , String _column , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.day(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	
	public String Day( String _paramKey, String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public String DayOfLast( String _dataset , String _column , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.dayoflast(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String DayOfLast( String _paramKey , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public String Week( String _dataset , String _column , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		try {
			_ret = Evaluate.week(_targetStr, _dateFormat);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return _ret;
	}
	
	public String Week( String _paramKey , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public String WeekKR( String _dataset , String _column , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		try {
			_ret = Evaluate.weekKr(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
		return _ret;
	}
	
	public String WeekKR( String _paramKey , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
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
	public String Time( String _dataset , String _column , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.time(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String Time( String _paramKey , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public String Minute( String _dataset , String _column , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.minute(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String Minute( String _paramKey , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public String Second( String _dataset , String _column , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		Object _valueObject = getDataSetValue(_dataset, _column);
		
		_targetStr =_valueObject.toString();
		
		long _deff=0;
		try {
			_deff = Evaluate.second(_targetStr, _dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		_ret = String.valueOf(_deff);
		
		return _ret;
	}
	
	public String Second( String _paramKey , String _dateFormat )
	{
		String _ret="";
		
		String _targetStr = "";

		if( isParam(_paramKey) ){
			_targetStr = getParameterValue(_paramKey);
		}else{
			_targetStr = _paramKey;
		}
		
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
	public String Now()
	{
		String _ret="";
		try {
			_ret = Evaluate.now();	
		} catch (Exception e) {
			return _ret;
		}
		return _ret;
	}
	
	public String Today(  )
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
	public String Replace( String _datasetName , String _datasetColumnName , String _oldStr , String _newStr)
	{
		String _ret="";
		
		String _replaceTarget="";
			
		Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
		
		if( _ds == null || _ds.toString().equals("") ){
			
		}
		else
		{
			_replaceTarget=_ds.toString();
		}
		
		_ret = Evaluate.replace(_replaceTarget , _oldStr, _newStr);
		
		return _ret;
	}
	
	
	
	
	public String Replace( String _paramKey , String _oldStr , String _newStr)
	{
		String _ret="";
		
		String _replaceTarget="";
			
		_replaceTarget = getParameterValue(_paramKey);
		
		_ret = Evaluate.replace(_replaceTarget , _oldStr, _newStr);
		
		return _ret;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * current page number
	 * */
	public String CurrentPage( )
	{
		String _result="";

		int _currentNum = mCurrentPageNum+1;
		_result = String.valueOf(_currentNum);
		
		return _result;
	}
	
	
	
	public String SectionCurrentPage()
	{
		String _result="";

		int _currentNum = mSectionCurrentPageNum+1;
		_result = String.valueOf(_currentNum);
		
		return _result;
	}
	
	public String SectionTotalPage()
	{
		// total page 값이 필요하다.
		String _result="";
		_result = String.valueOf(mSectionTotalPageNum);
		
		return _result;
	}
	
	/**
	 * cumulative sum of the specified Dataset Column
	 * */
	public String CumulativeSum( String _datasetName , String _datasetColumnName )
	{
		String _result="";
		
		String[] _valueList = getDataSetValueList2( _datasetName , _datasetColumnName , true );

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
		}
		return _result;
	}
	
	/**
	 * total page number
	 * */
	public String TotalPage()
	{
		// total page 값이 필요하다.
		String _result="";
		_result = String.valueOf(mTotalPageNum);
		
		return _result;
	}
	
	
	
	/**
	 * sum value of the group Dataset Column
	 * */
	public String GroupSum(String _datasetName , String _datasetColumnName )
	{
		String _result="";
		
		String[] _valueList=getDataSetValueList2(_datasetName , _datasetColumnName, false);

		if( _valueList != null ){
			BigDecimal _sumValue =Evaluate.sum2(_valueList);
			_result= _sumValue.toString();
		}
		
		return _result;
	}
	
	/**
	 * row number of the group Dataset Column
	 * */
	public String GroupRowNum( )
	{
		// 그룹 리스트 값이 있어야 함.
		String _result="";
		
		int rowNum=mRowIndex + 1;
		
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
		
		return _result;
	}
	
	/**
	 * row number of the Dataset Column
	 * */
	public String RowNum( String _datasetName  )
	{
		String _result="";
		_datasetName = getDataSetName( _datasetName );

		mDataset = mDatasetList.get(_datasetName);
		
		if( mDataset.size() > mRowIndex ){
			int rowNum=mRowIndex + 1;
			_result = String.valueOf(rowNum);
		}
		
		return _result;
	}
	
	/**
	 * */
	public String GroupDataColumn( String _value )
	{
		// 대문자 치환하는 함수가 맞는가?
		String _result="";
		return _result;
	}

	/**
	 * */
	public String GroupCurrentPage( )
	{
		// group current page 값이 필요하다.
		String _result="";
		
		_result = String.valueOf( mGroupCurrentPageIndex+1 );
		
		return _result;
	}
	
	/**
	 * 
	 * */
	public String GroupTotalPage()
	{
		// group total page 값이 필요하다.
		String _result="";
		_result = String.valueOf( mGroupTotalPageIndex ); 
		return _result;
	}
	
	/**
	 * 
	 * */
	public String GroupAvg( String _datasetName , String _datasetColumnName )
	{
		String _result="";
		String[] _valueList=getDataSetValueList2(_datasetName, _datasetColumnName, false);
		
		BigDecimal _avgValue =Evaluate.avg(_valueList); 
		
		_result= _avgValue.toString();
		
		return _result;
	}
	
	
	/**
	 * 
	 * */
	public String GroupCount( String _groupHeaderName )
	{
		String _result="";
		
		_groupHeaderName = _groupHeaderName.trim();
		_groupHeaderName = "§" + _groupHeaderName + "§";
		
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
	public String GroupMax( String _datasetName , String _datasetColumnName )
	{
		String _result="";
		String _maxValue="0"; 
		
		String[] _valueList = getDataSetValueList2( _datasetName ,  _datasetColumnName , false );
		
		_maxValue =Evaluate.max(_valueList);
		
		BigDecimal _bd = new BigDecimal(_maxValue);
		
		_result= _bd.toString();
		
		return _result;
	}
	
	/**
	 *  minimum value of the group Dataset Column.
	 * */
	public String GroupMin( String _datasetName , String _datasetColumnName )
	{
		String _result="";
		String _minValue="0"; 
		
		String[] _valueList = getDataSetValueList2( _datasetName ,  _datasetColumnName , false );
		
		_minValue =Evaluate.min(_valueList);
		BigDecimal _bd = new BigDecimal(_minValue);
		_result= _bd.toString();
		
		return _result;
	}
	

	
	
	
	
	private String[] getDataSetValueList2( String _datasetName , String _datasetColumnName , Boolean _checkGroup )
	{
		String[] _result=null;
		
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
	
	
	
	private String[] getDataSetValueListContition( String _datasetName , String _datasetColumnName , Boolean _checkGroup, String[] _conditionColumn, String[] _conditonValue )
	{
		String[] _result=null;
		
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
			_result=getDataSetListContition( mDataset , _datasetColumnName , mStartIndex , mLastIndex, _conditionColumn, _conditonValue );
		}else{
			_result=getDataSetListContition( mDataset , _datasetColumnName, _conditionColumn, _conditonValue );
		}
		
		return _result;
	}
	
	private String[] getDataSetListContition( ArrayList<HashMap<String, Object>> _list  , String _dataColumn , String[] _conditionColumn, String[] _conditonValue )
	{
		if( _list == null ){
			return null;
		}
		
		int _startIndex = 0;
		int _lastIndex  = 0;
		
		_lastIndex = _list.size();
		
		return getDataSetListContition(_list,_dataColumn, _startIndex, _lastIndex, _conditionColumn, _conditonValue );
	}
	
	private String[] getDataSetListContition( ArrayList<HashMap<String, Object>> _list  , String _dataColumn , int _startIndex , int _lastIndex, String[] _conditionColumn, String[] _conditonValue )
	{
		if( _list == null ){
			return null;
		}
		
		int _length=_lastIndex-_startIndex;
		
		if( _list.size() - _startIndex <  _length )
		{
			_length = _list.size() - _startIndex;
		}
		
		boolean isChk = false;
		
		HashMap<String, Object> _dataHm;
		Object _dataValue;
		
		// last index에  +1을 해야하나?
		int i;
		int j;
		int _conditionSize = _conditionColumn.length;
		boolean _isFlag = false;
		
		ArrayList<String> _sumList = new ArrayList<String>();
		
		for( i=_startIndex; i< _lastIndex;  i++ ){
			
			if( i >= _list.size())
			{
				continue;
			}
			
			_dataHm = _list.get(i);
			_dataValue = _dataHm.get(_dataColumn);
			
			for( j = 0; j < _conditionSize; j++ )
			{
				if( _dataHm.containsKey(_conditionColumn[j]) && _dataHm.get(_conditionColumn[j]).equals(_conditonValue[j]) )
				{
					_isFlag = true;
				}
				else
				{
					_isFlag = false;
					break;
				}
			}
			
			if( _isFlag )
			{
				String _value="0";
				try {
					_value = _dataValue.toString().replaceAll("[^\\d.-]", "");
					isChk = true;
					
				} catch (Exception e) {
					_value = "0";
				}
				
				_sumList.add(_value);
			}
			
		}
		
		String[] _valueList = new String[_sumList.size()];
		_valueList = (String[]) _sumList.toArray(_valueList);
		
		if(isChk == false) return null;
		
		return _valueList;
	}
	
	
	
	
	
	
	
	
	
	private String[] getDataSetValueListBoundray2( String _datasetName , String _datasetColumnName,int _startIndex, int _lastIndex , Boolean _checkGroup )
	{
		String[] _result=null;
		
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
		
		_datasetName = _value;
		_datasetName = _datasetName.trim();
		
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
	
	
	
	private String getDataSetNameGroup2( String _datasetName, String _datasetColumnName, boolean _isGroup )
	{
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
	
	
	public void setCloneIndex( int value )
	{
		mCloneIndex = value;
	}
	public int CloneIndex()
	{
		return mCloneIndex;
	}
	
	/**
	 * dataset의 첫번째 인자값을 두번째 인자값으로 변환하여, 반환
	 * _isOriDataFlag = false
	 * */
	public String RowDataValue( String _datasetName , String _datasetColumnName , int _rowIndex , boolean _isOriDataFlag )
	{
		String _ret="";
		
		
		
		//2017-11-30 (  3번째 인자 추가  true : 그룹밴드이더라도 1번째 인자값으로 받은 데이터셋 그대로 사용  )
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
	
	
	public String BoundarySum(  String _datasetName , String _datasetColumnName,int _startIndex, int _lastIndex )
	{
		String _ret="";
		
		String[] _valueList;

		_valueList=getDataSetValueListBoundray2( _datasetName , _datasetColumnName,_startIndex, _lastIndex , true );	
		
		if( _valueList != null ){
			BigDecimal _sumValue = null;
			_sumValue =Evaluate.sum2(_valueList);
			
			if(_sumValue != null)_ret= _sumValue.toString();
			else _ret= "0";
		}
		return _ret;
	}
	
	
	public String BoundaryMax(  String _datasetName , String _datasetColumnName,int _startIndex, int _lastIndex )
	{
		String _ret="";
		
		String[] _valueList;
			
		_valueList=getDataSetValueListBoundray2( _datasetName , _datasetColumnName,_startIndex, _lastIndex , true );	
		
		if( _valueList != null ){
			BigDecimal _sumValue = null;
			_sumValue = new BigDecimal( Evaluate.max(_valueList) );
			
			if(_sumValue != null)_ret= _sumValue.toString();
			else _ret= "0";
		}
		return _ret;
	}
	
	public String BoundaryMin(  String _datasetName , String _datasetColumnName,int _startIndex, int _lastIndex )
	{
		String _ret="";
		
		String[] _valueList;
			
		_valueList=getDataSetValueListBoundray2( _datasetName , _datasetColumnName,_startIndex, _lastIndex , true );	
		
		if( _valueList != null ){
			BigDecimal _sumValue = null;
			_sumValue = new BigDecimal( Evaluate.min(_valueList) );
			
			if(_sumValue != null)_ret= _sumValue.toString();
			else _ret= "0";
		}
		return _ret;
	}
	
	public String BoundaryAvg(  String _datasetName , String _datasetColumnName,int _startIndex, int _lastIndex )
	{
		String _ret="";
		
		String[] _valueList;
			
		_valueList=getDataSetValueListBoundray2( _datasetName , _datasetColumnName,_startIndex, _lastIndex , true );	
		
		if( _valueList != null ){
			BigDecimal _sumValue = null;
			_sumValue = Evaluate.avg(_valueList);
			
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
	public String DateCalculation( String _datasetName , String _datasetColumnName , int _addDate  , String _dateType  , String _resultDateType  , String _AddType )
	{
		String _ret="";
		
		String _dataSetValue="";
		
		Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
		if( _ds == null ){
			return _ret;
		}
		_dataSetValue=_ds.toString();
	
		
		// 연산할 일자 값
		
		//기본값으로 일자로 연산하도록 셋팅
		int _calenderAddType = Calendar.DATE;
		
		// 년/월/일 중 연산할값이 따로 있을경우
		
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
		
		try {
			_ret = Evaluate.dateCalculation(_dataSetValue, _addDate, _dateType, _resultDateType, _calenderAddType );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			_ret = "";
		}
		
		return _ret;
	}
	
	
	public String DateCalculation( String _paramKey , int _addDate  , String _dateType  , String _resultDateType  , String _AddType )
	{
		String _ret="";
		
		String _dataSetValue="";
		if( isParam(_paramKey) ){
			_dataSetValue = getParameterValue(_paramKey);
		}else{
			_dataSetValue = _paramKey;
		}
		
		
		// 연산할 일자 값
		
		//기본값으로 일자로 연산하도록 셋팅
		int _calenderAddType = Calendar.DATE;
		
		// 년/월/일 중 연산할값이 따로 있을경우
		
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
		
		try {
			_ret = Evaluate.dateCalculation(_dataSetValue, _addDate, _dateType, _resultDateType, _calenderAddType );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			_ret = "";
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
	
	
	
	/**
	 * '#§§'
	 * "#§§", "#,"
	 * */
	public String NumberFormatter(  String _datasetName, String _datasetColumnName , String _formatterStr , String _usePercentData )
	{
		String _ret="";
		
		String _dataValue = "";
		
		
				
		Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
		
		if(_ds == null) return "";

		if( _ds == "" && mDatasetList.containsKey(_datasetName) == false ){
			_dataValue = "";
		}
		else
		{
			_dataValue=_ds.toString();
		}
				
			
			
		
		try {
			
			if( _usePercentData.equals("false") )
			{
				_formatterStr = _formatterStr.replace("%", "§");
			}
			
			DecimalFormat _decFm2 = new DecimalFormat(_formatterStr);
			BigDecimal _bigDecimalValue2 = new BigDecimal(_dataValue);
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
		
		
		return _ret;
	}
	
	
	public String NumberFormatter(  String _paramKey , String _formatterStr , String _usePercentData )
	{
		String _ret="";
		
		String _dataValue = "";
		
		if( isParam(_paramKey) ){
			_dataValue = getParameterValue(_paramKey);
		}else{
			_dataValue = _paramKey;
		}
		
		try {
			
			if( _usePercentData.equals("false") )
			{
				_formatterStr = _formatterStr.replace("%", "§");
			}
			
			DecimalFormat _decFm2 = new DecimalFormat(_formatterStr);
			BigDecimal _bigDecimalValue2 = new BigDecimal(_dataValue);
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
		
		
		return _ret;
	}
	
	
	
	
	
	/***
	 * functionName : getConditionValueFunction : 특정 콘디션에서 지정한 값과 같은 결과 컬럼의 값을 리턴
	 * 콘디숀 맵파일은 UFile/sys/SYS/conditionMap.xml 파일에 정의되어 있다.
	 * @param _value ex) getConditionValue(콘디숀아이디, 기준값(dataset_0.col_1))
	 * @return
	 */
	public String getConditionValue( String _conditionId , String _datasetFieldStr )
	{
		String _ret="";
		
		String _datasetName = "";
		String _datasetColumnName = "";
		
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
		
		_ret = getConditionMapData(_conditionId, _conditionValue );
		
		
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
	
	public String GroupLength( String _value  )
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
	
	public String RowIndex(  )
	{
		String _result = String.valueOf( mRowIndex + 1 );
		
		return _result;
	}
	
	
	
	
	/**
	 * data에 빈값이 존재하는지 판단한다.
	 * 현재 Row비교 : none , 현재 데이터셋 비교 : group, 전체 데이터셋 비교 :  all 
	 * */
	public String IsEmpty( String _datasetName , String _columnName , String _type )
	{
		String _ret="true";
		
		
		// 	현재 Row비교 : none , 현재 데이터셋 비교 : group, 전체 데이터셋 비교 :  all 
		
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
		_datasetName = getDataSetNameGroup2(_datasetName , _columnName, _isGroup);

		_list = getDataSetGrp(_datasetName, _isGroup);
	
		HashMap<String, Object>  _rowItem=null;
		Object _rowItemValue = null;
		
		if( _list != null )
		{
			
			if( _type.equals("none") ){
				
				// 해당 Row가 없을경우 true 리턴
				if( _list.size() > this.mRowIndex )
				{
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
	
	
	
	/**
	 * DateFormat 함수 ( 데이터셋, 입력 포멧, 출력 포멧, Locale (en, ja, ko,....) )
	 * @return
	 */
	public String DateFormat( String _datasetName, String _datasetColumnName , String _dateType , String _resultDateType , String _locale)
	{
		
		String _ret="";
		String resultDate="";
		
		
		String _dataSetValue="";
				
		Object _ds = getDataSetValue( _datasetName , _datasetColumnName );
		if( _ds == null ){
			return _ret;
		}
		_dataSetValue=_ds.toString();
		
		
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
	    
	    if( _locale.equals("")) 
		{
	    	df = new SimpleDateFormat( _resultDateType );
		}
	    
		else
		{
			df = new SimpleDateFormat( _resultDateType ,  new Locale( _locale ) );
		}
        
        resultDate = df.format(_date.getTime());
	

        return resultDate;	
	}
	
	public String DateFormat( String _paramKey , String _dateType , String _resultDateType , String _locale)
	{
		String resultDate="";
		
		String _dataSetValue="";
		if( isParam(_paramKey) ){
			_dataSetValue = getParameterValue(_paramKey);
		}else{
			_dataSetValue = _paramKey;
		}
		
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
	    
	    if( _locale.equals("")) 
		{
	    	df = new SimpleDateFormat( _resultDateType );
		}
		else
		{
			df = new SimpleDateFormat( _resultDateType ,  new Locale( _locale ) );
		}
        
        resultDate = df.format(_date.getTime());
	

        return resultDate;	
	}
	
	public ArrayList<HashMap<String, Object>> getDataSetList( String _dataSetName )
	{
		ArrayList<HashMap<String, Object>> _resultList = new ArrayList<HashMap<String, Object>>();
		
		_resultList = mDatasetList.get(_dataSetName);
		
		return _resultList;
	}
	
	
	public String ConditionSum( String dataset , String column, String[] conditionColumns, String[] conditionValues )
	{
		String _ret="";
		String[] _valueList;
			
		_valueList=getDataSetValueListContition( dataset , column , true, conditionColumns, conditionValues );
		
		if( _valueList != null ){
			BigDecimal _sumValue =Evaluate.sum2(_valueList);
			_ret= _sumValue.toString();
		}
		
		return _ret;
	}
	public String ConditionAvg( String dataset , String column, String[] conditionColumns, String[] conditionValues )
	{
		String _ret="";
		String[] _valueList;
		
		_valueList=getDataSetValueListContition( dataset , column , true, conditionColumns, conditionValues );
		
		if( _valueList != null ){
			BigDecimal _sumValue =Evaluate.avg(_valueList);
			_ret= _sumValue.toString();
		}
		
		return _ret;
	}
	public String ConditionMax( String dataset , String column, String[] conditionColumns, String[] conditionValues )
	{
		String _ret="";
		String[] _valueList;
		
		_valueList=getDataSetValueListContition( dataset , column , true, conditionColumns, conditionValues );
		
		if( _valueList != null ){
			String _maxValue =Evaluate.max(_valueList);
			BigDecimal _bd = new BigDecimal(_maxValue);
			_ret= _bd.toString();
		}
		
		return _ret;
	}
	public String ConditionMin( String dataset , String column, String[] conditionColumns, String[] conditionValues )
	{
		String _ret="";
		String[] _valueList;
		
		_valueList=getDataSetValueListContition( dataset , column , true, conditionColumns, conditionValues );
		
		if( _valueList != null ){
			String _minValue =Evaluate.min(_valueList);
			BigDecimal _bd = new BigDecimal(_minValue);
			_ret= _bd.toString();
		}
		
		return _ret;
	}
	
	
	
	public String PointToPixel( String _value )
	{
		String _pixel="";
		
		float _ptSize=Float.parseFloat(_value);
		
		float _pxSize=_ptSize *  96 / 72;
		
		_pixel=String.valueOf(_pxSize);
		
		return _pixel;
	}
	
	public String NVL( String _value, Object _nvlValue )
	{
		
		if( _value == null )
		{
			return _nvlValue.toString();
		}
		
		return _value;
	}

	public String NVL( String _dataset, String _column , String _nvlValue )
	{
		
		Object _target = "";
		
		_target =getDataSetValue(_dataset, _column);
		
		
		if( _target == null )
		{
			return _nvlValue.toString();
		}
		
		return _target.toString();
	}
	
	
	public String getUserProperties(String _keyName)
	{
		String _resultStr = "";
		
		if( Log.ubiformUserPropJson != null && Log.ubiformUserPropJson.containsKey(_keyName) && Log.ubiformUserPropJson.get(_keyName) != null )
		{
			_resultStr = Log.ubiformUserPropJson.get(_keyName).toString();
		}
		
		return _resultStr;
	}
	
	
}
