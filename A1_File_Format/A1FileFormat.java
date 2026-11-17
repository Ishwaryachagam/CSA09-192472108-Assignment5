import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class A1FileFormat {
    record Pass(String passId, String regNo, String name, int routeNo,
                String boardingStop, LocalDate validityDate, double amountPaid) {}

    static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    static String toCsv(Pass p) {
        return String.join(",",
            csv(p.passId()), csv(p.regNo()), csv(p.name()),
            Integer.toString(p.routeNo()), csv(p.boardingStop()),
            csv(p.validityDate().toString()), Double.toString(p.amountPaid()));
    }

    static List<String> parseCsv(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"'); i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                values.add(field.toString()); field.setLength(0);
            } else field.append(c);
        }
        if (quoted) throw new IllegalArgumentException("Unclosed quote");
        values.add(field.toString());
        return values;
    }

    static Pass fromCsv(String line) {
        List<String> f = parseCsv(line);
        if (f.size() != 7) throw new IllegalArgumentException("Expected 7 fields");
        return new Pass(f.get(0), f.get(1), f.get(2), Integer.parseInt(f.get(3)),
            f.get(4), LocalDate.parse(f.get(5)), Double.parseDouble(f.get(6)));
    }

    public static void main(String[] args) throws Exception {
        Path textFile = Path.of("buspasses.csv");
        Path binaryFile = Path.of("buspasses.dat");
        List<Pass> passes = List.of(
            new Pass("P1001", "192472108", "Ishwarya", 12, "Main Gate",
                LocalDate.of(2027, 3, 31), 4500.00),
            new Pass("P1002", "192472109", "Arun", 15, "Library Stop",
                LocalDate.of(2027, 3, 31), 4200.00)
        );

        try (BufferedWriter w = Files.newBufferedWriter(textFile, StandardCharsets.UTF_8)) {
            for (Pass p : passes) { w.write(toCsv(p)); w.newLine(); }
        }
        System.out.println("Text file written: " + textFile);

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(binaryFile)))) {
            out.writeInt(1); // format version
            out.writeInt(passes.size());
            for (Pass p : passes) {
                out.writeUTF(p.passId()); out.writeUTF(p.regNo());
                out.writeUTF(p.name()); out.writeInt(p.routeNo());
                out.writeUTF(p.boardingStop()); out.writeUTF(p.validityDate().toString());
                out.writeDouble(p.amountPaid());
            }
        }
        System.out.println("Binary file written: " + binaryFile);

        System.out.println("\nReading text records:");
        try (BufferedReader r = Files.newBufferedReader(textFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) System.out.println(fromCsv(line));
        }

        System.out.println("\nReading binary records:");
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(binaryFile)))) {
            int version = in.readInt();
            if (version != 1) throw new IOException("Unsupported file version: " + version);
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                Pass p = new Pass(in.readUTF(), in.readUTF(), in.readUTF(), in.readInt(),
                    in.readUTF(), LocalDate.parse(in.readUTF()), in.readDouble());
                System.out.println(p);
            }
        }
    }
}
