package sample.sql;

import jdbq.mapping.SqlName;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record UserRow(
    int id,
    String fullName,
    @SqlName("dob")
    LocalDate birthday,
    OffsetDateTime lastLogin
) {}
