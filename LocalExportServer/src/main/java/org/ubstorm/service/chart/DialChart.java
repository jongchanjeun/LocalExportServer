package org.ubstorm.service.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.Align;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.ubstorm.service.utils.ValueConverter;

public class DialChart extends BaseChart  {

	private String model_type = "";
	
	public DialChart(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	

	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String[] _params) {
		
		int angle = 270;
		int rangeStart = 0;
		int rangeEnd = 100;
		String type = "normal";
		
		int _yBackgroundColor = 0xffffff;

		
		String _datasetID	= "".equals(_params[0]) ? null : _params[0]; 
		String _columnName	= "".equals(_params[1]) ? null : _params[1];
		String _interval	= "".equals(_params[2]) ? null : _params[2];
		
		rangeStart	= Integer.parseInt(_params[3]);
		rangeEnd 	= Integer.parseInt(_params[4]);
		angle 		= Integer.parseInt(_params[5]);
		type 		= _params[6];
		
		String _dataIdxStr		=  _params[7];
		
		if( _params.length == 9 ){
			_yBackgroundColor	= "".equals(_params[8]) ? 0xffffff : Integer.decode(_params[8]);
		}
		
		

		int _dataIdx		= Integer.parseInt(_dataIdxStr);

		if( _dataAC != null && _dataAC.size() > _dataIdx ){
			
			dataset = new DefaultValueDataset( );
			
			HashMap<String, Object> _dat=null;
			Object dataValue=null;
			Number nb = null;
			
			_dat = _dataAC.get(_dataIdx);	
			dataValue = _dat.get(_columnName);
			
			nb = ValueConverter.getFloat(dataValue);
			
			
			if( nb != null ){
				((DefaultValueDataset) dataset).setValue( nb );
			}
		}
		
		this.makeChart(null);
		
		//chart.setBackgroundPaint(new Color(_yBackgroundColor));
		
		this.setGraphProperty( angle,  rangeStart ,  rangeEnd ,  type);
		
		return true;
	}

	
	
	/**
	 *  차트에  카테고리, Legend 등의 속성를 설정 하는 함수
	*/
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title, 
			String _parameterStr, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor, int angle, int rangeStart , int rangeEnd , String type )
       {	
		// dataset id
		//_seriesXField
		
		// column name
		//_legendLocation
		
		//interval
		//_dataLabelPosition
		
		String [] _params	= _parameterStr.split(",");
		String _datasetID	= "".equals(_params[0]) ? null : _params[0]; 
		String _columnName	= "".equals(_params[1]) ? null : _params[1];
		String _interval	= "".equals(_params[2]) ? null : _params[2];
		String _dataIdxStr	= "".equals(_params[3]) ? null : _params[7];

		int _dataIdx		= Integer.parseInt(_dataIdxStr);

		if( _dataAC != null && _dataAC.size() > _dataIdx ){
			
			dataset = new DefaultValueDataset( );
			
			HashMap<String, Object> _dat=null;
			Object dataValue=null;
			Number nb = null;
			
			_dat = _dataAC.get(_dataIdx);	
			dataValue = _dat.get(_columnName);
			
			nb = ValueConverter.getFloat(dataValue);
			
			
			if( nb != null ){
				((DefaultValueDataset) dataset).setValue( nb );
			}
		}
		
		this.makeChart(_title);
		
		this.setGraphProperty( angle,  rangeStart ,  rangeEnd ,  type);
		
		return true;
	}
	
	
	
	
	protected void makeChart(String _title) 
	{
//		if(dataset == null)
//			makeDefaultDataSet();

		 MeterPlot meterplot = new MeterPlot((ValueDataset) dataset);
		 
		 chart = new JFreeChart(_title, JFreeChart.DEFAULT_TITLE_FONT, meterplot, true);
	}
	
	
	
	protected void makeDefaultDataSet()
	{
		dataset = new DefaultValueDataset( );
		((DefaultValueDataset) dataset).setValue(20);
		((DefaultValueDataset) dataset).setValue(30);
		((DefaultValueDataset) dataset).setValue(40);
	}
	
	
	protected boolean setGraphProperty(int angle, int rangeStart , int rangeEnd , String type ) {
		
		MeterPlot meterplot = (MeterPlot)chart.getPlot();
		chart.setBackgroundPaint(new Color(255, 255, 255, 0));
		
		// 반원 모양의 각도를 설정. 270까지 사용할 수 있는것으로 판단됨.
        meterplot.setMeterAngle(angle);
		
		// range 0~100을 설정한다.
        meterplot.setRange(new Range(rangeStart, rangeEnd));
        
        // 반원의 외곽 배경색
        meterplot.setDialBackgroundPaint(new Color(66, 134, 244, 0));

        // 반원모양을 설정.
        meterplot.setDialShape(DialShape.CHORD);
        
        // 범례 제거.
        chart.removeLegend();
        
        if(type.equals("tick")){
        	
        	meterplot.setDialBackgroundPaint(new Color(0, 0, 0, 0));
            meterplot.setDialOutlinePaint(new Color(0, 0, 0, 0));
            
            // 수치를 가르키는 침 색상.
            meterplot.setNeedlePaint(Color.black);   
            
            // 눈금에 설정되는 MeterInterval 폰트값 설정으로 보인다.
            meterplot.setTickPaint(new Color(0, 0, 0, 0));            
            
            // 눈금 표현 단위를 설정하는 것으로 보인다. 기본값이 10d 인듯 하다.
            meterplot.setTickLabelsVisible(false);            
            
            // 차트 중앙에 표현되는 data 값.
            meterplot.setValuePaint(new Color(255, 255, 255, 0));            
            meterplot.setUnits("");
            meterplot.setForegroundAlpha(0.7f);
            
            // 외곽선
            meterplot.setDialOutlinePaint(new Color(255, 255, 255, 0));              
                
        }else{
        	 MeterInterval meterinterval1 = new MeterInterval("", new Range(0.0D, 50D),new Color(255, 255, 255, 0), new BasicStroke(2.0F), new Color(0, 255, 0, 0));
             MeterInterval meterinterval2 = new MeterInterval("", new Range(50D, 70D),new Color(255, 255, 255, 0), new BasicStroke(2.0F), new Color(255, 255, 0, 0));
             MeterInterval meterinterval3 = new MeterInterval("", new Range(70D, 100D),new Color(255, 255, 255, 0), new BasicStroke(2.0F), new Color(255, 0, 0, 0));
             
             meterplot.addInterval(meterinterval1);
             meterplot.addInterval(meterinterval2);
             meterplot.addInterval(meterinterval3);            
            
             
             // 수치를 가르키는 침 색상.
             meterplot.setNeedlePaint(Color.black);            
            
             
             // 눈금에 설정되는 MeterInterval 폰트값 설정으로 보인다.
             meterplot.setTickLabelFont(new Font("Arial", 1, 14));
             meterplot.setTickLabelPaint(Color.black);
             
             
             // 눈금 표현 단위를 설정하는 것으로 보인다. 기본값이 10d 인듯 하다.
             meterplot.setTickSize(10D);
             meterplot.setTickPaint(Color.gray);
             
             
             // 차트 중앙에 표현되는 data 값.
             meterplot.setValuePaint(Color.black);
             meterplot.setValueFont(new Font("Arial", 1, 18));
             meterplot.setUnits("");
             
             
             
//             float lineWidth = 1.2f;
//             float dash[] = {5.0f};
//             float dot[] = {lineWidth};
//             meterplot.setOutlinePaint(new Color(255, 0, 0, 255));
//             meterplot.setOutlineStroke(new BasicStroke(5.0f));
//             meterplot.setOutlineVisible(true);

             // 외곽선
             meterplot.setDialOutlinePaint(new Color(255, 255, 255, 255));            
             
        }   
		return true;
	}
	
	
	
	

	
	
	/**
	 *  차트에 데이터 프로바이더를 설정 하는 함수
	 */
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
								String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
								String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor , String _colorFieldName)
	{
		

		return false;
	}
	
	

	/**
	 *  차트에  카테고리, Legend 등의 속성를 설정 하는 함수
	 */
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title, 
			String _parameterStr, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor)
	{
				
		return false;
	}
	
	
	
	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String _title, String _seriesXField, String _seriesCloseField,
			String _seriesHighField, String _seriesLowField,
			String _seriesOpenField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean setGraphData(ArrayList<ArrayList<HashMap<String, Object>>> _dataAC, String _title,
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor, boolean isOverlaid) 
	{
		return false;
	}


	@Override
	public boolean setGraphData(
			ArrayList<ArrayList<HashMap<String, Object>>> _dataAC,
			String _title, String[] _seriesXField,
			HashMap<Integer, String[]> _yFieldNames,
			HashMap<Integer, String[]> _yFieldDisplayNames, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight,
			String _gridLIneDirection, int _gridLIneColor,
			String _legendDirection, String _legendLabelPlacement,
			int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition,
			boolean _DubplicateAllow,
			HashMap<Integer, int[]> _yFieldFillColors, boolean isOverlaid) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String _title, String _seriesXField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, float seriesXFieldFontSize,
			float seriesYFieldFontSize, boolean _rotateCategoryLabel) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	protected boolean setGraphProperty(String _form, boolean _gridLIne,
			int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,
			String _legendDirection, String _legendLabelPlacement,
			int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition,
			boolean _DubplicateAllow, int[] _yFieldFillColor) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String _title, String _seriesXField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, int _xLineWeight, int _yLineWeight,
			int _outLineWeight, int[] _valueWeight) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String _title, String _seriesXField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, int _xLineWeight, int _yLineWeight,
			int _outLineWeight, int[] _valueWeight,
			float _seriesXFieldFontSize, float _seriesYFieldFontSize,
			boolean _rotateCategoryLabel) {
		// TODO Auto-generated method stub
		return false;
	}






	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String _title, String _seriesXField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, int _xLineWeight, int _yLineWeight,
			int _outLineWeight, int[] _valueWeight,
			float _seriesXFieldFontSize, float _seriesYFieldFontSize,
			boolean _rotateCategoryLabel, int _valueDisplayType,
			float _rangeMax, int _noDataType) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
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
			int _candleDownColor) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String _title, String _seriesXField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, float _rangeMax) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
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
			int _candleDownColor, float _rangeMax) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public boolean setGraphData(
			ArrayList<ArrayList<HashMap<String, Object>>> _dataAC,
			String _title, String _seriesXField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, boolean isOverlaid, float _rangeMax) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public boolean setGraphData(
			ArrayList<ArrayList<HashMap<String, Object>>> _dataAC,
			String _title, String[] _seriesXField,
			HashMap<Integer, String[]> _yFieldNames,
			HashMap<Integer, String[]> _yFieldDisplayNames, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight,
			String _gridLIneDirection, int _gridLIneColor,
			String _legendDirection, String _legendLabelPlacement,
			int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition,
			boolean _DubplicateAllow,
			HashMap<Integer, int[]> _yFieldFillColors, boolean isOverlaid,
			float _rangeMax) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String _title, String _seriesXField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, float _seriesXFieldFontSize,
			float _seriesYFieldFontSize, boolean _rotateCategoryLabel,
			int _xLineWeight, int _yLineWeight, int _outLineWeight,
			float _rangeMax) {
		// TODO Auto-generated method stub
		return false;
	}
}
