import java.io.*;
import java.util.*;

class Resident implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
    String registerNumber;
    transient String password;
    transient java.sql.Connection connection;

    Resident(String name, String registerNumber, String password) {
        this.name = name;
        this.registerNumber = registerNumber;
        this.password = password;
    }
}
class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    int roomNumber;
    List<Resident> residents = new ArrayList<>();
    Room(int roomNumber) { this.roomNumber = roomNumber; }
}
class Hostel implements Serializable {
    private static final long serialVersionUID = 1L;
    String hostelName;
    List<Room> rooms = new ArrayList<>();
    Hostel(String hostelName) { this.hostelName = hostelName; }
}

public class A4Serialization {
    public static void main(String[] args) {
        Hostel h = new Hostel("SIMATS Hostel");
        Room r = new Room(101);
        r.residents.add(new Resident("Ishwarya", "192472108", "do-not-store"));
        h.rooms.add(r);

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream("hostel.dat"))) {
            out.writeObject(h);
            System.out.println("Hostel object serialized.");
        } catch (IOException e) {
            System.out.println("Write error: " + e.getMessage());
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream("hostel.dat"))) {
            Hostel restored = (Hostel) in.readObject();
            System.out.println("Hostel: " + restored.hostelName);
            for (Room room : restored.rooms) {
                System.out.println("Room: " + room.roomNumber);
                for (Resident resident : room.residents) {
                    System.out.println(resident.name + " - " + resident.registerNumber);
                    System.out.println("Password after reading: " + resident.password);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Read error: " + e.getMessage());
        }
    }
}
