package org.ubstorm.service.parser;

/** 
 * <pre>
 * desc      : pixel to excel width.
 * ref			: http://ragnarock99.blogspot.kr/2012/05/getting-cell-witdth-from-excel-with.html
 * 
 * Description of how column widths are determined in Excel
 * https://support.microsoft.com/en-us/kb/214123
 * </pre>
 * 
 * */
class PixelUtil {
    public static final short EXCEL_COLUMN_WIDTH_FACTOR = 230;//256; 
    public static final short EXCEL_ROW_HEIGHT_FACTOR = 20; 
    public static final int UNIT_OFFSET_LENGTH = 7; 
    public static final int[] UNIT_OFFSET_MAP = new int[] { 0, 36, 73, 109, 146, 182, 219 };
    
    public static int pixel2WidthUnits(int pxs) {
    	short widthUnits = (short) (EXCEL_COLUMN_WIDTH_FACTOR * (pxs / UNIT_OFFSET_LENGTH)); 
        widthUnits += UNIT_OFFSET_MAP[(pxs % UNIT_OFFSET_LENGTH)];  
        return widthUnits; 
    } 
    public static int widthUnits2Pixel(short widthUnits) {
        int pixels = (widthUnits / EXCEL_COLUMN_WIDTH_FACTOR) * UNIT_OFFSET_LENGTH; 
        int offsetWidthUnits = widthUnits % EXCEL_COLUMN_WIDTH_FACTOR; 
        pixels += Math.floor((float) offsetWidthUnits / ((float) EXCEL_COLUMN_WIDTH_FACTOR / UNIT_OFFSET_LENGTH));   
        return pixels; 
    }
    public static int heightUnits2Pixel(short heightUnits) {
        int pixels = (heightUnits / EXCEL_ROW_HEIGHT_FACTOR); 
        int offsetWidthUnits = heightUnits % EXCEL_ROW_HEIGHT_FACTOR; 
        pixels += Math.floor((float) offsetWidthUnits / ((float) EXCEL_ROW_HEIGHT_FACTOR / UNIT_OFFSET_LENGTH));   
        return pixels; 
    }
 }
