package br.com.api.pedidos.user.controller;

import br.com.api.pedidos.user.dto.UsuarioRequestDTO;
import br.com.api.pedidos.user.dto.UsuarioResponseDTO;
import br.com.api.pedidos.user.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public UsuarioResponseDTO cadastrarUsuario(@RequestBody UsuarioRequestDTO usuarioRequestDTO) {
        return  usuarioService.cadastrarUsuario(usuarioRequestDTO);
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO buscarUsuarioPorId(@PathVariable Long id){
        return usuarioService.buscarUsuarioPorId(id);
    }

    @GetMapping("/email")
    public UsuarioResponseDTO buscarUsuarioPorEmail(@RequestParam String email) {
        return usuarioService.buscarUsuarioPorEmail(email);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.usuario.id or hasRole('ADMIN')")
    public UsuarioResponseDTO atualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequestDTO usuarioRequestDTO) {
        return usuarioService.atualizarUsuario(id, usuarioRequestDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void removerUsuario(@PathVariable Long id) {
        usuarioService.removerUsuario(id);
    }

}
