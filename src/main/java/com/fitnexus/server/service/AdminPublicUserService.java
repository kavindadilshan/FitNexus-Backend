package com.fitnexus.server.service;

import com.fitnexus.server.dto.classsession.SessionEnrollPublicDTO;
import com.fitnexus.server.dto.common.ChartDataDTO;
import com.fitnexus.server.dto.common.DashBoardDTO;
import com.fitnexus.server.dto.instructor.InstructorPackageEnrollPublicDTO;
import com.fitnexus.server.dto.membership.MembershipEnrollPublicDTO;
import com.fitnexus.server.dto.packages.PublicUserPackageSubscribeDTO;
import com.fitnexus.server.dto.publicuser.PublicUserAdminDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface AdminPublicUserService {

    long getNumberOfUsers();

    long getTotalNumberOfEnrollments(String username);

    DashBoardDTO getDashBoarDetails(String username);

    Page<PublicUserAdminDTO> getAllUsers(Pageable pageable, String username);

    PublicUserAdminDTO getPublicUserById(long id);

    ChartDataDTO getChartData(LocalDateTime start, LocalDateTime end, String username);

    Page<PublicUserAdminDTO> searchUsers(String data, Pageable pageable, String username);

    Page<InstructorPackageEnrollPublicDTO> getInstructorEnrollments(long id, Pageable pageable);

    Page<SessionEnrollPublicDTO> getOnlineClassEnrollments(long id, Pageable pageable);

    Page<SessionEnrollPublicDTO> getOnlinePersonalClassEnrollments(long id, Pageable pageable);

    Page<SessionEnrollPublicDTO> getPhysicalClassEnrollments(long id, Pageable pageable);

    Page<MembershipEnrollPublicDTO> getPhysicalCLassMembershipEnrollments(long id, Pageable pageable);

    Page<MembershipEnrollPublicDTO> getGymMembershipEnrollments(long id, Pageable pageable);

    Page<PublicUserPackageSubscribeDTO> getPackageSubscriptionsForPublicUser(long id, Pageable pageable);

    void deletePublicUser(String mobile);
}
