package me.safrain.validator;

import me.safrain.validator.accessor.ArrayAccessor;
import me.safrain.validator.accessor.PropertyAccessor;
import me.safrain.validator.expression.Expression;
import me.safrain.validator.expression.SegmentContext;
import me.safrain.validator.expression.ValidateCommand;
import me.safrain.validator.expression.resolver.ANTLRExpressionResolver;
import me.safrain.validator.expression.resolver.CachingExpressionResolver;
import me.safrain.validator.expression.resolver.ExpressionResolver;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EasyInspector {
    public static ThreadLocal<ValidationContext> contextThreadLocal = new InheritableThreadLocal<ValidationContext>();

    private ExpressionResolver expressionResolver;
    private PropertyAccessor propertyAccessor;
    private ArrayAccessor arrayAccessor;

    public EasyInspector(Config config) {
        expressionResolver = config.getExpressionResolver();
        propertyAccessor = config.getPropertyAccessor();
        arrayAccessor = config.getArrayAccessor();
    }

    List<Violation> validate(Object object, Validator validator) {
        ValidationContext context = contextThreadLocal.get();
        if (context == null) {
            context = new ValidationContext();
            context.setEasyInspector(this);
            contextThreadLocal.set(context);
        }
        context.push(object);
        try {
            validator.apply();
        } finally {
            context.pop();
        }
        if (context.isStackEmpty()) {
            contextThreadLocal.remove();
        }
        return context.getViolations();
    }

    public <T> T proxy(final Class clz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(final Object obj, Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
                // Do not proxy unwanted methods
                if (!(Modifier.isPublic(method.getModifiers()) &&
                        method.getReturnType() == Boolean.TYPE)) return proxy.invokeSuper(obj, args);

                // Parse the expression
                ValidationContext context = contextThreadLocal.get();
                Expression expression = expressionResolver.resolve((String) args[0]);

                // Setup context to process in segments
                SegmentContext segmentContext = new SegmentContext();
                segmentContext.setValidationContext(context);
                segmentContext.setMethod(method);
                segmentContext.setExpression(expression);
                segmentContext.setArgs(args);
                segmentContext.setValidateCommand(new ValidateCommand() {
                    @Override
                    public boolean validate(Object object) throws Throwable {
                        Object[] newArgs = new Object[args.length];
                        // Copy array to prevent args modification
                        newArgs[0] = object;
                        System.arraycopy(args, 1, newArgs, 1, args.length - 1);
                        // Call the actual method
                        return (Boolean) proxy.invokeSuper(obj, newArgs);
                    }
                });
                return segmentContext.getExpression().getSegments().get(0).process(context.getRootObject(), 0, segmentContext, expression.isOptional());
            }
        });

        return (T) enhancer.create();
    }

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
