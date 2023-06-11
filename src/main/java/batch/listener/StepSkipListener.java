package batch.listener;

import batch.entity.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;

@Slf4j
public class StepSkipListener implements SkipListener<Customer, Number> {

    @Override // item reader
    public void onSkipInRead(Throwable throwable) {
        log.info("A failure on read {} ", throwable.getMessage());
    }

    @Override // item writter
    public void onSkipInWrite(Number item, Throwable throwable) {
        log.info("A failure on write {} , {}", throwable.getMessage(), item);
    }

    @SneakyThrows
    @Override // item processor
    public void onSkipInProcess(Customer customer, Throwable throwable) {
        log.info("Item {}  was skipped due to the exception  {}", new ObjectMapper().writeValueAsString(customer),
                throwable.getMessage());
    }
}
