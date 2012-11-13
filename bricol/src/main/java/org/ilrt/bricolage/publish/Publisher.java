package org.ilrt.bricolage.publish;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jena.fuseki.DatasetAccessor;
import org.apache.jena.fuseki.DatasetAccessorFactory;
import org.ilrt.bricolage.Config;
import org.ilrt.bricolage.Defaults;
import org.ilrt.bricolage.NS;
import org.ilrt.bricolage.data.DataManager;
import org.ilrt.bricolage.data.DataManagerException;
import org.ilrt.bricolage.model.Archive;
import org.ilrt.bricolage.model.Person;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Publisher {

	private Log log = LogFactory.getLog(Publisher.class);

	private static Publisher instance = null;

	private DatasetAccessor dataAccessor;

	private DataManager dataManager;

	private Publisher() throws DataManagerException {
		dataAccessor = DatasetAccessorFactory
				.createHTTP(Defaults.DATASET_STEM_URI
						+ Defaults.DATASET_DATA_SUFFIX);
		dataManager = DataManager.getInstance();
	}

	public List<String> listPublished() throws PublisherException {

		List<String> published = new ArrayList<String>();

		String queryString = "select distinct ?g {graph ?g {?s ?p ?o}}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Defaults.DATASET_STEM_URI + Defaults.DATASET_QUERY_SUFFIX,
				query);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				RDFNode x = soln.get("g"); // Get a result variable by name.
				if (!Defaults.GRAPH_SAMEAS.equals(x.asResource().getURI())) {
					published.add(x.asResource().getURI());
				}
			}
		} catch (Exception e) {
			throw new PublisherException(e.getLocalizedMessage());
		} finally {
			qexec.close();
		}
		return published;
	}

	public List<String> listPeople() throws PublisherException {

		List<String> people = new ArrayList<String>();

		String queryString = "select distinct ?person {?person <"
				+ RDF.getURI() + "type> <" + FOAF.NS + "Person>}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Defaults.DATASET_STEM_URI + Defaults.DATASET_QUERY_SUFFIX,
				query);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				RDFNode x = soln.get("person"); // Get a result variable by
												// name.
				people.add(x.asResource().getURI());
			}
		} catch (Exception e) {
			throw new PublisherException(e.getLocalizedMessage());
		} finally {
			qexec.close();
		}
		return people;
	}

	public void report(Map<String, Archive> archives) {
		try {
			for (String uri : listPublished()) {
				try {
					String name = toName(uri);
					if (!archives.containsKey(name)) {
						archives.put(name, new Archive(name));
					}
					archives.get(name).setPublished(uri);
					archives.get(name).setLinkedDataURI(getPublishedURI(name));
				} catch (PublisherException pe) {
					System.err.println(pe.getLocalizedMessage());
				}
			}
		} catch (PublisherException e) {
			// listPublished failed
			// populate with error message
			for (Archive archive : archives.values()) {
				archive.setPublished(null, e.getLocalizedMessage());
			}
		}
	}

	public boolean isPublished(String name) throws PublisherException {
		return dataAccessor.containsModel(toGraphURI(name));
	}

	public String getPublished(String name) throws PublisherException {
		String graphURI = toGraphURI(name);
		if (dataAccessor.containsModel(graphURI)) {
			return graphURI;
		}
		return null;
	}

	public void publish(String name) throws PublisherException {

		// use the FileManager to find the input file
		InputStream in = FileManager.get().open(dataManager.getPath(name));
		if (in == null) {
			throw new PublisherException("File: " + name + " not found");
		}

		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		try {
			// read the RDF/XML file
			model.read(in, null);
		} catch (JenaException e) {
			throw new PublisherException("Problem parsing RDF: "
					+ e.getLocalizedMessage());
		}

		String g = toGraphURI(name);

		dataAccessor.deleteModel(g); // FIXME subsequent PUTs result in 204 No
										// content errors so we delete each time
		dataAccessor.putModel(g, model);

		if (!isPublished(name)) {
			throw new PublisherException("Publishing failed.");
		}
	}

	public void addSameAs(String source, String[] targets) {
		// get sameas model
		// add triple
		Model sModel = dataAccessor.getModel(Defaults.GRAPH_SAMEAS);
		if (sModel == null) {
			// create an empty model
			sModel = ModelFactory.createDefaultModel();
		}
		Resource s = sModel.createResource(source);
		Property p = sModel
				.createProperty("http://www.w3.org/2002/07/owl#sameAs");
		for (String target : targets) {
			Resource t = sModel.createResource(target);
			Statement stmt = sModel.createStatement(s, p, t);
			sModel.add(stmt);
		}
		dataAccessor.putModel(Defaults.GRAPH_SAMEAS, sModel);
	}

	public void removeSameAs(String source, String[] targets) {
		// get sameas model
		// add triple
		Model sModel = dataAccessor.getModel(Defaults.GRAPH_SAMEAS);
		if (sModel == null) {
			return;
		}
		Resource s = sModel.createResource(source);
		Property p = sModel
				.createProperty("http://www.w3.org/2002/07/owl#sameAs");
		for (String target : targets) {
			Resource t = sModel.createResource(target);
			Statement stmt = sModel.createStatement(s, p, t);
			sModel.remove(stmt);
		}
		dataAccessor.putModel(Defaults.GRAPH_SAMEAS, sModel);
	}

	private String toGraphURI(String name) throws PublisherException {
		try {
			String uri = URLEncoder.encode(name, "UTF-8");
			return Defaults.GRAPH_STEM + uri;
		} catch (UnsupportedEncodingException e) {
			throw new PublisherException(e.getLocalizedMessage());
		}
	}

	private String toName(String graphURI) throws PublisherException {

		try {
			String name = URLDecoder.decode(graphURI, "UTF-8");
			name = name.replace(Defaults.GRAPH_STEM, "");
			return name;
		} catch (UnsupportedEncodingException e) {
			throw new PublisherException(e.getLocalizedMessage());
		}
	}

	public static Publisher getInstance() throws PublisherException {
		if (instance == null) {
			try {
				instance = new Publisher();
			} catch (DataManagerException e) {
				throw new PublisherException(e.getLocalizedMessage());
			}
		}
		return instance;
	}

	public void remove(String name) throws PublisherException {
		String g = toGraphURI(name);
		dataAccessor.deleteModel(g);
	}

	public String getPublishedURI(String name) throws PublisherException {
		String uri = "";
		String g = toGraphURI(name);
		String queryString = "select ?s {graph <" + g + "> {?s <" + NS.LOCAH
				+ "level> <" + NS.FONDS + ">}}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Defaults.DATASET_STEM_URI + Defaults.DATASET_QUERY_SUFFIX,
				query);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				RDFNode x = soln.get("s"); // Get a result variable by name.
				uri = x.asResource().getURI();
			}
		} catch (Exception e) {
			throw new PublisherException(e.getLocalizedMessage());
		} finally {
			qexec.close();
		}
		return uri;
	}

	public void clearCache() throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(Config.ELDA_CONTROL + "clear-cache");

		ResponseHandler<String> handler = new ResponseHandler<String>() {
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
				} else {
					return null;
				}
			}
		};

		httpclient.execute(httpget, handler);
	}

	public Person getPerson(String uri) throws PublisherException {
		Person p = new Person(uri);
		String queryString = "select ?fname ?gname ?label {" + " OPTIONAL { <"
				+ uri + "> <" + FOAF.NS + "familyName> ?fname }"
				+ " OPTIONAL {" + "   <" + uri + "> <" + FOAF.NS
				+ "givenName> ?gname }" + " OPTIONAL {" + "   <" + uri + "> <"
				+ RDFS.label.getURI() + "> ?label }" + "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Defaults.DATASET_STEM_URI + Defaults.DATASET_QUERY_SUFFIX,
				query);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				RDFNode f = soln.get("fname");
				RDFNode g = soln.get("gname");
				RDFNode l = soln.get("label");
				String name = "";
				if (f != null) {
					name = (g != null ? g.asLiteral().getLexicalForm() + " "
							: "") + f.asLiteral().toString();
				} else {
					name = (l != null ? l.asLiteral().getLexicalForm() : "");
				}
				p.setName(name);
			}
		} catch (Exception e) {
			throw new PublisherException(e.getLocalizedMessage());
		} finally {
			qexec.close();
		}

		queryString = "select ?sameas {" + " <" + uri + "> <" + OWL.NS
				+ "sameAs> ?sameas " + "}";
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.sparqlService(Defaults.DATASET_STEM_URI
				+ Defaults.DATASET_QUERY_SUFFIX, query);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				RDFNode s = soln.get("sameas");
				p.addSameAs(s.asResource().getURI());
			}
		} catch (Exception e) {
			throw new PublisherException(e.getLocalizedMessage());
		} finally {
			qexec.close();
		}
		return p;
	}

}
