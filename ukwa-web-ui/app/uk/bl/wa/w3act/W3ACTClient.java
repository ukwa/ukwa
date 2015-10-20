/**
 * 
 */
package uk.bl.wa.w3act;

import static play.test.Helpers.*;

/**
 * @author andy
 *
 */
public class W3ACTClient {

	/**
	 * @param args
	 */
	public static void main(String[] args)  {

		/*
		 * response = requests.post(, data={"email": config.get('credentials',
		 * 'email'), "password": config.get('credentials','password')}) cookie =
		 * response.history[0].headers["set-cookie"] headers = { "Cookie":
		 * cookie }
		 */

		running(fakeApplication(inMemoryDatabase("test")), new Runnable() {
			@Override
			public void run() {
				
				W3ACTCache cache = new W3ACTCache();

			}
		});

	}
	
	
}
