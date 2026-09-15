import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Pruebas del punto 3. Se corren con: java Pruebas (o pruebas.bat).
 *
 * No usa JUnit para no tener que descargar otra libreria: cada prueba compara
 * lo esperado con lo obtenido y al final se muestra el resumen.
 */
public class Pruebas {

    private static int pasaron = 0;
    private static int fallaron = 0;

    public static void main(String[] args) {
        aceptaCadenasDeLaGramatica();
        rechazaLoQueNoEstaEnLaGramatica();
        dosMasTresPorCuatroTieneDosArboles();
        tambienEsAmbiguaConUnSoloOperador();
        unNumeroSoloTieneUnArbol();
        laCantidadDeArbolesCreceRapido();
        antlrEligeUnArbolSegunElOrdenDeLasAlternativas();
        antlrNoReportaLaAmbiguedad();

        System.out.println();
        System.out.println(pasaron + " pasaron, " + fallaron + " fallaron.");
        if (fallaron > 0) {
            System.exit(1);
        }
    }

    static void aceptaCadenasDeLaGramatica() {
        for (String texto : List.of("7", "2 + 3", "2 * 3", "2 + 3 * 4", "1 + 2 + 3")) {
            verificar("acepta " + texto, Main.analizar(texto).arbol() != null);
        }
    }

    static void rechazaLoQueNoEstaEnLaGramatica() {
        for (String texto : List.of("", "2 +", "2 3", "2 - 3", "(2 + 3) * 4")) {
            verificar("rechaza '" + texto + "'", Main.analizar(texto).arbol() == null);
        }
    }

    static void dosMasTresPorCuatroTieneDosArboles() {
        // Diapositivas 16 y 17: dos estructuras con resultados distintos
        List<Main.Arbol> arboles = arboles("2 + 3 * 4").stream()
                .sorted(Comparator.comparing(Main.Arbol::expresion))
                .collect(Collectors.toList());
        iguales("2 + 3 * 4 tiene dos arboles (20 y 14)",
                List.of(arbol("((2 + 3) * 4)", 20), arbol("(2 + (3 * 4))", 14)),
                arboles);
    }

    static void tambienEsAmbiguaConUnSoloOperador() {
        // (1 + 2) + 3 y 1 + (2 + 3): dan lo mismo, pero son dos arboles
        List<Main.Arbol> arboles = arboles("1 + 2 + 3");
        iguales("1 + 2 + 3 tiene dos arboles", 2, arboles.size());
        verificar("los dos arboles de 1 + 2 + 3 valen 6",
                  arboles.stream().allMatch(a -> a.valor().equals(BigInteger.valueOf(6))));
    }

    static void unNumeroSoloTieneUnArbol() {
        iguales("7 tiene un solo arbol", List.of(arbol("7", 7)), arboles("7"));
    }

    static void laCantidadDeArbolesCreceRapido() {
        // Con n operadores hay tantos arboles como el n-esimo numero de Catalan
        int[] esperados = {1, 1, 2, 5, 14, 42};
        for (int operadores = 1; operadores <= 5; operadores++) {
            String texto = String.join(" + ", Collections.nCopies(operadores + 1, "1"));
            iguales("arboles con " + operadores + " operador(es): " + esperados[operadores],
                    esperados[operadores], arboles(texto).size());
        }
    }

    static void antlrEligeUnArbolSegunElOrdenDeLasAlternativas() {
        iguales("ANTLR con el orden de la diapositiva da (2 + 3) * 4",
                arbol("((2 + 3) * 4)", 20), Main.leerArbol(Main.analizar("2 + 3 * 4").arbol()));
        iguales("ANTLR con el orden invertido da 2 + (3 * 4)",
                arbol("(2 + (3 * 4))", 14), Main.leerArbol(Main.analizar("2 + 3 * 4", true).arbol()));
    }

    static void antlrNoReportaLaAmbiguedad() {
        // Aunque la cadena tiene dos arboles, ANTLR resuelve con el orden de las
        // alternativas y su deteccion de ambiguedades no dice nada
        iguales("ANTLR reporta 0 ambiguedades en 2 + 3 * 4",
                0, Main.analizar("2 + 3 * 4").oyente().ambiguedades);
    }

    // ---- utilidades ----

    static List<Main.Arbol> arboles(String texto) {
        return Main.arbolesPosibles(Main.tokensDe(texto));
    }

    static Main.Arbol arbol(String expresion, long valor) {
        return new Main.Arbol(expresion, BigInteger.valueOf(valor));
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
