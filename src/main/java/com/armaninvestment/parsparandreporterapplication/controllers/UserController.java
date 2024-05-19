package com.armaninvestment.parsparandreporterapplication.controllers;

import com.armaninvestment.parsparandreporterapplication.dtos.CustomerSelect;
import com.armaninvestment.parsparandreporterapplication.dtos.UserDto;
import com.armaninvestment.parsparandreporterapplication.dtos.UserSelectDto;
import com.armaninvestment.parsparandreporterapplication.searchForms.UserSearch;
import com.armaninvestment.parsparandreporterapplication.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping(path = {"/", ""})
    public ResponseEntity<Page<UserDto>> getAllUsersByCriteria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            UserSearch search) {
        Page<UserDto> users = userService.findUserByCriteria(search, page, size, sortBy, order);
        return ResponseEntity.ok(users);
    }
    @GetMapping(path = "/select")
    public ResponseEntity<List<UserSelectDto>> findAllUserSelect(
            @RequestParam(required = false) String searchQuery) {
        List<UserSelectDto> users = userService.findAllUserSelect(searchQuery);
        return ResponseEntity.ok(users);
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PutMapping(path = {"/{id}"})
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer id, @RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download-all-users.xlsx")
    public ResponseEntity<byte[]> downloadAllUsersExcel() throws IOException {
        byte[] excelData = userService.exportUsersToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("all_users.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importUsersFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            String list = userService.importUsersFromExcel(file);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import users from Excel file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing Excel file: " + e.getMessage());
        }
    }
}
