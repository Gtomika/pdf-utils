package com.gaspar.pdfutils.modes;

import java.io.IOException;

/**
 * Mode base class.
 * @author Gáspár Tamás
 */
public abstract class Mode {

	/**
	 * This mode extracts the pages in range to separate images.
	 */
	public static final String MODE_EXTRACT_TO_IMAGES = "--EXTRACT_TO_IMAGES";
	/**
	 * This mode extracts the pages in range to a separate pdf file.
	 */
	public static final String MODE_EXTRACT_TO_PDF = "--EXTRACT_TO_PDF";
	/**
	 * This mode combines images into a single pdf files, with each image as a page. This works best when 
	 * the images are extracted PDF pages. Works only for PNG images.
	 */
	public static final String MODE_IMAGES_TO_PDF = "--IMAGES_TO_PDF";
	
	/**
	 * One of the mode constants
	 */
	protected String modeName;
	
	protected Mode(String modeName) {
		this.modeName = modeName;
	}
	
	/**
	 * Executes this mode on the selected pdf.
	 * @param sourcePath The path of the selected pdf file.
	 * @param destinationPath The path where the result will be placed.
	 * @throws IOException When the source or destination cant be opened.
	 */
	public abstract void execute(String sourcePath, String destinationPath) throws IOException;
}
