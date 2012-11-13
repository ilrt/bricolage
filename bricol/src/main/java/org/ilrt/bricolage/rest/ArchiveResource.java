package org.ilrt.bricolage.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import org.ilrt.bricolage.model.Archive;
import org.ilrt.bricolage.model.ArchiveDao;
import org.ilrt.bricolage.model.ModelException;

public class ArchiveResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	String id;
	public ArchiveResource(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = id;
	}
	
	//Application integration 		
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Archive getArchive() {
		Archive c;
		try {
			c = ArchiveDao.getInstance().get(id);
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
		if(c==null)
			throw new RuntimeException("Get: Archive with " + id +  " not found");
		return c;
	}
	
	// For the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public Archive getArchiveHTML() {
		Archive c;
		try {
			c = ArchiveDao.getInstance().get(id);
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
		if(c==null)
			throw new RuntimeException("Get: Archive with " + id +  " not found");
		return c;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response putArchive(JAXBElement<Archive> c) {
		Archive archive = c.getValue();
		return putAndGetResponse(archive);
	}

	@DELETE
	public void deleteArchive() {
		try {
			ArchiveDao.getInstance().remove(id);
		} catch (ModelException e) {
			throw RestUtils.buildException(e);
		}
//		if(c==null)
//			throw new RuntimeException("Delete: Collection with " + id +  " not found");
	}
	
	private Response putAndGetResponse(Archive c) {
//		Response res;
//		if(CollectionDao.instance.contains(c.getName())) {
//			res = Response.noContent().build();
//		} else {
//			res = Response.created(uriInfo.getAbsolutePath()).build();
//		}
//		CollectionDao.instance.put(c);
//		return res;
		return null;
	}
	
	

}
