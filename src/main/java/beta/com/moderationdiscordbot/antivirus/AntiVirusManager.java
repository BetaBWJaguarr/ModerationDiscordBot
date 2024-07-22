package beta.com.moderationdiscordbot.antivirus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The {@code AntiVirusManager} class provides antivirus functionalities to detect and handle potentially malicious files.
 * It is designed to identify suspicious content based on predefined signatures, patterns, file extensions, and other heuristic indicators.
 * The class supports scanning of various file types, including plain text files, zip archives, and executable files.
 *
 * <p>This class is equipped to:
 * <ul>
 *     <li>Check files for known virus signatures and patterns.</li>
 *     <li>Identify dangerous URL patterns and script tags.</li>
 *     <li>Detect files with potentially dangerous extensions.</li>
 *     <li>Analyze zip files for hidden viruses.</li>
 *     <li>Examine executable files for dangerous DLL functions.</li>
 *     <li>Recognize encrypted data and ransomware signatures.</li>
 *     <li>Verify that files have the necessary permissions.</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * AntiVirusManager avManager = new AntiVirusManager();
 * File fileToCheck = new File("example.zip");
 * boolean isInfected = avManager.isFileInfected(fileToCheck);
 * System.out.println("Is the file infected? " + isInfected);
 * }
 * </pre>
 *
 * <p>Dependencies:
 * <ul>
 *     <li>{@code java.io.*}: For file operations and input streams.</li>
 *     <li>{@code java.nio.charset.StandardCharsets}: For character encoding.</li>
 *     <li>{@code java.util.Scanner}: For reading file contents.</li>
 *     <li>{@code java.util.zip.*}: For handling zip files.</li>
 * </ul>
 *
 * <p>The class provides methods to:
 * <ul>
 *     <li>{@link #isFileInfected(File)} - Check if a file is infected or malicious.</li>
 *     <li>{@link #containsSignature(String, String[])} - Determine if content contains specific signatures.</li>
 *     <li>{@link #containsPattern(String, String[])} - Check if content matches dangerous patterns.</li>
 *     <li>{@link #isDangerousFileExtension(String)} - Identify dangerous file extensions.</li>
 *     <li>{@link #isZipFile(String)} - Check if the file is a zip archive.</li>
 *     <li>{@link #containsVirusInZip(InputStream)} - Scan a zip file for viruses.</li>
 *     <li>{@link #readFromFile(File)} - Read content from a file.</li>
 *     <li>{@link #readZipEntry(ZipInputStream)} - Read content from a zip entry.</li>
 *     <li>{@link #containsDangerousDLLFunctions(File)} - Detect dangerous functions in DLL files.</li>
 *     <li>{@link #isExecutableFile(String)} - Check if a file is an executable.</li>
 *     <li>{@link #hasRequiredPermissions(File)} - Verify if the file has necessary permissions.</li>
 *     <li>{@link #checkPermission(File, String)} - Check specific file permissions.</li>
 * </ul>
 *
 * @see java.io.File
 * @see java.io.InputStream
 * @see java.util.Scanner
 * @see java.util.zip.ZipInputStream
 */

public class AntiVirusManager {

    private static final String[] SIGNATURES = {
            "MZ", "This program cannot be run in DOS mode",
            "EVIL_VIRUS_SIGNATURE_1", "EVIL_VIRUS_SIGNATURE_2",
            "Trojan", "Worm", "Ransomware"
    };

    private static final String[] DANGEROUS_URL_PATTERNS = {
            ".*(http|https)://(malicious\\.com|evil\\.net|hack\\.org|phishing\\.com|spyware\\.org).*"
    };

    private static final String[] DANGEROUS_SCRIPT_PATTERNS = {
            "<script>", "alert\\(", "eval\\(", "document\\.write\\("
    };

    private static final String[] DANGEROUS_FILE_EXTENSIONS = {
            ".exe", ".bat", ".cmd", ".com", ".msi", ".jar", ".vbs",
            ".scr", ".pif", ".reg", ".hta", ".ws", ".wsf", ".cpl"
    };

    private static final String[] DANGEROUS_DLL_FUNCTIONS = {
            "LoadLibrary", "GetProcAddress", "ShellExecute", "CreateProcess"
    };

    private static final String[] HEURISTIC_PATTERNS = {
            "CreateRemoteThread", "VirtualAllocEx", "WriteProcessMemory",
            "NtQueryInformationProcess", "ZwQueryInformationProcess"
    };

    private static final String[] ENCRYPTED_DATA_SIGNATURES = {
            "RSA", "AES", "DES", "TripleDES"
    };

    private static final String[] RANSOMWARE_SIGNATURES = {
            "Your files are encrypted!", "Pay now to unlock your files.",
            "All your files have been locked!", "Your personal files are encrypted.",
            "Unlock your files with the decryption key.", "Your important files are encrypted.",
            "Pay ransom to get your files back."
    };

    private static final String[] PERMISSIONS_REQUIRED = {
            "read", "write", "execute"
    };

    public boolean isFileInfected(File file) {
        if (file.isDirectory() || isDangerousFileExtension(file.getName().toLowerCase()) || !hasRequiredPermissions(file)) {
            return true;
        }

        try {
            String content = readFromFile(file);

            return containsSignature(content, SIGNATURES)
                    || containsPattern(content, DANGEROUS_URL_PATTERNS)
                    || containsPattern(content, DANGEROUS_SCRIPT_PATTERNS)
                    || containsPattern(content, HEURISTIC_PATTERNS)
                    || (isZipFile(file.getName()) && containsVirusInZip(new FileInputStream(file)))
                    || (isExecutableFile(file.getName()) && containsDangerousDLLFunctions(file))
                    || containsSignature(content, ENCRYPTED_DATA_SIGNATURES)
                    || containsSignature(content, RANSOMWARE_SIGNATURES);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return false;
        }
    }

    private boolean containsSignature(String content, String[] signatures) {
        for (String signature : signatures) {
            if (content.contains(signature)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPattern(String content, String[] patterns) {
        for (String pattern : patterns) {
            if (content.matches(pattern)) {
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
                if (!entry.isDirectory() && (containsSignature(readZipEntry(zis), SIGNATURES) || containsPattern(readZipEntry(zis), HEURISTIC_PATTERNS))) {
                    return true;
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

    private String readZipEntry(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    private boolean containsDangerousDLLFunctions(File file) {
        if (file.getName().toLowerCase().endsWith(".dll")) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                byte[] dllContent = new byte[(int) raf.length()];
                raf.readFully(dllContent);
                return containsSignature(new String(dllContent, StandardCharsets.UTF_8), DANGEROUS_DLL_FUNCTIONS);
            } catch (IOException e) {
                System.err.println("Error reading DLL file: " + e.getMessage());
            }
        }
        return false;
    }

    private boolean isExecutableFile(String fileName) {
        return fileName.endsWith(".jar") || fileName.endsWith(".exe") || fileName.endsWith(".dll");
    }

    private boolean hasRequiredPermissions(File file) {
        for (String permission : PERMISSIONS_REQUIRED) {
            if (!checkPermission(file, permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPermission(File file, String permission) {
        return switch (permission) {
            case "read" -> file.canRead();
            case "write" -> file.canWrite();
            case "execute" -> true;
            default -> false;
        };
    }
}
