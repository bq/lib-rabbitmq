package io.corbel.lib.rabbitmq.config;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder.StatelessRetryInterceptorBuilder;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.util.ErrorHandler;

public interface AmqpConfigurer {

	TopicExchange topicExchange(String name, UnaryOperator<Exchange> modifier);

	default TopicExchange topicExchange(String name) {
		return topicExchange(name, null);
	}

	FanoutExchange fanoutExchange(String name, UnaryOperator<Exchange> modifier);

	default FanoutExchange fanoutExchange(String name) {
		return fanoutExchange(name, null);
	}

	UnaryOperator<Exchange> alternateExchange(final String name);

	default UnaryOperator<Exchange> alternateExchange(final Exchange alterExchange) {
		return alternateExchange(alterExchange.getName());
	}

	Queue queue(String name, Function<Queue, Queue>... modifiers);

	default Queue queue(String name) {
		return queue(name, null);
	}

	void bind(final String exchangeName, final String destination, final DestinationType destinationType,
			Optional<String> routingKey, Optional<Map<String, Object>> arguments);

	default void bind(final String exchangeName, final Queue destination, Optional<String> routingKey,
			Optional<Map<String, Object>> arguments) {
		bind(exchangeName, destination.getName(), DestinationType.QUEUE, routingKey, arguments);
	}

	UnaryOperator<Queue> setDeadLetterExchange(String exchangeName);

	default UnaryOperator<Queue> setDeadLetterExchange(Exchange exchange) {
		return setDeadLetterExchange(exchange.getName());
	}

	UnaryOperator<Queue> setTimeToLive(int milliseconds);

	SimpleMessageListenerContainer listenerContainer(Executor executor,
			UnaryOperator<SimpleMessageListenerContainer> modifier, String... queueNames);

	default SimpleMessageListenerContainer listenerContainer(UnaryOperator<SimpleMessageListenerContainer> modifier,
			String... queueNames) {
		return listenerContainer(Executors.newSingleThreadExecutor(), modifier, queueNames);
	}

	default SimpleMessageListenerContainer listenerContainer(String... queueNames) {
		return listenerContainer(Executors.newSingleThreadExecutor(), null, queueNames);
	}

	default SimpleMessageListenerContainer listenerContainer(Executor executor, String... queueNames) {
		return listenerContainer(executor, null, queueNames);
	}

	UnaryOperator<SimpleMessageListenerContainer> setRetryOpertations(RetryOperationsInterceptor interceptor);

	UnaryOperator<SimpleMessageListenerContainer> setErrorHandler(ErrorHandler errorHandler);

	default UnaryOperator<SimpleMessageListenerContainer> setRetryOpertations(Optional<Integer> maxAttempts,
			Optional<BackoffOptions> backoffOptions) {
		StatelessRetryInterceptorBuilder builder = RetryInterceptorBuilder.stateless();
		if (maxAttempts.isPresent()) {
			builder.maxAttempts(maxAttempts.get());
		}
		if (backoffOptions.isPresent()) {
			BackoffOptions options = backoffOptions.get();
			builder.backOffOptions(options.getInitialInterval(), options.getMultiplier(), options.getMaxInterval());
		}
		builder.recoverer(new RejectAndDontRequeueRecoverer());
		return setRetryOpertations(builder.build());
	}
}