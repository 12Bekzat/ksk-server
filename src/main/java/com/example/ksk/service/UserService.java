package com.example.ksk.service;

import com.example.ksk.dto.RegUserDto;
import com.example.ksk.entity.Role;
import com.example.ksk.entity.User;
import com.example.ksk.repository.RoleRepository;
import com.example.ksk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("Пользователь %s не найден", username)
        ));

        org.springframework.security.core.userdetails.User user1 = new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), !user.isBanned(), true, true, true,
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList()));

        return user1;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createNewUser(RegUserDto regUserDto) {
        User user = new User();
        System.out.println(regUserDto.getUser());
        user.setUsername(regUserDto.getUsername());
        user.setPassword(passwordEncoder.encode(regUserDto.getPassword()));
        user.setFullName(regUserDto.getFullName());
        user.setBanned(false);
        user.setEmail(regUserDto.getEmail());
        user.setRoles(List.of(roleRepository.findByName(regUserDto.getRole()).get()));
        return userRepository.save(user);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void removeUser(Long id) {
        userRepository.deleteById(id);
    }

    public void saveUserWithEncrypt(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void setRoles() {
        Role admin = new Role();
        admin.setName("ROLE_ADMIN");
        Role user = new Role();
        user.setName("ROLE_USER");
        Role ksk = new Role();
        user.setName("ROLE_KSK");
        roleRepository.saveAll(List.of(admin, user, ksk));
    }

    public void createAdmin() {
        User admin = new User();
        admin.setFullName("Admin");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin1234"));
        admin.setRoles(List.of(roleRepository.findByName("ROLE_ADMIN").get()));
        userRepository.save(admin);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
