package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.context.ContextTemplate;
import app.packed.extension.ExtensionContext;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanIntrospectorSetup;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.lifecycle.lifetime.runtime.PackedExtensionContext;

/** Implementation of {@link OperationTemplate}. */
public final class PackedOperationTemplate implements OperationTemplate {

    public static PackedOperationTemplate DEFAULTS = new PackedOperationTemplate(ReturnKind.CLASS, Object.class, true, null, List.of(), List.of());

    /** Additional args */
    // I don't know if it useful, can't remember if we ditched it because of embedded args
    // Where it doesn't really make sense to talk about the position
    public final List<Class<?>> args;

    /** Of the operation takes a bean instance. What type of instance does it take */
    @Nullable
    public final Class<?> beanClass;

    public final List<PackedContextTemplate> contexts;

    /** Do the operation require an ExtensionContext to work. */
    public final boolean extensionContextFlag;

    public final MethodType methodType;

    /** The return type of the operation. */
    public final Class<?> returnClass;

    public final ReturnKind returnKind;

    PackedOperationTemplate(ReturnKind returnKind, Class<?> returnClass, boolean extensionContextFlag, @Nullable Class<?> beanClass,
            List<PackedContextTemplate> contexts, List<Class<?>> args) {

        this.contexts = contexts;
        this.extensionContextFlag = extensionContextFlag;
        this.args = args;
        this.returnKind = returnKind;
        this.returnClass = returnClass;
        this.beanClass = beanClass;
        ArrayList<Class<?>> newMt = new ArrayList<>();
        if (extensionContextFlag) {
            newMt.add(ExtensionContext.class);

        }
        for (PackedContextTemplate pct : contexts) {
            if (!pct.bindAsConstant()) {
                newMt.add(pct.contextImplementationClass());
            }
        }
        newMt.addAll(args);
        methodType = MethodType.methodType(returnClass, newMt);
        if (methodType== MethodType.methodType(Object.class, ExtensionContext.class, PackedExtensionContext.class)) {
            throw new Error();
        }
    }

    // I think just a nullable Class
    // And then bean instance is always param 1
    PackedOperationTemplate appendBeanInstance(Class<?> beanClass) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public PackedOperationTemplate configure(Consumer<? super Configurator> configure) {
        return PackedOperationTemplate.configure(this, configure);
    }

    /** {@inheritDoc} */
    @Override
    public Descriptor descriptor() {
        return new PackedOperationTemplateDescriptor(this);
    }

    public PackedOperationInstaller newInstaller(BeanIntrospectorSetup extension, MethodHandle methodHandle, OperationMemberTarget<?> target,
            OperationType operationType) {
        return new PackedOperationInstaller(this, operationType, extension.scanner.bean, extension.extension) {

            @SuppressWarnings("unchecked")
            @Override
            public final <H extends OperationHandle<?>> H install(Function<? super OperationInstaller, H> handleFactory) {
                OperationSetup operation = newOperationFromMember(target, methodHandle, handleFactory);
                extension.scanner.unBoundOperations.add(operation);
                return (H) operation.handle();
            }
        };
    }

    public PackedOperationInstaller newInstaller(OperationType operationType, BeanSetup bean, ExtensionSetup operator) {
        return new PackedOperationInstaller(this, operationType, bean, operator);
    }

    /**
     * @return
     */
    public PackedOperationTemplate withRaw() {
        return new PackedOperationTemplate(ReturnKind.IGNORE, returnClass, false, beanClass, contexts, args);
    }

    PackedOperationTemplate withReturnIgnore() {
        return new PackedOperationTemplate(ReturnKind.IGNORE, void.class, extensionContextFlag, beanClass, contexts, args);
    }

    /** {@inheritDoc} */
    public PackedOperationTemplate withReturnType(Class<?> returnType) {
        requireNonNull(returnType, "returnType is null");
        return new PackedOperationTemplate(ReturnKind.CLASS, returnType, extensionContextFlag, beanClass, contexts, args);
    }

    /**
     * @return
     */
    public PackedOperationTemplate withReturnTypeDynamic() {
        return new PackedOperationTemplate(ReturnKind.DYNAMIC, Object.class, extensionContextFlag, beanClass, contexts, args);
    }

    public PackedOperationTemplate withArg(Class<?> type) {
        requireNonNull(type, "type is null");
        return new PackedOperationTemplate(returnKind, returnClass, extensionContextFlag, beanClass, contexts, args);
    }

    /** {@inheritDoc} */
    public PackedOperationTemplate withContext(ContextTemplate context) {
        PackedContextTemplate c = (PackedContextTemplate) context;
        ArrayList<PackedContextTemplate> m = new ArrayList<>(contexts);
        for (PackedContextTemplate pct : m) {
            if (pct.contextClass() == c.contextClass()) {
                throw new IllegalArgumentException("This template already contains the context " + context.contextClass());
            }
        }
        m.add(c);
        return new PackedOperationTemplate(returnKind, returnClass, extensionContextFlag, beanClass, List.copyOf(m), args);
    }

    public static PackedOperationTemplate configure(PackedOperationTemplate template, Consumer<? super Configurator> configure) {
        PackedOperationTemplateConfigurator c = new PackedOperationTemplateConfigurator();
        c.template = template;
        configure.accept(c);
        return c.template;
    }

    /** Implementation of {@link OperationTemplate.Configurator} */
    public static final class PackedOperationTemplateConfigurator implements OperationTemplate.Configurator {

        /** The template we are configuring. */
        private PackedOperationTemplate template;

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
        public Configurator raw() {
            this.template = template.withRaw();
            return this;

        }

        /** {@inheritDoc} */
        @Override
        public Configurator returnIgnore() {
            this.template = template.withReturnIgnore();
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator returnType(Class<?> type) {
            this.template = template.withReturnType(type);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator returnTypeDynamic() {
            this.template = template.withReturnTypeDynamic();
            return this;
        }
    }

    public enum ReturnKind {
        CLASS, DYNAMIC, IGNORE;
    }

    public record PackedOperationTemplateDescriptor(PackedOperationTemplate pot) implements OperationTemplate.Descriptor {

        /** {@inheritDoc} */
        @Override
        public int beanInstanceIndex() {
            throw new UnsupportedOperationException();
//            return 0 pot.beanInstanceIndex;
        }

        /** {@inheritDoc} */
        @Override
        public Map<Class<?>, ContextTemplate> contexts() {
            HashMap<Class<?>, ContextTemplate> m = new HashMap<>();
            pot.contexts.forEach(k -> m.put(k.contextClass(), k));
            return Map.copyOf(m);
        }

        /** {@inheritDoc} */
        @Override
        public MethodType invocationType() {
            return pot.methodType;
        }
    }

}