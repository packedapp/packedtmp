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
package app.packed.bean.hooks.usage;

import app.packed.extension.Extension;
import app.packed.inject.Factory1;
import app.packed.inject.Factory2;
import app.packed.operation.dependency.DependencyProvider;

/**
 *
 */
public class OwnLogger {

    @DependencyProvider.Hook(extension = MyExt.class)
    class Logger {}

    static class MyExt extends Extension<MyExt> {

        @Override
        protected void hookOnBeanDependencyProvider(DependencyProvider provider) {
            String name = provider.beanInfo().beanClass().getCanonicalName();
            provider.provide(new Factory1<RuntimeLoggerManager, Logger>(l -> l.newLogger(name)) {});
            // eller
            provider.provide(new Factory2<String, RuntimeLoggerManager, Logger>((n, l) -> l.newLogger(n)) {}.bind(name));
        }
    }

    static class RuntimeLoggerManager {
        Logger newLogger(String name) {
            throw new UnsupportedOperationException();
        }
    }

}
