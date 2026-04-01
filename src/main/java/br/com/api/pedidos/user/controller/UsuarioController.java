package br.com.api.pedidos.user.controller;

import br.com.api.pedidos.shared.response.RespostaApi;
import br.com.api.pedidos.user.dto.UsuarioRequestDTO;
import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import br.com.api.pedidos.user.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RespostaApi<UsuarioResponseDTO> cadastrarUsuario(@RequestBody UsuarioRequestDTO usuarioRequestDTO) {
        return RespostaApi.sucesso(usuarioService.cadastrarUsuario(usuarioRequestDTO),
                "Usuario cadastrado com sucesso");
    }

    @GetMapping("/{id}")
    public RespostaApi<UsuarioResponseDTO> buscarUsuarioPorId(@PathVariable Long id){
        return RespostaApi.sucesso(usuarioService.buscarUsuarioPorId(id),
                "Usuário encontrado");
    }

    @GetMapping("/email")
    public RespostaApi<UsuarioResponseDTO> buscarUsuarioPorEmail(@RequestParam String email) {
        return RespostaApi.sucesso(usuarioService.buscarUsuarioPorEmail(email),
                "Usuario encontrado");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.usuario.id or hasRole('ADMIN')")
    public RespostaApi<UsuarioResponseDTO> atualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequestDTO usuarioRequestDTO) {
        return RespostaApi.sucesso(usuarioService.atualizarUsuario(id, usuarioRequestDTO),
                "Usuário atualizado");
    }

}
