package com.service.event.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.event.domain.Evenement;
import com.service.event.domain.Tag;
import com.service.event.repository.EventRepository;
import com.service.event.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTest {
    @MockBean
    private TagRepository tagRepository;
    @MockBean
    private EventRepository eventRepository;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @InjectMocks
    private TagController tagController;
    @InjectMocks
    private EventController eventController;
    @Mock
    private Tag tag;

    @Mock
    private Evenement evenement;

    @Test
    public void shouldCreateTag() throws Exception {
        Tag tag = new Tag("1", "Devops");

        mockMvc.perform(post("/api/tag/addtag").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isAccepted())
                .andDo(print());

    }

    @Test
    public void shouldReturnListOfRoles() throws Exception {
        List<Tag> tags = new ArrayList<>(
                Arrays.asList(new Tag("1", "jenkins"),
                        new Tag("2", "Docker")
                ));

        when(tagRepository.findAll()).thenReturn(tags);
        mockMvc.perform(get("/api/tag/alltags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(tags.size()))
                .andDo(print());
    }

    @Test
    public void shouldCreateEvent() throws Exception{
        Evenement event=new Evenement(1L,"test event",
                "test event content", LocalDateTime.now());
        mockMvc.perform(post("/api/event/addEvent").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andDo(print());

    }
}
