package com.gaspar.pdfutils.modes;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * This mode extracts the pages in range to a separate pdf file.
 * @author Gáspár Tamás
 */
public class ModeExtractToPdf extends Mode {

	/**
	 * Start extract at this page;
	 */
	private final int fromPage;
	/**
	 * End extract at C:\_MSC\MSC-2\AngolInformatikathis page (inclusive).
	 */
	private final int toPage;
	/**
	 * This will be the name of the extracted PDF (dont write .pdf extension).
	 */
	private final String name;
	
	/**
	 * Contructor for this mode.
	 * @param fromPage Start extract at this page.
	 * @param toPage End extract at this page (inclusive).
	 * @param name This will be the name of the extracted PDF (dont write .pdf extension).
	 */
	public ModeExtractToPdf(int fromPage, int toPage, String name) {
		super(Mode.MODE_EXTRACT_TO_PDF);
		this.fromPage = fromPage;
		this.toPage = toPage;
		this.name = name;
	}

	/**
	 * Extracts the specified pages and converts them into a single PDF file.
	 * @param sourcePdfPath The path of the selected pdf file.
	 * @param destinationPath The path where the new PDF will be placed.
	 * @throws IOException When the source or destination cant be opened.
	 */
	@Override
	public void execute(String sourcePdfPath, String destinationPath) throws IOException {
		try(PDDocument document = PDDocument.load(new File(sourcePdfPath))) {
			try(PDDocument extracted = new PDDocument()) { //the new document
				for(int i=fromPage; i<=toPage; i++) {
					final PDPage page = document.getPage(i);
					extracted.addPage(page);
				}
				extracted.save(new File(destinationPath + "/" + name + ".pdf"));
			}
		}
	}
}
