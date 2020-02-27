package org.ubstorm.service.parser.svg;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class SvgSplit {

	/**
	 * svg를 지정된 높이에 맞게 잘라낸다.
	 * */
	public List splitSVG(String stSVG  , int _pageHeight, int _positionY , int _viewBoxWidth ) throws ParserConfigurationException
	{
		//변환 정보를 담는다.  y, height, svg
		List resultList=new ArrayList< HashMap<String, Object>>();
		
		// XPath 오류 발생 문자열 replace
		stSVG = stSVG.replace("&#160;", "_ubsp_");
		stSVG = stSVG.replace("$", "_ubsp2_");
		stSVG = stSVG.replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFF]+", "");	
		
		InputSource   is = new InputSource(new StringReader(stSVG)); 
		   
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(false);
		fac.setValidating(false);
		fac.setFeature("http://xml.org/sax/features/namespaces", false);
		fac.setFeature("http://xml.org/sax/features/validation", false);
		fac.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		fac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		
		Document document = null;

		try {
			document = fac.newDocumentBuilder().parse(is);
		} catch (SAXException e1) {
			// TODO Auto-generated catch blocks
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		XPath _xpath = XPathFactory.newInstance().newXPath();
		
		// 최초 svg가 그려질 영역을 계산한다.   페이지 높이 - 시작 좌표.
		int areaHeight=_pageHeight - _positionY;

		// 조회 범위.
		int _startY=0;
		int _endY=0;
		
		// 최소 조회 범위
		int MIN_Y=_pageHeight;
		
		// element 정보 변수.
		int[] areaValue=null;
		int[] testValue=null;
		
		int _elementY			=  0;
		int _elementHeight	=  0;
		int _baseLineOffset	=  0;
		
		NodeList list=null;
		NodeList list2=null;
		
		int saveY=0;
		int _areaHeight=0;
		
		try {
			
			int i=0;
			
			while(true)
			{
				// 첫번째 node를 구하기 위해서 최소 범위를 지정하여 목록을 조회한다. 
				list = (NodeList) _xpath.evaluate("//text[@y>"+_startY+" and @y<="+(_startY+MIN_Y)+"]", document.getDocumentElement(), XPathConstants.NODESET);
				
				// list가 더이상 없으면 while문을 빠져 나온다.
				if( list == null || list.getLength() == 0  ){
					break;
				}
				
				// 기준을 잡기 위해서 첫번째 node의 정보만 가져온다.
				areaValue=getElementAreaValue(list ,0);
				_baseLineOffset	=  areaValue[2];
				
				// 첫번째 node의 보정값을 영역 계산에 적용한다.
				areaHeight -=_baseLineOffset;
				
				_endY=_startY+areaHeight;
				
				// svg에서 y좌표 조건에 해당하는 node를 가져온다.
				list = (NodeList) _xpath.evaluate("//*[(@y)>="+(_startY)+" and (@y + @height)<="+(_endY)+"]", document.getDocumentElement(), XPathConstants.NODESET);
				
				// list가 더이상 없으면 while문을 빠져 나온다.
				if( list == null || list.getLength() == 0  ){
					
					// 첫회에서 시작범위가 너무 작으면 Element들이 들어갈 수 없으므로 페이지 높이 범위에 들어갈 수 있는 목록을 다시 계산한다.
					if(i == 0){
						list = (NodeList) _xpath.evaluate("//*[(@y)>="+(_startY)+" and (@y + @height)<="+(_pageHeight)+"]", document.getDocumentElement(), XPathConstants.NODESET);
						_endY = _pageHeight;
					}else{
						break;
					}
				}
				list2 = (NodeList) _xpath.evaluate("//text[(@y)<"+(_endY)+" and (@y + @height)>"+(_endY)+"]", document.getDocumentElement(), XPathConstants.NODESET);
				NodeList list3 = (NodeList) _xpath.evaluate("//image[(@y)<"+(_endY)+" and (@y + @height)>"+(_endY)+"]", document.getDocumentElement(), XPathConstants.NODESET);

				
				// list의 마지막 node의  y좌표 + 높이가 height를 넘어가면 안된다.
				areaValue=getLastTextElement(list  );
				_elementY				=  areaValue[0];
				_elementHeight	=  areaValue[1];
				//_baseLineOffset	=  areaValue[2];	// ?? 여기 있는게 맞을까?
				
				int _rangeArea=_elementY + _elementHeight;

				
				// 표현 영역의 높이.
				_areaHeight=(_rangeArea-_startY);

				//
				int _minusY=saveY -_baseLineOffset;
				int _testsy = saveY;
				
				if( list2 == null || list2.getLength() == 0  ){
					_startY= _endY;
				}else{
					
					if( list3 == null || list3.getLength() == 0  ){
						testValue=null;
					}else{
						testValue = getElementAreaValue(list3 ,0);
					}
					
					areaValue=getElementAreaValue(list2 ,0);
					
					// list의 마지막 node의  y좌표 + 높이가 height를 넘어가면 안된다.
					_elementY				=  areaValue[0];
					_elementHeight	=  areaValue[1];
					_baseLineOffset	=  areaValue[2];
					
					// image tag y 계산이 추가됨.  더 작은 값을 변수에 넣는다.
					if( testValue != null &&  ( testValue[0] < _elementY ) ){
						_startY = testValue[0];
					}else{
						// 마지막 node의 다음 node부터 조회할 수 있도록 시작 위치를 변경해 준다.
						_startY = _elementY;
					}
				}
				
				// Rect start
				NodeList rectList=null;
				rectList = (NodeList) _xpath.evaluate("//rect[(@y)<="+(_startY)+" and (@y + @height)>="+(_startY)+"]", document.getDocumentElement(), XPathConstants.NODESET);
				
				Object[] rectVals = changeRectElement(rectList,_startY , _baseLineOffset);
				Node[] result2=(Node[])rectVals[0];
				// Rect end
				
				
				//  ** Path 구하고, 자르기. start
				// start y 값을 기준으로  path를 잘라 넣어야 함.
				NodeList pathList=null;
				pathList = (NodeList) _xpath.evaluate("//path[(@y)<="+(_startY)+" and (@y + @height)>="+(_startY)+"]", document.getDocumentElement(), XPathConstants.NODESET);
				
				// path tag를 두동강내야 한다.
				//_startY- Element Y	 이거는 앞에
				// (Element Y+ Height)- _startY 이거는 뒤에 붙여야 한다.
				
				Object[] rVals = changePathElement(pathList,_startY);
				Node[] result1=(Node[])rVals[0];
				//  ** Path 구하고, 자르기. end
				
				
				StringBuffer resultSvg;
				//test start
				if( pathList.getLength() > 0  ){

					// list를 기준으로 svg를 뽑아낸다.
					resultSvg=createSVG(list, _minusY , _startY-saveY,_viewBoxWidth,result1,result2,_testsy,_baseLineOffset);

				}else{
					// list를 기준으로 svg를 뽑아낸다.
					resultSvg=createSVG(list, _minusY , _areaHeight,_viewBoxWidth,result1,result2,_testsy,_baseLineOffset);
				}
				//test end
				
				saveY=_startY;
				
				// 반환 정보를 map에 담는다.
				HashMap<String, Object> infoObject= new HashMap<String, Object>();
				infoObject.put("svg", resultSvg);
				infoObject.put("height",_areaHeight);
				//resultMap.put(i, infoObject);
				resultList.add(infoObject);
				
				//다음 표현 범위를 설정해줘야 한다.
				areaHeight=_pageHeight;
				
				i++;
			}
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultList;
	}
	
	
	
	/**
	 *  element의 y값과 height값을 반환한다.
	 * */
	private int[] getElementAreaValue(NodeList list , int idx)
	{
		int[] areaValues=new int[3];
		
		Node node=list.item(idx);
		Element element = (Element)node;
		String _yStr=element.getAttribute("y");
		String _heightStr=element.getAttribute("height");
		
		if( element.getNodeName() == "rect" && _yStr.equalsIgnoreCase("0") ){
			node=list.item(idx+1);
			element = (Element)node;
			_yStr=element.getAttribute("y");
			_heightStr=element.getAttribute("height");
		}
		
		
		String _baselineOffsetStr=element.getAttribute("baselineOffset");
		
		int _elementY =  Integer.parseInt( _yStr );
		int _elementHeight =  Integer.parseInt( _heightStr );
		int _baselineOffset =  0;
		
		if( _baselineOffsetStr.length() > 0 ){
			_baselineOffset=Integer.parseInt( _baselineOffsetStr );
		}
		
		areaValues[0]=_elementY;
		areaValues[1]=_elementHeight;
		areaValues[2]=_baselineOffset;
		
		return areaValues;
	}
	

	private int[] getLastTextElement(NodeList list )
	{
		int[] areaValues=new int[3];
		
		int _maxY=0;
		int _y=0;
		
		Element lastElement = null;
		
		Node node=null;
		Element element = null;
		String _yStr=null;
		String _heightStr=null;
		String _baselineOffsetStr=null;
		
		for ( int i=0; i<list.getLength(); i++ ){
			node=list.item(i);
			
			element = (Element)node;
			_yStr=element.getAttribute("y");
			_y = Integer.parseInt( _yStr );
			
			if( _y > _maxY ){
				_maxY = _y;
				lastElement = element;
			}
			
			
			// text의 마지막 Element base line을 가져오면 된다. - 추후 개선이 필요할 수 있다.
			// 반복문 수행 중, text는 base line 값을 기록해둔다.  - 마지막 element가 꼭 text라는 보장이 없다.
			if( node.getNodeName().equals("text")  ){
				_baselineOffsetStr=element.getAttribute("baselineOffset");
			}
			
		}

		_heightStr=lastElement.getAttribute("height");
		

		
		int _elementHeight =  Integer.parseInt( _heightStr );
		int _baselineOffset =  0;
		
		
		if(  _baselineOffsetStr != null &&  _baselineOffsetStr.length() > 0 ){
			_baselineOffset=Integer.parseInt( _baselineOffsetStr );
		}
		
		areaValues[0]=_maxY;
		areaValues[1]=_elementHeight;
		areaValues[2]=_baselineOffset;
		
		return areaValues;
	}

	/**
	 * page별 svg를 생성한다.
	 * */
	private StringBuffer createSVG(NodeList list , int _minusY,int _contentsY, int _viewBoxWidth , Node[] listGraphic, Node[] listRect , int _saveSY , int _baseLineOffset)
	{
		if( _minusY  < 0 ){
			_minusY=0;
		}
		
		
	   	 StringBuffer stbPageSvg = new StringBuffer();
		 
	   	 stbPageSvg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"  width=\""+_viewBoxWidth+"\" height=\""+_contentsY+"\" viewBox=\"0 0 "+_viewBoxWidth+" "+_contentsY+"\" zoomAndPan=\"disable\">");
		    
	   	 

	   	 //rect start
	   	 if( listRect != null ){
		   	 for (int i = 0; i < listRect.length; i++) {
				 
		   		 Node node = listRect[i];
			     
		   		 String strNodeY = node.getAttributes().getNamedItem("y").getTextContent();
			     
		   		 int setYValue=(Integer.valueOf(strNodeY)-(_minusY ) );
		   		 
		   		 //node.getAttributes().getNamedItem("y").setTextContent("" + (Integer.valueOf(strNodeY)-(_saveSY) ));
		   		 //node.getAttributes().getNamedItem("y").setTextContent("" + (Integer.valueOf(strNodeY)-(_minusY ) ));
		   		 node.getAttributes().getNamedItem("y").setTextContent("" + setYValue );
			     
		   		 if( _baseLineOffset == setYValue ){
		   			 System.out.println("kind of value");
		   			 node.getAttributes().getNamedItem("y").setTextContent("" +0 );
		   		 }
		   		 
		   		 
		   		 StringWriter buf = new StringWriter();
			     
		   		 Transformer xform = null;
		   		try {
					xform = TransformerFactory.newInstance().newTransformer();
				} catch (TransformerConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (TransformerFactoryConfigurationError e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		   		 
		   		 xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // optional
			     
		   		 xform.setOutputProperty(OutputKeys.INDENT, "yes"); // optional
			     
		   		 xform.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "text"); // optional
			     
		   		 try {
					xform.transform(new DOMSource(node), new StreamResult(buf));
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			     
		   		 String stTmp = buf.toString();
			     
		   		 stTmp = stTmp.replace("_ubsp_", " ");
		   		 stTmp = stTmp.replace("_ubsp2_", "$");
			     
		   		 stbPageSvg.append(stTmp);
		   	 }
	   	 }
	   	 
	   	 //rect end
	   	 
	   	 
	   	 if( listGraphic != null ){
		   	 for (int i = 0; i < listGraphic.length; i++) {
				 
		   		 Node node = listGraphic[i];
			     
		   		 String strNodeY = node.getAttributes().getNamedItem("y").getTextContent();
			     
		   		 node.getAttributes().getNamedItem("y").setTextContent("" + (Integer.valueOf(strNodeY)-(_saveSY)));
		   		 //node.getAttributes().getNamedItem("y").setTextContent("" + (Integer.valueOf(strNodeY)-(_minusY)));
			     
		   		 

		   		 
		   		if( node.getNodeName().equals("path") ){
		   			
		   			String strNodeD = node.getAttributes().getNamedItem("d").getTextContent();
		   			
		   			String[] strNodeDList = strNodeD.split(" ");
		   			String mData  = strNodeDList[1];
		   			String lData  = strNodeDList[3];
		   			
		   			String[] mNodeDList = mData.split(",");
		   			String[] lNodeDList = lData.split(",");
		   			
		   			String pathY1 = mNodeDList[1];
		   			String pathY2 = lNodeDList[1];
		   			
		   			int pathY1Int = Integer.valueOf(pathY1);
		   			int pathY2Int = Integer.valueOf(pathY2);
		   			
		   			int changePathY1=pathY1Int-_saveSY;
		   			int changePathY2=pathY2Int-_saveSY;
//		   			int changePathY1=pathY1Int-_minusY;
//		   			int changePathY2=pathY2Int-_minusY;
		   			
		   			String changePathData="M "+mNodeDList[0]+","+changePathY1+" L "+lNodeDList[0]+","+changePathY2;
		   			
		   			node.getAttributes().getNamedItem("d").setTextContent( changePathData );
		   			
		   			
		   		}
		   		 
		   		 
		   		 StringWriter buf = new StringWriter();
			     
		   		 Transformer xform = null;
		   		try {
					xform = TransformerFactory.newInstance().newTransformer();
				} catch (TransformerConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (TransformerFactoryConfigurationError e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		   		 
		   		 xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // optional
			     
		   		 xform.setOutputProperty(OutputKeys.INDENT, "yes"); // optional
			     
		   		 xform.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "text"); // optional
			     
		   		 try {
					xform.transform(new DOMSource(node), new StreamResult(buf));
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			     
		   		 String stTmp = buf.toString();
			     
		   		 stTmp = stTmp.replace("_ubsp_", " ");
		   		 stTmp = stTmp.replace("_ubsp2_", "$");
			     
		   		 stbPageSvg.append(stTmp);
		   	 }
	   	 }
	   	 
	   	 
	   	 
	   	 
	   	 
	   	 
	   	 
	   	 
	   	 for (int i = 0; i < list.getLength(); i++) {
		 
	   		 Node node = list.item(i);
		     
	   		 String strNodeY = node.getAttributes().getNamedItem("y").getTextContent();
		     
	   		if( node.getNodeName().equals("path") ){
	   			
	   			//int pathY=(Integer.valueOf(strNodeY)-_saveSY);
	   			int pathY=(Integer.valueOf(strNodeY)-_minusY);
	   			
	   			node.getAttributes().getNamedItem("y").setTextContent("" + pathY);
	   			
	   			//<path d="M 352,12 L 352,34" height="22" style="fill:none;stroke:#000000;stroke-width:1" width="0" x="352" y="0"/>
	   			
	   			//  d  property 수정. start
	   			String strNodeD = node.getAttributes().getNamedItem("d").getTextContent();
	   			
	   			String[] strNodeDList = strNodeD.split(" ");
	   			String mData  = strNodeDList[1];
	   			String lData  = strNodeDList[3];
	   			
	   			String[] mNodeDList = mData.split(",");
	   			String[] lNodeDList = lData.split(",");
	   			
	   			String pathY1 = mNodeDList[1];
	   			String pathY2 = lNodeDList[1];
	   			
	   			int pathY1Int = Integer.valueOf(pathY1);
	   			int pathY2Int = Integer.valueOf(pathY2);
	   			
//	   			int changePathY1=pathY1Int-_saveSY;
//	   			int changePathY2=pathY2Int-_saveSY;
	   			int changePathY1=pathY1Int-_minusY;
	   			int changePathY2=pathY2Int-_minusY;
	   			
	   			
			   	 if( _baseLineOffset == changePathY1 ){
		   			changePathY1 -= _baseLineOffset;
		   			//changePathY2 -= _baseLineOffset;
		   		 }
	   			
	   			String changePathData="M "+mNodeDList[0]+","+changePathY1+" L "+lNodeDList[0]+","+changePathY2;
	   			
	   			node.getAttributes().getNamedItem("d").setTextContent( changePathData );
	   		//  d  property 수정. end
	   			
	   			
	   		}else if( node.getNodeName().equals("rect") ){
	   			//node.getAttributes().getNamedItem("y").setTextContent("" + (Integer.valueOf(strNodeY)-_saveSY));
	   			node.getAttributes().getNamedItem("y").setTextContent("" + (Integer.valueOf(strNodeY)-_minusY));
	   			
	   			int setYValue=(Integer.valueOf(strNodeY)-_minusY);
	   			
	   			//
		   		 if( _baseLineOffset == setYValue ){
		   			 node.getAttributes().getNamedItem("y").setTextContent("" +0 );
		   		 }
	   			//
	   			
	   			
	   			
	   		}else{
	   			node.getAttributes().getNamedItem("y").setTextContent("" + (Integer.valueOf(strNodeY)-_minusY));	
	   		}
	   		 
	   		 
	   		if( node.getNodeName().equals("image") ){
	   			
	   			String aaa= node.toString();
	   			//stbPageSvg.append(aaa);
	   			
	   			Element testRoot = (Element)node;
	   			testRoot.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
	   			//continue;
	   		}
	   		 StringWriter buf = new StringWriter();
		     
	   		 Transformer xform = null;
	   		try {
				xform = TransformerFactory.newInstance().newTransformer();
			} catch (TransformerConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformerFactoryConfigurationError e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	   		 
	   		 xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // optional
		     
	   		 xform.setOutputProperty(OutputKeys.INDENT, "yes"); // optional
		     
	   		 xform.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "text"); // optional
		     
	   		 try {
				xform.transform(new DOMSource(node), new StreamResult(buf));
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     
	   		 String stTmp = buf.toString();
		     
	   		 stTmp = stTmp.replace("_ubsp_", " ");
	   		 stTmp = stTmp.replace("_ubsp2_", "$");
		     
	   		 stbPageSvg.append(stTmp);
	   	 }
	    
	   	 stbPageSvg.append("</svg>");
	   	 
	   	 return stbPageSvg;
	}
	
	private Object[] changePathElement( NodeList list , int splitY )
	{
		Node[] result1= new Node[list.getLength()];
		Node[] result2= new Node[list.getLength()];
		
		Object[] rVals = new Object[2];

		
	   	 for (int i = 0; i < list.getLength(); i++) {
			 
	   		 Node node = list.item(i);
		     
	   		 String strNodeY = node.getAttributes().getNamedItem("y").getTextContent();
	   		 String strNodeH = node.getAttributes().getNamedItem("height").getTextContent();
		     
	   		 int strNodeYInt=Integer.parseInt(strNodeY);
	   		 int strNodeHInt=Integer.parseInt(strNodeH);
	   		 
	   		int h1=splitY - strNodeYInt;
	   		int h2=(strNodeYInt+strNodeHInt)-splitY;
	   		 

	   		 //<path d="M 8,742 L 8,792" height="50" style="fill:none;stroke:#000000;stroke-width:1" width="0" x="8" y="742"/>
	   		//<path d="M 8,742 L 8,792" height="50" style="fill:none;stroke:#000000;stroke-width:1" width="0" x="8" y="742"/>
	   		//<path d="M 8,742 L 8,792" height="50" style="fill:none;stroke:#000000;stroke-width:1" width="0" x="8" y="742"/>
	   		 
	   		String strNodeD = node.getAttributes().getNamedItem("d").getTextContent();
	   		
   			String[] strNodeDList = strNodeD.split(" ");
   			String mData  = strNodeDList[1];
   			String lData  = strNodeDList[3];
   			
   			String[] mNodeDList = mData.split(",");
   			String[] lNodeDList = lData.split(",");
   			
   			String pathY1 = mNodeDList[1];
   			String pathY2 = lNodeDList[1];
   			
   			int pathY1Int = Integer.valueOf(pathY1);
   			//int pathY2Int = Integer.valueOf(pathY2);
	   		
   			
   			Node cloneNode = node.cloneNode(true);
   			
   			String changePathData="M "+mNodeDList[0]+","+mNodeDList[1]+" L "+lNodeDList[0]+","+(pathY1Int + h1);
   			cloneNode.getAttributes().getNamedItem("d").setTextContent( changePathData );
   			cloneNode.getAttributes().getNamedItem("height").setTextContent( h1+"" );
   			//cloneNode.getAttributes().getNamedItem("y").setTextContent( (pathY1Int + h1)+"" );
	   		
   			node.getParentNode().appendChild(cloneNode);
   			
   			result1[i]=cloneNode;
	   		
   			changePathData="M "+mNodeDList[0]+","+splitY+" L "+lNodeDList[0]+","+(splitY + h2);
   			node.getAttributes().getNamedItem("d").setTextContent( changePathData );
   			node.getAttributes().getNamedItem("height").setTextContent( h2+"" );
   			//node.getAttributes().getNamedItem("y").setTextContent( (splitY + h2)+"" );
   			node.getAttributes().getNamedItem("y").setTextContent( (splitY )+"" );
	   		
   			result2[i]=node;
		     
	   	 }
		
	   	 rVals[0] = result1;//int라 가정
	   	 rVals[1] = result2;//int라 가정
	   	 
	   	 return rVals;
	}
	
	
	
	
	private Object[] changeRectElement( NodeList list , int splitY  , int _baseLineOffset)
	{
		Node[] result1= new Node[list.getLength()];
		Node[] result2= new Node[list.getLength()];
		
		Object[] rVals = new Object[2];

		
	   	 for (int i = 0; i < list.getLength(); i++) {
			 
	   		 Node node = list.item(i);
		     
	   		 String strNodeY = node.getAttributes().getNamedItem("y").getTextContent();
	   		 String strNodeH = node.getAttributes().getNamedItem("height").getTextContent();
		     
	   		 int strNodeYInt=Integer.parseInt(strNodeY);
	   		 int strNodeHInt=Integer.parseInt(strNodeH);
	   		 
	   		int h1=splitY - strNodeYInt;
	   		int h2=(strNodeYInt+strNodeHInt)-splitY;
	   		 

	   		// <rect x="8" y="362" width="227" height="51" style="stroke:none;fill-opacity:1;fill:#9900ff" />
	   		
   			
   			Node cloneNode = node.cloneNode(true);
   			
   			cloneNode.getAttributes().getNamedItem("height").setTextContent( h1+"" );
   			//cloneNode.getAttributes().getNamedItem("y").setTextContent( (strNodeYInt + h1)+"" );
	   		
   			node.getParentNode().appendChild(cloneNode);
   			
   			result1[i]=cloneNode;
	   		
   			//node.getAttributes().getNamedItem("height").setTextContent( h2+"" );
   			node.getAttributes().getNamedItem("height").setTextContent( (h2+_baseLineOffset)+"" );
   			node.getAttributes().getNamedItem("y").setTextContent( (splitY)+"" );
	   		
   			result2[i]=node;
		     
	   	 }
		
	   	 rVals[0] = result1;//int라 가정
	   	 rVals[1] = result2;//int라 가정
	   	 
	   	 return rVals;
	}
	
	/**
	 * page별 svg를 생성한다.
	 * */
	private StringBuffer createSvgGraphic(NodeList list , int _minusY,int _contentsY, int _viewBoxWidth)
	{
		if( _minusY  < 0 ){
			_minusY=0;
		}
		
		
	   	 StringBuffer stbPageSvg = new StringBuffer();
		 
	   	 stbPageSvg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"  width=\""+_viewBoxWidth+"\" height=\""+_contentsY+"\" viewBox=\"0 0 "+_viewBoxWidth+" "+_contentsY+"\" zoomAndPan=\"disable\">");
		    
	   	 for (int i = 0; i < list.getLength(); i++) {
		 
	   		 Node node = list.item(i);
		     
	   		 String strNodeY = node.getAttributes().getNamedItem("y").getTextContent();
		     
	   		 node.getAttributes().getNamedItem("y").setTextContent("" + (Integer.valueOf(strNodeY)-_minusY));
		     
	   		 StringWriter buf = new StringWriter();
		     
	   		 Transformer xform = null;
	   		try {
				xform = TransformerFactory.newInstance().newTransformer();
			} catch (TransformerConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformerFactoryConfigurationError e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	   		 
	   		 xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // optional
		     
	   		 xform.setOutputProperty(OutputKeys.INDENT, "yes"); // optional
		     
	   		 xform.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "text"); // optional
		     
	   		 try {
				xform.transform(new DOMSource(node), new StreamResult(buf));
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     
	   		 String stTmp = buf.toString();
		     
	   		 stTmp = stTmp.replace("_ubsp_", " ");
	   		 stTmp = stTmp.replace("_ubsp2_", "$");
		     
	   		 stbPageSvg.append(stTmp);
	   	 }
	    
	   	 stbPageSvg.append("</svg>");
	   	 
	   	 return stbPageSvg;
	}
	
	
}

