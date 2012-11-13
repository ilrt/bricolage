package org.ilrt.bricolage.transform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;

public class ToJSON {

	private static ToJSON instance = null;

	private ToJSON() {
	}

	public void fromCSVPath(String path, String filename)
			throws TransformException {

		List<String[]> csv = Collections.emptyList();
		// read csv file
		try {
			CSVReader reader = new CSVReader(new FileReader(path));
			try {
				csv = reader.readAll();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String json = transform(csv);

		if (filename.trim().length() == 0) {
			System.out.println(json);
		} else {
			try {
				BufferedWriter out = new BufferedWriter(
						new FileWriter(filename));
				try {
					out.write(json);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					// Close the output stream
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String transform(List<String[]> csv) throws TransformException {
		return transform(csv, 1);
	}

	public String transform(List<String[]> csv, int ignoreFirstLines)
			throws TransformException {
		if (ignoreFirstLines < 0) {
			ignoreFirstLines = 0;
		}
		StringBuffer json = new StringBuffer();
		boolean firstLine = true;
		boolean headingLine = true;
		for (String[] line : csv) {
			if (ignoreFirstLines > 0) {
				ignoreFirstLines--;
				continue;
			}
			if (headingLine) {
				json.append("{\n");
				json.append("\"timeline\":");
				json.append("{\n");
				json.append("\"type\":\"default\"");
				json.append(",\n");
				json.append(parseLine(line));
				json.append(",\n");
				json.append("\"date\":");
				json.append("[\n");
				headingLine = false;
			} else {
				if (!firstLine) {
					json.append(",\n");
				}
				firstLine = false;
				json.append("{\n");
				json.append(parseLine(line));
				json.append("\n}");
			}
		}
		json.append("]\n");
		json.append("}\n");
		json.append("}\n");
		return json.toString();
	}

	private String parseLine(String[] line) throws TransformException {
		StringBuffer json = new StringBuffer();
		json.append("\"startDate\":\"" + formatDate(line[0]) + "\"");
		json.append(",\n");
		json.append("\"endDate\":\"" + formatDate(line[1]) + "\"");
		json.append(",\n");
		json.append("\"headline\":\"" + line[2] + "\"");
		json.append(",\n");
		// DM refs
		if (line[3].trim().length() > 0) {
			String refStr = line[3].trim();
			String[] refs = refStr.split("\\s*[,;]\\s*");
			StringBuffer sb = new StringBuffer();
			for (String ref : refs) {
				if (sb.length() > 0) {
					sb.append(";");
				}
				sb.append("gb-3-" + ref.toLowerCase());
			}
			json.append("\"text\":\"<span class='lod' res='"
					+ sb.toString()
					+ "'><img style='border:none;' class='throbber' src='img/throbber.gif'/></span>\"");
		} else {
			json.append("\"text\":\"" + " " + "\"");
		}
		// line[4] is DM title. Unused.
		// image
		if (line[5].trim().length() > 0) {
			json.append(",\n");
			json.append("\"asset\":");
			json.append("{\n");
			json.append("\"media\":\"img/archive/" + line[5] + "\"");
			json.append(",\n");
			json.append("\"credit\":\"copyright UoB\"");
			json.append(",\n");
			json.append("\"caption\":\"" + line[6] + "\"");
			json.append("\n}");
		}
		String jsonStr = json.toString().replaceAll("£", "&pound;");
		return jsonStr;
	}

	private String formatDate(String date) throws TransformException {
		if (date.trim().matches("\\d{4}")) {
			// year
			return date;
		}
		if (date.trim().matches("\\w+\\s+\\d{4}")) {
			// month year
			String[] parts = date.split("\\s+");
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(new SimpleDateFormat("MMM", Locale.ENGLISH)
						.parse(parts[0]));
				int month = cal.get(Calendar.MONTH);
				return parts[1] + "," + (month + 1) + ",1";
			} catch (ParseException e) {
				throw new TransformException(e);
			}
		}
		return date;
	}

	public static ToJSON getInstance() {
		if (instance == null) {
			instance = new ToJSON();
		}
		return instance;
	}

}
