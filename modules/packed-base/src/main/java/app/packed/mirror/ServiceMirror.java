package app.packed.mirror;

import app.packed.container.ContainerMirror;
import app.packed.inject.Service;

public interface ServiceMirror extends Mirror, Service {

    public static MirrorSet<ServiceMirror> allExports(ContainerMirror container) {
        throw new UnsupportedOperationException();
    }
    
    public static MirrorSet<ServiceMirror> allOf(ContainerMirror container) {
        throw new UnsupportedOperationException();
    }
}
