include "globals.mzn";

set of int: hora = {8, 9, 10, 12, 13};
set of int: ndias = 1..5;
set of int: nasig = 1..16;

set of int: bloques2h = {1, 3, 4, 5, 8, 10, 12, 13, 14};

array[nasig] of var ndias: dia_sel;
array[nasig] of var hora: hora_sel;
array[nasig] of string: strAsig = ["A1","A2","A3","A4","A5","A6","A7","A8","A9","A1","A2","A3","A4","A5","A6","A7"];

% No pueden existir clases a la misma hora el mismo dia
constraint forall(i, j in nasig where i < j)(hora_sel[i] != hora_sel[j] \/ dia_sel[i] != dia_sel[j]);

% Bloques de una misma asignatura no pueden darse en el mismo dia
constraint forall(i in 1..7)(dia_sel[i] != dia_sel[9+i]);

% Los bloques de 2h necesitan un espacio de 2h (teniendo en cuenta el recreo)
constraint forall(j in bloques2h)(hora_sel[j] < 13 /\ hora_sel[j] != 10);
constraint forall(j in bloques2h, i in nasig where i != j)(dia_sel[j] != dia_sel[i] \/ hora_sel[i] >= hora_sel[j] + 2 \/ hora_sel[i] < hora_sel[j]);

% Bloques de un mismo profesor(excepto prof4) no pueden darse en el mismo dia
constraint dia_sel[1] != dia_sel[3];
constraint dia_sel[4] != dia_sel[5];
constraint dia_sel[6] != dia_sel[9];

solve satisfy;