package org.ilrt.bricolage.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ilrt.bricolage.data.DataManager;
import org.ilrt.bricolage.data.DataManagerException;
import org.ilrt.bricolage.publish.Publisher;
import org.ilrt.bricolage.publish.PublisherException;
import org.ilrt.bricolage.transform.ToRDF;
import org.ilrt.bricolage.transform.TransformException;

public class ArchiveDao {

	private static ArchiveDao instance = null;

	private DataManager dataManager;
	private Publisher publisher;
	private ToRDF toRDF;

	private ArchiveDao() throws DataManagerException, PublisherException, TransformException {
		dataManager = DataManager.getInstance();
		publisher = Publisher.getInstance();
		toRDF = ToRDF.getInstance();
	}
	
	public static ArchiveDao getInstance() throws ModelException {
		if (instance == null) {
			try {
				instance = new ArchiveDao();
			} catch (DataManagerException e) {
				throw new ModelException(e);
			} catch (PublisherException e) {
				throw new ModelException(e);
			} catch (TransformException e) {
				throw new ModelException(e);
			}
		}
		return instance;
	}

	public Archive get(String name) {
		Archive c = new Archive(name);
		c.setEadFile(dataManager.getEADFile(name));
		c.setRdfFile(dataManager.getRDFFile(name));
		try {
			c.setPublished(publisher.getPublished(name));
		} catch (PublisherException e) {
			c.setPublished(e.getLocalizedMessage());
		}
		try {
			c.setLinkedDataURI(publisher.getPublishedURI(name));
		} catch (PublisherException e) {
			c.setLinkedDataURI(e.getLocalizedMessage());
		}
		return c;
	}

	public Collection<Archive> list() {
		Map<String, Archive> archivesMap = new HashMap<String, Archive>();
		dataManager.report(archivesMap);
		publisher.report(archivesMap);
		return archivesMap.values();
	}

	public void remove(String name) {
		try {
			publisher.remove(name);
		} catch (PublisherException e) {
			System.err.println(e.getLocalizedMessage());
		}
		dataManager.remove(name);
	}

	public int size() {
		return list().size();
	}

	public void transform(String name) throws TransformException {
		toRDF.fromEAD(name);
	}

	public void publish(String name) throws PublisherException {
		publisher.publish(name);
	}

	public void unpublish(String name) throws PublisherException {
		publisher.remove(name);
	}

	public String getURI(String name) throws PublisherException {
		return publisher.getPublishedURI(name);
	}

}