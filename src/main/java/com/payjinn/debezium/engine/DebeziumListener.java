package com.payjinn.debezium.engine;

import com.payjinn.debezium.service.StudentService;
import io.debezium.config.Configuration;
import io.debezium.data.Envelope.FieldName;
import io.debezium.data.Envelope.Operation;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DebeziumListener {

  private final Executor executor = Executors.newSingleThreadExecutor();

  private StudentService studentService;

  private DebeziumEngine<RecordChangeEvent<SourceRecord>> engine;

  public DebeziumListener(Configuration connectorConfig, StudentService studentService) {
    this.studentService = studentService;
    this.engine =
        DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
            .using(connectorConfig.asProperties())
            .notifying(this::handleEvent)
            .build();
  }

  private void handleEvent(RecordChangeEvent<SourceRecord> recordChangeEvent) {
    SourceRecord sr = recordChangeEvent.record();

    log.info("key: " + sr.key() + ", value: " + sr.value());

    Struct recordChangeValue = (Struct) sr.value();

    if (recordChangeValue != null) {
      Operation op = Operation.forCode((String) recordChangeValue.get(FieldName.OPERATION));

      if (op != Operation.READ) {
        String record = op == Operation.DELETE ? FieldName.BEFORE : FieldName.AFTER;

        Struct struct = (Struct) recordChangeValue.get(record);
        Map<String, Object> payload =
            struct.schema().fields().stream()
                .map(Field::name)
                .filter(fieldName -> struct.get(fieldName) != null)
                .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        this.studentService.copyData(payload, op);
        log.info("Updated Data: {} with Operation {}", payload, op.name());
      }
    }
  }

  @PostConstruct
  private void start() {
    this.executor.execute(engine);
  }

  @PreDestroy
  private void stop() throws IOException {
    if (this.engine != null) {
      this.engine.close();
    }
  }
}
