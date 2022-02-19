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
package app.packed.hooks3;

import java.util.function.Supplier;

import app.packed.bean.mirror.BeanOperationMirror;
import app.packed.extension.ExtensionConfiguration;

/**
 *
 */
public abstract class FieldHookBuilder {

    protected abstract void build();


// ---------------------    

    public final <T extends BeanOperationMirror> void addOperationMirror(Class<T> mirrorType, Supplier<T> supplier) {

    }

    public FieldHookBuilder newOperation(ExtensionConfiguration ec) {
        // fx hvis man vil provide den som en service og et eller andet samtidig
        // BeanSupport.
        /// Naah ikke en metode her...
        // Vi skal bruge extensionen der goer det...
        
        // BeanField.
        throw new UnsupportedOperationException();
    }

    public final VarInjector injector() {
        // Er ikke sikker paa vi supportere denne
        throw new UnsupportedOperationException();
    }

    public final MetaAnnotationReader metaAnnotations() {
        throw new UnsupportedOperationException();
    }

    public final void requireContext(Class<?> contextType) {
        // fx SchedulingContext ect... Don't know if we need it
    }
}
//
//public final InvokerConfiguration computeInvoker() {
//    throw new UnsupportedOperationException();
//}
//
//public final VarHandle computeVarHandle() {
//    throw new UnsupportedOperationException();
//}
//
//public final Field field() {
//    // Can add shortscuts when we are done
//    throw new UnsupportedOperationException();
//}
//
//public final InvokerConfiguration getterInvoker() {
//    throw new UnsupportedOperationException();
//}
//
//public final MethodHandle getterMethodHandle() {
//    throw new UnsupportedOperationException();
//}
//
//public final InvokerConfiguration setterInvoker() {
//    throw new UnsupportedOperationException();
//}
//
//public final MethodHandle setterMethodHandle() {
//    throw new UnsupportedOperationException();
//}
//
//public final Variable variable() {
//    throw new UnsupportedOperationException();
//}