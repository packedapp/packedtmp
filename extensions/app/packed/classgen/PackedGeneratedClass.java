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
package app.packed.classgen;

import java.lang.ref.WeakReference;

import app.packed.component.Realm;

/**
 *
 */
class PackedGeneratedClass implements GeneratedClassMirror {

    final WeakReference<Class<?>> generatedClass;

    final Realm realm;

    PackedGeneratedClass(Realm realm, Class<?> genereatedClass) {
        this.realm = realm;
        this.generatedClass = new WeakReference<Class<?>>(genereatedClass);
    }

    /** {@inheritDoc} */
    @Override
    public Realm generatedBy() {
        return realm;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> generatedClass() {
        Class<?> c = generatedClass.get();
        return c == null ? Object.class : c;
    }
    
    public String toString() {
        return "Generated class " + generatedClass.get() + " for " + realm;
    }
}
