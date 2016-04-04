package uk.ac.aber.dcs.cs394.sta17.majorproject;

import java.io.File;
import java.io.IOException;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * This is the class that predicts the song
 * 
 * @author Steven(Sta17@aber.ac.uk)
 *
 */
public class Classifier {

	private String modelPath;
	private String inputPath;
	private int x;

	private MultilayerPerceptron mlp;
	private CSVLoader loader;
	private Instances unlabeled;
	private Instances labeled;

	public Classifier(String modelPath, String inputPath) {
		this.modelPath = modelPath;
		this.inputPath = inputPath;
		mlp = new MultilayerPerceptron();
		loader = new CSVLoader();
		labeled = null;
	}

	/**
	 * Reads in the file, spesified in the constructor and "classifies"/predicts data based on that.
	 * 
	 * Source:
	 * https://weka.wikispaces.com/Use+Weka+in+your+Java+code#Classification-Classifying instances
	 * 
	 * @author Steven/Source.
	 * @throws IOException Failure has happened in the data to predict during data reading in.
	 * @throws Exception Reading of the model from modelpath has failed or if an error occurred during the prediction.
	 */
	public void Classify() throws IOException, Exception {

		// Load Classifier
		try {
			mlp = (MultilayerPerceptron) weka.core.SerializationHelper.read(modelPath);
		} catch (Exception e) {
			throw e;
		}

		// load unlabeled data
		try {
			loader.setSource(new File(inputPath));
			unlabeled = loader.getDataSet();
		} catch (IOException e) {
			throw e;
		}

		// set class attribute
		unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
		x = unlabeled.numAttributes();

		// create copy
		labeled = new Instances(unlabeled);

		// label instances
		for (int i = 0; i < unlabeled.numInstances(); i++) {
			double clsLabel;
			try {
				clsLabel = mlp.classifyInstance(unlabeled.instance(i));
				labeled.instance(i).setClassValue(clsLabel);
			} catch (Exception e) {
				throw e;
			}
		}
		
		// save labeled data
		// System.out.println(labeled.toString());
		for (int i = 0; i < labeled.size(); i++) {
			//System.out.println("Song Genre Classification is: " + labeled.get(i).stringValue(x));
		}

	}
	
	/**
	 * Returns the Genre of the specified instance i predicted, 
 	 * if input file contains only one file than i should be 0.
	 * Returns null if no classification has happened or if 
	 * index number i is larger than number of instances of 
	 * songs predicted.
	 * 
	 * @author Steven
	 * @param i  is Index number 
	 * @return String containing the genre
	 */
	public String getGenre(int i) {
		if (labeled.size() > i || labeled == null) {
			String temp = labeled.get(i).stringValue(x);
			return temp;
		} else {
			return null;
		}
	}

}
