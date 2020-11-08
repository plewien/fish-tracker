package com.tracker.model;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Queue;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.videoio.Videoio;

public class TrackerModel extends Observable {
	private MoviePlayer player;
	private List<Fish> fishes = new ArrayList<Fish>(Fish.NUM_FISH);
	
	public TrackerModel(Queue<File> videoFiles) {
		player = new MoviePlayer(videoFiles);
		initialiseFish(new Point(20,20));
		// TODO calibrate first before allowing user to play
		player.start(); pauseMovie();
	}
	
	public TrackerModel(File videoFile) {
		this(new LinkedList<File>(Arrays.asList(videoFile)));
	}
	
	/**
	 * Default constructor. No videos are loaded into the MoviePlayer, so it plays the default test1.mp4.
	 */
	public TrackerModel() {
		this(new LinkedList<File>());
	}
	
    public synchronized void resumeMovie() {
    	player.unpause();
    }
    
    public synchronized void pauseMovie() {
    	player.pause();
    }
    
    public boolean isPaused() { 
    	return player.paused; 
    }
    
    public List<Fish> getFishes() {
    	return fishes;
    }
    
    public Size getFrameSize() {
    	return player.currentVideo().getSize();
    }
    
	public int getTotalFrames() {
		int totalFrameCount = 0;
		for (MotionVideoCapture video: player.videos) {
			totalFrameCount += (int) video.get(Videoio.CAP_PROP_FRAME_COUNT);
		}
		return totalFrameCount;
	}
	
	public Mat getCurrentFrame() {
		return player.currentVideo().getFrame();
	}
	
	public int getCurrentFrameIndex() {
		return player.frameCounter;
	}
	
	public double getFps() {
		return player.currentVideo().get(Videoio.CV_CAP_PROP_FPS);
	}
	
	/** 
	 * Initialises fish location based on user's click location.
	 */
	private void initialiseFish(Point pos) {
		for (int i = 0; i < Fish.NUM_FISH; i++) {
			fishes.add(new Fish(pos, i));
		}
	}
	
	/**
	 * Calibrates fish location based on user's click location.
	 */
	public void updateFishLocation(int index, Point pos) {
		fishes.get(index).updateCenter(pos);
	}
	
	/**
	 * Returns reference to a fish object if the user's click is nearby to its centre. Used to move fish location under
	 * the calibrate fish method.
	 * @param userClickLocation
	 * @return nearest fish
	 */
	public Fish getNearbyFish(Point userClickLocation) {
		for (Fish fish: fishes) {
			if (fish.isNearPoint(userClickLocation)) {
				return fish;
			}
		}
		return null;
	}
	
	/**
	 * Saves the data from each fish into a csv file. Each fish is separated by an empty column.
	 * @param file The location for the data to be saved to.
	 * @throws FileNotFoundException If location is invalid.
	 */
	public void saveDataAsCSV(File file) throws FileNotFoundException {
		try (PrintWriter writer = new PrintWriter(file)) {
			int n_data = fishes.get(0).data.size();
			for (int i = 0; i < n_data; i++) {
				StringBuilder sb = new StringBuilder();
				for (Fish fish: fishes) {
					sb.append(fish.data.get(i).toCSV());
					sb.append(",,");
				}
				sb.append("\n");
				writer.write(sb.toString());
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Error: File not found");
			throw new FileNotFoundException();
		}
	}
	
	/**
	 * An internal class to handle the multi-threading necessary to have the movie playing while performing gui actions.
	 */
	public class MoviePlayer extends Thread implements Runnable {
		private volatile boolean running = true;
	    private volatile boolean paused = false;
	    private final Object pauseLock = new Object();
	    private List<MotionVideoCapture> videos;
		private int videoIndex = 0;
		private int frameCounter = 0, dataCounter = 0, framesPerDataCount = 10;
		
		
		public MoviePlayer() {
			this(new LinkedList<File>());
		}
		
		public MoviePlayer(Queue<File> videoFiles) {
			videos = new ArrayList<MotionVideoCapture>();
			for (File file: videoFiles) {
				videos.add(new MotionVideoCapture(file.getAbsolutePath()));
			}
		}
		
		public void run() {
			while (running && currentVideo().isOpened()) {
				synchronized (pauseLock) {
					if (!running) { // may have changed while waiting to synchronise on pauseLock
						break;
					}
					if (paused) {
						// First update the play/pause buttons
						setChanged(); notifyObservers();
	                    try {
	                        pauseLock.wait();
	                        // will cause this Thread to block until another thread calls pauseLock.notifyAll()
                            // Note that calling wait() will relinquish the synchronised lock that this thread holds on 
	                        // pauseLock so another thread can acquire the lock to call notifyAll()
	                    } catch (InterruptedException ex) {
	                        break;
	                    } finally {
	                    	dataCounter = 0;
	                    }
	                    if (!running) { // running might have changed since we paused
	                        break;
	                    }
	                }
					
					// get the next frame of the video
					if (currentVideo().nextFrame()) {
						frameCounter++;
						dataCounter = (dataCounter + 1) % framesPerDataCount;
						// now that we've handled the pause mechanism of the movie player, it's time to track the fish
						for (Fish fish: fishes) {
							fish.moveToBestCenter(currentVideo().getContours());
							if (dataCounter == 0) {
								fish.addData(currentVideo().get(Videoio.CAP_PROP_POS_MSEC));
							}
						}
						setChanged(); notifyObservers();
					} else {
						// no frames left in this video, so load the next one
						loadNextVideo();
					}
				}
			}
		}
		
		/**
		 * Stops the video from playing and closes the thread. To start the video again, you must start() the player 
		 * again.
		 */
		public void stopPlayer() {
			running = false;
		}
		
		/**
		 * Pauses the video thread. Use unpause() to get it kick-started again.
		 */
		public void pause() {
	        // May want to throw an IllegalStateException if !running
	        paused = true;
	    }
		
		/**
		 * Unblocks the running thread, which allows the movie to play again.
		 */
		public void unpause() {
	        synchronized (pauseLock) {
	            paused = false;
	            pauseLock.notifyAll(); // Unblocks thread
	        }
	    }
		
		/**
		 * Gets the motion video currently being played in the movie player.
		 * @return The current motion video object.
		 */
		public MotionVideoCapture currentVideo() {
			return videos.get(videoIndex);
		}
		
		public void loadNextVideo() {
			videoIndex++;
			if (videoIndex >= videos.size()) {
				System.out.println("All videos finished.");
				stopPlayer();
			} else {
				currentVideo().linkToPrevious(videos.get(videoIndex - 1));
			}
		}
	}
}