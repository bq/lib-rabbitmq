package com.bq.corbel.lib.rabbitmq.ioc;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.env.Environment;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Alexander De Leon <me@alexdeleon.name>
 */
public class AbstractRabbitMQConfigurationTest {


    @Test
    public void testConfigPrefix(){
        Environment envMock = mock(Environment.class);
        when(envMock.getProperty("test.rabbitmq.host")).thenReturn("TEST_HOST");

        AbstractRabbitMQConfiguration conf = new AbstractRabbitMQConfiguration() {
            @Override
            protected Environment getEnvironment() {
                return envMock;
            }

            @Override
            protected Optional<String> configPrefix() {
                return Optional.of("test");
            }
        };

        Assert.assertEquals("TEST_HOST", conf.getRabbitHost());

    }

    @Test
    public void testEmptyConfigPrefix(){
        Environment envMock = mock(Environment.class);
        when(envMock.getProperty("rabbitmq.host")).thenReturn("TEST_HOST");

        AbstractRabbitMQConfiguration conf = new AbstractRabbitMQConfiguration() {
            @Override
            protected Environment getEnvironment() {
                return envMock;
            }
        };

        Assert.assertEquals("TEST_HOST", conf.getRabbitHost());

    }
}