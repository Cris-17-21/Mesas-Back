package com.restaurante.resturante.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.restaurante.resturante.dto.security.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Credenciales Inválidas",
                                "El nombre de usuario o la contraseña son incorrectos.",
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(TokenRefreshException.class)
        public ResponseEntity<ErrorResponse> handleTokenRefreshException(TokenRefreshException ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "Token de Refresco Inválido",
                                ex.getMessage(),
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Usuario no encontrado",
                                ex.getMessage(),
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "Acceso Denegado",
                                ex.getMessage(),
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
                ex.printStackTrace(); // DEBUG: Print stack trace to console
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Error en la Solicitud",
                                ex.getMessage(),
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Error Interno del Servidor",
                                "Ha ocurrido un error inesperado. Por favor, contacte al soporte.",
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        org.springframework.web.bind.MethodArgumentNotValidException ex) {

                String mensaje = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(java.util.stream.Collectors.joining(" | "));

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Error de Validación",
                                mensaje,
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Para manejar el ID no encontrado
        @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
                        jakarta.persistence.EntityNotFoundException ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Recurso no encontrado",
                                ex.getMessage(),
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // Para manejar conflictos de negocio
        // Nota: Usar Conflict (409) es más preciso semánticamente que Bad Request (400)
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "Conflicto de Negocio",
                                ex.getMessage(),
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        // Para manejar argumentos nulos
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Argumento Inválido",
                                ex.getMessage(),
                                Instant.now());
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
                Map<String, String> response = new HashMap<>();

                // Extraemos el mensaje raíz real de la base de datos (es más seguro)
                String rootMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage()
                                : ex.getMessage();

                // 1. Verificamos si es un error de llave foránea (Dependencias)
                if (rootMsg.contains("foreign key constraint fails")) {
                        response.put("error", "Conflicto de Integridad");
                        response.put("message",
                                        "No se puede eliminar el registro porque tiene dependencias activas (sucursales, usuarios, etc).");
                }
                // 2. Verificamos si es un error de valor duplicado (Unique Constraint)
                else if (rootMsg.contains("Duplicate entry")) {
                        response.put("error", "Dato Duplicado");

                        // Podemos hacer el mensaje amigable para el usuario
                        response.put("message",
                                        "Ya existe un registro con este dato único (Ej. DNI, Correo o Usuario). Por favor, verifica e intenta con otro.");

                        // OPCIONAL: Si quieres ponerte exquisito, puedes extraer el valor exacto usando
                        // un poquito de manipulación de strings o Regex.
                        // Pero un mensaje general como el de arriba suele ser suficiente para el
                        // Frontend.
                }
                // 3. Fallback genérico para cualquier otra cosa rara de la BD
                else {
                        response.put("error", "Error de base de datos");
                        response.put("message", "Operación no permitida por integridad de datos.");
                }

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
}
