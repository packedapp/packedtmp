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
package tck.mirror;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.function.Executable;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanMirror;
import app.packed.container.Assembly;
import app.packed.extension.BaseExtension;
import tck.TckAssemblies;

/**
 *
 */
public abstract class AbstractMirrorTest {

    public static final ApplicationMirror HW = App.mirrorOf(new TckAssemblies.HelloWorldAssembly());

    static Assembly assembly(Consumer<BaseExtension> c) {
        return new TckAssemblies.SimpleAssembly(c);
    }

    protected static final void frameworkMustInitialize(Executable e) {
        assertThrows(IllegalStateException.class, e);
    }

    protected void assertIdenticalMirror(Object expected, Object actual) {
        assertEquals(expected, actual);
    }

    public static BeanMirror singleApplicationBean(ApplicationMirror a) {
        List<BeanMirror> list = a.container().beans().toList();
        assertThat(list).hasSize(1);
        return list.get(0);
    }
}
