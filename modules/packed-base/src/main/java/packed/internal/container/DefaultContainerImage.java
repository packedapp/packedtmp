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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import app.packed.container.AnyBundle;
import app.packed.container.ContainerImage;
import app.packed.container.Wirelet;

/**
 *
 */
public class DefaultContainerImage implements ContainerImage {

    final DefaultContainerConfiguration dcc;

    DefaultContainerImage(DefaultContainerConfiguration dcc) {
        this.dcc = requireNonNull(dcc);
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends AnyBundle> source() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerImage with(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
