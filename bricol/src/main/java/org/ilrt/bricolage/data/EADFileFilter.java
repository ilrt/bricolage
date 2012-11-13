package org.ilrt.bricolage.data;

import java.io.File;
import java.io.FileFilter;

import org.ilrt.bricolage.Defaults;

public class EADFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return (file != null && file.getName().toLowerCase().endsWith(Defaults.EAD_SUFFIX));
	}

}
