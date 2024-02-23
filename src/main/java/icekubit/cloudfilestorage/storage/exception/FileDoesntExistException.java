package icekubit.cloudfilestorage.storage.exception;

public class FileDoesntExistException extends RuntimeException {
    public FileDoesntExistException(String message) {
        super(message);
    }
}
