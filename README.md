# PlaigarismCoupler
----------------
## Some Notes
 - This can be deployed as a jar that can run on Windows natively, as well as a linux machine
 - No OSX support (yet)
 - 3 Programs in one
    - plaigarism coupler (Package: plaigarism)
       - Will run a range of students through [Jplag](https://github.com/jplag/jplag/releases), and will then construct a 
       [moss](http://theory.stanford.edu/~aiken/moss/) query for you to run.
       - Runnable jar can be found in the `deploy` directory
    - sakai anonimizer (Package: anonymyzer)
       - Will anonimize a sakai folder specified to it, used for data collection in research
    - results scraper (Package: scraper)
       - Simple scripting programs to pull and aggregate similarity scores with grade data and possibly more
 - This README assumes you are mainly interested in using the plaigarsim coupler.
 - Note that there is a 3rd plaigarism detector present but is not used called Plaggie. It is a classpath dependency for this project if you wish to import this into eclipse. You can dig around the code and run this or implement your own plaigarism detector in its place. We omit it from the actual deployed runs because we did not find the results from this detector useful. Plaggie can be found 
 [here] (https://github.com/mdaum/PlayingwPlaggie)
 
 ## Some Setup Needed to run coupler on Windows
 - Since we invoke cmd.exe throughout the program, we need to make your cmd a bit more nixy. Please follow step 2 in the instructions listed [here](https://www.julienklepatch.com/improve-windows-command-line/). You should be able to now perform commands like grep in cmd, which will be needed by the PlaigarsimCoupler.
 
 -------------------
 Linux should be able to run the deployed jar without any issues. If you want to run MOOSS, you will need perl still.
 --------------------
  
 ## Directory Setup for running coupler
 In one directory you must of the following.
 1. An unzipped assignment folder from sakai
 2. The runnable coupler jar
 3. a config folder containing the following
  - an ignore.txt file (optional, and may be named different based two config files)
  - plaggie.properties
  - plaigarism_config.properties
  - plaigarism_course.properties (overrides all duplicate entries in plaigarsim_config.properties)
 5. jplag-2.11.8-SNAPSHOT-jar-with-dependencies
 6. unzipAll.sh
 
 Feel free to draw from the uploaded files of the same name from this repo and modify as you wish. The plaggie.properties file is really used if you intend on designing your own plaigarsim detector by digging into the open-source implementation of JPLAG, Plaggie. Right now this is not used in the current deployable. For information on options in the other .properties files, see the inline comments.
 
 ## Running instructions
 1. `sh unzipAll.sh AssignmentX`
     - this will unzip each students zipped submission so it can be processed by JPLAG and the moss command generator.
 2. `java -jar Coupler.jar`
 3. No time to grab coffee this thing is fast!
 4. Results will be found in `plaigarismResults/JPlagResults`
    - Questions about interpretting index.html? Google is your friend :)
 5. The moss command outputted should be run with your moss executable in the same directory as the sakai folder
    - see the moss link for how to get set up with a moss runtime configured for you. PLEASE do not use mine they will rate-limit me :)
    
## FAQ for Windows ##
1. When the moss command is being outputted, I am not getting the correct paths to my .java files
   - Your find command provided by unixUtils may not be getting picked up. Make sure that addition to your path including wbin was put at the FRONT of your path.
2. when I run the perl moss ..... command it keeps saying that the files don't exist and stops
   - I have sometimes found that on some setups cmd.exe does not do well running moss. I normally run the moss command in git bash. Try that.
   --------------------------------
All other issues: please open a issue on this repo so I can address it and add it to the faq
 
