package com.mysite.sbb.question;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer>{
	
	Question findBySubject(String subject);
	
	Question findBySubjectAndContent(String subject, String content);

	// 제목에 특정 문자열 포함되어 있는 데이터 조회
	List<Question> findBySubjectLike(String subject);
	
	// 페이징
	Page<Question> findAll(Pageable pageable);
	
	// 검색
	Page<Question> findAll(Specification<Question> spec, Pageable pageable);
	
	// 검색 - Specification 대신 직접 쿼리작성
	@Query("select "
			+ "distinct q "
			+ "from Question q "
			+ "left outer join SiteUser u1 on q.author=u1 "
			+ "left outer join Answer a on a.question=q "
			+ "left outer join SiteUser u2 on a.author=u2 "
			+ "where "
			+ "		q.subject like %:keyword% "
			+ "     or q.content like %:keyword% "
			+ "     or u1.username like %:keyword% "
			+ "     or a.content like %:keyword% "
			+ "     or u2.username like %:keyword% ")
	Page<Question> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);
	
}
