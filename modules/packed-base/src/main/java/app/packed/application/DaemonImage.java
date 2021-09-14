package app.packed.application;

import app.packed.container.BaseBundle;

// Nahhh
public interface DaemonImage {

}

class Nice extends BaseBundle {

    @Override
    protected void build() {}

    public static void main(String[] args) {
        Daemon.launcher().args(args).start(new Nice());
    }
}