package org.ubstorm.service.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.ubstorm.service.utils.common;

public class CandleStickChart extends BaseChart {
	
	public CandleStickChart(int width, int height)
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
		
		String _seriesCloseField = null;
		String _seriesHighField = null;
		String _seriesLowField = null;
		String _seriesOpenField = null;
		
		String _colorFieldName = null;
		
		
		int _candleWidth = 1;
		int _candleUpColor = 0xff0000;
		int _candleDownColor = 0x00ff00;
		
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
		
		
		if(_params.length > 21){
			_seriesCloseField = "".equals(_params[17]) ? null : _params[17];
			_seriesHighField = "".equals(_params[18]) ? null : _params[18];
			_seriesLowField = "".equals(_params[19]) ? null : _params[19];
			_seriesOpenField = "".equals(_params[20]) ? null : _params[20];
			_colorFieldName = "".equals(_params[21]) ? null : _params[21];
		}
		
		
		
		if(_params.length > 29 ){	
			_candleWidth = "".equals(_params[26]) ? 0 : Integer.valueOf(_params[26]);
			_candleUpColor = "".equals(_params[27]) ? 0xff0000 : Integer.decode(_params[27]); 
			_candleDownColor = "".equals(_params[28]) ? 0x00ff00 : Integer.decode(_params[28]); 
			_rangeMax			="".equals(_params[29]) ? 0 : common.ParseFloatNullChk(_params[29],0);
		}
		
		if( _params.length == 33)
		{
			_xFieldVisible = "".equals(_params[30]) ? true : Boolean.valueOf(_params[30]); 
			_yFieldVisible = "".equals(_params[31]) ? true : Boolean.valueOf(_params[31]); 
			_yBackgroundColor	= "".equals(_params[32]) ? 0xffffff : Integer.decode(_params[32]);
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
				double[][] dynamicSeriesData = new double[4][_dataAC.size()];
				//String[] xFieldNames = new String[_dataAC.size()];
				
				//String[] timeData = new String[_dataAC.size()];
				
				for(int j = 0; j < _dataAC.size(); j++)
				{
					//xFieldNames[j] = _dataAC.get(j).get(_seriesXField).toString();
					
					/*
					for(int i = 0; i < _arr.length; i++)
					{
						dynamicSeriesData[i][j]	= Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
					}
					*/			
					dynamicSeriesData[0][j]	= Double.valueOf( _dataAC.get(j).get(_seriesCloseField).toString());
					dynamicSeriesData[1][j]	= Double.valueOf( _dataAC.get(j).get(_seriesHighField).toString());
					dynamicSeriesData[2][j]	= Double.valueOf( _dataAC.get(j).get(_seriesLowField).toString());
					dynamicSeriesData[3][j]	= Double.valueOf( _dataAC.get(j).get(_seriesOpenField).toString());
					
					//timeData[j] = _dataAC.get(j).get("col_5").toString();
				}
				
				//chart 속성 데이터 적용	
				//dataset = new DefaultXYZDataset( );
				//dataset.addSeries( "Series 1" , dynamicSeriesData );
				dataset = (OHLCDataset) new OHLCSeriesCollection();
//				OHLCSeries series = new OHLCSeries("Series 1");
				OHLCSeries series;
				
				String xFieldKey;
				
				xFieldKeys = new HashMap();
				
				Double maxDataValue=0d;
				Double dataValue;
				
			    for (int j = 0; j < _dataAC.size(); j++) {
			    	
			    	// 같은 이름의 series에 넣어야함.
			    	xFieldKey = _dataAC.get(j).get(_seriesXField).toString();
			    	
			    	series = getKeySeries(xFieldKey);
			    	
			    	 // Generate new bar time
			         Calendar cal = Calendar.getInstance();
			         cal.add(Calendar.MINUTE, j);
			         FixedMillisecond fm = new FixedMillisecond(cal.getTime());
			         
		             double close = dynamicSeriesData[0][j];
		             double high = dynamicSeriesData[1][j];
		             double row = dynamicSeriesData[2][j];
		             double open = dynamicSeriesData[3][j];
		             
		             if( maxDataValue < high ){
							maxDataValue = high;
						}
		             
		             //String timeString = timeData[j];
//		             
//			         DateFormat format = new SimpleDateFormat("yyyymmdd");
//			         Date time = null;
//					try {
//						time = format.parse(timeString);
//					} catch (ParseException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			         
//			         FixedMillisecond fm = new FixedMillisecond(time);
			         
		             
		             if( series == null ){
		            	 series = new OHLCSeries(xFieldKey);
		            	 series.add(fm, close, high, row, open);
		            	 xFieldKeys.put(xFieldKey, series);
		            	 
		            	 ((OHLCSeriesCollection) dataset).addSeries(series);
		             }else{
		            	 series.add(fm, close, high, row, open);
		             }
		        }
		        
				
				this.makeChart(null);
				
				chart.setBackgroundPaint(new Color(_yBackgroundColor));
				
				this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
						_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
						_dataLabelPosition, _DubplicateAllow, _yFieldFillColor,_candleWidth, _candleUpColor,_candleDownColor ,maxDataValue , _rangeMax,
						_xFieldVisible,_yFieldVisible);
							
				return true;
		
	}
	
	
	/**
	 *  차트에 데이터 프로바이더를 설정 하는 함수
	 */
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title,
								String _seriesXField, String _seriesCloseField, String _seriesHighField, String _seriesLowField, String _seriesOpenField, 
								String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
								String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor
								,int _candleWidth, int _candleUpColor,int _candleDownColor , float _rangeMax)
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
		double[][] dynamicSeriesData = new double[4][_dataAC.size()];
		//String[] xFieldNames = new String[_dataAC.size()];
		
		//String[] timeData = new String[_dataAC.size()];
		
		for(int j = 0; j < _dataAC.size(); j++)
		{
			//xFieldNames[j] = _dataAC.get(j).get(_seriesXField).toString();
			
			/*
			for(int i = 0; i < _arr.length; i++)
			{
				dynamicSeriesData[i][j]	= Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
			}
			*/			
			dynamicSeriesData[0][j]	= Double.valueOf( _dataAC.get(j).get(_seriesCloseField).toString());
			dynamicSeriesData[1][j]	= Double.valueOf( _dataAC.get(j).get(_seriesHighField).toString());
			dynamicSeriesData[2][j]	= Double.valueOf( _dataAC.get(j).get(_seriesLowField).toString());
			dynamicSeriesData[3][j]	= Double.valueOf( _dataAC.get(j).get(_seriesOpenField).toString());
			
			//timeData[j] = _dataAC.get(j).get("col_5").toString();
		}
		
		//chart 속성 데이터 적용	
		//dataset = new DefaultXYZDataset( );
		//dataset.addSeries( "Series 1" , dynamicSeriesData );
		dataset = (OHLCDataset) new OHLCSeriesCollection();
//		OHLCSeries series = new OHLCSeries("Series 1");
		OHLCSeries series;
		
		String xFieldKey;
		
		xFieldKeys = new HashMap();
		
		Double maxDataValue=0d;
		Double dataValue;
		
	    for (int j = 0; j < _dataAC.size(); j++) {
	    	
	    	// 같은 이름의 series에 넣어야함.
	    	xFieldKey = _dataAC.get(j).get(_seriesXField).toString();
	    	
	    	series = getKeySeries(xFieldKey);
	    	
	    	 // Generate new bar time
	         Calendar cal = Calendar.getInstance();
	         cal.add(Calendar.MINUTE, j);
	         FixedMillisecond fm = new FixedMillisecond(cal.getTime());
	         
             double close = dynamicSeriesData[0][j];
             double high = dynamicSeriesData[1][j];
             double row = dynamicSeriesData[2][j];
             double open = dynamicSeriesData[3][j];
             
             if( maxDataValue < high ){
					maxDataValue = high;
				}
             
             //String timeString = timeData[j];
//             
//	         DateFormat format = new SimpleDateFormat("yyyymmdd");
//	         Date time = null;
//			try {
//				time = format.parse(timeString);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	         
//	         FixedMillisecond fm = new FixedMillisecond(time);
	         
             
             if( series == null ){
            	 series = new OHLCSeries(xFieldKey);
            	 series.add(fm, close, high, row, open);
            	 xFieldKeys.put(xFieldKey, series);
            	 
            	 ((OHLCSeriesCollection) dataset).addSeries(series);
             }else{
            	 series.add(fm, close, high, row, open);
             }
        }
        
		
		this.makeChart(_title);
		
		this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
				_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
				_dataLabelPosition, _DubplicateAllow, _yFieldFillColor,_candleWidth, _candleUpColor,_candleDownColor ,maxDataValue , _rangeMax,
				true,true);
					
		return true;
	}
	
	private HashMap<String, OHLCSeries> xFieldKeys;
	
	private OHLCSeries getKeySeries( String _name )
	{
		// hasmap에서 동일한 name이 존재하는지 찾고, 없으면 null.
		OHLCSeries series = xFieldKeys.get(_name);
		
		if( series == null ){
			return null;
		}
		
		return series;
	}
	
	
	
	protected void makeChart(String _title) 
	{
		if(dataset == null)
			makeDefaultDataSet();
	   
	    chart = ChartFactory.createCandlestickChart(
	    		null, 
	            null, 
	            null, 
	            (OHLCDataset)dataset, 
	            true);
	}
	
	protected void makeDefaultDataSet()
	{
	    dataset = (OHLCDataset) new OHLCSeriesCollection();
		OHLCSeries series = new OHLCSeries("Test data");
		((OHLCSeriesCollection) dataset).addSeries(series);
		
	    createTestData((OHLCSeriesCollection)dataset);
	}
	
	/**
	  * Fill given series with test data
	  * @param seriesCollection
	  * collection with one series element to fill with data
	 */
	private void createTestData(OHLCSeriesCollection seriesCollection) {
	     OHLCSeries series = seriesCollection.getSeries(0);
	     for (int i = 0; i < 10; i++) {
	         // Generate new bar time
	         Calendar cal = Calendar.getInstance();
	         cal.add(Calendar.MINUTE, i);
	         FixedMillisecond fm = new FixedMillisecond(cal.getTime());
	         // Add bar to the data. Let's repeat the same bar
	         series.add(fm, 100, 110, 90, 105);
	     }
	}
	
	
	/**
	 *  차트에  카테고리, Legend 등의 속성를 설정 하는 함수
	 */
	protected boolean setGraphProperty(String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor,
								int _candleWidth, int _candleUpColor,int _candleDownColor , Double _maxDataValue , float _rangeMax,
								boolean _xFieldVisible,boolean _yFieldVisible)
	{
		XYPlot xyplot = ( XYPlot )chart.getPlot( );
		
		
		
//		DateAxis axis = (DateAxis) xyplot.getDomainAxis();
//		axis.setDateFormatOverride(new SimpleDateFormat("yyyymmdd"));
//		
		
		
        xyplot.setRangeGridlinePaint(Color.lightGray);
        xyplot.setBackgroundPaint(Color.white);
        
        
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
		
//        if( _yFieldFillColor !=null && _yFieldFillColor.length > 0 ){
//        	CandlestickRenderer candlestickRenderer =(CandlestickRenderer) xyplot.getRenderer(); 
//        	for(int i=0; i<_yFieldFillColor.length; i++ )
//        	{
//        		candlestickRenderer.setSeriesPaint( i , new Color(_yFieldFillColor[i]) );
//        		candlestickRenderer.setSeriesFillPaint(i , new Color(_yFieldFillColor[i]) );
//        	}
//        }


        //OHLCDataset _datase =(OHLCDataset) this.dataset;
        //int _dsCount= _datase.getSeriesCount();
		
        CandlestickRenderer candlestickRenderer =(CandlestickRenderer) xyplot.getRenderer();
        
        
        if( _candleWidth > 0 ){
            candlestickRenderer.setAutoWidthMethod(1);
            candlestickRenderer.setCandleWidth(_candleWidth);
        }
        
		candlestickRenderer.setUpPaint(new Color(_candleUpColor));
		candlestickRenderer.setDownPaint( new Color(_candleDownColor));
        
        //for( int j=0; j<_dsCount; j++ ){
//    		candlestickRenderer.setVolumePaint(new Color(0x0000ff));
//    		candlestickRenderer.setDrawVolume(true);
        	//candlestickRenderer.setSeriesPaint( j , new Color(_yFieldFillColor[j]) );
    		//candlestickRenderer.setSeriesFillPaint(j , new Color(_yFieldFillColor[j]) );
//    		candlestickRenderer.setSeriesFillPaint(j , Color.ORANGE );
    		//candlestickRenderer.setSeriesPaint(j, Color.BLACK);
    		//candlestickRenderer.setDrawVolume(false);
        //}
        
        
		
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
			Double MaxDataValue = _maxDataValue*_rangeMax;
			rangeAxis.setRange(new Range(0, MaxDataValue));
		}
		
		return true;
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
			int _candleDownColor) {
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
