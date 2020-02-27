package org.ubstorm.service.chart;

import java.text.AttributedString;

import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.data.general.PieDataset;

public class CustomLabelGenerator implements PieSectionLabelGenerator {

	private int showLabelType = 1;
	private String perDecimalFormat = "%.1f";
	private String valDecimalFormat = "%,.0f";
	
	/**
	 * Set label show type. 
	 * 
	 * 1 : value and %, 2 : key and value, 3 : key only, 4 : value only, 5 : % only
	 * 
	 * @param showType
	 */
	public void setShowLabelType(int showType)
	{
		this.showLabelType = showType;
	}
	public void setPercentDecimalFormat(String format)
	{
		this.perDecimalFormat = format;
	}
	public void setValueDecimalFormat(String format)
	{
		this.valDecimalFormat = format;
	}
	
	@Override
	public AttributedString generateAttributedSectionLabel(PieDataset arg0,
			Comparable arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateSectionLabel(PieDataset dataset, Comparable key) {
		Double totSum = 0.0;
		String result = null;    
        if (dataset != null) {
        	
        	int nCnt = dataset.getItemCount();
        	for(int i=0; i< nCnt; i++)
        		totSum = totSum + dataset.getValue(i).doubleValue();
        	Double curVal = dataset.getValue(key).doubleValue();
        	
        	String _valueDecmalStr = curVal.toString();
        	if(this.valDecimalFormat.equals("")==false) _valueDecmalStr = String.format(this.valDecimalFormat, curVal);
        	
        	switch(this.showLabelType)
        	{
        		case 1:
//        			result =  String.format(this.valDecimalFormat, curVal) + " " + String.format(this.perDecimalFormat, (curVal / totSum) * 100) + "%";
        			result =  _valueDecmalStr + " " + String.format(this.perDecimalFormat, (curVal / totSum) * 100) + "%";
        			break;
        		case 2:
        			result =  key.toString() + " " + _valueDecmalStr;
        			break;
        		case 3:
           			result =  key.toString();
           		 	break;
        		case 4:
           			result =  _valueDecmalStr;
           		 	break;
        		case 5:
           			result =  String.format(this.perDecimalFormat, (curVal / totSum) * 100) + "%";
           		 	break;
        		case 6:
        			result =  key.toString() + " " + String.format(this.perDecimalFormat, (curVal / totSum) * 100) + "%";
        			break;
        		default:	// same as 1
           			result =  _valueDecmalStr + " " + String.format(this.perDecimalFormat, (curVal / totSum) * 100) + "%";
           		 	break;
        	}
        }
        return result;
	}

}
