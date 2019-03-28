package de.unibayreuth.bayceer.oc.search.lucene;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.restassured.response.Response;



public class ImageControllerApplicationTests extends ControllerApplicationTests {
	
	
	
	@Before	
	public void setUp() throws IOException{
		super.setUp();						
	}
	
	@After
	public void shutDown() throws IOException {
		given(spec)
		.filter(document("images-delete",
				pathParameters(
						parameterWithName("collection").description("Collection identifier")				
					)
				)
		)
		.delete("/images/{collection}","default").then().statusCode(200);
		
		given(spec)
		.filter(document("thumbs-delete",
				pathParameters(
						parameterWithName("collection").description("Collection identifier")				
					)
				)
		)
		.delete("/thumbnails/{collection}","default").then().statusCode(200);
	}
	
	@Test 
	public void thumbPost() {
		// POST
		given(spec)
			.contentType("image/png")
			.filter(document("thumb-post",
						pathParameters(
							parameterWithName("collection").description("Collection identifier"),
							parameterWithName("id").description("File identifier")				
						)
						
					)
			)
			.body(new File("src/test/resources/thumb.png"))
			.post("/thumbnail/{collection}/{id}","default",6)
			.then()
			.statusCode(200);
		
		// GET
		Response r = given(spec)
			.accept("image/png")
			.filter(document("thumb-get",pathParameters(
				parameterWithName("collection").description("Collection identifier"),
				parameterWithName("id").description("File identifier")
			)))
		.when()
			.get("/thumbnail/{collection}/{id}","default",6)
		.then()
			.assertThat().header("Content-Length",Integer::parseInt, equalTo(4093))
			.statusCode(200)			
		.extract().response();		
		assertEquals(4093, r.body().asByteArray().length);
		
		// DELETE
		given(spec)
			.filter(document("thumb-delete",pathParameters(
				parameterWithName("collection").description("Collection identifier"),
				parameterWithName("id").description("File identifier")
			)))
		.when()
			.delete("/thumbnail/{collection}/{id}","default", 6)
		.then()
			.statusCode(200);
		
		// NOT FOUND 
		given(spec)
			.accept("image/png")
		.when()
			.get("/thumbnail/{collection}/{id}","default", 6)
		.then()
			.statusCode(404);
		
	}
	
	
	@Test 
	public void imagePost() {
		// POST
		given(spec)
			.contentType("image/png")
			.filter(document("image-post",
						pathParameters(
							parameterWithName("collection").description("Collection identifier"),								
							parameterWithName("id").description("File identifier")				
						)
						
					)
			)
			.body(new File("src/test/resources/image.png"))
			.post("/image/{collection}/{id}","default",10)
			.then()
			.statusCode(200);
		
		// GET
		Response r = given(spec)
			.accept("image/png")
			.filter(document("image-get",pathParameters(
				parameterWithName("collection").description("Collection identifier"),					
				parameterWithName("id").description("File identifier")
			)))
		.when()
			.get("/image/{collection}/{id}","default",10)
		.then()
			.assertThat().header("Content-Length",Integer::parseInt, equalTo(13372342))
			.statusCode(200)			
		.extract().response();		
		assertEquals(13372342, r.body().asByteArray().length);
		
		// DELETE
		given(spec)
			.filter(document("image-delete",pathParameters(
				parameterWithName("collection").description("Collection identifier"),
				parameterWithName("id").description("File identifier")
			)))
		.when()
			.delete("image/{collection}/{id}","default",10)
		.then()
			.statusCode(200);
		
		// NOT FOUND 
		given(spec)
			.accept("image/png")
		.when()
			.get("/image/{collection}/{id}","default",10)
		.then()
			.statusCode(404);
		
	}
	
	
	
	
	

}
