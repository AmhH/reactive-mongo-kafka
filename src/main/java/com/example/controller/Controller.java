package com.example.controller;


import com.example.repository.PersonRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@RestController
@RequestMapping("/")
public class Controller {

  @Autowired
  private PersonRepository personRepository;

  private Scheduler scheduler = Schedulers.newBoundedElastic(5, 10, "MyThreadGroup");

  @GetMapping("/threads/webflux")
  public Flux<String> getThreadsWebflux() {
    //instrument the calls to the publishers' methods, wrap the construction of the operator, and capture a stack trace:
    Hooks.onOperatorDebug();
    return Flux.fromIterable(getThreads());
  }

  @GetMapping("/threads/webclient")
  public Flux<String> getThreadsWebClient() {
    Hooks.onOperatorDebug();
    WebClient.create("http://localhost:8080/index")
        .get()
        .retrieve()
        .bodyToMono(String.class)
        .subscribeOn(scheduler)
        .publishOn(scheduler)
        .doOnNext(s -> log.info("Response: {}", s))
        .subscribe();

    return Flux.fromIterable(getThreads());
  }

  @GetMapping("/threads/mongodb")
  public Flux<String> getIndexMongo() {
    Hooks.onOperatorDebug();
    personRepository.findAll()
        .doOnNext(p -> log.info("Person: {}", p))
        .subscribe();
    return Flux.fromIterable(getThreads());
  }

  @GetMapping("/threads/reactor-kafka")
  public Flux<String> getIndexKafka() {
    Hooks.onOperatorDebug();
    Map<String, Object> producerProps = new HashMap<>();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    SenderOptions<Integer, String> senderOptions = SenderOptions.create(producerProps);
    KafkaSender<Integer, String> sender = KafkaSender.create(senderOptions);
    Flux<SenderRecord<Integer, String, Integer>> outboundFlux = Flux.range(1, 10)
        .map(i -> SenderRecord.create(new ProducerRecord<>("reactive-test", i, "Message_" + i), i));
    sender.send(outboundFlux)
        .subscribe();

    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "my-consumer");
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    ReceiverOptions<Integer, String> receiverOptions = ReceiverOptions.create(consumerProps);
    receiverOptions.subscription(Collections.singleton("reactive-test"));
    KafkaReceiver<Integer, String> receiver = KafkaReceiver.create(receiverOptions);
    Flux<ReceiverRecord<Integer, String>> inboundFlux = receiver.receive();
    inboundFlux.subscribe(r -> {
      log.info("Received message: {}", r.value());
      r.receiverOffset()
          .acknowledge();
    });
    return Flux.fromIterable(getThreads());
  }

  @GetMapping("/index")
  public Mono<String> getIndex() {
    Hooks.onOperatorDebug();
    return Mono.just("Hello world!");
  }


  private List<String> getThreads() {
    return Thread.getAllStackTraces()
        .keySet()
        .stream()
        .map(t -> String.format("%-20s \t %s \t %d \t %s\n", t.getName(), t.getState(), t.getPriority(), t.isDaemon() ? "Daemon" : "Normal"))
        .collect(Collectors.toList());
  }
}
