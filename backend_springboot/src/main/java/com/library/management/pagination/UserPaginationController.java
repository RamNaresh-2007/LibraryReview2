package com.library.management.pagination;

import com.library.management.User;
import com.library.management.UserRepository;
import com.library.management.dto.UserPaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/getallusers")
@PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
public class UserPaginationController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{page}/{limit}")
    public UserPaginationResponse getAllUsersPaginated(@PathVariable int page, @PathVariable int limit) {
        // In Spring Data JPA, page index is 0-based
        Pageable pageable = PageRequest.of(page, limit);
        Page<User> userPage = userRepository.findAll(pageable);

        return new UserPaginationResponse(userPage.getContent(), userPage.getTotalElements());
    }
}
