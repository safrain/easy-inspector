package me.safrain.validator.expression.resolver;

import me.safrain.validator.expression.Expression;

import java.util.concurrent.ConcurrentHashMap;

public class CachingExpressionResolver implements ExpressionResolver {

    private ExpressionResolver resolver;

    private ConcurrentHashMap<String, Expression> cache = new ConcurrentHashMap<String, Expression>();

    public CachingExpressionResolver(ExpressionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Expression resolve(String expression) {
        Expression result = cache.get(expression);
        if (result == null) {
            result = resolver.resolve(expression);
            cache.put(expression, result);
        }

        return result;
    }

    public void clearCache() {
        cache = new ConcurrentHashMap<String, Expression>();
    }

    public ExpressionResolver getResolver() {
        return resolver;
    }

    public void setResolver(ExpressionResolver resolver) {
        this.resolver = resolver;
    }
}
