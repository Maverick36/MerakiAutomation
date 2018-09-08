				Meraki Automation
What is it?

Meraki Automation is a java program, which automates viewing all organizations
and networks while collecting data for a user within Meraki. 

How does it work?

The program prompts the user if they want to add or delete users from the database. 
The user will type Y or y for yes and N or n for no. 
Once done with this operation the user will be prompted if they wish to continue to run the 
whole program. If they don't wish to run the whole program type N or n for no and it will terminate.
If they wish to run the whole program type Y or y for yes and it opens a firefox browser and logs in 
the user to Meraki, from there the program goes through all the organizations a users has access
to. While going through an organization the program stores all available organization into
a sqlite database. Also while within an organization the program stores all networks and devices.
While storing the devices into the database the program determines if a device status is not 
green to report it. Once the program has done a full run through Meraki it will generate a
html page with all devices that were found to be down currently along with previously found 
devices that are still down and the devices that came back up. This html page is then emailed 
to everyone responsible with an Excel version of the data as an attachment. 

Parameters.yml

This file is assumed to contain all the proper data it needs so the program can run.
This file must exist in the bin directory along with the jar file of the program with the correct
data. If the file doesnt not exist in the bin directory the program will generate a parameters.yml
file with the fields empty.The user must go into this file and fill out the fields properly
for the program to work.

Parameters.java

This class reads all the parameters from parameters.yml and creates an object with this data.
This object is used with the Automation class to use the correct parameters when and where needed.
Also this class also checks to see if parameters.yml and the database exist within the bin 
directory. If parameters.yml doesn't exist this class will generate one and throw an execption 
alerting the user to go fill it out correctly. If the database doesn't exist this class will 
generate a new empty database. This class assumes the schema meraki.sqlite file will exist within 
the bin directory to be able to generate a new database.

Users Database Table

This table contains all the users that can be emailed. When the program runs you will be prompted
if you want to add or delete a user to the user table. Once done adding or deleting a user the
program will ask you if want to add or delete another user until you type N or n for no. Then 
you will be prompt if you want to continue the program to generate a report or do you want to end
the program here. So you can interact with the user table or run the whole program with this feature. 

How to run the program?

For this program to run you must create a .jar file of this program. The .jar file must be
located within the bin directory of the project itself. Once the jar file is created you can
run the program by executing the jar file in command line with the following command
java -jar (jar file name). You must execute the command in the directory where the jar file exists.
If a parameters.yml file exists within the bin directory and its fields are correctly filled
out the program will run without issues.