package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;

import app.packed.operation.OperationTemplate;
import internal.app.packed.lifetime.runtime.PackedExtensionContext;

public final class PackedOperationTemplate implements OperationTemplate {

    public static PackedOperationTemplate DEFAULTS = new PackedOperationTemplate(0, -1, MethodType.methodType(void.class, PackedExtensionContext.class), false);
    final int beanInstanceIndex;
    final int extensionContext;

    final MethodType methodType;
    boolean ignoreReturnType;
    
    public PackedOperationTemplate(int extensionContext, int beanInstanceIndex, MethodType methodType, boolean ignoreReturnType) {
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
    public MethodType invocationType() {
        return methodType;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresExtensionContext() {
        return extensionContext != -1;
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate withArg(Class<?> type) {
        requireNonNull(type, "type is null");
        MethodType mt = methodType.appendParameterTypes(type);
        return new PackedOperationTemplate(extensionContext, beanInstanceIndex, mt, ignoreReturnType);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate withBeanInstance(Class<?> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        if (beanInstanceIndex != -1) {
            throw new UnsupportedOperationException("Already has a bean instance at index " + beanInstanceIndex);
        }
        int index = extensionContext == -1 ? 0 : 1;
        return new PackedOperationTemplate(extensionContext, index, methodType, ignoreReturnType);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate withReturnType(Class<?> returnType) {
        requireNonNull(returnType, "returnType is null");
        MethodType mt = methodType.changeReturnType(returnType);
        return new PackedOperationTemplate(extensionContext, beanInstanceIndex, mt, ignoreReturnType);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate withIgnoreReturnType() {
        MethodType mt = methodType.changeReturnType(void.class);
        return new PackedOperationTemplate(extensionContext, beanInstanceIndex, mt, true);
    }
}