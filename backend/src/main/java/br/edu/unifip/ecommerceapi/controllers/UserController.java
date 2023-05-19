package br.edu.unifip.ecommerceapi.controllers;

import br.edu.unifip.ecommerceapi.dtos.AuthRequest;
import br.edu.unifip.ecommerceapi.dtos.UserDto;
import br.edu.unifip.ecommerceapi.models.Product;
import br.edu.unifip.ecommerceapi.models.User;
import br.edu.unifip.ecommerceapi.service.JwtService;
import br.edu.unifip.ecommerceapi.service.UserService;
import br.edu.unifip.ecommerceapi.utils.FileDownloadUtil;
import br.edu.unifip.ecommerceapi.utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<User>> getUsersIsActive() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findByActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable UUID id) {
        Optional<User> userOptional = userService.findById(id);
        return userOptional.<ResponseEntity<Object>>map(user -> ResponseEntity.status(HttpStatus.OK).body(user)).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found."));
    }

    @PostMapping("/register")
    public ResponseEntity<Object> saveUser(@Valid UserDto userDto, HttpServletRequest request) throws IOException {
        var user = new User();

        BeanUtils.copyProperties(userDto, user); // O que vai ser convertido para o quê vai ser convertido

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("image");

        if (multipartFile != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "user-images/";

            try {
                String filecode = FileUploadUtil.saveFile(fileName, uploadDir, multipartFile);
                user.setImage("/api/users/user-images/" + filecode);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image not accepted.");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user));
    }

    @DeleteMapping("/soft-delete/{id}")
    public ResponseEntity<Object> softDeleteUser(@PathVariable UUID id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        userService.softDelete(userOptional.get());;
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully.");
    }

    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<Object> hardDeleteUser(@PathVariable UUID id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        userService.hardDelete(userOptional.get());;
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully.");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id, HttpServletRequest request) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        Map<Object, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            objectMap.put(entry.getKey(), entry.getValue()[0]);
        }

        // Salvar a url da imagem em uma variável separada
        String imageUrl = null;
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("image");
        if (multipartFile != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "user-images/";

            try {
                String filecode = FileUploadUtil.saveFile(fileName, uploadDir, multipartFile);
                imageUrl = "/api/users/user-images/" + filecode;
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image not accepted.");
            }
        }

        // Adicionar a url da imagem ao objeto mapeado, se ela foi enviada
        if (imageUrl != null) {
            objectMap.put("image", imageUrl);
        }

        userService.partialUpdate(userOptional.get(), objectMap);
        return ResponseEntity.status(HttpStatus.OK).body(userOptional.get());
    }

    @GetMapping("/findByUsername")
    public ResponseEntity<Optional<User>> getUserByUsername(@Validated @RequestParam(value = "username") String username) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findByUsername(username));
    }

    @GetMapping("/user-images/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileCode") String fileCode) {
        FileDownloadUtil downloadUtil = new FileDownloadUtil();

        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource(fileCode, "user-images");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        MediaType contentType;

        if (Objects.equals(FilenameUtils.getExtension(resource.getFilename()), "jpg")) {
            contentType = MediaType.IMAGE_JPEG;
        } else {
            contentType = MediaType.IMAGE_PNG;
        }

        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(authRequest.getUsername());
            Map<String, String> response = new HashMap<String, String>();
            response.put("token", token);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user credentials@");
    }


}