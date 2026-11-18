package kr.ac.ync.live.global.exception;

/** Common error body: {"status": 409, "code": "ALREADY_VOTED", "message": "..."} */
public record ErrorResponse(int status, String code, String message) {
}
