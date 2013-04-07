package com.martinwunderlich.nlp.collins.pa1;

import java.util.List;

import com.martinwunderlich.nlp.collins.common.Vocabulary;

public abstract class GeneTagger {

	protected CountFileProcessor myCFP = null;
	protected List<String> tagTypes = null;
	protected Vocabulary vocab = null;

	public GeneTagger() {
		super();
	}

	public abstract void tag(String inFile, String outFile);

}