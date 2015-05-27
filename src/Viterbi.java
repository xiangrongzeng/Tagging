import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.spec.IvParameterSpec;


public class Viterbi {
	private String[] words;
	
	private HashMap<String, Double> confusionMatrix = // 一个状态转移到另一个状态的概率
	        new HashMap<String, Double>();
	private HashMap<String, HashMap<String, Double>> transformMatrix = // 词选择某一词性（状态）的概率
	        new HashMap<String, HashMap<String, Double>>();
	int[][] priorTagRecords;
	String[][] tagsRecords;
	
	public Viterbi(String[] words, 
			HashMap<String, Double> confusionMatrix,
			HashMap<String, HashMap<String, Double>> transformMatrix){
		this.words = words;
		this.transformMatrix = transformMatrix;
		this.confusionMatrix = confusionMatrix;
		
		priorTagRecords = new int[words.length][]; // 记录每个词的某一tag对应前一个词的哪个tag
		tagsRecords = new String[words.length][]; // 记录每个词词的所有tag
	}
	
	
	public String[] calc() throws Exception{
		double[] score = new double[1];
		for(int i = 0; i < words.length; i++){
			double[] newScore = calcOne(words, i, score);		
			score = newScore;
//			System.out.println("score");
//			showArray(score);
		}
		
		String result ="";
		String[] tags = traceBack(score);
		for (int i = 0; i < words.length; i++) {
			result += words[i] + "/" + tags[i] + " ";
		}
//		System.out.println(result);
		return tags;
	}
	
	private double[] calcOne(String[] words, int wordNumber, double[] score){		
//		int wordTagsNumber = transformMatrix.get(words[wordNumber]).size();
		HashMap<String, Double> wordTags = 
				transformMatrix.get(words[wordNumber]);
		int[] wordPriorTagRecords = new int[wordTags.size()]; // 记录前一个状态
		String[] wordTagsRecords = new String[wordTags.size()];
		double[] newScore = new double[wordTags.size()];
		
		
		int tagNumber = 0;
		for(String tag : wordTags.keySet()){ // 当前词的每一个状态（tag）
			if(wordNumber == 0){
				newScore[tagNumber] = wordTags.get(tag);
				wordTagsRecords[tagNumber] = tag;
				wordPriorTagRecords[tagNumber] = -1;
			}else{
				double maxValue = -1000000;
				int maxValueIndex = -1;
				for(int i = 0; i < tagsRecords[wordNumber-1].length; i++){ // 前一个词的每个状态
					String priorTag = tagsRecords[wordNumber-1][i];
					String statesPair = priorTag + "," + tag;
					double confuseValue = confusionMatrix.get(statesPair);
					if(confuseValue > maxValue){
						maxValue = confuseValue;
						maxValueIndex = i;
					}
				}
				wordPriorTagRecords[tagNumber] = maxValueIndex;
				wordTagsRecords[tagNumber] = tag;
				newScore[tagNumber] = score[maxValueIndex] + maxValue + wordTags.get(tag);
			}
			tagNumber += 1;
		}
		
		priorTagRecords[wordNumber] = wordPriorTagRecords;
		tagsRecords[wordNumber] = wordTagsRecords;
		
		return newScore;
	}

	private void showArray(double[] score){
		for(double s : score){
			System.out.println(s);
		}
	}
	
	private String[] traceBack(double[] score) throws Exception{
		int maxIndex = -1;
		double maxValue = -1000000;
		for(int i = 0; i < score.length; i++){
			if(score[i] > maxValue){
				maxValue = score[i];
				maxIndex = i;
			}
		}
		
		int currentTagIndex = maxIndex;
		String[] tags = new String[tagsRecords.length];		
		for( int wordNumber = tags.length-1; wordNumber > 0; wordNumber--){
			tags[wordNumber] = tagsRecords[wordNumber][currentTagIndex];
			int priorTagIndex = priorTagRecords[wordNumber][currentTagIndex];
			currentTagIndex = priorTagIndex;
		}
		tags[0] = tagsRecords[0][currentTagIndex];
		return tags;
	}
}
