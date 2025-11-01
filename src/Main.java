package ru.hogwarts.school;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

// ==================== ГЛАВНЫЙ КЛАСС ПРИЛОЖЕНИЯ ====================
@SpringBootApplication
public class HogwartsApplication {
    public static void main(String[] args) {
        SpringApplication.run(HogwartsApplication.class, args);
    }
}

// ==================== МОДЕЛИ ====================

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
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return age == student.age &&
                Objects.equals(id, student.id) &&
                Objects.equals(name, student.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age);
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', age=" + age + "}";
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

    @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Faculty faculty = (Faculty) o;
        return Objects.equals(id, faculty.id) &&
                Objects.equals(name, faculty.name) &&
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
}

interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findByColor(String color);
    List<Faculty> findByNameIgnoreCaseOrColorIgnoreCase(String name, String color);
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
        if (studentRepository.existsById(id)) {
            student.setId(id);
            return studentRepository.save(student);
        }
        return null;
    }

    @Operation(summary = "Удалить студента")
    public boolean deleteStudent(Long id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Operation(summary = "Найти студентов по возрасту")
    public List<Student> getStudentsByAge(int age) {
        return studentRepository.findByAge(age);
    }

    @Operation(summary = "Найти студентов по диапазону возрастов")
    public List<Student> getStudentsByAgeBetween(int minAge, int maxAge) {
        return studentRepository.findByAgeBetween(minAge, maxAge);
    }

    @Operation(summary = "Получить факультет студента")
    public Faculty getStudentFaculty(Long studentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        return student.map(Student::getFaculty).orElse(null);
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
        if (facultyRepository.existsById(id)) {
            faculty.setId(id);
            return facultyRepository.save(faculty);
        }
        return null;
    }

    @Operation(summary = "Удалить факультет")
    public boolean deleteFaculty(Long id) {
        if (facultyRepository.existsById(id)) {
            facultyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Operation(summary = "Найти факультеты по цвету")
    public List<Faculty> getFacultiesByColor(String color) {
        return facultyRepository.findByColor(color);
    }

    @Operation(summary = "Найти факультеты по имени или цвету (регистронезависимо)")
    public List<Faculty> getFacultiesByNameOrColor(String searchTerm) {
        return facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(searchTerm, searchTerm);
    }

    @Operation(summary = "Получить студентов факультета")
    public List<Student> getFacultyStudents(Long facultyId) {
        return studentRepository.findByFacultyId(facultyId);
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
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return ResponseEntity.ok(createdStudent);
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
        if (updatedStudent != null) {
            return ResponseEntity.ok(updatedStudent);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить студента")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        if (studentService.deleteStudent(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/age/{age}")
    @Operation(summary = "Найти студентов по возрасту")
    public List<Student> getStudentsByAge(@PathVariable int age) {
        return studentService.getStudentsByAge(age);
    }

    @GetMapping("/age/between")
    @Operation(summary = "Найти студентов по диапазону возрастов")
    public List<Student> getStudentsByAgeRange(@RequestParam int min, @RequestParam int max) {
        return studentService.getStudentsByAgeBetween(min, max);
    }

    @GetMapping("/{id}/faculty")
    @Operation(summary = "Получить факультет студента")
    public ResponseEntity<Faculty> getStudentFaculty(@PathVariable Long id) {
        Faculty faculty = studentService.getStudentFaculty(id);
        if (faculty != null) {
            return ResponseEntity.ok(faculty);
        }
        return ResponseEntity.notFound().build();
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
    public ResponseEntity<Faculty> createFaculty(@RequestBody Faculty faculty) {
        Faculty createdFaculty = facultyService.createFaculty(faculty);
        return ResponseEntity.ok(createdFaculty);
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
        if (updatedFaculty != null) {
            return ResponseEntity.ok(updatedFaculty);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить факультет")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        if (facultyService.deleteFaculty(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/color/{color}")
    @Operation(summary = "Найти факультеты по цвету")
    public List<Faculty> getFacultiesByColor(@PathVariable String color) {
        return facultyService.getFacultiesByColor(color);
    }

    @GetMapping("/search")
    @Operation(summary = "Найти факультеты по имени или цвету (регистронезависимо)")
    public List<Faculty> getFacultiesByNameOrColor(@RequestParam String search) {
        return facultyService.getFacultiesByNameOrColor(search);
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "Получить студентов факультета")
    public List<Student> getFacultyStudents(@PathVariable Long id) {
        return facultyService.getFacultyStudents(id);
    }
}

// ==================== КОНФИГУРАЦИЯ ДЛЯ ИНИЦИАЛИЗАЦИИ ДАННЫХ ====================

@Component
class DataInitializer {
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public DataInitializer(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    @javax.annotation.PostConstruct
    public void init() {
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
                new Student("Джинни Уизли", 15),
                new Student("Винсент Крэбб", 16),
                new Student("Грегори Гойл", 16)
        );

        // Сохраняем студентов
        students = studentRepository.saveAll(students);

        // Назначаем студентов на факультеты
        students.get(0).setFaculty(gryffindor); // Гарри Поттер
        students.get(1).setFaculty(gryffindor); // Гермиона
        students.get(2).setFaculty(gryffindor); // Рон
        students.get(3).setFaculty(slytherin);  // Драко
        students.get(4).setFaculty(gryffindor); // Невилл
        students.get(5).setFaculty(ravenclaw);  // Полумна
        students.get(6).setFaculty(hufflepuff); // Седрик
        students.get(7).setFaculty(gryffindor); // Фред
        students.get(8).setFaculty(gryffindor); // Джордж
        students.get(9).setFaculty(gryffindor); // Джинни
        students.get(10).setFaculty(slytherin); // Крэбб
        students.get(11).setFaculty(slytherin); // Гойл

        studentRepository.saveAll(students);

        System.out.println("=== ДАННЫЕ УСПЕШНО ИНИЦИАЛИЗИРОВАНЫ ===");
        System.out.println("Создано факультетов: " + facultyRepository.count());
        System.out.println("Создано студентов: " + studentRepository.count());
    }
}

// ==================== ТЕСТЫ ====================

// Импорты для тестов
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ==================== TEST REST TEMPLATE TESTS ====================

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class StudentControllerTestRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    private static final String STUDENT_NAME = "Тестовый Студент";
    private static final int STUDENT_AGE = 20;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/student";
    }

    @Test
    void shouldCreateStudentSuccessfully() {
        Student student = new Student(STUDENT_NAME, STUDENT_AGE);
        ResponseEntity<Student> response = restTemplate.postForEntity(baseUrl, student, Student.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(STUDENT_NAME, response.getBody().getName());
        assertEquals(STUDENT_AGE, response.getBody().getAge());
    }

    @Test
    void shouldGetStudentByIdSuccessfully() {
        Student student = new Student(STUDENT_NAME, STUDENT_AGE);
        ResponseEntity<Student> createdResponse = restTemplate.postForEntity(baseUrl, student, Student.class);
        Long studentId = createdResponse.getBody().getId();
        ResponseEntity<Student> response = restTemplate.getForEntity(baseUrl + "/" + studentId, Student.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(studentId, response.getBody().getId());
        assertEquals(STUDENT_NAME, response.getBody().getName());
        assertEquals(STUDENT_AGE, response.getBody().getAge());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentStudent() {
        ResponseEntity<Student> response = restTemplate.getForEntity(baseUrl + "/999", Student.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetAllStudentsSuccessfully() {
        Student student1 = new Student("Студент 1", 18);
        Student student2 = new Student("Студент 2", 19);
        restTemplate.postForEntity(baseUrl, student1, Student.class);
        restTemplate.postForEntity(baseUrl, student2, Student.class);
        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl, List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 2);
    }

    @Test
    void shouldUpdateStudentSuccessfully() {
        Student student = new Student(STUDENT_NAME, STUDENT_AGE);
        ResponseEntity<Student> createdResponse = restTemplate.postForEntity(baseUrl, student, Student.class);
        Long studentId = createdResponse.getBody().getId();
        Student updatedStudent = new Student("Обновленный Студент", 21);
        restTemplate.put(baseUrl + "/" + studentId, updatedStudent);
        ResponseEntity<Student> response = restTemplate.getForEntity(baseUrl + "/" + studentId, Student.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Обновленный Студент", response.getBody().getName());
        assertEquals(21, response.getBody().getAge());
    }

    @Test
    void shouldDeleteStudentSuccessfully() {
        Student student = new Student(STUDENT_NAME, STUDENT_AGE);
        ResponseEntity<Student> createdResponse = restTemplate.postForEntity(baseUrl, student, Student.class);
        Long studentId = createdResponse.getBody().getId();
        restTemplate.delete(baseUrl + "/" + studentId);
        ResponseEntity<Student> response = restTemplate.getForEntity(baseUrl + "/" + studentId, Student.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetStudentsByAgeSuccessfully() {
        Student student1 = new Student("Студент 18 лет", 18);
        Student student2 = new Student("Студент 19 лет", 19);
        restTemplate.postForEntity(baseUrl, student1, Student.class);
        restTemplate.postForEntity(baseUrl, student2, Student.class);
        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl + "/age/18", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldGetStudentsByAgeRangeSuccessfully() {
        Student student1 = new Student("Студент 18 лет", 18);
        Student student2 = new Student("Студент 20 лет", 20);
        restTemplate.postForEntity(baseUrl, student1, Student.class);
        restTemplate.postForEntity(baseUrl, student2, Student.class);
        ResponseEntity<List> response = restTemplate.getForEntity(
                baseUrl + "/age/between?min=18&max=20", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldGetStudentFacultySuccessfully() {
        Faculty faculty = new Faculty("Гриффиндор", "красный");
        ResponseEntity<Faculty> facultyResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/faculty", faculty, Faculty.class);
        Long facultyId = facultyResponse.getBody().getId();
        Student student = new Student(STUDENT_NAME, STUDENT_AGE);
        student.setFaculty(facultyResponse.getBody());
        ResponseEntity<Student> studentResponse = restTemplate.postForEntity(baseUrl, student, Student.class);
        Long studentId = studentResponse.getBody().getId();
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/" + studentId + "/faculty", Faculty.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Гриффиндор", response.getBody().getName());
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FacultyControllerTestRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    private static final String FACULTY_NAME = "Гриффиндор";
    private static final String FACULTY_COLOR = "красный";

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/faculty";
    }

    @Test
    void shouldCreateFacultySuccessfully() {
        Faculty faculty = new Faculty(FACULTY_NAME, FACULTY_COLOR);
        ResponseEntity<Faculty> response = restTemplate.postForEntity(baseUrl, faculty, Faculty.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(FACULTY_NAME, response.getBody().getName());
        assertEquals(FACULTY_COLOR, response.getBody().getColor());
    }

    @Test
    void shouldGetFacultyByIdSuccessfully() {
        Faculty faculty = new Faculty(FACULTY_NAME, FACULTY_COLOR);
        ResponseEntity<Faculty> createdResponse = restTemplate.postForEntity(baseUrl, faculty, Faculty.class);
        Long facultyId = createdResponse.getBody().getId();
        ResponseEntity<Faculty> response = restTemplate.getForEntity(baseUrl + "/" + facultyId, Faculty.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(facultyId, response.getBody().getId());
        assertEquals(FACULTY_NAME, response.getBody().getName());
        assertEquals(FACULTY_COLOR, response.getBody().getColor());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentFaculty() {
        ResponseEntity<Faculty> response = restTemplate.getForEntity(baseUrl + "/999", Faculty.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetAllFacultiesSuccessfully() {
        Faculty faculty1 = new Faculty("Гриффиндор", "красный");
        Faculty faculty2 = new Faculty("Слизерин", "зеленый");
        restTemplate.postForEntity(baseUrl, faculty1, Faculty.class);
        restTemplate.postForEntity(baseUrl, faculty2, Faculty.class);
        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl, List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 2);
    }

    @Test
    void shouldUpdateFacultySuccessfully() {
        Faculty faculty = new Faculty(FACULTY_NAME, FACULTY_COLOR);
        ResponseEntity<Faculty> createdResponse = restTemplate.postForEntity(baseUrl, faculty, Faculty.class);
        Long facultyId = createdResponse.getBody().getId();
        Faculty updatedFaculty = new Faculty("Обновленный Факультет", "синий");
        restTemplate.put(baseUrl + "/" + facultyId, updatedFaculty);
        ResponseEntity<Faculty> response = restTemplate.getForEntity(baseUrl + "/" + facultyId, Faculty.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Обновленный Факультет", response.getBody().getName());
        assertEquals("синий", response.getBody().getColor());
    }

    @Test
    void shouldDeleteFacultySuccessfully() {
        Faculty faculty = new Faculty(FACULTY_NAME, FACULTY_COLOR);
        ResponseEntity<Faculty> createdResponse = restTemplate.postForEntity(baseUrl, faculty, Faculty.class);
        Long facultyId = createdResponse.getBody().getId();
        restTemplate.delete(baseUrl + "/" + facultyId);
        ResponseEntity<Faculty> response = restTemplate.getForEntity(baseUrl + "/" + facultyId, Faculty.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetFacultiesByColorSuccessfully() {
        Faculty faculty1 = new Faculty("Гриффиндор", "красный");
        Faculty faculty2 = new Faculty("Слизерин", "зеленый");
        restTemplate.postForEntity(baseUrl, faculty1, Faculty.class);
        restTemplate.postForEntity(baseUrl, faculty2, Faculty.class);
        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl + "/color/красный", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldSearchFacultiesByNameOrColorSuccessfully() {
        Faculty faculty1 = new Faculty("Гриффиндор", "красный");
        Faculty faculty2 = new Faculty("Слизерин", "зеленый");
        restTemplate.postForEntity(baseUrl, faculty1, Faculty.class);
        restTemplate.postForEntity(baseUrl, faculty2, Faculty.class);
        ResponseEntity<List> response = restTemplate.getForEntity(
                baseUrl + "/search?search=гриф", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldGetFacultyStudentsSuccessfully() {
        Faculty faculty = new Faculty(FACULTY_NAME, FACULTY_COLOR);
        ResponseEntity<Faculty> facultyResponse = restTemplate.postForEntity(baseUrl, faculty, Faculty.class);
        Long facultyId = facultyResponse.getBody().getId();
        Student student1 = new Student("Студент 1", 18);
        student1.setFaculty(facultyResponse.getBody());
        Student student2 = new Student("Студент 2", 19);
        student2.setFaculty(facultyResponse.getBody());
        String studentUrl = "http://localhost:" + port + "/student";
        restTemplate.postForEntity(studentUrl, student1, Student.class);
        restTemplate.postForEntity(studentUrl, student2, Student.class);
        ResponseEntity<List> response = restTemplate.getForEntity(
                baseUrl + "/" + facultyId + "/students", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

// ==================== WEB MVC TESTS ====================

@WebMvcTest(StudentController.class)
class StudentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String STUDENT_NAME = "Тестовый Студент";
    private static final int STUDENT_AGE = 20;

    @Test
    void shouldCreateStudentSuccessfully() throws Exception {
        Student student = new Student(STUDENT_NAME, STUDENT_AGE);
        Student savedStudent = new Student(1L, STUDENT_NAME, STUDENT_AGE);
        when(studentService.createStudent(any(Student.class))).thenReturn(savedStudent);
        mockMvc.perform(MockMvcRequestBuilders.post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(STUDENT_NAME))
                .andExpect(jsonPath("$.age").value(STUDENT_AGE));
    }

    @Test
    void shouldGetStudentByIdSuccessfully() throws Exception {
        Student student = new Student(1L, STUDENT_NAME, STUDENT_AGE);
        when(studentService.getStudentById(1L)).thenReturn(Optional.of(student));
        mockMvc.perform(MockMvcRequestBuilders.get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(STUDENT_NAME))
                .andExpect(jsonPath("$.age").value(STUDENT_AGE));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentStudent() throws Exception {
        when(studentService.getStudentById(anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get("/student/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllStudentsSuccessfully() throws Exception {
        List<Student> students = Arrays.asList(
                new Student(1L, "Студент 1", 18),
                new Student(2L, "Студент 2", 19)
        );
        when(studentService.getAllStudents()).thenReturn(students);
        mockMvc.perform(MockMvcRequestBuilders.get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void shouldUpdateStudentSuccessfully() throws Exception {
        Student updatedStudent = new Student(1L, "Обновленный Студент", 21);
        when(studentService.updateStudent(anyLong(), any(Student.class))).thenReturn(updatedStudent);
        mockMvc.perform(MockMvcRequestBuilders.put("/student/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Обновленный Студент"))
                .andExpect(jsonPath("$.age").value(21));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentStudent() throws Exception {
        when(studentService.updateStudent(anyLong(), any(Student.class))).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.put("/student/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Student())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteStudentSuccessfully() throws Exception {
        when(studentService.deleteStudent(1L)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.delete("/student/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentStudent() throws Exception {
        when(studentService.deleteStudent(anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete("/student/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetStudentsByAgeSuccessfully() throws Exception {
        List<Student> students = Arrays.asList(
                new Student(1L, "Студент 18 лет", 18),
                new Student(2L, "Студент 18 лет 2", 18)
        );
        when(studentService.getStudentsByAge(18)).thenReturn(students);
        mockMvc.perform(MockMvcRequestBuilders.get("/student/age/18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].age").value(18))
                .andExpect(jsonPath("$[1].age").value(18));
    }

    @Test
    void shouldGetStudentsByAgeRangeSuccessfully() throws Exception {
        List<Student> students = Arrays.asList(
                new Student(1L, "Студент 18 лет", 18),
                new Student(2L, "Студент 19 лет", 19)
        );
        when(studentService.getStudentsByAgeBetween(18, 20)).thenReturn(students);
        mockMvc.perform(MockMvcRequestBuilders.get("/student/age/between")
                        .param("min", "18")
                        .param("max", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].age").value(18))
                .andExpect(jsonPath("$[1].age").value(19));
    }

    @Test
    void shouldGetStudentFacultySuccessfully() throws Exception {
        Faculty faculty = new Faculty(1L, "Гриффиндор", "красный");
        when(studentService.getStudentFaculty(1L)).thenReturn(faculty);
        mockMvc.perform(MockMvcRequestBuilders.get("/student/1/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Гриффиндор"))
                .andExpect(jsonPath("$.color").value("красный"));
    }

    @Test
    void shouldReturnNotFoundWhenGettingFacultyOfNonExistentStudent() throws Exception {
        when(studentService.getStudentFaculty(anyLong())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/student/999/faculty"))
                .andExpect(status().isNotFound());
    }
}

@WebMvcTest(FacultyController.class)
class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacultyService facultyService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String FACULTY_NAME = "Гриффиндор";
    private static final String FACULTY_COLOR = "красный";

    @Test
    void shouldCreateFacultySuccessfully() throws Exception {
        Faculty faculty = new Faculty(FACULTY_NAME, FACULTY_COLOR);
        Faculty savedFaculty = new Faculty(1L, FACULTY_NAME, FACULTY_COLOR);
        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(savedFaculty);
        mockMvc.perform(MockMvcRequestBuilders.post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(FACULTY_NAME))
                .andExpect(jsonPath("$.color").value(FACULTY_COLOR));
    }

    @Test
    void shouldGetFacultyByIdSuccessfully() throws Exception {
        Faculty faculty = new Faculty(1L, FACULTY_NAME, FACULTY_COLOR);
        when(facultyService.getFacultyById(1L)).thenReturn(Optional.of(faculty));
        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(FACULTY_NAME))
                .andExpect(jsonPath("$.color").value(FACULTY_COLOR));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentFaculty() throws Exception {
        when(facultyService.getFacultyById(anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllFacultiesSuccessfully() throws Exception {
        List<Faculty> faculties = Arrays.asList(
                new Faculty(1L, "Гриффиндор", "красный"),
                new Faculty(2L, "Слизерин", "зеленый")
        );
        when(facultyService.getAllFaculties()).thenReturn(faculties);
        mockMvc.perform(MockMvcRequestBuilders.get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void shouldUpdateFacultySuccessfully() throws Exception {
        Faculty updatedFaculty = new Faculty(1L, "Обновленный Факультет", "синий");
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(updatedFaculty);
        mockMvc.perform(MockMvcRequestBuilders.put("/faculty/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFaculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Обновленный Факультет"))
                .andExpect(jsonPath("$.color").value("синий"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentFaculty() throws Exception {
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.put("/faculty/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Faculty())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteFacultySuccessfully() throws Exception {
        when(facultyService.deleteFaculty(1L)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.delete("/faculty/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentFaculty() throws Exception {
        when(facultyService.deleteFaculty(anyLong())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete("/faculty/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetFacultiesByColorSuccessfully() throws Exception {
        List<Faculty> faculties = Arrays.asList(
                new Faculty(1L, "Гриффиндор", "красный"),
                new Faculty(2L, "Другой красный факультет", "красный")
        );
        when(facultyService.getFacultiesByColor("красный")).thenReturn(faculties);
        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/color/красный"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].color").value("красный"))
                .andExpect(jsonPath("$[1].color").value("красный"));
    }

    @Test
    void shouldSearchFacultiesByNameOrColorSuccessfully() throws Exception {
        List<Faculty> faculties = Arrays.asList(
                new Faculty(1L, "Гриффиндор", "красный")
        );
        when(facultyService.getFacultiesByNameOrColor("гриф")).thenReturn(faculties);
        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/search")
                        .param("search", "гриф"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Гриффиндор"));
    }

    @Test
    void shouldGetFacultyStudentsSuccessfully() throws Exception {
        List<Student> students = Arrays.asList(
                new Student(1L, "Студент 1", 18),
                new Student(2L, "Студент 2", 19)
        );
        when(facultyService.getFacultyStudents(1L)).thenReturn(students);
        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }
}

// Вспомогательные методы для тестов
class TestUtils {
    static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected: " + expected + ", but was: " + actual);
        }
    }

    static void assertNotNull(Object object) {
        if (object == null) {
            throw new AssertionError("Object should not be null");
        }
    }

    static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Condition should be true");
        }
    }
}

// Статические импорты для методов assert
import static ru.hogwarts.school.TestUtils.assertEquals;
import static ru.hogwarts.school.TestUtils.assertNotNull;
import static ru.hogwarts.school.TestUtils.assertTrue;