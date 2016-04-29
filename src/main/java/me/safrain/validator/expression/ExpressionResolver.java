package me.safrain.validator.expression;

public interface ExpressionResolver {
    Expression resolve(String expression);

    Object NOT_FOUND = new Object();
}
