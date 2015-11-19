package io.corbel.lib.rabbitmq.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.ErrorHandler;

public class RabbitMQConfigurer implements AmqpConfigurer {

	private final RabbitAdmin rabbitAdmin;
	private final ConnectionFactory connectionFactory;

	public RabbitMQConfigurer(RabbitAdmin rabbitAdmin, ConnectionFactory connectionFactory) {
		this.rabbitAdmin = rabbitAdmin;
		this.connectionFactory = connectionFactory;
	}

	@Override
	public TopicExchange topicExchange(String name, UnaryOperator<Exchange> modifier) {
		return configureExchange(modifier, new TopicExchange(name));
	}

	@Override
	public FanoutExchange fanoutExchange(String name, UnaryOperator<Exchange> modifier) {
		return configureExchange(modifier, new FanoutExchange(name));
	}

	@Override
	public Queue queue(String name, Function<Queue, Queue>... modifiers) {
		Queue queue = new Queue(name, true, false, false, new HashMap<>());
		if (modifiers != null) {
			modify(queue, modifiers);
		}

		rabbitAdmin.declareQueue(queue);
		return queue;
	}

	@Override
	public UnaryOperator<Exchange> alternateExchange(final String name) {
		return (Exchange exchange) -> {
			exchange.getArguments().put("alternate-exchange", name);
			return exchange;
		};
	}

	@Override
	public void bind(final String exchangeName, String destination, DestinationType destinationType,
			Optional<String> routingKey, Optional<Map<String, Object>> arguments) {
		rabbitAdmin.declareBinding(new Binding(destination, destinationType, exchangeName, routingKey.orElse(""),
				arguments.orElse(null)));
	}

	@Override
	public UnaryOperator<Queue> setDeadLetterExchange(String exchangeName) {
		return (Queue queue) -> {
			queue.getArguments().put("x-dead-letter-exchange", exchangeName);
			return queue;
		};
	}

	@Override
	public UnaryOperator<Queue> setTTL(int milliseconds) {
		return (Queue queue) -> {
			queue.getArguments().put("x-message-ttl", milliseconds);
			return queue;
		};
	}

	@Override
	public SimpleMessageListenerContainer listenerContainer(Executor executor,
			UnaryOperator<SimpleMessageListenerContainer> modifier, String... queueNames) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setTaskExecutor(executor);
		container.setQueueNames(queueNames);
		container.setErrorHandler(TaskUtils.LOG_AND_PROPAGATE_ERROR_HANDLER);
		modify(container, modifier);
		return container;
	}

	@Override
	public UnaryOperator<SimpleMessageListenerContainer> setRetryOpertations(RetryOperationsInterceptor interceptor) {
		return (SimpleMessageListenerContainer container) -> {
			container.setAdviceChain(new Advice[] { interceptor });
			return container;
		};
	}

	@Override
	public UnaryOperator<SimpleMessageListenerContainer> setErrorHandler(ErrorHandler errorHandler) {
		return (SimpleMessageListenerContainer container) -> {
			container.setErrorHandler(errorHandler);
			return container;
		};
	}

	private <T extends Exchange> T configureExchange(UnaryOperator<Exchange> modifier, T exchange) {
		modify(exchange, modifier);
		rabbitAdmin.declareExchange(exchange);
		return exchange;
	}

	private <T> void modify(T element, UnaryOperator<T> modifier) {
		if (modifier != null) {
			modifier.apply(element);
		}
	}

	private <T> void modify(T element, Function<T, T>... modifiers) {
		for(Function<T, T> modifier : modifiers) {
			modifier.apply(element);
		}
	}

}
