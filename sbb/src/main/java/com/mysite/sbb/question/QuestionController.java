package com.mysite.sbb.question;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {
	
	private final QuestionService questionService;
	private final UserService userService;
	
	/* 페이징 전 코드
	 * // 전체 리스트 (처음 화면)
	 * @GetMapping("/list") public String list(Model model) {
	 * 
	 * List<Question> questionList = questionService.getList();
	 * model.addAttribute("questionList", questionList);
	 * 
	 * return "question_list"; }
	 */
	
	@GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="0") int page,
    					@RequestParam(value = "keyword", defaultValue = "") String keyword) {
		
        Page<Question> paging = this.questionService.getList(page, keyword);
        
        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);
        
        return "question_list";
    }
	
	// 질문 1개 조회
	@GetMapping(value = "/detail/{id}")
	public String detail(@PathVariable("id") Integer id, Model model, AnswerForm answerForm) {
		
		Question question = this.questionService.getQuestion(id);
		model.addAttribute("question", question);
		
		return "question_detail";
	}
	
	// 질문 등록 화면
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create")
	public String questionCreate(QuestionForm questionForm) {
		// -> question_form.html의 th:object에 의해 QuestionForm객체가 필요함
		return "question_form";
	}

	/* 
	 * // 질문 등록
	 * 
	 * @PostMapping("/create") public String questionCreate(@RequestParam String
	 * subject, @RequestParam String content, Model model) {
	 * this.questionService.create(subject, content); return
	 * "redirect:/question/list"; }
	 * 
	 */	
	
	
	// 질문 등록(검증)
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
		
		if(bindingResult.hasErrors()) {
			return "question_form";
		}
		
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser);
		
		return "redirect:/question/list";
	}
	
	// 질문 수정
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String questionModify(QuestionForm questionForm , @PathVariable("id") Integer id, Principal principal) {
		
		Question question = this.questionService.getQuestion(id);
		
		if(!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}
		
		questionForm.setSubject(question.getSubject());
		questionForm.setContent(question.getContent());
		
		return "question_form";
	}
	
	// 질문 수정
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {
		
		if(bindingResult.hasErrors()) {
			return "question_form";
		}
		
		Question question = this.questionService.getQuestion(id);
		
		if(!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}
		
		this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
		
		return String.format("redirect:/question/detail/%d", id);
	}
	
	// 질문 삭제
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
		
		// @PathVariable("id") 전달받은 id로 해당 질문정보 찾기
		Question question = this.questionService.getQuestion(id);
		
		// 예외
		if(!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
		}
		
		// 찾은 id로 질문 삭제
		this.questionService.delete(question);
		
		return "redirect:/";
	}
	
	// 추천
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public String questionVote(@PathVariable("id") Integer id, Principal principal) {
		
		Question question = this.questionService.getQuestion(id);
		
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		this.questionService.vote(question, siteUser);
		
		return String.format("redirect:/question/detail/%d", id);
	}
}
