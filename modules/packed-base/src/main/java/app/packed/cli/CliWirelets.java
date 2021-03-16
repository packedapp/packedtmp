package app.packed.cli;

import static java.util.Objects.requireNonNull;

import app.packed.component.Wirelet;

public class CliWirelets {

    public static Wirelet args(String... args) {
        return new MainArgsWirelet(MainArgs.of(args));
    }
    
    static final class MainArgsWirelet extends Wirelet {
        MainArgs args;

        MainArgsWirelet(MainArgs args) {
            this.args = requireNonNull(args);
        }
    }
}
