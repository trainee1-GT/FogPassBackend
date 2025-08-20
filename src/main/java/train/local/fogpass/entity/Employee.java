package train.local.fogpass.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "employee")
public class Employee {

    // Using String as per request; column names kept exactly as provided
    @Id
    @Column(name = "EmpId", nullable = false, length = 50)
    private String empId;

    @Column(name = "EmpName", length = 100)
    private String empName;

    @Column(name = "Des", length = 100)
    private String des;

    @Column(name = "Dept", length = 100)
    private String dept;

    @Column(name = "pwd", length = 255)
    private String pwd;

    @Column(name = "BOD", length = 20)
    private String bod;
}