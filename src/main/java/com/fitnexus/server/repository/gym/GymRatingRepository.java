package com.fitnexus.server.repository.gym;

import com.fitnexus.server.entity.gym.Gym;
import com.fitnexus.server.entity.gym.GymRating;
import com.fitnexus.server.entity.publicuser.PublicUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GymRatingRepository extends JpaRepository<GymRating, Long> {

    List<GymRating> findGymRatingsByPublicUserAndGym(PublicUser publicUser, Gym gym);

    Page<GymRating> findGymRatingsByGym(Gym gym, Pageable pageable);
}
