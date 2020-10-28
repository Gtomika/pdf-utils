package com.gaspar.pdfutils.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.gaspar.pdfutils.PdfUtilsMain;
import com.gaspar.pdfutils.modes.ModeExtractToPdf;

/**
 * This panel displays options for the user to enter details about the 
 * PDF extraction, such as file paths.
 * @author Gáspár Tamás
 * @see ModeExtractToPdf
 */
public class ModeExtractToPdfPanel extends JPanel {
	/**
	 * Contains the path of the source PDF.
	 */
	private final JTextField sourcePathField = new JTextField();
	/**
	 * Contains the path of the folder where the result pdf will be placed.
	 */
	private final JTextField destPathField = new JTextField();
	/**
	 * Optionally, enter the password for the PDF to be opened here.
	 */
	private final JPasswordField passwordField = new JPasswordField();
	/**
	 * This field is for the name of the resulting PDF file.
	 */
	private final JTextField nameField = new JTextField();
	/**
	 * Selects or deselects using password for the generated PDF file.
	 */
	final JCheckBox usePasswordCheckbox = new JCheckBox("Encrypt generated PDF with a password");
	/*
	 * This field is for the password for the new PDF file.
	 */
	private JPasswordField resultPasswordField = new JPasswordField();
	/**
	 *  This field is for the confirmation password for the new PDF file. Must have the same value as {@link #resultPasswordField}.
	 */
	private JPasswordField resultPasswordConfirmField = new JPasswordField();
	
	/**
	 * Builds a panel where all input fields are empty.
	 */
	public ModeExtractToPdfPanel() {
		final JPanel container = new JPanel();
		container.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		final BoxLayout gl = new BoxLayout(container, BoxLayout.Y_AXIS);
		container.setAlignmentX(LEFT_ALIGNMENT);
		container.setLayout(gl);
		
		final Font font = new Font("SansSerif", Font.PLAIN, 15);
		addSourceDestInputs(container, font);
		
		addPdfEncryptInputs(container, font);
		
		addPageSelectingTools(container, font);
		
		add(container);
	}

	/**
	 * Add tools to select source and destination path. Password for the PDF to be opened must be inputed here.
	 * @param container Will be added to this.
	 * @param font Texts will have this font.
	 */
	private void addSourceDestInputs(final JPanel container, final Font font) {
		JLabel sourceDesc = new JLabel("Select the source PDF file:");
		sourceDesc.setBorder(new EmptyBorder(new Insets(0, 20, 0, 20)));
		sourceDesc.setFont(font);
		container.add(sourceDesc);
		
		final FlowLayout fl = new FlowLayout(20);
		final JPanel sourcePanel = new JPanel(fl);
		JLabel pathLabel = new JLabel("Path:");
		pathLabel.setFont(font);
		sourcePanel.add(pathLabel);
		sourcePathField.setColumns(30);
		sourcePanel.add(sourcePathField);
		final JButton selectSourceButton = new JButton("Browse");
		selectSourceButton.setFont(font);
		selectSourceButton.addActionListener(Listeners.openPdfFileSelector(sourcePathField));
		sourcePanel.add(selectSourceButton);
		container.add(sourcePanel);
		
		JPanel passwordFlow = new JPanel(fl);
		JLabel pwdLabel = new JLabel("Password for PDF:");
		pwdLabel.setFont(font);
		passwordFlow.add(pwdLabel);
		passwordField.setToolTipText("Enter password here if the PDF is protected. Leave it empty if it has no password!");
		passwordField.setColumns(10);
		passwordFlow.add(passwordField);
		container.add(passwordFlow);
		
		JLabel destDesc = new JLabel("Select the destination folder for the extracted PDF:");
		destDesc.setBorder(new EmptyBorder(new Insets(0, 20, 0, 20)));
		destDesc.setFont(font);
		container.add(destDesc);
		
		final JPanel destPanel = new JPanel(fl);
		JLabel pathLabel2 = new JLabel("Path:");
		pathLabel2.setFont(font);
		destPanel.add(pathLabel2);
		destPathField.setColumns(30);
		destPanel.add(destPathField);
		final JButton selectDestButton = new JButton("Browse");
		selectDestButton.setFont(font);
		selectDestButton.addActionListener(Listeners.openFolderSelector(destPathField));
		destPanel.add(selectDestButton);
		container.add(destPanel);
		
		JPanel nameFlow = new JPanel(fl); //name
		JLabel nameLabel = new JLabel("Name of the result:");
		nameLabel.setFont(font);
		nameFlow.add(nameLabel);
		nameField.setToolTipText("You don't have to write '.pdf' to the end of the name, but you can.");
		nameField.setColumns(20);
		nameField.setFont(font);
		nameFlow.add(nameField);
		container.add(nameFlow);
	}
	
	/**
	 * Creates the fields where the user can specify if they want password protection for the new PDF file.
	 * @param container Components will be added to this.
	 * @param font Font to use.
	 */
	private void addPdfEncryptInputs(final JPanel container, final Font font) {
		//listener at the end so it can see the other panel
		usePasswordCheckbox.setFont(font);
		container.add(usePasswordCheckbox);
		
		final FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		fl.setHgap(20);
		final JPanel passwordInput = new JPanel(fl);
		JLabel pLabel = new JLabel("Enter password:");
		pLabel.setFont(font);
		passwordInput.add(pLabel);
		resultPasswordField.setColumns(20);
		passwordInput.add(resultPasswordField);
		passwordInput.setVisible(false); //checkbox unchecked at start, so this is not visible
		container.add(passwordInput);
		
		final JPanel passwordConfirmInput = new JPanel(fl);
		JLabel pcLabel = new JLabel("Confirm password:");
		pcLabel.setFont(font);
		passwordConfirmInput.add(pcLabel);
		resultPasswordConfirmField.setColumns(20);
		passwordConfirmInput.add(resultPasswordConfirmField);
		passwordConfirmInput.setVisible(false); //checkbox unchecked at start, so this is not visible
		container.add(passwordConfirmInput);
		
		usePasswordCheckbox.addItemListener(e -> {
			passwordInput.setVisible(e.getStateChange()==ItemEvent.SELECTED ? true : false);
			passwordConfirmInput.setVisible(e.getStateChange()==ItemEvent.SELECTED ? true : false);
			container.revalidate();
			container.repaint();
			PdfUtilsMain.getFrame().pack();
		});
		
		JSeparator sep = new JSeparator();
		container.add(sep);
	}
	
	//card layout IDs
	private static final String PAGE_RANGE = "pr", INDIVIDUAL_PAGES = "ip";
	
	/**
	 * Add tools to select pages to be extracted into the new pdf.
	 * @param container Will be added to this.
	 * @param font Texts will have this font.
	 */
	private void addPageSelectingTools(final JPanel container, final Font font) {
		JLabel pagesDesc = new JLabel("Choose the pages to extract:");
		pagesDesc.setBorder(new EmptyBorder(new Insets(0, 20, 0, 20)));
		pagesDesc.setFont(font);
		container.add(pagesDesc);
		
		final CardLayout cardLayout = new CardLayout();
		final JPanel pageSelectCards = new JPanel(cardLayout);
		
		FlowLayout fl = new FlowLayout(20);
		JPanel radioButtons = new JPanel(fl);
		final JRadioButton radioButtonPageRange = new JRadioButton("Range of pages");
		radioButtonPageRange.setSelected(true);
		radioButtonPageRange.setFont(font);
		radioButtonPageRange.addActionListener(e -> cardLayout.show(pageSelectCards, PAGE_RANGE));
		radioButtons.add(radioButtonPageRange);
		final JRadioButton radioButtonIndividualPages = new JRadioButton("Individual pages");
		radioButtonIndividualPages.addActionListener(e -> cardLayout.show(pageSelectCards, INDIVIDUAL_PAGES));
		radioButtonIndividualPages.setFont(font);
		radioButtons.add(radioButtonIndividualPages);
		ButtonGroup radioGroup = new ButtonGroup(); //group them
		radioGroup.add(radioButtonIndividualPages);
		radioGroup.add(radioButtonPageRange);
		container.add(radioButtons);
		
		//fill the card layout
		pageSelectCards.add(createPageRangePanel(font), PAGE_RANGE);
		pageSelectCards.add(createIndividualRangePanel(font), INDIVIDUAL_PAGES);
		container.add(pageSelectCards);
		cardLayout.show(pageSelectCards, PAGE_RANGE);
	}

	/**
	 * Create the panel that allows the user to select pages with a range.
	 * @param Font Applied to the texts.
	 * @return The panel.
	 */
	private JPanel createPageRangePanel(final Font font) {
		JPanel pageRangePanel = new JPanel(new GridLayout(0,1));
		
		JPanel pageRangeFlow = new JPanel(new FlowLayout(20));
		
		JLabel fromLabel = new JLabel("From this page:");
		fromLabel.setFont(font);
		fromLabel.setToolTipText("Extraction will start at this page. Must be a valid page number for the selected PDF.");
		pageRangeFlow.add(fromLabel);
		JTextField fromField = new JTextField();
		fromField.setColumns(2);
		pageRangeFlow.add(fromField);
		JLabel toLabel = new JLabel("To this page:");
		toLabel.setFont(font);
		toLabel.setToolTipText("Pages will be extracted up to this page (inclusive). Must be a valid page number for the selected PDF.");
		pageRangeFlow.add(toLabel);
		JTextField toField = new JTextField();
		toField.setColumns(2);
		pageRangeFlow.add(toField);
		pageRangePanel.add(pageRangeFlow);
		
		JButton executeOperationButton = new JButton("Extract pages");
		executeOperationButton.setFont(font);
		executeOperationButton.addActionListener(e -> {
			//the only thing that is not handled in the method below is comparing result passwords
			String resultPassword = "";
			if(usePasswordCheckbox.isSelected()) {
				if(!Arrays.equals(resultPasswordField.getPassword(), resultPasswordConfirmField.getPassword())) {
					JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "Password and confirmation do not match!", "Password error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				//using password for new PDf and password + confirmation matches
				resultPassword = new String(resultPasswordField.getPassword());
			}
			ModeExtractToPdf.attemptPdfExtraction(fromField.getText(),
					   toField.getText(),
					   nameField.getText(),
					   sourcePathField.getText(),
					   destPathField.getText(),
					   new String(passwordField.getPassword()),
					   resultPassword);
		});
		
		final JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
		wrapper.add(executeOperationButton);
		JButton previewButton = new JButton("Preview pages");
		previewButton.setFont(font);
		previewButton.addActionListener(e -> { //allows the user to preview the page range. no modification allowed inside the dialog
			try {
				int pageFrom = Integer.parseInt(fromField.getText());
				int pageTo = Integer.parseInt(toField.getText());
				if(pageTo < pageFrom) throw new NumberFormatException();
				List<Integer> pageNumbers = new ArrayList<>();
				for(int i=pageFrom; i<=pageTo; i++) pageNumbers.add(i);
				
				PageViewerDialog dialog = new PageViewerDialog(pageNumbers, sourcePathField.getText(), false);
				if(passwordField.getPassword().length>0) dialog.setPassword(new String(passwordField.getPassword()));
				dialog.startFilling();
				dialog.showPages();
			} catch(Exception exc) { //could not even build page numbers from user input
				String fromInput = fromField.getText().isEmpty() ? "[EMPTY]" : fromField.getText();
				String toInput = toField.getText().isEmpty() ? "[EMPTY]" : toField.getText();
				JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), fromInput+" and "+toInput+
						" is not a valid range of pages!","Invalid pages", JOptionPane.ERROR_MESSAGE);
			}
		}); 
		wrapper.add(previewButton);
		pageRangePanel.add(wrapper);
		return pageRangePanel;
	}
	
	/**
	 * Create the panel that allows the user to select individual pages. This happens with a dialog that displays the pages, or 
	 * a comma separated string with page numbers can be entered.
	 * @param Font Applied to the texts.
	 * @return The panel.
	 */
	private JPanel createIndividualRangePanel(final Font font) {
		JPanel individualPagesPanel = new JPanel(new GridLayout(0,1));
		individualPagesPanel.setFont(font);
		
		JPanel pageSpecFlow = new JPanel(new FlowLayout(20));
		JLabel l = new JLabel("Pages to be extracted:");
		l.setFont(font);
		pageSpecFlow.add(l);
		final JTextField csvPagesField = new JTextField();
		csvPagesField.setToolTipText("Separate page numbers with a comma, or use the selector tool!");
		csvPagesField.setColumns(20);
		pageSpecFlow.add(csvPagesField);
		individualPagesPanel.add(pageSpecFlow);
		
		JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton executeButton = new JButton("Extract");
		executeButton.setFont(font);
		executeButton.addActionListener(e -> {
			//the only thing that is not handled in the method below is comparing result passwords
			String resultPassword = "";
			if(usePasswordCheckbox.isSelected()) {
				if(!Arrays.equals(resultPasswordField.getPassword(), resultPasswordConfirmField.getPassword())) {
					JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "Password and confirmation do not match!", "Password error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				//using password for new PDf and password + confirmation matches
				resultPassword = new String(resultPasswordField.getPassword());
			}
			ModeExtractToPdf.attemptPdfExtraction(csvPagesField.getText(),
					   nameField.getText(),
					   sourcePathField.getText(),
					   destPathField.getText(),
					   new String(passwordField.getPassword()),
					   resultPassword);
		});
		buttonFlow.add(executeButton);
		JButton selectorButton = new JButton("Select pages"); //select pages tool
		selectorButton.setFont(font);
		selectorButton.addActionListener(e -> {
			PageViewerDialog dialog = new PageViewerDialog(sourcePathField.getText(), true);
			if(passwordField.getPassword().length>0) dialog.setPassword(new String(passwordField.getPassword()));
			dialog.startFilling();
			List<Integer> selectedPages = dialog.showPagesForResult();
			//make csv string from result
			StringBuilder b = new StringBuilder();
			for(int i=0; i<selectedPages.size();i++) {
				b.append(selectedPages.get(i));
				if(i<selectedPages.size()-1) b.append(",");
			}
			csvPagesField.setText(b.toString());
		});
		buttonFlow.add(selectorButton);
		individualPagesPanel.add(buttonFlow);
		
		return individualPagesPanel;
	}
}
