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
package app.packed.component.sourced;

import app.packed.base.Key;
import app.packed.component.Bundle;
import app.packed.component.SourcedComponentConfiguration;
import app.packed.container.ContainerBundle;
import app.packed.container.Wirelet;

class MBundle extends MyBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        InstanceComponentConfiguration<String> x = link(InstanceComponentConfiguration.driver(), "hejhej");
        x = x.as(new Key<CharSequence>() {});
        link(new MBundle());
    }
}

/**
 *
 */
public abstract class MyBundle extends ContainerBundle {

    public void link(Bundle<?> bundle, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public <T, X extends SourcedComponentConfiguration<T>> X link(SourcedComponentDriver<T, X> driver, T instance, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
