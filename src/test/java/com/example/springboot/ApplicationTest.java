package com.example.springboot;

import com.example.springboot.Application;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class ApplicationTest {
    public static final String POSTAPI="/api?post_input_text=";
    public static final String DELETE="/delete?post_text=";
    public static final String HISTORY="/history";


    public static final String DELETE_SUCCESS_MSG="The requested post has been deleted from history.";
    public static final String DELETE_FAIL_MSG="The requested post does not exist in the history.";
    @Autowired
    private MockMvc mockMvc;

    //TestCase 1: Deleted messages not shown in "/history" page
    @Test
    void testDeleteFromHistoryFunctionality() throws Exception{
        //Making a bunch of posts to fill the post log
        mockMvc.perform(MockMvcRequestBuilders.post(POSTAPI+"DeleteMe!")).andReturn();
        mockMvc.perform(MockMvcRequestBuilders.post(POSTAPI+"fillerpost1!")).andReturn();
        mockMvc.perform(MockMvcRequestBuilders.post(POSTAPI+"fillerpost2!")).andReturn();
        mockMvc.perform(MockMvcRequestBuilders.post(POSTAPI+"fillerpost3!")).andReturn();

        //Deleting "DeleteMe!" and expecting success message
        mockMvc.perform(MockMvcRequestBuilders.post(DELETE+"DeleteMe!"))
                .andExpect(content().string(containsString(DELETE_SUCCESS_MSG)));

        //Trying to delete "DeleteMe!" again and expecting failure message
        mockMvc.perform(MockMvcRequestBuilders.post(DELETE+"DeleteMe!"))
                .andExpect(content().string(containsString(DELETE_FAIL_MSG)));

        //Checking History page and expecting "DeleteMe!" to be absent, but all other posts remain unchanged
        mockMvc.perform(MockMvcRequestBuilders.get(HISTORY)
                .contentType(MediaType.ALL))
                .andExpect(content().string(Matchers.not(containsString("DeleteMe!"))))
                .andExpect(content().string(containsString("fillerpost1!")))
                .andExpect(content().string(containsString("fillerpost2!")))
                .andExpect(content().string(containsString("fillerpost3!")));

    }

    //TestCase 2: Case Sensitivity
    @Test
    void testDeleteCaseSensitivity() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.post(POSTAPI+"HELLO")).andReturn();
        mockMvc.perform(MockMvcRequestBuilders.post(POSTAPI+"hello")).andReturn();
        mockMvc.perform(MockMvcRequestBuilders.post(POSTAPI+"HeLlO")).andReturn();
        mockMvc.perform(MockMvcRequestBuilders.post(POSTAPI+"hElLo")).andReturn();

        //Deleting "hello" and expecting successful delete
        mockMvc.perform(MockMvcRequestBuilders.post(DELETE+"hello"))
                .andExpect(content().string(containsString(DELETE_SUCCESS_MSG)));

        //Deleting "hello" and expecting failed delete
        mockMvc.perform(MockMvcRequestBuilders.post(DELETE+"hello"))
                .andExpect(content().string(containsString(DELETE_FAIL_MSG)));


        //Expecting to not see "hello", but still see case variants
        mockMvc.perform(MockMvcRequestBuilders.get(HISTORY)
                .contentType(MediaType.ALL))
                .andExpect(content().string(Matchers.not(containsString("hello"))))
                .andExpect(content().string(containsString("HELLO")))
                .andExpect(content().string(containsString("HeLlO")))
                .andExpect(content().string(containsString("hElLo")));
    }
}
