package me.safrain.validator;

import me.safrain.validator.accessor.ArrayAccessor;
import me.safrain.validator.accessor.PropertyAccessor;
import me.safrain.validator.expression.*;
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
    ExpressionResolver expressionResolver = new DefaultExpressionResolver();

    public static ThreadLocal<ValidationContext> contextThreadLocal = new InheritableThreadLocal<ValidationContext>();

    PropertyAccessor propertyAccessor = new PropertyAccessor() {
        @Override
        public boolean accept(Object object) {
            if (!(object instanceof Map)) return false;
            for (Object o : ((Map) object).keySet()) {
                if (!(o instanceof String)) return false;
            }
            return true;
        }

        @Override
        public Object accessProperty(Object object, String propertyName) {
            return ((Map) object).get(propertyName);
        }

        @Override
        public List<String> getPropertyNames(Object object) {
            List<String> result = new ArrayList<String>();
            for (Object o : ((Map) object).keySet()) {
                result.add((String) o);
            }
            return result;
        }
    };

    ArrayAccessor arrayAccessor = new ArrayAccessor() {
        @Override
        public boolean accept(Object object, int index) {
            if (object == null) return false;
            if (!(object instanceof List) &&
                    !(object.getClass().isArray())) {
                return false;
            }
            int size = size(object);
            if (index >= size) return false;
            if (index < -size) return false;
            return true;
        }

        @Override
        public boolean accept(Object object) {
            if (object == null) return false;
            if (object instanceof List) return true;
            if (object.getClass().isArray()) return true;
            return false;
        }

        @Override
        public int size(Object object) {
            if (object instanceof List) {
                return ((List) object).size();
            }
            if (object.getClass().isArray()) {
                return Array.getLength(object);
            }
            throw new IllegalStateException();
        }

        @Override
        public Object accessIndex(Object object, int index) {
            if (object instanceof List) {
                int size = ((List) object).size();
                return ((List) object).get(index < 0 ? size + index : index);
            }
            if (object.getClass().isArray()) {
                int size = Array.getLength(object);
                return Array.get(object, index < 0 ? size + index : index);
            }

            throw new IllegalStateException();
        }
    };

    List<Violation> validate(Object object, Validator validator) {
        ValidationContext context = contextThreadLocal.get();
        if (context == null) {
            context = new ValidationContext();
            contextThreadLocal.set(context);
            context.easyInspector = this;
        }
        context.push(object);
        validator.apply();
        context.pop();
        if (context.isStackEmpty()) {
            contextThreadLocal.remove();
        }
        return context.violations;
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
                segmentContext.validationContext = context;
                segmentContext.expression = expression;
                segmentContext.propertyAccessor = propertyAccessor;
                segmentContext.arrayAccessor = arrayAccessor;
                segmentContext.validateCommand = new ValidateCommand() {
                    @Override
                    public boolean validate(Object object) throws Throwable {
                        Object[] newArgs = new Object[args.length];
                        // Copy array to prevent args modification
                        newArgs[0] = object;
                        System.arraycopy(args, 1, newArgs, 1, args.length - 1);
                        // Call the actual method
                        return (Boolean) proxy.invokeSuper(obj, newArgs);
                    }
                };

                return segmentContext.apply(context.getRootObject(), 0, expression.isOptional());
            }
        });

        return (T) enhancer.create();
    }
}
