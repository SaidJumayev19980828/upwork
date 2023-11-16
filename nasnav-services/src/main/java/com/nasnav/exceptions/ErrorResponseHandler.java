package com.nasnav.exceptions;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.response.ImportProcessStatusResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.security.oauth2.exceptions.InCompleteOAuthRegistration;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
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
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;

import java.io.FileNotFoundException;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ErrorResponseHandler extends ResponseEntityExceptionHandler {
	
	

	private final Logger exceptionLogger = LoggerFactory.getLogger(ErrorResponseHandler.class.getName());
	
	
	
	
	@ExceptionHandler(ImportProductException.class)
	@ResponseBody
	public ResponseEntity<ImportProductContext> handleImportProductException(ImportProductException e, WebRequest requestInfo , HttpServletRequest request) {
		logException(requestInfo, request , e);
		return new ResponseEntity<>(e.getContext(), NOT_ACCEPTABLE);
	}
	
	
	
	
	@ExceptionHandler(ImportProductRuntimeException.class)
	@ResponseBody
	public ResponseEntity<ImportProductContext> handleImportProductRunException(ImportProductRuntimeException e, WebRequest requestInfo , HttpServletRequest request) {
		logException(requestInfo, request , e);
		e.getContext().logNewError(e, e.getMessage(), -1);
		return new ResponseEntity<>(e.getContext(), NOT_ACCEPTABLE);
	}
	
	
	
	@ExceptionHandler(ImportImageBulkRuntimeException.class)
	@ResponseBody
	public ResponseEntity<ImageImportBulkErrorResponse> handleImageImportBulkExceptionInterface(ImportImageBulkRuntimeException e, WebRequest requestInfo , HttpServletRequest request) {
		logException(requestInfo, request , e);
		return new ResponseEntity<>(new ImageImportBulkErrorResponse(e.getErrors()), INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(DateTimeParseException.class)
	@ResponseBody
	public ResponseEntity<ErrorResponseDTO> handleDateTimeException(DateTimeParseException e, WebRequest requestInfo, HttpServletRequest request) {
		logException(requestInfo, request, e);
		ErrorCodes errCode = ErrorCodes.DATE$TIME$0002;
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(errCode.getValue(), errCode.name());
		return new ResponseEntity<>(errorResponseDTO, BAD_REQUEST);
	}




	@ExceptionHandler(RuntimeBusinessException.class)
	@ResponseBody
	public ResponseEntity<ErrorResponseDTO> handleBusinessExceptionInterface(RuntimeBusinessException e, WebRequest requestInfo , HttpServletRequest request) {
		logException(requestInfo, request , e);
		
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getErrorMessage(), e.getErrorCode());

		return new ResponseEntity<>(errorResponseDTO,
				e.getHttpStatus() != null ? e.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
	
	
	@ExceptionHandler(BusinessException.class)
	@ResponseBody
	public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException e, WebRequest requestInfo , HttpServletRequest request) {
		logException(requestInfo, request , e);
		
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getErrorMessage(), e.getErrorCode());

		return new ResponseEntity<>(errorResponseDTO,
				e.getHttpStatus() != null ? e.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(DataImportAsyncException.class)
	@ResponseBody
	public ResponseEntity<ImportProcessStatusResponse> handleDataImportAsyncException(DataImportAsyncException e,
			WebRequest requestInfo, HttpServletRequest request) {

		logException(requestInfo, request, e);

		return new ResponseEntity<>(e.getStatusResponse(), HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler(FileNotFoundException.class)
	@ResponseBody
	public ResponseEntity<ErrorResponseDTO> handleFileNotFound(FileNotFoundException e, WebRequest requestInfo , HttpServletRequest request) {
		logException(requestInfo, request, e);
		ErrorCodes errorCode = ErrorCodes.GEN$0011;
		// I don't like the following hardcoding but no easy way around it
		String uri = request.getRequestURI().replaceFirst("/?(v1)?/files", "");
		String errorMessage = String.format(errorCode.getValue(), uri);
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(errorMessage, errorCode.name());
		return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND);
	}
	
	

	@ExceptionHandler(Throwable.class)
	@ResponseBody
	public ResponseEntity<ErrorResponseDTO> handleThrowable(Throwable e, WebRequest requestInfo ,HttpServletRequest request) {
		logException(requestInfo, request , e);

		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage());

		return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
	

	/**
	 * Handle EntityValidationException exception
	 *
	 * @param ex      EntityValidationException to be handled
	 * @param requestInfo WebRequest that result in that EntityValidationException
	 * @return UserApiResponse object to the requester
	 */
	@ExceptionHandler(EntityValidationException.class)
	public final ResponseEntity<UserApiResponse> handleValidationException(EntityValidationException ex,
			WebRequest requestInfo , HttpServletRequest request) {
		logException(requestInfo, request , ex);
		return new ResponseEntity<UserApiResponse>(ex.getUserApiResponse(), ex.getHttpStatus());
	}

	@ExceptionHandler(InCompleteOAuthRegistration.class)
	public final ResponseEntity<Object> handleInCompleteOAuthRegistration(InCompleteOAuthRegistration ex,
			WebRequest requestInfo, HttpServletRequest request) {
		logException(requestInfo, request, ex);
		return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
				.header(HttpHeaders.LOCATION, EntityConstants.OAUTH_ENTER_EMAIL_PAGE + ex.getSocialLoginToken())
				.build();
	}
	
	
	
	
	/**
	 * Log failed request with exception details
	 *
	 * @param requestInfo WebRequest that result in that Exception
	 * @param ex      Exception to be handled
	 */
	private void logException(WebRequest requestInfo, HttpServletRequest request, Throwable ex) {
		StringBuilder msg = new StringBuilder( );
		
		Map<String, String> paramMap = readParametersMap(requestInfo);								
		
		msg.append(" Exception: Unable to process this request :  ")
			.append(requestInfo.getDescription(false))
			.append("\nWith Parameters: ")
			.append(paramMap);
		
		
		//This will work only if we add RequestBodyCachingFilter to the request filters, which
		//wraps the request as ContentCachingRequestWrapper.
		//for more info refer to RequestBodyCachingFilter javadoc
		if(request != null && request instanceof ContentCachingRequestWrapper){	
			ContentCachingRequestWrapper req = (ContentCachingRequestWrapper)request;
			String requestBody = new String( req.getContentAsByteArray());
			
			msg.append("\n>> Request Body: ");
			msg.append(requestBody);
		}
						
		
		exceptionLogger.error(msg.toString() , ex);
	}





	private Map<String, String> readParametersMap(WebRequest requestInfo) {
		return requestInfo.getParameterMap().entrySet().stream()
				.map(this::getRequstParamAsStr)
				.collect(Collectors.toMap(Entry<String, String>::getKey, Entry<String, String>::getValue));
	}





	private Entry<String, String> getRequstParamAsStr(Entry<String, String[]> e) {
		return new AbstractMap.SimpleEntry<String, String>(e.getKey(), Arrays.toString(e.getValue()));
	}
	
	

	/**
	 * Log failed request with exception details
	 *
	 * @param requestInfo WebRequest that result in that Exception
	 * @param ex      Exception to be handled
	 */
	private void logException(WebRequest requestInfo, Throwable ex) {
		logException(requestInfo, null, ex);
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		return createBadRequestHttpResponse(e);
	}
	
	
	
	
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(),
				HttpStatus.UNSUPPORTED_MEDIA_TYPE.name(), HttpStatus.UNSUPPORTED_MEDIA_TYPE.name());

		return new ResponseEntity<>(errorResponseDTO, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException e,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		return createBadRequestHttpResponse(e);
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException e, HttpHeaders headers,
			HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		return createBadRequestHttpResponse(e);		
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException e,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		return createBadRequestHttpResponse(e);	
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException e, HttpHeaders headers,

			HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		return createBadRequestHttpResponse(e);
	}
	
	
	
	

	@Override
	protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException e,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		return createBadRequestHttpResponse(e);
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException e, HttpHeaders headers, HttpStatus status,
			WebRequest requestInfo) {
		logException(requestInfo, e);
		
		return createBadRequestHttpResponse(e);
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		StringBuilder stringBuilder = new StringBuilder();
		for (org.springframework.validation.FieldError fieldError : e.getBindingResult().getFieldErrors()) {
			stringBuilder.append(fieldError.getDefaultMessage()).append(", ");
		}
		stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
		
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(stringBuilder.toString(),
				HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.name());

		return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);

	}
	
	
	
	

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException e,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		return createBadRequestHttpResponse(e);
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.METHOD_NOT_ALLOWED.name(),
				HttpStatus.METHOD_NOT_ALLOWED.name());

		return new ResponseEntity<>(errorResponseDTO, HttpStatus.METHOD_NOT_ALLOWED);

	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, e);
		
		if (((ServletWebRequest) requestInfo).getRequest().getServletPath().contains("/user")) {
			List<ResponseStatus> respStatuses = new ArrayList<ResponseStatus>(1);
			respStatuses.add(ResponseStatus.INVALID_PARAMETERS);

			return new ResponseEntity<>(new UserApiResponse(respStatuses), HttpStatus.NOT_ACCEPTABLE);
		}
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(),
				HttpStatus.BAD_REQUEST.name());

		return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleBindException(BindException e, HttpHeaders headers, HttpStatus status,
			WebRequest requestInfo) {
		logException(requestInfo, e);
		
		return createBadRequestHttpResponse(e);		
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex,
			HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
		logException(webRequest, ex);
		return super.handleAsyncRequestTimeoutException(ex, headers, status, webRequest);
	}
	
	
	

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, ex);
		return super.handleExceptionInternal(ex, body, headers, status, requestInfo);
	}
	
	
	
	

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest requestInfo) {
		logException(requestInfo, ex);
		
		return super.handleHttpMessageNotWritable(ex, headers, status, requestInfo);
	}
	
	
	


	private ResponseEntity<Object> createBadRequestHttpResponse(Exception e) {
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.name(),
				HttpStatus.BAD_REQUEST.name());

		return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
	}
}
