package scraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import model.Student;

// Will collect each of the following pieces of information per student, per assignment
// id: studentId, grade:_____, jplag_max:_____, jplag_min:_______, moss_max:______, moss_min:______
// see the Student object in model

public class Scrape {
	public static ConcurrentHashMap<String,Student>scrapedData;
	public static final String SCRAPER_CONFIG_FILE = "config/scraper_config.properties";
	public static Properties prop = new Properties();
	static Document Jplag,Moss;
	static boolean courseMode;
	
	//paths to be prefixed by folder in config
	static String gradesPath = "grades.csv";
	static String comparisonsPath = "comparisons.txt";
	static String mossResultsPath = "MossResults.html";
	
	public static void main(String[]args) throws IOException{
		runScraper();
		System.exit(0);
	}
	
	public static void runScraper() throws IOException{
		new File("tempGrades.csv").delete();
		scrapedData = new ConcurrentHashMap<String,Student>();
		readConfig();
		applyConfig();
		if(!courseMode){
			scrapeJplag();
			scrapeMoss();
			scrapeGrades();
			printStudents();
		}
		else{
			File courseFolder = new File(prop.getProperty("courseModeFolder"));
			for (String assignmentFolder : courseFolder.list()) {
				System.out.println(assignmentFolder +"\n");
				gradesPath = courseFolder + "/" + assignmentFolder + "/" + "grades.csv";
				comparisonsPath = courseFolder + "/" + assignmentFolder + "/" + "comparisons.txt";
				mossResultsPath = courseFolder + "/" + assignmentFolder + "/" + "MossResults.html";
				scrapeJplag();
				scrapeMoss();
				scrapeGrades();
				printStudents();
				System.out.println("end " +assignmentFolder + "\n\n");
				scrapedData.clear();
			}
		}
	}
	
	public static void readConfig() {
		try {
			prop.load(new FileInputStream(SCRAPER_CONFIG_FILE));
		} catch (Exception e) {
			System.out.println("Can't find plaggie properties file " + SCRAPER_CONFIG_FILE + ", or it is malformed");
		}
	}
	
	public static void applyConfig(){
		courseMode = Boolean.parseBoolean(prop.getProperty("courseMode"));
		if(!courseMode){
			String assignmentFolder = prop.getProperty("assignmentFolder");
			gradesPath = assignmentFolder + "/" + gradesPath;
			comparisonsPath = assignmentFolder + "/" + comparisonsPath;
			mossResultsPath = assignmentFolder + "/" + mossResultsPath;
		}
	}
	
	private static void printStudents() {
		for (Student s : scrapedData.values()) {
			if(s.getMin_sim_jplag() == Double.MAX_VALUE)s.setMin_sim_jplag(Double.NaN); // did not find it
			if(s.getMin_sim_moss() == Double.MAX_VALUE)s.setMin_sim_moss(Double.NaN);
			if(Double.isNaN(s.getMin_sim_moss()) && s.getMin_sim_moss() > s.getMax_sim_moss()) throw new Error("WHAT???"); 
			System.out.println(s);
		}
		System.out.println("Have data for "+ scrapedData.size() + " students.");
		
	}
	

	public static void scrapeGrades(){
		File grades=new File(gradesPath);
		Scanner in = null;
		try {
			in = new Scanner(grades);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		in.nextLine();//read first three lines to skip garbage....
		in.nextLine();
		in.nextLine();
		while(in.hasNextLine()){
			String line = in.nextLine();
			line = line.replaceAll("\"", "");
			String[] fields = line.split(",");
			String name = fields[0];
			double grade = 0;
			try{
				grade=Double.parseDouble(fields[4]);
			}
			catch(Exception e){
				continue;
			}
			Student s = scrapedData.get(name);
			if(s==null)continue;
			s.setGrade(grade);
		}
		in.close();
	}
	
	public static void scrapeJplag() throws IOException{
		File comparisons  =new File(comparisonsPath);
		BufferedReader r = new BufferedReader(new FileReader(comparisons));
		while(true){
				String line = r.readLine();
				if(line==null)break;
				String s1,s2;
				double score;
				s1=line.substring(line.indexOf("(")+1, line.indexOf(")"));
				s2=line.substring(line.lastIndexOf("(")+1,line.lastIndexOf(")"));
				score=Double.parseDouble(line.substring(line.indexOf(":")+2));
				Student left=scrapedData.get(s1);
				Student right=scrapedData.get(s2);
				if(left==null){
					left=new Student(s1,0,0,0,Double.MAX_VALUE,Double.MAX_VALUE);
					scrapedData.put(s1, left);
				}
				if(right==null){
					right=new Student(s2,0,0,0,Double.MAX_VALUE,Double.MAX_VALUE);
					scrapedData.put(s2, right);
				}
				if(left.getMax_sim_jplag()<score)left.setMax_sim_jplag(score);
				if(left.getMin_sim_jplag()>score)left.setMin_sim_jplag(score);
				if(right.getMax_sim_jplag()<score)right.setMax_sim_jplag(score);
				if(right.getMin_sim_jplag()>score)right.setMin_sim_jplag(score);
		}
		r.close();
		
	}
	
	public static void scrapeMoss(){
		try {
			Moss=Jsoup.parse(new File(mossResultsPath),"UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		Elements Entries = Moss.select("table:nth-of-type(1)").select("tr");//this table is matches sorted by Maximum similarity
		for(int i =1;i<Entries.size();i++){
			Elements cells = Entries.get(i).select("TD");
			String left,right, left_name,right_name,left_score,right_score;
			left=cells.get(0).select("a[href]").text();
			right=cells.get(1).select("a[href]").text();
			left_name=left.substring(left.indexOf("(")+1, left.indexOf(")"));
			right_name=right.substring(right.indexOf("(")+1, right.indexOf(")"));
			if(left_name.equals(right_name))continue;//don't want ppl compared w self
			left_score=left.substring(left.lastIndexOf("(")+1,left.lastIndexOf("%"));
			right_score=right.substring(right.lastIndexOf("(")+1,right.lastIndexOf("%"));
			double r = Double.parseDouble(right_score);
			double l= Double.parseDouble(left_score);
			Student s_l=scrapedData.get(left_name);
			if(s_l==null){
				s_l=new Student(left_name,0,0,0,Double.MAX_VALUE,Double.MAX_VALUE);
				scrapedData.put(left_name, s_l);
			}
			Student s_r=scrapedData.get(right_name);
			if(s_r==null){
				s_r=new Student(right_name,0,0,0,Double.MAX_VALUE,Double.MAX_VALUE);
				scrapedData.put(right_name, s_r);
			}
			if(s_l.getMax_sim_moss()<l)s_l.setMax_sim_moss(l);
			if(s_r.getMax_sim_moss()<r)s_r.setMax_sim_moss(r);
			if(s_l.getMin_sim_moss()>l)s_l.setMin_sim_moss(l);
			if(s_r.getMin_sim_moss()>r)s_r.setMin_sim_moss(r);
		}
	}
	
}
