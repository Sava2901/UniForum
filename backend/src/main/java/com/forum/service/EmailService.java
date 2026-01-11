package com.forum.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Async
    public void sendVerificationEmail(String email) {
        try {
            System.out.println("Starting email task for " + email + " on thread " + Thread.currentThread().getName());
            Thread.sleep(2000); 
            System.out.println("Sent verification email to " + email);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
