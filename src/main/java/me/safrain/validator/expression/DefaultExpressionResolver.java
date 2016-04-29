package me.safrain.validator.expression;

public class DefaultExpressionResolver implements ExpressionResolver {
    @Override
    public Expression resolve(String expression) {
        Expression result = new Expression();
        result.setExpression(expression);
        int l = extractPath(expression, result);
        extractComment(l, expression, result);
        return result;
    }

    private void extractComment(int start, String expression, Expression result) {
        boolean commentStart = false;
        boolean escape = false;
        StringBuilder comment = new StringBuilder();
        for (int i = start; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (escape) {
                processEscape(c, comment);
                escape = false;
            } else {
                // Normal mode
                switch (c) {
                    case '%': // Go into escape mode
                        escape = true;
                        break;
                    case '#': // Comment start
                        commentStart = true;
                        break;
                    default:
                        if (commentStart) comment.append(c);
                }
            }
        }
        if (comment.length() > 0) result.setComment(comment.toString());
    }


    // Return next index
    private int extractPath(String expression, Expression result) {
        boolean escape = false;
        boolean arrayAccess = false;
        StringBuilder segmentString = new StringBuilder();
        StringBuilder numberString = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (escape) {
                // Escape mode, add any valid char to the segment
                processEscape(c, segmentString);
                escape = false;
            } else if (arrayAccess) {
                if ((c >= '0' && c <= '9') || c == '-') {
                    // number
                    numberString.append(c);
                } else if (c == '*') {
                    if (numberString.length() > 0) throw new IllegalArgumentException();
                    numberString.append(c);
                } else if (c == ']') {
                    if (numberString.length() > 0) {
                        if ("*".equals(numberString.toString())) { // Each element
                            result.getSegments().add(new AllArrayElementSegment());
                        } else { // Element by index
                            Integer index = Integer.valueOf(numberString.toString());
                            result.getSegments().add(new ArrayIndexAccessSegment(index));
                        }
                    }
                    numberString = new StringBuilder();
                    arrayAccess = false;
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                // Normal mode
                switch (c) {
                    case '?': // Optional tag
                        if (!result.getSegments().isEmpty()) throw new IllegalArgumentException();
                        result.setOptional(true);
                        break;
                    case '%': // Go into escape mode
                        escape = true;
                        break;
                    case '/': // Segment splitter
                        if (segmentString.length() > 0) {
                            result.getSegments().add(parseSegment(segmentString.toString()));
                            segmentString = new StringBuilder();
                        }
                        break;
                    case '*': // All property / wildcard?
                        if (segmentString.length() > 0) {
                            throw new IllegalStateException();
                        }
                        result.getSegments().add(new AllPropertySegment());
                        break;
                    case '[': // Array access start
                        if (segmentString.length() > 0) {
                            result.getSegments().add(parseSegment(segmentString.toString()));
                            segmentString = new StringBuilder();
                        }
                        arrayAccess = true;
                        break;
                    case ' ': // End of path
                        if (segmentString.length() > 0) { // Last segment
                            result.getSegments().add(parseSegment(segmentString.toString()));
                        }
                        if (!result.getSegments().isEmpty()) return i + 1;
                        break;
                    default:
                        segmentString.append(c);
                }
            }
        }
        if (escape) throw new IllegalStateException();

        if (segmentString.length() > 0) {
            result.getSegments().add(parseSegment(segmentString.toString()));
        }
        return expression.length();
    }

    private void processEscape(char c, StringBuilder stringBuilder) {
        switch (c) {
            case '%':
            case ' ':
            case '/':
                stringBuilder.append(c);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private PathSegment parseSegment(String segmentString) {
        PathSegment result;

        result = AllPropertySegment.parse(segmentString);
        if (result != null) return result;

        result = PropertySegment.parse(segmentString);
        if (result != null) return result;

        throw new IllegalArgumentException();
    }
}
