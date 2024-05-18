package com.chandooiitm.accounts.service.impl;

import com.chandooiitm.accounts.dto.AccountsDto;
import com.chandooiitm.accounts.dto.CardsDto;
import com.chandooiitm.accounts.dto.CustomerDetailsDto;
import com.chandooiitm.accounts.dto.LoansDto;
import com.chandooiitm.accounts.entity.Accounts;
import com.chandooiitm.accounts.entity.Customer;
import com.chandooiitm.accounts.exception.ResourceNotFoundException;
import com.chandooiitm.accounts.mapper.AccountsMapper;
import com.chandooiitm.accounts.mapper.CustomerMapper;
import com.chandooiitm.accounts.repository.AccountsRepository;
import com.chandooiitm.accounts.repository.CustomerRepository;
import com.chandooiitm.accounts.service.ICustomersService;
import com.chandooiitm.accounts.service.client.CardsFeignClient;
import com.chandooiitm.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);
        customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());

        return customerDetailsDto;

    }
}
