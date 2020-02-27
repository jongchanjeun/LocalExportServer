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
import com.google.zxing.client.j2se.MatrixToSvgImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.oreilly.servlet.Base64Encoder;

public class QRCode {
	private QRCodeWriter qrCodeWriter = null;
	private String qrCodeText;
    private int size = 125;
    private BitMatrix byteMatrix = null;
    private String qrCodeType = "svg"; // png, svg

    public void makeBarcode(String _data, int _size) throws Exception {
    	this.qrCodeText = _data;
        this.size = _size;
        this.qrCodeWriter = new QRCodeWriter();
        	
    	//Write the bar code to PNG file
    	writeQRCode("svg");	// png, svg
    }

    private void writeQRCode(String fileType) throws Exception {
       this.qrCodeType = fileType;
    	
    	// Create the ByteMatrix for the QR-Code that encodes the given String
    	Hashtable<EncodeHintType, Object > hintMap = new Hashtable<EncodeHintType, Object>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hintMap.put(EncodeHintType.MARGIN, 0);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        
        this.byteMatrix = this.qrCodeWriter.encode(this.qrCodeText,
                BarcodeFormat.QR_CODE, this.size, this.size, hintMap);
    }

	public void writeToStream(OutputStream out) throws IOException
	{
		//Write the bar code to PNG file
		if(this.qrCodeType.equals("png"))
			MatrixToImageWriter.writeToStream(this.byteMatrix, "png", out);
		else if(this.qrCodeType.equals("svg"))
			MatrixToSvgImageWriter.writeToStream(this.byteMatrix, out, true);			
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
			if(this.qrCodeType.equals("png"))
			{
				MatrixToImageWriter.writeToStream(this.byteMatrix, "png", os);
				result = Base64Encoder.encode(os.toByteArray());
			}
			else if(this.qrCodeType.equals("svg"))
			{
				//MatrixToSvgImageWriter.writeToFile(this.byteMatrix, new File("test123.svg"), true);
				MatrixToSvgImageWriter.writeToStream(this.byteMatrix, os, true);			
				result = Base64Encoder.encode(os.toString());
			}
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
