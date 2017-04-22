package All;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import plag.parser.plaggie.Plaggie;
public class Runner {
public	static Properties prop = new Properties();
public	static Properties plag_prop=new Properties();
	static InputStream plag_in=null;
	static InputStream in = null;
	static ArrayList<String> exclude=null;
	static int numComparison=0;
	public static void main(String[] args) throws Exception {
		System.out.println("Welcome to Plaigarism Coupler.");
		SanitizeSpace();
		ReadProperties();
		RunJplag(true);
		RunMoss(true);
		//RunPlaggie(true);
		System.out.println("Finished");
		System.exit(0);
	}
	
	private static void SanitizeSpace() throws Exception {
		System.out.println("Sanitizing Space...");
		new ProcessBuilder("rm","-r","JplagResults").start();
		new ProcessBuilder("rm","MossLink.txt").start();
		new ProcessBuilder("rm","-r","PlaggieResults").start(); //avoid prompt
		new ProcessBuilder("rm","out.txt").start();
		new ProcessBuilder("rm","comparisons.txt").start();
	}

	private static void ReadProperties() {
		System.out.println("Reading config.properties...");
		try{
			in = new FileInputStream("config.properties");
			prop.load(in);
			plag_prop.load(new FileInputStream("plaggie.properties"));
			exclude=new ArrayList<String>();
			Scanner s = new Scanner(new File(prop.getProperty("excludeName"))); //create list of filters
			while(s.hasNextLine()){
				exclude.add(s.nextLine());
			}
			s.close();
		//	plag_in=new FileInputStream("plaggie.properties");
		//	plag_prop.load(plag_in);
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
		File comparisons = new File("comparisons.txt");
		comparisons.createNewFile();
		BufferedWriter w = new BufferedWriter(new FileWriter(comparisons));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			if(line.startsWith("Comparing")){
				w.write(line+"\n");
				w.flush();
				numComparison++;
			}
			System.out.println(line);
		}
		System.out.println("Comparisons: "+numComparison);
		r.close();
		w.close();
	}
	
	public static void RunMoss(boolean verbose) throws Exception{
		System.out.println("RUNNING MOSS ON "+prop.getProperty("inputFileFolderName")+"...\n--------------------------------");
		ArrayList<String> args = new ArrayList<String>();
		args.add("perl");args.add("moss"); args.add("-l"); args.add("java");args.add("-n");args.add(""+10000); //user must have perl installed and on path 
		//now to grab list of all java files in desired folder
		ArrayList<Integer>deep=new ArrayList<Integer>();
		new ProcessBuilder(new String[]{"cmd.exe","/c","find",prop.getProperty("inputFileFolderName"),"|","grep",".java",">>","out.txt"}).start();
		Thread.sleep(5000);
		BufferedReader getPaths= new BufferedReader(new FileReader(new File("out.txt")));
		//System.out.println(getPaths.lines().count());
		int count = 0;
		while (true) {
			count++;
			String line = getPaths.readLine();
			if (line == null) {
				break;
			}
//			if(verbose)System.out.println(line); //old stuff...now we just /* down to all spots with *.java files in em
			//now see if it is good to be compared in moss
/*			boolean bad = false;
		    for (String string : exclude) {
				if(line.endsWith(string)){bad=true;break;}
			}
		    if(line.contains("MACOSX/"))bad=true;//for some reason had a student who submitted this...
		    if(!bad)args.add(line);*/
			if(line.contains("MACOSX"))continue;
			String[] split = line.split("\\\\");
			//System.out.println(split.length);
			for(int i=0;i<split.length;i++){
				if(split[i].contains(".java")){
					if(!deep.contains(i)){
						deep.add(i);
						break;
					}
				}
			}
		}
		getPaths.close();
		for (Integer integer : deep) {
			String toAdd=prop.getProperty("inputFileFolderName")+"/";
			for(int i=0;i<integer-1;i++){
				toAdd+="*/";
			}
			toAdd+="*.java";
			args.add(toAdd);
		}
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
		r.close();
		r=new BufferedReader(new InputStreamReader(new ProcessBuilder(new String[]{"cmd.exe","/c","echo",link,">","MossLink.txt"}).start().getInputStream()));
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			if(verbose)System.out.println(line);
		}
		r.close();
	}
	public static void RunPlaggie(boolean verbose) throws Exception{
		System.out.println("RUNNING PLAGGIE ON "+prop.getProperty("inputFileFolderName")+"...\n--------------------------------");
		//now overwrite plaggie properties with applicable config.properties values
	if(	plag_prop.setProperty("plag.parser.plaggie.minimumMatchLength",prop.getProperty("plaggie.minimumMatchLength"))==null)throw new Exception();
	if(	plag_prop.setProperty("plag.parser.plaggie.minimumSubmissionSimilarityValue",prop.getProperty("plaggie.minimumSubmissionSimilarityValue"))==null)throw new Exception();
	if(	plag_prop.setProperty("plag.parser.plaggie.maximumDetectionResultsToReport",prop.getProperty("plaggie.maximumDetectionResultsToReport"))==null)throw new Exception();
	if(	plag_prop.setProperty("plag.parser.plaggie.useRecursive",prop.getProperty("useRecursive"))==null)throw new Exception();
	if(	plag_prop.setProperty("inputFileFolderName",prop.getProperty("inputFileFolderName"))==null)throw new Exception();
	if(	plag_prop.setProperty("plag.parser.plaggie.severalSubmissionDirectories",prop.getProperty("plaggie.severalSubmissionDirectories"))==null)throw new Exception();
	if(	plag_prop.setProperty("plag.parser.plaggie.submissionDirectory",prop.getProperty("plaggie.submissionDirectory"))==null)throw new Exception();
	if(	plag_prop.setProperty("plag.parser.plaggie.excludeInterfaces",prop.getProperty("plaggie.excludeInterfaces"))==null)throw new Exception();
	String plaggieExclude="";
	for(int i=0;i<exclude.size()-1;i++){//create exclusion list for plaggie
		plaggieExclude+=exclude.get(i)+",";
	}
		plaggieExclude+=exclude.get(exclude.size()-1);
	if(	plag_prop.setProperty("plag.parser.plaggie.excludeFiles",plaggieExclude)==null)throw new Exception();
	if(	plag_prop.setProperty("plag.parser.plaggie.minimumFileSimilarityValueToReport",prop.getProperty("plaggie.minimumFileSimilarityValueToReport"))==null)throw new Exception();
		printProperties();//for debugging
		Plaggie.main(null,plag_prop);
	}
	
	public static void printProperties(){
		System.out.println("Plaggie.properties....");
		for (Object o : plag_prop.keySet()) {
			System.out.print((String)o+": "+plag_prop.getProperty((String)o)+"\n");
		}
	}

}
