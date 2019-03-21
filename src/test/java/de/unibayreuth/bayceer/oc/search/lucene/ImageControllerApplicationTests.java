package de.unibayreuth.bayceer.oc.search.lucene;

import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.DEFINED_PORT)
public class ImageControllerApplicationTests {
	
	private RequestSpecification spec;
	
	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();
		
	
	@Before	
	public void setUp() throws IOException{				
		// Set default content type for all requests		
		this.spec = new RequestSpecBuilder()
		        .setContentType(ContentType.JSON)
		        .setAccept(ContentType.JSON)
		        .addFilter(documentationConfiguration(this.restDocumentation))
		        .build();		
		given(spec).delete("/images").then().statusCode(200);
		given(spec).delete("/thumbnails").then().statusCode(200);
				
	}
	
	@Test 
	public void testController() {
		// POST
		given(spec)
			.contentType("image/png")
			.filter(document("thumb-post",
						pathParameters(
							parameterWithName("id").description("File identifier")				
						)
					)
			)
			.body(new File("src/test/resources/thumb.png"))
				.post("/thumbnail/{id}",6)
			.then()
			.statusCode(200);
		
		// GET
		Response r = given(spec)
			.accept("image/png")
			.filter(document("thumb-get",pathParameters(
				parameterWithName("id").description("File identifier")
			)))
		.when()
			.get("/thumbnail/{id}",6)
		.then()
			.assertThat().header("Content-Length",Integer::parseInt, equalTo(4093))
			.statusCode(200)			
		.extract().response();		
		assertEquals(4093, r.body().asByteArray().length);
		
		// DELETE
		given(spec)
			.filter(document("thumb-delete",pathParameters(
				parameterWithName("id").description("File identifier")
			)))
		.when()
			.delete("thumbnail/{id}", 6)
		.then()
			.statusCode(200);
		
		// NOT FOUND 
		given(spec)
			.accept("image/png")
		.when()
			.get("/thumbnail/{id}",6)
		.then()
			.statusCode(404);
		
	}
	
	
	
	

}
