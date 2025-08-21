package train.local.fogpass.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A minimal Landmark entity that creates a database table
 * named 'landmark' with only a primary key column.
 */
@Entity
@Table(name = "landmark") // Specifies the table name as 'landmark'
public class Landmark {

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