package com.payjinn.debezium.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payjinn.debezium.model.Student;
import com.payjinn.debezium.repository.StudentRepository;
import io.debezium.data.Envelope.Operation;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

  private final StudentRepository studentRepo;

  public StudentService(StudentRepository studentRepo) {
    this.studentRepo = studentRepo;
  }

  public void copyData(Map<String, Object> data, Operation op) {
    Student student = new ObjectMapper().convertValue(data, Student.class);

    if (Operation.DELETE.equals(op)) {
      studentRepo.deleteById(student.getId());
    } else {
      studentRepo.save(student);
    }
  }
}
