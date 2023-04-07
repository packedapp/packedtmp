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
package testutil.tools;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import app.packed.extension.BaseExtensionPoint.CodeGenerated;
import app.packed.extension.BeanElement.BeanField;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.BeanWrappedVariable;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.AnnotationList;
import app.packed.util.Key;
import internal.app.packed.util.CollectionUtil;
import sandbox.extension.operation.OperationHandle;

/** A special extension used by the {@link TckApp}. */
public class TckExtension extends Extension<TckExtension> {

    static ExtensionContext EC;

    // Tmp hack until channels work better
    static Map<Integer, MethodHandle> M = new HashMap<>();

    int counter;

    BiConsumer<? super Class<?>, ? super BeanWrappedVariable> hookOnVariableType;

    ServiceableBeanConfiguration<MyBean> install;
    private HashMap<Integer, OperationHandle> m = new HashMap<>();

    BiConsumer<? super AnnotationList, ? super BeanField> onAnnotatedField;

    BiConsumer<? super AnnotationList, ? super BeanMethod> onAnnotatedMethod;

    TckExtension() {}

    void addFieldCallback(BiConsumer<? super AnnotationList, ? super BeanField> consumer) {
        onAnnotatedField = consumer;
    }

    void addMethodCallback(BiConsumer<? super AnnotationList, ? super BeanMethod> consumer) {
        onAnnotatedMethod = consumer;
    }


    void hookOnVariableType(BiConsumer<? super Class<?>, ? super BeanWrappedVariable> hookOnVariableType) {
        this.hookOnVariableType = hookOnVariableType;
    }

    <T> void generate(Integer name, OperationHandle h) {
        if (m.putIfAbsent(name, h) != null) {
            throw new AssertionError();
        }
    }

    <T> void generate(OperationHandle h) {
        generate(++counter, h);
    }


    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @Override
            public void hookOnAnnotatedField(AnnotationList hooks, BeanField field) {
                if (onAnnotatedField != null) {
                    onAnnotatedField.accept(hooks, field);
                } else {
                    super.hookOnAnnotatedField(hooks, field);
                }
            }

            @Override
            public void hookOnAnnotatedMethod(AnnotationList hooks, BeanMethod method) {
                if (onAnnotatedMethod != null) {
                    onAnnotatedMethod.accept(hooks, method);
                } else {
                    super.hookOnAnnotatedMethod(hooks, method);
                }
            }

            @Override
            public void hookOnVariableType(Class<?> hook, BeanWrappedVariable variable) {
                if (hookOnVariableType != null) {
                    hookOnVariableType.accept(hook, variable);
                } else {
                    super.hookOnVariableType(hook, variable);
                }
            }
        };
    }

    @Override
    protected void onNew() {
        base().installIfAbsent(MyBean.class, b -> {
            base().addCodeGenerated(b, new Key<Map<Integer, MethodHandle>>() {}, () -> {
                return M = CollectionUtil.copyOf(m, v -> v.generateMethodHandle());
            });
        });
    }

    public void runInCodegen(Runnable action) {
        super.runOnCodegen(action);
    }

    static class MyBean {
        final ExtensionContext ec;
        final Map<Integer, MethodHandle> mh;

        MyBean(ExtensionContext ec, @CodeGenerated Map<Integer, MethodHandle> mh) {
            this.mh = mh;
            this.ec = EC = ec;
        }
    }
}
