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
package tck;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.bean.scanning.BeanIntrospector.OnField;
import app.packed.bean.scanning.BeanIntrospector.OnVariableUnwrapped;
import app.packed.bean.scanning.BeanTrigger.OnAnnotatedField;
import app.packed.bean.scanning.BeanTrigger.OnAnnotatedMethod;
import app.packed.binding.Key;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import app.packed.extension.ExtensionHandle;
import app.packed.operation.OperationHandle;
import app.packed.util.AnnotationList;
import app.packed.util.Nullable;
import internal.app.packed.invoke.ServiceHelper;
import tck.AbstractAppTest.TestStore;
import testutil.MemberFinder;

/**
 *
 */
public class HookTestingExtension extends Extension<HookTestingExtension> {

    private static final Key<Map<String, MethodHandle>> KEY = new Key<Map<String, MethodHandle>>() {};
    private Map<String, OperationHandle<?>> ink = new HashMap<>();

    @Nullable
    BiConsumer<? super AnnotationList, ? super OnField> onAnnotatedField;

    @Nullable
    BiConsumer<? super AnnotationList, ? super BeanIntrospector.OnMethod> onAnnotatedMethod;

    @Nullable
    BiConsumer<? super Class<?>, ? super OnVariableUnwrapped> onVariableType;

    /**
     * @param handle
     */
    HookTestingExtension(ExtensionHandle<HookTestingExtension> handle) {
        super(handle);
    }

    void generate(String name, OperationHandle<?> oh) {
        requireNonNull(name);
        ink.putIfAbsent(name, oh);

        base().installIfAbsent(HookBean.class, b -> b.bindServiceInstance(KEY, ServiceHelper.forTestingMap(ink)));
    }

    public HookTestingExtension onAnnotatedField(BiConsumer<? super AnnotationList, ? super OnField> consumer) {
        onAnnotatedField = consumer;
        return this;
    }

    public HookTestingExtension onAnnotatedMethod(BiConsumer<? super AnnotationList, ? super BeanIntrospector.OnMethod> consumer) {
        onAnnotatedMethod = consumer;
        return this;
    }

    public HookTestingExtension onVariableType(BiConsumer<? super Class<?>, ? super OnVariableUnwrapped> onVariableType) {
        this.onVariableType = onVariableType;
        return this;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @OnAnnotatedField(introspector = HookTestingExtensionBeanIntrospector.class, allowGet = true, allowSet = true)
    public @interface FieldHook {

        String name() default "main";

        /** Exposes a private instance field with {@link AnnoOnField}. */
        public static class FieldPrivateInstanceString {

            public static final Field FOO_FIELD = MemberFinder.findFieldOnThisClass("foo");

            @FieldHook
            private String foo = "instance";

            public String fieldValue() {
                return foo;
            }
        }

        public static class FieldPrivateStaticString {

            @FieldHook
            private static String foo = "static";
        }
    }

    static class HB {
        public HB() {
            IO.println("HMMM");
        }
    }

    static class HookBean {
        final ExtensionContext ec;
        final Map<String, MethodHandle> mh;

        HookBean(ExtensionContext ec, Map<String, MethodHandle> mh) {
            this.mh = mh;
            TestStore ts = AbstractAppTest.ts();
            for (Entry<String, MethodHandle> e : mh.entrySet()) {
                MethodHandle m = e.getValue();
                Object[] args = {};
                if (m.type().parameterCount() > 0 && m.type().parameterType(0) == ExtensionContext.class) {
                    args = new Object[] { ec };
                }
                ts.invokers.put(e.getKey(), new Invoker(e.getKey(), m, args));
            }
            this.ec = ec;
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @OnAnnotatedMethod(introspector = HookTestingExtensionBeanIntrospector.class)
    public @interface MethodHook {

        public static class InstanceMethodNoParamsVoid {
            public static final Method FOO = MemberFinder.findMethod("foo");

            @MethodHook
            void foo() {}

            public static void validateFoo(AnnotationList hooks, BeanIntrospector.OnMethod m) {
                // validate annotations
                assertEquals(FOO, m.method());
                assertEquals(FOO.getModifiers(), m.modifiers());
                m.toKey(); // should fail
                assertEquals(Key.of(String.class), m.toKey());
            }
        }

    }
}
