package org.ubstorm.service.chart;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashMap;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

public class RadarChart extends BaseChart {
	
	public RadarChart(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	/**
	 *  차트에 데이터 프로바이더를 설정 하는 함수
	 */
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String [] _params)
	{
		//validation check
		if( _dataAC == null) return false;

		
		String _title;
		String _valueField;
		String _legendField;
		String _categoryField;
		String _interiorStr;
		int _interiorGap;

		boolean titleVisible=true;
		boolean legendVisible=true;
		
		int[] seriesColor=null;
		
		int _yBackgroundColor = 0xffffff;
		
		if( _params.length > 5 ){

			_title			= _params[0];
			_valueField		= _params[1];
			_legendField	= _params[2];
			_categoryField	= _params[3];
			_interiorStr	= _params[4];

			String _titleVisibleStr = _params[5];
			String _legendVisibleStr = _params[6];
			
			titleVisible	=  _titleVisibleStr.equals("true") ? true : false;
			
			legendVisible	= _legendVisibleStr.equals("true") ? true : false;

			
			if( _params.length > 7 ){
				if(!"".equals(_params[7]))
				{			
					String[] _staYFC = _params[7].split("~");
					seriesColor = new int[_staYFC.length];
		            for(int i=0; i< _staYFC.length; i++)
		            {
		            	seriesColor[i] = Integer.decode(_staYFC[i]);
		            }
				}
			}
			
			if( _params.length > 8 ){
				_yBackgroundColor	= "".equals(_params[8]) ? 0xffffff : Integer.decode(_params[8]);
			}
			
			
		}else{

			_title			= _params[0];
			_valueField		= _params[1];
			_legendField	= _params[2];
			_categoryField	= _params[3];
			_interiorStr	= _params[4];

		}
		_interiorGap		= Integer.parseInt(_interiorStr);
		
		
		
		String _legendDirection = legendVisible ? "bottom":"none";
		
		
		dataset = new DefaultCategoryDataset();
		
		double _value;
		String _legend;
		String _category;
		
		for(int j = 0; j < _dataAC.size(); j++)
		{
			_value		= Double.valueOf(_dataAC.get(j).get(_valueField).toString());
			_legend		= _dataAC.get(j).get(_legendField).toString();
			_category	= _dataAC.get(j).get(_categoryField).toString();
			
			((DefaultCategoryDataset) dataset).addValue(_value, _legend, _category);
		}
		
		if( _interiorGap > 0 ){
			this.makeChart(_title,_interiorGap);	
		}else{
			this.makeChart(_title);
		}
		
		chart.setBackgroundPaint(new Color(_yBackgroundColor));
		
		this.setGraphProperty("", false, 0, "", 0, 
				"", "", 0, 0, _legendDirection, 
				"", false, seriesColor , 0f , 0f,false);
					
		
		this.chart.getTitle().visible=titleVisible;
		
		return true;
	}
	
	/**
	 *  차트에 데이터 프로바이더를 설정 하는 함수
	 */
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
								String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
								String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor,
								float _seriesXFieldFontSize , float _seriesYFieldFontSize, boolean _rotateCategoryLabel)
	{
		//validation check
		if( _dataAC == null) return false;
		
		ArrayList<HashMap<String, Object>> _parseAC = new ArrayList<HashMap<String, Object>>();
		//crostab data check 시 데이터 변환 처리
		if( _crossTab == true )
		{
			for(int _i=1; _i < _dataAC.size(); _i++)
			{
				if( _i == 1 )
					_parseAC.add(_dataAC.get(0));
				else
				{
					if( _dataAC.get(_i-1).get(_seriesXField).toString() != _dataAC.get(_i).get(_seriesXField).toString() 
							&& _dataAC.get(_i-1).get(_yFieldName[0].toString()).toString() != _dataAC.get(_i).get(_yFieldName[0].toString()).toString() )
					{
						_parseAC.add(_dataAC.get(_i));		
					}
				}
			}	
			
			_dataAC = _parseAC;
			
		}
		
		
		
		//chart 속성 데이터 적용	
		//dataset = new DefaultCategoryDataset();
		this.makeChart(_title,0);
		
		this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
				_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
				_dataLabelPosition, _DubplicateAllow, _yFieldFillColor , _seriesXFieldFontSize , _seriesYFieldFontSize,_rotateCategoryLabel);
					
		return true;
	}
	
	
	protected void makeChart(String _title) 
	{
		if(dataset == null)
			makeDefaultDataSet();

		// 그리드 사용하는 경우 SpiderWebPlotWithGrid
		//SpiderWebPlotWithGrid plot = new SpiderWebPlotWithGrid((CategoryDataset) dataset , gridGap);
		SpiderWebPlot plot = new SpiderWebPlot((CategoryDataset) dataset);
		
		
		plot.setStartAngle(0);
	    plot.setInteriorGap(0.40);
	    plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
	    chart = new JFreeChart(_title,TextTitle.DEFAULT_FONT, plot, false);
	    LegendTitle legend = new LegendTitle(plot);
	    legend.setPosition(RectangleEdge.BOTTOM);
	    chart.addSubtitle(legend);  
	    
	}
	
	protected void makeChart(String _title,int gridGap) 
	{
		if(dataset == null)
			makeDefaultDataSet();

		// 그리드 사용하는 경우 SpiderWebPlotWithGrid
		SpiderWebPlotWithGrid plot = new SpiderWebPlotWithGrid((CategoryDataset) dataset , gridGap);
		//SpiderWebPlot plot = new SpiderWebPlot((CategoryDataset) dataset);
		
		
		plot.setStartAngle(0);
	    plot.setInteriorGap(0.40);
	    plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
	    chart = new JFreeChart(_title,TextTitle.DEFAULT_FONT, plot, false);
	    LegendTitle legend = new LegendTitle(plot);
	    legend.setPosition(RectangleEdge.BOTTOM);
	    chart.addSubtitle(legend);  
	    
	}
	
	// create a dataset...
	protected void makeDefaultDataSet()
	{
		dataset = new DefaultCategoryDataset();

		//계열
	    String taro = "太郎타로";
	    String jiro = "二郎지부";
	    String saburo = "三郎사부";
	 
	    //항목
	    String category1 = "優しさ부드러움";
	    String category2 = "強さ힘";
	    String category3 = "賢さ영리";
	    String category4 = "ユーモア유머";
	    String category5 = "かっこよさ멋있음";
	 
	    //데이터 집합을 만듦
	 
	    ((DefaultCategoryDataset) dataset).addValue(9.0, taro, category1);
	    ((DefaultCategoryDataset) dataset).addValue(4.0, taro, category2);
	    ((DefaultCategoryDataset) dataset).addValue(3.0, taro, category3);
	    ((DefaultCategoryDataset) dataset).addValue(1.0, taro, category4);
	    ((DefaultCategoryDataset) dataset).addValue(5.0, taro, category5);
	 
	    ((DefaultCategoryDataset) dataset).addValue(5.0, jiro, category1);
	    ((DefaultCategoryDataset) dataset).addValue(7.0, jiro, category2);
	    ((DefaultCategoryDataset) dataset).addValue(6.0, jiro, category3);
	    ((DefaultCategoryDataset) dataset).addValue(8.0, jiro, category4);
	    ((DefaultCategoryDataset) dataset).addValue(4.0, jiro, category5);
	 
	    ((DefaultCategoryDataset) dataset).addValue(4.0, saburo, category1);
	    ((DefaultCategoryDataset) dataset).addValue(3.0, saburo, category2);
	    ((DefaultCategoryDataset) dataset).addValue(2.0, saburo, category3);
	    ((DefaultCategoryDataset) dataset).addValue(9.0, saburo, category4);
	    ((DefaultCategoryDataset) dataset).addValue(6.0, saburo, category5);
	}
	
	/**
	 *  차트에  카테고리, Legend 등의 속성를 설정 하는 함수
	 */
	protected boolean setGraphProperty(String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor,
								float _seriesXFieldFontSize , float _seriesYFieldFontSize , boolean _rotateCategoryLabel)
	{

		//legend 위치
		LegendTitle legend = chart.getLegend();
		if("bottom".equals(_legendLocation))
		{
			legend.visible = true;
			legend.setPosition(RectangleEdge.BOTTOM);
		}
		else if("top".equals(_legendLocation))
		{
			legend.visible = true;
			legend.setPosition(RectangleEdge.TOP);
		}
		else if("left".equals(_legendLocation))
		{
			legend.visible = true;
			legend.setPosition(RectangleEdge.LEFT);
		}
		else if("right".equals(_legendLocation))
		{
			legend.visible = true;
			legend.setPosition(RectangleEdge.RIGHT);
		}
		else if("none".equals(_legendLocation))
		{
			legend.visible = false;
		}
		else
		{
			legend.visible = true;
			legend.setPosition(RectangleEdge.BOTTOM);
		}
		
		
		
		SpiderWebPlot plot = (SpiderWebPlot) chart.getPlot();
		Paint backgroundPaint = new Color(0xFFFFFF); 
		plot.setBackgroundPaint(backgroundPaint);
		plot.setBaseSeriesPaint(backgroundPaint);
		
		if( _yFieldFillColor != null ){

			for( int i=0; i<_yFieldFillColor.length; i++ ){
				plot.setSeriesPaint(i,new Color(_yFieldFillColor[i]));
			}
		}
		
		
		
		//ValueAxis domAxis = plot.getAxis();
		
		//Font font = domAxis.getLabelFont();
		// X축 라벨
		//domAxis.setLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
		// X축 도메인
		//domAxis.setTickLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
        
		//font = legend.getItemFont();
		// legend 라벨
		//legend.setItemFont(new Font("Dotum", Font.PLAIN, font.getSize()));
		
		return true;
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
			int[] _yFieldFillColor, String _colorFieldName) {
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
			int[] _yFieldFillColor) {
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
			int[] _yFieldFillColor, int angle, int rangeStart, int rangeEnd,
			String type) {
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
