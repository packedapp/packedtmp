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
package aexp.xain;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleReader;

import app.packed.component.Install;
import packed.internal.asm.AnnotationVisitor;
import packed.internal.asm.ClassReader;
import packed.internal.asm.ClassVisitor;
import packed.internal.asm.Opcodes;
import packed.internal.asm.Type;

/**
 *
 */

public class Fff {

    /** Lapp/packed/component/Install; */
    static final String INSTALL_DESCRIPTOR = Type.getDescriptor(Install.class);

    public static void main(String[] args) {
        System.out.println(INSTALL_DESCRIPTOR);
        Module m = Fff.class.getModule();
        try (ModuleReader reader = m.getLayer().configuration().findModule(m.getName()).get().reference().open()) {
            for (String s : reader.list().toArray(e -> new String[e])) {
                if (s.endsWith(".class") && !s.equals("module-info.class")) {
                    ClassReader cr = new ClassReader(reader.open(s).get());
                    M mm = new M();
                    cr.accept(mm, Opcodes.ASM7);
                    if (mm.shouldInstall) {
                        s = s.substring(0, s.length() - 6);
                        s = Type.getObjectType(s).getClassName();
                        System.out.println(Class.forName(m, s));
                    }
                }
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    static class M extends ClassVisitor {
        boolean shouldInstall;

        public M() {
            super(Opcodes.ASM7);
        }

        /** {@inheritDoc} */
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (descriptor.equals(INSTALL_DESCRIPTOR)) {
                shouldInstall = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

    }
}
