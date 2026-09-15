grammar Ambigua;

// Gramatica de la diapositiva 15, tal cual:
//   E -> E + E
//   E -> E * E
//   E -> num
//
// ANTLR 4 la acepta sin quejarse y no avisa que es ambigua. Al reescribir la
// recursion por la izquierda le da mas precedencia a la alternativa que
// aparece primero, asi que aqui '+' queda por encima de '*'.

inicio : e EOF ;

e : e '+' e
  | e '*' e
  | NUM
  ;

NUM : [0-9]+ ;
WS  : [ \t\r\n]+ -> skip ;
