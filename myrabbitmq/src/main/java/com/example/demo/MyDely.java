package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;

public class MyDely {
	/*
	 * rabbitmq 实现延迟队列(ttl+DLX)
	 * 
	 * 利用死信队列加上生存时间,让信息在ttl完毕后到达指定队列. 所用到队列2个,
	 * 1,一个是具有死信功能携带ttl属性的队列,routingkey绑定到正常的队列里面.
	 * 2,发送消息到hello_delay里面,然后消费者监听hello队列.
	 */
	public static void main(String[] args) {
		setTTL();
		
		goDLX();
	}
	/*
	 * 死信交换机DLX
	 */
	private static void goDLX() {
		/*
		 * 队列中的消息在以下三种情况下会变成死信 （1）消息被拒绝（basic.reject 或者
		 * basic.nack），并且requeue=false; （2）消息的过期时间到期了； （3）队列长度限制超过了。
		 * 当队列中的消息成为死信以后，如果队列设置了DLX那么消息会被发送到DLX。通过x-dead-letter-exchange设置DLX，
		 * 通过这个x-dead-letter-routing-key设置消息发送到DLX所用的routing-key，
		 * 如果不设置默认使用消息本身的routing-key;
		 * 
		 */
		/*
		 * channel.exchangeDeclare("some.exchange.name", "direct");
		 * 
		 * Map<String, Object> obj = new HashMap<String, Object>();
		 * obj.put("x-dead-letter-exchange", "some.exchange.name");
		 * obj.put("x-dead-letter-routing-key", "some-routing-key");
		 * channel.queueDeclare("myqueue", false, false, false, obj);
		 */
	}

	/*
	 * 基于队列和基于消息的TTL
	 */
	private static void setTTL() {
		/*
		 * 通过设置队列的x-message-ttl参数来设置指定队列上消息的存活时间，其值是一个非负整数，单位为微秒。
		 * 不同队列的过期时间互相之间没有影响，即使是对于同一条消息。队列中的消息存在队列中的时间超过过期时间则成为死信。
		 */
		/*
		 * channel = null; Map<String, Object> obj = new HashMap<String, Object>();
		 * obj.put("x-message-ttl", 60000); channel.queueDeclare("myqueue", false,
		 * false, false, obj);
		 */
		/*
		 * 发送消息时可以单独指定每条消息的TTL，通过设置AMQP.Properties的expiration属性可以指定消息的过期时间。
		 * 注意当队列过期时间和消息过期时间都存在时，取两者中较短的时间。
		 */
		/*
		 * byte[] messageBodyBytes = "Hello, world!".getBytes(); AMQP.BasicProperties
		 * properties = new AMQP.BasicProperties(); properties.setExpiration("60000");
		 * channel.basicPublish("my-exchange", "routing-key", properties,
		 * messageBodyBytes);
		 */
	}
}
