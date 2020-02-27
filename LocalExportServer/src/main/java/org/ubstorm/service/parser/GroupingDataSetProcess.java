package org.ubstorm.service.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.ubstorm.service.parser.formparser.data.BandInfoMapData;

public class GroupingDataSetProcess {

	public static String M_COLUMN_SEPARATOR = "§";
	//HashMap<String, ArrayList<HashMap<String, Object>>>
	public static ArrayList<Object> changeGroupDataSet(HashMap<String, ArrayList<HashMap<String, Object>>> DataSet, String grpName, String dataSetName, String col, Boolean descending, ArrayList<HashMap<String, String>> sortAC, 
			ArrayList<String> colAr,  ArrayList<String> operatorAr,  ArrayList<String> filterAr, Boolean originalOrder, HashMap<String, String> originalDataMap )
	{
		String separatorChar = BandInfoMapData.M_COLUMN_SEPARATOR;
		
		try {
			separatorChar = URLDecoder.decode(BandInfoMapData.M_COLUMN_SEPARATOR,"UTF-8");
		} catch (Exception e) {
			
		}

		ArrayList<Object> returnArray = new ArrayList<Object>();
		int _addDataCnt = 0;
		
		//HashMap<String, String> originalDataMap = new HashMap<String, String>();
		
		if( DataSet.containsKey(dataSetName) )
		{
			
			HashMap<String, ArrayList<HashMap<String, Object>>> groupingDataSet;
			
			ArrayList<String> grpList = new ArrayList<String>();
			ArrayList<HashMap<String, Object>> grpData;
			grpData = (ArrayList<HashMap<String, Object>>) DataSet.get(dataSetName);
			
			if( colAr != null && colAr.size() > 0 )
			{	
				// filter값을 이용하여 데이터 가공
				grpData = UBIDataParser.filterMultiColumn(grpData, colAr, operatorAr, filterAr);
			}
			else
			{
				
			}
			
			originalDataMap.put( separatorChar  + grpName + separatorChar ,dataSetName);
			
			// 그룹 데이터셋을 위하여 데이터셋에 담기
			DataSet.put(separatorChar + grpName + separatorChar , grpData );
			ArrayList<String> _tempColAr = new ArrayList<String>();
			_tempColAr.add(col);
			groupingDataSet = UBIDataParser.groupArrayCollection(grpData, _tempColAr, descending, originalOrder);
			
			ArrayList<HashMap<String, Object>> newGrpData;
			String grpingDataName = "";
			int loop = groupingDataSet.size();
			
			if( loop > 0 )
			{
				for (int i = 0; i < loop; i++) {
					
					grpingDataName =  grpName + "grp_" + String.valueOf(i) + "_" +  dataSetName;
					grpList.add( grpingDataName );
					
					newGrpData = groupingDataSet.get(String.valueOf(i));
					
					
					originalDataMap.put(grpingDataName, dataSetName);
					
					if( sortAC != null && sortAC.size() > 0 && newGrpData != null && newGrpData.size() > 0 )
					{
						
						ArrayList<String> _columnAr = new ArrayList<String>();
						ArrayList<String> _orderByAr = new ArrayList<String>();
						ArrayList<String> _numericAr = new ArrayList<String>();
						for (int j = 0; j < sortAC.size(); j++) {
							
							if( sortAC.get(j).containsKey("column") )
							{
								_columnAr.add( sortAC.get(j).get("column") );
							}
							
							if( sortAC.get(j).containsKey("orderBy") )
							{
								_orderByAr.add( sortAC.get(j).get("orderBy") );
							}
							
							if( sortAC.get(j).containsKey("isNumeric") )
							{
								_numericAr.add( sortAC.get(j).get("isNumeric") );
							}
							else
							{
								_numericAr.add("false");
							}
							
						}
						
						newGrpData = UBIDataParser.sortDataSet(newGrpData, _columnAr, _orderByAr, _numericAr);
						
					}
					
					DataSet.put(grpingDataName , newGrpData );
				}
				
			}
			// originalDataSet 맵을 저장하여 사용해야함
			_addDataCnt = loop;
		}
		else
		{
			
		}
		
		returnArray.add(DataSet);
		returnArray.add(originalDataMap);
		returnArray.add( _addDataCnt );
		
//		return DataSet;
		return returnArray;
	}
	
		
	public static ArrayList<ArrayList<HashMap<String, Object>>> changeGroupDataSetSub(HashMap<String, ArrayList<HashMap<String, Object>>> DataSet, String grpName, 
			String dataSetName, ArrayList<String> colAr, ArrayList<Boolean> descedingAr, ArrayList<HashMap<String, String>> sortAC, Boolean originalOrder, HashMap<String, String> originalDataMap )
	{
		ArrayList<ArrayList<HashMap<String, Object>>> retAr = new ArrayList<ArrayList<HashMap<String, Object>>>();
		
		if( DataSet.containsKey( dataSetName ))
		{
			HashMap<String, ArrayList<HashMap<String, Object>>> subDataSet;
			
			
			Boolean _orderBy = false;
			String originalDataName = "";
			
			if( originalDataMap.containsKey(dataSetName) && originalDataMap.get(dataSetName).equals("") == false )
			{
				originalDataName = originalDataMap.get(dataSetName);
			}
			else
			{
				originalDataName = dataSetName;
			}
			
			int i = 0;
			int j = 0;
			int k = 0;
			
			if( descedingAr.size() > 1)
			{
				_orderBy = descedingAr.get(1);
			}
			ArrayList<String> _cols = new ArrayList<String>();
			_cols.add( colAr.get(1) );
			subDataSet = UBIDataParser.groupArrayCollection(DataSet.get(dataSetName), _cols, _orderBy, originalOrder);
			
			for ( i = 0; i < subDataSet.size(); i++) {
				retAr.add(subDataSet.get(String.valueOf(i)));
			}
			
			int colLength = colAr.size();
			int retLength = retAr.size();	
			String newGrpDataName = "";
			
			ArrayList<ArrayList<HashMap<String, Object>>> _backAr;
			
			ArrayList<HashMap<String, Object>> newGrpData;
			
			for ( i = 1; i < colLength; i++) {
				
				_backAr = new ArrayList<ArrayList<HashMap<String, Object>>>();
				
				_cols.clear();
				_cols.add( colAr.get(i) );
				
				for ( j = 0; j < retAr.size(); j++) {
					
					if( retAr.get(j).get(0).containsKey("groupFooterIndex") )
					{
						_backAr.add(retAr.get(j));
					}
					else
					{
						if( descedingAr.size() >= i ) _orderBy = descedingAr.get(i);
						
						subDataSet = UBIDataParser.groupArrayCollection(retAr.get(j), _cols, _orderBy, originalOrder);
						
						for ( k = 0; k < subDataSet.size(); k++) {
							
							newGrpData = subDataSet.get(String.valueOf(k));
							
							_backAr.add( newGrpData );
							
							newGrpDataName = grpName + dataSetName + "_f_" + String.valueOf(i) + "_" + j + "_" +  String.valueOf(k);
							
//							originalDataMap.put(newGrpDataName, originalDataName);
							
							ArrayList<HashMap<String, Object>> grpFooterList = new ArrayList<HashMap<String,Object>>();
							HashMap<String, Object> grpFooterMap = new HashMap<String, Object>();
							grpFooterMap.put("groupFooterIndex", i);
							grpFooterMap.put("dataSet", newGrpDataName);
							grpFooterList.add(grpFooterMap);
							
							_backAr.add(grpFooterList);
							
							if( sortAC != null && sortAC.size() > 0 && newGrpData != null && newGrpData.size() > 0 )
							{
								
								ArrayList<String> _columnAr = new ArrayList<String>();
								ArrayList<String> _orderByAr = new ArrayList<String>();
								ArrayList<String> _numericAr = new ArrayList<String>();
								for (int l = 0; l < sortAC.size(); l++) {
									
									if( sortAC.get(l).containsKey("column") )
									{
										_columnAr.add( sortAC.get(l).get("column") );
									}
									
									if( sortAC.get(l).containsKey("orderBy") )
									{
										_orderByAr.add( sortAC.get(l).get("orderBy") );
									}
									
									if( sortAC.get(l).containsKey("isNumeric") )
									{
										_numericAr.add( sortAC.get(l).get("isNumeric") );
									}
									else
									{
										_numericAr.add("false");
									}
									
								}
								
								newGrpData = UBIDataParser.sortDataSet(newGrpData, _columnAr, _orderByAr, _numericAr);
								
							}
							
							
							DataSet.put(newGrpDataName, newGrpData );
						}
						
						
					}
					
				}
				
				retAr = _backAr;
				
			}
			
		}
		
		
		return retAr;
		
	}

	
	/**
	 * functionName :	groupingDataSetProcess</br>
	 * desc			:	데이터를 그룹핑 처리
	 * @param _dataSetName	데이터셋 명
	 * @param _data			실제 데이터
	 * @param _colAr		그룹핑할 컬럼리스트
	 * @param retGroupIngData	그룹핑된 데이터를 담을 맵
	 * @param _addGroupingNames	그룹핑된 데이터들의 키가 담긴 배열
	 * @return
	 */
	public static ArrayList<Object> groupingDataSetProcess( String _dataSetName, ArrayList<HashMap<String, Object>> _data, ArrayList<String> _colAr, HashMap<String, HashMap<String, ArrayList<HashMap<String,Object>>>> retGroupIngData, ArrayList<String> _addGroupingNames )
	{
		
		String _colData = "";
		int i = 0;
		int j = 0;
		int _retAcLength = 0;
		int _colLength = 0; 
		
		_retAcLength = _data.size();
		_colLength = _colAr.size();
		String separatorChar = M_COLUMN_SEPARATOR;
		
		try {
			separatorChar = URLDecoder.decode( M_COLUMN_SEPARATOR,"UTF-8"); 
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}
		ArrayList<String> _groupList = _addGroupingNames;
		
		HashMap<String, HashMap<String, ArrayList<HashMap<String,Object>>>> groupingDataSet = retGroupIngData;
		
		for ( i = 0; i < _retAcLength; i++) {
			_colData = "";
			
			for ( j = 0; j < _colLength; j++) 
			{
				if( "".equals(_colData) == false ) _colData = _colData + separatorChar;
				_colData = _colData + _data.get(i).get(_colAr.get(j)).toString();
			}
			
			if( groupingDataSet.containsKey(_colData) == false )
			{
				groupingDataSet.put(_colData, new HashMap<String, ArrayList<HashMap<String,Object>>>() );
				if( _groupList.contains(_colData) == false ) _groupList.add(_colData);
			}
			
			if( groupingDataSet.get(_colData).containsKey(_dataSetName) == false )
			{
				groupingDataSet.get(_colData).put(_dataSetName, new ArrayList<HashMap<String,Object>>() );
			}
			
			groupingDataSet.get(_colData).get(_dataSetName).add(_data.get(i));
		}
			
		ArrayList<Object> retAr = new ArrayList<Object>();
		
		retAr.add(groupingDataSet);
		retAr.add(_groupList);
		
		return 	retAr;
	}
	
	
	
	
	
}
