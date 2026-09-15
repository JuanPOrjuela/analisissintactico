"""Pruebas del punto 1. Se corren con: python pruebas.py"""
import unittest

from main import analizar


class PruebasGramaticaExpresiones(unittest.TestCase):

    def test_ejemplos_de_la_diapositiva(self):
        for texto in ["2 + 3 * 4", "2 + 3 - 4", "2 + 3 * (4 - 5)"]:
            with self.subTest(texto=texto):
                self.assertTrue(analizar(texto)[0])

    def test_identificadores_y_numeros(self):
        for texto in ["x", "42", "3.5 * radio", "x1 * (y + 10)", "((a))"]:
            with self.subTest(texto=texto):
                self.assertTrue(analizar(texto)[0])

    def test_expresiones_invalidas(self):
        invalidas = [
            "",                # vacia
            "2 +",             # falta el segundo operando
            "+ 2",             # empieza con operador
            "2 + * 3",         # dos operadores seguidos
            "(2 + 3",          # parentesis sin cerrar
            "2 + 3)",          # parentesis de mas
            "2 $ 3",           # simbolo que el lexer no conoce
            "2 (3 * 4)",       # no hay multiplicacion implicita
            "2 / 3",           # la division no esta en la gramatica
        ]
        for texto in invalidas:
            with self.subTest(texto=texto):
                self.assertFalse(analizar(texto)[0])

    def test_multiplicacion_tiene_mas_precedencia_que_suma(self):
        # 2 + (3 * 4): la suma queda en la raiz y el * dentro de T
        _, arbol, _ = analizar("2 + 3 * 4")
        self.assertEqual(
            arbol,
            "(expr (expr (term (factor 2))) + (term (term (factor 3)) * (factor 4)))",
        )

    def test_suma_y_resta_asocian_por_la_izquierda(self):
        # (2 + 3) - 4: la resta queda en la raiz y la suma a su izquierda
        _, arbol, _ = analizar("2 + 3 - 4")
        self.assertEqual(
            arbol,
            "(expr (expr (expr (term (factor 2))) + (term (factor 3))) - (term (factor 4)))",
        )

    def test_parentesis_cambian_la_agrupacion(self):
        # (2 + 3) * 4: ahora el * queda en la raiz
        _, arbol, _ = analizar("(2 + 3) * 4")
        self.assertTrue(arbol.startswith("(expr (term (term (factor ( (expr"))

    def test_el_error_indica_la_columna(self):
        _, _, errores = analizar("2 + * 3")
        self.assertIn("columna 5", errores[0])


if __name__ == "__main__":
    unittest.main(verbosity=2)
