import java.util.HashMap;


public class TagMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("训练中。。。");
		String filename = 
				"c:\\Users\\sunder\\Documents\\eclipse\\workspace\\tagging\\data\\199801.txt";
		Data data = new Data(filename);
		data.process();
		data.saveToFile();
		
		System.out.println("调用viterbi算法计算中。。。 ");
		HashMap<String, Double> confusionMatrix = data.getConfusionMatrix();
		HashMap<String, HashMap<String, Double>> transformMatrix = data.getTransformMatrix();
	String[] words = new String[7];
	words[0] = "123建国";
	words[1] = "以来";
	words[2] = "的";
	words[3] = "第九";
	words[4] = "任";
	words[5] = "总统";
	words[6] = "。";
	String wordsLine = "";
	for(String word: words){
		wordsLine += word;
	}
	System.out.println(wordsLine);
		Viterbi viterbi = new Viterbi(words, confusionMatrix, transformMatrix);
		viterbi.calc();
	}

}
