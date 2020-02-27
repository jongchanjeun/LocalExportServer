package org.ubstorm.service.event;

public class UnImplementedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnImplementedException(Class cls, String methodname) {
		super("Method " + methodname + " of Class " + cls.getName() + " not implemented.");
	}
}
