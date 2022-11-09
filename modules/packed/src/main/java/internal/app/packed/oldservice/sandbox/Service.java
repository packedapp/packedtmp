package internal.app.packed.oldservice.sandbox;

import app.packed.framework.Key;

/**
 * A description of a service.
 */
// isLazy.. Kan vi sige noget om det.. Det vil jeg ikke mene
public /* sealed */ interface Service {

    /**
     * Returns whether or not the instance that service provides is a constant.
     * <p>
     * Services that are constant will always provide the same service instance.
     * <p>
     * Services that are not constant may provide the same instance every time. But usually provides different. For example,
     * context dependent services
     * <p>
     * If a service is constant the instance can always be cached.Otherwise care must be taken.
     * 
     * @return whether or not the instance that service provides is a constant
     */
    // Hmm Saa lad sige jeg vil lave Logger..
    // Saa er den jo constant for mit scope...
    // Den returnere den samme logger hver gang...
    // 21-10-03: Men en logger ville jo aldrig vaere en service
    // Altsaa mere en description...
    boolean isConstant();

    /** {@return the key of the service.} */
    Key<?> key();
}

// Deleted
// Vi kan ikke sige noget om den type service der bliver provided.
// Kan jo returnere en hvilke som helst subtype
///**
// * Returns the raw type of the service.
// * <p>
// * Actual service instances may be subclasses of the returned type.
// * 
// * @return the raw type of the service
// * @see Key#rawType()
// */
//// Hmmm, IDK about this... Den siger jo ikke noget om typen...
//default Class<?> rawType() {
//    return key().rawType();
//}