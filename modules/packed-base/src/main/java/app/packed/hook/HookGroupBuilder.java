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
package app.packed.hook;

import java.lang.invoke.MethodHandles.Lookup;

/**
 *
 * Must have at least one method annotated with {@link OnHook}.
 */

// TODO Skal vi have en abstract klasse med hjaelpe metoder istedet for?????
// CustomHookBuilder
public interface HookGroupBuilder<T extends Hook> {

    /**
     * Invoked by the runtime when all relevant methods annotated with {@link OnHook} has been called.
     * 
     * @return the hook group that was built.
     */
    T build();

    static <T extends Hook> T generate(Class<T> groupType, Class<?> target) {
        throw new UnsupportedOperationException();
    }

    /**
     * A class that. Mainly used for testing. Instead of needing to spin up a container.
     * 
     * @param <T>
     *            the type of hook group to generate
     * @param groupType
     *            the builder type to instantiate
     * @param caller
     *            a lookup object that has permissions to instantiate the builder and access its and the targets hookable
     *            methods.
     * @param target
     *            the target class that should be processed be specified
     * @return a new group
     */
    static <T extends Hook> T generate(Class<T> groupType, Lookup caller, Class<?> target) {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        Ob o = generate(Ob.class, String.class);
        System.out.println(o);

        // Function<Class<?>, Ob> ff = s -> generate(Ob.class, s);
    }
}

class Ob implements Hook {

    static class Builder implements HookGroupBuilder<Ob> {

        /** {@inheritDoc} */
        @Override
        public Ob build() {
            return new Ob();
        }

        @OnHook
        public void onHook(AnnotatedFieldHook<Deprecated> f) {}
    }
}

/// **
// * A class that. Mainly used for testing. Instead of needing to spin up a container.
// *
// * @param <G>
// * the type of hook group to generate
// * @param caller
// * a lookup object that has permissions to instantiate the builder and access its and the targets hookable
// * methods.
// * @param groupType
// * the builder type to instantiate
// * @param target
// * the target class that should be processed
// * @param targetAccessor
// * if the specified caller does not have open rights to the specified target. Additional lookup objects can
// * be specified
// * @return a new group
// */
//// Will not cache generate value. You can put a ClassValue in front
//// Tror vi dropper denne...
// static <G, B extends HookGroupBuilder<G>> Function<Class<?>, G> generate(Lookup caller, Class<B> builderType) {
// throw new UnsupportedOperationException();
// }

/// **
// * Is typically used during tests to verify
// *
// * @param <A>
// * @param <T>
// * @param t
// * @param target
// * @return a new aggregate
// */
// static <A extends HookGroupBuilder<T>, T> T generate(Lookup caller, Class<A> t, Lookup lookupTarget, Class<?> target)
/// {
// // Ideen er at folk kan proeve at lave deres egen aggregate...
// throw new UnsupportedOperationException();
// }
//
// static <A extends HookGroupBuilder<T>, T> BiFunction<Lookup, Class<?>, T> generateGenerator(Lookup caller, Class<A>
/// t) {
// // Virker ikke med cross-module class hierachies. Maaske skal vi bare droppe det til andet end tests....
// throw new UnsupportedOperationException();
// }