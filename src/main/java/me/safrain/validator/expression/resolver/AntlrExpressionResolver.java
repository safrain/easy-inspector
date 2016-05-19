package me.safrain.validator.expression.resolver;

import antlr.generated.EVExpressionBaseListener;
import antlr.generated.EVExpressionLexer;
import antlr.generated.EVExpressionParser;
import me.safrain.validator.expression.Expression;
import me.safrain.validator.expression.SegmentContext;
import me.safrain.validator.expression.segments.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ANTLRExpressionResolver implements ExpressionResolver {

    public ANTLRExpressionResolver() {
    }

    @Override
    public Expression resolve(String expression) {
        Expression result = new Expression();
        result.setExpression(expression);

        ErrorListener errorListener = new ErrorListener();
        EVExpressionLexer lexer = new EVExpressionLexer(new ANTLRInputStream(expression));
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        EVExpressionParser parser = new EVExpressionParser(tokens);
        parser.addErrorListener(errorListener);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ParseListener(result) {
        }, parser.expression());

        if (!errorListener.errors.isEmpty()) throw new IllegalArgumentException();
        if (result.getSegments().isEmpty()) {
            result.getSegments().add(new RootSegment());
        }
        return result;
    }

    static String unescape(String input) {
        StringBuilder result = new StringBuilder();
        boolean escape = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (!escape) {
                if (c == '%') {
                    escape = true;
                } else {
                    result.append(c);
                }
            } else {
                switch (c) {
                    case '%':
                    case ' ':
                    case '/':
                    case '[':
                    case ']':
                        result.append(c);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                escape = false;
            }
        }
        return result.toString();
    }

    public static class ErrorListener extends BaseErrorListener {
        List<String> errors = new ArrayList<String>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            errors.add(msg);
        }
    }

    public static class ParseListener extends EVExpressionBaseListener {
        Expression result;

        int level;

        public ParseListener(Expression result) {
            this.result = result;
        }

        @Override
        public void enterOptionalTag(EVExpressionParser.OptionalTagContext ctx) {
            result.setOptional(true);
        }

        @Override
        public void enterComment(EVExpressionParser.CommentContext ctx) {
            result.setComment(ctx.getText());
        }

        @Override
        public void enterPropertyAccess(EVExpressionParser.PropertyAccessContext ctx) {
            if (level > 0) return;
            PropertySegment propertySegment = new PropertySegment();
            propertySegment.setPropertyName(unescape(ctx.getText()));
            result.getSegments().add(propertySegment);
        }

        @Override
        public void enterPropertyEveryAccess(EVExpressionParser.PropertyEveryAccessContext ctx) {
            if (level > 0) return;
            result.getSegments().add(new EveryPropertySegment());
        }

        @Override
        public void enterPropertyAnyAccess(EVExpressionParser.PropertyAnyAccessContext ctx) {
            if (level > 0) return;
            result.getSegments().add(new AnyPropertySegment());
        }

        @Override
        public void enterArrayAccess(EVExpressionParser.ArrayAccessContext ctx) {
            level++;
            if (ctx.propertyAccess() != null) {
                PropertySegment propertySegment = new PropertySegment();
                propertySegment.setPropertyName(unescape(ctx.propertyAccess().getText()));
                result.getSegments().add(propertySegment);
            }
        }

        @Override
        public void exitArrayAccess(EVExpressionParser.ArrayAccessContext ctx) {
            level--;
        }

        @Override
        public void enterArrayIndexAccess(EVExpressionParser.ArrayIndexAccessContext ctx) {
            result.getSegments().add(new ArrayIndexAccessSegment(Integer.valueOf(unescape(ctx.getText()))));
        }

        @Override
        public void enterArrayRangeAccess(EVExpressionParser.ArrayRangeAccessContext ctx) {
            result.getSegments().add(new ArrayRangeAccessSegment(
                    Integer.valueOf(ctx.arrayIndex(0).getText()),
                    Integer.valueOf(ctx.arrayIndex(1).getText())
            ));
        }

        @Override
        public void enterArrayEveryAccess(EVExpressionParser.ArrayEveryAccessContext ctx) {
            result.getSegments().add(new EveryArrayElementSegment());
        }

        @Override
        public void enterArrayAnyAccess(EVExpressionParser.ArrayAnyAccessContext ctx) {
            result.getSegments().add(new AnyArrayElementSegment());
        }
    }
}
