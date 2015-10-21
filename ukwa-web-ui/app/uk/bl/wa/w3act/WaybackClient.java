/**
 * 
 */
package uk.bl.wa.w3act;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import play.Logger;
import play.Play;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

/**
 * @author andy
 *
 */
public class WaybackClient {

    private final static int timeout = 10*1000;

    private static String prefix = Play.application().configuration().getString("wayback.prefix");

    public static List<Instance> getWaybackInstances(String url) {
	List<Instance> instances = new ArrayList<Instance>();
	Logger.info("Checking for Wayback instances of "+url);
	try {
	    Promise<WSResponse> xml = WS.url(prefix+"/xmlquery.jsp").setQueryParameter("url", url).get();
	    Document doc = xml.get(timeout).asXml();

	    XPathFactory xPathfactory = XPathFactory.newInstance();
	    XPath xpath = xPathfactory.newXPath();
	    XPathExpression expr = xpath.compile("/wayback/results/result");
	    XPathExpression capdateX = xpath.compile("./capturedate/text()");

	    NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

	    for (int i = 0; i < nodes.getLength(); i++) {
		String capturedate = (String) capdateX.evaluate(nodes.item(i), XPathConstants.STRING);
		Instance in = new Instance(capturedate, url);
		instances.add(in);
	    }
	} catch( Exception e ) {
	    Logger.error("Wayback URL lookup error.",e);
	}

	Logger.info("Found "+instances.size()+" instances of "+url);
	return instances;
    }
}
