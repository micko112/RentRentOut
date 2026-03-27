package org.landm.repository;

import org.landm.dto.admin.UserCreditSummaryDto;
import org.landm.entity.CreditTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {
    Page<CreditTransaction> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<CreditTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        SELECT new org.landm.dto.admin.UserCreditSummaryDto(
            u.id,
            CONCAT(u.firstname, ' ', u.lastname),
            u.email,
            u.credit,
            COALESCE(SUM(CASE WHEN ct.amount < 0 THEN ct.amount * -1 ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN ct.amount > 0 THEN ct.amount ELSE 0 END), 0),
            MAX(ct.createdAt)
        )
        FROM User u
        LEFT JOIN CreditTransaction ct ON ct.user.id = u.id
        WHERE u.role.name = 'ROLE_USER'
            AND (:search IS NULL OR :search = ''
                 OR LOWER(CONCAT(u.firstname, ' ', u.lastname)) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
        GROUP BY u.id, u.firstname, u.lastname, u.email, u.credit
        """)
    Page<UserCreditSummaryDto> findUserCreditSummaries(@Param("search") String search, Pageable pageable);
}
