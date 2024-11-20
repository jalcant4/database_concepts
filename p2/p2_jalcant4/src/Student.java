import java.io.*;
/*
 It provides various methods and interfaces for easy communication with the database.
 It provides two packages as follows, which contain the java SE and Java EE platforms
 to exhibit WORA(write once run anywhere) capabilities.
 */
import java.sql.*;
import java.util.*;
import org.apache.ibatis.jdbc.ScriptRunner;

public class Student{
    static Scanner scanner;
    static Connection connection;
    static Statement statement;
    static Map<String, String> urlMap = new HashMap<>();
    static Set<String> filePaths = new HashSet<>();
    static boolean run = true;
    static int loginCounter = 0;

    public static void main(String argv[])
    {
        scanner = new Scanner(System.in);
        try {
            connectToDatabase();
            readSQLScript();
            while (run)
                menu();
        } finally {
            cleanUp();
        }
    }

    public static void connectToDatabase()
    {
        String driverPrefixURL="jdbc:oracle:thin:@";
        String jdbc_url="artemis.vsnet.gmu.edu:1521/vse18c.vsnet.gmu.edu";


        // prompt the user for log in information
        String username= "jalcant4";
        String password= "gloanuhu";

        System.out.print("Enter your username: ");
        if (scanner.hasNext())
            username= scanner.nextLine();
        System.out.print("Enter your password: ");
        if (scanner.hasNext())
            password= scanner.nextLine();

        try{
	    //Register Oracle driver
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (Exception e) {
            System.out.println("Failed to load JDBC/ODBC driver.");
            return;
        }

       try{
            System.out.println(driverPrefixURL+jdbc_url);
            // create a connection
            connection=DriverManager.getConnection(driverPrefixURL+jdbc_url, username, password);
            DatabaseMetaData databaseMetaData=connection.getMetaData();
            statement=connection.createStatement();

            System.out.println("Connected.");

            if(databaseMetaData==null){
                System.out.println("No database meta data");
            }
            else {
                System.out.println("Database Product Name: "+databaseMetaData.getDatabaseProductName());
                System.out.println("Database Product Version: "+databaseMetaData.getDatabaseProductVersion());
                System.out.println("Database Driver Name: "+databaseMetaData.getDriverName());
                System.out.println("Database Driver Version: "+databaseMetaData.getDriverVersion());
            }
        }catch( Exception e) {
           System.out.println("Failed to connect to the database: " + ++loginCounter);
           if (loginCounter < 3)
               connectToDatabase();
           else {
               System.out.println("Quitting");
               System.exit(0);
           }
           // e.printStackTrace();
       }
    }// End of connectToDatabase()

    public static void readSQLScript() {
        try {
            if (connection != null) {
                System.out.print("Enter the sql filepath: ");
                String script = "";
                if (scanner.hasNextLine())
                    script = scanner.nextLine();
                /*
                    https://www.tutorialspoint.com/how-to-run-sql-script-using-jdbc
                    init the script runner
                 */
                ScriptRunner scriptRunner = new ScriptRunner(connection);
                // create a reader object
                Reader reader = new BufferedReader(new FileReader(script));
                scriptRunner.runScript(reader);
            }
        } catch ( Exception e) {
            System.out.println("Invalid script.");
            // e.printStackTrace();
            readSQLScript();
        }
    } // End of readSQLScript

    public static void cleanUp() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    } // End of cleanUp

    public static void menu() {
        try {
            String choice = "";
            // print menu
            String menu =
                "Enter a numeric command:\n" +
                "1. View tables\n" +
                "2. Search by publication id\n" +
                "3. Update url by publication id\n" +
                "4. Exit\n"+
                "Choice: ";
            System.out.println(menu);
            if (scanner.hasNextLine())
                // read the input
                choice = scanner.nextLine();
            // choices
            switch (choice) {
                case "1" ->
                    // SELECT * from tables
                    viewTables();
                case "2" ->
                    // SELECT * FROM tables WHERE PUBLICATIONID = n
                    viewTablesByPublicationID();
                case "3" ->
                    updatesTablesByURL();
                case "4" ->
                    run = false;
                default -> System.out.println("Enter a valid numeric option.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }// End of menu

    private static void updatesTablesByURL() {
        try {
            System.out.println("Enter the url.csv filepath: ");
            String urlFilepath = "";
            if (scanner.hasNextLine())
                urlFilepath = scanner.nextLine();
            // check if its stored
            if (!filePaths.contains(urlFilepath)) {
                // read from the file
                BufferedReader reader = new BufferedReader(new FileReader(urlFilepath));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Split the line into fields using a comma as the separator
                    String[] fields = line.split(",");
                    urlMap.put(fields[0], fields[1]);
                }
                filePaths.add(urlFilepath);
            }
            // get the Publication ID
            System.out.println("Publication ID? ");
            String pubID = "";
            if (scanner.hasNextLine())
                pubID = scanner.nextLine().toLowerCase();
            // update the url
            if (urlMap.containsKey(pubID)) {
                String url = urlMap.get(pubID);
                String query = "UPDATE PUBLICATIONS SET URL='" + url + "' WHERE PUBLICATIONID='" + pubID + "'";
                statement.executeQuery(query);
                System.out.println("URL updated successfully.");
            }
            // reprint w/ updated Publication ID
            String query = "SELECT * FROM PUBLICATIONS WHERE PUBLICATIONID= '" + pubID + "'";
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                System.out.print(
                        "PUBLICATIONID: " + rs.getString("PUBLICATIONID") +
                                ", YEAR: " + rs.getString("YEAR") +
                                ", TYPE: " + rs.getString("TYPE") +
                                ", TITLE: " + rs.getString("TITLE")
                );
                if (rs.getString("URL") != null) {
                    System.out.print(", URL: " + rs.getString("URL") + "\n");
                } else System.out.print("\n");
            } else System.out.print("No Pubication(s) w/ ID: " + pubID);
        } catch (IOException e) {
            System.out.println("File not found exception.");
            // e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Failed to query.");
            // e.printStackTrace();
        }
    }

    private static void viewTablesByPublicationID() {
        try {
            boolean question = true;
            // get publication ID
            System.out.println("Publication ID? ");
            String pubID = "";
            if (scanner.hasNextLine())
                pubID = scanner.nextLine().toLowerCase();
            // query Publications
            String query = "SELECT * FROM PUBLICATIONS WHERE PUBLICATIONID= '" + pubID + "'";
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                System.out.print(
                        "PUBLICATIONID: " + rs.getString("PUBLICATIONID") +
                                ", YEAR: " + rs.getString("YEAR") +
                                ", TYPE: " + rs.getString("TYPE") +
                                ", TITLE: " + rs.getString("TITLE")
                );
                if (rs.getString("URL") != null) {
                    System.out.print(", URL: " + rs.getString("URL") + "\n");
                } else System.out.print("\n");
            } else System.out.print("No Pubication(s) w/ ID: " + pubID);
            // query Authors
            query = "SELECT COUNT(*) AS NUM FROM AUTHORS WHERE PUBLICATIONID = " + pubID;
            rs = statement.executeQuery(query);
            if (rs.next()) {
                System.out.println("Number of authors: " + rs.getString("NUM"));
                /*
                while (rs.next()) {
                    System.out.println(
                            "PUBLICATIONID:" + rs.getString("PUBLICATIONID") +
                                    ", AUTHOR:" + rs.getString("AUTHOR")
                    );
                }
                 */
            } else System.out.println(", no Authors w/ ID: " + pubID);
        } catch (SQLException e) {
            System.out.println("Failed to query.");
            // e.printStackTrace();
        }
    }// viewTablesByPublicationID

    private static void viewTables() {
        try {
            boolean question = true;
            boolean printPublications = false;
            boolean printAuthors = false;
            // question publications
            while (question) {
                System.out.println("PUBLICATIONS (YES/NO)? ");
                String resPublications = "";
                if (scanner.hasNextLine())
                    resPublications = scanner.nextLine().toLowerCase();
                switch (resPublications) {
                    case "yes", "y" -> {
                        printPublications = true;
                        question = false;
                    }
                    case "no", "n" -> {
                        question = false;
                    }
                    default -> System.out.println("Enter a valid input.\n");
                }
            }
            // question authors
            question = true;
            while (question) {
                System.out.println("AUTHORS (YES/NO)? ");
                String resAuthors = "";
                if (scanner.hasNextLine())
                    resAuthors = scanner.nextLine().toLowerCase();
                switch (resAuthors) {
                    case "yes", "y" -> {
                        printAuthors = true;
                        question = false;
                    }
                    case "no", "n" -> {
                        question = false;
                    }
                    default -> System.out.println("Enter a valid input.\n");
                }
            }
            // print the info
            if (printPublications) {
               String query = "Select * from PUBLICATIONS";
               ResultSet rs = statement.executeQuery(query);
               // print the result set
                while (rs.next()) {
                    System.out.print(
                            "PUBLICATIONID: " + rs.getString("PUBLICATIONID") +
                            ", YEAR: " + rs.getString("YEAR") +
                            ", TYPE: " + rs.getString("TYPE") +
                            ", TITLE: " + rs.getString("TITLE")
                    );
                    if (rs.getString("URL") != null) {
                        System.out.print(", URL: " + rs.getString("URL") + "\n");
                    }
                    else System.out.print("\n");
                }
            }
            if (printAuthors) {
                String query = "Select * from AUTHORS";
                ResultSet rs = statement.executeQuery(query);
                // print the result set
                while (rs.next()) {
                    System.out.println(
                            "PUBLICATIONID:" + rs.getString("PUBLICATIONID") +
                            ", AUTHOR:" + rs.getString("AUTHOR")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to query.");
            // e.printStackTrace();
        }
    }// End of viewTables
}// End of class
