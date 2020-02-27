package org.ubstorm.service.parser.formparser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.xmlbeans.impl.jam.mutable.MParameter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.utils.common;

public class SbgridConvertParser {

	public SbgridConvertParser() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	int _headerBackground = 3381708;
	String backcolorAlternate = "16777215";
	String _headerRowHeight = "50";
	String _dataRowHeight = "30";
	int _pageWidth = 794;
	int _pageHeight = 1123;
	int _pageGap = 40; // 좌우 padding값
	String _chartXField = "5";
	String _chartYField = "1";
	// 프로젝트 xml생성
	
	// 페이지 xml생성 
	
	// 밴드 생성
	// captionColor : 헤더색상
	// backColorAlternate : 셀 배경색
	
	// 테이블 생성
	public String convertSbgridToXml( String jsonStr ) throws UnsupportedEncodingException
	{	
		String title = "타이틀";
		int _columnCnt = 1;
		ArrayList<Float> _columnWidth = new ArrayList<Float>();
		ArrayList<String> _columnData = new ArrayList<String>();
		ArrayList<String> _datasetName = new ArrayList<String>();
		ArrayList<String> _columnTextAlign = new ArrayList<String>();
		boolean _useChart = false;
		_pageWidth = 794;
		_pageHeight = 1123;
		String _xFiled = "classification";
		String _yFiled = "Latitude";
		JSONObject mParam = null;
		
		if( jsonStr.hashCode() != 0 )
		{
			//mParam = _jsonUtils.jsonToMap((String) _params); 
		 	Object ubObj;
			try {
				ubObj = JSONValue.parseWithException( jsonStr );
				mParam = (JSONObject)ubObj;
				
//		 	json_data.title = strTitle;
//			json_data.colCount = 1;
//			json_data.colWidth = new Array();		
//			json_data.datasetName = new Array();
//			json_data.colData = new Array();
//			json_data.text_align = new Array();
				
				if( mParam.containsKey("COLUMN_INFO") )
				{
					
					Object infoObj = JSONValue.parseWithException( URLDecoder.decode( mParam.get("COLUMN_INFO").toString(), "UTF-8")  );
					HashMap columnInfoJson = (HashMap) infoObj;  
					HashMap _columnInfo =  (HashMap) JSONValue.parseWithException(  columnInfoJson.get("parameter").toString() );
					
					title 			= _columnInfo.get("title").toString();
					_columnCnt 		= Integer.valueOf(_columnInfo.get("colCount").toString() );
					_columnData 	= (ArrayList<String>) JSONValue.parse( _columnInfo.get("colData").toString() );
					_datasetName 	= (ArrayList<String>) JSONValue.parse( _columnInfo.get("datasetName").toString() );
					_columnTextAlign = (ArrayList<String>) JSONValue.parse(_columnInfo.get("text_align").toString() );
//					_columnWidth 	= (ArrayList<Float>) JSONValue.parse(_columnInfo.get("colWidth").toString() );
					_columnWidth 	= Value.convertStringToArrayFloat( _columnInfo.get("colWidth").toString().replace("[", "").replace("]", "") );
					
					if( _columnInfo.containsKey("useChart") && "true".equals(_columnInfo.get("useChart").toString()) ) _useChart = true;
					if( _columnInfo.containsKey("xField")) _xFiled = _columnInfo.get("xField").toString();
					if(_columnInfo.containsKey("yField")) _yFiled = _columnInfo.get("yField").toString();
					
					
					// 페이지 방향 ( horizontal : 가로 방향, default:세로방향 )
					if(_columnInfo.containsKey("pageDirection"))
					{
						if( _columnInfo.get("pageDirection").toString().equals("horizontal") )
						{
							_pageWidth = 1123;
							_pageHeight = 794;
						}
					} 
					
					// 헤더 컬러
					if(_columnInfo.containsKey("captionColor") && _columnInfo.get("captionColor").toString().equals("") == false  )
					{
						_headerBackground = Integer.parseInt(_columnInfo.get("captionColor").toString().replace("#",""), 16) ;
					}
					
					// 그리드의 AlterNate 색상 ( 기본적으로 한건만 넘어오면 힌색,alterNate색상을 반복하여 지정
					if(_columnInfo.containsKey("backColorAlternate") && _columnInfo.get("backColorAlternate").toString().equals("") == false )
					{
						String[] _alterColor = _columnInfo.get("backColorAlternate").toString().split(",");
						String _alterColorString = "";
						for (int i = 0; i < _alterColor.length; i++) {
							if(_alterColorString.equals("") == false ) _alterColorString = _alterColorString + ",";
							_alterColorString = String.valueOf( Integer.parseInt( _alterColor[i].replace("#", ""), 16 ) );
						}
						
						// 반복색상이 존재할경우
						if( _alterColor.length == 1 )
						{
							backcolorAlternate = "16777215,"+_alterColorString;
						}
						else if( _alterColorString.equals("") )
						{
							backcolorAlternate = "16777215";
						}
					}
					
					// 헤더의 Height값 지정
					if( _columnInfo.containsKey("headerHeight") && _columnInfo.get("headerHeight").toString().equals("") == false )
					{
						_headerRowHeight = _columnInfo.get("headerHeight").toString();
					}
					
					// 데이터 밴드의 height값 지정
					if( _columnInfo.containsKey("rowHeight") && _columnInfo.get("rowHeight").toString().equals("") == false )
					{
						_dataRowHeight = _columnInfo.get("rowHeight").toString();
					}
						
					
					// columnWidth값을 이용하여 비율로 아이템들의 width값을 지정
					int max = _columnWidth.size();
					float _maxWidth = 0;
					float _widthPer = 0;
					float _realWdith = _pageWidth - 40;
					
					for (int i = 0; i < max; i++) {
						_maxWidth = _maxWidth +  _columnWidth.get(i);
					}
					
					if( _maxWidth > _realWdith ) 
					{
						_widthPer = _realWdith/_maxWidth;
					}
					
					if(_widthPer > 0)
					{
						for (int i = 0; i < max; i++) {
							_columnWidth.set(i,  (float) Math.floor( _columnWidth.get(i)*_widthPer ) );
						}
					}
					
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		 	
		}
		
		int i = 0;
		int max = 0;
		
		Document document = new Document();
		// project tag 생성
		Element project = new Element("project");
		project.setAttribute("formId", "");
		project.setAttribute("projectName", "");
		project.setAttribute("pageWidth", String.valueOf( _pageWidth ) );
		project.setAttribute("pageHeight", String.valueOf(_pageHeight) );
		project.setAttribute("pageContinue", "false");
		project.setAttribute("waterMark", "");
		
		Element root = new Element("page");
		// page속성 지정
		root.setAttribute("width", String.valueOf( _pageWidth ) );		// 문서의 width
		root.setAttribute("height", String.valueOf( _pageHeight ) );	// 문서의 height
		root.setAttribute("docType", "2");								// docType
		root.setAttribute("divide", "0");						
		root.setAttribute("backgroundColor", "16777215");				// 배경색
		root.setAttribute("reportType", "3");							// 문서의 Type
		root.setAttribute("clone", "0");	
		root.setAttribute("isPivot", "false");
		root.setAttribute("isGroup", "false");
		root.setAttribute("groupData", "null");
		
		Element _items = new Element("Items");
		Element _item = null;
		//band생성
		//1. pageHeaderBand 생성
		String[] _pageHeaderKeys = {"x","y","width","height","repeat"};
		String[] _pageHeaderValues = {"0","0",String.valueOf( _pageWidth ),"100","y"};
		String[] _pageHeaderTypes = {"number","number","number","number","string"};
		_item = makeItemElement(_pageHeaderKeys,_pageHeaderValues,_pageHeaderTypes,"item");
		_item.setAttribute("className","UBPageHeaderBand");
		_item.setAttribute("id","UBPageHeaderBand_0");
		
		_items.addContent(_item);
		
		if(_useChart)
		{
			// ETC밴드 생성 ( 차트가 삽입될 밴드 )
			// 2. headerBand지정
			String[] _emptyKeys = {"x","y","width","height"};
			String[] _emptyValues = {"0","0",String.valueOf( _pageWidth ),"350"};
			String[] _emptyTypes = {"number","number","number","number"};		
			
			_item = makeItemElement(_emptyKeys,_emptyValues,_emptyTypes,"item");
			_item.setAttribute("className","UBEmptyBand");
			_item.setAttribute("id","UBEmptyBand_0");
			
			_items.addContent(_item);
		}
		
		
		// 2. headerBand지정
		String[] _headerKeys = {"x","y","width","height","adjustableHeight"};
		String[] _headerValues = {"0","0",String.valueOf( _pageWidth ),_headerRowHeight,"false"};
		String[] _headerTypes = {"number","number","number","number","boolean"};
		
		_item = makeItemElement(_headerKeys,_headerValues,_headerTypes,"item");
		_item.setAttribute("className","UBDataHeaderBand");
		_item.setAttribute("id","UBDataHeaderBand_0");
		
		_items.addContent(_item);
		
		//3. DataBand생성
		String[] _dataKeys = {"x","y","width","height","adjustableHeight"};
		String[] _dataValues = {"0","50",String.valueOf( _pageWidth ),_dataRowHeight,"false"};
		String[] _dataTypes = {"number","number","number","number","boolean"};
		
		_item = makeItemElement(_dataKeys, _dataValues, _dataTypes, "item");
		_item.setAttribute("className","UBDataBand");
		_item.setAttribute("id","UBDataBand_0");
		
		_items.addContent(_item);
		
		
		// 헤더 타이틀 Add
		String[] _headerTitleKeys = {"x","y","width","height","textAlign","verticalAlign","fontSize","fontFamily","fontWeight","backgroundColor","text","band","band_x","band_y","borderType","borderSide"};
		String[] _headerTitleValues = {"0","0", String.valueOf(_pageWidth),"100","center","middle","30","%EB%A7%91%EC%9D%80%20%EA%B3%A0%EB%94%95","bold","16777215",title,"UBPageHeaderBand_0","0","0","SOLD","none"};
		String[] _headerTitleTypes = {"number","number","number","number","string","string","number","string","string","number","string","string","number","number","string","string"};

		_item = makeItemElement(_headerTitleKeys, _headerTitleValues, _headerTitleTypes, "item");
		_item.setAttribute("className","UBLabel");
		_item.setAttribute("id","UBLABEL_HEADER_0");
		
		_items.addContent(_item);
		
		
		// 차트 생성 ( RadialChart와 Column차트 생성 )
		
		if(_useChart)
		{
			String[] _chartAr = {"pieChart","columnChart"};
			float _gap = 20;
			float _x = _gap;
			float _width = (_pageWidth/_chartAr.length) - (( (_chartAr.length+1)*_gap)/2);
			
			for (int j = 0; j < _chartAr.length; j++) {
				
				_item = makeChartItemData( _columnData, _datasetName, _xFiled, _yFiled, _width, "UBEmptyBand_0", _chartAr[j], _x);
				_items.addContent(_item);
				
				_x = _x + _gap + _width;
				
			}
		}
		
		
		// 헤더 테이블  Add
		_item = makeTableElement("UBDataHeaderBand_0", _columnCnt, _columnData, _datasetName, _columnWidth, _columnTextAlign);
		_items.addContent(_item);

		// 데이터 테이블 Add
		_item = makeDataTableElement("UBDataBand_0", _columnCnt, _columnData, _datasetName, _columnWidth, _columnTextAlign);
		_items.addContent(_item);
		
		root.addContent(_items);
		project.addContent(root);
		project.addContent(paramDataSet());
		
		if(_useChart)
		{
	    	HashMap<String, String> _tempPList = (HashMap<String, String>) mParam.get("UBI_SBDATA");
			String _dataStr = String.valueOf( _tempPList.get("parameter") );
			Element _userElement = userDataSet( _xFiled, _yFiled.split(","),  _datasetName, _dataStr );
			project.addContent(_userElement);
		}
		
		document.addContent(project);
		
		
		String retStr = docuemntToString(document);
		
		document = null;
		project  = null;
		_columnWidth = null;
		_columnData =  null;
		_datasetName = null;
		_columnTextAlign =  null;
		
		return retStr;
	}

	private String docuemntToString( Document doc)
	{
		String retString = "";
		
		Format myFmt = Format.getCompactFormat();
		XMLOutputter fmt = new XMLOutputter(myFmt);
		
		retString = fmt.outputString(doc);
		
		return retString; 
	}
	
	
	private Element makeItemElement( String[] _keys, String[] _values,  String[] _types, String _elementName )
	{
		int i = 0;
		int max = 0;
		
		Element _item = new Element(_elementName);
		Element _property = null;
		
		max = _keys.length;
		
		for ( i = 0; i < max; i++) {
			_property = new Element("property");
			_property.setAttribute("name", _keys[i]);
			_property.setAttribute("value", _values[i]);
			_property.setAttribute("type", _types[i]);
			_item.addContent(_property);
		}

		
		return _item;
	}
	
	
	private Element makeTableElement( String _bandName, int _columnCnt, ArrayList<String> colData, ArrayList<String> datasetName, ArrayList<Float> colWidths, ArrayList<String> textAligns )
	{
		Element _item = new Element("item");
		Element _property = null;
		//Table 생성 HeaderData
		
		String[] _hTableKeys = {"x","y","width","height","version","rowCount","columnCount","band","band_x","band_y"};
		String[] _hTableValues = {"20","0", String.valueOf(_pageWidth-_pageGap),_headerRowHeight,"2.0","1","5","UBDataHeaderBand_0","20","0"};
		String[] _hTableTypes = {"number","number","number","number","string","number","number","string","number","number"};
		
		_item = makeItemElement(_hTableKeys,_hTableValues, _hTableTypes, "item");
		_item.setAttribute("className","UBTable");
		
		int _rowCnt = 1;
		int _colCnt = _columnCnt;
		
		Element _tableElement = new Element("table");
		Element tableMapData = null;
		Element cell = null; 
		
		String[] _tableMapDataKeys = {"x","y","width","height","columnIndex","rowIndex","rowHeight","columnWidth","rowSpan","colSpan","status","borderString"};
		String[] _tableMapDataValues = {"0","0","100", _headerRowHeight,"0","0",_headerRowHeight,"100","1","1","NORMAL","type%3AborderBottom%2CborderType%3ASOLD%2CborderColor%3A0%2CborderThickness%3A1%26type%3AborderLeft%2CborderType%3ASOLD%2CborderColor%3A0%2CborderThickness%3A1%26type%3AborderRight%2CborderType%3ASOLD%2CborderColor%3A0%2CborderThickness%3A1%26type%3AborderTop%2CborderType%3ASOLD%2CborderColor%3A0%2CborderThickness%3A1"};
		String[] _tableMapDataTypes = {"number","number","number","number","number","number","number","number","number","number","string","string"};
		
		String[] _cellKeys = {"x","y","width","height","backgroundColor","backgroundAlpha","textAlign","verticalAlign","fontSize","fontFamily","fontWeight","textDecoration","fontStyle","fontColor","text","padding","dataSet","column","dataType"};
		String[] _cellValues = {"0","0","100",_headerRowHeight, String.valueOf(_headerBackground) ,"1","center","middle","12","Tahoma","normal","none","normal","0","Header","3","null","null","0"};
		String[] _cellTypes = {"number","number","number","number","number","number","string","string","number","string","string","string","string","number","string","number","string","string","string"};
		
		for (int i = 0; i < _rowCnt; i++) {
			Element tableMap = new Element("tableMap");
			_tableMapDataValues[0] = "0";
			_tableMapDataValues[4] = "0";
			
			for (int j = 0; j < _colCnt; j++) {
				
				tableMapData = makeItemElement(_tableMapDataKeys,_tableMapDataValues, _tableMapDataTypes, "tableMapData");
				
				if( colData.size() > j )
				{
					_cellValues[14] = colData.get(j);
				}
				if( textAligns.size() > j )
				{
					if(textAligns.get(j).equals(""))
					{
						_cellValues[6] = "center";						
					}
					else
					{
						_cellValues[6] = textAligns.get(j);
					}
				}
				if( colWidths.size() > j )
				{
					_cellValues[2] = colWidths.get(j).toString();
				}
				
				cell =  makeItemElement(_cellKeys,_cellValues, _cellTypes, "cell");
				
				tableMapData.addContent(cell);
				_cellValues[0] =  String.valueOf( Float.valueOf(_cellValues[0]) +  Float.valueOf(_cellValues[2]) );							// Cell의 x좌표 업데이트
				_tableMapDataValues[0] =  String.valueOf( Float.valueOf(_tableMapDataValues[0]) +  Float.valueOf(_tableMapDataValues[2]) ); // TableMapData의 x좌표 업데이트
				_tableMapDataValues[4] =  String.valueOf(j);
				
				tableMap.addContent(tableMapData);
			}
			_tableMapDataValues[5] =  String.valueOf(i);
			
			_cellValues[1] =  String.valueOf( Float.valueOf(_cellValues[1]) +  Float.valueOf(_tableMapDataValues[6]) );						// Cell의 y좌표 업데이트
			_tableMapDataValues[1] = String.valueOf( Float.valueOf(_tableMapDataValues[1]) +  Float.valueOf(_tableMapDataValues[6]) );		// TableMapData의 y좌표 업데이트
			
			_tableElement.addContent(tableMap);
		}
		
		_item.addContent(_tableElement);
		return _item;
	}
	
	
	private Element makeDataTableElement( String _bandName, int _columnCnt, ArrayList<String> colData, ArrayList<String> datasetName, ArrayList<Float> colWidths, ArrayList<String> textAligns  )
	{
		Element _item = null;
		Element _property = null;
		//Table 생성 HeaderData
		
		String[] _hTableKeys = {"x","y","width","height","version","rowCount","columnCount","band","band_x","band_y"};
		String[] _hTableValues = {"20","50",String.valueOf(_pageWidth-_pageGap),_dataRowHeight,"2.0","1","5",_bandName,"20","0"};
		String[] _hTableTypes = {"number","number","number","number","string","number","number","string","number","number"};
		
		_item = makeItemElement(_hTableKeys,_hTableValues, _hTableTypes, "item");
		_item.setAttribute("className","UBTable");
		
		int _rowCnt = 1;
		int _colCnt = _columnCnt;
		
		
		Element tableMapData = null; 
		Element cell = null;
		
		String[] _tableMapDataKeys = {"x","y","width","height","columnIndex","rowIndex","rowHeight","columnWidth","rowSpan","colSpan","status","borderString"};
		String[] _tableMapDataValues = {"0","0","100",_dataRowHeight,"0","0",_dataRowHeight,"100","1","1","NORMAL","type%3AborderBottom%2CborderType%3ASOLD%2CborderColor%3A0%2CborderThickness%3A1%26type%3AborderLeft%2CborderType%3ASOLD%2CborderColor%3A0%2CborderThickness%3A1%26type%3AborderRight%2CborderType%3ASOLD%2CborderColor%3A0%2CborderThickness%3A1%26type%3AborderTop%2CborderType%3ASOLD%2CborderColor%3A0%2CborderThickness%3A1"};
		String[] _tableMapDataTypes = {"number","number","number","number","number","number","number","number","number","number","string","string"};
		
		String[] _cellKeys = {"x","y","width","height","backgroundColor","backgroundAlpha","textAlign","verticalAlign","fontSize","fontFamily","fontWeight","textDecoration","fontStyle","fontColor","text","padding","dataSet","column","dataType","repeatValue"};
//		String[] _cellValues = {"0","0","100","30","16777215,52377","1","center","middle","12","Tahoma","normal","none","normal","0","DATABAND","3","UBI_SBDATA","col_1","1","true"};
		String[] _cellValues = {"0","0","100",_dataRowHeight,backcolorAlternate,"1","center","middle","12","Tahoma","normal","none","normal","0","DATABAND","3","UBI_SBDATA","col_1","1","true"};
		String[] _cellTypes = {"number","number","number","number","string","number","string","string","number","string","string","string","string","number","string","number","string","string","string","boolean"};
		
		Element _tableElement = new Element("table"); 
		
		for (int i = 0; i < _rowCnt; i++) {
			Element tableMap = new Element("tableMap");
			_tableMapDataValues[0] = "0";
			_tableMapDataValues[4] = "0";
			
			for (int j = 0; j < _colCnt; j++) {
				
				tableMapData = makeItemElement(_tableMapDataKeys,_tableMapDataValues, _tableMapDataTypes, "tableMapData");
				
				if( textAligns.size() > j )
				{
					if(textAligns.get(j).equals(""))
					{
						_cellValues[6] = "left";						
					}
					else
					{
						_cellValues[6] = textAligns.get(j);
					}
				}
				if( colWidths.size() > j )
				{
					_cellValues[2] = colWidths.get(j).toString();
				}
				
				_cellValues[17] = "col_" + ( datasetName.indexOf(colData.get(j)) + 1);
				cell =  makeItemElement(_cellKeys,_cellValues, _cellTypes, "cell");
				
				tableMapData.addContent(cell);
				_cellValues[0] =  String.valueOf( Float.valueOf(_cellValues[0]) +  Float.valueOf(_cellValues[2]) );							// Cell의 x좌표 업데이트
				_tableMapDataValues[0] =  String.valueOf( Float.valueOf(_tableMapDataValues[0]) +  Float.valueOf(_tableMapDataValues[2]) ); // TableMapData의 x좌표 업데이트
				_tableMapDataValues[4] =  String.valueOf(j);
				
				tableMap.addContent(tableMapData);
			}
			
			_tableMapDataValues[5] =  String.valueOf(i);
			_cellValues[1] =  String.valueOf( Float.valueOf(_cellValues[1]) +  Float.valueOf(_tableMapDataValues[6]) );						// Cell의 y좌표 업데이트
			_tableMapDataValues[1] = String.valueOf( Float.valueOf(_tableMapDataValues[1]) +  Float.valueOf(_tableMapDataValues[6]) );		// TableMapData의 y좌표 업데이트
			
			_tableElement.addContent(tableMap);
		}
		
		_item.addContent(_tableElement);
		return _item;
	}
	
	public  ArrayList<HashMap<String, Object>> convertSbGridDataSet(JSONObject mParam)
	{
		HashMap<String, String> _tempPList = (HashMap<String, String>) mParam.get("UBI_SBDATA");
		String _dataStr = String.valueOf( _tempPList.get("parameter") );
		ArrayList<HashMap<String, Object>> _tempData = convertSbgridDataSet(_dataStr);

		return _tempData;
	}
	
	
	private ArrayList<HashMap<String, Object>> convertSbgridDataSet( String _str )
	{
		ArrayList<HashMap<String, Object>> _tmpdata = new ArrayList<HashMap<String, Object>>();
		
		String _delimiter = changeDelimiter("^");
		
		String[] _stAr = _str.split("\n");
		
		for (int i = 0; i < _stAr.length; i++) {
			_tmpdata =	setCsvDataSetMapping(_stAr[i].split(_delimiter) , _tmpdata );
		}
		
//		if( !_firstRow ) 
//		{
//			if( _tmpdata.size() > 0 )
//			{
//				_tmpdata.remove(0);
//			}
//		}
		
		return _tmpdata;
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
    
    
    private Element makeChartItemData( ArrayList<String> colData, ArrayList<String> datasetName, String _xFieldName, String _yFieldName , float _width, String _bandName, String _type , float _x )
    {
		
    	Element _item = null;
		Element _displayName = null;
		Element _column = null;
		
    	String _columnName = "col_" + (datasetName.indexOf(_xFieldName) + 1);
    	
		String[] _chartKeys = 	{"x","y","width","height","showDataTips","fontSize","fontFamily","band","band_x","band_y","dataSet","seriesXField","yFieldName","yFieldDisplayName","isCrossTabData","form","gridLine","gridLineWeight","gridLIneDirection","gridLIneColor","legendDirection","legendLabelPlacement","legendMarkHeight","legendMarkWidthlegendLocation","dataLabelPostion","isCrossTabData","isDuplication","yFieldFillColor","closeField","highField","lowField","openField"};
		String[]_chartValues = 	{ String.valueOf( _x ),"0",String.valueOf( _width ),"330","true","12","맑은 고딕",_bandName, String.valueOf( _x ),"10","UBI_SBUSERDATA",_columnName,"", "", "false","curve","","","vertical","0","null","","","","inside","false","false","","","","",""};
		String[] _chartTypes = 	{"number","number","number","number","boolean","number","string","string","number","number","string","string","string","string","boolean","string","string","string","string","string","object","string","string","string","string","boolean","boolean","string","string","string","string","string"};
    	
		String _className = "";
		String _id = "";
		
		if(_type.equals("columnChart"))
		{
			_className = "UBColumnChart";
			_id = "UBCOLUMNCHART_0";
		}
		else
		{
			_className = "UBPieChart";
			_id = "UBPIECHART_0";
		}
		
		_item = makeItemElement(_chartKeys, _chartValues, _chartTypes, "item");
		_item.setAttribute("className",_className);
		_item.setAttribute("id",_id);
		
		_displayName = new Element("displayName");
		_displayName.setAttribute("type","ArrayCollection");
		
		
		// column 정보 
		String[] _columnKeys = {"column","text","color","fn","expression","visible"};
		String[] _columnValues = {"col_1","","52275","null","null","false"};
		String[] _columnTypes = {"string","string","number","string","string","boolean"};
		
				
		for (int i = 0; i < datasetName.size(); i++) {
			
			if( datasetName.indexOf( _yFieldName ) == i )
			{
				_columnValues[5] = "true";
				_columnValues[0] = "col_" + (datasetName.indexOf(_yFieldName) + 1);
				_columnValues[1] = _yFieldName;
						
			}
			else
			{
				_columnValues[5] = "false";
				_columnValues[0] = "";
				_columnValues[1] = "";
			}
			_column = makeItemElement(_columnKeys, _columnValues, _columnTypes, "column");
			_column.setAttribute("type","HashTable");
			
			_displayName.addContent(_column);
		}
    	
		_item.addContent(_displayName);
		
		return _item;
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
     * functionName :   userDataSet</br>
     * desc			:	차트용 데이터를 생성하기 위하여 userDataSet형태로 작성
     * @return
     */
    private Element userDataSet( String _xField, String[] _yField, ArrayList<String> _dataName, String _dataStr )
    {
    	
    	try {
			_dataStr = URLDecoder.decode(_dataStr, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	_xField = "col_" + (_dataName.indexOf(_xField)+1);
    	
    	for (int i = 0; i < _yField.length; i++) {
    		_yField[i] = "col_" + (_dataName.indexOf( _yField[i] )+1);
		}
    	
    	ArrayList<HashMap<String, Object>> _temtData = convertSbgridDataSet(_dataStr);
    	ArrayList<HashMap<String, Object>> _retData = new ArrayList<HashMap<String,Object>>();
    	HashMap<String, Object> _rowData = null;
    	
    	HashMap<String, Integer> _positionMap = new HashMap<String, Integer>();
    	
    	int _max = _temtData.size();
    	int _curPosition = 0;
    	boolean _emptyItem = false; 
    	// 헤더값은 제거하고 처리
    	for (int i = 1; i < _max; i++) {
			
    		if( _positionMap.containsKey( _temtData.get(i).get(_xField ) )  )
    		{
    			_curPosition = _positionMap.get(_temtData.get(i).get(_xField ));
    			_rowData = _retData.get(_curPosition);
    			_emptyItem  = false;
    		}
    		else
    		{
    			_curPosition = _retData.size();
    			_positionMap.put( _temtData.get(i).get(_xField ).toString(), (Integer) _curPosition);
    			_rowData = new HashMap<String, Object>();
    			
    			_emptyItem  = true;
    			_rowData.put(_xField, _temtData.get(i).get(_xField ) );
    			_retData.add(_rowData);
    		}
    		
    		float _num = 0;
    		for (int j = 0; j < _yField.length; j++) {
				
    			_rowData = _retData.get(_curPosition);
    			
    			try {
    				_num = Float.valueOf( _temtData.get(i).get(_yField[j] ).toString() );
				} catch (Exception e) {
					// TODO: handle exception
					_num = 0;
				}
    			
    			if( _emptyItem )
    			{
    				_rowData.put(_yField[j], _num  );
    			}
    			else
    			{
    				_rowData.put(_yField[j], Float.valueOf( _rowData.get(_yField[j]).toString() ) +  _num );
    			}
    			
			}
    		
		}
    	
    	String _jsonStr = JSONArray.toJSONString(_retData);
    	
    	
    	Element userDataSet = null;
    	
    	String[] _userKeys = {"typeName","firstRow", "dataStructureType"};
		String[] _userValues = {"user","false","0"};
		String[] _userTypes = {"string","boolean","number"};
		
		userDataSet =  makeItemElement(_userKeys,_userValues, _userTypes, "item");
    	
		Element _data = new Element("data");
		try {
			_jsonStr = common.base64_encode_encrypt(_jsonStr, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_data.setText(  _jsonStr  );
		
		userDataSet.setAttribute("className", "UBDmcUser");
		userDataSet.setAttribute("id", "UBI_SBUSERDATA");
		userDataSet.addContent(_data);
		
    	return userDataSet;
    }
    
    private Element paramDataSet()
    {
    	Element paramDataSet = null;
    	
    	
		String[] _paramKeys = {"typeName","delimiter","firstRow","jason","fileType","uriEncode"};
		String[] _paramValues = {"param","^","false","false","csv","true"};
		String[] _paramTypes = {"string","string","boolean","boolean","string","boolean"};
		
		paramDataSet =  makeItemElement(_paramKeys,_paramValues, _paramTypes, "item");
    	
		paramDataSet.setAttribute("className", "UBDmcParam");
		paramDataSet.setAttribute("id", "UBI_SBDATA");

		return paramDataSet;
    }
	
	
}
