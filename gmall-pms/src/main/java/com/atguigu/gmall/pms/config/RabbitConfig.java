package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;
@Configuration
@Slf4j
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct // 构造方法执行之后就会执行
    // @PreDestroy // 对象销毁之前执行   <bean id="" class="" init-method="" destroy-method=""/>
    public void init(){
        // 确认消息是否到达交换机的回调，不管消息有没有到达交换机都会执行
        this.rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) -> {
            if (ack){
                log.info("消息已经到达交换机。");
            } else {
                log.error("消息没有到达交换机。原因：{}", cause);
    }

        });
        // 确认消息是否到达队列的回调，只有消息没有到达队列才会执行
        this.rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) -> {
            log.error("消息没有到达队列。交换机：{}，路由键：{}，消息内容：{}，状态码：{}，状态文本：{}",
                    exchange, routingKey, new String(message.getBody()), replyCode, replyText);
        });
    }
}
