package MOSS;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Runner {
	public static void main(String[] args) throws Exception {
		System.out.println(Run(false));
	}
	
	public static String Run(boolean verbose) throws Exception{
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
				"cd \"C:\\Users\\mdaum\\Google Drive\\UNC Semester 10\\COMP 992\\Moss Stuff\" && doMoss.sh");
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		String link = "no link";
		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}
			if(line.contains("moss.stanford.edu"))link=line;
			if(verbose)System.out.println(line);
		}
		return link;
	}

}