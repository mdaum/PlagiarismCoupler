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
import model.Plagi_Clusterings;
import model.Student;
import model.StudentPair;


public class OnJplagComparisons {
	public static Properties prop = new Properties();
	static String comparisonsPath = "comparisons.txt"; // to be prefixed by target folder in config
	public static final String Clusterer_CONFIG_FILE = "config/cluster_config.properties";
	public static Plagi_Clusterings cluster;
	public static Grading_Clusterings gcluster;
	public static HashMap<String, Student> seen; //makes sure student X is the same object in all Student Pairs (for set math)
	public static String[] arguments;
	public static double threshold = Double.NaN;
	public static int numComparisons = 0;
	public static void main(String args[]) throws IOException{
		arguments=args;
		clusterOnJplagComparisons();
		System.exit(0);
	}
	
	public static void clusterOnJplagComparisons() throws IOException{
		double rsthreshold = 40;
		double csthreshold = 40;
		readConfig();
		applyConfig();
		if(!Double.isNaN(threshold)){ //overwrite w/ args if present
			rsthreshold = threshold;
			csthreshold = threshold;
		}
		double avg  = computeAvgSimilarity();
		double rs = rsthreshold/avg;
		double cs = csthreshold/avg;
		double b4Population = System.currentTimeMillis();
		seen = new HashMap<String,Student>();
		cluster = new Plagi_Clusterings(rs,cs);
		populateCluster(avg);
		double afterPopulation = System.currentTimeMillis();
		double b4LoadingPairs = System.currentTimeMillis();
		cluster.loadInterestingPairs();
		double afterLoadingPairs = System.currentTimeMillis();
		int numInteresting = cluster.getInterestingPairs().size();
		double b4PlagCluster = System.currentTimeMillis();
		cluster.cluster();
		double afterPlagCluster = System.currentTimeMillis();
		double avgClusterSize = cluster.computeAverageSize();
		System.out.println("Printing plagi clusters\n");
		cluster.printClustering();
		int[] pHistogram = cluster.computeHistogram();
		gcluster = new Grading_Clusterings(cluster.getInterestingPairs(), cluster.getGroupings(), false);
		double b4GradeCluster = System.currentTimeMillis();
		gcluster.cluster();
		double afterGradeCluster = System.currentTimeMillis();
		double avgGClusterSize = gcluster.computeAverageSize();
		System.out.println("\n\n\n\n Printing grade clusters\n");
		gcluster.printClustering();
		int[] gHistogram = gcluster.computeHistogram();
		System.out.println("\n\n\nThere were a total of "+numComparisons+" comparisons made in this assignment");
		System.out.println("Average Similarity Over All Comparisons: "+avg);
		System.out.println("Clustered " + numInteresting +" interesting pairs of students");
		System.out.println("Of those pairs, there were "+gcluster.getInterestingStudents().size() + " distinct students");
		System.out.println("\nPlag Cluster Size Histogram:");
		printHistogram(pHistogram);
		System.out.println("\nGrade Cluster Size Histogram");
		printHistogram(gHistogram);
		System.out.println("Time to Populate: "+ (afterPopulation-b4Population));
		System.out.println("Time to Load Interesting pairs: "+ (afterLoadingPairs-b4LoadingPairs));
		System.out.println("Time compute Plagarism Clusters: "+ (afterPlagCluster-b4PlagCluster));
		System.out.println("Time to compute Grading Clusters: "+ (afterGradeCluster-b4GradeCluster));
		
	}
	
	private static void printHistogram(int[] histogram) {
		for(int i = 0;i<histogram.length-1;i++){
			System.out.print(i+1+":"+histogram[i]+" ");
		}
		System.out.println(histogram.length+":"+histogram[histogram.length-1]);
	}

	public static void readConfig() {
		try {
			prop.load(new FileInputStream(Clusterer_CONFIG_FILE));
		} catch (Exception e) {
			System.out.println("Can't find properties file " + Clusterer_CONFIG_FILE + ", or it is malformed");
		}
	}
	
	public static void applyConfig(){
		String targetFolder;
		if(arguments!=null&&arguments.length==2){ // targetFolder threshold
			targetFolder=arguments[0];
			threshold = Double.parseDouble(arguments[1]);
		}
		else targetFolder = prop.getProperty("targetFolder");
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
		numComparisons=counter;
		return totalScore/counter;
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
