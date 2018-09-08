package com.meraki.automation;

/**
 * Created by miguelmorales on 1/7/16.
 */

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.EmailException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class Automation {
    public static ConnectionSource connectionSource = null;
    public static Dao<Organization, String> organizationDao;
    public static Dao<Network, String> networkDao;
    public static Dao<Device, String> deviceDao;
    public static Dao<StatusReport, String> statusReportDao;
    public static Dao<Users, String> userDao;
    public static Parameters parameters = new Parameters();
    public static WebDriver driver = new FirefoxDriver();
    public static String JDBC_DRIVER = parameters.getJdbcDriver();
    public static String JDBC_URL = parameters.getJdbcUrl();
    public static List<Organization> organizationList = new ArrayList<Organization>();
    public static List<Network> networkList = new ArrayList<Network>();
    public static List<Device> deviceList = new ArrayList<Device>();
    public static int orgCounter = 0;
    public static String currentTimestamp;
    public static String downTimeStamp = null;
    public static int networkId = 1;
    public static boolean generateReport = true;
    public static int deviceCounter = 1;
    public static int startParse;
    public static int endParse;

    enum USERCASES {add, update, delete, list}

    enum COMMANDS {user, report, update, help}


    public static void main(String[] args) throws SQLException {


        try {
            Class.forName(JDBC_DRIVER);

            connectionSource = new JdbcConnectionSource(JDBC_URL);

            ormConfiguration();

            
            if (args.length > 0) {
                determineCommand(args);
            } else {
                System.out.println("Error: No Command was entered!");
                exitProgram();
            }


            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            driver.get("https://account.meraki.com/secure/login/dashboard_login");

            login();


            driver.findElement(By.id("login_form")).findElement(By.tagName("a")).click();


            driver.findElement(By.id("network_overview_list")).findElement(By.tagName("a")).click();


            List<WebElement> allOrganizations = driver.findElement(By.id("select_organization")).findElements(By.tagName("option"));

            String allCurrentUrls[] = new String[allOrganizations.size()];

            //Skips first organization because its a meraki generated organization.
            //Skips the second organization because when first visited the url
            //does not contain proper token.
            for (int organizationCounter = 2; organizationCounter < allOrganizations.size(); ++organizationCounter) {

                //Navigates to next organization overview page.
                allOrganizations.get(organizationCounter).click();


                //Checks if you have been logged out of meraki and if so logs you back in
                if (driver.getCurrentUrl().contains("login/dashboard_login")) {
                    login();

                    List<WebElement> allLoginUrls = driver.findElement(By.id("login_form")).findElements(By.tagName("a"));

                    allLoginUrls.get(organizationCounter - 1).click();


                    //Checks if network overview url link is available
                    // if not uses network drop down to visit overview page
                    if (driver.findElement(By.id("network_overview_list")).findElement(By.tagName("a")).getAttribute("href").contains("/organization/map")) {
                        driver.findElement(By.id("network_overview_list")).findElement(By.tagName("a")).click();
                    } else {
                        driver.findElement(By.id("select_network")).findElement(By.tagName("option")).click();
                    }


                }

                //This sleep makes sure all data needed from the page is loaded
                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                allCurrentUrls[organizationCounter - 1] = driver.getCurrentUrl();

                //Parsing organization token and id from url
                String org_token = allCurrentUrls[organizationCounter - 1].substring(allCurrentUrls[organizationCounter - 1].
                        indexOf("o/") + 2, allCurrentUrls[organizationCounter - 1].indexOf("/m"));

                startParse = allCurrentUrls[organizationCounter - 1].indexOf("//");
                endParse = allCurrentUrls[organizationCounter - 1].indexOf(".m");
                String org_id = allCurrentUrls[organizationCounter - 1].substring(startParse + 2, endParse);

                //Selects current available organization in organization drop down
                List<WebElement> allOrganizationsDropDown = driver.findElement(By.id("select_organization")).findElements(By.tagName("option"));
                String name = allOrganizationsDropDown.get(organizationCounter).getText();

                Organization org = new Organization(org_id, name, org_token);
                org.setId(orgCounter + 1);
                organizationList.add(org);


                organizationDao.assignEmptyForeignCollection(org, "networks");

                //Gains access to network table
                WebElement table = driver.findElement(By.id("network_table")).findElement(By.tagName("table"));
                List<WebElement> allRows = table.findElements(By.tagName("tr"));

                //Checks network table is not equal null
                if (allRows.get(allRows.size() - 1).getAttribute("data-idx") != null) {

                    //Reads network table for proper size
                    int networkTableSize = Integer.parseInt(allRows.get(allRows.size() - 1).getAttribute("data-idx"));

                    //Looping through all networks in an organization and retrieve all network data.

                    for (int networkTableCounter = 0; networkTableCounter <= networkTableSize; ++networkTableCounter) {
                        String networkName = null;
                        String networkStatus = null;

                        if (table.findElements(By.tagName("a")).size() > 0) {
                            networkName = driver.findElement(By.id("row_" + networkTableCounter)).findElement(By.tagName("a")).getText();
                            
                            networkStatus = driver.findElement(By.id("row_" + networkTableCounter)).findElement(By.tagName("a")).getAttribute("href");
                            //Parsing network status to retrieve network token and slug.
                            startParse = networkStatus.indexOf("m/");
                            endParse = networkStatus.indexOf("/n/");
                            String networkSlug = networkStatus.substring(startParse + 2, endParse);
                            String network_token = networkStatus.substring(networkStatus.indexOf("/n/") + 3, networkStatus.indexOf("/ma"));

                            Network network = new Network(organizationList.get(orgCounter), networkId, networkName, networkSlug, network_token);
                            network.setActive(1);


                            networkDao.assignEmptyForeignCollection(network, "devices");

                            networkList.add(network);
                            ++networkId;
                        } else {

                            differentAccess();
                            break;
                        }


                    }
                }


                //Switching from network table to device table.
                int left = allCurrentUrls[organizationCounter - 1].indexOf("=");
                driver.navigate().to(allCurrentUrls[organizationCounter - 1].replace(allCurrentUrls[organizationCounter - 1].substring(left + 1), "device"));

                //Gains acces of device table and reads all tr elements
                WebElement deviceTable = driver.findElement(By.id("device_table")).findElement(By.tagName("table"));
                List<WebElement> allRows2 = deviceTable.findElements(By.tagName("tr"));

                //Checks device table is not equal null
                if (allRows2.get(allRows2.size() - 1).getAttribute("data-idx") != null) {

                    //Reads the proper size of devices in device table

                    int deviceTableSize = Integer.parseInt(allRows2.get(allRows2.size() - 1).getAttribute("data-idx"));

                    //Looping through device table retrieving device data

                    for (int deviceTableCounter = 0; deviceTableCounter <= deviceTableSize; ++deviceTableCounter) {

                        WebElement deviceRow = deviceTable.findElement(By.id("row_" + deviceTableCounter));
                        WebElement deviceAtag = deviceRow.findElement(By.tagName("a"));

                        String deviceUrl = deviceAtag.getAttribute("href");

                        String macAddress = deviceRow.findElement(By.className("mac")).getAttribute("innerHTML");

                        startParse = deviceUrl.indexOf("n=");

                        String deviceStatus = deviceRow.findElement(By.tagName("img")).getAttribute("src");

                        String deviceId = deviceUrl.substring(startParse + 2);
                        String networkToken = deviceUrl.substring(deviceUrl.indexOf("/n/") + 3, deviceUrl.indexOf("/ma"));

                        startParse = deviceUrl.indexOf("m/");
                        endParse = deviceUrl.indexOf("/n/");
                        String networkSlug = deviceUrl.substring(startParse + 2, endParse);
                        Device device = new Device(deviceId, macAddress, deviceStatus, networkToken, networkSlug);

                        deviceList.add(device);

                    }
                }

                allOrganizations = driver.findElement(By.id("select_organization")).findElements(By.tagName("option"));

                //Checks if counter is at the last iteration.
                //If counter is at the last iteration makes counter zero right before
                //increment to go back to the first organization we skipped.
                if (organizationCounter == allOrganizations.size() - 1) {
                    organizationCounter = 0;
                }
                //Checks if we already visited the first organization then the program
                //has visited all organizations. So the counter gets set to the last iteration
                //and the program breaks from the loop.
                else if (organizationCounter == 1) {
                    organizationCounter = allOrganizations.size();
                }
                //Keeps track of organization visit order.
                ++orgCounter;
            }


            //Loops through organization list to determine if any new organizations should be insert to database.
            for (int organizationListCounter = 0; organizationListCounter < organizationList.size(); ++organizationListCounter) {
                QueryBuilder<Organization, String> qb = organizationDao.queryBuilder();

                qb.where().eq("token", organizationList.get(organizationListCounter).getOrganizationToken());

                List<Organization> results = qb.query();


                if (results.size() == 0) {
                    organizationDao.create(organizationList.get(organizationListCounter));
                }

                //checks if an organization has a new network to insert into database
                for (int networkListCounter = 0; networkListCounter < networkList.size(); ++networkListCounter) {

                    matchNetworkIdToDevice(networkListCounter);

                    if (networkList.get(networkListCounter).getOrganization().getId() == organizationList.get(organizationListCounter).getId()) {

                        if (!organizationList.get(organizationListCounter).doesNetworkExist(networkList.get(networkListCounter))) {
                            networkDao.create(networkList.get(networkListCounter));
                        }

                        //checks if a network has a new device to insert into database
                        for (int deviceListCounter = 0; deviceListCounter < deviceList.size(); ++deviceListCounter) {

                            if (networkList.get(networkListCounter).getToken().equals(deviceList.get(deviceListCounter).getNetworkToken())) {

                                deviceList.get(deviceListCounter).setId(deviceCounter);
                                ++deviceCounter;

                                if (!networkList.get(networkListCounter).doesDeviceExist(deviceList.get(deviceListCounter))) {

                                    if (!doesDeviceExistInDatabase(deviceListCounter)) {
                                        deviceDao.create(deviceList.get(deviceListCounter));


                                        if (!deviceList.get(deviceListCounter).getStatus().equals("green")) {
                                            downTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime());

                                            StatusReport statusReport = new StatusReport(deviceList.get(deviceListCounter).getId(), deviceList.get(deviceListCounter).getStatus(), downTimeStamp);
                                            statusReportDao.create(statusReport);
                                        }
                                    }
                                } else {

                                    updateDeviceIfNetworkChanged(deviceListCounter);

                                    QueryBuilder<StatusReport, String> statusReportQB = statusReportDao.queryBuilder();


                                    //checks if device status was down before and hasn't been reported up
                                    if (deviceList.get(deviceListCounter).getStatus().equals("green")) {

                                        statusReportQB.where().eq("deviceId", deviceList.get(deviceListCounter).getId()).and().isNull("reportedUp")
                                                .and().isNotNull("reportedDown");

                                        List<StatusReport> StatResults = statusReportQB.query();
                                        for (StatusReport statusReport : StatResults) {
                                            currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime());

                                            statusReport.setReportedUp(currentTimestamp);
                                            statusReportDao.update(statusReport);

                                        }
                                    } else {
                                        //checks if device has already been reported down, if not inserts downed device
                                        statusReportQB.where().eq("deviceId", deviceList.get(deviceListCounter).getId()).and().isNotNull("reportedDown")
                                                .and().isNull("reportedUp");
                                        List<StatusReport> statusResults = statusReportQB.query();
                                        if (statusResults.size() == 0) {
                                            downTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime());
                                            StatusReport statusReport = new StatusReport(deviceList.get(deviceListCounter).getId(), deviceList.get(deviceListCounter).getStatus(), downTimeStamp);
                                            statusReportDao.create(statusReport);
                                        }

                                    }

                                }
                            }
                        }
                    }
                }
            }

            checkNetworkStatus();

            if (generateReport) {

                createHtmlReport(true);
            } else {
                System.out.println("Database has been updated.");
            }


            connectionSource.close();

            driver.quit();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void login() {

        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys(parameters.getMerakiUsername());
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(parameters.getMerakiPassword());
        driver.findElement(By.id("commit")).click();
    }


    public static void createHtmlReport(boolean sendEmail) throws SQLException {

        Map<String, Object> root = new HashMap<String, Object>();
        root.put("oldDownNetworks", getOldDownNetworks());
        root.put("newDownNetworks", getNewDownNetworks());
        root.put("upNetworks", getUpNetworks());


        /* Get the template (uses cache internally) */
        Template temp = new CreateHtmlTemplate().CreateTemplate();

        /* Merge data-model with template */
        Writer out = new OutputStreamWriter(System.out);

        try {
            //temp.process(root, out);
            Writer file = new FileWriter(new File(System.getProperty("java.io.tmpdir") + "/Report.html"));
            temp.process(root, file);


        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (sendEmail) {
                generateAndSendEmail();
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (EmailException e) {
            e.printStackTrace();
        }


    }

    public static List<ReportedNetworks> findOldNetworks() throws SQLException {

        downTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime() - TimeUnit.MINUTES.toMillis(60));


        QueryBuilder<StatusReport, String> statusReportQB = statusReportDao.queryBuilder();

        statusReportQB.where().isNull("reportedUp").and().lt("reportedDown", downTimeStamp);
        List<StatusReport> statusReportResults = statusReportQB.query();


        return getReportedNetworkList(statusReportResults, false);


    }

    public static List<ReportedNetworks> getOldDownNetworks() throws SQLException {

        List<ReportedNetworks> results = findOldNetworks();
        boolean deviceExist = false;

        for (ReportedNetworks result : results) {

            String currentDeviceMacAddress = result.getMacAddress();
            for (int i = 0; i < deviceList.size(); ++i) {
                if (deviceList.get(i).getMac().equals(currentDeviceMacAddress)) {
                    deviceExist = true;
                }
            }
            if (!deviceExist) {
                updateDeviceUp(Integer.parseInt(result.getDeviceNumber()));

            } else {
                deviceExist = false;
            }

        }

        List<ReportedNetworks> newResults = findOldNetworks();

        return newResults;

    }

    public static List<ReportedNetworks> getNewDownNetworks() throws SQLException {
        downTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime() - TimeUnit.MINUTES.toMillis(60));
        QueryBuilder<StatusReport, String> statusReportQB = statusReportDao.queryBuilder();

        statusReportQB.where().isNull("reportedUp").and().ge("reportedDown", downTimeStamp);
        List<StatusReport> statusReportResults = statusReportQB.query();

        return getReportedNetworkList(statusReportResults, false);
    }

    public static List<ReportedNetworks> getUpNetworks() throws SQLException {

        currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime() - TimeUnit.MINUTES.toMillis(30));

        QueryBuilder<StatusReport, String> statusReportQB = statusReportDao.queryBuilder();

        statusReportQB.where().ge("reportedUp", currentTimestamp);
        List<StatusReport> statusReportResults = statusReportQB.query();

        return getReportedNetworkList(statusReportResults, true);
    }

    public static List<ReportedNetworks> getReportedNetworkList(List<StatusReport> statusReportResults, boolean uptime) throws SQLException {
        //Queries all tables looking for status report information

        QueryBuilder<Device, String> deviceQB = deviceDao.queryBuilder();
        QueryBuilder<Network, String> networkQB = networkDao.queryBuilder();
        QueryBuilder<Organization, String> organizationQB = organizationDao.queryBuilder();

        List<Device> deviceResults = null;
        List<Network> networkResults = null;
        List<Organization> organizationResults;
        List<ReportedNetworks> reportedNetworksList = new ArrayList<>();

        for (StatusReport statusReport : statusReportResults) {
            ReportedNetworks reportedNetworks = new ReportedNetworks();
            reportedNetworks.setDeviceNumber("" + statusReport.getDeviceId());
            reportedNetworks.setReportedDownTime(statusReport.getReportedDown());

            if (uptime) {
                reportedNetworks.setReportedUpTime(statusReport.getReportedUp());
            }
            deviceQB.where().eq("id", statusReport.getDeviceId());
            deviceResults = deviceQB.query();

            for (Device device : deviceResults) {
                reportedNetworks.setMacAddress(device.getMac());
                networkQB.where().eq("id", device.getNetwork().getId());
                networkResults = networkQB.query();

                for (Network network : networkResults) {
                    reportedNetworks.setNetworkName(network.getName());
                    organizationQB.where().eq("id", network.getOrganization().getId());
                    organizationResults = organizationQB.query();

                    for (Organization organization : organizationResults) {
                        reportedNetworks.setOrganizationName(organization.getName());
                    }
                }
            }
            reportedNetworksList.add(reportedNetworks);
        }

        return reportedNetworksList;

    }

    public static void generateAndSendEmail() throws MessagingException, EmailException {
        String FROM = parameters.getMailerFrom();

        // Replace with your "From" address. This address must be verified.

        String SUBJECT = parameters.getMailerSubject();

        // Supply your SMTP credentials below. Note that your SMTP credentials are different from your AWS credentials.
        String SMTP_USERNAME = parameters.getMailerUsername();

        // Replace with your SMTP username.
        String SMTP_PASSWORD = parameters.getMailerPassword();

        // Replace with your SMTP password.

        // Amazon SES SMTP host name. This example uses the US West (Oregon) region.
        String HOST = parameters.getMailerHost();


        // Port we will connect to on the Amazon SES SMTP endpoint. We are choosing port 25 because we will use
        // STARTTLS to encrypt the connection.
        int PORT = parameters.getMailerPort();


        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");

        props.put("mail.smtp.port", PORT);

        // Set properties indicating that we want to use STARTTLS to encrypt the connection.
        // The SMTP session will begin on an unencrypted connection, and then the client
        // will issue a STARTTLS command to upgrade to an encrypted connection.
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.

        //Convert html page into body of email
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(new FileInputStream(new File(System.getProperty("java.io.tmpdir") + "/Report.html")), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Users> users = new ArrayList<Users>();
        try {
            users.addAll(getUsers());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int userCounter = 0; userCounter < users.size(); ++userCounter) {
            String TO = users.get(userCounter).getEmail();

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(FROM));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(TO));
            msg.setSubject(SUBJECT);

            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            BodyPart messageBodyPart2 = new MimeBodyPart();
            messageBodyPart.setContent(writer.toString(), "text/html");
            messageBodyPart2.setHeader("content-type", "text/xls");
            messageBodyPart2.setContent(writer.toString(), "text/xls");
            messageBodyPart2.setFileName("Report.xls");
            multipart.addBodyPart(messageBodyPart2);

            File file = new File(System.getProperty("java.io.tmpdir") + "/Report.html");
            file.toString();
            messageBodyPart = new MimeBodyPart();

            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));

            multipart.addBodyPart(messageBodyPart);
            msg.setContent(multipart);

            // Create a transport.
            Transport transport = session.getTransport();

            // Send the message.
            try {
                System.out.println("Attempting to send an email through the Amazon SES SMTP interface...");

                // Connect to Amazon SES using the SMTP username and password you specified above.
                transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

                // Send the email.
                transport.sendMessage(msg, msg.getAllRecipients());
                System.out.println("Email sent!");
            } catch (Exception ex) {
                System.out.println("The email was not sent.");
                System.out.println("Error message: " + ex.getMessage());
            } finally {
                // Close and terminate the connection.
                transport.close();
            }
        }

    }

    public static List<Users> getUsers() throws SQLException {
        QueryBuilder<Users, String> usersQB = userDao.queryBuilder();
        List<Users> userResults = usersQB.query();
        return userResults;
    }


    public static void insertUser(Users user) throws SQLException {
        QueryBuilder<Users, String> userQB = userDao.queryBuilder();

        userQB.where().eq("email", user.getEmail());
        List<Users> userResults = userQB.query();
        if (userResults.size() == 0) {
            userDao.create(user);
        }

    }

    public static void deleteUser(String email) throws SQLException {
        QueryBuilder<Users, String> userQB = userDao.queryBuilder();
        userQB.where().eq("email", email);
        List<Users> userResults = userQB.query();
        userDao.delete(userResults.get(0));

    }

    public static void matchNetworkIdToDevice(int index) throws SQLException {

        for (int deviceCounter = 0; deviceCounter < networkList.size(); ++deviceCounter) {

            if (networkList.get(index).getToken().equals(deviceList.get(deviceCounter).getNetworkToken())) {

                deviceList.get(deviceCounter).setNetwork(networkList.get(index));

                networkList.get(index).setSlug(deviceList.get(deviceCounter).getNetworkSlug());

            }

        }
    }

    public static void addUser(String userEmail, String userName) throws SQLException {
        Users user = new Users(userName, userEmail);
        user.setActive(1);
        insertUser(user);
        if (checkUserExist(userEmail)) {
            System.out.println("User has been added.");
        }
    }

    public static void showUsers() throws SQLException {
        boolean dbIsEmpty = true;
        QueryBuilder<Users, String> userQB = userDao.queryBuilder();

        List<Users> userResults = userQB.query();
        System.out.println("All currently available users.");

        for (Users user : userResults) {
            System.out.println("Name: " + user.getName() + ", Email: " + user.getEmail() + ", Active Status: " +
                    user.getActive());
            dbIsEmpty = false;
        }
        if (dbIsEmpty) {
            System.out.println("User Table is Empty");
        }
    }

    public static void updateUser(String email, int status) throws SQLException {
        QueryBuilder<Users, String> userQB = userDao.queryBuilder();
        userQB.where().eq("email", email);
        List<Users> userResults = userQB.query();
        if (userResults.size() != 0) {
            userResults.get(0).setActive(status);
            userDao.update(userResults.get(0));
        }

    }

    public static void exitProgram() {
        driver.quit();
        System.exit(0);
    }

    public static boolean checkUserExist(String email) throws SQLException {
        boolean userFound = false;

        QueryBuilder<Users, String> usersQB = userDao.queryBuilder();

        usersQB.where().eq("email", email);
        List<Users> userResults = usersQB.query();
        if (userResults.size() != 0) {
            userFound = true;
        }

        return userFound;
    }

    public static void updateDeviceUp(int deviceId) throws SQLException {
        currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());

        QueryBuilder<StatusReport, String> statusReportQB = statusReportDao.queryBuilder();

        statusReportQB.where().eq("deviceId", deviceId).and().isNull("reportedUp");
        List<StatusReport> statusReportsResults = statusReportQB.query();
        statusReportsResults.get(0).setReportedUp(currentTimestamp);
        statusReportDao.update(statusReportsResults.get(0));
    }


    public static void determineCommand(String[] arg) throws SQLException {

        String name = null;
        String email = null;
        int status;

        USERCASES userCases;
        COMMANDS commands = COMMANDS.valueOf(arg[0]);

        switch (commands) {
            case user:
                userCases = USERCASES.valueOf(arg[1]);
                switch (userCases) {
                    case add:
                        email = arg[2];
                        name = arg[3];
                        addUser(email, name);
                        break;
                    case update:
                        email = arg[2];
                        status = Integer.parseInt(arg[3]);
                        if (checkUserExist(email)) {
                            updateUser(email, status);
                        }
                        break;
                    case delete:
                        email = arg[2];
                        if (checkUserExist(email)) {
                            deleteUser(email);
                        }
                        break;
                    case list:
                        showUsers();
                        break;
                    default:
                        System.out.println("Error: Command entered is not valid");
                        helpMenu();
                        break;
                }
                break;
            case report:
                return;

            case update:
                generateReport = false;
                return;

            case help:
                helpMenu();
                break;
            default:
                System.out.println("Error: Command entered is not valid");
                helpMenu();
                break;
        }

        exitProgram();

    }

    public static void helpMenu() {
        System.out.println("1) user add <email> <name>: adds user to database.");
        System.out.println("2) user update <email> <status>: updates user status in database.");
        System.out.println("3) user delete <email>: delete user from database.");
        System.out.println("4) user list: shows all users in database");
        System.out.println("5) report create: runs through meraki and generates report and sends email");
        System.out.println("6) update: runs through meraki and updates database but doesn't generate report or email");
        System.out.println("7) help: shows all available commands.");


    }

    public static void checkNetworkStatus() throws SQLException {
        //checks if any network is not active anymore and sets the network to
        //inactive in the network table.

        QueryBuilder<Network, String> networkQB = networkDao.queryBuilder();

        List<Network> networkResults = networkQB.query();
        boolean networkNotFound = true;

        for (Network network : networkResults) {

            for (Network network2 : networkList) {

                if (network2.getToken().equals(network.getToken())) {
                    networkNotFound = false;
                }
            }

            if (networkNotFound) {
                network.setActive(0);
                networkDao.update(network);

            }
            networkNotFound = true;

        }


    }


    public static boolean updateDeviceIfNetworkChanged(int index) throws SQLException {
        QueryBuilder<Device, String> deviceQB = deviceDao.queryBuilder();

        deviceQB.where().eq("deviceId", deviceList.get(index).getDeviceId());

        List<Device> devicesResults = deviceQB.query();

        if (devicesResults.get(0).getNetwork().getId() != deviceList.get(index).getNetwork().getId()) {
            deviceDao.update(deviceList.get(index));
            return false;
        }
        return true;
    }

    public static boolean doesDeviceExistInDatabase(int index) throws SQLException {


        QueryBuilder<Device, String> deviceQB = deviceDao.queryBuilder();

        deviceQB.where().eq("deviceId", deviceList.get(index).getDeviceId());

        List<Device> devicesResults = deviceQB.query();

        if (devicesResults.size() != 0) {

            return true;
        }

        return false;

    }


    public static void ormConfiguration() throws SQLException {

        organizationDao = DaoManager.createDao(connectionSource, Organization.class);
        networkDao = DaoManager.createDao(connectionSource, Network.class);
        deviceDao = DaoManager.createDao(connectionSource, Device.class);
        statusReportDao = DaoManager.createDao(connectionSource, StatusReport.class);
        userDao = DaoManager.createDao(connectionSource, Users.class);
        TableUtils.createTableIfNotExists(connectionSource, Organization.class);
        TableUtils.createTableIfNotExists(connectionSource, Network.class);
        TableUtils.createTableIfNotExists(connectionSource, Device.class);
        TableUtils.createTableIfNotExists(connectionSource, StatusReport.class);
        TableUtils.createTableIfNotExists(connectionSource, Users.class);
    }

    public static void differentAccess() throws SQLException {
        WebElement table = driver.findElement(By.id("select_network"));
        List<WebElement> allRows = table.findElements(By.tagName("option"));

        for (int rowCounter = 0; rowCounter < allRows.size(); ++rowCounter) {

            String networkName = allRows.get(rowCounter).getText();
            String networkStatus = allRows.get(rowCounter).getAttribute("value");

            startParse = networkStatus.indexOf("g-");

            String networkSlug = networkName;
            String network_token = networkStatus.substring(startParse + 2);

            Network network = new Network(organizationList.get(orgCounter), networkId, networkName, networkSlug, network_token);
            network.setActive(1);


            networkDao.assignEmptyForeignCollection(network, "devices");

            networkList.add(network);
            ++networkId;

        }

    }



}
