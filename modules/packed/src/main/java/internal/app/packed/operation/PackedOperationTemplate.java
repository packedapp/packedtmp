package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.context.Context;
import app.packed.context.ContextTemplate;
import app.packed.extension.ExtensionContext;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanScannerParticipant;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.extension.ExtensionSetup;

public final class PackedOperationTemplate implements OperationTemplate {

    public static PackedOperationTemplate DEFAULTS = new PackedOperationTemplate(Map.of(), 0, -1, MethodType.methodType(void.class, ExtensionContext.class),
            false);

    public static OperationTemplate RAW = new PackedOperationTemplate(Map.of(), -1, -1, MethodType.methodType(void.class), false);

    final int beanInstanceIndex;

    public final Map<Class<? extends Context<?>>, PackedContextTemplate> contexts;

    final int extensionContext;

    final boolean ignoreReturn;

    public final MethodType methodType;

    PackedOperationTemplate(Map<Class<? extends Context<?>>, PackedContextTemplate> contexts, int extensionContext, int beanInstanceIndex,
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

    PackedOperationTemplate appendBeanInstance(Class<?> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        if (beanInstanceIndex != -1) {
            throw new UnsupportedOperationException("Already has a bean instance at index " + beanInstanceIndex);
        }
        int index = extensionContext == -1 ? 0 : 1;
        return new PackedOperationTemplate(contexts, extensionContext, index, methodType, ignoreReturn);
    }

    /** {@inheritDoc} */
    @Override
    public Descriptor descriptor() {
        return new PackedOperationTemplateDescriptor(this);
    }

    public PackedOperationInstaller newInstaller(BeanScannerParticipant extension, MethodHandle methodHandle, OperationMemberTarget<?> target,
            OperationType operationType) {
        return new PackedOperationInstaller(this, operationType, extension.scanner.bean, extension.extension) {

            @SuppressWarnings("unchecked")
            @Override
            public final <H extends OperationHandle<?>> H install(Function<? super OperationInstaller, H> handleFactory) {
                OperationSetup operation = PackedOperationInstaller.newOperationFromMember(this, target, methodHandle, handleFactory);
                extension.scanner.unBoundOperations.add(operation);
                return (H) operation.handle();
            }
        };
    }

    public PackedOperationInstaller newInstaller(OperationType operationType, BeanSetup bean, ExtensionSetup operator) {
        return new PackedOperationInstaller(this, operationType, bean, operator);
    }

    /** {@inheritDoc} */
    @Override
    public PackedOperationTemplate reconfigure(Consumer<? super Configurator> configure) {
        return PackedOperationTemplate.configure(this, configure);
    }

    PackedOperationTemplate returnIgnore() {
        MethodType mt = methodType.changeReturnType(void.class);
        return new PackedOperationTemplate(contexts, extensionContext, beanInstanceIndex, mt, true);
    }

    /** {@inheritDoc} */
    public PackedOperationTemplate returnType(Class<?> returnType) {
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
    public PackedOperationTemplate withContext(ContextTemplate context) {
        PackedContextTemplate c = (PackedContextTemplate) context;
        Map<Class<? extends Context<?>>, PackedContextTemplate> m = new HashMap<>(contexts);
        if (m.putIfAbsent(context.contextClass(), c) != null) {
            throw new IllegalArgumentException("This template already contains the context " + context.contextClass());
        }
        //
        m = Map.copyOf(m);
        MethodType mt;
        if (!c.bindAsConstant()) {
            mt = methodType.appendParameterTypes(context.contextImplementationClass());
        } else {
            mt = methodType;
        }
        return new PackedOperationTemplate(m, extensionContext, beanInstanceIndex, mt, ignoreReturn);
    }

    public static PackedOperationTemplate configure(PackedOperationTemplate template, Consumer<? super Configurator> configure) {
        PackedOperationConfigurator c = new PackedOperationConfigurator(template);
        configure.accept(c);
        return c.template;
    }

    public static final class PackedOperationConfigurator implements OperationTemplate.Configurator {

        /** The template we are configuring. */
        private PackedOperationTemplate template;

        private PackedOperationConfigurator(PackedOperationTemplate template) {
            this.template = template;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator appendBeanInstance(Class<?> beanClass) {
            this.template = template.appendBeanInstance(beanClass);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator inContext(ContextTemplate context) {
            this.template = template.withContext(context);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator returnIgnore() {
            this.template = template.returnIgnore();
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator returnType(Class<?> type) {
            this.template = template.returnType(type);
            return this;
        }
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

}