package uk.ac.aber.dcs.cs394.sta17.majorproject.features;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

/**
 * This is the main class that handles the management of the features.
 * 
 * @author Steven
 *
 */
public class FeatureHandler {

	private int SAMPLING_NUMBER;
	private String GENRE;

	public FeatureHandler(int SAMPLING_NUMBER, String GENRE) {
		this.SAMPLING_NUMBER = SAMPLING_NUMBER;
		this.GENRE = GENRE;
	}

	/*--------------------------
	 * File and Folder handling
	 *--------------------------
	 */
	
	/**
	 * Opens a java swing window for file navigation. 
	 * Returns null if windows is cancelled or exited.
	 * 
	 * @return the absolute path to the chosen file
	 */
	private String getPath() {
		JFileChooser chooser = new JFileChooser();
		String path;
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Model, csv, Wav & MP3 files", "wav", "mp3", "csv",
				"model");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			path = file.getAbsolutePath();
			return path;
		}
		return null;
	}

	/**
	 * Opens a java swing window for folder navigation. 
	 * Returns null if windows is cancelled or exited.
	 * 
	 * @return the absolute path to the chosen folder
	 */
	private String getFolder() {
		JFileChooser chooser;
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getCurrentDirectory();
			String directoryName = file.getAbsolutePath();
			return directoryName;
		}
		
		return null;
	}

	/**
	 * List all files from a directory and its subdirectories
	 * 
	 * @param directoryName
	 *            to be listed
	 */
	private void getPathNameInFolders(String directoryName, ArrayList<String> paths) {
		File directory = new File(directoryName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				String temp = file.getAbsolutePath();
				paths.add(temp);
			} else if (file.isDirectory()) {
				getPathNameInFolders(file.getAbsolutePath(), paths);
			}
		}
	}

	/*-------------------------
	 * Main Class Methods Here
	 *-------------------------
	 */

	/**
	 * Handles Feature Extraction for one Song. 
	 * Opens a java Swing window for file navigation.
	 * 
	 * @param csvFilePath the path to save the csv file, for output.
	 * @throws UnsupportedAudioFileException
	 *             throws this if the audio file format is not supported by
	 *             FFMPEG.
	 * @throws IOException
	 *             throws this error if error happens during output saving or
	 *             audio file reading.
	 */
	public void HandleSingleSong(String csvFilePath) throws IOException, UnsupportedAudioFileException {
		String path = getPath();

		FeatureCollection fc = new FeatureCollection();
		FeatureExtractor extractor = new FeatureExtractor(SAMPLING_NUMBER);

		fc = extractor.ExtractFeatures(path);
		generateCsvFile(fc, csvFilePath);
	}
	
	/**
	 * Handles Feature Extraction for one Song. If the file path can be provided thought code.
	 * 
	 * @param csvFilePath the path to save the csv file, for output.
	 * @param audioFilePath the audio file to extract features from.
	 * @throws UnsupportedAudioFileException
	 *             throws this if the audio file format is not supported by
	 *             FFMPEG.
	 * @throws IOException
	 *             throws this error if error happens during output saving or
	 *             audio file reading.
	 */
	public void HandleSingleSong(String csvFilePath,String audioFilePath) throws IOException, UnsupportedAudioFileException {
		FeatureCollection fc = new FeatureCollection();
		FeatureExtractor extractor = new FeatureExtractor(SAMPLING_NUMBER);

		fc = extractor.ExtractFeatures(audioFilePath);
		generateCsvFile(fc, csvFilePath);
	}

	
	/**
	 * Handles Feature Extraction for multiple songs.
	 * This is for Test data generation, rather than classification. 
	 * Needs to be provided an absolute path to a directory to work 
	 * from. 
	 * 
	 * @param csvFilePath the path to save the csv file, for output.
	 * @param directoryName the path from which songs are collected.
	 */
	public void HandleMultipleSongs(String csvFilePath, String directoryName)
			throws IOException, UnsupportedAudioFileException {
		ArrayList<String> paths = new ArrayList<String>();

			getPathNameInFolders(directoryName, paths);

			ArrayList<FeatureCollection> extractors = new ArrayList<FeatureCollection>();
			for (String path : paths) {

				FeatureCollection fc = new FeatureCollection();
				FeatureExtractor fes = new FeatureExtractor(SAMPLING_NUMBER);
				fc = fes.ExtractFeatures(path);
				extractors.add(fc);
			}
			
			generateCSVFileMultiList(extractors, csvFilePath);
	}
	
	/**
	 * Handles Feature Extraction for multiple songs.
	 * This is for Test data generation, rather than classification. 
	 * Opens a java Swing window for directory navigation.
	 * 
	 * @param csvFilePath the path to save the csv file, for output.
	 */
	public void HandleMultipleSongs(String csvFilePath)
			throws IOException, UnsupportedAudioFileException {
		ArrayList<String> paths = new ArrayList<String>();

			String directoryName = getFolder();
			getPathNameInFolders(directoryName, paths);

			ArrayList<FeatureCollection> extractors = new ArrayList<FeatureCollection>();
			for (String path : paths) {

				FeatureCollection fc = new FeatureCollection();
				FeatureExtractor fes = new FeatureExtractor(SAMPLING_NUMBER);
				fc = fes.ExtractFeatures(path);
				extractors.add(fc);
			}
			
			generateCSVFileMultiList(extractors, csvFilePath);
	}

	/*-------------------------
	 *  Output Handling
	 *-------------------------
	 */
	
	/**
	 * Source:
	 * 
	 * Based upon example in given source.
	 * 
	 * Generates the CSV file the classifier wants.
	 * 
	 * @author Steven
	 * @param fc
	 * @param csvFile
	 *            the file to which the FeatureExtractorV2 out is supposed to be
	 *            saved to.
	 * @throws IOException
	 *             Error thrown when something goes wrong durring the file
	 *             writing face.
	 */
	private void generateCsvFile(FeatureCollection fc, String csvFilePath) throws IOException {
		FileWriter writer = new FileWriter(csvFilePath);

		// --------------------------------------
		// Header stuff
		// --------------------------------------
		for (int i = 0; i < SAMPLING_NUMBER; i++) {
			writer.append("RMS" + i);
			writer.append(',');
			writer.append("soundpreassure" + i);
			writer.append(',');
			writer.append("Pitch" + i);
			writer.append(',');
			writer.append("Proability" + i);
			writer.append(',');
		}
		writer.append("OnsetCount");
		writer.append(',');
		writer.append("BeatCount");
		writer.append(',');
		writer.append("Genre");
		writer.append('\n');// newline

		// --------------------------------------
		// Values
		// --------------------------------------

		for (int i = 0; i < SAMPLING_NUMBER; i++) {
			writer.append(fc.getRMS().get(i) + "");
			writer.append(',');
			writer.append(fc.getSoundSPL().get(i) + "");
			writer.append(',');
			writer.append(fc.getPitchPitch().get(i) + "");
			writer.append(',');
			writer.append(fc.getPitchProability().get(i) + "");
			writer.append(',');
		}
		writer.append(fc.getOnsetCount() + "");
		writer.append(',');
		writer.append(fc.getBeatCount() + "");
		writer.append(',');
		writer.append(GENRE);

		// End file bits here.

		writer.flush();
		writer.close();
	}

	/**
	 * Source:
	 * 
	 * Based upon example in given source.
	 * 
	 * Generates the CSV file the classifier wants.
	 * 
	 * @author Steven
	 * @param csvFile
	 *            the file to which the FeatureExtractorV2 out is supposed to be
	 *            saved to.
	 * @throws IOException
	 *             Error thrown when something goes wrong durring the file
	 *             writing face.
	 */
	private void generateCSVFileMultiList(ArrayList<FeatureCollection> fc, String csvFilePath) throws IOException {
		try {
			FileWriter writer = new FileWriter(csvFilePath);

			// --------------------------------------
			// Header stuff
			// --------------------------------------
			for (int i = 0; i < SAMPLING_NUMBER; i++) {
				writer.append("RMS" + i);
				writer.append(',');
				writer.append("soundpreassure" + i);
				writer.append(',');
				writer.append("Pitch" + i);
				writer.append(',');
				writer.append("Proability" + i);
				writer.append(',');
			}
			writer.append("OnsetCount");
			writer.append(',');
			writer.append("BeatCount");
			writer.append(',');
			writer.append("Genre");
			writer.append('\n');// newline

			// --------------------------------------
			// Values
			// --------------------------------------

			for (FeatureCollection extractor : fc) {

				for (int i = 0; i < SAMPLING_NUMBER; i++) {
					writer.append(extractor.getRMS().get(i) + "");
					writer.append(',');
					writer.append(extractor.getSoundSPL().get(i) + "");
					writer.append(',');
					writer.append(extractor.getPitchPitch().get(i) + "");
					writer.append(',');
					writer.append(extractor.getPitchProability().get(i) + "");
					writer.append(',');
				}
				writer.append(extractor.getOnsetCount() + "");
				writer.append(',');
				writer.append(extractor.getBeatCount() + "");
				writer.append(',');
				writer.append(GENRE);
				writer.append('\n');// newline
			}

			// End file bits here.

			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * This method will take in a CSV file and export an ARFF file, which is the
	 * preferred type for WEKA.
	 * 
	 * @param csvFile
	 *            location to source CSV file.
	 * @param arffFile
	 *            path to save location for ARFF file.
	 * @throws IOException
	 *             Error in Reading or Saving of file.
	 */
	public void generateARFFFile(String csvFile, String arffFile) throws IOException {
		// load CSV
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(csvFile));
		Instances data = loader.getDataSet();

		// save ARFF
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(arffFile));
		saver.setDestination(new File(arffFile));
		saver.writeBatch();
	}

}
