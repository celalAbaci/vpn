package com.celalabaci.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMeessage {

    private MessageType messageType;
    private String ofStatic;


    public String prepareErrorMeessage() {
        StringBuilder builder = new StringBuilder();
        builder.append(messageType.getMessage());
        if (this.ofStatic != null) {
            builder.append(" : " + ofStatic);
        }
        return builder.toString();
    }
}
