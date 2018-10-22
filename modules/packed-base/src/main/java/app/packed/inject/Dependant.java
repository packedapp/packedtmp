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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * An object with 0 or more dependencies.
 */
public interface Dependant {

    /**
     * Checks that this instance has no unfulfilled, or throws an {@link IllegalStateException} if there are unbound
     * dependencies.
     *
     * @return this dependent
     * @throws IllegalStateException
     *             if there are unbound dependencies
     */
    default Dependant checkNoDependencies() {
        if (hasDependencies()) {
            List<Dependency> unbound = getDependencies();
            throw new IllegalStateException("Unbound dependecies + " + unbound);
        }
        return this;
    }

    /**
     * Performs the given action for each dependency.
     * <p>
     * Any exception thrown by the specified action is relayed to the caller.
     *
     * @param action
     *            the action to perform
     * @throws NullPointerException
     *             if the specified action is null
     */
    default void forEachDependency(Consumer<? super Dependency> action) {
        requireNonNull(action, "action is null");
        getDependencies().forEach(action);
    }

    /**
     * Returns an immutable list of the unfulfilled dependencies of this instance. TODO mention something about you can
     * change it in a for loop with it being effected.
     *
     * @return an immutable snapshot of the unfulfilled dependencies of this instance
     */
    List<Dependency> getDependencies();

    /**
     * Returns the number of unfulfilled dependencies of this instance.
     *
     * @return the number of unfulfilled dependencies of this instance
     */
    default int getNumberOfDependencies() {
        return getDependencies().size();
    }

    /**
     * Returns the whether or not this instance has any unfulfilled dependencies.
     *
     * @return the whether or not this instance has any unfulfilled dependencies
     */
    default boolean hasDependencies() {
        return getNumberOfDependencies() > 0;
    }

}
//
/// **
// * A return value of {@code null} does not <i>necessarily</i> indicate that there does not exist a binding for the
// * specified index; it's also possible that the null has been bound to the specified index. The {@link #isBound(int)
// * isBound} operation may be used to distinguish these two cases.
// *
// * @param index
// * the index for which to return the binding
// * @return
// */
// default Object getBinding(int index) {
// Objects.checkIndex(index, getNumberOfDependencies());
// return null;
// }

/// **
// * Returns whether or not this object has any dependencies.
// *
// * @return whether or not this object has any dependencies
// */
// default boolean hasDependencies() {
// return getNumberOfDependencies() != 0;
// }

//
// static Dependable of(Dependency... dependencies) {
// throw new UnsupportedOperationException();
// }
//
// static Dependable of(Member member, Dependency... dependencies) {
// throw new UnsupportedOperationException();
// }
//
//// can be both Constructor, Method, Field (so not execuable)
// default Member getMember() {
// return null;
// }
