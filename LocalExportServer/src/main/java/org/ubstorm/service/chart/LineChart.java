package org.ubstorm.service.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;

public class LineChart extends BaseChart  {
	
	
	public LineChart(int width, int height)
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
		
		float _seriesXFieldFontSize = 12f;
		float _seriesYFieldFontSize = 12f;
		boolean _rotateCategoryLabel = false;
		
		int _xLineWeight = 0;
		int _yLineWeight = 0;
		int _outLineWeight = 0;
		
		int[] _valueWeight = null;
		
		int _valueDisplayType=0;
		float _rangeMax=0;
		int _noDataType=0;
		
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
		
		
		if( _params.length > 25 ){
			_seriesXFieldFontSize= "".equals(_params[23]) ? null : common.ParseFloatNullChk(_params[23],0);
			_seriesYFieldFontSize= "".equals(_params[24]) ? null : common.ParseFloatNullChk(_params[24],0);
			_rotateCategoryLabel = "".equals(_params[25]) ? false : Boolean.valueOf(_params[25]);
		}
		
		if( _params.length > 28 ){
			_xLineWeight = "".equals(_params[26]) ? 1 : Integer.valueOf(_params[26]);
			_yLineWeight = "".equals(_params[27]) ? 1 : Integer.valueOf(_params[27]);
			_outLineWeight = "".equals(_params[28]) ? 1 : Integer.valueOf(_params[28]);		
		}
		
		String[] _valueWeightStringArray= _params[29].split("~");
		_valueWeight = new int[_valueWeightStringArray.length];
		String _vauleWegihtString;
		for( int _valueWeightIndex=0; _valueWeightIndex<_valueWeightStringArray.length; _valueWeightIndex++ ){
			_vauleWegihtString = _valueWeightStringArray[_valueWeightIndex];
			if( _vauleWegihtString == null || _vauleWegihtString.equals("null") || _vauleWegihtString.equals("") ){
				_valueWeight[_valueWeightIndex] = 1;
			}else{
				_valueWeight[_valueWeightIndex] = Integer.valueOf(_vauleWegihtString);	
			}
		}
		
		if( _params.length > 32 ){
			_valueDisplayType	="".equals(_params[30]) ? 0 : Integer.valueOf(_params[30]);
			_rangeMax			="".equals(_params[31]) ? 0 : common.ParseFloatNullChk(_params[31], 0);
			_noDataType			="".equals(_params[32]) ? 0 : Integer.valueOf(_params[32]);
		}
		
		if( _params.length == 36)
		{
			_xFieldVisible = "".equals(_params[33]) ? true : Boolean.valueOf(_params[33]); 
			_yFieldVisible = "".equals(_params[34]) ? true : Boolean.valueOf(_params[34]); 
			_yBackgroundColor	= "".equals(_params[35]) ? 0xffffff : Integer.decode(_params[35]);
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
				
				// 숫자형의 데이터가 아닌 경우, 처리를 위해서 noData 변수를 column length 크기의 배열로 선언한다.
				Boolean[] _noDataAr = new Boolean[_arr.length];
				
				
				//series 데이터 생성
				double[][] dynamicSeriesData = new double[_arr.length][_dataAC.size()];
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
				
				HashMap _hXFields = new HashMap();
				String _stFieldKey = "";
				
				
				Double maxDataValue=0d;
				Double dataValue;
				
				for(int j = 0; j < _dataAC.size(); j++)
				{
					//xFieldNames[j] = _dataAC.get(j).get(_seriesXField).toString();
					_stFieldKey = _dataAC.get(j).get(_seriesXField).toString();
					if(_hXFields.containsKey(_stFieldKey))
						_stFieldKey = _stFieldKey + j;
					_hXFields.put(_stFieldKey, j);
					
					xFieldNames[j] = _stFieldKey; // 중복안되게해야함!

					for(int i = 0; i < _arr.length; i++)
					{
						// 20160201 데이터의 건수가 숫자형이 아닐경우 0으로 지정
						if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
						{
							dataValue = Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
							dynamicSeriesData[i][j]	= dataValue;
							if( maxDataValue < dataValue ){
								maxDataValue = dataValue;
							}
							_noDataAr[i]=true;
						}
						else
						{
							dynamicSeriesData[i][j]	= 0;
							_noDataAr[i]=false;
						}
					}
				}
				
				// 숫자형 data가 아닐 경우에는 차트에 표현하지 않는다.
				if( _noDataType == -1 ){
					
					// 정상적인 숫자형 data count를 계산한다.
					int _noDataCount=0;
					for( int m=0; m<_noDataAr.length; m++ ){
						if( _noDataAr[m] == true ){
							_noDataCount++;	
						}
					}
					
					// 정상적인 숫자형 data를 담을 배열을 새로 만든다.
					String[] displayName2=new String[_noDataCount]; 

					int _noDataTcount=0;
					for( int p=0; p<_noDataAr.length; p++ ){
						if( _noDataAr[p] == true ){
							displayName2[_noDataTcount]=displayName[p];
							_noDataTcount++;
						}
					}
					
					double[][] dynamicSeriesData2 = new double[_noDataCount][_dataAC.size()];
					_noDataTcount=0;
					for( int p=0; p<_noDataAr.length; p++ ){
						if( _noDataAr[p] == true ){
							dynamicSeriesData2[_noDataTcount]=dynamicSeriesData[p];
							_noDataTcount++;
						}
					}
					
					int [] _yFieldFillColor2 = new int[_noDataCount];
					_noDataTcount=0;
					for( int p=0; p<_noDataAr.length; p++ ){
						if( _noDataAr[p] == true ){
							_yFieldFillColor2[_noDataTcount]=_yFieldFillColor[p];
							_noDataTcount++;
						}
					}
					
					this.dataset = (DefaultCategoryDataset) DatasetUtilities.createCategoryDataset(
							displayName2, xFieldNames, dynamicSeriesData2
							);
					
					
					this.makeChart(null);
					
					chart.setBackgroundPaint(new Color(_yBackgroundColor));
					
					this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
							_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
							_dataLabelPosition, _DubplicateAllow, _yFieldFillColor2, _xLineWeight,
							 _yLineWeight,  _outLineWeight,  _valueWeight,
							 _seriesXFieldFontSize , _seriesYFieldFontSize,_rotateCategoryLabel,
							 _valueDisplayType,maxDataValue,_rangeMax,_xFieldVisible,_yFieldVisible);
					
				}else{
					//chart 속성 데이터 적용		
					this.dataset = (DefaultCategoryDataset) DatasetUtilities.createCategoryDataset(
							displayName, xFieldNames, dynamicSeriesData
				    );
					
					this.makeChart(null);
					
					chart.setBackgroundPaint(new Color(_yBackgroundColor));
					
					this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
							_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
							_dataLabelPosition, _DubplicateAllow, _yFieldFillColor, _xLineWeight,
							 _yLineWeight,  _outLineWeight,  _valueWeight,
							 _seriesXFieldFontSize , _seriesYFieldFontSize,_rotateCategoryLabel,
							 _valueDisplayType,maxDataValue,_rangeMax,_xFieldVisible,_yFieldVisible);
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
			String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor,
			int _xLineWeight,int _yLineWeight,int _outLineWeight,int[] _valueWeight ,
			float _seriesXFieldFontSize , float _seriesYFieldFontSize, boolean _rotateCategoryLabel,
			int _valueDisplayType , float _rangeMax, int _noDataType )
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
		
		// 숫자형의 데이터가 아닌 경우, 처리를 위해서 noData 변수를 column length 크기의 배열로 선언한다.
		Boolean[] _noDataAr = new Boolean[_arr.length];
		
		
		//series 데이터 생성
		double[][] dynamicSeriesData = new double[_arr.length][_dataAC.size()];
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
		
		HashMap _hXFields = new HashMap();
		String _stFieldKey = "";
		
		
		Double maxDataValue=0d;
		Double dataValue;
		
		for(int j = 0; j < _dataAC.size(); j++)
		{
			//xFieldNames[j] = _dataAC.get(j).get(_seriesXField).toString();
			_stFieldKey = _dataAC.get(j).get(_seriesXField).toString();
			if(_hXFields.containsKey(_stFieldKey))
				_stFieldKey = _stFieldKey + j;
			_hXFields.put(_stFieldKey, j);
			
			xFieldNames[j] = _stFieldKey; // 중복안되게해야함!

			for(int i = 0; i < _arr.length; i++)
			{
				// 20160201 데이터의 건수가 숫자형이 아닐경우 0으로 지정
				if( StringUtil.isDouble(_dataAC.get(j).get(_arr[i].toString()).toString()) )
				{
					dataValue = Double.valueOf( _dataAC.get(j).get(_arr[i].toString()).toString());
					dynamicSeriesData[i][j]	= dataValue;
					if( maxDataValue < dataValue ){
						maxDataValue = dataValue;
					}
					_noDataAr[i]=true;
				}
				else
				{
					dynamicSeriesData[i][j]	= 0;
					_noDataAr[i]=false;
				}
			}
		}
		
		// 숫자형 data가 아닐 경우에는 차트에 표현하지 않는다.
		if( _noDataType == -1 ){
			
			// 정상적인 숫자형 data count를 계산한다.
			int _noDataCount=0;
			for( int m=0; m<_noDataAr.length; m++ ){
				if( _noDataAr[m] == true ){
					_noDataCount++;	
				}
			}
			
			// 정상적인 숫자형 data를 담을 배열을 새로 만든다.
			String[] displayName2=new String[_noDataCount]; 

			int _noDataTcount=0;
			for( int p=0; p<_noDataAr.length; p++ ){
				if( _noDataAr[p] == true ){
					displayName2[_noDataTcount]=displayName[p];
					_noDataTcount++;
				}
			}
			
			double[][] dynamicSeriesData2 = new double[_noDataCount][_dataAC.size()];
			_noDataTcount=0;
			for( int p=0; p<_noDataAr.length; p++ ){
				if( _noDataAr[p] == true ){
					dynamicSeriesData2[_noDataTcount]=dynamicSeriesData[p];
					_noDataTcount++;
				}
			}
			
			int [] _yFieldFillColor2 = new int[_noDataCount];
			_noDataTcount=0;
			for( int p=0; p<_noDataAr.length; p++ ){
				if( _noDataAr[p] == true ){
					_yFieldFillColor2[_noDataTcount]=_yFieldFillColor[p];
					_noDataTcount++;
				}
			}
			
			this.dataset = (DefaultCategoryDataset) DatasetUtilities.createCategoryDataset(
					displayName2, xFieldNames, dynamicSeriesData2
					);
			
			
			this.makeChart(_title);
			
			this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
					_dataLabelPosition, _DubplicateAllow, _yFieldFillColor2, _xLineWeight,
					 _yLineWeight,  _outLineWeight,  _valueWeight,
					 _seriesXFieldFontSize , _seriesYFieldFontSize,_rotateCategoryLabel,
					 _valueDisplayType,maxDataValue,_rangeMax,true,true);
			
		}else{
			//chart 속성 데이터 적용		
			this.dataset = (DefaultCategoryDataset) DatasetUtilities.createCategoryDataset(
					displayName, xFieldNames, dynamicSeriesData
		    );
			
			this.makeChart(_title);
			
			this.setGraphProperty(_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, _legendLocation, 
					_dataLabelPosition, _DubplicateAllow, _yFieldFillColor, _xLineWeight,
					 _yLineWeight,  _outLineWeight,  _valueWeight,
					 _seriesXFieldFontSize , _seriesYFieldFontSize,_rotateCategoryLabel,
					 _valueDisplayType,maxDataValue,_rangeMax,true,true);
		}
					
		return true;
	}
	
	protected void makeChart(String _title) 
	{
		if(dataset == null)
			makeDefaultDataSet();
	
		chart = ChartFactory.createLineChart(
				 null, // chart title
		         null,
		         null,
		         (CategoryDataset) dataset, // data
		         PlotOrientation.VERTICAL,           
		         true, // include legend
		         true,
		         false);
		
	}
	
	protected void makeDefaultDataSet()
	{
		dataset = new DefaultCategoryDataset( );
		((DefaultCategoryDataset) dataset).addValue( 15 , "schools" , "1970" );
		((DefaultCategoryDataset) dataset).addValue( 30 , "schools" , "1980" );
		((DefaultCategoryDataset) dataset).addValue( 60 , "schools" ,  "1990" );
		((DefaultCategoryDataset) dataset).addValue( 120 , "schools" , "2000" );
		((DefaultCategoryDataset) dataset).addValue( 240 , "schools" , "2010" );
		((DefaultCategoryDataset) dataset).addValue( 300 , "schools" , "2014" );
	}
	
	/**
	 *  차트에  카테고리, Legend 등의 속성를 설정 하는 함수
	 */
	protected boolean setGraphProperty(String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection, int _gridLIneColor,  
								String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight, int _legendMarkWeight,
								String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow, int [] _yFieldFillColor,int _xLineWeight,
								int _yLineWeight, int _outLineWeight, int[] _valueWeight,
								float _seriesXFieldFontSize , float _seriesYFieldFontSize , boolean _rotateCategoryLabel,
								int _valueDisplayType,Double maxDataValue,float _rangeMax,boolean _xFieldVisible,boolean _yFieldVisible)
	{
		CategoryPlot plot = chart.getCategoryPlot();
        //plot.setForegroundAlpha(0.5f);
        Paint backgroundPaint = new Color(0xFFFFFF); 
        plot.setBackgroundPaint(backgroundPaint);

        Stroke lineStroke = new BasicStroke(_gridLineWeight) ;
		// grid line 속성 변환 
		if(_gridLIne == true)
		{
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
		
		if(_outLineWeight == 0){
			plot.setOutlineVisible(false);
		}else if(_outLineWeight > 0)
		{
			lineStroke = new BasicStroke(_outLineWeight) ;
			plot.setOutlineVisible(true);
			plot.setOutlineStroke(lineStroke);	//outline 가로 두께 설정 
		}

		//plot.setAxisOffset(RectangleInsets.ZERO_INSETS);// outline과 x,y축 간격 설정	
		
		
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
        	CategoryItemRenderer ctitemrenderer = plot.getRenderer();
        	for(int i=0; i<_yFieldFillColor.length; i++ )
        	{        		
        		ctitemrenderer.setSeriesPaint( i , new Color(_yFieldFillColor[i]) );   
        		//value값 두께설정
        		ctitemrenderer.setSeriesStroke(i, new BasicStroke(_valueWeight[i]));
        	}
		}
        
        // datalabel 위치
        CategoryItemRenderer cir = plot.getRenderer();
        cir.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        if("inside".equals(_dataLabelPosition))
		{
        	 cir.setBaseItemLabelsVisible(true);
		}
		else if("outside".equals(_dataLabelPosition))
		{
       	 	cir.setBaseItemLabelsVisible(true);
		}
		else if("callOut".equals(_dataLabelPosition) || "insideWithCallOut".equals(_dataLabelPosition))
		{
       	 	cir.setBaseItemLabelsVisible(true);
		}
		else if("none".equals(_dataLabelPosition))
		{
       	 	cir.setBaseItemLabelGenerator(null);
		}
		else
		{
       	 	cir.setBaseItemLabelGenerator(null);
		}
        
        
        //20190225
		if( _xFieldVisible == false ){
			// 차트 하단 data label visible
			plot.getDomainAxis().setVisible(false);
		}
		
		if( _yFieldVisible == false ){
			// 차트 좌측 label data visible
			plot.getRangeAxis().setTickLabelsVisible(false);
		}
		
        
		////////////////////////////////////////////////////////////////
		// 한글 깨짐 해결
        CategoryAxis domAxis = plot.getDomainAxis();
        
        
        if( _rotateCategoryLabel ){
            // 라벨을 45도 회전시켜서 보여줌
            domAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        
        
        // 라벨을 45도 회전시켜서 보여줌
        // domAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		Font font = domAxis.getLabelFont();
		
		
		int xFieldFontSize = (int) _seriesXFieldFontSize;
		int yFieldFontSize = (int) _seriesYFieldFontSize;

		
		
		// X축 라벨
		domAxis.setLabelFont(new Font("Dotum", Font.PLAIN, xFieldFontSize));
		// X축 도메인
		domAxis.setTickLabelFont(new Font("Dotum", Font.PLAIN, xFieldFontSize));		
		// X축 두께 설정
		domAxis.setAxisLineStroke(new BasicStroke(_xLineWeight));
		
		
		ValueAxis rangeAxis = plot.getRangeAxis();
		font = rangeAxis.getLabelFont();
		// Y축 라벨
		rangeAxis.setLabelFont(new Font("Dotum", Font.PLAIN, yFieldFontSize));
		// Y축 범위
		rangeAxis.setTickLabelFont(new Font("Dotum", Font.PLAIN, yFieldFontSize));
		// Y축 두께 설정
		rangeAxis.setAxisLineStroke(new BasicStroke(_yLineWeight));
		
		font = legend.getItemFont();
		// legend 라벨
		legend.setItemFont(new Font("Dotum", Font.PLAIN, font.getSize()));		
		
		
		
		// {label:"value",value:0},{label:"shape",value:1},{label:"shape with value",value:2},{label:"none",value:3}
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		if( _valueDisplayType == 1 ){
			//Enable shapes on the line chart
			renderer.setBaseItemLabelsVisible(false);
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.yellow);
		}else if( _valueDisplayType == 2 ){
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.yellow);
		}else if( _valueDisplayType == 3 ){
			renderer.setShapesVisible(false);
			renderer.setBaseItemLabelsVisible(false);
		}
		
		
		
		
		
		
		//XYPlot plot = chart.getXYPlot();
		//NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
		//rangeAxis.setAxisRange(0.0, 100.0);
		
		
		
		if( maxDataValue > 1000 ){
			// 20180214 억 단위 표현 안되는 문제.
			final long MILLION = 1000000L;			//백만.
		    final long BILLION = 1000000000L;		//십억.
		    final long TRILLION = 1000000000000L;	//일조.
		    final long THOUSAND = 1000L;			//일천.
		    
			NumberAxis nrangeAxis = (NumberAxis) plot.getRangeAxis();
			nrangeAxis.setNumberFormatOverride(new NumberFormat() {

		        @Override
		        public Number parse(String source, ParsePosition parsePosition) {
		            return null;
		        }

		        @Override
		        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {

		            String temp =  number < THOUSAND ? String.valueOf(number) :
		                number < MILLION ?  (int)(number / THOUSAND) + " K" :
		                number < BILLION ?  (int)(number / MILLION) + " M" :
		                number < TRILLION ? (int)(number / BILLION) + " B" : 
		                (int)(number / TRILLION) + " T";
		            return new StringBuffer(temp);
		        }


				@Override
				public StringBuffer format(double number, StringBuffer arg1, FieldPosition arg2) {
					
					
		            String temp =  number < THOUSAND ? String.valueOf(number) :
		                number < MILLION ?  (int)(number / THOUSAND) + " K" :
		                number < BILLION ?  (int)(number / MILLION) + " M" :
		                number < TRILLION ? (int)(number / BILLION) + " B" : 
		                (int)(number / TRILLION) + " T";

		            
		           
		            
		            // 값을 그대로 뽑을 수도 있다.
		            long _longValue = (new Double(number)).longValue();
		            //String _longValueStr = Long.toString(_longValue);
		            
		            //20181219  천단위 콤마 처리
		            String _longValueStr = String.format("%,d", _longValue);
		            
		            return new StringBuffer(_longValueStr);
		            
		           // return new StringBuffer(temp);
				}

		    });
			
		}
		

		
		
		
		
		
		
		if( _rangeMax != 0 ){
			Double MaxDataValue = maxDataValue*_rangeMax;
			rangeAxis.setRange(new Range(0, MaxDataValue));
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
			int[] _yFieldFillColor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean setGraphProperty(String _form, boolean _gridLIne, int _gridLineWeight, String _gridLIneDirection,
			int _gridLIneColor, String _legendDirection, String _legendLabelPlacement, int _legendMarkHeight,
			int _legendMarkWeight, String _legendLocation, String _dataLabelPosition, boolean _DubplicateAllow,
			int[] _yFieldFillColor) {
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
			boolean _rotateCategoryLabel) {
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
