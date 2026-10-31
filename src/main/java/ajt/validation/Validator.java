package ajt.validation;


public interface Validator<T> {

    boolean validate(T t);
}
