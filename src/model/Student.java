package model;

public class Student {
	String id;
	double grade;
	double max_sim_jplag;
	double max_sim_moss;
	double min_sim_jplag;
	double min_sim_moss;
	
	public Student(String id,double grade, double max_sim_jplag,double max_sim_moss,double minj, double minm){
		this.id=id;
		this.grade=grade;
		this.max_sim_jplag=max_sim_jplag;
		this.max_sim_moss=max_sim_moss;
		this.min_sim_jplag=minj;
		this.min_sim_moss=minm;
	}
	
	public Student(String id){ //for clustering purposes
		this.id=id;
		this.grade = Double.NaN;
		this.max_sim_jplag= Double.NaN;;
		this.max_sim_moss= Double.NaN;;
		this.min_sim_jplag= Double.NaN;;
		this.min_sim_moss= Double.NaN;;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getGrade() {
		return grade;
	}
	public void setGrade(double grade) {
		this.grade = grade;
	}
	public double getMax_sim_jplag() {
		return max_sim_jplag;
	}
	public void setMax_sim_jplag(double max_sim_jplag) {
		this.max_sim_jplag = max_sim_jplag;
	}
	public double getMax_sim_moss() {
		return max_sim_moss;
	}
	public void setMax_sim_moss(double max_sim_moss) {
		this.max_sim_moss = max_sim_moss;
	}
	public double getMin_sim_jplag() {
		return min_sim_jplag;
	}
	public void setMin_sim_jplag(double min_sim_jplag) {
		this.min_sim_jplag = min_sim_jplag;
	}
	public double getMin_sim_moss() {
		return min_sim_moss;
	}
	public void setMin_sim_moss(double min_sim_moss) {
		this.min_sim_moss = min_sim_moss;
	}
	@Override
	public String toString(){
		//return "id:"+id+", grade:"+grade+", jplag_max:"+max_sim_jplag+", jplag_min:"+min_sim_jplag+", moss_max:"+max_sim_moss+", moss_min:"+min_sim_moss;
		return id;
	}
}
