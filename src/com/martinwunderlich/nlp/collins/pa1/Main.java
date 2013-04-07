package com.martinwunderlich.nlp.collins.pa1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.martinwunderlich.nlp.collins.common.Vocabulary;
import com.rits.cloning.Cloner;

public class Main {


	private static HashMap<String, Double> emissionParameters = new HashMap<String, Double>();
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> tagTypes = new ArrayList<String>();
		String countFilePath = "";
		String inFile = "";
		String outFile = "";
		
		countFilePath = args[0];
		inFile = args[1];
		outFile = args[2];
		
		tagTypes.add("O");
		tagTypes.add("I-GENE");
		
		CountFileProcessor countFileprocessor = new CountFileProcessor(tagTypes);
		countFileprocessor.readCountResults(countFilePath);
		
		// adjustTrainingDataForRareWords(countFileprocessor); // This is needed only in preparation of the count file
		
		
		calculateEmissionParameters(tagTypes, countFileprocessor);	

		// Part 1 - Naive unigram tagger
		//	GeneTagger myUnigramTagger = new SimpleGeneTagger(countFileprocessor, tagTypes, emissionParameters);
		//	myUnigramTagger.tag(inFile, outFile);
		
		// Part 2 - Hidden Markov Model / Viterbi algorithm
		//	TrigramGeneTagger myTrigramTagger = new TrigramGeneTagger(countFileprocessor, tagTypes, emissionParameters);
		//	myTrigramTagger.tag(inFile, outFile);
		
		// Part 3 - Additional rare word classes (Numeric, All Capitals, Last Capital)
		//		System.out.println("Adjusting training data for rare word classes...");
		//		adjustTrainingDataForRareWordsWithAdditionalRareWordClasses(countFileprocessor);
		//		System.out.println("DONE.");
			TrigramGeneTagger_v2 myTrigramTagger = new TrigramGeneTagger_v2(countFileprocessor, tagTypes, emissionParameters);
			myTrigramTagger.tag(inFile, outFile);		
	}


	private static void adjustTrainingDataForRareWords(CountFileProcessor countFileprocessor) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("/Users/martinwunderlich/Documents/Fortbildung/NLP_Michael_Collins_2013/Prog_Assignment_1/h1-p/gene.train"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/martinwunderlich/Documents/Fortbildung/NLP_Michael_Collins_2013/Prog_Assignment_1/h1-p/gene_adjusted.train"));
	        String line = "";
	        String[] lineParts; 
	        Vocabulary vocab = countFileprocessor.getVocabulary();
	        
	        while((line = br.readLine()) != null) {
	             lineParts = line.split(" ");
	             String word = "";
	             String tag = "";
	             if( lineParts[0].equals("") )
	             {
	            	 bw.newLine();
	             	 continue;
	             }
	             
	             if( lineParts.length > 0 )
	            	 word = lineParts[0];
	             if( lineParts.length > 1 )
	            	 tag = lineParts[1];
	             
	             int count = vocab.getCountForWord(word);
            	 
	             if(count < 5)
	            	 word = "_RARE_";
	             else
	            	 System.out.println(word + " " + count);
	             
	             String newLine = word;
	             if( tag != "") 
	            	 newLine += " " + tag;
	             
	             bw.write(newLine);
	             bw.newLine();
	        }
	        
	        bw.close();
	        br.close();
		}
		catch(Exception ex) {
			System.out.println("ERROR while adjusting training data file: " + ex.getMessage());
		}
	}
	
	private static void adjustTrainingDataForRareWordsWithAdditionalRareWordClasses(CountFileProcessor countFileprocessor) {
		int lineCounter = 0;
        try {
			BufferedReader br = new BufferedReader(new FileReader("/Users/martinwunderlich/Documents/Fortbildung/NLP_Michael_Collins_2013/Prog_Assignment_1/h1-p/gene.train"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/martinwunderlich/Documents/Fortbildung/NLP_Michael_Collins_2013/Prog_Assignment_1/h1-p/gene_adjusted_v2.train"));
	        String line = "";
	        String[] lineParts; 
	        Vocabulary vocab = countFileprocessor.getVocabulary();
	        
	        while((line = br.readLine()) != null) {
	        	 lineCounter++;
	        	 lineParts = line.split(" ");
	             String word = "";
	             String tag = "";
	             if( lineParts[0].equals("") )
	             {
	            	 bw.newLine();
	             	 continue;
	             }
	             
	             if( lineParts.length > 0 )
	            	 word = lineParts[0];
	             if( lineParts.length > 1 )
	            	 tag = lineParts[1];
	             
	             int count = vocab.getCountForWord(word);
            		             // Adjust word for rare word classes
	             if(count < 5)
	            	 if( isNumeric(word) )
	            		 word = "_NUMERIC_";
	            	 else if( isAllCapitals(word) )
	            		 word = "_ALLCAPS_";
	            	 else if( endsWithCapitalLetter(word) )
	            		 word = "_ENDSWITHCAP_";
	            	 else 
	            		 word = "_RARE_";
//	             else
//	            	 System.out.println(word + " " + count);
	             
	             String newLine = word;
	             if( tag != "") 
	            	 newLine += " " + tag;
	             
	             bw.write(newLine);
	             bw.newLine();
	        }
	        
	        bw.close();
	        br.close();
		}
		catch(Exception ex) {
			System.out.println("ERROR while adjusting training data file: " + ex.getMessage());
		}
		
		System.out.println("Adjusting training data...DONE.");
	}


	private static boolean endsWithCapitalLetter(String word) {
		String lastLetter = word.substring(word.length() - 1, word.length());
		if( isAllCapitals(lastLetter) )
			return true;
		else 
			return false;
	}


	private static boolean isAllCapitals(String word) {
		if( word.toUpperCase().equals(word) )
			return true;
		else 
			return false;
	}


	private static boolean isNumeric(String word) {
		for( char c : word.toCharArray() )
			if( Character.isDigit(c) )
				return true; 
		
		return false;
	}


	/**
	 * @param tagTypes
	 * @param countFileprocessor
	 */
	private static void calculateEmissionParameters(List<String> tagTypes,
			CountFileProcessor countFileprocessor) {
		
		Vocabulary vocabulary = countFileprocessor.getVocabulary();
		HashMap<String, Integer> wordTagCounts = countFileprocessor.getWordTagCounts();
		HashMap<String, Integer> gramCounts = countFileprocessor.getGramCounts();

		
		for(String word : vocabulary.getAllWords())
			for(String tag : tagTypes) {
				String wordTagLabel = word + "+" + tag;
				int count = 0;
				if( wordTagCounts.containsKey(wordTagLabel) ) 
				{
					count = wordTagCounts.get(wordTagLabel);
				}
				else 
					count = 0;
				int tagCount = gramCounts.get(tag + "++");
				double emission = new Double(count) / new Double(tagCount);
				emissionParameters.put(wordTagLabel, emission);
			}
	}
}
