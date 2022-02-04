package fr.abes.colodus.batch;

import fr.abes.colodus.batch.tasklets.GenerateStatsTasklet;
import fr.abes.colodus.batch.tasklets.ReadFilesTasklet;
import fr.abes.colodus.batch.tasklets.VerifParams;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
@EnableBatchProcessing
public class StatsColodus extends DefaultBatchConfigurer {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;


    @Value("${source.directory}")
    private String repertoire;
    @Value("${destination.directory}")
    private String uploadPath;

    @Override
    public void setDataSource(DataSource dataSource) {
        //no state persistance nor bdd in this batch
    }

    @Bean
    public Job jobStats() {
        return jobBuilderFactory.get("jobStatsColodus").incrementer(new RunIdIncrementer())
                .start(verifParams()).on("FAILED").end()
                .from(verifParams()).on("COMPLETED").to(generateStats()).build().build();
    }

    @Bean
    public Step verifParams() {
        return stepBuilderFactory.get("verifParams").allowStartIfComplete(true)
                .tasklet(new VerifParams()).build();
    }
    @Bean
    public Flow generateStats() {
        return new FlowBuilder<Flow>("generateStats")
                .start(readFiles()).next(generateResultat())
                .build();
    }

    @Bean
    public Step readFiles() {
        return stepBuilderFactory.get("stepReadFiles").allowStartIfComplete(true)
                .tasklet(new ReadFilesTasklet(repertoire)).build();
    }

    @Bean
    public Step generateResultat() {
        return stepBuilderFactory.get("generateStatsFile").allowStartIfComplete(true)
                .tasklet(new GenerateStatsTasklet(uploadPath)).build();
    }


}
