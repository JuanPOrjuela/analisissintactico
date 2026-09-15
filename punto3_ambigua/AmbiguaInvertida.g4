grammar AmbiguaInvertida;

// Las mismas producciones de Ambigua.g4, solo que con E * E primero.
//
// En una gramatica libre de contexto el orden de las producciones no importa:
// las dos definen exactamente el mismo lenguaje. Si con solo reordenarlas ANTLR
// arma un arbol distinto para la misma cadena, es porque la gramatica permite
// mas de un arbol, o sea, es ambigua.

inicio : e EOF ;

e : e '*' e
  | e '+' e
  | NUM
  ;

NUM : [0-9]+ ;
WS  : [ \t\r\n]+ -> skip ;
