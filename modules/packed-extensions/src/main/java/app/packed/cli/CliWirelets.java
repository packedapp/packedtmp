package app.packed.cli;

import static java.util.Objects.requireNonNull;

import app.packed.bundle.Wirelet;

public final class CliWirelets {
    private CliWirelets() {}

    public static Wirelet args(String... args) {
        return new MainArgsWirelet(CliArgs.of(args));
    }

    static final class MainArgsWirelet extends Wirelet {
        final CliArgs args;

        MainArgsWirelet(CliArgs args) {
            this.args = requireNonNull(args);
        }
    }
}
