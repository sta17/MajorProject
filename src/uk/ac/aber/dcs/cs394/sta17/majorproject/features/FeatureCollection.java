package uk.ac.aber.dcs.cs394.sta17.majorproject.features;

import java.util.ArrayList;

/**
 * This class holds the collection of the 
 * various features collected from a song.
 * 
 * @author Steven
 *
 */
public class FeatureCollection {

	private ArrayList<Double> rms, soundSPL;
	private ArrayList<Float> pitchPitch, pitchProability;
	private int onsetCount, beatCount;

	public FeatureCollection() {
		onsetCount = 0;
		beatCount = 0;
		rms = new ArrayList<Double>();
		soundSPL = new ArrayList<Double>();
		pitchPitch = new ArrayList<Float>();
		pitchProability = new ArrayList<Float>();
	}

	/*-------------------------
	 *  Getters and Setters
	 * ------------------------
	 */

	public ArrayList<Double> getRMS() {
		return rms;
	}

	public void setRMS(ArrayList<Double> rms) {
		this.rms = rms;
	}

	public ArrayList<Double> getSoundSPL() {
		return soundSPL;
	}

	public void setSoundSPL(ArrayList<Double> soundSPL) {
		this.soundSPL = soundSPL;
	}

	public ArrayList<Float> getPitchPitch() {
		return pitchPitch;
	}

	public void setPitchPitch(ArrayList<Float> pitchPitch) {
		this.pitchPitch = pitchPitch;
	}

	public ArrayList<Float> getPitchProability() {
		return pitchProability;
	}

	public void setPitchProability(ArrayList<Float> pitchProability) {
		this.pitchProability = pitchProability;
	}

	public int getOnsetCount() {
		return onsetCount;
	}

	public void setOnsetCount(int onsetCount) {
		this.onsetCount = onsetCount;
	}

	public int getBeatCount() {
		return beatCount;
	}

	public void setBeatCount(int beatCount) {
		this.beatCount = beatCount;
	}

}
