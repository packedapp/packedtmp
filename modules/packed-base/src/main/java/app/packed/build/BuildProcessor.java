package app.packed.build;

import java.util.List;
import java.util.function.BiFunction;

import app.packed.application.ApplicationMirror;
import app.packed.application.Daemon;
import app.packed.bean.instance.TstExt;
import app.packed.cli.CliWirelets;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;

// Ideen er lidt at du er pisse genial

// Desvaerre giver den sgu ikke rigtig mening...

interface BuildProcessor<T> {

    boolean isFailed();
    boolean isSuccess();
    
    List<?> errors();
    
    T get();
    
    public static void main(String[] args) {
        BuildProcessor<ApplicationMirror> p = BuildProcessor.of(Daemon::mirror, new TstExt(), CliWirelets.args("qweqwe"));
        
        BuildProcessor<Daemon> px = BuildProcessor.of(Daemon::run, new TstExt(), CliWirelets.args("qweqwe"));
        
        System.out.println(p);
        System.out.println(px);
    }
    
    static <T> BuildProcessor<T> of(BiFunction<Assembly<?>, Wirelet[], T> action, Assembly<?> assembly, Wirelet... wirelets) {
        // Vi smider maaske en special BuildException som vi kan catche
        throw new UnsupportedOperationException();
    }
}
