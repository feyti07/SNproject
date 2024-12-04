package com.snim.demandesrh.handlers;
import com.snim.demandesrh.exceptions.ObjectValidationException;
import com.snim.demandesrh.exceptions.OperationNonPermittedException;
import com.snim.demandesrh.exceptions.ResourceNotFoundException;
import com.snim.demandesrh.exceptions.UserUpdateException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectValidationException.class)
    public ResponseEntity<ExceptionRepresntation> handleException(ObjectValidationException exception){
        ExceptionRepresntation represntation = ExceptionRepresntation.builder()
                .errorMessages("object not valid exception has occured")
                .errorSource(exception.getViolationSource())
                .validationErrors(exception.getViolations())
                .build();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(represntation);

    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionRepresntation> handleException(EntityNotFoundException exception){
        ExceptionRepresntation represntation = ExceptionRepresntation.builder()
                .errorMessages(exception.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(represntation);
    }
    @ExceptionHandler(UserUpdateException.class)
    public ResponseEntity<String> handleUserUpdateException(UserUpdateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(OperationNonPermittedException.class)
    public ResponseEntity<ExceptionRepresntation> handleException(OperationNonPermittedException exception){
        ExceptionRepresntation represntation = ExceptionRepresntation.builder()
                .errorMessages(exception.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(represntation);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionRepresntation> handleException(IllegalArgumentException exception){
        ExceptionRepresntation represntation = ExceptionRepresntation.builder()
                .errorMessages(exception.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(represntation);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        System.out.println("Access Denied: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: " + ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }


}
