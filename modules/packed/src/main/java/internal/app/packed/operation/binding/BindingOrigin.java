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
package internal.app.packed.operation.binding;

import internal.app.packed.bean.BeanSetup;

/**
 *
 */
// Hvad skal vi bruge den til???? Naar vi har Mirror??? Mirror'et skal maaske bruge den
public abstract sealed class BindingOrigin {

    public static final class OpTarget extends BindingOrigin {

    }

    public static final class CompositeBindingTarget extends BindingOrigin {

    }

    public static final class ExtensionServiceBindingTarget extends BindingOrigin {
        public BeanSetup extensionBean;

        public final Class<?> extensionBeanClass;

        ExtensionServiceBindingTarget(Class<?> extensionBeanClass) {
            this.extensionBeanClass = extensionBeanClass;
        }

    }

    public static final class ServiceBindingTarget extends BindingOrigin {

    }

    public static final class BindingHookTarget extends BindingOrigin {

    }
}
