package org.ubstorm.service.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.ubstorm.service.parser.formparser.data.Value;
import org.ubstorm.service.parser.formparser.info.PageInfo;
import org.ubstorm.service.parser.formparser.info.PageInfoSimple;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PageNumProcess {

	
	public int getFreeFormTotalNum(Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data)
	{
		String _dataSet = "";
		
		int _pageNum = 1;
		
		NodeList _child = _page.getElementsByTagName("item");

		// xml Item
		for(int j = 0; j < _child.getLength() ; j++)
		{
			Element _childItem = (Element) _child.item(j);
			
			NodeList _propertys = _childItem.getElementsByTagName("property");
			String _dataId = "";
			
			if( _childItem.getAttribute("className") != null && _childItem.getAttribute("className").equals("UBApproval")  )
			{
				continue;
			}
			
			for(int p = 0; p < _propertys.getLength(); p++)
			{
				Element _propItem = (Element) _propertys.item(p);
				
				String _name = _propItem.getAttribute("name");
				String _value = _propItem.getAttribute("value");
			
				if("dataType".equals(_name))
				{
					if( _value.equals("1") || _value.equals("2") )
					{
						
						if( _dataSet != _dataId)
						{
							_dataSet = _dataId;
							//Integer rowCnt = _dataRowInfo.get(_dataId);
							Integer rowCnt = _data.get(_dataSet) == null ? 0 : _data.get(_dataSet).size();
							
							if( _pageNum < rowCnt)
							{
								_pageNum = rowCnt;	
							}
						}
					}
				}
				else if( "dataSet".equals(_name))
				{
					//_dataId = _value;
					// 한글 dataset 지원을 위해 decode
					try {
						_dataId = URLDecoder.decode(_value,"UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		
		return _pageNum;
	}
	
	//public int getLabelBandTotalNum(Element _page , HashMap<String, Integer> _dataRowInfo)
	public int getLabelBandTotalNum(Element _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data, int _startColIndex , int _startRowIndex , int _startIndex)
	{
			String _dataSet = "";
			
			int _pageNum = 0;
			
			NodeList _child = _page.getElementsByTagName("item");
			String _dataId = "";
			int _colCnt = 0;
			int _rowCnt = 0;
			String _direction = "";
			
			// xml Item
			for(int j = 0; j < _child.getLength() ; j++)
			{
				Element _childItem = (Element) _child.item(j);
				
				String className = _childItem.getAttribute("className");
				
				NodeList _propertys = _childItem.getElementsByTagName("property");
				
				for(int p = 0; p < _propertys.getLength(); p++)
				{
					Element _propItem = (Element) _propertys.item(p);
					
					String _name = _propItem.getAttribute("name");
					String _value = _propItem.getAttribute("value");
					
					if( className.equals("UBLabelBand"))
					{
						if( _name.equals("columns"))
						{
							_colCnt = Integer.valueOf(_value);
						}
						else if( _name.equals("rows"))
						{
							_rowCnt = Integer.valueOf(_value);
						}
						else if( _name.equals("direction"))
						{
							_direction = _value;
						}
						
						if( _colCnt > 0 && _rowCnt > 0)
						{
							break;
						}
						
					}
					else
					{

						if("dataType".equals(_name))
						{
							if( _value.equals("1") )
							{

								if( !_dataSet.equals(_dataId))
								{
									_dataSet = _dataId;
									//Integer dataCnt = _dataRowInfo.get(_dataId);
									Integer dataCnt = _data.get(_dataSet) == null ? 0 : _data.get(_dataSet).size();
									
									if( _startIndex > 0 || ( _startColIndex > 0 && _startColIndex <= _colCnt &&  _startRowIndex > 0 && _startRowIndex <= _rowCnt ) )
									{
										int _pageStartIndex = 0;
										
										if(_startIndex == 0)
										{
											if("downCross".equals(_direction)) _pageStartIndex = (_rowCnt * (_startColIndex-1)) + _startRowIndex -1;
											else _pageStartIndex = (_colCnt * (_startRowIndex-1)) + _startColIndex -1;
										}
										else
										{
											_pageStartIndex = _startIndex;
										}
										
										if(dataCnt > 0 ) dataCnt = dataCnt + _pageStartIndex;
									}
									
									float _pCnt = (float) dataCnt / (_colCnt * _rowCnt);
									
									if( _pageNum < Math.ceil(_pCnt) )
									{
										_pageNum =  (int) Math.ceil(_pCnt);
									}
								}
								else
								{
									continue;
								}
							}
						}
						else if( "dataSet".equals(_name))
						{

							if( _value.equals(_dataId))
							{
								continue;
							}
							else
							{
								_dataId = _value;
							}
						}
					}
				}
			}
			
			return _pageNum;
		}

	public int getLabelBandTotalNumJson(PageInfo _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data, int _startColIndex , int _startRowIndex , int _startIndex)
	{
		int _pageNum = 0;
		
		ArrayList<HashMap<String, Value>> _child = _page.getItems();
		int _colCnt = 1;
		int _rowCnt = 1;
		String _direction = "";
		int _size = _child.size();
		
		// xml Item
		for(int j = 0; j < _size ; j++)
		{
			HashMap<String, Value> _childItem = _child.get(j);
			
			String className = _childItem.get("className").getStringValue();
			if( className.equals("UBLabelBand"))
			{
				_direction = _childItem.get("direction").getStringValue();
				_colCnt = _childItem.get("columns").getIntValue();
				_rowCnt = _childItem.get("rows").getIntValue();
						
				break;
			}
		}
		
		
		Integer dataCnt = _page.getDataRowCount();
		if( _startIndex > 0 || ( _startColIndex > 0 && _startColIndex <= _colCnt &&  _startRowIndex > 0 && _startRowIndex <= _rowCnt ) )
		{
			int _pageStartIndex = 0;
			
			if(_startIndex == 0)
			{
				if("downCross".equals(_direction)) _pageStartIndex = (_rowCnt * (_startColIndex-1)) + _startRowIndex -1;
				else _pageStartIndex = (_colCnt * (_startRowIndex-1)) + _startColIndex -1;
			}
			else
			{
				_pageStartIndex = _startIndex;
			}
			
			if(dataCnt > 0 ) dataCnt = dataCnt + _pageStartIndex;
		}
		
		float _pCnt = (float) dataCnt / (_colCnt * _rowCnt);
		
		if( _pageNum < Math.ceil(_pCnt) )
		{
			_pageNum =  (int) Math.ceil(_pCnt);
		}
		
	
		
		return _pageNum;
	}
	
	
	public int getFreeFormTotalNumJson(PageInfo _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data)
	{
		String _dataSet = "";
		
		int _pageNum = 1;
		
		HashMap<String, Value> _childItem;
		
		ArrayList<HashMap<String, Value>> _child = _page.getItems();
		
		// xml Item
		for(int j = 0; j < _child.size() ; j++)
		{
			_childItem = _child.get(j);
			
			if( _childItem.containsKey("dataType"))
			{
				String _dataId 		= _childItem.get("dataSet").getStringValue();
				String _dataType 	= _childItem.get("dataType").getStringValue();
				
				if( _dataType != null && _dataType.equals("1") || _dataType.equals("2") )
				{
					
					if( _data.containsKey(_dataId ) )
					{
						_dataSet = _dataId;
						Integer rowCnt = _data.get(_dataSet) == null ? 0 : _data.get(_dataSet).size();
						if( _pageNum < rowCnt)
						{
							_pageNum = rowCnt;	
						}
					}
				}
			}
			
		}
		
		return _pageNum;
	}

	public int getFreeFormTotalNumSimple(PageInfoSimple _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data)
	{
		String _dataSet = "";
		
		int _pageNum = 1;
		
		HashMap<String, Object> _childItem;
		
		ArrayList<HashMap<String, Object>> _child = _page.getItems();
		Object _dataObj = null;
		
		// xml Item
		for(int j = 0; j < _child.size() ; j++)
		{
			_childItem = _child.get(j);
			
			_dataObj = _childItem.get("dataType");
			if( _dataObj != null )
			{
				String _dataType 	= _dataObj.toString();
				
				if( _dataType != null && _dataType.equals("1") || _dataType.equals("2") )
				{
					_dataObj = _childItem.get("dataSet");
					
					
					if( _dataObj != null && _data.containsKey( _dataObj.toString() ) )
					{
						_dataSet = _dataObj.toString();
						Integer rowCnt = _data.get(_dataSet) == null ? 0 : _data.get(_dataSet).size();
						if( _pageNum < rowCnt)
						{
							_pageNum = rowCnt;	
						}
					}
				}
			}
			
		}
		
		return _pageNum;
	}
	
	public int getLabelBandTotalNumSimple(PageInfoSimple _page , HashMap<String, ArrayList<HashMap<String, Object>>> _data, int _startColIndex , int _startRowIndex , int _startIndex)
	{

		return 1;
	}
}
