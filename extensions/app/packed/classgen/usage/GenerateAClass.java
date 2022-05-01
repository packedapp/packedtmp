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
package app.packed.classgen.usage;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.classgen.ClassgenExtension;
import app.packed.classgen.ClassgenExtensionMirror;
import app.packed.container.BaseAssembly;

/**
 *
 */
public class GenerateAClass extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        try {
            Class<?> clazz = HiddenClass.class;
            String className = clazz.getName();
            String classAsPath = className.replace('.', '/') + ".class";
            InputStream stream = clazz.getClassLoader().getResourceAsStream(classAsPath);
            byte[] bytes = stream.readAllBytes();
            use(ClassgenExtension.class).defineHiddenClass(MethodHandles.lookup(), bytes, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ApplicationMirror am = App.mirrorOf(new GenerateAClass());

        am.useExtension(ClassgenExtensionMirror.class).generatedClasses().forEach(c -> System.out.println(c));
    }
}
