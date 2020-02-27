package org.ubstorm.service.parser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ubstorm.service.parser.formparser.UBIMapComparator;

public class UBIDataParser {

	
	public static ArrayList<HashMap<String, Object>> filterMultiColumn( ArrayList<HashMap<String, Object>> dataSet, ArrayList<String> colAr, ArrayList<String> operator, ArrayList<String> filter   )
	{
		ArrayList<HashMap<String, Object>> returnData = new ArrayList<HashMap<String,Object>>();
		
		if( colAr.size() < 1 )
		{
			return dataSet;
		}
		
		Boolean retVal = false;
		
		for (int i = 0; i < dataSet.size(); i++) {
			
			retVal = false;
			
			for (int j = 0; j < colAr.size(); j++) {
				
				if( operator.get(j).equals("=") || operator.get(j).equals("==") )
				{
					if( dataSet.get(i).get( colAr.get(j) ).toString().equals( filter.get(j) )  )
					{
						retVal = true;
					}
				}
				else if( operator.get(j).equals("<>") )
				{
					if( dataSet.get(i).get( colAr.get(j) ).toString().equals( filter.get(j) ) == false )
					{
						retVal = true;
					}
				}
				else if( operator.get(j).equals("<") )
				{
					if( Float.valueOf( dataSet.get(i).get( colAr.get(j) ).toString() ) < Float.valueOf( filter.get(j) ) )
					{
						retVal = true;
					}
				}
				else if( operator.get(j).equals(">") )
				{
					if( Float.valueOf( dataSet.get(i).get( colAr.get(j) ).toString() ) > Float.valueOf( filter.get(j) ) )
					{
						retVal = true;
					}
				}
				else if( operator.get(j).equals("<=") )
				{
					if( Float.valueOf( dataSet.get(i).get( colAr.get(j) ).toString() ) <= Float.valueOf( filter.get(j) ) )
					{
						retVal = true;
					}
				}
				else if( operator.get(j).equals(">=") )
				{
					if( Float.valueOf( dataSet.get(i).get( colAr.get(j) ).toString() ) >= Float.valueOf( filter.get(j) ) )
					{
						retVal = true;
					}
				}
				
				
				if( retVal )
				{
					returnData.add( dataSet.get(i) );
				}
				
			}
			
		}
		
		
		return returnData;
	}
	
	
	
	public static HashMap<String , ArrayList<HashMap<String, Object>>> groupArrayCollection( ArrayList<HashMap<String, Object>> dataSet, ArrayList<String> colAr, Boolean descending, Boolean originalOrder )
	{
		HashMap<String , ArrayList<HashMap<String, Object>>> resultDataSet = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		
		ArrayList<HashMap<String, Object>> paramAC = new ArrayList<HashMap<String,Object>>();
		ArrayList<HashMap<String, Object>> retAC = new ArrayList<HashMap<String,Object>>();
		
		int i = 0;
		int j = 0;
		ArrayList<String> descendingAr = new ArrayList<String>();
		ArrayList<String> sortNumericAr = new ArrayList<String>();
		
		 ArrayList<HashMap<String, Object>> resultAC = new ArrayList<HashMap<String,Object>>();
		
		if( !originalOrder )
		{
			for ( i = 0; i < dataSet.size(); i++) {
				paramAC.add( (HashMap<String, Object>) dataSet.get(i).clone() );
			}
			
			for ( i = 0; i < colAr.size(); i++) {
				if( descending ) descendingAr.add("true");
				else  descendingAr.add("false");
				
				sortNumericAr.add("false");
			}
			
			retAC = sortDataSet(paramAC, colAr,descendingAr, sortNumericAr );
		}
		else
		{
			retAC = dataSet;
		}
		
		int _retAcLength = retAC.size();
		int _colLength = colAr.size();
		
		HashMap<String, Object> _tempObject;
		String _colData = "";
		String _colDataBefore = "";
		int _loop = 0;
		
		for ( i = 0; i < _retAcLength; i++) {
			_colData = "";
			
			for ( j = 0; j < _colLength; j++) {
				if(  retAC.get(i).containsKey( colAr.get(j) ) )
				{
					_colData = _colData + retAC.get(i).get(colAr.get(j)).toString();
				}
			}
			
			if( i == 0 )	// 첫 로우는 신규로우 추가
			{
				_tempObject = retAC.get(i);
				resultAC.add(_tempObject);
				
				_colDataBefore = _colData;
				
				if( _retAcLength == 1 )
				{
					resultDataSet.put( String.valueOf(_loop) , resultAC);
				}
			}
			else if( i == _retAcLength -1 )		// 마지막 Row에서는 비교하여 마지막 로우 추가 처리
			{
				if( _colDataBefore.equals(_colData ) )
				{
					_tempObject = retAC.get(i);
					resultAC.add(_tempObject);
					resultDataSet.put( String.valueOf(_loop) , resultAC);
				}
				else
				{
					resultDataSet.put( String.valueOf(_loop) , resultAC);
					resultAC = new ArrayList<HashMap<String,Object>>();
					
					_loop++;
					
					_tempObject = retAC.get(i);
					resultAC.add(_tempObject);
					_colDataBefore = _colData;
					
					resultDataSet.put( String.valueOf(_loop) , resultAC);
				}
			}
			else 	//현재 column과 이전 column으 ㄹ비교하여 분기한다. 같으면 AC에 추가만 하고 다를경우 resultObje를 추가한후 다시 add
			{
				
				if( _colDataBefore.equals(_colData ) )
				{
					_tempObject = retAC.get(i);
					resultAC.add(_tempObject);
				}
				else
				{
					resultDataSet.put( String.valueOf(_loop) , resultAC);
					resultAC = new ArrayList<HashMap<String,Object>>();
					
					_tempObject = retAC.get(i);
					resultAC.add(_tempObject);
					_colDataBefore = _colData;
					
					_loop++;
				}
				
			}
			
		}
		
		
		
		return resultDataSet;
	}
	
	
	

	/**
	 * ArrayList를 Sort시키기 위한 함수.
	 * @param _dataAr	Sort할 데이터ArrayList
	 * @param colAr		Sort시킬 컬럼리스트
	 * @param descending	Sort시킬 컬럼별 Descading여부
	 * @param sortNumeric	Sort시킬 컬럼의 Numeric여부
	 * @return
	 */
	public static  ArrayList<HashMap<String, Object>> sortDataSet( ArrayList<HashMap<String, Object>> _dataAr, ArrayList<String> colAr,ArrayList<String> descending, ArrayList<String> sortNumeric)
	{
		ArrayList<HashMap<String, Object>> retAr = new ArrayList<HashMap<String,Object>>();

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
						
						if( mDesAr == null || mDesAr.size() == 0 || mDesAr.get(i).equals("false") )
						{
							if( mNumericAr != null && mNumericAr.size() > 0 && (mNumericAr.get(i).equals("true") || mNumericAr.get(i).equals("1") ))
							{
								_o1Dec = new BigDecimal(String.valueOf(o1.get (column)));
								_o2Dec = new BigDecimal(String.valueOf(o2.get (column)));
								c =  _o1Dec.compareTo(_o2Dec);
								//								c =  Integer.valueOf( String.valueOf(o1.get (column)) ).compareTo( Integer.valueOf(String.valueOf(o2.get (column)))  );
//								c =  Double.valueOf( String.valueOf(o1.get (column)) ).compareTo( Double.valueOf(String.valueOf(o2.get (column)))  );
							}
							else
							{
								c = String.valueOf(o1.get (column)).compareToIgnoreCase(String.valueOf(o2.get (column)) );
							}
						}
						else
						{
							if( mNumericAr != null && mNumericAr.size() > 0 && (mNumericAr.get(i).equals("true") || mNumericAr.get(i).equals("1") ))
							{
								_o1Dec = new BigDecimal(String.valueOf(o1.get (column)));
								_o2Dec = new BigDecimal(String.valueOf(o2.get (column)));
								c =  _o2Dec.compareTo(_o1Dec);
//								c =  Integer.valueOf( String.valueOf(o2.get (column)) ).compareTo( Integer.valueOf(String.valueOf(o1.get (column)))  );
//								c =  Double.valueOf( String.valueOf(o2.get (column)) ).compareTo( Double.valueOf(String.valueOf(o1.get (column)))  );
							}
							else
							{
								c = String.valueOf(o2.get (column)).compareToIgnoreCase(String.valueOf(o1.get (column)) );
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

	
	
	/**
	 * ArrayList를 Sort시키기 위한 함수.
	 * @param _dataAr	Sort할 데이터ArrayList
	 * @param colAr		Sort시킬 컬럼리스트
	 * @param descending	Sort시킬 컬럼별 Descading여부
	 * @param sortNumeric	Sort시킬 컬럼의 Numeric여부
	 * @return
	 */
	public static  ArrayList<HashMap<String, Object>> sortArrayList( ArrayList<HashMap<String, Object>> _dataAr, ArrayList<String> colAr,ArrayList<String> descending, ArrayList<String> sortNumeric)
	{
		ArrayList<HashMap<String, Object>> retAr = new ArrayList<HashMap<String,Object>>();
		
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
						
						if( mDesAr == null || mDesAr.size() == 0 || mDesAr.get(i).equals("false") )
						{
							if( mNumericAr != null && mNumericAr.size() > 0 && (mNumericAr.get(i).equals("true") || mNumericAr.get(i).equals("1") ))
							{
								_o1Dec = new BigDecimal(String.valueOf(o1.get (column)));
								_o2Dec = new BigDecimal(String.valueOf(o2.get (column)));
								c =  _o1Dec.compareTo(_o2Dec);
//								c =  Double.valueOf( String.valueOf(o1.get (column)) ).compareTo( Double.valueOf(String.valueOf(o2.get (column)))  );
							}
							else
							{
								c = String.valueOf(o1.get (column)).compareToIgnoreCase(String.valueOf(o2.get (column)) );
							}
						}
						else
						{
							if( mNumericAr != null && mNumericAr.size() > 0 && (mNumericAr.get(i).equals("true") || mNumericAr.get(i).equals("1") ))
							{
								_o1Dec = new BigDecimal(String.valueOf(o1.get (column)));
								_o2Dec = new BigDecimal(String.valueOf(o2.get (column)));
								c =  _o2Dec.compareTo(_o1Dec);
//								c =  Double.valueOf( String.valueOf(o2.get (column)) ).compareTo( Double.valueOf(String.valueOf(o1.get (column)))  );
							}
							else
							{
								c = String.valueOf(o2.get (column)).compareToIgnoreCase(String.valueOf(o1.get (column)) );
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
	
	
}
