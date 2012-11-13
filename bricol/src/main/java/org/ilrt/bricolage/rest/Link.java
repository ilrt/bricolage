package org.ilrt.bricolage.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.ClientProtocolException;
import org.ilrt.bricolage.link.VIAF;
import org.ilrt.bricolage.model.Archive;
import org.ilrt.bricolage.model.ArchiveDao;
import org.ilrt.bricolage.publish.Publisher;
import org.ilrt.bricolage.publish.PublisherException;

// POJO, no interface no extends

// The class registers its methods for the HTTP GET request using the @GET annotation. 
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML. 

// The browser requests per default the HTML MIME type.

//Sets the path to base URL + /control
@Path("/link")
public class Link {

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
	@GET
	@Path("/viaf-suggest/{terms}")
	public String viafSuggest(@PathParam("terms") final String terms) {
		try {
			return VIAF.getInstance().suggest(terms);
		} catch (ClientProtocolException e) {
			throw RestUtils.buildException(e);
		} catch (IOException e) {
			throw RestUtils.buildException(e);
		}
	}

	@POST
	@Path("/sameas/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void sameAs(@FormParam("source") String source,
			@FormParam("target") String target) {
		String[] targets = target.split("\\n");
		try {
			Publisher.getInstance().addSameAs(source, targets);
		} catch (PublisherException e) {
			throw RestUtils.buildException(e);
		}
	}

	@DELETE
	@Path("/sameas/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void removeSameAs(@FormParam("source") String source,
			@FormParam("target") String target) {
		String[] targets = target.split("\\n");
		try {
			Publisher.getInstance().removeSameAs(source, targets);
		} catch (PublisherException e) {
			throw RestUtils.buildException(e);
		}
	}

}
