package de.unibayreuth.bayceer.oc.search.lucene;

import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.DEFINED_PORT)
public abstract class ControllerApplicationTests {
	
	protected RequestSpecification spec;

	
	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();		
		
	@Before	
	public void setUp() throws IOException{				
		// Set default content type for all requests
		 this.spec = new RequestSpecBuilder()
				 	.setContentType("application/json")
			        .setAccept("application/json")
	                .addFilter(	                		
	                		documentationConfiguration(this.restDocumentation)	                			                			                	
	                ).build();

	}
	
		

}
