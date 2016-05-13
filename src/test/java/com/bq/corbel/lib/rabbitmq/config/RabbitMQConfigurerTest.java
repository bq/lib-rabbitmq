package com.bq.corbel.lib.rabbitmq.config;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

public class RabbitMQConfigurerTest {

	private RabbitMQConfigurer configurer;
	private RabbitAdmin rabbitAdminMock;
	private ConnectionFactory connectionFactory;

	private final String EXCHANGE_TEST = "test.exchange";
	private final String EXCHANGE2_TEST = "test2.exchange";
	private final String QUEUE_TEST = "test.queue";
	private final String ROUTING_PATTERN_TEST = "test.routing.pattern";

	@Before
	public void setup() {
		rabbitAdminMock = mock(RabbitAdmin.class);
		connectionFactory = mock(ConnectionFactory.class);

		int corePoolSize = 1;
		int maxPoolSize = 1;
		long keepAliveTime = 1;

		configurer = new RabbitMQConfigurer(rabbitAdminMock, connectionFactory);
	}

	@Test
	public void testBasicConfiguration() {
		ArgumentCaptor<Exchange> exchangeCaptor = ArgumentCaptor.forClass(Exchange.class);
		ArgumentCaptor<Queue> evciUnknownQueueCaptor = ArgumentCaptor.forClass(Queue.class);
		ArgumentCaptor<Binding> bindingCaptor = ArgumentCaptor.forClass(Binding.class);

		configurer
				.topicExchange(EXCHANGE_TEST, configurer.alternateExchange(configurer.fanoutExchange(EXCHANGE2_TEST)));

		configurer.bind(EXCHANGE2_TEST, configurer.queue(QUEUE_TEST), Optional.<String> empty(),
				Optional.<Map<String, Object>> empty());

		verify(rabbitAdminMock, times(2)).declareExchange(exchangeCaptor.capture());
		verify(rabbitAdminMock).declareQueue(evciUnknownQueueCaptor.capture());
		verify(rabbitAdminMock).declareBinding(bindingCaptor.capture());

		List<Exchange> capturedExchanges = exchangeCaptor.getAllValues();
		assertThat(capturedExchanges.get(0).getName()).isEqualTo(EXCHANGE2_TEST);
		assertThat(capturedExchanges.get(0).getType()).isEqualToIgnoringCase("fanout");

		assertThat(capturedExchanges.get(1).getName()).isEqualTo(EXCHANGE_TEST);
		assertThat(capturedExchanges.get(1).getType()).isEqualToIgnoringCase("topic");
		assertThat(capturedExchanges.get(1).getArguments().get("alternate-exchange")).isEqualTo(EXCHANGE2_TEST);

		assertThat(evciUnknownQueueCaptor.getValue().getName()).isEqualTo(QUEUE_TEST);

		Binding capturedBinding = bindingCaptor.getValue();
		assertThat(capturedBinding.getExchange()).isEqualTo(EXCHANGE2_TEST);
		assertThat(capturedBinding.getDestination()).isEqualTo(QUEUE_TEST);
		assertThat(capturedBinding.getDestinationType()).isEqualTo(DestinationType.QUEUE);
	}

	@Test
	public void testAlternateExchange() {
		configurer
				.topicExchange(EXCHANGE_TEST, configurer.alternateExchange(configurer.fanoutExchange(EXCHANGE2_TEST)));
		ArgumentCaptor<Exchange> exchangeCaptor = ArgumentCaptor.forClass(Exchange.class);
		verify(rabbitAdminMock, times(2)).declareExchange(exchangeCaptor.capture());
		assertThat(exchangeCaptor.getAllValues().get(1).getArguments().get("alternate-exchange")).isEqualTo(
				EXCHANGE2_TEST);
	}

	@Test
	public void testDeadLetterExchange() {
		configurer.queue(QUEUE_TEST, configurer.setDeadLetterExchange(EXCHANGE2_TEST));
		ArgumentCaptor<Queue> queueCaptor = ArgumentCaptor.forClass(Queue.class);
		verify(rabbitAdminMock, times(1)).declareQueue(queueCaptor.capture());
		assertThat(queueCaptor.getValue().getArguments().get("x-dead-letter-exchange")).isEqualTo(EXCHANGE2_TEST);
	}

	@Test
	public void testBind() {
		configurer.bind(EXCHANGE_TEST, QUEUE_TEST, DestinationType.QUEUE, Optional.of(ROUTING_PATTERN_TEST),
				Optional.empty());
		ArgumentCaptor<Binding> bindingCaptor = ArgumentCaptor.forClass(Binding.class);
		verify(rabbitAdminMock).declareBinding(bindingCaptor.capture());
		assertThat(bindingCaptor.getValue().getExchange()).isEqualTo(EXCHANGE_TEST);
		assertThat(bindingCaptor.getValue().getDestination()).isEqualTo(QUEUE_TEST);
		assertThat(bindingCaptor.getValue().getDestinationType()).isEqualTo(DestinationType.QUEUE);
		assertThat(bindingCaptor.getValue().getRoutingKey()).isEqualTo(ROUTING_PATTERN_TEST);
	}

	@Test
	public void testBindQueue() {
		configurer.bind(EXCHANGE_TEST, configurer.queue(QUEUE_TEST), Optional.of(ROUTING_PATTERN_TEST),
				Optional.empty());
		ArgumentCaptor<Binding> bindingCaptor = ArgumentCaptor.forClass(Binding.class);
		verify(rabbitAdminMock).declareBinding(bindingCaptor.capture());
		assertThat(bindingCaptor.getValue().getExchange()).isEqualTo(EXCHANGE_TEST);
		assertThat(bindingCaptor.getValue().getDestination()).isEqualTo(QUEUE_TEST);
		assertThat(bindingCaptor.getValue().getDestinationType()).isEqualTo(DestinationType.QUEUE);
		assertThat(bindingCaptor.getValue().getRoutingKey()).isEqualTo(ROUTING_PATTERN_TEST);
	}

	@Test
	public void testBindEmptyRoutingKey() {
		configurer.bind(EXCHANGE_TEST, QUEUE_TEST, DestinationType.QUEUE, Optional.empty(), Optional.empty());
		ArgumentCaptor<Binding> bindingCaptor = ArgumentCaptor.forClass(Binding.class);
		verify(rabbitAdminMock).declareBinding(bindingCaptor.capture());
		assertThat(bindingCaptor.getValue().getExchange()).isEqualTo(EXCHANGE_TEST);
		assertThat(bindingCaptor.getValue().getDestination()).isEqualTo(QUEUE_TEST);
		assertThat(bindingCaptor.getValue().getDestinationType()).isEqualTo(DestinationType.QUEUE);
		assertThat(bindingCaptor.getValue().getRoutingKey()).isEqualTo("");
	}

	@Test
	public void testDeadLetterE() {
		configurer.bind(EXCHANGE_TEST, QUEUE_TEST, DestinationType.QUEUE, Optional.of(ROUTING_PATTERN_TEST),
				Optional.empty());
		ArgumentCaptor<Binding> bindingCaptor = ArgumentCaptor.forClass(Binding.class);
		verify(rabbitAdminMock).declareBinding(bindingCaptor.capture());
		assertThat(bindingCaptor.getValue().getExchange()).isEqualTo(EXCHANGE_TEST);
		assertThat(bindingCaptor.getValue().getDestination()).isEqualTo(QUEUE_TEST);
		assertThat(bindingCaptor.getValue().getDestinationType()).isEqualTo(DestinationType.QUEUE);
		assertThat(bindingCaptor.getValue().getRoutingKey()).isEqualTo(ROUTING_PATTERN_TEST);
	}

}
