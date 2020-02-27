package org.ubstorm.service.parser;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.ShapeTypes;
import org.apache.tools.ant.filters.StringInputStream;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.dml.Theme;
import org.docx4j.jaxb.Context;
import org.docx4j.model.properties.table.tr.TrHeight;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.sharedtypes.STOnOff;
import org.docx4j.vml.CTFill;
import org.docx4j.vml.CTOval;
import org.docx4j.vml.CTPath;
import org.docx4j.vml.CTRect;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTStroke;
import org.docx4j.vml.CTTextbox;
import org.docx4j.vml.ObjectFactory;
import org.docx4j.vml.STFillMethod;
import org.docx4j.vml.STFillType;
import org.docx4j.vml.STStrokeEndCap;
import org.docx4j.vml.STStrokeLineStyle;
import org.docx4j.vml.STTrueFalse;
import org.docx4j.vml.officedrawing.STConnectorType;
import org.docx4j.wml.BooleanDefaultFalse;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTBackground;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTHeight;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTSignedTwipsMeasure;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.CTTblLook;
import org.docx4j.wml.CTTblPPr;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.CTVerticalJc;
import org.docx4j.wml.Color;
import org.docx4j.wml.Fonts;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.PPrBase.TextAlignment;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.STThemeColor;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.Pict;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STHeightRule;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.STVerticalJc;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgBorders;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcMar;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.TcBorders;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.json.simple.JSONObject;
import org.ubstorm.service.dictionary.ImageDictionary;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.xmlToUbForm.EBorderType;
import org.ubstorm.service.parser.xmlToUbForm.EFontWeight;
import org.ubstorm.service.parser.xmlToUbForm.ETextAlign;
import org.ubstorm.service.parser.xmlToUbForm.ETextDecoration;
import org.ubstorm.service.parser.xmlToUbForm.EVerticalAlign;
import org.ubstorm.service.parser.queue.IQueue;
import org.ubstorm.service.parser.queue.JobQueue;
import org.ubstorm.service.parser.thread.JobConsumer;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;

import com.lowagie.text.DocumentException;
import com.lowagie.text.ImgTemplate;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfTemplate;
import com.oreilly.servlet.Base64Decoder;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class ubFormToWord {
	
	public static String SECTION_MARK_NEXTPAGE 		= "nextPage";
	public static String SECTION_MARK_NEXTCOLUMN 	= "nextColumn";
	public static String SECTION_MARK_CONTINUOUS 	= "continuous";
	public static String SECTION_MARK_EVENPAGE 		= "evenPage";
	public static String SECTION_MARK_ODDPAGE 		= "oddPage";
	
	
	private Logger log = Logger.getLogger(getClass());
	private ArrayList<ArrayList<HashMap<String, Object>>> itemPropList;
	
	protected static IQueue jobQueue = null;
	protected static ArrayList jobThreadList = null;
	Runtime runtime = Runtime.getRuntime();

	private int START_PAGE = 0;
	
	private ImageDictionary mImageDictionary;
	
	public static BigInteger BIGINTEGER_ZERO=BigInteger.valueOf( 0);
	long SPACING_LINE = 20;
	public void setSTART_PAGE(int sTART_PAGE) {
		START_PAGE = sTART_PAGE;
	}
	
	JSONObject mBackgroundImage;
	boolean mIsLast = false;
	String mArgoPageOrientation = "";
	String mPageOrientation = "PORTRAIT";
	String PAGE_ORIENTATION_PORTRAIT = "PORTRAIT";
	String PAGE_ORIENTATION_LANDSCAPE = "LANDSCAPE";
	float mpageWdith = 0;
	float mpageHeight = 0;
	String mPageBackgroundColor = "";
	
	public void setBackgroundImage(JSONObject _bgImage)
	{
		mBackgroundImage = _bgImage;
	}
	public void setBackgroundColor( int _value )
	{
		mPageBackgroundColor = changeColorToHex(_value);
	}
	
	public void setIsLast( boolean _value )
	{
		mIsLast = _value;
	}
	
	public void setPageSize( float _width, float _height )
	{
		mpageWdith = _width;
		mpageHeight = _height;
		
		if( mpageWdith > mpageHeight )
		{
			mPageOrientation = this.PAGE_ORIENTATION_LANDSCAPE;
		}
		else
		{
			mPageOrientation = this.PAGE_ORIENTATION_PORTRAIT;
		}
	}
	
	
	/* 
	 * http://www.docx4java.org/svn/docx4j/trunk/docx4j/docs/Docx4j_GettingStarted.html
	 * api>>>>
	 * http://grepcode.com/file/repo1.maven.org/maven2/org.docx4j/docx4j/3.2.1/org/docx4j/
	 * Sample Source
	 * https://github.com/plutext/docx4j/blob/master/src/samples/docx4j/org/docx4j/samples/CreateWordprocessingMLDocument.java#L60
	 * */
	
	public WordprocessingMLPackage xmlParsingWord(ArrayList<ArrayList<HashMap<String, Object>>> dataMap) throws Exception {

		log.info(getClass().getName() + "::" + "Call xmlParsingWord()...");
		
		if( dataMap == null || dataMap.size() == 0){ 
			log.error(getClass().getName() + "::" + ">>>>> dataMap parameter is null.");
			return null;
		}
		else if( wordMLPackage == null ){
			log.error(getClass().getName() + "::" + ">>>>> wordMLPackage is null.");
			return null;
		}
		
		itemPropList = dataMap;
//		if( wordMLPackage == null ) settingWord();
		
		//큐를 생성한다.
		jobQueue = JobQueue.getInstance();
		jobThreadList = new ArrayList();
		
		// 소비자 쓰레드를 생성하고 시작한다.
		for(int i=0; i< 10; i++)
		{
			Thread conThr = new Thread(new JobConsumer(jobQueue, Integer.toString(i)));
			// 데몸 스레드로 설정한다.
			if(!conThr.isDaemon()) conThr.setDaemon(true);
			conThr.start();
			
			jobThreadList.add(conThr);
		}

		Date start = new Date();
		WordprocessingMLPackage rtnPkg = toWord();
		Date end = new Date();
		long diff = end.getTime() - start.getTime();
		long diffSec = diff / 1000% 60;         
		long diffMin = diff / (60 * 1000)% 60;        
		log.debug(getClass().getName() + "::toWord::" + "toWord complete. [ "+diffMin +":"+diffSec+"." + diff%1000+" ]");
		
		for(int i=0; i< jobThreadList.size(); i++ )
		{
			Thread conThr = (Thread) jobThreadList.get(i);
			// 데몸 스레드로 설정한다.
			if(conThr.isAlive()) conThr.interrupt();
		}
		jobThreadList.clear();
		
		return rtnPkg;
	}

	private WordprocessingMLPackage wordMLPackage;
	private int pageNo=0;
	
	public WordprocessingMLPackage xmlParsingWord(ArrayList<ArrayList<HashMap<String, Object>>> dataMap,WordprocessingMLPackage wordPkg, int pNo) throws Exception {

		log.info(getClass().getName() + "::" + "Call xmlParsingWord()...");
		
//		if( pNo > 200 ) return wordPkg;
		
		if( dataMap == null || dataMap.size() == 0){ 
			log.error(getClass().getName() + "::" + "dataMap parameter is null.");
			return null;
		}
		
		itemPropList = dataMap;
		wordMLPackage = wordPkg;
		wordPkg = null;
		mdp = wordMLPackage.getMainDocumentPart();
		pageNo = pNo;
		
		if( wordMLPackage == null ) settingWord(wordMLPackage);
		
		//큐를 생성한다.
		jobQueue = JobQueue.getInstance();
		jobThreadList = new ArrayList();
		
		// 소비자 쓰레드를 생성하고 시작한다.
		for(int i=0; i< 3; i++)
		{
			Thread conThr = new Thread(new JobConsumer(jobQueue, Integer.toString(i)));
			// 데몸 스레드로 설정한다.
			if(!conThr.isDaemon()) conThr.setDaemon(true);
			conThr.start();
			
			jobThreadList.add(conThr);
		}
		
		WordprocessingMLPackage rtnPkg = toWord();
		
		for(int i=0; i< jobThreadList.size(); i++ )
		{
			Thread conThr = (Thread) jobThreadList.get(i);
			// 데몸 스레드로 설정한다.
			if(conThr.isAlive()) conThr.interrupt();
		}
		jobThreadList.clear();
		
		return rtnPkg;
	}

	// http://docstore.mik.ua/orelly/java-ent/jnut/ch03_03.htm
	protected void finalize() throws Throwable {
		// Invoke the finalizer of our superclass
		// We haven't discussed superclasses or this syntax yet
		super.finalize();

		// Delete a temporary file we were using
		// If the file doesn't exist or tempfile is null, this can throw
		// an exception, but that exception is ignored.
	}
	
	/*****************************************************
	*	워드 문서 생성.
	* 	@return WordprocessingMLPackage
	*****************************************************/
	@SuppressWarnings("deprecation")
	private WordprocessingMLPackage toWord() throws Exception{
		
		log.info(getClass().getName() + "::" + "Start Parsing Word...");

		if( wordMLPackage == null ){
			settingWord(wordMLPackage);
		}

//		wordMLPackage = WordprocessingMLPackage.createPackage();
		//WordprocessingMLPackage wordMLPackage = Log.wordMLPackage;
//		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
//		org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
		
		/*
		// http://stackoverflow.com/questions/7308299/outofmemoryerror-while-doing-docx-comparison-using-docx4j

			Body newerBody = ((Document)newerPackage.getMainDocumentPart().getJaxbElement()).getBody();
	        Body olderBody = ((Document)olderPackage.getMainDocumentPart().getJaxbElement()).getBody();
	
	        // 2. Do the differencing
	        java.io.StringWriter sw = new java.io.StringWriter();
	        Docx4jDriver.diff( XmlUtils.marshaltoW3CDomDocument(newerBody).getDocumentElement(),
	                        XmlUtils.marshaltoW3CDomDocument(olderBody).getDocumentElement(), sw);
	
	        // 3. Get the result
	        String contentStr = sw.toString();
	        System.out.println("Result: \n\n " + contentStr);
	        Body newBody = (Body) org.docx4j.XmlUtils.unmarshalString(contentStr);
		*/
		
		int pageMargin = 0;
		// 용지 여백
//		SectPr sectPr = mdp.getJaxbElement().getBody().getSectPr();
		SectPr sectPr = wmlObjectFactory.createSectPr(); 
		PgMar pgMr = new PgMar();
		pgMr.setTop(BigInteger.valueOf(pageMargin));
		pgMr.setBottom(BigInteger.valueOf(pageMargin));
		pgMr.setLeft(BigInteger.valueOf(pageMargin));
		pgMr.setRight(BigInteger.valueOf(pageMargin));
		sectPr.setPgMar(pgMr);  
		
		PgSz pgSz = null;
		
		//sectPr.setPgBorders(createPageBackground());
		setDocumentBackGround(wordMLPackage,  mPageBackgroundColor );
		
		String _sectPrType = SECTION_MARK_NEXTPAGE;
		
		for (ArrayList<HashMap<String, Object>> pageAr : itemPropList) {
			Date begin = new Date();
			
			if( itemPropList.indexOf(pageAr) == 0 && pageAr.size() == 1 ){
				HashMap<String, Object> _item = pageAr.get(0);
				if( _item.containsKey("waterMark") ){
					makeWaterMark(String.valueOf(_item.get("waterMark")));
					continue;
				}
			}
			/**
			 * 
			 * 한 페이지에 한개의 P
			 * P안에 R 컨텐츠들이 각 아이템들이다.
			 * 
			 **/
			P p = wmlObjectFactory.createP();
			PPr ppr = wmlObjectFactory.createPPr();
			p.setPPr(ppr);
			List<Object> pList = p.getContent();
			
			
			BigInteger pHeight = BigInteger.valueOf(toNum(mpageHeight)*15);
			BigInteger pWidth= BigInteger.valueOf(toNum(mpageWdith)*15);
			
			if( pgSz == null ){
				pgSz = wmlObjectFactory.createSectPrPgSz();
				pgSz.setW(pWidth);
				pgSz.setH(pHeight);
				
				if( pWidth.compareTo( pHeight ) > 0 ) pgSz.setOrient(STPageOrientation.LANDSCAPE);
				else  pgSz.setOrient(STPageOrientation.PORTRAIT);
				sectPr.setPgSz(pgSz);
			}
			
			if( pageNo != START_PAGE &&  mArgoPageOrientation.equals(mPageOrientation)  ){
//				P _p = wmlObjectFactory.createP();
//				R r = wmlObjectFactory.createR(); 
//				Br breakObj = new Br();
//				breakObj.setType(STBrType.PAGE);
//				r.getContent().add(breakObj);
//				
//				_p.getContent().add(r);
//				mdp.getContent().add(_p);
			}
			
			if( !mIsLast )
			{
				// create new section and add it to the document
				SectPr.Type sectPrType = wmlObjectFactory.createSectPrType();
				sectPrType.setVal( _sectPrType ); // "continuous" maens no page break before section
				sectPr.setType(sectPrType);
				ppr.setSectPr(sectPr);
			}
			else
			{
				sectPr = wordMLPackage.getDocumentModel().getSections().get( wordMLPackage.getDocumentModel().getSections().size()-1 ).getSectPr();
				sectPr.setPgSz(pgSz);
				sectPr.setPgMar(pgMr);
			}
			
			
			//Background Image 처리 
			if( mBackgroundImage != null)
			{
				HashMap<String, Object> _bgItem = new HashMap<String, Object>();
				
				String _imageUrl = "";		
				String _type = "";
				if( mBackgroundImage.containsKey("type"))
				{
					_type = mBackgroundImage.get("type").toString();
					
					if( _type.equals("base64"))
					{
						_imageUrl= mBackgroundImage.get("data").toString();
						_imageUrl = URLEncoder.encode( _imageUrl, "UTF-8");
					}
					else if( _type.equals("url") )
					{
						_imageUrl= mBackgroundImage.get("url").toString();
					}
				}
				
				if( _imageUrl != null &&  _imageUrl.equals("null") == false && _imageUrl.equals("") == false )
				{
					_bgItem.put("src", _imageUrl);
					_bgItem.put("type","BACKGROUND_IMAGE");
					_bgItem.put("width",-1);
					_bgItem.put("height",-1);
					_bgItem.put("className","UBImage");
					_bgItem.put("pageWidth", mBackgroundImage.get("pageWidth"));
					_bgItem.put("pageHeight", mBackgroundImage.get("pageHeight"));
					_bgItem.put("className","UBImage");
					
					HashMap<String, Object> param = new HashMap();
					param.put("wordMLPackage", wordMLPackage);
					param.put("pageList", pList);
					param.put("curItem", _bgItem);
					param.put("imgDictionary", this.mImageDictionary);
					jobQueue.put(param);
					
					Thread.sleep(10);		
				}
			}
			
			for (HashMap<String, Object> _item : pageAr) {
				// page 정보를 담고있는 아이템인가?
				Object pH = null;
				Object pW = null;
				if ( ((pH=_item.get("cPHeight")) != null) && ((pW=_item.get("cPWidth")) != null) ) {
//					BigInteger pHeight = BigInteger.valueOf(toNum(pH)*15);
//					BigInteger pWidth= BigInteger.valueOf(toNum(pW)*15);
//					
//					if( pgSz == null ){
//						pgSz = wmlObjectFactory.createSectPrPgSz();
//						pgSz.setW(pWidth);
//						pgSz.setH(pHeight);
//						
//						if( pWidth.compareTo( pHeight ) > 0 ) pgSz.setOrient(STPageOrientation.LANDSCAPE);
//						else  pgSz.setOrient(STPageOrientation.PORTRAIT);
//						sectPr.setPgSz(pgSz);
//					}
				}
				else{
					// 클래스명이 없다면 패스
					if( _item.get("className") == null ) continue;

					// visible false인 아이템은 패스!
					if( _item.get("visible") != null && String.valueOf(_item.get("visible")).equals("false") ) continue;
					

					String itemClass = String.valueOf(_item.get("className"));

					try {

						// 아이템이 라벨 종류인 경우
						if( itemClass.equals("UBSVGRichText") == false && itemClass.equals("UBTextSignature") == false && ( itemClass.contains("Label") || itemClass.contains("Text") || itemClass.equals("UBApproval") ) ){
							R label = makeLabel(_item);
							if( label != null ){
								pList.add( label );
//								Thread.sleep(10);
							}
						}
						else if( itemClass.equals("UBComboBox") || itemClass.equals("UBDateFiled") ){
							// 콤보, 데이트필드의 경우 테두리를 그리지 않도록 한다. - 이장환이사님의견 반영.
							_item.put("borderWidth", "0" );
							_item.put("isCell", "false" );
							_item.put("borderType", "none" );
							_item.put("borderSide", "[]" );
							
							R label = makeLabel(_item);
							if( label != null ){
								pList.add( label );
//								Thread.sleep(10);
							}
						}
						else if( itemClass.equals("UBRadioBorder") || itemClass.equals("UBCheckBox") ){
							R label = makeLabel(setEformItemAttr(_item));
							if( label != null ){
								pList.add( label );
//								Thread.sleep(10);
							}
						}
						else if( itemClass.equals("UBTable") ){
							Tbl table = makeTable(_item);
							if( table != null ){
								
								boolean _useTextBoxTable = false;
//								boolean _useTextBoxTable = true;
								if( _useTextBoxTable )
								{
									Object _addTable = convertTableTextBox(table, _item);
									mdp.getContent().add( _addTable );
								}
								else
								{
									mdp.getContent().add( table );
								}
							
							}
						}
						else if( itemClass.equals("UBGraphicsCircle") ){
							R circle = makeCircle(_item);
							if( circle != null ){
								pList.add( circle );
							}
						}
						// UBGraphicsRectangle || UBGraphicsGradiantRectangle
						else if( itemClass.contains("Rectangle") ){
							R rect = makeRect(_item);
							if( rect != null ){
								pList.add( rect );
							}
						}
						else if( itemClass.equals("UBGraphicsLine") ){
							R line = makeLine(_item);
							if( line != null ){
								pList.add( line );
							}
						}
						else if( itemClass.equals("UBConnectLine") ){
							R line = makeConnectLine(_item);
							if( line != null ){
								pList.add( line );
							}
						}
						// svg 형식의 text
						else if( itemClass.equals("UBSVGRichText") ){
							R svgtxt = makeSVGText(_item);
							if( svgtxt != null ){
								pList.add( svgtxt );
							}
						}
						// High Chart : svg 태그를 base64로 변환하여 src 속성값 변경 후 이미지 생성 함수를 태운다.
						//else if( itemClass.equals("UBSVGArea") ){
						else if( itemClass.equals("UBSVGArea")){
							// TODO 이미지 엑박 현상 수정 필요.
							String _src = makeSVGArea(_item);
//							log.debug(getClass().getName() + "::" + "svg base64\n" + _src);
							_item.put("src", _src);
							// 없는 속성들.
//							_item.put("scaleX", 1);
//							_item.put("scaleY", 1);
//							_item.put("opacity", 1);
//							_item.put("angle", 0);
							
							HashMap<String, Object> param = new HashMap();
							param.put("wordMLPackage", wordMLPackage);
							param.put("pageList", pList);
							param.put("curItem", _item);
							
							jobQueue.put(param);
							
							Thread.sleep(10);		
						}
						else if( itemClass.equals("UBClipArtContainer") ){
							
							String _src = CreateClipImage(_item);
							if( !_src.equals("null") && !_src.equals("") ){
								_item.put("src", _src);
								
								HashMap<String, Object> param = new HashMap();
								param.put("wordMLPackage", wordMLPackage);
								param.put("pageList", pList);
								param.put("curItem", _item);
								
								jobQueue.put(param);
								
								Thread.sleep(10);		
							}
						}
						// 나머지 타입은 아미지로 처리.
						else{
							String _src = String.valueOf(_item.get("src"));
//							log.debug(getClass().getName() + "::" + "image src\n" + _src);
							if( !_src.equals("null") && !_src.equals("") ){
								
								HashMap<String, Object> param = new HashMap();
								param.put("wordMLPackage", wordMLPackage);
								param.put("pageList", pList);
								param.put("curItem", _item);
								param.put("imgDictionary", this.mImageDictionary);
								jobQueue.put(param);
								
								Thread.sleep(10);		
								
								/*
								R img = makeImage(wordMLPackage, _item);
								if( img != null ){
									pList.add( img );
								}
								*/
							}
							else{
								log.error(getClass().getName() + "::toWord::" + ">>>>>>>>>>[ "+_item.get("className")+"\t"+_item.get("id")+" ] item's src value is empty.");
							}
						}
						
					} catch (Exception e) {
						log.error(getClass().getName() + "::toWord::" + ">>>>>>>>>>[ "+_item.get("className")+"\t"+_item.get("id")+" ] parsing Error. >>>"+e.getMessage());
					}
				}
			}// For item End
			
			mdp.getContent().add( p );

			Date end = new Date();
			long diff = end.getTime() - begin.getTime();
			long diffSec = diff / 1000% 60;         
			long diffMin = diff / (60 * 1000)% 60;        
			
			log.debug(getClass().getName() + "::toWord::" +pageNo+" page [ "+diffMin +":"+diffSec+"." + diff%1000+" ]\t\t"+runtime.freeMemory()+" KB");
			
		}// For data list End
		
		
		// 큐에 있는 모든 작업이 완료될때 까지 기다린다.
		while(jobQueue.getJobCount() > 0)
		{
			Thread.sleep(100);
		}
		jobQueue.clear();
		
		mArgoPageOrientation = mPageOrientation;
		
		return wordMLPackage;
	}// toWord End
	

	/**
	 * <pre>
	 * 라디오버튼/체크박스 아이템을 선택된 상태에 따라 문자로 치환하여 표시한다.
	 * 라디오 :  "⊙ ":"○ "
	 * 체크 : "▣ ":"□ "
	 * 체크박스의 경우 체크모양 딩벳기호는 깨져 네모로 대체한다.
	 * </pre>
	 * @param _item 라디오/체크박스 아이템
	 * @return _item 텍스트 값으로 변경된 아이템.
	*/
	private HashMap<String, Object> setEformItemAttr( HashMap<String, Object> _item ) {
		
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
	
	private Object convertTableTextBox( Tbl _table, HashMap<String, Object> _item )
	{
		ObjectFactory vmlObjectFactory = new ObjectFactory();
		
		R resultR = wmlObjectFactory.createR();
		RPr resultRpr = wmlObjectFactory.createRPr();
		resultR.setRPr(resultRpr);
		Pict pict = wmlObjectFactory.createPict();
		resultR.getContent().add(pict);
		
		CTShape shape = vmlObjectFactory.createCTShape();
		JAXBElement<CTShape> shapeWrapped = vmlObjectFactory.createShape(shape);
		pict.getAnyAndAny().add(shapeWrapped);
		
		CTTextbox textbox = vmlObjectFactory.createCTTextbox();
		JAXBElement<CTTextbox> textboxWrapped = vmlObjectFactory.createTextbox(textbox);
		shape.getEGShapeElements().add(textboxWrapped);
		// Create object for txbxContent
		CTTxbxContent txbxcontent = wmlObjectFactory.createCTTxbxContent();
		textbox.setTxbxContent(txbxcontent);
		textbox.setInset("0,0,0,0");
		txbxcontent.getContent().add(_table);
		
		String styles = "position:absolute;" +
				"visibility:visible;" +
				"padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt;" +
				"mso-position-horizontal:absolute;" +
				"mso-position-vertical:absolute";
		//width
		if( _item.get("width") != null ){
			styles += ";width:"+toCM(_item.get("width"))+"cm";
		}
		//height
		if( _item.get("height") != null ){
			styles += ";height:"+toCM( Float.valueOf( _item.get("height").toString() ) + 5)+"cm";
		}
		//x
		if( _item.get("x") != null ){
			styles += ";margin-left:"+toPointStr(_item.get("x"))+"pt";
		}
		//y
		if( _item.get("y") != null ){
			styles += ";margin-top:"+toPointStr(_item.get("y"))+"pt";
		}
		
		shape.setStyle(styles);
		
		return pict;
	}
	
	
	MainDocumentPart mdp = null;
	org.docx4j.wml.ObjectFactory wmlObjectFactory = null;
	public void settingWord(WordprocessingMLPackage wordPkg) throws Exception {

		if( wordPkg == null ){
			Date begin = new Date();
			wordPkg = WordprocessingMLPackage.createPackage();
			Date end = new Date();
			long diff = end.getTime() - begin.getTime();
			long diffSec = diff / 1000% 60;         
			long diffMin = diff / (60 * 1000)% 60;        
			log.debug(getClass().getName() + "::settingWord::" + "Create WordprocessingMLPackage. [ "+diffMin +":"+diffSec+"." + diff%1000+" ]");
		}
		
		
		wordMLPackage = wordPkg;
		//WordprocessingMLPackage wordMLPackage = Log.wordMLPackage;
		
		/*MainDocumentPart */mdp = wordMLPackage.getMainDocumentPart();
		/* org.docx4j.wml.ObjectFactory */wmlObjectFactory = Context.getWmlObjectFactory();
	}

	/** 
	 * REF : http://stackoverflow.com/questions/22781843/inserting-any-shape-into-a-word-document-using-apache-poi 
	 * */
	/** 
	 * <pre>
	 *  라벨 아이템 생성 함수.
	 *  워드의 텍스트 상자 아이템 사용.
	 *  R/Pict/Shape/Textbox/TextboxContent/P/R/
	 * </pre>
	 * @return R
	 * @throws Exception 
	 * */
	protected R makeLabel( HashMap<String, Object> _item ) throws Exception {

//		org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
		ObjectFactory vmlObjectFactory = new ObjectFactory();

		// Create object for r
		R resultR = wmlObjectFactory.createR();
		// Create object for rPr
		RPr resultRpr = wmlObjectFactory.createRPr();
		resultR.setRPr(resultRpr);
		// Create object for noProof
//		BooleanDefaultTrue booleandefaulttrue = wmlObjectFactory.createBooleanDefaultTrue();
//		resultRpr.setNoProof(booleandefaulttrue);
		// Create object for pict (wrapped in JAXBElement)
		Pict pict = wmlObjectFactory.createPict();
//		JAXBElement<Pict> pictWrapped = wmlObjectFactory.createRPict(pict);
//		resultR.getContent().add(pictWrapped);
		resultR.getContent().add(pict);
		
		/*
			텍스트상자를 삽입하기 위해, Shape 필요.
		*/
		
		// Create object for shapetype (wrapped in JAXBElement)
//		CTShapetype shapetype = vmlObjectFactory.createCTShapetype();
//		JAXBElement<CTShapetype> shapetypeWrapped = vmlObjectFactory.createShapetype(shapetype);
//		pict.getAnyAndAny().add(shapetypeWrapped);

		// Create object for shape (wrapped in JAXBElement)
		CTShape shape = vmlObjectFactory.createCTShape();
		JAXBElement<CTShape> shapeWrapped = vmlObjectFactory.createShape(shape);
		pict.getAnyAndAny().add(shapeWrapped);
//		pict.getAnyAndAny().add(shape);
		
		// Create object for textbox (wrapped in JAXBElement)
		CTTextbox textbox = vmlObjectFactory.createCTTextbox();
		JAXBElement<CTTextbox> textboxWrapped = vmlObjectFactory.createTextbox(textbox);
		shape.getEGShapeElements().add(textboxWrapped);
		// Create object for txbxContent
		CTTxbxContent txbxcontent = wmlObjectFactory.createCTTxbxContent();
		textbox.setTxbxContent(txbxcontent);
		textbox.setInset("0,0,0,0");
		
		// testBox Size만큼 키우도록 변경 
		// Create object for p
		P p = wmlObjectFactory.createP();
		txbxcontent.getContent().add(p);
		// Create object for pPr
		PPr ppr = wmlObjectFactory.createPPr();
		p.setPPr(ppr);
		// Create object for rPr
//		ParaRPr pararpr2 = wmlObjectFactory.createParaRPr();
//		ppr.setRPr(pararpr2);
		
		int _fontPointSize = Double.valueOf(  Math.floor( Float.valueOf(_item.get("fontSize").toString()) * 72 / 96 * 12 / 10 ) ).intValue();
		float _lineHeight = 1.2f;
		//TODO LineHeight Test		
		if( _item.get("lineHeight") != null ){
			_lineHeight = Float.parseFloat( _item.get("lineHeight").toString());
			_fontPointSize = Double.valueOf(  Math.ceil( Float.valueOf(_item.get("fontSize").toString()) * 72 / 96 * (_lineHeight) ) ).intValue();
		}
		
		PPrBase.Spacing pprbasespacing = wmlObjectFactory.createPPrBaseSpacing(); 
		pprbasespacing.setAfter( ubFormToWord.BIGINTEGER_ZERO );
		pprbasespacing.setBefore( ubFormToWord.BIGINTEGER_ZERO );
		pprbasespacing.setAfterAutospacing(false);
		pprbasespacing.setBeforeAutospacing(false);
		pprbasespacing.setLine(BigInteger.valueOf(SPACING_LINE * _fontPointSize));
//		pprbasespacing.setLine(BigInteger.valueOf(225));
		pprbasespacing.setLineRule(STLineSpacingRule.EXACT);
		ppr.setSpacing(pprbasespacing); 
		// Create object for lang
//		CTLanguage language2 = wmlObjectFactory.createCTLanguage();
//		pararpr2.setLang(language2);
//		language2.setVal("en-AU");
		// Create object for r
		R r = wmlObjectFactory.createR();
		p.getContent().add(r);
		// Create object for rPr
		RPr rpr = wmlObjectFactory.createRPr();
		r.setRPr(rpr);
		
		// 단락뒤에 공백 제거
		CTSignedTwipsMeasure space = new CTSignedTwipsMeasure();
		space.setVal(ubFormToWord.BIGINTEGER_ZERO);
		rpr.setSpacing(space);
		
		// 기본 stroke 가 지정됨 --> 타입종류를 찾아봐야함..
		shape.setType("#_x0000_t202");
		
		//textAlign
		if( _item.get("textAlign") != null ){
			Jc justification = wmlObjectFactory.createJc();
			
			switch ( ETextAlign.valueOf(String.valueOf(_item.get("textAlign")))) {
			case left: justification.setVal(JcEnumeration.LEFT);break;
			case center: justification.setVal(JcEnumeration.CENTER); break;
			case right: justification.setVal(JcEnumeration.RIGHT); break;
			}
			
			ppr.setJc(justification);
		}
		
		// word단위일경우 true 아닐경우 false  - 20180102 최명진
		BooleanDefaultTrue _bt = new BooleanDefaultTrue();
		_bt.setVal(false);
		ppr.setWordWrap(_bt);
		
		if( _item.get("text") != null ){
			
			String itemTxt = String.valueOf(_item.get("text"));
			//itemTxt = URLDecoder.decode(itemTxt, "UTF-8");
			// 줄바꿈 처리
			itemTxt = itemTxt.replace("\\n", "\n").replaceAll("\\r", "\r");
			
			// 줄바꿈 문자가 들어있는 경우.
			if(  itemTxt.indexOf("\n") != -1 ){
				String[] splitTxt = itemTxt.split("\n");
				int splitSize = splitTxt.length;
				List<Object> rContent = r.getContent();
				for (int i = 0; i < splitSize; i++) {
					
					Text text = wmlObjectFactory.createText();
					text.setValue( splitTxt[i] );
					text.setSpace("preserve");
//					JAXBElement<Text> textWrapped = wmlObjectFactory.createRT(text);
//					rContent.add(textWrapped);
					rContent.add(text);
					
					// 마지막 문자가 아닌경우, 줄바꿈 추가.
					if( i != (splitSize-1) ){
						Br breakObj = new Br();
						rContent.add(breakObj);
					}
					
				}// end For
			}
			// 줄바꿈 없는 경우.
			else{
				Text text = wmlObjectFactory.createText();
//				JAXBElement<Text> textWrapped = wmlObjectFactory.createRT(text);
//				r.getContent().add(textWrapped);
				r.getContent().add(text);
				text.setValue( itemTxt );
				text.setSpace("preserve");
			}
		}
		
		if( _lineHeight > 1.2f )
		{
			TextAlignment _txtAligment = ppr.getTextAlignment();
			if( _txtAligment == null ){
				_txtAligment = wmlObjectFactory.createPPrBaseTextAlignment();
			}
			
			_txtAligment.setVal("bottom");
			ppr.setTextAlignment(_txtAligment);
		}
		
//		CTVerticalAlignRun vAlign = Context.getWmlObjectFactory().createCTVerticalAlignRun();
//		if( _lineHeight > 1.2f )
//		{
//			vAlign.setVal(STVerticalAlignRun.SUPERSCRIPT);			
//		}
//		else
//		{
//			vAlign.setVal(STVerticalAlignRun.BASELINE);
//		}
//		rpr.setVertAlign(vAlign);
		
		/* 
		 * http://blog.iprofs.nl/2012/11/19/adding-layout-to-your-docx4j-generated-word-documents-part-2/
		 * */
		
		//fontWeight
		if( _item.get("fontWeight") != null ){
			BooleanDefaultTrue b = new BooleanDefaultTrue();
		    
			switch (EFontWeight.valueOf(String.valueOf(_item.get("fontWeight")))) {
			case normal: b.setVal(false);rpr.setB(b); break;
			case bold: b.setVal(true);rpr.setB(b); break;
			}
		}
		//fontStyle
		if( _item.get("fontStyle") != null ){
			if( String.valueOf(_item.get("fontStyle")).equals("italic") ){
				BooleanDefaultTrue isItalic = new BooleanDefaultTrue();
				isItalic.setVal(true);
				rpr.setI(isItalic);
			}
		}
		//fontSize
		if( _item.get("fontSize") != null ){
			
			
			
//			float _readingSize = Float.valueOf(_item.get("fontSize").toString());
			
//			int _checkValue = (int) _readingSize;
//			if( _checkValue == _readingSize ){
//				System.out.println("정수 같다. : " + _checkValue+ ":"+_readingSize);
//				pprbasespacing.setLine(BigInteger.valueOf(225));
//			}else{
//				System.out.println("실수 다르다. : " + _checkValue+ ":"+_readingSize);
//				pprbasespacing.setLine(BigInteger.valueOf(235));
//			}
			
			// 소숫점이 유실되어 소숫점 1자리까지 처리하도록 수정.
			//_readingSize = (float) (Math.floor(_readingSize*10f) / 10f);
			
//			_readingSize = Double.valueOf(Float.valueOf(_item.get("fontSize").toString()) * 72 /  96 ).floatValue();
			
//			long fontSize = (long) (_readingSize*2);
			
//			long fontSize = (long) (toNum(_item.get("fontSize"))*1.5);
			long fontSize = (long) ( Float.valueOf(_item.get("fontSize").toString()) * 72 / 96 )*2;
			
			HpsMeasure size = new HpsMeasure();
	        size.setVal(BigInteger.valueOf(fontSize));
	        rpr.setSz(size);
		}
		//fontFamily
		if( _item.get("fontFamily") != null ){
			RFonts runFont = new RFonts();
			String fname = getFontName(String.valueOf(_item.get("fontFamily")));
//			fname = URLDecoder.decode(fname, "UTF-8");
	        runFont.setAscii( fname );
	        runFont.setHAnsi( fname );
	        runFont.setEastAsia(fname);
	        rpr.setRFonts(runFont);
		}
		//textDecoration
		if( _item.get("textDecoration") != null ){
			U underline = new U();
			switch (ETextDecoration.valueOf(String.valueOf(_item.get("textDecoration")))) {
			case none: underline.setVal(UnderlineEnumeration.NONE); rpr.setU(underline); break;
			case normal: underline.setVal(UnderlineEnumeration.NONE); rpr.setU(underline); break;
			case underline: underline.setVal(UnderlineEnumeration.SINGLE); rpr.setU(underline); break;
			}
		}
		//fontColor
		if( _item.get("fontColor") != null ){
			Color c = new Color();
			c.setVal(String.valueOf(_item.get("fontColor")).replace("#", ""));
			rpr.setColor(c);
		}
		//textRotate
		String rotate = String.valueOf(_item.get("textRotate"));
		if( !rotate.equals("null") && !rotate.equals("") ){
			// -90 ~ 90
			String rotateStyle = "";
			switch (Short.valueOf(rotate)) {
			case 0:
			case 45:
			case 180: rotateStyle = ""; break; // 지원안됨.
			case 90: rotateStyle = "layout-flow:vertical"; break;
			case 270: rotateStyle = "layout-flow:vertical;mso-layout-flow-alt:bottom-to-top"; break;
			}
			if( !rotateStyle.equals("") ){
				textbox.setStyle(rotateStyle);
			}
		}

		// Border : color, type, width 체크
		String vIsCell = String.valueOf(_item.get("isCell"));
		String bdSide = String.valueOf(_item.get("borderSide"));
		boolean isCell = ( vIsCell.equals("null") || vIsCell.equals("") ) ? false :Boolean.valueOf(vIsCell);
		String bdType = "solid";
		if( bdSide.equals("null") || bdSide.equals("[]")){
			bdType = "none";
		}
		else if( isCell ){
			// isCell = true. 속성이 배열. 첫번째 원소의 값으로 border를 지정한다.
			
			// 테두리는 TOP의 타입을 따른다.
			bdSide = bdSide.replace("[", "");
			bdSide = bdSide.replace("]", "");
			bdSide = bdSide.replaceAll(" ", "");
			ArrayList<String> bdSideAr = new ArrayList<String>(Arrays.asList(bdSide.split(",")));
			int bdTopIdx = bdSideAr.indexOf("top");
			// TOP이 없는 경우 LEFT를 가져온다. right, bottom 은 없는 경우가 많으므로 따로 체크하진 않는다.
			if( bdTopIdx == -1 ) bdTopIdx = bdSideAr.indexOf("left");

			//borderTypes
			String bdTypes = String.valueOf(_item.get("borderTypes"));
			if( !bdTypes.equals("null") && !bdTypes.equals("") ){
				bdTypes = bdTypes.replace("[", "");
				bdTypes = bdTypes.replace("]", "");
				bdTypes = bdTypes.replaceAll(" ", "");
				String[] bdTypeAr = bdTypes.split(",");
				
				if( bdTypeAr != null ){
					if(  bdTopIdx != -1 && bdTopIdx <= bdTypeAr.length)
						bdType = bdTypeAr[bdTopIdx];
					// top, left 가 없으면 0번째로..
					else
						bdType = bdTypeAr[0];
				}
			}
			if( !bdType.equals("none") ){
				/*
				 * borderColors
				 * word문서.xml 
				 * <v:shape id="_x0000_s1083" type="#_x0000_t202" style="~~" strokecolor="#5f497a [2407]">
				 * red, blue 등 문자형도 인식함. (종류를 다 판단하지는 못함.. red, blue, green 확인.	
				 */
				String bdColors = String.valueOf(_item.get("borderColors"));
				if( !bdColors.equals("null") && !bdColors.equals("")  ){
					bdColors = bdColors.replace("[", "");
					bdColors = bdColors.replace("]", "");
					bdColors = bdColors.replaceAll(" ", "");
					String[] bdColorAr = bdColors.split(",");
					
					if( bdColorAr != null ){
						bdColors = bdColorAr[0];
						shape.setStrokecolor(bdColors);
					}
				}
				//borderWidths
				String bdWidths = String.valueOf(_item.get("borderWidths"));
				if( !bdWidths.equals("null") && !bdWidths.equals("") ){
					bdWidths = bdWidths.replace("[", "");
					bdWidths = bdWidths.replace("]", "");
					bdWidths = bdWidths.replaceAll(" ", "");
					String[] bdWidthAr = bdWidths.split(",");
					
					if( bdWidthAr != null ) shape.setStrokeweight(toPoint(bdWidthAr[0])+"pt");
				}
			}
		}
		else{
			// isCell = false. 속성이 단일 값.
			// borderType
			bdType = String.valueOf(_item.get("borderType"));
			if( bdType.equals("null") || bdType.equals("") ){
				bdType = "none";
			}
			if( !bdType.equals("none") ){
				//borderColor
				String bdColor = String.valueOf(_item.get("borderColor"));
				if( !bdColor.equals("null") && !bdColor.equals("") ){
					shape.setStrokecolor(bdColor);
				}
				//borderWidth
				String bdWidth = String.valueOf(_item.get("borderWidth"));
				if( !bdWidth.equals("null") && !bdWidth.equals("") ){
					shape.setStrokeweight(toPoint(bdWidth)+"pt");
				}else{
					bdWidth = "1pt";
					shape.setStrokeweight(toPoint(bdWidth)+"pt");
				}
			}
		}
		

		// border Type 적용.
		if( bdType.equals("solid") || bdType.equals("SOLD")){
			// 일반실선이 기본.
		}
		else if( bdType.equals("none") || bdType.equals("") ){
			shape.setStroked(STTrueFalse.F);
		}
		else{
			CTStroke stroke = vmlObjectFactory.createCTStroke();
			/*
			 * [ Word 제공 선 스타일 속성 명 ] : WORD파일을 xml 저장해서 따온 속성들. ( api 에서는 속성정보를 명시해주지 않는다...)
			 * 
			 * circle dot : <v:stroke dashstyle="1 1" endcap="round"/>
			 * square dot : <v:stroke dashstyle="1 1"/>
			 * 
			 * dash : <v:stroke dashstyle="longDash"/>
			 * dash dot : <v:stroke dashstyle="longDashDot"/> 
			 * dash dot dot : <v:stroke dashstyle="longDashDotDot"/>
			 * 
			 *-- 짧은 dash 선의 경우 dash dot dot 제공안함. --> 다 긴선으로사용~
			 * dash : <v:stroke dashstyle="dash"/>
			 * short dash dot : <v:stroke dashstyle="dashDot"/>
			 */
			if( bdType.equals("double") ){
				stroke.setLinestyle(STStrokeLineStyle.THIN_THIN);
				// 3pt 로 해야 double 이 보이고, 더 얇으면 안보임... 에디터에서도 double border 인 경우 굵기 조정이 안됨.
				shape.setStrokeweight("3pt");
			}
			else{
				try {
					String strokeDashStyle = "";
					//borderType
					switch (EBorderType.valueOf(bdType)) {
					case dot: strokeDashStyle = "1 1";  stroke.setEndcap(STStrokeEndCap.ROUND);  break;
					case dash: strokeDashStyle = "longDash"; break;
					case dash_dot: strokeDashStyle = "longDashDot"; break;
					case dash_dot_dot: strokeDashStyle = "longDashDotDot"; break;
					}
					
					if( !strokeDashStyle.equals("") ){
						stroke.setDashstyle(strokeDashStyle);
					}
				} catch (Exception e) {
					log.error(getClass().getName()+"::>>>>>>>>>> EBorderType value is wrong : " +bdType);
				}
			}
			
			JAXBElement<CTStroke> strokeWrapped = vmlObjectFactory.createStroke(stroke);
			shape.getEGShapeElements().add(strokeWrapped);
			
		}
		/*
		 // 굵기
		if( _item.get("borderWidth") != null ){
			// 1px = 0.75pt
			double ptWeight = toPoint(_item.get("borderWidth"));
			if( ptWeight > 1584 ) ptWeight = 1584;
			shape.setStrokeweight( ptWeight+"pt");
			styles += ";border-width:"+ptWeight+"pt";
		}
		STBorder bType = STBorder.NONE;
		if( _item.get("borderType") != null ){
			
			switch (EBorderType.valueOf(String.valueOf(_item.get("borderType")))) {
			case solid: bType = STBorder.SINGLE; break;
			case dot: bType = STBorder.DOTTED;  break;
			case dash: bType = STBorder.DASHED; break;
			case dash_dot: bType = STBorder.DOT_DASH; break;
			case dbl: bType = STBorder.DOUBLE; break;
			case dash_dot_dot: bType = STBorder.DOT_DOT_DASH; break;
			case SOLD: bType = STBorder.NONE; break;
			}
			styles += ";border-style:"+String.valueOf(_item.get("borderType"));
	 
		}
		// borderSide
		//--------------------- R(글자 영역)에 border 지정하는 형식. 텍스트 상자 자체에는 부분으로 지정할 수 없음.
		if( _item.get("borderSide") != null ){
			
			String _b = String.valueOf(_item.get("borderSide")).trim().replaceAll(" ", "");
			
			if( _b.equals("[]")){
				shape.setType("");
			}
			else{
				
				// PPr에 Border를 줌. --> 텍스트 상자가 아니라 안쪽에 테두리가 생김. 여백을 지우고 100%??? 
				PPrBase.PBdr bdr = wmlObjectFactory.createPPrBasePBdr();
				CTBorder ctB = new CTBorder();
				ctB.setVal(bType);
				
				//앞뒤 괄호를 제거하고, 쉼표로 구분하여 배열 생성.
				ArrayList<String> bStrList = new ArrayList<String>(Arrays.asList(_b.substring(1, _b.length()-1).split(",")));
				
				if( bStrList.size() == 0 ){
//					log.debug(getClass().getName() + "::makeLabel::" + "borderSide value is invalid >>> " + _b);
				}
				else if( bStrList.size() > 4 ){
//					log.debug(getClass().getName() + "::makeLabel::" + "borderSide value is invalid >>> " + _b);
				}
				else{
					if( bStrList.indexOf("left") != -1 ) bdr.setLeft(ctB);
					if( bStrList.indexOf("right") != -1 ) bdr.setRight(ctB);
					if( bStrList.indexOf("top") != -1 ) bdr.setTop(ctB);
					if( bStrList.indexOf("bottom") != -1 ) bdr.setBottom(ctB);
				}
				
				//borderColor
				if( _item.get("borderColorInt") != null ){
					String c = changeColorToHex(Integer.valueOf(String.valueOf(_item.get("borderColorInt"))));
					if( bStrList.indexOf("left") != -1 ) shape.setBorderleftcolor(c);
					if( bStrList.indexOf("right") != -1 ) shape.setBorderrightcolor(c);
					if( bStrList.indexOf("top") != -1 ) shape.setBordertopcolor(c);
					if( bStrList.indexOf("bottom") != -1 ) shape.setBorderbottomcolor(c);

					textbox.setStyle("width:100%;height:100%;padding:0pt;margin:0pt;"
							+"border:0.75pt "+String.valueOf(_item.get("borderType"))+" "+ String.valueOf(_item.get("borderColor")).replace("#", ""));
				}
				
				//borderColor
				if( _item.get("borderColor") != null ){
					String c = String.valueOf(_item.get("borderColor")).replace("#", "");
					if( bStrList.indexOf("left") != -1 ) shape.setBorderleftcolor(c);
					if( bStrList.indexOf("right") != -1 ) shape.setBorderrightcolor(c);
					if( bStrList.indexOf("top") != -1 ) shape.setBordertopcolor(c);
					if( bStrList.indexOf("bottom") != -1 ) shape.setBorderbottomcolor(c);
					
					textbox.setStyle("width:100%;height:100%;padding:0pt;margin:0pt;"
							+"border-weight:0.75pt;"+"border-style:"+String.valueOf(_item.get("borderType"))
							+";border-color:"+String.valueOf(_item.get("borderColor"))
							);
					styles += ";border-color:"+String.valueOf(_item.get("borderColor"));
				}
				
				// 상자 안쪽에 Border... 스타일은 잘 먹지만,, 여백이 생김.
//				ppr.setPBdr(bdr);
			}
			
		}
		*/

		// 도형의 style 문자열로 속성 세팅.
		String styles = "position:absolute;" +
				"visibility:visible;" +
				"padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt;" +
				"mso-position-horizontal:absolute;" +
				"mso-position-vertical:absolute";

		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			styles += ";v-text-anchor:"+String.valueOf(_item.get("verticalAlign"));
		}
		//width
		if( _item.get("width") != null ){
			styles += ";width:"+toPointStr(_item.get("width"))+"pt";
		}
		//height
		if( _item.get("height") != null ){
			styles += ";height:"+toPointStr(_item.get("height"))+"pt";
		}
		//x
		if( _item.get("x") != null ){
			styles += ";margin-left:"+(toPointStrMinusPadding(_item.get("x"), "x"))+"pt";
		}
		//y
		if( _item.get("y") != null ){
			styles += ";margin-top:"+toPointStr(_item.get("y"))+"pt";
		}

		//backgroundColor
		if( _item.get("backgroundAlpha") != null ){
			String bgAlpha = String.valueOf(_item.get("backgroundAlpha"));
			if( bgAlpha.equals("0") ){
				shape.setFilled(STTrueFalse.F);
			}
			else{
				if( _item.get("backgroundColorInt") != null ){
					shape.setFillcolor(changeColorToRGB(Integer.valueOf(String.valueOf(_item.get("backgroundColorInt")))));

					// 기본 속성
					if( !bgAlpha.equals("1") ){
						CTFill fill = vmlObjectFactory.createCTFill();
						fill.setOpacity(bgAlpha);
						JAXBElement<CTFill> fillWrapped = vmlObjectFactory.createFill(fill); 
						shape.getEGShapeElements().add(fillWrapped);
					}
				}
			}
		}
		
		shape.setStyle(styles);

		return resultR;
	}
	
	/** 
	 * <pre>
	 *  테이블 아이템 생성 함수.
	 * </pre>
	 * @param _item 테이블 아이템.
	 * @return Tbl
	 * @throws Exception 
	 * */
	protected Tbl makeTable( HashMap<String, Object> _item ) throws Exception {

		log.info(getClass().getName() + "::makeTable::Call makeTable()");
		
		Tbl table = wmlObjectFactory.createTbl();
		
		TblPr tablePr = wmlObjectFactory.createTblPr();
		table.setTblPr(tablePr);
		// TblGrid tableGrid = wmlObjectFactory.createTblGrid();
		
		// Table Width 고정
		CTTblLayoutType _layType = new CTTblLayoutType();
		_layType.setType(STTblLayoutType.FIXED);
		tablePr.setTblLayout(_layType);
		
		
		//w:tblLook w:firstRow="1" w:lastRow="0" w:firstColumn="1" w:lastColumn="0" w:noHBand="0" w:noVBand="1"/>
//		CTTblLook _ctlook = new CTTblLook();
//		_ctlook.setFirstRow(STOnOff.ONE);
//		_ctlook.setFirstColumn(STOnOff.ONE);
//		_ctlook.setLastColumn(STOnOff.ZERO);
//		_ctlook.setLastRow(STOnOff.ZERO);
//		tablePr.setTblLook(_ctlook);
		
		// Tabel 셀의 padding값을 지정해준다.
		
		CTTblCellMar _cttblmar = wmlObjectFactory.createCTTblCellMar();
		
		TblWidth _tblMarZero = new TblWidth();
		_tblMarZero.setW( BigInteger.valueOf( 0 )   );
		_tblMarZero.setType(TblWidth.TYPE_AUTO);
		
		_cttblmar.setLeft(_tblMarZero);
		_cttblmar.setRight(_tblMarZero);
		_cttblmar.setTop(_tblMarZero);
		_cttblmar.setBottom(_tblMarZero);
			
		tablePr.setTblCellMar(_cttblmar);
		
		// xy 좌표
		CTTblPPr tablePpr = new CTTblPPr();
		tablePr.setTblpPr(tablePpr);
		
		TblWidth _tblWZero = new TblWidth();
		_tblWZero.setW( BigInteger.valueOf( 0 )   );
		_tblWZero.setType(TblWidth.TYPE_AUTO);
		
		tablePr.setTblW( _tblWZero );
		//x
		if( _item.get("x") != null ){
			tablePpr.setTblpX(BigInteger.valueOf((long)toNum(_item.get("x"))*15));
		}
		//y
		if( _item.get("y") != null ){
			tablePpr.setTblpY(BigInteger.valueOf((long)toNum(_item.get("y"))*15));
		}
		
		// _item의 속성에 들어있는 컬럼갯수정보.
		int colCnt = 0;
		// rowspan 빈 셀이 필요한지 여부를 저장하는 배열.
		ArrayList<HashMap<String, Object>> rowspanEndIdxAr = new ArrayList<HashMap<String, Object>>();
		if( _item.containsKey("columnCount") ){
			colCnt = toNum(_item.get("columnCount"));
		}
		
		// 아이템에서 rows 에 담긴 셀 아이템을 가져와서 그린다.
		ArrayList<ArrayList<HashMap<String, Object>>> rowAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("rows");
		ArrayList<Float> _rowHeightAr = (ArrayList<Float>) _item.get("heightInfo");		// Row의 Height List
		int rowSize = rowAr.size();
		
		
		HashMap<String, Object> rowspanInfo = null;
		
		
		for (int rIdx = 0; rIdx < rowSize; rIdx++) {

			Tr tbRow = wmlObjectFactory.createTr();
			TrPr tbRowPr = wmlObjectFactory.createTrPr();
			tbRow.setTrPr(tbRowPr);
			
			//Row의 Height값을 맵핑
			if( _rowHeightAr.size() > rIdx ){
				CTHeight ctHeight = new CTHeight();
				ctHeight.setHRule(STHeightRule.EXACT);
				
//				float _addH = (rIdx == rowSize-1)?1:0;
				
//				ctHeight.setVal(BigInteger.valueOf((long)toNum(_rowHeightAr.get(rIdx)+_addH)*15));
				ctHeight.setVal(BigInteger.valueOf((long)toNum(_rowHeightAr.get(rIdx))*15));
				TrHeight tblRowHeight = new TrHeight(ctHeight);
				tblRowHeight.set(tbRowPr);
			}
			
			ArrayList<HashMap<String, Object>> colAr = rowAr.get(rIdx);
			
			int _currentColPosition = 0;	// 현재 colAr의 인덱스
			for (int colIdx = 0; colIdx < colCnt; colIdx++) {
				
				// 빈셀 정보 받아오기.
				if(rowspanEndIdxAr.size() <= colIdx)
				{
					rowspanEndIdxAr.add(null);
				}
				rowspanInfo = rowspanEndIdxAr.get(colIdx);
				
				// 현재 인덱스와 colAr의 인덱스가 같을경우 셀 생성 아닐경우 에는 세로 병합된 셀인지 여부를 체크/ 병합된 셀일경우 빈 셀 생성
				if( colAr.size() > _currentColPosition && colAr.get(_currentColPosition).containsKey("colIndex") 
						&& colIdx == Integer.valueOf(colAr.get(_currentColPosition).get("colIndex").toString()) )
				{
					HashMap<String, Object> cellItem = colAr.get(_currentColPosition);
					
					Tc tableCol = makeTableCell(cellItem);
					if( tableCol != null ){
						// 먼저 rowspan 체크  
						if( cellItem.containsKey("rowSpan") ){
							TcPr tableColPr = tableCol.getTcPr();
							if( tableColPr == null ){
								tableColPr = wmlObjectFactory.createTcPr();
								tableCol.setTcPr(tableColPr);
							}
							int rowspanVal = toNum(cellItem.get("rowSpan"));
							if( rowspanVal > 1 ){
								int colspanVal = 0;
								if( cellItem.containsKey("colSpan") ){
									colspanVal = toNum(cellItem.get("colSpan"));
								}
								
								rowspanInfo = new HashMap<String, Object>();
								
								// null 값으로 들어있는 자리에 rowspan 인덱스 값으로 변경.
								rowspanInfo.put("index", rIdx+rowspanVal);
								rowspanInfo.put("border", tableColPr.getTcBorders());
								if( colspanVal > 1 ){
									rowspanInfo.put("colspan", colspanVal);
								}
								if( cellItem.containsKey("colIndex") ){
									rowspanEndIdxAr.set(toNum(cellItem.get("colIndex")), rowspanInfo);
								}
								
								VMerge rowspan = new VMerge();
								rowspan.setVal("restart"); // 병합이 시작될 셀.
								tableColPr.setVMerge(rowspan);
								
							}
						} // check rowspan
						tbRow.getContent().add(tableCol);
					} // add Tc
					
					_currentColPosition++;	// 셀 아이템포지션의 인덱스 증가
				}
				else if( rowspanEndIdxAr.get(colIdx) != null )
				{
					// 세로 병합되어있는 셀일경우 빈셀 Add
					// index = rIdx + rowspan
					Integer rowspanEndIdx = (Integer) rowspanInfo.get("index");
					if( rIdx < rowspanEndIdx ){
						// 빈 셀을 생성
						
						Tc emptyCell = wmlObjectFactory.createTc();
						TcPr emptyTcPr = wmlObjectFactory.createTcPr();
						
						// 하위셀들에도 보더속성을 다시 넣어줘야함.
						TcBorders tcBorderAr = (TcBorders) rowspanInfo.get("border");
						emptyTcPr.setTcBorders(tcBorderAr);
						
						// 빈 P 객체가 꼭 필요함. 없으면 문서형식오류 메시지가 뜸.
						P _p = wmlObjectFactory.createP();
						emptyCell.getContent().add(_p);
						
						if( rowspanInfo.containsKey("colspan") ){
							int colspanVal = toNum(rowspanInfo.get("colspan"));
							GridSpan colspan = wmlObjectFactory.createTcPrInnerGridSpan();
							colspan.setVal(BigInteger.valueOf(colspanVal));
							emptyTcPr.setGridSpan(colspan);
						}

						VMerge rowspan = new VMerge();
						rowspan.setVal("continue");
						emptyTcPr.setVMerge(rowspan);
						
						emptyCell.setTcPr(emptyTcPr);
						tbRow.getContent().add(emptyCell);
						
						// 병합이 모두 끝난경우, 해당 원소를 초기화.
						if( rowspanEndIdx - rIdx == 1 ){
							rowspanEndIdxAr.set(colIdx, null);
						}
					}
					
				}
				
				
			}
			

			table.getContent().add(tbRow);
		}
		
		return table;
	}
	/**
	 *********************초기에 rows에 있는 아이템들만 가지고 colspan, rowspan 체크한 소스.
	 *********************colIndex 값을 사용하도록 변경됨. 2016-03-11
	int rowSize = rowAr.size();
	
	ArrayList<HashMap<String, Object>> rowspanEndIdxAr = new ArrayList<HashMap<String, Object>>();
	if( _item.containsKey("columnCount") ){
		int colCnt = toNum(_item.get("columnCount"));
		rowspanEndIdxAr = new ArrayList<HashMap<String, Object>>(colCnt);
	}
	else{
		rowspanEndIdxAr = new ArrayList<HashMap<String, Object>>();
	}
	
	// rowspan이 종료되는 인덱스 정보를 저장할 배열.
	
	for (int rIdx = 0; rIdx < rowSize; rIdx++) {

		Tr tableRow = wmlObjectFactory.createTr();
		TrPr tbRowPr = wmlObjectFactory.createTrPr();
		tableRow.setTrPr(tbRowPr);
		
		ArrayList<HashMap<String, Object>> colAr = rowAr.get(rIdx);
		if( colAr == null ) continue;
		
		int colSize = colAr.size();
		for (int cIdx = 0; cIdx < colSize; cIdx++) {
			int rowspanEndIdxArSize = rowspanEndIdxAr.size();
			
			HashMap<String, Object> cellItem = colAr.get(cIdx);
			int colIndexVal = -1;
			if( cellItem.containsKey("colIndex") ){
				colIndexVal = toNum(cellItem.get("colIndex"));
			}
			
			HashMap<String, Object> rowspanInfo = null;
			if( rIdx > 0 && colIndexVal != -1 && rowspanEndIdxArSize > colIndexVal ){

				// 빈 셀의 index와 border 정보.
				rowspanInfo = rowspanEndIdxAr.get(colIndexVal);
				//rowspan 대상 셀인 경우
				if( rowspanInfo != null ){
					Integer rowspanEndIdx = (Integer) rowspanInfo.get("index");
					if( rIdx < rowspanEndIdx ){
						// 빈 셀을 생성하여 VMerge를 추가
						Tc emptyCell = wmlObjectFactory.createTc();
						TcPr emptyTcPr = wmlObjectFactory.createTcPr();
						
						// border의 경우 최상위 셀은 Top, Left 만 사용하고 나머지는 적용이 안됨. 
						// 하위셀들에도 보더속성을 다시 넣어줘야 나온다.
						TcBorders tcBorderAr = (TcBorders) rowspanInfo.get("border");
						emptyTcPr.setTcBorders(tcBorderAr);
						
						// 빈 P 객체가 꼭 필요함. 없으면 문서형식오류 메시지가 뜸.
						P _p = wmlObjectFactory.createP();
						emptyCell.getContent().add(_p);
						
						if( rowspanInfo.containsKey("colspan") ){
							int colspanVal = toNum(rowspanInfo.get("colspan"));
							GridSpan colspan = wmlObjectFactory.createTcPrInnerGridSpan();
							colspan.setVal(BigInteger.valueOf(colspanVal));
							emptyTcPr.setGridSpan(colspan);
						}

						// 병합이 되어야 할 셀 지정. restart 부터 continue가 연결되는 만큼 다 합쳐짐.
						VMerge rowspan = new VMerge();
						rowspan.setVal("continue");
						emptyTcPr.setVMerge(rowspan);
						
						emptyCell.setTcPr(emptyTcPr);
						tableRow.getContent().add(emptyCell);
						
						// 병합이 모두 끝난경우, 해당 원소를 초기화.
						if( rowspanEndIdx - rIdx == 1 ){
							rowspanEndIdxAr.set(cIdx, null);
						}
					}
				}
			}
			
			//height
			if( cellItem.containsKey("height") ){
				CTHeight ctHeight = new CTHeight();
				ctHeight.setHRule(STHeightRule.EXACT);
				ctHeight.setVal(BigInteger.valueOf((long)toNum(cellItem.get("height"))*15));
				TrHeight tblRowHeight = new TrHeight(ctHeight);
				tblRowHeight.set(tbRowPr);
			}
			
			Tc tableCol = makeTableCell(cellItem);
			if( tableCol != null ){
				// 먼저 rowspan 체크
				if( cellItem.containsKey("rowSpan") ){
					TcPr tableColPr = tableCol.getTcPr();
					if( tableColPr == null ){
						tableColPr = wmlObjectFactory.createTcPr();
						tableCol.setTcPr(tableColPr);
					}
					int rowspanVal = toNum(cellItem.get("rowSpan"));
					if( rowspanVal > 1 ){
						int colspanVal = 0;
						if( cellItem.containsKey("colSpan") ){
							colspanVal = toNum(cellItem.get("colSpan"));
						}
						// row 병합이 필요한 경우.
//						if( rIdx == 0 || rowspanEndIdxArSize <= cIdx ){
//							// index 정보 배열이 현재 cIdx보다 작을때는 추가.
//							rowspanInfo = new HashMap<String, Object>();
//							rowspanInfo.put("index", rIdx+rowspanVal);
//							rowspanInfo.put("border", tableColPr.getTcBorders());
//							if( colspanVal > 1 ){
//								rowspanInfo.put("colspan", colspanVal);
//							}
//							rowspanEndIdxAr.add(rowspanInfo);
//						}
//						else{
							// null 값으로 들어있는 자리에 rowspan 인덱스 값으로 변경.
							rowspanInfo.put("index", rIdx+rowspanVal);
							rowspanInfo.put("border", tableColPr.getTcBorders());
							if( colspanVal > 1 ){
								rowspanInfo.put("colspan", colspanVal);
							}
							if( cellItem.containsKey("colIndex") ){
								rowspanEndIdxAr.set(toNum(cellItem.get("colIndex")), rowspanInfo);
							}
//						}
						VMerge rowspan = new VMerge();
						rowspan.setVal("restart"); // 병합이 시작될 셀.
						tableColPr.setVMerge(rowspan);
					}
					else{
						// index 정보 배열이 현재 cIdx보다 작을때는 추가.
						if( rIdx == 0 || rowspanEndIdxArSize <= cIdx ){
							rowspanEndIdxAr.add(null);
						}
					}
				} // check rowspan
				
				tableRow.getContent().add(tableCol);
			} // add Tc
		} // for cIdx end
		table.getContent().add(tableRow);
	}
*/
	
	/** 
	 * <pre>
	 *  테이블 셀 아이템 생성 함수.
	 * </pre>
	 * @return Tc
	 * @throws Exception 
	 * */
	protected Tc makeTableCell( HashMap<String, Object> _item ) throws Exception {
		Tc tableCol = wmlObjectFactory.createTc();
		TcMar tblMar = wmlObjectFactory.createTcMar();
		
		TcPr tbColPr = wmlObjectFactory.createTcPr();
		tableCol.setTcPr(tbColPr);
		org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

		P p = wmlObjectFactory.createP();
		PPr ppr = wmlObjectFactory.createPPr();
		p.setPPr(ppr);
		
		// Tabel 셀의 padding값을 지정해준다.
		TblWidth _tblW = new TblWidth();
		TblWidth _tblWZero = new TblWidth();
		
		long _padding = (long) toNum(_item.get("padding"))*15;
		_tblW.setW( BigInteger.valueOf( _padding )  );
		_tblW.setType(TblWidth.TYPE_DXA);
		_tblWZero.setW( BigInteger.valueOf( 0 )   );
		_tblWZero.setType(TblWidth.TYPE_DXA);
		
		long _chkItemW = (long) toNum(_item.get("width"))*15;
		
		if( _item.get("text").equals("") && _chkItemW <= _padding*2 )
		{
			tblMar.setLeft(_tblWZero);
			tblMar.setRight(_tblWZero);
			tblMar.setTop(_tblWZero);
			tblMar.setBottom(_tblWZero);
		}
		else
		{
			tblMar.setLeft(_tblW);
			tblMar.setRight(_tblW);
			tblMar.setTop(_tblWZero);
			tblMar.setBottom(_tblWZero);
		}
		
//		if( _item.get("text").equals("") || ( _item.containsKey("textAlign") && _item.get("textAlign") != null && _item.get("textAlign").equals("center") )  )
//		{
//			tblMar.setLeft(_tblWZero);
//			tblMar.setRight(_tblWZero);
//			tblMar.setTop(_tblWZero);
//			tblMar.setBottom(_tblWZero);
//		}
//		else
//		{
//			tblMar.setLeft(_tblW);
//			tblMar.setRight(_tblW);
//			tblMar.setTop(_tblWZero);
//			tblMar.setBottom(_tblWZero);
//		}
		
		tbColPr.setTcMar(tblMar);
		
		int _fontPointSize = (_item.containsKey("fontSize")&& _item.get("fontSize")!=null)?Double.valueOf(  Math.ceil( Float.valueOf(_item.get("fontSize").toString()) * 72 / 96 * 12 / 10 ) ).intValue():0;
		float _lineHeight = 1.2f;
		
		//TODO LineHeight Test		
		if( _item.get("lineHeight") != null ){
			_lineHeight = Float.parseFloat( _item.get("lineHeight").toString());
			_fontPointSize = Double.valueOf(  Math.ceil( Float.valueOf(_item.get("fontSize").toString()) * 72 / 96 * (_lineHeight) ) ).intValue();
		}
		
		
		PPrBase.Spacing pprbasespacing = wmlObjectFactory.createPPrBaseSpacing(); 
		pprbasespacing.setAfter( ubFormToWord.BIGINTEGER_ZERO );
		pprbasespacing.setBefore( ubFormToWord.BIGINTEGER_ZERO );
		pprbasespacing.setAfterAutospacing(false);
		pprbasespacing.setBeforeAutospacing(false);
		
		pprbasespacing.setLine(BigInteger.valueOf(  SPACING_LINE * _fontPointSize )    );//180=10PT
		pprbasespacing.setLineRule(STLineSpacingRule.EXACT);
		
		//TEST
/**		if( _item.get("lineHeight") != null ){
			float _lineHeight = Float.parseFloat( _item.get("lineHeight").toString());
			float _fontPtSize = Double.valueOf(  Math.ceil( Float.valueOf(_item.get("fontSize").toString()) * 72 / 96 ) ).intValue();
			float _sc = (_fontPtSize * _lineHeight)/(_fontPtSize*2);
			float _sc2 = (1 - _sc) * _fontPtSize *15;
			_fontPtSize = _sc *240;
			
			pprbasespacing.setLine( BigInteger.valueOf( Float.valueOf(_fontPtSize).longValue()  )    );//180=10PT
			pprbasespacing.setLineRule(STLineSpacingRule.AUTO);
		}*/	
		
		ppr.setSpacing(pprbasespacing); 
		
		// Create object for r
		R r = wmlObjectFactory.createR();
		p.getContent().add(r);
		// Create object for rPr
		RPr rpr = wmlObjectFactory.createRPr();
		r.setRPr(rpr);
		
		// 단락뒤에 공백 제거
		CTSignedTwipsMeasure space = new CTSignedTwipsMeasure();
		space.setVal(ubFormToWord.BIGINTEGER_ZERO);
		rpr.setSpacing(space);
		
		if( _item.containsKey("colSpan") ){
			int colspanVal = toNum(_item.get("colSpan"));
			if( colspanVal > 1 ){
				GridSpan colspan = wmlObjectFactory.createTcPrInnerGridSpan();
				colspan.setVal(BigInteger.valueOf(colspanVal));
				tbColPr.setGridSpan(colspan);
			}
		}
		
		//textAlign
		if( _item.containsKey("textAlign") && _item.get("textAlign") != null && _item.get("textAlign").equals("null") == false  ){
			Jc justification = wmlObjectFactory.createJc();
			
			switch ( ETextAlign.valueOf(String.valueOf(_item.get("textAlign")))) {
			case left: justification.setVal(JcEnumeration.LEFT);break;
			case center: justification.setVal(JcEnumeration.CENTER); break;
			case right: justification.setVal(JcEnumeration.RIGHT); break;
			}
			
			ppr.setJc(justification);
		}
		
		// word단위일경우 true 아닐경우 false  - 20180102 최명진
		BooleanDefaultTrue _bt = new BooleanDefaultTrue();
		_bt.setVal(false);
		ppr.setWordWrap(_bt);
		
		if( _item.containsKey("text") ){
			
			String itemTxt = String.valueOf(_item.get("text"));
			//itemTxt = URLDecoder.decode(itemTxt, "UTF-8");
			// 줄바꿈 처리
			itemTxt = itemTxt.replace("\\n", "\n").replaceAll("\\r", "\r");
			
			// 줄바꿈 문자가 들어있는 경우.
			if(  itemTxt.indexOf("\n") != -1 ){
				String[] splitTxt = itemTxt.split("\n");
				int splitSize = splitTxt.length;
				List<Object> rContent = r.getContent();
				for (int i = 0; i < splitSize; i++) {
					
					Text text = wmlObjectFactory.createText();
					text.setValue( splitTxt[i] );
					text.setSpace("preserve");
					rContent.add(text);
					
					// 마지막 문자가 아닌경우, 줄바꿈 추가.
					if( i != (splitSize-1) ){
						Br breakObj = new Br();
						rContent.add(breakObj);
					}
					
				}// end For
			}
			// 줄바꿈 없는 경우.
			else{
				Text text = wmlObjectFactory.createText();
				r.getContent().add(text);
				text.setValue( itemTxt );
				text.setSpace("preserve");
			}
		}
		/* 
		 * http://blog.iprofs.nl/2012/11/19/adding-layout-to-your-docx4j-generated-word-documents-part-2/
		 * */
		
		//fontWeight
		if( _item.containsKey("fontWeight") ){
			BooleanDefaultTrue b = new BooleanDefaultTrue();
		    
			switch (EFontWeight.valueOf(String.valueOf(_item.get("fontWeight")))) {
			case normal: b.setVal(false);rpr.setB(b); break;
			case bold: b.setVal(true);rpr.setB(b); break;
			}
		}
		//fontStyle
		if( _item.containsKey("fontStyle") ){
			if( String.valueOf(_item.get("fontStyle")).equals("italic") ){
				BooleanDefaultTrue isItalic = new BooleanDefaultTrue();
				isItalic.setVal(true);
				rpr.setI(isItalic);
			}
		}
		//fontSize
		if( _item.containsKey("fontSize") ){
//			long fontSize = (long) (toNum(_item.get("fontSize"))*1.5);
			long fontSize = (long) ( Float.valueOf(_item.get("fontSize").toString()) * 72 / 96 )*2;
			HpsMeasure size = new HpsMeasure();
	        size.setVal(BigInteger.valueOf(fontSize));
	        rpr.setSz(size);
	        
		}
		//fontFamily
		if( _item.containsKey("fontFamily") ){
			RFonts runFont = new RFonts();
			String fname = getFontName(String.valueOf(_item.get("fontFamily")));
	        runFont.setAscii( fname );
	        runFont.setHAnsi( fname );
	        runFont.setEastAsia(fname);
	        rpr.setRFonts(runFont);
		}
		//textDecoration
		if( _item.containsKey("textDecoration") ){
			U underline = new U();
			switch (ETextDecoration.valueOf(String.valueOf(_item.get("textDecoration")))) {
			case none: underline.setVal(UnderlineEnumeration.NONE); rpr.setU(underline); break;
			case normal: underline.setVal(UnderlineEnumeration.NONE); rpr.setU(underline); break;
			case underline: underline.setVal(UnderlineEnumeration.SINGLE); rpr.setU(underline); break;
			}
		}
		//fontColor
		if( _item.containsKey("fontColor") ){
			Color c = new Color();
			c.setVal(String.valueOf(_item.get("fontColor")).replace("#", ""));
			rpr.setColor(c);
		}
		
		if( _lineHeight > 1.2f )
		{
			TextAlignment _txtAligment = ppr.getTextAlignment();
			if( _txtAligment == null ){
				_txtAligment = wmlObjectFactory.createPPrBaseTextAlignment();
			}
			
			_txtAligment.setVal("bottom");
			ppr.setTextAlignment(_txtAligment);
		}
		// Border : color, type, width 체크
		
		// http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/tcBorders.html
		TcBorders tcBorderAr = wmlObjectFactory.createTcPrInnerTcBorders();
		CTBorder top = new CTBorder();
		CTBorder left = new CTBorder();
		CTBorder bottom = new CTBorder();
		CTBorder right = new CTBorder();
		
		String bdSide = String.valueOf(_item.get("borderSide"));
		String bdType = "solid";
		if( bdSide.equals("null") || bdSide.equals("[]")){
			bdType = "none";
			top.setVal(STBorder.NONE);
			left.setVal(STBorder.NONE);
			bottom.setVal(STBorder.NONE);
			right.setVal(STBorder.NONE);
		}
		else{
			// isCell = true. 속성이 배열. 첫번째 원소의 값으로 border를 지정한다.
			
			// 테두리는 TOP의 타입을 따른다.
			bdSide = bdSide.replace("[", "");
			bdSide = bdSide.replace("]", "");
			bdSide = bdSide.replaceAll(" ", "");
			ArrayList<String> bdSideAr = new ArrayList<String>(Arrays.asList(bdSide.split(",")));
			int bdTopIdx = bdSideAr.indexOf("top");
			// TOP이 없는 경우 LEFT를 가져온다. right, bottom 은 없는 경우가 많으므로 따로 체크하진 않는다.
			if( bdTopIdx == -1 ) bdTopIdx = bdSideAr.indexOf("left");
			
			// 각속성을 저장할 배열들.
			String[] bdTypeAr = null;
			String[] bdColorAr = null;
			String[] bdWidthAr = null;
			
			//borderTypes
			String bdTypes = String.valueOf(_item.get("borderTypes"));
			if( !bdTypes.equals("null") && !bdTypes.equals("") ){
				bdTypes = bdTypes.replace("[", "");
				bdTypes = bdTypes.replace("]", "");
				bdTypes = bdTypes.replaceAll(" ", "");
				bdTypeAr = bdTypes.split(",");
			}
			if( !bdType.equals("none") ){
				/*
				 * borderColors
				 * word문서.xml 
				 * <v:shape id="_x0000_s1083" type="#_x0000_t202" style="~~" strokecolor="#5f497a [2407]">
				 * red, blue 등 문자형도 인식함. (종류를 다 판단하지는 못함.. red, blue, green 확인.	
				 */
				String bdColors = String.valueOf(_item.get("borderColors"));
				if( !bdColors.equals("null") && !bdColors.equals("")  ){
					bdColors = bdColors.replace("[", "");
					bdColors = bdColors.replace("]", "");
					bdColors = bdColors.replaceAll(" ", "");
					bdColorAr = bdColors.split(",");
				}
				//borderWidths
				String bdWidths = String.valueOf(_item.get("borderWidths"));
				if( !bdWidths.equals("null") && !bdWidths.equals("") ){
					bdWidths = bdWidths.replace("[", "");
					bdWidths = bdWidths.replace("]", "");
					bdWidths = bdWidths.replaceAll(" ", "");
					bdWidthAr = bdWidths.split(",");
				}
			}

			if( bdColorAr != null && bdTypeAr != null ){
				for (int i = 0; i < bdSideAr.size(); i++) {
					String _side = bdSideAr.get(i);
					
					int _borderSize = ( (Integer.parseInt(bdWidthAr[i]) -1) * 4 )+ 8;
					
					if(_borderSize < 1 ) _borderSize = 8;
					
					if( "top".equals(_side) ){
						top.setVal(getSTBorder(bdTypeAr[i], Integer.parseInt(bdWidthAr[i])));
						top.setColor(bdColorAr[i]);
						top.setSz( new BigInteger( String.valueOf( _borderSize ) ) );
					}
					else if( "left".equals(_side) ){
						left.setVal(getSTBorder(bdTypeAr[i], Integer.parseInt(bdWidthAr[i])));
						left.setColor(bdColorAr[i]);
						left.setSz( new BigInteger( String.valueOf( _borderSize ) ) );
					}
					else if( "bottom".equals(_side) ){
						bottom.setVal(getSTBorder(bdTypeAr[i], Integer.parseInt(bdWidthAr[i])));
						bottom.setColor(bdColorAr[i]);
						bottom.setSz( new BigInteger( String.valueOf( _borderSize ) ) );
					}
					else if( "right".equals(_side) ){
						right.setVal(getSTBorder(bdTypeAr[i], Integer.parseInt(bdWidthAr[i])));
						right.setColor(bdColorAr[i]);
						right.setSz( new BigInteger( String.valueOf( _borderSize ) ) );
					}
				}
			}
			else{
				//log.debug(getClass().getName()+"::>>>>>>>>>> border value is wrong : \nbdColorAr" +bdColorAr+", bdTypeAr : "+bdTypeAr);
			}
		}

		tcBorderAr.setTop(top);
		tcBorderAr.setLeft(left);
		tcBorderAr.setBottom(bottom);
		tcBorderAr.setRight(right);
		
		tbColPr.setTcBorders(tcBorderAr);
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			CTVerticalJc valign = new CTVerticalJc();
			switch (EVerticalAlign.valueOf(String.valueOf(_item.get("verticalAlign")))) {
			case top:	 valign.setVal(STVerticalJc.TOP);break;
			case middle: valign.setVal(STVerticalJc.CENTER);break;
			case bottom: valign.setVal(STVerticalJc.BOTTOM);break;
			default:	 valign.setVal(STVerticalJc.TOP);break;
			}
			tbColPr.setVAlign(valign);
		}
		//width
		if( _item.get("width") != null ){
			TblWidth tblWidth = new TblWidth();
			tblWidth.setW(BigInteger.valueOf((long) toNum(_item.get("width"))*15));
			tblWidth.setType( TblWidth.TYPE_DXA ); // http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/tblW_1.html
			tbColPr.setTcW(tblWidth);
		}
		//backgroundColor
		if( _item.get("backgroundAlpha") != null ){
			float bgAlpha = Float.valueOf(String.valueOf(_item.get("backgroundAlpha")));
			// 셀에는 알파값 적용 불가능. bgAlpha 가 0인지만 체크. 
			if( bgAlpha > 0 ){
				if( _item.containsKey("backgroundColor") ){
					CTShd _shd = new CTShd();
					String colorHex = String.valueOf(_item.get("backgroundColor")).replace("#", "");
					_shd.setFill(colorHex);
					tbColPr.setShd(_shd);
				}
			}
		}
		
		tableCol.getContent().add(p);
		
		return tableCol;
		
	}
	/**
	 * <pre>
	 *  SVG태그 형식의 텍스트 아이템 생성 함수.
	 * </pre>
	 * @return R
	 * @throws Exception 
	 * 
	*/
	protected R makeSVGText(HashMap<String, Object> _item) throws Exception{

		log.debug(getClass().getName() + "::makeSVGText...");

		ObjectFactory vmlObjectFactory = new ObjectFactory();

		R resultR = wmlObjectFactory.createR();
		RPr resultRpr = wmlObjectFactory.createRPr();
		resultR.setRPr(resultRpr);
		Pict pict = wmlObjectFactory.createPict();
		resultR.getContent().add(pict);
		
		CTShape shape = vmlObjectFactory.createCTShape();
		shape.setType("#_x0000_t202");
		shape.setStroked(STTrueFalse.F);
		JAXBElement<CTShape> shapeWrapped = vmlObjectFactory.createShape(shape);
		pict.getAnyAndAny().add(shapeWrapped);
		
		CTTextbox textbox = vmlObjectFactory.createCTTextbox();
		JAXBElement<CTTextbox> textboxWrapped = vmlObjectFactory.createTextbox(textbox);
		shape.getEGShapeElements().add(textboxWrapped);
		CTTxbxContent txbxcontent = wmlObjectFactory.createCTTxbxContent();
		textbox.setTxbxContent(txbxcontent);
		textbox.setInset("0,0,0,0");

		// 도형의 style 문자열로 속성 세팅.
		String styles = "position:absolute;" +
				"visibility:visible;" +
				"padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt;" +
				"mso-position-horizontal:absolute;" +
				"mso-position-vertical:absolute";

		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			styles += ";v-text-anchor:"+String.valueOf(_item.get("verticalAlign"));
		}
		//width
		if( _item.get("width") != null ){
			styles += ";width:"+toCM(_item.get("width"))+"cm";
		}
		//height
		if( _item.get("height") != null ){
			styles += ";height:"+toCM(_item.get("height"))+"cm";
		}
		//x
		if( _item.get("x") != null ){
			styles += ";margin-left:"+toPointStr(_item.get("x"))+"pt";
		}
		//y
		if( _item.get("y") != null ){
			styles += ";margin-top:"+toPointStr(_item.get("y"))+"pt";
		}

		//backgroundColor
		if( _item.get("backgroundAlpha") != null ){
			String bgAlpha = String.valueOf(_item.get("backgroundAlpha"));
			if( bgAlpha.equals("0") ){
				shape.setFilled(STTrueFalse.F);
			}
			else{
				if( _item.get("backgroundColorInt") != null ){
					shape.setFillcolor(changeColorToRGB(Integer.valueOf(String.valueOf(_item.get("backgroundColorInt")))));

					// 기본 속성
					if( !bgAlpha.equals("1") ){
						CTFill fill = vmlObjectFactory.createCTFill();
						fill.setOpacity(bgAlpha);
						JAXBElement<CTFill> fillWrapped = vmlObjectFactory.createFill(fill); 
						shape.getEGShapeElements().add(fillWrapped);
					}
				}
			}
		}
		
		shape.setStyle(styles);

		/*
		String _dataStr = URLDecoder.decode( String.valueOf(_item.get("data")) , "UTF-8");
		_dataStr = _dataStr.replaceAll("%20", " ");
		log.debug(getClass().getName() + "::makeSVGText\n"+_dataStr);
		
		*/
		
		// 현재는 사이즈 무조건 1.
		int lineGap = 10;
		try {
			lineGap = Integer.parseInt(String.valueOf(_item.get("lineGap")));
		} catch (Exception e) {
			// integer 변환 에러시 default로 사용.
		}
		
		ArrayList<ArrayList<HashMap<String, Object>>> lineAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("children");
		for (ArrayList<HashMap<String, Object>> textAr : lineAr) {
			txbxcontent.getContent().add( makeSVGTspan(textAr,lineGap) );
//			for (HashMap<String, Object> textItem : textAr) {
//				txbxcontent.getContent().add( makeSVGTspan(textItem) );
//			}
		}
		
		return resultR;
	}
	
	/**
	 * <pre>
	 *  SVG태그 형식의 텍스트 아이템 내부의 span을 각각 생성하는 함수.
	 * </pre>
	 * @return R
	 * @throws Exception 
	 * 
	 */
	protected P makeSVGTspan(ArrayList<HashMap<String, Object>> textAr, int lineGap ) throws Exception{
		
		// Create object for p
		P resultP = wmlObjectFactory.createP();
		// Create object for pPr
		PPr ppr = wmlObjectFactory.createPPr();
		resultP.setPPr(ppr);
		
//		PPrBase.Spacing pprbasespacing = wmlObjectFactory.createPPrBaseSpacing(); 
//		pprbasespacing.setAfter( BIGINTEGER_ZERO );
//		pprbasespacing.setBefore( BIGINTEGER_ZERO );
//		pprbasespacing.setAfterAutospacing(false);
//		pprbasespacing.setBeforeAutospacing(false);
//		pprbasespacing.setLine(BigInteger.valueOf(240));
//		pprbasespacing.setLineRule(STLineSpacingRule.AUTO);
//		ppr.setSpacing(pprbasespacing); 
		
        PPrBase.Spacing pprbasespacing = wmlObjectFactory.createPPrBaseSpacing(); 
		pprbasespacing.setAfter( ubFormToWord.BIGINTEGER_ZERO );
		pprbasespacing.setBefore( ubFormToWord.BIGINTEGER_ZERO );
		pprbasespacing.setAfterAutospacing(false);
		pprbasespacing.setBeforeAutospacing(false);
//		pprbasespacing.setLine(BigInteger.valueOf(SPACING_LINE * _fontPointSize));
//		pprbasespacing.setLine(BigInteger.valueOf(225));
		pprbasespacing.setLineRule(STLineSpacingRule.EXACT);
		ppr.setSpacing(pprbasespacing); 
		
		
		//------------tspan에 따라 여러개의 R생성 필요(Strong 적용)
		for (HashMap<String, Object> _item : textAr) {
			R r = wmlObjectFactory.createR();
			resultP.getContent().add(r);
			RPr rpr = wmlObjectFactory.createRPr();
			r.setRPr(rpr);
			
			CTSignedTwipsMeasure space = new CTSignedTwipsMeasure();
			space.setVal(ubFormToWord.BIGINTEGER_ZERO);
			rpr.setSpacing(space);
			/*
			//indent ------------임시. 추후 indent 속성값이 들어오는 경우 변경.
			if( false || _item.get("indent") != null ){
				int indent = 1;
//				int indent = Integer.valueOf(String.valueOf(_item.get("indent")));
				NumPr numpr = (ppr.getNumPr() == null)? new NumPr() : ppr.getNumPr();
				Ilvl ilvl = new Ilvl();
				ilvl.setVal(BigInteger.valueOf(indent)); //Depth
				numpr.setIlvl(ilvl);
				ppr.setNumPr(numpr);
			}
			// 글머리 기호
			if( _item.get("bullet") != null ){
				// ------------ Maybe 글머리 기호
				PStyle pstyle = new PStyle();
				pstyle.setVal("a3");
				ppr.setPStyle(pstyle);
				
//				사용하려면 정의(?)하는 태그를 추가해줘야할듯.
				NumPr numpr = (ppr.getNumPr() == null)? new NumPr() : ppr.getNumPr();
				NumId numid = new NumId();
				numid.setVal(BigInteger.valueOf(2)); // ■
				numid.setVal(BigInteger.valueOf(1)); // 가
				numid.setVal(BigInteger.valueOf(3)); // □
				numpr.setNumId(numid);
				Ind ind = new Ind();
				ind.setLeftChars(BIGINTEGER_ZERO);
			}
			*/
			//lineGap ------------임시. 추후 lineGap 속성값이 들어오는 경우 사용.
			if( _item.get("lineGap") != null ){
				pprbasespacing.setAfter( BigInteger.valueOf(toNum(_item.get("lineGap"))) );
			}
			if( _item.get("text") != null ){
				String itemTxt = String.valueOf(_item.get("text"));
				Text text = wmlObjectFactory.createText();
				text.setSpace("preserve");
				if( itemTxt.trim().equals("") ){
					// 공백인 경우 이걸 써줘야 나타남.
				}
				r.getContent().add(text);
				text.setValue( itemTxt );
			}
			//fontWeight
			if( _item.get("fontWeight") != null ){
				BooleanDefaultTrue b = new BooleanDefaultTrue();
			    
				switch (EFontWeight.valueOf(String.valueOf(_item.get("fontWeight")))) {
				case normal: b.setVal(false);rpr.setB(b); break;
				case bold: b.setVal(true);rpr.setB(b); break;
				}
			}
			//fontSize
			if( _item.get("fontSize") != null ){
//				long fontSize = (long) (toNum(_item.get("fontSize"))*1.5);
				long fontSize = (long) ( Float.valueOf(_item.get("fontSize").toString()) * 72 / 96 )*2;
				HpsMeasure size = new HpsMeasure();
		        size.setVal(BigInteger.valueOf(fontSize));
		        rpr.setSz(size);
		        /*
		        	lineHeight
		        	우선 fontsize 10pt, linegap 10px을 기준으로 뷰어화면과 가장 비슷하게 맞춤. 단위 환산이 정확하지않음...
		        	size는 fontsize 포함한 gap 으로 추정됨. 폰트사이즈를 더해줘야함.
		        	200==>10pt / 400==>20pt 로 값이 들어감.
		        */
		       long _gap =  Double.valueOf( Math.ceil( ( (fontSize/2) * SPACING_LINE) + (lineGap  * 72 / 96 * SPACING_LINE ))).intValue();
//		       long _gap =  Double.valueOf( Math.ceil( lineGap  * 72 / 96 * SPACING_LINE )).intValue();
		        
//		        pprbasespacing.setLine(BigInteger.valueOf(fontSize*10+lineGap*15));
		        pprbasespacing.setLine(BigInteger.valueOf( _gap ));
//		        pprbasespacing.setLine(BigInteger.valueOf(fontSize*10));
			}
			//fontFamily
			if( _item.get("fontFamily") != null ){
				RFonts runFont = new RFonts();
				String fname = getFontName(String.valueOf(_item.get("fontFamily")));
//				fname = URLDecoder.decode(fname, "UTF-8");
		        runFont.setAscii( fname );
		        runFont.setHAnsi( fname );
		        runFont.setEastAsia(fname);
		        rpr.setRFonts(runFont);
			}
			//--------------------Prudential 은 사용안함.
			//fontStyle
			if( _item.get("fontStyle") != null ){
				if( String.valueOf(_item.get("fontStyle")).equals("italic") ){
					BooleanDefaultTrue isItalic = new BooleanDefaultTrue();
					isItalic.setVal(true);
					rpr.setI(isItalic);
				}
			}
			//textAlign
			if( _item.get("textAlign") != null ){
				Jc justification = wmlObjectFactory.createJc();
				
				switch ( ETextAlign.valueOf(String.valueOf(_item.get("textAlign")))) {
				case left: justification.setVal(JcEnumeration.LEFT);break;
				case center: justification.setVal(JcEnumeration.CENTER); break;
				case right: justification.setVal(JcEnumeration.RIGHT); break;
				}
				
				ppr.setJc(justification);
			}
			//textDecoration
			if( _item.get("textDecoration") != null ){
				U underline = new U();
				switch (ETextDecoration.valueOf(String.valueOf(_item.get("textDecoration")))) {
				case none: underline.setVal(UnderlineEnumeration.NONE); rpr.setU(underline); break;
				case normal: underline.setVal(UnderlineEnumeration.NONE); rpr.setU(underline); break;
				case underline: underline.setVal(UnderlineEnumeration.SINGLE); rpr.setU(underline); break;
				}
			}
			//fontColor
			if( _item.get("fontColor") != null ){
				Color c = new Color();
				c.setVal(String.valueOf(_item.get("fontColor")).replace("#", ""));
				rpr.setColor(c);
			}
		}

		return resultP;
	}
	
	
	/**
	 * 원 아이템 생성
	*/
	protected R makeCircle( HashMap<String, Object> _item ) throws Exception {
		
		ObjectFactory vmlObjectFactory = new ObjectFactory();
		
		R resultR = wmlObjectFactory.createR();
		RPr resultRpr = wmlObjectFactory.createRPr();
		resultR.setRPr(resultRpr);
		Pict pict = wmlObjectFactory.createPict();
		resultR.getContent().add(pict);
		
		CTOval oval = vmlObjectFactory.createCTOval();
		JAXBElement<CTOval> ovalWrapped = vmlObjectFactory.createOval(oval); 
		pict.getAnyAndAny().add(ovalWrapped);
		
		// 도형의 style 문자열로 속성 세팅.
		String styles = "position:absolute;" +
				"visibility:visible;" +
				"padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt;" +
				"mso-position-horizontal:absolute;" +
				"mso-position-vertical:absolute";
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			styles += ";v-text-anchor:"+String.valueOf(_item.get("verticalAlign"));
		}
		//width
		if( _item.get("width") != null ){
			styles += ";width:"+toCM(_item.get("width"))+"cm";
		}
		//height
		if( _item.get("height") != null ){
			styles += ";height:"+toCM(_item.get("height"))+"cm";
		}
		//x
		if( _item.get("x") != null ){
			styles += ";margin-left:"+toPointStr(_item.get("x"))+"pt";
		}
		//y
		if( _item.get("y") != null ){
			styles += ";margin-top:"+toPointStr(_item.get("y"))+"pt";
		}
		if( _item.get("isBackground") != null ){
			boolean isBackground = Boolean.valueOf(String.valueOf(_item.get("isBackground")));
			if(isBackground){
				styles += ";z-index:-10";
			}
		}
		
		if( _item.get("contentBackgroundAlpha") != null ){
			String bgAlpha = String.valueOf(_item.get("contentBackgroundAlpha")).replace("0.", ".");
			if( bgAlpha.equals("0") ){
				oval.setFilled(STTrueFalse.F);
			}
			else{
				//투명도 추가
				CTFill fill = vmlObjectFactory.createCTFill();
				fill.setOpacity(bgAlpha);
				JAXBElement<CTFill> fillWrapped = vmlObjectFactory.createFill(fill); 
				oval.getEGShapeElements().add(fillWrapped);
				
				// backgroundColor
				if( _item.get("contentBackgroundColorInt") != null ){
					oval.setFillcolor(changeColorToRGB(Integer.valueOf(String.valueOf(_item.get("contentBackgroundColorInt")))));
					oval.setOpacity(bgAlpha);
				}
			}
		}
		
		if( _item.get("borderAlpha") != null ){
			String _borderAlpha = String.valueOf(_item.get("borderAlpha")).replace("0.", "."); if( _item.get("borderAlpha") != null )			
			//borderColor
			if( _borderAlpha.equals("0") ){
				oval.setStroked(STTrueFalse.F);				
				
			}else{
				if( _item.get("borderColorInt") != null ){
					oval.setStrokecolor( changeColorToRGB(Integer.valueOf(String.valueOf(_item.get("borderColorInt")))));
					//word에선 border alpha값을 설정 할 수 없음
					//oval.setOpacity(_borderAlpha);
				}
			}
		}	
		
		
		//strokeWidth
		if( _item.get("strokeWidth") != null ){
			oval.setStrokeweight(toPointStr(_item.get("strokeWidth"))+"pt");
		}
		
		oval.setStyle(styles);
		
		return resultR;
	}
	
	/**
	 * 사각형 아이템 생성
	 */
	protected R makeRect( HashMap<String, Object> _item ) throws Exception {
		
		ObjectFactory vmlObjectFactory = new ObjectFactory();
		
		R resultR = wmlObjectFactory.createR();
		RPr resultRpr = wmlObjectFactory.createRPr();
		resultR.setRPr(resultRpr);
		Pict pict = wmlObjectFactory.createPict();
		resultR.getContent().add(pict);
		
		CTRect rect = vmlObjectFactory.createCTRect();
		JAXBElement<CTRect> rectWrapped = vmlObjectFactory.createRect(rect); 
		pict.getAnyAndAny().add(rectWrapped);
		
		// 도형의 style 문자열로 속성 세팅.
		String styles = "position:absolute;" +
				"visibility:visible;" +
				"padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt;" +
				"mso-position-horizontal:absolute;" +
				"mso-position-vertical:absolute";
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			styles += ";v-text-anchor:"+String.valueOf(_item.get("verticalAlign"));
		}
		//width
		if( _item.get("width") != null ){
			styles += ";width:"+toCM(_item.get("width"))+"cm";
		}
		//height
		if( _item.get("height") != null ){
			styles += ";height:"+toCM(_item.get("height"))+"cm";
		}
		//x
		if( _item.get("x") != null ){
			styles += ";margin-left:"+toPointStr(_item.get("x"))+"pt";
		}
		//y
		if( _item.get("y") != null ){
			styles += ";margin-top:"+toPointStr(_item.get("y"))+"pt";
		}
		//z-index
		if( _item.get("isBackground") != null ){
			boolean isBackground = Boolean.valueOf(String.valueOf(_item.get("isBackground")));
			if(isBackground){
				styles += ";z-index:-10";
			}
		}
		
		// backgroundAlpha
		if( _item.get("contentBackgroundAlpha") != null ){		
			
			String bgAlpha = String.valueOf(_item.get("contentBackgroundAlpha")).replace("0.", ".");
			
			if(_item.get("className").equals("UBGraphicsRectangle")){
				if( bgAlpha.equals("0") ){
					rect.setFilled(STTrueFalse.F);
				}
				else{
					
					CTFill fill = vmlObjectFactory.createCTFill();
					fill.setOpacity(bgAlpha);
					JAXBElement<CTFill> fillWrapped = vmlObjectFactory.createFill(fill); 
					rect.getEGShapeElements().add(fillWrapped);
					
					/*
					 * TODO background 색상에 대한 alpha 로 처리 필요.
					 * backgroundAlpha <w:pict><v:rect><v:fill opacity="32896f(50%)"/></v:rect></w:pict> 이 구조로 만들어져야함.
//					CTFill fill = vmlObjectFactory.createCTFill();
//					fill.setOpacity(bgAlpha);
					*/
					// backgroundColor
					if( _item.get("contentBackgroundColorInt") != null ){
						rect.setFillcolor(changeColorToRGB(Integer.valueOf(String.valueOf(_item.get("contentBackgroundColorInt")))));
						rect.setOpacity(bgAlpha);
					}
				}
			}else{
//				String alphaArStr = String.valueOf(_item.get("contentBackgroundAlphas"));
//				alphaArStr = alphaArStr.replace("[", "");
//				alphaArStr = alphaArStr.replace("]", "");
//				alphaArStr = alphaArStr.replaceAll(" ", "");
//				String[] gradientalpha = alphaArStr.split(",");
				
				CTPath path = vmlObjectFactory.createCTPath();
				path.setGradientshapeok(STTrueFalse.T);

				CTFill fill = vmlObjectFactory.createCTFill();
				String[] gradient = null;
				if( _item.get("contentBackgroundColorsInt") != null ){
					String colorArStr = String.valueOf(_item.get("contentBackgroundColorsInt"));
					colorArStr = colorArStr.replace("[", "");
					colorArStr = colorArStr.replace("]", "");
					colorArStr = colorArStr.replaceAll(" ", "");
					gradient = colorArStr.split(",");
				}
				
//				if( gradientalpha.length < 2 ){
//					log.error(getClass().getName()+"::makeRect::"+">>>>>>>>>>>>>>>>>>>>contentBackgroundAlphas value is wrong!");
//				}
//				else{
					// 첫번째 색
					if(bgAlpha.equals("0")){
						rect.setFilled(STTrueFalse.F);
					}
					else{
						rect.setFillcolor(changeColorToRGB(Integer.valueOf(gradient[0].replace("#", ""))));
						
						// 기본 속성
						fill.setRecolor(STTrueFalse.T);
						fill.setRotate(STTrueFalse.T);
						fill.setFocus("100%");
						fill.setFocusposition("1");
						fill.setType(STFillType.GRADIENT);
						fill.setMethod(STFillMethod.LINEAR_SIGMA);
						if( !bgAlpha.equals("1") ) fill.setOpacity(bgAlpha);
					}
					// 두번째 색
					if(bgAlpha.equals("0")){
						fill.setOpacity2("1");
					}
					else{
						if( !bgAlpha.equals("1") ) fill.setOpacity2(bgAlpha);
						fill.setColor2(changeColorToRGB(Integer.valueOf(gradient[1])));
					}
					
//				}
				JAXBElement<CTFill> fillWrapped = vmlObjectFactory.createFill(fill); 
				rect.getEGShapeElements().add(fillWrapped);
			}			
		}
		// gradient
		// backgroundAlpha : Word 의 alpha는 반대. 1이면투명. 0이면 불투명.
		else if( _item.get("contentBackgroundAlphas") != null ){
			String alphaArStr = String.valueOf(_item.get("contentBackgroundAlphas"));
			alphaArStr = alphaArStr.replace("[", "");
			alphaArStr = alphaArStr.replace("]", "");
			alphaArStr = alphaArStr.replaceAll(" ", "");
			String[] gradientalpha = alphaArStr.split(",");
			
			CTPath path = vmlObjectFactory.createCTPath();
			path.setGradientshapeok(STTrueFalse.T);

			CTFill fill = vmlObjectFactory.createCTFill();
			String[] gradient = null;
			if( _item.get("contentBackgroundColorsInt") != null ){
				String colorArStr = String.valueOf(_item.get("contentBackgroundColorsInt"));
				colorArStr = colorArStr.replace("[", "");
				colorArStr = colorArStr.replace("]", "");
				colorArStr = colorArStr.replaceAll(" ", "");
				gradient = colorArStr.split(",");
			}
			
			if( gradientalpha.length < 2 ){
				log.error(getClass().getName()+"::makeRect::"+">>>>>>>>>>>>>>>>>>>>contentBackgroundAlphas value is wrong!");
			}
			else{
				// 첫번째 색
				if(gradientalpha[0].equals("0")){
					rect.setFilled(STTrueFalse.F);
				}
				else{
					rect.setFillcolor(changeColorToRGB(Integer.valueOf(gradient[0].replace("#", ""))));
					
					// 기본 속성
					fill.setRecolor(STTrueFalse.T);
					fill.setRotate(STTrueFalse.T);
					fill.setFocus("100%");
					fill.setFocusposition("1");
					fill.setType(STFillType.GRADIENT);
					fill.setMethod(STFillMethod.LINEAR_SIGMA);
					if( !gradientalpha[0].equals("1") ) fill.setOpacity(gradientalpha[0]);
				}
				// 두번째 색
				if(gradientalpha[1].equals("0")){
					fill.setOpacity2("1");
				}
				else{
					if( !gradientalpha[1].equals("1") ) fill.setOpacity2(gradientalpha[1]);
					fill.setColor2(changeColorToRGB(Integer.valueOf(gradient[1])));
				}
				
			}
			JAXBElement<CTFill> fillWrapped = vmlObjectFactory.createFill(fill); 
			rect.getEGShapeElements().add(fillWrapped);
		}
		
		if( _item.get("borderAlpha") != null ){
			String _borderAlpha = String.valueOf(_item.get("borderAlpha")).replace("0.", "."); if( _item.get("borderAlpha") != null )			
			//borderColor
			if( _borderAlpha.equals("0") ){
				rect.setStroked(STTrueFalse.F);	
			}else{
				//borderColor
				if( _item.get("borderColorInt") != null ){
					rect.setStrokecolor(changeColorToRGB(Integer.valueOf(String.valueOf(_item.get("borderColorInt")))));
				}				
			}
		}	
		
		
		//strokeWidth
		if( _item.get("borderThickness") != null ){
			rect.setStrokeweight(toPointStr(_item.get("borderThickness"))+"pt");
		}
		
		rect.setStyle(styles);
		
		return resultR;
	}
	
	/**
	 * 라인 아이템 생성
	 */
	protected R makeLine( HashMap<String, Object> _item ) throws Exception {
		
		ObjectFactory vmlObjectFactory = new ObjectFactory();
		
		R resultR = wmlObjectFactory.createR();
		RPr resultRpr = wmlObjectFactory.createRPr();
		resultR.setRPr(resultRpr);
		Pict pict = wmlObjectFactory.createPict();
		resultR.getContent().add(pict);
		
		CTShape shape = vmlObjectFactory.createCTShape();
		JAXBElement<CTShape> shapeWrapped = vmlObjectFactory.createShape(shape);
		pict.getAnyAndAny().add(shapeWrapped);
		
		shape.setType("#_x0000_t32");
		
		// 도형의 style 문자열로 속성 세팅.
		String styles = "position:absolute;" +
				"visibility:visible;" +
				"padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt;" +
				"mso-position-horizontal:absolute;" +
				"mso-position-vertical:absolute";
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			styles += ";v-text-anchor:"+String.valueOf(_item.get("verticalAlign"));
		}
		double xPt = toPoint( _item.get("x") );
		//x
		if( xPt != -1 ){
			styles += ";margin-left:"+xPt+"pt";
		}
		double yPt = toPoint( _item.get("y") );
		//y
		if( yPt != -1 ){
			styles += ";margin-top:"+yPt+"pt";
		}
		
		//width
		double x1 = toPoint(_item.get("x1"));
		double x2 = toPoint(_item.get("x2"));
		if( _item.get("x2") != null ){
			styles += ";width:"+( toPoint(_item.get("x2")) )+"pt";
		}
		double w = Math.abs(x2-x1); 
		
		//height
		double y1 = toPoint(_item.get("y1"));
		double y2 = toPoint(_item.get("y2"));
		double h = Math.abs(y2-y1); 
//		if( x1 < x2){
//			styles += ";width:"+w+"pt";
//			styles += ";height:"+h+"pt";
//		}
//		else{
//			styles += ";width:"+w+"pt";
//			styles += ";height:"+h+"pt;flip:y"; // ↗ 인 경우.
//		}
		
		if( (x1 > x2 && y1 < y2) || ( y1 > y2 && x2 > x1 )  )
		{
			styles += ";width:"+w+"pt";
			styles += ";height:"+h+"pt;flip:x"; // ↗ 인 경우.
		}
		else
		{
			styles += ";width:"+w+"pt";
			styles += ";height:"+h+"pt";
		}
		
		
		//borderColor
		if( _item.get("lineColorInt") != null ){
			shape.setStrokecolor(changeColorToRGB(Integer.valueOf(String.valueOf(_item.get("lineColorInt")))));
		}
		//strokeWidth
		if( _item.get("thickness") != null ){
			shape.setStrokeweight(toPointStr(_item.get("thickness"))+"pt");
		}
		
		shape.setConnectortype(STConnectorType.STRAIGHT);
		shape.setStyle(styles);
		
		return resultR;
	}
	
	/**
	 * 라인 아이템 생성
	 */
	protected R makeConnectLine( HashMap<String, Object> _item ) throws Exception {
		
		ObjectFactory vmlObjectFactory = new ObjectFactory();
		
		R resultR = wmlObjectFactory.createR();
		RPr resultRpr = wmlObjectFactory.createRPr();
		resultR.setRPr(resultRpr);
		Pict pict = wmlObjectFactory.createPict();
		resultR.getContent().add(pict);
		
		CTShape shape = vmlObjectFactory.createCTShape();
		JAXBElement<CTShape> shapeWrapped = vmlObjectFactory.createShape(shape);
		pict.getAnyAndAny().add(shapeWrapped);
		
		shape.setType("#_x0000_t32");
		
		// 도형의 style 문자열로 속성 세팅.
		String styles = "position:absolute;" +
				"visibility:visible;" +
				"padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt;" +
				"mso-position-horizontal:absolute;" +
				"mso-position-vertical:absolute";
		
		//verticalAlign
		if( _item.get("verticalAlign") != null ){
			styles += ";v-text-anchor:"+String.valueOf(_item.get("verticalAlign"));
		}
		double xPt = toPoint( _item.get("x") );
		//x
		if( xPt != -1 ){
			styles += ";margin-left:"+xPt+"pt";
		}
		double yPt = toPoint( _item.get("y") );
		//y
		if( yPt != -1 ){
			styles += ";margin-top:"+yPt+"pt";
		}
		//width
		if( _item.get("x2") != null ){
			styles += ";width:"+( toPoint(_item.get("x2")) )+"pt";
		}
		
		//height
		double y1 = toPoint(_item.get("y1"));
		double y2 = toPoint(_item.get("y2"));
		if( y1 < y2){
			styles += ";height:"+y2+"pt";
		}
		else{
			styles += ";height:"+y1+"pt;flip:y"; // ↗ 인 경우.
		}
		//borderColor
		if( _item.get("lineColorInt") != null ){
			shape.setStrokecolor(changeColorToRGB(Integer.valueOf(String.valueOf(_item.get("lineColorInt")))));
		}
		//strokeWidth
		if( _item.get("thickness") != null ){
			shape.setStrokeweight(toPointStr(_item.get("thickness"))+"pt");
		}
		
//		shape.setConnectortype(STConnectorType.STRAIGHT);
		shape.setConnectortype(STConnectorType.ELBOW);
		shape.setStyle(styles);
		
		return resultR;
	}

	/**
	 * <pre>
	 * Key : Encoded Font name
	 * Value : Decoded Font name 
	 * </pre>
	 * */
	private HashMap<String, String> encodedFont = new HashMap<String, String>(); 
	
	/**
	 * <pre>
	 * URLDecoder 실행하는 경우 시간이 오래걸려서, 폰트명을 저장해두고 쓴다.
	 * </pre>
	 * */
	private String getFontName( String fname ) throws Exception {
		if( encodedFont.containsKey(fname) ){
			return encodedFont.get(fname);
		}
		else{
			String val = URLDecoder.decode(fname, "UTF-8");
			encodedFont.put(fname, val);
			return val;
		}
	}
	
	/**
	 * REF : http://blog.iprofs.nl/2012/10/22/adding-images-and-layout-to-your-docx4j-generated-word-documents-part-1/
	 * */
	/**
	 * <pre>
	 * 이미지 생성함수. 라벨아이템을 제외한 모든 아이템은 이미지로 추가한다.
	 * 이미지 Position 지정을 위하여, 글상자를 생성하고, 그 안에 이미지를 추가한다.
	 * </pre>
	 * @throws Exception 
	 * */
	protected R makeImage(WordprocessingMLPackage wordMLPackage, HashMap<String, Object> _item){

		// --------- org.ubstorm.service.parser.thread : JobConsumer.makeImage() 에서 수행됨...

/*
		String _imgfileURL = String.valueOf(_item.get("src"));
		
		if( _imgfileURL.trim().equals("") ){
			log.error(getClass().getName() + "::makeImage::" + ">>>>>>>>>> item's src value is empty.");
			return null;
		}

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~makeImage()..._imgfileURL=" + _imgfileURL);
		
		// 아이템의 src 속성으로 byte 코드를 받아온다.
		//byte[] bAr = common.getBytesRemoteImageFile(String.valueOf(_item.get("src")));
		byte[] bAr = common.getBytesLocalImageFile(_imgfileURL);
		
		if( bAr == null || bAr.length == 0 ){
			log.error(getClass().getName() + "::makeImage::" + ">>>>>>>>>>Getting image byte code fail.");
			return null;
		}
		
		try {

			org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
			ObjectFactory vmlObjectFactory = new ObjectFactory();
			
			// Create object for r
			R resultR = wmlObjectFactory.createR();
			// Create object for rPr
			RPr resultRpr = wmlObjectFactory.createRPr();
			resultR.setRPr(resultRpr);
			
			// Create object for pict (wrapped in JAXBElement)
			Pict pict = wmlObjectFactory.createPict();
			JAXBElement<Pict> pictWrapped = wmlObjectFactory.createRPict(pict);
			resultR.getContent().add(pictWrapped);
			
			// Create object for shapetype (wrapped in JAXBElement)
			CTShapetype shapetype = vmlObjectFactory.createCTShapetype();
			// Create object for shape (wrapped in JAXBElement)
			CTShape shape = vmlObjectFactory.createCTShape();
			JAXBElement<CTShapetype> shapetypeWrapped = vmlObjectFactory.createShapetype(shapetype);
			pict.getAnyAndAny().add(shapetypeWrapped);
			JAXBElement<CTShape> shapeWrapped = vmlObjectFactory.createShape(shape);
			pict.getAnyAndAny().add(shapeWrapped);
			
			// Create object for textbox (wrapped in JAXBElement)
			CTTextbox textbox = vmlObjectFactory.createCTTextbox();
			JAXBElement<CTTextbox> textboxWrapped = vmlObjectFactory.createTextbox(textbox);
			shape.getEGShapeElements().add(textboxWrapped);
			// Create object for txbxContent
			CTTxbxContent txbxcontent = wmlObjectFactory.createCTTxbxContent();
			textbox.setTxbxContent(txbxcontent);
			textbox.setInset("0,0,0,0");
			
			// Create object for p
			P p = wmlObjectFactory.createP();
			txbxcontent.getContent().add(p);
			// Create object for pPr
			PPr ppr = wmlObjectFactory.createPPr();
			p.setPPr(ppr);
			// Create object for rPr
			ParaRPr pararpr2 = wmlObjectFactory.createParaRPr();
			ppr.setRPr(pararpr2);
			CTSignedTwipsMeasure par2Sp = new CTSignedTwipsMeasure();
			par2Sp.setVal(BigInteger.valueOf(0));
			resultRpr.setSpacing(par2Sp);
			
			PPrBase.Spacing pprbasespacing = wmlObjectFactory.createPPrBaseSpacing(); 
			pprbasespacing.setAfter(BigInteger.valueOf(0));
			pprbasespacing.setBefore(BigInteger.valueOf(0));
			pprbasespacing.setAfterAutospacing(false);
			pprbasespacing.setBeforeAutospacing(false);
			ppr.setSpacing(pprbasespacing);
			
			// Create object for lang
			CTLanguage language2 = wmlObjectFactory.createCTLanguage();
			pararpr2.setLang(language2);
			language2.setVal("en-AU");
			// Create object for r
			R run = wmlObjectFactory.createR();
			p.getContent().add(run);
			// Create object for rPr
			RPr rpr = wmlObjectFactory.createRPr();
			run.setRPr(rpr);
			
			BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bAr);
			String fnameHint = "";
			Object prop = null;
			// id
			if( (prop = _item.get("id")) != null ){
				fnameHint = String.valueOf(prop);
			}
			String altTxt = "";
			// tooltip
			if( (prop = _item.get("text")) != null ){
				altTxt = String.valueOf(prop);
			}
			
			// 고유 ID???
			int docPrId = 1;
			int cNvPrId = 2;
			
			Inline inline = null;
			if( _item.get("width") != null && _item.get("height") != null ){
				
				//	이미지 사이즈 변경에 사용되는 단위는 EMU. px-->twip-->emu 로 변환하여 사용한다.
				//	텍스트 상자에 들어가는 기본 여백 [ 좌우=0.25*2=0.5cm=18px, 상하=0.13*2=0.26cm=10px ]을 빼서 이미지의 크기를 정한다.. 
				//	여백을 지우려고했으나 스타일 지정이 안됨. 
				
				int cx = UnitsOfMeasurement.pxToTwip((float)toNum(_item.get("width")));
				int cy = UnitsOfMeasurement.pxToTwip((float)toNum(_item.get("height")));
				long cxL = UnitsOfMeasurement.twipToEMU(cx);
				long cyL = UnitsOfMeasurement.twipToEMU(cy);
				inline = imagePart.createImageInline( fnameHint, altTxt, docPrId, cNvPrId, cxL, cyL, false);
				
				// Now add the in-line image to a paragraph
				Drawing drawing = wmlObjectFactory.createDrawing();
				run.getContent().add(drawing);
				drawing.getAnchorOrInline().add(inline);
			}else{
				inline = imagePart.createImageInline( fnameHint, altTxt, docPrId, cNvPrId, false);
			}
			
			String shapeStyle = ""
						+ "mso-position-horizontal:absolute"
						+ ";position:absolute"
						+ ";padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt"
//						+ ";padding:0cm"
						+ ";mso-position-vertical:absolute";
			
			// x
			if( _item.get("x") != null ){
				shapeStyle += ";margin-left:" + toPointStr(_item.get("x")) + "pt";
			}
			// y
			if( _item.get("y") != null ){
				shapeStyle += ";margin-top:" + toPointStr(_item.get("y")) + "pt";
			}
			// width
			if ( _item.get("width") != null ) {
				shapeStyle += ";width:" + toCM(_item.get("width")) + "cm";
			}
			// height
			if ( _item.get("height") != null ) {
				shapeStyle += ";height:" + toCM(_item.get("height")) + "cm";
			}
			shape.setStyle(shapeStyle);
			
			return resultR;
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error(getClass().getName() + "::makeImage::" + "parsing [ "+String.valueOf(_item.get("id"))+" ] fail.");
			return null;
		}
*/
		return null;
	}
	
	/**
	 * <pre>
	 * SVG 차트 태그를 base64로 변환.
	 * 이미지 Position 지정을 위하여, 글상자를 생성하고, 그 안에 이미지를 추가한다.
	 * REF : https://xmlgraphics.apache.org/batik/using/transcoder.html
	 * 1. batik example
	 * http://thinktibits.blogspot.kr/2012/12/Batik-Convert-SVG-PNG-Java-Program-Example.html
	 * 2. svt2png.js
	 * https://gist.github.com/gustavohenke/9073132
	 * 3. batik api
	 * http://xmlgraphics.apache.org/batik/using/
	 * </pre>
	 * @return base64 문자열.
	 * @throws Exception 
	 * */
	protected String makeSVGArea(HashMap<String, Object> _item){

		log.debug(getClass().getName() + "::makeSVGArea::" + " Create SVG Image... " + String.valueOf(_item.get("id")));
		
		float  oWidth = 64f;
		float  oHeight = 64f;
		
		//width
		if( _item.get("width") != null ){
			oWidth = Double.valueOf(_item.get("width").toString()).floatValue();
	        oWidth = _item.get("width").toString().indexOf(".") != -1 ? oWidth : oWidth + 0.6f;
		}
		//height
		if( _item.get("height") != null ){
			oHeight = Double.valueOf(_item.get("height").toString()).floatValue();
	        oHeight = _item.get("height").toString().indexOf(".") != -1 ? oHeight : oHeight + 0.6f;
		}
        
		log.debug(getClass().getName()+"::makeSVGArea::" + "oWidth=" + oWidth + ",oHeight=" + oHeight);
		
		String _dataStr = "";
		try {
			_dataStr = URLDecoder.decode( String.valueOf(_item.get("data")) , "UTF-8");
			_dataStr = _dataStr.replaceAll("%20", " ");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error(getClass().getName() + "::makeSVGArea::" + "Decoding svg data fail.");
			return null;
		}
		
		String itemClassName = (String) _item.get("className");
		if( itemClassName != null && itemClassName.contains("UBQRCode") )
		{
			_dataStr = Base64Decoder.decode(_dataStr);
		}
		else
		{
			_dataStr = StringUtil.convertSvgStyleXPath(_dataStr);
		}
		
		try {
			TranscoderInput ti = new TranscoderInput(new StringInputStream(_dataStr , "UTF-8"));
	        /*
	         *************** File 로 저장하는 경우.
			File newFile = new File("G:\\converted_"+new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date())+".png");
			OutputStream png_ostream = new FileOutputStream(newFile);
			TranscoderOutput to = new TranscoderOutput(png_ostream);
			PNGTranscoder converter = new PNGTranscoder();
			converter.transcode(ti, to);
			png_ostream.flush();
			png_ostream.close();
			ti = new TranscoderInput(new StringInputStream(_dataStr , "UTF-8"));
	         */
	        //--------------- 파일저장은 정상. docx4j로 첨부하면 엑박... ---------------//
			//OutputStream png_ostream = new ByteOutputStream();
			OutputStream png_ostream = new ByteArrayOutputStream();
			TranscoderOutput to = new TranscoderOutput(png_ostream);
			PNGTranscoder converter = new PNGTranscoder();
			converter.addTranscodingHint(PNGTranscoder.KEY_WIDTH, oWidth);
			converter.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, oHeight);
			
			converter.transcode(ti, to);
			png_ostream.flush();
			
			//String source = common.base64_encode_byte(((ByteOutputStream)png_ostream).toByteArray());
			String source = new String(Base64.encodeBase64(((ByteArrayOutputStream) png_ostream).toByteArray()));
	        return source;
	        
		} catch (Exception e) {
			e.printStackTrace();
			log.error(getClass().getName() + "::makeSVGArea::" + "Create SVG Image fail.");
			return null;
		}
		
	}
	
	private void makeWaterMark(String watermarkTxt) throws Exception {
		
		//REF : https://github.com/plutext/docx4j/blob/master/src/samples/docx4j/org/docx4j/samples/Watermark.java
		StringBuilder openXML = new StringBuilder();
		openXML.append("<w:p xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\">");
		openXML.append("<w:pPr>");
		openXML.append("<w:pStyle w:val=\"Header\"/>");
		openXML.append("</w:pPr>"); 
		openXML.append("<w:sdt>"); 
		openXML.append("<w:sdtPr>");
		openXML.append("<w:id w:val=\"-1589924921\"/>");
		openXML.append("<w:lock w:val=\"sdtContentLocked\"/>");
		openXML.append("<w:docPartObj>");
		openXML.append("<w:docPartGallery w:val=\"Watermarks\"/>");
		openXML.append("<w:docPartUnique/>");
		openXML.append("</w:docPartObj>");
		openXML.append("</w:sdtPr>");
		openXML.append("<w:sdtEndPr/>");
		openXML.append("<w:sdtContent>");
		openXML.append("<w:r>");
		openXML.append("<w:rPr>");
		openXML.append("<w:noProof/>");
		openXML.append("<w:lang w:eastAsia=\"zh-TW\"/>");
		openXML.append("</w:rPr>");
		openXML.append("<w:pict>");
		openXML.append("<v:shapetype adj=\"10800\" coordsize=\"21600,21600\" id=\"_x0000_t136\" o:spt=\"136\" path=\"m@7,l@8,m@5,21600l@6,21600e\">");
		openXML.append("<v:formulas>");
		openXML.append("<v:f eqn=\"sum #0 0 10800\"/>");
		openXML.append("<v:f eqn=\"prod #0 2 1\"/>");
		openXML.append("<v:f eqn=\"sum 21600 0 @1\"/>");
		openXML.append("<v:f eqn=\"sum 0 0 @2\"/>");
		openXML.append("<v:f eqn=\"sum 21600 0 @3\"/>");
		openXML.append("<v:f eqn=\"if @0 @3 0\"/>");
		openXML.append("<v:f eqn=\"if @0 21600 @1\"/>");
		openXML.append("<v:f eqn=\"if @0 0 @2\"/>");
		openXML.append("<v:f eqn=\"if @0 @4 21600\"/>");
		openXML.append("<v:f eqn=\"mid @5 @6\"/>");
		openXML.append("<v:f eqn=\"mid @8 @5\"/>");
		openXML.append("<v:f eqn=\"mid @7 @8\"/>");
		openXML.append("<v:f eqn=\"mid @6 @7\"/>");
		openXML.append("<v:f eqn=\"sum @6 0 @5\"/>");
		openXML.append("</v:formulas>");
		openXML.append("<v:path o:connectangles=\"270,180,90,0\" o:connectlocs=\"@9,0;@10,10800;@11,21600;@12,10800\" o:connecttype=\"custom\" textpathok=\"t\"/>");
		openXML.append("<v:textpath fitshape=\"t\" on=\"t\"/>");
		openXML.append("<v:handles>");
		openXML.append("<v:h position=\"#0,bottomRight\" xrange=\"6629,14971\"/>");
		openXML.append("</v:handles>");
		openXML.append("<o:lock shapetype=\"t\" text=\"t\" v:ext=\"edit\"/>");
		openXML.append("</v:shapetype>");
		openXML.append("<v:shape fillcolor=\"silver\" id=\"PowerPlusWaterMarkObject357476642\" o:allowincell=\"f\" o:spid=\"_x0000_s2049\" stroked=\"f\" style=\"position:absolute;margin-left:0;margin-top:0;width:527.85pt;height:131.95pt;rotation:315;z-index:-251658752;mso-position-horizontal:center;mso-position-horizontal-relative:margin;mso-position-vertical:center;mso-position-vertical-relative:margin\" type=\"#_x0000_t136\">");
		openXML.append("<v:fill opacity=\".5\"/>");
		openXML.append("<v:textpath string=\""+watermarkTxt+"\" style=\"font-family:&quot;Calibri&quot;;font-size:1pt\"/>");
		openXML.append("<w10:wrap anchorx=\"margin\" anchory=\"margin\"/>");
		openXML.append("</v:shape>");
		openXML.append("</w:pict>");
		openXML.append("</w:r>");
		openXML.append("</w:sdtContent>");
		openXML.append("</w:sdt>");
		openXML.append("</w:p>");
			
		P p = (P)XmlUtils.unmarshalString(openXML.toString());	
		
		HeaderPart headerPart = new HeaderPart();
		Relationship relationship =  mdp.addTargetPart(headerPart);

		Hdr hdr = wmlObjectFactory.createHdr();
		hdr.getContent().add(p);
		headerPart.setJaxbElement(hdr);
		
		List<SectionWrapper> sections = wordMLPackage.getDocumentModel().getSections();
		   
		SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectPr==null ) {
			sectPr = wmlObjectFactory.createSectPr();
			mdp.addObject(sectPr);
			sections.get(sections.size() - 1).setSectPr(sectPr);
		}

		HeaderReference headerReference = wmlObjectFactory.createHeaderReference();
		headerReference.setId(relationship.getId());
		headerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(headerReference);
		
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
	 * <pre>
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
	private double toPoint( Object o ){
		if( o == null ){
			return -1;
		}
		return Math.floor( Double.parseDouble( String.valueOf(o) ) )*0.75;
	}
	
	/**
	 * <pre>
	 * convert to pt value
	 * </pre>
	 * @param o : numberValue
	 * @return String 소수점 2자리 까지만 표현된 문자열. 
	 * */
	private String toPointStr( Object o ){
		if( o == null ){
			return "";
		}
		double pt = Math.floor( Double.parseDouble( String.valueOf(o) ) )*0.75;
		return (new DecimalFormat(".##").format(pt));
	}
	
	private String toPointStrMinusPadding( Object o, String _type ){
		if( o == null ){
			return "";
		}
		
		double pt = (Math.floor( Double.parseDouble( String.valueOf(o) ) )*0.75);
		if( _type.equals("x"))
		{
			pt = pt - 3;
		}
		else if( _type.equals("y") )
 		{
			pt = pt + 3;
		}
		return (new DecimalFormat(".##").format(pt));
	}
	
	/**
	 * <pre>
	 * convert to pt value
	 * 1 px = 0.02645833333333 cm
	 * </pre>
	 * @param o : numberValue
	 * @return double cm 
	 * */
	private double toCM( Object o ){
		if( o == null ){
			return -1;
		}
		return Math.floor( Double.parseDouble( String.valueOf(o) ) ) * 0.02645833333333;
	}
	
	/**
	 * Hex String 을 RGB 문자열로 변환.
	 * @param colorStr e.g. "#FFFFFF"
	 * @return {String} RGB(255,255,255)
	 */
	public String hex2RgbString(String colorStr) {
		if( colorStr.length() == 4){
			// triple hex string #000
			char[] strAr = colorStr.toCharArray();
			colorStr = strAr[0] + strAr[1] + strAr[1] + strAr[2] + strAr[2] + strAr[3] + strAr[3]+"";
		}
		else if( colorStr.length() != 7 ){
			log.error(getClass().getName()+"::hex2RgbString::"+" color code is invalid.\t"+colorStr);
		}
		
		String r = String.valueOf(Integer.valueOf( colorStr.substring( 1, 3 ), 16 ));
		String g = String.valueOf(Integer.valueOf( colorStr.substring( 3, 5 ), 16 ));
		String b = String.valueOf(Integer.valueOf( colorStr.substring( 5, 7 ), 16 ));
		
		return "RGB(" + r + "," + g + "," + b + ")";
	}
	
	/**
	 * Int color to RGB String
	 * @return RGB(255,0,0) 
	 * */
	public String changeColorToRGB(int _color)
	{
		java.awt.Color _c = new java.awt.Color(_color);
		return "RGB(" + _c.getRed() + "," + _c.getGreen() + "," + _c.getBlue() + ")";
	}
	
	/**
	 * Int color to RGBA String
	 * @return RGBA(255,0,0,255) 
	 * */
	public String changeColorToRGBA(int _color , String alpha)
	{
		java.awt.Color _c = new java.awt.Color(_color);
		return "RGBA(" + _c.getRed() + "," + _c.getGreen() + "," + _c.getBlue() + "," + Integer.parseInt(alpha)*255  + ")";
	}
	
	/**
	 * Int color to RGB String
	 * @param borderType 종류명 ex) solid
	 * @param borderThick 두께
	 * @return RGB(255,0,0) 
	 * */
	private STBorder getSTBorder(String borderType , int borderThick ){
		if( borderType == null ) return null;
		
		STBorder _stbd = STBorder.NONE;

		if( borderType == null || borderType.equals("") || borderThick <= 0){
			borderType = "none";
		}
		//double 이 예약어라 enum에서 dbl로 사용.
		if( borderType.equals("double") ){
			borderType = "dbl";
		}
		// thick의 굵기를 반영하여 타입지정.
		else if( borderType.equals("solid") ){
//			if( borderThick > 3) borderType = "thick";
			if( borderThick > 1) borderType = "solid";
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
		
		try {
			switch (EBorderType.valueOf(String.valueOf(borderType))) {
			case solid: _stbd = STBorder.SINGLE; break;
			case dot: _stbd = STBorder.DOTTED;  break;
			case dash: _stbd = STBorder.DASHED; break;
			case dash_dot: _stbd = STBorder.DOT_DASH; break;
			case dash_dot_dot: _stbd = STBorder.DOT_DOT_DASH; break;
			case SOLD: _stbd = STBorder.SINGLE; break;
			case dbl: _stbd = STBorder.DOUBLE; break;
			case thick: _stbd = STBorder.THICK; break;
			case none: _stbd = STBorder.NONE; break;
			}
		} catch (IllegalArgumentException e) {
			log.error(getClass().getName()+"::getSTBorder::"+"borderType argument is wrong. >>> " + borderType);
		}
		
		return _stbd;
	}
	
	
	
	private String CreateClipImage( HashMap<String, Object> _item ) throws MalformedURLException, IOException, TranscoderException
	{
		String _clipName = ValueConverter.getString(_item.get("clipArtData"));
		
		if( !_clipName.substring(_clipName.length() - 3, _clipName.length()).toUpperCase().equals("SVG"))
		{
			_clipName = _clipName+".svg";
		}
		
		
		//Step -1: We read the input SVG document into Transcoder Input
        //We use Java NIO for this purpose
		String _urlStr = Log.basePath + "UView5/assets/images/svg/" + _clipName;
		
		java.io.File _file = new java.io.File(_urlStr);
		
        TranscoderInput input_svg_image = new TranscoderInput( _file.toURL().toString() );    
//        TranscoderInput input_svg_image = new TranscoderInput(_urlStr);
     
        
        
//        OutputStream png_ostream = new FileOutputStream("chessboard.png");
        OutputStream png_ostream = new ByteArrayOutputStream();
        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);        
        
        PNGTranscoder my_converter = new PNGTranscoder();
        my_converter.transcode(input_svg_image, output_png_image);
        		
        png_ostream.flush();
//        png_ostream.close(); 	
        
        String base64 = new String(Base64.encodeBase64(((ByteArrayOutputStream) png_ostream).toByteArray()));
        return base64;
        
	}

	public ImageDictionary getmImageDictionary() {
		return mImageDictionary;
	}

	public void setmImageDictionary(ImageDictionary mImageDictionary) {
		this.mImageDictionary = mImageDictionary;
	}
	
	private PgBorders createPageBackground()
	{
	     SectPr.PgBorders sectprpgborders = wmlObjectFactory.createSectPrPgBorders();
	      // Create object for top 
	      CTBorder border = wmlObjectFactory.createCTBorder(); 
	      sectprpgborders.setTop(border); 
	       border.setVal(org.docx4j.wml.STBorder.SINGLE); 
	       border.setSz(BigInteger.valueOf(4)); 
	       border.setColor("auto"); 
	       border.setSpace(BigInteger.valueOf(0)); 
	      // Create object for left 
	      CTBorder border2 = wmlObjectFactory.createCTBorder(); 
	      sectprpgborders.setLeft(border2); 
	       border2.setVal(org.docx4j.wml.STBorder.SINGLE); 
	       border2.setSz(BigInteger.valueOf(4)); 
	       border2.setColor("auto"); 
	       border2.setSpace(BigInteger.valueOf(0)); 
	      // Create object for bottom 
	      CTBorder border3 = wmlObjectFactory.createCTBorder(); 
	      sectprpgborders.setBottom(border3); 
	       border3.setVal(org.docx4j.wml.STBorder.SINGLE); 
	       border3.setSz(BigInteger.valueOf(4)); 
	       border3.setColor("auto"); 
	       border3.setSpace(BigInteger.valueOf(0)); 
	      // Create object for right 
	      CTBorder border4 = wmlObjectFactory.createCTBorder(); 
	      sectprpgborders.setRight(border4); 
	       border4.setVal(org.docx4j.wml.STBorder.SINGLE); 
	       border4.setSz(BigInteger.valueOf(4)); 
	       border4.setColor("auto"); 
	       border4.setSpace(BigInteger.valueOf(0)); 
	      sectprpgborders.setOffsetFrom(org.docx4j.wml.STPageBorderOffset.PAGE); 
	      
	      
	      return sectprpgborders;
	}
	
	
    public void setDocumentBackGround(WordprocessingMLPackage wordPackage, String color) throws Exception {
        MainDocumentPart mdp = wordPackage.getMainDocumentPart();
        CTBackground bkground = mdp.getContents().getBackground();
        if (bkground == null) {
            bkground = wmlObjectFactory.createCTBackground();
        }
        
        bkground.setColor(color);
        mdp.getContents().setBackground(bkground);
    }
    
	/**
	 * int로 된 color를 Hex 로 변경 한다. 
	 * @return String _hex
	 * 
	 */
	public String changeColorToHex(int _color)
	{
		String _hex = "";

		java.awt.Color _c = new java.awt.Color(_color);

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
		
		_hex = _red + _green + _blue;

		return _hex;
	}
	
    
}
// class End
