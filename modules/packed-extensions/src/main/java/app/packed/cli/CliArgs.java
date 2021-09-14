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

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.hooks.sandbox2.OldAutoService;

/**
 * Represents the arguments to a method main.
 */
@OldAutoService
// CliArgs??? Jaa, was MainArgs
// Maaske MainArgs kun kan bruges med @Main og friends...
public final class CliArgs {

    private final String[] args;

    // Det gode ved declarativt er at
    CliArgs(String... args) {
        this.args = args;
    }

    /**
     * Returns an array containing all arguments.
     * <p>
     * The returned array will be "safe" in that no references to it are maintained by this class. The caller is thus free
     * to modify the returned array.
     * <p>
     * Think replace it. The mutability of the array is undertemined. But the contents of the array will always represent
     * the correct
     *
     * @return an array containing all of the elements in this list in proper sequence
     */
    public String[] toArray() {
        return args.clone();
    }

    /**
     * Returns a list containing all arguments in proper sequence.
     *
     * @return an list containing all of the arguments in proper sequence
     */
    public List<String> toList() {
        return List.of(args);
    }

    // som default wirelet er den nok unconsumed

    /**
     * Create a new MainArgs instance using the specified arguments
     * 
     * @param args
     *            the arguments
     * @return the new main args instance
     */
    public static CliArgs of(String... args) {
        requireNonNull(args, "args is null");
        return new CliArgs(args);
    }
}
// Er ikke en service, men en extension type

//Kunne vaere fedt hvis man kunne gemme den...
//Maaske har vi en CLI extension der kan gemme den...
//Saa MainArgs er en Packlet??? IDK

//Tror det bliver en wirelet
//Og saa tror den autoaktive
//Saa behoever man heller ikke provide den op og ned

//Eneste problem er at hvis vi bruger den paa et image...
//Saa skal CliExtension vaere enablet inde...
//Men det er den vel ogsaa hvis man bruger Main et sted...
//Men det er maaske fint at man ikke kan specificere den hvis den ikke bliver brugt
//Saa vi er baade wirelet og auto-service

//Kraever vi har en cli extension...
//Ved ikke rigtig hvad den skal...
//CliExtension.addHelp();

//Lidt speciel, maaske vi godt vil bibeholder @WireletConsume...
//Saa kan vi baade faa den injected i extensionen. og brugerkode

//Jeg tror ikke det er en inheritable wirelet...
//Men styres gennem selve extensionen...
//Saa den har noget AutoService paa... dvs man kan injecte den paa 2 forskellige maader
//@WireletConsume <--- kan kun bruges af CliExtension
