package com.finan.orcamento.controller;

import com.finan.orcamento.model.UsuarioModel;
import com.finan.orcamento.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @InitBinder("usuarioModel")
    public void configurarFormulario(WebDataBinder binder) {
        binder.setAllowedFields("nomeUsuario", "cpf", "dataNascimento");
    }

    @GetMapping({"", "/pesquisa"})
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.buscarUsuario());
        model.addAttribute("usuarioModel", new UsuarioModel());
        return "usuarioPage";
    }

    @PostMapping
    public String cadastrarUsuario(@Valid @ModelAttribute("usuarioModel") UsuarioModel usuario,
                                   BindingResult resultado, Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("usuarios", usuarioService.buscarUsuario());
            return "usuarioPage";
        }
        usuarioService.cadastrarUsuario(usuario);
        redirect.addFlashAttribute("sucesso", "Usuário cadastrado com sucesso!");
        return "redirect:/usuarios";
    }
}
