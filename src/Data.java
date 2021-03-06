import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import javax.print.attribute.IntegerSyntax;

/**
 * @author zengxiangrong
 * @date 2015/5/6
 */
class Data{
    // the filename of the data
	private String ENCODING = "utf-8";
    private String filename;

    private HashMap<String, Integer> statesPairsAppearingTimes = // 两个tag先后出现的额次数
        new HashMap<String, Integer>();
    private HashMap<String, Integer> statesAppearingTimes = // 某个状态出现的次数
        new HashMap<String, Integer>();
    private HashMap<String, Integer> appearingStatesPairsTimes = // 某状态的以自己开始的状态对出现过的对数
        new HashMap<String, Integer>();
    private HashMap<String, Double> transformMatrix = // 一个状态转移到另一个状态的概率
        new HashMap<String, Double>();
    
    private HashMap<String, HashMap<String, Integer>> wordStatePairsAppearingTimes = // 词/词性 对出现的次数
    	new HashMap<String, HashMap<String, Integer>>();
    private HashMap<String, HashMap<String, Double>> confusionMatrix = // 词性选择某一词的概率
        new HashMap<String, HashMap<String, Double>>();
   
    public Data(String filename){
        this.filename = filename;
    }

    public HashMap<String, Double> getTransformMatrix(){
    	return transformMatrix;
    }
    public HashMap<String, HashMap<String, Double>> getConfusionMatrix(){
    	return confusionMatrix;
    }
    
    /**
     * 读入数据，并进行预处理。然后进行所需的统计
     */
    public void process(){
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
                
                String[] pairs = line.split("#");
                for (int i=1; i < pairs.length; i++){
//                    System.out.println(pairs[i]);
                    countbiGramHMM(i-1, pairs);
                    countWordStatesAppearingTimes(pairs[i]);
                }
                if(statesAppearingTimes.get("*") ==null){
                    statesAppearingTimes.put("*", 1);
                }else{
                    statesAppearingTimes.put("*", 1+statesAppearingTimes.get("*"));
                }
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        calcConfusionMatrix();
//System.out.println(confusionMatrix);
        calcTransformMatrix();
//System.out.println(wordStatePairsAppearingTimes);
//System.out.println(transformMatrix);
    }
       
    /*
     * 一阶马尔科夫
     */
    private void countbiGramHMM(int position, String[] pairs){
        String currentPair = pairs[position + 1];
        String[] currentWordStatePair = currentPair.split("/");

        String statesPairs = "";
        if(position == 0){
            statesPairs = "*," + currentWordStatePair[1];
        }else{
            String[] lastWordStatePair = pairs[position].split("/");
            statesPairs = lastWordStatePair[1] + "," + currentWordStatePair[1];
        }
        if (statesPairsAppearingTimes.get(statesPairs) == null){
            statesPairsAppearingTimes.put(statesPairs, 1);
        }else{
            int value = statesPairsAppearingTimes.get(statesPairs);
            statesPairsAppearingTimes.put(statesPairs, value + 1);
        }
        //
        if (statesAppearingTimes.get(currentWordStatePair[1]) == null){
            statesAppearingTimes.put(currentWordStatePair[1],1);
        }else{
            int value = statesAppearingTimes.get(currentWordStatePair[1]);
            statesAppearingTimes.put(currentWordStatePair[1], value+1);
        }
    }

    private void countAppearingStatesPairsTimes(){
    	for(Map.Entry<String, Integer> entry: statesAppearingTimes.entrySet()){
            String tag = entry.getKey().split(",")[0];
            if(appearingStatesPairsTimes.containsKey(tag)){
            	appearingStatesPairsTimes.put(tag, appearingStatesPairsTimes.get(tag)+1);
            }else{
            	appearingStatesPairsTimes.put(tag, 1);
            }
    	}
    }
    
    /*
     * 计算混淆矩阵
     */
    private void calcTransformMatrix(){
    	countAppearingStatesPairsTimes();
        for(Map.Entry<String, Integer> entry: statesAppearingTimes.entrySet()){
            String tag = entry.getKey();
            for(Map.Entry<String, Integer> entryNext: statesAppearingTimes.entrySet()){
                String tagNext = entryNext.getKey();
                
                String statesPair = tag + "," + tagNext;
                int statesPairTimes;
                // 加一平滑
                if(statesPairsAppearingTimes.containsKey(statesPair)){
                	statesPairTimes = statesPairsAppearingTimes.get(statesPair);
                }else{
                	statesPairTimes = 1;
                }
                int statesNumber = statesAppearingTimes.size();
                int modifiedStatesPairTimes = (statesNumber - appearingStatesPairsTimes.get(tag))
                		+ statesAppearingTimes.get(tag);
                double posibility = Math.log(1.0*statesPairTimes/modifiedStatesPairTimes);
                // 存入map
                transformMatrix.put(statesPair, posibility);
                
            }
        }
    }

    /*
     * 统计词/词性对的出现情况
     */
    private void countWordStatesAppearingTimes(String pair){
        String[] wordStatePair = pair.split("/");
        String word = wordStatePair[0];
        String state = wordStatePair[1];
        HashMap<String, Integer> wordTransformMatrix =
            new HashMap<String, Integer>();
        if(wordStatePairsAppearingTimes.get(word) == null){
            wordTransformMatrix.put(state,1);
        }else{
            wordTransformMatrix = wordStatePairsAppearingTimes.get(word);
            if(wordTransformMatrix.get(state) == null){
                wordTransformMatrix.put(state,1);
            }else{
                int value = wordTransformMatrix.get(state);
                wordTransformMatrix.put(state, value+1);
            }
        }
        wordStatePairsAppearingTimes.put(word,wordTransformMatrix);
    }
    
    /*
     * 计算状态转移矩阵
     */
    private void calcConfusionMatrix(){
    	for(Map.Entry<String, HashMap<String, Integer>> entry: 
    		wordStatePairsAppearingTimes.entrySet()){
            String word = entry.getKey();
            HashMap<String, Integer> tagsAppearingTimes = entry.getValue();
            
            // 对词计算概率出现每一种tag的概率p(word|tag)
            HashMap<String, Double> wordTransformationMatrix = 
            		new HashMap<String, Double>();
            for(Map.Entry<String, Integer> tagEntry: tagsAppearingTimes.entrySet()){
            	String tag = tagEntry.getKey();
            	int wordTagAppearingTimes = tagEntry.getValue();
            	int tagAppearingTimes = statesAppearingTimes.get(tag);
            	double value = Math.log(wordTagAppearingTimes*1.0/ tagAppearingTimes);
            	wordTransformationMatrix.put(tag, value);
//System.out.println(tagAppearingTimes + " / " +wordAppearingTimes);
            }
            // 保存
            confusionMatrix.put(word, wordTransformationMatrix);
            
    	}
    }
    
    /*
     * 写入到文件
     */
    public void saveToFile(){
    	// transformMatrix
    	String transformMatrixFileName = "c:\\Users\\sunder\\Documents\\eclipse\\workspace\\tagging\\data\\transform_matrix.txt";
    	try {
			BufferedWriter confusionMatrixWriter = 
					new BufferedWriter(new FileWriter(transformMatrixFileName));
			for(Map.Entry<String, Double> entry: transformMatrix.entrySet()){
				String pair = entry.getKey();
				double value = entry.getValue();
				confusionMatrixWriter.write(pair + ":" + value + "\n");
			}
			confusionMatrixWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

    	// confusionMatrix 
    	String confusionMatrixFileName = "c:\\Users\\sunder\\Documents\\eclipse\\workspace\\tagging\\data\\confusion_matrix.txt";
    	try {
			BufferedWriter transformMatrixWriter = 
					new BufferedWriter(new FileWriter(confusionMatrixFileName));
			for(Map.Entry<String, HashMap<String, Double>> entry: 
				confusionMatrix.entrySet()){
				String word = entry.getKey();
				HashMap<String, Double> tags = entry.getValue();
				String writeLine = word ;
				for(Map.Entry<String, Double> tagEntry: tags.entrySet()){
					String tag = tagEntry.getKey();
					Double value = tagEntry.getValue();
					writeLine += "," + tag + ":" + value;
				}					
				transformMatrixWriter.write(writeLine + "\n");
			}
			transformMatrixWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}
