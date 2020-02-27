package org.ubstorm.service.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;

public class PlotChart extends BaseChart {
	
	private int N = 8;
    private Random rand = new Random();
	
	public PlotChart(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	
	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String[] _params) {
		
		
		String _seriesXField = null;
		String[] _yFieldName = null;
		String[] _yFieldDisplayName = null;
		boolean _crossTab = false; 
		String _form = "segment";
		boolean _gridLIne = true; 
		int _gridLineWeight = 1;
		String _gridLIneDirection = "both";
		int _gridLIneColor = 0xd8d3d3;
		String _legendDirection = "vertical";
		String _legendLabelPlacement = "right";
		int _legendMarkHeight = 10;
		int _legendMarkWeight = 10;
		String _legendLocation = "bottom";
		String _dataLabelPosition = "inside";
		boolean _DubplicateAllow = false; 
		int [] _yFieldFillColor = null;
		
		float _rangeMax=0;
		
		boolean _xFieldVisible = true; 
		boolean _yFieldVisible = true; 
		
		int _yBackgroundColor = 0xffffff;
		
		_seriesXField = "".equals(_params[0]) ? null : _params[0];
		_yFieldName = "".equals(_params[1]) ? null : _params[1].split("~");
		_yFieldDisplayName = "".equals(_params[2]) ? null : _params[2].split("~");
		_crossTab = "".equals(_params[3]) ? false : Boolean.valueOf(_params[3]); 
		_form = "".equals(_params[4]) ? "segment" : _params[4];
		_gridLIne = "".equals(_params[5]) ? true : Boolean.valueOf(_params[5]); 
		_gridLineWeight = "".equals(_params[6]) ? 1 : Integer.valueOf(_params[6]);
		_gridLIneDirection = "".equals(_params[7]) ? "both" : _params[7];
		_gridLIneColor = "".equals(_params[8]) ? 0xd8d3d3 : Integer.decode(_params[8]);
		_legendDirection = "".equals(_params[9]) ? "vertical" : _params[9];
		_legendLabelPlacement = "".equals(_params[10]) ? "right" : _params[10];
		_legendMarkHeight = "".equals(_params[11]) ? 10 : Integer.valueOf(_params[11]);
		_legendMarkWeight = "".equals(_params[12]) ? 10 : Integer.valueOf(_params[12]);
		_legendLocation = (_params.length==23 &"".equals(_params[22])) ? "bottom" : _params[22];
		_dataLabelPosition = "".equals(_params[13]) ? "inside" : _params[13];
		_DubplicateAllow = "".equals(_params[15]) ? false : Boolean.valueOf(_params[15]); 
		_yFieldFillColor = null;
		if(!"".equals(_params[16]))
		{			
			String[] _staYFC = _params[16].split("~");
			_yFieldFillColor = new int[_staYFC.length];
            for(int i=0; i< _staYFC.length; i++)
            {
            	_yFieldFillColor[i] = Integer.decode(_staYFC[i]);
            }
		}
		
		
		if( _params.length > 26)
		{
			_rangeMax			="".equals(_params[26]) ? 0 : common.ParseFloatNullChk(_params[26],0);
		}
		
		if( _params.length == 30)
		{
			_xFieldVisible = "".equals(_params[27]) ? true : Boolean.valueOf(_params[27]); 
			_yFieldVisible = "".equals(_params[28]) ? true : Boolean.valueOf(_params[28]); 
			_yBackgroundColor	= "".equals(_params[29]) ? 0xffffff : Integer.decode(_params[29]);
		}
		
		
		
		//validation check
		if( _dataAC == null) {
			log.error(getClass().getName() + ":: setGraphData() - DataSet is null.");
			return false;
		}
		
		if( _yFieldDisplayName == null) {
			
			_yFieldDisplayName = new String[_yFieldName.length];
			
			for(int i = 0; i < _yFieldName.length; i++)
			{
				_yFieldDisplayName[i] = _yFieldName[i];
			}
		}
		
		if( _yFieldDisplayName == null) {
			log.error(getClass().getName() + ":: setGraphData() - _yFieldDisplayName is null.");
			return false;
		}
		

		
		
		
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
		dataset = (XYDataset) new XYSeriesCollection();
		
		
		String _ySeriesFieldName;
		
		Double _xSeriesValue;
		Double _ySeriesValue;
		
		for( int _idx=0; _idx<_yFieldDisplayName.length; _idx++ ){
			
			XYSeries series = new XYSeries(_yFieldDisplayName[_idx]);
			
			_ySeriesFieldName = _yFieldName[_idx];
			
			for( int _dIdx=0; _dIdx<_dataAC.size(); _dIdx++ ){
				
				_xSeriesValue = Double.valueOf( _dataAC.get(_dIdx).get(_seriesXField).toString() );
				
				_ySeriesValue = Double.valueOf( _dataAC.get(_dIdx).get(_ySeriesFieldName).toString() );
				
				series.add(_xSeriesValue,_ySeriesValue);
			}
			
			((XYSeriesCollection) dataset).addSeries(series);
			
		}
		
		
		
		
		
		
		

		
//	    for (int j = 0; j < _dataAC.size(); j++) {
//            double x = dynamicSeriesData[0][j];
//            double y = dynamicSeriesData[1][j];
//        
//            String _seriesName = _dataAC.get(j).get(_seriesXField).toString();
//            XYSeries series = new XYSeries(_seriesName);
//            series.add(x, y);
//            ((XYSeriesCollection) dataset).addSeries(series);
//        }
        
		
		this.makeChart(null);
		
		chart.setBackgroundPaint(new Color(_yBackgroundColor));
		
		this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
				_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
				_dataLabelPosition, _DubplicateAllow, _yFieldFillColor,_xFieldVisible,_yFieldVisible);
					
		return true;
	}

	
	
	/**
	 *  차트에 데이터 프로바이더를 설정 하는 함수
	 */
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
								String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
								String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor)
	{
		//validation check
		if( _dataAC == null) {
			log.error(getClass().getName() + ":: setGraphData() - DataSet is null.");
			return false;
		}
		
		if( _yFieldDisplayName == null) {
			
			_yFieldDisplayName = new String[_yFieldName.length];
			
			for(int i = 0; i < _yFieldName.length; i++)
			{
				_yFieldDisplayName[i] = _yFieldName[i];
			}
		}
		
		if( _yFieldDisplayName == null) {
			log.error(getClass().getName() + ":: setGraphData() - _yFieldDisplayName is null.");
			return false;
		}
		

		
		
		
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
		dataset = (XYDataset) new XYSeriesCollection();
		
		
		String _ySeriesFieldName;
		
		Double _xSeriesValue;
		Double _ySeriesValue;
		
		for( int _idx=0; _idx<_yFieldDisplayName.length; _idx++ ){
			
			XYSeries series = new XYSeries(_yFieldDisplayName[_idx]);
			
			_ySeriesFieldName = _yFieldName[_idx];
			
			for( int _dIdx=0; _dIdx<_dataAC.size(); _dIdx++ ){
				
				_xSeriesValue = Double.valueOf( _dataAC.get(_dIdx).get(_seriesXField).toString() );
				
				_ySeriesValue = Double.valueOf( _dataAC.get(_dIdx).get(_ySeriesFieldName).toString() );
				
				series.add(_xSeriesValue,_ySeriesValue);
			}
			
			((XYSeriesCollection) dataset).addSeries(series);
			
		}
		
		
		
		
		
		
		

		
//	    for (int j = 0; j < _dataAC.size(); j++) {
//            double x = dynamicSeriesData[0][j];
//            double y = dynamicSeriesData[1][j];
//        
//            String _seriesName = _dataAC.get(j).get(_seriesXField).toString();
//            XYSeries series = new XYSeries(_seriesName);
//            series.add(x, y);
//            ((XYSeriesCollection) dataset).addSeries(series);
//        }
        
		
		this.makeChart(_title);
		
		this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
				_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
				_dataLabelPosition, _DubplicateAllow, _yFieldFillColor,true,true);

		return true;
	}
	
	
	protected void makeChart(String _title) 
	{
		if(dataset == null)
			makeDefaultDataSet();
	
		chart = ChartFactory.createScatterPlot(
				 null, 
		         null, 
		         null, 
		         (XYDataset) dataset, // data
		         PlotOrientation.VERTICAL,           
		         true, // include legend
		         true,
		         false);			         
		
		XYPlot xyPlot = (XYPlot) chart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setVerticalTickLabels(true);
	}
	
	protected void makeDefaultDataSet()
	{
		dataset = (XYDataset) new XYSeriesCollection();

		XYSeries series = new XYSeries("Random");
        for (int i = 0; i < N * N; i++) {
            double x = rand.nextGaussian();
            double y = rand.nextGaussian();
            series.add(x, y);
        }
        ((XYSeriesCollection) dataset).addSeries(series);
        
        XYSeries added = new XYSeries("Added");
        for (int i = 0; i < N; i++) {
            added.add(rand.nextGaussian(), rand.nextGaussian());
        }
        ((XYSeriesCollection) dataset).addSeries(added);
	}
	
	/**
	 *  차트에  카테고리, Legend 등의 속성를 설정 하는 함수
	 */
	protected boolean setGraphProperty(String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor,
								boolean _xFieldVisible,boolean _yFieldVisible)
	{
		XYPlot xyplot = ( XYPlot )chart.getPlot( );
        Paint backgroundPaint = new Color(255,255,255,0); 
        xyplot.setBackgroundPaint(backgroundPaint);
        xyplot.setBackgroundAlpha(0.5f);

        Stroke lineStroke = new BasicStroke(_gridLineWeight) ;
		// grid line 속성 변환 
		if(_gridLIne == true)
		{
//	        xyplot.setDomainGridlinesVisible(true);
//	        Paint domainLinePaint = new Color(_gridLIneColor);
//	        xyplot.setDomainGridlinePaint(domainLinePaint);
//	        xyplot.setRangeGridlinesVisible(true);
//	        xyplot.setRangeGridlinePaint(domainLinePaint);
	        
			//공통속성 				      
			Paint domainLinePaint = new Color(_gridLIneColor);
	        
			//세로
			if(_gridLIneDirection.equals("both") || _gridLIneDirection.equals("vertical")){
				xyplot.setDomainGridlinesVisible(true);  
				xyplot.setDomainGridlinePaint(domainLinePaint);
		        xyplot.setDomainGridlineStroke(lineStroke);	//그리드 세로 두께 설정
			}
			 //가로
			if(_gridLIneDirection.equals("both") || _gridLIneDirection.equals("horizontal")){				
				xyplot.setRangeGridlinesVisible(true);
		        xyplot.setRangeGridlinePaint(domainLinePaint);	        
		        xyplot.setRangeGridlineStroke(lineStroke);	//그리드 가로 두께 설정 
			}   
	        
	 	}
		else
		{
			xyplot.setDomainGridlinesVisible(false);
			xyplot.setRangeGridlinesVisible(false);
		}
					
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
		
        if( _yFieldFillColor.length > 0 ){
        	XYItemRenderer xyitemrenderer = xyplot.getRenderer();
        	for(int i=0; i<_yFieldFillColor.length; i++ )
        	{
        		xyitemrenderer.setSeriesPaint( i , new Color(_yFieldFillColor[i]) );
        	}
        }
        
        // datalabel 위치
        XYItemRenderer cir = xyplot.getRenderer();
        cir.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
        if("inside".equals(_dataLabelPosition))
		{
        	 cir.setBaseItemLabelsVisible(true);
        	 cir.setNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE12, TextAnchor.CENTER));
		}
		else if("outside".equals(_dataLabelPosition))
		{
       	 	cir.setBaseItemLabelsVisible(true);
       	 	cir.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER));
		}
		else if("callOut".equals(_dataLabelPosition) || "insideWithCallOut".equals(_dataLabelPosition))
		{
       	 	cir.setBaseItemLabelsVisible(true);
       	 	cir.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));
		}
		else if("none".equals(_dataLabelPosition))
		{
			//cir.setBaseItemLabelsVisible(false);
       	 	cir.setBaseItemLabelGenerator(null);
		}
		else
		{
			//cir.setBaseItemLabelsVisible(false);
       	 	cir.setBaseItemLabelGenerator(null);
       	 	//cir.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER));
		}
        
        
        //20190225
		if( _xFieldVisible == false ){
			// 차트 하단 data label visible
			xyplot.getDomainAxis().setVisible(false);
		}
		
		if( _yFieldVisible == false ){
			// 차트 좌측 label data visible
			xyplot.getRangeAxis().setTickLabelsVisible(false);
		}
        
        
		////////////////////////////////////////////////////////////////
		// 한글 깨짐 해결
		ValueAxis domAxis = xyplot.getDomainAxis();
        
		Font font = domAxis.getLabelFont();
		// X축 라벨
		domAxis.setLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
		// X축 도메인
		domAxis.setTickLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
		
		ValueAxis rangeAxis = xyplot.getRangeAxis();
		font = rangeAxis.getLabelFont();
		// Y축 라벨
		rangeAxis.setLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
		// Y축 범위
		rangeAxis.setTickLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
        
		font = legend.getItemFont();
		// legend 라벨
		legend.setItemFont(new Font("Dotum", Font.PLAIN, font.getSize()));
		
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
			int[] _yFieldFillColor, float seriesXFieldFontSize,
			float seriesYFieldFontSize, boolean _rotateCategoryLabel) {
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
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title, String _seriesXField,
			String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab, String _form, boolean _gridLIne,
			int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow, int[] _yFieldFillColor, int angle, int rangeStart,
			int rangeEnd, String type) {
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
	protected boolean setGraphProperty(String _form, boolean _gridLIne,
			int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,
			String _legendDirection, String _legendLabelPlacement,
			int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition,
			boolean _DubplicateAllow, int[] _yFieldFillColor) {
		// TODO Auto-generated method stub
		return false;
	}

}
