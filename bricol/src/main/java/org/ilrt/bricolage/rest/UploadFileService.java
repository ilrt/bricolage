package org.ilrt.bricolage.rest;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ilrt.bricolage.data.DataManager;
import org.ilrt.bricolage.data.DataManagerException;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
 
@Path("/file")
public class UploadFileService {
 
	private Log log = LogFactory.getLog(UploadFileService.class);
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(
		@FormDataParam("file") InputStream uploadedInputStream,
		@FormDataParam("file") FormDataContentDisposition fileDetail) {
 
		// save it
		try {
			DataManager.getInstance().upload(fileDetail.getFileName(), uploadedInputStream);
		} catch (DataManagerException e) {
			log.error("Upload error: " + e);
			throw RestUtils.buildException(e);
		}
 
		String output = "OK";
 
		return Response.status(200).entity(output).build();
 
	}
}