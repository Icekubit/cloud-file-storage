package icekubit.cloudfilestorage.storage.handler;

import icekubit.cloudfilestorage.storage.exception.ResourceDoesNotExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(ResourceDoesNotExistException exception) {
        log.error("Exception was caught: " + exception.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception exception) {
        log.error("Exception was caught: " + exception.getMessage());
        return "error/500";
    }


}