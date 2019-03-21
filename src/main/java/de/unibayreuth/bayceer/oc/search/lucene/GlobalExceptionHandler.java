package de.unibayreuth.bayceer.oc.search.lucene;

import java.nio.file.NoSuchFileException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ NoSuchFileException.class })
    public final ResponseEntity<String> handleException(Exception ex, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (ex instanceof NoSuchFileException) {
        	NoSuchFileException nsf = (NoSuchFileException) ex;        	
            return new ResponseEntity<String>("No such file:" + nsf.getFile(), headers, HttpStatus.NOT_FOUND);
        } else {
        	return new ResponseEntity<String>("Internal server error", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
}