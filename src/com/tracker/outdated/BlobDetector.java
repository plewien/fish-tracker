package com.tracker.outdated;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.tracker.model.Fish;

public interface BlobDetector {
	Scalar colour = new Scalar(255,255,0);
	Scalar textColour = new Scalar(255,255,255);
	List<Fish> fishes = new ArrayList<Fish>(Fish.NUM_FISH);
	
	public default void detectBlobs(Mat frame, Mat binary) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(binary, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.drawContours(frame, contours, -1, colour);

		for (Fish fish: fishes) {
			fish.moveToBestCenter(contours);
		}
	}
}