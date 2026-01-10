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
package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import internal.app.packed.bean.sidebean.SidebeanHandle;

/**
 * A mirror of a side bean.
 */
public final class SidehandleBeanMirror extends BeanMirror {

    /** The handle of the sidebean. */
    final SidebeanHandle<?> handle;

    public SidehandleBeanMirror(BeanHandle<?> handle) {
        this.handle = (SidebeanHandle<?>) requireNonNull(handle);
        super(handle);
    }

    /** {@return a stream of all places where this sidebean has been attached} */
    public Stream<SidehandleMirror> attachments() {
     //   handle.attachments().
        throw new UnsupportedOperationException();
    }

    public SidehandleTargetKind targetKind() {
        throw new UnsupportedOperationException();
    }
}
