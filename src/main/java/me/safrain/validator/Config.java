package me.safrain.validator;

import me.safrain.validator.accessor.ArrayAccessor;
import me.safrain.validator.accessor.PropertyAccessor;
import me.safrain.validator.expression.resolver.ExpressionResolver;

public class Config {
    private ExpressionResolver expressionResolver;
    private PropertyAccessor propertyAccessor;
    private ArrayAccessor arrayAccessor;

    public ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    public void setExpressionResolver(ExpressionResolver expressionResolver) {
        this.expressionResolver = expressionResolver;
    }

    public PropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    public void setPropertyAccessor(PropertyAccessor propertyAccessor) {
        this.propertyAccessor = propertyAccessor;
    }

    public ArrayAccessor getArrayAccessor() {
        return arrayAccessor;
    }

    public void setArrayAccessor(ArrayAccessor arrayAccessor) {
        this.arrayAccessor = arrayAccessor;
    }
}
