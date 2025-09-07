package com.example.userservice.dao;

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
class UserDaoImplAutonomousIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15.3")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private UserDao userDao;
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
        userDao = new UserDaoImpl(sessionFactory);
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
    void createUser_PersistsInDatabase() {
        User user = new User("Alex", "alex@example.com", 25);
        Long id = userDao.create(user);

        assertNotNull(id);
        Optional<User> fromDb = userDao.findById(id);
        assertTrue(fromDb.isPresent());
        assertEquals("Alex", fromDb.get().getName());
    }

    @Test
    void findByEmail_ReturnsCorrectUser() {
        userDao.create(new User("Alex", "alex@example.com", 25));

        Optional<User> found = userDao.findByEmail("alex@example.com");
        assertTrue(found.isPresent());
        assertEquals("alex@example.com", found.get().getEmail());
    }

    @Test
    void updateUser_ChangesPersisted() {
        Long id = userDao.create(new User("Alex", "alex@example.com", 25));

        User user = userDao.findById(id).orElseThrow();
        user.setName("Alex Updated");
        userDao.update(user);

        Optional<User> updated = userDao.findById(id);
        assertTrue(updated.isPresent());
        assertEquals("Alex Updated", updated.get().getName());
    }

    @Test
    void deleteUser_RemovesFromDatabase() {
        Long id = userDao.create(new User("Alex", "alex@example.com", 25));

        userDao.deleteById(id);

        Optional<User> deleted = userDao.findById(id);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void findAll_ReturnsList() {
        userDao.create(new User("Alex", "alex@example.com", 25));
        userDao.create(new User("Bob", "bob@example.com", 30));

        List<User> users = userDao.findAll();
        assertEquals(2, users.size());
    }
}