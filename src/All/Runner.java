package All;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Properties;

public class Runner {
	static Properties prop = new Properties();
	static InputStream in = null;
	public static void main(String[] args) throws Exception {
		System.out.println("Welcome to Plaigarism Coupler.");
		SanitizeSpace();
		ReadProperties();
		RunJplag(true);
		RunMoss(true);
		System.out.println("Finished");
		System.exit(0);
	}
	
	private static void SanitizeSpace() throws Exception {
		System.out.println("Sanitizing Space...");
		new ProcessBuilder("rm","-r","JplagResults").start();
		new ProcessBuilder("rm","MossLink.txt").start();
	}

	private static void ReadProperties() {
		System.out.println("Reading config.properties...");
		try{
			in = new FileInputStream("config.properties");
			prop.load(in);
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("Can't find config.properties file, or it is malformed");
			System.exit(0);
		}
		
	}

	public static void RunJplag(boolean verbose) throws Exception{
		System.out.println("RUNNING JPLAG ON "+prop.getProperty("inputFileFolderName")+"...\n--------------------------------");
		ArrayList<String> args = new ArrayList<String>();
		args.add("java");args.add("-jar");args.add("jplag-2.11.8-SNAPSHOT-jar-with-dependencies.jar");
		args.add("-l");args.add("java17");args.add("-s");args.add("-r");args.add("JPlagResults");
		args.add("-m");args.add((int)(Float.parseFloat(prop.getProperty("plaggie.minimumFileSimilarityValueToReport"))*100)+"%");
		//args.add("-m");args.add(prop.getProperty("plaggie.maximumDetectionResultsToReport")); //looks like this does not work with JPlag
		if(Boolean.parseBoolean(prop.getProperty("excludeFiles"))){ //file exclusion
			args.add("-x");args.add(prop.getProperty("excludeName"));
		}
		args.add(prop.getProperty("inputFileFolderName"));
		String[] toPass = new String[args.size()];
		ProcessBuilder builder = new ProcessBuilder(args.toArray(toPass));
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			System.out.println(line);
		}
	}
	
	public static void RunMoss(boolean verbose) throws Exception{
		System.out.println("RUNNING MOSS ON "+prop.getProperty("inputFileFolderName")+"...\n--------------------------------");
		ArrayList<String> args = new ArrayList<String>();
		args.add("ls");//placeholder
		String[] toPass = new String[args.size()];
		ProcessBuilder builder = new ProcessBuilder(args.toArray(toPass));
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		String link = "no link";
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			if(line.contains("moss.stanford.edu"))link=line;
			if(verbose)System.out.println(line);
		}
		r=new BufferedReader(new InputStreamReader(new ProcessBuilder(new String[]{"cmd.exe","/c","echo",link,">","MossLink.txt"}).start().getInputStream()));
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			if(verbose)System.out.println(line);
		}
	}

}
