include "globals.mzn";

set of int: hora = 0..23;

% Vector solución con la hora en la que da clase cada profesor
array[1..6] of var hora: prof;

% Las clases tienen que empezar todas en una hora distinta, y respetar
% los horarios de cada profesor
constraint alldifferent(prof);
constraint prof[1] >= 11 /\ prof[1] < 15;
constraint prof[2] >= 11 /\ prof[2] < 13;
constraint prof[3] >= 10 /\ prof[3] < 14;
constraint prof[4] >= 10 /\ prof[4] < 13;
constraint prof[5] >= 11 /\ prof[5] < 13;
constraint prof[6] >= 9 /\ prof[6] < 15;

solve satisfy;