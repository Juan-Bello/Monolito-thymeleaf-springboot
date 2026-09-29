package co.javeriana.dw.biblioteca.service;

import co.javeriana.dw.biblioteca.entity.Estudiante;
import co.javeriana.dw.biblioteca.exception.NegocioException;
import co.javeriana.dw.biblioteca.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listar() {
        return estudianteRepository.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Estudiante crear(Estudiante estudiante) {
        if (estudianteRepository.existsByCodigo(estudiante.getCodigo())) {
            throw new NegocioException("Ya existe un estudiante con el código " + estudiante.getCodigo());
        }
        return estudianteRepository.save(estudiante);
    }
}
