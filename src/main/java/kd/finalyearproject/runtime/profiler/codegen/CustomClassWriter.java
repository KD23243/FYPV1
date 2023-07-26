package kd.finalyearproject.runtime.profiler.codegen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class CustomClassWriter extends ClassWriter {
    private static final String OBJECT_CLASS = "java/lang/Object";

    public CustomClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        ClassLoader classLoader = getClassLoader();
        Class<?> class1;
        try {
            class1 = Class.forName(type1.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            return OBJECT_CLASS;
        }
        Class<?> class2;
        try {
            class2 = Class.forName(type2.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            return OBJECT_CLASS;
        }
        if (class1.isAssignableFrom(class2)) {
            return type1;
        } else if (class2.isAssignableFrom(class1)) {
            return type2;
        } else if (class1.isInterface() || class2.isInterface()) {
            return OBJECT_CLASS;
        }
        class1 = class1.getSuperclass();
        while (!class1.isAssignableFrom(class2)) {
            class1 = class1.getSuperclass();
        }
        return class1.getName().replace('.', '/');
    }
}
