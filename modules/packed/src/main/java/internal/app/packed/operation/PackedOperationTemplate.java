package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import app.packed.context.Context;
import app.packed.extension.ExtensionContext;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.context.publish.ContextTemplate;
import sandbox.extension.operation.OperationTemplate;

public final class PackedOperationTemplate implements OperationTemplate {

    public static PackedOperationTemplate DEFAULTS = new PackedOperationTemplate(Map.of(), 0, -1, MethodType.methodType(void.class, ExtensionContext.class),
            false);

    final int beanInstanceIndex;

    public final Map<Class<? extends Context<?>>, PackedContextTemplate> contexts;

    final int extensionContext;

    boolean ignoreReturn;
    final MethodType methodType;

    public PackedOperationTemplate(Map<Class<? extends Context<?>>, PackedContextTemplate> contexts, int extensionContext, int beanInstanceIndex,
            MethodType methodType, boolean ignoreReturn) {
        this.contexts = contexts;
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
    public OperationTemplate returnIgnore() {
        MethodType mt = methodType.changeReturnType(void.class);
        return new PackedOperationTemplate(contexts, extensionContext, beanInstanceIndex, mt, true);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate returnType(Class<?> returnType) {
        requireNonNull(returnType, "returnType is null");
        MethodType mt = methodType.changeReturnType(returnType);
        return new PackedOperationTemplate(contexts, extensionContext, beanInstanceIndex, mt, ignoreReturn);
    }

    public PackedOperationTemplate withArg(Class<?> type) {
        requireNonNull(type, "type is null");
        MethodType mt = methodType.appendParameterTypes(type);
        return new PackedOperationTemplate(contexts, extensionContext, beanInstanceIndex, mt, ignoreReturn);
    }

    /** {@inheritDoc} */
    @Override
    public PackedOperationTemplate appendBeanInstance(Class<?> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        if (beanInstanceIndex != -1) {
            throw new UnsupportedOperationException("Already has a bean instance at index " + beanInstanceIndex);
        }
        int index = extensionContext == -1 ? 0 : 1;
        return new PackedOperationTemplate(contexts, extensionContext, index, methodType, ignoreReturn);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate withContext(ContextTemplate context) {
        Map<Class<? extends Context<?>>, PackedContextTemplate> m = new HashMap<>(contexts);
        if (m.putIfAbsent(context.contextClass(), (PackedContextTemplate) context) != null) {
            throw new IllegalArgumentException("This template already contains the context " + context.contextClass());
        }
        m = Map.copyOf(m);
        MethodType mt = methodType.appendParameterTypes(context.contextImplementationClass());
        return new PackedOperationTemplate(m, extensionContext, beanInstanceIndex, mt, ignoreReturn);
    }

    public record PackedOperationTemplateDescriptor(PackedOperationTemplate pot) implements OperationTemplate.Descriptor {

        /** {@inheritDoc} */
        @Override
        public int beanInstanceIndex() {
            return pot.beanInstanceIndex;
        }

        /** {@inheritDoc} */
        @Override
        public Map<Class<?>, ContextTemplate.Descriptor> contexts() {
            HashMap<Class<?>, ContextTemplate.Descriptor> m = new HashMap<>();
            pot.contexts.forEach((k, v) -> m.put(k, v.descriptor()));
            return Map.copyOf(m);
        }

        /** {@inheritDoc} */
        @Override
        public MethodType invocationType() {
            return pot.methodType;
        }

    }

    /** {@inheritDoc} */
    @Override
    public Descriptor descriptor() {
        return new PackedOperationTemplateDescriptor(this);
    }
}