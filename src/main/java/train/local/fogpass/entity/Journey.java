package train.local.fogpass.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * This entity will create a table named 'journey' with a single primary key column.
 */
@Entity
@Table(name = "journey")
public class Journey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // You can add other entities here as well, like:
    // @Entity
    // @Table(name = "passenger")
    // public class Passenger { ... }
}