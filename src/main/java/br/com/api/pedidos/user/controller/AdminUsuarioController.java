package br.com.api.pedidos.user.controller;

import br.com.api.pedidos.shared.pagination.dto.PaginaResponseDTO;
import br.com.api.pedidos.shared.response.RespostaApi;
import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import br.com.api.pedidos.user.service.UsuarioService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public RespostaApi<PaginaResponseDTO<UsuarioResponseDTO>> listarUsuarios(
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "nome",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return RespostaApi.sucesso(
                usuarioService.listarUsuarios(pageable),
                "Lista de usuários"
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/ativar")
    public RespostaApi<Void> ativar(@PathVariable Long id) {
        usuarioService.ativarUsuario(id);
        return RespostaApi.sucesso(null, "Usuario ativado com sucesso");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public RespostaApi<Void> desativar(@PathVariable Long id) {
        usuarioService.desativarUsuario(id);
        return RespostaApi.sucesso(null, "Usuario desativado com sucesso");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public RespostaApi<Void> removerUsuario(@PathVariable Long id) {
        usuarioService.removerUsuario(id);
        return RespostaApi.sucesso(null, "Usuário removido com sucesso");
    }

}
