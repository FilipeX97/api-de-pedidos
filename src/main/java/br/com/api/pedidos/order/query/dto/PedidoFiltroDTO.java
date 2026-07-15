package br.com.api.pedidos.order.query.dto;

import br.com.api.pedidos.order.state.StatusPedido;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PedidoFiltroDTO(
        StatusPedido status,

        @Positive(message = "Id do usuário deve ser maior que zero")
        Long idUsuario,

        @Email(message = "E-mail do cliente inválido")
        @Size(
                max = 255,
                message = "E-mail do cliente deve ter no máximo 255 caracteres"
        )
        String emailCliente,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataInicio,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataFim,

        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Valor mínimo não pode ser negativo"
        )
        BigDecimal valorMinimo,

        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Valor máximo não pode ser negativo"
        )
        BigDecimal valorMaximo,

        @Size(
                max = 255,
                message = "Código do cupom deve ter no máximo 255 caracteres"
        )
        String codigoCupom
) {
}