package de.unibayreuth.bayceer.oc.search.lucene;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.DEFINED_PORT)
public class IndexControllerApplicationTests {
	
	private RequestSpecification spec;
	
	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();
		
	
	private String jsonIn;
		
	@Before	
	public void setUp() throws IOException{				
		// Set default content type for all requests		
		this.spec = new RequestSpecBuilder()
		        .setContentType(ContentType.JSON)
		        .setAccept(ContentType.JSON)
		        .addFilter(documentationConfiguration(this.restDocumentation))
		        .build();				

		// Init index 		
		jsonIn = new String(Files.readAllBytes(Paths.get("src/test/resources/dc.json")));
		
		given(spec).delete("/indexes").then().statusCode(200);		
		// Import sample data 		
		given(spec).body(jsonIn).post("/indexes").then().statusCode(200);		
		
	}
		
	@Test
	public void queryContent() {
		given(spec)
		.filter(
			document("index-get", 
				requestParameters( 
						parameterWithName("query").description("Lucene query string"), 
						parameterWithName("start").description("Start index"),
						parameterWithName("hitsPerPage").description("Number of hit records per page")
				), 
				responseFields( 
						subsectionWithPath("hits").description("An array of hits."),
						fieldWithPath("totalHits").description("The number of all hits found.")
						
				)
			)
		)
		.param("query", "Maggie")
		.param("start", 0)
		.param("hitsPerPage", 10)
		.get("/index").then().assertThat().body("hits.size()", is(2))
		.and().body("totalHits", equalTo(2));
	}
	
	@Test
	public void queryDocument() {
		given(spec)
		.filter(
				document("document-get",
						pathParameters(
								parameterWithName("id").description("File identifier")				
						),
						responseFields( 
								fieldWithPath("id").description("File identifier").type(JsonFieldType.NUMBER),
								fieldWithPath("path").description("ownCloud file path").type(JsonFieldType.STRING),
								fieldWithPath("content").description("File content as string").type(JsonFieldType.STRING),
								fieldWithPath("lastModified").description("Last modification time").type(JsonFieldType.NUMBER).optional()								
						)
						
				)
		)
		.get("index/{id}",10).then().assertThat()
		.body("content",equalTo("title:Secondary microplastics\ncreator:Lisa Simpson\npublisher:University of Calgary\n"));
	}
		
	
	@Test 
	public void queryParameter() {
		given(spec).param("query", "creator:Maggie")
		.get("/index").then()
		.assertThat().body("hits.size()", is(2))
		.and().body("totalHits", equalTo(2));
	}
	
	@Test 
	public void queryParameterWithPaging() {
		given(spec).param("query", "microplastics").param("start", 0).param("hitsPerPage",5)
		.get("/index").then()
		.assertThat().body("hits.size()", is(5)).and().body("totalHits", equalTo(12));
						
		given(spec).param("query", "microplastics").param("start", 5).param("hitsPerPage",5)
		.get("/index").then()
		.assertThat().body("hits.size()", is(5)).and().body("totalHits", equalTo(12));
		
		given(spec).param("query", "microplastics").param("start", 10).param("hitsPerPage",5)
		.get("/index").then()
		.assertThat().body("hits.size()",is(2)).and().body("totalHits", equalTo(12));
	}
			
	@Test
	public void updateContentWithPut() throws IOException {		
		given(spec).body(new String(Files.readAllBytes(Paths.get("src/test/resources/dc_update.json")))).put("/index/2").then().statusCode(200);		
		given(spec).param("query","id:2").get("/index").then().assertThat().body("hits[0].path",equalTo("SFB/Microplastics/Munich/README.dc"));	
	}
		
		
	@Test
	public void deleteContent() {
		given(spec).delete("/index/2").then().statusCode(200);
		given(spec).param("query","id:2").get("/index").then().assertThat().body("totalHits",equalTo(0));
		
	}
	
	
	

}
