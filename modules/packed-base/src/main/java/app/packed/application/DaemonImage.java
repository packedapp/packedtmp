package app.packed.application;

import app.packed.container.BaseAssembly;

// Nahhh
public interface DaemonImage {

}

class Nice extends BaseAssembly {

    @Override
    protected void build() {}

    public static void main(String[] args) {
        Daemon.launcher().args(args).start(new Nice());
    }
}