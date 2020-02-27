package org.ubstorm.service.barcode.qrgen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN8Writer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.oreilly.servlet.Base64Encoder;

public class Ean8Code {
	private EAN8Writer ean8Writer = null;
	private BitMatrix byteMatrix = null;
	private String ean8Text;
	private int barWidth;
	private int barHeight;

    public void makeBarcode(String _data, int _barWidth, int _barHeight) throws Exception {
    	this.ean8Text = _data;
    	this.barWidth = _barWidth;
    	this.barHeight = _barHeight;
        this.ean8Writer = new EAN8Writer();
        
       	//Write the bar code to PNG file
       	writeQRCode("png");
    }

    private void writeQRCode(String fileType) throws Exception {
        // Create the ByteMatrix for the QR-Code that encodes the given String
    	Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
       
        this.byteMatrix = this.ean8Writer.encode(this.ean8Text,
                BarcodeFormat.EAN_8, this.barWidth, this.barHeight, hintMap);
    }

    public void writeToStream(OutputStream out) throws IOException
	{
		//Write the bar code to PNG file
		MatrixToImageWriter.writeToStream(this.byteMatrix, "png", out);
	}
	
	public void writeToFile(File file) throws IOException
	{
		//Write the bar code to PNG file
		OutputStream outputStream = new FileOutputStream(file);
		writeToStream(outputStream);
		outputStream.close();
	}
    
	public String getBase64String()
	{
		String result = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			MatrixToImageWriter.writeToStream(this.byteMatrix, "png", os);
			result = Base64Encoder.encode(os.toByteArray());
			result = result.replaceAll("(\\r|\\n)", "");			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (IOException e) {}
		}
		
		return result;
	}
	
}
