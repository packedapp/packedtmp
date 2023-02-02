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
package app.packed.service.sandbox;

import java.lang.annotation.Annotation;

import app.packed.bindings.Key;

/**
 *
 */
public class ServiceRequirementsTransformer {

    /**
     * @param <T>
     * @param key
     * @param instance
     * @return
     */
    public <T> Object provideInstance(Key<T> key, T instance) {
        return null;
    }

    /**
     * @param a
     * @return
     */
    public Object rekeyAllAddQualifier(Annotation a) {
        return null;
    }

}
