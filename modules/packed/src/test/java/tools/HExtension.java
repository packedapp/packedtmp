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
package tools;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import app.packed.extension.BaseExtensionPoint.CodeGenerated;
import app.packed.extension.BeanElement.BeanField;
import app.packed.extension.operation.OperationHandle;
import app.packed.extension.Extension;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.ContainerContext;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.AnnotationList;
import app.packed.util.Key;
import internal.app.packed.util.CollectionUtil;

/**
 *
 */
public class HExtension extends Extension<HExtension> {

    // Tmp hack until channels work better
    static Map<Integer, MethodHandle> M = new HashMap<>();

    static ContainerContext EC;

    ServiceableBeanConfiguration<MyBean> install;

    private HashMap<Integer, OperationHandle> m = new HashMap<>();

    BiConsumer<? super AnnotationList, ? super BeanField> onAnnotatedField;

    int counter;

    HExtension() {}

    void addFieldCallback(BiConsumer<? super AnnotationList, ? super BeanField> consumer) {
        onAnnotatedField = consumer;
    }

    <T> void generate(OperationHandle h) {
        generate(++counter, h);
    }

    <T> void generate(Integer name, OperationHandle h) {
        if (m.putIfAbsent(name, h) != null) {
            throw new AssertionError();
        }
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
        final ContainerContext ec;
        final Map<Integer, MethodHandle> mh;

        MyBean(ContainerContext ec, @CodeGenerated Map<Integer, MethodHandle> mh) throws Throwable {
            this.mh = mh;
            this.ec = EC = ec;
        }
    }
}
