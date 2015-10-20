/**
 * 
 */
package uk.bl.wa.w3act;

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author andy
 *
 */
public class Target implements Serializable {

	private static final long serialVersionUID = 5978088401293495160L;
	
	public long id;
	public String title;

	public Target(JsonNode json) {
		this.id = json.get("id").longValue();
		this.title = json.get("title").textValue();
	}

}
