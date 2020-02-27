package org.ubstorm.service.data;

public class UDMDataQuery {
	
	public String getDATASET() {
		return DATASET;
	}
	public void setDATASET(String dATASET) {
		DATASET = dATASET;
	}
	public String getACTION() {
		return ACTION;
	}
	public void setACTION(String aCTION) {
		ACTION = aCTION;
	}
	public String getTEXT() {
		return TEXT;
	}
	public void setTEXT(String tEXT) {
		TEXT = tEXT;
	}
	private String DATASET;			// DataSet 이름
	private String ACTION;			// 액션 : "SELECT" …
	private String TEXT;			// 요청 쿼리문
	
	
	private String TYPE;			// DB 종류 (oracle / mssql / mysql / ... )
	private String SERVER;			// SERVER ALIAS(TNS) 또는 IP ADDRESS
	private String DATABASE;		// DataBase 명
	private String ID;				// DB Log in ID
	private String PW;				// DB Log in PASSWD
	private String PORT;			// DataBase 연결 포트번호
	private String TABLE;			// DB Table 이름
	private String DATA;			// json array 형태로 DB에 입력하기 위한 데이터 집합
	private String OLDPK;			// 이전 PK 명
	private String NEWPK;			// 신규 PK 명
	private String ALL;				// Y/N  (table 전체 삭제 유무)
	private String PK;				// Primary Key 명
	private String FUNCTION;		// Function 명
	private String PROCEDURE;		// Procedure 명
	private String MAXROW;			// MSSQL의 경우 최대 Row 개수
	private String FIRSTCOL;		// MSSQL의 경우 Row번호를 부여하기 위한 첫번째 컬럼명
	private String TRIGGER;			// 트리거명
	private String VIEW;			// 뷰 명
	
	private String ENCODING;		// 암호화여부
	private String COMPRESS;		// 압축여부
	
	private String QUERY_VERSION;	// DB의 실제 field를 사용할 것인지 판단하는 Dataset 생성 version
	
	public String getCREATOR() {
		return CREATOR;
	}
	public void setCREATOR(String cREATOR) {
		CREATOR = cREATOR;
	}
	private String CREATOR;			// 테이블/프로시저 등의 생성자

	public String getQPARAMS() {
		return QPARAMS;
	}
	public void setQPARAMS(String qPARAMS) {
		QPARAMS = qPARAMS;
	}
	private String QPARAMS;
	
	public String getTYPE() {
		return TYPE;
	}
	public void setTYPE(String tYPE) {
		TYPE = tYPE;
	}
	public String getSERVER() {
		return SERVER;
	}
	public void setSERVER(String sERVER) {
		SERVER = sERVER;
	}
	public String getDATABASE() {
		return DATABASE;
	}
	public void setDATABASE(String dATABASE) {
		DATABASE = dATABASE;
	}
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getPW() {
		return PW;
	}
	public void setPW(String pW) {
		PW = pW;
	}
	public String getPORT() {
		return PORT;
	}
	public void setPORT(String pORT) {
		PORT = pORT;
	}
	public String getTABLE() {
		return TABLE;
	}
	public void setTABLE(String tABLE) {
		TABLE = tABLE;
	}
	public String getDATA() {
		return DATA;
	}
	public void setDATA(String dATA) {
		DATA = dATA;
	}
	public String getOLDPK() {
		return OLDPK;
	}
	public void setOLDPK(String oLDPK) {
		OLDPK = oLDPK;
	}
	public String getNEWPK() {
		return NEWPK;
	}
	public void setNEWPK(String nEWPK) {
		NEWPK = nEWPK;
	}
	public String getALL() {
		return ALL;
	}
	public void setALL(String aLL) {
		ALL = aLL;
	}
	public String getPK() {
		return PK;
	}
	public void setPK(String pK) {
		PK = pK;
	}
	public String getFUNCTION() {
		return FUNCTION;
	}
	public void setFUNCTION(String fUNCTION) {
		FUNCTION = fUNCTION;
	}
	public String getPROCEDURE() {
		return PROCEDURE;
	}
	public void setPROCEDURE(String pROCEDURE) {
		PROCEDURE = pROCEDURE;
	}
	public String getMAXROW() {
		return MAXROW;
	}
	public void setMAXROW(String mAXROW) {
		MAXROW = mAXROW;
	}
	public String getFIRSTCOL() {
		return FIRSTCOL;
	}
	public void setFIRSTCOL(String fIRSTCOL) {
		FIRSTCOL = fIRSTCOL;
	}
	public String getTRIGGER() {
		return TRIGGER;
	}
	public void setTRIGGER(String tRIGGER) {
		TRIGGER = tRIGGER;
	}
	public String getVIEW() {
		return VIEW;
	}
	public void setVIEW(String vIEW) {
		VIEW = vIEW;
	}
	
	public String getENCODING() {
		return ENCODING;
	}
	public void setENCODING(String eNCODING) {
		ENCODING = eNCODING;
	}
	public String getCOMPRESS() {
		return COMPRESS;
	}
	public void setCOMPRESS(String cOMPRESS) {
		COMPRESS = cOMPRESS;
	}
	public String getQUERY_VERSION() {
		return QUERY_VERSION;
	}
	public void setQUERY_VERSION(String qUERY_VERSION) {
		QUERY_VERSION = qUERY_VERSION;
	}
}
