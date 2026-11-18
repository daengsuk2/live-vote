package kr.ac.ync.live.global.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** REST controllers only: every failure leaves as an HTTP status code with the common error body. */
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApi(ApiException e) {
		return body(e.getStatus(), e.getCode(), e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(f -> f.getDefaultMessage())
				.orElse("잘못된 요청입니다.");
		return body(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
	}

	/** DB constraints (B4, B5, B6) are the last line of defence: a violation means "already done" -> 409. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException e) {
		return body(HttpStatus.CONFLICT, "CONFLICT", "이미 처리되었거나 충돌하는 요청입니다.");
	}

	private ResponseEntity<ErrorResponse> body(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status).body(new ErrorResponse(status.value(), code, message));
	}
}
