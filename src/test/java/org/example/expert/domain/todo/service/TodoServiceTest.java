package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("할일 저장하는 기능이 정상 동작하는지 테스트")
    void saveTodo() {
        //given
        AuthUser author = new AuthUser(1L, "a@a.com", UserRole.USER);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");
        String expectedWeather = "sunny";

        // WeatherClient가 날씨 반환
        given(weatherClient.getTodayWeather()).willReturn(expectedWeather);

        Todo savedTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                expectedWeather,
                User.fromAuthUser(author)
        );
        ReflectionTestUtils.setField(savedTodo, "id", 1L);

        given(todoRepository.save(any(Todo.class))).willReturn(savedTodo);

        // when
        TodoSaveResponse response = todoService.saveTodo(author, todoSaveRequest);

        //then
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getEmail()).isEqualTo("a@a.com");
        assertThat(response.getTitle()).isEqualTo("title");
        assertThat(response.getWeather()).isEqualTo("sunny");
    }

    @Test
    @DisplayName("할일 목록을 페이징하여 출력 기능이 정상 작동하는지 테스트")
    void getTodos() {
        //given
        int page = 1;
        int size = 10;
        User user = new User("a@a.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo1 = new Todo("title1","contents1", "sunny", user);
        ReflectionTestUtils.setField(todo1, "id", 1L);
        Todo todo2 = new Todo("title2","contents2", "sunny", user);
        ReflectionTestUtils.setField(todo2, "id", 2L);

        Page<Todo> todoPage = new PageImpl<>(List.of(todo1, todo2));

        Pageable pageable = PageRequest.of(page - 1, size);
        given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(todoPage);

        //when
        Page<TodoResponse> result = todoService.getTodos(page, size);

        //then
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("title1");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("title2");
    }

    @Test
    @DisplayName("할일 조회 기능이 정상 작동하는지 테스트")
    void getTodo() {
        // given
        long todoId = 1L;
        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "id", 1L);
        User user = new User();
        ReflectionTestUtils.setField(todo, "user", user);

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        //when
        TodoResponse result = todoService.getTodo(todoId);
        //then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("할일 조회 기능이 할일을 조회 못한 경우 오류 처리 테스트")
    void getTodoErrorNotFoundTodo() {
        //given
        long todoId = 1L;
        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.empty());

        //when
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> todoService.getTodo(todoId));

        //then
        assertThat(invalidRequestException.getMessage()).isEqualTo("Todo not found");
    }
}