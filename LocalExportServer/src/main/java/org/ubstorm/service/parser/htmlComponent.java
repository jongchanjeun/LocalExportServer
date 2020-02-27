package org.ubstorm.service.parser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;

public class htmlComponent {
	
	private int _gIndex = 0;
	public HashMap mImgIdList;;
	public String replaceText = "";
	
	public String mProtected = "true";
	
	public void init()
	{
		mImgIdList = new HashMap();
	}
	
	
	public String RectangleMatching_handler( HashMap<String, Object> _item, int _zOrder)
	{
		StringBuilder _rectangleBuilder = new StringBuilder();
		StringBuilder _styleBuilder = new StringBuilder();
		StringBuilder _styleTranformBuilder = new StringBuilder();
		
		int _x = toNum(_item.get("x"));
		int _y = toNum(_item.get("y"));
		int _w = toNum(_item.get("width"));
		int _h = toNum(_item.get("height"));
		
		float _lineHeight = ValueConverter.getFloat(_item.get( "lineHeight" )) * 100;
		
		float _backgroundAlpha = 0;
		float _alpha = 0;
		
		float _rotate = 0;
		
		if( _item.containsKey("rotation") )
		{
			_rotate = ValueConverter.getFloat(_item.get("rotation"));
		}
		HashMap<String, Float> _mat = getRotateMatrix( _x, _y , _w, _h , _rotate );
		
		_backgroundAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		_alpha = ValueConverter.getFloat(_item.get("alpha"));
		
		ArrayList<Integer> _lineWs = (ArrayList<Integer>) _item.get("borderWidths"); 
		int _lineW = 0;

		_styleTranformBuilder.append("transform:rotate(" + _mat.get("rotate")  + "deg); transform-origin: 0% 0%;");
		_styleTranformBuilder.append("-ms-transform:rotate(" + _mat.get("rotate")  + "deg); -ms-transform-origin: 0% 0%;");	// IE9
		_styleTranformBuilder.append("-webkit-transform:rotate(" + _mat.get("rotate")  + "deg); -webkit-transform-origin: 0% 0%;");	// Chrome, Safari, Opera
				
		//_styleBuilder.append("transform:rotate(" + _mat.get("rotate")  + "deg); transform-origin: 0% 0%; position:absolute;" + "left:" + _x + "px;" + "top:" + _y + "px;" + "width:" + _w + "px;" + "height:" + _h + "px;" + "display:table;");
		_styleBuilder.append(_styleTranformBuilder.toString() + "position:absolute;" + "left:" + _x + "px;" + "top:" + _y + "px;" + "width:" + _w + "px;" + "height:" + _h + "px;" + "display:table;" + "vertical-align:baseline;line-height:" + _lineHeight +"%;");
		
		String _bgColor = "RGB(255,255,255)";
		if( _item.containsKey( "backgroundColorInt" ) )
		{
			int _backColor = ValueConverter.getInteger(_item.get("backgroundColorInt"));
			_bgColor = changeColorToRGBA( _backColor , _backgroundAlpha*_alpha );			
			_styleBuilder.append("background-color:" + _bgColor  + ";" );			
		}
		
		if( _item.containsKey("borderSide") )
		{
			ArrayList<Integer> _borderColors = (ArrayList<Integer>) _item.get("borderColorsInt");
			int _borderColor = ValueConverter.getInteger(_item.get("borderColorInt"));
			ArrayList<String> a = new ArrayList<String>();
			ArrayList<String> _types = (ArrayList<String>) _item.get("borderTypes");
			
			String _className = ValueConverter.getString(_item.get("className"));
			
			if( _className.equals("UBTextArea") )
			{
			}
			else
			{
				a = (ArrayList<String>) _item.get("borderSide");
				if( a.size() == 0 || a.size() > 4 )	{	}
				else
				{
					for (int i = 0; i < a.size(); i++) 
					{
						String _side = _types.get(i);
						_lineW = _lineWs.get(i);
						if( _side == "none" || _side == "" )
						{
							_styleBuilder.append("border-" + ValueConverter.getString(a.get(i)) + ":" + _lineW + "px" + " " + "solid" + " " + _bgColor + ";");
						}
						else
						{
							_styleBuilder.append("border-" + ValueConverter.getString(a.get(i)) + ":" + _lineW + "px" + " " + getBorderType(_side) + " " + changeColorToRGBA(_borderColors.get(i),_alpha) + ";");
						}
					}
				}
			}
		}

		 if( _item.containsKey("textAlign") )
	     {
	          String _hAlign = ValueConverter.getString(_item.get("textAlign"));
	 
	         _styleBuilder.append("text-align:" + _hAlign + ";"); 
	     }
		 
		 /*
		 if( _item.containsKey("verticalAlign") )
	     {
	          String _vAlign = ValueConverter.getString(_item.get("verticalAlign"));
	 
	         _styleBuilder.append("vertical-align:" + _vAlign + ";"); 
	     }
		 */
		 
		 if( _item.containsKey("fontFamily") )
	     {
	         String _fontFamily = ValueConverter.getString(_item.get("fontFamily"));
	          
	        _styleBuilder.append("font-family:" + _fontFamily + ";"); 
	     }
		 
		 if( _item.containsKey("fontSize") )
		 {
			 String _fonSize = ValueConverter.getString(_item.get("fontSize"));
           
			 _styleBuilder.append("font-size:" + _fonSize + "px;"); 
		 }
		 
		 if( _item.containsKey("fontWeight") )
	     {
	         String _fontWeight = ValueConverter.getString(_item.get("fontWeight"));
	         //System.out.println("htmlComponent::_fontWeight===================================================>>>>>" + _fontWeight);
	         _fontWeight = _fontWeight.equals("true") || _fontWeight.equals("bold")  ? "bold" : "normal";
	 
	        _styleBuilder.append("font-weight:" + _fontWeight + ";"); 
	     }	
		 
		 if( _item.containsKey("fontStyle") )
		 {
			boolean _isItalic = ValueConverter.getBoolean(_item.get("fontStyle"));
			String _fontStyle = _isItalic ? "italic" : "none";
			
			_styleBuilder.append("font-style:" + _fontStyle + ";"); 
		 }
		 
		 if( _item.containsKey("textDecoration") )
		 {
			boolean _isDecoration = ValueConverter.getBoolean(_item.get("textDecoration"));
			String _textDecoration = _isDecoration ? "underline" : "none";
			
			_styleBuilder.append("text-decoration:" + _textDecoration + ";"); 
		 }
		 
		 if( _item.containsKey( "fontColorInt" ) )
		 {
			 int _fontColor = ValueConverter.getInteger(_item.get("fontColorInt"));
			
			 _styleBuilder.append("color:" + changeColorToRGBA( _fontColor , _alpha )  + ";");			
		 }
				 
		 float _padding = 3;
	      _rectangleBuilder.append("<DIV style=\"" + _styleBuilder.toString() + "\">");
	      
	      String _vAlign = "top";
	      if( _item.containsKey("verticalAlign") )
		  {
		      _vAlign = ValueConverter.getString(_item.get("verticalAlign"));
		  }
	      _rectangleBuilder.append("<span style=\"padding:"+_padding+"px; display:table-cell; vertical-align:" + _vAlign + ";\">");
	 
	      _rectangleBuilder.append(ParaListMatching_handler( _item ));
	     
	     _rectangleBuilder.append("</span>");
	     _rectangleBuilder.append("</DIV>\n");
		
		return _rectangleBuilder.toString();
	}
	
	
	public String changeColorToRGB(int _color)
	{
	    java.awt.Color _c = new  java.awt.Color(_color);
	    return "RGB(" + _c.getRed() + "," + _c.getGreen() + "," + _c.getBlue() + ")";
	}
	
	public String changeColorToRGBA(int _color , float alpha)
	{
	    java.awt.Color _c = new  java.awt.Color(_color);
	    return "RGBA(" + _c.getRed() + "," + _c.getGreen() + "," + _c.getBlue() + "," + alpha + ")";
	}
	
	public String SVGRichTextLabelMatching_handler( HashMap<String, Object> _item )
	{
		StringBuilder _richTextBuilder = new StringBuilder();
		return _richTextBuilder.toString();
	}

	private String SVGRichTextMatching_handler( ArrayList<HashMap<String,Object>> _item )
	{
		StringBuilder _rTextBuilder = new StringBuilder();
		return _rTextBuilder.toString();
	}
	
	
	private String ParaListMatching_handler( HashMap<String, Object> _item )
	{
		StringBuilder _pTagBuilder = new StringBuilder();
		
		if( _item.containsKey("text") )
		{
			String _txt = ValueConverter.getString(_item.get("text"));
			// 줄바꿈 처리
			_txt = _txt.replace("\\n", "\n").replaceAll("\\r", "\r");
			
			_txt = _txt.replaceAll("&", "&amp;");
			// < > 부호 변환 후 줄바꿈 변경.
			_txt = _txt.replaceAll("<", "&lt;").replace(">", "&gt;");

			_txt = _txt.replaceAll("\r\n", "<br/>");
			_txt = _txt.replaceAll("\n", "<br/>");
			_txt = _txt.replaceAll("\r", "");// 최명진 과장 2016 04 15
			
			_pTagBuilder.append(_txt);
		}
		
		return _pTagBuilder.toString();
	}
	
	public String GraphicMatching_handler( HashMap<String, Object> _item, int _zOrder)
	{
		StringBuilder _graphicBuilder = new StringBuilder();
		
		int _x = toNum(_item.get("x"));
		int _y = toNum(_item.get("y"));
		int _w = toNum(_item.get("width"));
		int _h = toNum(_item.get("height"));
		
		float _borderAlpha = 0;
		float _backgroundAlpha = 0;
		float _alpha = 0;
		
		float _lineW = 0;
		String _lineC = "";

		String _bColor1 = "";
		String _bColor2 = "";
		
		String _className = ValueConverter.getString(_item.get("className"));
		float _rotate = 0;
		
		if( _className.equals("UBGraphicsCircle") )
		{
			// 원;
			_borderAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
			_backgroundAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
			_alpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
			
			_w = toNum(_item.get("width"));
			_h = toNum(_item.get("height"));
			
		}
		else if( _className.equals("UBGraphicsLine") )
		{
			// 선;
			_borderAlpha = 1;
			_backgroundAlpha = 1;
			_alpha = 1;
			
			float[] _point = linePoint(_item);
			
			_w = (int) Math.abs (_point[0] - _point[2] );
			
			if( _w < 1 )
			{
				_w = toNum( _item.get("thickness") );
			}
			
			_h = (int) Math.abs (_point[1] - _point[3] );
			
			if( _h < 1 )
			{
				_h = toNum( _item.get("thickness") );
			}
			
		}
		else if( _className.equals("UBGraphicsGradiantRectangle") )
		{
			// 그라디언트사각형;
//			_borderAlpha = _item['borderAlpha'];
//			_backgroundAlpha = _item['contentBackgroundAlphas'][0];
//			_alpha = _item['alpha'];
			ArrayList<String> _cbAlphas = (ArrayList<String>) _item.get("contentBackgroundAlphas");
			_borderAlpha = _cbAlphas.size()  >  0 ? ValueConverter.getFloat(_cbAlphas.get(0)) :  1;
			_backgroundAlpha = _cbAlphas.size()  >  0 ? ValueConverter.getFloat(_cbAlphas.get(0)) :  1;
			_alpha =  _cbAlphas.size()  >  0 ?  ValueConverter.getFloat(_cbAlphas.get(0)) :  1;
			
			_w = toNum(_item.get("width"));
			_h = toNum(_item.get("height"));
		}
		else
		{
			// 사각형;
			_borderAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
			_backgroundAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
			_alpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
			
			_w = toNum(_item.get("width"));
			_h = toNum(_item.get("height"));
		}

		if( _item.containsKey("rotation") )
		{
			_rotate = ValueConverter.getFloat(_item.get("rotation"));
		}
		
		HashMap<String, Float> _mat = getRotateMatrix( _x, _y , _w, _h , _rotate );		
		
		_graphicBuilder.append("<svg width=\"" + (_mat.get("w")+20) + "px\" height=\"" + (_mat.get("h")+20) + "px\" style=\"position:absolute;left:" + _mat.get("x") + "px;top:" + _mat.get("y") + "px;\">");
		if( _className.equals("UBGraphicsLine") )
		{
			float[] _linePoint = linePoint(_item);
			
			_lineW = toNum( _item.get("thickness") );
			_lineC = changeColorToRGB( ValueConverter.getInteger(_item.get("lineColorInt")) );
			
			_graphicBuilder.append("<line x1=\"" + (int) Math.abs(_x-_linePoint[0]) + "\" y1=\"" + (int) Math.abs(_y-_linePoint[1]) + "\" x2=\"" + (int) Math.abs(_x-_linePoint[2]) + "\" y2=\"" + (int) Math.abs(_y-_linePoint[3]) + "\" style=\"stroke-opacity:" + _borderAlpha + ";stroke-width:" + _lineW + "px;stroke:" + _lineC + ";\" />");
		}
		else
		{
			_lineW = toNum( _item.get("borderThickness") );
			_lineC = changeColorToRGB( ValueConverter.getInteger(_item.get("borderColorInt")) );
			
			if( _className.equals("UBGraphicsGradiantRectangle") )
			{
				ArrayList<String> _bColors = (ArrayList<String>) _item.get("contentBackgroundColorsInt");
				
				_bColor1 = changeColorToRGB( ValueConverter.getInteger(_bColors.get(0)) );
				_bColor2 = changeColorToRGB( ValueConverter.getInteger(_bColors.get(1)));
				
				_graphicBuilder.append("<defs>");
				_graphicBuilder.append("<linearGradient id=\"grad" + _gIndex + "\" x1=\"0%\" y1=\"50%\" x2=\"0%\" y2=\"100%\">");
				_graphicBuilder.append("<stop offset=\"0%\" style=\"stop-color:" + _bColor1 + ";stop-opacity:1\" />");
				_graphicBuilder.append("<stop offset=\"100%\" style=\"stop-color:" + _bColor2 + ";stop-opacity:1\" />");
				_graphicBuilder.append("</linearGradient>");
				_graphicBuilder.append("</defs>");

				_graphicBuilder.append("<rect transform=\"" + "translate(" + _mat.get("tx") + " " + _mat.get("ty") + ") rotate(" + _mat.get("rotate") + " " + _mat.get("cx") + " " + _mat.get("cy") + ")" + "\" width=\"" + _w + "px\" height=\"" + _h + "px\" style=\"fill:" + "url(#grad" + _gIndex + ")" + ";fill-opacity:" + _backgroundAlpha + ";stroke-opacity:" + _borderAlpha + ";stroke-width:" + _lineW + "px;stroke:" + _lineC + ";\" />");
			}
			else
			{
				_bColor1 = changeColorToRGB( ValueConverter.getInteger(_item.get("contentBackgroundColorInt")) );
				
				if( _className.equals("UBGraphicsCircle") )
				{
					float _cx = _w/2;
					float _cy = _h/2;
					float _rx = _w/2;
					float _ry = _h/2;
					
					_graphicBuilder.append("<ellipse width=\"" + _w + "px\" height=\"" + _h + "px\" cx=\"" + _cx + "px\" cy=\"" + _cy + "px\" rx=\"" + _rx + "px\" ry=\"" + _ry + "px\" style=\"fill:" + _bColor1 + ";fill-opacity:" + _backgroundAlpha + ";stroke-opacity:" + _borderAlpha + ";stroke-width:" + _lineW + "px;stroke:" + _lineC + ";\" />");
				
				}
				else
					_graphicBuilder.append("<rect transform=\"" + "translate(" + _mat.get("tx") + " " + _mat.get("ty") + ") rotate(" + _mat.get("rotate") + " " + _mat.get("cx") + " " + _mat.get("cy") + ")" + "\" width=\"" + _w + "px\" height=\"" + _h + "px\" style=\"fill:" + _bColor1 + ";fill-opacity:" + _backgroundAlpha + ";stroke-opacity:" + _borderAlpha + ";stroke-width:" + _lineW + "px;stroke:" + _lineC + ";\" />");
			}
			
		}
		
		_graphicBuilder.append("</svg>");
		
		_gIndex++;
		
		return _graphicBuilder.toString();
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
	
	
	private String GraphicTypeTxtSetting( HashMap<String, Object> _item , String _type )
	{
		StringBuilder _graphicHFBuilder = new StringBuilder();
		return _graphicHFBuilder.toString();
	}
	
	public String ImagesMatching_handler( HashMap<String, Object> _item, int _zOrder, HashMap<String, String> _mImageListHm )
	{
		
		boolean _isOriginSize = false;
		
		//이미지 원본 비율 유지 속성
		if( _item.containsKey("isOriginalSize") &&  !_item.get("isOriginalSize").equals(""))
		{
			_isOriginSize = Boolean.valueOf(_item.get("isOriginalSize").toString());
		}
		
		StringBuilder _imageBuilder = new StringBuilder();
		
		int _x = toNum(_item.get("x"));
		int _y = toNum(_item.get("y"));
		int _w = toNum(_item.get("width"));
		int _h = toNum(_item.get("height"));
		
		if(_isOriginSize){
			String _imageUrl = ValueConverter.getString(_item.get("src"));				
			byte[] bAr = null;			
			if(!_imageUrl.equals("null") && _imageUrl.length()>0){
				bAr = common.getBytesLocalImageFile(_imageUrl);				
				if(bAr != null ){
					try {
						Image _image = Image.getInstance(bAr);
						HashMap<String,Float> _orignSize = common.getOriginSize(_w,_h,_image);
						_w = Math.round(_orignSize.get("width"));
						_h =  Math.round(_orignSize.get("height"));
						_x = _x +  Math.round(_orignSize.get("marginX"));
						_y = _y +  Math.round(_orignSize.get("marginY"));
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
		_imgId = ValueConverter.getString(_item.get("imgId"));
		
		float _alpha = 1;
		
		String _imageCode = _mImageListHm.get(_imgId);
        if( !_imgId.equals("null")) {
            _imageBuilder.append("<img src=\"" + _imageCode + "\" style=\"position:absolute;left:" + _x + "px;top:" + _y + "px;width:" + _w + "px;height:" + _h + "px;\"> ");
        }
		
		_gIndex++;
		return _imageBuilder.toString();
	}
	
		
	public String TableMatching_handler( HashMap<String, Object> _item, int _zOrder) 
	{
		StringBuilder _tableBuilder = new StringBuilder();
		StringBuilder _rowBuilder = new StringBuilder();
		StringBuilder _cellBuilder = new StringBuilder();
		
		int _x = toNum(_item.get("x"));
		int _y = toNum(_item.get("y"));
		int _w = toNum(_item.get("width"));
		int _h = toNum(_item.get("height"));
		
		
		
		StringBuilder _styleBuilder = new  StringBuilder();
		_styleBuilder.append("position:absolute;" + "left:" + _x + "px;" + "top:" + _y + "px;" + "width:" + _w + "px;" + "height:" + _h + "px;");
		 if( _item.containsKey("fontFamily") )
	     {
	         String _fontFamily = ValueConverter.getString(_item.get("fontFamily"));
	          
	        _styleBuilder.append("font-family:" + _fontFamily + ";"); 
	     }
		 
		 if( _item.containsKey("fontSize") )
		 {
			 String _fonSize = ValueConverter.getString(_item.get("fontSize"));
           
			 _styleBuilder.append("font-size:" + _fonSize + "px;"); 
		 }			
		
		// border, background
		int _rowSpan = 0;
		int _colSpan = 0;
		
		HashMap<Integer, String> _rowObj = new HashMap<Integer, String>();
		
		ArrayList<ArrayList<HashMap<String, Object>>> _rows = new ArrayList<ArrayList<HashMap<String,Object>>>();
		ArrayList<HashMap<String, Object>> _cells = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> _cellItem = new HashMap<String, Object>();
		int _cellCnt = ValueConverter.getInteger( _item.get("columnCount") );
		int _rowCnt = ValueConverter.getInteger( _item.get("rowCount") );
		
		int _colIndex = 0;
		int _rowIndex = 0;
		
		_rows = (ArrayList<ArrayList<HashMap<String,Object>>>) _item.get("rows");
		
		for (int i = 0; i < _rows.size() ; i++) 
		{
			_cellBuilder = new StringBuilder();
			_rowBuilder.append("<TR>\n");
			_colIndex = 0;

			_cells = _rows.get(i);
			
			for (int j = 0; j < _cells.size(); j++) 
			{
				_cellItem = _cells.get(j);
				
				if( _cellItem == null ) continue;
				
				int _cellW = toNum( _cellItem.get("width") );
				int _cellH = toNum( _cellItem.get("height") );		
				
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

				
				StringBuilder _cellStyleBuilder = new  StringBuilder();
				 if( _cellItem.containsKey("fontFamily") )
			     {
			         String _fontFamily = ValueConverter.getString(_cellItem.get("fontFamily"));
			          
			         _cellStyleBuilder.append("font-family:" + _fontFamily + ";"); 
			     }
				 
				 if( _cellItem.containsKey("fontSize") )
				 {
					 String _fonSize = ValueConverter.getString(_cellItem.get("fontSize"));
		            int _cellFonSize = toNum( _fonSize );						
					 _cellStyleBuilder.append("font-size:" + _cellFonSize + "px;"); 
				 }	
				 
				 _cellStyleBuilder.append("width:" + _cellW + "px;"); 
				 _cellStyleBuilder.append("height:" + (_cellH-1) + "px;"); 
				 
				 String _hAlign = "left";
				 if( _cellItem.containsKey("textAlign") )
				 {
					 _hAlign = ValueConverter.getString( _cellItem.get("textAlign") );
				 }
				 
				 String _vAlign = "middle";
				 if( _cellItem.containsKey("verticalAlign") )
				 {
					_vAlign = ValueConverter.getString( _cellItem.get("verticalAlign") );
				 }
				 
				 float _backgroundAlpha = ValueConverter.getFloat(_cellItem.get("backgroundAlpha"));
				 float _alpha = ValueConverter.getFloat(_cellItem.get("alpha"));
				
				 String _bgColor = "RGB(255,255,255)";
				 if( _cellItem.containsKey( "backgroundColorInt" ) )
				 {
					int _backColor = ValueConverter.getInteger(_cellItem.get("backgroundColorInt"));
					_bgColor = changeColorToRGBA( _backColor , _alpha);
					_cellStyleBuilder.append("background-color:" + _bgColor  + ";");			
				 }
				 
				 ArrayList<Integer> _lineWs = (ArrayList<Integer>) _cellItem.get("borderWidths"); 
				 int _lineW = 0;
				 if( _cellItem.containsKey("borderSide") )
				 {
					ArrayList<Integer> _borderColors = (ArrayList<Integer>) _cellItem.get("borderColorsInt");
					int _borderColor = ValueConverter.getInteger(_cellItem.get("borderColorInt"));
					ArrayList<String> _borderSides = (ArrayList<String>) _cellItem.get("borderSide");
					ArrayList<String> _types = (ArrayList<String>) _cellItem.get("borderTypes");
					
					String _className = ValueConverter.getString(_cellItem.get("className"));
					
					if( _className.equals("UBTextArea") )
					{
					}
					else
					{
						if( _borderSides.size() == 0 || _borderSides.size() > 4 )	{	}
						else
						{
							_cellStyleBuilder.append("border-side:" + _borderSides.get(3) + " " + _borderSides.get(2) + " " + _borderSides.get(0) + " " + _borderSides.get(1) + ";");
							_cellStyleBuilder.append("border-style:" + getBorderType(_types.get(3)) + " " + getBorderType(_types.get(2)) + " " + getBorderType(_types.get(0)) + " " + getBorderType(_types.get(1)) + ";");
							_cellStyleBuilder.append("border-width:" + (getBorderType(_types.get(3))=="None" ? 0 :_lineWs.get(3)) + "px; " + (getBorderType(_types.get(2))=="None"? 0 :_lineWs.get(2)) + "px; " + (getBorderType(_types.get(0))=="None"? 0 :_lineWs.get(0)) + "px; " + (getBorderType(_types.get(1))=="None"? 0 :_lineWs.get(1)) + "px;");
							_cellStyleBuilder.append("border-color:" + changeColorToRGBA(_borderColors.get(3),_alpha) + " " + changeColorToRGBA(_borderColors.get(2),_alpha) + " " + changeColorToRGBA(_borderColors.get(0),_alpha) + " " + changeColorToRGBA(_borderColors.get(1),_alpha) + ";");
						}
					}
				 }
				 if( _cellItem.containsKey("lineHeight") )
				 {
					 float _lineHeight = ValueConverter.getFloat(_cellItem.get( "lineHeight" )) * 100;
					 _cellStyleBuilder.append("line-height:" + _lineHeight  + "%;");		
					 _cellStyleBuilder.append("vertical-align:baseline;");		
					 
				 }
					
				 // top, right, bottom, left 순
			 	//_cellStyleBuilder.append("border-style:solid dotted solid dashed; border-width: 1px 1px 1px 1px; border-color: RGB(255,0,0) RGB(0,255,0) RGB(0,0,255) RGB(0,0,0);");		
				_cellBuilder.append("<TD ColAddr=\"" + _colIndex + "\" ColSpan=\"" + _colSpan + "\" RowAddr=\"" + _rowIndex + "\" RowSpan=\"" + _rowSpan + "\" align=\"" + _hAlign + "\" valign=\"" + _vAlign + "\" style=\"" + _cellStyleBuilder.toString() + "\">\n");
				
				_cellBuilder.append(ParaListMatching_handler( _cellItem ));
				_cellBuilder.append("</TD>\n");
				
				if( _rowSpan != 1 )
				{
					_rowObj.put(_colIndex, _rowSpan + "#" + _colSpan);
				}
				
				_colIndex += _colSpan;
				
			} // for _cells.size();
			_rowBuilder.append(_cellBuilder.toString() + "</TR>\n");
			
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
		
		_tableBuilder.append("<DIV style=\"" + _styleBuilder.toString() + "\">\n");
		_tableBuilder.append("<TABLE style=\"border-collapse:collapse;\" cellspacing=\"0px\" cellpadding=\"0px\">\n");
		_tableBuilder.append(tableHeaderTag_Handler( _item, _zOrder ));
		_tableBuilder.append(_rowBuilder.toString() + "</TABLE>\n");
		_tableBuilder.append("</DIV>\n");

		return _tableBuilder.toString();
	}
	
	public String TableMatching_handlerDiv( HashMap<String, Object> _item, int _zOrder) 
	{
		StringBuilder _tableBuilder = new StringBuilder();
		StringBuilder _rowBuilder = new StringBuilder();
		StringBuilder _cellBuilder = new StringBuilder();
		
		int _x = toNum(_item.get("x"));
		int _y = toNum(_item.get("y"));
		int _w = toNum(_item.get("width"));
		int _h = toNum(_item.get("height"));
		
		
		
		StringBuilder _styleBuilder = new  StringBuilder();
		_styleBuilder.append("position:absolute;" + "left:" + _x + "px;" + "top:" + _y + "px;" + "width:" + _w + "px;" + "height:" + _h + "px;");
		 if( _item.containsKey("fontFamily") )
	     {
	         String _fontFamily = ValueConverter.getString(_item.get("fontFamily"));
	          
	        _styleBuilder.append("font-family:" + _fontFamily + ";"); 
	     }
		 
		 if( _item.containsKey("fontSize") )
		 {
			 String _fonSize = ValueConverter.getString(_item.get("fontSize"));
           
			 _styleBuilder.append("font-size:" + _fonSize + "px;"); 
		 }			
		
		// border, background
		int _rowSpan = 0;
		int _colSpan = 0;
		
		HashMap<Integer, String> _rowObj = new HashMap<Integer, String>();
		
		ArrayList<ArrayList<HashMap<String, Object>>> _rows = new ArrayList<ArrayList<HashMap<String,Object>>>();
		ArrayList<HashMap<String, Object>> _cells = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> _cellItem = new HashMap<String, Object>();
		int _cellCnt = ValueConverter.getInteger( _item.get("columnCount") );
		int _rowCnt = ValueConverter.getInteger( _item.get("rowCount") );
		
		int _colIndex = 0;
		int _rowIndex = 0;
		
		_rows = (ArrayList<ArrayList<HashMap<String,Object>>>) _item.get("rows");
		
		for (int i = 0; i < _rows.size() ; i++) 
		{
			_cellBuilder = new StringBuilder();
			_rowBuilder.append("<TR>\n");
			_colIndex = 0;

			_cells = _rows.get(i);
			
			for (int j = 0; j < _cells.size(); j++) 
			{
				_cellItem = _cells.get(j);
				
				if( _cellItem == null ) continue;
				
				int _cellW = toNum( _cellItem.get("width") );
				int _cellH = toNum( _cellItem.get("height") );		
				
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

				
				StringBuilder _cellStyleBuilder = new  StringBuilder();
				 if( _cellItem.containsKey("fontFamily") )
			     {
			         String _fontFamily = ValueConverter.getString(_cellItem.get("fontFamily"));
			          
			         _cellStyleBuilder.append("font-family:" + _fontFamily + ";"); 
			     }
				 
				 if( _cellItem.containsKey("fontSize") )
				 {
					 String _fonSize = ValueConverter.getString(_cellItem.get("fontSize"));
		            int _cellFonSize = toNum( _fonSize );						
					 _cellStyleBuilder.append("font-size:" + _cellFonSize + "px;"); 
				 }	
				 
				 _cellStyleBuilder.append("width:" + _cellW + "px;"); 
				 _cellStyleBuilder.append("height:" + (_cellH-1) + "px;"); 
				 
				 String _hAlign = "left";
				 if( _cellItem.containsKey("textAlign") )
				 {
					 _hAlign = ValueConverter.getString( _cellItem.get("textAlign") );
				 }
				 
				 String _vAlign = "middle";
				 if( _cellItem.containsKey("verticalAlign") )
				 {
					_vAlign = ValueConverter.getString( _cellItem.get("verticalAlign") );
				 }
				 
				 float _backgroundAlpha = ValueConverter.getFloat(_cellItem.get("backgroundAlpha"));
				 float _alpha = ValueConverter.getFloat(_cellItem.get("alpha"));

				 if( _cellItem.containsKey( "fontColorInt" ) )
				 {
					 int _fontColor = ValueConverter.getInteger(_cellItem.get("fontColorInt"));
					
					 _cellStyleBuilder.append("color:" + changeColorToRGBA( _fontColor , _alpha )  + ";");			
				 }
				 
				 if( _cellItem.containsKey("fontWeight") )
			     {
			         String _fontWeight = ValueConverter.getString(_cellItem.get("fontWeight"));
			         //System.out.println("htmlComponent::_fontWeight===================================================>>>>>" + _fontWeight);
			         _fontWeight = _fontWeight.equals("true") || _fontWeight.equals("bold")  ? "bold" : "normal";
			 
			         _cellStyleBuilder.append("font-weight:" + _fontWeight + ";"); 
			     }	
				 
				 if( _cellItem.containsKey("fontStyle") )
				 {
					boolean _isItalic = ValueConverter.getBoolean(_cellItem.get("fontStyle"));
					String _fontStyle = _isItalic ? "italic" : "none";
					
					_cellStyleBuilder.append("font-style:" + _fontStyle + ";"); 
				 }
				 
				 if( _cellItem.containsKey("textDecoration") )
				 {
					boolean _isDecoration = ValueConverter.getBoolean(_cellItem.get("textDecoration"));
					String _textDecoration = _isDecoration ? "underline" : "none";
					
					_cellStyleBuilder.append("text-decoration:" + _textDecoration + ";"); 
				 }				 
				
				 String _bgColor = "RGB(255,255,255)";
				 if( _cellItem.containsKey( "backgroundColorInt" ) )
				 {
					int _backColor = ValueConverter.getInteger(_cellItem.get("backgroundColorInt"));
					_bgColor = changeColorToRGBA( _backColor , _alpha);
					_cellStyleBuilder.append("background-color:" + _bgColor  + ";");			
				 }
				 
				 ArrayList<Integer> _lineWs = (ArrayList<Integer>) _cellItem.get("borderWidths"); 
				 int _lineW = 0;
				 if( _cellItem.containsKey("borderSide") )
				 {
					ArrayList<Integer> _borderColors = (ArrayList<Integer>) _cellItem.get("borderColorsInt");
					int _borderColor = ValueConverter.getInteger(_cellItem.get("borderColorInt"));
					ArrayList<String> _borderSides = (ArrayList<String>) _cellItem.get("borderSide");
					ArrayList<String> _types = (ArrayList<String>) _cellItem.get("borderTypes");
					
					String _className = ValueConverter.getString(_cellItem.get("className"));
					
					if( _className.equals("UBTextArea") )
					{
					}
					else
					{
						if( _borderSides.size() == 0 || _borderSides.size() > 4 )	{	}
						else
						{
							_cellStyleBuilder.append("border-side:" + _borderSides.get(3) + " " + _borderSides.get(2) + " " + _borderSides.get(0) + " " + _borderSides.get(1) + ";");
							_cellStyleBuilder.append("border-style:" + getBorderType(_types.get(3)) + " " + getBorderType(_types.get(2)) + " " + getBorderType(_types.get(0)) + " " + getBorderType(_types.get(1)) + ";");
							_cellStyleBuilder.append("border-width:" + (getBorderType(_types.get(3))=="None" ? 0 :_lineWs.get(3)) + "px; " + (getBorderType(_types.get(2))=="None"? 0 :_lineWs.get(2)) + "px; " + (getBorderType(_types.get(0))=="None"? 0 :_lineWs.get(0)) + "px; " + (getBorderType(_types.get(1))=="None"? 0 :_lineWs.get(1)) + "px;");
							_cellStyleBuilder.append("border-color:" + changeColorToRGBA(_borderColors.get(3),_alpha) + " " + changeColorToRGBA(_borderColors.get(2),_alpha) + " " + changeColorToRGBA(_borderColors.get(0),_alpha) + " " + changeColorToRGBA(_borderColors.get(1),_alpha) + ";");
						}
					}
				 }
				 /*
				 if( _cellItem.containsKey("lineHeight") )
				 {
					 float _lineHeight = ValueConverter.getFloat(_cellItem.get( "lineHeight" )) * 100;
					 _cellStyleBuilder.append("line-height:" + _lineHeight  + "%;");		
					 _cellStyleBuilder.append("vertical-align:baseline;");		
					 
				 }
				 */
				 float _padding = 0.5f;
				 _cellStyleBuilder.append("padding:"+_padding+"px;");	
					
				 // top, right, bottom, left 순
			 	//_cellStyleBuilder.append("border-style:solid dotted solid dashed; border-width: 1px 1px 1px 1px; border-color: RGB(255,0,0) RGB(0,255,0) RGB(0,0,255) RGB(0,0,0);");		
				_cellBuilder.append("<TD ColAddr=\"" + _colIndex + "\" ColSpan=\"" + _colSpan + "\" RowAddr=\"" + _rowIndex + "\" RowSpan=\"" + _rowSpan + "\" align=\"" + _hAlign + "\" valign=\"" + _vAlign + "\" style=\"" + _cellStyleBuilder.toString() + "\">\n");
				
				_cellBuilder.append(ParaListMatching_handler( _cellItem ));
				_cellBuilder.append("</TD>\n");
				
				if( _rowSpan != 1 )
				{
					_rowObj.put(_colIndex, _rowSpan + "#" + _colSpan);
				}
				
				_colIndex += _colSpan;
				
			} // for _cells.size();
			_rowBuilder.append(_cellBuilder.toString() + "</TR>\n");
			
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
		
		_tableBuilder.append("<DIV style=\"" + _styleBuilder.toString() + "\">\n");
		_tableBuilder.append("<TABLE style=\"border-collapse:collapse;\" cellspacing=\"0px\" cellpadding=\"0px\">\n");
		_tableBuilder.append(tableHeaderTag_Handler( _item, _zOrder ));
		_tableBuilder.append(_rowBuilder.toString() + "</TABLE>\n");
		_tableBuilder.append("</DIV>\n");

		return _tableBuilder.toString();
	}
		
	private String tableHeaderTag_Handler( HashMap<String, Object> _item, int _zOrder ) 
	{
		StringBuilder _tableHeaderBuilder = new StringBuilder();
		return _tableHeaderBuilder.toString();
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
			Float[] _xoAr = new Float[4];
			Float[] _yoAr = new Float[4];
			
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
				_xoAr[i] = (( _xValue * _cos ) - ( _yValue * _sin ));
				_yoAr[i] = (( _xValue * _sin ) + ( _yValue * _cos ));
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
			
//			_tx = x - _minX; 
//			_ty = y - _minY;
			
//			_cx = (float) Math.floor( _cW / 2 );
//			_cy = (float) Math.floor( _cH / 2 );
			_cx = 0;
			_cy = 0;
			
			_tx = x - _minX;
			_ty = y - _minY;

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
	
	private HashMap<String, Float> rotationPosition( float _x, float _y, Double _rotation )
	{
		
		HashMap<String, Float> _ret = new HashMap<String, Float>();
		
		double _retX = 0;
		double _retY = 0;
		
		_retX = ( _x * Math.cos(_rotation) ) - ( _y * Math.sin(_rotation) );
		_retY = ( _x * Math.sin(_rotation) ) + ( _y * Math.cos(_rotation) );
		
		_ret.put("x", (float) _retX);
		_ret.put("y", (float) _retY);
		
		return _ret;
	}
	
	/**
	 * Floor( Object to String to Double ) to Integer
	 * @param o : numberValue
	 * @return Integer number 
	 * */
	private int toNum( Object o ){
		if( o == null ){
			return -1;
		}
		else if( o instanceof Integer){
			return (Integer) o;
		}
		else{
			String oVal = String.valueOf(o).trim();
			
			if( oVal.equals("") ) return -1;
			
			if( oVal.contains(".") ){
				return (int) Math.floor(Double.parseDouble(oVal));
			}
			else{
				return Integer.valueOf(oVal);
			}
		}
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
	

	
}
