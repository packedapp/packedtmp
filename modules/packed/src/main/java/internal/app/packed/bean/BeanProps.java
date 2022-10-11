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
package internal.app.packed.bean;

import app.packed.base.Nullable;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.RealmSetup;

public record BeanProps(

        /** The kind of bean. */
        BeanKind kind,

        /** The bean class, is typical void.class for functional beans. */
        Class<?> beanClass,

        /** The type of source the installer is created from. */
        BeanSourceKind sourceKind,

        /** The source ({@code null}, {@link Class}, {@link PackedOp}, or an instance) */
        @Nullable Object source,

        /** A model of hooks on the bean class. Or null if no member scanning was performed. */
        @Nullable BeanClassModel beanModel,

        /** The operator of the bean. */
        ExtensionSetup operator,

        RealmSetup realm,

        @Nullable ExtensionSetup extensionOwner,

        // Dem her har vi ikke behov for at gemme
        @Nullable String namePrefix,

        boolean multiInstall) {
}
