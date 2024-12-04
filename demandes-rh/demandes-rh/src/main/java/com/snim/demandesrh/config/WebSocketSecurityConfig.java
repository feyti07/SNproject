package com.snim.demandesrh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpDestMatchers("/app/**").permitAll() // Secure app destination
                .simpSubscribeDestMatchers("/topic/**").permitAll() // Secure subscriptions
                .anyMessage().permitAll();  // Ensure all other messages are authenticated
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;  // Disable same-origin policy
    }
}

