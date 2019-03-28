package com.nasnav.exceptions;

import com.nasnav.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@EnableWebMvc
@Slf4j
public class ErrorResponseHandler extends ResponseEntityExceptionHandler {

    private final Logger exceptionLogger = LoggerFactory.getLogger(ErrorResponseHandler.class.getName());

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException e, WebRequest request) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getErrorMessage(), e.getErrorCode());

        return new ResponseEntity<>(errorResponseDTO, e.getHttpStatus() != null ? e.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception e, WebRequest request) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<ErrorResponseDTO> handleThrowable(Throwable e, WebRequest request) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle EntityValidationException exception
     *
     * @param ex      EntityValidationException to be handled
     * @param request WebRequest that result in that EntityValidationException
     * @return ApiResponse object to the requester
     */
    @ExceptionHandler(EntityValidationException.class)
    public final ResponseEntity<ApiResponse> handleValidationException(EntityValidationException ex, WebRequest request) {
        logException(request, ex);
        return new ResponseEntity<ApiResponse>(ex.getApiResponse(), ex.getHttpStatus());
    }
    /**
     * Log failed request with exception details
     *
     * @param request WebRequest that result in that Exception
     * @param ex      Exception to be handled
     */
    private void logException(WebRequest request, Exception ex) {
        exceptionLogger.error(" Exception: Unable to process this request :  " + request.getDescription(false), ex);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e,
                                                                      HttpHeaders headers, HttpStatus status, WebRequest request) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e,
                                                                     HttpHeaders headers, HttpStatus status, WebRequest request) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE.name(), HttpStatus.UNSUPPORTED_MEDIA_TYPE.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException e,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException e,
                                                                     HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException e, HttpHeaders headers,

                                                                   HttpStatus status, WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException e,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException e, HttpHeaders headers, HttpStatus status,
                                                        WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        for (org.springframework.validation.FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            stringBuilder.append(fieldError.getDefaultMessage()).append(", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(stringBuilder.toString(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);

    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException e,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                                         HttpHeaders headers, HttpStatus status, WebRequest request) {


        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.METHOD_NOT_ALLOWED.name(), HttpStatus.METHOD_NOT_ALLOWED.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.METHOD_NOT_ALLOWED);

    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException e, HttpHeaders headers, HttpStatus status,
                                                         WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        return super.handleAsyncRequestTimeoutException(ex, headers, status, webRequest);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return super.handleHttpMessageNotWritable(ex, headers, status, request);
    }
}
