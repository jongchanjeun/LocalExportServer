package org.ubstorm.service.parser.thread;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTGraphicalObjectFrameLocking;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualGraphicFrameProperties;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.CTEffectExtent;
import org.docx4j.dml.wordprocessingDrawing.CTPosH;
import org.docx4j.dml.wordprocessingDrawing.CTPosV;
import org.docx4j.dml.wordprocessingDrawing.CTWrapPath;
import org.docx4j.dml.wordprocessingDrawing.CTWrapTight;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.dml.wordprocessingDrawing.STAlignH;
import org.docx4j.dml.wordprocessingDrawing.STRelFromH;
import org.docx4j.dml.wordprocessingDrawing.STRelFromV;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTShapetype;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTSignedTwipsMeasure;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.Pict;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.ubstorm.service.dictionary.ImageDictionary;
import org.ubstorm.service.dictionary.ImageDictionaryVO;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.queue.IQueue;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.oreilly.servlet.Base64Decoder;

public class JobConsumer implements Runnable {

	private Logger log = Logger.getLogger(getClass());
	
	private IQueue queue = null;
	private String threadName = null;
	private static final Object monitor = new Object();
	
	public JobConsumer(IQueue queue, String index)
	{
		this.queue = queue;
		this.threadName = "[JobComsumer-" + index + "]";
	}
	
	@Override
	public void run() {
		log.info(getClass().getName() + "::" + this.threadName + " Start!!!...");
		
		try{
			
			while(!Thread.currentThread().isInterrupted()) {
				//(1) 작업큐에서 수행할 작업을 하나 가져온다.
				HashMap param = (HashMap) queue.pop();
				
				// (2) 얻어온 작업을 실제 수행한다.
				boolean bResult = makeDocumentProcess(param);
				if(bResult == true)
				{
					log.info(getClass().getName() + "::" + this.threadName + " makeDocumentProcess success!");
				}
				else
				{
					log.info(getClass().getName() + "::" + this.threadName + " makeDocumentProcess fail!");
				}
								
				queue.reduceJobCount();
				
				// CPU의 부담을 덜기 위해서 잠간 쉬게 한다.
				Thread.sleep(10);
			}
			
		}
		catch(Exception exp)
		{
			
		}
		finally {
			log.info(getClass().getName() + "::" + this.threadName + " End...");
		}
		
	}
	
	
	private boolean makeDocumentProcess(HashMap param) {
		boolean bResult = false;
		
		WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage) param.get("wordMLPackage");
		List pList = (ArrayList) param.get("pageList");
		HashMap<String, Object> _item = (HashMap) param.get("curItem");
		ImageDictionary imageDictionary = (ImageDictionary) param.get("imgDictionary");
		
		try
		{
			R img = makeImage(wordMLPackage, _item,imageDictionary);
			if( img != null ){
				pList.add( img );
			}

			bResult = true;
		}
		catch(Exception exp)
		{
			exp.printStackTrace();
			bResult = false;
		}
		
		return bResult;
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
		return (int) Math.floor( Double.parseDouble( String.valueOf(o) ) );
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
	protected R makeImage(WordprocessingMLPackage wordMLPackage, HashMap<String, Object> _item , ImageDictionary imageDictionary){

		String _imgfileURL = String.valueOf(_item.get("src"));

		Boolean _hasDictionary=false;
		ImageDictionaryVO _newImgDictionary=null;
		
		if( imageDictionary == null)
		{
			imageDictionary = new ImageDictionary();
		}
		
		ImageDictionaryVO _imgDictionary=imageDictionary.getDictionaryData(_imgfileURL);
		if( _imgDictionary == null ){
			_hasDictionary = false;
		}else{
			_hasDictionary = true;
		}
		
		if( _imgfileURL.trim().equals("") ){
			log.error(getClass().getName() + "::makeImage::" + ">>>>>>>>>> item's src value is empty.");
			return null;
		}

		//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~makeImage()..._imgfileURL=" + _imgfileURL);
		
		// 아이템의 src 속성으로 byte 코드를 받아온다.
		//byte[] bAr = common.getBytesRemoteImageFile(String.valueOf(_item.get("src")));
		byte[] bAr = null;
		Image _image;
		String _className = _item.get("className").toString();
		if(_className.equals("UBSVGArea") || _className.equals("UBQRCode"))
		{
			try {
				if( _className.contains("UBQRCode") )
				{
					String _svgData = ValueConverter.getString(_item.get("src"));
					_item.put("data", _svgData.substring(4));
				}				
				
				bAr = CreateSVGAreaImage(_item);
			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(_className.equals("UBClipArtContainer") )
		{
			try {
				bAr = CreateClipImage(_item);
			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			bAr = common.getBytesLocalImageFile(_imgfileURL);
		}
		
		
		if( bAr == null || bAr.length == 0 ){
			log.error(getClass().getName() + "::makeImage::" + ">>>>>>>>>>Getting image byte code fail.");
			return null;
		}

		boolean _isOriginSize = false;
		boolean _isBG = false;
		
		if( _item.containsKey("type") && _item.get("type").equals("BACKGROUND_IMAGE") )
		{
			_isBG = true;
		}
		
		try {
			
			//이미지 원본 비율 유지 속성
			if( _item.containsKey("isOriginalSize") &&  !_item.get("isOriginalSize").equals(""))
			{
				_isOriginSize = Boolean.valueOf(_item.get("isOriginalSize").toString());
			}
			
			org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
			org.docx4j.vml.ObjectFactory vmlObjectFactory = new org.docx4j.vml.ObjectFactory();
//			org.docx4j.dml.wordprocessingDrawing.ObjectFactory dmlwordprocessingDrawingObjectFactory = new org.docx4j.dml.wordprocessingDrawing.ObjectFactory();

			// Create object for r
			R resultR = wmlObjectFactory.createR();
			// Create object for rPr
			RPr resultRpr = wmlObjectFactory.createRPr();
			resultR.setRPr(resultRpr);
			
			/**
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
			CTTxbxContent txbxcontent = wmlObjectFactory.createCTTxbxContent();
			textbox.setTxbxContent(txbxcontent);
			textbox.setInset("0,0,0,0");
			
			// Create object for p
			P p = wmlObjectFactory.createP();
			
			//2018-04-30 최명진 : Image가 텍스트박스의 사이즈때문에 보이지 않는 현상을 막기 위하여 추가( 텍스트박스의 도형 서식 -> 텍스트 상자  : 도형을  텍스트 크기에 맞춤 옵션 )
			textbox.setStyle("mso-fit-shape-to-text:t");
			
//			Anchor _anchor = dmlwordprocessingDrawingObjectFactory.createAnchor();
			// Main에 Add처리 
//			wordMLPackage.getMainDocumentPart().getContent().add(p);
			txbxcontent.getContent().add(p);
	
			
			
			// Create object for pPr
			PPr ppr = wmlObjectFactory.createPPr();
			p.setPPr(ppr);
			// Create object for rPr
			ParaRPr pararpr2 = wmlObjectFactory.createParaRPr();
			ppr.setRPr(pararpr2);
			CTSignedTwipsMeasure par2Sp = new CTSignedTwipsMeasure();
			par2Sp.setVal(BigInteger.valueOf(0));
			pararpr2.setSpacing(par2Sp);
			
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
					*/
			
			
			BinaryPartAbstractImage imagePart = null;
			synchronized (monitor) {
				
				if( _hasDictionary == false ){
					_className = String.valueOf(_item.get("className"));
					//if( String.valueOf(_item.get("className")).equals("UBSVGArea") ){
					if( _className.equals("UBSVGArea") || _className.equals("UBQRCode") ){	
						// svg를 변환한 이미지의 경우 mime(ContentTypes)을 불러오지 못해 에러가 나면서 추가가 안됨. 직접 지정.
						imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, wordMLPackage.getMainDocumentPart(), bAr, ContentTypes.IMAGE_PNG);
					}
					else if( _className.equals("UBClipArtContainer") )
					{
//						byte[] imageBytes = Base64.decodeBase64(_item.get("src").toString().getBytes("UTF-8"));
//						imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, imageBytes );
						imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bAr );
					}
					else{
						imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bAr);
					}
					_newImgDictionary =imageDictionary.createDictionaryData(_imgfileURL, imagePart);
				}else{
					imagePart = _imgDictionary.getmDocImage();
				}
				
				
			}
			
			String fnameHint = "";
			Object prop = null;
			// id
			if( (prop = _item.get("id")) != null ){
				fnameHint = String.valueOf(prop);
			}
			String altTxt = "";
			// tooltip
			if( (prop = _item.get("text")) != null ){
				altTxt = common.htmlspecialchars( String.valueOf(prop) );
			}
			
			// 고유 ID???
			int docPrId = 1;
			int cNvPrId = 2;
			
			Inline inline = null;
			float oWidth =  0;
			float oHeight = 0;	
			HashMap<String,Float> _orignSize = null;
				
			float _itemX = 0; 
			float _itemY = 0;
			int _rotate = 0;
			
			if(_item.containsKey("rotate"))
			{
				_rotate = Float.valueOf( _item.get("rotate").toString() ).intValue();
			}
			
			if( _isBG )
			{
				HashMap<String, Float> _position = getBgPosition(_item, imagePart);
				_itemX = _position.get("left");
				_itemY = _position.get("top");
				_item.put("width", _position.get("width"));
				_item.put("height", _position.get("height"));
			}
			else
			{
				_itemX = (float) toNum(_item.get("x")); 
				_itemY = (float)toNum(_item.get("y")); 

			}
			
			if( _rotate != 0 )
			{
				float _w = (float) (toNum(_item.get("width")) / 2) * -1; 
				float _h = (float) (toNum(_item.get("height")) / 2) * -1; 

				Point _rotPosition = common.rotationPosition(_w , _h,_rotate);
				
				_itemX = Double.valueOf( _itemX + ( _w - _rotPosition.getX() ) ).floatValue();
				_itemY = Double.valueOf( _itemY + ( _h - _rotPosition.getY() ) ).floatValue();
				
			}
			
			
			long cxL = 0;
			long cyL = 0;
			
			Anchor _currentAnchor = null;
			
			if( _item.get("width") != null && _item.get("height") != null ){
				int cx = UnitsOfMeasurement.pxToTwip((float)toNum(_item.get("width")));
				int cy = UnitsOfMeasurement.pxToTwip((float)toNum(_item.get("height")));
				
				if(_isOriginSize){
					 _image = Image.getInstance(bAr);
					 _orignSize = common.getOriginSize(_item.get("width"),_item.get("height"),_image);	
					 cx = UnitsOfMeasurement.pxToTwip( _orignSize.get("width"));
					 cy = UnitsOfMeasurement.pxToTwip( _orignSize.get("height"));
				}
				
				cxL = UnitsOfMeasurement.twipToEMU(cx);
				cyL = UnitsOfMeasurement.twipToEMU(cy);
				inline = imagePart.createImageInline( fnameHint, altTxt, docPrId, cNvPrId, cxL, cyL, false);
				
				// Now add the in-line image to a paragraph
				Drawing drawing = wmlObjectFactory.createDrawing();
				resultR.getContent().add(drawing);
//				drawing.getAnchorOrInline().add(inline);
				//BigInteger.valueOf((long)toNum(_item.get("x"))*15)
				
				long _positionX = UnitsOfMeasurement.twipToEMU( UnitsOfMeasurement.pxToTwip( _itemX ) );
				long _positionY = UnitsOfMeasurement.twipToEMU( UnitsOfMeasurement.pxToTwip( _itemY ) );

				_currentAnchor = createAnchor(inline, _positionX, _positionY, _isBG);
				drawing.getAnchorOrInline().add(_currentAnchor);
			}else{
				inline = imagePart.createImageInline( fnameHint, altTxt, docPrId, cNvPrId, false);
			}

			String _embedID="";
			if( _hasDictionary == false ){
				_embedID=inline.getGraphic().getGraphicData().getPic().getBlipFill().getBlip().getEmbed();
				_newImgDictionary.setmEmbedID(_embedID);
			}else{
				_embedID=_imgDictionary.getmEmbedID();
				inline.getGraphic().getGraphicData().getPic().getBlipFill().getBlip().setEmbed(_embedID);
			}
			
			if(_rotate != 0 )
			{
				_currentAnchor.getGraphic().getGraphicData().getPic().getSpPr().setXfrm( createXfrm( _currentAnchor.getGraphic().getGraphicData().getPic().getSpPr(), cxL, cyL, _rotate )  );
			}
			
			String shapeStyle = ""
						+ "mso-position-horizontal:absolute"
						+ ";position:absolute"
						+ ";padding-left:0pt;padding-top:0pt;padding-bottom:0pt;padding-right:0pt"
						+ ";mso-position-vertical:absolute";
					
			
			if(_isOriginSize){
				//이미지 원 사이즈 정보를 얻기위해 이미지 정보 생성
							 
				oWidth =  _orignSize.get("width");
				oHeight = _orignSize.get("height");	
				float oX = Float.valueOf(_item.get("x").toString()) + _orignSize.get("marginX");
				float oY = Float.valueOf(_item.get("y").toString()) +  _orignSize.get("marginY");		
				
				
				shapeStyle += ";margin-left:" + toPointStr(oX) + "pt";
				shapeStyle += ";margin-top:" + toPointStr(oY) + "pt";
				shapeStyle += ";width:" + toCM(oWidth) + "cm";
				shapeStyle += ";height:" + toCM(oHeight) + "cm";
			}else{
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
			}
			
			
//			shape.setStyle(shapeStyle);
			
			return resultR;
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error(getClass().getName() + "::makeImage::" + "parsing [ "+String.valueOf(_item.get("id"))+" ] fail.");
			return null;
		}

	}
	
	private CTTransform2D createXfrm(  CTShapeProperties _ctprop,  long _w, long _h, int _rotate ) {
		  
		CTTransform2D xfrm = _ctprop.getXfrm();
		if( xfrm == null ) xfrm = new CTTransform2D();
		
		
		CTPoint2D off = xfrm.getOff();
		if( off == null ) off = new CTPoint2D();
		  
		xfrm.setRot( _rotate * 60000 );
		  
		off.setX(0);
		off.setY(0);
		  
		xfrm.setOff(off);
		  
		CTPositiveSize2D ext = xfrm.getExt();
		if( off == null ) ext = new CTPositiveSize2D();
		
		ext.setCx(_w);
		ext.setCy(_h);
		 
		xfrm.setExt(ext);
		  
		return xfrm;
	}
	
	private byte[] CreateClipImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException, TranscoderException
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
        
        OutputStream png_ostream = new ByteArrayOutputStream();
        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);        
        
        PNGTranscoder my_converter = new PNGTranscoder();
        my_converter.transcode(input_svg_image, output_png_image);
        		
        png_ostream.flush();
//        png_ostream.close(); 	
        
        return ((ByteArrayOutputStream) png_ostream).toByteArray();
        
	}
	
	/***
	 * Excel 저장시 SVG 차트 이미지 저장
	 * @param _item
	 * @return
	 * @throws DocumentException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws TranscoderException
	 */
	private byte[] CreateSVGAreaImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException, TranscoderException
	{
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
        
		if(oWidth <= 0 || oHeight <= 0 ) return null;
		log.debug(getClass().getName()+"::CreateSVGAreaImage::" + "oWidth=" + oWidth + ",oHeight=" + oHeight);
		
		//String _svgTag = URLDecoder.decode( _item.get("data").toString() );
		String _svgTag = URLDecoder.decode( String.valueOf(_item.get("data")) , "UTF-8");
		_svgTag = _svgTag.replaceAll("%20", " ");
		
		String itemClassName = (String) _item.get("className");
		if( itemClassName != null && itemClassName.contains("UBQRCode") )
		{
			_svgTag = Base64Decoder.decode(_svgTag);
			System.out.print("UBQRCode:svg=[" + _svgTag + "]");
		}
		else
		{
//			_svgTag = StringUtil.convertSvgStyle(_svgTag);
			_svgTag = StringUtil.convertSvgStyleXPath(_svgTag);
		}
		
		InputStream _is = new ByteArrayInputStream( _svgTag.getBytes("UTF-8") );
		
        TranscoderInput input_svg_image = new TranscoderInput( _is ); 

        OutputStream png_ostream = new ByteArrayOutputStream();
        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);        
        
        PNGTranscoder my_converter = new PNGTranscoder();
        my_converter.addTranscodingHint(PNGTranscoder.KEY_WIDTH, oWidth);
        my_converter.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, oHeight);
		
        my_converter.transcode(input_svg_image, output_png_image);
        
        png_ostream.flush();
        
        return ((ByteArrayOutputStream) png_ostream).toByteArray();
	}
	
	
	private Anchor getAnchor( Graphic _grpic)
	{
		org.docx4j.dml.wordprocessingDrawing.ObjectFactory dmlwordprocessingDrawingObjectFactory = new org.docx4j.dml.wordprocessingDrawing.ObjectFactory();

		Anchor anchor = dmlwordprocessingDrawingObjectFactory.createAnchor(); 
		    // Create object for wrapTight
		    CTWrapTight wraptight = dmlwordprocessingDrawingObjectFactory.createCTWrapTight(); 
		    anchor.setWrapTight(wraptight); 
		        wraptight.setWrapText(org.docx4j.dml.wordprocessingDrawing.STWrapText.BOTH_SIDES);
		        // Create object for wrapPolygon
		        CTWrapPath wrappath = dmlwordprocessingDrawingObjectFactory.createCTWrapPath(); 
		        wraptight.setWrapPolygon(wrappath); 
		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();
		            // Create object for lineTo
		            CTPoint2D point2d = dmlObjectFactory.createCTPoint2D(); 
		            wrappath.getLineTo().add( point2d); 
		                point2d.setY( 21127 );
		                point2d.setX( 0 );
		            // Create object for lineTo
		            CTPoint2D point2d2 = dmlObjectFactory.createCTPoint2D(); 
		            wrappath.getLineTo().add( point2d2); 
		                point2d2.setY( 21127 );
		                point2d2.setX( 21413 );
		            // Create object for lineTo
		            CTPoint2D point2d3 = dmlObjectFactory.createCTPoint2D(); 
		            wrappath.getLineTo().add( point2d3); 
		                point2d3.setY( 0 );
		                point2d3.setX( 21413 );
		            // Create object for lineTo
		            CTPoint2D point2d4 = dmlObjectFactory.createCTPoint2D(); 
		            wrappath.getLineTo().add( point2d4); 
		                point2d4.setY( 0 );
		                point2d4.setX( 0 );
		            // Create object for start
		            CTPoint2D point2d5 = dmlObjectFactory.createCTPoint2D(); 
		            wrappath.setStart(point2d5); 
		                point2d5.setY( 0 );
		                point2d5.setX( 0 );
		    // Create object for docPr
		    CTNonVisualDrawingProps nonvisualdrawingprops = dmlObjectFactory.createCTNonVisualDrawingProps(); 
		    anchor.setDocPr(nonvisualdrawingprops); 
		        nonvisualdrawingprops.setDescr( "http://louisianalawblog.wp.lexblogs.com/wp-content/uploads/sites/342/2015/02/app.jpg"); 
		        nonvisualdrawingprops.setName( "Picture 1"); 
		        nonvisualdrawingprops.setId( 1 );
		    // Create object for cNvGraphicFramePr
		    CTNonVisualGraphicFrameProperties nonvisualgraphicframeproperties = dmlObjectFactory.createCTNonVisualGraphicFrameProperties(); 
		    anchor.setCNvGraphicFramePr(nonvisualgraphicframeproperties); 
		        // Create object for graphicFrameLocks
		        CTGraphicalObjectFrameLocking graphicalobjectframelocking = dmlObjectFactory.createCTGraphicalObjectFrameLocking(); 
		        nonvisualgraphicframeproperties.setGraphicFrameLocks(graphicalobjectframelocking); 
		    // Create object for graphic
		    anchor.setGraphic(_grpic); 
            org.docx4j.dml.picture.ObjectFactory dmlpictureObjectFactory = new org.docx4j.dml.picture.ObjectFactory();
		    anchor.setDistT( new Long(0) );
		    anchor.setDistB( new Long(0) );
		    anchor.setDistL( new Long(114300) );
		    anchor.setDistR( new Long(114300) );
		    anchor.setRelativeHeight( 251658240 );
		    // Create object for simplePos
		    CTPoint2D point2d7 = dmlObjectFactory.createCTPoint2D(); 
		    anchor.setSimplePos(point2d7); 
		        point2d7.setY( 0 );
		        point2d7.setX( 0 );
		    // Create object for positionH
		    CTPosH posh = dmlwordprocessingDrawingObjectFactory.createCTPosH(); 
		    anchor.setPositionH(posh); 
		        posh.setRelativeFrom(org.docx4j.dml.wordprocessingDrawing.STRelFromH.COLUMN);
		        posh.setPosOffset( new Integer(4152900) );
		    // Create object for positionV
		    CTPosV posv = dmlwordprocessingDrawingObjectFactory.createCTPosV(); 
		    anchor.setPositionV(posv); 
		        posv.setRelativeFrom(org.docx4j.dml.wordprocessingDrawing.STRelFromV.PARAGRAPH);
		        posv.setPosOffset( new Integer(0) );
		    // Create object for extent
		    CTPositiveSize2D positivesize2d2 = dmlObjectFactory.createCTPositiveSize2D(); 
		    anchor.setExtent(positivesize2d2); 
		        positivesize2d2.setCx( 1903016 );
		        positivesize2d2.setCy( 1304925 );
		    // Create object for effectExtent
		    CTEffectExtent effectextent = dmlwordprocessingDrawingObjectFactory.createCTEffectExtent(); 
		    anchor.setEffectExtent(effectextent); 
		        effectextent.setT( 0 );
		        effectextent.setL( 0 );
		        effectextent.setR( 2540 );
		        effectextent.setB( 0 );

		return anchor;
	}
	
	private Anchor createAnchor( Inline inline, long _cxL, long _cyL, boolean _isBG ) throws JAXBException
	{
	    // convert the inline to an anchor (xml contents are essentially the same)
	    String anchorXml = XmlUtils.marshaltoString(inline, true, false, Context.jc, Namespaces.NS_WORD12, "anchor",Inline.class);
	    
	    int _x = Long.valueOf(_cxL).intValue();
	    int _y = Long.valueOf(_cyL).intValue() ;
	    
	    org.docx4j.dml.ObjectFactory dmlFactory = new org.docx4j.dml.ObjectFactory();
	    org.docx4j.dml.wordprocessingDrawing.ObjectFactory wordDmlFactory = new org.docx4j.dml.wordprocessingDrawing.ObjectFactory();

	    Anchor anchor = (Anchor) XmlUtils.unmarshalString(anchorXml, Context.jc, Anchor.class);
	    anchor.setSimplePos(dmlFactory.createCTPoint2D());
	    anchor.getSimplePos().setX(0);
	    anchor.getSimplePos().setY(0);
	    anchor.setSimplePosAttr(false);
	    
	    anchor.setBehindDoc(_isBG);
	    anchor.setAllowOverlap(true);

	    anchor.setPositionH(wordDmlFactory.createCTPosH());
	    anchor.getPositionH().setPosOffset( Long.valueOf(_cxL).intValue() );
//	    anchor.getPositionH().setRelativeFrom(STRelFromH.COLUMN);
	    anchor.getPositionH().setRelativeFrom(STRelFromH.PAGE);
	    anchor.setPositionV(wordDmlFactory.createCTPosV());
	    anchor.getPositionV().setPosOffset( Long.valueOf(_cyL).intValue() );
//	    anchor.getPositionV().setRelativeFrom(STRelFromV.TOP_MARGIN);
	    anchor.getPositionV().setRelativeFrom(STRelFromV.PAGE);
	    anchor.setWrapNone(wordDmlFactory.createCTWrapNone());
	    

	    // Now add the inline in w:p/w:r/w:drawing
	    return anchor;
	}
	
	private HashMap<String,Float> getBgPosition( HashMap<String, Object> _item, BinaryPartAbstractImage imagePart )
	{
		HashMap<String,Float> _poistion = new HashMap<String,Float>();
		
		float _imgW = imagePart.getImageInfo().getSize().getWidthPx();
		float _imgH = imagePart.getImageInfo().getSize().getHeightPx();
		
		float _pageW  = Float.valueOf(_item.get("pageWidth").toString());
		float _pageH  = Float.valueOf(_item.get("pageHeight").toString());
		float _scaleW = _pageW/_imgW;
		float _scaleH = _pageH/_imgH;
		float _scale = 1;
			
        if( _scaleH > _scaleW )
        {
        	// width scale에 맞춰서 이미지의 사이즈변경
        	_scale = _scaleW;
        }else
        {
        	_scale = _scaleH;
        }
        
        _imgW = _imgW * _scale;
        _imgH = _imgH * _scale;
        
        float _top =  (_pageH - _imgH )/2;
        float _left = (_pageW - _imgW )/2;
        
        _poistion.put("left", _left);
        _poistion.put("top", _top);
        _poistion.put("width", _imgW);
        _poistion.put("height", _imgH);
		
		return _poistion;
	}
	
}
