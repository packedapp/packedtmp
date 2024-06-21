// Two ways to a

// Link
// -- MyAssTran.preBuild
// ---- Link(Ass)
// ....
// - Assembly.Build

// Application???
// Container
// Bean
// Operation
// Binding???
//// WebNamespace

// Can be applied as "ClientProxy" <-- Maybe this is Augmentation

// Can be applied as @BeanHook
// Can be applied as @AssemblyHook

//Can be applied as AssemblyTransformer

// Questions
//// 1. Stateless  (+ Locals) vs Statefull Must
//// 2. Pre/post? More control.
//// 3. Context or protected methods?

// Hook -> Match + Transformer

// BuildTransformer???

// Would be nice to have a way to fx apply @Debug everywhere.
// A readonly transformer

// Ville måske være godt at kunne få info ned...
// Ellers må vi jo have oplysningerne på ContainerConfiguration og så tage den med.

// Det ville være rigtig fedt at kunne se hvem der havde transformeret hvad

// Det ville også være fint at have hvem og hvad seperaret

// We don't actually transforming anything just prepare it
// Would be nice to able
// I think we are returning an delegating assembly

// I think delegating assembly may allow hooks annotations. But must be open!!
// No maybe this is simply the way we support it for now..

// We need some cool examples.
// Like print everytime a bean is instantiated

// Maybe it is instrument\
// Maaske optrader det en synthetics delegated assembly

// Recursively er specielt.. ComponentTransformer??? Hmmm