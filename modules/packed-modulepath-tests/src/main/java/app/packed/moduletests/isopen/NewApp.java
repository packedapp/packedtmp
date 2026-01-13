/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.moduletests.isopen;

import static app.packed.component.SidehandleBinding.Kind.FROM_CONTEXT;

import app.packed.application.BootstrapApp;
import app.packed.bean.Bean;
import app.packed.lifecycle.LifecycleKind;
import app.packed.component.SidehandleBinding;
import app.packed.component.SidehandleContext;
import app.packed.lifecycle.runtime.ManagedLifecycle;

/**
 *
 */
public class NewApp {
   static final class PackedApp {

        /** The bootstrap app for this application. */
        // Hmm, read of constructor, think we need module expose to packed, should probably be in the docs somewhere
        public static final BootstrapApp<PackedApp> BOOTSTRAP_APP = BootstrapApp.of(LifecycleKind.MANAGED, Bean.of(PackedApp.class));

        PackedApp(@SidehandleBinding(FROM_CONTEXT) ManagedLifecycle lc, SidehandleContext context) {
        }
    }
    public static void main(String[] args) {
        BootstrapApp.of(LifecycleKind.MANAGED, Bean.of(PackedApp.class));
    }
}
