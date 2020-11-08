package com.tracker.view;

import javax.swing.JFrame;
import javax.swing.BoxLayout;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import javax.swing.border.LineBorder;
import javax.swing.undo.UndoManager;

import org.opencv.core.Core;

import com.tracker.controller.TrackerEventHandler;
import com.tracker.model.TrackerModel;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JToolBar;
import javax.swing.JLayeredPane;

public class TrackerGUI2 extends JFrame {

	private static final long serialVersionUID = -1243292443717623794L;
	public VideoContainer videoContainer = null;
	public JMenuItem mntmUndo, mntmRedo, mntmCalibrateFish, mntmCalibrateScale;
	public JMenuItem mntmSave, mntmSaveAs;
	
	public static void main(String[] args) {
		// load library;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LinkedList<File> files = new LinkedList<File>();
					files.add(new File("test1.mp4"));
					files.add(new File("test1.mp4"));
					TrackerModel model = new TrackerModel(files);
					new TrackerGUI2(model);
				} catch (Exception e) {
					System.err.println("Error: Unable to load panel");
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Instigate the application.
	 */
	public TrackerGUI2(TrackerModel model) {
		super();
		initializeGUI(model);
		new TrackerEventHandler(model, this);
		pack();
		setVisible(true);
	}

	/**
	 * Initialises the contents of the tracker GUI frame.
	 * @param model the fish tracker model
	 */
	private void initializeGUI(TrackerModel model) {
		setTitle("Fish Tracker");
		//setBounds(0, 0, 1920, 1080);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		//screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		/* Drop-down bar */
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(Color.WHITE);
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNew = new JMenuItem("New");
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		mntmSaveAs = new JMenuItem("Save As...");
		mnFile.add(mntmSaveAs);
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		mntmUndo = new JMenuItem("Undo");
		mnEdit.add(mntmUndo);
		
		mntmRedo = new JMenuItem("Redo");
		mnEdit.add(mntmRedo);
		
		JSeparator separator_4 = new JSeparator();
		mnEdit.add(separator_4);
		
		mntmCalibrateFish = new JMenuItem("Calibrate Fish");
		mnEdit.add(mntmCalibrateFish);
		
		mntmCalibrateScale = new JMenuItem("Calibrate Scale");
		mnEdit.add(mntmCalibrateScale);
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		
		videoContainer = new VideoContainer(model);
		getContentPane().add(videoContainer);
		
		/* Data Panel */		
		JLayeredPane dataPanel = new JLayeredPane();
		dataPanel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		getContentPane().add(dataPanel);
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		
		JToolBar sourceToolBar = new JToolBar("Source");
		dataPanel.add(sourceToolBar);
		
		SourcePanel sourcePanel = new SourcePanel();
		sourceToolBar.add(sourcePanel);
		
		JSeparator separator_2 = new JSeparator();
		dataPanel.add(separator_2);
		
		JToolBar videoControlToolBar = new JToolBar("Video Controls");
		dataPanel.add(videoControlToolBar);
		
		VideoControlPanel videoControlPanel = new VideoControlPanel(model);
		videoControlToolBar.add(videoControlPanel);
		
		JSeparator separator_3 = new JSeparator();
		dataPanel.add(separator_3);
		
		JToolBar statsToolBar = new JToolBar("Statistics");
		dataPanel.add(statsToolBar);
		
		StatisticsPanel statsPanel = new StatisticsPanel(model);
		statsToolBar.add(statsPanel);
		
		System.out.println("System initialised.");
	}
	
	/**
	* This method is called after each undoable operation in order to refresh the presentation state of the undo/redo
	*  GUI
	*  @param undoManager the holder of all undoable operations
	*/
	public void refreshUndoRedo(UndoManager undoManager) {
	
	   // refresh undo
	   mntmUndo.setText(undoManager.getUndoPresentationName());
	   mntmUndo.setEnabled(undoManager.canUndo());
	
	   // refresh redo
	   mntmRedo.setText(undoManager.getRedoPresentationName());
	   mntmRedo.setEnabled(undoManager.canRedo());
	}
}


