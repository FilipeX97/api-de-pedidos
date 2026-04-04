package br.com.api.pedidos.config;

import br.com.api.pedidos.product.entity.Produto;
import br.com.api.pedidos.product.repository.ProdutoRepository;
import br.com.api.pedidos.user.entity.Perfil;
import br.com.api.pedidos.user.entity.Usuario;
import br.com.api.pedidos.user.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Random;

@Configuration
public class CarregadorDadosIniciais {

    private final Random random = new Random();

    @Bean
    public CommandLineRunner gerarMassa(
            UsuarioRepository usuarioRepository,
            ProdutoRepository produtoRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (usuarioRepository.count() > 0) return;

            usuarioRepository.save(new Usuario(
                    "Admin",
                    "admin@api.com",
                    passwordEncoder.encode("123456"),
                    Perfil.ADMIN
            ));

            for (int i = 1; i <= 100; i++) {
                usuarioRepository.save(new Usuario(
                        "Usuario " + i,
                        "user" + i + "@teste.com",
                        passwordEncoder.encode("123456"),
                        Perfil.USER
                ));
            }

            for (int i = 1; i <= 200; i++) {

                produtoRepository.save(new Produto(
                        gerarNomeProduto(i),
                        "Descrição do produto " + i,
                        gerarPreco(),
                        gerarEstoque()
                ));
            }

            System.out.println("Massa de dados gerada com sucesso!");
        };
    }

    private String gerarNomeProduto(int i) {
        String[] nomes = {
                "Notebook", "Mouse", "Teclado", "Monitor",
                "Cadeira Gamer", "Headset", "Webcam"
        };

        return nomes[random.nextInt(nomes.length)] + " " + i;
    }

    private BigDecimal gerarPreco() {
        double valor = 50 + (5000 - 50) * random.nextDouble();
        return BigDecimal.valueOf(valor).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private Long gerarEstoque() {
        return (long) random.nextInt(100);
    }
}
