package de.unibayreuth.bayceer.oc.search.lucene;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class RequestConfig implements WebMvcConfigurer{
	
	@Value("${REMOTE_REQUEST:false}")
	private Boolean remote_request;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (remote_request == false) {		
			registry.addInterceptor(new LocalRequestInterceptor());
		}
	}

}
