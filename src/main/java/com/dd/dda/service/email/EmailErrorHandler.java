package com.dd.dda.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

@Slf4j
@MessageEndpoint
public class EmailErrorHandler extends AbstractMessageHandler/*implements GenericHandler<MessagingException>*/ {


    @Override
    protected void handleMessageInternal(Message<?> message) {
        MessagingException payload = (MessagingException) message.getPayload();
        log.error("Error happened during job integration... => {} ", payload.getLocalizedMessage());
        log.error("Cause {}", payload.getCause());


    }
}
