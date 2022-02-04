package fr.abes.colodus.batch.tasklets;

import fr.abes.colodus.batch.dto.SourceStatDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ReadFilesTasklet implements Tasklet, StepExecutionListener {
    private String annee;
    private String mois;
    private List<SourceStatDto> listeStats = new ArrayList<>();
    private String repertoire;

    public ReadFilesTasklet(String repertoire) {
        this.repertoire = repertoire;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.annee = stepExecution.getJobExecution().getExecutionContext().get("annee").toString();
        int moisTemp = stepExecution.getJobExecution().getExecutionContext().getInt("mois");
        if (moisTemp < 10) {
            this.mois = "0" + moisTemp;
        }
        else {
            this.mois = String.valueOf(moisTemp);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext().put("annee", this.annee);
        stepExecution.getJobExecution().getExecutionContext().put("mois", this.mois);
        stepExecution.getJobExecution().getExecutionContext().put("listeStats", this.listeStats);
        return stepExecution.getExitStatus();
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String dateFichiers = this.annee + "-" + mois;

        String[] listeFichiers = (new File(repertoire)).list();
        Arrays.stream(listeFichiers).forEach(fichier -> {
            //on prend dans le répertoire cible tous les fichiers qui contiennent l'année et le mois dans leur nom
            if (fichier.contains(dateFichiers)) {
                try {
                    Reader f = new FileReader(repertoire + fichier);
                    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').parse(f);
                    for (CSVRecord record : records) {
                        //on teste si la 3è colonne matche bien sur un iln pour gérer les cas d'erreur dans le fichier source
                        if (record.get(2).matches("\\d*")) {
                            SourceStatDto source = new SourceStatDto(record.get(0), record.get(1), Integer.parseInt(record.get(2)), record.get(3), record.get(4));
                            this.listeStats.add(source);
                        }
                    }
                }
                catch (IOException e) {
                        log.error("Impossible de lire le fichier : " + fichier);
                        contribution.setExitStatus(ExitStatus.FAILED);
                }
            }
        });
        return RepeatStatus.FINISHED;
    }
}
