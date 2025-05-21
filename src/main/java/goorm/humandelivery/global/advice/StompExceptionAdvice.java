package goorm.humandelivery.global.advice;

import goorm.humandelivery.global.exception.*;
import goorm.humandelivery.shared.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class StompExceptionAdvice {

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handle(Exception e) {
        log.error("WebSocket 처리 중 예외 발생", e);
        return new ErrorResponse("ERROR", e.getMessage());
    }

    @MessageExceptionHandler(OffDutyLocationUpdateException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleOffDutyLocationUpdateException(OffDutyLocationUpdateException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("OffDutyLocationUpdateException", ex.getMessage());
    }

    @MessageExceptionHandler(CustomerNotAssignedException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleCustomerNotAssignedException(CustomerNotAssignedException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("CustomerNotAssignedException", ex.getMessage());
    }

    @MessageExceptionHandler(CallAlreadyCompletedException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleCallAlreadyCompletedException(CallAlreadyCompletedException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("CallAlreadyCompletedException", ex.getMessage());
    }


    @MessageExceptionHandler(CallInfoEntityNotFoundException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleCallInfoEntityNotFoundException(CallInfoEntityNotFoundException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("CallInfoEntityNotFoundException", ex.getMessage());
    }

    @MessageExceptionHandler(TaxiDriverEntityNotFoundException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleTaxiDriverEntityNotFoundException(TaxiDriverEntityNotFoundException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("TaxiDriverEntityNotFoundException", ex.getMessage());
    }

    @MessageExceptionHandler(MatchingEntityNotFoundException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleMatchingEntityNotFoundException(MatchingEntityNotFoundException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("MatchingEntityNotFoundException", ex.getMessage());

    }

    @MessageExceptionHandler(NoAvailableTaxiException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleNoAvailableTaxiException(NoAvailableTaxiException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("NoAvailableTaxiException", ex.getMessage());
    }

    @MessageExceptionHandler(RedisKeyNotFoundException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleRedisKeyNotFoundException(RedisKeyNotFoundException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("RedisKeyNotFoundException", ex.getMessage());
    }


    @MessageExceptionHandler(LocationNotInRedisException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleLocationNotInRedisException(LocationNotInRedisException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("LocationNotInRedisException", ex.getMessage());
    }


    @MessageExceptionHandler(DrivingInfoEntityNotFoundException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleDrivingInfoEntityNotFoundException(DrivingInfoEntityNotFoundException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("DrivingInfoEntityNotFoundException", ex.getMessage());
    }

    @MessageExceptionHandler(IncorrectTaxiDriverStatusException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleIncorrectTaxiDriverStatusException(IncorrectTaxiDriverStatusException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("IncorrectTaxiDriverStatusException", ex.getMessage());
    }

    @MessageExceptionHandler(AlreadyAssignedCallException.class)
    @SendToUser("/queue/errors") // /user/queue/errors
    public ErrorResponse handleAlreadyAssignedCallException(AlreadyAssignedCallException ex) {
        log.error("exception: {}", ex.getClass());
        return new ErrorResponse("AlreadyAssignedCallException", ex.getMessage());
    }

}
