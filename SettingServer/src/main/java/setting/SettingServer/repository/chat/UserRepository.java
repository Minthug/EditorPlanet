package setting.SettingServer.repository.chat;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import setting.SettingServer.entity.Member;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Member, String> {
    //id를 가지고 User 조회 (lazy 로딩으로 n+1 해결)
//    @EntityGraph(attributePaths = {"authorities", "file"})
//    Optional<Member> findWithAuthoritiesAndFileById(String userId);

    //id를 가지고 User 조회 (lazy 로딩으로 n+1 해결)
//    @EntityGraph(attributePaths = {"file"})
//    Optional<Member> findWithFileById(String userId);


    //id를 가지고 User 조회 (lazy 로딩으로 n+1 해결)
//    @EntityGraph(attributePaths = {"authorities"})
//    Optional<Member> findWithAuthoritiesById(String userId);

/*    //id를 가지고 User 조회 (lazy 로딩으로 n+1 해결)
    @EntityGraph(attributePaths = {"authorities", "file"})
    @Query("select u from User u left join fetch u.file where u.id = :userId")
    Optional<User> findAllJPQLFetch(String userId);*/

    //이메일 찾기
//    Optional<Member> findByEmail(String email);
//
//    //활성화된 사용자(계정) 반환
//    Member findByIdAndActiveIsTrue(String id);
//
//    //email와 id가 일치하는 활성화되지 않은 사용자
//    Optional<Member> findByEmailAndId(String email, String id);
}
