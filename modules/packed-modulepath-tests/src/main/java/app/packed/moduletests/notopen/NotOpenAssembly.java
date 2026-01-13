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
package app.packed.moduletests.notopen;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.application.App;
import app.packed.assembly.AssemblyConfiguration;
import app.packed.assembly.BaseAssembly;
import app.packed.container.Wirelets;

/**
 *
 */
public class NotOpenAssembly extends BaseAssembly {

    final Consumer<? super AssemblyConfiguration> consumer;

    public NotOpenAssembly(Consumer<? super AssemblyConfiguration> consumer) {
        this.consumer = requireNonNull(consumer);
    }

    /** {@inheritDoc} */
    @Override
    protected void build() {
        consumer.accept(assembly());
    }

    public static void build(Consumer<? super AssemblyConfiguration> consumer) {
        App.verify(new NotOpenAssembly(consumer), Wirelets.codegenAlways());
    }
}
