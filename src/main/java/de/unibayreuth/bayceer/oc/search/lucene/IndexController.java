package de.unibayreuth.bayceer.oc.search.lucene;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

	@Autowired
	IndexWriter indexWriter;

	@Autowired
	Analyzer analyzer;
	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final String prevViewPreTag = "<mark>"; 
	private static final String prevViewPostTag = "</mark>";
	
	private static final int maxHits = 100;
	
	
	
	@RequestMapping(value = "/index", method = RequestMethod.POST)
	public void create(@RequestBody DcFile file, @RequestParam(value="overWrite",defaultValue="true") boolean overWrite) throws IOException {
		logger.debug("create index for file:" + file.getPath());					
		if (overWrite) {
			indexWriter.deleteDocuments(new Term("id", Long.toString(file.getId())));			
		}
		indexWriter.addDocument(getDocument(file));
		indexWriter.commit();
	}
	
	
	@RequestMapping(value = "/index/{id}", method = RequestMethod.PUT)
	public void update(@PathVariable Long id, @RequestBody DcFile file) throws IOException {
		logger.debug("update index for file:" + file.getPath());
		indexWriter.updateDocument(new Term("id", Long.toString(id)), getDocument(file));
		indexWriter.commit();
	}
	
	@RequestMapping(path = "/index/{id}", method = RequestMethod.DELETE)
	public void delete(@PathVariable Long id) throws IOException {
		logger.debug("delete index for file:" + id);
		indexWriter.deleteDocuments(new Term("id", Long.toString(id)));
		indexWriter.commit();
	}
	
			
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public Response search(@RequestParam("query") String queryString,
			@RequestParam(value="start",defaultValue="0") int start,
			@RequestParam(value="hitsPerPage", defaultValue="10") int hitsPerPage, @RequestParam(value="preViewSize", defaultValue="10") int preViewSize) throws ParseException, IOException {
		logger.info("searching with query:" + queryString + " start:" + start + " hitsPerPage:" + hitsPerPage);
		
		Query q = new QueryParser("content", analyzer).parse(queryString);

		
		
		// Hit highlighting 
		Formatter formatter = new SimpleHTMLFormatter(prevViewPreTag,prevViewPostTag);			
        QueryScorer scorer = new QueryScorer(q);        
        Highlighter highlighter = new Highlighter(formatter, scorer);                
        Fragmenter fragmenter = new SimpleFragmenter(20);
        highlighter.setTextFragmenter(fragmenter);
        
                
		IndexReader indexReader = DirectoryReader.open(indexWriter);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(maxHits);
		searcher.search(q, collector);		
		List<Hit> hits = new ArrayList<Hit>(hitsPerPage);
		for (ScoreDoc s : collector.topDocs(start, hitsPerPage).scoreDocs) {			
			Document hd = searcher.doc(s.doc);						
			TokenStream stream = TokenSources.getAnyTokenStream(indexReader,s.doc, "content", analyzer);					
			String[] previews = null;			
			try {
			  previews = highlighter.getBestFragments(stream, hd.get("content"), 4);
			} catch (IOException | InvalidTokenOffsetsException e) {
				logger.warn(e.getMessage());
				
			}			
			hits.add(new Hit(Long.parseLong(hd.get("id")), s.score, hd.get("path"),previews));
		}
		indexReader.close();						
		return new Response(hits,collector.getTotalHits());
	}
		
	
		
	@RequestMapping(value = "/indexes", method = RequestMethod.POST)
	public void createMany(@RequestBody List<DcFile> files) throws IOException {
		for(DcFile file:files) {
			logger.debug("create index for file:" + file.getPath());
			indexWriter.addDocument(getDocument(file));	
		}		
		indexWriter.commit();
	}

		
	@RequestMapping(path = "/indexes", method = RequestMethod.DELETE)
	public void deleteAll() throws IOException {
		logger.debug("delete alle indexes");
		indexWriter.deleteAll();
		indexWriter.commit();
	}

	private Document getDocument(DcFile file) throws IOException {		
		Document doc = new Document();
		
		// Field search in selected parameters 
		doc.add(new StringField("id", Long.toString(file.getId()), Field.Store.YES));		
		doc.add(new StringField("path", file.getPath(), Field.Store.YES));
		doc.add(new LongPoint("lastModified", file.getLastModified()));
						
		// Full text and field search in content field
		doc.add(new TextField("content", file.getContent(), Field.Store.YES));						

		// Field based index 		
		try {
			for(SimpleEntry<String, String> e: DcParser.parse(file.getContent())) {
				doc.add(new TextField(e.getKey(), e.getValue(), Field.Store.YES));						
			}
		} catch (DcParserException e) {
			logger.error(e.getMessage());
		}
		return doc;
	}
	
		
	
	

}



