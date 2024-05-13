package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.UserDto;
import com.armaninvestment.parsparandreporterapplication.entities.User;
import com.armaninvestment.parsparandreporterapplication.mappers.UserMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.UserRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.UserSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.UserSpecification;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataExporter;
import com.armaninvestment.parsparandreporterapplication.utils.ExcelDataImporter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Page<UserDto> findUserByCriteria(UserSearch search, int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Specification<User> specification = UserSpecification.bySearchCriteria(search);
        return userRepository.findAll(specification, pageRequest)
                .map(userMapper::toDto);
    }

    public UserDto createUser(UserDto userDto) {
        var userEntity = userMapper.toEntity(userDto);
        var savedUser = userRepository.save(userEntity);
        return userMapper.toDto(savedUser);
    }

    public UserDto getUserById(Integer id) {
        var userEntity = userRepository.findById(id).orElseThrow();
        return userMapper.toDto(userEntity);
    }

    public UserDto updateUser(Integer id, UserDto userDto) {
        var userEntity = userRepository.findById(id).orElseThrow();
        User partialUpdate = userMapper.partialUpdate(userDto, userEntity);
        var updatedUser = userRepository.save(partialUpdate);
        return userMapper.toDto(updatedUser);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public String importUsersFromExcel(MultipartFile file) throws IOException {
        List<UserDto> userDtos = ExcelDataImporter.importData(file, UserDto.class);
        List<User> users = userDtos.stream().map(userMapper::toEntity).collect(Collectors.toList());
        userRepository.saveAll(users);
        return users.size() + " users have been imported successfully.";
    }

    public byte[] exportUsersToExcel() throws IOException {
        List<UserDto> userDtos = userRepository.findAll().stream().map(userMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(userDtos, UserDto.class);
    }
}
