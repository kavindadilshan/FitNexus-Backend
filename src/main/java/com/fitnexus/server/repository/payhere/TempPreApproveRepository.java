package com.fitnexus.server.repository.payhere;

import com.fitnexus.server.entity.publicuser.TempPreApproveDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempPreApproveRepository extends JpaRepository<TempPreApproveDetails,Long> {

    TempPreApproveDetails findByOrderId(String oid);

}
