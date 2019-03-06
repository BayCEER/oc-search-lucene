package de.unibayreuth.bayceer.oc.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharTokenizer;

public class ReadMeDcAnalyzer extends Analyzer {

	public class ColonTokenizer extends CharTokenizer {
		@Override
		protected boolean isTokenChar(int c) {			
			return Character.isLetter(c) && c != ':'; 						
		}
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {		
		Tokenizer tokenizer = new ColonTokenizer();		
		TokenStream filter = new LowerCaseFilter(tokenizer);						
		return new TokenStreamComponents(tokenizer,filter);
	}
	
	

}
