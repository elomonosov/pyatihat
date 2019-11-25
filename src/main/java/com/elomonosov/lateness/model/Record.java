package com.elomonosov.lateness.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Embeddable
@Access(AccessType.FIELD)
@Data
@NoArgsConstructor
public class Record {

    private int value;
    private LocalDateTime created_at = LocalDateTime.now();
    @Enumerated
    @Column(name = "type", columnDefinition = "smallint")
    private Type type;
    private String reporter;

    public enum Type {
        DEBT(0),
        PAYMENT(1);

        private int code;

        Type(int code) {
            this.code = code;
        }

        @JsonValue
        public int getCode() {
            return code;
        }

        public static Type fromCode(Integer code) {
            for (Type s : Type.values()) {
                if (code.equals(s.getCode())) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown code: " + code);
        }
    }
}
