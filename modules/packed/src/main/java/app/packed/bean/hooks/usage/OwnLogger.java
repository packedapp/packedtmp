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

import app.packed.bean.hooks.BeanVarInjector;
import app.packed.extension.Extension;
import app.packed.inject.Factory1;

/**
 *
 */
public class OwnLogger {

    @BeanVarInjector.Hook(extension = MyExt.class)
    class Logger {}

    static class MyExt extends Extension<MyExt> {

        @Override
        protected void hookOnBeanVarInjector(BeanVarInjector injector) {
            String name = injector.beanInfo().beanClass().getCanonicalName();
            injector.provide(new Factory1<RuntimeLoggerManager, Logger>(l -> l.newLogger(name)) {});
        }
    }

    static class RuntimeLoggerManager {
        Logger newLogger(String name) {
            throw new UnsupportedOperationException();
        }
    }

}
