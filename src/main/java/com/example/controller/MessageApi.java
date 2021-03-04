package com.example.controller;

import lombok.Data;

@Data
public class MessageApi  {
  private String from;
  private String to;
  private String text;
}
