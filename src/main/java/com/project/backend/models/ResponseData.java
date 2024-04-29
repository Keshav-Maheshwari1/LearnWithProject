package com.project.backend.models;

import com.project.backend.entities.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class ResponseData {
    private int status;
    private Member data;
    private String message;
    public ResponseData(HttpStatus status, Member data, String message) {
        this.status = status.value(); // Get the integer value of the HttpStatus
        this.data = data;
        this.message = message ;
    }
}
