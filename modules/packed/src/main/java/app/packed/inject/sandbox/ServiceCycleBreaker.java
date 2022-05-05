package app.packed.inject.sandbox;

// Virker kun internt i en container
// Virker kun paa constructuren/factoriet
// Maaske behoever vi ikke <T> jo syntes det er ok

// ServiceCycleBreak
interface ServiceCycleBreaker<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param t
     *            the function argument
     * @return the function result
     * @throws IllegalStateException
     *             if invoked more than once
     */
    // Creates an R and initializes it
    // Well knowing that thisObject is being constructed
    T breakCycle(Object thisObject);
}

class Server {

    final Client client;

    // Framework should throw if breakCycle is not called exactly once
    // ISE/InjectionException Server.class failed to invoke DCB.breakCycle
    Server(ServiceCycleBreaker<Client> breaker) {
        // Creates and Initializes Client
        this.client = breaker.breakCycle(this);
        // Client will have been initialized at this point
    }
}

class Client {

    final Server server;

    Client(Server server) {
        this.server = server;
    }
}