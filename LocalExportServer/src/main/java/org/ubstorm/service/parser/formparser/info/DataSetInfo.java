package org.ubstorm.service.parser.formparser.info;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *	데이터셋 정보를 담아두는 객체 
 *	건별로 데이터셋의 정보를 담아둔다.
 **/
public class DataSetInfo {	
	
	protected String mTypeName;
	protected String mDelimiter;

	protected String mClassName;

	protected String mFirstRow = "false";
	protected String mJason;
	protected String mPath;
	protected String mFileType;
	protected int mTotalCount = -1;
	protected int mAddCount = -1;
	protected String mID;
	
	
	protected String mUrl = "";	
	protected String mParamType = "";	
	protected String mParamValue = "";
	protected String mParamEvent = "";

	
	protected String rootElement = "";
	protected String dataElement = "";
	protected String httpType = "";
	protected String systemCode = "";
	protected String reference = "";
	protected String referenceID = "";
	protected String sql_type = "";
	protected String encoding_data = "";
	protected String encoding_type = "";
	protected String compress = "";	
	protected String db_type = "";
	protected String db_server = "";
	protected String db_database = "";
	protected String db_id = "";
	protected String db_pw = "";
	protected String sql = "";
	
	protected String crud = "";	
	protected String title = "";
	protected String db_port = "";
	protected String orderBy = "";
	protected String queryType = "";
	protected String serverType = "";
	protected String domain = "";
	protected String queryVersion = "";	
	
	protected String alias = "";
	protected String displayName = "";	
	
	protected String dataStructureType = "";	

	protected String isEncodingSQL = "";
	
	protected String mData = "";
	
	protected ArrayList<HashMap<String, String>> mColumns;
	protected ArrayList<HashMap<String, Object>> mSorts;
	protected ArrayList<HashMap<String, String>> mParams;
	protected ArrayList<HashMap<String, String>> mMergedInfoList;
	
	
	protected String mJsonDataType = "";
	protected String mJsonObjectKey = "";

	protected String mTagRefID = "";
	
	protected String mUriEncode = "";
	
	protected String mBase64Encode = "";
	
	interface IMethodBar {
		void callMethod( Object value );
	}

	HashMap<String, IMethodBar> propertyMapping = new HashMap<String, IMethodBar>();

	
	public DataSetInfo() {
		// TODO Auto-generated constructor stub
	}
	
	public DataSetInfo( HashMap<String, Object> _value ) {
		// TODO Auto-generated constructor stub
		propertyMappingFn();
		
		setProperties(_value);
	}
	
	//project기본 셋팅
	private void setProperties( HashMap<String, Object> _jObj )
	{
		for (Object key : _jObj.keySet()) {
	        String keyStr = (String)key;
	        Object keyvalue = _jObj.get(keyStr);
	        
	        if( propertyMapping.containsKey(keyStr) && keyvalue != null )
	        {
				propertyMapping.get(keyStr).callMethod( keyvalue );
	        }
	    }
	}
	
	
	private void propertyMappingFn()
	{
		propertyMapping.put("id", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setId( value.toString() ); } } );

		propertyMapping.put("className", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setClassName( value.toString() ); } } );

		propertyMapping.put("typeName", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setTypeName( value.toString() ); } } );

		propertyMapping.put("delimiter", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDelimiter( value.toString() ); } } );

		propertyMapping.put("firstRow", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setFirstRow( value.toString() ); } } );
		
		propertyMapping.put("jason", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setJason( value.toString() ); } } );
		
		propertyMapping.put("path", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setPath( value.toString() ); } } );

		propertyMapping.put("fileType", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setFileType( value.toString() ); } } );
		
		propertyMapping.put("url", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setUrl( value.toString() ); } } );

		propertyMapping.put("paramType", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setParamType( value.toString() ); } } );

		propertyMapping.put("paramValue", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setParamValue( value.toString() ); } } );
		
		propertyMapping.put("paramEvent", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setParamEvent(value.toString() ); } } );
		
		propertyMapping.put("rootElement", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setRootElement( value.toString() ); } } );
		
		propertyMapping.put("dataElement", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDataElement( value.toString() ); } } );
		
		propertyMapping.put("httpType", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setHttpType( value.toString() ); } } );
		
		propertyMapping.put("systemCode", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setSystemCode( value.toString() ); } } );
		
		propertyMapping.put("reference", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setReference( value.toString() ); } } );
		
		propertyMapping.put("referenceID", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setReferenceID( value.toString() ); } } );
		
		propertyMapping.put("sql_type", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setSql_type( value.toString() ); } } );
		
		propertyMapping.put("encoding_data", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setEncoding_data( value.toString() ); } } );
		
		propertyMapping.put("encoding_type", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setEncoding_type( value.toString() ); } } );
		
		propertyMapping.put("compress", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setCompress( value.toString() ); } } );
		
		propertyMapping.put("db_type", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDb_type( value.toString() ); } } );
		
		propertyMapping.put("db_server", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDb_server( value.toString() ); } } );
		
		propertyMapping.put("db_database", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDb_database( value.toString() ); } } );
		
		propertyMapping.put("db_id", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDb_id( value.toString() ); } } );
		
		propertyMapping.put("db_pw", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDb_pw( value.toString() ); } } );
		
		propertyMapping.put("sql", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setSql( value.toString() ); } } );
		
		propertyMapping.put("crud", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setCrud( value.toString() ); } } );
		
		propertyMapping.put("title", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setTitle( value.toString() ); } } );
		
		propertyMapping.put("db_port", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDb_port( value.toString() ); } } );
		
		propertyMapping.put("orderBy", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setOrderBy( value.toString() ); } } );
		
		propertyMapping.put("queryType", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setQueryType( value.toString() ); } } );
		
		propertyMapping.put("serverType", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setServerType( value.toString() ); } } );
		
		propertyMapping.put("domain", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDomain( value.toString() ); } } );
		
		propertyMapping.put("dataStructureType", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDataStructureType( value.toString() ); } } );
		
		propertyMapping.put("queryVersion", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setQueryVersion( value.toString() ); } } );
		
		propertyMapping.put("alias", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setAlias( value.toString() ); } } );
		
		propertyMapping.put("displayName", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setDisplayName( value.toString() ); } } );
		
		propertyMapping.put("columns", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setColumn( (ArrayList<HashMap<String, String>> ) value ); } } );

		propertyMapping.put("params", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setParams( (ArrayList<HashMap<String, String>> ) value ); } } );

		propertyMapping.put("data", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setData((String) value ); } } );

		propertyMapping.put("uriEncode", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setUriEncode( value.toString() ); } } );

		propertyMapping.put("base64Encode", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setBase64Encode( value.toString() ); } } );

		propertyMapping.put("isEncodingSQL", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setIsEncodingSQL( value.toString() ); } } );

		propertyMapping.put("mergedinfo", new IMethodBar(){@Override 
			public void callMethod( Object value ) {
			setMergedInfoList((ArrayList<HashMap<String, String>> ) value ); } } );

	}
	
	public String getId()
	{
		return mID;
	}
	public void setId( String _value )
	{
		this.mID = _value;
	}
	
	public String getClassName()
	{
		return mClassName;
	}
	public void setClassName( String _value )
	{
		mClassName = _value;
	}
	
	
	public String getTypeName() {
		return mTypeName;
	}

	public void setTypeName(String mTypeName) {
		this.mTypeName = mTypeName;
	}

	public String getDelimiter() {
		return mDelimiter;
	}

	public void setDelimiter(String mDelimiter) {
		this.mDelimiter = mDelimiter;
	}

	public String getFirstRow() {
		return mFirstRow;
	}

	public void setFirstRow(String mFirstRow) {
		this.mFirstRow = mFirstRow;
	}

	public String getJason() {
		return mJason;
	}

	public void setJason(String mJason) {
		this.mJason = mJason;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String mPath) {
		this.mPath = mPath;
	}
	
	public String getFileType() {
		return mFileType;
	}

	public void setFileType(String mFileType) {
		this.mFileType = mFileType;
	}

	public int getTotalCount() {
		return mTotalCount;
	}

	public void setTotalCount(int mTotalCount) {
		this.mTotalCount = mTotalCount;
	}

	public int getAddCount() {
		return mAddCount;
	}

	public void setAddCount(int mAddCount) {
		this.mAddCount = mAddCount;
	}

	public String getID() {
		return mID;
	}

	public void setID(String mID) {
		this.mID = mID;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public String getParamType() {
		return mParamType;
	}

	public void setParamType(String mParamType) {
		this.mParamType = mParamType;
	}

	public String getParamValue() {
		return mParamValue;
	}

	public void setParamValue(String mParamValue) {
		this.mParamValue = mParamValue;
	}

	public String getParamEvent() {
		return mParamEvent;
	}

	public void setParamEvent(String mParamEvent) {
		this.mParamEvent = mParamEvent;
	}

	public String getRootElement() {
		return rootElement;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	public String getDataElement() {
		return dataElement;
	}

	public void setDataElement(String dataElement) {
		this.dataElement = dataElement;
	}

	public String getHttpType() {
		return httpType;
	}

	public void setHttpType(String httpType) {
		this.httpType = httpType;
	}

	public String getSystemCode() {
		return systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getReferenceID() {
		return referenceID;
	}

	public void setReferenceID(String referenceID) {
		this.referenceID = referenceID;
	}

	public String getSql_type() {
		return sql_type;
	}

	public void setSql_type(String sql_type) {
		this.sql_type = sql_type;
	}

	public String getEncoding_data() {
		return encoding_data;
	}

	public void setEncoding_data(String encoding_data) {
		this.encoding_data = encoding_data;
	}

	public String getEncoding_type() {
		return encoding_type;
	}

	public void setEncoding_type(String encoding_type) {
		this.encoding_type = encoding_type;
	}

	public String getCompress() {
		return compress;
	}

	public void setCompress(String compress) {
		this.compress = compress;
	}

	public String getDb_type() {
		return db_type;
	}

	public void setDb_type(String db_type) {
		this.db_type = db_type;
	}

	public String getDb_server() {
		return db_server;
	}

	public void setDb_server(String db_server) {
		this.db_server = db_server;
	}

	public String getDb_database() {
		return db_database;
	}

	public void setDb_database(String db_database) {
		this.db_database = db_database;
	}

	public String getDb_id() {
		return db_id;
	}

	public void setDb_id(String db_id) {
		this.db_id = db_id;
	}

	public String getDb_pw() {
		return db_pw;
	}

	public void setDb_pw(String db_pw) {
		this.db_pw = db_pw;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getCrud() {
		return crud;
	}

	public void setCrud(String crud) {
		this.crud = crud;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDb_port() {
		return db_port;
	}

	public void setDb_port(String db_port) {
		this.db_port = db_port;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getQueryVersion() {
		return queryVersion;
	}

	public void setQueryVersion(String queryVersion) {
		this.queryVersion = queryVersion;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDataStructureType() {
		return dataStructureType;
	}

	public void setDataStructureType(String dataStructureType) {
		this.dataStructureType = dataStructureType;
	}

	public ArrayList<HashMap<String, String>> getColumns() {
		return mColumns;
	}

	public void setmColumns(ArrayList<HashMap<String, String>> _columns) {
		this.mColumns = _columns;
	}
	
	
	public void setData( String _value )
	{
		this.mData = _value;
	}
	public String getData()
	{
		return mData;
	}
	
	public ArrayList<HashMap<String, String>> getMergedInfoList()
	{
		return mMergedInfoList;
	}

	public void setMergedInfoList(ArrayList<HashMap<String, String>> _value )
	{
		mMergedInfoList = _value;
	}
	
	public void setColumn( ArrayList<HashMap<String, String>> _value )
	{
		this.mColumns = _value;
	}
	
	public void setParams(ArrayList<HashMap<String, String>> _value)
	{
		mParams = parameterUpdate(_value);
	}
	public ArrayList<HashMap<String, String>> getParams()
	{
		return mParams;
	}
	
	// 파라미터 사용여부 확인
	private ArrayList<HashMap<String, String>> parameterUpdate( ArrayList<HashMap<String, String>> _value )
	{
		int i = 0; 
		int _cnt = 0;
		HashMap<String, String> _paramData;
		ArrayList<HashMap<String, String>> _resultArray = new ArrayList<HashMap<String, String>>();
		
		// 파라미터를 읽어들여서 
		if( _value != null && _value.size() > 0 )
		{
			_cnt = _value.size();
			
			for( i=0; i < _cnt; i++ )
			{
				_paramData = _value.get(i);
				//_paramProp.containsKey("useParam") && _paramProp.get("useParam").equals("N") && _paramMap.containsKey(_paramID)
				
				if( _paramData.containsKey("useParam") == false || _paramData.get("useParam").equals("N") == false )
				{
					_resultArray.add(_paramData);
				}
			}
			
		}
		
		return _resultArray;
	}
	
	
	public ArrayList<String> getDateFieldList()
	{
		ArrayList<String> _resultAr = new ArrayList<String>();
		if( mColumns.size() > 0 )
		{
			for( int i=0; i < mColumns.size(); i++ )
			{
				if( mColumns.get(i).containsKey("dataField"))
				{
					_resultAr.add( mColumns.get(i).get("dataField") );
				}
			}
		}
		
		return _resultAr;
	}
	
	public String getUriEncode()
	{
		return mUriEncode;
	}
	
	public void setUriEncode( String _value )
	{
		this.mUriEncode = _value;
	}

	public String getBase64Encode()
	{
		return mBase64Encode;
	}
	
	public void setBase64Encode( String _value )
	{
		this.mBase64Encode = _value;
	}

	public String getIsEncodingSQL()
	{
		return isEncodingSQL;
	}
	
	public void setIsEncodingSQL( String _value )
	{
		this.isEncodingSQL = _value;
	}
	
	public String getJsonDataType()
	{
		return mJsonDataType;
	}
	
	public void setJsonDataType( String _value )
	{
		this.mJsonDataType = _value;
	}

	public String getJsonObjectKey()
	{
		return mJsonObjectKey;
	}
	
	public void setJsonObjectKey( String _value )
	{
		this.mJsonObjectKey = _value;
	}
	
	public String getTagRefID()
	{
		return this.mTagRefID;
	}
	
	
}

