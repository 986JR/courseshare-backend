package backend.courseshare.service;

import backend.courseshare.dto.user.UserPatchDTO;
import backend.courseshare.dto.user.UserProfileDTO;
import backend.courseshare.dto.user.UserUpdateDTO;
import backend.courseshare.entity.Users;
import backend.courseshare.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class Userservice {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private static  final String CHARS="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int MAX_RETRIES =10;


    public Userservice(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public  Users createUser(Users user) {
        //I must validate Email and Usersname to be uniques in the Table
        if(user==null) {
            throw new IllegalArgumentException("User must not be null");
        }

        String rawEmail = user.getEmail()==null ? "" : user.getEmail().trim().toLowerCase();
        String rawUsername = user.getUsername() == null ? "" : user.getUsername().trim();
        String rawPassword = user.getPasswordHash() == null ? "" : user.getPasswordHash();

        //Validating
        if(rawEmail.isBlank() || !isValidEmail(rawEmail)){
            throw new IllegalArgumentException("Invalid email format");
        }

        if(rawUsername.isBlank()) {
            throw new IllegalArgumentException("username must not be empty");

        }

        if(rawPassword.length()<6){
            throw new IllegalArgumentException("Password must be atleasr 6 characters");
        }

        //Checkimh the usernames and emails to be unique
        if(repo.existsByEmail(rawEmail)) {
            throw new IllegalArgumentException("Email is already Taken");
        }

        if(repo.existsByUsername(rawUsername)) {
            throw new IllegalArgumentException("User name is already in use");
        }



        //If no uniques publicId, I should create
        if(user.getPublicId() == null || user.getPublicId().isBlank()) {
            user.setPublicId(generateUniquePublicId());
        }
        //Also I have to hash the Pwd

        String hashed;
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder bcrypt =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        hashed = bcrypt.encode(rawPassword);

        user.setEmail(rawEmail);
        user.setUsername(rawUsername);
        user.setPasswordHash(hashed);

        return repo.save(user);
    }

    public String generateUniquePublicId() {
        String candidate;
        int attempts = 0;
        do {
            if (++attempts > MAX_RETRIES) {
                throw new IllegalStateException("Unable to generate unique public ID after "+MAX_RETRIES+" attempts");

            }
            candidate=generateCustomId();
        }while(repo.existsByPublicId(candidate));
        return candidate;
    }

    public String generateCustomId() {
        String yearSuffix = String.valueOf(Year.now().getValue()).substring(2);
        return "ID"+yearSuffix+"-"+randomPart(3);
    }

    public String randomPart(int length) {
        StringBuilder sb = new StringBuilder(length);
        var rnd = ThreadLocalRandom.current();
        for (int i=0; i<length; i++) {
            sb.append(CHARS.charAt(rnd.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
    //validating methods
//For Email
    private boolean isValidEmail(String email) {
        if(email==null) return false;

        final java.util.regex.Pattern EMAIL_PARTERN = java.util.regex.Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",java.util.regex.Pattern.CASE_INSENSITIVE);
        return EMAIL_PARTERN.matcher(email).matches();
    }


    //For user DTOs

    //Adminis
    @Transactional(readOnly = true)
    public List<UserProfileDTO> getAllUsers() {
        return repo.findAll().stream()
                .map(u -> getUserByPublicId(u.getPublicId()))  // use new method
                .collect(Collectors.toList());
    }


    //Lnk to DTO of Profile
    /*private UserProfileDTO toProfileDto(Users u) {
        return new UserProfileDTO(
                u.getPublicId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole() == null ? null : u.getRole().name(),
                u.getCreated_At()
        );
    }*/

    //Find user by Publicc Id
    /*@Transactional(readOnly = true)
    public UserProfileDTO getUserByPublicId(String publicId) {
        Users u = repo.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toProfileDto(u);
    }*/

    public UserProfileDTO getUserByPublicId(String publicId) {
        return repo.getFullProfile(publicId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    //PUT
    @Transactional
    public UserProfileDTO updateUser(String publicId, UserUpdateDTO dto) {
        Users u = repo.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not Found"));

        String newEmail = dto.email().trim().toLowerCase();
        String newUsername = dto.username().trim();

        if(!newEmail.equalsIgnoreCase(u.getEmail()) && repo.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use ");
        }

        if(!newUsername.equals(u.getUsername()) && repo.existsByUsername(newUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Username is Taken");
        }

        u.setEmail(newEmail);
        u.setUsername(newUsername);
        u.setPasswordHash(passwordEncoder.encode(dto.password()));

        //Critical
        if(dto.role() !=null) {
            try {
                u.setRole(u.getRole());
            }
            catch(IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid Role");
            }
        }

        try {
            Users saved = repo.save(u);
            return getUserByPublicId(saved.getPublicId());
        }
        catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Contraint Violation ");
        }
    }

    //Patch
    @Transactional
    public UserProfileDTO patchUser(String publicId, UserPatchDTO dto) {
        Users u = repo.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not Found"));

        if(dto.email() != null && !dto.email().isBlank()) {
            String newEmail = dto.email().trim().toLowerCase();
            if(newEmail.equalsIgnoreCase(u.getEmail()) && repo.existsByEmail(newEmail)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email Already in use ");
            }

            u.setEmail(newEmail);
        }

        if(dto.username() != null && !dto.username().isBlank()) {
            String newUsername =  dto.username().trim();
            if(newUsername.equals(u.getUsername()) && repo.existsByUsername(newUsername)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT," Username Already in Use");
            }

            u.setUsername(newUsername);
        }

        if(dto.password() != null && !dto.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(dto.password()));
        }

        if(dto.role() != null) {
            try {
                u.setRole(u.getRole());

            }
            catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid Role");
            }

        }

        try {
            Users saved = repo.save(u);
            return getUserByPublicId(saved.getPublicId());

        }catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Conflict Violation");
        }

    }

    //Delete
    @Transactional
    public void deleteUserByPublicId(String publicId) {
        Users u = repo.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User Not Found"));
        repo.delete(u);
    }


    public boolean isAdmin(Users user) {
        return user != null && user.getRole() != null && user.getRole().toString().equalsIgnoreCase("ROLE_ADMIN");
    }

    public Users findByPublicIdOrThrow(String userPublicId) {
        return repo.findByPublicId(userPublicId).orElseThrow(() -> new RuntimeException("user with '"+userPublicId + "' not Found"));
    }
}



