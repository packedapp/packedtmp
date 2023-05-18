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
package tck.context;

import java.lang.invoke.MethodHandles;

import app.packed.context.Context;
import app.packed.extension.BeanHook.BindingTypeHook;
import internal.app.packed.context.publish.ContextTemplate;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationTemplate;
import tck.AbstractBootstrapedAppTest;
import tck.HookExtension;

/** Various context class and their templates that can be used where applicable. */
public class ContextsHelpers {

    @BindingTypeHook(extension = HookExtension.class)
    public record NoImplContext(int i) implements Context<HookExtension> {
        /** A template. */
        public static final ContextTemplate CT = ContextTemplate.of(MethodHandles.lookup(), NoImplContext.class, NoImplContext.class);

        /** A simple operation with the context, that ignores return values. */
        public static final OperationTemplate OT = OperationTemplate.defaults().withContext(NoImplContext.CT).returnIgnore();

        /** A simple operation with the context, that ignores return values. */
        public static final OperationTemplate OTINT = OperationTemplate.defaults().withContext(NoImplContext.CT).returnType(int.class);
    }

    public static void bindSimple(AbstractBootstrapedAppTest<?> t) {
        t.hooks().onVariableType((cl, v) -> {
            assert (cl == NoImplContext.class);
            v.bindContextValue(NoImplContext.class);
        });

        t.hooks().onAnnotatedMethod((l, b) -> {
            OperationHandle h = b.newOperation(ContextsHelpers.NoImplContext.OTINT);
            t.add(h);
        });

    }
}
