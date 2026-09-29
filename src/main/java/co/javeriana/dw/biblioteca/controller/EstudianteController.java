package co.javeriana.dw.biblioteca.controller;

import co.javeriana.dw.biblioteca.entity.Estudiante;
import co.javeriana.dw.biblioteca.service.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("activeTab", "estudiantes");
        model.addAttribute("estudiantes", estudianteService.listar());
        return "estudiantes";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("activeTab", "estudiantes");
        model.addAttribute("estudiante", new Estudiante());
        return "estudiante-form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("estudiante") Estudiante estudiante, BindingResult result,
                        Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("activeTab", "estudiantes");
            return "estudiante-form";
        }
        try {
            estudianteService.crear(estudiante);
            redirectAttributes.addFlashAttribute("successMessage", "Estudiante registrado correctamente");
            return "redirect:/estudiantes";
        } catch (co.javeriana.dw.biblioteca.exception.NegocioException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "estudiante-form";
        }
    }
}
