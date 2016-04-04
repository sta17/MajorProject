package uk.ac.aber.dcs.cs394.sta17.majorproject.features;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.UnsupportedAudioFileException;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

/**
 * Provides support for different types of command line audio feature
 * extraction. Based on work by Joren Six
 * 
 * @author Steven, Feature extractors by Joren Six
 */
public class FeatureExtractor {

	private SoundPressureLevelExtractor SoundPressureLevelExtractor;
	private PitchExtractor PitchExtractor;
	private RootMeanSquareExtractor RootMeanSquareExtractor;
	private OnsetExtractor OnsetExtractor;
	private BeatExtractor BeatExtractor;

	private ArrayList<Double> rmsTimestamp, soundTimestamp, pitchTimestamp, onsetTimestamp, beatTimestamp;
	private ArrayList<Double> rms, soundSPL;
	private ArrayList<Float> pitchPitch, pitchProability;
	private int SAMPLING_NUMBER;

	public FeatureExtractor(int SAMPLING_NUMBER) {

		this.SAMPLING_NUMBER = SAMPLING_NUMBER;

		rmsTimestamp = new ArrayList<Double>();
		rms = new ArrayList<Double>();
		soundTimestamp = new ArrayList<Double>();
		soundSPL = new ArrayList<Double>();
		pitchTimestamp = new ArrayList<Double>();
		pitchPitch = new ArrayList<Float>();
		pitchProability = new ArrayList<Float>();

		onsetTimestamp = new ArrayList<Double>();
		beatTimestamp = new ArrayList<Double>();

		SoundPressureLevelExtractor = new SoundPressureLevelExtractor();
		PitchExtractor = new PitchExtractor();
		RootMeanSquareExtractor = new RootMeanSquareExtractor();
		OnsetExtractor = new OnsetExtractor();
		BeatExtractor = new BeatExtractor();

	}

	/*-------------------------
	 * Main Class Methods Here
	 * ------------------------
	 */

	/**
	 * Does all the feature Extraction for from the audio File path supplied.
	 * Takes in an path for an audio file, runs it thought the extractors and
	 * puts out a collection of Features in the form of FeatureCollection class.
	 * 
	 * @param audioFilePath
	 *            the audio
	 * @throws UnsupportedAudioFileException
	 *             throws this if the audio file format is not supported by
	 *             FFMPEG.
	 * @throws IOException
	 *             throws this error if error happens during audio file reading
	 *             in.
	 */
	public FeatureCollection ExtractFeatures(String audioFilePath) throws IOException, UnsupportedAudioFileException {
		FeatureCollection fc = new FeatureCollection();

		ArrayList<Double> finalTimestamp = new ArrayList<Double>();
		ArrayList<Double> finalRMS = new ArrayList<Double>();
		ArrayList<Double> finalSoundSPL = new ArrayList<Double>();
		ArrayList<Float> finalPitchPitch = new ArrayList<Float>();
		ArrayList<Float> finalPitchProability = new ArrayList<Float>();

		// -------------------------------------------------------------
		// Do Feature Extraction from file
		// -------------------------------------------------------------

		try {
			RootMeanSquareExtractor.run(audioFilePath);
			SoundPressureLevelExtractor.run(audioFilePath);
			PitchExtractor.run(audioFilePath);
			OnsetExtractor.run(audioFilePath);
			BeatExtractor.run(audioFilePath);
		} catch (IOException e) {
			throw e;
		}
		;

		// -------------------------------------------------------------
		// Removing all times which don't exist in any other list
		// -------------------------------------------------------------

		finalTimestamp = rmsTimestamp;
		finalTimestamp.retainAll(soundTimestamp);
		finalTimestamp.retainAll(pitchTimestamp);

		// ----------------------------------------------------------
		// Getting The shared time values
		// ----------------------------------------------------------

		int size = finalTimestamp.size();
		size = size / SAMPLING_NUMBER;
		int temp = size;

		for (int i = 0; i < SAMPLING_NUMBER; i++) {
			Double value = rms.get(rmsTimestamp.indexOf(finalTimestamp.get(size - 1)));
			Double finalValue = Math.round(value * 1000000.0) / 1000000.0;
			finalRMS.add(finalValue);
			// System.out.println(finalValue);

			value = soundSPL.get(soundTimestamp.indexOf(finalTimestamp.get(size - 1)));
			finalValue = Math.round(value * 1000000.0) / 1000000.0;
			finalSoundSPL.add(finalValue);
			// System.out.println(finalValue);

			Float value2 = pitchPitch.get(pitchTimestamp.indexOf(finalTimestamp.get(size - 1)));
			if (value2 > 0) {
				Float finalValue2 = (float) (Math.round(value2 * 1000000.0) / 1000000.0);
				finalPitchPitch.add(finalValue2);
				// System.out.println(finalValue2);
			} else {
				finalPitchPitch.add((float) 0);
				// System.out.println(0);
			}

			value2 = pitchProability.get(pitchTimestamp.indexOf(finalTimestamp.get(size - 1)));
			Float finalValue2 = (float) (Math.round(value2 * 1000000.0) / 1000000.0);
			finalPitchProability.add(finalValue2);
			// System.out.println(finalValue2);

			size = size + temp;
		}

		fc.setPitchPitch(finalPitchPitch);
		fc.setPitchProability(finalPitchProability);
		fc.setRMS(finalRMS);
		fc.setSoundSPL(finalSoundSPL);

		// ----------------------------------------------------------
		// Getting The other values
		// ----------------------------------------------------------

		// Onset Value
		fc.setOnsetCount(onsetTimestamp.size());
		// Beat Value
		fc.setBeatCount(beatTimestamp.size());
		return fc;

	}

	/*-------------------------
	 * Feature Extractors Here
	 * ------------------------
	 */

	/**
	 * Calculates the root mean square of an audio signal for each block of 2048
	 * samples. The output gives you a timestamp and the RMS value.
	 * 
	 * @author Joren Six, modified by Steven
	 *
	 */
	private class RootMeanSquareExtractor {

		/**
		 * This is main method for the Feature Extractor.
		 * 
		 * @param inputFile
		 *            the audio file to take features from.
		 * @return true if success.
		 * @throws IOException
		 *             When an error occurs reading the file.
		 * @throws UnsupportedAudioFileException
		 *             if the audio file format is not supported
		 */
		public boolean run(String inputFile) throws IOException, UnsupportedAudioFileException {
			File audioFile = new File(inputFile);
			int size = 2048;
			int overlap = 0;

			AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, size, overlap);

			dispatcher.addAudioProcessor(new AudioProcessor() {
				@Override
				public void processingFinished() {
				}

				@Override
				public boolean process(AudioEvent audioEvent) {
					// System.out.println(audioEvent.getTimeStamp() + ";" +
					// audioEvent.getRMS());

					rmsTimestamp.add(audioEvent.getTimeStamp());
					rms.add(audioEvent.getRMS());

					return true;
				}
			});
			dispatcher.run();
			return true;
		}
	}

	/**
	 * Calculates a sound pressure level in dB for each block of 2048 samples.
	 * The output gives you a timestamp and a value in dBSPL.
	 * 
	 * @author Joren Six, modified by Steven
	 *
	 */
	private class SoundPressureLevelExtractor {

		/**
		 * This is main method for the Feature Extractor.
		 * 
		 * @param inputFile
		 *            the audio file to take features from.
		 * @return true if success.
		 * @throws IOException
		 *             When an error occurs reading the file.
		 */
		public boolean run(String inputFile) throws IOException {
			int size = 2048;
			int overlap = 0;

			final SilenceDetector silenceDetecor = new SilenceDetector();
			AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(inputFile, 44100, size, overlap);
			dispatcher.addAudioProcessor(silenceDetecor);
			dispatcher.addAudioProcessor(new AudioProcessor() {
				@Override
				public void processingFinished() {
				}

				@Override
				public boolean process(AudioEvent audioEvent) {
					// System.out.println(audioEvent.getTimeStamp() + ";" +
					// silenceDetecor.currentSPL());

					soundTimestamp.add(audioEvent.getTimeStamp());
					soundSPL.add(silenceDetecor.currentSPL());

					return true;
				}
			});
			dispatcher.run();
			return true;
		}
	}

	/**
	 * Calculates pitch in Hz for each block of 2048 samples. The output is
	 * frequency in hertz and probability which describes how pitched the sound
	 * is at the given time.
	 * 
	 * @author Joren Six, modified by Steven
	 *
	 */
	private class PitchExtractor implements PitchDetectionHandler {

		/**
		 * This is main method for the Feature Extractor.
		 * 
		 * @param inputFile
		 *            the audio file to take features from.
		 * @return true if success.
		 * @throws IOException
		 *             When an error occurs reading the file.
		 */
		public boolean run(String inputFile) throws IOException {
			PitchEstimationAlgorithm algo = PitchEstimationAlgorithm.FFT_YIN;

			File audioFile = new File(inputFile);

			int size = 1024;
			int overlap = 0;
			AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(audioFile.getAbsolutePath(), 44100, size,
					overlap);
			dispatcher.addAudioProcessor(new PitchProcessor(algo, 44100, size, this));
			dispatcher.run();
			return true;

		}

		@Override
		public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
			double timeStamp = audioEvent.getTimeStamp();
			float pitch = pitchDetectionResult.getPitch();
			float probability = pitchDetectionResult.getProbability();
			// System.out.println(timeStamp + ";" + pitch + ";" + probability);

			pitchTimestamp.add(timeStamp);
			pitchPitch.add(pitch);
			pitchProability.add(probability);

		}
	}

	/**
	 * Calculates onsets using a complex domain onset detector.
	 * 
	 * @author Joren Six, modified by Steven
	 *
	 */
	private class OnsetExtractor implements OnsetHandler {

		/**
		 * This is main method for the Feature Extractor.
		 * 
		 * @param inputFile
		 *            the audio file to take features from.
		 * @return true if success.
		 * @throws IOException
		 *             When an error occurs reading the file.
		 */
		public boolean run(String inputFile) throws IOException {
			File audioFile = new File(inputFile);
			int size = 512;
			int overlap = 256;
			AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(audioFile.getAbsolutePath(), 44100, size,
					overlap);
			ComplexOnsetDetector detector = new ComplexOnsetDetector(size, 0.7, 0.1);
			detector.setHandler(this);
			dispatcher.addAudioProcessor(detector);

			dispatcher.run();
			return true;
		}

		@Override
		public void handleOnset(double time, double salience) {
			// System.out.println(time + " , " + salience);
			onsetTimestamp.add(time);
		}
	}

	/**
	 * Finds the number of beats in the audio provided.
	 * 
	 * @author Joren Six, modified by Steven
	 *
	 */
	private class BeatExtractor implements OnsetHandler {

		/**
		 * This is main method for the Feature Extractor.
		 * 
		 * @param inputFile
		 *            the audio file to take features from.
		 * @return true if success.
		 * @throws IOException
		 *             When an error occurs reading the file.
		 * @throws UnsupportedAudioFileException
		 *             if the audio file format is not supported
		 */
		public boolean run(String inputFile) throws IOException, UnsupportedAudioFileException {
			File audioFile = new File(inputFile);
			int size = 512;
			int overlap = 256;
			AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, size, overlap);

			ComplexOnsetDetector detector = new ComplexOnsetDetector(size);
			BeatRootOnsetEventHandler handler = new BeatRootOnsetEventHandler();
			detector.setHandler(handler);

			dispatcher.addAudioProcessor(detector);
			dispatcher.run();

			handler.trackBeats(this);

			return true;
		}

		@Override
		public void handleOnset(double time, double salience) {
			// System.out.println(time);
			beatTimestamp.add(time);
		}

	}

}
