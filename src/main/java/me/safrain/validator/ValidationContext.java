package me.safrain.validator;

import me.safrain.validator.expression.segments.PathSegment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ValidationContext {
    private Deque<Object> targetStack = new ArrayDeque<Object>();
    private List<Violation> violations = new ArrayList<Violation>();

    private List<PathSegment> scope = new ArrayList<PathSegment>();
    private EasyInspector easyInspector;

    private boolean manual;

    public Object getRootObject() {
        return targetStack.peek();
    }

    public void push(Object object) {
        targetStack.push(object);
    }


    public void pop() {
        targetStack.pop();
    }

    public boolean isStackEmpty() {
        return targetStack.isEmpty();
    }

    public EasyInspector getEasyInspector() {
        return easyInspector;
    }

    public void setEasyInspector(EasyInspector easyInspector) {
        this.easyInspector = easyInspector;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }

    public List<PathSegment> getScope() {
        return scope;
    }

    public void setScope(List<PathSegment> scope) {
        this.scope = scope;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }
}
