package com.mysite.sbb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;

@SpringBootTest
public class AnswerTest {
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private AnswerRepository answerRepository;
	
	@Test
	void answer_save() {
		//어떤 질문에 답변 달 것인지 조회
		Optional<Question> oq = this.questionRepository.findById(2);
		assertTrue(oq.isPresent());
		Question q = oq.get();
		
		//답변 저장
		Answer a = new Answer();
		a.setContent("네 자동으로 생성됩니다.");
		a.setQuestion(q); // 어떤 질문의 답변인지 알기 위해서 Question 객체 필요
		a.setCreateDate(LocalDateTime.now());
		this.answerRepository.save(a);
	}
	
	@Test
	void answer_selectOne() {
		// 답변 id 1인 값 조회
		Optional<Answer> oa = this.answerRepository.findById(1);
		assertTrue(oa.isPresent());
		Answer a = oa.get();
		
		// 답변 id=1 의 질문 id가 2인지 
		assertEquals(2, a.getQuestion().getId()); 
	}
	
	// 삭제
	@Test
	void answer_delete() {
		// 삭제할 답변의 질문 id
		Optional<Question> oq = this.questionRepository.findById(2);
		assertTrue(oq.isPresent());
		Question q = oq.get();
		
		// 삭제 전 수
		assertEquals(4, this.answerRepository.count());
		
		// 삭제하기
		// 삭제할 답변 id 확인
		Optional<Answer> oa = this.answerRepository.findById(3);
		assertTrue(oa.isPresent());
		Answer a = oa.get();
		
		Optional<Answer> oa2 = this.answerRepository.findById(4);
		assertTrue(oa2.isPresent());
		Answer a2 = oa2.get();
		
		this.answerRepository.delete(a);
		this.answerRepository.delete(a2);
		
		assertEquals(2, this.answerRepository.count());
		
		
	}

}
