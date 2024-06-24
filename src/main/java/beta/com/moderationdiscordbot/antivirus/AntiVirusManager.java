package beta.com.moderationdiscordbot.antivirus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AntiVirusManager {

    private static final String[] VIRUS_SIGNATURES = {
            "MZ",
            "This program cannot be run in DOS mode",
            "EVIL_VIRUS_SIGNATURE_1",
            "EVIL_VIRUS_SIGNATURE_2",
            "Trojan",
            "Worm",
            "Ransomware",
    };

    private static final String[] DANGEROUS_URL_PATTERNS = {
            ".*(http|https)://malicious\\.com.*",
            ".*(http|https)://evil\\.net.*",
            ".*(http|https)://hack\\.org.*",
            ".*(http|https)://phishing\\.com.*",
            ".*(http|https)://spyware\\.org.*",
    };

    private static final String[] DANGEROUS_SCRIPT_PATTERNS = {
            "<script>",
            "alert\\(",
            "eval\\(",
            "document\\.write\\(",
    };

    private static final String[] DANGEROUS_FILE_EXTENSIONS = {
            ".exe", ".bat", ".cmd", ".com", ".msi", ".jar", ".vbs",
            ".scr", ".pif", ".reg", ".hta", ".ws", ".wsf", ".cpl"
    };

    private static final String[] HEURISTIC_PATTERNS = {
            "CreateRemoteThread",
            "VirtualAllocEx",
            "WriteProcessMemory",
            "NtQueryInformationProcess",
            "ZwQueryInformationProcess"
    };

    public boolean isFileInfected(File file) {
        if (file.isDirectory()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        if (isDangerousFileExtension(fileName)) {
            return true;
        }

        try {
            String content = readFromFile(file);

            if (containsVirusSignature(content)) {
                return true;
            }

            if (containsDangerousUrl(content)) {
                return true;
            }

            if (containsDangerousScript(content)) {
                return true;
            }

            if (containsHeuristicPattern(content)) {
                return true;
            }

            if (isZipFile(fileName)) {
                if (containsVirusInZip(new FileInputStream(file))) {
                    return true;
                }
            }

            return false;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return false;
        }
    }

    private boolean containsVirusSignature(String content) {
        for (String signature : VIRUS_SIGNATURES) {
            if (content.contains(signature)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsDangerousUrl(String content) {
        for (String pattern : DANGEROUS_URL_PATTERNS) {
            if (content.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsDangerousScript(String content) {
        for (String pattern : DANGEROUS_SCRIPT_PATTERNS) {
            if (content.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsHeuristicPattern(String content) {
        for (String pattern : HEURISTIC_PATTERNS) {
            if (content.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDangerousFileExtension(String fileName) {
        for (String extension : DANGEROUS_FILE_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private boolean isZipFile(String fileName) {
        return fileName.endsWith(".zip");
    }

    private boolean containsVirusInZip(InputStream zipInputStream) {
        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    String content = baos.toString(StandardCharsets.UTF_8.name());

                    if (containsVirusSignature(content) || containsHeuristicPattern(content)) {
                        return true;
                    }
                }
                zis.closeEntry();
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error reading zip file: " + e.getMessage());
            return false;
        }
    }

    private String readFromFile(File file) throws IOException {
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }
}
