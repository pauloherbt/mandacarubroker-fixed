package com.mandacarubroker.domain;

import com.mandacarubroker.domain.user.User;
import com.mandacarubroker.dtos.RequestUserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    void setup(){
        user = new User("adrian", "password", "adrian@mail.com",
                "Adrian", "Wilker", LocalDate.parse("2001-04-15"), 250.00);
    }

    @Test
    void constructorShouldReturnUserWhenUserDTOIsAParameter() {
        User userFromDTO = new User(new RequestUserDTO(user));
        assertNotNull(userFromDTO);
        assertEquals("adrian", userFromDTO.getUsername());
        assertEquals("password", userFromDTO.getPassword());
        assertEquals("adrian@mail.com", userFromDTO.getEmail());
        assertEquals("Adrian", userFromDTO.getFirstName());
        assertEquals("Wilker", userFromDTO.getLastName());
        assertEquals(LocalDate.parse("2001-04-15"), userFromDTO.getBirthDate());
        assertEquals(250.00, userFromDTO.getBalance());
    }

    @Test
    void depositShouldCauseIncreasedBalance(){
        double initialBalance = user.getBalance();
        double depositAmount = 50.00;
        user.deposit(depositAmount);
        assertEquals(initialBalance - depositAmount, user.getBalance(), 0.001);
    }

    @Test
    void withdrawShouldCauseDecreasedBalance(){
        double initialBalance = user.getBalance();
        double withdrawAmount = 50.00;
        user.withdraw(withdrawAmount);
        assertEquals(initialBalance - withdrawAmount, user.getBalance(), 0.001);
    }

    @Test
    void depositShouldThrowIllegalArgumentExceptionWhenAmountIsNegative(){
        double negativeAmount = -50.00;
        assertThrows(IllegalArgumentException.class, () -> {
            user.deposit(negativeAmount);
        });
    }

    @Test
    void withdrawShouldThrowIllegalArgumentExceptionWhenAmountIsNegative(){
        double negativeAmount = -50.00;
        assertThrows(IllegalArgumentException.class, () -> {
            user.withdraw(negativeAmount);
        });
    }

    @Test
    void withdrawShouldThrowIllegalArgumentExceptionWhenAmountExceedsBalance(){
        double withdrawalAmount = 500.00;
        assertThrows(IllegalArgumentException.class, () -> {
            user.withdraw(withdrawalAmount);
        });
    }
}
