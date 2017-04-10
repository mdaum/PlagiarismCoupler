package Scraper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scrape {
	static ConcurrentHashMap<String,Student>data;
	static Document Jplag,Moss;
	static File grades;
	public static void main(String[]args){
		data = new ConcurrentHashMap<String,Student>();
		scrapeJplag();
		scrapeMoss();
		scrapeGrades();
		printStudents();
	}
	private static void printStudents() {
		for (Student s : data.values()) {
			System.out.println(s);
		}
		
	}
	public static void scrapeGrades(){
		grades=new File("grades.csv");
		Scanner in = null;
		try {
			in = new Scanner(grades);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		in.nextLine();//read first three lines to skip garbage....
		in.nextLine();
		in.nextLine();
		while(in.hasNextLine()){
			String line = in.nextLine();
			String[] fields = line.split(",");
			String name = fields[0];
			double grade = 0;
			try{
				grade=Double.parseDouble(fields[4]);
			}
			catch(Exception e){
				continue;
			}
			Student s = data.get(name);
			if(s==null)continue;
			s.grade=grade;
		}
	}
	public static void scrapeJplag(){
		try {
			Jplag=Jsoup.parse(new File("JplagResults/index.html"),"UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		Elements Entries = Jplag.select("table:nth-of-type(3)").select("tr");//this table is matches sorted by Maximum similarity
/*		Elements LinksInTable = Table.select("a[href]");
		for (Element element : LinksInTable) {
			if(element.hasText())System.out.println(element.text());
		}*/
		for (Element element : Entries) {
			Elements cells = element.select("td");
			String s1=cells.get(0).text();
			s1=s1.substring(s1.lastIndexOf("(")+1, s1.lastIndexOf(")"));//s1 is now onyen
			Student left = data.get(s1);
			if(left==null){
				left = new Student(s1,0,0,0);
				data.put(s1, left);
			}
			for(int i=2;i<cells.size();i++){
				String s2 = cells.get(i).select("a[href]").text();
				s2=s2.substring(s2.lastIndexOf("(")+1, s2.lastIndexOf(")"));
				Student right = data.get(s2);
				if(right==null){
					right=new Student(s2,0,0,0);
					data.put(s2, right);
				}
				String score = cells.get(i).select("font").text();
				score=score.substring(score.lastIndexOf("(")+1,score.lastIndexOf("%"));
				double temp = Double.parseDouble(score);
				if(left.max_sim_jplag<temp)left.max_sim_jplag=temp;
				if(right.max_sim_jplag<temp)right.max_sim_jplag=temp;
			}
		}
		
	}
	
	public static void scrapeMoss(){
		try {
			Moss=Jsoup.parse(new File("MossResults.htm"),"UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			Student s_l=data.get(left_name);
			if(s_l==null){
				s_l=new Student(left_name,0,0,0);
				data.put(left_name, s_l);
			}
			Student s_r=data.get(right_name);
			if(s_r==null){
				s_r=new Student(right_name,0,0,0);
				data.put(right_name, s_r);
			}
			if(s_l.max_sim_moss<l)s_l.max_sim_moss=l;
			if(s_r.max_sim_moss<r)s_r.max_sim_moss=l;
		}
	}
	
}
