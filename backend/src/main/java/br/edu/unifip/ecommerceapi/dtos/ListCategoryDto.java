package br.edu.unifip.ecommerceapi.dtos;

import br.edu.unifip.ecommerceapi.models.Category;
import lombok.Data;

import java.util.UUID;

@Data
public class ListCategoryDto {

    private UUID id;
    private String name;
    private String description;
    private boolean active;

    public ListCategoryDto(Category category) {

        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.active = category.isActive();

    }

}
