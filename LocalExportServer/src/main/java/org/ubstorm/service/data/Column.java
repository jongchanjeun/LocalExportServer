package org.ubstorm.service.data;

public class Column
{
	private String colName = "";
	private int colType = 0;

	public Column() {
	}
	
	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public int getColType() {
		return colType;
	}

	public void setColType(int colType) {
		this.colType = colType;
	}
}
