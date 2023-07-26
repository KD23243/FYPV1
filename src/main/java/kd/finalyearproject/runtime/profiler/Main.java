package kd.finalyearproject.runtime.profiler;

import kd.finalyearproject.runtime.profiler.codegen.CustomClassLoader;
import kd.finalyearproject.runtime.profiler.codegen.MethodWrapper;
import kd.finalyearproject.runtime.profiler.util.Constants;
import kd.finalyearproject.runtime.profiler.util.CustomException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static kd.finalyearproject.runtime.profiler.ui.FrontEnd.initializeHTMLExport;
import static kd.finalyearproject.runtime.profiler.ui.JSONParser.initializeCPUParser;
import static kd.finalyearproject.runtime.profiler.util.Constants.OUT;

public class Main {
    static long profilerStartTime;
    static int exitCode = 0;
    private static String originJarArgs = null;
    static String originJarName = null;
    static String skipFunctionString = null;
    private static int functionCount = 0;
    static final List<String> INSTRUMENTED_PATHS = new ArrayList<>();
    static final List<String> INSTRUMENTED_FILES = new ArrayList<>();

    public static void main(String[] args) throws CustomException {
        profilerStartTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
        addShutdownHookAndCleanup();
        printHeader();
        handleProfilerArguments(args);
        extractProfiler();
        createTempJar(originJarName);
        initializeProfiling(originJarName);
    }

    private static void printHeader() {
        String header = "%n" +
                Constants.ANSI_GRAY
                + "================================================================================" +
                Constants.ANSI_RESET
                + "%n" +
                Constants.ANSI_CYAN
                + "FYP Profiler" + Constants.ANSI_RESET + ": Profiling..." + "%n" +
                Constants.ANSI_GRAY
                + "================================================================================" +
                Constants.ANSI_RESET
                + "%n" +
                "WARNING : FYP Profiler is an experimental feature.";
        OUT.printf(header + "%n");
    }

    private static void handleProfilerArguments(String[] args) {
        if (args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--file":
                        originJarName = args[i + 1];
                        break;
                    case "--args":
                        originJarArgs = args[i + 1];
                        originJarArgs = originJarArgs.substring(1, originJarArgs.length() - 1);
                        break;
                }
            }
        }
    }

    private static void extractProfiler() throws CustomException {
        OUT.printf(Constants.ANSI_CYAN + "[1/6] Initializing Profiler..." + Constants.ANSI_RESET + "%n");
        try {
            new ProcessBuilder("jar", "xvf", "profiler.jar", "kd/finalyearproject/runtime/profiler/runtime")
                    .start()
                    .waitFor();
        } catch (IOException | InterruptedException exception) {
            throw new CustomException(exception);
        }
    }

    public static void createTempJar(String originJarName) {
        try {
            OUT.printf(Constants.ANSI_CYAN + "[2/6] Copying Executable..." + Constants.ANSI_RESET + "%n");
            Path sourcePath = Paths.get(originJarName);
            Path destinationPath = Paths.get(Constants.TEMP_JAR_FILE_NAME);
            Files.copy(sourcePath, destinationPath);
        } catch (IOException e) {
            exitCode = 2;
            OUT.printf("Error occurred while copying the file: %s%n", e.getMessage());
        }
    }

    private static void initializeProfiling(String originJarName) throws CustomException {
        OUT.printf(Constants.ANSI_CYAN + "[3/6] Performing Analysis..." + Constants.ANSI_RESET + "%n");
        ArrayList<String> classNames = new ArrayList<>();
        try {
            findAllClassNames(originJarName, classNames);
        } catch (Exception e) {
            OUT.printf("(No such file or directory)" + "%n");
        }
        OUT.printf(Constants.ANSI_CYAN + "[4/6] Instrumenting Functions..." + Constants.ANSI_RESET + "%n");
        try (JarFile jarFile = new JarFile(originJarName)) {
            CustomClassLoader customClassLoader = new CustomClassLoader(
                    new URLClassLoader(new URL[]{new File(originJarName).toURI().toURL()}));
            Set<String> usedPaths = new HashSet<>();
            for (String className : classNames) {
                    try (InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className))) {
                        byte[] code = MethodWrapper.modifyMethods(inputStream);
                        customClassLoader.loadClass(code);
                        usedPaths.add(className.replace(".class", "").replace("/", "."));
                        MethodWrapper.printCode(className, code);
                }
            }
            OUT.printf(" ○ Instrumented Class Count: " + classNames.size() + "%n");
            try (PrintWriter printWriter = new PrintWriter("usedPathsList.txt", StandardCharsets.UTF_8)) {
                printWriter.println(String.join(", ", usedPaths));
            }
            OUT.printf(" ○ Instrumented Function Count: " + functionCount + "%n");

        } catch (Throwable throwable) {
            throw new CustomException(throwable);
        }
        try {
            modifyJar();
        } catch (Throwable throwable) {
            throw new CustomException(throwable);
        }
    }

    private static void modifyJar() throws InterruptedException, IOException {
        try {
            final File userDirectory = new File(System.getProperty("user.dir")); // Get the user directory
            listAllFiles(userDirectory); // List all files in the user directory and its subdirectories
            // Get a list of the directories containing instrumented files
            List<String> changedDirectories = INSTRUMENTED_FILES.stream().distinct().collect(Collectors.toList());
            loadDirectories(changedDirectories);
        } finally {
            for (String instrumentedFilePath : INSTRUMENTED_PATHS) {
                FileUtils.deleteDirectory(new File(instrumentedFilePath));
            }
            FileUtils.deleteDirectory(new File("kd/finalyearproject/runtime/profiler/runtime"));
            MethodWrapper.invokeMethods();
        }
    }

    private static void loadDirectories(List<String> changedDirs) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "uf", Constants.TEMP_JAR_FILE_NAME);
            processBuilder.command().addAll(changedDirs);
            processBuilder.start().waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void listAllFiles(final File userDirectory) {
        String absolutePath = Paths.get(Constants.TEMP_JAR_FILE_NAME).toFile()
                .getAbsolutePath().replaceAll(Constants.TEMP_JAR_FILE_NAME, "");

        File[] files = userDirectory.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                if (fileEntry.isDirectory()) {
                    listAllFiles(fileEntry);
                } else {
                    String fileEntryString = String.valueOf(fileEntry);
                    if (fileEntryString.endsWith(".class")) {
                        fileEntryString = fileEntryString.replaceAll(absolutePath, "");
                        int index = fileEntryString.lastIndexOf('/');
                        fileEntryString = fileEntryString.substring(0, index);
                        String[] fileEntryParts = fileEntryString.split("/");
                        INSTRUMENTED_PATHS.add(fileEntryParts[0]);
                        INSTRUMENTED_FILES.add(fileEntryString);
                    }
                }
            }
        }
    }

    private static void findAllClassNames(String jarPath, ArrayList<String> classNames) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarPath))) {
            for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    classNames.add(String.valueOf(entry));
                }
            }
        }
    }

    private static void deleteTempData() {
        String filePrefix = "jartmp";
        File[] files = new File(System.getProperty("user.dir")).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(filePrefix)) {
                    FileUtils.deleteQuietly(file);
                }
            }
        }
    }

    private static void addShutdownHookAndCleanup() {
        // Add a shutdown hook to stop the profiler and parse the output when the program is closed.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                long profilerTotalTime = TimeUnit.MILLISECONDS.convert(
                        System.nanoTime(), TimeUnit.NANOSECONDS) - profilerStartTime;
                File tempJarFile = new File(Constants.TEMP_JAR_FILE_NAME);
                if (tempJarFile.exists()) {
                    boolean deleted = tempJarFile.delete();
                    if (!deleted) {
                        System.err.printf("Failed to delete temp jar file: " + Constants.TEMP_JAR_FILE_NAME + "%n");
                    }
                }
                OUT.printf("%n" + Constants.ANSI_CYAN
                        + "[6/6] Generating Output..." + Constants.ANSI_RESET + "%n");
                Thread.sleep(100);
                initializeCPUParser(skipFunctionString);
                deleteFileIfExists("usedPathsList.txt");
                deleteFileIfExists("CpuPre.json");
                OUT.printf(" ○ Execution Time: " + profilerTotalTime / 1000 + " Seconds" + "%n");
                deleteTempData();
                initializeHTMLExport();
                deleteFileIfExists("performance_report.json");
                OUT.printf("----------------------------------------");
                OUT.printf("----------------------------------------" + "%n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private static void deleteFileIfExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.printf("Failed to delete file: " + filePath + "%n");
            }
        }
    }

    public static String getOriginJarArgs() {
        return originJarArgs;
    }
}

