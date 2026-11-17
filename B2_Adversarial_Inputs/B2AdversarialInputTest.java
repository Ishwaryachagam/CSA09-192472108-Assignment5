
import java.util.*;

public class B2AdversarialInputTest {

    public static void main(String[] args) {

        String[] records = {
            "P2001,192472001,Meena,4,3200.0",
            "P2002,192472002,Arun",
            "P2003,192472003,Divya,7,ABC",
            "",
            "P2004,192472004,Ravi,5,4500.0"
        };

        int valid = 0;
        int invalid = 0;

        System.out.println("ADVERSARIAL INPUT TESTING");
        System.out.println("-------------------------");

        for (String record : records) {

            try {
                if (record.isBlank()) {
                    throw new IllegalArgumentException("Empty record");
                }

                String[] fields = record.split(",");

                if (fields.length != 5) {
                    throw new IllegalArgumentException(
                        "Incorrect number of fields");
                }

                int route = Integer.parseInt(fields[3]);
                double amount = Double.parseDouble(fields[4]);

                if (route <= 0 || amount < 0
                        || !Double.isFinite(amount)) {
                    throw new IllegalArgumentException(
                        "Invalid route or amount");
                }

                System.out.println("VALID: " + record);
                valid++;

            } catch (Exception e) {

                System.out.println("INVALID: " + record);
                System.out.println("Reason: " + e.getMessage());
                invalid++;
            }
        }

        System.out.println("\nTEST SUMMARY");
        System.out.println("Valid Records: " + valid);
        System.out.println("Invalid Records: " + invalid);
        System.out.println("Total Records: " + records.length);
    }
}
