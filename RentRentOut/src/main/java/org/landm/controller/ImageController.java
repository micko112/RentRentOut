package org.landm.controller;

import jakarta.mail.Multipart;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/images")
public class ImageController {

    public final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public ImageController() {

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory where the uploaded files will be stored ", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadImages(@RequestParam("files")MultipartFile[] files){
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file: files){
            try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(fileName);

                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);


            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

            fileUrls.add(fileDownloadUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok(fileUrls);
    }
}
