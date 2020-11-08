package com.tracker.controller;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.opencv.core.Point;

import com.tracker.model.Fish;
import com.tracker.model.TrackerModel;
import com.tracker.view.TrackerGUI2;
import com.tracker.view.VideoPanel;

public class TrackerEventHandler extends Observable {
	private TrackerModel model;
	private TrackerGUI2 gui;
	private VideoPanel panel;
	private boolean alreadyCalibrating = false;
	private Fish fishToken = null;
	
	private UndoManager undoManager;         // history list
	private UndoableEditSupport undoSupport; // event support

	public TrackerEventHandler(TrackerModel model, TrackerGUI2 gui) {
		this.model = model;
		this.gui = gui;
		this.panel = gui.videoContainer.videoPanel;
		
		undoManager = new UndoManager();
		undoSupport = new UndoableEditSupport();
		undoSupport.addUndoableEditListener(new UndoAdapter());
		gui.refreshUndoRedo(undoManager);
		gui.mntmUndo.addActionListener(new UndoAction());
		gui.mntmRedo.addActionListener(new RedoAction());
		gui.mntmSave.addActionListener(new SaveAction(SaveAction.SAVE));
		gui.mntmSaveAs.addActionListener(new SaveAction(SaveAction.SAVE_AS));
		gui.mntmCalibrateFish.addActionListener(new CalibrateFishAction());
		gui.mntmCalibrateScale.addActionListener(new CalibrateScaleAction());

	}
	
	/**
	 * The save action is performed when the user wants to save their accumulated data to a csv file. This data can 
	 * then be used for plotting the movement of the fish over time, in a program like excel.
	 */
	private class SaveAction extends AbstractAction {
		private static final long serialVersionUID = -3036238243546760519L;
		public static final int SAVE_AS = 1;
		public static final int SAVE = 0;
		public final int opt;
		public File fileToSave = null;
		
		/**
		 * Save action constructor, which sets the method through which saving occurs.
		 * @param opt Either SAVE or SAVE_AS.
		 */
		public SaveAction(int opt) {
			this.opt = opt;
			if (opt == SAVE) {
				gui.mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			}
		}
		
		/**
		 * Method that occurs when a user saves. If they choose 'save as' or haven't specified a file to save yet, then
		 * the user is first prompted to give a file location.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// Choose file to save as
			if (opt == SAVE_AS || fileToSave == null) { // || !fileToSave.isFile()) {
				boolean userReturn = chooseFileToSave();
				if (userReturn == false) {
					System.out.println("File not saved.");
					return;
				}
			}
			
			// Save as csv
			System.out.println("Saving as file: " + fileToSave.getAbsolutePath());
			try {
				model.saveDataAsCSV(fileToSave);
				System.out.println("Data Saved Successfully.");
			} catch (FileNotFoundException ex){
				System.out.println("Data not saved.");
			}
		}
		
		/**
		 * Prompts user for the file location to save to. This is done through a JFileChooser.
		 * @return True, if the user chooses a valid file location.
		 */
		public boolean chooseFileToSave() {
			JFrame saveFrame = new JFrame();
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Specify a file to save");
			fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			fileChooser.setSelectedFile(new File("fishdata.csv"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("CSV File", "csv"));
			
			int userSelection = fileChooser.showSaveDialog(saveFrame);
			 
			if (userSelection == JFileChooser.APPROVE_OPTION) {
			    fileToSave = fileChooser.getSelectedFile();
			    if (fileToSave == null) {
			    	return false;
			    } else {
				    return true;
			    }
			} else {
				return false;
			}
		}
	}
	
    /**
    * Calibration of Fish action. Under this action, the user can drag the fish marker to its correct location without
    * adding to the fish's distance travelled. 
    * 
    * If this action occurs again while undergoing calibration, the calibration stops and the video resumes.
    */
    private class CalibrateFishAction extends AbstractAction {
    	private static final long serialVersionUID = -1737639684582380831L;
    	private MouseListener fishCalibrationMouseListener;
    	private MouseMotionListener fishCalibrationMotionListener;
    	private Point userClickLocation;
    	
    	/** 
    	 * Constructor of the fish action. This sets the calibrator accelerator key combination, and outlines the 
    	 * details for the interaction with the mouse through a mouse listener. This mouse listener only gets enabled 
    	 * when the action is performed.
    	 */
    	public CalibrateFishAction() {
    		gui.mntmCalibrateFish.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
    		fishCalibrationMouseListener = new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {}
				@Override 
				public void mousePressed(MouseEvent e) {
					userClickLocation = new Point(e.getX(), e.getY());
					fishToken = model.getNearbyFish(userClickLocation);
					if (fishToken != null) {
						panel.setCalibratingPoint(new Point(e.getX(), e.getY()));
						panel.setCalibratingFish(fishToken);
					}
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					if (fishToken != null) {
						UndoableEdit edit = new CalibrateFishEdit(fishToken, new Point(e.getX(), e.getY()));
				        undoSupport.postEdit(edit);
					}
					panel.setCalibratingPoint(null);
					panel.setCalibratingFish(null);
					fishToken = null;
					panel.repaint();
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					panel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Change to grabber hand if possible
				}
				@Override
				public void mouseExited(MouseEvent e) {
					panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			};
			
			fishCalibrationMotionListener = new MouseMotionAdapter() {
				@Override
	            public void mouseDragged(MouseEvent e) {
					if (fishToken != null) {
						panel.setCalibratingPoint(new Point(e.getX(), e.getY()));
		                panel.repaint();
					}
	            }
	        };
    	}
    	
		public void actionPerformed(ActionEvent evt) {
			if (alreadyCalibrating) {
				gui.videoContainer.videoPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				gui.videoContainer.resetCommand();
				model.resumeMovie();
				stopListeningToPanel();
				alreadyCalibrating = false;
			} else {
				gui.videoContainer.videoPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
				gui.videoContainer.setCommand("Calibrate Fish Position");
				model.pauseMovie();
				listenToPanel();
				alreadyCalibrating = true;
			}
        }
		
		private void listenToPanel() {
			panel.addMouseListener(fishCalibrationMouseListener);
			panel.addMouseMotionListener(fishCalibrationMotionListener);
		}
		
		private void stopListeningToPanel() {
			panel.removeMouseListener(fishCalibrationMouseListener);
			panel.removeMouseMotionListener(fishCalibrationMotionListener);
		}
     }
    
    /**
     * Calibration of scale action. This is performed to calibrate the relationship between pixels and distance. In the
     * fish videos, the small squares have a side length of 5mm while the large squares have a side length of 25mm.
     * 
     * To perform this calibration, the user is asked to click on four points, in order:
     * 1. The top-left corner of a given large square.
     * 2. The top-right corner of a given large square.
     * 3. The bottom-right corner of a given large square.
     * 4. The bottom-left corner of a given large square.
     * 
     * From this data, a calibration scale is obtained.
     */
     private class CalibrateScaleAction extends AbstractAction {
     	private static final long serialVersionUID = -1737639684582380831L;
     	final String[] CORNERS = {"top left", "top right", "bottom right", "bottom left"};
     	final String COMMAND = "Please click on the %s corner of a 25mm square.";
     	private MouseListener scaleCalibrationMouseListener;
     	private List<Point> calibrationLocations;
     	
     	public CalibrateScaleAction() {
     		gui.mntmCalibrateScale.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_MASK));
     		scaleCalibrationMouseListener = new MouseListener() {
    			@Override
    			public void mouseClicked(MouseEvent e) {}
    			@Override 
    			public void mousePressed(MouseEvent e) {
    				Point userClickLocation = new Point(e.getX(), e.getY());
    				calibrationLocations.add(userClickLocation);
    				int numClicks = calibrationLocations.size();
    				if (numClicks < CORNERS.length) {
    					gui.videoContainer.setCommand(String.format(COMMAND, CORNERS[numClicks]));
    				} else {
    					UndoableEdit edit = new CalibrateScaleEdit(calibrationLocations);
				        undoSupport.postEdit(edit);
    					gui.videoContainer.resetCommand();
    					panel.removeMouseListener(scaleCalibrationMouseListener);
    				}
    			}
    			@Override
    			public void mouseReleased(MouseEvent e) {}
    			@Override
    			public void mouseEntered(MouseEvent e) {
    				panel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    			}
    			@Override
    			public void mouseExited(MouseEvent e) {
    				panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    			}
    		};	
     	}
     	
 		public void actionPerformed(ActionEvent evt) {
 			calibrationLocations = new ArrayList<Point>();	// Reset location list
 			gui.videoContainer.setCommand(String.format(COMMAND, CORNERS[0]));
 			model.pauseMovie();
 			panel.addMouseListener(scaleCalibrationMouseListener);
         }
      }

     /**
     *  undo action
     */
     private class UndoAction extends AbstractAction {
		private static final long serialVersionUID = 4559171834834659949L;
		
		public UndoAction() {
			gui.mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent evt) {
            undoManager.undo();
       	 	gui.refreshUndoRedo(undoManager);
         }
     }

    /**
    * Inner class that defines the redo action.
    */
     private class RedoAction extends AbstractAction {
		private static final long serialVersionUID = 5724997940717539839L;
		
		public RedoAction() {
			gui.mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent evt) {
    	 	undoManager.redo();
        	gui.refreshUndoRedo(undoManager);
    	 }
     }

     /**
     * An undo/redo adapter. The adapter is notified when an undo edit occurs. The adaptor extract the edit from the 
     * event, adds it to the UndoManager, and refreshes the GUI.
     */
     private class UndoAdapter implements UndoableEditListener {
		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			UndoableEdit edit = e.getEdit();
        	undoManager.addEdit(edit);
        	gui.refreshUndoRedo(undoManager);
		}
     }
}
