package interpreter;

import SymbolTable.LocalSymbols;
import grammar.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateVisitor extends firstBaseVisitor<Integer> {
    private TokenStream tokStream = null;
    private CharStream input = null;

    private final LocalSymbols<Integer> variables = new LocalSymbols<>();
    private final Map<String, UserFunction> functions = new HashMap<>();

    private static class UserFunction {
        List<String> params;
        firstParser.BlockContext body;

        UserFunction(List<String> params, firstParser.BlockContext body) {
            this.params = params;
            this.body = body;
        }
    }

    public CalculateVisitor(CharStream inp) {
        super();
        this.input = inp;
    }

    public CalculateVisitor(TokenStream tok) {
        super();
        this.tokStream = tok;
    }

    public CalculateVisitor(CharStream inp, TokenStream tok) {
        super();
        this.input = inp;
        this.tokStream = tok;
    }

    private String getText(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        if (input == null) throw new RuntimeException("Input stream undefined");
        return input.getText(new Interval(a, b));
    }

    @Override
    public Integer visitIf_stat(firstParser.If_statContext ctx) {
        Integer result = 0;
        if (visit(ctx.cond) != 0) {
            result = visit(ctx.then);
        } else {
            if (ctx.else_ != null) {
                result = visit(ctx.else_);
            }
        }
        return result;
    }

    @Override
    public Integer visitPrint_stat(firstParser.Print_statContext ctx) {
        var st = ctx.expr();
        var result = visit(st);
//        System.out.printf("|%s=%d|\n", st.getText(), result);
//        System.out.printf("|%s=%d|\n", getText(st), result);
        System.out.printf("|%s=%d|\n", tokStream.getText(st), result);
        return result;
    }

    @Override
    public Integer visitInt_tok(firstParser.Int_tokContext ctx) {
        return Integer.valueOf(ctx.INT().getText());
    }

    @Override
    public Integer visitPars(firstParser.ParsContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Integer visitBinOp(firstParser.BinOpContext ctx) {
        Integer result = 0;
        switch (ctx.op.getType()) {
            case firstLexer.ADD:
                result = visit(ctx.l) + visit(ctx.r);
                break;
            case firstLexer.SUB:
                result = visit(ctx.l) - visit(ctx.r);
                break;
            case firstLexer.MUL:
                result = visit(ctx.l) * visit(ctx.r);
                break;
            case firstLexer.DIV:
                try {
                    result = visit(ctx.l) / visit(ctx.r);
                } catch (Exception e) {
                    System.err.println("Div by zero");
                    throw new ArithmeticException();
                }
                break;
        }
        return result;
    }

    @Override
    public Integer visitAssign(firstParser.AssignContext ctx) {
        String name = ctx.ID().getText();
        Integer value = visit(ctx.expr());

        if (variables.hasSymbolDepth(name) != null) {
            variables.setSymbol(name, value);
        } else {
            variables.newSymbol(name);
            variables.setSymbol(name, value);
        }

        return value;
    }

    @Override
    public Integer visitId_tok(firstParser.Id_tokContext ctx) {
        String name = ctx.ID().getText();
        return variables.getSymbol(name);
    }

    @Override
    public Integer visitFunction_def_stat(firstParser.Function_def_statContext ctx) {
        var def = ctx.functionDef();
        String name = def.ID().getText();

        List<String> params = new ArrayList<>();
        if (def.paramList() != null) {
            for (var idToken : def.paramList().ID()) {
                params.add(idToken.getText());
            }
        }

        functions.put(name, new UserFunction(params, def.body));
        return 0;
    }

    @Override
    public Integer visitFuncCall(firstParser.FuncCallContext ctx) {
        String name = ctx.ID().getText();

        List<Integer> args = new ArrayList<>();
        if (ctx.argList() != null) {
            for (var expr : ctx.argList().expr()) {
                args.add(visit(expr));
            }
        }

        if (name.equals("Czesc")) {
            if (args.size() != 1) {
                throw new RuntimeException("Czesc expects exactly 1 argument");
            }
            System.out.println("Siema " + args.get(0));
            return 0;
        }

        UserFunction fn = functions.get(name);
        if (fn == null) {
            throw new RuntimeException("Function " + name + " is not defined");
        }

        if (fn.params.size() != args.size()) {
            throw new RuntimeException(
                    "Function " + name + " expects " + fn.params.size() +
                            " arguments, got " + args.size()
            );
        }

        variables.enterScope();
        try {
            for (int i = 0; i < fn.params.size(); i++) {
                variables.newSymbol(fn.params.get(i));
                variables.setSymbol(fn.params.get(i), args.get(i));
            }

            return visit(fn.body);
        } finally {
            variables.leaveScope();
        }
    }

    @Override
    public Integer visitBlock_single(firstParser.Block_singleContext ctx) {
        return visit(ctx.stat());
    }

    @Override
    public Integer visitBlock_real(firstParser.Block_realContext ctx) {
        Integer result = 0;
        for (var st : ctx.stat()) {
            result = visit(st);
        }
        return result;
    }
}