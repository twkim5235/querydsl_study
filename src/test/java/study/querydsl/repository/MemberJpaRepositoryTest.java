package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() throws Exception{
        //given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        List<Member> result = memberJpaRepository.findAll();

        List<Member> result2 = memberJpaRepository.findByUsername("member1");

        //when
        Member findMember = memberJpaRepository.findById(member.getId()).orElse(null);

        //then
        assertThat(findMember).isEqualTo(member);

        assertThat(result).containsExactly(member);

        assertThat(result2).containsExactly(member);
    }

    @Test
    public void basicQuerydslTest() throws Exception{
        //given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        List<Member> result = memberJpaRepository.findAllByQuerydsl();

        List<Member> result2 = memberJpaRepository.findByUsernameByQuerydsl("member1");

        //when

        //then
        assertThat(result).containsExactly(member);

        assertThat(result2).containsExactly(member);
    }
}