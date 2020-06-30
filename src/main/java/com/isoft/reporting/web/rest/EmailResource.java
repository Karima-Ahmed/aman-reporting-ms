package com.isoft.reporting.web.rest;

import com.isoft.reporting.service.EmailService;
import com.isoft.reporting.web.rest.errors.BadRequestAlertException;
import com.isoft.reporting.service.dto.EmailDTO;
import com.isoft.reporting.service.dto.EmailCriteria;
import com.isoft.reporting.service.EmailQueryService;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.isoft.reporting.domain.Email}.
 */
@RestController
@RequestMapping("/api")
public class EmailResource {

    private final Logger log = LoggerFactory.getLogger(EmailResource.class);

    private static final String ENTITY_NAME = "amanReportingMsEmail";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EmailService emailService;

    private final EmailQueryService emailQueryService;

    public EmailResource(EmailService emailService, EmailQueryService emailQueryService) {
        this.emailService = emailService;
        this.emailQueryService = emailQueryService;
    }

    /**
     * {@code POST  /emails} : Create a new email.
     *
     * @param emailDTO the emailDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new emailDTO, or with status {@code 400 (Bad Request)} if the email has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/emails")
    public ResponseEntity<EmailDTO> createEmail(@RequestBody EmailDTO emailDTO) throws URISyntaxException {
        log.debug("REST request to save Email : {}", emailDTO);
        if (emailDTO.getId() != null) {
            throw new BadRequestAlertException("A new email cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EmailDTO result = emailService.save(emailDTO);
        return ResponseEntity.created(new URI("/api/emails/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /emails} : Updates an existing email.
     *
     * @param emailDTO the emailDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated emailDTO,
     * or with status {@code 400 (Bad Request)} if the emailDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the emailDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/emails")
    public ResponseEntity<EmailDTO> updateEmail(@RequestBody EmailDTO emailDTO) throws URISyntaxException {
        log.debug("REST request to update Email : {}", emailDTO);
        if (emailDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        EmailDTO result = emailService.save(emailDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, emailDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /emails} : get all the emails.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of emails in body.
     */
    @GetMapping("/emails")
    public ResponseEntity<List<EmailDTO>> getAllEmails(EmailCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Emails by criteria: {}", criteria);
        Page<EmailDTO> page = emailQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /emails/count} : count all the emails.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/emails/count")
    public ResponseEntity<Long> countEmails(EmailCriteria criteria) {
        log.debug("REST request to count Emails by criteria: {}", criteria);
        return ResponseEntity.ok().body(emailQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /emails/:id} : get the "id" email.
     *
     * @param id the id of the emailDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the emailDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/emails/{id}")
    public ResponseEntity<EmailDTO> getEmail(@PathVariable Long id) {
        log.debug("REST request to get Email : {}", id);
        Optional<EmailDTO> emailDTO = emailService.findOne(id);
        return ResponseUtil.wrapOrNotFound(emailDTO);
    }

    /**
     * {@code DELETE  /emails/:id} : delete the "id" email.
     *
     * @param id the id of the emailDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/emails/{id}")
    public ResponseEntity<Void> deleteEmail(@PathVariable Long id) {
        log.debug("REST request to delete Email : {}", id);
        emailService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
