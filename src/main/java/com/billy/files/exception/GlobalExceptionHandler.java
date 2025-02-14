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

	@ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        logger.error("IOException capturada: ", ex);
        // Se reemplaza el mensaje detallado por uno genérico para evitar exponer detalles internos.
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "IO_EXCEPTION",
                "Ocurrió un error en el procesamiento del archivo."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

	@ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorResponse> handleMessagingException(MessagingException ex) {
        logger.error("MessagingException capturada: ", ex);
        // Se retorna un mensaje genérico sin detalles de la excepción.
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "MAIL_EXCEPTION",
                "Ocurrió un error al enviar el correo."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Excepción general capturada: ", ex);
        // Se devuelve un mensaje genérico sin detalles internos para excepciones inesperadas.
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "GENERAL_EXCEPTION",
                "Ocurrió un error inesperado. Por favor, inténtelo más tarde."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        logger.warn("MissingServletRequestPartException capturada: {}", ex.getMessage());
        //* Se utiliza un mensaje genérico para solicitudes mal formadas.
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "MISSING_PART",
                "Falta una parte requerida en la solicitud."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(org.springframework.mail.MailSendException.class)
    public ResponseEntity<ErrorResponse> handleMailSendException(org.springframework.mail.MailSendException ex) {
        logger.error("MailSendException capturada: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "MAIL_EXCEPTION",
                "Ocurrió un error al enviar el correo. Por favor, reinténtelo más tarde."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
