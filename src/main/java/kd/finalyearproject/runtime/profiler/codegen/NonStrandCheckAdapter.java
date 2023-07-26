package kd.finalyearproject.runtime.profiler.codegen;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * This class is used as the advice adapter for the FYP profiler.
 * This class only manages the functions that doesn't contain the strand parameter.
 *
 * @since 2201.7.0
 */
public class NonStrandCheckAdapter extends AdviceAdapter {
    String profilerOwner = "kd/finalyearproject/runtime/profiler/runtime/Profiler";
    String profilerDescriptor = "()Lkd/finalyearproject/runtime/profiler/runtime/Profiler;";

    /**
     Constructor for MethodWrapperAdapter.
     @param access - access flag of the method that is wrapped.
     @param mv - MethodVisitor instance to generate the bytecode.
     @param methodName - name of the method that is wrapped.
     @param description - description of the method that is wrapped.
     */

    public NonStrandCheckAdapter(int access, MethodVisitor mv, String methodName, String description) {
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
