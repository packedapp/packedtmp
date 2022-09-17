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
package app.packed.operation.bindings.sandbox;

import java.lang.annotation.Annotation;
import java.util.List;

import app.packed.base.Key;

/**
 *
 */
// Er stadig ikke sikker pa om vi skal override Dependency eller eksistere som en composite
// Hvis man vil extende Dependency vil jeg gaette paa man altid vil kende bindingkind paa forhaand
public interface Binding {

    BindingKind kind();

    /**
     * A binding that was created based on an `annotation
     */
    interface OnAnnotation extends Binding {

        Annotation annotation();

        default Class<? extends Annotation> annotationType() {
            return annotation().annotationType();
        }
    }

    interface OnComposite extends Binding {

        List<Binding2Mirror> dependencies();

        // Tror ikke laengere vi bliver resolved som en compond.
        // get(Req, Res) -> Har bare 2 parametere. (Maaske idk)
        //boolean isFuncionalInterface();

    }

    interface OnKey extends Binding {

        /** {@return the key the binding was created on}. */
        Key<?> key();
    }

    interface OnOther extends Binding {}

    interface OnType extends Binding {
        Class<?> type();
    }
}
