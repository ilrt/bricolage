package org.ilrt.bricolage;


public class Defaults {

	public static final String EAD_SUFFIX = ".xml";
	public static final String RDF_SUFFIX = ".rdf";

	// classpath resources
	public static final String EAD_DTD = "ead.dtd";
	public static final String EAD_XSL_DEFAULT = "ead2rdf-nons.xsl";
	public static final String AUTHORITY = "authority.xml";

	public static final String GRAPH_STEM = "http://data.bris.ac.uk/collections/penguin/";
	public static final String GRAPH_SAMEAS = "http://data.bris.ac.uk/collections/penguin/sameas";

	public static final String TMP_FILE = "/tmp/bricol.tmp";
	
	// Must be visible from Bricolage webapp and Elda webapp
	public static final String DATASET_STEM_URI = "http://localhost:3030/data/";
	public static final String DATASET_DATA_SUFFIX = "data";
	public static final String DATASET_QUERY_SUFFIX = "query";
	
	public static final String VIAF_SUGGEST = "http://viaf.org/viaf/AutoSuggest?query=";

}
