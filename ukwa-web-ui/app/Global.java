import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import akka.actor.Cancellable;
import play.*;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import uk.bl.wa.w3act.SolrCollectionsDataSource;


public class Global extends GlobalSettings {

    private static final String SOLR_DEFAULT_URL = "http://localhost:8983/solr/collections";
    private final String defaultTmpDir = System.getProperty("java.io.tmpdir");

    private Cancellable cacheChecker;

    // Set up the DB from a cache file:
    public static DB db;

    @Override
    public void onStart(Application app) {
        Logger.info("Application startup...");

        /*String tmpDir = Play.application().configuration().getString("w3act.cache.tmp.dir", defaultTmpDir);
        // Set up the cache DB:
        db = DBMaker.fileDB(new File(tmpDir, "ukwa-cache.mapdb"))
                .closeOnJvmShutdown()
                .checksumEnable()
                .make();
        // Initialise the W3ACT caches:
        controllers.Application.collectionsDataSource.init(db);

        // Check for updates periodically
        cacheChecker = Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                Duration.create(1, TimeUnit.MINUTES),
                new Runnable() {
                    @Override
                    public void run() {
                        controllers.Application.w3act.checkForUpdate();
                    }
                },
                Akka.system().dispatcher()
        );*/

        SolrCollectionsDataSource collectionsDataSource = (SolrCollectionsDataSource)controllers.Application.collectionsDataSource;
        collectionsDataSource.init(new HttpSolrClient.Builder(Play.application().configuration().getString("solr.url", SOLR_DEFAULT_URL)).build());
    }

    @Override
    public void onStop(Application app) {
        Logger.info("Application shutdown...");
        if(cacheChecker != null && !cacheChecker.isCancelled()) {
            cacheChecker.cancel();
        }
        if(!db.isClosed()) {
            db.commit();
            db.close();
            try {
                Thread.sleep(1000 * 5);
            }
            catch(InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
