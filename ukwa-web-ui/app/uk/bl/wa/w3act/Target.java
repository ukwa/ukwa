/**
 * 
 */
package uk.bl.wa.w3act;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import play.Logger;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author andy
 *
 */
public class Target implements Serializable {

	private static final long serialVersionUID = 5978088401293495160L;
	
	public long id;
	public String title;
	public String language;
	public TargetUrl primaryUrl = null;
	public List<TargetUrl> additionalUrls = new ArrayList<TargetUrl>();
	

	public Target(JsonNode json) {
		this.id = json.get("id").longValue();
		this.title = json.get("title").textValue();
		for( JsonNode url : json.get("fieldUrls")) {
			if( primaryUrl == null ) {
				primaryUrl = new TargetUrl(url.get("url").textValue(), true);
			} else {
				additionalUrls.add(new TargetUrl(url.get("url").textValue(), false));
			}
		}
	}

}
