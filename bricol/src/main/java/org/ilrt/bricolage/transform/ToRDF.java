package org.ilrt.bricolage.transform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ilrt.bricolage.Config;
import org.ilrt.bricolage.Defaults;
import org.ilrt.bricolage.data.DataManager;
import org.ilrt.bricolage.data.DataManagerException;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class ToRDF {

	private static ToRDF instance = null;

	private DataManager dataManager;

	private ToRDF() throws DataManagerException {
		dataManager = DataManager.getInstance();
	}

	public void fromEAD(String name) throws TransformException {
		File f = dataManager.getEADFile(name);
		if (f != null) {
			fromEADPath(f.getAbsolutePath(), null);
		} else {
			throw new TransformException("No EAD file found");
		}
	}

	public void fromEADPath(String sourceID) throws TransformException {
		fromEADPath(sourceID, null);
	}

	public void fromEADPath(String sourceID, String filename)
			throws TransformException {

		// load the default xslt from the classpath
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		URL fileURL = classLoader.getResource(Defaults.EAD_XSL_DEFAULT);
		String systemID = fileURL.toExternalForm();

		fromEADPath(sourceID, filename, systemID);
	}

	public void fromEADPath(String sourceID, String filename, String xslt)
			throws TransformException {
		transform(sourceID, xslt);

		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
		InputStream in = FileManager.get().open(Defaults.TMP_FILE);
		if (in == null) {
			throw new TransformException("File: " + Defaults.TMP_FILE
					+ " not found");
		}

		// read the RDF/XML file
		model.read(in, null);

		if (filename == null) {

			int sepIndex = sourceID.lastIndexOf(File.separator);
			if (sepIndex != -1) {
				filename = sourceID.substring(sepIndex);
				int extIndex = filename.lastIndexOf('.');
				if (extIndex != -1) {
					filename = filename.substring(0, extIndex);
				}
			}

			if (filename.trim().length() == 0) {
				throw new TransformException(
						"Could not calculate target filename from " + sourceID);
			} else {
				filename = filename + ".rdf";
			}
		}

		if (filename.trim().length() == 0) {
			// write it to standard out
			model.write(System.out);
		} else {
			try {
				// Create file
				File folder = new File(Config.DATA_FOLDER);
				if (!folder.isDirectory()) {
					if (!(new File(Config.DATA_FOLDER)).mkdirs()) {
						throw new TransformerException(
								"Error creating data folder: "
										+ Config.DATA_FOLDER);
					}
				}
				FileWriter fwriter = new FileWriter(Config.DATA_FOLDER + "/"
						+ filename);
				BufferedWriter out = new BufferedWriter(fwriter);
				model.write(out);
				// Close the output stream
				out.close();
			} catch (Exception e) {// Catch exception if any
				throw new TransformException("File writing error: "
						+ e.getMessage());
			}
		}
	}

	public void transform(String sourceID, String xslID)
			throws TransformException {

		try {
			// Create a transform factory instance.
			TransformerFactory tfactory = TransformerFactory.newInstance();

			// Create a transformer for the stylesheet.
			// Sets the systemID which allows for resolution of relative imports
			Source source = new StreamSource(xslID);
			source.setSystemId(xslID);
			Transformer transformer = tfactory.newTransformer(source);

			transformer.setParameter("authfile", Defaults.AUTHORITY);
			transformer.setParameter("root", Config.URI_STEM);

			// Transform the source XML to System.out.
			transformer.transform(new StreamSource(sourceID), new StreamResult(
					new File(Defaults.TMP_FILE)));
		} catch (Exception ex) {

			Throwable e = ex;

			if (ex instanceof TransformerConfigurationException) {
				Throwable ex1 = ((TransformerConfigurationException) ex)
						.getException();
				if (ex1 != null) {
					e = ex1;
					if (ex1 instanceof SAXException) {
						e = ((SAXException) ex1).getException();
					}
				}
			}
			throw new TransformException(e);
		}

	}

	public static ToRDF getInstance() throws TransformException {
		if (instance == null) {
			try {
				instance = new ToRDF();
			} catch (DataManagerException e) {
				throw new TransformException(e);
			}
		}
		return instance;
	}

}
