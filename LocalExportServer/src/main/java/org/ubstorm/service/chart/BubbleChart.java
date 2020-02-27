package org.ubstorm.service.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;

public class BubbleChart extends BaseChart {
	
	public BubbleChart(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	
	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC,
			String[] _params) {
		// TODO Auto-generated method stub
		
		
		
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
//		if(!"".equals(_params[16]))
//		{			
//			String[] _staYFC = _params[16].split("~");
//			_yFieldFillColor = new int[_staYFC.length];
//            for(int i=0; i< _staYFC.length; i++)
//            {
//            	_yFieldFillColor[i] = Integer.decode(_staYFC[i]);
//            }
//		}
		
		
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
		
		//series 를 만들기 위해서 series 의 YField 를 위한 array 를 만든다.
		String[] _arr = _yFieldName;
		
		//series 데이터 생성
		//double[][] dynamicSeriesData = new double[_arr.length][_dataAC.size()];
		/*
		String[] displayName = new String[_arr.length];
		String[] xFieldNames = new String[_dataAC.size()];
		for(int i = 0; i < _arr.length; i++)
		{
			//display name filed 를 확인 한다.
			if(_yFieldDisplayName == null)
				displayName[i] = _arr[i];
			else
				displayName[i] = _yFieldDisplayName[i];
		}
		*/
		Double maxDataYValue=0d;
		Double maxDataXValue=0d;
		Double dataXValue = 0d;
		Double dataYValue = 0d;
		
		double xValues[] = new double[_dataAC.size()];
		double yValues[] = new double[_dataAC.size()];
		double zValues[] = new double[_dataAC.size()];
		 
		for(int j = 0; j < _dataAC.size(); j++)
		{
			//xFieldNames[j] = _dataAC.get(j).get(_seriesXField).toString();
			
			for(int i = 0; i < _arr.length; i++)
			{
				if(i==0)
				{
					// 20160201 데이터의 건수가 숫자형이 아닐경우 0으로 지정
					if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
					{
						dataXValue =  Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
						xValues[j]	= dataXValue;
						if( maxDataXValue < dataXValue ){
							maxDataXValue = dataXValue;
						}	
								
					}
					else
					{
						xValues[j]	= 0;
					}
				}
				else if(i==1)
				{
					// 20160201 데이터의 건수가 숫자형이 아닐경우 0으로 지정
					if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
					{
						dataYValue =  Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
						yValues[j]	= dataYValue;
						if( maxDataYValue < dataYValue ){
							maxDataYValue = dataYValue;
						}	
					}
					else
					{
						yValues[j]	= 0;
					}
				}
				else if(i==2)
				{
					// 20160201 데이터의 건수가 숫자형이 아닐경우 0으로 지정
					if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
					{
						zValues[j]	= Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
					}
					else
					{
						zValues[j]	= 0;
					}
				}
			}
		}
		
		//chart 속성 데이터 적용	
		dataset = new DefaultXYZDataset( );
//		double[][] dynamicSeriesData = {xValues, yValues, zValues};
//		((DefaultXYZDataset) dataset).addSeries( "Series 1" , dynamicSeriesData );
		//dataset = null;
		
		//TEST 20180409
		
		for( int cidx=0; cidx < xValues.length; cidx++ ){
			
			
			double xValues2[] = new double[1];
			xValues2[0] = xValues[cidx]; 
			
			double yValues2[] = new double[1];
			yValues2[0] = yValues[cidx]; 
			
			double zValues2[] = new double[1];
			zValues2[0] = zValues[cidx]; 
			
			double[][] dynamicSeriesData2 = {xValues2, yValues2, zValues2};
			
			String _seriesName = _dataAC.get(cidx).get(_seriesXField).toString(); 
			
			((DefaultXYZDataset) dataset).addSeries( _seriesName , dynamicSeriesData2 );
		}
		
		//TEST END 20180409
		
		
		this.makeChart(null);
		
		chart.setBackgroundPaint(new Color(_yBackgroundColor));
		
		this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
				_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
				_dataLabelPosition, _DubplicateAllow, _yFieldFillColor ,maxDataXValue,maxDataYValue,_rangeMax,
				_xFieldVisible,_yFieldVisible);
					
		return true;
	}
	
	/**
	 *  차트에 데이터 프로바이더를 설정 하는 함수
	 */
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
								String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
								String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor, float _rangeMax)
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
		
		//series 를 만들기 위해서 series 의 YField 를 위한 array 를 만든다.
		String[] _arr = _yFieldName;
		
		//series 데이터 생성
		//double[][] dynamicSeriesData = new double[_arr.length][_dataAC.size()];
		/*
		String[] displayName = new String[_arr.length];
		String[] xFieldNames = new String[_dataAC.size()];
		for(int i = 0; i < _arr.length; i++)
		{
			//display name filed 를 확인 한다.
			if(_yFieldDisplayName == null)
				displayName[i] = _arr[i];
			else
				displayName[i] = _yFieldDisplayName[i];
		}
		*/
		Double maxDataYValue=0d;
		Double maxDataXValue=0d;
		Double dataXValue = 0d;
		Double dataYValue = 0d;
		
		double xValues[] = new double[_dataAC.size()];
		double yValues[] = new double[_dataAC.size()];
		double zValues[] = new double[_dataAC.size()];
		 
		for(int j = 0; j < _dataAC.size(); j++)
		{
			//xFieldNames[j] = _dataAC.get(j).get(_seriesXField).toString();
			
			for(int i = 0; i < _arr.length; i++)
			{
				if(i==0)
				{
					// 20160201 데이터의 건수가 숫자형이 아닐경우 0으로 지정
					if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
					{
						dataXValue =  Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
						xValues[j]	= dataXValue;
						if( maxDataXValue < dataXValue ){
							maxDataXValue = dataXValue;
						}	
								
					}
					else
					{
						xValues[j]	= 0;
					}
				}
				else if(i==1)
				{
					// 20160201 데이터의 건수가 숫자형이 아닐경우 0으로 지정
					if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
					{
						dataYValue =  Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
						yValues[j]	= dataYValue;
						if( maxDataYValue < dataYValue ){
							maxDataYValue = dataYValue;
						}	
					}
					else
					{
						yValues[j]	= 0;
					}
				}
				else if(i==2)
				{
					// 20160201 데이터의 건수가 숫자형이 아닐경우 0으로 지정
					if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
					{
						zValues[j]	= Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
					}
					else
					{
						zValues[j]	= 0;
					}
				}
			}
		}
		
		//chart 속성 데이터 적용	
		dataset = new DefaultXYZDataset( );
//		double[][] dynamicSeriesData = {xValues, yValues, zValues};
//		((DefaultXYZDataset) dataset).addSeries( "Series 1" , dynamicSeriesData );
		//dataset = null;
		
		//TEST 20180409
		
		for( int cidx=0; cidx < xValues.length; cidx++ ){
			
			
			double xValues2[] = new double[1];
			xValues2[0] = xValues[cidx]; 
			
			double yValues2[] = new double[1];
			yValues2[0] = yValues[cidx]; 
			
			double zValues2[] = new double[1];
			zValues2[0] = zValues[cidx]; 
			
			double[][] dynamicSeriesData2 = {xValues2, yValues2, zValues2};
			
			String _seriesName = _dataAC.get(cidx).get(_seriesXField).toString(); 
			
			((DefaultXYZDataset) dataset).addSeries( _seriesName , dynamicSeriesData2 );
		}
		
		//TEST END 20180409
		
		
		this.makeChart(_title);
		
		this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
				_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
				_dataLabelPosition, _DubplicateAllow, _yFieldFillColor ,maxDataXValue,maxDataYValue,_rangeMax,true,true);
					
		return true;
	}
		
	protected void makeChart(String _title) 
	{
		if(dataset == null)
			makeDefaultDataSet();
	
		chart = ChartFactory.createBubbleChart(
				 null, 
		         null, 
		         null, 
		         (DefaultXYZDataset)dataset, // data
		         PlotOrientation.HORIZONTAL,           
		         true, // include legend
		         true,
		         false);
		         
		
		XYPlot xyplot = ( XYPlot )chart.getPlot( );
	 
		NumberAxis numberaxis = ( NumberAxis )xyplot.getDomainAxis( );
	    numberaxis.setLowerMargin( 0.2 );
	    numberaxis.setUpperMargin( 0.5 );
	    NumberAxis numberaxis1 = ( NumberAxis )xyplot.getRangeAxis( );
	    numberaxis1.setLowerMargin( 0.8 );
	    numberaxis1.setUpperMargin( 0.9 );
	}
	
	protected void makeDefaultDataSet()
	{
		dataset = new DefaultXYZDataset( );
		double ad[ ] = { 30 , 40 , 50 , 60 , 70 , 80 };
	    double ad1[ ] = { 10 , 20 , 30 , 40 , 50 , 60 };
	    double ad2[ ] = { 4 , 5 , 10 , 8 , 9 , 6 };
	    double ad3[ ][ ] = { ad , ad1 , ad2 };
	    ((DefaultXYZDataset) dataset).addSeries( "Series 1" , ad3 );
	}
	
	/**
	 *  차트에  카테고리, Legend 등의 속성를 설정 하는 함수
	 */
	protected boolean setGraphProperty(String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor,Double _maxDataXValue, Double _maxDataYValue,float _rangeMax,
								boolean _xFieldVisible,boolean _yFieldVisible)
	{
		XYPlot xyplot = ( XYPlot )chart.getPlot( );
		//xyplot.setForegroundAlpha(0.5f);
        Paint backgroundPaint = new Color(0xFFFFFF); 
        xyplot.setBackgroundPaint(backgroundPaint);

        xyplot.setForegroundAlpha(0.7f);	
		
        Stroke lineStroke = new BasicStroke(_gridLineWeight) ;
		// grid line 속성 변환 
		if(_gridLIne == true)
		{
//	        xyplot.setDomainGridlinesVisible(true);
//	        Paint domainLinePaint = new Color(_gridLIneColor);
//	        xyplot.setDomainGridlinePaint(domainLinePaint);
//	        xyplot.setRangeGridlinesVisible(true);
//	        xyplot.setRangeGridlinePaint(domainLinePaint);
//	        
	        
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
		
//        if( _yFieldFillColor.length > 0 ){
//        	XYItemRenderer xyitemrenderer = xyplot.getRenderer();
//        	for(int i=0; i<_yFieldFillColor.length; i++ )
//        	{
//        		xyitemrenderer.setSeriesPaint( i , new Color(_yFieldFillColor[i]) );
//        	}
//        }
        
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
		
		if( _rangeMax != 0 ){
			Double MaxDataYValue = _maxDataYValue*_rangeMax;
			rangeAxis.setRange(new Range(0, MaxDataYValue));
			
			Double MaxDataXValue = _maxDataXValue*_rangeMax;
			domAxis.setRange(new Range(0, MaxDataXValue));
		}
		
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
			int[] _yFieldFillColor) {
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
			int[] _yFieldFillColor, float _seriesXFieldFontSize,
			float _seriesYFieldFontSize, boolean _rotateCategoryLabel,
			int _xLineWeight, int _yLineWeight, int _outLineWeight,
			float _rangeMax) {
		// TODO Auto-generated method stub
		return false;
	}

}
