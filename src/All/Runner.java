package All;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class Runner {
	static Properties prop = new Properties();
	static InputStream in = null;
	public static void main(String[] args) throws Exception {
		System.out.println("Welcome to Plaigarism Coupler.");
		ReadProperties();
		System.out.println(RunJplag(true));
	}
	
	private static void ReadProperties() {
		System.out.println("Reading config.properties...");
		try{
			in = new FileInputStream("config.properties");
			prop.load(in);
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("Can't find config.properties file, or it is malformed");
			System.exit(0);
		}
		
	}

	public static String RunJplag(boolean verbose) throws Exception{
		ProcessBuilder builder = new ProcessBuilder("java","-jar","JplagSpace/jplag-2.11.8-SNAPSHOT-jar-with-dependencies.jar","-l",
				"java17");
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
