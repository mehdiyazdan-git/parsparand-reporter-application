package com.armaninvestment.parsparandreporterapplication.repositories;

import com.armaninvestment.parsparandreporterapplication.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    @Query("select (count(u) > 0) from User u where u.username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("select (count(u) > 0) from User u where u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("select (count(u) > 0) from User u where u.username = :username and u.id <> :id")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Integer id);

    @Query("select (count(u) > 0) from User u where u.email = :email and u.id <> :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Integer id);
}