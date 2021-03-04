package com.example.model;

import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Data
public class Message {
  private String from;
  private String to;
  private String text;
  private Date date;
  private UUID id;
}
