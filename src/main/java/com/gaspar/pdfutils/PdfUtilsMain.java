package com.gaspar.pdfutils;

import java.io.IOException;
import java.util.Scanner;

import com.gaspar.pdfutils.modes.Mode;
import com.gaspar.pdfutils.modes.ModeExtractToImages;

public class PdfUtilsMain {
	
	/**
	 * Entry point.
	 * @param args First parameter must be the mode (from {@link PdfUtilConstants}), then the arguments for this mode.
	 * @throws IOException Some file access error.
	 */
	public static void main(String[] args) throws IOException {
		Mode mode = parseMode(args);
		String source, dest;
		try(Scanner scanner = new Scanner(System.in)) {
			System.out.println("Enter the source PDF file path: ");
			source = scanner.nextLine();
			System.out.println("Enter the path where the result should be placed: ");
			dest = scanner.nextLine();
		}
		System.out.println("Depending on the PDF size, this may take some time. Please wait...");
		mode.execute(source, dest);
		System.out.println("Operation completed.");
	}
	
	/**
	 * Determines and creates the {@link Mode} from the command line arguments.
	 * @param args Command line arguments.
	 * @return The parsed mode.
	 */
	private static Mode parseMode(String[] args) {
		Mode mode = null;
		if(args.length == 0) throw new IllegalArgumentException("No mode specified!");
		String modeNameInput = args[0];
		switch (modeNameInput) {
		case Mode.MODE_EXTRACT_TO_IMAGES:
			try {
				int fromPage = Integer.parseInt(args[1]);
				int toPage = Integer.parseInt(args[2]);
				if(args.length > 3) { //custom image prefix
					mode = new ModeExtractToImages(fromPage, toPage, args[3]);
				} else {
					mode = new ModeExtractToImages(fromPage, toPage);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid extract to image mode arguments! They must be: pageFrom, pageTo, imagePrefix.");
			}
			break;
		default:
			throw new IllegalArgumentException("Unrecognized mode: " + modeNameInput);
		}
		return mode;
	}
}
