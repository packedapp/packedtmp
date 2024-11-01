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
package app.packed.build;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Maybe ApplicationMirror.Printer
 * <p>
 * And maybe have the same for Container, Bean, ect... inject BeanMirror m, m.printer.details().print(); // maybe have
 * m.printDetails();
 */

// Where to Print,
// Serialisation Format (I think we only support text based)

public interface MirrorPrinter {

    // maybe it is not mirrors, but (open) options
    MirrorPrinter add(Class<? extends Mirror> mirrorType);

    MirrorPrinter addAll();

    MirrorPrinter format(); // Some kind of serialializer

    MirrorPrinter json();

    /**
     * Prints to {@code System.out}.
     * <p>
     * This is short for {@code print(System.out)}
     */
    default void print() {
        print(System.out);
    }

    /**
     * Prints to the specified file, overwritten any content that may already be present.
     * <p>
     * This is short for {@code print(new PrintStream(Files.newOutputStream(path)))}
     *
     * @param path
     *            the path of the file.
     * @throws IOException
     *             if the contents could not be printed to the specified files
     */
    default void print(Path path) throws IOException {
        print(new PrintStream(Files.newOutputStream(path)));
    }

    /**
     * Prints to the specified print stream.
     * <p>
     * @param stream
     *            the print stream to print to.
     */
    void print(PrintStream stream);

    // Skal specificeres sammen serializations
    // Ect en ScopedLocal... Hvis den ikke kan komme med som parameters
    // MirrorPrinterDetailLevel
    // printIfDetailed(Consumer<SomeKindOfPrinter>); // behoever maaske ikke en gang serialization
    enum Details {
       OVERVIEW, ALL;
    }
}
