package org.ubstorm.service.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ubstorm.service.data.UDMParamSet;
import org.ubstorm.service.formatter.UBFormatter;
import org.ubstorm.service.function.Function;
import org.ubstorm.service.logger.Log;
//import org.ubstorm.service.method.ViewerInfo5;
import org.ubstorm.service.parser.formparser.ItemConvertParser;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.TempletItemInfo;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.PageInfo;
import org.ubstorm.service.parser.formparser.info.PageInfoSimple;
import org.ubstorm.service.parser.formparser.info.item.UBComponent;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CreateFormProcess {

	
	private ItemPropertyVariable mItemPropVar;
	private ItemPropertyProcess mPropertyFn;
	private ItemConvertParser mItemConvertFn;
	private UDMParamSet m_appParams;
	private String mChartDataFileName = "chartdata.dat";
	private HashMap<String,String> mImageData;
	private HashMap<String,Object> mChartData;
	private Function mFunction;
	
	private String isExportType = "";
	
	private HashMap<String, TempletItemInfo> mTempletInfo;
	
	protected HashMap<String, Object> changeItemList;
	public void setChangeItemList( HashMap<String, Object> _value)
	{
		changeItemList = _value;
		if( mItemConvertFn != null ) mItemConvertFn.setChangeItemList(_value);
	}

	public CreateFormProcess() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(UDMParamSet appParams)
	{
		mItemPropVar = new ItemPropertyVariable();
		
//		mItemPropVar.init();
		mPropertyFn = new ItemPropertyProcess();
		this.m_appParams = appParams;

		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);		
		mItemPropVar.setIsMarkAny(_isMarkAny);
		
		mItemConvertFn = new ItemConvertParser(null, mChartDataFileName, m_appParams);
		mItemConvertFn.setReportType("1");
	}
	
	public void setChartDataFileName(String file_name)
	{
		this.mChartDataFileName = file_name;
	}
	
	public void setImageData(HashMap<String,String> imgData)
	{
		this.mImageData = imgData;
	}
	
	public void setChartData(HashMap<String,Object> chartData)
	{
		this.mChartData = chartData;
	}
	
	public void setFunction( Function _function )
	{
		this.mFunction = _function;
		mItemConvertFn.setFunction(this.mFunction);
	}
	
	public String getIsExportType() {
		return isExportType;
	}

	public void setIsExportType(String isExportType) {
		this.isExportType = isExportType;
	}
	
	public HashMap<String, TempletItemInfo> getTempletInfo()
	{
		return mTempletInfo;
	}
	public void setTempletInfo(HashMap<String, TempletItemInfo> _templetInfo)
	{
		mTempletInfo = _templetInfo;
	}
	
	public ArrayList<HashMap<String, Object>> CreateFreeFormAll(Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt , int _totalPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		return CreateFreeFormAll(_page, _data, _param, _dCnt , _totalPageNum, _currentPageNum, 0, 0 );
	}
	
	
	public ArrayList<HashMap<String, Object>> CreateFreeFormAll(Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt , int _totalPageNum, int _currentPageNum , float _cloneX, float _cloneY ) throws UnsupportedEncodingException, ScriptException
	{
		ArrayList<HashMap<String, Object>> _objects = new ArrayList<HashMap<String, Object>>();
		NodeList _child = _page.getElementsByTagName("item");
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		
		
		int _minimumResizeFontSize = 0;
		if( _page.hasAttribute("minimumResizeFontSize") && StringUtil.isInteger(_page.getAttribute("minimumResizeFontSize")) )
		{
			_minimumResizeFontSize = Integer.valueOf(_page.getAttribute("minimumResizeFontSize"));
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		// xml Item
		for(int j = 0; j < _child.getLength() ; j++)
		{
			Element _childItem = (Element) _child.item(j);

			String _itemId = _childItem.getAttribute("id");
			String _className = _childItem.getAttribute("className");

			Boolean _bType = false;

			if(_className.equals("UBLabelBorder"))
			{
				_bType = true;
			}
			else
			{
				_bType = false;
			}

			NodeList _propertys = _childItem.getElementsByTagName("property");
			NodeList _ubfunction = _childItem.getElementsByTagName("ubfunction");
			HashMap<String, Object> _propList = new HashMap<String, Object>();

			String _dataTypeStr = "";
			String _dataColumn = "";
			String _dataID = "";
			String _model_type = "";
			String _barcode_type = "";
			
			// formatter variables
			String _formatter="";
			String _nation="";
			String _align="";
			String _dataType="";
			String _mask="";
			
			int _decimalPointLength=0;
			Boolean _useThousandComma=false;
			Boolean _isDecimal=false;
			String _formatString="";
			
			// image variable
			String _prefix="";
			String _suffix="";
			
			if( _className.equals("UBTable") || _className.equals("UBApproval") )
			{
				try {
					mItemConvertFn.convertElementTableToItem(_childItem , _dCnt , _data , _param, _cloneX, _cloneY, -1,_objects, _totalPageNum, _currentPageNum, false);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				_propList = mItemConvertFn.convertElementToItem(_childItem, _dCnt, _data, _param, _cloneX, _cloneY, -1, _totalPageNum, _currentPageNum, false);
				if(_propList != null )
				{
					_propList.put("className" , _className );
					_propList.put("id" , _itemId );
					
					_objects.add(_propList);
				}
			}

		}
		
		return _objects;
	}
	
	

	@SuppressWarnings({ "unchecked", "unused" })
	public ArrayList<HashMap<String, Object>> CreateFreeForm( DataSetProcess _dataSetFn, Element _page , HashMap<String, Element> _dataSetItems , HashMap<String, Object> _param , int rowStartIndex, int fetchRowCount , HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet , int _totalPageNum ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		
		ArrayList<HashMap<String, Object>> _objects = new ArrayList<HashMap<String, Object>>();
		NodeList _child = _page.getElementsByTagName("item");
		
		boolean bLoadDataSet = false;
		ArrayList<HashMap<String, Object>> _list = null;
		
		String _cDataId = "";
		
		int _minimumResizeFontSize = 0;
		if( _page.hasAttribute("minimumResizeFontSize") && StringUtil.isInteger(_page.getAttribute("minimumResizeFontSize")) )
		{
			_minimumResizeFontSize = Integer.valueOf(_page.getAttribute("minimumResizeFontSize"));
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		// xml Item
		for(int j = 0; j < _child.getLength() ; j++)
		{
			Element _childItem = (Element) _child.item(j);

			String _itemId = _childItem.getAttribute("id");
			String _className = _childItem.getAttribute("className");

			Boolean _bType = false;

			if(_className.equals("UBLabelBorder"))
			{
				_bType = true;
			}
			else
			{
				_bType = false;
			}

			//_className = "borderLabel";

			NodeList _propertys = _childItem.getElementsByTagName("property");
			NodeList _ubfunction = _childItem.getElementsByTagName("ubfunction");
			HashMap<String, Object> _propList = new HashMap<String, Object>();

			String _dataTypeStr = "";
			String _dataColumn = "";
			String _dataID = "";
			String _model_type = "";
			String _barcode_type = "";
			
			// system function
			String _systemFunction="";
			String _dataTye="";
			
			// formatter variables
			String _formatter="";
			String _nation="";
			String _align="";
			String _dataType="";
			String _mask="";
			
			int _decimalPointLength=0;
			Boolean _useThousandComma=false;
			Boolean _isDecimal=false;
			String _formatString="";
			
			// image variable
			String _prefix="";
			String _suffix="";

			String[] _datasets={"",""};
			
			_propList = mItemPropVar.getItemName(_className);

			// xml Item propertys
			for(int p = 0; p < _propertys.getLength(); p++)
			{
				Element _propItem = (Element) _propertys.item(p);

				String _name = _propItem.getAttribute("name");
				String _value = _propItem.getAttribute("value");
				//					if( mItemPropVar.equals(_className) )
				//					{
				if(_propList.containsKey(_name))
				{
					if( _name.equals("fontFamily"))
					{
						_value = URLDecoder.decode(_value, "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.indexOf("Color") != -1)
					{
						_propList.put((_name + "Int"), _value);
						_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value));
						//								_value = changeColorToRGB(Integer.parseInt(_value));
						_propList.put(_name, _value);
					}
					else if( _name.equals("lineHeight"))
					{
						//_value = "1.16"; //TODO LineHeight Test
						_value = _value.replace("%25", "");
						_value = String.valueOf((Float.parseFloat(_value)/100));		
						_propList.put(_name, _value);
					}
					else if( _name.equals("borderType"))
					{
						_propList.put(_name, _value);
					}
					else if( _name.equals("text"))
					{
						_value = URLDecoder.decode(_value, "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.equals("borderSide"))
					{
						ArrayList<String> _bSide = new ArrayList<String>();
						_value = URLDecoder.decode(_value, "UTF-8");
						if( !_value.equals("none") )
						{
							_bSide = mPropertyFn.getBorderSideToArrayList(_value);

							if( _bSide.size() > 0)
							{
								String _type = (String) _propList.get("borderType");
								_type = mPropertyFn.getBorderType(_type);
								_propList.put("borderType", _type);
							}

						}

						_propList.put(_name, _bSide);
					}
					else if( _name.equals("x"))
					{
						_propList.put(_name, _value);
						_propList.put("left", _value);
					}
					else if( _name.equals("y"))
					{
						_propList.put(_name, _value);
						_propList.put("top", _value);
					}
					else if( _name.equals("type") )
					{
						_model_type = _value;
						_propList.put(_name, _value);
					}
					else if( _name.equals("barcodeType") )
					{
						_barcode_type = _value;
						_propList.put(_name, _value);
					}
					else
					{
						_propList.put(_name, _value);
					}
				}
				else if(_name.equals("dataType"))
				{
					_dataTye=_value;
					
					if( _value.equals("1") )
					{
						if(!bLoadDataSet) // 명우 DataSet id가 다를경우 체크.
						{
							bLoadDataSet = true;
							
							//ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
							Element _udm = _dataSetItems.get(_dataID);
							//HashMap<String, Object> _dataHm = _list.get(_dCnt);
							
							HashMap<String, HashMap<String, String>> _datasetParam = new HashMap<String, HashMap<String, String>>();
							
							for(Entry<String, Object> entry : _param.entrySet()) {
								String key = entry.getKey();
								HashMap<String, String> value = (HashMap<String, String>) entry.getValue();
								_datasetParam.put(key, value);
							}
						
							_list = _dataSetFn.dataSetLoadData(_udm, _datasetParam, rowStartIndex, fetchRowCount);
						}
						
						HashMap<String, Object> _dataHm = _list != null && _list.size() > 0 ? _list.get(0) : new HashMap<String, Object>();
						
						Object _dataValue = _dataHm.get(_dataColumn);
						
						_propList.put("text", _dataValue == null ? "" : _dataValue);
						
						// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
						_propList.put("tooltip", _propList.get("text"));
					}
					else if( _value.equals("3"))
					{
						String _txt = _propList.get("text").toString();
						int _inOf = _txt.indexOf("{param:");
						String _pKey = "";
						if( _inOf != -1 )
						{
							_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);

							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

							String _pValue = _pList.get("parameter");

							if( _pValue.equals("undefined"))
							{
								_propList.put("text", "");
							}
							else
							{
								_propList.put("text", _pValue);
							}

						}
						else
						{
							_propList.put("text", "");
						}
					}
				}
				
				else if( _name.equals("data") )
				{
					if(_className.equals("UBImage"))
					{
						// test
						/*
						String projName = m_appParams.getREQ_INFO().getPROJECT_NAME();
						String formName = m_appParams.getREQ_INFO().getFORM_ID();
						
						String	_url = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getImageData&PROJECT_NAME=" + projName + "&FORM_ID=" + formName + "&ITEM_ID="+_itemId;
						_propList.put("src", _url);

						if(!mImageData.containsKey(_itemId))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							mImageData.put(_itemId, _value);
						}	
						*/
						
						_value = URLDecoder.decode(_value, "UTF-8");
						_propList.put("src",  URLEncoder.encode(_value, "UTF-8"));
					}
				}


				else if( _name.equals("prefix") ){
					_prefix=URLDecoder.decode(_value, "UTF-8");
				}

				else if( _name.equals("suffix") ){
					//_suffix=URLDecoder.decode(_value, "UTF-8");
					_suffix=_value;
				}
				
				else if( _name.equals("systemFunction") ){
					_systemFunction = _value.toString();
				}

				else if(_name.equals("column"))
				{
					_dataColumn = _value;
				}
				else if(_name.equals("dataSet"))
				{
					if( !_value.equals("null") )
					{
/*
						if( !_cDataId.equals(_value) )
						{
							bLoadDataSet = false;
							_cDataId = _value; 
						}
*/						
						_dataID = _value;
					}
				}
				else if(_name.equals("dataSets"))
				{
					if( !_value.equals("null") )
					{
						_datasets = _value.split("%2C");
					}
				}
				else if(_name.equals("startPoint"))
				{

					_value = URLDecoder.decode(_value, "UTF-8");
					String[] _sPoint = _value.split(",");

					_propList.put("x1", _sPoint[0]);
					_propList.put("y1", _sPoint[1]);
				}
				else if(_name.equals("endPoint"))
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					String[] _ePoint = _value.split(",");

					_propList.put("x2", _ePoint[0]);
					_propList.put("y2", _ePoint[1]);
				}
				else if(_name.equals("lineThickness"))
				{
					_propList.put("thickness", _value);
				}
				else if(_name.equals("borderThickness"))
				{
					_propList.put("borderWidth", _value);
				}
				else if(_name.equals("formatter"))
				{
					_formatter = _value;
				}

				else if(_name.equals("nation"))
				{
					_nation = URLDecoder.decode(_value, "UTF-8");
				}

				else if(_name.equals("align"))
				{
					_align=_value;
				}
				else if( _name.equals("mask") ){
					_mask = URLDecoder.decode(_value, "UTF-8");
				}

				else if( _name.equals("decimalPointLength") ){

					_decimalPointLength = Integer.parseInt(_value);
				}				

				else if( _name.equals("useThousandComma") ){
					_useThousandComma = Boolean.parseBoolean(_value);
				}		
				else if( _name.equals("isDecimal") ){
					_isDecimal = Boolean.parseBoolean(_value);
				}		
				else if( _name.equals("formatString") ){
					_formatString = _value;
				}	

				//					}
				//					log.debug(getClass().getName() + "::" + "Item = Propertys >>>>> " + _name + " == " + _value);
			}
			
			
			if( _dataTye.equalsIgnoreCase("2") ){

				try {
					_systemFunction = URLDecoder.decode(_systemFunction, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
					mFunction.setDatasetList(_dataSet);
					
					String _fnValue;
					
					if( mFunction.getFunctionVersion().equals("2.0") ){
						_fnValue = mFunction.testFN(_systemFunction,rowStartIndex,_totalPageNum,rowStartIndex,-1,-1, "" );
					}else{
						_fnValue = mFunction.function(_systemFunction,rowStartIndex,_totalPageNum,rowStartIndex,-1,-1);
					}
					
					_propList.put("text", _fnValue == null ? "" : _fnValue);
				}
			}
			
			// UBI FX 
			for(int _ubfxIndex = 0; _ubfxIndex < _ubfunction.getLength(); _ubfxIndex++)
			{
//				Element _ubfxItem = (Element) _ubfunction.item(_ubfxIndex);
//
//				String _name = _ubfxItem.getAttribute("property");
//				String _value = _ubfxItem.getAttribute("value");
//				
//				_value = URLDecoder.decode(_value, "UTF-8");
//				ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
//				String _fnValue = UBFunction.fnTest(_value, _data, _list, _dataColumn ,_dCnt);
//				_propList.put(_name, _fnValue);
//				System.out.println( "ubfx visible= "+ _propList.get("visible") );
			}
			
			if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
				String _formatValue="";
				if( _dataType.equalsIgnoreCase("1") ){
//					ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
//					HashMap<String, Object> _dataHm = _list.get(_dCnt);
//					Object _dataValue = _dataHm.get(_dataColumn);
//					_formatValue = _dataValue.toString();
				}else{
					_formatValue = _propList.get("text").toString();
				}
				try {
					
					if( _formatter.equalsIgnoreCase("Currency") ){
						_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					}else if( _formatter.equalsIgnoreCase("Date") ){
						_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
						_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					}else if( _formatter.equalsIgnoreCase("MaskString") ){
						_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
					}
				} catch (ParseException e) {
//					e.printStackTrace();
				}
				_propList.put("text", _formatValue);
			}
			
			///////;
			if(_className.equals("UBImage") ){
				if( _dataType.equals("1") )
				{
					String projName = m_appParams.getREQ_INFO().getPROJECT_NAME();
					String formName = m_appParams.getREQ_INFO().getFORM_ID();
					
					HashMap<String, Object> _dataHm = _list != null && _list.size() > 0 ? _list.get(0) : new HashMap<String, Object>();
					Object _dataValue = _dataHm.get(_dataColumn);
					
					String _url="";
					String _txt = _dataValue.toString();
					_url= _prefix + _txt + _suffix;

					String	_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+_url;
					
					_propList.put("src", _servicesUrl);
				}
			}			

			if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
			{
				int _itmWidth = Integer.valueOf(_propList.get("width").toString());
				int _itmheight = Integer.valueOf(_propList.get("height").toString());
				
				if(_className.equals("UBQRCode"))
				{
					_propList.put("type" , "qrcodeSvgCtl");
					//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _propList.get("text").toString());
					
					// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
					String _barcodeValue = "";
					{
						//ViewerInfo5 vi5 = new ViewerInfo5();
				    	String SHOW_LABEL = "false";
				    	String IMG_TYPE = "qrcode";
				    	String MODEL_TYPE = _barcode_type;
				    	String FILE_CONTENT = _propList.get("text").toString();
				    	
				    	try {
							//_barcodeValue = vi5.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
							_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//_barcodeValue = URLDecoder.decode(_barcodeValue, "UTF-8");
					_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
				}
				else
				{
					boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
					
					String _barcodeData = _propList.get("text").toString();
					String _barcodeSrc;
					if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
						_barcodeSrc="";
					}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 7 ){
						_barcodeSrc="";
					}else if( _barcode_type.equalsIgnoreCase("upc") && _barcodeData.length() != 11 ){
						_barcodeSrc="";
					}
					/*
					else if( _barcode_type.equalsIgnoreCase("itf14") && _barcodeData.length() != 14 ){
						_barcodeSrc="";
					}
					*/
					else
					{
						if(StringUtil.containsKorean(_barcodeData))
						{
							_barcodeSrc="";
						}
						else
						{
							if("datamatrix".equals(_barcode_type))
							{	
								_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
							}
							_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
						}
					}
					//_propList.put("src" , _barcodeSrc );
					
					// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
					String _barcodeValue = "";
					if(!"".equals(_barcodeSrc))
					{
						//ViewerInfo5 vi5 = new ViewerInfo5();
				    	String SHOW_LABEL = _showLabel ? "true" : "false";
				    	String IMG_TYPE = "barcode";
				    	String MODEL_TYPE = _barcode_type;
				    	String FILE_CONTENT = _barcodeData;
				    	
				    	try {
				    		if("datamatrix".equals(MODEL_TYPE))
							{	
				    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
							}
							//_barcodeValue = vi5.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
							_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//_barcodeValue = URLDecoder.decode(_barcodeValue, "UTF-8");
					_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
				}		
			}
			else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart") )
			{
				String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
				String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
				/*
							String _seriesXField = null;
							String[] _yFieldName = null;
							String[] _yFieldDisplayName = null;
							boolean _crossTab = false; 
							String _form = "segment";
							boolean _gridLIne = true; 
							int _gridLineWeight = 1;
							String _gridLIneDirection = "both";
							int _gridLIneColor = 0xd8d3d3;
							String _legendDirection = "vertical";
							String _legendLabelPlacement = "right";
							int _legendMarkHeight = 10;
							int _legendMarkWeight = 10;
							String _legendLocation = "bottom";
							String _dataLabelPosition = "inside";
							boolean _DubplicateAllow = false; 
							int [] _yFieldFillColor = null;
							String _seriexCloseField = null;
							String _seriesHighField = null;
							String _seriesLowField = null;
							String _seriesOpenField = null;
				 */
				String IMG_TYPE = "";
				String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
				
				HashMap<Integer, String> displayNamesMap=null;
				
				PARAM = mPropertyFn.getChartParamToElement(_childItem);
				if(_className.equals("UBPieChart"))
				{
					_propList.put("type" , "pieChartCtl");
					IMG_TYPE = "pie";
				}
				else if(_className.equals("UBLineChart"))
				{
					_propList.put("type" , "lineChartCtl");
					IMG_TYPE = "line";
				}
				else if(_className.equals("UBBarChart"))
				{
					_propList.put("type" , "barChartCtl");
					IMG_TYPE = "bar";
				}
				else if(_className.equals("UBColumnChart"))
				{
					_propList.put("type" , "columnChartCtl");
					IMG_TYPE = "column";
				}
				else if(_className.equals("UBAreaChart"))
				{
					_propList.put("type" , "areaChartCtl");
					IMG_TYPE = "area";
				}
				else if(_className.equals("UBCombinedColumnChart"))
				{
					displayNamesMap  = getChartParamToElement2(_childItem );
					_propList.put("type" , "combinedColumnChartCtl");
					IMG_TYPE = "combcolumn";
				}
				else if(_className.equals("UBBubbleChart"))
				{
					_propList.put("type" , "bubbleChartCtl");
					IMG_TYPE = "bubble";
				}
				
				String _chartValue = "";
				if(IMG_TYPE.equals("combcolumn"))
				{
					String [] arrDataId = _datasets;
					
					ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
					
					for(int i=0; i< arrDataId.length; i++)
					{
						ArrayList _alllist = new ArrayList();
						if(!mChartData.containsKey(arrDataId[i]) && _list != null && _list.size() > 0)
						{
							Element _udm = _dataSetItems.get(arrDataId[i]);
							HashMap<String, HashMap<String, String>> _datasetParam = new HashMap<String, HashMap<String, String>>();
							
							for(Entry<String, Object> entry : _param.entrySet()) {
								String key = entry.getKey();
								HashMap<String, String> value = (HashMap<String, String>) entry.getValue();
								_datasetParam.put(key, value);
							}
						
							_alllist = _dataSetFn.dataSetLoadData(_udm, _datasetParam, -1, -1);
						}		
						
						_dslist.add(_alllist);
					}
					
					//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _model_type + "&PARAM=" + PARAM + "&FILE_NAME=" + this.mChartDataFileName + "&PROJECT_NAME=" + PROJECT_NAME + "&FORM_ID=" + FOLDER_NAME + "&DATASET=" + _dataID );
					
					if(!"".equals(IMG_TYPE))
					{
						//ViewerInfo5 vi5 = new ViewerInfo5();
						String FILE_NAME = this.mChartDataFileName;
						//String DATA_ID = _dataID;
						String MODEL_TYPE = _model_type;
						int _itmWidth = Integer.valueOf(_propList.get("width").toString());
						int _itmheight = Integer.valueOf(_propList.get("height").toString());
						
				    	try {
				    		//_chartValue = vi5.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
				    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else
				{
					ArrayList _alllist = new ArrayList();
					if(!mChartData.containsKey(_dataID) && _list != null && _list.size() > 0)
					{
						Element _udm = _dataSetItems.get(_dataID);
						HashMap<String, HashMap<String, String>> _datasetParam = new HashMap<String, HashMap<String, String>>();
						
						for(Entry<String, Object> entry : _param.entrySet()) {
							String key = entry.getKey();
							HashMap<String, String> value = (HashMap<String, String>) entry.getValue();
							_datasetParam.put(key, value);
						}
					
						_alllist = _dataSetFn.dataSetLoadData(_udm, _datasetParam, -1, -1);
						//mChartData.put(_dataID, _alllist);
					}		
					
					//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _model_type + "&PARAM=" + PARAM + "&FILE_NAME=" + this.mChartDataFileName + "&PROJECT_NAME=" + PROJECT_NAME + "&FORM_ID=" + FOLDER_NAME + "&DATASET=" + _dataID );
					
					if(!"".equals(IMG_TYPE))
					{
						//ViewerInfo5 vi5 = new ViewerInfo5();
						String FILE_NAME = this.mChartDataFileName;
						//String DATA_ID = _dataID;
						String MODEL_TYPE = _model_type;
						int _itmWidth = Integer.valueOf(_propList.get("width").toString());
						int _itmheight = Integer.valueOf(_propList.get("height").toString());
						
				    	try {
				    		//_chartValue = vi5.getLocalChartImageToBase64(_alllist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
				    		_chartValue = common.getLocalChartImageToBase64(_alllist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				//_barcodeValue = URLDecoder.decode(_chartValue, "UTF-8");
				_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
			}
			
			// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
			if(_propList.containsKey("text"))
			{
				String _value = _propList.get("text").toString();
				_propList.put("tooltip", _value);
			}

			_propList.put("className" , _className );
			
			//_propList.put("type" , _className );
			_propList.put("id" , _itemId );

			_objects.add(_propList);

		}
		
		return _objects;
	}

	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> CreateLabelBandAll(Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt , int _totalPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		return CreateLabelBandAll(_page , _data ,_param , _dCnt , _totalPageNum, _currentPageNum, 0, 0 );
	}
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> CreateLabelBandAll(Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt , int _totalPageNum, int _currentPageNum, float _cloneX, float _cloneY ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		
		// return ArrayList
		ArrayList<HashMap<String, Object>> _objects = new ArrayList<HashMap<String, Object>>();
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		
		// Page 의 Item list
		NodeList _child = _page.getElementsByTagName("item");
		
		// DataSet 담는 객채
		ArrayList<HashMap<String, Object>> _list = new ArrayList<HashMap<String,Object>>();
		
		int _minimumResizeFontSize = 0;
		if( _page.hasAttribute("minimumResizeFontSize") && StringUtil.isInteger(_page.getAttribute("minimumResizeFontSize")) )
		{
			_minimumResizeFontSize = Integer.valueOf(_page.getAttribute("minimumResizeFontSize"));
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		// Data 건수 조회
		int _dataMaxCnt = 0;
		XPath _xpath = XPathFactory.newInstance().newXPath();
		Element _propDataSet;
		try {
			NodeList _dNodeList = (NodeList) _xpath.evaluate("//property[@name='dataSet'][@value != 'null'][@value != '']", _page, XPathConstants.NODESET);
			for (int i = 0; i < _dNodeList.getLength(); i++) {
				_propDataSet = (Element) _dNodeList.item(i);
				String _value = _propDataSet.getAttribute("value");
				
				_list = _data.get(_value);
				
				if( _list.size() > _dataMaxCnt)
				{
					_dataMaxCnt = _list.size();
				}
				
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Label Band Property 담기.
		HashMap<String, String> _labelProperty = new HashMap<String, String>();
		for (int i = 0; i < _child.getLength(); i++) 
		{
			Element _labelBand = (Element) _child.item(i);
			
			if( _labelBand.getAttribute("className").equals("UBLabelBand"))
			{
				NodeList _labelProps = _labelBand.getElementsByTagName("property");
			
				for (int j = 0; j < _labelProps.getLength(); j++) 
				{
					Element _labelProp = (Element) _labelProps.item(j);
					
					String _lName = _labelProp.getAttribute("name");
					String _lValue = _labelProp.getAttribute("value");
					
					_labelProperty.put(_lName, _lValue);
					
				}
				
			}
			else
			{
				if( _labelProperty.size() > 0 )
				{
					break;
				}
			}
		}
		
		// 가로, 세로 갯수 확인후 Page에 들어가는 총 Band 갯수 구하기.
		int _columns = Integer.valueOf(_labelProperty.get("columns")); 
		int _rows = Integer.valueOf(_labelProperty.get("rows"));
		
		int _pageMaxCnt = (_columns * _rows) * (_dCnt + 1);
		
		// 반복하는 Item의 좌표 및 방향
		float _bandX = Float.valueOf(_labelProperty.get("border_x"));
		float _bandY = Float.valueOf(_labelProperty.get("border_y"));
		float _bandWidth = Float.valueOf(_labelProperty.get("border_width"));
		float _bandHeight = Float.valueOf(_labelProperty.get("border_height"));
		
		String _bandDirection = _labelProperty.get("direction");
		
		
		// Data 시작 Number 구하기 , 증가할 dataNum 선언.
		int _dataStNum = 0;
		
		_dataStNum = (_columns * _rows) * _dCnt;
		
		int _pageStartIndex = 0;
		
		// param의 startIndex값이 잇을경우 _pageMaxCnt값과 _dataStNum에서 -처리 시작 bandIndex값을 _dataStNum값이 이 0일경우 startIndex값으로 셋팅 
//		if(_param.containsKey(GlobalVariableData.UB_LABEL_START_INDEX))
		if( _param.containsKey(GlobalVariableData.UB_LABEL_ST_INDEX) || ( _param.containsKey(GlobalVariableData.UB_LABEL_COL_INDEX) && _param.containsKey(GlobalVariableData.UB_LABEL_ROW_INDEX) ) )
		{
			HashMap<String, String> _pList;
			int _stColIndex = 0;
			int _stRowIndex = 0;
			int _stIndex = 0;
			
			if(_param.containsKey(GlobalVariableData.UB_LABEL_ST_INDEX) == false)
			{
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_COL_INDEX);
				_stColIndex = Integer.valueOf( _pList.get("parameter") );
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_ROW_INDEX);
				_stRowIndex = Integer.valueOf( _pList.get("parameter") );
				
				if( _stColIndex > 0 && _stColIndex <= _columns &&  _stRowIndex > 0 && _stRowIndex <= _rows )
				{
					if("downCross".equals(_labelProperty.get("direction"))) _pageStartIndex = (_rows * (_stColIndex-1)) + _stRowIndex -1;
					else _pageStartIndex = (_columns * (_stRowIndex-1)) + _stColIndex -1;
				}
				else
				{
					_pageStartIndex = 0;
				}
				
			}
			else
			{
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_ST_INDEX);
				_stIndex = Integer.valueOf( _pList.get("parameter") );
				
				if( _stIndex > 0 )
				{
					_pageStartIndex = _stIndex;
				}
				else
				{
					_pageStartIndex = 0;
				}
			}
				
			
			if(_pageStartIndex > 0 )
			{
				_pageMaxCnt = _pageMaxCnt - _pageStartIndex;
				if( _dataStNum > 0 )
				{
					_dataStNum = _dataStNum - _pageStartIndex;
					
					if(_dataStNum < 0 )
					{
						_dataStNum = 0;
					}
				}
				
				_pageStartIndex = _pageStartIndex - ((_columns * _rows) * _dCnt);
				if(_pageStartIndex < 0 ) _pageStartIndex = 0;
			}
			
		}
		
		if( _pageMaxCnt > _dataMaxCnt)
		{
			_pageMaxCnt = _dataMaxCnt;
		}
		
		
		// xml Item Parsing
		for (int t = 0; t < _child.getLength(); t++) 
		{
			Element _childItem = (Element) _child.item(t);
			
			String _id = _childItem.getAttribute("id");
			String _className = _childItem.getAttribute("className");
			
			HashMap<String, Object> _propList = new HashMap<String, Object>();
			// 해당 Item의 Property 확인.
			
			NodeList _propertys = _childItem.getElementsByTagName("property");
			NodeList _ubfunction = _childItem.getElementsByTagName("ubfunction");
			
			// dataset 정보 담는 객채
			String _dataTypeStr = "";
			String _datasetColumn = "";
			String _datasetId = "";
			
			// formatter variables
			String _formatter="";
			String _nation="";
			String _align="";
			String _dataType="";
			String _mask="";
			
			int _decimalPointLength=0;
			Boolean _useThousandComma=false;
			Boolean _isDecimal=false;
			String _formatString="";
			String _systemFunction = "";

			ArrayList<HashMap<String, Object>> _arrayList = new ArrayList<HashMap<String, Object>>();
			
			if( _className.equals("UBTable") || _className.equals("UBApproval") )
			{
				try {
					mItemConvertFn.convertElementTableToItem(_childItem , _dCnt , _data , _param, 0, 0, -1,_arrayList, _totalPageNum, _currentPageNum, true);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				_propList = mItemConvertFn.convertElementToItem(_childItem, _dCnt, _data, _param, 0, 0, -1, _totalPageNum, _currentPageNum, true);
				
				if( _propList == null )
				{
					continue;
				}
				
				if(_propList.get("className").equals("UBLabelBand"))
				{
					if( _labelProperty.get("borderVisible").equals("true"))
					{
						_propList.put("x", _labelProperty.get("border_x"));
						_propList.put("y", _labelProperty.get("border_y"));
						_propList.put("width", _labelProperty.get("border_width"));
						_propList.put("height", _labelProperty.get("border_height"));

					}
					else
					{
						_propList.clear();
					}
				}
//				else
//				{
//					_propList.put("x", _propList.get("band_x"));
//					_propList.put("y", _propList.get("band_y"));
//				}
				
			}
			
			if( _propList.isEmpty() == true && _arrayList.size() == 0)
			{
				continue;
			}
			
			
			if( _arrayList.size() == 0 )
			{
				_arrayList.add(_propList);
			}
			
			HashMap<String, Object> _labelItem = new HashMap<String, Object>();

			
			for (int i = 0; i < _arrayList.size(); i++) 
			{
				HashMap<String, Object> _arItem = _arrayList.get(i);
				
				NodeList formatterItem = null;
				
				if(_arItem.containsKey("band_x")) _arItem.put("x", _arItem.get("band_x"));
				if(_arItem.containsKey("band_y")) _arItem.put("y", _arItem.get("band_y"));

				
				boolean _useFormatElement = false;
				
				if( _arItem.containsKey("formatterElement") )
				{
					// 포맷터 값이존재할경우각각의 데이터를 담는 처리
					formatterItem =(NodeList) _arItem.get("formatterElement");
					if(formatterItem.getLength() > 0 ) _useFormatElement = true;
					
					_arItem.remove("formatterElement");
				}
				
				if( !_useFormatElement )
				{
					formatterItem = _childItem.getElementsByTagName("formatter");
				}
				
				if( formatterItem != null && formatterItem.getLength() > 0 )
				{
					NodeList formatterProperty = null;
					
					if( _useFormatElement )
					{
						formatterProperty = formatterItem;
					}
					else
					{
						formatterProperty = ((Element) formatterItem.item(0)).getElementsByTagName("property");
					}
					
					if(_arItem.containsKey("formatter")) _formatter = _arItem.get("formatter").toString();
					
					int propertySize = formatterProperty.getLength();
					for (int p = 0; p < propertySize; p++) {
						
						Element _propItem = (Element) formatterProperty.item(p);

						String _name = _propItem.getAttribute("name");
						String _value = _propItem.getAttribute("value");
						
						if( _name.equals("mask") ){
							_mask = URLDecoder.decode(_value, "UTF-8");
						}
						else if( _name.equals("decimalPointLength") ){
							
							_decimalPointLength = Integer.parseInt(_value);
						}				
						else if( _name.equals("useThousandComma") ){
							_useThousandComma = Boolean.parseBoolean(_value);
						}		
						else if( _name.equals("isDecimal") ){
							_isDecimal = Boolean.parseBoolean(_value);
						}		
						else if( _name.equals("formatString") ){
							_formatString = _value;
						}
					}
				}
				
				
//				int _bandIdx = 0;
				int _bandIdx = _pageStartIndex;
				for (int d = _dataStNum; d < _pageMaxCnt; d++) 
				{
					_labelItem = (HashMap<String, Object>) _arItem.clone();

					_dataTypeStr = String.valueOf(_labelItem.get("dataType"));
					_datasetId = String.valueOf(_labelItem.get("dataSet"));
					_datasetColumn = String.valueOf(_labelItem.get("column"));
					
					if( _dataTypeStr.equals("1") )
					{
						_list = _data.get(_datasetId);
						if( d >= _list.size()) 
						{
							_labelItem.put("text", "");
							_labelItem.put("tooltip", "");
							continue;
						}
						HashMap<String, Object> _dataHm = _list.get(d);

						Object _dataValue = _dataHm.get(_datasetColumn);

						_labelItem.put("text", _dataValue == null ? "" : _dataValue);

						// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
						_labelItem.put("tooltip", _dataValue == null ? "" : _dataValue);
						
						if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") )
						{
							mItemConvertFn.changeLabelBandImgItem(_labelItem, _childItem, _data);
						}
					}
					else if(  _dataTypeStr.equals("2") )
					{
						if( _labelItem.containsKey("systemFunction") )
						{
							_systemFunction = _labelItem.get("systemFunction").toString();
							
							if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
								mFunction.setDatasetList(_data);
								
								String _fnValue;
								
								if( mFunction.getFunctionVersion().equals("2.0") ){
									_fnValue = mFunction.testFN(_systemFunction,d, _totalPageNum , _currentPageNum,-1,-1, "" );
								}else{
									_fnValue = mFunction.function(_systemFunction,d, _totalPageNum , _currentPageNum,-1,-1);
								}
								
								_labelItem.put("text", _fnValue);
							}
						}
						
					}
					else if( _dataTypeStr.equals("3"))
					{
						String _txt = _labelItem.get("text").toString();
						int _inOf = _txt.indexOf("{param:");
						String _pKey = "";
						if( _inOf != -1 )
						{
							_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);

							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

							String _value = _pList.get("parameter");

							if( _value.equals("undefined"))
							{
								_labelItem.put("text", "");
							}
							else
							{
								_labelItem.put("text", _value);
							}

						}
						else
						{
							_labelItem.put("text", _txt);
						}
						
						if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart"))
						{
							mItemConvertFn.changeLabelBandImgItem(_labelItem, _childItem, _data);
						}
					}

					for(int _ubfxIndex = 0; _ubfxIndex < _ubfunction.getLength(); _ubfxIndex++)
					{
						Element _ubfxItem = (Element) _ubfunction.item(_ubfxIndex);
						String _name = _ubfxItem.getAttribute("property");
						String _value = _ubfxItem.getAttribute("value");
						_value = URLDecoder.decode(_value, "UTF-8");

						mFunction.setDatasetList(_data);
						
						String _fnValue;
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = mFunction.testFN(_value, d, 0, _dCnt,-1,-1, "" );
						}else{
							_fnValue = mFunction.function(_value, d, 0, _dCnt,-1,-1);
						}
						
						
						_fnValue = _fnValue.trim();
						
						_labelItem.put(_name, _fnValue);
					}

					if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
						String _formatValue="";
						_formatValue = _labelItem.get("text").toString();
						try {
							if( _formatter.equalsIgnoreCase("Currency") ){
								_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
							}else if( _formatter.equalsIgnoreCase("Date") ){
								_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
							}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
								_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
							}else if( _formatter.equalsIgnoreCase("MaskString") ){
								_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
							}
						} catch (ParseException e) {
//							e.printStackTrace();
						}
						_labelItem.put("text", _formatValue);
					}




					float _itemX = 0;
					float _itemY = 0;


					if( _bandDirection.equals("crossDown")) // 아래로
					{
						_itemX = (_bandWidth * (_bandIdx % _columns) ) + (((_bandIdx % _columns)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString()));
						_itemY = (float) ((_bandHeight * ( Math.floor(_bandIdx / _columns))) + ((Math.floor(_bandIdx / _columns)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString())));

					}
					else // 옆으로
					{

						_itemX = (float) ((_bandWidth * (Math.floor(_bandIdx / _rows)) ) + ((Math.floor(_bandIdx / _rows)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString())));
						_itemY = (_bandHeight * (_bandIdx % _rows) ) + (((_bandIdx % _rows)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString()));

					}
					_labelItem.put("x",  _itemX + _cloneX );
					_labelItem.put("y", _itemY + _cloneY );	
					_labelItem.put("top", _itemY + _cloneY );	
					_labelItem.put("left", _itemX + _cloneX );	

					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					if(_labelItem.containsKey("text"))
					{
						String _value = _labelItem.get("text").toString();
						_labelItem.put("tooltip", _value);
					}
					
					if( _labelItem.containsKey("visible") == false ||  "false".equals( _labelItem.get("visible") ) != true) _objects.add(_labelItem);
					_bandIdx++;
				} // for _pageMaxCnt

			}// for arraylist.size();
			
		} // for _child.getLength()
		
		return _objects;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> CreateLabelBandAll(PageInfo _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt , int _totalPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		return CreateLabelBandAll( _page , _data , _param , _dCnt , _totalPageNum, _currentPageNum, 0, 0);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> CreateLabelBandAll(PageInfo _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt , int _totalPageNum, int _currentPageNum, float _cloneX, float _cloneY ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		
		// return ArrayList
		ArrayList<HashMap<String, Object>> _objects = new ArrayList<HashMap<String, Object>>();
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		
		// Page 의 Item list
		ArrayList<HashMap<String, Value>> _child = _page.getItems();
		
		// DataSet 담는 객채
		ArrayList<HashMap<String, Object>> _list = new ArrayList<HashMap<String,Object>>();
		
		int _minimumResizeFontSize = 0;
		if( _page.getMinimumResizeFontSize() > 0 )
		{
			_minimumResizeFontSize = _page.getMinimumResizeFontSize();
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		String _className = "";
		// Data 건수 조회
		int _dataMaxCnt = _page.getDataRowCount();
		int _itemSize = _child.size();
		// Label Band Property 담기.
		HashMap<String, Value> _labelProperty = null;
		
		for( int i=0; i < _itemSize; i++ )
		{
			_className = _child.get(i).get("className").getStringValue();
			
			if( _className.equals("UBLabelBand"))
			{
				_labelProperty = _child.get(i);
				break;
			}
			
		}
		
		if( _labelProperty == null ) return null;
		
		// 가로, 세로 갯수 확인후 Page에 들어가는 총 Band 갯수 구하기.
		int _columns = Integer.valueOf(_labelProperty.get("columns").getStringValue()); 
		int _rows = Integer.valueOf(_labelProperty.get("rows").getStringValue());
		int _pageMaxCnt = (_columns * _rows) * (_dCnt + 1);
		
		// 반복하는 Item의 좌표 및 방향
		float _bandX = Float.valueOf(_labelProperty.get("border_x").getStringValue());
		float _bandY = Float.valueOf(_labelProperty.get("border_y").getStringValue());
		float _bandWidth = Float.valueOf(_labelProperty.get("border_width").getStringValue());
		float _bandHeight = Float.valueOf(_labelProperty.get("border_height").getStringValue());
		
		String _bandDirection = _labelProperty.get("direction").getStringValue();
		
		// Data 시작 Number 구하기 , 증가할 dataNum 선언.
		int _dataStNum = 0;
		
		_dataStNum = (_columns * _rows) * _dCnt;
		
		int _pageStartIndex = 0;
		
		// param의 startIndex값이 잇을경우 _pageMaxCnt값과 _dataStNum에서 -처리 시작 bandIndex값을 _dataStNum값이 이 0일경우 startIndex값으로 셋팅 
		if( _param.containsKey(GlobalVariableData.UB_LABEL_ST_INDEX) || ( _param.containsKey(GlobalVariableData.UB_LABEL_COL_INDEX) && _param.containsKey(GlobalVariableData.UB_LABEL_ROW_INDEX) ) )
		{
			HashMap<String, String> _pList;
			int _stColIndex = 0;
			int _stRowIndex = 0;
			int _stIndex = 0;
			
			if(_param.containsKey(GlobalVariableData.UB_LABEL_ST_INDEX) == false)
			{
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_COL_INDEX);
				_stColIndex = Integer.valueOf( _pList.get("parameter") );
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_ROW_INDEX);
				_stRowIndex = Integer.valueOf( _pList.get("parameter") );
				
				if( _stColIndex > 0 && _stColIndex <= _columns &&  _stRowIndex > 0 && _stRowIndex <= _rows )
				{
					if("downCross".equals(_labelProperty.get("direction").getStringValue())) _pageStartIndex = (_rows * (_stColIndex-1)) + _stRowIndex -1;
					else _pageStartIndex = (_columns * (_stRowIndex-1)) + _stColIndex -1;
				}
				else
				{
					_pageStartIndex = 0;
				}
				
			}
			else
			{
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_ST_INDEX);
				_stIndex = Integer.valueOf( _pList.get("parameter") );
				
				if( _stIndex > 0 )
				{
					_pageStartIndex = _stIndex;
				}
				else
				{
					_pageStartIndex = 0;
				}
			}
				
			
			if(_pageStartIndex > 0 )
			{
				_pageMaxCnt = _pageMaxCnt - _pageStartIndex;
				if( _dataStNum > 0 )
				{
					_dataStNum = _dataStNum - _pageStartIndex;
					
					if(_dataStNum < 0 )
					{
						_dataStNum = 0;
					}
				}
				
				_pageStartIndex = _pageStartIndex - ((_columns * _rows) * _dCnt);
				if(_pageStartIndex < 0 ) _pageStartIndex = 0;
			}
			
		}
		
		if( _pageMaxCnt > _dataMaxCnt)
		{
			_pageMaxCnt = _dataMaxCnt;
		}
		
		HashMap<String, Value> _childItem;
		_itemSize = _child.size();
		// xml Item Parsing
		for (int t = 0; t < _itemSize; t++) 
		{
			_childItem = _child.get(t);
			
			String _excelFormatterStr = "";
			String _id = _childItem.get("id").getStringValue();
			_className = _childItem.get("className").getStringValue();
			
			HashMap<String, Object> _propList = new HashMap<String, Object>();
			// 해당 Item의 Property 확인.
			
			// dataset 정보 담는 객채
			String _dataTypeStr = "";
			String _datasetColumn = "";
			String _datasetId = "";
			
			// formatter variables
			String _formatter="";
			String _nation="";
			String _align="";
			String _dataType="";
			String _mask="";
			String _inputForamtString="";
			String _outputFormatString="";
			
			int _decimalPointLength=0;
			Boolean _useThousandComma=false;
			Boolean _isDecimal=false;
			String _formatString="";
			String _systemFunction = "";

			ArrayList<HashMap<String, Object>> _arrayList = new ArrayList<HashMap<String, Object>>();
			
			_propList = mItemConvertFn.convertItemDataJson(null, _childItem, _data, _dCnt, _param, -1, -1, _totalPageNum, _currentPageNum, "",0,0,  -1 );
			
			if( _propList == null )
			{
				continue;
			}
				
			if(_childItem.get("className").getStringValue().equals("UBLabelBand"))
			{
				if( _labelProperty.get("borderVisible").getStringValue().equals("true"))
				{
					_propList.put("x", _labelProperty.get("border_x").getStringValue());
					_propList.put("y", _labelProperty.get("border_y").getStringValue());
					_propList.put("width", _labelProperty.get("border_width").getStringValue());
					_propList.put("height", _labelProperty.get("border_height").getStringValue());
				}
				else
				{
					_propList.clear();
				}
			}
			else
			{
				if(_propList.get("band_x") != null ) _propList.put("x", _propList.get("band_x"));
				if(_propList.get("band_y") != null ) _propList.put("y", _propList.get("band_y"));
			}
			
			if( _propList.isEmpty() == true && _arrayList.size() == 0)
			{
				continue;
			}
			
			
			if( _arrayList.size() == 0 )
			{
				_arrayList.add(_propList);
			}
			
			HashMap<String, Object> _labelItem = new HashMap<String, Object>();
			
			for (int i = 0; i < _arrayList.size(); i++) 
			{
				HashMap<String, Object> _arItem = _arrayList.get(i);
				
				NodeList formatterItem = null;
				
				boolean _useFormatElement = false;
				
				if( _propList.containsKey("formatter") && !_propList.get("formatter").equals("") && _propList.get("formatter") != null )
				{
					_formatter 			= (_propList.get("formatter") == null)?"": _propList.get("formatter").toString();
					_mask 				= (_propList.get("mask") == null)?"":_propList.get("mask").toString();
					_decimalPointLength = (_propList.get("decimalPointLength") == null)?0:Integer.parseInt(_propList.get("decimalPointLength").toString());
					_useThousandComma 	= (_propList.get("useThousandComma") == null)?false:_propList.get("useThousandComma").toString().equals("true");
					_isDecimal 			= (_propList.get("isDecimal") == null)?false:_propList.get("isDecimal").toString().equals("true");
					_formatString 		= (_propList.get("formatString") == null)?"":_propList.get("formatString").toString();
					_nation 			= (_propList.get("nation") == null)?"":_propList.get("nation").toString();
					_align 				= (_propList.get("currencyAlign") == null)?"":_propList.get("currencyAlign").toString();
					_inputForamtString 	= (_propList.get("inputFormatString") == null)?"":_propList.get("inputFormatString").toString();
					_outputFormatString = (_propList.get("outputFormatString") == null)?"":_propList.get("outputFormatString").toString();
				}
				
				int _bandIdx = _pageStartIndex;
				for (int d = _dataStNum; d < _pageMaxCnt; d++) 
				{
					_labelItem = (HashMap<String, Object>) _arItem.clone();

					_dataTypeStr = String.valueOf(_labelItem.get("dataType"));
					_datasetId = String.valueOf(_labelItem.get("dataSet"));
					_datasetColumn = String.valueOf(_labelItem.get("column"));
					
					_excelFormatterStr = "";
					
					if( _dataTypeStr.equals("1") )
					{
						_list = _data.get(_datasetId);
						if( d >= _list.size()) 
						{
							_labelItem.put("text", "");
							_labelItem.put("tooltip", "");
							continue;
						}
						HashMap<String, Object> _dataHm = _list.get(d);

						Object _dataValue = _dataHm.get(_datasetColumn);

						_labelItem.put("text", _dataValue == null ? "" : _dataValue);

						// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
						_labelItem.put("tooltip", _dataValue == null ? "" : _dataValue);
						
						if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart"))
						{
							//@처리 필요 
							mItemConvertFn.changeLabelBandImgItemJson(_labelItem, _childItem, _data);
						}
					}
					else if(  _dataTypeStr.equals("2") )
					{
						if( _labelItem.containsKey("systemFunction") )
						{
							_systemFunction = _labelItem.get("systemFunction").toString();
							
							if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
								mFunction.setDatasetList(_data);
								
								String _fnValue;
								
								if( mFunction.getFunctionVersion().equals("2.0") ){
									_fnValue = mFunction.testFN(_systemFunction,d, _totalPageNum , _currentPageNum,-1,-1, "" );
								}else{
									_fnValue = mFunction.function(_systemFunction,d, _totalPageNum , _currentPageNum,-1,-1);
								}
								
								_labelItem.put("text", _fnValue);
							}
						}
						
					}
					else if( _dataTypeStr.equals("3"))
					{
						String _txt = _labelItem.get("text").toString();
						int _inOf = _txt.indexOf("{param:");
						String _pKey = "";
						if( _inOf != -1 )
						{
							_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);

							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

							String _value = _pList.get("parameter");

							if( _value.equals("undefined"))
							{
								_labelItem.put("text", "");
							}
							else
							{
								_labelItem.put("text", _value);
							}

						}
						else
						{
							_labelItem.put("text", _txt);
						}
						
						if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart"))
						{
							//@처리 필요 
							mItemConvertFn.changeLabelBandImgItemJson(_labelItem, _childItem, _data);
						}
					}
					
					//Table의 UBFX가 존재할경우 처리( Table의 ubfx를 먼저 처리후 Cell의 ubfx를 처리 )
					if( _childItem.containsKey("ubfx") || _childItem.containsKey("tableUbfunction") )
					{
						ArrayList<HashMap<String, String>> _ubfxs = new ArrayList<HashMap<String, String>>();
						
						ArrayList<ArrayList<HashMap<String, String>>> _ubfxsList = new ArrayList<ArrayList<HashMap<String, String>>>();
						
						int _ubfxListCnt = 0;
						
						if( _childItem.get("tableUbfunction") != null  )
						{
							ArrayList<HashMap<String, String>> _tblUbfxs = (ArrayList<HashMap<String, String>>) _childItem.get("tableUbfunction").getValue();
							_ubfxsList.add( _tblUbfxs );
						}
						
						if( _childItem.get("ubfx") != null )
						{
							_ubfxs = (ArrayList<HashMap<String, String>>) _childItem.get("ubfx").getValue();
							_ubfxsList.add( _ubfxs );
						}
						
						int _nodeCnts = _ubfxsList.size();
						
						for(int _ubfxListIndex= 0; _ubfxListIndex < _nodeCnts; _ubfxListIndex++)
						{
							ArrayList<HashMap<String, String>> _selectUbfxList = _ubfxsList.get(_ubfxListIndex);
							for(int _ubfxIndex = 0; _ubfxIndex < _selectUbfxList.size(); _ubfxIndex++)
							{
								HashMap<String, String> _ubfxItem = _selectUbfxList.get(_ubfxIndex);
								String _ubfxProperty = _ubfxItem.get("property");
								String _ubfxValue = _ubfxItem.get("value");
								try {
									_ubfxValue = URLDecoder.decode(_ubfxValue, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								mFunction.setDatasetList(_data);
								mFunction.setParam(_param);
								String _fnValue;
								
								if( mFunction.getFunctionVersion().equals("2.0") ){
									_fnValue = mFunction.testFN(_ubfxValue,d,_totalPageNum,_currentPageNum , -1,-1,"");
								}else{
									_fnValue = mFunction.function(_ubfxValue,d,_totalPageNum,_currentPageNum , -1,-1,"");
								}
								
								_fnValue = _fnValue.trim();
								
								if(_fnValue.equals("") == false)
								{
									if(_ubfxProperty.indexOf("Color") != -1 )
									{
										_propList.put((_ubfxProperty + "Int"), mPropertyFn.changeColorHexToInt(_fnValue) );
									}
									_propList.put(_ubfxProperty, _fnValue.trim());			// 20170531 true false에 공백이 붙어 나오는 현상이 있어 수정
									
									// color 속성은 color + Int 속성을 넣어줘야 한다.
									if( _ubfxProperty.contains("Color") ){
										_propList.put((_ubfxProperty + "Int"), common.getIntClor(_fnValue) );
									}
								}
							}
						}
						
					}
					
					//hyperLinkedParam처리
					if( _childItem.containsKey("ubHyperLinkType") && "2".equals( _childItem.get("ubHyperLinkType").getStringValue() )  )
					{
						ArrayList<HashMap<String, String>> _hyperLinkedParam = (ArrayList<HashMap<String, String>>) _childItem.get("ubHyperLinkParm").getObjectValue();
						HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
						
						if( _hyperLinkedParam != null && _hyperLinkedParam.size() > 0 )
						{
							int _hyperLinkedParamSize = _hyperLinkedParam.size();
							
							HashMap<String, String> _hMap;
							
							for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
							{
								_hMap = _hyperLinkedParam.get(_hyperIdx);
								String _hyperParamKey = "";
								String _hyperParamValue = "";
								String _hyperParamType = "";
								
								if( _hMap.containsKey("id") )
								{
									_hyperParamKey = _hMap.get("value").toString();
								}
								else if( _hMap.containsKey("value") )
								{
									_hyperParamValue = _hMap.get("value").toString();
								}
								else if( _hMap.containsKey("type") )
								{
									_hyperParamType = _hMap.get("value").toString();
								}
								
								if( "DataSet".equals(_hyperParamType) )
								{
									String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
									String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
									
									_hyperParamValue = "";
									
									if(_data.containsKey(_hyperLinkedDataSetId))
									{
										ArrayList<HashMap<String, Object>> _dsList = _data.get( _hyperLinkedDataSetId );
										Object _dataValue = "";
										if( _list != null ){
											if( d < _dsList.size() )
											{
												HashMap<String, Object> _dataHm = _dsList.get(d);
												_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
											}
										}
									}
								}
								else if("Parameter".equals(_hyperParamType) )
								{
									String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
									String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
									HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

									String _pValue = _pList.get("parameter");

									if( _pValue.equals("undefined"))
									{
										_hyperParamValue = "";
									}
									else
									{
										_hyperParamValue = _pValue;
									}
								}
								
								_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
							}
							
							_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
						}
						
					}

					if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
						Object _propValue;
						_propValue=_propList.get("text");

						String _formatValue="";
						_formatValue = _labelItem.get("text").toString();
						try {
							if( _formatter.equalsIgnoreCase("Currency") ){
								_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
							}
							else if( _formatter.equalsIgnoreCase("Date") ){
								_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
							}
							else if( _formatter.equalsIgnoreCase("MaskNumber") ){
								_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
							}
							else if( _formatter.equalsIgnoreCase("MaskString") ){
								_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
							}
							else if( _formatter.equalsIgnoreCase("CustomDate") )
							{
								_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
								_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
								
								_propList.put("inputFormatString", _inputForamtString);
								_propList.put("outputFormatString", _outputFormatString);
							}
						} catch (ParseException e) {
//							e.printStackTrace();
						}
						_labelItem.put("text", _formatValue);

						if( isExportType.equals("EXCEL") && _excelFormatterStr.equals("") == false && common.getPropertyValue("excelExport.useFormatter") != null && common.getPropertyValue("excelExport.useFormatter").equals("true") ) 
						{
							_propList.put("EX_FORMATTER", _formatter);
							_propList.put("EX_FORMAT_DATA_STR", _excelFormatterStr);
							_propList.put("EX_FORMAT_ORIGINAL_STR", _propValue.toString() );
						}
					}
					

					float _itemX = 0;
					float _itemY = 0;


					if( _bandDirection.equals("crossDown")) // 아래로
					{
						_itemX = (_bandWidth * (_bandIdx % _columns) ) + (((_bandIdx % _columns)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString()));
						_itemY = (float) ((_bandHeight * ( Math.floor(_bandIdx / _columns))) + ((Math.floor(_bandIdx / _columns)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString())));

					}
					else // 옆으로
					{

						_itemX = (float) ((_bandWidth * (Math.floor(_bandIdx / _rows)) ) + ((Math.floor(_bandIdx / _rows)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString())));
						_itemY = (_bandHeight * (_bandIdx % _rows) ) + (((_bandIdx % _rows)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString()));

					}
					_labelItem.put("x",  _itemX + _cloneX);
					_labelItem.put("y", _itemY + _cloneY);	
					_labelItem.put("top", _itemY + _cloneY);	
					_labelItem.put("left", _itemX + _cloneX);	

					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					if(_labelItem.containsKey("text"))
					{
						String _value = _labelItem.get("text").toString();
						_labelItem.put("tooltip", _value);
					}
					
					if( _labelItem.containsKey("visible") == false ||  "false".equals( _labelItem.get("visible") ) != true) _objects.add(_labelItem);
					_bandIdx++;
				} // for _pageMaxCnt

			}// for arraylist.size();
			
		} // for _child.getLength()
		
		return _objects;
	}
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> CreateLabelBand(DataSetProcess _dataSetFn , Element _page , HashMap<String, Element> _dataSetItems , HashMap<String, Object> _param , int rowStartIndex) throws UnsupportedEncodingException
	{
		
		// return ArrayList
		ArrayList<HashMap<String, Object>> _objects = new ArrayList<HashMap<String, Object>>();
		
		// Page 의 Item list
		NodeList _child = _page.getElementsByTagName("item");
		
		// DataSet 담는 객채
		ArrayList<HashMap<String, Object>> _list = new ArrayList<HashMap<String,Object>>();
		
		// Label Band Property 담기.
		HashMap<String, String> _labelProperty = new HashMap<String, String>();
		for (int i = 0; i < _child.getLength(); i++) 
		{
			Element _labelBand = (Element) _child.item(i);
			
			if( _labelBand.getAttribute("className").equals("UBLabelBand"))
			{
				NodeList _labelProps = _labelBand.getElementsByTagName("property");
				
				for (int j = 0; j < _labelProps.getLength(); j++) 
				{
					Element _labelProp = (Element) _labelProps.item(j);
					
					String _lName = _labelProp.getAttribute("name");
					String _lValue = _labelProp.getAttribute("value");
					
					_labelProperty.put(_lName, _lValue);
					
				}
				
			}
			else
			{
				if( _labelProperty.size() > 0 )
				{
					break;
				}
			}
		}
		
		// 가로, 세로 갯수 확인후 Page에 들어가는 총 Band 갯수 구하기.
		int _columns = Integer.valueOf(_labelProperty.get("columns")); 
		int _rows = Integer.valueOf(_labelProperty.get("rows"));
		
		int _pageMaxCnt = (_columns * _rows) * (rowStartIndex + 1);
		
		
		// Data 시작 Number 구하기 , 증가할 dataNum 선언.
		int _dataStNum = 0;
		
		_dataStNum = (_columns * _rows) * rowStartIndex;
		
		// Data 건수 조회
		int _dataMaxCnt = 0;
		HashMap<String, ArrayList<HashMap<String,Object>>> _dataSetHm = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		XPath _xpath = XPathFactory.newInstance().newXPath();
		Element _propDataSet;
		try {
			NodeList _dNodeList = (NodeList) _xpath.evaluate("//property[@name='dataSet'][@value != 'null'][@value != '']", _page, XPathConstants.NODESET);
			for (int i = 0; i < _dNodeList.getLength(); i++) {
				_propDataSet = (Element) _dNodeList.item(i);
				String _value = _propDataSet.getAttribute("value");
				
				if( !_dataSetHm.containsKey(_value) )
				{
					//ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
					Element _udm = _dataSetItems.get(_value);
					//HashMap<String, Object> _dataHm = _list.get(_dCnt);
					
					HashMap<String, HashMap<String, String>> _datasetParam = new HashMap<String, HashMap<String, String>>();
					
					for(Entry<String, Object> entry : _param.entrySet()) {
						String key = entry.getKey();
						HashMap<String, String> value = (HashMap<String, String>) entry.getValue();
						_datasetParam.put(key, value);
					}
					
					_list = _dataSetFn.dataSetLoadData(_udm, _datasetParam, _dataStNum, _pageMaxCnt);
					
					_dataSetHm.put(_value, _list);
				}
				if( _list.size() > _dataMaxCnt)
				{
					_dataMaxCnt = _list.size();
				}
				
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		_pageMaxCnt = _dataMaxCnt;
		
		
		int _pageStartIndex = 0;
		
		// param의 startIndex값이 잇을경우 _pageMaxCnt값과 _dataStNum에서 -처리 시작 bandIndex값을 _dataStNum값이 이 0일경우 startIndex값으로 셋팅
//		if(_param.containsKey(GlobalVariableData.UB_LABEL_START_INDEX)) 
		if( _param.containsKey(GlobalVariableData.UB_LABEL_COL_INDEX) && _param.containsKey(GlobalVariableData.UB_LABEL_ROW_INDEX) )
		{
			HashMap<String, String> _pList;
			_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_COL_INDEX);
			int _stColIndex = Integer.valueOf( _pList.get("parameter") );
			_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_ROW_INDEX);
			int _stRowIndex = Integer.valueOf( _pList.get("parameter") );
			
			if( _stColIndex > 0 && _stColIndex <= _columns &&  _stRowIndex > 0 && _stRowIndex <= _rows )
			{
				if("downCross".equals(_labelProperty.get("direction"))) _pageStartIndex = (_rows * (_stColIndex-1)) + _stRowIndex -1;
				else _pageStartIndex = (_columns * (_stRowIndex-1)) + _stColIndex -1;
			}
			else
			{
				_pageStartIndex = 0;
			}
			
			if(_pageStartIndex > 0 )
			{
				_pageMaxCnt = _pageMaxCnt - _pageStartIndex;
				if( _dataStNum > 0 )
				{
					_dataStNum = _dataStNum - _pageStartIndex;
//					_pageStartIndex = 0;
					
					if(_dataStNum < 0 )
					{
						_dataStNum = 0;
					}
				}
				
				_pageStartIndex = _pageStartIndex - ((_columns * _rows) * rowStartIndex);
				if(_pageStartIndex < 0 ) _pageStartIndex = 0;
			}
			
		}
		
		
		// 반복하는 Item의 좌표 및 방향
		float _bandX = Float.valueOf(_labelProperty.get("border_x"));
		float _bandY = Float.valueOf(_labelProperty.get("border_y"));
		float _bandWidth = Float.valueOf(_labelProperty.get("border_width"));
		float _bandHeight = Float.valueOf(_labelProperty.get("border_height"));
		
		String _bandDirection = _labelProperty.get("direction");
		
		// xml Item Parsing
		for (int t = 0; t < _child.getLength(); t++) 
		{
			Element _childItem = (Element) _child.item(t);
			
			String _id = _childItem.getAttribute("id");
			String _className = _childItem.getAttribute("className");
			
			// borderLabel 확인
			boolean _borderFlg = false;
			if( _className.equals("UBLabelBorder"))
			{
				_borderFlg = true;
			}
			
			// 해당 Item의 Property 확인.
			
			NodeList _propertys = _childItem.getElementsByTagName("property");
			
			// 해당 Item의 Property 기본 속성 불러오기.
			HashMap<String, Object> _propList = new HashMap<String, Object>();
			_propList = mItemPropVar.getItemName(_className);
			
			// id와 className 넣기.
			_propList.put("className" , _className );
			_propList.put("id" , _id );
			
			// dataset 정보 담는 객채
			String _dataTypeStr = "";
			String _datasetColumn = "";
			String _datasetId = "";
			
			
			for (int p = 0; p < _propertys.getLength(); p++) 
			{
				Element _property = (Element) _propertys.item(p);
				
				String _pName = _property.getAttribute("name");
				String _pValue = _property.getAttribute("value");
				
				
				if(_className.equals("UBLabelBand"))
				{
					if( _labelProperty.get("borderVisible").equals("true"))
					{
						_propList.put("x", _labelProperty.get("border_x"));
						_propList.put("y", _labelProperty.get("border_y"));
						_propList.put("width", _labelProperty.get("border_width"));
						_propList.put("height", _labelProperty.get("border_height"));
					}
					else
					{
						_propList.clear();
					}
					
					break;
				}
				else
				{
					if(_propList.containsKey(_pName))
					{
						if( _pName.equals("fontFamily"))
						{
							_pValue = URLDecoder.decode(_pValue, "UTF-8");
							_propList.put(_pName, _pValue);
						}
						else if( _pName.indexOf("Color") != -1)
						{
							_propList.put((_pName + "Int"), _pValue);
							_pValue = mPropertyFn.changeColorToHex(Integer.parseInt(_pValue));
							_propList.put(_pName, _pValue);
						}
						else if( _pName.equals("lineHeight"))
						{
							//_pValue = "1.16"; //TODO LineHeight Test
							_pValue = _pValue.replace("%25", "");
							_pValue = String.valueOf((Float.parseFloat(_pValue)/100));		
							_propList.put(_pName, _pValue);
						}
						else if( _pName.equals("borderType"))
						{
							_propList.put(_pName, _pValue);
						}
						else if( _pName.equals("text"))
						{
							_pValue = URLDecoder.decode(_pValue, "UTF-8");
							_propList.put(_pName, _pValue);
						}
						else if( _pName.equals("borderSide"))
						{
							ArrayList<String> _bSide = new ArrayList<String>();
							_pValue = URLDecoder.decode(_pValue, "UTF-8");
							if( !_pValue.equals("none") )
							{
								_bSide = mPropertyFn.getBorderSideToArrayList(_pValue);
								
								if( _bSide.size() > 0)
								{
									String _type = (String) _propList.get("borderType");
									_type = mPropertyFn.getBorderType(_type);
									_propList.put("borderType", _type);
								}
								
							}
							
							_propList.put(_pName, _bSide);
						}
						else
						{
							_propList.put(_pName, _pValue);
						}
					}
					else if(_pName.equals("dataType"))
					{
						_dataTypeStr = _pValue;
					}
					else if(_pName.equals("column"))
					{
						_datasetColumn = _pValue;
					}
					else if( _pName.equals("band_x"))
					{
						_propList.put("x", _pValue);
					}
					else if( _pName.equals("band_y"))
					{
						_propList.put("y", _pValue);
					}
					else if(_pName.equals("dataSet"))
					{
						_datasetId = _pValue;
					}
					else if(_pName.equals("borderThickness"))
					{
						_propList.put("borderWidth", _pValue);
					}
				} // else _className.equals("UBLabelBand") 
				
			} // for _propertys.getLength()
			
			if( _propList.isEmpty())
			{
				continue;
			}
			
			
			HashMap<String, Object> _labelItem = new HashMap<String, Object>();
//			int _bandIdx = 0;
			int _bandIdx = _pageStartIndex;
			for (int d = 0; d < _pageMaxCnt; d++) 
			{
				_labelItem = (HashMap<String, Object>) _propList.clone();
				
				if( _dataTypeStr.equals("1") )
				{
					
					_list = _dataSetHm.get(_datasetId);
					HashMap<String, Object> _dataHm = _list.get(d);
					
					Object _dataValue = _dataHm.get(_datasetColumn);
					
					_labelItem.put("text", _dataValue);
					
					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					_labelItem.put("tooltip", _dataValue);
				}
				else if( _dataTypeStr.equals("3"))
				{
					String _txt = _labelItem.get("text").toString();
					int _inOf = _txt.indexOf("{param:");
					String _pKey = "";
					if( _inOf != -1 )
					{
						_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);
						
						HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);
						
						String _value = _pList.get("parameter");
						
						if( _value.equals("undefined"))
						{
							_labelItem.put("text", "");
						}
						else
						{
							_labelItem.put("text", _value);
						}
						
					}
					else
					{
						_labelItem.put("text", _txt);
					}
				}
				
				float _itemX = 0;
				float _itemY = 0;
				
				
				if( _bandDirection.equals("crossDown")) // 아래로
				{
					_itemX = (_bandWidth * (_bandIdx % _columns) ) + (((_bandIdx % _columns)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString()));
					_itemY = (float) ((_bandHeight * ( Math.floor(_bandIdx / _columns))) + ((Math.floor(_bandIdx / _columns)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString())));
					
				}
				else // 옆으로
				{
					
					_itemX = (float) ((_bandWidth * (Math.floor(_bandIdx / _rows)) ) + ((Math.floor(_bandIdx / _rows)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString())));
					_itemY = (_bandHeight * (_bandIdx % _rows) ) + (((_bandIdx % _rows)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString()));
					
				}
				_labelItem.put("x",  _itemX);
				_labelItem.put("y", _itemY);	
								
				// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
				if(_labelItem.containsKey("text"))
				{
					String _value = _labelItem.get("text").toString();
					_labelItem.put("tooltip", _value);
				}
				
				_objects.add(_labelItem);
				_bandIdx++;
			} // for _pageMaxCnt
			
		} // for _child.getLength()
		
		return _objects;
	}
	
		
	
	
	private HashMap<String, Object> setItemPropertys( String _className , HashMap<String, Object> _item, NodeList _propertys , Integer _dataCnt) throws UnsupportedEncodingException
	{
		
		String _dataTypeStr = "";
		String _dataColumn = "";
		String _dataID = "";
		String _model_type = "";
		String _barcode_type = "";
		
		// formatter variables
		String _formatter="";
		String _nation="";
		String _align="";
		String _dataType="";
		String _mask="";
		
		int _decimalPointLength=0;
		Boolean _useThousandComma=false;
		Boolean _isDecimal=false;
		String _formatString="";
		
		
		
		return _item; 
	}
	
	
	public ArrayList<HashMap<String, Object>> createFreeFormConnect(Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt, float _cloneX, float _cloneY, ArrayList<HashMap<String, Object>> _objects, int _totPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		NodeList _child = _page.getElementsByTagName("item");
		
		//mImageData=new HashMap<String,String>();
		
		int _minimumResizeFontSize = 0;
		if( _page.hasAttribute("minimumResizeFontSize") && StringUtil.isInteger(_page.getAttribute("minimumResizeFontSize")) )
		{
			_minimumResizeFontSize = Integer.valueOf(_page.getAttribute("minimumResizeFontSize"));
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		
		ArrayList<HashMap<String, Object>> _radioButtons = new ArrayList<HashMap<String, Object>>();
		
		// xml Item
		for(int j = 0; j < _child.getLength() ; j++)
		{
			Element _childItem = (Element) _child.item(j);

			String _itemId = _childItem.getAttribute("id");
			String _className = _childItem.getAttribute("className");

			HashMap<String, Object> _propList = null;
			
			if( _className.equals("UBTable") || _className.equals("UBApproval") )
			{
				try {
					mItemConvertFn.convertElementTableToItem(_childItem, _dCnt , _data , _param, _cloneX, _cloneY, -1,_objects, _totPageNum, _currentPageNum, false);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if( _className.equals("UBTemplateArea") )
			{	
				_objects = mTempletInfo.get(_itemId).convertItemData( _dCnt, _cloneX, _cloneY, _objects, mFunction, null, _currentPageNum, _totPageNum, -1, -1, changeItemList, null, mItemConvertFn);
			}
			else
			{
				_propList = mItemConvertFn.convertElementToItem( _childItem, _dCnt, _data, _param,  _cloneX, _cloneY, -1, _totPageNum, _currentPageNum, false);
				if( _propList != null )
				{
					_propList.put("className" , _className );
					_propList.put("id" , _itemId );
					
					//RadioButtonGroup은 RadioButton보다 앞서서 생성되어야 한다
					if( _className.equals("UBRadioButtonGroup") )
					{
						_objects.add(0, _propList);
					}
					else
					{
						_objects.add(_propList);
						
						if( _className.equals("UBRadioBorder") )
						{
							_radioButtons.add(_propList);
						}
					}
					
				}
			}

		}
		
		if( _radioButtons.size() > 0 )
		{
			for (int i = 0; i < _radioButtons.size(); i++) {
				Boolean _isSelected = mItemConvertFn.radiobuttonHandler(_radioButtons.get(i));
				_radioButtons.get(i).put("selected", _isSelected);
			}
			_radioButtons.clear();
		}
		
		return _objects;
	}
	
	
	@SuppressWarnings("unchecked")									 
	public ArrayList<HashMap<String, Object>> CreateLabelBandConnect(Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt , float _cloneX, float _cloneY, ArrayList<HashMap<String, Object>> _objects, int _totPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		
		// Page 의 Item list
		NodeList _child = _page.getElementsByTagName("item");
		
		// DataSet 담는 객채
		ArrayList<HashMap<String, Object>> _list = new ArrayList<HashMap<String,Object>>();
		
		// Data 건수 조회
		int _dataMaxCnt = 0;
		XPath _xpath = XPathFactory.newInstance().newXPath();
		Element _propDataSet;
		try {
			NodeList _dNodeList = (NodeList) _xpath.evaluate("//property[@name='dataSet'][@value != 'null'][@value != '']", _page, XPathConstants.NODESET);
			for (int i = 0; i < _dNodeList.getLength(); i++) {
				_propDataSet = (Element) _dNodeList.item(i);
				String _value = _propDataSet.getAttribute("value");
				
				_list = _data.get(_value);
				
				if( _list.size() > _dataMaxCnt)
				{
					_dataMaxCnt = _list.size();
				}
				
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int _minimumResizeFontSize = 0;
		if( _page.hasAttribute("minimumResizeFontSize") && StringUtil.isInteger(_page.getAttribute("minimumResizeFontSize")) )
		{
			_minimumResizeFontSize = Integer.valueOf(_page.getAttribute("minimumResizeFontSize"));
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		
		// Label Band Property 담기.
		HashMap<String, String> _labelProperty = new HashMap<String, String>();
		for (int i = 0; i < _child.getLength(); i++) 
		{
			Element _labelBand = (Element) _child.item(i);
			
			if( _labelBand.getAttribute("className").equals("UBLabelBand"))
			{
				NodeList _labelProps = _labelBand.getElementsByTagName("property");
			
				for (int j = 0; j < _labelProps.getLength(); j++) 
				{
					Element _labelProp = (Element) _labelProps.item(j);
					
					String _lName = _labelProp.getAttribute("name");
					String _lValue = _labelProp.getAttribute("value");
					
					_labelProperty.put(_lName, _lValue);
					
				}
				
			}
			else
			{
				if( _labelProperty.size() > 0 )
				{
					break;
				}
			}
		}
		
		// 가로, 세로 갯수 확인후 Page에 들어가는 총 Band 갯수 구하기.
		int _columns = Integer.valueOf(_labelProperty.get("columns")); 
		int _rows = Integer.valueOf(_labelProperty.get("rows"));
		
		int _pageMaxCnt = (_columns * _rows) * (_dCnt + 1);
		
		// 반복하는 Item의 좌표 및 방향
		float _bandX = Float.valueOf(_labelProperty.get("border_x"));
		float _bandY = Float.valueOf(_labelProperty.get("border_y"));
		float _bandWidth = Float.valueOf(_labelProperty.get("border_width"));
		float _bandHeight = Float.valueOf(_labelProperty.get("border_height"));
		
		String _bandDirection = _labelProperty.get("direction");
		
		
		// Data 시작 Number 구하기 , 증가할 dataNum 선언.
		int _dataStNum = 0;
		
		_dataStNum = (_columns * _rows) * _dCnt;
		
		int _pageStartIndex = 0;
		
		// param의 startIndex값이 잇을경우 _pageMaxCnt값과 _dataStNum에서 -처리 시작 bandIndex값을 _dataStNum값이 이 0일경우 startIndex값으로 셋팅
//		if(_param.containsKey(GlobalVariableData.UB_LABEL_START_INDEX))
		if( _param.containsKey(GlobalVariableData.UB_LABEL_ST_INDEX) || ( _param.containsKey(GlobalVariableData.UB_LABEL_COL_INDEX) && _param.containsKey(GlobalVariableData.UB_LABEL_ROW_INDEX) ) )
		{
			HashMap<String, String> _pList;
			int _stColIndex = 0;
			int _stRowIndex = 0;
			int _stIndex = 0;
			
			if(_param.containsKey(GlobalVariableData.UB_LABEL_ST_INDEX) == false)
			{
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_COL_INDEX);
				_stColIndex = Integer.valueOf( _pList.get("parameter") );
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_ROW_INDEX);
				_stRowIndex = Integer.valueOf( _pList.get("parameter") );
				
				if( _stColIndex > 0 && _stColIndex <= _columns &&  _stRowIndex > 0 && _stRowIndex <= _rows )
				{
					if("downCross".equals(_labelProperty.get("direction"))) _pageStartIndex = (_rows * (_stColIndex-1)) + _stRowIndex -1;
					else _pageStartIndex = (_columns * (_stRowIndex-1)) + _stColIndex -1;
				}
				else
				{
					_pageStartIndex = 0;
				}
				
			}
			else
			{
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_ST_INDEX);
				_stIndex = Integer.valueOf( _pList.get("parameter") );
				
				if( _stIndex > 0 )
				{
					_pageStartIndex = _stIndex;
				}
				else
				{
					_pageStartIndex = 0;
				}
			}
				
			
			if(_pageStartIndex > 0 )
			{
				_pageMaxCnt = _pageMaxCnt - _pageStartIndex;
				if( _dataStNum > 0 )
				{
					_dataStNum = _dataStNum - _pageStartIndex;
					
					if(_dataStNum < 0 )
					{
						_dataStNum = 0;
					}
				}
				
				_pageStartIndex = _pageStartIndex - ((_columns * _rows) * _dCnt);
				if(_pageStartIndex < 0 ) _pageStartIndex = 0;
			}
			
		}
		
		if( _pageMaxCnt > _dataMaxCnt)
		{
			_pageMaxCnt = _dataMaxCnt;
		}
		
		
		// xml Item Parsing
		for (int t = 0; t < _child.getLength(); t++) 
		{
			Element _childItem = (Element) _child.item(t);
			
			String _id = _childItem.getAttribute("id");
			String _className = _childItem.getAttribute("className");
			
			HashMap<String, Object> _propList = new HashMap<String, Object>();
			// 해당 Item의 Property 확인.
			
			NodeList _propertys = _childItem.getElementsByTagName("property");
			NodeList _ubfunction = _childItem.getElementsByTagName("ubfunction");
			
			// dataset 정보 담는 객채
			String _dataTypeStr = "";
			String _datasetColumn = "";
			String _datasetId = "";
			
			// formatter variables
			String _formatter="";
			String _nation="";
			String _align="";
			String _dataType="";
			String _mask="";
			
			int _decimalPointLength=0;
			Boolean _useThousandComma=false;
			Boolean _isDecimal=false;
			String _formatString="";
			String _systemFunction = "";
			
			
			ArrayList<HashMap<String, Object>> _arrayList = new ArrayList<HashMap<String, Object>>();
			
			if( _className.equals("UBTable") || _className.equals("UBApproval") )
			{
				try {
					mItemConvertFn.convertElementTableToItem(_childItem , _dCnt , _data , _param, _cloneX, 0, -1,_arrayList, _totPageNum, _currentPageNum, true);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				_propList = mItemConvertFn.convertElementToItem(_childItem, _dCnt, _data, _param, _cloneX, 0, -1, _totPageNum, _currentPageNum, true);
				
				if( _propList == null )
				{
					continue;
				}
				
				if(_propList.get("className").equals("UBLabelBand"))
				{
					if( _labelProperty.get("borderVisible").equals("true"))
					{
						_propList.put("x", _labelProperty.get("border_x"));
						_propList.put("y", _labelProperty.get("border_y"));
						_propList.put("width", _labelProperty.get("border_width"));
						_propList.put("height", _labelProperty.get("border_height"));

					}
					else
					{
						_propList.clear();
					}
				}
				else
				{
					_propList.put("x", _propList.get("band_x"));
					_propList.put("y", _propList.get("band_y"));
				}
				
			}
			
			if( _propList.isEmpty() == true && _arrayList.size() == 0)
			{
				continue;
			}
			
			
			int  _arrayListSize =  _arrayList.size();
			if( _arrayListSize == 0 )
			{
				_arrayList.add(_propList);
				_arrayListSize = 1;
			}
			
			HashMap<String, Object> _labelItem = new HashMap<String, Object>();

			
			for (int i = 0; i < _arrayListSize; i++) 
			{
				HashMap<String, Object> _arItem = _arrayList.get(i);
				
				NodeList formatterItem = null;
				boolean _useFormatElement = false;
				
				if( _arItem.containsKey("formatterElement") )
				{
					// 포맷터 값이존재할경우각각의 데이터를 담는 처리
					formatterItem =(NodeList) _arItem.get("formatterElement");
					if(formatterItem.getLength() > 0 ) _useFormatElement = true;
					
					_arItem.remove("formatterElement");
				}

				if( !_useFormatElement )
				{
					formatterItem = _childItem.getElementsByTagName("formatter");
				}
				
				if( formatterItem != null && formatterItem.getLength() > 0 )
				{
					NodeList formatterProperty = null;
					
					if( _useFormatElement )
					{
						formatterProperty = formatterItem;
					}
					else
					{
						formatterProperty = ((Element) formatterItem.item(0)).getElementsByTagName("property");
					}
					
					if(_arItem.containsKey("formatter")) _formatter = _arItem.get("formatter").toString();
					
					
//							NodeList formatterProperty = ((Element) formatterItem.item(0)).getElementsByTagName("property");
					int propertySize = formatterProperty.getLength();
					for (int p = 0; p < propertySize; p++) {
						
						Element _propItem = (Element) formatterProperty.item(p);

						String _name = _propItem.getAttribute("name");
						String _value = _propItem.getAttribute("value");
						
						if( _name.equals("mask") ){
							_mask = URLDecoder.decode(_value, "UTF-8");
						}
						else if( _name.equals("decimalPointLength") ){
							
							_decimalPointLength = Integer.parseInt(_value);
						}				
						else if( _name.equals("useThousandComma") ){
							_useThousandComma = Boolean.parseBoolean(_value);
						}		
						else if( _name.equals("isDecimal") ){
							_isDecimal = Boolean.parseBoolean(_value);
						}		
						else if( _name.equals("formatString") ){
							_formatString = _value;
						}
					}
				}
				
//						int _bandIdx = 0;
				int _bandIdx = _pageStartIndex;
				for (int d = _dataStNum; d < _pageMaxCnt; d++) 
				{
					_labelItem = (HashMap<String, Object>) _arItem.clone();

					_dataTypeStr = String.valueOf(_labelItem.get("dataType"));
					_datasetId = String.valueOf(_labelItem.get("dataSet"));
					_datasetColumn = String.valueOf(_labelItem.get("column"));
					
					if( _dataTypeStr.equals("1") )
					{
						_list = _data.get(_datasetId);
						if( d >= _list.size()) 
						{
							_labelItem.put("text", "");
							_labelItem.put("tooltip", "");
							continue;
						}
						HashMap<String, Object> _dataHm = _list.get(d);

						Object _dataValue = _dataHm.get(_datasetColumn);

						_labelItem.put("text", _dataValue == null ? "" : _dataValue);

						// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
						_labelItem.put("tooltip", _dataValue == null ? "" : _dataValue);
						
						if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart"))
						{
							mItemConvertFn.changeLabelBandImgItem(_labelItem, _childItem, _data);
						}
					}
					else if(  _dataTypeStr.equals("2") )
					{
						if( _labelItem.containsKey("systemFunction") )
						{
							_systemFunction = _labelItem.get("systemFunction").toString();
							
							if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
								mFunction.setDatasetList(_data);
								
								String _fnValue;
								
								if( mFunction.getFunctionVersion().equals("2.0") ){
									_fnValue = mFunction.testFN(_systemFunction,d, _totPageNum , _currentPageNum,-1,-1, "" );
								}else{
									_fnValue = mFunction.function(_systemFunction,d, _totPageNum , _currentPageNum,-1,-1);
								}
								
								_labelItem.put("text", _fnValue);
							}
						}
						
						if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart"))
						{
							mItemConvertFn.changeLabelBandImgItem(_labelItem, _childItem, _data);
						}
					}
					else if( _dataTypeStr.equals("3"))
					{
						String _txt = _labelItem.get("text").toString();
						int _inOf = _txt.indexOf("{param:");
						String _pKey = "";
						if( _inOf != -1 )
						{
							_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);

							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

							String _value = _pList.get("parameter");

							if( _value.equals("undefined"))
							{
								_labelItem.put("text", "");
							}
							else
							{
								_labelItem.put("text", _value);
							}

						}
						else
						{
							_labelItem.put("text", _txt);
						}
						
						if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart"))
						{
							mItemConvertFn.changeLabelBandImgItem(_labelItem, _childItem, _data);
						}
					}

					for(int _ubfxIndex = 0; _ubfxIndex < _ubfunction.getLength(); _ubfxIndex++)
					{
						Element _ubfxItem = (Element) _ubfunction.item(_ubfxIndex);
						String _name = _ubfxItem.getAttribute("property");
						String _value = _ubfxItem.getAttribute("value");
						_value = URLDecoder.decode(_value, "UTF-8");

						mFunction.setDatasetList(_data);
						
						String _fnValue;
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = mFunction.testFN(_value, d, 0, _dCnt,-1,-1, "" );
						}else{
							_fnValue = mFunction.function(_value, d, 0, _dCnt,-1,-1);
						}
						
						_fnValue = _fnValue.trim();
						
						_labelItem.put(_name, _fnValue);
					}

					_formatter = (String) _arItem.get("formatter");
					if( _formatter != null && !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
						String _formatValue="";
						_formatValue = _labelItem.get("text").toString();
						
						_mask=(String) _labelItem.get("mask");
						_decimalPointLength= Integer.valueOf( _labelItem.get("decimalPointLength" ).toString() );
						_useThousandComma=(Boolean) _labelItem.get("useThousandComma");
						_isDecimal=(Boolean) _labelItem.get("isDecimal");
						_formatValue= _labelItem.get("text").toString();
						//_formatValue=(String) _labelItem.get("formatString");
						
						try {
							if( _formatter.equalsIgnoreCase("Currency") ){
								_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
							}else if( _formatter.equalsIgnoreCase("Date") ){
								_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
							}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
								_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
							}else if( _formatter.equalsIgnoreCase("MaskString") ){
								_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
							}
						} catch (ParseException e) {
//									e.printStackTrace();
						}
						_labelItem.put("text", _formatValue);
					}




					float _itemX = 0;
					float _itemY = 0;


					if( _bandDirection.equals("crossDown")) // 아래로
					{
						_itemX = (_bandWidth * (_bandIdx % _columns) ) + (((_bandIdx % _columns)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString()));
						_itemY = (float) ((_bandHeight * ( Math.floor(_bandIdx / _columns))) + ((Math.floor(_bandIdx / _columns)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString())));

					}
					else // 옆으로
					{

						_itemX = (float) ((_bandWidth * (Math.floor(_bandIdx / _rows)) ) + ((Math.floor(_bandIdx / _rows)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString())));
						_itemY = (_bandHeight * (_bandIdx % _rows) ) + (((_bandIdx % _rows)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString()));

					}
					
					_itemX = _itemX + _cloneX;
					_itemY = _itemY + _cloneY;
					
					_labelItem.put("x",  _itemX);
					_labelItem.put("y", _itemY);	

					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					if(_labelItem.containsKey("text"))
					{
						String _value = _labelItem.get("text").toString();
						_labelItem.put("tooltip", _value);
					}
					
					if(isExportType.equals("WORD") || isExportType.equals("HWP") || isExportType.equals("PPT") )
					{
						// Cell을 Export처리를 위하여 TableID를 담고 BorderType를 업데이트 처리 
						if( _labelItem.containsKey("ORIGINAL_TABLE_ID"))
						{
							_labelItem.put("ORIGINAL_TABLE_ID", _labelItem.get("ORIGINAL_TABLE_ID") + "_"+ d );
							_labelItem.put("TABLE_ID", _labelItem.get("ORIGINAL_TABLE_ID") );
							
							if(_labelItem.containsKey("borderOriginalTypes")) _labelItem.put("borderTypes", _labelItem.get("borderOriginalTypes"));
							_labelItem.remove("cellY");
						}
					}
					
					if( _labelItem.containsKey("visible") == false ||  "false".equals( _labelItem.get("visible") ) != true) _objects.add(_labelItem);
					_bandIdx++;
				} // for _pageMaxCnt

			}// for arraylist.size();
			
		} // for _child.getLength()
				
		return _objects;
	}
	
	
	
	
	public ArrayList<HashMap<String, Object>> createPageItemList( Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt, float _cloneX, float _cloneY, ArrayList<HashMap<String, Object>> _objects) throws Exception
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		
		NodeList _child = _page.getElementsByTagName("item");
		
		//mImageData=new HashMap<String,String>();
		
		// xml Item
		for(int j = 0; j < _child.getLength() ; j++)
		{
			Element _childItem = (Element) _child.item(j);

			String _itemId = _childItem.getAttribute("id");
			String _className = _childItem.getAttribute("className");

			NodeList _propertys = _childItem.getElementsByTagName("property");
			NodeList _ubfunction = _childItem.getElementsByTagName("ubfunction");
			HashMap<String, Object> _propList = new HashMap<String, Object>();

			String _dataTypeStr = "";
			String _dataColumn = "";
			String _dataID = "";
			String _model_type = "";
			String _barcode_type = "";
			
			// formatter variables
			String _formatter="";
			String _nation="";
			String _align="";
			String _dataType="";
			String _mask="";
			
			int _decimalPointLength=0;
			Boolean _useThousandComma=false;
			Boolean _isDecimal=false;
			String _formatString="";
			
			// image variable
			String _prefix="";
			String _suffix="";
			String[] _datasets={"",""};
			
			_propList = mItemPropVar.getItemName(_className);

			// xml Item propertys
			for(int p = 0; p < _propertys.getLength(); p++)
			{
				Element _propItem = (Element) _propertys.item(p);

				String _name = _propItem.getAttribute("name");
				String _value = _propItem.getAttribute("value");

				//					if( mItemPropVar.equals(_className) )
				//					{
				if(_propList.containsKey(_name))
				{
					if( _name.equals("fontFamily"))
					{
						_value = URLDecoder.decode(_value, "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.indexOf("Color") != -1)
					{
						_propList.put((_name + "Int"), _value);
						_value = mPropertyFn.changeColorToHex(Integer.parseInt(_value));
						_propList.put(_name, _value);
					}
					else if( _name.equals("lineHeight"))
					{
						//_value = "1.16"; //TODO LineHeight Test
						_value = _value.replace("%25", "");
						_value = String.valueOf((Float.parseFloat(_value)/100));		
						_propList.put(_name, _value);
					}
					else if( _name.equals("borderType"))
					{
						_propList.put(_name, _value);
					}
					else if( _name.equals("text"))
					{
						_value = URLDecoder.decode(_value, "UTF-8");
						_propList.put(_name, _value);
					}
					else if( _name.equals("borderSide"))
					{
						ArrayList<String> _bSide = new ArrayList<String>();
						_value = URLDecoder.decode(_value, "UTF-8");
						if( !_value.equals("none") )
						{
							_bSide = mPropertyFn.getBorderSideToArrayList(_value);

							if( _bSide.size() > 0)
							{
								String _type = (String) _propList.get("borderType");
								_type = mPropertyFn.getBorderType(_type);
								_propList.put("borderType", _type);
							}

						}

						_propList.put(_name, _bSide);
					}
					else if( _name.equals("x"))
					{
						_propList.put(_name, Float.valueOf(  _value.toString() ) + _cloneX);
						_propList.put("left", Float.valueOf(  _value.toString() ) + _cloneX);
					}
					else if( _name.equals("y"))
					{
						_propList.put(_name, Float.valueOf(  _value.toString() ) + _cloneY);
						_propList.put("top", Float.valueOf(  _value.toString() ) + _cloneY);
					}
					else if( _name.equals("type") )
					{
						_model_type = _value;
						_propList.put(_name, _value);
					}
					else if( _name.equals("barcodeType") )
					{
						_barcode_type = _value;
						_propList.put(_name, _value);
					}
					else
					{
						_propList.put(_name, _value);
					}
				}
				else if(_name.equals("dataType"))
				{
					_dataType=_value;

					if( _value.equals("1") )
					{
						ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
						
						if( _dCnt >= _list.size()) 
						{
							_propList.put("text", "");
							continue;
						}
						
						HashMap<String, Object> _dataHm = _list.get(_dCnt);

						Object _dataValue = _dataHm.get(_dataColumn);

						_propList.put("text", _dataValue == null ? "" : _dataValue);
					}
					else if( _value.equals("3"))
					{
						String _txt = _propList.get("text").toString();
						int _inOf = _txt.indexOf("{param:");
						String _pKey = "";
						if( _inOf != -1 )
						{
							_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);

							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

							String _pValue = _pList.get("parameter");

							if( _pValue.equals("undefined"))
							{
								_propList.put("text", "");
							}
							else
							{
								_propList.put("text", _pValue);
							}

						}
						else
						{
							_propList.put("text", "");
						}
					}
				}

				
				else if( _name.equals("data") )
				{
					if(_className.equals("UBImage"))
					{
						// test
						/*
						String projName = m_appParams.getREQ_INFO().getPROJECT_NAME();
						String formName = m_appParams.getREQ_INFO().getFORM_ID();
						
						String	_url = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getImageData&PROJECT_NAME=" + projName + "&FORM_ID=" + formName + "&ITEM_ID="+_itemId;
						_propList.put("src", _url);
						
						if(!mImageData.containsKey(_itemId))
						{
							_value = URLDecoder.decode(_value, "UTF-8");
							mImageData.put(_itemId, _value);
						}
						*/
						
						_value = URLDecoder.decode(_value, "UTF-8");
						_propList.put("src",  URLEncoder.encode(_value, "UTF-8"));
					}
				}
				
				
				else if( _name.equals("prefix") ){
					_prefix=URLDecoder.decode(_value, "UTF-8");
				}
				
				else if( _name.equals("suffix") ){
					//_suffix=URLDecoder.decode(_value, "UTF-8");
					_suffix=_value;
				}
				
				else if( _name.equals("systemFunction") ){

					if( !_value.equalsIgnoreCase("null") && !_value.equalsIgnoreCase("") ){
						_value = URLDecoder.decode(_value, "UTF-8");
						mFunction.setDatasetList(_data);
						
						String _fnValue;
						
						if( mFunction.getFunctionVersion().equals("2.0") ){
							_fnValue = mFunction.testFN(_value, _dCnt, 0, _dCnt , -1 , -1 , "" );
						}else{
							_fnValue = mFunction.function(_value, _dCnt, 0, _dCnt , -1 , -1 , "");
						}
						
						_propList.put("text", _fnValue);
					}
				}

				else if(_name.equals("column"))
				{
					_dataColumn = _value;
				}
				else if(_name.equals("dataSet"))
				{
					_dataID = _value;
				}
				else if(_name.equals("dataSets"))
				{
					_datasets=_value.split("%2C");
				}
				else if(_name.equals("startPoint"))
				{

					_value = URLDecoder.decode(_value, "UTF-8");
					String[] _sPoint = _value.split(",");

					_propList.put("x1", Float.valueOf( _sPoint[0] ) + _cloneX );
					_propList.put("y1", Float.valueOf(_sPoint[1] ) + _cloneY );
				}
				else if(_name.equals("endPoint"))
				{
					_value = URLDecoder.decode(_value, "UTF-8");
					String[] _ePoint = _value.split(",");

					_propList.put("x2", Float.valueOf(_ePoint[0] ) + _cloneX );
					_propList.put("y2", Float.valueOf(_ePoint[1] ) + _cloneY );
				}
				else if(_name.equals("lineThickness"))
				{
					_propList.put("thickness", _value);
				}
				else if(_name.equals("borderThickness"))
				{
					_propList.put("borderWidth", _value);
				}
				else if(_name.equals("formatter"))
				{
					_formatter = _value;
				}

				else if(_name.equals("nation"))
				{
					_nation = URLDecoder.decode(_value, "UTF-8");
				}

				else if(_name.equals("align"))
				{
					_align=_value;
				}
				else if( _name.equals("mask") ){
					_mask = URLDecoder.decode(_value, "UTF-8");
				}

				else if( _name.equals("decimalPointLength") ){

					_decimalPointLength = Integer.parseInt(_value);
				}				

				else if( _name.equals("useThousandComma") ){
					_useThousandComma = Boolean.parseBoolean(_value);
				}		
				else if( _name.equals("isDecimal") ){
					_isDecimal = Boolean.parseBoolean(_value);
				}		
				else if( _name.equals("formatString") ){
					_formatString = _value;
				}	


			}
					
			
			if(_className.equals("UBImage") ){
				if( _dataType.equals("1") )
				{
					String projName = m_appParams.getREQ_INFO().getPROJECT_NAME();
					String formName = m_appParams.getREQ_INFO().getFORM_ID();
//					String	_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&PROJECT_NAME=" + projName + "&FORM_ID=" + formName + "&ITEM_ID="+_itemId;
					
					ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
					HashMap<String, Object> _dataHm = _list.get(_dCnt);
					Object _dataValue = _dataHm.get(_dataColumn);
					String _url="";
					String _txt = _dataValue.toString();
					_url= _prefix + _txt + _suffix;

					String	_servicesUrl = Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getUrlImageData&IMG_URL="+_url;
					
					_propList.put("src", _servicesUrl);
				}
			}
			
			
			for(int _ubfxIndex = 0; _ubfxIndex < _ubfunction.getLength(); _ubfxIndex++)
			{
				Element _ubfxItem = (Element) _ubfunction.item(_ubfxIndex);

				String _name = _ubfxItem.getAttribute("property");
				String _value = _ubfxItem.getAttribute("value");
				
				_value = URLDecoder.decode(_value, "UTF-8");
				
				mFunction.setDatasetList(_data);
				
				String _fnValue;
				
				if( mFunction.getFunctionVersion().equals("2.0") ){
					_fnValue = mFunction.testFN(_value, _dCnt, 0, _dCnt, -1 , -1 , "");
				}else{
					_fnValue = mFunction.function(_value, _dCnt, 0, _dCnt, -1 , -1 , "");
				}
				
				_propList.put(_name, _fnValue);
			}
			
			if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
				String _formatValue="";
				if( _dataType.equalsIgnoreCase("1") ){
					ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
					
					HashMap<String, Object> _dataHm;
					if( _list.size() > _dCnt  ){
						_dataHm = _list.get(_dCnt);	
						Object _dataValue = _dataHm.get(_dataColumn);
						_formatValue = _dataValue.toString();
					}

				}else{
					_formatValue = _propList.get("text").toString();
				}
				try {
					
					if( _formatter.equalsIgnoreCase("Currency") ){
						_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
					}else if( _formatter.equalsIgnoreCase("Date") ){
						_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
					}else if( _formatter.equalsIgnoreCase("MaskNumber") ){
						_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
					}else if( _formatter.equalsIgnoreCase("MaskString") ){
						_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
					}
				} catch (ParseException e) {
//					e.printStackTrace();
				}
				_propList.put("text", _formatValue);
			}

			// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
			if(_propList.containsKey("text"))
				_propList.put("tooltip", _propList.get("text").toString());
			
			
			///////
			
			if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2"))
			{
				int _itmWidth = Integer.valueOf(_propList.get("width").toString());
				int _itmheight = Integer.valueOf(_propList.get("height").toString());
				
				if(_className.equals("UBQRCode"))
				{
					_propList.put("type" , "qrcodeSvgCtl");
					//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _propList.get("text").toString());
					
					// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
					String _barcodeValue = "";
					{
						//ViewerInfo5 vi5 = new ViewerInfo5();
				    	String SHOW_LABEL = "false";
				    	String IMG_TYPE = "qrcode";
				    	String MODEL_TYPE = _barcode_type;
				    	String FILE_CONTENT = _propList.get("text").toString();
				    	
				    	try {
							//_barcodeValue = vi5.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
							_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//_barcodeValue = URLDecoder.decode(_barcodeValue, "UTF-8");
					_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
				}
				else
				{
					boolean _showLabel = _propList.containsKey("showLabel") ? Boolean.valueOf((String)_propList.get("showLabel")) : true;
					
					String _barcodeData = _propList.get("text").toString();
					String _barcodeSrc;
					if( _barcode_type.equalsIgnoreCase("ean13") && _barcodeData.length() != 12 ){
						_barcodeSrc="";
					}else if( _barcode_type.equalsIgnoreCase("ean8") && _barcodeData.length() != 7 ){
						_barcodeSrc="";
					}else if( _barcode_type.equalsIgnoreCase("upc") && _barcodeData.length() != 11 ){
						_barcodeSrc="";
					}
					/*
					else if( _barcode_type.equalsIgnoreCase("itf14") && _barcodeData.length() != 14 ){
						_barcodeSrc="";
					}
					*/
					else
					{
						if(StringUtil.containsKorean(_barcodeData))
						{
							_barcodeSrc="";
						}
						else
						{
							if("datamatrix".equals(_barcode_type))
							{	
								_barcode_type = Math.ceil(_itmWidth / _itmheight) > 1 ? _barcode_type + "2" : _barcode_type;
							}
							_barcodeSrc=_propList.get("src").toString() + "&SHOW_LABEL=" + _showLabel + "&MODEL_TYPE=" + _barcode_type + "&FILE_CONTENT=" + _barcodeData;
						}
					}
					
					//_propList.put("src" , _barcodeSrc);
					
					// base64 이미지 데이터로 대신하여 클라이언트로 보내도록 함
					String _barcodeValue = "";
					if(!"".equals(_barcodeSrc))
					{
						//ViewerInfo5 vi5 = new ViewerInfo5();
				    	String SHOW_LABEL = _showLabel ? "true" : "false";
				    	String IMG_TYPE = "barcode";
				    	String MODEL_TYPE = _barcode_type;
				    	String FILE_CONTENT = _barcodeData;
				    	
				    	try {
				    		if("datamatrix".equals(MODEL_TYPE))
							{	
				    			MODEL_TYPE = Math.ceil(_itmWidth / _itmheight) > 1 ? MODEL_TYPE + "2" : MODEL_TYPE;
							}
							//_barcodeValue = vi5.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
							_barcodeValue = common.getLocalBarcodeImageToBase64(SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//_barcodeValue = URLDecoder.decode(_barcodeValue, "UTF-8");
					_propList.put("src",  URLEncoder.encode(_barcodeValue, "UTF-8"));
				}		
			}
			else if(_className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart") || _className.equals("UBBubbleChart"))
			{
				String PROJECT_NAME = m_appParams.getREQ_INFO().getPROJECT_NAME();
				String FOLDER_NAME = m_appParams.getREQ_INFO().getFORM_ID();
				
				String IMG_TYPE = "";
				String PARAM = ",,,,,,,,,,,,,,,,,,,,"; // 21개 파라미터항목
				
				HashMap<Integer, String> displayNamesMap=null;
				
				PARAM = mPropertyFn.getChartParamToElement(_childItem);
				if(_className.equals("UBPieChart"))
				{
					_propList.put("type" , "pieChartCtl");
					IMG_TYPE = "pie";
				}
				else if(_className.equals("UBLineChart"))
				{
					_propList.put("type" , "lineChartCtl");
					IMG_TYPE = "line";
				}
				else if(_className.equals("UBBarChart"))
				{
					_propList.put("type" , "barChartCtl");
					IMG_TYPE = "bar";
				}
				else if(_className.equals("UBColumnChart"))
				{
					_propList.put("type" , "columnChartCtl");
					IMG_TYPE = "column";
				}
				else if(_className.equals("UBAreaChart"))
				{
					_propList.put("type" , "areaChartCtl");
					IMG_TYPE = "area";
				}
				else if(_className.equals("UBCombinedColumnChart"))
				{
					displayNamesMap  = getChartParamToElement2(_childItem );
					_propList.put("type" , "combinedColumnChartCtl");
					IMG_TYPE = "combcolumn";
				}
				else if(_className.equals("UBBubbleChart"))
				{
					_propList.put("type" , "bubbleChartCtl");
					IMG_TYPE = "bubble";
				}
				
				String _chartValue = "";
				if(IMG_TYPE.equals("combcolumn"))
				{
					String [] arrDataId = _datasets;
					
					ArrayList<ArrayList<HashMap<String, Object>>> _dslist = new ArrayList<ArrayList<HashMap<String, Object>>>();
					
					for(int i=0; i< arrDataId.length; i++)
					{
						ArrayList<HashMap<String, Object>> _list = _data.get(arrDataId[i]);
						_dslist.add(_list);
					}
					
					//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _model_type + "&PARAM=" + PARAM + "&FILE_NAME=" + this.mChartDataFileName + "&PROJECT_NAME=" + PROJECT_NAME + "&FORM_ID=" + FOLDER_NAME + "&DATASET=" + _dataID );
					
					if(!"".equals(IMG_TYPE))
					{
						//ViewerInfo5 vi5 = new ViewerInfo5();
						String FILE_NAME = this.mChartDataFileName;
						//String DATA_ID = _dataID;
						String MODEL_TYPE = _model_type;
						int _itmWidth = Integer.valueOf(_propList.get("width").toString());
						int _itmheight = Integer.valueOf(_propList.get("height").toString());
						
				    	try {
				    		//_chartValue = vi5.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
				    		_chartValue = common.getLocalChartImageToBase64M(_dslist, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, displayNamesMap, MODEL_TYPE);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else
				{
					ArrayList<HashMap<String, Object>> _list = _data.get(_dataID);
					/*
					if(!mChartData.containsKey(_itemId) && _list != null && _list.size() > 0)
					{
						mChartData.put(_dataID, _list);
					}
					*/		
					
					//_propList.put("src" , _propList.get("src").toString() + "&MODEL_TYPE=" + _model_type + "&PARAM=" + PARAM + "&FILE_NAME=" + this.mChartDataFileName + "&PROJECT_NAME=" + PROJECT_NAME + "&FORM_ID=" + FOLDER_NAME + "&DATASET=" + _dataID );
					
					if(!"".equals(IMG_TYPE))
					{
						//ViewerInfo5 vi5 = new ViewerInfo5();
						String FILE_NAME = this.mChartDataFileName;
						//String DATA_ID = _dataID;
						String MODEL_TYPE = _model_type;
						int _itmWidth = Integer.valueOf(_propList.get("width").toString());
						int _itmheight = Integer.valueOf(_propList.get("height").toString());
						
				    	try {
				    		//_chartValue = vi5.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
				    		_chartValue = common.getLocalChartImageToBase64(_list, _itmWidth, _itmheight, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, PARAM, MODEL_TYPE);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				//_barcodeValue = URLDecoder.decode(_chartValue, "UTF-8");
				_propList.put("src",  URLEncoder.encode(_chartValue, "UTF-8"));
			}
			
			// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
			if(_propList.containsKey("text"))
			{
				String _value = _propList.get("text").toString();
				_propList.put("tooltip", _value);
			}
			
			_propList.put("className" , _className );
			_propList.put("id" , _itemId );

			_objects.add(_propList);

		}
		
		return _objects;
	}
	
	
	private static HashMap<Integer, String> getChartParamToElement2( Element _element)
	{
		// Chart의 데이터값을 추출
		int i = 0;
		int j = 0;
//		XPath _xpath = XPathFactory.newInstance().newXPath();
		String _legendLabelPlacement = "right";
		String _legendMarkHeight = "10";
		String _legendMarkWeight = "10";
		ArrayList<String> chartList = getChartPropertys2();
		HashMap<String, String> chartData = new HashMap<String, String>();
		HashMap<String, String> displayMap = new HashMap<String, String>();
		NodeList list;
		NodeList displaylist;
		
		
		HashMap<Integer, String> displayNamesMap = new HashMap<Integer, String>();
		NodeList list_displayNames;
		
		Element _itemData;
		Element _displayItemData;
		
		String yFieldsStr = "";
		String yFieldsDisplayStr = "";
		String yFieldsColorStr = "";

		String returnString = "";
		int _max = 0;
		int _subMax = 0;
		
		try {
//			list = (NodeList) _xpath.evaluate("./property", _element, XPathConstants.NODESET);
			list = _element.getElementsByTagName("property");
			
			for ( i = 0; i < list.getLength(); i++) {
				
				_itemData = (Element) list.item(i);
				
				if( chartList.indexOf( _itemData.getAttribute("name") ) != -1  )
				{
					
					if( _itemData.getAttribute("name").equalsIgnoreCase("seriesXFields")  ){
						String _xfieldsValue= URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8");
						_xfieldsValue=_xfieldsValue.replaceAll("," , "~" );
						chartData.put(_itemData.getAttribute("name") , _xfieldsValue  );	
					}else{
						chartData.put(_itemData.getAttribute("name") , URLDecoder.decode(_itemData.getAttribute("value") , "UTF-8")  );	
					}
					
				}
			}
//			list = (NodeList) _xpath.evaluate("./displayName/column", _element, XPathConstants.NODESET);
			
			
			
			
			
			
			
			list_displayNames = ((Element)_element.getElementsByTagName("displayNames").item(0)).getElementsByTagName("displayName");
			int _max_displayNames=list_displayNames.getLength();
			
			//Element _itemData_displayNames;
			for( int dispIndex=0; dispIndex<_max_displayNames;  dispIndex++ ){
				
				returnString="";
				yFieldsStr="";
				yFieldsDisplayStr="";
				yFieldsColorStr="";
				
				list = ((Element)list_displayNames.item(dispIndex)).getElementsByTagName("column");
				
				//list = ((Element)_element.getElementsByTagName("displayName").item(0)).getElementsByTagName("column");
				_max = list.getLength();
				
				for ( i = 0; i < _max; i++) {
					
					_itemData = (Element) list.item(i);
//					displaylist = (NodeList) _xpath.evaluate("./property", _itemData, XPathConstants.NODESET);
					displaylist = _itemData.getElementsByTagName("property");
					displayMap.clear();
					
					_subMax = displaylist.getLength();
					
					for ( j = 0; j < _subMax; j++) {
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
				
				_max = chartList.size();
//				for ( i = 0; i < chartList.size(); i++) {
				for ( i = 0; i < _max; i++) {
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
					else if( chartList.get(i).equals("legendLabelPlacement") )
					{
						returnString = returnString + yFieldsColorStr;
					}
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
				
				
				displayNamesMap.put(dispIndex, returnString);
			}
			
			
			
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return displayNamesMap;
	}
	
	private static ArrayList<String> getChartPropertys2()
	{
		String chartPropertyStr = "seriesXFields,yFieldName,yFieldDisplayName,isCrossTabData,form,gridLine,gridLineWeight,gridLIneDirection,gridLIneColor," +
				"legendDirection,legendLabelPlacement,legendMarkHeight,legendMarkWidthlegendLocation,dataLabelPostion," +
				"isCrossTabData,isDuplication,yFieldFillColor,closeField,highField,lowField,openField";
		ArrayList<String> chartPropertyList = new ArrayList(Arrays.asList(chartPropertyStr.split(",")));
		
		return chartPropertyList;
	}
	
	
	
	
	
	
	
	
	


	public ArrayList<HashMap<String, Object>> createFreeFormConnectPrint(Element _page, ArrayList<HashMap<String, Object>> _pageItems, HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt, float _cloneX, float _cloneY, ArrayList<HashMap<String, Object>> _objects, int _totPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		NodeList _child = _page.getElementsByTagName("item");
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		ArrayList<Object> _resultAr = new ArrayList<Object>();
		
		if(_pageItems == null)
		{
			// xml Item
			for(int j = 0; j < _child.getLength() ; j++)
			{
				Element _childItem = (Element) _child.item(j);
				
				String _itemId = _childItem.getAttribute("id");
				String _className = _childItem.getAttribute("className");
				
				HashMap<String, Object> _propList = new HashMap<String, Object>();
				
				if( _className.equals("UBTable") || _className.equals("UBApproval") )
				{
					try {
						mItemConvertFn.convertElementTableToItemClone(_childItem , _dCnt , _data , _param, _cloneX, _cloneY, -1,_objects, _totPageNum, _currentPageNum, false);
					} catch (XPathExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					_propList = mItemConvertFn.convertElementToItem(_childItem, _dCnt, _data, _param,  _cloneX, _cloneY, -1, _totPageNum, _currentPageNum, false);
					if( _propList != null )
					{
						_propList.put("className" , _className );
						_propList.put("id" , _itemId );
						_propList.put("ELEMENT_XML", _childItem);
						_objects.add(_propList);
					}
				}
				
			}
			
		}
		else
		{
			int itemSize = _pageItems.size();
			for (int i = 0; i < itemSize; i++) {
				HashMap<String, Object> _propList = new HashMap<String, Object>();
				_propList = mItemConvertFn.convertElementToItem_Clone( (HashMap<String,Object>) _pageItems.get(i).clone(), _dCnt, _data, _param,  _cloneX, _cloneY, -1, _totPageNum, _currentPageNum, false);
				_objects.add((HashMap<String, Object> ) _propList.clone() );
			}
			
		}
		
		return _objects;
	}
	
	


	public ArrayList<HashMap<String, Object>> createFreeFormConnectItemList(ArrayList<HashMap<String, Object>> _items , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt, float _cloneX, float _cloneY, ArrayList<HashMap<String, Object>> _objects, int _totPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		 
		// xml Item
		for(int j = 0; j < _items.size() ; j++)
		{

			String _itemId = _items.get(j).get("id").toString();
			String _className = _items.get(j).get("className").toString();

			HashMap<String, Object> _propList = new HashMap<String, Object>();
			_propList = mItemConvertFn.convertHashMapToItem((HashMap<String, Object>) _items.get(j).clone(), _dCnt, _data, _param,  _cloneX, _cloneY, -1, _totPageNum, _currentPageNum, false);
			if( _propList != null )
			{
				_propList.put("className" , _className );
				_propList.put("id" , _itemId );
				
				_objects.add(_propList);
			}

		}
		
		return _objects;
	}
	
	
	
	private String getNodeString(Node node) {
	    try {
	        StringWriter writer = new StringWriter();
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.transform(new DOMSource(node), new StreamResult(writer));
	        String output = writer.toString();
	        return output.substring(output.indexOf("?>") + 2);//remove <?xml version="1.0" encoding="UTF-8"?>
	    } catch (TransformerException e) {
	        e.printStackTrace();
	    }
	    return node.getTextContent();
	}
	
	private Element getStringNode( String _xml)
	{
		Element node = null;
		try {
			node = DocumentBuilderFactory
				    .newInstance()
				    .newDocumentBuilder()
				    .parse(new ByteArrayInputStream(_xml.getBytes()))
				    .getDocumentElement();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return node;
	}
	

	public ArrayList<HashMap<String, Object>> createFreeFormConnectThr(Element _mpage , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt, float _cloneX, float _cloneY, ArrayList<HashMap<String, Object>> _objects, int _totPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		String _eleStr = getNodeString(_mpage);
		Element _page = getStringNode(_eleStr);

		NodeList _child = _page.getElementsByTagName("item");
		
//		NodeList _child = _page.getElementsByTagName("item");
		
		//mImageData=new HashMap<String,String>();
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		
		// xml Item
		for(int j = 0; j < _child.getLength() ; j++)
		{
			Element _childItem = (Element) _child.item(j);

			String _itemId = _childItem.getAttribute("id");
			String _className = _childItem.getAttribute("className");
			
			HashMap<String, Object> _propList = new HashMap<String, Object>();
			
			if( _className.equals("UBTable") || _className.equals("UBApproval") )
			{
				try {
					mItemConvertFn.convertElementTableToItem(_childItem, _dCnt , _data , _param, _cloneX, _cloneY, -1,_objects, _totPageNum, _currentPageNum, false);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				_propList = mItemConvertFn.convertElementToItem( _childItem, _dCnt, _data, _param,  _cloneX, _cloneY, -1, _totPageNum, _currentPageNum, false);
				if( _propList != null )
				{
					_propList.put("className" , _className );
					_propList.put("id" , _itemId );
					
					_objects.add(_propList);
				}
			}

		}
		
		return _objects;
	}
	
	public ArrayList<HashMap<String, Object>> createFreeFormConnect(PageInfo _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt, float _cloneX, float _cloneY, ArrayList<HashMap<String, Object>> _objects, int _totPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		ArrayList<HashMap<String, Value>> _child = _page.getItems();
		
		//mImageData=new HashMap<String,String>();
		mTempletInfo = _page.getProjectInfo().getTempletInfo();
		
		int _minimumResizeFontSize = 0;
		if( _page.getMinimumResizeFontSize()  > 0 )
		{
			_minimumResizeFontSize = _page.getMinimumResizeFontSize();
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		
		ArrayList<HashMap<String, Object>> _radioButtons = new ArrayList<HashMap<String, Object>>();
		
		// xml Item
		for(int j = 0; j < _child.size() ; j++)
		{
			HashMap<String, Value> _childItem = _child.get(j);

			String _itemId = _childItem.get("id").getStringValue();
			String _className = _childItem.get("className").getStringValue();

			HashMap<String, Object> _propList = new HashMap<String, Object>();
			
			if( _className.equals("UBTemplateArea") )
			{	
				_objects = mTempletInfo.get(_itemId).convertItemData( _dCnt, _cloneX, _cloneY, _objects, mFunction, null, _currentPageNum, _totPageNum, -1, -1, changeItemList, null, mItemConvertFn);
			}
			else
			{
				_propList = mItemConvertFn.convertItemDataJson( null, _childItem, _data, _dCnt, _param, -1, -1, _totPageNum, _currentPageNum, "", _cloneX, _cloneY, -1);
				if( _propList != null )
				{
					_propList.put("className" , _className );
					
					//RadioButtonGroup은 RadioButton보다 앞서서 생성되어야 한다
					if( _className.equals("UBRadioButtonGroup") )
					{
						_objects.add(0, _propList);
					}
					else
					{
						_propList.put("id" , _itemId );
						_objects.add(_propList);
						
						if( _className.equals("UBRadioBorder") )
						{
							_radioButtons.add(_propList);
						}
					}
					
				}
			}

		}
		
		if( _radioButtons.size() > 0 )
		{
			for (int i = 0; i < _radioButtons.size(); i++) {
				Boolean _isSelected = mItemConvertFn.radiobuttonHandler(_radioButtons.get(i));
				_radioButtons.get(i).put("selected", _isSelected);
			}
			_radioButtons.clear();
		}
		
		return _objects;
	}
	
	public ArrayList<HashMap<String, Object>> createFreeFormConnect(PageInfoSimple _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt, float _cloneX, float _cloneY, ArrayList<HashMap<String, Object>> _objects, int _totPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		ArrayList<HashMap<String, Object>> _child = _page.getItems();
		
		//mImageData=new HashMap<String,String>();
		mTempletInfo = _page.getProjectInfo().getTempletInfo();
		
		int _minimumResizeFontSize = 0;
		if( _page.getMinimumResizeFontSize()  > 0 )
		{
			_minimumResizeFontSize = _page.getMinimumResizeFontSize();
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		
		ArrayList<HashMap<String, Object>> _radioButtons = new ArrayList<HashMap<String, Object>>();
		
		// xml Item
		int _childSize = _child.size();
		for(int j = 0; j < _childSize ; j++)
		{
			HashMap<String, Object> _childItem = _child.get(j);

			String _itemId = _childItem.get("id").toString();
			String _className = _childItem.get("className").toString();

			HashMap<String, Object> _propList = new HashMap<String, Object>();
			
			if( _className.equals("UBTemplateArea") )
			{	
				_objects = mTempletInfo.get(_itemId).convertItemData( _dCnt, _cloneX, _cloneY, _objects, mFunction, null, _currentPageNum, _totPageNum, -1, -1, changeItemList, null, mItemConvertFn);
			}
			else
			{
				_propList = mItemConvertFn.convertItemDataSimple( null, _childItem, _data, _dCnt, _param, -1, -1, _totPageNum, _currentPageNum, "", _cloneX, _cloneY, -1);
				if( _propList != null )
				{
					_propList.put("className" , _className );
					
					//RadioButtonGroup은 RadioButton보다 앞서서 생성되어야 한다
					if( _className.equals("UBRadioButtonGroup") )
					{
						_objects.add(0, _propList);
					}
					else
					{
						_propList.put("id" , _itemId );
						_objects.add(_propList);
						
						if( _className.equals("UBRadioBorder") )
						{
							_radioButtons.add(_propList);
						}
					}
					
				}
			}

		}
		
		if( _radioButtons.size() > 0 )
		{
			for (int i = 0; i < _radioButtons.size(); i++) {
				Boolean _isSelected = mItemConvertFn.radiobuttonHandler(_radioButtons.get(i));
				_radioButtons.get(i).put("selected", _isSelected);
			}
			_radioButtons.clear();
		}
		
		return _objects;
	}
	
	
	public ArrayList<HashMap<String, Object>> createFreeFormConnectPdf(PageInfoSimple _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt, float _cloneX, float _cloneY, ArrayList<HashMap<String, Object>> _objects, int _totPageNum, int _currentPageNum, ubFormToPDF _ubPDF, int _exportPageIdx ) throws UnsupportedEncodingException, ScriptException
	{
		ArrayList<HashMap<String, Object>> _child = _page.getItems();
		
		//mImageData=new HashMap<String,String>();
		mTempletInfo = _page.getProjectInfo().getTempletInfo();
		
		int _minimumResizeFontSize = 0;
		if( _page.getMinimumResizeFontSize()  > 0 )
		{
			_minimumResizeFontSize = _page.getMinimumResizeFontSize();
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		
		ArrayList<HashMap<String, Object>> _radioButtons = new ArrayList<HashMap<String, Object>>();
		
		// xml Item
		int _childSize = _child.size();
		for(int j = 0; j < _childSize ; j++)
		{
			HashMap<String, Object> _childItem = _child.get(j);

			String _itemId = _childItem.get("id").toString();
			String _className = _childItem.get("className").toString();

			HashMap<String, Object> _propList = null;
			
			if( _className.equals("UBTemplateArea") )
			{	
				_objects = mTempletInfo.get(_itemId).convertItemData( _dCnt, _cloneX, _cloneY, _objects, mFunction, null, _currentPageNum, _totPageNum, -1, -1, changeItemList, null, mItemConvertFn);
			}
			else
			{
				_propList = mItemConvertFn.convertItemDataSimple( null, _childItem, _data, _dCnt, _param, -1, -1, _totPageNum, _currentPageNum, "", _cloneX, _cloneY, -1);
				if( _propList != null )
				{
					_propList.put("className" , _className );

					//RadioButtonGroup은 RadioButton보다 앞서서 생성되어야 한다
					if( _className.equals("UBRadioBorder") )
					{
						if( _radioButtons.size() > 0 )
						{
							Boolean _isSelected = mItemConvertFn.radiobuttonHandler(_propList);
							_propList.put("selected", _isSelected);
						}
					}

					try {
						_ubPDF.toPdfOneItem(_propList, String.valueOf(_exportPageIdx) );
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			}

		}
		
		return _objects;
	}
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> CreateLabelBandAll(PageInfoSimple _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt , int _totalPageNum, int _currentPageNum) throws UnsupportedEncodingException, ScriptException
	{
		return CreateLabelBandAll( _page , _data , _param , _dCnt , _totalPageNum, _currentPageNum, 0, 0);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> CreateLabelBandAll(PageInfoSimple _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data , HashMap<String, Object> _param , int _dCnt , int _totalPageNum, int _currentPageNum, float _cloneX, float _cloneY ) throws UnsupportedEncodingException, ScriptException
	{
		String _methodName = m_appParams.getREQ_INFO().getMETHOD_NAME();
		boolean _isMarkAny = "saveMarkAnyData".equals(_methodName);
		
		// return ArrayList
		ArrayList<HashMap<String, Object>> _objects = new ArrayList<HashMap<String, Object>>();
		
		mItemConvertFn.setImageData(this.mImageData);
		mItemConvertFn.setFunction(mFunction);
		mItemConvertFn.setChartData(mChartData);
		mItemConvertFn.setIsExportType(isExportType);
		
		// Page 의 Item list
		ArrayList<HashMap<String, Object>> _child = _page.getItems();
		
		// DataSet 담는 객채
		ArrayList<HashMap<String, Object>> _list = new ArrayList<HashMap<String,Object>>();
		
		int _minimumResizeFontSize = 0;
		if( _page.getMinimumResizeFontSize() > 0 )
		{
			_minimumResizeFontSize = _page.getMinimumResizeFontSize();
			mItemConvertFn.setMinimumResizeFontSize(_minimumResizeFontSize);
		}
		
		String _className = "";
		// Data 건수 조회
		int _dataMaxCnt = _page.getDataRowCount();
		int _itemSize = _child.size();
		// Label Band Property 담기.
		HashMap<String, Object> _labelProperty = null;
		
		for( int i=0; i < _itemSize; i++ )
		{
			_className = _child.get(i).get("className").toString();
			
			if( _className.equals("UBLabelBand"))
			{
				_labelProperty = _child.get(i);
				break;
			}
			
		}
		
		if( _labelProperty == null ) return null;
		
		// 가로, 세로 갯수 확인후 Page에 들어가는 총 Band 갯수 구하기.
		int _columns = Integer.valueOf(_labelProperty.get("columns").toString()); 
		int _rows = Integer.valueOf(_labelProperty.get("rows").toString());
		int _pageMaxCnt = (_columns * _rows) * (_dCnt + 1);
		
		// 반복하는 Item의 좌표 및 방향
		float _bandX = 		Float.valueOf(UBComponent.getProperties(_labelProperty, _className, "border_x").toString());
		float _bandY = 		Float.valueOf(UBComponent.getProperties(_labelProperty, _className, "border_y").toString());
		float _bandWidth = 	Float.valueOf(UBComponent.getProperties(_labelProperty, _className, "border_width").toString());
		float _bandHeight = Float.valueOf(UBComponent.getProperties(_labelProperty, _className, "border_height").toString());
		
		String _bandDirection =  UBComponent.getProperties(_labelProperty, _className, "direction").toString();
		String _borderVisible =  UBComponent.getProperties(_labelProperty, _className, "borderVisible","").toString();
		// Data 시작 Number 구하기 , 증가할 dataNum 선언.
		int _dataStNum = 0;
		
		_dataStNum = (_columns * _rows) * _dCnt;
		
		int _pageStartIndex = 0;
		
		// param의 startIndex값이 잇을경우 _pageMaxCnt값과 _dataStNum에서 -처리 시작 bandIndex값을 _dataStNum값이 이 0일경우 startIndex값으로 셋팅 
		if( _param.containsKey(GlobalVariableData.UB_LABEL_ST_INDEX) || ( _param.containsKey(GlobalVariableData.UB_LABEL_COL_INDEX) && _param.containsKey(GlobalVariableData.UB_LABEL_ROW_INDEX) ) )
		{
			HashMap<String, String> _pList;
			int _stColIndex = 0;
			int _stRowIndex = 0;
			int _stIndex = 0;
			
			if(_param.containsKey(GlobalVariableData.UB_LABEL_ST_INDEX) == false)
			{
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_COL_INDEX);
				_stColIndex = Integer.valueOf( _pList.get("parameter") );
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_ROW_INDEX);
				_stRowIndex = Integer.valueOf( _pList.get("parameter") );
				
				if( _stColIndex > 0 && _stColIndex <= _columns &&  _stRowIndex > 0 && _stRowIndex <= _rows )
				{
					if("downCross".equals(_bandDirection)) _pageStartIndex = (_rows * (_stColIndex-1)) + _stRowIndex -1;
					else _pageStartIndex = (_columns * (_stRowIndex-1)) + _stColIndex -1;
				}
				else
				{
					_pageStartIndex = 0;
				}
				
			}
			else
			{
				_pList = (HashMap<String, String>) _param.get(GlobalVariableData.UB_LABEL_ST_INDEX);
				_stIndex = Integer.valueOf( _pList.get("parameter") );
				
				if( _stIndex > 0 )
				{
					_pageStartIndex = _stIndex;
				}
				else
				{
					_pageStartIndex = 0;
				}
			}
				
			
			if(_pageStartIndex > 0 )
			{
				_pageMaxCnt = _pageMaxCnt - _pageStartIndex;
				if( _dataStNum > 0 )
				{
					_dataStNum = _dataStNum - _pageStartIndex;
					
					if(_dataStNum < 0 )
					{
						_dataStNum = 0;
					}
				}
				
				_pageStartIndex = _pageStartIndex - ((_columns * _rows) * _dCnt);
				if(_pageStartIndex < 0 ) _pageStartIndex = 0;
			}
			
		}
		
		if( _pageMaxCnt > _dataMaxCnt)
		{
			_pageMaxCnt = _dataMaxCnt;
		}
		
		HashMap<String, Object> _childItem;
		_itemSize = _child.size();
		// xml Item Parsing
		for (int t = 0; t < _itemSize; t++) 
		{
			_childItem = _child.get(t);
			
			String _excelFormatterStr = "";
			String _id = _childItem.get("id").toString();
			_className = _childItem.get("className").toString();
			
			HashMap<String, Object> _propList = new HashMap<String, Object>();
			// 해당 Item의 Property 확인.
			
			// dataset 정보 담는 객채
			String _dataTypeStr = "";
			String _datasetColumn = "";
			String _datasetId = "";
			
			// formatter variables
			String _formatter="";
			String _nation="";
			String _align="";
			String _dataType="";
			String _mask="";
			String _inputForamtString="";
			String _outputFormatString="";
			
			int _decimalPointLength=0;
			Boolean _useThousandComma=false;
			Boolean _isDecimal=false;
			String _formatString="";
			String _systemFunction = "";

			ArrayList<HashMap<String, Object>> _arrayList = new ArrayList<HashMap<String, Object>>();
			
			_propList = mItemConvertFn.convertItemDataSimple(null, _childItem, _data, _dCnt, _param, -1, -1, _totalPageNum, _currentPageNum, "",0,0,  -1 );
			
			if( _propList == null )
			{
				continue;
			}
				
			if(_childItem.get("className").toString().equals("UBLabelBand"))
			{
				if( _borderVisible.equals("true"))
				{
					_propList.put("x", _labelProperty.get("border_x").toString());
					_propList.put("y", _labelProperty.get("border_y").toString());
					_propList.put("width", _labelProperty.get("border_width").toString());
					_propList.put("height", _labelProperty.get("border_height").toString());
				}
				else
				{
					_propList.clear();
				}
			}
			else
			{
				if(_propList.get("band_x") != null ) _propList.put("x", _propList.get("band_x"));
				if(_propList.get("band_y") != null ) _propList.put("y", _propList.get("band_y"));
			}
			
			if( _propList.isEmpty() == true && _arrayList.size() == 0)
			{
				continue;
			}
			
			
			if( _arrayList.size() == 0 )
			{
				_arrayList.add(_propList);
			}
			
			HashMap<String, Object> _labelItem = new HashMap<String, Object>();
			
			for (int i = 0; i < _arrayList.size(); i++) 
			{
				HashMap<String, Object> _arItem = _arrayList.get(i);
				
				NodeList formatterItem = null;
				
				boolean _useFormatElement = false;
				
				if( _propList.containsKey("formatter") && !_propList.get("formatter").equals("") && _propList.get("formatter") != null )
				{
					_formatter 			= (_propList.get("formatter") == null)?"": _propList.get("formatter").toString();
					_mask 				= (_propList.get("mask") == null)?"":_propList.get("mask").toString();
					_decimalPointLength = (_propList.get("decimalPointLength") == null)?0:Integer.parseInt(_propList.get("decimalPointLength").toString());
					_useThousandComma 	= (_propList.get("useThousandComma") == null)?false:_propList.get("useThousandComma").toString().equals("true");
					_isDecimal 			= (_propList.get("isDecimal") == null)?false:_propList.get("isDecimal").toString().equals("true");
					_formatString 		= (_propList.get("formatString") == null)?"":_propList.get("formatString").toString();
					_nation 			= (_propList.get("nation") == null)?"":_propList.get("nation").toString();
					_align 				= (_propList.get("currencyAlign") == null)?"":_propList.get("currencyAlign").toString();
					_inputForamtString 	= (_propList.get("inputFormatString") == null)?"":_propList.get("inputFormatString").toString();
					_outputFormatString = (_propList.get("outputFormatString") == null)?"":_propList.get("outputFormatString").toString();
				}
				
				int _bandIdx = _pageStartIndex;
				for (int d = _dataStNum; d < _pageMaxCnt; d++) 
				{
					_labelItem = (HashMap<String, Object>) _arItem.clone();

					_dataTypeStr = String.valueOf(_labelItem.get("dataType"));
					_datasetId = String.valueOf(_labelItem.get("dataSet"));
					_datasetColumn = String.valueOf(_labelItem.get("column"));
					
					_excelFormatterStr = "";
					
					if( _dataTypeStr.equals("1") )
					{
						_list = _data.get(_datasetId);
						if( d >= _list.size()) 
						{
							_labelItem.put("text", "");
							_labelItem.put("tooltip", "");
							continue;
						}
						HashMap<String, Object> _dataHm = _list.get(d);

						Object _dataValue = _dataHm.get(_datasetColumn);

						_labelItem.put("text", _dataValue == null ? "" : _dataValue);

						// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
						_labelItem.put("tooltip", _dataValue == null ? "" : _dataValue);
						
						if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart"))
						{
							//@처리 필요 
							mItemConvertFn.changeLabelBandImgItemSimple(_labelItem, _childItem, _data);
						}
					}
					else if(  _dataTypeStr.equals("2") )
					{
						if( _labelItem.containsKey("systemFunction") )
						{
							_systemFunction = _labelItem.get("systemFunction").toString();
							
							if( !_systemFunction.equalsIgnoreCase("null") && !_systemFunction.equalsIgnoreCase("") ){
								mFunction.setDatasetList(_data);
								
								String _fnValue;
								
								if( mFunction.getFunctionVersion().equals("2.0") ){
									_fnValue = mFunction.testFN(_systemFunction,d, _totalPageNum , _currentPageNum,-1,-1, "" );
								}else{
									_fnValue = mFunction.function(_systemFunction,d, _totalPageNum , _currentPageNum,-1,-1);
								}
								
								_labelItem.put("text", _fnValue);
							}
						}
						
					}
					else if( _dataTypeStr.equals("3"))
					{
						String _txt = _labelItem.get("text").toString();
						int _inOf = _txt.indexOf("{param:");
						String _pKey = "";
						if( _inOf != -1 )
						{
							_pKey = _txt.substring(_inOf + 7 , _txt.length()-1);

							HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_pKey);

							String _value = _pList.get("parameter");

							if( _value.equals("undefined"))
							{
								_labelItem.put("text", "");
							}
							else
							{
								_labelItem.put("text", _value);
							}

						}
						else
						{
							_labelItem.put("text", _txt);
						}
						
						if(_className.equals("UBQRCode") || _className.equals("UBBarCode") || _className.equals("UBBarCode2") || _className.equals("UBPieChart") || _className.equals("UBLineChart") || _className.equals("UBBarChart") || _className.equals("UBColumnChart") || _className.equals("UBAreaChart") || _className.equals("UBCombinedColumnChart"))
						{
							//@처리 필요 
							mItemConvertFn.changeLabelBandImgItemSimple(_labelItem, _childItem, _data);
						}
					}
					
					//Table의 UBFX가 존재할경우 처리( Table의 ubfx를 먼저 처리후 Cell의 ubfx를 처리 )
					if( _childItem.containsKey("ubfx") || _childItem.containsKey("tableUbfunction") )
					{
						ArrayList<HashMap<String, String>> _ubfxs = new ArrayList<HashMap<String, String>>();
						
						ArrayList<ArrayList<HashMap<String, String>>> _ubfxsList = new ArrayList<ArrayList<HashMap<String, String>>>();
						
						int _ubfxListCnt = 0;
						
						if( _childItem.get("tableUbfunction") != null  )
						{
							ArrayList<HashMap<String, String>> _tblUbfxs = (ArrayList<HashMap<String, String>>) _childItem.get("tableUbfunction");
							_ubfxsList.add( _tblUbfxs );
						}
						
						if( _childItem.get("ubfx") != null )
						{
							_ubfxs = (ArrayList<HashMap<String, String>>) _childItem.get("ubfx");
							_ubfxsList.add( _ubfxs );
						}
						
						int _nodeCnts = _ubfxsList.size();
						
						for(int _ubfxListIndex= 0; _ubfxListIndex < _nodeCnts; _ubfxListIndex++)
						{
							ArrayList<HashMap<String, String>> _selectUbfxList = _ubfxsList.get(_ubfxListIndex);
							for(int _ubfxIndex = 0; _ubfxIndex < _selectUbfxList.size(); _ubfxIndex++)
							{
								HashMap<String, String> _ubfxItem = _selectUbfxList.get(_ubfxIndex);
								String _ubfxProperty = _ubfxItem.get("property");
								String _ubfxValue = _ubfxItem.get("value");
								try {
									_ubfxValue = URLDecoder.decode(_ubfxValue, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								mFunction.setDatasetList(_data);
								mFunction.setParam(_param);
								String _fnValue;
								
								if( mFunction.getFunctionVersion().equals("2.0") ){
									_fnValue = mFunction.testFN(_ubfxValue,d,_totalPageNum,_currentPageNum , -1,-1,"");
								}else{
									_fnValue = mFunction.function(_ubfxValue,d,_totalPageNum,_currentPageNum , -1,-1,"");
								}
								
								_fnValue = _fnValue.trim();
								
								if(_fnValue.equals("") == false)
								{
									if(_ubfxProperty.indexOf("Color") != -1 )
									{
										_propList.put((_ubfxProperty + "Int"), mPropertyFn.changeColorHexToInt(_fnValue) );
									}
									_propList.put(_ubfxProperty, _fnValue.trim());			// 20170531 true false에 공백이 붙어 나오는 현상이 있어 수정
									
									// color 속성은 color + Int 속성을 넣어줘야 한다.
									if( _ubfxProperty.contains("Color") ){
										_propList.put((_ubfxProperty + "Int"), common.getIntClor(_fnValue) );
									}
								}
							}
						}
						
					}
					
					//hyperLinkedParam처리
					if( _childItem.containsKey("ubHyperLinkType") && "2".equals( _childItem.get("ubHyperLinkType").toString() )  )
					{
						ArrayList<HashMap<String, String>> _hyperLinkedParam = (ArrayList<HashMap<String, String>>) _childItem.get("ubHyperLinkParm");
						HashMap<String, String> _hyperLinkedParamMap = new HashMap<String, String>();
						
						if( _hyperLinkedParam != null && _hyperLinkedParam.size() > 0 )
						{
							int _hyperLinkedParamSize = _hyperLinkedParam.size();
							
							HashMap<String, String> _hMap;
							
							for(int _hyperIdx = 0; _hyperIdx < _hyperLinkedParamSize; _hyperIdx++ )
							{
								_hMap = _hyperLinkedParam.get(_hyperIdx);
								String _hyperParamKey = "";
								String _hyperParamValue = "";
								String _hyperParamType = "";
								
								if( _hMap.containsKey("id") )
								{
									_hyperParamKey = _hMap.get("value").toString();
								}
								else if( _hMap.containsKey("value") )
								{
									_hyperParamValue = _hMap.get("value").toString();
								}
								else if( _hMap.containsKey("type") )
								{
									_hyperParamType = _hMap.get("value").toString();
								}
								
								if( "DataSet".equals(_hyperParamType) )
								{
									String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
									String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
									
									_hyperParamValue = "";
									
									if(_data.containsKey(_hyperLinkedDataSetId))
									{
										ArrayList<HashMap<String, Object>> _dsList = _data.get( _hyperLinkedDataSetId );
										Object _dataValue = "";
										if( _list != null ){
											if( d < _dsList.size() )
											{
												HashMap<String, Object> _dataHm = _dsList.get(d);
												_hyperParamValue = _dataHm.get( _hyperLinkedDataSetColumn ).toString();
											}
										}
									}
								}
								else if("Parameter".equals(_hyperParamType) )
								{
									String _hyperLinkedDataSetId = _hyperParamValue.substring(0, _hyperParamValue.indexOf("."));
									String _hyperLinkedDataSetColumn = _hyperParamValue.substring( _hyperParamValue.indexOf(".")+1, _hyperParamValue.length() );
									HashMap<String, String> _pList = (HashMap<String, String>) _param.get(_hyperLinkedDataSetColumn);

									String _pValue = _pList.get("parameter");

									if( _pValue.equals("undefined"))
									{
										_hyperParamValue = "";
									}
									else
									{
										_hyperParamValue = _pValue;
									}
								}
								
								_hyperLinkedParamMap.put( _hyperParamKey, _hyperParamValue);
							}
							
							_propList.put("ubHyperLinkParm", _hyperLinkedParamMap);
						}
						
					}

					if( !_formatter.equalsIgnoreCase("null") && !_formatter.equalsIgnoreCase("") ){
						Object _propValue;
						_propValue=_propList.get("text");

						String _formatValue="";
						_formatValue = _labelItem.get("text").toString();
						try {
							if( _formatter.equalsIgnoreCase("Currency") ){
								_formatValue =UBFormatter.currencyFormat("", _nation, _align, _formatValue);
							}
							else if( _formatter.equalsIgnoreCase("Date") ){
								_formatValue=UBFormatter.dateFormat(_formatString, _formatValue);
							}
							else if( _formatter.equalsIgnoreCase("MaskNumber") ){
								_formatValue =UBFormatter.maskNumberFormat(_mask, _decimalPointLength, _useThousandComma, _isDecimal, _formatValue);
							}
							else if( _formatter.equalsIgnoreCase("MaskString") ){
								_formatValue=UBFormatter.maskStringFormat(_mask, _formatValue);
							}
							else if( _formatter.equalsIgnoreCase("CustomDate") )
							{
								_excelFormatterStr = _inputForamtString  + "§" + _outputFormatString;
								_formatValue = UBFormatter.customDateFormatter(_inputForamtString, _outputFormatString, _formatValue);
								
								_propList.put("inputFormatString", _inputForamtString);
								_propList.put("outputFormatString", _outputFormatString);
							}
						} catch (ParseException e) {
//							e.printStackTrace();
						}
						_labelItem.put("text", _formatValue);

						if( isExportType.equals("EXCEL") && _excelFormatterStr.equals("") == false && common.getPropertyValue("excelExport.useFormatter") != null && common.getPropertyValue("excelExport.useFormatter").equals("true") ) 
						{
							_propList.put("EX_FORMATTER", _formatter);
							_propList.put("EX_FORMAT_DATA_STR", _excelFormatterStr);
							_propList.put("EX_FORMAT_ORIGINAL_STR", _propValue.toString() );
						}
					}
					

					float _itemX = 0;
					float _itemY = 0;


					if( _bandDirection.equals("crossDown")) // 아래로
					{
						_itemX = (_bandWidth * (_bandIdx % _columns) ) + (((_bandIdx % _columns)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString()));
						_itemY = (float) ((_bandHeight * ( Math.floor(_bandIdx / _columns))) + ((Math.floor(_bandIdx / _columns)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString())));

					}
					else // 옆으로
					{

						_itemX = (float) ((_bandWidth * (Math.floor(_bandIdx / _rows)) ) + ((Math.floor(_bandIdx / _rows)) * _bandX) + (Float.valueOf(_labelItem.get("x").toString())));
						_itemY = (_bandHeight * (_bandIdx % _rows) ) + (((_bandIdx % _rows)) * _bandY) + (Float.valueOf(_labelItem.get("y").toString()));

					}
					_labelItem.put("x",  _itemX + _cloneX);
					_labelItem.put("y", _itemY + _cloneY);	
					_labelItem.put("top", _itemY + _cloneY);	
					_labelItem.put("left", _itemX + _cloneX);	

					// tooltip 태스트를 위한 코드(추후, ubToolTipType과 ubToolTipContents에 따라 표현되도록 해야 함!)
					if(_labelItem.containsKey("text"))
					{
						String _value = _labelItem.get("text").toString();
						_labelItem.put("tooltip", _value);
					}
					
					if( _labelItem.containsKey("visible") == false ||  "false".equals( _labelItem.get("visible") ) != true) _objects.add(_labelItem);
					_bandIdx++;
				} // for _pageMaxCnt

			}// for arraylist.size();
			
		} // for _child.getLength()
		
		return _objects;
	}	
	
}
