/*
0 * Copyright (c) 2008 Kasper Nielsen.
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
package app.packed.operation.driver;

import java.util.function.BiConsumer;

import app.packed.base.TypeToken;
import app.packed.container.Extension;
import app.packed.inject.serviceexpose.ServiceExtension;
import app.packed.operation.OperationMirror;

/**
 *
 */
public class OperationDriver {

    String name;

    Class<? extends Extension<?>> extensionType = ServiceExtension.class;

    public static OperationDriver of(String name) {
        throw new UnsupportedOperationException();
    }

    public static OperationDriver of(String name, Class<? extends OperationMirror> mirrorType) {
        throw new UnsupportedOperationException();
    }

    public static OperationDriver of(Class<?> functionType, Class<? extends OperationMirror> mirrorType) {
        throw new UnsupportedOperationException();
    }

    public static OperationDriver of(TypeToken<?> functionType, Class<? extends OperationMirror> mirrorType) {
        throw new UnsupportedOperationException();
    }
}

class Usage {
    public static void main(String[] args) {
        // ExtensionLookup.createOperation();
        OperationDriver.of(new TypeToken<BiConsumer<Object, Object>>() {}, OperationMirror.class);
    }
}
