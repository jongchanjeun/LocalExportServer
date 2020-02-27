/**
 * 2015-09-02
 * 최명진
 * 
 * Ubform의 CrossTabBand의 처리를 담당하는 Class
 */


package org.ubstorm.service.parser.formparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.ProjectInfo;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class CrossTabBandParser {
	
	String mSummeryHeaderText = "&sum";
	String mSummeryValueText = "&summery";
	String mGrandValueText = "&grand";
	String mTitleText = "&title";
	String mTitleSumText = "&sum";
	String mLeftFixedText = "&lfixed";
	String mRightFixedText = "&rfixed";
	String mTitleTotText = "&total";
	String mEmptyText = "&empty";
	Array mUbFx;
	String mHeaderGrandTotText = "§grand total";
	String mHeaderTotalText = "§total";
	String mHeaderSumText = "§sum";
	String FXFUNCTION_IF = "if";
	String GRANDSUMMARY_TYPE_ALL = "all";
	String GRANDSUMMARY_TYPE_HORIZONTAL = "horizontal";
	String GRANDSUMMARY_TYPE_VERTICAL = "vertical";
	
	String THIS_TEXT = "this.text";
	String UBFX_COL = "col_";
	
	float mMaxPageWidth = 0;
	ArrayList<Float> mMaxPageWidthAr = new ArrayList<Float>();
	boolean isExportData = false;
	float mRowNumWidth = 40;
	
	boolean mIsExportTable = true;
	
	public static final String CROSSTAB_VERSION_1 = "1.0"; 
	public static final String CROSSTAB_VERSION_2 = "2.0"; 
	
	// Export여부를 판단하기 위한 변수
	private String isExportType = "";
	private String isExcelOption = "";
	
	String FUNCTION_VERSION = "1.0";
	
	public void setFunctionVersion( String _value )
	{
		FUNCTION_VERSION = _value;
	}
	
	public boolean isExportData() {
		return isExportData;
	}

	public void setExportData(boolean isExportData) {
		this.isExportData = isExportData;
	}

	public float getMaxPageWidth()
	{
		return mMaxPageWidth;
	}
	
	public ArrayList<Float> getMaxPageWidthAr()
	{
		return mMaxPageWidthAr;
	}
	
	public void setIsExportType(String _value)
	{
		isExportType = _value;
	}

	public void setIsExcelOption(String _value)
	{
		isExcelOption = _value;
	}
	
	HashMap<String, ArrayList<HashMap<String, Object>>> DataSet;
	
	public void testLoadXml(Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt) throws Exception 
	{
		
		NodeList _child = _page.getElementsByTagName("item");
		
		DataSet = _data;
		
		// xml Item
		for(int j = 0; j < _child.getLength() ; j++)
		{
			Element _childItem = (Element) _child.item(j);

			String _itemId = _childItem.getAttribute("id");
			String _className = _childItem.getAttribute("className");
			
			if( _className.toUpperCase().equals("UBCROSSTABBAND") ) {
//				convertCrossTabXmltoItem(_childItem, _data);
			}
			
		}
		
		
		
	}
	
	/**
	 * functionName :	convertCrossTabXmlToItem</br>
	 * desc			:	크로스탭의 정보를 가지고있는 xml을 크로스탭 object로 변환 
	 * @return
	 * @throws XPathExpressionException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public ArrayList<ArrayList<HashMap<String, Value>>> convertCrossTabXmltoItem( Element _pageXml, HashMap<String, ArrayList<HashMap<String, Object>>> dataSet, Float pageWidth, Float startPosition, Float maxPosition, Float currentStartPosition, ArrayList<Integer> mXAr )throws IOException, XPathExpressionException, SAXException, ParserConfigurationException
	{
		
		String _itemId = "";
		HashMap<String, Value> _infoObj = new HashMap<String, Value>();		// 크로스탭의 기본 정보가 담긴 HashMap 데이터
		HashMap<String, ArrayList<String>> _arrayListData;
		HashMap<String, Value> _crossTabMap = new HashMap<String, Value>();
		
		ArrayList<String> _colAr = new ArrayList<String>();		// Col 정보
		ArrayList<String> _rowAr = new ArrayList<String>();		// Row 정보
		ArrayList<String> _valAr = new ArrayList<String>();		// Val 정보 
		Element _childItem;
		ArrayList<String> _leftFixedColumnAr 	= new ArrayList<String>();
		ArrayList<String> _rightFixedColumnAr 	= new ArrayList<String>();
		Boolean _headerLabelVisible = false;
		
		_itemId =  _pageXml.getAttribute("id");
		
//		NodeList _propertys = _pageXml.getElementsByTagName("property"); 
		
//		XPath _xpath = XPathFactory.newInstance().newXPath();
//		NodeList _propertys = (NodeList) _xpath.evaluate("property", _pageXml, XPathConstants.NODESET);
//		NodeList _propertys = _pageXml.getElementsByTagName("property");
		NodeList _propertys = _pageXml.getChildNodes();
		
		DataSet = dataSet;
		
		for (int i = 0; i < _propertys.getLength(); i++) {
			
			if( _propertys.item(i) instanceof Element )
			{
				_childItem = (Element) _propertys.item(i);
				
				if(_childItem.getTagName().equals("property"))
				{
					if( !(_childItem.getAttribute("name").equals("rightFixedColumn") || _childItem.getAttribute("name").equals("leftFixedColumn")) && 
							_childItem.getAttribute("value").equals("") == false ){
						_infoObj.put(_childItem.getAttribute("name"), new Value( URLDecoder.decode( _childItem.getAttribute("value"), "UTF-8" ), _childItem.getAttribute("type") ) );
					}
					else
					{
						if(_childItem.getAttribute("name").equals("rightFixedColumn") || _childItem.getAttribute("name").equals("leftFixedColumn") )
						{
							if( !URLDecoder.decode( _childItem.getAttribute("value"), "UTF-8").equals("null") && !URLDecoder.decode( _childItem.getAttribute("value"), "UTF-8").equals("") )
							{
								_infoObj.put(_childItem.getAttribute("name"), new Value( Value.setArrayString( URLDecoder.decode( _childItem.getAttribute("value"), "UTF-8" ) ),"arraystr" ) );
							}
						}
					}
				}
				
			}
			
		}
		
		//info 정보에 version 담기
		String _crossTabVersion = CROSSTAB_VERSION_1;
		if( _infoObj.containsKey("version"))
		{
			_crossTabVersion = _infoObj.get("version").getStringValue();
		}
		
		// 데이터셋 정보 담아두기
		if( _infoObj.get("rowNum") != null && _infoObj.get("rowNum").getBooleanValue() ){
			_rowAr.add("$No");
		}
		
		NodeList crossTabData 		= _pageXml.getElementsByTagName("crossTabData");
		// xml의 crossTabData 로드
		Element _crosstabDataNode 	= (Element) crossTabData.item(0);
		NodeList crossTabDataNodes 	= _crosstabDataNode.getElementsByTagName("property");
		
		for (int k = 0; k < crossTabDataNodes.getLength(); k++) {
			
			_childItem = (Element) crossTabDataNodes.item(k);
			
			if( _childItem.getParentNode().getNodeName().equals("value") ){
				_valAr.add(_childItem.getAttribute("value"));
			}else if( _childItem.getParentNode().getNodeName().equals("column") ){
				_colAr.add(_childItem.getAttribute("value"));
			}else if( _childItem.getParentNode().getNodeName().equals("row") ){
				_rowAr.add(_childItem.getAttribute("value"));
			}
			
		}
		
		
		// SummaryVisible 담기
		if( _infoObj.containsKey("summaryVisible") ){
			_crossTabMap.put("summaryVisible", new Value( _infoObj.get("summaryVisible").getBooleanValue(), "boolean" ));
		}else{
			_crossTabMap.put("summaryVisible", new Value( true, "boolean" ));
		}
		
		// SummaryType 담기
		if( _infoObj.containsKey("summaryType") ){
			_crossTabMap.put("summaryType", new Value( _infoObj.get("summaryType").getStringValue(), "string" ));
		}else{
			_crossTabMap.put("summaryType", new Value( GlobalVariableData.GRANDSUMMARY_TYPE_ALL, "string" ));
		}
		
		// grandSummaryVisible 담기
		if( _infoObj.containsKey("grandSummaryVisible") ){
			_crossTabMap.put("grandSummaryVisible", new Value( _infoObj.get("grandSummaryVisible").getBooleanValue(), "boolean" ));
		}else{
			_crossTabMap.put("grandSummaryVisible", new Value( true, "boolean" ));
		}
		
		// grandSummaryType 담기
		if( _infoObj.containsKey("grandSummaryType") ){
			_crossTabMap.put("grandSummaryType", new Value( _infoObj.get("grandSummaryType").getStringValue(), "string" ));
		}else{
			_crossTabMap.put("grandSummaryType", new Value( GlobalVariableData.GRANDSUMMARY_TYPE_ALL, "string" ));
		}
		
		// LeftFixed Column값 저장
		if( _infoObj.containsKey("leftFixedColumn") ){
			_leftFixedColumnAr = _infoObj.get("leftFixedColumn").getArrayStringValue();
		}

		// RightFixed Column값 저장
		if( _infoObj.containsKey("rightFixedColumn") ){
			_rightFixedColumnAr =  _infoObj.get("rightFixedColumn").getArrayStringValue();
		}
		
		if( _infoObj.containsKey("headerLabelVisible") && _infoObj.get("headerLabelVisible").getBooleanValue() == true ){
			_headerLabelVisible = _infoObj.get("headerLabelVisible").getBooleanValue();
		}
		
		
		// isExportData가 true일경우 info속성의 newPage속성값을 강제로 false 로 지정해야함
		if(isExportData) _infoObj.put("newPage", Value.fromBoolean(false) );
		
		//컬럼별 합계를 위한 정보를 담아서 DataParser시 화면에 표시하도록 처리
		_crossTabMap.put("info",new Value(_infoObj,"map"));
		
		// crossTab의 버전 담기
		_crossTabMap.put("VERSION",new Value(_crossTabVersion,"string"));
		
		HashMap<String, Value> _arrayList = new HashMap<String, Value>();
		_arrayList.put("COL", new Value(_colAr, "array"));
		_arrayList.put("ROW", new Value(_rowAr, "array"));
		_arrayList.put("VAL", new Value(_valAr, "array"));
		_crossTabMap.put("arrayList", new Value(_arrayList, "map") );
		
		String _rowNumTitle = "";
		
		if( _infoObj.containsKey("rowNumTitle") && _infoObj.get("rowNumTitle").getStringValue().equals("")==false)
		{
			_rowNumTitle = _infoObj.get("rowNumTitle").getStringValue();
		}
		
		// CrossTab의 각 타입별 스타일 담기
		HashMap<String, Object> styleMap = makeCrossTabItemStyle( _pageXml, _colAr.size(), _rowAr.size(), _valAr.size(), _leftFixedColumnAr, _rightFixedColumnAr, _headerLabelVisible, _infoObj.get("rowNum").getBooleanValue() , _itemId, _rowNumTitle );
		
		_crossTabMap.put("itemStyle", new Value(styleMap, "object") );
		
		_crossTabMap = convertCrossTabData(_crossTabMap, _colAr, _rowAr, _valAr );
		
		if(_crossTabMap == null ) return null;
		
		ArrayList<ArrayList<HashMap<String, Value>>> allPageDatas  = continueBandCrossTabPage(_crossTabMap, pageWidth, startPosition, maxPosition, currentStartPosition, mXAr);
		return allPageDatas;
	}
	
	
	
	private void continueBandDatachangeToCrossTab( HashMap<String, Value> crossTabItem )
	{
		
		int lastNum = 0;
		int i = 0;
		int j = 0;
		int k = 0;
		
		// DefaultYposition 처리
		
		
		
		// column병합 처리 
		
		
		// _startPage를 CurrentPage로 변경
		
		// DATA를 이용하여 모든 페이지의 아이템을 생성하여 리턴
		ArrayList<String> headerMakeList = new ArrayList<String>();
		
		if( crossTabItem.containsKey("info") && crossTabItem.get("info").getMapValue().containsKey("headerLabelPosition") && 
				crossTabItem.get("info").getMapValue().get("headerLabelPosition").equals("top") )
		{
			headerMakeList.add("headerLabel");
			headerMakeList.add("header");
		}
		else
		{
			headerMakeList.add("header");
			headerMakeList.add("headerLabel");
		}
		
		// 총 데이터의 Row수 
		lastNum = ((ArrayList<ArrayList<String>>) crossTabItem.get("pageInfo").getMapValue().get("DATA")).size();
		
		for ( i = 0; i < headerMakeList.size(); i++) {
			
			// 헤더 정보 처리
			if( headerMakeList.get(i).equals("header"))
			{
				
			}
			else
			{
				
			}
			
			
		}
		
		
		
		
	}
	
	
	//
	private HashMap<String, Object> makeCrossTabItemStyle( Element _xml, Integer _colLength, Integer _rowLength, Integer _valueLength, ArrayList<String> _lAr, ArrayList<String> _rAr, Boolean _headerLabelFlag, Boolean _rowNumFlag, String _tblID, String _rowNumTitle ) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException{
		
		
		HashMap<String, Object> retCrossTabMap = new HashMap<String, Object>();
		HashMap<String, Object> retMap;
		// 각각의 xml별로 처리 ( header, headerLabel, data, surmmary )
		
		NodeList crossNodes;
		NodeList nodes;
		Element _crosstabDataNode;
		Element _subElement;
		
		String _noTitleText = _rowNumTitle;
		
		crossNodes = _xml.getElementsByTagName("crossTab");
		_crosstabDataNode 	= (Element) crossNodes.item(0);
		
		//Header정보 담기
		nodes = _crosstabDataNode.getElementsByTagName("hearder");
		_subElement 	= (Element) nodes.item(0);
		
//		makeBandStyleToObject(_xml, _colLength, _rowLength, false, false, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag );
		retMap = makeBandStyleToObject(_subElement, _colLength, _rowLength, false, false, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, _noTitleText ); 
		
		retCrossTabMap.put("headerColumnStyle", 	retMap.get("COL") );
		retCrossTabMap.put("headerValueStyle", 		retMap.get("VALUE") );
		retCrossTabMap.put("headerSumStyle", 		retMap.get("SUM") );
		retCrossTabMap.put("headerGrdTotStyle", 	retMap.get("GRD") );
		retCrossTabMap.put("headerLfcStyle", 		retMap.get("LFC") );
		retCrossTabMap.put("headerRfcStyle", 		retMap.get("RFC") );
		
		retCrossTabMap.put("headerInfoStyle", 		retMap.get("BAND") );
		
		// 헤더 라벨의 속성 담기
		if( _headerLabelFlag )
		{
			nodes = _crosstabDataNode.getElementsByTagName("headerLabel");
		}
		if( _headerLabelFlag && nodes.getLength() > 0 ){
			_subElement 	= (Element) nodes.item(0);
			retMap = makeBandStyleToObject(_subElement, _colLength, _rowLength, false, false, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, _noTitleText );
			
			retCrossTabMap.put("headerLabelColumnStyle", 	retMap.get("COL") );
			retCrossTabMap.put("headerLabelValueStyle", 	retMap.get("VALUE") );
			retCrossTabMap.put("headerLabelSumStyle", 		retMap.get("SUM") );
			retCrossTabMap.put("headerLabelGrdTotStyle", 	retMap.get("GRD") );
			retCrossTabMap.put("headerLabelLfcStyle", 		retMap.get("LFC") );
			retCrossTabMap.put("headerLabelRfcStyle", 		retMap.get("RFC") );
		}else{
			
			retCrossTabMap.put("headerLabelColumnStyle", 	new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelValueStyle", 		new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelSumStyle", 		new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelGrdTotStyle", 	new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelLfcStyle", 		new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelRfcStyle", 		new ArrayList<Object>() );
			
		}
		
		
		nodes = _crosstabDataNode.getElementsByTagName("data");
		_subElement 	= (Element) nodes.item(0);
		retMap = makeBandStyleToObject(_subElement, _colLength, _rowLength, false, false, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, "" );
		
		retCrossTabMap.put("valueColumnStyle", 	retMap.get("COL") );
		retCrossTabMap.put("valueValueStyle", 	retMap.get("VALUE") );
		retCrossTabMap.put("valueSumStyle", 	retMap.get("SUM") );
		retCrossTabMap.put("valueGrdTotStyle", 	retMap.get("GRD") );
		retCrossTabMap.put("valueLfcStyle", 	retMap.get("LFC") );
		retCrossTabMap.put("valueRfcStyle", 	retMap.get("RFC") );
		
		nodes = _crosstabDataNode.getElementsByTagName("surmmary");
		_subElement 	= (Element) nodes.item(0);
		
		retMap = makeBandStyleToObject(_subElement, _colLength, _rowLength, true, true, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, "" );
		
		retCrossTabMap.put("summeryColumnStyle", 	retMap.get("COL") );
		retCrossTabMap.put("summeryValueStyle", 	retMap.get("VALUE") );
		retCrossTabMap.put("summerySumStyle", 		retMap.get("SUM") );
		retCrossTabMap.put("summeryGrdTotStyle", 	retMap.get("GRD") );
		retCrossTabMap.put("summeryLfcStyle", 		retMap.get("LFC") );
		retCrossTabMap.put("summeryRfcStyle", 		retMap.get("RFC") );
		
		
		//grd tag처리 
		NodeList bands = _subElement.getElementsByTagName("band");
		String nodeStr = "<grandSummery>" + nodeToString( bands.item(bands.getLength()-1) ) + "</grandSummery>";
		// 신규로 생성된 xml문자를 이용하여 Node Element를 생성 
		Element node =  DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(nodeStr.getBytes()))
			    .getDocumentElement();
		
		retMap = makeBandStyleToObject(node, _colLength, _rowLength, false, false, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, "" );
		
		retCrossTabMap.put("grdTotColumnStyle", 	retMap.get("COL") );
		retCrossTabMap.put("grdTotValueStyle", 		retMap.get("VALUE") );
		retCrossTabMap.put("grdTotSumStyle", 		retMap.get("SUM") );
		retCrossTabMap.put("grdTotGrdTotStyle", 	retMap.get("GRD") );
		retCrossTabMap.put("grdTotLfcStyle", 		retMap.get("LFC") );
		retCrossTabMap.put("grdTotRfcStyle", 		retMap.get("RFC") );
		
		return retCrossTabMap;
	}
	
	private static String nodeToString(Node node) {
	    StringWriter sw = new StringWriter();
	    try {
	      Transformer t = TransformerFactory.newInstance().newTransformer();
	      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	      t.setOutputProperty(OutputKeys.INDENT, "yes");
	      t.transform(new DOMSource(node), new StreamResult(sw));
	    } catch (TransformerException te) {
	      System.out.println("nodeToString Transformer Exception");
	    }
	    return sw.toString();
	 }
	
	private HashMap<String, Object> makeBandStyleToObject( Element _xml, Integer _colLength, Integer _rowLength, Boolean _lastsFlag, Boolean _reverse, Integer _valueLength, ArrayList<String> _lAr, ArrayList<String> _rAr, Boolean _headerLabelFlag, Boolean _rowNumFlag, String _tblID, String _noTitle ) throws XPathExpressionException{
		
		ArrayList<String> _lftAr = new ArrayList<String>();
		ArrayList<String> _rftAr = new ArrayList<String>();
		
		HashMap<String, Object> _retMap = new HashMap<String, Object>();
		
		Integer _lastNum = 0;
		Integer i = 0;
		Integer j = 0;
		Integer k = 0;
		Integer n = 0;
		
		NodeList _bands = _xml.getElementsByTagName("band");
		
		if( _rowNumFlag ) _rowLength = _rowLength -1;
		
		if( _lAr != null ) _lftAr = _lAr;
		if( _rAr != null ) _rftAr = _rAr;
		
		if( _lastsFlag )
		{
			_lastNum = _bands.getLength()-1;
		}
		else
		{
			_lastNum = _bands.getLength();
		}
		
		ArrayList<ArrayList<HashMap<String, Value>>> _columnStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _valueStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _summeryStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _grandTotStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _leftFixedStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _rightFixedStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		
		
		//
		ArrayList<HashMap<String, Value> > _bandAr = new ArrayList<HashMap<String,Value>>();
		ArrayList<HashMap<String, Value>> _colAr;
		ArrayList<HashMap<String, Value>> _totAr;
		ArrayList<HashMap<String, Value>> _grdToAr;
		ArrayList<HashMap<String, Value>> _valueAr;
		ArrayList<HashMap<String, Value>> _leftFAr;
		ArrayList<HashMap<String, Value>> _rightFAr;
		
		HashMap<String, Value> _property;
		
		Element BandItem = (Element) _bands.item(i);
		Element BandPropertyXml;
		Element _bandItem;			//아이템별 Property
		NodeList _bandPropertys;
		
		NodeList _propertys = null;
		Element _propertyElement = null;
		int propertyLength = 0;
		
		// Reverse값이 false 일경우 처리
		if( _reverse == false)
		{
			for (i = 0; i < _lastNum; i++) {
				
				if(_bands.getLength() > i )
				{
					BandItem = (Element) _bands.item(i);
				}
				else
				{
					BandItem = (Element) _bands.item(0);
				}
				
				_colAr = new ArrayList<HashMap<String, Value>>();
				_totAr = new ArrayList<HashMap<String, Value>>();
				_grdToAr = new ArrayList<HashMap<String, Value>>();
				_valueAr = new ArrayList<HashMap<String, Value>>();
				_leftFAr = new ArrayList<HashMap<String, Value>>();
				_rightFAr = new ArrayList<HashMap<String, Value>>();
				
				_property = new HashMap<String, Value>();
				
				NodeList bandProperty = BandItem.getElementsByTagName("property");
				HashMap<String, Value> bandPropertyMap = new HashMap<String, Value>();
				for ( j = 0; j < bandProperty.getLength(); j++) {
					BandPropertyXml = (Element) bandProperty.item(j);
					bandPropertyMap.put( BandPropertyXml.getAttribute("name") , new Value(BandPropertyXml.getAttribute("value"), BandPropertyXml.getAttribute("type")));
					
				}
				
				_bandAr.add(bandPropertyMap);
				
				NodeList _bandItems = BandItem.getElementsByTagName("item");
				
				for ( k = 0; k < _bandItems.getLength(); k++) {
					
					bandPropertyMap = new HashMap<String, Value>();
					_bandItem = (Element) _bandItems.item(k);
					_bandPropertys = _bandItem.getElementsByTagName("property");
					
					for ( j = 0; j < _bandPropertys.getLength(); j++) {
						BandPropertyXml = (Element) _bandPropertys.item(j);
						try {
							bandPropertyMap.put( BandPropertyXml.getAttribute("name") , new Value( URLDecoder.decode( BandPropertyXml.getAttribute("value"), "UTF-8" ), BandPropertyXml.getAttribute("type")));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					bandPropertyMap.put("className", Value.fromString("UBLabelBorder") );
					bandPropertyMap.put("Element", new Value( _bandItem, "element") );
					
					if( mIsExportTable )
					{
						bandPropertyMap.put("isTable", new Value( "true", "string") );
						bandPropertyMap.put("TABLE_ID", new Value( _tblID, "string") );
					}
					
					if( k < _rowLength  ) _colAr.add(bandPropertyMap);
					else if( k < ( _rowLength + _lftAr.size() ) ) _leftFAr.add(bandPropertyMap);
					else if( k < ( _rowLength + _lftAr.size() + _valueLength ) ) _valueAr.add(bandPropertyMap);
					else if( k < (_bandItems.getLength() -1) && k >= ( (_bandItems.getLength()-1)-_rftAr.size() ) ) _rightFAr.add(bandPropertyMap);
					else if( k == (_bandItems.getLength() -1) ) _grdToAr.add(bandPropertyMap);
					else _totAr.add(bandPropertyMap);
					
				}
				
				if( _rowNumFlag ){
					Integer l = 0;
					NodeList _propertyList;
					Node ndlist; 
					
					// Node정보 업데이트 
					
					if( _totAr != null && _totAr.size() > 0 )
					{
						_bandItem = (Element) _totAr.get(0).get("Element").getElementValue().cloneNode(true);
						_totAr.add(0, (HashMap<String, Value>) _totAr.get(0).clone() );
						_totAr.get(0).put("Element", new Value( _bandItem,"element") );
						_totAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						
//						XPath _xpath = XPathFactory.newInstance().newXPath();
//						ndlist = (Node) _xpath.evaluate("./property[@name='width']", _bandItem, XPathConstants.NODE);
//						ndlist.getAttributes().getNamedItem("value").setNodeValue("40");
						_propertys = _bandItem.getElementsByTagName("property");
						propertyLength = _propertys.getLength();
						for ( j = 0; j < propertyLength; j++) {
							
							_propertyElement = (Element) _propertys.item(j);
							
							if(_propertyElement.getAttribute("name").equals("width"))
							{
								_propertyElement.setAttribute("value", Float.valueOf( mRowNumWidth ).toString());
								break;
							}
							
						}
						
					}
					
					if( _grdToAr != null && _grdToAr.size() > 0 )
					{
						
						_bandItem = (Element) _grdToAr.get(0).get("Element").getElementValue().cloneNode(true);
						_grdToAr.add(0, (HashMap<String, Value>) _grdToAr.get(0).clone() );
						_grdToAr.get(0).put("Element", new Value( _bandItem,"element") );
						_grdToAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						
//						_grdToAr.add(0, (Element)_grdToAr.get(0).cloneNode(true) );
						
//						XPath _xpath = XPathFactory.newInstance().newXPath();
//						ndlist = (Node) _xpath.evaluate("./property[@name='width']", _bandItem, XPathConstants.NODE);
//						
//						ndlist.getAttributes().getNamedItem("value").setNodeValue("40");
						
						_propertys = _bandItem.getElementsByTagName("property");
						propertyLength = _propertys.getLength();
						for ( j = 0; j < propertyLength; j++) {
							
							_propertyElement = (Element) _propertys.item(j);
							
							if(_propertyElement.getAttribute("name").equals("width"))
							{
								_propertyElement.setAttribute("value", Float.valueOf( mRowNumWidth ).toString());
								break;
							}
							
						}
						
					}
					
					if( _colAr != null && _colAr.size() > 0 )
					{
						_bandItem = (Element) _colAr.get(0).get("Element").getElementValue().cloneNode(true);
						_colAr.add(0, (HashMap<String, Value>) _colAr.get(0).clone() );
						_colAr.get(0).put("Element", new Value( _bandItem,"element") );
						_colAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						if( _noTitle.equals("") == false ) _colAr.get(0).put("text",new Value(_noTitle,"string") );
//						_colAr.get(0).put("text",new Value("NO","string") );
						_colAr.get(0).put("textAlign",new Value("center","string") );
						_colAr.get(0).put("verticalAlign",new Value("middle","string") );
						
//						_colAr.add(0, (Element)_colAr.get(0).cloneNode(true) );
//						XPath _xpath = XPathFactory.newInstance().newXPath();
						//	category[@name='Sport' and ./author/text()='James Small']
//						NodeList ndlists = (NodeList) _xpath.evaluate("./property[@name='width'] | ./property[@name='textAlign'] | ./property[ @name='text'] |  ./property[@name='verticalAlign' ]", _bandItem, XPathConstants.NODESET);
						NodeList ndlists = _bandItem.getElementsByTagName("property");
						String attName = "";
						for ( n = 0; n < ndlists.getLength(); n++) {
							
							ndlist = (Node)ndlists.item(n);
							attName = ndlist.getAttributes().getNamedItem("name").getNodeValue();
							
							if( attName.equals("width") ){
								ndlist.getAttributes().getNamedItem("value").setNodeValue( Float.valueOf( mRowNumWidth ).toString() );
							}else if( attName.equals("text") ){
								if( _noTitle.equals("") == false ) ndlist.getAttributes().getNamedItem("value").setNodeValue(_noTitle);
//								ndlist.getAttributes().getNamedItem("value").setNodeValue("NO");
							}else if( attName.equals("textAlign") ){
								ndlist.getAttributes().getNamedItem("value").setNodeValue("center");
							}else if( attName.equals("verticalAlign") ){
								ndlist.getAttributes().getNamedItem("value").setNodeValue("middle");
							}
							
						}
						
					}
					
				}
				
				// 생성된 Array를 스타일 배열에 추가
				_columnStyle.add(_colAr);
				_valueStyle.add(_valueAr);
				_summeryStyle.add(_totAr);
				_grandTotStyle.add(_grdToAr);
				_leftFixedStyle.add(_leftFAr);
				_rightFixedStyle.add(_rightFAr);
				
			}
			
			
		}
		else
		{
//			i = _lastNum -1;
			for ( i = _lastNum -1; i >= 0; i--) {
				
				_colAr = new ArrayList<HashMap<String, Value>>();
				_totAr = new ArrayList<HashMap<String, Value>>();
				_grdToAr = new ArrayList<HashMap<String, Value>>();
				_valueAr = new ArrayList<HashMap<String, Value>>();
				_leftFAr = new ArrayList<HashMap<String, Value>>();
				_rightFAr = new ArrayList<HashMap<String, Value>>();
				
				_property = new HashMap<String, Value>();
				
				BandItem = (Element) _bands.item(i);
				NodeList bandProperty = BandItem.getElementsByTagName("property");
				HashMap<String, Value> bandPropertyMap = new HashMap<String, Value>();
				for ( j = 0; j < bandProperty.getLength(); j++) {
					BandPropertyXml = (Element) bandProperty.item(j);
					bandPropertyMap.put( BandPropertyXml.getAttribute("name") , new Value(BandPropertyXml.getAttribute("value"), BandPropertyXml.getAttribute("type")));
					
				}
				_bandAr.add(bandPropertyMap);
				
				NodeList _bandItems = BandItem.getElementsByTagName("item");
				
				for ( k = 0; k < _bandItems.getLength(); k++) {
					
					bandPropertyMap = new HashMap<String, Value>();
					_bandItem = (Element) _bandItems.item(k);
					_bandPropertys = _bandItem.getElementsByTagName("property");
					
					for ( j = 0; j < _bandPropertys.getLength(); j++) {
						BandPropertyXml = (Element) _bandPropertys.item(j);
						try {
							bandPropertyMap.put( BandPropertyXml.getAttribute("name") , new Value(  URLDecoder.decode(BandPropertyXml.getAttribute("value"),"UTF-8"), BandPropertyXml.getAttribute("type")));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					bandPropertyMap.put("Element", new Value( _bandItem, "element") );
					bandPropertyMap.put("className", Value.fromString("UBLabelBorder") );
					
					if( mIsExportTable )
					{
						bandPropertyMap.put("isTable", new Value( "true", "string") );
						bandPropertyMap.put("TABLE_ID", new Value( _tblID, "string") );
					}
					
					if( k < _rowLength  ) _colAr.add(bandPropertyMap);
					else if( k < ( _rowLength + _lftAr.size() ) ) _leftFAr.add(bandPropertyMap);
					else if( k < ( _rowLength + _lftAr.size() + _valueLength ) ) _valueAr.add(bandPropertyMap);
					else if( k < (_bandItems.getLength() -1) && k >= ( (_bandItems.getLength()-1)-_rftAr.size() ) ) _rightFAr.add(bandPropertyMap);
					else if( k == (_bandItems.getLength() -1) ) _grdToAr.add(bandPropertyMap);
					else _totAr.add(bandPropertyMap);
					
				}
				
				if( _rowNumFlag ){
					Integer l = 0;
					NodeList _propertyList;
					Node ndlist; 
					
					// Node정보 업데이트 
					
					if( _totAr != null && _totAr.size() > 0 )
					{
//						_totAr.add(0, (Element)_totAr.get(0).cloneNode(true) );
						
						_bandItem = (Element) _totAr.get(0).get("Element").getElementValue().cloneNode(true);
						_totAr.add(0, (HashMap<String, Value>) _totAr.get(0).clone() );
						_totAr.get(0).put("Element", new Value( _bandItem,"element") );
						_totAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						
//						XPath _xpath = XPathFactory.newInstance().newXPath();
//						ndlist = (Node) _xpath.evaluate("./property[@name='width']", _bandItem, XPathConstants.NODE);
//						
//						ndlist.getAttributes().getNamedItem("value").setNodeValue("40");
						
						_propertys = _bandItem.getElementsByTagName("property");
						propertyLength = _propertys.getLength();
						for ( j = 0; j < propertyLength; j++) {
							
							_propertyElement = (Element) _propertys.item(j);
							
							if(_propertyElement.getAttribute("name").equals("width"))
							{
								_propertyElement.setAttribute("value", Float.valueOf( mRowNumWidth ).toString());
								break;
							}
							
						}
						
					}
					
					if( _grdToAr != null && _grdToAr.size() > 0 )
					{
//						_grdToAr.add(0, (Element)_grdToAr.get(0).cloneNode(true) );
					
						_bandItem = (Element) _grdToAr.get(0).get("Element").getElementValue().cloneNode(true);
						_grdToAr.add(0, (HashMap<String, Value>) _grdToAr.get(0).clone() );
						_grdToAr.get(0).put("Element", new Value( _bandItem,"element") );
						_grdToAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						
//						XPath _xpath = XPathFactory.newInstance().newXPath();
//						ndlist = (Node) _xpath.evaluate("./property[@name='width']", _bandItem, XPathConstants.NODE);
//						
//						ndlist.getAttributes().getNamedItem("value").setNodeValue("40");
						_propertys = _bandItem.getElementsByTagName("property");
						propertyLength = _propertys.getLength();
						for ( j = 0; j < propertyLength; j++) {
							
							_propertyElement = (Element) _propertys.item(j);
							
							if(_propertyElement.getAttribute("name").equals("width"))
							{
								_propertyElement.setAttribute("value", Float.valueOf( mRowNumWidth ).toString());
								break;
							}
							
						}
					}
					
					if( _colAr != null && _colAr.size() > 0 )
					{
//						_colAr.add(0, (Element)_colAr.get(0).cloneNode(true) );

						_bandItem = (Element) _colAr.get(0).get("Element").getElementValue().cloneNode(true);
						_colAr.add(0, (HashMap<String, Value>) _colAr.get(0).clone() );
						_colAr.get(0).put("Element", new Value( _bandItem,"element") );
						_colAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						if( _noTitle.equals("") == false )_colAr.get(0).put("text",new Value(_noTitle,"string") );
						_colAr.get(0).put("textAlign",new Value("center","string") );
						_colAr.get(0).put("verticalAlign",new Value("middle","string") );
						
//						XPath _xpath = XPathFactory.newInstance().newXPath();
//						NodeList ndlists = (NodeList) _xpath.evaluate("./property[@name='width'] | ./property[@name='textAlign'] | ./property[ @name='text'] |  ./property[@name='verticalAlign' ]", _bandItem, XPathConstants.NODESET);
						NodeList ndlists = _bandItem.getElementsByTagName("property");
						String attName = "";
						for ( n = 0; n < ndlists.getLength(); n++) {
							
							ndlist = (Node)ndlists.item(n);
							attName = ndlist.getAttributes().getNamedItem("name").getNodeValue();
							
							if( attName.equals("width") ){
								ndlist.getAttributes().getNamedItem("value").setNodeValue(Float.valueOf( mRowNumWidth ).toString());
							}else if( attName.equals("text") ){
								if( _noTitle.equals("") == false )ndlist.getAttributes().getNamedItem("value").setNodeValue(_noTitle);
							}else if( attName.equals("textAlign") ){
								ndlist.getAttributes().getNamedItem("value").setNodeValue("center");
							}else if( attName.equals("verticalAlign") ){
								ndlist.getAttributes().getNamedItem("value").setNodeValue("middle");
							}
							
						}
						
					}
					
				}
				
				// 생성된 Array를 스타일 배열에 추가
				_columnStyle.add(_colAr);
				_valueStyle.add(_valueAr);
				_summeryStyle.add(_totAr);
				_grandTotStyle.add(_grdToAr);
				_leftFixedStyle.add(_leftFAr);
				_rightFixedStyle.add(_rightFAr);
				
			}
				
		}
		
		_retMap.put("COL", _columnStyle);
		_retMap.put("VALUE", _valueStyle);
		_retMap.put("SUM", _summeryStyle);
		_retMap.put("GRD", _grandTotStyle);
		_retMap.put("LFC", _leftFixedStyle);
		_retMap.put("RFC", _rightFixedStyle);
		_retMap.put("BAND", _bandAr);
		
		return _retMap;
	}
	
	
	/**
	 * 크로스탭 화면에 맞춰 데이터 가공처리
	 * @param crossTabMap
	 * @param colAr
	 * @param rowAr
	 * @param valAr
	 */
	private HashMap<String, Value> convertCrossTabData( HashMap<String, Value> crossTabMap, ArrayList<String> colAr,ArrayList<String> rowAr,ArrayList<String> valAr )
	{
	
		if( !crossTabMap.containsKey("pageInfo") ){
			crossTabMap.put("pageInfo", new Value("") );
		}
		
		ArrayList<String> valueAr = new ArrayList<String>();
		HashMap<String, Value> infoData = crossTabMap.get("info").getMapValue();
		String dataSetName = infoData.get("dataSet").getStringValue();
		String _valueName = "";
		String _valueFn   = "";
		
		ArrayList<String> sortColumnAr = new ArrayList<String>();
		ArrayList<String> orderByColumnAr = new ArrayList<String>();
		ArrayList<String> numericColumnAr = new ArrayList<String>();
		
		ArrayList<String> sortColumnAr2 = new ArrayList<String>();
		ArrayList<String> orderByColumnAr2 = new ArrayList<String>();
		ArrayList<String> numericColumnAr2 = new ArrayList<String>();
		
		
		ArrayList<String> _leftFixedColumnAr 	= new ArrayList<String>();
		ArrayList<String> _RightFixedColumnAr 	= new ArrayList<String>();
		
		
		
		Integer j=0;
		// 데이터 셋에 CrossTab에서 사용되는 데이터셋이 존재할경우 존재하지 않을경루 크로스탭을 처리하지 않는다.
		if( DataSet.containsKey(dataSetName) ){
			
			try {
				for (int i = 0; i < valAr.size(); i++) {
					
					_valueName = URLDecoder.decode(valAr.get(i), "UTF-8");
					_valueName = _valueName.replaceAll(" ", "");
					
					if( _valueName.indexOf("(") != -1){
						if( i == 0 ) _valueFn = _valueName.substring(0, _valueName.indexOf("(")).toUpperCase();
						_valueName = _valueName.substring( _valueName.indexOf("(") + 1 , _valueName.indexOf(")") );
					}
					
					valueAr.add(_valueName);
				}
				
				ArrayList<Object> _summaryColumn = new ArrayList<Object>();
				
				if( infoData.containsKey("summaryColumn") && infoData.get("summaryColumn")!=null && infoData.get("summaryColumn").getArrayCollection() != null && infoData.get("summaryColumn").getArrayCollection().size() > 0 )
				{
					_summaryColumn = (ArrayList<Object>) infoData.get("summaryColumn").getArrayCollection();
				}
				
				if( infoData.containsKey("sort") && infoData.get("sort").getArrayCollection() != null && infoData.get("sort").getArrayCollection().size() > 0 )
				{
					
					for ( j = 0; j < infoData.get("sort").getArrayCollection().size(); j++) {
						
						HashMap<String, String> _sortData = (HashMap<String, String>) infoData.get("sort").getArrayCollection().get(j);
						
						sortColumnAr.add( _sortData.get("column") );
						orderByColumnAr.add( _sortData.get("orderBy") );
						if( _sortData.containsKey("numeric") ) numericColumnAr.add( _sortData.get("numeric") );
						else  numericColumnAr.add( "false" );
						
					}
					
				}
				else
				{
					sortColumnAr = null;
					orderByColumnAr = null;
					numericColumnAr = null;
				}
				
				if( infoData.containsKey("columnSort") && infoData.get("columnSort").getArrayCollection() != null && infoData.get("columnSort").getArrayCollection().size() > 0 )
				{
					for ( j = 0; j < infoData.get("columnSort").getArrayCollection().size(); j++) {
						
						HashMap<String, String> _sortData = (HashMap<String, String>) infoData.get("columnSort").getArrayCollection().get(j);
						
						sortColumnAr2.add( _sortData.get("column") );
						orderByColumnAr2.add( _sortData.get("orderBy") );
						if( _sortData.containsKey("numeric") ) numericColumnAr2.add( _sortData.get("numeric") );
						else  numericColumnAr2.add( "false" );
						
					}
				}
				else
				{
					sortColumnAr2 = null;
					orderByColumnAr2 = null;
					numericColumnAr2 = null;
				}
				
				
				if( infoData.containsKey("leftFixedColumn") && infoData.get("leftFixedColumn") != null )
				{
					_leftFixedColumnAr = infoData.get("leftFixedColumn").getArrayStringValue();
				}
				
				if( infoData.containsKey("rightFixedColumn") && infoData.get("rightFixedColumn") != null )
				{
					_RightFixedColumnAr = infoData.get("rightFixedColumn").getArrayStringValue();
				}
				
				
				// SummaryColumn 정보를 같이 넘겨서 데이터를 가공하고 결과를  넘겨받음
				
				HashMap<String, Object> retData = null; 
				
				if( DataSet.get(dataSetName).size() > 0  )
				{
					UBIPivotParser ubpivot = new UBIPivotParser();
					retData = ubpivot.convert( DataSet.get(dataSetName), colAr, rowAr, valueAr, _valueFn, false, sortColumnAr, orderByColumnAr, _leftFixedColumnAr, _RightFixedColumnAr, _summaryColumn , sortColumnAr2, orderByColumnAr2, numericColumnAr, numericColumnAr2);
					HashMap<String, ArrayList<ArrayList<String>>> summaryData = summaryVisibleToCrossTabData(retData, crossTabMap.get("summaryVisible").getBooleanValue(), crossTabMap.get("grandSummaryVisible").getBooleanValue(), crossTabMap.get("grandSummaryType").getStringValue(), crossTabMap.get("summaryType").getStringValue());
					retData.put("HEADER", summaryData.get("HEADER"));
					retData.put("DATA", summaryData.get("DATA"));
				}
				else
				{
					return null;
				}
				
				
				if( !retData.containsKey("RESULT") || !retData.get("RESULT").equals("SUCCESS") ){
					//ERROR 메시지 처리
					
					
				}
				crossTabMap.put("pageInfo", new Value( retData , "object" ));
				
				//  crossTab의 itemcount값 가져오기
				crossTabMap.put("itemCount", new Value( checkCrossDataCount(crossTabMap), "object" ));
				//  crosstab의 headersumcount값 업데이트
//				int headerCnt = Integer.valueOf( crossTabMap.get("headerSumCount").getStringValue()) /  (Integer) crossTabMap.get("itemCount").getMapValue().get("HEADER_COUNT");
//				crossTabMap.put("headerSumCount", new Value(headerCnt, "number"));
				crossTabMap.put("columnLists", new Value( makeColumnInfoArr2( (ArrayList<ArrayList<String>>) retData.get("HEADER")), "arraystr" ) );
				crossTabMap.put("RowList", new Value( makeRowListInfoToArray( (ArrayList<ArrayList<String>>) retData.get("DATA")) , "arraystr" ) );
				crossTabMap.put("valueColumnIndexAr", new Value( makeValueColumnIndexAr(crossTabMap.get("columnLists").getArrayStringValue() , valueAr.size() ), "arrayint" ));
				
				// 데이터 컨버팅 완료  //
				
				
			} catch (Exception e) {
				// TODO: handle exception
				System.out.print(e.getMessage());
			}
			
			
		}
		
		return crossTabMap;
	}
	
	
	/**
	 * functionName	:	summaryVisibleToCrossTabData</br>
	 * desc			:	크로스탭 데이터를 summaryVisible에 맞춰서 가공
	 * @param obj
	 * @param sumVisible
	 * @param grdVisible
	 * @param grdType
	 * @param summaryType
	 */
	private HashMap<String, ArrayList<ArrayList<String>>> summaryVisibleToCrossTabData( HashMap<String, Object> obj, Boolean sumVisible, Boolean grdVisible, String grdType, String summaryType )
	{
		
		int i = 0;
		int j = 0;
		ArrayList<String> arr = new ArrayList<String>();
		ArrayList<ArrayList<String>> retHeader = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> retValueData = new ArrayList<ArrayList<String>>();
		HashMap<String, ArrayList<String>> headerData = (HashMap<String, ArrayList<String>>) obj.get("HEADER");
		ArrayList<ArrayList<String>> valueData = (ArrayList<ArrayList<String>>) obj.get("DATA");
		HashMap<String, ArrayList<ArrayList<String>>> returnHashMap = new HashMap<String, ArrayList<ArrayList<String>>>();
		
		ArrayList<String> columnList = makeColumnInfoArr(headerData);
		
		String str = "";
		
		// 헤더 데이터 가공 생성 
		for ( i = 0; i < headerData.size(); i++) {
			arr = new ArrayList<String>();
			for ( j = 0; j < headerData.get(String.valueOf(i)).size(); j++) {
				if( (!columnList.get(j).equals("sum") || (sumVisible  && !summaryType.equals(GRANDSUMMARY_TYPE_HORIZONTAL)) ) &&
					( !columnList.get(j).equals("grand") || verticalSummaryCheck( grdVisible, grdType, headerData.get(String.valueOf(i)).get(j)) )  )
				{
					arr.add(  headerData.get(String.valueOf(i)).get(j)   );
				}
				
			}
			retHeader.add(arr);
		}
		
		// 데이터 가공 생성
		for ( i = 0; i < valueData.size(); i++) {
			
			if(valueData.get(i).size() > 0 )
			{	
				arr = new ArrayList<String>();
				str = valueData.get(i).get(0);
				if( (str.toLowerCase().indexOf(mSummeryValueText) == -1 || ( sumVisible && !summaryType.equals(GRANDSUMMARY_TYPE_VERTICAL) ) )
						&& ( str.toLowerCase().indexOf(mGrandValueText) == -1 || (grdVisible && !grdType.equals(GRANDSUMMARY_TYPE_VERTICAL) ) ))
				{
					for ( j = 0; j < valueData.get(i).size(); j++) {
						if( ( !columnList.get(j).equals("sum") || (sumVisible && !summaryType.equals(GRANDSUMMARY_TYPE_HORIZONTAL) ) ) 
								&& (!columnList.get(j).equals("grand") || verticalSummaryCheck(grdVisible, grdType, headerData.get("0").get(j)  )  ))
						{
							arr.add( valueData.get(i).get(j) );
						}
					}
					retValueData.add(arr);
				}
			}
			
		}
		
		returnHashMap.put("HEADER", retHeader);
		returnHashMap.put("DATA", retValueData);
		
		return returnHashMap;
	}
	
	private boolean verticalSummaryCheck(Boolean grdFlag, String grdType, String chkTxt )
	{
		
		if( ( grdFlag && !grdType.equals(GRANDSUMMARY_TYPE_HORIZONTAL) ) || UBIPivotParser.substringUBF(chkTxt, chkTxt.length()-1, chkTxt.length()).equals("&")  )
		{
			return true;
		}
		
		return false;
	}
	
	private HashMap<String, Integer> checkCrossDataCount( HashMap<String, Value> crossObj )
	{
		
		HashMap<String, Integer> retHashMap = new HashMap<String, Integer>();
		
		
		
		return retHashMap;
	}
	
	
	
	
	private ArrayList<String> makeColumnInfoArr( HashMap<String, ArrayList<String>> arr )
	{
		ArrayList<String> retData = new ArrayList<String>();
		
		ArrayList<String> list = arr.get( String.valueOf( arr.size()-1 ) );
		int i = 0;
		int j = 0;
		int _size = arr.size()-1;
		int rowIndex = 0;
		String column = "";
		
//		for ( i = 0; i < list.size(); i++) {
//			column = list.get(i);
//			retData.add( convertColumnText(column) );
//		}
//		
//		for ( i = 0; i < retData.size(); i++) {
//			
//			rowIndex = _size;
//			
//			if(retData.get(i).equals("empty"))
//			{
//				
//				while( retData.get(i).equals("empty") || rowIndex >= arr.size()  )
//				{
//					rowIndex--;
//					retData.set(i, convertColumnText( arr.get( String.valueOf(rowIndex) ).get(i) ));
//				}
//			}
//		}
		
		int _colSize = 0;
		int rowSize = arr.size();
		for( i = 0; i < rowSize; i++  )
		{
			_colSize = arr.get(String.valueOf(i) ).size();
			list = arr.get(String.valueOf(i));
			for( j =0; j < _colSize; j++ )
			{
				column = list.get(j);
				column = convertColumnText(column);
				
				if( i == 0 )
				{
					retData.add( column );
				}
				else if( retData.get(j).equals("empty") ||  (column.equals("value") == false && column.equals("empty") == false) )
				{
					retData.set(j, column);
				}
				
			}
			
		}
		
		return retData;
	}
	
	private ArrayList<String> makeColumnInfoArr2( ArrayList<ArrayList<String>> arr )
	{
		ArrayList<String> retData = new ArrayList<String>();
		
		ArrayList<String> list = arr.get(0);
		int i = 0;
		int j = 0;
		int rowIndex = 0;
		String column = "";
		
//		for ( i = 0; i < list.size(); i++) {
//			column = list.get(i);
//			retData.add( convertColumnText(column) );
//		}
//		
//		for ( i = 0; i < retData.size(); i++) {
//			
//			rowIndex = 0;
//			
//			if(retData.get(i).equals("empty"))
//			{
//				
//				while( retData.get(i).equals("empty") || rowIndex >= arr.size()  )
//				{
//					rowIndex++;
//					retData.set(i, convertColumnText( arr.get(rowIndex).get(i) ));
//				}
//			}
//		}
		
		int _colSize = 0;
		int rowSize = arr.size();
		for( i = 0; i < rowSize; i++  )
		{
			_colSize = arr.get(i).size();
			list = arr.get(i);
			for( j =0; j < _colSize; j++ )
			{
				column = list.get(j);
				column = convertColumnText(column);
				
				if( i == 0 )
				{
					retData.add( column );
				}
				else if( retData.get(j).equals("empty") ||  (column.equals("value") == false && column.equals("empty") == false) )
				{
					retData.set(j, column);
				}
				
			}
			
		}
		
		
		return retData;
	}
	
	private ArrayList<String> makeRowListInfoToArray( ArrayList<ArrayList<String>> arr )
	{
		ArrayList<String> retAr = new ArrayList<String>();
		String _str = "";
		
		for (int i = 0; i < arr.size() ; i++) {
			_str = arr.get(i).get(0);
			
			retAr.add( convertRowText(_str) );
		}
		
		return retAr;
	}
	
	
	private ArrayList<Integer> makeValueColumnIndexAr( ArrayList<String> colar, int valueCnt )
	{
		ArrayList<Integer> retAr = new ArrayList<Integer>();
		
		int i = 0;
		int _currentIndex = 0;

		for (i = 0; i < colar.size(); i++) {
			
			if( colar.get(i).equals("value") )
			{
				retAr.add(_currentIndex);
				if( _currentIndex +1 == valueCnt)
				{
					_currentIndex = 0;
				}
				else
				{
					_currentIndex = _currentIndex + 1;
				}
				
			}
			else
			{
				retAr.add(-1);
			}
			
		}
		
		
		return retAr;
	}
	
	private String convertColumnText( String str )
	{
		
		if(  UBIPivotParser.substringUBF(str, 0, 1).equals("&") == false  ||  UBIPivotParser.substringUBF(str, 0, 2) == "&#" )
		{
			return "value";
		}
		else if( str.toLowerCase().indexOf(mTitleSumText) != -1 )
		{
			return "sum";
		}
		else if( str.toLowerCase().indexOf(mSummeryValueText) != -1 )
		{
			return "sum";
		}
		else if( str.toLowerCase().indexOf(mGrandValueText) != -1 )
		{
			return "grand";
		}
		else if( str.toLowerCase().indexOf(mTitleTotText) != -1 )
		{
			return "grand";
		}
		else if( str.toLowerCase().indexOf(mTitleText) != -1 )
		{
			return "title";
		}
		else if( str.toLowerCase().indexOf(mEmptyText) != -1 )
		{
			return "empty";
		}
		else if( str.toLowerCase().indexOf(mLeftFixedText) != -1 )
		{
			return "lfixed";
		}
		else if( str.toLowerCase().indexOf(mRightFixedText) != -1 )
		{
			return "rfixed";
		}
		else
		{
			return "title";
		}
		
	}
	
	private String convertRowText(String _str)
	{
		String retStr = "";
		
		if( _str.toLowerCase().indexOf(mTitleText) != -1 )
		{
			retStr = "title";
		}
		else if( _str.toLowerCase().indexOf(mSummeryValueText) != -1 )
		{
			retStr = "sum";
		}
		else if( _str.toLowerCase().indexOf(mGrandValueText) != -1 )
		{
			retStr = "grand";
		}
		else
		{
			retStr = "value";
		}
		
		
		return retStr;
	}
	
	

	public static String arrayJoin(String glue, String array[]) {
	  String result = "";

	  for (int i = 0; i < array.length; i++) {
		result += array[i];
	    if (i < array.length - 1) result += glue;
	  }
	  
	  return result;
	}

	
	
	private ArrayList<ArrayList<HashMap<String, Value>>> continueBandCrossTabPage( HashMap<String, Value> crossObj, Float pageWidth, Float startPosition, Float maxPosition, Float currentStartPosition, ArrayList<Integer> mXAr )
	{
		//1.  HeaderHeight 구하기
		Float _headerHeight = getHeaderHeight(crossObj);
		float _paddingLeft = 0f;
		float _autoWidthNum = 0f;
		float _cloneXposition = 0f;
		float _realPageWidth = 0f;
//		float _realPageHeight = maxPosition - currentStartPosition;
		float _realPageHeight = maxPosition;
		float _rowHeight = 0f;
		
		int rowCnt = 0;
		int titleCnt = 0;
		int valueCnt = 0;
		int i = 0;
		int j = 0;
		int k = 0;
		int _grandPosition = 0;
		int _lFixIndex = 0;
		int _rFixIndex = 0;
		String _crosstabVersion = "";
		
		
		//2. autoWidth속성값 여부 체크( autoWidth값이 true일경우 value의 width값을 조정 ) 
		Boolean autoWidth = false;
		HashMap<String, Value> infoData = crossObj.get("info").getMapValue();
		_crosstabVersion = (crossObj.containsKey("VERSION"))? crossObj.get("VERSION").getStringValue():"";
		
		ArrayList<Integer> columnCntAr = new ArrayList<Integer>();	// 각 페이지별로 몃건의 컬럼이 표시되는지 담아두는 객체
		
		ArrayList<String> columnList = (ArrayList<String>) crossObj.get("columnLists").getArrayStringValue();
		
		ArrayList<Integer> valueColumnIndexAr = crossObj.get("valueColumnIndexAr").getArrayIntegerValue();
		
		HashMap<String, Object> itemstyleData = (HashMap<String, Object>) crossObj.get("itemStyle").getMapValue();
		
		ArrayList<ArrayList<HashMap<String, Value>>> valueGrdTotStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("valueGrdTotStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> valueSumStyle 		= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("valueSumStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> valueColumnStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("valueColumnStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> valueLfcStyle 		= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("valueLfcStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> valueRfcStyle 		= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("valueRfcStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> valueValueStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("valueValueStyle");

		ArrayList<ArrayList<HashMap<String, Value>>> headerGrdTotStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerGrdTotStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerSumStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerSumStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerColumnStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerColumnStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerLfcStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerLfcStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerRfcStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerRfcStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerValueStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerValueStyle");

		ArrayList<ArrayList<HashMap<String, Value>>> headerLabelGrdTotStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerLabelGrdTotStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerLabelSumStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerLabelSumStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerLabelColumnStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerLabelColumnStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerLabelLfcStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerLabelLfcStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerLabelRfcStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerLabelRfcStyle");
		ArrayList<ArrayList<HashMap<String, Value>>> headerLabelValueStyle 	= (ArrayList<ArrayList<HashMap<String, Value>>>) itemstyleData.get("headerLabelValueStyle");
		
		ArrayList<ArrayList<ArrayList<HashMap<String, Value>>>> pageItems = new ArrayList<ArrayList<ArrayList<HashMap<String, Value>>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> pageHeaderItems = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> currentRowItems = new ArrayList<ArrayList<HashMap<String, Value>>>();
		
		boolean _visibleType = false;
		
		
		if( infoData.containsKey("autoWidth") && infoData.get("autoWidth").getBooleanValue() )
		{
			autoWidth = infoData.get("autoWidth").getBooleanValue();
		}
		
		if( infoData.containsKey("paddingLeft") && infoData.get("paddingLeft").getStringValue() != "" )
		{
			_paddingLeft = infoData.get("paddingLeft").getIntegerValue();
		}
		
		if( infoData.get("rowNum").getBooleanValue() )_grandPosition = 1;
		
		_realPageWidth = pageWidth - (_paddingLeft*2);
		
		boolean _isNewPage = false;
		boolean _useFitOnePage = false;
		
		if( maxPosition == -1 )
		{
			_visibleType = false;
			infoData.get("newPage").setBooleanValue(false);
			
			if( autoWidth == false && infoData.get("newPage").getBooleanValue() == false && infoData.containsKey("visibleType") && infoData.get("visibleType").getStringValue().equals(BandInfoMapData.VISIBLE_TYPE_ALL) )
			{
				_visibleType = true;
				_useFitOnePage = true;
			}
		}
		else if( infoData.containsKey("newPage") && infoData.get("newPage").getBooleanValue() )
		{
			_isNewPage = true;
		}
		else if( autoWidth == false && infoData.get("newPage").getBooleanValue() == false && infoData.containsKey("visibleType") && infoData.get("visibleType").getStringValue().equals(BandInfoMapData.VISIBLE_TYPE_ALL) )
		{
			_visibleType = true;
			
			// visibleType가 all일경우 밴드의 최대 사이즈를 초과시 페이지를 분할할수 있도록 속성을 변경
			_realPageWidth = BandInfoMapData.BAND_MAX_WIDTH - (_paddingLeft*2);
			_isNewPage = true;
		}
		
		float defaultColumnWidth = 0;
		float defaultRightColumnWidth = 0;
		int defaultColumnCnt = 0;
		int defaultRightColumnCnt = 0;
		int defaultGrdCnt = 0;

		
		//2-1 autoHeight값이 true일경우 autoWidth값 구하기
		float thisWidth = pageWidth;
		titleCnt = 0;
			
		for (String columnName : columnList) {
			
			if( columnName.equals("grand") && crossObj.get("grandSummaryVisible").getBooleanValue() )
			{
				thisWidth = thisWidth - headerGrdTotStyle.get(0).get(0 + _grandPosition).get("width").getIntegerValue();
				
//				defaultRightColumnWidth = defaultRightColumnWidth + headerGrdTotStyle.get(0).get(0 + _grandPosition).get("width").getIntegerValue();
//				defaultRightColumnCnt++;
				defaultGrdCnt++;
			}
			else if( columnName.equals("sum") && crossObj.get("summaryVisible").getBooleanValue() )
			{
				thisWidth = thisWidth - headerSumStyle.get(0).get(0 + _grandPosition).get("width").getIntegerValue();
			}
			else if( columnName.equals("title") )
			{
				thisWidth = thisWidth - headerColumnStyle.get(0).get(titleCnt).get("width").getIntegerValue();
				defaultColumnWidth = defaultColumnWidth + headerColumnStyle.get(0).get(titleCnt).get("width").getIntegerValue();
				defaultColumnCnt++;
				titleCnt++;
			}
			else if( columnName.equals("lfixed") )
			{
				thisWidth = thisWidth - headerLfcStyle.get(0).get(_lFixIndex).get("width").getIntegerValue();
				defaultColumnWidth = defaultColumnWidth + headerLfcStyle.get(0).get(_lFixIndex).get("width").getIntegerValue();
				defaultColumnCnt++;
				_lFixIndex++;
			}
			else if( columnName.equals("rfixed") )
			{
				thisWidth = thisWidth - headerRfcStyle.get(0).get(_rFixIndex).get("width").getIntegerValue();
				defaultRightColumnWidth = defaultRightColumnWidth + headerRfcStyle.get(0).get(_rFixIndex).get("width").getIntegerValue();
				defaultRightColumnCnt++;
				_rFixIndex++;
			}
			else
			{
				valueCnt = valueCnt + 1;
			}
			
		}
		
		float _subAutoWidthNum = 0;
		float _addSize = 0;
		float _tmpSize = 0;
		
		
		// valueCnt : value값의 총 column 갯수 
		// autoWidth가 true이고 페이지별 최대 column수가 지정되 있을경우 
		int _columnMaxCnt = -1;
		int _chkColCnt = 0;
		boolean _autoWNextFlag = false;
		int _lastPageValueIdx = 0;
		float _lastPageAutoWidthNum = -1;
		float _lastPageSubAutoWdith = -1;
		float _addAutoW = 0;
		float _addSubAutoW = 0;
		boolean _valueRepeat = false;
		String _valueRepeatType = "none";
		
		if( infoData.containsKey("repeatedValueType") )
		{
			_valueRepeatType = infoData.get("repeatedValueType").getStringValue();
		}
		
		if( infoData.containsKey("autoWidthCount"))
		{
			_columnMaxCnt = infoData.get("autoWidthCount").getIntValue();
		}
		
		boolean _isVRepeatValue = false;
		boolean _isHRepeatValue = false;
		
		// 가로 병합 확인(value영역)
		if( _valueRepeatType.equals(GlobalVariableData.CROSSTAB_REPEAT_VALUE_HORIZONTAL) || _valueRepeatType.equals(GlobalVariableData.CROSSTAB_REPEAT_VALUE_ALL) )
		{
			_isHRepeatValue = true;
			_valueRepeat = true;
		}
		
		// 세로 병합 확인(value영역)
		if( _valueRepeatType.equals(GlobalVariableData.CROSSTAB_REPEAT_VALUE_VERTICAL) || _valueRepeatType.equals(GlobalVariableData.CROSSTAB_REPEAT_VALUE_ALL) )
		{
			_isVRepeatValue = true;
			_valueRepeat = true;
		}
		
		if(autoWidth)
		{	
			thisWidth = thisWidth -  (_paddingLeft*2);
			
			if( _columnMaxCnt > 0 && valueCnt > _columnMaxCnt )
			{
				_chkColCnt = _columnMaxCnt;
				_autoWNextFlag = true;
				_isNewPage = true;
				
				_lastPageValueIdx = Double.valueOf( (Math.floor( valueCnt/_columnMaxCnt ) * _columnMaxCnt) ).intValue();
				_lastPageAutoWidthNum = thisWidth/(valueCnt%_columnMaxCnt);
				_lastPageSubAutoWdith = Float.valueOf( Math.round( (_lastPageAutoWidthNum - Math.floor(_lastPageAutoWidthNum))*100) )/100;
				_lastPageAutoWidthNum = Double.valueOf( Math.floor(_lastPageAutoWidthNum)).floatValue();
			}
			else
			{
				_chkColCnt = valueCnt;
				_autoWNextFlag = false;
			}
			
			_autoWidthNum = thisWidth/_chkColCnt;
			_subAutoWidthNum = Float.valueOf( Math.round( (_autoWidthNum - Math.floor(_autoWidthNum))*100) )/100;
			_autoWidthNum = Double.valueOf( Math.floor(_autoWidthNum)).floatValue();
		}
		else
		{
			_realPageWidth = _realPageWidth - defaultRightColumnWidth;
		}
		
		
		_lFixIndex = columnList.indexOf("lfixed");
		_rFixIndex = columnList.indexOf("rfixed");
		
		
		//3. DATA를 이용하여 총 페이지수 구하기( newPage속성으로 가로 페이지구하기 )
		HashMap<String, Value> _rowColumMap = (HashMap<String, Value>) crossObj.get("arrayList").getMapValue();
		ArrayList<String> _rowLists = _rowColumMap.get("ROW").getArrayStringValue();
		ArrayList<String> _colLists = _rowColumMap.get("COL").getArrayStringValue();
		
		float _x = 0f;
		float _y = 0f;
		
		ArrayList<ArrayList<String>> rowData; 	// Row의 데이터
		rowData = (ArrayList<ArrayList<String>>) crossObj.get("pageInfo").getMapValue().get("DATA");
		rowCnt = rowData.size();				// 총 데이터 수
		
		HashMap<String, Value> _property;
		
		// 아이템의 y좌표를 위해 기본값을 매칭
		_y = currentStartPosition + _headerHeight;
		
		int _colIndex = 0;
		int _addPosition = 0;
		int _rowSummeryNum = 0;
		int headerCnt = 0;
		int _firstColumnIndex = 0;
		boolean _useBandExcel = false;
//		_y = 0f;
		
		boolean _adjustableHeight 	=	false;
		boolean _resizeText			=	false;
		
		if( crossObj.get("info").getMapValue().containsKey("adjustableHeight") )
		{
			_adjustableHeight = ((Value) crossObj.get("info").getMapValue().get("adjustableHeight")).getBooleanValue();
			if( _adjustableHeight && _isVRepeatValue ) _isVRepeatValue = false;		// 가변Row일경우 value의 세로병합을 제한한다.
		}

		if( crossObj.get("info").getMapValue().containsKey("resizeText") )
		{
			_resizeText = ((Value) crossObj.get("info").getMapValue().get("resizeText")).getBooleanValue();
		}

		if( isExportType.equals("EXCEL") && isExcelOption.equals("BAND") )
		{
			_resizeText = false;
			_useBandExcel = true;
		}
		
		int argoIndex = 0;
		
		float _updateHeight = 0f;
		float _updateWidth = 0f;
		String valueTxt = "";
		float _moveAddPosition = 0;
		HashMap<String, Value> _addProperty;
		HashMap<String, Value> _chkeckProperty;
		HashMap<String, Object> _styleReturnMap;
		HashMap<Integer, Integer> rowItemListData = new HashMap<Integer, Integer>();
		
		columnCntAr.add(0);
		
		// 임시로 아이템을 담아둘 객체
		ArrayList<ArrayList<ArrayList<HashMap<String, Value>>>> tempPageItems = new ArrayList<ArrayList<ArrayList<HashMap<String, Value>>>>();
		boolean _nextPageFlag = false;
		
		if(_visibleType)
		{
			pageWidth = BandInfoMapData.BAND_MAX_WIDTH ;
		}
		
		// newPage활성화시 다음페이지로 넘어갈시 신규 배열에 담기( 컬럼인덱스 담기 )
		
		// 컬럼별 이전 컬럼의 타이틀 명(summary 라벨에 텍스트변경을 위해 이전컬럼값을 담아둔다 
		ArrayList<String> _columnTitleAr = new ArrayList<String>();
		// 컬럼별 repeatedValue 속성값을 담아두는 배열 
		ArrayList<Boolean> _columnRepeatAr = new ArrayList<Boolean>();
		
		for ( i = 0; i < rowCnt; i++) {
			_x = _paddingLeft;
			_addPosition = 0;
			
//			rowItemListData = new HashMap<Integer, Integer>();
			
			_realPageWidth = pageWidth - (_paddingLeft*2) - defaultRightColumnWidth;
			
			_addSize = 0;
			
			int _columnValueIdx = 0;
			boolean _useColumnRepeatValue = true;
			boolean _isValueField = true;
			boolean _isSummaryField = false;
			
			
			// 이전 ROW의 아이템의 TEXT 담아두기 (SUMMARY이고 REPEATVALUE가 FALSE일경우 TEXT교체 )
			// 이전 ROW의 REPEATEDVALUE 속성 담기
			
			
			// 가변Row height 기능 추가 
			// 가로 병합여부 판단 ( 가로 병합일경우 전체 병합된 셀의 width에 맞춰서 변경된 height를 담기 
			// 아이템이 최대 사이즈를 넘길경우 페이지 분할 기능 요건 
			// title / row / summary  가변 처리 영역 지정 방법
			// 총 분할된 셀의 row갯수만큼 배열에 추가  [ [ {},{},{}],[{},{},{}],[{},{},{}],... ]
			
			/**			*/
			ArrayList<Float> _rowHeights = new ArrayList<Float>();
			ArrayList<HashMap<String, Value> > _rowItems = new ArrayList<HashMap<String, Value> >();
			int _pageLimitCnt = 0;
			float _tmpY = _y;
			
			for ( j = 0; j < rowData.get(i).size(); j++) {
				
				if( rowData.get(i) == null ) continue;
				
				//1. 아이템의 기본 properties를 추출
				if( rowData.get(i).size()-j <= defaultRightColumnCnt )
				{
//					if( pageWidth - (_paddingLeft*2) - _x >= defaultRightColumnWidth )
//					{
//						_realPageWidth = pageWidth - (_paddingLeft*2);
//					}
				}
				
				if( rowData.get(i).get(0).toLowerCase().indexOf(mSummeryValueText) != -1 )
				{
					_rowSummeryNum = getSummeryColumn(rowData.get(i));
					_styleReturnMap = crossTabItemStyleChanger(crossObj, "summery", _rowSummeryNum, j, _lFixIndex, _rFixIndex, valueColumnIndexAr.get(j) );
					
					_isValueField = false;
					_isSummaryField = true;
				}
				else if( rowData.get(i).get(0).toLowerCase().indexOf(mGrandValueText) != -1 )
				{
					_styleReturnMap = crossTabItemStyleChanger(crossObj, "grand", 0, j, _lFixIndex, _rFixIndex, valueColumnIndexAr.get(j) );
					_isValueField = false;
					_isSummaryField = false;
				}
				else
				{
					_styleReturnMap = crossTabItemStyleChanger(crossObj, "", 0, j, _lFixIndex, _rFixIndex, valueColumnIndexAr.get(j) );
					
					_isValueField = true;
					_isSummaryField = false;
				}
				
				_property = (HashMap<String, Value>) _styleReturnMap.get("style");
				valueTxt = (String) _styleReturnMap.get("valueTxt");
				_property.put("height", new Value( _styleReturnMap.get("height"),"number" ));
				_property.put("text", new Value( crosstabChangeText(valueTxt, rowData.get(i).get(j).toString()), "string" ));
				
				if( columnList.get(j).equals("title") && _crosstabVersion.equals(CROSSTAB_VERSION_2))
				{
					if( _isValueField && _columnRepeatAr.size() < j+1)
					{
						boolean _repeatValue = (_property.containsKey("repeatedValue"))? _property.get("repeatedValue").getBooleanValue() : true;
						_columnRepeatAr.add(_repeatValue);
					}

					_useColumnRepeatValue = _columnRepeatAr.get(j);
					
					if( _isValueField )
					{
						if( _columnTitleAr.size() > j)  _columnTitleAr.set(j, _property.get("text").getStringValue() );
						else _columnTitleAr.add( _property.get("text").getStringValue() );
					}
					else if( !_useColumnRepeatValue && _isSummaryField && rowData.get(i).get(j).toLowerCase().equals(mSummeryValueText) )
					{
						_property.put("text", new Value( _columnTitleAr.get(j), "string" ));
					}
				}
				
				if( autoWidth && !(columnList.get(j).equals("grand") || columnList.get(j).equals("sum") || columnList.get(j).equals("title") 
						|| columnList.get(j).equals("lfixed") || columnList.get(j).equals("rfixed")) )
				{
					if( autoWidth )
					{
						// autoWidth값이 true이고 maxColumnCount가 존재하고 마지막 페이지일때 
						if( _autoWNextFlag && _lastPageValueIdx > 0 && _columnValueIdx >= _lastPageValueIdx )
						{
							_addAutoW 		= _lastPageAutoWidthNum;
							_addSubAutoW 	= _lastPageSubAutoWdith;
						}
						else
						{
							_addAutoW 		= _autoWidthNum;
							_addSubAutoW 	= _subAutoWidthNum;
						}
						
						_addSize = _addSize + _addSubAutoW;
						if( _addSize > 1 )
						{
							_tmpSize = 1;
							_addSize = _addSize -1;
						}
						else
						{
							_tmpSize = 0;
						}
						_property.put("width", new Value( _addAutoW + _tmpSize , "number" ));
					}
					
					_columnValueIdx++;
				}
				
				//newPage속성이 true일경우 width값이 페이지를 넘어갈시 다음 배열에 값 입력
				if ( (_autoWNextFlag == true || autoWidth == false) && _isNewPage ) {
					
					if( _x +  _property.get("width").getIntegerValue() > _paddingLeft + _realPageWidth )
					{
						_x = _paddingLeft + defaultColumnWidth;
						_pageLimitCnt = j;
					}
				}
				
				// property값 업데이트
				_property.put("x", new Value( _x + _cloneXposition, "number" ));
				_property.put("y", new Value( _tmpY, "number" ));
				
				//@UBFX
				ArrayList<ArrayList<String>> _headerData = (ArrayList<ArrayList<String>>) crossObj.get("pageInfo").getMapValue().get("HEADER");
				
				if( crossObj.containsKey("FILE_LOAD_TYPE") && crossObj.get("FILE_LOAD_TYPE").getStringValue().equals(GlobalVariableData.M_FILE_LOAD_TYPE_JSON))
				{
					HashMap<String, Object> _retElement = convertUBFXJson(_property, rowData.get(i),_rowLists, j, _headerData,_colLists);
					if(_retElement != null ) _property.put("Element", new Value( _retElement));
				}
				else
				{
					Element _cloneElement = null;
					_cloneElement = convertUBFX(_property, rowData.get(i),_rowLists, j, _headerData,_colLists);

					if( _cloneElement != null )_property.put("Element", new Value( _cloneElement, "element"));
				}
				
				_x = _x + _property.get("width").getIntegerValue();
				_rowHeight = _property.get("height").getIntegerValue();
				
				if(_realPageHeight > -1 && _realPageHeight < _tmpY + _rowHeight )
				{
					_realPageHeight = maxPosition;
					_tmpY = startPosition + _headerHeight;
				}
				
				//2. 아이템의 가로 병합 여부 확인 ( 세로 병합은 하지 않는다. )
				
				// value처리
				if( isExportData == false && j >= headerColumnStyle.get(0).size() && _valueRepeat )
				{
					////	crossTab의 value 병합 처리 
					// value 이고 repeatValue가 지정되 있을경우 
					// row와 column병합 여부를 결정하고 병합처리 
					String chkStr = rowData.get(i).get(j);
					String firstColumnType = rowData.get(i).get(0).toLowerCase();
					_chkeckProperty = null;
					int _removeItemHPosition = -1;
					HashMap<String, Value> _tmpProperty = null;
					
					int _chk = 0;
					int _argoChk = 0;
					_chk = _rowItems.size() -1;
					boolean _useVerticalRepeat = true;
					
					if( _chk > -1 && _chk > _pageLimitCnt )
					{
						// 가로 병합
						
						if( j > 0 && _isHRepeatValue  )
						{
							for ( k = _chk; k >  _pageLimitCnt ; k--) {
								
								if( _rowItems.get(k) != null )
								{
									_chkeckProperty = _rowItems.get(k);
									argoIndex = k;
									break;
								}
							}
							
							if( _chkeckProperty.get("text").getStringValue().equals(chkStr) )
							{
								_updateWidth = (_property.get("x").getIntegerValue() - _chkeckProperty.get("x").getIntegerValue()) + _property.get("width").getIntegerValue();
								_rowItems.get(k).put("width", new Value( _updateWidth,"number") );
								
								_tmpProperty = _rowItems.get(k);
								
								_property = null;
								_removeItemHPosition = argoIndex;
							}
						}
						
					}
					////	crossTab의 value 병합 처리 End
				}
				
				_rowItems.add(_property);
				
			}
			
			HashMap<String, Value> _tmpProp;
			float _itemMaxHeight = 0;
			
			boolean _useAdjusTableHeight = false;
			ArrayList<ArrayList<HashMap<String, Value> >> _rowItemsSplit = new ArrayList<ArrayList<HashMap<String, Value> >>();
			ArrayList<Float> _rowHeightArList = new ArrayList<Float>();
			ArrayList<ArrayList<String>> _textArList = new ArrayList<ArrayList<String>>(); 
			
			
			//3. 아이템의 변경된 height추출
			for ( j = 0; j < _rowItems.size(); j++) {
				
				_tmpProp = _rowItems.get(j);
				
				// 가변Row가 있을경우 처리 
//				if( _tmpProp != null && _tmpProp.containsKey("adjustableHeight") && _tmpProp.get("adjustableHeight").getBooleanValue() )
				if( _tmpProp != null && _adjustableHeight )
				{
					HashMap<String, Object> _resultMap = null;
					float _rHeight = maxPosition - startPosition - _headerHeight;
					
					if(_useBandExcel)
					{
						_rHeight = 10000;	// 밴드타입일경우 height제한없이 height를 구하도록 한다.
					}
					
					if( _resizeText )
					{
						float _curH = _realPageHeight -  _tmpProp.get("y").getIntegerValue();
						if( _curH < _tmpProp.get("height").getIntegerValue()  )_curH = _tmpProp.get("height").getIntegerValue();
						float[] _heightAr = {_curH, _rHeight };
						_resultMap = getSplitCharacter(_tmpProp, _heightAr);
					}
					else
					{
						float[] _heightAr = {_rHeight , _rHeight };
						_resultMap = getSplitCharacter(_tmpProp, _heightAr);
					}
					
					if(_itemMaxHeight == 0)
					{
						_itemMaxHeight = _tmpProp.get("height").getIntegerValue();
					}
					
					if( _resultMap.containsKey("Height"))
					{
						ArrayList<Float> _hAr = new ArrayList<Float>();
						ArrayList<String> _tAr = new ArrayList<String>();
						
						if( _resizeText )
						{
							_hAr = (ArrayList<Float>) _resultMap.get("Height");
							_tAr = (ArrayList<String>) _resultMap.get("Text");
						}
						else
						{
							_hAr.add( ((ArrayList<Float>) _resultMap.get("Height")).get(0));
							_tAr.add( _tmpProp.get("text").getStringValue() );
						}
						
						if(  j >= headerColumnStyle.get(0).size() )
						{
							_textArList.add( _tAr  );
							
							if( _hAr.size() > 0)
							{ 
								for( int m = 0; m < _hAr.size(); m++ )
								{
									if( _rowHeightArList.size() > m )
									{
										if( _rowHeightArList.get(m) < _hAr.get(m) )
										{
											_rowHeightArList.set(m, _hAr.get(m));
										}
										
									}
									else
									{
										if( _hAr.get(m) > _tmpProp.get("height").getIntegerValue() )
										{
											_rowHeightArList.add(_hAr.get(m));
										}
										else
										{
											_rowHeightArList.add( _tmpProp.get("height").getIntegerValue() );
										}
									}
								}
							}
							
						}
						else
						{
							_textArList.add( null );
						}
//						
						_useAdjusTableHeight = true;
					}
					else
					{
						_textArList.add(null);
					}
					
				}
				else
				{
					_textArList.add(null);
				}
				
			}
			
			if(_useAdjusTableHeight)
			{
				ArrayList<HashMap<String, Value> > _tmpRows = new ArrayList<HashMap<String, Value> >();
				String _addStr = "";
				ArrayList<String> _tmpStringRows = new ArrayList<String>();
				for( int _rowIdx = 0; _rowIdx < _rowHeightArList.size(); _rowIdx++ )
				{
					_tmpRows = new ArrayList<HashMap<String, Value>>();
					for( int l = 0; l < _rowItems.size(); l++ )
					{
						_tmpProp = _rowItems.get(l);
						if( _tmpProp == null )
						{
							_tmpRows.add( null );
						}
						else
						{
							_tmpProp.put("height", new Value(_rowHeightArList.get(_rowIdx), "number"));
							
							if( l >= headerColumnStyle.get(0).size() && _textArList.get(l) == null )
							{
								_addStr = "";
							}
							else if(  l < headerColumnStyle.get(0).size() )
							{
								_addStr =  _tmpProp.get("text").getStringValue();
							}
							else if( _textArList.get(l).size() > _rowIdx )
							{
								_addStr = _textArList.get(l).get(_rowIdx);
							}
							else
							{
								_addStr = "";
							}
							
							_tmpProp.put("text", new Value( _addStr ,"string"));
							
							if( j == 0 )
							{
								_tmpRows.add(_tmpProp);
							}
							else
							{
								_tmpRows.add( (HashMap<String, Value>) _tmpProp.clone() );
							}
							
						}
					}
					_rowItemsSplit.add(_tmpRows);
				}
				
			}
			else
			{
				_rowItemsSplit.add(_rowItems);
			}
			
			//4. 최대 몃개의 페이지로 구분되는지 확인
			
			
			//5. 최대 페이지에 맞춰서 페이지별 아이템 생성
			
			
			// 가로 병합까지 완료된 아이템을 확인하여 아이템의 변경된 Text값 가져오기

//			for ( j = 0; j < rowData.get(i).size(); j++) {
			for( int r = 0; r < _rowItemsSplit.size(); r ++ )
			{
				// 페이지 분할이므로 모두 초기화 처리 
				_property = _rowItemsSplit.get(r).get(0);
				_x = _paddingLeft;
				_addPosition = 0;
				
				if(_realPageHeight > -1 && _realPageHeight < _y + _property.get("height").getIntegerValue() )
				{
					for (int l = i-1; l >= 0; l--) {
						rowData.remove(l);
					}
					rowCnt = rowCnt - i;
					i = 0;
					for (int l = 0; l < pageItems.size(); l++) {
						tempPageItems.add(pageItems.get(l));
					}
					
					_realPageHeight = maxPosition;
					_y = startPosition + _headerHeight;
					_nextPageFlag = false;
					pageItems.clear();
				}
				
				for ( j = 0; j < _rowItemsSplit.get(r).size(); j++) {
					
					_property = _rowItemsSplit.get(r).get(j);
					
					/**
					if( rowData.get(i).size()-j <= defaultRightColumnCnt )
					{
						if( pageWidth - (_paddingLeft*2) - _x >= defaultRightColumnWidth )
						{
							_realPageWidth = pageWidth - (_paddingLeft*2);
						}
					}
					
					if( rowData.get(i).get(0).toLowerCase().indexOf(mSummeryValueText) != -1 )
					{
						_rowSummeryNum = getSummeryColumn(rowData.get(i));
						_styleReturnMap = crossTabItemStyleChanger(crossObj, "summery", _rowSummeryNum, j, _lFixIndex, _rFixIndex, valueColumnIndexAr.get(j) );
						
						_isValueField = false;
						_isSummaryField = true;
					}
					else if( rowData.get(i).get(0).toLowerCase().indexOf(mGrandValueText) != -1 )
					{
						_styleReturnMap = crossTabItemStyleChanger(crossObj, "grand", 0, j, _lFixIndex, _rFixIndex, valueColumnIndexAr.get(j) );
						_isValueField = false;
						_isSummaryField = false;
					}
					else
					{
						_styleReturnMap = crossTabItemStyleChanger(crossObj, "", 0, j, _lFixIndex, _rFixIndex, valueColumnIndexAr.get(j) );
						
						_isValueField = true;
						_isSummaryField = false;
					}
					
					_property = (HashMap<String, Value>) _styleReturnMap.get("style");
					valueTxt = (String) _styleReturnMap.get("valueTxt");
					_property.put("height", new Value( _styleReturnMap.get("height"),"number" ));
					_property.put("text", new Value( crosstabChangeText(valueTxt, rowData.get(i).get(j).toString()), "string" ));
					
					if( columnList.get(j).equals("title") && _crosstabVersion.equals(CROSSTAB_VERSION_2))
					{
						if( _isValueField && _columnRepeatAr.size() < j+1)
						{
							boolean _repeatValue = (_property.containsKey("repeatedValue"))? _property.get("repeatedValue").getBooleanValue() : true;
							_columnRepeatAr.add(_repeatValue);
						}

						_useColumnRepeatValue = _columnRepeatAr.get(j);
						
						if( _isValueField )
						{
							if( _columnTitleAr.size() > j)  _columnTitleAr.set(j, _property.get("text").getStringValue() );
							else _columnTitleAr.add( _property.get("text").getStringValue() );
						}
						else if( !_useColumnRepeatValue && _isSummaryField && rowData.get(i).get(j).toLowerCase().equals(mSummeryValueText) )
						{
							_property.put("text", new Value( _columnTitleAr.get(j), "string" ));
						}
					}
					
					
					
					
					if( autoWidth && !(columnList.get(j).equals("grand") || columnList.get(j).equals("sum") || columnList.get(j).equals("title") 
							|| columnList.get(j).equals("lfixed") || columnList.get(j).equals("rfixed")) )
					{
						if( autoWidth )
						{
							// autoWidth값이 true이고 maxColumnCount가 존재하고 마지막 페이지일때 
							if( _autoWNextFlag && _lastPageValueIdx > 0 && _columnValueIdx >= _lastPageValueIdx )
							{
								_addAutoW 		= _lastPageAutoWidthNum;
								_addSubAutoW 	= _lastPageSubAutoWdith;
							}
							else
							{
								_addAutoW 		= _autoWidthNum;
								_addSubAutoW 	= _subAutoWidthNum;
							}
							
							_addSize = _addSize + _addSubAutoW;
							if( _addSize > 1 )
							{
								_tmpSize = 1;
								_addSize = _addSize -1;
							}
							else
							{
								_tmpSize = 0;
							}
							_property.put("width", new Value( _addAutoW + _tmpSize , "number" ));
						}
						
						_columnValueIdx++;
					}
					
					
//					if( _realPageHeight < _y + _rowHeight ){
//						_realPageHeight = maxPosition;
//						_addPosition = pageItems.size();
//						
//						_y = startPosition + _headerHeight;
//					}
					
					//newPage속성이 true일경우 width값이 페이지를 넘어갈시 다음 배열에 값 입력
					if ( (_autoWNextFlag = true || autoWidth == false) && _isNewPage ) {
						
						if( _x +  _property.get("width").getIntegerValue() > _paddingLeft + _realPageWidth )
						{
							_x = _paddingLeft + defaultColumnWidth;
//							_x = _paddingLeft;
							_addSize = 0;
							if( columnCntAr.indexOf(j) == -1) columnCntAr.add(j);
							
							_addPosition = columnCntAr.indexOf(j) + _colIndex;
							
							if( mMaxPageWidth > 0 )
							{
								if(mMaxPageWidthAr.size() > _addPosition-1) mMaxPageWidthAr.set(_addPosition-1, mMaxPageWidth);
								else mMaxPageWidthAr.add(mMaxPageWidth);
								mMaxPageWidth = 0;
							}
						}
						
						// 페이지의 maxWidth값 가져오기
						if( ( infoData.containsKey("newPage")==false || infoData.get("newPage").getBooleanValue()==false ) && _visibleType && mMaxPageWidth < _x +  _property.get("width").getIntegerValue() )
						{
							mMaxPageWidth = _x +  _property.get("width").getIntegerValue() + _paddingLeft;
						}
					}
					*/ 
					
					//newPage속성이 true일경우 width값이 페이지를 넘어갈시 다음 배열에 값 입력
					if ( ((_autoWNextFlag == true || autoWidth == false) && _isNewPage ) || _useFitOnePage )  {
						
						float _cW = 0;
						if( _property != null ) _cW = _property.get("width").getIntegerValue();
							
						if( _x +  _cW > _paddingLeft + _realPageWidth )
						{
							_x = _paddingLeft + defaultColumnWidth;
							_addSize = 0;
							if( columnCntAr.indexOf(j) == -1) columnCntAr.add(j);
							
							_pageLimitCnt = j;
							
							_addPosition = columnCntAr.indexOf(j) + _colIndex;
							
							if( mMaxPageWidth > 0 )
							{
								if(mMaxPageWidthAr.size() > _addPosition-1) mMaxPageWidthAr.set(_addPosition-1, mMaxPageWidth);
								else mMaxPageWidthAr.add(mMaxPageWidth);
								mMaxPageWidth = 0;
							}
						}
						
						// 페이지의 maxWidth값 가져오기
						if( ( infoData.containsKey("newPage")==false || infoData.get("newPage").getBooleanValue()==false || _useFitOnePage ) && _visibleType && mMaxPageWidth < _x +  _cW )
						{
							mMaxPageWidth = _x +  _cW + _paddingLeft;
						}
					}
					
					
					if( _property == null ) continue;
					
					// property값 업데이트
					_property.put("x", new Value( _x + _cloneXposition, "number" ));
					_property.put("y", new Value( _y, "number" ));
					
					//@UBFX
					ArrayList<ArrayList<String>> _headerData = (ArrayList<ArrayList<String>>) crossObj.get("pageInfo").getMapValue().get("HEADER");
					
					if( crossObj.containsKey("FILE_LOAD_TYPE") && crossObj.get("FILE_LOAD_TYPE").getStringValue().equals(GlobalVariableData.M_FILE_LOAD_TYPE_JSON))
					{
						
						HashMap<String, Object> _retElement = convertUBFXJson(_property, rowData.get(i),_rowLists, j, _headerData,_colLists);
						if(_retElement != null ) _property.put("Element", new Value( _retElement));
					}
					else
					{
						Element _cloneElement = null;
						_cloneElement = convertUBFX(_property, rowData.get(i),_rowLists, j, _headerData,_colLists);

						if( _cloneElement != null )_property.put("Element", new Value( _cloneElement, "element"));
					}
					
					_x = _x + _property.get("width").getIntegerValue();
					_rowHeight = _property.get("height").getIntegerValue();
					
					if( rowCnt-1 > i )
					{
						if( _realPageHeight > -1 && _realPageHeight < _y + (_rowHeight*2) ){
							_nextPageFlag = true;
						}
					}
					
					// 컬럼 병합처리
					if( isExportData == false && j < headerColumnStyle.get(0).size() ){
//					if( isExportData == false ){
						
						String chkStr = rowData.get(i).get(j);
						String firstColumnType = rowData.get(i).get(0).toLowerCase();
						_chkeckProperty = null;
						
						if( j > 0 && firstColumnType.indexOf(mSummeryValueText) != -1 && (chkStr.toLowerCase().equals(mEmptyText) || _property.get("text").getStringValue().equals(""))  )
						{
							rowItemListData.put(j, null);
							
							for ( k = j-1; k >  -1 ; k--) {
								
								if( pageItems.get(_addPosition).get(i).get(k-_addPosition) != null )
								{
									_chkeckProperty = pageItems.get(_addPosition).get(i).get(k-_addPosition);
									argoIndex = k;
									break;
								}
								
							}
							
							if( _chkeckProperty != null )
							{
								_updateWidth = (_property.get("x").getIntegerValue() - _chkeckProperty.get("x").getIntegerValue()) + _property.get("width").getIntegerValue();
								pageItems.get(_addPosition).get(i).get(argoIndex-_addPosition).put("width", new Value( _updateWidth,"number") );
								
								_property = null;
							}
							
							
						}
						else if( firstColumnType.indexOf(mGrandValueText) != -1 && j > 0 && (chkStr.toLowerCase().equals(mEmptyText) || _property.get("text").getStringValue().equals("")) )
						{
							rowItemListData.put(j, null);
							
							for ( k = j-1; k > -1 ; k--) {
								
								if( pageItems.get(_addPosition).get(i).get(k-_addPosition) != null )
								{
									_chkeckProperty = pageItems.get(_addPosition).get(i).get(k-_addPosition);
									argoIndex = k;
									break;
								}
								
							}
							
							if( _chkeckProperty != null )
							{
								_updateWidth = (_property.get("x").getIntegerValue() - _chkeckProperty.get("x").getIntegerValue()) + _property.get("width").getIntegerValue();
								pageItems.get(_addPosition).get(i).get(argoIndex-_addPosition).put("width", new Value( _updateWidth,"number") );
								
								_property = null;
							}
							
							
						}
						else if( _useColumnRepeatValue && rowItemListData != null && rowItemListData.get(j) != null && i > 0 &&  pageItems.size() > _addPosition && pageItems.get(_addPosition).get(rowItemListData.get(j)).size() > (j-_addPosition) && pageItems.get(_addPosition).get(rowItemListData.get(j)).get(j-_addPosition) != null && 
								(columnRepeatFlag(i, j, pageItems.get(_addPosition).get(rowItemListData.get(j)).get(j-_addPosition).get("text").getStringValue(), rowData.get(i).get(j), rowData) || chkStr.toLowerCase().equals(mSummeryValueText)) )
						{
							
							_updateHeight = ( _property.get("y").getIntegerValue() - pageItems.get(_addPosition).get(rowItemListData.get(j)).get(j-_addPosition).get("y").getIntegerValue() ) + _property.get("height").getIntegerValue();
							pageItems.get(_addPosition).get(rowItemListData.get(j)).get(j-_addPosition).put("height", new Value( _updateHeight,"number") );
							
							for (int l = _addPosition + 1 ; l < pageItems.size(); l++) {
								pageItems.get(l).get(rowItemListData.get(j)).get(j-_addPosition).put("height", new Value( _updateHeight,"number") );
							}
							
							_property = null;
							
						}
						else if( _useColumnRepeatValue && tempPageItems.size() > 0 && chkStr.toLowerCase().equals(mSummeryValueText))
						{	
							
							int _rowP = tempPageItems.get(((tempPageItems.size()-1) - _addPosition) ).size() -1;
							float _oriHeight = _property.get("height").getIntegerValue();
							float _oriY = _property.get("y").getIntegerValue();
							if( rowItemListData.containsKey(j) )
							{
								_rowP = rowItemListData.get(j);
							}
							//pageItems.get(_addPosition).get(i).add(_property);
							HashMap<String, Value> _tempMap = tempPageItems.get(((tempPageItems.size()-1) - _addPosition) ).get( _rowP ).get(j);
							if( _tempMap != null )
							{
								_property = (HashMap<String, Value>) _tempMap.clone();
								_property.put("height", new Value( _oriHeight,"number"));
								_property.put("y", new Value( _oriY,"number"));
							}
							rowItemListData.put(j, i);
//							_property = pageItems.get(_addPosition).get(j);
						}
						else
						{
							rowItemListData.put(j, i);
						}
						
					}
					
					
					if( _addPosition >= pageItems.size() ){
						pageItems.add(new ArrayList<ArrayList<HashMap<String,Value>>>() );
					}
					
					// 페이지가 분할되었을경우 Column 영역을 추가한다.
					if( pageItems.get(_addPosition).size() < i+1 )
					{
						pageItems.get(_addPosition).add(new ArrayList<HashMap<String,Value>>());
						
						if( _addPosition > 0 )
						{
							for (int _c2 = 0; _c2 < defaultColumnCnt; _c2++) {
								
								if(  pageItems.get(0).get(i).get(_c2) != null ){
									_addProperty =  (HashMap<String,Value>) pageItems.get(0).get(i).get(_c2).clone();
									pageItems.get(_addPosition).get(i).add( _addProperty );
								}
								else
								{ 
									pageItems.get(_addPosition).get(i).add( null ); 
								} 
							}
						}
						
					}
					
					
					// value처리
					if( isExportData == false && j >= headerColumnStyle.get(0).size() && _valueRepeat )
					{
						////	crossTab의 value 병합 처리 
						// value 이고 repeatValue가 지정되 있을경우 
						// row와 column병합 여부를 결정하고 병합처리 
						
						String chkStr = rowData.get(i).get(j);
						String firstColumnType = rowData.get(i).get(0).toLowerCase();
						_chkeckProperty = null;
						int _removeItemHPosition = -1;
						HashMap<String, Value> _tmpProperty = null;
						
						int _chk = 0;
						int _argoChk = 0;
						
						_chk = pageItems.get(_addPosition).get(i).size()-1;
						boolean _useVerticalRepeat = true;
						
						if( pageItems.size() > _addPosition )
						{
							// 가로 병합
							
							if( j > 0 && _isHRepeatValue && pageItems.get(_addPosition).size() > i )
							{
								for ( k = _chk; k >  -1 ; k--) {
									
									if( pageItems.get(_addPosition).get(i).get(k) != null )
									{
										_chkeckProperty = pageItems.get(_addPosition).get(i).get(k);
										argoIndex = k;
										break;
									}
								}
								
								if( _chkeckProperty.get("text").getStringValue().equals(chkStr) )
								{
									_updateWidth = (_property.get("x").getIntegerValue() - _chkeckProperty.get("x").getIntegerValue()) + _property.get("width").getIntegerValue();
									pageItems.get(_addPosition).get(i).get(argoIndex).put("width", new Value( _updateWidth,"number") );
									
									_tmpProperty = pageItems.get(_addPosition).get(i).get(argoIndex);
									
									_property = null;
									_removeItemHPosition = argoIndex;
									
									if( _isVRepeatValue )
									{
										if( rowItemListData != null && rowItemListData.get(j) != null && i > 0 &&  pageItems.size() > _addPosition && pageItems.get(_addPosition).get(rowItemListData.get(j)).size() > (_removeItemHPosition) && pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_removeItemHPosition) != null && 
												valueRepeatFlag(i, j, pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_removeItemHPosition).get("text").getStringValue(), rowData.get(i).get(j), rowData) )
										{
											if( _tmpProperty.get("width").getIntegerValue().equals( pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_removeItemHPosition).get("width").getIntegerValue() ) )
											{
												_updateHeight = ( _tmpProperty.get("y").getIntegerValue() - pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_removeItemHPosition).get("y").getIntegerValue() ) + _tmpProperty.get("height").getIntegerValue();
												pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_removeItemHPosition).put("height", new Value( _updateHeight,"number") );
												
												//이전 Row의 병합된 셀의 height 증가후 현재 Row의 아이템 제거
												pageItems.get(_addPosition).get(i).set(argoIndex, null);
												
												_useVerticalRepeat = false;
											}
										}
									}
								}
							}
							
						}
						
						//가로 병합 후 세로 병합까지 처리완료시 아래 부분은 처리하지 않음
						if(_isVRepeatValue && _useVerticalRepeat)
						{
							// 세로병합
							if( rowItemListData != null && rowItemListData.get(j) != null && i > 0 &&  pageItems.size() > _addPosition && pageItems.get(_addPosition).get(rowItemListData.get(j)).size() > (_chk + 1) && pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_chk + 1) != null && 
									valueRepeatFlag(i, j, pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_chk + 1).get("text").getStringValue(), rowData.get(i).get(j), rowData) )
							{
								
								if(_property != null && _property.get("width").getIntegerValue().equals( pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_chk+1).get("width").getIntegerValue() ) )
								{
									_updateHeight = ( _property.get("y").getIntegerValue() - pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_chk+1).get("y").getIntegerValue() ) + _property.get("height").getIntegerValue();
									pageItems.get(_addPosition).get(rowItemListData.get(j)).get(_chk+1).put("height", new Value( _updateHeight,"number") );
									
									_property = null;
								}
								else
								{
									rowItemListData.put(j, i);
								}
							}
							else
							{
								rowItemListData.put(j, i);
							}
						}
						
						////	crossTab의 value 병합 처리 End
					}
					
					if( _property != null )
					{
						//Excel 내보내기시 X배열값을 위하여 X배열을 담아둔다
						int _excelX = Math.round(_property.get("x").getIntegerValue()); 
						int _excelWidth = Math.round(_property.get("width").getIntegerValue()); 
						
						if(mXAr.contains(_excelX) == false)
						{
							mXAr.add(_excelX);
						}
						if(mXAr.contains(_excelX+_excelWidth) == false)
						{
							mXAr.add(_excelX+_excelWidth);
						}
						
						if( k == 0 && infoData.get("rowNum").getBooleanValue() )
						{
							mXAr.add(_excelX + Float.valueOf( mRowNumWidth ).intValue() );
						}
					}
					
					pageItems.get(_addPosition).get(i).add(_property);
					
				}// column for 종료 
				
				_property = _rowItemsSplit.get(r).get(0);
				_y = _y + _property.get("height").getIntegerValue();
			}
			
			if( defaultRightColumnCnt > 0 )
			{
				for (int l = 0; l < pageItems.size() - 1 ; l++) {
					
					for (int l2 = 0; l2 < defaultRightColumnCnt; l2++) {
						_addProperty = (HashMap<String,Value>) pageItems.get(pageItems.size()-1).get(i).get( pageItems.get(pageItems.size()-1).get(i).size() - defaultRightColumnCnt - defaultGrdCnt + l2 ).clone();
						_moveAddPosition = pageItems.get(l).get(i).get(pageItems.get(l).get(i).size()-1).get("x").getIntegerValue() + pageItems.get(l).get(i).get(pageItems.get(l).get(i).size()-1).get("width").getIntegerValue();
						_addProperty.put("x", new Value(_moveAddPosition, "number") );
						pageItems.get(l).get(i).add( _addProperty );
						
					}
				}
			}
			if( mMaxPageWidth > 0 )
			{
				if(mMaxPageWidthAr.size()-1 < _addPosition)
				{
					mMaxPageWidthAr.add(mMaxPageWidth);
				}
				mMaxPageWidth = 0;
			}
			//_y = _y + _rowHeight;
			
			// 1 Row 종료 
			
			
		}
		
		if(pageItems.size() > 0 )
		{
			for (int l = 0; l < pageItems.size(); l++) {
				tempPageItems.add(pageItems.get(l));
			}
		}
		
		pageItems = tempPageItems;
		
		// value영역의 아이템 초기화
		valueGrdTotStyle.clear();
		valueSumStyle.clear();
		valueColumnStyle.clear();
		valueLfcStyle.clear();
		valueRfcStyle.clear();
		valueValueStyle.clear();
		
		Boolean useNextPosition = false;
		//4. DATA영역수만큼 Header영역 생성
		ArrayList<String> headerList = new ArrayList<String>();
		boolean _headerLabelTop = false;
		
		if( crossObj.get("info").getMapValue().containsKey("headerLabelPosition") )
		{
			if(crossObj.get("info").getMapValue().get("headerLabelPosition") instanceof Value )
			{
				_headerLabelTop = ((Value) crossObj.get("info").getMapValue().get("headerLabelPosition")).getStringValue().equals("top");
			}
			else
			{
				_headerLabelTop = crossObj.get("info").getMapValue().get("headerLabelPosition").equals("top");
			}
		}
		
		if( _headerLabelTop )
		{
			headerList.add("headerLabel");
			headerList.add("header");
		}
		else
		{
			headerList.add("header");
			headerList.add("headerLabel");
		}
		
		ArrayList< ArrayList<ArrayList<HashMap<String, Value>>> > headerListData = new ArrayList< ArrayList<ArrayList<HashMap<String, Value>>> >();
		columnCntAr = new ArrayList<Integer>();
		columnCntAr.add(0);
				
		for ( i = 0; i < headerList.size(); i++) {
			_rowHeight = 0;
			useNextPosition = false;
			if(headerList.get(i).equals("header"))
			{
				ArrayList<ArrayList<String>> headerData = (ArrayList<ArrayList<String>>) crossObj.get("pageInfo").getMapValue().get("HEADER");
				headerCnt = headerData.size();
				
				for ( j = 0; j < headerCnt; j++) {
					
					useNextPosition = false;
					_x = _paddingLeft;
					_addPosition = 0;
					_firstColumnIndex = 0;
					pageHeaderItems = new ArrayList<ArrayList<HashMap<String,Value>>>();
					pageHeaderItems.add(new ArrayList<HashMap<String,Value>>() );
					
					_realPageWidth = pageWidth - (_paddingLeft*2) - defaultRightColumnWidth;
					_addSize = 0;
					
					int _columnValueIdx = 0;
					
					for ( k = 0; k < headerData.get(j).size(); k++) {
						
						if( headerData.get(j).size()-k <= defaultRightColumnCnt )
						{
							if( pageWidth - (_paddingLeft*2) - _x >= defaultRightColumnWidth )
							{
								_realPageWidth = pageWidth - (_paddingLeft*2);
							}
						}
						
						useNextPosition = false;
						
						if( columnList.get(k).equals("grand") )
						{
							_property = (HashMap<String, Value>) headerGrdTotStyle.get(j).get( 0+_grandPosition ).clone();
						}
						else if( columnList.get(k).equals("sum") )
						{
							_property = (HashMap<String, Value>) headerSumStyle.get(j).get( 0+_grandPosition ).clone();
						}
						else if( columnList.get(k).equals("title") )
						{
							_property = (HashMap<String, Value>) headerColumnStyle.get(j).get(k).clone();
						}
						else if( columnList.get(k).equals("lfixed") )
						{
							_property = (HashMap<String, Value>) headerLfcStyle.get(j).get(k - _lFixIndex).clone();
						}
						else if( columnList.get(k).equals("rfixed") )
						{
							_property = (HashMap<String, Value>) headerRfcStyle.get(j).get(k - _rFixIndex).clone();
						}
						else
						{
							_property = (HashMap<String, Value>) headerValueStyle.get(j).get( valueColumnIndexAr.get(k) ).clone();
//							if( autoWidth ) _property.put("width", new Value( _autoWidthNum, "number" ));
						}
						
						if( columnList.get(k).equals("title") == false ){
							_property.put("text", new Value( crosstabChangeText( _property.get("text").getStringValue(), headerData.get(j).get(k) ),"string" ));
						}
						
						if( autoWidth && !(columnList.get(k).equals("grand") || columnList.get(k).equals("sum") || columnList.get(k).equals("title") 
								|| columnList.get(k).equals("lfixed") ||columnList.get(k).equals("rfixed")) )
						{
							
							if( _autoWNextFlag &&  _lastPageValueIdx > 0 && _columnValueIdx >= _lastPageValueIdx )
							{
								_addAutoW 		= _lastPageAutoWidthNum;
								_addSubAutoW 	= _lastPageSubAutoWdith;
							}
							else
							{
								_addAutoW 		= _autoWidthNum;
								_addSubAutoW 	= _subAutoWidthNum;
							}
							
							_addSize = _addSize + _addSubAutoW;
							if( _addSize > 1 )
							{
								_tmpSize = 1;
								_addSize = _addSize -1;
							}
							else
							{
								_tmpSize = 0;
							}
							_property.put("width", new Value( _addAutoW + _tmpSize , "number" ));
//							if( autoWidth ) _property.put("width", new Value( _autoWidthNum, "number" ));
							
							_columnValueIdx++;
						}
						
						//newPage속성이 true일경우 width값이 페이지를 넘어갈시 다음 배열에 값 입력
						if ( (_autoWNextFlag == true || autoWidth == false) && _isNewPage ) {
							if( _x +  _property.get("width").getIntegerValue() > _paddingLeft + _realPageWidth )
							{
								_x = _paddingLeft + defaultColumnWidth;
//								_x = _paddingLeft;
								_addSize = 0;
								if( columnCntAr.indexOf(k) == -1) columnCntAr.add(k);
								
								_addPosition = columnCntAr.indexOf(k) + _colIndex;
								
								useNextPosition = true;
							}
						}
						
						if( useNextPosition ){
							pageHeaderItems.add(new ArrayList<HashMap<String,Value>>() );
						}
						
						// property값 업데이트
						_property.put("x", new Value( _x + _cloneXposition, "number" ));
						_property.put("y", new Value( _y, "number" ));
						
						//@UBFX
						_x = _x + _property.get("width").getIntegerValue();
						_rowHeight = _property.get("height").getIntegerValue();
						
						// 헤더 병합 처리
						if( k >= columnCntAr.get(_addPosition) && headerListData.size() >= j && _property.get("text").getStringValue().toLowerCase().equals(mEmptyText) && j > 0 
								&& columnList.get(k).equals("value") == false )
						{
							_updateHeight = (_property.get("y").getIntegerValue() - headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).get("y").getIntegerValue() ) + _property.get("height").getIntegerValue();
							headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).put("height", new Value(_updateHeight,"number"));
							
							if( _property.containsKey("bottomBorderType") )
							{
								headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).put("bottomBorderType", _property.get("bottomBorderType"));
							}
							
							_property = headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition));
						}
						else if(k > 0 && k > columnCntAr.get(_addPosition) && headerListData.size() >= j && pageHeaderItems.get(pageHeaderItems.size()-1) != null && pageHeaderItems.get(pageHeaderItems.size()-1).size() > (k- columnCntAr.get(_addPosition) -1) &&
								titleMergeFlag(j, k, pageHeaderItems.get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)-1).get("text").getStringValue(), _property.get("text").getStringValue(), headerData ) )
						{
							_updateWidth =  (_property.get("x").getIntegerValue() - pageHeaderItems.get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)-1).get("x").getIntegerValue() )  + _property.get("width").getIntegerValue();
							pageHeaderItems.get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)-1).put("width", new Value( _updateWidth, "number"));
							
							_property = pageHeaderItems.get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)-1);
						}
						else if( j > 0 && k >= columnCntAr.get(_addPosition) && headerListData.size() >= j && columnList.get(k).equals("value") == false && 
								_property.get("text").getStringValue().equals( headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k- columnCntAr.get(_addPosition) ).get("text").getStringValue() ) )
						{
							_updateHeight = (_property.get("y").getIntegerValue() - headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).get("y").getIntegerValue() ) + _property.get("height").getIntegerValue();
							headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).put("height", new Value(_updateHeight,"number"));
							
							if( _property.containsKey("bottomBorderType") )
							{
								headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).put("bottomBorderType", _property.get("bottomBorderType"));
							}
							
							_property = headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition));
						}
						
						
						pageHeaderItems.get(pageHeaderItems.size()-1).add(_property);
						
						// ubfx처리 
						//_property.get("Element")
						if( crossObj.containsKey("FILE_LOAD_TYPE") && crossObj.get("FILE_LOAD_TYPE").getStringValue().equals(GlobalVariableData.M_FILE_LOAD_TYPE_JSON))
						{
							
							HashMap<String, Object> _retElement = convertUBFXJson(_property, rowData.get(i),_rowLists, j, headerData,_colLists);
							if(_retElement != null ) _property.put("Element", new Value( _retElement));
						}
						else
						{
							Element _cloneElement = null;
							_cloneElement = convertUBFX(_property, headerData.get(j),_rowLists, k, headerData,_colLists );
	
							if( _cloneElement != null )_property.put("Element", new Value( _cloneElement, "element"));
						}
						
						
					}
					
					_y = _y + _rowHeight;					// y포지션 업데이트
					
					headerListData.add(pageHeaderItems);	// 생성된 헤더를 List에 애드
					
					
				}
				
			}
			else
			{
				
				headerCnt = headerLabelValueStyle.size();
				
				for ( j = 0; j < headerCnt; j++) {
					
					_x = _paddingLeft;
					_addPosition = 0;
					useNextPosition = false;
					pageHeaderItems = new ArrayList<ArrayList<HashMap<String,Value>>>();
					pageHeaderItems.add(new ArrayList<HashMap<String,Value>>() );
					
					_realPageWidth = pageWidth - (_paddingLeft*2) - defaultRightColumnWidth;
					
					for ( k = 0; k < columnList.size(); k++) {
						
						if( columnList.size()-k <= defaultRightColumnCnt )
						{
							if( pageWidth - (_paddingLeft*2) - _x >= defaultRightColumnWidth )
							{
								_realPageWidth = pageWidth - (_paddingLeft*2);
							}
						}
						
						useNextPosition = false;
						
						if( columnList.get(k).equals("grand") )
						{
							_property = (HashMap<String, Value>) headerLabelGrdTotStyle.get(j).get( 0+_grandPosition ).clone();
						}
						else if( columnList.get(k).equals("sum") )
						{
							_property = (HashMap<String, Value>) headerLabelSumStyle.get(j).get( 0+_grandPosition ).clone();
						}
						else if( columnList.get(k).equals("title") )
						{
							_property = (HashMap<String, Value>) headerLabelColumnStyle.get(j).get(k).clone();
						}
						else if( columnList.get(k).equals("lfixed") )
						{
							_property = (HashMap<String, Value>) headerLabelLfcStyle.get(j).get(k - _lFixIndex).clone();
						}
						else if( columnList.get(k).equals("rfixed") )
						{
							_property = (HashMap<String, Value>) headerLabelRfcStyle.get(j).get(k - _rFixIndex).clone();
						}
						else
						{
							_property = (HashMap<String, Value>) headerLabelValueStyle.get(j).get( valueColumnIndexAr.get(k) ).clone();
							if( autoWidth ) _property.put("width", new Value( _autoWidthNum, "number" ));
						}
						
						// 헤더 병합 처리
						
						
						
						//newPage속성이 true일경우 width값이 페이지를 넘어갈시 다음 배열에 값 입력
						if ( autoWidth == false && _isNewPage ) {
							if( _x +  _property.get("width").getIntegerValue() > _paddingLeft + _realPageWidth  )
							{
								_x = _paddingLeft + defaultColumnWidth;
//								_x = _paddingLeft;
								
								if( columnCntAr.indexOf(k) == -1) columnCntAr.add(k);
								
								_addPosition = columnCntAr.indexOf(k) + _colIndex;
								useNextPosition = true;
							}
						}
						
						// property값 업데이트
						_property.put("x", new Value( _x + _cloneXposition, "number" ));
						_property.put("y", new Value( _y, "number" ));
						
						
						_x = _x + _property.get("width").getIntegerValue();
						_rowHeight = _property.get("height").getIntegerValue();
						
						
						// 헤더라벨 병합 처리 
						if( j > 0 && k >= columnCntAr.get(_addPosition) && headerListData.size() > 0 && headerListData.get(headerListData.size()-1).size() > k  && 
								( _property.get("text").getStringValue().toLowerCase().equals(mEmptyText) || _property.get("text").getStringValue().toLowerCase().equals("") 
										&& columnList.get(k).equals("value") == false)	)
						{
							_updateHeight = (_property.get("y").getIntegerValue() - headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).get("y").getIntegerValue() ) + _property.get("height").getIntegerValue();
							headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).put("height", new Value(_updateHeight,"number"));
							
							if( _property.containsKey("bottomBorderType") )
							{
								headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).put("bottomBorderType", _property.get("bottomBorderType"));
							}
							
							_property = headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition));
						}
						else if( j > 0 &&  k >= columnCntAr.get(_addPosition) &&  headerListData.get(headerListData.size()-1).size() > j+1 && columnList.get(k).equals("value") == false &&
								_property.get("text").getStringValue().equals( headerListData.get(headerListData.size()-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).get("text").getStringValue() ))
						{
							_updateHeight = (_property.get("y").getIntegerValue() - headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).get("y").getIntegerValue() ) + _property.get("height").getIntegerValue();
							headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).put("height", new Value(_updateHeight,"number"));
							
							if( _property.containsKey("bottomBorderType") )
							{
								headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).put("bottomBorderType", _property.get("bottomBorderType"));
							}
							
							_property = headerListData.get(j-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition));
						}
						else if( k > columnCntAr.get(_addPosition) && k > 0 && pageHeaderItems.get(pageHeaderItems.size()-1) != null && pageHeaderItems.get(_addPosition).size() >= k-columnCntAr.get(_addPosition) && columnList.get(k).equals(columnList.get(k-1)) &&
								( _property.get("text").getStringValue().equals( pageHeaderItems.get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)-1).get("text").getStringValue() ) ||  _property.get("text").getStringValue().toLowerCase().equals(mEmptyText) )
								&& ( j == 0  || headerListData.get(headerListData.size()-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).get("text").getStringValue().toLowerCase().indexOf(mSummeryHeaderText) == -1 ) 
								)
//						else if( k > columnCntAr.get(_addPosition) && k > 0 &&  headerListData.get(headerListData.size()-1).size() > j+1 && pageHeaderItems.get(pageHeaderItems.size()-1) != null && pageHeaderItems.get(_addPosition).size() > k-columnCntAr.get(_addPosition) &&
//						( _property.get("text").getStringValue().equals( pageHeaderItems.get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).get("text").getStringValue() ) ||  _property.get("text").getStringValue().toLowerCase().equals(mEmptyText) )
//						&& ( j == 0  || headerListData.get(headerListData.size()-1).get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)).get("text").getStringValue().toLowerCase().indexOf(mSummeryHeaderText) == -1 ) 
//								)
						{
							_updateWidth =  (_property.get("x").getIntegerValue() - pageHeaderItems.get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)-1).get("x").getIntegerValue() )  + _property.get("width").getIntegerValue();
							pageHeaderItems.get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)-1).put("width", new Value( _updateWidth, "number"));
							
							_property = pageHeaderItems.get(pageHeaderItems.size()-1).get(k-columnCntAr.get(_addPosition)-1);
						}
						
						
//						if( _addPosition >= pageHeaderItems.size() ){
//							pageHeaderItems.add(new ArrayList<HashMap<String,Value>>() );
//						}
						if( useNextPosition ){
								pageHeaderItems.add(new ArrayList<HashMap<String,Value>>() );
						}
						pageHeaderItems.get(pageHeaderItems.size()-1).add(_property);
						
					}
					
					_y = _y + _rowHeight;						// y포지션 업데이트
					
					headerListData.add(pageHeaderItems);		// 생성된 헤더를 List에 애드
					
				}
				
				
			}
			
			
		}
		float lastXP = 0;
		HashMap<String, Value> addItem;
		// 헤더 데이터를 가져와서 title/leftFixedColumn/rightFixedColumn값을 업데이트처리 
		for ( i = 0; i < headerListData.size(); i++) {
			lastXP = 0;
			
			for (j = 0; j < headerListData.get(i).size(); j++) {
				
				if( j > 0 )
				{
					for ( k = 0; k < defaultColumnCnt; k++) {
							
						addItem = (HashMap<String, Value>) headerListData.get(i).get(0).get(k).clone();
						lastXP = lastXP + addItem.get("width").getIntegerValue();
						headerListData.get(i).get(j).add(k, (HashMap<String, Value>) headerListData.get(i).get(0).get(k).clone() );
					}
				}
				
				if( j < headerListData.get(i).size()-1 )
				{	
					int _lastAddPosition = headerListData.get(i).get(headerListData.get(i).size()-1).size() - defaultRightColumnCnt-defaultGrdCnt; 
					lastXP = headerListData.get(i).get(j).get(  headerListData.get(i).get(j).size() - 1 ).get("width").getIntegerValue() + headerListData.get(i).get(j).get(  headerListData.get(i).get(j).size() - 1 ).get("x").getIntegerValue();
					for ( k = 0; k < defaultRightColumnCnt; k++) {
						addItem = (HashMap<String, Value>) headerListData.get(i).get(headerListData.get(i).size()-1).get(_lastAddPosition + k).clone();
						addItem.put("x", new Value( lastXP, "number" ) );
						headerListData.get(i).get(j).add( addItem );
						lastXP = lastXP + addItem.get("width").getIntegerValue();
					}
				}
			}
			
		}
		
		//5. 생성된 Header/Headerlabel/value 를 합쳐셔 페이지별 아이템 리스트 생성 
		
		//   pageItems 아이템의 반복된 순서만큼 아이템을 ADD 
		
		int _currentPosition = 0;
		int _maxPosition = headerListData.get(0).size();
		float _startP = 0f;
		_rowHeight = 0f;
		ArrayList<ArrayList<HashMap<String, Value>>> AllPageItems = new ArrayList<ArrayList<HashMap<String, Value>>>();
		
		int _excelX = 0;
		int _excelWidth = 0;
		
		if( isExportData )
		{
			
			ArrayList<HashMap<String, Value>> _headerTemp = new ArrayList<HashMap<String, Value>>();
			int _onePageCnt = 0;
			
			for ( j = 0; j < headerListData.size(); j++) {
				
				if( headerList.get(j).equals("headerLabel") == false )
				{
					int _subIndex = headerListData.get(j).size() -1;
					_headerTemp = new ArrayList<HashMap<String, Value>>();
					
					for (int l = 0; l < headerListData.get(j).get(_subIndex).size(); l++) {
						_headerTemp.add( headerListData.get(j).get(_subIndex).get(l) );
					}
				
				}
				AllPageItems.add(_headerTemp);
			}
			
			for ( i = 0; i < pageItems.size(); i++) {
				
				for ( j = 0; j < pageItems.get(i).size(); j++) {
					_headerTemp = new ArrayList<HashMap<String, Value>>();
					
					for ( k = 0; k < pageItems.get(i).get(j).size(); k++) {
						_headerTemp.add( pageItems.get(i).get(j).get(k) );
					}
					AllPageItems.add(_headerTemp);
				}
				
			}
			
		}
		else
		{
			
			ArrayList<HashMap<String, Value>> _addItemChkAr = new ArrayList<HashMap<String, Value>>();
			
			for ( i = 0; i < pageItems.size(); i++) {
				
				_addItemChkAr.clear();
				
				ArrayList<HashMap<String, Value>> _subPage = new ArrayList<HashMap<String, Value>>();
				
				if(i < _maxPosition)
				{
					_startP = currentStartPosition;
				}
				else
				{
					_startP = startPosition;
				}
				
				if( _currentPosition == _maxPosition )
				{
					_currentPosition = 0;
				}
				_rowHeight = 0;
				
				for ( j = 0; j < headerListData.size(); j++) {
					if( headerList.size() > 1 && headerLabelValueStyle.size() > 0 )
					{
						if( headerList.get(0).equals("headerLabel") )
						{
							if(j==0)
							{
								_rowHeight = headerLabelValueStyle.get(0).get(0).get("height").getIntegerValue();
							}
							else
							{
								_rowHeight = headerValueStyle.get(j-1).get(0).get("height").getIntegerValue();
							}
						}
						else
						{
							if(j== headerListData.size()-1 )
							{
								_rowHeight = headerLabelValueStyle.get(0).get(0).get("height").getIntegerValue();
							}
							else
							{
								_rowHeight = headerValueStyle.get(j).get(0).get("height").getIntegerValue();
							}
						}
					}
					else
					{
//				_rowHeight = headerListData.get(j).get(_currentPosition).get(0).get("height").getIntegerValue();
						_rowHeight = headerValueStyle.get(j).get(0).get("height").getIntegerValue();
					}
					
					for ( k = 0; k < headerListData.get(j).get(_currentPosition).size(); k++) {
						if( _subPage.contains(headerListData.get(j).get(_currentPosition).get(k)) == false )
						{
							
							//Excel 내보내기시 X배열값을 위하여 X배열을 담아둔다
							_excelX = Math.round(headerListData.get(j).get(_currentPosition).get(k).get("x").getIntegerValue()); 
							_excelWidth = Math.round(headerListData.get(j).get(_currentPosition).get(k).get("width").getIntegerValue()); 
							
							if(mXAr.contains(_excelX) == false)
							{
								mXAr.add(_excelX);
							}
							if(mXAr.contains(_excelX+_excelWidth) == false)
							{
								mXAr.add(_excelX+_excelWidth);
							}
							
							if( k == 0 && infoData.get("rowNum").getBooleanValue() )
							{
								mXAr.add(_excelX + Float.valueOf( mRowNumWidth ).intValue() );
							}
							
							if( j > 0 && headerListData.get(j).get(_currentPosition).get(k).get("text").equals( headerListData.get(j-1).get(_currentPosition).get(k).get("text") ) )
							{
								continue;
							}
							HashMap<String, Value> _tempHeaderMap = (HashMap<String, Value>) headerListData.get(j).get(_currentPosition).get(k).clone();
//						headerListData.get(j).get(_currentPosition).get(k).put("y", Value.fromInteger(_startP));
//						_subPage.add( headerListData.get(j).get(_currentPosition).get(k) );
							
							if( _addItemChkAr.indexOf( headerListData.get(j).get(_currentPosition).get(k) ) == -1)
							{
								_tempHeaderMap.put("y", Value.fromInteger(_startP));
								_subPage.add( _tempHeaderMap );
								
								_addItemChkAr.add( headerListData.get(j).get(_currentPosition).get(k) );
							}
							
						}
					}
					
					_startP = _startP + _rowHeight;
				}
				
				_currentPosition++;
				
				for ( j = 0; j < pageItems.get(i).size(); j++) {
					
					for ( k = 0; k < pageItems.get(i).get(j).size(); k++) {
						
						if( pageItems.get(i).get(j).get(k) != null ) _subPage.add( pageItems.get(i).get(j).get(k) );
						
					}
					
				}
				
				
				AllPageItems.add(_subPage);
			}
			
		}
		
		
		headerListData.clear();
		pageItems.clear();
		
		
		return AllPageItems;
		
	}
	
	
	
	private String crosstabChangeText( String _txt, String crossTabData )
	{
		String returnStr = "";
		
		if( crossTabData.indexOf("&") != -1 && crossTabData.toLowerCase().equals(mEmptyText) == false )
		{
			if( _txt.equals("") == false && String.valueOf(_txt.charAt(0)).equals("@") == false &&  String.valueOf(_txt.charAt(0)).equals("$") == false )
			{
				if( crossTabData.indexOf("_") == -1 )
				{
					returnStr = _txt;
				}
				else if( String.valueOf( crossTabData.charAt(crossTabData.length()-1) ).equals("&") )
				{
					returnStr = crossTabData.substring( crossTabData.indexOf("_")+1 ,  crossTabData.length() - 1 );
				}
				else if( String.valueOf( crossTabData.charAt(0) ).equals("&") && _txt.equals("") == false )
				{
					returnStr = crossTabData.substring( crossTabData.indexOf("_")+1 ,  crossTabData.length() ) + _txt;
				}
				else
				{
//					returnStr = _txt + " " + crossTabData.substring( crossTabData.indexOf("_")+1 ,  crossTabData.length() - 1 );
					returnStr = crossTabData.substring( 1 ,  crossTabData.length());
				}
			}
			else
			{
				if( String.valueOf( crossTabData.charAt(crossTabData.length()-1) ).equals("&") )
				{
					returnStr = crossTabData.substring( crossTabData.indexOf("_")+1 ,  crossTabData.length() - 1 );
				}
				else
				{
					returnStr = crossTabData.substring( 1 ,  crossTabData.length() );
				}
				
			}
			
			
		}
		else
		{
			if( UBIPivotParser.isNumber(crossTabData) && crossTabData.indexOf(".") > -1 )
			{
				
				if( Math.floor( Double.parseDouble(crossTabData)) == Double.parseDouble(crossTabData) )
				{
					returnStr = crossTabData.substring(0,crossTabData.indexOf("."));
				}
				else
				{
					returnStr = crossTabData;
				}
				
			}
			else
			{
				returnStr = crossTabData;
			}
			
		}
		
		
		return returnStr;
	}
	
	
	private Float getHeaderHeight( HashMap<String, Value> crossObj )
	{
		ArrayList<ArrayList<HashMap<String, Value>>> headerValueStyleAr = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("headerValueStyle");
		ArrayList<HashMap<String, Value>> headerValueInfoAr = (ArrayList<HashMap<String, Value>>) crossObj.get("itemStyle").getMapValue().get("headerInfoStyle");
		// 헤더의 높이를 체크
		
		int _page = 0;
		int i = 0;
		int max = 0;
		int _rowNumSum = 0;
		float _defaultHeight = 0f;
		float _sPosition = 0f;
		float _currentHeight = 0f;
		float _currentYposition = 0f;
		float _lPosition = 0f;
		
		for ( i = 0; i < headerValueStyleAr.size(); i++) {
			if( headerValueInfoAr.get(i).containsKey("headerVisible") && headerValueInfoAr.get(i).get("headerVisible").getBooleanValue() == false )
			{
				continue;
			}
			
			if(headerValueStyleAr.get(i).get(0).containsKey("height") )
			{
				_defaultHeight = _defaultHeight + headerValueStyleAr.get(i).get(0).get("height").getIntegerValue();
			}
			
		}
		
		headerValueStyleAr = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("headerLabelValueStyle");
		
		for ( i = 0; i < headerValueStyleAr.size(); i++) {
			if(headerValueStyleAr.get(i).get(0).containsKey("height") )
			{
				_defaultHeight = _defaultHeight + headerValueStyleAr.get(i).get(0).get("height").getIntegerValue();
			}
		}
		
		
		return _defaultHeight;
	}
	
	
	
	/**
	 * functionName	:	columnRepeatFlag</br>
	 * desc			:	컬럼위 row병합여부를 판단하여 true/false리턴 
	 * @param _row
	 * @param _column
	 * @param _argo
	 * @param str
	 * @param arr
	 * @return
	 */
	private Boolean columnRepeatFlag( int _row, int _column, String _argo, String str, ArrayList<ArrayList<String>> arr )
	{
		
		Boolean retFlag = false;
		int i = 0;
		
		if( _row > 0 && _argo.equals(str))
		{
			
			if( _column == 0 )
			{
				retFlag = true;
			}
			else
			{
				i = _column;
				for ( i = 0; i >= 0; i--) {
					
					if( arr.get(_row-1).get(i).toLowerCase().equals(mSummeryValueText) || arr.get(_row-1).get(i).equals( arr.get(_row).get(i) ) ||  arr.get(_row).get(i).toLowerCase().indexOf(mEmptyText) != -1  )
					{
						retFlag = true;
					}
					else
					{
						retFlag = false;
						break;
					}
					
				}
			}
			
		}
		
		return retFlag;
	}

	private Boolean valueRepeatFlag( int _row, int _column, String _argo, String str, ArrayList<ArrayList<String>> arr )
	{
		
		Boolean retFlag = false;
		int i = 0;
		
		if( _row > 0 && _argo.equals(str))
		{
			
			if( arr.get(_row).get(0).toLowerCase().indexOf(mSummeryValueText) == -1 && arr.get(_row).get(0).toLowerCase().indexOf(mGrandValueText) == -1 && arr.get(_row-1).get(_column).equals( arr.get(_row).get(_column) ) )
			{
				retFlag = true;
			}
			else
			{
				retFlag = false;
			}
		}
		
		return retFlag;
	}
	
	
	/**
	 * functionName	:	titleMergeFlag</br>
	 * desc			:	title 영역의 병합 처리 여부 판단
	 * @param _row
	 * @param _column
	 * @param _argo
	 * @param str
	 * @param arr
	 * @return
	 */
	private Boolean titleMergeFlag( int _row, int _column, String _argo, String str, ArrayList<ArrayList<String>> arr )
	{
		Boolean retFlag = false;
		int i = 0;
		
		if( _argo.equals(str)  )
		{
			
			if(_column == 0 )
			{
				retFlag = true;
			}
			else
			{
				
				i = _row;
				
				for ( i = 0; i >= 0 ; i--) {
					
					if( arr.get(i).get(_column -1 ).equals( arr.get(i).get(_column) ) || arr.get(i).get(_column).toLowerCase().indexOf(mEmptyText) != -1
							|| checkHeaderItemType( arr.get(i).get(_column -1 ), arr.get(i).get(_column)) )
					{
						retFlag = true;
					}
					else
					{
						retFlag = false;
						break;
					}
					
				}
				
			}
			
		}
		
		
		return retFlag;
	}
	
	
	
	private Boolean checkHeaderItemType( String _argoHeader, String _header)
	{
		
		if( String.valueOf( _header.charAt(0) ).equals("&") == false )
		{
			return false;
		}
		else if( _argoHeader.toLowerCase().indexOf(mTitleText) != -1 && _header.toLowerCase().indexOf(mTitleText) != -1 )
		{
			return true;
		}
		else if( _argoHeader.toLowerCase().indexOf(mLeftFixedText) != -1 && _header.toLowerCase().indexOf(mLeftFixedText) != -1 )
		{
			return true;
		}
		else if( _argoHeader.toLowerCase().indexOf(mRightFixedText) != -1 && _header.toLowerCase().indexOf(mRightFixedText) != -1 )
		{
			return true;
		}
		
		return false;
	}
	
	
	
	private HashMap<String, Object> crossTabItemStyleChanger( HashMap<String, Value> crossObj, String style, int rowIndex, int colIndex, int lFixIndex, int rFixed, int valueColIndex )
	{
		ArrayList<String> columnList = (ArrayList<String>) crossObj.get("columnLists").getArrayStringValue();
		
		int _rowNumIdx = 0;
		
		String columnStr = crossObj.get("columnLists").getArrayStringValue().get(colIndex);
		HashMap<String, Value> retProperty;
		String valueTxt = "";
		HashMap<String, Object> retHahsMap = new HashMap<String, Object>();
		float itemHeight = 0f;
		int _lfixedIndex = columnList.indexOf("lfixed");
		int _rfixedIndex = columnList.indexOf("rfixed");
		HashMap<String, Value> _infoMap = (HashMap<String, Value>) crossObj.get("info").getMapValue();
		if( _infoMap.get("rowNum").getBooleanValue() )
		{
			_rowNumIdx = 1;
		}
		
		ArrayList<ArrayList<HashMap<String, Value>>> Styles;
		if( style.equals("summery") )
		{
			if( columnStr.equals("sum") ) 
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("summerySumStyle");
//				retProperty = Styles.get(rowIndex).get(0 + _rowNumIdx);
				retProperty = Styles.get(rowIndex).get(Styles.get(0).size()-1);
			}
			else if( columnStr.equals("title") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("summeryColumnStyle");
				retProperty = Styles.get(rowIndex).get(colIndex);
			}
			else if( columnStr.equals("lfixed") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("summeryLfcStyle");
				retProperty = Styles.get(rowIndex).get(colIndex-_lfixedIndex);
			}
			else if( columnStr.equals("rfixed") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("summeryRfcStyle");
				retProperty = Styles.get(rowIndex).get(colIndex-_rfixedIndex);
			}
			else if( columnStr.equals("grand") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("summeryGrdTotStyle");
				retProperty = Styles.get(0).get(Styles.get(0).size()-1);
			}
			else
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("summeryValueStyle");
				retProperty = Styles.get(rowIndex).get(valueColIndex);
			}
			
			Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("summeryValueStyle");
			itemHeight = Styles.get(rowIndex).get(0).get("height").getIntegerValue();
			
		}
		else if( style.equals("grand") )
		{
			if( columnStr.equals("sum") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("grdTotSumStyle");
//				retProperty = Styles.get(0).get(0 + _rowNumIdx);
				retProperty = Styles.get(0).get(Styles.get(0).size()-1);
			}
			else if( columnStr.equals("title") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("grdTotColumnStyle");
				retProperty = Styles.get(0).get(colIndex);
			}
			else if( columnStr.equals("lfixed") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("grdTotLfcStyle");
				retProperty = Styles.get(rowIndex).get(colIndex-_lfixedIndex);
			}
			else if( columnStr.equals("rfixed") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("grdTotRfcStyle");
				retProperty = Styles.get(rowIndex).get(colIndex-_rfixedIndex);
			}
			else if( columnStr.equals("grand") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("grdTotGrdTotStyle");
				retProperty = Styles.get(0).get(Styles.get(0).size()-1);
			}
			else
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("grdTotValueStyle");
				retProperty = Styles.get(0).get(valueColIndex);
			}
			
			Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("grdTotValueStyle");
			itemHeight = Styles.get(0).get(0).get("height").getIntegerValue();
			
		}
		else
		{
			
			if( columnStr.equals("sum") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("valueSumStyle");
//				retProperty = Styles.get(0).get(0 + _rowNumIdx);
				retProperty = Styles.get(0).get(Styles.get(0).size()-1);
			}
			else if( columnStr.equals("title") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("valueColumnStyle");
				retProperty = Styles.get(0).get(colIndex);
			}
			else if( columnStr.equals("lfixed") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("valueLfcStyle");
				retProperty = Styles.get(rowIndex).get(colIndex-_lfixedIndex);
			}
			else if( columnStr.equals("rfixed") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("valueRfcStyle");
				retProperty = Styles.get(rowIndex).get(colIndex-_rfixedIndex);
			}
			else if( columnStr.equals("grand") )
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("valueGrdTotStyle");
				retProperty = Styles.get(0).get(Styles.get(0).size()-1);
			}
			else
			{
				Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("valueValueStyle");
				retProperty = Styles.get(0).get(valueColIndex);
			}
			
			Styles = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("valueValueStyle");
			itemHeight = Styles.get(0).get(0).get("height").getIntegerValue();
		}
		
		retHahsMap.put("style", retProperty.clone());
		retHahsMap.put("height", itemHeight);

		if( retProperty != null &&  retProperty.containsKey("text"))
		{
			retHahsMap.put("valueTxt", retProperty.get("text").getStringValue() );
		}
		else
		{
			retHahsMap.put("valueTxt", "" );
		}
		
		
		return retHahsMap;
	}
	
	
	
	
	/***
	 * 미사용 처리
	 * functionName	:	makeCurrentPageToYposition</br>
	 * desc			:	총 페이지의 시작 지접과 페이지별 아이템의 갯수 그리고 아이템의 높일르 담아둔 배열을 작성
	 * @param startPosition
	 * @param maxPosition
	 * @param crossObj
	 * @param currentStartPosition
	 */
	private void makeCurrentPageToYposition( Float startPosition, Float maxPosition, HashMap<String, Value> crossObj, Float currentStartPosition  )
	{
		ArrayList<ArrayList<String>> rowData = new ArrayList<ArrayList<String>>();

		int _page = 0;
		int i = 0;
		int max = 0;
		int _rowNumSum = 0;
		float _defaultHeight = 0f;
		float _sPosition = 0f;
		float _currentHeight = 0f;
		float _currentYposition = 0f;
		float _lPosition = 0f;
		String _str = "";
		ArrayList<HashMap<String, Float>> _ypositonAr = new ArrayList<HashMap<String,Float>>();
		HashMap<String, Float> rowHashMap;
		
		
		ArrayList<HashMap<String, Value>> headerValueStyleAr = (ArrayList<HashMap<String, Value>>) crossObj.get("itemStyle").getMapValue().get("headerValueStyle");
		ArrayList<HashMap<String, Value>> headerValueInfoAr = (ArrayList<HashMap<String, Value>>) crossObj.get("itemStyle").getMapValue().get("headerInfoStyle");
		// 헤더의 높이를 체크
		
		rowData = (ArrayList<ArrayList<String>>) crossObj.get("pageInfo").getMapValue().get("DATA");
		max = rowData.size();
		
		
		for ( i = 0; i < headerValueStyleAr.size(); i++) {
			if( headerValueInfoAr.get(i).containsKey("headerVisible") && headerValueInfoAr.get(i).get("headerVisible").getBooleanValue() == false )
			{
				continue;
			}
			
			if(headerValueStyleAr.get(i).containsKey("height") )
			{
				_defaultHeight = _defaultHeight + headerValueStyleAr.get(i).get("height").getIntegerValue();
			}
			
		}
		
		headerValueStyleAr = (ArrayList<HashMap<String, Value>>) crossObj.get("itemStyle").getMapValue().get("headerLabelValueStyle");
		
		for ( i = 0; i < headerValueStyleAr.size(); i++) {
			if(headerValueStyleAr.get(i).containsKey("height") )
			{
				_defaultHeight = _defaultHeight + headerValueStyleAr.get(i).get("height").getIntegerValue();
			}
		}
		
		_sPosition = _sPosition + _defaultHeight + currentStartPosition;
		
		ArrayList<ArrayList<HashMap<String, Value>>> styleAr;
		// DATA의 Row수를 가져와서 총 페이지수와 페이지별 아이템의 Row수를 담기
		for ( i = 0; i < max; i++) {
			
			_str = rowData.get(i).get(0);
			
			if( _str.toLowerCase().indexOf(mSummeryValueText) != -1 )
			{
				_rowNumSum = getSummeryColumn(rowData.get(i));
				styleAr = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("summeryValueStyle");
				_currentHeight = styleAr.get(_rowNumSum).get(0).get("height").getIntegerValue();
				
			}
			else if( _str.toLowerCase().indexOf(mGrandValueText) != -1 )
			{
				styleAr = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("grdTotValueStyle");
				_currentHeight = styleAr.get(0).get(0).get("height").getIntegerValue();
			}
			else
			{
				styleAr = (ArrayList<ArrayList<HashMap<String, Value>>>) crossObj.get("itemStyle").getMapValue().get("valueValueStyle");
				_currentHeight = styleAr.get(0).get(0).get("height").getIntegerValue();
			}
			
			if( (_sPosition + _currentHeight ) >  _lPosition )
			{
				_sPosition = startPosition + _defaultHeight + _currentHeight;
				_currentYposition = startPosition + _defaultHeight;
				_page = _page + 1;
			}
			else
			{
				_currentYposition = _sPosition;
				_sPosition = _sPosition + _currentHeight;
			}
			
			rowHashMap = new HashMap<String, Float>();
			rowHashMap.put("y", _currentYposition);
			rowHashMap.put("height", _currentHeight);
			_ypositonAr.add(rowHashMap);
			
		}
		
		_currentYposition = _currentYposition + _currentHeight;
		
		
		HashMap<String, Value> infoData = (HashMap<String, Value>) crossObj.get("info").getMapValue();
		
		
	}
	
	
	
	private Integer getSummeryColumn( ArrayList<String> arr)
	{
		String _str = "";
		
		for (int i = 0; i < arr.size(); i++) {
			
			_str = arr.get(i).toLowerCase();
			
			if( _str.indexOf(mSummeryValueText) != -1 && _str.indexOf("_") != -1 )
			{
				return i;
			}
//			else if( _str.indexOf(mSummeryValueText) != -1 )
//			{
//				return 0;
//			}
			else if( _str.indexOf(mSummeryValueText) == -1 )
			{
				return 0;
			}
		}
		
		
		return 0;
	}
	
	private Element convertUBFX( HashMap<String, Value> _property, ArrayList<String> rowData, ArrayList<String> _rowCols, int _colIdx, ArrayList<ArrayList<String>> headerData, ArrayList<String> colData )
	{
		//@ubfx 처리
		if( _property.containsKey("Element") && _property.get("Element") != null )
		{
			Element _ubfxElement = _property.get("Element").getElementValue();
			Element _propertyElement = null;
			String _ubfxString = "";
			NodeList _propertys = null;
			
			Element _cloneElement = (Element) _ubfxElement.cloneNode(true);
			
			_propertys = _cloneElement.getElementsByTagName("ubfunction");
			int propertyLength = _propertys.getLength();
			for (int  _j = 0; _j < propertyLength; _j++) {
				
				_propertyElement = (Element) _propertys.item(_j);
				try {
					_ubfxString = URLDecoder.decode( _propertyElement.getAttribute("value"),"UTF-8" );
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					_ubfxString = _propertyElement.getAttribute("value");
				}
				
				String _checkStr = ",+-=()*/<> ;";
				
				
				String _chkUbfxString = ubfxUseDataSet( _ubfxString, _rowCols, rowData, _colIdx, headerData, colData );
//				
				while( _ubfxString.equals(_chkUbfxString) == false )
				{
					_ubfxString = _chkUbfxString;
					
					_chkUbfxString = ubfxUseDataSet(_ubfxString, _rowCols, rowData, _colIdx, headerData, colData );
				}
				
				if( _ubfxString.contains( THIS_TEXT ) )
				{
					if( FUNCTION_VERSION.equals("2.0"))
					{
						_ubfxString = _ubfxString.replaceAll( THIS_TEXT, "\"" + _property.get("text").getStringValue() + "\"" );
					}
					else
					{
						_ubfxString = _ubfxString.replaceAll( THIS_TEXT, _property.get("text").getStringValue() );
					}
				}
				
				/**
				while( _ubfxString.contains(UBFX_COL) )
				{
					int _st = _ubfxString.indexOf(UBFX_COL);
					int _lastInt = 0;
					String _lastChar = "";
					for (int l = _st+UBFX_COL.length() ; l < _ubfxString.length()+1; l++) {
						_lastChar = "";		//_ubfxString.substring(l-1, l) )
						if( _checkStr.indexOf(  String.valueOf(_ubfxString.charAt(l-1)) ) != -1 )
						{
							_lastInt = l-1;
							_lastChar = _ubfxString.substring(l-1, l);
							break;
						}
						else
						{
							_lastInt = _ubfxString.length();
						}
					}
					
					
					String _targetStr = _ubfxString.substring(_st+UBFX_COL.length(), _lastInt);
					String _convertText = "";
					int _targetIndex = 0;
					_targetIndex = _rowCols.indexOf( UBFX_COL + _targetStr );
					
					if( _targetIndex != -1 )
					{
						_convertText = rowData.get( _targetIndex );
					}
					else if( StringUtil.isInteger(_targetStr) )
					{
						_convertText = rowData.get( Integer.parseInt(_targetStr) -1 );
					}
					else
					{
						_convertText = _convertText + _lastChar;
					}
					
					_ubfxString = _ubfxString.replace( UBFX_COL+_targetStr+_lastChar,_convertText + _lastChar );
					
				}
				*/
				
				try {
					_ubfxString = URLEncoder.encode( _ubfxString, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				_propertyElement.setAttribute("value", _ubfxString );
				
			}
			
			
			if( propertyLength > 0 )
			{
				return _cloneElement;
				//_property.put("Element", new Value( _cloneElement, "element"));
			}
			
		}
		
		return null;
	}
	
	private String ubfxUseDataSet( String _ubfxStr, ArrayList<String> _colAr, ArrayList<String> _rowData, int _colIdx, ArrayList<ArrayList<String>> headerData, ArrayList<String> colData)
	{
		String _checkStr = ",+-=()*/<> ;";
		String _returnStr = "";
		for (int i = 0; i < _colAr.size(); i++) {
			
			int _index = _ubfxStr.indexOf(_colAr.get(i));
			
			if( _index > -1)
			{
				for (int j = _index; j < _ubfxStr.length(); j++) {
					
					if( _checkStr.indexOf(  String.valueOf(_ubfxStr.charAt(j)) ) != -1 || String.valueOf(_ubfxStr.charAt(j)).equals("\n") )
					{
						_returnStr = _ubfxStr.substring(_index,j);
						
						if( _returnStr.equals("") == false )
						{
							if( FUNCTION_VERSION.equals("2.0"))
							{
								_ubfxStr = _ubfxStr.replaceAll( _returnStr, "\"" + _rowData.get(i) + "\"" );
							}
							else
							{
								_ubfxStr = _ubfxStr.replaceAll( _returnStr, _rowData.get(i) );
							}
//							_ubfxStr = _ubfxStr.replaceAll(_returnStr, _rowData.get(i));
						}
						break;
					}
					
				}
			}
		}
		
		for (int i = 0; i < colData.size(); i++) {
			
			int _index = _ubfxStr.indexOf(colData.get(i));
			int _columnIndex = -1;
			
			if( _index > -1)
			{
				for (int j = _index; j < _ubfxStr.length(); j++) {
					
					if( _checkStr.indexOf(  String.valueOf(_ubfxStr.charAt(j)) ) != -1 )
					{
						_returnStr = _ubfxStr.substring(_index,j);
						
						if( _returnStr.equals("") == false )
						{
							_columnIndex = colData.indexOf(_returnStr.trim());
							
							if( _columnIndex > -1 && headerData.size() > _columnIndex &&  headerData.get( _columnIndex ).size() > _colIdx && j >= _colAr.size()  )
							{
								if( FUNCTION_VERSION.equals("2.0"))
								{
									_ubfxStr = _ubfxStr.replaceAll( _returnStr, "\"" + headerData.get( _columnIndex ).get(_colIdx) + "\"" );
								}
								else
								{
									_ubfxStr = _ubfxStr.replaceAll( _returnStr, headerData.get( _columnIndex ).get(_colIdx) );
								}
							}
						}
						break;
					}
					
				}
			}
		}

		return _ubfxStr;
	}
	
	
	///////////////////////JSON 타입을 로드 
	/**
	 * functionName :	convertCrossTabXmlToItem</br>
	 * desc			:	크로스탭의 정보를 가지고있는 xml을 크로스탭 object로 변환 
	 * @return
	 * @throws XPathExpressionException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	*/
	public ArrayList<ArrayList<HashMap<String, Value>>> convertCrossTabObjecttoItem( HashMap<String, Object> _pageObj, HashMap<String, ArrayList<HashMap<String, Object>>> dataSet, Float pageWidth, Float startPosition, Float maxPosition, Float currentStartPosition, ArrayList<Integer> mXAr )throws IOException, XPathExpressionException, SAXException, ParserConfigurationException
	{
		
		String _itemId = "";
		HashMap<String, Value> _infoObj = new HashMap<String, Value>();		// 크로스탭의 기본 정보가 담긴 HashMap 데이터
		HashMap<String, ArrayList<String>> _arrayListData;
		HashMap<String, Value> _crossTabMap = new HashMap<String, Value>();
		
		ArrayList<String> _colAr = new ArrayList<String>();		// Col 정보
		ArrayList<String> _rowAr = new ArrayList<String>();		// Row 정보
		ArrayList<String> _valAr = new ArrayList<String>();		// Val 정보 
		Element _childItem;
		ArrayList<String> _leftFixedColumnAr 	= new ArrayList<String>();
		ArrayList<String> _rightFixedColumnAr 	= new ArrayList<String>();
		Boolean _headerLabelVisible = false;
		
		_itemId =  _pageObj.get("id").toString();
		DataSet = dataSet;
		
		String _value = "";
		String _keyStr = "";

		JSONObject crossTabData = null;
		JSONObject crossTabItem = null;
		Object _addItem;
		
		for( Object _key : _pageObj.keySet() )
		{
			_keyStr = _key.toString();
			if( _pageObj.get(_keyStr) instanceof String)
			{
				_addItem =  URLDecoder.decode( _pageObj.get(_keyStr).toString(), "UTF-8" );
				if( _addItem.equals("null") ) _addItem = null;
			}
			else
			{
				_addItem = _pageObj.get(_key);
			}
			if( _addItem == null ) continue;
			
			if( _pageObj.get(_key) != null && !_pageObj.get(_key).equals("") )
			{
				if( _keyStr.equals("rightFixedColumn") || _keyStr.equals("leftFixedColumn") )
				{
					if( !_addItem.equals("null") && !_addItem.equals("") ) _infoObj.put( _keyStr, new Value( new ArrayList<String>(Arrays.asList(  _addItem.toString().split(",") ) ), "arraystr" ) );
				}
				else if( _keyStr.equals("crossTabData")  )
				{
					crossTabData = (JSONObject) _pageObj.get("crossTabData");
				}
				else if( _keyStr.equals("crossTab")  )
				{
					crossTabItem = (JSONObject) _pageObj.get("crossTab");
				}
				else
				{
					if( _addItem instanceof String && ( ((String) _addItem).startsWith("[") || ((String) _addItem).startsWith("}")  ) )
					{
						String _type = "ArrayCollection";
						if( ( (String) _addItem).startsWith("}") ) _type = "map";
						_infoObj.put(_keyStr, new Value( _addItem , _type ) );
					}
					else
					{
						_infoObj.put(_keyStr, new Value( _addItem ) );
					}
				}
			}
			
		}
		
		// 데이터셋 정보 담아두기
		if( _infoObj.get("rowNum") != null && _infoObj.get("rowNum").getBooleanValue() ){
			_rowAr.add("$No");
		}
		
		
		//info 정보에 version 담기
		String _crossTabVersion = CROSSTAB_VERSION_1;
		if( _infoObj.containsKey("version"))
		{
			_crossTabVersion = _infoObj.get("version").getStringValue();
		}
		
		JSONArray _jrr;
		
		for(  Object _key : crossTabData.keySet() )
		{
			_keyStr = _key.toString();
			_jrr = (JSONArray) crossTabData.get(_key);
			
			for( int i = 0; i < _jrr.size(); i++ )
			{
				_value = URLDecoder.decode( _jrr.get(i).toString(), "UTF-8" ) ;
				if( _keyStr.equals("value"))
				{
					_valAr.add(_value);
				}
				else if( _keyStr.equals("column") )
				{
					_colAr.add(_value);
				}
				else if( _keyStr.equals("row") )
				{
					_rowAr.add(_value);
				}
				
			}
			
		}
		
		// SummaryVisible 담기
		if( _infoObj.containsKey("summaryVisible") ){
			_crossTabMap.put("summaryVisible", new Value( _infoObj.get("summaryVisible").getBooleanValue(), "boolean" ));
		}else{
			_crossTabMap.put("summaryVisible", new Value( true, "boolean" ));
		}
		
		// SummaryType 담기
		if( _infoObj.containsKey("summaryType") ){
			_crossTabMap.put("summaryType", new Value( _infoObj.get("summaryType").getStringValue(), "string" ));
		}else{
			_crossTabMap.put("summaryType", new Value( GlobalVariableData.GRANDSUMMARY_TYPE_ALL, "string" ));
		}
		
		// grandSummaryVisible 담기
		if( _infoObj.containsKey("grandSummaryVisible") ){
			_crossTabMap.put("grandSummaryVisible", new Value( _infoObj.get("grandSummaryVisible").getBooleanValue(), "boolean" ));
		}else{
			_crossTabMap.put("grandSummaryVisible", new Value( true, "boolean" ));
		}
		
		// grandSummaryType 담기
		if( _infoObj.containsKey("grandSummaryType") ){
			_crossTabMap.put("grandSummaryType", new Value( _infoObj.get("grandSummaryType").getStringValue(), "string" ));
		}else{
			_crossTabMap.put("grandSummaryType", new Value( GlobalVariableData.GRANDSUMMARY_TYPE_ALL, "string" ));
		}
		
		// LeftFixed Column값 저장
		if( _infoObj.containsKey("leftFixedColumn") && !_infoObj.get("leftFixedColumn").getStringValue().equals("") ){
			_leftFixedColumnAr = _infoObj.get("leftFixedColumn").getArrayStringValue();
		}

		// RightFixed Column값 저장
		if( _infoObj.containsKey("rightFixedColumn")&& !_infoObj.get("rightFixedColumn").getStringValue().equals("") ){
			_rightFixedColumnAr =  _infoObj.get("rightFixedColumn").getArrayStringValue();
		}
		
		if( _infoObj.containsKey("headerLabelVisible") && _infoObj.get("headerLabelVisible").getBooleanValue() == true ){
			_headerLabelVisible = _infoObj.get("headerLabelVisible").getBooleanValue();
		}
		
		
		// isExportData가 true일경우 info속성의 newPage속성값을 강제로 false 로 지정해야함
		if(isExportData) _infoObj.put("newPage", Value.fromBoolean(false) );
		
		//컬럼별 합계를 위한 정보를 담아서 DataParser시 화면에 표시하도록 처리
		_crossTabMap.put("info",new Value(_infoObj,"map"));
		_crossTabMap.put("FILE_LOAD_TYPE", new Value(GlobalVariableData.M_FILE_LOAD_TYPE_JSON,"string"));
		
		// crossTab의 버전 담기
		_crossTabMap.put("VERSION",new Value(_crossTabVersion,"string"));
		
		HashMap<String, Value> _arrayList = new HashMap<String, Value>();
		_arrayList.put("COL", new Value(_colAr, "array"));
		_arrayList.put("ROW", new Value(_rowAr, "array"));
		_arrayList.put("VAL", new Value(_valAr, "array"));
		_crossTabMap.put("arrayList", new Value(_arrayList, "map") );
		
		String _rowNumTitle = "";
		
		if( _infoObj.containsKey("rowNumTitle") && _infoObj.get("rowNumTitle").getStringValue().equals("")==false)
		{
			_rowNumTitle = _infoObj.get("rowNumTitle").getStringValue();
		}
		
		// CrossTab의 각 타입별 스타일 담기
		HashMap<String, Object> styleMap = makeCrossTabItemStyle( (JSONObject) _pageObj.get("crossTab"), _colAr.size(), _rowAr.size(), _valAr.size(), _leftFixedColumnAr, _rightFixedColumnAr, _headerLabelVisible, _infoObj.get("rowNum").getBooleanValue() , _itemId, _rowNumTitle );
		
		_crossTabMap.put("itemStyle", new Value(styleMap, "object") );
		
		_crossTabMap = convertCrossTabData(_crossTabMap, _colAr, _rowAr, _valAr );
		
		if(_crossTabMap == null ) return null;
		
		ArrayList<ArrayList<HashMap<String, Value>>> allPageDatas  = continueBandCrossTabPage(_crossTabMap, pageWidth, startPosition, maxPosition, currentStartPosition, mXAr);
		return allPageDatas;
	}

	private HashMap<String, Object> makeCrossTabItemStyle( JSONObject _itemMap , Integer _colLength, Integer _rowLength, Integer _valueLength, ArrayList<String> _lAr, ArrayList<String> _rAr, Boolean _headerLabelFlag, Boolean _rowNumFlag, String _tblID, String _rowNumTitle ) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException{
		
		
		HashMap<String, Object> retCrossTabMap = new HashMap<String, Object>();
		HashMap<String, Object> retMap;
		// 각각의 xml별로 처리 ( header, headerLabel, data, surmmary )
		
		ArrayList<HashMap<String, Object>> _itemArr = null;
		
		String _noTitleText = _rowNumTitle;
		
		_itemArr = (ArrayList<HashMap<String, Object>>)  _itemMap.get("header");
		
		retMap = makeBandStyleToObject(_itemArr, _colLength, _rowLength, false, false, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, _noTitleText ); 
		
		retCrossTabMap.put("headerColumnStyle", 	retMap.get("COL") );
		retCrossTabMap.put("headerValueStyle", 		retMap.get("VALUE") );
		retCrossTabMap.put("headerSumStyle", 		retMap.get("SUM") );
		retCrossTabMap.put("headerGrdTotStyle", 	retMap.get("GRD") );
		retCrossTabMap.put("headerLfcStyle", 		retMap.get("LFC") );
		retCrossTabMap.put("headerRfcStyle", 		retMap.get("RFC") );
		
		retCrossTabMap.put("headerInfoStyle", 		retMap.get("BAND") );
		
		// 헤더 라벨의 속성 담기
		if( _headerLabelFlag && _itemMap.containsKey("headerLabel") && ((ArrayList<HashMap<String, Object>>)_itemMap.get("headerLabel")).size()  > 0 ){
			
			_itemArr = (ArrayList<HashMap<String, Object>>) _itemMap.get("headerLabel");
			retMap = makeBandStyleToObject(_itemArr, _colLength, _rowLength, false, false, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, _noTitleText );
			
			retCrossTabMap.put("headerLabelColumnStyle", 	retMap.get("COL") );
			retCrossTabMap.put("headerLabelValueStyle", 	retMap.get("VALUE") );
			retCrossTabMap.put("headerLabelSumStyle", 		retMap.get("SUM") );
			retCrossTabMap.put("headerLabelGrdTotStyle", 	retMap.get("GRD") );
			retCrossTabMap.put("headerLabelLfcStyle", 		retMap.get("LFC") );
			retCrossTabMap.put("headerLabelRfcStyle", 		retMap.get("RFC") );
		}else{
			
			retCrossTabMap.put("headerLabelColumnStyle", 	new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelValueStyle", 		new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelSumStyle", 		new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelGrdTotStyle", 	new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelLfcStyle", 		new ArrayList<Object>() );
			retCrossTabMap.put("headerLabelRfcStyle", 		new ArrayList<Object>() );
			
		}
		
		_itemArr = (ArrayList<HashMap<String, Object>>) _itemMap.get("data");
		retMap = makeBandStyleToObject(_itemArr, _colLength, _rowLength, false, false, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, "" );
		
		retCrossTabMap.put("valueColumnStyle", 	retMap.get("COL") );
		retCrossTabMap.put("valueValueStyle", 	retMap.get("VALUE") );
		retCrossTabMap.put("valueSumStyle", 	retMap.get("SUM") );
		retCrossTabMap.put("valueGrdTotStyle", 	retMap.get("GRD") );
		retCrossTabMap.put("valueLfcStyle", 	retMap.get("LFC") );
		retCrossTabMap.put("valueRfcStyle", 	retMap.get("RFC") );
		
		
		_itemArr = (ArrayList<HashMap<String, Object>>) _itemMap.get("surmmary");
		retMap = makeBandStyleToObject(_itemArr, _colLength, _rowLength, true, true, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, "" );
		
		retCrossTabMap.put("summeryColumnStyle", 	retMap.get("COL") );
		retCrossTabMap.put("summeryValueStyle", 	retMap.get("VALUE") );
		retCrossTabMap.put("summerySumStyle", 		retMap.get("SUM") );
		retCrossTabMap.put("summeryGrdTotStyle", 	retMap.get("GRD") );
		retCrossTabMap.put("summeryLfcStyle", 		retMap.get("LFC") );
		retCrossTabMap.put("summeryRfcStyle", 		retMap.get("RFC") );
		
		
		//grd tag처리 
		ArrayList<HashMap<String, Object>> _grdSummeryInfo = new ArrayList<HashMap<String, Object>>();
		_grdSummeryInfo.add( _itemArr.get(_itemArr.size()-1) );
		
		retMap = makeBandStyleToObject(_grdSummeryInfo, _colLength, _rowLength, false, false, _valueLength, _lAr, _rAr, _headerLabelFlag, _rowNumFlag, _tblID, "" );
		
		retCrossTabMap.put("grdTotColumnStyle", 	retMap.get("COL") );
		retCrossTabMap.put("grdTotValueStyle", 		retMap.get("VALUE") );
		retCrossTabMap.put("grdTotSumStyle", 		retMap.get("SUM") );
		retCrossTabMap.put("grdTotGrdTotStyle", 	retMap.get("GRD") );
		retCrossTabMap.put("grdTotLfcStyle", 		retMap.get("LFC") );
		retCrossTabMap.put("grdTotRfcStyle", 		retMap.get("RFC") );
		
		return retCrossTabMap;
	}
	
	
	
	private HashMap<String, Object> makeBandStyleToObject( ArrayList<HashMap<String, Object>> _bandItemList, Integer _colLength, Integer _rowLength, Boolean _lastsFlag, Boolean _reverse, Integer _valueLength, ArrayList<String> _lAr, ArrayList<String> _rAr, Boolean _headerLabelFlag, Boolean _rowNumFlag, String _tblID, String _noTitle ) throws XPathExpressionException{
		
		ArrayList<String> _lftAr = new ArrayList<String>();
		ArrayList<String> _rftAr = new ArrayList<String>();
		
		HashMap<String, Object> _retMap = new HashMap<String, Object>();
		
		Integer _lastNum = 0;
		Integer i = 0;
		Integer j = 0;
		Integer k = 0;
		Integer n = 0;
		
		if( _rowNumFlag ) _rowLength = _rowLength -1;
		
		if( _lAr != null ) _lftAr = _lAr;
		if( _rAr != null ) _rftAr = _rAr;
		
		if( _lastsFlag )
		{
			_lastNum = _bandItemList.size()-1;
		}
		else
		{
			_lastNum = _bandItemList.size();
		}
		
		ArrayList<ArrayList<HashMap<String, Value>>> _columnStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _valueStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _summeryStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _grandTotStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _leftFixedStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		ArrayList<ArrayList<HashMap<String, Value>>> _rightFixedStyle = new ArrayList<ArrayList<HashMap<String, Value>>>();
		
		
		//
		ArrayList<HashMap<String, Value> > _bandAr = new ArrayList<HashMap<String,Value>>();
		ArrayList<HashMap<String, Value>> _colAr;
		ArrayList<HashMap<String, Value>> _totAr;
		ArrayList<HashMap<String, Value>> _grdToAr;
		ArrayList<HashMap<String, Value>> _valueAr;
		ArrayList<HashMap<String, Value>> _leftFAr;
		ArrayList<HashMap<String, Value>> _rightFAr;
		
		HashMap<String, Value> _property;
		
		HashMap<String, Object> BandItem = null;
		HashMap<String, Object> _bandItem;			//아이템별 Property

		Element BandPropertyXml;
		NodeList _bandPropertys;
		
		NodeList _propertys = null;
		Element _propertyElement = null;
		int propertyLength = 0;
		
		// Reverse값이 false 일경우 처리
		if( _reverse == false)
		{
			for (i = 0; i < _lastNum; i++) {
				
				if( _bandItemList.size() > i )
				{
					BandItem = (HashMap<String, Object>) _bandItemList.get(i);
				}
				else
				{
					BandItem = (HashMap<String, Object>)  _bandItemList.get(0);
				}
				
				_colAr = new ArrayList<HashMap<String, Value>>();
				_totAr = new ArrayList<HashMap<String, Value>>();
				_grdToAr = new ArrayList<HashMap<String, Value>>();
				_valueAr = new ArrayList<HashMap<String, Value>>();
				_leftFAr = new ArrayList<HashMap<String, Value>>();
				_rightFAr = new ArrayList<HashMap<String, Value>>();
				
				_property = new HashMap<String, Value>();
				HashMap<String, Value> bandPropertyMap = new HashMap<String, Value>();
				
				for( String _key : BandItem.keySet() )
				{
					if( !_key.equals("items"))
					{
						bandPropertyMap.put( _key , new Value( BandItem.get(_key)));
					}
				}
				
				_bandAr.add(bandPropertyMap);
				
				ArrayList<HashMap<String, Object>> _bandItems = (ArrayList<HashMap<String, Object>>) BandItem.get("items");
				
				for ( k = 0; k < _bandItems.size(); k++) {
					
					bandPropertyMap = new HashMap<String, Value>();
					_bandItem = _bandItems.get(k);
					
					for( String _key : _bandItem.keySet() )
					{
						
						try {
							if( _bandItem.get(_key) instanceof String)
							{
								bandPropertyMap.put( _key , new Value( URLDecoder.decode( _bandItem.get(_key).toString(), "UTF-8" ) ));
							}
							else
							{
								bandPropertyMap.put( _key , new Value( _bandItem.get(_key) ));
							}
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					bandPropertyMap.put("Element", new Value(_bandItem));
					bandPropertyMap.put("className", Value.fromString("UBLabelBorder") );
					bandPropertyMap.put("LOAD_TYPE", new Value( ProjectInfo.LOAD_TYPE_JSON ) );
					if( mIsExportTable )
					{
						bandPropertyMap.put("isTable", new Value( "true", "string") );
						bandPropertyMap.put("TABLE_ID", new Value( _tblID, "string") );
					}
					
					if( k < _rowLength  ) _colAr.add(bandPropertyMap);
					else if( k < ( _rowLength + _lftAr.size() ) ) _leftFAr.add(bandPropertyMap);
					else if( k < ( _rowLength + _lftAr.size() + _valueLength ) ) _valueAr.add(bandPropertyMap);
					else if( k < (_bandItems.size() -1) && k >= ( (_bandItems.size()-1)-_rftAr.size() ) ) _rightFAr.add(bandPropertyMap);
					else if( k == (_bandItems.size() -1) ) _grdToAr.add(bandPropertyMap);
					else _totAr.add(bandPropertyMap);
					
				}
				
				if( _rowNumFlag ){
					Integer l = 0;
					NodeList _propertyList;
					Node ndlist; 
					
					// Node정보 업데이트 
					
					if( _totAr != null && _totAr.size() > 0 )
					{
						_bandItem = (HashMap<String, Object>) ((HashMap<String, Object>) _totAr.get(0).get("Element").getObjectValue() ).clone();
						_totAr.add(0, (HashMap<String, Value>) _totAr.get(0).clone() );
						_totAr.get(0).put("Element", new Value( _bandItem ) );
						_totAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						
						_bandItem.put("width", Float.valueOf( mRowNumWidth ).toString() );
					}
					
					if( _grdToAr != null && _grdToAr.size() > 0 )
					{
						
						_bandItem = (HashMap<String, Object>) ((HashMap<String, Object>) _grdToAr.get(0).get("Element").getObjectValue()).clone();
						_grdToAr.add(0, (HashMap<String, Value>) _grdToAr.get(0).clone() );
						_grdToAr.get(0).put("Element", new Value( _bandItem,"element") );
						_grdToAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						
						_bandItem.put("width", Float.valueOf( mRowNumWidth ).toString() );
					}
					
					if( _colAr != null && _colAr.size() > 0 )
					{
						_bandItem = (HashMap<String, Object>) ((HashMap<String, Object>) _colAr.get(0).get("Element").getObjectValue()).clone();
						_colAr.add(0, (HashMap<String, Value>) _colAr.get(0).clone() );
						_colAr.get(0).put("Element", new Value( _bandItem,"element") );
						_colAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						if( _noTitle.equals("") == false ) _colAr.get(0).put("text",new Value(_noTitle,"string") );
						_colAr.get(0).put("textAlign",new Value("center","string") );
						_colAr.get(0).put("verticalAlign",new Value("middle","string") );
						
						_bandItem.put("width", Float.valueOf( mRowNumWidth ).toString());
						_bandItem.put("text", _noTitle);
						_bandItem.put("textAlign", "center");
						_bandItem.put("verticalAlign", "middle");
					}
					
				}
				
				// 생성된 Array를 스타일 배열에 추가
				_columnStyle.add(_colAr);
				_valueStyle.add(_valueAr);
				_summeryStyle.add(_totAr);
				_grandTotStyle.add(_grdToAr);
				_leftFixedStyle.add(_leftFAr);
				_rightFixedStyle.add(_rightFAr);
				
			}
			
			
		}
		else
		{
			for ( i = _lastNum -1; i >= 0; i--) {
				
				_colAr = new ArrayList<HashMap<String, Value>>();
				_totAr = new ArrayList<HashMap<String, Value>>();
				_grdToAr = new ArrayList<HashMap<String, Value>>();
				_valueAr = new ArrayList<HashMap<String, Value>>();
				_leftFAr = new ArrayList<HashMap<String, Value>>();
				_rightFAr = new ArrayList<HashMap<String, Value>>();
				
				_property = new HashMap<String, Value>();
				
				BandItem = _bandItemList.get(i);
				HashMap<String, Value> bandPropertyMap = new HashMap<String, Value>();
				
				for( String _key : BandItem.keySet() )
				{
					if( !_key.equals("items"))
					{
						bandPropertyMap.put( _key , new Value( BandItem.get(_key)));
					}
				}
				
				_bandAr.add(bandPropertyMap);
				
				ArrayList<HashMap<String, Object>> _bandItems = (ArrayList<HashMap<String, Object>>) BandItem.get("items");
				
				for ( k = 0; k < _bandItems.size(); k++) {
					
					bandPropertyMap = new HashMap<String, Value>();
					_bandItem = _bandItems.get(k);
					
					for( String _key : _bandItem.keySet() )
					{
					
						try {
							if( _bandItem.get(_key) instanceof String)
							{
								bandPropertyMap.put( _key , new Value( URLDecoder.decode( _bandItem.get(_key).toString(), "UTF-8" ) ));
							}
							else
							{
								bandPropertyMap.put( _key , new Value( _bandItem.get(_key) ));
							}
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					bandPropertyMap.put("Element", new Value(_bandItem));
					bandPropertyMap.put("className", Value.fromString("UBLabelBorder") );
					bandPropertyMap.put("LOAD_TYPE", new Value( ProjectInfo.LOAD_TYPE_JSON ) );
					
					if( mIsExportTable )
					{
						bandPropertyMap.put("isTable", new Value( "true", "string") );
						bandPropertyMap.put("TABLE_ID", new Value( _tblID, "string") );
					}
					
					if( k < _rowLength  ) _colAr.add(bandPropertyMap);
					else if( k < ( _rowLength + _lftAr.size() ) ) _leftFAr.add(bandPropertyMap);
					else if( k < ( _rowLength + _lftAr.size() + _valueLength ) ) _valueAr.add(bandPropertyMap);
					else if( k < (_bandItems.size() -1) && k >= ( (_bandItems.size()-1)-_rftAr.size() ) ) _rightFAr.add(bandPropertyMap);
					else if( k == (_bandItems.size() -1) ) _grdToAr.add(bandPropertyMap);
					else _totAr.add(bandPropertyMap);
					
				}
				
				if( _rowNumFlag ){
					Integer l = 0;
					NodeList _propertyList;
					Node ndlist; 
					
					// Node정보 업데이트 
					
					if( _totAr != null && _totAr.size() > 0 )
					{
						_bandItem = (HashMap<String, Object>) ((HashMap<String, Object>) _totAr.get(0).get("Element").getObjectValue() ).clone();
						_totAr.add(0, (HashMap<String, Value>) _totAr.get(0).clone() );
						_totAr.get(0).put("Element", new Value( _bandItem ) );
						_totAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						
						_bandItem.put("width", Float.valueOf( mRowNumWidth ).toString() );
					}
					
					if( _grdToAr != null && _grdToAr.size() > 0 )
					{
						_bandItem = (HashMap<String, Object>) ((HashMap<String, Object>) _grdToAr.get(0).get("Element").getObjectValue()).clone();
						_grdToAr.add(0, (HashMap<String, Value>) _grdToAr.get(0).clone() );
						_grdToAr.get(0).put("Element", new Value( _bandItem,"element") );
						_grdToAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						
						_bandItem.put("width", Float.valueOf( mRowNumWidth ).toString() );
					}
					
					if( _colAr != null && _colAr.size() > 0 )
					{
						_bandItem = (HashMap<String, Object>) ((HashMap<String, Object>) _colAr.get(0).get("Element").getObjectValue()).clone();
						_colAr.add(0, (HashMap<String, Value>) _colAr.get(0).clone() );
						_colAr.get(0).put("Element", new Value( _bandItem,"element") );
						_colAr.get(0).put("width",new Value(mRowNumWidth,"number") );
						if( _noTitle.equals("") == false ) _colAr.get(0).put("text",new Value(_noTitle,"string") );
						_colAr.get(0).put("textAlign",new Value("center","string") );
						_colAr.get(0).put("verticalAlign",new Value("middle","string") );
						
						_bandItem.put("width", Float.valueOf( mRowNumWidth ).toString());
						_bandItem.put("text", _noTitle);
						_bandItem.put("textAlign", "center");
						_bandItem.put("verticalAlign", "middle");
						
					}
					
				}
				
				// 생성된 Array를 스타일 배열에 추가
				_columnStyle.add(_colAr);
				_valueStyle.add(_valueAr);
				_summeryStyle.add(_totAr);
				_grandTotStyle.add(_grdToAr);
				_leftFixedStyle.add(_leftFAr);
				_rightFixedStyle.add(_rightFAr);
			}
				
		}
		
		_retMap.put("COL", _columnStyle);
		_retMap.put("VALUE", _valueStyle);
		_retMap.put("SUM", _summeryStyle);
		_retMap.put("GRD", _grandTotStyle);
		_retMap.put("LFC", _leftFixedStyle);
		_retMap.put("RFC", _rightFixedStyle);
		_retMap.put("BAND", _bandAr);
		
		return _retMap;
	}
	private HashMap<String, Object> convertUBFXJson( HashMap<String, Value> _property, ArrayList<String> rowData, ArrayList<String> _rowCols, int _colIdx, ArrayList<ArrayList<String>> headerData, ArrayList<String> colData )
	{
		//@ubfx 처리
		if( _property.containsKey("Element") && _property.get("Element") != null )
		{
			HashMap<String, Object> _item = (HashMap<String, Object>) _property.get("Element").getObjectValue();
			
			Element _propertyElement = null;
			String _ubfxString = "";
			NodeList _propertys = null;
			if( _item.containsKey("ubfx") == false ) return null;
			
			ArrayList<HashMap<String, String>> _ubfxList = (ArrayList<HashMap<String, String>>) _item.get("ubfx");
			ArrayList<HashMap<String, String>> _newFxLit = new ArrayList<HashMap<String, String>>();

			Element _cloneElement = null;
			
			int _ubfxSize = _ubfxList.size();
			for (int  _j = 0; _j < _ubfxSize; _j++) {
				
				try {
					_ubfxString = URLDecoder.decode( _ubfxList.get(_j).get("value"),"UTF-8" );
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					_ubfxString = _propertyElement.getAttribute("value");
				}
				
				String _checkStr = ",+-=()*/<> ;";
				
				String _chkUbfxString = ubfxUseDataSet( _ubfxString, _rowCols, rowData, _colIdx, headerData, colData );
				while( _ubfxString.equals(_chkUbfxString) == false )
				{
					_ubfxString = _chkUbfxString;
					
					_chkUbfxString = ubfxUseDataSet(_ubfxString, _rowCols, rowData, _colIdx, headerData, colData );
				}
				
				if( _ubfxString.contains( THIS_TEXT ) )
				{
					if( FUNCTION_VERSION.equals("2.0"))
					{
						_ubfxString = _ubfxString.replaceAll( THIS_TEXT, "\"" + _property.get("text").getStringValue() + "\"" );
					}
					else
					{
						_ubfxString = _ubfxString.replaceAll( THIS_TEXT, _property.get("text").getStringValue() );
					}
				}
				
				try {
					_ubfxString = URLEncoder.encode( _ubfxString, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				HashMap<String, String> _ubfxItem = new HashMap<String, String>();
				_ubfxItem.put("property", _ubfxList.get(_j).get("property"));
				_ubfxItem.put("value", _ubfxString);
				
				_newFxLit.add(_ubfxItem);
//				_ubfxList.get(_j).put("value", _ubfxString );
				
			}
			_property.put("ubfx", new Value(_newFxLit) );
			
			if( _ubfxSize > 0 )
			{
				return _item;
			}
			
		}
		
		return null;
	}
	
	protected HashMap<String, Object> getSplitCharacter(HashMap<String, Value> _item, float[] _heightAr )
	{
		HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
		float _fontSize =  _item.get("fontSize").getIntegerValue();
		float _lineHeight =  1.2f;
		
		if( _item.containsKey("lineHeight"))
		{
			if( _item.get("lineHeight").getStringValue().indexOf("%") != -1  )
			{
				_lineHeight = Float.valueOf(_item.get("lineHeight").getStringValue().replace("%", "")) / 100;
			}
			else
			{
				_lineHeight = _item.get("lineHeight").getIntegerValue();
			}
		}
		
		String _fontFamily =  _item.get("fontFamily").getStringValue();
		String fontWeight =  _item.get("fontWeight").getStringValue();
		float _margin = -1;
		String _text = _item.get("text").getStringValue();
		
		//					_value = _value.replace("%25", "");
//		_value = String.valueOf((Float.parseFloat(_value)/100));
		
		if( _item.containsKey("AdjustableHeightMargin") )
		{
			_margin = _item.get("AdjustableHeightMargin").getIntegerValue();
		}
		
		optionMap.put("width", new float[]{ _item.get("width").getIntegerValue() });
		optionMap.put("height", _heightAr);
		optionMap.put("fontSize", new float[]{ _fontSize });
		optionMap.put("lineHeight", new float[]{ _lineHeight });
		if( _item.containsKey("padding") == false ){
			optionMap.put("padding", new float[]{ 3 });
		}else{
			optionMap.put("padding", new float[]{_item.get("padding").getIntegerValue()});
		}
		HashMap<String, Object> _result = StringUtil.getSplitCharacter(_text, optionMap, fontWeight, _fontFamily, _fontSize, _margin);
		
		ArrayList<Float> _har = (ArrayList<Float>) _result.get("Height");
		
		return _result;
	}
	
	
}
