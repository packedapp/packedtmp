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
package packed.internal.lifecycle2;

import static java.util.Objects.requireNonNull;

import app.packed.container.ExtensionSidecar;

/**
 *
 */
public class StateExpression {

    private final String expression;

    StateExpression(String expression) {
        this.expression = requireNonNull(expression);
    }

    public boolean isMatch(String state) {
        if (expression.equals("*")) {
            return true;
        }
        return expression.equals(expression);
    }

    public static String foo = ExtensionSidecar.ASSEMBLED + "|" + ExtensionSidecar.INSTANTIATING;

    public static StateExpression of(String expression) {
        return new StateExpression(expression);
    }
}
