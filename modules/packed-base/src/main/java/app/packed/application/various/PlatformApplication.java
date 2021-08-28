package app.packed.application.various;

import java.util.function.Consumer;

import app.packed.state.sandbox.InstanceState;

// root application
// will automatically have a shutdown hook installed
// will have a unique name

// PlatformHost?
// Ved ikke om vi har brug for den...
// Det er jo bare en wirelet der siger om vi er en platform application

// ApplicationHostRegistry platformHost();

public interface PlatformApplication {

    /**
     * Returns the name of the application.
     * <p>
     * Every platform application has a unique name.
     * 
     * @return
     */
    String name();

    InstanceState state(); // the current state

    public static void forEach(Consumer<? super PlatformApplication> action) {

    }
}
// Det er jo saadan set en stor host der er gemt i et statisk field...
// Saa alle de der guest - linger settings kan vel blive anvendt.

// ScheduledOnFixedRate(1 minute)
// if (lastActivae<now-60 seconds) {
// application.persist()
// }

// En maade er fx at loebe alle cache entries igennem en gang i minuttet.
// Og se om der er nogle der er blevet read inde for de sidste 60 sekunder
interface LastActive {
    void active();
}

interface PlatformEnvironment {
  
    static PlatformEnvironment instance() {
        throw new UnsupportedOperationException();
    }
}