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
package app.packed.bean.hooks.sandboxinvoke.mirror;

import java.lang.annotation.Annotation;

import app.packed.base.Key;
import app.packed.component.Realm;

/**
 *
 */
public sealed interface ResolvedDependency extends Dependency {

    // Er vel kun interessant for service???
    // Hooks er jo altid kun lokale
    Object injectionScope();

    // The same value for the bean instance or the bean?? Maybe it does not make sense
    default boolean isConstant() {
        return producerSite() instanceof ProducerSite.ConstantProducerSite;
    }

    default Realm producer() {
        return producerSite().producer();
    }

    ProducerSite producerSite();

    non-sealed interface OfAnnotatedDependencyHook extends ResolvedDependency {

        Annotation annotation();

        default Class<? extends Annotation> annotationType() {
            return annotation().annotationType();
        }
    }

    non-sealed interface OfService extends ResolvedDependency /* ServiceDependency */ {

        /** {@return the key under which the service is provided.} */
        Key<?> key();

    }

    non-sealed interface OfSpecial extends ResolvedDependency {}

    non-sealed interface OfTypedDependencyHook extends ResolvedDependency {}
}
