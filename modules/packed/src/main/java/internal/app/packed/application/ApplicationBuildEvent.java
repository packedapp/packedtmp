package internal.app.packed.application;

import app.packed.container.Assembly;

/**
    *
    */
public class ApplicationBuildEvent extends jdk.jfr.Event {

    public String applicationName;

    public Class<? extends Assembly> assemblyClass;

}