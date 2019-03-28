package de.unibayreuth.bayceer.oc.search.lucene;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LuceneConfig {
	
	@Value("${INDEX_PATH:}")
	private String indexPath;
	
	@Value("${IMAGE_PATH:}")
	private String imagePath;

		
	@Bean Analyzer analyzer() {		
		Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();		
		analyzerPerField.put("content", new ReadMeDcAnalyzer());		
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);				 
	}
		
		
	
	@Bean	
	public String imagePath() throws IOException  {		
		Path p;
		if (imagePath.isEmpty()) {
			p = FileSystems.getDefault().getPath("oc_images");
		} else {
			p = Paths.get(imagePath);
		}				
		if (Files.notExists(p)) {
			Files.createDirectories(p);			
		}		
		return p.toString();		 
	}
	
	
	@Bean	
	public String indexPath() throws IOException  {		
		Path p;
		if (indexPath.isEmpty()) {
			p = FileSystems.getDefault().getPath("oc_index");
		} else {
			p = Paths.get(indexPath);
		}				
		if (Files.notExists(p)) {
			Files.createDirectories(p);			
		}		
		return p.toString();		 
	}
	
				
	

}
