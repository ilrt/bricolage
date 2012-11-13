package org.ilrt.bricolage.rest;

import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.ClientProtocolException;
import org.ilrt.bricolage.publish.Publisher;
import org.ilrt.bricolage.publish.PublisherException;

// POJO, no interface no extends

// The class registers its methods for the HTTP GET request using the @GET annotation. 
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML. 

// The browser requests per default the HTML MIME type.

//Sets the path to base URL + /control
@Path("/manager")
public class Manager {

	// This method is called if TEXT_PLAIN is request
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayPlainTextHello() {
		return "Hello Jersey";
	}

	// This method is called if XML is request
	@GET
	@Produces(MediaType.TEXT_XML)
	public String sayXMLHello() {
		return "<?xml version=\"1.0\"?>" + "<hello> Hello Jersey" + "</hello>";
	}

	// This method is called if HTML is request
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello() {
		return "<html> " + "<title>" + "Hello Jersey" + "</title>"
				+ "<body><h1>" + "Hello Jersey" + "</body></h1>" + "</html> ";
	}

	// This method is called if HTML is request
	@DELETE
	@Path("/clear-ld-cache")
	public void clearLinkedDataCache() {
		try {
			Publisher.getInstance().clearCache();
		} catch (ClientProtocolException e) {
			throw RestUtils.buildException(e);
		} catch (IOException e) {
			throw RestUtils.buildException(e);
		} catch (PublisherException e) {
			throw RestUtils.buildException(e);
		}
	}

}
