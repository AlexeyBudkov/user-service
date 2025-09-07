package com.example.userservice.service;

import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.UserDaoImpl;
import com.example.userservice.entity.User;
import com.example.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceImplIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15.3")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private UserService userService;
    private SessionFactory sessionFactory;

    @BeforeAll
    void setUp() throws Exception {
        try (Connection conn = postgres.createConnection("");
             Statement stmt = conn.createStatement()) {
            String schemaSql = Files.readString(Paths.get("src/test/resources/schema.sql"));
            stmt.execute(schemaSql);
        }

        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());

        sessionFactory = HibernateUtil.getSessionFactory();

        UserDao userDao = new UserDaoImpl(sessionFactory);
        userService = new UserServiceImpl(userDao);
    }

    @BeforeEach
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
            tx.commit();
        }
    }

    @Test
    void createUser_ValidData_SavesAndReturnsUser() {
        User user = new User("Alex", "alex@example.com", 25);
        User created = userService.createUser(user);

        assertNotNull(created.getId());
        assertEquals("Alex", created.getName());
        assertEquals("alex@example.com", created.getEmail());
    }

    @Test
    void createUser_InvalidEmail_ThrowsException() {
        User user = new User("Alex", "invalid-email", 25);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(user));

        assertEquals("Некорректный email", ex.getMessage());
    }

    @Test
    void getUserById_UserExists_ReturnsUser() {
        User created = userService.createUser(new User("Alex", "alex@example.com", 25));

        Optional<User> found = userService.getUserById(created.getId());

        assertTrue(found.isPresent());
        assertEquals("Alex", found.get().getName());
    }

    @Test
    void getUserById_UserNotFound_ReturnsEmpty() {
        Optional<User> found = userService.getUserById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void updateUser_ChangesPersisted() {
        User created = userService.createUser(new User("Alex", "alex@example.com", 25));
        created.setName("Alex Updated");

        User updated = userService.updateUser(created);

        assertEquals("Alex Updated", updated.getName());
    }

    @Test
    void deleteUser_UserExists_ReturnsTrue() {
        User created = userService.createUser(new User("Alex", "alex@example.com", 25));

        boolean deleted = userService.deleteUser(created.getId());

        assertTrue(deleted);
        assertTrue(userService.getUserById(created.getId()).isEmpty());
    }

    @Test
    void deleteUser_UserNotFound_ReturnsFalse() {
        boolean deleted = userService.deleteUser(999L);
        assertFalse(deleted);
    }

    @Test
    void getAllUsers_ReturnsList() {
        userService.createUser(new User("Alex", "alex@example.com", 25));
        userService.createUser(new User("Bob", "bob@example.com", 30));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }
}