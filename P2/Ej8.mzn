include "globals.mzn";

set of int: ntareas = 1..9;

array[ntareas] of int: duracion = [7, 3, 1, 8, 2, 1, 1, 3, 2];
array[ntareas] of int: trabajadores_necesarios = [2, 3, 2, 2, 1, 2, 1, 1, 2];
array[ntareas] of var int: tiempo_inicio;
array[ntareas] of var int: tiempo_fin;
var int: duracion_total;

% El tiempo de fin debe ser el tiempo de inicio más la duración de la tarea.
constraint forall(i in ntareas)(tiempo_fin[i] == tiempo_inicio[i] + duracion[i]);
% Los tiempos deben ser mayores a 0
constraint forall(i in ntareas)(tiempo_inicio[i] >= 0 /\ tiempo_fin[i] >= 0);

% Establecemos las restricciones de precedencia
constraint tiempo_inicio[2] >= tiempo_fin[1];
constraint tiempo_inicio[3] >= tiempo_fin[2];
constraint tiempo_inicio[4] >= tiempo_fin[1];
constraint tiempo_inicio[5] >= tiempo_fin[3] /\ tiempo_inicio[5] >= tiempo_fin[4];
constraint tiempo_inicio[6] >= tiempo_fin[3] /\ tiempo_inicio[6] >= tiempo_fin[4];
constraint tiempo_inicio[7] >= tiempo_fin[3] /\ tiempo_inicio[7] >= tiempo_fin[4];
constraint tiempo_inicio[8] >= tiempo_fin[1];
constraint tiempo_inicio[9] >= tiempo_fin[6] /\ tiempo_inicio[9] >= tiempo_fin[8];

constraint duracion_total == max(tiempo_fin);

% Minimizamos cuando se terminan de realizar todas las tareas
solve minimize duracion_total;