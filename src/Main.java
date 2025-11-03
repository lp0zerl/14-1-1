package ru.hogwarts.school;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.persistence.*;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

// ==================== ГЛАВНЫЙ КЛАСС ПРИЛОЖЕНИЯ ====================
@SpringBootApplication
public class HogwartsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HogwartsApplication.class, args);
    }

    // Бин для инициализации данных
    @Bean
    public DataInitializer dataInitializer(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        return new DataInitializer(studentRepository, facultyRepository);
    }
}

// ==================== МОДЕЛИ (ENTITY) ====================

@Entity
@Table(name = "students")
class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "age", nullable = false)
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    // Конструкторы
    public Student() {}

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Student(Long id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return age == student.age && Objects.equals(id, student.id) && Objects.equals(name, student.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age);
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', age=" + age +
                (faculty != null ? ", faculty=" + faculty.getName() : "") + "}";
    }
}

@Entity
@Table(name = "faculties")
class Faculty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "color", nullable = false)
    private String color;

    @OneToMany(mappedBy = "faculty", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

    // Конструкторы
    public Faculty() {}

    public Faculty(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Faculty(Long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Faculty faculty = (Faculty) o;
        return Objects.equals(id, faculty.id) && Objects.equals(name, faculty.name) &&
                Objects.equals(color, faculty.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color);
    }

    @Override
    public String toString() {
        return "Faculty{id=" + id + ", name='" + name + "', color='" + color + "'}";
    }
}

// ==================== РЕПОЗИТОРИИ ====================

interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByAge(int age);
    List<Student> findByAgeBetween(int minAge, int maxAge);
    List<Student> findByFacultyId(Long facultyId);
    List<Student> findByNameContainingIgnoreCase(String name);
}

interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findByColor(String color);
    List<Faculty> findByNameIgnoreCaseOrColorIgnoreCase(String name, String color);
    List<Faculty> findByNameContainingIgnoreCase(String name);
}

// ==================== СЕРВИСЫ ====================

@Service
@Tag(name = "Student Service", description = "Сервис для работы со студентами")
class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Operation(summary = "Создать студента")
    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    @Operation(summary = "Получить студента по ID")
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    @Operation(summary = "Получить всех студентов")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Operation(summary = "Обновить студента")
    public Student updateStudent(Long id, Student student) {
        student.setId(id);
        return studentRepository.save(student);
    }

    @Operation(summary = "Удалить студента")
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    @Operation(summary = "Найти студентов по возрасту")
    public List<Student> getStudentsByAge(int age) {
        return studentRepository.findByAge(age);
    }

    @Operation(summary = "Найти студентов по диапазону возрастов")
    public List<Student> getStudentsByAgeBetween(int minAge, int maxAge) {
        return studentRepository.findByAgeBetween(minAge, maxAge);
    }

    @Operation(summary = "Найти студентов по имени")
    public List<Student> getStudentsByName(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name);
    }

    @Operation(summary = "Получить общее количество студентов")
    public Long getTotalCount() {
        return studentRepository.count();
    }

    @Operation(summary = "Получить средний возраст студентов")
    public Double getAverageAge() {
        return studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0.0);
    }

    @Operation(summary = "Получить последних 5 студентов")
    public List<Student> getLastFiveStudents() {
        List<Student> allStudents = studentRepository.findAll();
        return allStudents.stream()
                .sorted((s1, s2) -> Long.compare(s2.getId(), s1.getId()))
                .limit(5)
                .collect(Collectors.toList());
    }
}

@Service
@Tag(name = "Faculty Service", description = "Сервис для работы с факультетами")
class FacultyService {
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    public FacultyService(FacultyRepository facultyRepository, StudentRepository studentRepository) {
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    @Operation(summary = "Создать факультет")
    public Faculty createFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    @Operation(summary = "Получить факультет по ID")
    public Optional<Faculty> getFacultyById(Long id) {
        return facultyRepository.findById(id);
    }

    @Operation(summary = "Получить все факультеты")
    public List<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    @Operation(summary = "Обновить факультет")
    public Faculty updateFaculty(Long id, Faculty faculty) {
        faculty.setId(id);
        return facultyRepository.save(faculty);
    }

    @Operation(summary = "Удалить факультет")
    public void deleteFaculty(Long id) {
        facultyRepository.deleteById(id);
    }

    @Operation(summary = "Найти факультеты по цвету")
    public List<Faculty> getFacultiesByColor(String color) {
        return facultyRepository.findByColor(color);
    }

    @Operation(summary = "Найти факультеты по имени или цвету")
    public List<Faculty> getFacultiesByNameOrColor(String searchTerm) {
        return facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(searchTerm, searchTerm);
    }

    @Operation(summary = "Найти факультет по имени")
    public List<Faculty> getFacultiesByName(String name) {
        return facultyRepository.findByNameContainingIgnoreCase(name);
    }

    @Operation(summary = "Получить студентов факультета")
    public List<Student> getFacultyStudents(Long facultyId) {
        return studentRepository.findByFacultyId(facultyId);
    }

    @Operation(summary = "Получить самый длинный название факультета")
    public Optional<String> getLongestFacultyName() {
        return facultyRepository.findAll().stream()
                .map(Faculty::getName)
                .max(Comparator.comparing(String::length));
    }
}

// ==================== КОНТРОЛЛЕРЫ ====================

@RestController
@RequestMapping("/student")
@Tag(name = "Student Controller", description = "Контроллер для управления студентами")
class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @Operation(summary = "Создать нового студента")
    public Student createStudent(@RequestBody Student student) {
        return studentService.createStudent(student);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить студента по ID")
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        Optional<Student> student = studentService.getStudentById(id);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Получить всех студентов")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные студента")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        Student updatedStudent = studentService.updateStudent(id, student);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить студента")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/age/{age}")
    @Operation(summary = "Найти студентов по возрасту")
    public List<Student> getStudentsByAge(@PathVariable int age) {
        return studentService.getStudentsByAge(age);
    }

    @GetMapping("/age")
    @Operation(summary = "Найти студентов по диапазону возрастов")
    public List<Student> getStudentsByAgeRange(@RequestParam int min, @RequestParam int max) {
        return studentService.getStudentsByAgeBetween(min, max);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Найти студентов по имени")
    public List<Student> getStudentsByName(@PathVariable String name) {
        return studentService.getStudentsByName(name);
    }

    @GetMapping("/count")
    @Operation(summary = "Получить общее количество студентов")
    public Long getTotalCount() {
        return studentService.getTotalCount();
    }

    @GetMapping("/average-age")
    @Operation(summary = "Получить средний возраст студентов")
    public Double getAverageAge() {
        return studentService.getAverageAge();
    }

    @GetMapping("/last-five")
    @Operation(summary = "Получить последних 5 студентов")
    public List<Student> getLastFiveStudents() {
        return studentService.getLastFiveStudents();
    }
}

@RestController
@RequestMapping("/faculty")
@Tag(name = "Faculty Controller", description = "Контроллер для управления факультетами")
class FacultyController {
    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PostMapping
    @Operation(summary = "Создать новый факультет")
    public Faculty createFaculty(@RequestBody Faculty faculty) {
        return facultyService.createFaculty(faculty);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить факультет по ID")
    public ResponseEntity<Faculty> getFaculty(@PathVariable Long id) {
        Optional<Faculty> faculty = facultyService.getFacultyById(id);
        return faculty.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Получить все факультеты")
    public List<Faculty> getAllFaculties() {
        return facultyService.getAllFaculties();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные факультета")
    public ResponseEntity<Faculty> updateFaculty(@PathVariable Long id, @RequestBody Faculty faculty) {
        Faculty updatedFaculty = facultyService.updateFaculty(id, faculty);
        return ResponseEntity.ok(updatedFaculty);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить факультет")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/color/{color}")
    @Operation(summary = "Найти факультеты по цвету")
    public List<Faculty> getFacultiesByColor(@PathVariable String color) {
        return facultyService.getFacultiesByColor(color);
    }

    @GetMapping("/search")
    @Operation(summary = "Найти факультеты по имени или цвету")
    public List<Faculty> getFacultiesByNameOrColor(@RequestParam String search) {
        return facultyService.getFacultiesByNameOrColor(search);
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "Получить студентов факультета")
    public List<Student> getFacultyStudents(@PathVariable Long id) {
        return facultyService.getFacultyStudents(id);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Найти факультеты по имени")
    public List<Faculty> getFacultiesByName(@PathVariable String name) {
        return facultyService.getFacultiesByName(name);
    }

    @GetMapping("/longest-name")
    @Operation(summary = "Получить самое длинное название факультета")
    public ResponseEntity<String> getLongestFacultyName() {
        Optional<String> longestName = facultyService.getLongestFacultyName();
        return longestName.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

// ==================== ИНИЦИАЛИЗАЦИЯ ДАННЫХ ====================

class DataInitializer {
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public DataInitializer(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    @PostConstruct
    public void init() {
        // Очистка существующих данных
        studentRepository.deleteAll();
        facultyRepository.deleteAll();

        // Создаем факультеты
        Faculty gryffindor = facultyRepository.save(new Faculty("Гриффиндор", "красный"));
        Faculty slytherin = facultyRepository.save(new Faculty("Слизерин", "зеленый"));
        Faculty ravenclaw = facultyRepository.save(new Faculty("Когтевран", "синий"));
        Faculty hufflepuff = facultyRepository.save(new Faculty("Пуффендуй", "желтый"));

        // Создаем студентов
        List<Student> students = Arrays.asList(
                new Student("Гарри Поттер", 17),
                new Student("Гермиона Грейнджер", 17),
                new Student("Рон Уизли", 17),
                new Student("Драко Малфой", 17),
                new Student("Невилл Лонгботтом", 17),
                new Student("Полумна Лавгуд", 16),
                new Student("Седрик Диггори", 18),
                new Student("Фред Уизли", 18),
                new Student("Джордж Уизли", 18),
                new Student("Наташа Романова", 19),
                new Student("Тони Старк", 20),
                new Student("Брюс Беннер", 21)
        );

        // Сохраняем студентов
        students = studentRepository.saveAll(students);

        // Назначаем студентов на факультеты
        students.get(0).setFaculty(gryffindor);  // Гарри
        students.get(1).setFaculty(gryffindor);  // Гермиона
        students.get(2).setFaculty(gryffindor);  // Рон
        students.get(3).setFaculty(slytherin);   // Драко
        students.get(4).setFaculty(gryffindor);  // Невилл
        students.get(5).setFaculty(ravenclaw);   // Полумна
        students.get(6).setFaculty(hufflepuff);  // Седрик
        students.get(7).setFaculty(gryffindor);  // Фред
        students.get(8).setFaculty(gryffindor);  // Джордж
        students.get(9).setFaculty(slytherin);   // Наташа
        students.get(10).setFaculty(ravenclaw);  // Тони
        students.get(11).setFaculty(hufflepuff); // Брюс

        studentRepository.saveAll(students);

        System.out.println("=== ДАННЫЕ УСПЕШНО ИНИЦИАЛИЗИРОВАНЫ ===");
        System.out.println("Создано факультетов: " + facultyRepository.count());
        System.out.println("Создано студентов: " + studentRepository.count());

        // Тестовый вывод
        System.out.println("Факультеты:");
        facultyRepository.findAll().forEach(System.out::println);

        System.out.println("Студенты:");
        studentRepository.findAll().forEach(System.out::println);
    }
}