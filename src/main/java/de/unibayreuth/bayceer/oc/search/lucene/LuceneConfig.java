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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

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
	public IndexWriterConfig indexWriterConfig() {		
		return new IndexWriterConfig(analyzer());					
	}
	
	
	@Bean	
	public String thumbPath() throws IOException  {
		Path p = (!imagePath.isEmpty())?Paths.get(imagePath):FileSystems.getDefault().getPath("oc_images");		
		if (Files.notExists(p)) {
			Files.createDirectories(p);			
		}		
		return p.toString();		 
	}
	
	
	
	@Bean
	@Order(1)
	public Directory directory() throws IOException  {		 		
		if (indexPath.isEmpty()) {			
			return FSDirectory.open(FileSystems.getDefault().getPath("oc_index"));			
		} else {
			return FSDirectory.open(Paths.get(indexPath));	
		} 
	}
		
		
	@Bean
	@Order(2)
	public IndexWriter indexWriter() throws IOException {
		IndexWriter w = new IndexWriter(directory(),indexWriterConfig());
		w.commit();
		return w;					
	}
	
	
	
	
	

}
