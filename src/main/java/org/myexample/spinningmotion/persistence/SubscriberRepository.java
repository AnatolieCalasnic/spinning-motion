package org.myexample.spinningmotion.persistence;


import org.myexample.spinningmotion.persistence.entity.SubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<SubscriberEntity, Long> {
    boolean existsByEmail(String email);
    Optional<SubscriberEntity> findByEmail(String email);}
