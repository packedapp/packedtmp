/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package sandbox.app.packed.lifecycle;

/**
 *
 */
public @interface Start2 {

    /**
     * If there are multiple methods with {@link OnStart} and the same {@link #order()} on a single bean. This attribute can
     * be used to control the order in which they are invoked. With a higher bean order be invoked first for
     * {@link OperationDependencyOrder#BEFORE_DEPENDENCIES}. And lower bean order be invoked first for
     * {@link OperationDependencyOrder#AFTER_DEPENDENCIES}. If the bean order are identical the framework may invoke them in
     * any order.
     * <p>
     * NOTE: This attribute cannot be used to control ordering with regards to other beans.
     *
     * @return the bean order
     * @implNote current the framework will invoked them in the order returned by {@link Class#getMethods()}
     */
    byte beanInvocationOrder() default 0; // use -1 and, alternative before, after (String operation name)


    /**
     * Whether or not the bean should be marked as failed to start if the method throws
     *
     * @return
     */
    boolean stopOnFailure() default true; // Class<? extends Throwable> stopOnFailure() default Throwable.class

}
