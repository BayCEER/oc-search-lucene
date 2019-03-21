package de.unibayreuth.bayceer.oc.search.lucene;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class LocalRequestInterceptor extends HandlerInterceptorAdapter {
	
	private static Logger log = LoggerFactory.getLogger(LocalRequestInterceptor.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
			if (!request.getRemoteAddr().equals(request.getLocalAddr())) {
				log.warn(String.format("Denied access from:%s",request.getRemoteAddr()));
				response.setStatus(401);
				return false;
			} else {
				return true;
			}
	}
}
