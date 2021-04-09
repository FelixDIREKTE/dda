package com.dd.dda.service.email.output;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;

@Slf4j
@MessageEndpoint
public class EmailJobFinishHandler implements GenericHandler<Object> {


    @Override
    public Object handle(Object payload, MessageHeaders headers) {
        log.info("payload => {}", payload);
        log.info("MessageHeaders => {}", headers);
        return payload;
    }
}
