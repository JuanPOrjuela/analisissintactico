"""Punto 1: gramatica de la diapositiva 11 implementada en ANTLR (Python).

Uso:
    python main.py               modo consola
    python main.py ejemplos.txt  analiza un archivo (una expresion por linea)
"""
import sys

from antlr4 import CommonTokenStream, InputStream
from antlr4.error.ErrorListener import ErrorListener

from ExprLexer import ExprLexer
from ExprParser import ExprParser


class ColectorErrores(ErrorListener):
    """Guarda los errores del lexer y del parser en vez de imprimirlos.

    ANTLR arma un arbol aunque la entrada sea invalida (inventa tokens para
    recuperarse), asi que la forma confiable de saber si la expresion fue
    aceptada es revisar si hubo errores.
    """

    def __init__(self):
        super().__init__()
        self.errores = []

    def syntaxError(self, recognizer, offendingSymbol, line, column, msg, e):
        self.errores.append(f"columna {column + 1}: {msg}")


def analizar(texto):
    """Devuelve (aceptada, arbol, errores). El arbol es None si se rechaza."""
    colector = ColectorErrores()

    lexer = ExprLexer(InputStream(texto))
    lexer.removeErrorListeners()
    lexer.addErrorListener(colector)

    parser = ExprParser(CommonTokenStream(lexer))
    parser.removeErrorListeners()
    parser.addErrorListener(colector)

    arbol = parser.inicio()

    if colector.errores:
        return False, None, colector.errores
    return True, arbol.expr().toStringTree(recog=parser), []


def mostrar(texto):
    aceptada, arbol, errores = analizar(texto)
    if aceptada:
        print(f"{texto}  ->  ACEPTADA")
        print(f"    arbol: {arbol}")
    else:
        print(f"{texto}  ->  RECHAZADA")
        for error in errores:
            print(f"    {error}")
    return aceptada


def analizar_archivo(ruta):
    """Cada linea no vacia del archivo es una expresion."""
    try:
        with open(ruta, encoding="utf-8-sig") as archivo:
            lineas = archivo.read().splitlines()
    except (OSError, UnicodeDecodeError) as e:
        print(f"No se pudo leer '{ruta}': {e}")
        return

    print(f"== {ruta} ==")
    total = aceptadas = 0
    for numero, linea in enumerate(lineas, start=1):
        linea = linea.strip()
        if not linea:
            continue
        total += 1
        print(f"Linea {numero}: ", end="")
        aceptadas += mostrar(linea)
    print(f"{aceptadas} de {total} expresiones aceptadas.\n")


def modo_consola():
    print("Escribe una expresion, la ruta de un archivo .txt, o 'salir'.")
    while True:
        try:
            linea = input("> ").strip()
        except EOFError:  # Ctrl+Z o fin del texto redirigido
            break
        if not linea:
            continue
        if linea.lower() == "salir":
            break

        # Al arrastrar un archivo a la terminal de Windows se pega entre comillas
        ruta = linea.strip('"')
        if ruta.lower().endswith(".txt"):
            analizar_archivo(ruta)
        else:
            mostrar(linea)


def main():
    # PowerShell manda el texto redirigido con BOM; utf-8-sig lo descarta
    sys.stdin.reconfigure(encoding="utf-8-sig")
    sys.stdout.reconfigure(encoding="utf-8")

    if len(sys.argv) > 1:
        for ruta in sys.argv[1:]:
            analizar_archivo(ruta)
    else:
        modo_consola()


if __name__ == "__main__":
    main()
