package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

public class Grading_Clusterings {
	HashMap<String, StudentPair> interestingPairs;
	static HashSet<Set<Student>> groupings; //will start off as copy of Plagi_Cluster's groupings....and will condense with algo
	HashSet<String> interestingStudents;
	public Grading_Clusterings(HashMap<String, StudentPair> iP, HashSet<Set<Student>> g, boolean deepCopy){ //takes in Plagi_Clusterings interestingPairs (shared dictionary), and deep copies it's groupings
		this.interestingPairs = iP;
		if(deepCopy)deepCopy(g);
		else this.groupings=g;
		this.interestingStudents = new HashSet<String>();
		populate_interesting_students();
	}
	
	public void populate_interesting_students(){
		for (StudentPair p : interestingPairs.values()) {
			interestingStudents.add(p.left.id);
			interestingStudents.add(p.right.id);
		}
	}
	
	public void deepCopy(HashSet<Set<Student>> g){
		groupings = new HashSet<Set<Student>>();
		for (Set<Student> set : g) {
			Set<Student> toAdd = new HashSet<Student>();
			for (Student student : set) {
				toAdd.add(student);
			}
			groupings.add(toAdd);
		}
	}
	
	public void cluster(){ //assuming we have an initialized groupings, we now cluster recursively until no combinations can be made
		for (String s : interestingStudents) {
			Double minVariance = Double.MAX_VALUE;
			Set<Student> bestGroup = null;
			for (Set<Student> group : groupings) { //first find group we wish to stay in (lowest variance with other members)
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
						values.add(pk.relativeScore);
						total += pk.relativeScore;
					}
					else{
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
				if(minVariance == variance)bestGroup = group;
			}
			//now we remove student s from all groups except for target
			removeFromOtherGroups(s, bestGroup);
		}
	}
	
	public Object removeFromOtherGroups(String s, Set<Student> stay){
		for (Set<Student> group : groupings) {
			if(group==stay)continue;
			for (Student student : group) {
				if(student.id.equals(s)){
					group.remove(student);
					return removeFromOtherGroups(s,stay); // avoiding concurrent modification
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
		for (Set<Student> s:groupings) {
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
	public HashSet<Set<Student>> getGroupings() {
		return groupings;
	}
	
	public double computeAverageSize() {
		int count = 0;
		int total = 0;
		for (Set<Student> set : groupings) {
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
		for (Set<Student> set : groupings) {
			if(set.size()>maxSize)maxSize=set.size();
		}
		int[]h = new int[maxSize];
		for(int i = 1; i<= maxSize;i++){
			int count = 0;
			for (Set<Student> set : groupings) {
				if(set.size()==i)count++;
			}
			h[i-1]=count;
		}
		return h;
	}
	
}
