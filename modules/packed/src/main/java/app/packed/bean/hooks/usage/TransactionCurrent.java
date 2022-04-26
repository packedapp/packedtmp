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

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.inject.Factory0;
import app.packed.operation.dependency.DependencyProvider;

/**
 *
 */
public class TransactionCurrent {

    @DependencyProvider.Hook(extension = MyExt.class)
    static class Transaction {

        @Nullable
        static Transaction current() {
            throw new UnsupportedOperationException();
        }
    }
    
    static class MyExt extends Extension<MyExt> {

        // Ved ikke om @Nullable er for subtle
        @Override
        protected void hookOnBeanDependencyProvider(DependencyProvider injector) {
            injector.provide(new Factory0<@Nullable Transaction>(() -> Transaction.current()) {});
        }
    }
}
