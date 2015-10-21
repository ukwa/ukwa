package uk.bl.wa.w3act;

import java.util.Date;

public class Instance {

    public String waybackTimestamp;
    public String url;
    public Date timestamp;
    
    public Instance( String waybackTimestamp, String url ) {
	this.waybackTimestamp = waybackTimestamp;
	this.url = url;
    }
}
