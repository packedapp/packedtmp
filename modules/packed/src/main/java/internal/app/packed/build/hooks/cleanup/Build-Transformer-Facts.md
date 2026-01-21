============== Filtering
Filtering is always programatically. Otherwise we need to invent a lot of stuff, and maybe a "descriptor".
We also have it in code when we debug debugging

if (bean.isAnnotatedWith(Entity.class)) {
  bean.apply(
}

All transformers is basically just a list
BeanTransformer[DB] - [ddood.class, ]

Map<Class, List<Class>> <-- Tags + Recursively

============== ComponentSpecTree

Application
  Assembly
    Container
      Extension
      Bean (hmm, kan jo applies til extension ogsaa vel???? 
        Operation
      