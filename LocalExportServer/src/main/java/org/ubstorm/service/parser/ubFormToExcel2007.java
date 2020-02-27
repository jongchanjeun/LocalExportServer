package org.ubstorm.service.parser;

import java.awt.Color;
import java.awt.font.FontRenderContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.ShapeTypes;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.ubstorm.service.dictionary.ImageDictionary;
import org.ubstorm.service.dictionary.ImageDictionaryVO;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.xmlToUbForm.EFontStyle;
import org.ubstorm.service.parser.xmlToUbForm.EFontWeight;
import org.ubstorm.service.parser.xmlToUbForm.ETextAlign;
import org.ubstorm.service.parser.xmlToUbForm.ETextDecoration;
import org.ubstorm.service.parser.xmlToUbForm.EVerticalAlign;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.ValueConverter;
import org.ubstorm.service.utils.common;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.oreilly.servlet.Base64Decoder;

public class ubFormToExcel2007 extends ubFormToExcelBase {

	
	/**
	 * <pre>
	 * </pre>
	 * @param pageNo 생성할 페이지 번호.
	 * */
	@Override
	public Workbook toExcelOnePage(int pageNo/*, int itemArLen */) throws Exception {
		
		//BackgroundImage의 정보를 가져오기
		createBackgroundImage();
		
		ArrayList<HashMap<String,Object>> xySet = null;
		ArrayList<Integer> _yAr = makeYArray(pageNo);
		makePositionArray2( _yAr, pageNo);
		
		int yArLen = xySetArray.size();
		
		Cell _colCell = null;
		HashMap<String, Object> _item;
		CellRangeAddress region;
		CellStyle cStyle;
		Row _row;
		float cHeight = 0f;
		String cellValue = "";
		int yPos = 0;
		int xPos = 0;
		String itemClassName = "";
		String itemClassType = "";
		String styleId = "";
		int rspan = 0;
		int cspan = 0;
		boolean isSVGTxt = false;
		
		drawBackgroundImage( rowIdx,  xArrayGlobal,  _yAr );
		
		for (int yIdx = 0; yIdx < yArLen; yIdx++) {
			
			_colCell = null;
			_item = null;
			
//			row = ( sheet.getRow(rowIdx+yIdx) == null )? sheet.createRow(rowIdx+yIdx) : sheet.getRow(rowIdx+yIdx);
			
			if( sheet.getRow(rowIdx+yIdx) == null  )
			{
				row = sheet.createRow(rowIdx+yIdx);
				row.setHeightInPoints(0);
			}
			else
			{
				row = sheet.getRow(rowIdx+yIdx);
			}
			
			if( _yAr.size() > yIdx )
			{
				row.setHeightInPoints( _yAr.get(yIdx+1) -  _yAr.get(yIdx) );
			}
			
			xySet = xySetArray.get(yIdx);
			int xArLen = xySet.size();
			for (int xIdx = 0; xIdx < xArLen; xIdx++) {
				HashMap<String, Object> xyArItem = xySet.get(xIdx);

				// type = item, none, D
				if( xyArItem == null || xyArItem.get("type") == null ) continue;
				
				// type이 none인 아이템
				if( ValueConverter.getString(xyArItem.get("type")).equals("none") ){ 
					
					// 가장 첫 row에서만 셀 너비를 지정한다. --> 아이템의 너비와 충돌하는 경우 방지.
					if( rowIdx == 0 && yIdx == 0 ){
						// Width
//						if( xyArItem.containsKey("width") ){
//							int celW = PixelUtil.pixel2WidthUnits( toNum(xyArItem.get("width")) );
//							sheet.setColumnWidth( xIdx, celW );
//						}
					}
					// Height
					if( xyArItem.containsKey("height") ){
//						float cHeight = (float) Math.min(toNum(xyArItem.get("height"))*0.75, 409);
						
						cHeight = convertRatioHeight(xyArItem.get("height"), false, null);
//						row.setHeightInPoints(cHeight);
						if( row.getHeightInPoints() < cHeight ) row.setHeightInPoints( cHeight );
						
					}
					// Border : 셀이 쪼개진 경우 NONE에 border, bgcolor 값이 들어있을 수 있음 : getBorderTypeIdx
					if( xyArItem.containsKey("uBorder") ){
						_colCell = row.createCell(xIdx);

						cStyle = null;
						styleId = getStyleId(xyArItem);
						cStyle = styleXSSFTables.get(styleId);
						 _colCell.setCellStyle(cStyle);
					 }
					else if( xIdx == xArLen -1 && yIdx == yArLen -1 )
					{
						_colCell = row.createCell(xIdx);
						_colCell.setCellValue(" ");
					}
				}
				
				// 아이템 추가하기.
				else if( xyArItem.get("type").toString().equals("item") ){
					
//					if( !ValueConverter.getBoolean(xyArItem.get("visible")) ) continue;
					if( xyArItem.get("visible") != null && xyArItem.get("visible").equals("")==false && !ValueConverter.getBoolean(xyArItem.get("visible")) ) continue;
					
					// 추가될 셀의 Y에는 rowIdx 를 더해주고,
					yPos = toNum( xyArItem.get("y") ) + rowIdx;
					xPos = toNum( xyArItem.get("x") );
					// 저장된 배열에서 해당 좌표의 아이템을 받아올때는 빼주기.
					_item = xySetArray.get(yPos-rowIdx).get(xPos);
						
					try {
						
						if( isValid(_item.get("colSpan")) && isValid(_item.get("rowSpan")) ){
							
							// end row no
							rspan = Math.max( toNum(_item.get("rowSpan"))+yPos, yPos+1 );
							// end column no
							cspan = Math.max( toNum(_item.get("colSpan"))+xPos, xPos+1 );
						
							region = new CellRangeAddress( yPos, rspan-1, xPos, cspan-1 );
							
							// 라벨 아이템인 경우
							cStyle = null;
	//						if( _item.get("className").toString().indexOf("Label") != -1 ){
							if( !isValid(_item.get("className")) ) continue;
							itemClassName = ValueConverter.getString(_item.get("className"));
							itemClassType = ValueConverter.getString(_item.get("classType"));
							
							System.out.println("ubFormToExcel2007::208::========================================================>_classType=" + itemClassType);		
							
							isSVGTxt = itemClassName.equals("UBSVGRichText");
							if( isSVGTxt ){
								_item = convertSvgTextItem(_item);
							}
							
							if( !itemClassName.equals("UBTextSignature") && ( itemClassName.contains("Label") || itemClassName.equals("UBTable") || itemClassName.equals("UBApproval") 
									|| itemClassName.equals("UBRadioBorder") || itemClassName.equals("UBCheckBox") 
									|| itemClassName.equals("UBComboBox") || itemClassName.equals("UBDateFiled") 
									|| itemClassName.contains("Text") || isSVGTxt ) ){
								
								if(itemClassType.equals("specialFontLabel"))
								{
									if( itemClassName.contains("Label") )
									{
										String _imgData = URLDecoder.decode(ValueConverter.getString(_item.get("text")),"UTF-8");
										_item.put("src", _imgData);
										addImageBase64(_item, region);
									}
									continue;
								}
								
//								if( rspan - yPos > 1 ||  cspan - xPos > 1 ) sheet.addMergedRegion( region );		//@최명진 테스트 필요 3.15버전 이상에서 셀이 깨져보이는 현상이 발생 병합된 셀이 아니면 region을 생성하지 않게
								
								if( itemClassName.equals("UBRadioBorder") || itemClassName.equals("UBCheckBox") ){
									_item = setEformItemAttr(_item);
								}
								// 콤보, 데이트필드의 경우 테두리를 그리지 않도록 한다. - 이장환이사님의견 반영.
								else if( itemClassName.equals("UBComboBox") || itemClassName.equals("UBDateFiled") ){
									_item.remove("uBorder");
									_item.put("borderWidth", "0" );
									_item.put("isCell", "false" );
									_item.put("borderType", "none" );
									_item.put("borderSide", "[]" );
								}
								
								cellValue = ValueConverter.getString(_item.get("text"));
								
								// 줄바꿈 처리
								cellValue = cellValue.replace("\\n", "\n").replaceAll("\\r", "\r");
								
								//item FontSize변경을 위해 여기서 한번 Size를 체크하여 변경시킨다. 
								_item  = convertFontSize(_item, cellValue);
								// ITEM의 rowHeight변경되는 부분 담기
								checkFitRowHeight( _item, yPos, rspan-1, cellValue);
								
								styleId = getStyleId(_item);
								cStyle = styleXSSFTables.get(styleId);
								
								// merged cell 영역에 해당 스타일을 모두지정.
								for (int _r = yPos; _r < rspan; _r++) {
									
									_row = sheet.getRow(_r);
									if( _row == null )
									{
										_row = sheet.createRow(_r);
										_row.setHeightInPoints(0);
									}
									
									for (int _c = xPos; _c < cspan; _c++) {
										
										_colCell = _row.createCell(_c);
										
										// 병합되지 않은 원본 셀에만 값을 지정한다. 
										if(_r == yPos && _c == xPos ) _colCell = setFormatType(_item, _colCell, cellValue);
//										if( isValid(cellValue) ){
//											_colCell.setCellValue(cellValue);
//										}
										
										_colCell.setCellStyle(cStyle);
										
										
										// merge 영역에 none, D의 Height 를 세팅한다.
										HashMap<String, Object> tmp = xySetArray.get(_r-rowIdx).get(_c);
										if( tmp != null && !tmp.get("type").toString().equals("item")){
											
//											float cHeight = (float) (toNum(tmp.get("height"))*0.75);
											cHeight = convertRatioHeight(tmp.get("height"), true, _item);
											
											if( _row.getHeightInPoints() < cHeight ) _row.setHeightInPoints( cHeight );
										}
										
										// style 에서 rotation 되는 경우 정렬이 깨져서 다시 정렬 해줘야함.
										//textAlign
										if( _item.containsKey("distributed") && _item.get("distributed") != null && _item.get("distributed").toString().equals("true")) 
										{
											//균등분할 옵션처리 필요 ( 대동아 요청사항 )
											cStyle.setAlignment(HorizontalAlignment.DISTRIBUTED);
											
										}
										else if( _item.containsKey("textAlign") && isValid(_item.get("textAlign")) )
										{
											switch ( ETextAlign.valueOf(ValueConverter.getString(_item.get("textAlign")))) {
												case left: cStyle.setAlignment(CellStyle.ALIGN_LEFT); break;
												case center: cStyle.setAlignment(CellStyle.ALIGN_CENTER); break;
												case right: cStyle.setAlignment(CellStyle.ALIGN_RIGHT); break;
											}
										}
										
										
										
										//verticalAlign
										if( _item.containsKey("verticalAlign") && isValid(_item.get("verticalAlign")) ){
											switch ( EVerticalAlign.valueOf(ValueConverter.getString(_item.get("verticalAlign")))) {
												case top: cStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP); break;
												case middle: cStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); break;
												case bottom: cStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM); break;
											}
										}
										// vAlign 속성이 빠져있어서 임시로 처리해놓음 추후 제거!
										else{
											cStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
										}
									}
								}
								
								// repeatValue처리를 위하여 Map에 담기
								if( useRepeatValue && _item.containsKey("datatext"))
								{
									if(mRepeatValueCheckMap==null) mRepeatValueCheckMap = new HashMap<String, ArrayList<CellRangeAddress>>();
									String _itemID = String.valueOf(xPos);
									String _repeatValueText = _item.get("datatext").toString();
									ArrayList<CellRangeAddress> _cellRangeAr;
									CellRangeAddress _cellRange;
									if( mRepeatValueCheckMap.containsKey( _itemID ) == false )
									{
										_cellRangeAr = new ArrayList<CellRangeAddress>();
										_cellRangeAr.add(region);
										
										mRepeatValueCheckMap.put( _itemID, _cellRangeAr);
//										mLastRepeatValueStringMap = new HashMap<String, String>();
									}
									else
									{
										if(  mLastRepeatValueStringMap.get(_itemID).equals(_repeatValueText) == false )
										{
											_cellRangeAr = mRepeatValueCheckMap.get(_itemID);
											_cellRangeAr.add(region);
										}
										else
										{
											_cellRangeAr =  (ArrayList<CellRangeAddress>) mRepeatValueCheckMap.get(_itemID);
											_cellRange = _cellRangeAr.get(_cellRangeAr.size()-1);
											_cellRange.setLastColumn(region.getLastColumn());
											_cellRange.setLastRow(region.getLastRow());
										}
									}
									if( mLastRepeatValueStringMap == null ) mLastRepeatValueStringMap = new HashMap<String, String>();
									mLastRepeatValueStringMap.put( _itemID, _repeatValueText);
									
								}
								else
								{
									if( rspan - yPos > 1 ||  cspan - xPos > 1 )
									{
										if(mCellRangeAddressList == null ) mCellRangeAddressList = new ArrayList<CellRangeAddress>();
										mCellRangeAddressList.add(region);
									}
//									if( rspan - yPos > 1 ||  cspan - xPos > 1 ) sheet.addMergedRegion( region );		//@최명진 테스트 필요 3.15버전 이상에서 셀이 깨져보이는 현상이 발생 병합된 셀이 아니면 region을 생성하지 않게
								}
								
							}
							// UBGraphicsLine
							else if( itemClassName.contains("Chart") == false && itemClassName.contains("Line") ){
								addLine(_item, region);
							}
							// UBGraphicsCircle
							else if( itemClassName.contains("Circle") ){
								addShape(_item, region, "circle");
							}
							// UBGraphicsRectangle || UBGraphicsGradiantRectangle
							else if( itemClassName.contains("Rectangle") ){
								addShape(_item, region, "rect");
							}
							else if( itemClassName.contains("UBClipArtContainer") ){
								String _src = CreateClipImage(_item);
								_item.put("src", _src);
								addImageBase64(_item, region);
							}
							//else if( itemClassName.contains("UBSVGArea") )
							else if( itemClassName.contains("UBSVGArea") || itemClassName.contains("UBQRCode"))	
							{
								if( itemClassName.contains("UBQRCode") )
								{
									String _svgData = ValueConverter.getString(_item.get("src"));
									_item.put("data", _svgData.substring(4));
								}
								String 	_src = CreateSVGAreaImage( _item );
								
								_item.put("src", _src);
//								_item.put("src", URLDecoder.decode( _item.get("src").toString() , "UTF-8"));
								addImageBase64(_item, region);
							}
							// 나머지 아이템들은 이미지로 추가.
							else{
								if( _item.get("src") != null ){
									addImage(_item, region, xArrayGlobal, _yAr);
								}
							}
						}

					} catch (Exception e) {
						log.error(getClass().getName() + "::toExcelOnePage::" + "xIdx : " + xIdx + ", yIdx : " + yIdx + " item parsing Error!\t"+_item.get("className")+"-"+_item.get("id"));
					}
				}// add item end
			}// x For
		}// y For
		/*
		// 이미지만 따로 뽑아서 추가하는 경우....
		
		for (HashMap<String, Object> imgItem : mImgItemList) {
			if( imgItem.get("src") != null ){
				String itemY = String.valueOf(imgItem.get("y"));
				String itemX = String.valueOf(imgItem.get("x"));
				
				if( itemY.equals("null") || itemY.equals("") || itemX.equals("null") || itemX.equals("") ) continue;
				
				int yPosImg = Integer.parseInt(itemY) + rowIdx;
				int xPosImg = Integer.parseInt(itemX);
				int rspanImg = toNum(imgItem.get("rowSpan"))+yPos;
				int cspanImg = toNum(imgItem.get("colSpan"))+xPos;

				CellRangeAddress regionImg = new CellRangeAddress( yPosImg, rspanImg-1, xPosImg, cspanImg-1 );
				
				addImage(imgItem, regionImg);
			}
		}
		
		end = new Date();
		*/
		rowIdx+=yArLen;
		
		if( isExcelOption.equals("BAND") == false ) mergedCellList();
		
		return wb;
	}
	
	
	/**
	 * 이미지 타입 아이템 생성.
	 * @param _curMap item정보를 담고있는 HashMap
	 * @param region 이미지가 추가될 영역. merge 된.
	 * @throws URISyntaxException 
	 * */
	@Override
	protected void addImage( HashMap<String, Object> _curMap, CellRangeAddress region, ArrayList<Integer> _xAr, ArrayList<Integer> _yAr ) throws URISyntaxException{
		
		 if( !mExcelIncludeImage && _curMap.containsKey("className") &&  _curMap.get("className").equals("UBImage") )
		 {
			 return;
		 }
		
		String _imageUrl = ValueConverter.getString(_curMap.get("src"));
		if( _imageUrl.equals("") || _imageUrl.equals("null") ){
			log.debug( getClass().getName()+"::addImage::"+">>>>>>>> Item's src property is not exist.");
			return;
		}
		//byte[] bAr = common.getBytesRemoteImageFile(_imageUrl);
		byte[] bAr = null;
		if(_imageUrl != null)
		{
			//int imgId = wb.addPicture(bAr, Workbook.PICTURE_TYPE_PNG);
			
			int imgId = -1;
			//
			Boolean _hasDictionary=false;
			ImageDictionaryVO _newImgDictionary=null;
			
			ImageDictionaryVO _imgDictionary = null;
			if( mImageDictionary != null ){
				_imgDictionary=mImageDictionary.getDictionaryData(_curMap.get("src").toString());
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
			}else{
				
				bAr = common.getBytesLocalImageFile(_imageUrl);				
				if(bAr == null ) return;
				imgId = wb.addPicture(bAr, Workbook.PICTURE_TYPE_PNG);
				_newImgDictionary =mImageDictionary.createPDFDictionaryData(_curMap.get("src").toString(),null);
				_newImgDictionary.setmEmbedID(String.valueOf(imgId));				
			}
			
			if(imgId == -1) return;
			
			/* Create the drawing container */
			XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
	        /* Create an anchor point */
			CreationHelper helper = wb.getCreationHelper();
			ClientAnchor my_anchor = helper.createClientAnchor();
			
//			
//			Hyperlink link = helper.createHyperlink(Hyperlink.LINK_URL);
//			link.setAddress("http://poi.apache.org/");
//			
//			XSSFHyperlink url = (XSSFHyperlink) helper.createHyperlink(XSSFHyperlink.LINK_URL);
//			url.setAddress("http://poi.apache.org/");
			
			/*
			 * Define top left corner, and we can resize picture suitable from
			 * there
			 */
			my_anchor.setCol1(region.getFirstColumn());
			my_anchor.setRow1(region.getFirstRow());
			my_anchor.setCol2(region.getLastColumn());
			my_anchor.setRow2(region.getLastRow());
			
			// 아이템 사이즈 정보로 이미지 사이즈 지정. Dimension 이용. 이 경우 resize 메서드를 호출하면 안된다.
			String itemW = String.valueOf(_curMap.get("width"));
			String itemH = String.valueOf(_curMap.get("height"));
			
			boolean _isOriginSize = false;
			if( _curMap.containsKey("isOriginalSize") &&  !_curMap.get("isOriginalSize").equals(""))
			{
				_isOriginSize = Boolean.valueOf(_curMap.get("isOriginalSize").toString());
			}
			int dx = 0;
			int dy = 0;
			if(_isOriginSize){	
				if(bAr != null){
					try {
						_image = Image.getInstance(bAr);
						HashMap<String,Float> _orignSize = common.getOriginSize(itemW,itemH,_image);
						itemW = String.valueOf( _orignSize.get("width") + _orignSize.get("marginX"));
						itemH = String.valueOf(_orignSize.get("height") + _orignSize.get("marginY"));	
						dx = Integer.valueOf(Math.round(_orignSize.get("marginX")));
						dy = Integer.valueOf(Math.round(_orignSize.get("marginY")));
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
			
			float _rotation = Float.valueOf(_curMap.get("rotate").toString());
			
			// 시작 = 0
			my_anchor.setDx1(dx * XSSFShape.EMU_PER_PIXEL);
			my_anchor.setDy1(dy * XSSFShape.EMU_PER_PIXEL);
			
			// pixel 값을 emu로 변환하기 위해 상수를 곱해준다.
			if( !itemW.equals("null") && !itemW.equals("")){
				my_anchor.setDx2(toNum(itemW) * XSSFShape.EMU_PER_PIXEL);
			}
			if( !itemH.equals("null") && !itemH.equals("")){
				my_anchor.setDy2(toNum(itemH) * XSSFShape.EMU_PER_PIXEL);
			}
			/* Invoke createPicture and pass the anchor point and ID */
			XSSFPicture my_picture = drawing.createPicture(my_anchor, imgId);
			
			if( _rotation != 0  )
			{
				CTTransform2D xfrm = createXfrm((XSSFClientAnchor) my_anchor, sheet, Float.valueOf(itemW), Float.valueOf(itemH), _rotation );
				my_picture.getCTPicture().getSpPr().setXfrm(xfrm);
			}
			
			String _fileDownloadUrl = ValueConverter.getString(_curMap.get("fileDownloadUrl"));
			if( _fileDownloadUrl != null && !(_fileDownloadUrl.equals("")) && !(_fileDownloadUrl.equals("null"))){
				PackageRelationship rel = drawing.getPackagePart().addRelationship(new URI(_fileDownloadUrl),
			            TargetMode.EXTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink");
				drawing.addRelation(rel.getId(),new POIXMLDocumentPart());
				CTHyperlink hLinkClick =my_picture.getCTPicture().getNvPicPr().getCNvPr().addNewHlinkClick();
				hLinkClick.setId(rel.getId());
			}
			
			/*
	        try
	        {
//	        	Call resize method, which resizes the image 
	        	my_picture.resize(1, 1); 
	        }
	        catch(Exception exp)
	        {
	        	exp.printStackTrace();
	        }
	        */
			
		}
		bAr = null;
		
	}
	
	/**
	 * 이미지 타입 아이템 생성.
	 * @param _curMap item정보를 담고있는 HashMap
	 * @param region 이미지가 추가될 영역. merge 된.
	 * @throws URISyntaxException 
	 * @throws UnsupportedEncodingException 
	 * */
	@Override
	protected void addImageBase64( HashMap<String, Object> _curMap, CellRangeAddress region ) throws URISyntaxException, UnsupportedEncodingException{
		
		String _imageUrl = ValueConverter.getString(_curMap.get("src"));
		if( _imageUrl.equals("") || _imageUrl.equals("null") ){
			log.error(getClass().getName()+"::addImage::"+">>>>>>>> Item's src property is not exist.");
			return;
		}
		//byte[] bAr = common.getBytesRemoteImageFile(_imageUrl);
		byte[] bAr = null;
		if(_imageUrl != null)
		{
			int imgId = -1;
			
			Boolean _hasDictionary=false;
			ImageDictionaryVO _newImgDictionary=null;
			
			ImageDictionaryVO _imgDictionary = null;
			if( mImageDictionary != null ){
				_imgDictionary=mImageDictionary.getDictionaryData(_curMap.get("src").toString());
			}else{
				mImageDictionary = new ImageDictionary();
			}
			
			if( _imgDictionary == null ){
				_hasDictionary = false;
			}else{
				_hasDictionary = true;
			}
			
			if( _hasDictionary ){
				imgId = Integer.parseInt(_imgDictionary.getmEmbedID());
			}else{
				bAr = Base64.decodeBase64(_curMap.get("src").toString().getBytes("UTF-8"));
				
				if(bAr == null) return;
				
				imgId = wb.addPicture(bAr, Workbook.PICTURE_TYPE_PNG);
				_newImgDictionary =mImageDictionary.createPDFDictionaryData(_curMap.get("src").toString(),null);
				_newImgDictionary.setmEmbedID(String.valueOf(imgId));
			}
			
			if( imgId == -1 ) return;
			
			/* Create the drawing container */
			XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
	        /* Create an anchor point */
			CreationHelper helper = wb.getCreationHelper();
			ClientAnchor my_anchor = helper.createClientAnchor();
			
			my_anchor.setCol1(region.getFirstColumn());
			my_anchor.setRow1(region.getFirstRow());
			my_anchor.setCol2(region.getLastColumn());
			my_anchor.setRow2(region.getLastRow());
			
			// 아이템 사이즈 정보로 이미지 사이즈 지정. Dimension 이용. 이 경우 resize 메서드를 호출하면 안된다.
			String itemW = String.valueOf(_curMap.get("width"));
			String itemH = String.valueOf(_curMap.get("height"));
			
			// 시작 = 0
			my_anchor.setDx1(0);
			my_anchor.setDy1(0);
			
			// pixel 값을 emu로 변환하기 위해 상수를 곱해준다.
			if( !itemW.equals("null") && !itemW.equals("")){
//				my_anchor.setDx2(toNum(itemW) * XSSFShape.EMU_PER_PIXEL);
				my_anchor.setDx2( Units.pixelToEMU( toNum(itemW) ) );
			}
			if( !itemH.equals("null") && !itemH.equals("")){
//				my_anchor.setDy2(toNum(itemH) * XSSFShape.EMU_PER_PIXEL);
				my_anchor.setDy2( Units.pixelToEMU(toNum(itemH)) );
			}
			
			/* Invoke createPicture and pass the anchor point and ID */
			XSSFPicture my_picture = drawing.createPicture(my_anchor, imgId);
			
			String _fileDownloadUrl = ValueConverter.getString(_curMap.get("fileDownloadUrl"));
			if( _fileDownloadUrl != null && !(_fileDownloadUrl.equals("")) ){
				PackageRelationship rel = drawing.getPackagePart().addRelationship(new URI(_fileDownloadUrl),
			            TargetMode.EXTERNAL, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink");
				drawing.addRelation(rel.getId(),new POIXMLDocumentPart());
				CTHyperlink hLinkClick =my_picture.getCTPicture().getNvPicPr().getCNvPr().addNewHlinkClick();
				hLinkClick.setId(rel.getId());
			}
			
		}
		
		bAr = null;
	}
	
	/**
	 * 라인 아이템 생성.
	*/
	@Override
	protected void addLine( HashMap<String, Object> _item, CellRangeAddress region ) {
		XSSFDrawing patriarch= (XSSFDrawing) sheet.createDrawingPatriarch();

		int x1 = toNum(_item.get("x1"));
		int x2 = toNum(_item.get("x2"));
		int y1 = toNum(_item.get("y1"));
		int y2 = toNum(_item.get("y2"));
		
		//int itemW = toNum(_item.get("x2"))* XSSFShape.EMU_PER_PIXEL;
		int itemW = Math.abs(x2 - x1) * XSSFShape.EMU_PER_PIXEL;
		//int itemH = Math.max(y1, y2)* XSSFShape.EMU_PER_PIXEL;
		int itemH = Math.abs(y2 - y1)* XSSFShape.EMU_PER_PIXEL;
		
		int _stX = 0;
		int _edX = itemW;
		int _stY = 0;
		int _edY = itemH;
		
		if( y2 < y1 )
		{
			_stY = itemH;
			_edY = 0;
		}
		
		XSSFClientAnchor anchor = patriarch.createAnchor(
												0, //dx1
												0, //dy1
												itemW, //dx2
												itemH, //dy2
												region.getFirstColumn(), //col1
												region.getFirstRow(), //row1
												region.getLastColumn(), //col2
												region.getLastRow()); //row2
		
		
		XSSFSimpleShape regionShape = patriarch.createSimpleShape(anchor);
		
		// Thick
		regionShape.setLineWidth(toPoint(_item.get("thickness")));
		// Color
		java.awt.Color _c = new java.awt.Color(ValueConverter.getInteger(_item.get("lineColorInt")));
		regionShape.setLineStyleColor(_c.getRed(), _c.getGreen(), _c.getBlue());
		
		
		if( (x1 > x2 && y1 < y2) || ( y1 > y2 && x2 > x1 )  )
		{
			regionShape.setShapeType(ShapeTypes.LINE_INV);
		}
		else
		{
			regionShape.setShapeType(ShapeTypes.LINE);
		}
		
//		if( x1 < x2 ){
//			regionShape.setShapeType(ShapeTypes.LINE);
//		}
//		else{
//			regionShape.setShapeType(ShapeTypes.LINE_INV);
//		}
		
	}
	
	/**
	 * 타입에 따라 원/사각형 아이템 생성.
	 * @param type circle : 원 / rect:사각형
	 */
	@Override
	protected void addShape( HashMap<String, Object> _item, CellRangeAddress region, String type ) {
		XSSFDrawing patriarch= (XSSFDrawing) sheet.createDrawingPatriarch();
		
		int itemW = toNum(_item.get("width"))* XSSFShape.EMU_PER_PIXEL;
		int itemH = toNum(_item.get("height"))* XSSFShape.EMU_PER_PIXEL;
		
//		int _fCol = Integer.parseInt(_item.get("x").toString());
//		int _fRow = Integer.parseInt(_item.get("y").toString());
//		int _lCol = Integer.parseInt(_item.get("colSpan").toString()) -1;
//		int _lRow = Integer.parseInt(_item.get("rowSpan").toString()) -1;
//		
//		if( _fCol == -1 || _fRow == -1 || _lCol == -1 || _lRow == -1 ) return;
		
		XSSFClientAnchor anchor = patriarch.createAnchor(
				0, //dx1
				0, //dy1
				itemW, //dx2
				itemH, //dy2
				region.getFirstColumn(), //col1
				region.getFirstRow(), //row1
				region.getLastColumn(), //col2
				region.getLastRow()); //row2
		XSSFSimpleShape regionShape = patriarch.createSimpleShape(anchor);

		// Color
		if( String.valueOf(_item.get("className")).equals("UBGraphicsGradiantRectangle")){
			// cell fill gradient sample : http://thinktibits.blogspot.com.au/2014/09/apache-poi-excel-gradient-fill-example.html
			// gradiant 인 경우는 첫번째 색으로 채운다. size=2
			String[] bgColor = jsonArrayToArray(ValueConverter.getString(_item.get("contentBackgroundColorsInt")));
			java.awt.Color _c = new java.awt.Color(ValueConverter.getInteger(bgColor[0]));
			regionShape.setFillColor(_c.getRed(), _c.getGreen(), _c.getBlue());
		}
		else{
			java.awt.Color _c = new java.awt.Color(ValueConverter.getInteger(_item.get("contentBackgroundColorInt")));
			regionShape.setFillColor(_c.getRed(), _c.getGreen(), _c.getBlue());
		}
		
		// border
		int borderThickness = toNum(_item.get("borderThickness"));
		if( borderThickness > 0 ){
			// Thick
			regionShape.setLineWidth(toPoint(borderThickness));
			// Color
			java.awt.Color _c = new java.awt.Color(ValueConverter.getInteger(_item.get("borderColorInt")));
			regionShape.setLineStyleColor(_c.getRed(), _c.getGreen(), _c.getBlue());
		}
		
		// type
		int shapeType = ShapeTypes.ELLIPSE;
		if( type.equals("circle") ){
			shapeType = ShapeTypes.ELLIPSE;
		}
		else if( type.equals("rect") ){
			shapeType = ShapeTypes.RECT;
		}
		regionShape.setShapeType(shapeType);
		
	}
	
	
	@Override
	protected String CreateClipImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException, TranscoderException
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
        
        String base64 = new String(Base64.encodeBase64(((ByteArrayOutputStream) png_ostream).toByteArray()));
        return base64;
        
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
	@Override
	protected String CreateSVGAreaImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException, TranscoderException
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
        
		log.debug(getClass().getName()+"::CreateSVGAreaImage::" + "oWidth=" + oWidth + ",oHeight=" + oHeight);
		
		//String _svgTag = URLDecoder.decode( _item.get("data").toString() );
		String _svgTag = URLDecoder.decode( ValueConverter.getString(_item.get("data")) , "UTF-8");
		_svgTag = _svgTag.replaceAll("%20", " ");
		
		String itemClassName = (String) _item.get("className");
		if( itemClassName != null && itemClassName.contains("UBQRCode") )
		{
			_svgTag = Base64Decoder.decode(_svgTag);
			System.out.print("CreateSVGAreaImage::UBQRCode:svg=[" + _svgTag + "]");
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
        
        String base64 = new String(Base64.encodeBase64(((ByteArrayOutputStream) png_ostream).toByteArray()));
        return base64;
        
	}
	
	/***
	 * Excel 저장시 SVG 차트 이미지 저장
	 * @param _item
	 * @return
	 * @throws DocumentException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws TranscoderException
	 * @throws JDOMException 
	 */
	@Override
	protected String CreateSVGLabelImage( HashMap<String, Object> _item ) throws DocumentException, MalformedURLException, IOException, TranscoderException, JDOMException
	{
		float _ow = ValueConverter.getFloat(_item.get("width"));
		float _oh = ValueConverter.getFloat(_item.get("height"));
		
		//String _svgTag = URLDecoder.decode( _item.get("data").toString() );
		String _svgTag = URLDecoder.decode( ValueConverter.getString(_item.get("data")) , "UTF-8");
		_svgTag = _svgTag.replaceAll("%20", " ");
		
		// viewBox 를 이용해서 사이즈 변경 //		
		int _startIdx = _svgTag.indexOf("<svg");
		if( _startIdx == -1 ) 
		{
			_svgTag = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"  width=\"" + _ow + "\" height=\"" + _oh + "\" viewBox=\"0 0 " + _ow + " " + _oh + "\" zoomAndPan=\"disable\" >" + _svgTag;
			_svgTag = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>" + _svgTag;
			_svgTag = _svgTag + "</svg>";
		}
		
		_startIdx = _svgTag.indexOf("<svg");
		if( _startIdx == -1 ) {
			return null; // svg tag 가 아니면 그리지 않는다..
		}
		
		//_dataStr = _dataStr.replaceAll("fill=\"rgba(255,255,255,0)\"", "fill=\"rgb(255,255,255)\" fill-opacity=\"0\"");
		// DOM에 SVG 문서를 넣고, 위와 같이 속상값을 찾아서 변경을 해주도록 한다.
		InputStream stream = new ByteArrayInputStream(_svgTag.getBytes("UTF-8"));
		org.jdom2.Document doc = (org.jdom2.Document) new SAXBuilder().build(stream);

		org.jdom2.Element xmlRoot = (org.jdom2.Element) doc.getRootElement(); //<svg>
   	 	List<org.jdom2.Element> firstItemList = xmlRoot.getChildren();
   	 	for (org.jdom2.Element firstItem : firstItemList) 
	   	{
	   		List<org.jdom2.Element> secondItemList = firstItem.getChildren(); // Item
	   		for (org.jdom2.Element secondItem : secondItemList) {
				 String secondItemFillAttr = secondItem.getAttributeValue("fill");
				 if( secondItemFillAttr != null && secondItemFillAttr.contains("rgba") ){
					 int beginIndex = secondItemFillAttr.indexOf("(");
					 int endIndex = secondItemFillAttr.indexOf(")");
					 String fillAttRgbaValue = secondItemFillAttr.substring(beginIndex+1, endIndex);
					 String[] arrfillAttRgbaValues = fillAttRgbaValue.split(",");
					 
					 secondItem.setAttribute("fill", "rgb(" + arrfillAttRgbaValues[0] + "," + arrfillAttRgbaValues[1] + "," + arrfillAttRgbaValues[2] + ")");
					 secondItem.setAttribute("fill-opacity", arrfillAttRgbaValues[3]);
				 }
			}
		}
   	 	_svgTag = new XMLOutputter().outputString(doc);   	 	
   
		log.debug(getClass().getName()+"::"+"ubFormToExcel2007::"+"SVGTAG=[" + _svgTag + "]");				
		
		InputStream _is = new ByteArrayInputStream( _svgTag.getBytes("UTF-8") );
		
        TranscoderInput input_svg_image = new TranscoderInput( _is ); 

        OutputStream png_ostream = new ByteArrayOutputStream();
        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);        
        
        PNGTranscoder my_converter = new PNGTranscoder();
        my_converter.transcode(input_svg_image, output_png_image);
        
        png_ostream.flush();
        
        String base64 = new String(Base64.encodeBase64(((ByteArrayOutputStream) png_ostream).toByteArray()));
        return base64;
        
	}
	
	/**
	 * 아이템에서 셀스타일 지정에 필요한 속성을 String 으로 뽑은  HashMap 을 임의의 Key 값과 매칭시켜 저장한다.
	 * */
	private HashMap<String,HashMap<String, String>> styleStringTables = new HashMap<String,HashMap<String,String>>();
	/**
	 * styleStringTables 의 key 와 동일한 key로 XSSFCellStyle 이 매핑될 테이블.
	 * */
	private HashMap<String,XSSFCellStyle> styleXSSFTables = new HashMap<String,XSSFCellStyle>();
	
	public boolean clearStyleTable()
	{
		styleXSSFTables.clear();
		styleStringTables.clear();
		return true;
	}
	
	
	/**
	 * 스타일 아이디 값 지정을 위한 상수. <b>"style"+styleCnt</b> 로 아이디 생성.
	 * */
	private int styleCnt = 0;
	
	/**
	 * 비교대상아이템의 Style String Map 을 만들어 styleStringTables 에 저장된 스타일이 있는지 찾는다. 없으면 아이템 정보를 바탕으로 새 스타일을 만든다.
	 * @param _item 비교할 아이템
	 * @return String 찾은, 혹은 새로 생성한 스타일 ID
	 * */
	private String getStyleId( HashMap<String, Object> _item ) throws Exception {
		
		int styleTableLen = styleStringTables.size();
		
		// 현재 아이템의 스타일 값을 모아서 스타일이 존재하는지 찾는다.
		HashMap<String, String> itemStyles = setItemStringStyle(_item);
		if( styleTableLen != 0 ){
			for (String key : styleStringTables.keySet()) {
				if( itemStyles.equals(styleStringTables.get(key))){
					// 존재하면 해당 key 값을 리턴.
					return key;
				}
			}
		}
		
		String styleId = "style"+styleCnt;
		createStyleToTable(styleId, itemStyles);
		styleStringTables.put(styleId, itemStyles);
		styleCnt++;
		return styleId;
	}

	/**
	 * 아이템의 스타일 생성에 필요한 정보를 뽑아 새로운 해시맵을 만든다. 
	 * @param _item 스타일 체크할 아이템
	 * @param styleType [ font, cell ] 
	 * @return HashMap<String, String> String 정보가 담긴 배열.
	 * */
	private HashMap<String, String> setItemStringStyle( HashMap<String, Object> _item ) {
		HashMap<String, String> itemStyles = new HashMap<String, String>();

		if( _item.containsKey("backgroundColorInt") && isValid(_item.get("backgroundColorInt")) ) itemStyles.put("backgroundColorInt", ValueConverter.getString(_item.get("backgroundColorInt")));
		
		if( _item.containsKey("isCell") && isValid(_item.get("isCell")) ) itemStyles.put("isCell", ValueConverter.getString(_item.get("isCell")));
		
		boolean isCell = ValueConverter.getBoolean(_item.get("isCell"));
		if( isCell ){
			if( _item.containsKey("borderColorsInt") && isValid(_item.get("borderColorsInt")) ) itemStyles.put("borderColorsInt", ValueConverter.getString(_item.get("borderColorsInt")));
			if( _item.containsKey("borderWidths") && isValid(_item.get("borderWidths")) ) itemStyles.put("borderWidths", ValueConverter.getString(_item.get("borderWidths")));
			if( _item.containsKey("borderTypes") && isValid(_item.get("borderTypes")) ) itemStyles.put("borderTypes", ValueConverter.getString(_item.get("borderTypes")));
		}
		else{
			if( _item.containsKey("borderWidth") && isValid(_item.get("borderWidth")) ) itemStyles.put("borderWidth", ValueConverter.getString(_item.get("borderWidth")));
			if( _item.containsKey("borderColorInt") && isValid(_item.get("borderColorInt")) ) itemStyles.put("borderColorInt", ValueConverter.getString(_item.get("borderColorInt")));
			if( _item.containsKey("borderType") && isValid(_item.get("borderType")) ) itemStyles.put("borderType", ValueConverter.getString(_item.get("borderType")));
		}
		
		if( _item.containsKey("uBorder")){
			if( _item.containsKey("uBorder") && isValid(_item.get("uBorder")) ) itemStyles.put("uBorder", ValueConverter.getString(_item.get("uBorder")));
			HashMap<String, HashMap<String, Object>> _uBorder = (HashMap<String, HashMap<String, Object>>) _item.get("uBorder");
			if( _uBorder.containsKey("T") ){
				HashMap<String, Object> _T = _uBorder.get("T");
				for (String key : _T.keySet()) {
					itemStyles.put("uBorder_T_"+key, ValueConverter.getString(_T.get(key)));
				}
			}
			if( _uBorder.containsKey("L") ){
				HashMap<String, Object> _L = _uBorder.get("L");
				for (String key : _L.keySet()) {
					itemStyles.put("uBorder_L_"+key, ValueConverter.getString(_L.get(key)));
				}
			}
			if( _uBorder.containsKey("R") ){
				HashMap<String, Object> _R = _uBorder.get("R");
				for (String key : _R.keySet()) {
					itemStyles.put("uBorder_R_"+key, ValueConverter.getString(_R.get(key)));
				}
			}
			if( _uBorder.containsKey("B") ){
				HashMap<String, Object> _B = _uBorder.get("B");
				for (String key : _B.keySet()) {
					itemStyles.put("uBorder_B_"+key, ValueConverter.getString(_B.get(key)));
				}
			}
		}
		if( _item.containsKey("borderSide") && isValid(_item.get("borderSide")) ) itemStyles.put("borderSide", ValueConverter.getString(_item.get("borderSide")));
		if( _item.containsKey("textRotate") && isValid(_item.get("textRotate")) ) itemStyles.put("textRotate", ValueConverter.getString(_item.get("textRotate")));
		if( _item.containsKey("textAlign") && isValid(_item.get("textAlign")) ) itemStyles.put("textAlign", ValueConverter.getString(_item.get("textAlign")));
		if( _item.containsKey("verticalAlign") && isValid(_item.get("verticalAlign")) ) itemStyles.put("verticalAlign", ValueConverter.getString(_item.get("verticalAlign")));
		
		// Font 생성여부를 판단하기 위해 text가 있으면, 임의의 값인 true 를 스타일 맵에 추가한다.(스타일만 체크할 것이므로 value 를 넣으면 안됨.) 
		// createStyleToTable 에서 containsKey("text") 만 체크해서 판단함.
		if( _item.containsKey("text") && isValid(_item.get("text")) ) itemStyles.put("text", "true");
		if( _item.containsKey("textDecoration") && isValid(_item.get("textDecoration")) ) itemStyles.put("textDecoration", ValueConverter.getString(_item.get("textDecoration")));
		if( _item.containsKey("fontStyle") && isValid(_item.get("fontStyle")) ) itemStyles.put("fontStyle", ValueConverter.getString(_item.get("fontStyle")));
		if( _item.containsKey("fontSize") && isValid(_item.get("fontSize")) ) itemStyles.put("fontSize", ValueConverter.getString(_item.get("fontSize")));
		if( _item.containsKey("fontWeight") && isValid(_item.get("fontWeight")) ) itemStyles.put("fontWeight", ValueConverter.getString(_item.get("fontWeight")));
		if( _item.containsKey("fontFamily") && isValid(_item.get("fontFamily")) ) itemStyles.put("fontFamily", ValueConverter.getString(_item.get("fontFamily")));
		if( _item.containsKey("fontColorInt") && isValid(_item.get("fontColorInt")) ) itemStyles.put("fontColorInt", ValueConverter.getString(_item.get("fontColorInt")));
		
		// 포맷터 정보 담기
		if(_item.containsKey("EX_FORMATTER") && isValid(_item.get("EX_FORMATTER")) )
		{
			itemStyles.put("EX_FORMATTER", ValueConverter.getString( _item.get("EX_FORMATTER") ) );
			itemStyles.put("EX_FORMAT_DATA_STR", ValueConverter.getString( _item.get("EX_FORMAT_DATA_STR") ) );
		}
		
		if( _item.containsKey("distributed") && _item.get("distributed") != null )
		{
			itemStyles.put("distributed", ValueConverter.getString( _item.get("distributed") ) );
		}
		
		return itemStyles;
	}
	
	/**
	 * 실제 XSSFCellStyle 을 생성하는 함수. styleXSSFTables에 저장됨.
	 * @param id CellStyle 구분자. [style0, style1 ... ]
	 * @param itemStyles setItemStringStyle에서 만들어진 문자열로 이루어진 스타일 정보.
	 * */
	private void createStyleToTable( String id, HashMap<String, String> itemStyles ) throws Exception {
		XSSFCellStyle cStyle = (XSSFCellStyle) wb.createCellStyle();
		//backgroundColor
		if( itemStyles.containsKey("backgroundColorInt") && isValid(itemStyles.get("backgroundColorInt"))){
			cStyle.setFillForegroundColor( new XSSFColor(changeColorToByteAr(ValueConverter.getInteger(itemStyles.get("backgroundColorInt")))) );
			cStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		}
		
		cStyle.setWrapText(true);
		
		// uBorder
		if( itemStyles.containsKey("uBorder") ){
			if(itemStyles.containsKey("uBorder_T_borderType")) cStyle.setBorderTop(ValueConverter.getShort(itemStyles.get("uBorder_T_borderType")));
			if(itemStyles.containsKey("uBorder_T_borderColorInt")) cStyle.setTopBorderColor(new XSSFColor(changeColorToByteAr(ValueConverter.getInteger(itemStyles.get("uBorder_T_borderColorInt")))));
			
			if(itemStyles.containsKey("uBorder_L_borderType")) cStyle.setBorderLeft(ValueConverter.getShort(itemStyles.get("uBorder_L_borderType")));
			if(itemStyles.containsKey("uBorder_L_borderColorInt")) cStyle.setLeftBorderColor(new XSSFColor(changeColorToByteAr(ValueConverter.getInteger(itemStyles.get("uBorder_L_borderColorInt")))));
			
			if(itemStyles.containsKey("uBorder_R_borderType")) cStyle.setBorderRight(ValueConverter.getShort(itemStyles.get("uBorder_R_borderType")));
			if(itemStyles.containsKey("uBorder_R_borderColorInt")) cStyle.setRightBorderColor(new XSSFColor(changeColorToByteAr(ValueConverter.getInteger(itemStyles.get("uBorder_R_borderColorInt")))));
			
			if(itemStyles.containsKey("uBorder_B_borderType")) cStyle.setBorderBottom(Short.valueOf(itemStyles.get("uBorder_B_borderType")));
			if(itemStyles.containsKey("uBorder_B_borderColorInt")) cStyle.setBottomBorderColor(new XSSFColor(changeColorToByteAr(ValueConverter.getInteger(itemStyles.get("uBorder_B_borderColorInt")))));
		}
		//borderSide [top, bottom, left, right] : 순서는 바뀔 수 있음.
		else if( itemStyles.containsKey("borderSide") ){
			// borderSide 정보에 따라 테두리 지정.
			String _b = ValueConverter.getString(itemStyles.get("borderSide"));
			if( !isValid(_b) || _b.equals("[]")){
				// side 없음!
			}
			else{
				// 1.  테이블 속성인지 아닌지에 따라 arProps 에 정보를 저장한다.
				
				// 테두리 관련 속성 정보를 저장할 맵.
				HashMap<String, Object> arProps = new HashMap<String, Object>();

				boolean isCell = ValueConverter.getBoolean(itemStyles.get("isCell"));
				
				if( isCell ){
					// isCell = true. 속성이 배열.
					
					//borderColorsInt
					if( itemStyles.containsKey("borderColorsInt") ){
						arProps.put("borderColor", arrayToString( jsonArrayToArray(itemStyles.get("borderColorsInt")) ));
					}
					//borderTypes
					String bdWidthAr = itemStyles.get("borderWidths");
					String bdTypeAr = itemStyles.get("borderTypes");
					if( isValid(bdTypeAr) ){
						//borderWidths
						String [] bWidths = (isValid(bdWidthAr))? jsonArrayToArray(bdWidthAr) : null;
						String [] bTypes = jsonArrayToArray( bdTypeAr );
						if( bTypes != null ){
							String typeArrayString = "";
							int len = bTypes.length;
							for (int i = 0; i < len; i++) {
								if( bWidths != null && bWidths.length > i ){
									typeArrayString += getBorderTypeIdx( bTypes[i], ValueConverter.getInteger(bWidths[i]));
								}
								else{
									typeArrayString += getBorderTypeIdx( bTypes[i], 1);
								}
								if( i != len-1 ){
									typeArrayString += ",";
								}
							}
							arProps.put("borderType", typeArrayString);
						}
					}
				}
				else{
					// isCell = false. 속성이 단일 값.
					int borderWidth = (isValid(itemStyles.get("borderWidth")))? ValueConverter.getInteger(itemStyles.get("borderWidth")):1;
					//borderColor
					if( isValid(itemStyles.get("borderColorInt")) ){
						arProps.put("borderColor", itemStyles.get("borderColorInt"));
					}
					// borderType
					if( isValid(itemStyles.get("borderType")) ){
						arProps.put("borderType", getBorderTypeIdx(itemStyles.get("borderType"), borderWidth));
					}
				}
				
				ArrayList<String> bStrList = new ArrayList<String>(Arrays.asList(jsonArrayToArray(_b)));
				if( bStrList == null || bStrList.size() == 0 ){
					// border None!
				}
				else{
					// borderSide 배열을 기준으로 boderWidth, borderColor, borderType 스타일 적용.
					int size = bStrList.size();
					for (int i = 0; i < size; i++) {
						int bColor = 0;
						short bType = 0;
						
						// 속성이 여러 개(테이블) 인지 한 개 인지에 따라 값 지정. 
						if( isCell ){
							// isCell = true : 속성값을 ,로 쪼개서, borderSide 인덱스와 일치하는 값을 대입.
							if( arProps.containsKey("borderColor") && isValid(arProps.get("borderColor")) ){
								String[] bColorStrAr = ValueConverter.getString(arProps.get("borderColor")).trim().split(",");
								if( bColorStrAr.length > i ) bColor = Integer.valueOf(bColorStrAr[i]);
							}
							if( arProps.containsKey("borderType") && isValid(arProps.get("borderType")) ){
								String[] bTypeStr = ValueConverter.getString(arProps.get("borderType")).trim().split(",");
								if( bTypeStr.length > i ) bType = Short.valueOf(bTypeStr[i]);
							}
						}
						else{
							// isCell = false : 값을 그대로 받아옴.
							if( arProps.containsKey("borderColor") && isValid(arProps.get("borderColor")) ) bColor = ValueConverter.getInteger(arProps.get("borderColor"));
							if( arProps.containsKey("borderType") && isValid(arProps.get("borderType")) ) bType = ValueConverter.getShort(arProps.get("borderType"));
						}
						
						// boderSide의 값에 따라 스타일 지정.
						if( bStrList.get(i).equals("left") ){
							cStyle.setLeftBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
							cStyle.setBorderLeft(bType);
						}
						else if( bStrList.get(i).equals("right") ){
							cStyle.setRightBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
							cStyle.setBorderRight(bType);
						}
						else if( bStrList.get(i).equals("top") ){
							cStyle.setTopBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
							cStyle.setBorderTop(bType);
						}
						else if( bStrList.get(i).equals("bottom") ){
							cStyle.setBottomBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
							cStyle.setBorderBottom(bType);
						}
						
					}// End boderSide For
				}
			}
		}// end borderSide

		//textRotate
		if( itemStyles.containsKey("textRotate") && isValid(itemStyles.get("textRotate")) ){
			// -90 ~ 90
			short rotation = 0;
			switch (ValueConverter.getShort(itemStyles.get("textRotate"))) {
			case 0: rotation = 0; break;
			case 45: rotation = 120; break;
			case 90: rotation = 180; break;
			case 180: rotation = 0; break; // 180 은 지원안됨.
			case 270: rotation = 90; break;
			}
			cStyle.setRotation(rotation);
		}
		
		
		
		
		// 폰트가 많이 생성되면 속도가 저하됨. 아이템에 텍스트가 있을때만 생성하도록 한다.
		if( itemStyles.containsKey("text") ){
			XSSFFont font = (XSSFFont) wb.createFont();
			//textDecoration
			if( isValid(itemStyles.get("textDecoration")) ){
				switch (ETextDecoration.valueOf(itemStyles.get("textDecoration"))) {
				case none: font.setUnderline(Font.U_NONE); break;
				case normal: font.setUnderline(Font.U_NONE); break;
				case underline: font.setUnderline(Font.U_SINGLE); break;
				}
			}
			//fontStyle
			if( isValid(itemStyles.get("fontStyle")) ){
				switch (EFontStyle.valueOf(itemStyles.get("fontStyle"))) {
				case italic: font.setItalic(true); break;
				case normal: font.setItalic(false); break;
				}
			}
			//fontSize
			if( isValid(itemStyles.get("fontSize")) ){
//				font.setFontHeightInPoints( (short)(toNum(fSize)*0.75) );
				font.setFontHeight(toPoint(itemStyles.get("fontSize")));
			}
			//fontWeight
			if( isValid(itemStyles.get("fontWeight")) ){
				switch (EFontWeight.valueOf(itemStyles.get("fontWeight"))) {
				case normal: font.setBold(false); break;
				case bold: font.setBold(true); break;
				}
			}
			//fontFamily
			if( isValid(itemStyles.get("fontFamily")) ){
				font.setFontName( URLDecoder.decode(itemStyles.get("fontFamily"), "UTF-8") );
			}
			//fontColor
			if( isValid(itemStyles.get("fontColorInt")) ){
				font.setColor(new XSSFColor(changeColorToByteAr( ValueConverter.getInteger(itemStyles.get("fontColorInt")) )));
			}
			
			cStyle.setFont(font);
		}
		
		// 아이템의 Formatter 속성 추가
		if( itemStyles.containsKey("EX_FORMATTER") )
		{
			makeCellFormatter( itemStyles.get("EX_FORMATTER"), itemStyles.get("EX_FORMAT_DATA_STR"), cStyle );
		}
		
		if( itemStyles.containsKey("distributed") && itemStyles.get("distributed") != null && itemStyles.get("distributed").toString().equals("true")) 
		{
			//균등분할 옵션처리 필요 ( 대동아 요청사항 )
			cStyle.setAlignment(HorizontalAlignment.DISTRIBUTED);
		}
		
		
		styleXSSFTables.put(id, cStyle);
		
	}
	
	/** 
	 * 라벨 아이템 Cell의 스타일을 지정한다.
	 * @param c : 대상 셀. org.apache.poi.ss.usermodel.Cell
	 * @param x : column index
	 * @param y : row index
	 * @throws UnsupportedEncodingException 
	 * */
	private Cell setLabelCell( Cell c, HashMap<String, Object> _item ) throws Exception{

		XSSFCellStyle cStyle = (XSSFCellStyle) c.getCellStyle();
		c.setCellType(Cell.CELL_TYPE_STRING);
		
		//backgroundColor
		if( _item.containsKey("backgroundColorInt") && isValid(_item.get("backgroundColorInt")) ){
			cStyle.setFillForegroundColor( new XSSFColor(changeColorToByteAr(ValueConverter.getInteger(_item.get("backgroundColorInt")))) );
			cStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		}
		
		boolean isCell = ValueConverter.getBoolean(_item.get("isCell"));
		
		// 배열로 들어오는 경우가 있는 속성들의 맵.
		HashMap<String, Object> arProps = new HashMap<String, Object>();
		if( isCell ){
			// isCell = true. 속성이 배열.
			
			//borderColorsInt
			String bdColorAr = String.valueOf(_item.get("borderColorsInt"));
			if( !bdColorAr.equals("null") && !bdColorAr.equals("")  ){
				arProps.put("borderColor", arrayToString( jsonArrayToArray(bdColorAr) ));
			}
			//borderTypes
			String bdWidthAr = String.valueOf(_item.get("borderWidths"));
			String bdTypeAr = String.valueOf(_item.get("borderTypes"));
			if( !bdTypeAr.equals("null") && !bdTypeAr.equals("") ){
				//borderWidths
				String [] bWidths = (bdWidthAr.equals("null") || bdWidthAr.equals(""))? null : jsonArrayToArray(bdWidthAr);
				String [] bTypes = jsonArrayToArray( bdTypeAr );
				if( bTypes != null ){
					String typeArrayString = "";
					int len = bTypes.length;
					for (int i = 0; i < len; i++) {
						if( bWidths != null && bWidths.length > i ){
							typeArrayString += getBorderTypeIdx( bTypes[i], Integer.valueOf(bWidths[i]));
						}
						else{
							typeArrayString += getBorderTypeIdx( bTypes[i], 1);
						}
						if( i != len-1 ){
							typeArrayString += ",";
						}
					}
					arProps.put("borderType", typeArrayString);
				}
			}
		}
		else{
			// isCell = false. 속성이 단일 값.
			//borderWidth
			String bdWidth = String.valueOf(_item.get("borderWidth"));
			int borderWidth = (bdWidth.equals("null") || bdWidth.equals(""))? 1 : Integer.valueOf(bdWidth);
			//borderColor
			String bdColor = String.valueOf(_item.get("borderColorInt"));
			if( !bdColor.equals("null") && !bdColor.equals("") ){
				arProps.put("borderColor", bdColor);
			}
			// borderType
			String bdType = String.valueOf(_item.get("borderType"));
			if( !bdType.equals("null") && !bdType.equals("") ){
				arProps.put("borderType", getBorderTypeIdx(bdType, borderWidth));
			}
		}
		
		if( _item.containsKey("uBorder") ){

			 HashMap<String, HashMap<String, Object>> _uBorder =  (HashMap<String, HashMap<String, Object>>)_item.get("uBorder") ;
			 HashMap<String, Object> borderProp = null;
			 int bColor = 0;
			 short bType = 0;
			 if( _uBorder.containsKey("L") )
			 {
				borderProp = _uBorder.get("L");
				bColor = Integer.valueOf(String.valueOf(borderProp.get("borderColorInt")));
				cStyle.setLeftBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
				bType = Short.valueOf(String.valueOf(borderProp.get("borderType")));
				cStyle.setBorderLeft(bType);
			 }
			 if( _uBorder.containsKey("R") )
			 {
				borderProp = _uBorder.get("R");
				bColor = Integer.valueOf(String.valueOf(borderProp.get("borderColorInt")));
				cStyle.setRightBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
				bType = Short.valueOf(String.valueOf(borderProp.get("borderType")));
				cStyle.setBorderRight(bType);
			 }
			 if( _uBorder.containsKey("T") )
			 {
				borderProp = _uBorder.get("T");
				bColor = Integer.valueOf(String.valueOf(borderProp.get("borderColorInt")));
				cStyle.setTopBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
				bType = Short.valueOf(String.valueOf(borderProp.get("borderType")));
				cStyle.setBorderTop(bType);
			 }
			 if( _uBorder.containsKey("B") )
			 {
				borderProp = _uBorder.get("B");
				bColor = Integer.valueOf(String.valueOf(borderProp.get("borderColorInt")));
				cStyle.setBottomBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
				bType = Short.valueOf(String.valueOf(borderProp.get("borderType")));
				cStyle.setBorderBottom(bType);
			 }
		}
		//borderSide [top, bottom, left, right] : 순서는 바뀔 수 있음.
		else if( _item.get("borderSide") != null ){
			String _b = _item.get("borderSide").toString().replaceAll(" ", "");
//			//앞뒤 괄호를 제거하고, 쉼표로 구분하여 배열 생성.
			if(_b.equals("[]") || _b.equals("")){
				// side 없음!
			}
			else if( _b.charAt(0) == '[' && _b.charAt(_b.length()-1) == ']'){
//				String[] bStr = (_b.length()==2)? new String[1]: _b.substring(1, _b.length()-1).split(",");
				ArrayList<String> bStrList = new ArrayList<String>(Arrays.asList(jsonArrayToArray(_b)));
				
				if( bStrList.size() == 0 ){
					// border None!
				}
				else{
					// borderSide 배열을 기준으로 boderWidth, borderColor, borderType 지정.
					int size = bStrList.size();
					for (int i = 0; i < size; i++) {
						int bColor = 0;
						short bType = 0;
						
						// 속성이 여러 개 인지 한 개 인지에 따라 값 지정. 
						if( isCell ){
							// isCell = true : 속성값을 ,로 쪼개서, borderSide 인덱스와 일치하는 값을 대입.
							if( arProps.containsKey("borderColor") && isValid(arProps.get("borderColor")) ){
								String[] bColorStrAr = ValueConverter.getString(arProps.get("borderColor")).trim().split(",");
								if( bColorStrAr.length > i ) 
									bColor = ValueConverter.getInteger(bColorStrAr[i]);
							}
							if( arProps.containsKey("borderType") && isValid(arProps.get("borderType")) ){
								String[] bTypeStr = ValueConverter.getString(arProps.get("borderType")).trim().split(",");
								if( bTypeStr.length > i ) 
									bType = ValueConverter.getShort(bTypeStr[i]);
							}
						}
						else{
							// isCell = false : 값을 그대로 받아옴.
							if( arProps.containsKey("borderColor") && isValid(arProps.get("borderColor")) ) 
								bColor = ValueConverter.getInteger(arProps.get("borderColor"));
							
							if( arProps.containsKey("borderType") && isValid(arProps.get("borderType")) ) 
								bType = ValueConverter.getShort(arProps.get("borderType"));
						}
						
						// boderSide의 값에 따라 스타일 지정.
						if( bStrList.get(i).equals("left") ){
							cStyle.setLeftBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
							cStyle.setBorderLeft(bType);
						}
						else if( bStrList.get(i).equals("right") ){
							cStyle.setRightBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
							cStyle.setBorderRight(bType);
						}
						else if( bStrList.get(i).equals("top") ){
							cStyle.setTopBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
							cStyle.setBorderTop(bType);
						}
						else if( bStrList.get(i).equals("bottom") ){
							cStyle.setBottomBorderColor(new XSSFColor(changeColorToByteAr(bColor)));
							cStyle.setBorderBottom(bType);
						}
						
					}// End boderSide For
				}
			}
		}// end borderSide
		
		// Text 
		if( _item.containsKey("text") && isValid(_item.get("text"))){
			String itemTxt = URLDecoder.decode(ValueConverter.getString(_item.get("text")), "UTF-8");
			c.setCellValue(itemTxt);

			//textRotate
			if( _item.containsKey("textRotate") && isValid(_item.get("textRotate")) ){
				// -90 ~ 90
				short rotation = 0;
				switch (ValueConverter.getShort(_item.get("textRotate"))) {
				case 0: rotation = 0; break;
				case 45: rotation = 120; break;
				case 90: rotation = 180; break;
				case 180: rotation = 0; break; // 180 은 지원안됨.
				case 270: rotation = 90; break;
				}
				cStyle.setRotation(rotation);
			}
			
			/*
			// cell 줄바꿈 설정. -- 셀 너비보다 텍스트가 큰 경우도 줄바꿈이 되어야함. --> 무조건 true 로
			if( itemTxt.indexOf("\n") != -1 ) cStyle.setWrapText(true);
			else cStyle.setWrapText(false);
			*/
			cStyle.setWrapText(true);
			
			XSSFFont font = null;
			//textDecoration
			if( _item.containsKey("textDecoration") && isValid(_item.containsKey("textDecoration")) ){
				if( font == null ) font = (XSSFFont) wb.createFont();
				switch (ETextDecoration.valueOf(ValueConverter.getString(_item.get("textDecoration")))) {
				case none: font.setUnderline(Font.U_NONE); break;
				case normal: font.setUnderline(Font.U_NONE); break;
				case underline: font.setUnderline(Font.U_SINGLE); break;
				}
			}
			//fontStyle
			if( _item.containsKey("fontStyle") && isValid(_item.get("fontStyle")) ){
				if( font == null ) font = (XSSFFont) wb.createFont();
				switch (EFontStyle.valueOf(ValueConverter.getString(_item.get("fontStyle")))) {
				case italic: font.setItalic(true); break;
				case normal: font.setItalic(false); break;
				}
			}
			//fontSize
			if( _item.containsKey("fontSize") && isValid(_item.get("fontSize")) ){
				if( font == null ) font = (XSSFFont) wb.createFont();
//				font.setFontHeightInPoints( (short)toPoint(_item.get("fontSize")) );
				font.setFontHeight( toPoint(_item.get("fontSize")) );
			}
			//fontWeight
			if( _item.containsKey("fontWeight") && isValid(_item.get("fontWeight")) ){
				if( font == null ) font = (XSSFFont) wb.createFont();
				String fWeight = String.valueOf(_item.get("fontWeight"));
				switch (EFontWeight.valueOf(fWeight)) {
				case normal: font.setBold(false); break;
				case bold: font.setBold(true); break;
				}
			}
			//fontFamily
			if( _item.containsKey("fontFamily") && isValid(_item.get("fontFamily")) ){
				String fname = ValueConverter.getString(_item.get("fontFamily"));
				if( font == null ) font = (XSSFFont) wb.createFont();
				fname = URLDecoder.decode(fname, "UTF-8");
				font.setFontName( fname );
			}
			//fontColor
			if( _item.containsKey("fontColorInt") && isValid(_item.get("fontColorInt")) ){
				if( font == null ) font = (XSSFFont) wb.createFont();
				font.setColor(new XSSFColor(changeColorToByteAr(ValueConverter.getInteger(_item.get("fontColorInt")))));
			}
			
			if( font != null ){
				cStyle.setFont(font);
			}
			
		} // Text End

		// ------ 텍스트가 회전하면 정렬이 깨지는 현상 방지.
		//textAlign
		String tAlign = String.valueOf(_item.get("textAlign"));
		if( isValid(tAlign) ){
			switch ( ETextAlign.valueOf(tAlign)) {
			case left: cStyle.setAlignment(CellStyle.ALIGN_LEFT); break;
			case center: cStyle.setAlignment(CellStyle.ALIGN_CENTER); break;
			case right: cStyle.setAlignment(CellStyle.ALIGN_RIGHT); break;
			}
		}
		
		//verticalAlign
		String vAlign = String.valueOf(_item.get("verticalAlign"));
		if( isValid(vAlign) ){
			switch ( EVerticalAlign.valueOf(vAlign)) {
			case top: cStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP); break;
			case middle: cStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); break;
			case bottom: cStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM); break;
			}
		}
		// vAlign 속성이 빠져있어서 임시로 처리해놓음 추후 제거!
		else{
			cStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		}
		
		//width
		int cWidth = -1;
		//height
		float cHeight = -1;
		// merge 영역에 width, height 값을 가진 아이템이 있으면, 세팅한다.
		HashMap<String, Object> tmp = xySetArray.get(c.getRowIndex()-rowIdx).get(c.getColumnIndex());
		if( tmp != null ){
			cWidth = toNum(tmp.get("width"));
			cHeight = toNum(tmp.get("height"));
		}
		else{
			cWidth = toNum(_item.get("width"));
			cHeight = toNum(_item.get("height"));
		}
		
		
		if( cWidth != -1){
			cWidth = PixelUtil.pixel2WidthUnits(cWidth);
			sheet.setColumnWidth( c.getColumnIndex(), cWidth );
		}
		
		if( cHeight != -1){
			//The maximum row height for an individual row is 409 points. (1 point equals approximately 1/72 inch)
			cHeight = (float) Math.min(cHeight*0.75, 409);
			c.getRow().setHeightInPoints( cHeight );
		}
		
//		c.setCellStyle(cStyle);
		
		return c;
	}
	
	//-------------------------------  Export Basic End -------------------------------//
	
	
	
	//-------------------------------  Export Data Only  -------------------------------//

	// Data Only's Workbook Styles
	private XSSFCellStyle headerStyle;
	private XSSFCellStyle rowStyle;

	/**
	 * <pre>
	 * Export Data Only
	 * 
	 * <b>Data Set 구조</b>
	 * { dataset_name : { 
	 *                     "header" : [values], 
	 *                     "data":[ [value per row],[value],[value] ]
	 *                   }
	 * }
	 * 1. 데이터 셋 이름으로 Sheet 생성.
	 * 2. Row 생성.
	 * 3. get("header"):ArrayList&lt;String&gt;
	 *  headerStyle을 적용한 데이터 셀 생성.
	 * 4. get("data"):ArrayList&lt;:ArrayList&lt;String&gt;&gt; 
	 * rowStyle Row 별 ArrayList를 기반으로 셀 생성.
	 * </pre>
	 * 
	 * @param datasetMap 사용된 데이터셋의 컬럼,데이터 정보.
	 * @throws UnsupportedEncodingException URL Decode Exception
	 * */
	public Workbook xmlParsingExcel(HashMap<String, HashMap<String, ArrayList<Object>>> datasetMap) throws UnsupportedEncodingException {

		log.info(getClass().getName() + "::" + "Call xmlParsingDataExcel()...");
		
		if( datasetMap == null || datasetMap.size() == 0){
			log.error(getClass().getName() + "::" + "datasetMap parameter is null.");
			return null;
		}
		
		wb = new XSSFWorkbook();
		
		createHeaderStyle();
		createDataStyle();
		
		return toExcel(datasetMap);
	}
	
	
	/**
	 * <pre>
	 * Export Excel 페이지 별.
	 * 1. 파라미터로 넘겨받은 Workbook 사용.
	 * 2. sheet 도 Workbook 에서 받아와서 사용.
	 * 
	 * </pre>
	 * @param dataMap 아이템별 속성정보.
	 * @param _wb Workbook.
	 * @param _pageNo 페이지 번호.
	 * */
	public Workbook xmlParsingExcel(ArrayList<ArrayList<HashMap<String, Object>>> dataMap, Workbook _wb , int _pageNo, String _formName, boolean _isNewDoc, int _documentIdx ) throws Exception {

		if( dataMap == null || dataMap.size() == 0){
			log.error(getClass().getName() + "::" + "dataMap parameter is null.");
			return null;
		}

		itemPropList = dataMap;
		
		log.info(getClass().getName() + "::" + "Call partial excelSetting()...");
		
		isPartial = true;
		currentPageNo = _pageNo;

		
		/**if( _wb == null )
		{
			File _excelFile = new File(mExcelFileName);
			XSSFWorkbook xssfwb = null;
			if(_excelFile.isFile()) 
			{
				xssfwb = new XSSFWorkbook(_excelFile);
			}
			else
			{
				xssfwb = new XSSFWorkbook();
			}
			wb = new SXSSFWorkbook(xssfwb,100);
		}
		else
		{
			wb = _wb;
		}*/
		
		wb = ( _wb == null )? new SXSSFWorkbook(100) : _wb;
		ZipSecureFile.setMinInflateRatio(0.00000001); 

		// 50 페이지마다 새 시트 생성!
//		if( _pageNo % 50 == 0 ){
		if(_isNewDoc)
		{
			argoPageNo = _pageNo;
		}

		if( documentXArrayGlobal.size() > _documentIdx ) setXArray( documentXArrayGlobal.get(_documentIdx) );
		
		// --------- row 가 크면 새 시트 생성! 3000? 5000?
		if( wb.getNumberOfSheets() == 0 || mExcelSheetSplitType.equals(GlobalVariableData.UB_EXCEL_SHEET_SPLIT_PAGE) || _isNewDoc || ( _pageNo == START_PAGE && ( wb.getNumberOfSheets() == START_PAGE ) ) || ( maxSheetSize > -1 && rowIdx > maxSheetSize ) ){
			
			int _sheetIdx = wb.getNumberOfSheets()+1;
			
			if(sheet != null && sheet instanceof SXSSFSheet )
			{
				((SXSSFSheet) sheet).flushRows();
			}
			
			String _sheetNm =  "";
			
			if(mExcelSheetSplitType.equals(GlobalVariableData.UB_EXCEL_SHEET_SPLIT_PAGE))
			{
				int _idx = wb.getNumberOfSheets();
				_sheetNm = "";
				
				if( mExcelSheetNames != null && mExcelSheetNames.size() > _idx )
				{
					_sheetNm = mExcelSheetNames.get(_idx);
				}
				
				if( _sheetNm.equals("") )
				{
					_sheetNm =  "Sheet" + ( _idx + 1);
				}
				
			}
			else
			{
				if(mSheetName.equals(""))
				{
					_sheetNm =  _sheetIdx+ "_"+ _formName + (currentPageNo - argoPageNo)+"page";
				}
				else
				{
					_sheetNm = mSheetName;
				}
			}
			
			int _idx = wb.getSheetIndex(_sheetNm);
			int _addIndx = 0;
			String _sheetTempName = _sheetNm;
			while( _idx != -1 )
			{
				_addIndx = _addIndx + 1;
				_sheetTempName = _sheetNm + "(" + _addIndx + ")";
				_idx = wb.getSheetIndex(_sheetTempName);
				
				if( _idx == -1 ) _sheetNm = _sheetTempName;
			}			
			sheet = wb.createSheet(_sheetNm);
//			sheet = wb.createSheet( (currentPageNo - argoPageNo)+"page");
			rowIdx=0;
			if( _isNewDoc || _pageNo == 0 ){
				if( itemPropList.get(0).get(0).containsKey("waterMark") ){
					itemPropList.remove(0); // watermark 일단 제거. -- 엑셀에 워터마크 추가 시 사용.
				}
			}
		}
		else{
			sheet = wb.getSheetAt(wb.getNumberOfSheets()-1);
		}
		
		/*
		try {
			sheet = wb.getSheetAt(0);
		} catch (Exception e) {
			sheet = wb.createSheet();
		}
		 */
		sheet.setPrintGridlines(false); 
		sheet.setDisplayGridlines(false);
		
		if(sheet != null && sheet instanceof SXSSFSheet )
		{
			((SXSSFSheet) sheet).flushRows();
		}
		
		return super.toExcel();
	}
	
	private void drawBackgroundImage( int _rowAdx,  ArrayList<Integer> _xAr, ArrayList<Integer> _yAr )
	{
		
		if( this.mBackgroundImage != null && mExcelIncludeImage )
		{
			
			if(mBackgroundImage.containsKey("imageID"))
			{
				int imgId = -1;
				imgId = Integer.valueOf( mBackgroundImage.get("imageID").toString() );
				/* Create the drawing container */
				XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
		        /* Create an anchor point */
				CreationHelper helper = wb.getCreationHelper();
				ClientAnchor my_anchor = helper.createClientAnchor();
				
				int _rowSt = 0;
				int _rowEd = 0;
				int _colSt = 0;
				int _colEd = 0;
				
				int _argoH = Float.valueOf(mBackgroundImage.get("argoH").toString() ).intValue();
				int _x = Float.valueOf( mBackgroundImage.get("left").toString() ).intValue();
				int _y = Float.valueOf( mBackgroundImage.get("top").toString() ).intValue();
				int _w = Float.valueOf( mBackgroundImage.get("width").toString() ).intValue();
				int _h = Float.valueOf( mBackgroundImage.get("height").toString() ).intValue();
				
				int _yPosition = _yAr.indexOf(_y+_argoH);
				if( _yPosition > 0 ) _yPosition = _yPosition - 1;
				
				_colSt = _xAr.indexOf(_x);
				_colEd = _xAr.indexOf(_x+_w);
				_rowSt = _yPosition + _rowAdx;
				_rowEd = _yAr.indexOf(_y+_h+_argoH) + _rowAdx -1;
				
				CellRangeAddress region = new CellRangeAddress(_rowSt, _rowEd, _colSt, _colEd );
				my_anchor.setCol1(region.getFirstColumn());
				my_anchor.setRow1(region.getFirstRow());
				my_anchor.setCol2(region.getLastColumn());
				my_anchor.setRow2(region.getLastRow());
				
				// 아이템 사이즈 정보로 이미지 사이즈 지정. Dimension 이용. 이 경우 resize 메서드를 호출하면 안된다.
				String itemW = String.valueOf( mBackgroundImage.get("pageWidth") );
				String itemH = String.valueOf( mBackgroundImage.get("pageHeight") );
				
				int dx = 0;
				int dy = 0;
				itemW = String.valueOf(_x+_w);
				itemH = String.valueOf(_y + _h);	
				dx = _x;
				dy = _y;
				
				// 시작 = 0
				my_anchor.setDx1(dx * XSSFShape.EMU_PER_PIXEL);
				my_anchor.setDy1(dy * XSSFShape.EMU_PER_PIXEL);
				
				// pixel 값을 emu로 변환하기 위해 상수를 곱해준다.
				if( !itemW.equals("null") && !itemW.equals("")){
					my_anchor.setDx2(toNum(itemW) * XSSFShape.EMU_PER_PIXEL);
				}
				if( !itemH.equals("null") && !itemH.equals("")){
					my_anchor.setDy2(toNum(itemH) * XSSFShape.EMU_PER_PIXEL);
				}
				/* Invoke createPicture and pass the anchor point and ID */
				XSSFPicture my_picture = drawing.createPicture(my_anchor, imgId);
			}
			
		}
		
	}
	
	
	/*****************************************************
	* <pre>
	* Export Data Only
	* 
	* REF :
	* GS_UBIViewPagelib_ver2.0/src/ubstorm/view/pageprocess/ExportDocumentProcess.as > exportToDataSet()
	* </pre>
	* @param datasetMap
	* @return Workbook
	 * @throws UnsupportedEncodingException 
	* @throws Exception 
	*****************************************************/
	public Workbook toExcel(HashMap<String, HashMap<String, ArrayList<Object>>> datasetMap) throws UnsupportedEncodingException {

		log.info(getClass().getName() + "::" + "Start Parsing Data Excel...");
		
		if(wb.getNumCellStyles()<3){
			if( wb.getCellStyleAt((short)1) == null ){
				createHeaderStyle();
			}
			if( wb.getCellStyleAt((short)2) == null ){
				createDataStyle();
			}
		}
		
		// Dataset 마다 Sheet 생성.
		for( String dsKey : datasetMap.keySet() ){
			Sheet s = wb.createSheet(dsKey);
			s.setPrintGridlines(false); 
			s.setDisplayGridlines(false);
			
			int rowCnt = 0;
			HashMap<String, ArrayList<Object>> dataGroup = datasetMap.get(dsKey);

			// Header Setting.
			ArrayList<Object> headerAr = dataGroup.get("header");
			if( headerAr != null && headerAr.size()>0 ){
				Row row = s.createRow(rowCnt++);
				for (int i = 0; i < headerAr.size(); i++) {
					Cell c = row.createCell(i);
					c.setCellStyle(wb.getCellStyleAt((short) 1));
					s.setColumnWidth(i, PixelUtil.pixel2WidthUnits(130));
					String itemTxt = String.valueOf(headerAr.get(i));
					itemTxt = URLDecoder.decode(itemTxt, "UTF-8");
					c.setCellValue(itemTxt);
				}
			}
			// Data Value Setting.
			ArrayList<Object> dataAr = dataGroup.get("data");
			if( dataAr != null && dataAr.size()>0 ){
				for (int i = 0; i < dataAr.size(); i++) {
					Row row = s.createRow(rowCnt++);
					
					ArrayList<String> dataValueAr = (ArrayList<String>)dataAr.get(i);
					if( dataValueAr != null && dataValueAr.size()>0 ){
						for (int j=0; j<dataValueAr.size(); j++) {
							String val = dataValueAr.get(j);
							Cell c = row.createCell(j);
							c.setCellStyle(wb.getCellStyleAt((short) 2));
							c.setCellValue(val);
						}// end col for
					}
					
				}// end row for
			}// row setting end
			
		}
		
		log.info(getClass().getName() + "::" + "toExcel::" + "create Data Excel Complete!");
		
		return wb;
	}

	private void createHeaderStyle() {
		
		headerStyle = (XSSFCellStyle) wb.createCellStyle();
		headerStyle.setFillForegroundColor( IndexedColors.GREY_25_PERCENT.getIndex() );
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		
		headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
		
		headerStyle.setBorderTop(CellStyle.BORDER_THIN);
		headerStyle.setBorderRight(CellStyle.BORDER_THIN);
		headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
		headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
		
		headerStyle.setWrapText(true);
		headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		
		Font font = wb.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short)11);
		headerStyle.setFont(font);
		
	}
	private void createDataStyle() {

		rowStyle = (XSSFCellStyle) wb.createCellStyle();
		
		rowStyle.setBorderTop(CellStyle.BORDER_THIN);
		rowStyle.setBorderRight(CellStyle.BORDER_THIN);
		rowStyle.setBorderBottom(CellStyle.BORDER_THIN);
		rowStyle.setBorderLeft(CellStyle.BORDER_THIN);
		
		rowStyle.setWrapText(true);
		rowStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		
		Font font = wb.createFont();
		font.setFontHeightInPoints((short)9);
		rowStyle.setFont(font);
		
	}
	
	//-------------------------------Export Data Only End-------------------------------//
	
	
	private CTTransform2D createXfrm(XSSFClientAnchor anchor , Sheet _sheet, float _w, float _h, float _rotate ) {
		  CTTransform2D xfrm = CTTransform2D.Factory.newInstance();
		  CTPoint2D off = xfrm.addNewOff();
		  
		  xfrm.setRot( Float.valueOf(_rotate * 60000).intValue() );
		  
		  off.setX(anchor.getDx1());
		  off.setY(anchor.getDy1());
		 
		  long width = Units.pixelToEMU(Float.valueOf(_w).intValue());
		  long height = Units.pixelToEMU(Float.valueOf(_h).intValue());
		  CTPositiveSize2D ext = xfrm.addNewExt();
		  ext.setCx(anchor.getDx1() + anchor.getDx2());
		  ext.setCy(anchor.getDy1() + anchor.getDy2());
		  return xfrm;
	}
	
	
	
}
// class End
