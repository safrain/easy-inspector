package me.safrain.validator;

import me.safrain.validator.expression.Expression;

import java.lang.reflect.Method;

public class Violation {
    public Method method;
    public Expression expression;
    public Throwable throwable;
    public String message;

    public Violation(String message) {
        this.message = message;
    }

    public Violation(Method method, Expression expression) {
        this.method = method;
        this.expression = expression;
    }

    public Violation(Method method, Expression expression, Throwable throwable) {
        this.method = method;
        this.expression = expression;
        this.throwable = throwable;
    }

    public Violation(Throwable throwable) {
        this.throwable = throwable;
    }

    public Violation() {
    }
}
