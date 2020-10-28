package com.gaspar.pdfutils.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.gaspar.pdfutils.PdfUtilsMain;
import com.gaspar.pdfutils.modes.ModeImagesToPdf;

/**
 * This panel displays a UI for the user to select images to combine into a single PDF file
 * @author Gáspár Tamás
 * @see ModeImagesToPdf
 */
public class ModeImagesToPdfPanel extends JPanel {
	/**
	 * Contains the path of the folder where the result pdf will be placed.
	 */
	private final JTextField destPathField = new JTextField();
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
	 * These are the image files selected. These are guaranteed to be one of the supported formats, as the file selector only allows those to be selected.
	 */
	private final List<File> images = new ArrayList<>();
	
	/**
	 * Builds a panel where all input fields are empty.
	 */
	public ModeImagesToPdfPanel() {
		final JPanel container = new JPanel();
		container.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		final BoxLayout gl = new BoxLayout(container, BoxLayout.Y_AXIS);
		container.setAlignmentX(LEFT_ALIGNMENT);
		container.setLayout(gl);
		
		final Font font = new Font("SansSerif", Font.PLAIN, 15);
		
		addDestinationInputFields(container, font);
		container.add(new JSeparator());
		addPdfEncryptInputs(container, font);
		container.add(new JSeparator());
		addImageSelectionTools(container, font);
		add(container);
	}
	
	/**
	 * Add tools to select destination path and name.
	 * @param container Will be added to this.
	 * @param font Texts will have this font.
	 */
	private void addDestinationInputFields(final JPanel container, final Font font) {
		JLabel destDesc = new JLabel("Select the destination folder for the combined PDF:");
		destDesc.setBorder(new EmptyBorder(new Insets(0, 20, 0, 20)));
		destDesc.setFont(font);
		container.add(destDesc);
		
		final FlowLayout fl = new FlowLayout(20);
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
	}
	
	/**
	 * Adds tools to select images and start the operation.
	 * @param container Components will be added to this.
	 * @param font Font to use.
	 */
	public void addImageSelectionTools(final JPanel container, final Font font) {
		JLabel selectLabel = new JLabel("Select the images to be combined:");
		selectLabel.setFont(font);
		container.add(selectLabel);
		
		final FlowLayout fl = new FlowLayout(20);
		fl.setAlignment(FlowLayout.CENTER);
		
		JPanel selectedFlow = new JPanel(fl);
		JTextArea selectedField = new JTextArea("No images selected...");
		selectedField.setFont(font);
		selectedField.setEditable(false); //must use the filechooser
		selectedField.setToolTipText("Use the image selector tool to specify images!");
		selectedField.setColumns(30);
		selectedField.setRows(2);
		selectedFlow.add(selectedField);
		final JButton clearButton = new JButton("Clear");
		clearButton.setFont(font);
		clearButton.addActionListener(e -> {
			images.clear();
			selectedField.setText("No images selected...");
			selectedField.revalidate();
			selectedField.repaint();
		});
		selectedFlow.add(clearButton);
		container.add(selectedFlow);
		
		JPanel buttonFlow = new JPanel(fl);
		final JButton executeButton = new JButton("Combine");
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
			ModeImagesToPdf.attemptImageCombination(destPathField.getText(), nameField.getText(), images, resultPassword);
		});
		buttonFlow.add(executeButton);
		
		final JButton selectButton = new JButton("Select images");
		selectButton.setFont(font);
		selectButton.addActionListener(Listeners.openImageSelector(images, selectedField));
		buttonFlow.add(selectButton);
		container.add(buttonFlow);
	}
}
