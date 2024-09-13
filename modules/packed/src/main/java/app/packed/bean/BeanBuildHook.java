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

import app.packed.build.hook.BuildHook;

/**
 * A bean transformer
 */
// What do we do about beans owned by extensions
public non-sealed abstract class BeanBuildHook extends BuildHook {

    /**
     * Invoked immediately after a new bean is created. But before the configuration is returned to the user.
     *
     * @param configuration
     *            the configuration of the new bean
     */
    public void onNew(BeanConfiguration configuration) {}

    // sourceKind must be identical
    public Object replaceSource(BeanSourceKind sourceKind, Object source) {
        // return source == Foo.class ? TestFoo.class : source;
        return source;
    }

    // Argh besvaerligt at (at hvad?)
    public void onNew(@SuppressWarnings("exports") TransformerChain tc, BeanConfiguration bean) {}

    // before being returned to the user

    /**
     * Called before a bean is scanned
     *
     * @param bean
     *            a mutator for the bean
     */
    public void preScan(BeanClassMutator bean) {}

    /**
     * When an application has finished building this method is called to check.
     * <p>
     *
     * onSuccess??? verify?
     *
     * @param mirror
     *            a mirror of the bean to verify
     */
    // Do we take a ApplicationVerify thingy where we can register issues??? IDK
    // Then we can have fail, warn, info ect.
    public void verify(BeanMirror mirror) {}

    interface TransformerChain {
        // After any children in the transformer chain
        void doFinally(Runnable r);

        void endNow();
    }
}
