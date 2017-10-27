package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Clusterings {
	HashMap<String, StudentPair> interestingPairs;
	float rsThreshold; // relative similarity threshold, used to determine whether a comparison goes into interestingPairs
	float csThreshold; // combination/clustering simlarity threshold, used to determine whether or not we merge two sets of students
	HashSet<Set<Student>> groupings; //will start off as the set version of all interesting pairs, and then we condense as much as we can. This will then contain the desired groups/clusterings
	
	public Clusterings(float rsThreshold, float csThreshold){
		this.rsThreshold = rsThreshold;
		this.csThreshold = csThreshold;
		this.interestingPairs = new HashMap<String, StudentPair>();
		this.groupings = new HashSet<Set<Student>>();
	}
	
	public void addInterestingPair(StudentPair p){ // to be used by various clusterers
		if(p.relativeScore >= rsThreshold)interestingPairs.put(p.id, p);
	}
	
	public void loadInterestingPairs(){ //assuming we have now populated interestingPairs, we load them all into our groupings, initializing our clustering
		for (StudentPair p : interestingPairs.values()) {
			HashSet<Student> toAdd = new HashSet<Student>();
			toAdd.add(p.left);
			toAdd.add(p.right);
			groupings.add(toAdd);
		}
	}
	
	public void cluster(){ //assuming we have an initialized groupings, we now cluster recursively until no combinations can be made
		boolean combinationsMade = false; //terminating condition
		for (Set<Student> s : groupings) {
			for (Set<Student> s_p : groupings) { // s prime
				if(s.equals(s_p))continue;
				Set<Student> s_d = new HashSet<Student>(); // XOR of s and s_p
				Set<Student> s_u = new HashSet<Student>(); // Union of s and s_p
				Set<Student> s_i = new HashSet<Student>(); // Intersection of s and s_p
				s_u.addAll(s); s_u.addAll(s_p); // s_u init
				s_i.addAll(s); s_i.retainAll(s_p); // s_i init
				s_d.addAll(s_u); s_d.removeAll(s_i); //s_d init
				ArrayList<String> keys = deriveKeys(s_d); //will generate all keys to look up studentPairs
				boolean doNothing = false; //will set to true if we see that s and s_p should not be combined
				for (String k : keys) { //note that the key may be in the opposite order contained in hashmap
					String k_reversed = k.split("\\|")[0] + "|" + k.substring(k.indexOf('|') + 1);
					StudentPair pk = interestingPairs.get(k);
					StudentPair pkr = interestingPairs.get(k_reversed);
					if(pk == null && pkr == null){
						doNothing = true;
						break;
					}
					if(pk !=null && pkr!=null)throw new Error("WHAT??");
					if(pk!=null){
						doNothing = pk.relativeScore >= csThreshold;
					}
					else{
						doNothing = pkr.relativeScore >= csThreshold;
					}
				}
				if(!doNothing){
					s.clear();
					s.addAll(s_u);
					groupings.remove(s_p);
					combinationsMade = true;
				}
			}
		}
		if(combinationsMade) cluster();
	}
	
	private ArrayList<String> deriveKeys(Set<Student> students) {
		ArrayList<String> toRet = new ArrayList<String>();
		Student[] arr = (Student[]) students.toArray();
		for(int i =0; i < arr.length -1;i++){
			for(int j = i+1; j< arr.length; j++){
				toRet.add(arr[i].id + "|" + arr[j].id);
			}
		}
		return toRet;
	}

	// getters
	public HashMap<String, StudentPair> getInterestingPairs() {
		return interestingPairs;
	}
	public float getRsThreshold() {
		return rsThreshold;
	}
	public float getCsThreshold() {
		return csThreshold;
	}
	public HashSet<Set<Student>> getGroupings() {
		return groupings;
	}
	
}
