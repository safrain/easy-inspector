package me.safrain.validator.expression;

public interface ValidateCommand {
    boolean validate(Object object) throws Throwable;
}
