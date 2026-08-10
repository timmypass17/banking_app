package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.example.exceptions.InvalidAmountException;
import com.example.models.Customer;
import com.example.models.TransferResult;
import com.example.services.BankDAO;
import com.example.services.BankService;
import com.example.services.CustomerDAO;
import com.example.services.CustomerService;


public class ServiceTest {

    private CustomerDAO customerDao;
    private CustomerService customerService;

    private BankDAO bankDao;
    private BankService bankService;

    // run before every test method
    @Before
    public void setup() {
        customerDao = mock(CustomerDAO.class);
        customerService = new CustomerService(customerDao);

        bankDao = mock(BankDAO.class);
        bankService = new BankService(bankDao);
    }

    @Test
    public void testLogin() {
        Customer customer = new Customer(
            "1",
            "Timmy",
            "Nguyen",
            LocalDate.now(),
            "timmy@gmail.com",
            "password"
        );

        when(customerDao.login("timmy@gmail.com", "password"))
            .thenReturn(Optional.of(customer));

        Optional<Customer> result =
            customerService.login("timmy@gmail.com", "password");

        assertTrue(result.isPresent());
        assertEquals(customer, result.get());

        verify(customerDao).login("timmy@gmail.com", "password");
    }

    @Test
    public void testDepositPositiveAmount() throws Exception {
        when(bankDao.deposit("1", "1", 25))
            .thenReturn(25L);

        long result = bankService.deposit("1", "1", 25);

        assertEquals(25L, result);

        // called exactly once
        verify(bankDao).deposit("1", "1", 25);
    }

    @Test
    public void testDepositZeroAmount() throws Exception {
        assertThrows(
            InvalidAmountException.class,
            () -> bankService.deposit("1", "1", 0)
        );

        // never called
        verify(bankDao, never()).deposit(anyString(), anyString(), anyLong());
    }

    @Test
    public void testDepositNegativeAmount() throws Exception {
        assertThrows(
            InvalidAmountException.class,
            () -> bankService.deposit("1", "100", -100)
        );

        verify(bankDao, never()).deposit(anyString(), anyString(), anyLong());
    }

    @Test
    public void testWithdrawPositiveAmount() throws Exception {
        when(bankDao.withdraw("1", 25, "1"))
            .thenReturn(25L);

        long result = bankService.withdraw("1", 25, "1");

        assertEquals(25L, result);

        verify(bankDao).withdraw("1", 25, "1");
    }

    @Test
    public void testWithdrawZeroAmount() throws Exception {
        assertThrows(
            InvalidAmountException.class,
            () -> bankService.withdraw("1", 0, "1")
        );

        verify(bankDao, never()).withdraw(anyString(), anyLong(), anyString());
    }

    @Test
    public void testWithdrawNegativeAmount() throws Exception {
        assertThrows(
            InvalidAmountException.class,
            () -> bankService.withdraw("1",  -25, "1")
        );

        verify(bankDao, never()).withdraw(anyString(), anyLong(), anyString());
    }

    @Test
    public void testTransferPositiveAmount() throws Exception {
        when(bankDao.transferMoney(25L, "1", "2", "1"))
            .thenReturn(new TransferResult(0L, 25L));

        TransferResult result = bankService.transferMoney(25L, "1", "2", "1");

        assertEquals(0L, result.getSourceNewBalance());
        assertEquals(25L, result.getDestinationNewBalance());

        verify(bankDao).transferMoney(25L, "1", "2", "1");
    }

    @Test
    public void testTransferZeroAmount() throws Exception {
        assertThrows(
            InvalidAmountException.class,
            () -> bankService.transferMoney(0L, "1", "2", "1")
        );

        verify(bankDao, never()).transferMoney(0L, "1", "2", "1");
    }

    @Test
    public void testTransferNegativeAmount() throws Exception {
        assertThrows(
            InvalidAmountException.class,
            () -> bankService.transferMoney(-25L, "1", "2", "1")
        );

        verify(bankDao, never()).transferMoney(-25L, "1", "2", "1");
    }
}