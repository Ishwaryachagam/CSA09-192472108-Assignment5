
import java.sql.*;

public class B3JdbcBenchmark {

    private static final String URL =
            "jdbc:mysql://localhost:3306/csa09";

    private static final String USER = "root";

    // Enter your MySQL password here
    private static final String PASSWORD = "";

    private static final int BATCH_SIZE = 1000;

    public static void main(String[] args) {

        try (Connection con =
                DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("MySQL connected successfully!");

            createTable(con);
            showDatabaseMetadata(con);

            int[] sizes = {1000, 10000, 100000};

            System.out.println("\n===== JDBC BENCHMARK =====");

            for (int size : sizes) {

                System.out.println("\nRecords: " + size);

                benchmark(con, size, "Statement", true);
                benchmark(con, size, "PreparedStatement", true);
                benchmark(con, size, "Batch", true);
            }

            System.out.println("\n===== AUTO-COMMIT VS TRANSACTION =====");

            int comparisonSize = 1000;

            String[] methods = {
                "Statement",
                "PreparedStatement",
                "Batch"
            };

            for (String method : methods) {

                System.out.println("\nMethod: " + method);

                benchmark(con, comparisonSize, method, true);
                benchmark(con, comparisonSize, method, false);
            }

            System.out.println("\nBenchmark completed successfully!");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void createTable(Connection con)
            throws SQLException {

        String sql = "CREATE TABLE IF NOT EXISTS b3_benchmark (" +
                "id INT PRIMARY KEY, " +
                "record_name VARCHAR(100), " +
                "amount DOUBLE)";

        try (Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }

        System.out.println("Benchmark table ready.");
    }

    private static void showDatabaseMetadata(Connection con)
            throws SQLException {

        DatabaseMetaData meta = con.getMetaData();

        System.out.println("\n===== DATABASE METADATA =====");
        System.out.println("Database: " + meta.getDatabaseProductName());
        System.out.println("Database Version: " +
                meta.getDatabaseProductVersion());
        System.out.println("Driver: " + meta.getDriverName());
        System.out.println("Driver Version: " +
                meta.getDriverVersion());
    }

    private static void clearTable(Connection con)
            throws SQLException {

        try (Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM b3_benchmark");
        }
    }

    private static void benchmark(
            Connection con,
            int size,
            String method,
            boolean autoCommit) throws SQLException {

        clearTable(con);

        con.setAutoCommit(autoCommit);

        long start = System.nanoTime();

        try {

            if (method.equals("Statement")) {
                insertUsingStatement(con, size);

            } else if (method.equals("PreparedStatement")) {
                insertUsingPreparedStatement(con, size);

            } else if (method.equals("Batch")) {
                insertUsingBatch(con, size);
            }

            if (!autoCommit) {
                con.commit();
            }

        } catch (SQLException e) {

            if (!autoCommit) {
                con.rollback();
            }

            throw e;

        } finally {
            con.setAutoCommit(true);
        }

        long end = System.nanoTime();

        double time = (end - start) / 1_000_000.0;

        System.out.printf(
                "%s | Records: %d | Mode: %s | Time: %.2f ms%n",
                method,
                size,
                autoCommit ? "Auto-commit" : "Single Transaction",
                time
        );
    }

    private static void insertUsingStatement(
            Connection con, int size) throws SQLException {

        try (Statement st = con.createStatement()) {

            for (int i = 1; i <= size; i++) {

                String sql = "INSERT INTO b3_benchmark " +
                        "VALUES (" + i + ", 'Student" + i +
                        "', " + (1000 + i) + ")";

                st.executeUpdate(sql);
            }
        }
    }

    private static void insertUsingPreparedStatement(
            Connection con, int size) throws SQLException {

        String sql = "INSERT INTO b3_benchmark VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= size; i++) {

                ps.setInt(1, i);
                ps.setString(2, "Student" + i);
                ps.setDouble(3, 1000 + i);

                ps.executeUpdate();
            }
        }
    }

    private static void insertUsingBatch(
            Connection con, int size) throws SQLException {

        String sql = "INSERT INTO b3_benchmark VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= size; i++) {

                ps.setInt(1, i);
                ps.setString(2, "Student" + i);
                ps.setDouble(3, 1000 + i);

                ps.addBatch();

                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }

            if (size % BATCH_SIZE != 0) {
                ps.executeBatch();
            }
        }
    }
}

