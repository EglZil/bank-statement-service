package io.inventi.bankStatementService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDto {
    private int imported;
    private int skipped;
    private List<RowError> errors;

    @Data
    @AllArgsConstructor
    public static class RowError {
        private long row;
        private String message;
    }
}
