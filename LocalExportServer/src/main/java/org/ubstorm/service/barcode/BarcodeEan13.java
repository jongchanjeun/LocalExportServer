package org.ubstorm.service.barcode;

import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;

public class BarcodeEan13 extends BaseBarcode {
	
	public void makeBarcode(String _data, int _barWidth, int _barHeight, boolean _showLabel) throws BarcodeException  
	{
		barcode = BarcodeFactory.createEAN13(_data);
		if(_barWidth > 0)
			barcode.setBarWidth(_barWidth);
		if(_barHeight > 0)
			barcode.setBarHeight(_barHeight);
		
		barcode.setDrawingText(_showLabel);
	}

}
