package com.gaspar.pdfutils.modes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * This mode combines images into a single pdf files, with each image as a page. This works best when 
 * the images are extracted PDF pages. Works only for PNG images.
 * <p>
 * Warning: images in the source folder are compiled in ABC ordering.
 * @author Gáspár Tamás
 */
public class ModeImagesToPdf extends Mode {

	/**
	 * Only images with this prefix will be combined.
	 */
	private final String imagePrefix;
	/**
	 * Name of the resulting PDF file.
	 */
	private final String name;
	
	/**
	 * Contructor for this mode with specified image prefix.
	 * @param imagePrefix Only images with this prefix will be combined.
	 * @param name Name of the resulting PDF file.
	 */
	public ModeImagesToPdf(String name, String imagePrefix) {
		super(Mode.MODE_IMAGES_TO_PDF);
		this.imagePrefix = imagePrefix;
		this.name = name;
	}
	
	/**
	 * Contructor for this mode with no image prefix (all PNG-s in the source directory will be combined).
	 * @param name Name of the resulting PDF file.
	 */
	public ModeImagesToPdf(String name) {
		super(Mode.MODE_IMAGES_TO_PDF);
		this.imagePrefix = null;
		this.name = name;
	}

	/**
	 * Combines images into a PDF file.
	 * @param sourcePath This is the folder where the images are. 
	 * @param destinationPath The path where the new PDF will be placed.
	 * @throws IOException When the source or destination cant be opened.
	 */
	@Override
	public void execute(String sourcePath, String destinationPath) throws IOException {
		try(PDDocument combined = new PDDocument()) {
			File sourceFolder = new File(sourcePath);
			if(!sourceFolder.isDirectory()) throw new IllegalArgumentException("Source path must be a directory where the images are!");
			File[] images = sourceFolder.listFiles();
			
			List<File> validImages = Arrays.asList(images).stream() //wow
					.filter(img -> isValidImage(img.getPath()))
					.sorted(Comparator.comparing(img -> Paths.get(img.getPath()).getFileName()))
					.collect(Collectors.toList());
			
			for(File image: validImages) {
				 PDPage page = new PDPage();
		         combined.addPage(page);
		         final PDRectangle mediaBox = page.getMediaBox();
		         final PDImageXObject pdImage = PDImageXObject.createFromFile(image.getPath(), combined);
		         
		         try(PDPageContentStream contents = new PDPageContentStream(combined, page)) {
		        	 contents.drawImage(pdImage, 0, 0, mediaBox.getWidth(), mediaBox.getHeight());
		         }
			}
			combined.save(new File(destinationPath + "/" + name + ".pdf"));
		}
	}
	
	/**
	 * Determines if this image is to be combined.
	 * @param p The image path.
	 * @return True only if this is a PNG file and the prefix matches, if there is one.
	 */
	private boolean isValidImage(String p) {
		boolean valid = p.endsWith(".png");
		if(imagePrefix != null) {
			String fileName = Paths.get(p).getFileName().toString();
			valid = fileName.startsWith(imagePrefix);
		}
		return valid;
	}
}
