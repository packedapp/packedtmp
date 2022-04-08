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
package app.packed.hooks.sandboxinvoke;

import app.packed.base.Variable;
import app.packed.bean.hooks.sandbox.BeanInfo;

/**
 *
 */
// Builder giver ikke meget 
public abstract class InjectableVariableHookBuilder {

    protected abstract void build();

    public final BeanInfo beanInfo() {
        throw new UnsupportedOperationException();
    }
    
    public final VarInjector injector() {
        throw new UnsupportedOperationException();
    }

    public final MetaAnnotationReader annotations() {
        throw new UnsupportedOperationException();
    }

    public final void requireContext(Class<?> contextType) {
        // fx SchedulingContext ect... Don't know if we need it
    }

    public final Variable variable() {
        throw new UnsupportedOperationException();
    }

    public final CommonVarInfo variableParse() {
        return variableParse(CommonVarInfo.DEFAULT);
    }

    public final <T> T variableParse(VariableParser<T> parser) {
        throw new UnsupportedOperationException();
    }
}
