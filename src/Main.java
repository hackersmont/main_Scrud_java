import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.*;

// Interfaz CRUD
interface CrudOperations<T> {
    void create(T item);
    T read(String id);
    void update(T item);
    void delete(String id);
    List<T> readAll();
}

// Clase Estudiante
class Estudiante implements Serializable {
    private static final long serialVersionUID = 1L;
    private String cedula;
    private String nombre;
    private String apellido;
    private int semestre;
    private List<String> materias;
    private static final double COSTO_POR_MATERIA = 50.0;

    public Estudiante(String cedula, String nombre, String apellido, int semestre) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.semestre = semestre;
        this.materias = new ArrayList<>();
    }

    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public int getSemestre() { return semestre; }
    public List<String> getMaterias() { return materias; }
    public void setSemestre(int semestre) { this.semestre = semestre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public void agregarMateria(String materia) {
        if (!materias.contains(materia)) {
            materias.add(materia);
        }
    }

    public double calcularCostoMatricula() {
        return materias.size() * COSTO_POR_MATERIA;
    }

    @Override
    public String toString() {
        return String.format("Estudiante{cedula='%s', nombre='%s', apellido='%s', semestre=%d, materias=%s}",
                cedula, nombre, apellido, semestre, materias);
    }
}

// Clase MatriculaManager
class MatriculaManager implements CrudOperations<Estudiante>, Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Estudiante> estudiantes;
    private static final String FILENAME = "estudiantes.dat";

    public MatriculaManager() {
        this.estudiantes = new HashMap<>();
        cargarDatos();
    }

    private void cargarDatos() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILENAME))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Estudiante> loadedData = (Map<String, Estudiante>) obj;
                this.estudiantes = loadedData;
                System.out.println("Datos cargados exitosamente.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("No se encontró archivo de datos. Se iniciará con una base de datos vacía.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error al cargar los datos: " + e.getMessage());
        }
    }

    public void guardarDatos() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILENAME))) {
            oos.writeObject(estudiantes);
            System.out.println("Datos guardados exitosamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar los datos: " + e.getMessage());
        }
    }

    @Override
    public void create(Estudiante estudiante) {
        if (!estudiantes.containsKey(estudiante.getCedula())) {
            estudiantes.put(estudiante.getCedula(), estudiante);
            System.out.println("Estudiante matriculado exitosamente.");
        } else {
            System.out.println("Error: Ya existe un estudiante con esa cédula.");
        }
    }

    @Override
    public Estudiante read(String cedula) {
        return estudiantes.get(cedula);
    }

    @Override
    public void update(Estudiante estudiante) {
        if (estudiantes.containsKey(estudiante.getCedula())) {
            estudiantes.put(estudiante.getCedula(), estudiante);
            System.out.println("Información del estudiante actualizada.");
        } else {
            System.out.println("Error: Estudiante no encontrado.");
        }
    }

    @Override
    public void delete(String cedula) {
        if (estudiantes.remove(cedula) != null) {
            System.out.println("Estudiante eliminado del sistema.");
        } else {
            System.out.println("Error: Estudiante no encontrado.");
        }
    }

    @Override
    public List<Estudiante> readAll() {
        return new ArrayList<>(estudiantes.values());
    }

    public void imprimirResumenMatricula() {
        System.out.println("\n=== RESUMEN DE MATRÍCULA IUTEPAL ===");
        System.out.println("Total de estudiantes matriculados: " + estudiantes.size());

        double ingresoTotal = estudiantes.values().stream()
                .mapToDouble(Estudiante::calcularCostoMatricula)
                .sum();

        System.out.printf("Ingreso total por matrículas: %.2f Bs\n", ingresoTotal);
    }

    public void generarReporte() {
        System.out.println("\n=== GENERANDO REPORTE DE ESTUDIANTES ===");

        List<Estudiante> reporte = estudiantes.values().stream()
                .filter(e -> e.getSemestre() >= 3 && e.getMaterias().size() > 2)
                .map(e -> {
                    e.setNombre(e.getNombre().toUpperCase());
                    return e;
                })
                .sorted((e1, e2) -> e1.getApellido().compareTo(e2.getApellido()))
                .limit(5)
                .collect(Collectors.toList());

        // Guardar el reporte en un archivo de texto
        try (PrintWriter writer = new PrintWriter(new FileWriter("reporte.txt"))) {
            writer.println("=== REPORTE DE ESTUDIANTES ===");
            for (Estudiante e : reporte) {
                writer.println(e.toString());
            }
            System.out.println("Reporte guardado exitosamente en 'reporte.txt'");
        } catch (IOException e) {
            System.out.println("Error al guardar el reporte: " + e.getMessage());
        }

        // Mostrar los estudiantes seleccionados en consola
        reporte.forEach(e -> System.out.println(e.toString()));
    }
}

// Clase principal
class Main {
    public static void main(String[] args) {
        MatriculaManager matricula = new MatriculaManager();

        // Crear algunos estudiantes de ejemplo si la base de datos está vacía
        if (matricula.readAll().isEmpty()) {
            Estudiante est1 = new Estudiante("V12345678", "Juan", "Pérez", 3);
            est1.agregarMateria("Programación III");
            est1.agregarMateria("Bases de Datos");
            est1.agregarMateria("Redes");

            Estudiante est2 = new Estudiante("V87654321", "María", "González", 2);
            est2.agregarMateria("Programación II");
            est2.agregarMateria("Matemáticas Discretas");

            System.out.println("\n=== Matriculando estudiantes ===");
            matricula.create(est1);
            matricula.create(est2);
        } else {
            System.out.println("\n=== Estudiantes cargados de la base de datos ===");
        }

        // Generar y guardar reporte
        matricula.generarReporte();

        // Guardar datos antes de salir
        matricula.guardarDatos();
    }
}

