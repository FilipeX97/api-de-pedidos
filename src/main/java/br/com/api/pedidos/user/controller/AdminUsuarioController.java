package br.com.api.pedidos.user.controller;

import br.com.api.pedidos.shared.response.RespostaApi;
import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import br.com.api.pedidos.user.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public RespostaApi<List<UsuarioResponseDTO>> listarUsuarios() {
        return RespostaApi.sucesso(usuarioService.listarUsuarios(),
                "Lista de usuários");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/ativar")
    public void ativar(@PathVariable Long id) {
        usuarioService.ativarUsuario(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public void desativar(@PathVariable Long id) {
        usuarioService.desativarUsuario(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public RespostaApi<Void> removerUsuario(@PathVariable Long id) {
        usuarioService.removerUsuario(id);
        return RespostaApi.sucesso(null, "Usuário removido com sucesso");
    }

}
