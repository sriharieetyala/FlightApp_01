package com.flightapp.emailservice;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class EmailServiceApplicationTests {

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() { }
}

