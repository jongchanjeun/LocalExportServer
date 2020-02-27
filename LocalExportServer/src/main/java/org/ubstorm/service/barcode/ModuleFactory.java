/*
 * Copyright 2013, MP Objects, http://www.mp-objects.com
 */
package org.ubstorm.service.barcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.barbecue.Module;

/**
 * ModuleFactory more or less the copied from {@link net.sourceforge.barbecue.linear.code39.ModuleFactory} but with
 * code93 values. Source of code93 info: http://en.wikipedia.org/wiki/Code_93
 * 
 * @author Michiel Hendriks <michiel.hendriks@mp-objects.com>
 */
public class ModuleFactory {

	/**
	 * The start and stop character for the barcode
	 */
	public static final Module START_STOP = new Module(new int[] { 1, 1, 1, 1, 4, 1 });

	public static final Module START_STOP_REVERSE = new Module(new int[] { 1, 1, 4, 1, 1, 1 });

	public static final Module TERMINATOR = new Module(new int[] { 1 });

	private static final List<Character> KEYS;
	private static final Map<Character, Module> SET;
	private static final Map<Character, String> EXT_CHARS;

	/**
	 * The escape chars use special unicode characters for easier usage. This table contains the translation of these
	 * characters to the actual string used in the spec.
	 */
	private static final Map<Character, String> ESCAPE_CHARS;

	private static final char ESCAPE_DOLLAR = '\uFFF0';
	private static final char ESCAPE_PERCENT = '\uFFF1';
	private static final char ESCAPE_SLASH = '\uFFF2';
	private static final char ESCAPE_PLUS = '\uFFF3';

	static {
		KEYS = new ArrayList<Character>();
		SET = new HashMap<Character, Module>();
		EXT_CHARS = new HashMap<Character, String>();
		ESCAPE_CHARS = new HashMap<Character, String>();

		initBaseSet();
		initExtendedSet();
	}

	private ModuleFactory() {
	}

	/**
	 * Returns the string of characters from the standard encoding table that encode the given extended character set
	 * character.
	 * 
	 * @param c
	 *            The character from the extended ASCII set to encode
	 * @return The string of characters from the default Code 39 encoding table that represent the given character
	 */
	public static String getExtendedCharacter(char c) {
		return EXT_CHARS.get(new Character(c));
	}

	/**
	 * Returns the index of the given character in the encoding tables. This is used when calculating the checksum.
	 * 
	 * @param key
	 *            The data character sequence to get the index for
	 * @return The index for the given key
	 */
	public static int getIndex(char key) {
		return KEYS.indexOf(key);
	}

	/**
	 * Get the character for a given index
	 * 
	 * @param index
	 * @return
	 */
	public static Character getKeyForIndex(int index) {
		return KEYS.get(index);
	}

	/**
	 * Returns the module that represents the specified character.
	 * 
	 * @param key
	 *            The data character to get the encoding module for
	 * @return The module that encodes the given char
	 */
	public static Module getModule(char key) {
		Module module = null;
		module = SET.get(key);
		module.setSymbol(Character.toString(key));
		return module;
	}

	/**
	 * Returns the encoded module at the given index position. This is used to get the encoded checksum character.
	 * 
	 * @param index
	 *            The index to get the module for
	 * @return The module at the specified index
	 */
	public static Module getModuleForIndex(int index) {
		return getModule(KEYS.get(index));
	}

	/**
	 * Indicates whether the given key is represented in the default encoding table that this module factory contains.
	 * 
	 * @return True if the key has a direct module encoding, false if not
	 */
	public static boolean hasModule(char key) {
		if (ESCAPE_CHARS.containsKey(key)) {
			return false;
		}
		return getIndex(key) != -1;
	}

	/**
	 * @param aEscapeDollar
	 * @param aString
	 * @param aIs
	 */
	private static void addEscapeModule(char character, String symbol, int[] bars) {
		Module module = new Module(bars);
		module.setSymbol(symbol);
		KEYS.add(character);
		SET.put(character, module);
		ESCAPE_CHARS.put(character, symbol);
	}

	/**
	 * Register a module for a given character
	 * 
	 * @param character
	 * @param bars
	 */
	private static void addModule(char character, int[] bars) {
		Module module = new Module(bars);
		module.setSymbol(Character.toString(character));
		KEYS.add(character);
		SET.put(character, module);
	}

	/**
	 * Initialise the module definitions.
	 */
	private static void initBaseSet() {
		addModule('0', new int[] { 1, 3, 1, 1, 1, 2 });
		addModule('1', new int[] { 1, 1, 1, 2, 1, 3 });
		addModule('2', new int[] { 1, 1, 1, 3, 1, 2 });
		addModule('3', new int[] { 1, 1, 1, 4, 1, 1 });
		addModule('4', new int[] { 1, 2, 1, 1, 1, 3 });
		addModule('5', new int[] { 1, 2, 1, 2, 1, 2 });
		addModule('6', new int[] { 1, 2, 1, 3, 1, 1 });
		addModule('7', new int[] { 1, 1, 1, 1, 1, 4 });
		addModule('8', new int[] { 1, 3, 1, 2, 1, 1 });
		addModule('9', new int[] { 1, 4, 1, 1, 1, 1 });
		addModule('A', new int[] { 2, 1, 1, 1, 1, 3 });
		addModule('B', new int[] { 2, 1, 1, 2, 1, 2 });
		addModule('C', new int[] { 2, 1, 1, 3, 1, 1 });
		addModule('D', new int[] { 2, 2, 1, 1, 1, 2 });
		addModule('E', new int[] { 2, 2, 1, 2, 1, 1 });
		addModule('F', new int[] { 2, 3, 1, 1, 1, 1 });
		addModule('G', new int[] { 1, 1, 2, 1, 1, 3 });
		addModule('H', new int[] { 1, 1, 2, 2, 1, 2 });
		addModule('I', new int[] { 1, 1, 2, 3, 1, 1 });
		addModule('J', new int[] { 1, 2, 2, 1, 1, 2 });
		addModule('K', new int[] { 1, 3, 2, 1, 1, 1 });
		addModule('L', new int[] { 1, 1, 1, 1, 2, 3 });
		addModule('M', new int[] { 1, 1, 1, 2, 2, 2 });
		addModule('N', new int[] { 1, 1, 1, 3, 2, 1 });
		addModule('O', new int[] { 1, 2, 1, 1, 2, 2 });
		addModule('P', new int[] { 1, 3, 1, 1, 2, 1 });
		addModule('Q', new int[] { 2, 1, 2, 1, 1, 2 });
		addModule('R', new int[] { 2, 1, 2, 2, 1, 1 });
		addModule('S', new int[] { 2, 1, 1, 1, 2, 2 });
		addModule('T', new int[] { 2, 1, 1, 2, 2, 1 });
		addModule('U', new int[] { 2, 2, 1, 1, 2, 1 });
		addModule('V', new int[] { 2, 2, 2, 1, 1, 1 });
		addModule('W', new int[] { 1, 1, 2, 1, 2, 2 });
		addModule('X', new int[] { 1, 1, 2, 2, 2, 1 });
		addModule('Y', new int[] { 1, 2, 2, 1, 2, 1 });
		addModule('Z', new int[] { 1, 2, 3, 1, 1, 1 });
		addModule('-', new int[] { 1, 2, 1, 1, 3, 1 });
		addModule('.', new int[] { 3, 1, 1, 1, 1, 2 });
		addModule(' ', new int[] { 3, 1, 1, 2, 1, 1 });
		addModule('$', new int[] { 3, 2, 1, 1, 1, 1 });
		addModule('/', new int[] { 1, 1, 2, 1, 3, 1 });
		addModule('+', new int[] { 1, 1, 3, 1, 2, 1 });
		addModule('%', new int[] { 2, 1, 1, 1, 3, 1 });

		addEscapeModule(ESCAPE_DOLLAR, "($)", new int[] { 1, 2, 1, 2, 2, 1 });
		addEscapeModule(ESCAPE_PERCENT, "(%)", new int[] { 3, 1, 2, 1, 1, 1 });
		addEscapeModule(ESCAPE_SLASH, "(/)", new int[] { 3, 1, 1, 1, 2, 1 });
		addEscapeModule(ESCAPE_PLUS, "(+)", new int[] { 1, 2, 2, 2, 1, 1 });
	}

	/**
	 * Initialise the extended ASCII set lookup table.
	 */
	private static void initExtendedSet() {
		EXT_CHARS.put(new Character('\000'), ESCAPE_PERCENT + "U");
		EXT_CHARS.put(new Character('\001'), ESCAPE_DOLLAR + "A");
		EXT_CHARS.put(new Character('\002'), ESCAPE_DOLLAR + "B");
		EXT_CHARS.put(new Character('\003'), ESCAPE_DOLLAR + "C");
		EXT_CHARS.put(new Character('\004'), ESCAPE_DOLLAR + "D");
		EXT_CHARS.put(new Character('\005'), ESCAPE_DOLLAR + "E");
		EXT_CHARS.put(new Character('\006'), ESCAPE_DOLLAR + "F");
		EXT_CHARS.put(new Character('\007'), ESCAPE_DOLLAR + "G");
		EXT_CHARS.put(new Character('\010'), ESCAPE_DOLLAR + "H");
		EXT_CHARS.put(new Character('\011'), ESCAPE_DOLLAR + "I");
		EXT_CHARS.put(new Character('\012'), ESCAPE_DOLLAR + "J");
		EXT_CHARS.put(new Character('\013'), ESCAPE_DOLLAR + "K");
		EXT_CHARS.put(new Character('\014'), ESCAPE_DOLLAR + "L");
		EXT_CHARS.put(new Character('\015'), ESCAPE_DOLLAR + "M");
		EXT_CHARS.put(new Character('\016'), ESCAPE_DOLLAR + "N");
		EXT_CHARS.put(new Character('\017'), ESCAPE_DOLLAR + "O");
		EXT_CHARS.put(new Character('\020'), ESCAPE_DOLLAR + "P");
		EXT_CHARS.put(new Character('\021'), ESCAPE_DOLLAR + "Q");
		EXT_CHARS.put(new Character('\022'), ESCAPE_DOLLAR + "R");
		EXT_CHARS.put(new Character('\023'), ESCAPE_DOLLAR + "S");
		EXT_CHARS.put(new Character('\024'), ESCAPE_DOLLAR + "T");
		EXT_CHARS.put(new Character('\025'), ESCAPE_DOLLAR + "U");
		EXT_CHARS.put(new Character('\026'), ESCAPE_DOLLAR + "V");
		EXT_CHARS.put(new Character('\027'), ESCAPE_DOLLAR + "W");
		EXT_CHARS.put(new Character('\030'), ESCAPE_DOLLAR + "X");
		EXT_CHARS.put(new Character('\031'), ESCAPE_DOLLAR + "Y");
		EXT_CHARS.put(new Character('\032'), ESCAPE_DOLLAR + "Z");
		EXT_CHARS.put(new Character('\033'), ESCAPE_PERCENT + "A");
		EXT_CHARS.put(new Character('\034'), ESCAPE_PERCENT + "B");
		EXT_CHARS.put(new Character('\035'), ESCAPE_PERCENT + "C");
		EXT_CHARS.put(new Character('\036'), ESCAPE_PERCENT + "D");
		EXT_CHARS.put(new Character('\037'), ESCAPE_PERCENT + "E");
		EXT_CHARS.put(new Character('\177'), ESCAPE_PERCENT + "T"); // Also %X, %Y, %Z

		EXT_CHARS.put(new Character('!'), ESCAPE_SLASH + "A");
		EXT_CHARS.put(new Character('"'), ESCAPE_SLASH + "B");
		EXT_CHARS.put(new Character('#'), ESCAPE_SLASH + "C");
		EXT_CHARS.put(new Character('$'), ESCAPE_SLASH + "D");
		EXT_CHARS.put(new Character('%'), ESCAPE_SLASH + "E");
		EXT_CHARS.put(new Character('&'), ESCAPE_SLASH + "F");
		EXT_CHARS.put(new Character('\''), ESCAPE_SLASH + "G");
		EXT_CHARS.put(new Character('('), ESCAPE_SLASH + "H");
		EXT_CHARS.put(new Character(')'), ESCAPE_SLASH + "I");
		EXT_CHARS.put(new Character('*'), ESCAPE_SLASH + "J");
		EXT_CHARS.put(new Character('+'), ESCAPE_SLASH + "K");
		EXT_CHARS.put(new Character(','), ESCAPE_SLASH + "L");
		EXT_CHARS.put(new Character('/'), ESCAPE_SLASH + "O");
		EXT_CHARS.put(new Character(':'), ESCAPE_SLASH + "Z");
		EXT_CHARS.put(new Character(';'), ESCAPE_PERCENT + "F");
		EXT_CHARS.put(new Character('<'), ESCAPE_PERCENT + "G");
		EXT_CHARS.put(new Character('='), ESCAPE_PERCENT + "H");
		EXT_CHARS.put(new Character('>'), ESCAPE_PERCENT + "I");
		EXT_CHARS.put(new Character('?'), ESCAPE_PERCENT + "J");
		EXT_CHARS.put(new Character('@'), ESCAPE_PERCENT + "V");
		EXT_CHARS.put(new Character('['), ESCAPE_PERCENT + "K");
		EXT_CHARS.put(new Character('\\'), ESCAPE_PERCENT + "L");
		EXT_CHARS.put(new Character(']'), ESCAPE_PERCENT + "M");
		EXT_CHARS.put(new Character('^'), ESCAPE_PERCENT + "N");
		EXT_CHARS.put(new Character('_'), ESCAPE_PERCENT + "O");
		EXT_CHARS.put(new Character('`'), ESCAPE_PERCENT + "W");
		EXT_CHARS.put(new Character('{'), ESCAPE_PERCENT + "P");
		EXT_CHARS.put(new Character('|'), ESCAPE_PERCENT + "Q");
		EXT_CHARS.put(new Character('}'), ESCAPE_PERCENT + "R");
		EXT_CHARS.put(new Character('~'), ESCAPE_PERCENT + "S");

		EXT_CHARS.put(new Character('a'), ESCAPE_PLUS + "A");
		EXT_CHARS.put(new Character('b'), ESCAPE_PLUS + "B");
		EXT_CHARS.put(new Character('c'), ESCAPE_PLUS + "C");
		EXT_CHARS.put(new Character('d'), ESCAPE_PLUS + "D");
		EXT_CHARS.put(new Character('e'), ESCAPE_PLUS + "E");
		EXT_CHARS.put(new Character('f'), ESCAPE_PLUS + "F");
		EXT_CHARS.put(new Character('g'), ESCAPE_PLUS + "G");
		EXT_CHARS.put(new Character('h'), ESCAPE_PLUS + "H");
		EXT_CHARS.put(new Character('i'), ESCAPE_PLUS + "I");
		EXT_CHARS.put(new Character('j'), ESCAPE_PLUS + "J");
		EXT_CHARS.put(new Character('k'), ESCAPE_PLUS + "K");
		EXT_CHARS.put(new Character('l'), ESCAPE_PLUS + "L");
		EXT_CHARS.put(new Character('m'), ESCAPE_PLUS + "M");
		EXT_CHARS.put(new Character('n'), ESCAPE_PLUS + "N");
		EXT_CHARS.put(new Character('o'), ESCAPE_PLUS + "O");
		EXT_CHARS.put(new Character('p'), ESCAPE_PLUS + "P");
		EXT_CHARS.put(new Character('q'), ESCAPE_PLUS + "Q");
		EXT_CHARS.put(new Character('r'), ESCAPE_PLUS + "R");
		EXT_CHARS.put(new Character('s'), ESCAPE_PLUS + "S");
		EXT_CHARS.put(new Character('t'), ESCAPE_PLUS + "T");
		EXT_CHARS.put(new Character('u'), ESCAPE_PLUS + "U");
		EXT_CHARS.put(new Character('v'), ESCAPE_PLUS + "V");
		EXT_CHARS.put(new Character('w'), ESCAPE_PLUS + "W");
		EXT_CHARS.put(new Character('x'), ESCAPE_PLUS + "X");
		EXT_CHARS.put(new Character('y'), ESCAPE_PLUS + "Y");
		EXT_CHARS.put(new Character('z'), ESCAPE_PLUS + "Z");
	}

}
