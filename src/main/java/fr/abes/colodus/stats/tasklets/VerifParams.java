package fr.abes.colodus.stats.tasklets;

import fr.abes.colodus.stats.LogTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class VerifParams implements Tasklet, StepExecutionListener {
    private Integer annee;
    private Integer mois;


    @Override
    public void beforeStep(StepExecution stepExecution) {
        LogTime.logDebutTraitement(stepExecution);
        if (System.getProperty("annee") != null && System.getProperty("mois") != null) {
            this.annee = Integer.parseInt(System.getProperty("annee"));
            this.mois = Integer.parseInt(System.getProperty("mois"));
        }
        else {
            //cas où le batch est appelé sans paramètre, le 1er du mois
            //on calcule le mois et l'année en fonction de la date courante
            Calendar dateJour = Calendar.getInstance();
            //même si le batch est lancé en début de mois suivant, on ne modifie pas le mois car le compteur de l'objet calendar part de 0
            this.annee = dateJour.get(Calendar.YEAR);
            this.mois = dateJour.get(Calendar.MONTH);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext().put("annee", annee);
        stepExecution.getJobExecution().getExecutionContext().put("mois", mois);
        return stepExecution.getExitStatus();
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        if ((mois <= 0) && (mois > 12)) {
            log.error("Le mois doit être compris entre 1 et 12");
            stepContribution.setExitStatus(ExitStatus.FAILED);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(Calendar.getInstance().getTime());

        if (annee < Integer.parseInt(date.substring(0,4))) {
            log.error("L'année ne peut pas être inférieure à l'année courante");
            stepContribution.setExitStatus(ExitStatus.FAILED);
        }
        stepContribution.setExitStatus(ExitStatus.COMPLETED);
        return RepeatStatus.FINISHED;
    }
}
