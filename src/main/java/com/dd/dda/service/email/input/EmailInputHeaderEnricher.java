package com.dd.dda.service.email.input;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;


@MessageEndpoint
public class EmailInputHeaderEnricher implements GenericHandler<Object> {

    @Override
    public Object handle(Object payload, MessageHeaders headers) {
        MessageBuilder<Object> responseBuilder = MessageBuilder.withPayload(payload).copyHeaders(headers);
        return responseBuilder.build();
    }

}
