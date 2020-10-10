include "globals.mzn";

set of int: hora = 9..12;
set of int: naula = 1..4;
set of int: ngrupo = 1..4;
set of int: nasig = 1..3;

array[nasig] of string: strAsig = ["IA","TSI","FBD"];
array[ngrupo] of string: strGrupo = ["G1","G2","G3","G4"];
array[nasig, ngrupo] of var naula: aula_sel;
array[nasig, ngrupo] of var hora: hora_sel;

% Un grupo no puede dar clase a la misma hora
constraint forall(grupo in ngrupo)(alldifferent([hora_sel[asig, grupo] | asig in nasig]));

% Un profesor no puede dar varias clases a la vez
constraint alldifferent([hora_sel[1,1], hora_sel[1,2], hora_sel[2,1], hora_sel[2,2]]);
constraint alldifferent([hora_sel[3,1], hora_sel[3,2]]);
constraint alldifferent([hora_sel[2,3], hora_sel[2,4], hora_sel[3,3], hora_sel[3,4]]);
constraint alldifferent([hora_sel[1,3], hora_sel[1,4]]);

% Un profesor no puede dar clase fuera de su horario
constraint hora_sel[3,1] != 10 /\ hora_sel[3,2] != 10;
constraint hora_sel[1,3] != 9 /\ hora_sel[1,4] != 9;

constraint forall(i in nasig, j in ngrupo)(forall(k in nasig, l in ngrupo)((i == k /\ j == l) \/ hora_sel[i, j] != hora_sel[k, l] \/ aula_sel[i, j] != aula_sel[k, l]));

solve satisfy;

output [strAsig[i] ++ "-" ++ strGrupo[j] ++ ": \(hora_sel[i, j]):00-\(hora_sel[i, j]+1):00 A\(aula_sel[i, j])\n" | i in nasig, j in ngrupo];