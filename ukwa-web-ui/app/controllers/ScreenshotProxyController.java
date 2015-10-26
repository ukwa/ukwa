package controllers;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

public class ScreenshotProxyController extends Controller {
	
	public Result screenshot(String url) throws ClientProtocolException, IOException {
		
		String prefix = Play.application().configuration().getString("screenshotter.prefix");
		
		// Build up the wayback query:
		String urlBuilder = prefix + "/" + url;
		String q = ctx()._requestHeader().rawQueryString();
		if( q != null && q.length() > 0 ) {
			Logger.info("Passing through raw Query String: "+q);
			urlBuilder += "?"+q;
		}
		final String screenshotUrl = urlBuilder;
		Logger.info("Using URL: "+screenshotUrl);

		// Build up URL and copy over query parameters:		
		CloseableHttpClient httpclient = HttpClientBuilder.create()
			    .disableRedirectHandling()
			    .build();
		//
		HttpGet httpGet = new HttpGet(screenshotUrl);
		CloseableHttpResponse response = httpclient.execute(httpGet);
		
		// If this looks like a redirect, return that:
		if ( response.getFirstHeader(LOCATION) != null ) {
			String location = response.getFirstHeader(LOCATION).getValue();
			response.close();
			Logger.info("Got LOCATION: "+location);
			// Issue the redirect directly...
			return redirect(location);
		}
		
		String contentType = response.getFirstHeader(CONTENT_TYPE).getValue();
		Logger.debug("Response content type: " + contentType);
		HttpEntity entity = response.getEntity();
		return status(response.getStatusLine().getStatusCode(), entity.getContent()).as(contentType);
	}
	
	public Result waybackRoot() throws ClientProtocolException, IOException {
		return screenshot("");
	}

}

