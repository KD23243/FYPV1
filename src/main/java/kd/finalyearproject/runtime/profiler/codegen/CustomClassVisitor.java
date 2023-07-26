package kd.finalyearproject.runtime.profiler.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class CustomClassVisitor extends ClassVisitor {

    public CustomClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.contains("<init>") || name.contains("main")){
            return new NonStrandCheckAdapter(access, methodVisitor, name, desc);
        }else {
            return new StrandCheckAdapter(access, methodVisitor, name, desc);
        }

    }
}
