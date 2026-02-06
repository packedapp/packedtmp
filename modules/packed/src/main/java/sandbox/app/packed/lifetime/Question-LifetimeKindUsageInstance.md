[Der er noget med Type, Usage, Instance]
Igen 23 Pods -> Som kan initiseres forskelligt, og hver har instanser
Hvis vi har 23 Pods, har 23 lifetime mirrors, Men de har samme "Type"
Ellers kan vi ikke aflaese afhandig
"Heh" Det betyder ogsaa at fx Agents, har vaere deres lifetime
 Men ved ikke om vi bare har en type Eller 3 som nu (LifetimeMirror, BeanLifetimeMirror, CompositeLifetimeMirror)
BeanLifetimeMirror vs LifetimeBeanMirror could also potentially be a bit confusing


Saa en lifetime ved vi maaske ikke engang om den er managed?
Kan vi vaere begge dele? Tror maaske ikke det giver mening at mixse.
If pods -> Kan ikke vaere begge dele
If Foreign -> Er det kun managed vi gemmer


Transient->Foreign Unmanaged har jo ikke engang et store... Vi skal ikke gemme noget

Unmanaged+Transient er jo faktisk ogsaa 2 forskellige ting
Saa Application kan baade vaere managed og unmanaged... dependending on the mood.
So the Type may be name agnostic

Men er typen knyttet op til beans?   

----------
Tror ikke en lifetime har et navn direkte? 
Maaske heller ikke et id
Men kan have en reference til en eller anden type

Tror til gaengaeld helt sikkert en lifetime er managed eller unmanaged
 
 