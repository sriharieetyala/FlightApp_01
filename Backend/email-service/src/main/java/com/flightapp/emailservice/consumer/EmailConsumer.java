package com.flightapp.emailservice.consumer;

import com.flightapp.emailservice.config.RabbitMQConfig;
import com.flightapp.emailservice.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailSenderService emailSenderService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmailMessage(String messageJson) {
        System.out.println(" Received email payload: " + messageJson);

        // Default values
        String email = "";
        String pnr = "";
        String status = "Confirmed"; // fallback if not found

        // Try to extract email and PNR from the message using simple lines
        String[] lines = messageJson.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Email:")) {
                email = line.substring(6).trim();
            } else if (line.startsWith("PNR:")) {
                pnr = line.substring(4).trim();
            } else if (line.startsWith("Booking")) { // optional: extract status from first line
                if (line.toLowerCase().contains("confirmed")) {
                    status = "Confirmed";
                } else if (line.toLowerCase().contains("cancelled")) {
                    status = "Cancelled";
                }
            }
        }

        if (!email.isEmpty() && !pnr.isEmpty()) {
            String subject = "Your Booking Status - " + status;
            String body = "Your booking is " + status + ".\nPNR: " + pnr;

            emailSenderService.sendEmail(email, subject, body);
        } else {
            System.out.println("⚠ Could not extract email or PNR from payload.");
        }
    }

}
