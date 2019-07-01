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
package packed.internal.inject2;

import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.AnnotatedMethodHook;
import app.packed.container.ContainerExtensionHookProcessor;
import app.packed.hook.OnHook;
import app.packed.inject.Provide2;
import packed.internal.componentcache.Injector2Extension;

/**
 *
 */
// No Constructor... because they should be stateless.

// Maybe have an abstract class with stuff like checkNotStatic()....

// Saa ideen, er at Packed instantiere en ny instance af NewBuilder hver gang, den faar en klasse.
// Som har annoteringer der har en @ActiveExtension(NewBuilder.class) paa sig.
// Derefter kalder den alle relevante @OnHook metoder....
// Hvorefter der bliver lavet en BiConsumer som bliver invokeret hver gang gang vi stoeder paa
// komponent typen...

public final class ProvideHookProcessor extends ContainerExtensionHookProcessor<Injector2Extension> {

    @OnHook
    public void onProvidedMethod(AnnotatedMethodHook<Provide2> h) {
        // If Static
        // h.create()
    }

    /** {@inheritDoc} */
    @Override
    public BiConsumer<ComponentConfiguration, Injector2Extension> onBuild() {
        throw new UnsupportedOperationException();
        // return (cc, e) -> e.onInstall(this, cc);
    }
}
// Or Just methods that take AnnotatedMethodHook<Provide2>
// No need to annotate them with @OnHook, Nah its nice isn't....

// Implementati

// Look for @OnHook methods on the class
// Compile MethodHandles..

/// Its fairly clear for extension method, extension fields