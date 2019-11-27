package com.elomonosov.lateness.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SlackResponse {

    @JsonProperty("text")
    private String text;

    @JsonProperty("response_type")
    private ResponseType responseType = ResponseType.EPHEMERAL;

    public enum ResponseType {
        IN_CHANNEL("in_channel"),
        EPHEMERAL("ephemeral");

        String value;

        ResponseType(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }
}