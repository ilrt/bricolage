package org.ilrt.bricolage.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

public class RestUtils {

	public static WebApplicationException buildException(Throwable e) {
		ResponseBuilderImpl builder = new ResponseBuilderImpl();
		builder.status(Response.Status.BAD_REQUEST);
		builder.entity(e.getLocalizedMessage());
		Response response = builder.build();
		return new WebApplicationException(response);
	}

}
