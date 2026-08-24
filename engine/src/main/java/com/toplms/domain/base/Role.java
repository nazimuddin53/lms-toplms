package com.toplms.domain.base;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "role")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 50)
    private String  name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "menu_json")
    private Map<String, Object> menuJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Role(String name, Map<String, Object> menuJson) {
        this.name = name;
        this.menuJson = menuJson;
    }

    // Lifecycle hooks to handle dates automatically
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
