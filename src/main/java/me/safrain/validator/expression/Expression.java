package me.safrain.validator.expression;

import me.safrain.validator.expression.segments.PathSegment;

import java.util.ArrayList;
import java.util.List;

public class Expression {
    /**
     * Raw expression String
     */
    private String expression;

    /**
     * Segments parsed from raw expression string
     */
    private List<PathSegment> segments = new ArrayList<PathSegment>();
    /**
     * Optional flag: the '?' prefix
     */
    private boolean optional;
    /**
     * Comment string from first '#' to the end
     */
    private String comment;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public List<PathSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<PathSegment> segments) {
        this.segments = segments;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
