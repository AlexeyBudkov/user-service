package com.example.userservice;

import com.example.userservice.dao.DaoException;
import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.UserDaoImpl;
import com.example.userservice.entity.User;
import com.example.userservice.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final UserDao userDao = new UserDaoImpl();

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
                            running = false;
                            log.info("Выход из программы...");
                            System.out.println("Выход из программы...");
                        }
                        default -> {
                            log.warn("Неизвестная команда: {}", choice);
                            System.out.println("Неизвестная команда. Повторите ввод.");
                        }
                    }
                } catch (DaoException e) {
                    log.error("Ошибка работы с базой данных: {}", e.getMessage(), e);
                    System.out.println("Ошибка работы с базой данных: " + e.getMessage());
                } catch (NumberFormatException e) {
                    log.warn("Ошибка ввода: ожидалось число");
                    System.out.println("Ошибка ввода: ожидалось число.");
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
        log.info("Запуск создания пользователя");
        System.out.print("Имя: ");
        String name = sc.nextLine().trim();
        if (!isValidName(name)) {
            log.warn("Введено некорректное имя: '{}'", name);
            System.out.println("Имя должно содержать минимум 2 символа и не быть пустым.");
            return;
        }

        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        if (!isValidEmail(email)) {
            log.warn("Введён некорректный email: '{}'", email);
            System.out.println("Некорректный email.");
            return;
        }

        System.out.print("Возраст (Enter — пропустить): ");
        String ageStr = sc.nextLine().trim();
        Integer age = ageStr.isEmpty() ? null : Integer.parseInt(ageStr);

        Long id = userDao.create(new User(name, email, age));
        log.info("Пользователь создан с ID = {}", id);
        System.out.println("Пользователь создан с ID = " + id);
    }

    private static void findUserById(Scanner sc) {
        System.out.print("Введите ID: ");
        Long id = Long.parseLong(sc.nextLine().trim());
        Optional<User> user = userDao.findById(id);
        if (user.isPresent()) {
            log.info("Найден пользователь по ID {}: {}", id, user.get());
            System.out.println(user.get());
        } else {
            log.warn("Пользователь с ID {} не найден", id);
            System.out.println("Пользователь не найден.");
        }
    }

    private static void findUserByEmail(Scanner sc) {
        System.out.print("Введите email: ");
        String email = sc.nextLine().trim();
        if (!isValidEmail(email)) {
            log.warn("Введён некорректный email: '{}'", email);
            System.out.println("Некорректный email.");
            return;
        }
        Optional<User> user = userDao.findByEmail(email);
        if (user.isPresent()) {
            log.info("Найден пользователь по email {}: {}", email, user.get());
            System.out.println(user.get());
        } else {
            log.warn("Пользователь с email {} не найден", email);
            System.out.println("Пользователь не найден.");
        }
    }

    private static void listAllUsers() {
        List<User> users = userDao.findAll();
        if (users.isEmpty()) {
            log.info("Список пользователей пуст");
            System.out.println("Список пользователей пуст.");
        } else {
            log.info("Вывод списка всех пользователей ({} шт.)", users.size());
            users.forEach(System.out::println);
        }
    }

    private static void updateUser(Scanner sc) {
        System.out.print("Введите ID пользователя для обновления: ");
        Long id = Long.parseLong(sc.nextLine().trim());
        Optional<User> optUser = userDao.findById(id);

        if (optUser.isEmpty()) {
            log.warn("Пользователь с ID {} не найден для обновления", id);
            System.out.println("Пользователь не найден.");
            return;
        }

        User user = optUser.get();
        log.info("Обновление пользователя: {}", user);
        System.out.println("Текущие данные: " + user);

        System.out.print("Новое имя (Enter — оставить без изменений): ");
        String name = sc.nextLine().trim();
        if (!name.isEmpty()) {
            if (!isValidName(name)) {
                log.warn("Введено некорректное имя при обновлении: '{}'", name);
                System.out.println("Имя должно содержать минимум 2 символа и не быть пустым.");
                return;
            }
            user.setName(name);
        }

        System.out.print("Новый email (Enter — оставить без изменений): ");
        String email = sc.nextLine().trim();
        if (!email.isEmpty()) {
            if (!isValidEmail(email)) {
                log.warn("Введён некорректный email при обновлении: '{}'", email);
                System.out.println("Некорректный email.");
                return;
            }
            user.setEmail(email);
        }

        System.out.print("Новый возраст (Enter — оставить без изменений): ");
        String ageStr = sc.nextLine().trim();
        if (!ageStr.isEmpty()) {
            user.setAge(Integer.parseInt(ageStr));
        }

        userDao.update(user);
        log.info("Пользователь с ID {} обновлён", id);
        System.out.println("Пользователь обновлён.");
    }

    private static void deleteUser(Scanner sc) {
        System.out.print("Введите ID для удаления: ");
        Long id = Long.parseLong(sc.nextLine().trim());
        userDao.deleteById(id);
        log.info("Пользователь с ID {} удалён (если существовал)", id);
        System.out.println("Пользователь удалён (если существовал).");
    }

    private static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.length() >= 2;
    }

    private static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
