package uk.bl.wa.w3act;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.mapdb.DB;
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
	
	private final static int timeout = 1000*1000;

	private static boolean forceExpiration = false;
	
	private static int maximumCollections = -1;
	
	private DB db;
	
	public HTreeMap<String, String> stats;
	
	public HTreeMap<Long, CollectionTree> collections;
	
	public HTreeMap<Long, Target> targets;

	private ISO8601DateFormat df = new ISO8601DateFormat();
	
	private final static String W3ACT_COLLECTIONS = "w3act-collections";
	private final static String W3ACT_TARGETS = "w3act-targets";
	private final static String W3ACT_STATS = "w3act-stats";
	private final static String STATS_LAST_UPDATED = "last-updated";
	private final static String TOTAL_TARGETS = "total-targets";
	private final static String TOTAL_COLLECTIONS = "total-collections";
	private final static String TOTAL_TOP_COLLECTIONS = "total-top-collections";
	
	public W3ACTCache() {
	}
	
	public void init(DB db) {
		this.db = db;
		// Init caches:
		stats = db.hashMapCreate(W3ACT_STATS).makeOrGet();
		collections = db.hashMapCreate(W3ACT_COLLECTIONS).makeOrGet();
		targets = db.hashMapCreate(W3ACT_TARGETS).makeOrGet();
	}
	
	public void checkForUpdate() {
		// Populate as needed:
		if( cacheExpired() || forceExpiration ) {
			fillCache();
		} else { 
			Logger.info("Using cached collections, size: "+ collections.size());
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
		if( r.getStatus() >= 400 ) {
			Logger.error("Login failed.");
			return;
		}
		Logger.info("Login succeeded.");
		String cookie = r.getHeader("Set-Cookie");

		// Get the collections tree:
		JsonNode jcollections = getJsonFrom(cookie, act_url+"/api/collections");
		CollectionTree tct = new CollectionTree(jcollections);
	
		// Get all the collections (inc. details), and the targets.
		List<CollectionTree> allCollections = tct.getAllCollections();
		int numc = 0;
		for( CollectionTree ct : allCollections ) {
			JsonNode collection = getJsonFrom(cookie, act_url + "/api/collections/"+ct.id);
			ct.addCollectionDetails(collection);

			JsonNode jtargets = getJsonFrom(cookie, act_url+"/api/targets/bycollection/"+ct.id);
			ct.addTargets(jtargets);
			for( Target t : ct.targets ) {
				targets.put( t.id, t );
			}
			
			numc++;
			if( W3ACTCache.maximumCollections > 0 && numc > W3ACTCache.maximumCollections ) { 
				break;
			}
		}
		
		for( CollectionTree ct : tct.children ) {
			// Record the top-level collections:
			collections.put(ct.id, ct);
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
		Promise<JsonNode> jsonPromise = WS.url(url).setRequestTimeout(timeout).setHeader("Cookie", cookie).get().map(
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
