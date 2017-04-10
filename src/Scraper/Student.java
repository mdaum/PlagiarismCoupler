package Scraper;

public class Student {
	String id;
	double grade;
	double max_sim_jplag;
	double max_sim_moss;
	
	public Student(String id,double grade, double max_sim_jplag,double max_sim_moss){
		this.id=id;
		this.grade=grade;
		this.max_sim_jplag=max_sim_jplag;
		this.max_sim_moss=max_sim_moss;
	}
	@Override
	public String toString(){
		return "id:"+id+",grade:"+grade+",jplag:"+max_sim_jplag+",moss:"+max_sim_moss;
	
	}
}
