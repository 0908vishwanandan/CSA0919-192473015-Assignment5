public class Student {

    private Long studentId;

    private String registerNo;

    private String studentName;

    private String email;

    // Constructor for a new student
    public Student(
            String registerNo,
            String studentName,
            String email) {

        this.registerNo = registerNo;

        this.studentName = studentName;

        this.email = email;
    }

    // Constructor for an existing student
    public Student(
            Long studentId,
            String registerNo,
            String studentName,
            String email) {

        this.studentId = studentId;

        this.registerNo = registerNo;

        this.studentName = studentName;

        this.email = email;
    }

    public Long getStudentId() {

        return studentId;
    }

    public void setStudentId(Long studentId) {

        this.studentId = studentId;
    }

    public String getRegisterNo() {

        return registerNo;
    }

    public void setRegisterNo(String registerNo) {

        this.registerNo = registerNo;
    }

    public String getStudentName() {

        return studentName;
    }

    public void setStudentName(String studentName) {

        this.studentName = studentName;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    @Override
    public String toString() {

        return "Student ID: " + studentId
            + ", Register No: " + registerNo
            + ", Name: " + studentName
            + ", Email: " + email;
    }
}
