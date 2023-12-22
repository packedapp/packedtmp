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
package app.packed.cli;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import app.packed.extension.ExtensionMetaHook.AnnotatedBeanMethodHook;
import app.packed.namespace.NamespaceOperation;

/**
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NamespaceOperation
@AnnotatedBeanMethodHook(extension = CliExtension.class)
public @interface CliCommand {

    /**
     * The name(s) of the command. If no name is specified. The name of the annotated method is used. Method names starting
     * with underscores will automatically be renamed to hyphens.
     *
     * @return the name(s) of the command
     */
    String[] name() default {};

    /**
     * A builder that allows for programmatically constructing commands.
     *
     * @see CliExtension#addCommand(String...)
     */
    // Hvad er det praecis den skal kunne???
    // cli.addCommand().name("foobar").execute(new Op1<MyService>(s->s.print);
    public interface Builder {

         Builder name(String... name);

        void run(Consumer<CliCommandContext> action);
    }
}
