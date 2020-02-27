/*
 * Copyright 2013, MP Objects, http://www.mp-objects.com
 */
package org.ubstorm.service.barcode;

import java.awt.image.BufferedImage;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeImageHandler;
import net.sourceforge.barbecue.CompositeModule;
import net.sourceforge.barbecue.Module;
import net.sourceforge.barbecue.linear.LinearBarcode;
import net.sourceforge.barbecue.output.OutputException;

/**
 * Code 93 barcode. Based on the specs posted on: http://www.barcodeisland.com/code93.phtml and
 * http://en.wikipedia.org/wiki/Code_93
 * 
 * @author Michiel Hendriks <michiel.hendriks@mp-objects.com>
 */
public class Code93Barcode extends LinearBarcode {
	/**
	 * A list of type identifiers for the Code39 barcode format
	 */
	public static final String[] TYPES = new String[] { "Code39" };

	private final String label;

	/**
	 * Constructs a basic mode Code 39 barcode with the specified data and an optional checksum.
	 * 
	 * @param data
	 *            The data to encode
	 * @param requiresChecksum
	 *            A flag indicating whether a checksum is required or not
	 * @throws BarcodeException
	 *             If the data to be encoded is invalid
	 */
	public Code93Barcode(String data) throws BarcodeException {
		this(data, false);
	}

	/**
	 * Constructs an extended mode Code 39 barcode with the specified data and an optional checksum. The extended mode
	 * encodes all 128 ASCII characters using two character pairs from the basic Code 39 character set. Note that most
	 * barcode scanners will need to be configured to accept extended Code 39.
	 * 
	 * @param data
	 *            The data to encode
	 * @param extendedMode
	 *            Puts the barcode into extended mode, where all 128 ASCII characters can be encoded
	 * @throws BarcodeException
	 *             If the data to be encoded is invalid
	 */
	public Code93Barcode(String data, boolean extendedMode) throws BarcodeException {
		super(extendedMode ? encodeExtendedChars(data) : validateBasicChars(data));
		label = data;
	}

	/**
	 * Returns the for the Mod-47 checkIndex for the barcode as an int
	 * 
	 * @return Mod-47 checkIndex for the given data String
	 */
	public static int calculateMod47(final String givenData, final int weightMod) {
		if (givenData == null) {
			return 0;
		}
		int sum = 0;
		int weight = givenData.length() % weightMod;
		if (weight == 0) {
			weight = weightMod;
		}
		StringCharacterIterator iter = new StringCharacterIterator(givenData);
		for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			int idx = ModuleFactory.getIndex(c);
			sum += idx * weight;
			--weight;
			if (weight < 1) {
				weight = weightMod;
			}
		}
		int checkIndex = sum % 47;
		return checkIndex;
	}

	/**
	 * Generate a barcode image. <br />
	 * Can be used from a jasper report like:
	 * 
	 * <pre>
	 * &lt;imageExpression class="java.awt.Image"&gt;
	 *     &lt;![CDATA[com.mpobjects.barbecue.Code93Barcode.getBarcodeImage($P{BarCode},2,140,false)]]&gt;
	 * &lt;/imageExpression&gt;
	 * </pre>
	 * 
	 * @param aCode
	 *            The content of the barcode
	 * @param aBarWidth
	 *            The width in pixels of the thinnest bar
	 * @param aHeight
	 *            The height of the barcode
	 * @param aIncludeText
	 *            If true also print the code below the barcode
	 * @return Rendered barcode
	 */
	public static final BufferedImage getBarcodeImage(Object aCode, int aBarWidth, int aHeight, boolean aIncludeText) {
		if (aCode == null) {
			return null;
		}

		String strCode = aCode.toString();
		Code93Barcode barcode;
		try {
			barcode = new Code93Barcode(strCode, true);
		} catch (BarcodeException e) {
			throw new IllegalArgumentException(e);
		}
		if (aBarWidth > 0) {
			barcode.setBarWidth(aBarWidth);
		}
		if (aHeight > 0) {
			barcode.setBarHeight(aHeight);
		}
		barcode.setDrawingText(aIncludeText);
		try {
			return BarcodeImageHandler.getImage(barcode);
		} catch (OutputException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String encodeExtendedChars(String data) throws BarcodeException {
		StringBuffer buf = new StringBuffer();
		StringCharacterIterator iter = new StringCharacterIterator(data);
		for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			if (!ModuleFactory.hasModule(c)) {
				String xlate = ModuleFactory.getExtendedCharacter(c);
				if (xlate == null) {
					throw new BarcodeException("Illegal character [" + c + "] - not part of the ASCII character set");
				}
				buf.append(xlate);
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	private static String validateBasicChars(String data) throws BarcodeException {
		StringCharacterIterator iter = new StringCharacterIterator(data);
		for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			if (!ModuleFactory.hasModule(c)) {
				throw new BarcodeException("Illegal character [" + c + "] - try using extended mode if you need to encode the full ASCII character set");
			}
		}
		return data;
	}

	/**
	 * Returns the text that will be displayed underneath the barcode (if requested).
	 * 
	 * @return The text label for the barcode
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the checksum for the barcode, pre-encoded as a Module.
	 * 
	 * @return Null if no checksum is required, a Mod-43 calculated checksum otherwise
	 */
	@Override
	protected Module calculateChecksum() {
		CompositeModule compositeModule = new CompositeModule();
		// C checksum
		int checkIndex = calculateMod47(data, 20);
		compositeModule.add(ModuleFactory.getModuleForIndex(checkIndex));
		// K checksum
		checkIndex = calculateMod47(data + ModuleFactory.getKeyForIndex(checkIndex), 15);
		compositeModule.add(ModuleFactory.getModuleForIndex(checkIndex));
		return compositeModule;
	}

	/**
	 * Returns the encoded data for the barcode.
	 * 
	 * @return An array of modules that represent the data as a barcode
	 */
	@Override
	protected Module[] encodeData() {
		List<Module> modules = new ArrayList<Module>();
		for (int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			Module module = ModuleFactory.getModule(c);
			modules.add(module);
		}
		return modules.toArray(new Module[0]);
	}

	/**
	 * Returns the post-amble for the barcode.
	 * 
	 * @return ModuleFactory.START_STOP
	 */
	@Override
	protected Module getPostAmble() {
		CompositeModule compositeModule = new CompositeModule();
		compositeModule.add(ModuleFactory.START_STOP);
		compositeModule.add(ModuleFactory.TERMINATOR);
		return compositeModule;
	}

	/**
	 * Returns the pre-amble for the barcode.
	 * 
	 * @return ModuleFactory.START_STOP
	 */
	@Override
	protected Module getPreAmble() {
		return ModuleFactory.START_STOP;
	}
}
