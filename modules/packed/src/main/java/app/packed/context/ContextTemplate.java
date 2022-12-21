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
package app.packed.context;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanHook.AnnotatedMethodHook;
import app.packed.bean.BeanHook.VariableTypeHook;
import app.packed.extension.BaseExtensionPoint.InvocationArgument;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionContext;
import app.packed.operation.Op1;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.service.Key;

// ContainerLaunchContext

// 2 muligheder context services...
// or @FromChild

/**
 *
 */
public interface ContextTemplate {

    /** {@return the context this template is a part of.} */
    Class<? extends Context<?>> contextClass();

    /** {@return the extension this template is a part of.} */
    Class<? extends Extension<?>> extensionClass();

    /** {@return the type of arguments that must be provided.} */
    List<Class<?>> invocationArguments(); // Not a method type because no return type

    Set<Key<?>> keys();

    ContextTemplate withArgument(Class<?> argument);

    default ContextTemplate withServiceFromArgument(Class<?> key, int index) {
        return withServiceFromArgument(Key.of(key), index);
    }

    ContextTemplate withDynamicServiceResolver(Function<Key<?>, ?> f);

    ContextTemplate withServiceFromArgument(Key<?> key, int index);

    static ContextTemplate of(MethodHandles.Lookup lookup, Class<? extends Context<?>> contextClass, Class<?>... invocationArguments) {
        throw new UnsupportedOperationException();
    }
}

class Usage {

    static class SchedulingBean {

    }

    static class ScheContext implements SchedulingContext {
        SchedulingHistory history;
    }

    @AnnotatedMethodHook(extension = MyExt.class, requiresContext = SchedulingContext.class, allowInvoke = true)
    @interface Cron {
        String value();
    }

    @VariableTypeHook(extension = MyExt.class, requiresContext = SchedulingContext.class)
    interface SchedulingContext extends Context<MyExt> {}

    @VariableTypeHook(extension = MyExt.class, requiresContext = SchedulingContext.class)
    interface SchedulingHistory {}

    static class MyExt extends Extension<MyExt> {
        static final ContextTemplate CT = ContextTemplate.of(MethodHandles.lookup(), ExtensionContext.class, ScheContext.class);

        static final OperationTemplate ot = OperationTemplate.defaults().withContext(CT);

        @Override
        protected BeanIntrospector newBeanIntrospector() {
            return new BeanIntrospector() {

                @SuppressWarnings("unused")
                @Override
                public void onMethod(OnMethod method) {
                    Cron c = method.annotations().readRequired(Cron.class);
                    OperationHandle oh = method.newOperation(ot);
                    ExtensionBeanConfiguration<SchedulingBean> bean = lifetimeRoot().base().installIfAbsent(SchedulingBean.class);

                    // bean, add scheduling + 
                    // Manytons dur ikke direkte
                    
                    // 
                    // bean.addSchedule
                    // parse expresion

                }

                @Override
                public void onVariableProvide(OnVariableProvide binding) {
                    if (binding.hookClass() == SchedulingContext.class) {
                        binding.bindToInvocationArgument(0, SchedulingContext.class);
                    } else if (binding.hookClass() == SchedulingHistory.class) {
                        binding.bindTo(new Op1<@InvocationArgument(context = SchedulingContext.class) ScheContext, SchedulingHistory>(c -> c.history) {});
                    } else {
                        super.onVariableProvide(binding);
                    }
                }
            };
        }

    }
}