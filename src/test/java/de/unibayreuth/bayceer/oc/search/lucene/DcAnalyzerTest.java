package de.unibayreuth.bayceer.oc.search.lucene;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;


public class DcAnalyzerTest {
	
	private static String SAMPLE_TEXT = "creator:Maggie Simpson";
	private static String FIELD_NAME = "content";
	
		
	@Test
	public void useStandardAnalyzer() throws IOException {
	    List<String> result = analyze(SAMPLE_TEXT, new SimpleAnalyzer());
	    assertTrue(result.contains("creator"));
	    assertTrue(result.contains("maggie"));
	    assertTrue(result.contains("simpson"));	    
	}
	
	@Test
	public void useReadmeDcAnalyzer() throws IOException {
	    List<String> result = analyze(SAMPLE_TEXT, new ReadMeDcAnalyzer());
	    assertTrue(result.contains("creator"));
	    assertTrue(result.contains("maggie"));
	    assertTrue(result.contains("simpson"));	    
	}
	
	
	    
	 
	    
	
	public List<String> analyze(String text, Analyzer analyzer) throws IOException{
	    List<String> result = new ArrayList<String>();
	    TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text);
	    CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
	    tokenStream.reset();
	    while(tokenStream.incrementToken()) {
	       result.add(attr.toString());
	    }       
	    return result;
	}

}
