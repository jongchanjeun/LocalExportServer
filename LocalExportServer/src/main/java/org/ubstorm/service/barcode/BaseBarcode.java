package org.ubstorm.service.barcode;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeImageHandler;
import net.sourceforge.barbecue.output.OutputException;
import sun.misc.BASE64Decoder;

import com.oreilly.servlet.Base64Encoder;

public abstract class BaseBarcode {
	
	protected Barcode barcode = null;
	
	public abstract void makeBarcode(String _data, int _barWidth, int _barHeight, boolean _showLabel) throws BarcodeException;
	
	public void writeToStream(OutputStream out)
	{
		if( barcode == null ){
			return;
		}
		
		//Write the bar code to PNG file
		try {
			BarcodeImageHandler.writePNG(this.barcode, out);
		} catch (OutputException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	public void writeToFile(File file)
	{
		//Write the bar code to PNG file
		try {
			BarcodeImageHandler.savePNG(this.barcode, file);
		} catch (OutputException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	public String getBase64String()
	{
		String result = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			BufferedImage bi = BarcodeImageHandler.getImage(this.barcode);
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
	
	
	
	
	/**
     * Decode string to image
     * @param imageString The string to decode
     * @return decoded image
     */
    public BufferedImage decodeToImage(String imageString) {

        BufferedImage image = null;
        byte[] imageByte;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return image;
    }

}
