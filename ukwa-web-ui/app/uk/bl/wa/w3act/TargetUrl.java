package uk.bl.wa.w3act;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class TargetUrl implements Serializable {

	private static final long serialVersionUID = 120374894976440752L;

	@JsonProperty
	public String url;
	
	public boolean isSeed;
	
	public TargetUrl(String url, boolean isSeed ) {
		this.url = url;
		this.isSeed = isSeed;
	}

	@Override
	public String toString() {
	    return "TargetUrl [url=" + url + ", isSeed=" + isSeed + "]";
	}
	
}
