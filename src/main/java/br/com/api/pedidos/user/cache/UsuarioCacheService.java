package br.com.api.pedidos.user.cache;

import br.com.api.pedidos.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class UsuarioCacheService {

    @Caching(evict = {
            @CacheEvict(value = CacheNames.USUARIOS_DTO, key = "#email"),
            @CacheEvict(value = CacheNames.USUARIOS_AUTH, key = "#email")
    })
    public void removerCacheUsuario(String email) {
    }

}
