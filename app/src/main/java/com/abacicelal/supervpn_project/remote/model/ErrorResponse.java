package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * Backend ErrorResponse format - 4xx/5xx hatalarında dönen yapı.
 * GlobalExceptionHandler: { "timestamp", "status", "error", "message", "path" }
 */
public class ErrorResponse {

    @SerializedName("status")
    private int status;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("path")
    private String path;

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
