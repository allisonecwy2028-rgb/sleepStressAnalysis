import java.sql.*;
import java.util.Scanner;

public class Main {
    private static void Spec1(Connection conn) throws SQLException{
        System.out.println("\nSPEC1 TEST: CSV Load Check");
        String fetchFirstTwo = "SELECT * FROM sleep LIMIT 2";
        String totalRowsSQL = "SELECT COUNT(*) AS Total_Loaded_Records FROM sleep";
        System.out.println("First two loaded CSV records:");
        runSQLandPrint(fetchFirstTwo, conn);
        runSQLandPrint(totalRowsSQL, conn);
    }
    private static void Spec4(String selectedOption){
        System.out.println("\n[SPEC4 NAV TEST] Navigating to " + switch(selectedOption){
            case "1" -> "Search";
            case "2" -> "SleepDebt";
            case "3" -> "OccupationAnalysis";
            case "4" -> "RiskAssessment";
            default -> "UndefinedScreen";
        });
    }

    private static boolean runSQLandPrint(String query, Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData metaData = rs.getMetaData();
        int cols = metaData.getColumnCount();   //get the number of columns

        if (!rs.isBeforeFirst()) {
            System.out.println("\n(!) Error: No records found. Please try a valid input");
            return false;
        }
        int[] colWidths = new int[cols];
        //initialize column widths with header lengths
        for (int i=0; i < cols; i++) {
            colWidths[i] = metaData.getColumnLabel(i+1).length();
        }
        //loop through results to get maximum lengths in each column
        while (rs.next()) {
            for (int i=0; i < colWidths.length; i++) {
                try {
                    int len = rs.getString(i+1).length();
                    if (len > colWidths[i])
                        colWidths[i] = len;
                } catch (NullPointerException e) {
                    //getString() can return NULL, so ignore them
                }
            }
        }
        //re-run query to return to the first row
        rs = stmt.executeQuery(query);
        metaData = rs.getMetaData();

        //print out column headers
        for (int i=1; i <= cols; i++) {
            System.out.printf("%-" + (colWidths[i-1]+1) + "s", metaData.getColumnLabel(i));
        }
        System.out.println();
        //print each row
        while (rs.next()) {
            for (int i=1; i <= cols; i++) {
                System.out.printf("%-" + (colWidths[i-1]+1) + "s", rs.getString(i));
            }
            System.out.println();
        }
        return true;
    }

    public static void main(String[] args) {
        String url = "jdbc:sqlite:identifier.sqlite";
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(url)) {
            //Spec1(conn); //spec1 test
            boolean running = true;
            while (running) {
                System.out.println("\n=== STRESS & SLEEP ANALYSIS ===");
                System.out.println("\n       -- MAIN MENU --");
                System.out.println("1. Search by Occupation");
                System.out.println("2. Sleep Debt Calculator");
                System.out.println("3. Occupation Analysis");
                System.out.println("4. Risk Assessment");
                System.out.println("5. Exit");
                System.out.print("Choice: ");
                //System.out.println("[SPEC3 TEST] Menu is working"); //spec3

                String choice = sc.nextLine();
                if (choice.equals("5")) running = false;
                else handleChoice(choice, conn, sc);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    public static void handleChoice(String choice, Connection conn, Scanner sc) throws SQLException {
        //Spec4(choice); //spec4 test
        boolean runSuccess = false;

        switch (choice) {
            case "1": //job search
                System.out.print("Enter occupation: ");
                String job = sc.nextLine();

                // check for blank entries (if the user just clicked enter)
                if (job.trim().isEmpty()) {
                    System.out.println("\n(!) Error: Occupation cannot be left blank");
                    break;
                }

                /*
                System.out.println("\n==== SPEC5 Filter Count Test ===="); //spec5
                String spec5NormalSQL = "SELECT COUNT(*) AS FilterCount FROM sleep WHERE occupation='Software Engineer'";
                System.out.println("[Normal Test: Software Engineer, Age 20~30 Expected Count]");
                runSQLandPrint(spec5NormalSQL, conn);
                System.out.println("Compare above count vs app displayed total results");

                 */




                /*
                System.out.println("\n[SPEC6 OCCUPATION SEARCH TEST]"); //spec6
                String spec6Normal = "SELECT * FROM sleep WHERE occupation='Designer' LIMIT 1";
                System.out.println("Expected First Row for Designer from Test Query:");
                runSQLandPrint(spec6Normal, conn);
                System.out.println("Check app search result matches above line");

                 */


                String searchSQL = "SELECT occupation, ROUND(AVG(stress_level), 1) AS Avg_Stress, COUNT(*) AS Total_Records " +
                        "FROM sleep WHERE occupation LIKE '%" + job + "%' " +
                        "GROUP BY occupation";

                System.out.println("\n--- Search Results for: " + job + " ---");
                // only success if valid table was outputted
                runSuccess = runSQLandPrint(searchSQL, conn); //
                break;

            case "2": // sleep debt
                System.out.print("How many hours do you sleep? ");
                String input = sc.nextLine();

                // check for empty strings
                if (input.trim().isEmpty()) {
                    System.out.println("\n(!) Error: Sleep duration cannot be blank");
                    break;
                }

                try {
                    double userSleep = Double.parseDouble(input);
                    ResultSet rs = conn.createStatement().executeQuery("SELECT AVG(sleep_duration_hours) FROM sleep");

                    double avgSleep = 0;
                    if (rs.next()) {
                        avgSleep = rs.getDouble(1);
                    }


                    /*
                    System.out.println("\n[SPEC10 Global Sleep Avg Test]"); //spec10
                    String spec10SQL = "SELECT AVG(sleep_duration_hours) AS GlobalAvg FROM sleep";
                    runSQLandPrint(spec10SQL, conn);
                    System.out.println("Confirm above value matches app's displayed global average");

                     */


                    double debt = avgSleep - userSleep;
                    System.out.printf("The average person sleeps %.2f hours. ", avgSleep);
                    if(debt > 0) System.out.printf("You have a sleep debt of %.2f hours!%n", debt);
                    else System.out.println("You are sleeping better than average!");

                    runSuccess = true; // table was successfully printed, allow it to continue
                } catch (NumberFormatException e) {
                    System.out.println("(!) Error: Please enter a valid number for sleep hours");
                }
                break;

            case "3": // top 5 occupations in diff categories
                /*
                System.out.println("\n===== SPEC2 TEST: Occupation Top5 Stress Check ====="); //spec2 test
                String spec2HighTest = "SELECT occupation, ROUND(AVG(stress_level),1) AS AvgStress FROM sleep GROUP BY occupation ORDER BY AvgStress DESC LIMIT 5";
                String spec2LowTest = "SELECT occupation, ROUND(AVG(stress_level),1) AS AvgStress FROM sleep GROUP BY occupation ORDER BY AvgStress ASC LIMIT 5";
                System.out.println("Expected Top5 High Stress from Test Query:");
                runSQLandPrint(spec2HighTest, conn);
                System.out.println("Expected Top5 Low Stress from Test Query:");
                runSQLandPrint(spec2LowTest, conn);

                 */



                System.out.println("\n-- OCCUPATION ANALYSIS LEADERBOARD (TOP 5) --");

                System.out.println("\nHIGHEST STRESS LEVELS");
                String highStress = "SELECT occupation, ROUND(AVG(stress_level), 1) AS 'Average Stress' " +
                        "FROM sleep GROUP BY occupation ORDER BY 2 DESC LIMIT 5";
                runSQLandPrint(highStress, conn);

                System.out.println("\nLEAST PHYSICALLY ACTIVE");
                String lowActivity = "SELECT occupation, ROUND(AVG(physical_activity_minutes), 1) AS 'Average Activity (Min)' " +
                        "FROM sleep GROUP BY occupation ORDER BY 2 ASC LIMIT 5";
                runSQLandPrint(lowActivity, conn);

                runSuccess = true; // always pause until user wants to continue
                break;

            case "4": // risk assessment
                try {
                    System.out.println("\n-- STRESS RISK CALCULATOR --");

                    System.out.print("Enter your Age: ");
                    String ageInput = sc.nextLine();
                    if (ageInput.trim().isEmpty()) { System.out.println("\n(!) Error: Input cannot be blank"); break; }
                    int age = Integer.parseInt(ageInput);

                    System.out.print("Enter your daily Screen Time (hours): ");
                    String screenInput = sc.nextLine();
                    if (screenInput.trim().isEmpty()) { System.out.println("\n(!) Error: Input cannot be blank"); break; }
                    double screen = Double.parseDouble(screenInput);

                    System.out.print("On a scale of 1-10, how stressed do you feel? ");
                    String stressInput = sc.nextLine();
                    if (stressInput.trim().isEmpty()) { System.out.println("\n(!) Error: Input cannot be blank"); break; }
                    int userStress = Integer.parseInt(stressInput);


                    /*
                    System.out.println("[SPEC11 INPUT TEST] Age entered = "+age); //spec11
                    System.out.println("[SPEC11 INPUT TEST] ScreenTime entered = "+screen);
                    System.out.println("[SPEC11 INPUT TEST] Stress entered = "+userStress);

                     */



                    System.out.println("\nCalculating results against 15,000 records...");


                    /*
                    System.out.println("\n[SPEC12 RISK CATEGORIZATION TEST]"); //spec12
                    int testAge=24; double testScreen=7; int testStress=9;
                    System.out.printf("Test Input: Age%d, Screen%.1fh, Stress%d → Expected High Risk%n",testAge,testScreen,testStress);

                     */


                    if (userStress >= 8 && screen > 6) {
                        System.out.println("--> RESULT: HIGH RISK - Oh no! You have a high risk of being stressed!");
                    } else if (userStress >= 5 || screen > 4) {
                        System.out.println("--> RESULT: MODERATE RISK - Consider reducing screen time before bed!");
                    } else {
                        System.out.println("--> RESULT: LOW RISK - Congrats! You are maintaining a stress-free lifestyle!");
                    }

                    runSuccess = true; // complete, pause to show result
                } catch (NumberFormatException e) {
                    System.out.println("(!) Error: Please enter valid numbers for age, screen time, and stress");
                }
                break;
        }

        // only happens if it displayed the correct thing
        if (runSuccess) {
            System.out.println("\nPress Enter to return to the Main Menu...");
            sc.nextLine();
        }
    }
}