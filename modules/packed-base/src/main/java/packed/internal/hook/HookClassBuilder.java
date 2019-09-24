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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.IdentityHashMap;

import app.packed.component.ComponentConfiguration;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.HookGroupBuilder;
import app.packed.hook.OnHook;
import app.packed.hook.OnHookGroup;
import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.NativeImage;
import packed.internal.util.StringFormatter;

/**
 *
 */
public final class HookClassBuilder {

    public final Class<?> actualType;

    /** Fields annotated with {@link OnHook} taking a single {@link AnnotatedFieldHook} as parameter. */
    public final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields = new IdentityHashMap<>();

    /** Fields annotated with {@link OnHook} taking a single {@link AnnotatedMethodHook} as parameter. */
    public final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods = new IdentityHashMap<>();

    /** Fields annotated with {@link OnHook} taking a single {@link AnnotatedTypeHook} as parameter. */
    public final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes = new IdentityHashMap<>();

    public final IdentityHashMap<Class<?>, MethodHandle> groups = new IdentityHashMap<>();

    /** Components that are of a specific type. */
    public final IdentityHashMap<Class<?>, MethodHandle> instanceOfs = new IdentityHashMap<>();

    private final boolean isHookGroupBuilder;

    public Lookup lookup;

    public HookClassBuilder(Class<?> actualType, boolean isHookGroupBuilder) {
        this.actualType = requireNonNull(actualType);
        this.isHookGroupBuilder = isHookGroupBuilder;

        this.lookup = MethodHandles.lookup();

        // Create lazy??? For example, for bundle it is only needed if the class actually has
        try {
            lookup = MethodHandles.privateLookupIn(actualType, lookup);
        } catch (IllegalAccessException | InaccessibleObjectException e) {
            throw new UncheckedIllegalAccessException("In order to use the hook aggregate " + StringFormatter.format(actualType) + ", the module '"
                    + actualType.getModule().getName() + "' in which the class is located must be 'open' to 'app.packed.base'", e);
        }
    }

    protected void onHook(MethodHandles.Lookup lookup, Method method, Parameter p1, Parameter p2,
            IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotations) {
        ParameterizedType pt = (ParameterizedType) p1.getParameterizedType();
        @SuppressWarnings("unchecked")
        Class<? extends Annotation> annotationType = (Class<? extends Annotation>) pt.getActualTypeArguments()[0];

        // Check that we have not added another previously for the same annotation
        if (annotations.containsKey(annotationType)) {
            throw new InvalidDeclarationException("There are multiple methods annotated with @OnHook on " + StringFormatter.format(method.getDeclaringClass())
                    + " that takes " + p1.getParameterizedType());
        }

        MethodHandle mh;
        try {
            mh = lookup.unreflect(method);
        } catch (IllegalAccessException | InaccessibleObjectException e) {
            throw new UncheckedIllegalAccessException("In order to use the extension " + StringFormatter.format(method.getDeclaringClass()) + ", the module '"
                    + method.getDeclaringClass().getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
        }

        NativeImage.registerMethod(method);
        annotations.put(annotationType, mh);
    }

    /**
     * @param method
     * @param oh
     */
    private void onHookGroup(Method method, OnHookGroup oh) {
        if (isHookGroupBuilder) {
            throw new InvalidDeclarationException(
                    "Cannot use @" + OnHookGroup.class.getSimpleName() + " on a hook group builder, method = " + StringFormatter.format(method));
        }
        Class<? extends HookGroupBuilder<?>> aggregateType = oh.value();

        MethodHandle mh;
        try {
            mh = lookup.unreflect(method);
        } catch (IllegalAccessException | InaccessibleObjectException e) {
            throw new UncheckedIllegalAccessException("In order to use the extension " + StringFormatter.format(method.getDeclaringClass()) + ", the module '"
                    + method.getDeclaringClass().getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
        }

        NativeImage.registerMethod(method);

        groups.put(aggregateType, mh);
        HookGroupBuilderModel oha = HookGroupBuilderModel.of(aggregateType);

        // TODO we should check that the type matches....

        annotatedFields.putAll(oha.annotatedFields);
        annotatedMethods.putAll(oha.annotatedMethods);
        annotatedTypes.putAll(oha.annotatedTypes);
        // aggregators.p

        // Do something
    }

    public void processMethod(Method method) {
        // First see if the method is annotated with @OnHook
        if (method.isAnnotationPresent(OnHook.class)) {
            if (method.isAnnotationPresent(OnHookGroup.class)) {
                throw new InvalidDeclarationException("Cannot use both @" + OnHookGroup.class.getSimpleName() + " and @" + OnHookGroup.class.getSimpleName()
                        + "on a method, method = " + StringFormatter.format(method));
            }
            if (method.getParameterCount() > 0 && method.getParameterCount() <= (isHookGroupBuilder ? 1 : 2)) {
                Parameter[] parameters = method.getParameters();
                Parameter p1 = parameters[0];
                Class<?> p1Type = p1.getType();
                Parameter p2 = null;
                if (isHookGroupBuilder || (method.getParameterCount() == 2 && (p2 = parameters[1]).getType() == ComponentConfiguration.class)) {
                    if (p1Type == AnnotatedFieldHook.class) {
                        onHook(lookup, method, p1, p2, annotatedFields);
                        return;
                    } else if (p1Type == AnnotatedMethodHook.class) {
                        onHook(lookup, method, p1, p2, annotatedMethods);
                        return;
                    } else if (p1Type == AnnotatedTypeHook.class) {
                        onHook(lookup, method, p1, p2, annotatedTypes);
                        return;
                    }
                }
            }

            if (isHookGroupBuilder) {
                throw new InvalidDeclarationException("Methods annotated with @OnHook on hook group builders must have exactly one parameter of type "
                        + AnnotatedFieldHook.class.getSimpleName() + ", " + AnnotatedMethodHook.class.getSimpleName() + ", or"
                        + AnnotatedTypeHook.class.getSimpleName() + ", " + " for method = " + StringFormatter.format(method));
            } else {
                throw new InvalidDeclarationException("stuff");
            }
        }

        // Next, let us see if it is annotated with @OnHookGroup
        OnHookGroup g = method.getAnnotation(OnHookGroup.class);
        if (g != null) {
            onHookGroup(method, g);
        }
    }
}
