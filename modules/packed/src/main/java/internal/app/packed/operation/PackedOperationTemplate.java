package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;

import app.packed.extension.ContainerContext;
import app.packed.extension.context.ContextTemplate;
import app.packed.extension.operation.OperationTemplate;

public final class PackedOperationTemplate implements OperationTemplate {

    public static PackedOperationTemplate DEFAULTS = new PackedOperationTemplate(0, -1, MethodType.methodType(void.class, ContainerContext.class), false);
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

    public OperationTemplate withArg(Class<?> type) {
        requireNonNull(type, "type is null");
        MethodType mt = methodType.appendParameterTypes(type);
        return new PackedOperationTemplate(extensionContext, beanInstanceIndex, mt, ignoreReturn);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate withBeanInstance(Class<?> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        if (beanInstanceIndex != -1) {
            throw new UnsupportedOperationException("Already has a bean instance at index " + beanInstanceIndex);
        }
        int index = extensionContext == -1 ? 0 : 1;
        return new PackedOperationTemplate(extensionContext, index, methodType, ignoreReturn);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate returnType(Class<?> returnType) {
        requireNonNull(returnType, "returnType is null");
        MethodType mt = methodType.changeReturnType(returnType);
        return new PackedOperationTemplate(extensionContext, beanInstanceIndex, mt, ignoreReturn);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate returnIgnore() {
        MethodType mt = methodType.changeReturnType(void.class);
        return new PackedOperationTemplate(extensionContext, beanInstanceIndex, mt, true);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate withContext(ContextTemplate context) {
        return withArg(context.valueClass());
    }
}