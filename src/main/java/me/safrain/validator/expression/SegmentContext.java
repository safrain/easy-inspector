package me.safrain.validator.expression;

import me.safrain.validator.ValidationContext;
import me.safrain.validator.Violation;
import me.safrain.validator.accessor.ArrayAccessor;
import me.safrain.validator.accessor.PropertyAccessor;
import me.safrain.validator.expression.segments.PathSegment;

import java.lang.reflect.Method;

public class SegmentContext {
    private ValidationContext validationContext;
    private Method method;
    private Expression expression;
    private PropertyAccessor propertyAccessor;
    private ArrayAccessor arrayAccessor;
    private ValidateCommand validateCommand;
    private Object[] args;

    /**
     * The suppress lock: who activate suppress mode first?
     */
    private PathSegment suppressSegment;
    /**
     * If any violation occurred or violations are force suppressed?
     */
    private boolean suppress;

    /**
     * In an aggregate segment, violation in each segment should be collect only once
     * Use suppress mode to prevent 'Too many violations' case happens
     * (Consider each element in a huge array produces a violation)
     */
    public void activateSuppressMode(PathSegment segment, boolean force) {
        if (suppressSegment != null) return;
        suppressSegment = segment;
        suppress = force;
    }

    public void deactivateSuppressMode(PathSegment segment) {
        if (suppressSegment != segment) return;
        suppressSegment = null;
        suppress = false;
    }


    /**
     * The object is rejected by the accessor
     * Add violation to context if expression is not optional
     */
    public boolean onRejected(Object object) {
        if (object == null && expression.isOptional()) {
            return true;
        } else {
            Violation violation = createViolation();
            violation.setType(Violation.Type.SEGMENT_REJECTED);
            violation.setObject(object);
            addViolation(violation);
            return false;
        }
    }

    /**
     * Call the actual validate method to validate current object
     * Add violation to context if failed or exception occurred
     */
    public boolean onValidation(Object object) {
        try {
            if (!validateCommand.validate(object)) {
                Violation violation = createViolation();
                violation.setType(Violation.Type.INVALID);
                violation.setObject(object);
                addViolation(violation);
                return false;
            }
        } catch (Throwable e) {
            Violation violation = createViolation();
            violation.setType(Violation.Type.EXCEPTION);
            violation.setObject(object);
            violation.setThrowable(e);
            addViolation(violation);
            return false;
        }

        return true;
    }

    public boolean isLastSegment(int index) {
        return index == validationContext.scope.size() + expression.getSegments().size() - 1;
    }

    public PathSegment getSegment(int index) {
        return index < validationContext.scope.size() ?
                validationContext.scope.get(index) :
                expression.getSegments().get(index - validationContext.scope.size());
    }

    public Violation createViolation() {
        Violation result = new Violation();
        result.setExpression(expression);
        result.setMethod(method);
        result.setArgs(args);
        return result;
    }

    public void addViolation(Violation violation) {
        if (!isViolationSuppressed()) {
            // Do not add violation while in manual mode or already has violation in aggregate sibling
            if (args != null) {
                Object[] a = new Object[args.length - 1];
                System.arraycopy(args, 1, a, 0, args.length - 1);
                violation.setArgs(a);
            }
            validationContext.violations.add(violation);
        }
    }

    public boolean isViolationSuppressed() {
        return validationContext.manual || suppress;
    }


    public ValidationContext getValidationContext() {
        return validationContext;
    }

    public void setValidationContext(ValidationContext validationContext) {
        this.validationContext = validationContext;
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

    public ValidateCommand getValidateCommand() {
        return validateCommand;
    }

    public void setValidateCommand(ValidateCommand validateCommand) {
        this.validateCommand = validateCommand;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
