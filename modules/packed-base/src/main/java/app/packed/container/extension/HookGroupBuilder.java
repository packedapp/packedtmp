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
package app.packed.container.extension;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

/**
 *
 */
// Take all methods that starts with "on"

// TODO Skal vi have en abstract klasse med hjaelpe metoder istedet for?????
/**
 *
 * Must have at least one method annotated with {@link OnHook}.
 */
public interface HookGroupBuilder<G> {

    /** Builds and returns a hook group. */
    G build();

    /**
     * A class that. Mainly used for testing. Instead of needing to spin up a container.
     * 
     * @param <G>
     *            the type of hook group to generate
     * @param <B>
     *            the type of builder
     * @param caller
     *            a lookup object that has permissions to instantiate the builder
     * @param builderType
     *            the builder type to instantiate
     * @param target
     *            the target class that should be processed
     * @param targetAccessor
     *            if the specified caller does not have open rights to the specified target. Additional lookup objects can
     *            be specified
     * @return a new group
     */
    static <G, B extends HookGroupBuilder<G>> G generate(Lookup caller, Class<B> builderType, Class<?> target, Lookup... targetAccessor) {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        Ob o = generate(MethodHandles.lookup(), MyBuilder.class, String.class);
        System.out.println(o);
    }
}

class MyBuilder implements HookGroupBuilder<Ob> {

    /** {@inheritDoc} */
    @Override
    public Ob build() {
        // TODO Auto-generated method stub
        return null;
    }

}

class Ob {

}
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