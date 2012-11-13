package org.ilrt.bricolage.rest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.ilrt.bricolage.data.DataManager;
import org.ilrt.bricolage.model.Archive;
import org.ilrt.bricolage.model.ArchiveDao;
import org.ilrt.bricolage.model.ModelException;
import org.ilrt.bricolage.model.Person;
import org.ilrt.bricolage.model.PersonDao;
import org.ilrt.bricolage.publish.Publisher;
import org.ilrt.bricolage.publish.PublisherException;
import org.ilrt.bricolage.transform.TransformException;

// Will map the resource to the URL todos
@Path("/archives")
public class ArchivesResource {

	// Allows to insert contextual objects into the class, 
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;


	// Return the list of todos to the user in the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public List<Archive> getArchivesBrowser() {
		List<Archive> colls = new ArrayList<Archive>();
		try {
			colls.addAll( ArchiveDao.getInstance().list() );
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
		return colls; 
	}
	
	// Return the list of todos for applications
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public List<Archive> getArchives() {
		List<Archive> colls = new ArrayList<Archive>();
		try {
			colls.addAll( ArchiveDao.getInstance().list() );
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
		return colls; 
	}
	
	@GET
	@Path("people")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public List<Person> getPeople() {
		List<Person> people = new ArrayList<Person>();
		try {
			people.addAll( PersonDao.getInstance().list() );
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
		return people; 
	}
	
	// retuns the number of todos
	// Use http://localhost:8080/de.vogella.jersey.todo/rest/todos/count
	// to get the total number of records
	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		int count;
		try {
			count = ArchiveDao.getInstance().size();
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
		return String.valueOf(count);
	}
	
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void newArchive(
			@FormParam("id") String id,
			@Context HttpServletResponse servletResponse
	) throws IOException {
		Archive archive = new Archive(id);
//		ArchiveDao.instance.put(archive);
		
		servletResponse.sendRedirect("../create_todo.html");
	}
	
	@POST
	@Path("/transform/{archive}")
	public void transform(@PathParam("archive") String id) {
		try {
			ArchiveDao.getInstance().transform(id);
		} catch (TransformException te) {
			throw RestUtils.buildException(te);
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
	}

	@POST
	@Path("/publish/{archive}")
	public void publish(@PathParam("archive") String id) {
		try {
			ArchiveDao.getInstance().publish(id);
		} catch (PublisherException pe) {
			throw RestUtils.buildException(pe);
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
	}

	@DELETE
	@Path("/publish/{archive}")
	public void unpublish(@PathParam("archive") String id) {
		try {
			ArchiveDao.getInstance().unpublish(id);
		} catch (PublisherException pe) {
			throw RestUtils.buildException(pe);
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
	}

	@GET
	@Path("/ead/{archive}")
	@Produces({"text/plain"})
	public StreamingOutput getEAD(@PathParam("archive") final String id) throws Exception {
	    return new StreamingOutput() {
	        public void write(OutputStream output) throws IOException, WebApplicationException {
	            try {
	        		File f = DataManager.getInstance().getEADFile(id);
	        		if(f != null) {
	        			FileUtils.copyFile(f, output);
	        		}
	            } catch (Exception e) {
	                throw RestUtils.buildException(e);
	            }
	        }
	    };
	}

	@GET
	@Path("/rdf/{archive}")
	@Produces({"text/plain"})
	public StreamingOutput getRDF(@PathParam("archive") final String id) throws Exception {
	    return new StreamingOutput() {
	        public void write(OutputStream output) throws IOException, WebApplicationException {
	            try {
	        		File f = DataManager.getInstance().getRDFFile(id);
	        		if(f != null) {
	        			FileUtils.copyFile(f, output);
	        		}
	            } catch (Exception e) {
	                throw RestUtils.buildException(e);
	            }
	        }
	    };
	}

	@GET
	@Path("/uri/{archive}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getArchiveURI(@PathParam("archive") String id) {
		try {
			return ArchiveDao.getInstance().getURI(id);
		} catch (PublisherException pe) {
			throw RestUtils.buildException(pe);
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
	}

	// Defines that the next path parameter after todos is
	// treated as a parameter and passed to the TodoResourcesRestUtils.
	// Allows to type http://localhost:8080/de.vogella.jersey.todo/rest/todos/1
	// 1 will be treaded as parameter todo and passed to TodoResource
	@Path("{archive}")
	public ArchiveResource getArchive(
			@PathParam("archive") String id) {
		return new ArchiveResource(uriInfo, request, id);
	}
	
}