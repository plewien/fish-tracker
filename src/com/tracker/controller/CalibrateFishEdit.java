package com.tracker.controller;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.opencv.core.Point;

import com.tracker.model.Fish;

public class CalibrateFishEdit extends AbstractUndoableEdit {
	private static final long serialVersionUID = 4414858244617883124L;
	private Fish fish;
	private Point oldPoint, newPoint;
	
	public CalibrateFishEdit(Fish fish, Point pt) {
		this.fish = fish;
		oldPoint = fish.getCenter();
		this.newPoint = pt;
		redo();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		System.out.println("Moving " + fish.getName() + " back to " + oldPoint);
		fish.moveCenter(oldPoint);
	}

	@Override
	public boolean canUndo() {
		return fish.getCenter() == newPoint;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		System.out.println("Moving " + fish.getName() + " to " + newPoint);
		fish.moveCenter(newPoint);
	}

	@Override
	public boolean canRedo() {
		return fish.getCenter() == oldPoint;
	}
	
	@Override
	public String getPresentationName() {
		return "Fish Position Calibration";
	}
}
