package com.gaspar.pdfutils.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.gaspar.pdfutils.PdfUtilsMain;
import com.gaspar.pdfutils.modes.Mode;
import com.gaspar.pdfutils.modes.ModeExtractToImages;
import com.gaspar.pdfutils.modes.ModeExtractToPdf;
import com.gaspar.pdfutils.modes.ModeImagesToPdf;

/**
 * This panel is the "main menu" of the GUI application, it allows the user to select a mode 
 * and shows a description for this mode.
 * @author Gáspár Tamás
 */
public class ModeSelectPanel extends JPanel {

	/**
	 * The user selected this mode. In the beginning, this is null. Using the {@link Mode#getDescription()}} and the 
	 * {@link Mode#getModePanel()} methods, this object will be used to update the GUI.
	 */
	private Mode selectedMode;
	
	/**
	 * This label displays the description of the selected mode. In the beginnig, it displays nothing.
	 */
	private final JTextArea descDisplay = new JTextArea("Choose an option to see a description!",2,30);
	
	public ModeSelectPanel() {
		JPanel container = new JPanel();
		container.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		add(container);
		
		GridLayout gl = new GridLayout(0, 1);
		gl.setVgap(20);
		container.setLayout(gl);
		container.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		final JPanel wrapper = new JPanel(new FlowLayout(20));
		wrapper.add(new JLabel("Choose:"));
		wrapper.add(buildComboBox());
		container.add(wrapper);
		
		descDisplay.setFont(new Font("SansSerif", Font.PLAIN,15));
		descDisplay.setMargin(new Insets(20, 20, 20, 20));
		descDisplay.setLineWrap(true);
		descDisplay.setWrapStyleWord(true);
		descDisplay.setOpaque(false);
		descDisplay.setEditable(false);
		container.add(descDisplay);
		
		final JButton selectButton = new JButton("Select");
		selectButton.setMargin(new Insets(10, 10, 10, 10));
		selectButton.setFont(new Font("SansSerif",Font.PLAIN, 15));
		selectButton.addActionListener(e -> { //selected mode panel will replace this panel as center in the root
			if(selectedMode != null) {
				final RootPanel root = RootPanel.getInstance();
				root.remove(this);
				final JPanel modePanel = selectedMode.getModePanel();
				root.add(modePanel, BorderLayout.CENTER);
				//update exit button to be back button
				root.getBackButton().setText("Back");
				root.getBackButton().removeActionListener(root.getBackButton().getActionListeners()[0]);
				root.getBackButton().addActionListener(e1 -> { //on click it will not exit anymore, but go back
					root.remove(modePanel);
					root.add(new ModeSelectPanel(), BorderLayout.CENTER);
					root.getBackButton().setText("Exit");
					root.getBackButton().removeActionListener(root.getBackButton().getActionListeners()[0]);
					root.getBackButton().addActionListener(Listeners.getExitListener()); //make back button an exit button again
					PdfUtilsMain.getFrame().pack();
				});
				PdfUtilsMain.getFrame().pack();
			} else {
				JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "Choose an operation first!");
			}
		});
		selectButton.setMargin(new Insets(20, 20, 20, 20));
		final FlowLayout bLayout = new FlowLayout(FlowLayout.CENTER);
		final JPanel bPanel = new JPanel(bLayout);
		bPanel.add(selectButton);
		container.add(bPanel);
	}
	
	/**
	 * Creates the dropdown menu that the user can use to select a mode.
	 * @return The dropdown menu.
	 */
	@SuppressWarnings("unchecked")
	private JComboBox<String> buildComboBox() {
		JComboBox<String> dropdown = new JComboBox<>();
		dropdown.addItem(Mode.MODE_EXTRACT_TO_IMAGES);
		dropdown.addItem(Mode.MODE_EXTRACT_TO_PDF);
		dropdown.addItem(Mode.MODE_IMAGES_TO_PDF);
		dropdown.setSelectedIndex(-1);
		
		dropdown.addActionListener(e -> { //on select, update mode
			String selected = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
			switch (selected) {
			case Mode.MODE_EXTRACT_TO_IMAGES:
				selectedMode = new ModeExtractToImages();
				break;
			case Mode.MODE_EXTRACT_TO_PDF:
				selectedMode = new ModeExtractToPdf();
				break;
			case Mode.MODE_IMAGES_TO_PDF:
				selectedMode = new ModeImagesToPdf();
				break;
			default: //not possible
				break;
			}
			descDisplay.setText(selectedMode.getDescription());
			PdfUtilsMain.getFrame().pack();
		});
		return dropdown;
	}
}
