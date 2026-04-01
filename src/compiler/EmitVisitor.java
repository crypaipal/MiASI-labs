package compiler;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import grammar.firstBaseVisitor;
import grammar.firstParser;

import java.util.LinkedHashSet;
import java.util.Set;

public class EmitVisitor extends firstBaseVisitor<ST> {
    private final STGroup stGroup;
    private final Set<String> globals = new LinkedHashSet<String>();

    public EmitVisitor(STGroup group) {
        super();
        this.stGroup = group;
    }

    @Override
    protected ST defaultResult() {
        return stGroup.getInstanceOf("deflt");
    }

    @Override
    protected ST aggregateResult(ST aggregate, ST nextResult) {
        if(nextResult!=null)
            aggregate.add("elem",nextResult);
        return aggregate;
    }

//    @Override
//    public ST visitTerminal(TerminalNode node) {
//        return new ST("Terminal node:<n>").add("n",node.getText());
//    }

    @Override
    public ST visitInt_tok(firstParser.Int_tokContext ctx) {
        ST st = stGroup.getInstanceOf("int");
        st.add("i",ctx.INT().getText());
        return st;
    }

    @Override
    public ST visitBinOp(firstParser.BinOpContext ctx) {
        ST st = null;

        switch (ctx.op.getType()) {
            case firstParser.ADD:
                st = stGroup.getInstanceOf("dodaj");
                break;
            case firstParser.SUB:
                st = stGroup.getInstanceOf("odejmij");
                break;
            case firstParser.MUL:
                st = stGroup.getInstanceOf("pomnoz");
                break;
            case firstParser.DIV:
                st = stGroup.getInstanceOf("podziel");
                break;
        }

        return st.add("p1", visit(ctx.l)).add("p2", visit(ctx.r));
    }

    @Override
    public ST visitId_tok(firstParser.Id_tokContext ctx) {
        String name = ctx.ID().getText();
        globals.add(name);

        ST st = stGroup.getInstanceOf("load");
        st.add("name", name);
        return st;
    }

    @Override
    public ST visitAssign(firstParser.AssignContext ctx) {
        String name = ctx.ID().getText();
        globals.add(name);

        ST st = stGroup.getInstanceOf("store");
        st.add("name", name);
        st.add("value", visit(ctx.expr()));
        return st;
    }

    @Override
    public ST visitDecl_stat(firstParser.Decl_statContext ctx) {
        String name = ctx.ID().getText();
        globals.add(name);

        return stGroup.getInstanceOf("noop");
    }

    @Override
    public ST visitDecl_assign_stat(firstParser.Decl_assign_statContext ctx) {
        String name = ctx.ID().getText();
        globals.add(name);

        ST st = stGroup.getInstanceOf("store");
        st.add("name", name);
        st.add("value", visit(ctx.expr()));
        return st;
    }

    @Override
    public ST visitProg(firstParser.ProgContext ctx) {
        ST body = stGroup.getInstanceOf("deflt");

        for (var s : ctx.stat()) {
            body.add("elem", visit(s));
        }

        ST result = stGroup.getInstanceOf("program");

        for (String g : globals) {
            ST d = stGroup.getInstanceOf("dek");
            d.add("n", g);
            result.add("decls", d);
        }

        result.add("body", body);
        return result;
    }
}
