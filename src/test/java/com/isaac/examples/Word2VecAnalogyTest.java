package com.isaac.examples;

import com.isaac.word2vecf.utils.WordVectorSerializer;
import com.isaac.word2vecf.Word2Vec;
import org.nd4j.linalg.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by zhanghao on 20/4/17.
 * @author  ZHANG HAO
 * email: isaac.changhau@gmail.com
 */
public class Word2VecAnalogyTest {

	private static Logger log = LoggerFactory.getLogger(Word2VecAnalogyTest.class);

	public static void main (String[] args) {
		Word2Vec w2v = WordVectorSerializer.loadWord2VecModel("/home/zhanghao/Documents/GoogleNews-vectors-negative300.bin", true);
		// 2. Analogy test -- "king - queen = man - woman"
		measure(w2v);
	}

	private static void measure(Word2Vec w2v) {
		log.info("load SynSem data...");
		Map<String, List<SynSemNode>> anaMap = loadSynSemData();
		log.info("run the test..");
		for (Map.Entry<String, List<SynSemNode>> entry : anaMap.entrySet()) {
			log.info(entry.getKey());
			List<SynSemNode> list = entry.getValue();
			int total = 0;
			int pass = 0;
			int ignore = 0;
			for (SynSemNode node : list) {
				if (w2v.hasWord(node.str1) && w2v.hasWord(node.str2) && w2v.hasWord(node.str3)) {
					total++;
					String answer = w2v.wordsNearest(Arrays.asList(node.str2, node.str3), Collections.singletonList(node.str1), 1).iterator().next();
					// convert answer and gold result to same form to eliminate errors
					answer = answer.replaceAll("[^A-Za-z]", "");
					if (answer.length() < 1) { ignore++; continue; }
					answer = answer.toLowerCase();
					answer = answer.substring(0, 1).toUpperCase() + answer.substring(1);
					String nodeStr4 = node.str4.substring(0, 1).toUpperCase() + node.str4.substring(1);
					if (answer.equals(nodeStr4)) pass++;
					log.info(node.toString() + "\t" + answer + "\t" + answer.equals(nodeStr4));
				} else { ignore++; }
			}
			log.info("Total Questions: " + list.size() + ", Ignore: " + ignore + ", Accuracy: " + String.format("%.2f", (1.0 * pass) / total * 100.0) + "%(" + pass + "/" + total + ")");
		}
		log.info("done.");
	}

	public static Map<String, List<SynSemNode>> loadSynSemData() {
		Map<String, List<SynSemNode>> map = new LinkedHashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new ClassPathResource("data/questions-words.txt").getFile())));
			List<List<SynSemNode>> ques = new ArrayList<>();
			List<String> head = new ArrayList<>();
			String line;
			List<SynSemNode> list = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				if (line.split(" ")[0].equals(":")) {
					head.add(line.split(" ")[1]);
					if (list.size() > 0) {
						ques.add(list);
						list = new ArrayList<>();
					}
				} else {
					String[] arr = line.split(" ");
					SynSemNode node = new SynSemNode(arr[0], arr[1], arr[2], arr[3]);
					list.add(node);
				}
			}
			if (list.size() > 0) ques.add(list);
			for (int i = 0; i < head.size(); i++) map.put(head.get(i), ques.get(i));
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	static class SynSemNode {
		String str1;
		String str2;
		String str3;
		String str4;
		SynSemNode (String str1, String str2, String str3, String str4) {
			this.str1 = str1;
			this.str2 = str2;
			this.str3 = str3;
			this.str4 = str4;
		}
		@Override
		public String toString () {
			return this.str1 + " " + this.str2 + " " + this.str3 + " " + this.str4;
		}
	}
}
