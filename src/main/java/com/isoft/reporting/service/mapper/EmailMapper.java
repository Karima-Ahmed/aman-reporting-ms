package com.isoft.reporting.service.mapper;


import com.isoft.reporting.domain.*;
import com.isoft.reporting.service.dto.EmailDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Email} and its DTO {@link EmailDTO}.
 */
@Mapper(componentModel = "spring", uses = {EmployeeMapper.class})
public interface EmailMapper extends EntityMapper<EmailDTO, Email> {

    @Mapping(source = "employee.id", target = "employeeId")
    EmailDTO toDto(Email email);

    @Mapping(source = "employeeId", target = "employee")
    Email toEntity(EmailDTO emailDTO);

    default Email fromId(Long id) {
        if (id == null) {
            return null;
        }
        Email email = new Email();
        email.setId(id);
        return email;
    }
}
