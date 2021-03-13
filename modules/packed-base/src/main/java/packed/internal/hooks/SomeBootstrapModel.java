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
import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.hooks.MethodHook;
import packed.internal.hooks.SomeBuilder.extensionClass;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class SomeBootstrapModel extends SomeBuildModel {

    /** A cache of any extensions a particular annotation activates. */
    private static final ClassValue<SomeBootstrapModel> ANNOTATED_METHODS = new ClassValue<>() {

        @Override
        protected SomeBootstrapModel computeValue(Class<?> type) {
            MethodHook ams = type.getAnnotation(MethodHook.class);
            return ams == null ? null : new SomeBuilder(ams.bootstrap(), extensionClass.METHOD, 0).bootstrap();
        }
    };

    /** A method handle for the constructor of a bootstrap. */
    private final MethodHandle mhConstructor = null;

    SomeBootstrapModel(SomeBuilder builder) {
        super(builder);
    }

    /**
     * Returns the bootstrap class.
     * 
     * @return the bootstrap class
     */
    public final Class<?> bootstrapImplementation() {
        return mhConstructor.type().returnType();
    }

    /**
     * Returns a new bootstrap instance.
     * 
     * @return a new bootstrap instance
     * 
     */
    public final Object newInstance() {
        try {
            return mhConstructor.invoke();
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    @Nullable
    public static SomeBootstrapModel getForAnnotatedExtensionMethod(Class<? extends Annotation> c) {
        return ANNOTATED_METHODS.get(c);
    }
}
