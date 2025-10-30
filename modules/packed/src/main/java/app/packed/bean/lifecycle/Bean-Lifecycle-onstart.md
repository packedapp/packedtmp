


// Lifetime = Whole Application, Non-Application Container Lifetime (ala session), Bean Lifetime

// onBean, onLifetime, onApplication...

// A bean is started when all OnStart methods on the bean has completed successfully

//Hvordan sikre vi os at DB kører førend WEB
//Hvis de ikke depender paa hinanden.
////Samtidig vil vi gerne maximere async tid, saa joine senest muligt

//Tror phases er en daarlig ide. Fordi vi godt vil encapsulated foer og efter. Det fungere daar

// When used on a field the target type must be Lazy



// Koer foer dependants
// Koer efter dependants
// Koer lige inde vi starter


///// !
//// Never await, keep running
//// Until Finished
//@Deprecated(since = "OnStart is always joining")
//JOIN_NEVER

//String JOIN_ON_AFTER_DEPENDENCIES = "AFTER_DEPENDENCIES";
//
///** Can be used, for example, */
//String JOIN_ON_KEEP_RUNNING = "LIFETIME_KEEP_RUNNING";
//
//String JOIN_ON_LIFETIME_COMPLETE = "LIFETIME_COMPLETE";

//
///**
// * Starts a new thread to run the given task.
// *
// * @return {@code true} if
// */
//// Synchronous before dependencies
//// Synchronous after dependencies
//// Asynchronous before dependencies, stop after dependencies
//// Asynchronous before dependencies, stop lifetime.complete
//// Asynchronous before dependencies, keep running
//// Asynchronous after dependencies, stop lifetime.complete
//// Asynchronous After dependencies, keep running
////// Before some string based "Event"
//
//// async =
//boolean fork() default false;
//
//// MethodReferences would just be fucking awesome
//// joinPolicy default JoinPolicy::immediately();
//
///**
// * Whether or not any thread will be interrupted if shutdown while starting
// *
// * @return
// */
//// Only if Async??? I think this only works on forked...
//// Because if on main thread this annotation is completely ignored
//// Unless we interrupt the main thread from another thread...
//// Interrupt on stop is part of fork
//boolean interruptOnStop() default false; // Maybe have an InterruptionPolicy {NEVER, DEFAULT, ALWAYS}

// order = "SomE:1"; (I forhold til andre der er bruger SomE
// eller maaske = "->Foo"; (Jeg released Foo)
// String[] after() default {};

// Problemet med at have baade after og before er at det er forskellige modeller.
// Hvis tilfoejer en before <- Saa virker den paa den maade at skal med en beforeX skal vaere faerdige inde vi kan
// begyndede...
// Maaske kun have starting points til at starte med???

// Maaske gaa over til XXX og *XXX
// XXX venter man paa en, *XXX venter paa alle er faerdige, flere end en kan complete den...

// OnStart()
// @StartPoint();<- fields and methods
// public CompletableFuture<>