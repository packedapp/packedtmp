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

import app.packed.base.Nullable;

/**
 * The result of a validation.
 */
//https://github.com/making/yavi
// https://www.fpcomplete.com/blog/monads-gats-nightly-rust/
public final class Validation {

    /**
     * Common instance for {@code empty()}.
     */
    private static final Validation VALID = new Validation(null);

    @Nullable
    final PackedValidationFailure violations;

    Validation(PackedValidationFailure violations) {
        this.violations = violations;
    }

    // ServiceContract validate...
    public Validation and(Validation validation) {
        throw new UnsupportedOperationException();
    }

    public Validation and(Validation... validations) {
        throw new UnsupportedOperationException();
    }

    public void assertValid() {

    }
//    boolean isValid();
//
//    Set<?> violations();

    public boolean isInvalid() {
        return violations != null;
    }

    /**
     * If the validation was successful, performs the given action, otherwise does nothing.
     *
     * @param action
     *            the action to be performed, if the validation was successful
     */
    public void ifValid(Runnable action) {
        if (violations == null) {
            action.run();
        }
    }

    public boolean isValid() {
        return violations == null;
    }

    public static Validation valid() {
        return VALID;
    }
}
