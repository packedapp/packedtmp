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

import app.packed.bean.hooks.BeanMethod;

/**
 *
 */
// En checker goer det let at skrive validators.

// F.eks. ComponentSystemChecker.
final class MethodChecker {

    final ContraintCheckContext context = null;

    /** The method that is being checked */
    private final Method method;

    private MethodChecker(Method method) {
        this.method = requireNonNull(method);
    }

    public MethodChecker isModifierSet(int modifier) {
        if ((method.getModifiers() & modifier) == 0) {

        }
        return this;
    }

    /**
     * Checks that the method is static.
     * 
     * @return this checker
     * 
     * @see Modifier#isStatic(int)
     */
    public MethodChecker isStatic() {
        return isModifierSet(Modifier.STATIC);
    }

    static MethodChecker of(Method method) {
        return of(method, null /* default */ );
    }

    // Throw immediately? Collect errors
    static MethodChecker of(Method method, Object validationContext) {
        return new MethodChecker(method);
    }

    static Validator<? super Method> validator(Consumer<? super MethodChecker> checker) {
        throw new UnsupportedOperationException();
    }
}

/**
 * Various validators. People can write them them self. But easier to just call them here.
 * <p>
 * Alternative we could have a MethodValidator, wrapping a method and used like .validate().isInstance().isStatic();
 */
class ZandboxValidations {

    public final void checkIsInstance() {}

    public final void checkIsStatic() {
        checkModifier(Modifier.STATIC);
    }

    /**
     * @param modifier
     *            the modifier to check that it is set
     * @see Modifier
     */
    public final void checkModifier(int modifier) {}

    public final void checkModifierNotSet(int modifier) {}

    // Checks that the method does not have any checked exceptions
    public final void checkNoCheckedExceptions() {}

    public final void checkNoParameters() {}

    // rename to validate instead of check...
    public final void checkNoReturnValue() {}

    // Must be either an error or runtime exception
    // I don't know what the default exception we throw is
    // also validate vs check....
    // Maybe we can just throw ValidationException
    public final void setCheckerException(Function<String, Throwable> f) {}

    static abstract class ValidatingMethodSidecar extends BeanMethod {
        public final MethodChecker check() {
            throw new UnsupportedOperationException();
        };
        // check().isStatic();
    }

}