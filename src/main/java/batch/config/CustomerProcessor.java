package batch.config;

import batch.entity.Customer;
import org.springframework.batch.item.ItemProcessor;

// read the input as a Customer and write it as a Customer (inbound and outbound).
public class CustomerProcessor implements ItemProcessor<Customer,Customer> {

    @Override
    public Customer process(Customer customer) throws Exception {
        if(customer.getCountry().equals("United States")) {
            return customer;
        }else{
            return null;
        }
    }
}