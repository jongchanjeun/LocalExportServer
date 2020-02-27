package org.ubstorm.service.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.ubstorm.service.utils.ValueConverter;

public class ubFormToHtml {

	private Logger log = Logger.getLogger(getClass());
	
	
	public String xmlParsingHTML(ArrayList<ArrayList<HashMap<String, Object>>> itemPropList) {

		String _result="<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-spacing:0px;width:100%;border-collapse:collapse;\"><tbody>";	
		
		String _tabletag="";
		String _imageTag="";
		
		log.error(getClass().getName() + "::xmlParsingHTML Start...........");
		
		for (ArrayList<HashMap<String, Object>> pageAr : itemPropList) {
			Date begin = new Date();
			_result += "<tr><td>";
			for (HashMap<String, Object> _item : pageAr) {
				// page 정보를 담고있는 아이템인가?
				Object pH = null;
				Object pW = null;
				if ( ((pH=_item.get("cPHeight")) != null) && ((pW=_item.get("cPWidth")) != null) ) {
					//_result+="<div align=\"center\" style=\"background-Color:#" + ChangeColorToMSA(Integer.parseInt(_item.get("backgroundColor").toString()))+ ";\">";
				}
				else{
					// 클래스명이 없다면 패스
					if( _item.get("className") == null ) continue;

					// visible false인 아이템은 패스!
					if( _item.get("visible") != null && String.valueOf(_item.get("visible")).equals("false") ) continue;

					String itemClass = String.valueOf(_item.get("className"));

					try {

						// 아이템이 라벨 종류인 경우
						if( itemClass.contains("Label") || itemClass.contains("Text") || itemClass.equals("UBApproval") ){
							String labeltag="";
						}
//						
//						
//						else if( itemClass.equals("UBImage")){
//							_imageTag = getImageTag( _item );
//							
//							_result += _imageTag;
//						}
						
						else if( itemClass.equals("UBTable") ){						
							
							_tabletag+="<div  align=\"center\" >";
							_tabletag+="<table  cellpadding=\"0\" cellspacing=\"0\"  align=\"center\" style=\"table-layout: fixed; border-spacing:0px;border-collapse:collapse; width:" +_item.get("width") + "px;background-color:#ffffff;\">";
							
							ArrayList<ArrayList<HashMap<String, Object>>>  rowAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("rows");							
							//colgroup 생성
							String _colGroupTag ="<colgroup>"; 
							ArrayList<Float> widthInfo = (ArrayList<Float>)_item.get("widthInfo");	
							for(int i = 0; i<widthInfo.size(); i++){
								_colGroupTag += "<col style=\"width: " +widthInfo.get(i) + "px;\" />";
							}
							_colGroupTag += "</colgroup>";
							_tabletag += _colGroupTag;
							
							int rowSize = rowAr.size();
							int colCnt = 0;
							
							if( _item.containsKey("columnCount") ){
								colCnt = toNum(_item.get("columnCount"));
							}
							
							for (int rIdx = 0; rIdx < rowSize; rIdx++) {//row count 
								
								_tabletag += "<tr>";
								
								ArrayList<HashMap<String, Object>> colAr = rowAr.get(rIdx);
								
								
								int _colCnt = colAr.size();	// 현재 colAr의 인덱스
								
								for (int colIdx = 0; colIdx < _colCnt; colIdx++) {//column count
																		
									HashMap<String, Object> cellItem = colAr.get(colIdx);
									
									//_tabletag += "<td>";		
									String _cellTag = getCellTag(cellItem);
									
									//_tabletag += "</td>";
									_tabletag +=_cellTag;	
								}
								
								_tabletag += "</tr>";
								
							}
							
							_tabletag += "</table></div>";
							
						}
						// 나머지 타입은 이미지로 처리.
						else{
							String _src = String.valueOf(_item.get("src"));
							if( !_src.equals("null") && !_src.equals("") ){
								String _imagetag="";
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
			_result +=_tabletag+ "</td></tr>";
		}
		//_result += _tabletag;
		//_result += _tabletag;
		_result+="<tbody></table>";
		return _result;
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

		return _color;
	}
	
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
	
	
	private String getImageTag( HashMap<String, Object> _item )
	{
		String _result = "";
		
		long _width=20;
		long _height=20;
		
		Boolean isHttp=true;
		String _src = String.valueOf(_item.get("src"));
	
		
		try {
			_src = URLDecoder.decode(_src,"UTF-8");
			String [] arrSrc = _src.split("IMG_URL=");
			if(arrSrc.length>1){
				_src = arrSrc[1];
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if( !_src.equals("null") && !_src.equals("") ){
			
			_result = "<p style=\"margin-top:" + _item.get("topPadding").toString() + "px;margin-left:" + _item.get("leftPadding").toString() + "px;\">";
			
			if( _src.startsWith("http") ){
				isHttp=true;
			}else{
				isHttp=false;
			}
			
			//
			if( _item.containsKey("height") ){
				_height = (long) (toNum(_item.get("height")));
			}
			//
			if( _item.containsKey("width") ){
				_width = (long) (toNum(_item.get("width")));
			}
			
			
			String _hyperLinkUrl = "";
			Boolean _hasHyperLink=false;
			
			if( _item.get("ubHyperLinkUrl") != null ){
				_hyperLinkUrl = _item.get("ubHyperLinkUrl").toString();
			}
			
			if( !_hyperLinkUrl.endsWith("null") && _hyperLinkUrl.length() > 0 ){
				
				_hyperLinkUrl = _item.get("ubHyperLinkUrl").toString();
				_hasHyperLink = true;
				
				_result+="<a href=\""+ _hyperLinkUrl +"\" style=\"outline : none;\" target=\"_blank\">";
			}
			
			
			if( isHttp ){
				//_result+="<img src=\""+_src+"\" style=\"border:0px;width:"+_width+"px; height:" +_height+"px;\" />";
				_result+="<img src=\""+_src+"\" width=\""+_width+"\" height=\""+_height+"\" style=\"border:0px;width:100%;max-width:"+_width+"px;\" />";
			}else{
				_result+="<img src=\"data:image/png;base64,"+_src+"\" width=\""+_width+"px\" height=\""+_height+"px\"  style=\"border:0px;\"/>";	
			}
			
			if( _hasHyperLink ){
				_result+="</a>";
			}
			
			
			_result+="</p>";
//			 <p style="margin-top:0;margin-bottom:0;line-height:1;font-size:6.75pt;">
//		        <img src="data:image/png;base64," width="374" height="238"/>
//		    </p>
			
			// <img src="data:image/png;base64," width="374" height="238"/>
		}
		
		
		return _result;
	}
	
	
	
	private String getCellTag( HashMap<String, Object> cellItem )
	{
		String _result = "<td ";
		
		if(!cellItem.get("rowSpan").equals(null)){
			_result = _result + "rowSpan='" + cellItem.get("rowSpan").toString() + "' ";			
		}
		if(!cellItem.get("colSpan").equals(null)){
			_result = _result + "colSpan='" + cellItem.get("colSpan").toString() + "' ";					
		}
		
		String itemTxt="";
		String _fontWeight="";
		String _fontStyle="";
		long fontSize=10;
		String _fontFamily="";
		String _textDecoration="";
		String _fontColor="";
		String _backgroundColor="";
		long _width=20;
		long _height=20;
		float lineHeight = 120;
		
		String textAlign="left";
		String verticalAlign="top";
		
		
		if( cellItem.containsKey("text") ){
			
			itemTxt = String.valueOf(cellItem.get("text"));
			//itemTxt = URLDecoder.decode(itemTxt, "UTF-8");
			// 줄바꿈 처리
			itemTxt = itemTxt.replace("\n", "<br>").replaceAll("\r", "<br>").replace("\\n", "<br>").replaceAll("\\r", "<br>");
		}
		
		//fontWeight
		if( cellItem.containsKey("fontWeight") ){
			_fontWeight = String.valueOf(cellItem.get("fontWeight"));									    
		}
		//fontStyle
		if( cellItem.containsKey("fontStyle") ){
			 _fontStyle = String.valueOf(cellItem.get("fontStyle"));
		}
		//fontSize
		if( cellItem.containsKey("fontSize") ){
			fontSize = (long) (toNum(cellItem.get("fontSize")));
//			fontSize = (long) (toNum(cellItem.get("fontSize"))*1.5);
		}
		//fontFamily
		if( cellItem.containsKey("fontFamily") ){
			try {
				_fontFamily = getFontName(String.valueOf(cellItem.get("fontFamily")));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//textDecoration
		if( cellItem.containsKey("textDecoration") ){
			_textDecoration = String.valueOf(cellItem.get("textDecoration"));
		}
		//fontColor
		if( cellItem.containsKey("fontColor") ){
			_fontColor = String.valueOf(cellItem.get("fontColor"));
		}
		
		if( cellItem.containsKey("backgroundColor") ){
//			_backgroundColor = String.valueOf(cellItem.get("backgroundColor")).replace("#", "");
			_backgroundColor = String.valueOf(cellItem.get("backgroundColor"));
		}
		
		//
		if( cellItem.containsKey("height") ){
			_height = (long) (toNum(cellItem.get("height")));
		}
		//
		if( cellItem.containsKey("width") ){
			_width = (long) (toNum(cellItem.get("width")));
		}
		
		
		if( cellItem.containsKey("textAlign") ){
			textAlign = String.valueOf(cellItem.get("textAlign"));
		}
		if( cellItem.containsKey("verticalAlign") ){
			verticalAlign = String.valueOf(cellItem.get("verticalAlign"));
		}

		String _hyperLinkText = "";
		String _hyperLinkUrl = "";
		Boolean _hasHyperLink=false;
		
		if( cellItem.get("ubHyperLinkUrl") != null ){
			_hyperLinkUrl = cellItem.get("ubHyperLinkUrl").toString();
		}	
		if( !_hyperLinkUrl.endsWith("null") && _hyperLinkUrl.length() > 0 ){
			
			try {
				_hyperLinkUrl = URLDecoder.decode(_hyperLinkUrl, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			_hasHyperLink = true;
		}
		if( cellItem.get("ubHyperLinkText") != null ){
			_hyperLinkText = cellItem.get("ubHyperLinkText").toString();
		}	
		if( !_hyperLinkText.endsWith("null") && _hyperLinkText.length() > 0 ){
			
			try {
				_hyperLinkText = URLDecoder.decode(_hyperLinkText, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}			
		}
		
		
		if( cellItem.containsKey("lineHeight") ){
			lineHeight = Float.valueOf(cellItem.get("lineHeight").toString()) * 100;//percent
		}
		
		//borderTypes
		String bdTypes = String.valueOf(cellItem.get("borderTypes"));
		String[] bdTypeAr = bdTypes.split(",");
		
		if( !bdTypes.equals("null") && !bdTypes.equals("") ){
			bdTypes = bdTypes.replace("[", "");
			bdTypes = bdTypes.replace("]", "");
			bdTypes = bdTypes.replaceAll(" ", "");
			bdTypeAr = bdTypes.split(",");
		}
		
		String bdType = "solid";
		
		
		String[] bdColorAr=null;
		String[] bdWidthAr=null;
		
		if( !bdType.equals("none") ){
			String bdColors = String.valueOf(cellItem.get("borderColors"));
			if( !bdColors.equals("null") && !bdColors.equals("")  ){
				bdColors = bdColors.replace("[", "");
				bdColors = bdColors.replace("]", "");
				bdColors = bdColors.replaceAll(" ", "");
				bdColorAr = bdColors.split(",");
			}
			//borderWidths
			String bdWidths = String.valueOf(cellItem.get("borderWidths"));
			if( !bdWidths.equals("null") && !bdWidths.equals("") ){
				bdWidths = bdWidths.replace("[", "");
				bdWidths = bdWidths.replace("]", "");
				bdWidths = bdWidths.replaceAll(" ", "");
				bdWidthAr = bdWidths.split(",");
			}
		}
		
		
		String bdSide = String.valueOf(cellItem.get("borderSide"));
		
		bdSide = bdSide.replace("[", "");
		bdSide = bdSide.replace("]", "");
		bdSide = bdSide.replaceAll(" ", "");
		
		ArrayList<String> bdSideAr = new ArrayList<String>(Arrays.asList(bdSide.split(",")));
		
		String _bdTopStyle="";
		String _bdLeftStyle="";
		String _bdBottomStyle="";
		String _bdRightStyle="";

		if( bdColorAr != null && bdTypeAr != null ){
			for (int i = 0; i < bdSideAr.size(); i++) {
				String _side = bdSideAr.get(i);				
				String _bdtype = bdTypeAr[i];
				int bdWidth = Integer.parseInt(bdWidthAr[i]);
				String _bdcolor = bdColorAr[i];
				
				if( "top".equals(_side) && !"none".equals(_bdtype)){
					_bdTopStyle="border-top:"+_bdcolor+" "+bdWidth+"px "+_bdtype+";";
				}
				else if( "left".equals(_side) && !"none".equals(_bdtype) ){
					_bdLeftStyle="border-left:"+_bdcolor+" "+bdWidth+"px "+_bdtype+";";
				}
				else if( "bottom".equals(_side) && !"none".equals(_bdtype) ){
					_bdBottomStyle="border-bottom:"+_bdcolor+" "+bdWidth+"px "+_bdtype+";";
				}
				else if( "right".equals(_side) && !"none".equals(_bdtype) ){
					_bdRightStyle="border-right:"+_bdcolor+" "+bdWidth+"px "+_bdtype+";";
				}
			}			
			
			//_result += " width=\""+_width+"px\" height=\""+_height+"px\"  ";	style속성으로 변경		
			
			_result += " valign=\""+verticalAlign+"\" ";
			
			_result += " bgcolor=\""+_backgroundColor+"\" ";
			//기본으로 1이 들어가서 0으로 세팅
			_result += "style=\" width:"+_width+"px; height:"+_height+"px; word-wrap:break-word;word-break:break-all;padding:0px;"+ _bdTopStyle  + _bdLeftStyle+_bdBottomStyle + _bdRightStyle + ";vertical-align:baseline;line-height:" + lineHeight + "%;\">";
			
			
		}
		else{
			log.error(getClass().getName()+"::>>>>>>>>>> border value is wrong : \nbdColorAr" +bdColorAr+", bdTypeAr : "+bdTypeAr);
		}
		
		//style="border-top:#000000 1px solid;border-left:#000000 1px solid;border-bottom:#000000 1px solid;border-right:#000000 1px solid;
		
		if(!itemTxt.equals(null) && !itemTxt.equals("")){		
			String _pTag="<p align=\""+ textAlign +"\" style=\"padding:" + cellItem.get("padding").toString() + "px;margin-top:0;margin-bottom:0;\">";

			String _spanTag = "<span style=\"font-family:";
			_spanTag += _fontFamily + ";";
			_spanTag += " font-size:"+fontSize + "px;";			
			_spanTag += " color:"+_fontColor + ";";
			if(!_textDecoration.equals(null) && !_textDecoration.equals("")){
				_spanTag += " text-decoration:"+_textDecoration + ";";
			}
			if(!_fontWeight.equals(null) && !_fontWeight.equals("")){
				_spanTag += " font-weight:"+_fontWeight + ";";
			}
			if(!_fontStyle.equals(null) && !_fontStyle.equals("")){
				_spanTag += " font-style:"+_fontStyle + ";";
			}
			_spanTag += "\">";
			
			String _spanTag2 = "<span style=\"color:" +_fontColor + ";\">";
			_spanTag2 += itemTxt;
			_spanTag2 += "</>";			
			
			String _fontTag="<font style=\"font-family:";
			_fontTag += _fontFamily + ";";
			_fontTag += " font-size:"+fontSize + "px;";
			_fontTag += " color:"+_fontColor + ";";
			if(!_textDecoration.equals(null) && !_textDecoration.equals("")){
				_fontTag += " text-decoration:"+_textDecoration + ";";
			}
			if(!_fontWeight.equals(null) && !_fontWeight.equals("")){
				_fontTag += " font-weight:"+_fontWeight + ";";
			}
			if(!_fontStyle.equals(null) && !_fontStyle.equals("")){
				_fontTag += " font-style:"+_fontStyle + ";";
			}
			
			_fontTag += "\">";
			_fontTag += itemTxt;
			
			_fontTag += "</font>";
			
			String _aTag = _spanTag2;
			
			if( _hasHyperLink ){
				
				if(_hyperLinkText != null && _hyperLinkText.length()>0){
					String [] arrTemp = _fontTag.split(_hyperLinkText);
					if(arrTemp.length>1){
						_aTag = arrTemp[0] + "<a style=\"text-decoration: none; \"  href=\""+_hyperLinkUrl+"\" target=\"_blank\">"  + _hyperLinkText + "</a>" + arrTemp[1];
					}else{
						_aTag = "<a style=\"text-decoration: none; \"  href=\""+_hyperLinkUrl+"\" target=\"_blank\">"  + _spanTag2 + "</a>";
					}
				}else{
					_aTag = "<a style=\"text-decoration: none; \"  href=\""+_hyperLinkUrl+"\" target=\"_blank\">"  + _spanTag2 + "</a>";
				}
			}
	    	
			//_result += _pTag + _fontTag + "</p>";
			_result += _pTag + _spanTag + _aTag + "</p>";
		
		}
		
		HashMap<String, Object> inclusiveItem;
		
		if(cellItem.containsKey("inclusiveItem") && !cellItem.get("inclusiveItem").equals(null)){
			inclusiveItem = (HashMap<String, Object>)cellItem.get("inclusiveItem");
			if( String.valueOf(inclusiveItem.get("className")).equals("UBImage") ){
				String _imageTag = getImageTag( inclusiveItem );
				_result += _imageTag;
			}
			
			if( String.valueOf(inclusiveItem.get("className")).equals("UBLabelBorder") ){
				String _labelTag = getLabelTag( inclusiveItem );	
				_result += _labelTag;
			}
		}
		
		_result += "</td>";
		
		return _result;
	}
	
	private String getLabelTag( HashMap<String, Object> cellItem )
	{
		String _result = "";	
		_result+="<table cellpadding=\"0\" cellspacing=\"0\" style=\"border-spacing:0px; width:" +cellItem.get("width") + "px; margin-top:" + cellItem.get("topPadding").toString() + "px;margin-left:" + cellItem.get("leftPadding").toString() + "px;\">";
		_result+="<tr>";
		_result+="<td ";	
		
		String itemTxt="";
		String _fontWeight="";
		String _fontStyle="";
		long fontSize=10;
		String _fontFamily="";
		String _textDecoration="";
		String _fontColor="";
		String _backgroundColor="";
		long _width=20;
		long _height=20;
		
		
		String textAlign="left";
		String verticalAlign="top";
		float lineHeight = 120;
		
		if( cellItem.containsKey("text") ){
			
			itemTxt = String.valueOf(cellItem.get("text"));
			//itemTxt = URLDecoder.decode(itemTxt, "UTF-8");
			// 줄바꿈 처리
			itemTxt = itemTxt.replace("\n", "<br>").replaceAll("\r", "<br>").replace("\n", "<br>").replaceAll("\r", "<br>");
		}
		
		//fontWeight
		if( cellItem.containsKey("fontWeight") ){
			_fontWeight = String.valueOf(cellItem.get("fontWeight"));									    
		}
		//fontStyle
		if( cellItem.containsKey("fontStyle") ){
			 _fontStyle = String.valueOf(cellItem.get("fontStyle"));
		}
		//fontSize
		if( cellItem.containsKey("fontSize") ){
			fontSize = (long) (toNum(cellItem.get("fontSize")));
//			fontSize = (long) (toNum(cellItem.get("fontSize"))*1.5);
		}
		//fontFamily
		if( cellItem.containsKey("fontFamily") ){
			try {
				_fontFamily = getFontName(String.valueOf(cellItem.get("fontFamily")));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//textDecoration
		if( cellItem.containsKey("textDecoration") ){
			_textDecoration = String.valueOf(cellItem.get("textDecoration"));
		}
		//fontColor
		if( cellItem.containsKey("fontColor") ){
			_fontColor = String.valueOf(cellItem.get("fontColor"));
		}
		
		if( cellItem.containsKey("backgroundColor") ){
//			_backgroundColor = String.valueOf(cellItem.get("backgroundColor")).replace("#", "");
			_backgroundColor = String.valueOf(cellItem.get("backgroundColor"));
		}
		
		//
		if( cellItem.containsKey("height") ){
			_height = (long) (toNum(cellItem.get("height")));
		}
		//
		if( cellItem.containsKey("width") ){
			_width = (long) (toNum(cellItem.get("width")));
		}
		
		
		if( cellItem.containsKey("textAlign") ){
			textAlign = String.valueOf(cellItem.get("textAlign"));
		}
		if( cellItem.containsKey("verticalAlign") ){
			verticalAlign = String.valueOf(cellItem.get("verticalAlign"));
		}
		
		
		if( cellItem.containsKey("lineHeight") ){
			lineHeight = Float.valueOf(cellItem.get("lineHeight").toString()) * 100;//percent
		}
		

		String _hyperLinkText = "";
		String _hyperLinkUrl = "";
		Boolean _hasHyperLink=false;
		
		if( cellItem.get("ubHyperLinkUrl") != null ){
			_hyperLinkUrl = cellItem.get("ubHyperLinkUrl").toString();
		}	
		if( !_hyperLinkUrl.endsWith("null") && _hyperLinkUrl.length() > 0 ){
			
			try {
				_hyperLinkUrl = URLDecoder.decode(_hyperLinkUrl, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			_hasHyperLink = true;
		}
		if( cellItem.get("ubHyperLinkText") != null ){
			_hyperLinkText = cellItem.get("ubHyperLinkText").toString();
		}	
		if( !_hyperLinkText.endsWith("null") && _hyperLinkText.length() > 0 ){
			
			try {
				_hyperLinkText = URLDecoder.decode(_hyperLinkText, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}			
		}
		
		
		//borderTypes
		String bdTypes = String.valueOf(cellItem.get("borderTypes"));
		String[] bdTypeAr = bdTypes.split(",");
		
		if( !bdTypes.equals("null") && !bdTypes.equals("") ){
			bdTypes = bdTypes.replace("[", "");
			bdTypes = bdTypes.replace("]", "");
			bdTypes = bdTypes.replaceAll(" ", "");
			bdTypeAr = bdTypes.split(",");
		}
		
		String bdType = "solid";
		
		
		String[] bdColorAr=null;
		String[] bdWidthAr=null;
		
		if( !bdType.equals("none") ){
			String bdColors = String.valueOf(cellItem.get("borderColors"));
			if( !bdColors.equals("null") && !bdColors.equals("")  ){
				bdColors = bdColors.replace("[", "");
				bdColors = bdColors.replace("]", "");
				bdColors = bdColors.replaceAll(" ", "");
				bdColorAr = bdColors.split(",");
			}
			//borderWidths
			String bdWidths = String.valueOf(cellItem.get("borderWidths"));
			if( !bdWidths.equals("null") && !bdWidths.equals("") ){
				bdWidths = bdWidths.replace("[", "");
				bdWidths = bdWidths.replace("]", "");
				bdWidths = bdWidths.replaceAll(" ", "");
				bdWidthAr = bdWidths.split(",");
			}
		}
		
		
		String bdSide = String.valueOf(cellItem.get("borderSide"));
		
		bdSide = bdSide.replace("[", "");
		bdSide = bdSide.replace("]", "");
		bdSide = bdSide.replaceAll(" ", "");
		
		ArrayList<String> bdSideAr = new ArrayList<String>(Arrays.asList(bdSide.split(",")));
		
		String _bdTopStyle="";
		String _bdLeftStyle="";
		String _bdBottomStyle="";
		String _bdRightStyle="";

		if( bdColorAr != null && bdTypeAr != null ){
			for (int i = 0; i < bdSideAr.size(); i++) {
				String _side = bdSideAr.get(i);				
				String _bdtype = bdTypeAr[i];
				int bdWidth = Integer.parseInt(bdWidthAr[i]);
				String _bdcolor = bdColorAr[i];
				
				if( "top".equals(_side) && !"none".equals(_bdtype)){
					_bdTopStyle="border-top:"+_bdcolor+" "+bdWidth+"px "+_bdtype+";";
				}
				else if( "left".equals(_side) && !"none".equals(_bdtype) ){
					_bdLeftStyle="border-left:"+_bdcolor+" "+bdWidth+"px "+_bdtype+";";
				}
				else if( "bottom".equals(_side) && !"none".equals(_bdtype) ){
					_bdBottomStyle="border-bottom:"+_bdcolor+" "+bdWidth+"px "+_bdtype+";";
				}
				else if( "right".equals(_side) && !"none".equals(_bdtype) ){
					_bdRightStyle="border-right:"+_bdcolor+" "+bdWidth+"px "+_bdtype+";";
				}
			}			
			
			//_result += " width=\""+_width+"px\" height=\""+_height+"px\"  ";//style속성으로 변경	
			
			_result += "valign=\""+verticalAlign+"\" ";
			
			_result += "bgcolor=\""+_backgroundColor+"\" ";
			//기본으로 1이 들어가서 0으로 세팅
			_result += "style=\" width:"+_width+"px; height:"+_height+"px; word-wrap:break-word;word-break:break-all;padding:0px;"+ _bdTopStyle  + _bdLeftStyle+_bdBottomStyle + _bdRightStyle + "\">";
			
			
		}
		else{
			log.error(getClass().getName()+"::>>>>>>>>>> border value is wrong : \nbdColorAr" +bdColorAr+", bdTypeAr : "+bdTypeAr);
		}
		
		//style="border-top:#000000 1px solid;border-left:#000000 1px solid;border-bottom:#000000 1px solid;border-right:#000000 1px solid;
		
		if(!itemTxt.equals(null) && !itemTxt.equals("")){		
			String _pTag="<p align=\""+ textAlign +"\" style=\"padding:" + cellItem.get("padding").toString() + "px;margin-top:0;margin-bottom:0;line-height:" + lineHeight + "%;\">";

			String _fontTag="<font style=\"font-family:";
			_fontTag += _fontFamily + ";";
			_fontTag += " font-size:"+fontSize + "px;";
			_fontTag += " color:"+_fontColor + ";";
			if(!_textDecoration.equals(null) && !_textDecoration.equals("")){
				_fontTag += " text-decoration:"+_textDecoration + ";";
			}
			if(!_fontWeight.equals(null) && !_fontWeight.equals("")){
				_fontTag += " font-weight:"+_fontWeight + ";";
			}
			if(!_fontStyle.equals(null) && !_fontStyle.equals("")){
				_fontTag += " font-style:"+_fontStyle + ";";
			}
			
			_fontTag += "\">";
			_fontTag += itemTxt;
			
			_fontTag += "</font>";
			
			if( _hasHyperLink ){
				
				if(_hyperLinkText != null && _hyperLinkText.length()>0){
					String [] arrTemp = _fontTag.split(_hyperLinkText);
					if(arrTemp.length>1){
						_fontTag = arrTemp[0] + "<a  href=\""+_hyperLinkUrl+"\" target=\"_blank\">"  + _hyperLinkText + "</a>" + arrTemp[1];
					}else{
						_fontTag = "<a  href=\""+_hyperLinkUrl+"\" target=\"_blank\">"  + _fontTag + "</a>";
					}
				}else{
					_fontTag = "<a  href=\""+_hyperLinkUrl+"\" target=\"_blank\">"  + _fontTag + "</a>";
				}
			}
	    	
			_result += _pTag + _fontTag + "</p>";		
		}
		
		_result += "</td>";
		_result += "</tr>";
		_result += "</table>";
		return _result;
	}
	
}
