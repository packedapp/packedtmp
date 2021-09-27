package app.packed.application.various;

import java.io.IOException;

import app.packed.application.ApplicationDriver;
import app.packed.bundle.Bundle;

// Jeg taenker man vil typisk wrappe den i et andet image


// En anden maade er at wrappe exceptions af en given type i wrapper type
// IOException -> wrap my LocalExcep (som ikek initializere et TrackState)
// Hvorefter vi udpakker den oprindelige exception og smider den

interface ThrowableApplicationImage<A, T extends Throwable> {
    A launch() throws T;
}

class IOApplicationImage<A> {

    final ThrowableApplicationImage<A, IOException> image;

    IOApplicationImage(ThrowableApplicationImage<A, IOException> image) {
        this.image = image;
    }

    public A launch() throws IOException {
        return image.launch();
    }

    public IOApplicationImage<A> of(ApplicationDriver<A> driver, Bundle<?> assembly) {
        // driver.mapExceptions().imageOf();
        throw new UnsupportedOperationException();
    }
}