package kd.finalyearproject.runtime.profiler.codegen;

import org.objectweb.asm.ClassReader;

import static kd.finalyearproject.runtime.profiler.util.Constants.OUT;

public class CustomClassLoader extends ClassLoader {
    public CustomClassLoader(ClassLoader parent) {
        super(parent);
    }
    public Class<?> loadClass(byte[] code) {
        Class<?> classOut = null;
        String name = readClassName(code);
        try {
            classOut = defineClass(name, code, 0, code.length);
        } catch (Error e) {
            OUT.printf(name + "\n");
        }
        return classOut;
    }

    public String readClassName(final byte[] byteCode) {
        String className;
        className = new ClassReader(byteCode).getClassName().replace("/", ".");
        return className;
    }
}
