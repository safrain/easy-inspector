package me.safrain.validator;

import me.safrain.validator.expression.PathSegment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ValidationContext {
    private Deque<Object> targetStack = new ArrayDeque<Object>();
    public List<Violation> violations = new ArrayList<Violation>();

    public List<PathSegment> scope = new ArrayList<PathSegment>();
    EasyInspector easyInspector;

    public boolean manual;

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

}
