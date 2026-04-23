package pl.gogacz.planner.core.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String type;
    @Enumerated(EnumType.STRING)
    private ResourceStatus status = ResourceStatus.AVAILABLE;
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
