package clusterer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.jsoup.nodes.Document;

import model.Grading_Clusterings;
import model.Plaig_Clusterings;
import model.Student;
import model.StudentPair;


public class OnJplagComparisons {
	public static Properties prop = new Properties();
	static String comparisonsPath = "comparisons.txt"; // to be prefixed by target folder in config
	public static final String Clusterer_CONFIG_FILE = "config/cluster_config.properties";
	public static Plaig_Clusterings cluster;
	public static Grading_Clusterings gcluster;
	public static HashMap<String, Student> seen; //makes sure student X is the same object in all Student Pairs (for set math)
	public static void main(String args[]) throws IOException{
		clusterOnJplagComparisons();
		System.exit(0);
	}
	
	public static void clusterOnJplagComparisons() throws IOException{
		readConfig();
		applyConfig();
		double avg  = computeAvgSimilarity();
		double rs = computeRsThreshold(avg);
		double cs = computeCsThreshold(avg);
		seen = new HashMap<String,Student>();
		cluster = new Plaig_Clusterings(rs,cs);
		populateCluster(avg);
		cluster.loadInterestingPairs();
		cluster.cluster();
		System.out.println("Printing plaig clusters\n");
		cluster.printClustering();
		System.out.println("\n\n\n\n Printing grade clusters\n");
		gcluster = new Grading_Clusterings(cluster.getInterestingPairs(), cluster.getGroupings());
		gcluster.cluster();
		gcluster.printClustering();
		
	}
	
	public static void readConfig() {
		try {
			prop.load(new FileInputStream(Clusterer_CONFIG_FILE));
		} catch (Exception e) {
			System.out.println("Can't find properties file " + Clusterer_CONFIG_FILE + ", or it is malformed");
		}
	}
	
	public static void applyConfig(){
		String targetFolder = prop.getProperty("targetFolder");
		comparisonsPath = targetFolder + "/" + comparisonsPath;
	}
	
	public static double computeAvgSimilarity() throws IOException{
		File comparisons  =new File(comparisonsPath);
		BufferedReader r = new BufferedReader(new FileReader(comparisons));
		double totalScore = 0;
		int counter = 0;
		while(true){
				String line = r.readLine();
				if(line==null)break;
				counter++;
				totalScore+=Double.parseDouble(line.substring(line.indexOf(":")+2));
		}
		r.close();
		return totalScore/counter;
	}
	
	//for now these two will just hardcoded....will figure out how to compute thresholds later...right now this 40 is based off A1
	public static double computeRsThreshold(double avg){
		return 40/avg;
	}
	
	public static double computeCsThreshold(double avg){
		return 40/avg;
	}
	
	public static void populateCluster(double avg) throws IOException{
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
				Student left = seen.get(s1);
				Student right= seen.get(s2);
				if(left == null){
					left = new Student(s1);
					seen.put(s1, left);
				}
				if(right == null){
					right = new Student(s2);
					seen.put(s2, right);
				}
				StudentPair p = new StudentPair(left,right,score,score/avg);
				cluster.addInterestingPair(p);
		}
		r.close();
	}
}
