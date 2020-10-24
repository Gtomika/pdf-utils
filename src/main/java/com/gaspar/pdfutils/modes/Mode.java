package com.gaspar.pdfutils.modes;

import java.io.IOException;

import javax.swing.JPanel;

/**
 * Mode base class.
 * @author Gáspár Tamás
 */
public abstract class Mode {

	/**
	 * This mode extracts the pages in range to separate images.
	 */
	public static final String MODE_EXTRACT_TO_IMAGES = "Extract to images";
	/**
	 * This mode extracts the pages in range to a separate pdf file.
	 */
	public static final String MODE_EXTRACT_TO_PDF = "Extract to PDF";
	/**
	 * This mode combines images into a single pdf files, with each image as a page. This works best when 
	 * the images are extracted PDF pages. Works only for PNG images.
	 */
	public static final String MODE_IMAGES_TO_PDF = "Images to PDF";
	
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
	
	/**
	 * Provides a description for this mode to be displayed in the mode selection panel.
	 * @return The description.
	 */
	public abstract String getDescription();
	
	/**
	 * Creates a panel where the user can enter information needed to execute this mode.
	 * @return The panel.
	 */
	public abstract JPanel getModePanel();
	
	/**
	 * Legacy constant for image extract mode. Only used in console mode.
	 */
	public static final String MODE_EXTRACT_TO_IMAGES_LEGACY = "--EXTRACT_TO_IMAGES";
	/**
	 * Legacy constant for PDF extract mode. Only used in console mode.
	 */
	public static final String MODE_EXTRACT_TO_PDF_LEGACY = "--EXTRACT_TO_PDF";
	/**
	 * Legacy constant for image combine mode. Only used in console mode.
	 */
	public static final String MODE_IMAGES_TO_PDF_LEGACY = "--IMAGES_TO_PDF";
}
