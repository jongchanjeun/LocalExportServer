package org.ubstorm.service.utils;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class ImageUtil {
    public static final int RATIO = 0;
    public static final int SAME = -1;
    public static float MAXINUM_LENGTH = 100000;		// BASE64문자열의 이미지의 변환여부를 판단할 문자수
    
    public static void resize(File src, File dest, int width, int height) throws IOException {
        Image srcImg = null; 
        String suffix = src.getName().substring(src.getName().lastIndexOf('.')+1).toLowerCase();
        if (suffix.equals("bmp") || suffix.equals("png") || suffix.equals("gif")) {
            srcImg = ImageIO.read(src);
        } else {
            // BMP가 아닌 경우 ImageIcon을 활용해서 Image 생성
            // 이렇게 하는 이유는 getScaledInstance를 통해 구한 이미지를
            // PixelGrabber.grabPixels로 리사이즈 할때
            // 빠르게 처리하기 위함이다.
            srcImg = new ImageIcon(src.toURL()).getImage();
        }
        
        int srcWidth = srcImg.getWidth(null);
        int srcHeight = srcImg.getHeight(null);
        
        int destWidth = -1, destHeight = -1;
        
        if (width == SAME) {
            destWidth = srcWidth;
        } else if (width > 0) {
            destWidth = width;
        }
        
        if (height == SAME) {
            destHeight = srcHeight;
        } else if (height > 0) {
            destHeight = height;
        }
        
        if (width == RATIO && height == RATIO) {
            destWidth = srcWidth;
            destHeight = srcHeight;
        } else if (width == RATIO) {
            double ratio = ((double)destHeight) / ((double)srcHeight);
            destWidth = (int)((double)srcWidth * ratio);
        } else if (height == RATIO) {
            double ratio = ((double)destWidth) / ((double)srcWidth);
            destHeight = (int)((double)srcHeight * ratio);
        }
        
        Image imgTarget = srcImg.getScaledInstance(destWidth, destHeight, Image.SCALE_SMOOTH); 
        int pixels[] = new int[destWidth * destHeight]; 
        PixelGrabber pg = new PixelGrabber(imgTarget, 0, 0, destWidth, destHeight, pixels, 0, destWidth); 
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        } 
        BufferedImage destImg = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB); 
        destImg.setRGB(0, 0, destWidth, destHeight, pixels, 0, destWidth); 
        
        ImageIO.write(destImg, "jpg", dest);
    }

    public static BufferedImage resize(BufferedImage src, int width, int height, boolean _useResize , boolean _useAlpha) throws IOException 
    {
    	Image srcImg;
        ImageIcon srcImgIcon;

        srcImg = Toolkit.getDefaultToolkit().createImage(src.getSource());
        srcImgIcon = new ImageIcon(srcImg);
    	
    	 int srcWidth = srcImg.getWidth(null);
         int srcHeight = srcImg.getHeight(null);
         
         int destWidth = -1, destHeight = -1;
         
         if (width == SAME) {
             destWidth = srcWidth;
         } else if (width > 0) {
             destWidth = width;
         }
         
         if (height == SAME) {
             destHeight = srcHeight;
         } else if (height > 0) {
             destHeight = height;
         }
         
         if( _useResize )
         {
        	 double ratio = 1;
        	 if( ((double)destHeight) / ((double)srcHeight) > ((double)destWidth) / ((double)srcWidth) )
        	 {
        		 ratio = ((double)destWidth) / ((double)srcWidth);
        	 }
        	 else
        	 {
        		 ratio = ((double)destHeight) / ((double)srcHeight);
        	 }
        	 
        	 destWidth = (int)((double)srcWidth * ratio);
        	 destHeight = (int)((double)srcHeight * ratio);
         }
         else
         {
        	 if (width == RATIO && height == RATIO) {
                 destWidth = srcWidth;
                 destHeight = srcHeight;
             } else if (width == RATIO) {
                 double ratio = ((double)destHeight) / ((double)srcHeight);
                 destWidth = (int)((double)srcWidth * ratio);
             } else if (height == RATIO) {
                 double ratio = ((double)destWidth) / ((double)srcWidth);
                 destHeight = (int)((double)srcHeight * ratio);
             }
         }
         
         Image imgTarget = srcImg.getScaledInstance(destWidth, destHeight, Image.SCALE_SMOOTH); 
         int pixels[] = new int[destWidth * destHeight]; 
         PixelGrabber pg = new PixelGrabber(imgTarget, 0, 0, destWidth, destHeight, pixels, 0, destWidth); 
         try {
             pg.grabPixels();
         } catch (InterruptedException e) {
             throw new IOException(e.getMessage());
         } 
         
         int _type = BufferedImage.TYPE_INT_RGB;
         if(_useAlpha)
         {
        	 _type = BufferedImage.TYPE_INT_ARGB;
         }
         
         BufferedImage destImg = new BufferedImage(destWidth, destHeight, _type); 
         destImg.setRGB(0, 0, destWidth, destHeight, pixels, 0, destWidth); 
         
         
         srcImg = null;
         srcImgIcon = null;
         src = null;
         
         return destImg;
         //ImageIO.write(destImg, "jpg", dest);
    }
    
    public static String resizeBLOBData(String blobStr, int width, int height, boolean _useResize) throws IOException 
    {
    	BASE64Decoder _base64Decode = new BASE64Decoder();
    	BASE64Encoder _base64Encode = new BASE64Encoder();
    	String imageString = "";
    	String imageType = "jpg";
		byte[] bt= _base64Decode.decodeBuffer(blobStr); 
		
		if( bt != null )
		{
			BufferedImage im = ImageIO.read(new ByteArrayInputStream(bt));
			
//			boolean _hasAlpha = im.getColorModel().hasAlpha(); 
//			_hasAlpha = false;
//			im = resize( im, 0, 0, false, false);			//  원본 해상도 유지하고 용량만 Resize
			im = resizeImage( im, width, height, false );	//	해상도를 아이템에 해상도에 맞춰서 변경
			
//			if(_hasAlpha)
//			{
//				imageType = "png";
//			}
			
			//image to bytes
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        
	        try {
	        	
	            ImageIO.write(im, imageType, baos);
	            baos.flush();
	            byte[] imageAsRawBytes = baos.toByteArray();
	            

	            //bytes to string
	            imageString =  _base64Encode.encode(imageAsRawBytes);
	        } catch (IOException ex) {
	            
	        }

	        im = null;
		}
		return imageString;
    }
    
    

	public static String resize2(String blobStr, int width, int height) throws IOException {
        // reads input image
		
		BASE64Decoder _base64Decode = new BASE64Decoder();
    	BASE64Encoder _base64Encode = new BASE64Encoder();
    	String imageString = "";
    	System.out.println("BLOB_STR_LENGHT : " + blobStr.length());
		byte[] bt= _base64Decode.decodeBuffer(blobStr);
		
		if( bt != null )
		{
			BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(bt));
			
			int _orizinalW = inputImage.getWidth();
			int _orizinalH = inputImage.getHeight();
			
			float _scale = 0;
			if( width/_orizinalW > height/_orizinalH )
			{
				_scale = (float) height/_orizinalH;
			}
			else
			{
				_scale = (float) width/_orizinalW;
			}
			
			_scale = _scale * 2;
			
			width = Float.valueOf(_orizinalW*_scale).intValue(); 
			height = Float.valueOf(_orizinalH*_scale).intValue(); 
			
			inputImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			
			
			// creates output image
			BufferedImage outputImage = new BufferedImage(width,
					height, inputImage.getType());
			
			// scales the input image to the output image
			Graphics2D g2d = outputImage.createGraphics();
			g2d.drawImage(inputImage, 0, 0, width, height, null);
			g2d.dispose();
			
			//image to bytes
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        System.out.println();
	        try {
	            ImageIO.write(outputImage, "png", baos);
	            baos.flush();
	            byte[] imageAsRawBytes = baos.toByteArray();
	            

	            //bytes to string
	            imageString =  _base64Encode.encode(imageAsRawBytes);
	        } catch (IOException ex) {
	            
	        }
		}
		
		System.out.println("imageString_LENGHT : " + imageString.length());
		
		return imageString;
    }
	
	
	public static String resize3(String blobStr, int width, int height) throws IOException {
        // reads input image
		
		BASE64Decoder _base64Decode = new BASE64Decoder();
    	BASE64Encoder _base64Encode = new BASE64Encoder();
    	String imageString = "";
//    	System.out.println("BLOB_STR_LENGHT : " + blobStr.length());
		byte[] bt= _base64Decode.decodeBuffer(blobStr);
		
		if( bt != null )
		{
			BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(bt));
			
			int _orizinalW = inputImage.getWidth();
			int _orizinalH = inputImage.getHeight();
			
			float _scale = 0;
			if( width/_orizinalW > height/_orizinalH )
			{
				_scale = (float) height/_orizinalH;
			}
			else
			{
				_scale = (float) width/_orizinalW;
			}
			
			_scale = _scale * 2;
			
			width = Float.valueOf(_orizinalW*_scale).intValue(); 
			height = Float.valueOf(_orizinalH*_scale).intValue(); 
			
			inputImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			
			
			BufferedImage outputImage = resizeImage( inputImage, width, height, inputImage.getColorModel().hasAlpha() );
			
			//image to bytes
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        System.out.println();
	        try {
	            ImageIO.write(outputImage, "png", baos);
	            baos.flush();
	            byte[] imageAsRawBytes = baos.toByteArray();

	            //bytes to string
	            imageString =  _base64Encode.encode(imageAsRawBytes);
	        } catch (IOException ex) {
	            
	        }
		}
		
//		System.out.println("imageString_LENGHT : " + imageString.length());
		
		return imageString;
    }
	

    /**
     * This function resize the image file and returns the BufferedImage object that can be saved to file system.
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height, boolean _useAlpha) {
    	
    	int _type = BufferedImage.TYPE_INT_RGB;
    	if(_useAlpha)
    	{
    		_type = BufferedImage.TYPE_INT_ARGB;
    	}
    	
    	image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        final BufferedImage bufferedImage = new BufferedImage(width, height, _type );
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        
        //below three lines are for RenderingHints for better image quality at cost of higher processing time
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }
    
	
    
}

