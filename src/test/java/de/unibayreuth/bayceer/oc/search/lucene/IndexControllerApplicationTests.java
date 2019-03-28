package de.unibayreuth.bayceer.oc.search.lucene;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class IndexControllerApplicationTests extends ControllerApplicationTests {
	
	
	FieldDescriptor[] dcDocumentFields = new FieldDescriptor[] {
			fieldWithPath("id").description("File identifier").type(JsonFieldType.NUMBER),
			fieldWithPath("path").description("ownCloud file path").type(JsonFieldType.STRING),
			fieldWithPath("content").description("File content as string").type(JsonFieldType.STRING),
			fieldWithPath("lastModified").description("Last modification time").type(JsonFieldType.NUMBER).optional()			
	};
	
	
		
	@Before	
	public void setUp() throws IOException{
		super.setUp();
		
		given(spec).delete("/indexes/default").then().statusCode(200);
		given(spec).delete("/indexes/private").then().statusCode(200);
				
		// Import dc data 		
		given(spec)
		.filter(
				document("indexes-post",
						pathParameters(
								parameterWithName("collection").description("Collection identifier")				
						),
						requestFields(
								fieldWithPath("[]").description("An array of documents")).andWithPrefix("[].", dcDocumentFields)													
						)
		)
		.body(new String(Files.readAllBytes(Paths.get("src/test/resources/default.json"))))
		.post("/indexes/{collection}","default")
		.then()
		.statusCode(200);				
		
		
		// Import dc2 data 		
		given(spec)		
		.body(new String(Files.readAllBytes(Paths.get("src/test/resources/private.json"))))
		.post("/indexes/{collection}","private")
		.then()
		.statusCode(200);
		
	}
	
		
		
	
	@Test
	public void indexPost() throws IOException {
		given(spec)
		.filter(
			document("index-post",
					pathParameters(
							parameterWithName("collection").description("Collection identifier")				
					),	
					requestParameters( 
							parameterWithName("overWrite").description("Overwrite entry: {true|false}").optional() 							
					), 
					requestFields(dcDocumentFields)																			
			)
		)
		.body(new String(Files.readAllBytes(Paths.get("src/test/resources/default_post.json"))))
		.post("/index/{collection}","default")
		.then()
		.statusCode(200);				
	}
		
	@Test
	public void indexGet() {
		given(spec)
		.filter(
			document("index-get", 
				pathParameters(
						parameterWithName("collection").description("Collection identifier")				
				),	
				requestParameters( 
						parameterWithName("query").description("http://lucene.apache.org/core/7_7_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package.description[Lucene query parser syntax]"), 
						parameterWithName("start").description("Start index"),
						parameterWithName("hitsPerPage").description("Number of hit records per page")
				), 
				responseFields( 
						subsectionWithPath("hits").description("An array of hits"),
						fieldWithPath("hits[].id").description("File identifier").type(JsonFieldType.NUMBER),
						fieldWithPath("hits[].score").description("Match score").type(JsonFieldType.NUMBER),
						fieldWithPath("hits[].path").description("File path").type(JsonFieldType.STRING),
						fieldWithPath("hits[].previews").description("Hit highlighted text fragment").type(JsonFieldType.ARRAY),						
						fieldWithPath("hits[].thumb").description("Thumbnail").type(JsonFieldType.STRING).optional(),						
						fieldWithPath("totalHits").description("The number of all hits found")												
				)
			)
		)
		.param("query", "Maggie")
		.param("start", 0)
		.param("hitsPerPage", 10)
		.get("/index/{collection}","default").then().assertThat().body("hits.size()", is(2))
		.and().body("totalHits", equalTo(2));
	}
	
	@Test
	public void documentGet() {
		given(spec)
		.filter(
				document("document-get",
						pathParameters(
								parameterWithName("collection").description("Collection identifier"),
								parameterWithName("id").description("File identifier")				
						),
						responseFields(dcDocumentFields)						
				)
		)
		.get("index/{collection}/{id}","default",10).then().assertThat()
		.body("content",equalTo("title:Secondary microplastics\ncreator:Lisa Simpson\npublisher:University of Calgary\n"));
	}
		
	
	@Test 
	public void parameterQueryDefaultCollection() {
		given(spec).param("query", "creator:Maggie")
		.get("/index/{collection}","default").then()
		.assertThat().body("hits.size()", is(2))
		.and().body("totalHits", equalTo(2));
	}
	
	@Test 
	public void parameterQuerySecondCollection() {
		given(spec).param("query", "creator:Maggie")
		.get("/index/{collection}","private").then()
		.assertThat().body("hits.size()", is(1))
		.and().body("totalHits", equalTo(1));
	}
	
	@Test 
	public void indexPaging() {
		given(spec).param("query", "microplastics").param("start", 0).param("hitsPerPage",5)
		.get("/index/{collection}","default").then()
		.assertThat().body("hits.size()", is(5)).and().body("totalHits", equalTo(12));
						
		given(spec).param("query", "microplastics").param("start", 5).param("hitsPerPage",5)
		.get("/index/{collection}","default").then()
		.assertThat().body("hits.size()", is(5)).and().body("totalHits", equalTo(12));
		
		given(spec).param("query", "microplastics").param("start", 10).param("hitsPerPage",5)
		.get("/index/{collection}","default").then()
		.assertThat().body("hits.size()",is(2)).and().body("totalHits", equalTo(12));
	}
			
	@Test
	public void indexPut() throws IOException {		
		given(spec)
		.filter(
		document("index-put",
				pathParameters(
						parameterWithName("collection").description("Collection identifier"),
						parameterWithName("id").description("File identifier")				
				),
				requestFields(dcDocumentFields)																			
				)
		)
		.body(new String(Files.readAllBytes(Paths.get("src/test/resources/default_update.json"))))
		.put("/index/{collection}/{id}","default",2)
		.then()
		.statusCode(200);		
		
		given(spec).param("query","id:2").get("/index/{collection}","default").then().assertThat().body("hits[0].path",equalTo("SFB/Microplastics/Munich/README.dc"));	
	}
		
		
	@Test
	public void indexDelete() {
		given(spec)
		.filter(document("index-delete",
				pathParameters(
						parameterWithName("collection").description("Collection identifier"),
						parameterWithName("id").description("File identifier")				
						)
				
				)
		)
		.delete("/index/{collection}/{id}","default",2).then().statusCode(200);
		given(spec).param("query","id:2").get("/index/{collection}","default").then().assertThat().body("totalHits",equalTo(0));
		
	}
	
	
	

}
