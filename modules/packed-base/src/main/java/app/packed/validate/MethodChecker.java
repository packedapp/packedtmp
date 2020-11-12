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
package app.packed.validate;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.sidecar.MethodSidecar;

/**
 *
 */
// En checker goer det let at skrive validators.

// F.eks. ComponentSystemChecker.
final class MethodChecker {

    final Method method;

    final ContraintCheckContext context = null;

    private MethodChecker(Method method) {
        this.method = requireNonNull(method);
    }

    public MethodChecker isStatic() {
        return modifierSet(Modifier.STATIC);
    }

    public MethodChecker modifierSet(int modifier) {
        return this;
    }

    static Validator<? super Method> validator(Consumer<? super MethodChecker> checker) {
        throw new UnsupportedOperationException();
    }

    static MethodChecker of(Method method) {
        return of(method, null /* default */ );
    }

    // Throw immediately? Collect errors
    static MethodChecker of(Method method, Object validationContext) {
        return new MethodChecker(method);
    }
}

/**
 * Various validators. People can write them them self. But easier to just call them here.
 * <p>
 * Alternative we could have a MethodValidator, wrapping a method and used like .validate().isInstance().isStatic();
 */
class ZandboxValidations {

    static abstract class ValidatingMethodSidecar extends MethodSidecar {
        public final MethodChecker check() {
            throw new UnsupportedOperationException();
        };
        // check().isStatic();
    }

    // Must be either an error or runtime exception
    // I don't know what the default exception we throw is
    // also validate vs check....
    // Maybe we can just throw ValidationException
    public final void setCheckerException(Function<String, Throwable> f) {}

    // rename to validate instead of check...
    public final void checkNoReturnValue() {}

    public final void checkNoParameters() {}

    public final void checkIsInstance() {}

    public final void checkIsStatic() {
        checkModifier(Modifier.STATIC);
    }

    // Checks that the method does not have any checked exceptions
    public final void checkNoCheckedExceptions() {}

    /**
     * @param modifier
     *            the modifier to check that it is set
     * @see Modifier
     */
    public final void checkModifier(int modifier) {}

    public final void checkModifierNotSet(int modifier) {}

}