import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        StudentDao dao = new StudentDao();

        try {

            // Insert student
            Student s1 = new Student(
                "192311001",
                "Arun Kumar",
                "arun@gmail.com"
            );

            dao.insert(s1);

            System.out.println("Student inserted successfully.");

            System.out.println(
                "Generated Student ID: " + s1.getStudentId()
            );

            // Find student
            Optional<Student> found =
                dao.findById(s1.getStudentId());

            if (found.isPresent()) {

                System.out.println("Student found:");

                System.out.println(found.get());

            } else {

                System.out.println("Student not found.");
            }

            // Retrieve all students
            System.out.println("\nAll Students:");

            List<Student> students = dao.findAll();

            for (Student s : students) {

                System.out.println(s);
            }

            // Update student
            s1.setStudentName("Arun Kumar Raj");

            boolean updated = dao.update(s1);

            System.out.println(
                "\nStudent updated: " + updated
            );

            // Delete student
            boolean deleted =
                dao.deleteById(s1.getStudentId());

            System.out.println(
                "Student deleted: " + deleted
            );

        } catch (SQLException e) {

            System.out.println(
                "Database error: " + e.getMessage()
            );
        }
    }
}
