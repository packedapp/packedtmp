package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;

import app.packed.operation.InvocationType;
import internal.app.packed.lifetime.LifetimeObjectArena;

public final class PackedInvocationType implements InvocationType {

    public static PackedInvocationType DEFAULTS = new PackedInvocationType(0, -1, MethodType.methodType(void.class, LifetimeObjectArena.class));
    final int beanInstanceIndex;
    final int extensionContext;

    final MethodType methodType;

    public PackedInvocationType(int extensionContext, int beanInstanceIndex, MethodType methodType) {
        this.extensionContext = extensionContext;
        this.beanInstanceIndex = beanInstanceIndex;
        this.methodType = methodType;
    }

    /** {@inheritDoc} */
    @Override
    public int beanInstanceIndex() {
        return beanInstanceIndex;
    }

    /** {@inheritDoc} */
    @Override
    public MethodType methodType() {
        return methodType;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresArena() {
        return extensionContext != -1;
    }

    /** {@inheritDoc} */
    @Override
    public InvocationType withArg(Class<?> type) {
        requireNonNull(type, "type is null");
        MethodType mt = methodType.appendParameterTypes(type);
        return new PackedInvocationType(extensionContext, beanInstanceIndex, mt);
    }

    /** {@inheritDoc} */
    @Override
    public InvocationType withBeanInstance(Class<?> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        if (beanInstanceIndex != -1) {
            throw new UnsupportedOperationException("Already has a bean instance at index " + beanInstanceIndex);
        }
        int index = extensionContext == -1 ? 0 : 1;
        return new PackedInvocationType(extensionContext, index, methodType);
    }

    /** {@inheritDoc} */
    @Override
    public InvocationType withReturnType(Class<?> returnType) {
        requireNonNull(returnType, "returnType is null");
        MethodType mt = methodType.changeReturnType(returnType);
        return new PackedInvocationType(extensionContext, beanInstanceIndex, mt);
    }
}