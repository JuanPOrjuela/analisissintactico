grammar Expr;

// Gramatica de las diapositivas 11 y 12:
//   E -> E + T | T
//   T -> T * F | F
//   F -> id | num | ( E )
//
// Se agrega E - T para poder ver en el AST como asocia la resta.

// Punto de entrada: una expresion completa. El EOF obliga a consumir toda la
// entrada; sin el, "3 + 4 )" se aceptaria ignorando lo que sobra.
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
