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

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import app.packed.service.Key;


// ContainerLaunchContext

// 2 muligheder context services...
// or @FromChild


/**
 *
 */
public interface ContextTemplate {

    /** {@return the context this template is a part of.} */
    Class<? extends Context<?>> contextClass();

    /** {@return the extension this template is a part of.} */
    Class<? extends Extension<?>> extensionClass();

    /** {@return the type of arguments that must be provided.} */
    List<Class<?>> invocationArguments(); // Not a method type because no return type

    Set<Key<?>> keys();

    ContextTemplate withArgument(Class<?> argument);

    default ContextTemplate withServiceFromArgument(Class<?> key, int index) {
        return withServiceFromArgument(Key.of(key), index);
    }

    ContextTemplate withDynamicServiceResolver(Function<Key<?>, ?> f); 
    
    ContextTemplate withServiceFromArgument(Key<?> key, int index);

    static ContextTemplate of(MethodHandles.Lookup lookup, Class<? extends Context<?>> contextClass) {
        throw new UnsupportedOperationException();
    }
}

class Usage {

    public static void main(String[] args) {
        ContextTemplate t = ContextTemplate.of(MethodHandles.lookup(), ExtensionContext.class);
        t.withArgument(ScheContext.class).withServiceFromArgument(SchedulingContext.class, 0);
    }

    static class ScheContext implements SchedulingContext {

    }

    interface SchedulingContext {

    }
}