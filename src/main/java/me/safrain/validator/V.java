package me.safrain.validator;

import me.safrain.validator.expression.Expression;

import java.util.ArrayList;
import java.util.List;

public class V {
    public static StringRules string;
    public static NumberRules number;
    public static CommonRules common;

    public static class CommonRules {
        public boolean isNull(Object obj) {
            return obj == null;
        }

        public boolean notNull(Object obj) {
            return obj != null;
        }

        public boolean isEquals(Object obj, Object o) {
            if (obj == null) return o == null;
            return obj.equals(o);
        }
    }

    public static class NumberRules {
        public boolean isInteger(Object obj) {
            return obj instanceof Integer;
        }

        public boolean isNumber(Object obj) {
            return obj instanceof Number;
        }

        public boolean notZero(Object obj) {
            return obj instanceof Number && !obj.equals(0);
        }
    }

    public static class StringRules {
        public boolean isString(Object obj) {
            return obj instanceof String;
        }

        public boolean notEmpty(Object obj) {
            return obj instanceof String && !((String) obj).isEmpty();
        }
    }

    public static Object get(String expression) {
        ValidationContext context = EasyInspector.contextThreadLocal.get();
        return null;
    }

    public static void scope(String expression, Runnable closure) {
        ValidationContext context = EasyInspector.contextThreadLocal.get();
        Expression exp = context.easyInspector.expressionResolver.resolve(expression);
        context.scope.addAll(exp.getSegments());
        closure.run();
        for (int i = 0; i < exp.getSegments().size(); i++) {
            context.scope.remove(context.scope.size() - 1);
        }

    }

    public static void manual(Manual manual) {
        ValidationContext context = EasyInspector.contextThreadLocal.get();
        context.manual = true;
        List<Violation> violations = new ArrayList<Violation>();
        manual.validate(violations);
        context.violations.addAll(violations);
        context.manual = false;
    }

    public interface Manual {
        void validate(List<Violation> violations);
    }
}
