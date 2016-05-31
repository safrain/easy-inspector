package me.safrain.validator;

import me.safrain.validator.expression.Expression;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

public class Violation {
    public enum Type {
        EXCEPTION, SEGMENT_REJECTED, INVALID
    }

    private Method method;
    private Expression expression;
    private Throwable throwable;
    private String message;
    private Object object;
    private Object[] args;
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Violation(String message) {
        this.message = message;
    }

    public Violation(Method method, Expression expression) {
        this.method = method;
        this.expression = expression;
    }

    public Violation(Method method, Expression expression, Object object) {
        this.method = method;
        this.expression = expression;
        this.object = object;
    }

    public Violation(Method method, Expression expression, Object object, Throwable throwable) {
        this.method = method;
        this.expression = expression;
        this.object = object;
        this.throwable = throwable;
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

    public String getCommand() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Violation [");
        for (Iterator<String> iterator = Arrays.asList(
                method != null ? "command=" + getCommand() : null,
                expression != null ? "expression=" + expression.getExpression() : null,
                message != null ? "message=" + message : null,
                args != null ? "args=" + Arrays.toString(args) : null
        ).iterator(); iterator.hasNext(); ) {
            String string = iterator.next();
            if (string == null) continue;
            s.append(string);
            if (iterator.hasNext()) s.append(", ");
        }
        s.append("]");
        return s.toString();

    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
