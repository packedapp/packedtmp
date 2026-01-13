module app.packed.modulepath.tests {
    requires app.packed;
    //  requires jdk.incubator.concurrent; // Sometimes test

    opens app.packed.moduletests.isopen to app.packed;
}
