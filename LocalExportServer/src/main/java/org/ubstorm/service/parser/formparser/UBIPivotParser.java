package org.ubstorm.service.parser.formparser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UBIPivotParser {

	public UBIPivotParser() {
		super();
	}

	public final String COLUMNSUMMERY_TYPE_SUM 		= "sum";
	public final String COLUMNSUMMERY_TYPE_AVG 		= "avg";
	public final String COLUMNSUMMERY_TYPE_DATAAVG 	= "dataAvg";
	
	
	public HashMap<String, Object> convert( ArrayList<HashMap<String, Object>> _data, ArrayList<String> colAr, ArrayList<String> rowAr, ArrayList<String> valueAr
			, String valueFunctionStr, Boolean totalUse, ArrayList<String> sortColumn, ArrayList<String> descending
			, ArrayList<String> leftFixedAr, ArrayList<String> rightFixedAr, List<Object> summaryColumn, ArrayList<String> sortColumn2, ArrayList<String> descanding2
			,ArrayList<String> sortNumeric, ArrayList<String> sortNumeirc2 )
	{
		
		int _maxDivideCnt = 10;
		int _defaultDecimalRound = BigDecimal.ROUND_DOWN; 
//		if( _data.size() == 0 ) return; 
//		valueFunctionStr = "AVG";
		
		HashMap<String, Object> retHashMap = new HashMap<String, Object>();
		
//		ArrayList<String> headerList 	= new ArrayList<String>();
//		ArrayList<String> dataList 		= new ArrayList<String>();
		
		HashMap<String, ArrayList<String>> dataMap 			= new HashMap<String, ArrayList<String>>();
//		HashMap<String, ArrayList<String>> summeryMap 		= new HashMap<String, ArrayList<String>>();
//		HashMap<String, ArrayList<String>> grandtotalMap 	= new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> headerMap 		= new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> _newHeaderMap = new HashMap<String, ArrayList<String>>();
		 
//		String colData 			= "";
//		String colBeforeData 	= "";
		Boolean useRowNum = false;
		
		int i = 0;
		int j = 0;
		int k = 0;
//		int li = 0;
		int l = 0;
		int m = 0;

		int colLength = 0;
		int rowLength = 0;
//		int dataLength = 0;
		int leftFixedLength = 0;
		int rightFixedLength = 0;
		
		if( colAr != null ) colLength = colAr.size();
		if( rowAr != null ) rowLength = rowAr.size();
//		if( valueAr != null ) dataLength = valueAr.size();
		if( leftFixedAr != null ) leftFixedLength = leftFixedAr.size();
		if( rightFixedAr != null ) rightFixedLength = rightFixedAr.size();
		
		
		// RowNum사용시 
		if (rowAr.size() > 0 && rowAr.get(0).equals("$No")) {
			rowLength = rowLength -1;
			rowAr.remove(0);
			useRowNum = true;
		}
		
		//sort 데이터
		ArrayList<HashMap<String, Object>> _retArr = sortDataSet(_data, colAr, new ArrayList<String>(),  new ArrayList<String>() );
		
		if( _retArr.size() < 1 ) return retHashMap;
 		
		/***	Data 영역을 만든다. _row만큼 grouping하여 group title만 가져온다.	***/
		HashMap<String, ArrayList<HashMap<String, Object>>> retData = groupArrayCollection(_retArr, colAr);
		
		if(retData.containsKey("0") == false && _retArr.size() == 1)
		{
			retData.put("0", _retArr);
		}
		
		ArrayList<ArrayList<String>> dArray = new ArrayList<ArrayList<String>>();
		HashMap<String, Object> tempObj;
		ArrayList<HashMap<String, Object>> tempAC;
		ArrayList<HashMap<String, Object>> rowGroupAr;
		
		int grpIndex = 0;
		
		//Column Sort
		if( sortColumn2 != null && sortColumn2.size() > 0 ){
			
			int grpLength = retData.size();
			rowGroupAr = new ArrayList<HashMap<String,Object>>();
			
			for ( i = 0; i < grpLength; i++) {
				tempObj = new HashMap<String, Object>();
				tempAC = sortDataSet(retData.get(String.valueOf(i)), sortColumn2, descanding2, sortNumeirc2 );
				
				for ( j = 0; j < sortColumn2.size(); j++) {
					tempObj.put(sortColumn2.get(j), tempAC.get(0).get(sortColumn2.get(j)) );
				}
				tempObj.put("AC", tempAC);
				rowGroupAr.add(tempObj);
			}
			
			rowGroupAr = sortDataSet(rowGroupAr, sortColumn2, descanding2, sortNumeirc2);
			retData = new HashMap<String, ArrayList<HashMap<String,Object>>>();
			for ( i = 0; i < rowGroupAr.size(); i++) {
				retData.put(String.valueOf(i), (ArrayList<HashMap<String,Object>>) rowGroupAr.get(i).get("AC"));
			}
			
		}
		
		
		int realHeaderDataLength = 0;
		ArrayList<String> headerArray = new ArrayList<String>();
		for ( i = 0; i < colLength; i++) {
			
			String columnName = colAr.get(i);
			realHeaderDataLength = 0;
			headerArray = new ArrayList<String>();
			
			for ( j = 0; j < rowLength; j++) {
				headerArray.add("&Title" + String.valueOf(j));
			}

			for ( j = 0; j < leftFixedLength; j++) {
				headerArray.add("&LFixed" + String.valueOf(j));
			}
			
			for ( j = 0; j < retData.size(); j++) {
				for ( k = 0; k < valueAr.size(); k++) {
					headerArray.add( String.valueOf( retData.get(String.valueOf(j)).get(0).get(columnName) ) );
					realHeaderDataLength++;
				}
				
			}
			
			for ( j = 0; j < rightFixedLength; j++) {
				headerArray.add("&RFixed" + String.valueOf(j));
			}
			
			headerArray.add("&Total");
			
			headerMap.put(String.valueOf(i), headerArray);
		}
		
		
		/***	DATA영역을 생성 _row만큼 grouping하여 groupTitle만 가져온다.	***/
		/***	Data 영역을 만든다. _row만큼 grouping하여 group title만 가져온다.	***/
//		HashMap<String, ArrayList<HashMap<String, Object>>> retValueData = groupArrayCollection(_retArr, rowAr);
		retData = groupArrayCollection2(_retArr, rowAr, null, descending, leftFixedAr, rightFixedAr );
		
		if( retData.size() == 0 && _retArr.size() == 1 )
		{
			retData.put("0", _retArr);
		}
		
		// ColumnSort에 Column값을 지정이 되엇을경우를 처리
		if( (sortColumn2 == null || sortColumn2.size() == 0) && sortColumn != null && sortColumn.size() > 0 )
		{
			sortColumn2 = sortColumn;
			descanding2 = descending;
			sortNumeirc2 = sortNumeric;
		}
		else if( sortColumn != null && sortColumn.size() > 0 )
		{
			for (int n = 0; n < sortColumn2.size(); n++) {
				if( rowAr.indexOf( sortColumn2.get(n) ) != -1 && sortColumn.indexOf( sortColumn2.get(n) ) == -1 )
				{
					sortColumn.add(sortColumn2.get(n));
					descending.add(descanding2.get(n));
					sortNumeric.add(sortNumeirc2.get(n));
				}
			}
			
			sortColumn2 = sortColumn;
			descanding2 = descending;
			sortNumeirc2 = sortNumeric;
		}
		
		if(sortColumn2 != null && sortColumn2.size() > 0 )
		{
			rowGroupAr = new ArrayList<HashMap<String,Object>>();
			
			for ( grpIndex = 0; grpIndex < retData.size(); grpIndex++) {
				
				tempObj = new HashMap<String, Object>();
				tempAC = sortDataSet(retData.get(String.valueOf(grpIndex)), sortColumn2, descanding2, sortNumeirc2 );
				
				for ( j = 0; j < sortColumn2.size(); j++) {
					tempObj.put(sortColumn2.get(j), tempAC.get(0).get(sortColumn2.get(j)) );
				}
				
				tempObj.put("AC", tempAC);
				rowGroupAr.add(tempObj);
			}
			
			
			rowGroupAr = sortDataSet(rowGroupAr, sortColumn2, descanding2, sortNumeirc2);
			retData = new HashMap<String, ArrayList<HashMap<String,Object>>>();
			for ( i = 0; i < rowGroupAr.size(); i++) {
				retData.put(String.valueOf(i), (ArrayList<HashMap<String,Object>>) rowGroupAr.get(i).get("AC"));
			}
			
		}
		
		int _loop = 0;
		
		for ( i = 0; i < retData.size(); i++) {
			
			retData.get(String.valueOf(i));
			ArrayList<String> dataArray = new ArrayList<String>();
//			long _totalValue=0;
//			long _valueTot = 0;
			BigDecimal _totalValue= new BigDecimal(0);
			BigDecimal _valueTot = new BigDecimal(0);
			String _valueTxt = "";
			
			
			int _realRow = 0;
			for ( j = 0; j < rowLength; j++) {
				dataArray.add( String.valueOf( retData.get(String.valueOf(i)).get(0).get( rowAr.get(j) ) ) );
				_realRow++;
			}			
			
			for ( j = 0; j < leftFixedLength; j++) {
				dataArray.add( String.valueOf( retData.get(String.valueOf(i)).get(0).get( leftFixedAr.get(j) ) ) );
			}
			
			int _valueNumber = 0;
			for ( j = rowLength + leftFixedLength; j < realHeaderDataLength + rowLength + leftFixedLength; j++) {
				
				ArrayList<HashMap<String, Object>> _calAC = new ArrayList<HashMap<String, Object>>();
				_calAC = _retArr;
				
				for(k=0; k < colLength; k++ ){
					
					String colNy = colAr.get(k);
					ArrayList<String> tmpA1 = headerMap.get(String.valueOf(k));
					String _colData1 = tmpA1.get(j);
					
					_calAC = calData( _calAC, colNy, _colData1 );
				}
				
				for(k=0; k < _realRow; k++ ) {
					
					if( rowAr.get(0).equals("$No") && i == 0 ) continue;
					
					_calAC = calData( _calAC, rowAr.get(k), dataArray.get(k) );
					
				}
				
				_valueTot = new BigDecimal(0);
				_valueTxt = "";
				
				if( _valueNumber == realHeaderDataLength )
				{
					_valueNumber = 0;
				}
				else if(  _valueNumber == valueAr.size() )
				{
					_valueNumber = 0;
				}
				
				for (HashMap<String, Object> _hashMap : _calAC) {
					String _hashMapValue = String.valueOf( _hashMap.get(valueAr.get(_valueNumber) ) ); 
					if( valueFunctionStr.equals("TEXT"))
					{
						_valueTxt = _hashMapValue;
					}
					else
					{
						if(_hashMapValue.equals("")) _hashMapValue = "0";
//						if( isNumber(_hashMapValue) )	_valueTot = _valueTot + Long.valueOf( _hashMapValue );
						if( isNumber(_hashMapValue) )	_valueTot = _valueTot.add( new BigDecimal(_hashMapValue) );
					}
					
				}
				_valueNumber++;
				
				
				
				if( valueFunctionStr.equals("TEXT") )
				{
					if( _valueTxt != null ) dataArray.add(_valueTxt);
					else  dataArray.add("");
					
				}
				else if( valueFunctionStr.equals("AVG") )
				{
//					dataArray.add( String.valueOf( _valueTot.divide( new BigDecimal(_calAC.size()), _maxDivideCnt, _defaultDecimalRound ) ) );
					dataArray.add( String.valueOf( bigIntegerDivide( _valueTot,  new BigDecimal(_calAC.size() ) )) );
				}
				else
				{
					dataArray.add( String.valueOf(_valueTot) );
				}
				
				
				try {
					if( valueFunctionStr.equals("TEXT") == false )
					{
						if( valueFunctionStr.equals("AVG") )
						{
//							_totalValue = _totalValue.add( _valueTot.divide( new BigDecimal(_calAC.size()), _maxDivideCnt, _defaultDecimalRound ) );
							_totalValue = _totalValue.add( bigIntegerDivide( _valueTot, new BigDecimal(_calAC.size()) ) );
						}
						else
						{
							_totalValue = _totalValue.add( _valueTot );
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					
				}
				
			}
			
			
			for ( j = 0; j < rightFixedLength; j++) {
				dataArray.add( String.valueOf(  retData.get(String.valueOf(i)).get(0).get( rightFixedAr.get(j) )  )  );
			}
			
			if( valueFunctionStr.equals("TEXT"))
			{
				dataArray.add(_valueTxt);
			}
			else if( valueFunctionStr.equals("AVG") )
			{
//				dataArray.add( String.valueOf(_totalValue.divide( new BigDecimal(realHeaderDataLength), _maxDivideCnt, _defaultDecimalRound )  ) );
				dataArray.add( String.valueOf(  bigIntegerDivide(    _totalValue,  new BigDecimal(realHeaderDataLength) )  ) );
			}
			else
			{
				dataArray.add( String.valueOf(_totalValue) );
			}
			
			
			dataMap.put(String.valueOf( _loop ) , dataArray );
			
			_loop++;
		}
		
		
		/***	Grand Total 값을 입력   	***/
		
		ArrayList<String> _gtSum = new ArrayList<String>();
//		ArrayList<String> _gtHorizontalSum = new ArrayList<String>();
		int _currentColumnCnt = 0;
		ArrayList<String> ar;
//		int _summaryColumnIndex = 0;
		int _max = 0;
//		int _colP = 0;
		int _ck = 0;
		ArrayList<ArrayList<BigDecimal>> summaryDataAr = new ArrayList<ArrayList<BigDecimal>>();
		HashMap<String, String> sumData;
		HashMap<String, ArrayList<String>>   _grandTotalObj = new HashMap<String, ArrayList<String>>();
//		HashMap<String, ArrayList<String>>   _grandTotalVerticalObj = new HashMap<String, ArrayList<String>>();
		
		if( summaryColumn != null && summaryColumn.size() > 0 ){
			_max = summaryColumn.size();
			for ( i = 0; i < _max; i++) {
				if( summaryDataAr.size() <= i ) summaryDataAr.add(new ArrayList<BigDecimal>());
			}
		}
		
		for ( i = 0; i < _loop; i++) {
			
			ar = dataMap.get( String.valueOf(i));
			_currentColumnCnt = 0;
			
			for (ArrayList<BigDecimal> _sumAr : summaryDataAr) {
				_sumAr.add( new BigDecimal(0) );
			}
			
//			if( _gtSum.size() <= i) _gtSum.add("");
			
			for ( j = 0; j < ar.size(); j++) {
				
				if( substringUBF(headerArray.get(j), 0, 6 ).equals("&Title") && j < 1 )
				{
					if(_gtSum.size() < j+1 ) _gtSum.add(j, "&Grand Total");
					else  _gtSum.set(j, "&Grand Total");
				}
				else if( substringUBF( headerArray.get(j),0, 6 ).equals("&Title") && j > 0 )
				{
					if(_gtSum.size() < j+1 ) _gtSum.add(j, "&Empty");
					else _gtSum.set(j, "&Empty");
				}
				else
				{
					if( ar.get(j) != null && isNumber(ar.get(j) )  )
					{	
//						else if( Float.isNaN( Float.valueOf( _gtSum.get(j))  ) == false ) _gtSum.set(j,  String.valueOf( Float.valueOf( _gtSum.get(j)) + Float.valueOf(ar.get(j)) ) );
						if(_gtSum.size() < j+1 )
						{
							_gtSum.add(j, ar.get(j));
						}
						else if( isNumber(_gtSum.get(j) ) )
						{
							_gtSum.set(j,  String.valueOf( UBIPivotParser.convertBigDecimal( _gtSum.get(j) ).add( UBIPivotParser.convertBigDecimal(ar.get(j))  )  ) );
						}
						else
						{
							_gtSum.set(j, "" );
						}
					}
					else
					{
						if(_gtSum.size() < j+1 )_gtSum.add(j, "");
						else _gtSum.set(j, "" );
					}
					
				}
				
				if( substringUBF( headerArray.get(j),0, 6).equals("&Total") == false && substringUBF(headerArray.get(j),0, 7).equals("&LFixed") == false 
						 && substringUBF(headerArray.get(j),0, 7).equals("&RFixed") == false && substringUBF(headerArray.get(j),0, 6).equals("&Title") == false
						 && substringUBF(headerArray.get(j),0, 6).equals("&Empty") == false && substringUBF(headerArray.get(j),0, 6).equals("$No") == false )
				{
					
					if( summaryColumn != null && summaryColumn.size() > 0 )
					{
//						_colP = _currentColumnCnt%valueAr.size();
						_max = summaryColumn.size();
						
						for ( k = 0; k < _max; k++) {
							sumData = (HashMap<String, String>) summaryColumn.get(k);
//							_summaryColumnIndex = valueAr.indexOf( sumData.get("column") );
							_ck = colAr.indexOf( sumData.get("column") );
							
							BigDecimal _num = UBIPivotParser.convertBigDecimal(ar.get(j));
							
							if( _ck  != -1  )
							{
								
								if( sumData.get("operation").equals("=") && headerMap.get(String.valueOf(_ck)).get(j).equals(sumData.get("targetStr") ) )
								{
									summaryDataAr.get(k).set(i, ( summaryDataAr.get(k).get(i).add(_num  ) )  );
								}
								else if( sumData.get("operation").equals("!=") && headerMap.get(String.valueOf(_ck)).get(j).equals(sumData.get("targetStr")) == false  )
								{
									summaryDataAr.get(k).set(i, (summaryDataAr.get(k).get(i).add( _num  )  )  );
								}
							}
							else
							{
								summaryDataAr.get(k).set(i, (  summaryDataAr.get(k).get(i).add( _num  )  )  );
							}
							
						}	
						
						
					}
					
					_currentColumnCnt++;
					
				}
				// 컬럼별 합계 구하기 완료
				
			}// 로우별 합계완료
			
			
			/**	컬럼별 합계가 존재할경우 각 값이 avg인지 sum인지 dataAvg인지 체크하여 평균이나 합계를 처리	**/
			if( summaryColumn != null && summaryColumn.size() > 0 && summaryDataAr.size() > 0 )
			{
				
				_max = summaryColumn.size();
				for ( k = 0; k < _max; k++) {
//					String _mathStr = "none";
					sumData = (HashMap<String, String>) summaryColumn.get(k);
//					if( sumData.containsKey("math") )
//					{
//						_mathStr = sumData.get("math");
//					}
					
					if( sumData.containsKey("type") )
					{
						
						if( sumData.get("type").equals( COLUMNSUMMERY_TYPE_SUM ) )
						{
							summaryDataAr.get(k).set(i, getMathData(summaryDataAr.get(k).get(i), sumData.get("type")) );
						}
						else if( sumData.get("type").equals( COLUMNSUMMERY_TYPE_AVG ) )
						{
//							summaryDataAr.get(k).set(i, getMathData(summaryDataAr.get(k).get(i).divide( new BigDecimal( (_currentColumnCnt/valueAr.size() ) ), _maxDivideCnt, _defaultDecimalRound ) , sumData.get("type")) );
							summaryDataAr.get(k).set(i, getMathData(    bigIntegerDivide( summaryDataAr.get(k).get(i), new BigDecimal( (_currentColumnCnt/valueAr.size() ) ) ) , sumData.get("type")) );
						}
						else if( sumData.get("type").equals( COLUMNSUMMERY_TYPE_DATAAVG ) )
						{
//							summaryDataAr.get(k).set(i, getMathData(  summaryDataAr.get(k).get(i).divide( new BigDecimal( getDataAvgCount( retData.get(String.valueOf(i) ) , colAr, sumData)), _maxDivideCnt, _defaultDecimalRound  ), sumData.get("type")) );
							summaryDataAr.get(k).set(i, getMathData( bigIntegerDivide( summaryDataAr.get(k).get(i), new BigDecimal( getDataAvgCount( retData.get(String.valueOf(i) ) , colAr, sumData))  ), sumData.get("type")) );
						}
						
						
					}
					
					
				}			
				
			}
			//컬럼별 합계 값 처리 
			
			
			
		}
		
		if( valueFunctionStr.equals("AVG") )
		{
			for (int n = rowLength; n < _gtSum.size(); n++) {
				String _totNum = _gtSum.get(n);
				try {
//					_totNum = String.valueOf( Double.valueOf(_gtSum.get(n))/_loop );
//					_totNum = String.valueOf( new BigDecimal(_gtSum.get(n) ).divide( new BigDecimal(_loop) , _maxDivideCnt, _defaultDecimalRound  ) );
					_totNum = String.valueOf(  bigIntegerDivide( UBIPivotParser.convertBigDecimal(_gtSum.get(n) ), new BigDecimal(_loop) ) );
				} catch (Exception e) {
					// TODO: handle exception
				}
				_gtSum.set(n, _totNum );
			}
		}
		
		 _grandTotalObj.put("0", _gtSum);
//		 _grandTotalVerticalObj.put("0", _gtSum);
		
		 
		 /** header 를 분석하여 column 별 summery 를 계산 하여 cell 을 생성 한다. **/
		 if( colLength > 0 )
		 {
			 ArrayList<String> _tA1; 
			 String _beforeCol = "";
			 int _spliceCnt = 0;
			 int _spliceCnt1 = 0;
			 int _firstFlag = 0;
			 ArrayList<String> _newHeaderArray;
			 ArrayList<String> _tA;
			 
			 for ( i = 0; i < colLength - 1; i++) {
				 _newHeaderArray = new ArrayList<String>();
				 _tA = headerMap.get(String.valueOf(i));
				 _firstFlag = 0;
				 _spliceCnt1 = _spliceCnt = 0;
				 
				 for ( j = 0; j < _tA.size(); j++) {
					
					 if( substringUBF(_tA.get(j),0, 6).equals("&Title") ||  substringUBF(_tA.get(j),0, 5).equals("&Sum_"))
					 {
						 _newHeaderArray.add( _tA.get(j) );
						 _beforeCol = _tA.get(j);
						 continue;
					 }
					 
					 if( substringUBF(_beforeCol,0, 6).equals("&Empty") )
					 {
						 _newHeaderArray.add( _tA.get(j) );
						 _beforeCol = _tA.get(j);
						 continue;
					 }
					 
					 if( substringUBF(_beforeCol,0, 7).equals("&LFixed") )
					 {
						 _newHeaderArray.add( _tA.get(j) );
						 _beforeCol = _tA.get(j);
						 continue;
					 }
					 
					 if( substringUBF(_beforeCol,0, 7).equals("&RFixed") )
					 {
						 _newHeaderArray.add( _tA.get(j) );
						 _beforeCol = _tA.get(j);
						 continue;
					 }
					 
					 if(_beforeCol.equals(_tA.get(j)) == false )
					 {
						 
						 if( _firstFlag == 0 )
						 {
							 _firstFlag++;
							 _newHeaderArray.add( _tA.get(j) );
							 _beforeCol = _tA.get(j);
							 continue;
						 }
						 
						 _newHeaderArray.add("&Sum_" + _tA.get(j-1));
						 for(k = i+1; k < colLength; k++)
						 {
							 _tA1 = headerMap.get(String.valueOf(k));
							 _tA1.add(j+ _spliceCnt, "&Empty");
							 
							 headerMap.put(String.valueOf(k) , _tA1);
						 }
						 
						 _spliceCnt++;
						 
						 // 현재 헤더 상위의 헤더들의 배열을 수정 
						 for(k= i-1; k > -1; k--)
						 {
							 
							 _tA1 = _newHeaderMap.get(String.valueOf(k));
							 String _emptyStr = _tA1.get(j+ _spliceCnt1 -1);
							 
//							 _tA1.add(j+ _spliceCnt1, "&Empty");		//@@@ 20190722 헤더 병합 조건 수정 필요 
							 _tA1.add(j+ _spliceCnt1, _emptyStr);		//@@@ 20190722 헤더 병합 조건 수정 필요 
							 
							 _newHeaderMap.put(String.valueOf(k) , _tA1);
						 }
						 
						 _spliceCnt1++;
					 }
					 
					 _newHeaderArray.add(_tA.get(j));
					 _beforeCol = _tA.get(j);
				}
				 _newHeaderMap.put(String.valueOf(i), _newHeaderArray);
			}
			 _newHeaderMap.put(String.valueOf(i), headerMap.get(String.valueOf(i))); //제일 마지막 헤더 부분은 그대로 삽입

			 
			 if( colLength > 2 )
			 {
				 for ( k = 0; k < colLength; k++) {
					 
					 _tA1 = _newHeaderMap.get(String.valueOf(k));
					_newHeaderMap.put(String.valueOf(k), _tA1);	
				 }
			 }
			 
			 
			 /** 데이터 영역의 값을 입력하기 위한 빈 컬럼을 생성하여 준다 **/
			 _tA1 = _newHeaderMap.get(String.valueOf(colLength-1));
			 
			for ( i = 0; i < _tA1.size(); i++) {
				if( substringUBF(_tA1.get(i),0, 6).equals("&Empty") )
				{
					for ( j = 0; j < _loop; j++) {
						dataMap.get(String.valueOf(j)).add(i, "&Empty");
					} 
				
					_grandTotalObj.get("0").add(i, "&Empty");
				}
			}			 
			 
			/**새로 생성된 헤더 어레이를 이용하여 Data 영역의 summery컬럼을 추가 하면서 실제 데이터를 입력한다**/
			BigDecimal _totSum = new BigDecimal(0);
			BigDecimal _totSumG = new BigDecimal(0);
			int _start = 0;
			int _end = 0;
//			String _grandTot = "";
//			String _colN = "";

			BigDecimal _sum = new BigDecimal(0);
			BigDecimal _sumG = new BigDecimal(0);
			
			for ( i = 0; i < colLength; i++) {
//				_grandTot 	= "";
//				_colN 		= "";
				_start 		= 0;
				_end 		= 0;
				
				_tA1 = _newHeaderMap.get(String.valueOf(i));
				
				for ( j = 0; j < _tA1.size(); j++) {
					_totSum = new BigDecimal(0);
					_totSumG = new BigDecimal(0);
					
					if( substringUBF(_tA1.get(j),0, 7).equals("&LFixed") || substringUBF(_tA1.get(j),0, 7).equals("&RFixed") || substringUBF(_tA1.get(j),0, 6).equals("&Total") 
							|| substringUBF(_tA1.get(j),0, 6).equals("&Title") || substringUBF(_tA1.get(j),0, 6).equals("&Empty")  || substringUBF(_tA1.get(j),0, 3).equals("$No") )
					{
						continue;
					}
					
					if(  substringUBF(_tA1.get(j),0, 5).equals("&Sum_") )
					{
						_end = j;
						
						for ( k = 0; k < _loop; k++) {
							
							if( j == _tA1.size()-2)
							{
								dataMap.get(String.valueOf(k)).set(j, String.valueOf(_totSum) );
							}
							
							_sum = new BigDecimal(0);
//							_colP = 0;
							_ck = 0;
							
							for ( l = _start; l < _end; l++) {
								
								if( substringUBF(_tA1.get(l),0, 7).equals("&LFixed") || substringUBF(_tA1.get(l),0, 7).equals("&RFixed") || substringUBF(_tA1.get(l),0, 6).equals("&Total") 
										|| substringUBF(_tA1.get(l),0, 6).equals("&Title") || substringUBF(_tA1.get(l),0, 6).equals("&Empty")  || substringUBF(_tA1.get(l),0, 3).equals("$No") )
								{
									continue;
								}
								
								if( valueFunctionStr.equals("TEXT") == false && substringUBF( dataMap.get(String.valueOf(k)).get(l), 0, 6).equals("&Empty") == false )
								{
//									_sum = _sum + Long.valueOf( dataMap.get(String.valueOf(k)).get(l) );
									_sum = _sum.add( UBIPivotParser.convertBigDecimal( dataMap.get(String.valueOf(k)).get(l) )  );
								}
							}
								
							dataMap.get(String.valueOf(k)).set(j, String.valueOf(_sum)	);
							_totSum = _totSum.add(_sum) ;
						}
						// Grand Total Object 합계 계산 후 입력
						// 제일 마지막 계산을 한다.
						if(j == _tA1.size()-2)
						{
							_grandTotalObj.get("0").set(j, String.valueOf(_totSumG)	);
						}
						
						_sumG = new BigDecimal(0);
						
						for ( l = _start; l < _end; l++) {
							
							if( valueFunctionStr.equals("TEXT") == false && substringUBF(_grandTotalObj.get("0").get(l),0, 6) != "&Empty" )
							{
								_sumG = _sumG.add( new BigDecimal( _grandTotalObj.get("0").get(l) ) );
							}
						}
						
						
						_grandTotalObj.get("0").set(j, String.valueOf(_sumG));
						_totSumG = _totSumG.add(_sumG);
						
						_start = 0;
						_end = 0;
						
					}
					else
					{
						if(_start == 0) _start = j;
						
					}
					
				}
				
			}
			
			
			/** 컬럼별 합계를 여기에 추가 모든 컬럼별 합계 추가후 헤더에 컬럼명 추가 작업 필요 **/
			if( summaryColumn != null )
			{
				int _sumColIndex = 0;
				BigDecimal _sumTotal = new BigDecimal(0);
				ArrayList<BigDecimal> _sumA;
				String _sumColText = "";
				
				int _sumRowMax = 0;
				int _sumRowIndex = 0;
//				int _sumRowTotal = 0;
				
				for ( i = 0; i < summaryColumn.size(); i++) {
					sumData = ( HashMap<String, String> ) summaryColumn.get(i);
					
					_sumColIndex = valueAr.indexOf(sumData.get("columnName"));
					if( _sumColIndex != -1 )
					{
						_sumA = summaryDataAr.get(i);
						
						_sumRowMax = _loop;
						_sumRowIndex = 0;
//						_sumRowTotal = 0;
						
						for ( _sumRowIndex = 0; _sumRowIndex < _sumRowMax; _sumRowIndex++) {
							
							dataMap.get(String.valueOf(_sumRowIndex)).add(dataMap.get(String.valueOf(_sumRowIndex)).size()-1, String.valueOf( _sumA.get(_sumRowIndex) )	);
//							_sumTotal = _sumTotal +  Double.valueOf(_sumA.get(_sumRowIndex).toString());
							_sumTotal = _sumTotal.add( UBIPivotParser.convertBigDecimal( _sumA.get(_sumRowIndex).toString()) );
						}
						_sumRowIndex = 0;
						_sumRowMax = colAr.size();
						
						for ( _sumRowIndex = 0; _sumRowIndex < _sumRowMax; _sumRowIndex++) {
							_sumColText = "&total_" + sumData.get("headerName") + "&";
							
							_newHeaderMap.get(String.valueOf(_sumRowIndex)).add( _newHeaderMap.get(String.valueOf(_sumRowIndex)).size()-1 , _sumColText);
						}
						
						_grandTotalObj.get("0").add( _grandTotalObj.get("0").size()-1 , String.valueOf(_sumTotal) ); 
						
					}
				}
				
			}
			
			
			/***	RowCount가 2 이상이면 row 별 중간 summery 영역을 만든 후 데이터를 입력한다.	***/
			ArrayList<BigDecimal> _sumA = new ArrayList<BigDecimal>();
			ArrayList<String> _tA3;
			for ( i = 0; i < _loop; i++) {
				dArray.add( dataMap.get(String.valueOf(i) ) );
			}
			
			if( rowLength > 1 )
			{
				
				int _summeryCnt = 0;
				_tA = _newHeaderMap.get("0");
				
				for ( i = 0; i < _tA.size(); i++) {
					
					if( substringUBF(_tA.get(i),0, 6).equals("&Title") )
					{
						_summeryCnt++;
					}
				}
				
//				int _spCnt = -1;
				for ( i = _summeryCnt-2; i > -1; i--) {
					
					_tA1 = new ArrayList<String>();
					String _beforeData = "start";
					int _loopStart = 0;
					int _increase = 0;
//					int _xLoop = 0;
					
//					_spCnt++;
					
					for ( j = 0; j < _loop +1; j++) {
						
						if (j == 0) {
							_beforeData = dataMap.get(String.valueOf(j)).get(i);
							continue;
						}
						
						if(  j == _loop || _beforeData.equals( dataMap.get(String.valueOf(j)).get(i)) == false )
						{
							_tA1 = new ArrayList<String>();
							
							// row 한개를 해당 index에 insert한 후 데이터를 계산하여 입력한다. 
							
							//타이틀을 만든다. (우측 타이틀 입력)
							for (k = 0; k < i; k++) {
								_tA1.add("&Summery");
							}
							
							if(_tA1.size() > i)
							{
								_tA1.set(i, "&Summery_" + dataMap.get(String.valueOf(j-1)).get(i) );
							}
							else
							{
								_tA1.add( "&Summery_" + dataMap.get(String.valueOf(j-1)).get(i) );
							}
							
							//타이틀을 만든다. (우측 타이틀 입력)
							for (k = i+1; k < _summeryCnt; k++) {
								_tA1.add("");
							}
							
							_sumA = new ArrayList<BigDecimal>();
							
							_tA3 = dataMap.get("0");
							
							int _j1 = j;
							
//							if( j == _loop -1 )
							if( j == _loop )
							{
								_j1 = j;
								_sumA.add( new BigDecimal(0) );
							}
							
							for ( l =  _loopStart; l < _j1; l++) {
								//////// UBIPivotParser.as 885 line 
								for ( m = 0; m < _tA3.size(); m++) {
									if( isNumber( dataMap.get(String.valueOf(l)).get(m)  ) )
									{
										if( _sumA.size() <= m ) _sumA.add( UBIPivotParser.convertBigDecimal( dataMap.get(String.valueOf(l)).get(m) ) );
										else _sumA.set(m, _sumA.get(m).add( UBIPivotParser.convertBigDecimal( dataMap.get(String.valueOf(l)).get(m) ) )  );
									}
									else
									{
										if( _sumA.size() <= m ) _sumA.add( new BigDecimal(0) ); 
										else _sumA.set(m, _sumA.get(m).add(new BigDecimal(0)) );
									}
								}
								
							}
							
							if( valueFunctionStr.equals("AVG") )
							{
								for (int n = rowLength; n < _sumA.size(); n++) {
//									_sumA.set(n, _sumA.get(n).divide( new BigDecimal( (_j1-_loopStart) ) , _maxDivideCnt, _defaultDecimalRound ) );
									_sumA.set(n,  bigIntegerDivide( _sumA.get(n),  new BigDecimal( (_j1-_loopStart) )  ) );
								}
							}
							
							int _ta1Size = _tA1.size();
							for ( l = 0; l < _tA3.size(); l++) {
								if( l >= _ta1Size)
								{
									if(_tA1.size() > l)
									{
										_tA1.set(l, String.valueOf( _sumA.get(l) )	);
									}
									else
									{
										_tA1.add( String.valueOf( _sumA.get(l) )	);
									}
									
								}
							}
							
							
							if( j == _loop ){
								dArray.add(dArray.size(), _tA1);
							}
							else
							{
								// j + 1 이상의 칼럼이 &Summery 이면 increase 를 하나씩 증가 하여 처리 한다.
								//처음 시작 점을 원래 배열의 index 를 이용하여 찾는다.
								
//								int _xs = j;
//								
//								if( i < _summeryCnt -2 )
//								{
//									
//									for ( k = 0; k < dArray.size(); k++) {
//										
//										if( _tA1.size() > 0 && isNumber( dArray.get(k).get(_tA1.size()-1) ))
//										{
//											int _idx = Integer.valueOf((int) Math.floor( Float.valueOf(dArray.get(k).get(_tA1.size()-1)) )  );
//											if( _idx == j )
//											{
//												_xs = k;
//											}
//										}
//									}
//								}
								//@@@TEST 크로스탭 처리해야할부분 2015-12-23
//								String darrayTitle = "";
//								for ( k = _xs; k < dArray.size() ; k++) {
//									darrayTitle = dArray.get(k).get(0);
//									if( substringUBF(darrayTitle,0, 8).equals("&Summery") || substringUBF(darrayTitle,0, 9).equals("&Summery_") )
//									{
//										_increase++;
//									}
//									else
//									{
//										break;
//									}
//								}
								
								for ( k = _increase; k < dArray.size(); k++) {
									if( !dArray.get(k).get(i).equals( dataMap.get(String.valueOf(j-1)).get(i) ) && !(substringUBF(dArray.get(k).get(i),0, 8).equals("&Summery") || substringUBF(dArray.get(k).get(i),0, 9).equals("&Summery_")) )
									{
										_increase = k;
										break;
									}
								}
								
								
								dArray.add( _increase , _tA1);
								_increase = _increase + 1;
								/* 
								if( _xLoop == 0 )
								{
									if( _tA1.size() > 0 )dArray.add( _xs + _increase, _tA1);
								}
								else
								{
//									dArray.add( _xs + _increase + _xLoop - _spCnt , _tA1);
//									dArray.add( _xs + _xLoop - _spCnt , _tA1);
								}
								*/
								
//								_xLoop++;
								
							}
							
							_loopStart = j;
							
						}
						
						if(dataMap.containsKey(String.valueOf(j)))
						{
							_beforeData = dataMap.get(String.valueOf(j)).get(i);
						}
						
					}
					
					
				}
			}
			
		 }
		 
		 
		for ( i = 0; i < dArray.size(); i++) {
//			dArray.get(i).remove(dArray.get(i).size()-1);
		}
		
		dArray.add( _grandTotalObj.get("0") ); 
		
		if(useRowNum)
		{
			int _dMax = dArray.size();
			int _dataCount = 0;
			
			for ( i = 0; i < _dMax; i++) {
				
				if( i ==_dMax-1 )
				{
					dArray.get(i).add(rowLength, "&Empty");
				}
//				else if( dArray.get(i).get(0).equals("&Summery") )
				else if( substringUBF(dArray.get(i).get(0),0, 8).equals("&Summery")  )
				{
					dArray.get(i).add(rowLength, "&Empty");
				}
				else
				{
					_dataCount++;
					dArray.get(i).add(0, String.valueOf(_dataCount));
				}
			}
			
			for ( i = 0; i < colAr.size(); i++) {
				
				_newHeaderMap.get(String.valueOf(i)).add( rowLength, "&Title" + rowLength );
				
			}
			
		}
		
		retHashMap.put("ROWCOUNT", _loop);
		retHashMap.put("RESULT", "SUCCESS");
		retHashMap.put("HEADER", _newHeaderMap);
		retHashMap.put("DATA", dArray);
		retHashMap.put("GRANDTOTAL", _grandTotalObj);
		
		return retHashMap;
	}
	
	
	 public static boolean isNumber(String number){
	    boolean flag = true;
	    if ( number == null  ||    "".equals( number )  )
	     return false;

	    int size = number.length();
	    int st_no= 0;

	    if ( number.charAt(0)  ==  45 )//음수인지 아닌지 판별 . 음수면 시작위치를 1부터
	     st_no = 1;
	    

	    for ( int i = st_no ; i < size ; ++i ){
	     if ( !(".".charAt(0) == number.charAt(i)) &&  !( 48   <=  ((int)number.charAt(i))   && 57>= ( (int)number.charAt(i) )  )  ){
	      flag = false;
	      break;
	     }
	     
	    }
	    return flag;
	 }
	 
	 public static BigDecimal convertBigDecimal( String _value)
	 {
		 BigDecimal _retDec = new BigDecimal(0);
		 
		 try {
			 _retDec = new BigDecimal(_value);
		} catch (Exception e) {
			// TODO: handle exception
		}
		 
		 return _retDec;
	 }
	 
	 
	
	private ArrayList<HashMap<String, Object>> calData( ArrayList<HashMap<String, Object>> data, String colName, String colData)
	{
		ArrayList<HashMap<String, Object>> retAC = new ArrayList<HashMap<String,Object>>();
		
		for (HashMap<String, Object> hashMap : data) {
			
//			if( hashMap.get(colName) == colData){
			if( hashMap.get(colName).toString().equals(colData) ){
				retAC.add(hashMap);
			}
			
		}
		
		return retAC;
	}
	
	
	
	
	
	/**
	 * ArrayList를 Sort시키기 위한 함수.
	 * @param _dataAr	Sort할 데이터ArrayList
	 * @param colAr		Sort시킬 컬럼리스트
	 * @param descending	Sort시킬 컬럼별 Descading여부
	 * @param sortNumeric	Sort시킬 컬럼의 Numeric여부 
	 * @return
	 */
	private  ArrayList<HashMap<String, Object>> sortDataSet( ArrayList<HashMap<String, Object>> _dataAr, ArrayList<String> colAr,ArrayList<String> descending, ArrayList<String> sortNumeric)
	{
//		ArrayList<HashMap<String, Object>> retAr = new ArrayList<HashMap<String,Object>>();

		//sort 시 descend와 numeric값을 이용하여 처리
		if( colAr.size() > 0 ){
			Collections.sort(_dataAr, new UBIMapComparator(colAr, descending, sortNumeric){
				
				@Override
				public int compare(Map<String, Object> o1,
						Map<String, Object> o2) {
					// TODO Auto-generated method stub
					c = 0;
					BigDecimal _o1Dec;
					BigDecimal _o2Dec;
					
					for (int i = 0; i < mColAr.size(); i++) {
						
						column = mColAr.get(i);
						
						if( mDesAr == null || mDesAr.size() == 0 || mDesAr.get(i) == "false" )
						{
							if( mNumericAr != null && mNumericAr.size() > 0 && mNumericAr.get(i) == "true")
							{
								_o1Dec = new BigDecimal(String.valueOf(o1.get (column)));
								_o2Dec = new BigDecimal(String.valueOf(o2.get (column)));
								c =  _o1Dec.compareTo(_o2Dec);
//								c =  Integer.valueOf( String.valueOf(o1.get (column)) ).compareTo( Integer.valueOf(String.valueOf(o2.get (column)))  );
							}
							else
							{
								c = String.valueOf(o1.get (column)).compareTo(String.valueOf(o2.get (column)) );
							}
						}
						else
						{
							if( mNumericAr != null && mNumericAr.size() > 0 && mNumericAr.get(i) == "true")
							{
								_o1Dec = new BigDecimal(String.valueOf(o1.get (column)));
								_o2Dec = new BigDecimal(String.valueOf(o2.get (column)));
								c =  _o2Dec.compareTo(_o1Dec);
//								c =  Integer.valueOf( String.valueOf(o2.get (column)) ).compareTo( Integer.valueOf(String.valueOf(o1.get (column)))  );
							}
							else
							{
								c = String.valueOf(o2.get (column)).compareTo(String.valueOf(o1.get (column)) );
							}
						}
						if( c != 0 ) return c;
					}
					
					return super.compare(o1, o2);
				}
				
			});
		}
		
		return _dataAr;
	};
	
	
   
	private HashMap<String, ArrayList<HashMap<String, Object>>> groupArrayCollection( ArrayList<HashMap<String, Object>> dataAr, ArrayList<String> colAr)
	{
		
		/***	sorting	***/
		ArrayList<HashMap<String, Object>> resultAC = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> tempData = new HashMap<String, Object>();
		HashMap<String, ArrayList<HashMap<String, Object>>> resultHashMap = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		/***		***/
		String _colData = "";
		String _colDataBefore = "";
		int _loop = 0;
		
		dataAr = sortDataSet(dataAr, colAr, null, null);
		
		/** loop 돌면서 해당 컬럼의 데이터가 모두 동일한 Row를 모아서 하나의 ArrayList에 담는다.*/
		for (int i = 0; i < dataAr.size(); i++) {
			
			_colData = "";
			
			for (int j = 0; j < colAr.size(); j++) {
				
				_colData = _colData + dataAr.get(i).get(colAr.get(j));
				
			}
			
			if( i == 0)
			{
				tempData = dataAr.get(i);
				resultAC.add( tempData );
				_colDataBefore = _colData;
			}
			else if( i == (dataAr.size()-1) )
			{
				if( _colDataBefore.equals(_colData) )
				{
					tempData = dataAr.get(i);
					resultAC.add( tempData );
					resultHashMap.put(String.valueOf(_loop), resultAC );
				}
				else
				{
					resultHashMap.put(String.valueOf(_loop), resultAC);
					resultAC = new ArrayList<HashMap<String,Object>>();
					
					_loop++;
					
					tempData = dataAr.get(i);
					resultAC.add(tempData);
					_colDataBefore = _colData;
				
					resultHashMap.put(String.valueOf(_loop), resultAC );
				}
				
				break;
			}
			else
			{
				
				if( _colDataBefore.equals(_colData))
				{
					tempData = dataAr.get(i);
					resultAC.add(tempData);
				}
				else
				{
					resultHashMap.put(String.valueOf(_loop), resultAC );
					resultAC = new ArrayList<HashMap<String,Object>>();
					
					tempData = dataAr.get(i);
					resultAC.add(tempData);
					_colDataBefore = _colData;
					
					_loop++;
				}
				
				
			}
			
			
		}		
		
		
		
		return resultHashMap;
		
	}
	
	
	
	private HashMap<String, ArrayList<HashMap<String, Object>>> groupArrayCollection2(ArrayList<HashMap<String, Object>> dataAr, ArrayList<String> colAr, ArrayList<String> sortColAr, ArrayList<String> descendingAr, ArrayList<String> leftFixedAr, ArrayList<String> rightFixedAr )
	{
		
		/***	sorting	***/
		ArrayList<HashMap<String, Object>> resultAC = new ArrayList<HashMap<String,Object>>();
		ArrayList<HashMap<String, Object>> retAC = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> tempData = new HashMap<String, Object>();
		HashMap<String, ArrayList<HashMap<String, Object>>> resultHashMap = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		/***		***/
		String _colData = "";
		String _colDataBefore = "";
		int _loop = 0;
		
		retAC = sortDataSet(dataAr, colAr, new ArrayList<String>(), new ArrayList<String>());
		
		// 추가로 정렬할 컬럼이 존재할경우 Sort처리
		if( sortColAr != null && sortColAr.size() > 0 )
		{
			retAC = sortDataSet(retAC, sortColAr, descendingAr , new ArrayList<String>());
		}
		
		
		/** loop 돌면서 해당 컬럼의 데이터가 모두 동일한 Row를 모아서 하나의 ArrayList에 담는다.*/
		for (int i = 0; i < dataAr.size(); i++) {
			
			_colData = "";
			
			for (int j = 0; j < colAr.size(); j++) {
				
				_colData = _colData + dataAr.get(i).get(colAr.get(j));
				
			}
			
			if( i == 0)
			{
				tempData = dataAr.get(i);
				resultAC.add( tempData );
				_colDataBefore = _colData;
			}
			else if( i == (dataAr.size()-1) )
			{
				if( _colDataBefore.equals(_colData) )
				{
					tempData = dataAr.get(i);
					resultAC.add( tempData );
					resultHashMap.put(String.valueOf(_loop), resultAC );
				}
				else
				{
					resultHashMap.put(String.valueOf(_loop), resultAC);
					resultAC = new ArrayList<HashMap<String,Object>>();
					
					_loop++;
					
					tempData = dataAr.get(i);
					resultAC.add(tempData);
					_colDataBefore = _colData;
				
					resultHashMap.put(String.valueOf(_loop), resultAC );
				}
				
				break;
			}
			else
			{
				
				if( _colDataBefore.equals(_colData))
				{
					tempData = dataAr.get(i);
					resultAC.add(tempData);
				}
				else
				{
					resultHashMap.put(String.valueOf(_loop), resultAC );
					resultAC = new ArrayList<HashMap<String,Object>>();
					
					tempData = dataAr.get(i);
					resultAC.add(tempData);
					_colDataBefore = _colData;
					
					_loop++;
				}
				
				
			}
			
			
		}		
		
		
		return resultHashMap;
	}
    
    
	private BigDecimal getMathData( BigDecimal _num, String type )
	{
		BigDecimal retNum = new BigDecimal(0);
		MathContext mc = new MathContext(0);
		
		if( type.equals("none") )
		{
			retNum = _num;
		}
		else if( type.equals("round"))
		{
//			retNum = (float) Math.round( _num );
			retNum = _num.round(mc);
		}
		else if( type.equals("ceil"))
		{
//			retNum = (float) Math.ceil( _num );
			retNum = _num.setScale(0, RoundingMode.CEILING);
		}
		else if( type.equals("floor"))
		{
//			retNum = (float) Math.floor( _num );
			retNum = _num.setScale(0, RoundingMode.FLOOR);
		}
		else
		{
			retNum = _num;
		}
		
		
		return retNum;
	}
	
	
    private Integer getDataAvgCount( ArrayList<HashMap<String, Object>> ac, ArrayList<String> colar, HashMap<String, String> columnData )
    {
    	int _cnt = 0;
    	ArrayList<String> _chkColumnDataAr = new ArrayList<String>();
    	int _chkNum = 0;
    	int _chkMax = colar.size();
    	String _chkTitleStr = "";
    	String _colName = "";
    	for (HashMap<String, Object> hmp : ac) {
    		if( columnData.containsKey("columnName") )_colName = columnData.get("columnName");
    		else _colName = "";
    		
    		if(  hmp.containsKey( _colName ) && hmp.get(_colName) != null && isNumber(String.valueOf( hmp.get(_colName) ) )  && hmp.get(_colName) != ""  )
    		{
    			
    			_chkTitleStr = "";
    			
    			for(_chkNum = 0; _chkNum < _chkMax; _chkNum++)
    			{
    				
    				if( _chkTitleStr != "") _chkTitleStr += "_";
    				_chkTitleStr = _chkTitleStr + hmp.get(_colName);
    				
    				if(_chkColumnDataAr.indexOf(_chkTitleStr) == -1)
    				{
    					_cnt++;
    					_chkColumnDataAr.add(_chkTitleStr);
    				}
    				
    			}
    			
    		}
    		
		}
    	
    	if( _cnt == 0 ) _cnt = 1;
    	
    	return _cnt;
    }
    
    public static String substringUBF(String str, int _strIndex, int _lastIndex)
    {
    	String retStr = "";
    	
    	if(str.length() < _lastIndex ) _lastIndex = str.length();
    	
    	retStr = str.substring(_strIndex, _lastIndex);
    	
    	return retStr;
    }
    
    
    
    private BigDecimal bigIntegerDivide( BigDecimal _value, BigDecimal _target)
    {
//    	_value.divide(_target, _value.scale() - _target.scale() , RoundingMode.CEILING );
    	
    	try {
    		_value = _value.divide(_target, RoundingMode.HALF_UP );
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	return _value;
    }
    
    
    
}
