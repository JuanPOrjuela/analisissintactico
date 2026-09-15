# Análisis sintáctico con ANTLR

Esta es la práctica del tema de análisis sintáctico (presentación `04_LP`). Son tres puntos y cada uno está en su carpeta:

| Carpeta | Qué hace | Lenguaje |
|---|---|---|
| `punto1_expresiones` | Gramática de la diapositiva 11, con pruebas | Python |
| `punto2_ast` | Comparación entre el parse tree y el AST (diapositivas 12 y 13) | Java |
| `punto3_ambigua` | Gramática de la diapositiva 15 y prueba de si es ambigua | Java |

El punto 1 lo hice en Python porque así lo pide el enunciado; los otros dos los hice en Java. El jar de ANTLR está en `lib/`, así que no hay que descargarlo aparte.

## Qué se necesita

- **Java 17**. Sirve para generar los parsers con ANTLR y para correr los puntos 2 y 3.
- **Python 3** con el runtime de ANTLR, solo para el punto 1:

```powershell
pip install antlr4-python3-runtime==4.13.2
```

Los `.bat` usan el Java de `JAVA_HOME` si esa variable existe. Para los comandos a mano, si `java -version` muestra una versión menor a 17, antes hay que correr esto en la misma ventana:

```powershell
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

En los tres puntos el programa funciona igual. Sin argumentos abre el modo consola, donde se puede escribir una expresión, la ruta de un `.txt` o `salir`. Si se le pasa un archivo, toma cada línea como una expresión.

## Cómo correrlo con los .bat

Los scripts se llaman igual en las tres carpetas. Desde PowerShell, dentro de la carpeta del punto:

```powershell
.\construir.bat              # genera el parser (y en Java además compila)
.\ejecutar.bat               # modo consola
.\ejecutar.bat ejemplos.txt  # analiza el archivo de ejemplos
.\pruebas.bat                # corre las pruebas
```

## Cómo correrlo con los comandos

**Punto 1 (Python)**

```powershell
cd punto1_expresiones
java -jar ..\lib\antlr-4.13.2-complete.jar -Dlanguage=Python3 Expr.g4
python main.py ejemplos.txt
python pruebas.py
```

**Punto 2 (Java)**

```powershell
cd punto2_ast
java -jar ..\lib\antlr-4.13.2-complete.jar -visitor -no-listener -o gen Expr.g4
javac -encoding UTF-8 -cp ..\lib\antlr-4.13.2-complete.jar -d build gen\*.java *.java
java -cp "build;..\lib\antlr-4.13.2-complete.jar" Main ejemplos.txt
java -cp "build;..\lib\antlr-4.13.2-complete.jar" Pruebas
```

**Punto 3 (Java)**

```powershell
cd punto3_ambigua
java -jar ..\lib\antlr-4.13.2-complete.jar -no-listener -o gen Ambigua.g4 AmbiguaInvertida.g4
javac -encoding UTF-8 -cp ..\lib\antlr-4.13.2-complete.jar -d build gen\*.java *.java
java -cp "build;..\lib\antlr-4.13.2-complete.jar" Main ejemplos.txt
java -cp "build;..\lib\antlr-4.13.2-complete.jar" Pruebas
```

Para abrir el modo consola se usa el mismo comando sin `ejemplos.txt`. Lo que genera ANTLR y los `.class` no están en el repositorio; se crean con estos comandos o con `construir.bat`.

## Punto 1: gramática de la diapositiva 11

```
E -> E + T | T
T -> T * F | F
F -> id | num | (E)
```

El programa dice si la expresión es aceptada y muestra el árbol, o si es rechazada y en qué columna está el error. Le agregué la resta (`E -> E - T`) porque los ejemplos de la diapositiva la usan aunque la regla no la tenga.

En las pruebas revisé los tres ejemplos de la diapositiva y varias expresiones inválidas, como `2 + * 3`, `(2 + 3` o `2 (3 * 4)`. También revisé que la multiplicación tenga más precedencia que la suma y que la suma y la resta agrupen por la izquierda.

## Punto 2: formas de AST

Usé la misma gramática, pero con un **Visitor** (`ConstructorAST.java`) que convierte el parse tree en un AST. Para cada expresión se imprimen los dos árboles dibujados, cuántos nodos tiene cada uno y el AST en una línea, por ejemplo `(+ 3 (* 4 5))`.

Lo que pude comprobar:

- Para `3 + 4 * 5` el parse tree tiene 13 nodos y sale igual al de la diapositiva 12. El AST tiene 5 nodos y sale igual al de la 13.
- `3 + 4 * 5` y `3 + (4 * 5)` tienen parse trees distintos (el segundo tiene 18 nodos por los paréntesis), pero **el mismo AST**. Los paréntesis no pasan al AST porque la agrupación ya se ve en la forma del árbol.
- `(3 + 4) * 5` sí cambia el AST, porque el `*` pasa a la raíz.
- `3 - 4 - 5` da `(- (- 3 4) 5)` y `3 - (4 - 5)` da `(- 3 (- 4 5))`, así que la asociatividad también se nota en el AST.
- `((3))` queda como un solo nodo: `3`.

Con los 10 ejemplos del archivo salen 9 formas distintas de AST.

## Punto 3: gramática ambigua de la diapositiva 15

```
E -> E + E
E -> E * E
E -> num
```

`Ambigua.g4` es la gramática tal cual. `AmbiguaInvertida.g4` tiene exactamente las mismas producciones, pero con `E * E` primero.

Lo más interesante fue que **ANTLR no avisa que la gramática es ambigua**. La acepta sin problema y siempre arma un solo árbol. Lo que hace es darle más precedencia a la alternativa que está escrita primero. Por eso lo probé de tres formas:

1. **Contando los árboles de derivación.** En `Main.java` hay un método que genera todos los árboles posibles de la gramática. Para `2 + 3 * 4` salen dos, `(2 + 3) * 4 = 20` y `2 + (3 * 4) = 14`, igual que en las diapositivas 16 y 17. Incluso `1 + 2 + 3` tiene dos árboles, aunque den el mismo resultado. Con más operadores la cantidad crece rápido: 1, 2, 5, 14, 42...
2. **Cambiando el orden de las reglas.** Con el orden de la diapositiva, ANTLR da `(2 + 3) * 4 = 20`, o sea que le da más precedencia a la suma, al revés de lo normal. Con el orden invertido da `2 + (3 * 4) = 14`. Si con solo reordenar las producciones cambia el árbol, es porque la gramática permite más de uno.
3. **Con el modo de detección de ambigüedades de ANTLR** (`LL_EXACT_AMBIG_DETECTION`). Reporta 0 avisos. Para confirmar que el detector sí funcionaba, lo probé aparte con una gramática ambigua sencilla sin recursión (`x : 'a' y | 'a' z`), y ahí sí reportó la ambigüedad. El problema es que ANTLR reescribe la recursión por la izquierda y resuelve el conflicto por el orden antes de que se note.

**Conclusión:** la gramática de la diapositiva 15 es ambigua. No se nota al correrla en ANTLR, porque ANTLR elige un árbol por su cuenta. Para evitarlo hay que escribir la precedencia en la gramática, como en la del punto 1 (con E, T y F).
