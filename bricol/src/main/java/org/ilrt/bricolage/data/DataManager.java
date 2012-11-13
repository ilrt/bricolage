package org.ilrt.bricolage.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.ilrt.bricolage.Config;
import org.ilrt.bricolage.Defaults;
import org.ilrt.bricolage.model.Archive;

public class DataManager {

	private static DataManager instance = null;

	private File folder;

	private EADFileFilter eadfilter;
	private RDFFileFilter rdffilter;

	private DataManager() throws IOException {
		folder = new File(Config.DATA_FOLDER);
		eadfilter = new EADFileFilter();
		rdffilter = new RDFFileFilter();

		// ensure DTD in right place for transform
		File dtd = new File(Config.DATA_FOLDER + File.separator
				+ Defaults.EAD_DTD);
		if (!dtd.exists()) {
			FileUtils.copyInputStreamToFile(
					this.getClass()
							.getClassLoader()
							.getResourceAsStream(
									Defaults.CLASSPATH_RESOURCES
											+ Defaults.EAD_DTD), new File(
							Config.DATA_FOLDER + File.separator
									+ Defaults.EAD_DTD));
		}
	}

	public File getEADFile(String name) {
		name = toEADFilename(name);
		File f = new File(folder.getAbsolutePath() + File.separator + name);
		if (f.isFile()) {
			return f;
		}
		return null;
	}

	public File getRDFFile(String name) {
		name = toRDFFilename(name);
		File f = new File(folder.getAbsolutePath() + File.separator + name);
		if (f.isFile()) {
			return f;
		}
		return null;
	}

	public File[] listEAD() {
		File[] files = folder.listFiles(eadfilter);
		if (files == null) {
			files = new File[0];
		}
		return files;
	}

	public File[] listRDF() {
		File[] files = folder.listFiles(rdffilter);
		if (files == null) {
			files = new File[0];
		}
		return files;
	}

	public static DataManager getInstance() throws DataManagerException {
		if (instance == null) {
			try {
				instance = new DataManager();
			} catch (IOException e) {
				throw new DataManagerException("Error initing DataManager: " + e.getLocalizedMessage());
			}
		}
		return instance;
	}

	public String upload(String infile) throws IOException {
		File srcFile = new File(infile);
		String filename = srcFile.getName().replaceAll("[\\s\\+]+", "");
		File destFile = new File(Config.DATA_FOLDER + File.separator + filename);
		FileUtils.copyFile(srcFile, destFile);
		return destFile.getPath();
	}

	public void upload(String name, InputStream uploadedInputStream) {
		String filename = name.replaceAll("[\\s\\+]+", "");
		File destFile = new File(Config.DATA_FOLDER + File.separator + filename);

		try {
			OutputStream out = new FileOutputStream(destFile);
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(destFile);
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPath(String name) {
		File f = new File(Config.DATA_FOLDER + File.separator + name + ".rdf");
		return f.getAbsolutePath();
	}

	public void report(Map<String, Archive> archives) {
		for (File f : listEAD()) {
			String name = toName(f);
			if (!archives.containsKey(name)) {
				archives.put(name, new Archive(name));
			}
			archives.get(name).setEadFile(f);
		}
		for (File f : listRDF()) {
			String name = toName(f);
			if (!archives.containsKey(name)) {
				archives.put(name, new Archive(name));
			}
			archives.get(name).setRdfFile(f);
		}
	}

	private String toName(File file) {
		String n = file.getName();
		int suffixIndex = n.lastIndexOf('.');
		if (suffixIndex != -1) {
			n = n.substring(0, suffixIndex);
		}
		return n;
	}

	private String toEADFilename(String name) {
		int suffixIndex = name.lastIndexOf('.');
		if (suffixIndex != -1) {
			name = name.substring(0, suffixIndex);
		}
		return name + ".xml";
	}

	private String toRDFFilename(String name) {
		int suffixIndex = name.lastIndexOf('.');
		if (suffixIndex != -1) {
			name = name.substring(0, suffixIndex);
		}
		return name + ".rdf";
	}

	public void remove(String name) {
		File f = getEADFile(name);
		if (f != null) {
			f.delete();
		}
		f = getRDFFile(name);
		if (f != null) {
			f.delete();
		}
	}
}
