import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Punto 2: parse tree vs. AST para la gramatica de la diapositiva 12.
 *
 * Para cada expresion muestra el parse tree (todas las reglas de la gramatica)
 * y el AST que arma el Visitor, para comparar las distintas formas que toma.
 *
 * Uso:
 *   java Main               modo consola
 *   java Main ejemplos.txt  analiza un archivo (una expresion por linea)
 */
public class Main {

    /** Marca invisible que algunos programas ponen al inicio del texto UTF-8. */
    static final String BOM = String.valueOf((char) 0xFEFF);

    /** Guarda los errores del lexer y del parser en vez de imprimirlos sueltos. */
    static class ColectorErrores extends BaseErrorListener {
        final List<String> errores = new ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> r, Object simbolo, int linea,
                                int columna, String msg, RecognitionException e) {
            errores.add("columna " + (columna + 1) + ": " + msg);
        }
    }

    /** Resultado de analizar una expresion. Los arboles son null si hubo errores. */
    record Resultado(ExprParser.ExprContext parseTree, Nodo ast, List<String> errores) {}

    static Resultado construir(String texto) {
        ColectorErrores colector = new ColectorErrores();

        ExprLexer lexer = new ExprLexer(CharStreams.fromString(texto));
        lexer.removeErrorListeners();
        lexer.addErrorListener(colector);

        ExprParser parser = new ExprParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(colector);

        ExprParser.InicioContext arbol = parser.inicio();

        // Con errores el arbol tiene nodos que ANTLR invento; no se construye el AST
        if (!colector.errores.isEmpty()) {
            return new Resultado(null, null, colector.errores);
        }
        return new Resultado(arbol.expr(), new ConstructorAST().visit(arbol), List.of());
    }

    /** Imprime los dos arboles. Devuelve el AST en notacion prefija, o null si se rechaza. */
    static String mostrar(String texto) {
        Resultado r = construir(texto);
        if (r.ast() == null) {
            System.out.println(texto + "  ->  RECHAZADA");
            for (String error : r.errores()) {
                System.out.println("    " + error);
            }
            System.out.println();
            return null;
        }

        System.out.println(texto);
        System.out.println("  Parse tree (" + nodos(Arboles.nodosParseTree(r.parseTree())) + "):");
        System.out.println(sangria(Arboles.dibujarParseTree(r.parseTree())));
        System.out.println("  AST (" + nodos(Arboles.nodosAst(r.ast())) + "):");
        System.out.println(sangria(Arboles.dibujarAst(r.ast())));
        System.out.println("  AST en una linea: " + Arboles.prefija(r.ast()));
        System.out.println();
        return Arboles.prefija(r.ast());
    }

    static String nodos(int cantidad) {
        return cantidad == 1 ? "1 nodo" : cantidad + " nodos";
    }

    static String sangria(String dibujo) {
        return "    " + dibujo.replace("\n", "\n    ");
    }

    /** Cada linea no vacia del archivo es una expresion. */
    static void analizarArchivo(String ruta) {
        List<String> lineas;
        try {
            lineas = Files.readAllLines(Paths.get(ruta), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("No se pudo leer '" + ruta + "': " + e);
            return;
        }

        System.out.println("== " + ruta + " ==");
        System.out.println();

        // Se agrupan las expresiones por AST para ver cuales comparten la misma forma
        Map<String, List<String>> expresionesPorAst = new LinkedHashMap<>();
        for (String linea : lineas) {
            linea = linea.replace(BOM, "").trim();
            if (linea.isEmpty()) {
                continue;
            }
            String ast = mostrar(linea);
            if (ast != null) {
                expresionesPorAst.computeIfAbsent(ast, k -> new ArrayList<>()).add(linea);
            }
        }

        int total = 0;
        for (List<String> grupo : expresionesPorAst.values()) {
            total += grupo.size();
        }
        System.out.println(total + " expresiones aceptadas, "
                           + expresionesPorAst.size() + " formas distintas de AST.");
        for (Map.Entry<String, List<String>> grupo : expresionesPorAst.entrySet()) {
            if (grupo.getValue().size() > 1) {
                System.out.println("  Mismo AST " + grupo.getKey() + ": "
                                   + String.join("  |  ", grupo.getValue()));
            }
        }
        System.out.println();
    }

    static void modoConsola() {
        System.out.println("Escribe una expresion, la ruta de un archivo .txt, o 'salir'.");
        Scanner entrada = new Scanner(System.in, "UTF-8");

        while (true) {
            System.out.print("> ");
            if (!entrada.hasNextLine()) {
                break;  // fin de la entrada (Ctrl+Z o texto redirigido)
            }
            // PowerShell antepone un BOM al redirigir texto con |
            String linea = entrada.nextLine().replace(BOM, "").trim();

            if (linea.isEmpty()) {
                continue;
            }
            if (linea.equalsIgnoreCase("salir")) {
                break;
            }

            // Al arrastrar un archivo a la terminal de Windows se pega entre comillas
            String ruta = linea.replaceAll("^\"|\"$", "");
            if (ruta.toLowerCase().endsWith(".txt")) {
                analizarArchivo(ruta);
            } else {
                mostrar(linea);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            for (String ruta : args) {
                analizarArchivo(ruta);
            }
        } else {
            modoConsola();
        }
    }
}
