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
package app.packed.app;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A wrapper around an array of strings. Typically provided via a public-static-main method.
 */
public final class MainArgs implements Iterable<String> {

    private final List<String> args;

    MainArgs(String[] args) {
        this.args = List.of(args);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<String> iterator() {
        return args.iterator();
    }

    public void onOption(String option, Runnable r) {
        if (args.contains(option)) {
            r.run();
        }
    }

    public void onCommand(String option, Consumer<String> r) {

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

    public static void main(String[] args) {
        MainArgs ma = MainArgs.of(args);
        ma.onOption("-f", () -> System.out.println("ff"));

        // ma.onOption("-f", () -> new );
    }

    /// Main ArgsBuidler

    // onOption(xxxx).helpWith();
}
