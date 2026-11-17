
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class B1PassStore {

    public record Pass(String passId, String regNo, String name,
                       int routeNo, String boardingStop,
                       LocalDate validityDate, double amountPaid) {}

    private final Path file;

    public B1PassStore(Path file) {
        this.file = file;
    }

    private static String q(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static String encode(Pass p) {
        return String.join(",",
            q(p.passId()), q(p.regNo()), q(p.name()),
            Integer.toString(p.routeNo()),
            q(p.boardingStop()),
            q(p.validityDate().toString()),
            Double.toString(p.amountPaid()));
    }

    private static List<String> splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder b = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (quoted && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    b.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                out.add(b.toString());
                b.setLength(0);
            } else {
                b.append(c);
            }
        }

        if (quoted)
            throw new IllegalArgumentException("Unclosed quoted field");

        out.add(b.toString());
        return out;
    }

    private static Pass parse(String line) {
        List<String> f = splitCsv(line);

        if (f.size() != 7)
            throw new IllegalArgumentException("Expected 7 fields");

        if (f.stream().anyMatch(String::isBlank))
            throw new IllegalArgumentException("Required field is empty");

        int route = Integer.parseInt(f.get(3));
        double amount = Double.parseDouble(f.get(6));
        LocalDate date = LocalDate.parse(f.get(5));

        if (route <= 0 || amount < 0 || !Double.isFinite(amount))
            throw new IllegalArgumentException("Invalid route or amount");

        return new Pass(f.get(0), f.get(1), f.get(2),
                route, f.get(4), date, amount);
    }

    public void add(Pass p) throws IOException {
        validate(p);

        Files.createDirectories(file.toAbsolutePath().getParent());

        if (find(p.passId()) != null) {
            System.out.println("Pass already exists: " + p.passId());
            return;
        }

        try (BufferedWriter w = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            w.write(encode(p));
            w.newLine();
        }

        System.out.println("Pass added successfully!");
    }

    private static void validate(Pass p) {
        if (p.passId().isBlank() || p.regNo().isBlank()
                || p.name().isBlank() || p.boardingStop().isBlank()
                || p.routeNo() <= 0 || p.amountPaid() < 0
                || !Double.isFinite(p.amountPaid())) {
            throw new IllegalArgumentException("Invalid pass data");
        }
    }

    public Pass find(String id) throws IOException {
        if (!Files.exists(file))
            return null;

        try (BufferedReader r = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String line;

            while ((line = r.readLine()) != null) {
                try {
                    Pass p = parse(line);

                    if (p.passId().equals(id))
                        return p;

                } catch (RuntimeException bad) {
                    System.err.println("Skipping damaged row: "
                            + bad.getMessage());
                }
            }
        }

        return null;
    }

    public List<Pass> listByRoute(int route) throws IOException {
        List<Pass> result = new ArrayList<>();

        if (!Files.exists(file))
            return result;

        try (BufferedReader r = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String line;

            while ((line = r.readLine()) != null) {
                try {
                    Pass p = parse(line);

                    if (p.routeNo() == route)
                        result.add(p);

                } catch (RuntimeException bad) {
                    System.err.println("Skipping damaged row: "
                            + bad.getMessage());
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {

        B1PassStore store =
                new B1PassStore(Path.of("buspasses.csv"));

        store.add(new Pass(
                "P1001", "192472108", "Ishwarya", 12,
                "Main Gate", LocalDate.of(2027, 3, 31), 4500));

        System.out.println("\nSearching Pass:");
        System.out.println(store.find("P1001"));

        System.out.println("\nListing Route 12 Passes:");
        System.out.println(store.listByRoute(12));
    }
}
