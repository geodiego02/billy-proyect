package com.billy.files.exception;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import jakarta.mail.MessagingException;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), error, message);
        return new ResponseEntity<>(errorResponse, status);
    }

	@ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        logger.error("IOException capturada: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "IO_EXCEPTION", "Ocurrió un error en el procesamiento del archivo.");
    }

	@ExceptionHandler({MessagingException.class, org.springframework.mail.MailSendException.class})
	public ResponseEntity<ErrorResponse> handleMailExceptions(Exception ex) {
	    logger.error("Excepción de envío de correo capturada: ", ex);
	    // Puedes optar por un mensaje genérico o diferenciarlo según el tipo si lo necesitas:
	    String message = "Ocurrió un error al enviar el correo. Por favor, reinténtelo más tarde.";
	    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL_EXCEPTION", message);
	}
	
	@ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        logger.warn("MissingServletRequestPartException capturada: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_PART", "Falta una parte requerida en la solicitud.");
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Excepción general capturada: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "GENERAL_EXCEPTION", "Ocurrió un error inesperado. Por favor, inténtelo más tarde.");
    }
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
	    logger.warn("IllegalArgumentException capturada: {}", ex.getMessage());
	    return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
	}

}
