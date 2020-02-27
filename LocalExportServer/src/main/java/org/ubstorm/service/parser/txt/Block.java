/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubstorm.service.parser.txt;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *
 * @author Thedath Oudarya
 */
public final class Block {

    protected static int nextIndex = 0;

    private Board board;

    private final int index;

    private int width;

    private int height;

    private boolean allowGrid;

    private int blockAlign;

    public static final int BLOCK_LEFT = 1;

    public static final int BLOCK_CENTRE = 2;

    public static final int BLOCK_RIGHT = 3;

    private String data;

    private int dataAlign;

    public static final int DATA_TOP_LEFT = 4;

    public static final int DATA_TOP_MIDDLE = 5;

    public static final int DATA_TOP_RIGHT = 6;

    public static final int DATA_MIDDLE_LEFT = 7;

    public static final int DATA_CENTER = 8;

    public static final int DATA_MIDDLE_RIGHT = 9;

    public static final int DATA_BOTTOM_LEFT = 10;

    public static final int DATA_BOTTOM_MIDDLE = 11;

    public static final int DATA_BOTTOM_RIGHT = 12;

    private int x;

    private int y;

    private Block rightBlock;

    private Block belowBlock;

    private List<Charr> charrsList;

    private String preview;
    
    private String borderLeft="solid";
    private String borderRight="solid"; 
    private String borderTop="solid";
    private String borderBottom="solid"; 

    public Block(Board board, int width, int height) {
        this.board = board;
        if (width <= board.boardWidth) {
            this.width = width;
        } else {
            throw new RuntimeException("Block " + toString() + " exceeded the board width " + board.boardWidth);
        }
        this.height = height;
        this.allowGrid = true;
        this.blockAlign = BLOCK_LEFT;
        this.data = null;
        this.dataAlign = DATA_TOP_LEFT;
        this.x = 0;
        this.y = 0;
        this.rightBlock = null;
        this.belowBlock = null;
        this.charrsList = new ArrayList<Charr>();
        this.preview = "";
        this.index = nextIndex;
        Block.nextIndex++;
    }

    public Block(Board board, int width, int height, String data) {
        this(board, width, height);
        this.data = data;
    }

    public Block(Board board, int width, int height, String data, Block rightBlock, Block belowBlock) {
        this(board, width, height, data);
        if (rightBlock != null) {
            rightBlock.setX(getX() + getWidth() + (isGridAllowed() ? 1 : 0));
            rightBlock.setY(getY());
            this.rightBlock = rightBlock;
        }
        if (belowBlock != null) {
            belowBlock.setX(getX());
            belowBlock.setY(getY() + getHeight() + (isGridAllowed() ? 1 : 0));
            this.belowBlock = belowBlock;
        }
    }

    protected int getIndex() {
        return index;
    }

    public int getWidth() {
        return width;
    }

    public Block setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Block setHeight(int height) {
        this.height = height;
        return this;
    }

    public boolean isGridAllowed() {
        return allowGrid;
    }

    public Block allowGrid(boolean allowGrid) {
        this.allowGrid = allowGrid;
        return this;
    }

    public int getBlockAlign() {
        return blockAlign;
    }

    public Block setBlockAlign(int blockAlign) {
        if (blockAlign == BLOCK_LEFT || blockAlign == BLOCK_CENTRE || blockAlign == BLOCK_RIGHT) {
            this.blockAlign = blockAlign;
        } else {
            throw new RuntimeException("Invalid block align mode. " + dataAlign + " given.");
        }
        return this;
    }

    public String getData() {
        return data;
    }

    public Block setData(String data) {
        this.data = data;
        return this;
    }

    public int getDataAlign() {
        return dataAlign;
    }

    public Block setDataAlign(int dataAlign) {
        if (dataAlign == DATA_TOP_LEFT || dataAlign == DATA_TOP_MIDDLE || dataAlign == DATA_TOP_RIGHT
                || dataAlign == DATA_MIDDLE_LEFT || dataAlign == DATA_CENTER || dataAlign == DATA_MIDDLE_RIGHT
                || dataAlign == DATA_BOTTOM_LEFT || dataAlign == DATA_BOTTOM_MIDDLE || dataAlign == DATA_BOTTOM_RIGHT) {
            this.dataAlign = dataAlign;
        } else {
            throw new RuntimeException("Invalid data align mode. " + dataAlign + " given.");
        }
        return this;
    }

    protected int getX() {
        return x;
    }

    protected Block setX(int x) {
        if (x + getWidth() + (isGridAllowed() ? 2 : 0) <= board.boardWidth) {
            this.x = x;
        } else {
            throw new RuntimeException("Block " + toString() + " exceeded the board width " + board.boardWidth);
        }
        return this;
    }

    protected int getY() {
        return y;
    }

    protected Block setY(int y) {
        this.y = y;
        return this;
    }

    public Block getRightBlock() {
        return rightBlock;
    }

    public Block setRightBlock(Block rightBlock) {
        if (rightBlock != null) {
            rightBlock.setX(getX() + getWidth() + (isGridAllowed() ? 1 : 0));
            rightBlock.setY(getY());
            this.rightBlock = rightBlock;
        }
        return this;
    }

    public Block getBelowBlock() {
        return belowBlock;
    }

    public Block setBelowBlock(Block belowBlock) {
        if (belowBlock != null) {
            belowBlock.setX(getX());
            belowBlock.setY(getY() + getHeight() + (isGridAllowed() ? 1 : 0));
            this.belowBlock = belowBlock;
        }
        return this;
    }

    protected Block invalidate() {
        charrsList = new ArrayList<Charr>();
        preview = "";
        return this;
    }

    protected Block build() {
        if (charrsList.isEmpty()) {
            int ix = x;
            int iy = y;
            int blockLeftSideSpaces = -1;
            int additionalWidth = (isGridAllowed() ? 2 : 0);
            switch (getBlockAlign()) {
                case BLOCK_LEFT: {
                    blockLeftSideSpaces = 0;
                    break;
                }
                case BLOCK_CENTRE: {
                    blockLeftSideSpaces = (board.boardWidth - (ix + getWidth() + additionalWidth)) / 2 + (board.boardWidth - (ix + getWidth() + additionalWidth)) % 2;
                    break;
                }
                case BLOCK_RIGHT: {
                    blockLeftSideSpaces = board.boardWidth - (ix + getWidth() + additionalWidth);
                    break;
                }
            }
            ix += blockLeftSideSpaces;
            if (data == null) {
                data = toString();
            }
            String[] lines = data.split("\n");
            List<String> dataInLines = new ArrayList<String>();
            if (board.showBlockIndex) {
                dataInLines.add("i = " + index);
            }
            for (String line : lines) {
                if (getHeight() > dataInLines.size()) {
                    dataInLines.add(line);
                } else {
                    break;
                }
            }
            for (int i = dataInLines.size(); i < getHeight(); i++) {
                dataInLines.add("");
            }
            for (int i = 0; i < dataInLines.size(); i++) {
                String dataLine = dataInLines.get(i);
                if (dataLine.length() > getWidth()) {
                    dataInLines.set(i, dataLine.substring(0, getWidth()));
                    if (i + 1 != dataInLines.size()) {
                        String prifix = dataLine.substring(getWidth(), dataLine.length());
                        String suffix = dataInLines.get(i + 1);
                        String combinedValue = prifix.concat((suffix.length() > 0 ? String.valueOf(Charr.S) : "")).concat(suffix);
                        dataInLines.set(i + 1, combinedValue);
                    }
                }
            }

            for (int i = 0; i < dataInLines.size(); i++) {
                if (dataInLines.remove("")) {
                    i--;
                }
            }

            int givenAlign = getDataAlign();
            int dataStartingLineIndex = -1;
            int additionalHeight = (isGridAllowed() ? 1 : 0);
            if (givenAlign == DATA_TOP_LEFT || givenAlign == DATA_TOP_MIDDLE || givenAlign == DATA_TOP_RIGHT) {
                dataStartingLineIndex = iy + additionalHeight;
            } else if (givenAlign == DATA_MIDDLE_LEFT || givenAlign == DATA_CENTER || givenAlign == DATA_MIDDLE_RIGHT) {
                dataStartingLineIndex = iy + additionalHeight + ((getHeight() - dataInLines.size()) / 2 + (getHeight() - dataInLines.size()) % 2);
            } else if (givenAlign == DATA_BOTTOM_LEFT || givenAlign == DATA_BOTTOM_MIDDLE || givenAlign == DATA_BOTTOM_RIGHT) {
                dataStartingLineIndex = iy + additionalHeight + (getHeight() - dataInLines.size());
            }
            int dataEndingLineIndex = dataStartingLineIndex + dataInLines.size();

            int extendedIX = ix + getWidth() + (isGridAllowed() ? 2 : 0);
            int extendedIY = iy + getHeight() + (isGridAllowed() ? 2 : 0);
            int startingIX = ix;
            int startingIY = iy;
            int _gapLineDataLength = 0;
            		
            for (; iy < extendedIY; iy++) {
                for (; ix < extendedIX; ix++) {
                    boolean writeData;
                    if (isGridAllowed()) {
                        if ((iy == startingIY) || (iy == extendedIY - 1)) {
                        	
                        	if( (iy == startingIY) ){
                        		
                        		if( this.borderTop.equals("none") ){
                        			
                        			if ((ix == startingIX)) {
                                		if( this.borderLeft.equals("solid") ){
                                            charrsList.add(new Charr(ix, iy, Charr.P));
                                            writeData = false;
                                		}
                                    }

                        			if ( (ix == extendedIX - 1)) {
                                		if( this.borderRight.equals("solid") ){
                                            charrsList.add(new Charr(ix, iy, Charr.P));
                                            writeData = false;
                                		}
                                    }
                        			continue;
                        		}
                        		//System.out.println("위");
                        	}
                        	
                        	if( (iy == extendedIY - 1) ){
                        		if( this.borderBottom.equals("none") ){
                        			
                        			
                        			if ((ix == startingIX)) {
                                		if( this.borderLeft.equals("solid") ){
                                            charrsList.add(new Charr(ix, iy, Charr.P));
                                            writeData = false;
                                		}
                                    }

                        			if ( (ix == extendedIX - 1)) {
                                		if( this.borderRight.equals("solid") ){
                                            charrsList.add(new Charr(ix, iy, Charr.P));
                                            writeData = false;
                                		}
                                    }
                        			
                        			continue;
                        		}
                        		//System.out.println("아래");
                        	}
                        	
                        	
                            if ((ix == startingIX) || (ix == extendedIX - 1)) {
                                charrsList.add(new Charr(ix, iy, Charr.P));
                                writeData = false;
                            } else {
                                charrsList.add(new Charr(ix, iy, Charr.D));
                                writeData = false;
                            }
                        } else {
                            if ((ix == startingIX) || (ix == extendedIX - 1)) {
                            	
                            	
                            	if( (ix == startingIX) ){
                            		if( this.borderLeft.equals("none") ){
                            			continue;
                            		}
                            		//System.out.println("왼쪽");
                            	}
                            	
                            	if( (ix == extendedIX - 1) ){
                            		if( this.borderRight.equals("none") ){
                            			continue;
                            		}
                            		//System.out.println("오른쪽");
                            	}
                            	
                            	
                                charrsList.add(new Charr(ix, iy, Charr.VL));
                            	//charrsList.add(new Charr(ix, iy, Charr.S));
                            	
                            	writeData = false;
                            } else {
                                writeData = true;
                            }
                        }
                    } else {
                        writeData = true;
                    }
                    if (writeData && (iy >= dataStartingLineIndex && iy < dataEndingLineIndex)) {
                        int dataLineIndex = iy - dataStartingLineIndex;
                        String lineData = dataInLines.get(dataLineIndex);
                        //System.out.println(dataLineIndex + ":lineData=" + lineData);
                        int _realLineDataLength = getLineDataLength(lineData);
                        _gapLineDataLength = _realLineDataLength - lineData.length();
                        //System.out.println(dataLineIndex + ":_realLineDataLength=" + _realLineDataLength);
                        
                        if (!lineData.isEmpty()) {
                            int dataLeftSideSpaces = -1;
                            
                            if (givenAlign == DATA_TOP_LEFT || givenAlign == DATA_MIDDLE_LEFT || givenAlign == DATA_BOTTOM_LEFT) {
                                dataLeftSideSpaces = 0;
                            } else if (givenAlign == DATA_TOP_MIDDLE || givenAlign == DATA_CENTER || givenAlign == DATA_BOTTOM_MIDDLE) {
                                //dataLeftSideSpaces = (getWidth() - lineData.length()) / 2 + (getWidth() - lineData.length()) % 2;
                                dataLeftSideSpaces = (getWidth() - getLineDataLength(lineData)) / 2 + (getWidth() - getLineDataLength(lineData)) % 2;
                            } else if (givenAlign == DATA_TOP_RIGHT || givenAlign == DATA_MIDDLE_RIGHT || givenAlign == DATA_BOTTOM_RIGHT) {
                                //dataLeftSideSpaces = getWidth() - lineData.length();
                                dataLeftSideSpaces = getWidth() - getLineDataLength(lineData);
                            }
                            int dataStartingIndex = (startingIX + dataLeftSideSpaces + (isGridAllowed() ? 1 : 0));
                            int dataEndingIndex = (startingIX + dataLeftSideSpaces + lineData.length() - (isGridAllowed() ? 0 : 1));
                            int realDataEndingIndex = (startingIX + dataLeftSideSpaces + getLineDataLength(lineData) - (isGridAllowed() ? 0 : 1));
                            //System.out.println(dataLineIndex + ":ix=" + ix + ",dataStartingIndex=" + dataStartingIndex + ",dataEndingIndex=" + dataEndingIndex);
                            if (ix >= dataStartingIndex && ix <= realDataEndingIndex) {
                                char charData = ix <= dataEndingIndex ? lineData.charAt(ix - dataStartingIndex) : '^';
                                charrsList.add(new Charr(ix, iy, charData));
                                //System.out.println("(dataLineIndex=" + dataLineIndex + ",ix=" + ix + "):charData=" + Character.toString(charData));
                            }
                        }
                    }
                }
                ix = startingIX;
            }
        }
        return this;
    }
    
    
    private int getLineDataLength(String lineData)
    {
    	int dl = lineData.length();
    	int textLength = 0;
    	
    	int spaceWidth = measureTextWidth(" ");
    	int charWidth = measureTextWidth("가");
    	int ratioWidth = Math.round(charWidth/spaceWidth);
    	//System.out.println("spaceWidth=" + spaceWidth + ", charWidth=" + charWidth + ", ratioWidth=" + ratioWidth);
    	
    	for(int i=0 ; i< dl; i++)
    	{
    		textLength += getHangleType(lineData.charAt(i)) ? ratioWidth : 1;
    	}
        	
    	return textLength;
    }
    
    private boolean getHangleType(char chart) {
        return Pattern.matches("^[\uAC00-\uD7A3\u3131-\u314e\u314f-\u3163\u2000-\u2BFF\u3200-\u32FF\u00AE-\u00BF\u0391-\u03A9\u03B1-\u03C9]*$", Character.toString(chart));
    }
    
    private int measureTextWidth(String text)
	{
    	Font font = new Font("GulimChe", Font.PLAIN, 12);
    	
		AffineTransform affinetransform = new AffineTransform();     
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
		
		int textwidth = (int)(font.getStringBounds(text, frc).getWidth());

		return textwidth;
	}

    protected List<Charr> getChars() {
        return this.charrsList;
    }

    public String getPreview() {
        build();
        if (preview.isEmpty()) {
            int maxY = -1;
            int maxX = -1;
            for (Charr charr : charrsList) {
                int testY = charr.getY();
                int testX = charr.getX();
                if (maxY < testY) {
                    maxY = testY;
                }
                if (maxX < testX) {
                    maxX = testX;
                }
            }
            String[][] dataPoints = new String[maxY + 1][board.boardWidth];
            for (Charr charr : charrsList) {
                dataPoints[charr.getY()][charr.getX()] = String.valueOf(charr.getC());
            }

            for (String[] dataPoint : dataPoints) {
                for (String point : dataPoint) {
                    if (point == null) {
                        point = String.valueOf(Charr.S);
                    }
                    preview = preview.concat(point);
                }
                preview = preview.concat(String.valueOf(Charr.NR)+String.valueOf(Charr.NL));
            }
        }
        return preview;
    }

    public Block getMostRightBlock() {
        return getMostRightBlock(this);
    }

    private Block getMostRightBlock(Block block) {
        if (block.getRightBlock() == null) {
            return block;
        } else {
            return getMostRightBlock(block.getRightBlock());
        }
    }

    public Block getMostBelowBlock() {
        return getMostBelowBlock(this);
    }

    private Block getMostBelowBlock(Block block) {
        if (block.getBelowBlock() == null) {
            return block;
        } else {
            return getMostBelowBlock(block.getBelowBlock());
        }
    }

    @Override
    public String toString() {
        return index + " = [" + x + "," + y + "," + width + "," + height + "]";
    }

    @Override
    public boolean equals(Object block) {
        if (block == null) {
            return false;
        }
        if (!(block instanceof Block)) {
            return false;
        }
        Block b = (Block) block;
        return b.getIndex() == getIndex() && b.getX() == getX() && b.getY() == getY();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + this.index;
        hash = 43 * hash + this.width;
        hash = 43 * hash + this.height;
        hash = 43 * hash + (this.allowGrid ? 1 : 0);
        hash = 43 * hash + this.blockAlign;
        hash = 43 * hash + Objects.hashCode(this.data);
        hash = 43 * hash + this.dataAlign;
        hash = 43 * hash + this.x;
        hash = 43 * hash + this.y;
        hash = 43 * hash + Objects.hashCode(this.rightBlock);
        hash = 43 * hash + Objects.hashCode(this.belowBlock);
        return hash;
    }
    
    
	public String getBorderLeft() {
		return borderLeft;
	}

	public void setBorderLeft(String borderLeft) {
		this.borderLeft = borderLeft;
	}

	public String getBorderRight() {
		return borderRight;
	}

	public void setBorderRight(String borderRight) {
		this.borderRight = borderRight;
	}

	public String getBorderTop() {
		return borderTop;
	}

	public void setBorderTop(String borderTop) {
		this.borderTop = borderTop;
	}

	public String getBorderBottom() {
		return borderBottom;
	}

	public void setBorderBottom(String borderBottom) {
		this.borderBottom = borderBottom;
	}
	
}
