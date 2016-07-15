/**
 * 
 */
package uk.bl.wa.w3act;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

/**
 * @author andy
 *
 */
public class Target implements Serializable {

	private static final long serialVersionUID = 5978088401293496160L;

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
	public boolean isOpenAccess = true;


	/*
...
"field_hidden": false,
"field_key_site": false,
"field_wct_id": 34078727,
"field_spt_id": 147114,
"nominating_organisation": 
{

    "id": 1,
    "createdAt": 1358261596000,
    "updatedAt": 1423490802527,
    "url": "act-101",
    "title": "The British Library",
    "field_abbreviation": "BL"

},
...
	 */

	Target() {}

	public Target(JsonNode json) {
		this.id = json.get("id").longValue();
		this.title = json.get("title").textValue();
		this.description = json.get("description").textValue();
		this.language = json.get("language").textValue();
		this.isOpenAccess = false;
		// Is there an active licence?
		JsonNode licensesJson = json.get("licenses");
		for( JsonNode licenseJson : licensesJson) {
		    if( licenseJson.get("id").intValue() > 0 ) {
			this.isOpenAccess = true;
		    }
		}
		// This only really works if the W3ACT permissions process has been used.
		//String licenseStatus = json.get("licenseStatus").textValue();
		
		// Start date:
		this.startDateText = json.get("crawlStartDateISO").textValue();
		if( ! StringUtils.isEmpty(this.startDateText) ) {
		    try {
			this.startDate = df.parse(startDateText);
		    } catch (Exception e) {
			Logger.warn("Could not parse start date string "+this.startDateText+" for Target "+id);
		    }
		}
		
		// End date:
		this.endDateText = json.get("crawlEndDateISO").textValue();
		if( ! StringUtils.isEmpty(this.endDateText) ) {
		    try {
			this.endDate = df.parse(endDateText);
		    } catch (Exception e) {
			Logger.warn("Could not parse end date string "+this.endDateText+" for Target "+id);
		    }
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

	public String getPlaybackUrl() { 
	    return WaybackClient.getPlaybackUrlFor(this);
	}

	@Override
	public String toString() {
	    return "Target [id=" + id + ", title=" + title
		    + ", description=" + description + ", language=" + language
		    + ", startDateText=" + startDateText + ", startDate="
		    + startDate + ", endDateText=" + endDateText + ", endDate="
		    + endDate + ", primaryUrl=" + primaryUrl
		    + ", additionalUrls=" + additionalUrls + ", isOpenAccess="
		    + isOpenAccess + "]";
	}

    public long getId() {
        return id;
    }
}
