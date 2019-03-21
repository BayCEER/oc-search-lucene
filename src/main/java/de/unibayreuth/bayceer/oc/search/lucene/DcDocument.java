package de.unibayreuth.bayceer.oc.search.lucene;

public class DcDocument {
	
	public DcDocument() {
	
	}	
		
	private Long id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Long getLastModified() {
		return lastModified;
	}
	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}


	private String path;
	private String content;
	private Long lastModified;

	
}
