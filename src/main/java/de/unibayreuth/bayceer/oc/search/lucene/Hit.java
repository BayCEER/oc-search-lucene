package de.unibayreuth.bayceer.oc.search.lucene;


public class Hit {
	public Long id;
	public Float score;
	public String path;
	public String[] previews;
	
	
	public Hit(long id, float score, String path, String[] previews) {
		this.id = id;
		this.score = score;
		this.path = path;
		this.previews = previews;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Float getScore() {
		return score;
	}
	public void setScore(Float score) {
		this.score = score;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String[] getPreviews() {
		return previews;
	}
	public void setPreviews(String[] previews) {
		this.previews = previews;
	}
	

	
}