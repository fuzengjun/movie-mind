package com.example.movie.dto.admin;

import lombok.Data;

@Data
public class TmdbImportResultDTO {

    private Integer requested;
    private Integer imported;
    private Integer skipped;
    private String source;
}
