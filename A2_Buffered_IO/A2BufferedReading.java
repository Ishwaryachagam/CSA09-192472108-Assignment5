import java.io.*;
import java.nio.file.*;

public class A2BufferedReading {
    static long countLinesUnbuffered(Path path) throws IOException {
        long lines = 0;
        boolean hasData = false, lastWasNewline = false;
        try (InputStream in = new FileInputStream(path.toFile())) {
            int b;
            while ((b = in.read()) != -1) {
                hasData = true;
                if (b == '\n') { lines++; lastWasNewline = true; }
                else lastWasNewline = false;
            }
        }
        if (hasData && !lastWasNewline) lines++;
        return lines;
    }

    static long countLinesBuffered(Path path) throws IOException {
        long lines = 0;
        boolean hasData = false, lastWasNewline = false;
        try (InputStream in = new BufferedInputStream(
                new FileInputStream(path.toFile()), 8192)) {
            int b;
            while ((b = in.read()) != -1) {
                hasData = true;
                if (b == '\n') { lines++; lastWasNewline = true; }
                else lastWasNewline = false;
            }
        }
        if (hasData && !lastWasNewline) lines++;
        return lines;
    }

    public static void main(String[] args) throws Exception {
        Path file = Path.of("attendance.txt");
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            for (int i = 1; i <= 100000; i++) {
                w.write("Attendance record " + i);
                w.newLine();
            }
        }

        long start = System.nanoTime();
        long a = countLinesUnbuffered(file);
        long unbufferedNs = System.nanoTime() - start;

        start = System.nanoTime();
        long b = countLinesBuffered(file);
        long bufferedNs = System.nanoTime() - start;

        System.out.println("Unbuffered lines: " + a);
        System.out.println("Unbuffered time (ms): " + unbufferedNs / 1_000_000.0);
        System.out.println("Buffered lines: " + b);
        System.out.println("Buffered time (ms): " + bufferedNs / 1_000_000.0);
    }
}
