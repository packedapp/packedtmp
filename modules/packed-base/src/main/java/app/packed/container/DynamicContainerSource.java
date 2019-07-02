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
package app.packed.container;

import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import packed.internal.asm.AnnotationVisitor;
import packed.internal.asm.Attribute;
import packed.internal.asm.ClassReader;
import packed.internal.asm.ClassVisitor;
import packed.internal.asm.Opcodes;
import packed.internal.asm.Type;

/**
 *
 */
// ExternalContainerSource???

// Sikkerhed... MethodHandles.Lookup object?????
//// Hvem har tilladelse til at loade moduler

// Hele ideen er vel at vi kan lazy loade?
// Ellers kunne vi vel ligesaa godt lave en Bundle direkte.

public final class DynamicContainerSource implements ArtifactSource {

    /** Lapp/packed/component/Install; */
    static final String PACKED_MODULE_DESCRIPTOR = Type.getDescriptor(PackedModule.class);

    public static ArtifactSource load(Path... paths) {
        try {
            return load0(paths);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ArtifactSource load0(Path... paths) throws Exception {
        ModuleFinder mf = ModuleFinder.of(paths);

        String moduleName = null;
        String initializeMe = null;
        for (ModuleReference mr : mf.findAll()) {
            Optional<InputStream> o = mr.open().open("module-info.class");
            if (o.isPresent()) {
                ClassReader cr = new ClassReader(o.get());
                M mm = new M();
                cr.accept(mm, Opcodes.ASM7);
                if (mm.className != null) {
                    moduleName = mr.descriptor().name();
                    initializeMe = mm.className;
                }
            }
        }
        if (moduleName != null) {
            ModuleLayer parent = ModuleLayer.boot();

            Configuration cf = parent.configuration().resolve(mf, ModuleFinder.of(), Set.of(moduleName));
            ClassLoader scl = ClassLoader.getSystemClassLoader();

            ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);

            Class<?> c = layer.findLoader(moduleName).loadClass(initializeMe);
            Constructor<?> cc = c.getConstructor();
            cc.setAccessible(true);
            ContainerBundle b = (ContainerBundle) cc.newInstance();
            // System.out.println(System.currentTimeMillis() - now);
            return b;
            // App aa = App.of(b);
            // System.out.println(System.currentTimeMillis() - now);
            // aa.stream().forEach(ccc -> System.out.println(ccc.path()));
        }
        throw new UnsupportedOperationException();
    }

    static class M extends ClassVisitor {
        boolean shouldInstall;

        String className;

        public M() {
            super(Opcodes.ASM7);
        }

        /** {@inheritDoc} */
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (descriptor.equals(PACKED_MODULE_DESCRIPTOR)) {
                shouldInstall = true;
                return new AnnotationVisitor(Opcodes.ASM7) {
                    @Override
                    public void visit(final String name, final Object value) {
                        if (name.equals("bundle")) {
                            className = Type.getType(value.toString()).getClassName();
                        }
                    }
                };
            }
            return super.visitAnnotation(descriptor, visible);
        }

        /** {@inheritDoc} */
        @Override
        public void visitAttribute(Attribute attribute) {
            System.out.println(attribute);
            super.visitAttribute(attribute);
        }
    }
}
// Functionalitet
// Loading from external sources....
// Module layers...

// Things to support
/// Multiple versions of the same jar
/// Different ClassLoaders
/// FromDesk
/// Leightweight API ontop of modules...

// Maybe make an example with two modules implementing the same api....

// Do we cache the information??? I think yes....
// As long as you keep the ContainerSource instance around....

// Maybe some support for naming the layers. So it is easier to find out where.
// A class is loaded from

// host.deploy(DCL.module("sd/sd/sd/ddd.module"))

// Think we want an annotation on the Module

// @PackedModule(boot = Ddd.class)

// DynamicContainerLoader.link("asdasdasd.jar");

// Descriptors from ModuleSources....

// - Someway to watch a directory....
/// onFileChange(), onFileNew(), onFileDelete()