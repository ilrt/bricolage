/**
 * 
 */
package org.ilrt.bricolage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.ilrt.bricolage.data.DataManager;
import org.ilrt.bricolage.data.DataManagerException;
import org.ilrt.bricolage.link.VIAF;
import org.ilrt.bricolage.model.Archive;
import org.ilrt.bricolage.publish.Publisher;
import org.ilrt.bricolage.publish.PublisherException;
import org.ilrt.bricolage.transform.ToJSON;
import org.ilrt.bricolage.transform.ToRDF;
import org.ilrt.bricolage.transform.TransformException;

/**
 * @author ecjet
 * 
 */
public class BricolCli {

	// create Options object
	static Options options = new Options();

	static String infile = "";
	static String outfile = "";
	static String xslfile = "";

	static String collection = "";

	private static enum Action {
		UPLOAD, DATA_LIST, HELP, PUBLISH, PUBLISH_STATUS, TIMELINE_CONVERT, LIST_PEOPLE, VIAF_SUGGEST
	};

	private static Action action = Action.HELP;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ToRDF toRDF = null;
		try {
			toRDF = ToRDF.getInstance();
		} catch (TransformException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(0);
		}
		ToJSON toJSON = ToJSON.getInstance();
		DataManager dataManager = null;
		try {
			dataManager = DataManager.getInstance();
		} catch (DataManagerException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(0);
		}
		Publisher publisher = null;
		try {
			publisher = Publisher.getInstance();
		} catch (PublisherException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(0);
		}

		parseOptions(args);

		switch (action) {
		case DATA_LIST:
			Map<String, Archive> archives = new HashMap<String, Archive>();
			dataManager.report(archives);
			publisher.report(archives);
			System.out.format("%-50s%-50s%-50s\n", "EAD filename",
					"RDF filename", "Published graph name");
			for (Archive c : archives.values()) {
				System.out.format("%-50s%-50s%-50s\n", c.getEadFilename(),
						c.getRdfFilename(), c.getPublished());
			}
			break;
		case LIST_PEOPLE:
			try {
				for (String person : publisher.listPeople()) {
					System.out.println(publisher.getPerson(person));
				}
			} catch (PublisherException e) {
				System.err.println(e.getLocalizedMessage());
				System.exit(0);
			}
			break;
		case VIAF_SUGGEST:
			try {
				System.out.println(VIAF.getInstance().suggest(argsRemain));
			} catch (ClientProtocolException e) {
				System.err.println(e.getLocalizedMessage());
				System.exit(0);
			} catch (IOException e) {
				System.err.println(e.getLocalizedMessage());
				System.exit(0);
			}
			break;
		case UPLOAD:
			if (infile.trim().length() == 0) {
				System.exit(0);
			}

			if (xslfile.trim().length() == 0) {
				xslfile = null;
			}

			if (outfile.trim().length() == 0) {
				outfile = null;
			}

			// 'upload' infile
			String upfile = "";
			try {
				upfile = dataManager.upload(infile);
			} catch (IOException e) {
				System.err.println(e.getLocalizedMessage());
				System.exit(0);
			}

			// transform
			try {
				if (xslfile == null) {
					toRDF.fromEADPath(upfile);
				} else {
					toRDF.fromEADPath(upfile, outfile, xslfile);
				}
			} catch (TransformException e) {
				System.err.println(e.getLocalizedMessage());
				System.exit(0);
			}
			break;
		case PUBLISH:
			if (collection.trim().length() == 0) {
				System.exit(0);
			}
			try {
				publisher.publish(collection);
			} catch (PublisherException e) {
				System.err.println(e.getLocalizedMessage());
				System.exit(0);
			}
			break;
		case PUBLISH_STATUS:
			if (collection.trim().length() == 0) {
				try {
					for (String l : publisher.listPublished()) {
						System.out.println(l);
					}
				} catch (PublisherException e) {
					System.err.println(e.getLocalizedMessage());
					System.exit(0);
				}
			} else {
				try {
					System.out.println(publisher.isPublished(collection));
				} catch (PublisherException e) {
					System.err.println(e.getLocalizedMessage());
					System.exit(0);
				}
			}
			break;
		case TIMELINE_CONVERT:
			if (infile.trim().length() == 0) {
				System.exit(0);
			}
			try {
				toJSON.fromCSVPath(infile, outfile);
			} catch (TransformException e) {
				System.err.println(e.getLocalizedMessage());
				System.exit(0);
			}
			break;
		default:
			help();
			break;
		}

	}

	static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private static List<String> argsRemain = Collections.emptyList();

	private static String longAsDateString(long epochMs) {
		Date date = new Date(epochMs);
		return sdf.format(date);
	}

	private static void help() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("BricolCli", options);
	}

	@SuppressWarnings("static-access")
	private static void parseOptions(String[] args) {
		// upload
		options.addOption(OptionBuilder.withArgName("u")
				.withDescription("upload and transform EAD XML file")
				.create("u"));
		// infile
		options.addOption(OptionBuilder.withArgName("in").hasArg()
				.withDescription("input EAD XML file").create("in"));
		// xsl
		options.addOption(OptionBuilder.withArgName("xsl").hasArg()
				.withDescription("xsl file").create("xsl"));
		// outfile
		options.addOption(OptionBuilder.withArgName("out").hasArg()
				.withDescription("output file").create("out"));

		// list
		options.addOption(OptionBuilder.withArgName("l")
				.withDescription("list EAD and RDF files").create("l"));

		// publish
		options.addOption(OptionBuilder.withArgName("p")
				.withDescription("publish RDF file").create("p"));
		// infile
		options.addOption(OptionBuilder.withArgName("collection").hasArg()
				.withDescription("Collection name").create("collection"));

		// publish status
		options.addOption(OptionBuilder.withArgName("s")
				.withDescription("publish status").create("s"));

		// convert timeline file
		options.addOption(OptionBuilder.withArgName("t")
				.withDescription("convert timeline csv file to json")
				.create("t"));

		// list people
		options.addOption(OptionBuilder.withArgName("people")
				.withDescription("list foaf:Persons").create("people"));

		// viaf suggest
		options.addOption(OptionBuilder.withArgName("viaf")
				.withDescription("viaf suggest").create("viaf"));

		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("u")) {
				action = Action.UPLOAD;
				if (line.hasOption("in")) {
					infile = line.getOptionValue("in");
				}
				if (line.hasOption("out")) {
					outfile = line.getOptionValue("out");
				}
				if (line.hasOption("xsl")) {
					xslfile = line.getOptionValue("xsl");
				}
			}

			if (line.hasOption("l")) {
				action = Action.DATA_LIST;
			}

			if (line.hasOption("people")) {
				action = Action.LIST_PEOPLE;
			}

			if (line.hasOption("viaf")) {
				action = Action.VIAF_SUGGEST;
				argsRemain  = line.getArgList();
			}

			if (line.hasOption("h")) {
				action = Action.HELP;
			}

			if (line.hasOption("p")) {
				action = Action.PUBLISH;
				if (line.hasOption("collection")) {
					collection = line.getOptionValue("collection");
				}
			}

			if (line.hasOption("s")) {
				action = Action.PUBLISH_STATUS;
				if (line.hasOption("collection")) {
					collection = line.getOptionValue("collection");
				}
			}

			if (line.hasOption("t")) {
				action = Action.TIMELINE_CONVERT;
				if (line.hasOption("in")) {
					infile = line.getOptionValue("in");
				}
				if (line.hasOption("out")) {
					outfile = line.getOptionValue("out");
				}
			}

		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}
	}

}
