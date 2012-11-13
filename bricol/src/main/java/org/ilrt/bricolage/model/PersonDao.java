package org.ilrt.bricolage.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ilrt.bricolage.data.DataManagerException;
import org.ilrt.bricolage.publish.Publisher;
import org.ilrt.bricolage.publish.PublisherException;

public class PersonDao {
	private static PersonDao instance = null;

	private Publisher publisher;

	private PersonDao() throws PublisherException {
		publisher = Publisher.getInstance();
	}

	public Person get(String uri) {
		try {
			return publisher.getPerson(uri);
		} catch (PublisherException e) {
		}
		return null;
	}
	
	public static PersonDao getInstance() throws ModelException {
		if (instance == null) {
			try {
				instance = new PersonDao();
			} catch (PublisherException e) {
				throw new ModelException(e);
			}
		}
		return instance;
	}

	public Collection<Person> list() {
		Map<String, Person> peopleMap = new HashMap<String, Person>();
		try {
			for(String uri: publisher.listPeople()) {
				peopleMap.put(uri, get(uri));
			}
		} catch (PublisherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return peopleMap.values();
	}

	public int size() {
		return list().size();
	}

}