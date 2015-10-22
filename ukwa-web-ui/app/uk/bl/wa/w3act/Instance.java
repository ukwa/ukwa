package uk.bl.wa.w3act;

import java.text.ParseException;
import java.util.Date;

import org.archive.util.DateUtils;

import play.Logger;

public class Instance {

    public String waybackTimestamp;
    public String url;
    public Date timestamp;
    
    public Instance( String waybackTimestamp, String url ) {
	this.waybackTimestamp = waybackTimestamp;
	try {
	    this.timestamp = DateUtils.getDate(waybackTimestamp);
	} catch (ParseException e) {
	    Logger.error("Could not parse capturedate "+this.waybackTimestamp);
	    this.timestamp = null;
	}
	this.url = url;
    }
    
    public String getPlaybackUrl() {
	return WaybackClient.getPlaybackUrlFor(this);
    }
}
