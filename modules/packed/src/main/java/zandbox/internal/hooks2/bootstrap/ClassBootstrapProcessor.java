package zandbox.internal.hooks2.bootstrap;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import app.packed.base.Nullable;
import packed.internal.base.variable2.FieldVar;
import packed.internal.base.variable2.Var;
import packed.internal.util.MemberScanner;
import packed.internal.util.OpenClass;
import zandbox.internal.hooks2.bootstrap.VarModel.Settings;
import zandbox.internal.hooks2.repository.HookRepository;
import zandbox.internal.hooks2.repository.MetaHookRepository;

public abstract class ClassBootstrapProcessor {

    /**
     * A common super class that exposes annotation information. We have this in place because we will likely have meta
     * annotation functionality at some point.
     */
    public static abstract class AbstractAnnotatedElementProcessor {

        private final AnnotatedElement annotatedElement;

        private AbstractAnnotatedElementProcessor(AnnotatedElement annotatedElement) {
            this.annotatedElement = requireNonNull(annotatedElement);
        }

        public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return annotatedElement.getAnnotation(annotationClass);
        }

        public final Annotation[] getAnnotations() {
            return annotatedElement.getAnnotations();
        }

        public final <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
            return annotatedElement.getAnnotationsByType(annotationClass);
        }

        /**
         * Returns true if an annotation for the specified type is <em>present</em> on the hooked field, else false.
         * 
         * @param annotationClass
         *            the Class object corresponding to the annotation type
         * @return true if an annotation for the specified annotation type is present on the hooked field, else false
         * 
         * @see Field#isAnnotationPresent(Class)
         */
        public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return annotatedElement.isAnnotationPresent(annotationClass);
        }
    }

    public static class Builder extends MemberScanner {
        final OpenClass oc;

        final HookRepository repository;

        public Builder(Class<?> clazz) {
            super(clazz);
            oc = OpenClass.of(MethodHandles.lookup(), clazz);
            repository = new MetaHookRepository();
        }

        /**
         * Creates a new field processor for the specified field.
         * 
         * @param field
         *            the field to return a processor for
         * @return the new field processor
         */
        public FieldProcessor newFieldProcessor(Field field) {
            return new FieldProcessor(this, field);
        }

        public MethodProcessor newMethodProcessor(Method method) {
            return new MethodProcessor(this, method);
        }

        public void build() {
            scan(true, Object.class);
        }

        @Override
        protected void onField(Field field) {
            ClassBootstrapProcessorHelper.scanFieldForAnnotations(this, field);
        }

        @Override
        protected void onMethod(Method method) {
            ClassBootstrapProcessorHelper.scanMethodForAnnotations(this, method);

        }
    }

    public static abstract class VariableProcessor extends AbstractAnnotatedElementProcessor {
        public final VarModel va;

        VariableProcessor(Var var, VarModel va) {
            super(var);
            this.var = requireNonNull(var);
            this.va = requireNonNull(va);
        }

        public final Var var;

        public abstract Type getActualParameterizedType();

        public final Class<?> getActualType() {
            return var.getType();
        }
    }

    public static final class FieldVariableProcessor extends VariableProcessor {
        final FieldProcessor processor;

        FieldVariableProcessor(FieldProcessor processor, Var var, VarModel va) {
            super(var, va);
            this.processor = requireNonNull(processor);
        }

        @Override
        public Type getActualParameterizedType() {
            return processor.field.getGenericType();
        }
    }

    public static final class ParameterVariableProcessor extends VariableProcessor {
        final Parameter parameter;

        ParameterVariableProcessor(Parameter parameter) {
            super(null, null);
            throw new UnsupportedOperationException();
        }

        @Override
        public Type getActualParameterizedType() {
            return parameter.getParameterizedType();
        }
    }

    /**
     * <p>
     * If a field activates multiple hooks, each hook will share the same field processor.
     */
    public static final class FieldProcessor extends AbstractAnnotatedElementProcessor {

        /** The class builder. */
        private final Builder builder;

        /**
         * Any module the field is exposed to. A field instance can only be exposed to a single module. After which is must be
         * cloned.
         */
        @Nullable
        private Module exposedTo;

        /** The field we are processing. */
        private final Field field;

        private FieldProcessor(Builder builder, Field field) {
            super(field);
            this.builder = builder;
            this.field = field;
        }

        VariableProcessor toVariable(Settings request) {
            Var va = new FieldVar(field);
            VarModel vaa = new VarModel.Builder(va).build(request);

            return new FieldVariableProcessor(this, va, vaa);
        }

        /**
         * Exposes the field to an end-user.
         * <p>
         * 
         * @param toModule
         *            the module the field is exposed to
         * @return the field
         */
        public Field expose(Module toModule) {
            if (exposedTo == null) {
                exposedTo = toModule;
            } else if (exposedTo != toModule) {
                // we do not want to share a field instance across different modules because of Field#setAccessible
                try {
                    return field.getDeclaringClass().getDeclaredField(field.getName());
                } catch (NoSuchFieldException e) {
                    throw new IllegalStateException(e);
                }
            }
            return field;
        }

        /** {@return unreflects the underlying field as specified by Lookup#unreflectGetter(Field).} */
        public MethodHandle unreflectGetter() {
            return builder.oc.unreflectGetter(field);
        }

        /** {@return unreflects the underlying field as specified by Lookup#unreflectSetter(Field).} */
        public MethodHandle unreflectSetter() {
            return builder.oc.unreflectSetter(field);
        }

        /** {@return unreflects the underlying field as specified by Lookup#unreflectVarHandle(Field).} */
        public VarHandle unreflectVarhandle() {
            return builder.oc.unreflectVarHandle(field);
        }
    }

    public static final class MethodProcessor extends AbstractAnnotatedElementProcessor {

        /** The class builder. */
        private final Builder builder;

        /**
         * Any module the method is exposed to. A method instance can only be exposed to a single module. After which is must be
         * cloned.
         */
        @Nullable
        private Module exposedTo;

        /** The method we are processing. */
        private final Method method;

        private MethodProcessor(Builder builder, Method method) {
            super(method);
            this.builder = builder;
            this.method = method;
        }

        public Method expose(Module module) {
            if (exposedTo == null) {
                exposedTo = module;
            } else if (exposedTo != module) {
                // we do not want to share a method instance across different modules because of Method#setAccessible
                try {
                    return method.getDeclaringClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
            }
            return method;
        }

        /** {@return unreflects the underlying method as specified by Lookup#unreflect(Method).} */
        public MethodHandle unreflect() {
            return builder.oc.unreflect(method);
        }
    }

}
