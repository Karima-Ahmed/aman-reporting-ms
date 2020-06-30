package com.isoft.reporting.web.rest;

import com.isoft.reporting.ReportingApp;
import com.isoft.reporting.config.SecurityBeanOverrideConfiguration;
import com.isoft.reporting.domain.Email;
import com.isoft.reporting.domain.Employee;
import com.isoft.reporting.repository.EmailRepository;
import com.isoft.reporting.service.EmailService;
import com.isoft.reporting.service.dto.EmailDTO;
import com.isoft.reporting.service.mapper.EmailMapper;
import com.isoft.reporting.service.dto.EmailCriteria;
import com.isoft.reporting.service.EmailQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link EmailResource} REST controller.
 */
@SpringBootTest(classes = { SecurityBeanOverrideConfiguration.class, ReportingApp.class })

@AutoConfigureMockMvc
@WithMockUser
public class EmailResourceIT {

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private EmailMapper emailMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailQueryService emailQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEmailMockMvc;

    private Email email;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Email createEntity(EntityManager em) {
        Email email = new Email()
            .address(DEFAULT_ADDRESS);
        return email;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Email createUpdatedEntity(EntityManager em) {
        Email email = new Email()
            .address(UPDATED_ADDRESS);
        return email;
    }

    @BeforeEach
    public void initTest() {
        email = createEntity(em);
    }

    @Test
    @Transactional
    public void createEmail() throws Exception {
        int databaseSizeBeforeCreate = emailRepository.findAll().size();

        // Create the Email
        EmailDTO emailDTO = emailMapper.toDto(email);
        restEmailMockMvc.perform(post("/api/emails").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(emailDTO)))
            .andExpect(status().isCreated());

        // Validate the Email in the database
        List<Email> emailList = emailRepository.findAll();
        assertThat(emailList).hasSize(databaseSizeBeforeCreate + 1);
        Email testEmail = emailList.get(emailList.size() - 1);
        assertThat(testEmail.getAddress()).isEqualTo(DEFAULT_ADDRESS);
    }

    @Test
    @Transactional
    public void createEmailWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = emailRepository.findAll().size();

        // Create the Email with an existing ID
        email.setId(1L);
        EmailDTO emailDTO = emailMapper.toDto(email);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEmailMockMvc.perform(post("/api/emails").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(emailDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Email in the database
        List<Email> emailList = emailRepository.findAll();
        assertThat(emailList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllEmails() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        // Get all the emailList
        restEmailMockMvc.perform(get("/api/emails?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(email.getId().intValue())))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)));
    }
    
    @Test
    @Transactional
    public void getEmail() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        // Get the email
        restEmailMockMvc.perform(get("/api/emails/{id}", email.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(email.getId().intValue()))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS));
    }


    @Test
    @Transactional
    public void getEmailsByIdFiltering() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        Long id = email.getId();

        defaultEmailShouldBeFound("id.equals=" + id);
        defaultEmailShouldNotBeFound("id.notEquals=" + id);

        defaultEmailShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultEmailShouldNotBeFound("id.greaterThan=" + id);

        defaultEmailShouldBeFound("id.lessThanOrEqual=" + id);
        defaultEmailShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllEmailsByAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        // Get all the emailList where address equals to DEFAULT_ADDRESS
        defaultEmailShouldBeFound("address.equals=" + DEFAULT_ADDRESS);

        // Get all the emailList where address equals to UPDATED_ADDRESS
        defaultEmailShouldNotBeFound("address.equals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    public void getAllEmailsByAddressIsNotEqualToSomething() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        // Get all the emailList where address not equals to DEFAULT_ADDRESS
        defaultEmailShouldNotBeFound("address.notEquals=" + DEFAULT_ADDRESS);

        // Get all the emailList where address not equals to UPDATED_ADDRESS
        defaultEmailShouldBeFound("address.notEquals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    public void getAllEmailsByAddressIsInShouldWork() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        // Get all the emailList where address in DEFAULT_ADDRESS or UPDATED_ADDRESS
        defaultEmailShouldBeFound("address.in=" + DEFAULT_ADDRESS + "," + UPDATED_ADDRESS);

        // Get all the emailList where address equals to UPDATED_ADDRESS
        defaultEmailShouldNotBeFound("address.in=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    public void getAllEmailsByAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        // Get all the emailList where address is not null
        defaultEmailShouldBeFound("address.specified=true");

        // Get all the emailList where address is null
        defaultEmailShouldNotBeFound("address.specified=false");
    }
                @Test
    @Transactional
    public void getAllEmailsByAddressContainsSomething() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        // Get all the emailList where address contains DEFAULT_ADDRESS
        defaultEmailShouldBeFound("address.contains=" + DEFAULT_ADDRESS);

        // Get all the emailList where address contains UPDATED_ADDRESS
        defaultEmailShouldNotBeFound("address.contains=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    public void getAllEmailsByAddressNotContainsSomething() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        // Get all the emailList where address does not contain DEFAULT_ADDRESS
        defaultEmailShouldNotBeFound("address.doesNotContain=" + DEFAULT_ADDRESS);

        // Get all the emailList where address does not contain UPDATED_ADDRESS
        defaultEmailShouldBeFound("address.doesNotContain=" + UPDATED_ADDRESS);
    }


    @Test
    @Transactional
    public void getAllEmailsByEmployeeIsEqualToSomething() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);
        Employee employee = EmployeeResourceIT.createEntity(em);
        em.persist(employee);
        em.flush();
        email.setEmployee(employee);
        emailRepository.saveAndFlush(email);
        Long employeeId = employee.getId();

        // Get all the emailList where employee equals to employeeId
        defaultEmailShouldBeFound("employeeId.equals=" + employeeId);

        // Get all the emailList where employee equals to employeeId + 1
        defaultEmailShouldNotBeFound("employeeId.equals=" + (employeeId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultEmailShouldBeFound(String filter) throws Exception {
        restEmailMockMvc.perform(get("/api/emails?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(email.getId().intValue())))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)));

        // Check, that the count call also returns 1
        restEmailMockMvc.perform(get("/api/emails/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultEmailShouldNotBeFound(String filter) throws Exception {
        restEmailMockMvc.perform(get("/api/emails?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restEmailMockMvc.perform(get("/api/emails/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingEmail() throws Exception {
        // Get the email
        restEmailMockMvc.perform(get("/api/emails/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEmail() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        int databaseSizeBeforeUpdate = emailRepository.findAll().size();

        // Update the email
        Email updatedEmail = emailRepository.findById(email.getId()).get();
        // Disconnect from session so that the updates on updatedEmail are not directly saved in db
        em.detach(updatedEmail);
        updatedEmail
            .address(UPDATED_ADDRESS);
        EmailDTO emailDTO = emailMapper.toDto(updatedEmail);

        restEmailMockMvc.perform(put("/api/emails").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(emailDTO)))
            .andExpect(status().isOk());

        // Validate the Email in the database
        List<Email> emailList = emailRepository.findAll();
        assertThat(emailList).hasSize(databaseSizeBeforeUpdate);
        Email testEmail = emailList.get(emailList.size() - 1);
        assertThat(testEmail.getAddress()).isEqualTo(UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    public void updateNonExistingEmail() throws Exception {
        int databaseSizeBeforeUpdate = emailRepository.findAll().size();

        // Create the Email
        EmailDTO emailDTO = emailMapper.toDto(email);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEmailMockMvc.perform(put("/api/emails").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(emailDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Email in the database
        List<Email> emailList = emailRepository.findAll();
        assertThat(emailList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteEmail() throws Exception {
        // Initialize the database
        emailRepository.saveAndFlush(email);

        int databaseSizeBeforeDelete = emailRepository.findAll().size();

        // Delete the email
        restEmailMockMvc.perform(delete("/api/emails/{id}", email.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Email> emailList = emailRepository.findAll();
        assertThat(emailList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
