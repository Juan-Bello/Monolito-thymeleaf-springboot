package co.javeriana.dw.biblioteca.controller;

import co.javeriana.dw.biblioteca.entity.Libro;
import co.javeriana.dw.biblioteca.service.LibroService;
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
@RequestMapping("/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("activeTab", "libros");
        model.addAttribute("libros", libroService.listar());
        return "libros";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("activeTab", "libros");
        model.addAttribute("libro", new Libro());
        return "libro-form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("libro") Libro libro, BindingResult result,
                        Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("activeTab", "libros");
            return "libro-form";
        }
        try {
            libroService.crear(libro);
            redirectAttributes.addFlashAttribute("successMessage", "Libro registrado correctamente");
            return "redirect:/libros";
        } catch (co.javeriana.dw.biblioteca.exception.NegocioException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "libro-form";
        }
    }
}
