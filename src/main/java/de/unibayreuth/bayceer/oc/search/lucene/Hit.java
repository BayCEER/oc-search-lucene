package de.unibayreuth.bayceer.oc.search.lucene;


public class Hit {
	public Long id;
	public Float score;
	public String path;
	
	public Hit(long id, float score, String path) {
		this.id = id;
		this.score = score;
		this.path = path;
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

	
}