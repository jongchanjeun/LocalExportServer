package org.ubstorm.service.parser.txt;

import java.util.ArrayList;
import java.util.HashMap;


public class PlainTextTableParser {

	private int columnCount=0;
	
	private int getAlignValue(String _textAlign , String _verticalAlgin)
	{
		int result = Block.DATA_MIDDLE_LEFT;

		if( _textAlign.equalsIgnoreCase("left") && _verticalAlgin.equalsIgnoreCase("top") ){
			result=Block.DATA_TOP_LEFT;
		}else if( _textAlign.equalsIgnoreCase("left") && _verticalAlgin.equalsIgnoreCase("middle") ){
			result=Block.DATA_MIDDLE_LEFT;
		}else if( _textAlign.equalsIgnoreCase("left") && _verticalAlgin.equalsIgnoreCase("bottom") ){
			result=Block.DATA_BOTTOM_LEFT;
		}else if( _textAlign.equalsIgnoreCase("center") && _verticalAlgin.equalsIgnoreCase("top") ){
			result=Block.DATA_TOP_MIDDLE;
		}else if( _textAlign.equalsIgnoreCase("center") && _verticalAlgin.equalsIgnoreCase("middle") ){
			result=Block.DATA_CENTER;
		}else if( _textAlign.equalsIgnoreCase("center") && _verticalAlgin.equalsIgnoreCase("bottom") ){
			result=Block.DATA_BOTTOM_MIDDLE;
		}else if( _textAlign.equalsIgnoreCase("right") && _verticalAlgin.equalsIgnoreCase("top") ){
			result=Block.DATA_TOP_RIGHT;
		}else if( _textAlign.equalsIgnoreCase("right") && _verticalAlgin.equalsIgnoreCase("middle") ){
			result=Block.DATA_MIDDLE_RIGHT;
		}else if( _textAlign.equalsIgnoreCase("right") && _verticalAlgin.equalsIgnoreCase("bottom") ){
			result=Block.DATA_BOTTOM_RIGHT;
		}
			
		return result;
	}
	
	
	
	
	
//	
//	ArrayList<ArrayList<HashMap<String, String>>>
//	hashmap 구조
//	 { colIndex, rowIndex, text, width, colSpan, rowSpan, textAlign, verticalAlign }
//
//	
	
	public String getTableText(ArrayList<ArrayList<HashMap<String, String>>> rows, int _tblWidth )
	{
		String result="";
		
		// cell width * column count + 전체 라인수. 
//		final int BOARD_WIDTH= (CELL_WIDTH*columnCount)+(columnCount+1);	// test value
		final int BOARD_WIDTH= _tblWidth+(columnCount+1);	// test value
		
		Board board = new Board(BOARD_WIDTH);
		
		
		
		ArrayList<HashMap<String, String>> _row = null;
		HashMap<String, String> _cell      = null;
		
		String text=null;
		int colSpan=-1;
		int rowSpan=-1;
		int cellwidth = -1;
		
		String textAlign="left";
		String verticalAlign="middle";
		String type="";
		Boolean hasNone=false;
		int align=Block.DATA_MIDDLE_LEFT;
		
		String borderLeft="";
		String borderRight="";
		String borderTop="";
		String borderBottom="";
		
		// add target
		Block _target=null;
		
		// add left target
		Block _firstLeftTarget=null;
		
		// 병합 처리를 위해 block를 담아둔다.
		ArrayList<Block[]> tempRows=new ArrayList<Block[]>();
		
		Block block=null;
		
		for( int rowIndex=0; rowIndex<rows.size(); rowIndex++ ){
			_row = rows.get(rowIndex);
			
			Block[] tempRow=new Block[_row.size()];
			
			for( int cellIndex=0; cellIndex<_row.size(); cellIndex++ ){
				_cell = _row.get(cellIndex);
				
				type = _cell.get("type");
				
				
				if( type != null && type.equals("none")){
					tempRow[cellIndex] = getMatrixBlock(tempRows,rowIndex-1,cellIndex);
					hasNone=true;
					continue;
				}
				
				if( type != null && type.equals("colspan")){
					tempRow[cellIndex] = getRowBlock(tempRow,cellIndex-1);
					continue;
				}
				
				
				text=_cell.get("text");
				colSpan=Integer.parseInt(_cell.get("colSpan"));
				rowSpan=Integer.parseInt(_cell.get("rowSpan"));
				cellwidth=Integer.parseInt(_cell.get("CELL_WIDTH"));
				textAlign = _cell.get("textAlign");
				verticalAlign = _cell.get("verticalAlign");
				
				borderLeft = _cell.get("borderLeft");
				borderRight = _cell.get("borderRight");
				borderTop = _cell.get("borderTop");
				borderBottom = _cell.get("borderBottom");
				
				align = getAlignValue(textAlign,verticalAlign);

				// row height
				rowSpan = rowSpan == 1 ? 1 : (rowSpan*2)-1;
				
				block = new Block(board, (cellwidth)+(colSpan-1), rowSpan, text); 
				block.setBorderLeft(borderLeft);
				block.setBorderRight(borderRight);
				block.setBorderTop(borderTop);
				block.setBorderBottom(borderBottom);
				
				
				if( rowIndex==0 && cellIndex==0 ){
					
					board.setInitialBlock(block.setDataAlign(align));
					
					_target=block;
					_firstLeftTarget=block;
					
				}else if( cellIndex==0 ){

					if( hasNone==true ){
						hasNone=false;
						_firstLeftTarget=getMatrixBlock(tempRows,rowIndex-1,cellIndex);
						_firstLeftTarget.setBelowBlock(block.setDataAlign(align));
					}else{
						_firstLeftTarget.setBelowBlock(block.setDataAlign(align));
					}
					
					_target=block;
					_firstLeftTarget=block;
					
				}else{
					
					if( hasNone==true ){
						hasNone=false;
						_target=getMatrixBlock(tempRows,rowIndex-1,cellIndex);
						_target.setBelowBlock(block.setDataAlign(align));
					}else{
						_target.setRightBlock(block.setDataAlign(align));
					}
					
					_target=block;
				}
				
				tempRow[cellIndex] = _target;
			}
			tempRows.add(tempRow);
		}
		
		
		result =board.invalidate().build().getPreview();
		
		
		return result;
	}

	
	public Block getRowBlock( Block[] tempRow  , int columnIndex )
	{
		Block block=null;

		int _index=columnIndex;
		while(true)
		{
			block=tempRow[_index];
			if( block != null ) {
				break;
			}else{
				_index--;
			}
		}
		
		return block;
	}
	

	public Block getMatrixBlock( ArrayList<Block[]> tempRows , int rowIndex , int columnIndex )
	{
		Block block=null;
		
		Block[] _row=tempRows.get(rowIndex);
		
		block=_row[columnIndex];
		
		return block;
	}


	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}
	
}
