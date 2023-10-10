package com.mysite.sbb.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.SiteUser;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionService {
	
	private final QuestionRepository questionRepository;
	
	
	/* 페이징 처리 전 코드
	 * // 전체 리스트 조회
	 * public List<Question> getList(){ return this.questionRepository.findAll(); }
	 */
	
	// 전체 리스트 조회
	public Page<Question> getList(int page, String keyword) {
		
		// 게시물 역순 조회
		List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		
		//페이징 처리 추가
		/*지금까지 만든 페이징 기능에 '처음'과 '마지막' 링크를 추가하고 
		 * '…' 생략 기호까지 추가하면 더 완성도 높은 페이징 기능이 될 것이다.


		 * */
		
		// 검색 방법1 - Specification
//		Specification<Question> spec = search(keyword);
//		
//		return this.questionRepository.findAll(spec, pageable);
		
		// 검색 방법2 - 쿼리
		return this.questionRepository.findAllByKeyword(keyword, pageable);
	}
	
	// 질문 1개 조회
	public Question getQuestion(Integer id) {
		Optional<Question> question = this.questionRepository.findById(id);
		
		if(question.isPresent()) {
			return question.get();
		} else {
			throw new DataNotFoundException("question not found");
		}
	}

	// 질문 등록
	public void create(String subject, String content, SiteUser user) {
		Question q = new Question();
		
		// 이부분도 dto를 만들어서 바꿀수 있는지(빌더나 메소드)
		q.setSubject(subject);
		q.setContent(content);
		q.setCreateDate(LocalDateTime.now());
		q.setAuthor(user);
		
		// TODO Auto-generated method stub
		this.questionRepository.save(q);
	}
	
	// 질문 수정
	public void modify(Question question, String subject, String content) {
		
		question.setSubject(subject);
		question.setContent(content);
		question.setModifyDate(LocalDateTime.now());
		
		this.questionRepository.save(question);
	}
	
	
	// 질문 삭제
	public void delete(Question question) {
		this.questionRepository.delete(question);
	}

	// 추천
	public void vote(Question question, SiteUser siteUser) {
		// TODO Auto-generated method stub
		question.getVoter().add(siteUser);
		this.questionRepository.save(question);
		
	}
	
	// 검색
	private Specification<Question> search(String keyword) {
		return new Specification<Question>() {
			
			private static final long serilVersionUID = 1L;
			
			@Override
			public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				
				query.distinct(true);//증복제거
				
				Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
				Join<Question, Answer> a  = q.join("answerList", JoinType.LEFT);
				Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
				
				return criteriaBuilder.or(criteriaBuilder.like(q.get("subject"), "%" + keyword +"%"), //제목
											criteriaBuilder.like(q.get("content"), "%" + keyword +"%"), // 내용
											criteriaBuilder.like(u1.get("username"), "%" + keyword +"%"), // 질문작성자
											criteriaBuilder.like(a.get("content"), "%" + keyword +"%"), // 답변 내용
											criteriaBuilder.like(u2.get("username"), "%" + keyword +"%")); // 답변 작성자
			}
		};
		
	}

}
