import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Scanner;

/**
 * Punto 3: la gramatica de la diapositiva 15 en ANTLR y la prueba de ambiguedad.
 *
 * Para cada cadena se muestra:
 *   - todos los arboles de derivacion que permite la gramatica,
 *   - el arbol que elige ANTLR con el orden de la diapositiva y con el orden invertido,
 *   - cuantas ambiguedades reporto ANTLR con su modo de deteccion.
 *
 * Uso:
 *   java Main               modo consola
 *   java Main ejemplos.txt  analiza un archivo (una expresion por linea)
 */
public class Main {

    /** Marca invisible que algunos programas ponen al inicio del texto UTF-8. */
    static final String BOM = String.valueOf((char) 0xFEFF);

    /** Con mas operadores la cantidad de arboles se dispara (8 operadores ya son 1430). */
    static final int MAX_OPERADORES = 8;

    /** Cuantos arboles se imprimen como maximo por cadena. */
    static final int MAX_MOSTRAR = 10;

    /** Junta los errores de sintaxis y cuenta los avisos de ambiguedad de ANTLR. */
    static class Oyente extends BaseErrorListener {
        final List<String> errores = new ArrayList<>();
        int ambiguedades = 0;

        @Override
        public void syntaxError(Recognizer<?, ?> r, Object simbolo, int linea,
                                int columna, String msg, RecognitionException e) {
            errores.add("columna " + (columna + 1) + ": " + msg);
        }

        @Override
        public void reportAmbiguity(Parser parser, DFA dfa, int inicio, int fin, boolean exacta,
                                    BitSet alternativas, ATNConfigSet configuraciones) {
            ambiguedades++;
        }
    }

    /** Un arbol de derivacion escrito con parentesis, junto con su valor. */
    record Arbol(String expresion, BigInteger valor) {}

    /** Resultado de pasar la cadena por ANTLR. El arbol es null si se rechaza. */
    record Resultado(ParseTree arbol, Oyente oyente) {}

    static Resultado analizar(String texto) {
        return analizar(texto, false);
    }

    /** Analiza con Ambigua.g4, o con AmbiguaInvertida.g4 si invertida es true. */
    static Resultado analizar(String texto, boolean invertida) {
        Oyente oyente = new Oyente();
        CharStream entrada = CharStreams.fromString(texto);

        Lexer lexer = invertida ? new AmbiguaInvertidaLexer(entrada) : new AmbiguaLexer(entrada);
        lexer.removeErrorListeners();
        lexer.addErrorListener(oyente);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ParseTree arbol;
        if (invertida) {
            AmbiguaInvertidaParser parser = new AmbiguaInvertidaParser(tokens);
            preparar(parser, oyente);
            arbol = parser.inicio().e();
        } else {
            AmbiguaParser parser = new AmbiguaParser(tokens);
            preparar(parser, oyente);
            arbol = parser.inicio().e();
        }
        return new Resultado(oyente.errores.isEmpty() ? arbol : null, oyente);
    }

    static void preparar(Parser parser, Oyente oyente) {
        parser.removeErrorListeners();
        parser.addErrorListener(oyente);
        // Modo de prediccion que le pide a ANTLR reportar las ambiguedades que encuentre
        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
    }

    /** Recorre el arbol que armo ANTLR (sirve para las dos gramaticas). */
    static Arbol leerArbol(ParseTree nodo) {
        if (nodo.getChildCount() == 1) {  // E -> num
            return new Arbol(nodo.getText(), new BigInteger(nodo.getText()));
        }
        return combinar(leerArbol(nodo.getChild(0)), nodo.getChild(1).getText(),
                        leerArbol(nodo.getChild(2)));
    }

    static Arbol combinar(Arbol izq, String op, Arbol der) {
        BigInteger valor = op.equals("+") ? izq.valor().add(der.valor())
                                          : izq.valor().multiply(der.valor());
        return new Arbol("(" + izq.expresion() + " " + op + " " + der.expresion() + ")", valor);
    }

    /**
     * Todos los arboles de derivacion de E -> E+E | E*E | num para los tokens.
     *
     * No depende de ANTLR: prueba cada operador como raiz (E -> E op E) y combina
     * todos los arboles posibles de la izquierda con todos los de la derecha.
     */
    static List<Arbol> arbolesPosibles(List<String> tokens) {
        if (tokens.size() == 1) {
            return List.of(new Arbol(tokens.get(0), new BigInteger(tokens.get(0))));
        }
        List<Arbol> arboles = new ArrayList<>();
        for (int i = 1; i < tokens.size(); i += 2) {  // los operadores estan en posiciones impares
            String op = tokens.get(i);
            for (Arbol izq : arbolesPosibles(tokens.subList(0, i))) {
                for (Arbol der : arbolesPosibles(tokens.subList(i + 1, tokens.size()))) {
                    arboles.add(combinar(izq, op, der));
                }
            }
        }
        return arboles;
    }

    static List<String> tokensDe(String texto) {
        List<String> tokens = new ArrayList<>();
        for (Token token : new AmbiguaLexer(CharStreams.fromString(texto)).getAllTokens()) {
            tokens.add(token.getText());
        }
        return tokens;
    }

    static String sinParentesisExternos(String expresion) {
        return expresion.startsWith("(") ? expresion.substring(1, expresion.length() - 1) : expresion;
    }

    enum Veredicto { RECHAZADA, UN_ARBOL, AMBIGUA }

    /** Imprime el analisis de una cadena. */
    static Veredicto mostrar(String texto) {
        Resultado original = analizar(texto);
        if (original.arbol() == null) {
            System.out.println(texto + "  ->  RECHAZADA");
            for (String error : original.oyente().errores) {
                System.out.println("    " + error);
            }
            System.out.println();
            return Veredicto.RECHAZADA;
        }
        System.out.println(texto + "  ->  ACEPTADA");

        List<String> tokens = tokensDe(texto);
        int operadores = tokens.size() / 2;
        // Con 2 o mas operadores siempre hay mas de un arbol en esta gramatica
        boolean ambigua = operadores >= 2;

        if (operadores > MAX_OPERADORES) {
            System.out.println("  Arboles de derivacion posibles: miles (no se listan con mas de "
                               + MAX_OPERADORES + " operadores)");
        } else {
            List<Arbol> todos = arbolesPosibles(tokens);
            System.out.println("  Arboles de derivacion posibles: " + todos.size());
            for (Arbol arbol : todos.subList(0, Math.min(todos.size(), MAX_MOSTRAR))) {
                System.out.println("    " + sinParentesisExternos(arbol.expresion()) + " = " + arbol.valor());
            }
            if (todos.size() > MAX_MOSTRAR) {
                System.out.println("    ... y " + (todos.size() - MAX_MOSTRAR) + " mas");
            }
        }

        Arbol elegido = leerArbol(original.arbol());
        Arbol invertido = leerArbol(analizar(texto, true).arbol());
        System.out.println("  ANTLR con el orden de la diapositiva: "
                           + sinParentesisExternos(elegido.expresion()) + " = " + elegido.valor());
        System.out.println("  ANTLR con el orden invertido:        "
                           + sinParentesisExternos(invertido.expresion()) + " = " + invertido.valor());
        System.out.println("  Avisos de ambiguedad de ANTLR: " + original.oyente().ambiguedades);
        System.out.println(ambigua ? "  => AMBIGUA: la misma cadena tiene mas de un arbol"
                                   : "  => Un solo arbol");
        System.out.println();
        return ambigua ? Veredicto.AMBIGUA : Veredicto.UN_ARBOL;
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
        int aceptadas = 0;
        int ambiguas = 0;
        for (String linea : lineas) {
            linea = linea.replace(BOM, "").trim();
            if (linea.isEmpty()) {
                continue;
            }
            Veredicto veredicto = mostrar(linea);
            if (veredicto != Veredicto.RECHAZADA) {
                aceptadas++;
            }
            if (veredicto == Veredicto.AMBIGUA) {
                ambiguas++;
            }
        }

        System.out.println(ambiguas + " de " + aceptadas + " cadenas aceptadas tienen mas de un arbol.");
        if (ambiguas > 0) {
            System.out.println("Conclusion: la gramatica de la diapositiva 15 es ambigua.");
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
