package com.restaurent.manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @ManyToMany(mappedBy = "permissions",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private Set<Package> packages = new HashSet<>();
    private int maximum;
    public void addPermissionToPackage(Package pack){
        pack.getPermissions().add(this);
        packages.add(pack);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission )) return false;
        return id != null && id.equals(((Permission) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
