package com.tracker.controller;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.opencv.core.Point;

import com.tracker.model.Fish;


public class CalibrateScaleEdit extends AbstractUndoableEdit {
	private static final long serialVersionUID = -6903336551709544139L;
	private double newRatio, oldRatio;
	
	public CalibrateScaleEdit(List<Point> pts) {
		newRatio = calculatePixelToMMRatio(pts);
		oldRatio = Fish.getPixelToMM();
		redo();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		System.out.printf("Resetting to previous ratio: %.2f pixels per mm\n", 1/oldRatio);
		Fish.setPixelToMM(oldRatio);
	}
	
	@Override
	public boolean canUndo() {
		return Fish.getPixelToMM() == newRatio;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		System.out.printf("Setting new ratio: %.2f pixels per mm\n", 1/newRatio);
		Fish.setPixelToMM(newRatio);
	}

	@Override
	public boolean canRedo() {
		return Fish.getPixelToMM() == oldRatio;
	}
	
	@Override
	public String getPresentationName() {
		return "Scale Calibration";
	}
	
	/**
	 * Method used to determine the ratio between pixels and millimetres in the video being analysed. This is done by 
	 * comparing the perimeter of a 25mm square, to the perimeter created by four clicks on the user's screen.
	 * @param pts A list of four points defining the four corners of a 25mm square.
	 * @return A ratio of pixels to mm.
	 */
	private double calculatePixelToMMRatio(List<Point> pts) {
		double sideLength = 25.0; 		// mm
		double perimeter = Math.sqrt(Fish.distSquared(pts.get(0), pts.get(pts.size()-1)));
		for (int i = 0; i < pts.size()-1; i++) {
			perimeter += Math.sqrt(Fish.distSquared(pts.get(i), pts.get(i+1)));
		}
		return (pts.size()*sideLength)/perimeter;
	}
}
