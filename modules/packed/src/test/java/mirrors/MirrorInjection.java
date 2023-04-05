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
package mirrors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanMirror;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Author;
import app.packed.extension.BaseExtension;
import app.packed.extension.BeanHook.BindingTypeHook;
import app.packed.operation.BindingKind;
import app.packed.operation.BindingMirror;
import app.packed.operation.OperationMirror;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationTemplate;
import tools.TestApp;

/**
 * Tests direct injection of various mirrors
 */
public class MirrorInjection {

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
                assertEquals("/Into", bm.path().toString());

                List<?> l = List.of(ApplicationMirror.class, AssemblyMirror.class, ContainerMirror.class, BeanMirror.class, OperationMirror.class);
                assertEquals(5, om.bindings().size());
                for (int i = 0; i < 5; i++) {
                    BindingMirror bim = om.bindings().get(i);
                    assertEquals(om, bim.operation());
                    assertEquals(i, bim.operationBindingIndex());
                    assertEquals(BindingKind.HOOK, bim.bindingKind());
                    assertSame(Author.extension(BaseExtension.class), bim.zBoundBy());
                    assertTrue(bim.variable().annotations().isEmpty());
                    assertEquals(l.get(i), bim.variable().type());
                    assertEquals(l.get(i), bim.variable().rawType());
                }
            }
        }

        TestApp t = TestApp.of(c -> {
            c.onAnnotatedFieldHook((l, b) -> {
                OperationHandle h = b.newGetOperation(OperationTemplate.defaults());
                c.generate(h);
            });
            c.provide(Into.class).export();
        });
        assertNotNull(t.use(Into.class));
    }

    @Test
    public void unknownMirror() {
        @BindingTypeHook(extension = BaseExtension.class)
        record MirrorAlien() {}
        TestApp.assertThrows(BeanInstallationException.class, c -> {
            c.onAnnotatedFieldHook((l, b) -> b.newGetOperation(OperationTemplate.defaults()));
            record Foo(MirrorAlien alien) {}
            c.install(Foo.class);
        });
    }
}
