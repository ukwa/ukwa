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

/*
 * 
<wayback>
	<request>
		<startdate>19960101000000</startdate>
		<numreturned>356</numreturned>
		<type>urlquery</type>
		<enddate>20151021220130</enddate>
		<numresults>356</numresults>
		<firstreturned>0</firstreturned>
		<url>bbc.co.uk/news/</url>
		<resultsrequested>10000</resultsrequested>
		<resultstype>resultstypecapture</resultstype>
	</request>
	<results>
		<result>
			<compressedoffset>8584855</compressedoffset>
			<mimetype>text/html</mimetype>
			<file>f6ivc2c93382e#BL-36995091-202.warc.gz</file>
			<redirecturl>http://news.bbc.co.uk/</redirecturl>
			<urlkey>bbc.co.uk/news/</urlkey>
			<digest>sha512:2d00b60b87f2913c589cad026d6767445e42a6b6014a15eb0b593b7f522c8f53bcedb5e41618cf14263bb78e2a7cdb79a2c9254cf1cf73b84629296da5195e10</digest>
			<httpresponsecode>302</httpresponsecode>
			<robotflags>O</robotflags>
			<url>http://www.bbc.co.uk/news/</url>
			<capturedate>20100309122434</capturedate>
		</result>
...
        </results>
</wayback>
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
    
    public static String getPlaybackUrlFor(Instance i) {
	return prefix+"/"+i.waybackTimestamp+"/"+i.url;
    }
}
