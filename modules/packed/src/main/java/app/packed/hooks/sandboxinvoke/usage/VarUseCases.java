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
package app.packed.hooks.sandboxinvoke.usage;

import java.lang.System.Logger;
import java.lang.System.LoggerFinder;
import java.time.LocalDateTime;

import app.packed.base.Nullable;
import app.packed.hooks.sandboxinvoke.InjectableVariableHook;
import app.packed.hooks.sandboxinvoke.InjectableVariableHookBuilder;
import app.packed.inject.Factory0;
import app.packed.inject.Factory1;

/**
 *
 */
public class VarUseCases {

    class NowProvide extends InjectableVariableHookBuilder {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            injector().inject(new Factory0<>(() -> LocalDateTime.now()) {});
        }
    }

    class TransactionCurrent extends InjectableVariableHookBuilder {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            // injector().nullIsMissing();
            injector().inject(new Factory0<@Nullable Transaction>(() -> Transaction.current()) {});
            // injector().injectFromStaticMethod(Transaction.class, "current"); // Factory er bare lettere til at starte med..
        }

        static class Transaction {

            @Nullable
            static Transaction current() {
                throw new UnsupportedOperationException();
            }
        }
    }

    class LoggerOwnLogger extends InjectableVariableHookBuilder {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            String name = beanInfo().beanClass().getCanonicalName();
            injector().inject(new Factory1<RuntimeLoggerManager, Logger>(l -> l.newLogger(name)) {});
        }

        @InjectableVariableHook(processor = LoggerOwnLogger.class)
        class Logger {}

        class RuntimeLoggerManager {
            Logger newLogger(String name) {
                throw new UnsupportedOperationException();
            }
        }

        class Usage {
            void foo(Logger logger) {}
        }
    }

    class LoggerSystemLogger extends InjectableVariableHookBuilder {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            String name = beanInfo().beanClass().getCanonicalName();
            Module module = beanInfo().beanClass().getModule();
            injector().inject(new Factory0<Logger>(() -> LoggerFinder.getLoggerFinder().getLogger(name, module)) {});
        }

        class ConfigureLoggerManager {
            // We need to add to a BeanHook class and add it

            // mapDependency(Logger.class, LoggerSystemLogger.class);
            // mapDependency("java.lang.System.Logger", LoggerSystemLogger.class);
            // mapDependency("java.lang.System.Logger", "xxx.LoggerSystemLogger"); // No need to load if it not used...
            // mapMethodAnnotation("foo.class.HttpGet", "xxx.LoggerSystemLogger"); // No need to load if it not used...
        }

        class Usage {
            void foo(Logger logger) {}
        }
    }

    class PlusNumbers extends InjectableVariableHookBuilder {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            Plus p = annotations().readRequired(Plus.class);
            injector().injectConstant(p.arg1() + p.arg2());
        }

        @InjectableVariableHook(processor = PlusNumbers.class)
        @interface Plus {
            int arg1();

            int arg2();
        }

        class Usage {
            void foo(@Plus(arg1 = 123, arg2 = 4545) int valc) {}
        }
    }

    class SystemPropety extends InjectableVariableHookBuilder {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            ReadSystemProperty p = annotations().readRequired(ReadSystemProperty.class);
            String valToRead = p.value();

            // injector().autoConvert(Option.NullIsMissing);
            injector().inject(new Factory0<@Nullable String>(() -> System.getProperty(valToRead)) {});
        }

        @InjectableVariableHook(processor = SystemPropety.class)
        @interface ReadSystemProperty {
            String value();
        }

        class Usage {
            void foo(@ReadSystemProperty("LineLenght") int length) {}

        }
    }
}
