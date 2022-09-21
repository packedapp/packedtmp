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
package internal.app.packed.lifetime.pool;

/**
 *
 * @param <R>
 *            the type of value returned by invoke.
 */
// Taenker paa at droppe nullable...
// Men ved ikke hvordan det vil fungere end gang med generics specialization.
// Kan vi ende ud i en situation hvor vi har @Nullable paa en metode
// Og saa kan den lige pludselig ikke specialiseres???

public interface MethodAccessor<R> {

    /**
     * Invokes the underlying method handle discards any result.
     * 
     * @throws Throwable
     */
    void call() throws Throwable;

    default void call(Object... arguments) throws Throwable {
        throw new UnsupportedOperationException();
    }

    /**
     * Invokes the underlying method (or constructor)
     * 
     * @return the result
     * @throws Throwable
     *             anything thrown by the underlying method (or constructor) propagates unchanged through this call
     * 
     */
    R invoke() throws Throwable;
}
// We do not have methods that takes null into consideration
// You can do <@Nullable R>