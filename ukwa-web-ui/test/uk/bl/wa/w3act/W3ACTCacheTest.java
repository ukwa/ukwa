package uk.bl.wa.w3act;

import static org.junit.Assert.*;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;

import org.junit.Test;

public class W3ACTCacheTest {

	@Test
	public void test() {
		running(fakeApplication(inMemoryDatabase("test")), new Runnable() {
			@Override
			public void run() {
				
				W3ACTCache cache = new W3ACTCache();

			}
		});
	}

}
