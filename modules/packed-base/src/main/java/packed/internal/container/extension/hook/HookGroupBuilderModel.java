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
package packed.internal.container.extension.hook;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Map;

import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.AnnotatedTypeHook;
import app.packed.container.extension.HookGroupBuilder;
import app.packed.container.extension.OnHook;
import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.InvalidDeclarationException;
import packed.internal.reflect.AbstractInstantiableModel;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.TypeUtil;

/**
 * An {@link HookGroupBuilderModel} wraps
 */
final class HookGroupBuilderModel extends AbstractInstantiableModel<HookGroupBuilder<?>> {

    /** A cache of information for aggregator types. */
    private static final ClassValue<HookGroupBuilderModel> MODEL_CACHE = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected HookGroupBuilderModel computeValue(Class<?> type) {
            return new HookGroupBuilderModel.Builder((Class<? extends HookGroupBuilder<?>>) type).build();
        }
    };

    /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
    final Map<Class<? extends Annotation>, MethodHandle> annotatedFields;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final Map<Class<? extends Annotation>, MethodHandle> annotatedMethods;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final Map<Class<? extends Annotation>, MethodHandle> annotatedTypes;

    /** The type of aggregator. */
    private final Class<?> builderType;

    /** The type of result the aggregator produces. */
    private final Class<?> groupType;

    /**
     * Creates a new model from the specified builder.
     * 
     * @param builder
     *            the builder to create a model from
     */
    private HookGroupBuilderModel(Builder builder) {
        super(builder.findNoParameterConstructor());
        this.builderType = builder.actualType;
        this.groupType = builder.groupType;
        this.annotatedMethods = Map.copyOf(builder.annotatedMethods);
        this.annotatedFields = Map.copyOf(builder.annotatedFields);
        this.annotatedTypes = Map.copyOf(builder.annotatedTypes);
    }

    /**
     * Returns the type of group the builder produces.
     * 
     * @return the type of group the builder produces
     */
    final Class<?> groupType() {
        return groupType;
    }

    void invokeOnHook(HookGroupBuilder<?> aggregator, AnnotatedFieldHook<?> hook) {
        if (aggregator.getClass() != builderType) {
            throw new IllegalArgumentException("Must be specify an aggregator of type " + builderType + ", but was " + aggregator.getClass());
        }
        Class<? extends Annotation> an = hook.annotation().annotationType();

        MethodHandle om = annotatedFields.get(an);
        if (om == null) {
            // We will normally have checked for this previously
            throw new IllegalStateException("" + an);
        }

        try {
            om.invoke(aggregator, hook);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new RuntimeException(e);
        }
    }

    void invokeOnHook(HookGroupBuilder<?> aggregator, AnnotatedMethodHook<?> hook) {
        if (aggregator.getClass() != builderType) {
            throw new IllegalArgumentException("Must be specify an aggregator of type " + builderType + ", but was " + aggregator.getClass());
        }
        Class<? extends Annotation> an = hook.annotation().annotationType();

        MethodHandle om = annotatedMethods.get(an);
        if (om == null) {
            throw new IllegalStateException("" + an);
        }

        try {
            om.invoke(aggregator, hook);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new RuntimeException(e);
        }
    }

    void invokeOnHook(HookGroupBuilder<?> aggregator, AnnotatedTypeHook<?> hook) {
        if (aggregator.getClass() != builderType) {
            throw new IllegalArgumentException("Must be specify an aggregator of type " + builderType + ", but was " + aggregator.getClass());
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a model for the specified hook group builder type.
     * 
     * @param type
     *            the type of hook group builder
     * @return a model for the specified group builder type
     */
    public static HookGroupBuilderModel of(Class<? extends HookGroupBuilder<?>> type) {
        return MODEL_CACHE.get(type);
    }

    /** A builder object, that extract relevant methods from a hook group builder. */
    private static class Builder extends OnHookMemberProcessor {

        /** An type variable extractor to extract the type of hook group the builder produces. */
        private static final TypeVariableExtractor AGGREGATE_BUILDER_TV_EXTRACTOR = TypeVariableExtractor.of(HookGroupBuilder.class);

        /** The type of hook group the builder produces. */
        private final Class<?> groupType;

        /**
         * Creates a new builder for the specified hook group builder type.
         * 
         * @param builderType
         *            the type of hook group builder
         */
        @SuppressWarnings({ "rawtypes" })
        private Builder(Class<? extends HookGroupBuilder<?>> builderType) {
            super(HookGroupBuilder.class, builderType, true);
            this.groupType = (Class) AGGREGATE_BUILDER_TV_EXTRACTOR.extract(builderType);

            lookup = MethodHandles.lookup();
            try {
                lookup = MethodHandles.privateLookupIn(builderType, lookup);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new UncheckedIllegalAccessException("In order to use the hook aggregate " + StringFormatter.format(builderType) + ", the module '"
                        + builderType.getModule().getName() + "' in which the class is located must be 'open' to 'app.packed.base'", e);
            }
        }

        HookGroupBuilderModel build() {
            TypeUtil.checkClassIsInstantiable(actualType);
            findMethods();
            if (annotatedFields.isEmpty() && annotatedMethods.isEmpty() && annotatedTypes.isEmpty()) {
                throw new InvalidDeclarationException("Hook aggregator builder '" + StringFormatter.format(actualType)
                        + "' must define at least one method annotated with @" + OnHook.class.getSimpleName());
            }
            return new HookGroupBuilderModel(this);
        }

    }
}
