package uk.bl.wa.w3act;

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.TimeUnit;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import play.Logger;
import play.Play;
import play.libs.Json;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

public class W3ACTCache {
	
	private final String tmpDir = "/var/tmp/";// System.getProperty("java.io.tmpdir")
	
	private final static int timeout = 1000*1000;

	private final static long expiration = 24; // hours
	
	private DB db;
	
	private HTreeMap<String, String> stats;
	
	private HTreeMap<Long, CollectionTree> collections;
	
	private HTreeMap<Long, Target> targets;

	private ISO8601DateFormat df = new ISO8601DateFormat();
	
	private final static String W3ACT_COLLECTIONS = "w3act-collections";
	private final static String W3ACT_TARGETS = "w3act-targets";
	private final static String W3ACT_STATS = "w3act-stats";
	private final static String STATS_LAST_UPDATED = "last-updated";
	private final static String TOTAL_TARGETS = "total-targets";
	private final static String TOTAL_COLLECTIONS = "total-collections";
	private final static String TOTAL_TOP_COLLECTIONS = "total-top-collections";
	
	public W3ACTCache() {
		// Set up the DB from a cache file:
		db = DBMaker.fileDB(new File(tmpDir, "ukwa-cache.mapdb"))
		.closeOnJvmShutdown().make();

		// Init caches:
		stats = db.hashMapCreate(W3ACT_STATS).makeOrGet();
		collections = db.hashMapCreate(W3ACT_COLLECTIONS).makeOrGet();
		targets = db.hashMapCreate(W3ACT_TARGETS).makeOrGet();
		// Populate as needed:
		if( cacheExpired() ) {
			fillCache();
		} else { 
			Logger.info("Using cached collections, size: "+ collections.size());
		}
		for( Long id : collections.keySet() ) {
			Logger.info("Collection ID "+collections.get(id).title);
		}
		for( Long id : targets.keySet() ) {
			Logger.info("Target ID "+targets.get(id).title);
		}
		
	}
	
	/*
	 * Check if the cache has expired - this might be 
	 * because it's empty, or because it's over a day old.
	 */
	private boolean cacheExpired() {
		String cacheDateString = stats.get(STATS_LAST_UPDATED);
		
		if( cacheDateString == null ) {
			Logger.info("No cache date found.");
			return true;
		}
		
		Date cacheDate;
		try {
			cacheDate = df.parse(cacheDateString);
		} catch (ParseException e) {
			Logger.error("Could not parse date string: "+cacheDateString);
			return false;
		}

		// A day ago:
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR,-1);
		Date ago = cal.getTime();
	
		// Checking the date;
		Logger.info("Checking if "+cacheDate+" is before "+ago);
		if( cacheDate.before(ago) ) {
			return true;
		}
		Logger.info("Cached but not expired yet.");
		return false;
	}
	
	private String getNowISO8601() {
		return df.format(new Date());
	}
	
	private void fillCache() {
		String act_url = Play.application().configuration().getString("w3act.url");
		String act_user = Play.application().configuration().getString("w3act.username");
		String act_pw = Play.application().configuration().getString("w3act.password");
		Logger.info("Logging into "+act_url);
		Logger.info("Logging in as "+act_user+" "+act_pw);
		JsonNode json = Json.newObject()
                .put("email",act_user)
                .put("password",act_pw)
                .put("redirectToUrl", "");
		Promise<WSResponse> login = WS
				.url(act_url + "/login")
				.setContentType("application/x-www-form-urlencoded")
				.setFollowRedirects(false)
				.post(json);
		WSResponse r = login.get(timeout);
		Logger.info("Status: " + r.getStatus()+ " " + r.getStatusText());
		if( r.getStatus() >= 400 ) {
			Logger.error("Login failed.");
			return;
		}
		String cookie = r.getHeader("Set-Cookie");

		// 
		String url = act_url+"/api/collections";
		JsonNode jcollections = getJsonFrom(cookie,url);
		CollectionTree tct = new CollectionTree(jcollections);
	
		// Fill...
		List<CollectionTree> allCollections = tct.getAllCollections();
		for( CollectionTree ct : allCollections ) {
			JsonNode collection = getJsonFrom(cookie, act_url + "/api/collections/"+ct.id);
			ct.addCollectionDetails(collection);
			collections.put(ct.id, ct);

			JsonNode jtargets = getJsonFrom(cookie, act_url+"/api/targets/bycollection/"+ct.id);
			ct.addTargets(jtargets);
			for( Target t : ct.targets ) {
				targets.put( t.id, t );
			}

		}

		// Record the caching time and some stats:
		stats.put(STATS_LAST_UPDATED, getNowISO8601());
		stats.put(TOTAL_TARGETS, ""+targets.size());
		stats.put(TOTAL_COLLECTIONS, ""+allCollections.size());
		stats.put(TOTAL_TOP_COLLECTIONS, ""+collections.size());
		// And commit:
		db.commit();
	}
	
	
	private static JsonNode getJsonFrom(String cookie, String url) {
		Logger.info("Getting "+url);
		Promise<JsonNode> jsonPromise = WS.url(url).setHeader("Cookie", cookie).get().map(
		        new Function<WSResponse, JsonNode>() {
		            public JsonNode apply(WSResponse response) {
		                JsonNode json = response.asJson();
		                return json;
		            }
		        }
		);
		return jsonPromise.get(timeout);
	}


}
