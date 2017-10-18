package scraper;

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
	@Override
	public String toString(){
		return "id:"+id+", grade:"+grade+", jplag_max:"+max_sim_jplag+", jplag_min:"+min_sim_jplag+", moss_max:"+max_sim_moss+", moss_min:"+min_sim_moss;
	
	}
}
