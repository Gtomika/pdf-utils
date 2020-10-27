package com.gaspar.pdfutils.modes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import com.gaspar.pdfutils.OperationThread;
import com.gaspar.pdfutils.PdfUtilsMain;
import com.gaspar.pdfutils.gui.ModeExtractToPdfPanel;
import com.gaspar.pdfutils.gui.RootPanel;

/**
 * This mode extracts the pages in range to a separate PDF file. 
 * You can give a range of pages using {@link ModeExtractToPdf#ModeExtractToPdf(int, int, String)} or specify 
 * individual pages using {@link ModeExtractToPdf#ModeExtractToPdf(List, String)}.
 * @author Gáspár Tamás
 */
public class ModeExtractToPdf extends Mode {

	/**
	 * Start extract at this page. This is 1 BASED, not 0 based!
	 */
	private final int fromPage;
	/**
	 * End extract at this page (inclusive). This is 1 BASED, not 0 based!
	 */
	private final int toPage;
	/**
	 * This will be the name of the extracted PDF (may or may not contain the .pdf extension).
	 */
	private final String name;
	/**
	 * Pages to be extracted. If this object is created with a page range, then this is null. This is from the UI, so numbers are 
	 * 1 based.
	 */
	private final List<Integer> pageNumbers;
	/**
	 * Password for the original PDF file. Null if no password is given. Must be set after object creation.
	 */
	private String password = null;
	/**
	 * Password for the generated PDF file. Null if the file will not be protected. Must be set after object creation.
	 */
	private String resultPassword = null;
	
	/**
	 * Contructor which specifies a page range. 
	 * @param fromPage Start extract at this page.
	 * @param toPage End extract at this page (inclusive).
	 * @param name This will be the name of the extracted PDF (dont write .pdf extension).
	 */
	public ModeExtractToPdf(int fromPage, int toPage, String name) {
		super(Mode.MODE_EXTRACT_TO_PDF);
		this.fromPage = fromPage;
		this.toPage = toPage;
		this.name = name;
		pageNumbers = null;
	}

	/**
	 * Conostructor which specifies the pages to be extracted directly.
	 * @param pageNumbers The page numbers in a list. This is from the UI, so numbers are 1 based.
	 * @param imagePrefix The image prefix.
	 */
	public ModeExtractToPdf(List<Integer> pageNumbers, String name) {
		super(Mode.MODE_EXTRACT_TO_PDF);
		this.pageNumbers = pageNumbers;
		this.name = name;
		toPage = -1; //these do not matter when pages are specified in a list
		fromPage = -1;
	}
	
	/**
	 * A default constructor to create a mode object only for dislaying name 
	 * and description. {@link #execute(String, String)} should not be called on this!
	 */
	public ModeExtractToPdf() {
		this(-1,-1,"img_");
	}
	
	/**
	 * Extracts the specified pages and converts them into a single PDF file. This is run on an {@link OperationThread} in the background, so 
	 * changes to the GUI must be made with {@link SwingUtilities#invokeLater(Runnable)}.
	 * @param sourcePdfPath The path of the selected pdf file.
	 * @param destinationPath The path where the new PDF will be placed.
	 * @throws IOException When the source or destination cant be opened.
	 * @throws InvalidPasswordException When the specified password is incorrect.
	 */
	@Override
	public void execute(String sourcePdfPath, String destinationPath) throws IOException, InvalidPasswordException {
		try(PDDocument document = password==null ? PDDocument.load(new File(sourcePdfPath)) : PDDocument.load(new File(sourcePdfPath), password)) {
			
			SwingUtilities.invokeLater(() -> RootPanel.getInstance().updateOperationProgress(0)); //show 0 progress
			
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
			
			try(PDDocument extracted = new PDDocument()) { //the new document
				
				//handle the encryption if needed
				if(resultPassword != null) {
					int keyLength = 128;
					AccessPermission ap = new AccessPermission();
					StandardProtectionPolicy spp = new StandardProtectionPolicy(resultPassword, resultPassword, ap);
					spp.setEncryptionKeyLength(keyLength);
					spp.setPermissions(ap);
					extracted.protect(spp);
				}
				
				for(int pageNumber: _pageNumbers) {
					final PDPage page = document.getPage(pageNumber);
					extracted.addPage(page);
					
					//update operation progress on GUI
	                final int newPercent = (int)(100*(Double.valueOf(pageNumber)/_pageNumbers.size()));
	                SwingUtilities.invokeLater(() -> RootPanel.getInstance().updateOperationProgress(newPercent));
				}
				//create name and save
				String nameWithExtension = name.endsWith(".pdf") ? name : name+".pdf";
				extracted.save(new File(destinationPath + "/" + nameWithExtension));
			}
		}
	}
	
	/**
	 * This method will attempt to extract the images using a page range. If something goes wrong, a dialog will display the problem.
	 * These parameters are not checked in any ways here!
	 * @param fromPage Start page as string.
	 * @param toPage End page as string.
	 * @param name Name of the resulting PDF file.
	 * @param sourcePath Path of PDF file.
	 * @param destPath Path of the images
	 * @param password Password for the original PDF file.
	 * @param resultPassword Password for the generated PDF file.
	 */
	public static void attemptPdfExtraction(String fromPage, String toPage, String name, String sourcePath, 
			String destPath, String password, String resultPassword) {
		
		if(name.isEmpty()) {
			JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "No name given for the PDF file!", "Invalid name", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int fromPageInt, toPageInt;
		try {
			fromPageInt = Integer.parseInt(fromPage);
			toPageInt = Integer.parseInt(toPage);
			
			ModeExtractToPdf mode = new ModeExtractToPdf(fromPageInt, toPageInt, name);
			if(!password.isEmpty()) mode.password = password;
			if(!resultPassword.isEmpty()) mode.resultPassword = resultPassword;
			
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
	 * This method will attempt to extract the PDF pages using directly specified page numbers. If something goes wrong, a dialog will display the problem.
	 * These parameters are not checked in any ways here!
	 * @param csvPages The pages in a comma separated string, and are 1 based! for example 1,3,7,2. Can be in any order and can contain duplicates.
	 * @param name Name of the resulting PDF file.
	 * @param sourcePath Path of PDF file.
	 * @param destPath Path of the generated PDF file..
	 * @param password Password for the source PDF file.
	 * @param resultPassword Password for the generated PDF file.
	 */
	public static void attemptPdfExtraction(String csvPages, String name, String sourcePath, String destPath, String password, String resultPassword) {
		try {
			if(name.isEmpty()) {
				JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "No name given for the PDF file!", "Invalid name", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			List<Integer> pageNumbers = Arrays.asList(csvPages.split(",")).stream()
					.map(p -> Integer.parseInt(p))
					.collect(Collectors.toList()); //attempt to convert them into a valid int list
			
			ModeExtractToPdf mode = new ModeExtractToPdf(pageNumbers, name);
			if(!password.isEmpty()) mode.password = password;
			if(!resultPassword.isEmpty()) mode.resultPassword = resultPassword;
			
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
		return "Extract pages from a PDF file. The pages will be combined into a new PDF file.";
	}

	@Override
	public JPanel getModePanel() {
		return new ModeExtractToPdfPanel();
	}
}
