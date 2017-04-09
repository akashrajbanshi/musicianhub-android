package com.project.musicianhub.model;

/**
 * Model class for the custom Response
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class CustomResponse {

    private String code;
    private String message;

    public CustomResponse() {
    }

    public CustomResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
