/**
 * Recorre el parse tree con un Visitor y arma el AST.
 *
 * Se descartan los nodos de paso (E -> T, T -> F) y los parentesis: la
 * agrupacion que indicaban ya queda representada en la forma del arbol.
 */
public class ConstructorAST extends ExprBaseVisitor<Nodo> {

    @Override
    public Nodo visitInicio(ExprParser.InicioContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Nodo visitExpr(ExprParser.ExprContext ctx) {
        if (ctx.expr() == null) {  // E -> T
            return visit(ctx.term());
        }
        String op = ctx.getChild(1).getText();  // "+" o "-"
        return new Nodo.OpBin(op, visit(ctx.expr()), visit(ctx.term()));
    }

    @Override
    public Nodo visitTerm(ExprParser.TermContext ctx) {
        if (ctx.term() == null) {  // T -> F
            return visit(ctx.factor());
        }
        return new Nodo.OpBin("*", visit(ctx.term()), visit(ctx.factor()));
    }

    @Override
    public Nodo visitFactor(ExprParser.FactorContext ctx) {
        if (ctx.ID() != null) {
            return new Nodo.Id(ctx.getText());
        }
        if (ctx.NUM() != null) {
            return new Nodo.Num(ctx.getText());
        }
        return visit(ctx.expr());  // F -> ( E ): el parentesis no pasa al AST
    }
}
