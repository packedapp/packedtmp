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

/**
 *
 */
@FunctionalInterface
// https://github.com/making/yavi
public interface Validator<T> {

    default void assertValid(T input) {
        if (!isValid(input)) {
            throw new AssertionError("The specified input could not be validated, input = " + input);
        }
    }

    /**
     * Evaluates the input argument.
     * 
     * @param input
     *            the input to validate
     * @return {@code true} if the input argument is valid, otherwise {@code false}
     */
    boolean isValid(T input);

    default Validation tryValidate(T input) {
        throw new UnsupportedOperationException();
    }

    /**
     * Validates the specified input.
     *
     * @param input
     *            the input to validate
     * @throws ValidationException
     *             if the input could not be validated
     */
    default void validate(T input) {
        if (!isValid(input)) {
            throw new ValidationException("The specified input could not be validated, input = " + input);
        }
    }
}
