package de.unibayreuth.bayceer.oc.search.lucene;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DcParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void parseSimple() throws IOException, DcParserException {
		String content = new String(Files.readAllBytes(Paths.get("src/test/resources/READMEdc1.txt")));		
		List<SimpleEntry<String, String>> dc = DcParser.parse(content);
		assertEquals(12,dc.size());
	}
	
	@Test 
	public void parseInvalid() throws IOException, DcParserException {
		String content = new String(Files.readAllBytes(Paths.get("src/test/resources/READMEdc2.txt")));		
		List<SimpleEntry<String, String>> dc = DcParser.parse(content);
		assertEquals(2, dc.size());
	}

}
