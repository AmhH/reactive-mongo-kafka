package com.example.service;

import com.example.model.Message;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

  public Message deliverMessage (Message message) {

    return message;
  }
}