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
package app.packed.service.dep2;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import app.packed.inject.Provider;
import app.packed.lang.Key;
import app.packed.service.Provide;
import app.packed.service.ServiceExtension;

/**
 * Must be accompanied by a {@link Provide} annotation
 * 
 * @see ServiceExtension#export(Key)
 */
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Export {
    Class<?>[] as() default {};// empty the key it is registered under..Tae

    // String description() default "${service.description}"; Hvordan siger vi keep existing
}

@Export(as = ListOfStrings.class)
class ListOfStrings implements Provider<Key<?>> {

    /** {@inheritDoc} */
    @Override
    public Key<?> provide() {
        return new Key<List<String>>() {};
    }
}

/// **
// * Indicates that the service should be exported from any bundle that it is registered in. This method can be used
// * to.... to avoid having to export it from the bundle.
// *
// * <p>
// * The service is always exported using the same key as it is registered under internally in a container. If you wish
/// to
// * export it out under another you key you can use {@link #exportAs()}. If you need to export the service out under a
// * key that uses a qualifier or a generic type. There is no way out of having to it manually
// *
// * to use another key, the service must be explicitly exported, for example, using Bundle#exportService().
// * <p>
// * The default value is {@code false}, indicating that the provided service is not exported.
// *
// * @return whether or not the provided service should be exported
// */
// boolean export() default false;
//
//// Maybe we do not want this...
//// Class<?> exportAs(); Only way to specify generic type or Qualifier is manually... Do we remove any qualifier?????
//// exportAs overrides Qualifiers og generic information. identical to calling as(MyInterface.class)
//// Essential a export(this).as(SomeInterface.class)
//
//// We do not main qualifiers when using this method...
//// Or maybe???
//// @Internal Injector = exportAs= Injector.class
//// Definitely not keep qualifier.
// Class<?> exportAs() default Object.class;