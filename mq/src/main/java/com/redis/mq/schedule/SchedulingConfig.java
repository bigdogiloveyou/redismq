package com.redis.mq.schedule;


import com.redis.mq.mymq.Consumer;
import com.redis.mq.mymq.Producer;
import com.redis.mq.mymq.RedisMessageModel;
import com.redis.mq.utils.RedisCache;
import com.redis.mq.utils.UtilLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;


/**
 * @author LJ
 * @ClassName: SchedulingConfig
 * @Description: 定时任务 实例
 * @date 2018年3月8日 下午2:58:01
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    private static Logger LOGGER = LoggerFactory.getLogger(SchedulingConfig.class);


    /**
     * 消费消息间隔 5s
     */
    private static final long PULL_FIXED_DELAY = 1000 * 5;

    @Scheduled(fixedDelay = PULL_FIXED_DELAY)
    public void consumer() {
        UtilLogger.debug(LOGGER, "consumer job is start");
        Consumer consumer = new Consumer("xushu", RedisCache.getDefaultKeyPrefix());
        consumer.consume(message -> {
            System.out.println("我是消费者：" + message.getContent());
        });
        UtilLogger.debug(LOGGER, "consumer job is end");
    }

    @Scheduled(fixedDelay = PULL_FIXED_DELAY)
    public void produce() {
        UtilLogger.debug(LOGGER, "producer job is start");
        Producer producer = new Producer("xushu");
        RedisMessageModel messageModel = new RedisMessageModel();
        messageModel.setContent("gggggggggg");
        producer.publish(messageModel);
        UtilLogger.debug(LOGGER, "consumer job is end");
    }

}
