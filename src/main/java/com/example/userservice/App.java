package com.example.userservice;

import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.UserDaoImpl;
import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import com.example.userservice.service.UserServiceImpl;
import com.example.userservice.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final UserService userService;

    static {
        UserDao userDao = new UserDaoImpl();
        userService = new UserServiceImpl(userDao);
    }

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                printMenu();
                String choice = sc.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> createUser(sc);
                        case "2" -> findUserById(sc);
                        case "3" -> findUserByEmail(sc);
                        case "4" -> listAllUsers();
                        case "5" -> updateUser(sc);
                        case "6" -> deleteUser(sc);
                        case "0" -> {
                            log.info("Выход из программы...");
                            System.out.println("Выход из программы...");
                            running = false;
                        }
                        default -> {
                            log.warn("Неизвестная команда: {}", choice);
                            System.out.println("Неизвестная команда. Повторите ввод.");
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Ошибки валидации из сервиса
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (Exception e) {
                    log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
                    System.out.println("Непредвиденная ошибка: " + e.getMessage());
                }
            }
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void printMenu() {
        System.out.println("""
                ===== User Service =====
                1) Создать пользователя
                2) Найти пользователя по ID
                3) Найти пользователя по email
                4) Показать всех пользователей
                5) Обновить пользователя
                6) Удалить пользователя
                0) Выход
                Выберите пункт:""");
    }

    private static void createUser(Scanner sc) {
        System.out.print("Имя: ");
        String name = sc.nextLine().trim();

        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Возраст (Enter — пропустить): ");
        String ageStr = sc.nextLine().trim();
        Integer age = ageStr.isEmpty() ? null : Integer.parseInt(ageStr);

        User created = userService.createUser(new User(name, email, age));
        System.out.println("Пользователь создан: " + created);
    }

    private static void findUserById(Scanner sc) {
        System.out.print("Введите ID: ");
        Long id = Long.parseLong(sc.nextLine().trim());

        Optional<User> user = userService.getUserById(id);
        user.ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Пользователь не найден.")
        );
    }

    private static void findUserByEmail(Scanner sc) {
        System.out.print("Введите email: ");
        String email = sc.nextLine().trim();

        Optional<User> user = userService.getUserByEmail(email);
        user.ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Пользователь не найден.")
        );
    }

    private static void listAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Список пользователей пуст.");
        } else {
            users.forEach(System.out::println);
        }
    }

    private static void updateUser(Scanner sc) {
        System.out.print("Введите ID пользователя для обновления: ");
        Long id = Long.parseLong(sc.nextLine().trim());

        Optional<User> optUser = userService.getUserById(id);
        if (optUser.isEmpty()) {
            System.out.println("Пользователь не найден.");
            return;
        }

        User user = optUser.get();
        System.out.println("Текущие данные: " + user);

        System.out.print("Новое имя (Enter — оставить без изменений): ");
        String name = sc.nextLine().trim();
        if (!name.isEmpty()) {
            user.setName(name);
        }

        System.out.print("Новый email (Enter — оставить без изменений): ");
        String email = sc.nextLine().trim();
        if (!email.isEmpty()) {
            user.setEmail(email);
        }

        System.out.print("Новый возраст (Enter — оставить без изменений): ");
        String ageStr = sc.nextLine().trim();
        if (!ageStr.isEmpty()) {
            user.setAge(Integer.parseInt(ageStr));
        }

        userService.updateUser(user);
        System.out.println("Пользователь обновлён.");
    }

    private static void deleteUser(Scanner sc) {
        System.out.print("Введите ID для удаления: ");
        Long id = Long.parseLong(sc.nextLine().trim());

        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            System.out.println("Пользователь удалён.");
        } else {
            System.out.println("Пользователь не найден.");
        }
    }
}