
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;

public class B4Migration {

    private static final String URL =
            "jdbc:mysql://localhost:3306/csa09";

    private static final String USER = "root";

    // Enter your MySQL password here
    private static final String PASSWORD = "";

    public static void main(String[] args) {

        String inputFile = "buspasses.csv";
        String rejectFile = "rejected_records.csv";

        int accepted = 0;
        int rejected = 0;

        String createTable =
                "CREATE TABLE IF NOT EXISTS migrated_passes (" +
                "pass_id VARCHAR(20) PRIMARY KEY, " +
                "reg_no VARCHAR(20), " +
                "name VARCHAR(100), " +
                "route_no INT, " +
                "boarding_stop VARCHAR(100), " +
                "validity_date DATE, " +
                "amount_paid DOUBLE)";

        String insertSQL =
                "INSERT INTO migrated_passes VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con =
                    DriverManager.getConnection(URL, USER, PASSWORD);
             BufferedReader reader = Files.newBufferedReader(
                    Path.of(inputFile), StandardCharsets.UTF_8);
             BufferedWriter rejectWriter = Files.newBufferedWriter(
                    Path.of(rejectFile), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

            System.out.println("MySQL connected successfully!");

            try (Statement st = con.createStatement()) {
                st.executeUpdate(createTable);
            }

            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(insertSQL)) {

                String line;

                while ((line = reader.readLine()) != null) {

                    try {
                        String[] f = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        if (f.length != 7) {
                            throw new IllegalArgumentException(
                                    "Incorrect number of fields");
                        }

                        for (int i = 0; i < f.length; i++) {
                            f[i] = f[i].trim().replaceAll("^\"|\"$", "")
                                    .replace("\"\"", "\"");
                        }

                        if (line.isBlank()) {
                            throw new IllegalArgumentException("Empty record");
                        }

                        if (f[0].isBlank() || f[1].isBlank()
                                || f[2].isBlank() || f[4].isBlank()
                                || f[5].isBlank()) {
                            throw new IllegalArgumentException(
                                    "Required field is empty");
                        }

                        int route = Integer.parseInt(f[3]);
                        double amount = Double.parseDouble(f[6]);

                        if (route <= 0 || amount < 0
                                || !Double.isFinite(amount)) {
                            throw new IllegalArgumentException(
                                    "Invalid route or amount");
                        }

                        ps.setString(1, f[0]);
                        ps.setString(2, f[1]);
                        ps.setString(3, f[2]);
                        ps.setInt(4, route);
                        ps.setString(5, f[4]);
                        ps.setDate(6, Date.valueOf(f[5]));
                        ps.setDouble(7, amount);

                        ps.addBatch();
                        accepted++;

                        if (accepted % 1000 == 0) {
                            ps.executeBatch();
                        }

                    } catch (Exception e) {
                        rejectWriter.write(line + " | " + e.getMessage());
                        rejectWriter.newLine();
                        rejected++;
                    }
                }

                ps.executeBatch();
                con.commit();

                System.out.println("\nMigration completed!");
                System.out.println("Accepted records: " + accepted);
                System.out.println("Rejected records: " + rejected);
            }

        } catch (Exception e) {
            System.out.println("Migration error: " + e.getMessage());
        }

        System.out.println("Rejected records saved in: " + rejectFile);
    }
}
