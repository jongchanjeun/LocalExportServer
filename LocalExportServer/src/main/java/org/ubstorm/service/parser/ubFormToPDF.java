package org.ubstorm.service.parser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;
import org.jdom2.JDOMException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.ubstorm.server.commnuication.UDPClient;
//import org.seuksa.itextkhmer.render.UnicodeRender;
import org.ubstorm.service.dictionary.ImageDictionary;
import org.ubstorm.service.dictionary.ImageDictionaryVO;
import org.ubstorm.service.logger.Log;
//import org.ubstorm.service.method.ViewerInfo5;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.info.item.UBComponent;
import org.ubstorm.service.parser.thread.DaemonThreadFactory;
import org.ubstorm.service.utils.Base64Coder;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;
import org.xml.sax.SAXException;

import com.lowagie.text.Annotation;
import com.lowagie.text.Cell;
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
import com.lowagie.text.Table;
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
import com.lowagie.text.pdf.interfaces.PdfVersion;
import com.oreilly.servlet.Base64Decoder;

import net.sf.jni4net.Bridge;
import pdfmerge.PdfMerge;

public class ubFormToPDF {

	
	private int mHeight = 0;
	
	// class
	private ItemPropertyVariable mItemPropVar = new ItemPropertyVariable();
	private ItemPropertyProcess mPropertyFn = new ItemPropertyProcess();
	// class
	private Logger log = Logger.getLogger(getClass());
	private HashMap<String, Object> mParam = new HashMap<String, Object>();
	

	private HashMap<String, Object> mProjectHm;
	private HashMap<String, ArrayList<HashMap<String, Object>>> mItemHm;
	
	private float mPageHeight = 0;
	private float mPageWidth = 0;
	
	private String mWaterMarkTxt = "";
	
	private Image mWaterMarkImg = null;
	private HashMap<String, Object> mWaterMarkImgMap = null;
	
	private HashMap<String, Font> _useFontMap = new HashMap<String, Font>();

	private HashMap<String, ArrayList<HashMap<String, Object>>> mTBborder;
	
	// pdf
	private Document mDocument;
	private PdfWriter mWriter;
	private BaseFont mBaseFont;
	private Font mDefFont;
//	private ByteArrayOutputStream mBaos;
//	private FileOutputStream mBaos;
	private OutputStream mBaos;
	
	private boolean mUseThread =  false;	// pageGroup으로 파일이 분할될때 thread를 이용하여 파일을분할할지 여부값 
	private int mThreadCount = 1;
	private int mThreadMaxCount = 20;
	
	// pdf 
	
	private ArrayList<String> mFontListAr;
	private float mMarginX = 0;
	private float mMarginY = 0;
	private float mScale = 1; 
	
	private int mPDF_PAGE_IDX = 0;
	private ArrayList<String> mSaveFilePathAr = new ArrayList<String>();
	
	private String pdfFilePath = "";
	private String pdfFileName = "ubformLocal";
	public void setPdfFileName(String pdfFileName) {
		this.pdfFileName = pdfFileName;
	}

	private String EXPORT_DIR = null;
	
	private String mExportPath = "";
	private int mExportDivCount = 100;
	private int mTotalPageCount = 0;
	
	private String mPDF_PRINT_VERSION = "2.0";		//PDF 내보내기시 Version 처리 ( 1.0 기존 버전 / 2.0 폰트 수정 버전 ) 
	
	private String mSESSION_ID = "";
	
	private HashMap<String, Object> mChangeItemList;
	
	private ImageDictionary mImageDictionary;
	
	private String mUseUiDialog = "true";
	
	private UDPClient mUdpClient = null;
	
//	public boolean mUseKhmerParser = false;
//	public List<String> mKhmerFontNames;
//	public UnicodeRender mUnicodeRender;
	
	private String mResultFileName = "";
	private String mSplitFileName = "";
	private String mSplitFilePath = "";
	boolean mUseFileSplit = false;
	
	int mPageBackgroundColor = -1;
	
	boolean mUsePrint = false;	// 인쇄지정여부
	int mCirculation = 1;		// 부수
	float mPrintScale = 1;		// pdf페이지 스케일
	
	// 생성시에 Khmer언어 목록을 담기
	public ubFormToPDF()
	{
/*		
		String _khmerFomtProp = common.getPropertyValue("pdfExport.khmerFontNames");
		
		if( _khmerFomtProp != null && _khmerFomtProp.equals("") == false )
		{
			mUseKhmerParser = true;
			
			mKhmerFontNames = Arrays.asList(_khmerFomtProp.split(","));
			
			mUnicodeRender = new UnicodeRender();
		}
*/		
	}
	
	public void setBackgroundColor( int _value )
	{
		mPageBackgroundColor = _value;
	}
	
	public void setUdpClient( UDPClient _value )
	{
		mUdpClient = _value;
	}
	
	public String getPDF_PRINT_VERSION() {
		return mPDF_PRINT_VERSION;
	}

	public void setPDF_PRINT_VERSION(String pDF_PRINT_VERSION) {
		this.mPDF_PRINT_VERSION = pDF_PRINT_VERSION;
	}
	
	public HashMap<String, Object> getChangeItemList() {
		return mChangeItemList;
	}

	public void setChangeItemList(HashMap<String, Object> value) {
		this.mChangeItemList = value;
	}
	
	public String getSESSION_ID() {
		return mSESSION_ID;
	}

	public void setSESSION_ID(String _value) {
		this.mSESSION_ID = _value;
	}
	
	public String getSplitFilePath()
	{
		return mSplitFilePath;
	}
	
	public void setSplitFilePath( String _value )
	{
		if( _value.indexOf("pdf") != -1)
		{
			_value = _value.replace(".pdf", "/");
		}
		
		mSplitFilePath = _value;
		
		File theDir = new File(mSplitFilePath);

		if (!theDir.exists()) {
		    try{
		        theDir.mkdir();
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		}
		
		mUseFileSplit = true;
	}
	
	public void chageSplitFilePath()
	{
		File theDir = new File(mSplitFilePath);
		if (theDir.isDirectory()) {
			theDir.delete();
		}
		
		mSplitFilePath = mSplitFilePath.substring(0, mSplitFilePath.lastIndexOf("/", mSplitFilePath.length()-2) + 1 );
		
		mUseFileSplit = false;
	}
	
	ArrayList<String> mSplitFileNames = null;
	public void setSplitFileName( String _value )
	{
		if(mSplitFileNames==null) mSplitFileNames = new ArrayList<String>();
		mSplitFileName = _value + ".pdf";
		mResultFileName = mSplitFilePath + mSplitFileName;
		
		mSplitFileNames.add(mResultFileName);
	}
	
	public void setResultFileName( String _value )
	{
		mResultFileName = _value;
	}
	
	public void setExportPath( String _value )
	{
		mExportPath = _value;
	}
	public void setExportDivCount( int _value )
	{
		mExportDivCount = _value;
	}
	
	public void setTotalPageCount( int _value )
	{
		mTotalPageCount = _value;
	}
	
	public boolean isStarting()
	{
		return (mBaos == null)? true:false;
	}
	
	//public FileOutputStream getPdfFileStream()
	public OutputStream getPdfFileStream()
	{
		return mBaos;
	}
	
	private String mPdfActionStr = "";
	public String PdfActionStr()
	{
		return mPdfActionStr;
	}
	
	public void clear()
	{
		if(mParam != null)
		{
			mParam.clear();
			mParam = null;
		}
		
		if(mProjectHm != null)
		{
			mProjectHm.clear();
			mProjectHm = null;
		}
		
		if(mWriter != null)
		{
			mWriter.close();
			mWriter = null;
		}
		
		if(mBaos != null) {
			try {
				mBaos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mBaos = null;
		
		if(mDocument != null)
		{
			mDocument.close();
			mDocument = null;
		}
	}
	
	private String mUseFitWindow = "true";
	private String mUuseUiDialog = "true";
	private int mPrintCirculation = 1;
	
	public String getUseFitWindow()
	{
		return mUseFitWindow;
	}
	public String getUseUiDialog()
	{
		return mUuseUiDialog;
	}
	public int getPrintCirculation()
	{
		return mPrintCirculation;
	}
	
	

	@SuppressWarnings("unchecked")
	//private void pdfPagePropertySetting( HashMap _hashData ) throws DocumentException, IOException
	private void pdfPagePropertySetting( JSONObject _hashData, Boolean usePrint, int _circulation ) throws DocumentException, IOException
	{
		log.info(getClass().getName() + "::" + "Call pdfPagePropertySetting()...");
		mProjectHm = (HashMap) _hashData.get("project");
		
		float _pWidth = 0;
		float _pHeight = 0;
		int _bgColorInt = 0;
		mWaterMarkTxt = "";
		
		if( _hashData.containsKey("pageWidth") && _hashData.containsKey("pageHeight") )
		{
			_pWidth = convertDpiFloat(_hashData.get("pageWidth"));
			_pHeight = convertDpiFloat(_hashData.get("pageHeight"));
		}
		else
		{
			_pWidth = convertDpiFloat(mProjectHm.get("pageWidth"));
			_pHeight = convertDpiFloat(mProjectHm.get("pageHeight"));
		}
		//_bgColorInt = ValueConverter.getInteger(mProjectHm.get("pageBackgroundColorInt"));
		mWaterMarkTxt = mProjectHm.get("waterMark") != null ? mProjectHm.get("waterMark").toString() : "";
		
		mPageWidth = _pWidth;
		mPageHeight = _pHeight;
		
		// pdf 출력을 위한 준비 ( waterMark Image가 있을경우 담아두기 )
		if( mProjectHm.get("WATER_MARK") != null && ((HashMap<String, Object>)  mProjectHm.get("WATER_MARK")).isEmpty() == false )
		{
			mWaterMarkImgMap = createWaterMarkImage( (HashMap<String, Object>)  mProjectHm.get("WATER_MARK") );
		}
		
//		Rectangle _pageSize = PageSize.A4;
//		Rectangle _pageSize = new Rectangle(_pWidth, _pHeight);
		
		Rectangle _pageSize = new Rectangle(_pWidth, _pHeight);
		
		if(mScale < 1)
		{
			mMarginX = (_pWidth-(_pWidth*mScale))/2;
			mMarginY = (_pHeight-(_pHeight*mScale))/2;
		}
		
		//Color _pageBgColor = new Color(_bgColorInt);
		
		if( mPageBackgroundColor != -1 ) _pageSize.setBackgroundColor(  new Color(mPageBackgroundColor) );
		else _pageSize.setBackgroundColor(Color.WHITE);
		
		mDocument = new Document(_pageSize, 0, 0, 0, 0);
		mWriter = PdfWriter.getInstance(mDocument, mBaos);
//		mWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_6);
		// bcprov-jdk14-124.jar 사용필요 , pdf
//		mWriter.setEncryption("123456".getBytes("UTF-8"), "123456".getBytes("UTF-8"), PdfWriter.AllowScreenReaders, PdfWriter.ENCRYPTION_AES_128 | PdfWriter.DO_NOT_ENCRYPT_METADATA);
		
		//TEST 문서 오픈시 바로 인쇄를 위한 액션 추가 테스트 최명진
		/**
		 */
		if(usePrint)
		{
			
			String _useFitWindow = "true";
			String _useUiDialog = "true";
			
			if( _hashData.containsKey("printOption") )
			{
				HashMap<String, String> _printOption = (HashMap<String, String>) _hashData.get("printOption");
				if(_printOption.containsKey(GlobalVariableData.UB_PRINT_FIT_SIZE))
				{
					mUseFitWindow = _printOption.get(GlobalVariableData.UB_PRINT_FIT_SIZE);
				}
				if(_printOption.containsKey(GlobalVariableData.UB_PRINT_USE_UI))
				{
					mUseUiDialog = _printOption.get(GlobalVariableData.UB_PRINT_USE_UI);
				}
			}
			
			mUseFitWindow = _useFitWindow;
			mUuseUiDialog = _useUiDialog;
			mPrintCirculation = _circulation;
			
			PdfAction action = new PdfAction( PdfAction.PRINTDIALOG);
			mPdfActionStr = "{bUI: "+mUseUiDialog+", bSilent: true, bShrinkToFit: " + mUseFitWindow + "}";
			//action.put(PdfName.JS, new PdfString("this.print({bUI: "+_useUiDialog+", bSilent: true, bShrinkToFit: " + _useFitWindow + "});\r"));
			action.put(PdfName.JS, new PdfString("this.print(" + mPdfActionStr + ");\r"));
			// bShrinkToFit : true = 맞추기, false = 실제크기
			// bSilent : true = 바로인쇄, false = 사용자 반응확인 
			// bUI : true = PRINT Dialog 사용, false = PRINT Dialog 미사용 
			mWriter.setOpenAction(action);
			mWriter.addViewerPreference(PdfName.NUMCOPIES, new PdfNumber( ValueConverter.getString(_circulation) ));
//			mWriter.addViewerPreference(PdfName.PRINTSCALING, PdfName.NONE);
//			mWriter.addViewerPreference(PdfName.PRINTSCALING, PdfName.APPDEFAULT);
//		    mWriter.setViewerPreferences(PdfWriter.PrintScalingNone);
		    
//			mWriter.setAdditionalAction(PdfWriter.WILL_PRINT, PdfAction.javaScript("app.alert('WILL_PRINT');",mWriter )); 
//			mWriter.setAdditionalAction(PdfWriter.DID_PRINT, PdfAction.javaScript("app.alert('DID_PRINT');",mWriter ));
			
		}
		
		//mBaseFont = BaseFont.createFont("HYGoThic-Medium", "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED);
		//mDefFont = new Font(mBaseFont, 10, Font.NORMAL, Color.black);
		mDefFont = FontFactory.getFont("맑은 고딕",BaseFont.IDENTITY_H,10,Font.NORMAL,Color.black);
		mBaseFont = mDefFont.getBaseFont();
		System.out.println("mDefFont=" + mDefFont.getFamilyname());
	}
	
	private Paragraph setParagraph(String text) { 
		return new Paragraph(text, mDefFont); 
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
	
	@SuppressWarnings("unchecked")
	private void CreatePdfLabel( HashMap<String, Object> _item ) throws DocumentException, UnsupportedEncodingException
	{
		String _pamClassName = _item.get("className").toString();
	    float _pamX = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "x") );
	    float _pamY =  ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "y" ) );
	    float _pamWidth = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "width" ) );
	    float _pamHeight = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "height") );

	    float _x = ObjectToFloat( _pamX );
	    float _y = mPageHeight - ObjectToFloat( _pamY );
	    float _w = ObjectToFloat( _pamX + _pamWidth );
	    float _h = mPageHeight - ObjectToFloat(_pamY + _pamHeight);
	    
	    String _id = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "id" ) );
	    String _type = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "type" ) );
	      
	    PdfContentByte _contentByte = mWriter.getDirectContent();
	    PdfGState _gState = new PdfGState();
	    Rectangle _bRect;
	    boolean _verticalRotateFlg = false;
	      
	    _contentByte.saveState();
	    
	      
	      // background 
	    float _bgAlpha = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "backgroundAlpha" ) );
	    Color _bgColor = new Color( ValueConverter.getInteger( UBComponent.getProperties( _item , _pamClassName , "backgroundColorInt" ) ) );
	    float _alpha = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "alpha" ) );
	    
	    // border
	    HashMap<String, Float> _rect = new HashMap<String, Float>();
	    ArrayList<HashMap<String, Object>> _content;
	    
	    HashMap<String, Float> _rotateDef = new HashMap<String, Float>();
	    
	    // text
	    String _pamTextAlign = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "textAlign" ) );
	    String _pamVerticalAlign = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "verticalAlign" ) );
	    String _pamFontStyle = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "fontStyle" ) );
	    float _pamFontSize = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "fontSize" ) );
	    int _pamFontColorInt = ValueConverter.getInteger( UBComponent.getProperties( _item , _pamClassName , "fontColorInt" ) );
	    
	    int _align = getAlignmentInt( _pamTextAlign );
	    String _verticalAlign = _pamVerticalAlign;
	    int _txStyle = fontStyleToInt(_pamFontStyle, "normal", "none");
	    
	    float _fontSize = _pamFontSize*0.9f;
	    
	    float _lineHeight = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "lineHeight" ) );
	    
	    if(mPDF_PRINT_VERSION.equals( GlobalVariableData.UB_PDF_PRINT_VERSION_1 ) )
	    {
		    if( _pamFontSize <= 12 )_fontSize =  Double.valueOf( Math.ceil( _fontSize ) ).floatValue();
	    }
	    else
	    {
	       // PontSize를 PT사이즈로 지정
	       _fontSize = Double.valueOf(_pamFontSize * 72 /  96 ).floatValue();
	    }
	      
	    Color _txColor = new Color(_pamFontColorInt);
	    //Color _txColor = new Color(ValueConverter.getInteger(_item.get("fontColorInt")));
	    
	    // font Color  처리
	    
	    String _fontName = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "fontFamily" ) );
	    String _fName = getDefFont(_fontName);
//    	log.info(getClass().getName() + "CreatePdfLabel ===> " + "_fontName : " + _fontName + ", _fName : " + _fName);
	    String _itemFontName = _fontName + "_" + _fontSize + "_" + _txStyle + "_" + _txColor;
	    
	    Font _itemFont = null;
	    if( _useFontMap.containsKey(_itemFontName) )
	    {
	    	_itemFont = _useFontMap.get(_itemFontName);
	    }
	    else
	    {
	    	if( _fName.equals("") )
	    	{
	    		_itemFont = FontFactory.getFont(_fontName, BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
	    		
	    		if(_itemFont.getBaseFont() == null)
	    		{
	    			if( mFontListAr.indexOf("나눔 고딕") != -1 )
	    			{
	    				_itemFont = FontFactory.getFont("나눔 고딕", BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
	    			}
	    			else
	    			{
	    				_itemFont = new Font(mBaseFont, _fontSize, _txStyle , _txColor);
	    			}
	    		}
	    	}
	    	else
	    	{
	    		_itemFont = FontFactory.getFont(_fName, BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
	    	}
	    	_useFontMap.put(_itemFontName, _itemFont);
	    }
	    

	    String _itemText = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "text" ) );
	      
	      // 줄바꿈 처리
	    _itemText = _itemText.replace("\\n", "\n").replaceAll("\\r", "\r");
	    
	    _itemText = convertItemText( _itemText, _fontName );
	      
		//일부 Link기능 추가		
		Font mLinkFont = new Font(mBaseFont, _fontSize, _txStyle , Color.blue);
		ArrayList<Chunk> _arrChunk  = new ArrayList <Chunk> ();
		boolean _splitLinkText = false; 
		boolean _useLinkText = false;
		
		String _hyperLinkText = "";
		
		String _hyperLinkUrl = "";		
		if( _item.get("ubHyperLinkUrl") != null  && _item.get("ubHyperLinkUrl").equals("null") == false  && _item.get("ubHyperLinkUrl").equals("") == false ){
			_hyperLinkUrl = _item.get("ubHyperLinkUrl").toString();

			if( !_hyperLinkUrl.endsWith("null") && _hyperLinkUrl.length() > 0 ){
				_hyperLinkUrl = URLDecoder.decode(_hyperLinkUrl, "UTF-8");
			}
			
			if( _item.get("ubHyperLinkText") != null && _item.get("ubHyperLinkText").equals("") == false ){
				_hyperLinkText = _item.get("ubHyperLinkText").toString();
			}
			
			if( !_hyperLinkText.endsWith("null") && _hyperLinkText.length() > 0 ){
				_hyperLinkText = URLDecoder.decode(_hyperLinkText, "UTF-8");
				//_hyperLinkText 값은 유일해야함
				String [] arrTemp = _itemText.split(_hyperLinkText);
				if(arrTemp.length>1){
					_arrChunk.add(new Chunk(arrTemp[0], _itemFont));
					_arrChunk.add(new Chunk(_hyperLinkText, _itemFont));
					_arrChunk.add(new Chunk(arrTemp[1], _itemFont));
					_splitLinkText = true;
				}else{
					_arrChunk.add(new Chunk(_itemText, _itemFont));
				}
			}else{
				_arrChunk.add(new Chunk(_itemText, _itemFont));
			}
			
			_useLinkText = true;
		}
		else
		{
			_arrChunk.add(new Chunk(_itemText, _itemFont));
		}    
	      
		String _pamFontWeight = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "fontWeight" ) );
		String _pamFontFamily = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "fontFamily" ) );
		String _pamTextDecoration = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "textDecoration" ) );
	      
	    Paragraph _txtp = new Paragraph();
	    for(Chunk _chunk:_arrChunk ){
	         // 자동 줄바꿈시 문자단위로 줄바꿈 되지 않도록 하기 위하여 SplitCharacter 지정
	         _chunk.setSplitCharacter(new SplitCharacter() {         
	          @Override
	          public boolean isSplitCharacter(int start, int current, int end, char[] cc,
	                PdfChunk[] ck) {
	               return false;
	          }
	       });
		   if( !"normal".equals(_pamFontWeight)  ) // bold 의 두깨를 조절.
	       {
	          float _blodNum = (Math.round( ( _fontSize / 35f ) * 10f ) / 10f );         
	            
	          _chunk.setTextRenderMode( PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE , _blodNum, _txColor);
	       }
	         
	       if( !"none".equals(_pamTextDecoration) )
	       {
//	          _chunk.setUnderline(1f,-3f);
	          float _thickness = (_fontSize/2) / 10;
	          if( _thickness < 0.8f ) _thickness = 0.8f;
	          _chunk.setUnderline(_txColor, _thickness, 0f, 0.5f, -0.2f ,PdfContentByte.LINE_CAP_BUTT);
	       }
	         
	       if(_useLinkText)
	       {
	          if(!_splitLinkText){
	             _chunk.setAnchor(_hyperLinkUrl);   
	          }else{
	               
	             if(_chunk.getContent().equals(_hyperLinkText)){
	                _chunk.setAnchor(_hyperLinkUrl);
	                _chunk.setFont( mLinkFont);               
	                float _thickness = (_fontSize/2) / 10;
	                if( _thickness < 0.8f ) _thickness = 0.8f;
	                _chunk.setUnderline(Color.blue, _thickness, 0f, 0.5f, -0.2f ,PdfContentByte.LINE_CAP_BUTT);
	             }
	          }         
	       }
	         
	       _txtp.add(_chunk);
	    }   
	    
	   // _txtp.setLeading(_fontSize * 1.16f);
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
//	       float _readingSize = (float) Math.floor( _fontSize * 1.2f );
	      // float _readingSize = _fontSize * 1.2f;
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

	        optionMap.put("width", new float[]{ _pamWidth });
	        optionMap.put("height",new float[]{ mPageHeight , mPageHeight });
	        optionMap.put("fontSize", new float[]{ _fontSize });
	        optionMap.put("lineHeight", new float[]{ _lineHeight }); 
	        //optionMap.put("lineHeight", new float[]{1.2f});//TODO LineHeightTEST
	         
		    float _pamPadding = ValueConverter.getFloat( UBComponent.getProperties( _item, _pamClassName , "padding" , 8 ));
	        
	        if( _item.containsKey("padding") == false ){
	           optionMap.put("padding", new float[]{ 3 });
	        }else{
	           optionMap.put("padding", new float[]{ _pamPadding });
	        }
	        
	        HashMap<String,Object> resultMap = StringUtil.getSplitCharacter( _itemText, optionMap, _pamFontWeight, _pamFontFamily, _pamFontSize,-1 );
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
           float _rotateNum = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "textRotate" , 0 ) );
	       if(0 != _rotateNum )
	       {
	          _verticalRotateFlg = true;
	          
	          _rotateDef.put("x", _x);
	          _rotateDef.put("y", _y);
	          _rotateDef.put("w", _w);
	          _rotateDef.put("h", _h);
	       }
	    }
	    else
	    {
	         
	    }
      
   //  테이블의 Border의 Right값이 없으면 Width를 borderWidth값만큼 빼고 border의 bottom값이 없을경우 height값의 borderWidth값만큼 빼기 2016-03-22
      /**
//      if( _id.split("_")[0].equals("TB"))
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
      
		/** TEST */
	    _bRect = new Rectangle(_x, _y, _w, _h);
	      
	    _bRect.setBackgroundColor(_bgColor);
	    _gState.setFillOpacity(_bgAlpha*_alpha);
	    _contentByte.setGState(_gState);
	      
	    _bRect.setBorder(0);
	    _contentByte.rectangle(_bRect);
	   
//	    int[] _cmykColor = rgbToCmyk(_bgColor.getRed(), _bgColor.getGreen(), _bgColor.getBlue());
//	    _contentByte.setCMYKColorFill(_cmykColor[0], _cmykColor[1], _cmykColor[2], _cmykColor[3]);
//	    _contentByte.rectangle(_x, _h, _w - _x, _y - _h);
//	    _contentByte.fill();

	    _rect.put("top", _y);
	    _rect.put("left", _x);
	    _rect.put("right", _w);
	    _rect.put("bottom", _h);
      
	    String _pamORIGINAL_TABLE_ID = ValueConverter.getString( UBComponent.getProperties( _item , _pamClassName , "ORIGINAL_TABLE_ID" ) );
	    ArrayList<String> _pamBorderSide = (ArrayList<String>) UBComponent.getProperties( _item , _pamClassName , "borderSide" );
	    ArrayList<String> _pamBorderTypes = (ArrayList<String>) UBComponent.getProperties( _item , _pamClassName , "borderTypes" );
	    ArrayList<Integer> _pamBorderColorsInt =  (ArrayList<Integer>) UBComponent.getProperties( _item , _pamClassName , "borderColorsInt" );
	    ArrayList<Integer> _pamBorderWidths =  (ArrayList<Integer>) UBComponent.getProperties( _item , _pamClassName , "borderWidths" );
	    
	    if(_item.containsKey("ORIGINAL_TABLE_ID") && "".equals(_pamORIGINAL_TABLE_ID) == false )
	    {
		    ArrayList<String> _pamBorderOriginalTypes = (ArrayList<String>) UBComponent.getProperties( _item , _pamClassName , "borderOriginalTypes" );
		    ArrayList<Boolean> _pamBeforeBorderType = (ArrayList<Boolean>)  UBComponent.getProperties( _item , _pamClassName , "beforeBorderType" );
	       String _tbId = _pamORIGINAL_TABLE_ID;
	       if( !mTBborder.containsKey(_tbId))
	       {
	          _content = new ArrayList<HashMap<String,Object>>();
	       }
	       else
	       {
	          _content = mTBborder.get(_tbId);
	       }
	       mTBborder.put( _tbId, tbBorderSetting( _pamBorderSide , _pamBorderTypes , _pamBorderColorsInt , _pamBorderWidths , _rect , _content , _alpha , _pamBorderOriginalTypes , _pamBeforeBorderType ) );
	    }
	    else
	    {
	       _contentByte = setBorderSetting( _contentByte , _pamBorderSide , _pamBorderTypes , _pamBorderColorsInt , _pamBorderWidths , _rect , _alpha );
	    }
	    _contentByte.restoreState();
	      
	    _contentByte.saveState();
	   
	      
	    if( !_type.equals("rotateLabel") && (_y - _h) < _txtp.getLeading() )
	    {
	       _txtp.setLeading((_y-_h) - 1f);
	    }
	      
	    if( _type.equals("rotateLabel"))
	    {
           float _rotateNum = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "textRotate" , 0 ) );
	       if(0 != _rotateNum )
	       {
	          _verticalRotateFlg = true;
	          
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
	    float _pamPadding = ObjectToFloat( UBComponent.getProperties( _item , _pamClassName , "padding" , 0 ) );
      // padding 값이 크게 들어가서 기본패딩 들어있던 부분 생략. 2016-09-26 공혜지
      	float _padding = ObjectToFloat(0.5);
//      	float _padding = ObjectToFloat(0.0f);
//		if( _item.containsKey("padding") && _pamPadding != 0 )
      	if( _pamPadding != 0 )
		{
			_padding = _padding + _pamPadding;
		}
		
		float _verticalPadding = _pamPadding;
	    
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
      
       float _H = 0;
       
       
       // VerticalAlign이 TOP이나 BOTTOM일때 사용할 padding값을 만들기
   		_H = (_cY - _cH) - (_txtp.getLeading()*_lineCount);
	    if( _lineCount > 1 ) _H = _H - Double.valueOf(_txtp.getLeading()*0.2).floatValue();
	    
		if( _H < _verticalPadding )
		{
			_verticalPadding = (_H > 0)?_H:0;
		}
		
       
	    if( !_verticalAlign.equals("") && !_type.equals("stretchLabel") )
	    {
	       if( !_verticalRotateFlg )
	       {
	          if( _verticalAlign.equals("middle"))
	          {
	             _cY = _cY - ((_fontY - _cH) / 2 );
	             if( _lineCount > 1 )
	             {
	            	 _cY = _cY + ( Double.valueOf(_txtp.getLeading() * 0.2).floatValue() );
	             }
	             
	          }
	          else if( _verticalAlign.equals("bottom"))
	          {
	        	  
	        	_cY = (_cY - (_fontY - _cH));
	        	  
	        	if( _lineCount > 1 )
	            {
	        		_cY = _cY + ( Double.valueOf(_txtp.getLeading() * 0.2).floatValue() );
	            }
	        	 
	        	// Padding값을 지정하기 위해 추가
	        	 _cY = _cY + _verticalPadding;
	          }	
	          else if( _verticalAlign.equals("top") )
	          {
				if( _lineCount > 1 )
				{
					_cY = _cY + ( Double.valueOf(_txtp.getLeading() * 0.2).floatValue() );
				}
	        	 
	        	// Padding값을 지정하기 위해 추가
	        	_cY = _cY - _verticalPadding;
	          }
	          
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
      
	    // item의 Alpha처리
	    if( _alpha <  1.0 )
	    {
	    	_gState = new PdfGState();
	    	_gState.setFillOpacity( _alpha );
	    	_gState.setStrokeOpacity( _alpha );
	    	_contentByte.setGState(_gState);
	    }
	    
       float _rotateNum = ValueConverter.getFloat( UBComponent.getProperties( _item , _pamClassName , "textRotate" , 0 ) );
	    if( !_type.equals("rotateLabel") || _rotateNum == 0 )
	    {
	    	//글상자가 leading 보다 작은경우 사이즈 맞춤!!
	    	float _itemH = _cY - _cH;
	    	float tempY = _itemH - (_txtp.getLeading()*_lineCount);
	    	
	    	if( _lineCount > 1 ) tempY = tempY - Double.valueOf(_txtp.getLeading()*0.2).floatValue();
	    	
	    	if(tempY<0){
	    		float _tempMargin = (_txtp.getLeading()*_lineCount) - _itemH;
	    		_cH = _cH - _tempMargin;		    	
	    	}
	    }
	    
		drawColumnText( _contentByte, _cX, _cY, _cW, _cH, _txtp, false);

		_contentByte.restoreState();
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
	
	private HashMap<String, Float> rotationItemImage( PdfContentByte _canvas , float _x , float _y , float _w , float _h , float _rotation )
	{
		HashMap<String, Float> _retHm = new HashMap<String, Float>();
		double pi = _rotation * Math.PI / 180;
		float cos = (float) Math.cos(pi);
		float sin = (float) Math.sin(pi);
		
		_canvas.concatCTM(cos, sin, -sin, cos, 0, 0);
		
		double pi2 = pi * -1;
		
		HashMap<String, Float> _xyPosition = rotationPosition( _x, _y+_h, pi2 ); 
		
		_retHm.put("x", _xyPosition.get("x") );
		_retHm.put("y", _xyPosition.get("y") - _h );
		_retHm.put("w", _xyPosition.get("x"));
		_retHm.put("h", _xyPosition.get("y"));

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
	
	private PdfContentByte setBorderSetting(PdfContentByte _contentByte , ArrayList<String> _sides , ArrayList<String> _types , ArrayList<Integer> _colors , ArrayList<Integer> _widths , HashMap<String, Float> _rect )
	{
		return setBorderSetting( _contentByte , _sides , _types , _colors , _widths , _rect, 1.0f);
	}
	
	private PdfContentByte setBorderSetting(PdfContentByte _contentByte , ArrayList<String> _sides , ArrayList<String> _types , ArrayList<Integer> _colors , ArrayList<Integer> _widths , HashMap<String, Float> _rect, float _alpha )
	{
		
//		Color _boColor = new Color(ObjectToInt(_item.get("borderColorInt")));
//		_bRect.setBorderWidth(SizeToInt(_item.get("borderWidth")));		
//		_bRect.setBorderColor(_boColor);		
		
		PdfGState _gState = new PdfGState();
		float _doubleGap = ObjectToFloat(5);
		HashMap<String, String> _doubleBorderMap = new HashMap<String, String>();
		
		int _sLength = _sides.size();
		for (int i = 0; i < _sLength; i++) 
		{
			String _side = _sides.get(i);
			String _type = _types.get(i);
		
			if(_type.toUpperCase().equals("DOUBLE"))
			{
				_doubleBorderMap.put(_side, _type);
			}
		}
		
		float _sp1 = 0;
		float _sp2 = 0;
		float _ep1 = 0;
		float _ep2 = 0;

		for (int i = 0; i < _sLength; i++) 
		{
			String _side = _sides.get(i);
			String _type = _types.get(i);
			Color _color = new Color(_colors.get(i));
			int _width = _widths.get(i);
			
			//PDF 저장시 라벨의 Border Width와 table의 borderWidth 값의 차이가 나서 수정 2017-02-20 최명진
			float _widthF = ObjectToFloat(_width);
//			float _widthF = ValueConverter.getFloat(_hm.get("width"));
			_widthF = _widthF - 0.3f;
			
			if( _type.equals("none"))
			{
				continue;
			}
			
//			_color = new Color( Integer.valueOf("FF", 16),Integer.valueOf("FF", 16),Integer.valueOf("00", 16), 240 );
			
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
				
				_sp1 = _rect.get("left");
				_sp2 = _rect.get("top");
				_ep1 = _rect.get("right");
				_ep2 =  _rect.get("top");
			}
			else if( _side.equals("left"))
			{
				_contentByte.moveTo(_rect.get("left"), _rect.get("top")); 
				_contentByte.lineTo(_rect.get("left"), _rect.get("bottom"));
				_contentByte.stroke(); 
				
				_sp1 = _rect.get("left");
				_sp2 = _rect.get("top");
				_ep1 = _rect.get("left");
				_ep2 =  _rect.get("bottom");
			}
			else if(_side.equals("bottom"))
			{
				_contentByte.moveTo(_rect.get("left"), _rect.get("bottom")); 
				_contentByte.lineTo(_rect.get("right"), _rect.get("bottom"));
				_contentByte.stroke(); 
				
				_sp1 = _rect.get("left");
				_sp2 = _rect.get("bottom");
				_ep1 = _rect.get("right");
				_ep2 =  _rect.get("bottom");
			}
			else if( _side.equals("right"))
			{
				_contentByte.moveTo(_rect.get("right"), _rect.get("top")); 
				_contentByte.lineTo(_rect.get("right"), _rect.get("bottom"));
				_contentByte.stroke(); 
				
				_sp1 = _rect.get("right");
				_sp2 = _rect.get("top");
				_ep1 = _rect.get("right");
				_ep2 =  _rect.get("bottom");
			}
			
			//border처리
			// double 끼리 만나는 부분은 갭만큼 띄워서 시작할 필요가 있음.
			// double 일때 한번더 선을 긋도록 처리 
			if( _type.toUpperCase().equals("DOUBLE")  ) 
			{
				if( _side.equals("left")||_side.equals("right"))
				{
					if( _doubleBorderMap.containsKey("top") )
					{
						_sp2 = _sp2 - _doubleGap;
					}
					if( _doubleBorderMap.containsKey("bottom") )
					{
						_ep2 = _ep2 + _doubleGap;
					}
					
					if(_side.equals("left") )
					{
						_sp1 = _sp1+_doubleGap;
						_ep1 = _ep1+_doubleGap;
					}
					else if(_side.equals("right") )
					{
						_sp1 = _sp1-_doubleGap;
						_ep1 = _ep1-_doubleGap;
					}
				}
				else
				{
					if( _doubleBorderMap.containsKey("left") )
					{
						_sp1 = _sp1 + _doubleGap;
					}
					if( _doubleBorderMap.containsKey("right") )
					{
						_ep1 = _ep1 - _doubleGap;
					}
					
					if(_side.equals("top") )
					{
						_sp2 = _sp2-_doubleGap;
						_ep2 = _ep2-_doubleGap;
					}
					else if(_side.equals("bottom") )
					{
						_sp2 = _sp2+_doubleGap;
						_ep2 = _ep2+_doubleGap;
					}
				}
				
				_contentByte.moveTo(_sp1,_sp2); 
				_contentByte.lineTo(_ep1,_ep2);
				_contentByte.stroke(); 
			}
			
			
			_contentByte.restoreState();
		}
		
		return _contentByte;
	}
	
	private PdfContentByte borderDot( PdfContentByte _contentByte, String _side, String _type, float _width, HashMap<String, Float> _rect , Color _color )
	{
		
		boolean _isVertical = false;
		
		float _sp1 = 0;
		float _sp2 = 0;
		float _ep1 = 0;
		float _ep2 = 0;
		
		boolean isLast = false;
		float _addHpoint = 0;
		float _addVpoint = 0;

		// Dash Dot Dot일때 Border처리 
		String[] _borderData = _type.split("_");
		
		_width = (_width > 0.5)? Double.valueOf( 0.5 + ( (_width - 0.5) * 0.5 ) ).floatValue():_width;

		_contentByte.setColorFill(_color);
		_contentByte.setLineWidth( _width );
		
		if( _side.equals("top"))
		{
			_sp1 = _rect.get("left");
			_sp2 = _rect.get("top");
			_ep1 = _rect.get("right");
			_ep2 =  _rect.get("top");
			
			_addHpoint = _width*4;
		}
		else if( _side.equals("left"))
		{
			_sp1 = _rect.get("left");
			_sp2 = _rect.get("top");
			_ep1 = _rect.get("left");
			_ep2 =  _rect.get("bottom");
			
			_addVpoint = _width*4;
		}
		else if(_side.equals("bottom"))
		{
			_sp1 = _rect.get("left");
			_sp2 = _rect.get("bottom");
			_ep1 = _rect.get("right");
			_ep2 =  _rect.get("bottom");
			
			_addHpoint = _width*4;
		}
		else if( _side.equals("right"))
		{
			_sp1 = _rect.get("right");
			_sp2 = _rect.get("top");
			_ep1 = _rect.get("right");
			_ep2 =  _rect.get("bottom");
			
			_addVpoint = _width*4;
		}
		
		int _chk = 0;
		
		float _dotSzie = Double.valueOf( _width * 2 ).floatValue();
		
		while( !isLast )
		{
			if(_borderData[_chk].toUpperCase().equals("DASH"))
			{
				_contentByte.moveTo(_sp1, _sp2);
				
				if( _addHpoint > 0 && _ep1 - _sp1 < _addHpoint ) break;
				if( _addVpoint > 0 && _sp2 - _ep2 < _addVpoint ) break;
				
				_sp1 = _sp1 + (_addHpoint*3);
				_sp2 = _sp2 - (_addVpoint*3);
				
				if( _sp1 > _ep1 )
				{
					_sp1 = _ep1;
				}
				if( _sp2 < _ep2 )
				{
					_sp2 = _ep2;
				}
				
				_contentByte.lineTo(_sp1, _sp2);
				_contentByte.stroke();
			}
			else
			{
				if( !( _sp1 > _ep1 || _sp2 < _ep2 ) )
				{
					_contentByte.arc(_sp1 - (_dotSzie/2), _sp2 - (_dotSzie/2), _sp1 + (_dotSzie/2), _sp2 + (_dotSzie/2), 0, 360);
					_contentByte.fill();
				}
				
			}
			
			_sp1 = _sp1 + _addHpoint;
			_sp2 = _sp2 - _addVpoint;
			
			if( _sp1 > _ep1 || _sp2 < _ep2 )
			{
				isLast = true;
			}
			
			_chk++;
			if( _chk >= _borderData.length )_chk = 0;
		}
		
		_contentByte.closePath();
		return _contentByte;
	}
	private PdfContentByte borderDotTbl( PdfContentByte _contentByte, String _side, String _type, float _width, float _startp1, float _startp2, float _endp1, float _endp2 , Color _color )
	{
		boolean isLast = false;
		float _addHpoint = 0;
		float _addVpoint = 0;
		
		float _sp1 = (_startp1>_endp1)?_endp1:_startp1;
		float _sp2 = (_startp2<_endp2)?_endp2:_startp2;
		float _ep1 = (_startp1<_endp1)?_endp1:_startp1;
		float _ep2 = (_startp2>_endp2)?_endp2:_startp2;
		
		_width = (_width > 0.5)? Double.valueOf( 0.5 + ( (_width - 0.5) * 0.5 ) ).floatValue():_width;
		
		_contentByte.setColorFill(_color);
		_contentByte.setLineWidth( _width );
		
		// Dash Dot Dot일때 Border처리 
		String[] _borderData = _type.split("_");
		
		if( _sp1 == _ep1)
		{
			_addVpoint = _width*4;
		}
		else
		{
			_addHpoint = _width*4;
		}
		
		int _chk = 0;
		float _dotSzie = Double.valueOf( _width * 2 ).floatValue();
		
		while( !isLast )
		{
			if(_borderData[_chk].toUpperCase().equals("DASH"))
			{
				_contentByte.moveTo(_sp1, _sp2);
				
				if( _addHpoint > 0 && _ep1 - _sp1 < _addHpoint ) break;
				if( _addVpoint > 0 && _sp2 - _ep2 < _addVpoint ) break;
				
				_sp1 = _sp1 + (_addHpoint*3);
				_sp2 = _sp2 - (_addVpoint*3);
				
				if( _sp1 > _ep1 ) _sp1 = _ep1;
				if( _sp2 < _ep2 ) _sp2 = _ep2;
				
				_contentByte.lineTo(_sp1, _sp2);
				_contentByte.stroke();
			}
			else
			{
				if( !( _sp1 > _ep1 || _sp2 < _ep2 ) )
				{
					_contentByte.arc(_sp1 - (_dotSzie/2), _sp2 - (_dotSzie/2), _sp1 + (_dotSzie/2), _sp2 + (_dotSzie/2), 0, 360);
					_contentByte.fill();
				}
				
			}
			
			_sp1 = _sp1 + _addHpoint;
			_sp2 = _sp2 - _addVpoint;
			
			if( _sp1 > _ep1 || _sp2 < _ep2 )
			{
				isLast = true;
			}
			
			_chk++;
			if( _chk >= _borderData.length )_chk = 0;
		}
		
		return _contentByte;
	}
	
	private ArrayList<HashMap<String, Object>> tbBorderSetting(ArrayList<String> _sides , ArrayList<String> _types , ArrayList<Integer> _colors , ArrayList<Integer> _widths , HashMap<String, Float> _rect , ArrayList<HashMap<String, Object>> _content , float _alpha, ArrayList<String> _originalTypes, ArrayList<Boolean> _beforeBorderType )
	{
		
//		Color _boColor = new Color(ObjectToInt(_item.get("borderColorInt")));
//		_bRect.setBorderWidth(SizeToInt(_item.get("borderWidth")));		
//		_bRect.setBorderColor(_boColor);		
		
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
			Color _color = new Color(_colors.get(i));
			String _oriType = _type;
			
			if(_originalTypes != null && _originalTypes.size() > i-1) _oriType =  _originalTypes.get(i);

			int _width = _widths.get(i);
			
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
					s2 = _defaltY - (gap/2); 					
					doubleGap = _isDoubleRight && _isRightShare ? (gap/2):(_isDoubleRight?gap:0); 
					e1 = _defaltW - doubleGap;
					e2 = _defaltY - (gap/2);
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
						ds2 = _defaltY- gap; 	
						doubleGap = _isDoubleRight && _isRightShare ? (gap/2):(_isDoubleRight?gap:0);
						de1 = _defaltW - doubleGap;
						de2 = _defaltY - gap;
						
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
						ds2 = _defaltY - doubleGap; 	
						doubleGap = _isDoubleBottom && _isBottomShare ? (gap/2):(_isDoubleBottom?gap:0);
						de1 =  _defaltW - gap;
						de2 = _defaltH + doubleGap;	
						
						doubleBorderSet = new HashMap<String, Object>();
						doubleBorderSet  = (HashMap<String, Object>)borderSet.clone(); 
						doubleBorderSet.put("sp",ds1 + "," + ds2);
						doubleBorderSet.put("ep",de1 + "," + de2);
						rtnList.add(doubleBorderSet);		
					}
					
				}else if(_type.equals("none") && _oriType.equals("double") ){
					doubleGap = _isDoubleTop && _isTopShare ? (gap/2):(_isDoubleTop?gap:0); 
					s1 = _defaltW - (gap/2); 
					s2 = _defaltY - doubleGap; 
					doubleGap = _isDoubleBottom && _isBottomShare ? (gap/2):(_isDoubleBottom?gap:0); 					
					e1 = _defaltW - (gap/2);
					e2 =  _defaltH + doubleGap ;
					
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
						ds2 = _defaltH + gap; 	
						doubleGap = _isDoubleLeft && _isLeftShare ? (gap/2):(_isDoubleLeft?gap:0); 
						de1 = _defaltX + doubleGap;
						de2 =  _defaltH + gap;						
						doubleBorderSet = new HashMap<String, Object>();
						doubleBorderSet  = (HashMap<String, Object>)borderSet.clone(); 
						doubleBorderSet.put("sp",ds1 + "," + ds2);
						doubleBorderSet.put("ep",de1 + "," + de2);
						rtnList.add(doubleBorderSet);		
					}
					
				}else if(_type.equals("none") && _oriType.equals("double") ){
					doubleGap = _isDoubleRight && _isRightShare ? (gap/2):(_isDoubleRight?gap:0); 
					s1 = _defaltW -doubleGap ; 
					s2 = _defaltH + (gap/2); 
					doubleGap = _isDoubleLeft && _isLeftShare ? (gap/2):(_isDoubleLeft?gap:0); 				
					e1 =  _defaltX+doubleGap;
					e2 =  _defaltH + (gap/2);					
					//none type => double 변경하여 넘김
					borderSet.put("type",_oriType);
				}
			}
			
			if( _side.equals("left") && !_type.equals("none"))
			{
				if( _type.equals("double_share")){
					doubleGap = _isDoubleBottom  && _isBottomShare ?(gap/2):(_isDoubleBottom?gap:0); 
					s1 = _defaltX + (gap/2); 
					s2 =  _defaltH + doubleGap; 					
					doubleGap = _isDoubleTop && _isTopShare ? (gap/2):(_isDoubleTop?gap:0); 
					e1 =  _defaltX + (gap/2);
					e2 = _defaltY - doubleGap;
				}else{
					if( _isLeftShare){//공유 실선 인경우
						doubleGap = _isDoubleBottom  && _isBottomShare ?(gap/2):(_isDoubleBottom?gap:0);	
						if(_beforeBorderType != null && _beforeBorderType.get(3))doubleGap = 0;//top 변경인경우
						s1 = _defaltX;
						s2 = _defaltH + doubleGap;						
						doubleGap = _isDoubleTop  && _isTopShare ?(gap/2):(_isDoubleTop?gap:0); 
						if(_beforeBorderType != null && _beforeBorderType.get(0))doubleGap = 0;//top 변경인경우
						e1 = _defaltX;
						e2 = _defaltY - doubleGap;
					}else{
						s1 = _defaltX; 
						s2 = _defaltH; 	
						e1 = _defaltX;
						e2 = _defaltY;
					}
					if(_type.equals("double")){
						doubleGap = _isDoubleBottom  && _isBottomShare ?(gap/2):(_isDoubleBottom?gap:0); 
						ds1 =  _defaltX + gap; 
						ds2 = _defaltH + doubleGap; 	
						doubleGap = _isDoubleTop && _isTopShare ? (gap/2):(_isDoubleTop?gap:0); 
						de1 = _defaltX + gap;
						de2 = _defaltY - doubleGap;						
						
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
	
	private void setTBborderSetting()
	{
		PdfGState _gState = new PdfGState();	
		float _doubleGap = ObjectToFloat(5);
		String _chkBorderType = "";
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
				
				PdfContentByte _contentByte = mWriter.getDirectContent();
				
		    	float _alpha = 1.0f;
		    	if( _hm.containsKey("alpha") ) _alpha =  Float.valueOf( _hm.get("alpha").toString() );
		    	if( _alpha <  1.0)
		    	{
		    		_gState = new PdfGState();
		    		_gState.setStrokeOpacity( _alpha );
		    		_gState.setFillOpacity( _alpha );
		    		_contentByte.setGState(_gState);
		    	}

				_contentByte.saveState();
//				_contentByte.setLineWidth( _widthF );
				_contentByte.setColorStroke(_color);
				
				_chkBorderType = _type.replace("_share", "");
				
				if( _chkBorderType.toUpperCase().indexOf("DOT") != -1 || _chkBorderType.toUpperCase().equals("DASH") )
			    {
					_contentByte.setLineCap(PdfContentByte.LINE_CAP_ROUND);	
					borderDotTbl(_contentByte, _side, _chkBorderType, _widthF, _sp1, _sp2, _ep1, _ep2, _color);
			    	_contentByte.restoreState();
			    	continue;
			    }
				_contentByte.setLineWidth( _widthF );
				_contentByte = lineType_Setting( _type.replace("_share", "") , _contentByte , _width );
				
				if( _type.equals("none")==false )
				{
					_contentByte.moveTo(_sp1,_sp2); 
					_contentByte.lineTo(_ep1,_ep2);
				}
				
				
				// double 끼리 만나는 부분은 갭만큼 띄워서 시작할 필요가 있음.
				// double 일때 한번더 선을 긋도록 처리 
				// 앞에서 Double 처리하여 주석처리
//				if( _originalType.toUpperCase().equals("DOUBLE") || _type.toUpperCase().equals("DOUBLE")  ) 
//				{
//					if( _side.equals("left")||_side.equals("right"))
//					{
//						if( _doubleBorderMap.containsKey("top") )
//						{
//							_sp2 = _sp2 - _doubleGap;
//						}
//						if( _doubleBorderMap.containsKey("bottom") )
//						{
//							_ep2 = _ep2 + _doubleGap;
//						}
//						
//						if(_side.equals("left") )
//						{
//							_sp1 = _sp1+_doubleGap;
//							_ep1 = _ep1+_doubleGap;
//						}
//						else if(_side.equals("right") )
//						{
//							_sp1 = _sp1-_doubleGap;
//							_ep1 = _ep1-_doubleGap;
//						}
//					}
//					else
//					{
//						if( _doubleBorderMap.containsKey("left") )
//						{
//							_sp1 = _sp1 + _doubleGap;
//						}
//						if( _doubleBorderMap.containsKey("right") )
//						{
//							_ep1 = _ep1 - _doubleGap;
//						}
//						
//						if(_side.equals("top") )
//						{
//							_sp2 = _sp2-_doubleGap;
//							_ep2 = _ep2-_doubleGap;
//						}
//						else if(_side.equals("bottom") )
//						{
//							_sp2 = _sp2+_doubleGap;
//							_ep2 = _ep2+_doubleGap;
//						}
//					}
//					
//					_contentByte.moveTo(_sp1,_sp2); 
//					_contentByte.lineTo(_ep1,_ep2);
//				}
				
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
	
	private void CreatePdfDoImage( HashMap<String, Object> _item ) throws MalformedURLException, IOException, DocumentException
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y")) - ObjectToFloat(_item.get("height"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
	
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		PdfGState _alpha = new PdfGState();
		
//		var _bitmapD:BitmapData = _item.source;
		
		String _imageUrl = "";
		
		_imageUrl = ValueConverter.getString(_item.get("src"));
		//Image _image = Image.getInstance(new URL(mUrl + _imageUrl));	
		//Image _image = common.getRemoteImageFile(_imageUrl);
		Image _image = common.getLocalImageFile(_imageUrl,mSESSION_ID);
		if(_image != null)
		{
			_contentByte.saveState();
			
			 if( _item.containsKey("rotate") && Float.valueOf(_item.get("rotate").toString())!= 0 )
			 {
				float _rotateNum = ValueConverter.getFloat(_item.get("rotate"));
				
				HashMap<String, Float> _posionData = rotationItemImage(_contentByte, _x, _y, _w, _h, _rotateNum * -1);
				    
				_x = _posionData.get("x");
				_y = _posionData.get("y");
				//_w = _posionData.get("w");
				//_h = _posionData.get("h");
			    
//				_image.setRotationDegrees( _rotateNum );
			 }
			 			
//			_image.scalePercent(10, 10);
			_alpha.setFillOpacity(1);
			_image.setInterpolation(true);
//			_image.setInterpolation(false);
				
			_contentByte.setGState(_alpha);
			_contentByte.addImage(_image, _w, 0, 0, _h, _x, _y);
			
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
		
//		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@ ubFormToPDF::CreatePdfLine===>mPageHeight=" + mPageHeight + ", _y="+ _y + ", [_x1=" + _x1 + ", _y1=" + _y1 + "], [_x2=" + _x2 + ", _y2=" + _y2 + "]");
		
		float _h = Math.abs(_y2-_y1);
		
		float _sX = 0;
		float _sY = 0;
		float _eX = 0;
		float _eY = 0;
		
		_sX = _x + _x1;
//		_sX = _x1;
		_sY = _y - _y1;	
		
		_eX = _x + _x2;
//		_eX = _x2;
		_eY = _y - _y2;

//		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@ ubFormToPDF::CreatePdfLine===>[_sX=" + _sX + ", _sY=" + _sY + "], [_eX=" + _eX + ", _eY=" + _eY + "]");
		
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
		Image _image=null;
		if( _hasDictionary ){
//		if( false ){
			_image = _imgDictionary.getmPDFImage();
		}else{
			_image = common.getLocalImageFile(_imageUrl, mSESSION_ID);	
			//_imgDictionary.setmPDFImage(_image);
			
			_newImgDictionary =mImageDictionary.createPDFDictionaryData(_imageUrl, _image);
		}
		
		
		String _hyperLinkUrl = "";
		if( _item.get("ubHyperLinkUrl") != null ){
			_hyperLinkUrl = _item.get("ubHyperLinkUrl").toString();
		}
		if( !_hyperLinkUrl.endsWith("null") && _hyperLinkUrl.length() > 0 ){
			_image.setAnnotation(new Annotation(0, 0, 0, 0, _item.get("ubHyperLinkUrl").toString()));
		}
		
		
		
   		if(_image != null)
   		{
			 if( _item.containsKey("rotate") && Float.valueOf(_item.get("rotate").toString())!= 0 )
			 {
				float _rotateNum = ValueConverter.getFloat(_item.get("rotate"));
				
				HashMap<String, Float> _posionData = rotationItemImage(_contentByte, _x, _y, _w, _h, _rotateNum * -1);
				    
				_x = _posionData.get("x");
				_y = _posionData.get("y");
			 }
			 
			_image.scalePercent(200);
			_alpha.setFillOpacity(_itemAlpha);
			_contentByte.setGState(_alpha);
			_image.setInterpolation(true);
	
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
		
		try {
			if(bAr!=null) _image = Image.getInstance(bAr);
		} catch (Exception e) {
			// TODO: handle exception
		}
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
	
	private void CreatePdfSfsImage( HashMap<String, Object> _item ) throws MalformedURLException, IOException, DocumentException
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y")) - ObjectToFloat(_item.get("height"));
		float _w = ObjectToFloat(_item.get("width"));
		float _h = ObjectToFloat(_item.get("height"));
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		PdfGState _alpha = new PdfGState();
		
//		float _itemAlpha = ValueConverter.getFloat(_item.get("alpha"));
		float _itemAlpha =  (_item.get("alpha") != null)?ValueConverter.getFloat(_item.get("alpha")):1;
		
//		var _bitmapD:BitmapData = _item.source;
		
		_contentByte.saveState();
		
		String _signSrc = "";		
		_signSrc = ValueConverter.getString(_item.get("text"));
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
		
		try {
			if(bAr!=null) _image = Image.getInstance(bAr);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
//		bAr = org.apache.commons.codec.binary.Base64.decodeBase64(URLDecoder.decode(_imageUrl).getBytes());
		
		if(_image != null)
		{
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
	
	private int mCNum = 0;
	
	private void CreatePdfCheckBox( HashMap<String, Object> _item ) throws DocumentException, IOException
	{
		
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		float _w = _x + ObjectToFloat(_item.get("width"));
		float _h = _y - ObjectToFloat(_item.get("height"));
		
		// background 
		float _bgAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));
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
		
		_contentByte = setBorderSetting(_contentByte , (ArrayList<String>) _item.get("borderSide") , (ArrayList<String>) _item.get("borderTypes") , (ArrayList<Integer>) _item.get("borderColorsInt") , (ArrayList<Integer>) _item.get("borderWidths") , _rHm);
		
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
		Color _fontColor = new Color(ValueConverter.getInteger( _item.get("fontColorInt")));
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
		
		_itemText = convertItemText( _itemText, _fontName );
		
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
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));
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
		
		_contentByte = setBorderSetting(_contentByte , (ArrayList<String>) _item.get("borderSide") , (ArrayList<String>) _item.get("borderTypes") , (ArrayList<Integer>) _item.get("borderColorsInt") , (ArrayList<Integer>) _item.get("borderWidths") , _rHm);
		
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
		Color _fontColor = new Color(ValueConverter.getInteger( _item.get("fontColorInt")));
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
		_itemText = convertItemText( _itemText, _fontName );
		
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
		_contentByte.saveState();
		
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("contentBackgroundColorInt")));
		Color _brColor = new Color(ValueConverter.getInteger(_item.get("borderColorInt")));
//		float _lineW = ValueConverter.getFloat(_item.get("strokeWidth"));
		float _lineW = ObjectToFloat(_item.get("strokeWidth"));
		_contentByte.setGState(_alpha);
		_contentByte.setColorFill(_bgColor);
		_contentByte.ellipse(_x, _y, _w, _h);
		_contentByte.fill();
		_contentByte.restoreState();
		
		if( borderAlpha > 0 )
		{
			// 선을 나중에 긋는 샘플
			_contentByte.saveState();
			_alpha.setStrokeOpacity(alpha*borderAlpha);// border		
			_contentByte.setGState(_alpha);
			_contentByte.setColorStroke(_brColor);
			_contentByte.setLineWidth(_lineW);
			_contentByte.ellipse(_x, _y, _w, _h);
			_contentByte.stroke();
			_contentByte.restoreState();
		}
		
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
 		
 		int _radius = ValueConverter.getInteger(_item.get("rx"));
 		
		Color _bgColor = new Color(ValueConverter.getInteger(_item.get("contentBackgroundColorInt")));
		Color _brColor = new Color(ValueConverter.getInteger(_item.get("borderColorInt")));
//		float _lineW = ValueConverter.getFloat(_item.get("borderThickness"));
		float _lineW = ObjectToFloat(_item.get("borderThickness"));					//@@최명진
		_contentByte.setGState(_alpha);
		_contentByte.setColorFill(_bgColor);
		_contentByte.roundRectangle(_x, _y, _w, _h, _radius);
		_contentByte.fill();
		_contentByte.restoreState();
		
		if( borderAlpha > 0 )
		{
			// 선을 나중에 긋는 샘플
			_contentByte.saveState();
			_contentByte.setLineJoin(PdfContentByte.LINE_JOIN_MITER);				// 라인의 겹침부위 모양 정의
			_contentByte.setLineCap(PdfContentByte.LINE_CAP_PROJECTING_SQUARE);		// 한쪽 모서리가 끊기는 현상 수정
			_alpha.setStrokeOpacity(alpha*borderAlpha);// border		
			_contentByte.setGState(_alpha);
			_contentByte.setColorStroke(_brColor);
			_contentByte.setLineWidth(_lineW);
			_contentByte.roundRectangle(_x, _y, _w, _h, _radius);
			_contentByte.stroke();
			_contentByte.restoreState();
		}
		
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
		float contentBgAlpha = 1;
		
		if( _item.containsKey("contentBackgroundAlpha") )
		{
			contentBgAlpha = ValueConverter.getFloat(_item.get("contentBackgroundAlpha"));
		}
		
		//bg 투명도 정보는 있으나 적용방법 확인해 봐야함
 		_alpha.setFillOpacity(alpha * contentBgAlpha); 
 		
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
		
		_contentByte.setColorStroke(_brColor);
		_contentByte.roundRectangle(_x, _y, _w, _h, _radius);
		_contentByte.fill();
		_contentByte.restoreState();
		
		if( borderAlpha > 0 )
		{
			// 선을 나중에 긋는 샘플
			_contentByte.saveState();
			_contentByte.setLineJoin(PdfContentByte.LINE_JOIN_MITER);				// 라인의 겹침부위 모양 정의
			_contentByte.setLineCap(PdfContentByte.LINE_CAP_PROJECTING_SQUARE);		// 한쪽 모서리가 끊기는 현상 수정
			_alpha.setStrokeOpacity(alpha*borderAlpha);// border		
			_contentByte.setGState(_alpha);
			_contentByte.setColorStroke(_brColor);
			_contentByte.setLineWidth(_lineW);
			_contentByte.roundRectangle(_x, _y, _w, _h, _radius);
			_contentByte.stroke();
			_contentByte.restoreState();
		}
		
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
		
		if( _w <= 0 || _h <= 0 )
		{
			return;
		}
		
		// border, background;
		PdfContentByte _contentByte = mWriter.getDirectContent();
		PdfGState _alpha = new PdfGState();
		
		String _dataStr = URLDecoder.decode( ValueConverter.getString(_item.get("data")) , "UTF-8");
		_dataStr = _dataStr.replaceAll("%20", " ");
		
		String _className = (String) _item.get("className");
		if(_className != null && _className.equals("UBQRCode"))
		{
			_dataStr = Base64Decoder.decode(_dataStr);
			System.out.print("UBQRCode:svg=[" + _dataStr + "]");
		}
		
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
		String _viewBoxString = "";
		String _preserveAspectRatioString = "";
		
		if(_viewBoxIdx > -1)
		{
			_viewBoxString = _svgTag.substring( _viewBoxIdx , _svgTag.indexOf("\"" , _viewBoxIdx + 9) + 1);
			_dataStr = _dataStr.replace(_viewBoxString, "");
		}
		
		int _preserveAspectRatioIdx = _svgTag.indexOf("preserveAspectRatio");
		
		if(_preserveAspectRatioIdx > -1)
		{
			//preserveAspectRatio="xMidYMid meet" 가운데 비율 유지  
			
			_preserveAspectRatioString = _svgTag.substring( _preserveAspectRatioIdx , _svgTag.indexOf("\"" , _preserveAspectRatioIdx + 21) + 1);
			_dataStr = _dataStr.replace(_preserveAspectRatioString, "");
		}
		else
		{
			if( _item.containsKey("PRESERVE_ASPECT_RATIO") )
			{
				_preserveAspectRatioString = "preserveAspectRatio=\"" + _item.get("PRESERVE_ASPECT_RATIO").toString() +"\"";
			}
			else
			{
				_preserveAspectRatioString = "preserveAspectRatio=\"none\"";
			}
		}
		
//		if( _item.containsKey("fixedToSize") && _item.get("fixedToSize").toString().equals("true") )
//		{
//			if(_preserveAspectRatioIdx > -1)
//			{
//				//preserveAspectRatio="xMidYMid meet" 가운데 비율 유지  
//				
//				_preserveAspectRatioString = _svgTag.substring( _preserveAspectRatioIdx , _svgTag.indexOf("\"" , _preserveAspectRatioIdx + 21) + 1);
//				_dataStr = _dataStr.replace(_preserveAspectRatioString, "");
//			}
//			else
//			{
//				_preserveAspectRatioString = "preserveAspectRatio=\"none\"";
//			}
//		}
		
		
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
		
		// svg 태그에 viewBox값이 없을경우 svg태그의 width/height를 지정
		if(_viewBoxString.equals("") == true ) _viewBoxString = " viewBox=\"0 0 " + _widthValue + " " + _heightValue + "\" ";
		
//		String _heightString = _svgTag.substring( _heightIdx , _svgTag.indexOf("\"" , _heightIdx + 8) + 1);
//		float _heightValue = ValueConverter.getFloat(_heightString.substring(_heightString.indexOf("\"") + 1, _heightString.indexOf("\"", _heightString.indexOf("\"") + 1)));
//		String _changeHeight = "height=\"" + _h + "\" viewBox=\"0 0 " + _widthValue + " " + _heightValue + "\" preserveAspectRatio=\"none\"";
		
		String _changeHeight = "height=\"" + _h + "\" "+ _viewBoxString + " " + _preserveAspectRatioString;
		// viewBox 를 이용해서 사이즈 변경 //
		
		_dataStr = _dataStr.replace(_widthString, _changeWidth)// width 변경
							.replace(_heightString, _changeHeight);// height 변경
		
//		_dataStr = StringUtil.convertSvgStyle(_dataStr);
		
		//TEST
		_dataStr = StringUtil.convertSvgStyleXPath(_dataStr);
		
		
		PdfTemplate template = _contentByte.createTemplate(_w,_h);
		Graphics2D g2 = template.createGraphics(_w,_h);          
		
		PrintTranscoder prm = new PrintTranscoder();
		TranscoderInput ti = new TranscoderInput(new StringInputStream(_dataStr , "UTF-8"));
		
		prm.addTranscodingHint(PrintTranscoder.KEY_WIDTH, _w);
		prm.addTranscodingHint(PrintTranscoder.KEY_HEIGHT, _h);
		
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
	
	
	/**
	 * 
	 * */
	private float ObjectToFloat(Object value)
	{
		return convertDpiFloat(value);
	}
	
	/**
	 * 
	 * */
	private Integer SizeToInt( Object value)
	{
		return convertDpiInt(ValueConverter.getInteger(value)) == 0 ? 1 : convertDpiInt(ValueConverter.getInteger(value));
	}
	
	/**
	 * 
	 * */
	private Integer convertDpiInt( Object value)
	{
		return Math.round((ValueConverter.getInteger(value) / 96 ) * 72 );
	}
	
	/**
	 * 
	 * */
	private float convertDpiFloat( Object value)
	{
		return (Math.round(((ValueConverter.getFloat(value) / 96f ) * 72f ) * 10f))/10f;
	}
	
	private Integer borderSideSetting( String side )
	{
		if( side.equals("top"))
		{
			return Rectangle.BOTTOM;
//			return Rectangle.TOP;
		}
		else if( side.equals("left"))
		{
			return Rectangle.LEFT;
		}
		else if(side.equals("bottom"))
		{
			return Rectangle.TOP;
//			return Rectangle.BOTTOM;
		}
		else if( side.equals("right"))
		{
			return Rectangle.RIGHT;
		}
		
		return 0;
	}
	
	private Integer fontStyleToInt( Object _italic , Object _bold , Object _underLine)
	{
		int _style = 0;
		
		if( !"none".equals(_underLine))
		{
			_style += Font.UNDERLINE;
		}
		
		if( !"normal".equals(_italic))
		{
			_style += Font.ITALIC;
		}
		
		if( !"normal".equals(_bold))
		{
			_style += Font.BOLD;
		}
		
		return _style;
	}
	
	private class Watermark extends PdfPageEventHelper {
		
		float _fSize = 65;
		float _pageW = 0f;
		//Font _font;
		
		@Override
        public void onEndPage(PdfWriter writer, Document document) {
        	
			setWaterMark( writer, document );
        }
		
		public void setPageWaterMark( PdfWriter writer, Document document )
		{
			//setWaterMark( writer, document );
		}
		
        private void setWaterMark( PdfWriter writer, Document document )
        {
        	String stLicenseType = Log.clientLicenseType;
        	//if(ViewerInfo5.getLicenseType() != null && ViewerInfo5.getLicenseType().length() > 0)
       		if(stLicenseType != null && stLicenseType.length() > 0)
        	{
        		setLicenseWaterMark(writer, document );
        	}
        	
        	if(mWaterMarkImgMap != null && mWaterMarkImgMap.isEmpty() == false )
        	{
        		try {
					setImageWaterMark( writer, document );
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	
        	if(mWaterMarkTxt != null && mWaterMarkTxt.length() > 0)
        	{
        		try {
        			String _checkWaterMarkValue = URLDecoder.decode(mWaterMarkTxt, "UTF-8");
        			String _orgWaterMarkValue = URLEncoder.encode(_checkWaterMarkValue, "UTF-8").replace("+","%20");
        			log.debug(getClass().getName()+"::"+"_orgWaterMarkValue=[" + _orgWaterMarkValue + "], mWaterMarkTxt=[" + mWaterMarkTxt + "]");
        			
        			if(mWaterMarkTxt.equals(_orgWaterMarkValue))
        			{
        				log.debug(getClass().getName()+"::"+"mWaterMarkTxt is URLEncoded!!!~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        				mWaterMarkTxt = _checkWaterMarkValue;
        			}
        			else
        				log.debug(getClass().getName()+"::"+"mWaterMarkTxt is not URLEncoded!!!~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		setTextWaterMark(writer, document );
        	}
        }

		// Image 워터마트 처리 
        private void setImageWaterMark(PdfWriter writer, Document document) throws DocumentException
        {
        	PdfContentByte _contentByte = mWriter.getDirectContent();
        	_contentByte.saveState();
        	
        	PdfGState _gState = new PdfGState();
        	float _alpha = (float) 0.5;
//        	float _alpha =  (float) mWaterMarkImgMap.get("ALPHA");
        	//PdfContentByte _contentByte = mWriter.getDirectContent();
        	_gState.setFillOpacity(_alpha);
        	_gState.setStrokeOpacity(_alpha);
			_contentByte.setGState(_gState);
    		
			Image _img =(Image)  mWaterMarkImgMap.get("IMAGE");
			float _w = Float.valueOf( mWaterMarkImgMap.get("WIDTH").toString() );
			float _h = Float.valueOf( mWaterMarkImgMap.get("HEIGHT").toString() );
			
//			float _x = (mPageWidth - _w) /2;
//			float _y = (mPageHeight - _h) /2;
			float _x = Float.valueOf( mWaterMarkImgMap.get("X").toString() );
			float _y = Float.valueOf( mWaterMarkImgMap.get("Y").toString() );
			_y = mPageHeight - (_y + _h);
			
       		if(_img != null)
       		{
//       			_img.scalePercent(10, 10);
    			_contentByte.addImage(_img, _w, 0, 0, _h, _x, _y);
       		}
       		
       		_contentByte.restoreState();
       		
        }

        private void setTextWaterMark(PdfWriter writer, Document document)
        {
        	PdfContentByte _contentByte = mWriter.getDirectContent();
        	_contentByte.saveState();
        	
        	_fSize = getFontSize();
        	
        	Font _font;
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
    		Phrase watermark = new Phrase(mWaterMarkTxt, _font);
        	PdfGState _gState = new PdfGState();
        	float _alpha = (float) 0.5;
        	//PdfContentByte _contentByte = mWriter.getDirectContent();
        	_gState.setFillOpacity(_alpha);
        	_gState.setStrokeOpacity(_alpha);
			_contentByte.setGState(_gState);
			ColumnText _a = new ColumnText(_contentByte);
            ColumnText.showTextAligned(_contentByte, Element.ALIGN_CENTER, watermark, mPageWidth / 2, mPageHeight / 2, 45);
            
            _contentByte.restoreState();
        }
        
        
        private void setLicenseWaterMark(PdfWriter writer, Document document)
        {
        	String stLicenseType = Log.clientLicenseType != null ? Log.clientLicenseType : "";
        	
        	//if( "FREE".equals(ViewerInfo5.getLicenseType().toUpperCase()) ) return;
        	if( "FREE".equals(stLicenseType.toUpperCase()) ) return;
        	
        	PdfContentByte _contentByte = mWriter.getDirectContent();
        	_contentByte.saveState();
        	
        	_fSize = 40;
        	
        	Font _font;
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
    		Phrase watermark = new Phrase(stLicenseType.toUpperCase(), _font);
        	
        	PdfGState _gState = new PdfGState();
        	float _alpha = (float) 0.5;
        	//PdfContentByte _contentByte = mWriter.getDirectContent();
        	_gState.setFillOpacity(_alpha);
        	_gState.setStrokeOpacity(_alpha);
			_contentByte.setGState(_gState);
            ColumnText.showTextAligned(_contentByte, Element.ALIGN_LEFT, watermark, 0, mPageHeight - 40, 0);
            
            _contentByte.restoreState();
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
	
	public void colorRectangle(PdfContentByte canvas,
	        Color color, float x, float y, float width, float height) 
	{
		PdfGState _alpha = new PdfGState();
		_alpha.setFillOpacity(0);
		canvas.saveState();
        canvas.setGState(_alpha);
        canvas.setLineWidth(1f);
        canvas.setColorStroke(color);
        canvas.rectangle(x, y, width, height);
        canvas.fillStroke();
        canvas.restoreState();
    }
	
	 /**
     * Draws a colored rectangle.
     * @param canvas the canvas to draw on
     * @param color the Color
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
	
	private void TestPDF ()
	{
		  PdfContentByte canvas = mWriter.getDirectContent();
		  PdfShading axial = PdfShading.simpleAxial(mWriter, 400, 330, 420,
	                270, Color.ORANGE, Color.BLUE);
	        PdfShadingPattern shading = new PdfShadingPattern(axial);
	        canvas.setShadingFill(shading);
	        canvas.roundRectangle(370, 270, 80, 60, 0);
	        canvas.fillStroke();
	}
	
	
	/**
     * text align to pdfAlign int
     * @param Text-Align
     */
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
 
	
	/***
	 * 
	 * @return
	 * @throws DocumentException
	 * @throws IOException

	public byte[] pdfFontTest() throws DocumentException, IOException
	{
		
		log.info(getClass().getName() + "::" + "Call xmlParsingpdf()...");
		
		mBaos = new ByteArrayOutputStream();	
		mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		
		Document document = new Document();
		
		String _url = Log.ufilePath + "UFile\\sys\\fonts\\";
		
//		int i = FontFactory.registerDirectory("C:\\Windows\\Fonts\\");
		int i = FontFactory.registerDirectory(_url);
		System.out.println("Result = " + i);
										
		Set<String> set= FontFactory.getRegisteredFonts();
		Iterator<String> it = set.iterator();
		
		ArrayList<String> _list = new ArrayList<String>(set);
		
		document.setPageSize(PageSize.A5);
		PdfWriter writer = PdfWriter.getInstance(document, mBaos);
		Font font;
		String fontname="";
		String font_arr[] = new String[]{
//			BaseFont.CP1250, BaseFont.CP1252, BaseFont.CP1257, 
			BaseFont.IDENTITY_H 
//			BaseFont.IDENTITY_V , 
//			BaseFont.WINANSI
			};
		document.open();	
		Paragraph _textPara = null;
		Chapter chapter = null;
		int _cnt = 0;
//		while(it.hasNext()){
//			fontname = it.next();				
//			chapter = new Chapter(new Paragraph("Hello Pdf!!!!." + fontname), 1);
//			if( _cnt < 0 ) continue;
			String _txtStr = "";
			fontname = "맑은 고딕";
			_txtStr = "FontName==>>" +fontname + " ===|||==>> Hello Pdf!!! 한글 입니다!!??@#$11 ===||==>액·인";
			
			Font _font01 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H , 10 , Font.BOLD , Color.red);	
			Chunk _txtCh01 = new Chunk(_txtStr , _font01);

			fontname = "돋움";
			_txtStr = "FontName==>>" +fontname + " ===|||==>> Hello Pdf!!! 한글 입니다!!??@#$11 ===||==>액·인";
			Font _font02 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H , 12 , Font.UNDEFINED , Color.blue);	
			Chunk _txtCh02 = new Chunk(_txtStr , _font02);
			
			fontname = "굴림";
			_txtStr = "FontName==>>" +fontname + " ===|||==>> Hello Pdf!!! 한글 입니다!!??@#$11 ===||==>액·인";
			Font _font03 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H , 8 , Font.ITALIC , Color.pink);	
			Chunk _txtCh03 = new Chunk(_txtStr , _font03);
			
			fontname = "궁서";
			_txtStr = "FontName==>>" +fontname + " ===|||==>> Hello Pdf!!! 한글 입니다!!??@#$11 ===||==>액·인";
			Font _font04 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H , 10 , Font.UNDEFINED , Color.gray);
			Chunk _txtCh04 = new Chunk(_txtStr , _font04);
			_txtCh04.setUnderline(Color.gray, 0.8f, 0f, 0.5f, -0.2f ,PdfContentByte.LINE_CAP_BUTT);
			
			_textPara = new Paragraph();
			_textPara.setLeading( 20f );
			_textPara.add(_txtCh01);
			_textPara.add(_txtCh02);
			_textPara.add(_txtCh03);
			_textPara.add(_txtCh04);
			
			document.add( _textPara );
			
			Paragraph _textPara2 = new Paragraph();
			_textPara2.setLeading( 14f );
			_textPara2.setIndentationLeft(30f);
			_textPara2.add(_txtCh02);
			_textPara2.add(_txtCh04);
			_textPara2.add(_txtCh03);
			_textPara2.add(_txtCh01);
			
			document.add( _textPara2 );
			
			
//			for(i=0; i < font_arr.length; i++) {
//				try {
//					System.out.println(fontname + "::" + font_arr[i]);
//					font = FontFactory.getFont(fontname, font_arr[i] , 10 , Font.BOLD , Color.red);	
////					font.setSize(10);
//					chapter.add(new Paragraph("FontName==>>" +fontname + " ===|||=== Style==>>" + font_arr[i] + " ===|||==>> Hello Pdf!!! 한글 입니다!!??@#$11 ===||==>혈액 인계·인수서", font));
//				}catch (Exception e) {
////					e.printStackTrace();
//				}
//			}
//			document.add(chapter);
//			if( _cnt > 10 ) break;				
			document.newPage();
//			_cnt++;
//			if( _cnt > 100 ) break;
//		}
//		writer.add(chapter);
		document.close();
		
		return mBaos.toByteArray();
	}
	*/
	
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
		
		if( _rtnFontName.equals("") == false && GlobalVariableData.M_REGISTER_FONT_LISE.indexOf(_rtnFontName) == -1 && GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.containsKey(_rtnFontName) )
		{
			_rtnFontName = GlobalVariableData.M_REGISTER_FONT_LOCAL_MAP.get(_rtnFontName);
		}
		
		return _rtnFontName;
	}
	
	
	
	
	private void CreatePdfConnectLine( HashMap<String, Object> _item )
	{
		float _x = ObjectToFloat(_item.get("x"));
		float _y = mPageHeight - ObjectToFloat(_item.get("y"));
		
		float _x1 = ObjectToFloat(_item.get("cx1"));
		float _y1 = ObjectToFloat(_item.get("cy1"));
		
		float _x2 = ObjectToFloat(_item.get("cx2"));
		float _y2 = ObjectToFloat(_item.get("cy2"));

		float _x3 = ObjectToFloat(_item.get("cx3"));
		float _y3 = ObjectToFloat(_item.get("cy3"));
		
		float _x4 = ObjectToFloat(_item.get("cx4"));
		float _y4 = ObjectToFloat(_item.get("cy4"));
		
		float _x5 = ObjectToFloat(_item.get("cx5"));
		float _y5 = ObjectToFloat(_item.get("cy5"));
		
		float _x6 = ObjectToFloat(_item.get("cx6"));
		float _y6 = ObjectToFloat(_item.get("cy6"));
		
		
		_x1 = _x + _x1;
		_y1 = _y - _y1;
		
		_x2 = _x + _x2;
		_y2 = _y - _y2;
		
		_x3 = _x + _x3;
		_y3 = _y - _y3;
		
		_x4 = _x + _x4;
		_y4 = _y - _y4;
		
		_x5 = _x + _x5;
		_y5 = _y - _y5;
		
		_x6 = _x + _x6;
		_y6 = _y - _y6;
		
		
		PdfContentByte _contentByte = mWriter.getDirectContent();
		
		Color _lineColor = new Color(ValueConverter.getInteger(_item.get("lineColorInt")));
		
		
		
		
		
		
		_contentByte.saveState();
//		_contentByte.setLineWidth( ValueConverter.getInteger(_item.get("thickness")));
		_contentByte.setLineWidth( ObjectToFloat(_item.get("thickness")));
		_contentByte.setColorStroke(_lineColor);
		_contentByte.moveTo( _x1, _y1 );
		_contentByte.lineTo( _x2, _y2 );
		_contentByte.lineTo( _x3, _y3 );
		_contentByte.lineTo( _x4, _y4 );
		_contentByte.lineTo( _x5, _y5 );
		_contentByte.lineTo( _x6, _y6 );
		_contentByte.stroke();
		_contentByte.restoreState();
		
		
		// test rectangle
//		_contentByte.saveState();
//		_contentByte.roundRectangle(_x1-5, _y1-5, 10, 10, 0);
//		_contentByte.fillStroke();
//		_contentByte.restoreState();
		
		_contentByte.saveState();
		//_contentByte.setColorFill(_bgColor);
		//_contentByte.ellipse(0, 0, 1, 1);
		_contentByte.circle(_x6, _y6, 10);
		_contentByte.fillStroke();
		_contentByte.restoreState();
		
		
		
//		canvas.moveTo(x,y);        
//		canvas.lineTo(x + side, y);
//		canvas.lineTo(x + (side / 2), (float)(y + (side * Math.sin(Math.PI / 3))));
//		canvas.closePathFillStroke();
//		
		
		_contentByte.saveState();
		_contentByte.moveTo( _x1+5, _y1 );
		_contentByte.lineTo( _x1-5, _y1+5 );
		_contentByte.lineTo( _x1-5, _y1-5 );
		_contentByte.closePathFillStroke();
		_contentByte.restoreState();
	}
	
	
	public void toPdfOnePage( HashMap _hashData, String _pageIndex ) throws DocumentException, MalformedURLException, IOException
	{
		// 인쇄시 Scale값이 1이 아닐경우 처리
		float _pageWidth = ValueConverter.getFloat(mProjectHm.get("pageWidth"));
		float _pageHeight = ValueConverter.getFloat(mProjectHm.get("pageHeight"));
		float _pWidth = _pageWidth;
		float _pHeight = _pageHeight;
		_pWidth = convertDpiFloat(_pWidth);
		_pHeight = convertDpiFloat(_pHeight);
		
		if(mScale < 1)
		{
			mMarginX = (_pageWidth-(_pageWidth*mScale))/2;
			mMarginY = (_pageHeight-(_pageHeight*mScale))/2;
		}
		
		mPageWidth = _pWidth;
		mPageHeight = _pHeight;

		
		HashMap<String, String> _pageHash = null;
		
		if( _hashData.containsKey("page") )
		{
			_pageHash = (HashMap<String, String>) _hashData.get("page");
			if( _pageHash.containsKey("width") && _pageHash.get("width").equals("") == false ) _pWidth 		= convertDpiFloat(ValueConverter.getFloat(_pageHash.get("width")));
			if( _pageHash.containsKey("height") && _pageHash.get("height").equals("") == false ) _pHeight 	= convertDpiFloat(ValueConverter.getFloat(_pageHash.get("height")));
		}
		
		Rectangle _newPSize = new Rectangle(_pWidth,_pHeight);
		if( mPageBackgroundColor != -1 ) _newPSize.setBackgroundColor(  new Color(mPageBackgroundColor) );
		else _newPSize.setBackgroundColor(Color.WHITE);
		
		mDocument.setPageSize(_newPSize);
		mDocument.setMargins(0, 0, 0, 0);
		mDocument.newPage();
		
		ArrayList<HashMap<String, Object>> _itemList = (ArrayList<HashMap<String,Object>>) _hashData.get("pageData");
		ArrayList<HashMap<String, Object>> _itemList2 = new ArrayList<HashMap<String,Object>>();
		
		if( _itemList.size() <= 0)
		{
			PdfContentByte _contentByte = mWriter.getDirectContent();
            ColumnText.showTextAligned(_contentByte, -1 , new Phrase(""), 0, 0, 0);
         // New Page
			mDocument.newPage();
			return;
		}
		
		
		//Background 처리 필요 
		if(_pageHash != null)
		{
			if( _pageHash.containsKey("backgroundImage"))
			{
				JSONObject _bgObj = (JSONObject) JSONValue.parse(_pageHash.get("backgroundImage"));
//				setBackgroundImage(_bgObj.get("url").toString());
				CreatePdfImageBG(_bgObj);
			}
		}
		
		// 페이지 변환시 changeList에 값이 있을경우 처리 
		if(mChangeItemList != null && mChangeItemList.size() > 0)
		{
			testPdfChangeMapping(mChangeItemList, _hashData, _pageIndex);
		}
		
		_itemList2.add(_itemList.get(0));
		
		for (int j = 0; j < _itemList.size(); j++) 
		{
			HashMap<String, Object> _item = _itemList.get(j);
			
			String _className = "";
			
			if( mScale < 1 )
			{
				_item = convertScaleItem(_item, mMarginX, mMarginY );
			}
			
			if( _item.containsKey("className"))
			{
				_className = (String) _item.get("className");
				
				
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
				else if( _className.equals("UBTable") )
				{
					createPdfTable(_item);
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
				else if( _className.equals("UBTextSignature") )
				{
					CreatePdfSign( _item );
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
				else if( _className.indexOf("Code") != -1 || _className.indexOf("Chart") != -1 || _className.equals("UBTaximeter") )
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
		
		// 라이센스
		// New Page
		
		if( mTBborder.size() > 0) // 페이지 넘기기전 보더를 안그린게 있으면 그린다.
		{
			setTBborderSetting();
			mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		}
		
		
		if(!mUseFileSplit)
		{		
		
	//		Rectangle _newPSize = new Rectangle(_pWidth, _pHeight);
			int _nCurPageIndex = mPDF_PAGE_IDX*mExportDivCount + mWriter.getPageNumber();
	
			log.debug("toPdfOnePage() :: mTotalPageCount:curPageIndex=====>" + mTotalPageCount + ":" + _nCurPageIndex);
			this.mUdpClient.sendMessage(JSONConverter("SUCCESS", "MAKEPDF", "99999", "Make Pdf page : " + _nCurPageIndex + "/" + mTotalPageCount));
			
			if( mWriter.getPageNumber() >= mExportDivCount )
			{
				if(mTotalPageCount > _nCurPageIndex)
				{
					mDocument.close();
					
					// 동일한 파일명으로 치완
					try {
						String _filePath = getPdfFileName();
		
						mBaos.close();
						mBaos = null;
						mWriter.close();
						
						mDocument = new Document(_newPSize);
						//mDocument.plainRandomAccess = true;
						mDocument.compress = true;
		
						mBaos = new FileOutputStream(_filePath);	
						mWriter = PdfWriter.getInstance(mDocument, mBaos);
						
						mDocument.open();
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			{
				if(mTotalPageCount > _nCurPageIndex)
				{
					mDocument.setPageSize(_newPSize);
					mDocument.setMargins(0, 0, 0, 0);
					
					mDocument.newPage();
				}
			}
		
		}
		else
		{
			int _nCurPageIndex = mWriter.getPageNumber();
			
			log.debug("toPdfOnePage() :: mTotalPageCount:curPageIndex=====>" + mTotalPageCount + ":" + _nCurPageIndex);
			this.mUdpClient.sendMessage(JSONConverter("SUCCESS", "MAKEPDF", "99999", "Make Pdf page : " + _nCurPageIndex + "/" + mTotalPageCount));
		}
		
		_hashData.clear();
		_hashData = null;
		
		try {
			if(Thread.currentThread().interrupted() == false) Thread.currentThread().sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private String JSONConverter(String type, String command, String code, String message) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("port", Log.wsPort);
		jsonObject.put("type", type);
		jsonObject.put("command", command);
		jsonObject.put("code", code);
		jsonObject.put("message", message);
		return jsonObject.toString();
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
		String _oClassType = "";

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
				_oClassType = (String) _oItem.get("type");		
				
//				System.out.println("ubFormToPDF::4187::======================================================>_classType=" + _oClassType);
				
//				if( _oClassName == null )
				if( _oClassName == null || ( _oItem.get("dataType") != null &&(  _oItem.get("dataType").equals("1") || _oItem.get("dataType").equals("2") ) ) )
				{
					if( _oClassType != null && _oClassType.equals("specialFontLabel") && _oID.equals(_cID) )
					{
						String _cData = (String) _cItem.get("Value");
						_oItem.put("text", _cData);
						break;
					}					
					else
						continue;
				}
				
				if( _oClassName.equals("UBSignature") || _oClassName.equals("UBPicture") || _oClassName.equals("UBCheckBox") || _oClassName.equals("UBTextSignature") )		
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
						else if( _oClassName.equals("UBTextSignature") )
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
							if( _oItem.containsKey("selectedText") && _oItem.get("selectedText").equals(_cData)  )
							{
								_cData = "true";
							}
							
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
	
	public ArrayList<String> getPdfFilePaths()
	{
		return this.mSaveFilePathAr;
	}	
	
	public void xmlParsingpdfStart(JSONObject _hm ) throws DocumentException, IOException
	{
		xmlParsingpdfStart(_hm, mUsePrint, mCirculation, mPrintScale);
	}
	
	private Watermark mPdfWaterMarkInfo;
	
	public void xmlParsingpdfStart(JSONObject _hm, Boolean usePrint, int _circulation, float _printScale ) throws DocumentException, IOException
	{
//		mFontListAr = new ArrayList<String>(FontFactory.getRegisteredFonts());
		if( !mUseFileSplit )
		{
			mUseThread = false;
		}
		else
		{
			String _useThread = common.getPropertyValue("ubform.pageGroupUseThread");
			if( _useThread != null && _useThread.equals("true") )
			{
				mUseThread = true;
				String _threadCount = common.getPropertyValue("ubform.pageGroupThreadCount");
				
				if( _threadCount != null &&  StringUtil.isInteger(_threadCount) && Integer.valueOf(_threadCount) > 0 && mThreadMaxCount > Integer.valueOf(_threadCount) )
				{
					mThreadCount = Integer.valueOf(_threadCount);
				}
			}
		}
		
		String _filePath = this.mUseFileSplit ? this.mResultFileName : savePDFFile(mExportPath);
		if(this.mUseFileSplit)
		{
			String _currentTime = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
			saveFileName = pdfFileName + "_" + _currentTime;
		}
		
		this.pdfFilePath = _filePath;
		mSaveFilePathAr.add(this.pdfFilePath);	
		
		try
		{
			if( mUseThread ) mBaos = new ByteArrayOutputStream();
			else mBaos = new FileOutputStream(_filePath);	
		}
		catch(FileNotFoundException exp)
		{
			exp.printStackTrace();
			log.error(exp.getMessage());
		}
		
			
		
		mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		// 파라미터의 사이즈값이 존재할경우 맞춰서 width와 height값을 늘리고 mMarginX와 mMarginY값을 세팅
		
		// pdf Page , Document, BaseFont, font 셋팅
		pdfPagePropertySetting( _hm, usePrint, _circulation );
		mFontListAr = new ArrayList<String>(FontFactory.getRegisteredFonts());
		
		if(usePrint)
		{
			if( _printScale > 1 ) _printScale = 1;
			
			mScale = _printScale;
		}
		
		
		String stLicenseType = Log.clientLicenseType;
		//워터 마크
//		if(!mWaterMarkTxt.equals(""))
//		if(!mWaterMarkTxt.equals("") ||  mWaterMarkImgMap != null || ViewerInfo5.getLicenseType() != null)
		if(!mWaterMarkTxt.equals("") ||  mWaterMarkImgMap != null || stLicenseType != null)
		{
			mPdfWaterMarkInfo = new Watermark();
			mWriter.setPageEvent(mPdfWaterMarkInfo);
		}
		
		// Bridge.setVerbose(true);
		//Bridge.init(new File(Log.basePath + "lib\\jni4net.n.w32.v40-0.8.8.0.dll"));
		String _WindowsBit = common.getPropertyValue("Form.WindowsBit");		   	
		if(_WindowsBit != null && "64".equals(_WindowsBit))
		{
			Bridge.init(new File(Log.basePath + "lib\\jni4net.n.w64.v40-0.8.8.0.dll"));
		}
		else
		{
			Bridge.init(new File(Log.basePath + "lib\\jni4net.n.w32.v40-0.8.8.0.dll"));
		}
		File coreFile = new File(Log.basePath + "lib\\jni4net.n-0.8.8.0.dll");
        File dllFile = new File(Log.basePath + "lib\\PdfMerge.j4n.dll");
        //File jsonLibFile = new File(Log.basePath + "lib\\System.Net.Json.dll");
	      
        Bridge.LoadAndRegisterAssemblyFrom(coreFile);
        Bridge.LoadAndRegisterAssemblyFrom(dllFile);        
        //Bridge.LoadAndRegisterAssemblyFrom(jsonLibFile);        
        
		mDocument.open();
	}
	
	
	
	private String saveFileName = "";
	private String PATH = "";
	
	public String getPdfByteData()
	{
		Date stTime = new Date();
		log.debug(" START : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(stTime) +"]" );
		
		if(mWriter != null ) log.debug("=========== SAVE PDF PAGE_SIZE : " + mWriter.getPageNumber() );
		
		if( mUseThread )
		{
			try {
				threadFileSave();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mDocument.close();
				
				shutdownThreadPool();
			}
		}
		else
		{
			if(mDocument != null)
			{
				try {
					mDocument.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mDocument = null;
			}
		}
		
		
		if(mBaos != null)
		{
			try {
				
				if( !mUseThread )
				{
					mBaos.close();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
			if( !mUseThread )
			{
				mBaos = null;
			}
			
		}
		
		Date end = new Date();
		long diff = end.getTime() - stTime.getTime();
		long diffSec = diff / 1000% 60;         
		long diffMin = diff / (60 * 1000)% 60;
		
		log.debug(" START : [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(end) +"]" );
		log.info("================= ONE FILE END >>>>>>> [ " + diffMin +":"+diffSec+"."+diff%1000+" ]");
		
		return pdfFilePath;
	}
	
	// C# 모듈에 생성된 Pdf file들을 머지요청하고, 머지된 파일경로을 리턴받는다.
	public String makeMergePdfFiles(boolean usePrint) throws Exception
	{
		String _exportPath = EXPORT_DIR;
		ArrayList<String> _fileList = mSaveFilePathAr;
		String _resultFilePath = "";
		
		try
		{
			if(mUseFileSplit)
			{			
				// mSaveFilePathAr file들을 zip file로 묶어서 리턴한다.
				if(_fileList.size() == 1)
				{
					_resultFilePath = _fileList.get(0);
				}
				else
				{
					String fileName = saveFileName;             
		            int pageNumber = 1;
		            
		            String _zipFileName = mSplitFilePath + File.separator + fileName + ".zip";
		    		FileOutputStream fout = new FileOutputStream(_zipFileName);
		    		ZipOutputStream zout = new ZipOutputStream(fout);
		    		
		    		for (int i=0; i< _fileList.size(); i++) {
		    			
		    			File outputfile = new File(_fileList.get(i));
		    			FileInputStream ins = new FileInputStream(outputfile);
		                ZipEntry ze = new ZipEntry( outputfile.getName() );
		                zout.putNextEntry(ze);
		    		    zout.write(IOUtils.toByteArray(ins));
		    		    zout.closeEntry();
		    		    ins.close();
		               								    		    
		                pageNumber++;
		    			
		    		}
		    		
		    		zout.close();
		    		fout.close();
		    		
		    		log.debug("makeMergePdfFiles() : PDF zipFileName=[" + _zipFileName + "]");		    		
		    		
		    		for (int i=0; i< _fileList.size(); i++) {
		    			
		    			File outputfile = new File(_fileList.get(i));
		    			outputfile.delete();
		    			System.out.println("Delete file -> "+ outputfile.getAbsolutePath());
		    		}
		    		
		    		_resultFilePath = _zipFileName;
				}
			}
			else
			{
				/*
				// Bridge.setVerbose(true);
		        Bridge.init(new File(Log.basePath + "lib\\jni4net.n.w32.v40-0.8.8.0.dll"));
		        File coreFile = new File(Log.basePath + "lib\\jni4net.n-0.8.8.0.dll");
		        File dllFile = new File(Log.basePath + "lib\\PdfMerge.j4n.dll");
			      
		        Bridge.LoadAndRegisterAssemblyFrom(coreFile);
		        Bridge.LoadAndRegisterAssemblyFrom(dllFile);        
		        */
				
		        PdfMerge pdfmergeObj = new PdfMerge();
				
		        if(usePrint)
		        	_resultFilePath = pdfmergeObj.MergePdfFiles(_exportPath, saveFileName, _fileList.size(), mPdfActionStr);
		        else
		        	_resultFilePath = pdfmergeObj.MergePdfFiles(_exportPath, saveFileName, _fileList.size(), null);
				log.debug("PdfMerge::MergePdfFiles() returnValue=" + _resultFilePath);
				
				String resultCode = _resultFilePath.substring(0, 2);
				if(resultCode.equals("S:"))
				{
					_resultFilePath = _exportPath + _resultFilePath.substring(2) + ".pdf";
				}
				else	// Fail
				{
					_resultFilePath = _resultFilePath.substring(2);
					throw new Exception("MegrePdfFilesException:" + _resultFilePath);
				}
			}
		}
		catch(Exception exp)
		{
			exp.printStackTrace();
			throw exp;
		}
		
		return _resultFilePath;
	}

	public ImageDictionary getmImageDictionary() {
		return mImageDictionary;
	}

	public void setmImageDictionary(ImageDictionary mImageDictionary) {
		this.mImageDictionary = mImageDictionary;
	}
	
	public Document getDocument()
	{
		return mDocument;
	}
	
	public void setDocument( Document _doc) 
	{
		mDocument = _doc;
	}
	
	public boolean makeMaxPage( int _pageCnt )
	{
		for (int i = 0; i < _pageCnt; i++) {
			mDocument.newPage();
		}
		
		return true;
	}
	private boolean mUsePdfStamp = false;
	
	public void UsePdfStamp( boolean _flag)
	{
		mUsePdfStamp = _flag;
	}
	
	private HashMap<String, Object> createWaterMarkImage( HashMap<String, Object> _waterMap )
	{
		HashMap<String, Object> _resultWaterMarkMap = null;
		
		if( _waterMap.containsKey("type") && _waterMap.get("type").equals("image") )
		{
			_resultWaterMarkMap = new HashMap<String, Object>();
			float _itemAlpha = ValueConverter.getFloat(_waterMap.get("alpha"));
			
			String _imageUrl = "";		
			
			if(_waterMap.containsKey("data") == false) return null;
			
			_imageUrl = _waterMap.get("data").toString();
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
			
			Image _image=null;
			if( _hasDictionary ){
				_image = _imgDictionary.getmPDFImage();
			}else{
				_image = common.getLocalImageFile(_imageUrl, mSESSION_ID);	
				_newImgDictionary =mImageDictionary.createPDFDictionaryData(_imageUrl, _image);
			}
			
	   		if(_image != null)
	   		{
	   			float _w = ObjectToFloat(_waterMap.get("width"));
	   			float _h = ObjectToFloat(_waterMap.get("height"));
	   			
	   			if( _w <= 0 ) _w = ObjectToFloat( _image.getWidth() );
	   			if( _h <= 0 ) _h = ObjectToFloat( _image.getHeight() );
	   			
//	   			float _x = (mPageWidth -  _w ) / 2;
//	   			float _y = (mPageHeight - _h) / 2;
	   			float _x = ObjectToFloat(_waterMap.get("x"));
	   			float _y = ObjectToFloat(_waterMap.get("y"));
	   			
				_resultWaterMarkMap.put("X", _x);
				_resultWaterMarkMap.put("Y", _y);
				_resultWaterMarkMap.put("WIDTH", _w);
				_resultWaterMarkMap.put("HEIGHT", _h);
				_resultWaterMarkMap.put("ALPHA", _itemAlpha);
				_resultWaterMarkMap.put("IMAGE", _image);
	   		}
	   		else
	   		{
	   			_resultWaterMarkMap = null;
	   		}
		}
		else if( _waterMap.containsKey("type") && _waterMap.get("type").equals("text")  )
		{
			mWaterMarkTxt = _waterMap.get("text").toString();
		}

		
		return _resultWaterMarkMap;
	}
	
	private String savePDFFile( String _exportPath )
	{
		String fname = "";
		String _currentTime = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
		
		if(_exportPath != null && _exportPath.length() > 0)
		{
			EXPORT_DIR = _exportPath;
		}
		else
		{
			String _userHome = System.getProperty("user.home");
			EXPORT_DIR = _userHome + "/Downloads/";
		}
		
		// Write pdf File.
		fname = pdfFileName + "_" + _currentTime;
		
		//디렉토리 생성
		File _directory = new File( EXPORT_DIR );
		if(!_directory.exists()){
			_directory.mkdirs(); 
		}
		
		PATH = EXPORT_DIR + "" + fname + ".pdf";
		
		saveFileName = fname;
				
		return PATH;
	}
	
	private String getPdfFileName()
	{
		mPDF_PAGE_IDX = mPDF_PAGE_IDX + 1;
		String _saveFile = saveFileName + "_"+ mPDF_PAGE_IDX +".pdf";
		mSaveFilePathAr.add(EXPORT_DIR + _saveFile);
				
		return EXPORT_DIR + _saveFile;
	}
	
	   public void setBackgroundImage(String _url ) throws IOException, DocumentException {
	        PdfContentByte canvas = mWriter.getDirectContentUnder();
	        
	        Image image = Image.getInstance(_url);
	        image.scaleAbsolute( mPageWidth, mPageHeight );
	        image.setAbsolutePosition(0, 0);
	        canvas.addImage(image);
	    }      
		
		
		private void CreatePdfImageBG( JSONObject _bgObj ) throws MalformedURLException, IOException, DocumentException
		{
			float _x = 0;
			float _y = mPageHeight;
			float _w = -1;
			float _h = -1;
			float _itemAlpha = 1;
			
			PdfContentByte _contentByte = mWriter.getDirectContent();
			
			PdfGState _alpha = new PdfGState();
			_contentByte.saveState();
			
			String _imageUrl = "";		
			String _type = "";
			if( _bgObj.containsKey("type"))
			{
				_type = _bgObj.get("type").toString();
				
				if( _type.equals("base64"))
				{
					_imageUrl= _bgObj.get("data").toString();
					_imageUrl = URLEncoder.encode( _imageUrl, "UTF-8");
				}
				else if( _type.equals("url") )
				{
					_imageUrl= _bgObj.get("url").toString();
				}
			}
			
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
			
			Image _image=null;
			if( _hasDictionary ){
				_image = _imgDictionary.getmPDFImage();
			}else{
				_image = common.getLocalImageFile(_imageUrl, mSESSION_ID);	
				
				_newImgDictionary =mImageDictionary.createPDFDictionaryData(_imageUrl, _image);
			}
			
	   		if(_image != null)
	   		{
	   			
	   			float _imgW = _image.getWidth();
	   			float _imgH = _image.getHeight();
	   			
	   			float _scaleW = mPageWidth/_imgW;
	   			float _scaleH = mPageHeight/_imgH;
	   			float _scale = 1;
	   			
	            if( _scaleH > _scaleW )
	            {
	            	// width scale에 맞춰서 이미지의 사이즈변경
	            	_scale = _scaleW;
	            }else
	            {
	            	_scale = _scaleH;
	            }
	            
	            _imgW = _image.getWidth() * _scale;
	            _imgH = _image.getHeight() * _scale;
	            
	            float _top =  (mPageHeight - _imgH )/2;
	            float _left = (mPageWidth - _imgW )/2;
	            
				_image.scalePercent(10, 10);
				_alpha.setFillOpacity(_itemAlpha);	
				_contentByte.setGState(_alpha);
				_contentByte.addImage(_image, _imgW, 0, 0, _imgH, _left, _top);
				_contentByte.restoreState();
				
				
	   		}
	   		else
	   		{
	   			_contentByte.restoreState();
	   		}
			
		}
		
		/**
		 * functionName : convertItemText
		 * desc			:	아이템의 text속성을 폰트에 맞춰서 바꾸기위한 함수
		 * @param _text
		 * @param _fontName
		 * @return
		 */
		public String convertItemText( String _text, String _fontName)
		{
/*
			// 크메르 언어 변환처리 
			if(mUseKhmerParser && mUnicodeRender != null)
			{
				if( mKhmerFontNames.indexOf(_fontName) != -1 )
				{
					return mUnicodeRender.render(_text);
				}
			}
*/			
			return _text;
		}    
		
		
		protected ExecutorService pool = null;
		
		public void shutdownThreadPool()
		{
			if( pool != null )
			{
				pool.shutdown();
				
				try { 
				    // Wait a while for existing tasks to terminate 
				    if (!pool.awaitTermination(60, TimeUnit.SECONDS)) { 
				        pool.shutdownNow(); // Cancel currently executing tasks 
				        // Wait a while for tasks to respond to being cancelled 
				        if (!pool.awaitTermination(60, TimeUnit.SECONDS)) 
				           log.debug("Pool did not terminate");
				     }
				} catch (InterruptedException ie) { 
				    // (Re-)Cancel if current thread also interrupted 
				    pool.shutdownNow(); 
				    // Preserve interrupt status 
				    Thread.currentThread().interrupt(); 
				}
				pool = null;
			}
			
		}
		
		public void threadFileSave() throws InterruptedException
		{
			log.debug("===================================== PAGE GROUP USE THREAD =====================================");
			//mBaos
			if( pool == null )
			{
				pool = Executors.newFixedThreadPool(mThreadCount, DaemonThreadFactory.instance);
			}
			// thread runnable
			Runnable _fileSaveRunnable = new Runnable() {
				
				private String _fileName = "";
				private ByteArrayOutputStream _byte = null;
				private Document _document;
				private ArrayList<String> _splitFilenames = null;
				
				public Runnable init(String _fileNM, ByteArrayOutputStream _byteArr, Document _doc,  ArrayList<String> _splitnames ) {
			        this._fileName=_fileNM;
			        this._byte=_byteArr;
			        this._document = _doc;
			        this._splitFilenames = _splitnames;
			        return(this);
			    }
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
	            	
					FileOutputStream _file=null;
	                try {
	                	_document.close();
	                	
	                	_file = new FileOutputStream( this._fileName );
	                	_file.write( this._byte.toByteArray() );
	                } catch (Exception e) {
	                	if( this._splitFilenames.indexOf(this._fileName) != -1 )
						{
//							this._splitFilenames.remove( _splitFilenames.indexOf(this._fileName) );
							this._splitFilenames.set( _splitFilenames.indexOf(this._fileName) , null );
						}
	                	
	                }
	                finally
	                {
	                	if( _file != null )
	                	{
							try {
								_file.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                	}
	                	
	                	if( this._byte != null)
	                	{
	                		try {
								this._byte.close();
								this._byte = null;
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                		
	                	}
	                	
	                	_document = null;
	                }
						
	                
				}
			}.init(mResultFileName, (ByteArrayOutputStream) mBaos, mDocument, mSplitFileNames );
			
			pool.execute( _fileSaveRunnable );
			
			Thread.sleep(10);
		}

		
		
		private void createPdfTable(  HashMap<String, Object> _item ) throws DocumentException, UnsupportedEncodingException
		{
//			Table table = new Table(3);
//			table.setBorderWidth(1);
//			table.setBorderColor(new Color(0, 0, 255));
//			table.setPadding(5);
//			table.setSpacing(5);
//			
//			Cell cell = new Cell("header");
//			cell.setHeader(true);
//			cell.setColspan(3);
//			table.addCell(cell);
//			table.endHeaders();
//			
//			
//			
//			cell = new Cell("example cell with colspan 1 and rowspan 2");
//			cell.setRowspan(2);
//			cell.setBorderColor(new Color(255, 0, 0));
//			table.addCell(cell);
//			table.addCell("1.1");
//			table.addCell("2.1");
//			table.addCell("1.2");
//			table.addCell("2.2");
//			table.addCell("cell test1");
//			cell = new Cell("big cell");
//			cell.setRowspan(2);
//			cell.setColspan(2);
//			table.addCell(cell);
//			table.addCell("cell test2");
			
			
			float _x = ObjectToFloat(_item.get("x"));
			float _y = ObjectToFloat(_item.get("y"));
			
			// _item의 속성에 들어있는 컬럼갯수정보.
			int colCnt = 0;
			// rowspan 빈 셀이 필요한지 여부를 저장하는 배열.
			ArrayList<HashMap<String, Object>> rowspanEndIdxAr = new ArrayList<HashMap<String, Object>>();
			if( _item.containsKey("columnCount") ){
				colCnt = Integer.valueOf(_item.get("columnCount").toString());
			}
			
			Table table = new Table(colCnt);
			table.setBorderWidth(1);
			table.setBorderColor(new Color(0, 0, 255));
			table.setPadding(0);
			table.setSpacing(0);
			
			// 아이템에서 rows 에 담긴 셀 아이템을 가져와서 그린다.
			ArrayList<ArrayList<HashMap<String, Object>>> rowAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("rows");
			ArrayList<Float> _rowHeightAr = (ArrayList<Float>) _item.get("heightInfo");		// Row의 Height List
			int rowSize = rowAr.size();
			
			HashMap<String, Object> rowspanInfo = null;
			Cell cell;
			HashMap<String, Object> _cellItem;
			int _colSpan = 1;
			int _rowSpan = 1;
			
			
			for (int rIdx = 0; rIdx < rowSize; rIdx++) {

				//Row의 Height값을 맵핑
				if( _rowHeightAr.size() > rIdx ){
					
				}
				
				ArrayList<HashMap<String, Object>> colAr = rowAr.get(rIdx);
				
				int _currentColPosition = 0;	// 현재 colAr의 인덱스
				int _colCnt = colAr.size();
				boolean _chk = false;
				
				for (int colIdx = 0; colIdx < _colCnt; colIdx++) {
					
					_cellItem = colAr.get(colIdx);
					
					if( !_chk )
					{
						
						int _txStyle = fontStyleToInt(_cellItem.get("fontStyle"), "normal", "none");
						
						float _fontSize = ObjectToFloat(_cellItem.get("fontSize"))*0.9f;
						
						float _lineHeight = Float.parseFloat(_cellItem.get("lineHeight").toString());
						
						if(mPDF_PRINT_VERSION.equals( GlobalVariableData.UB_PDF_PRINT_VERSION_1 ) )
						{
							if( Float.valueOf(_cellItem.get("fontSize").toString()) <= 12 )_fontSize =  Double.valueOf( Math.ceil( _fontSize ) ).floatValue();
						}
						else
						{
							// PontSize를 PT사이즈로 지정
							_fontSize = Double.valueOf(Float.valueOf(_cellItem.get("fontSize").toString()) * 72 /  96 ).floatValue();
						}
						
						Object _fontColorInt = _cellItem.get("fontColorInt"); 
						
						if( _fontColorInt == null || _fontColorInt == "" ){
							_fontColorInt = 0;
						}
						Color _txColor = new Color(ValueConverter.getInteger(_fontColorInt));
						String _fontName = ValueConverter.getString(_cellItem.get("fontFamily"));
						String _fName = getDefFont(_fontName);
						
						if( _fName.equals("") )
						{
							mDefFont = FontFactory.getFont(_fontName, BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
							
							if(mDefFont.getBaseFont() == null)
							{
								if( mFontListAr.indexOf("나눔 고딕") != -1 )
								{
									mDefFont = FontFactory.getFont("나눔 고딕", BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
								}
								else
								{
									mDefFont = new Font(mBaseFont, _fontSize, _txStyle , _txColor);
								}
							}
						}
						else
						{
							mDefFont = FontFactory.getFont(_fName, BaseFont.IDENTITY_H , _fontSize, _txStyle, _txColor);
						}
						
						_chk = true;
					}
					
					_colSpan = Integer.valueOf(_cellItem.get("colSpan").toString());
					_rowSpan = Integer.valueOf(_cellItem.get("rowSpan").toString());
					
//					cell = new Cell();
//					cell.add(CreatePdfParagraph(_cellItem));
					cell = new Cell(_cellItem.get("text").toString());
					cell.setRowspan(_rowSpan);
					cell.setColspan(_colSpan);
					cell.setBorderColor(new Color(255, 0, 0));
					cell.setWidth(ObjectToFloat(_cellItem.get("width")));
					
					
					table.addCell(cell);
				
				}
			}
			table.setLeft(_x);
			table.setTop(_y);
			
			mDocument.add(table);
			
		}
		
		
		@SuppressWarnings("unchecked")
		private Paragraph CreatePdfParagraph( HashMap<String, Object> _item ) throws DocumentException, UnsupportedEncodingException
		{
		    float _x = ObjectToFloat( _item.get("x") );
		    float _y = mPageHeight - ObjectToFloat( _item.get("y") );
		    float _w = ObjectToFloat( ValueConverter.getFloat(_item.get("x")) + ValueConverter.getFloat(_item.get("width")) );
		    float _h = mPageHeight - ObjectToFloat( ValueConverter.getFloat(_item.get("y")) + ValueConverter.getFloat(_item.get("height")) );
		      
		    String _id = ValueConverter.getString(_item.get("id"));
		    String _type = ValueConverter.getString(_item.get("type"));
		    float  _fontSize = Double.valueOf(Float.valueOf(_item.get("fontSize").toString()) * 72 /  96 ).floatValue();
		    int _txStyle = fontStyleToInt(_item.get("fontStyle"), "normal", "none");
		    float _lineHeight = Float.parseFloat(_item.get("lineHeight").toString());
		    Object _fontColorInt = _item.get("fontColorInt"); 
			
			if( _fontColorInt == null || _fontColorInt == "" ){
				_fontColorInt = 0;
			}
			Color _txColor = new Color(ValueConverter.getInteger(_fontColorInt));
			
		    PdfContentByte _contentByte = mWriter.getDirectContent();
		    PdfGState _gState = new PdfGState();
		    Rectangle _bRect;
		    boolean _verticalRotateFlg = false;
		      
		      // background 
		    float _bgAlpha = ValueConverter.getFloat(_item.get("backgroundAlpha"));
		    Color _bgColor = new Color(ValueConverter.getInteger(_item.get("backgroundColorInt")));
		    float _alpha =  (_item.get("alpha") != null)?ValueConverter.getFloat(_item.get("alpha")):1;

		    // border
		    HashMap<String, Float> _rect = new HashMap<String, Float>();
		    ArrayList<HashMap<String, Object>> _content;
		      
		    HashMap<String, Float> _rotateDef = new HashMap<String, Float>();
		      
		    // text
		    int _align = getAlignmentInt( ValueConverter.getString(_item.get("textAlign")) );
		    String _verticalAlign = ValueConverter.getString(_item.get("verticalAlign"));
		    // font Color  처리
		    
		    String _itemText = ValueConverter.getString(_item.get("text"));
		      
		      // 줄바꿈 처리
		    _itemText = _itemText.replace("\\n", "\n").replaceAll("\\r", "\r");
		    
			//일부 Link기능 추가		
			Font mLinkFont = new Font(mBaseFont, _fontSize, _txStyle , Color.blue);
			ArrayList<Chunk> _arrChunk  = new ArrayList <Chunk> ();
			boolean _splitLinkText = false; 
			boolean _useLinkText = false;
			
			String _hyperLinkText = "";
			
			String _hyperLinkUrl = "";		
			if( _item.get("ubHyperLinkUrl") != null  && _item.get("ubHyperLinkUrl").equals("null") == false  && _item.get("ubHyperLinkUrl").equals("") == false ){
				_hyperLinkUrl = _item.get("ubHyperLinkUrl").toString();

				if( !_hyperLinkUrl.endsWith("null") && _hyperLinkUrl.length() > 0 ){
					_hyperLinkUrl = URLDecoder.decode(_hyperLinkUrl, "UTF-8");
				}
				
				if( _item.get("ubHyperLinkText") != null && _item.get("ubHyperLinkText").equals("") == false ){
					_hyperLinkText = _item.get("ubHyperLinkText").toString();
				}
				
				if( !_hyperLinkText.endsWith("null") && _hyperLinkText.length() > 0 ){
					_hyperLinkText = URLDecoder.decode(_hyperLinkText, "UTF-8");
					//_hyperLinkText 값은 유일해야함
					String [] arrTemp = _itemText.split(_hyperLinkText);
					if(arrTemp.length>1){
						_arrChunk.add(new Chunk(arrTemp[0], mDefFont));
						_arrChunk.add(new Chunk(_hyperLinkText, mDefFont));
						_arrChunk.add(new Chunk(arrTemp[1], mDefFont));
						_splitLinkText = true;
					}else{
						_arrChunk.add(new Chunk(_itemText, mDefFont));
					}
				}else{
					_arrChunk.add(new Chunk(_itemText, mDefFont));
				}
				
				_useLinkText = true;
			}
			else
			{
				_arrChunk.add(new Chunk(_itemText, mDefFont));
			}    
		      
		    Paragraph _txtp = new Paragraph();
		    for(Chunk _chunk:_arrChunk ){
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
		         
		       if( !ValueConverter.getString(_item.get("textDecoration")).equals("none") )
		       {
		          float _thickness = (_fontSize/2) / 10;
		          if( _thickness < 0.8f ) _thickness = 0.8f;
		          _chunk.setUnderline(_txColor, _thickness, 0f, 0.5f, -0.2f ,PdfContentByte.LINE_CAP_BUTT);
		       }
		         
		       if(_useLinkText)
		       {
		          if(!_splitLinkText){
		             _chunk.setAnchor(_hyperLinkUrl);   
		          }else{
		               
		             if(_chunk.getContent().equals(_hyperLinkText)){
		                _chunk.setAnchor(_hyperLinkUrl);
		                _chunk.setFont( mLinkFont);               
		                float _thickness = (_fontSize/2) / 10;
		                if( _thickness < 0.8f ) _thickness = 0.8f;
		                _chunk.setUnderline(Color.blue, _thickness, 0f, 0.5f, -0.2f ,PdfContentByte.LINE_CAP_BUTT);
		             }
		          }         
		       }
		         
		       _txtp.add(_chunk);
		    }   
		    
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
		    	 float _readingSize = _fontSize * _lineHeight;
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
		        optionMap.put("lineHeight", new float[]{ObjectToFloat(_item.get("lineHeight"))}); 
		        //optionMap.put("lineHeight", new float[]{1.2f});//TODO LineHeightTEST
		         
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
		          _rotateNum = ValueConverter.getFloat(_item.get("textRotate"));
		            
		          _rotateDef.put("x", _x);
		          _rotateDef.put("y", _y);
		          _rotateDef.put("w", _w);
		          _rotateDef.put("h", _h);
		            
		       }
		    }
		    else
		    {
		         
		    }
	      
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
	    
	      // padding 값이 크게 들어가서 기본패딩 들어있던 부분 생략. 2016-09-26 공혜지
	      	float _padding = ObjectToFloat(0.5);
//	      	float _padding = ObjectToFloat(0.0f);
			if( _item.containsKey("padding") && ObjectToFloat(_item.get("padding")) != 0 )
			{
				_padding = _padding + ObjectToFloat(_item.get("padding"));
			}
			
			float _verticalPadding = ObjectToFloat(_item.get("padding"));
		    
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
	      
	       float _H = 0;
	       
	       
	       // VerticalAlign이 TOP이나 BOTTOM일때 사용할 padding값을 만들기
	   		_H = (_cY - _cH) - (_txtp.getLeading()*_lineCount);
		    if( _lineCount > 1 ) _H = _H - Double.valueOf(_txtp.getLeading()*0.2).floatValue();
		    
			if( _H < _verticalPadding )
			{
				_verticalPadding = (_H > 0)?_H:0;
			}
			
	      
			return _txtp;
	   }
		
		
		public void toPdfPageOpen( HashMap<String, String> _pageHash )
		{
			
			// 인쇄시 Scale값이 1이 아닐경우 처리
			float _pageWidth = ValueConverter.getFloat(mProjectHm.get("pageWidth"));
			float _pageHeight = ValueConverter.getFloat(mProjectHm.get("pageHeight"));
			float _pWidth = _pageWidth;
			float _pHeight = _pageHeight;
			_pWidth = convertDpiFloat(_pWidth);
			_pHeight = convertDpiFloat(_pHeight);
			
			if(mScale < 1)
			{
				mMarginX = (_pageWidth-(_pageWidth*mScale))/2;
				mMarginY = (_pageHeight-(_pageHeight*mScale))/2;
			}
			
			if( _pageHash != null )
			{
				if( _pageHash.containsKey("width") && _pageHash.get("width").equals("") == false ) _pWidth 		= convertDpiFloat(ValueConverter.getFloat(_pageHash.get("width")));
				if( _pageHash.containsKey("height") && _pageHash.get("height").equals("") == false ) _pHeight 	= convertDpiFloat(ValueConverter.getFloat(_pageHash.get("height")));
			}
			
			Rectangle _newPSize = new Rectangle(_pWidth,_pHeight);
			if( mPageBackgroundColor != -1 ) _newPSize.setBackgroundColor(  new Color(mPageBackgroundColor) );
			else _newPSize.setBackgroundColor(Color.WHITE);
			
			mDocument.setPageSize(_newPSize);
			mDocument.setMargins(0, 0, 0, 0);
			mDocument.newPage();
			
			// 페이지 생성시에 WaterMark를 표시처리
			mPdfWaterMarkInfo.setPageWaterMark(mWriter, mDocument);
			
			mPageWidth = _pWidth;
			mPageHeight = _pHeight;

		}
		public void toPdfPageClose()
		{
			if( mTBborder.size() > 0) // 페이지 넘기기전 보더를 안그린게 있으면 그린다.
			{
				setTBborderSetting();
				mTBborder = new HashMap<String, ArrayList<HashMap<String,Object>>>();
			}
		}
		
		public void toPdfOneItem( HashMap<String, Object> _item, String _pageIndex ) throws DocumentException, MalformedURLException, IOException, JDOMException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException
		{
			
			// 페이지 변환시 changeList에 값이 있을경우 처리 
			if(mChangeItemList != null && mChangeItemList.size() > 0)
			{
				//testPdfChangeMapping(mChangeItemList, _hashData, _pageIndex);
			}
			
			String _className = "";
			String _classType = "";
			
			if( mScale < 1 )
			{
				_item = convertScaleItem(_item, mMarginX, mMarginY );
			}
			
			if( _item.containsKey("className"))
			{
				_className = (String) _item.get("className");
				_classType = (String) _item.get("type");
				
				
				if( _className.equals("UBLabelBand") ||  _className.equals("UBLabel") || _className.equals("UBLabelBorder") 
					||_className.equals("UBStretchLabel") || _className.equals("UBRotateLabel")  || _className.equals("UBTextInput")
					|| _className.equals("UBTextArea"))
				{  
					
					String _id = ValueConverter.getString(_item.get("id")); 
					
					if(_item.containsKey("ORIGINAL_TABLE_ID") && _item.get("ORIGINAL_TABLE_ID").equals("") == false )
					{
						
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
					if("specialFontLabel".equals(_classType))
					{
						CreatePdfSfsImage(_item);
					}
					else
						CreatePdfLabel(_item);
				}
				else if( _className.equals("UBTable") )
				{
					createPdfTable(_item);
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
				else if( _className.equals("UBTextSignature") )
				{
					CreatePdfSign( _item );
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
				else if( _className.indexOf("Code") != -1 || _className.indexOf("Chart") != -1 || _className.equals("UBTaximeter") )
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
			}
			
			
		}		
		
		
		public int[] rgbToCmyk(float red, float green, float blue)
	    {
	        int black = Float.valueOf(  Math.min(Math.min(255 - red, 255 - green), 255 - blue) ).intValue();
	        
	        if (black!=255) {
	            int cyan    = Float.valueOf( ((255-red-black)/(255-black)) * 255 ).intValue();
	            int magenta = Float.valueOf( ((255-green-black)/(255-black)) * 255 ).intValue();
	            int yellow  = Float.valueOf( ((255-blue-black)/(255-black)) * 255 ).intValue();
	            return new int[] {cyan,magenta,yellow,black};
	        } else {
	            int cyan = Float.valueOf(255 - red).intValue();;
	            int magenta = Float.valueOf(255 - green).intValue();
	            int yellow = Float.valueOf(255 - blue).intValue();
	            return new int[] {cyan,magenta,yellow,black};
	        }
	    }	 	
		
}
