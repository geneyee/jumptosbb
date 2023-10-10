package com.mysite.sbb.answer;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AnswerService {
	
	private final AnswerRepository answerRepository;
	
	public Answer create(Question question, String content, SiteUser author) {
		
		Answer answer = new Answer();
		
		// 이부분을 빌드? 혹은 메소드로 만들수 있지 않을까..
		answer.setContent(content);
		answer.setCreateDate(LocalDateTime.now());
		answer.setQuestion(question);
		answer.setAuthor(author);
		
		this.answerRepository.save(answer);
		 
		return answer;
	}
	
	// id로 정보 찾기
	public Answer getAnswer(Integer id) {
		
		Optional<Answer> answer =  this.answerRepository.findById(id);
		
		//예외처리
		if(answer.isPresent()) {
			return answer.get();
		} else {
			throw new DataNotFoundException("answer not found");
		}
	}

	// 답변 수정
	public void modify(Answer answer, String content) {
		// TODO Auto-generated method stub
		answer.setContent(content);
		answer.setCreateDate(LocalDateTime.now());
		this.answerRepository.save(answer);
	}
	
	// 답변 삭제
	public void delete(Answer answer) {
		// TODO Auto-generated method stub
		this.answerRepository.delete(answer);
		
	}

	// 추천
	public void vote(Answer answer, SiteUser siteUser) {
		// TODO Auto-generated method stub
		answer.getVoter().add(siteUser);
		this.answerRepository.save(answer);
	}

}
