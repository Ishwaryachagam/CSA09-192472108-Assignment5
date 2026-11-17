
import java.sql.*;

public class A5TransactionAndInjection {

    static final String URL =
        "jdbc:mysql://localhost:3306/csa09";

    static final String USER = "root";
    static final String PASSWORD = "";

    // Create required database tables
    public static void createTables(Connection con)
            throws SQLException {

        Statement st = con.createStatement();

        st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS wallet_accounts (" +
            "account_id VARCHAR(20) PRIMARY KEY, " +
            "holder_name VARCHAR(100), " +
            "balance DECIMAL(12,2))"
        );

        st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS app_users (" +
            "username VARCHAR(50) PRIMARY KEY, " +
            "password VARCHAR(100))"
        );

        // Insert sample accounts if they do not exist
        st.executeUpdate(
            "INSERT IGNORE INTO wallet_accounts " +
            "VALUES ('ACC1001','Ishwarya',5000.00)"
        );

        st.executeUpdate(
            "INSERT IGNORE INTO wallet_accounts " +
            "VALUES ('ACC1002','Arun',3000.00)"
        );

        // Insert sample login account
        st.executeUpdate(
            "INSERT IGNORE INTO app_users " +
            "VALUES ('admin','admin123')"
        );

        st.close();
    }

    // Display wallet balance
    public static void displayBalance(Connection con)
            throws SQLException {

        String sql =
            "SELECT * FROM wallet_accounts";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        System.out.println("\nWallet Account Details:");

        while (rs.next()) {
            System.out.println(
                rs.getString("account_id") + " | " +
                rs.getString("holder_name") + " | Balance: " +
                rs.getDouble("balance")
            );
        }

        rs.close();
        st.close();
    }

    // Transfer money using transaction
    public static void transferMoney(
            Connection con,
            String fromAccount,
            String toAccount,
            double amount) throws SQLException {

        try {
            con.setAutoCommit(false);

            // Debit amount from sender
            String debitSQL =
                "UPDATE wallet_accounts " +
                "SET balance = balance - ? " +
                "WHERE account_id = ? AND balance >= ?";

            PreparedStatement debit =
                con.prepareStatement(debitSQL);

            debit.setDouble(1, amount);
            debit.setString(2, fromAccount);
            debit.setDouble(3, amount);

            int debitRows = debit.executeUpdate();
            debit.close();

            if (debitRows != 1) {
                throw new SQLException(
                    "Insufficient balance or sender account not found."
                );
            }

            // Credit amount to receiver
            String creditSQL =
                "UPDATE wallet_accounts " +
                "SET balance = balance + ? " +
                "WHERE account_id = ?";

            PreparedStatement credit =
                con.prepareStatement(creditSQL);

            credit.setDouble(1, amount);
            credit.setString(2, toAccount);

            int creditRows = credit.executeUpdate();
            credit.close();

            if (creditRows != 1) {
                throw new SQLException(
                    "Receiver account not found."
                );
            }

            // Save both operations
            con.commit();

            System.out.println(
                "\nTransaction completed successfully!"
            );

            System.out.println(
                "Amount transferred: Rs. " + amount
            );

        } catch (SQLException e) {

            // Undo changes if any operation fails
            con.rollback();

            System.out.println(
                "\nTransaction failed. Changes rolled back."
            );

            System.out.println(e.getMessage());

        } finally {
            con.setAutoCommit(true);
        }
    }

    // Secure authentication using PreparedStatement
    public static void authenticate(
            Connection con,
            String username,
            String password) throws SQLException {

        String sql =
            "SELECT * FROM app_users " +
            "WHERE username = ? AND password = ?";

        PreparedStatement ps =
            con.prepareStatement(sql);

        ps.setString(1, username);
        ps.setString(2, password);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println(
                "\nLogin successful!"
            );
        } else {
            System.out.println(
                "\nInvalid username or password."
            );
        }

        rs.close();
        ps.close();
    }

    public static void main(String[] args) {

        try {
            // Connect to MySQL
            Connection con = DriverManager.getConnection(
                URL, USER, PASSWORD
            );

            System.out.println(
                "MySQL connected successfully!"
            );

            // Create tables and sample records
            createTables(con);

            // Display balances before transfer
            System.out.println("\nBefore Transaction:");
            displayBalance(con);

            // Transfer Rs. 500
            transferMoney(
                con, "ACC1001", "ACC1002", 500.00
            );

            // Display balances after transfer
            System.out.println("\nAfter Transaction:");
            displayBalance(con);

            // Test secure authentication
            authenticate(
                con, "admin", "admin123"
            );

            // Test invalid login
            authenticate(
                con, "unknown", "wrong123"
            );

            con.close();

            System.out.println(
                "\nDatabase connection closed."
            );

        } catch (SQLException e) {
            System.out.println(
                "Database error: " + e.getMessage()
            );
        }
    }
}
