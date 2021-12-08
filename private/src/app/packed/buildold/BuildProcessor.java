package app.packed.buildold;

import java.util.List;
import java.util.function.BiFunction;

import app.packed.application.ApplicationMirror;
import app.packed.application.AsyncApp;
import app.packed.bean.instance.TstExt;
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
        BuildProcessor<ApplicationMirror> p = BuildProcessor.of(AsyncApp::mirrorOf, new TstExt(), Wirelet.named("qweqwe"));
        
     //   BuildProcessor<Void> px = BuildProcessor.of(App::run, new TstExt(), Wirelet.named("qweqwe"));
        
        System.out.println(p);
       // System.out.println(px);
    }
    
    static <T> BuildProcessor<T> of(BiFunction<Assembly , Wirelet[], T> action, Assembly  assembly, Wirelet... wirelets) {
        // Vi smider maaske en special BuildException som vi kan catche
        throw new UnsupportedOperationException();
    }
}
