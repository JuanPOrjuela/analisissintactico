/** Nodos del AST: solo operadores y operandos, sin E, T, F ni parentesis. */
public interface Nodo {

    record Num(String valor) implements Nodo {}

    record Id(String nombre) implements Nodo {}

    record OpBin(String op, Nodo izq, Nodo der) implements Nodo {}
}
