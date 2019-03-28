package de.unibayreuth.bayceer.oc.search.lucene;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.unibayreuth.bayceer.oc.search.lucene.ImageController.ImageType;

@RestController
public class IndexController {

			
	@Autowired
	ImageController imageController;
		
	@Autowired 
	Analyzer analyzer;
	
	@Autowired 
	String indexPath;
	
	Map<String,IndexWriter> writers = new Hashtable<String,IndexWriter>(5);
			
	private final Logger logger = LoggerFactory.getLogger(this.getClass());	
	private static final String prevViewPreTag = "<mark>"; 
	private static final String prevViewPostTag = "</mark>";	
	private static final int maxHits = 100;
	
	
	public IndexWriter getWriter(String collection) throws IOException {		
		if (writers.containsKey(collection)) {
			return writers.get(collection);
		} else {						
			IndexWriter w = new IndexWriter(FSDirectory.open(Paths.get(indexPath,collection)),new IndexWriterConfig(analyzer));
			w.commit();
			writers.put(collection, w);
			return w;
		}						
	}
	
	
	
		
	@RequestMapping(value = "/index/{collection}", method = RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void create(@PathVariable String collection, @RequestBody DcDocument file, @RequestParam(value="overWrite",defaultValue="true") boolean overWrite) throws IOException {
		logger.debug("create index for file:" + file.getPath());					
		if (overWrite) {
			getWriter(collection).deleteDocuments(new Term("id", Long.toString(file.getId())));			
		}
		getWriter(collection).addDocument(getDocument(file));
		getWriter(collection).commit();
	}
	
	
	@RequestMapping(value = "/index/{collection}/{id}", method = RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void update(@PathVariable String collection, @PathVariable Long id, @RequestBody DcDocument file) throws IOException {
		logger.debug("update index for file:" + file.getPath());
		getWriter(collection).updateDocument(new Term("id", Long.toString(id)), getDocument(file));
		getWriter(collection).commit();
	}
	
	@RequestMapping(value = "/index/{collection}/{id}", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public DcDocument getDocument(@PathVariable String collection, @PathVariable Long id) throws IOException {				
		IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(getWriter(collection)));		
		Query q = new TermQuery(new Term("id", Long.toString(id)));
		ScoreDoc[] hits = searcher.search(q, 1).scoreDocs;					
		if (hits.length == 0) {
			return null;
		} else {
			Document d = searcher.doc(hits[0].doc);	
			DcDocument dc = new DcDocument();
			dc.setId(id);
			dc.setPath(d.get("path"));
			dc.setContent(d.get("content"));
			String lm = d.get("lastModified");			
			dc.setLastModified((lm!=null)?Long.valueOf(lm):null);
			return dc;			
		}							
	}
	
	
	@RequestMapping(path = "/index/{collection}/{id}", method = RequestMethod.DELETE)
	public void delete(@PathVariable String collection, @PathVariable Long id) throws IOException {
		logger.debug("delete index for file:" + id);
		getWriter(collection).deleteDocuments(new Term("id", Long.toString(id)));
		getWriter(collection).commit();
	}
	
			
	@RequestMapping(value = "/index/{collection}", method = RequestMethod.GET,produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Response search(@PathVariable String collection, @RequestParam("query") String queryString,
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
        
                
		IndexReader indexReader = DirectoryReader.open(getWriter(collection));
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
			
			byte[] thumb = null;
			Long id = Long.valueOf(hd.get("id"));
			if (imageController.exits(collection, id, ImageType.THUMBNAIL)) {
				thumb = imageController.getThumb(collection, id);	
			}			
			hits.add(new Hit(Long.parseLong(hd.get("id")), s.score, hd.get("path"),previews,thumb));
		}
		indexReader.close();	
		
		
		return new Response(hits,collector.getTotalHits());
	}
		
	
		
	@RequestMapping(value = "/indexes/{collection}", method = RequestMethod.POST,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void createMany(@PathVariable String collection, @RequestBody List<DcDocument> files) throws IOException {
		for(DcDocument file:files) {
			logger.debug("create index for file:" + file.getPath());
			getWriter(collection).addDocument(getDocument(file));	
		}		
		getWriter(collection).commit();
	}

		
	@RequestMapping(path = "/indexes/{collection}", method = RequestMethod.DELETE)
	public void deleteAll(@PathVariable String collection) throws IOException {
		logger.debug("delete alle indexes");
		getWriter(collection).deleteAll();
		getWriter(collection).commit();
	}

	private Document getDocument(DcDocument file) throws IOException {		
		Document doc = new Document();
		
		// Field search in selected parameters 
		doc.add(new StringField("id", Long.toString(file.getId()), Field.Store.YES));		
		doc.add(new StringField("path", file.getPath(), Field.Store.YES));
		
		if (file.getLastModified() != null) {
			doc.add(new LongPoint("lastModified", file.getLastModified()));	
		}
		
						
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



