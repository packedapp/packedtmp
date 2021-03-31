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
package packed.internal.hooks;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import app.packed.base.Nullable;
import app.packed.hooks.ClassHook;

/**
 *
 */
public final class BootstrapClassClassHookModel extends BootstrapClassModel<ClassHook.Bootstrap> {

    public final boolean allowAllAccess;

    /**
     * @param builder
     */
    private BootstrapClassClassHookModel(Builder builder) {
        super(builder);
        this.allowAllAccess = builder.allowAllAccess;
    }

    /** A cache of any extensions a particular annotation activates. */
    private static final ClassValue<BootstrapClassClassHookModel> EXTENSION_METHOD_ANNOTATION = new ClassValue<>() {

        @Override
        protected BootstrapClassClassHookModel computeValue(Class<?> type) {
            ClassHook ec = type.getAnnotation(ClassHook.class);
            return ec == null ? null : new Builder(ec.bootstrap(), ec.allowAllAccess()).build();
        }
    };

    /** A cache of any extensions a particular annotation activates. */
    private static final ClassValue<BootstrapClassClassHookModel> MANAGED_NOT_ANNOTATED = new ClassValue<>() {

        @Override
        protected BootstrapClassClassHookModel computeValue(Class<?> type) {
            return new Builder(type, false).build();
        }
    };

    @Nullable
    public static BootstrapClassClassHookModel getForAnnotatedClass(Class<? extends Annotation> c) {
        return EXTENSION_METHOD_ANNOTATION.get(c);
    }

    public static BootstrapClassClassHookModel ofManaged(Class<? extends ClassHook.Bootstrap> cl) {
        return MANAGED_NOT_ANNOTATED.get(cl);
    }

    /** A builder for method sidecar. */
    public final static class Builder extends BootstrapClassModel.Builder<ClassHook.Bootstrap> {

        public final boolean allowAllAccess;

        Builder(Class<?> clazz, boolean allowAccess) {
            super(clazz);
            this.allowAllAccess = allowAccess;
        }

        /** {@inheritDoc} */
        @Override
        protected BootstrapClassClassHookModel build() {
            return new BootstrapClassClassHookModel(this);
        }

        @Override
        protected void onMethod(Method method) {
            throw new UnsupportedOperationException();
        }
    }
}
