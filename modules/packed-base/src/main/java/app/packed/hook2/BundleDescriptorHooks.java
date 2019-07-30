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
package app.packed.hook2;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.packed.hook.AnnotatedFieldHook;

/**
 *
 */
public class BundleDescriptorHooks {

    // Permissions-> For AOP, For Invocation, for da shizzla

    public Map<Class<? extends Class<?>>, Collection<AnnotatedFieldHook<?>>> annotatedFieldExports() {
        return Map.of();
    }

    /**
     * Returns a collection of all exported annotated field hooks of the particular type.
     * 
     * @param <T>
     *            the type of field annotation
     * @param annotationType
     *            the type of field hook
     * @return a collection of all exported annotated field hooks of the particular type
     */
    /// Ehmmm no, AnnotatedFieldHook is unsafe
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends Annotation> Collection<AnnotatedFieldHook<T>> annotatedFieldExports(Class<T> annotationType) {
        requireNonNull(annotationType, "annotationType is null");
        return (Collection) annotatedFieldExports().getOrDefault(annotationType, List.of());
    }

    /**
     * Returns a collection of all hooks that the bundle exports in no particular order.
     * 
     * @return a collection of all hooks that the bundle exports in no particular order
     */
    // Maybe have a HookVisitor...
    public Collection<Hook> exports() {
        return Set.of();
    }

    public final class Builder {}
    // captures
    // exposes

    // expose hooks, capture hooks

    // Another key feature is hooks.
}
