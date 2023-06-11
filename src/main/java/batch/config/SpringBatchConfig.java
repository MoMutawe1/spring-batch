package batch.config;


import batch.entity.Customer;
import batch.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    //private JobBuilder jobBuilderFactory;

    //private StepBuilder stepBuilderFactory;

    private PlatformTransactionManager transactionManager;

    private JobRepository jobRepository;

    private CustomerRepository customerRepository;

    @Bean
    public FlatFileItemReader<Customer> reader() {
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);   // skip the first line (header line)
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    // How to read the csv file, and how to map the data from the csv file to the Customer object.
    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        // lineTokenizer will extract the data from the csv file.
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        //will map the extracted value by the lineTokenizer to the target class which is Customer.
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        // both objects from previous step need to provide to the line mapper.
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    @Bean
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> writer() {
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        // here we're telling our CustomerRepository to use customerRepository.save method
        // to write the information or the csv data to the Database.
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step1() {
        return new StepBuilder("csv-step", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(reader())
                .processor((ItemProcessor<? super Customer, ? extends Customer>) processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob() {
        return new JobBuilder("importCustomers", jobRepository)
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }

}
