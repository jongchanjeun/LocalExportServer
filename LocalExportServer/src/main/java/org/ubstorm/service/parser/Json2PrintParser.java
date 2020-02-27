package org.ubstorm.service.parser;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubstorm.service.dictionary.ImageDictionary;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.utils.Base64Coder;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Document;

import com.lowagie.text.FontFactory;
import com.lowagie.text.Rectangle;
import com.oreilly.servlet.Base64Decoder;


public class Json2PrintParser {
	private static final Logger log = LoggerFactory.getLogger(Json2PrintParser.class);
	private String mPDF_PRINT_VERSION = "2.0";		//PDF 내보내기시 Version 처리 ( 1.0 기존 버전 / 2.0 폰트 수정 버전 )
	
	private float mPageHeight = 0;
	private float mPageWidth = 0;
	
	private String mWaterMarkTxt = "";
	
	private double mMarginX = 0;
	private double mMarginY = 0;
	
	private double mScale = 1; 
	
	private Font mDefFont;

	
	private ArrayList<String> mFontListAr;
	
	private HashMap<String, ArrayList<HashMap<String, Object>>> mTBborder; 
	
	private String licenseType = "";
	
	private HashMap<String, Object> mChangeItemList;
	
	private ImageDictionary mImageDictionary;
	
	private Graphics2D mg2;
	
	private printCmdMng printCmd;
	
	
	public HashMap<String, Object> getChangeItemList() {
		return mChangeItemList;
	}

	public void setChangeItemList(HashMap<String, Object> value) {
		this.mChangeItemList = value;
	}

	public void init(String warterMark, String licensetype, String pageWidth, String pageHeight, double printScale, double printMarginX, double printMarginY)
	{
		//this.mg2 = g2;
		if("free".equalsIgnoreCase(licensetype))
			this.licenseType = "";	
		else
			this.licenseType = licensetype;	
			
		this.mWaterMarkTxt = warterMark;	
		this.mScale = printScale;
		this.mMarginX = printMarginX;
		this.mMarginY = printMarginY;
		double _pageWidth = Double.parseDouble(pageWidth);
		double _pageHeight =  Double.parseDouble(pageHeight);			
	
			
		mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		// 파라미터의 사이즈값이 존재할경우 맞춰서 width와 height값을 늘리고 mMarginX와 mMarginY값을 세팅
		
		// pdf Page , Document, BaseFont, font 셋팅
		JSONObject _hm = new JSONObject();
		_hm.put("pageWidth", pageWidth);
		_hm.put("pageHeight", pageHeight);
		
		pdfPagePropertySetting( _hm, true, 0 );
		mFontListAr = new ArrayList<String>(FontFactory.getRegisteredFonts());		
	}
	
	
	private void pdfPagePropertySetting(JSONObject _hashData, Boolean usePrint, int _circulation )
	{
		float _pWidth = 974.0f;
		float _pHeight = 1123.0f;
		int _bgColorInt = 0;
		
		_pWidth = convertDpiFloat(_hashData.get("pageWidth"));
		_pHeight = convertDpiFloat(_hashData.get("pageHeight"));
		
		mPageWidth = _pWidth;
		mPageHeight = _pHeight;
		
//		if(mScale < 1)
//		{
//			mMarginX = (_pWidth-(_pWidth*mScale))/2;
//			mMarginY = (_pHeight-(_pHeight*mScale))/2;
//		}
		
		//Color _pageBgColor = new Color(_bgColorInt);
		//_pageSize.setBackgroundColor(Color.WHITE);
		//--- Set the drawing color to WHITE
		//this.mg2.setPaint(Color.WHITE);		
		//mDefFont = new Font(mBaseFont, 10, Font.NORMAL, Color.black);
		mDefFont = new Font("맑은고딕", Font.PLAIN, 12);
	}
	
	private ArrayList<printCmd> cmdList = null;
	public void makePdfPage(Graphics2D g2, JSONObject _pageData, String _pageIndex, int nTimeCalled)
	{
		this.mg2 = g2;
		//float _pWidth = convertDpiFloat(mPageWidth);
		//float _pHeight = convertDpiFloat(mPageHeight);
		
		float _pWidth = mPageWidth;
		float _pHeight = mPageHeight;
		
		ArrayList<HashMap<String, Object>> _itemList = (ArrayList<HashMap<String,Object>>) _pageData.get("pageData");
		ArrayList<HashMap<String, Object>> _itemList2 = new ArrayList<HashMap<String,Object>>();
		
		log.debug("makePdfPage() nTimeCalled=" + nTimeCalled + ", before renderer :: page._itemList.size=" + _itemList.size());
		
		if(_itemList != null)
		{
			if( _itemList.size() <= 0)
			{
				return;
			}			
			// 페이지 변환시 changeList에 값이 있을경우 처리 
			if(mChangeItemList != null && mChangeItemList.size() > 0)
			{
				testPdfChangeMapping(mChangeItemList, _pageData, _pageIndex);
			}
			
			_itemList2.add(_itemList.get(0));
			
			if(nTimeCalled>1){
				if(cmdList == null)	cmdList = printCmd.getCommands();
				for(int i = 0; i<cmdList.size();i++){
					cmdList.get(i).execute(this);
					//log.debug("###################### Command : " +  cmdList.get(i).toString());
				}
				return;
			}else{					
				printCmd = new printCmdMng(this.mg2, 0);	
				cmdList = null;
			}
			
			for (int j = 0; j < _itemList.size(); j++) 
			{
				HashMap<String, Object> _item = _itemList.get(j);
				//JSONObject _item = _itemList.get(j);
				
				String _className = "";
				
				if( mScale < 1 )
				{
//					_item = convertScaleItem(_item, _pWidth, _pHeight );
					_item = convertScaleItem(_item, mMarginX, mMarginY );
				}
				
				if( _item.containsKey("className"))
				{
					_className = (String) _item.get("className");
					
					//log.debug(getClass().getName() + "::className=" + _className);
					
					if( _className.equals("UBLabelBand") ||  _className.equals("UBLabel") || _className.equals("UBLabelBorder") 
						||_className.equals("UBStretchLabel") || _className.equals("UBRotateLabel")  || _className.equals("UBTextInput")
						|| _className.equals("UBTextArea"))
					{  
						
						String _id = ValueConverter.getString(_item.get("id")); 
						
//						if( _id.split("_")[0].equals("TB"))
						if(_item.containsKey("ORIGINAL_TABLE_ID") && _item.get("ORIGINAL_TABLE_ID").equals("") == false )
						{
							
//							String _tbId = _id.substring(0,_id.lastIndexOf("_"));
							String _tbId = _item.get("ORIGINAL_TABLE_ID").toString();
							
							if( mTBborder.size() > 0 )
							{
								if( !mTBborder.containsKey(_tbId) )
								{
									setTBborderSetting();
									mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
								}
							}
						}
						else
						{
							if( mTBborder.size() > 0 )
							{
								setTBborderSetting();
								mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
							}
						}
						
						CreatePdfLabel(_item);
					}
					else if( _className.equals("UBRichTextLabel") )
					{
						
					}
					else if( _className.equals("UBComboBox") || _className.equals("UBDateFiled") )
					{ 
						// 콤보, 데이트필드의 경우 테두리를 그리지 않도록 한다. - 이장환이사님의견 반영.
						_item.put("borderTypes", new ArrayList<String>( Arrays.asList("none","none","none","none")) );
						_item.put("borderWidths", new ArrayList<Integer>( Arrays.asList(0,0,0,0)) );
						_item.put("padding", 3 );
						_item.put("verticalAlign", "middle" );
						_item.put("textAlign", "left" );
						
						CreatePdfLabel(_item );
					}
					else if( _className.equals("UBCheckBox") )
					{
						CreatePdfCheckBox(_item );
					}
					else if( _className.equals("UBRadioBorder") )
					{
						CreatePdfRadioGroup(_item ); // 함수 안에서 수정 더 해야 함.	e-Form
					}
					else if( _className.equals("UBImage") )
					{
						CreatePdfImage(_item );
					}
					else if( _className.equals("UBPicture") )
					{
						CreatePdfSign(_item );
					}
					else if( _className.equals("UBSignature") )
					{
						CreatePdfSign(_item ); // signature 이미지 확인 해야 함.	e-Form
					}
					else if( _className.equals("UBGraphicsLine") )
					{
						CreatePdfLine(_item );
					}
					else if( _className.equals("UBGraphicsCircle") )
					{
						CreatePdfCircle(_item );
					}
					else if( _className.equals("UBGraphicsRectangle") )
					{
						CreatePdfRectangle(_item );
					}
					else if( _className.equals("UBGraphicsGradiantRectangle") )
					{
						CreatePdfGradiantRectangle(_item );
					}
					else if( _className.equals("UBClipArtContainer") )
					{
						CreatePdfClipImage(_item );
					}
					else if( _className.equals("UBSVGArea") )
					{
						CreatePdfSVGArea(_item );
					}
					else if( _className.equals("UBSVGRichText") )
					{
						CreatePdfSVGRichTextLabel(_item );
					}
					else if( _className.equals("UBPresentaionGraphic") ) // 그리기 부분.
					{
						
					}
					else if( _className.equals("UBConnectLine") )
					{
						//CreatePdfConnectLine( _item );
					}
					else if( _className.indexOf("Code") != -1 || _className.indexOf("Chart") != -1  || _className.indexOf("UBTaximeter") != -1 )
					{
						if( _className.equals("UBQRCode") )
						{
							String _svgData = ValueConverter.getString(_item.get("src"));
							_item.put("data", _svgData.substring(4));
							CreatePdfSVGArea( _item );
						}
						else
							CreatePdfDoImage( _item );
					}
					
				}// if _item.containsKey("className")
				
			}// for _itemList.size()
			
//			TestPDF();
			
			// 라이센스
			drawLicense();
			
			//watermark
			drawWaterMark();
			// New Page
			
			if( mTBborder.size() > 0) // 페이지 넘기기전 보더를 안그린게 있으면 그린다.
			{
				setTBborderSetting();
				mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
			}	
			
			// OutOfMemoryError 발생을 방지하기 위해 곧바로 사용한 페이지정보를 지운다.
			//_pageHm.remove(_key);
			try {
				if(Thread.currentThread().interrupted() == false) Thread.currentThread().sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
			
			//this.mg2.dispose();
			log.info("makePdfPage() make " + _pageIndex + " PdfPage ..... end!!!");
			
		}
	}	

	public void drawLicense(){
		//license 그리기
		if(!this.licenseType.equals("")){
					
			int _align = getAlignmentInt("left" );
			String _verticalAlign = "top";
			int _txStyle = fontStyleToInt("normal" ,"normal",  "none");	
			float _fontSize = (float) (mScale < 1 ? 45*mScale : 45);	
			
			Color _txColor =  Color.LIGHT_GRAY;			
			Font trialFont = new Font("tahoma", _txStyle, (int)_fontSize);
					
			this.mg2.setFont(trialFont);		
			this.mg2.setColor(_txColor);			
			
			
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
			this.mg2.setComposite(ac);	
			
//			  Color color = new Color(1, 0, 0, 0.5f); //Red 
//			  this.mg2.setPaint(color);
			  
			//this.mg2.drawString(this.licenseType, 0, 0);	  
			FontMetrics metrics = this.mg2.getFontMetrics(trialFont);	
			this.mg2.drawString( this.licenseType, 10, metrics.getHeight() - metrics.getDescent());
			//drawString(this.mg2, trialFont, this.licenseType, 0, 0, _align, _verticalAlign, mPageWidth, 0 , 0);				
			printCmd.addLicense(this.licenseType, _txColor, trialFont, metrics, ac);
		}
	}
	
	public void execDrawLicense(String _license, Color _color, Font _font , FontMetrics _metrics , AlphaComposite _ac) {  
		this.mg2.setFont(_font);	
		this.mg2.setPaint(_color);
		this.mg2.setComposite(_ac);	
		this.mg2.drawString( _license, 10, _metrics.getHeight() - _metrics.getDescent());
	}
	
	public void drawWaterMark(){
		float _fSize = 65;
		//license 그리기
		if(!this.mWaterMarkTxt.equals("")){
			
			_fSize = getFontSize(_fSize);		
			int _txStyle = fontStyleToInt("normal" ,"normal",  "none");				
			Color _txColor =  Color.LIGHT_GRAY;			
			Font tFont = new Font("tahoma", _txStyle, (int)_fSize);					
			this.mg2.setFont(tFont);		
			this.mg2.setColor(_txColor);
			
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
			this.mg2.setComposite(ac); 
			
			FontMetrics metrics = this.mg2.getFontMetrics(tFont);
			int _strWidth  = metrics.stringWidth(this.mWaterMarkTxt);
			double d = Math.sqrt(2);
			float _box =  (float) (_strWidth/d);	  //기울어진 TEXT의 가로 세로가 동일  	
	    	
	    	int _x = (int) (((mPageWidth  - _box) / 2) + metrics.getHeight());//_totalLineHeight
	    	int _y = (int) (((mPageHeight + _box) / 2))+ metrics.getHeight() + metrics.getDescent();		    	
			
	    	this.mg2.translate(_x,_y);
			this.mg2.rotate(-45 * java.lang.Math.PI/180);	
			this.mg2.drawString(this.mWaterMarkTxt,0,0);
			this.mg2.rotate( 45 * java.lang.Math.PI/180);	
			this.mg2.translate(-_x,-_y);	    
			printCmd.addWaterMark(this.mWaterMarkTxt, _txColor, tFont, _x, _y , ac);			
		}
	}
	
	public void execDrawWaterMark(String _waterMarkTxt, Color _txColor, Font _font, int _x , int _y  ,AlphaComposite _ac) {  
		this.mg2.setFont(_font);		
		this.mg2.setColor(_txColor);
		this.mg2.setComposite(_ac);	
		this.mg2.translate(_x,_y);
		this.mg2.rotate(-45 * java.lang.Math.PI/180);	
		this.mg2.drawString(_waterMarkTxt,0,0);
		this.mg2.rotate( 45 * java.lang.Math.PI/180);	
		this.mg2.translate(-_x,-_y);	  
	}
	
	
	 private float getFontSize(float _fSize)
     {
     	
     	float _gap = mWaterMarkTxt.length() * (_fSize / 2);
     	
     	double result = Math.pow(mPageWidth,2)+Math.pow((mPageHeight/2f),2);
     	
     	double _value = Math.sqrt(result);
     	
     	
     	
     	if( _value < _gap )
     	{
     		float _a = (Math.round((_gap / _value)*1000f) / 1000f);
     		
     		float _size = (Math.round((_fSize / _a) * 100f ) / 100f);
     		
     		return _size;
     	}
     	else
     	{
     		return 65f;
     	}
     }
	
	private float ObjectToFloat(Object value)
	{
		return convertDpiFloat(value);
	}
	
	private float convertDpiFloat( Object value)
	{
		return (Math.round(((ValueConverter.getFloat(value) / 96f ) * 72f ) * 10f))/10f;
	}
	
	
	
	private int getAlignmentInt( String _align )
	{
		int _aInt = 0;
		
		if( _align.equals("left") )
		{
			_aInt = 0;
		}
		else if( _align.equals("center") )
		{
			_aInt = 1;
		}
		else if( _align.equals("right") )
		{
			_aInt = 2;
		}
		else
		{
			_aInt = -1;
		}
		
		return _aInt;
	}
	
	
	
	
	private Integer fontStyleToInt( Object _italic , Object _bold , Object _underLine)
	{
		int _style = 0;
		
		if( !_underLine.equals("none"))
		{
			//_style += Font.UNDERLINE;
		}
		
		if( !_italic.equals("normal"))
		{
			_style += Font.ITALIC;
		}
		
		if( !_bold.equals("normal"))
		{
			_style += Font.BOLD;
		}
		
		return _style;
	}
	
	
	private String getDefFont(String _fName)
	{
		String _rtnFontName = _fName;
		int _size = mFontListAr.size();
		
		for (int i = 0; i < _size ; i++) 
		{
			String n1 = mFontListAr.get(i).toLowerCase();
			String n2 = _fName.toLowerCase();
			
			if( n1.equals(n2))
			{
				_rtnFontName = mFontListAr.get(i);
				break;
			}
		}
		
		return _rtnFontName;
	}
	
	
	/**
	 * 
	 * @param colorStr e.g. "#FFFFFF" or "0xFFFFFF"
	 * @return 
	 */
	private Color hex2Rgb(String colorStr) {
		
		String tmpColorStr = "0x".equals(colorStr.substring(0, 2)) ? colorStr.substring(1) : colorStr;	
		
	    return new Color(
	            Integer.valueOf( tmpColorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( tmpColorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( tmpColorStr.substring( 5, 7 ), 16 ) );
	}	
	
	@SuppressWarnings("unchecked")
	private void CreatePdfLabel(HashMap<String, Object> _item )
	{
		boolean _verticalRotateFlg = false;
		float _x = ObjectToFloat( _item.get("x") );
		//float _y = mPageHeight - ObjectToFloat( _item.get("y") );
		float _y = ObjectToFloat( _item.get("y") );
		float _w = ObjectToFloat( ValueConverter.getFloat(_item.get("x")) + ValueConverter.getFloat(_item.get("width")) );
		//float _h = mPageHeight - ObjectToFloat( ValueConverter.getFloat(_item.get("y")) + ValueConverter.getFloat(_item.get("height")) );
		float _h = ObjectToFloat( ValueConverter.getFloat(_item.get("y")) + ValueConverter.getFloat(_item.get("height")) );
		float _padding =  ValueConverter.getFloat(_item.get("padding"));
		
		
		int _width = Math.round(ObjectToFloat( ValueConverter.getFloat(_item.get("width"))));		
		int _height = Math.round( ObjectToFloat( ValueConverter.getFloat(_item.get("height"))));
		int _xVal = Math.round(ObjectToFloat( ValueConverter.getFloat(_item.get("x"))));		
		int _yVal = Math.round( ObjectToFloat( ValueConverter.getFloat(_item.get("y"))));
		
		
		String _id = ValueConverter.getString(_item.get("id"));
		String _type = ValueConverter.getString(_item.get("type"));
		
		
		// background 
		float _bgAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		//Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));		
		float _alpha = _item.get("alpha") == null ? 1:  ValueConverter.getFloat(_item.get("alpha"));
		float _itemAlpha = _bgAlpha*_alpha;
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));		
		//Color _alphaColor = new Color(_bgColor.getRed()/255 , _bgColor.getGreen()/255 , _bgColor.getBlue()/255,(_bgAlpha*_alpha));
		
		// border
		HashMap<String, Float> _rect = new HashMap<String, Float>();
		ArrayList<HashMap<String, Object>> _content;
		
		HashMap<String, Float> _rotateDef = new HashMap<String, Float>();
		
		
		this.mg2.setColor(_bgColor);		
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _itemAlpha);
		this.mg2.setComposite(ac);		
		this.mg2.fillRect(_xVal, _yVal, _width, _height);		
		
		printCmd.addBgFill(_bgColor, ac, _xVal, _yVal, _width, _height);
		
		
		// text		
		int _align = getAlignmentInt( ValueConverter.getString(_item.get("textAlign")) );
		String _verticalAlign = ValueConverter.getString(_item.get("verticalAlign"));
		int _txStyle = fontStyleToInt(_item.get("fontStyle"), _item.get("fontWeight"), _item.get("textDecoration"));
//		int _txStyle = fontStyleToInt(_item.get("fontStyle"), "normal", "none");
//		float _fontSize = 11f;
		
		float _fontSize = ObjectToFloat(_item.get("fontSize"))*0.9f;
		float _lineHeight = 1.2f;
		if(_item.containsKey("lineHeight")){
			 _lineHeight = Float.valueOf(_item.get("lineHeight").toString());
		}
		
		
		
		if(mPDF_PRINT_VERSION.equals( GlobalVariableData.UB_PDF_PRINT_VERSION_1 ) )
		{
			if( Float.valueOf(_item.get("fontSize").toString()) <= 12 )_fontSize =  Double.valueOf( Math.ceil( _fontSize ) ).floatValue();
		}
		else
		{
			// PontSize를 PT사이즈로 지정
			_fontSize = Double.valueOf(Float.valueOf(_item.get("fontSize").toString()) * 72 /  96 ).floatValue();
		}		
		
		Color _txColor = new Color(ValueConverter.getInteger(_item.get("fontColorInt")));
		//Color _txColor = hex2Rgb(ValueConverter.getString(_item.get("fontColor")));
		String _fontName = ValueConverter.getString(_item.get("fontFamily"));
		String _fName = getDefFont(_fontName);
//		log.info(getClass().getName() + "CreatePdfLabel ===> " + "_fontName : " + _fontName + ", _fName : " + _fName);

		this.mg2.setColor(_txColor);
		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);
		this.mg2.setComposite(ac);		
		
		if( _fName.equals("") )
		{
			mDefFont = new Font("맑은고딕", _txStyle, (int)_fontSize);
		}
		else
		{
			mDefFont = new Font(_fName, _txStyle, (int)_fontSize);
		}		
		
		this.mg2.setFont(mDefFont);		
		
		String _itemText = ValueConverter.getString(_item.get("text"));		
		if(_type.equals("stretchLabel"))
		{
	        HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
	         
	        optionMap.put("width", new float[]{ObjectToFloat(_item.get("width"))});
	        optionMap.put("height",new float[]{ mPageHeight , mPageHeight });
	        optionMap.put("fontSize", new float[]{_fontSize});
	        optionMap.put("lineHeight",new float[]{_lineHeight});
	         
	        if( _item.containsKey("padding") == false ){
	           optionMap.put("padding", new float[]{ 3 });
	        }else{
	           optionMap.put("padding", new float[]{ObjectToFloat( _item.get("padding"))});
	        }
	         
	        HashMap<String,Object> resultMap = StringUtil.getSplitCharacter(_itemText, optionMap ,_item.get("fontWeight").toString(),_item.get("fontFamily").toString(),  ObjectToFloat(_item.get("fontSize")), -1 );
	        ArrayList<Float> _resultHeightAr = (ArrayList<Float>) resultMap.get("Height");	          
	       
	        float _txh = _resultHeightAr.get(0);
	          
	        if( _txh > 0f)
	        {
	           _h = _txh;	          
	        }
	        if( _txh > 5f && _txh < 10f )
	        {
	           _y = 2f;
	        }
	          
	    }
	    else if ( _type.equals("rotateLabel"))
	    {
	       if( !_item.get("textRotate").equals(0))
	       {
//	    	   at = new AffineTransform();
//	    	   at.setToRotation(Math.PI/2.0);
//	    	   this.mg2.setTransform(at);
	       }
	    }
				
		
		// 줄바꿈 처리
		_itemText = _itemText.replace("\\n", "\n").replaceAll("\\r", "\r");
		String txtDeco = _item.get("textDecoration").toString();		
       if(_type.equals("rotateLabel")  && !_item.get("textRotate").equals("0") && !_item.get("textRotate").equals("360")){
		      
//          _verticalRotateFlg = true;
//                  
    	   int _rotate = Integer.parseInt(_item.get("textRotate").toString());
//    	   double theta = _rotate * java.lang.Math.PI/180;    	   
//    	   AffineTransform fontAT = new AffineTransform();
//	       // get the current font
//	       Font theFont = this.mg2.getFont();	      
//	       // Derive a new font using a rotatation transform
//	       fontAT.rotate(theta);
//	       Font theDerivedFont = theFont.deriveFont(fontAT);
//	       this.mg2.setFont(theDerivedFont);
	       
	       // Render a string using the derived font

	       drawRotateString(this.mg2, mDefFont, _itemText, _x, _y, _align, _verticalAlign, 
		   			ObjectToFloat(ValueConverter.getFloat(_item.get("width"))), ObjectToFloat(ValueConverter.getFloat(_item.get("height"))) , _padding, _rotate, ac ,_txColor,txtDeco,_lineHeight);
    	   
       }else{
    	   drawString(this.mg2, mDefFont, _itemText,_x, _y, _align, _verticalAlign, 
   				ObjectToFloat(ValueConverter.getFloat(_item.get("width"))), ObjectToFloat(ValueConverter.getFloat(_item.get("height"))) , _padding, ac ,_txColor,txtDeco,_lineHeight);
   		
       }
		
		_rect.put("top", _y);
		_rect.put("left", _x);
		_rect.put("right", _w);
		_rect.put("bottom", _h);
		
		if(_item.containsKey("ORIGINAL_TABLE_ID") && _item.get("ORIGINAL_TABLE_ID").equals("") == false )
		{
//			String _tbId = _id.substring(0,_id.lastIndexOf("_"));
			String _tbId = _item.get("ORIGINAL_TABLE_ID").toString();
			if( !mTBborder.containsKey(_tbId))
			{
				_content = new ArrayList<HashMap<String,Object>>();
			}
			else
			{
				_content = mTBborder.get(_tbId);
			}
			
			mTBborder.put(_tbId, tbBorderSetting((ArrayList<String>) _item.get("borderSide") , (ArrayList<String>) _item.get("borderTypes") , (ArrayList<Integer>) _item.get("borderColorsInt") , (ArrayList<Integer>) _item.get("borderWidths") , _rect, _content , _alpha,(ArrayList<String>) _item.get("borderOriginalTypes"),(ArrayList<Boolean>) _item.get("beforeBorderType")  ));
		}
		else
		{
			setBorderSetting((ArrayList<String>) _item.get("borderSide") , (ArrayList<String>) _item.get("borderTypes") , (ArrayList<String>) _item.get("borderColors") , (ArrayList<Long>) _item.get("borderWidths") , _rect , _alpha );
		}	
	}

	
	public void execDrawBgFill( Color _bgColor, AlphaComposite _ac , int _xVal, int _yVal, int _width, int _height) {  
		this.mg2.setColor(_bgColor);	
		this.mg2.setComposite(_ac);		
		this.mg2.fillRect(_xVal, _yVal, _width, _height);	
	}
	
	
	private HashMap<String, Float> rotationItem( float _x , float _y , float _w , float _h , float _rotation )
	{
		HashMap<String, Float> _retHm = new HashMap<String, Float>();
		double pi = _rotation * Math.PI / 180;
		float cos = (float) Math.cos(pi);
		float sin = (float) Math.sin(pi);
		
		
		double pi2 = pi * -1;
		
		float _chkX = 0;
		float _chkY = 0;
		
		float _centerX = _x + ( (_w - _x) /2);
		float _centerY = _y - ( (_y - _h) /2 );
		float _wNum = ( (_w - _x) /2 ); 
		float _hNum = ( (_y - _h) /2 );
		
		if ( Math.abs(_rotation) == 90 || Math.abs(_rotation) == 270 )
		{
			_wNum =  ( (_y - _h) /2 );
			_hNum =  ( (_w - _x) /2 );	
		}
		
		HashMap<String, Float> _centerPositionR = rotationPosition( _centerX, _centerY, pi2 );
		
		float _moveX = _centerPositionR.get("x");
		float _moveY = _centerPositionR.get("y");
		
		
		_retHm.put("x", _moveX - _wNum);
		_retHm.put("y", _moveY - _hNum);
		_retHm.put("w", _moveX + _wNum);
		_retHm.put("h", _moveY + _hNum);

		return _retHm;
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
	
	private void drawString(Graphics2D g2, Font font, String text, float x, float y, int align, String vAlign, float itemWidth, float itemHeight,  float padding,  AlphaComposite _fontAc , Color _fontColor,String txtDeco,float lineHeight) {
        
		float _padding =  ObjectToFloat(0.5f);
		if(padding != 0){
			_padding = _padding + ObjectToFloat(padding);
		}
		
		ArrayList<AttributedCharacterIterator> texts   = new ArrayList<AttributedCharacterIterator>();
		ArrayList<Float> transXs   = new ArrayList<Float>();
		ArrayList<Float> transYs  = new ArrayList<Float>();		
		float _transX = 0;
		float _transY = 0;
				
		FontMetrics metrics = g2.getFontMetrics(font);	    
	    
		float _x = x;
		float _y = y;
	    int _strWidth = 0;
	    int _lineHeight = metrics.getHeight();   
	    int _yTemp = 0;
	    
	    text = StringUtil.wrapLineInto(text,metrics, Math.round(itemWidth- (_padding*2)));	   
	    
	    String [] arrLine = text.split("\n");	
	    int _totalLineHeight   =  arrLine.length * _lineHeight;   
	    int tempLineHeight = 0;
	     if(arrLine.length >1){
	    	 for (int i = 1; i<=arrLine.length;i++){				    	
	    		 //Item 사이즈보다 Text 높이가 큰경우   line 버림
	    		 tempLineHeight = 	i * (metrics.getAscent() + metrics.getDescent());
	    		 if(itemHeight < tempLineHeight ){	
	    			 
	    			 break;
	    		 }
	    		 _totalLineHeight  =  i * metrics.getHeight();
	    	 }	    	 
	     }
		 
	    int topSpace = 0;
	    if("middle".equals(vAlign))
		{
	    	if(((itemHeight - _totalLineHeight) / 2) < 1){
	    		topSpace = Math.round(_padding/2);
	    	}
			// Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
	    	_y = (int) (y + ((itemHeight - _totalLineHeight) / 2) + metrics.getAscent() + topSpace);	    	
		}else if("top".equals(vAlign)){				
			_y = (int) (y +  metrics.getAscent() + _padding );
			
		}else if("bottom".equals(vAlign)){	
			_y = (int) (y + itemHeight - _totalLineHeight  - _padding  +  metrics.getAscent() +  metrics.getLeading());
		}
	    
	    String _line = "";    
	    
	    g2.setFont(font); 
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    	        RenderingHints.VALUE_ANTIALIAS_ON);
    	g2.setRenderingHint(RenderingHints.KEY_RENDERING,
    	        RenderingHints.VALUE_RENDER_QUALITY);
    	AttributedString as = null;
    	
    	float _lineSpacing = (float) ((metrics.getHeight() * (lineHeight-1)) * 0.9666);
    	
	    for (int i = 0; i<arrLine.length;i++){	 
	    	_line = arrLine[i];
	    	//Item 사이즈보다 Text 높이가 큰경우   line 버림
	    	if(arrLine.length>1){
	    		if(itemHeight < ((i+1)*(metrics.getAscent() + metrics.getDescent())) ){
		    		break;
		    	}
	    	}
	    	_strWidth = metrics.stringWidth(_line);	    	
	    	
	    	if(align == 1)	// center
			{				
				_x = (int) (x + ( itemWidth/2 - _strWidth/2 ));
			}
			else if(align == 2) // right
			{
				_x = (int) (x + itemWidth - _strWidth - _padding );
				
			}else{	// left
				
				_x = (int)(x + _padding);					
			}	    	
	    	
	    	_transX = _x; 
	    	_transY = _y +_yTemp; 
	    	 as = new AttributedString(_line);
	    	 if(_line.length()>0){
	    		 as.addAttribute(TextAttribute.FONT, font);
		    	 if( !txtDeco.equals("none")){				 
					 as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);	
				 }	
		    	 g2.drawString(as.getIterator(), _transX, _transY);	
		    	 texts.add(as.getIterator());
		    	 transXs.add(_transX);
		    	 transYs.add(_transY);	
	    	 }	   
			_yTemp +=  metrics.getHeight()+_lineSpacing; 
			
	    }	    
	    printCmd.addLabelString(texts, transXs, transYs, font , _fontAc, _fontColor);
    }
	
	//label String cmd 실행 
	public void execDrawString(ArrayList<AttributedCharacterIterator> texts  , ArrayList<Float> transX , ArrayList<Float> transY ,Font font,  AlphaComposite _fontAc , Color _fontColor) {  
		this.mg2.setFont(font); 
		this.mg2.setColor(_fontColor);
		this.mg2.setComposite(_fontAc);	
		this.mg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_OFF);
		this.mg2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		this.mg2.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);		
		this.mg2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		for(int i = 0; i<texts.size();i++){					
			this.mg2.drawString(texts.get(i), transX.get(i),transY.get(i));	  			
		}
	}
	
	
	private void drawRotateString(Graphics2D g2, Font font, String text, float x, float y, int align, String vAlign, float itemWidth, float itemHeight ,  float padding ,int rotate
			, AlphaComposite _fontAc , Color _fontColor ,String txtDeco, float lineHeight) {
		AttributedString as = null;
		ArrayList<AttributedCharacterIterator> texts   = new ArrayList<AttributedCharacterIterator>();
		ArrayList<Float> transXs   = new ArrayList<Float>();
		ArrayList<Float> transYs  = new ArrayList<Float>();		
		float _transX = 0;
		float _transY = 0;
		double _rotation = 0;
		
		int _itemWidth = Math.round(itemWidth);
    	int _itemHeight = Math.round(itemHeight);
    	double d = Math.sqrt(2);
    	// Get the FontMetrics
		if(rotate == 90 || rotate == 270 ){
			_itemWidth = Math.round(itemHeight);
			_itemHeight = Math.round(itemWidth);
		}else{			
			//_itemWidth = (int) (d * itemHeight);
		}
	    FontMetrics metrics = g2.getFontMetrics(font);
	    text = StringUtil.wrapLineInto(text,metrics, Math.round(_itemWidth- (padding*2)));
	    g2.setFont(font);	
	    float _x = x;
	    float _y = y;
	    int _strWidth = 0;
	    int _lineHeight = metrics.getHeight();
	    int _yTemp = 0;
	    
	    String [] arrLine = text.split("\n");
	    int _totalLineHeight   = arrLine.length * _lineHeight;	    
	    String _line = "";
	    _rotation = rotate * java.lang.Math.PI/180;
	    float _lineSpacing = (float) ((metrics.getHeight() * (lineHeight-1)) * 0.9666);
	    if(rotate ==  90){
	    	if("middle".equals(vAlign))
			{
				// Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
				_x = (int) ((x+_itemHeight) - (_itemHeight / 2 -_totalLineHeight/2) - metrics.getAscent());
			}else if("top".equals(vAlign)){
				_x = (int) (x + _itemHeight  - metrics.getAscent());
			}else if("bottom".equals(vAlign)){				
				_x = (int) (x + _totalLineHeight  -   metrics.getAscent());
			}	    	
	    	
	    	for (int i = 0; i<arrLine.length;i++){
		    	_line = arrLine[i];
		    	_strWidth = metrics.stringWidth(_line);	    	
		    	
		    	if(align == 1)	// center
				{
					_y = (int) (y + ( _itemWidth/2 - _strWidth/2 ));
				}
				else if(align == 2) // right
				{
					_y = (int) (y + _itemWidth - _strWidth - padding );
				}else{	// left
					_y = (int)(y + padding);
				}
		    	_transX = _x -_yTemp;
		    	_transY = _y;
		    	
		    	 as = new AttributedString(_line);
		    	 as.addAttribute(TextAttribute.FONT, font);
		    	 if( !txtDeco.equals("none")){				 
					 as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);	
				 }	    
		    	 
		    	g2.translate(_transX,_transY);
		    	g2.rotate(_rotation);			       
		    	g2.drawString(as.getIterator(),0,0);
		    	g2.rotate(-_rotation);
		    	g2.translate(-_transX,-_transY);			    	
		    	
		    	texts.add(as.getIterator());
		    	transXs.add(_transX);
		    	transYs.add(_transY);
		    	
		    	//g2.drawString(_line, _x -_yTemp, _y);	
				_yTemp +=_lineHeight + _lineSpacing; 
		    }
	    }else if(rotate == 270){	    	
	    	if("middle".equals(vAlign))
			{				
				_x = (int) (x + (_itemWidth / 2 -_totalLineHeight/2) + metrics.getAscent());
			}else if("top".equals(vAlign)){
				_x = (int) (x + metrics.getAscent());
			}else if("bottom".equals(vAlign)){	
				_x = (int) (x + _itemHeight - _totalLineHeight+ metrics.getAscent());
			}	
	    	
	    	for (int i = 0; i<arrLine.length;i++){
		    	_line = arrLine[i];
		    	_strWidth = metrics.stringWidth(_line);	    	
		    	
		    	if(align == 1)	// center
				{
					_y = (int) ((y + _itemHeight) - ( _itemHeight/2 - _strWidth/2 )) - padding;
				}
				else if(align == 2) // right
				{
					_y = (int)(y + padding);
				}else{	// left
					
					_y = (int) (y + _itemWidth - _strWidth - padding );
				}
		    	
		    	_transX = _x +_yTemp;
		    	_transY = _y;
		    	
		    	as = new AttributedString(_line);
		    	 as.addAttribute(TextAttribute.FONT, font);
		    	 if( !txtDeco.equals("none")){				 
					 as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);	
				 }	    
		    	 
		    	g2.translate(_transX,_transY);
		    	g2.rotate(_rotation);			       
		    	g2.drawString(as.getIterator(),0,0);
		    	g2.rotate(-_rotation);
		    	g2.translate(-_transX,-_transY);			    	
		    	
		    	texts.add(as.getIterator());
		    	transXs.add(_transX);
		    	transYs.add(_transY);
		    	
		    	//g2.drawString(_line, _x +_yTemp, _y);	
				_yTemp +=_lineHeight + _lineSpacing; 
		    }
	    }else if(rotate == 180){	    	
	    	if("middle".equals(vAlign))
			{
		    	_y = (int) ((y + itemHeight) - (itemHeight / 2 -_totalLineHeight/2) - metrics.getAscent());
		    	
			}else if("top".equals(vAlign)){				
				_y = (int) (y + itemHeight -  metrics.getAscent());						
			}else if("bottom".equals(vAlign)){			
				_y = (int) (y + _totalLineHeight -   metrics.getAscent());				
			}
	    	
	    	for (int i = 0; i<arrLine.length;i++){
		    	_line = arrLine[i];
		    	_strWidth = metrics.stringWidth(_line);	    	
		    	
		    	if(align == 1)	// center
				{
		    		_x = (int)((x + itemWidth) - ( itemWidth/2 - _strWidth/2 ));
				}
				else if(align == 2) // right
				{
					_x = (int)(x + _strWidth + padding);
				}else{	// left
					
					_x = (int) (x + itemWidth - padding );			
				}
		    	
		    	_transX = _x;
		    	_transY = _y - _yTemp;
		    	
		    	as = new AttributedString(_line);
		    	 as.addAttribute(TextAttribute.FONT, font);
		    	 if( !txtDeco.equals("none")){				 
					 as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);	
				 }	    
		    	 
		    	g2.translate(_transX,_transY);
		    	g2.rotate(_rotation);			       
		    	g2.drawString(as.getIterator(),0,0);
		    	g2.rotate(-_rotation);
		    	g2.translate(-_transX,-_transY);			    	
		    	
		    	texts.add(as.getIterator());		    	
		    	transXs.add(_transX);
		    	transYs.add(_transY);
		    	
		    	//g2.drawString(_line, _x , _y - _yTemp);	
				_yTemp +=_lineHeight + _lineSpacing; 
		    }
	    }else if(rotate == 45){	    
	    	
	    	//일단 한줄짜리만 고려	    	    	
	    	for (int i = 0; i<arrLine.length;i++){
		    	_line = arrLine[i];
		    	_strWidth = metrics.stringWidth(_line);			    	
		    	float _box =  (float) (_strWidth/d);	  //기울어진 TEXT의 가로 세로가 동일  	
		    	
		    	_x = (int) (x + ((itemWidth  - _box) / 2));//_totalLineHeight
		    	_y = (int) (y + ((itemHeight - _box) / 2) + padding);		    	
		    	
		    	_transX = _x - _yTemp;
		    	_transY = _y;
		    	
		    	as = new AttributedString(_line);
		    	 as.addAttribute(TextAttribute.FONT, font);
		    	 if( !txtDeco.equals("none")){				 
					 as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);	
				 }	    
		    	 
		    	g2.translate(_transX,_transY);
		    	g2.rotate(_rotation);			       
		    	g2.drawString(as.getIterator(),0,0);
		    	g2.rotate(-_rotation);
		    	g2.translate(-_transX,-_transY);			    	
		    	
		    	texts.add(as.getIterator());
		    	transXs.add(_transX);
		    	transYs.add(_transY);	    	
		    	
		    	//g2.drawString(_line, _x , _y - _yTemp);	
		    	_yTemp +=_lineHeight + _lineSpacing; 
		    }
	    }else{
	    	int tempRotate = rotate;
	    	//일단 한줄짜리만 고려	    	    	
	    	for (int i = 0; i<arrLine.length;i++){
		    	_line = arrLine[i];
		    	_strWidth = metrics.stringWidth(_line);	
		    	
		    	double _lineW = 0;//_lineHeight/Math.sin(Math.toRadians(rotate));
		    	double _lineH = 0;//_lineHeight * Math.cos(Math.toRadians(rotate));   	
		    	if(rotate < 90){
		    		_lineW = _strWidth * Math.cos(Math.toRadians((rotate)));   	
		    		_lineH = _strWidth * Math.sin(Math.toRadians((rotate)));
		    		
		    		_x = (int) (x + ((itemWidth  - _lineW) / 2)) - (int)(metrics.getAscent()/2 * Math.cos(Math.toRadians((90-rotate))));//_totalLineHeight
			    	_y = (int) (y + ((itemHeight - _lineH) / 2)) + (int)(metrics.getAscent()/2 * Math.sin(Math.toRadians((90-rotate))));
			    	
		    	}else if(rotate < 180){
		    		tempRotate = 180-rotate;
		    		
		    		_lineW = _strWidth * Math.cos(Math.toRadians(tempRotate));   	
		    		_lineH = _strWidth * Math.sin(Math.toRadians(tempRotate));
		    		
		    		_x = (int) ((x + itemWidth) - ((itemWidth  - _lineW) / 2)) - (int)(metrics.getAscent()/2 * Math.cos(Math.toRadians((90-tempRotate))));//_totalLineHeight
			    	_y = (int) (y + ((itemHeight - _lineH) / 2))- padding;	
		    		
		    		
		    	}else if(rotate < 270){
		    		
		    		tempRotate =  rotate - 180;
		    		
		    		_lineW = _strWidth * Math.cos(Math.toRadians(tempRotate));   	
		    		_lineH = _strWidth * Math.sin(Math.toRadians(tempRotate));
		    		
		    		_x = (int) ((x + itemWidth) - ((itemWidth  - _lineW) / 2)) + (int)(metrics.getAscent()/2 * Math.cos(Math.toRadians((90-tempRotate))));//_totalLineHeight
			    	_y = (int) ((y + itemHeight) - ((itemHeight - _lineH) / 2)) - (int)(metrics.getAscent()/2 * Math.sin(Math.toRadians((90-tempRotate))));		    		
		    	}else{
		    		
		    		tempRotate =  360 - rotate;
		    		
		    		_lineW = _strWidth * Math.cos(Math.toRadians(tempRotate));   	
		    		_lineH = _strWidth * Math.sin(Math.toRadians(tempRotate));
		    		
		    		_x = (int)(x + ((itemWidth  - _lineW) / 2)) + (int)(metrics.getAscent()/2 * Math.cos(Math.toRadians((90-tempRotate))));//_totalLineHeight
			    	_y = (int) ((y + itemHeight) - ((itemHeight - _lineH) / 2)) + (int)(metrics.getAscent()/2 * Math.sin(Math.toRadians((90-tempRotate))));		
		    	}
		    	
		    	
		    	
		    	//float _box =  (float) (_strWidth/d);	  //기울어진 TEXT의 가로 세로가 동일  	
		    	
		    			    	
		    	
		    	_transX = _x - _yTemp;
		    	_transY = _y;
		    	
//		    	if(rotate>45 && rotate < 90){
//		    		_transY =_transY + (int)(_lineW * Math.tan(Math.toRadians((90-rotate))));
//		    	}else if(rotate<45){
//		    		_transY =_transY + (int)(_lineW * Math.tan(Math.toRadians(rotate)));
//		    	}
		    	
		    	
		    	as = new AttributedString(_line);
		    	as.addAttribute(TextAttribute.FONT, font);
		    	if( !txtDeco.equals("none")){
		    		as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);	
				}	
		    	
//		    	g2.translate(x,y);
//		    	g2.rotate(_rotation);			       
//		    	g2.drawString(as.getIterator(),0,0);
//		    	g2.rotate(-_rotation);
//		    	g2.translate(-x,-y);
//		    	transXs.add(x);
//		    	transYs.add(y);
		    	
		    	g2.translate(_transX,_transY);
		    	g2.rotate(_rotation);			       
		    	g2.drawString(as.getIterator(),0,0);
		    	g2.rotate(-_rotation);
		    	g2.translate(-_transX,-_transY);			    	
		    	transXs.add(_transX);
		    	transYs.add(_transY);		    	
		    	texts.add(as.getIterator());
		    	
		    	//g2.drawString(_line, _x , _y - _yTemp);	
		    	_yTemp +=_lineHeight +_lineSpacing; 
		    }
	    }
	   printCmd.addRotateLabelString(texts, transXs, transYs ,font ,_rotation,_fontAc,_fontColor );
    }
	
	//rotation label String cmd 실행 
	public void execDrawRotateString(ArrayList<AttributedCharacterIterator> texts  , ArrayList<Float> transX , ArrayList<Float> transY ,Font font , Double rotation,  AlphaComposite _fontAc , Color _fontColor) {  
		this.mg2.setFont(font);
		this.mg2.setColor(_fontColor);
		this.mg2.setComposite(_fontAc);	
		this.mg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    	        RenderingHints.VALUE_ANTIALIAS_ON);
		this.mg2.setRenderingHint(RenderingHints.KEY_RENDERING,
    	        RenderingHints.VALUE_RENDER_QUALITY);
		for(int i = 0; i<texts.size();i++){			
			this.mg2.translate(transX.get(i),transY.get(i));
			this.mg2.rotate(rotation);			       
			this.mg2.drawString(texts.get(i),0,0);
			this.mg2.rotate(-rotation);
			this.mg2.translate(-transX.get(i),-transY.get(i));	  		
		}
	}
	
	private void setBorderSetting(ArrayList<String> _sides , ArrayList<String> _types , ArrayList<String> _colors , ArrayList<Long> _widths , HashMap<String, Float> _rect )
	{
		setBorderSetting( _sides , _types , _colors , _widths , _rect, 1.0f);
	}
	
	private void setBorderSetting(ArrayList<String> _sides , ArrayList<String> _types , ArrayList<String> _colors , ArrayList<Long> _widths , HashMap<String, Float> _rect, float _alpha )
	{
		int _sLength = _sides.size();
		Line2D.Float line = null;		
		
		ArrayList<Color> colors = new ArrayList<Color>();
		ArrayList<Line2D.Float> lines = new ArrayList<Line2D.Float>();
		ArrayList<Stroke> strokes = new ArrayList<Stroke>();
		ArrayList<AlphaComposite> acs = new ArrayList<AlphaComposite>();
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);
		float _left = _rect.get("left");
		float _top = _rect.get("top");
		float _right = _rect.get("right");
		float _bottom = _rect.get("bottom");
		
		for (int i = 0; i < _sLength; i++) 
		{
			String _side = _sides.get(i);
			String _type = _types.get(i);
			
			Color color = hex2Rgb(ValueConverter.getString(_colors.get(i)));
			
			int _width = _widths.get(i).intValue();
			
			//PDF 저장시 라벨의 Border Width와 table의 borderWidth 값의 차이가 나서 수정 2017-02-20 최명진
			float _widthF = ObjectToFloat(_width);
//			float _widthF = ValueConverter.getFloat(_hm.get("width"));
			_widthF = _widthF - 0.3f;
			
			if( _type.equals("none"))
			{
				continue;
			}
			
			this.mg2.setColor(color);
			// creates a solid stroke with line width
			Stroke stroke = new BasicStroke(_widthF);
			this.mg2.setStroke(stroke);			
			
			if( _side.equals("top"))
			{
				line = new Line2D.Float(_rect.get("left"),  _rect.get("top"), _rect.get("right"),  _rect.get("top"));
				this.mg2.draw(line);
			}
			else if( _side.equals("left"))
			{
				line = new Line2D.Float(_rect.get("left"),  _rect.get("top"), _rect.get("left"),  _rect.get("bottom"));
				this.mg2.draw(line);				
			}
			else if(_side.equals("bottom"))
			{
				line = new Line2D.Float(_rect.get("left"),  _rect.get("bottom"), _rect.get("right"),  _rect.get("bottom"));
				this.mg2.draw(line);				
			}
			else if( _side.equals("right"))
			{
				line = new Line2D.Float(_rect.get("right"),  _rect.get("top"), _rect.get("right"),  _rect.get("bottom"));
				this.mg2.draw(line);				
			}
			if(line != null){
				lines.add(line);
				colors.add(color);
				strokes.add( stroke);	
				acs.add(ac);
			}
		}
		if(lines.size()>0){
			printCmd.addBorder(colors, strokes, lines ,acs);
		}		
	}	
	
	public void execDrawBorder(ArrayList<Color> colors ,ArrayList<Stroke> strokes , ArrayList<Line2D.Float> lines, ArrayList<AlphaComposite> acs) {  
		
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		rh.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		rh.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		this.mg2.setRenderingHints(rh);
		this.mg2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		this.mg2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		this.mg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

		for(int i = 0; i<lines.size();i++){		
				this.mg2.setComposite(acs.get(i));
				this.mg2.setColor(colors.get(i));
				this.mg2.setStroke(strokes.get(i));
				this.mg2.draw(lines.get(i));				
		}
	}
	
		
	private int mCNum = 0;
	private void CreatePdfCheckBox( HashMap<String, Object> _item )
	{
		
		float _x = ObjectToFloat(_item.get("x"));
		float _y = ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		// background 
		float _bgAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));	
				
		HashMap<String, Float> _rHm = new HashMap<String, Float>();
		_rHm.put("top", _y);
		_rHm.put("left", _x);
		_rHm.put("right", _w);
		_rHm.put("bottom", _h);			
	
		AlphaComposite _bgAc = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _bgAlpha);
		
		printCmd.addBgFill(_bgColor, _bgAc, (int)_x, (int)_y, (int)_w, (int)_h);
		
			
		int _checkSize = Math.round(ObjectToFloat(12));
		
		String _label = ValueConverter.getString(_item.get("label"));
				
		boolean _selected = ValueConverter.getBoolean(_item.get("selected"));
		
		float _chY = _y + ((_h / 2)  - (_checkSize/2));		
		
		
		int _selX = (int)_x + 2;
		int _selY = (int)_chY;
		
		Color borderColor = new Color(0,0,0);
		this.mg2.setColor(borderColor);   
		
		float _widthF  = ObjectToFloat(1);		
		Stroke stroke = new BasicStroke(_widthF);
		this.mg2.setStroke(stroke);	
		
		Rectangle2D checkRect = new  Rectangle2D.Double(_selX,_selY,_checkSize,_checkSize);		
		this.mg2.draw(checkRect);	
		
		printCmd.addRectBorder(stroke, borderColor, checkRect);
				
		
		if( _selected){	
			_widthF = _widthF - 0.3f;	
			 stroke = new BasicStroke(_widthF);
			
			Line2D _line = new Line2D.Float(_selX, _selY,_selX + _checkSize/2, _selY + _checkSize);
			this.mg2.draw(_line);	
			printCmd.addLine(stroke, borderColor, _line);
			
			_line = new Line2D.Float(_selX + _checkSize/2, _selY + _checkSize,_selX + _checkSize, _selY);
			this.mg2.draw(_line);			
			printCmd.addLine(stroke, borderColor, _line);
		}
			
		int _txStyle = 0;
		float _fontSize = 0;
		String _verticalAlign = "middle";
		if(_item.containsKey("verticalAlign") && !_item.get("verticalAlign").equals(null) && !_item.get("verticalAlign").equals("")){
			_verticalAlign = ValueConverter.getString(_item.get("verticalAlign"));
		}
				
		_txStyle = fontStyleToInt(_item.get("fontStyle"), _item.get("fontWeight"), _item.get("textDecoration"));
		_fontSize = ObjectToFloat(_item.get("fontSize"));
		Color _fontColor = new Color(ValueConverter.getInteger( _item.get("fontColorInt")));
		String _fontName = ValueConverter.getString(_item.get("fontFamily"));
		String txtDeco = _item.get("textDecoration").toString();
		String _fName = getDefFont(_fontName);
		
		this.mg2.setColor(_fontColor);
		if( _fName.equals("") )
		{
			mDefFont = new Font("맑은고딕", _txStyle, (int)_fontSize);
		}
		else
		{
			mDefFont = new Font(_fName, _txStyle, (int)_fontSize);
		}		
		
		this.mg2.setFont(mDefFont);			
		drawString(this.mg2, mDefFont, _label, (int)_x + 18, (int)_y, 0, _verticalAlign, _w, _h , 0 ,AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1),_fontColor,txtDeco, 1.12f);
	}
	
	
	//Rectangle Border command
	public void execDrawRectBorder(Stroke stroke , Color color, Rectangle2D rect){
		//checkbox
		this.mg2.setStroke(stroke);	
		this.mg2.setColor(new Color(0,0,0));   
		this.mg2.draw(rect);
	}
	
	//single string command
	public void execDrawSingleString(Color fontColor, Font font,int x, int y ,  String text) {  
		
		// string
		this.mg2.setColor(fontColor);
		this.mg2.setFont(font);			
		this.mg2.drawString(text, x,y);	 
	}
	
	
	//Ellipse2D Border and Fill command
	public void execDrawEllipse(Stroke stroke , Color borderColor, Color fillColor, Ellipse2D.Double hole , boolean isFill){
		this.mg2.setColor(borderColor); 
		this.mg2.setStroke(stroke);		
		this.mg2.draw(hole);
		if(isFill){
			this.mg2.setColor(fillColor); 
			this.mg2.draw(hole);
		}
	}
	
	
	private HashMap<String,Object > _paraGraphMap = new HashMap<String,Object>();
	
	private void CreatePdfRadioGroup( HashMap<String, Object> _item )
	{
		
		float _x = ObjectToFloat(_item.get("x"));
		float _y = ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		String _symbolType = (String) _item.get("symbolType");
		
		int _checkSize = Math.round(ObjectToFloat(12));
		
		String _label = ValueConverter.getString(_item.get("label"));
				
		boolean _selected = ValueConverter.getBoolean(_item.get("selected"));
		
		float _chY = _y + ((_h / 2)  - (_checkSize/2));		
		// background 
		float _bgAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));
		
		AlphaComposite _bgAc = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _bgAlpha);		
		printCmd.addBgFill(_bgColor, _bgAc, (int)_x, (int)_y, (int)_w, (int)_h);
		
		float _widthF  = ObjectToFloat(1);		
		Stroke stroke = new BasicStroke(_widthF);
		this.mg2.setStroke(stroke);
		
		Color borderColor = new Color(0,0,0);
		this.mg2.setColor(borderColor);   
		
		int _selX = (int)_x + 2;
		int _selY = (int)_chY;
		
		if( _symbolType.equalsIgnoreCase("check") ){

			
			
			Rectangle2D checkRect = new  Rectangle2D.Double(_selX,_selY,_checkSize,_checkSize);		
			this.mg2.draw(checkRect);				
			printCmd.addRectBorder(stroke, borderColor, checkRect);
			if( _selected){		
				_widthF = _widthF - 0.3f;	
				stroke = new BasicStroke(_widthF);
				this.mg2.setStroke(stroke);		
				
				Line2D _line = new Line2D.Float(_selX, _selY,_selX + _checkSize/2, _selY + _checkSize);
				this.mg2.draw(_line);	
				printCmd.addLine(stroke, borderColor, _line);
				
				_line = new Line2D.Float(_selX + _checkSize/2, _selY + _checkSize,_selX + _checkSize, _selY);
				this.mg2.draw(_line);			
				printCmd.addLine(stroke, borderColor, _line);
			}
		}else{
			this.mg2.setColor(new Color(0,0,0)); 			
			Ellipse2D.Double hole = new Ellipse2D.Double();			
		    hole.width = _checkSize;
		    hole.height = _checkSize;
		    hole.x = _selX;
		    hole.y = _selY;		
		    this.mg2.draw(hole);		    
		    
			printCmd.addEllipse(stroke, borderColor, borderColor, hole, false);
			 
			if( _selected){
				this.mg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				this.mg2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			    hole = new Ellipse2D.Double();
			    hole.width = _checkSize/2;
			    hole.height = _checkSize/2;
			    hole.x = _selX + (_checkSize/2 - _checkSize/4)+0.5;
			    hole.y = _selY + (_checkSize/2 - _checkSize/4)+0.5;
			    this.mg2.fill(hole);
			    printCmd.addEllipse(stroke, borderColor, borderColor, hole, true); 
			}
		}
		
		int _txStyle = 0;
		float _fontSize = 0;
		String _verticalAlign = "middle";
		if(_item.containsKey("verticalAlign") && !_item.get("verticalAlign").equals(null) && !_item.get("verticalAlign").equals("")){
			_verticalAlign = ValueConverter.getString(_item.get("verticalAlign"));
		}
				
		_txStyle = fontStyleToInt(_item.get("fontStyle"), _item.get("fontWeight"), _item.get("textDecoration"));
		String txtDeco =  _item.get("textDecoration").toString();
		_fontSize = ObjectToFloat(_item.get("fontSize"));
		Color _fontColor = new Color(ValueConverter.getInteger( _item.get("fontColorInt")));
		String _fontName = ValueConverter.getString(_item.get("fontFamily"));

		String _fName = getDefFont(_fontName);
		
		this.mg2.setColor(_fontColor);
		if( _fName.equals("") )
		{
			mDefFont = new Font("맑은고딕", _txStyle, (int)_fontSize);
		}
		else
		{
			mDefFont = new Font(_fName, _txStyle, (int)_fontSize);
		}	
		this.mg2.setFont(mDefFont);			
		drawString(this.mg2, mDefFont, _label, (int)_x + 18, (int)_y, 0, _verticalAlign, _w, _h , 0, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f), _fontColor,txtDeco, 1.12f);		

	}
		
	private void CreatePdfImage( HashMap<String, Object> _item )
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
	
		float _itemAlpha = ValueConverter.getFloat(_item.get("alpha"));
		
		String _imageUrl = "";		
		_imageUrl = ValueConverter.getString(_item.get("src"));
		if( _imageUrl == null || _imageUrl.equals("null") )
		{			
			return;
		}
		
		boolean _isOriginSize = false;
		if( _item.containsKey("isOriginalSize") &&  !_item.get("isOriginalSize").equals(""))
		{
			_isOriginSize = Boolean.valueOf(_item.get("isOriginalSize").toString());
		}
		
		BufferedImage _image = getLocalImageFile(_imageUrl);
		if(_image != null)
		{
			if(_isOriginSize){
				HashMap<String,Float> _orignSize = common.getOriginSize(_item.get("width"),_item.get("height"),_image);				
				_w = ObjectToFloat(_orignSize.get("width"));
				_h = ObjectToFloat(_orignSize.get("height"));
				_x = _x + 	ObjectToFloat(_orignSize.get("marginX"));	
				_y = _y + 	ObjectToFloat(_orignSize.get("marginY"));	
			}
			
			AlphaComposite _ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _itemAlpha);
			this.mg2.setComposite(_ac);
			
			this.mg2.drawImage(_image,(int) _x, (int)_y, (int)_w, (int)_h,null);
			
			//command 등록
			printCmd.addImage(_ac, _image, (int) _x, (int)_y, (int)_w, (int)_h);			
		}		 		
	}

	
	private void CreatePdfSign( HashMap<String, Object> _item )
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		float _itemAlpha = ValueConverter.getFloat(_item.get("alpha"));	
		
		String _signSrc = "";		
		_signSrc = ValueConverter.getString(_item.get("src"));
		if( _signSrc == null || _signSrc.equals("null") || _signSrc.equals("") )
		{		
			return;
		}		
		
		byte[] bAr=null;
		BufferedImage _image = null;
		
		try {
			bAr = Base64Coder.decode(_signSrc);
		} catch (Exception e) {
			// TODO: handle exception
			try {
				bAr = Base64Coder.decode( URLDecoder.decode(_signSrc,"UTF-8"));
			} catch (Exception e2) {
				// TODO: handle exception
				
			}
		}
		if(bAr!=null){
			 try {
				_image = ImageIO.read(new ByteArrayInputStream(bAr));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(_image != null)
		{			
			AlphaComposite _ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _itemAlpha);
			this.mg2.setComposite(_ac);			
			this.mg2.drawImage(_image,(int) _x, (int)_y, (int)_w, (int)_h,null);
			
			//command 등록			
			printCmd.addImage(_ac, _image,(int) _x, (int)_y, (int)_w, (int)_h);
		}	
	}
		
	//drawRectangle command 실행
	public void execDrawImage( Object _ac , BufferedImage _image, int _x, int _y, int _w,int _h) {  
		this.mg2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		this.mg2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		this.mg2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
		this.mg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );				
		this.mg2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		this.mg2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);		
		if(_ac != null){//normal image
			this.mg2.setComposite((AlphaComposite)_ac);	
		}
		this.mg2.drawImage(_image,(int) _x, (int)_y, (int)_w, (int)_h,null);
		
	}

	
	private void CreatePdfLine( HashMap<String, Object> _item )
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = ObjectToFloat(_item.get("y"));
		
		float _x1 = ObjectToFloat(_item.get("x1"));
		float _y1 = ObjectToFloat(_item.get("y1"));
		
		float _x2 = ObjectToFloat(_item.get("x2"));
		float _y2 = ObjectToFloat(_item.get("y2"));
		
		float _sX = 0;
		float _sY = 0;
		float _eX = 0;
		float _eY = 0;
		
		
		if( _x1 == 0 )
		{
			_sX = _x;
		}
		else
		{
			_sX = _x + _x1;
		}
		
		
		if( _y1 == 0 )
		{
			_sY = _y;
		}
		else
		{
			_sY = _y + _y1;
		}
		
		if( _x2 == 0 )
		{
			_eX = _x;
		}
		else
		{
			_eX = _x + _x2;
		}
		
		
		if( _y2 == 0 )
		{
			_eY = _y;
		}
		else
		{
			_eY = _y + _y2;
		}
		
		
		Color _lineColor = new Color(ValueConverter.getInteger(_item.get("lineColorInt")));
		
		// 기존에는 보더의 thickness값이 ObjectToFloat(_item.get("thickness")) 로만 지정되어있음
		// 라벨의 보더처리시엔느 아래와 같이 -0.3f 하고잇어 이를 동일하게 변경
		float _widthF  = ObjectToFloat(_item.get("thickness"));
		_widthF = _widthF - 0.3f;
		
		this.mg2.setColor(_lineColor);	
			
		// creates a solid stroke with line width
		Stroke _stroke = new BasicStroke(_widthF);
		this.mg2.setStroke(_stroke);	
		Line2D _line =  new Line2D.Float(_sX, _sY, _eX,_eY);		
		this.mg2.draw(_line);
		
		printCmd.addLine(_stroke,  _lineColor, _line);		
	}
	
	//drawRectangle command 실행
	public void execDrawLine(Stroke _stroke, Color _lineColor, Line2D _line) {  
		this.mg2.setStroke(_stroke);	
		this.mg2.setColor(_lineColor);	
		this.mg2.draw(_line);
	}

	
	private void CreatePdfCircle( HashMap<String, Object> _item )
	{		
		int _x = Math.round(ObjectToFloat(_item.get("x")));
		int _y = Math.round(ObjectToFloat(_item.get("y")));
		int _w = Math.round(ObjectToFloat(_item.get("width")));
		int _h = Math.round(ObjectToFloat(_item.get("height")));		
		
		float _lineW = ObjectToFloat(_item.get("strokeWidth"));
		
		float _alpha = ValueConverter.getFloat(_item.get("alpha"));
		float _bgAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
		float _brAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));
				
		
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("contentBackgroundColorInt")));
		Color _brColor = new Color(ValueConverter.getInteger(_item.get("borderColorInt")));
		
		float _itemAlpha = _bgAlpha * _alpha;
		float _borderAlpha = _brAlpha * _alpha;
		
		Stroke _stroke = new BasicStroke(_lineW);
		this.mg2.setStroke(_stroke);
		
		AlphaComposite _bgAc = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _itemAlpha);
		this.mg2.setComposite(_bgAc);
		this.mg2.setColor(_bgColor);		
		
		this.mg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.mg2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
	    Ellipse2D.Double _hole = new Ellipse2D.Double();
	    _hole.width = _w;
	    _hole.height = _h;
	    _hole.x = _x;
	    _hole.y = _y;
	    this.mg2.fill(_hole);				

	    AlphaComposite _brAc = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _borderAlpha);
		this.mg2.setComposite(_brAc);
		
		this.mg2.setColor(_brColor); 	
		
		this.mg2.drawOval(_x,_y,_w,_h);		
		printCmd.addCircle(_stroke, _bgAc, _hole, _brAc, _bgColor, _brColor, _x, _y, _w, _h);
		
	}
	
	//drawRectangle command 실행
	public void execDrawCircle(Stroke _stroke, AlphaComposite _bgAc , Ellipse2D.Double _hole, AlphaComposite _brAc ,Color _bgColor, Color _brColor,
    		int _x, int _y, int _w,int _h) {  
		this.mg2.setStroke(_stroke);			
		this.mg2.setComposite(_bgAc);
		this.mg2.setColor(_bgColor);	
		this.mg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.mg2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);		
		this.mg2.fill(_hole);	
		this.mg2.setComposite(_brAc);
		this.mg2.setColor(_brColor);   
		this.mg2.drawOval(_x,_y,_w,_h);
	}
			
	
	private void CreatePdfRectangle( HashMap<String, Object> _item )
	{
		int _x = Math.round(ObjectToFloat(_item.get("x")));
		int _y = Math.round(ObjectToFloat(_item.get("y")));
		int _w = Math.round(ObjectToFloat(_item.get("width")));
		int _h = Math.round(ObjectToFloat(_item.get("height")));
		int _radius = ValueConverter.getInteger(_item.get("rx"));
		float _lineW = ObjectToFloat(_item.get("borderThickness"));		
		
		float _alpha =  ValueConverter.getFloat(_item.get("alpha")); // ITEM alpha
		float _bgAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));// Background alpha	
		float _brAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));
		
		
		Color _bgColor = hex2Rgb(ValueConverter.getString(_item.get("contentBackgroundColor")));		
		Color _brColor = new Color(ValueConverter.getInteger(_item.get("borderColorInt")));		

		float _itemAlpha = _bgAlpha * _alpha;
		float _borderAlpha = _brAlpha * _alpha;
		
		AlphaComposite ac = null;
		
		Stroke stroke = new BasicStroke(_lineW);
		this.mg2.setStroke(stroke);

		
		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _itemAlpha);
		this.mg2.setComposite(ac);
		this.mg2.setColor(_bgColor);		
		
		this.mg2.fillRoundRect(_x,_y,_w,_h,_radius,_radius);			

		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _borderAlpha);
		this.mg2.setComposite(ac);
		
		this.mg2.setColor(_brColor);   
		this.mg2.drawRoundRect(_x,_y,_w,_h,_radius,_radius);	
		//실행한 명령어 등록
		printCmd.addRectangle(stroke, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _bgAlpha), AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha), _bgColor, _brColor, _x, _y, _w, _h, _radius);
	}	
	
	//drawRectangle command 실행
	public void execDrawRectangle(Stroke _stroke, AlphaComposite _bgAc , AlphaComposite _brAc ,Color _bgColor, Color _brColor,
    		int _x, int _y, int _w,int _h,int _radius ) {  
		this.mg2.setStroke(_stroke);			
		this.mg2.setComposite(_bgAc);
		this.mg2.setColor(_bgColor);	
		this.mg2.fillRoundRect(_x,_y,_w,_h,_radius,_radius);
		this.mg2.setComposite(_brAc);
		this.mg2.setColor(_brColor);   
		this.mg2.drawRoundRect(_x,_y,_w,_h,_radius,_radius);
	}
		
	private void CreatePdfGradiantRectangle( HashMap<String, Object> _item )
	{
		int _x = Math.round(ObjectToFloat(_item.get("x")));
		int _y = Math.round(ObjectToFloat(_item.get("y")));
		int _w = Math.round(ObjectToFloat(_item.get("width")));
		int _h = Math.round(ObjectToFloat(_item.get("height")));
		int _radius = ValueConverter.getInteger(_item.get("rx"));
		float _lineW = ObjectToFloat(_item.get("borderThickness"));		
		
		float _alpha =  ValueConverter.getFloat(_item.get("alpha")); // ITEM alpha
		float _brAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));
		
		
		//float _bgAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));// Background alpha			
		Color _brColor = new Color(ValueConverter.getInteger(_item.get("borderColorInt")));		
		
		Stroke stroke = new BasicStroke(_lineW);
		this.mg2.setStroke(stroke);
	
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);
		this.mg2.setComposite(ac);
		
		ArrayList<Integer> _bgList = (ArrayList<Integer>) _item.get("contentBackgroundColorsInt");
 		Color _bgColor1 = new Color( ValueConverter.getInteger(_bgList.get(0)) );
 		Color _bgColor2 = new Color( ValueConverter.getInteger(_bgList.get(1)) );
 		
 		GradientPaint gradient =  new GradientPaint(_x, _y, _bgColor1, _x, _y +_h , _bgColor2,true);
		this.mg2.setPaint(gradient);		
		this.mg2.fillRoundRect(_x,_y,_w,_h,_radius,_radius);	

		float _borderAlpha = _brAlpha * _alpha;
		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _borderAlpha);
		this.mg2.setComposite(ac);
		
		this.mg2.setColor(_brColor);   
		this.mg2.drawRoundRect(_x,_y,_w,_h,_radius,_radius);	
		
		printCmd.addGradiantRectangle(stroke, ac, gradient, _brColor, _x, _y, _w, _h, _radius);
 		
	}
	
	//drawGradiantRectangle command 실행
	public void exdcDrawGradiantRectangle(Stroke _stroke, AlphaComposite _ac , GradientPaint gradient, Color _brColor,
    		int _x, int _y, int _w,int _h,int _radius ) {  
		this.mg2.setStroke(_stroke);			
		this.mg2.setComposite(_ac);
		this.mg2.setPaint(gradient);		
		this.mg2.fillRoundRect(_x,_y,_w,_h,_radius,_radius);
		this.mg2.setColor(_brColor);   
		this.mg2.drawRoundRect(_x,_y,_w,_h,_radius,_radius);
	}
	
	
	private void CreatePdfClipImage( HashMap<String, Object> _item )
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		String _clipName = ValueConverter.getString(_item.get("clipArtData"));
		if( !_clipName.substring(_clipName.length() - 3, _clipName.length()).toUpperCase().equals("SVG"))
		{
			_clipName = _clipName+".svg";
		}		
		
		URL Url = null; 		
		String _urlStr = Log.serverURL.replace("ubiform.do", "") + "UView5/assets/images/svg/" + _clipName;				
		
		try {
			
			Url = new URL(_urlStr);
			
			URLConnection _urlConnection = Url.openConnection();		
			InputStream is  = _urlConnection.getInputStream();			
			InputStreamReader _inputR = new InputStreamReader(is , "UTF-8");
			BufferedReader _buf = new BufferedReader(_inputR);
			String _str = "";
			String _svgStr = "";
			while(true)
			{
				_str = _buf.readLine();
				if( _str == null) break;
				_svgStr += _str;		 		
			}			
			
			BufferedImage _image = null;			
			_image = svgToPng(_svgStr, _w, _h);			    
		    if(_image != null)
			{ 	                
				this.mg2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				this.mg2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
				this.mg2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
				this.mg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );				
				this.mg2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
				this.mg2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);		
                
                this.mg2.drawImage(_image,(int) _x, (int)_y, null);
                
                //command 등록			
    			printCmd.addImage(null, _image,(int) _x, (int)_y,(int) _w, (int)_h);
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
        
	}
	
	
	
	private BufferedImage svgToPng(String _svgStr , float _w , float _h) {
		BufferedImage _image = null;
	    try {
	    	 
	    	_svgStr = _svgStr.replaceAll("%20", " ");
	    	_svgStr = _svgStr.replaceAll("px", "");
			int _widthIdx = _svgStr.indexOf("width");
			String _widthString =  "";
			float _widthValue = 0f;
			if( _widthIdx > -1 )
			{
				_widthString = _svgStr.substring( _widthIdx , _svgStr.indexOf("\"" , _widthIdx + 7) + 1);
				String _widthData = _widthString.substring(_widthString.indexOf("\"") + 1, _widthString.indexOf("\"", _widthString.indexOf("\"") + 1));
				_widthData = _widthData.replaceAll("[^\\d.-]", "");	// width의 px와 같은 문자를 제거
				_widthValue = ValueConverter.getFloat( _widthData );
			}
			
			int _heightIdx = _svgStr.indexOf("height");
			String _heightString = "";
			float _heightValue = 0f;
			if( _heightIdx > -1 )
			{
				_heightString = _svgStr.substring( _heightIdx , _svgStr.indexOf("\"" , _heightIdx + 8) + 1);
				String _heightData = _heightString.substring(_heightString.indexOf("\"") + 1, _heightString.indexOf("\"", _heightString.indexOf("\"") + 1));
				_heightData = _heightData.replaceAll("[^\\d.-]", "");	// width의 px와 같은 문자를 제거				
				_heightValue = ValueConverter.getFloat(_heightData);
			}

			
			TranscodingHints transcoderHints = new TranscodingHints();
		    transcoderHints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
		    transcoderHints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, SVGDOMImplementation.getDOMImplementation());
		    transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,		            SVGConstants.SVG_NAMESPACE_URI);
		    transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
		    
		    
		    float _aplyWidth = _widthValue > _w? _widthValue : _w;
		    float _aplyHeight = _heightValue > _h? _heightValue : _h;
		    
		    transcoderHints.put(ImageTranscoder.KEY_WIDTH, _aplyWidth );
		    transcoderHints.put(ImageTranscoder.KEY_HEIGHT, _aplyHeight );
		    
		    
		    java.awt.Rectangle _aoi = new java.awt.Rectangle();
		    _aoi.x = 0;
		    _aoi.y = 0;
		    _aoi.width = (int)_widthValue;
		    _aoi.height = (int)_heightValue;
		    
		    transcoderHints.put(ImageTranscoder.KEY_AOI,_aoi);		    
			
			//TranscoderInput input_png_image =  new TranscoderInput(new ByteArrayInputStream(_svgStr.getBytes()));		
		    InputStream inputStream = new ByteArrayInputStream(_svgStr.getBytes());
		    Document doc = getDocument(inputStream);
		    TranscoderInput input_png_image = new TranscoderInput(doc);
		    
			ByteArrayOutputStream png_ostream = new ByteArrayOutputStream();
		    TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);     
			
		    PNGTranscoder my_converter = new PNGTranscoder(); 
		    my_converter.setTranscodingHints(transcoderHints);		 
			my_converter.transcode(input_png_image, output_png_image);
			
		    png_ostream.flush();
		    png_ostream.close();

		    _image =  ImageIO.read(new ByteArrayInputStream(png_ostream.toByteArray()));	
	    	
		    inputStream.close();
	    } catch (Exception exc) {
	    	exc.printStackTrace();
	    }
	    return _image;
	}
	
	private Document getDocument(InputStream inputStream) throws IOException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        Document doc = f.createDocument("http://www.w3.org/2000/svg", inputStream);
        return doc;
    }
	
	
	private void CreatePdfSVGArea( HashMap<String, Object> _item )
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));		
		
		String _dataStr;
		try {
			_dataStr = URLDecoder.decode( ValueConverter.getString(_item.get("data")) , "UTF-8");
			
			String _className = (String) _item.get("className");
			if(_className != null && _className.equals("UBQRCode"))
			{
				_dataStr = Base64Decoder.decode(_dataStr);
				System.out.print("UBQRCode:svg=[" + _dataStr + "]");
			}
			
			BufferedImage  _image = null;
			_image = svgToPng(_dataStr , _w,_h);
		
			if(_image != null)
			{                 
				this.mg2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				this.mg2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
				this.mg2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
				this.mg2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );				
				this.mg2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
				this.mg2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);		
				this.mg2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);					
                
                this.mg2.drawImage(_image,(int) _x, (int)_y,(int) _w, (int)_h,null);
                
              //command 등록			
    			printCmd.addImage(null, _image,(int) _x, (int)_y,(int) _w, (int)_h);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		} 
	}	
	
	private void CreatePdfSVGRichTextLabel( HashMap<String, Object> _item )
	{
		
		float _x = ObjectToFloat( _item.get("x") );
		float _y = ObjectToFloat( _item.get("y") );
		float _w = ObjectToFloat( ValueConverter.getFloat(_item.get("x")) + ValueConverter.getFloat(_item.get("width")) );
		float _h = ObjectToFloat( ValueConverter.getFloat(_item.get("y")) + ValueConverter.getFloat(_item.get("height")) );
		
		AlphaComposite _ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
		
		String _id = ValueConverter.getString(_item.get("id"));
		String _type = ValueConverter.getString(_item.get("type"));		
	
		Rectangle _bRect;
		boolean _verticalRotateFlg = false;
		
		ArrayList<ArrayList<HashMap<String, Object>>> lineAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("children");
		
		if(lineAr == null || lineAr.size() == 0 ) return;

		
		// background 
//		float _bgAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		float _bgAlpha = 0;
		Color _bgColor = new Color(0);
//		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));
		
		
		// border
		HashMap<String, Float> _rect = new HashMap<String, Float>();
		ArrayList<HashMap<String, Object>> _content;
		
		HashMap<String, Float> _rotateDef = new HashMap<String, Float>();
		
		// text
		int _align = getAlignmentInt( "left" );
		String _verticalAlign = ValueConverter.getString("top");
//		int _txStyle = fontStyleToInt(_item.get("fontStyle"), "normal", "none");
//		float _fontSize = ObjectToFloat(_item.get("fontSize"));
		float _fontSize = Double.valueOf(Math.floor((ValueConverter.getFloat(_item.get("fontSize")) / 96f ) * 72f )).floatValue();
//		Color _txColor = new Color(ValueConverter.getInteger(_item.get("fontColorInt")));
		Color _txColorDef = new Color(ValueConverter.getInteger(_item.get("fontColorInt")));
		String _fontName = ValueConverter.getString(_item.get("fontFamily"));
		float _lineGap = Float.valueOf( _item.get("lineGap").toString() );	
		
		String _fName = getDefFont(_fontName);
		Color _txColor;
		
		float _minW = _w - _x;
		FontMetrics metrics = this.mg2.getFontMetrics(mDefFont);
		FontMetrics metricsTemp = null; 
		float _startWidth = 0; 
		float _startHeight = _y; 
		ArrayList<String> _txtp = new ArrayList<String>();
		for (ArrayList<HashMap<String, Object>> textAr : lineAr) {			   	
			float _lineMinW = _x;
			for (int i = 0; i < textAr.size(); i++) {
				
				HashMap<String, Object> _lineItem = textAr.get(i);
				String _fontStyle = "normal";
				if( _lineItem.containsKey("font-style") )
				{
					_fontStyle = _lineItem.get("font-style").toString();
				}
				String _textDecoration = "none";
				if( _lineItem.containsKey("text-decoration") )
				{
					_textDecoration = _lineItem.get("text-decoration").toString();
				}
				
				String _fontWeight = "normal";
				if( _lineItem.containsKey("fontWeight") )
				{
					_fontWeight = _lineItem.get("fontWeight").toString();
				}			
				
				
				int _txStyle = fontStyleToInt(_fontStyle, _fontWeight, _textDecoration);
				_txColor = _txColorDef;
				
				if(_lineItem.containsKey("fontColor"))
				{
					if( _lineItem.get("fontColor").toString().charAt(0) == '#' )
					{
						_txColor = new Color(Integer.parseInt(_lineItem.get("fontColor").toString().substring(1), 16));
					}
				}
				this.mg2.setColor(_txColor);
				
				
				if( _fName.equals("") )
				{				
					if( mFontListAr.indexOf("맑은 고딕") != -1 )
					{
						mDefFont = new Font("맑은고딕", _txStyle, (int)_fontSize);
					}
					else
					{
						mDefFont = new Font(_fName, _txStyle, (int)_fontSize);
					}
				}
				else
				{
					mDefFont = new Font(_fName, _txStyle, (int)_fontSize);
				}
				
				this.mg2.setFont(mDefFont);	
				metricsTemp = this.mg2.getFontMetrics(mDefFont);
				String _itemText = ValueConverter.getString(_lineItem.get("text"));		
				
				//float _padding =  ValueConverter.getFloat(_lineItem.get("padding"));				
				drawString(this.mg2, mDefFont, _itemText, (int)_lineMinW, (int)_startHeight, _align, _verticalAlign, 
		   				ObjectToFloat(ValueConverter.getFloat(_item.get("width"))), ObjectToFloat(ValueConverter.getFloat(_item.get("height"))) , 0 , _ac ,_txColor,_textDecoration,1.12f);					
				
				_lineMinW = _lineMinW +  metricsTemp.stringWidth(_itemText);
			}
			if( _minW < _lineMinW )
			{
				_minW = _lineMinW;
			}
			_startWidth = _x;
			_startHeight = _startHeight + metrics.getHeight();			
		}
	}
	
	private void CreatePdfSVGRichText( HashMap<String, Object> _item )
	{
		float _x = ObjectToFloat(_item.get("x"));
//		float _y = ObjectToFloat(_item.get("y"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
	}
	
	private void CreatePdfDoImage( HashMap<String, Object> _item )
	{
		float _x = ObjectToFloat(_item.get("x"));
		//float _y = mPageHeight - ObjectToFloat(_item.get("y")) - ObjectToFloat(_item.get("height"));
		float _y = ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		String _imageUrl = "";
		
		_imageUrl = ValueConverter.getString(_item.get("src"));
		//Image _image = Image.getInstance(new URL(mUrl + _imageUrl));	
		//Image _image = common.getRemoteImageFile(_imageUrl);
		BufferedImage _image = getLocalImageFile(_imageUrl);
		if(_image != null)
		{			
			this.mg2.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR);	
			this.mg2.drawImage(_image,(int) _x, (int)_y, (int)_w, (int)_h, null);
			//command 등록			
			printCmd.addImage(null, _image,(int) _x, (int)_y, (int)_w, (int)_h);
		}		
	}
		
	// 로컬에 있는 서블릿 메소드를 호출하여 이미지 데이터를 받아온다.
	private BufferedImage getLocalImageFile(String _imageUrl)
	{
		BufferedImage bufImg = null;
		byte[] bAr = null;
		
		if(_imageUrl == null || "".equals(_imageUrl))
			return null;
		
		if( _imageUrl.startsWith("http") ){
			return null;
		}
		else
		{
			//bAr = Base64Coder.decode(URLDecoder.decode(_imageUrl));
			try {
				bAr = org.apache.commons.codec.binary.Base64.decodeBase64(URLDecoder.decode(_imageUrl).getBytes());
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			}
		    try {
				if( bAr != null ) bufImg = ImageIO.read(new ByteArrayInputStream(bAr));
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
        
        return bufImg;
	}
	
	
	private BufferedImage resizeBimg(BufferedImage img, int newW, int newH) { 			
				
	    java.awt.Image tmp = img.getScaledInstance(newW, newH , java.awt.Image.SCALE_FAST);	    
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2d = dimg.createGraphics();	   
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR);	    
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

		//BufferedImage dimg = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, newW, newH, Scalr.OP_GRAYSCALE);

/*		
		Resizer resizer = DefaultResizerFactory.getInstance().getResizer(
				  new Dimension(img.getWidth(), img.getHeight()), 
				  new Dimension(newW, newH));
		BufferedImage dimg = new FixedSizeThumbnailMaker(
						newW, newH, false, true).resizer(resizer).make(img);
*/		
	    return dimg;
	}  
	
	private void testPdfChangeMapping( HashMap<String, Object> _change, HashMap<String, ArrayList<HashMap<String,Object>>> _pageData, String _chkKey )
	{
		HashMap<String, ArrayList<HashMap<String, Object>>> _changePageData;
		ArrayList<HashMap<String, Object>> _itemList;
		ArrayList<HashMap<String, Object>> _changeItemList;
		HashMap<String, Object> _oItem;
		HashMap<String, Object> _cItem;
		
		if( _change.containsKey(_chkKey) == false )
		{
			return;
		}
		
		// change Data
		_changePageData = (HashMap<String, ArrayList<HashMap<String,Object>>>) _change.get(_chkKey);
		_changeItemList = _changePageData.get("Controls");

		// orignal Data
//			_pageData = _orignal.get(_key);
		_itemList = _pageData.get("pageData");
		
		String _oID = "";
		String _oClassName = "";

		String _cID = "";
		for (int j = 0; j < _changeItemList.size(); j++) 
		{
			_cItem = _changeItemList.get(j);
			_cID = (String) _cItem.get("CtrlId");

			for (int i = 0; i < _itemList.size(); i++)
			{
				_oItem = _itemList.get(i);
				_oID = (String) _oItem.get("id");
				_oClassName = (String) _oItem.get("className");					
				
				if(_oClassName == null)
				{
					continue;
				}
				
				if( _oClassName.equals("UBSignature") || _oClassName.equals("UBPicture") || _oClassName.equals("UBCheckBox") )		
				{
					if( _oID.equals(_cID))
					{
						String _cData = (String) _cItem.get("Value");

						if( _oClassName.equals("UBSignature") )
						{ 
							_oItem.put("src", _cData.substring( _cData.indexOf(",") + 1 , _cData.length()));
							break;
						}
						else if( _oClassName.equals("UBPicture") )
						{ 
							_oItem.put("src", _cData.substring( _cData.indexOf(",") + 1 , _cData.length()));
							break;
						}
						/*
						else if( _oClassName.equals("UBTextInput") )
						{
							_oItem.put("text", _cData);
							break;
						}
						else if( _oClassName.equals("UBTextArea") )
						{
							_oItem.put("text", _cData);
							break;
						}
						else if( _oClassName.equals("UBRadioBorder") )
						{
							_oItem.put("selected", _cData);
							break;
						}
						*/
						else if( _oClassName.equals("UBCheckBox") )
						{
							_oItem.put("selected", _cData);
							break;
						}
						
					} // if id 비교
				} // if className 체크
//					else if( _oItem.containsKey("editable") &&  ValueConverter.getBoolean(_oItem.get("editable")) )  		// 임시로 주석 처리 ( 아이템에 editable속성은 없음 ) 
				else
				{
					if( false && ( _oItem.get("dataType") == null || !( _oItem.get("dataType").equals("1") ||_oItem.get("dataType").equals("2")) ) && _oID.equals(_cID) )
					{
						String _cData = (String) _cItem.get("Value");
						_oItem.put("text", _cData);
						break;
					}
				}
			}// for orignal ItemList
		}
	}
	
	
	
	private void setTBborderSetting()
	{	
		ArrayList<Color> colors = new ArrayList<Color>();
		ArrayList<Line2D.Float> lines = new ArrayList<Line2D.Float>();
		ArrayList<Stroke> strokes = new ArrayList<Stroke>();
		ArrayList<AlphaComposite> acs = new ArrayList<AlphaComposite>();
		AlphaComposite ac = null;
		Line2D.Float _line = null;
		float _alpha = 1;
		
		for(String key : mTBborder.keySet())
		{
			ArrayList<HashMap<String, Object>> _tbBorderList = mTBborder.get(key);
			
			int _size = _tbBorderList.size();
			
			for (int i = 0; i < _size; i++) 
			{
				HashMap<String, Object> _hm = _tbBorderList.get(i);
				HashMap<String, String> _doubleBorderMap = (HashMap<String, String>) _hm.get("doubleBorderMap");
				String _type = ValueConverter.getString(_hm.get("type"));
				Color _color = (Color) _hm.get("color");
				int _width = ValueConverter.getInteger(_hm.get("width"));
				float _widthF = ObjectToFloat(_hm.get("width"));
				String _side = ValueConverter.getString(_hm.get("side"));
				_alpha =  ValueConverter.getFloat(_hm.get("alpha"));

				String _originalType = ValueConverter.getString(_hm.get("originalType"));
//				float _widthF = ValueConverter.getFloat(_hm.get("width"));
				_widthF = _widthF - 0.3f;
				String _sp = "";
				String _ep = "";

				_sp = ValueConverter.getString(_hm.get("sp"));
				_ep = ValueConverter.getString(_hm.get("ep"));
				
				float _sp1 = 0;
				float _sp2 = 0;
				float _ep1 = 0;
				float _ep2 = 0;

				_sp1 = ValueConverter.getFloat(_sp.split(",")[0]);
				_sp2 = ValueConverter.getFloat(_sp.split(",")[1]);
				
				_ep1 = ValueConverter.getFloat(_ep.split(",")[0]);
				_ep2 = ValueConverter.getFloat(_ep.split(",")[1]);			
				
				
				if( _type.equals("none")==false )
				{
					ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);
					this.mg2.setComposite(ac);
					this.mg2.setColor(_color);
					// creates a solid stroke with line width
					Stroke _stroke = lineType_Setting(_type.replace("_share", "")  , _widthF );
					this.mg2.setStroke(_stroke);		
					_line = new Line2D.Float(_sp1, _sp2, _ep1, _ep2);
					this.mg2.draw(_line);
				
					lines.add(_line);
					colors.add(_color);
					strokes.add( _stroke);	
					acs.add(ac);
				}				
			}
		}
		printCmd.addBorder(colors, strokes, lines,acs);
	}	
	
	
	private Stroke lineType_Setting( String _type , float _width )
	{
		Stroke stroke = null;
		
		float _dash = 3f * _width;
		float _dot = 0.5f;
		float _dashDot = 6f * _width;
		
		float[] _dash1 = {_dash,_dash};
		float[] _dot1 = { _dot, _dash};
		
		float[] _dash_dot1 = {_dashDot,_dash,0.5f,_dash};
		float[] _dash_dot2 = {_dashDot, _dash, 0.5f ,_dash , 0.5f, _dash };
		
		if( _type.toUpperCase().equals("DASH"))
		{
			stroke = new BasicStroke(_width,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER , _dash,_dash1 ,0f);			
		}
		else if( _type.toUpperCase().equals("DASH_DOT") )
		{
			stroke = new BasicStroke(_width,BasicStroke.CAP_ROUND ,BasicStroke.JOIN_ROUND , 0.5f,_dash_dot1 ,0f);
		}
		else if( _type.toUpperCase().equals("DASH_DOT_DOT") )
		{
			stroke = new BasicStroke(_width,BasicStroke.CAP_ROUND ,BasicStroke.JOIN_ROUND , 0.5f,_dash_dot2 ,0f);			
		}
		else if( _type.toUpperCase().equals("DOT") )
		{
			stroke = new BasicStroke(_width,BasicStroke.CAP_ROUND ,BasicStroke.JOIN_ROUND , 0.5f,_dot1 ,0f);		
		}
		else if( _type.toUpperCase().equals("SOLID") || _type.toUpperCase().equals("DOUBLE") )
		{
			stroke = new BasicStroke(_width);
		}
		
		return stroke;
		
	}
	
	
	
	private ArrayList<HashMap<String, Object>> tbBorderSetting(ArrayList<String> _sides , ArrayList<String> _types , ArrayList<Integer> _colors , ArrayList<Integer> _widths , HashMap<String, Float> _rect , ArrayList<HashMap<String, Object>> _content, float _alpha , ArrayList<String> _originalTypes, ArrayList<Boolean> _beforeBorderType )
	{
		//이중선인경우 gab적용 여부를 위한 설정
    	Boolean _isDoubleTop = false;	
    	Boolean _isTopShare = false;	
    	Boolean _isDoubleLeft = false; 
    	Boolean _isLeftShare = false;	
    	Boolean _isDoubleRight = false;
    	Boolean _isRightShare = false;	
    	Boolean _isDoubleBottom = false;
    	Boolean _isBottomShare = false;	 
    	
    	float s1 = 0;
    	float s2 = 0;    	
    	float e1 = 0;
    	float e2 = 0;
    	
    	float ds1 = 0;
    	float ds2 = 0;    	
    	float de1 = 0;
    	float de2 = 0;
        	
    	float _defaltX = Float.parseFloat(_rect.get("left").toString());
    	float _defaltY = Float.parseFloat(_rect.get("top").toString());
    	float _defaltW = Float.parseFloat(_rect.get("right").toString());
    	float _defaltH = Float.parseFloat(_rect.get("bottom").toString());
    	 
    	float gap= ObjectToFloat(6);	
    	float doubleGap = 0;
		ArrayList<HashMap<String, Object>> rtnList = _content;
		HashMap<String, Object> borderSet;
		HashMap<String, Object> doubleBorderSet;
		HashMap<String, String> _doubleBorderSideMap = new HashMap<String, String>();
		int _sLength = _sides.size();
		
		for (int i = 0; i < _sLength; i++) 
		{
			String _side = _sides.get(i);
			String _type = _types.get(i);
			String _oriType = _type;
			
			if(_originalTypes != null && _originalTypes.size() > i-1) _oriType =  _originalTypes.get(i);
			
			if( "top".equals(_side) )  
			{
				if( _type.indexOf("double")>-1 || _oriType.equals("double") )
					_isDoubleTop = true;
				if(_type.indexOf("share")>-1)
					_isTopShare = true;
			}
			else if( "left".equals(_side) )
			{
				if( _type.indexOf("double")>-1 || _oriType.equals("double") )
					_isDoubleLeft = true;
				if(_type.indexOf("share")>-1)
					_isLeftShare = true;
			}
			else if( "right".equals(_side) )
			{
				if( _type.indexOf("double")>-1 || _oriType.equals("double") )
					_isDoubleRight = true;
				if(_type.indexOf("share")>-1 || (_oriType.equals("double") && !_type.equals(_oriType)))
					_isRightShare = true;
			}
			else if( "bottom".equals(_side) )
			{
				if( _type.indexOf("double")>-1 || _oriType.equals("double") )
					_isDoubleBottom = true;
				if(_type.indexOf("share")>-1  || (_oriType.equals("double") && !_type.equals(_oriType)))
					_isBottomShare = true;
			}

		}
		
		for (int i = 0; i < _sLength; i++) 
		{
			String _sp = "";
			String _ep = ""; 
			borderSet = new HashMap<String, Object>();
			
			String _side = _sides.get(i);
			String _type = _types.get(i);			
			Color _color = new Color(Integer.valueOf(String.valueOf(_colors.get(i))));
			String _oriType = _type;
			
			if(_originalTypes != null && _originalTypes.size() > i-1) _oriType =  _originalTypes.get(i);

			int _width = Integer.valueOf(String.valueOf(_widths.get(i)));
			
			if( _oriType.toUpperCase().equals("DOUBLE") || _type.toUpperCase().equals("DOUBLE"))
			{
				_doubleBorderSideMap.put(_side, _oriType);
			}
			
			if( _oriType.toUpperCase().equals("DOUBLE") == false && _type.equals("none")) 
			{
				continue; 
			}
			
			
			borderSet.put("width", _width);
			borderSet.put("color",_color); 
			borderSet.put("type",_type);
			borderSet.put("side",_side);
			borderSet.put("originalType",_oriType);
			borderSet.put("alpha", _alpha);
			borderSet.put("doubleBorderMap", _doubleBorderSideMap);
			
			
			if( _side.equals("top") && !_type.equals("none"))
			{
				if( _type.equals("double_share")){
					doubleGap = _isDoubleLeft && _isLeftShare ?  (gap/2):(_isDoubleLeft?gap:0); 
					s1 = _defaltX + doubleGap; 
					s2 = _defaltY + (gap/2); 					
					doubleGap = _isDoubleRight && _isRightShare ? (gap/2):(_isDoubleRight?gap:0); 
					e1 = _defaltW - doubleGap;
					e2 = _defaltY + (gap/2);
				}else{
					if( _isTopShare){//공유 실선 인경우
						doubleGap = _isLeftShare && _isDoubleLeft ?  (gap/2):(_isDoubleLeft?gap:0); 		
						if(_beforeBorderType != null && _beforeBorderType.get(1))doubleGap = 0;//left 변경인경우
						s1 = _defaltX + doubleGap;
						s2 =_defaltY;
						
						doubleGap = _isRightShare && _isDoubleRight ?  (gap/2):(_isDoubleRight?gap:0); 
						if(_beforeBorderType != null && _beforeBorderType.get(2))doubleGap = 0;//right 변경인경우
						e1 = _defaltW - doubleGap;
						e2 = _defaltY;
					}else{
						s1 = _defaltX; 
						s2 = _defaltY; 	
						e1 = _defaltW;
						e2 = _defaltY;
					}
					if(_type.equals("double")){
						doubleGap = _isDoubleLeft && _isLeftShare ?  (gap/2):(_isDoubleLeft?gap:0); 
						ds1 = _defaltX + doubleGap; 
						ds2 = _defaltY + gap; 	
						doubleGap = _isDoubleRight && _isRightShare ? (gap/2):(_isDoubleRight?gap:0);
						de1 = _defaltW - doubleGap;
						de2 = _defaltY + gap;
						
						doubleBorderSet = new HashMap<String, Object>();
						doubleBorderSet  = (HashMap<String, Object>)borderSet.clone(); 
						doubleBorderSet.put("sp",ds1 + "," + ds2);
						doubleBorderSet.put("ep",de1 + "," + de2);
						rtnList.add(doubleBorderSet);		
					}
				}
			}
			
			if( _side.equals("right")){
				if(!_type.equals("none")){
					
					s1 = _defaltW; 
					s2 = _defaltY; 	
					e1 = _defaltW;
					e2 = _defaltH;
					
					if(_type.equals("double")){
						doubleGap = _isDoubleTop && _isTopShare ? (gap/2):(_isDoubleTop?gap:0); 
						ds1 = _defaltW - gap; 
						ds2 = _defaltY + doubleGap; 	
						doubleGap = _isDoubleBottom && _isBottomShare ? (gap/2):(_isDoubleBottom?gap:0);
						de1 =  _defaltW - gap;
						de2 = _defaltH - doubleGap;	
						
						doubleBorderSet = new HashMap<String, Object>();
						doubleBorderSet  = (HashMap<String, Object>)borderSet.clone(); 
						doubleBorderSet.put("sp",ds1 + "," + ds2);
						doubleBorderSet.put("ep",de1 + "," + de2);
						rtnList.add(doubleBorderSet);		
					}
					
				}else if(_type.equals("none") && _oriType.equals("double") ){
					doubleGap = _isDoubleTop && _isTopShare ? (gap/2):(_isDoubleTop?gap:0); 
					s1 = _defaltW - (gap/2); 
					s2 = _defaltY + doubleGap; 
					doubleGap = _isDoubleBottom && _isBottomShare ? (gap/2):(_isDoubleBottom?gap:0); 					
					e1 = _defaltW - (gap/2);
					e2 =  _defaltH - doubleGap ;
					
					//none type => double 변경하여 넘김
					borderSet.put("type",_oriType);
				}
			}
			if( _side.equals("bottom")){
				if(!_type.equals("none")){					
					s1 = _defaltW; 
					s2 = _defaltH; 	
					e1 = _defaltX;
					e2 = _defaltH;
					
					if(_type.equals("double")){
						doubleGap = _isDoubleRight && _isRightShare ? (gap/2):(_isDoubleRight?gap:0);
						ds1 = _defaltW - doubleGap ; 
						ds2 = _defaltH - gap; 	
						doubleGap = _isDoubleLeft && _isLeftShare ? (gap/2):(_isDoubleLeft?gap:0); 
						de1 = _defaltX + doubleGap;
						de2 =  _defaltH - gap;						
						doubleBorderSet = new HashMap<String, Object>();
						doubleBorderSet  = (HashMap<String, Object>)borderSet.clone(); 
						doubleBorderSet.put("sp",ds1 + "," + ds2);
						doubleBorderSet.put("ep",de1 + "," + de2);
						rtnList.add(doubleBorderSet);		
					}
					
				}else if(_type.equals("none") && _oriType.equals("double") ){
					doubleGap = _isDoubleRight && _isRightShare ? (gap/2):(_isDoubleRight?gap:0); 
					s1 = _defaltW -doubleGap ; 
					s2 = _defaltH - (gap/2); 
					doubleGap = _isDoubleLeft && _isLeftShare ? (gap/2):(_isDoubleLeft?gap:0); 				
					e1 =  _defaltX+doubleGap;
					e2 =  _defaltH - (gap/2);					
					//none type => double 변경하여 넘김
					borderSet.put("type",_oriType);
				}
			}
			
			if( _side.equals("left") && !_type.equals("none"))
			{
				if( _type.equals("double_share")){
					doubleGap = _isDoubleBottom  && _isBottomShare ?(gap/2):(_isDoubleBottom?gap:0); 
					s1 = _defaltX + (gap/2); 
					s2 =  _defaltH - doubleGap; 					
					doubleGap = _isDoubleTop && _isTopShare ? (gap/2):(_isDoubleTop?gap:0); 
					e1 =  _defaltX + (gap/2);
					e2 = _defaltY + doubleGap;
				}else{
					if( _isLeftShare){//공유 실선 인경우
						doubleGap = _isDoubleBottom  && _isBottomShare ?(gap/2):(_isDoubleBottom?gap:0);	
						if(_beforeBorderType != null && _beforeBorderType.get(3))doubleGap = 0;//top 변경인경우
						s1 = _defaltX;
						s2 = _defaltH - doubleGap;						
						doubleGap = _isDoubleTop  && _isTopShare ?(gap/2):(_isDoubleTop?gap:0); 
						if(_beforeBorderType != null && _beforeBorderType.get(0))doubleGap = 0;//top 변경인경우
						e1 = _defaltX;
						e2 = _defaltY + doubleGap;
					}else{
						s1 = _defaltX; 
						s2 = _defaltH; 	
						e1 = _defaltX;
						e2 = _defaltY;
					}
					if(_type.equals("double")){
						doubleGap = _isDoubleBottom  && _isBottomShare ?(gap/2):(_isDoubleBottom?gap:0); 
						ds1 =  _defaltX + gap; 
						ds2 = _defaltH - doubleGap; 	
						doubleGap = _isDoubleTop && _isTopShare ? (gap/2):(_isDoubleTop?gap:0); 
						de1 = _defaltX + gap;
						de2 = _defaltY + doubleGap;						
						
						doubleBorderSet = new HashMap<String, Object>();
						doubleBorderSet  = (HashMap<String, Object>)borderSet.clone(); 
						doubleBorderSet.put("sp",ds1 + "," + ds2);
						doubleBorderSet.put("ep",de1 + "," + de2);
						rtnList.add(doubleBorderSet);		
					}
				}
			}
			
			_sp = s1 + "," + s2;
			_ep = e1 + "," + e2;
			borderSet.put("sp",_sp);
			borderSet.put("ep",_ep);
			rtnList.add(borderSet);			
		}
		return rtnList;
	}
	

/*	
	private float drawColumnText(PdfContentByte canvas,float _x,float _y ,float _w ,float _h, Paragraph p, boolean simulate) throws DocumentException {
        ColumnText _columnText = new ColumnText(canvas);
        _columnText.setSimpleColumn(_x,_y,_w,_h);
        _columnText.addElement(p);
        _columnText.setRunDirection(PdfWriter.RUN_DIRECTION_LTR);
        _columnText.go(simulate);
        return _columnText.getYLine();
    }
	
	
	private int drawColumnTextLine(PdfContentByte canvas,float _x,float _y ,float _w ,float _h, Paragraph p, boolean simulate) throws DocumentException {
        ColumnText _columnText = new ColumnText(canvas);
        _columnText.setSimpleColumn(_x,_y,_w,_h);
        _columnText.addElement(p);
        _columnText.setRunDirection(PdfWriter.RUN_DIRECTION_LTR);
        _columnText.go(simulate);
        return _columnText.getLinesWritten();
    }
	
	
	
	private void drawColumnRotateText(PdfContentByte canvas,float _x,float _y , Paragraph p, int _rotate) throws DocumentException {
		ColumnText _columnText = new ColumnText(canvas);
		_columnText.showTextAligned(canvas, p.getAlignment(), p, _x, _y, _rotate);
		_columnText.setRunDirection(PdfWriter.RUN_DIRECTION_LTR);
		_columnText.go();
	}
*/
	
	private HashMap<String, Object> convertScaleItem( HashMap<String, Object> _item, double _pWidth, double _pHeight )
	{
		String[] _chnageProperty = {"width","height","x","y","x1","y1","x2","y2","fontSize"};
		String _className = ValueConverter.getString(_item.get("className"));
		
		_pWidth = (_pWidth / 72 * 96);
		_pHeight =  (_pHeight / 72 * 96);
		
		int _changeCnt = _chnageProperty.length;
		double _itemFloat = 0;
		
		
		for (int k = 0; k < _changeCnt; k++) {
			if( _item.containsKey(_chnageProperty[k]) && _item.get(_chnageProperty[k]) != null )
			{
				
				_itemFloat = Double.parseDouble( _item.get(_chnageProperty[k]).toString() );
				
				if( _chnageProperty[k] == "x" || _chnageProperty[k] == "x1" || _chnageProperty[k] == "x2" )
				{
					_itemFloat = _itemFloat * mScale;
					_itemFloat =  Math.floor(_itemFloat);
					if( _chnageProperty[k] == "x" ){
						_itemFloat = _itemFloat + _pWidth;
					}
					
				}
				else if( _chnageProperty[k] == "y" || _chnageProperty[k] == "y1" || _chnageProperty[k] == "y2" )
				{
					_itemFloat = _itemFloat * mScale;
					if( _chnageProperty[k] == "y" ){
						_itemFloat = _itemFloat + _pHeight;	
					}
				}
				else if( _chnageProperty[k] == "width")
				{
					_itemFloat = (_itemFloat + Double.parseDouble(_item.get("x").toString())) * mScale;
					Double xTemp = Math.floor(Double.parseDouble(_item.get("x").toString()) * mScale);
					_itemFloat =  Math.floor(_itemFloat) - xTemp;
					
//					_itemFloat = _itemFloat; 
				}
				else if( _chnageProperty[k] == "height")
				{
					_itemFloat = _itemFloat * mScale;
//					_itemFloat = _itemFloat;
				}
				else
				{
					_itemFloat = _itemFloat * mScale; 
				}
				
				_item.put(_chnageProperty[k], _itemFloat);
			}
		}
		
		return _item;
	}
	
	
	
	public void setChangeColor(Color color){
		this.mg2.setColor(color);
	}	
	
	
	
}
