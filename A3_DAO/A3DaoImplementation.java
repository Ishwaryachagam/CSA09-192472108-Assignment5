
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Student Model
class Student {
    String registerNo;
    String name;
    String department;

    Student(String registerNo, String name, String department) {
        this.registerNo = registerNo;
        this.name = name;
        this.department = department;
    }

    public String toString() {
        return "Register No: " + registerNo +
               ", Name: " + name +
               ", Department: " + department;
    }
}

// DAO Interface
interface StudentDAO {
    void insert(Student s) throws SQLException;
    Student findById(String id) throws SQLException;
    List<Student> findAll() throws SQLException;
    void update(Student s) throws SQLException;
    void delete(String id) throws SQLException;
}

// DAO Implementation
class StudentDAOImpl implements StudentDAO {

    Connection con;

    StudentDAOImpl(Connection con) {
        this.con = con;
    }

    public void insert(Student s) throws SQLException {
        String sql = "INSERT INTO Student VALUES (?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, s.registerNo);
        ps.setString(2, s.name);
        ps.setString(3, s.department);

        ps.executeUpdate();
        ps.close();

        System.out.println("Student inserted successfully.");
    }

    public Student findById(String id) throws SQLException {
        String sql = "SELECT * FROM Student WHERE registerNo = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();

        Student s = null;

        if (rs.next()) {
            s = new Student(
                rs.getString("registerNo"),
                rs.getString("name"),
                rs.getString("department")
            );
        }

        rs.close();
        ps.close();

        return s;
    }

    public List<Student> findAll() throws SQLException {
        List<Student> list = new ArrayList<>();

        String sql = "SELECT * FROM Student";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Student s = new Student(
                rs.getString("registerNo"),
                rs.getString("name"),
                rs.getString("department")
            );

            list.add(s);
        }

        rs.close();
        st.close();

        return list;
    }

    public void update(Student s) throws SQLException {
        String sql = "UPDATE Student SET name=?, department=? WHERE registerNo=?";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, s.name);
        ps.setString(2, s.department);
        ps.setString(3, s.registerNo);

        int rows = ps.executeUpdate();

        if (rows > 0)
            System.out.println("Student updated successfully.");
        else
            System.out.println("Student not found.");

        ps.close();
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM Student WHERE registerNo=?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, id);

        int rows = ps.executeUpdate();

        if (rows > 0)
            System.out.println("Student deleted successfully.");
        else
            System.out.println("Student not found.");

        ps.close();
    }
}

// Main Class
public class A3DaoImplementation {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/csa09";
        String user = "root";
        String password = "";

        try {
            Connection con = DriverManager.getConnection(
                url, user, password
            );

            System.out.println("MySQL connected successfully!");

            StudentDAO dao = new StudentDAOImpl(con);

            // Insert student
            Student s1 = new Student(
                "192472108",
                "Ishwarya",
                "CSE AI"
            );

            dao.insert(s1);

            // Search student
            System.out.println("\nSearching Student:");
            System.out.println(dao.findById("192472108"));

            // Display all students
            System.out.println("\nAll Student Records:");

            for (Student s : dao.findAll()) {
                System.out.println(s);
            }

            // Update student
            Student s2 = new Student(
                "192472108",
                "Ishwarya",
                "AI and Data Science"
            );

            dao.update(s2);

            System.out.println("\nUpdated Student:");
            System.out.println(dao.findById("192472108"));

            con.close();

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
