package org.ubstorm.service.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;

public class CombinedColumnChart extends BaseChart {

	private ArrayList datasets = new ArrayList<Object>();
	private String model_type = "stacked";
	
	public CombinedColumnChart(int width, int height, String model_type)
	{
		this.width = width;
		this.height = height;
		this.model_type = model_type;
	}
	
	
	/**
	 *  차트에 데이터 프로바이더를 설정 하는 함수
	 */
	@Override
	public boolean setGraphData(ArrayList<ArrayList<HashMap<String, Object>>> _dataACL,HashMap<Integer, String> displayNamesMap) 
	{
		float _rangeMax=0;
		
		String[] _seriesXField = null;
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
		String _seriexCloseField = null;
		String _seriesHighField = null;
		String _seriesLowField = null;
		String _seriesOpenField = null;

		
		
		
		boolean _xFieldVisible = true; 
		boolean _yFieldVisible = true; 
		
		String _columnAxisName="Column Axis";
		String _lineAxisName="Line Axis";
		
		int _yBackgroundColor = 0xffffff;
		
		double _categoryMargin=0.2d;
		double _itemMargin=0.2d;
		
		
		String [] _params;// = PARAM.split(",");
		
		HashMap<Integer, String[]> _yFieldNames = new HashMap<Integer, String[]>();
		HashMap<Integer, String[]> _yFieldDisplayNames = new HashMap<Integer, String[]>();
		HashMap<Integer, int []> _yFieldFillColors = new HashMap<Integer, int[]>();
		
		String _dispNamesValue="";
		
		for( int dispIndex=0; dispIndex<displayNamesMap.size(); dispIndex++ ){
			
			_dispNamesValue = displayNamesMap.get(dispIndex);
			_params= _dispNamesValue.split(",");
			
			if(_params != null && (_params.length == 17 || _params.length == 23 || _params.length == 27 || _params.length == 32 || _params.length == 34))
			{
				_seriesXField = "".equals(_params[0]) ? null : _params[0].split("~");
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
				
				if(_params.length == 21)
				{
					_seriexCloseField = "".equals(_params[17]) ? null : _params[17];
					_seriesHighField = "".equals(_params[18]) ? null : _params[18];
					_seriesLowField = "".equals(_params[19]) ? null : _params[19];
					_seriesOpenField = "".equals(_params[20]) ? null : _params[20];
				}		
				if( _params.length > 26 ){					
					_rangeMax			="".equals(_params[26]) ? 0 : common.ParseFloatNullChk(_params[26],0);					
				}
				if(_params.length == 32 || _params.length == 34){
					
					_xFieldVisible = "".equals(_params[27]) ? true : Boolean.valueOf(_params[27]); 
					_yFieldVisible = "".equals(_params[28]) ? true : Boolean.valueOf(_params[28]); 
					
					
					_columnAxisName		="".equals(_params[29]) ? "Column Axis" : _params[29];
					_lineAxisName		="".equals(_params[30]) ? "Line Axis" : _params[30];
					_yBackgroundColor	= "".equals(_params[31]) ? 0xffffff : Integer.decode(_params[31]);
					
					if(_params.length == 34)
					{
						_categoryMargin		= "".equals(_params[32]) ? 0.2d : Double.valueOf(_params[32]);
						_itemMargin			= "".equals(_params[33]) ? 0.2d : Double.valueOf(_params[33]);
					}
				}
				
			}			
			_yFieldNames.put(dispIndex, _yFieldName);
			_yFieldDisplayNames.put(dispIndex, _yFieldDisplayName);
			_yFieldFillColors.put(dispIndex, _yFieldFillColor);
		}
		
		
		
		
		
		
		
		
		
		
		
		//validation check
		if(_dataACL.size() == 0) return false;
		
		Double maxColumnDataValue=0d; 
		Double maxLineDataValue=0d;
		Double dataValue;
		
		
		for(int k=0; k < _dataACL.size(); k++)
		{
			ArrayList<HashMap<String, Object>> _dataAC = _dataACL.get(k);
				
			if( _dataAC == null) return false;

			if(_yFieldNames == null)
			{
				log.error(getClass().getName() + "::" + "Call setGraphData()...yFiledName is null.");
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
						
						
						if( _dataAC.get(_i-1).get(_seriesXField[_i]).toString() != _dataAC.get(_i).get(_seriesXField[_i]).toString() 
								&& _dataAC.get(_i-1).get(_yFieldNames.get(k)[0].toString()).toString() != _dataAC.get(_i).get(_yFieldNames.get(k)[0].toString()).toString() )
						{
							_parseAC.add(_dataAC.get(_i));		
						}
					}
				}	
				
				_dataAC = _parseAC;
				
			}
			
			//series 를 만들기 위해서 series 의 YField 를 위한 array 를 만든다.
			String[] _arr = _yFieldNames.get(k);
			
			//series 데이터 생성
			double[][] dynamicSeriesData = new double[_arr.length][_dataAC.size()];
			String[] displayName = new String[_arr.length];
			String[] xFieldNames = new String[_dataAC.size()];
			for(int i = 0; i < _arr.length; i++)
			{
				//display name filed 를 확인 한다.
				if(_yFieldDisplayNames == null)
					displayName[i] = _arr[i];
				else
					displayName[i] = _yFieldDisplayNames.get(k) == null || (_yFieldDisplayNames.get(k).length < (i+1)) ? _arr[i] : _yFieldDisplayNames.get(k)[i];	
			}
			
			HashMap _hXFields = new HashMap();
			String _stFieldKey = "";
			for(int j = 0; j < _dataAC.size(); j++)
			{
				_stFieldKey = _dataAC.get(j).get(_seriesXField[0]).toString();	// BarRenderer의 CategoryLabel만 표시되도록 하기위해
//				_stFieldKey = _dataAC.get(j).get(_seriesXFields[k]).toString();
				
				if(_hXFields.containsKey(_stFieldKey))
					_stFieldKey = _stFieldKey + j;
				_hXFields.put(_stFieldKey, j);
				
				xFieldNames[j] = _stFieldKey; // 중복안되게해야함!
				
				for(int i = 0; i < _arr.length; i++)
				{
					if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
					{
							dataValue = Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString()); 
							dynamicSeriesData[i][j]	= dataValue;
							if(k == 0){
								if( maxColumnDataValue < dataValue ){
									maxColumnDataValue = dataValue;
								}
							}else{
								if( maxLineDataValue < dataValue ){
									maxLineDataValue = dataValue;
								}
							}
					}
					else
					{
						dynamicSeriesData[i][j]	= 0;
					}
				}
			}
			
			//chart 속성 데이터 적용
			DefaultCategoryDataset _dataset  = (DefaultCategoryDataset) DatasetUtilities.createCategoryDataset(
					displayName, xFieldNames, dynamicSeriesData
		    );		
			
			this.datasets.add(_dataset);
		}
		
		this.makeChart(null,_yFieldFillColors , _columnAxisName , _lineAxisName,_yBackgroundColor);
		
		
		this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
				_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
				_dataLabelPosition, _DubplicateAllow, _yFieldFillColors ,maxColumnDataValue , maxLineDataValue , _rangeMax,
				_categoryMargin,_itemMargin,_xFieldVisible,_yFieldVisible );
					
		return true;
	}
	
	
	
	/**
	 *  차트에 데이터 프로바이더를 설정 하는 함수
	 */
	@Override
	public boolean setGraphData(ArrayList<ArrayList<HashMap<String, Object>>> _dataACL, String _title,
			String _seriesXFields[], HashMap<Integer, String[]> _yFieldNames, HashMap<Integer, String[]> _yFieldDisplayNames, boolean _crossTab,
			String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
			String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, HashMap<Integer, int[]> _yFieldFillColors, boolean isOverlaid, float _rangeMax) 
	{
		
		//validation check
		if(_dataACL.size() == 0) return false;
		
		Double maxColumnDataValue=0d; 
		Double maxLineDataValue=0d;
		Double dataValue;
		
		
		for(int k=0; k < _dataACL.size(); k++)
		{
			ArrayList<HashMap<String, Object>> _dataAC = _dataACL.get(k);
				
			if( _dataAC == null) return false;

			if(_yFieldNames == null)
			{
				log.error(getClass().getName() + "::" + "Call setGraphData()...yFiledName is null.");
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
						
						
						if( _dataAC.get(_i-1).get(_seriesXFields[_i]).toString() != _dataAC.get(_i).get(_seriesXFields[_i]).toString() 
								&& _dataAC.get(_i-1).get(_yFieldNames.get(k)[0].toString()).toString() != _dataAC.get(_i).get(_yFieldNames.get(k)[0].toString()).toString() )
						{
							_parseAC.add(_dataAC.get(_i));		
						}
					}
				}	
				
				_dataAC = _parseAC;
				
			}
			
			//series 를 만들기 위해서 series 의 YField 를 위한 array 를 만든다.
			String[] _arr = _yFieldNames.get(k);
			
			//series 데이터 생성
			double[][] dynamicSeriesData = new double[_arr.length][_dataAC.size()];
			String[] displayName = new String[_arr.length];
			String[] xFieldNames = new String[_dataAC.size()];
			for(int i = 0; i < _arr.length; i++)
			{
				//display name filed 를 확인 한다.
				if(_yFieldDisplayNames == null)
					displayName[i] = _arr[i];
				else
					displayName[i] = _yFieldDisplayNames.get(k) == null || (_yFieldDisplayNames.get(k).length < (i+1)) ? _arr[i] : _yFieldDisplayNames.get(k)[i];	
			}
			
			HashMap _hXFields = new HashMap();
			String _stFieldKey = "";
			for(int j = 0; j < _dataAC.size(); j++)
			{
				_stFieldKey = _dataAC.get(j).get(_seriesXFields[0]).toString();	// BarRenderer의 CategoryLabel만 표시되도록 하기위해
//				_stFieldKey = _dataAC.get(j).get(_seriesXFields[k]).toString();
				
				if(_hXFields.containsKey(_stFieldKey))
					_stFieldKey = _stFieldKey + j;
				_hXFields.put(_stFieldKey, j);
				
				xFieldNames[j] = _stFieldKey; // 중복안되게해야함!
				
				for(int i = 0; i < _arr.length; i++)
				{
					if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
					{
							dataValue = Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString()); 
							dynamicSeriesData[i][j]	= dataValue;
							if(k == 0){
								if( maxColumnDataValue < dataValue ){
									maxColumnDataValue = dataValue;
								}
							}else{
								if( maxLineDataValue < dataValue ){
									maxLineDataValue = dataValue;
								}
							}
					}
					else
					{
						dynamicSeriesData[i][j]	= 0;
					}
				}
			}
			
			//chart 속성 데이터 적용
			DefaultCategoryDataset _dataset  = (DefaultCategoryDataset) DatasetUtilities.createCategoryDataset(
					displayName, xFieldNames, dynamicSeriesData
		    );		
			
			this.datasets.add(_dataset);
		}
		
		this.makeChart(_title,_yFieldFillColors,"","",0xffffff);
		
		this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
				_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
				_dataLabelPosition, _DubplicateAllow, _yFieldFillColors ,maxColumnDataValue , maxLineDataValue , _rangeMax,0.2,0.2,true,true );
					
		return true;
	}

	
	
	protected void makeChart(String _title , HashMap<Integer, int[]> _yFieldFillColors, String _columnAxisName , String _lineAxisName, int _backgroundColor) 
	{
		if(this.datasets.size() == 0)
			makeDefaultDataSet();
	
			
		// create the first renderer...
		final BarRenderer renderer = new BarRenderer();
		renderer.setItemLabelsVisible(false);
		renderer.setShadowVisible(false);
		        
		final CategoryPlot plot = new CategoryPlot();
        plot.setDataset((CategoryDataset) this.datasets.get(0));
        for(int j=0; j< _yFieldFillColors.get(0).length; j++)
        {
        	 renderer.setSeriesPaint( j , new Color(_yFieldFillColors.get(0)[j]) );
        }
        
        plot.setRenderer(renderer);
        // 첫번째 차트는 bar chart
        plot.setDomainAxis(new CategoryAxis(""));
        plot.setRangeAxis(new NumberAxis(_columnAxisName));
        plot.setOrientation(PlotOrientation.VERTICAL);

        // line 반복
        for( int i=1; i<this.datasets.size(); i++ ){
            LineAndShapeRenderer renderer2 = new LineAndShapeRenderer(); 
            plot.setDataset(i, (CategoryDataset) this.datasets.get(i));
            //renderer2.setSeriesPaint( 0 , new Color(_yFieldFillColors.get(i)[0]) );
            
            DefaultCategoryDataset _datase =(DefaultCategoryDataset) this.datasets.get(i);
            
            List rowKeys= _datase.getRowKeys();
            for( int j=0; j<rowKeys.size(); j++ ){
            	String _key = (String) rowKeys.get(j);
            	renderer2.setSeriesPaint( j , new Color(_yFieldFillColors.get(i)[j]) );
            }
            
            //renderer2.setSeriesPaint( 0 , Color.ORANGE );
            //renderer2.setSeriesPaint( 1 , Color.YELLOW );            
             
            plot.setRenderer(i, renderer2);
            
            if( i==1 ){
            	plot.setRangeAxis(i, new NumberAxis(_lineAxisName));	
            	plot.mapDatasetToRangeAxis(1, 1);
            	//plot.getRangeAxis().setRange(new Range(0,  ));
            } 
        }
        
        // 반복 끝
        
        // change the rendering order so the primary dataset appears "behind" the 
        // other datasets...
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        chart = new JFreeChart(plot);        
        chart.setBackgroundPaint(new Color(_backgroundColor));
 	}
	
	// create a dataset...
	protected void makeDefaultDataSet()
	{
		// create the first dataset...
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(1.0, "S1", "서울");
        dataset1.addValue(4.0, "S1", "대전");
        dataset1.addValue(3.0, "S1", "대구");
        dataset1.addValue(5.0, "S1", "부산");
        dataset1.addValue(5.0, "S1", "광주");
        dataset1.addValue(7.0, "S1", "제주");
        dataset1.addValue(7.0, "S1", "전주");
        dataset1.addValue(8.0, "S1", "춘천");

        dataset1.addValue(5.0, "S2", "서울");
        dataset1.addValue(7.0, "S2", "대전");
        dataset1.addValue(6.0, "S2", "대구");
        dataset1.addValue(8.0, "S2", "부산");
        dataset1.addValue(4.0, "S2", "광주");
        dataset1.addValue(4.0, "S2", "제주");
        dataset1.addValue(2.0, "S2", "전주");
        dataset1.addValue(1.0, "S2", "춘천");

        this.datasets.add(dataset1);
        
        // now create the second dataset and renderer...
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.addValue(9.0, "T1", "서울");
        dataset2.addValue(7.0, "T1", "대전");
        dataset2.addValue(2.0, "T1", "대구");
        dataset2.addValue(6.0, "T1", "부산");
        dataset2.addValue(6.0, "T1", "광주");
        dataset2.addValue(9.0, "T1", "제주");
        dataset2.addValue(5.0, "T1", "전주");
        dataset2.addValue(4.0, "T1", "춘천");

        this.datasets.add(dataset2);
	}
	
	/**
	 *  차트에  카테고리, Legend 등의 속성를 설정 하는 함수
	 */
	protected boolean setGraphProperty(String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, HashMap<Integer,int []> _yFieldFillColors ,
								Double _maxColumnDataValue ,Double _maxLineDataValue , float _rangeMax, double _categoryMargin, double _itemMargin,
								boolean _xFieldVisible,boolean _yFieldVisible)
	{

		CategoryPlot plot = chart.getCategoryPlot();
        //plot.setForegroundAlpha(0.5f);
		((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
        Paint backgroundPaint = new Color(0xFFFFFF); 
        plot.setBackgroundPaint(backgroundPaint);
        
        Stroke lineStroke = new BasicStroke(_gridLineWeight) ;
		// grid line 속성 변환 
		if(_gridLIne == true)
		{
//	        plot.setDomainGridlinesVisible(true);
//	        Paint domainLinePaint = new Color(_gridLIneColor);
//	        plot.setDomainGridlinePaint(domainLinePaint);
//	        plot.setRangeGridlinesVisible(true);
//	        plot.setRangeGridlinePaint(domainLinePaint);
//	        
	        
			//공통속성 				      
			Paint domainLinePaint = new Color(_gridLIneColor);
	        
			//세로
			if(_gridLIneDirection.equals("both") || _gridLIneDirection.equals("vertical")){
				plot.setDomainGridlinesVisible(true);  
		        plot.setDomainGridlinePaint(domainLinePaint);
		        plot.setDomainGridlineStroke(lineStroke);	//그리드 세로 두께 설정
			}
			 //가로
			if(_gridLIneDirection.equals("both") || _gridLIneDirection.equals("horizontal")){				
		        plot.setRangeGridlinesVisible(true);
		        plot.setRangeGridlinePaint(domainLinePaint);	        
		        plot.setRangeGridlineStroke(lineStroke);	//그리드 가로 두께 설정 
			}   
	        
		}
		else
		{
			plot.setDomainGridlinesVisible(false);
			plot.setRangeGridlinesVisible(false);
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
        
		/*
        if( _yFieldFillColors.size() > 0 ){
        	
                if( _yFieldFillColors.get(0).length > 0 ){
                	CategoryItemRenderer ctitemrenderer = plot.getRenderer();
                	for(int i=0; i<_yFieldFillColors.get(0).length; i++ )
                	{
                		ctitemrenderer.setSeriesPaint( 0 , new Color(_yFieldFillColors.get(0)[i]) );
                	}
        		}
        	
        }
        */
		
        // datalabel 위치
        CategoryItemRenderer cir = plot.getRenderer();
        cir.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
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
        
		////////////////////////////////////////////////////////////////
		// 한글 깨짐 해결
		Font font = plot.getDomainAxis().getLabelFont();
		// X축 라벨
		plot.getDomainAxis().setLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
		// X축 도메인
		plot.getDomainAxis().setTickLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
		
		font = plot.getRangeAxis().getLabelFont();
		// Y축 라벨
		plot.getRangeAxis().setLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
		// Y축 범위
		plot.getRangeAxis().setTickLabelFont(new Font("Dotum", Font.PLAIN, font.getSize()));
        
		font = legend.getItemFont();
		// legend 라벨
		legend.setItemFont(new Font("Dotum", Font.PLAIN, font.getSize()));		
		
		if( _rangeMax != 0 ){
			//바 차트 범위 확장
			Double MaxDataValue = _maxColumnDataValue*_rangeMax;
			plot.getRangeAxis(0).setRange(new Range(0, MaxDataValue));
			
			//라인 차트 범위 확장
			MaxDataValue = _maxLineDataValue*_rangeMax;
			plot.getRangeAxis(1).setRange(new Range(0, MaxDataValue));		
		}
		
		
		
		//plot.getRangeAxis().setVisible(false);
		
		// 차트 좌측 visible 
		//plot.getRangeAxis(0).setVisible(false);
		
		// 차트 우측 visible
		//plot.getRangeAxis(1).setVisible(false);
		
		
		if( _xFieldVisible == false ){
			// 차트 하단 data label visible
			plot.getDomainAxis().setVisible(false);
		}
		
		if( _yFieldVisible == false ){
			// 차트 좌측 label data visible
			plot.getRangeAxis().setTickLabelsVisible(false);
			// 차트 우측 label data visible
			plot.getRangeAxis(1).setTickLabelsVisible(false);
		}

		// column chart bar margin   category margin
		//plot.getDomainAxis().getCategoryMargin();	// default 0.2
		plot.getDomainAxis().setCategoryMargin(_categoryMargin);
		
		
		// bar 간격?
        BarRenderer br = (BarRenderer) plot.getRenderer();
        //br.setMaximumBarWidth(1);
        // column 내부  bar 간격.  item margin
        br.setItemMargin(_itemMargin);
        
		
		return true;
	}

	@Override
	public boolean setGraphData(ArrayList<HashMap<String, Object>> _dataAC, String _title, 
			String _seriesXField, String[] _yFieldName, String[] _yFieldDisplayName, boolean _crossTab,
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
	public boolean setGraphData(
			ArrayList<ArrayList<HashMap<String, Object>>> _dataAC,
			String _title, String _seriesXField, String[] _yFieldName,
			String[] _yFieldDisplayName, boolean _crossTab, String _form,
			boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection,
			String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation,
			String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor, boolean isOverlaid) {
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
	protected void makeChart(String _title) {
		// TODO Auto-generated method stub		
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
			String[] _params) {
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
			HashMap<Integer, int[]> _yFieldFillColors, boolean isOverlaid) {
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
