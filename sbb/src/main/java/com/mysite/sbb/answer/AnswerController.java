package com.mysite.sbb.answer;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@RequestMapping("/answer")
@Controller
public class AnswerController {
	
	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;

	// https://wikidocs.net/161357 2-11 답변등록
	
	/*
	 * @PostMapping("/create/{id}") public String createAnswer(@PathVariable("id")
	 * Integer id, @RequestParam String content, Model model) {
	 * 
	 * log.info(this.getClass().getName()); //com.mysite.sbb.answer.AnswerController
	 * 
	 * // ${question.id}로 답변 작성할 질문 가져오기 Question question =
	 * this.questionService.getQuestion(id);
	 * 
	 * // 가져온 질문에 답변 달기 this.answerService.create(question, content);
	 * 
	 * // TODO: 답변을 저장한다. return String.format("redirect:/question/detail/%d", id);
	 * //return "redirect:/question/detail/id"; }
	 */
	
	// 검증추가
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createAnswer(Model model, @PathVariable("id") Integer id, @Valid AnswerForm answerForm, BindingResult bindingResult, 
								Principal principal) {
		
		log.info(this.getClass().getName()); //com.mysite.sbb.answer.AnswerController
		
		Question question = this.questionService.getQuestion(id);
		
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		if(bindingResult.hasErrors()) {
			// ${question.id}로 답변 작성할 질문 가져오기
			model.addAttribute("question", question);
			return "question_detail";
		}
		
		// 가져온 질문에 답변 달기
		Answer answer = this.answerService.create(question, answerForm.getContent(), siteUser);
		
		// TODO: 답변을 저장한다.
		return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());

//		return String.format("redirect:/question/detail/%d", id);
		//return "redirect:/question/detail/id";
	}
	
	// 답변 수정
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String answerModify(Principal principal, @PathVariable("id") Integer id, AnswerForm answerForm) {
		
		Answer answer = this.answerService.getAnswer(id);
		
		if(!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}
		
		answerForm.setContent(answer.getContent());
		
		return "answer_form";
	}
	
	// 답변 수정
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult,
								@PathVariable("id") Integer id, Principal principal) {
		
		// 검증 에러시
		if(bindingResult.hasErrors()) {
			return "answer_form";
		}
		
		Answer answer = this.answerService.getAnswer(id);
		
		if(!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}
		
		this.answerService.modify(answer, answerForm.getContent());
		
//		return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
		return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());

	}
	
	// 답변 삭제
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String answerDelete(@PathVariable("id") Integer id, Principal principal) {
		
		Answer answer = this.answerService.getAnswer(id);
		
		if(!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
		}
		
		this.answerService.delete(answer);
		
		return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
	}
	
	// 추천
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public String answerVote(Principal principal, @PathVariable("id") Integer id) {
		
		Answer answer = this.answerService.getAnswer(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		this.answerService.vote(answer, siteUser);
		
//		return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
		return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
	}

}
