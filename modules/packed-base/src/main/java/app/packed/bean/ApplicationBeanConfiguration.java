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
package app.packed.bean;

import java.util.concurrent.Callable;

/**
 * An application bean that can serve as basis for actual component configuration types.
 * <p>
 * Component configuration classes do not need to extend this class.
 */
public non-sealed class ApplicationBeanConfiguration<T> extends BeanConfiguration<T> {

    public ApplicationBeanConfiguration() {}

    public <X extends Runnable & Callable<String>> X foo() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final BeanKind kind() {
        return BeanKind.APPLICATION;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }
}
