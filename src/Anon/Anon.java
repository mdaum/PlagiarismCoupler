package Anon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Anon {
	static String folderName;
	static String currIden;
	static int depth;
	static HashMap<String,String>CommentsIdenMap;
	public static void main(String[] args) throws IOException {
		CommentsIdenMap=new HashMap<String,String>();
		depth = 1;
		try{
			depth=Integer.parseInt(args[1]);
		}
		catch(Exception e){
			System.out.println("No arg for depth found...using depth of 1");
		}
		folderName=args[0];
		clearHeaders();

	}
	
	public static void Anon_ize(int depth){//depth is the depth of the name: 0 is base folder
		
	}
	
	public static void clearHeaders() throws IOException{
		Process p = null;
		try {
			p=new ProcessBuilder(new String[]{"cmd.exe","/c","find",folderName,"|","grep",".java"}).start();
			Thread.sleep(2000);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			String[] split = line.split("\\\\");
			ArrayList<String>names=new ArrayList<String>();
			names.add(split[depth].substring(0, split[depth].indexOf(",")));
			names.add(split[depth].substring(split[depth].indexOf(",")+2,split[depth].indexOf("(")));
			names.add(split[depth].substring(split[depth].indexOf("(")+1,split[depth].indexOf(")")));
			File f = new File(line);
			if(!f.canWrite()){
				System.out.println("can't write file "+line);
				continue;
			}
			File temp = new File("TEMP_GOO");
			temp.createNewFile();
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			BufferedReader r_1=new BufferedReader(new FileReader(f));
			while(true){
				String line_1=r_1.readLine();
				if(line_1==null)break;
				for (String name : names) {
					line_1=line_1.replaceAll(name, shuffle(name));//shuffle all names
				}
				w.write(line_1+"\n");
			}
			w.close();
			r_1.close();
			f.delete();
		if(!temp.renameTo(f)){System.out.println("Couldn't replace file!?");System.exit(0);}
		}
		r.close();
	}
	
	public static String shuffle(String text) {
		if(CommentsIdenMap.get(text)!=null)return CommentsIdenMap.get(text);
	    int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    int targetStringLength = 15;
	    StringBuilder buffer = new StringBuilder(targetStringLength);
	    for (int i = 0; i < targetStringLength; i++) {
	        int randomLimitedInt = leftLimit + (int) 
	          (new Random().nextFloat() * (rightLimit - leftLimit + 1));
	        buffer.append((char) randomLimitedInt);
	    }
	    String generatedString = buffer.toString();
	    CommentsIdenMap.put(text, generatedString);
	    return generatedString;
	    
	}

}
