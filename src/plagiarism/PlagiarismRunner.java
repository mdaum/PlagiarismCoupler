package plagiarism;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import plag.parser.plaggie.Plaggie;

public class PlagiarismRunner {
	private static final String EXCLUDE_FILES = "excludeFiles";
	private static final String PLAGGIE_MINIMUM_FILE_SIMILARITY_VALUE_TO_REPORT = "plaggie.minimumFileSimilarityValueToReport";
	private static final String INPUT_FILE_FOLDER_NAME = "inputFileFolderName";
	public static Properties prop = new Properties();
	public static Properties plag_prop = new Properties();

	// config file locations
	public static final String PLAGIARISM_CONFIG_FILE = "config/plagiarism_config.properties";
	public static final String PLAGIARISM_COURSE_CONFIG_FILE = "config/plagiarism_course.properties";
	public static final String PLAGGIE_CONFIG_FILE = "config/plaggie.properties";

	static InputStream plag_in = null;
	static InputStream in = null;
	static Collection<String> exclude = new HashSet();

	static int numComparison = 0;
	
	public static void main(String[] args) throws Exception {
		runPlagiarismDetector();
		System.exit(0);
	}
	
	public static void runPlagiarismDetector() {
		try {
			System.out.println("Welcome to Plagiarism Coupler.");
			ReadProperties();
			processAfterProperties();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void processAfterProperties() {
		try {
			initDerivedFileNames();
			// determine which version to run based on OS
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("window")) {
				SanitizeSpace_Windows();
				RunJplag_Windows(true);
				RunMoss_Windows(true);
				System.out.println("Finished");

			} else if (os.contains("mac"))
				System.out.println("We currently do not support mac. Sorry. Please try on Windows or Linux");
			else if (os.contains("linux")) {
				SanitizeSpace_Linux();
				RunJplag_Linux(true);
				RunMoss_Linux(true);
				System.out.println("Finished");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static String jplagResultsFolderName = null;

	private static void SanitizeSpace_Windows() throws Exception {
		System.out.println("Sanitizing Space...");
		File outputfolder = new File(outputResultsFolderName);
		if (outputfolder.exists() && outputfolder.isDirectory()) { // only wait for folder deletion if it exists
			new ProcessBuilder("rm", "-r", outputResultsFolderName).start();
			Thread.sleep(10000); // let the results folder to finish deleting
		}
		new File(outputResultsFolderName).mkdir();
	}

	private static void SanitizeSpace_Linux() throws Exception {
		System.out.println("Sanitizing Space...");
		Process rm = Runtime.getRuntime().exec(new String[] { "sanitizeSpace.sh", outputResultsFolderName });
		Thread.sleep(300);
		System.out.println("Sanitized Space.");
	}

	public static void ReadProperties() {
		try {
			plag_prop.load(new FileInputStream(PLAGGIE_CONFIG_FILE));
		} catch (Exception e) {
			System.out.println("Can't find plaggie properties file " + PLAGGIE_CONFIG_FILE + ", or it is malformed");
		}
		ReadProperties(PLAGIARISM_CONFIG_FILE);
		ReadProperties(PLAGIARISM_COURSE_CONFIG_FILE);

	}

	static protected String outputResultsFolderName = ".";

	private static void ReadProperties(String aFileName) {
		System.out.println("Reading: " + aFileName + " ...");
		try {
			in = new FileInputStream(aFileName);
			prop.load(in);
			String anOutputResultsFolderName = prop.getProperty("outputResultsFolderName");
			if (anOutputResultsFolderName != null) {
				outputResultsFolderName = anOutputResultsFolderName;
			}
			Scanner s = new Scanner(new File(prop.getProperty("excludeName"))); // create list of filters
			while (s.hasNextLine()) {
				exclude.add(s.nextLine());
			}
			s.close();
		} catch (Exception e) {
			System.out.println("Can't find config properties file " + aFileName + ", or it is malformed");
		}

	}

	static String comparisonFile = "";

	public static void RunJplag_Windows(boolean verbose) throws Exception {
		System.out.println("RUNNING JPLAG ON " + prop.getProperty(INPUT_FILE_FOLDER_NAME)
				+ "...\n--------------------------------");
		ArrayList<String> args = new ArrayList<String>();
		args.add("java");
		args.add("-jar");
		args.add("jplag-2.11.8-SNAPSHOT-jar-with-dependencies.jar");
		args.add("-l");
		args.add("java17");
		args.add("-s");
		args.add("-r");
		args.add(jplagResultsFolderName);
		args.add("-m");
		args.add((int) (Float.parseFloat(prop.getProperty(PLAGGIE_MINIMUM_FILE_SIMILARITY_VALUE_TO_REPORT)) * 100)
				+ "%");
		if (Boolean.parseBoolean(prop.getProperty(EXCLUDE_FILES))) { // file exclusion
			args.add("-x");
			args.add(prop.getProperty("excludeName"));
		}
		args.add(prop.getProperty(INPUT_FILE_FOLDER_NAME));
		String[] toPass = new String[args.size()];
		ProcessBuilder builder = new ProcessBuilder(args.toArray(toPass));
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		File comparisons = new File(comparisonFile);
		comparisons.createNewFile();
		BufferedWriter w = new BufferedWriter(new FileWriter(comparisons));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			if (line.startsWith("Comparing")) {
				w.write(line + "\n");
				w.flush();
				numComparison++;
			}
			System.out.println(line);
		}
		System.out.println("Comparisons: " + numComparison);
		r.close();
		w.close();
	}

	public static void RunJplag_Linux(boolean verbose) throws Exception {
		System.out.println("RUNNING JPLAG ON " + prop.getProperty(INPUT_FILE_FOLDER_NAME)
				+ "...\n--------------------------------");
		Process rm = Runtime.getRuntime().exec(new String[] { "runJplag.sh",
				((int) (Float.parseFloat(prop.getProperty(PLAGGIE_MINIMUM_FILE_SIMILARITY_VALUE_TO_REPORT)) * 100)
						+ "%"),
				(Boolean.parseBoolean(prop.getProperty(EXCLUDE_FILES))) ? "-x " + prop.getProperty("excludeName") : "",
				prop.getProperty(INPUT_FILE_FOLDER_NAME), jplagResultsFolderName }); // added
		BufferedReader r = new BufferedReader(new InputStreamReader(rm.getInputStream()));
		File comparisons = new File(comparisonFile);
		comparisons.createNewFile();
		BufferedWriter w = new BufferedWriter(new FileWriter(comparisons));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			if (line.startsWith("Comparing")) {
				w.write(line + "\n");
				w.flush();
				numComparison++;
			}
			System.out.println(line);
		}
		System.out.println("Comparisons: " + numComparison);
		r.close();
		w.close();
	}

	static String mossCommandFile;
	static String allJavaPathsFile;
	static String[] mossArgs = {};

	public static void RunMoss_Windows(boolean verbose) throws Exception {
		System.out.println("RUNNING MOSS ON " + prop.getProperty(INPUT_FILE_FOLDER_NAME)
				+ "...\n--------------------------------");
		ArrayList<String> args = new ArrayList<String>();
		args.add("perl");
		args.add("moss");
		args.add("-l");
		args.add("java");
		args.add("-n");
		args.add("" + 1000); // user must have perl installed and on path
		// now to grab list of all java files in desired folder
		ArrayList<Integer> deep = new ArrayList<Integer>();
		new ProcessBuilder(new String[] { "cmd.exe", "/c", "find", prop.getProperty(INPUT_FILE_FOLDER_NAME), "|",
				"grep", ".java", ">>", allJavaPathsFile }).start();
		Thread.sleep(15000);
		BufferedReader getPaths = new BufferedReader(new FileReader(new File(allJavaPathsFile)));
		int count = 0;
		while (true) {
			count++;
			String line = getPaths.readLine();
			if (line == null) {
				break;
			}
			if (line.contains("MACOSX"))
				continue;
			String[] split = line.split("\\\\");
			for (int i = 0; i < split.length; i++) {
				if (split[i].contains(".java")) {
					if (!deep.contains(i)) {
						deep.add(i);
						// System.out.println("found new one: "+line); //used for debugging
						break;
					}
				}
			}
		}
		System.out.println("found java files #:" + count);
		getPaths.close();
		for (Integer integer : deep) {
			String toAdd = prop.getProperty(INPUT_FILE_FOLDER_NAME) + "/";
			for (int i = 0; i < integer - 1; i++) {
				toAdd += "*/";
			}
			toAdd += "*.java";
			args.add(toAdd);
		}
		mossArgs = args.toArray(mossArgs);
		String op = args.toString().replace(",", "").replace("[", "").replace("]", "");
		System.out.println("Please paste the following op in a nixy terminal to run moss on your folder: " + op);
		BufferedReader r = new BufferedReader(new InputStreamReader(
				new ProcessBuilder(new String[] { "cmd.exe", "/c", "echo", op, ">", mossCommandFile }).start()
						.getInputStream()));
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			if (verbose)
				System.out.println(line);
		}
		r.close();
	}

	public static void RunMoss_Linux(boolean b) throws IOException, InterruptedException {
		System.out.println("RUNNING MOSS ON " + prop.getProperty(INPUT_FILE_FOLDER_NAME)
				+ "...\n--------------------------------");
		ArrayList<String> args = new ArrayList<String>();
		args.add("perl");
		args.add("moss");
		args.add("-l");
		args.add("java");
		args.add("-n");
		args.add("" + 1000); // user must have perl installed and on path
		// now to grab list of all java files in desired folder
		ArrayList<Integer> deep = new ArrayList<Integer>();
		File out = new File(allJavaPathsFile);
		out.createNewFile();
		Process getPaths = Runtime.getRuntime()
				.exec(new String[] { "getPaths.sh", prop.getProperty(INPUT_FILE_FOLDER_NAME), allJavaPathsFile });
		Thread.sleep(5000);
		BufferedReader r = new BufferedReader(new FileReader(out));
		String line;
		int count = 0;
		while (true) {
			count++;
			line = r.readLine();
			if (line == null) {
				break;
			}
			if (line.contains("MACOSX"))
				continue;
			String[] split = line.split("/");
			for (int i = 0; i < split.length; i++) {
				if (split[i].contains(".java")) {
					if (!deep.contains(i)) {
						deep.add(i);
						System.out.println("found new one: " + line);
						break;
					}
				}
			}
		}
		System.out.println(count);
		r.close();
		for (Integer integer : deep) {
			String toAdd = prop.getProperty(INPUT_FILE_FOLDER_NAME) + "/";
			for (int i = 0; i < integer - 1; i++) {
				toAdd += "*/";
			}
			toAdd += "*.java";
			args.add(toAdd);
		}
		String op = args.toString().replace(",", "").replace("[", "").replace("]", "");
		System.out.println("Please paste the following op in a nixy terminal to run moss on your folder: " + op);
		Process writeMossCommand = Runtime.getRuntime()
				.exec(new String[] { "writeMossCommand.sh", op, mossCommandFile });
	}

	public static void RunPlaggie_Windows(boolean verbose) throws Exception {
		System.out.println("RUNNING PLAGGIE ON " + prop.getProperty(INPUT_FILE_FOLDER_NAME)
				+ "...\n--------------------------------");
		// now overwrite plaggie properties with applicable config.properties
		// values
		if (plag_prop.setProperty("plag.parser.plaggie.minimumMatchLength",
				prop.getProperty("plaggie.minimumMatchLength")) == null)
			throw new Exception();
		if (plag_prop.setProperty("plag.parser.plaggie.minimumSubmissionSimilarityValue",
				prop.getProperty("plaggie.minimumSubmissionSimilarityValue")) == null)
			throw new Exception();
		if (plag_prop.setProperty("plag.parser.plaggie.maximumDetectionResultsToReport",
				prop.getProperty("plaggie.maximumDetectionResultsToReport")) == null)
			throw new Exception();
		if (plag_prop.setProperty("plag.parser.plaggie.useRecursive", prop.getProperty("useRecursive")) == null)
			throw new Exception();
		if (plag_prop.setProperty(INPUT_FILE_FOLDER_NAME, prop.getProperty(INPUT_FILE_FOLDER_NAME)) == null)
			throw new Exception();
		if (plag_prop.setProperty("plag.parser.plaggie.severalSubmissionDirectories",
				prop.getProperty("plaggie.severalSubmissionDirectories")) == null)
			throw new Exception();
		if (plag_prop.setProperty("plag.parser.plaggie.submissionDirectory",
				prop.getProperty("plaggie.submissionDirectory")) == null)
			throw new Exception();
		if (plag_prop.setProperty("plag.parser.plaggie.excludeInterfaces",
				prop.getProperty("plaggie.excludeInterfaces")) == null)
			throw new Exception();
		String plaggieExclude = "";
		List<String> excludeArrayList = new ArrayList(exclude);
		for (int i = 0; i < excludeArrayList.size() - 1; i++) {// create exclusion list for plaggie
			plaggieExclude += excludeArrayList.get(i) + ",";
		}
		plaggieExclude += excludeArrayList.get(exclude.size() - 1);
		if (plag_prop.setProperty("plag.parser.plaggie.excludeFiles", plaggieExclude) == null)
			throw new Exception();
		if (plag_prop.setProperty("plag.parser.plaggie.minimumFileSimilarityValueToReport",
				prop.getProperty(PLAGGIE_MINIMUM_FILE_SIMILARITY_VALUE_TO_REPORT)) == null)
			throw new Exception();
		printProperties();// for debugging
		Plaggie.main(null, plag_prop);
	}

	public static void printProperties() {
		System.out.println("Plaggie.properties....");
		for (Object o : plag_prop.keySet()) {
			System.out.print((String) o + ": " + plag_prop.getProperty((String) o) + "\n");
		}
	}

	public static String getJplagIndex() {
		return jplagResultsFolderName + "/" + "index.html";
	}

	public static String getMossCommandFile() {
		return mossCommandFile;
	}

	public static String[] getMossArgs() {
		return mossArgs;
	}

	public static String getComparisonFile() {
		return comparisonFile;
	}

	public static String getAllJavaPathsFile() {
		return allJavaPathsFile;
	}

	static void initDerivedFileNames() {
		jplagResultsFolderName = outputResultsFolderName + "/" + "JPlagResults";
		allJavaPathsFile = outputResultsFolderName + "/" + "allJavaPaths.txt";
		comparisonFile = outputResultsFolderName + "/" + "comparisons.txt";
		mossCommandFile = outputResultsFolderName + "/" + "MossCommand.txt";

	}
	
	//setters for use as a dependency
	
	public static void setProperty(String aPropertyName, String aValue) {
		prop.setProperty(aPropertyName, aValue);
	}

	public static void setInputFileFolderName(String newValue) {
		prop.setProperty(INPUT_FILE_FOLDER_NAME, newValue);
	}

}
