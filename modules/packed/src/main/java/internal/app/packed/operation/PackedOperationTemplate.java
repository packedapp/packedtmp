package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;

import app.packed.extension.ExtensionContext;
import app.packed.operation.BeanOperationTemplate;

public final class PackedOperationTemplate implements BeanOperationTemplate {

    public static PackedOperationTemplate DEFAULTS = new PackedOperationTemplate(0, -1, MethodType.methodType(void.class, ExtensionContext.class), false);
    final int beanInstanceIndex;
    final int extensionContext;

    final MethodType methodType;
    boolean ignoreReturn;

    public PackedOperationTemplate(int extensionContext, int beanInstanceIndex, MethodType methodType, boolean ignoreReturn) {
        this.extensionContext = extensionContext;
        this.beanInstanceIndex = beanInstanceIndex;
        if (ignoreReturn) {
            methodType = methodType.changeReturnType(void.class);
        }
        this.methodType = methodType;
        this.ignoreReturn = ignoreReturn;
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
    public BeanOperationTemplate withArg(Class<?> type) {
        requireNonNull(type, "type is null");
        MethodType mt = methodType.appendParameterTypes(type);
        return new PackedOperationTemplate(extensionContext, beanInstanceIndex, mt, ignoreReturn);
    }

    /** {@inheritDoc} */
    @Override
    public BeanOperationTemplate withBeanInstance(Class<?> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        if (beanInstanceIndex != -1) {
            throw new UnsupportedOperationException("Already has a bean instance at index " + beanInstanceIndex);
        }
        int index = extensionContext == -1 ? 0 : 1;
        return new PackedOperationTemplate(extensionContext, index, methodType, ignoreReturn);
    }

    /** {@inheritDoc} */
    @Override
    public BeanOperationTemplate withReturnType(Class<?> returnType) {
        requireNonNull(returnType, "returnType is null");
        MethodType mt = methodType.changeReturnType(returnType);
        return new PackedOperationTemplate(extensionContext, beanInstanceIndex, mt, ignoreReturn);
    }

    /** {@inheritDoc} */
    @Override
    public BeanOperationTemplate withIgnoreReturn() {
        MethodType mt = methodType.changeReturnType(void.class);
        return new PackedOperationTemplate(extensionContext, beanInstanceIndex, mt, true);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isIgnoreReturn() {
        return ignoreReturn;
    }

    /** {@inheritDoc} */
    @Override
    public int extensionContextIndex() {
        return extensionContext;
    }
}