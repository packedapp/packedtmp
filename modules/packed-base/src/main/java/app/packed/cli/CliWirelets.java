package app.packed.cli;

import static java.util.Objects.requireNonNull;

import app.packed.container.Wirelet;
import packed.internal.container.ContainerSetup;
import packed.internal.container.InternalWirelet;

public class CliWirelets {

    public static Wirelet args(String... args) {
        return new MainArgsWirelet(CliArgs.of(args));
    }
    
    static final class MainArgsWirelet extends InternalWirelet {
        final CliArgs args;

        MainArgsWirelet(CliArgs args) {
            this.args = requireNonNull(args);
        }

        @Override
        protected void onBuild(ContainerSetup component) {
            // TODO Auto-generated method stub
            
        }
    }
}
