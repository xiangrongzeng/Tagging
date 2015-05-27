import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.Iterator;


public class TagMain {

	private static String ENCODING = "utf-8";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("训练中。。。");
		String filename = 
				"c:\\Users\\sunder\\Documents\\eclipse\\workspace\\tagging\\data\\199801.txt";
		Data data = new Data(filename);
		data.process();
		data.saveToFile();
		
		System.out.println("调用viterbi算法计算中。。。 ");
		HashMap<String, Double> confusionMatrix = data.getTransformMatrix();
		HashMap<String, HashMap<String, Double>> transformMatrix = data.getConfusionMatrix();
//	String[] words = new String[7];
//	words[0] = "123建国";
//	words[1] = "以来";
//	words[2] = "的";
//	words[3] = "第九";
//	words[4] = "任";
//	words[5] = "总统";
//	words[6] = "。";
//	String wordsLine = "";
//	for(String word: words){
//		wordsLine += word;
//	}
//	System.out.println(wordsLine);
//		Viterbi viterbi = new Viterbi(words, confusionMatrix, transformMatrix);
//		viterbi.calc();
		System.out.println("测试中");
		double averageScore = test(confusionMatrix, transformMatrix);
		System.out.println("平均分是");
		System.out.println(averageScore);
	}
	
	/**
	 * 对当前模型进行评价
	 * 评价指标：平均每个句子标注的正确率
	 */
	public static double test(HashMap<String, Double> confusionMatrix,
			HashMap<String, HashMap<String, Double>> transformMatrix){
		// 用训练语料测试。因为缺少语料，无奈之举
		String filename = 
				"c:\\Users\\sunder\\Documents\\eclipse\\workspace\\tagging\\data\\199801.txt";
		ArrayList<String[]> sentences = new ArrayList<String[]>(); // 存储所有“句子”。这些“句子”都是句子分词得到的词
		// 读入数据
		try{
            BufferedReader br = new BufferedReader(new InputStreamReader(  
                    new FileInputStream(filename), ENCODING));
            String line;
            while((line = br.readLine()) != null){
//                System.out.println(line);
            	// 处理双空格和单个空格
            	Pattern p = Pattern.compile("\\s+");
            	Matcher m = p.matcher(line);
            	line = m.replaceAll("#");
                // 处理 [ 符号带来的问题
                p = Pattern.compile("\\[");
                m = p.matcher(line);
                line = m.replaceAll("");
                // 处理] 符号带来的问题
                p = Pattern.compile("].*?#");
                m = p.matcher(line);
                line = m.replaceAll("#");
               
                String[] tmpPairs = line.split("#");
                if(tmpPairs.length > 1){
                    String[] pairs = new String[tmpPairs.length - 1];
                    // tmpPairs 的第一项不要
                    for(int i = 1; i < tmpPairs.length; i++){
                    	pairs[i-1] = tmpPairs[i];
                    }
                    sentences.add(pairs);
                }
            }
		}catch (Exception e) {
			// TODO: handle exception
            e.printStackTrace();
		}
		
		double[] scores = new double[sentences.size()]; // 存储每个句子的得分
		// 分别计算每一个句子的得分
		for(int sentenceNumber = 0; sentenceNumber < sentences.size(); sentenceNumber++){
			String[] pairs = sentences.get(sentenceNumber);
			// 产生词数组和标准的词性数组
			String[] words = new String[pairs.length];
			String[] tags = new String[pairs.length]; // 语料中的词性
			for(int i = 0; i<pairs.length; i++){
				String[] wordAndTag = pairs[i].split("/");
				words[i] = wordAndTag[0];
				tags[i] = wordAndTag[1];
			}
			Viterbi viterbi = new Viterbi(words, confusionMatrix, transformMatrix);
			String[] myTags = new String[words.length];
			double score = 0;
			try {
				myTags = viterbi.calc(); // 计算得到的词性				
				// 计算标准词性和算法计算得到的词性之间的差别
				int correctNumber = 0;
				for(int i = 0; i < tags.length; i++){
					if(tags[i].equals(myTags[i]))	correctNumber += 1;
				}
				score = correctNumber*1.0 / tags.length;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			scores[sentenceNumber] = score;
//			System.out.println(score);
			
		}
		
		// 计算平均分
		double sum = 0;
		for(double score : scores){
			sum += score;
		}
            
		double averageScore = sum *1.0 / scores.length;
		return averageScore;
	}

}
