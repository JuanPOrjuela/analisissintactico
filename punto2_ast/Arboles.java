import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Dibuja y cuenta los nodos del parse tree y del AST. */
public class Arboles {

    // Nombres cortos para que el parse tree se vea como en la diapositiva 12
    private static final Map<String, String> NOMBRES =
            Map.of("expr", "E", "term", "T", "factor", "F");

    public static String dibujarParseTree(ParseTree arbol) {
        return dibujar(arbol, Arboles::etiquetaParseTree, Arboles::hijosParseTree);
    }

    public static String dibujarAst(Nodo ast) {
        return dibujar(ast, Arboles::etiquetaAst, Arboles::hijosAst);
    }

    public static int nodosParseTree(ParseTree arbol) {
        return contar(arbol, Arboles::hijosParseTree);
    }

    public static int nodosAst(Nodo ast) {
        return contar(ast, Arboles::hijosAst);
    }

    /** El AST en una linea, operador primero: (+ 3 (* 4 5)). */
    public static String prefija(Nodo nodo) {
        if (nodo instanceof Nodo.OpBin op) {
            return "(" + op.op() + " " + prefija(op.izq()) + " " + prefija(op.der()) + ")";
        }
        return etiquetaAst(nodo);
    }

    private static String etiquetaParseTree(ParseTree nodo) {
        if (nodo instanceof TerminalNode) {
            return nodo.getText();
        }
        int regla = ((RuleContext) nodo).getRuleIndex();
        return NOMBRES.get(ExprParser.ruleNames[regla]);
    }

    private static List<ParseTree> hijosParseTree(ParseTree nodo) {
        List<ParseTree> hijos = new ArrayList<>();
        for (int i = 0; i < nodo.getChildCount(); i++) {
            hijos.add(nodo.getChild(i));
        }
        return hijos;
    }

    private static String etiquetaAst(Nodo nodo) {
        if (nodo instanceof Nodo.OpBin op) {
            return op.op();
        }
        if (nodo instanceof Nodo.Num num) {
            return num.valor();
        }
        return ((Nodo.Id) nodo).nombre();
    }

    private static List<Nodo> hijosAst(Nodo nodo) {
        if (nodo instanceof Nodo.OpBin op) {
            return List.of(op.izq(), op.der());
        }
        return List.of();
    }

    // Se dibuja con caracteres ASCII porque la consola de Windows no siempre
    // muestra bien los caracteres de caja (├ └ │)
    private static <T> String dibujar(T raiz, Function<T, String> etiqueta,
                                      Function<T, List<T>> hijos) {
        StringBuilder dibujo = new StringBuilder(etiqueta.apply(raiz));
        dibujarHijos(raiz, "", etiqueta, hijos, dibujo);
        return dibujo.toString();
    }

    private static <T> void dibujarHijos(T nodo, String prefijo, Function<T, String> etiqueta,
                                         Function<T, List<T>> hijos, StringBuilder dibujo) {
        List<T> lista = hijos.apply(nodo);
        for (int i = 0; i < lista.size(); i++) {
            boolean ultimo = i == lista.size() - 1;
            T hijo = lista.get(i);
            dibujo.append('\n').append(prefijo).append(ultimo ? "`-- " : "|-- ")
                  .append(etiqueta.apply(hijo));
            dibujarHijos(hijo, prefijo + (ultimo ? "    " : "|   "), etiqueta, hijos, dibujo);
        }
    }

    private static <T> int contar(T nodo, Function<T, List<T>> hijos) {
        int total = 1;
        for (T hijo : hijos.apply(nodo)) {
            total += contar(hijo, hijos);
        }
        return total;
    }
}
