package de.unibayreuth.bayceer.oc.search.lucene;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {
	
	private static final String image_extension = ".png";
	
	public enum ImageType {
		THUMBNAIL("th"), IMAGE("img");		
		public String code;
		private ImageType(String code) {
			this.code = code;
		}		
	}
	
	@Autowired
	String imagePath;
			
	
	@RequestMapping(value = "/thumbnail/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] getThumb(@PathVariable Long id) throws NoSuchFileException, IOException {
			return Files.readAllBytes(getImagePath(id, ImageType.THUMBNAIL));		
	}
	
	public boolean exits(Long id, ImageType imageType) {
	    return getImagePath(id, imageType).toFile().exists();		
	}
	
	@RequestMapping(value = "/thumbnails", method = RequestMethod.DELETE)
	public void deleteImages() {
		deleteFiles(ImageType.THUMBNAIL);
	}
	
	@RequestMapping(value = "/images", method = RequestMethod.DELETE)
	public void deleteThumbs() {
		deleteFiles(ImageType.IMAGE);
	}
	
	private FilenameFilter fileFilter(ImageType imageType) {
		return new FilenameFilter() {			
			String suffix = imageType.code + image_extension;
			@Override
			public boolean accept(File dir, String name) {	
				return name.endsWith(suffix);				
			}
		};
		
	}
	
	private void deleteFiles(ImageType imageType) {
		File d = new File(imagePath);		
		for(File f: d.listFiles(fileFilter(imageType))) {
			f.delete();
		}		
	}
	
	@RequestMapping(value = "/thumbnail/{id}", method = RequestMethod.POST, consumes = MediaType.IMAGE_PNG_VALUE)
	public void createThumb(@PathVariable Long id,@RequestBody byte[] image) throws IOException {		
			Files.write(getImagePath(id, ImageType.THUMBNAIL), image);		
	}
	
	@RequestMapping(value = "/thumbnail/{id}", method = RequestMethod.DELETE)
	public void deleteThumb(@PathVariable Long id) throws IOException {
			Files.deleteIfExists(getImagePath(id, ImageType.THUMBNAIL));		
	}
	
	@RequestMapping(value = "/image/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] getImage(@PathVariable Long id) throws NoSuchFileException, IOException {				
			return Files.readAllBytes(getImagePath(id, ImageType.IMAGE));				 
	}
		
	@RequestMapping(value = "/image/{id}", method = RequestMethod.POST, consumes = MediaType.IMAGE_PNG_VALUE)
	public void createImage(@PathVariable Long id,@RequestBody byte[] image) throws IOException {		
			Files.write(getImagePath(id, ImageType.IMAGE), image);		
	}
	
	@RequestMapping(value = "/image/{id}", method = RequestMethod.DELETE)
	public void deleteImage(@PathVariable Long id) throws IOException {
			Files.deleteIfExists(getImagePath(id, ImageType.IMAGE));		
	}
	
		
	private Path getImagePath(Long id, ImageType imageType) {							
		return Paths.get(imagePath,String.valueOf(id)  + "_" + imageType.code + image_extension);	
	}
	

}
