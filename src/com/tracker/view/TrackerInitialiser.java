package com.tracker.view;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JFileChooser;

import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.core.Core;

import com.tracker.model.TrackerModel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.awt.event.ActionListener;

public class TrackerInitialiser extends JFrame {
	private static final long serialVersionUID = 339984153930286683L;
	private final JPanel welcomePanel = new JPanel();
	private final JPanel selectorPanel = new JPanel();
	private final JPanel videoListPanel = new JPanel();
	private final JButton btnSelectVideos = new JButton("Select Videos");
	private final JButton btnRemoveVideos = new JButton("Remove Videos");
	private final JPanel goPanel = new JPanel();
	private final JButton btnGo  = new JButton("Go!");
	
	
	private Queue<File> videoFiles = new LinkedList<File>();
	private File latestDirectory = new File("C:\\Users\\plewi\\Documents\\Programming\\Java\\FishTracker");

	/**
	 * Launches the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TrackerInitialiser window = new TrackerInitialiser();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Creates the application.
	 */
	public TrackerInitialiser() {
		super();
		initialize();
		setLocationRelativeTo(null);
	}

	/**
	 * Initialises the contents of the frame.
	 */
	private void initialize() {
		setTitle("Fish Tracker");
		setBounds(100, 100, 420, 343);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		getContentPane().add(welcomePanel);
		welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
		
		welcomePanel.add(Box.createVerticalStrut(40));
		
		JLabel lblWelcomeTo = new JLabel("Welcome to....");
		lblWelcomeTo.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblWelcomeTo.setFont(new Font("Dialog", Font.BOLD, 14));
		lblWelcomeTo.setHorizontalAlignment(SwingConstants.CENTER);
		welcomePanel.add(lblWelcomeTo);
		
		JLabel lblFishTracker = new JLabel("My Fish Tracker");
		lblFishTracker.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblFishTracker.setFont(new Font("Edwardian Script ITC", Font.BOLD, 56));
		lblFishTracker.setHorizontalAlignment(SwingConstants.CENTER);
		welcomePanel.add(lblFishTracker);
		
		welcomePanel.add(Box.createRigidArea(new Dimension(400, 40)));
		
		/* File Selection */
		getContentPane().add(selectorPanel);
		selectorPanel.setLayout(new BoxLayout(selectorPanel, BoxLayout.X_AXIS));
		
		selectorPanel.add(btnSelectVideos);
		btnSelectVideos.setPreferredSize(new Dimension(144, 30));
		btnSelectVideos.setFont(new Font("Dialog", Font.BOLD, 14));
		btnSelectVideos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame openFrame = new JFrame();
				JFileChooser fileChooser = new JFileChooser(latestDirectory);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("*.mp4, *.avi", "mp4", "avi"); //Are there more file types?
				fileChooser.setDialogTitle("Specify Videos to Open");
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				fileChooser.addChoosableFileFilter(filter);
				fileChooser.setFileFilter(filter);
				
				int userSelection = fileChooser.showOpenDialog(openFrame);
				latestDirectory = fileChooser.getCurrentDirectory();
		
				if (userSelection == JFileChooser.APPROVE_OPTION) {
					// TODO Sanity check that all videos added are playable
				    Collections.addAll(videoFiles, fileChooser.getSelectedFiles());
				    syncVideos();
				}
			}
		});
		
		selectorPanel.add(Box.createHorizontalStrut(10));
		
		selectorPanel.add(btnRemoveVideos);
		btnRemoveVideos.setPreferredSize(new Dimension(144, 30));
		btnRemoveVideos.setFont(new Font("Dialog", Font.BOLD, 14));
		btnRemoveVideos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Add in functionality to remove only some videos if accidentally selected
				videoFiles.clear();
				syncVideos();
				videoListPanel.add(new JLabel("All video files removed.", SwingConstants.CENTER));
			}
		});
		
		
		/* Video file displaying/removing */
		getContentPane().add(videoListPanel);
		videoListPanel.setPreferredSize(new Dimension(320, 80));
		syncVideos();
		
		/* Go panel ready for motion tracking */
		getContentPane().add(goPanel);
		
		goPanel.add(btnGo);
		btnGo.setPreferredSize(new Dimension(60, 40));
		btnGo.setFont(new Font("Dialog", Font.BOLD, 14));
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// load library
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				
				// close initialiser
				setVisible(false);
				dispose();
				
				// run program
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							// TODO Actually load the videos
							TrackerModel model = new TrackerModel(videoFiles);
							new TrackerGUI2(model);
						} catch (Exception e) {
							System.err.println("Error: Unable to load panel");
							e.printStackTrace();
						}
					}
				});
				
			}
		});
		
		// Pack contents to fit window
		pack();
	}
	
	/**
	 * Synchronises the videos that have been selected to the initialiser panel.
	 */
	private void syncVideos() {
		videoListPanel.removeAll();
		for (File video: videoFiles) {
			videoListPanel.add(new JLabel(video.getName(), SwingConstants.CENTER));
		}
		videoListPanel.revalidate();
		repaint();
		
		// Enable go button if there is a video.
		if (videoFiles.size() > 0) {
			btnGo.setEnabled(true);
			btnRemoveVideos.setEnabled(true);
		} else {
			btnGo.setEnabled(false);
			btnRemoveVideos.setEnabled(false);
		}
	}
}
