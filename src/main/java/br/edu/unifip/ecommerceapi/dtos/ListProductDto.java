package br.edu.unifip.ecommerceapi.dtos;

import br.edu.unifip.ecommerceapi.models.Category;
import br.edu.unifip.ecommerceapi.models.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ListProductDto {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private boolean active;
    private String image;

    public ListProductDto(Product produto) {
        this.id = produto.getId();
        this.name = produto.getName();
        this.description = produto.getDescription();
        this.price = produto.getPrice();
        this.category = produto.getCategory();
        this.active = produto.isActive();
        this.image = produto.getImage();
    }
}
