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
package app.packed.context;

import java.util.List;

import app.packed.application.NamespacePath;
import app.packed.service.Key;
import app.packed.service.Qualifier;

/**
 *
 */
// Resolves dublicates
public interface ContextKeyResolver {

    // Path = Operation, Bean, Container,...

    // Er det ikke altid operation? For det er vel kun hvis de bliver brugt??
    Class<? extends Context<?>> resolve(NamespacePath path, Key<?> key, List<Class<? extends Context<?>>> contexts);

    // Alternativt

    // Ville vaere fedt hvis man kunne sige no context... Saa ville man faa services
    @interface FromContext {
        Class<? extends Context<?>>[] value();
    }

    public void foo(@FromContext({}) String asContainerService, @FromContext({}) String asInitialization);
    
    // endnu nyere... Eller contexts skal explicit skrives ud
    // Er lidt traels... men hmm
    @Qualifier
    @interface FromContextX {
        Class<? extends Context<?>> value();
    }
}
