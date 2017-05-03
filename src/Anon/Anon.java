package Anon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


/*
 * 
 * Research: Make the anonimizer platform agnostic....make it work with folders w/o headers....and w them....also anonimize grades.csv!!! 
 * Pehaps make anonimizing process more readable too
 * ...student 1, student 2 etc...needs to work on windows....mac....linux....
 * so really need to revamp this thing! 
 * Also add logging support so user is 
 * confident that stuff is actually being renamed....just log it to a file....

After this i can get aikat and whoever else to use this and hand me sakai folders w/ grades.csv
 so I can then do a high-low-median in similiarity 
 scores for a semester's course and finally get some graphs going!!
 */
public class Anon {
	static String folderName;
	static String currIden;
	static int depth;
	static HashMap<String,String>CommentsIdenMap;
	static File log_file;
	static FileWriter log;
	public static void main(String[] args) throws IOException, InterruptedException {
		//instantiate vars
		CommentsIdenMap=new HashMap<String,String>();
		depth = 1;
		folderName=args[0];
		log_file=new File("anon_log");
		log_file.delete();
		log_file.createNewFile();
		log = new FileWriter(log_file);
		//determine which version to run based on OS
		String os=System.getProperty("os.name").toLowerCase();
		if(os.contains("window")){
			clearHeaders_Windows();
			Anon_ize_Windows(1);
			Anon_ize_grades();
		}
		else{
			System.out.println("not windows");
		}

	}
	public static void Anon_ize_grades() throws IOException, InterruptedException{
		log.write("CLEARING GRADES.CSV\n");
		//get csv file
		File csv=new File(folderName+"/grades.csv");
		//reader for it
		BufferedReader r = new BufferedReader(new FileReader(csv));
		//file to be the anoncsv
		File temp = new File(folderName+"/ANONGrades.csv");
		temp.createNewFile();
		//writer for it
		BufferedWriter w = new BufferedWriter(new FileWriter(temp));
		//first three lines do not contain any student stuff
		w.write(r.readLine()+"\n");
		w.flush();
		w.write(r.readLine()+"\n");
		w.flush();
		w.write(r.readLine()+"\n");
		
		while(true){
			w.flush();
			String line=r.readLine();
			if (line == null) {
				break;
			}
			line=line.replaceAll(" ", "");//string spaces
			String[]names = line.split(",");
			//replace each occurence of name w its shuffled version
			line=line.replaceAll(names[0], shuffle(names[0]));
			line=line.replaceAll(names[1], shuffle(names[1]));
			line=line.replaceAll(names[2], shuffle(names[2]));
			line=line.replaceAll(names[3], shuffle(names[3]));
			//write to the new file
			w.write(line+"\n");
		}
		w.close(); 
		//remove old csv and rename the new one
		Process rmcsv =new ProcessBuilder(new String[]{"cmd.exe","/c","cd",folderName,"&","rm","grades.csv","&","exit"}).start();
		Thread.sleep(100);
		//if(!temp.renameTo(csv)){System.out.println("Couldn't replace file!?");System.exit(0);}
		r.close();
	}
	
	public static void Anon_ize_Windows(int depth) throws IOException, InterruptedException{//depth is the depth of the name: 0 is base folder
		Process p = null;
		log.write("CLEARING TOP-LEVEL DIRECTORY NAMES\n");
		try{
			//look at each student directory
			String[]command = new String[8];
			command[0]="cmd.exe";
			command[1]="/c";
				command[2]="cd";
			command[3]=folderName;
			command[4]="&";
			command[5]="ls";
			command[6]="&";
			command[7]="exit";
			p=new ProcessBuilder(command).start();
			Thread.sleep(2000);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		//reader for it
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while(true){
			line = r.readLine();
			if (line == null) {
				break;
			}
			//csv handled elsewhere
			if(line.contains(".csv"))continue;//skip csv file
			Process rm = (new ProcessBuilder(new String[]{"cmd.exe","/c","cd","\""+folderName+"/"+line+"\"","&","rm","*.txt","*.html","&","exit"}).start());//kill txt and html...could have names
			Thread.sleep(300);
			//get lastname,firstname,onyen
			String lastName=line.substring(0, line.indexOf(","));
			String firstName=line.substring(line.indexOf(",")+2,line.indexOf("("));
			String onyen=line.substring(line.indexOf("(")+1,line.indexOf(")"));
			//toReplace has anon versions
			String toReplace=shuffle(lastName)+", "+shuffle(firstName)+"("+shuffle(onyen)+")";
			//System.out.println("\"" + folderName + "/" +"\"");
			//rename the directory
			Process rename=(new ProcessBuilder(new String[]{"cmd.exe","/c","cd",folderName,"&","rename","\""+line+"\"","\""+toReplace+"\"","&","exit"}).start());
			Thread.sleep(100);
			log.write("renamed directory "+line+" to "+toReplace+"\n");
			log.write("removed all txt files and html files from directory "+line+"\n");
		}
	}
	
	public static void clearHeaders_Windows() throws IOException{
		Process p = null;
		log.write("CLEARING JAVA FILES OF NAMES\n");
		try {
			//get path to each java file
			p=new ProcessBuilder(new String[]{"cmd.exe","/c","find",folderName,"|","grep",".java","&","exit"}).start();
			Thread.sleep(2000);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
		//reader for it
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
	//		System.out.println(line);
			//hold on to orig line for cd later
			String orig_line=line;
			line=line.replaceAll("\\\\", "/"); //sanitize
			String[] split = line.split("/");
			//load up our known names
			ArrayList<String>names=new ArrayList<String>();
			//should be last name
			names.add(split[depth].substring(0, split[depth].indexOf(",")));
			//should be first name
			names.add(split[depth].substring(split[depth].indexOf(",")+2,split[depth].indexOf("(")));
			//should be onyen
			names.add(split[depth].substring(split[depth].indexOf("(")+1,split[depth].indexOf(")")));
			//make a new file to write to
			File f = new File(orig_line);
			if(!f.canWrite()){
				System.out.println("can't write file "+line);
				continue;
			}
			File temp = new File("TEMP_GOO");
			temp.createNewFile();
			//writer for new file...and reader for our orig java file
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			BufferedReader r_1=new BufferedReader(new FileReader(f));
			int line_num=0;
			while(true){
				String line_1=r_1.readLine();
				if(line_1==null)break;
				//replace all instances of names with anon version
				for (String name : names) {
					line_1=line_1.replaceAll(name, shuffle(name));//shuffle all names
					log.write("changed "+name+" on line "+line_num+" of "+f.getName()+"\n");
				}
				//write it to new file
				w.write(line_1+"\n");
				line_num++;
			}
			w.close();
			r_1.close();
			//delete orig java file
			f.delete();
			//rename our new file to orig name
		if(!temp.renameTo(f)){System.out.println("Couldn't replace file!?");System.exit(0);}
		}
		r.close();
	}
	
	public static String shuffle(String text) {
		//we see if we have seen the name before, returning its mapped anon version if it exists
		if(CommentsIdenMap.get(text)!=null)return CommentsIdenMap.get(text);
		//otherwise we generate 10 char a-z identifier
	    int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    int targetStringLength = 10;
	    StringBuilder buffer = new StringBuilder(targetStringLength);
	    for (int i = 0; i < targetStringLength; i++) {
	        int randomLimitedInt = leftLimit + (int) 
	          (new Random().nextFloat() * (rightLimit - leftLimit + 1));
	        buffer.append((char) randomLimitedInt);
	    }
	    String generatedString = buffer.toString();
	    //load the generated identifier into hash map
	    CommentsIdenMap.put(text, generatedString);
	    return generatedString;
	    
	}

}
