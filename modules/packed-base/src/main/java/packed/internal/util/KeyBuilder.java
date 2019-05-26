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
package packed.internal.util;

import app.packed.component.Component;
import app.packed.container.Container;
import app.packed.inject.ProvideHelper;
import app.packed.inject.Injector;
import app.packed.util.Key;

/** A builder of keys, mainly useful for creating proper error messages. */
public final class KeyBuilder {

    /** The {@link Component} interface as a key. */
    public static final Key<Component> COMPONENT_KEY = Key.of(Component.class);

    /** The {@link Container} class as a key. */
    public static final Key<?> CONTAINER_KEY = Key.of(Container.class);

    /** The {@link ProvideHelper} class as a key. */
    public static final Key<?> INJECTION_SITE_KEY = Key.of(ProvideHelper.class);

    /** The {@link Injector} class as a key. */
    public static final Key<?> INJECTOR_KEY = Key.of(Injector.class);

    /** Cannot instantiate. */
    private KeyBuilder() {}
}

// Saa metoder ved hvorfor, the caller knows where/what
// WHERE/What could not because of why...
// Maybe have a isValidKey(Type) or <T> checkValidKey(T extends RuntimeException, String message) throws T;
// Maybe have a string with "%s, %s".. Maybe A consumer with the message because XYZ
// because it "xxxxxx"

//
/// **
// * Returns the type of qualifier this key have, or null if this key has no qualifier.
// *
// * @return the type of qualifier this key have, or null if this key has no qualifier
// */
// @Nullable
// public final Class<? extends Annotation> getQualifierType() {
// return qualifier == null ? null : qualifier.annotationType();
// }
// An easy way to create annotations with one value, or maybe put it on TypeLiteral
// withNamedAnnotations(type, String name, Object value)
// withNamedAnnotations(type, String name1, Object value1, String name2, Object value2)
// withNamedAnnotations(type, String name1, Object value1, String name2, Object value2, String name3, Object value3);
// public static <T> Key<T> withAnnotation(Type type, Class<? extends Annotation> cl, Object value) {
// withAnnotation(Integer.class, Named.class, "left");
// throw new UnsupportedOperationException();
// }
//// @Provides method cannot have void return type.
// if (descriptor().getReturnType() == void.class) {
// throw new IllegalArgumentException("@Provides method " + description + " cannot have void return type");
// }
//
//// TODO check not reserved return types
//
//// TODO check return type is not optional
//// Or maybe they can.
//// If a Provides wants to provide null to someone the return type of the method should be Optional<XXXXX>
//// Null indicates look in next injector...
//