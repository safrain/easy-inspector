package me.safrain.validator;

import me.safrain.validator.expression.Expression;
import me.safrain.validator.expression.SegmentContext;
import me.safrain.validator.expression.ValidateCommand;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class V {
    public static StringRules STRING;
    public static NumberRules NUMBER;
    public static CommonRules COMMON;
    public static ArrayRules ARRAY;

    public static class ArrayRules {
        public boolean isArray(Object obj) {
            return obj != null && obj.getClass().isArray();
        }

        public boolean isList(Object obj) {
            return obj instanceof List;
        }

        public boolean isArrayOrList(Object obj) {
            return isArray(obj) || isList(obj);
        }

        public boolean notEmpty(Object obj) {
            if (isArray(obj)) {
                return Array.getLength(obj) > 0;
            }
            if (isList(obj)) {
                return !((List) obj).isEmpty();
            }
            return false;
        }
    }

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
        public boolean inRange(Object obj, Integer from, Integer to) {
            if (!(obj instanceof Number)) return false;
            int val = ((Number) obj).intValue();
            if (from != null) {
                if (val < from) return false;
            }
            if (to != null) {
                if (val > to) return false;
            }
            return true;
        }

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


    static class Holder implements ValidateCommand {
        ArrayList<Object> result = new ArrayList<Object>();

        @Override
        public boolean validate(Object object) throws Throwable {
            result.add(object);
            return true;
        }
    }

    public static Object get(String expression) {
        List<Object> result = getAll(expression);
        if (result.isEmpty()) return null;
        return result.get(0);
    }

    static Method GET_ALL_METHOD;

    static {
        try {
            GET_ALL_METHOD = V.class.getDeclaredMethod("getAll", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException();
        }
    }

    public static List<Object> getAll(String expression) {
        ValidationContext context = EasyInspector.contextThreadLocal.get();
        Expression exp = context.getEasyInspector().getExpressionResolver().resolve(expression);
        SegmentContext segmentContext = new SegmentContext();
        segmentContext.setValidationContext(context);
        segmentContext.setMethod(GET_ALL_METHOD);
        segmentContext.setExpression(exp);
        segmentContext.setArgs(new Object[]{expression});
        Holder holder = new Holder();
        segmentContext.setValidateCommand(holder);
        segmentContext.activateSuppressMode(exp.getSegments().get(0), true);
        segmentContext.getExpression().getSegments().get(0).process(context.getRootObject(), 0, segmentContext, true);
        return holder.result;
    }


    public static void scope(String expression, Runnable closure) {
        ValidationContext context = EasyInspector.contextThreadLocal.get();
        Expression exp = context.getEasyInspector().getExpressionResolver().resolve(expression);
        context.getScope().addAll(exp.getSegments());
        closure.run();
        for (int i = 0; i < exp.getSegments().size(); i++) {
            context.getScope().remove(context.getScope().size() - 1);
        }

    }

    public static void manual(Manual manual) {
        ValidationContext context = EasyInspector.contextThreadLocal.get();
        context.setManual(true);
        List<Violation> violations = new ArrayList<Violation>();
        manual.validate(violations);
        context.getViolations().addAll(violations);
        context.setManual(false);
    }

    public interface Manual {
        void validate(List<Violation> violations);
    }
}
