package com.groo.service;

import com.groo.domain.group.Group;
import com.groo.domain.group.GroupRepository;
import com.groo.dto.GroupDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public List<GroupDto> findAll() {
        return groupRepository.findAll()
                .stream()
                .map(GroupDto::from)
                .toList();
    }

    @Transactional
    public GroupDto create(String name, String description) {
        Group group = new Group(name, description);
        return GroupDto.from(groupRepository.save(group));
    }
}
