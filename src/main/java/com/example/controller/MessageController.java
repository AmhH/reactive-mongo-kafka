package com.example.controller;

import com.example.model.Message;
import com.example.service.MessageService;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageController {

  @Autowired
  private MessageService messageService;

  @PostMapping
  public Message createMessage (@RequestBody MessageApi messageDTO) {
    Message message = new Message();
    message.setText(messageDTO.getText());
    message.setFrom(messageDTO.getFrom());
    message.setTo(messageDTO.getTo());
    message.setDate(Date.from(Instant.now()));
    message.setId(UUID.randomUUID());

    return messageService.deliverMessage(message);
  }
}
