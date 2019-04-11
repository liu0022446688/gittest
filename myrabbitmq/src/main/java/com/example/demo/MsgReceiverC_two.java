package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
//比如一个生产者，多个消费者，可以写多个消费者，并且他们的分发是负载均衡的。
@Component
@RabbitListener(queues = RabbitConfig.QUEUE_A)
public class MsgReceiverC_two {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RabbitHandler
	public void process(String content) {
		logger.info("处理器two接收处理队列A当中的消息： " + content);
	}
}
