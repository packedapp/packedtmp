package internal.app.packed.jfr;

import app.packed.container.Assembly;

/**
    *
    */
public class BuildApplicationEvent extends jdk.jfr.Event {

    public String applicationName;

    public Class<? extends Assembly> assemblyClass;

}