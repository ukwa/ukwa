/**
 * 
 */
package uk.bl.wa.w3act;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import play.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

/**
 * @author andy
 *
 */
public class Target implements Serializable {

	private static final long serialVersionUID = 5978088401293495160L;

	private ISO8601DateFormat df = new ISO8601DateFormat();

	public long id;
	public String title;
	public String description;
	public String language;
	public String startDateText;
	public Date startDate;
	public String endDateText;
	public Date endDate;
	public TargetUrl primaryUrl = null;
	public List<TargetUrl> additionalUrls = new ArrayList<TargetUrl>();


	public Target(JsonNode json) {
		this.id = json.get("id").longValue();
		this.title = json.get("title").textValue();
		this.description = json.get("description").textValue();
		this.language = json.get("language").textValue();
		
		// Start date:
		this.startDateText = json.get("crawlStartDateISO").textValue();
		try {
			this.startDate = df.parse(startDateText);
		} catch (Exception e) {
			Logger.warn("Could not parse start date string "+this.startDateText+" for Target "+id);
		}
		
		// End date:
		this.endDateText = json.get("crawlEndDateISO").textValue();
		try {
			this.endDate = df.parse(endDateText);
		} catch (Exception e) {
			Logger.warn("Could not parse end date string "+this.endDateText+" for Target "+id);
		}

		// URLs
		for( JsonNode url : json.get("fieldUrls")) {
			if( primaryUrl == null ) {
				primaryUrl = new TargetUrl(url.get("url").textValue(), true);
			} else {
				additionalUrls.add(new TargetUrl(url.get("url").textValue(), false));
			}
		}
	}
	
	public List<Instance> getAllWaybackInstances() {
	    List<Instance> allWaybackInstances = WaybackClient.getWaybackInstances(primaryUrl.url);
	    return allWaybackInstances;
	}
	
	public List<Instance> getInstances() {
	    List<Instance> allWaybackInstances = WaybackClient.getWaybackInstances(primaryUrl.url);
	    int total = allWaybackInstances.size();
	    
	    if( total < 20 ) {
		return allWaybackInstances;
	    }
	    
	    // Otherwise, downsample:
	    List<Instance> sample = new ArrayList<Instance>();
	    for( int i = 0; i < total; i = i + total/20 ) {
		sample.add(allWaybackInstances.get(i));
	    }
	    return sample;
	    
	}

}
