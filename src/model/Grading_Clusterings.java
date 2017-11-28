package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

public class Grading_Clusterings {
	HashMap<String, StudentPair> interestingPairs;
	static ConcurrentHashMap<Integer,Set<Student>> groupings; //will start off as copy of Plagi_Cluster's groupings....and will condense with algo
	HashSet<String> interestingStudents;
	HashMap<String,Integer>jobs;
	public Grading_Clusterings(HashMap<String, StudentPair> iP, ConcurrentHashMap<Integer,Set<Student>> g, boolean deepCopy){ //takes in Plagi_Clusterings interestingPairs (shared dictionary), and deep copies it's groupings
		this.interestingPairs = iP;
		this.groupings=g;
		this.interestingStudents = new HashSet<String>();
		populate_interesting_students();
		jobs = new HashMap<String,Integer>();
	}
	
	public void populate_interesting_students(){
		for (StudentPair p : interestingPairs.values()) {
			interestingStudents.add(p.left.id);
			interestingStudents.add(p.right.id);
		}
	}
	
	public void cluster(){ //assuming we have an initialized groupings, we now cluster recursively until no combinations can be made
		for (String s : interestingStudents) {
			Double minVariance = Double.MAX_VALUE;
			int bestGroup = -1;//never chosen as key
			int highestMatchGroup=-1;
			Double highestMatch = Double.MIN_VALUE;
			for (int i: groupings.keySet()) { //first find group we wish to stay in (lowest variance with other members)
				Set<Student>group = groupings.get(i);
				boolean found = false;
				for(Student student: group){
					if(student.id.equals(s))found = true;
				}
				if(!found)continue;
				ArrayList<Double> values = new ArrayList<Double>();
				int counter = 0;
				double total = 0;
				double mean = 0;
				for (String comparison : deriveKeys(group)) {
					if(!comparison.substring(comparison.indexOf('|') + 1).equals(s) && !comparison.split("\\|")[0].equals(s))continue;
					String comparison_reversed =  comparison.substring(comparison.indexOf('|') + 1)+ "|" +comparison.split("\\|")[0] ;
					StudentPair pk = interestingPairs.get(comparison);
					StudentPair pkr = interestingPairs.get(comparison_reversed);
					if(pk==null && pkr == null) continue;
					counter++;
					if (pk !=null){
						if(pk.relativeScore>highestMatch){
							highestMatchGroup=i;
							highestMatch=pk.relativeScore;
						}
						values.add(pk.relativeScore);
						total += pk.relativeScore;
					}
					else{
						if(pkr.relativeScore>highestMatch){
							highestMatchGroup=i;
							highestMatch=pkr.relativeScore;
						}
						values.add(pkr.relativeScore);
						total += pkr.relativeScore;
					}
				}
				mean = total/counter;
				double variance = 0;
				for (Double score : values) {
					variance += ((score - mean) * (score - mean));
				}
				variance = variance / (counter-1);
				minVariance = Math.min(minVariance, variance);
				if(minVariance == variance)bestGroup = i;
			}
			//now we remove student s from all groups except for target
			if(bestGroup>-1)jobs.put(s,bestGroup);
			else{//student is only in a group of twos, making all variances NAN....go with highest sim
				jobs.put(s, highestMatchGroup);
			}
		}
		for (String k : jobs.keySet()) {
			removeFromOtherGroups(k,jobs.get(k));
		}
	}
	
	public Object removeFromOtherGroups(String s, int stay){
		for (int i: groupings.keySet()) {
			if(i==stay)continue;
			Set<Student> group = groupings.get(i);
			for (Student student : group) {
				if(student.id.equals(s)){
					group.remove(student);
					return removeFromOtherGroups(s,stay);
				}
			}
		}
		return null;
	}
	
	private ArrayList<String> deriveKeys(Set<Student> students) {
		ArrayList<String> toRet = new ArrayList<String>();
		Object[] arr = students.toArray();
		for(int i =0; i < arr.length -1;i++){
			for(int j = i+1; j< arr.length; j++){
				Student i_s = (Student) arr[i];
				Student j_s = (Student) arr[j];
				toRet.add(i_s.id + "|" + j_s.id);
			}
		}
		return toRet;
	}
	
	public static void printClustering(){
		for (Set<Student> s:groupings.values()) {
			if (s.isEmpty())continue;
			System.out.print("{ ");
			for (Student student : s) {
				System.out.print(student.id+" ");
			}
			System.out.println("}");
		}
	}

	// getters
	public HashMap<String, StudentPair> getInterestingPairs() {
		return interestingPairs;
	}
	public Collection<Set<Student>> getGroupings() {
		return groupings.values();
	}
	
	public double computeAverageSize() {
		int count = 0;
		int total = 0;
		for (Set<Student> set : groupings.values()) {
			if(set.size()==0)continue;
			count++;
			total += set.size();
		}
		return total/count;
	}

	public HashSet<String> getInterestingStudents() {
		return interestingStudents;
	}

	public int[] computeHistogram() {
		int maxSize = 0;
		for (Set<Student> set : groupings.values()) {
			if(set.size()>maxSize)maxSize=set.size();
		}
		int[]h = new int[maxSize];
		for(int i = 1; i<= maxSize;i++){
			int count = 0;
			for (Set<Student> set : groupings.values()) {
				if(set.size()==i)count++;
			}
			h[i-1]=count;
		}
		return h;
	}
	
}
