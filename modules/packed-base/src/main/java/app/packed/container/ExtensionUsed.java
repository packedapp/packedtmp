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
package app.packed.container;

import app.packed.container.ExtensionUsed.Mode;
import app.packed.service.ServiceExtension;

/**
 *
 */
public @interface ExtensionUsed {

    Mode value() default Mode.THIS;

    enum Mode {
        THIS, CHILD, FIRST_ANCESTOR;
    }
}

interface ContainerDescriptor {}

/**
 * A container relation desc
 */
interface ContainerRelationDescriptor {
    int distance();

    ContainerDescriptor from();

    ContainerDescriptor to();
}

/// Okay hvad maa jeg faa at vide??????
/// Er det maaske mere en Path?????
interface PathDescriptor<T> extends Iterable<T> {

    int distance(); // the

    T from();

    T to();
}

class FooBar {

    @ExtensionUsed
    public void foo(ServiceExtension se) {}

    @ExtensionUsed(Mode.CHILD)
    public void foox(ServiceExtension se) {}

    @ExtensionUsed(Mode.FIRST_ANCESTOR)
    public void foof(ServiceExtension se) {}
}