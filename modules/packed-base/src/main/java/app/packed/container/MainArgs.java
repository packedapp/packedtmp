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

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A simple wrapper around an array of strings. Is typically used to wrap the string array argument to a
 * public-static-main method.
 */
// Rename to StringArgs and put it in utils...
// AppWiringOptions.main(String... args)
public final class MainArgs implements Iterable<String> /* WiringOperation */ {

    private final List<String> args;

    MainArgs(String[] args) {
        this.args = List.of(args);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<String> iterator() {
        return args.iterator();
    }

    // Maybe, have Runnable, String option, String alternatives()
    public void onOption(String option, Runnable r) {
        if (args.contains(option)) {
            r.run();
        }
    }

    public void onCommand(String option, Consumer<String> r) {
        // Den fungere godt med den nye switch???
        // Det der er, er at vi gerne vil kunne checke om vi har en valid kommando.
        //
        // dvs. ifNoCommandsHaveBeenRunExecute()....
        // men saa er denne struktur ogsaa mutable...
        // Lidt det samme som pattern matching
    }

    public void onCommandList(String option, Consumer<String> r) {
        // invoke every time.
    }

    public <T> void onCommand(String option, Class<T> type, Consumer<T> r) {

    }

    public Stream<String> stream() {
        return args.stream();
    }

    public static MainArgs of(String... args) {
        requireNonNull(args, "args is null");
        return new MainArgs(args);// check null
    }

    public static Wirelet wirelet(String... args) {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        MainArgs ma = MainArgs.of(args);
        ma.onOption("-f", () -> System.out.println("ff"));

        // ma.onOption("-f", () -> new );
    }

    /// Main ArgsBuidler

    // onOption(xxxx).helpWith();
}
