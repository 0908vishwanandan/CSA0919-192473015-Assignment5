import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDao implements Dao<Student, Long> {

    // Insert student
    @Override
    public void insert(Student student) throws SQLException {

        String sql =
            "INSERT INTO students " +
            "(register_no, student_name, email) " +
            "VALUES (?, ?, ?)";

        try (
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
                )
        ) {

            ps.setString(1, student.getRegisterNo());

            ps.setString(2, student.getStudentName());

            ps.setString(3, student.getEmail());

            ps.executeUpdate();

            // Retrieve automatically generated primary key
            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {

                    student.setStudentId(rs.getLong(1));
                }
            }
        }
    }

    // Find student by ID
    @Override
    public Optional<Student> findById(Long id)
            throws SQLException {

        String sql =
            "SELECT * FROM students WHERE student_id = ?";

        try (
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql)
        ) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return Optional.of(mapStudent(rs));
                }
            }
        }

        return Optional.empty();
    }

    // Retrieve all students
    @Override
    public List<Student> findAll() throws SQLException {

        List<Student> students = new ArrayList<>();

        String sql = "SELECT * FROM students";

        try (
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                students.add(mapStudent(rs));
            }
        }

        return students;
    }

    // Update student
    @Override
    public boolean update(Student student)
            throws SQLException {

        String sql =
            "UPDATE students SET " +
            "register_no = ?, student_name = ?, email = ? " +
            "WHERE student_id = ?";

        try (
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql)
        ) {

            ps.setString(1, student.getRegisterNo());

            ps.setString(2, student.getStudentName());

            ps.setString(3, student.getEmail());

            ps.setLong(4, student.getStudentId());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete student by ID
    @Override
    public boolean deleteById(Long id)
            throws SQLException {

        String sql =
            "DELETE FROM students WHERE student_id = ?";

        try (
            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql)
        ) {

            ps.setLong(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into Student object
    private Student mapStudent(ResultSet rs)
            throws SQLException {

        return new Student(
            rs.getLong("student_id"),
            rs.getString("register_no"),
            rs.getString("student_name"),
            rs.getString("email")
        );
    }
}
