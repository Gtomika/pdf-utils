package com.gaspar.pdfutils.modes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.gaspar.pdfutils.OperationThread;
import com.gaspar.pdfutils.PdfUtilsMain;
import com.gaspar.pdfutils.gui.ModeExtractToImagesPanel;
import com.gaspar.pdfutils.gui.RootPanel;

/**
 * This mode extracts the specifies pages to separate images. The images will be suffixed in a way that 
 * ABC ordering their names will give sort them in their original order. This is useful to the mode 
 * {@link ModeImagesToPdf}, which will be able to combine these images in their original order!
 * <p>
 * You can give a range of pages using {@link ModeExtractToImages#ModeExtractToImages(int, int, String)} or specify 
 * individual pages using {@link ModeExtractToImages#ModeExtractToImages(List, String)}.
 */
public class ModeExtractToImages extends Mode {

	/**
	 * Start extract at this page. This is 1 BASED, not 0 based!
	 */
	private final int fromPage;
	/**
	 * End extract at this page (inclusive). This is 1 BASED, not 0 based!
	 */
	private final int toPage;
	/**
	 * Pages to be extracted. If this object is created with a page range, then this is null. This is from the UI, so numbers are 
	 * 1 based.
	 */
	private List<Integer> pageNumbers;
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
	private String password = null;
	
	/**
	 * Constructor with specified image prefix, and a page range.
	 * @param fromPage Start extract at this page. 1 based!
	 * @param toPage End extract at this page (inclusive). 1 based!
	 * @param imageNamePrefix Generated images will be enumerated with this prefix.
	 */
	public ModeExtractToImages(int fromPage, int toPage, String imageNamePrefix) {
		super(Mode.MODE_EXTRACT_TO_IMAGES);
		this.fromPage = fromPage;
		this.toPage = toPage;
		this.imageNamePrefix = imageNamePrefix;
	}
	
	/**
	 * Conostructor which specifies the pages to be extracted directly.
	 * @param pageNumbers The page numbers in a list. This is from the UI, so numbers are 1 based.
	 * @param imagePrefix The image prefix.
	 */
	public ModeExtractToImages(List<Integer> pageNumbers, String imagePrefix) {
		super(Mode.MODE_EXTRACT_TO_IMAGES);
		this.pageNumbers = pageNumbers;
		this.imageNamePrefix = imagePrefix;
		toPage = -1; //these do not matter when pages are specified in a list
		fromPage = -1;
	}
	
	/**
	 * A default constructor to create a mode object only for dislaying name 
	 * and description. {@link #execute(String, String)} should not be called on this!
	 */
	public ModeExtractToImages() {
		this(-1,-1,"img_");
	}

	/**
	 * Extracts the specified pages and converts them into images. This is run on an {@link OperationThread} in the background, so 
	 * changes to the GUI must be made with {@link SwingUtilities#invokeLater(Runnable)}.
	 * @param sourcePdfPath The path of the selected pdf file.
	 * @param destinationPath The path where the images will be placed.
	 * @throws IOException When the source or destination cant be opened.
	 */
	@Override
	public void execute(String sourcePdfPath, String destPath) throws IOException {
		try(PDDocument document = password==null ? PDDocument.load(new File(sourcePdfPath)) : PDDocument.load(new File(sourcePdfPath), password)) {
			
			SwingUtilities.invokeLater(() -> RootPanel.getInstance().updateOperationProgress(0)); //show 0 progress
			
			final PDFRenderer pdfRenderer = new PDFRenderer(document);
			
			List<Integer> _pageNumbers = null; //fill a list with 0 based indices
			if(pageNumbers != null) { //already have a list, reduce indices by one, to make it 0 based
				_pageNumbers = pageNumbers.stream().map(page -> page-1).collect(Collectors.toList());
			} else {
				_pageNumbers = new ArrayList<>(); //fill list from range, reducing by one to make it 0 based
				for(int i=fromPage-1; i<toPage; i++) {
					_pageNumbers.add(i);
				}
			}
			//now it does not matter if it was page range or not, _pageNumbers has the 0 based page indices
			int amount = _pageNumbers.size();
			int digits = String.valueOf(amount).length();
			
			int counter = 1;
			for(int i: _pageNumbers) {
				final BufferedImage image = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);
				
				String orderingString = ""; //guarantes the same ABC ordering
				for(int j = digits-1; j >= 0; j--) {
					orderingString += (counter/(int) Math.pow(10, j));
					if(j>0) orderingString += "_";
				}
				
                String fileName = destPath + "/" + imageNamePrefix + orderingString + ".png";
                ImageIO.write(image, "png", new File(fileName));
                counter++;
                
                //update operation progress on GUI
                final int newPercent = (int)(100*(Double.valueOf(i)/_pageNumbers.size()));
                SwingUtilities.invokeLater(() -> RootPanel.getInstance().updateOperationProgress(newPercent));
			}
		}
	}
	
	/**
	 * This method will attempt to extract the images using a page range. If something goes wrong, a dialog will display the problem.
	 * These parameters are not checked in any ways here!
	 * @param fromPage Start page as string.
	 * @param toPage End page as string.
	 * @param imagePrefix Prefix of generated images.
	 * @param sourcePath Path of PDF file.
	 * @param destPath Path of the images.
	 */
	public static void attemptImageExtraction(String fromPage, String toPage, String imagePrefix, String sourcePath, String destPath, String password) {
		if(imagePrefix.isEmpty()) {
			JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "There must be an image prefix!", "Invalid prefix", JOptionPane.ERROR_MESSAGE);
		}
		int fromPageInt, toPageInt;
		try {
			fromPageInt = Integer.parseInt(fromPage);
			toPageInt = Integer.parseInt(toPage);
			
			ModeExtractToImages mode = new ModeExtractToImages(fromPageInt, toPageInt, imagePrefix);
			if(!password.isEmpty()) mode.setPassword(password);
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
			opThread.start();
		} catch (NumberFormatException e) {
			String fromInput = fromPage.isEmpty() ? "[EMPTY]" : fromPage;
			String toInput = toPage.isEmpty() ? "[EMPTY]" : toPage;
			JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), fromInput+" and "+toInput+" is not a valid range of pages in this document!","Invalid pages", JOptionPane.ERROR_MESSAGE);
		} 
	}
	
	/**
	 * This method will attempt to extract the images using directly specified pages. If something goes wrong, a dialog will display the problem.
	 * These parameters are not checked in any ways here!
	 * @param csvPages The pages in a comma separated string, and are 1 based! for example 1,3,7,2. Can be in any order and can contain duplicates.
	 * @param imagePrefix Prefix of generated images.
	 * @param sourcePath Path of PDF file.
	 * @param destPath Path of the images.
	 */
	public static void attemptImageExtraction(String csvPages, String imagePrefix, String sourcePath, String destPath, String password) {
		try {
			if(imagePrefix.isEmpty()) {
				JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "There must be an image prefix!", "Invalid prefix", JOptionPane.ERROR_MESSAGE);
			}
			
			List<Integer> pageNumbers = Arrays.asList(csvPages.split(",")).stream()
					.map(p -> Integer.parseInt(p))
					.collect(Collectors.toList()); //attempt to convert them into a valid int list
			
			ModeExtractToImages mode = new ModeExtractToImages(pageNumbers, imagePrefix);
			if(!password.isEmpty()) mode.setPassword(password);
			
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
			opThread.start();
		} catch (NumberFormatException e) {
			String input = csvPages.isEmpty() ? "[EMPTY]" : csvPages;
			JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), input+" is not a valid page specifier! Use the page selector tool!","Invalid pages", JOptionPane.ERROR_MESSAGE);
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
