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
package app.packed.service.advanced;

import java.util.List;
import java.util.Map;
import java.util.Set;

import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.context.Context;
import app.packed.operation.OperationSite;

/**
 *
 */
// Could be on operation mirror. But may be more simple here
// I think a mirror is probably better...
// Tror ideen er at samle alt omkring injection of hvorfor ting kommer saa de goer
// Kan altid definere en Service som Optional. Og saa bruge den her til at forstaa hvorfor


// Vi snakkede om Tracer som er en advanced optional some kan bruges. Fungere bare ikke super godt paa hooks

public interface InjectionSite {

    Map<Integer, ServiceProviderKind> services();

    List<Variable> variables();

    OperationSite operation();

    // The services t
    Set<Key<?>> operationKeys();

    Map<Key<?>, Available> availableKeys();

    sealed interface Available {
        Key<?> key();

        record AtOperation(Key<?> key) implements Available {}

        record AtBean(Key<?> key) implements Available {}

        record AtContext(Key<?> key, Class<? extends Context<?>> context) implements Available {}

        record AtNamespace(Key<?> key, String namespace) implements Available {}
    }
}
