package org.gl.eir.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TacListDto {
    String tac;

    public String getTac() {
        return tac;
    }

    public void setTac(String tac) {
        this.tac = tac;
    }
}
