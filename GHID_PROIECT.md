# Proiect DCS — controlul fluxului urban cu retele Petri fuzzy

## Ce cere poza

Sistemul are doua parti: controllerul decide culorile semafoarelor, iar Plant modeleaza fluxul prin patru benzi de intrare, intersectie si patru benzi de iesire.

- Intrare: `P_i -> t_in -> Lane -> t_send -> Ready -> t_out -> P_c`.
- `t_in` si `t_send` folosesc regulile FLRS de identitate: NL->NL, NM->NM, ZR->ZR, PM->PM, PL->PL.
- `t_out` permite trecerea numai la verde. Semnalul este fuzzy: rosu=-1, verde=0, galben=+1. Zero este un token valid, nu lipsa tokenului.
- Intersectia combina P_c1/P_c2 si P_c3/P_c4, apoi combina cele doua rezultate. Tabelul FLRS este adunarea saturata desenata in poza.
- `t_split` inmulteste rezultatul cu constante fuzzy, folosind `Prod_Fuzzy`.
- Iesire: `P_o -> t_exit -> P_Exit`, cu FLRS de identitate.

## Implementarea adaugata

Surse: `All_Petri_Nets_Framework_4_IntelliJ/All_Petri_FW/All_Petri_FW/src/Fuzzy_UVS/`.

`Controller.java` asambleaza modelele din fisiere separate: `Lane1.java` ... `Lane4.java`, fiecare continand intrarea si iesirea aceleiasi benzi, `Intersection.java` si `Controller.java`. `TrafficSupport.java` contine operatiile comune. Benzile sunt module ale aceluiasi Plant, nu procese separate. Sistemul construieste doua retele: Plant pe portul **1090**, controllerul pe **1091**. Controllerul trimite semnale `DataFuzzy` prin `DataTransfer` si `SendOverNetwork` catre `P_TL1` ... `P_TL4` din Plant.

Pentru inceput, benzile si intersectia sunt in aceeasi retea Plant. Transferul dintre acestea este local; schema din poza propune si separarea lor in procese cu transfer prin retea. Acea separare nu este implementata in aceasta versiune.

Controllerul foloseste ciclul G1,Y1,G2,Y2,G3,Y3,G4,Y4; celelalte trei semafoare sunt rosii. Fiecare faza dureaza nominal 5 secunde. Este o alegere de proiectare conservatoare pentru a permite si viraje; poza nu precizeaza ordinea fazelor. Controllerul este temporizat, nu adapteaza durata la trafic. Trimiterea catre un supervisor apare in poza ca „not yet” si nu este implementata.

In framework, o tranzitie sincronizata cu `Delay=4` si `pn.Delay=1000` se executa la al cincilea tick dupa activarea starii. Temporizarea depinde de executia Java si nu este o garantie de timp real strict.

## Rulare in IntelliJ

1. Deschide proiectul Maven din directorul `All_Petri_FW/All_Petri_FW` (cel care contine `pom.xml`). Foloseste JDK 11 sau mai nou.
2. Ruleaza `Fuzzy_UVS.Controller` cu **Program arguments: `demo`**. Se deschide Plant, preincarcat cu 0.1 pe fiecare intrare.
3. Apasa **Start** in fereastra Plant.
4. Ruleaza aceeasi clasa intr-o a doua configuratie cu **Program arguments: `controller`**. Apasa **Start** in fereastra controllerului. Plant trebuie pornit primul pentru a primi mesajele.
5. Urmareste `P_TL1` ... `P_TL4`, apoi `P_c1` ... `P_c4`, `Pair12`, `Pair34`, `P_I`, `P_o1` ... `P_o4` si `P_Exit1` ... `P_Exit4`.

Modul `plant` porneste cu intrarile goale. Pentru fluxuri noi, foloseste clasa existenta `GUIs.InputFuzzy`: port **1090**, loc **P_i1**, **P_i2**, **P_i3** sau **P_i4** si valoare intre 0 si 1. Trimite o singura valoare pe loc liber; receptorul frameworkului inlocuieste valoarea existenta, nu creeaza o coada.

Modelul intersectiei din poza sincronizeaza perechi de intrari. Pentru un lot complet trebuie sa furnizezi valori pentru toate cele patru benzi; pentru o banda fara trafic trimite explicit **0**. Daca nu trimiti token, tranzitia asteapta. Demo reprezinta un singur lot, nu un generator continuu de masini.

## Neconcordante si precizie

Ponderile scrise in poza sunt 0.25, 0.50, 0.12 si 0.12. Suma este **0.99**, desi dedesubt apare „Total=1”. Codul pastreaza cifrele din poza. Pentru conservarea exacta a sumei trebuie clarificat daca ultima pondere ar trebui sa fie 0.13.

FLRS limiteaza reprezentarea la domeniul [-1,1] si rotunjeste rezultatele inferentei la doua zecimale. Astfel, combinarea nu este o adunare nelimitata a numarului de masini. Valorile sunt fluxuri normalizate. Dupa `Prod_Fuzzy`, 0.4*0.12=0.048; dupa FLRS-ul de iesire, valoarea devine 0.05.

## Verificare efectuata

`Fuzzy_UVS.TrafficSystemCheck` ruleaza fara ferestre si verifica blocarea la rosu/galben, trecerea la verde, pastrarea semnalului, combinarea a patru intrari de 0.1 in 0.4, cele patru produse fuzzy, iesirile si un al doilea lot cu valori zero. Verifica si configurarea intarzierilor controllerului.

Compilarea cu JDK 11 si aceste verificari au trecut. Functionarea ferestrelor, transmisia reala intre procese si durata masurata a fazelor necesita verificare prin rularea demo de mai sus; nu au fost validate automat.

Clasele originale din `DCS_UVS` au ramas nemodificate; acelea folosesc masini individuale si semnale text si sunt exemple pentru alt tip de model.
