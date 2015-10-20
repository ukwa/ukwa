/**
 * 
 */
package uk.bl.wa.w3act;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.fasterxml.jackson.databind.JsonNode;

import play.Play;
import play.api.PlayConfig;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
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

		final int timeout = 1000*1000;

		running(fakeApplication(inMemoryDatabase("test")), new Runnable() {
			@Override
			public void run() {
				String act_url = Play.application().configuration().getString("w3act.url");
				JsonNode json = Json.newObject()
	                    .put("email",Play.application().configuration().getString("w3act.username"))
	                    .put("password",Play.application().configuration().getString("w3act.password"))
	                    ;
	                    //.put("redirectToUrl", "");
				Promise<WSResponse> login = WS
						.url(act_url + "/login")
						.setContentType("application/x-www-form-urlencoded")
						.setFollowRedirects(false)
						.post(json);
				WSResponse r = login.get(timeout);
				System.out.println("Status: " + r.getStatus()+ " " + r.getStatusText());
				for( String h : r.getAllHeaders().keySet()) {
					System.out.println(h + ": " + r.getHeader(h));
				}
				String cookie = r.getHeader("Set-Cookie");
				System.out.println("Cookie: " + cookie);
				System.out.println(r.getBody());
			}
		});

	}

}
