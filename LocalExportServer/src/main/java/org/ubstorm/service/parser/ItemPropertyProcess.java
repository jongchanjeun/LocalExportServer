package org.ubstorm.service.parser;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.script.ScriptException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.formparser.UBIDataUtilPraser;
import org.ubstorm.service.utils.JsonUtils;
import org.ubstorm.service.parser.formparser.info.PageInfoSimple;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 아이템에서 사용되는 속성값을 변환 할때 쓰는 함수가 있습니다.
 * */
public class ItemPropertyProcess {

	
	/**
	 * 대문자로 들어오는 보더 속성을 소문자로 변경해 줌. 
	 * @return String _bStr
	 * 
	 */
	public String getBorderType(String _bType)
	{
		String _bStr = "";

		if( _bType.equals("SOLD"))
		{
			_bStr = "solid";
		}
		else if( _bType.equals("DASH"))
		{
			_bStr = "dash";
		}
		else if( _bType.equals("DASH_DOT"))
		{
			_bStr = "dash_dot";
		}
		else if( _bType.equals("DASH_DOT_DOT"))
		{
			_bStr = "dash_dot_dot";
		}
		else if( _bType.equals("DOT"))
		{
			_bStr = "dot";
		}
		else if( _bType.equals("DOUBLE"))
		{
			_bStr = "double";
		}
		else
		{
			_bStr = "none";
		}

		return _bStr;
	}
	

	/**
	 * int로 된 color를 RGB로 변경 한다. 
	 * @return String _rgb
	 * 
	 */
	public String changeColorToRGB(int _color)
	{
		String _rgb = "";

		Color _c = new Color(_color);

		_rgb = "rbg(" + _c.getRed() + "," + _c.getGreen() + "," + _c.getBlue() + ")";

		return _rgb;
	}

	/**
	 * int로 된 color를 Hex 로 변경 한다. 
	 * @return String _hex
	 * 
	 */
	public String changeColorToHex(int _color)
	{
		String _hex = "";

		Color _c = new Color(_color);

		String _red = "";
		
		if(Integer.toHexString(_c.getRed()).length() != 2 )
		{
			_red = "0" + Integer.toHexString(_c.getRed()); 
		}
		else
		{
			_red = Integer.toHexString(_c.getRed());
		}
		
		String _blue = "";
		
		if(Integer.toHexString(_c.getBlue()).length() != 2 )
		{
			_blue = "0" + Integer.toHexString(_c.getBlue()); 
		}
		else
		{
			_blue = Integer.toHexString(_c.getBlue());
		}
		
		String _green = "";
		
		if(Integer.toHexString(_c.getGreen()).length() != 2 )
		{
			_green = "0" + Integer.toHexString(_c.getGreen()); 
		}
		else
		{
			_green = Integer.toHexString(_c.getGreen());
		}
		
		
		
		_hex = "#" + _red + _green + _blue;

		return _hex;
	}
	
	public String changeColorHexToInt(String _color)
	{
		String _colorStr = "";
		
		if( _color.indexOf("#") == 0 )
		{
			_colorStr = _color.substring(1, _color.length() );
		}
		else if( _color.indexOf("0x") == 0 )
		{
			_colorStr = _color.substring(2, _color.length() );
		}
		
		if( _colorStr.equals("")==false)
		{
			try {
				_colorStr = String.valueOf( Integer.parseInt( _colorStr, 16 ) ); 
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		
		return _colorStr;
	}
	
	/**
	 * String인 borderSide 를 ArrayList로 변환. 
	 * @return ArrayList _borderSideAr
	 * 
	 */
	public ArrayList<String> getColorArrayString(ArrayList<String> _value)
	{
		ArrayList<String> _borderSideAr = new ArrayList<String>();
		
		for(String _bStr : _value )
		{
			_borderSideAr.add( changeColorToHex(Integer.parseInt(_bStr)) );
		}

		return _borderSideAr;
	}
	

	/**
	 * String인 borderSide 를 ArrayList로 변환. 
	 * @return ArrayList _borderSideAr
	 * 
	 */
	public ArrayList<String> getBorderSideToArrayList(String _bSide)
	{
		ArrayList<String> _borderSideAr = new ArrayList<String>();

		String[] _strAr = _bSide.split(",");

		for(String _bStr : _strAr )
		{
			_borderSideAr.add(_bStr);
		}

		return _borderSideAr;
	}
	
	/**
	 * String인 borderSide 를 ArrayList로 변환. 
	 * @return ArrayList _borderSideAr
	 * 
	 */
	public ArrayList<String> getBorderSideToArrayList(ArrayList<String> _bSides)
	{
		ArrayList<String> _borderSideAr = new ArrayList<String>();

		for(String _bStr : _bSides )
		{
			_borderSideAr.add(_bStr);
		}

		return _borderSideAr;
	}
	
	/**
	 * NodeMap 형태의 Property를 HashMap으로 변경 한다.
	 * @return HashMap _hsMap
	 * 
	 */
	//public HashMap<String , String> getAttrObject(NamedNodeMap _map)
	public JSONObject getAttrObject(NamedNodeMap _map)
	{
		JSONObject _hsMap = new JSONObject();

		for(int m = 0; m < _map.getLength(); m++)
		{
			Node _mNode = _map.item(m);

			String _name = _mNode.getNodeName();
			String _value = _mNode.getNodeValue();

			if(_name.indexOf("Color") != -1)
			{
				_hsMap.put(_name + "Int", _value);
				_value = changeColorToHex(Integer.parseInt(_value));
			}

			_hsMap.put(_name, _value);
		}

		return _hsMap;
	}
	
	public static JSONObject getPageBackgroundImage( Element _page, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, Object>  _param ,Function mFunction )
	{
		NodeList _list = _page.getElementsByTagName("backgroundImage");
		NodeList _properties;
		Element _pageBackgroundEl;
		Element _property;
		int _size = 0;
		JSONObject _backgroundObj = new JSONObject();
		String _url = "";
		
		if( _list.getLength() > 0 )
		{
			_pageBackgroundEl = (Element) _list.item(0);
			
			_properties = _pageBackgroundEl.getElementsByTagName("property");
			
			_size = _properties.getLength();
			
			for( int i = 0; i < _size; i++ )
			{
				_property = (Element) _properties.item(i);
				
				
				try {
					
					if( _property.getAttribute("name").equals("url") )
					{
						_backgroundObj.put(_property.getAttribute("name"), Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+  _property.getAttribute("value") );
						_url = _property.getAttribute("value");
					}
					else
					{
						_backgroundObj.put(_property.getAttribute("name"), URLDecoder.decode(_property.getAttribute("value") , "UTF-8"));
					}
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			_backgroundObj.put("pageWidth", _page.getAttribute("width"));
			_backgroundObj.put("pageHeight", _page.getAttribute("height"));
		}
		
		try {
			String _ubfx = UBIDataUtilPraser.getCanvasBackgroundFX(  _dataSet , _page, _param, mFunction);
			
			if( _ubfx.equals("") == false && _backgroundObj.containsKey("type"))
			{
				if( _backgroundObj.get("type").equals("base64"))
				{
					 _backgroundObj.put("data", _ubfx);
				}
				else if( _backgroundObj.get("type").equals("url") )
				{
					_url = _ubfx;
					_backgroundObj.put("url", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+  _ubfx  );
//					 _backgroundObj.put("url", _ubfx);
				}
			}
			
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			if(_backgroundObj.containsKey("type")){
				String _useBase64Img = common.getPropertyValue("ubform.useBackgroundBase64");
				if(_useBase64Img!=null && _useBase64Img.equals("true") && _backgroundObj.get("type").equals("url") && _url.equals("") == false )
				{
					byte[] _imageByte	=	common.getBytesRemoteImageFile( URLDecoder.decode(_url, "UTF-8") );
					String _data 		= 	common.base64_encode_byte(_imageByte);
					
					_backgroundObj.put("type", "base64");
					_backgroundObj.put("data", _data);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return _backgroundObj;
	}


//	public static JSONObject getPageBackgroundImage( PageInfoSimple _page, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, Object>  _param ,Function mFunction, DataServiceManager _oService )
	public static JSONObject getPageBackgroundImage( PageInfoSimple _page, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, HashMap<String, Object>  _param ,Function mFunction )
	{
		JSONObject _backgroundObj = _page.getBackgroundImage();
		int _size = 0;
//		JSONObject _backgroundObj = new JSONObject();
		String _url = "";
		
		if( _backgroundObj.isEmpty() == false )
		{
			_backgroundObj.put("pageWidth", _page.getWidth());
			_backgroundObj.put("pageHeight", _page.getHeight());
			_url = _backgroundObj.containsKey("originalUrl")?_backgroundObj.get("originalUrl").toString():"";
		}
		
		try {
			String _ubfx = UBIDataUtilPraser.getCanvasBackgroundFX(  _dataSet , _page, _param, mFunction);
			
			if( _ubfx.equals("") == false && _backgroundObj.containsKey("type"))
			{
				if( _backgroundObj.get("type").equals("base64"))
				{
					 _backgroundObj.put("data", _ubfx);
				}
				else if( _backgroundObj.get("type").equals("url") )
				{
					_url = _ubfx;
					_backgroundObj.put("url", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+  _ubfx  );
				}
			}
			
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			if(_backgroundObj.containsKey("type")){
				String _useBase64Img = common.getPropertyValue("ubform.useBackgroundBase64");
				if(_useBase64Img!=null && _useBase64Img.equals("true") && _backgroundObj.get("type").equals("url") && _url.equals("") == false )
				{
					
					/*
					String _contextPath = _oService.getHttpRequest().getContextPath();
					String _imageUrl = URLDecoder.decode(_url, "UTF-8");
					String _realImgPath = "";
					byte[] _imageByte = null;
					
					// backgroundImage가 http로 시작하지 않고 contextPath와 url의 시작url 이 같으면 local의 파일을 읽어들이도록 처리 
					if(_contextPath.equals("") == false && _contextPath.equals("/") == false && _imageUrl.startsWith( _contextPath+"/" ) )
					{
						_realImgPath = _imageUrl.replace(_contextPath+"/", Log.basePath );
						_imageByte = common.getBackgroundLocalImage(_realImgPath);
					}
					
					if( _imageByte == null ) _imageByte	=	common.getBytesRemoteImageFile( _imageUrl );
					
					String _data 		= 	common.base64_encode_byte(_imageByte);
					
					_backgroundObj.put("type", "base64");
					_backgroundObj.put("data", _data);
					*/
					byte[] _imageByte	=	common.getBytesRemoteImageFile( URLDecoder.decode(_url, "UTF-8") );
					String _data 		= 	common.base64_encode_byte(_imageByte);
					
					_backgroundObj.put("type", "base64");
					_backgroundObj.put("data", _data);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return _backgroundObj;
	}

	
	private ArrayList<String> getChartPropertys()
	{
		String chartPropertyStr = "seriesXField,yFieldName,yFieldDisplayName,isCrossTabData,form,gridLine,gridLineWeight,gridLIneDirection,gridLIneColor," +
				"legendDirection,legendLabelPlacement,legendMarkHeight,legendMarkWidthlegendLocation,dataLabelPostion," +
				"isCrossTabData,isDuplication,yFieldFillColor,closeField,highField,lowField,openField";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}
	
	
	/**
	 * 
	 **/
	public String getChartParamToElement( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		ArrayList<String> chartList = getChartPropertys();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		NodeList list;
		NodeList displaylist;
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";

		String returnString = "";
		
		try {
			list = (NodeList) _xpath.evaluate("./property", _element, XPathConstants.NODESET);
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				
				if( chartList.indexOf( _itemData.getAttribute("name") ) != -1  )
				{
					chartData.put(_itemData.getAttribute("name") , URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8")  );
				}
			}
			
			list = (NodeList) _xpath.evaluate("./displayName/column", _element, XPathConstants.NODESET);
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				displaylist = (NodeList) _xpath.evaluate("./property", _itemData, XPathConstants.NODESET);
				displayMap.clear();
				
				for ( j = 0; j < displaylist.getLength(); j++) {
					_displayItemData = (Element) displaylist.item(j);
					displayMap.put(_displayItemData.getAttribute("name") , URLDecoder.decode(_displayItemData.getAttribute("value") , "UTF-8") );
				}
				
				if(displayMap.containsKey("visible") && displayMap.get("visible").equals("true") )
				{
					if( yFieldsStr.equals("") == false )
					{
						yFieldsStr = yFieldsStr + "~";
					}
					if( yFieldsDisplayStr.equals("") == false )
					{
						yFieldsDisplayStr = yFieldsDisplayStr + "~";
					}
					if( yFieldsColorStr.equals("") == false )
					{
						yFieldsColorStr = yFieldsColorStr + "~";
					}
					
					yFieldsStr = yFieldsStr + displayMap.get("column");
					yFieldsDisplayStr = yFieldsDisplayStr + displayMap.get("text");
					yFieldsColorStr = yFieldsColorStr + displayMap.get("color");
				}
				
			}
			
			for ( i = 0; i < chartList.size(); i++) {
				if( returnString.equals("") == false ) returnString = returnString + ",";
				if( chartList.get(i).equals("yFieldName") )
				{
					returnString = returnString + yFieldsStr;
				}
				else if( chartList.get(i).equals("yFieldDisplayName") )
				{
					returnString = returnString + yFieldsDisplayStr;
				}
				else if( chartList.get(i).equals("yFieldFillColor") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				/*
				else if( chartList.get(i).equals("legendLabelPlacement") )
				{
					returnString = returnString + yFieldsColorStr;
				}
				*/
				else if( chartList.get(i).equals("legendMarkHeight") )
				{
					returnString = returnString + _legendMarkHeight;
				}
				else if( chartList.get(i).equals("legendMarkWidth") )
				{
					returnString = returnString + _legendMarkWeight;
				}
				else if( chartData.containsKey(chartList.get(i)))
				{
					returnString = returnString + chartData.get(chartList.get(i));
				}
				else
				{
					returnString = returnString + "";
				}
			}
			
			
		
		
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnString;
	}
	
	
	
	public HashMap<String, Object> changeElementToHashMap( Element _item)
	{
		HashMap<String, Object> _retItem = new HashMap<String, Object>();
		
		NodeList _itemList = _item.getElementsByTagName("property");
		ArrayList<String> _dataColumnList = new ArrayList<String>();
		
		for (int i = 0; i < _itemList.getLength(); i++) 
		{
			
			Element _childItem = (Element) _itemList.item(i);
			String _name = _childItem.getAttribute("name");
			String _value = _childItem.getAttribute("value");
			
			if( _childItem.getParentNode().getNodeName().equals("column") )
			{
				if( _name.equals("dataField") )
				{
					_dataColumnList.add(_value);
				}
			}
			else
			{
				_retItem.put(_name, _value);
			}
			
		}
		
		_retItem.put("dateFieldList", _dataColumnList);	// 기본 데이터 필드명 리스트
		_retItem.put("element", _item);
		
		return _retItem;
	}
	
	/**
	 * String인 borderSide 를 ArrayList로 변환. 
	 * @return ArrayList _borderSideAr
	 * 
	 */
	public ArrayList<String> getColorArrayString(String _value)
	{
		ArrayList<String> _borderSideAr = new ArrayList<String>();
		
		String[] _strAr = _value.split(",");

		for(String _bStr : _strAr )
		{
			_borderSideAr.add( changeColorToHex(Integer.parseInt(_bStr)) );
		}

		return _borderSideAr;
	}
	
	
	public ArrayList<String> getPathArrayString(String _value)
	{
		ArrayList<String> _borderSideAr = new ArrayList<String>();
		
		JSONArray _jsonObj=(JSONArray) JSONValue.parse(_value);
		
		
		Object _nextX="";
		Object _nextY="";
		
		for( int i=_jsonObj.size(); i>0 ; i-- ){
			JSONObject _jo=(JSONObject) _jsonObj.get(i-1);
			
			Object _x=_jo.get("x");
			Object _y=_jo.get("y");
			
			if( i == _jsonObj.size() ){
//				String _pathArray="["+ "L" + "," + _x.toString() + "," + _y.toString()  +"]";
				String _pathArray="L" + "," + _x.toString() + "," + _y.toString();
				_borderSideAr.add(0,_pathArray.toString());
			}else if( i == 1 ){
//				String _pathArray="["+ "M"+ "," + _x.toString() + "," + _y.toString()  +"]";
				String _pathArray="M"+ "," + _x.toString() + "," + _y.toString();
				_borderSideAr.add(0,_pathArray.toString());
			}else{
//				String _pathArray="["+ "Q"+ "," +  _x.toString() + "," +  _y.toString() + "," +  _nextX.toString() + "," +  _nextY.toString()  +"]";
				String _pathArray="Q"+ "," +  _x.toString() + "," +  _y.toString() + "," +  _nextX.toString() + "," +  _nextY.toString();
				_borderSideAr.add(0,_pathArray.toString());
			}
			_nextX=_jo.get("x");
			_nextY=_jo.get("y");
		}
		
		// M , Q , L
		// ["Q",22.849999999999994,0,22.849999999999994,0]
		// Array[ Array ] 

		return _borderSideAr;
	}
	
	public static HashMap<String, Object> checkedItemProperties( HashMap<String, Object> _item )
	{
		
		// Image일때 src값이 없을때는 false를 리턴한다. 
		if( _item.containsKey("src") && ( _item.get("src") == null || _item.get("src").toString().equals("") ) )
		{
			if(_item.containsKey("className") &&  _item.get("className").equals("UBSignature") ) return _item;
			
			if( _item.containsKey("data") )
			{
				if( _item.get("data") == null || _item.get("data").toString().equals(""))
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}
		
		return _item;
	}
	
}
