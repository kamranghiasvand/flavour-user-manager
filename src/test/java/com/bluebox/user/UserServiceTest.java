package com.bluebox.user;

import com.bluebox.service.mail.MailService;
import com.bluebox.service.user.UserException;
import com.bluebox.service.user.UserRepository;
import com.bluebox.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("user")
class UserServiceTest {

    @Autowired
    private UserService service;
    @Autowired
    private UserRepository repository;
    @MockBean
    private MailService mailSender;

    @BeforeEach
    public void before() {
        repository.deleteAll();
    }

    @Test()
    void nullInput_shouldThrowException() {
        var exception = assertThrows(UserException.class, () -> service.create(null));
        var message = exception.getMessage();
        assertEquals("User cannot be null", message);
    }

    @Test()
    void userWithoutEmail_shouldThrowException() {
        var user = new UserBuilder().build();
        var exception = assertThrows(ConstraintViolationException.class, () -> service.create(user));
        assertNotNull(exception);
    }

    @Test()
    void correctUserWithPass_shouldEncryptPass() throws UserException {
        var user = new UserBuilder().email("test@email.com").password("pass").build();
        user = service.create(user);
        assertNotEquals("pass", user.getPassword());
    }

    @Test()
    void duplicateUser_shouldThrowException() throws UserException {
        var first = new UserBuilder().email("test@email.com").password("pass").build();
        service.create(first);
        var second = new UserBuilder().email("test@email.com").password("pass").build();
        assertThrows(UserException.class, () -> service.create(second));
    }

    @Test()
    void findByEmail_returnsExistingUser() throws UserException {
        var first = new UserBuilder().email("test@email.com").password("pass").build();
        first = service.create(first);
        var optional = service.findByEmail(first.getEmail());
        assertTrue(optional.isPresent());
    }

    @Test()
    void findByEmailForNotExisingUser_returnsNull() {
        var optional = service.findByEmail("not-existing@email.com");
        assertTrue(optional.isEmpty());
    }
}
