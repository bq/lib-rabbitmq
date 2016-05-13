package com.bq.corbel.lib.rabbitmq.config;

@FunctionalInterface
public interface AmqpConfiguration {

	void configure(AmqpConfigurer configurer);

}
