package uk.ac.aber.dcs.cs394.sta17.majorproject;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;

import uk.ac.aber.dcs.cs394.sta17.majorproject.features.FeatureHandler;

public class Main {

	public static void main(String[] args) {

		String arffFile = "tempOutput.arff";
		String csvFilePath = "tempOutput.csv";
		String modelFilePath = "genreModel.model";
		int SAMPLING_NUMBER = 10;
		String GENRE = "ROCK";
		FeatureHandler handler = new FeatureHandler(SAMPLING_NUMBER,GENRE);

		try {
			handler.HandleSingleSong(csvFilePath);
			handler.HandleMultipleSongs(csvFilePath);
			System.out.println("Feature Extractor Finished Working.");
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}

		/*
		Classifier cls = new Classifier(modelFilePath,arffFile);
		try {
			cls.Classify();
			System.out.println(cls.getGenre(0));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Reading in data to predict has failed.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}

}
