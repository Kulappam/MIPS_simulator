package mips.exceptions;

// исключение при неправильных аргументах
public class ArgumentException extends RuntimeException {
    public ArgumentException(String message) {
        super(message);
    }
}
