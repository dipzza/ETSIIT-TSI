include "globals.mzn";

set of int: dig = 0..9;

% Cada letra debe ser un dígito de 0 a 9
var dig: A; var dig: D; var dig: E;
var dig: F; var dig: I; var dig: K;
var dig: N; var dig: R; var dig: S;
var dig: T;
array[1..5] of var int: acarreo;

% Deben ser diferentes y verificar la suma
constraint alldifferent([A, D, E, F, I, K, N, R, S, T]);
constraint E + E + E == E + 10 * acarreo[1];
constraint acarreo[1] + T + T + N == T + 10 * acarreo[2];
constraint acarreo[2] + S + S + I == F + 10 * acarreo[3];
constraint acarreo[3] + E + E + E == A + 10 * acarreo[4];
constraint acarreo[4] + T + F + D == R + 10 * acarreo[5];
constraint acarreo[5] == K;

solve satisfy;