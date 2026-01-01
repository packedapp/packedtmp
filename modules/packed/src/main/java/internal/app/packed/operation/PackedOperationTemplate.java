package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import app.packed.context.Context;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanIntrospectorSetup;
import internal.app.packed.context.ContextModel;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.lifecycle.runtime.PackedExtensionContext;
import internal.app.packed.operation.PackedOperationTarget.BeanAccessOperationTarget;

/** Implementation of {@link OperationTemplate}. */
public final class PackedOperationTemplate {

    public static PackedOperationTemplate DEFAULTS = new PackedOperationTemplate(ReturnKind.CLASS, Object.class, true, null, List.of(), List.of());

    public final List<Class<? extends Throwable>> allowedThrowables = List.of(Throwable.class);

    /** Additional args */
    // I don't know if it useful, can't remember if we ditched it because of embedded args
    // Where it doesn't really make sense to talk about the position
    public final List<Class<?>> args;

    /** If the operation takes a bean instance. What type of instance does it take */
    @Nullable
    public final Class<?> beanClass;

    public final List<ContextModel> contexts;

    /** Do the operation require an ExtensionContext to work. */
    public final boolean extensionContextFlag;

    public final MethodType methodType;

    /** The return type of the operation. */
    public final Class<?> returnClass;

    public final ReturnKind returnKind;

    PackedOperationTemplate(ReturnKind returnKind, Class<?> returnClass, boolean extensionContextFlag, @Nullable Class<?> beanClass,
            List<ContextModel> contexts, List<Class<?>> args) {

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
        for (ContextModel pct : contexts) {
            if (!pct.bindAsConstant()) {
                newMt.add(pct.contextImplementationClass());
            }
        }
        newMt.addAll(args);
        methodType = MethodType.methodType(returnClass, newMt);
        if (methodType == MethodType.methodType(Object.class, ExtensionContext.class, PackedExtensionContext.class)) {
            throw new Error();
        }
    }

    public boolean newLifetime() {
        return false;
    }

    /** {@inheritDoc} */
    public List<Class<? extends Throwable>> allowedThrowables() {
        return allowedThrowables;
    }

    // I think just a nullable Class
    // And then bean instance is always param 1
    PackedOperationTemplate appendBeanInstance(Class<?> beanClass) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public Map<Class<?>, ContextModel> contexts() {
        HashMap<Class<?>, ContextModel> m = new HashMap<>();
        contexts.forEach(k -> m.put(k.contextClass(), k));
        return Map.copyOf(m);
    }

    /** {@inheritDoc} */
    public MethodType invocationType() {
        return methodType;
    }

    public static PackedOperationInstaller newInstaller(BeanIntrospectorSetup extension, MethodHandle directMH, OperationMemberTarget<?> target,
            OperationType operationType) {
        return new PackedOperationInstaller(PackedOperationTemplate.DEFAULTS, operationType, extension.scanner.bean, extension.extension()) {

            @SuppressWarnings("unchecked")
            @Override
            public final <H extends OperationHandle<?>> H install(Function<? super OperationInstaller, H> handleFactory) {
                OperationSetup operation = newOperationFromMember(target, directMH, handleFactory);
                extension.scanner.unBoundOperations.add(operation);
                return (H) operation.handle();
            }
        };
    }

    public PackedOperationInstaller newInstaller(OperationType operationType, BeanSetup bean, ExtensionSetup operator) {
        return new PackedOperationInstaller(this, operationType, bean, operator);
    }

    public PackedOperationInstaller newInstallerFromBeanAccess(OperationType operationType, BeanSetup bean, ExtensionSetup operator) {
        PackedOperationInstaller poi = newInstaller(operationType, bean, operator);
        poi.operationTarget = new BeanAccessOperationTarget();
        poi.namePrefix = "BeanInstanceAccess";
        return poi;
    }

    /** {@inheritDoc} */
    public PackedOperationTemplate withAppendBeanInstance(Class<?> beanClass) {
        return new PackedBuilder(this).appendBeanInstance(beanClass).build();
    }

    public PackedOperationTemplate withArg(Class<?> type) {
        requireNonNull(type, "type is null");
        return new PackedOperationTemplate(returnKind, returnClass, extensionContextFlag, beanClass, contexts, args);
    }

    public PackedOperationTemplate withContext(Class<? extends Context<?>> contextClass) {
        return new PackedBuilder(this).context(contextClass).build();
    }

    /**
     * @return
     */
    public PackedOperationTemplate withRaw() {
        return new PackedBuilder(this).raw().build();
    }

    public PackedOperationTemplate withReturnIgnore() {
        return new PackedBuilder(this).returnIgnore().build();
    }

    /** {@inheritDoc} */
    public PackedOperationTemplate withReturnType(Class<?> returnType) {
        return new PackedBuilder(this).returnType(returnType).build();
    }

    public PackedOperationTemplate withReturnTypeDynamic() {
        return new PackedBuilder(this).returnTypeDynamic().build();
    }

    public enum ReturnKind {
        CLASS, DYNAMIC, IGNORE;
    }

    /** {@inheritDoc} */
    public PackedOperationTemplate withAllowedThrowables(Class<? extends Throwable> allowed) {
        return new PackedBuilder(this).allowedThrowables(allowed).build();
    }

    public static PackedBuilder builder() {
        return new PackedBuilder();
    }

    /** Implementation of {@link OperationTemplate.Builder}. */
    public static final class PackedBuilder {
        private ReturnKind returnKind = ReturnKind.CLASS;
        private Class<?> returnClass = Object.class;
        private boolean extensionContextFlag = true;
        private Class<?> beanClass = null;
        private List<ContextModel> contexts = List.of();
        private List<Class<?>> args = List.of();
        private List<Class<? extends Throwable>> allowedThrowables = List.of(Throwable.class);

        PackedBuilder() {}

        /** Copy constructor for creating a builder from an existing template. */
        PackedBuilder(PackedOperationTemplate template) {
            this.returnKind = template.returnKind;
            this.returnClass = template.returnClass;
            this.extensionContextFlag = template.extensionContextFlag;
            this.beanClass = template.beanClass;
            this.contexts = template.contexts;
            this.args = template.args;
            this.allowedThrowables = template.allowedThrowables;
        }

        public PackedBuilder context(Class<? extends Context<?>> contextClass) {
            ContextModel c = ContextModel.of(contextClass);
            ArrayList<ContextModel> m = new ArrayList<>(contexts);
            for (ContextModel pct : m) {
                if (pct.contextClass() == c.contextClass()) {
                    throw new IllegalArgumentException("This template already contains the context " + c.contextClass());
                }
            }
            m.add(c);
            this.contexts = List.copyOf(m);
            return this;
        }

        public PackedBuilder returnType(Class<?> type) {
            requireNonNull(type, "type is null");
            this.returnKind = ReturnKind.CLASS;
            this.returnClass = type;
            return this;
        }

        public PackedBuilder returnIgnore() {
            this.returnKind = ReturnKind.IGNORE;
            this.returnClass = void.class;
            return this;
        }

        public PackedBuilder returnTypeDynamic() {
            this.returnKind = ReturnKind.DYNAMIC;
            this.returnClass = Object.class;
            return this;
        }

        public PackedBuilder raw() {
            this.returnKind = ReturnKind.IGNORE;
            this.extensionContextFlag = false;
            return this;
        }

        public PackedBuilder appendBeanInstance(Class<?> beanClass) {
            this.beanClass = beanClass;
            return this;
        }

        public PackedBuilder allowedThrowables(Class<? extends Throwable> allowed) {
            requireNonNull(allowed, "allowed is null");
            ArrayList<Class<? extends Throwable>> list = new ArrayList<>(allowedThrowables);
            list.add(allowed);
            this.allowedThrowables = List.copyOf(list);
            return this;
        }

        public PackedOperationTemplate build() {
            return new PackedOperationTemplate(returnKind, returnClass, extensionContextFlag, beanClass, contexts, args);
        }
    }
}