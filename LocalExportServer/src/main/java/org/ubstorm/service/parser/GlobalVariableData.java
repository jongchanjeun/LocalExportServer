package org.ubstorm.service.parser;


public class GlobalVariableData {

	public final static String GRANDSUMMARY_TYPE_ALL = "all";
	
	public final static String CLONE_PAGE_NORMAL 		= "0";
	public final static String CLONE_PAGE_HORIZONTAL 	= "1";
	public final static String CLONE_PAGE_VERTICAL 		= "2";
	public final static String CLONE_PAGE_CUSTOM 		= "3";
	
	public final static String CLONE_DIRECT_ACROSS_DOWN = "crossDown";
	public final static String CLONE_DIRECT_DOWN_CROSS = "downCross";
	
	// 엑셀 저장시에 값을 true로 변경
	public static Boolean mExcelFlag = true;		 
	
	// 유비폼에서 사용되는 시스템파라미터명을 담은 배열
	public static String[] mSystemParams = {"UBIPRINTPOSITIONX","UBIPRINTPOSITIONX2","UBIPRINTPOSITIONY","UBIPRINTPOSITIONY2","UBLABELBAND_COLIDX","UBLABELBAND_ROWIDX","UBLABELBAND_STIDX",
												"UBIPRINT_SCALE_X","UBIPRINT_SCALE_Y","UBIEXPORT_PATH","UBIPRINT_FIT_SIZE","UBIPRINT_USE_UI", "UBI_PREVIEW_XML","UBPDF_FILE_PATH","UBPDF_FILE_NAME"};
	
	public static String UB_PRINT_POSITION_X = "UBIPRINTPOSITIONX";					// 인쇄시 이동할 X좌표값
	public static String UB_PRINT_POSITION_X2 = "UBIPRINTPOSITIONX2";				// 인쇄시 이동할 X좌표값 클론페이지
	public static String UB_PRINT_POSITION_Y = "UBIPRINTPOSITIONY";					// 인쇄시 이동할 Y좌표값
	public static String UB_PRINT_POSITION_Y2 = "UBIPRINTPOSITIONY2";				// 인쇄시 이동할 Y좌표값 클론페이지

	public static String UB_LABEL_COL_INDEX = "UBLABELBAND_COLIDX";					// 라벨밴드의 시작 COLUMN좌표
	public static String UB_LABEL_ROW_INDEX = "UBLABELBAND_ROWIDX";					// 라벨밴드의 시작 ROW좌표
	public static String UB_LABEL_ST_INDEX = "UBLABELBAND_STIDX";					// 라벨밴드의 시작 인덱스 좌표( 6이 들어오면 7번째 부터 표시 ) 
	
	public static String UB_PRINT_SCALE_X = "UBIPRINT_SCALE_X";						// 라벨밴드의 띄울 공백값
	public static String UB_PRINT_SCALE_Y = "UBIPRINT_SCALE_Y";						// 라벨밴드의 띄울 공백값

	public static String UB_PRINT_FIT_SIZE 	= "UBIPRINT_FIT_SIZE";					// 인쇄시 맞추기 옵션 여부(true:맞추기,false:실제크기)
	public static String UB_PRINT_USE_UI 	= "UBIPRINT_USE_UI";					// 인쇄시 프린트 Dialog 사용 여부(true:사용,false:바로 인쇄)
	public static String UB_PRINT_VIEW_USE_UI 	= "UBIPRINT_UBF_USE_UI";			// 인쇄시 프린트 클라이언트 Dialog 사용 여부(true:사용,false:바로 인쇄)
	
	public static String UB_EXPORT_PATH = "UBIEXPORT_PATH";							// exportPDF시 서버 path
	
	public static String UB_EXPORT_BASE_PATH = "";									// EXPORT PDF 시 서버의 실제 BASE PATH
	
	public static String UB_PDF_PRINT_VERSION = "UB_PDF_PRINT_VERSION";				// 신규 PDF 폰트 사이즈 반영 지정값			

	public static String UB_DEF_PDF_PRINT_VERSION = "2.0";							// 신규 PDF 폰트 사이즈 반영 지정
	public static String UB_PDF_PRINT_VERSION_2 = "2.0";							// 신규 PDF 폰트 사이즈 반영 지정값			
	public static String UB_PDF_PRINT_VERSION_1 = "1.0";							// 신규 PDF 폰트 사이즈 반영 지정값			
	
	public static boolean USE_TIME_LOG = false;
	
	public GlobalVariableData() {
		// TODO Auto-generated constructor stub
	}
	
	
}
