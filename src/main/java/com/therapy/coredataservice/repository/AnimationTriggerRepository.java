package com.therapy.coredataservice.repository;

import com.therapy.coredataservice.entity.AnimationTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimationTriggerRepository extends JpaRepository<AnimationTrigger, String> {
}
