install (non-void)
BeanClassTransformations() Arbejder kun paa BeanKlassen. Her kan du hide, introducere annotations, osv.
  Er primaert benyttet af brugere
-> ClassHooks Hooks (Her kan vi lave proxies og andre spaendende ting)  (Extension ejer disse)

BeanConfiguration klar (Og beslutninger omkring vi bruger en proxy eller ej er besluttet, maaske kan vi dog konfigure proxy settings)
transformers.onNew(BeanConfiguration

return BeanConfiguration to user


---
Custom hooks a transformer? Or an annotation