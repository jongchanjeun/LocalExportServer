package org.ubstorm.service.parser;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.log4j.Logger;
import org.ubstorm.service.parser.txt.PlainTextTableParser;

public class ubFormToTEXT {

	private Logger log = Logger.getLogger(getClass());
	private ArrayList<ArrayList<HashMap<String, Object>>> itemPropList;
	
	FileWriterWithEncoding fw = null;
	
	public ubFormToTEXT( String _filePath ) throws IOException {
		// TODO Auto-generated constructor stub
		fw =  new FileWriterWithEncoding( _filePath,"UTF-8", true ); 
	}

	
	public String xmlPasingtoText(ArrayList<ArrayList<HashMap<String, Object>>> dataMap ) throws IOException
	{
		itemPropList = dataMap;
		
		toTextFile();
		
		return "";
	}
	
	public boolean closeFileWriter()
	{
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Component 생성.
	 */
	/**
	 * functionName :	toTextFile</br>
	 * desc			:	 
	 * @param _filePath
	 * @throws IOException 
	 */
	private boolean toTextFile() throws IOException
	{
//		  try{
			HashMap< String, ArrayList< HashMap<String, Object> >> _headerBandItems = new HashMap< String, ArrayList< HashMap<String, Object> >>();
			HashMap<String, Object> _tableItem;
			StringBuilder resultBuilder = new StringBuilder();
			ArrayList<HashMap<String, Object>> _itemList = new ArrayList<HashMap<String, Object>>();
			String itemClass = "";
			
			//아이템 갯수만큼 반복하면서 진행
			for (ArrayList<HashMap<String, Object>> pageAr : itemPropList) {
				
				for (HashMap<String, Object> _item : pageAr) {
				
					itemClass = String.valueOf(_item.get("className"));
					// 출력할 아이템만 선택하여 아이템 추출
					 if( itemClass.equals("UBTable") ){
						 _tableItem = convertTableItem( _item, _headerBandItems ) ;
						 
						 if( _item.containsKey("BAND_NAME") && !_item.containsKey("HEADER_BAND_NAME") )
						 {
							 if( !_headerBandItems.containsKey( _item.get("BAND_NAME").toString() ) )
							 {
								 _headerBandItems.put(_item.get("BAND_NAME").toString() , new ArrayList< HashMap<String, Object> >());
							 }
							 _headerBandItems.get(_item.get("BAND_NAME").toString()).add(_tableItem);
						 }
						 
						 if( _tableItem != null )_itemList.add( _tableItem );
					 }
					 else if( itemClass.equals("UBLabel") || itemClass.equals("UBLabelBorder") )
					 {
						 _itemList.add( convertLabel(_item) );
					 }
					 
				}
			}
			
			// _itemList 객체 x,y값으로 Sort
			ArrayList<String> _columnAr = new ArrayList<String>();
			_columnAr.add("y");
			_columnAr.add("x");
			
			ArrayList<String> _desAr = new ArrayList<String>();
			_desAr.add("false");
			_desAr.add("false");
			
			ArrayList<String> _numericAr = new ArrayList<String>();
			_numericAr.add("true");
			_numericAr.add("true");
			
			_itemList = UBIDataParser.sortArrayList(_itemList, _columnAr, _desAr, _numericAr);
			
			// 테이블과 라벨 text변환하여 file에 write처리 
			
			PlainTextTableParser _textParser = new PlainTextTableParser();
			
			// 아이템의 className가 UBTable일경우 텍스트 변환처리
			// 일반 라벨 아이템일경우 텍스트를 String에 append 처리 
			
			int _itemSize = _itemList.size();
			String _text = "";
			ArrayList<ArrayList<HashMap<String,String>>> _rowItems;
			for( int i = 0; i < _itemSize; i++ )
			{
				itemClass = _itemList.get(i).get("className").toString();
				
				 if( _itemList.get(i).get("className").equals("UBTable") )
				 {
					 _rowItems = (ArrayList<ArrayList<HashMap<String,String>>>) _itemList.get(i).get("data");
					 
					 // Table의 columnWidth 배열 값 변환 처리 
					 ArrayList<Integer> _convertWidthInfo = convertCellWidthInfo( Float.valueOf( _itemList.get(i).get("fontSize").toString() ), (ArrayList<Float>) _itemList.get(i).get("widthInfo") );
					 int _tblSize = 0;
					 
					 for(int j = 0; j < _convertWidthInfo.size(); j++ )
					 {
						 _tblSize += _convertWidthInfo.get(j);
					 }
					 
					 // 셀 전체의 WIDTH값 변경( PIXEL 값을 SPACE의 글자수로 변경 )
					 _rowItems = convertCellWidth(_convertWidthInfo, _rowItems );
					 
					 // TABLE => TEXT로 변경 
					 _textParser.setColumnCount( Integer.parseInt( _itemList.get(i).get("columnCount").toString() ) );
					 _text = _textParser.getTableText( _rowItems, _tblSize );
				 }
				 else
				 {
					 _text = _itemList.get(i).get("text").toString();
				 }
				 
				 resultBuilder.append("\r\n");
				 resultBuilder.append(_text);	
				 resultBuilder.append("\r\n");
			}
			
			fw.write(resultBuilder.toString());
			return true;
//		  }catch (Exception e) {
//			log.error(" save Text Exception ===== > " + e.getMessage());  
//			return false;
//		  }
	}
	
	private ArrayList<Integer> convertCellWidthInfo( float _fontSize, ArrayList<Float> _widthInfos )
	{
		ArrayList<Integer> _resultWidthInfos = new ArrayList<Integer>();
		float _ptSize = _fontSize * 72 /96;		// pixel 을 pt로 변환
		float _defFontSize = 12;
		
		int _size = measureTextWidth(" ");
		
		float _scale =  _defFontSize / _fontSize;
		
		float _width = 0;
		int _convertWidth = 0;
		
		for( int i = 0; i < _widthInfos.size(); i++ )
		{
			_width = _widthInfos.get(i);
			_convertWidth = Float.valueOf((_width * _scale) / _size).intValue();
			_resultWidthInfos.add(_convertWidth);
		}
		
		return _resultWidthInfos;
	}
	
	private ArrayList<ArrayList<HashMap<String,String>>> convertCellWidth( ArrayList<Integer> _widthInfos,  ArrayList<ArrayList<HashMap<String,String>>> _rows  )
	{
		int i = 0;
		int j = 0;
		int _rowSize = _rows.size();
		int _colSize = 0;
		HashMap<String,String> _item;
		
		int _stIdx = 0;
		int _edIdx = 0;
		int _newWidth = 0;
		
		for( i = 0 ; i < _rowSize; i++ )
		{
			_colSize = _rows.get(i).size();
			
			for( j =0; j < _colSize; j++ )
			{
				_item = _rows.get(i).get(j);
				if( _item.get("type").equals("ITEM") )
				{
					_newWidth = 0;
					
					_stIdx = Integer.valueOf( _item.get("colIndex") );
					_edIdx = _stIdx + Integer.valueOf( _item.get("colSpan") );
					
					for( int k = _stIdx; k < _edIdx; k++ )
					{
						_newWidth += _widthInfos.get(k);
					}
					
					_item.put("CELL_WIDTH", String.valueOf( _newWidth ));
				}
			}
			
		}
		
		return _rows;
	}
	
	private HashMap<String, Object> getHeaderBandItem( HashMap<String, Object> _item , HashMap< String, ArrayList< HashMap<String, Object>> > _headerBandItems )
	{
		HashMap<String, Object> _resultTable = null;
		int _columnCnt = 0;
		int _headerColumnCnt = 0;
		
		if( _item.containsKey("HEADER_BAND_NAME") && _item.get("HEADER_BAND_NAME")!= null  && _headerBandItems.containsKey(_item.get("HEADER_BAND_NAME")))
		{
			int _size =  _headerBandItems.get(_item.get("HEADER_BAND_NAME")).size();
			
			_columnCnt = ((ArrayList<Float>) _item.get("widthInfo")).size();
					
			for( int i = 0; i < _size; i++ )
			{
				_headerColumnCnt = ((ArrayList<Float>) _headerBandItems.get(_item.get("HEADER_BAND_NAME")).get(i).get("widthInfo")).size();
				
				if( _columnCnt == _headerColumnCnt && Float.valueOf(_item.get("x").toString()).equals( Float.valueOf(_headerBandItems.get(_item.get("HEADER_BAND_NAME")).get(i).get("x").toString())  )  )
				{
					_resultTable = _headerBandItems.get(_item.get("HEADER_BAND_NAME")).get(i);
					break;
				}
			}
		}
		
		return _resultTable;
	}
	
	private HashMap<String, Object> convertTableItem( HashMap<String, Object> _item , HashMap< String, ArrayList< HashMap<String, Object>> > _headerBandItems )
	{
		// result 아이템 = {TYPE : table , x, y, width,  colWidth:[],  header:[[],[]], data: [[],[]...] }
		// header 	= [ [  { colIndex, rowIndex, text, width, colSpan, rowSpan, textAlign, verticalAlign }  ],[]  ]
		// data 	= [ [],[].... ]
		boolean _useHeaderBand = false;
		HashMap<String, Object> _resultItem = null;
		float _fontSize = 0;
		int _headerRowLength = 0;
		
		ArrayList<ArrayList<HashMap<String, String>>> _allRows;
		
		_resultItem = getHeaderBandItem( _item, _headerBandItems );
		
		// 헤더밴드 유무 판단
		if( _resultItem != null )
		{
			_allRows = (ArrayList<ArrayList<HashMap<String, String>>>) _resultItem.get("data");
			_useHeaderBand = true;
			_fontSize = Float.valueOf( _resultItem.get("fontSize").toString() );
			_headerRowLength = _allRows.size();
		}
		else
		{
			_resultItem = new HashMap<String, Object>();
			_allRows = new ArrayList<ArrayList<HashMap<String, String>>>();
			_useHeaderBand = false;
			_headerRowLength = 0;
			
		}
		
		// 아이템에서 rows 에 담긴 셀 아이템을 가져와서 그린다.
		ArrayList<ArrayList<HashMap<String, Object>>> rowAr = (ArrayList<ArrayList<HashMap<String, Object>>>) _item.get("rows");
		ArrayList<Float> _rowHeightAr = (ArrayList<Float>) _item.get("heightInfo");		// Row의 Height List
		int rowSize = rowAr.size();
		
		// 1. row / column 만큼 돌면서 아이템 속성추출 ( text )
		// 2. 병합된 셀 처리 방안 필요 
		HashMap<String, Object> _tmpCell;
		HashMap<String, String> _cellItem;
		float _itemFontSize = 0;
		int _colCount = Integer.parseInt( _item.get("columnCount").toString() );
		
		int i = 0;
		int _colIdx = 0;
		int _colSpan = 0;
		int _rowSpan = 0;
		
		HashMap<String, String> _noneItem = new HashMap<String, String>();
		HashMap<String, String> _colspanItem = new HashMap<String, String>();
		_noneItem.put("type", "none");
		_colspanItem.put("type", "colspan");
		// type : colspan 
		
		HashMap<Integer, Integer> _rowSpanCheckMap = new HashMap<Integer, Integer>();
		
		//rowSpan 인덱스 담기 ( rowSpan값과 colIndex 값 담아두기( colidx + colSpan -1 ) )
		ArrayList<Integer> _rowSpanInfo = new ArrayList<Integer>();
		HashMap<Integer, Integer> _rowSpanOrizianlInfo = new HashMap<Integer, Integer>();

		for(i=0; i < _colCount; i++ )
		{
			_rowSpanInfo.add(0);
		}
		
		for( int r = 0; r < rowSize; r++ )
		{
			ArrayList<HashMap<String, String>> _rowItems = new ArrayList<HashMap<String, String>>();
			int _colSize = rowAr.get(r).size();
			
			for( i = 0; i < _colCount; i++)
			{
				if( _rowSpanInfo.get(i) > 0 )
				{
					_rowItems.add( _noneItem );
				}
				else
				{
					_rowItems.add(_colspanItem);
				}
			}
			
			int _textLine = 0;
			int _currentTextLine = 0;
			boolean _useUpdateRowspan = true;
			
			for( i =0; i < _colSize; i++)
			{
				_tmpCell = rowAr.get(r).get(i);
				_colIdx = Integer.parseInt(_tmpCell.get("colIndex").toString());
				_cellItem = convertCell(_tmpCell);
				
				_rowSpan = Integer.parseInt(_cellItem.get("rowSpan").toString());
				_colSpan = Integer.parseInt(_cellItem.get("colSpan").toString());
				
				_itemFontSize = Float.valueOf(_cellItem.get("fontSize"));
				
				if( Float.compare(_itemFontSize, -1) != 0 && ( _fontSize == 0 || _fontSize > _itemFontSize ) )
				{
					_fontSize = _itemFontSize;
				}
				
				//if( _rowSpan > 1 ) _useUpdateRowspan = false;
				if( _rowSpan == 1 )
				{
					_currentTextLine = _cellItem.get("text").split("\n").length;
					if( _currentTextLine > _textLine ) _textLine = _currentTextLine;
				}
				
				_rowItems.set(_colIdx, _cellItem);
				
				if( _rowSpan > 1 )
				{
					for( int j = 0; j < _colSpan; j++ )
					{
						_rowSpanInfo.set(_colIdx + j, _rowSpan);
					}
					_rowSpanOrizianlInfo.put(_colIdx, r+_headerRowLength);
				}
			}
			
			// 병합되지 않은 셀의 텍스트가 줄바꿈이 있을경우 셀이 RowSpan의 값을 줄바꿈된 라인수만큼 증가시켜준다.
			if(_textLine > 1 && _useUpdateRowspan )
			{
				_textLine = Double.valueOf( Math.floor(_textLine / 2 ) + 1).intValue();
				int _originalRowSpan = 1;
				
				for(i = 0; i < _rowItems.size(); i++ )
				{
					if( _rowItems.get(i).get("type").equals("none") && _rowSpanOrizianlInfo.containsKey(i) )
					{
						_originalRowSpan = Integer.parseInt( _allRows.get(_rowSpanOrizianlInfo.get(i)).get(i).get("rowSpan") );
						_allRows.get(_rowSpanOrizianlInfo.get(i)).get(i).put("rowSpan", String.valueOf(  _originalRowSpan + (_textLine-1)) );
					}
					else if( _rowItems.get(i).containsKey("rowSpan") )
					{
						int  _itemRowSpan = Integer.valueOf(_rowItems.get(i).get("rowSpan").toString());
						if(_itemRowSpan > 1)
						{
							_rowItems.get(i).put("rowSpan", String.valueOf(_itemRowSpan + _textLine-1) );
						}
						else
						{
							_rowItems.get(i).put("rowSpan", String.valueOf( _textLine) );
						}
					}
				}
			}
			// RowSpan된 컬럼의 인덱스에 RowSpan 값을 1씩 제거
			for(i=0; i < _colCount; i++ )
			{
				if( _rowSpanInfo.get(i)>0) _rowSpanInfo.set(i, _rowSpanInfo.get(i)-1);
				
				if( _rowSpanInfo.get(i) == 0 && _rowSpanOrizianlInfo.containsKey(i) )
				{
					_rowSpanOrizianlInfo.remove(i);
				}
			}
			
			_allRows.add(_rowItems);
		}
		
		_resultItem.put("className", _item.get("className"));
		_resultItem.put("data", _allRows);
		_resultItem.put("x", _item.get("x"));
		_resultItem.put("fontSize", _fontSize);
		
		if(!_useHeaderBand)
		{
			_resultItem.put("y", _item.get("y"));
			_resultItem.put("columnCount", _item.get("columnCount"));
			_resultItem.put("rowCount", _item.get("rowCount") );
			_resultItem.put("widthInfo", _item.get("widthInfo"));
			return _resultItem;
		}
		else
		{
			_resultItem.put("rowCount", Integer.parseInt( _resultItem.get("rowCount").toString())  + Integer.parseInt( _item.get("rowCount").toString() )  );
			return null;
		}
		
	}
	
	private HashMap<String, String> convertCell( HashMap<String, Object> _cell )
	{
		HashMap<String, String> _retItem = new HashMap<String, String>();
		
		if( _cell == null )
		{
			return null;
		}
		
		String _cellText = _cell.get("text").toString().replaceAll("\\n","\n").replaceAll("\\\\n","\n");
		
		String _colSpan = (_cell.containsKey("colSpan"))? _cell.get("colSpan").toString() : "1";
		String _rowSpan = (_cell.containsKey("rowSpan"))? _cell.get("rowSpan").toString() : "1";
		String _textAlign =  (_cell.containsKey("textAlign"))? _cell.get("textAlign").toString() : "center";
		String _verticalAlign =  (_cell.containsKey("verticalAlign"))? _cell.get("verticalAlign").toString() : "top";
		String _fontSize = ( _cell.containsKey("CELL_TYPE") &&_cell.get("CELL_TYPE").toString().equals("EMPTY_CELL"))?"-1":_cell.get("fontSize").toString();
		
		_retItem.put("type", "ITEM");
		_retItem.put("colIndex", _cell.get("colIndex").toString());
		_retItem.put("rowIndex", _cell.get("rowIndex").toString());
		_retItem.put("text",	_cellText );
		_retItem.put("width", 	_cell.get("width").toString());
		_retItem.put("colSpan", _colSpan);
		_retItem.put("rowSpan", _rowSpan);
		_retItem.put("fontSize", _fontSize);

		_retItem.put("textAlign", 		_textAlign);
		_retItem.put("verticalAlign", 	_verticalAlign);

		_retItem.put("borderLeft", 	"solid");
		_retItem.put("borderRight",	"solid");
		_retItem.put("borderTop", 	"solid");
		_retItem.put("borderBottom", "solid");
		
		ArrayList<String> _borderSides = new ArrayList<String>();
		ArrayList<String> _borderTypes = new ArrayList<String>();
		String _borderType = "";
		String _borderSide = "";
		String _borderSideName = "";
		
		if( _cell.containsKey("borderSide") && _cell.get("borderSide") != null ) _borderSides = (ArrayList<String>) _cell.get("borderSide");
		if( _cell.containsKey("borderTypes") && _cell.get("borderTypes") != null ) _borderTypes = (ArrayList<String>) _cell.get("borderTypes");

//		if( _cell.containsKey("borderOriginalTypes") && _cell.get("borderOriginalTypes") != null ) _borderTypes = (ArrayList<String>) _cell.get("borderOriginalTypes");
//		else if( _cell.containsKey("borderTypes") && _cell.get("borderTypes") != null ) _borderTypes = (ArrayList<String>) _cell.get("borderTypes");
		
		// cell의 Border 지정
		for( int i = 0; i < _borderSides.size(); i++ )
		{	
			if( _borderTypes.get(i).equals("none") ) _borderType = "none";
			else _borderType = "solid";
			
			_borderSideName = _borderSides.get(i);
			
			if( _borderSideName.equals("left") )
			{
				_borderSide = "borderLeft";
			}
			else if( _borderSideName.equals("top") )
			{
				_borderSide = "borderTop";
			}
			else if( _borderSideName.equals("right") )
			{
				_borderSide = "borderRight";
			}
			else if( _borderSideName.equals("bottom") )
			{
				_borderSide = "borderBottom";
			}
			
			_retItem.put(_borderSide, _borderType);
		}
		
		return _retItem;
	}
	
	
	private HashMap<String, Object> convertLabel( HashMap<String, Object> _label )
	{
		HashMap<String, Object> _retItem = new HashMap<String, Object>();
		
		_retItem.put("className", _label.get("className"));
		_retItem.put("text", _label.get("text").toString().replaceAll("\n", "\r\n").replaceAll("\\n", "\r\n") );
		_retItem.put("width", _label.get("width").toString());
		_retItem.put("x", _label.get("x").toString());
		_retItem.put("y", _label.get("y").toString());
		
		return  _retItem;
	}
	
	
	
	/**
	 * Floor( Object to String to Double ) to Integer
	 * @param o : numberValue
	 * @return Integer number 
	 * */
	private int toNum( Object o ){
		if( o == null ){
			return -1;
		}
		else if( o instanceof Integer){
			return (Integer) o;
		}
		else{
			String oVal = String.valueOf(o).trim();
			
			if( oVal.equals("") ) return -1;
			
			if( oVal.contains(".") ){
				return (int) Math.floor(Double.parseDouble(oVal));
			}
			else{
				return Integer.valueOf(oVal);
			}
		}
	}
	
	
    private int measureTextWidth(String text)
	{
    	Font font = new Font("GulimChe", Font.PLAIN, 12);
    	
		AffineTransform affinetransform = new AffineTransform();     
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
		
		int textwidth = (int)(font.getStringBounds(text, frc).getWidth());

		return textwidth;
	}
    
	
}
