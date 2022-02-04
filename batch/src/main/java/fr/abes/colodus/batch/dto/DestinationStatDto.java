package fr.abes.colodus.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DestinationStatDto {
    private Integer iln;
    private long creation;
    private long modification;
    private long suppression;
    private long creationLocalisation;
    private long modificationLocalisation;
    private long suppressionLocalisation;
}
