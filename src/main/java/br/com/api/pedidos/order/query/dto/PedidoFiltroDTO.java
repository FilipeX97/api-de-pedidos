package br.com.api.pedidos.order.query.dto;

import br.com.api.pedidos.order.state.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        name = "PedidoFiltro",
        description = """
                Filtros opcionais utilizados na consulta administrativa
                de pedidos.

                Quando nenhum filtro é informado, todos os pedidos são
                considerados na consulta.
                """
)
public record PedidoFiltroDTO(
        @Schema(
                description = "Filtra os pedidos pelo status atual",
                example = "PAGO",
                allowableValues = {
                        "CRIADO",
                        "AGUARDANDO_PAGAMENTO",
                        "PAGO",
                        "ENVIADO",
                        "ENTREGUE",
                        "CANCELAMENTO_SOLICITADO",
                        "ESTORNADO",
                        "CANCELADO"
                },
                implementation = StatusPedido.class
        )
        StatusPedido status,

        @Schema(
                description = """
                        Filtra os pedidos pelo identificador do usuário
                        proprietário
                        """,
                example = "10",
                minimum = "1"
        )
        @Positive(
                message = "Id do usuário deve ser maior que zero"
        )
        Long idUsuario,

        @Schema(
                description = """
                        Filtra os pedidos pelo e-mail exato do cliente.

                        A comparação ignora diferenças entre letras
                        maiúsculas e minúsculas.
                        """,
                example = "cliente@exemplo.com",
                format = "email",
                maxLength = 255
        )
        @Email(message = "E-mail do cliente inválido")
        @Size(
                max = 255,
                message = """
                        E-mail do cliente deve ter no máximo 255 caracteres
                        """
        )
        String emailCliente,

        @Schema(
                description = """
                        Data inicial do período de criação dos pedidos.

                        A data inicial é incluída na consulta.
                        """,
                example = "2026-07-01",
                format = "date"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataInicio,

        @Schema(
                description = """
                        Data final do período de criação dos pedidos.

                        Todo o dia informado é incluído na consulta.
                        """,
                example = "2026-07-31",
                format = "date"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataFim,

        @Schema(
                description = """
                        Valor final mínimo do pedido.

                        Pedidos com valor igual ao mínimo também são
                        incluídos.
                        """,
                example = "100.00",
                minimum = "0.00"
        )
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Valor mínimo não pode ser negativo"
        )
        BigDecimal valorMinimo,

        @Schema(
                description = """
                        Valor final máximo do pedido.

                        Pedidos com valor igual ao máximo também são
                        incluídos.
                        """,
                example = "1000.00",
                minimum = "0.00"
        )
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Valor máximo não pode ser negativo"
        )
        BigDecimal valorMaximo,

        @Schema(
                description = """
                        Filtra os pedidos pelo código exato do cupom aplicado.

                        A comparação ignora diferenças entre letras
                        maiúsculas e minúsculas.
                        """,
                example = "DESCONTO15",
                maxLength = 255
        )
        @Size(
                max = 255,
                message = """
                        Código do cupom deve ter no máximo 255 caracteres
                        """
        )
        String codigoCupom
) {
}