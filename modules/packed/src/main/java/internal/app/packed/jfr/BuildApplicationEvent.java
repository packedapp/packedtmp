package internal.app.packed.jfr;

import app.packed.assembly.Assembly;

/**
    *
    */

// Enabling JFR adds about 40 ms to application startup time
public class BuildApplicationEvent extends jdk.jfr.Event {

    public String applicationName;

    public Class<? extends Assembly> assemblyClass;

}