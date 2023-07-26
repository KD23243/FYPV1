package kd.finalyearproject.runtime.profiler.codegen;

import kd.finalyearproject.runtime.profiler.util.Constants;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static kd.finalyearproject.runtime.profiler.Main.getOriginJarArgs;
import static kd.finalyearproject.runtime.profiler.util.Constants.OUT;

public class MethodWrapper extends ClassLoader {
    public static void invokeMethods() throws IOException, InterruptedException {
        String originJarArgs = getOriginJarArgs();
        String[] command = {"java", "-jar", Constants.TEMP_JAR_FILE_NAME};
        if (originJarArgs != null) {
            command = Arrays.copyOf(command, command.length + 1);
            command[3] = originJarArgs;
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        OUT.printf(Constants.ANSI_CYAN + "[5/6] Running Executable..." + Constants.ANSI_RESET + "%n");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().forEach(OUT::println);
        }
        process.waitFor();
    }

    public static byte[] modifyMethods(InputStream inputStream) {
        byte[] code;
        try {
            ClassReader reader = new ClassReader(inputStream);
            ClassWriter classWriter = new CustomClassWriter(reader,
                    ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            ClassVisitor change = new CustomClassVisitor(classWriter);
            reader.accept(change, ClassReader.EXPAND_FRAMES);
            code = classWriter.toByteArray();
            return code;
        } catch (Exception | Error e) {
            OUT.printf(e + "%n");
        }
        return new byte[0]; // Return a zero-length byte array if the code was not modified
    }

    // Print out the modified class code
    public static void printCode(String className, byte[] code) {
        int lastSlashIndex = className.lastIndexOf('/');
        String output = className.substring(0, lastSlashIndex);
        File directory = new File(output);

        if (!directory.exists()) {
            boolean directoryCreated = directory.mkdirs();
            if (!directoryCreated) {
                return;
            }
        }

        try (FileOutputStream fos = new FileOutputStream(className)) {
            fos.write(code); // Write the code to the output stream
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
