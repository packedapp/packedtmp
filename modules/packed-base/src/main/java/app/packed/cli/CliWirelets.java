package app.packed.cli;

import static java.util.Objects.requireNonNull;

import app.packed.component.Wirelet;
import packed.internal.component.ComponentSetup;
import packed.internal.component.InternalWirelet;

public class CliWirelets {

    public static Wirelet args(String... args) {
        return new MainArgsWirelet(MainArgs.of(args));
    }
    
    static final class MainArgsWirelet extends InternalWirelet {
        final MainArgs args;

        MainArgsWirelet(MainArgs args) {
            this.args = requireNonNull(args);
        }

        @Override
        protected void onBuild(ComponentSetup component) {
            // TODO Auto-generated method stub
            
        }
    }
}
