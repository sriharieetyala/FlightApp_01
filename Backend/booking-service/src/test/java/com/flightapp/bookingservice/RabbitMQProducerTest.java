package com.flightapp.bookingservice;



import com.flightapp.bookingservice.config.RabbitMQConfig;
import com.flightapp.bookingservice.producer.RabbitMQProducer;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class RabbitMQProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQProducer producer;

    @Test
    void sendBookingEmail_invokesRabbitTemplate() {

        String msg = "Booking Successful";

        producer.sendBookingEmail(msg);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.ROUTING_KEY),
                eq(msg)
        );
    }
}
