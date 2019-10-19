/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packed.internal.hook;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.util.InvalidDeclarationException;
import packed.internal.reflect.ConstructorFinder;
import packed.internal.reflect.MemberFinder;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.TypeUtil;

/**
 * An {@link HookBuilderModel} wraps
 */
final class HookBuilderModel {

    /** A cache of information for aggregator types. */
    private static final ClassValue<HookBuilderModel> MODEL_CACHE = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected HookBuilderModel computeValue(Class<?> type) {
            return new HookBuilderModel.Builder((Class<? extends Hook.Builder<?>>) type).build();
        }
    };

    /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
    final Map<Class<? extends Annotation>, MethodHandle> annotatedFields;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final Map<Class<? extends Annotation>, MethodHandle> annotatedMethods;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final Map<Class<? extends Annotation>, MethodHandle> annotatedTypes;

    // /** The type of aggregator. */
    // private final Class<?> builderType;

    /** The method handle used to create a new instances. */
    private final MethodHandle constructor;

    /** The type of result the aggregator produces. */
    private final Class<?> groupType;

    /**
     * Creates a new model from the specified builder.
     * 
     * @param builder
     *            the builder to create a model from
     */
    private HookBuilderModel(Builder builder) {
        this.constructor = ConstructorFinder.find(builder.p.actualType);
        // this.builderType = builder.p.actualType;
        this.groupType = builder.groupType;
        this.annotatedMethods = Map.copyOf(builder.p.annotatedMethods);
        this.annotatedFields = Map.copyOf(builder.p.annotatedFields);
        this.annotatedTypes = Map.copyOf(builder.p.annotatedTypes);
    }

    /**
     * Returns the type of group the builder produces.
     * 
     * @return the type of group the builder produces
     */
    final Class<?> groupType() {
        return groupType;
    }

    /**
     * Creates a new instance.
     * 
     * @return a new instance
     */
    public static Hook.Builder<?> newInstance(Class<? extends Hook.Builder<?>> type) {
        HookBuilderModel m = of(type);
        try {
            return (Hook.Builder<?>) m.constructor.invoke();
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Returns a model for the specified hook group builder type.
     * 
     * @param type
     *            the type of hook group builder
     * @return a model for the specified group builder type
     */
    public static HookBuilderModel of(Class<? extends Hook.Builder<?>> type) {
        return MODEL_CACHE.get(type);
    }

    /** A builder object, that extract relevant methods from a hook group builder. */
    private static class Builder {

        /** An type variable extractor to extract the type of hook group the builder produces. */
        private static final TypeVariableExtractor AGGREGATE_BUILDER_TV_EXTRACTOR = TypeVariableExtractor.of(Hook.Builder.class);

        /** The type of hook group the builder produces. */
        private final Class<?> groupType;

        final HookClassBuilder p;

        /**
         * Creates a new builder for the specified hook group builder type.
         * 
         * @param builderType
         *            the type of hook group builder
         */
        @SuppressWarnings({ "rawtypes" })
        private Builder(Class<? extends Hook.Builder<?>> builderType) {
            p = new HookClassBuilder(builderType, true);
            this.groupType = (Class) AGGREGATE_BUILDER_TV_EXTRACTOR.extract(builderType);
            TypeUtil.checkClassIsInstantiable(builderType);
        }

        HookBuilderModel build() {
            MemberFinder.findMethods(Hook.Builder.class, p.actualType, m -> p.processMethod(m));
            if (p.annotatedFields.isEmpty() && p.annotatedMethods.isEmpty() && p.annotatedTypes.isEmpty()) {
                throw new InvalidDeclarationException("Hook aggregator builder '" + StringFormatter.format(p.actualType)
                        + "' must define at least one method annotated with @" + OnHook.class.getSimpleName());
            }
            return new HookBuilderModel(this);
        }

    }
}

// void invokeOnHook(HookGroupBuilder<?> groupBuilder, AnnotatedFieldHook<?> hook) {
// if (groupBuilder.getClass() != builderType) {
// throw new IllegalArgumentException("Must be specify an aggregator of type " + builderType + ", but was " +
// groupBuilder.getClass());
// }
// Class<? extends Annotation> an = hook.annotation().annotationType();
//
// MethodHandle om = annotatedFields.get(an);
// if (om == null) {
// // We will normally have checked for this previously
// throw new IllegalStateException("" + an);
// }
//
// try {
// om.invoke(groupBuilder, hook);
// } catch (Throwable e) {
// ThrowableUtil.rethrowErrorOrRuntimeException(e);
// throw new UndeclaredThrowableException(e);
// }
// }
//
// void invokeOnHook(HookGroupBuilder<?> hgb, AnnotatedMethodHook<?> hook) {
// if (hgb.getClass() != builderType) {
// throw new IllegalArgumentException("Must be specify an aggregator of type " + builderType + ", but was " +
// hgb.getClass());
// }
// Class<? extends Annotation> an = hook.annotation().annotationType();
//
// MethodHandle om = annotatedMethods.get(an);
// if (om == null) {
// throw new IllegalStateException("" + an);
// }
//
// try {
// om.invoke(hgb, hook);
// } catch (Throwable e) {
// ThrowableUtil.rethrowErrorOrRuntimeException(e);
// throw new UndeclaredThrowableException(e);
// }
// }
//
// void invokeOnHook(HookGroupBuilder<?> aggregator, AnnotatedTypeHook<?> hook) {
// if (aggregator.getClass() != builderType) {
// throw new IllegalArgumentException("Must be specify an aggregator of type " + builderType + ", but was " +
// aggregator.getClass());
// }
// throw new UnsupportedOperationException();
// }
