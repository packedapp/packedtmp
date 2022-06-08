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
package packed.internal.bean;

import app.packed.base.Key;

/**
 *
 */
// Altsaa Vi har jo lidt brug for den i forbindelse med extension beans taenker jeg...
// Der er ingen grund til at scanne dem flere gange...
public final class BeanClassModel {

    /** The class of the model. */
    public final Class<?> clazz;

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    private String simpleName;

    BeanClassModel(Class<?> clazz) {
        this.clazz = clazz;
    }
    
    public Key<?> defaultKey() {
        // What if instance has Qualifier???
        return Key.of(clazz);
    }
    
    /**
     * Returns the default prefix for the source.
     * 
     * @return the default prefix for the source
     */
    public String simpleName() {
        String s = simpleName;
        if (s == null) {
            s = simpleName = clazz.getSimpleName();
            // TODO Some things to do for anonymous classes
            if (s.length() == 0) {
                s = simpleName = "Anon";
            }
        }
        return s;
    }

}
