grammar Expr;

// Gramatica de la diapositiva 11:
//   E -> E + T | T
//   T -> T * F | F
//   F -> id | num | ( E )
//
// Se agrega E - T porque los ejemplos (2 + 3 - 4, 2 + 3 * (4 - 5)) usan resta
// aunque la regla escrita solo muestre la suma. ANTLR 4 admite recursion
// izquierda directa, asi que las reglas se escriben igual que en la teoria.

// Punto de entrada: una expresion completa. El EOF obliga a consumir toda la
// entrada; sin el, "2 + 3 )" se aceptaria ignorando lo que sobra.
inicio : expr EOF ;

// E
expr : expr '+' term
     | expr '-' term
     | term
     ;

// T
term : term '*' factor
     | factor
     ;

// F
factor : ID
       | NUM
       | '(' expr ')'
       ;

ID  : [a-zA-Z_] [a-zA-Z_0-9]* ;
NUM : [0-9]+ ('.' [0-9]+)? ;
WS  : [ \t\r\n]+ -> skip ;
