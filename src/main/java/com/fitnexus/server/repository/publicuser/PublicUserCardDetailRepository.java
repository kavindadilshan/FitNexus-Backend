package com.fitnexus.server.repository.publicuser;

import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.entity.publicuser.PublicUserCardDetail;
import com.fitnexus.server.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PublicUserCardDetailRepository extends JpaRepository<PublicUserCardDetail, Long> {
    PublicUserCardDetail findByStripePaymentMethodId(String paymentMethodId);
    PublicUserCardDetail findByStripePaymentMethodIdOrPayHerePaymentMethodId(String stipeId,String payhereId);
    PublicUserCardDetail findByPayHerePaymentMethodId(String paymentMethodId);
    List<PublicUserCardDetail> findAllByStatusNotAndPublicUser(CardStatus status, PublicUser publicUser);

    List<PublicUserCardDetail> findPublicUserCardDetailsByPublicUser(PublicUser publicUser);
}
