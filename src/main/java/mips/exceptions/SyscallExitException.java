package mips.exceptions;

public class SyscallExitException extends RuntimeException {
    public SyscallExitException(String message) {
        super(message);
    }
}
