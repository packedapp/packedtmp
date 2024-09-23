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
package internal.app.packed.bean.scanning;

import java.lang.reflect.Member;

import app.packed.util.AnnotationList;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.util.PackedAnnotationList;

/** The super class of operational members. The inheritance hierarchy follows that of {@link Member}. */
abstract sealed class PackedBeanMember<M extends Member> extends PackedBeanElement permits PackedBeanExecutable {

    /** Annotations on the member. */
    private final PackedAnnotationList annotations;

    /** The member. */
    final M member;

    final BeanScannerParticipant participant;

    PackedBeanMember(BeanScannerParticipant participant, M member, PackedAnnotationList annotations) {
        this.participant = participant;
        this.member = member;
        this.annotations = annotations;
    }

    /** {@return a list of annotations on the member.} */
    @Override
    public final AnnotationList annotations() {
        return annotations;
    }

    @Override
    public BeanSetup bean() {
        return participant.scanner.bean;
    }

    /** Check that we calling from within {@link BeanIntrospector#onField(OnField).} */
    final void checkConfigurable() {
        // TODO we need to check the Authority instead of the assemvly
        BeanSetup bean = bean();
        if (bean.owner instanceof AssemblySetup) {
            if (!participant.extension.container.assembly.isConfigurable()) {
                throw new IllegalStateException("This method must be called before the assembly is closed");
            }
        } else {
            ExtensionSetup e = (ExtensionSetup) bean.owner;
            System.out.println(e.isConfigurable());
            if (!e.isConfigurable()) {
                throw new IllegalStateException("This method must be called before the extension is closed");
            }
        }
    }

    /** {@return the modifiers of the member.} */
    @Override
    public final int modifiers() {
        return member.getModifiers();
    }
}
