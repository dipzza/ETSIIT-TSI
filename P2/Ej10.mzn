include "globals.mzn";

set of int: nobjetos = 1..12;

% Datos del problema
array[nobjetos] of int: pesos = [9, 13, 153, 50, 15, 68, 27, 39, 23, 52, 11, 32];
array[nobjetos] of 0..200: preferencia = [150, 35, 200, 160, 60, 45, 60, 40, 30, 10, 70, 30];
% Array binario en la que un 0 indica que la posición i no se selecciona y 1 que sí.
array[nobjetos] of var 0..1: seleccion;
var int: preferencia_obtenida;

% El peso de los elementos seleccionados debe ser menor al peso máximo
constraint sum([pesos[i] * seleccion[i] | i in nobjetos]) <= 275;
constraint preferencia_obtenida == sum([preferencia[i] * seleccion[i] | i in nobjetos]);
% Queremos maximizar la preferencia de los elementos seleccionados
solve maximize preferencia_obtenida;