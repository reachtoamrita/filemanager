package com.arani.filemanager.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@Slf4j
public class FileUploaderApi {

    String baseFileDirectory = "C:/Users/arani/OneDrive - Itron/Amrita/Projects/PersonalProjects/filemanager/out";

    //how to increase the size of the file. Currently it is 1048576 bytes which is 1 MB of file
    // I have tested it with jpeg xlsx & txt

    @PostMapping(value="saveFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Why form data
    public String saveFile(@RequestParam(value="file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        StringBuilder path = new StringBuilder(baseFileDirectory);
        path.append("/");
        path.append(fileName);
        File outputFile = new File(path.toString()); // Rectify it to get the base path of the application
        outputFile.createNewFile(); //Checks if the file exists, otherwise creats a new file and the operation is atomic
        try(FileOutputStream outputStream = new FileOutputStream(outputFile))
        {
            outputStream.write(file.getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "File has uploaded successfully";
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        
        Resource resource = null;

        // Try to determine file's content type
        String contentType = null;
        try {
            resource = getFile(fileName);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }
        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    private Resource getFile(String fileName) throws IOException {
        // Load file as Resource
        Resource resource;
        try {
            Path fileStorageLocation = Paths.get(baseFileDirectory).toAbsolutePath().normalize();
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                log.info("Could not determine file type.");
                throw new IOException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            log.info("Could not determine file type.");
            throw new IOException("File not found " + fileName, ex);
        }
    }
}
