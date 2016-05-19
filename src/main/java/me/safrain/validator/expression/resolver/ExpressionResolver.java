package me.safrain.validator.expression.resolver;

import me.safrain.validator.expression.Expression;

public interface ExpressionResolver {
    Expression resolve(String expression);

    NotFound NOT_FOUND = new NotFound();

    class NotFound {
        private NotFound() {
        }

        @Override
        public String toString() {
            return "Not found";
        }
    }
}
