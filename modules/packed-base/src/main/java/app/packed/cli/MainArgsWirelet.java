package app.packed.cli;

import static java.util.Objects.requireNonNull;

import app.packed.component.Wirelet;

public final class MainArgsWirelet extends Wirelet {
    MainArgs args;

    MainArgsWirelet(MainArgs args) {
        this.args = requireNonNull(args);
    }
}
