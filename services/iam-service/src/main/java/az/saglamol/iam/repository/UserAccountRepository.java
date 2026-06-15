package az.saglamol.iam.repository;

import az.saglamol.iam.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    Optional<UserAccount> findByPhoneNumber(String phoneNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("""
            select distinct user from UserAccount user
            left join fetch user.roles roles
            where lower(user.email) like lower(concat('%', :query, '%'))
               or user.phoneNumber like concat('%', :query, '%')
            order by user.createdAt desc
            """)
    List<UserAccount> search(String query);
}
