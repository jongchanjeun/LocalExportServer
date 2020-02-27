package org.ubstorm.service.barcode.qrgen;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.DataMatrixWriter;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.oreilly.servlet.Base64Encoder;

public class DataMatrix {
	private DataMatrixWriter dmWriter = null;
	private String dmCodeText;
    private int size = 64;
    private BitMatrix byteMatrix = null;
    private String shapeType = "S";

    public void makeBarcode(String _data, int _size) throws Exception {
    	this.dmCodeText = _data;
        this.size = _size;
        this.dmWriter = new DataMatrixWriter();
        this.shapeType = "S";
        
       	//Write the bar code to SQUARE type
       	writeDataMatrix();
    }
    
    public void makeBarcode2(String _data, int _size) throws Exception {
    	this.dmCodeText = _data;
        this.size = _size;
        this.dmWriter = new DataMatrixWriter();
        this.shapeType = "R";
        
       	//Write the bar code to RECTANGLE type
       	writeDataMatrix();
    }

    private void writeDataMatrix() throws Exception {
        // Create the ByteMatrix for the QR-Code that encodes the given String
        Map<EncodeHintType,Object> hintMap = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
        
        if("R".equals(this.shapeType))
        {
        	hintMap.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_RECTANGLE);
        	
        	this.byteMatrix = this.dmWriter.encode(this.dmCodeText,
                    BarcodeFormat.DATA_MATRIX, this.size*2, this.size, hintMap);
        }
        else
        {
        	hintMap.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_SQUARE);
       
        	this.byteMatrix = this.dmWriter.encode(this.dmCodeText,
                BarcodeFormat.DATA_MATRIX, this.size, this.size, hintMap);
        }
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
			BufferedImage bi = null;
			if("R".equals(this.shapeType))
				bi = scaleOfBitMatrix(this.size*2, this.size/2);
			else
				bi = scaleOfBitMatrix(this.size, this.size);
			ImageIO.write(bi, "png", os);		
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
	
	
	private BufferedImage scaleOfBitMatrix(int requestedWidth, int requestedHeight)
	{
		int BLACK = 0xFF000000;
		int WHITE = 0xFFFFFFFF;

		int width = this.byteMatrix.getWidth();
		int height = this.byteMatrix.getHeight();

		// calculating the scaling factor
		int pixelsize = requestedWidth/width;
		if (pixelsize > requestedHeight/height)
		{
		  pixelsize = requestedHeight/height;
		}

		int[] pixels = new int[requestedWidth * requestedHeight];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
		  int offset = y * requestedWidth * pixelsize;

		  // scaling pixel height
		  for (int pixelsizeHeight = 0; pixelsizeHeight < pixelsize; pixelsizeHeight++, offset+=requestedWidth) {
		    for (int x = 0; x < width; x++) {
		      int color = this.byteMatrix.get(x, y) ? BLACK : WHITE;

		      // scaling pixel width
		      for (int pixelsizeWidth = 0; pixelsizeWidth < pixelsize; pixelsizeWidth++) {
		        pixels[offset + x * pixelsize + pixelsizeWidth] = color;
		      }
		    }
		  }
		}
		
		// I could only test it with BufferedImage and a modified version of the zxing J2SE client
		BufferedImage bitmap = new BufferedImage(requestedWidth, requestedHeight, BufferedImage.TYPE_INT_ARGB);
		bitmap.getRaster().setDataElements(0, 0, requestedWidth, requestedHeight, pixels);
		
		return bitmap;
	}
	
}
