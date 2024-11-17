package org.example.datingapp.repositories;

import org.example.datingapp.models.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByEmail(String keyword);

    List<Profile> findByOpenInfoContainingIgnoreCase(String keyword);
}