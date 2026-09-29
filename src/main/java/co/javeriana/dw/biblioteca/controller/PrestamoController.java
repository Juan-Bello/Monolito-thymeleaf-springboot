package co.javeriana.dw.biblioteca.controller;

import co.javeriana.dw.biblioteca.entity.Prestamo;
import co.javeriana.dw.biblioteca.exception.NegocioException;
import co.javeriana.dw.biblioteca.service.EstudianteService;
import co.javeriana.dw.biblioteca.service.LibroService;
import co.javeriana.dw.biblioteca.service.PrestamoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;
    private final LibroService libroService;
    private final EstudianteService estudianteService;

    public PrestamoController(PrestamoService prestamoService, LibroService libroService, EstudianteService estudianteService) {
        this.prestamoService = prestamoService;
        this.libroService = libroService;
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("activeTab", "prestamos");
        model.addAttribute("prestamos", prestamoService.listar());
        return "prestamos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("activeTab", "prestamos");
        model.addAttribute("libros", libroService.listarConDisponibilidad());
        model.addAttribute("estudiantes", estudianteService.listar());
        model.addAttribute("prestamoForm", new PrestamoForm());
        return "prestamo-form";
    }

    @PostMapping
    public String crear(@ModelAttribute PrestamoForm form, Model model, RedirectAttributes redirectAttributes) {
        try {
            Prestamo prestamo = prestamoService.solicitarPrestamo(form.getLibroId(), form.getEstudianteId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Préstamo registrado: fecha límite " + prestamo.getFechaLimite());
            return "redirect:/prestamos";
        } catch (NegocioException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("libros", libroService.listarConDisponibilidad());
            model.addAttribute("estudiantes", estudianteService.listar());
            model.addAttribute("prestamoForm", form);
            return "prestamo-form";
        }
    }

    @PostMapping("/{id}/devolver")
    public String devolver(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Prestamo prestamo = prestamoService.devolver(id);
            if (prestamo.getMoraDias() != null && prestamo.getMoraDias() > 0) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Devolución registrada con mora de " + prestamo.getMoraDias() + " día(s)");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Devolución registrada a tiempo");
            }
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/prestamos";
    }
}
