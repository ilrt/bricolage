package org.ilrt.bricolage.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Person {

	private String name;
	private String uri;
	private Set<String> sameas = new HashSet<String>();

	public Person() {
	}

	public Person(String uri) {
		this.uri = uri;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUri() {
		return uri;
	}
	
	public Set<String> getSameas() {
		return sameas;
	}

	public void addSameAs(String uri) {
		sameas.add(uri);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(uri + "\n " + name + "\n");
		for(String same: sameas) {
			sb.append(" - " + same + "\n");
		}
		return sb.toString();
	}

}
