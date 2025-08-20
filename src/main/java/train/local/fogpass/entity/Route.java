package train.local.fogpass.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A minimal Route entity that creates a database table
 * named 'route' with only a primary key column.
 */
@Entity
@Table(name = "route") // Specifies the table name as 'route'
public class Route {

    @Id // Marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures auto-generation for the ID.
    private Long id;

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}