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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import packed.internal.classscan.invoke.MethodHandleBuilder;
import packed.internal.classscan.invoke.OpenClass;
import packed.internal.sidecar.model.Model;
import packed.internal.sidecar.old.SidecarTypeMeta;

/**
 * A model of a sidecar.
 */
public abstract class AbstractExtensionModel extends Model {

    /**
     * Creates a new sidecar model.
     * 
     * @param builder
     *            the builder
     */
    protected AbstractExtensionModel(Builder builder) {
        super(builder.sidecarType);
    }

    public static abstract class Builder {

        public MethodHandle builderMethod;

        /** The constructor used to create a new extension instance. */
        MethodHandle constructor;

        protected final Class<?> sidecarType;

        protected Builder(Class<?> sidecarType, SidecarTypeMeta statics) {
            this.sidecarType = requireNonNull(sidecarType);
        }

        protected void onMethod(Method m) {}

        protected OpenClass prep(MethodHandleBuilder spec) {
            OpenClass cp = new OpenClass(MethodHandles.lookup(), sidecarType, true);
            this.constructor = cp.findConstructor(spec);
            cp.findMethods(m -> {
                onMethod(m);
            });

            return cp;
        }
    }

}
