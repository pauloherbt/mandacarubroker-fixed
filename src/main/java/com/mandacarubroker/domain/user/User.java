package com.mandacarubroker.domain.user;

import com.mandacarubroker.domain.Role;
import com.mandacarubroker.dtos.RequestUserDTO;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity(name = "user")
@Table(name = "tb_user")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of="id")
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.UUID) @NonNull
    private String id;
    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private String email;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private LocalDate birthDate;
    @NonNull
    private Double balance;
    @Enumerated(EnumType.STRING)
    private Role role;

    public User(RequestUserDTO dto){
        setUsername(dto.username());
        setEmail(dto.email());
        setFirstName(dto.firstName());
        setLastName(dto.lastName());
        setBirthDate(dto.birthDate());
        setBalance(dto.balance());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toString()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
