package fr.abes.colodus.batch.tasklets;

import fr.abes.colodus.batch.dto.DestinationStatDto;
import fr.abes.colodus.batch.dto.SourceStatDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GenerateStatsTasklet implements Tasklet, StepExecutionListener {
    private String annee;
    private String mois;
    private List<SourceStatDto> listeStats;

    private String uploadPath;

    public GenerateStatsTasklet(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.annee = stepExecution.getJobExecution().getExecutionContext().getString("annee");
        this.mois = stepExecution.getJobExecution().getExecutionContext().getString("mois");
        this.listeStats = (List<SourceStatDto>) stepExecution.getJobExecution().getExecutionContext().get("listeStats");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<DestinationStatDto> listeOut = new ArrayList<>();
        //on boucle sur les ILN trouvés dans la liste
        this.listeStats.stream().map(s -> s.getIln()).distinct().sorted().forEach(iln -> {
            long nbCreation = listeStats.stream().filter(s -> s.getIln().equals(iln) && s.getAction().equals("CRE exemplaire")).count();
            long nbModification = listeStats.stream().filter(s -> s.getIln().equals(iln) && s.getAction().equals("MOD exemplaire")).count();
            long nbSuppression = listeStats.stream().filter(s -> s.getIln().equals(iln) && s.getAction().equals("SUP exemplaire")).count();
            long nbCreationLocal = listeStats.stream().filter(s -> s.getIln().equals(iln) && s.getAction().equals("CRE locales")).count();
            long nbModificationLocal = listeStats.stream().filter(s -> s.getIln().equals(iln) && s.getAction().equals("MOD locales")).count();
            long nbSuppressionLocal = listeStats.stream().filter(s -> s.getIln().equals(iln) && s.getAction().equals("SUP locales")).count();
            DestinationStatDto dto = new DestinationStatDto(iln, nbCreation, nbModification, nbSuppression, nbCreationLocal, nbModificationLocal, nbSuppressionLocal);
            listeOut.add(dto);
        });
        FileWriter out = new FileWriter(uploadPath + "colodus_" + annee + mois);
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n'))) {
            listeOut.forEach(dto -> {
                try {
                    printer.printRecord(dto.getIln(), dto.getCreation(), dto.getModification(), dto.getSuppression(), dto.getCreationLocalisation(), dto.getModificationLocalisation(), dto.getSuppressionLocalisation());
                } catch (IOException e) {
                    log.error("Erreur lors de la génération du fichier csv : " + e.getMessage());
                    contribution.setExitStatus(ExitStatus.FAILED);
                }
            });
        }
        return RepeatStatus.FINISHED;
    }
}
