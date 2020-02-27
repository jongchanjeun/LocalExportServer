package org.ubstorm.service.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.ubstorm.service.logger.Log;

public class ItemPropertyVariable {
	
	private boolean mIsMarkAny = false;
	
	public void init()
	{
//		mItemList.put("borderLabel", getborderLabel());
	}
	
	private static String[] ItemPropertyDef = {"id","fontFamily","fontWeight","fontStyle","textDecoration","fontColor","fontSize","textAlign","verticalAlign","lineHeight","borderType","text","label","systemFunction",
		"borderSide","checked","borderThickness","borderWeight","formatter","systemFunction","dataType", "band_y","borderTypes","borderWidths","borderColors","backgroundColor","borderColor"
		,"width","height","x","y","isCell","isTable","leftBorderType","rightBorderType","topBorderType","bottomBorderType","editable","dataSet","column","unMasked","useRequiredValue","validateType","validateScript"
		,"prompt","ubHyperLinkType","ubHyperLinkUrl","ubHyperLinkText","multiLine","inputType","editableBackgroundColor","editableBackgroundVisible","viewerOnlyBackgroundColor","viewerOnlyBackgroundVisible","scriptEvent"
		,"dataLimitRows","distributed","dataProvider","editItemFormatter","editItemFormatValue"};
	
	private static String[] ItemPropertyBarcode = {"id","band_y","width","height","x","y","text","type","showLabel","label", "clipArtData","systemFunction","dataType",
		"backgroundColor","barcodeType","src","data","rotation","editable"};
	
	private static String[] ItemPropertyChart = {"id","band_y","width","height","x","y","columnAxisName","lineAxisName" ,"backgroundColor", "xFieldVisible","yFieldVisible" ,
		"type", "categoryMargin", "itemMargin","src","data","rotation","editable"};
	
	private static String[] ItemPropertyImage = {"id","band_y","width","height","x","y","src","data","rotation","prefix","suffix","tooltip","isOriginalSize","dataSet",
		"column", "fileDownloadUrl","systemFunction","dataType","isOriginalSize","borderThickness","borderType","borderColor","borderWeight","editable"};
	
	private static String[] ItemPropertyGraphic = { "id","band_y","width","height","x","y","x1","y1","x2","y2","startPoint","endPoint","borderColor","borderWeight","points","lineThickness","systemFunction","dataType",
		"contentBackgroundAlphas","contentBackgroundColors","contentBackgroundAlpha","contentBackgroundColor","rotation","lineColor","editable"};

	private static String[] ItemPropertyInputItem = {"id","fontFamily","fontWeight","fontStyle","textDecoration","fontColor","fontSize","textAlign","verticalAlign","borderColor","lineHeight","borderType","text","label","systemFunction",
		"borderSide","checked","borderThickness","borderWeight","formatter","systemFunction","dataType", "band_y","borderTypes","borderWidths","borderColors","backgroundColor","borderColor"
		,"width","height","x","y","isCell","isTable","leftBorderType","rightBorderType","topBorderType","bottomBorderType","editable","fieldName","dataSet","column"
		,"useRequiredValue","scriptEvent","selectedText","deSelectedText","boxColor","dataProvider","groupName","value","symbolType","buttonGroup"};
	
	private static String[] ItemPropertyDEFAULT = {"id","fontFamily","fontColor","fontSize","textAlign","verticalAlign","borderColors","lineHeight","borderType","text","systemFunction",
		"borderSide","type","barcodeType","borderSide","checked","borderThickness","backgroundColor","borderWeight","formatter","systemFunction","dataType", "band_y"
		,"width","height","x","y","isCell","src","data","rotation","contentBackgroundColors", "contentBackgroundColor", "clipArtData","checked","prefix","suffix"
		,"leftBorderType","rightBorderType","topBorderType","bottomBorderType", "startPoint" ,"borderColor", "endPoint","showLabel","label","editable" ,"dataSet","column"
		,"useRequiredValue","scriptEvent","selectedText","deSelectedText","boxColor","dataProvider","groupName","value","symbolType","buttonGroup"};
	
	public static String[] getSimpleItemProperty( String _className )
	{
//		return  ItemPropertyDEFAULT;
		
		if( _className.toUpperCase().indexOf("CHART") != -1)
		{
			return ItemPropertyChart;
		}
		else if( _className.toUpperCase().indexOf("IMAGE") != -1 || _className.toUpperCase().indexOf("SIGN") != -1 || _className.toUpperCase().indexOf("CLIPART") != -1 )
		{
			return ItemPropertyImage;
		}
		else if( _className.toUpperCase().indexOf("BARCODE") != -1 || _className.toUpperCase().indexOf("QR") != -1 )
		{
			return ItemPropertyBarcode;
		}
		else if( _className.toUpperCase().indexOf("GRAPHICS") != -1 )
		{
			return ItemPropertyGraphic;
		}
		else if( _className.toUpperCase().indexOf("RADIO") != -1 ||  _className.toUpperCase().indexOf("CHECK") != -1 ||  _className.toUpperCase().indexOf("COMBO") != -1 ) 
		{
			return ItemPropertyInputItem;
		}
		
		return ItemPropertyDef;
	}
	
	
	
	
	public void setIsMarkAny(boolean isMarkAny)
	{
		mIsMarkAny = isMarkAny;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getItemName(String _itemName)
	{
		HashMap<String, Object> _item = new HashMap<String, Object>();
		
		if(!mIsMarkAny)
		{	
			if( mItemList.containsKey(_itemName) )
			{
				_item = (HashMap<String, Object>) mItemList.get(_itemName).clone();
				return _item;
			}
		}
		else
		{
			if( mItemListForMarkany.containsKey(_itemName) )
			{
				_item = (HashMap<String, Object>) mItemListForMarkany.get(_itemName).clone();
				return _item;
			}
		}

		return null;
	}
	
	
	///// Label /////
	
	@SuppressWarnings("serial")
	private ArrayList<String> mBorderSide = new ArrayList<String>(){
		{
			add("left");
			add("right");
			add("top");
			add("bottom");
		}
	};
	@SuppressWarnings("serial")
	private ArrayList<String> mBorderTypes = new ArrayList<String>(){
		{
			add("solid");
			add("solid");
			add("solid");
			add("solid");
		}
	};
	@SuppressWarnings("serial")
	private ArrayList<Integer> mBorderColorsInt = new ArrayList<Integer>(){
		{
			add(0);
			add(0);
			add(0);
			add(0);
		}
	};
	@SuppressWarnings("serial")
	private ArrayList<String> mBorderColors = new ArrayList<String>(){
		{
			add("#000000");
			add("#000000");
			add("#000000");
			add("#000000");
		}
	};
	@SuppressWarnings("serial")
	private ArrayList<Integer> mBorderWidths = new ArrayList<Integer>(){
		{
			add(1);
			add(1);
			add(1);
			add(1);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mBorderLabel = new HashMap<String, Object>(){
		{
			//put("type", "specialFontLabel");
			put("type", "borderLabel");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("alpha", 1);
			put("rotate", 0);
			put("visible", true);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", 1);
			put("text", "");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("lineHeight", 1.16);
			put("textDecoration", "normal");
			put("textAlign", "left");
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("isViewer", true);
			put("verticalAlign", "top");
			put("isCell", false);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("borderOriginalTypes", mBorderTypes);
			put("padding", 3);
			put("editable", false);
			put("id", "");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("unMasked", "");
			put("useRequiredValue", "");
			put("validateType", "");
			put("validateScript", "");
			put("prompt", "");
			put("ubHyperLinkType", "");
			put("ubHyperLinkUrl", "");
			put("ubHyperLinkText", "");
			put("multiLine", false);
			put("inputType", "text");
			put("editableBackgroundColor", "#ffff00");
			put("editableBackgroundVisible", false);
			put("viewerOnlyBackgroundColor", "#ffff00");
			put("viewerOnlyBackgroundVisible", false);
			put("scriptEvent", "");
			put("dataLimitRows", false);
			put("distributed", false);
		}
	};
	
	private HashMap<String, Object> mBorderLabelForMarkany = new HashMap<String, Object>(){
		{
			put("type", "borderLabel");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("text", "");
//			put("fontColorInt", 0);
//			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("lineHeight", 1.16);
			put("textDecoration", "normal");
			put("textAlign", "left");
//			put("borderColorInt", 0);
//			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("verticalAlign", "top");
			put("isViewer", true);
			put("borderTypes", mBorderTypes);
//			put("borderColorsInt", mBorderColorsInt);
//			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("borderOriginalTypes", mBorderTypes);
			put("isCell", false);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mStretchLabel = new HashMap<String, Object>(){
		{
			put("type", "borderLabel");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("alpha", 1);
			put("rotate", 0);
			put("visible", true);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", 1);
			put("text", "");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("lineHeight", 1.16);
			put("textDecoration", "normal");
			put("textAlign", "left");
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("isViewer", true);
			put("verticalAlign", "top");
			put("isCell", false);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("borderOriginalTypes", mBorderTypes);
			put("padding", 3);
			put("editable", false);
			put("id", "");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("unMasked", "");
			put("useRequiredValue", "");
			put("validateType", "");
			put("validateScript", "");
			put("prompt", "");
			put("ubHyperLinkType", "");
			put("ubHyperLinkUrl", "");
			put("ubHyperLinkText", "");
			put("multiLine", false);
			put("inputType", "text");
			put("editableBackgroundColor", "#ffff00");
			put("editableBackgroundVisible", false);
			put("viewerOnlyBackgroundColor", "#ffff00");
			put("viewerOnlyBackgroundVisible", false);
			put("scriptEvent", ""); 
			put("dataLimitRows", false);
		}
	};
		
	@SuppressWarnings("serial")
	private HashMap<String, Object> mRotateLabel = new HashMap<String, Object>(){
		{
			put("type", "rotateLabel");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("alpha", 1);
			put("visible", true);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", 1);
			put("text", "0");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("lineHeight", 1.16);
			put("textDecoration", "normal");
			put("textAlign", "left");
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("isViewer", true);
			put("rotate", 0);
			put("textRotate", 0);
			put("verticalAlign", "top");
			put("isCell", false);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("padding", 3);
			put("dataSet", "");
			put("column", "");

		}
	};
	
	///// Label /////

	@SuppressWarnings("serial")
	private HashMap<String, Object> mLine = new HashMap<String, Object>(){
		{
			put("type", "customLine");
			put("x", 0);
			put("y", 0);
			put("x1", 0);
			put("y1", 0);
			put("x2", 0);
			put("y2", 0);
			put("lineColorInt", 0);
			put("lineColor", "#000000");
			put("thickness", 1);
			put("visible", true);
			put("isViewer", true);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mConnectLine = new HashMap<String, Object>(){
		{
			put("type", "connectLine");
			put("x", 0);
			put("y", 0);
			put("cx1", 0);
			put("cy1", 0);
			put("cx2", 0);
			put("cy2", 0);
			put("cx3", 0);
			put("cy3", 0);
			put("cx4", 0);
			put("cy4", 0);
			put("cx5", 0);
			put("cy5", 0);
			put("cx6", 0);
			put("cy6", 0);
			put("startButtonType", "circle");
			put("endButtonType", "circle");
			put("startPosition", "TOP_LEFT");
			put("endPosition", "BOTTOM_RIGHT");
			put("lineColorInt", 0);
			put("lineColor", "#000000");
			put("startButtonColor", "#000000");
			put("endButtonColor", "#000000");
			put("thickness", 1);
			put("visible", true);
			put("isViewer", true);
		}
	};
	
	
	///// Chart /////
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mAreaChart = new HashMap<String, Object>(){
		{
			put("type", "areaChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", new ArrayList<String>());
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=area");
			put("isViewer", true);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mLineChart = new HashMap<String, Object>(){
		{
			put("type", "lineChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", new ArrayList<String>());
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=line");
			put("isViewer", true);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mPieChart = new HashMap<String, Object>(){
		{
			put("type", "pieChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", "0");
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=pie");
			put("isViewer", true);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mBarChart = new HashMap<String, Object>(){
		{
			put("type", "barChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", new ArrayList<String>());
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=bar");
			put("isViewer", true);
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mColumnChart = new HashMap<String, Object>(){
		{
			put("type", "columnChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", new ArrayList<String>());
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=column");
			put("isViewer", true);
		}
	};
	
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mCombinedColumnChart = new HashMap<String, Object>(){
		{
			put("type", "combinedColumnChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", new ArrayList<String>());
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=combcolumn");
			put("isViewer", true);
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mBubbleChart = new HashMap<String, Object>(){
		{
			put("type", "bubbleChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", "0");
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=bubble");
			put("isViewer", true);
		}
	};
	
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mCandleStickChart = new HashMap<String, Object>(){
		{
			put("type", "candleChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", "0");
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=candle");
			put("isViewer", true);
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mPlotChart = new HashMap<String, Object>(){
		{
			put("type", "candleChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", "0");
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=plot");
			put("isViewer", true);
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mRadarChart = new HashMap<String, Object>(){
		{
			put("type", "radarChartCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", "0");
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=radar");
			put("isViewer", true);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mTaximeter = new HashMap<String, Object>(){
		{
			put("type", "TaximeterCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("seriesXField", "0");
			put("seriesYField", "0");
			put("fillColorsInt", new ArrayList<Integer>());
			put("fillColors", new ArrayList<String>());
			put("data", "[{\"column_0\":\"Data0\"}]");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getChartImage&IMG_TYPE=taximeter");
			put("isViewer", true);
		}
	};
	
	///// Chart /////

	
	///// BarCode /////
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mBarcode = new HashMap<String, Object>(){
		{
			put("type", "barCodeCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("text", "0");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getBarcodeImage&IMG_TYPE=barcode");
			put("barcodeType", "Code128B");
			put("showLabel", true);
			put("isViewer", true);
			put("dataSet", "");
			put("column", "");
			put("widthMatch", true);
		}
	};
			
	@SuppressWarnings("serial")
	private HashMap<String, Object> mQRcode = new HashMap<String, Object>(){
		{
			put("type", "qrcodeSvgCtl");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("text", "0");
			put("src", Log.serverURL + "?domain=default&FILE_TYPE=exec&CALL=VIEWER5&METHOD_NAME=getBarcodeImage&IMG_TYPE=qrcode");
			put("isViewer", true);
			put("dataSet", "");
			put("column", "");
		}
	};
	
	
	///// BarCode /////
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mCheckBox = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "checkBox");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("text", "0");
			put("label", "0");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("textDecoration", "normal");
			put("selected", true);
			put("isViewer", true);
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", 1);
			put("boxColor", "#000000");
			put("fieldName", "");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("useRequiredValue", "");
			put("scriptEvent", "");
			put("editable", true);
			put("selectedText", "true");
			put("deSelectedText", "false");
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mImage = new HashMap<String, Object>(){
		{
			put("type", "uImage");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("alpha", 1);
			put("visible", true);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("src", "0");
			put("scaleX", 1);
			put("scaleY", 1);
			put("opacity", 1);
			put("angle", 0);
			put("text", "");
			put("isViewer", true);
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("ubHyperLinkUrl", "");
			put("tooltip", "");
			put("fileDownloadUrl", "");
			put("dataSet", "");
			put("column", "");
			put("isOriginalSize", false);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mClipArt = new HashMap<String, Object>(){
		{
			put("type", "uClipArt");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("alpha", 1);
			put("visible", true);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("clipArtData", "0");
			put("scaleX", 1);
			put("scaleY", 1);
			put("opacity", 1);
			put("angle", 0);
			put("isViewer", true);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mGraphicsRectangle = new HashMap<String, Object>(){
		{
			put("type" , "graphicsRectangle" );
			put("x", 0);
			put("y", 0);
			put("padding" , 0 );
			put("width" , 100 );
			put("height" , 100 );
			put("borderColor" , "#000000" );
			put("angle" , "0" );
			put("stroke" , "#000000" );
			put("strokeWidth" , 1 );
			put("opacity" , 1 );
			put("borderAlpha" , 1 );			
			put("alpha" , 1 );
			put("contentBackgroundAlpha" , 1);
			put("contentBackgroundColor" , "#000000" );
			put("borderColor" , "#000000" );
			put("borderThickness" , 1);
			put("rx" , 0);
			put("ry" , 0);
			put("isBackground" , false);
			put("rotation" , 0);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mGraphicsCircle = new HashMap<String, Object>(){
		{
			put("type" , "graphicsCircle" );
			put("x", 0);
			put("y", 0);
			put("padding" , 0 );
			put("width" , 100 );
			put("height" , 100 );
			put("borderColor" , "#000000" );
			put("angle" , "0" );
			put("stroke" , "#000000" );
			put("strokeWidth" , 1 );
			put("opacity" , 1 );
			put("alpha" , 1 );
			put("borderAlpha" , 1 );
			put("contentBackgroundAlpha" , 1);
			put("contentBackgroundColor" , "#000000" );
			put("borderColor" , "#000000" );
			put("borderThickness" , 1);
			put("conerRadius" , 0);
			put("rotation" , 0);
			put("isBackground" , false);
			put("radius" , 0);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mGradiantRectangle = new HashMap<String, Object>(){
		{
			put("type" , "gradiantRectangle" );
			put("x", 0);
			put("y", 0);
			put("padding" , 0 );
			put("width" , 100 );
			put("height" , 100 );
			put("borderColor" , "#000000" );
			put("angle" , "0" );
			put("stroke" , "#000000" );
			put("strokeWidth" , 1 );
			put("opacity" , 1 );
			put("alpha" , 1 );
			put("borderAlpha" , 1 );
			put("contentBackgroundAlphas" , new ArrayList<String>());
			put("contentBackgroundColors" , new ArrayList<String>());
			put("contentBackgroundAlpha" , 1);
			put("borderColor" , "#000000" );
			put("borderThickness" , 1);
			put("rx" , 0);
			put("ry" , 0);
			put("isBackground" , false);
			put("rotation" , 0);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mPresentaionGraphic = new HashMap<String, Object>(){
		{
			put("type", "presentationLine");
			put("x", 0);
			put("y", 0);
			put("width" , 100 );
			put("height" , 100 );
			put("path", new ArrayList<String>() );
			put("lineColorInt", 0);
			put("lineColor", "#000000");
			put("lineBorder", 1);
			put("lineAlpha" , 1 );
			put("visible", true);
			put("isViewer", true);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mTable = new HashMap<String, Object>(){
		{
			put("x",0);
			put("y",0);
			put("alpha", 1);
			put("width",100);
			put("height",100);
			put("rows", new ArrayList<ArrayList<HashMap<String, Object>>>());
			put("cells", new ArrayList<HashMap<String, Object>>());
			put("columnCount", 1);
			put("rowCount", 1);
			put("widthInfo" , new ArrayList<Float>());
			put("heightInfo" , new ArrayList<Float>());
			put("visible", true);
			put("isViewer", true);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mSVGArea = new HashMap<String, Object>(){
		{
			put("type", "svgTagCtl");
			put("x",0);
			put("y",0);
			put("alpha", 1);
			put("width",100);
			put("height",100);
			put("visible", true);
			put("isViewer", true);
			put("src", "");
			put("text", "");
			put("dataSet", "");
			put("column", "");
			put("fixedToSize", false);
			put("preserveAspectRatio", false);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mSVGRichText = new HashMap<String, Object>(){
		{
			put("type", "svgRichTextCtl");
			put("x",0);
			put("y",0);
			put("alpha", 1);
			put("width",100);
			put("height",100);
			put("lineGap",10);
			put("visible", true);
			put("isViewer", true);
			put("src", "");
			put("text", "");
			put("fontColor", "#000000");
			put("fontSize", 12f);
			put("fontWeight", "normal");
			put("fontFamily", "돋움");
			put("wordWrap",true);
			put("dataSet", "");
			put("column", "");
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mSignature = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "uSignpad");
			put("x",0);
			put("y",0);
			put("alpha", 1);
			put("width",100);
			put("height",100);
			put("lineGap",10);
			put("visible", true);
			put("isViewer", true);
			put("src", "");
			put("text", "");
			put("fontColor", "#000000");
			put("fontSize", 12f);
			put("fontWeight", "normal");
			put("fontFamily", "돋움");
			put("contentBackgroundAlpha" , 0.3);
			put("contentBackgroundColor" , "#AFFFFA" );
			put("tooltip" , "Click here for sign" );
			put("wordWrap",true);
			put("fieldName", "");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("useRequiredValue", "");
			put("signComplateEvent", "");
			put("editable", "");
			put("signViewTickness", 1);
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mTextInput = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "textInput");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("alpha", 1);
			put("rotate", 0);
			put("visible", true);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", 1);
			put("text", "");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("lineHeight", 1.16);
			put("textDecoration", "normal");
			put("textAlign", "left");
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("isViewer", true);
			put("verticalAlign", "top");
			put("isCell", false);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("padding", 3);
			put("fieldName", "");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("unMasked", "");
			put("useRequiredValue", "");
			put("prompt", "");
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mRadioBorder = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "radioBorder");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("text", "Radio Button");
			put("label", "Radio Button");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("textDecoration", "normal");
			put("selected", false);
			put("isViewer", true);
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", 1);
			put("groupName", "");
			put("fieldName", "");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("value", "");
			put("useRequiredValue", "");
			put("symbolType", "radio");
			put("scriptEvent", "");
			put("editable", true);
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mRadioButtonGroup = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "radioButtonGroup");
			put("x", 0);
			put("y", 0);
			put("width", 10);
			put("height", 10);
			put("visible", true);
			put("isViewer", true);
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("selection", "");
			put("useRequiredValue", "");
			put("buttonGroup", "");
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mButton = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "button");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("text", "0");
			put("label", "0");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("textDecoration", "normal");
			put("selected", true);
			put("isViewer", true);
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", 1);
			put("boxColor", "#000000");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mComboBox = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "comboBox");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("text", "ComboBox");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("textDecoration", "normal");
			put("selected", true);
			put("isViewer", true);
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", 1);
			put("boxColor", "#000000");
			put("dataProvider", "[{\"column_0\":\"Data0\"}]");
			put("fieldName", "");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("useRequiredValue", "");
		}
	};
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mDateField = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "dateField");
			put("x", 0);
			put("y", 0);
			put("width", 100);
			put("height", 100);
			put("visible", true);
			put("text", "");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 10f);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("textDecoration", "normal");
			put("selected", true);
			put("isViewer", true);
			put("borderColorInt", 0);
			put("borderColor", "#000000");
			put("borderType", "solid");
			put("borderSide", mBorderSide);
			put("borderWidth", 1);
			put("borderTypes", mBorderTypes);
			put("borderColorsInt", mBorderColorsInt);
			put("borderColors", mBorderColors);
			put("borderWidths", mBorderWidths);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", 1);
			put("boxColor", "#000000");
			put("dataProvider", "[{\"column_0\":\"Data0\"}]");
			
			put("rotation", 100);
			put("alpha", 100);
			put("locked", true);
			put("selectedDate", "");
			put("selectableRange", ""); // 고정
			put("dateFormat", "yyyy-MM-dd"); // 고정
			put("fieldName", "");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("useRequiredValue", "");
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mPicture = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "uPicture");
			put("x",0);
			put("y",0);
			put("alpha", 1);
			put("width",100);
			put("height",100);
			put("lineGap",10);
			put("visible", true);
			put("isViewer", true);
			put("src", "");
			put("text", "");
			put("fontColor", "#000000");
			put("fontSize", 12f);
			put("fontWeight", "normal");
			put("fontFamily", "돋움");
			put("contentBackgroundAlpha" , 0.3);
			put("contentBackgroundColor" , "#AFFFFA" );
			put("tooltip" , "Click here for picture" );
			put("wordWrap",true);
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("useRequiredValue", "");
			put("useDraw", "");
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, Object> mTextSignature = new HashMap<String, Object>(){
		{
			put("id", "");
			put("type", "uTextSignpad");
			put("x",0);
			put("y",0);
			put("alpha", 1);
			put("width",100);
			put("height",100);
			put("lineGap",10);
			put("visible", true);
			put("isViewer", true);
			put("src", "");
			put("text", "");
			put("fontColor", "#000000");
			put("fontSize", 12f);
			put("fontWeight", "normal");
			put("fontFamily", "돋움");
			put("contentBackgroundAlpha" , 0.3);
			put("contentBackgroundColor" , "#AFFFFA" );
			put("tooltip" , "Click here for sign" );
			put("wordWrap",true);
			put("fieldName", "");
			put("dataSet", "");
			put("column", "");
			put("rowId","");
			put("useRequiredValue", "");
			put("signComplateEvent", "");
			put("editable", "");
			put("signViewTickness", 1);
		}
	};
	
//	@SuppressWarnings("serial")
//	private HashMap<String, Object> mRichTextLabel = new HashMap<String, Object>(){
//		{
//			put("type", "richTextLabel");
//			put("x", 0);
//			put("y", 0);
//			put("width", 100);
//			put("height", 100);
//			put("alpha", 1);
//			put("rotate", 0);
//			put("visible", true);
//			put("backgroundColorInt", 16777215);
//			put("backgroundColor", "#ffffff");
//			put("backgroundAlpha", 1);
//			put("text", "");
//			put("fontColorInt", 0);
//			put("fontColor", "#000000");
//			put("fontSize", 0);
//			put("fontWeight", "normal");
//			put("fontFamily", "");
//			put("fontStyle", "normal");
//			put("lineHeight", 1.16);
//			put("textDecoration", "normal");
//			put("textAlign", "left");
//			put("borderColorInt", 0);
//			put("borderColor", "#000000");
//			put("borderType", "solid");
//			put("borderSide", mBorderSide);
//			put("borderWidth", 1);
//			put("isViewer", true);
//			put("verticalAlign", "top");
//			put("isCell", false);
//			put("borderTypes", mBorderTypes);
//			put("borderColorsInt", mBorderColorsInt);
//			put("borderColors", mBorderColors);
//			put("borderWidths", mBorderWidths);
//			put("borderOriginalTypes", mBorderTypes);
//			put("padding", 3);
//			put("editable", false);
//			put("id", "");
//			put("dataSet", "");
//			put("column", "");
//			put("rowId","");
//			put("unMasked", "");
//			put("useRequiredValue", "");
//			put("validateType", "");
//			put("validateScript", "");
//			put("prompt", "");
//			put("ubHyperLinkType", "");
//			put("ubHyperLinkUrl", "");
//			put("ubHyperLinkText", "");
//			put("multiLine", false);
//			put("inputType", "text");
//			put("editableBackgroundColor", "#ffff00");
//			put("editableBackgroundVisible", false);
//			put("scriptEvent", "");	
//			put("textFlow", "");	
//			put("styles", new JSONObject());
//		}
//	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, HashMap<String, Object>> mItemList = new HashMap<String, HashMap<String, Object>>(){
		{
			put("UBLabelBand", mBorderLabel);
			put("UBLabel", mBorderLabel);
			put("UBLabelBorder", mBorderLabel);
			put("UBRichTextLabel", mBorderLabel);
			put("UBStretchLabel", mStretchLabel);
			put("UBRotateLabel", mRotateLabel);
			put("UBImage", mImage);
			put("UBClipArtContainer", mClipArt);
			
			put("UBTable" , mTable);
			
			put("UBQRCode", mQRcode);
			put("UBBarCode", mBarcode);
			put("UBBarCode2", mBarcode);
			
			put("UBAreaChart", mAreaChart);
			put("UBLineChart", mLineChart);
			put("UBPieChart", mPieChart);
			put("UBBarChart", mBarChart);
			put("UBColumnChart", mColumnChart);
			put("UBCombinedColumnChart", mCombinedColumnChart);		
			put("UBBubbleChart", mBubbleChart);	
			
			put("UBCandleStickChart", mCandleStickChart);
			put("UBPlotChart", mPlotChart);
			put("UBRadarChart", mRadarChart);
			
			put("UBTaximeter", mTaximeter);		

			put("UBGraphicsLine", mLine);
			put("UBConnectLine", mConnectLine);
			put("UBGraphicsRectangle", mGraphicsRectangle);
			put("UBGraphicsCircle", mGraphicsCircle);
			put("UBGraphicsGradiantRectangle", mGradiantRectangle);
			
			put("UBPresentaionGraphic", mPresentaionGraphic);

			put("UBSVGArea", mSVGArea);
			put("UBSVGRichText", mSVGRichText);
//			put("UBRichTextLabel", mRichTextLabel);
			
			/**
			 * Editable Controls for e-Form
			 */
			put("UBSignature", mSignature);
			put("UBTextInput", mTextInput);
			put("UBTextArea", mBorderLabel);
			put("UBRadioBorder", mRadioBorder);
			put("UBRadioButtonGroup", mRadioButtonGroup);
			put("UBCheckBox", mCheckBox);
			put("UBButton", mButton);
			put("UBComboBox", mComboBox);
			put("UBDateFiled", mDateField);
			put("UBPicture", mPicture);
			put("UBTextSignature", mTextSignature);
		}
	};
	
	
	@SuppressWarnings("serial")
	private HashMap<String, HashMap<String, Object>> mItemListForMarkany = new HashMap<String, HashMap<String, Object>>(){
		{
			put("UBLabelBand", mBorderLabelForMarkany);
			put("UBLabel", mBorderLabelForMarkany);
			put("UBLabelBorder", mBorderLabelForMarkany);
			put("UBRichTextLabel", mBorderLabelForMarkany);
			put("UBStretchLabel", mStretchLabel);
			put("UBRotateLabel", mRotateLabel);
			put("UBImage", mImage);
			put("UBClipArtContainer", mClipArt);
			
			put("UBTable" , mTable);
			
			put("UBQRCode", mQRcode);
			put("UBBarCode", mBarcode);
			put("UBBarCode2", mBarcode);
			
			put("UBAreaChart", mAreaChart);
			put("UBLineChart", mLineChart);
			put("UBPieChart", mPieChart);
			put("UBBarChart", mBarChart);
			put("UBColumnChart", mColumnChart);
			put("UBCombinedColumnChart", mCombinedColumnChart);		
			put("UBBubbleChart", mBubbleChart);	
			
			put("UBCandleStickChart", mCandleStickChart);
			put("UBPlotChart", mPlotChart);
			put("UBRadarChart", mRadarChart);
			
			put("UBTaximeter", mTaximeter);		

			put("UBGraphicsLine", mLine);
			put("UBConnectLine", mConnectLine);
			put("UBGraphicsRectangle", mGraphicsRectangle);
			put("UBGraphicsCircle", mGraphicsCircle);
			put("UBGraphicsGradiantRectangle", mGradiantRectangle);
			
			put("UBPresentaionGraphic", mPresentaionGraphic);

			put("UBSVGArea", mSVGArea);
			put("UBSVGRichText", mSVGRichText);
//			put("UBRichTextLabel", mRichTextLabel);
			
			/**
			 * Editable Controls for e-Form
			 */
			put("UBSignature", mSignature);
			put("UBTextInput", mTextInput);
			put("UBTextArea", mBorderLabelForMarkany);
			put("UBRadioBorder", mRadioBorder);
			put("UBRadioButtonGroup", mRadioButtonGroup);
			put("UBCheckBox", mCheckBox);
			put("UBButton", mButton);
			put("UBComboBox", mComboBox);
			put("UBDateFiled", mDateField);
			put("UBPicture", mPicture);
			put("UBTextSignature", mTextSignature);
		}
	};
}


