package fr.abes.colodus.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class SourceStatDto implements Serializable {
    private String jour;
    private String heure;
    private Integer iln;
    private String rcr;
    private String action;
}
