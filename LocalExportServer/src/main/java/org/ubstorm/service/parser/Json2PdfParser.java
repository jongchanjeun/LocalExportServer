package org.ubstorm.service.parser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.apache.tools.ant.filters.StringInputStream;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubstorm.service.dictionary.ImageDictionary;
import org.ubstorm.service.dictionary.ImageDictionaryVO;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.utils.Base64Coder;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;

import com.lowagie.text.Annotation;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.ImgTemplate;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.SplitCharacter;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfChunk;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfShading;
import com.lowagie.text.pdf.PdfShadingPattern;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RadioCheckField;

public class Json2PdfParser {
	private static final Logger log = LoggerFactory.getLogger(Json2PdfParser.class);
	private String mPDF_PRINT_VERSION = "2.0";		//PDF 내보내기시 Version 처리 ( 1.0 기존 버전 / 2.0 폰트 수정 버전 )
	
	// pdf
	private Document mDocument = new Document();
	private PdfWriter mWriter;
	
	private float mPageHeight = 0;
	private float mPageWidth = 0;
	
	private String mWaterMarkTxt = "";
	
	private float mMarginX = 0;
	private float mMarginY = 0;
	
	private float mScale = 1; 
	
	//private ByteArrayOutputStream mBaos;
	private FileOutputStream mBaos;	
	
	private FileOutputStream mFileOutputstream;
	
	private Font mDefFont;
	private BaseFont mBaseFont;
	
	private ArrayList<String> mFontListAr;
	
	private HashMap<String, ArrayList<HashMap<String, Object>>> mTBborder;
	
	private String licenseType = "";
	private String pdfFilePath = "";
	
	private HashMap<String, Object> mChangeItemList;
	
	private ImageDictionary mImageDictionary;
	
	private ArrayList<String> mFilePaths = new ArrayList<String>();
	
	private ArrayList<InputStream> mFileList = new ArrayList<InputStream>();
	
	public HashMap<String, Object> getChangeItemList() {
		return mChangeItemList;
	}

	public void setChangeItemList(HashMap<String, Object> value) {
		this.mChangeItemList = value;
	}

	public void init(String filename, String licensetype, String pageWidth, String pageHeight, String usePrnDialog) throws DocumentException, IOException
	{
		//this.pdfFilePath = filename;
		this.licenseType = licensetype;
		
		if(filename != null && filename.length() > 0)
		{
		}
		else
		{
			String _userHome = System.getProperty("user.home");
			filename = _userHome + "/Downloads/ubformLocal.pdf";	
		}
		
		this.pdfFilePath = filename;
		
		//mBaos = new ByteArrayOutputStream();	
		try
		{
			mBaos = new FileOutputStream(filename);	
		}
		catch(FileNotFoundException exp)
		{
			int dRandom = (int) getCountUbLocalPdfFiles(filename);			
			filename = filename + "_" + dRandom + ".pdf";
			this.pdfFilePath = filename;
			
			mBaos = new FileOutputStream(filename);
		}
		
		mFilePaths.add(this.pdfFilePath);
		//mFileList.add(new FileInputStream(this.pdfFilePath));
		
		//mFileOutputstream = new FileOutputStream(filename);
		
		mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		// 파라미터의 사이즈값이 존재할경우 맞춰서 width와 height값을 늘리고 mMarginX와 mMarginY값을 세팅
		
		// pdf Page , Document, BaseFont, font 셋팅
		JSONObject _hm = new JSONObject();
		_hm.put("pageWidth", pageWidth);
		_hm.put("pageHeight", pageHeight);
		_hm.put("usePrintDialog", usePrnDialog);
		
		pdfPagePropertySetting( _hm, true, 0 );
		mFontListAr = new ArrayList<String>(FontFactory.getRegisteredFonts());
		
		
		//워터 마크
		if(!mWaterMarkTxt.equals(""))
		{
			mWriter.setPageEvent(new Watermark());
		}
		// license
		if( this.licenseType != null)
		{
			if( !"free".equalsIgnoreCase(this.licenseType) ) 
			{
				mWriter.setPageEvent(new License(this.licenseType));
			}
		}		
		
		mDocument.open();
	}
	
	private int getCountUbLocalPdfFiles(String filePath)
	{
		int count = 0;
		
		File[] files = null;
		File f = new File( filePath );
		if(f.isDirectory())
		{
			files = f.listFiles();
		}
		else
		{
			files = new File(f.getParent()).listFiles();
		}

		// files
		String _curFileName = "";
		for (int i = 0; i < files.length; i++) 
		{
			if ( files[i].isFile() ) {
				
				_curFileName = files[i].getName();
				if(_curFileName.contains("ubformLocal.pdf"))
				{
					System.out.println( "파일 : " + files[i].getName() );
					count++;
				}
			} else {
				System.out.println( "디렉토리명 : " + files[i].getName() );
			}
		} // end of for
		
		return count;
	}
	
	private void pdfPagePropertySetting( JSONObject _hashData, Boolean usePrint, int _circulation ) throws DocumentException, IOException
	{
		float _pWidth = 974.0f;
		float _pHeight = 1123.0f;
		int _bgColorInt = 0;
		mWaterMarkTxt = "";		
		
		_pWidth = convertDpiFloat(_hashData.get("pageWidth"));
		_pHeight = convertDpiFloat(_hashData.get("pageHeight"));
		
		mPageWidth = _pWidth;
		mPageHeight = _pHeight;
		
		Rectangle _pageSize = new Rectangle(_pWidth, _pHeight);
		
		/*
		Rectangle _pageSize = PageSize.A4;
		
		mPageWidth = _pageSize.getWidth();
		mPageHeight = _pageSize.getHeight();	
		*/
		
		if(mScale < 1)
		{
			mMarginX = (_pWidth-(_pWidth*mScale))/2;
			mMarginY = (_pHeight-(_pHeight*mScale))/2;
		}
		
		//Color _pageBgColor = new Color(_bgColorInt);
		_pageSize.setBackgroundColor(Color.WHITE);
		
		mDocument = new Document(_pageSize, 0, 0, 0, 0);
		mDocument.compress = true;
		
		mWriter = PdfWriter.getInstance(mDocument, mBaos);
		
		
		// bcprov-jdk14-124.jar 사용필요 , pdf
//		mWriter.setEncryption("123456".getBytes("UTF-8"), "123456".getBytes("UTF-8"), PdfWriter.AllowScreenReaders, PdfWriter.ENCRYPTION_AES_128 | PdfWriter.DO_NOT_ENCRYPT_METADATA);
		
		//TEST 문서 오픈시 바로 인쇄를 위한 액션 추가 테스트 최명진
		/**
		 */
		if(usePrint)
		{
			
			String _useFitWindow = "true";
			String _useUiDialog = "true";
			
			/*
			if( _hashData.containsKey("printOption") )
			{
				HashMap<String, String> _printOption = (HashMap<String, String>) _hashData.get("printOption");
				if(_printOption.containsKey(GlobalVariableData.UB_PRINT_FIT_SIZE))
				{
					_useFitWindow = _printOption.get(GlobalVariableData.UB_PRINT_FIT_SIZE);
				}
				if(_printOption.containsKey(GlobalVariableData.UB_PRINT_USE_UI))
				{
					_useUiDialog = _printOption.get(GlobalVariableData.UB_PRINT_USE_UI);
				}
			}
			*/
			_useUiDialog = (String) _hashData.get("usePrintDialog");

			PdfAction action = new PdfAction( PdfAction.PRINTDIALOG);
			action.put(PdfName.JS, new PdfString("this.print({bUI: "+_useUiDialog+", bSilent: true, bShrinkToFit: " + _useFitWindow + "});\r"));
			// bShrinkToFit : true = 맞추기, false = 실제크기
			// bSilent : true = 바로인쇄, false = 사용자 반응확인 
			// bUI : true = PRINT Dialog 사용, false = PRINT Dialog 미사용 
			mWriter.setOpenAction(action);
			mWriter.addViewerPreference(PdfName.NUMCOPIES, new PdfNumber( ValueConverter.getString(_circulation) ));
		}
		
		//mBaseFont = BaseFont.createFont("HYGoThic-Medium", "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED);
		//mDefFont = new Font(mBaseFont, 10, Font.NORMAL, Color.black);
		mDefFont = FontFactory.getFont("맑은 고딕",BaseFont.IDENTITY_H,10,Font.NORMAL,Color.black);
	}
	
	
	
	public String getPdfFilePath()
	{
		return this.pdfFilePath;
	}
	
	public ArrayList<String> getPdfFilePaths()
	{
		return this.mFilePaths;
	}
	
	public ArrayList<InputStream> getPdfFileList()
	{
		return this.mFileList;
	}
	
	public void makePdfPage( JSONObject _pageData, String _pageIndex) throws DocumentException, MalformedURLException, IOException
	{
		//float _pWidth = convertDpiFloat(mPageWidth);
		//float _pHeight = convertDpiFloat(mPageHeight);
		
		float _pWidth = mPageWidth;
		float _pHeight = mPageHeight;
			
		ArrayList<HashMap<String, Object>> _itemList = (ArrayList<HashMap<String,Object>>) _pageData.get("pageData");
		ArrayList<HashMap<String, Object>> _itemList2 = new ArrayList<HashMap<String,Object>>();
		
		if(_itemList != null)
		{
			if( _itemList.size() <= 0)
			{
				PdfContentByte _contentByte = mWriter.getDirectContent();
	            ColumnText.showTextAligned(_contentByte, -1 , new Phrase(""), 0, 0, 0);
	            // New Page
				mDocument.newPage();
				return;
			}
			
			// 페이지 변환시 changeList에 값이 있을경우 처리 
			if(mChangeItemList != null && mChangeItemList.size() > 0)
			{
				testPdfChangeMapping(mChangeItemList, _pageData, _pageIndex);
			}
			
			_itemList2.add(_itemList.get(0));
			
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
						
						CreatePdfLabel( _item );
					}
					else if( _className.equals("UBCheckBox") )
					{
						CreatePdfCheckBox( _item );
					}
					else if( _className.equals("UBRadioBorder") )
					{
						CreatePdfRadioGroup( _item ); // 함수 안에서 수정 더 해야 함.	e-Form
					}
					else if( _className.equals("UBImage") )
					{
						CreatePdfImage( _item );
					}
					else if( _className.equals("UBPicture") )
					{
						CreatePdfSign( _item );
					}
					else if( _className.equals("UBSignature") )
					{
						CreatePdfSign( _item ); // signature 이미지 확인 해야 함.	e-Form
					}
					else if( _className.equals("UBGraphicsLine") )
					{
						CreatePdfLine( _item );
					}
					else if( _className.equals("UBGraphicsCircle") )
					{
						CreatePdfCircle( _item );
					}
					else if( _className.equals("UBGraphicsRectangle") )
					{
						CreatePdfRectangle( _item );
					}
					else if( _className.equals("UBGraphicsGradiantRectangle") )
					{
						CreatePdfGradiantRectangle( _item );
					}
					else if( _className.equals("UBClipArtContainer") )
					{
						CreatePdfClipImage( _item );
					}
					else if( _className.equals("UBSVGArea") )
					{
						CreatePdfSVGArea( _item );
					}
					else if( _className.equals("UBSVGRichText") )
					{
						CreatePdfSVGRichTextLabel( _item );
					}
					else if( _className.equals("UBPresentaionGraphic") ) // 그리기 부분.
					{
						
					}
					else if( _className.equals("UBConnectLine") )
					{
						//CreatePdfConnectLine( _item );
					}
					else if( _className.indexOf("Code") != -1 || _className.indexOf("Chart") != -1 )
					{
						CreatePdfDoImage( _item );
					}
					
				}// if _item.containsKey("className")
				
			}// for _itemList.size()
			
//			TestPDF();
			
			// 라이센스
			
			// New Page
			
			if( mTBborder.size() > 0) // 페이지 넘기기전 보더를 안그린게 있으면 그린다.
			{
				setTBborderSetting();
				mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
			}
			
			Rectangle _newPSize = new Rectangle(_pWidth, _pHeight);
			
			mDocument.setPageSize(_newPSize);
			mDocument.setMargins(0, 0, 0, 0);
			
			mDocument.newPage();
			
			// OutOfMemoryError 발생을 방지하기 위해 곧바로 사용한 페이지정보를 지운다.
			//_pageHm.remove(_key);
			try {
				if(Thread.currentThread().interrupted() == false) Thread.currentThread().sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println("flushToPdf::mWriter.getCurrentDocumentSize()====>" + mWriter.getCurrentDocumentSize());
		}
			
	}
	
	
	public void flushToPdf()
	{
		try {
			//mFileOutputstream.write(mBaos.toByteArray());
			mBaos.flush();
			//mFileOutputstream.flush();
			//mBaos.reset();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void endToPdf() 
	{
		try {
			//if(mDocument.isOpen())
				mDocument.close();
			
			//mFileOutputstream.write(mBaos.toByteArray());
			//mBaos.flush();
			//mFileOutputstream.flush();
			//mFileOutputstream.close();
			mBaos.close();
			mWriter.close();
			mDocument = null;
			
			gc();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
    * This method guarantees that garbage collection is
    * done unlike <code>{@link System#gc()}</code>
    */
   public static void gc() {
     Object obj = new Object();
     @SuppressWarnings("unchecked")
	WeakReference ref = new WeakReference(obj);
     obj = null;
     while(ref.get() != null) {
       System.gc();
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
			_aInt = Element.ALIGN_LEFT;
		}
		else if( _align.equals("center") )
		{
			_aInt = Element.ALIGN_CENTER;
		}
		else if( _align.equals("right") )
		{
			_aInt = Element.ALIGN_RIGHT;
		}
		else
		{
			_aInt = Element.ALIGN_UNDEFINED;
		}
		
		return _aInt;
	}
	
	
	
	
	private Integer fontStyleToInt( Object _italic , Object _bold , Object _underLine)
	{
		int _style = 0;
		
		if( !_underLine.equals("none"))
		{
			_style += Font.UNDERLINE;
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
		String _rtnFontName = "";
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
	
	private HashMap<String, Float> rotationItem( PdfContentByte _canvas , float _x , float _y , float _w , float _h , float _rotation )
	{
		HashMap<String, Float> _retHm = new HashMap<String, Float>();
		double pi = _rotation * Math.PI / 180;
		float cos = (float) Math.cos(pi);
		float sin = (float) Math.sin(pi);
		
		_canvas.concatCTM(cos, sin, -sin, cos, 0, 0);
		
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
	
	
	/**
	 * 
	 * @param colorStr e.g. "#FFFFFF"
	 * @return 
	 */
	private Color hex2Rgb(String colorStr) {
	    return new Color(
	            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
	}	
	
	@SuppressWarnings("unchecked")
	private void CreatePdfLabel( HashMap<String, Object> _item ) throws DocumentException, UnsupportedEncodingException
	{
		float _x = ObjectToFloat( _item.get("x") );
		float _y = mPageHeight - ObjectToFloat( _item.get("y") );
		float _w = ObjectToFloat( ValueConverter.getFloat(_item.get("x")) + ValueConverter.getFloat(_item.get("width")) );
		float _h = mPageHeight - ObjectToFloat( ValueConverter.getFloat(_item.get("y")) + ValueConverter.getFloat(_item.get("height")) );
		
		String _id = ValueConverter.getString(_item.get("id"));
		String _type = ValueConverter.getString(_item.get("type"));
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		PdfGState _gState = new PdfGState();
		Rectangle _bRect;
		boolean _verticalRotateFlg = false;
		
		_contentByte.saveState();
		
		
		// background 
		float _bgAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		//Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));
		Color _bgColor = hex2Rgb(ValueConverter.getString(_item.get("backgroundColor")));
		float _alpha =  ValueConverter.getFloat(_item.get("alpha"));
		
		// border
		HashMap<String, Float> _rect = new HashMap<String, Float>();
		ArrayList<HashMap<String, Object>> _content;
		
		HashMap<String, Float> _rotateDef = new HashMap<String, Float>();
		
		// text
		int _align = getAlignmentInt( ValueConverter.getString(_item.get("textAlign")) );
		String _verticalAlign = ValueConverter.getString(_item.get("verticalAlign"));
//		int _txStyle = fontStyleToInt(_item.get("fontStyle"), _item.get("fontWeight"), _item.get("textDecoration"));
		int _txStyle = fontStyleToInt(_item.get("fontStyle"), "normal", "none");
//		float _fontSize = 11f;
		
		float _fontSize = ObjectToFloat(_item.get("fontSize"))*0.9f;
		float _lineHeight = Float.parseFloat(_item.get("lineHeight").toString());
		
		
		if(mPDF_PRINT_VERSION.equals( GlobalVariableData.UB_PDF_PRINT_VERSION_1 ) )
		{
			if( Float.valueOf(_item.get("fontSize").toString()) <= 12 )_fontSize =  Double.valueOf( Math.ceil( _fontSize ) ).floatValue();
		}
		else
		{
			// PontSize를 PT사이즈로 지정
			_fontSize = Double.valueOf(Float.valueOf(_item.get("fontSize").toString()) * 72 /  96 ).floatValue();
		}
		
		
		//Color _txColor = new Color(ValueConverter.getInteger(_item.get("fontColorInt")));
		Color _txColor = hex2Rgb(ValueConverter.getString(_item.get("fontColor")));
		String _fontName = ValueConverter.getString(_item.get("fontFamily"));
		String _fName = getDefFont(_fontName);
//		log.info(getClass().getName() + "CreatePdfLabel ===> " + "_fontName : " + _fontName + ", _fName : " + _fName);

		if( _fName.equals("") )
		{
			if( mFontListAr.indexOf("맑은 고딕") != -1 )
			{
				mDefFont = FontFactory.getFont("맑은 고딕", BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
			}
			else
			{
				mDefFont = new Font(mBaseFont, _fontSize, _txStyle , _txColor);
			}
		}
		else
		{
			mDefFont = FontFactory.getFont(_fName, BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
		}

		String _itemText = ValueConverter.getString(_item.get("text"));
		
		// 줄바꿈 처리
		_itemText = _itemText.replace("\\n", "\n").replaceAll("\\r", "\r");
		
//		_itemText = _itemText.replace(" ", "|");
//		Phrase _txt = new Phrase(_itemText, mDefFont);
//		Paragraph _txtp = new Paragraph(_txt);

		Chunk _chunk = new Chunk(_itemText, mDefFont);
		// 자동 줄바꿈시 문자단위로 줄바꿈 되지 않도록 하기 위하여 SplitCharacter 지정
		_chunk.setSplitCharacter(new SplitCharacter() {
			
			@Override
			public boolean isSplitCharacter(int start, int current, int end, char[] cc,
					PdfChunk[] ck) {
		        return false;
			}
		});
		if( ValueConverter.getString(_item.get("fontWeight")).equals("normal") == false ) // bold 의 두깨를 조절.
		{
			float _blodNum = (Math.round( ( _fontSize / 35f ) * 10f ) / 10f );
			_chunk.setTextRenderMode( PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE , _blodNum, _txColor);
		}
		
		Paragraph _txtp = new Paragraph(_chunk);		 
		 
		//_txtp.setLeading(_fontSize * 1.16f);		
		_txtp.setLeading(_fontSize *_lineHeight);
		
		if(mPDF_PRINT_VERSION.equals( GlobalVariableData.UB_PDF_PRINT_VERSION_1 ) )
		{
			if( _fontSize >= 60)
			{
				_txtp.setLeading(_fontSize * 0.7f);
			}
			else if( _fontSize > 15 )
			{
				_txtp.setLeading(_fontSize * 0.8f);
			}
			else
			{
				_txtp.setLeading(_fontSize * 1.2f);	
			}
		}
		else
		{
//			float _readingSize = (float) Math.floor( _fontSize * 1.2f );			
			//float _readingSize = _fontSize * 1.2f;
			float _readingSize = _fontSize * _lineHeight;
			// 소숫점이 유실되어 소숫점 1자리까지 처리하도록 수정.
			_readingSize = (float) (Math.floor(_readingSize*10f) / 10f);
			_txtp.setLeading( _readingSize); 
		}
		
		_txtp.setAlignment(_align);

		// label type 구분.
		if(_type.equals("stretchLabel"))
		{
			HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
			
			optionMap.put("width", new float[]{ObjectToFloat(_item.get("width"))});
			optionMap.put("height",new float[]{ mPageHeight , mPageHeight });
			optionMap.put("fontSize", new float[]{_fontSize});
			//optionMap.put("lineHeight", new float[]{1.2f});
			optionMap.put("lineHeight", new float[]{ObjectToFloat(_item.get("lineHeight"))}); 
			
			if( _item.containsKey("padding") == false ){
				optionMap.put("padding", new float[]{ 3 });
			}else{
				optionMap.put("padding", new float[]{ObjectToFloat( _item.get("padding"))});
			}
			
			HashMap<String,Object> resultMap = StringUtil.getSplitCharacter(_itemText, optionMap ,_item.get("fontWeight").toString(),_item.get("fontFamily").toString(),  ObjectToFloat(_item.get("fontSize")), -1 );
			
			 ArrayList<String> _resultTextAr = (ArrayList<String>) resultMap.get("Text");
			 ArrayList<Float> _resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
			 
			 String _txd = _resultTextAr.get(0);
			 float _txh = _resultHeightAr.get(0);
			 
			 
			 if( _txh > 0f)
			 {
				 _h = _y - _txh;
				 _txd = _txd + "zzz";
			 }

			 if( _txh > 5f && _txh < 10f )
			 {
				 _y = _y + 2f;
			 }
			 
		}
		else if ( _type.equals("rotateLabel"))
		{
			if( !_item.get("textRotate").equals(0))
			{
				_verticalRotateFlg = true;
				float _rotateNum = 0;
				
				//_rotateNum = ValueConverter.getInteger(_item.get("textRotate"));
				_rotateNum = ValueConverter.getFloat(_item.get("textRotate"));
				
				_rotateDef.put("x", _x);
				_rotateDef.put("y", _y);
				_rotateDef.put("w", _w);
				_rotateDef.put("h", _h);
				
				HashMap<String, Float> _posionData = rotationItem(_contentByte, _x, _y, _w, _h, _rotateNum * -1);
				
				_x = _posionData.get("x");
				_y = _posionData.get("y");
				_w = _posionData.get("w");
				_h = _posionData.get("h");
			}
		}
		else
		{
			
		}
		
	//  테이블의 Border의 Right값이 없으면 Width를 borderWidth값만큼 빼고 border의 bottom값이 없을경우 height값의 borderWidth값만큼 빼기 2016-03-22
		/**
//		if( _id.split("_")[0].equals("TB"))
		if(_item.containsKey("ORIGINAL_TABLE_ID") && _item.get("ORIGINAL_TABLE_ID").equals("") == false )
		{ 
			ArrayList<String> _tbBorderTypeTemp = (ArrayList<String>) _item.get("borderTypes");
			ArrayList<String> _tbBorderSideTemp = (ArrayList<String>) _item.get("borderSide");
			ArrayList<Integer> _tbBorderWidthsTemp = (ArrayList<Integer>) _item.get("borderWidths");
			for (int i = 0; i < _tbBorderTypeTemp.size(); i++) {
				
				float _widthF = ObjectToFloat(_tbBorderWidthsTemp.get(i));
				_widthF = _widthF - 0.3f;
				
				if(_tbBorderSideTemp.get(i).equals("right") && _tbBorderTypeTemp.get(i).equals("none"))
				{
					_w = _w - _widthF;
				}
				else if(_tbBorderSideTemp.get(i).equals("bottom") && _tbBorderTypeTemp.get(i).equals("none"))
				{
					_h = _h + _widthF;
				}
				
			}
		}
		*/
		
		_bRect = new Rectangle(_x, _y, _w, _h);
		
		_bRect.setBackgroundColor(_bgColor);
		_gState.setFillOpacity(_bgAlpha*_alpha);
		_contentByte.setGState(_gState);
		
		_bRect.setBorder(0);
		_contentByte.rectangle(_bRect);
		
		_rect.put("top", _y);
		_rect.put("left", _x);
		_rect.put("right", _w);
		_rect.put("bottom", _h);
		
//		if( _id.split("_")[0].equals("TB"))
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
			
			mTBborder.put(_tbId, tbBorderSetting((ArrayList<String>) _item.get("borderSide") , (ArrayList<String>) _item.get("borderTypes") , (ArrayList<String>) _item.get("borderColors") , (ArrayList<Integer>) _item.get("borderWidths") , _rect, _content , _alpha ));
		}
		else
		{
			_contentByte = setBorderSetting(_contentByte , (ArrayList<String>) _item.get("borderSide") , (ArrayList<String>) _item.get("borderTypes") , (ArrayList<String>) _item.get("borderColors") , (ArrayList<Long>) _item.get("borderWidths") , _rect , _alpha );
		}
		_contentByte.restoreState();
		
		_contentByte.saveState();
	
		if( !_type.equals("rotateLabel") && (_y - _h) < _txtp.getLeading() )
		{
			_txtp.setLeading((_y-_h) - 1f);
		}
		
		if( _type.equals("rotateLabel"))
		{
			if( !_item.get("textRotate").equals(0))
			{
				_verticalRotateFlg = true;
				float _rotateNum = 0;
				
				_rotateNum = ValueConverter.getFloat(_item.get("textRotate"));
				
				HashMap<String, Float> _posionData = rotationItem(_contentByte, _rotateDef.get("x"), _rotateDef.get("y"), _rotateDef.get("w"), _rotateDef.get("h"), _rotateNum * -1);
				
				_x = _posionData.get("x");
				_y = _posionData.get("y");
				_w = _posionData.get("w");
				_h = _posionData.get("h");
			}
		}
/*
		float _padding = ObjectToFloat(3);
		if( _item.containsKey("padding") )
		{
			
			if( ObjectToFloat(_item.get("padding")) != 0 )
			{
				_padding = _padding + ObjectToFloat(_item.get("padding"));
			}
			else
			{
				_padding = ObjectToFloat(1);
			}
		}
*/		
		// padding 값이 크게 들어가서 기본패딩 들어있던 부분 생략. 2016-09-26 공혜지
		float _padding = ObjectToFloat(0.5);
//		float _padding = ObjectToFloat(0.0f);
		if( _item.containsKey("padding") && ObjectToFloat(_item.get("padding")) != 0 )
		{
			_padding = _padding + ObjectToFloat(_item.get("padding"));
		}
		
		_x = _x + _padding;
		_w = _w - _padding;
		
		float _fontY = drawColumnText( _contentByte, _x, _y, _w, _h,  _txtp, true);
		
		float _cX = _x;
		float _cY = _y;
		float _cW = _w;
		float _cH = _h;
		
		
		int _lineCount = drawColumnTextLine(_contentByte, _x, _y, _w, _h,  _txtp, true);
		if( _lineCount == 1 ){
			_txtp.setLeading(_fontSize);	
		}
		
		
		if( !ValueConverter.getString(_item.get("textDecoration")).equals("none") )
		{
//			_chunk.setUnderline(1f,-3f);
			float _thickness = (_fontSize/2) / 10;
			if( _thickness < 0.8f ) _thickness = 0.8f;
			_chunk.setUnderline(_txColor, _thickness, 0f, 0.5f, -0.2f ,PdfContentByte.LINE_CAP_BUTT);
		}
		
		String _hyperLinkUrl = "";
		if( _item.get("ubHyperLinkUrl") != null ){
			_hyperLinkUrl = _item.get("ubHyperLinkUrl").toString();
		}
		if( !_hyperLinkUrl.endsWith("null") && _hyperLinkUrl.length() > 0 ){
			_hyperLinkUrl = URLDecoder.decode(_hyperLinkUrl, "UTF-8");
			_chunk.setAnchor(_hyperLinkUrl);	
		}
		
	
		if( !_verticalAlign.equals("") && !_verticalAlign.equals("top") && !_type.equals("stretchLabel") )
		{
			if( !_verticalRotateFlg )
			{
				if( _verticalAlign.equals("middle"))
				{
					_cY = _cY - ((_fontY - _cH) / 2 );
				}
				else if( _verticalAlign.equals("bottom"))
				{
					_cY = (_cY - (_fontY - _cH));
					_cY = _cY + 3f;
				}
				_cY = _cY + 2f;
			}
			else
			{
				
				float _p1 = _cY - _fontY;
				float _p2 = _fontY - _cH;
				
				if( _verticalAlign.equals("middle"))
				{
					_cH = _cH + ( _p1 / 2 );
				}
				else if( _verticalAlign.equals("bottom"))
				{
					_cH = _cH + _p1;
					_cH = _cH + 3f;
				}
				_cH = _cH + 2f;
			}
		}
	
		
		drawColumnText( _contentByte, _cX, _cY, _cW, _cH, _txtp, false);
		
		_contentByte.restoreState();
	}
	
	private int mCNum = 0;
	private void CreatePdfCheckBox( HashMap<String, Object> _item ) throws DocumentException, IOException
	{
		
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = _x + ObjectToFloat(_item.get("width"));
		float _h = _y - ObjectToFloat(_item.get("height"));
		
		// background 
		float _bgAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		//Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));
		Color _bgColor = hex2Rgb(ValueConverter.getString(_item.get("backgroundColor")));
		
		PdfGState _gState = new PdfGState();
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		_contentByte.saveState();
		
		Rectangle _box = new Rectangle(_x,_y,_w,_h);
		_box.setBackgroundColor(_bgColor);
		_gState.setFillOpacity(_bgAlpha);
		_contentByte.setGState(_gState);
		_box.setBorder(0);
		_contentByte.rectangle(_box);
		_contentByte.restoreState();
		
		_contentByte.saveState();
		
		HashMap<String, Float> _rHm = new HashMap<String, Float>();
		_rHm.put("top", _y);
		_rHm.put("left", _x);
		_rHm.put("right", _w);
		_rHm.put("bottom", _h);
		
		_contentByte = setBorderSetting(_contentByte , (ArrayList<String>) _item.get("borderSide") , (ArrayList<String>) _item.get("borderTypes") , (ArrayList<String>) _item.get("borderColors") , (ArrayList<Long>) _item.get("borderWidths") , _rHm);
		
		String _label = ValueConverter.getString(_item.get("label"));
		
		boolean _selected = ValueConverter.getBoolean(_item.get("selected"));
		
		float _chY = _y - ( (_y - _h) / 2f);
		
		Rectangle _border = new Rectangle(_x + 2 , _chY + 5, _x + 12, _chY - 5);
		
		RadioCheckField _checkbox = new RadioCheckField(mWriter, _border, _label,"Yes");
		
		_checkbox.setBorderColor(new Color(0,0,0));
		_checkbox.setBorderStyle(0);
		_checkbox.setBorderWidth(1f);
		_checkbox.setCheckType(RadioCheckField.TYPE_CHECK);
		_checkbox.setChecked(_selected);
		_checkbox.setOptions(RadioCheckField.READ_ONLY);
		
		PdfFormField field = _checkbox.getCheckField();
		
		String _itemId = "";
		
		_itemId = _item.get("id") == null ? ("check_" + mCNum) : ValueConverter.getString(_item.get("id")) + "_" + mCNum; 
		
		field.setFieldName(_itemId);

		mWriter.addAnnotation(field);
		mCNum++;
		
		
		int _txStyle = 0;
		float _fontSize = 0;
		String _verticalAlign = ValueConverter.getString(_item.get("verticalAlign"));
		_txStyle = fontStyleToInt(_item.get("fontStyle"), _item.get("fontWeight"), _item.get("textDecoration"));
		_fontSize = ObjectToFloat(_item.get("fontSize"));
		//Color _fontColor = new Color(ValueConverter.getInteger( _item.get("fontColorInt")));
		Color _fontColor = hex2Rgb(ValueConverter.getString(_item.get("fontColor")));
		String _fontName = ValueConverter.getString(_item.get("fontFamily"));

		String _fName = getDefFont(_fontName);
		
		if( _fName.equals("") )
		{
			if( mFontListAr.indexOf("맑은 고딕") != -1 )
			{
				mDefFont = FontFactory.getFont("맑은 고딕", BaseFont.IDENTITY_H , _fontSize, _txStyle, _fontColor);
			}
			else
			{
				mDefFont = new Font(mBaseFont, _fontSize, _txStyle , _fontColor);
			}
		}
		else
		{
			mDefFont = FontFactory.getFont(_fName, BaseFont.IDENTITY_H , _fontSize, _txStyle, _fontColor);
		}
		
		String _itemText = ValueConverter.getString(_item.get("label"));
		Phrase _txt = new Phrase(_itemText, mDefFont);
		Paragraph _txtp = new Paragraph(_txt);
		_txtp.setLeading(_fontSize * 1.16f);
		
		float _fontY = drawColumnText(_contentByte, _x, _y, _w, _h, _txtp, true);
		
		float _cX = _x + 18;
		float _cY = _y;
		float _cW = _w;
		float _cH = _h;
		
		
		if( !_verticalAlign.equals("null") )
		{
			if( !_verticalAlign.equals("") && !_verticalAlign.equals("top") )
			{
				if( _verticalAlign.equals("middle"))
				{
					_cY = _cY - ((_fontY - _cH) / 2f );
				}
				else if( _verticalAlign.equals("bottom"))
				{
					_cY = (_cY - (_fontY - _cH));

				}
				_cY = _cY + 2f;
			}
		}
		else
		{
			_cY = _cY - ((_fontY - _cH) / 2f );
			_cY = _cY + 2f;
		}
		
		drawColumnText( _contentByte, _cX, _cY, _cW, _cH, _txtp, false);
		
		_contentByte.restoreState();	
	}
	
	
	private HashMap<String,Object > _paraGraphMap = new HashMap<String,Object>();
	
	private void CreatePdfRadioGroup( HashMap<String, Object> _item ) throws DocumentException, IOException
	{
		
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = _x + ObjectToFloat(_item.get("width"));
		float _h = _y - ObjectToFloat(_item.get("height"));
		
		String _symbolType = (String) _item.get("symbolType");
		
		// background 
		float _bgAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		//Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));
		Color _bgColor = hex2Rgb(ValueConverter.getString(_item.get("backgroundColor")));
		
		PdfGState _gState = new PdfGState();
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		_contentByte.saveState();
		
		Rectangle _box = new Rectangle(_x,_y,_w,_h);
		_box.setBackgroundColor(_bgColor);
		_gState.setFillOpacity(_bgAlpha);
		_contentByte.setGState(_gState);
		_box.setBorder(0);
		_contentByte.rectangle(_box);
		_contentByte.restoreState();
		
		_contentByte.saveState();
		
		HashMap<String, Float> _rHm = new HashMap<String, Float>();
		_rHm.put("top", _y);
		_rHm.put("left", _x);
		_rHm.put("right", _w);
		_rHm.put("bottom", _h);
		
		_contentByte = setBorderSetting(_contentByte , (ArrayList<String>) _item.get("borderSide") , (ArrayList<String>) _item.get("borderTypes") , (ArrayList<String>) _item.get("borderColors") , (ArrayList<Long>) _item.get("borderWidths") , _rHm);
		
		String _label = ValueConverter.getString(_item.get("label"));
		
		boolean _selected = ValueConverter.getBoolean(_item.get("selected"));
		
		float _chY = _y - ( (_y - _h) / 2f);
		
		Rectangle _border = new Rectangle(_x + 2 , _chY + 5, _x + 12, _chY - 5);
		
		
//		RadioCheckField _radioButton = new RadioCheckField(mWriter, _border, _label,("v" + mCNum));
		RadioCheckField _radioButton = new RadioCheckField(mWriter, _border, _label,"Yes");
		
		_radioButton.setBorderColor(new Color(0,0,0));
		_radioButton.setBorderStyle(0);
		_radioButton.setBorderWidth(1f);
		
		if( _symbolType.equalsIgnoreCase("check") ){
			_radioButton.setCheckType(RadioCheckField.TYPE_CHECK);
		}else{
			_radioButton.setCheckType(RadioCheckField.TYPE_CIRCLE);
		}
		
		_radioButton.setChecked(_selected);
		
		PdfFormField _top = _radioButton.getRadioGroup(true, false);
		PdfFormField field = _radioButton.getRadioField();
		
		String _itemId = "";
		
		_itemId = _item.get("id") == null ? ("check_" + mCNum) : ValueConverter.getString(_item.get("id")) + "_" + mCNum; 
		
		field.setFieldName(_itemId);
		field.setFieldFlags(PdfFormField.FF_READ_ONLY);
		
		_top.addKid(field);
		
		
		
		mWriter.addAnnotation(_top);
		mCNum++;
		
		
		int _txStyle = 0;
		float _fontSize = 0;
		String _verticalAlign = ValueConverter.getString(_item.get("verticalAlign"));
		_txStyle = fontStyleToInt(_item.get("fontStyle"), _item.get("fontWeight"), _item.get("textDecoration"));
		_fontSize = ObjectToFloat(_item.get("fontSize"));
		//Color _fontColor = new Color(ValueConverter.getInteger( _item.get("fontColorInt")));
		Color _fontColor = hex2Rgb(ValueConverter.getString(_item.get("fontColor")));
		String _fontName = ValueConverter.getString(_item.get("fontFamily"));
		
		if( mFontListAr.indexOf(_fontName) != -1 )
		{
			mDefFont = FontFactory.getFont(_fontName, BaseFont.IDENTITY_H, _fontSize, _txStyle , _fontColor);
		}
		else
		{
			if( mFontListAr.indexOf("맑은 고딕") != -1 )
			{
				mDefFont = FontFactory.getFont("맑은 고딕", BaseFont.IDENTITY_H, _fontSize, _txStyle , _fontColor);
			}
			else
			{
				mDefFont = new Font(mBaseFont, _fontSize, _txStyle , _fontColor);
			}
		}
		
		String _itemText = ValueConverter.getString(_item.get("label"));
		Phrase _txt = new Phrase(_itemText, mDefFont);
		Paragraph _txtp = new Paragraph(_txt);
		
		_txtp = new Paragraph(_txt);
		_txtp.setLeading(_fontSize * 1.16f);
		
		float _fontY = drawColumnText(_contentByte, _x, _y, _w, _h, _txtp, true);
		
		float _cX = _x + 18;
		float _cY = _y;
		float _cW = _w;
		float _cH = _h;
		
		
		if( !_verticalAlign.equals("null") )
		{
			if( !_verticalAlign.equals("") && !_verticalAlign.equals("top") )
			{
				if( _verticalAlign.equals("middle"))
				{
					_cY = _cY - ((_fontY - _cH) / 2f );
				}
				else if( _verticalAlign.equals("bottom"))
				{
					_cY = (_cY - (_fontY - _cH));
					
				}
				_cY = _cY + 2f;
			}
		}
		else
		{
			_cY = _cY - ((_fontY - _cH) / 2f );
			_cY = _cY + 2f;
		}
		
		drawColumnText( _contentByte, _cX, _cY, _cW, _cH, _txtp, false);
		
		_contentByte.restoreState();	
	}
	
	
	private void CreatePdfImage( HashMap<String, Object> _item ) throws MalformedURLException, IOException, DocumentException
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y")) - ObjectToFloat(_item.get("height"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
	
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		PdfGState _alpha = new PdfGState();
		
		float _itemAlpha = ValueConverter.getFloat(_item.get("alpha"));
		
//		var _bitmapD:BitmapData = _item.source;
		
		_contentByte.saveState();
		
		String _imageUrl = "";		
		_imageUrl = ValueConverter.getString(_item.get("src"));
		if( _imageUrl == null || _imageUrl.equals("null") )
		{
			_contentByte.restoreState();
			return;
		}
		
		Boolean _hasDictionary=false;
		ImageDictionaryVO _newImgDictionary=null;
		ImageDictionaryVO _imgDictionary = null;
		
		if( mImageDictionary != null ){
			_imgDictionary=mImageDictionary.getDictionaryData(_imageUrl);
		}else{
			mImageDictionary = new ImageDictionary();
		}
		
		if( _imgDictionary == null ){
			_hasDictionary = false;
		}else{
			_hasDictionary = true;
		}
		
		//Image _image = Image.getInstance(new URL(_imageUrl));		
		//Image _image = common.getRemoteImageFile(_imageUrl);
		Image _image = null;
		byte[] bAr = null;
		
		if( _hasDictionary ){
			_image = _imgDictionary.getmPDFImage();
		}else{
			try {
				bAr = org.apache.commons.codec.binary.Base64.decodeBase64(URLDecoder.decode(_imageUrl).getBytes());
				if( bAr != null )
				{
					_image = Image.getInstance(bAr);
					_newImgDictionary =mImageDictionary.createPDFDictionaryData(_imageUrl, _image);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		
		
		String _hyperLinkUrl = "";
		if( _item.get("ubHyperLinkUrl") != null ){
			_hyperLinkUrl = _item.get("ubHyperLinkUrl").toString();
		}
		if( !_hyperLinkUrl.endsWith("null") && _hyperLinkUrl.length() > 0 ){
			_image.setAnnotation(new Annotation(0, 0, 0, 0, _item.get("ubHyperLinkUrl").toString()));
		}
		
		
		boolean _isOriginSize = false;
		if( _item.containsKey("isOriginalSize") &&  !_item.get("isOriginalSize").equals(""))
		{
			_isOriginSize = Boolean.valueOf(_item.get("isOriginalSize").toString());
		}
		
   		if(_image != null)
   		{
			_image.scalePercent(10, 10);
			_alpha.setFillOpacity(_itemAlpha);	
			_contentByte.setGState(_alpha);
			
//			_image.setBorder(Rectangle.BOX);
//			_image.setBorderColor(Color.RED);
//			_image.setBorderWidth(3);
			
			if(_isOriginSize){
				HashMap<String,Float> _orignSize = common.getOriginSize(_item.get("width"),_item.get("height"),_image);
				_w = ObjectToFloat(_orignSize.get("width"));
				_h = ObjectToFloat(_orignSize.get("height"));
				_x = _x + 	ObjectToFloat(_orignSize.get("marginX"));	
				_y = _y + 	ObjectToFloat(_orignSize.get("marginY"));	
			}			
			_contentByte.addImage(_image, _w, 0, 0, _h, _x, _y);
			_contentByte.restoreState();
   		}
   		else
   		{
   			_contentByte.restoreState();
   		}
		
	}

	
	private void CreatePdfSign( HashMap<String, Object> _item ) throws MalformedURLException, IOException, DocumentException
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y")) - ObjectToFloat(_item.get("height"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		PdfGState _alpha = new PdfGState();
		
		float _itemAlpha = ValueConverter.getFloat(_item.get("alpha"));
		
//		var _bitmapD:BitmapData = _item.source;
		
		_contentByte.saveState();
		
		String _signSrc = "";		
		_signSrc = ValueConverter.getString(_item.get("src"));
		if( _signSrc == null || _signSrc.equals("null") || _signSrc.equals("") )
		{
			_contentByte.restoreState();
			return;
		}
		//Image _image = Image.getInstance(new URL(_imageUrl));		
		//Image _image = common.getRemoteImageFile(_imageUrl);
//		Image _image = common.getLocalImageFile(_signSrc);
		
		byte[] bAr=null;
		Image _image = null;
		
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
		if(bAr!=null) _image = Image.getInstance(bAr);
//		bAr = org.apache.commons.codec.binary.Base64.decodeBase64(URLDecoder.decode(_imageUrl).getBytes());
		
		if(_image != null)
		{
			_image.scalePercent(10, 10);
			_alpha.setFillOpacity(_itemAlpha);
			
			_contentByte.setGState(_alpha);
			_contentByte.addImage(_image, _w, 0, 0, _h, _x, _y);
			
			_contentByte.restoreState();
		}
		else
		{
			_contentByte.restoreState();
		}
		
	}
		
	
	
	private void CreatePdfLine( HashMap<String, Object> _item )
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		
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
			_sY = _y - _y1;
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
			_eY = _y - _y2;
		}
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		Color _lineColor = new Color(ValueConverter.getInteger(_item.get("lineColorInt")));
//		put("x", 0);
//		put("y", 0);
//		put("x1", 0);
//		put("y1", 0);
//		put("x2", 0);
//		put("y2", 0);
		
		// 기존에는 보더의 thickness값이 ObjectToFloat(_item.get("thickness")) 로만 지정되어있음
		// 라벨의 보더처리시엔느 아래와 같이 -0.3f 하고잇어 이를 동일하게 변경
		float _widthF  = ObjectToFloat(_item.get("thickness"));
		_widthF = _widthF - 0.3f;
		
		_contentByte.saveState();
//		_contentByte.setLineWidth( ValueConverter.getInteger(_item.get("thickness")));
		_contentByte.setLineWidth( _widthF );
		_contentByte.setColorStroke(_lineColor);
		_contentByte.moveTo( _sX, _sY );
		_contentByte.lineTo( _eX, _eY );
		_contentByte.stroke();
		_contentByte.restoreState();
		
	}
	
	private void CreatePdfCircle( HashMap<String, Object> _item ) throws DocumentException
	{
		
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat( ValueConverter.getFloat(_item.get("x")) + ValueConverter.getFloat(_item.get("width")) );
		float _h = mPageHeight - ObjectToFloat( ValueConverter.getFloat(_item.get("y")) + ValueConverter.getFloat(_item.get("height")) );
	
		
		// border, background;
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		PdfGState _alpha = new PdfGState();
		float alpha = ValueConverter.getFloat(_item.get("alpha"));
		float contentBgAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
		float borderAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));
		
 		_alpha.setFillOpacity(alpha*contentBgAlpha); 
 		_alpha.setStrokeOpacity(alpha*borderAlpha);// border		
		
		_contentByte.saveState();
		
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("contentBackgroundColorInt")));
		Color _brColor = new Color(ValueConverter.getInteger(_item.get("borderColorInt")));
//		float _lineW = ValueConverter.getFloat(_item.get("strokeWidth"));
		float _lineW = ObjectToFloat(_item.get("strokeWidth"));
		_contentByte.setGState(_alpha);
		_contentByte.setColorStroke(_brColor);
		_contentByte.setLineWidth(_lineW);
		_contentByte.setColorFill(_bgColor);
		_contentByte.ellipse(_x, _y, _w, _h);
		_contentByte.fillStroke();
		_contentByte.restoreState();
	}
	
	
	private void CreatePdfRectangle( HashMap<String, Object> _item ) throws DocumentException
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
	
		_y = _y -_h;
		// border, background;
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		_contentByte.saveState();
		
		PdfGState _alpha = new PdfGState();
 		
		float alpha = ValueConverter.getFloat(_item.get("alpha"));
		float contentBgAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
		float borderAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));
		
 		_alpha.setFillOpacity(alpha*contentBgAlpha); 
 		_alpha.setStrokeOpacity(alpha*borderAlpha);// border			
		
 		
 		int _radius = ValueConverter.getInteger(_item.get("rx"));
 		
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("contentBackgroundColorInt")));
		Color _brColor = new Color(ValueConverter.getInteger(_item.get("borderColorInt")));
//		float _lineW = ValueConverter.getFloat(_item.get("borderThickness"));
		float _lineW = ObjectToFloat(_item.get("borderThickness"));					//@@최명진
		_contentByte.setGState(_alpha);
		_contentByte.setLineWidth(_lineW);
		_contentByte.setColorFill(_bgColor);
		_contentByte.setColorStroke(_brColor);
		_contentByte.roundRectangle(_x, _y, _w, _h, _radius);
		_contentByte.fillStroke();
		_contentByte.restoreState();
	}	
	
	private void CreatePdfGradiantRectangle( HashMap<String, Object> _item ) throws DocumentException
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat( _item.get("width"));
		float _h = ObjectToFloat( _item.get("height"));
	
		_y = _y -_h;
		// border, background;
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		_contentByte.saveState();
		
		PdfGState _alpha = new PdfGState();
		float alpha = ValueConverter.getFloat(_item.get("alpha"));
		float borderAlpha = ValueConverter.getFloat(_item.get("borderAlpha"));
		//bg 투명도 정보는 있으나 적용방법 확인해 봐야함
		
 		_alpha.setFillOpacity(alpha); 
 		_alpha.setStrokeOpacity(alpha*borderAlpha);// border	
// 		contentBackgroundAlphas
 		
		ArrayList<Integer> _bgList = (ArrayList<Integer>) _item.get("contentBackgroundColorsInt");
		
// 		Color _bgColor1 = new Color(_bgList.get(0));
// 		Color _bgColor2 = new Color(_bgList.get(1));
 		Color _bgColor1 = new Color( ValueConverter.getInteger(_bgList.get(0)) );
 		Color _bgColor2 = new Color( ValueConverter.getInteger(_bgList.get(1)) );
 		Color _brColor = new Color(ValueConverter.getInteger(_item.get("borderColorInt")));
// 		float _lineW = ValueConverter.getFloat(_item.get("borderThickness"));
 		float _lineW = ObjectToFloat(_item.get("borderThickness"));		//@@최명진
 		
 		int _radius = ValueConverter.getInteger(_item.get("rx"));
 		float _rW = _w / 2f;
 		
		PdfShading axial = PdfShading.simpleAxial(mWriter, _x + _rW, _y , (_x + _w) - _rW, _y + _h, _bgColor2, _bgColor1);
		PdfShadingPattern shading = new PdfShadingPattern(axial);
		_contentByte.setShadingFill(shading);
		_contentByte.setGState(_alpha);
		
		_contentByte.setLineWidth(_lineW);
		_contentByte.setColorStroke(_brColor);
		_contentByte.roundRectangle(_x, _y, _w, _h, _radius);
		_contentByte.fillStroke();
		_contentByte.restoreState();
	}
	
	
	private void CreatePdfClipImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException
	{
		float _x = ObjectToFloat(_item.get("x"));
//		float _y = ObjectToFloat(_item.get("y"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		// border, background;
		PdfContentByte _contentByte = mWriter.getDirectContent();
		PdfGState _alpha = new PdfGState();

		String _clipName = ValueConverter.getString(_item.get("clipArtData"));
		
		log.info(getClass().getName() + "CreatePdfClipImage ===> " + "ClipName : " + _clipName);
		
		if( !_clipName.substring(_clipName.length() - 3, _clipName.length()).toUpperCase().equals("SVG"))
		{
			_clipName = _clipName+".svg";
		}
		
		String _urlStr = Log.basePath + "UView5/assets/images/svg/" + _clipName;
		
        PdfTemplate template = _contentByte.createTemplate(_w,_h);
        Graphics2D g2 = template.createGraphics(_w,_h);          

        PrintTranscoder prm = new PrintTranscoder();
        TranscoderInput ti = new TranscoderInput(new FileInputStream(_urlStr));
        
        if( ti == null ) return;
        
        prm.transcode(ti, null);

        PageFormat pg = new PageFormat();
        Paper pp= new Paper();
        pp.setSize(_w, _h);	
        pp.setImageableArea(0, 0, _w, _h);
        pg.setPaper(pp);
        prm.print(g2, pg, 0); 
        g2.dispose(); 
        
        PdfTemplate _iT = new ImgTemplate(template).getTemplateData();
        
        _contentByte.addTemplate(_iT, _x, (_y - _h));
        
	}

	private void CreatePdfSVGArea( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException
	{
		float _x = ObjectToFloat(_item.get("x"));
//		float _y = ObjectToFloat(_item.get("y"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		// border, background;
		PdfContentByte _contentByte = mWriter.getDirectContent();
		PdfGState _alpha = new PdfGState();
		
		String _dataStr = URLDecoder.decode( ValueConverter.getString(_item.get("data")) , "UTF-8");
		
		_dataStr = _dataStr.replaceAll("%20", " ");
		
		// viewBox 를 이용해서 사이즈 변경 //		
		int _startIdx = _dataStr.indexOf("<svg");
		if( _startIdx == -1 ) return; // svg tag 가 아니면 그리지 않는다..
		String _svgTag = _dataStr.substring(_startIdx, _dataStr.indexOf(">" , _startIdx) + 1);
		
		String _defSize = (mPageWidth > mPageHeight)? String.valueOf(mPageHeight):  String.valueOf(mPageWidth);
		
		int _widthIdx = _svgTag.indexOf("width");
		String _widthString =  "";
		float _widthValue = 0f;
		if( _widthIdx > -1 )
		{
			_widthString = _svgTag.substring( _widthIdx , _svgTag.indexOf("\"" , _widthIdx + 7) + 1);
			String _widthData = _widthString.substring(_widthString.indexOf("\"") + 1, _widthString.indexOf("\"", _widthString.indexOf("\"") + 1));
			_widthData = _widthData.replaceAll("[^\\d.-]", "");	// width의 px와 같은 문자를 제거
			
			_widthValue = ValueConverter.getFloat( _widthData );
		}
		else
		{
			_widthString = _defSize;
			_widthValue =  ValueConverter.getFloat( _widthString );
		}
		
//		float _widthValue = ValueConverter.getFloat(_widthString.substring(_widthString.indexOf("\"") + 1, _widthString.indexOf("\"", _widthString.indexOf("\"") + 1)));
		String _changeWidth = "width=\"" + _w + "\"";

		//viewBox 태그가 존재할경우 아래에서 넣어주기 때문에 제거
		int _viewBoxIdx = _svgTag.indexOf("viewBox");
		if(_viewBoxIdx > -1)
		{
			String _viewBoxString = _svgTag.substring( _viewBoxIdx , _svgTag.indexOf("\"" , _viewBoxIdx + 9) + 1);
			_dataStr = _dataStr.replace(_viewBoxString, "");
		}
		
		int _heightIdx = _svgTag.indexOf("height");
		String _heightString = "";
		float _heightValue = 0f;
		if( _heightIdx > -1 )
		{
			_heightString = _svgTag.substring( _heightIdx , _svgTag.indexOf("\"" , _heightIdx + 8) + 1);
			String _heightData = _heightString.substring(_heightString.indexOf("\"") + 1, _heightString.indexOf("\"", _heightString.indexOf("\"") + 1));
			_heightData = _heightData.replaceAll("[^\\d.-]", "");	// width의 px와 같은 문자를 제거

			
			_heightValue = ValueConverter.getFloat(_heightData);
		}
		else
		{
			_heightString = _defSize;
			_heightValue = ValueConverter.getFloat(_heightString);
		}
		
//		String _heightString = _svgTag.substring( _heightIdx , _svgTag.indexOf("\"" , _heightIdx + 8) + 1);
//		float _heightValue = ValueConverter.getFloat(_heightString.substring(_heightString.indexOf("\"") + 1, _heightString.indexOf("\"", _heightString.indexOf("\"") + 1)));
		String _changeHeight = "height=\"" + _h + "\" viewBox=\"0 0 " + _widthValue + " " + _heightValue + "\" preserveAspectRatio=\"none\"";
		// viewBox 를 이용해서 사이즈 변경 //
		
		_dataStr = _dataStr.replace(_widthString, _changeWidth)// width 변경
							.replace(_heightString, _changeHeight);// height 변경
		
		PdfTemplate template = _contentByte.createTemplate(_w,_h);
		Graphics2D g2 = template.createGraphics(_w,_h);          
		
		PrintTranscoder prm = new PrintTranscoder();
		TranscoderInput ti = new TranscoderInput(new StringInputStream(_dataStr , "UTF-8"));
		prm.transcode(ti, null);
		
		PageFormat pg = new PageFormat();
		Paper pp= new Paper();
		pp.setSize(_w, _h);	
		pp.setImageableArea(0,0, _w, _h);
		pg.setPaper(pp);
		prm.print(g2, pg, 0); 
		g2.dispose(); 
		
		PdfTemplate _iT = new ImgTemplate(template).getTemplateData();
		
		_contentByte.addTemplate(_iT, _x, (_y - _h));
		
//		colorRectangle(_contentByte, new Color(255,0,0), _x, _y, _w, _h);
		
	}	
	
	private void CreatePdfSVGRichTextLabel( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException
	{
		
		float _x = ObjectToFloat( _item.get("x") );
		float _y = mPageHeight - ObjectToFloat( _item.get("y") );
		float _w = ObjectToFloat( ValueConverter.getFloat(_item.get("x")) + ValueConverter.getFloat(_item.get("width")) );
		float _h = mPageHeight - ObjectToFloat( ValueConverter.getFloat(_item.get("y")) + ValueConverter.getFloat(_item.get("height")) );
		
		String _id = ValueConverter.getString(_item.get("id"));
		String _type = ValueConverter.getString(_item.get("type"));
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		PdfGState _gState = new PdfGState();
		Rectangle _bRect;
		boolean _verticalRotateFlg = false;
		
		ArrayList<ArrayList<HashMap<String, Object>>> lineAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("children");
		
		if(lineAr == null || lineAr.size() == 0 ) return;
		
		_contentByte.saveState();
		
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
		
		Paragraph _txtp = new Paragraph();
		
		String _fName = getDefFont(_fontName);
		Color _txColor;
		
		float _minW = _w - _x;
		
		for (ArrayList<HashMap<String, Object>> textAr : lineAr) {
			
			float _lineMinW = 0;
			for (int i = 0; i < textAr.size(); i++) {
				
				HashMap<String, Object> _lineItem = textAr.get(i);
				String _fontStyle = "normal";
				if( _lineItem.containsKey("font-style") )
				{
					_fontStyle = _lineItem.get("font-style").toString();
				}
				int _txStyle = fontStyleToInt(_fontStyle, "normal", "none");
				
				_txColor = _txColorDef;
				
				if(_lineItem.containsKey("fontColor"))
				{
					if( _lineItem.get("fontColor").toString().charAt(0) == '#' )
					{
						_txColor = new Color(Integer.parseInt(_lineItem.get("fontColor").toString().substring(1), 16));
					}
				}
				
				if( _fName.equals("") )
				{
					if( mFontListAr.indexOf("맑은 고딕") != -1 )
					{
						mDefFont = FontFactory.getFont("맑은 고딕", BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
					}
					else
					{
						mDefFont = new Font(mBaseFont, _fontSize, _txStyle , _txColor);
					}
				}
				else
				{
					mDefFont = FontFactory.getFont(_fName, BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
				}
				
				String _itemText = ValueConverter.getString(_lineItem.get("text"));
				
				Chunk _chunk = new Chunk(_itemText, mDefFont);
				
				// 자동 줄바꿈시 문자단위로 줄바꿈 되지 않도록 하기 위하여 SplitCharacter 지정
				_chunk.setSplitCharacter(new SplitCharacter() {
					
					@Override
					public boolean isSplitCharacter(int start, int current, int end, char[] cc,
							PdfChunk[] ck) {
				        return false;
					}
				});
				if( ValueConverter.getString(_lineItem.get("fontWeight")).equals("normal") == false ) // bold 의 두깨를 조절.
				{
					float _blodNum = (Math.round( ( _fontSize / 35f ) * 10f ) / 10f );
					_chunk.setTextRenderMode( PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE , _blodNum, _txColor);
				}
				
				_txtp.add(_chunk);
				
				if( !ValueConverter.getString(_lineItem.get("text-decoration")).equals("none") && !ValueConverter.getString(_lineItem.get("text-decoration")).equals("") )
				{
					float _thickness = (_fontSize/2) / 10;
					if( _thickness < 0.8f ) _thickness = 0.8f;
					_chunk.setUnderline(_txColor, _thickness, 0f, 0.5f, -0.2f ,PdfContentByte.LINE_CAP_BUTT);
				}
				
				_lineMinW = _lineMinW + _chunk.getWidthPoint();
				
			}
			if( _minW < _lineMinW )
			{
				_minW = _lineMinW;
			}
			_txtp.add(new Chunk("\n"));
		}
		
		

		if( _fontSize >= 60)
		{
			_txtp.setLeading((_fontSize+_lineGap) * 0.7f);
		}
		else if( _fontSize > 15 )
		{
			_txtp.setLeading((_fontSize+_lineGap) * 0.8f);
		}
		else
		{
			_txtp.setLeading((_fontSize+_lineGap) * 1.2f);	
		}
		
		_txtp.setAlignment(_align);
		
		_bRect = new Rectangle(_x, _y, _w, _h);
		
		_bRect.setBackgroundColor(_bgColor);
		_gState.setFillOpacity(_bgAlpha);
		_contentByte.setGState(_gState);
		
		_bRect.setBorder(0);
		_contentByte.rectangle(_bRect);
		
		_rect.put("top", _y);
		_rect.put("left", _x);
		_rect.put("right", _w);
		_rect.put("bottom", _h);
		
		
//		_contentByte = setBorderSetting(_contentByte , (ArrayList<String>) _item.get("borderSide") , (ArrayList<String>) _item.get("borderTypes") , (ArrayList<Integer>) _item.get("borderColorsInt") , (ArrayList<Integer>) _item.get("borderWidths") , _rect);
		
		_contentByte.restoreState();
		
		_contentByte.saveState();
		
		if( !_type.equals("rotateLabel") && (_y - _h) < _txtp.getLeading() )
		{
			_txtp.setLeading((_y-_h) - 1f);
		}
		
//		float _padding = ObjectToFloat(0);
//		if( _item.containsKey("padding") )
//		{
//			
//			if( ObjectToFloat(_item.get("padding")) != 0 )
//			{
//				_padding = _padding + ObjectToFloat(_item.get("padding"));
//			}
//			else
//			{
//				_padding = ObjectToFloat(1);
//			}
//		}
//		
//		_x = _x + _padding;
//		_w = _w - _padding;
		
		if( _w-_x < _minW )
		{
			_w = _x + _minW;
		}
		
		float _fontY = drawColumnText( _contentByte, _x, _y, _w, _h,  _txtp, true);
		
		float _cX = _x;
		float _cY = _y;
		float _cW = _w;
		float _cH = _h;
		
		
		drawColumnText( _contentByte, _cX, _cY, _cW, _cH, _txtp, false);
		
		_contentByte.restoreState();
		
	}
	
	private void CreatePdfSVGRichText( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException
	{
		float _x = ObjectToFloat(_item.get("x"));
//		float _y = ObjectToFloat(_item.get("y"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		// border, background;
		PdfContentByte _contentByte = mWriter.getDirectContent();
		PdfGState _alpha = new PdfGState();
		
		String _dataStr = URLDecoder.decode( ValueConverter.getString(_item.get("data")) , "UTF-8");
		
		_dataStr = _dataStr.replaceAll("%20", " ");
		
		PdfTemplate template = _contentByte.createTemplate(_w,_h);
		Graphics2D g2 = template.createGraphics(_w,_h);          
		
		
		PrintTranscoder prm = new PrintTranscoder();
		TranscoderInput ti = new TranscoderInput(new StringInputStream(_dataStr , "UTF-8"));
		prm.transcode(ti, null);
		
		PageFormat pg = new PageFormat();
		Paper pp= new Paper();
		pp.setImageableArea(0,0, _w, _h);
		pg.setPaper(pp);
		prm.print(g2, pg, 0); 
		g2.dispose(); 
		
		PdfTemplate _iT = new ImgTemplate(template).getTemplateData();
		
		_contentByte.addTemplate(_iT, _x, (_y - _h));
	
//		colorRectangle(_contentByte, Color.RED, _x, (_y - _h), _w, _h);
//		colorRectangle(_contentByte, Color.RED, _x, (_y - _h), ValueConverter.getFloat(pg.getImageableWidth()), ValueConverter.getFloat(pg.getImageableHeight()));
	}
	
	private void CreatePdfDoImage( HashMap<String, Object> _item ) throws MalformedURLException, IOException, DocumentException
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y")) - ObjectToFloat(_item.get("height"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
	
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		PdfGState _alpha = new PdfGState();
		
//		var _bitmapD:BitmapData = _item.source;
		
		_contentByte.saveState();
		
		String _imageUrl = "";
		
		_imageUrl = ValueConverter.getString(_item.get("src"));
		//Image _image = Image.getInstance(new URL(mUrl + _imageUrl));	
		//Image _image = common.getRemoteImageFile(_imageUrl);
		Image _image = getLocalImageFile(_imageUrl);
		if(_image != null)
		{
			_image.scalePercent(10, 10);
			_alpha.setFillOpacity(1);
	
			_contentByte.setGState(_alpha);
			_contentByte.addImage(_image, _w, 0, 0, _h, _x, _y);
			
			_contentByte.restoreState();
		}
		
	}
		
	// 로컬에 있는 서블릿 메소드를 호출하여 이미지 데이터를 받아온다.
	private Image getLocalImageFile(String _imageUrl)
	{
		Image _image = null;
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
				if( bAr != null ) _image = Image.getInstance(bAr);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
        
        return _image;
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
		for(String key : mTBborder.keySet())
		{
			ArrayList<HashMap<String, Object>> _tbBorderList = mTBborder.get(key);
			
			int _size = _tbBorderList.size();
			
			for (int i = 0; i < _size; i++) 
			{
				HashMap<String, Object> _hm = _tbBorderList.get(i);
				
				String _type = ValueConverter.getString(_hm.get("type"));
				Color _color = (Color) _hm.get("color");
				int _width = ValueConverter.getInteger(_hm.get("width"));
				float _widthF = ObjectToFloat(_hm.get("width"));
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
				
				PdfContentByte _contentByte = mWriter.getDirectContent();
				
				_contentByte.saveState();
				_contentByte.setLineWidth( _widthF );
				_contentByte.setColorStroke(_color);
				_contentByte = lineType_Setting( _type , _contentByte , _width );
				_contentByte.moveTo(_sp1,_sp2); 
				_contentByte.lineTo(_ep1,_ep2);
				_contentByte.stroke(); 
				_contentByte.restoreState();
			}
		}
	}
	
	
	
	
	
	private PdfContentByte lineType_Setting( String _type , PdfContentByte _contentByte , float _width )
	{
		float _dash = 3f * _width;
		float _dot = 0.5f;
		float _dashDot = 6f * _width;
		
		float[] _dash_dot1 = {_dashDot,_dash,0.5f,_dash};
		float[] _dash_dot2 = {_dashDot, _dash, 0.5f ,_dash , 0.5f, _dash };
		
		if( _type.toUpperCase().equals("DASH"))
		{
			_contentByte.setLineCap(PdfContentByte.LINE_CAP_BUTT);
			_contentByte.setLineDash( _dash, 0 );
		}
		else if( _type.toUpperCase().equals("DASH_DOT") )
		{
			_contentByte.setLineCap( PdfContentByte.LINE_CAP_ROUND );
			_contentByte.setLineDash( _dash_dot1 , 0 );
		}
		else if( _type.toUpperCase().equals("DASH_DOT_DOT") )
		{
			_contentByte.setLineCap( PdfContentByte.LINE_CAP_ROUND );
			_contentByte.setLineDash( _dash_dot2 , 0 );
		}
		else if( _type.toUpperCase().equals("DOT") )
		{
			_contentByte.setLineCap(PdfContentByte.LINE_CAP_ROUND);
			_contentByte.setLineDash( _dot, _dash, 0 );
		}
//		else if( _type.toUpperCase().equals("DOUBLE") )
//		{
//			mContentByte.setLineDash2( _dash, 0 );
//		}
		else if( _type.toUpperCase().equals("SOLID") || _type.toUpperCase().equals("DOUBLE") )
		{
			_contentByte.setLineCap(PdfContentByte.LINE_CAP_ROUND);	
		}
		
		return _contentByte;
		
	}
	
	
	
	private ArrayList<HashMap<String, Object>> tbBorderSetting(ArrayList<String> _sides , ArrayList<String> _types , ArrayList<String> _colors , ArrayList<Integer> _widths , HashMap<String, Float> _rect , ArrayList<HashMap<String, Object>> _content, float _alpha )
	{
		
//		Color _boColor = new Color(ObjectToInt(_item.get("borderColorInt")));
//		_bRect.setBorderWidth(SizeToInt(_item.get("borderWidth")));		
//		_bRect.setBorderColor(_boColor);		
		
		ArrayList<HashMap<String, Object>> rtnList = _content;
		HashMap<String, Object> borderSet;
		
		int _sLength = _sides.size();
		for (int i = 0; i < _sLength; i++) 
		{
			
			borderSet = new HashMap<String, Object>();
			
			String _side = _sides.get(i);
			String _type = _types.get(i);
			
			//Color _color = new Color(ValueConverter.getInteger(_colors.get(i)));
			Color _color = hex2Rgb(ValueConverter.getString(_colors.get(i)));
			
			int _width = ValueConverter.getInteger(_widths.get(i));

			if( _type.equals("none")) 
			{
				continue; 
			}
			
			borderSet.put("width", _width);
			borderSet.put("color",_color); 
			borderSet.put("type",_type);
			
			String _sp = "";
			String _ep = ""; 
			
			if( _side.equals("top"))
			{
				_sp = _rect.get("left") + "," + _rect.get("top");
				_ep = _rect.get("right") + "," + _rect.get("top");
			}
			else if( _side.equals("left"))
			{
				_sp = _rect.get("left") + "," + _rect.get("top");
				_ep = _rect.get("left") + "," + _rect.get("bottom");
			}
			else if(_side.equals("bottom"))
			{
				_sp = _rect.get("left") + "," + _rect.get("bottom");
				_ep = _rect.get("right") + "," + _rect.get("bottom");
			}
			else if( _side.equals("right"))
			{
				_sp = _rect.get("right") + "," + _rect.get("top");
				_ep = _rect.get("right") + "," + _rect.get("bottom");
			}
			
			borderSet.put("sp",_sp);
			borderSet.put("ep",_ep);
			
			borderSet.put("alpha", _alpha);
			
			rtnList.add(borderSet);
			
		}
		return rtnList;
	}
	
	
	private PdfContentByte setBorderSetting(PdfContentByte _contentByte , ArrayList<String> _sides , ArrayList<String> _types , ArrayList<String> _colors , ArrayList<Long> _widths , HashMap<String, Float> _rect )
	{
		return setBorderSetting( _contentByte , _sides , _types , _colors , _widths , _rect, 1.0f);
	}
	
	private PdfContentByte setBorderSetting(PdfContentByte _contentByte , ArrayList<String> _sides , ArrayList<String> _types , ArrayList<String> _colors , ArrayList<Long> _widths , HashMap<String, Float> _rect, float _alpha )
	{
		
//		Color _boColor = new Color(ObjectToInt(_item.get("borderColorInt")));
//		_bRect.setBorderWidth(SizeToInt(_item.get("borderWidth")));		
//		_bRect.setBorderColor(_boColor);	
		
		PdfGState _gState = new PdfGState();
		
		int _sLength = _sides.size();
		for (int i = 0; i < _sLength; i++) 
		{
			String _side = _sides.get(i);
			String _type = _types.get(i);
			
			//int fucker = _colors.get(i).intValue();
			//Color _color = new Color(fucker);
			Color _color = hex2Rgb(ValueConverter.getString(_colors.get(i)));
			
			int _width = _widths.get(i).intValue();
			
			//PDF 저장시 라벨의 Border Width와 table의 borderWidth 값의 차이가 나서 수정 2017-02-20 최명진
			float _widthF = ObjectToFloat(_width);
//			float _widthF = ValueConverter.getFloat(_hm.get("width"));
			_widthF = _widthF - 0.3f;
			
			if( _type.equals("none"))
			{
				continue;
			}
			
			_contentByte.saveState();
//			_contentByte.setLineWidth( _width );
			_contentByte.setLineWidth( _widthF );
			_contentByte.setColorStroke(_color);
//			_contentByte = lineType_Setting( _type , _contentByte , _width );
			_contentByte = lineType_Setting( _type , _contentByte , _widthF );
			
			 // item의 Alpha처리
		    if( _alpha <  1.0 )
		    {
		    	_gState = new PdfGState();
		    	_gState.setStrokeOpacity( _alpha );
		    	_gState.setFillOpacity( _alpha );
		    	_contentByte.setGState(_gState);
		    }
			
			if( _side.equals("top"))
			{
				_contentByte.moveTo(_rect.get("left"), _rect.get("top")); 
				_contentByte.lineTo(_rect.get("right"), _rect.get("top"));
				_contentByte.stroke(); 
			}
			else if( _side.equals("left"))
			{
				_contentByte.moveTo(_rect.get("left"), _rect.get("top")); 
				_contentByte.lineTo(_rect.get("left"), _rect.get("bottom"));
				_contentByte.stroke(); 
			}
			else if(_side.equals("bottom"))
			{
				_contentByte.moveTo(_rect.get("left"), _rect.get("bottom")); 
				_contentByte.lineTo(_rect.get("right"), _rect.get("bottom"));
				_contentByte.stroke(); 
			}
			else if( _side.equals("right"))
			{
				_contentByte.moveTo(_rect.get("right"), _rect.get("top")); 
				_contentByte.lineTo(_rect.get("right"), _rect.get("bottom"));
				_contentByte.stroke(); 
			}
			_contentByte.restoreState();
		}
		return _contentByte;
	}
	
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
	
	private HashMap<String, Object> convertScaleItem( HashMap<String, Object> _item, float _pWidth, float _pHeight )
	{
		String[] _chnageProperty = {"width","height","x","y","x1","y1","x2","y2","fontSize"};
		String _className = ValueConverter.getString(_item.get("className"));
		
		int _changeCnt = _chnageProperty.length;
		float _itemFloat = 0;
		
		
		for (int k = 0; k < _changeCnt; k++) {
			if( _item.containsKey(_chnageProperty[k]) && _item.get(_chnageProperty[k]) != null )
			{
				
				_itemFloat = ValueConverter.getFloat( _item.get(_chnageProperty[k]) );
				
				if( _chnageProperty[k] == "x" || _chnageProperty[k] == "x1" || _chnageProperty[k] == "x2" )
				{
					_itemFloat = _itemFloat * mScale;
					_itemFloat = _itemFloat + _pWidth;
				}
				else if( _chnageProperty[k] == "y" || _chnageProperty[k] == "y1" || _chnageProperty[k] == "y2" )
				{
					_itemFloat = _itemFloat * mScale;
					_itemFloat = _itemFloat + _pHeight;
				}
				else if( _chnageProperty[k] == "width")
				{
					_itemFloat = _itemFloat * mScale; 
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
	
	
	
	
	
	private class Watermark extends PdfPageEventHelper {
		
		float _fSize = 65;
		
		Font _font;
		
		protected Phrase watermark;
	   
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
        	
        	_fSize = getFontSize();
        	
        	
        	String _fontName = "맑은 고딕";

    		if( mFontListAr.indexOf(_fontName) != -1 )
    		{
    			_font = FontFactory.getFont(_fontName, BaseFont.IDENTITY_H, _fSize, Font.BOLD, Color.LIGHT_GRAY);
    		}
    		else
    		{
    			_font = new Font(mBaseFont, _fSize, Font.BOLD , Color.LIGHT_GRAY);
    		}
        	
//        	_font = new Font(mBaseFont, _fSize , Font.BOLD, Color.LIGHT_GRAY);
        	watermark = new Phrase(mWaterMarkTxt, _font);
        	PdfGState _gState = new PdfGState();
        	float _alpha = (float) 0.5;
        	PdfContentByte _contentByte = mWriter.getDirectContent();
        	_gState.setFillOpacity(_alpha);
        	_gState.setStrokeOpacity(_alpha);
			_contentByte.setGState(_gState);
			ColumnText _a = new ColumnText(_contentByte);
            ColumnText.showTextAligned(_contentByte, Element.ALIGN_CENTER, watermark, mPageWidth / 2, mPageHeight / 2, 45);
        }

        private float getFontSize()
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
        
	}
	private class License extends PdfPageEventHelper {
		
		float _fSize = 45;
		
		Font _font;
		
		private String licenseType;
		
		protected Phrase license;
		
		
		public License(String licensetype) {
			super();
			
			this.licenseType = licensetype;
		}
	   
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
        	
        	if( "FREE".equals(this.licenseType.toUpperCase()) ) return;
        	
        	String _fontName = "맑은 고딕";

    		if( mFontListAr.indexOf(_fontName) != -1 )
    		{
    			_font = FontFactory.getFont(_fontName, BaseFont.IDENTITY_H, _fSize, Font.BOLD, Color.LIGHT_GRAY);
    		}
    		else
    		{
    			_font = new Font(mBaseFont, _fSize, Font.BOLD , Color.LIGHT_GRAY);
    		}
        	
//        	_font = new Font(mBaseFont, _fSize , Font.NORMAL, Color.LIGHT_GRAY);
        	license = new Phrase(this.licenseType.toUpperCase(), _font);
        	
        	PdfGState _gState = new PdfGState();
        	float _alpha = (float) 0.5;
        	PdfContentByte _contentByte = mWriter.getDirectContent();
        	_gState.setFillOpacity(_alpha);
        	_gState.setStrokeOpacity(_alpha);
			_contentByte.setGState(_gState);
            ColumnText.showTextAligned(_contentByte, Element.ALIGN_LEFT, license, 0, mPageHeight - 40, 0);
        }
        
	}
	
}
