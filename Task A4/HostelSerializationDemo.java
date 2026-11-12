import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class HostelSerializationDemo {

    // Hostel class
    static class Hostel implements Serializable {

        private static final long serialVersionUID = 1L;

        private String hostelName;

        private List<Room> rooms = new ArrayList<>();

        Hostel(String hostelName) {

            this.hostelName = hostelName;
        }

        public void addRoom(Room room) {

            rooms.add(room);
        }

        public void display() {

            System.out.println("Hostel Name: " + hostelName);

            for (Room room : rooms) {

                room.display();
            }
        }
    }

    // Room class
    static class Room implements Serializable {

        private static final long serialVersionUID = 1L;

        private int roomNumber;

        private List<Resident> residents = new ArrayList<>();

        Room(int roomNumber) {

            this.roomNumber = roomNumber;
        }

        public void addResident(Resident resident) {

            residents.add(resident);
        }

        public void display() {

            System.out.println("Room Number: " + roomNumber);

            for (Resident resident : residents) {

                resident.display();
            }
        }
    }

    // Resident class
    static class Resident implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;

        private String registerNumber;

        // New field added in a later version
        private String phoneNumber;

        // Sensitive field excluded from serialization
        private transient String password;

        // Live database resource excluded from serialization
        private transient Connection connection;

        Resident(
                String name,
                String registerNumber,
                String phoneNumber) {

            this.name = name;

            this.registerNumber = registerNumber;

            this.phoneNumber = phoneNumber;
        }

        public void setPassword(String password) {

            this.password = password;
        }

        public void display() {

            System.out.println("Resident Name: " + name);

            System.out.println(
                "Register Number: " + registerNumber
            );

            System.out.println(
                "Phone Number: " + phoneNumber
            );

            System.out.println(
                "Password after reading: " + password
            );

            System.out.println(
                "Connection after reading: " + connection
            );
        }
    }

    // Main method
    public static void main(String[] args) {

        Hostel hostel = new Hostel("SIMATS Hostel");

        Room room = new Room(101);

        Resident resident = new Resident(
            "Arun Kumar",
            "192311001",
            "9876543210"
        );

        // Sample-only value to demonstrate transient behaviour
        resident.setPassword("sample-password");

        room.addResident(resident);

        hostel.addRoom(room);

        // Serialize hostel object
        try (
            ObjectOutputStream out =
                new ObjectOutputStream(
                    new FileOutputStream("hostel.dat")
                )
        ) {

            out.writeObject(hostel);

            System.out.println(
                "Hostel data saved successfully."
            );

        } catch (IOException e) {

            System.out.println(
                "Serialization error: " + e.getMessage()
            );
        }

        // Deserialize hostel object
        try (
            ObjectInputStream in =
                new ObjectInputStream(
                    new FileInputStream("hostel.dat")
                )
        ) {

            Hostel restoredHostel =
                (Hostel) in.readObject();

            System.out.println(
                "\nRestored Hostel Information:"
            );

            restoredHostel.display();

        } catch (IOException | ClassNotFoundException e) {

            System.out.println(
                "Deserialization error: " + e.getMessage()
            );
        }
    }
}
