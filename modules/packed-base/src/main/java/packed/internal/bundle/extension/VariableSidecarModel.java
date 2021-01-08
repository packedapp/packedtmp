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
package packed.internal.bundle.extension;

import app.packed.base.invoke.Invoker;

/**
 *
 */
public class VariableSidecarModel {

    /** A configuration object we provide to MethodSidecar. */
    public final static class VariableSidecarConfiguration {

        boolean debug;

        Class<?> invoker;

        public void provideInvoker() {
            if (invoker != null) {
                throw new IllegalStateException("Cannot provide more than 1 " + Invoker.class.getSimpleName());
            }
            invoker = Object.class;
        }

        public void debug() {
            System.out.println("DEBUG DU ER SEJ");
            this.debug = true;
        }
    }
}
