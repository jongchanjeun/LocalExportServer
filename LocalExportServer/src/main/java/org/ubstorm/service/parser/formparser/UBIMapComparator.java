package org.ubstorm.service.parser.formparser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class UBIMapComparator implements Comparator<Map<String, Object>> {
	
	public ArrayList<String> mColAr;
	public ArrayList<String> mDesAr;
	public ArrayList<String> mNumericAr;
	
	public String column = "";
	public int c = 0;
	
	public UBIMapComparator(ArrayList<String> _colAr, ArrayList<String> _desAr, ArrayList<String> _numericAr ) {
		
		super();

		mColAr = _colAr;
		mDesAr = _desAr;
		mNumericAr = _numericAr;
		
		// TODO Auto-generated constructor stub
	}

	public int compare(Map<String, Object> o1, Map<String, Object> o2) 
    {
        return 0;
    }
	
}
