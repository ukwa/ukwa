package uk.bl.wa.w3act;

import static org.junit.Assert.*;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import play.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class W3ACTCacheTest {

	//@Test
/*
	public void testCacheLiveW3ACT() {
		running(fakeApplication(inMemoryDatabase("test")), new Runnable() {
			@Override
			public void run() {
				
				W3ACTCache cache = new W3ACTCache();

			}
		});
	}
*/

	@Test
	public void testW3ACTJsonParsing() throws JsonProcessingException, IOException {
	    
	    // Set up a test colln:
	    CollectionTree ct = new CollectionTree();
	    ct.id = 169;
	    
	    // Load collection JSON:
	    ObjectMapper m = new ObjectMapper();
	    JsonNode collectionsJson = m.readTree(new File("test/data/magna-carta-collection.json"));
	    ct.addCollectionDetails(collectionsJson);
	    
	    // Load targets:
	    JsonNode targetsJson = m.readTree(new File("test/data/magna-carta-collection-targets.json"));
	    for( JsonNode targetJson : targetsJson) {
		ct.addTarget(targetJson);
	    }
	    
	    // Prints and tests
	    Long numTargetsTotal = ct.getNumberOfTargets(true);
	    Logger.info("GOT "+numTargetsTotal+" targets.");
	    assertEquals("Wrong number of targets!", new Long(1052),numTargetsTotal);
	    Long numOpenAccessTargetsTotal = ct.getNumberOfOpenAccessTargets(true);
	    Logger.info("GOT "+numOpenAccessTargetsTotal+" OA targets.");
	    assertEquals("Wrong number of OA targets!", new Long(5),numOpenAccessTargetsTotal);
	    
	    // Perform some data tests:
	    Target t = ct.findTarget(19273l);
	    Logger.info("Got "+t);
	    assertEquals("OA license not found!", true, t.isOpenAccess);
	    
	}
}
