package com.example.controller;

import static org.mockito.Mockito.times;

import com.example.model.Message;
import com.example.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MessageControllerUnitTest {

  @Mock
  private MessageService messageService;

  @InjectMocks
  private MessageController messageController;

  @Test
  public void createMessage_NewMessage_OK() {
    MessageApi messageApi = new MessageApi();
    messageApi.setFrom("me");
    messageApi.setTo("you");
    messageApi.setText("Hello, you!");

    messageController.createMessage(messageApi);

    Message message = new Message();
    message.setFrom("me");
    message.setTo("you");
    message.setText("Hello, you!");

    Mockito.verify(messageService, times(1)).deliverMessage(ArgumentMatchers.argThat(new MessageMatcher(message)));
  }

  public class MessageMatcher implements ArgumentMatcher<Message> {

    private Message left;

    public MessageMatcher(Message message) {
      this.left = message;
    }

    @Override
    public boolean matches(Message right) {
      return left.getFrom().equals(right.getFrom()) &&
          left.getTo().equals(right.getTo()) &&
          left.getText().equals(right.getText()) &&
          right.getDate() != null &&
          right.getId() != null;
    }
  }
}