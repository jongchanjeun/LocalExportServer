package org.ubstorm.service.parser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;

public class hwpComponent {
	
	private int _instId1 = 1000;
	private int _instId2 = 9999;
	private int _gIndex = 0;
	public HashMap mImgIdList;;
	public String replaceText = "";
	
	public String mProtected = "true";
	
	public void init()
	{
		mImgIdList = new HashMap();
		
//		String _protected = common.getPropertyValue("hwp.cellFixed");
//		if(_protected != "") mProtected = _protected;
	}
	
	
	public String RectangleMatching_handler( HashMap<String, Object> _item, int _zOrder )
	{
		StringBuilder _rectangleBuilder = new StringBuilder();
		
		String _id1 = ValueConverter.getString(_instId1) + ValueConverter.getString(_instId2);
		String _id2 = ValueConverter.getString(_instId2) + ValueConverter.getString(_instId1);
		
		float _x = transPixcel2Hwpunit(_item.get("x"));
		float _y = transPixcel2Hwpunit(_item.get("y"));
		float _w = transPixcel2Hwpunit(_item.get("width"));
		float _h = transPixcel2Hwpunit(_item.get("height"));
		
		float _backgroundAlpha = 0;
		float _alpha = 0;
		
//		_borderAlpha = _item['borderAlpha'];
		_backgroundAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		_alpha = ValueConverter.getFloat(_item.get("alpha"));
		
		ArrayList<Integer> _lineWs = (ArrayList<Integer>) _item.get("borderWidths"); 
		
		int _lineW = 0;
						
		float _rotate = 0;
		
		if( _item.containsKey("rotation") )
		{
			_rotate = ValueConverter.getFloat(_item.get("rotation"));
		}
		
		HashMap<String, Float> _mat;
		
		_mat = getRotateMatrix( _x, _y , _w, _h , _rotate );
		
//		_mat.a = '1.00000';
//		_mat.b = '0.00000';
//		_mat.c = '0.00000';
//		_mat.d = '1.00000';
		
		_rectangleBuilder.append("<RECTANGLE Ratio=\"0\" X0=\"0\" Y0=\"0\" X1=\"" + _w + "\" Y1=\"0\" X2=\"" + _w + "\" Y2=\"" + _h + "\" X3=\"0\" Y3=\"" + _h + "\">\n");
		_rectangleBuilder.append("<SHAPEOBJECT InstId=\"" + _id1 + "\" Lock=\"false\" NumberingType=\"Figure\" TextWrap=\"InFrontOfText\" ZOrder=\""+_zOrder+"\">\n");
		_rectangleBuilder.append("<SIZE Height=\"" + _mat.get("h") + "\" HeightRelTo=\"Absolute\" Protect=\"false\" Width=\"" + _mat.get("w") + "\" WidthRelTo=\"Absolute\"/>\n");
		_rectangleBuilder.append("<POSITION AffectLSpacing=\"false\" AllowOverlap=\"true\" FlowWithText=\"false\" HoldAnchorAndSO=\"false\" HorzAlign=\"Left\" HorzOffset=\"" + _mat.get("x") + "\" HorzRelTo=\"Paper\" TreatAsChar=\"false\" VertAlign=\"Top\" VertOffset=\"" + _mat.get("y") + "\" VertRelTo=\"Paper\"/>\n");
		_rectangleBuilder.append("<OUTSIDEMARGIN Bottom=\"0\" Left=\"0\" Right=\"0\" Top=\"0\"/>\n</SHAPEOBJECT>\n");
		_rectangleBuilder.append("<DRAWINGOBJECT>\n");
		_rectangleBuilder.append("<SHAPECOMPONENT GroupLevel=\"0\" HorzFlip=\"false\" InstID=\"" + _id2 + "\" OriHeight=\"" + _h + "\" OriWidth=\"" + _w + "\" VertFlip=\"false\" XPos=\"" + _mat.get("tx") + "\" YPos=\"" + _mat.get("ty") + "\">\n");
		_rectangleBuilder.append("<ROTATIONINFO Angle=\"" + _mat.get("rotate") + "\" CenterX=\"" + _mat.get("cx") + "\" CenterY=\"" + _mat.get("cy") + "\"/>\n");
		_rectangleBuilder.append("<RENDERINGINFO>\n");
		_rectangleBuilder.append("<TRANSMATRIX E1=\"1.00000\" E2=\"0.00000\" E3=\"" + _mat.get("tx") + "0\" E4=\"0.00000\" E5=\"1.00000\" E6=\"" + _mat.get("ty") + "\"/>\n");
		_rectangleBuilder.append("<SCAMATRIX E1=\"1.00000\" E2=\"0.00000\" E3=\"0.00000\" E4=\"0.00000\" E5=\"1.00000\" E6=\"0.00000\"/>\n");
		_rectangleBuilder.append("<ROTMATRIX E1=\"" + _mat.get("a") + "\" E2=\"" + _mat.get("c") + "\" E3=\"0.00000\" E4=\"" + _mat.get("b") + "\" E5=\"" + _mat.get("d") + "\" E6=\"0.00000\"/>\n");
		_rectangleBuilder.append("</RENDERINGINFO>\n");
		_rectangleBuilder.append("</SHAPECOMPONENT>\n");
		
		if( _alpha == 1f )
		{
			if( _backgroundAlpha == 1f)
			{
				_backgroundAlpha = 0f;
			}
			else
			{
				_backgroundAlpha = getGraphicAlpha( _backgroundAlpha );
			}
		}
		else
		{
			_backgroundAlpha = getGraphicAlpha( _alpha );	
		}
		
		if( _item.containsKey("borderSide") )
		{
			ArrayList<Integer> _borderColors = (ArrayList<Integer>) _item.get("borderColorsInt");
			int _borderColor = ValueConverter.getInteger(_item.get("borderColorInt"));
			ArrayList<String> a = new ArrayList<String>();
			ArrayList<String> _types = (ArrayList<String>) _item.get("borderTypes");
			
//			if( _borderColor == false )
//			{
//				_borderColor = 0;
//			}
			
			//				|| _item.className == "UBTextArea" || _item.className == "UBTextInput" 아래 IF문에 사용 되었었음.  명우
			if( _item.get("className").equals("UBLabelBorder") )
			{
//				_rectangleBuilder.append("<LINESHAPE Alpha=\"" + "0" + "\" EndCap=\"Flat\" Style=\"Solid\" Width=\"" + getBorderWidth(1) + "\" Color=\"" + ChangeColorToMSA( _borderColor ) + "\"/>\n");
				_rectangleBuilder.append("<LINESHAPE Alpha=\"" + "0" + "\" EndCap=\"Flat\" Style=\"Solid\" Width=\"" + getBorderWidthString("Solid",1) + "\" Color=\"" + ChangeColorToMSA( _borderColor ) + "\"/>\n");
			}
			else if( _item.get("className").equals("UBTextArea") )
			{
				
			}
			else
			{
				a = (ArrayList<String>) _item.get("borderSide");
				if( a.size() == 0 )
				{
					
				}
				else if( a.size() > 4 )
				{
					
				}
				else
				{
					boolean isNoneBoarder = false;
					for (int i = 0; i < a.size(); i++) 
					{
						String _side = _types.get(i);
						_lineW = _lineWs.get(i);
						if( _side == "none" || _side == "" )
						{
							isNoneBoarder = true;
						}
						else
						{
//							_rectangleBuilder.append("<LINESHAPE Alpha=\"" + "0" + "\" EndCap=\"Flat\" Style=\"" + getBorderType( _side ) + "\" Width=\"" + getBorderWidth(_lineW) + "\" Color=\"" +  ChangeColorToMSA( _borderColors.get(i) ) + "\"/>\n");
							_rectangleBuilder.append("<LINESHAPE Alpha=\"" + "0" + "\" EndCap=\"Flat\" Style=\"" + getBorderType( _side ) + "\" Width=\"" + getBorderWidthString(getBorderType( _side ),_lineW) + "\" Color=\"" +  ChangeColorToMSA( _borderColors.get(i) ) + "\"/>\n");
							isNoneBoarder = false;
							break;		
						}
					}
					if( isNoneBoarder ) _rectangleBuilder.append("<LINESHAPE Alpha=\"" + "0" + "\" EndCap=\"Flat\" Style=\"none\" Width=\"0\" Color=\"0\"/>\n");			// 선이 없을경우 빈 선을 Add처리 
				}
			}
		}
		
		if( _item.containsKey( "backgroundColorInt" ) )
		{
			int _backColor = ValueConverter.getInteger(_item.get("backgroundColorInt"));
			
//			if( _backColor == false )
//			{
//				_backColor = 16777215;
//			}
			
//			if( _backColor != 16777215 )
//			{
			_rectangleBuilder.append("<FILLBRUSH>\n<WINDOWBRUSH Alpha=\"" + _backgroundAlpha + "\" FaceColor=\"" + ChangeColorToMSA( _backColor ) + "\" HatchColor=\"0\"/>\n</FILLBRUSH>\n");
//			}
		}
		_rectangleBuilder.append("<DRAWTEXT LastWidth=\"" + _w + "\">\n");
//		float _padding = transPixcel2Hwpunit(3);
		float _padding = 283f;
		if( _item.containsKey("verticalAlign") )
		{
			String _vAlign;
			_vAlign = ValueConverter.getString( _item.get("verticalAlign") );
			
			if(_vAlign.toLowerCase().equals("top"))
			{
				_rectangleBuilder.append("<TEXTMARGIN Bottom=\"0\" Left=\"" + _padding + "\" Right=\"" + _padding + "\" Top=\"" + _padding + "\"/>\n");		
			}
			else if(_vAlign.toLowerCase().equals("bottom"))
			{
				_rectangleBuilder.append("<TEXTMARGIN Bottom=\"" + _padding + "\" Left=\"" + _padding + "\" Right=\"" + _padding + "\" Top=\"0\"/>\n");
			}
			else
			{
				_rectangleBuilder.append("<TEXTMARGIN Bottom=\"0\" Left=\"" + _padding + "\" Right=\"" + _padding + "\" Top=\"0\"/>\n");
			}
		}		
		else
		{
			_rectangleBuilder.append("<TEXTMARGIN Bottom=\"0\" Left=\"" + _padding + "\" Right=\"" + _padding + "\" Top=\"0\"/>\n");
		}
		_rectangleBuilder.append(ParaListMatching_handler( _item ));
		_rectangleBuilder.append("</DRAWTEXT>\n</DRAWINGOBJECT>\n</RECTANGLE>\n");
		
		
		return _rectangleBuilder.toString();
	}
	
	public String SVGRichTextLabelMatching_handler( HashMap<String, Object> _item )
	{
		
		// null 체크
		if( _item.get("childs") == null ) return "";
		
		StringBuilder _richTextBuilder = new StringBuilder();

		String _id1 = ValueConverter.getString(_instId1) + ValueConverter.getString(_instId2);
		String _id2 = ValueConverter.getString(_instId2) + ValueConverter.getString(_instId1);

		float _x = transPixcel2Hwpunit(_item.get("x"));
		float _y = transPixcel2Hwpunit(_item.get("y"));
		float _w = transPixcel2Hwpunit(_item.get("width"));
		float _h = transPixcel2Hwpunit(_item.get("height"));

		float _backgroundAlpha = 0;
		float _alpha = 0;

		//		_borderAlpha = _item['borderAlpha'];
//		_backgroundAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
//		_alpha = ValueConverter.getFloat(_item.get("alpha"));

//		ArrayList<Integer> _lineWs = (ArrayList<Integer>) _item.get("borderWidths"); 
		ArrayList<Integer> _lineWs = new ArrayList<Integer>(); 

		int _lineW = 0;

		float _rotate = 0;

		if( _item.containsKey("rotation") )
		{
			_rotate = ValueConverter.getFloat(_item.get("rotation"));
		}

		HashMap<String, Float> _mat;

		_mat = getRotateMatrix( _x, _y , _w, _h , _rotate );


		_richTextBuilder.append("<RECTANGLE Ratio=\"0\" X0=\"0\" Y0=\"0\" X1=\"" + _w + "\" Y1=\"0\" X2=\"" + _w + "\" Y2=\"" + _h + "\" X3=\"0\" Y3=\"" + _h + "\">\n");
		_richTextBuilder.append("<SHAPEOBJECT InstId=\"" + _id1 + "\" Lock=\"false\" NumberingType=\"Figure\" TextWrap=\"InFrontOfText\" ZOrder=\"0\">\n");
		_richTextBuilder.append("<SIZE Height=\"" + _mat.get("h") + "\" HeightRelTo=\"Absolute\" Protect=\"false\" Width=\"" + _mat.get("w") + "\" WidthRelTo=\"Absolute\"/>\n");
		_richTextBuilder.append("<POSITION AffectLSpacing=\"false\" AllowOverlap=\"true\" FlowWithText=\"false\" HoldAnchorAndSO=\"false\" HorzAlign=\"Left\" HorzOffset=\"" + _mat.get("x") + "\" HorzRelTo=\"Paper\" TreatAsChar=\"false\" VertAlign=\"Top\" VertOffset=\"" + _mat.get("y") + "\" VertRelTo=\"Paper\"/>\n");
		_richTextBuilder.append("<OUTSIDEMARGIN Bottom=\"0\" Left=\"0\" Right=\"0\" Top=\"0\"/>\n</SHAPEOBJECT>\n");
		_richTextBuilder.append("<DRAWINGOBJECT>\n");
		_richTextBuilder.append("<SHAPECOMPONENT GroupLevel=\"0\" HorzFlip=\"false\" InstID=\"" + _id2 + "\" OriHeight=\"" + _h + "\" OriWidth=\"" + _w + "\" VertFlip=\"false\" XPos=\"" + _mat.get("tx") + "\" YPos=\"" + _mat.get("ty") + "\">\n");
		_richTextBuilder.append("<ROTATIONINFO Angle=\"" + _mat.get("rotate") + "\" CenterX=\"" + _mat.get("cx") + "\" CenterY=\"" + _mat.get("cy") + "\"/>\n");
		_richTextBuilder.append("<RENDERINGINFO>\n");
		_richTextBuilder.append("<TRANSMATRIX E1=\"1.00000\" E2=\"0.00000\" E3=\"" + _mat.get("tx") + ".00000\" E4=\"0.00000\" E5=\"1.00000\" E6=\"" + _mat.get("ty") + ".00000\"/>\n");
		_richTextBuilder.append("<SCAMATRIX E1=\"1.00000\" E2=\"0.00000\" E3=\"0.00000\" E4=\"0.00000\" E5=\"1.00000\" E6=\"0.00000\"/>\n");
		_richTextBuilder.append("<ROTMATRIX E1=\"" + _mat.get("a") + "\" E2=\"" + _mat.get("c") + "\" E3=\"0.00000\" E4=\"" + _mat.get("b") + "\" E5=\"" + _mat.get("d") + "\" E6=\"0.00000\"/>\n");
		_richTextBuilder.append("</RENDERINGINFO>\n");
		_richTextBuilder.append("</SHAPECOMPONENT>\n");

		if( _alpha == 1f )
		{
			if( _backgroundAlpha == 1f)
			{
				_backgroundAlpha = 0f;
			}
			else
			{
				_backgroundAlpha = getGraphicAlpha( _backgroundAlpha );
			}
		}
		else
		{
			_backgroundAlpha = getGraphicAlpha( _alpha );	
		}

		if( _item.containsKey("borderSide") )
		{
			ArrayList<Integer> _borderColors = (ArrayList<Integer>) _item.get("borderColorsInt");
			int _borderColor = ValueConverter.getInteger(_item.get("borderColorInt"));
			ArrayList<String> a = new ArrayList<String>();
			ArrayList<String> _types = (ArrayList<String>) _item.get("borderTypes");

			//		if( _borderColor == false )
			//		{
			//			_borderColor = 0;
			//		}

			//				|| _item.className == "UBTextArea" || _item.className == "UBTextInput" 아래 IF문에 사용 되었었음.  명우
			if( _item.get("className").equals("UBLabelBorder") )
			{
//				_richTextBuilder.append("<LINESHAPE Alpha=\"" + "0" + "\" EndCap=\"Flat\" Style=\"Solid\" Width=\"" + getBorderWidth(1) + "\" Color=\"" + ChangeColorToMSA( _borderColor ) + "\"/>\n");
				_richTextBuilder.append("<LINESHAPE Alpha=\"" + "0" + "\" EndCap=\"Flat\" Style=\"Solid\" Width=\"" + getBorderWidthString("Solid",1) + "\" Color=\"" + ChangeColorToMSA( _borderColor ) + "\"/>\n");
			}
			else if( _item.get("className").equals("UBTextArea") )
			{

			}
			else
			{
				a = (ArrayList<String>) _item.get("borderSide");
				if( a.size() == 0 )
				{

				}
				else if( a.size() > 4 )
				{

				}
				else
				{
					for (int i = 0; i < a.size(); i++) 
					{
						String _side = _types.get(i);
						_lineW = _lineWs.get(i);
						if( _side == "none" || _side == "" )
						{

						}
						else
						{
//							_richTextBuilder.append("<LINESHAPE Alpha=\"" + "0" + "\" EndCap=\"Flat\" Style=\"" + getBorderType( _side ) + "\" Width=\"" + getBorderWidth(_lineW) + "\" Color=\"" +  ChangeColorToMSA( _borderColors.get(i) ) + "\"/>\n");
							_richTextBuilder.append("<LINESHAPE Alpha=\"" + "0" + "\" EndCap=\"Flat\" Style=\"" + getBorderType( _side ) + "\" Width=\"" + getBorderWidthString( getBorderType( _side ),_lineW) + "\" Color=\"" +  ChangeColorToMSA( _borderColors.get(i) ) + "\"/>\n");
							break;		
						}
					}
				}
			}
		}

		if( _item.containsKey( "backgroundColorInt" ) )
		{
			int _backColor = ValueConverter.getInteger(_item.get("backgroundColorInt"));

			//		if( _backColor == false )
			//		{
			//			_backColor = 16777215;
			//		}

			//		if( _backColor != 16777215 )
			//		{
			_richTextBuilder.append("<FILLBRUSH>\n<WINDOWBRUSH Alpha=\"" + _backgroundAlpha + "\" FaceColor=\"" + ChangeColorToMSA( _backColor ) + "\" HatchColor=\"0\"/>\n</FILLBRUSH>\n");
			//		}
		}

		_richTextBuilder.append("<DRAWTEXT LastWidth=\"" + _w + "\">\n");
		String _vAlign = "top"; 
//		if( _item.containsKey("verticalAlign") )
//		{
//			_vAlign = ValueConverter.getString( _item.get("verticalAlign") );
//
//			if(_vAlign.toLowerCase().equals("top"))
//			{
//				_richTextBuilder.append("<TEXTMARGIN Bottom=\"0\" Left=\"224\" Right=\"224\" Top=\"224\"/>\n");		
//			}
//			else if(_vAlign.toLowerCase().equals("bottom"))
//			{
//				_richTextBuilder.append("<TEXTMARGIN Bottom=\"224\" Left=\"224\" Right=\"224\" Top=\"0\"/>\n");
//			}
//			else
//			{
//				_richTextBuilder.append("<TEXTMARGIN Bottom=\"0\" Left=\"224\" Right=\"224\" Top=\"0\"/>\n");
//			}
//		}		
//		else
//		{
//		_richTextBuilder.append("<TEXTMARGIN Bottom=\"224\" Left=\"224\" Right=\"224\" Top=\"224\"/>\n");
		// top margin이 정상적으로 넘어 올경우 1px 당 31.45hwpunit; y 값이 18==> 566.1
			_richTextBuilder.append("<TEXTMARGIN Bottom=\"0\" Left=\"0\" Right=\"0\" Top=\"566.1\"/>\n");
//		}
		_richTextBuilder.append("<PARALIST LineWrap=\"Break\" LinkListID=\"0\" LinkListIDNext=\"0\" TextDirection=\"0\" VertAlign=\"" + getVerticalAlign( _vAlign ) + "\">\n");

		ArrayList<ArrayList<HashMap<String, Object>>> _childs = new ArrayList<ArrayList<HashMap<String,Object>>>();

		_childs = (ArrayList<ArrayList<HashMap<String,Object>>>) _item.get("childs");

		for ( ArrayList<HashMap<String, Object>> _ar : _childs)
		{
			if( _ar.size() != 0)
			{
				_richTextBuilder.append("<P ParaShape=\"" + _item.get("pID") + "\">\n");
				_richTextBuilder.append(SVGRichTextMatching_handler( _ar ));
				_richTextBuilder.append("</P>\n");

			}
		}
		
		_richTextBuilder.append("</PARALIST>\n</DRAWTEXT>\n</DRAWINGOBJECT>\n</RECTANGLE>\n");

		return _richTextBuilder.toString();
	}

	private String SVGRichTextMatching_handler( ArrayList<HashMap<String,Object>> _item )
	{
		StringBuilder _rTextBuilder = new StringBuilder();

		
		

		for (HashMap<String, Object> _childItem : _item) 
		{
			_rTextBuilder.append("<TEXT CharShape=\"" + _childItem.get("sID") + "\">\n");
			_rTextBuilder.append("<CHAR>");

			if( _childItem.containsKey("text") )
			{
				String _txt = ValueConverter.getString(_childItem.get("text"));

				_txt = _txt.replaceAll("\n", "<LINEBREAK/>").replaceAll("<", "&lt;").replaceAll(">", "&gt;");

				_rTextBuilder.append(_txt);
			}

			_rTextBuilder.append("</CHAR>\n");
			_rTextBuilder.append("</TEXT>\n");
		}

		return _rTextBuilder.toString();
	}
	
	
	private String ParaListMatching_handler( HashMap<String, Object> _item )
	{
		StringBuilder _pTagBuilder = new StringBuilder();
		
		int _txRotate = 0;
		
		if( _item.containsKey("textRotate") )
		{
			_txRotate = ValueConverter.getInteger(_item.get("textRotate"));
			
			if( _txRotate != 0 )
			{
				_txRotate = 1;
			}
			
		}
		
		String _vAlign = "Center";
		
		if( _item.containsKey("verticalAlign") )
		{
			_vAlign = ValueConverter.getString( _item.get("verticalAlign") );
			
			_vAlign = getVerticalAlign(_vAlign); 
		}
		
		_pTagBuilder.append("<PARALIST LineWrap=\"Break\" LinkListID=\"0\" LinkListIDNext=\"0\" TextDirection=\"" + _txRotate + "\" VertAlign=\"" + _vAlign + "\">\n");
//		_pTagBuilder.append("<P ParaShape=\"" + AlignSettingToParaShape(ValueConverter.getString(_item.get("textAlign")))  + "\">\n<TEXT CharShape=\"" + _item.get("sID") + "\">\n");
		_pTagBuilder.append("<P ParaShape=\"" + _item.get("pID")  + "\">\n<TEXT CharShape=\"" + _item.get("sID") + "\">\n");
		_pTagBuilder.append("<CHAR>");
		
		if( _item.containsKey("text") )
		{
			String _txt = ValueConverter.getString(_item.get("text"));
			// 줄바꿈 처리
			_txt = _txt.replace("\\n", "\n").replaceAll("\\r", "\r");
			
			_txt = _txt.replaceAll("&", "&amp;");
			// < > 부호 변환 후 줄바꿈 변경.
			_txt = _txt.replaceAll("<", "&lt;").replace(">", "&gt;");

			_txt = _txt.replaceAll("\r\n", "<LINEBREAK/>");
			_txt = _txt.replaceAll("\n", "<LINEBREAK/>");
			_txt = _txt.replaceAll("\r", "");// 최명진 과장 2016 04 15
			
			_txt = _txt.replaceAll("\\p{Cntrl}", replaceText);	// Control 문자 제거
//			_txt = _txt.replaceAll("[^x00-x7F]", "");		// 특수문자 제거 ASCii
//			_txt = org.apache.commons.lang3.StringEscapeUtils.escapeXml10(_txt);  
			_pTagBuilder.append(_txt);
		}
		
		_pTagBuilder.append("</CHAR>\n");
		_pTagBuilder.append("</TEXT>\n</P>\n</PARALIST>\n");
		
		return _pTagBuilder.toString();
	}
	
	public String GraphicMatching_handler( HashMap<String, Object> _item, int _zOrder )
	{
		StringBuilder _graphicBuilder = new StringBuilder();
		
		_graphicBuilder.append(GraphicTypeTxtSetting( _item , "h"));
		String _id1 = String.valueOf(_instId1) + String.valueOf(_instId2);
		String _id2 = String.valueOf(_instId2) + String.valueOf(_instId1);
		
		float _x = transPixcel2Hwpunit(_item.get("x"));
		float _y = transPixcel2Hwpunit(_item.get("y"));
		float _w = 0;
		float _h = 0;
		
		float _lineW = 0;
		String _lineC = "";

		String _bColor1 = "";
		String _bColor2 = "";
		
		int _rotate = 0;

		_rotate = (int) Math.floor( ValueConverter.getFloat(_item.get("rotation") == null ? 0 : _item.get("rotation")) );
		
		
		HashMap<String, Float> _mat;
		
//		_mat = getRotateMatrix( _x, _y , _w, _h , _rotate );
		
		float _borderAlpha = 0;
		float _backgroundAlpha = 0;
		float _alpha = 0;
		
		String _className = ValueConverter.getString(_item.get("className"));
		
		if( _className.equals("UBGraphicsCircle") )
		{
			// 원;
//			_borderAlpha = _item.get("borderAlpha");
//			_alpha = _item.get("alpha");
			_borderAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));
			_backgroundAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
			_alpha = ValueConverter.getFloat(_item.get("alpha"));
			
			_w = transPixcel2Hwpunit(_item.get("width"));
			_h = transPixcel2Hwpunit(_item.get("height"));
			
		}
		else if( _className.equals("UBGraphicsLine") )
		{
			// 선;
			_borderAlpha = 1;
			_backgroundAlpha = 1;
			_alpha = 1;
			
			float[] _point = linePoint(_item);
			
			_w = transPixcel2Hwpunit(Math.abs(_point[0] - _point[2]) );
			
			if( _w < 1 )
			{
				_w = transPixcel2Hwpunit( _item.get("thickness") );
			}
			
			
			_h = transPixcel2Hwpunit(Math.abs(_point[1] - _point[3]) );
			
			if( _h < 1 )
			{
				_h = transPixcel2Hwpunit( _item.get("thickness") );
			}
			
		}
		else if( _className.equals("UBGraphicsGradiantRectangle") )
		{
			// 그라디언트사각형;
//			_borderAlpha = _item['borderAlpha'];
//			_backgroundAlpha = _item['contentBackgroundAlphas'][0];
//			_alpha = _item['alpha'];
			ArrayList<String> _cbAlphas = null;
			if(_item.containsKey("contentBackgroundAlpha")){
				_backgroundAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
			}else{
				if(_item.containsKey("contentBackgroundAlphas")){
					_cbAlphas = (ArrayList<String>) _item.get("contentBackgroundAlphas");					
					if(_cbAlphas.size()>1){
						_backgroundAlpha = ValueConverter.getFloat(_cbAlphas.get(0));
					}
				}
			}			
			_borderAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));			
			_alpha = ValueConverter.getFloat(_item.get("alpha"));
			
			_w = transPixcel2Hwpunit(_item.get("width"));
			_h = transPixcel2Hwpunit(_item.get("height"));
		}
		else
		{
			// 사각형;_borderAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));
			_borderAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));
			_backgroundAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
			_alpha = ValueConverter.getFloat(_item.get("alpha"));
			
			_w = transPixcel2Hwpunit(_item.get("width"));
			_h = transPixcel2Hwpunit(_item.get("height"));
		}
			
		if( _alpha == 1 )
		{
			if( _borderAlpha == 1)
			{
				_borderAlpha = 0;
			}
			else
			{
				_borderAlpha = getGraphicAlpha( _borderAlpha );
			}
			
			if( _backgroundAlpha == 1)
			{
				_backgroundAlpha = 0;
			}
			else
			{
				_backgroundAlpha = getGraphicAlpha( _backgroundAlpha );
			}
		}
		else
		{
			_borderAlpha = _backgroundAlpha = getGraphicAlpha( _alpha );	
		}
		
		_mat = getRotateMatrix( _x, _y, _w, _h , _rotate );
		
		// rect 에 isBackground 가 true 일 경우 behindText 를 넣음
		
		String _txtWrap = "";
		
		if( _item.containsKey("isBackground") && ValueConverter.getBoolean(_item.get("isBackground")) )
		{
			_txtWrap = "BehindText";		// 글자 뒤.
		}
		else
		{
			_txtWrap = "InFrontOfText";		// 글자 앞.
		}
		
		
//		_graphicBuilder.append("<SHAPEOBJECT InstId=\"" + _id1 + "\" Lock=\"false\" NumberingType=\"Figure\" TextWrap=\"InFrontOfText\" ZOrder=\"" + _gIndex + "\">\n");
		_graphicBuilder.append("<SHAPEOBJECT InstId=\"" + _id1 + "\" Lock=\"false\" NumberingType=\"Figure\" TextWrap=\"" + _txtWrap + "\" ZOrder=\"" + _zOrder + "\">\n");
		_graphicBuilder.append("<SIZE Height=\"" + _mat.get("h") + "\" HeightRelTo=\"Absolute\" Protect=\"false\" Width=\"" + _mat.get("w") + "\" WidthRelTo=\"Absolute\"/>\n");
		_graphicBuilder.append("<POSITION AffectLSpacing=\"false\" AllowOverlap=\"true\" FlowWithText=\"false\" HoldAnchorAndSO=\"false\" HorzAlign=\"Left\" HorzOffset=\"" + _mat.get("x") + "\" HorzRelTo=\"Paper\" TreatAsChar=\"false\" VertAlign=\"Top\" VertOffset=\"" + _mat.get("y") + "\" VertRelTo=\"Paper\"/>\n");
		_graphicBuilder.append("<OUTSIDEMARGIN Bottom=\"0\" Left=\"0\" Right=\"0\" Top=\"0\"/>\n");
		_graphicBuilder.append("</SHAPEOBJECT>\n");
		_graphicBuilder.append("<DRAWINGOBJECT>\n");
		_graphicBuilder.append("<SHAPECOMPONENT GroupLevel=\"0\" HorzFlip=\"false\" InstID=\"" + _id2 + "\" OriHeight=\"" + _h + "\" OriWidth=\"" + _w + "\" VertFlip=\"false\" XPos=\"" + _mat.get("tx") + "\" YPos=\"" + _mat.get("ty") + "\">\n");
		_graphicBuilder.append("<ROTATIONINFO Angle=\"" + _mat.get("rotate") + "\" CenterX=\"" + _mat.get("cx") + "\" CenterY=\"" + _mat.get("cy") + "\"/>\n");
		_graphicBuilder.append("<RENDERINGINFO>\n");
		_graphicBuilder.append("<TRANSMATRIX E1=\"1.00000\" E2=\"0.00000\" E3=\"" + _mat.get("tx") + "\" E4=\"0.00000\" E5=\"1.00000\" E6=\"" + _mat.get("ty") + "\"/>\n");
		_graphicBuilder.append("<SCAMATRIX E1=\"1.00000\" E2=\"0.00000\" E3=\"0.00000\" E4=\"0.00000\" E5=\"1.00000\" E6=\"0.00000\"/>\n");
		_graphicBuilder.append("<ROTMATRIX E1=\"" + _mat.get("a") + "\" E2=\"" + _mat.get("c") + "\" E3=\"0.00000\" E4=\"" + _mat.get("b") + "\" E5=\"" + _mat.get("d") + "\" E6=\"0.00000\"/>\n");
		_graphicBuilder.append("</RENDERINGINFO>\n");
		_graphicBuilder.append("</SHAPECOMPONENT>\n");
	
		
		if( _className.equals("UBGraphicsLine") )
		{
			_lineW = transPixcel2Hwpunit( _item.get("thickness") );
			_lineC = ChangeColorToMSA( ValueConverter.getInteger(_item.get("lineColorInt")) );
			_graphicBuilder.append("<LINESHAPE Alpha=\"" + _borderAlpha + "\" EndCap=\"Round\" Style=\"Solid\" Width=\"" + _lineW + "\" Color=\"" + _lineC + "\"/>\n");
//			_graphicBuilder.append("<SHADOW Alpha=\"0\" Color=\"11711154\" OffsetX=\"0\" OffsetY=\"0\" Type=\"0\"/>\n");
			_graphicBuilder.append("</DRAWINGOBJECT>\n");
		}
		else
		{
			_lineW = transPixcel2Hwpunit( _item.get("borderThickness") );
			_lineC = ChangeColorToMSA( ValueConverter.getInteger(_item.get("borderColorInt")) );
			_graphicBuilder.append("<LINESHAPE Alpha=\"" + _borderAlpha + "\" EndCap=\"Flat\" Style=\"Solid\" Width=\"" + _lineW + "\" Color=\"" + _lineC + "\"/>\n");
			_graphicBuilder.append("<FILLBRUSH>\n");
			if( _className.equals("UBGraphicsGradiantRectangle") )
			{
				ArrayList<String> _bColors = (ArrayList<String>) _item.get("contentBackgroundColorsInt");
				
				_bColor1 = ChangeColorToMSA( ValueConverter.getInteger(_bColors.get(0)) );
				_bColor2 = ChangeColorToMSA( ValueConverter.getInteger(_bColors.get(1)));
				
				_graphicBuilder.append("<GRADATION Alpha=\"" + _backgroundAlpha + "\" Angle=\"0\" CenterX=\"0\" CenterY=\"0\" ColorNum=\"2\" Step=\"255\" StepCenter=\"50\" Type=\"Linear\">\n");
				_graphicBuilder.append("<COLOR Value=\"" + _bColor1 + "\"/>\n");
				_graphicBuilder.append("<COLOR Value=\"" + _bColor2 + "\"/>\n");
				_graphicBuilder.append("</GRADATION>\n");
			}
			else
			{
				_bColor1 = ChangeColorToMSA( ValueConverter.getInteger(_item.get("contentBackgroundColorInt")) );
				
				_graphicBuilder.append("<WINDOWBRUSH Alpha=\"" + _backgroundAlpha + "\" FaceColor=\"" + _bColor1 +"\" HatchColor=\"0\"/>\n");
			}
			
			_graphicBuilder.append("</FILLBRUSH>\n");
//			_graphicBuilder.append("<SHADOW Alpha=\"0\" Color=\"11711154\" OffsetX=\"0\" OffsetY=\"0\" Type=\"0\"/>\n");
			_graphicBuilder.append("</DRAWINGOBJECT>\n");
		}
		_graphicBuilder.append(GraphicTypeTxtSetting( _item , "e"));
		_gIndex++;		
		_instId1++;
		_instId2++;
		return _graphicBuilder.toString();
	}
	
	private String GraphicTypeTxtSetting( HashMap<String, Object> _item , String _type )
	{
		StringBuilder _graphicHFBuilder = new StringBuilder();
		String _className = String.valueOf(_item.get("className"));
		float _x = transPixcel2Hwpunit(_item.get("x"));
		float _y = transPixcel2Hwpunit(_item.get("y"));
		float _w = transPixcel2Hwpunit(_item.get("width"));
		float _h = transPixcel2Hwpunit(_item.get("height"));
		
		if( _type == "h" )
		{
			if( _className.equals("UBGraphicsCircle") )
			{
				_graphicHFBuilder.append("<ELLIPSE ArcType=\"Normal\" Axis1X=\"" + _w + "\" Axis1Y=\"" + (_h / 2) + "\" Axis2X=\"" + (_w / 2) + "\" Axis2Y=\"0\" CenterX=\"" + (_w / 2) + "\" CenterY=\"" + (_h / 2) + "\" End1X=\"65\" End1Y=\"0\" End2X=\"0\" End2Y=\"0\" HasArcProperty=\"false\" IntervalDirty=\"false\" Start1X=\"23579\" Start1Y=\"0\" Start2X=\"0\" Start2Y=\"0\">\n");
			}
			else if( _className.equals("UBGraphicsLine") )
			{
				//float _x1 = transPixcel2Hwpunit( _item.get("x1") );
				//float _y1 = transPixcel2Hwpunit( _item.get("y1") );
				//float _x2 = transPixcel2Hwpunit( _item.get("x2") );
				//float _y2 = transPixcel2Hwpunit( _item.get("y2") );
				
				float _x0 = ValueConverter.getFloat( _item.get("x") );
				float _y0 = ValueConverter.getFloat( _item.get("y") );
				float _x1 = ValueConverter.getFloat( _item.get("x1") );
				float _y1 = ValueConverter.getFloat( _item.get("y1") );
				float _x2 = ValueConverter.getFloat( _item.get("x2") );
				float _y2 = ValueConverter.getFloat( _item.get("y2") );
				
//				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ================>_x=" + _x0 + ",_y=" + _y0 + ",_x1=" + _x1 + ",_y1=" + _y1 + ", _x2=" + _x2 + ", _y2=" + _y2);
				
				_h = Math.abs(_y2-_y1);

//				_y2 = _h;
//				_y1 = 0.0f;
				
				float _sx = transPixcel2Hwpunit2(_x1);
				float _sy =  transPixcel2Hwpunit2(_y1);
				float _ex = transPixcel2Hwpunit2(_x2);
				float _ey = transPixcel2Hwpunit2(_y2);
				
//				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ================>_sx=" + _sx + ",_sy=" + _sy + ", _ex=" + _ex + ", _ey=" + _ey);
				
				_graphicHFBuilder.append("<LINE EndX=\"" + _ex + "\" EndY=\"" + _ey + "\" IsReverseHV=\"false\" StartX=\"" + _sx + "\" StartY=\"" + _sy + "\">\n");
			}
			else
			{
				int radius = 0;
				if( _item.containsKey("rx"))
				{
					radius = (ValueConverter.getInteger(_item.get("rx")) / 2); 
				}
				
				_graphicHFBuilder.append("<RECTANGLE Ratio=\"" + radius + "\" X0=\"0\" Y0=\"0\" X1=\"" + _w + "\" Y1=\"0\" X2=\"" + _w + "\" Y2=\"" + _h + "\" X3=\"0\" Y3=\"" + _h + "\">\n");
			}
		}
		else
		{
			if( _className.equals("UBGraphicsCircle") )
			{
				_graphicHFBuilder.append("</ELLIPSE>\n");
			}
			else if( _className.equals("UBGraphicsLine") )
			{
				_graphicHFBuilder.append("</LINE>\n");
			}
			else
			{
				_graphicHFBuilder.append("</RECTANGLE>\n");
			}
		}
		return _graphicHFBuilder.toString();
	}
	
	private String getVerticalAlign( String _vAlign )
	{
		
		String retVA = "Top";
		
		if( _vAlign.equals("bottom") )
		{
			retVA = "Bottom";
		}
		else if( _vAlign.equals("middle") )
		{
			retVA = "Center";
		}
		else
		{
			retVA = "Top";
		}
		
		return retVA;
	}
	
	private String AlignSettingToParaShape( String _align )
	{
		String _psID = "0";
		
		if( _align.equals("center") )
		{
			_psID = "1";
		}
		else if( _align.equals("right") )
		{
			_psID = "3";
		}
		else
		{
			_psID = "2";
		}
			
		return _psID;
	}
	
	
	public String ImagesMatching_handler( HashMap<String, Object> _item, int _zOrder )
	{
		boolean _isOriginSize = false;
		
		//이미지 원본 비율 유지 속성
		if( _item.containsKey("isOriginalSize") &&  !_item.get("isOriginalSize").equals(""))
		{
			_isOriginSize = Boolean.valueOf(_item.get("isOriginalSize").toString());
		}
		
		StringBuilder _imageBuilder = new StringBuilder();
		String _id1 = String.valueOf(_instId1) + String.valueOf(_instId2);
		String _id2 = String.valueOf(_instId2) + String.valueOf(_instId1);
		
		float _x = transPixcel2Hwpunit(_item.get("x"));
		float _y = transPixcel2Hwpunit(_item.get("y"));
		float _w = transPixcel2Hwpunit(_item.get("width"));
		float _h = transPixcel2Hwpunit(_item.get("height"));
		
		if(_isOriginSize){
			String _imageUrl = ValueConverter.getString(_item.get("src"));				
			byte[] bAr = null;			
			if(!_imageUrl.equals("null") && _imageUrl.length()>0){
				bAr = common.getBytesLocalImageFile(_imageUrl);				
				if(bAr != null ){
					try {
						Image _image = Image.getInstance(bAr);
						HashMap<String,Float> _orignSize = common.getOriginSize(_w,_h,_image);
						_w =  _orignSize.get("width");
						_h = _orignSize.get("height");
						_x = _x + _orignSize.get("marginX");
						_y = _y + _orignSize.get("marginY");
					} catch (BadElementException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}
		}
		
		float _rotate = 0;
		
		if( _item.containsKey("rotation") )
		{
			_rotate = ValueConverter.getFloat(_item.get("rotation"));
		}
		
		HashMap<String, Float> _mat;
		
		_mat = getRotateMatrix( _x, _y , _w, _h , _rotate );
		
		String _imgId = ""; 
		
		String _textWrap = "Square";
		
		if( _item.containsKey("isBackgroundImage") && _item.get("isBackgroundImage").equals("true")) _textWrap = "BehindText";
		
		_imgId = ValueConverter.getString(_item.get("imgId"));
		
		_imageBuilder.append("<PICTURE Reverse=\"false\">\n");
//		_imageBuilder.append("<SHAPEOBJECT InstId=\"" + _id1 + "\" Lock=\"false\" NumberingType=\"Figure\" TextWrap=\"InFrontOfText\" ZOrder=\"" + _imgId + "\">\n");
		_imageBuilder.append("<SHAPEOBJECT InstId=\"" + _id1 + "\" Lock=\"false\" NumberingType=\"Figure\" TextWrap=\"" + _textWrap + "\" ZOrder=\"" + _zOrder + "\">\n");
		
		_imageBuilder.append("<SIZE Height=\"" + _mat.get("h") + "\" HeightRelTo=\"Absolute\" Protect=\"false\" Width=\"" + _mat.get("w") + "\" WidthRelTo=\"Absolute\"/>\n");
		_imageBuilder.append("<POSITION AffectLSpacing=\"false\" AllowOverlap=\"true\" FlowWithText=\"true\" HoldAnchorAndSO=\"false\" HorzAlign=\"Left\" HorzOffset=\"" + _mat.get("x") + "\" HorzRelTo=\"Column\" TreatAsChar=\"false\" VertAlign=\"Top\" VertOffset=\"" + _mat.get("y") + "\" VertRelTo=\"Para\"/>\n");
		_imageBuilder.append("<OUTSIDEMARGIN Bottom=\"0\" Left=\"0\" Right=\"0\" Top=\"0\"/>\n");
		_imageBuilder.append("</SHAPEOBJECT>\n");
		
		_imageBuilder.append("<SHAPECOMPONENT GroupLevel=\"0\" HorzFlip=\"false\" InstID=\"" + _id2 + "\" OriHeight=\"" + _h + "\" OriWidth=\"" + _w + "\" VertFlip=\"false\" XPos=\"" + _mat.get("tx") + "\" YPos=\"" + _mat.get("ty") + "\">\n");
		_imageBuilder.append("<ROTATIONINFO Angle=\"" + _mat.get("rotate") + "\" CenterX=\"" + _mat.get("cx") + "\" CenterY=\"" + _mat.get("cy") + "\"/>\n");
		_imageBuilder.append("<RENDERINGINFO>\n");
		_imageBuilder.append("<TRANSMATRIX E1=\"1.00000\" E2=\"0.00000\" E3=\"" + _mat.get("tx") + "\" E4=\"0.00000\" E5=\"1.00000\" E6=\"" + _mat.get("ty") + "\"/>\n");
		_imageBuilder.append("<SCAMATRIX E1=\"1.00000\" E2=\"0.00000\" E3=\"0.00000\" E4=\"0.00000\" E5=\"1.00000\" E6=\"0.00000\"/>\n");
		_imageBuilder.append("<ROTMATRIX E1=\"" + _mat.get("a") + "\" E2=\"" + _mat.get("c") + "\" E3=\"0.00000\" E4=\"" + _mat.get("b") + "\" E5=\"" + _mat.get("d") + "\" E6=\"0.00000\"/>\n");
		_imageBuilder.append("</RENDERINGINFO>\n");
		_imageBuilder.append("</SHAPECOMPONENT>\n");
		
//		if( _item["borderSide"] )
//		{
//			if( _item.borderSide != "none" )
//			{
//				var _bW:Number = ExportHwpParser.transPixcel2Hwpunit( _item.borderThickness );
//				var _bC:String = ExportHwpParser.ChangeColorToMSA( _item.borderColor );
//				
//				_imageBuilder.append("<LINESHAPE Alpha=\"0\" EndCap=\"Round\" OutlineStyle=\"Outer\" Style=\"Solid\" Width=\"" + _bW + "\" Color=\"" + _bC + "\"/>\n");
//			}
//		}
//		
		float _alpha = 1;
		
//		_alpha = _item.get("alpha");
		_imageBuilder.append("<IMAGERECT X0=\"0\" X1=\"" + _w + "\" X2=\"" + _w + "\" X3=\"0\" Y0=\"0\" Y1=\"0\" Y2=\"" + _h + "\" Y3=\"" + _h + "\"/>\n");
		_imageBuilder.append("<IMAGECLIP Bottom=\"" + "0" + "\" Left=\"0\" Right=\"" + "0" + "\" Top=\"0\"/>\n");
		_imageBuilder.append("<INSIDEMARGIN Bottom=\"0\" Left=\"0\" Right=\"0\" Top=\"0\"/>\n");
		if( !_imgId.equals("null") )
		{
			_imageBuilder.append("<IMAGE Alpha=\"" + getGraphicAlpha( _alpha ) + "\" BinItem=\"" + _imgId + "\" Bright=\"0\" Contrast=\"0\" Effect=\"RealPic\"/>\n");
		}
		_imageBuilder.append("</PICTURE>\n");
		
		_gIndex++;
		_instId1++;
		_instId2++;
		return _imageBuilder.toString();
	}
	
	
	public String TableMatching_handler( HashMap<String, Object> _item, int _zOrder ) 
	{
		StringBuilder _tableBuilder = new StringBuilder();
		StringBuilder _rowBuilder = new StringBuilder();
		StringBuilder _cellBuilder = new StringBuilder();
		
		
//		HashMap<String, ArrayList<Float>> _itemXY = new HashMap<String, ArrayList<Float>>();
//		ArrayList<Float> _itemX = new ArrayList<Float>();
//		ArrayList<Float> _itemY = new ArrayList<Float>();
//		
//		_itemXY = pointXY_Handeler( _item );
//		_itemX = _itemXY.get("x");
//		_itemY = _itemXY.get("y");
//		
		// border, background
		int _rowSpan = 0;
		int _colSpan = 0;
		
		HashMap<Integer, String> _rowObj = new HashMap<Integer, String>();
		
		ArrayList<ArrayList<HashMap<String, Object>>> _rows = new ArrayList<ArrayList<HashMap<String,Object>>>();
		ArrayList<HashMap<String, Object>> _cells = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> _cellItem = new HashMap<String, Object>();
		int _cellCnt = ValueConverter.getInteger( _item.get("columnCount") );
		int _rowCnt = ValueConverter.getInteger( _item.get("rowCount") );
//		int _cellCnt = (_itemX.size() - 1);
//		int _rowCnt = (_itemY.size() - 1);
		
		int _colIndex = 0;
		int _rowIndex = 0;
		
		_rows = (ArrayList<ArrayList<HashMap<String,Object>>>) _item.get("rows");
		
		
		for (int i = 0; i < _rows.size() ; i++) 
		{
			_cellBuilder = new StringBuilder();
			_rowBuilder.append("<ROW>\n");
			_colIndex = 0;

			_cells = _rows.get(i);
			
			for (int j = 0; j < _cells.size(); j++) 
			{
				_cellItem = _cells.get(j);
				
				if( _cellItem == null ) continue;
				
				float _cellW = transPixcel2Hwpunit( _cellItem.get("width") );
				float _cellH = transPixcel2Hwpunit( _cellItem.get("height") );
				
//				_colSpan = getSpanInt(_cellItem.get("x") , _cellItem.get("width") , _itemX);
//				_rowSpan = getSpanInt(_cellItem.get("y") , _cellItem.get("height") , _itemY);
				_colSpan = ValueConverter.getInteger( _cellItem.get("colSpan") );
				_rowSpan = ValueConverter.getInteger( _cellItem.get("rowSpan") );
				
				while(true)
				{
					if( _rowObj.containsKey(_colIndex) )
					{
						String[] _ar = _rowObj.get(_colIndex).split("#");
						int _c = 0;
						int _r = 0;
						
						_c = ValueConverter.getInteger(_ar[1]);
						_r = ValueConverter.getInteger(_ar[0]);
						
						if( _r == 0 )
						{
							_rowObj.remove(_colIndex);
						}
						else
						{
							_colIndex += _c;
						}
						
					}
					else
					{
						break;
					}
				}

				// 명우 padding 으로 인해 줄바꿈이 될경우.. 빼세요..
				// 여기 부터.
				// 1 = 283 , 2 = 566 , 0.5 = 141 , 1.5 = 425
				// padding은 3 당 283 의 값을 적용 함.
				float _padding = 283;
				if( _cellItem.containsKey("padding"))
				{
					float _mm = ( ValueConverter.getFloat(_cellItem.get("padding")) / 3f );
					
//					_padding = (_padding + ( _mm * 283f ));
					_padding = ( _mm * 283f );
				}
				if( ValueConverter.getString(_cellItem.get("textAlign")).equals("center") )
				{
					_padding = _padding - 141f;
				}
				// 여기 까지. 빼고 아래껄 주석 해제 해 주세요.
				// 이거
//				float _padding = 224;
//				if( _cellItem.containsKey("padding"))
//				{
//					_padding = transPixcel2Hwpunit(_cellItem.get("padding"));
//				}
				
				
				
				_cellBuilder.append("<CELL BorderFill=\"" + _cellItem.get("bID") + "\" ColAddr=\"" + _colIndex + "\" ColSpan=\"" + _colSpan + "\" Dirty=\"false\" Editable=\"false\" HasMargin=\"true\" Header=\"false\" Height=\"" + _cellH + "\" Protect=\"false\" RowAddr=\"" + _rowIndex + "\" RowSpan=\"" + _rowSpan + "\" Width=\"" + _cellW + "\">\n");
				if( _cellItem.containsKey("verticalAlign") )
				{
					String _vAlign;
					_vAlign = ValueConverter.getString( _cellItem.get("verticalAlign") );
					
					if(_vAlign.toLowerCase().equals("top"))
					{
						_cellBuilder.append("<CELLMARGIN Bottom=\"0\" Left=\"" + _padding + "\" Right=\"" + _padding + "\" Top=\"" + _padding + "\"/>\n");		
					}
					else if(_vAlign.toLowerCase().equals("bottom"))
					{
						_cellBuilder.append("<CELLMARGIN Bottom=\"" + _padding + "\" Left=\"" + _padding + "\" Right=\"" + _padding + "\" Top=\"0\"/>\n");
					}
					else
					{
						_cellBuilder.append("<CELLMARGIN Bottom=\"0\" Left=\"" + _padding + "\" Right=\"" + _padding + "\" Top=\"0\"/>\n");
					}
				}
				else
				{
					_cellBuilder.append("<CELLMARGIN Bottom=\"0\" Left=\"" + _padding + "\" Right=\"" + _padding + "\" Top=\"0\"/>\n");
				}
				_cellBuilder.append(ParaListMatching_handler( _cellItem ));
				_cellBuilder.append("</CELL>\n");
				
				if( _rowSpan != 1 )
				{
					_rowObj.put(_colIndex, _rowSpan + "#" + _colSpan);
				}
				
				_colIndex += _colSpan;
				
			} // for _cells.size();
			_rowBuilder.append(_cellBuilder.toString() + "</ROW>\n");
			
			ArrayList<Integer> _removeKey = new ArrayList<Integer>();
			
			for(Integer key : _rowObj.keySet()) 
			{
				String[] _arR = _rowObj.get(key).split("#");
				int _cR = 0;
				int _rR = 0;
				
				_cR = ValueConverter.getInteger(_arR[1]);
				_rR = ValueConverter.getInteger(_arR[0]);
				
				if( _rR == 0 )
				{
					_removeKey.add(key);
//					_rowObj.remove(key);
				}
				else
				{
					_rR = _rR -1;
					_rowObj.put(key, _rR + "#" + _cR);
				}
				
			}
			
			for (int j = 0; j < _removeKey.size(); j++) 
			{
				_rowObj.remove(_removeKey.get(j));
			}
			
			
			_rowIndex++;
			
		} // for _rows.size();
		
		_tableBuilder.append("<TABLE CellSpacing=\"0\" ColCount=\"" + _cellCnt + "\" PageBreak=\"Cell\" RepeatHeader=\"true\" RowCount=\"" + _rowCnt + "\">\n");
//		_tableBuilder.append("<P ParaShape=\"1\" Style=\"0\">\n");
//		_tableBuilder.append("<TEXT>\n");
//		_tableBuilder.append("<TABLE BorderFill=\"1\" CellSpacing=\"0\" ColCount=\"" + _cellCnt + "\" PageBreak=\"Cell\" RepeatHeader=\"true\" RowCount=\"" + _rowCnt + "\">\n");
		_tableBuilder.append(tableHeaderTag_Handler( _item, _zOrder ));
		_tableBuilder.append(_rowBuilder.toString() + "</TABLE>\n");

		return _tableBuilder.toString();
	}
	
	
	
	private String tableHeaderTag_Handler( HashMap<String, Object> _item, int _zOrder ) 
	{
		StringBuilder _tableHeaderBuilder = new StringBuilder();
		
		String _id1 = String.valueOf(_instId1) + String.valueOf(_instId2);
		
		float _x = transPixcel2Hwpunit(_item.get("x"));
		float _y = transPixcel2Hwpunit(_item.get("y"));
		float _w = transPixcel2Hwpunit(_item.get("width"));
		float _h = transPixcel2Hwpunit(_item.get("height"));
		
		_tableHeaderBuilder.append("<SHAPEOBJECT InstId=\"" + _id1 + "\" Lock=\"false\" NumberingType=\"Table\" TextWrap=\"TopAndBottom\" ZOrder=\"" +_zOrder +  "\">\n");
		_tableHeaderBuilder.append("<SIZE Height=\"" + _h + "\" HeightRelTo=\"Absolute\" Protect=\""+mProtected+"\" Width=\"" + _w + "\" WidthRelTo=\"Absolute\"/>\n");
		_tableHeaderBuilder.append("<POSITION AffectLSpacing=\"false\" AllowOverlap=\"false\" FlowWithText=\"true\" HoldAnchorAndSO=\"false\" HorzAlign=\"Left\" HorzOffset=\"" + _x + "\" HorzRelTo=\"Column\" TreatAsChar=\"false\" VertAlign=\"Top\" VertOffset=\"" + _y + "\" VertRelTo=\"Para\"/>\n");
		_tableHeaderBuilder.append("<OUTSIDEMARGIN Bottom=\"0\" Left=\"0\" Right=\"0\" Top=\"0\"/>\n");
		_tableHeaderBuilder.append("</SHAPEOBJECT>\n");
		_tableHeaderBuilder.append("<INSIDEMARGIN Bottom=\"0\" Left=\"0\" Right=\"0\" Top=\"0\"/>\n");
		
		_instId1++;
		return _tableHeaderBuilder.toString();
	}
	
	
//	private float getImageAlpha( float _alpha )
//	{
//		var _a:Number = 0;
//		
//		if( _alpha != 1 )
//		{
//			_a = 100 - Math.round( _alpha * 100 );
//		}
//		else
//		{
//			_a = 0;
//		}
//		
//		return _a;
//	}
	
	
	private float getGraphicAlpha( float _alpha )
	{
		float _a = 0f;
		float _b = 0f;
		float _c = 0f;
		
		if( _alpha == 0f ) return 255f;
		
		if( _alpha != 1f )
		{
			_b = 1f / _alpha; 
			_c = 255f / _b;
			_a = 255f - _c;
		}
		else
		{
			_a = 0f;
		}
		
		return _a;
	}

	
	private HashMap<String, Float> getRotateMatrix( float x , float y , float w , float h, float rotate )
	{
		HashMap<String, Float> _mat = new HashMap<String, Float>();
		
		if( rotate != 0 )
		{
			float _scale = 1f;
			
			float _cx;
			float _cy;
			
			float _cos;
			float _sin;
			
			float _tx;
			float _ty;
			
			Float[] _xList = {0f,w,0f,w};
			Float[] _yList = {0f,0f,h,h};
			Float[] _xAr = new Float[4];
			Float[] _yAr = new Float[4];
			
			_cos = (float) (Math.floor( Math.cos( rotate * Math.PI/180 ) * 1000000 ) / 1000000);
			_sin = (float) (Math.floor( Math.sin( rotate * Math.PI/180 ) * 1000000 ) / 1000000);
			
			for (int i = 0; i < 4; i++) 
			{
				float _xValue = 0;
				float _yValue = 0;
				
				_xValue = _xList[i];
				_yValue = _yList[i];
				_xAr[i] = (( _xValue * _cos ) - ( _yValue * _sin )) + x;
				_yAr[i] = (( _xValue * _sin ) + ( _yValue * _cos )) + y;
			}
			
			float _mxX1 = Math.max(_xAr[0],_xAr[1]);
			float _mxX2 = Math.max(_xAr[2],_xAr[3]);
			float _mxY1 = Math.max(_yAr[0],_yAr[1]);
			float _mxY2 = Math.max(_yAr[2],_yAr[3]);
			
			float _maxX = Math.max( _mxX1,_mxX2 );
			float _maxY = Math.max( _mxY1,_mxY2 );
			
			float _mnX1 = Math.min(_xAr[0],_xAr[1]);
			float _mnX2 = Math.min(_xAr[2],_xAr[3]);
			float _mnY1 = Math.min(_yAr[0],_yAr[1]);
			float _mnY2 = Math.min(_yAr[2],_yAr[3]);
			
			float _minX = Math.min( _mnX1,_mnX2 );
			float _minY = Math.min( _mnY1,_mnY2 );
			
			
			float _cW = (float) Math.floor( _maxX - _minX );
			float _cH = (float) Math.floor( _maxY - _minY );
			
			_tx = x - _minX; 
			_ty = y - _minY;
			
			_cx = (float) Math.floor( _cW / 2 );
			_cy = (float) Math.floor( _cH / 2 );
			
			_mat.put("x",_minX);
			_mat.put("y",_minY);
			_mat.put("w",_cW);
			_mat.put("h",_cH);
			_mat.put("rotate",rotate);
			_mat.put("cx",_cx);
			_mat.put("cy",_cy);
			_mat.put("a",_scale * _cos);
			_mat.put("b",_scale * _sin);
			_mat.put("c",_scale * _sin * -1);
			_mat.put("d",_scale * _cos);
			_mat.put("tx",_tx);
			_mat.put("ty",_ty);
			
		}
		else
		{
			_mat.put("x",x);
			_mat.put("y",y);
			_mat.put("w",w);
			_mat.put("h",h);
			_mat.put("rotate",rotate);
			_mat.put("cx",0f);
			_mat.put("cy",0f);
			_mat.put("a",1.00000f);
			_mat.put("b",0.00000f);
			_mat.put("c",0.00000f);
			_mat.put("d",1.00000f);
			_mat.put("tx",0f);
			_mat.put("ty",0f);
		}
		
		return _mat;
	}
	
	/**
	 *  HWP 에서 사용하는 사이즈로 변경
	 *  Pixcel => Hwpunit
	 * @param _num
	 * @return
	 */
	private float transPixcel2Hwpunit( Object _num )
	{
		if( _num == null ) return 0;
		float _mm = ValueConverter.getFloat(_num) * 0.26458333331386f;
		
		float _inch = _mm * 0.03937007874f; 
		
		float _hwpunit = (float) Math.floor( _inch * 7200f );  
		
		return _hwpunit;
	}

	private float transPixcel2Hwpunit2( float _num )
	{
		float _mm = _num * 0.26458333331386f;
		
		float _inch = _mm * 0.03937007874f; 
		
		float _hwpunit = (float) Math.floor( _inch * 7200f );  
		
		return _hwpunit;
	}
	
	/**
	 * borderType을 변경해서 돌려줌
	 * @param _side
	 * @return
	 */
	private String getBorderType( String _side )
	{
		String _type = "";
		_side = _side.toUpperCase();
		
		if( _side.equals("SOLD") || _side.equals("SOLID") )
		{
			_type = "Solid";
		}
		else if( _side.equals("DASH") )
		{
			_type = "Dash";
		}
		else if( _side.equals("DASH_DOT") )
		{
			_type = "DashDot";
		}
		else if( _side.equals("DASH_DOT_DOT") )
		{
			_type = "DashDotDot";
		}
		else if( _side.equals("DOT") )
		{
			_type = "Dot";
		}
		else if( _side.equals("DOUBLE") )
		{
			_type = "DoubleSlim";
		}
		else
		{
			_type = "None";
		}
		
		return _type;
	}
	
	/**
	 * border 굵기를 Hwp 에 맞춰서 돌려줌
	 * @param _num
	 * @return
	 */
//	private float getBorderWidth( int _num )
	private int getBorderWidth( int _num )
	{
//		float _width = 0;
		int _width = 0;
		
		switch(_num)
		{
			case 1:
//				_width = 0.25f;
				_width = 70; // 한글 2010;
				break;
			
			case 2:
//				_width = 0.5f;
				_width = 141; // 한글 2010;
				break;
			
			case 3:
//				_width = 0.7f;
				_width = 198; // 한글 2010;
				break;
			
			case 4:
			case 5:
//				_width = 1f;
				_width = 283; // 한글 2010;
				break;
			
			case 6:
//				_width = 1.5f;
				_width = 425; // 한글 2010;
				break;
			
			case 7:
			case 8:
			case 9:
//				_width = 2f;
				_width = 566; // 한글 2010;
				break;
			
			case 10:
//				_width = 3f;
				_width = 850; // 한글 2010;
				break;
			
			default:
//				_width = 0.12f;
				_width = 33; // 한글 2010;
				break;
		}
		
		return _width;
	}
	
	
	/**
	 * HWP 에서 사용하는 컬러로 변경
	 * unit => MSAColor
	 * @return
	 */
	private String ChangeColorToMSA( Integer _uint )
	{
		
		if( _uint == 0 ) return "0";
		
		String _color = Integer.toString(_uint, 16);
		
		while(_color.length() < 6){
			_color = "0" + _color;
		}

		return String.valueOf( Integer.parseInt( ( _color.substring( 4, 6 ) + _color.substring( 2, 4 ) + _color.substring( 0, 2 ) )  , 16 ));
		
	}
	private float[] linePoint( HashMap<String, Object> _item )
	{
		float _x = ValueConverter.getFloat(_item.get("x"));
		float _y = ValueConverter.getFloat(_item.get("y"));
		
		float _x1 = ValueConverter.getFloat(_item.get("x1"));
		float _y1 = ValueConverter.getFloat(_item.get("y1"));
		
		float _x2 = ValueConverter.getFloat(_item.get("x2"));
		float _y2 = ValueConverter.getFloat(_item.get("y2"));
		
		float _sX = 0;
		float _sY = 0;
		float _eX = 0;
		float _eY = 0;
		
		float _h = Math.abs(_y2-_y1);
	
		_sX = _x + _x1;
		_sY = _y;	
		
		_eX = _x + _x2;
		_eY = _y - _h;
		
		float[] _pointAr = { _sX , _sY , _eX , _eY };
		
		return _pointAr;
	}
	
	private HashMap<String, HashMap<String, ArrayList<Float>>> mPointXY = new HashMap<String, HashMap<String,ArrayList<Float>>>();
	
	public HashMap<String, ArrayList<Float>> pointXY_Handeler( HashMap<String, Object> _table )
	{
		ArrayList<Float> _pointX = new ArrayList<Float>();
		ArrayList<Float> _pointY = new ArrayList<Float>();

		HashMap<String, ArrayList<Float>> _tableIdMap = new HashMap<String, ArrayList<Float>>();
		
		 // cells가 없으면 rows 부터 for문 돌려야 함. 명우
		ArrayList<HashMap<String, Object>> _cells = (ArrayList<HashMap<String,Object>>) _table.get("cells");
		
		int _cellsSize = _cells.size();
		
		String _tID = "";
		
		_tID = ValueConverter.getString( _table.get("id"));
		
		if( mPointXY.containsKey( _tID ) == false )
		{
			
			for (int i = 0; i < _cellsSize ; i++)
			{
				HashMap<String, Object> _cellItem = _cells.get(i);
				
				float _x = ValueConverter.getFloat(_cellItem.get("x"));
				float _y = ValueConverter.getFloat(_cellItem.get("y"));
				float _w = ValueConverter.getFloat(_cellItem.get("width"));
				float _h = ValueConverter.getFloat(_cellItem.get("height"));
				
				
				if( _pointX.indexOf( _x ) == -1 ) _pointX.add( _x );
				if( _pointX.indexOf( _x + _w ) == -1 ) _pointX.add( _x + _w );
				
				if( _pointY.indexOf( _y ) == -1 ) _pointY.add( _y );
				if( _pointY.indexOf( _y + _h ) == -1 ) _pointY.add( _y + _h );
				
			}
			
			
			Comparator<Float> _byFirstElement = new Comparator<Float>() {
				@Override
				public int compare(Float arg0, Float arg1) {
					// TODO Auto-generated method stub
					return arg0 < arg1 ? -1: arg0 > arg1 ? 1:0;
				}
			};
			
			
			Collections.sort(_pointX, _byFirstElement);
			Collections.sort(_pointY, _byFirstElement);
			
			_tableIdMap.put("x", _pointX);
			_tableIdMap.put("y", _pointY);
			
			mPointXY.put(_tID, _tableIdMap);
			
			return mPointXY.get(_tID);
		}
		else
		{
			return mPointXY.get(_tID);
		}
	}

	
	/**
	 * colspan 과 rowspan 값을 돌려 준다.
	 * @param _a
	 * @param _b
	 * @param _List
	 * @return
	 */
	private int getSpanInt(Object _a , Object _b , ArrayList<Float> _List )
	{
		int _span = _List.indexOf( ValueConverter.getFloat(_b) + ValueConverter.getFloat(_a) ) - _List.indexOf(ValueConverter.getFloat(_a));
		return _span;
	}
	
	private String getBorderWidthString(String _borderType,  int _num )
	{
		String _widthStr = "";
		
		if( _borderType.equals("DoubleSlim"))
		{
			_widthStr = String.valueOf( Math.floor(_num * 141.5) );
		}
		else
		{
			_widthStr = String.valueOf(  Math.floor( _num * (141.5/5*2) )  );
//			_widthStr = String.valueOf( getBorderWidth(_num) );
		}
		
		return _widthStr;
	}
}
