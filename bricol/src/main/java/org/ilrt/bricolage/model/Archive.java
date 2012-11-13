package org.ilrt.bricolage.model;

import java.io.File;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Archive {

	private String name;
	private File ead;
	private File rdf;
	private String published;
	private String uri;
	private String publishedMessage;

	public Archive() {
	}

	public Archive(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setEadFile(File f) {
		ead = f;
	}

	public void setRdfFile(File f) {
		rdf = f;
	}

	public void setPublished(String uri) {
		setPublished(uri, "");
	}

	public void setPublished(String uri, String message) {
		published = uri;
		publishedMessage = message;
	}

	public void setLinkedDataURI(String uri) {
		this.uri = uri;
	}

	@XmlElement
	public String getEadFilename() {
		return ead == null? "" : ead.getName();
	}

	@XmlElement
	public Long getEadModified() {
		return (Long) (ead == null? null : ead.lastModified());
	}

	@XmlElement
	public String getRdfFilename() {
		return rdf == null? "" : rdf.getName();
	}

	@XmlElement
	public Long getRdfModified() {
		return (Long) (rdf == null? null : rdf.lastModified());
	}

	@XmlElement
	public String getPublished() {
		return published == null? "" : published;
	}

	@XmlElement
	public String getPublishedMessage() {
		return publishedMessage == null? "" : publishedMessage;
	}

	@XmlElement
	public String getLinkedDataUri() {
		return uri == null? "" : uri;
	}

}
