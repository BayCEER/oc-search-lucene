package de.unibayreuth.bayceer.oc.search.lucene;

import java.util.List;


public class Response {
	public List<Hit> hits;
	public int totalHits;
	
	
	public Response(List<Hit> hits, int totalHits) {
		super();
		this.hits = hits;
		this.totalHits = totalHits;
	}
	public List<Hit> getHits() {
		return hits;
	}
	public void setHits(List<Hit> hits) {
		this.hits = hits;
	}
	public int getTotalHits() {
		return totalHits;
	}
	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	}