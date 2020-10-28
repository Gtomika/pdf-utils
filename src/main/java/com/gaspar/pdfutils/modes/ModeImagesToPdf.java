package com.gaspar.pdfutils.modes;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.gaspar.pdfutils.OperationThread;
import com.gaspar.pdfutils.PdfUtilsMain;
import com.gaspar.pdfutils.gui.ModeImagesToPdfPanel;
import com.gaspar.pdfutils.gui.RootPanel;

/**
 * This mode combines images into a single pdf files, with each image as a page. This works best when 
 * the images are extracted PDF pages and they need to be recombined after some edits.
 * <p>
 * The following image formats are supported: JPG, JPEG, TIF, TIFF, GIF, BMP and PNG.
 * @author Gáspár Tamás
 */
public class ModeImagesToPdf extends Mode {
	
	public static final String[] SUPPORTED_FORMATS = {"jpg","jpeg","tif","tiff","gif","bmp","png"};

	/**
	 * Name of the resulting PDF file. May or may not contain the '.pdf' extension.
	 */
	private final String name;
	/**
	 * Password for the resulting PDF file. If no password is needed, this is null. Must be set after object creation.
	 */
	private String password;
	/**
	 * Stores the image files that needs to be combined. They dont need to be in one folder. All of these images 
	 * are one of the supported formats, as the file chooser only allows to select those.
	 */
	private final List<File> images;
	
	/**
	 * Contructor for image combination mode.
	 * @param name Name of the resulting PDF file.
	 */
	public ModeImagesToPdf(String name, final List<File> images) {
		super(Mode.MODE_IMAGES_TO_PDF);
		this.name = name;
		this.images = images;
	}

	/**
	 * A default constructor to create a mode object only for dislaying name 
	 * and description. {@link #execute(String, String)} should not be called on this!
	 */
	public ModeImagesToPdf() {
		this("combined",null);
	}
	
	/**
	 * Combines images into a PDF file. This is run on an {@link OperationThread} in the background, so 
	 * changes to the GUI must be made with {@link SwingUtilities#invokeLater(Runnable)}.
	 * @param sourcePath For this mode, this parameter is ignored, since the images may be in multiple folders, and their path is 
	 * already specified in {@link #images} file array. 
	 * @param destinationPath The path where the new PDF will be placed.
	 * @throws IOException When the source or destination cant be opened.
	 */
	@Override
	public void execute(String sourcePath, String destinationPath) throws IOException {
		try(PDDocument combined = new PDDocument()) {
			SwingUtilities.invokeLater(() -> RootPanel.getInstance().updateOperationProgress(0)); //show 0 progress
			
			//handle the encryption if needed
			if(password != null) {
				int keyLength = 128;
				AccessPermission ap = new AccessPermission();
				StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, ap);
				spp.setEncryptionKeyLength(keyLength);
				spp.setPermissions(ap);
				combined.protect(spp);
			}
			int counter = 0;
			for(File image: images) {
				 final PDPage page = new PDPage();
		         combined.addPage(page);
		         final PDRectangle mediaBox = page.getMediaBox();
		         final PDImageXObject pdImage = PDImageXObject.createFromFile(image.getPath(), combined);
		         
		         try(PDPageContentStream contents = new PDPageContentStream(combined, page)) {
		        	 contents.drawImage(pdImage, 0, 0, mediaBox.getWidth(), mediaBox.getHeight());
		         }
		         //update operation progress on GUI
	             final int newPercent = (int)(100*(Double.valueOf(counter++)/images.size()));
	             SwingUtilities.invokeLater(() -> RootPanel.getInstance().updateOperationProgress(newPercent));
			}
			//create name and save
			String nameWithExtension = name.endsWith(".pdf") ? name : name+".pdf";
			combined.save(new File(destinationPath + "/" + nameWithExtension));
		}
	}
	
	/**
	 * Attempts to combine the images into a PDF file.
	 * @param destPath The folder where the new PDF will be places.
	 * @param name Name of the resulting PDF.
	 * @param images File objects for each image to be included.
	 * @param password Password for the new PDF.
	 */
	public static void attemptImageCombination(String destPath, String name, final List<File> images, String password) {
		if(name.isEmpty()) {
			JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "You must specify a name for the PDF file!","Invalid name",JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(images.size() == 0) {
			JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "You must select at least one image!","No images",JOptionPane.ERROR_MESSAGE);
			return;
		}
		final ModeImagesToPdf mode = new ModeImagesToPdf(name, images);
		if(!password.isEmpty()) mode.password = password;
		//this handler displays dialogs from background exceptions
		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
		    @Override
		    public void uncaughtException(Thread th, Throwable ex) {
		        JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		    }
		};
		OperationThread opThread = new OperationThread(() -> {
			try {
				mode.execute("", destPath); //this mode ignores source path parameter
			} catch(InvalidPasswordException e) {
				throw new RuntimeException("Password is incorrect for this PDF file!");
			} catch (IOException e) {
				throw new RuntimeException("The source or destination files could not be opened! Maybe they don't exist or this app does not have permission to read/write there.");
			}
		});
		opThread.setUncaughtExceptionHandler(h);
		opThread.start();
	}

	@Override
	public String getDescription() {
		return "Combine images into a PDF file. This works best if the images are extracted PDF pages.";
	}

	@Override
	public JPanel getModePanel() {
		return new ModeImagesToPdfPanel();
	}
}
