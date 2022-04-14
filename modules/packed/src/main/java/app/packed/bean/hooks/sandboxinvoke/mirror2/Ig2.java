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
package app.packed.bean.hooks.sandboxinvoke.mirror2;

import java.lang.annotation.Annotation;
import java.util.List;

import app.packed.base.Key;
import app.packed.bean.BeanMirror;

/**
 *
 */
// Ligesom vi har hidden beans, har vi hidden operations!!!!!!!!
public interface Ig2 {

    List<Node> nodes();

    sealed interface Node {
        
        BeanMirror bean();

        List<Node> dependencies();
    }

    non-sealed interface ServiceNode extends Node {
        Key<?> key();
    }

    non-sealed interface PrimeAnnoNode extends Node {
        Class<? extends Annotation> annotationType();
    }
}
