package kd.finalyearproject.runtime.profiler.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class TryCatchAdapter extends AdviceAdapter {
    String profilerOwner = "kd/finalyearproject/runtime/profiler/runtime/Profiler";
    String profilerDescriptor = "()Lkd/finalyearproject/runtime/profiler/runtime/Profiler;";
    Label tryStart = new Label();

    public TryCatchAdapter(int access, MethodVisitor mv, String methodName, String description) {
        super(Opcodes.ASM9, mv, access, methodName, description);
    }

    public void visitCode() {
        super.visitCode();
        mv.visitLabel(tryStart);
    }

    protected void onMethodEnter() {
        mv.visitMethodInsn(INVOKESTATIC, profilerOwner, "getInstance", profilerDescriptor, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, profilerOwner, "start", "()V", false);
    }

    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            onFinally();
        }
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        Label tryEnd = new Label();
        mv.visitTryCatchBlock(tryStart, tryEnd, tryEnd, null);
        mv.visitLabel(tryEnd);
        onFinally();
        mv.visitInsn(ATHROW);
        mv.visitMaxs(-1, -1);
    }

    private void onFinally() {
        mv.visitMethodInsn(INVOKESTATIC, profilerOwner, "getInstance", profilerDescriptor, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, profilerOwner, "stop", "()V", false);
    }
}
