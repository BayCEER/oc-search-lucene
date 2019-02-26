package de.unibayreuth.bayceer.oc.search.lucene;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

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
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.DEFINED_PORT)
public class OcSearchLuceneApplicationTests {
	
	
	private RequestSpecification req;
		
	@Before	
	public void setUp() throws IOException{				
		// Set default content type for all requests		
		this.req = new RequestSpecBuilder()
		        .setContentType(ContentType.JSON)
		        .setAccept(ContentType.JSON)
		        .build();				

		// Init index 
		given(req).delete("/indexes").then().statusCode(200);		
		// Import sample data 		
		given(req).body(new String(Files.readAllBytes(Paths.get("src/test/resources/dc.json")))).post("/indexes").then().statusCode(200);		
		
	}
		
	@Test
	public void queryContent() {
		given(req).param("query", "Maggie").get("/index").then().assertThat().body("hits.size()", is(2))
		.and().body("totalHits", equalTo(2));
	}
	
	@Test 
	public void queryParameter() {
		given(req).param("query", "creator:Maggie")
		.get("/index").then()
		.assertThat().body("hits.size()", is(2))
		.and().body("totalHits", equalTo(2));
	}
	
	@Test 
	public void queryParameterWithPaging() {
		given(req).param("query", "microplastics").param("start", 0).param("hitsPerPage",5)
		.get("/index").then()
		.assertThat().body("hits.size()", is(5)).and().body("totalHits", equalTo(12));
						
		given(req).param("query", "microplastics").param("start", 5).param("hitsPerPage",5)
		.get("/index").then()
		.assertThat().body("hits.size()", is(5)).and().body("totalHits", equalTo(12));
		
		given(req).param("query", "microplastics").param("start", 10).param("hitsPerPage",5)
		.get("/index").then()
		.assertThat().body("hits.size()",is(2)).and().body("totalHits", equalTo(12));
	}
			
	@Test
	public void updateContent() throws IOException {		
		given(req).body(new String(Files.readAllBytes(Paths.get("src/test/resources/dc_update.json")))).put("/index/2").then().statusCode(200);		
		given(req).param("query","id:2").get("/index").then().assertThat().body("hits[0].path",equalTo("SFB/Microplastics/Munich/README.dc"));	
	}
	
	
	@Test
	public void deleteContent() {
		given(req).delete("/index/2").then().statusCode(200);
		given(req).param("query","id:2").get("/index").then().assertThat().body("totalHits",equalTo(0));
		
	}
	
	
	

}
