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

import app.packed.bean.operation.OperationMirror;

/**
 *
 */

// Vi har en per applikation
// En graph er altid resolved paa en gang

// Der kan vaere grafer i grafer?

// Der kan vaere regler omkring cykler
public interface InjectionGraphMirror {

    // Nodes
    // Do we include operationer der ikke har dependencies eller provider noget???

    List<OperationMirror> operations();

    // Edges
    List<Dependency> dependencies();

    List<Dependency> findAnnotated(Class<? extends Annotation> annoType);
}

class Usage {

    public static void main(InjectionGraphMirror m) {

//        for (var v : m.findAnnotated(SystemProp.class)) {
//            // v.
//        }

    }

    @interface SystemProp {

    }
}
// ApplicationInjectionGraphMirror?