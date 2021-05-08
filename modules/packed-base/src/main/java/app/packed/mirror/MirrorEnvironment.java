package app.packed.mirror;


// https://docs.scala-lang.org/overviews/reflection/environment-universes-mirrors.html
// reflect = build time, introspect = runtime.. IDK
// Der er jo saadan set ikke noget i vejen for at supportere det runtume
enum MirrorEnvironment {
    BUILD_TIME, RUNTIME;
}
