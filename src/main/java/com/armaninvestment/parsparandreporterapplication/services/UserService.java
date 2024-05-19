package com.armaninvestment.parsparandreporterapplication.services;

import com.armaninvestment.parsparandreporterapplication.dtos.CustomerSelect;
import com.armaninvestment.parsparandreporterapplication.dtos.UserDto;
import com.armaninvestment.parsparandreporterapplication.dtos.UserSelectDto;
import com.armaninvestment.parsparandreporterapplication.entities.Customer;
import com.armaninvestment.parsparandreporterapplication.entities.User;
import com.armaninvestment.parsparandreporterapplication.mappers.UserMapper;
import com.armaninvestment.parsparandreporterapplication.repositories.UserRepository;
import com.armaninvestment.parsparandreporterapplication.searchForms.UserSearch;
import com.armaninvestment.parsparandreporterapplication.specifications.CustomerSpecification;
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
    public List<UserSelectDto> findAllUserSelect(String searchParam) {
        Specification<User> specification = UserSpecification.getSelectSpecification(searchParam);
        return userRepository
                .findAll(specification)
                .stream()
                .map(userMapper::toSelectDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalStateException("نام کاربری قبلاً استفاده شده است.");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalStateException("ایمیل قبلاً استفاده شده است.");
        }
        var userEntity = userMapper.toEntity(userDto);
        var savedUser = userRepository.save(userEntity);
        return userMapper.toDto(savedUser);
    }

    public UserDto getUserById(Integer id) {
        var userEntity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("کاربر با شناسه " + id + " پیدا نشد."));
        return userMapper.toDto(userEntity);
    }

    public UserDto updateUser(Integer id, UserDto userDto) {
        var existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("کاربر با شناسه " + id + " پیدا نشد."));

        if (userRepository.existsByUsernameAndIdNot(userDto.getUsername(), id)) {
            throw new IllegalStateException("نام کاربری دیگری با این نام قبلاً استفاده شده است.");
        }
        if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id)) {
            throw new IllegalStateException("ایمیل دیگری با این ایمیل قبلاً استفاده شده است.");
        }

        userMapper.partialUpdate(userDto, existingUser);
        var updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("کاربر با شناسه " + id + " پیدا نشد.");
        }
        userRepository.deleteById(id);
    }

    public String importUsersFromExcel(MultipartFile file) throws IOException {
        List<UserDto> userDtos = ExcelDataImporter.importData(file, UserDto.class);
        List<User> users = userDtos.stream()
                .map(userMapper::toEntity)
                .collect(Collectors.toList());

        for (User user : users) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new IllegalStateException("نام کاربری " + user.getUsername() + " قبلاً استفاده شده است.");
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalStateException("ایمیل " + user.getEmail() + " قبلاً استفاده شده است.");
            }
        }

        userRepository.saveAll(users);
        return users.size() + " کاربر با موفقیت وارد شد.";
    }

    public byte[] exportUsersToExcel() throws IOException {
        List<UserDto> userDtos = userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        return ExcelDataExporter.exportData(userDtos, UserDto.class);
    }
}
