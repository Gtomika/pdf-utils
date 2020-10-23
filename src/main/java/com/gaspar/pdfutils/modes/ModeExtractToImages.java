package com.gaspar.pdfutils.modes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * This mode extracts the specifies pages to separate images.
 */
public class ModeExtractToImages extends Mode {

	/**
	 * Start extract at this page;
	 */
	private final int fromPage;
	/**
	 * End extract at this page (inclusive).
	 */
	private final int toPage;
	/**
	 * Generated images will be enumerated with this prefix. For example if you set this "homework_" then 
	 * the generated images will be "homework_1", "homework_2", ...
	 * <p>
	 * This is optional, default value is "img_"
	 */
	private final String imageNamePrefix;
	
	/**
	 * Constructor with specified image prefix.
	 * @param fromPage Start extract at this page.
	 * @param toPage End extract at this page (inclusive).
	 * @param imageNamePrefix Generated images will be enumerated with this prefix.
	 */
	public ModeExtractToImages(int fromPage, int toPage, String imageNamePrefix) {
		super(Mode.MODE_EXTRACT_TO_IMAGES);
		this.fromPage = fromPage;
		this.toPage = toPage;
		this.imageNamePrefix = imageNamePrefix;
	}
	
	/**
	 * Constructor with default "img_" image prefix.
	 * @param fromPage Start extract at this page.
	 * @param toPage End extract at this page (inclusive).
	 */
	public ModeExtractToImages(int fromPage, int toPage) {
		super(Mode.MODE_EXTRACT_TO_IMAGES);
		this.fromPage = fromPage;
		this.toPage = toPage;
		this.imageNamePrefix = "img_";
	}

	/**
	 * Extracts the specified pages and converts them into images.
	 * @param sourcePdfPath The path of the selected pdf file.
	 * @param destinationPath The path where the images will be placed.
	 * @throws IOException When the source or destination cant be opened.
	 */
	@Override
	public void execute(String sourcePdfPath, String destPath) throws IOException {
		try(PDDocument document = PDDocument.load(new File(sourcePdfPath))) {
			final PDFRenderer pdfRenderer = new PDFRenderer(document);
			int counter = 1;
			for(int i=fromPage; i<=toPage; i++) {
				final BufferedImage image = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);
                String fileName = destPath + "/" + imageNamePrefix + (++counter) + ".png";
                ImageIO.write(image, "png", new File(fileName));
			}
		}
	}
}
