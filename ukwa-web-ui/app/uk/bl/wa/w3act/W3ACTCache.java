package uk.bl.wa.w3act;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.mapdb.DB;
import org.mapdb.HTreeMap;

import play.Logger;
import play.Play;
import play.cache.CacheApi;
import play.libs.Json;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

public class W3ACTCache implements CollectionsDataSource {

    private final static int timeout = 1000 * 1000;

    private static boolean forceExpiration = false;

    private static int maximumCollections = -1;

    private DB db;

    @Inject
    private CacheApi cache;
    private Stats stats;
    private HTreeMap<Long, CollectionTree> collections;

    @Override
    public Stats getStats() {
        return stats;
    }

    private void setStats(Stats stats) {
        this.stats = stats;
    }

    @Override
    public HTreeMap<Long, CollectionTree> getCollections() {
        return collections;
    }

    private void setCollections(HTreeMap<Long, CollectionTree> collections) {
        this.collections = collections;
    }

    @Override
    public Target getTarget(long id){
        return getTargets().get(id);
    }

    public HTreeMap<Long, Target> getTargets() {
        return targets;
    }

    private void setTargets(HTreeMap<Long, Target> targets) {
        this.targets = targets;
    }

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
    }

    public void init(DB db) {
        if(cache != null) {
            Logger.info("GOT: " + cache.get("helo"));
            cache.set("helo", new String("World"));
        }
        this.db = db;
        // Init caches:
        setCollections(db.hashMapCreate(W3ACT_COLLECTIONS).makeOrGet());
        setTargets(db.hashMapCreate(W3ACT_TARGETS).makeOrGet());
    }

    public void checkForUpdate() {
        // Populate as needed:
        if(cacheExpired() || forceExpiration) {
            fillCache();
        }
        else {
            Logger.info("Using cached collections, size: " + getCollections().size());
        }
    }

    /*
     * Check if the cache has expired - this might be 
     * because it's empty, or because it's over a day old.
     */
    private boolean cacheExpired() {
        String cacheDateString = stats.getLastUpdatedString();

        if(cacheDateString == null) {
            Logger.info("No cache date found.");
            return true;
        }

        Date cacheDate;
        try {
            cacheDate = df.parse(cacheDateString);
        }
        catch(ParseException e) {
            Logger.error("Could not parse date string: " + cacheDateString);
            return false;
        }

        // A day ago:
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date ago = cal.getTime();

        // Checking the date;
        Logger.info("Checking if " + cacheDate + " is before " + ago);
        if(cacheDate.before(ago)) {
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
        Logger.info("Logging into " + act_url);
        Logger.info("Logging in as " + act_user + " " + act_pw);
        JsonNode json = Json.newObject()
                .put("email", act_user)
                .put("password", act_pw)
                .put("redirectToUrl", "");
        Promise<WSResponse> login = WS
                .url(act_url + "/login")
                .setContentType("application/x-www-form-urlencoded")
                .setFollowRedirects(false)
                .post(json);
        WSResponse r = login.get(timeout);
        if(r.getStatus() >= 400) {
            Logger.error("Login failed.");
            return;
        }
        Logger.info("Login succeeded.");
        String cookie = r.getHeader("Set-Cookie");

        // Get the collections tree:
        JsonNode jcollections = getJsonFrom(cookie, act_url + "/api/collections");
        CollectionTree tct = new CollectionTree(jcollections);

        // Add all the top-level collections to the cache system:
        int numc = 0;
        for(CollectionTree ct : tct.children) {
            // Add collection details and for the children too:
            addCollectionData(ct, cookie, act_url);

            // Record the top-level collections:
            getCollections().put(ct.id, ct);

            // Stop if we're for stopping:
            numc++;
            if(W3ACTCache.maximumCollections > 0 && numc >= W3ACTCache.maximumCollections) {
                break;
            }
            // Commit after each addition:
            db.commit();
        }


        // Record the caching time and some stats:
        stats = new Stats(
                getNowISO8601(),
                getTargets().size(),
                tct.getAllCollections().size(),
                getCollections().size());
        // And do the final commit:
        db.commit();
        db.compact();
        Logger.info("Data synced and committed.");
    }

    private void addCollectionData(CollectionTree ct, String cookie, String act_url) {
        JsonNode collection = getJsonFrom(cookie, act_url + "/api/collections/" + ct.id);
        ct.addCollectionDetails(collection);
        addTargetsToCollection(act_url, cookie, ct);
        for(CollectionTree ctn : ct.children) {
            addCollectionData(ctn, cookie, act_url);
        }
    }

    protected void addTargetsToCollection(String act_url, String cookie, CollectionTree ct) {
        JsonNode jtargets = getJsonFrom(cookie, act_url + "/api/targets/bycollection/" + ct.id);
        for(JsonNode jtid : jtargets) {
            Long tid = jtid.longValue();
            Target t = null;
            if(!getTargets().containsKey(tid)) {
                JsonNode jtarget = getJsonFrom(cookie, act_url + "/api/targets/" + tid);
                t = new Target(jtarget);
                getTargets().put(t.id, t);
            }
            else {
                Logger.info("Target " + tid + " already cached.");
                t = getTargets().get(tid);
            }
            ct.targets.add(t);
        }
    }


    protected static JsonNode getJsonFrom(String cookie, String url) {
        Logger.info("Getting " + url);
        while(true) {
            try {
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
            catch(Exception e) {
                Logger.error("Exception while talking to W3ACT - sleeping for 15 seconds before retrying.", e);
                try {
                    Thread.sleep(15 * 1000);
                }
                catch(InterruptedException e1) {
                    Logger.error("Thread sleep was interrupted.", e);
                }
            }
        }
    }

}
