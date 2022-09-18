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
package sandbox.operation.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;

import app.packed.base.Key;

/**
 *
 */
public interface OperationBuilder1 {

    int addArgument(Class<?> type);

    int addComputed(MethodHandle methodHandle, int... dependencies);
    
    void provide(int index, Key<?> key);
    void provide(int index, Class<?> key);
    
}
interface Sandbox {

    // --------------- ///
    // Looks up in the beans context
    int addBeanLookup(Class<?> key);

    int addBeanLookup(Key<?> key); // Ideen er at vi kan slaa services op vi kan bruge i andre method handles...

    int addTemplatedArgument(Class<?> type); // uses @Provide

    // @ExtractHeader("msg-id")
    // Skal vaere en Runnable. kan maaske ogsaa vaere noget paa BeanOperation

    void onAnnotation(Class<? extends Annotation> a, Runnable runnable);

    // inputType
    // spawnNewThread    
}