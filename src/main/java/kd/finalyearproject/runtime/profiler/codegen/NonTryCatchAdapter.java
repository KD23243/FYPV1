package kd.finalyearproject.runtime.profiler.codegen;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class NonTryCatchAdapter extends AdviceAdapter {
    String profilerOwner = "kd/finalyearproject/runtime/profiler/runtime/Profiler";
    String profilerDescriptor = "()Lkd/finalyearproject/runtime/profiler/runtime/Profiler;";

    public NonTryCatchAdapter(int access, MethodVisitor mv, String methodName, String description) {
        super(Opcodes.ASM9, mv, access, methodName, description);
    }

    protected void onMethodEnter() {
        mv.visitMethodInsn(INVOKESTATIC, profilerOwner, "getInstance", profilerDescriptor, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, profilerOwner, "start", "()V", false);
    }

    protected void onMethodExit(int opcode) {
        mv.visitMethodInsn(INVOKESTATIC, profilerOwner, "getInstance", profilerDescriptor, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, profilerOwner, "stop", "()V", false);
    }
}
