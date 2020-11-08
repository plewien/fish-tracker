package com.tracker.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Swing panel which redirects System.out and System.err streams to a text area of a GUI
 */
public class SourcePanel extends JPanel {
	private static final long serialVersionUID = 9193099875156528950L;
	private JTextArea consoleTextArea;
	private int panelWidth = 320; //pixels
	private int labelHeight = 30; //pixels
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame frame = new JFrame();
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.add(new SourcePanel());
					frame.pack();
					frame.setVisible(true);
					for (int i = 0; i<100; i++) {
						System.out.println("Hello! " + i);
					}
					
				} catch (Exception e) {
					System.err.println("Error: Unable to load panel");
					e.printStackTrace();
				}
			}
		});
	}
	
	public SourcePanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel lblSource = new JLabel("Source");
		lblSource.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblSource.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblSource.setPreferredSize(new Dimension(panelWidth, labelHeight));
		add(lblSource);
		
		consoleTextArea = new JTextArea();
		consoleTextArea.setEditable(false);
		consoleTextArea.setLineWrap(true);
		
		JScrollPane scrollPane = new JScrollPane(consoleTextArea, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(panelWidth, 400));
		add(scrollPane);
		
		//Comment this out to keep stream in the console
		redirectSystemStreams();
	}
	
	/**
	 * Moves the System.out and System.err streams into the location defined by updateTextArea().
	 */
	private void redirectSystemStreams() {
    	OutputStream out = new OutputStream() {
		    @Override
		    public void write(int b) throws IOException {
		      updateTextArea(String.valueOf((char) b));
		    }
		 
		    @Override
		    public void write(byte[] b, int off, int len) throws IOException {
		      updateTextArea(new String(b, off, len));
		    }
		 
		    @Override
		    public void write(byte[] b) throws IOException {
		      write(b, 0, b.length);
		    }
    	};
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
	
	/**
	 * Writes text to the consoleTextArea
	 * @param text
	 */
    public void updateTextArea(final String text) {
    	SwingUtilities.invokeLater(new Runnable() {
    	    public void run() {
    	    	consoleTextArea.append(text);
    	    }
    	});
    }
}
