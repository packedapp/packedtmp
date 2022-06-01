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
package packed.internal.bean.hooks;

import java.lang.reflect.Member;

import packed.internal.bean.BeanSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.operation.PackedOperationTarget;
import packed.internal.util.OpenClass;

/**
 *
 *
 * <p>
 * If there are member hooks for multiple extensions (operators) a bean member is created for each extension.
 */
public abstract sealed class PackedBeanMember<T extends Member> implements PackedOperationTarget permits PackedBeanField, PackedBeanMethod {

    /** The bean that declares the member */
    public final BeanSetup bean;

    /** The bean member. */
    protected final T member;

    final OpenClass openClass;

    /** The extension that will operate any operations. */
    public final ExtensionSetup operator;

    PackedBeanMember(BeanMemberScanner scanner, ExtensionSetup operator, T member) {
        this.openClass = scanner.oc;
        this.bean = scanner.bean;
        this.operator = operator;
        this.member = member;
    }

    /** {@return modifiers for the member.} */
    public final int getModifiers() {
        return member.getModifiers();
    }
}
