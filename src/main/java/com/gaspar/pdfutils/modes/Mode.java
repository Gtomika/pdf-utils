package com.gaspar.pdfutils.modes;

import java.io.IOException;

/**
 * Mode base class.
 * @author Gáspár Tamás
 */
public abstract class Mode {

	/**
	 * This mode extracts the specifies pages to separate images.
	 */
	public static final String MODE_EXTRACT_TO_IMAGES = "--EXTRACT_TO_IMAGES";
	
	/**
	 * One of the mode constants
	 */
	protected String modeName;
	
	protected Mode(String modeName) {
		this.modeName = modeName;
	}
	
	/**
	 * Executes this mode on the selected pdf.
	 * @param sourcePdfPath The path of the selected pdf file.
	 * @param destinationPath The path where the result will be placed.
	 * @throws IOException When the source or destination cant be opened.
	 */
	public abstract void execute(String sourcePdfPath, String destinationPath) throws IOException;
}
