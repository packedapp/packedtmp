/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package tck.extensions.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import app.packed.application.ApplicationMirror;
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanTrigger.AutoInject;
import app.packed.binding.BindingKind;
import app.packed.binding.BindingMirror;
import app.packed.component.ComponentRealm;
import app.packed.bean.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.container.ContainerMirror;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationMirror;
import tck.ServiceLocatorAppTest;

/**
 * Tests direct injection of various mirrors
 */
public class RuntimeMirrorInjectionTest extends ServiceLocatorAppTest {

    @Test
    public void instanceFieldGet() throws Throwable {
        record Into(ApplicationMirror am, AssemblyMirror asm, ContainerMirror cm, BeanMirror bm, OperationMirror om) {

            @SuppressWarnings("unused")
            Into {
                assertEquals(cm.application(), am);
                assertEquals(asm.application(), am);
                assertEquals(asm.container(), cm);
                assertEquals(bm.container(), cm);
                assertEquals(cm.assembly(), asm);
                assertEquals(om.bean(), bm);
                //assertEquals("/Into", bm.componentPath().toString());

                List<?> l = List.of(ApplicationMirror.class, AssemblyMirror.class, ContainerMirror.class, BeanMirror.class, OperationMirror.class);
                assertEquals(5, om.bindings().size());
                for (int i = 0; i < 5; i++) {
                    BindingMirror bim = om.bindings().get(i);
                    assertEquals(om, bim.operation());
                    assertEquals(i, bim.bindingIndex());
                    assertEquals(BindingKind.ANNOTATION, bim.bindingKind());
                    assertSame(ComponentRealm.extension(BaseExtension.class), bim.boundBy());
                    assertTrue(bim.variable().annotations().isEmpty());
                    assertEquals(l.get(i), bim.variable().type());
                    assertEquals(l.get(i), bim.variable().rawType());
                }
            }
        }

        install(Into.class).export();

        assertNotNull(app().use(Into.class));
    }

    @Test
    public void unknownMirror() {
        @AutoInject(introspector = MirrorAlienBeanIntrospector.class)
        record MirrorAlien() {}

        hooks().onAnnotatedField((_, b) -> b.newGetOperation());
        record Foo(MirrorAlien alien) {}

        assertThrows(BeanInstallationException.class, () -> install(Foo.class));
    }
}

final class MirrorAlienBeanIntrospector extends BeanIntrospector<BaseExtension> {

    @Override
    public void onExtensionService(Key<?> key, OnContextService service) {
        super.onExtensionService(key, service);
    }
}
