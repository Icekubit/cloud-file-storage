package icekubit.cloudfilestorage.auth.exception;

public class UniqueEmailConstraintException extends RuntimeException {
    public UniqueEmailConstraintException(String message) {
        super(message);
    }
}
