package com.gaspar.pdfutils.modes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.gaspar.pdfutils.OperationThread;
import com.gaspar.pdfutils.PdfUtilsMain;
import com.gaspar.pdfutils.gui.ModeExtractToImagesPanel;

/**
 * This mode extracts the specifies pages to separate images. The images will be suffixed in a way that 
 * ABC ordering their names will give sort them in their original order. This is useful to the mode 
 * {@link ModeImagesToPdf}, which will be able to combine these images in their original order!
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
	 * Password for the pdf file. Null if no password is given. Must be set after object creation.
	 */
	private String password;
	
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
		password = null;
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
		password = null;
	}
	
	/**
	 * A default constructor to create a mode object only for dislaying name 
	 * and description. {@link #execute(String, String)} should not be called on this!
	 */
	public ModeExtractToImages() {
		this(-1,-1,"img_");
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
			
			int amount = toPage - fromPage;
			int digits = String.valueOf(amount).length();
			
			int counter = 1;
			for(int i=fromPage; i<=toPage; i++) {
				final BufferedImage image = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);
				
				String orderingString = ""; //guarantes the same ABC ordering
				for(int j = digits-1; j >= 0; j--) {
					orderingString += (counter/(int) Math.pow(10, j));
					if(j>0) orderingString += "_";
				}
				
                String fileName = destPath + "/" + imageNamePrefix + orderingString + ".png";
                ImageIO.write(image, "png", new File(fileName));
                counter++;
			}
		}
	}
	
	/**
	 * This method will attempt to extract the images with the given data. If something goes wrong, a dialog will display the problem.
	 * These parameters are not checked in any ways here!
	 * @param fromPage Start page as string.
	 * @param toPage End page as string.
	 * @param imagePrefix Prefix of generated images.
	 * @param sourcePath Path of PDF file.
	 * @param destPath Path of the images.
	 */
	public static void attemptImageExtraction(String fromPage, String toPage, String imagePrefix, String sourcePath, String destPath, String password) {
		int fromPageInt, toPageInt;
		try {
			fromPageInt = Integer.parseInt(fromPage);
			toPageInt = Integer.parseInt(toPage);
			
			ModeExtractToImages mode = new ModeExtractToImages(fromPageInt, toPageInt, imagePrefix);
			mode.setPassword(password);
			//this handler displays dialogs from background exceptions
			Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
			    @Override
			    public void uncaughtException(Thread th, Throwable ex) {
			        JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			    }
			};
			
			OperationThread opThread = new OperationThread(() -> {
				try {
					mode.execute(sourcePath, destPath);
				} catch(InvalidPasswordException e) {
					throw new RuntimeException("Password is incorrect for this PDF file!");
				} catch (IOException e) {
					throw new RuntimeException("The source or destination file could not be opened! Maybe they don't exist or this app does not have permission to read/write there.");
				}
			});
			opThread.setUncaughtExceptionHandler(h);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), fromPage+" and "+toPage+" is not a valid range of pages in this document!","Invalid pages", JOptionPane.ERROR_MESSAGE);
			return;
		} 
	}

	@Override
	public String getDescription() {
		return "Extract pages from a PDF. Each selected page will be a separate image.";
	}

	@Override
	public JPanel getModePanel() {
		return new ModeExtractToImagesPanel();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
