package br.edu.unifip.ecommerceapi.models;

import br.edu.unifip.ecommerceapi.dtos.CategoryDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@Table(name = "TB_CATEGORY")
@AllArgsConstructor
@NoArgsConstructor
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(nullable = false, length = 70)
    private String name;
    @Column(nullable = false, length = 130)
    private String description;
    private boolean active = true;

    public Category(CategoryDto categoryDto) {
        this.name = categoryDto.getName();
        this.description = categoryDto.getDescription();
    }

}
