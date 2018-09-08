package com.meraki.automation;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

/**
 * Created by miguelmorales on 2/8/16.
 */
public class Parameters {
    private String merakiUsername;
    private String merakiPassword;
    private String mailerTransport;
    private String mailerHost;
    private String mailerFrom;
    private String mailerUsername;
    private String mailerPassword;
    private int mailerPort;
    private String mailerSubject;
    private String reportFileLocation;
    private String jdbcDriver;
    private String jdbcUrl;
    private int reportNowRange;

    public Parameters() {
        InputStream parametersInput = this.getClass().getResourceAsStream("config/parameters.yml");

        InputStream schemaInput = this.getClass().getResourceAsStream("config/meraki.sqlite");

        String parametersClassPath = Parameters.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        String binDirectoryPath = parametersClassPath.substring(0, parametersClassPath.indexOf("bin/") + 4);

        boolean parametersExist = new File(binDirectoryPath, "parameters.yml").exists();

        boolean databaseExist = new File(binDirectoryPath, "meraki.db").exists();

        boolean schemaExist = new File(binDirectoryPath, "meraki.sqlite").exists();

        if (!parametersExist) {
            createMissingFile(binDirectoryPath, "parameters.yml", parametersInput);

        } else {
            //If parameters.yml does exist then read file
            try {
                parametersInput = new FileInputStream("parameters.yml");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (!schemaExist) {
            createMissingFile(binDirectoryPath, "meraki.sqlite", schemaInput);

        }
        if (!databaseExist) {

            createDatabase();

        }


        Yaml yaml = new Yaml();
        Map object = (Map) yaml.load(parametersInput);
        Map parameters = (Map) object.get("parameters");
        this.merakiUsername = (String) parameters.get("meraki_username");
        this.merakiPassword = (String) parameters.get("meraki_password");
        this.mailerTransport = (String) parameters.get("mailer_transport");
        this.mailerHost = (String) parameters.get("mailer_host");
        this.mailerFrom = (String) parameters.get("mailer_from");
        this.mailerUsername = (String) parameters.get("mailer_username");
        this.mailerPassword = (String) parameters.get("mailer_password");
        this.mailerPort = (int) parameters.get("mailer_port");
        this.mailerSubject = (String) parameters.get("mailer_subject");
        //this.reportFileLocation = (String) parameters.get("reportFile_location");
        this.jdbcDriver = (String) parameters.get("jdbc_driver");
        this.jdbcUrl = "jdbc:sqlite:" + binDirectoryPath + "meraki.db";

        if (parameters.get("report_now_range") != null) {
            this.reportNowRange = (int) parameters.get("report_now_range");
        } else {
            this.reportNowRange = 30;
        }
    }

    public String getMerakiUsername() {
        return merakiUsername;
    }

    public String getMerakiPassword() {
        return merakiPassword;
    }

    public String getMailerTransport() {
        return mailerTransport;
    }

    public String getMailerHost() {
        return mailerHost;
    }

    public String getMailerFrom() {
        return mailerFrom;
    }

    public String getMailerUsername() {
        return mailerUsername;
    }

    public String getMailerPassword() {
        return mailerPassword;
    }

    public int getMailerPort() {
        return mailerPort;
    }

    public String getReportFileLocation() {
        return reportFileLocation;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public int getReportNowRange() {
        return reportNowRange;
    }

    public String getMailerSubject() {
        return mailerSubject;
    }

    private void createMissingFile(String path, String fileName, InputStream input) {

        OutputStream resStreamOut = null;
        int readBytes;
        byte[] buffer = new byte[4096];
        try {
            resStreamOut = new FileOutputStream(path + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while ((readBytes = input.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fileName.equals("parameters.yml")) {
            try {
                throw new Exception("Fill out parameters.yml, found in JAR directory");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createDatabase() {

        String s = null;

        try {

            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "sqlite3 meraki.db < meraki.sqlite"});

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
