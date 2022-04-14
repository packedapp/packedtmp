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
package app.packed.bean.context;

import static java.util.Objects.requireNonNull;

/**
 *
 */
public abstract class DependencyContext {
    private final DependencyContextKind kind;

    protected DependencyContext(DependencyContextKind kind) {
        this.kind = requireNonNull(kind);
    }

    public final DependencyContextKind kind() {
        return kind;
    }

    public static class None extends DependencyContext {

        protected None() {
            super(DependencyContextKind.NONE);
        }

    }

    static class EntityBean extends DependencyContext {

        protected EntityBean() {
            super(DependencyContextKind.BEAN);
        }

        public String oops(Class<?> hookType) {
            return "This annotation is only available for entity beans";
        }
    }
}
