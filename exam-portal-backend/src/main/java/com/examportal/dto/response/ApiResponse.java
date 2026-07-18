package com.examportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "success",
        "status",
        "message",
        "data",
        "errors",
        "timestamp",
        "path"
})
public class ApiResponse<T> {

     private boolean success;
     private int status;
     private String message;
     private T data;

     @JsonInclude(JsonInclude.Include.NON_EMPTY)
     private List<FieldError>  errors;

     @Builder.Default
     private String timeStamp = Instant.now().toString();

     private String path;

     public static <T> ApiResponse<T> success(String message, T data){

          return ApiResponse.<T> builder()
                  .success(true)
                  .status(200)
                  .message(message)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T>  ApiResponse<T>  success(String message){

          return ApiResponse.<T>builder()
                  .success(true)
                  .status(200)
                  .message(message)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T> ApiResponse<T> created(String message,T data){

          return ApiResponse.<T>builder()
                  .success(true)
                  .status(201)
                  .message(message)
                  .data(data)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T> ApiResponse<T> error(String message){

          return ApiResponse.<T>builder()
                  .success(false)
                  .status(400)
                  .message(message)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T> ApiResponse<T> validationError(String message,List<FieldError> errors){

          return  ApiResponse.<T>builder()
                  .success(false)
                  .status(400)
                  .message(message)
                  .errors(errors)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T> ApiResponse<T>  unauthorized(String message){

          return ApiResponse.<T>builder()
                  .success(false)
                  .status(401)
                  .message(message)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T> ApiResponse<T> forbidden(String message){

          return ApiResponse.<T>builder()
                  .success(false)
                  .status(403)
                  .message(message)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T> ApiResponse<T> notFound(String message){

          return ApiResponse.<T>builder()
                  .success(false)
                  .status(404)
                  .message(message)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T> ApiResponse<T> conflict(String message){

          return ApiResponse.<T>builder()
                  .success(false)
                  .status(409)
                  .message(message)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T> ApiResponse<T> internalError(String message){

          return ApiResponse.<T>builder()
                  .success(false)
                  .status(500)
                  .message(message)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public static <T> ApiResponse<T> serviceUnavailable(String message){

          return ApiResponse.<T>builder()
                  .success(false)
                  .status(503)
                  .message(message)
                  .timeStamp(Instant.now().toString())
                  .build();
     }

     public ApiResponse<T> withPath(String path){

          this.path = path;
          return this;
     }

     public boolean hasErrors(){

          return errors != null && !errors.isEmpty();
     }

     public boolean hasData(){

          return data != null;
     }

     @Data
     @Builder
     @AllArgsConstructor
     @NoArgsConstructor
     @JsonInclude(JsonInclude.Include.NON_NULL)
     public static class FieldError{

          private String field;
          private String message;
          private Object rejectedValue;

          public FieldError(String field, String message){

               this.field  = field;
               this.message = message;
          }
     }
}
