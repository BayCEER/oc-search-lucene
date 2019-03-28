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
	
	
	@Autowired
	String imagePath;
	
	private static final String image_extension = ".png";
	
	public enum ImageType {
		THUMBNAIL("th"), IMAGE("img");		
		public String code;
		private ImageType(String code) {
			this.code = code;
		}		
	}
					
	
	@RequestMapping(value = "/thumbnail/{collection}/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] getThumb(@PathVariable String collection, @PathVariable Long id) throws NoSuchFileException, IOException {
			return Files.readAllBytes(getImagePath(collection,id,ImageType.THUMBNAIL));		
	}
	
	public boolean exits(String collection, Long id, ImageType imageType) {
	    return getImagePath(collection, id, imageType).toFile().exists();		
	}
	
	@RequestMapping(value = "/thumbnails/{collection}", method = RequestMethod.DELETE)
	public void deleteImages(@PathVariable String collection) {
		deleteFiles(collection, ImageType.THUMBNAIL);
	}
	
	@RequestMapping(value = "/images/{collection}", method = RequestMethod.DELETE)
	public void deleteThumbs(@PathVariable String collection) {
		deleteFiles(collection,ImageType.IMAGE);
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
	
	private void deleteFiles(String collection, ImageType imageType) {				
		for(File f: Paths.get(imagePath,collection).toFile().listFiles(fileFilter(imageType))) {
			f.delete();
		}		
	}
	
	@RequestMapping(value = "/thumbnail/{collection}/{id}", method = RequestMethod.POST, consumes = MediaType.IMAGE_PNG_VALUE)
	public void createThumb(@PathVariable String collection, @PathVariable Long id,@RequestBody byte[] image) throws IOException {
			Files.createDirectories(Paths.get(imagePath, collection));	
			Files.write(getImagePath(collection, id, ImageType.THUMBNAIL), image);		
	}
	
	@RequestMapping(value = "/thumbnail/{collection}/{id}", method = RequestMethod.DELETE)
	public void deleteThumb(@PathVariable String collection,@PathVariable Long id) throws IOException {
			Files.deleteIfExists(getImagePath(collection,id,ImageType.THUMBNAIL));		
	}
	
	@RequestMapping(value = "/image/{collection}/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] getImage(@PathVariable String collection, @PathVariable Long id) throws NoSuchFileException, IOException {				
			return Files.readAllBytes(getImagePath(collection, id, ImageType.IMAGE));				 
	}
		
	@RequestMapping(value = "/image/{collection}/{id}", method = RequestMethod.POST, consumes = MediaType.IMAGE_PNG_VALUE)
	public void createImage(@PathVariable String collection, @PathVariable Long id,@RequestBody byte[] image) throws IOException {
			Files.createDirectories(Paths.get(imagePath, collection));
			Files.write(getImagePath(collection,id, ImageType.IMAGE), image);		
	}
	
	@RequestMapping(value = "/image/{collection}/{id}", method = RequestMethod.DELETE)
	public void deleteImage(@PathVariable String collection, @PathVariable Long id) throws IOException {
			Files.deleteIfExists(getImagePath(collection,id, ImageType.IMAGE));		
	}
	
		
	private Path getImagePath(String collection, Long id, ImageType imageType) {	
		return Paths.get(imagePath,collection, String.valueOf(id)  + "_" + imageType.code + image_extension);	
	}
	

}
