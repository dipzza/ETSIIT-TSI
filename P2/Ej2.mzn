include "globals.mzn";

set of int: dig = 0..9;

% Número x con cada componente siendo un dígito del 0 al 9
array[0..9] of var dig: x;

% Restricción para que cada dígito represente el número de Xi en X
constraint forall(i in 0..9)(count(x, i) == x[i]);

solve satisfy;