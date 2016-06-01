package me.safrain.validator

import antlr.generated.EVExpressionBaseListener
import antlr.generated.EVExpressionLexer
import antlr.generated.EVExpressionParser
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.junit.Test

class AntlrTest {

    class ErrorListener implements ANTLRErrorListener {
        boolean error

        @Override
        void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            error = true
        }

        @Override
        void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
            error = true
        }

        @Override
        void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
            error = true
        }

        @Override
        void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
            error = true
        }
    }

    @Test
    void testPropertyName() {
        [
                '/a /a'
        ].each {
            assert !test(it)
        }
    }

    @Test
    void testAntlr1() {
        [
                '/a/[1]'
        ].each {
            assert test(it)
        }
    }

    @Test
    void testAntlr() {
        [
                '/',
                '/a',
                '/a/b',
                '/a[0]',
                '/a[10]',
                '/a[-1]',
                '/a[-10]',
                '/a/[0]',
                '/a/[-1]',
                '/a/[-1 ]',
                '/a/[0 .. 1]',
                '/a/[0.. 9]',
                '/a/[ 0.. 1 ]',
                '/a/[*]',
                '/a/[?]',
                '/a[*]',
                '/a[?]',
                '/[1]',
                '[1]',
                '[1..2]'
        ].each {
            assert test(it)
        }

        [
                '//',
                '/a/b/',
                '/aa%',
                '/aa%f',
                '/a /b'
        ].each {
            assert !test(it)
        }
    }


    boolean test(String input, exp = null) {
        def errorListener = new ErrorListener()
        EVExpressionLexer lexer = new EVExpressionLexer(new ANTLRInputStream(input))
        lexer.addErrorListener(errorListener)
        CommonTokenStream tokens = new CommonTokenStream(lexer)
        EVExpressionParser parser = new EVExpressionParser(tokens);
        parser.addErrorListener(errorListener)
        ParseTreeWalker walker = new ParseTreeWalker()
        walker.walk(new EVExpressionBaseListener() {
        }, exp == null ? parser.expression() : parser."$exp"())


        return !errorListener.error
    }
}
