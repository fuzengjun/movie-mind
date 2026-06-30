package com.example.movie.dto.admin;

import lombok.Data;

import java.util.List;

@Data
public class TmdbImportResultDTO {

    private String mode;
    private Integer requested;
    private Integer imported;
    private Integer updated;
    private Integer skipped;
    private String source;
    private List<String> errors;
}
