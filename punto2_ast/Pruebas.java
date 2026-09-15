import java.util.Objects;

/**
 * Pruebas del punto 2. Se corren con: java Pruebas (o pruebas.bat).
 *
 * No usa JUnit para no tener que descargar otra libreria: cada prueba compara
 * lo esperado con lo obtenido y al final se muestra el resumen.
 */
public class Pruebas {

    private static int pasaron = 0;
    private static int fallaron = 0;

    public static void main(String[] args) {
        parseTreeDeLaDiapositiva12();
        astDeLaDiapositiva13();
        elAstEsMasCompacto();
        parentesisRedundantesDanElMismoAst();
        parentesisQueCambianLaPrecedenciaCambianElAst();
        asociatividadIzquierdaEnElAst();
        multiplicacionALaIzquierda();
        soloParentesisQuedaUnNumero();
        identificadoresDiapositiva31();
        expresionInvalidaNoGeneraAst();

        System.out.println();
        System.out.println(pasaron + " pasaron, " + fallaron + " fallaron.");
        if (fallaron > 0) {
            System.exit(1);
        }
    }

    static void parseTreeDeLaDiapositiva12() {
        String esperado = String.join("\n",
                "E",
                "|-- E",
                "|   `-- T",
                "|       `-- F",
                "|           `-- 3",
                "|-- +",
                "`-- T",
                "    |-- T",
                "    |   `-- F",
                "    |       `-- 4",
                "    |-- *",
                "    `-- F",
                "        `-- 5");
        iguales("parse tree de la diapositiva 12", esperado,
                Arboles.dibujarParseTree(Main.construir("3 + 4 * 5").parseTree()));
    }

    static void astDeLaDiapositiva13() {
        Nodo ast = ast("3 + 4 * 5");
        iguales("AST de la diapositiva 13", op("+", num("3"), op("*", num("4"), num("5"))), ast);
        iguales("dibujo del AST de la diapositiva 13",
                String.join("\n", "+", "|-- 3", "`-- *", "    |-- 4", "    `-- 5"),
                Arboles.dibujarAst(ast));
    }

    static void elAstEsMasCompacto() {
        Main.Resultado r = Main.construir("3 + 4 * 5");
        iguales("el parse tree tiene 13 nodos", 13, Arboles.nodosParseTree(r.parseTree()));
        iguales("el AST tiene 5 nodos", 5, Arboles.nodosAst(r.ast()));
    }

    static void parentesisRedundantesDanElMismoAst() {
        Main.Resultado sin = Main.construir("3 + 4 * 5");
        Main.Resultado con = Main.construir("3 + (4 * 5)");
        verificar("con parentesis de sobra el parse tree cambia",
                  Arboles.nodosParseTree(sin.parseTree()) != Arboles.nodosParseTree(con.parseTree()));
        iguales("con parentesis de sobra el AST es el mismo", sin.ast(), con.ast());
    }

    static void parentesisQueCambianLaPrecedenciaCambianElAst() {
        iguales("(3 + 4) * 5 deja el * en la raiz",
                op("*", op("+", num("3"), num("4")), num("5")), ast("(3 + 4) * 5"));
    }

    static void asociatividadIzquierdaEnElAst() {
        iguales("3 - 4 - 5 agrupa por la izquierda",
                op("-", op("-", num("3"), num("4")), num("5")), ast("3 - 4 - 5"));
        iguales("3 - (4 - 5) agrupa por la derecha",
                op("-", num("3"), op("-", num("4"), num("5"))), ast("3 - (4 - 5)"));
    }

    static void multiplicacionALaIzquierda() {
        iguales("3 * 4 + 5 deja el + en la raiz",
                op("+", op("*", num("3"), num("4")), num("5")), ast("3 * 4 + 5"));
    }

    static void soloParentesisQuedaUnNumero() {
        iguales("((3)) queda como un solo numero", num("3"), ast("((3))"));
    }

    static void identificadoresDiapositiva31() {
        iguales("a + b * c de la diapositiva 31",
                op("+", new Nodo.Id("a"), op("*", new Nodo.Id("b"), new Nodo.Id("c"))),
                ast("a + b * c"));
    }

    static void expresionInvalidaNoGeneraAst() {
        Main.Resultado r = Main.construir("3 + * 5");
        verificar("una expresion invalida no genera AST", r.ast() == null && !r.errores().isEmpty());
    }

    // ---- utilidades ----

    static Nodo ast(String texto) {
        return Main.construir(texto).ast();
    }

    static Nodo num(String valor) {
        return new Nodo.Num(valor);
    }

    static Nodo op(String op, Nodo izq, Nodo der) {
        return new Nodo.OpBin(op, izq, der);
    }

    static void verificar(String nombre, boolean condicion) {
        if (condicion) {
            pasaron++;
            System.out.println("ok     " + nombre);
        } else {
            fallaron++;
            System.out.println("FALLA  " + nombre);
        }
    }

    static void iguales(String nombre, Object esperado, Object obtenido) {
        verificar(nombre, Objects.equals(esperado, obtenido));
        if (!Objects.equals(esperado, obtenido)) {
            System.out.println("       esperado: " + esperado);
            System.out.println("       obtenido: " + obtenido);
        }
    }
}
