include "globals.mzn";

set of int: ncasas = 1..5;
set of int: ncarac = 1..5;

% Casas 1 - primera a la izquierda .. 5 - última a la derecha
% 1. 1 - Andaluz, 2 - Catalán, 3 - Gallego, 4 - Navarro, 5 - Vasco
% 2. 1 - Roja, 2 - Azul, 3 - Verde, 4 - Amarilla, 5 - blanca
% 3. 1 - Perro, 2 - Caracoles, 3 - Zorro, 4 - Caballo, 5 - Cebra
% 4. 1 - Pintor, 2 - Escultor, 3 - Diplomático, 4 - Violinista, 5 - Médico
% 5. 1 - Té, 2 - Café, 3 - Leche, 4 - Zumo, 5 - Agua

array[ncasas, ncarac] of var ncarac: calle;
array[ncarac] of string: strRegion = ["Andaluz", "Catalán", "Gallego", "Navarro", "Vasco"];
var ncasas: casa_cebra;
var ncarac: reg_agua;

constraint forall(i in ncarac)(alldifferent([calle[j, i] | j in ncasas]));

% Vasco en casa roja
constraint exists(i in ncasas)(calle[i, 1] == 5 /\ calle[i, 2] == 1);
% Catalán con perro
constraint exists(i in ncasas)(calle[i, 1] == 2 /\ calle[i, 3] == 1);
% Gallego es pintor
constraint exists(i in ncasas)(calle[i, 1] == 3 /\ calle[i, 4] == 1);
% Navarro bebe té
constraint exists(i in ncasas)(calle[i, 1] == 4 /\ calle[i, 5] == 1);
% Andaluz en primera casa
constraint calle[1, 1] == 1;
% Casa verde bebe café
constraint exists(i in ncasas)(calle[i, 2] == 3 /\ calle[i, 5] == 2);
% Casa verde a la derecha de la blanca
constraint exists(i in ncasas)(calle[i, 2] == 3 /\ calle[i-1, 2] == 5);
% Escultor cría caracoles
constraint exists(i in ncasas)(calle[i, 4] == 2 /\ calle[i, 3] == 2);
% Diplomático en casa amarilla.
constraint exists(i in ncasas)(calle[i, 4] == 3 /\ calle[i, 2] == 4);
% Casa 3 bebe leche
constraint calle[3, 5] == 3;
% Casa del andaluz al lado de la azul
constraint exists(i in ncasas)(calle[i, 1] == 1 /\ (calle[i-1, 2] == 2 \/ calle[i+1, 2] == 2));
% Violinista bebe zumo
constraint exists(i in ncasas)(calle[i, 4] == 4 /\ calle[i, 5] == 4);
% Zorro al lado de casa del médico
constraint exists(i in ncasas)(calle[i, 3] == 3 /\ (calle[i-1, 4] == 5 \/ calle[i+1, 4] == 5));
% Caballo al lado de casa del diplomático
constraint exists(i in ncasas)(calle[i, 3] == 4 /\ (calle[i-1, 4] == 3 \/ calle[i+1, 4] == 3));

solve satisfy;

output [
  if fix(calle[i, 3]) == 5 then
    "\nCebra en casa \(i) con el " ++ strRegion[fix(calle[i, 1])]
  else
    ""
  endif
  ++
  if fix(calle[i, 5]) == 5 then
    "\nBebe agua el " ++ strRegion[fix(calle[i, 1])]
  else
    ""
  endif

  | i in ncasas
];