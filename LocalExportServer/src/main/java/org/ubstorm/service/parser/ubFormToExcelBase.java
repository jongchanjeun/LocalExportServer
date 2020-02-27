package org.ubstorm.service.parser;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.print.DocFlavor.STRING;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdom2.JDOMException;
import org.json.simple.JSONObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.ubstorm.service.dictionary.ImageDictionary;
import org.ubstorm.service.dictionary.ImageDictionaryVO;
import org.ubstorm.service.parser.xmlToUbForm.EBorderType;
import org.ubstorm.service.parser.formparser.data.BandInfoMapData;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.PageInfo;
import org.ubstorm.service.parser.formparser.info.ProjectInfo;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.zxing.aztec.decoder.Decoder;
import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;

public abstract class ubFormToExcelBase {

	// class
	protected Logger log = Logger.getLogger(getClass());

	/**
	 * 파싱된 아이템 정보를 저장할 Array List. 
	 **/
	protected ArrayList<ArrayList<HashMap<String, Object>>> itemPropList = null;
	
	// excel property
	protected Workbook wb;
	protected Sheet sheet;
	Runtime runtime = Runtime.getRuntime();
	protected boolean isPartial = false;
	protected int currentPageNo = 0;
	protected int argoPageNo = 0;
	protected int totalXSize = 0;
	protected int START_PAGE = 0;
	
	// repeatValue체크를 위하여 담아두는 객체
	protected boolean useRepeatValue = false;
	protected HashMap<String, ArrayList<CellRangeAddress>> mRepeatValueCheckMap;
	protected HashMap<String, String> mLastRepeatValueStringMap;
	protected ArrayList<CellRangeAddress> mCellRangeAddressList;
	private HashMap<Integer, JSONObject> mBackgroundInfoMap = new HashMap<Integer, JSONObject>();
	
	
	/**
	 * 페이지별 Y좌표에 더해줄 값. 페이지의 yAr Size 누적. 
	 * */
	protected int rowIdx = 0;
	protected int rowStartIDX = 0;
	protected Row row;
	protected String isExcelOption = "";
//	public int maxSheetSize = 2000;	
	public int maxSheetSize = -1;				// Excel 출력시 Sheet하나에 몃Row까지 표현할지 지정 -1 무제한
	
	protected float mRatio = 1;					// Excel 출력시 화면을 비율로 줄여서 출력 여부 ( 인쇄시 A4에 출력을 위해 비율을 입력받오록 처리 )
	protected String mSheetName = "";
	protected boolean mAutoFontSize = false;	// fontSize 자동조절
	protected boolean mAutoHeightSize=false;	// Height자동 조절
	
	protected boolean mEnabledFit = false;
	protected boolean mFitWidth 	= false;
	protected boolean mFitHeight = false;
	
	protected String mExcelSheetSplitType = "";
	protected ArrayList<String> mExcelSheetNames = null;
	/**
	 * 이미지 아이템들만 모아 저장할 배열. 
	 * */
	private ArrayList<HashMap<String, Object>> mImgItemList = new ArrayList<HashMap<String,Object>>();
	
	private HashMap<Element, ArrayList<Integer>> pageInfoXAr = new HashMap<Element, ArrayList<Integer>>();
	/**
	 * 전체 아이템의 x 좌표가 저장된 배열. 
	 * */
	protected ArrayList<Integer> xArrayGlobal = new ArrayList<Integer>();
	protected ArrayList<ArrayList<Integer>> documentXArrayGlobal = new ArrayList<ArrayList<Integer>>();
	
	protected boolean mExcelIncludeImage = true; 

	protected boolean mExcelUsePageHeight = true; 		//페이지의 height를 export시에 사용
	/**
	 * <pre>
	 *  x, y 좌표를 연결한 값이 저장된 배열.
	 *  이차원 배열 구조
	 *  
	 *  배열원소
	 *  [ [{'x':0, 'y':0, 'colSpan':0, 'rowSpan':0, 'type':'item/none/D'}, {HashMap}, ... , {HashMap} ], [], ... , [] ]
	 *  </pre>
	 * */
	protected ArrayList< ArrayList< HashMap<String, Object>> > xySetArray;
	
	
	//-------------------------------  Define  abstract methods   -------------------------------//
	public abstract Workbook toExcelOnePage(int pageNo/*, int itemArLen */) throws Exception;	
	public abstract Workbook xmlParsingExcel(HashMap<String, HashMap<String, ArrayList<Object>>> datasetMap) throws UnsupportedEncodingException;
	public abstract Workbook xmlParsingExcel(ArrayList<ArrayList<HashMap<String, Object>>> dataMap, Workbook _wb , int _pageNo, String _formName, boolean _isNewDoc, int _documentIdx ) throws Exception;
	public abstract Workbook toExcel(HashMap<String, HashMap<String, ArrayList<Object>>> datasetMap) throws UnsupportedEncodingException;
	
	protected abstract void addImage( HashMap<String, Object> _curMap, CellRangeAddress region, ArrayList<Integer> _xAr, ArrayList<Integer> _yAr ) throws URISyntaxException;
	protected abstract void addImageBase64( HashMap<String, Object> _curMap, CellRangeAddress region ) throws URISyntaxException, UnsupportedEncodingException;
	protected abstract void addLine( HashMap<String, Object> _item, CellRangeAddress region );
	protected abstract void addShape( HashMap<String, Object> _item, CellRangeAddress region, String type );
	protected abstract String CreateClipImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException, TranscoderException;
	protected abstract String CreateSVGAreaImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException, TranscoderException;
	
	protected abstract boolean clearStyleTable();
	
	protected List<String> mNotExportItems;
	
	protected ImageDictionary mImageDictionary;
	
	JSONObject mBackgroundImage;
	
	private String mSplitFileName = "";
	private String mSplitFilePath = "";
	
	public void setBackgroundImage(JSONObject _bgImage)
	{
		mBackgroundImage = _bgImage;
	}
	
	public ImageDictionary getmImageDictionary() {
		return mImageDictionary;
	}

	public void setmImageDictionary(ImageDictionary mImageDictionary) {
		this.mImageDictionary = mImageDictionary;
	}
	
	public void setExcelIncludeImage(boolean _isIncludeImage)
	{
		mExcelIncludeImage = _isIncludeImage;
	}

	public void setExcelUsePageHeight(boolean _excelUsePageHeight)
	{
		mExcelUsePageHeight = _excelUsePageHeight;
	}
	
	
	
	public ubFormToExcelBase()
	{
		String _scaleEnabled = common.getPropertyValue("excelExport.enabledScale");
		String _scale = common.getPropertyValue("excelExport.scale");
		String _enabledAutoScale = common.getPropertyValue("excelExport.autoHeightSize");
		String _enabledAutoFontSize = common.getPropertyValue("excelExport.autoFontSize");
		
		String _enabledFit = common.getPropertyValue("excelExport.enabledFit");
		String _fitWidth = common.getPropertyValue("excelExport.fitWidth");
		String _fitHeight = common.getPropertyValue("excelExport.fitHeight");
		
		
		if( _scaleEnabled != null && _scaleEnabled.equals("true") && _scale != null && _scale.equals("") == false )
		{
			mRatio = Float.valueOf(_scale);
		}
		
		if( _enabledAutoScale != null && _enabledAutoScale.equals("true") )
		{
			mAutoHeightSize = true;
		}
		
		if( _enabledAutoFontSize != null && _enabledAutoFontSize.equals("true") )
		{
			mAutoFontSize = true;
		}
		
		if(_enabledFit != null && _enabledFit.equals("true") )
		{
			mEnabledFit = true;
			if(_fitWidth != null && _fitWidth.equals("true") )
			{
				mFitWidth = true;
			}

			if(_fitHeight != null && _fitHeight.equals("true") )
			{
				mFitHeight = true;
			}
		}
		
		mNotExportItems = Arrays.asList(GlobalVariableData.M_NOT_EXPORT_ITEMS);
	}
	
	/**
	 * xmlToUbForm 에서 만든 X배열을 전역변수에 세팅한다. 
	 * */
	public void setXArray( ArrayList<Integer> xAr ) {
		xArrayGlobal = xAr;
		totalXSize = xAr.size();
	}
	
	/**
	 * xmlToUbForm 에서 만든 X배열을 전역변수에 세팅한다. 
	 * */
	public void setDcoXArray( ArrayList<ArrayList<Integer>> _docXAr ) {
		documentXArrayGlobal = _docXAr;
//		totalXSize = xAr.size();
	}
	public ArrayList<ArrayList<Integer>> getDocXArray()
	{
		return documentXArrayGlobal;
	}
	
	public void setSTART_PAGE(int sTART_PAGE) {
		START_PAGE = sTART_PAGE;
	}
	
	public void setIsExcelOption( String _option)
	{
		if( _option != null ) isExcelOption = _option;
	}
	public String getIsExcelOption()
	{
		if( isExcelOption == null ) isExcelOption = "";
		return isExcelOption;
	}
	
	public void setMaxSheetSize(int _value)
	{
		maxSheetSize = _value;
	}

	public int getMaxSheetSize()
	{
		return maxSheetSize;
	}
	
	public void setUseRepeatValue( boolean _value )
	{
		useRepeatValue = _value;
	}
	public boolean getUseRepeatValue()
	{
		return useRepeatValue;
	}
	
	private String mExcelFileName  = "";
	
	public void setExcelFileName( String _value )
	{
		mExcelFileName = _value;
	}

	public String getExcelFileName()
	{
		return mExcelFileName;
	}
	
	
	public String getSplitFilePath()
	{
		return mSplitFilePath;
	}
	
	public void setSplitFilePath( String _value )
	{
		if( _value.indexOf("xlsx") != -1)
		{
			_value = _value.replace(".xlsx", "/");
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
	}
	
	public void chageSplitFilePath()
	{
		File theDir = new File(mSplitFilePath);
		if (theDir.isDirectory()) {
			theDir.delete();
		}
		
		mSplitFilePath = mSplitFilePath.substring(0, mSplitFilePath.lastIndexOf("/", mSplitFilePath.length()-2) + 1 );
	}
	
	ArrayList<String> mSplitFileNames = null;
	public void setSplitFileName( String _value )
	{
		if(mSplitFileNames==null) mSplitFileNames = new ArrayList<String>();
		mSplitFileName = _value + ".xlsx";
		mExcelFileName = mSplitFilePath + mSplitFileName;
		
		mSplitFileNames.add(mExcelFileName);
	}
	
	// Excel 의 sheet구분 방법 지정
	public void setExcelSheetSplitType( String _value )
	{
		mExcelSheetSplitType = _value;
	}

	public void setExcelSheetNames( String _value )
	{
		if( _value != null && _value.equals("") == false )
		{
			try {
				_value = URLDecoder.decode(_value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				
			}
			String[] _array = _value.split(",");
			mExcelSheetNames = new ArrayList<String>( Arrays.asList(_array) );
		}
	}
	
	/*****************************************************
	*	만들어진 좌표 정보를 가지고 엑셀 Workbook 생성.
	* 	@return Workbook
	 * @throws Exception 
	*****************************************************/
	public Workbook toExcel() throws Exception {
		
		//log.info(getClass().getName() + "::" + "Start Parsing Excel...");
		Date begin = new Date();
		Date end = new Date();
		int itemArLen = itemPropList.size();
		
		// 2015-12-02 페이지 별로 쪼개진 방식. 무조건 한페이지만 계산.
		if( isPartial ){
			toExcelOnePage(currentPageNo/*, itemArLen, xySet*/);
			end = new Date();
			long diff = end.getTime() - begin.getTime();
			long diffSec = diff / 1000% 60;         
			long diffMin = diff / (60 * 1000)% 60;        
			
//			log.info(getClass().getName()+"::toExcel::create Excel Complete!>>> "
//					+currentPageNo+" page [ " + diffMin +":"+diffSec+"." + diff%1000 +" ]\t"
//					+wb.getNumCellStyles()+" cellstyle  //  " + styleStringTables.size()+" stringTb    ///   " + styleXSSFTables.size() +" xssf ");
		}
		// 2015-11-16 각 page 마다 Y배열 계산 후 아이템 생성.
		else{
			for (int pageNo = currentPageNo; pageNo < currentPageNo+itemArLen; pageNo++) {
//			for (int pageNo = 0; pageNo < itemArLen; pageNo++) {
//			if( pageNo == 76 ) return wb;
				toExcelOnePage(pageNo/*, itemArLen, xySet*/);
			}
			end = new Date();
			long diff = end.getTime() - begin.getTime();
			long diffSec = diff / 1000% 60;         
			long diffMin = diff / (60 * 1000)% 60;        
			
			log.info(getClass().getName()+"::toExcel::create Excel Complete!>>> "
					+currentPageNo+" ~ "+currentPageNo+itemArLen+" page [ " + diffMin +":"+diffSec+"." + diff%1000 +" ]");
		}
		
		if( mEnabledFit )
		{
			if( sheet.getPrintSetup() instanceof XSSFPrintSetup    )
			{
				XSSFPrintSetup _xsPrintSetup = (XSSFPrintSetup) sheet.getPrintSetup();
				sheet.setFitToPage(true);
				if(mFitWidth) _xsPrintSetup.setFitWidth((short) 1);
				else  _xsPrintSetup.setFitWidth((short) 0);
				
				if(mFitHeight) _xsPrintSetup.setFitHeight((short) 1);
				else _xsPrintSetup.setFitHeight((short) 0);
				
				_xsPrintSetup.setPaperSize(XSSFPrintSetup.A4_PAPERSIZE);
			}
			else if( sheet.getPrintSetup() instanceof HSSFPrintSetup )
			{
				HSSFPrintSetup _xsPrintSetup = (HSSFPrintSetup) sheet.getPrintSetup();
				sheet.setFitToPage(true);
				if(mFitWidth) _xsPrintSetup.setFitWidth((short) 1);
				else  _xsPrintSetup.setFitWidth((short) 0);
				
				if(mFitHeight) _xsPrintSetup.setFitHeight((short) 1);
				else _xsPrintSetup.setFitHeight((short) 0);
				
				_xsPrintSetup.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
			}
			
			convertRowHeight();

			// 용지 방향 지정 
			printPageSetting();
			
			// 출력시 한페이지의 영역을 지정하기 위해 처리 페이지가 바뀌어야 할 영역을 지정한다
			if(sheet.getLastRowNum() > 0 )
			{
				//sheet.setColumnBreak( xArrayGlobal.size() + 1 );
				sheet.setRowBreak( sheet.getLastRowNum() );				// 대용량 저장시 엑셀 setRowBreak 지정시 엑셀 오픈시 에러 발생함 
			}
			
//			sheet.setColumnBreak(sheet.getRow( sheet.getFirstRowNum()).getLastCellNum() );
		}

		return wb;
	}
	
	
	/*
	 * 2015-11-16 추가.
	 * cell의 갯수가 많아지면 xySetArray의 size가 커져서, Heap Space 에러가 나타남.
	 * XArray만 먼저 구해서 저장해두고, y Array는 페이지별로 구한다.
	 * 
	 * 1. makeXArray() : 전체 아이템의 x 좌표를 구해 전역변수 xArrayGlobal에 저장한다.
	 * 2. makeYArray(int pageNo) : pageNo 값에 따라 y Array 를 계산한다. ( 인덱스가 0부터. 아이템 생성시 row 수 더해줘야함.
	 * 3. makePositionArray2( yArray ) : 현재 페이지의 yArray 값을 가지고 xySetArray 세팅.
	 * 4. 이후 작업은 이전과 동일 mappingItemPosition - cellPropertySetting - getborderCheck
	 * */
	/**
	 * <pre>
	 * XML 정보에서 x 좌표를 뽑아 xArrayGlobal 에 저장한다.
	 * For Excel Export : 페이지 별 분할전송으로 변경되면서 X좌표 계산 부분이 중복되어 이 방식으로 변경함.
	 * 2015-12-02
	 * </pre>
	 * */
	private ArrayList<Integer> makeXArray(ArrayList<Element> pages, ArrayList<Float> _pageMarginX, ArrayList<Integer> mXArr, HashMap<String, ArrayList<HashMap<String, Object>>> _currentDataSet ) {

		log.info(getClass().getName() + "::" + "Call xmlToUbForm makeXArray()...");
		
		ArrayList<Integer> xAr = new ArrayList<Integer>();
		ArrayList<Float> xArFloat = new ArrayList<Float>();
		ArrayList<Integer> cloneXAr = new ArrayList<Integer>();
		
		if(mXArr.size() > 0)
		{
			xAr = mXArr;
			if(xAr.contains(0) == false )xAr.add(0);
		}
		else
		{
			xAr.add(0);
			xArFloat.add(0f);
		}
		
		int _pageCnt = pages.size();
		float _pageXPosition = 0f;
		int _cloneCountArgo = 1;
		boolean _isConnect = false;
		float _pageW = 0;
		
		for (int i2 = 0; i2 < _pageCnt; i2++) {
			
			Element _page = pages.get(i2);
			
			_pageW = Float.valueOf(_page.getAttribute("width").toString());
			NodeList _items = _page.getElementsByTagName("item");
			int itemLen = _items.getLength();
			
			if( _page.getAttribute("isConnect") != null &&  _page.getAttribute("isConnect").equals("true"))
			{
				_isConnect = true;
			}
			_pageXPosition = _pageMarginX.get(i2);
			
			ArrayList<Integer> _xAr = new ArrayList<Integer>();

			ArrayList<Float> _xArOri = new ArrayList<Float>();
			
			// label band 에 필요한 속성을 담을 맵.
			HashMap<String, Integer> labelBandProp = null;
			HashMap<String, HashMap<String, String>> bandData = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> bandInfo;
			for (int i = 0; i < itemLen; i++) {
				Element _item = ((Element) _items.item(i));
				String className = _item.getAttribute("className");
				
				if(className.indexOf("Band") != -1)
				{
					if( "UBLabelBand".equals(className) )
					{
						NodeList _props = _item.getElementsByTagName("property");
						int propLen = _props.getLength();
						labelBandProp = new HashMap<String, Integer>();
						// property
						for (int j = 0; j < propLen; j++) {
							NamedNodeMap itemPropNode = _props.item(j).getAttributes();
							
							if( itemPropNode.getNamedItem("name") == null ) continue;
							
							String propName = itemPropNode.getNamedItem("name").getNodeValue();
							String propVal = itemPropNode.getNamedItem("value").getNodeValue();
							if( propName.equals("columns") && !propVal.equals("null") && !propVal.equals("") ){
								labelBandProp.put("columns", Integer.parseInt(propVal));
							}
							else if( propName.equals("border_x") && !propVal.equals("null") && !propVal.equals("") ){
								labelBandProp.put("border_x", Integer.parseInt(propVal));
							}
							else if( propName.equals("border_width") && !propVal.equals("null") && !propVal.equals("") ){
								labelBandProp.put("border_width", Integer.parseInt(propVal));
							}
						}// item prop Node List For
					}
					else
					{
						NodeList _props = _item.getElementsByTagName("property");
						int propLen = _props.getLength();
						bandInfo = new HashMap<String, String>();
						// property
						for (int j = 0; j < propLen; j++) {
							NamedNodeMap itemPropNode = _props.item(j).getAttributes();
							if( itemPropNode.getNamedItem("name") == null ) continue;
							
							String propName = itemPropNode.getNamedItem("name").getNodeValue();
							String propVal = itemPropNode.getNamedItem("value").getNodeValue();
							if( propName.equals("useLabelBand") && !propVal.equals("null") && !propVal.equals("") ){
								bandInfo.put("useLabelBand", propVal);
							}
							else if( propName.equals("labelBandColCount") && !propVal.equals("null") && !propVal.equals("") ){
								bandInfo.put("labelBandColCount", propVal);
							}
							else if( propName.equals("labelBandDisplayWidth") && !propVal.equals("null") && !propVal.equals("") ){
								bandInfo.put("labelBandDisplayWidth", propVal);
							}
						}// item prop Node List For
						if(bandInfo.containsKey("useLabelBand") && bandInfo.get("useLabelBand").equals("true"))
						{
							bandData.put(_item.getAttribute("id").toString(), bandInfo);
						}
					}
					continue;
				}
				
				// 라벨 밴드면 columns, border_x, border_width 값을 저장해 두었다가, 컬럼수 만큼 x 값을 추가함.
				if( "UBLabelBand".equals(className) ){
					NodeList _props = _item.getElementsByTagName("property");
					int propLen = _props.getLength();
					labelBandProp = new HashMap<String, Integer>();
					// property
					for (int j = 0; j < propLen; j++) {
						NamedNodeMap itemPropNode = _props.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						if( propName.equals("columns") && !propVal.equals("null") && !propVal.equals("") ){
							labelBandProp.put("columns", Integer.parseInt(propVal));
						}
						else if( propName.equals("border_x") && !propVal.equals("null") && !propVal.equals("") ){
							labelBandProp.put("border_x", Integer.parseInt(propVal));
						}
						else if( propName.equals("border_width") && !propVal.equals("null") && !propVal.equals("") ){
							labelBandProp.put("border_width", Integer.parseInt(propVal));
						}
					}// item prop Node List For
				}
				// 테이블이면 -- 셀 탐색.
				else if( "UBTable".equals(className) || "UBApproval".equals(className) ){
					// Table x 추출
					NodeList _tableProps = _item.getElementsByTagName("property");
					int tbPropLen = _tableProps.getLength();
					int tbBandCnt = 1;
					float tbBandWidth = 0;
					boolean _useBand = true;
					if( bandData.isEmpty() ) _useBand = false;
					String _dsName = "";
					
					int tbX = -1;
					
					float _tbXOri = -1;
					for (int j = 0; j < tbPropLen; j++) {
						NamedNodeMap itemPropNode = _tableProps.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						
//						if( _tableProps.item(j).getParentNode().getNodeName().equals("item") == false && (!"UBApproval".equals(className)||_dsName.equals("") == false )  ) break;
						if( _tableProps.item(j).getParentNode().getNodeName().equals("item") == false ) break;
						
						if( propName.equals("x") && !propVal.equals("null") && !propVal.equals("") ){
							try {
								int dotIdx = propVal.indexOf(".");
								if( dotIdx != -1 ){
									tbX = (int) (Math.round(Double.parseDouble(propVal)) + _pageXPosition);
								}
								else{
//										tbX = Integer.valueOf(propVal) + _pageXPosition;
									tbX = (int) Math.round( Float.valueOf(propVal) + _pageXPosition );
								}
								tbX = Integer.valueOf(propVal);
								
								_tbXOri = Float.valueOf(propVal) + _pageXPosition;
//									if(_useBand == false) break;
							} catch (Exception e) {
								continue;
							}
						}
						else if( bandData.isEmpty()== false && propName.equals("band") && !propVal.equals("null") && !propVal.equals("") )
						{
							if(bandData.containsKey(propVal))
							{
								tbBandCnt = Integer.parseInt( bandData.get(propVal).get("labelBandColCount") );
								tbBandWidth = Float.valueOf(bandData.get(propVal).get("labelBandDisplayWidth"));
							}
							_useBand = false;
//								if(tbX > -1 ) break;
						}
						else if( propName.equals("dataSet") &&  !propVal.equals("null") &&  !propVal.equals("") )
						{
							_dsName = propVal;
						}
					
					}
					if( tbX == -1 ) continue;
					
					NodeList _cells = _item.getElementsByTagName("cell");
					
					int cellLen = _cells.getLength();
					for (int cIdx = 0; cIdx < cellLen ; cIdx++) {
						Element _cell = (Element)_cells.item(cIdx);
						NodeList _props = _cell.getElementsByTagName("property");
						int propLen = _props.getLength();
						
						int xVal = -1;
						int widthVal = -1;
						int dotIdx = -1;

						float xValFloat = -1;
						float widthValFloat = -1;
						// property
						for (int j = 0; j < propLen; j++) {
							NamedNodeMap itemPropNode = _props.item(j).getAttributes();
							String propName = itemPropNode.getNamedItem("name").getNodeValue();
							String propVal = itemPropNode.getNamedItem("value").getNodeValue();
							
							if( propName.equals("x") && !propVal.equals("null") && !propVal.equals("") ){
								try {
									dotIdx = propVal.indexOf(".");
									if( dotIdx != -1 ){
										xVal = (int) Math.round((float) Math.round(Double.parseDouble(propVal)*100)/100 );
									}
									else{
										xVal = Integer.valueOf(propVal);
									}
									
									xValFloat = Float.valueOf(propVal);
									if( widthVal != -1 ) break;
								} catch (Exception e) {
									continue;
								}
							}
							else if( propName.equals("width") && !propVal.equals("null") && !propVal.equals("") ){
								try {
									dotIdx = propVal.indexOf(".");
									if( dotIdx != -1 ){
										widthVal = (int) Math.round( (float) Math.round(Double.parseDouble(propVal)*100)/100 );
									}
									else{
										widthVal = Integer.valueOf(propVal);
									}
									
									widthValFloat = Float.valueOf(propVal);
									if( xVal != -1 ) break;
								} catch (Exception e) {
									continue;
								}
							}
						}// item prop Node List For
						
						if( xVal != -1 && widthVal != -1 ){
							if("UBApproval".equals(className) && !_dsName.equals("") && xVal > 0)
							{
								int _dsSize = 0;
								if( _currentDataSet.get(_dsName) != null )
								{
									_dsSize = _currentDataSet.get(_dsName).size();
								}
								for (int j = 0; j < _dsSize; j++) 
								{
									int _cellX = xVal + ( widthVal *  j );
									if( !_xAr.contains( _cellX +tbX) )	_xAr.add( _cellX +tbX);
									if( !_xAr.contains( _cellX +widthVal+tbX) )	_xAr.add( _cellX +widthVal+tbX);		
									
									float _cellXFl = xValFloat + (widthValFloat * j );
									if( !_xArOri.contains( _cellXFl +_tbXOri) )	_xArOri.add( _cellXFl +_tbXOri);
									if( !_xArOri.contains( _cellXFl +widthVal+_tbXOri) )	_xArOri.add( _cellXFl +widthVal+_tbXOri);		
								}
							}
							else
							{
								if( !_xAr.contains(xVal+tbX) )	_xAr.add(xVal+tbX);
								if( !_xAr.contains(xVal+widthVal+tbX) )	_xAr.add(xVal+widthVal+tbX);
								
								if( !_xArOri.contains( xValFloat +_tbXOri) )	_xArOri.add( xValFloat +_tbXOri);
								if( !_xArOri.contains( xValFloat +widthVal+_tbXOri) )	_xArOri.add( xValFloat +widthVal+_tbXOri);		
							}
						}
						if(tbBandCnt > 1)
						{	
							int _addW = 0;
							float _addWFl = 0f;
							if( tbBandWidth <= 0 ) tbBandWidth = _pageW;
							
							for (int _bandRep = 1; _bandRep < tbBandCnt; _bandRep++) {
								
								_addW = Float.valueOf( (float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep) ).intValue();
								_addWFl =(float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep);
								
								
								
								if( !_xAr.contains(xVal+tbX+(_addW)) )	_xAr.add(xVal+tbX+(_addW));
								if( !_xAr.contains(xVal+widthVal+tbX+(_addW)) )	_xAr.add(xVal+widthVal+tbX+(_addW));
								
								if( !_xArOri.contains(xValFloat+_tbXOri+(_addWFl)) )	_xArOri.add(xValFloat+_tbXOri+(_addWFl));
								if( !_xArOri.contains(xValFloat+widthVal+_tbXOri+(_addWFl)) )	_xArOri.add(xVal+widthVal+_tbXOri+(_addWFl));
								
							}
						}
					}// cell For End.
					
				}
				else if( "UBDataGrid~~~".equals(className) ){
					//----------Data grid 추가되면??
				}
				else if( "UBBarCode2".equals(className) || "UBImage".equals(className) )
				{
					NodeList _props = _item.getElementsByTagName("property");
					int propLen = _props.getLength();
					
					int xVal = -1;
					int yVal = -1;
					int heightVal = -1;
					int widthVal = -1;
					int dotIdx = -1;
					float xValFloat = -1;
					float widthValFloat = -1;
					int _rotate = 0;
					
					int tbBandCnt = 1;
					float tbBandWidth = 0;
					boolean _useBand = true;
					if( bandData.isEmpty() ) _useBand = false;
					int _tmpInt = 0;
					float _tmpFloat = 0;
					
					// property
					for (int j = 0; j < propLen; j++) {
						NamedNodeMap itemPropNode = _props.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						
						if( propName.equals("x") || propName.equals("y") || propName.equals("width") || propName.equals("height") || propName.equals("rotation") )
						{
							try {
								dotIdx = propVal.indexOf(".");
								
								if(propName.equals("rotation"))
								{
									if( dotIdx != -1 ){
										_tmpInt = (int) ( Math.round(Double.parseDouble(propVal)) );
									}
									else{
										_tmpInt = (int) Math.round( Float.valueOf(propVal) );
									}
								}
								else
								{
									if( dotIdx != -1 ){
										_tmpInt = (int) (Math.round(Double.parseDouble(propVal)) + _pageXPosition);
									}
									else{
										_tmpInt = (int) Math.round( Float.valueOf(propVal) + _pageXPosition );
									}
									
									_tmpFloat = Float.valueOf(propVal) + _pageXPosition;
								}
							} catch (Exception e) {
								continue;
							}
							
							if( propName.equals("x") )
							{
								xVal = _tmpInt;
								xValFloat = _tmpFloat;
							}
							else if( propName.equals("width") )
							{
								widthVal = _tmpInt;
								widthValFloat = _tmpFloat;
							}
							else if( propName.equals("y") )
							{
								yVal = _tmpInt;
							}
							else if( propName.equals("height") )
							{
								heightVal = _tmpInt;
							}
							else if( propName.equals("rotation") )
							{
								_rotate = _tmpInt;
							}
							
						}
						else if( bandData.isEmpty()== false && propName.equals("band") && !propVal.equals("null") && !propVal.equals("") )
						{
							if(bandData.containsKey(propVal))
							{
								tbBandCnt = Integer.parseInt( bandData.get(propVal).get("labelBandColCount") );
								tbBandWidth = Float.valueOf(bandData.get(propVal).get("labelBandDisplayWidth"));
							}
							if(xVal != -1 && widthVal != -1  && _rotate != -1 )
							{
								break;
							}
						}
						
					}// item prop Node List For
					
					if( _rotate != 0 )
					{
						//Point _stPoint = common.rotationPosition( (widthVal/2)*-1, (heightVal/2)*-1, _rotate);
						Point _edPoint = common.rotationPosition( (widthVal), (heightVal), _rotate);
						
						if( 0 > _edPoint.x )
						{
							xVal = xVal + _edPoint.x;
							widthValFloat = widthVal =  Math.abs( _edPoint.x );
						}
						else
						{
//							xVal = xVal
							widthValFloat = widthVal = Math.abs( _edPoint.x );
						}
						
					}
					
					if( xVal != -1 && widthVal != -1 ){
						if( !_xAr.contains(xVal) )	_xAr.add(xVal);
						if( !_xAr.contains(xVal+widthVal) )	_xAr.add(xVal+widthVal);
						
						if( !_xArOri.contains( xValFloat) )	_xArOri.add( xValFloat );
						if( !_xArOri.contains( xValFloat + widthVal ) )	_xArOri.add( xValFloat + widthVal );		
					}
					
					if(tbBandCnt > 1)
					{	
						int _addW = 0;
						if( tbBandWidth <= 0 ) tbBandWidth = _pageW;
						for (int _bandRep = 1; _bandRep < tbBandCnt+1; _bandRep++) {
							
							_addW = Float.valueOf( (float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep) ).intValue();
							
							if( !_xAr.contains(xVal+(_addW)) )	_xAr.add(xVal+(_addW));
							if( !_xAr.contains(xVal+widthVal+(_addW)) )	_xAr.add(xVal+widthVal+(_addW));
							
						}
					}
				}
				else
				{
					
					NodeList _props = _item.getElementsByTagName("property");
					int propLen = _props.getLength();
					
					int xVal = -1;
					int widthVal = -1;
					int dotIdx = -1;
					float xValFloat = -1;
					float widthValFloat = -1;
					
					int tbBandCnt = 1;
					float tbBandWidth = 0;
					boolean _useBand = true;
					if( bandData.isEmpty() ) _useBand = false;
					
					// property
					for (int j = 0; j < propLen; j++) {
						NamedNodeMap itemPropNode = _props.item(j).getAttributes();
						String propName = itemPropNode.getNamedItem("name").getNodeValue();
						String propVal = itemPropNode.getNamedItem("value").getNodeValue();
						
						if( propName.equals("x") && !propVal.equals("null") && !propVal.equals("") ){
							try {
								dotIdx = propVal.indexOf(".");
								if( dotIdx != -1 ){
									xVal = (int) (Math.round(Double.parseDouble(propVal)) + _pageXPosition);
								}
								else{
									xVal = (int) Math.round( Float.valueOf(propVal) + _pageXPosition );
								}
								
								xValFloat = Float.valueOf(propVal) + _pageXPosition;
								if( widthVal != -1 && _useBand == false ) break;
							} catch (Exception e) {
								continue;
							}
						}
						else if( propName.equals("width") && !propVal.equals("null") && !propVal.equals("") ){
							try {
								dotIdx = propVal.indexOf(".");
								if( dotIdx != -1 ){
									widthVal = (int)Math.round(Double.parseDouble(propVal));
								}
								else{
									widthVal = Integer.valueOf(propVal);
								}
								widthValFloat = Float.valueOf(propVal);
								if( xVal != -1  && _useBand == false  ) break;
							} catch (Exception e) {
								continue;
							}
						}
						else if( bandData.isEmpty()== false && propName.equals("band") && !propVal.equals("null") && !propVal.equals("") )
						{
							if(bandData.containsKey(propVal))
							{
								tbBandCnt = Integer.parseInt( bandData.get(propVal).get("labelBandColCount") );
//								tbBandWidth = Float.valueOf(bandData.get(propVal).get("width"));
								tbBandWidth = Float.valueOf(bandData.get(propVal).get("labelBandDisplayWidth"));
							}
							if(xVal != -1 && widthVal != -1 )
							{
								break;
							}
						}
					}// item prop Node List For
					
					if( xVal != -1 && widthVal != -1 ){
						if( !_xAr.contains(xVal) )	_xAr.add(xVal);
						if( !_xAr.contains(xVal+widthVal) )	_xAr.add(xVal+widthVal);
						
						if( !_xArOri.contains( xValFloat) )	_xArOri.add( xValFloat );
						if( !_xArOri.contains( xValFloat + widthVal ) )	_xArOri.add( xValFloat + widthVal );		
					}
					
					if(tbBandCnt > 1)
					{	
						int _addW = 0;
						if( tbBandWidth <= 0 ) tbBandWidth = _pageW;
						for (int _bandRep = 1; _bandRep < tbBandCnt+1; _bandRep++) {
							
							_addW = Float.valueOf( (float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep) ).intValue();
							
							if( !_xAr.contains(xVal+(_addW)) )	_xAr.add(xVal+(_addW));
							if( !_xAr.contains(xVal+widthVal+(_addW)) )	_xAr.add(xVal+widthVal+(_addW));
							
						}
					}
					
				}
			}// item for
			
			int xLen = 0;
			// 문서가 라벨밴드라면,
			if( labelBandProp != null ){
				int columns = labelBandProp.get("columns");
				int labelWidth = labelBandProp.get("border_x") + labelBandProp.get("border_width");
				
				// 위에서 만든 x array 에 column 개수만큼 더해준다.
				xLen = _xAr.size();
				for (int i = 0; i < xLen; i++) {
					for (int j = 1; j < columns; j++) {
						int newPos = _xAr.get(i)+labelWidth*j;
						if( !_xAr.contains(newPos) )	_xAr.add(newPos);
						
						float newPosFl = _xArOri.get(i)+labelWidth*j;
						if( !_xArOri.contains( newPosFl) )	_xArOri.add( newPosFl );
					}
				}
			}
			
			// 페이지 별 x 배열을 누적시킨다.
			xLen = _xAr.size();
			for (int i = 0; i < xLen; i++) {
				int _xVal = _xAr.get(i);
				float _xArFl = Integer.valueOf(_xAr.get(i)).floatValue();
				if( !xAr.contains(_xVal) )	xAr.add(_xVal);
				
//				xArFloat.add(_xArOri.get(i));
				if( !xArFloat.contains(_xArFl) )	xArFloat.add( _xArFl );
				
			}
			
			xLen = _xArOri.size();
			for (int i = 0; i < xLen; i++) {
				float _xValF = _xArOri.get(i);
				if( !xArFloat.contains(_xValF) )	xArFloat.add(_xValF);
			}
			
			
			// Clone Page값 담기
			String cloneData = _page.getAttribute("clone");
			int _cloneColCnt = 1;
			
			if( _isConnect == false )
			{
				if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL)  || cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM))
				{
					if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
					{
						_cloneColCnt = 1;
					}
					else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) )
					{
						_cloneColCnt = 2;
					}
					else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM) )
					{
						_cloneColCnt = Integer.parseInt(_page.getAttribute("cloneColCount"));
					}
				}
			}
			else
			{
				_cloneColCnt = _cloneCountArgo;
			}
			
			if( _cloneColCnt > 1)
			{
				float pageWidth = Float.valueOf(_page.getAttribute("width")) / _cloneColCnt;
				
				int _size = xArFloat.size();
				
				for (int j = 0; j < _cloneColCnt; j++) {
					for (int i = 0; i < _size; i++) {
						int _itemX = Math.round((float) Math.round( ( xArFloat.get(i) + ( pageWidth * j ) )*100)/100 );
						if( cloneXAr.indexOf(_itemX) == -1 ) cloneXAr.add(_itemX);
					}
				}
				xAr = cloneXAr;
				_cloneCountArgo = _cloneColCnt;
			}
			
		}// page for
		
		Collections.sort(xAr);

		return xAr;
	}
	
	protected ArrayList<Integer> onePageXArr;
	
	/** 
	 * 페이지 별 Y좌표 계산.
	 * @param pageNo 현재 페이지 번호.
	 * @return 계산된 yArray.
	 * */
	protected ArrayList<Integer> makeYArray(int pageNo) {
		ArrayList<Integer> yArray = new ArrayList<Integer>();
		
		boolean _isSplitOnePage = false;
		if(mExcelSheetSplitType.equals(GlobalVariableData.UB_EXCEL_SHEET_SPLIT_PAGE))
		{
			onePageXArr = new ArrayList<Integer>();
			_isSplitOnePage = true;
		}
		
		if( itemPropList == null ){
			log.error(getClass().getName() + "::" + "makeYArray::" + "item Array is null." );
			return null;
		}
		ArrayList<HashMap<String, Object>> mAr = itemPropList.get(0);
//		ArrayList<HashMap<String, Object>> mAr = itemPropList.get(pageNo);
			
		// 페이지  y 시작점을 위하여.
		HashMap<String, Object> pageInfo = mAr.get(mAr.size()-1);
		int bPageH = toNum(pageInfo.get("bPageHeight"));
		int cPageH = toNum(pageInfo.get("cPHeight"));
		int cArgoH = toNum(pageInfo.get("cPStartHeight"));
		if( cPageH != -1 ){
			cPageH += bPageH;
		}
		
		if( bPageH == -1 && cArgoH != -1  ) cArgoH = cArgoH -1; 
		
		if( !yArray.contains( cArgoH ) && cArgoH != -1 ) yArray.add(cArgoH);
		if( !yArray.contains( bPageH ) && bPageH != -1 ) yArray.add(bPageH);
		if( !yArray.contains( cPageH ) && cPageH != -1 && mExcelUsePageHeight ) yArray.add(cPageH);
		
		if(mBackgroundImage != null && mBackgroundImage.isEmpty() == false )
		{
			mBackgroundImage.put("argoH",bPageH );
			
			if( yArray.indexOf( Float.valueOf( mBackgroundImage.get("top").toString()).intValue() + bPageH ) == -1 )
			{
				yArray.add( Float.valueOf(mBackgroundImage.get("top").toString()).intValue() + bPageH );
			}
			if( yArray.indexOf( Float.valueOf(mBackgroundImage.get("top").toString()).intValue() + Float.valueOf(mBackgroundImage.get("height").toString()).intValue() + bPageH ) == -1 )
			{
				yArray.add( Float.valueOf(mBackgroundImage.get("top").toString()).intValue() + Float.valueOf(mBackgroundImage.get("height").toString()).intValue() + bPageH );
			}
			
			if(_isSplitOnePage)
			{
				if( onePageXArr.indexOf( Float.valueOf( mBackgroundImage.get("left").toString() ).intValue() ) == -1 )
				{
					onePageXArr.add(Float.valueOf( mBackgroundImage.get("left").toString() ).intValue() );
				}
				
				if( onePageXArr.indexOf( Float.valueOf( mBackgroundImage.get("left").toString() ).intValue() + Float.valueOf(mBackgroundImage.get("width").toString()).intValue() ) == -1 )
				{
					onePageXArr.add(Float.valueOf( mBackgroundImage.get("left").toString() ).intValue() + Float.valueOf(mBackgroundImage.get("width").toString()).intValue() );
				}
			}
		}
		
		for (HashMap<String, Object> item : mAr) {
			
			// visible false인 아이템은 패스!
			if( item.get("className") == null || ( item.get("visible") != null && item.get("visible").equals("")==false && !ValueConverter.getBoolean(item.get("visible")) ) ) continue;
			
			if( mNotExportItems.indexOf( item.get("className").toString() ) != -1 ) continue;
			
			if( item.containsKey("rotate") && Integer.parseInt(item.get("rotate").toString()) != 0 )
			{
				Point _edPoint = common.rotationPosition( toNum( item.get("width") ), toNum( item.get("height") ) , Integer.parseInt(item.get("rotate").toString()) );
				
				if( 0 > _edPoint.x )
				{
					item.put("x", toNum( item.get("x") ) + _edPoint.x );
				}
				
				item.put("width", Math.abs( _edPoint.x ));
				item.put("height", Math.abs( _edPoint.y ));
				
				if( 0 > _edPoint.y )
				{
					item.put("y", toNum( item.get("y") ) + _edPoint.y );
				}
				
			}
			
			//@ TEST 엑셀 저장시 SVGArea아이템의 fixedToSize가 false일때 xAr에 아이템의 width배열이 없어 이미지가 표시되지 않음.
//			if(  item.get("className").equals("UBSVGArea") && item.containsKey("ORI_WIDTH") && item.containsKey("ORI_HEIGHT"))
//			{
//				item.put("width", item.get("ORI_WIDTH"));
//				item.put("height", item.get("ORI_HEIGHT"));
//			}
			
			Integer y = toNum( item.get("y") ) + bPageH;
			Integer h = toNum( item.get("height") );

			Integer x = toNum( item.get("x") );
			Integer w = toNum( item.get("width") );
			
			//--------------------------------------Y Array--------------------------------------//
			// item이 라인인 경우.
			if( String.valueOf(item.get("className")).equals("UBGraphicsLine") ){

				Integer x1 = toNum( item.get("x1") );
				Integer x2 = toNum( item.get("x2") );
				Integer y1 = toNum( item.get("y1") );
				Integer y2 = toNum( item.get("y2") );
				
				w = Math.abs(x2 - x1);
				h = Math.abs(y2 - y1);
				
				if( y != -1 &&  y1 != -1 &&  y2 != -1 ){	
					if( !yArray.contains( y ) ) yArray.add(y);
					if( !yArray.contains( y+h ) ) yArray.add(y+h);
				}
				item.put("height", h);
				item.put("width", w);
				
				x = (x2 > x1) ? toNum( item.get("x1") ) : toNum( item.get("x2") );
			}
			else if( y != -1 && h != -1 ){
				if( !yArray.contains( y ) ) yArray.add(y);
				if( !yArray.contains( y+h ) ) yArray.add(y+h);
		}
			
			if(_isSplitOnePage &&  x != -1 && w != -1 )
			{
				if( !onePageXArr.contains( x ) )
				{
					onePageXArr.add( x );
				}
				
				if( !onePageXArr.contains( x + w ) )
				{
					onePageXArr.add( x + w );
				}
				
			}
		}
		
		
		if(_isSplitOnePage )
		{
			if( onePageXArr.contains(0) == false ) onePageXArr.add(0);
		}
		
		Collections.sort(yArray);
		
		if( yArray.size() > 1)
		{
			boolean _useAppendRowSize = false;
			int _argoH = yArray.get(0);
			int _chkNum = 0;
			int _tmpNum = 500;
			
			// yAr의 사이 간격이 500px이 넘을경우 500마다 값을 추가한다.
			for( int i = 1; i < yArray.size(); i++ )
			{
				_chkNum = yArray.get(i);
				
				while( _chkNum - _argoH  > _tmpNum )
				{
					yArray.add( _argoH + _tmpNum);
					_chkNum -= _tmpNum;
					if(!_useAppendRowSize)_useAppendRowSize = true;
				}
				
				_argoH = yArray.get(i);
			}
			
			Collections.sort(yArray);
			
//			if( !mExcelUsePageHeight )
//			{
//				int _lastH = yArray.get(yArray.size() -1 );
//				yArray.add(_lastH +  30);
//			}
			
		}
		
		
		if(_isSplitOnePage && onePageXArr != null && onePageXArr.size() > 0 )
		{
			Collections.sort(onePageXArr);
			xArrayGlobal = onePageXArr;
		}

		if( pageInfo.containsKey("cPWidth"))
		{
			int _cpWidth = toNum(pageInfo.get("cPWidth"));
			
			if( xArrayGlobal.indexOf(_cpWidth ) == -1 ) xArrayGlobal.add(_cpWidth);
			Collections.sort(xArrayGlobal);
		}
		
		
		return yArray;
	}
	
	
	/**
	 * functionName : getDocumentXArray
	 * @return [Arraylsit:elementList(page의 item Element들이 담긴 리스트), ArrayList: pageMarginList ( ConnectLinked 일경우 각 페이지의 X좌표 값을 담아두는 배열 ) ]
	 */
	protected ArrayList<ArrayList<Integer>> getDocumentXArray( ArrayList<String> _documents, Document _oneDoc, HashMap<String, Object> _pageInfo, ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>> _dataSets, HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, ArrayList< HashMap<String,String> > _documentInfos )
	{
		int _docIdx = 0;
		int _docLength = 0;
		
		Document _doc = null;
		int _pageCnt = 0;
		
		Document _subDoc = null;
		NodeList _pages = null;
		int i = 0;
		int j = 0;
		int _docPagesSize = 0;
		HashMap<String, ArrayList<HashMap<String, Object>>> _currentDataSet = null;
		if( _documents != null && _documents.size() > 0)
		{
			_docLength = _documentInfos.size();
		}
		else
		{
			_docLength = 1;
			_currentDataSet = _dataSet;
		}
		
		ArrayList<ArrayList<Integer>> _retXarray = new ArrayList<ArrayList<Integer>>();
		
		ArrayList<ArrayList<Integer>> _totXAr = null;
		ArrayList<Integer> _xAr = null;
		if( _pageInfo.containsKey("EXCEL_X_ARR_TOT") )
		{
			_totXAr = (ArrayList<ArrayList<Integer>>) _pageInfo.get("EXCEL_X_ARR_TOT");
		}
		else
		{
			_xAr = (ArrayList<Integer>) _pageInfo.get("EXCEL_X_ARR");
		}
		
		for( _docIdx = 0; _docIdx < _docLength; _docIdx++ )
		{
			ArrayList<Element> _pageElements = new ArrayList<Element>();
			ArrayList<Float> _pageMarginList = new ArrayList<Float>();
			
			if( _documents != null && _documents.size() > 0)
			{
//				_doc = _documents.get(_docIdx);
				InputSource _is = new InputSource(new StringReader( _documents.get( Integer.parseInt( _documentInfos.get(_docIdx).get("IDX") ) ) ));
				try {
					_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				_currentDataSet = _dataSets.get(_docIdx);
			}
			else
			{
				_doc = _oneDoc;
			}
			
			if( _totXAr != null ){
				_xAr = _totXAr.get(_docIdx);
			}
			
			if(_doc.getDocumentElement().getAttribute("projectType").equals("12"))
			{
				ArrayList<Object> _pageInfoData = (ArrayList<Object>) _pageInfo.get("PAGE_INFO_DATA_LIST");
				
				int _infoS = _pageInfoData.size();
				
				for (int k = 0; k < _infoS; k++) {
					
					ArrayList<HashMap<String, Object>> pagesInfoData = (ArrayList<HashMap<String, Object>>) ( (HashMap<String,Object>)_pageInfoData.get(k) ).get("pageInfoData");
					
					int _pageInfoSize = pagesInfoData.size();
					
					for ( i = 0; i < _pageInfoSize; i++) {
						HashMap<String, Object> _info = pagesInfoData.get(i);
						_subDoc = (Document) _info.get("document");
						_pages = _subDoc.getElementsByTagName("page");
						_docPagesSize = _pages.getLength();
						
						for ( j = 0; j < _docPagesSize; j++) {
							_pageElements.add((Element) _pages.item(j) );
							_pageMarginList.add( Float.valueOf( ((HashMap<String, Object>) _info.get("itemProperty")).get("x").toString() ) );
						}
					}
				}
			}
			else
			{
				_pages = _doc.getElementsByTagName("page");
				_docPagesSize = _pages.getLength();
				
				for ( j = 0; j < _docPagesSize; j++) {
					_pageElements.add((Element) _pages.item(j) );
					_pageMarginList.add(0f);
				}
			}
			
			_retXarray.add( makeXArray(_pageElements, _pageMarginList, _xAr , _currentDataSet ) );
		}
		
		
		return _retXarray;
	}
	
	
	/**
	 * <pre>
	 * 라디오버튼/체크박스 아이템을 선택된 상태에 따라 문자로 치환하여 표시한다.
	 * 라디오 :  "⊙ ":"○ "
	 * 체크 : "▣ ":"□ "
	 * 체크박스의 경우 체크모양 딩벳기호는 깨져 네모로 대체한다.
	 * </pre>
	 * @param _item 라디오버튼 아이템
	 * @return _item 텍스트 값이 지정된 아이템.
	*/
	protected HashMap<String, Object> setEformItemAttr( HashMap<String, Object> _item ) {
		
		String itemClassName = ValueConverter.getString(_item.get("className"));
		boolean isSelected = ValueConverter.getBoolean(_item.get("selected"));
		String iconTxt = "";

		String symbolType = ValueConverter.getString(_item.get("symbolType"));
		
		if( itemClassName.equals("UBRadioBorder") ){
			if( symbolType.equalsIgnoreCase("check") ){
				iconTxt = (isSelected)? "▣ ":"□ ";
			}else{
				iconTxt = (isSelected)? "⊙ ":"○ ";	
			}
			
		}
		else if( itemClassName.equals("UBCheckBox") ){
			iconTxt = (isSelected)? "▣ ":"□ ";
		}
		
		_item.put("text", iconTxt+ValueConverter.getString(_item.get("label")) );
		
		return _item;
	}
	
	
	/**
	 * <pre>
	 * 서버에서 만들어진 SVGRichText 아이템을 
	 * 엑셀에서는 텍스트와 첫번째 아이템의 스타일만 뽑아서 넣는다.
	 * </pre>
	 * @param _item svg text값이 배열로 들어있는 아이템.
	 * @return HashMap<String, Object> 스타일 속성 및 text 값을 뽑아낸 새 아이템.
	*/
	protected HashMap<String, Object> convertSvgTextItem( HashMap<String, Object> _item ){
		
		HashMap<String, Object> convertedItem = new HashMap<String, Object>();
		convertedItem.put("verticalAlign", "top");
		convertedItem.put("textAlign", "left");
		convertedItem.put("fontColor", _item.get("fontColor"));
		convertedItem.put("fontWeight", _item.get("fontWeight"));
		convertedItem.put("fontSize", _item.get("fontSize"));
		convertedItem.put("fontFamily", _item.get("fontFamily"));
		
		StringBuilder builder = new StringBuilder();
		
		// linegap
		try {
			int lineGap = ValueConverter.getInteger(_item.get("lineGap"));
		} catch (Exception e) {
			// integer 변환 에러시 default로 사용.
		}
		
		ArrayList<ArrayList<HashMap<String, Object>>> lineAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("children");
		for (ArrayList<HashMap<String, Object>> textAr : lineAr) {
			int len = textAr.size();
			for (int i = 0; i < len ; i++) {
				HashMap<String, Object> textItem = textAr.get(i);
				
				// 첫번째 아이템의 스타일로 셀에 적용하기.
//				if( i==0 ){
//					convertedItem.put("fontWeight", textItem.get("fontWeight"));
//					convertedItem.put("fontSize", textItem.get("fontSize"));
//					convertedItem.put("fontFamily", textItem.get("fontFamily"));
//				}
				builder.append( ValueConverter.getString(textItem.get("text")) );
			}
			builder.append("\n");
		}
		
		convertedItem.put("text", builder.toString());
		
		return convertedItem;
	}
	
	
	/** 
	 * xArrayGlobal와 현재 페이지 yArray 를 가지고 xySetSetting
	 * @param yArray 현재 페이지의 Y좌표 리스트.
	 * */
	protected void makePositionArray2( ArrayList<Integer> yArray, int pageNo ) {

		if( itemPropList == null ){
			log.error(getClass().getName() + "::" + "makePositionArray2::" + "item Array is null." );
			return;
		}
		// X, Y 값을 정렬하여, 2차원 배열 구조의 xySetArray 생성.
		
//		log.info(getClass().getName() + "::" + "makePositionArray2::" + "makePositionArray...");
		
		xySetArray = new ArrayList<ArrayList<HashMap<String, Object>>>();
		if( xArrayGlobal.get(xArrayGlobal.size()-1) < 845 )
		{
			//xArrayGlobal.add(845);
		}
		
		if(mBackgroundImage != null && mBackgroundImage.isEmpty() == false )
		{
			if( xArrayGlobal.indexOf( Float.valueOf(mBackgroundImage.get("left").toString()).intValue() ) == -1 )
			{
				xArrayGlobal.add( Float.valueOf(mBackgroundImage.get("left").toString()).intValue() );
			}
			if( xArrayGlobal.indexOf( Float.valueOf(mBackgroundImage.get("left").toString()).intValue() + Float.valueOf(mBackgroundImage.get("width").toString()).intValue() ) == -1 )
			{
				xArrayGlobal.add( Float.valueOf(mBackgroundImage.get("left").toString()).intValue() + Float.valueOf(mBackgroundImage.get("width").toString()).intValue()  );
			}
			
			Collections.sort(xArrayGlobal);
		}
		
		int yArLen = yArray.size();
		for (int y = 0; y < yArLen-1; y++) {
			
			int h = yArray.get(y+1) - yArray.get(y);
			
			ArrayList< HashMap<String, Object>> xSetArray = new ArrayList<HashMap<String, Object>>();
			int xArLen = xArrayGlobal.size();
			for (int _x = 0; _x < xArLen-1; _x++) {
				int w = xArrayGlobal.get(_x+1) - xArrayGlobal.get(_x);
				
				HashMap<String, Object> tmp = new HashMap<String, Object>();
				tmp.put("width", w);
				tmp.put("height", h);
				tmp.put("type", "none");
				
				xSetArray.add( tmp );
				
				 if(  y == 0 && rowIdx == 0 )
			      {
					 
//			            int celW = PixelUtil.pixel2WidthUnits( w );
			            int celW = getRatioPixel2WidthUnits( w ); 
			            sheet.setColumnWidth( _x, celW );
			      }
				
			}
			
			xySetArray.add(xSetArray);
		}
		
//		log.info(getClass().getName() + "::" + "makePositionArray2::" + "Making Position Array is complete!");

		mappingItemPosition( xArrayGlobal, yArray, pageNo );
	}


	/** 
	 * <pre>
	 * 생성된 X, Y 좌표 배열 정보와 아이템의 정보를 매핑.
	 * 기존 아이템에 속성 추가
	 * 
	 * x 좌표 = column 인덱스
	 * y 좌표 = row 인덱스
	 * colSpan
	 * rowSpan
	 * type = item
	 * 
	 * <b>Reference</b>
	 * GS_UBIViewlib.src.ubstorm.ubExport.ExportExcel.ExportExcelParser.as
	 * tableObjectInit()
	 * </pre>
	 * */
	private void mappingItemPosition( ArrayList<Integer> xAr, ArrayList<Integer> yAr, int pageNo ){
		if( xAr == null || xAr.size() == 0 || yAr == null || yAr.size() == 0 ){ 
			log.error(getClass().getName() + "::" + "mappingItemNPosition::" + "x or y position array is invalid.");
			return;
		}

		// xy 테이블 배열의 값(PX)을 인덱스로 변경.
//		log.info(getClass().getName() + "::" + "mappingItemNPosition::" + "mapping Items and Position...");
		
		ArrayList< HashMap<String, Object>> newItemList = new ArrayList< HashMap<String, Object>>();
//		mImgItemList = new ArrayList<HashMap<String,Object>>();

		// yAr 의 범위에 있는 아이템만 검색.
		ArrayList<HashMap<String, Object>> mAr = itemPropList.get(0);
//		ArrayList<HashMap<String, Object>> mAr = itemPropList.get(pageNo);
//		for (ArrayList<HashMap<String, Object>> mAr : itemPropList) {

			// 페이지  y 시작점을 위하여.
			int bPageH = toNum(mAr.get(mAr.size()-1).get("bPageHeight"));
			
			for (HashMap<String, Object> item : mAr) {
				// visible false인 아이템은 패스!
				if( ( item.get("visible") != null && item.get("visible").equals("")==false &&  !ValueConverter.getBoolean(item.get("visible")) ) || item.containsKey("x") == false ) continue;
				
				// 배열에 저장된 값의 인덱스 추출. 
//				int itemX = toNum(item.get("x")); 
				int itemY = toNum(item.get("y"));
				
				// x값이 34.499999999999 width값이 300.3333393339999 와 같이 소숫점때문에 값이 틀어질수 있어 *100하고 /100하여 값을 근사치로 올리고 round시키도록 수정  -2016.04.15  최명진 
				int _itemX = Math.round((float) Math.round(Double.parseDouble(item.get("x").toString())*100)/100 );
				int _itemW = Math.round((float) Math.round(Double.parseDouble(item.get("width").toString())*100)/100 ); 
				
//				int x = xAr.indexOf(itemX);
				int x = xAr.indexOf(_itemX);
				int y = yAr.indexOf(itemY+bPageH);
//				int w = xAr.indexOf(itemX+toNum(item.get("width")) );
				int w = xAr.indexOf(_itemX+_itemW );
				int h = yAr.indexOf(itemY+bPageH+toNum(item.get("height")) );
				
				if( w == -1 && item.get("className").toString().equals("UBSVGArea") && item.containsKey("fixedToSize") && item.get("fixedToSize").toString().equals("false") )
				{
					for(int i = x +1; i < xAr.size(); i++ )
					{
						if( xAr.get(i) > _itemX+_itemW )
						{
							w = i;
							break;
						}
					}
				}
				
				
				if( x == -1 || y == -1 || w == -1 || h == -1 ) continue;
				item.put("colSpan", w);
				item.put("rowSpan", h);
				item.put("classType", item.get("type"));
				item.put("type", "item");
				item.put("x", x);
				item.put("y", y);
				// 배열에 바꿔치기.
				if(xySetArray.size() > y && xySetArray.get(y).size() > x)
				{
					xySetArray.get(y).set(x, item);
					newItemList.add(item);
				}
				
				// 겹친 셀을 계산할 라벨 아이템만 리스트에 추가한다.
//				String className = String.valueOf(item.get("className"));
//				if( className.contains("Label") || className.equals("UBTable") || className.equals("UBApproval")){
//					xySetArray.get(y).set(x, item);
//					newItemList.add(item);
//				}
//				// 이미지로 추가할 아이템은 이미지 리스트에 따로 저장한다.
//				else{
//					mImgItemList.add(item);
//				}
				
			}
			
//		} // itemPropList for
			mAr = null;
//		log.info(getClass().getName() + "::" + "mappingItemNPosition::" + "mapping Items and Position Complete!");

		cellPropertySetting( xAr, yAr, newItemList );
	}

	/**
	 * <pre>
	 * 좌표겹치는 경우 계산.
	 * 
	 * 1. list 에서 item의 위치 찾기
	 * 2. item의 colSpan , rowSpan에서 type 검색
	 * 3. type none 이 아닌 td가 있을경우 위치 체크
	 * 
	 * <b>Reference</b>
	 * GS_UBIViewlib.src.ubstorm.ubExport.ExportExcel.ExportExcelParser.as
	 * cellPropertySetting()
	 * </pre>
	 *  */
	private void cellPropertySetting( ArrayList<Integer> xAr, ArrayList<Integer> yAr, ArrayList< HashMap<String, Object>> newItemList ){

//		log.info(getClass().getName() + "::" + "cellPropertySetting::" + "cellPropertySetting...");
		int x=0;
		int y=0;
		int c=0;
		int r=0;
		int i=0;
		int j=0;
		ArrayList<HashMap<String, Object>> sizeAr;
		
//		int cnt = 0;
		for (HashMap<String, Object> item : newItemList) {
			
			if( !ValueConverter.getBoolean(item.get("visible")) && item.get("visible") != null && item.get("visible").equals("")==false ) continue;
			
			x = toNum( item.get("x") );
			y = toNum( item.get("y") );
			c = toNum( item.get("colSpan") );
			r = toNum( item.get("rowSpan") );
			
			// 이미지, 도형 아이템의 경우는 span값만 바꿔주고 아무 작업도 하지 않는다.
			String className = String.valueOf(item.get("className"));
			if( item.containsKey("src") || className.contains("Graphic") ){
				item.put("colSpan", c-x);
				item.put("rowSpan", r-y);
				continue;
			}
			
			sizeAr = new ArrayList<HashMap<String, Object>>();
			// 아이템의 col, row span 을 검사해서 겹친 아이템이 있는지 확인한다.
			for (i = y; i < r; i++) {
				for (j = x; j < c; j++) {
					HashMap<String, Object> ijSetCell = xySetArray.get(i).get(j);
					if( item != ijSetCell ){
						// 겹친 아이템이 이미지, 도형인 경우 패스!
						if( ijSetCell.containsKey("src") ||( ijSetCell.containsKey("className") && ijSetCell.get("className").toString().contains("Graphic") ) ||  className.contains("Graphic") ){
							continue;
						}
						else{
							String sType = String.valueOf(ijSetCell.get("type"));
							if( "item".equals(sType)|| "D".equals(sType)){
								// colspan 변경.
								c= j;
								break;
							}
						}
					}// item != xySetArray.get(y).get(x) 
				}// x(j) For
				HashMap<String, Object> _tmp = new HashMap<String, Object>();
				_tmp.put("max", ( toNum(yAr.get(i+1))-toNum(yAr.get(y)) )*( toNum(xAr.get(c))-toNum(xAr.get(x))) );
				_tmp.put("w", toNum(xAr.get(c))-xAr.get(x));
				_tmp.put("c", c);
				_tmp.put("r", i+1); // row span 변경
				sizeAr.add(_tmp);
				_tmp = null;
			}// y(i) For

			if( sizeAr.size() > 0 ){
				HashMap<String, Object> size = new HashMap<String, Object>();
				
				for (HashMap<String, Object> sMap : sizeAr) {

					if( size.containsKey("max") ){
						int sizeMax = toNum(size.get("max"));
						int sMapMax = toNum(sMap.get("max"));
						if( sizeMax == sMapMax ){
							if( toNum(sMap.get("w")) > 50 ){
								size = sMap;
							}
						}
						else{
							if( sizeMax < sMapMax ){
								size = sMap;
							}
						}
					}
					else{
						size = sMap;
					}
					
				}//sizeAr For

				// 원래 크기의 col, row span 값
				 int _defCol = toNum( item.get("colSpan") ) - x;
				 int _defRow = toNum( item.get("rowSpan") ) - y;
				 
				 // 쪼개지는 col, row span 값
				int itemColSpan = toNum(size.get("c"))-x;
				int itemRowSpan = toNum(size.get("r"))-y;				
				item.put("colSpan", itemColSpan);
				item.put("rowSpan", itemRowSpan);
				
				HashMap<String, Object> yxSetCell = null;
				
				int yN = y;
				int xN = x;
				for (i = 0; i < itemRowSpan; i++) {
					xN = x;
					for (j = 0; j < itemColSpan; j++) {
						//겹친경우
//						if( item != yxSetCell && yxSetCell != null ){
						yxSetCell = xySetArray.get(yN).get(xN);
						if( yxSetCell != null &&  item != yxSetCell/*!item.get("id").toString().equals(yxSetCell.get("id").toString()) */){
							if( xySetArray.get(yN).size()<=xN ){
								log.error(getClass().getName() + "::" + "cellPropertySetting::" + "Index Out Of Bounds Exception" );
								break;
							}
							
							if( ValueConverter.getString(yxSetCell.get("type")).equals("none") ){
								yxSetCell.put("type","D");
							}
							else{
//								log.debug(getClass().getName() + "::" + "cellPropertySetting::" + "type == " + yxNSetCell.get("type") );
							}
						}// item != yxSetCell
						xN++;
					}
					yN++;
				}
				
				
				/*
					쪼개진 아이템의 border 정보 변경.
				 */
				 item = getborderCheck(item);
				 if( item != null && item.containsKey("uBorder") ){
					 /*
					 HashMap<String, Object> _lB = new HashMap<String, Object>();
					 HashMap<String, Object> _rB = new HashMap<String, Object>();
					 HashMap<String, Object> _tB = new HashMap<String, Object>();
					 HashMap<String, Object> _bB = new HashMap<String, Object>();
					 */
					 HashMap<String, Object> _lB = null;
					 HashMap<String, Object> _rB = null;
					 HashMap<String, Object> _tB = null;
					 HashMap<String, Object> _bB = null;
					 HashMap<String, HashMap<String, Object>> _uBorder =  (HashMap<String, HashMap<String, Object>>) item.get("uBorder");
					 
					 if( _uBorder.containsKey("L") )	_lB = _uBorder.get("L");
					 if( _uBorder.containsKey("R") )	_rB = _uBorder.get("R");
					 if( _uBorder.containsKey("T") )	_tB = _uBorder.get("T");
					 if( _uBorder.containsKey("B") )	_bB = _uBorder.get("B");
					 
					 if( toNum(item.get("colSpan")) == _defCol && toNum(item.get("rowSpan")) == _defRow )
					 {
					 }
					 else
					 {
						 int _iY = i+y;
						 int _jX = j+x;
						 
						 // 쪼개진 셀이면 Right, Bottom 테두리 제거
						 if( toNum(item.get("colSpan")) != _defCol )
						 {
							 if( _rB.containsKey("borderType") )
								 _uBorder.remove("R");
						 }
						 
						 if( toNum(item.get("rowSpan")) != _defRow )
						 {
							 if( _bB.containsKey("borderType") )
								 _uBorder.remove("B");
						 }
						 
						 // 셀 쪼개기 전의 colspan, rowspan 만큼 반복하면서 type이 none인 아이템을 찾아, 선을 추가한다.
						 for( i = 0 ; i < _defRow ; i ++ )
						 {
							 _iY = i + y;
							 
							 // 맨 윗줄이거나, 오른쪽 끝 셀인경우.
							 if( _iY == y || i == ( _defRow - 1 ) )
							 {
								 for( j = 0 ; j < _defCol ; j ++ )
								 {
									 _jX = j + x;
									 
									 HashMap<String, Object> checkItem = xySetArray.get(_iY).get(_jX);
									 if( checkItem != item )
									 {
										 String t = ValueConverter.getString(checkItem.get("type"));
										 
										 // item 이 none 이면
										 if( !t.equals("D") && !t.equals("item") )
										 {
											 HashMap<String, Object> newBorder = new HashMap<String, Object>();
											 // 기존에 있던 값을 none 셀에 넣어줌.
											 if( _iY == y )
											 {
//												 if( _tB.size() > 0 )
												 if( _tB != null )
													 newBorder.put("T", _tB);
												 
												 if( j == ( _defCol - 1 ) )
												 {
													 if( _rB.containsKey("borderType") )
														 newBorder.put("R", _rB);
												 } 
											 }
											 else
											 {
//												 if( _bB.size() > 0 )
												 if( _bB != null )
													 newBorder.put("B", _bB);
												 
												 if( j == ( _defCol - 1 ) )
												 {
//													 if( _rB.size() > 0 )
													 if( _rB != null )
														 newBorder.put("R", _rB);
												 }
												 else if( _jX == x )
												 {
//													 if( _lB.size() > 0 )
													 if( _lB != null )
														 newBorder.put("L", _lB);
												 }
											 }
											 checkItem.put("uBorder", newBorder);
											 newBorder = null;
											 // background color 추가
											 if( item.containsKey("backgroundColorInt") && isValid(item.get("backgroundColorInt")) ){
												 checkItem.put("backgroundColorInt", item.get("backgroundColorInt"));
											 }
											 
										 }// !t.equals("D") && !t.equals("item")
									 }// checkItem != item
								 }// j For
								 
							 }// _iY == y || i == ( _defRow - 1 )
							 
							 
							 else
							 {
								 HashMap<String, Object> checkItem = null;
								 String t="";
								 
								 // Left
								 checkItem = xySetArray.get(_iY).get(x);
								 t = String.valueOf(checkItem.get("type"));
								 if( t.equals("D") || t.equals("item") )
								 {
								 }
								 else
								 {
									 // background color 추가
									 if( isValid(item.get("backgroundColorInt")) ){
										 checkItem.put("backgroundColorInt", item.get("backgroundColorInt"));
									 }
									 
									 // j == x LEFT
									 if( _lB.containsKey("borderType") )
									 {
										 checkItem = xySetArray.get(_iY).get(x);
										 HashMap<String, Object> newBorder = new HashMap<String, Object>();
										 newBorder.put("L", _lB);
										 checkItem.put("uBorder", newBorder);
									 }
								 }
								 
								 // Right
								 checkItem = xySetArray.get(_iY).get(x + (_defCol -1));
								 t = String.valueOf(checkItem.get("type"));
								 if( t.equals("D") || t.equals("item") )
								 {
								 }
								 else
								 {
									// background color 추가
									 if( isValid(item.get("backgroundColorInt")) ){
										 checkItem.put("backgroundColorInt", item.get("backgroundColorInt"));
									 }
									 
									 if( _rB.containsKey("borderType") )
									 {
										 HashMap<String, Object> newBorder = new HashMap<String, Object>();
										 newBorder.put("R", _rB);
										 checkItem.put("uBorder", newBorder);
									 }
								 }
							 }
						 }// _defRow - 0 For
					 }
				 }// uBorder
					 
			}// if sizeAr.size() > 0
			
		}//newItemList For

	}

	/**
	 * <pre>
	 * 겹친 셀의 테두리 스타일 지정을 위해 기존 테두리 정보를 저장할 uBorder라는 속성을 세팅.
	 * 
	 * EX) {uBorder={T={borderColorInt=0, borderType=solid}, L={borderColorInt=0, borderType=solid}}
	 * 
	 * <b>Reference</b>
	 * GS_UBIViewlib.src.ubstorm.ubExport.ExportExcel.ExportExcelParser.as
	 * getborderCheck()
	 * </pre>
	 *  */
	private HashMap<String, Object> getborderCheck( HashMap<String, Object> _item ){
		HashMap<String, Object> _border = new HashMap<String, Object>();
		
//		if( _item.containsKey("className") && _item.get("className").equals("UBLabelBorder") )
//		{
//			HashMap<String, Object> borderProps = new HashMap<String, Object>();
//			
//			if( _item.containsKey("borderColorInt") ){
//				borderProps.put("borderColorInt", _item.get("borderColorInt"));
//			}
//			if( _item.containsKey("borderType") ){
//				short bType = 0;
//				if( _item.containsKey("borderWidth") ){
//					bType = getBorderTypeIdx(String.valueOf(_item.get("borderType")), Integer.valueOf(String.valueOf(_item.get("borderWidth"))));
//				}
//				else{
//					bType = getBorderTypeIdx(String.valueOf(_item.get("borderType")), 1);
//				}
//				borderProps.put("borderType",bType);
//			}
//			
//			if( borderProps.size() > 0 ){
//				_border.put("L", borderProps.clone());
//				_border.put("T", borderProps.clone());
//				_border.put("R", borderProps.clone());
//				_border.put("B", borderProps.clone());
//			}
//		}
//		
		if( _item.containsKey("className") && _item.get("className").equals("UBTextArea") )
		{
			_border = null;
		}
		else if( _item.containsKey("borderSide") == false || _item.get("borderSide").equals("") )
		{
			HashMap<String, Object> borderProps = new HashMap<String, Object>();
			
			if( _item.containsKey("borderColorInt") ){
				borderProps.put("borderColorInt", _item.get("borderColorInt"));
			}
			if( _item.containsKey("borderType") ){
				short bType = 0;
				if( _item.containsKey("borderWidth") ){
					bType = getBorderTypeIdx(String.valueOf(_item.get("borderType")), Integer.valueOf(String.valueOf(_item.get("borderWidth"))));
				}
				else{
					bType = getBorderTypeIdx(String.valueOf(_item.get("borderType")), 1);
				}
				borderProps.put("borderType",bType);
			}
			
			if( borderProps.size() > 0 ){
				_border.put("L", borderProps.clone());
				_border.put("T", borderProps.clone());
				_border.put("R", borderProps.clone());
				_border.put("B", borderProps.clone());
			}
		}
		else{
			boolean isCell = false;
			if( isValid(_item.get("isCell")) ){
				isCell = ValueConverter.getBoolean(_item.get("isCell"));
			}
			
			// borderSide 로 값 매핑.
			String[] a = jsonArrayToArray(String.valueOf(_item.get("borderSide")));
			int aLen = ( a == null )? 0 : a.length;
			if( aLen == 0 )
			{
				_border = null;
			}
			else if( aLen > 4 )
			{
				_border = null;
			}
			else
			{
				// color, type, width 가 배열.
				if( isCell ){
					// border color int 배열 생성.
					ArrayList<String> bColorStrAr = new ArrayList<String>();
					if( _item.containsKey("borderColorsInt") ){
						bColorStrAr = new ArrayList<String>( Arrays.asList( jsonArrayToArray(String.valueOf(_item.get("borderColorsInt"))) ) );
					}
					
					// border type+width 로 구한 Excel Border Style 배열 생성.
					ArrayList<String> bTypeStr = new ArrayList<String>();
					if( _item.containsKey("borderTypes") ){
						String [] bWidths = null;
						//borderWidths
						if( _item.containsKey("borderWidths") ){
							bWidths = jsonArrayToArray(String.valueOf(_item.get("borderWidths")));
						}
						
						String [] bTypes = jsonArrayToArray( String.valueOf(_item.get("borderTypes")) );
						if( bTypes != null ){
							int len = bTypes.length;
							for (int i = 0; i < len; i++) {
								// width 값이 있으면 넣고,
								if( bWidths != null && bWidths.length > i ){
									bTypeStr.add(String.valueOf(getBorderTypeIdx( bTypes[i], Integer.valueOf(bWidths[i]))));
								}
								// 없으면 1로
								else{
									bTypeStr.add(String.valueOf(getBorderTypeIdx( bTypes[i], 1)));
								}
							}
						}
					}
					// 값 매칭.
					for( int i = 0; i < aLen; i++)
					{
						String _side = a[i];
						if( _side != "none" )
						{
							HashMap<String, Object> borderProps = new HashMap<String, Object>();
							
							if( bColorStrAr.size() > 0 && bColorStrAr.size() >= i ){
								borderProps.put("borderColorInt", bColorStrAr.get(i));
							}
							if( bTypeStr.size() > 0 && bTypeStr.size() >= i ){
								borderProps.put("borderType", bTypeStr.get(i));
							}
							
							if( borderProps.size() > 0 ){
								if( _side.equals("left") )				_border.put("L", borderProps);
								else if( _side.equals("top") ) 		_border.put("T", borderProps);
								else if( _side.equals("right") )		_border.put("R", borderProps);
								else if( _side.equals("bottom") )	_border.put("B", borderProps);
							}
						}
						else
						{
							_border = null;
						}
					}// a For
					
				}
				
				// isCell = false. color, type, width 가 단일 값.
				else
				{
					HashMap<String, Object> borderProps = new HashMap<String, Object>();
					
					if( _item.containsKey("borderColorInt") ){
						borderProps.put("borderColorInt", _item.get("borderColorInt"));
					}
					if( _item.containsKey("borderType") ){
						short bType = 0;
						if( _item.containsKey("borderWidth") ){
							bType = getBorderTypeIdx(String.valueOf(_item.get("borderType")), ValueConverter.getInteger(_item.get("borderWidth")) );
						}
						else{
							bType = getBorderTypeIdx(String.valueOf(_item.get("borderType")), 1);
						}
						borderProps.put("borderType",bType);
					}
					// 값 매칭.
					for( int i = 0; i < aLen; i++)
					{
						String _side = a[i];
						if( _side == "none" ){
							_border = null;
						}
						else
						{
							if( borderProps.size() > 0 ){
								if( _side.equals("left") )				_border.put("L", borderProps);
								else if( _side.equals("top") ) 		_border.put("T", borderProps);
								else if( _side.equals("right") )		_border.put("R", borderProps);
								else if( _side.equals("bottom") )	_border.put("B", borderProps);
							}
						}
					}// a For
					
				}// isCell=false
				
			}// set border side

		}// other class

		
		// 세팅한 border 값이 있는 경우, item 을 리턴.
		if( _border != null && _border.size() > 0){
			_item.put("uBorder", _border);
			return _item;
		}
		
		return null;
	}

	/** 
	 * <pre>
	 * 
	 * [ xySetArray 가 너무 커져서, 변경됨. 현재 사용 안함. ]
	 * 
	 * 
	 * 아이템의 x, width 값으로 X 좌표 배열(xArray), 아이템의 y, height 값으로 Y 좌표 배열(yArray)을 생성.
	 * 
	 *<b>xPos_ref</b>
	 * GS_UBIViewlib.src.component.popup.preview.PreviewPageModuleAS.exportHtmlProcess.as
	 * getAllItemsToXPosition()
	 *<b>yPos_ref</b>
	 * GS_UBIViewlib.src.ubstorm.ubExport.ExportExcel.ExportExcelParser.as
	 * tableObjectInit()
	 * 
	 *  X,Y배열의 값으로 item위치에 해당 Item으로 변경 한다, 이때 type은 item으로 변경 한다.
	 * 
	 * _nTdAr.push( {'width':_w , 'height':_h , 'type':'none'} );
	 * 
	 * GS_UBIViewlib.src.ubstorm.ubExport.ExportExcel.ExportExcelParser.as
	 * tableObjectInit()
	 * </pre>
	 * */
	private void makePositionArray() {
		
		// make Excel Table Array
		ArrayList<Integer> xArray = new ArrayList<Integer>();
		ArrayList<Integer> yArray = new ArrayList<Integer>();
	
		
		if( itemPropList == null ){
			log.error(getClass().getName() + "::" + "makeEachPosArray::" + "item Array is null." );
			return;
		}

		// 아이템 속성의 x, y 값을 각각 저장하는 배열 생성.
		log.info(getClass().getName() + "::" + "makeEachPosArray::" + "make Array...");

		xArray.add(0);
		for (ArrayList<HashMap<String, Object>> mAr : itemPropList) {
			
			// 페이지  y 시작점을 위하여.
			int bPageH = toNum(mAr.get(mAr.size()-1).get("bPageHeight"));
			if( !yArray.contains( bPageH ) ) yArray.add(bPageH);
			
//			String _className = "";
			
			for (HashMap<String, Object> item : mAr) {
				
				/*
				// Check item's type
				//if( item.get("type") == null ) continue;
				
				if( item.containsKey("className") == false ) continue;
	
				_className = (String) item.get("className");
				*/
				
				// visible false인 아이템은 패스!
				if( !ValueConverter.getBoolean(item.get("visible")) ) continue;
				
				//--------------------------------------X Array--------------------------------------//
				if( isValid(item.get("x")) && isValid(item.get("width")) ){
					
					Integer x = toNum( item.get("x") );
					Integer w= toNum( item.get("width") );
					
					if( !xArray.contains( x ) )	xArray.add(x);
					if( !xArray.contains( x+w ) )	xArray.add(x+w);
					
				}
				//--------------------------------------Y Array--------------------------------------//
				if( isValid(item.get("y")) && isValid(item.get("height"))  /*&&
						item.get("bPageHeight") != null || item.get("height").toString().equals("")*/ ){
					
					Integer y = toNum( item.get("y") ) + bPageH;
					Integer h = toNum( item.get("height") );
	
					if( !yArray.contains( y ) ) yArray.add(y);
					if( !yArray.contains( y+h ) ) yArray.add(y+h);
					
				}
			}
		}
		
		Collections.sort(xArray);
		Collections.sort(yArray);
		
		// X, Y 값을 정렬하여, 2차원 배열 구조의 xySetArray 생성.
		
		log.info(getClass().getName() + "::" + "makePositionArray::" + "makePositionArray...");
		
		xySetArray = new ArrayList<ArrayList<HashMap<String, Object>>>();
		
		int yArLen = yArray.size();
		for (int y = 0; y < yArLen-1; y++) {
			
			int h = yArray.get(y+1) - yArray.get(y);
			
			ArrayList< HashMap<String, Object>> xSetArray = new ArrayList<HashMap<String, Object>>();
			int xArLen = xArray.size();
			for (int _x = 0; _x < xArLen-1; _x++) {
				int w = xArray.get(_x+1) - xArray.get(_x);
				
				HashMap<String, Object> tmp = new HashMap<String, Object>();
				tmp.put("width", w);
				tmp.put("height", h);
				tmp.put("type", "none");
				
				xSetArray.add( tmp );
			}
			
			xySetArray.add(xSetArray);
		}
		
		log.info(getClass().getName() + "::" + "makePositionArray::" + "Making Position Array is complete!");

//		mappingItemPosition( xArray, yArray );
	}

	
	//-------------------------------공통 메서드-------------------------------//
	/**
	 * Floor( Object to String to Double ) to Integer
	 * @param o : numberValue
	 * @return Integer number 
	 * */
	protected int toNum( Object o ){
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
//				oVal = oVal.substring(0, oVal.indexOf("."));
				return (int) Math.round(Double.parseDouble(oVal));
			}
			else{
				return Integer.valueOf(oVal);
			}
		}
		
//		return -1;
	}

	/**
	 * <pre>
	 * 폰트사이즈계산용
	 * convert to pt value
	 * 1 px = 0.75pt
	 * 
	 * float f = 0.55555f
	 * DecimalFormat format = new DecimalFormat(".##");
	 * String str = format.format(f);
	 * 
	 * </pre>
	 * @param o : numberValue
	 * @return double pt 
	 * */
	protected double toPoint( Object o ){
		
		double val = -1;
		if( o == null ){
			return val;
		}
		else if( o instanceof Integer){
			val = (Integer) o;
		}
		else if( o instanceof Double){
			val = (Double) o;
		}
		else if( o instanceof Float){
			val = (Float) o;
		}
		else{
			String oVal = String.valueOf(o).trim();
			if( oVal.equals("") ) return -1;
			val = Double.parseDouble(oVal);
		}
//		return Math.floor(val)*0.75 * mRatio;
		return Math.floor(val*0.75) * mRatio;
//		return Math.floor(val*0.75 * mRatio);
	}

	/**
	 * json array 를 String[]으로 변환.
	 * @param json array (ex : [top, left, right, bottom] )
	 * @return String[]
	 * */
	protected String[] jsonArrayToArray( String jsonArray ){
		if( jsonArray == null ) return null;

		jsonArray = jsonArray.replace("[", "");
		jsonArray = jsonArray.replace("]", "");
		jsonArray = jsonArray.replaceAll(" ", "");
		
		jsonArray = jsonArray.trim();
		return jsonArray.split(",");
	}
	
	/**
	 * String 배열을 ,로 연결된 문자열로 변환.
	 * @param strAr String array
	 * @return {String} ex) "apple,banana,grape" 
	 * */
	protected String arrayToString( String[] strAr ){
		String s = "";
		
		if( strAr == null ) return "";
		
		for (int i = 0; i < strAr.length; i++) {
			s += strAr[i];
			if ( i != strAr.length-1 ) s += ",";
		}
		return s;
	}
	
	
	protected boolean isValid( Object item) {
		if( item == null ){
			return false;
		}
		String val = ValueConverter.getString(item).trim();
		if( val.equals("") || val.equals("null")){
			return false;
		}
		
		return true;
	}
	protected boolean isValid( String item ) {
		if( item == null ){
			return false;
		}
		item = item.trim();
		if( item.equals("") || item.equals("null")){
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * borderType 속성의 문자열을 Cell Style index 로 변환.
	 * @param borderType 속성값.
	 * @return short borderTypeIdx 
	 * */
	protected short getBorderTypeIdx( String borderType , int borderThick ){
		
		if( borderType == null || borderType.equals("") || borderThick <= 0){
			borderType = "none";
		}
		//double 이 예약어라 enum에서 dbl로 사용.
		if( borderType.equals("double") ){
			borderType = "dbl";
		}
		// thick의 굵기를 반영하여 타입지정.
		else if( borderType.equals("solid") ){
			if( borderThick > 3) borderType = "thick";
			else if( borderThick > 1) borderType = "medium";
		}
		else if( borderType.equals("dash") ){
			if( borderThick > 1) borderType = "m_dash";
		}
		else if( borderType.equals("dash_dot") ){
			if( borderThick > 1) borderType = "m_dash_dot";
		}
		else if( borderType.equals("dash_dot_dot") ){
			if( borderThick > 1) borderType = "m_dash_dot_dot";
		}
		
		if( borderType.indexOf("_share") != -1 ) borderType = borderType.replace("_share", "");
		
		short borderTypeIdx = CellStyle.BORDER_NONE;
		
		try {
			switch (EBorderType.valueOf(borderType)) {
			case solid: borderTypeIdx = CellStyle.BORDER_THIN; break;
			case dot: borderTypeIdx = CellStyle.BORDER_DOTTED;  break;
			case dash: borderTypeIdx = CellStyle.BORDER_DASHED; break;
			case dash_dot: borderTypeIdx = CellStyle.BORDER_DASH_DOT; break;
			case dash_dot_dot: borderTypeIdx = CellStyle.BORDER_DASH_DOT_DOT; break;
			case SOLD: borderTypeIdx = CellStyle.BORDER_THIN; break;
			case dbl: borderTypeIdx = CellStyle.BORDER_DOUBLE; break;
			case thick: borderTypeIdx = CellStyle.BORDER_THICK; break;
			case medium: borderTypeIdx = CellStyle.BORDER_MEDIUM; break;
			case m_dash: borderTypeIdx = CellStyle.BORDER_MEDIUM_DASHED; break;
			case m_dash_dot: borderTypeIdx = CellStyle.BORDER_MEDIUM_DASH_DOT; break;
			case m_dash_dot_dot: borderTypeIdx = CellStyle.BORDER_MEDIUM_DASH_DOT_DOT; break;
			case none: borderTypeIdx = CellStyle.BORDER_NONE; break;
			}
		} catch (IllegalArgumentException e) {
			log.error(getClass().getName()+"::getBorderTypeIdx::"+"borderType argument is wrong. >>> " + borderType);
		}
		return borderTypeIdx;
	}
	
	/**
	 * Int color to RGB byte array. (ubFormToPdf 참고함.)
	 * @return byteArray[3] = { 'r' , 'g' , 'b' }
	 * */
	public byte[] changeColorToByteAr(int _color)
	{
		java.awt.Color _c = new java.awt.Color(_color);
		byte[] color = {(byte) _c.getRed(), (byte) _c.getGreen(),(byte) _c.getBlue()};
		return color;
	}
	public java.awt.Color changeIntToColor(int _color)
	{
		java.awt.Color _c = new java.awt.Color(_color);
		return _c;
	}
	
	public boolean repeatValueCheck()
	{
		
		if( mRepeatValueCheckMap != null )
		{
			
	        // 방법1
	        Iterator<String> keys = mRepeatValueCheckMap.keySet().iterator();
	        while( keys.hasNext() ){
	            String key = keys.next();
	            
	            ArrayList<CellRangeAddress> _cellRanges = (ArrayList<CellRangeAddress>) mRepeatValueCheckMap.get(key);
	            int _cell_cellRangesSize = _cellRanges.size();
	            for (int i = 0; i < _cell_cellRangesSize; i++) {
	            	if( _cellRanges.get(i).getLastColumn() - _cellRanges.get(i).getFirstColumn() > 0 || _cellRanges.get(i).getLastRow() - _cellRanges.get(i).getFirstRow() > 0  )
	            	{
//	            		_cellRanges.get(i).setLastColumn(_cellRanges.get(i).getFirstColumn());
//	            		sheet.addMergedRegion( _cellRanges.get(i) );
	            		sheet.addMergedRegionUnsafe(_cellRanges.get(i));
	            	}
				}
	            
	        }
			
		}
		
		mLastRepeatValueStringMap = null;
		mRepeatValueCheckMap = null;
		
		return true;
	}
	
	/**
	 * 셀병합 정보가 담긴 배열을 이용하여 셀 병합처리 
	 * @return
	 */
	public boolean mergedCellList()
	{
		
		if( mCellRangeAddressList != null )
		{
			int _size = mCellRangeAddressList.size();
			
			for (int i = 0; i < _size; i++) {
            	sheet.addMergedRegionUnsafe( mCellRangeAddressList.get(i) );
            		
			}
			
		}
		
		mCellRangeAddressList = null;
		
		return true;
	}
	
	
	/**
	 * functionName : getDocumentXArray
	 * @return [Arraylsit:elementList(page의 item Element들이 담긴 리스트), ArrayList: pageMarginList ( ConnectLinked 일경우 각 페이지의 X좌표 값을 담아두는 배열 ) ]
	 */
	public ArrayList<Integer> getDocumentXArrayOnePage( ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList, HashMap<String, BandInfoMapData> bandInfo , HashMap<String, ArrayList<HashMap<String, Object>>> _dataSet, ArrayList<Integer> _xArr )
	{
		HashMap<String, ArrayList<HashMap<String, Object>>> _currentDataSet = null;
		ArrayList<Integer> _retXarray = new ArrayList<Integer>();
		
		ArrayList<Integer> _xAr = new ArrayList<Integer>();
		if( _xArr != null && _xArr.size() > 0 ) _xAr = _xArr;
		
		_retXarray =  makeXArrayBandList(pagesRowList, bandInfo, 0f, _xAr , _dataSet );
		
		return _retXarray;
	}
	
	/**
	 * <pre>
	 * XML 정보에서 x 좌표를 뽑아 xArrayGlobal 에 저장한다.
	 * For Excel Export : 페이지 별 분할전송으로 변경되면서 X좌표 계산 부분이 중복되어 이 방식으로 변경함.
	 * 2015-12-02
	 * </pre>
	 * */
	private ArrayList<Integer> makeXArrayBandList( ArrayList<ArrayList<HashMap<String, Value>>> pagesRowList, HashMap<String, BandInfoMapData> bandInfo ,Float _pageMarginX, ArrayList<Integer> mXArr, HashMap<String, ArrayList<HashMap<String, Object>>> _currentDataSet ) {

		ArrayList<Integer> xAr = new ArrayList<Integer>();
		ArrayList<Float> xArFloat = new ArrayList<Float>();
		ArrayList<Integer> cloneXAr = new ArrayList<Integer>();
		
		if(mXArr.size() > 0)
		{
			xAr = mXArr;
			if(xAr.contains(0) == false )xAr.add(0);
		}
		else
		{
			xAr.add(0);
			xArFloat.add(0f);
		}
		
		float _pageXPosition = 0f;
		int _cloneCountArgo = 1;
		boolean _isConnect = false;
		
		HashMap<String, Value> currentBandCntMap;
		String bandName = "";
		ArrayList<HashMap<String, Value>> _child = null;
		
		int itemLen = 0;
		int _bandSize = 0;
		int _pageSize = pagesRowList.size();
		float _pageW = 0;
		
		ArrayList<Integer> _xAr = new ArrayList<Integer>();
		ArrayList<Float> _xArOri = new ArrayList<Float>();
		
        Iterator<String> keys = bandInfo.keySet().iterator();
        while( keys.hasNext() ){
            String key = keys.next();
            
            BandInfoMapData _currentBandInfo = bandInfo.get(key);
            
            // 밴드 내부의 아이템을 가져와서 x, width를 이용하여 x배열을 생성
            if( _currentBandInfo.getChildren() != null && _currentBandInfo.getChildren().size() > 0 )
            {
            	_child = _currentBandInfo.getChildren();
            	_pageW = _currentBandInfo.getWidth();
            	
            	for (HashMap<String, Value> _childItem : _child) {
					
    				int xVal = -1;
    				int widthVal = -1;
    				int dotIdx = -1;
    				float xValFloat = -1;
    				float widthValFloat = -1;
    				
    				int tbBandCnt = 1;
    				float tbBandWidth = 0;
    				boolean _useBand = true;
    				
    				String propValX =  _childItem.get("x").getStringValue();
    				String propValW =  _childItem.get("width").getStringValue();
    				
					dotIdx = propValX.indexOf(".");
					if( dotIdx != -1 ){
						xVal = (int) (Math.round(Double.parseDouble( propValX )) + _pageXPosition);
					}
					else{
						xVal = (int) Math.round( Float.valueOf( propValX ) + _pageXPosition );
					}
					
					xValFloat = Float.valueOf( propValX ) + _pageXPosition;
					
					
					dotIdx = propValW.indexOf(".");
					if( dotIdx != -1 ){
						widthVal = (int)Math.round(Double.parseDouble(propValW));
					}
					else{
						widthVal = Integer.valueOf(propValW);
					}
					
					widthValFloat = Float.valueOf(propValW);
					
					tbBandCnt = _currentBandInfo.getLabelBandColCount();
					tbBandWidth = _currentBandInfo.getLabelBandDisplayWidth();
    				
    				if( xVal != -1 && widthVal != -1 ){
    					if( !_xAr.contains(xVal) )	_xAr.add(xVal);
    					if( !_xAr.contains(xVal+widthVal) )	_xAr.add(xVal+widthVal);
    					
    					if( !_xArOri.contains( xValFloat) )	_xArOri.add( xValFloat );
    					if( !_xArOri.contains( xValFloat + widthVal ) )	_xArOri.add( xValFloat + widthVal );		
    				}
    				
    				if(tbBandCnt > 1)
    				{	
    					int _addW = 0;
    					if( tbBandWidth <= 0 ) tbBandWidth = _pageW;
    					
    					for (int _bandRep = 1; _bandRep < tbBandCnt+1; _bandRep++) {
    						
    						_addW = Float.valueOf( (float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep) ).intValue();
    						
    						if( !_xAr.contains(xVal+(_addW)) )	_xAr.add(xVal+(_addW));
    						if( !_xAr.contains(xVal+widthVal+(_addW)) )	_xAr.add(xVal+widthVal+(_addW));

    						if( !_xArOri.contains( xValFloat+(_addW)) )	_xArOri.add( xValFloat+(_addW) );
        					if( !_xArOri.contains( xValFloat + widthVal+(_addW) ) )	_xArOri.add( xValFloat+widthVal+(_addW) );		
    						
    					}
    				}
            		
            		
				}
            	
            }
            
        }
		
		// 페이지 별 x 배열을 누적시킨다.
		int xLen = _xAr.size();
		for (int i = 0; i < xLen; i++) {
			int _xVal = _xAr.get(i);
			if( !xAr.contains(_xVal) )	xAr.add(_xVal);
			
			xArFloat.add(_xArOri.get(i));
		}
		
		xLen = _xArOri.size();
		for (int i = 0; i < xLen; i++) {
			float _xValF = _xArOri.get(i);
			if( !xArFloat.contains(_xValF) )	xArFloat.add(_xValF);
		}
		
		Collections.sort(xAr);

		return xAr;
	}
	
	
	/**
	 * functionName : getRatioPixel2WidthUnits
	 * desc			: 원본의 픽셀 사이즈에 비율을 이용하여 변환된 사이즈를 리턴
	 * @param _size
	 * @return
	 */
	protected int getRatioPixel2WidthUnits( int _size )
	{
		int _resultInt = _size;
		
		_resultInt = Float.valueOf( _resultInt * mRatio ).intValue(); 
		if( _resultInt == 0 ) _resultInt = 1;
		
		int pixelInt = PixelUtil.pixel2WidthUnits( _resultInt );
		
		return pixelInt;
	}
	
	
	protected float convertRatioHeight( Object _value, boolean _isItemFlag, HashMap<String, Object> _item )
	{
		float cHeight = 0;
		
		if(_isItemFlag)
		{
			cHeight = Double.valueOf( toNum(_value)*0.75 ).floatValue();
		}
		else
		{
			cHeight = (float) Math.min(toNum(_value)*0.75, 409);
		}
		
		cHeight = cHeight * mRatio;
		
		return cHeight;
	}
	
	ArrayList<HashMap<String, String>> mConvertRowHeight = new ArrayList<HashMap<String, String>>();
	
	protected void checkFitRowHeight( HashMap<String, Object> _item , int _startRowIdx, int _lastRowIdx, String _cellValue )
	{
		if( mAutoHeightSize && _item != null  )
		{
			float _result = setRowHeight( Float.valueOf(_item.get("width").toString()), Float.valueOf(_item.get("fontSize").toString()), _item.get("fontWeight").toString(),_item.get("fontFamily").toString(), _cellValue );
			
			if( _result > -1 && _result > Float.valueOf(_item.get("height").toString() ) )
			{
				_result = Double.valueOf( toNum(_result)*0.75 ).floatValue();
				
				HashMap<String, String> _convertRowItem = new HashMap<String, String>();
				_convertRowItem.put("CONVERT_HEIGHT", String.valueOf( _result ) );
				_convertRowItem.put("START_ROW", String.valueOf(_startRowIdx) );
				_convertRowItem.put("END_ROW", String.valueOf(_lastRowIdx) );
				
				mConvertRowHeight.add(_convertRowItem);
			}
		}
		
	}
	
	
	public void setSheetName( String _sheetName )
	{
		mSheetName = _sheetName;
	}
	

	
	private float getFontSize( float _width, float _height, float _fontSize, String _fontWeight, String _fontFamily,  String _cellValue )
	{
		HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
		
		optionMap.put("width", new float[]{ _width });
		optionMap.put("height", new float[]{ -1,-1 });
		optionMap.put("fontSize", new float[]{_fontSize});
		optionMap.put("lineHeight", new float[]{(float) 1.28});
		optionMap.put("padding", new float[]{3});
		
		HashMap<String,Object> resultMap;
		ArrayList<Object> _resultTextAr;
		ArrayList<Float> _resultHeightAr;
		
		resultMap = StringUtil.getSplitCharacter( _cellValue, optionMap, 
				_fontWeight, 
				_fontFamily , 
				_fontSize,
				-1);
		
		_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
		_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
		
		int _lineCnt = _resultTextAr.get(0).toString().split("\n").length;
		
		// 아이템의 height에 몃개의 line가 들어갈수 있는지 확인하여 총 라인수에 맞춰서 아이템의 fontSize를 변경
//		int _lineCnt = _resultTextAr.get(0).toString().split("\n").length;
		float _resultH = _resultHeightAr.get(0).floatValue();
		float _fontSz = _fontSize;
		
		if( _lineCnt  < 2 || _resultH <= _height ) return _fontSize;
		
		while( _resultH > _height )
		{
			_fontSize = _fontSize - Float.valueOf("0.2");
//			_fontSize = _fontSize - 1;
			
			optionMap.put("fontSize", new float[]{_fontSize});
			
			resultMap = StringUtil.getSplitCharacter( _cellValue, optionMap, 
					_fontWeight, 
					_fontFamily , 
					_fontSize,
					-1);
			
			_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
			_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
			
			if( _fontSize < 8 ) break;
			
			if( _fontSz > _fontSize ) _fontSz = _fontSize;
			_resultH = _resultHeightAr.get(0).floatValue();
			
		}
		
		return _fontSz; 
	}
	
	protected HashMap<String, Object> convertFontSize( HashMap<String, Object> _item,  String _cellValue )
	{
		if( mAutoFontSize )
		{
			float _result = getFontSize( Float.valueOf(_item.get("width").toString()), Float.valueOf(_item.get("height").toString()),  Float.valueOf(_item.get("fontSize").toString()), _item.get("fontWeight").toString(),_item.get("fontFamily").toString(), _item.get("text").toString() );
			
			if( _result < Float.valueOf( _item.get("fontSize").toString() )  )
			{
				_item.put("fontSize", _result);
			}
		}
		
		return _item;
	}
	
	
	////////////////////////////TEST
	
	private float setRowHeight( float _width, float _fontSize, String _fontWeight, String _fontFamily,  String _cellValue )
	{
		HashMap<String, float[]> optionMap = new HashMap<String, float[]>();
		
		float _lineHeight = 1.3f;
		
		if( _fontSize > 10 ) _lineHeight = 1.4f;
		
		optionMap.put("width", new float[]{ _width });
		optionMap.put("height", new float[]{ -1,-1 });
		optionMap.put("fontSize", new float[]{_fontSize});
		optionMap.put("lineHeight", new float[]{_lineHeight});
		optionMap.put("padding", new float[]{3});
		
		HashMap<String,Object> resultMap;
		ArrayList<Object> _resultTextAr;
		ArrayList<Float> _resultHeightAr;
		
		if( _cellValue.split("\n").length < 2 ) return -1;
		
		resultMap = StringUtil.getSplitCharacter( _cellValue, optionMap, 
				_fontWeight, 
				_fontFamily , 
				_fontSize,
				-1);
		
		_resultTextAr = (ArrayList<Object>) resultMap.get("Text");
		_resultHeightAr = (ArrayList<Float>) resultMap.get("Height");
		
		return  _resultHeightAr.get(0); 
	}
	
	private void convertRowHeight()
	{
		HashMap<String, String> _rowItem;
		int _max = mConvertRowHeight.size();
		int _rowSt = 0;
		int _rowEnd = 0;
		float _currentTotalHeight = 0;
		float _convetTotalHeight = 0;
		float _addHeight = 0;
		
		float _allAddHeight = 0;
		
		for (int i = 0; i < _max; i++) {
			
			_currentTotalHeight = 0;
			_rowItem = mConvertRowHeight.get(i);
			
			_convetTotalHeight = Float.valueOf( _rowItem.get("CONVERT_HEIGHT"));
			
			_rowSt = Integer.valueOf( _rowItem.get("START_ROW") );
			_rowEnd = Integer.valueOf( _rowItem.get("END_ROW") ) + 1;
			
			for (int j = _rowSt; j < _rowEnd; j++) {
				if(  sheet.getRow(j) != null )	_currentTotalHeight = _currentTotalHeight +  sheet.getRow(j).getHeightInPoints();
			}
			if( _convetTotalHeight > _currentTotalHeight )
			{
				_addHeight = _convetTotalHeight - _currentTotalHeight;
				
				_allAddHeight = _allAddHeight + _addHeight;
						
				_addHeight = _addHeight / (_rowEnd - _rowSt);
				
				for (int j = _rowSt; j < _rowEnd; j++) {
					
					if(  sheet.getRow(j) != null )
					{
						float _currentH = sheet.getRow(j).getHeightInPoints();
						sheet.getRow(j).setHeightInPoints(_currentH + _addHeight);
					}
					
				}
			}
			
			_rowItem.clear();
		}
//		float _addWFl = _allAddHeight*Float.valueOf("0.66");
//		int _addW = PixelUtil.pixel2WidthUnits( Math.round( _addWFl )  );
//		sheet.setColumnWidth( xArrayGlobal.size()-2, Math.round( sheet.getColumnWidth(xArrayGlobal.size()-2) + _addW  ) );
//		mConvertRowHeight.clear();
		
		int _maxNum = sheet.getLastRowNum();
		int m = 0;
		float _rowH = 0;
		float _colW = 0;
		
		// 현재 page의 Row의 총 height값 구하기 
		for( m = rowStartIDX; m <_maxNum; m++ )
		{
			if(  sheet.getRow(m) != null )
			{
				_rowH = _rowH + (sheet.getRow(m).getHeightInPoints() / 72* 96);
			}
		}
		
		// 현재 page의 Column의 총 Width값 구하기 
		_maxNum = xArrayGlobal.size();
		for( m = 0; m < _maxNum-1; m++ )
		{
			_colW = _colW + sheet.getColumnWidthInPixels(m);
		}
		
		// 세로의 0.64배보다 width가 더 커야 한페이지에 인쇄가 됨 ( width가 더 작을경우 인쇄시 페이지가 더 출력이 됨 )
		// width를 넓혀줄때 좌우에 동일한 영역을 추가하여 width를 변경
		// 
		if( _rowH * 0.64 > _colW ) 
		{
			int _addW = PixelUtil.pixel2WidthUnits( Long.valueOf(Math.round( (_rowH * 0.64) - _colW )).intValue()  )/2;
			sheet.setColumnWidth( 0, Math.round( sheet.getColumnWidth(0) + _addW  ) );						//첫번재 컬럼의 columnWidth 증가 
			sheet.setColumnWidth( xArrayGlobal.size()-2, Math.round( sheet.getColumnWidth(xArrayGlobal.size()-2) + _addW  ) );	//마지막 컬럼의 columnWidth 증가
		}
		
		mConvertRowHeight.clear();
		
	}

	
	public String makeCellFormatter( String _formatterStr, String _formatValueStr , CellStyle _cellStyle )
	{
		//formatter 처리 
//		_colCell.setCellType( CellType.NUMERIC );
//		_colCell.setCellValue( Float.parseFloat(cellValue) );
//		cStyle.setDataFormat( createHelper.createDataFormat().getFormat("#,###.##") );
//		cStyle..setAlignment(CellStyle.ALIGN_RIGHT);
		CreationHelper createHelper = wb.getCreationHelper();
		
		String[] _formatterValues = _formatValueStr.split("§");
		String _formatStr = "";
		String _decimalStr = "";
		
		if( _formatValueStr.equals("") == false && _formatterValues.length > 0 )
		{
			if( "Currency".equals(_formatterStr))
			{
				// _nation  + "§" + _align;
//				_formatStr = "\""+_formatterValues[0] + "\"" + "#,##0";
//				_formatStr = "\"￦\"#,##0";
				if("right".equals(_formatterValues[1] ))_formatStr = "#,##0"+ convertCurrency(_formatterValues[0] ) +"_);(#,##0" + convertCurrency(_formatterValues[0]) + ")";
				else _formatStr = convertCurrency(_formatterValues[0] ) + "#,##0_);(" + convertCurrency(_formatterValues[0]) + "#,##0)";
				
			}
			else if( "Date".equals(_formatterStr))
			{
				//_formatString
				_formatStr = _formatterValues[0];
			}
			else if( "MaskNumber".equals(_formatterStr))
			{
				//_mask  + "§" + _decimalPointLength  + "§" + _useThousandComma  + "§" + _isDecimal
				
				int _decimalPointF = Integer.parseInt( _formatterValues[1] );
				boolean _isDecimalF = _formatterValues[3].equals("true")?true:false;
				boolean _useThouandCommapF = _formatterValues[2].equals("true")?true:false;
				
				//천단위 콤마 사용시 
				if( _useThouandCommapF )
				{
					_formatStr = "#,##0";
				}
				else
				{
					_formatStr = "0";
				}
				
				for( int i=0; i < _decimalPointF; i++ )
				{
					if( "".equals(_decimalStr) )
					{
						_decimalStr = ".";
					}
					_decimalStr = _decimalStr + "0";
				}
				
				if(  "".equals(_decimalStr) == false  ) _formatStr = _formatStr + _decimalStr;
				
				_formatStr = _formatStr +  "_ ";
				
				//_cellStyle.setAlignment(HorizontalAlignment.RIGHT);		// 숫자형 오른쪽 정렬 (?)
			}
			else if( "CustomDate".equals(_formatterStr) )
			{
				String _inputFormatStr = _formatterValues[0];
				String _outFormatStr = _formatterValues[1];
				
				//Excel 에서는 millisecond 는 SSS가 아니라 000으로 지정되어야 한다.
				_outFormatStr = _outFormatStr.replaceAll("S", "0");
				
				_formatStr = convertDateFormat(_outFormatStr);
			}
			if( _formatStr.equals("") == false ) _cellStyle.setDataFormat( createHelper.createDataFormat().getFormat( _formatStr ) );
			
		}
		
		return "";
	}
	
	private String convertDateFormat( String _dF )
	{
		// 포맷 형식의 \를 변경
		if( _dF.indexOf("\\") != -1 )
		{
			_dF = _dF.replace("\\", "\\\\");
		}
		
		// 포맷 형식의 /를 \/로 변경해야 엑셀에서 정상적으로 표현
		if( _dF.indexOf("/") != -1 )
		{
			_dF = _dF.replaceAll("/", "\\\\/");
		}
		
		return _dF;
	}
	
	private String convertCurrency(String _cur)
	{
		// Currency formatter사용시 원 표시 사용시 원문자 변경 
		if( _cur.equals("￦")) return "\\\\";
		
		return _cur;
	}
	
	public Cell setFormatType( HashMap<String, Object> _item,  Cell _cell, String cellValue )
	{
		String _formatType = "";
		
		if( _item.containsKey("EX_FORMATTER") ) 
		{
			_formatType = _item.get("EX_FORMATTER").toString();
		}
		
		if( "MaskNumber".equals(_formatType) || "Currency".equals(_formatType) )
		{
			try {
				_cell.setCellType( CellType.NUMERIC );
				_cell.setCellValue( new BigDecimal(  _item.get("EX_FORMAT_ORIGINAL_STR").toString() ).doubleValue() );
			} catch (Exception e) {
				if( isValid(cellValue) ){
					_cell.setCellValue(cellValue);
				}
			}
		}
		else if( "Date".equals(_formatType) && _item.get("EX_FORMAT_ORIGINAL_STR").toString().length() == 8 )
		{
			try {
				SimpleDateFormat _sdf = new SimpleDateFormat("yyyyMMdd");
				Date _date = _sdf.parse(_item.get("EX_FORMAT_ORIGINAL_STR").toString());
				_cell.setCellValue(_date);
				
				_sdf = null;
				
			} catch (Exception e) {
				if( isValid(cellValue) ){
					_cell.setCellValue(cellValue);
				}
			}
		}
		else if( "CustomDate".equals(_formatType) &&  _item.get("inputFormatString").toString().equals("") == false && _item.get("EX_FORMAT_ORIGINAL_STR").toString().equals("") == false )
		{
			
			
			try {
				
				SimpleDateFormat _sdf = new SimpleDateFormat( _item.get("inputFormatString").toString() );
				Date _date = _sdf.parse(_item.get("EX_FORMAT_ORIGINAL_STR").toString());
				
				String _dateForamt  = _item.get("outputFormatString").toString();
				
				// 입력된 날짜가 시간만 담겨있는지 확인( Time만 있을경우 년월일값을 제거하는 작업 필요 )
				if( _dateForamt.matches(".*[G,y,M,w,W,D,d,F,E].*") == false )
				{
					// 원본 날짜를 이용하여 Calendar를 생성
					Calendar _cal = Calendar.getInstance();
					_cal.setTimeZone( _sdf.getTimeZone() );
					_cal.setTimeInMillis(_date.getTime() + _sdf.getTimeZone().getRawOffset() );			//Date의 millisecond에 TimeZone의 offset값을 더하여 시간 생성
					
					// 생성한 Calendar에서 시분초를 0으로 변경하여 제거할 시간을 가져온다
					Calendar _tempCal = Calendar.getInstance();
					_tempCal.setTimeZone( _sdf.getTimeZone() );
					_tempCal.setTimeInMillis(0);
					
					// 1900년 또는 1904년을 사용시 true지정.
					boolean _use1904windowing = false;
					
					if( _cal.get(Calendar.YEAR) == 1900 || _cal.get(Calendar.YEAR) == 1904 )
					{
						_use1904windowing = true;
					}
					
					double _excelTimeOri = DateUtil.getExcelDate(_cal, _use1904windowing);
					double _excelTimeTemp = DateUtil.getExcelDate(_tempCal, _use1904windowing);
					
					double _timeData =  _excelTimeOri  - _excelTimeTemp;
					
					_cell.setCellValue( _timeData );
				}
				else
				{
					
					_cell.setCellValue(_date);
				}
				
				_sdf = null;
			} catch (Exception e) {
				if( isValid(cellValue) ){
					_cell.setCellValue(cellValue);
				}
			}
			
		}
		else
		{
			if( isValid(cellValue) ){
				
				if( org.apache.poi.util.StringUtil.hasMultibyte(cellValue) )
				{
					//log.debug("==============================	CELL TEXT :	" + cellValue   );
					_cell.setCellValue(cellValue);
				}
				else
				{
					_cell.setCellValue(cellValue);
				}
			}
		}
		
		return _cell;
	}
	
	public void createBackgroundImage() throws BadElementException, MalformedURLException, IOException
	{
		if( !mExcelIncludeImage )
		{
			mBackgroundImage = null;
			return;
		}
		
		if( this.mBackgroundImage != null && this.mBackgroundImage.isEmpty() ==false  )
		{
			String _imageUrl = "";
			
			if( mBackgroundImage.get("type").equals("base64"))
			{
				_imageUrl = URLEncoder.encode( mBackgroundImage.get("data").toString(), "UTF-8");
			}
			else
			{
				_imageUrl = mBackgroundImage.get("url").toString();
			}
			
			if( _imageUrl.equals("") || _imageUrl.equals("null") ){
				log.debug( getClass().getName()+"::addImage::"+">>>>>>>> Item's src property is not exist.");
				return;
			}
			
			byte[] bAr = null;
			if(_imageUrl != null)
			{
				int imgId = -1;
				//
				Boolean _hasDictionary=false;
				ImageDictionaryVO _newImgDictionary=null;
				
				ImageDictionaryVO _imgDictionary = null;
				if( mImageDictionary != null ){
					_imgDictionary=mImageDictionary.getDictionaryData( _imageUrl );
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
					imgId = Integer.parseInt(_imgDictionary.getmEmbedID());
					_image = _imgDictionary.getmPDFImage();
					
					if( mBackgroundInfoMap.containsKey(imgId))
					{
						mBackgroundImage = mBackgroundInfoMap.get(imgId);
					}
				}else{
					
					bAr = common.getBytesLocalImageFile(_imageUrl);				
					if(bAr == null ) return;
					imgId = wb.addPicture(bAr, Workbook.PICTURE_TYPE_PNG);
					_newImgDictionary =mImageDictionary.createPDFDictionaryData( _imageUrl ,null);
					_newImgDictionary.setmEmbedID(String.valueOf(imgId));	
					
					_image = Image.getInstance(bAr);
				}
				
				
				if( _image != null )
				{
					// 아이템 사이즈 정보로 이미지 사이즈 지정. Dimension 이용. 이 경우 resize 메서드를 호출하면 안된다.
					String itemW = String.valueOf( mBackgroundImage.get("pageWidth") );
					String itemH = String.valueOf( mBackgroundImage.get("pageHeight") );

					HashMap<String,Float> _orignSize = common.getOriginSize(itemW,itemH,_image);
					
					mBackgroundImage.put("width", _orignSize.get("width"));
					mBackgroundImage.put("height", _orignSize.get("height"));
					mBackgroundImage.put("top", _orignSize.get("marginY"));
					mBackgroundImage.put("left", _orignSize.get("marginX"));
					mBackgroundImage.put("widthRate", _orignSize.get("widthRate"));
					mBackgroundImage.put("heightRate", _orignSize.get("heightRate"));
					
					mBackgroundInfoMap.put( imgId, mBackgroundImage);
				}
				mBackgroundImage.put("imageID", imgId);
				
			}
			bAr = null;
		}
	}
	
	protected void printPageSetting()
	{
		ArrayList<HashMap<String, Object>> mAr = itemPropList.get(0);
			
		HashMap<String, Object> pageInfo = mAr.get(mAr.size()-1);
		int _pageWidth = toNum(pageInfo.get("cPWidth"));
		int _pageHeight = toNum(pageInfo.get("cPHeight"));
		boolean _isLandscape = false;
		
		short _pageSize = getExcelPrintPageSize(_pageWidth, _pageHeight );
		
		if( _pageWidth > _pageHeight )
		{
			_isLandscape = true;
		}
		
		if( sheet.getPrintSetup() instanceof XSSFPrintSetup    )
		{
			XSSFPrintSetup _xsPrintSetup = (XSSFPrintSetup) sheet.getPrintSetup();
			_xsPrintSetup.setLandscape(_isLandscape);
			
			_xsPrintSetup.setHeaderMargin(0);
			_xsPrintSetup.setFooterMargin(0);
			
			_xsPrintSetup.setPaperSize(_pageSize);
			
		    
		}
		else if( sheet.getPrintSetup() instanceof HSSFPrintSetup )
		{
			HSSFPrintSetup _xsPrintSetup = (HSSFPrintSetup) sheet.getPrintSetup();
			_xsPrintSetup.setLandscape(_isLandscape);
			
			_xsPrintSetup.setHeaderMargin(0);
			_xsPrintSetup.setFooterMargin(0);
			
			_xsPrintSetup.setPaperSize(_pageSize);
		}
		
		/*
			sheet.setMargin(sheet.LeftMargin, 0 );
			sheet.setMargin(sheet.RightMargin, 0 );
			sheet.setMargin(sheet.BottomMargin, 0 );
			sheet.setMargin(sheet.TopMargin, 0 );
		*/
		
	}
	
	protected short getExcelPrintPageSize( int _w, int _h )
	{
		if( _w == 1123 && _h == 794 )
		{
			return XSSFPrintSetup.A4_PAPERSIZE;
		}
		
		return XSSFPrintSetup.A4_PAPERSIZE;
	}
	
	
	////////json타입의 문서에서 xArr가져오기
	/**
	 * functionName : getDocumentXArray
	 * @return [Arraylsit:elementList(page의 item Element들이 담긴 리스트), ArrayList: pageMarginList ( ConnectLinked 일경우 각 페이지의 X좌표 값을 담아두는 배열 ) ]
	 */
	protected ArrayList<ArrayList<Integer>> getDocumentXArrayJS( ArrayList<ProjectInfo>  _projectInfos, HashMap<String, Object> _pageInfo )
	{
		int _docIdx = 0;
		int _docLength = 0;
		int _pageCnt = 0;
		int i = 0;
		int j = 0;
		int _docPagesSize = 0;
		HashMap<String, ArrayList<HashMap<String, Object>>> _currentDataSet = null;
		_docLength = _projectInfos.size();

		ArrayList<ArrayList<Integer>> _retXarray = new ArrayList<ArrayList<Integer>>();
		
		ArrayList<ArrayList<Integer>> _totXAr = null;
		ArrayList<Integer> _xAr = null;
		if( _pageInfo.containsKey("EXCEL_X_ARR_TOT") )
		{
			_totXAr = (ArrayList<ArrayList<Integer>>) _pageInfo.get("EXCEL_X_ARR_TOT");
		}
		else
		{
			_xAr = (ArrayList<Integer>) _pageInfo.get("EXCEL_X_ARR");
		}
		
		ProjectInfo _projectInfo;
		
		for( _docIdx = 0; _docIdx < _docLength; _docIdx++ )
		{
			ArrayList<Float> _pageMarginList = new ArrayList<Float>();
			_projectInfo = _projectInfos.get(_docIdx);
			if( _totXAr != null ){
				_xAr = _totXAr.get(_docIdx);
			}
			
			if( _projectInfo.getProjectType().equals("12") )
			{
				ArrayList<Object> _pageInfoData = (ArrayList<Object>) _pageInfo.get("PAGE_INFO_DATA_LIST");
				
				int _infoS = _pageInfoData.size();
				
				for (int k = 0; k < _infoS; k++) {
					
					ArrayList<HashMap<String, Object>> pagesInfoData = (ArrayList<HashMap<String, Object>>) ( (HashMap<String,Object>)_pageInfoData.get(k) ).get("pageInfoData");
					
					int _pageInfoSize = pagesInfoData.size();
					
					for ( i = 0; i < _pageInfoSize; i++) {
						HashMap<String, Object> _info = pagesInfoData.get(i);
						_docPagesSize = _projectInfo.getPageList().size();
						
						for ( j = 0; j < _docPagesSize; j++) {
							_pageMarginList.add( Float.valueOf( ((HashMap<String, Object>) _info.get("itemProperty")).get("x").toString() ) );
						}
					}
				}
			}
			else
			{
				_docPagesSize = _projectInfo.getPageList().size();
				
				for ( j = 0; j < _docPagesSize; j++) {
					_pageMarginList.add(0f);
				}
			}
			
			_retXarray.add( makeXArrayJS(_projectInfo, _pageMarginList, _xAr ) );
		}
		
		
		return _retXarray;
	}
	
	/**
	 * <pre>
	 * XML 정보에서 x 좌표를 뽑아 xArrayGlobal 에 저장한다.
	 * For Excel Export : 페이지 별 분할전송으로 변경되면서 X좌표 계산 부분이 중복되어 이 방식으로 변경함.
	 * 2015-12-02
	 * </pre>
	 * */
	private ArrayList<Integer> makeXArrayJS(ProjectInfo _projectInfo, ArrayList<Float> _pageMarginX, ArrayList<Integer> mXArr ) {

		log.info(getClass().getName() + "::" + "Call xmlToUbForm makeXArray()...");
		
		ArrayList<Integer> xAr = new ArrayList<Integer>();
		ArrayList<Float> xArFloat = new ArrayList<Float>();
		ArrayList<Integer> cloneXAr = new ArrayList<Integer>();
		
		if(mXArr.size() > 0)
		{
			xAr = mXArr;
			if(xAr.contains(0) == false )xAr.add(0);
		}
		else
		{
			xAr.add(0);
			xArFloat.add(0f);
		}
		
		PageInfo _pageInfo;
		int _pageCnt = _projectInfo.getPageList().size();
		float _pageXPosition = 0f;
		int _cloneCountArgo = 1;
		float _pageW = 0;
		
		for (int i2 = 0; i2 < _pageCnt; i2++) {
			
			_pageInfo = _projectInfo.getPageList().get(i2);
			
			_pageW = _pageInfo.getWidth();
			ArrayList<HashMap<String, Value>> _items = _pageInfo.getItems();
			int itemLen = _items.size();
			
			_pageXPosition = _pageMarginX.get(i2);
			ArrayList<Integer> _xAr = _pageInfo.getXArr();
			ArrayList<Float> _xArOri = new ArrayList<Float>();
			
			// label band 에 필요한 속성을 담을 맵.
			HashMap<String, Integer> labelBandProp = null;
			HashMap<String, HashMap<String, String>> bandData = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> bandInfo;
//			for (int i = 0; i < itemLen; i++) {
//				HashMap<String, Value> _item =  _items.get(i);
//				String className = _item.get("className").getStringValue();
//				
//				if(className.indexOf("Band") != -1)
//				{
//					if( "UBLabelBand".equals(className) )
//					{
//						labelBandProp = new HashMap<String, Integer>();
//						labelBandProp.put("columns", _item.get("columns").getIntValue());
//						labelBandProp.put("border_x", _item.get("border_x").getIntValue());
//						labelBandProp.put("border_width", _item.get("border_width").getIntValue());
//					}
//					continue;
//				}
//				
//				if( _item != null )
//				{
//					int tbBandCnt = -1;
//					float tbBandWidth = -1;
//					int xVal = -1;
//					int widthVal = -1;
//					int dotIdx = -1;
//					float xValFloat = -1;
//					float widthValFloat = -1;
//					xValFloat = _item.get("x").getIntegerValue();
//					widthValFloat = _item.get("width").getIntegerValue();
//					
//					xVal = (int) (Math.round(xValFloat) + _pageXPosition);
//					widthVal = (int)Math.round(widthValFloat);
//					
//					String _band = (_item.get("band")!=null)? _item.get("band").getStringValue():"";
//					BandInfoMapData _bandInfo = null;
//					
//					if( _pageInfo.getBandInfoData().containsKey(_band) )
//					{
//						_bandInfo = _pageInfo.getBandInfoData().get(_band);
//					}
//					
//					if( _bandInfo != null )
//					{
//						if( _bandInfo.getLabelBandColCount() >  1 )
//						{
//							tbBandCnt = _bandInfo.getLabelBandColCount();
//						}
//						
//						if( _bandInfo.getLabelBandDisplayWidth() >  1 )
//						{
//							tbBandWidth = _bandInfo.getLabelBandDisplayWidth() ;
//						}
//					}
//					if( xVal != -1 && widthVal != -1 ){
//						if( !_xAr.contains(xVal) )	_xAr.add(xVal);
//						if( !_xAr.contains(xVal+widthVal) )	_xAr.add(xVal+widthVal);
//						
//						if( !_xArOri.contains( xValFloat) )	_xArOri.add( xValFloat );
//						if( !_xArOri.contains( xValFloat + widthVal ) )	_xArOri.add( xValFloat + widthVal );		
//					}
//					
//					if(tbBandCnt > 1)
//					{	
//						int _addW = 0;
//						if( tbBandWidth <= 0 ) tbBandWidth = _pageW;
//						for (int _bandRep = 1; _bandRep < tbBandCnt+1; _bandRep++) {
//							
//							_addW = Float.valueOf( (float) Math.floor((tbBandWidth/tbBandCnt) * _bandRep) ).intValue();
//							
//							if( !_xAr.contains(xVal+(_addW)) )	_xAr.add(xVal+(_addW));
//							if( !_xAr.contains(xVal+widthVal+(_addW)) )	_xAr.add(xVal+widthVal+(_addW));
//							
//						}
//					}
//					
//					
//				}
//			}
				
//			if( _pageInfo.getReportType().equals(ProjectInfo.FORM_TYPE_LABELBAND) )
//			{
//				labelBandProp = new HashMap<String, Integer>();
//				labelBandProp.put("columns", _item.get("columns").getIntValue());
//				labelBandProp.put("border_x", _item.get("border_x").getIntValue());
//				labelBandProp.put("border_width", _item.get("border_width").getIntValue());
//			}
			
			int xLen = 0;
			// 문서가 라벨밴드라면,
			HashMap<String, Integer> _labelBandInfo = _pageInfo.getLabelBandInfo();
			if( _labelBandInfo != null && !_labelBandInfo.isEmpty() ){
				int columns = _labelBandInfo.get("columns");
				int labelWidth = _labelBandInfo.get("border_x") + _labelBandInfo.get("border_width");
				
				// 위에서 만든 x array 에 column 개수만큼 더해준다.
				xLen = _xAr.size();
				for (int i = 0; i < xLen; i++) {
					for (int j = 1; j < columns; j++) {
						int newPos = _xAr.get(i)+labelWidth*j;
						if( !_xAr.contains(newPos) )	_xAr.add(newPos);

						 float newPosFl = newPos;
						if(_xArOri.size() > i ) newPosFl = _xArOri.get(i)+labelWidth*j;
						if( !_xArOri.contains( newPosFl) )	_xArOri.add( newPosFl );
					}
				}
			}
			
			
			
			
			// 페이지 별 x 배열을 누적시킨다.
			xLen = _xAr.size();
			for (int i = 0; i < xLen; i++) {
				int _xVal = _xAr.get(i);
				if( !xAr.contains(_xVal) )	xAr.add(_xVal);
				
//				xArFloat.add(_xArOri.get(i));
				xArFloat.add( Integer.valueOf(_xAr.get(i)).floatValue() );
			}
			
			xLen = _xArOri.size();
			for (int i = 0; i < xLen; i++) {
				float _xValF = _xArOri.get(i);
				if( !xArFloat.contains(_xValF) )	xArFloat.add(_xValF);
			}
			
			if( !xAr.contains(Float.valueOf(_pageW).intValue()) )	xAr.add( Float.valueOf(_pageW).intValue() );
			if( !xArFloat.contains(_pageW) )	xArFloat.add(_pageW);
			
			// Clone Page값 담기
			String cloneData = _pageInfo.getClone();
			int _cloneColCnt = 1;
			
			if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) ||cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL)  || cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM))
			{
				if(  cloneData.equals(GlobalVariableData.CLONE_PAGE_HORIZONTAL) )
				{
					_cloneColCnt = 1;
				}
				else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_VERTICAL) )
				{
					_cloneColCnt = 2;
				}
				else if( cloneData.equals(GlobalVariableData.CLONE_PAGE_CUSTOM) )
				{
					_cloneColCnt = _pageInfo.getCloneColCount();
				}
			}
			
			if( _cloneColCnt > 1)
			{
				float pageWidth = _pageInfo.getWidth() / _cloneColCnt;
				
				int _size = xArFloat.size();
				
				for (int j = 0; j < _cloneColCnt; j++) {
					for (int i = 0; i < _size; i++) {
						int _itemX = Math.round((float) Math.round( ( xArFloat.get(i) + ( pageWidth * j ) )*100)/100 );
						if( cloneXAr.indexOf(_itemX) == -1 ) cloneXAr.add(_itemX);
					}
				}
				xAr = cloneXAr;
				_cloneCountArgo = _cloneColCnt;
			}
			
		}// page for
		
		Collections.sort(xAr);

		return xAr;
	}
	
	protected String CreateSVGLabelImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException, TranscoderException, JDOMException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
