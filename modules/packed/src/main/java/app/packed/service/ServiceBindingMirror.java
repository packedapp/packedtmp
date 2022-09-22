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
package app.packed.service;

import app.packed.base.Key;
import app.packed.container.ContainerMirror;
import app.packed.operation.BindingMirror;

/**
 *
 */
public class ServiceBindingMirror extends BindingMirror {

    public Key<?> key() {
        throw new UnsupportedOperationException();
    }
    
    public ContainerMirror container() {
        throw new UnsupportedOperationException();
    }
}
