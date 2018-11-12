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
package packed.internal.invokers2;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.inject.BindingMode;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.util.descriptor.InternalAnnotatedElement;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/** A descriptor of the {@link Provides} annotation. */
public final class AtProvides {

    /** The annotated member, either an {@link InternalFieldDescriptor} or ab {@link InternalMethodDescriptor}. */
    private final InternalAnnotatedElement annotatedMember;

    /** The binding mode from {@link Provides#bindingMode()}. */
    private final BindingMode bindingMode;

    /** If the annotation is present on a method, any dependencies (parameters) the method has. */
    private final List<InternalDependency> dependencies;

    /** An (optional) description from {@link Provides#description()}. */
    @Nullable
    private final String description;

    /** The key under which this method will deliver services. */
    private final Key<?> key;

    AtProvides(InternalAnnotatedElement annotatedMember, Key<?> key, Provides provides, List<InternalDependency> dependencies) {
        this.annotatedMember = requireNonNull(annotatedMember);
        this.key = requireNonNull(key, "key is null");
        this.description = provides.description().length() > 0 ? provides.description() : null;
        this.bindingMode = provides.bindingMode();
        this.dependencies = requireNonNull(dependencies);
    }

    /**
     * Returns a {@link InternalFieldDescriptor} or {@link InternalMethodDescriptor} for the annotated field or method.
     * 
     * @return a descriptor of the annotated member
     */
    public InternalAnnotatedElement getAnnotatedMember() {
        return annotatedMember;
    }

    /**
     * Returns the binding mode as defined by {@link Provides#bindingMode()}.
     *
     * @return the binding mode
     */
    public BindingMode getBindingMode() {
        return bindingMode;
    }

    /**
     * Returns a list of any dependencies the annotated method might have. Is always empty for fields.
     * 
     * @return a list of any dependencies the annotated method might have
     */
    public List<InternalDependency> getDependencies() {
        return dependencies;
    }

    /**
     * Returns the (optional) description.
     * 
     * @return the (optional) description
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Returns the key under which the provided service will be made available.
     * 
     * @return the key under which the provided service will be made available
     */
    public Key<?> getKey() {
        return key;
    }

    public static AtProvides from(InternalAnnotatedElement fieldOrMethod, Provides p) {

        // Extract key
        // Men vi skal jo have informationer om hvorfor

        // Saa metoder ved hvorfor, the caller knows where/what
        // WHERE/What could not because of why...
        // Maybe have a isValidKey(Type) or <T> checkValidKey(T extends RuntimeException, String message) throws T;
        // Maybe have a string with "%s, %s".. Maybe A consumer with the message because XYZ
        // because it "xxxxxx"

        throw new UnsupportedOperationException();
    }
}
