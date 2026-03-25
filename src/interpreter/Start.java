package interpreter;

import grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;

public class Start {
    public static void main(String[] args) {
        CharStream inp;
        try {
            String fileName = args.length > 0 ? args[0] : "we.first";
            inp = CharStreams.fromFileName(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        firstLexer lex = new firstLexer(inp);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        firstParser par = new firstParser(tokens);

        ParseTree tree = par.prog();

        CalculateVisitor v = new CalculateVisitor(inp, tokens);
        v.visit(tree);
    }
}