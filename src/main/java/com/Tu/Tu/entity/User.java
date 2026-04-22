package com.Tu.Tu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String email;

    String password;
    String fullName;
    LocalDate dob;
    String phone;
    LocalDate createAt;
    Boolean active;
    Boolean emailVerified;
    Boolean googleLinked;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name", referencedColumnName = "name")
    )
    Set<Role> roles;

    @OneToMany(mappedBy = "user")
    List<Review> reviewList;

    @OneToMany(mappedBy = "user")
    List<Booking> bookingList;

    @OneToMany(mappedBy = "user")
    List<Tour> tourList;
}