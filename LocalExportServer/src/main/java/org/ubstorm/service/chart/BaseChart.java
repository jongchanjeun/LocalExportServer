package org.ubstorm.service.chart;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.oreilly.servlet.Base64Encoder;

public abstract class BaseChart {
	
	protected Logger log = Logger.getLogger(getClass());
	
	protected JFreeChart chart = null;
	protected Object dataset = null;

	protected int width = 100;
	protected int height = 100;
	
	
	/**
	 *  차트에 데이터 프로바이더를 설정 하는 함수
	 */
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
								String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
								String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor , float _rangeMax);

	
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor);

	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String [] _params);

	
	
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor, int angle , int rangeStart, int rangeEnd, String type);

	
	
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor,
			int _xLineWeight,int _yLineWeight,int _outLineWeight,int[] _valueWeight );
	
	
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor,
			int _xLineWeight,int _yLineWeight,int _outLineWeight,int[] _valueWeight ,
			float _seriesXFieldFontSize , float _seriesYFieldFontSize, boolean _rotateCategoryLabel);
	
	
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor , float _seriesXFieldFontSize , float _seriesYFieldFontSize,  boolean _rotateCategoryLabel);

	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor , float _seriesXFieldFontSize , float _seriesYFieldFontSize,  boolean _rotateCategoryLabel
			,int _xLineWeight,int _yLineWeight,int _outLineWeight , float _rangeMax);
	
	
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor, String _colorFieldName);
	
	public abstract boolean setGraphData(ArrayList<ArrayList<HashMap<String, Object>>> _dataAC, String _title,
								String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
								String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor, boolean isOverlaid);
	
	public abstract boolean setGraphData(ArrayList<ArrayList<HashMap<String, Object>>> _dataAC, String _title,
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor, boolean isOverlaid , float _rangeMax);
	
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
								String _seriesXField, String _seriesCloseField, String _seriesHighField, String _seriesLowField, String _seriesOpenField,  
								String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
								String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor);

	public abstract boolean setGraphData(ArrayList<ArrayList<HashMap<String, Object>>> _dataAC, String _title,
			String _seriesXField[], HashMap<Integer, String[]> _yFieldNames, HashMap<Integer, String[]> _yFieldDisplayNames, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, HashMap<Integer, int[]> _yFieldFillColors, boolean isOverlaid);
	
	public abstract boolean setGraphData(ArrayList<ArrayList<HashMap<String, Object>>> _dataAC, String _title,
			String _seriesXField[], HashMap<Integer, String[]> _yFieldNames, HashMap<Integer, String[]> _yFieldDisplayNames, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, HashMap<Integer, int[]> _yFieldFillColors, boolean isOverlaid , float _rangeMax);
	
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,String _title, 
			String _seriesXField, String[] _yFieldName,String[] _yFieldDisplayName, boolean _crossTab, 
			String _form,boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,int _gridLIneColor, 
			String _legendDirection,String _legendLabelPlacement, int _legendMarkHeight,int _legendMarkWeight, 
			String _legendLocation,String _dataLabelPosition, boolean _DubplicateAllow,int[] _yFieldFillColor, 
			int _xLineWeight, int _yLineWeight,int _outLineWeight, int[] _valueWeight,
			float _seriesXFieldFontSize, float _seriesYFieldFontSize,boolean _rotateCategoryLabel, 
			int _valueDisplayType,float _rangeMax, int _noDataType);
	
	
	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String _title, String _seriesXField, String _seriexCloseField,
			String _seriesHighField, String _seriesLowField,
			String _seriesOpenField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, int _candleWidth, int _candleUpColor,
			int _candleDownColor);

	public abstract boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String _title, String _seriesXField, String _seriexCloseField,
			String _seriesHighField, String _seriesLowField,
			String _seriesOpenField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, int _candleWidth, int _candleUpColor,
			int _candleDownColor , float _rangeMax);

	
	
	protected abstract void makeDefaultDataSet();
	protected abstract void makeChart(String _title); 
	
	/**
	 *  차트에  카테고리, Legend 등의 속성를 설정 하는 함수
	 */
	protected abstract boolean setGraphProperty(String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor);
	
	/*
	 * taxiMeter 속성 추가 
	 * */
	
	public void writeChartToStream(OutputStream out)
	{
		try {
			ChartUtilities.writeChartAsPNG(out, this.chart , this.width , this.height );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeChartToFile(File file)
	{
		try {
			ChartUtilities.saveChartAsPNG(file, this.chart , this.width , this.height );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getBase64String()
	{
		String result = null;
		BufferedImage bi = this.chart.createBufferedImage(this.width , this.height);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "png", os);
			result = Base64Encoder.encode(os.toByteArray());
			result = result.replaceAll("(\\r|\\n)", "");			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (IOException e) {}
		}
		
		return result;
	}
	
	
	
	public void writeSvgChartToFile(String fileName)
	{
		try {
			/* Our logical Pie chart is ready at this step. We can now write the chart as SVG using Batik */
            /* Get DOM Implementation */
            DOMImplementation mySVGDOM= GenericDOMImplementation.getDOMImplementation();
            
            /* create Document object */
            Document document = mySVGDOM.createDocument(null, "svg", null);
            /* Create SVG Generator */
            SVGGraphics2D my_svg_generator = new SVGGraphics2D(document);
            /* Render chart as SVG 2D Graphics object */
            this.chart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, this.width, this.height), null);
            /* Write output to file */
            //my_svg_generator.stream("output_pie_chart.svg");     
            my_svg_generator.stream(fileName);   
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeSvgChartToStream(OutputStream out)
	{
		try {
			/* Our logical Pie chart is ready at this step. We can now write the chart as SVG using Batik */
            /* Get DOM Implementation */
            DOMImplementation mySVGDOM= GenericDOMImplementation.getDOMImplementation();
            
            /* create Document object */
            Document document = mySVGDOM.createDocument(null, "svg", null);
            /* Create SVG Generator */
            SVGGraphics2D my_svg_generator = new SVGGraphics2D(document);
            /* Render chart as SVG 2D Graphics object */
            this.chart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, this.width, this.height), null);
            /* Write output to file */
            //my_svg_generator.stream("output_pie_chart.svg");         
            Writer outwt = new OutputStreamWriter(out, "UTF-8");
            my_svg_generator.stream(outwt, false);
            outwt.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public String getSvgData()
	{
		String result = null;
		
		StringWriter outwt = new StringWriter();
		
		try {
			/* Our logical Pie chart is ready at this step. We can now write the chart as SVG using Batik */
            /* Get DOM Implementation */
            DOMImplementation mySVGDOM= GenericDOMImplementation.getDOMImplementation();
            
            /* create Document object */
            Document document = mySVGDOM.createDocument(null, "svg", null);
            /* Create SVG Generator */
            SVGGraphics2D my_svg_generator = new SVGGraphics2D(document);
            /* Render chart as SVG 2D Graphics object */
            this.chart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, this.width, this.height), null);
            /* Write output to file */
            //my_svg_generator.stream("output_pie_chart.svg");         
            // Writer outwt = new OutputStreamWriter(out, "UTF-8");
            my_svg_generator.stream(outwt, false);
            result = outwt.toString();
			result = result.replaceAll("(\\r|\\n)", "");
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				outwt.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
		System.out.println(result);
		
		return result;
	}


	public boolean setGraphData(
			ArrayList<ArrayList<HashMap<String, Object>>> _dataACL,
			HashMap<Integer, String> displayNamesMap) {
		// TODO Auto-generated method stub
		return false;
	}







}
