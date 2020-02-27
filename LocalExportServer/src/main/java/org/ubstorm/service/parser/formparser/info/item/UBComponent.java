package org.ubstorm.service.parser.formparser.info.item;

import java.util.ArrayList;
import java.util.HashMap;

public class UBComponent {
	
	protected static final String DEFAULT_PACKAGE = UBComponent.class.getPackage().getName();
	
	public static final String id = "";
	public static final String text = "";
	public static final float x = -1;
	public static final float y = -1;
	public static final float width = -1;
	public static final float height = -1;
	public static final String band = "";
	
	public static final boolean visible = true;
	public static final int backgroundColorInt = 16777215;
	public static final String backgroundColor = "#ffffff";
	public static final float backgroundAlpha = 1;
	
	public static final int fontColorInt = 0;
	public static final String fontColor = "#000000";
	public static final int fontSize = 12;
	public static final String fontWeight = "normal";
	public static final String fontFamily = "Arial";
	public static final String fontStyle = "normal";
	public static final float lineHeight = 1.16f;
	public static final String textDecoration = "normal";
	public static final String textAlign = "left";
	
	public static final float alpha = 1;
	
	
	public static final ArrayList<String> borderSide = new ArrayList<String>(){
		{
			add("left");
			add("right");
			add("top");
			add("bottom");
		}
	};
//	public static final int borderWidth = 1;
	public static final int borderThickness = 1;
	public static final boolean isViewer = true;
	public static final String verticalAlign = "top";
	public static final boolean isCell = false;
	
	public static final ArrayList<String> borderTypes = new ArrayList<String>(){
		{
			add("solid");
			add("solid");
			add("solid");
			add("solid");
		}
	};
	public static final ArrayList<Integer> borderColorsInt = new ArrayList<Integer>(){
		{
			add(0);
			add(0);
			add(0);
			add(0);
		}
	};
	public static final ArrayList<String> borderColors = new ArrayList<String>(){
		{
			add("#000000");
			add("#000000");
			add("#000000");
			add("#000000");
		}
	};
	public static final ArrayList<Integer> borderWidths = new ArrayList<Integer>(){
		{
			add(1);
			add(1);
			add(1);
			add(1);
		}
	};
	
	public static final HashMap<String, String> typeInfo = new HashMap<String, String>(){
		{
			put("UBLabel", "borderLabel");
			put("UBRotateLabel", "rotateLabel");
			put("UBGraphicsLine", "customLine");
			put("UBLabel", "connectLine");
			put("UBAreaChart", "areaChartCtl");

			put("UBLineChart", "lineChartCtl");
			put("UBPieChart", "pieChartCtl");
			put("UBBarChart", "barChartCtl");
			put("UBColumnChart", "columnChartCtl");

			
			put("UBCombinedColumnChart", "combinedColumnChartCtl");
			put("UBBubbleChart", "bubbleChartCtl");
			put("UBCandleStickChart", "candleChartCtl");
			put("UBPlotChart", "candleChartCtl");
			put("UBRadarChart", "radarChartCtl");
			put("UBTaximeter", "TaximeterCtl");

			put("UBTaximeter", "barCodeCtl");
			put("UBTaximeter", "qrcodeSvgCtl");
			put("UBTaximeter", "checkBox");
			
			put("UBLabelBand", "borderLabel");
			put("UBLabel", "borderLabel");
			put("UBLabelBorder", "borderLabel");
			put("UBRichTextLabel", "borderLabel");
			put("UBStretchLabel", "borderLabel");
			put("UBRotateLabel", "rotateLabel");
			put("UBImage", "uImage");
			put("UBClipArtContainer", "uClipArt");
			put("UBQRCode", "qrcodeSvgCtl");
			put("UBBarCode", "barCodeCtl");
			put("UBBarCode2", "barCodeCtl");
			
			put("UBAreaChart", "areaChartCtl");
			put("UBLineChart", "lineChartCtl");
			put("UBPieChart", "pieChartCtl");
			put("UBBarChart", "barChartCtl");
			put("UBColumnChart", "columnChartCtl");
			put("UBCombinedColumnChart", "combinedColumnChartCtl");		
			put("UBBubbleChart", "bubbleChartCtl");	
			
			put("UBCandleStickChart", "candleChartCtl");
			put("UBPlotChart", "candleChartCtl");
			put("UBRadarChart", "radarChartCtl");
			
			put("UBTaximeter", "TaximeterCtl");		

			put("UBGraphicsLine", "customLine");
			put("UBConnectLine", "connectLine");
			put("UBGraphicsRectangle", "graphicsRectangle");
			put("UBGraphicsCircle", "graphicsCircle");
			put("UBGraphicsGradiantRectangle", "gradiantRectangle");
			
			put("UBPresentaionGraphic", "presentationLine");

			put("UBSVGArea", "svgTagCtl");
			put("UBSVGRichText", "svgRichTextCtl");
			
			/**
			 * Editable Controls for e-Form
			 */
			put("UBSignature", "uSignpad");
			put("UBTextInput", "textInput");
			put("UBTextArea", "borderLabel");
			put("UBRadioBorder", "radioBorder");
			put("UBRadioButtonGroup", "radioButtonGroup");
			put("UBCheckBox", "checkBox");
			put("UBButton", "button");
			put("UBComboBox", "comboBox");
			put("UBDateFiled", "dateField");
			put("UBPicture", "uPicture");
			put("UBTextSignature", "uTextSignpad");
			
			
		}
	};
	
	public static HashMap<String, Object> mDefaultProp = new HashMap<String, Object>()
	{
		{
			put("id", "");
			put("text", "");
			put("x", -1);
			put("y", -1);
			put("width", -1);
			put("height", -1);
			put("band", "");
			put("visible", true);
			put("backgroundColorInt", 16777215);
			put("backgroundColor", "#ffffff");
			put("backgroundAlpha", "1");
			put("fontColorInt", 0);
			put("fontColor", "#000000");
			put("fontSize", 12);
			put("fontWeight", "normal");
			put("fontFamily", "Arial");
			put("fontStyle", "normal");
			put("lineHeight",  1.16f);
			put("textDecoration",  "none");
			put("textAlign", "left");
			
			put("alpha", 1);
			put("rotate", 1);
			put("markanyVisible", true);
			put("includeLayoutType", "none");
			put("borderColorInt", 0);
			put("borderColor", "0");
			put("colSpan", "1");
			put("rowSpan", "1");
			put("columnCount", "1");
			put("rowCount", "1");
			put("columnWidth", "10");
			put("rowHeight", "10");
			put("status", "NORMAL");
			put("ubfunction", "");
			
			put("borderSide", borderSide);

			//put("borderWidth", 1);
			put("borderThickness", 1);
			put("borderType", "solid");

			put("showLabel", "true");
			put("crc", "true");
			put("widthMatch", "true");
			put("barcodeType", "code128");
		
			put("padding", 3);
						
			put("border_x", 0);
			put("border_y", 0);
			put("border_width", 0);
			put("border_height", 0);
			put("direction", "crossDown");
		}
		
	};
	
	
	public static Object getProperties(HashMap<String, Object> _item,String _className, String _propName ) 
	{
		return getProperties(_item, _className, _propName, null);
	}
	
	public static HashMap<String, Class<UBComponent>> ClassMap = new HashMap<String, Class<UBComponent>>();
	
	public static Object getProperties(HashMap<String, Object> _item,String _className, String _propName, Object _nullValue ) 
	{
		
		Object _result = _item.get(_propName);
		
		if( _result == null )
		{
			
			_result = mDefaultProp.get(_propName);
			/**
			Class _cls = ClassMap.get(_className);
			Field _field;
			
			if( _cls == null )
			{
				try {
					_cls = Class.forName( DEFAULT_PACKAGE + "." + _className );
					ClassMap.put( _className,_cls );
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
			
			if( _cls != null )
			{
				try {
					_field = _cls.getField(_propName);
					_result = _field.get(null);
				}catch (NoSuchFieldException e) {
					//e.printStackTrace();
				} catch (SecurityException e) {
					//e.printStackTrace();
				} catch (IllegalArgumentException e) {
					//e.printStackTrace();
				} catch (IllegalAccessException e) {
					//e.printStackTrace();
				}
			}
			*/
		}
		
		if( _result == null )  return _nullValue;
		
		return _result;
	}
	
	
}
