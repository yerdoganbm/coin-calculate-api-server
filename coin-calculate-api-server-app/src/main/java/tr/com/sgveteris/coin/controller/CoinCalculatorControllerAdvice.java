package tr.com.sgveteris.coin.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tr.com.sgveteris.coin.enumeration.EnumResponseCode;
import tr.com.sgveteris.coin.exception.CoinCalculateError;
import tr.com.sgveteris.coin.exception.CoinCalculateException;
import tr.com.sgveteris.coin.exception.CoinCalculateExceptionContext;
import tr.com.sgveteris.coin.logging.CorrelationData;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class CoinCalculatorControllerAdvice extends ResponseEntityExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = CoinCalculateException.class)
    public ResponseEntity<Object> kolayPacksException(CoinCalculateException ex, WebRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("Exception: ", ex);
        }

        return ResponseEntity.badRequest().body(buildError(ex, ex.getRc()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("Exception: ", ex);
        }
        return ResponseEntity.badRequest().body(buildError(ex, EnumResponseCode.RC_INVALID_REQUEST));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("Exception: ", ex);
        }
        return ResponseEntity.badRequest().body(buildError(ex, EnumResponseCode.RC_INVALID_REQUEST));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return getInvalidRequest(ex);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return getInvalidRequest(ex);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return getInvalidRequest(ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return getInvalidRequest(ex);
    }

    private ResponseEntity<Object> getInvalidRequest(Exception ex) {
        if (log.isDebugEnabled()) {
            log.debug("Exception: ", ex);
        }
        return ResponseEntity.badRequest().body(buildError(ex, EnumResponseCode.RC_INVALID_REQUEST));
    }

    private CoinCalculateError buildError(Exception ex, EnumResponseCode rc) {
        CoinCalculateError error;
        CoinCalculateError transactionErrorFromContext = CoinCalculateExceptionContext.getCoinCalculateError();

        if (transactionErrorFromContext != null) {
            error = transactionErrorFromContext;
        } else {
            error = new CoinCalculateError();
            error.setRequestTimestamp(LocalDateTime.now());

            error.setCorrelationId(CorrelationData.getId());
        }

        error.setResponseCode(rc.getRc());
        error.setResponseDesc(rc.getRcDesc());

        if (StringUtils.isNotEmpty(ex.getMessage()))
            error.setErrorDetails(ex.getMessage());

        CoinCalculateExceptionContext.clear();

        return error;
    }

}
