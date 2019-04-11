package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.rabbitmq.client.Channel;
//编写RabbitConfig类，类里面设置很多个EXCHANGE,QUEUE,ROUTINGKEY，是为了接下来的不同使用场景。/**Broker:它提供一种传输服务,它的角色就是维护一条从生产者到消费者的路线，保证数据能按照指定的方式进行传输, Exchange：消息交换机,它指定消息按什么规则,路由到哪个队列。 Queue:消息的载体,每个消息都会被投到一个或多个队列。 Binding:绑定，它的作用就是把exchange和queue按照路由规则绑定起来. Routing Key:路由关键字,exchange根据这个关键字进行消息投递。 vhost:虚拟主机,一个broker里可以有多个vhost，用作不同用户的权限分离。 Producer:消息生产者,就是投递消息的程序. Consumer:消息消费者,就是接受消息的程序. Channel:消息通道,在客户端的每个连接里,可建立多个channel.*/

@Configuration
public class RabbitConfig {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${spring.rabbitmq.host}")
	private String host;
	@Value("${spring.rabbitmq.port}")
	private int port;
	@Value("${spring.rabbitmq.username}")
	private String username;
	@Value("${spring.rabbitmq.password}")
	private String password;
	public static final String EXCHANGE_A = "my-mq-exchange_A";
	public static final String EXCHANGE_B = "my-mq-exchange_B";
	public static final String EXCHANGE_C = "my-mq-exchange_C";
	public static final String QUEUE_A = "QUEUE_A";
	public static final String QUEUE_B = "QUEUE_B";
	public static final String QUEUE_C = "QUEUE_C";
	public static final String ROUTINGKEY_A = "spring-boot-routingKey_A";
	public static final String ROUTINGKEY_B = "spring-boot-routingKey_B";
	public static final String ROUTINGKEY_C = "spring-boot-routingKey_C";
	private static final String FANOUT_EXCHANGE = null;

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost("/");
		connectionFactory.setPublisherConfirms(true);
		return connectionFactory;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	// 必须是prototype类型
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		return template;
	}

	/**
	 * * 针对消费者配置 * 1. 设置交换机类型 * 2. 将队列绑定到交换机 FanoutExchange:
	 * 将消息分发到所有的绑定队列，无routingkey的概念 HeadersExchange ：通过添加属性key-value匹配
	 * DirectExchange:按照routingkey分发到指定队列 TopicExchange:多关键字匹配
	 */
	//把交换机，队列，通过路由关键字进行绑定，写在RabbitConfig类当中
	@Bean
	public DirectExchange defaultExchange() {
		return new DirectExchange(EXCHANGE_A);
	}

	/** * 获取队列A * @return */
	@Bean
	public Queue queueA() {
		return new Queue(QUEUE_A, true); // 队列持久
	}

	@Bean
	public Binding binding() {
		return BindingBuilder.bind(queueA()).to(defaultExchange()).with(RabbitConfig.ROUTINGKEY_A);
	}
//一个交换机可以绑定多个消息队列，也就是消息通过一个交换机，可以分发到不同的队列当中去。
	@Bean
	public Binding bindingB() {
		return BindingBuilder.bind(queueB()).to(defaultExchange()).with(RabbitConfig.ROUTINGKEY_B);
	}

	private Queue queueB() {
		return new Queue(QUEUE_B, true); // 队列持久
	}

	//另外一种消息处理机制的写法如下，在RabbitMQConfig类里面增加bean：
	@Bean
	public SimpleMessageListenerContainer messageContainer() {
		// 加载处理消息A的队列
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
		// 设置接收多个队列里面的消息，这里设置接收队列A
		// 假如想一个消费者处理多个队列里面的信息可以如下设置： //container.setQueues(queueA(),queueB(),queueC());
		container.setQueues(queueA());
		container.setExposeListenerChannel(true);
		// 设置最大的并发的消费者数量
		container.setMaxConcurrentConsumers(10);
		// 最小的并发消费者的数量
		container.setConcurrentConsumers(1);
		// 设置确认模式手工确认
		container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		container.setMessageListener(new ChannelAwareMessageListener() {
			@Override
			public void onMessage(Message message, Channel channel) throws Exception {
				/**
				 * 通过basic.qos方法设置prefetch_count=1，这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message，
				 * 换句话说,在接收到该Consumer的ack前,它不会将新的Message分发给它
				 */
				channel.basicQos(1);
				byte[] body = message.getBody();
				logger.info("接收处理队列A当中的消息:" + new String(body));
				/**
				 * 为了保证永远不会丢失消息，RabbitMQ支持消息应答机制。
				 * 当消费者接收到消息并完成任务后会往RabbitMQ服务器发送一条确认的命令，然后RabbitMQ才会将消息删除。
				 */
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			}

		});
		return container;
	}
/*
 * Fanout Exchange
   Fanout 就是我们熟悉的广播模式，给Fanout交换机发送消息，绑定了这个交换机的所有队列都收到这个消息。
 */
	// 配置fanout_exchange
	@Bean
	FanoutExchange fanoutExchange() {
		return new FanoutExchange(RabbitConfig.FANOUT_EXCHANGE);
	}

	// 把所有的队列都绑定到这个交换机上去
	@Bean
	Binding bindingExchangeA(Queue queueA, FanoutExchange fanoutExchange) {
		return BindingBuilder.bind(queueA).to(fanoutExchange);
	}

	@Bean
	Binding bindingExchangeB(Queue queueB, FanoutExchange fanoutExchange) {
		return BindingBuilder.bind(queueB).to(fanoutExchange);
	}

	@Bean
	Binding bindingExchangeC(Queue queueC, FanoutExchange fanoutExchange) {
		return BindingBuilder.bind(queueC).to(fanoutExchange);
	}

}
