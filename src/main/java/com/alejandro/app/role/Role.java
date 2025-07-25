package com.alejandro.app.role;

import com.alejandro.app.common.BaseEntity;
import com.alejandro.app.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ROLES")
@EntityListeners(AuditingEntityListener.class)
public class Role extends BaseEntity {

    @Column(name = "NAME", nullable = false)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

}
