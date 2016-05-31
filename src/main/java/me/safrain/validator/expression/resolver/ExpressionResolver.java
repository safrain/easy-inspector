package me.safrain.validator.expression.resolver;

import me.safrain.validator.expression.Expression;

public interface ExpressionResolver {
    Expression resolve(String expression);
}
