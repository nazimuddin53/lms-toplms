package com.toplms.domain.base;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "subscription_plan")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {
    @Id
    @Column(nullable = false, unique = true, length = 30)
    private String id; // 'FREE', 'GROWTH', 'ENTERPRISE'

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "max_courses", nullable = false)
    private int maxCourses; // -1 for unlimited

    @Column(name = "max_students", nullable = false)
    private int maxStudents; // -1 for unlimited

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "modules_json", columnDefinition = "jsonb")
    private Map<String, Boolean> modulesJSON;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_json", columnDefinition = "jsonb")
    private Map<String, Double> priceJSON;
}
