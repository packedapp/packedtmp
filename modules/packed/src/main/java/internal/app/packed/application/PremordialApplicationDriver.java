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
package internal.app.packed.application;

import java.util.function.Supplier;

import app.packed.application.ApplicationMirror;
import app.packed.application.BootstrapApp;
import app.packed.container.Wirelet;
import internal.app.packed.lifetime.sandbox2.OldLifetimeKind;

/**
 *
 */
public final class PremordialApplicationDriver<A> extends ApplicationDriver<BootstrapApp<A>> {
    
    /** {@inheritDoc} */
    @Override
    public OldLifetimeKind lifetimeKind() {
        return OldLifetimeKind.UNMANAGED;
    }

    /** {@inheritDoc} */
    @Override
    public Supplier<? extends ApplicationMirror> mirrorSupplier() {
        return ApplicationMirror::new;
    }

    /** {@inheritDoc} */
    @Override
    public BootstrapApp<A> newInstance(ApplicationInitializationContext context) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wirelet() {
        return null;
    }
}
