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
package app.packed.bean.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.base.Variable;
import app.packed.bean.hooks.sandbox.AnnotationReader;
import app.packed.bean.hooks.sandbox.CommonVarInfo;
import app.packed.bean.hooks.sandbox.VariableParser;
import app.packed.extension.Extension;
import app.packed.inject.Factory;

/**
 *
 */
// Provides objects for member injection (parameter, field)

// Informational om Variabele
//// Type part
//// Annotation part

// Mirroring?

// provide

// modeSetting: modeRaw...
// requireContext (<---- on BeanElement????)

@SuppressWarnings("exports")
public non-sealed interface BeanVarInjector extends BeanElement {

    AnnotationReader annotations();

    void provide(Factory<?> fac);

    void provide(MethodHandle methodHandle);

    /**
     * <p>
     * Vi tager Nullable med saa vi bruge raw.
     * <p>
     * Tror vi smider et eller andet hvis vi er normal og man angiver null. Kan kun bruges for raw
     * 
     * @param instance
     *            the instance to provide to the variable
     * 
     * @throws ClassCastException
     *             if the type of the instance does not match the type of the variable
     * @throws IllegalStateException
     *             if a provide method has already been called on this injector (I think it is fine to allow it to be
     *             overriden by itself). Or if the container has closed
     */
    void provideInstance(@Nullable Object obj);

    /**
     * <p>
     * For raw er det automatisk en fejl
     */
    void provideMissing();

    void requireContext(Class<?> contextType);

    Variable variable();

    default CommonVarInfo variableParse() {
        return variableParse(CommonVarInfo.DEFAULT);
    }

    <T> T variableParse(VariableParser<T> parser);

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RUNTIME)
    @Documented
    public @interface Hook {

        /** The extension this hook is a part of. Must be located in the same module as the annotated element. */
        Class<? extends Extension<?>> extension();

        // HttpRequestContext... requireAllContexts, requireAnyContexts
        Class<?>[] requiresContext() default {};
    }
    
    // Skal saettes statisk paa bootstrappe vil jeg mene??? IDK
    enum Availability {
        /** For example, ServiceRegistry. */
        ALWAYS_AVAILABLE, 
        
        /** For example, DatabaseX */
        KNOWN_AT_BUILDTIME,
        
        /** For example, Transaction (extracted from ScopeLocal) */
        KNOWN_AT_RUNTIME // Kan den misforstaas som at naar vi er initialiseret ved vi det?? Er det i virkeligheden unknown
    }
}
