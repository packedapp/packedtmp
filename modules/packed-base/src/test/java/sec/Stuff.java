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
package sec;

import java.lang.reflect.Modifier;

import app.packed.inject.Inject;
import packed.internal.thirdparty.asm.ClassWriter;
import packed.internal.thirdparty.asm.MethodVisitor;
import packed.internal.thirdparty.asm.Opcodes;
import packed.internal.thirdparty.asm.Type;
import support.stubs.Letters.B;

/**
 *
 */
public class Stuff {

    public static void main(String[] args) {
        TestableClassLoader tcl = new TestableClassLoader();

        TestableModuleFinder tmf = new TestableModuleFinder(tcl);

        for (var m : tmf.findAll()) {
            System.out.println(m.location());
        }

    }

    @SuppressWarnings("deprecation")
    public static void madin(String[] args) throws Exception {
        GeneratingClassLoader gcl = new GeneratingClassLoader();

        Class<?> cl = gcl.generate();

        cl.newInstance();

        System.out.println("Bye");
    }

    static class GeneratingClassLoader extends ClassLoader {
        static String name = "__generated";

        GeneratingClassLoader() {
            super(B.class.getClassLoader());
        }

        Class<?> generate() {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cw.visit(Opcodes.V1_5, Modifier.PUBLIC, name, null, Type.getInternalName(Object.class), null);

            String sig = "(" + Type.getDescriptor(B.class) + ")V";

            MethodVisitor mv = cw.visitMethod(Modifier.PUBLIC, "<init>", sig, null, null);

            mv.visitAnnotation(Type.getDescriptor(Inject.class), true);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            cw.visitEnd();

            byte[] buf = cw.toByteArray();

            return defineClass(name.replace('/', '.'), buf, 0, buf.length);
        }
    }

}
