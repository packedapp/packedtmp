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
package packed.internal.hook.model;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Set;

import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.ThrowableFactory;

/**
 * Something that contains methods with {@link OnHook}
 */
public class OnHookContainerModel {

    /** Any builders that this model depends on. */
    final List<OnHookContainerModel> builders = List.of();

    final Class<?> containerType = null;

    // Is null for a non-hook builder container, do we want a subclass??
    @Nullable
    final MethodHandle hookBuilderConstructor = null;

    /**
     * Returns a set of all
     * 
     * @return stuff
     */
    public Set<Class<?>> annotatedFieldTargets() {
        return Set.of();
    }

    /**
     * Returns a model for the specified extension type.
     * <p>
     * This method assumes that it has already been established that the specified extension type is open to
     * 'app.packed.base'
     * 
     * @param cp
     *            the class processor.
     * @return a container model for the specified extension
     */
    public OnHookContainerModel ofExtension(ClassProcessor cp) {
        throw new UnsupportedOperationException();
    }

    public OnHookContainerModel ofBundle(ClassProcessor cp) {
        throw new UnsupportedOperationException();
    }

    public <T extends Throwable> OnHookContainerModel ofBundle(ClassProcessor cp, ThrowableFactory<T> tf, Class<?>... additionalParameterTypes) throws T {
        // Eneste problem er nok at vi gerne f.eks. vil skrive the Extension
        // Mht til ClassProcessor saa vil vi ogsaa gerne have en MaxType... F.eks. Bundle eller Extension..
        throw new UnsupportedOperationException();
    }

}
