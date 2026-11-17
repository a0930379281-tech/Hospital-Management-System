package ca.learncodegap.hospital;
import java.time.LocalDate;
public record Patient(int id, String name, int age, String gender, String phone, String diagnosis, LocalDate admissionDate) {}
