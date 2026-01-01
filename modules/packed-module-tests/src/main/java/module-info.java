// is open because of Eclipse Junit
module app.packed.module.tests {
    requires app.packed;
    //  requires jdk.incubator.concurrent; // Sometimes test

    opens app.packed.moduletests.isopen to app.packed;
}
